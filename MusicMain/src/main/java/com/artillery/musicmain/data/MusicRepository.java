package com.artillery.musicmain.data;

import android.database.Observable;

import androidx.annotation.NonNull;

import com.artillery.musicbase.base.BaseModel;
import com.artillery.musicmain.data.source.MusicHttpSource;
import com.artillery.musicmain.data.source.MusicLocalSource;
import com.artillery.musicmain.data.source.MusicPlaySource;
import com.artillery.musicmain.data.source.contract.MusicPlayContractImpl;
import com.artillery.musicmain.data.source.contract.view.MusicPlayView;
import com.artillery.musicmain.data.source.local.MusicLocalSourceImpl;
import com.artillery.musicservice.data.PlayList;
import com.artillery.musicservice.data.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * MVVM的Model层，统一模块的数据仓库，包含网络数据和本地数据（一个应用可以有多个Repository）
 *
 * @author ArtilleryOrchid
 */
public class MusicRepository extends BaseModel implements MusicHttpSource, MusicLocalSource, MusicPlaySource {
    private volatile static MusicRepository INSTANCE = null;
//    private final MusicHttpSource mHttpDataSource;
    private final MusicLocalSource mLocalDataSource;
    private final MusicPlaySource mMusicPlaySource;
    private MusicRepository(@NonNull MusicLocalSource localDataSource, @NonNull MusicPlaySource musicPlaySource) {
//        mHttpDataSource = httpDataSource;
        mLocalDataSource = localDataSource;
        mMusicPlaySource = musicPlaySource;
    }

    public static MusicRepository getInstance(MusicLocalSource localDataSource, MusicPlaySource musicPlaySource) {
        if (INSTANCE == null) {
            synchronized (MusicRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MusicRepository(localDataSource, musicPlaySource);
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance(){
        MusicPlayContractImpl.destroyInstance();
        MusicLocalSourceImpl.destroyInstance();
        INSTANCE = null;
    }

    @Override
    public Observable<List<PlayList>> playLists() {
        return mLocalDataSource.playLists();
    }

    @Override
    public List<PlayList> cachedPlayLists() {
        return mLocalDataSource.cachedPlayLists();
    }

    @Override
    public Observable<PlayList> create(PlayList playList) {
        return mLocalDataSource.create(playList);
    }

    @Override
    public Observable<PlayList> update(PlayList playList) {
        return mLocalDataSource.update(playList);
    }

    @Override
    public Observable<PlayList> delete(PlayList playList) {
        return mLocalDataSource.delete(playList);
    }

    @Override
    public Observable<ArrayList<Song>> insert(ArrayList<Song> songs) {
        return mLocalDataSource.insert(songs);
    }

    @Override
    public Observable<Song> update(Song song) {
        return mLocalDataSource.update(song);
    }

    @Override
    public void bindMusicView(MusicPlayView musicPlayView) {
        mMusicPlaySource.bindMusicView(musicPlayView);
    }

    @Override
    public void retrieveLastPlayMode() {
        mMusicPlaySource.retrieveLastPlayMode();
    }

    @Override
    public void setSongAsFavorite(Song song, boolean favorite) {
        mMusicPlaySource.setSongAsFavorite(song, favorite);
    }

    @Override
    public void bindMusicService() {
        mMusicPlaySource.bindMusicService();
    }

    @Override
    public void unBindMusicService() {
        mMusicPlaySource.unBindMusicService();
    }
}
