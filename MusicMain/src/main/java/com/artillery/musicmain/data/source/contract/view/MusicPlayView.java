package com.artillery.musicmain.data.source.contract.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.artillery.musicservice.data.Song;
import com.artillery.musicservice.service.MusicMode;
import com.artillery.musicservice.service.MusicService;

/**
 * @author ArtilleryOrchid
 */
public abstract interface MusicPlayView {
    void handleError(Throwable error);
    void onPlaybackServiceBound(MusicService service);

    void onPlaybackServiceUnbound();
    void onSongSetAsFavorite(@NonNull Song song);
    void onSongUpdated(@Nullable Song song);
    void updatePlayMode(MusicMode playMode);
    void updatePlayToggle(boolean play);
    void updateFavoriteToggle(boolean favorite);
}
