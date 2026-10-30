package com.artillery.musicbase.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.core.app.NotificationCompat;

import com.artillery.musicbase.R;
import com.os.sys.musicskin.MusicSkin;

public class NotificationHelper {
    private static final String CHANNEL_ID = "music_playback";
    private static volatile NotificationHelper instance;
    private final Context mContext;
    private final NotificationManager mNotificationManager;
    private final MediaSessionCompat mMediaSession;
    private int mAccentColor;
    private int mLastNotificationId = -1;
    private String mLastTitle;
    private String mLastArtist;
    private long mLastDuration;
    private long mLastPosition;
    private Bitmap mLastArtwork;
    private boolean mLastPlaying;
    private PendingIntent mLastContentIntent;
    private PendingIntent mLastPreviousIntent;
    private PendingIntent mLastPlayIntent;
    private PendingIntent mLastNextIntent;
    private SeekListener mSeekListener;

    public interface SeekListener {
        void seekTo(long position);
    }

    private NotificationHelper(Context context) {
        mContext = context.getApplicationContext();
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mMediaSession = new MediaSessionCompat(mContext, mContext.getString(R.string.base_notification_title));
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                sendPendingIntent(mLastPlayIntent);
            }

            @Override
            public void onPause() {
                sendPendingIntent(mLastPlayIntent);
            }

            @Override
            public void onSkipToPrevious() {
                sendPendingIntent(mLastPreviousIntent);
            }

            @Override
            public void onSkipToNext() {
                sendPendingIntent(mLastNextIntent);
            }

            @Override
            public void onSeekTo(long position) {
                if (mSeekListener != null) {
                    mSeekListener.seekTo(position);
                }
            }
        });
        createNotificationChannel();
        refreshAccentColorFromSavedSkin();
    }

    public static NotificationHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (NotificationHelper.class) {
                if (instance == null) {
                    instance = new NotificationHelper(context);
                }
            }
        }
        return instance;
    }

    public void setAccentColor(int accentColor) {
        if (mAccentColor == accentColor) {
            return;
        }
        mAccentColor = accentColor;
        refreshNotification();
    }

    public void refreshAccentColorFromSavedSkin() {
        setAccentColor(MusicSkin.load(mContext).accent(mContext));
    }

    public void setSeekListener(SeekListener seekListener) {
        mSeekListener = seekListener;
    }

    public Notification buildPlaybackNotification(String title, String artist, boolean playing,
                                                   PendingIntent contentIntent,
                                                   PendingIntent previousIntent,
                                                   PendingIntent playIntent,
                                                   PendingIntent nextIntent,
                                                   long position,
                                                   long duration,
                                                   Bitmap artwork,
                                                   int notificationId) {
        mLastTitle = title;
        mLastArtist = artist;
        mLastPlaying = playing;
        mLastContentIntent = contentIntent;
        mLastPreviousIntent = previousIntent;
        mLastPlayIntent = playIntent;
        mLastNextIntent = nextIntent;
        mLastPosition = Math.max(0, position);
        mLastDuration = Math.max(0, duration);
        mLastArtwork = artwork;
        mLastNotificationId = notificationId;
        mMediaSession.setActive(true);
        if (contentIntent != null) {
            mMediaSession.setSessionActivity(contentIntent);
        }
        updateMediaSession();
        return createPlaybackNotification();
    }

    public void clearPlaybackNotification(int notificationId) {
        mNotificationManager.cancel(notificationId);
        if (mLastNotificationId == notificationId) {
            mLastNotificationId = -1;
        }
        mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
                .setActions(0)
                .build());
        mMediaSession.setMetadata(null);
        mMediaSession.setActive(false);
        mLastTitle = null;
        mLastArtist = null;
        mLastArtwork = null;
    }

    public void notifyPlaybackNotification(int notificationId, Notification notification) {
        mNotificationManager.notify(notificationId, notification);
    }

    private Notification createPlaybackNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setColor(mAccentColor)
                .setContentTitle(mLastTitle)
                .setContentText(mLastArtist)
                .setSubText(mContext.getString(R.string.base_notification_title))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mMediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .setOngoing(true)
                .setAutoCancel(false)
                .setLocalOnly(true)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        if (mLastArtwork != null && !mLastArtwork.isRecycled()) {
            builder.setLargeIcon(mLastArtwork);
        }
        if (mLastDuration > 0) {
            int progressMax = (int) Math.min(Integer.MAX_VALUE, mLastDuration);
            int progress = (int) Math.min(progressMax, mLastPosition);
            builder.setProgress(progressMax, progress, false);
        }
        if (mLastContentIntent != null) {
            builder.setContentIntent(mLastContentIntent);
        }
        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_previous,
                        mContext.getString(R.string.base_notification_previous), mLastPreviousIntent))
                .addAction(new NotificationCompat.Action(mLastPlaying
                                ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play,
                        mContext.getString(mLastPlaying
                                ? R.string.base_notification_pause : R.string.base_notification_play), mLastPlayIntent))
                .addAction(new NotificationCompat.Action(android.R.drawable.ic_media_next,
                        mContext.getString(R.string.base_notification_next), mLastNextIntent));
        return builder.build();
    }

    private void updateMediaSession() {
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder()
                .putText(MediaMetadataCompat.METADATA_KEY_TITLE, mLastTitle)
                .putText(MediaMetadataCompat.METADATA_KEY_ARTIST, mLastArtist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mLastDuration);
        if (mLastArtwork != null && !mLastArtwork.isRecycled()) {
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, mLastArtwork);
        }
        mMediaSession.setMetadata(metadataBuilder.build());

        long actions = PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                | PlaybackStateCompat.ACTION_SEEK_TO
                | PlaybackStateCompat.ACTION_PLAY_PAUSE
                | (mLastPlaying ? PlaybackStateCompat.ACTION_PAUSE : PlaybackStateCompat.ACTION_PLAY);
        int state = mLastPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
        mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setActions(actions)
                .setState(state, mLastPosition, mLastPlaying ? 1f : 0f, SystemClock.elapsedRealtime())
                .build());
    }

    private void sendPendingIntent(PendingIntent pendingIntent) {
        if (pendingIntent == null) {
            return;
        }
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException ignored) {
        }
    }

    private void refreshNotification() {
        if (mLastNotificationId != -1 && mLastTitle != null) {
            mNotificationManager.notify(mLastNotificationId, createPlaybackNotification());
        }
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    mContext.getString(R.string.base_notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW);
            channel.setSound(null, null);
            channel.setShowBadge(true);
            mNotificationManager.createNotificationChannel(channel);
        }
    }
}
