package com.artillery.musicmain.viewmodel;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;

import com.artillery.musicbase.base.MultiItemViewModel;
import com.artillery.musicbase.binding.command.BindingAction;
import com.artillery.musicbase.binding.command.BindingCommand;
import com.artillery.musicmain.data.MusicDaraListener;
import com.artillery.musicmain.data.source.http.model.MetingTrack;
import com.os.sys.musicskin.MusicSkin;
import com.artillery.musicmain.ui.MusicMainDialog;
import com.artillery.musicservice.data.Song;

public class MetingTrackItemViewModel extends MultiItemViewModel<MusicOnlineViewModel> {
    public final ObservableField<String> title = new ObservableField<>();
    public final ObservableField<String> artist = new ObservableField<>();
    public final ObservableField<String> imageUrl = new ObservableField<>();
    public final ObservableField<String> duration = new ObservableField<>("--:--");
    public final ObservableField<android.graphics.drawable.GradientDrawable> cardBackground = new ObservableField<>();
    public final ObservableBoolean selected = new ObservableBoolean(false);
    public final ObservableInt titleColor = new ObservableInt();
    public final ObservableInt artistColor = new ObservableInt();
    public final ObservableInt durationColor = new ObservableInt();
    public final ObservableInt selectedIndicatorColor = new ObservableInt();
    private final MetingTrack mTrack;

    public MetingTrackItemViewModel(@NonNull MusicOnlineViewModel viewModel, MetingTrack track) {
        super(viewModel);
        mTrack = track;
        title.set(track.title);
        artist.set(track.author);
        imageUrl.set(track.pic);
        updateBackground();
    }

    public BindingCommand play = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            String songId = mTrack.getSongId();
            if (songId == null || songId.isEmpty()) {
                return;
            }
            mViewModel.selectItem(MetingTrackItemViewModel.this);
            Song song = new Song();
            song.setId(songId.hashCode());
            song.setTitle(mTrack.title);
            song.setArtist(mTrack.author);
            song.setAlbum(mTrack.author);
            song.setPath(getPlaybackUrl());
            if (MusicDaraListener.getInstance().playOnline(song)) {
                MusicMainDialog.showCurrent();
            }
        }
    });

    public String getPlaybackUrl() {
        return mViewModel.getPlaybackUrl(mTrack.getSongId());
    }

    public void setSelected(boolean value) {
        selected.set(value);
        updateBackground();
    }

    public void applySkin(MusicSkin skin) {
        android.content.Context context = mViewModel.getApplication();
        boolean isSelected = selected.get();
        cardBackground.set(skin.cardBackground(context, isSelected));
        titleColor.set(isSelected ? skin.headerPrimaryText(context) : skin.primaryText(context));
        artistColor.set(isSelected ? skin.headerSecondaryText(context) : skin.secondaryText(context));
        durationColor.set(isSelected ? skin.headerSecondaryText(context) : skin.secondaryText(context));
        selectedIndicatorColor.set(skin.headerPrimaryText(context));
    }

    private void updateBackground() {
        applySkin(MusicSkin.load(mViewModel.getApplication()));
    }
}
