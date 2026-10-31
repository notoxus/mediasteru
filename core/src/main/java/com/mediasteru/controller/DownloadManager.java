package com.mediasteru.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mediasteru.model.DownloadOptions;
import com.mediasteru.model.DownloadStrategy;
import com.mediasteru.model.DownloadTask;
import com.mediasteru.model.Observer;
import com.mediasteru.model.TrimOptions;
import com.mediasteru.model.VideoInfo;
import com.mediasteru.ui.DownloadUiBridge;

public class DownloadManager {
	private DownloadStrategy strategy;
	private DownloadUiBridge uiBridge;
	private final Map<Integer, DownloadTask> pendingTasks = new ConcurrentHashMap<>();
	private final BlockingQueue<DownloadTask> downloadQueue = new LinkedBlockingQueue<>();
	private final Thread queueWorker;
	private final Set<String> analyzingUrls = ConcurrentHashMap.newKeySet();
	private final ExecutorService executor = Executors.newFixedThreadPool(10);

	public DownloadManager() {
		this.queueWorker = new Thread(this::processQueue, "download-queue-worker");
		queueWorker.setDaemon(true);
		queueWorker.start();
	}

	public void setStrategy(DownloadStrategy strategy) {
		this.strategy = strategy;
	}

	public DownloadStrategy getStrategy() {
		return strategy;
	}

	public void setUiBridge(DownloadUiBridge bridge) {
		this.uiBridge = bridge;
	}

	public DownloadUiBridge getUiBridge() {
		return uiBridge;
	}

	public Map<Integer, DownloadTask> getPendingTasks() {
		return pendingTasks;
	}

