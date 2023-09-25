package com.artillery.musicmain.ui.music;

import android.os.Bundle;
import android.os.Handler;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.artillery.musicbase.base.BaseActivity;
import com.artillery.musicbase.binding.command.BindingAction;
import com.artillery.musicbase.binding.command.BindingCommand;
import com.artillery.musicbase.utils.Utils;
import com.artillery.musicmain.BR;
import com.artillery.musicmain.R;
import com.artillery.musicmain.app.AppViewModelFactory;
import com.artillery.musicmain.data.source.contract.view.MusicPlayView;
import com.artillery.musicmain.databinding.ActivityMusicMainBinding;
import com.artillery.musicservice.data.MusicLocalUtils;
import com.artillery.musicservice.data.Song;
import com.artillery.musicservice.service.MusicListener;
import com.artillery.musicservice.service.MusicMode;
import com.artillery.musicservice.service.MusicService;

import java.util.ArrayList;

/**
 * @author ArtilleryOrchid
 */
public class MusicMainActivity extends BaseActivity<ActivityMusicMainBinding, MusicMainViewModel> implements MusicPlayView, MusicListener.Callback, SeekBar.OnSeekBarChangeListener {
    private static final long UPDATE_PROGRESS_INTERVAL = 1000;
    private MusicListener mMusicListener;
    private Handler mHandler = new Handler();
    private Runnable mProgressCallback = new Runnable() {
        @Override
        public void run() {
            if (mMusicListener.isPlaying()) {
                mHandler.postDelayed(this, UPDATE_PROGRESS_INTERVAL);
            }
        }
    };
    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_music_main;
    }

    @Override
    public int initVariableId() {
        return BR.musicModel;
    }

    @Override
    public MusicMainViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(getApplication());
        return ViewModelProviders.of(this,factory).get(MusicMainViewModel.class);
    }

    @Override
    public void initData() {
    }

    @Override
    public void initViewObservable() {
        viewModel.mMusicRepository.bindMusicView(this);
        binding.musicSeekbar.setOnSeekBarChangeListener(this);
        viewModel.play = new BindingCommand(new BindingAction() {
            @Override
            public void call() {
                if (mMusicListener == null) {
                    return;
                }
                MusicLocalUtils instance = MusicLocalUtils.getInstance();
                ArrayList<Song> music = instance.getMusic(Utils.getContext());
                mMusicListener.play(music.get(0));
//                if (mMusicListener.isPlaying()) {
//                    mMusicListener.pause();
//                } else {
//                    mMusicListener.play();
//                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mMusicListener != null && mMusicListener.isPlaying()) {
            mHandler.removeCallbacks(mProgressCallback);
            mHandler.post(mProgressCallback);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mProgressCallback);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onSwitchLast(@Nullable Song last) {
        onSongUpdated(last);
    }

    @Override
    public void onSwitchNext(@Nullable Song next) {
        onSongUpdated(next);
    }

    @Override
    public void onComplete(@Nullable Song next) {
        onSongUpdated(next);
    }

    @Override
    public void onPlayStatusChanged(boolean isPlaying) {

    }

    @Override
    public void handleError(Throwable error) {

    }

    @Override
    public void onPlaybackServiceBound(MusicService service) {
        mMusicListener = service;
        mMusicListener.registerCallback(this);
    }

    @Override
    public void onPlaybackServiceUnbound() {
        mMusicListener.unregisterCallback(this);
        mMusicListener = null;
    }

    @Override
    public void onSongSetAsFavorite(@NonNull Song song) {

    }

    @Override
    public void onSongUpdated(@Nullable Song song) {
        if (song == null) {
            mHandler.removeCallbacks(mProgressCallback);
            return;
        }
        mHandler.removeCallbacks(mProgressCallback);
        if (mMusicListener.isPlaying()) {
            mHandler.post(mProgressCallback);
        }
    }

    @Override
    public void updatePlayMode(MusicMode playMode) {

    }

    @Override
    public void updatePlayToggle(boolean play) {

    }

    @Override
    public void updateFavoriteToggle(boolean favorite) {

    }
}