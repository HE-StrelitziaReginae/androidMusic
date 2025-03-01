package com.artillery.musicmain.ui.musicList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.artillery.musicbase.base.BaseFragment;
import com.artillery.musicmain.BR;
import com.artillery.musicmain.R;
import com.artillery.musicmain.databinding.FragmentMusicListBinding;
import com.artillery.musicmain.viewmodel.MusicListViewModel;

/**
 * @author ArtilleryOrchid
 */
public class MusicListFragment extends BaseFragment<FragmentMusicListBinding, MusicListViewModel> {
    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_music_list;
    }

    @Override
    public int initVariableId() {
        return BR.musicListModel;
    }

}