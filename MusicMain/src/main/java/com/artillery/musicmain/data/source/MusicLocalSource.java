package com.artillery.musicmain.data.source;

import androidx.annotation.Nullable;

import com.artillery.musicservice.data.PlayList;
import com.artillery.musicservice.data.Song;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

/**
 * 本地存储
 *
 * @author ArtilleryOrchid
 */
public interface MusicLocalSource {
    Observable<List<PlayList>> playLists();

    Observable<PlayList> create(PlayList playList);

    Observable<PlayList> update(PlayList playList);

    Observable<PlayList> delete(PlayList playList);

    Observable<ArrayList<Song>> insert(ArrayList<Song> songs);

    Observable<Song> update(Song song);

    Observable<Song> delete(Song song);

    Observable<Song> setFavorite(Song song, boolean favorite);

    Observable<ArrayList<Song>> songs();

    Observable<ArrayList<Song>> favoriteSongs();

    @Nullable
    ArrayList<Song> scanMusicList();
}
