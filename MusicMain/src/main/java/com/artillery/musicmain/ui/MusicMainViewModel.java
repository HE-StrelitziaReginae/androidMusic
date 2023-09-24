package com.artillery.musicmain.ui;

import android.app.Application;

import androidx.annotation.NonNull;

import com.artillery.musicbase.base.BaseViewModel;
import com.artillery.musicmain.data.Repository;

/**
 * @author ArtilleryOrchid
 */
public class MusicMainViewModel extends BaseViewModel<Repository> {
    public MusicMainViewModel(@NonNull Application application, Repository model) {
        super(application, model);
    }
}
