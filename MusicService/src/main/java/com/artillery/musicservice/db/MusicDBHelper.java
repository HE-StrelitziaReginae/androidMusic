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
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS playback_state "
                    + "(id INTEGER NOT NULL, songPath TEXT, position INTEGER NOT NULL, "
                    + "playing INTEGER NOT NULL, PRIMARY KEY(id))");
        }
    };
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            if (hasTable(database, "Song")) {
                database.execSQL("ALTER TABLE `Song` RENAME TO `songs_legacy`");
                database.execSQL("CREATE TABLE `songs_new` ("
                        + "`id` INTEGER NOT NULL, `title` TEXT, `displayName` TEXT, "
                        + "`artist` TEXT, `album` TEXT, `path` TEXT, `duration` INTEGER NOT NULL, "
                        + "`size` INTEGER NOT NULL, `favorite` INTEGER NOT NULL, PRIMARY KEY(`id`))");
                database.execSQL("INSERT INTO `songs_new` "
                        + "(`id`, `title`, `displayName`, `artist`, `album`, `path`, "
                        + "`duration`, `size`, `favorite`) "
                        + "SELECT `id`, `title`, `displayName`, `artist`, `album`, `path`, "
                        + "`duration`, `size`, `favorite` FROM `songs_legacy`");
                database.execSQL("DROP TABLE `songs_legacy`");
                database.execSQL("ALTER TABLE `songs_new` RENAME TO `songs`");
            }
            database.execSQL("CREATE TABLE IF NOT EXISTS `playlists` ("
                    + "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, "
                    + "`numOfSongs` INTEGER NOT NULL, `favorite` INTEGER NOT NULL, "
                    + "`createdAt` INTEGER, `updatedAt` INTEGER, `songs` TEXT, "
                    + "`playingIndex` INTEGER NOT NULL, `playMode` TEXT)");
        }
    };
    private final MusicDataBase musicDataBase;

    private MusicDBHelper() {
        musicDataBase = Room.databaseBuilder(Utils.getContext(), MusicDataBase.class, "music")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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

    private static boolean hasTable(SupportSQLiteDatabase database, String tableName) {
        android.database.Cursor cursor = database.query(
                "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
                new Object[]{tableName});
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }
}
