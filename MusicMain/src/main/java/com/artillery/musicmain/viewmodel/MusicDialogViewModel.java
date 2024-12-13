package com.artillery.musicmain.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import com.artillery.musicbase.base.BaseViewModel;
import com.artillery.musicbase.binding.command.BindingCommand;

public class MusicDialogViewModel extends BaseViewModel {
    public BindingCommand mPre;
    public BindingCommand mPlay;
    public BindingCommand mNext;

    public MusicDialogViewModel(@NonNull Application application) {
        super(application);
    }
}
