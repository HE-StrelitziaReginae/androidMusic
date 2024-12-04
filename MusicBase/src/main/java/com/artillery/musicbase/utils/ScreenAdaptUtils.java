package com.artillery.musicbase.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.View;
import android.view.WindowInsets;

import java.util.List;

public class ScreenAdaptUtils {

    private static final String TAG = "ScreenAdapterUtils";

    /**
     * 获取屏幕安全区域的边界信息
     *
     * @param activity 当前 Activity
     * @return 安全区域的 Rect
     */
    public static Rect getSafeArea(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowInsets windowInsets = activity.getWindow().getDecorView().getRootWindowInsets();
            if (windowInsets != null) {
                DisplayCutout displayCutout = windowInsets.getDisplayCutout();
                if (displayCutout != null) {
                    return new Rect(
                            displayCutout.getSafeInsetLeft(),
                            displayCutout.getSafeInsetTop(),
                            displayCutout.getSafeInsetRight(),
                            displayCutout.getSafeInsetBottom()
                    );
                }
            }
        }
        Log.w(TAG, "Returning default safe area as no cutout information is available.");
        return new Rect(0, 0, 0, 0);
    }

    /**
     * 检查设备是否有凹口区域
     *
     * @param activity 当前 Activity
     * @return 是否存在凹口
     */
    public static boolean hasCutout(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowInsets windowInsets = activity.getWindow().getDecorView().getRootWindowInsets();
            if (windowInsets != null) {
                DisplayCutout displayCutout = windowInsets.getDisplayCutout();
                return displayCutout != null && !displayCutout.getBoundingRects().isEmpty();
            }
        }
        return false;
    }

    /**
     * 打印所有凹口区域，用于调试
     *
     * @param activity 当前 Activity
     */
    public static void logCutoutAreas(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowInsets windowInsets = activity.getWindow().getDecorView().getRootWindowInsets();
            if (windowInsets != null) {
                DisplayCutout displayCutout = windowInsets.getDisplayCutout();
                if (displayCutout != null) {
                    List<Rect> boundingRects = displayCutout.getBoundingRects();
                    for (Rect rect : boundingRects) {
                        Log.d(TAG, "Cutout Area: " + rect.toString());
                    }
                } else {
                    Log.d(TAG, "No cutout areas found.");
                }
            }
        } else {
            Log.d(TAG, "Device does not support cutout API.");
        }
    }

    /**
     * 监听窗口安全区域的变化
     *
     * @param view     需要监听的 View
     * @param listener 窗口变化监听器
     */
    public static void observeWindowInsets(View view, WindowInsetsListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            view.setOnApplyWindowInsetsListener((v, insets) -> {
                DisplayCutout cutout = insets.getDisplayCutout();
                if (cutout != null) {
                    Rect safeArea = new Rect(
                            cutout.getSafeInsetLeft(),
                            cutout.getSafeInsetTop(),
                            cutout.getSafeInsetRight(),
                            cutout.getSafeInsetBottom()
                    );
                    listener.onSafeAreaChanged(safeArea);
                }
                return insets;
            });
        } else {
            Log.w(TAG, "Window insets listener is not supported on this device.");
        }
    }

    /**
     * 检查是否隐藏了凹口区域
     *
     * @param activity 当前 Activity
     * @return 是否隐藏凹口
     */
    public static boolean isCutoutHidden(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowInsets insets = activity.getWindow().getDecorView().getRootWindowInsets();
            if (insets != null) {
                DisplayCutout cutout = insets.getDisplayCutout();
                return cutout == null || cutout.getBoundingRects().isEmpty();
            }
        }
        return true; // 默认假定没有凹口
    }

    /**
     * 检查是否处于多窗口模式
     *
     * @param activity 当前 Activity
     * @return 是否为多窗口模式
     */
    public static boolean isInMultiWindowMode(Activity activity) {
        return activity.isInMultiWindowMode();
    }

    /**
     * 窗口安全区域监听器接口
     */
    public interface WindowInsetsListener {
        void onSafeAreaChanged(Rect safeArea);
    }
}
