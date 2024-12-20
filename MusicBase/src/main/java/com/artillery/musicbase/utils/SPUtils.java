package com.artillery.musicbase.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SPUtils {
    // 单例实例
    private static volatile SPUtils instance;

    // SharedPreferences 对象
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // 默认的SP文件名
    private static final String SP_NAME = "music_preferences";

    // 私有构造方法，防止外部直接实例化
    private SPUtils(Context context) {
        sharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // 获取单例实例
    public static SPUtils getInstance(Context context) {
        if (instance == null) {
            synchronized (SPUtils.class) {
                if (instance == null) {
                    instance = new SPUtils(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    // 保存数据
    public void put(String key, Object value) {
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        }
        editor.apply();
    }

    // 获取String类型数据
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    // 获取int类型数据
    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    // 获取boolean类型数据
    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    // 获取long类型数据
    public long getLong(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    // 获取float类型数据
    public float getFloat(String key, float defaultValue) {
        return sharedPreferences.getFloat(key, defaultValue);
    }

    // 删除指定key的数据
    public void remove(String key) {
        editor.remove(key);
        editor.apply();
    }

    // 清空所有数据
    public void clear() {
        editor.clear();
        editor.apply();
    }

    // 检查某个key是否存在
    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }
}