package com.artillery.musicmain.ui;

import android.content.Context;
import android.animation.ObjectAnimator;
import android.graphics.drawable.GradientDrawable;
import android.text.format.DateUtils;
import android.view.animation.LinearInterpolator;
import android.view.View;
import android.widget.SeekBar;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.artillery.musicbase.base.AppManager;
import com.artillery.musicbase.base.BaseDialog;
import com.artillery.musicbase.utils.BackgroundThreadUtils;
import com.artillery.musicbase.utils.KLogUtils;
import com.artillery.musicbase.utils.SystemBarUtils;
import com.artillery.musicmain.R;
import com.artillery.musicmain.data.MusicDaraListener;
import com.artillery.musicmain.databinding.MusicMianDialogBinding;
import com.artillery.musicmain.observer.MusicObserver;
import com.os.sys.musicskin.MusicSkin;
import com.artillery.musicmain.utils.TimeUtils;
import com.artillery.musicservice.data.Song;

import static com.artillery.musicbase.R.dimen.*;

public class MusicMainDialog extends BaseDialog<MusicMianDialogBinding> implements SeekBar.OnSeekBarChangeListener, MusicObserver {
    private MusicMainFragment mFragment;
    private Song mSong;
    private boolean mLyricsVisible;
    private boolean mPlaying;
    private ObjectAnimator mAlbumRotationAnimator;
    public final Runnable mProgressCallback = new Runnable() {
        @Override
        public void run() {
            if (!isShowing() || mBinding == null || mFragment == null || mFragment.mMusicListener == null) {
                return;
            }
            mBinding.musicSeekbar.post(MusicMainDialog.this::updateProgressOnMainThread);
        }
    };

    public MusicMainDialog(Context context) {
        super(context, R.style.CustomFullDialog);
    }

    private static MusicMainDialog sInstance;

    public static synchronized MusicMainDialog getInstance() {
        Context context = AppManager.getAppManager().currentActivity();
        if (context == null) {
            return null;
        }
        if (sInstance == null || sInstance.getContext() != context) {
            if (sInstance != null && sInstance.isShowing()) {
                sInstance.dismiss();
            }
            sInstance = new MusicMainDialog(context);
        }
        return sInstance;
    }

    public static void showCurrent() {
        MusicMainDialog dialog = getInstance();
        if (dialog != null) {
            dialog.bindFragment((MusicMainFragment) AppManager.getAppManager().currentFragment());
            if (!dialog.isShowing()) {
                dialog.show();
            }
            dialog.syncCurrentPlaybackState();
        }
    }

    public static MusicMainDialog getExistingInstance() {
        return sInstance;
    }

    public static void dismissCurrent() {
        if (sInstance != null && sInstance.isShowing()) {
            sInstance.dismiss();
        }
    }

    public static void applySkinCurrent(MusicSkin skin) {
        if (sInstance != null) {
            sInstance.applySkin(skin);
        }
    }

    @Override
    public int initContentView() {
        return R.layout.music_mian_dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SystemBarUtils.configureImmersive(getWindow());
        super.onCreate(savedInstanceState);
        SystemBarUtils.setLightSystemBars(getWindow(), MusicSkin.load(getContext()).usesLightSystemBars());
    }

    @Override
    public void initData() {
        KLogUtils.i("initData: ");
        bindFragment((MusicMainFragment) AppManager.getAppManager().currentFragment());
        applySystemBarInsets();
        applySkin(MusicSkin.load(getContext()));
    }

    public void bindFragment(@Nullable MusicMainFragment fragment) {
        mFragment = fragment;
        MusicDaraListener.getInstance().setMusicObserver(fragment == null ? null : this);
    }

    public void syncCurrentPlaybackState() {
        if (mBinding == null || mFragment == null || mFragment.mMusicListener == null) {
            return;
        }
        update(mFragment.mMusicListener.getPlayingSong());
        updateUi(mFragment.mMusicListener.isPlaying());
        updatePosition(mFragment.mMusicListener.currentPosition());
    }

