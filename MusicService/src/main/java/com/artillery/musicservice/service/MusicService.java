package com.artillery.musicservice.service;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.artillery.musicbase.utils.KLogUtils;
import com.artillery.musicservice.data.PlayList;
import com.artillery.musicservice.data.Song;

/**
 * @author ArtilleryOrchid
 */
public class MusicService extends Service implements MusicListener, MusicListener.Callback {
    private static final int NOTIFICATION_ID = 1;
    private MusicPlayer mMusicPlayer;
    private final Binder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        KLogUtils.e("onCreate: StartService");
        mMusicPlayer = MusicPlayer.getInstance();
        mMusicPlayer.registerCallback(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            KLogUtils.e("onStartCommand action: " + action);
            if (MusicContext.ACTION_PLAY.equals(action)) {
                if (isPlaying()) {
                    pause();
                } else {
                    play();
                }
            } else if (MusicContext.ACTION_PLAY_PRE.equals(action)) {
                playLast();
            } else if (MusicContext.ACTION_PLAY_NEXT.equals(action)) {
                playNext();
            } else if (MusicContext.ACTION_STOP_SERVICE.equals(action)) {
                if (isPlaying()) {
                    pause();
                }
                stopForeground(true);
                unregisterCallback(this);
            }
        }
        return START_STICKY;
    }

    @Override
    public void setPlayList(PlayList list) {
        mMusicPlayer.setPlayList(list);
    }

    @Override
    public boolean play() {
        return mMusicPlayer.play();
    }

    @Override
    public boolean play(PlayList list) {
        return mMusicPlayer.play(list);
    }

    @Override
    public boolean play(PlayList list, int startIndex) {
        return mMusicPlayer.play(list, startIndex);
    }

    @Override
    public boolean play(Song song) {
        return mMusicPlayer.play(song);
    }

    @Override
    public boolean playLast() {
        return mMusicPlayer.playLast();
    }

    @Override
    public boolean playNext() {
        return mMusicPlayer.playNext();
    }

    @Override
    public boolean pause() {
        return mMusicPlayer.pause();
    }

    @Override
    public boolean isPlaying() {
        return mMusicPlayer.isPlaying();
    }

    @Override
    public int getProgress() {
        return mMusicPlayer.getProgress();
    }

    @Override
    public Song getPlayingSong() {
        return mMusicPlayer.getPlayingSong();
    }

    @Override
    public long currentPosition() {
        return mMusicPlayer.currentPosition();
    }

    @Override
    public boolean seekTo(int progress) {
        return mMusicPlayer.seekTo(progress);
    }

    @Override
    public void setPlayMode(MusicMode playMode) {
        mMusicPlayer.setPlayMode(playMode);
    }

    @Override
    public void registerCallback(Callback callback) {
        mMusicPlayer.registerCallback(callback);
    }

    @Override
    public void unregisterCallback(Callback callback) {
        mMusicPlayer.unregisterCallback(callback);
    }

    @Override
    public void removeCallbacks() {
        mMusicPlayer.removeCallbacks();
    }

    @Override
    public void releasePlayer() {
        mMusicPlayer.releasePlayer();
        super.onDestroy();
    }

    @Override
    public void onSwitchLast(@Nullable Song last) {
    }

    @Override
    public void onSwitchNext(@Nullable Song next) {

    }

    @Override
    public void onComplete(@Nullable Song next) {

    }

    @Override
    public void onPlayStatusChanged(boolean isPlaying) {

    }

    @SuppressLint({"WrongConstant", "UnspecifiedImmutableFlag"})
    private PendingIntent getPendingIntent(String action) {
        return PendingIntent.getService(this, MusicContext.ACTION_CODE, new Intent(action), MusicContext.ACTION_FLAG);
    }

    @Override
    public void onDestroy() {
        releasePlayer();
        super.onDestroy();
    }
}
