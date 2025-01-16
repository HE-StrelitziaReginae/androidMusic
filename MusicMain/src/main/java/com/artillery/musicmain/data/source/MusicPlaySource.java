package com.artillery.musicmain.data.source;

import com.artillery.musicmain.data.source.contract.view.MusicPlayView;
import com.artillery.musicservice.data.Song;

/**
 * @author ArtilleryOrchid
 */
public interface MusicPlaySource {
    void bindMusicView(MusicPlayView musicPlayView);

    void retrieveLastPlayMode();

    void setSongAsFavorite(Song song, boolean favorite);

    void bindMusicService();

    void unBindMusicService();
}
