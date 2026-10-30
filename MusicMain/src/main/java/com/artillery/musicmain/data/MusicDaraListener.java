package com.artillery.musicmain.data;

import com.artillery.musicmain.observer.MusicObserver;
import com.artillery.musicservice.data.Song;
import com.artillery.musicservice.service.MusicListener;

import java.util.ArrayList;

/**
 * @author ArtilleryOrchid
 */
public class MusicDaraListener {
    private MusicObserver mMusicObserver;
    private MusicDataListener mMusicDataListener;
    private MusicListener mMusicListener;

    private static class Holder {
        private static final MusicDaraListener instance = new MusicDaraListener();
    }

    public static MusicDaraListener getInstance() {
        return MusicDaraListener.Holder.instance;
    }


    public void observerMusicData(ArrayList<Song> songList, int startIndex, Song song) {
        if (mMusicDataListener != null) {
            mMusicDataListener.sendMusicSong(songList, startIndex, song);
        }
    }

    public void updateMusicList(ArrayList<Song> songList) {
        if (mMusicDataListener != null) {
            mMusicDataListener.onMusicListUpdated(songList);
        }
    }

    public void setMusicDataListener(MusicDataListener mMusicDataListener) {
        this.mMusicDataListener = mMusicDataListener;
    }

    public void setMusicListener(MusicListener musicListener) {
        mMusicListener = musicListener;
    }

    public Song getPlayingSong() {
        return mMusicListener == null ? null : mMusicListener.getPlayingSong();
    }

    public boolean playOnline(Song song) {
        return mMusicListener != null && mMusicListener.playOnline(song);
    }

    public void setUpdate(Song song) {
        if (mMusicObserver != null) {
            mMusicObserver.update(song);
        }
    }

    public void setUpdateUi(boolean isPlaying) {
        if (mMusicObserver != null) {
            mMusicObserver.updateUi(isPlaying);
        }
    }

    public void setUpdatePosition(long position) {
        if (mMusicObserver != null) {
            mMusicObserver.updatePosition(position);
        }
    }

    public void setMusicObserver(MusicObserver musicObserver) {
        mMusicObserver = musicObserver;
    }

    public void clearMusicDataListener() {
        mMusicDataListener = null;
    }

    public void clearMusicListener(MusicListener musicListener) {
        if (mMusicListener == musicListener) {
            mMusicListener = null;
        }
    }

    public void clearMusicObserver() {
        mMusicObserver = null;
    }
}
