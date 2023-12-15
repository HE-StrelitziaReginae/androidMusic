package com.artillery.musicmain.ui;

import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.artillery.musicbase.base.BaseActivity;
import com.artillery.musicmain.BR;
import com.artillery.musicmain.R;
import com.artillery.musicmain.databinding.ActivityMusicMainBinding;
import com.artillery.musicmain.ui.list.MusicListFragment;
import com.artillery.musicmain.ui.mine.MusicMineFragment;
import com.artillery.musicmain.ui.oline.MusicOnlineFragment;
import com.artillery.musicmain.viewmodel.MusicMainViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ArtilleryOrchid
 */
public class MusicMainActivity extends BaseActivity<ActivityMusicMainBinding, MusicMainViewModel> {
    private List<Fragment> mFragments;

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_music_main;
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
        binding.musicGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == binding.musicMain.getId()) {
                    commitAllowingStateLoss(0);
                } else if (checkedId == binding.musicOnline.getId()) {
                    commitAllowingStateLoss(1);
                } else if (checkedId == binding.musicMine.getId()) {
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
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(position + "");
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
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        for (int i = 0; i < mFragments.size(); i++) {
            Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(i + "");
            if (currentFragment != null) {
                transaction.hide(currentFragment);
            }
        }
        transaction.commitAllowingStateLoss();
    }
}