    public void applySkin(MusicSkin skin) {
        if (mBinding == null) {
            return;
        }
        int background = skin.background(getContext());
        int primaryText = skin.primaryText(getContext());
        int secondaryText = skin.secondaryText(getContext());
        mBinding.musicDialog.setBackgroundColor(background);
        mBinding.musicNamePlay.setTextColor(primaryText);
        mBinding.musicArtistPlay.setTextColor(secondaryText);
        mBinding.musicTimeStart.setTextColor(secondaryText);
        mBinding.musicTimeEnd.setTextColor(secondaryText);
        mBinding.musicPreBtn.setColorFilter(skin.icon(getContext()));
        mBinding.musicPlayBtn.setColorFilter(skin.icon(getContext()));
        mBinding.musicNextBtn.setColorFilter(skin.icon(getContext()));
        mBinding.musicDialogClose.setColorFilter(skin.icon(getContext()));
        mBinding.musicSeekbar.setProgressDrawable(skin.seekBarDrawable(getContext()));
        boolean characterSkin = skin.isCharacterSkin();
        mBinding.musicCharacterBackground.setVisibility(characterSkin ? View.VISIBLE : View.GONE);
        mBinding.musicCharacterScrim.setVisibility(characterSkin ? View.VISIBLE : View.GONE);
        mBinding.musicCharacterBadge.setVisibility(characterSkin ? View.VISIBLE : View.GONE);
        mBinding.musicCharacterName.setVisibility(characterSkin ? View.VISIBLE : View.GONE);
        if (characterSkin) {
            mBinding.musicCharacterBackground.setImageResource(skin.keyArt());
            mBinding.musicCharacterBadge.setText(skin.characterBadge());
            mBinding.musicCharacterName.setText(skin.characterName());
        }
        mBinding.musicCharacterBadge.setTextColor(skin.accent(getContext()));
        mBinding.musicCharacterName.setTextColor(primaryText);
        mBinding.musicAlbumImage.setImageResource(characterSkin
                ? skin.avatar() : R.drawable.ellipse);
        mBinding.musicAlbumGrooves.setColorFilter(withAlpha(skin.headerPrimaryText(getContext()), 70));
        mBinding.musicNowPlayingBadge.setBackground(skin.cardBackground(getContext(), false));
        mBinding.musicNowPlayingIndicator.setBackground(roundBackground(skin.accent(getContext()), 4));
        mBinding.musicNowPlayingText.setTextColor(skin.secondaryText(getContext()));
        mBinding.musicDialogRoot.setBackground(skin.cardBackground(getContext(), false));
        mBinding.musicActionLiner.setBackground(skin.cardBackground(getContext(), false));
        int accent = skin.accent(getContext());
        int lyricsPrimary = skin.isCharacterSkin() ? skin.headerPrimaryText(getContext()) : primaryText;
        int lyricsSecondary = skin.isCharacterSkin() ? skin.headerSecondaryText(getContext()) : secondaryText;
        mBinding.musicLyricsPage.setBackground(roundBackground(withAlpha(background, 205), dp_0));
        mBinding.musicLyricsTitle.setTextColor(accent);
        mBinding.musicLyricsSong.setTextColor(lyricsPrimary);
        mBinding.musicLyricsEmpty.setTextColor(lyricsSecondary);
        mBinding.musicLyricsHint.setTextColor(lyricsSecondary);
        mBinding.musicAlbumCard.setBackground(roundBackgroundPx(
                withAlpha(accent, 60),
                getContext().getResources().getDimension(dp_116)));
        mBinding.musicDialogRoot.setBackground(skin.cardBackground(getContext(), false));
        mBinding.musicActionLiner.setBackground(skin.cardBackground(getContext(), false));
        updateCoverPresentation();
        if (getWindow() != null) {
            SystemBarUtils.setLightSystemBars(getWindow(), skin.usesLightSystemBars());
        }
    }

