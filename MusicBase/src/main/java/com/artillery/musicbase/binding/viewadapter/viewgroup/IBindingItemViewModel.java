package com.artillery.musicbase.binding.viewadapter.viewgroup;

import androidx.databinding.ViewDataBinding;

/**
 * @author ArtilleryOrchid
 */
public interface IBindingItemViewModel<V extends ViewDataBinding> {
    void injectDataBinding(V binding);
}
