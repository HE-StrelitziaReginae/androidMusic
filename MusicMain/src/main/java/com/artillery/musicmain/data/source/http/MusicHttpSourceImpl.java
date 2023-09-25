package com.artillery.musicmain.data.source.http;

import com.artillery.musicmain.data.source.http.service.ApiService;
import com.artillery.musicmain.data.source.MusicHttpSource;

/**
 * @author ArtilleryOrchid
 */
public class MusicHttpSourceImpl implements MusicHttpSource {
    private ApiService apiService;
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
}
