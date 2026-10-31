package com.mediasteru.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class HunterActivity extends AppCompatActivity {

    public static final String EXTRA_INITIAL_URL = "extra_initial_url";
    public static final String RESULT_STREAM_URL = "result_stream_url";
    public static final String RESULT_REFERER = "result_referer";

    private WebView webView;
    private TextView txtUrl, txtDetectedStreamUrl;
    private ProgressBar progressBar;
    private View bannerDetected;

    private String lastCapturedStream = null;
    private String lastReferer = null;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunter);

        txtUrl = findViewById(R.id.txtHunterUrl);
        txtDetectedStreamUrl = findViewById(R.id.txtDetectedStreamUrl);
        progressBar = findViewById(R.id.hunterProgress);
        bannerDetected = findViewById(R.id.bannerStreamDetected);
        webView = findViewById(R.id.hunterWebView);

        findViewById(R.id.btnHunterBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnHunterRefresh).setOnClickListener(v -> webView.reload());

        findViewById(R.id.btnCaptureToQueue).setOnClickListener(v -> {
            if (lastCapturedStream != null) {
                Intent result = new Intent();
                result.putExtra(RESULT_STREAM_URL, lastCapturedStream);
                result.putExtra(RESULT_REFERER, lastReferer);
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        });

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36");

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                txtUrl.setText(url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                txtUrl.setText(url);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String requestUrl = request.getUrl().toString();
                if (looksLikeStream(requestUrl)) {
                    runOnUiThread(() -> onStreamDetected(requestUrl, view.getUrl()));
                }
                return super.shouldInterceptRequest(view, request);
            }
        });

        String targetUrl = getIntent().getStringExtra(EXTRA_INITIAL_URL);
        if (targetUrl == null || targetUrl.isBlank()) {
            targetUrl = "https://www.google.com";
        }
        webView.loadUrl(targetUrl);
    }

    private boolean looksLikeStream(String url) {
        String lower = url.toLowerCase();
        if (lower.contains(".m3u8") || lower.contains(".m3u") || lower.contains(".mpd")) {
            return !lower.contains("audio-only") && !lower.contains("/preview");
        }
        return lower.contains("format=m3u8") || lower.contains("format=hls")
                || lower.contains("protocol=hls") || lower.contains("manifest")
                || lower.contains("master.m3u8") || lower.contains("playlist.m3u8");
    }

    private void onStreamDetected(String streamUrl, String pageUrl) {
        lastCapturedStream = streamUrl;
        lastReferer = pageUrl;
        txtDetectedStreamUrl.setText(streamUrl);
        bannerDetected.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
