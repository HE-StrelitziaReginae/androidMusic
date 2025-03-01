package com.artillery.musicmain.viewmodel;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.artillery.musicbase.base.MultiItemViewModel;
import com.artillery.musicbase.binding.command.BindingAction;
import com.artillery.musicbase.binding.command.BindingCommand;
import com.artillery.musicmain.data.MusicDaraListener;
import com.artillery.musicmain.ui.MusicMainDialog;
import com.artillery.musicservice.data.Song;

import java.util.ArrayList;

/**
 * @author ArtilleryOrchid
 */
public class MusicListItemViewModel extends MultiItemViewModel<MusicListViewModel> {

    //暂无专辑图片
    //public ObservableField<Bitmap> musicAlbum = new ObservableField<>();
    public ObservableField<String> musicName = new ObservableField<>();
    public ObservableField<String> musicArtist = new ObservableField<>();
    private final ArrayList<Song> mSongList = new ArrayList<>();
    private final Song mSong;
    private final int mStartIndex;

    public MusicListItemViewModel(@NonNull MusicListViewModel viewModel, Song song, ArrayList<Song> music, int start) {
        super(viewModel);
        mSongList.addAll(music);
        mSong = song;
        mStartIndex = start;
        musicName.set(song.getTitle());
        musicArtist.set(song.getArtist());
    }

    public BindingCommand musicPlay = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            MusicMainDialog.getInstance().show();
            MusicDaraListener.getInstance().observerMusicData(mSongList, mStartIndex, mSong);
        }
    });
}
