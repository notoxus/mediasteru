package com.mediasteru.android;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VjCueActivity extends AppCompatActivity {

    private final OkHttpClient httpClient = new OkHttpClient();
    private EditText editHostIp;
    private TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vj_cue);

        findViewById(R.id.btnVjBack).setOnClickListener(v -> finish());
        editHostIp = findViewById(R.id.editHostIp);
        txtStatus = findViewById(R.id.txtConnectionStatus);

        findViewById(R.id.btnConnectPc).setOnClickListener(v -> pingPcEngine());

        findViewById(R.id.btnCue1).setOnClickListener(v -> sendCueCommand("cue_1"));
        findViewById(R.id.btnCue2).setOnClickListener(v -> sendCueCommand("cue_2"));
        findViewById(R.id.btnCue3).setOnClickListener(v -> sendCueCommand("cue_3"));
        findViewById(R.id.btnCue4).setOnClickListener(v -> sendCueCommand("cue_4"));
        findViewById(R.id.btnPausePlayback).setOnClickListener(v -> sendCueCommand("toggle_pause"));
        findViewById(R.id.btnBlackout).setOnClickListener(v -> sendCueCommand("blackout"));
    }

    private String getBaseUrl() {
        String host = editHostIp.getText().toString().trim();
        if (!host.startsWith("http://") && !host.startsWith("https://")) {
            host = "http://" + host;
        }
        return host;
    }

    private void pingPcEngine() {
        String url = getBaseUrl() + "/ping";
        txtStatus.setText("Checking connection...");

        Request request = new Request.Builder().url(url).build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    txtStatus.setText("❌ Connection failed: " + e.getMessage());
                    txtStatus.setTextColor(getColor(R.color.badge_failed));
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                boolean ok = response.isSuccessful();
                runOnUiThread(() -> {
                    if (ok) {
                        txtStatus.setText("✅ Connected to MediaSteru Desktop Engine!");
                        txtStatus.setTextColor(getColor(R.color.badge_completed));
                    } else {
                        txtStatus.setText("⚠️ PC returned status code: " + response.code());
                        txtStatus.setTextColor(getColor(R.color.badge_waiting));
                    }
                });
            }
        });
    }

    private void sendCueCommand(String action) {
        String url = getBaseUrl() + "/vj/cue";
        String json = "{\"action\":\"" + action + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        Request request = new Request.Builder().url(url).post(body).build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(VjCueActivity.this, "Failed to send command: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> Toast.makeText(VjCueActivity.this, "VJ Cue sent: " + action, Toast.LENGTH_SHORT).show());
            }
        });
    }
}
