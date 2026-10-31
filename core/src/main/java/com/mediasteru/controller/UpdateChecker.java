package com.mediasteru.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import java.util.function.Consumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class UpdateChecker {
	private static final String DEFAULT_FALLBACK_VERSION = "v1.0.0";
	private static String cachedVersion = null;

	private static final String REPO_OWNER = "notoxus";
	private static final String REPO_NAME = "mediasteru";

	private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".MediaSteru";
	private static final File SKIP_FILE = new File(CONFIG_DIR, "skipped_version.txt");

	public record UpdateInfo(String latestVersion, String releaseUrl, String assetUrl) {
		public boolean hasAsset() {
			return assetUrl != null && !assetUrl.isBlank();
		}
	}

	public static String getCurrentVersion() {
		if (cachedVersion != null) {
			return cachedVersion;
		}

		// 1. Try reading version from version.properties
		try (InputStream in = UpdateChecker.class.getResourceAsStream("/version.properties")) {
			if (in != null) {
				Properties props = new Properties();
				props.load(in);
				String v = props.getProperty("version");
				if (v != null && !v.isBlank() && !v.contains("${")) {
					cachedVersion = formatVersion(v);
					return cachedVersion;
				}
			}
		} catch (Exception ignored) {
		}

		// 2. Try reading from Package Implementation-Version (from Manifest)
		try {
			String implVer = UpdateChecker.class.getPackage().getImplementationVersion();
			if (implVer != null && !implVer.isBlank()) {
				cachedVersion = formatVersion(implVer);
				return cachedVersion;
			}
		} catch (Exception ignored) {
		}

		// 3. In dev mode with Git, detect git tag via 'git describe --tags --abbrev=0'
		try {
			Process p = new ProcessBuilder("git", "describe", "--tags", "--abbrev=0").start();
			if (p.waitFor() == 0) {
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
					String tag = reader.readLine();
					if (tag != null && !tag.isBlank()) {
						cachedVersion = formatVersion(tag);
						return cachedVersion;
					}
				}
			}
		} catch (Exception ignored) {
		}

		cachedVersion = DEFAULT_FALLBACK_VERSION;
		return cachedVersion;
	}

	private static String formatVersion(String v) {
		String clean = v.trim();
		if (!clean.startsWith("v") && !clean.startsWith("V")) {
			return "v" + clean;
		}
		return clean;
	}

	public static void checkForUpdates(Consumer<UpdateInfo> onUpdateFound) {
		new Thread(() -> {
			try {
				URL url = new URI("https://api.github.com/repos/" + REPO_OWNER + "/" + REPO_NAME + "/releases/latest")
						.toURL();
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

				if (conn.getResponseCode() == 200) {
					BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					StringBuilder response = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						response.append(line);
					}
					reader.close();

					JsonObject release = JsonParser.parseString(response.toString()).getAsJsonObject();
					String latestVersion = release.get("tag_name").getAsString();
					String releaseUrl = release.get("html_url").getAsString();
					String currentVersion = getCurrentVersion();

					if (isNewer(latestVersion, currentVersion) && !isVersionSkipped(latestVersion)) {
						String assetUrl = findPlatformAssetUrl(release);
						if (onUpdateFound != null) {
							onUpdateFound.accept(new UpdateInfo(latestVersion, releaseUrl, assetUrl));
						}
					}
				}
			} catch (Exception e) {
				System.err.println("[System] Update check failed (No internet or API limit).");
			}
		}, "update-checker").start();
	}

	public static boolean isNewer(String latest, String current) {
		try {
			int[] a = parseVersion(latest);
			int[] b = parseVersion(current);
			for (int i = 0; i < Math.max(a.length, b.length); i++) {
				int x = i < a.length ? a[i] : 0;
				int y = i < b.length ? b[i] : 0;
				if (x != y) {
					return x > y;
				}
			}
			return false;
		} catch (Exception e) {
			return !latest.equals(current);
		}
	}

	private static int[] parseVersion(String v) {
		String cleaned = v.trim().toLowerCase().replaceFirst("^v", "").replaceAll("[^0-9.].*$", "");
		String[] parts = cleaned.split("\\.");
		int[] nums = new int[parts.length];
		for (int i = 0; i < parts.length; i++) {
			nums[i] = parts[i].isEmpty() ? 0 : Integer.parseInt(parts[i]);
		}
		return nums;
	}

	private static String findPlatformAssetUrl(JsonObject release) {
		String suffix = platformAssetSuffix();
		if (suffix == null || !release.has("assets")) {
			return null;
		}
		JsonArray assets = release.getAsJsonArray("assets");
		for (JsonElement el : assets) {
			JsonObject asset = el.getAsJsonObject();
			String name = asset.get("name").getAsString();
			if (name.endsWith(suffix)) {
				return asset.get("browser_download_url").getAsString();
			}
		}
		return null;
	}

	private static String platformAssetSuffix() {
		String os = System.getProperty("os.name").toLowerCase();
		String arch = System.getProperty("os.arch").toLowerCase();
		boolean isArm = arch.contains("aarch64") || arch.contains("arm");

		if (os.contains("win")) {
			return "-Win.zip";
		} else if (os.contains("mac")) {
			return isArm ? "-Mac-AppleSilicon.tar.gz" : "-Mac-Intel.tar.gz";
		} else {
			return isArm ? "-Linux-ARM.tar.gz" : "-Linux-x64.tar.gz";
		}
	}

	public static boolean isVersionSkipped(String version) {
		try {
			if (SKIP_FILE.exists()) {
				BufferedReader reader = new BufferedReader(new FileReader(SKIP_FILE));
				String skippedVersion = reader.readLine();
				reader.close();
				return version.equals(skippedVersion);
			}
		} catch (Exception e) {
			// Ignore
		}
		return false;
	}

	public static void saveSkippedVersion(String version) {
		try {
			if (!SKIP_FILE.getParentFile().exists()) {
				SKIP_FILE.getParentFile().mkdirs();
			}
			FileWriter writer = new FileWriter(SKIP_FILE);
			writer.write(version);
			writer.close();
		} catch (Exception e) {
			System.err.println("Could not save skip config.");
		}
	}

	public static void downloadPackage(String assetUrl, File targetDir, Consumer<Integer> progressListener,
			Consumer<File> onComplete, Consumer<String> onError) {
		new Thread(() -> {
			try {
				String fileName = assetUrl.substring(assetUrl.lastIndexOf('/') + 1);
				File outFile = new File(targetDir, fileName);

				HttpURLConnection conn = (HttpURLConnection) new URI(assetUrl).toURL().openConnection();
				conn.setInstanceFollowRedirects(true);
				conn.setRequestProperty("User-Agent", "MediaSteru-Updater");
				long total = conn.getContentLengthLong();

				try (InputStream in = conn.getInputStream(); FileOutputStream out = new FileOutputStream(outFile)) {
					byte[] buffer = new byte[8192];
					long downloaded = 0;
					int read;
					int lastLoggedPct = -1;
					while ((read = in.read(buffer)) != -1) {
						out.write(buffer, 0, read);
						downloaded += read;
						if (total > 0) {
							int pct = (int) (downloaded * 100 / total);
							if (pct != lastLoggedPct && pct % 10 == 0) {
								if (progressListener != null) {
									progressListener.accept(pct);
								}
								lastLoggedPct = pct;
							}
						}
					}
				}

				if (onComplete != null) {
					onComplete.accept(outFile);
				}
			} catch (Exception e) {
				if (onError != null) {
					onError.accept(e.getMessage());
				}
			}
		}, "update-downloader").start();
	}
}
