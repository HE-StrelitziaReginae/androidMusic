package com.artillery.musicbase.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class NotificationHelper {
    private static final String CHANNEL_ID = "custom_notification_channel";
    private static final String CHANNEL_NAME = "custom_notification_channel_name";
    private static volatile NotificationHelper instance; // 添加 volatile 保证可见性
    private final NotificationManager mNotificationManager;

    private NotificationHelper(Context context) {
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    public static NotificationHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (NotificationHelper.class) {
                if (instance == null) {
                    instance = new NotificationHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setSound(null, null);
            channel.setVibrationPattern(new long[]{0});
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification.Builder createNotificationBuilder(Context context) {
        return new Notification.Builder(context, CHANNEL_ID);
    }

    public void showCustomNotification(Context context) {
        // 通知逻辑与前文相同
    }
}