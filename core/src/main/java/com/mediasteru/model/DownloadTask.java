package com.mediasteru.model;

import java.util.Map;

public class DownloadTask {
	public String url;
	public String savePath;
	public String format;
	public String quality;
	public String trimSection; // null = full video
	public boolean preciseCut;
	public int rowIndex;
	public String referer;
	public Map<String, String> requestHeaders;

	public DownloadTask(String url, String savePath, String format, String trimSection, boolean preciseCut,
			int rowIndex) {
		this(url, savePath, format, "1080", trimSection, preciseCut, rowIndex, null, Map.of());
	}

	public DownloadTask(String url, String savePath, String format, String trimSection, boolean preciseCut,
			int rowIndex, String referer) {
		this(url, savePath, format, "1080", trimSection, preciseCut, rowIndex, referer, Map.of());
	}

	public DownloadTask(String url, String savePath, String format, String quality, String trimSection,
			boolean preciseCut, int rowIndex) {
		this(url, savePath, format, quality, trimSection, preciseCut, rowIndex, null, Map.of());
	}

	public DownloadTask(String url, String savePath, String format, String quality, String trimSection,
			boolean preciseCut, int rowIndex, String referer, Map<String, String> requestHeaders) {
		this.url = url;
		this.savePath = savePath;
		this.format = format;
		this.quality = quality;
		this.trimSection = trimSection;
		this.preciseCut = preciseCut;
		this.rowIndex = rowIndex;
		this.referer = referer;
		this.requestHeaders = requestHeaders == null ? Map.of() : Map.copyOf(requestHeaders);
	}

	public boolean isConfigured() {
		return savePath != null && !savePath.isBlank();
	}
}
