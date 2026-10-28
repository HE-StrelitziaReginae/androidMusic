package com.artillery.musicservice.data;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "playback_state")
public class PlaybackState {
    @PrimaryKey
    private int id = 1;
    @Nullable
    private String songPath;
    private long position;
    private boolean playing;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Nullable
    public String getSongPath() {
        return songPath;
    }

    public void setSongPath(@Nullable String songPath) {
        this.songPath = songPath;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = Math.max(0, position);
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }
}
