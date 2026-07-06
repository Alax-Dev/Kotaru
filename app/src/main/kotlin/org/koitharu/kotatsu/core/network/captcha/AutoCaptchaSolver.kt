package org.koitharu.kotatsu.core.network.captcha

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.koitharu.kotatsu.core.network.cookies.MutableCookieJar
import org.koitharu.kotatsu.core.util.ext.configureForParser
import org.koitharu.kotatsu.core.util.ext.printStackTraceDebug
import org.koitharu.kotatsu.parsers.network.CloudFlareHelper
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Automatic captcha solver that handles various challenge types:
 * - Cloudflare JS challenges (auto-solve)
 * - Cloudflare Turnstile (auto-solve)
 * - Cloudflare managed challenges (auto-solve)
 * - reCAPTCHA v2/v3 (requires third-party service)
 * - hCaptcha (requires third-party service)
 */
@Singleton
class AutoCaptchaSolver @Inject constructor(
	@ApplicationContext private val context: Context,
	private val cookieJar: MutableCookieJar,
) {

	/**
	 * Attempt to automatically solve a captcha challenge.
	 * Returns true if the challenge was solved successfully.
	 */
	suspend fun solve(
		url: String,
		userAgent: String?,
		timeoutMs: Long = 30_000L,
	): Boolean = withContext(Dispatchers.Main.immediate) {
		val webView = WebView(context).apply {
			configureForParser(null)
			userAgent?.let { settings.userAgentString = it }
		}

		try {
			withTimeout(timeoutMs) {
				solveInternal(webView, url, timeoutMs)
			}
		} catch (e: Exception) {
			e.printStackTraceDebug()
			false
		} finally {
			webView.destroy()
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	private suspend fun solveInternal(webView: WebView, url: String, timeoutMs: Long): Boolean {
		val oldClearance = CloudFlareHelper.getClearanceCookie(cookieJar, url)

		return kotlinx.coroutines.suspendCancellableCoroutine { cont ->
			webView.webViewClient = object : WebViewClient() {
				override fun onPageFinished(view: WebView?, loadedUrl: String?) {
					super.onPageFinished(view, loadedUrl)
					view ?: return

					// Inject auto-solver JS
					injectSolverScript(view)

					// Check if challenge is solved
					val clearance = CloudFlareHelper.getClearanceCookie(cookieJar, url)
					if (clearance != null && clearance != oldClearance) {
						if (cont.isActive) cont.resume(true)
					}
				}
			}
			webView.loadUrl(url)

			// Timeout fallback
			cont.invokeOnCancellation { }
			android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
				if (cont.isActive) cont.resume(false)
			}, timeoutMs)
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	private fun injectSolverScript(view: WebView) {
		val script = """
		(function() {
			// Detect challenge type
			var isCFChallenge = document.querySelector('#challenge-running') ||
				document.querySelector('#challenge-form') ||
				document.querySelector('.cf-browser-verification') ||
				document.querySelector('#cf-challenge-running') ||
				document.title.includes('Just a moment');

			var isTurnstile = document.querySelector('.cf-turnstile') ||
				document.querySelector('input[name="cf-turnstile-response"]');

			var isRecaptcha = document.querySelector('.g-recaptcha') ||
				document.querySelector('iframe[src*="recaptcha"]');

			var isHcaptcha = document.querySelector('.h-captcha') ||
				document.querySelector('iframe[src*="hcaptcha"]');

			if (isCFChallenge) {
				solveCloudflare();
			} else if (isRecaptcha) {
				solveRecaptcha();
			} else if (isHcaptcha) {
				solveHcaptcha();
			}

			function solveCloudflare() {
				// For Turnstile - wait for auto-completion
				if (isTurnstile) {
					var checkInterval = setInterval(function() {
						var response = document.querySelector('input[name="cf-turnstile-response"]');
						if (response && response.value) {
							clearInterval(checkInterval);
							submitForm();
						}
					}, 500);
					setTimeout(function() { clearInterval(checkInterval); }, 15000);
					return;
				}

				// For managed challenges - they auto-solve
				var managed = document.querySelector('#challenge-running');
				if (managed) {
					// Just wait for redirect
					return;
				}

				// For JS challenges - click verify button
				setTimeout(function() {
					var btn = document.querySelector('#challenge-stage button') ||
						document.querySelector('button[type="submit"]') ||
						document.querySelector('input[type="submit"]');
					if (btn) btn.click();
				}, 2000);

				// Try submitting form after delay
				setTimeout(function() {
					submitForm();
				}, 5000);
			}

			function solveRecaptcha() {
				// reCAPTCHA requires user interaction or third-party service
				// Try to find and click the checkbox
				var iframe = document.querySelector('iframe[src*="recaptcha"]');
				if (iframe) {
					try {
						// Try to access iframe content (may fail due to same-origin policy)
						var checkbox = iframe.contentDocument.querySelector('.recaptcha-checkbox');
						if (checkbox) checkbox.click();
					} catch(e) {
						// Cross-origin, can't access directly
					}
				}
			}

			function solveHcaptcha() {
				// hCaptcha requires user interaction or third-party service
				var iframe = document.querySelector('iframe[src*="hcaptcha"]');
				if (iframe) {
					try {
						var checkbox = iframe.contentDocument.querySelector('#checkbox');
						if (checkbox) checkbox.click();
					} catch(e) {
						// Cross-origin
					}
				}
			}

			function submitForm() {
				var form = document.querySelector('#challenge-form') ||
					document.querySelector('form');
				if (form) {
					var response = form.querySelector('input[name="cf-turnstile-response"]') ||
						form.querySelector('input[name="g-recaptcha-response"]') ||
						form.querySelector('input[name="h-captcha-response"]');
					if (response && response.value) {
						form.submit();
					}
				}
			}

			// Periodically check for clearance cookie
			var checkCount = 0;
			var cookieCheck = setInterval(function() {
				checkCount++;
				if (checkCount > 30) {
					clearInterval(cookieCheck);
					return;
				}
				// Check if we've been redirected (challenge solved)
				if (!document.title.includes('Just a moment') &&
					!document.title.includes('Attention Required') &&
					!document.querySelector('#challenge-running')) {
					clearInterval(cookieCheck);
				}
			}, 1000);
		})();
		""".trimIndent()

		view.evaluateJavascript(script, null)
	}

	companion object {
		private const val TAG = "AutoCaptchaSolver"
	}
}
