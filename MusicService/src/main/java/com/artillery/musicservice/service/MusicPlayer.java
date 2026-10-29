package com.artillery.musicservice.service;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.artillery.musicbase.utils.KLogUtils;
import com.artillery.musicbase.utils.BackgroundThreadUtils;
import com.artillery.musicbase.utils.SPUtils;
import com.artillery.musicbase.utils.Utils;
import com.artillery.musicservice.data.PlaybackState;
import com.artillery.musicservice.data.PlayList;
import com.artillery.musicservice.data.Song;
import com.artillery.musicservice.db.PlaybackStateStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ArtilleryOrchid
 */
public class MusicPlayer implements MusicListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private static final String TAG = "MusicPlayer";
    private static final String KEY_PLAY_MODE = "play_mode";
    private MediaPlayer mPlayer;
    private PlayList mPlayList;
    private PlayList mLocalPlayList;
    private MusicMode mPlayMode = MusicMode.getDefault();
    private final List<Callback> mCallbacks = new ArrayList<>(2);
    private boolean isPaused;
    private boolean isPreparing;
    private long mPlaybackGeneration;
    private final PlaybackStateStore mPlaybackStateStore = new PlaybackStateStore();
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private boolean mPlaybackStateLoaded;
    private boolean mExplicitSelection;
    private boolean mOnlinePlayback;
    private long mRestoredPosition;
    private String mRestoredSongPath;
    private final Runnable mPlaybackStateSaver = new Runnable() {
        @Override
        public void run() {
            synchronized (MusicPlayer.this) {
                if (!isPlaying()) {
                    return;
                }
                persistPlaybackState(true, false);
                BackgroundThreadUtils.getInstance().postDelayed(this, 1000);
            }
        }
    };

    private MusicPlayer() {
        mPlayer = createMediaPlayer();
        mPlayList = new PlayList();
        mLocalPlayList = new PlayList();
        mPlayMode = loadPlayMode();
        mPlayList.setPlayMode(mPlayMode);
        mLocalPlayList.setPlayMode(mPlayMode);
        mPlaybackStateStore.load(this::onPlaybackStateLoaded);
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
        ensurePlayList();
        if (list == null) {
            list = new PlayList();
        }
        list.setPlayMode(mPlayMode);
        mLocalPlayList = list;
        mOnlinePlayback = false;
        mPlayList = list;
    }

    @Override
    public synchronized void syncPlayList(ArrayList<Song> songs) {
        ensurePlayList();

        if (mLocalPlayList == null) {
            mLocalPlayList = new PlayList();
        }
        int oldIndex = mLocalPlayList.getPlayingIndex();
        Song currentSong = null;
        ArrayList<Song> oldSongs = mLocalPlayList.getSongs();
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
        } else if (mPlaybackStateLoaded && mRestoredSongPath != null) {
            for (int index = 0; index < updatedSongs.size(); index++) {
                if (mRestoredSongPath.equals(updatedSongs.get(index).getPath())) {
                    updatedIndex = index;
                    break;
                }
            }
        } else if (!updatedSongs.isEmpty()) {
            updatedIndex = oldIndex >= 0 ? Math.min(oldIndex, updatedSongs.size() - 1) : 0;
        }

        mLocalPlayList.setSongs(updatedSongs);
        mLocalPlayList.setNumOfSongs(updatedSongs.size());
        mLocalPlayList.setPlayingIndex(updatedIndex);
        if (!mOnlinePlayback) {
            mPlayList = mLocalPlayList;
        }
        notifyPlaybackStateChanged(mPlayList.getCurrentSong(), currentPosition(), isPlaying());
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
        ensurePlayer();
        ensurePlayList();
        KLogUtils.i("play: " + isPaused);
        if (isPaused) {
            try {
                mPlayer.start();
                isPaused = false;
                persistPlaybackState(true, true);
                notifyPlayStatusChanged(true);
                return true;
            } catch (IllegalStateException e) {
                KLogUtils.e(TAG + e);
                isPaused = false;
                notifyPlayStatusChanged(false);
                return false;
            }
        }
        boolean prepared = mPlayList.prepare();
        KLogUtils.i("prepare: " + prepared);
        if (prepared) {
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
        if (song == null || song.getPath() == null || song.getPath().isEmpty()) {
            return false;
        }
        final long generation = ++mPlaybackGeneration;
        final MediaPlayer nextPlayer = createMediaPlayer();
        final MediaPlayer previousPlayer = mPlayer;

        nextPlayer.setOnCompletionListener(this);
        nextPlayer.setOnPreparedListener(mp -> {
            synchronized (MusicPlayer.this) {
                if (generation != mPlaybackGeneration || mp != mPlayer) {
                    return;
                }
                try {
                    int preparedDuration = mp.getDuration();
                    if (preparedDuration > 0 && (mOnlinePlayback || song.getDuration() <= 0)) {
                        song.setDuration(preparedDuration);
                    }
                    if (mRestoredSongPath != null
                            && mRestoredSongPath.equals(song.getPath())
                            && mRestoredPosition > 0
                            && mRestoredPosition < song.getDuration()) {
                        mp.seekTo((int) mRestoredPosition);
                    }
                    mRestoredSongPath = null;
                    mRestoredPosition = 0;
                    mp.start();
                    isPreparing = false;
                    isPaused = false;
                    persistPlaybackState(true, true);
                } catch (IllegalStateException e) {
                    KLogUtils.e(TAG + e);
                    notifyPlayStatusChanged(false);
                }
            }
        });

        mPlayer = nextPlayer;
        isPreparing = true;
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
            isPreparing = false;
            releaseMediaPlayer(nextPlayer);
            return false;
        }
    }

    private void onPlaybackStateLoaded(PlaybackState state) {
        synchronized (this) {
            mPlaybackStateLoaded = true;
            if (state != null) {
                mRestoredSongPath = state.getSongPath();
                mRestoredPosition = state.getPosition();
                if (!mExplicitSelection && mPlayList != null && mRestoredSongPath != null) {
                    ArrayList<Song> songs = mPlayList.getSongs();
                    for (int index = 0; index < songs.size(); index++) {
                        if (mRestoredSongPath.equals(songs.get(index).getPath())) {
                            mPlayList.setPlayingIndex(index);
                            break;
                        }
                    }
                }
            }
        }
        notifyPlaybackStateChanged(getPlayingSong(), currentPosition(), false);
    }

    private MediaPlayer createMediaPlayer() {
        MediaPlayer player = new MediaPlayer();
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        return player;
    }

    private void ensurePlayer() {
        if (mPlayer == null) {
            mPlayer = createMediaPlayer();
        }
    }

    private void ensurePlayList() {
        if (mPlayList == null) {
            mPlayList = new PlayList();
            mPlayList.setPlayMode(mPlayMode);
        }
    }

    private void releaseMediaPlayer(MediaPlayer player) {
        if (player == null) {
            return;
        }
        try {
            player.setOnCompletionListener(null);
            player.setOnPreparedListener(null);
            player.setOnErrorListener(null);
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
        mExplicitSelection = true;
        mOnlinePlayback = false;
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
        mExplicitSelection = true;
        mOnlinePlayback = false;
        mPlayList = new PlayList(song);
        mPlayList.setPlayMode(mPlayMode);
        mPlayList.setPlayingIndex(0);
        return play();
    }

    @Override
    public synchronized boolean playOnline(Song song) {
        if (song == null) {
            return false;
        }
        isPaused = false;
        mExplicitSelection = true;
        mOnlinePlayback = true;
        mPlayList = new PlayList(song);
        mPlayList.setPlayMode(mPlayMode);
        mPlayList.setPlayingIndex(0);
        return play();
    }

    @Override
    public synchronized boolean playLast() {
        KLogUtils.i("playLast: ");
        isPaused = false;
        mExplicitSelection = true;
        if (mOnlinePlayback && mLocalPlayList != null && !mLocalPlayList.getSongs().isEmpty()) {
            mOnlinePlayback = false;
            mPlayList = mLocalPlayList;
        }
        boolean hasLast = mPlayList.hasLast();
        if (hasLast) {
            Song last = mPlayList.last();
            if (play()) {
                notifyPlayLast(last);
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized boolean playNext() {
        KLogUtils.i("playNext: ");
        isPaused = false;
        mExplicitSelection = true;
        if (mOnlinePlayback && mLocalPlayList != null && !mLocalPlayList.getSongs().isEmpty()) {
            mOnlinePlayback = false;
            mPlayList = mLocalPlayList;
        }
        boolean hasNext = mPlayList.hasNext(false);
        if (hasNext) {
            Song next = mPlayList.next();
            if (play()) {
                notifyPlayNext(next);
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized boolean pause() {
        ensurePlayer();
        KLogUtils.i("pause: ");
        if (isPreparing) {
            ++mPlaybackGeneration;
            isPreparing = false;
            releaseMediaPlayer(mPlayer);
            mPlayer = createMediaPlayer();
            isPaused = false;
            persistPlaybackState(false, true);
            notifyPlayStatusChanged(false);
            return true;
        }
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            isPaused = true;
            persistPlaybackState(false, true);
            notifyPlayStatusChanged(false);
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean isPlaying() {
        return mPlayer != null && mPlayer.isPlaying();
    }

    @Override
    public synchronized int getProgress() {
        return mPlayer == null ? 0 : mPlayer.getCurrentPosition();
    }

    @Nullable
    @Override
    public synchronized Song getPlayingSong() {
        return mPlayList == null ? null : mPlayList.getCurrentSong();
    }

    @Override
    public synchronized long currentPosition() {
        long position = mPlayer == null ? 0 : mPlayer.getCurrentPosition();
        if (position == 0 && mRestoredPosition > 0 && isRestoredSongSelected()) {
            return mRestoredPosition;
        }
        return position;
    }

    private boolean isRestoredSongSelected() {
        if (mRestoredSongPath == null || mPlayList == null) {
            return false;
        }
        Song currentSong = mPlayList.getCurrentSong();
        return currentSong != null && mRestoredSongPath.equals(currentSong.getPath());
    }

    @Override
    public synchronized boolean seekTo(int progress) {
        ensurePlayer();
        ensurePlayList();
        if (mPlayList.getSongs().isEmpty()) {
            return false;
        }
        Song currentSong = mPlayList.getCurrentSong();
        if (currentSong != null) {
            if (currentSong.getDuration() <= progress) {
                onCompletion(mPlayer);
            } else {
                mPlayer.seekTo(progress);
                persistPlaybackState(isPlaying(), true);
            }
            return true;
        }
        return false;
    }

    @Override
    public synchronized void setPlayMode(MusicMode playMode) {
        ensurePlayList();
        mPlayMode = playMode == null ? MusicMode.getDefault() : playMode;
        mPlayList.setPlayMode(mPlayMode);
        if (mLocalPlayList != null) {
            mLocalPlayList.setPlayMode(mPlayMode);
        }
        savePlayMode(mPlayMode);
    }

    @Override
    public synchronized MusicMode getPlayMode() {
        return mPlayMode;
    }

    private MusicMode loadPlayMode() {
        String value = SPUtils.getInstance(Utils.getContext()).getString(KEY_PLAY_MODE, null);
        if (value == null) {
            return MusicMode.getDefault();
        }
        try {
            return MusicMode.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return MusicMode.getDefault();
        }
    }

    private void savePlayMode(MusicMode playMode) {
        SPUtils.getInstance(Utils.getContext()).put(KEY_PLAY_MODE, playMode.name());
    }

    //Listener
    @Override
    public synchronized void onCompletion(MediaPlayer mp) {
        if (mp != mPlayer || mPlayList == null) {
            return;
        }
        isPreparing = false;
        persistPlaybackState(false, true);
        Song next = null;
        if (mPlayList.getPlayMode() == MusicMode.LIST && mPlayList.getPlayingIndex() == mPlayList.getNumOfSongs() - 1) {
            isPaused = false;
            notifyPlayStatusChanged(false);
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
        releaseMediaPlayer(mPlayer);
        mPlayer = null;
        mPlayList = new PlayList();
        mLocalPlayList = new PlayList();
        mPlayList.setPlayMode(mPlayMode);
        mLocalPlayList.setPlayMode(mPlayMode);
        isPaused = false;
        isPreparing = false;
        mMainHandler.removeCallbacksAndMessages(null);
        persistPlaybackState(false, true);
    }

    // Callbacks
    @Override
    public synchronized void registerCallback(Callback callback) {
        if (callback != null && !mCallbacks.contains(callback)) {
            mCallbacks.add(callback);
        }
    }

    @Override
    public synchronized void unregisterCallback(Callback callback) {
        mCallbacks.remove(callback);
    }

    @Override
    public synchronized void removeCallbacks() {
        mCallbacks.clear();
    }

    private void notifyPlayStatusChanged(boolean isPlaying) {
        for (Callback callback : callbackSnapshot()) {
            callback.onPlayStatusChanged(isPlaying);
        }
    }

    private void notifyPlayLast(Song song) {
        for (Callback callback : callbackSnapshot()) {
            callback.onSwitchLast(song);
        }
    }

    private void notifyPlayNext(Song song) {
        for (Callback callback : callbackSnapshot()) {
            callback.onSwitchNext(song);
        }
    }

    private void notifyComplete(Song song) {
        for (Callback callback : callbackSnapshot()) {
            callback.onComplete(song);
        }
    }

    private synchronized Callback[] callbackSnapshot() {
        return mCallbacks.toArray(new Callback[0]);
    }

    private void persistPlaybackState(boolean playing, boolean notify) {
        Song currentSong = mPlayList == null ? null : mPlayList.getCurrentSong();
        long position = mPlayer == null ? 0 : mPlayer.getCurrentPosition();
        String songPath = currentSong == null ? null : currentSong.getPath();
        if (!mOnlinePlayback) {
            mPlaybackStateStore.save(songPath, position, playing);
        }
        notifyPlaybackPosition(position);
        if (notify) {
            notifyPlaybackStateChanged(currentSong, position, playing);
        }
        if (playing) {
            mMainHandler.removeCallbacks(mPlaybackStateSaver);
            mMainHandler.postDelayed(mPlaybackStateSaver, 1000);
        } else {
            mMainHandler.removeCallbacks(mPlaybackStateSaver);
        }
    }

    private void notifyPlaybackStateChanged(@Nullable Song song, long position, boolean playing) {
        mMainHandler.post(() -> {
            for (Callback callback : callbackSnapshot()) {
                callback.onPlaybackStateChanged(song, position, playing);
            }
        });
    }

    private void notifyPlaybackPosition(long position) {
        mMainHandler.post(() -> {
            for (Callback callback : callbackSnapshot()) {
                callback.onPlaybackStateChanged(getPlayingSong(), position, isPlaying());
            }
        });
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        synchronized (this) {
            if (mp != mPlayer) {
                return true;
            }
            isPaused = false;
            isPreparing = false;
            ++mPlaybackGeneration;
            releaseMediaPlayer(mPlayer);
            mPlayer = createMediaPlayer();
        }
        notifyPlayStatusChanged(false);
        return true;
    }
}
