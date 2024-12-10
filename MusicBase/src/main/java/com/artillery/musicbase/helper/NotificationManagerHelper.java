package com.artillery.musicbase.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;
public class NotificationManagerHelper {

    public static final String DEFAULT_CHANNEL_ID = "default_channel";
    private static final String DEFAULT_CHANNEL_NAME = "Default Notifications";
    private final Context mContext;
    private final NotificationManager mNotificationManager;

    // 单例实例
    private static NotificationManagerHelper instance;

    // 私有构造方法
    private NotificationManagerHelper(Context context) {
        mContext = context.getApplicationContext();
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // 创建默认频道（仅适用于Android O及以上版本）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    DEFAULT_CHANNEL_ID,
                    DEFAULT_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    // 获取单例实例
    public static synchronized NotificationManagerHelper getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationManagerHelper(context);
        }
        return instance;
    }

    /**
     * 创建通知频道（仅适用于Android O及以上版本）。
     */
    public void createNotificationChannel(String channelId, String channelName, int importance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription("Notification channel for " + channelName);
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * 显示简单通知。
     */
    public void showNotification(int notificationId, String title, String content, Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(mContext, DEFAULT_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(android.R.drawable.ic_notification_overlay)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        mNotificationManager.notify(notificationId, notification);
    }

    /**
     * 使用自定义频道显示通知。
     */
    public void showNotificationWithChannel(int notificationId, String channelId, String title, String content, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mNotificationManager.getNotificationChannel(channelId) == null) {
            createNotificationChannel(channelId, channelId + " Channel", NotificationManager.IMPORTANCE_DEFAULT);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(mContext, channelId)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(android.R.drawable.ic_notification_overlay)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        mNotificationManager.notify(notificationId, notification);
    }

    /**
     * 取消通知。
     */
    public void cancelNotification(int notificationId) {
        mNotificationManager.cancel(notificationId);
    }

    /**
     * 取消所有通知。
     */
    public void cancelAllNotifications() {
        mNotificationManager.cancelAll();
    }
}