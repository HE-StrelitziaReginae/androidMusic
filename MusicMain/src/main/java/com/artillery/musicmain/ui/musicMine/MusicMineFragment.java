package com.artillery.musicmain.ui.musicMine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.artillery.musicbase.base.BaseFragment;
import com.artillery.musicmain.BR;
import com.artillery.musicmain.R;
import com.artillery.musicmain.databinding.FragmentMusicMineBinding;
import com.artillery.musicmain.ui.musicList.MusicListFragment;
import com.os.sys.musicskin.MusicSkin;
import com.artillery.musicmain.viewmodel.MusicMineViewModel;

/**
 * @author ArtilleryOrchid
 */
public class MusicMineFragment extends BaseFragment<FragmentMusicMineBinding, MusicMineViewModel> {
    @Override
    public void onStart() {
        super.onStart();
        applySkin(MusicSkin.load(requireContext()));
    }

    public void applySkin(MusicSkin skin) {
        if (mBinding == null) {
            return;
        }
        boolean characterSkin = skin.isCharacterSkin();
        mBinding.musicMineRoot.setBackgroundColor(skin.background(requireContext()));
        mBinding.musicMineSkinBanner.setVisibility(characterSkin ? View.VISIBLE : View.GONE);
        if (characterSkin) {
            mBinding.musicMineSkinBannerArt.setImageResource(skin.keyArt());
            mBinding.musicMineSkinBanner.setBackground(skin.featureBackground(requireContext()));
            mBinding.musicMineSkinEyebrow.setText(skin.mineEyebrow());
            mBinding.musicMineSkinTitle.setText(skin.mineTitle());
            mBinding.musicMineSkinSubtitle.setText(skin.mineSubtitle());
            mBinding.musicMineSkinEyebrow.setTextColor(skin.headerSecondaryText(requireContext()));
            mBinding.musicMineSkinTitle.setTextColor(skin.headerPrimaryText(requireContext()));
            mBinding.musicMineSkinSubtitle.setTextColor(skin.headerSecondaryText(requireContext()));
        }
        mBinding.musicMineFavorites.setBackground(skin.outlinedCardBackground(requireContext()));
        mBinding.musicMineLocalTracks.setBackground(skin.outlinedCardBackground(requireContext()));
        mBinding.musicMinePlaylists.setBackground(skin.outlinedCardBackground(requireContext()));
        mBinding.musicMineFavorites.setTextColor(skin.primaryText(requireContext()));
        mBinding.musicMineLocalTracks.setTextColor(skin.primaryText(requireContext()));
        mBinding.musicMinePlaylists.setTextColor(skin.primaryText(requireContext()));
        mBinding.musicMineEmptyCard.setBackground(skin.outlinedCardBackground(requireContext()));
        mBinding.musicMineEmptyIcon.setColorFilter(skin.icon(requireContext()));
        mBinding.musicMineEmptyTitle.setTextColor(skin.primaryText(requireContext()));
        mBinding.musicMineEmptyMessage.setTextColor(skin.secondaryText(requireContext()));
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_music_mine;
    }

    @Override
    public int initVariableId() {
        return BR.musicMineModel;
    }
}
