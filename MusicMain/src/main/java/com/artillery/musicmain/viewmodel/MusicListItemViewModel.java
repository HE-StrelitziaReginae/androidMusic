package com.artillery.musicmain.viewmodel;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.artillery.musicbase.base.MultiItemViewModel;
import com.artillery.musicbase.binding.command.BindingAction;
import com.artillery.musicbase.binding.command.BindingCommand;
import com.artillery.musicmain.data.MusicDaraListener;
import com.artillery.musicmain.utils.AlbumUtils;
import com.artillery.musicservice.data.Song;

import java.util.ArrayList;

/**
 * @author ArtilleryOrchid
 */
public class MusicListItemViewModel extends MultiItemViewModel<MusicListViewModel> {
    public ObservableField<Bitmap> musicAlbum = new ObservableField<>();
    public ObservableField<String> musicName = new ObservableField<>();
    public ObservableField<String> musicArtist = new ObservableField<>();
    private final ArrayList<Song> mSongList = new ArrayList<>();
    private Song mSong;
    private int mStartIndex = 0;

    public MusicListItemViewModel(@NonNull MusicListViewModel viewModel, Song song, ArrayList<Song> music, int start) {
        super(viewModel);
        mSongList.addAll(music);
        mSong = song;
        mStartIndex = start;
        musicAlbum.set(AlbumUtils.parseAlbum(song));
        musicName.set(song.getTitle());
        musicArtist.set(song.getArtist());
    }

    public BindingCommand musicPlay = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            MusicDaraListener.getInstance().putMusicData(mSongList, mStartIndex, mSong);
        }
    });
}
