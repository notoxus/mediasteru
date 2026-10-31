package com.mediasteru.android.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.ChipGroup;
import com.mediasteru.android.R;
import com.mediasteru.android.model.AndroidTaskItem;

public class DownloadOptionsBottomSheet extends BottomSheetDialogFragment {

    public interface OnOptionsSavedListener {
        void onOptionsSaved(String format, String quality);
    }

    private final AndroidTaskItem item;
    private final OnOptionsSavedListener listener;

    public DownloadOptionsBottomSheet(AndroidTaskItem item, OnOptionsSavedListener listener) {
        this.item = item;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_download_options, container, false);

        ChipGroup chipGroupFormat = view.findViewById(R.id.chipGroupFormat);
        ChipGroup chipGroupQuality = view.findViewById(R.id.chipGroupQuality);
        View labelQuality = view.findViewById(R.id.labelQuality);

        if ("mp3".equalsIgnoreCase(item.getFormat())) {
            chipGroupFormat.check(R.id.chipMp3);
            chipGroupQuality.setVisibility(View.GONE);
            labelQuality.setVisibility(View.GONE);
        } else if ("mkv".equalsIgnoreCase(item.getFormat())) {
            chipGroupFormat.check(R.id.chipMkv);
        } else {
            chipGroupFormat.check(R.id.chipMp4);
        }

        chipGroupFormat.setOnCheckedStateChangeListener((group, checkedIds) -> {
            boolean isMp3 = checkedIds.contains(R.id.chipMp3);
            chipGroupQuality.setVisibility(isMp3 ? View.GONE : View.VISIBLE);
            labelQuality.setVisibility(isMp3 ? View.GONE : View.VISIBLE);
        });

        if ("720".equals(item.getQuality())) {
            chipGroupQuality.check(R.id.chip720);
        } else if ("480".equals(item.getQuality())) {
            chipGroupQuality.check(R.id.chip480);
        } else if ("1080".equals(item.getQuality())) {
            chipGroupQuality.check(R.id.chip1080);
        } else {
            chipGroupQuality.check(R.id.chipBest);
        }

        view.findViewById(R.id.btnCancelOptions).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btnSaveOptions).setOnClickListener(v -> {
            String format = "mp4";
            int selectedFormatId = chipGroupFormat.getCheckedChipId();
            if (selectedFormatId == R.id.chipMp3) {
                format = "mp3";
            } else if (selectedFormatId == R.id.chipMkv) {
                format = "mkv";
            }

            String quality = "best";
            int selectedQualityId = chipGroupQuality.getCheckedChipId();
            if (selectedQualityId == R.id.chip1080) {
                quality = "1080";
            } else if (selectedQualityId == R.id.chip720) {
                quality = "720";
            } else if (selectedQualityId == R.id.chip480) {
                quality = "480";
            }

            if (listener != null) {
                listener.onOptionsSaved(format, quality);
            }
            dismiss();
        });

        return view;
    }
}
