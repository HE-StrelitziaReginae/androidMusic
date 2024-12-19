package com.artillery.musicbase.base;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.appcompat.app.AppCompatDialog;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

public abstract class BaseDialog<V extends ViewDataBinding> extends AppCompatDialog implements IBaseView {
    protected V mBinding;

    public BaseDialog(Context context) {
        super(context);
        initParam();
    }

    public BaseDialog(Context context, int theme) {
        super(context, theme);
        initParam();
    }

    public BaseDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        initParam();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //私有的初始化DataBinding
        initViewDataBinding();
        //页面数据初始化方法
        initData();
        //页面事件监听的方法，一般用于ViewModel层转到View层的事件注册
        initViewObservable();
    }

    /**
     * 注入绑定
     */
    private void initViewDataBinding() {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), initContentView(), null, false);
        setContentView(mBinding.getRoot());
    }

    @Override
    public void initParam() {

    }

    @Override
    public void initData() {

    }

    @Override
    public void initViewObservable() {

    }

    /**
     * 初始化根布局
     *
     * @return 布局layout的id
     */
    public abstract int initContentView();

}
