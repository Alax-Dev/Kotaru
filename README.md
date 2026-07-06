# Kotaru 📖

A modern manga reader app for Android with iOS-style design, auto captcha solver, and 19 curated sources.

## ✨ Features

- **iOS-Style Design** — Clean, modern UI inspired by Apple's design language
- **Auto CAPTCHA Solver** — Automatically solves Cloudflare challenges, Turnstile, and managed challenges
- **19 Curated Sources** — Only the sources you need, no bloat
- **Dark Mode** — Full dark mode support with iOS-style colors
- **Offline Reading** — Download manga for offline reading
- **Tracking** — Track your reading progress across multiple services

## 📱 Screenshots

| Home | Detail | Reader |
|------|--------|--------|
| iOS-style grid/list | Clean info table | Immersive reading |

## 📚 Supported Sources

| Source | Domain | Content |
|--------|--------|---------|
| MangaGeko | mgeko.cc | Manga |
| OmegaScans | omegascans.org | Manhwa |
| Manhwa18.cc | manhwa18.cc | Manhwa |
| Manhwa18.net | manhwa18.net | Manhwa |
| Manhwa18.com | manhwa18.com | Manhwa |
| TooMicsEn | toomics.com/en | Manhwa |
| ToonGod | toongod.org | Manhwa |
| Toonily | toonily.com | Manhwa |
| Toonily.Me | toonily.me | Manhwa |
| HotComics | hotcomics.me/en | Manhwa |
| CoComic | cocomic.co | Manhwa |
| KissManga | kissmanga.in | Manga |
| LikeManga | likemanga.ink | Manga |
| kaliscan.io | kaliscan.io | Manga |
| ManhwaDen | manhwaden.com | Manhwa |
| MadaraDex | madaradex.org | Manhwa |
| RavenScans | ravenscans.org | Manga |
| MgRead | mgread.io | Manga/Manhwa |
| HeyToon | toonhey.com | Manhwa |

## 🛡️ Auto CAPTCHA Solver

Kotaru includes an intelligent captcha solver that handles:

| Challenge Type | Auto-Solve | Method |
|----------------|------------|--------|
| Cloudflare JS | ✅ | WebView JS injection |
| Cloudflare Turnstile | ✅ | Auto-completion wait |
| Cloudflare Managed | ✅ | Redirect detection |
| reCAPTCHA v2 | ⚠️ | Checkbox attempt |
| hCaptcha | ⚠️ | Checkbox attempt |

**Settings:** Per-source toggle in Settings → Source Settings → "Auto CAPTCHA Solver"

## 🎨 Design System

Kotaru uses an iOS-inspired design system:

- **Colors** — iOS system colors (systemBlue, systemRed, etc.)
- **Typography** — Clean sans-serif with iOS-style sizing
- **Components** — Rounded cards, chevron accessories, Settings-style tables
- **Navigation** — iOS-style bottom tab bar
- **Dark Mode** — Full iOS dark mode support

## 🏗️ Building

### Prerequisites

- Android Studio Arctic Fox or later
- JDK 17+
- Android SDK 34+

### Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/Alaxroy121/Kotaru.git
   ```

2. Open in Android Studio

3. Sync Gradle and build

4. Run on device or emulator

## 📁 Project Structure

```
Kotaru/
├── app/                          # Main application module
│   └── src/main/
│       ├── kotlin/               # Kotlin source code
│       │   └── org/koitharu/kotatsu/
│       │       ├── core/         # Core functionality
│       │       │   ├── network/  # Network & captcha handling
│       │       │   ├── parser/   # Manga parser framework
│       │       │   └── prefs/    # Settings & preferences
│       │       ├── details/      # Manga detail screen
│       │       ├── main/         # Main activity
│       │       └── reader/       # Manga reader
│       └── res/                  # Resources
│           ├── layout/           # XML layouts (iOS-style)
│           ├── values/           # Colors, themes, strings
│           └── drawable/         # Icons & drawables
└── gradle/                       # Gradle configuration
```

## 🔧 Technical Details

- **Language:** Kotlin
- **UI:** XML layouts with Material3 + iOS customizations
- **Architecture:** MVVM with Hilt dependency injection
- **Network:** OkHttp + custom cookie management
- **Image Loading:** Coil
- **Database:** Room
- **Parsing:** Custom parser framework with KSP code generation

## 📄 License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## 🙏 Credits

- Original [Kotatsu](https://github.com/KotatsuApp/Kotatsu) team
- [kotatsu-parsers](https://github.com/KotatsuApp/kotatsu-parsers) contributors
- iOS design inspiration from Apple's Human Interface Guidelines

## 📞 Support

- Create an issue for bug reports
- Pull requests are welcome
- Star the repo if you find it useful!

---

**Made with ❤️ for manga readers**
