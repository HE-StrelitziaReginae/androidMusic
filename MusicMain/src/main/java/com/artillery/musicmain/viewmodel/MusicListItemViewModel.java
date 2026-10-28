package com.artillery.musicmain.viewmodel;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;

import com.artillery.musicbase.base.MultiItemViewModel;
import com.artillery.musicbase.binding.command.BindingAction;
import com.artillery.musicbase.binding.command.BindingCommand;
import com.artillery.musicmain.data.MusicDaraListener;
import com.artillery.musicmain.ui.MusicMainDialog;
import com.os.sys.musicskin.MusicSkin;
import com.artillery.musicmain.utils.TimeUtils;
import com.artillery.musicservice.data.Song;

import java.util.ArrayList;

/**
 * @author ArtilleryOrchid
 */
public class MusicListItemViewModel extends MultiItemViewModel<MusicListViewModel> {

    //暂无专辑图片
    //public ObservableField<Bitmap> musicAlbum = new ObservableField<>();
    public ObservableField<String> musicName = new ObservableField<>();
    public ObservableField<String> musicArtist = new ObservableField<>();
    public ObservableField<String> musicDuration = new ObservableField<>();
    public ObservableField<android.graphics.drawable.GradientDrawable> cardBackground = new ObservableField<>();
    public ObservableBoolean selected = new ObservableBoolean(false);
    public ObservableInt musicNameColor = new ObservableInt();
    public ObservableInt musicArtistColor = new ObservableInt();
    public ObservableInt musicDurationColor = new ObservableInt();
    public ObservableInt selectedIndicatorColor = new ObservableInt();
    private final ArrayList<Song> mSongList = new ArrayList<>();
    private final Song mSong;
    private final int mStartIndex;

    public MusicListItemViewModel(@NonNull MusicListViewModel viewModel, Song song, ArrayList<Song> music, int start) {
        super(viewModel);
        mSongList.addAll(music);
        mSong = song;
        mStartIndex = start;
        musicName.set(song.getTitle());
        musicArtist.set(song.getArtist());
        musicDuration.set(TimeUtils.formatDuration(song.getDuration()));
        updateBackground();
    }

    public BindingCommand musicPlay = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            mViewModel.selectItem(MusicListItemViewModel.this);
            MusicDaraListener.getInstance().observerMusicData(mSongList, mStartIndex, mSong);
            MusicMainDialog.showCurrent();
        }
    });

    public void setSelected(boolean value) {
        selected.set(value);
        applySkin(MusicSkin.load(mViewModel.getApplication()));
    }

    public void applySkin(MusicSkin skin) {
        android.content.Context context = mViewModel.getApplication();
        boolean isSelected = selected.get();
        cardBackground.set(skin.cardBackground(context, isSelected));
        musicNameColor.set(isSelected ? skin.headerPrimaryText(context) : skin.primaryText(context));
        musicArtistColor.set(isSelected ? skin.headerSecondaryText(context) : skin.secondaryText(context));
        musicDurationColor.set(isSelected ? skin.headerSecondaryText(context) : skin.secondaryText(context));
        selectedIndicatorColor.set(skin.headerPrimaryText(context));
    }

    public String getSongPath() {
        return mSong.getPath();
    }

    private void updateBackground() {
        setSelected(false);
    }
}
