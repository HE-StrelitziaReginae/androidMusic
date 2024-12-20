package com.artillery.musicmain.data;

import com.artillery.musicmain.observer.MusicObserver;
import com.artillery.musicservice.data.Song;

import java.util.ArrayList;

/**
 * @author ArtilleryOrchid
 */
public class MusicDaraListener {
    private MusicObserver mMusicObserver;
    private MusicDataListener mMusicDataListener;

    private static class Holder {
        private static final MusicDaraListener instance = new MusicDaraListener();
    }

    public static MusicDaraListener getInstance() {
        return MusicDaraListener.Holder.instance;
    }


    public void observerMusicData(ArrayList<Song> songList, int startIndex, Song song) {
        mMusicDataListener.sendMusicSong(songList, startIndex, song);
    }

    public void setMusicDataListener(MusicDataListener mMusicDataListener) {
        this.mMusicDataListener = mMusicDataListener;
    }

    public void setUpdate(Song song) {
        mMusicObserver.update(song);
    }

    public void setUpdateUi(boolean isPlaying) {
        mMusicObserver.updateUi(isPlaying);
    }

    public void setMusicObserver(MusicObserver musicObserver) {
        mMusicObserver = musicObserver;
    }
}
