package com.artillery.musicbase.base;

import android.annotation.SuppressLint;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author ArtilleryOrchid
 */
public class ViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    @SuppressLint("StaticFieldLeak")
    private static volatile ViewModelFactory INSTANCE;

    private final Application mApplication;

    public static ViewModelFactory getInstance(Application application) {

        if (INSTANCE == null) {
            synchronized (ViewModelFactory.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ViewModelFactory(application);
                }
            }
        }
        return INSTANCE;
    }

    protected ViewModelFactory(Application application) {
        mApplication = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(BaseViewModel.class)) {
            return (T) new BaseViewModel(mApplication);
        }
        //反射动态实例化ViewModel
        try {
            String className = modelClass.getCanonicalName();
            Class<?> classViewModel = Class.forName(className);
            Constructor<?> cons = classViewModel.getConstructor(Application.class);
            ViewModel viewModel = (ViewModel) cons.newInstance(mApplication);
            return (T) viewModel;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
