package com.artillery.musicmain.ui;

import android.content.Context;
import android.widget.SeekBar;

import androidx.annotation.Nullable;

import com.artillery.musicbase.base.BaseDialog;
import com.artillery.musicbase.utils.KLogUtils;
import com.artillery.musicmain.R;
import com.artillery.musicmain.databinding.MusicMianDialogBinding;
import com.artillery.musicservice.data.PlayList;
import com.artillery.musicservice.data.Song;
import com.artillery.musicservice.service.MusicListener;

import java.util.ArrayList;

public class MusicMainDialog extends BaseDialog<MusicMianDialogBinding> implements MusicListener.Callback, SeekBar.OnSeekBarChangeListener {
    private MusicListener mMusicListener;
    private PlayList mPlayList;
    private int mStartIndex = 0;
    private Song mSong;

    public MusicMainDialog(Context context) {
        super(context, R.style.CustomFullDialog);
    }

    @Override
    public int initContentView() {
        return R.layout.music_mian_dialog;
    }

    public void obServerMusicData(ArrayList<Song> songArrayList, int index, Song song) {
        KLogUtils.i("sendMusicList: ");
        mPlayList = new PlayList();
        mPlayList.setSongs(songArrayList);
        mPlayList.setNumOfSongs(songArrayList.size());
        mStartIndex = index;
        mSong = song;
    }

    @Override
    public void initData() {
        KLogUtils.i("initData: ");
        updateMainUi(mSong);
        startPlay();
        activateMarquee(true);
    }

    private void startPlay() {
        KLogUtils.e("startPlay: ");
        if (mMusicListener != null) {
            mMusicListener.play(mPlayList, mStartIndex);
        }
    }

    private void updateMainUi(Song song) {
        KLogUtils.e("updateMainUi: ");
        mBinding.musicNamePlay.setText(song.getTitle());
        mBinding.musicArtistPlay.setText(song.getArtist());
    }

    private void activateMarquee(boolean activate) {
        mBinding.musicNamePlay.setFocusable(activate);
        mBinding.musicNamePlay.setFocusableInTouchMode(activate);
        mBinding.musicNamePlay.setSelected(activate);
    }

    @Override
    public void initViewObservable() {
        KLogUtils.i("initViewObservable: ");
    }

    @Override
    public void onSwitchLast(@Nullable Song last) {

    }

    @Override
    public void onSwitchNext(@Nullable Song next) {

    }

    @Override
    public void onComplete(@Nullable Song next) {

    }

    @Override
    public void onPlayStatusChanged(boolean isPlaying) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
