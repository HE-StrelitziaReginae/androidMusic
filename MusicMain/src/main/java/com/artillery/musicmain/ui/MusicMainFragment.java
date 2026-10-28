package com.artillery.musicmain.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProviders;

import com.artillery.musicbase.base.AppManager;
import com.artillery.musicbase.base.BaseFragment;
import com.artillery.musicbase.binding.command.BindingCommand;
import com.artillery.musicbase.utils.BackgroundThreadUtils;
import com.artillery.musicbase.utils.KLogUtils;
import com.artillery.musicmain.BR;
import com.artillery.musicmain.R;
import com.artillery.musicmain.app.AppViewModelFactory;
import com.artillery.musicmain.data.MusicDaraListener;
import com.artillery.musicmain.data.MusicDataListener;
import com.artillery.musicmain.data.source.contract.view.MusicPlayView;
import com.artillery.musicmain.databinding.FragmentMusicMainBinding;
import com.artillery.musicmain.viewmodel.MusicMainViewModel;
import com.artillery.musicservice.data.PlayList;
import com.artillery.musicservice.data.Song;
import com.artillery.musicservice.service.MusicListener;
import com.artillery.musicservice.service.MusicMode;
import com.artillery.musicservice.service.MusicService;

import java.util.ArrayList;

/**
 * @author ArtilleryOrchid
 */
