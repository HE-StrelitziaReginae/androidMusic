package com.artillery.musicbase.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;

public class BackgroundThreadUtils extends HandlerThread {
    private static Handler mHandler;

    private BackgroundThreadUtils() {
        super("Music.Background", Process.THREAD_PRIORITY_DEFAULT);
    }

    private static class Holder {
        private static final BackgroundThreadUtils instance = new BackgroundThreadUtils();
    }

    public static BackgroundThreadUtils getInstance() {
        return BackgroundThreadUtils.Holder.instance;
    }

    private Handler getHandler() {
        synchronized (BackgroundThreadUtils.class) {
            if (mHandler == null) {
                if (!getInstance().isAlive()) {
                    getInstance().start();
                }
                mHandler = new Handler(getInstance().getLooper());
            }
            return mHandler;
        }
    }

    public void post(Runnable runnable) {
        getHandler().post(runnable);
    }

    public void postAtTime(Runnable runnable, long uptime) {
        getHandler().postAtTime(runnable, uptime);
    }

    public void postDelayed(Runnable runnable, long delay) {
        getHandler().postDelayed(runnable, delay);
    }

    public void postAtFrontOfQueue(Runnable runnable) {
        getHandler().postAtFrontOfQueue(runnable);
    }

    public void removeCallbacks(Runnable runnable) {
        getHandler().removeCallbacks(runnable);
    }

    public void removeAllCallbacks() {
        getHandler().removeCallbacksAndMessages(null);
    }

    public void setThreadPriority(int priority) {
        Process.setThreadPriority(getInstance().getThreadId(), priority);
    }

    public void shutdown() {
        synchronized (BackgroundThreadUtils.class) {
            if (mHandler != null) {
                mHandler.getLooper().quit();
                mHandler = null;
            }
        }
    }

    public Looper getBackgroundLooper() {
        return getHandler().getLooper();
    }
}