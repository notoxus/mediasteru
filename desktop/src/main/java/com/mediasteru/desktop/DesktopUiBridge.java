package com.mediasteru.desktop;

import java.awt.Toolkit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import com.mediasteru.model.DownloadOptions;
import com.mediasteru.model.TrimOptions;
import com.mediasteru.ui.DownloadUiBridge;
import com.mediasteru.view.AppGUI;
import com.mediasteru.view.DownloadOptionsDialog;
import com.mediasteru.view.FolderSelector;
import com.mediasteru.view.TrimDialog;

public class DesktopUiBridge implements DownloadUiBridge {
	private AppGUI gui;

	public DesktopUiBridge() {
	}

	public DesktopUiBridge(AppGUI gui) {
		this.gui = gui;
	}

	public void setGui(AppGUI gui) {
		this.gui = gui;
	}

	public AppGUI getGui() {
		return gui;
	}

	@Override
	public int onQueueItemAddRequested(String displayName, String formatLabel, String status) {
		if (gui == null) {
			return -1;
		}
		if (SwingUtilities.isEventDispatchThread()) {
			return gui.addQueueItem(displayName, formatLabel, status);
		}
		AtomicInteger result = new AtomicInteger(-1);
		try {
			SwingUtilities.invokeAndWait(() -> {
				if (gui != null) {
					result.set(gui.addQueueItem(displayName, formatLabel, status));
				}
			});
		} catch (Exception e) {
			result.set(gui.addQueueItem(displayName, formatLabel, status));
		}
		return result.get();
	}

	@Override
	public void onQueueItemStatusUpdated(int rowIndex, String status, String progress) {
		if (gui == null) return;
		SwingUtilities.invokeLater(() -> {
			if (gui != null) {
				gui.updateQueueItemStatus(rowIndex, status, progress);
			}
		});
	}

	@Override
	public void onQueueItemFormatUpdated(int rowIndex, String formatLabel) {
		if (gui == null) return;
		SwingUtilities.invokeLater(() -> {
			if (gui != null) {
				gui.updateQueueItemFormat(rowIndex, formatLabel);
			}
		});
	}

	@Override
	public void onQueueItemNameUpdated(int rowIndex, String name) {
		if (gui == null) return;
		SwingUtilities.invokeLater(() -> {
			if (gui != null) {
				gui.updateQueueItemName(rowIndex, name);
			}
		});
	}

	@Override
	public void onLog(String message) {
		if (gui == null) return;
		SwingUtilities.invokeLater(() -> {
			if (gui != null) {
				gui.logToConsole(message);
			}
		});
	}

	@Override
	public void onStreamCaptured(String url, int rowIndex) {
		try {
			Toolkit.getDefaultToolkit().beep();
		} catch (Exception ignored) {
		}
	}

	@Override
	public DownloadOptions promptDownloadOptions(String promptMessage) {
		if (SwingUtilities.isEventDispatchThread()) {
			DownloadOptionsDialog.Options opts = DownloadOptionsDialog.show(promptMessage);
			return opts == null ? null : new DownloadOptions(opts.format, opts.quality);
		}
		AtomicReference<DownloadOptions> result = new AtomicReference<>();
		try {
			SwingUtilities.invokeAndWait(() -> {
				DownloadOptionsDialog.Options opts = DownloadOptionsDialog.show(promptMessage);
				if (opts != null) {
					result.set(new DownloadOptions(opts.format, opts.quality));
				}
			});
		} catch (Exception ignored) {
		}
		return result.get();
	}

	@Override
	public String promptSaveDirectory() {
		if (SwingUtilities.isEventDispatchThread()) {
			return FolderSelector.chooseSaveDirectory();
		}
		AtomicReference<String> result = new AtomicReference<>();
		try {
			SwingUtilities.invokeAndWait(() -> {
				result.set(FolderSelector.chooseSaveDirectory());
			});
		} catch (Exception ignored) {
		}
		return result.get();
	}

	@Override
	public TrimOptions promptTrim(String title, String url, int durationHint, boolean isAudio) {
		if (SwingUtilities.isEventDispatchThread()) {
			TrimDialog.TrimOptions trimOpts = TrimDialog.show(title, url, durationHint, url, isAudio);
			return trimOpts == null ? null : new TrimOptions(trimOpts.trimSection, trimOpts.preciseCut);
		}
		AtomicReference<TrimOptions> result = new AtomicReference<>();
		try {
			SwingUtilities.invokeAndWait(() -> {
				TrimDialog.TrimOptions trimOpts = TrimDialog.show(title, url, durationHint, url, isAudio);
				if (trimOpts != null) {
					result.set(new TrimOptions(trimOpts.trimSection, trimOpts.preciseCut));
				}
			});
		} catch (Exception ignored) {
		}
		return result.get();
	}
}
