package com.artillery.musicmain.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import com.artillery.musicbase.base.BaseViewModel;
import com.artillery.musicbase.binding.command.BindingCommand;
import com.artillery.musicmain.data.MusicRepository;

/**
 * @author ArtilleryOrchid
 */
public class MusicPlayViewModel extends BaseViewModel<MusicRepository> {
    public BindingCommand pre;
    public BindingCommand play;
    public BindingCommand next;
    public MusicRepository mMusicRepository;

    public MusicPlayViewModel(@NonNull Application application, MusicRepository model) {
        super(application, model);
        model.bindMusicService();
        mMusicRepository = model;
    }
}
