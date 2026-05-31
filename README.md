<div align="center">

# InstaLoaderApp

### Bulk Instagram media downloader for Android.

Download profiles, posts, reels, and hashtags using [`instaloader`](https://github.com/instaloader/instaloader) — with live progress, full CLI options, and optional login.

![GitHub release](https://img.shields.io/github/v/release/AzeemIdrisi/InstaLoaderApp)
![GitHub Repo stars](https://img.shields.io/github/stars/AzeemIdrisi/InstaLoaderApp?style=social)
![GitHub all releases](https://img.shields.io/github/downloads/azeemidrisi/instaloaderapp/total)

</div>

## Overview

**InstaLoaderApp** uses Jetpack Compose, foreground downloads with notifications, and instaloader 4.15.1 via Chaquopy. Files save to **`Downloads/InstaLoaderApp/`** by default (changeable in Settings → Storage).

> [!TIP]
> Login is optional — only needed for private content, stories, highlights, or comments.

---

## Screenshots

<div align="center">

<img height="600px" src="screenshots/1.png" />
<img height="600px" src="screenshots/2.png" />
<img height="600px" src="screenshots/3.png" />

</div>

---

## Features

- Profiles, single post/reel URLs, and hashtags
- Live progress (done / left / failed) + notification
- Full Instaloader settings (media, metadata, filters, naming, resume)
- Optional login with 2FA; on-device only (session file in private app storage, username in encrypted settings)
- Public download folder + custom location presets

---

## Installation

[<img src="https://github.com/machiav3lli/oandbackupx/blob/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png" alt="Get it on GitHub" height="80">](https://github.com/AzeemIdrisi/InstaLoaderApp/releases/latest)

Install the APK, then on first download grant **All files access** (Android 11+) and **Notifications** (Android 13+) when prompted.

---

## Usage

| Input           | Example                     | Output folder                             |
| --------------- | --------------------------- | ----------------------------------------- |
| Username        | `instagram`                 | `Downloads/InstaLoaderApp/<username>/`    |
| Post / reel URL | `https://instagram.com/p/…` | `Downloads/InstaLoaderApp/posts/`         |
| Hashtag         | `#travel`                   | `Downloads/InstaLoaderApp/hashtag_<tag>/` |

1. Enter a username, URL, or hashtag → **Start download**.
2. For posts/reels, copy the link from Instagram and paste into the app.

> [!WARNING]
> Large profiles can take a long time. Instagram may restrict accounts used for automated downloading — use a secondary account at your own risk if you log in.

---

## Build from source

```
git clone https://github.com/AzeemIdrisi/InstaLoaderApp.git
cd InstaLoaderApp
```

Set `sdk.dir` in `local.properties`, then:

```
./gradlew installDebug
```

Requires Android Studio, JDK 17, and Python 3 for Chaquopy. See `app/build.gradle` for `buildPython` path.

---

## Disclaimer

Use responsibly. Download only content you may access. The developer is not responsible for misuse. Credentials never leave your device.

Licensed under [GPL-3.0](LICENSE). Contributions welcome via pull request.

---

## Developer

<a href="https://github.com/azeemidrisi/">
 <img width="150px" src="https://github.com/AzeemIdrisi/PhoneSploit-Pro/assets/112647789/a5fa646c-93a2-460f-bcb7-528fedb147e9" />
</a>

**Azeem Idrisi** - [@AzeemIdrisi](https://github.com/azeemidrisi/)

### Contributors

Special thanks to all the contributors :

[@noobshubham](https://github.com/noobshubham/)

[@PuruSinghvi](https://github.com/PuruSinghvi)

<a href="https://paypal.me/AzeemIdrisi" target="_blank"><img src="https://github.com/AzeemIdrisi/AzeemIdrisi/blob/main/docs/paypal-button-blue.png" alt="PayPal" width="147"></a>
<a href="https://www.buymeacoffee.com/AzeemIdrisi" target="_blank"><img src="https://github.com/AzeemIdrisi/AzeemIdrisi/blob/main/docs/default-yellow.png" alt="Buy Me A Coffee" width="200"></a>

<hr>

Copyright © 2026 Azeem Idrisi ([github.com/AzeemIdrisi](https://github.com/AzeemIdrisi))
