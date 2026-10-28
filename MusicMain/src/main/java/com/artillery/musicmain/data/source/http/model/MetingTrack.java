package com.artillery.musicmain.data.source.http.model;

public class MetingTrack {
    public String title;
    public String author;
    public String url;
    public String pic;
    public String lrc;

    public String getSongId() {
        if (url == null) {
            return null;
        }
        String marker = "id=";
        int start = url.indexOf(marker);
        if (start < 0) {
            return null;
        }
        start += marker.length();
        int end = url.indexOf('&', start);
        return end < 0 ? url.substring(start) : url.substring(start, end);
    }
}
