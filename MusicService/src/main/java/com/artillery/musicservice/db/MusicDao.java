package com.artillery.musicservice.db;

import android.database.Observable;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.artillery.musicservice.data.Song;

import java.util.ArrayList;

/**
 * @author ArtilleryOrchid
 */
@Dao
public interface MusicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMusic(ArrayList<Song> song);

    @Query("select * from song")
    Observable<ArrayList<Song>> getMusic();

    @Query("delete from song where title=:title")
    void deleteMusic(String title);
}
