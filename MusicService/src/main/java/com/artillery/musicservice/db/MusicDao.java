package com.artillery.musicservice.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.artillery.musicservice.data.Song;
import com.artillery.musicservice.data.PlaybackState;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ArtilleryOrchid
 */
@Dao
public interface MusicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMusic(List<Song> song);

    @Query("select * from song")
    List<Song> getMusic();

    @Query("delete from song where title=:title")
    void deleteMusic(String title);

    @Query("select * from playback_state where id=1 limit 1")
    PlaybackState getPlaybackState();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void savePlaybackState(PlaybackState state);
}
