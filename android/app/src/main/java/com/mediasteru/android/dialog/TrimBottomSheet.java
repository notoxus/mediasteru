package com.mediasteru.android.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.mediasteru.android.R;
import com.mediasteru.android.model.AndroidTaskItem;

public class TrimBottomSheet extends BottomSheetDialogFragment {

    public interface OnTrimSavedListener {
        void onTrimSaved(String trimSection, boolean preciseCut);
    }

    private final AndroidTaskItem item;
    private final OnTrimSavedListener listener;

    public TrimBottomSheet(AndroidTaskItem item, OnTrimSavedListener listener) {
        this.item = item;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_trim, container, false);

        EditText editStart = view.findViewById(R.id.editTrimStart);
        EditText editEnd = view.findViewById(R.id.editTrimEnd);
        MaterialSwitch switchPrecise = view.findViewById(R.id.switchPreciseCut);

        if (item.getTrimSection() != null) {
            String section = item.getTrimSection().replace("*", "");
            String[] parts = section.split("-");
            if (parts.length >= 2) {
                editStart.setText(parts[0]);
                editEnd.setText(parts[1]);
            }
        }
        switchPrecise.setChecked(item.isPreciseCut());

        view.findViewById(R.id.btnCancelTrim).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btnApplyTrim).setOnClickListener(v -> {
            String start = editStart.getText().toString().trim();
            String end = editEnd.getText().toString().trim();
            if (start.isEmpty()) start = "00:00:00";
            if (end.isEmpty()) end = "00:01:00";

            String trimSection = "*" + start + "-" + end;
            boolean precise = switchPrecise.isChecked();

            if (listener != null) {
                listener.onTrimSaved(trimSection, precise);
            }
            dismiss();
        });

        return view;
    }
}
