package com.artillery.musicservice.db;

import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.artillery.musicbase.utils.Utils;

/**
 * @author ArtilleryOrchid
 */
public class MusicDBHelper {
    private static volatile MusicDBHelper mInstance;
    private final MusicDataBase musicDataBase;

    private MusicDBHelper() {
        musicDataBase = Room.databaseBuilder(Utils.getContext(), MusicDataBase.class, "music")
                .addMigrations(new Migration(1, 2) {
                    @Override
                    public void migrate(SupportSQLiteDatabase database) {
                        database.execSQL("CREATE TABLE IF NOT EXISTS playback_state "
                                + "(id INTEGER NOT NULL, songPath TEXT, position INTEGER NOT NULL, "
                                + "playing INTEGER NOT NULL, PRIMARY KEY(id))");
                    }
                })
                .fallbackToDestructiveMigration()
                .build();
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
