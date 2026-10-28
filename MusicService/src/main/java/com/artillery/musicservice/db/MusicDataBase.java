package com.artillery.musicservice.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.artillery.musicservice.data.Song;
import com.artillery.musicservice.data.PlaybackState;

/**
 * @author ArtilleryOrchid
 */
@Database(entities = {Song.class, PlaybackState.class}, version = 2)
public abstract class MusicDataBase extends RoomDatabase {
    public abstract MusicDao musicDao();
}
