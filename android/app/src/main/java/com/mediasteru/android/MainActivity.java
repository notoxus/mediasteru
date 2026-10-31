package com.mediasteru.android;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mediasteru.android.adapter.QueueAdapter;
import com.mediasteru.android.dialog.DownloadOptionsBottomSheet;
import com.mediasteru.android.dialog.TrimBottomSheet;
import com.mediasteru.android.model.AndroidTaskItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity implements QueueAdapter.OnItemActionListener {

    private EditText editUrl;
    private RecyclerView recyclerQueue;
    private View layoutEmptyQueue;
    private TextView txtQueueCount;

    private final List<AndroidTaskItem> queueList = new ArrayList<>();
    private QueueAdapter adapter;
    private final AtomicInteger idCounter = new AtomicInteger(1);

    private ActivityResultLauncher<Intent> hunterLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editUrl = findViewById(R.id.editUrl);
        recyclerQueue = findViewById(R.id.recyclerQueue);
        layoutEmptyQueue = findViewById(R.id.layoutEmptyQueue);
        txtQueueCount = findViewById(R.id.txtQueueCount);

        adapter = new QueueAdapter(this, queueList, this);
        recyclerQueue.setLayoutManager(new LinearLayoutManager(this));
        recyclerQueue.setAdapter(adapter);

        setupHunterLauncher();
        setupActionButtons();
        handleIncomingIntent(getIntent());
        updateEmptyState();
    }

    private void setupHunterLauncher() {
        hunterLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String streamUrl = result.getData().getStringExtra(HunterActivity.RESULT_STREAM_URL);
                        if (streamUrl != null && !streamUrl.isBlank()) {
                            addNewQueueItem(streamUrl, "Captured Stream");
                            Toast.makeText(this, "🎯 Stream added to queue!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void setupActionButtons() {
        findViewById(R.id.btnPaste).setOnClickListener(v -> pasteFromClipboard());

        findViewById(R.id.btnHunt).setOnClickListener(v -> {
            String url = editUrl.getText().toString().trim();
            Intent intent = new Intent(this, HunterActivity.class);
            if (!url.isEmpty()) {
                intent.putExtra(HunterActivity.EXTRA_INITIAL_URL, url);
            }
            hunterLauncher.launch(intent);
        });

        findViewById(R.id.btnImportApi).setOnClickListener(v -> showApiImportDialog());

        findViewById(R.id.btnVjCue).setOnClickListener(v -> {
            Intent intent = new Intent(this, VjCueActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnClearQueue).setOnClickListener(v -> {
            queueList.clear();
            adapter.notifyDataSetChanged();
            updateEmptyState();
        });
    }

    private void pasteFromClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip() && clipboard.getPrimaryClip().getItemCount() > 0) {
            CharSequence text = clipboard.getPrimaryClip().getItemAt(0).getText();
            if (text != null) {
                String url = text.toString().trim();
                editUrl.setText(url);
                addNewQueueItem(url, url);
                Toast.makeText(this, "Link pasted from clipboard!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void showApiImportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Import Batch Links / JSON API");

        final EditText input = new EditText(this);
        input.setHint("Paste JSON or link list (one URL per line)...");
        input.setMinLines(5);
        input.setPadding(32, 24, 32, 24);
        builder.setView(input);

        builder.setPositiveButton("Add to Queue", (dialog, which) -> {
            String content = input.getText().toString().trim();
            if (!content.isEmpty()) {
                String[] lines = content.split("\\r?\\n");
                int added = 0;
                for (String line : lines) {
                    String clean = line.trim();
                    if (clean.startsWith("http://") || clean.startsWith("https://")) {
                        addNewQueueItem(clean, clean);
                        added++;
                    }
                }
                Toast.makeText(this, "Added " + added + " items to queue", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void addNewQueueItem(String url, String title) {
        AndroidTaskItem item = new AndroidTaskItem(idCounter.getAndIncrement(), url, title);
        queueList.add(item);
        adapter.notifyItemInserted(queueList.size() - 1);
        updateEmptyState();
    }

    private void updateEmptyState() {
        boolean isEmpty = queueList.isEmpty();
        layoutEmptyQueue.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerQueue.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        txtQueueCount.setText(queueList.size() + " items");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent != null && Intent.ACTION_SEND.equals(intent.getAction()) && "text/plain".equals(intent.getType())) {
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText != null && !sharedText.isBlank()) {
                String url = sharedText.trim();
                editUrl.setText(url);
                addNewQueueItem(url, url);
                Toast.makeText(this, "Received shared link!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onStart(AndroidTaskItem item) {
        item.setStatus("Downloading...");
        item.setProgress(15);
        item.setSpeed("2.8 MB/s");
        int index = queueList.indexOf(item);
        if (index != -1) {
            adapter.notifyItemChanged(index);
        }
        Toast.makeText(this, "Starting download: " + item.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onOptions(AndroidTaskItem item) {
        DownloadOptionsBottomSheet sheet = new DownloadOptionsBottomSheet(item, (format, quality) -> {
            item.setFormat(format);
            item.setQuality(quality);
            int index = queueList.indexOf(item);
            if (index != -1) {
                adapter.notifyItemChanged(index);
            }
        });
        sheet.show(getSupportFragmentManager(), "options_sheet");
    }

    @Override
    public void onTrim(AndroidTaskItem item) {
        TrimBottomSheet sheet = new TrimBottomSheet(item, (trimSection, preciseCut) -> {
            item.setTrimSection(trimSection);
            item.setPreciseCut(preciseCut);
            int index = queueList.indexOf(item);
            if (index != -1) {
                adapter.notifyItemChanged(index);
            }
        });
        sheet.show(getSupportFragmentManager(), "trim_sheet");
    }

    @Override
    public void onDelete(AndroidTaskItem item) {
        int index = queueList.indexOf(item);
        if (index != -1) {
            queueList.remove(index);
            adapter.notifyItemRemoved(index);
            updateEmptyState();
        }
    }
}
