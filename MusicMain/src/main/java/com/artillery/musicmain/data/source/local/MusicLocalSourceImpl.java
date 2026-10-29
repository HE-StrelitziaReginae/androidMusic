package com.artillery.musicmain.data.source.local;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.artillery.musicbase.utils.Utils;
import com.artillery.musicmain.data.source.MusicLocalSource;
import com.artillery.musicservice.data.MusicLocalUtils;
import com.artillery.musicservice.data.PlayList;
import com.artillery.musicservice.data.Song;
import com.artillery.musicservice.db.MusicDBHelper;
import com.artillery.musicservice.db.MusicDao;
import com.artillery.musicservice.db.MusicDataBase;
import com.artillery.musicservice.service.MusicMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * 本地数据源，可配合Room框架使用
 *
 * @author ArtilleryOrchid
 */
public class MusicLocalSourceImpl implements MusicLocalSource {
    private final MusicDataBase mMusicDataBase;
    private final MusicDao mMusicDao;
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
        synchronized (MusicLocalSourceImpl.class) {
            INSTANCE = null;
        }
    }

    private MusicLocalSourceImpl() {
        mMusicDataBase = MusicDBHelper.getInstance().getMusicDataBase();
        mMusicDao = mMusicDataBase.musicDao();
    }

    @Override
    public Observable<List<PlayList>> playLists() {
        return Observable.<List<PlayList>>fromCallable(() -> {
            List<PlayList> playLists = mMusicDao.getPlaylists();
            return new ArrayList<>(playLists);
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<PlayList> create(PlayList playList) {
        return Observable.fromCallable(() -> {
            requirePlaylist(playList);
            normalizePlaylistForWrite(playList);
            long id = mMusicDao.insertPlaylist(playList);
            playList.setId(id);
            return playList;
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<PlayList> update(PlayList playList) {
        return Observable.fromCallable(() -> {
            requirePlaylist(playList);
            requirePersistedId(playList.getId(), "playList");
            normalizePlaylistForWrite(playList);
            if (mMusicDao.updatePlaylist(playList) == 0) {
                throw new IllegalStateException("Playlist does not exist: " + playList.getId());
            }
            return playList;
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<PlayList> delete(PlayList playList) {
        return Observable.fromCallable(() -> {
            requirePlaylist(playList);
            requirePersistedId(playList.getId(), "playList");
            if (mMusicDao.deletePlaylist(playList) == 0) {
                throw new IllegalStateException("Playlist does not exist: " + playList.getId());
            }
            return playList;
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<ArrayList<Song>> insert(ArrayList<Song> songs) {
        return Observable.fromCallable(() -> {
            ArrayList<Song> validSongs = nonNullSongs(songs);
            if (validSongs.isEmpty()) {
                return validSongs;
            }
            mergeFavoriteState(validSongs, mMusicDao.getSongs());
            mMusicDao.insertSongs(validSongs);
            return validSongs;
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Song> update(Song song) {
        return Observable.fromCallable(() -> {
            requireSong(song);
            requirePersistedId(song.getId(), "song");
            if (mMusicDao.updateSong(song) == 0) {
                throw new IllegalStateException("Song does not exist: " + song.getId());
            }
            return song;
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Song> delete(Song song) {
        return Observable.fromCallable(() -> {
            requireSong(song);
            requirePersistedId(song.getId(), "song");
            if (mMusicDao.deleteSong(song) == 0) {
                throw new IllegalStateException("Song does not exist: " + song.getId());
            }
            return song;
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Song> setFavorite(Song song, boolean favorite) {
        return Observable.fromCallable(() -> {
            requireSong(song);
            requirePersistedId(song.getId(), "song");
            song.setFavorite(favorite);
            if (mMusicDao.updateSong(song) == 0) {
                throw new IllegalStateException("Song does not exist: " + song.getId());
            }
            return song;
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<ArrayList<Song>> songs() {
        return Observable.fromCallable(() -> new ArrayList<>(mMusicDao.getSongs()))
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<ArrayList<Song>> favoriteSongs() {
        return Observable.fromCallable(() -> new ArrayList<>(mMusicDao.getFavoriteSongs()))
                .subscribeOn(Schedulers.io());
    }

    @WorkerThread
    @Nullable
    @Override
    public ArrayList<Song> scanMusicList() {
        ArrayList<Song> scannedSongs = MusicLocalUtils.getMusic(Utils.getContext());
        if (scannedSongs == null) {
            return null;
        }
        final ArrayList<Song> result = scannedSongs;
        mMusicDataBase.runInTransaction(() -> {
            mergeFavoriteState(result, mMusicDao.getSongs());
            mMusicDao.deleteAllSongs();
            if (!result.isEmpty()) {
                mMusicDao.insertSongs(result);
            }
        });
        return result;
    }

    private ArrayList<Song> nonNullSongs(ArrayList<Song> songs) {
        ArrayList<Song> validSongs = new ArrayList<>();
        if (songs == null) {
            return validSongs;
        }
        for (Song song : songs) {
            if (song != null) {
                validSongs.add(song);
            }
        }
        return validSongs;
    }

    private void mergeFavoriteState(List<Song> targetSongs, List<Song> storedSongs) {
        Map<String, Boolean> favoriteByPath = new HashMap<>();
        Map<String, Long> storedIdByPath = new HashMap<>();
        Map<Long, Boolean> favoriteById = new HashMap<>();
        for (Song storedSong : storedSongs) {
            if (storedSong.getPath() != null) {
                storedIdByPath.put(storedSong.getPath(), storedSong.getId());
            }
            if (!storedSong.isFavorite()) {
                continue;
            }
            if (storedSong.getPath() != null) {
                favoriteByPath.put(storedSong.getPath(), true);
            }
            favoriteById.put(storedSong.getId(), true);
        }
        for (Song song : targetSongs) {
            if (song.getPath() != null && storedIdByPath.containsKey(song.getPath())) {
                song.setId(storedIdByPath.get(song.getPath()));
            }
            if (!song.isFavorite() && ((song.getPath() != null && favoriteByPath.containsKey(song.getPath()))
                    || (song.getId() != 0 && favoriteById.containsKey(song.getId())))) {
                song.setFavorite(true);
            }
        }
    }

    private void normalizePlaylistForWrite(PlayList playList) {
        Date now = new Date();
        if (playList.getCreatedAt() == null) {
            playList.setCreatedAt(now);
        }
        playList.setUpdatedAt(now);
        playList.setNumOfSongs(playList.getSongs().size());
        if (playList.getPlayMode() == null) {
            playList.setPlayMode(MusicMode.getDefault());
        }
    }

    private void requirePlaylist(PlayList playList) {
        if (playList == null) {
            throw new IllegalArgumentException("playList must not be null");
        }
    }

    private void requireSong(Song song) {
        if (song == null) {
            throw new IllegalArgumentException("song must not be null");
        }
    }

    private void requirePersistedId(long id, String type) {
        if (id == 0) {
            throw new IllegalArgumentException(type + " must have a persisted id");
        }
    }

}
