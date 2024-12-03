package com.artillery.musicservice.data;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;

/**
 * @author ArtilleryOrchid
 */
public class MusicLocalUtils {
    private static volatile MusicLocalUtils mInstance;

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
        ArrayList<Song> list = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                , null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Song song = new Song();
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                int size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
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
        assert cursor != null;
        cursor.close();
        return list;
    }
}
