package com.artillery.musicmain.ui.fragment.list;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.artillery.musicbase.base.MultiItemViewModel;
import com.artillery.musicbase.binding.command.BindingAction;
import com.artillery.musicbase.binding.command.BindingCommand;
import com.artillery.musicmain.data.MusicContext;
import com.artillery.musicmain.ui.music.MusicPlayActivity;
import com.artillery.musicmain.utils.AlbumUtils;
import com.artillery.musicservice.data.Song;

/**
 * @author ArtilleryOrchid
 */
public class MusicListItemViewModel extends MultiItemViewModel<MusicListFragmentViewModel> {
    public ObservableField<Bitmap> musicAlbum = new ObservableField<>();
    public ObservableField<String> musicName = new ObservableField<>();
    public ObservableField<String> musicArtist = new ObservableField<>();
    private Song mSong;

    public MusicListItemViewModel(@NonNull MusicListFragmentViewModel viewModel, Song song) {
        super(viewModel);
        mSong = song;
        musicAlbum.set(AlbumUtils.parseAlbum(song));
        musicName.set(song.getTitle());
        musicArtist.set(song.getArtist());
    }

    public BindingCommand musicPlay = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            Bundle bundle = new Bundle();
            bundle.putParcelable(MusicContext.MUSIC_PLAY_SONG, mSong);
            viewModel.startActivity(MusicPlayActivity.class, bundle);
        }
    });
}
