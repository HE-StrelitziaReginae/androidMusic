package com.artillery.musicmain.theme;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

public enum MusicSkin {
    LIGHT("light", true),
    DARK("dark", false),
    OCEAN("ocean", true);

    private static final String PREFERENCES = "music_skin_preferences";
    private static final String KEY_SKIN = "skin";

    private final String value;
    private final boolean lightSystemBars;

    MusicSkin(String value, boolean lightSystemBars) {
        this.value = value;
        this.lightSystemBars = lightSystemBars;
    }

    public static MusicSkin load(Context context) {
        String value = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                .getString(KEY_SKIN, LIGHT.value);
        for (MusicSkin skin : values()) {
            if (skin.value.equals(value)) {
                return skin;
            }
        }
        return LIGHT;
    }

    public void save(Context context) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_SKIN, value)
                .apply();
    }

    public boolean usesLightSystemBars() {
        return lightSystemBars;
    }

    public int background() {
        switch (this) {
            case DARK:
                return color("#12161D");
            case OCEAN:
                return color("#EAF5F7");
            case LIGHT:
            default:
                return color("#F7F8FA");
        }
    }

    public int surface() {
        switch (this) {
            case DARK:
                return color("#1B222C");
            case OCEAN:
            case LIGHT:
            default:
                return Color.WHITE;
        }
    }

    public int drawer() {
        switch (this) {
            case DARK:
                return color("#171D26");
            case OCEAN:
                return color("#F5FBFC");
            case LIGHT:
            default:
                return Color.WHITE;
        }
    }

    public int header() {
        switch (this) {
            case DARK:
                return color("#0D1117");
            case OCEAN:
                return color("#176778");
            case LIGHT:
            default:
                return color("#202536");
        }
    }

    public int primaryText() {
        switch (this) {
            case DARK:
                return color("#F2F5F8");
            case OCEAN:
                return color("#12313D");
            case LIGHT:
            default:
                return color("#202124");
        }
    }

    public int secondaryText() {
        switch (this) {
            case DARK:
                return color("#AEB8C5");
            case OCEAN:
                return color("#51717A");
            case LIGHT:
            default:
                return color("#74757C");
        }
    }

    public int headerSecondaryText() {
        switch (this) {
            case DARK:
                return color("#B9C4D2");
            case OCEAN:
                return color("#D8F0F3");
            case LIGHT:
            default:
                return color("#D7D9E7");
        }
    }

    public int headerPrimaryText() {
        return Color.WHITE;
    }

    public int icon() {
        switch (this) {
            case DARK:
                return color("#C5D0DD");
            case OCEAN:
                return color("#2B6D7D");
            case LIGHT:
            default:
                return color("#5F6368");
        }
    }

    private int color(String value) {
        return Color.parseColor(value);
    }
}
