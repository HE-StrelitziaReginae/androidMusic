package com.artillery.musicservice.service;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.artillery.musicbase.helper.NotificationHelper;
import com.artillery.musicbase.utils.KLogUtils;
import com.artillery.musicservice.data.PlayList;
import com.artillery.musicservice.data.Song;

import java.util.ArrayList;

/**
 * @author ArtilleryOrchid
 */
public class MusicService extends Service implements MusicListener, MusicListener.Callback {
    private static final int NOTIFICATION_ID = 1;
    private MusicPlayer mMusicPlayer;
    private NotificationHelper mNotificationHelper;
    private boolean mForegroundStarted;
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
        mNotificationHelper = NotificationHelper.getInstance(this);
        mNotificationHelper.setSeekListener(position -> seekTo((int) Math.max(0, position)));
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
                mNotificationHelper.clearPlaybackNotification(NOTIFICATION_ID);
                stopForeground(true);
                mForegroundStarted = false;
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
    public void syncPlayList(ArrayList<Song> songs) {
        mMusicPlayer.syncPlayList(songs);
    }

    @Override
    public boolean play() {
        boolean result = mMusicPlayer.play();
        updateNotification();
        return result;
    }

    @Override
    public boolean play(PlayList list) {
        boolean result = mMusicPlayer.play(list);
        updateNotification();
        return result;
    }

    @Override
    public boolean play(PlayList list, int startIndex) {
        boolean result = mMusicPlayer.play(list, startIndex);
        updateNotification();
        return result;
    }

    @Override
    public boolean play(Song song) {
        boolean result = mMusicPlayer.play(song);
        updateNotification();
        return result;
    }

    @Override
    public boolean playOnline(Song song) {
        boolean result = mMusicPlayer.playOnline(song);
        updateNotification();
        return result;
    }

    @Override
    public boolean playLast() {
        boolean result = mMusicPlayer.playLast();
        updateNotification();
        return result;
    }

    @Override
    public boolean playNext() {
        boolean result = mMusicPlayer.playNext();
        updateNotification();
        return result;
    }

    @Override
    public boolean pause() {
        boolean result = mMusicPlayer.pause();
        updateNotification();
        return result;
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
    public MusicMode getPlayMode() {
        return mMusicPlayer.getPlayMode();
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
    }

    @Override
    public void onSwitchLast(@Nullable Song last) {
        updateNotification();
    }

    @Override
    public void onSwitchNext(@Nullable Song next) {
        updateNotification();
    }

    @Override
    public void onComplete(@Nullable Song next) {
        updateNotification();
    }

    @Override
    public void onPlayStatusChanged(boolean isPlaying) {
        updateNotification();
    }

    @Override
    public void onPlaybackStateChanged(@Nullable Song song, long position, boolean isPlaying) {
        updateNotification(song, position, isPlaying);
    }

    @SuppressLint({"WrongConstant", "UnspecifiedImmutableFlag"})
    private PendingIntent getPendingIntent(String action) {
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getService(this, MusicContext.ACTION_CODE, new Intent(this, MusicService.class)
                .setAction(action), flags);
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private PendingIntent getLaunchPendingIntent() {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (launchIntent == null) {
            return null;
        }
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getActivity(this, MusicContext.ACTION_CODE, launchIntent, flags);
    }

    private void updateNotification() {
        if (mMusicPlayer == null) {
            return;
        }
        updateNotification(mMusicPlayer.getPlayingSong(), mMusicPlayer.currentPosition(), mMusicPlayer.isPlaying());
    }

    private void updateNotification(@Nullable Song song, long position, boolean playing) {
        if (mMusicPlayer == null || mNotificationHelper == null) {
            return;
        }
        if (song == null) {
            mNotificationHelper.clearPlaybackNotification(NOTIFICATION_ID);
            stopForeground(true);
            mForegroundStarted = false;
            return;
        }
        String title = TextUtils.isEmpty(song.getTitle()) ? song.getDisplayName() : song.getTitle();
        String artist = TextUtils.isEmpty(song.getArtist())
                ? getString(com.artillery.musicbase.R.string.base_notification_text) : song.getArtist();
        mNotificationHelper.refreshAccentColorFromSavedSkin();
        android.app.Notification notification = mNotificationHelper.buildPlaybackNotification(title, artist, playing,
                getLaunchPendingIntent(), getPendingIntent(MusicContext.ACTION_PLAY_PRE),
                getPendingIntent(MusicContext.ACTION_PLAY), getPendingIntent(MusicContext.ACTION_PLAY_NEXT),
                position, song.getDuration(), NOTIFICATION_ID);
        if (!mForegroundStarted) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification,
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
            } else {
                startForeground(NOTIFICATION_ID, notification);
            }
            mForegroundStarted = true;
        } else {
            mNotificationHelper.notifyPlaybackNotification(NOTIFICATION_ID, notification);
        }
    }

    @Override
    public void onDestroy() {
        if (mNotificationHelper != null) {
            mNotificationHelper.clearPlaybackNotification(NOTIFICATION_ID);
        }
        mForegroundStarted = false;
        mMusicPlayer.unregisterCallback(this);
        releasePlayer();
        super.onDestroy();
    }
}
