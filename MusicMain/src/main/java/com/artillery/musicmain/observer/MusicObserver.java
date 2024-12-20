package com.artillery.musicmain.observer;

import com.artillery.musicservice.data.Song;

public interface MusicObserver {
    void update(Song song);

    void updateUi(boolean isPlaying);
}
