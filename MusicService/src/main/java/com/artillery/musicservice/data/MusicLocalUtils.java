package com.artillery.musicservice.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;

/**
 * @author ArtilleryOrchid
 */
public class MusicLocalUtils {
    private static volatile MusicLocalUtils mInstance;

    private static ArrayList<Song> list;

    private static Song song;
    private static String name;
    private static String artist;
    private static String path;
    private static long duration;
    private static int size;
    private static String album;
    private static int id;
    //获取专辑封面的Uri
    private Uri albumArtUri;

    public static MusicLocalUtils getInstance() {
        if (mInstance == null) {
            synchronized (MusicLocalUtils.class) {
                if (mInstance == null) {
                    mInstance = new MusicLocalUtils();
                }
            }
        }
        return mInstance;
    }

    public ArrayList<Song> getMusic(Context context) {
        list = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                , null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                song = new Song();
                name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                //把歌曲名字和歌手切割开
                song.setArtist(artist);
                song.setPath(path);
                song.setDuration(duration);
                song.setSize(size);
                song.setId(id);
                song.setAlbum(album);
                if (size > 1000 * 800) {
                    if (name.contains("-")) {
                        String[] str = name.split("-");
                        artist = str[0];
                        song.setArtist(artist);
                        name = str[1];
                        song.setTitle(name);
                    } else {
                        song.setTitle(name);
                    }
                    list.add(song);
                }
            }
        }
        cursor.close();
        return list;
    }
}
