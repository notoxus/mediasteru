# MediaSteru - The VJ Cue & Media Hunting Engine

![Version](https://img.shields.io/badge/version-<!-- VERSION_START -->1.0.0<!-- VERSION_END -->-blue)

## Why is its name Media Steru?

That is a fusion of *Media*, *Steer*, and *Mastery*. It acts as a powerful web media downloader (Hunting) and a zero-latency VJ desktop engine for live performance (Cueing). It is especially for Linux users who deal with VJ cue issues, and UNIX users in general, btw.

Powered by **Spring Boot**, the core application manages media extraction, local library indexing (SQLite), and communicates directly with an active `mpv` instance via IPC sockets for ultra-fast video switching.

## Core Capabilities

| **Feature** | **Description** |
|---|---|
| **Real-time VJ Cueing** | Trigger, switch, and layer videos instantly via the Companion App. Bypasses standard overhead by sending direct IPC commands to `mpv`. | 
| **Browser Hunting** | Intercepts HLS/DASH stream URLs as you browse via an automatically managed Chromium browser extension. | 
| **Direct Download** | YouTube, TikTok, Facebook, Instagram — extracted directly via `yt-dlp`. | 
| **Clipboard Monitor** | A background thread watches your clipboard; paste a URL and it auto-queues. | 
| **Format & Quality Choice** | Download as MP4, MKV, or extract audio as MP3. Select resolutions up to 4K. | 
| **Trim Before Download** | Cut a specific section (e.g., 01:30 → 02:45) and download only that clip — no full download needed. Supports both fast-cut and frame-accurate re-encoding. | 
| **Bulk API Import** | Import a JSON payload with multiple episodes to queue an entire series at once. | 
| **Companion Remote API** | Built-in Spring Boot REST/WebSocket server on port `8765` to receive commands from the Android Companion App. | 
| **Engine Auto-Update** | `yt-dlp` and `ffmpeg` self-update in the background based on a pinned manifest, ensuring site extractors never break. | 

### Requirements

* **Java:** JDK 21 or higher

* **Player:** `mpv` installed natively and accessible in system `PATH`

* **Internet:** Required to resolve online media and synchronize initial binaries.

### Build & Run

```
# Clone the repository
git clone https://github.com/notoxus/mediasteru.git
cd mediasteru
./gradlew clean bootJar
# Build the executable Spring Boot application
./gradlew build
# Run the VJ engine & REST API server locally
./gradlew bootRun

```
## Public Release Automation:

[Tag and Release](RELEASE_FLOW.md)

[Android signing](keystore/README.md)

## How Hunting Works?

MediaSteru generates a Chromium extension at `~/.MediaSteru/Extension/chromium/` and loads it into a disposable browser profile. The extension hooks into the browser's `webRequest` API with two interception layers:

1. **URL Pattern Matching:** Fires before each request, checking for `.m3u8`, `.mpd`, HLS query parameters, and common path segments (`/hls/`, `/dash/`).

2. **Content-Type Sniffing:** Fires when response headers arrive, checking for `application/x-mpegURL` or `application/dash+xml` to catch extension-less streams.

Captured request headers (including `Referer` and `Cookies`) are forwarded to the engine so streams protected by hotlink checks have the same request context as the browser.

Once media plays, the extension displays a "Download with MediaSteru" label. Clicking it sends the HTTP POST payload to the engine (`localhost:8765`), dropping the stream straight into your review queue.

## ✄ Advanced Trimming

When queuing a video, tick **"Cut a section before downloading"**. The app reads the video length and provides a dual-handle slider.

* **Fast cut (default):** Cuts on the nearest keyframe using a stream copy (`-c copy`). Nearly as quick as a normal download.

* **Frame-accurate cut:** Re-encodes the clip to start/end at the exact millisecond. Slower but precise.

## ⚙️ Engine Dependency Automation

The engine relies on external binaries (`yt-dlp`, `FFmpeg`) which are heavy and frequently updated. We do not commit these to the repository.

Instead, `tools-manifest.json` serves as the single source of truth for versions, download URLs, and SHA-256 hashes.

* On the first run, the Spring Boot engine verifies and downloads the correct binaries for your OS and CPU architecture (Windows/macOS/Linux, x64/ARM64) into the ignored `tools/` directory.

* Developers can synchronize the local tools by running `node scripts/sync-tools.mjs --all`.

## 🛟 Troubleshooting

* **The Download label does not appear in Hunting Mode:**
  Ensure the Spring Boot app is running before opening the capture browser. Check that port `8765` is not blocked by a firewall. Widevine/DRM-encrypted streams cannot be extracted.

* **Download fails unexpectedly:**
  Some sites require authentication. Open the site normally in the Hunting browser, log in to establish cookies, then reload the video.

* **Player switch delay:**
  Ensure `mpv` is configured to launch with `--input-ipc-server`. MediaSteru uses this socket to queue and trigger videos instead of cold-booting the player.
