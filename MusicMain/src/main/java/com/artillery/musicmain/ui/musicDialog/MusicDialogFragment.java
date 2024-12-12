package com.artillery.musicmain.ui.musicDialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.artillery.musicbase.base.BaseDialogFragment;
import com.artillery.musicmain.BR;
import com.artillery.musicmain.R;
import com.artillery.musicmain.app.AppViewModelFactory;
import com.artillery.musicmain.data.source.contract.view.MusicPlayView;
import com.artillery.musicmain.databinding.FragmentMusicDialogBinding;
import com.artillery.musicmain.viewmodel.MusicDialogViewModel;
import com.artillery.musicservice.data.Song;
import com.artillery.musicservice.service.MusicListener;
import com.artillery.musicservice.service.MusicMode;
import com.artillery.musicservice.service.MusicService;

public class MusicDialogFragment extends BaseDialogFragment<FragmentMusicDialogBinding, MusicDialogViewModel>
        implements MusicPlayView, MusicListener.Callback, SeekBar.OnSeekBarChangeListener {
    private static class Holder {
        private static final MusicDialogFragment instance = new MusicDialogFragment();
    }

    public static MusicDialogFragment getInstance() {
        return MusicDialogFragment.Holder.instance;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void handleError(Throwable error) {

    }

    @Override
    public void onPlaybackServiceBound(MusicService service) {

    }

    @Override
    public void onPlaybackServiceUnbound() {

    }

    @Override
    public void onSongSetAsFavorite(@NonNull Song song) {

    }

    @Override
    public void onSongUpdated(@Nullable Song song) {

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

    @Override
    public void onSwitchLast(@Nullable Song last) {

    }

    @Override
    public void onSwitchNext(@Nullable Song next) {

    }

    @Override
    public void onComplete(@Nullable Song next) {

    }

    @Override
    public void onPlayStatusChanged(boolean isPlaying) {

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
