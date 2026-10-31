package com.mediasteru.model;

public class DownloadOptions {
	private final String format;
	private final String quality;

	public DownloadOptions(String format, String quality) {
		this.format = format;
		this.quality = quality;
	}

	public String getFormat() {
		return format;
	}

	public String getQuality() {
		return quality;
	}
}