public class MusicMainFragment extends BaseFragment<FragmentMusicMainBinding, MusicMainViewModel>
        implements MusicPlayView, MusicListener.Callback {
    protected MusicListener mMusicListener;
    private PlayList mPlayList;
    private ArrayList<Song> mMusicList = new ArrayList<>();
    private boolean mMusicListLoaded;
    private int mStartIndex = 0;

    @Override
    public void initData() {
        AppManager.getAppManager().addFragment(this);
        mViewModel.showFragment(requireActivity(), 0);
        MusicDaraListener.getInstance().setMusicDataListener(new MusicDataListener() {
            @Override
            public void sendMusicSong(ArrayList<Song> songArrayList, int index, Song song) {
                KLogUtils.i("sendMusicSong: ");
                mPlayList = new PlayList();
                mPlayList.setSongs(songArrayList);
                mPlayList.setNumOfSongs(songArrayList.size());
                mStartIndex = index;
                updateMainUi(song);
                startPlay();
                activateMarquee(true);
            }

            @Override
            public void onMusicListUpdated(ArrayList<Song> songArrayList) {
                mMusicList = songArrayList == null ? new ArrayList<>() : new ArrayList<>(songArrayList);
                mMusicListLoaded = true;
                if (mMusicListener != null) {
                    mMusicListener.syncPlayList(mMusicList);
                }
            }
        });
    }

    @Override
    public void initViewObservable() {
        applySystemBarInsets();
        mViewModel.binMusicView(this);
        mBinding.musicGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == mBinding.musicMain.getId()) {
                    mViewModel.showFragment(requireActivity(), 0);
                } else if (checkedId == mBinding.musicOnline.getId()) {
                    mViewModel.showFragment(requireActivity(), 1);
                } else if (checkedId == mBinding.musicMine.getId()) {
                    mViewModel.showFragment(requireActivity(), 2);
                }
            }
        });
        mViewModel.mPlay = new BindingCommand(() -> {
            if (mMusicListener.isPlaying()) {
                mMusicListener.pause();
            } else {
                mMusicListener.play();
            }
        });
        mViewModel.mShow = new BindingCommand(() -> {
            if (!MusicMainDialog.getInstance().isShowing()) {
                MusicMainDialog.getInstance().show();
            }
        });
        initDrawer();
    }

    private void applySystemBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.musicDrawerLayout, (view, windowInsets) -> {
            int topInset = windowInsets.getSystemWindowInsetTop();
            int bottomInset = windowInsets.getSystemWindowInsetBottom();
            ViewGroup.MarginLayoutParams toolbarParams =
                    (ViewGroup.MarginLayoutParams) mBinding.musicToolbar.getLayoutParams();
            toolbarParams.topMargin = topInset;
            mBinding.musicToolbar.setLayoutParams(toolbarParams);

            ViewGroup.MarginLayoutParams groupParams =
                    (ViewGroup.MarginLayoutParams) mBinding.musicGroup.getLayoutParams();
            groupParams.bottomMargin = bottomInset;
            mBinding.musicGroup.setLayoutParams(groupParams);

            mBinding.musicDrawerPanel.setPadding(
                    mBinding.musicDrawerPanel.getPaddingLeft(),
                    topInset,
                    mBinding.musicDrawerPanel.getPaddingRight(),
                    bottomInset);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(mBinding.musicDrawerLayout);
    }

    private void initDrawer() {
        updateDrawerPlayMode(MusicMode.getDefault());
        mBinding.musicMenuBtn.setOnClickListener(view -> mBinding.musicDrawerLayout.openDrawer(mBinding.musicDrawerPanel));
        mBinding.drawerProfile.setOnClickListener(view -> showProfileDialog());
        mBinding.drawerRefresh.setOnClickListener(view -> {
            mViewModel.refreshMusicList(requireActivity());
            mBinding.musicDrawerLayout.closeDrawer(mBinding.musicDrawerPanel);
            Toast.makeText(requireContext(), R.string.music_refresh_done, Toast.LENGTH_SHORT).show();
        });
        mBinding.drawerPlayMode.setOnClickListener(view -> showPlayModeDialog());
        mBinding.drawerPlayer.setOnClickListener(view -> {
            mBinding.musicDrawerLayout.closeDrawer(mBinding.musicDrawerPanel);
            if (!MusicMainDialog.getInstance().isShowing()) {
                MusicMainDialog.getInstance().show();
            }
        });
        mBinding.drawerAbout.setOnClickListener(view -> showAboutDialog());
    }

    private void showProfileDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.music_profile_name)
                .setMessage(R.string.music_profile_message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.music_about)
                .setMessage(R.string.music_about_message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showPlayModeDialog() {
        if (mMusicListener == null) {
            return;
        }
        MusicMode currentMode = mMusicListener.getPlayMode();
        MusicMode[] modes = MusicMode.values();
        int selectedIndex = currentMode == null ? MusicMode.getDefault().ordinal() : currentMode.ordinal();
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.music_play_mode_title)
                .setSingleChoiceItems(R.array.music_play_modes, selectedIndex, (dialog, which) -> {
                    mMusicListener.setPlayMode(modes[which]);
                    updatePlayMode(modes[which]);
                    dialog.dismiss();
                    mBinding.musicDrawerLayout.closeDrawer(mBinding.musicDrawerPanel);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void updateDrawerPlayMode(@Nullable MusicMode playMode) {
        MusicMode mode = playMode == null ? MusicMode.getDefault() : playMode;
        String[] modeNames = getResources().getStringArray(R.array.music_play_modes);
        mBinding.drawerPlayModeValue.setText(modeNames[mode.ordinal()]);
    }

    private void activateMarquee(boolean activate) {
        mBinding.musicNamePlay.setFocusable(activate);
        mBinding.musicNamePlay.setFocusableInTouchMode(activate);
        mBinding.musicNamePlay.setSelected(activate);
    }

    @Override
    public void onStop() {
        super.onStop();
        activateMarquee(false);
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
        updateDrawerPlayMode(mMusicListener.getPlayMode());
        if (mMusicListLoaded) {
            mMusicListener.syncPlayList(mMusicList);
        }
    }

    private void startPlay() {
        KLogUtils.e("startPlay: ");
        if (mMusicListener != null) {
            mMusicListener.play(mPlayList, mStartIndex);
            BackgroundThreadUtils.getInstance().post(MusicMainDialog.getInstance().mProgressCallback);
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
            return;
        }
        //Music Duration
        updateMainUi(song);
    }

    private void updateMainUi(Song song) {
        KLogUtils.e("updateMainUi: ");
        MusicDaraListener.getInstance().setUpdate(song);
        mBinding.musicNamePlay.setText(song.getTitle());
        mBinding.musicArtistPlay.setText(song.getArtist());
    }

    @Override
    public void updatePlayMode(MusicMode playMode) {
        KLogUtils.e("updatePlayMode: ");
        updateDrawerPlayMode(playMode);
    }

    @Override
    public void updatePlayToggle(boolean play) {
        KLogUtils.e("updatePlayToggle: " + play);
        MusicDaraListener.getInstance().setUpdateUi(play);
        mBinding.musicPlayBtn.setImageDrawable(play ? ContextCompat.getDrawable(requireContext(), R.drawable.pause)
                : ContextCompat.getDrawable(requireContext(), R.drawable.play));
    }

    @Override
    public void updateFavoriteToggle(boolean favorite) {
        KLogUtils.e("updateFavoriteToggle: ");
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_music_main;
    }

    @Override
    public MusicMainViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(requireActivity().getApplication());
        return ViewModelProviders.of(this, factory).get(MusicMainViewModel.class);
    }

    @Override
    public int initVariableId() {
        return BR.musicMainModel;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (MusicMainDialog.getInstance().isShowing()) {
            MusicMainDialog.getInstance().dismiss();
        }
        mViewModel.mMusicRepository.unBindMusicService();
    }
}