    private void applySystemBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.musicContent, (view, insets) -> {
            int topInset = insets.getSystemWindowInsetTop();
            int bottomInset = insets.getSystemWindowInsetBottom();
            mBinding.musicContent.setPadding(
                    mBinding.musicDialog.getPaddingLeft(),
                    topInset,
                    mBinding.musicDialog.getPaddingRight(),
                    bottomInset);
            return insets;
        });
        ViewCompat.requestApplyInsets(mBinding.musicContent);
    }

    @Override
    public void update(Song song) {
        if (song == null) {
            mSong = null;
            mBinding.musicLyricsSong.setText(R.string.music_not_play);
            mBinding.musicNamePlay.setText(R.string.music_not_play);
            mBinding.musicArtistPlay.setText(R.string.music_unknown);
            applyAlbumSkin();
            mBinding.musicTimeStart.setText(R.string.music_time);
            mBinding.musicTimeEnd.setText(R.string.music_time);
            mBinding.musicSeekbar.setMax(0);
            mBinding.musicSeekbar.setProgress(0);
            return;
        }
        updateMainUi(song);
        activateMarquee(true);
    }

    @Override
    public void updateUi(boolean isPlaying) {
        KLogUtils.e("updatePlayToggle: " + isPlaying);
        mPlaying = isPlaying;
        mBinding.musicPlayBtn.setImageDrawable(isPlaying ? ContextCompat.getDrawable(getContext(), R.drawable.pause)
                : ContextCompat.getDrawable(getContext(), R.drawable.play));
        mBinding.musicPlayBtn.setColorFilter(MusicSkin.load(getContext()).icon(getContext()));
        updateCoverPresentation();
    }

    @Override
    public void updatePosition(long position) {
        if (mBinding == null || mSong == null) {
            return;
        }
        updateProgress(position, mSong.getDuration());
    }

    @Override
    public void dismiss() {
        BackgroundThreadUtils.getInstance().removeCallbacks(mProgressCallback);
        stopAlbumRotation(true);
        mLyricsVisible = false;
        mPlaying = false;
        MusicDaraListener.getInstance().clearMusicObserver();
        mFragment = null;
        super.dismiss();
        activateMarquee(false);
    }

    private void updateMainUi(Song song) {
        KLogUtils.e("updateMainUi: ");
        mSong = song;
        mBinding.musicSeekbar.setProgress(0);
        mBinding.musicSeekbar.setMax((int) (getCurrentSongDuration() / 1000));
        mBinding.musicNamePlay.setText(song.getTitle());
        mBinding.musicArtistPlay.setText(song.getArtist());
        mBinding.musicLyricsSong.setText(song.getTitle());
        applyAlbumSkin();
        mBinding.musicTimeEnd.setText(TimeUtils.formatDuration(song.getDuration()));
    }

    private void applyAlbumSkin() {
        MusicSkin skin = MusicSkin.load(getContext());
        mBinding.musicAlbumImage.setImageResource(skin.isCharacterSkin()
                ? skin.avatar() : R.drawable.ellipse);
        if (mSong == null) {
            mBinding.musicLyricsSong.setText(R.string.music_not_play);
        } else {
            mBinding.musicLyricsSong.setText(mSong.getTitle());
        }
        updateCoverPresentation();
    }

    private void toggleLyrics() {
        mLyricsVisible = !mLyricsVisible;
        updateCoverPresentation();
    }

    private void updateCoverPresentation() {
        if (mBinding == null) {
            return;
        }
        mBinding.musicLyricsPage.setVisibility(mLyricsVisible ? View.VISIBLE : View.GONE);
        mBinding.musicAlbumImage.setVisibility(mLyricsVisible ? View.INVISIBLE : View.VISIBLE);
        mBinding.musicAlbumStage.setBackground(roundBackgroundPx(
                withAlpha(MusicSkin.load(getContext()).surface(getContext()), 230),
                getContext().getResources().getDimension(dp_116)));
        mBinding.musicAlbumCard.setContentDescription(getContext().getString(R.string.music_album_toggle));
        if (mPlaying && !mLyricsVisible) {
            mBinding.musicNowPlayingIndicator.setAlpha(1f);
            startAlbumRotation();
        } else {
            mBinding.musicNowPlayingIndicator.setAlpha(0.42f);
            stopAlbumRotation(false);
        }
    }

    private void startAlbumRotation() {
        if (mAlbumRotationAnimator == null) {
            mAlbumRotationAnimator = ObjectAnimator.ofFloat(mBinding.musicAlbumImage, View.ROTATION, 0f, 360f);
            mAlbumRotationAnimator.setDuration(getContext().getResources()
                    .getInteger(R.integer.music_album_rotation_duration));
            mAlbumRotationAnimator.setInterpolator(new LinearInterpolator());
            mAlbumRotationAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            mAlbumRotationAnimator.setRepeatMode(ObjectAnimator.RESTART);
            mAlbumRotationAnimator.start();
            return;
        }
        if (mAlbumRotationAnimator.isPaused()) {
            mAlbumRotationAnimator.resume();
        }
    }

    private void stopAlbumRotation(boolean release) {
        if (mAlbumRotationAnimator == null) {
            return;
        }
        if (release) {
            mAlbumRotationAnimator.cancel();
            mAlbumRotationAnimator = null;
        } else if (mAlbumRotationAnimator.isStarted() && !mAlbumRotationAnimator.isPaused()) {
            mAlbumRotationAnimator.pause();
        }
    }

    private GradientDrawable roundBackground(int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private GradientDrawable roundBackgroundPx(int color, float radiusPx) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radiusPx);
        return drawable;
    }

    private int withAlpha(int color, int alpha) {
        return android.graphics.Color.argb(alpha, android.graphics.Color.red(color),
                android.graphics.Color.green(color), android.graphics.Color.blue(color));
    }

    private int dp(int value) {
        return Math.round(value * getContext().getResources().getDisplayMetrics().density);
    }

    private void activateMarquee(boolean activate) {
        mBinding.musicNamePlay.setFocusable(activate);
        mBinding.musicNamePlay.setFocusableInTouchMode(activate);
        mBinding.musicNamePlay.setSelected(activate);
    }

    private int getDuration(int progress) {
        int max = mBinding.musicSeekbar.getMax();
        return max == 0 ? 0 : (int) (getCurrentSongDuration() * ((float) progress / max));
    }

    private void updateProgressTextWithProgress(int progress) {
        mBinding.musicTimeStart.setText(TimeUtils.formatDuration(getDuration(progress)));
    }

    private void updateProgressTextWithDuration(long duration) {
        mBinding.musicTimeStart.setText(TimeUtils.formatDuration(duration));
    }

    private void updateProgress(long position, long duration) {
        if (!isShowing() || mBinding == null) {
            return;
        }
        int max = Math.max(1, (int) (duration / 1000));
        int progress = Math.max(0, Math.min(max, (int) (position / 1000)));
        mBinding.musicSeekbar.setMax(max);
        mBinding.musicSeekbar.setProgress(progress);
        updateProgressTextWithDuration(position);
        if (mFragment != null && mFragment.mMusicListener != null && mFragment.mMusicListener.isPlaying()) {
            BackgroundThreadUtils.getInstance().postDelayed(mProgressCallback, DateUtils.SECOND_IN_MILLIS);
        }
    }

    private void updateProgressOnMainThread() {
        if (!isShowing() || mBinding == null || mFragment == null || mFragment.mMusicListener == null) {
            return;
        }
        long duration = getCurrentSongDuration();
        if (duration <= 0) {
            return;
        }
        long position = mFragment.mMusicListener.currentPosition();
        updateProgress(position, duration);
    }

    private void seekTo(int duration) {
        if (mFragment != null && mFragment.mMusicListener != null) {
            mFragment.mMusicListener.seekTo(duration);
        }
    }

    private long getCurrentSongDuration() {
        Song currentSong = mSong;
        int duration = 0;
        if (currentSong != null) {
            duration = currentSong.getDuration();
        }
        return duration;
    }

    @Override
    public void initViewObservable() {
        KLogUtils.i("initViewObservable: ");
        mBinding.musicSeekbar.setOnSeekBarChangeListener(this);
        mBinding.musicDialogClose.setOnClickListener(view -> dismiss());
        mBinding.musicAlbumCard.setOnClickListener(view -> toggleLyrics());
        mBinding.musicLyricsPage.setOnClickListener(view -> toggleLyrics());
        mBinding.musicPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFragment == null || mFragment.mMusicListener == null) {
                    return;
                }
                if (mFragment.mMusicListener.isPlaying()) {
                    mFragment.mMusicListener.pause();
                } else {
                    mFragment.mMusicListener.play();
                }
            }
        });
        mBinding.musicNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFragment != null && mFragment.mMusicListener != null) {
                    mFragment.mMusicListener.playNext();
                }
            }
        });
        mBinding.musicPreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFragment != null && mFragment.mMusicListener != null) {
                    mFragment.mMusicListener.playLast();
                }
            }
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            updateProgressTextWithProgress(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        BackgroundThreadUtils.getInstance().removeCallbacks(mProgressCallback);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        seekTo(getDuration(seekBar.getProgress()));
        if (mFragment != null && mFragment.mMusicListener != null && mFragment.mMusicListener.isPlaying()) {
            BackgroundThreadUtils.getInstance().removeCallbacks(mProgressCallback);
            BackgroundThreadUtils.getInstance().post(mProgressCallback);
        }
    }
}
