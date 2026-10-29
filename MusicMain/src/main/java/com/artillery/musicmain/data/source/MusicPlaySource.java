package com.artillery.musicmain.data.source;

import com.artillery.musicmain.data.source.contract.view.MusicPlayView;

/**
 * @author ArtilleryOrchid
 */
public interface MusicPlaySource {
    void bindMusicView(MusicPlayView musicPlayView);

    void bindMusicService();

    void unBindMusicService();
}
