package com.artillery.musicmain.ui.musicList;

import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.artillery.musicbase.base.BaseFragment;
import com.artillery.musicmain.BR;
import com.artillery.musicmain.R;
import com.artillery.musicmain.data.MusicDaraListener;
import com.artillery.musicmain.databinding.FragmentMusicListBinding;
import com.artillery.musicmain.viewmodel.MusicListViewModel;

/**
 * @author ArtilleryOrchid
 */
public class MusicListFragment extends BaseFragment<FragmentMusicListBinding, MusicListViewModel> {
    private final ContentObserver mMusicObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            refreshMusicList();
        }
    };

    private boolean mObserverRegistered;

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_music_list;
    }

    @Override
    public int initVariableId() {
        return BR.musicListModel;
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshMusicList();
        if (!mObserverRegistered) {
            requireContext().getContentResolver().registerContentObserver(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, mMusicObserver);
            mObserverRegistered = true;
        }
    }

    @Override
    public void onStop() {
        if (mObserverRegistered) {
            requireContext().getContentResolver().unregisterContentObserver(mMusicObserver);
            mObserverRegistered = false;
        }
        super.onStop();
    }

    public void refreshMusicList() {
        if (mViewModel == null || !isAdded() || getView() == null) {
            return;
        }
        MusicDaraListener.getInstance().updateMusicList(mViewModel.refreshMusicList());
    }

}
