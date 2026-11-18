package com.artillery.musicmain.ui.musicList;

import android.database.ContentObserver;
import android.os.Build;
import android.os.Looper;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.os.Handler;
import android.widget.Toast;

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
    private ArrayList<Song> mCurrentMusic = new ArrayList<>();

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
        configureInteractions();
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

    private void configureInteractions() {
        if (mBinding == null) {
            return;
        }
        if (!mBinding.musicRefreshButton.hasOnClickListeners()) {
            mBinding.musicRefreshButton.setOnClickListener(view -> {
                animatePress(view);
                refreshMusicList();
            });
        }
        if (!mBinding.musicShuffleButton.hasOnClickListeners()) {
            mBinding.musicShuffleButton.setOnClickListener(view -> {
                animatePress(view);
                if (mCurrentMusic.isEmpty()) {
                    Toast.makeText(requireContext(), R.string.music_empty_subtitle, Toast.LENGTH_SHORT).show();
                    return;
                }
                int index = new java.util.Random().nextInt(mCurrentMusic.size());
                Song song = mCurrentMusic.get(index);
                mViewModel.selectSong(song);
                MusicDaraListener.getInstance().observerMusicData(mCurrentMusic, index, song);
            });
        }
    }

    private void animatePress(View view) {
        view.animate().cancel();
        view.setScaleX(0.94f);
        view.setScaleY(0.94f);
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(220)
                .setInterpolator(new DecelerateInterpolator())
                .start();
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
                mCurrentMusic = new ArrayList<>(music);
                mMusicListLoaded = true;
                mLastMediaGeneration = getMediaGeneration();
                mViewModel.updateMusicList(music);
                MusicDaraListener.getInstance().updateMusicList(music);
                updateLibraryState(music);
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

    private void updateLibraryState(ArrayList<Song> music) {
        if (mBinding == null) {
            return;
        }
        int count = music == null ? 0 : music.size();
        mBinding.musicSkinBannerCount.setText(getString(R.string.music_library_count, count));
        boolean empty = count == 0;
        mBinding.musicList.setVisibility(empty ? View.GONE : View.VISIBLE);
        mBinding.musicEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        mBinding.musicShuffleButton.setAlpha(empty ? 0.48f : 1f);
    }

    public void applySkin(MusicSkin skin) {
        mMusicSkin = skin;
        if (mBinding == null) {
            return;
        }
        mViewModel.applySkin(skin);
        mBinding.musicListRoot.setBackgroundColor(skin.background(requireContext()));
        boolean characterSkin = skin.isCharacterSkin();
        mBinding.musicSkinBanner.setBackground(skin.featureBackground(requireContext()));
        mBinding.musicSkinBannerArt.setVisibility(characterSkin ? View.VISIBLE : View.GONE);
        mBinding.musicSkinBannerScrim.setVisibility(characterSkin ? View.VISIBLE : View.GONE);
        if (characterSkin) {
            mBinding.musicSkinBannerArt.setImageResource(skin.bannerArt());
            mBinding.musicSkinBannerArt.setScaleType(skin.hasSeparateBannerArt()
                    ? android.widget.ImageView.ScaleType.FIT_END
                    : android.widget.ImageView.ScaleType.CENTER_CROP);
            mBinding.musicSkinBannerEyebrow.setText(skin.musicEyebrow());
            mBinding.musicSkinBannerTitle.setText(skin.musicTitle());
            mBinding.musicSkinBannerSubtitle.setText(skin.musicSubtitle());
            mBinding.musicEmptyAvatar.setImageResource(skin.avatar());
        } else {
            mBinding.musicSkinBannerEyebrow.setText(R.string.music_library_greeting);
            mBinding.musicSkinBannerTitle.setText(R.string.music_library_title);
            mBinding.musicSkinBannerSubtitle.setText(R.string.music_library_subtitle);
            mBinding.musicEmptyAvatar.setImageResource(R.drawable.ellipse);
        }
        int bannerPrimary = characterSkin ? skin.headerPrimaryText(requireContext()) : skin.primaryText(requireContext());
        int bannerSecondary = characterSkin ? skin.headerSecondaryText(requireContext()) : skin.secondaryText(requireContext());
        mBinding.musicSkinBannerEyebrow.setTextColor(bannerSecondary);
        mBinding.musicSkinBannerTitle.setTextColor(bannerPrimary);
        mBinding.musicSkinBannerSubtitle.setTextColor(bannerSecondary);
        mBinding.musicSkinBannerCount.setTextColor(bannerPrimary);
        mBinding.musicLibraryLabel.setTextColor(skin.secondaryText(requireContext()));
        mBinding.musicShuffleButton.setBackground(skin.accentPillBackground(requireContext()));
        mBinding.musicRefreshButton.setBackground(skin.accentPillBackground(requireContext()));
        mBinding.musicShuffleButton.setTextColor(skin.accent(requireContext()));
        mBinding.musicRefreshButton.setColorFilter(skin.accent(requireContext()));
        mBinding.musicEmptyTitle.setTextColor(skin.primaryText(requireContext()));
        mBinding.musicEmptySubtitle.setTextColor(skin.secondaryText(requireContext()));
        updateLibraryState(mCurrentMusic);
    }

    public void selectSong(@Nullable Song song) {
        if (mViewModel != null) {
            mViewModel.selectSong(song);
        }
    }

}
