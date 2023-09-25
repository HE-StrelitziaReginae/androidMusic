package com.artillery.musicservice.db;

import androidx.room.Room;

import com.artillery.musicbase.utils.Utils;

/**
 * @author ArtilleryOrchid
 */
public class MusicDBHelper {
    private static volatile MusicDBHelper mInstance;
    private final MusicDataBase musicDataBase;

    private MusicDBHelper() {
        musicDataBase = Room.databaseBuilder(Utils.getContext(), MusicDataBase.class, "music").build();
    }

    public static MusicDBHelper getInstance() {
        if (mInstance == null) {
            synchronized (MusicDBHelper.class) {
                if (mInstance == null) {
                    mInstance = new MusicDBHelper();
                }
            }
        }
        return mInstance;
    }

    public MusicDataBase getMusicDataBase() {
        return musicDataBase;
    }

    public void close() {
        if (musicDataBase != null && musicDataBase.isOpen()) {
            musicDataBase.close();
        }
    }
}
