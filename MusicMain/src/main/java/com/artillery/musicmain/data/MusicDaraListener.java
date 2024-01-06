package com.artillery.musicmain.data;

import com.artillery.musicservice.data.Song;

import java.util.ArrayList;

/**
 * @author ArtilleryOrchid
 */
public class MusicDaraListener {
    private static class Holder {
        private static MusicDaraListener instance = new MusicDaraListener();
    }

    public static MusicDaraListener getInstance() {
        return MusicDaraListener.Holder.instance;
    }

    private MusicDataListener mMusicDataListener;

    public void putMusicData(ArrayList<Song> mSongList, int mStartIndex, Song mSong) {
        mMusicDataListener.sendMusicList(mSongList, mStartIndex);
        mMusicDataListener.sendMusicSong(mSong);
    }

    public void setMusicDataListener(MusicDataListener mMusicDataListener) {
        this.mMusicDataListener = mMusicDataListener;
    }
}
