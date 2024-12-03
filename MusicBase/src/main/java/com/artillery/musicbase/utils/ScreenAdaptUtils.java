package com.artillery.musicbase.utils;

import android.app.Activity;
import android.view.View;

public class ScreenAdaptUtils {

    /**
     * 启用沉浸式模式
     *
     * @param activity 当前活动
     */
    public static void enableImmersiveMode(Activity activity) {
        // 获取当前活动的根视图
        View decorView = activity.getWindow().getDecorView();

        // 设置沉浸式模式的标志
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        // 应用沉浸式模式
        decorView.setSystemUiVisibility(uiOptions);

        // 监听系统UI显示状态，保持沉浸式模式
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    // 如果系统UI显示，重新隐藏
                    decorView.setSystemUiVisibility(uiOptions);
                }
            }
        });
    }

    /**
     * 退出沉浸式模式，恢复系统UI显示
     *
     * @param activity 当前活动
     */
    public static void disableImmersiveMode(Activity activity) {
        // 获取当前活动的根视图
        View decorView = activity.getWindow().getDecorView();

        // 恢复正常的系统UI显示
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }
}
