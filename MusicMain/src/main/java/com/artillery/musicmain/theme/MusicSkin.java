package com.artillery.musicmain.theme;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import com.artillery.musicbase.R;

public enum MusicSkin {
    LIGHT("light", true, R.color.snow, R.color.white, R.color.white, R.color.darkslategray,
            R.color.black, R.color.gray, R.color.lightgray, R.color.white, R.color.gray),
    DARK("dark", false, R.color.black, R.color.darkslategray, R.color.darkslategrey, R.color.navy,
            R.color.white, R.color.lightgray, R.color.lightblue, R.color.white, R.color.lightgray),
    OCEAN("ocean", true, R.color.aliceblue, R.color.white, R.color.azure, R.color.teal,
            R.color.darkslategray, R.color.cadetblue, R.color.lightcyan, R.color.white, R.color.teal);

    private static final String PREFERENCES = "music_skin_preferences";
    private static final String KEY_SKIN = "skin";

    private final String value;
    private final boolean lightSystemBars;
    private final int backgroundColor;
    private final int surfaceColor;
    private final int drawerColor;
    private final int headerColor;
    private final int primaryTextColor;
    private final int secondaryTextColor;
    private final int headerSecondaryTextColor;
    private final int headerPrimaryTextColor;
    private final int iconColor;

    MusicSkin(String value, boolean lightSystemBars,
              @ColorRes int backgroundColor, @ColorRes int surfaceColor, @ColorRes int drawerColor,
              @ColorRes int headerColor, @ColorRes int primaryTextColor, @ColorRes int secondaryTextColor,
              @ColorRes int headerSecondaryTextColor, @ColorRes int headerPrimaryTextColor,
              @ColorRes int iconColor) {
        this.value = value;
        this.lightSystemBars = lightSystemBars;
        this.backgroundColor = backgroundColor;
        this.surfaceColor = surfaceColor;
        this.drawerColor = drawerColor;
        this.headerColor = headerColor;
        this.primaryTextColor = primaryTextColor;
        this.secondaryTextColor = secondaryTextColor;
        this.headerSecondaryTextColor = headerSecondaryTextColor;
        this.headerPrimaryTextColor = headerPrimaryTextColor;
        this.iconColor = iconColor;
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

    public int background(Context context) {
        return resolve(context, backgroundColor);
    }

    public int surface(Context context) {
        return resolve(context, surfaceColor);
    }

    public int drawer(Context context) {
        return resolve(context, drawerColor);
    }

    public int header(Context context) {
        return resolve(context, headerColor);
    }

    public int primaryText(Context context) {
        return resolve(context, primaryTextColor);
    }

    public int secondaryText(Context context) {
        return resolve(context, secondaryTextColor);
    }

    public int headerSecondaryText(Context context) {
        return resolve(context, headerSecondaryTextColor);
    }

    public int headerPrimaryText(Context context) {
        return resolve(context, headerPrimaryTextColor);
    }

    public int icon(Context context) {
        return resolve(context, iconColor);
    }

    public GradientDrawable cardBackground(Context context, boolean selected) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(selected ? header(context) : surface(context));
        drawable.setCornerRadius(12 * context.getResources().getDisplayMetrics().density);
        return drawable;
    }

    private int resolve(Context context, @ColorRes int colorResId) {
        return ContextCompat.getColor(context, colorResId);
    }
}
