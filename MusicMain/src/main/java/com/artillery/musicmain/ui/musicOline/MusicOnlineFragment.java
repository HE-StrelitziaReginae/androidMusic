package com.artillery.musicmain.ui.musicOline;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.artillery.musicbase.base.BaseFragment;
import com.artillery.musicmain.BR;
import com.artillery.musicmain.R;
import com.artillery.musicmain.databinding.FragmentMusicOnlineBinding;
import com.artillery.musicmain.viewmodel.MusicOnlineViewModel;

/**
 * @author ArtilleryOrchid
 */
public class MusicOnlineFragment extends BaseFragment<FragmentMusicOnlineBinding, MusicOnlineViewModel> {
    private static class Holder {
        private static final MusicOnlineFragment instance = new MusicOnlineFragment();
    }

    public static MusicOnlineFragment getInstance() {
        return MusicOnlineFragment.Holder.instance;
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
