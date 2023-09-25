package com.artillery.musicmain.data.source;

import android.database.Observable;

import com.artillery.musicservice.data.PlayList;
import com.artillery.musicservice.data.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地存储
 *
 * @author ArtilleryOrchid
 */
public interface MusicLocalSource {

    // Play List
    Observable<List<PlayList>> playLists();

    List<PlayList> cachedPlayLists();

    Observable<PlayList> create(PlayList playList);

    Observable<PlayList> update(PlayList playList);

    Observable<PlayList> delete(PlayList playList);

    // Song

    Observable<ArrayList<Song>> insert(ArrayList<Song> songs);

    Observable<Song> update(Song song);
}
