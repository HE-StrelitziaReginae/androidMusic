package com.artillery.musicmain.data.source.local;

import android.database.Observable;

import com.artillery.musicmain.data.source.MusicLocalSource;
import com.artillery.musicservice.data.PlayList;
import com.artillery.musicservice.data.Song;
import com.artillery.musicservice.db.MusicDataBase;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地数据源，可配合Room框架使用
 *
 * @author ArtilleryOrchid
 */
public class MusicLocalSourceImpl implements MusicLocalSource {
    private MusicDataBase mMusicDataBase;
    private volatile static MusicLocalSourceImpl INSTANCE = null;

    public static MusicLocalSourceImpl getInstance() {
        if (INSTANCE == null) {
            synchronized (MusicLocalSourceImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MusicLocalSourceImpl();
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    private MusicLocalSourceImpl() {
        //数据库Helper构建
        //mMusicDataBase = MusicDBHelper.getInstance().getMusicDataBase();
    }

    @Override
    public Observable<List<PlayList>> playLists() {
        return null;
    }

    @Override
    public List<PlayList> cachedPlayLists() {
        return null;
    }

    @Override
    public Observable<PlayList> create(PlayList playList) {
        return null;
    }

    @Override
    public Observable<PlayList> update(PlayList playList) {
        return null;
    }

    @Override
    public Observable<PlayList> delete(PlayList playList) {
        return null;
    }

    @Override
    public Observable<ArrayList<Song>> insert(ArrayList<Song> songs) {
        return null;
    }

    @Override
    public Observable<Song> update(Song song) {
        return null;
    }
}
