package org.koitharu.kotatsu.core.network.webview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebView
import org.koitharu.kotatsu.core.network.cookies.MutableCookieJar
import org.koitharu.kotatsu.parsers.network.CloudFlareHelper
import kotlin.coroutines.Continuation

/**
 * Enhanced CloudFlare captcha resolver that automatically solves
 * JS challenges, Turnstile, and managed challenges.
 */
class AutoCaptchaContinuationClient(
	private val cookieJar: MutableCookieJar,
	private val targetUrl: String,
	continuation: Continuation<Unit>,
) : ContinuationResumeWebViewClient(continuation) {

	private val oldClearance = CloudFlareHelper.getClearanceCookie(cookieJar, targetUrl)
	private var challengeDetected = false
	private var retryCount = 0
	private val maxRetries = 3

	override fun onPageFinished(view: WebView?, url: String?) {
		super.onPageFinished(view, url)
		view ?: return
		// Try to auto-solve the challenge after page loads
		injectAutoSolver(view)
		checkClearance(view)
	}

	override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
		super.onPageStarted(view, url, favicon)
		challengeDetected = true
	}

	@SuppressLint("SetJavaScriptEnabled")
	private fun injectAutoSolver(view: WebView) {
		// JavaScript to auto-solve Cloudflare challenges
		val script = """
		(function() {
			// Check if this is a Cloudflare challenge page
			var isChallenge = document.querySelector('#challenge-running') ||
				document.querySelector('#challenge-form') ||
				document.querySelector('.cf-browser-verification') ||
				document.querySelector('#cf-challenge-running') ||
				document.querySelector('input[name="cf-turnstile-response"]') ||
				document.querySelector('.cf-turnstile') ||
				document.querySelector('#challenge-stage') ||
				document.title.includes('Just a moment') ||
				document.title.includes('Attention Required');

			if (!isChallenge) return;

			// Try to find and click the verify button if it exists
			var verifyBtn = document.querySelector('#challenge-stage button') ||
				document.querySelector('.cf-turnstile input[type="submit"]') ||
				document.querySelector('input[type="submit"]') ||
				document.querySelector('button[type="submit"]');

			if (verifyBtn) {
				setTimeout(function() {
					verifyBtn.click();
				}, 1500);
			}

			// For Turnstile challenge - wait for it to auto-complete
			var turnstile = document.querySelector('.cf-turnstile');
			if (turnstile) {
				// Turnstile usually auto-solves, just wait
				var checkInterval = setInterval(function() {
					var response = document.querySelector('input[name="cf-turnstile-response"]');
					if (response && response.value) {
						clearInterval(checkInterval);
						var form = document.querySelector('#challenge-form');
						if (form) {
							form.submit();
						}
					}
				}, 500);

				// Timeout after 10 seconds
				setTimeout(function() {
					clearInterval(checkInterval);
				}, 10000);
			}

			// For managed challenge (auto-solves)
			var managedChallenge = document.querySelector('#challenge-running');
			if (managedChallenge) {
				// These auto-solve, just wait for redirect
			}

			// Try to submit the form if it has a valid response
			var form = document.querySelector('#challenge-form');
			if (form) {
				var responseInput = form.querySelector('input[name="cf-turnstile-response"]') ||
					form.querySelector('input[name="g-recaptcha-response"]') ||
					form.querySelector('input[name="h-captcha-response"]');

				if (responseInput && responseInput.value) {
					setTimeout(function() {
						form.submit();
					}, 500);
				}
			}
		})();
		""".trimIndent()

		view.evaluateJavascript(script, null)
	}

	private fun checkClearance(view: WebView?) {
		val clearance = CloudFlareHelper.getClearanceCookie(cookieJar, targetUrl)
		if (clearance != null && clearance != oldClearance) {
			resumeContinuation(view)
		}
	}
}
