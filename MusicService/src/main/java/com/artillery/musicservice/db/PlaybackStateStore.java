package com.artillery.musicservice.db;

import com.artillery.musicbase.utils.BackgroundThreadUtils;
import com.artillery.musicservice.data.PlaybackState;

public class PlaybackStateStore {
    public interface Callback {
        void onLoaded(PlaybackState state);
    }

    private final MusicDao mDao;

    public PlaybackStateStore() {
        mDao = MusicDBHelper.getInstance().getMusicDataBase().musicDao();
    }

    public void load(Callback callback) {
        BackgroundThreadUtils.getInstance().post(() -> {
            PlaybackState state = mDao.getPlaybackState();
            if (callback != null) {
                callback.onLoaded(state);
            }
        });
    }

    public void save(String songPath, long position, boolean playing) {
        PlaybackState state = new PlaybackState();
        state.setSongPath(songPath);
        state.setPosition(position);
        state.setPlaying(playing);
        BackgroundThreadUtils.getInstance().post(() -> mDao.savePlaybackState(state));
    }
}
