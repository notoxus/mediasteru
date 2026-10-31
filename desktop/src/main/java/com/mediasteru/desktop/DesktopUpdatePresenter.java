package com.mediasteru.desktop;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.mediasteru.controller.UpdateChecker;

public class DesktopUpdatePresenter {

	public static void checkAndPromptUpdate() {
		UpdateChecker.checkForUpdates(updateInfo -> {
			SwingUtilities.invokeLater(() -> showUpdateDialog(updateInfo));
		});
	}

	private static void showUpdateDialog(UpdateChecker.UpdateInfo updateInfo) {
		boolean canAutoDownload = updateInfo.hasAsset();
		String message = "A new version (" + updateInfo.latestVersion() + ") is available!\n"
				+ "You are currently using " + UpdateChecker.getCurrentVersion() + ".\n\n"
				+ (canAutoDownload ? "Download the ready-to-use package for your system now?"
						: "Open the download page to get the update?");

		String downloadLabel = canAutoDownload ? "Download for my system" : "Open download page";
		String[] options = { downloadLabel, "Skip this version", "Remind me later" };
		int choice = JOptionPane.showOptionDialog(null, message, "Update Available", JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

		if (choice == 0) {
			if (canAutoDownload) {
				downloadPackage(updateInfo.assetUrl());
			} else {
				openInBrowser(updateInfo.releaseUrl());
			}
		} else if (choice == 1) {
			UpdateChecker.saveSkippedVersion(updateInfo.latestVersion());
			System.out.println("=> [System] Skipped update for version: " + updateInfo.latestVersion());
		}
	}

	private static void downloadPackage(String assetUrl) {
		JOptionPane.showMessageDialog(null,
				"The update is downloading in the background.\nYou'll be notified when it's ready to install.",
				"Downloading update", JOptionPane.INFORMATION_MESSAGE);

		File targetDir = resolveDownloadDir();
		UpdateChecker.downloadPackage(
				assetUrl,
				targetDir,
				pct -> System.out.println("[Updater] Downloading update... " + pct + "%"),
				outFile -> SwingUtilities.invokeLater(() -> onDownloadComplete(outFile)),
				errMsg -> SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
						"The automatic download failed: " + errMsg
								+ "\nPlease download the update manually from the Releases page.",
						"Update failed", JOptionPane.WARNING_MESSAGE)));
	}

	private static void onDownloadComplete(File outFile) {
		String msg = "Update downloaded successfully!\n\nSaved to:\n" + outFile.getAbsolutePath()
				+ "\n\nExtract the archive and run it to finish updating.\n"
				+ "(You can keep using this version until then.)";
		int choice = JOptionPane.showConfirmDialog(null, msg + "\n\nOpen the containing folder now?", "Update ready",
				JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
		if (choice == JOptionPane.YES_OPTION) {
			try {
				Desktop.getDesktop().open(outFile.getParentFile());
			} catch (Exception e) {
				System.err.println("[Updater] Could not open folder: " + e.getMessage());
			}
		}
	}

	private static File resolveDownloadDir() {
		File downloads = new File(System.getProperty("user.home"), "Downloads");
		if (downloads.isDirectory() || downloads.mkdirs()) {
			return downloads;
		}
		return new File(System.getProperty("user.home"));
	}

	private static void openInBrowser(String releaseUrl) {
		try {
			Desktop.getDesktop().browse(new URI(releaseUrl));
		} catch (Exception e) {
			System.err.println("Could not open browser: " + e.getMessage());
		}
	}
}
