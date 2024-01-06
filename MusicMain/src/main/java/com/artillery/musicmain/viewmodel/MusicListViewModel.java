package com.artillery.musicmain.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;

import com.artillery.musicbase.base.BaseViewModel;
import com.artillery.musicmain.BR;
import com.artillery.musicmain.R;
import com.artillery.musicmain.data.MusicRepository;
import com.artillery.musicmain.utils.LambadaTools;
import com.artillery.musicservice.data.MusicLocalUtils;
import com.artillery.musicservice.data.Song;

import java.util.ArrayList;

import me.tatarka.bindingcollectionadapter2.ItemBinding;

/**
 * @author ArtilleryOrchid
 */
public class MusicListViewModel extends BaseViewModel<MusicRepository> {

    public MusicListViewModel(@NonNull Application application) {
        super(application);
        MusicLocalUtils instance = MusicLocalUtils.getInstance();
        ArrayList<Song> music = instance.getMusic(getApplication());
        music.forEach(LambadaTools.forEachWithIndex((song, start) -> {
            musicListItem.add(new MusicListItemViewModel(this, song, music, start));
        }));
    }

    public ObservableList<MusicListItemViewModel> musicListItem = new ObservableArrayList<>();
    public ItemBinding<MusicListItemViewModel> musicListBinding = ItemBinding.of(BR.musicListItemModel, R.layout.music_list_item);
}
