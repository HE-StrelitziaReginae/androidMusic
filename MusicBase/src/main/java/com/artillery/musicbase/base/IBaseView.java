package com.artillery.musicbase.base;

/**
 * @author ArtilleryOrchid
 */
public interface IBaseView {
    /**
     * 初始化界面传递参数
     */
    void initParam();

    /**
     * 初始化Bar
     */
    void initBar();

    /**
     * 初始化数据
     */
    void initData();

    /**
     * 初始化界面观察者的监听
     */
    void initViewObservable();
}
