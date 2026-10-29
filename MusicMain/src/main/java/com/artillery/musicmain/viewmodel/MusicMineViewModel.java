package com.artillery.musicmain.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import com.artillery.musicbase.base.BaseViewModel;
import com.artillery.musicmain.data.MusicRepository;

/**
 * @author ArtilleryOrchid
 */
public class MusicMineViewModel extends BaseViewModel<MusicRepository> {
    public MusicMineViewModel(@NonNull Application application, @NonNull MusicRepository repository) {
        super(application, repository);
    }
}
