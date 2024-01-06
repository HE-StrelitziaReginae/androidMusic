package com.artillery.androidmusic;

import android.os.Bundle;

import com.artillery.music.BR;
import com.artillery.music.R;
import com.artillery.music.databinding.ActivityLauncherBinding;
import com.artillery.musicbase.base.BaseActivity;
import com.artillery.musicmain.ui.MusicMainFragment;

/**
 * @author ArtilleryOrchid
 */
public class LauncherActivity extends BaseActivity<ActivityLauncherBinding, LauncherViewModel> {
    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_launcher;
    }

    @Override
    public int initVariableId() {
        return BR.launcher;
    }

    @Override
    public void initData() {
        startContainerActivity(MusicMainFragment.class.getCanonicalName());
        finish();
    }
}
