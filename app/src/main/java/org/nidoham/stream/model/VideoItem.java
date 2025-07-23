package org.nidoham.stream.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.Image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VideoItem {
    private String title;
    private String uploader;
    private List<Image> thumbnails;
    private long duration;
    private long viewCount;
    private String description;
    private String uploadDate;
    private List<VideoStream> videoStreams;
    private List<VideoStream> videoOnlyStreams;  // Reintroduced
    private List<AudioStream> audioStreams;

    // Basic getters
    @Nullable public String getTitle() { return title; }
    @Nullable public String getUploader() { return uploader; }
    @Nullable public List<Image> getThumbnails() { return thumbnails; }
    @Nullable public String getDescription() { return description; }
    @Nullable public String getUploadDate() { return uploadDate; }
    @Nullable public List<VideoStream> getVideoStreams() { return videoStreams; }
    @Nullable public List<VideoStream> getVideoOnlyStreams() { return videoOnlyStreams; }
    @Nullable public List<AudioStream> getAudioStreams() { return audioStreams; }

    public long getDuration() { return duration; }
    public long getViewCount() { return viewCount; }

    // Safe getters with defaults
    @NonNull public String getTitleOrEmpty() { return title != null ? title : ""; }
    @NonNull public String getUploaderOrEmpty() { return uploader != null ? uploader : ""; }
    @NonNull public String getDescriptionOrEmpty() { return description != null ? description : ""; }

    @NonNull public List<Image> getThumbnailsOrEmpty() {
        return thumbnails != null ? thumbnails : Collections.emptyList();
    }

    @NonNull public List<VideoStream> getVideoStreamsOrEmpty() {
        return videoStreams != null ? videoStreams : Collections.emptyList();
    }

    @NonNull public List<VideoStream> getVideoOnlyStreamsOrEmpty() {
        return videoOnlyStreams != null ? videoOnlyStreams : Collections.emptyList();
    }

    @NonNull public List<AudioStream> getAudioStreamsOrEmpty() {
        return audioStreams != null ? audioStreams : Collections.emptyList();
    }

    // Setters with validation
    public void setTitle(@Nullable String title) { this.title = title; }
    public void setUploader(@Nullable String uploader) { this.uploader = uploader; }
    public void setThumbnails(@Nullable List<Image> thumbnails) { this.thumbnails = thumbnails; }
    public void setDescription(@Nullable String description) { this.description = description; }
    public void setUploadDate(@Nullable String uploadDate) { this.uploadDate = uploadDate; }
    public void setVideoStreams(@Nullable List<VideoStream> videoStreams) { this.videoStreams = videoStreams; }
    public void setVideoOnlyStreams(@Nullable List<VideoStream> videoOnlyStreams) { this.videoOnlyStreams = videoOnlyStreams; }
    public void setAudioStreams(@Nullable List<AudioStream> audioStreams) { this.audioStreams = audioStreams; }

    public void setDuration(long duration) { this.duration = Math.max(0, duration); }
    public void setViewCount(long viewCount) { this.viewCount = Math.max(0, viewCount); }

    // Stream utility methods
    public boolean hasVideoStreams() { return !getVideoStreamsOrEmpty().isEmpty(); }
    public boolean hasVideoOnlyStreams() { return !getVideoOnlyStreamsOrEmpty().isEmpty(); }
    public boolean hasAudioStreams() { return !getAudioStreamsOrEmpty().isEmpty(); }
    public boolean hasAnyStreams() {
        return hasVideoStreams() || hasVideoOnlyStreams() || hasAudioStreams();
    }

    @NonNull
    public List<String> getAllStreamUrls() {
        List<String> urls = new ArrayList<>();

        for (VideoStream stream : getVideoStreamsOrEmpty()) {
            if (stream.getUrl() != null) urls.add(stream.getUrl());
        }

        for (VideoStream stream : getVideoOnlyStreamsOrEmpty()) {
            if (stream.getUrl() != null) urls.add(stream.getUrl());
        }

        for (AudioStream stream : getAudioStreamsOrEmpty()) {
            if (stream.getUrl() != null) urls.add(stream.getUrl());
        }

        return urls;
    }

    // Formatted output methods
    @NonNull
    public String getFormattedDuration() {
        if (duration <= 0) return "0:00";

        long hours = duration / 3600;
        long minutes = (duration % 3600) / 60;
        long seconds = duration % 60;

        return hours > 0 ?
                String.format("%d:%02d:%02d", hours, minutes, seconds) :
                String.format("%d:%02d", minutes, seconds);
    }

    @NonNull
    public String getFormattedViewCount() {
        if (viewCount < 1000) return String.valueOf(viewCount);
        if (viewCount < 1000000) return String.format("%.1fK", viewCount / 1000.0);
        if (viewCount < 1000000000) return String.format("%.1fM", viewCount / 1000000.0);
        return String.format("%.1fB", viewCount / 1000000000.0);
    }
}