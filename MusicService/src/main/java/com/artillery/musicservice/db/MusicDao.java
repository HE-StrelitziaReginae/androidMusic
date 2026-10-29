package com.artillery.musicservice.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.artillery.musicservice.data.PlayList;
import com.artillery.musicservice.data.Song;

import java.util.List;

/**
 * @author ArtilleryOrchid
 */
@Dao
public interface MusicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSongs(List<Song> songs);

    @Update
    int updateSong(Song song);

    @Delete
    int deleteSong(Song song);

    @Query("SELECT * FROM songs ORDER BY title COLLATE NOCASE, artist COLLATE NOCASE")
    List<Song> getSongs();

    @Query("SELECT * FROM songs WHERE favorite = 1 ORDER BY title COLLATE NOCASE, artist COLLATE NOCASE")
    List<Song> getFavoriteSongs();

    @Query("DELETE FROM songs")
    int deleteAllSongs();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPlaylist(PlayList playList);

    @Update
    int updatePlaylist(PlayList playList);

    @Delete
    int deletePlaylist(PlayList playList);

    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC, createdAt DESC, name COLLATE NOCASE")
    List<PlayList> getPlaylists();

}
