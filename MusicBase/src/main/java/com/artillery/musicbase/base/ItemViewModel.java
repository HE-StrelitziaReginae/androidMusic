package com.artillery.musicbase.base;


import androidx.annotation.NonNull;

/**
 * ItemViewModel
 *
 * @author ArtilleryOrchid
 */

public class ItemViewModel<VM extends BaseViewModel> {
    protected VM viewModel;

    public ItemViewModel(@NonNull VM viewModel) {
        this.viewModel = viewModel;
    }
}
