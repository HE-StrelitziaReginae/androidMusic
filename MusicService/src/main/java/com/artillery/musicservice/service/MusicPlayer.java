package com.artillery.musicservice.service;

import android.media.MediaPlayer;

import androidx.annotation.Nullable;

import com.artillery.musicbase.utils.KLogUtils;
import com.artillery.musicservice.data.PlayList;
import com.artillery.musicservice.data.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ArtilleryOrchid
 */
public class MusicPlayer implements MusicListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private static final String TAG = "MusicPlayer";
    private MediaPlayer mPlayer;
    private PlayList mPlayList;
    private final List<Callback> mCallbacks = new ArrayList<>(2);
    private boolean isPaused;

    private MusicPlayer() {
        mPlayer = new MediaPlayer();
        mPlayList = new PlayList();
        mPlayer.setOnCompletionListener(this);
    }

    // 静态内部类，只有第一次使用时才会加载
    private static class SingletonHelper {
        private static final MusicPlayer INSTANCE = new MusicPlayer();
    }

    public static MusicPlayer getInstance() {
        return SingletonHelper.INSTANCE;
    }

    @Override
    public void setPlayList(PlayList list) {
        if (list == null) {
            list = new PlayList();
        }
        mPlayList = list;
    }

    @Override
    public boolean play() {
        KLogUtils.i("play: " + isPaused);
        if (isPaused) {
            mPlayer.start();
            notifyPlayStatusChanged(true);
            return true;
        }
        KLogUtils.i("prepare: " + mPlayList.prepare());
        if (mPlayList.prepare()) {
            Song song = mPlayList.getCurrentSong();
            try {
                mPlayer.reset();
                mPlayer.setDataSource(song.getPath());
                mPlayer.prepare();
                mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        KLogUtils.i("onPrepared: " + mp.toString());
                        // 文件准备好后开始播放
                        mPlayer.start();
                    }
                });
                notifyPlayStatusChanged(true);
            } catch (IOException e) {
                KLogUtils.e(TAG + e);
                notifyPlayStatusChanged(false);
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean play(PlayList list) {
        if (list == null) {
            return false;
        }
        isPaused = false;
        setPlayList(list);
        return play();
    }

    @Override
    public boolean play(PlayList list, int startIndex) {
        if (list == null || startIndex < 0 || startIndex >= list.getNumOfSongs()) {
            return false;
        }
        isPaused = false;
        list.setPlayingIndex(startIndex);
        setPlayList(list);
        return play();
    }

    @Override
    public boolean play(Song song) {
        KLogUtils.i("play: " + song);
        if (song == null) {
            return false;
        }
        isPaused = false;
        mPlayList.getSongs().clear();
        mPlayList.getSongs().add(song);
        return play();
    }

    @Override
    public boolean playLast() {
        KLogUtils.i("playLast: ");
        isPaused = false;
        boolean hasLast = mPlayList.hasLast();
        if (hasLast) {
            Song last = mPlayList.last();
            play();
            notifyPlayLast(last);
            return true;
        }
        return false;
    }

    @Override
    public boolean playNext() {
        KLogUtils.i("playNext: ");
        isPaused = false;
        boolean hasNext = mPlayList.hasNext(false);
        if (hasNext) {
            Song next = mPlayList.next();
            play();
            notifyPlayNext(next);
            return true;
        }
        return false;
    }

    @Override
    public boolean pause() {
        KLogUtils.i("pause: ");
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            isPaused = true;
            notifyPlayStatusChanged(false);
            return true;
        }
        return false;
    }

    @Override
    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    @Override
    public int getProgress() {
        return mPlayer.getCurrentPosition();
    }

    @Nullable
    @Override
    public Song getPlayingSong() {
        return mPlayList.getCurrentSong();
    }

    @Override
    public long currentPosition() {
        return mPlayer.getCurrentPosition();
    }

    @Override
    public boolean seekTo(int progress) {
        if (mPlayList.getSongs().isEmpty()) {
            return false;
        }
        Song currentSong = mPlayList.getCurrentSong();
        if (currentSong != null) {
            if (currentSong.getDuration() <= progress) {
                onCompletion(mPlayer);
            } else {
                mPlayer.seekTo(progress);
            }
            return true;
        }
        return false;
    }

    @Override
    public void setPlayMode(MusicMode playMode) {
        mPlayList.setPlayMode(playMode);
    }

    //Listener
    @Override
    public void onCompletion(MediaPlayer mp) {
        Song next = null;
        if (mPlayList.getPlayMode() == MusicMode.LIST && mPlayList.getPlayingIndex() == mPlayList.getNumOfSongs() - 1) {
            //TODO
            KLogUtils.i("onCompletion: ");
        } else if (mPlayList.getPlayMode() == MusicMode.SINGLE) {
            next = mPlayList.getCurrentSong();
            play();
        } else {
            boolean hasNext = mPlayList.hasNext(true);
            if (hasNext) {
                next = mPlayList.next();
                play();
            }
        }
        notifyComplete(next);
    }

    @Override
    public void releasePlayer() {
        mPlayList = null;
        mPlayer.reset();
        mPlayer.release();
        mPlayer = null;
    }

    // Callbacks
    @Override
    public void registerCallback(Callback callback) {
        mCallbacks.add(callback);
    }

    @Override
    public void unregisterCallback(Callback callback) {
        mCallbacks.remove(callback);
    }

    @Override
    public void removeCallbacks() {
        mCallbacks.clear();
    }

    private void notifyPlayStatusChanged(boolean isPlaying) {
        mCallbacks.forEach(callback -> {
            callback.onPlayStatusChanged(isPlaying);
        });
    }

    private void notifyPlayLast(Song song) {
        mCallbacks.forEach(callback -> {
            callback.onSwitchLast(song);
        });
    }

    private void notifyPlayNext(Song song) {
        mCallbacks.forEach(callback -> {
            callback.onSwitchNext(song);
        });
    }

    private void notifyComplete(Song song) {
        mCallbacks.forEach(callback -> {
            callback.onComplete(song);
        });
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }
}
