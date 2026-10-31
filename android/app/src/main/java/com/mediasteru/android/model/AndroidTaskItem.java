package com.mediasteru.android.model;

import java.io.Serializable;

public class AndroidTaskItem implements Serializable {
    private int id;
    private String url;
    private String title;
    private String format = "mp4";
    private String quality = "1080";
    private String trimSection = null;
    private boolean preciseCut = false;
    private String status = "Waiting...";
    private int progress = 0;
    private String speed = "";

    public AndroidTaskItem(int id, String url, String title) {
        this.id = id;
        this.url = url;
        this.title = title;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getTitle() { return title != null && !title.isBlank() ? title : url; }
    public void setTitle(String title) { this.title = title; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }

    public String getTrimSection() { return trimSection; }
    public void setTrimSection(String trimSection) { this.trimSection = trimSection; }

    public boolean isPreciseCut() { return preciseCut; }
    public void setPreciseCut(boolean preciseCut) { this.preciseCut = preciseCut; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public String getSpeed() { return speed; }
    public void setSpeed(String speed) { this.speed = speed; }

    public String getFormatLabel() {
        if ("mp3".equalsIgnoreCase(format)) {
            return "MP3" + (trimSection != null ? " ✂" : "");
        }
        String q = "best".equalsIgnoreCase(quality) ? "Best" : quality + "p";
        return format.toUpperCase() + " · " + q + (trimSection != null ? " ✂" : "");
    }
}
