# 🚀 Installation Guide

Choose the Electron desktop package for the modern desktop client or the Java
portable package to use the existing Swing app. Both releases bundle the media
tools they need; only the Java package includes a trimmed JRE.

## Step 1: Requirements

* **Java Portable only:** No system Java installation is required; a trimmed
  Java 21 Runtime Environment is bundled with the package.
* **Browser Hunting:** The Java desktop app supports Chromium-based browsers,
  including Helium, Google Chrome, Chromium, Brave, Microsoft Edge, Vivaldi,
  Opera, Thorium, and compatible derivatives. Electron includes its own Hunting
  browser.

## Step 2: Installing the App

1. Navigate to the **[Releases](https://github.com/notoxus/mediasteru/releases)** page of this repository.
2. Choose the Electron package or the Java Portable package for your OS and
   architecture. Replace `vX.Y.Z` with the release version:
   * **Windows x64:** `MediaSteru-vX.Y.Z-Electron-Windows-x64.zip` or
     `MediaSteru-vX.Y.Z-Portable-Windows-x64.zip`
   * **macOS Intel:** `MediaSteru-vX.Y.Z-Electron-macOS-Intel-x64.tar.gz` or
     `MediaSteru-vX.Y.Z-Portable-macOS-Intel-x64.tar.gz`
   * **macOS Apple Silicon:** `MediaSteru-vX.Y.Z-Electron-macOS-Apple-Silicon-arm64.tar.gz` or
     `MediaSteru-vX.Y.Z-Portable-macOS-Apple-Silicon-arm64.tar.gz`
   * **Linux x64:** `MediaSteru-vX.Y.Z-Electron-Linux-x64.tar.gz` or
     `MediaSteru-vX.Y.Z-Portable-Linux-x64.tar.gz`
   * **Linux ARM64:** `MediaSteru-vX.Y.Z-Electron-Linux-ARM64.tar.gz` or
     `MediaSteru-vX.Y.Z-Portable-Linux-ARM64.tar.gz`
   * **Android companion:** Download the APK from the [mediasteru-clients releases](https://github.com/notoxus/mediasteru-clients/releases). Its version is independent of the desktop host release.

## 📂 Package Structure (What's Inside)

The Java Portable archive contains:

* **Core App:** `MediaSteru.jar` — the main compiled application.
* **Launcher Script:** `run.bat` (Windows) or `run.sh` (Mac/Linux) — starts the app.
* **Embedded Runtime:** A trimmed JRE 21 tailored for your platform, built automatically by CI using `jdeps` and `jlink`.
* **Engine Tools:** The `tools/` folder contains only the checksum-verified `yt-dlp`, `ffmpeg`, and `deno` binaries selected for the package's OS and CPU architecture. The JRE is packaged separately beside the app.
* **Dependency Manifest:** `tools-manifest.json`, the exact version and hash
  record used by both local development and GitHub release builds.

---

## Step 3: Run the Application

### For Windows Users

1. Extract the `...-Portable-Windows-x64.zip` archive to any folder.
2. **Double-click `run.bat`** to launch the app.

> The launcher silently checks for the bundled JRE. If it's missing for any reason, it will download and install it automatically before launching.

### For macOS & Linux Users

Unix-based systems require explicit permission to execute launcher scripts.

1. Extract the downloaded `.tar.gz` archive.
2. Open **Terminal** and navigate to the extracted directory:
   ```bash
   cd /path/to/extracted/folder
   ```
3. Grant execution permission:
   ```bash
   chmod +x run.sh
   ```
4. Launch the application:
   ```bash
   ./run.sh
   ```

> Same as Windows — the launcher auto-detects your OS and architecture, checks for the bundled JRE, and downloads it from Adoptium if missing.

#### Linux display scaling

The app uses a 175% interface scale on Linux because some Wayland compositors do not expose fractional DPI correctly to Swing. To override it, launch with a percentage or decimal value:

```bash
MEDIASTERU_UI_SCALE=150% ./run.sh
# or use 1.0 to restore the unscaled size
```

### For Android Companion Users

Install the `-remote-desktop.apk` file and allow installation from unknown sources when prompted. The phone and desktop must be on the same local network, and the desktop app must be running.

## Electron Desktop and Terminal Client

Extract the Electron archive and launch the `MediaSteru` app or executable
inside it. The package includes the platform-specific media tools. These initial
packages are unsigned, and macOS builds are not notarized, so the OS may show a
first-launch security prompt. The Rust terminal client remains a developer build;
build and start Electron first, then run the terminal client in another terminal:

```bash
cd electron
npm install
npm start

# In another terminal, from the repository root:
cargo run --manifest-path cli/Cargo.toml -- tui
```

See [`electron/README.md`](electron/README.md) and [`cli/README.md`](cli/README.md) for details.

## Docker / Podman Headless Installation

The container edition runs MediaSteru as a background service and includes the terminal client. It does not require Java, Node.js, Rust, Electron, yt-dlp, FFmpeg, or Deno on the host.

```bash
git clone https://github.com/notoxus/mediasteru.git
cd mediasteru
mkdir -p downloads
docker compose up -d
alias mediasteru="$PWD/docker/mediasteru"

mediasteru status
mediasteru tui
```

Use `docker compose pull && docker compose up -d` to update. Files are written to `./downloads`, while the media library and logs persist in the `mediasteru-data` Docker volume.

Set `MEDIASTERU_HOST_PORT=9876` before `docker compose up -d` when the default host port `8765` is already occupied. For a local image build, use `MEDIASTERU_IMAGE=mediasteru:local docker compose up -d --build`.

The container is intended for direct downloads, CLI/TUI use, and Android Companion submissions. Use the Electron or Swing desktop client when you need an interactive Hunting browser; containerized mpv playback is not connected to the host desktop.

---

## 🔄 Updating the App

When a new version is available, the app will notify you on launch. You can:
- **Download for my system** — automatically downloads the correct package for your OS/architecture into your Downloads folder.
- **Skip this version** — suppresses the notification for that release.
- **Remind me later** — dismissed until next launch.

After downloading, extract the new archive and replace your old installation folder.
