package com.artillery.musicmain.ui.musicOline;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.artillery.musicbase.base.BaseFragment;
import com.artillery.musicmain.BR;
import com.artillery.musicmain.R;
import com.artillery.musicmain.databinding.FragmentMusicOnlineBinding;
import com.os.sys.musicskin.MusicSkin;
import com.artillery.musicmain.viewmodel.MusicOnlineViewModel;

/**
 * @author ArtilleryOrchid
 */
public class MusicOnlineFragment extends BaseFragment<FragmentMusicOnlineBinding, MusicOnlineViewModel> {
    private MusicSkin mMusicSkin;

    @Override
    public void onStart() {
        super.onStart();
        applySkin(MusicSkin.load(requireContext()));
    }

    public void applySkin(MusicSkin skin) {
        if (mBinding == null) {
            return;
        }
        mMusicSkin = skin;
        mViewModel.applySkin(skin);
        mBinding.onlineRoot.setBackgroundColor(skin.background(requireContext()));
        mBinding.onlineList.setBackgroundColor(skin.background(requireContext()));
        boolean characterSkin = skin.isCharacterSkin();
        mBinding.onlineSkinBanner.setVisibility(characterSkin ? View.VISIBLE : View.GONE);
        if (characterSkin) {
            mBinding.onlineSkinBannerArt.setImageResource(skin.keyArt());
            mBinding.onlineSkinBanner.setBackground(skin.featureBackground(requireContext()));
            mBinding.onlineSkinBannerEyebrow.setText(skin.onlineEyebrow());
            mBinding.onlineSkinBannerTitle.setText(skin.onlineTitle());
            mBinding.onlineSkinBannerSubtitle.setText(skin.onlineSubtitle());
            mBinding.onlineSkinBannerEyebrow.setTextColor(skin.headerSecondaryText(requireContext()));
            mBinding.onlineSkinBannerTitle.setTextColor(skin.headerPrimaryText(requireContext()));
            mBinding.onlineSkinBannerSubtitle.setTextColor(skin.headerSecondaryText(requireContext()));
        }
        mBinding.onlineEmpty.setTextColor(skin.secondaryText(requireContext()));
        mBinding.onlineSearchInput.setTextColor(skin.primaryText(requireContext()));
        mBinding.onlineSearchInput.setHintTextColor(skin.secondaryText(requireContext()));
        mBinding.onlineSearchBar.setBackground(roundBackground(skin.surface(requireContext()), 14));
        mBinding.onlineCategoryGroup.setBackground(roundBackground(skin.surface(requireContext()), 12));
        mBinding.onlineSearchLeading.setColorFilter(skin.icon(requireContext()));
        mBinding.onlineSearchButton.setColorFilter(skin.icon(requireContext()));
        mBinding.onlineClearButton.setColorFilter(skin.secondaryText(requireContext()));
        mBinding.onlineEmptyIcon.setColorFilter(skin.icon(requireContext()));
        updateCategoryStyles(skin);
    }

    @Override
    public void initViewObservable() {
        mBinding.onlineSearchButton.setOnClickListener(view -> search());
        mBinding.onlineClearButton.setOnClickListener(view -> clearSearch());
        mBinding.onlineSearchInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search();
                return true;
            }
            return false;
        });
        mBinding.onlineSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                mBinding.onlineClearButton.setVisibility(text == null || text.length() == 0
                        ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mBinding.onlineCategoryGroup.setOnCheckedChangeListener((group, checkedId) -> {
            updateCategoryStyles(mMusicSkin == null ? MusicSkin.load(requireContext()) : mMusicSkin);
            if (hasQuery()) {
                search();
            }
        });
        updateEmptyState(true, false);
    }

    private void search() {
        String query = mBinding.onlineSearchInput.getText().toString().trim();
        if (query.isEmpty()) {
            mViewModel.clear();
            updateEmptyState(true, false);
            return;
        }
        mViewModel.clear();
        mBinding.onlineLoading.setVisibility(View.VISIBLE);
        mBinding.onlineEmptyState.setVisibility(View.GONE);
        int category = selectedCategory();
        mViewModel.search(query, category, new MusicOnlineViewModel.SearchCallback() {
            @Override
            public void onSuccess(boolean empty) {
                mBinding.onlineLoading.setVisibility(View.GONE);
                updateEmptyState(empty, empty);
            }

            @Override
            public void onError(Throwable error) {
                mBinding.onlineLoading.setVisibility(View.GONE);
                updateEmptyState(true, true);
                Toast.makeText(requireContext(), R.string.music_online_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearSearch() {
        mBinding.onlineSearchInput.setText("");
        mViewModel.clear();
        updateEmptyState(true, false);
    }

    private boolean hasQuery() {
        return mBinding.onlineSearchInput.getText() != null
                && mBinding.onlineSearchInput.getText().toString().trim().length() > 0;
    }

    private int selectedCategory() {
        int checkedId = mBinding.onlineCategoryGroup.getCheckedRadioButtonId();
        if (checkedId == mBinding.onlineCategoryWestern.getId()) {
            return 1;
        }
        if (checkedId == mBinding.onlineCategoryJapanKorea.getId()) {
            return 2;
        }
        if (checkedId == mBinding.onlineCategoryOther.getId()) {
            return 3;
        }
        return 0;
    }

    private void updateEmptyState(boolean visible, boolean noResult) {
        mBinding.onlineEmpty.setText(noResult ? R.string.music_online_no_result : R.string.music_online_empty);
        mBinding.onlineEmptyState.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void updateCategoryStyles(MusicSkin skin) {
        int selectedBackground = skin.header(requireContext());
        int selectedText = skin.headerPrimaryText(requireContext());
        int normalText = skin.secondaryText(requireContext());
        updateCategory(mBinding.onlineCategoryChinese, selectedBackground, selectedText, normalText);
        updateCategory(mBinding.onlineCategoryWestern, selectedBackground, selectedText, normalText);
        updateCategory(mBinding.onlineCategoryJapanKorea, selectedBackground, selectedText, normalText);
        updateCategory(mBinding.onlineCategoryOther, selectedBackground, selectedText, normalText);
    }

    private void updateCategory(android.widget.RadioButton button, int selectedBackground,
                                int selectedText, int normalText) {
        button.setTextColor(button.isChecked() ? selectedText : normalText);
        button.setBackground(roundBackground(button.isChecked() ? selectedBackground : android.graphics.Color.TRANSPARENT, 10));
    }

    private GradientDrawable roundBackground(int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radiusDp * getResources().getDisplayMetrics().density);
        return drawable;
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_music_online;
    }

    @Override
    public int initVariableId() {
        return BR.musicOnlineModel;
    }
}
