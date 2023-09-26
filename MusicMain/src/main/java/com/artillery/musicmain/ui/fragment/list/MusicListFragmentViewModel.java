package com.artillery.musicmain.ui.fragment.list;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;

import com.artillery.musicbase.base.BaseViewModel;
import com.artillery.musicbase.utils.Utils;
import com.artillery.musicmain.BR;
import com.artillery.musicmain.R;
import com.artillery.musicservice.data.MusicLocalUtils;
import com.artillery.musicservice.data.Song;

import java.util.ArrayList;

import me.tatarka.bindingcollectionadapter2.ItemBinding;
import me.tatarka.bindingcollectionadapter2.OnItemBind;

/**
 * @author ArtilleryOrchid
 */
public class MusicListFragmentViewModel extends BaseViewModel {

    public MusicListFragmentViewModel(@NonNull Application application) {
        super(application);
        MusicLocalUtils instance = MusicLocalUtils.getInstance();
        ArrayList<Song> music = instance.getMusic(Utils.getContext());
        for (Song song : music) {
            musicListItem.add(new MusicListItemViewModel(this, song));
        }
    }

    public ObservableList<MusicListItemViewModel> musicListItem = new ObservableArrayList<>();
    //RecyclerView多布局添加ItemBinding
    public ItemBinding<MusicListItemViewModel> musicListBinding = ItemBinding.of(BR.musicListItemModel, R.layout.music_list_item);
}
