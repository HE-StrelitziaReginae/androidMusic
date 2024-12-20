package com.artillery.musicmain.ui;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.SeekBar;

import androidx.core.content.ContextCompat;

import com.artillery.musicbase.base.AppManager;
import com.artillery.musicbase.base.BaseDialog;
import com.artillery.musicbase.utils.BackgroundThreadUtils;
import com.artillery.musicbase.utils.KLogUtils;
import com.artillery.musicmain.R;
import com.artillery.musicmain.data.MusicDaraListener;
import com.artillery.musicmain.databinding.MusicMianDialogBinding;
import com.artillery.musicmain.observer.MusicObserver;
import com.artillery.musicmain.utils.TimeUtils;
import com.artillery.musicservice.data.Song;

public class MusicMainDialog extends BaseDialog<MusicMianDialogBinding> implements SeekBar.OnSeekBarChangeListener, MusicObserver {
    private MusicMainFragment mFragment;
    private Song mSong;
    public final Runnable mProgressCallback = new Runnable() {
        @Override
        public void run() {
            int progress = (int) (mBinding.musicSeekbar.getMax()
                    * ((float) mFragment.mMusicListener.getProgress() / (float) getCurrentSongDuration()));
            mBinding.musicSeekbar.post(() -> {
                updateProgressTextWithDuration(mFragment.mMusicListener.getProgress());
            });
            if (progress >= 0 && progress <= mBinding.musicSeekbar.getMax()) {
                mBinding.musicSeekbar.setProgress((int) mFragment.mMusicListener.currentPosition() / 1000);
                BackgroundThreadUtils.getInstance().postDelayed(mProgressCallback, DateUtils.SECOND_IN_MILLIS);
            }
        }
    };

    private static class Holder {
        private static final MusicMainDialog instance = new MusicMainDialog(AppManager.getAppManager().currentActivity());
    }

    public static MusicMainDialog getInstance() {
        return MusicMainDialog.Holder.instance;
    }

    public MusicMainDialog(Context context) {
        super(context, R.style.CustomFullDialog);
    }

    @Override
    public int initContentView() {
        return R.layout.music_mian_dialog;
    }

    @Override
    public void initData() {
        KLogUtils.i("initData: ");
        mFragment = (MusicMainFragment) AppManager.getAppManager().currentFragment();
        MusicDaraListener.getInstance().setMusicObserver(this);
    }

    @Override
    public void update(Song song) {
        updateMainUi(song);
        activateMarquee(true);
    }

    @Override
    public void updateUi(boolean isPlaying) {
        KLogUtils.e("updatePlayToggle: " + isPlaying);
        mBinding.musicPlayBtn.setImageDrawable(isPlaying ? ContextCompat.getDrawable(getContext(), R.drawable.pause)
                : ContextCompat.getDrawable(getContext(), R.drawable.play));
    }

    @Override
    public void dismiss() {
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
        mBinding.musicTimeEnd.setText(TimeUtils.formatDuration(song.getDuration()));
    }

    private void activateMarquee(boolean activate) {
        mBinding.musicNamePlay.setFocusable(activate);
        mBinding.musicNamePlay.setFocusableInTouchMode(activate);
        mBinding.musicNamePlay.setSelected(activate);
    }

    private int getDuration(int progress) {
        return (int) (getCurrentSongDuration() * ((float) progress / mBinding.musicSeekbar.getMax()));
    }

    private void updateProgressTextWithProgress(int progress) {
        mBinding.musicTimeStart.setText(TimeUtils.formatDuration(getDuration(progress)));
    }

    private void updateProgressTextWithDuration(int duration) {
        mBinding.musicTimeStart.setText(TimeUtils.formatDuration(duration));
    }

    private void seekTo(int duration) {
        mFragment.mMusicListener.seekTo(duration);
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
        mBinding.musicPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                mFragment.mMusicListener.playNext();
            }
        });
        mBinding.musicPreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragment.mMusicListener.playLast();
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
        if (mFragment.mMusicListener.isPlaying()) {
            BackgroundThreadUtils.getInstance().removeCallbacks(mProgressCallback);
            BackgroundThreadUtils.getInstance().post(mProgressCallback);
        }
    }
}
