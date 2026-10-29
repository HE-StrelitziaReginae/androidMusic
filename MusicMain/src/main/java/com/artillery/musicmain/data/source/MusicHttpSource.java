package com.artillery.musicmain.data.source;

import com.artillery.musicmain.data.source.http.model.MetingTrack;

import java.util.List;

import io.reactivex.Observable;

/**
 * 请求接口
 *
 * @author ArtilleryOrchid
 */
public interface MusicHttpSource {
    Observable<List<MetingTrack>> searchSongs(String server, String type, String query, int limit);

    String getPlaybackUrl(String songId);
}
