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
import com.artillery.musicmain.theme.MusicSkin;
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
        applySkin(MusicSkin.load(requireContext()));
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
        if (mMusicSkin != null) {
            applySkin(mMusicSkin);
        }
    }

    private MusicSkin mMusicSkin;

    public void applySkin(MusicSkin skin) {
        mMusicSkin = skin;
        if (mBinding == null) {
            return;
        }
        mBinding.musicList.setBackgroundColor(skin.background());
        mBinding.musicList.post(() -> {
            for (int index = 0; index < mBinding.musicList.getChildCount(); index++) {
                android.view.View item = mBinding.musicList.getChildAt(index);
                item.setBackgroundColor(skin.surface());
                android.widget.TextView title = item.findViewById(R.id.music_name);
                android.widget.TextView artist = item.findViewById(R.id.music_artist);
                if (title != null) {
                    title.setTextColor(skin.primaryText());
                }
                if (artist != null) {
                    artist.setTextColor(skin.secondaryText());
                }
            }
        });
    }

}
