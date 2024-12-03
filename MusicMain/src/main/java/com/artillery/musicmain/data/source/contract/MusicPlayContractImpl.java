package com.artillery.musicmain.data.source.contract;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.artillery.musicbase.utils.Utils;
import com.artillery.musicmain.data.source.MusicPlaySource;
import com.artillery.musicmain.data.source.contract.view.MusicPlayView;
import com.artillery.musicservice.data.Song;
import com.artillery.musicservice.service.MusicService;

/**
 * @author ArtilleryOrchid
 */
public class MusicPlayContractImpl implements MusicPlaySource {
    private volatile static MusicPlayContractImpl INSTANCE = null;
    private boolean mIsServiceBound;
    private MusicPlayView mMusicPlayView;
    private MusicService mMusicService;
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMusicService = ((MusicService.LocalBinder) service).getService();
            mMusicPlayView.onPlaybackServiceBound(mMusicService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMusicService = null;
            mMusicPlayView.onPlaybackServiceUnbound();
        }
    };

    public static MusicPlayContractImpl getInstance() {
        if (INSTANCE == null) {
            synchronized (MusicPlayContractImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MusicPlayContractImpl();
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public void bindMusicView(MusicPlayView musicPlayView) {
        mMusicPlayView = musicPlayView;
    }

    @Override
    public void retrieveLastPlayMode() {
    }

    @Override
    public void setSongAsFavorite(Song song, boolean favorite) {

    }

    @Override
    public void bindMusicService() {
        Utils.getContext().bindService(new Intent(Utils.getContext(), MusicService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsServiceBound = true;
    }

    @Override
    public void unBindMusicService() {
        if (mIsServiceBound) {
            Utils.getContext().unbindService(mConnection);
            mIsServiceBound = false;
        }
    }

}