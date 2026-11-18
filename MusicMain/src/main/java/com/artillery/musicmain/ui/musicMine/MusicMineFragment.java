package com.artillery.musicmain.ui.musicMine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.artillery.musicbase.base.BaseFragment;
import com.artillery.musicmain.BR;
import com.artillery.musicmain.R;
import com.artillery.musicmain.app.AppViewModelFactory;
import com.artillery.musicmain.databinding.FragmentMusicMineBinding;
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
        mBinding.musicMineSkinBanner.setBackground(skin.featureBackground(requireContext()));
        mBinding.musicMineSkinBannerArt.setVisibility(characterSkin ? View.VISIBLE : View.GONE);
        mBinding.musicMineSkinScrim.setVisibility(characterSkin ? View.VISIBLE : View.GONE);
        if (characterSkin) {
            mBinding.musicMineSkinBannerArt.setImageResource(skin.bannerArt());
            mBinding.musicMineSkinBannerArt.setScaleType(skin.hasSeparateBannerArt()
                    ? android.widget.ImageView.ScaleType.FIT_END
                    : android.widget.ImageView.ScaleType.CENTER_CROP);
            mBinding.musicMineSkinEyebrow.setText(skin.mineEyebrow());
            mBinding.musicMineSkinTitle.setText(skin.mineTitle());
            mBinding.musicMineSkinSubtitle.setText(skin.mineSubtitle());
            mBinding.musicMineEmptyIcon.setImageResource(skin.avatar());
        } else {
            mBinding.musicMineSkinEyebrow.setText(R.string.music_mine_default_eyebrow);
            mBinding.musicMineSkinTitle.setText(R.string.music_mine_default_title);
            mBinding.musicMineSkinSubtitle.setText(R.string.music_mine_default_subtitle);
            mBinding.musicMineEmptyIcon.setImageResource(R.drawable.music_player);
        }
        int bannerPrimary = characterSkin ? skin.headerPrimaryText(requireContext()) : skin.primaryText(requireContext());
        int bannerSecondary = characterSkin ? skin.headerSecondaryText(requireContext()) : skin.secondaryText(requireContext());
        mBinding.musicMineSkinEyebrow.setTextColor(bannerSecondary);
        mBinding.musicMineSkinTitle.setTextColor(bannerPrimary);
        mBinding.musicMineSkinSubtitle.setTextColor(bannerSecondary);
        mBinding.musicMineSectionTitle.setTextColor(skin.secondaryText(requireContext()));
        mBinding.musicMineFavorites.setBackground(skin.outlinedCardBackground(requireContext()));
        mBinding.musicMineLocalTracks.setBackground(skin.outlinedCardBackground(requireContext()));
        mBinding.musicMinePlaylists.setBackground(skin.outlinedCardBackground(requireContext()));
        mBinding.musicMineFavorites.setTextColor(skin.primaryText(requireContext()));
        mBinding.musicMineLocalTracks.setTextColor(skin.primaryText(requireContext()));
        mBinding.musicMinePlaylists.setTextColor(skin.primaryText(requireContext()));
        mBinding.musicMineEmptyCard.setBackground(skin.outlinedCardBackground(requireContext()));
        mBinding.musicMineEmptyIcon.setBackground(skin.toolbarMascotBackground(requireContext()));
        mBinding.musicMineEmptyIcon.setColorFilter(skin.icon(requireContext()));
        mBinding.musicMineEmptyTitle.setTextColor(skin.primaryText(requireContext()));
        mBinding.musicMineEmptyMessage.setTextColor(skin.secondaryText(requireContext()));
    }

    @Override
    public void initViewObservable() {
        bindCard(mBinding.musicMineFavorites, R.string.music_mine_favorites);
        bindCard(mBinding.musicMineLocalTracks, R.string.music_mine_local_tracks);
        bindCard(mBinding.musicMinePlaylists, R.string.music_mine_playlists);
        bindCard(mBinding.musicMineEmptyCard, R.string.music_mine_empty_message);
    }

    private void bindCard(View view, int messageRes) {
        view.setOnClickListener(clicked -> Toast.makeText(requireContext(), messageRes, Toast.LENGTH_SHORT).show());
        view.setOnTouchListener((pressedView, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                pressedView.animate().cancel();
                pressedView.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).start();
            } else if (event.getAction() == android.view.MotionEvent.ACTION_UP
                    || event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                pressedView.animate().cancel();
                pressedView.animate().scaleX(1f).scaleY(1f).setDuration(180)
                        .setInterpolator(new DecelerateInterpolator()).start();
            }
            return false;
        });
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_music_mine;
    }

    @Override
    public int initVariableId() {
        return BR.musicMineModel;
    }

    @Override
    public MusicMineViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(
                requireActivity().getApplication());
        return ViewModelProviders.of(this, factory).get(MusicMineViewModel.class);
    }
}
