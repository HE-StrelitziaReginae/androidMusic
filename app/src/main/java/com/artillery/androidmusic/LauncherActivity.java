package com.artillery.androidmusic;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.artillery.music.BR;
import com.artillery.music.R;
import com.artillery.music.databinding.ActivityLauncherBinding;
import com.artillery.musicbase.base.BaseActivity;
import com.artillery.musicbase.utils.KLogUtils;
import com.artillery.musicmain.ui.MusicMainFragment;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.List;

/**
 * @author ArtilleryOrchid
 */
public class LauncherActivity extends BaseActivity<ActivityLauncherBinding, LauncherViewModel> {
    private final Handler mHandler = new Handler(Looper.getMainLooper());

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
        XXPermissions.with(this)
                .permission(Permission.WRITE_EXTERNAL_STORAGE, Permission.READ_MEDIA_IMAGES, Permission.READ_MEDIA_VIDEO, Permission.READ_MEDIA_AUDIO)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                        KLogUtils.i("onGranted: " + allGranted);
                        mHandler.postDelayed(() -> {
                            startContainerActivity(MusicMainFragment.class.getCanonicalName());
                            finish();
                        }, 2000);
                    }
                });
    }
}
