package com.artillery.musicservice.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.artillery.musicservice.data.PlaybackState;

@Dao
public interface PlaybackStateDao {
    @Query("SELECT * FROM playback_state WHERE id = 1 LIMIT 1")
    PlaybackState getPlaybackState();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void savePlaybackState(PlaybackState state);
}
