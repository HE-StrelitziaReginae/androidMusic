package com.artillery.musicmain.ui.musicDialog;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.artillery.musicbase.base.BaseDialogFragment;
import com.artillery.musicbase.binding.command.BindingAction;
import com.artillery.musicbase.binding.command.BindingCommand;
import com.artillery.musicbase.utils.BackgroundThreadUtils;
import com.artillery.musicbase.utils.KLogUtils;
import com.artillery.musicmain.BR;
import com.artillery.musicmain.R;
import com.artillery.musicmain.app.AppViewModelFactory;
import com.artillery.musicmain.data.MusicDaraListener;
import com.artillery.musicmain.data.MusicDataListener;
import com.artillery.musicmain.data.source.contract.view.MusicPlayView;
import com.artillery.musicmain.databinding.FragmentMusicDialogBinding;
import com.artillery.musicmain.utils.TimeUtils;
import com.artillery.musicmain.viewmodel.MusicDialogViewModel;
import com.artillery.musicservice.data.PlayList;
import com.artillery.musicservice.data.Song;
import com.artillery.musicservice.service.MusicListener;
import com.artillery.musicservice.service.MusicMode;
import com.artillery.musicservice.service.MusicService;

import java.util.ArrayList;

public class MusicDialogFragment extends BaseDialogFragment<FragmentMusicDialogBinding, MusicDialogViewModel>
        implements MusicPlayView, MusicListener.Callback, SeekBar.OnSeekBarChangeListener {
    private MusicListener mMusicListener;
    private PlayList mPlayList;
    private int mStartIndex = 0;
    private final Runnable mProgressCallback = new Runnable() {
        @Override
        public void run() {
            if (isDetached()) {
                return;
            }
            int progress = (int) (mBinding.musicSeekbar.getMax()
                    * ((float) mMusicListener.getProgress() / (float) getCurrentSongDuration()));
            updateProgressTextWithDuration(mMusicListener.getProgress());
            if (progress >= 0 && progress <= mBinding.musicSeekbar.getMax()) {
                mBinding.musicSeekbar.setProgress((int) mMusicListener.currentPosition() / 1000);
                BackgroundThreadUtils.getInstance().postDelayed(mProgressCallback, DateUtils.SECOND_IN_MILLIS);
            }
        }
    };

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        mBinding.musicSeekbar.setOnSeekBarChangeListener(this);
        mViewModel.mPlay = new BindingCommand(new BindingAction() {
            @Override
            public void call() {
                if (mMusicListener.isPlaying()) {
                    mMusicListener.pause();
                } else {
                    mMusicListener.play();
                }
                BackgroundThreadUtils.getInstance().post(mProgressCallback);
            }
        });
        mViewModel.mNext = new BindingCommand(new BindingAction() {
            @Override
            public void call() {
                mMusicListener.playNext();
            }
        });
        mViewModel.mPre = new BindingCommand(new BindingAction() {
            @Override
            public void call() {
                mMusicListener.playLast();
            }
        });
        MusicDaraListener.getInstance().setMusicDataListener(new MusicDataListener() {
            @Override
            public void sendMusicList(ArrayList<Song> songArrayList, int index) {
                mPlayList = new PlayList();
                mPlayList.setSongs(songArrayList);
                mPlayList.setNumOfSongs(songArrayList.size());
                mStartIndex = index;
                startPlay();
                activateMarquee(true);
            }

            @Override
            public void sendMusicSong(Song song) {
                updateMainUi(song);
            }
        });
    }

    private void activateMarquee(boolean activate) {
        mBinding.musicNamePlay.setFocusable(activate);
        mBinding.musicNamePlay.setFocusableInTouchMode(activate);
        mBinding.musicNamePlay.setSelected(activate);
    }

    public void onStart() {
        super.onStart();
        if (mMusicListener != null && mMusicListener.isPlaying()) {
            BackgroundThreadUtils.getInstance().removeCallbacks(mProgressCallback);
            BackgroundThreadUtils.getInstance().post(mProgressCallback);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        activateMarquee(false);
        BackgroundThreadUtils.getInstance().removeCallbacks(mProgressCallback);
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
        if (mMusicListener.isPlaying()) {
            BackgroundThreadUtils.getInstance().removeCallbacks(mProgressCallback);
            BackgroundThreadUtils.getInstance().post(mProgressCallback);
        }
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
        mMusicListener.seekTo(duration);
    }

    private long getCurrentSongDuration() {
        Song currentSong = mMusicListener.getPlayingSong();
        int duration = 0;
        if (currentSong != null) {
            duration = currentSong.getDuration();
        }
        return duration;
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
        updatePlayToggle(isPlaying);
        if (isPlaying) {
            BackgroundThreadUtils.getInstance().removeCallbacks(mProgressCallback);
            BackgroundThreadUtils.getInstance().post(mProgressCallback);
        } else {
            BackgroundThreadUtils.getInstance().removeCallbacks(mProgressCallback);
        }
    }

    @Override
    public void handleError(Throwable error) {
        KLogUtils.e("error: " + error);
    }

    @Override
    public void onPlaybackServiceBound(MusicService service) {
        KLogUtils.e("onPlaybackServiceBound: ");
        mMusicListener = service;
        mMusicListener.registerCallback(this);
    }

    private void startPlay() {
        KLogUtils.e("startPlay: ");
        if (mMusicListener != null) {
            mMusicListener.play(mPlayList, mStartIndex);
            BackgroundThreadUtils.getInstance().post(mProgressCallback);
        }
    }

    @Override
    public void onPlaybackServiceUnbound() {
        KLogUtils.e("onPlaybackServiceUnbound: ");
        mMusicListener.unregisterCallback(this);
        mMusicListener = null;
    }

    @Override
    public void onSongSetAsFavorite(@NonNull Song song) {
        KLogUtils.e("onSongSetAsFavorite: ");
    }

    @Override
    public void onSongUpdated(@Nullable Song song) {
        KLogUtils.e("onSongUpdated: ");
        if (song == null) {
            BackgroundThreadUtils.getInstance().removeCallbacks(mProgressCallback);
            return;
        }
        //Music Duration
        updateMainUi(song);
        BackgroundThreadUtils.getInstance().removeCallbacks(mProgressCallback);
        if (mMusicListener.isPlaying()) {
            BackgroundThreadUtils.getInstance().post(mProgressCallback);
        }
    }

    private void updateMainUi(Song song) {
        KLogUtils.e("updateMainUi: ");
        mBinding.musicSeekbar.setProgress(0);
        mBinding.musicSeekbar.setMax((int) (getCurrentSongDuration() / 1000));
        mBinding.musicNamePlay.setText(song.getTitle());
        mBinding.musicArtistPlay.setText(song.getArtist());
        mBinding.musicTimeEnd.setText(TimeUtils.formatDuration(song.getDuration()));
    }

    @Override
    public void updatePlayMode(MusicMode playMode) {
        KLogUtils.e("updatePlayMode: ");
    }

    @Override
    public void updatePlayToggle(boolean play) {
        KLogUtils.e("updatePlayToggle: " + play);
        mBinding.musicPlayBtn.setImageDrawable(play ? ContextCompat.getDrawable(requireContext(), R.drawable.pause)
                : ContextCompat.getDrawable(requireContext(), R.drawable.play));
    }

    @Override
    public void updateFavoriteToggle(boolean favorite) {
        KLogUtils.e("updateFavoriteToggle: ");
    }

    @Override
    public MusicDialogViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(requireActivity().getApplication());
        return ViewModelProviders.of(this, factory).get(MusicDialogViewModel.class);
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_music_dialog;
    }

    @Override
    public int initVariableId() {
        return BR.musicDialog;
    }
}
