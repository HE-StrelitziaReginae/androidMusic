package com.artillery.musicmain.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;

import com.artillery.musicmain.BR;
import com.artillery.musicmain.data.source.http.model.MetingTrack;
import com.artillery.musicmain.data.source.http.service.MetingApiService;
import com.artillery.musicmain.net.RetrofitClient;
import com.artillery.musicmain.R;
import com.artillery.musicmain.theme.MusicSkin;

import com.artillery.musicbase.base.BaseViewModel;

import me.tatarka.bindingcollectionadapter2.ItemBinding;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author ArtilleryOrchid
 */
public class MusicOnlineViewModel extends BaseViewModel {
    public final ObservableList<MetingTrackItemViewModel> items = new ObservableArrayList<>();
    public final ItemBinding<MetingTrackItemViewModel> itemBinding =
            ItemBinding.of(BR.trackModel, R.layout.music_online_item);
    private final MetingApiService mApi = RetrofitClient.getInstance().create(MetingApiService.class);
    private MetingTrackItemViewModel selectedItem;

    public MusicOnlineViewModel(@NonNull Application application) {
        super(application);
    }


    public void search(String query, int category, SearchCallback callback) {
        addSubscribe(mApi.searchSongs("netease", "search", query, 30)
                .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(tracks -> {
                    items.clear();
                    selectedItem = null;
                    Map<String, MetingTrack> uniqueTracks = new LinkedHashMap<>();
                    if (tracks != null) {
                        for (MetingTrack track : tracks) {
                            String songId = track == null ? null : track.getSongId();
                            if (songId != null && !songId.isEmpty() && matchesCategory(track, category)) {
                                uniqueTracks.put(songId, track);
                            }
                        }
                    }
                    for (MetingTrack track : uniqueTracks.values()) {
                        items.add(new MetingTrackItemViewModel(this, track));
                    }
                    callback.onSuccess(items.isEmpty());
                }, callback::onError));
    }

    public void selectItem(MetingTrackItemViewModel item) {
        if (selectedItem != null && selectedItem != item) {
            selectedItem.setSelected(false);
        }
        selectedItem = item;
        if (item != null) {
            item.setSelected(true);
        }
    }

    public void clear() {
        items.clear();
        selectedItem = null;
    }

    public void applySkin(MusicSkin skin) {
        for (MetingTrackItemViewModel item : items) {
            item.applySkin(skin);
        }
    }

    private boolean matchesCategory(MetingTrack track, int category) {
        String text = ((track.title == null ? "" : track.title)
                + " " + (track.author == null ? "" : track.author));
        boolean hasChinese = false;
        boolean hasJapanese = false;
        boolean hasKorean = false;
        boolean hasLatin = false;
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            hasChinese |= character >= 0x4E00 && character <= 0x9FFF;
            hasJapanese |= (character >= 0x3040 && character <= 0x30FF);
            hasKorean |= (character >= 0xAC00 && character <= 0xD7AF);
            hasLatin |= (character >= 'A' && character <= 'Z')
                    || (character >= 'a' && character <= 'z');
        }
        switch (category) {
            case 0:
                return hasChinese && !hasJapanese && !hasKorean;
            case 1:
                return hasLatin && !hasChinese && !hasJapanese && !hasKorean;
            case 2:
                return hasJapanese || hasKorean;
            default:
                return !hasChinese && !hasJapanese && !hasKorean && !hasLatin;
        }
    }

    public interface SearchCallback {
        void onSuccess(boolean empty);

        void onError(Throwable error);
    }

}
