package com.artillery.musicmain.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.artillery.musicbase.base.BaseViewModel;
import com.artillery.musicbase.binding.command.BindingCommand;
import com.artillery.musicmain.R;
import com.artillery.musicmain.data.MusicRepository;
import com.artillery.musicmain.ui.MusicMainFragment;
import com.artillery.musicmain.ui.musicList.MusicListFragment;
import com.artillery.musicmain.ui.musicMine.MusicMineFragment;
import com.artillery.musicmain.ui.musicOline.MusicOnlineFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ArtilleryOrchid
 */
public class MusicMainViewModel extends BaseViewModel {
    public BindingCommand mPre;
    public BindingCommand mPlay;
    public BindingCommand mNext;
    public MusicRepository mMusicRepository;
    private List<Fragment> mFragments;

    public MusicMainViewModel(@NonNull Application application, MusicRepository mode) {
        super(application);
        mMusicRepository = mode;
        mMusicRepository.bindMusicService();
        initFragment();
    }

    public void binMusicView(MusicMainFragment musicPlayFragment) {
        mMusicRepository.bindMusicView(musicPlayFragment);
    }

    private void initFragment() {
        mFragments = new ArrayList<>();
        mFragments.add(new MusicListFragment());
        mFragments.add(new MusicOnlineFragment());
        mFragments.add(new MusicMineFragment());
    }

    /**
     * show fragment
     */
    public void showFragment(FragmentActivity fragmentActivity, int position) {
        hideAllFragment(fragmentActivity);
        FragmentTransaction transaction = fragmentActivity.getSupportFragmentManager().beginTransaction();
        Fragment currentFragment = fragmentActivity.getSupportFragmentManager().findFragmentByTag(position + "");
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
    private void hideAllFragment(FragmentActivity fragmentActivity) {
        FragmentTransaction transaction = fragmentActivity.getSupportFragmentManager().beginTransaction();
        for (int i = 0; i < mFragments.size(); i++) {
            Fragment currentFragment = fragmentActivity.getSupportFragmentManager().findFragmentByTag(i + "");
            if (currentFragment != null) {
                transaction.hide(currentFragment);
            }
        }
        transaction.commitAllowingStateLoss();
    }
}
