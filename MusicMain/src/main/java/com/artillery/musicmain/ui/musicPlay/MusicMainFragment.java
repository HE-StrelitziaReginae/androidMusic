package com.artillery.musicmain.ui.musicPlay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.artillery.musicbase.base.BaseFragment;
import com.artillery.musicmain.BR;
import com.artillery.musicmain.R;
import com.artillery.musicmain.databinding.FragmentMusicMainBinding;
import com.artillery.musicmain.ui.musicList.MusicListFragment;
import com.artillery.musicmain.ui.musicMine.MusicMineFragment;
import com.artillery.musicmain.ui.musicOline.MusicOnlineFragment;
import com.artillery.musicmain.viewmodel.MusicMainViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ArtilleryOrchid
 */
public class MusicMainFragment extends BaseFragment<FragmentMusicMainBinding, MusicMainViewModel> {
    private List<Fragment> mFragments;

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_music_main;
    }

    @Override
    public int initVariableId() {
        return BR.musicMain;
    }

    @Override
    public void initData() {
        mFragments = new ArrayList<>();
        mFragments.add(new MusicListFragment());
        mFragments.add(new MusicOnlineFragment());
        mFragments.add(new MusicMineFragment());
        commitAllowingStateLoss(0);
    }

    @Override
    public void initViewObservable() {
        mBinding.musicGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == mBinding.musicMain.getId()) {
                    commitAllowingStateLoss(0);
                } else if (checkedId == mBinding.musicOnline.getId()) {
                    commitAllowingStateLoss(1);
                } else if (checkedId == mBinding.musicMine.getId()) {
                    commitAllowingStateLoss(2);
                }
            }
        });
    }

    /**
     * show fragment
     *
     * @param position
     */
    private void commitAllowingStateLoss(int position) {
        hideAllFragment();
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        Fragment currentFragment = requireActivity().getSupportFragmentManager().findFragmentByTag(position + "");
        if (currentFragment != null) {
            transaction.show(currentFragment);
        } else {
            currentFragment = mFragments.get(position);
            transaction.add(R.id.music_frame, currentFragment, position + "");
        }
        transaction.commitAllowingStateLoss();
    }

    /**
     * hide fragment
     */
    private void hideAllFragment() {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        for (int i = 0; i < mFragments.size(); i++) {
            Fragment currentFragment = requireActivity().getSupportFragmentManager().findFragmentByTag(i + "");
            if (currentFragment != null) {
                transaction.hide(currentFragment);
            }
        }
        transaction.commitAllowingStateLoss();
    }
}