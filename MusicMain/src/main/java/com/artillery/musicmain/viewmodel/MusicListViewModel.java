package com.artillery.musicmain.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;

import com.artillery.musicbase.base.BaseViewModel;
import com.artillery.musicmain.BR;
import com.artillery.musicmain.R;
import com.artillery.musicmain.data.MusicRepository;
import com.os.sys.musicskin.MusicSkin;
import com.artillery.musicmain.utils.LambadaTools;
import com.artillery.musicservice.data.Song;

import java.util.ArrayList;

import me.tatarka.bindingcollectionadapter2.ItemBinding;

/**
 * @author ArtilleryOrchid
 */
public class MusicListViewModel extends BaseViewModel<MusicRepository> {
    private MusicListItemViewModel selectedItem;
    private String selectedSongPath;

    public MusicListViewModel(@NonNull Application application, @NonNull MusicRepository repository) {
        super(application, repository);
    }

    @Nullable
    public ArrayList<Song> scanMusicList() {
        ArrayList<Song> music = model.scanMusicList();
        return music;
    }

    public void updateMusicList(ArrayList<Song> music) {
        if (music == null) {
            music = new ArrayList<>();
        }
        String selectedPath = selectedItem == null ? selectedSongPath : selectedItem.getSongPath();
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
        selectedSongPath = item == null ? null : item.getSongPath();
        if (item != null) {
            item.setSelected(true);
        }
    }

    public void selectSong(@Nullable Song song) {
        selectedSongPath = song == null ? null : song.getPath();
        MusicListItemViewModel matchingItem = null;
        if (selectedSongPath != null) {
            for (MusicListItemViewModel item : musicListItem) {
                if (selectedSongPath.equals(item.getSongPath())) {
                    matchingItem = item;
                    break;
                }
            }
        }
        if (matchingItem != null || song == null) {
            selectItem(matchingItem);
        } else {
            if (selectedItem != null) {
                selectedItem.setSelected(false);
                selectedItem = null;
            }
        }
    }

    public void applySkin(MusicSkin skin) {
        for (MusicListItemViewModel item : musicListItem) {
            item.applySkin(skin);
        }
    }
}
