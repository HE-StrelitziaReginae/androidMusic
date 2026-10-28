package com.artillery.musicmain.data.source.http.service;

import com.artillery.musicmain.data.source.http.model.MetingTrack;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MetingApiService {
    @GET("api")
    Observable<List<MetingTrack>> searchSongs(
            @Query("server") String server,
            @Query("type") String type,
            @Query("id") String query,
            @Query("limit") int limit);
}
