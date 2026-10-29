package com.artillery.musicmain.ui.musicList;

import android.database.ContentObserver;
import android.os.Looper;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Handler;

import androidx.annotation.Nullable;

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
    private final ContentObserver mMusicObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            refreshMusicList();
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
        refreshMusicList();
        applySkin(MusicSkin.load(requireContext()));
        if (!mObserverRegistered) {
            requireContext().getContentResolver().registerContentObserver(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, mMusicObserver);
            mObserverRegistered = true;
        }
    }

    @Override
    public void onStop() {
        mRefreshGeneration++;
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
        final int generation = ++mRefreshGeneration;
        BackgroundThreadUtils.getInstance().post(() -> {
            ArrayList<Song> music = mViewModel.scanMusicList();
            mMainHandler.post(() -> {
                if (!isAdded() || mViewModel == null || generation != mRefreshGeneration) {
                    return;
                }
                if (music == null) {
                    return;
                }
                mViewModel.updateMusicList(music);
                MusicDaraListener.getInstance().updateMusicList(music);
                if (mMusicSkin != null) {
                    applySkin(mMusicSkin);
                }
            });
        });
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
        mBinding.musicList.post(() -> {
            for (int index = 0; index < mBinding.musicList.getChildCount(); index++) {
                android.view.View item = mBinding.musicList.getChildAt(index);
                android.widget.TextView title = item.findViewById(R.id.music_name);
                android.widget.TextView artist = item.findViewById(R.id.music_artist);
                if (title != null) {
                    title.setTextColor(skin.primaryText(requireContext()));
                }
                if (artist != null) {
                    artist.setTextColor(skin.secondaryText(requireContext()));
                }
            }
        });
    }

}
