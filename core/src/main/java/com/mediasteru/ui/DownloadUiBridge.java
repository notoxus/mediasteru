package com.mediasteru.ui;

import com.mediasteru.model.DownloadOptions;
import com.mediasteru.model.TrimOptions;

public interface DownloadUiBridge {

	int onQueueItemAddRequested(String displayName, String formatLabel, String status);

	void onQueueItemStatusUpdated(int rowIndex, String status, String progress);

	void onQueueItemFormatUpdated(int rowIndex, String formatLabel);

	void onQueueItemNameUpdated(int rowIndex, String name);

	void onLog(String message);

	void onStreamCaptured(String url, int rowIndex);

	DownloadOptions promptDownloadOptions(String promptMessage);

	String promptSaveDirectory();

	TrimOptions promptTrim(String title, String url, int durationHint, boolean isAudio);
}
