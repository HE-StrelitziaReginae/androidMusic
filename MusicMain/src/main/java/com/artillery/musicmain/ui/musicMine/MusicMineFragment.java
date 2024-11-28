package com.artillery.musicmain.ui.musicMine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.artillery.musicbase.base.BaseFragment;
import com.artillery.musicmain.BR;
import com.artillery.musicmain.R;
import com.artillery.musicmain.databinding.FragmentMusicMineBinding;
import com.artillery.musicmain.ui.musicList.MusicListFragment;
import com.artillery.musicmain.viewmodel.MusicMineViewModel;

/**
 * @author ArtilleryOrchid
 */
public class MusicMineFragment extends BaseFragment<FragmentMusicMineBinding, MusicMineViewModel> {
    private static class Holder {
        private static final MusicMineFragment instance = new MusicMineFragment();
    }

    public static MusicMineFragment getInstance() {
        return MusicMineFragment.Holder.instance;
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
