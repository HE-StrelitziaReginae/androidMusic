package com.artillery.musicmain.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;

import com.artillery.musicbase.base.BaseViewModel;
import com.artillery.musicmain.BR;
import com.artillery.musicmain.R;
import com.artillery.musicmain.data.MusicRepository;
import com.os.sys.musicskin.MusicSkin;
import com.artillery.musicmain.utils.LambadaTools;
import com.artillery.musicservice.data.MusicLocalUtils;
import com.artillery.musicservice.data.Song;

import java.util.ArrayList;

import me.tatarka.bindingcollectionadapter2.ItemBinding;

/**
 * @author ArtilleryOrchid
 */
public class MusicListViewModel extends BaseViewModel<MusicRepository> {
    private MusicListItemViewModel selectedItem;

    public MusicListViewModel(@NonNull Application application) {
        super(application);
    }

    public ArrayList<Song> scanMusicList() {
        ArrayList<Song> music = MusicLocalUtils.getInstance().getMusic(getApplication());
        if (music == null) {
            music = new ArrayList<>();
        }
        return music;
    }

    public void updateMusicList(ArrayList<Song> music) {
        if (music == null) {
            music = new ArrayList<>();
        }
        String selectedPath = selectedItem == null ? null : selectedItem.getSongPath();
        musicListItem.clear();
        selectedItem = null;
        ArrayList<Song> currentMusic = new ArrayList<>(music);
        currentMusic.forEach(LambadaTools.forEachWithIndex((song, start) -> {
            MusicListItemViewModel item = new MusicListItemViewModel(this, song, currentMusic, start);
            musicListItem.add(item);
            if (selectedPath != null && selectedPath.equals(song.getPath())) {
                selectItem(item);
            }
        }));
    }

    public ObservableList<MusicListItemViewModel> musicListItem = new ObservableArrayList<>();
    public ItemBinding<MusicListItemViewModel> musicListBinding = ItemBinding.of(BR.musicListItemModel, R.layout.music_list_item);

    public void selectItem(MusicListItemViewModel item) {
        if (selectedItem != null && selectedItem != item) {
            selectedItem.setSelected(false);
        }
        selectedItem = item;
        if (item != null) {
            item.setSelected(true);
        }
    }

    public void applySkin(MusicSkin skin) {
        for (MusicListItemViewModel item : musicListItem) {
            item.applySkin(skin);
        }
    }
}
