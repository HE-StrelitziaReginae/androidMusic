package com.artillery.musicmain.ui.fragment.list;

import android.app.Application;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;

import com.artillery.musicbase.base.BaseViewModel;
import com.artillery.musicbase.utils.Utils;
import com.artillery.musicmain.BR;
import com.artillery.musicmain.R;
import com.artillery.musicmain.utils.LambadaTools;
import com.artillery.musicservice.data.MusicLocalUtils;
import com.artillery.musicservice.data.Song;

import java.util.ArrayList;

import me.tatarka.bindingcollectionadapter2.ItemBinding;

/**
 * @author ArtilleryOrchid
 */
public class MusicListFragmentViewModel extends BaseViewModel {

    @RequiresApi(api = Build.VERSION_CODES.N)
    public MusicListFragmentViewModel(@NonNull Application application) {
        super(application);
        MusicLocalUtils instance = MusicLocalUtils.getInstance();
        ArrayList<Song> music = instance.getMusic(Utils.getContext());
        music.forEach(LambadaTools.forEachWithIndex((song, start) -> {
            musicListItem.add(new MusicListItemViewModel(this, song, music, start));
        }));
    }

    public ObservableList<MusicListItemViewModel> musicListItem = new ObservableArrayList<>();
    public ItemBinding<MusicListItemViewModel> musicListBinding = ItemBinding.of(BR.musicListItemModel, R.layout.music_list_item);
}
