package com.artillery.musicmain.data.source.http;

import com.artillery.musicmain.data.source.MusicHttpSource;
import com.artillery.musicmain.data.source.http.model.MetingTrack;
import com.artillery.musicmain.data.source.http.service.ApiService;
import com.artillery.musicmain.net.RetrofitClient;

import java.util.List;

import io.reactivex.Observable;

/**
 * @author ArtilleryOrchid
 */
public class MusicHttpSourceImpl implements MusicHttpSource {
    private final ApiService apiService;
    private volatile static MusicHttpSourceImpl INSTANCE = null;

    public static MusicHttpSourceImpl getInstance(ApiService apiService) {
        if (INSTANCE == null) {
            synchronized (MusicHttpSourceImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MusicHttpSourceImpl(apiService);
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    private MusicHttpSourceImpl(ApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public Observable<List<MetingTrack>> searchSongs(String server, String type, String query, int limit) {
        return apiService.searchSongs(server, type, query, limit);
    }

    @Override
    public String getPlaybackUrl(String songId) {
        return RetrofitClient.publicPlaybackUrl(songId);
    }
}
