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
    private long mPlaybackGeneration;

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
    public synchronized void setPlayList(PlayList list) {
        if (list == null) {
            list = new PlayList();
        }
        mPlayList = list;
    }

    @Override
    public synchronized void syncPlayList(ArrayList<Song> songs) {
        if (mPlayList == null) {
            mPlayList = new PlayList();
        }

        int oldIndex = mPlayList.getPlayingIndex();
        Song currentSong = null;
        ArrayList<Song> oldSongs = mPlayList.getSongs();
        if (oldIndex >= 0 && oldIndex < oldSongs.size()) {
            currentSong = oldSongs.get(oldIndex);
        }

        ArrayList<Song> updatedSongs = songs == null ? new ArrayList<>() : new ArrayList<>(songs);
        int updatedIndex = PlayList.NO_POSITION;
        if (currentSong != null) {
            for (int index = 0; index < updatedSongs.size(); index++) {
                if (sameSong(updatedSongs.get(index), currentSong)) {
                    updatedIndex = index;
                    break;
                }
            }
        } else if (oldIndex >= 0 && !updatedSongs.isEmpty()) {
            updatedIndex = Math.min(oldIndex, updatedSongs.size() - 1);
        }

        mPlayList.setSongs(updatedSongs);
        mPlayList.setNumOfSongs(updatedSongs.size());
        mPlayList.setPlayingIndex(updatedIndex);
    }

    private boolean sameSong(Song first, Song second) {
        if (first == null || second == null) {
            return first == second;
        }
        if (first.getPath() == null) {
            return second.getPath() == null;
        }
        return first.getPath().equals(second.getPath());
    }

    @Override
    public synchronized boolean play() {
        KLogUtils.i("play: " + isPaused);
        if (isPaused) {
            try {
                mPlayer.start();
                isPaused = false;
                notifyPlayStatusChanged(true);
                return true;
            } catch (IllegalStateException e) {
                KLogUtils.e(TAG + e);
                isPaused = false;
                notifyPlayStatusChanged(false);
                return false;
            }
        }
        KLogUtils.i("prepare: " + mPlayList.prepare());
        if (mPlayList.prepare()) {
            Song song = mPlayList.getCurrentSong();
            if (prepareAndStart(song)) {
                notifyPlayStatusChanged(true);
                return true;
            }
            notifyPlayStatusChanged(false);
        }
        return false;
    }

    private boolean prepareAndStart(Song song) {
        final long generation = ++mPlaybackGeneration;
        final MediaPlayer nextPlayer = new MediaPlayer();
        final MediaPlayer previousPlayer = mPlayer;

        nextPlayer.setOnCompletionListener(this);
        nextPlayer.setOnPreparedListener(mp -> {
            synchronized (MusicPlayer.this) {
                if (generation != mPlaybackGeneration || mp != mPlayer) {
                    return;
                }
                try {
                    mp.start();
                    isPaused = false;
                } catch (IllegalStateException e) {
                    KLogUtils.e(TAG + e);
                    notifyPlayStatusChanged(false);
                }
            }
        });

        mPlayer = nextPlayer;
        try {
            nextPlayer.setDataSource(song.getPath());
            nextPlayer.prepareAsync();
            releaseMediaPlayer(previousPlayer);
            return true;
        } catch (IOException | IllegalArgumentException | IllegalStateException | SecurityException e) {
            KLogUtils.e(TAG + e);
            if (mPlayer == nextPlayer) {
                mPlayer = previousPlayer;
            }
            releaseMediaPlayer(nextPlayer);
            return false;
        }
    }

    private void releaseMediaPlayer(MediaPlayer player) {
        if (player == null) {
            return;
        }
        try {
            player.setOnCompletionListener(null);
            player.setOnPreparedListener(null);
            player.reset();
        } catch (IllegalStateException e) {
            KLogUtils.e(TAG + e);
        } finally {
            player.release();
        }
    }

    @Override
    public synchronized boolean play(PlayList list) {
        if (list == null) {
            return false;
        }
        isPaused = false;
        setPlayList(list);
        return play();
    }

    @Override
    public synchronized boolean play(PlayList list, int startIndex) {
        if (list == null || startIndex < 0 || startIndex >= list.getNumOfSongs()) {
            return false;
        }
        isPaused = false;
        list.setPlayingIndex(startIndex);
        setPlayList(list);
        return play();
    }

    @Override
    public synchronized boolean play(Song song) {
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
    public synchronized boolean playLast() {
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
    public synchronized boolean playNext() {
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
    public synchronized boolean pause() {
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
    public synchronized boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    @Override
    public synchronized int getProgress() {
        return mPlayer.getCurrentPosition();
    }

    @Nullable
    @Override
    public synchronized Song getPlayingSong() {
        return mPlayList.getCurrentSong();
    }

    @Override
    public synchronized long currentPosition() {
        return mPlayer.getCurrentPosition();
    }

    @Override
    public synchronized boolean seekTo(int progress) {
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
    public synchronized void setPlayMode(MusicMode playMode) {
        mPlayList.setPlayMode(playMode);
    }

    @Override
    public synchronized MusicMode getPlayMode() {
        return mPlayList.getPlayMode();
    }

    //Listener
    @Override
    public synchronized void onCompletion(MediaPlayer mp) {
        if (mp != mPlayer) {
            return;
        }
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
    public synchronized void releasePlayer() {
        ++mPlaybackGeneration;
        mPlayList = null;
        releaseMediaPlayer(mPlayer);
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
