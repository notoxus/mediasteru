package com.mediasteru.android.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.mediasteru.android.R;
import com.mediasteru.android.model.AndroidTaskItem;

import java.util.List;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.QueueViewHolder> {

    public interface OnItemActionListener {
        void onStart(AndroidTaskItem item);
        void onOptions(AndroidTaskItem item);
        void onTrim(AndroidTaskItem item);
        void onDelete(AndroidTaskItem item);
    }

    private final Context context;
    private final List<AndroidTaskItem> items;
    private final OnItemActionListener listener;

    public QueueAdapter(Context context, List<AndroidTaskItem> items, OnItemActionListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_queue, parent, false);
        return new QueueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QueueViewHolder holder, int position) {
        AndroidTaskItem item = items.get(position);
        holder.txtTitle.setText(item.getTitle());
        holder.txtFormat.setText(item.getFormatLabel());
        holder.txtStatus.setText(item.getStatus());
        holder.txtSpeed.setText(item.getSpeed());

        holder.progressBar.setProgress(item.getProgress());

        int statusColor;
        String statusLower = item.getStatus().toLowerCase();
        if (statusLower.contains("downloading")) {
            statusColor = ContextCompat.getColor(context, R.color.badge_downloading);
        } else if (statusLower.contains("completed")) {
            statusColor = ContextCompat.getColor(context, R.color.badge_completed);
        } else if (statusLower.contains("failed") || statusLower.contains("error")) {
            statusColor = ContextCompat.getColor(context, R.color.badge_failed);
        } else if (statusLower.contains("captured")) {
            statusColor = ContextCompat.getColor(context, R.color.badge_captured);
        } else {
            statusColor = ContextCompat.getColor(context, R.color.badge_waiting);
        }
        holder.txtStatus.setTextColor(statusColor);

        holder.btnStart.setOnClickListener(v -> {
            if (listener != null) listener.onStart(item);
        });
        holder.btnOptions.setOnClickListener(v -> {
            if (listener != null) listener.onOptions(item);
        });
        holder.btnTrim.setOnClickListener(v -> {
            if (listener != null) listener.onTrim(item);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class QueueViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtFormat, txtStatus, txtSpeed;
        LinearProgressIndicator progressBar;
        MaterialButton btnOptions, btnTrim, btnStart;
        ImageButton btnDelete;

        public QueueViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtItemTitle);
            txtFormat = itemView.findViewById(R.id.txtItemFormat);
            txtStatus = itemView.findViewById(R.id.txtItemStatus);
            txtSpeed = itemView.findViewById(R.id.txtItemSpeed);
            progressBar = itemView.findViewById(R.id.progressItem);
            btnOptions = itemView.findViewById(R.id.btnItemOptions);
            btnTrim = itemView.findViewById(R.id.btnItemTrim);
            btnStart = itemView.findViewById(R.id.btnItemStart);
            btnDelete = itemView.findViewById(R.id.btnItemDelete);
        }
    }
}
