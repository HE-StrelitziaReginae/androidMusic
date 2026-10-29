package com.artillery.musicservice.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.artillery.musicservice.data.PlayList;
import com.artillery.musicservice.data.Song;
import com.artillery.musicservice.data.PlaybackState;

/**
 * @author ArtilleryOrchid
 */
@Database(entities = {Song.class, PlayList.class, PlaybackState.class}, version = 3,
        exportSchema = true)
@TypeConverters(MusicTypeConverters.class)
public abstract class MusicDataBase extends RoomDatabase {
    public abstract MusicDao musicDao();

    public abstract PlaybackStateDao playbackStateDao();
}
