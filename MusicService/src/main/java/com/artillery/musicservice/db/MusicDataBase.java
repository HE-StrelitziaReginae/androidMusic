package com.artillery.musicservice.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.artillery.musicservice.data.Song;

/**
 * @author ArtilleryOrchid
 */
@Database(entities = {Song.class}, version = 1)
public abstract class MusicDataBase extends RoomDatabase {
    public abstract MusicDao musicDao();
}
