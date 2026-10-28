package com.os.sys.musicskin;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.Gravity;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

public enum MusicSkin {
    LIGHT("light", true, R.color.snow, R.color.white, R.color.white, R.color.darkslategray,
            R.color.black, R.color.gray, R.color.lightgray, R.color.white, R.color.gray, R.color.blue),
    DARK("dark", false, R.color.black, R.color.darkslategray, R.color.darkslategrey, R.color.navy,
            R.color.white, R.color.lightgray, R.color.lightblue, R.color.white, R.color.lightgray, R.color.deepskyblue),
    OCEAN("ocean", true, R.color.aliceblue, R.color.white, R.color.azure, R.color.teal,
            R.color.darkslategray, R.color.cadetblue, R.color.lightcyan, R.color.white, R.color.teal, R.color.darkcyan),
    SAKURA("sakura", true, R.color.music_sakura_background, R.color.music_sakura_surface,
            R.color.music_sakura_drawer, R.color.music_sakura_header, R.color.music_sakura_primary,
            R.color.music_sakura_secondary, R.color.music_sakura_header_secondary,
            R.color.music_sakura_header_primary, R.color.music_sakura_icon, R.color.music_sakura_accent);

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
    private final int accentColor;

    MusicSkin(String value, boolean lightSystemBars,
              @ColorRes int backgroundColor, @ColorRes int surfaceColor, @ColorRes int drawerColor,
              @ColorRes int headerColor, @ColorRes int primaryTextColor, @ColorRes int secondaryTextColor,
              @ColorRes int headerSecondaryTextColor, @ColorRes int headerPrimaryTextColor,
              @ColorRes int iconColor, @ColorRes int accentColor) {
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
        this.accentColor = accentColor;
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

    public boolean isCharacterSkin() {
        return this == SAKURA;
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

    public int accent(Context context) {
        return resolve(context, accentColor);
    }

    public ColorStateList tabTextColors(Context context) {
        return new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{accent(context), secondaryText(context)});
    }

    public ColorStateList tabIconTint(Context context) {
        return tabTextColors(context);
    }

    public StateListDrawable tabBackground(Context context) {
        StateListDrawable drawable = new StateListDrawable();
        GradientDrawable selected = roundedDrawable(withAlpha(accent(context), 34), 18, context);
        selected.setStroke(dp(context, 1), withAlpha(accent(context), 95));
        drawable.addState(new int[]{android.R.attr.state_checked}, selected);
        drawable.addState(new int[]{}, roundedDrawable(Color.TRANSPARENT, 18, context));
        return drawable;
    }

    public GradientDrawable tabContainerBackground(Context context) {
        GradientDrawable drawable = roundedDrawable(surface(context), 18, context);
        drawable.setStroke(dp(context, 1), withAlpha(secondaryText(context), 35));
        return drawable;
    }

    public GradientDrawable drawerHeaderBackground(Context context) {
        if (!isCharacterSkin()) {
            return roundedDrawable(header(context), 0, context);
        }
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(62, 43, 94), header(context), accent(context)});
        drawable.setCornerRadius(0);
        return drawable;
    }

    public GradientDrawable featureBackground(Context context) {
        if (!isCharacterSkin()) {
            return cardBackground(context, false);
        }
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.rgb(75, 54, 116), header(context), accent(context)});
        drawable.setCornerRadius(dp(context, 20));
        drawable.setStroke(dp(context, 1), withAlpha(headerPrimaryText(context), 65));
        return drawable;
    }

    public GradientDrawable outlinedCardBackground(Context context) {
        GradientDrawable drawable = roundedDrawable(surface(context), 16, context);
        drawable.setStroke(dp(context, 1), withAlpha(
                isCharacterSkin() ? accent(context) : secondaryText(context), 65));
        return drawable;
    }

    public LayerDrawable seekBarDrawable(Context context) {
        Drawable background = roundedDrawable(withAlpha(secondaryText(context), 45), 8, context);
        Drawable secondary = new ClipDrawable(
                roundedDrawable(withAlpha(accent(context), 90), 8, context),
                Gravity.LEFT, ClipDrawable.HORIZONTAL);
        Drawable progress = new ClipDrawable(
                roundedDrawable(accent(context), 8, context),
                Gravity.LEFT, ClipDrawable.HORIZONTAL);
        LayerDrawable drawable = new LayerDrawable(new Drawable[]{background, secondary, progress});
        drawable.setId(0, android.R.id.background);
        drawable.setId(1, android.R.id.secondaryProgress);
        drawable.setId(2, android.R.id.progress);
        return drawable;
    }

    public GradientDrawable cardBackground(Context context, boolean selected) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(selected ? header(context) : surface(context));
        drawable.setCornerRadius(12 * context.getResources().getDisplayMetrics().density);
        if (isCharacterSkin()) {
            drawable.setStroke(dp(context, 1), withAlpha(accent(context), selected ? 150 : 70));
        }
        return drawable;
    }

    private int resolve(Context context, @ColorRes int colorResId) {
        return ContextCompat.getColor(context, colorResId);
    }

    private GradientDrawable roundedDrawable(int color, int radiusDp, Context context) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radiusDp * context.getResources().getDisplayMetrics().density);
        return drawable;
    }

    private int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
