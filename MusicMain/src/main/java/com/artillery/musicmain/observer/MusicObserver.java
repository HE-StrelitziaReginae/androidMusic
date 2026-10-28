package com.artillery.musicmain.observer;

import androidx.annotation.Nullable;

import com.artillery.musicservice.data.Song;

public interface MusicObserver {
    void update(@Nullable Song song);

    void updateUi(boolean isPlaying);

    void updatePosition(long position);
}
