package com.artillery.musicmain.ui;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.artillery.musicbase.base.BaseActivity;
import com.artillery.musicbase.base.ViewModelFactory;
import com.artillery.musicmain.BR;
import com.artillery.musicmain.R;
import com.artillery.musicmain.databinding.ActivityMusicMainBinding;

/**
 * @author ArtilleryOrchid
 */
public class MusicMainActivity extends BaseActivity<ActivityMusicMainBinding, MusicMainViewModel> {

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_music_main;
    }

    @Override
    public int initVariableId() {
        return BR.musicModel;
    }

    @Override
    public MusicMainViewModel initViewModel() {
        ViewModelFactory factory = ViewModelFactory.getInstance(getApplication());
        return ViewModelProviders.of(this, factory).get(MusicMainViewModel.class);
    }
}