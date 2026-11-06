package com.artillery.musicmain.ui.musicList;

import android.database.ContentObserver;
import android.os.Build;
import android.os.Looper;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.artillery.musicbase.base.BaseFragment;
import com.artillery.musicbase.utils.BackgroundThreadUtils;
import com.artillery.musicmain.BR;
import com.artillery.musicmain.R;
import com.artillery.musicmain.app.AppViewModelFactory;
import com.artillery.musicmain.data.MusicDaraListener;
import com.artillery.musicmain.databinding.FragmentMusicListBinding;
import com.os.sys.musicskin.MusicSkin;
import com.artillery.musicmain.viewmodel.MusicListViewModel;
import com.artillery.musicservice.data.Song;

import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;

/**
 * @author ArtilleryOrchid
 */
public class MusicListFragment extends BaseFragment<FragmentMusicListBinding, MusicListViewModel> {
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private int mRefreshGeneration;
    private boolean mMusicListLoaded;
    private long mLastMediaGeneration = -1;
    private boolean mScanInProgress;
    private boolean mRefreshRequested;
    private final Runnable mDebouncedRefresh = this::refreshMusicList;
    private final ContentObserver mMusicObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            mMainHandler.removeCallbacks(mDebouncedRefresh);
            mMainHandler.postDelayed(mDebouncedRefresh, 500);
        }
    };

    private boolean mObserverRegistered;

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_music_list;
    }

    @Override
    public int initVariableId() {
        return BR.musicListModel;
    }

    @Override
    public MusicListViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(
                requireActivity().getApplication());
        return ViewModelProviders.of(this, factory).get(MusicListViewModel.class);
    }

    @Override
    public void onStart() {
        super.onStart();
        configureListPerformance();
        Song playingSong = MusicDaraListener.getInstance().getPlayingSong();
        mViewModel.selectSong(playingSong);
        long mediaGeneration = getMediaGeneration();
        if (!mMusicListLoaded || mediaGeneration < 0 || mediaGeneration != mLastMediaGeneration) {
            refreshMusicList();
        }
        applySkin(MusicSkin.load(requireContext()));
        if (!mObserverRegistered) {
            requireContext().getContentResolver().registerContentObserver(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, mMusicObserver);
            mObserverRegistered = true;
        }
    }

    private void configureListPerformance() {
        if (mBinding == null) {
            return;
        }
        RecyclerView recyclerView = mBinding.musicList;
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(8);
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
    }

    @Override
    public void onStop() {
        mRefreshGeneration++;
        mMainHandler.removeCallbacks(mDebouncedRefresh);
        if (mObserverRegistered) {
            requireContext().getContentResolver().unregisterContentObserver(mMusicObserver);
            mObserverRegistered = false;
        }
        super.onStop();
    }

    public void refreshMusicList() {
        if (mViewModel == null || !isAdded() || getView() == null) {
            return;
        }
        if (mScanInProgress) {
            mRefreshRequested = true;
            return;
        }
        mScanInProgress = true;
        mRefreshRequested = false;
        final int generation = ++mRefreshGeneration;
        BackgroundThreadUtils.getInstance().post(() -> {
            ArrayList<Song> music = mViewModel.scanMusicList();
            mMainHandler.post(() -> {
                mScanInProgress = false;
                if (!isAdded() || mViewModel == null || generation != mRefreshGeneration) {
                    refreshIfRequested();
                    return;
                }
                if (music == null) {
                    refreshIfRequested();
                    return;
                }
                mMusicListLoaded = true;
                mLastMediaGeneration = getMediaGeneration();
                mViewModel.updateMusicList(music);
                MusicDaraListener.getInstance().updateMusicList(music);
                if (mMusicSkin != null) {
                    applySkin(mMusicSkin);
                }
                refreshIfRequested();
            });
        });
    }

    private void refreshIfRequested() {
        if (mRefreshRequested && isAdded() && getView() != null) {
            mRefreshRequested = false;
            refreshMusicList();
        }
    }

    private long getMediaGeneration() {
        if (!isAdded() || Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return -1;
        }
        try {
            return MediaStore.getGeneration(requireContext(), MediaStore.VOLUME_EXTERNAL);
        } catch (RuntimeException ignored) {
            return -1;
        }
    }

    private MusicSkin mMusicSkin;

    public void applySkin(MusicSkin skin) {
        mMusicSkin = skin;
        if (mBinding == null) {
            return;
        }
        mViewModel.applySkin(skin);
        mBinding.musicListRoot.setBackgroundColor(skin.background(requireContext()));
        boolean characterSkin = skin.isCharacterSkin();
        mBinding.musicSkinBanner.setVisibility(characterSkin ? View.VISIBLE : View.GONE);
        if (characterSkin) {
            mBinding.musicSkinBannerArt.setImageResource(skin.keyArt());
            mBinding.musicSkinBanner.setBackground(skin.featureBackground(requireContext()));
            mBinding.musicSkinBannerEyebrow.setText(skin.musicEyebrow());
            mBinding.musicSkinBannerTitle.setText(skin.musicTitle());
            mBinding.musicSkinBannerSubtitle.setText(skin.musicSubtitle());
            mBinding.musicSkinBannerEyebrow.setTextColor(skin.headerSecondaryText(requireContext()));
            mBinding.musicSkinBannerTitle.setTextColor(skin.headerPrimaryText(requireContext()));
            mBinding.musicSkinBannerSubtitle.setTextColor(skin.headerSecondaryText(requireContext()));
        }
    }

    public void selectSong(@Nullable Song song) {
        if (mViewModel != null) {
            mViewModel.selectSong(song);
        }
    }

}
