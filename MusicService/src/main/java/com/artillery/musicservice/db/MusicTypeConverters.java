package com.artillery.musicservice.db;

import androidx.room.TypeConverter;

import com.artillery.musicservice.data.Song;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

public final class MusicTypeConverters {
    private static final Gson GSON = new Gson();
    private static final Type SONG_LIST_TYPE = new TypeToken<ArrayList<Song>>() {
    }.getType();

    private MusicTypeConverters() {
    }

    @TypeConverter
    public static Long fromDate(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static String fromSongs(ArrayList<Song> songs) {
        return songs == null ? null : GSON.toJson(songs);
    }

    @TypeConverter
    public static ArrayList<Song> toSongs(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<>();
        }
        ArrayList<Song> songs = GSON.fromJson(value, SONG_LIST_TYPE);
        return songs == null ? new ArrayList<>() : songs;
    }
}
