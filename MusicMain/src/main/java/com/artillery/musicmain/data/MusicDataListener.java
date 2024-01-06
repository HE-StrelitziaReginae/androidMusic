package com.artillery.musicmain.data;

import com.artillery.musicservice.data.Song;

import java.util.ArrayList;

/**
 * @author ArtilleryOrchid
 */
public interface MusicDataListener {
    void sendMusicList(ArrayList<Song> songArrayList, int index);

    void sendMusicSong(Song song);
}