	public boolean enqueuePendingTask(int rowIndex) {
		DownloadTask task = pendingTasks.get(rowIndex);
		if (task == null) {
			return false;
		}
		if (!task.isConfigured()) {
			if (uiBridge != null) {
				uiBridge.onLog("=> [Queue] Choose options for this captured stream before starting it.");
			}
			return false;
		}
		pendingTasks.remove(rowIndex);
		try {
			downloadQueue.put(task);
			return true;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return false;
	}

	public void removePendingTask(int rowIndex) {
		pendingTasks.remove(rowIndex);
		Map<Integer, DownloadTask> updatedMap = new ConcurrentHashMap<>();
		for (Map.Entry<Integer, DownloadTask> entry : pendingTasks.entrySet()) {
			int oldIndex = entry.getKey();
			DownloadTask task = entry.getValue();
			if (oldIndex > rowIndex) {
				int newIndex = oldIndex - 1;
				task.rowIndex = newIndex;
				updatedMap.put(newIndex, task);
			} else {
				updatedMap.put(oldIndex, task);
			}
		}
		pendingTasks.clear();
		pendingTasks.putAll(updatedMap);
	}

	private void processQueue() {
		while (true) {
			try {
				DownloadTask task = downloadQueue.take();
				int remaining = downloadQueue.size();
				System.out.println("\n==================================");
				if (uiBridge != null) {
					uiBridge.onLog("=> [Queue] Handling link: " + task.url);
					uiBridge.onQueueItemStatusUpdated(task.rowIndex, "Loading...", "0%");
				}
				System.out.println("Remaining: " + remaining + " video.");
				if (strategy != null) {
					final int currentRow = task.rowIndex;
					Observer taskObserver = new Observer() {
						@Override
						public void onProgressUpdate(String videoId, double percent, String speed) {
							int width = 50;
							int progress = (int) (percent / 100 * width);

							StringBuilder bar = new StringBuilder("[");
							for (int i = 0; i < width; i++) {
								if (i < progress)
									bar.append("=");
								else if (i == progress)
									bar.append(">");
								else
									bar.append(" ");
							}

							String output = String.format("\r%s] %.1f%% | speed: %s", bar.toString(), percent, speed);
							System.out.print(output);

							if (uiBridge != null) {
								String progressStr = String.format("%.1f%%", percent);
								if (speed != null && !speed.trim().isEmpty() && !speed.equals("N/A")) {
									progressStr += " (" + speed + ")";
								}
								uiBridge.onQueueItemStatusUpdated(currentRow, "Downloading...", progressStr);
							}
						}

						@Override
						public void onComplete(String videoId, String savedPath) {
							System.out.println("\n>> Video download completed successfully!");
							System.out.println(">> Saved at: " + savedPath);
							System.out.println("--------------------------------------------------");

							if (uiBridge != null) {
								uiBridge.onQueueItemStatusUpdated(currentRow, "Completed", "100%");
								uiBridge.onLog(">> Download completed: " + savedPath);
							}
						}

						@Override
						public void onError(String videoId, String errorMessage) {
							System.err.println("\n>> Error: failed to download. Details: " + errorMessage);

							if (uiBridge != null) {
								uiBridge.onQueueItemStatusUpdated(currentRow, "Failed", "—");
								uiBridge.onLog(">> Download failed: " + errorMessage);
							}
						}
					};
					strategy.startDownload(task.url, task.savePath, task.format, task.trimSection, task.preciseCut,
							taskObserver, task.referer, task.requestHeaders, task.quality);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			} catch (Exception e) {
				System.err.println("[Queue] Error: " + e.getMessage());
				if (uiBridge != null)
					uiBridge.onLog("[Queue Error] " + e.getMessage());
			}
		}
	}

	public void processAutoCapture(String url) {
		processAutoCapture(url, null, Map.of());
	}

	public void processAutoCapture(String url, String referer) {
		processAutoCapture(url, referer, Map.of());
	}

	public void processAutoCapture(String url, String referer, Map<String, String> requestHeaders) {
		System.out.println("\n[Auto-Capture] Stream caught and added to the review queue: " + url);
		if (uiBridge != null) {
			int newRow = uiBridge.onQueueItemAddRequested(url, "Choose options", "Captured");
			pendingTasks.put(newRow,
					new DownloadTask(url, null, "mp4", "1080", null, false, newRow, referer, requestHeaders));
			uiBridge.onStreamCaptured(url, newRow);
			uiBridge.onLog("=> [Hunter] Captured a stream (Row " + newRow
					+ "). Double-click it to choose format, quality, and folder.");
			resolveTitleAsync(url, newRow);
		}
	}

	public boolean configurePendingTask(int rowIndex) {
		DownloadTask task = pendingTasks.get(rowIndex);
		if (task == null || uiBridge == null)
			return false;

		DownloadOptions opts = uiBridge.promptDownloadOptions("Choose options for the captured stream.");
		if (opts == null)
			return false;

		String savePath = uiBridge.promptSaveDirectory();
		if (savePath == null || savePath.isBlank())
			return false;

		task.format = opts.getFormat();
		task.quality = opts.getQuality();
		task.savePath = savePath;

		uiBridge.onQueueItemFormatUpdated(rowIndex, formatLabel(task));
		uiBridge.onQueueItemStatusUpdated(rowIndex, "Waiting...", "0%");
		uiBridge.onLog("=> [Queue] Stream options saved.");
		return true;
	}

	public String formatLabel(DownloadTask task) {
		if (task.format.equalsIgnoreCase("mp3"))
			return "MP3";
		String quality = task.quality.equalsIgnoreCase("best") ? "Best" : task.quality + "p";
		return task.format.toUpperCase() + " · " + quality;
	}

	private void resolveTitleAsync(String url, int rowIndex) {
		executor.submit(() -> {
			try {
				if (strategy != null) {
					VideoInfo info = strategy.fetchMetadata(url);
					if (info != null && info.getTitle() != null && !info.getTitle().isBlank() && uiBridge != null) {
						uiBridge.onQueueItemNameUpdated(rowIndex, info.getTitle());
					}
				}
			} catch (Exception ignored) {
			}
		});
	}

	public void openTrimDialog(int row) {
		DownloadTask task = pendingTasks.get(row);
		if (task == null || uiBridge == null)
			return;

		boolean isAudio = task.format.equalsIgnoreCase("mp3");
		TrimOptions trimOpts = uiBridge.promptTrim("Trim Video", task.url, 0, isAudio);
		if (trimOpts != null) {
			task.trimSection = trimOpts.getTrimSection();
			task.preciseCut = trimOpts.isPreciseCut();
			String formatLabel = formatLabel(task) + (task.trimSection != null ? " ✂" : "");
			uiBridge.onQueueItemFormatUpdated(row, formatLabel);
		}
	}

	public void processLink(String url) {
		if (!analyzingUrls.add(url)) {
			return;
		}

		executor.submit(() -> {
			try {
				System.out.println("\n[Analyzing link] " + url);
				List<String> links = strategy != null ? strategy.extractPlaylistLinks(url) : List.of();
				if (links.isEmpty()) {
					System.out.println("=> Couldn't find video/playlist or internet connection issues.");
					return;
				}

				VideoInfo info = strategy.fetchMetadata(links.get(0));
				String displayTitle = (links.size() > 1)
						? "Playlist (" + links.size() + " videos): " + (info != null ? info.getTitle() : "")
						: (info != null ? info.getTitle() : links.get(0));

				if (uiBridge == null)
					return;

				String savePath = uiBridge.promptSaveDirectory();
				if (savePath == null || savePath.isEmpty())
					return;

				DownloadOptions opts = uiBridge.promptDownloadOptions("Choose download options for:\n" + displayTitle);
				if (opts == null)
					return;

				String formatLabel = opts.getFormat().equalsIgnoreCase("mp3") ? "MP3"
						: opts.getFormat().toUpperCase() + " · "
								+ (opts.getQuality().equalsIgnoreCase("best") ? "Best" : opts.getQuality() + "p");
				boolean resolveTitles = links.size() <= 20;

				for (String link : links) {
					String display = (links.size() == 1 && info != null && info.getTitle() != null
							&& !info.getTitle().isBlank()) ? info.getTitle() : link;
					int newRow = uiBridge.onQueueItemAddRequested(display, formatLabel, "Waiting...");
					if (newRow != -1) {
						pendingTasks.put(newRow, new DownloadTask(link, savePath, opts.getFormat(), opts.getQuality(),
								null, false, newRow));
						if (links.size() > 1 && resolveTitles) {
							resolveTitleAsync(link, newRow);
						}
					}
				}
				uiBridge.onLog("=> [System] Added " + links.size() + " items to waitlist.");
			} catch (Exception e) {
				System.err.println("Analytics error! Error: " + e.getMessage());
			} finally {
				analyzingUrls.remove(url);
			}
		});
	}

	public void processApiJson(String jsonResponse) {
		try {
			JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();
			JsonObject movie = root.getAsJsonObject("movie");

			String movieName = movie.get("name").getAsString();

			JsonArray episodesArray = root.getAsJsonArray("episodes");
			if (episodesArray.size() > 0) {
				JsonObject server = episodesArray.get(0).getAsJsonObject();
				JsonArray items = server.getAsJsonArray("items");

				System.out.println("\n[API Parser] Found Movie: " + movieName);
				System.out.println("[API Parser] Total episodes: " + items.size());

				if (uiBridge == null)
					return;

				String savePath = uiBridge.promptSaveDirectory();
				if (savePath == null || savePath.isEmpty())
					return;

				for (JsonElement itemElement : items) {
					JsonObject item = itemElement.getAsJsonObject();
					String epNumber = item.get("name").getAsString();
					String m3u8Url = item.get("m3u8").getAsString();

					String fileName = movieName + " - "
							+ (epNumber.toLowerCase().startsWith("episode") || epNumber.toLowerCase().startsWith("ep")
									? epNumber
									: "Episode " + epNumber);

					int newRow = uiBridge.onQueueItemAddRequested(fileName, "MP4 · 1080p", "Waiting...");
					pendingTasks.put(newRow,
							new DownloadTask(m3u8Url, savePath, "mp4", "1080", null, false, newRow));
				}
				uiBridge.onLog("=> [API] Successfully loaded " + items.size() + " episodes. Ready to download.");
			}
		} catch (Exception e) {
			System.err.println("API Parse Error: " + e.getMessage());
		}
	}
}
