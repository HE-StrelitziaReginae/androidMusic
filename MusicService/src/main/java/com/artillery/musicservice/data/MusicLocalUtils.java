package com.artillery.musicservice.data;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public final class MusicLocalUtils {
    private MusicLocalUtils() {
    }

    @Nullable
    public static ArrayList<Song> getMusic(Context context) {
        ArrayList<Song> songs = new ArrayList<>();
        String[] projection = {
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.MIME_TYPE
        };
        Cursor cursor;
        try {
            cursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    MediaStore.Audio.Media.IS_MUSIC + " != 0 OR "
                            + MediaStore.Audio.Media.MIME_TYPE + " LIKE ?",
                    new String[]{"audio/%"},
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        } catch (SecurityException error) {
            return null;
        }
        if (cursor == null) {
            return null;
        }
        try {
            while (cursor.moveToNext()) {
                String displayName = cursor.getString(cursor.getColumnIndexOrThrow(
                        MediaStore.Audio.Media.DISPLAY_NAME));
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(
                        MediaStore.Audio.Media.DATA));
                String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(
                        MediaStore.Audio.Media.MIME_TYPE));
                if (TextUtils.isEmpty(filePath) || TextUtils.isEmpty(displayName)
                        || !isAudioFile(displayName, mimeType)) {
                    continue;
                }

                int fileSize = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                if (fileSize <= 800 * 1000) {
                    continue;
                }

                Song song = new Song();
                song.setId(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
                song.setDisplayName(displayName);
                song.setPath(filePath);
                song.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
                song.setSize(fileSize);
                song.setAlbum(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));

                String[] parsedData = parseDisplayName(displayName);
                if (parsedData != null) {
                    song.setTitle(parsedData[0]);
                    song.setArtist(parsedData[1]);
                } else {
                    song.setTitle(displayName.trim());
                    song.setArtist(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
                }
                songs.add(song);
            }
        } finally {
            cursor.close();
        }
        return songs;
    }

    private static String[] parseDisplayName(String displayName) {
        if (displayName == null || !displayName.contains("-")) {
            return null;
        }
        String[] splitName = displayName.split("-", 2);
        if (splitName.length < 2) {
            return null;
        }
        String title = splitName[0].trim();
        String artist = splitName[1].trim().replaceAll("\\.[^.]+$", "");
        return new String[]{title, artist};
    }

    private static boolean isAudioFile(String displayName, String mimeType) {
        if (!TextUtils.isEmpty(mimeType) && mimeType.startsWith("audio/")) {
            return true;
        }
        String lowerName = displayName.toLowerCase(java.util.Locale.ROOT);
        return lowerName.endsWith(".mp3")
                || lowerName.endsWith(".flac")
                || lowerName.endsWith(".wav")
                || lowerName.endsWith(".m4a")
                || lowerName.endsWith(".aac")
                || lowerName.endsWith(".ogg")
                || lowerName.endsWith(".opus")
                || lowerName.endsWith(".wma");
    }
}
