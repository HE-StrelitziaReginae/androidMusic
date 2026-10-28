package com.artillery.musicmain.ui;

import android.content.Context;
import android.content.ClipData;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.TextViewCompat;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;

import com.artillery.musicbase.base.AppManager;
import com.artillery.musicbase.base.BaseFragment;
import com.artillery.musicbase.binding.command.BindingCommand;
import com.artillery.musicbase.utils.BackgroundThreadUtils;
import com.artillery.musicbase.utils.KLogUtils;
import com.artillery.musicbase.utils.SystemBarUtils;
import com.artillery.musicmain.BR;
import com.artillery.musicmain.R;
import com.artillery.musicmain.app.AppViewModelFactory;
import com.artillery.musicmain.data.MusicDaraListener;
import com.artillery.musicmain.data.MusicDataListener;
import com.artillery.musicmain.data.source.contract.view.MusicPlayView;
import com.artillery.musicmain.databinding.FragmentMusicMainBinding;
import com.artillery.musicmain.viewmodel.MusicMainViewModel;
import com.os.sys.musicskin.MusicSkin;
import com.artillery.musicservice.data.PlayList;
import com.artillery.musicservice.data.Song;
import com.artillery.musicservice.service.MusicListener;
import com.artillery.musicservice.service.MusicMode;
import com.artillery.musicservice.service.MusicService;

import java.util.ArrayList;

/**
 * @author ArtilleryOrchid
 */
public class MusicMainFragment extends BaseFragment<FragmentMusicMainBinding, MusicMainViewModel>
        implements MusicPlayView, MusicListener.Callback {
    private static final int REQUEST_PROFILE_IMAGE = 1001;
    private static final int REQUEST_PROFILE_CROP = 1002;
    private static final String PROFILE_PREFERENCES = "music_profile_preferences";
    private static final String PROFILE_IMAGE_URI = "profile_image_uri";
    private Uri mPendingProfileImageUri;
    protected MusicListener mMusicListener;
    private PlayList mPlayList;
    private ArrayList<Song> mMusicList = new ArrayList<>();
    private boolean mMusicListLoaded;
    private int mStartIndex = 0;
    private MusicSkin mMusicSkin;

    @Override
    public void initData() {
        loadProfileImage();
        AppManager.getAppManager().addFragment(this);
        mViewModel.showFragment(requireActivity(), 0);
        MusicDaraListener.getInstance().setMusicDataListener(new MusicDataListener() {
            @Override
            public void sendMusicSong(ArrayList<Song> songArrayList, int index, Song song) {
                KLogUtils.i("sendMusicSong: ");
                mPlayList = new PlayList();
                mPlayList.setSongs(songArrayList);
                mPlayList.setNumOfSongs(songArrayList.size());
                mStartIndex = index;
                updateMainUi(song);
                startPlay();
                activateMarquee(true);
            }

            @Override
            public void onMusicListUpdated(ArrayList<Song> songArrayList) {
                mMusicList = songArrayList == null ? new ArrayList<>() : new ArrayList<>(songArrayList);
                mMusicListLoaded = true;
                if (mMusicListener != null) {
                    mMusicListener.syncPlayList(mMusicList);
                    if (mMusicList.isEmpty()) {
                        updateMainUi(null);
                        updatePlayToggle(false);
                    }
                }
            }
        });
    }

    @Override
    public void initViewObservable() {
        applySystemBarInsets();
        mViewModel.binMusicView(this);
        mBinding.musicGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == mBinding.musicMain.getId()) {
                    mViewModel.showFragment(requireActivity(), 0);
                } else if (checkedId == mBinding.musicOnline.getId()) {
                    mViewModel.showFragment(requireActivity(), 1);
                } else if (checkedId == mBinding.musicMine.getId()) {
                    mViewModel.showFragment(requireActivity(), 2);
                }
            }
        });
        mViewModel.mPlay = new BindingCommand(() -> {
            if (mMusicListener == null) {
                return;
            }
            if (mMusicListener.isPlaying()) {
                mMusicListener.pause();
            } else {
                mMusicListener.play();
            }
        });
        mViewModel.mShow = new BindingCommand(() -> {
            MusicMainDialog.showCurrent();
        });
        initDrawer();
        applySkin(MusicSkin.load(requireContext()));
    }

    private void applySystemBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.musicDrawerLayout, (view, windowInsets) -> {
            int topInset = windowInsets.getSystemWindowInsetTop();
            int bottomInset = windowInsets.getSystemWindowInsetBottom();
            ViewGroup.LayoutParams toolbarParams = mBinding.musicToolbar.getLayoutParams();
            toolbarParams.height = getResources().getDimensionPixelSize(com.artillery.musicbase.R.dimen.dp_56) + topInset;
            mBinding.musicToolbar.setLayoutParams(toolbarParams);
            mBinding.musicToolbar.setPadding(
                    mBinding.musicToolbar.getPaddingLeft(),
                    topInset,
                    mBinding.musicToolbar.getPaddingRight(),
                    mBinding.musicToolbar.getPaddingBottom());

            ViewGroup.LayoutParams groupParams = mBinding.musicGroup.getLayoutParams();
            groupParams.height = getResources().getDimensionPixelSize(com.artillery.musicbase.R.dimen.dp_60) + bottomInset;
            mBinding.musicGroup.setLayoutParams(groupParams);
            mBinding.musicGroup.setPadding(
                    mBinding.musicGroup.getPaddingLeft(),
                    mBinding.musicGroup.getPaddingTop(),
                    mBinding.musicGroup.getPaddingRight(),
                    bottomInset);

            mBinding.musicDrawerPanel.setPadding(
                    mBinding.musicDrawerPanel.getPaddingLeft(),
                    topInset,
                    mBinding.musicDrawerPanel.getPaddingRight(),
                    bottomInset);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(mBinding.musicDrawerLayout);
    }

    private void initDrawer() {
        updateDrawerPlayMode(MusicMode.getDefault());
        mBinding.musicMenuBtn.setOnClickListener(view -> mBinding.musicDrawerLayout.openDrawer(mBinding.musicDrawerPanel));
        mBinding.drawerProfile.setOnClickListener(view -> showProfileDialog());
        mBinding.drawerAvatar.setOnClickListener(view -> openProfileImagePicker());
        mBinding.drawerRefresh.setOnClickListener(view -> {
            mViewModel.refreshMusicList(requireActivity());
            mBinding.musicDrawerLayout.closeDrawer(mBinding.musicDrawerPanel);
            Toast.makeText(requireContext(), R.string.music_refresh_done, Toast.LENGTH_SHORT).show();
        });
        mBinding.drawerPlayMode.setOnClickListener(view -> showPlayModeDialog());
        mBinding.drawerSkin.setOnClickListener(view -> showSkinDialog());
        mBinding.drawerAbout.setOnClickListener(view -> showAboutDialog());
    }

    private void openProfileImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_PROFILE_IMAGE);
    }

    private void startProfileCrop(Uri sourceUri) {
        File outputFile = getProfileImageFile();
        if (outputFile.exists()) {
            outputFile.delete();
        }
        Uri outputUri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".fileprovider",
                outputFile);
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(sourceUri, "image/*");
        cropIntent.putExtra("crop", "true");
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        cropIntent.putExtra("outputX", 512);
        cropIntent.putExtra("outputY", 512);
        cropIntent.putExtra("scale", true);
        cropIntent.putExtra("return-data", false);
        cropIntent.putExtra("output", outputUri);
        cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cropIntent.setClipData(ClipData.newRawUri("profile_image", sourceUri));
        if (cropIntent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivityForResult(cropIntent, REQUEST_PROFILE_CROP);
        } else {
            saveProfileImage(sourceUri);
        }
    }

    private File getProfileImageFile() {
        File directory = new File(requireContext().getFilesDir(), "profile");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return new File(directory, "avatar.jpg");
    }

    private void saveProfileImage(Uri uri) {
        requireContext().getSharedPreferences(PROFILE_PREFERENCES, Context.MODE_PRIVATE)
                .edit().putString(PROFILE_IMAGE_URI, uri.toString()).apply();
        loadProfileImage();
    }

    private void loadProfileImage() {
        mBinding.drawerAvatar.setImageResource(R.drawable.ellipse);
        String uri = requireContext().getSharedPreferences(PROFILE_PREFERENCES, Context.MODE_PRIVATE)
                .getString(PROFILE_IMAGE_URI, null);
        if (!TextUtils.isEmpty(uri)) {
            Glide.with(this).load(Uri.parse(uri)).circleCrop().placeholder(R.drawable.ellipse).error(R.drawable.ellipse)
                    .into(mBinding.drawerAvatar);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != android.app.Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_PROFILE_IMAGE && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                requireContext().getContentResolver().takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException ignored) {
            }
            mPendingProfileImageUri = uri;
            startProfileCrop(uri);
        } else if (requestCode == REQUEST_PROFILE_CROP) {
            File outputFile = getProfileImageFile();
            if (outputFile.exists()) {
                Uri uri = FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().getPackageName() + ".fileprovider",
                        outputFile);
                saveProfileImage(uri);
            } else if (mPendingProfileImageUri != null) {
                saveProfileImage(mPendingProfileImageUri);
            }
        }
    }

    private void showSkinDialog() {
        MusicSkin[] skins = MusicSkin.values();
        int selectedIndex = mMusicSkin == null ? MusicSkin.LIGHT.ordinal() : mMusicSkin.ordinal();
        new AlertDialog.Builder(requireContext())
                .setTitle(com.os.sys.musicskin.R.string.music_skin_title)
                .setSingleChoiceItems(com.os.sys.musicskin.R.array.music_skins, selectedIndex, (dialog, which) -> {
                    applySkin(skins[which]);
                    dialog.dismiss();
                    mBinding.musicDrawerLayout.closeDrawer(mBinding.musicDrawerPanel);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void applySkin(MusicSkin skin) {
        mMusicSkin = skin;
        skin.save(requireContext());
        int background = skin.background(requireContext());
        int surface = skin.surface(requireContext());
        int icon = skin.icon(requireContext());
        mBinding.musicDrawerLayout.setBackgroundColor(background);
        mBinding.musicToolbar.setBackgroundColor(surface);
        mBinding.musicToolbarTitle.setTextColor(skin.primaryText(requireContext()));
        mBinding.musicMenuBtn.setColorFilter(icon);
        mBinding.musicShowDialog.setBackgroundColor(surface);
        mBinding.musicNamePlay.setTextColor(skin.primaryText(requireContext()));
        mBinding.musicArtistPlay.setTextColor(skin.secondaryText(requireContext()));
        mBinding.musicLine.setBackgroundColor(skin.secondaryText(requireContext()));
        mBinding.musicPlayBtn.setColorFilter(icon);
        mBinding.musicDrawerPanel.setBackgroundColor(skin.drawer(requireContext()));
        mBinding.drawerProfile.setBackground(skin.drawerHeaderBackground(requireContext()));
        mBinding.drawerCharacterArt.setVisibility(skin.isCharacterSkin() ? View.VISIBLE : View.GONE);
        mBinding.drawerCharacterScrim.setVisibility(skin.isCharacterSkin() ? View.VISIBLE : View.GONE);
        mBinding.drawerProfileName.setText(skin.isCharacterSkin()
                ? com.os.sys.musicskin.R.string.music_character_name : R.string.music_profile_name);
        mBinding.drawerProfileSubtitle.setText(skin.isCharacterSkin()
                ? com.os.sys.musicskin.R.string.music_character_badge : R.string.music_profile_subtitle);
        mBinding.drawerProfileHint.setText(skin.isCharacterSkin()
                ? com.os.sys.musicskin.R.string.music_character_hint : R.string.music_profile_hint);
        mBinding.drawerProfileName.setTextColor(skin.headerPrimaryText(requireContext()));
        mBinding.drawerProfileSubtitle.setTextColor(skin.headerSecondaryText(requireContext()));
        mBinding.drawerProfileHint.setTextColor(skin.headerSecondaryText(requireContext()));
        applyProfileAvatar(skin);
        mBinding.musicAlbumImg.setImageResource(skin.isCharacterSkin()
                ? com.os.sys.musicskin.R.drawable.skin_sakura_avatar : R.drawable.ellipse);
        mBinding.drawerLibrarySection.setTextColor(skin.secondaryText(requireContext()));
        mBinding.drawerPlayerSection.setTextColor(skin.secondaryText(requireContext()));
        mBinding.drawerRefreshText.setTextColor(skin.primaryText(requireContext()));
        mBinding.drawerPlayModeText.setTextColor(skin.primaryText(requireContext()));
        mBinding.drawerPlayModeValue.setTextColor(skin.secondaryText(requireContext()));
        mBinding.drawerSkinText.setTextColor(skin.primaryText(requireContext()));
        mBinding.drawerAboutText.setTextColor(skin.primaryText(requireContext()));
        applyDrawerItemSkin(skin);
        mBinding.musicGroup.setBackground(skin.tabContainerBackground(requireContext()));
        mBinding.musicMain.setTextColor(skin.tabTextColors(requireContext()));
        mBinding.musicOnline.setTextColor(skin.tabTextColors(requireContext()));
        mBinding.musicMine.setTextColor(skin.tabTextColors(requireContext()));
        applyTabVisual(mBinding.musicMain, skin, com.os.sys.musicskin.R.drawable.music_tab_home,
                com.os.sys.musicskin.R.drawable.skin_sakura_tab_music);
        applyTabVisual(mBinding.musicOnline, skin, com.os.sys.musicskin.R.drawable.music_tab_online,
                com.os.sys.musicskin.R.drawable.skin_sakura_tab_online);
        applyTabVisual(mBinding.musicMine, skin, com.os.sys.musicskin.R.drawable.music_tab_mine,
                com.os.sys.musicskin.R.drawable.skin_sakura_tab_mine);
        mBinding.musicMain.setBackground(skin.tabBackground(requireContext()));
        mBinding.musicOnline.setBackground(skin.tabBackground(requireContext()));
        mBinding.musicMine.setBackground(skin.tabBackground(requireContext()));
        tintDrawerIcons(icon);
        mViewModel.applySkin(requireActivity(), skin);
        MusicMainDialog.applySkinCurrent(skin);
        SystemBarUtils.setLightSystemBars(requireActivity().getWindow(), skin.usesLightSystemBars());
    }

    private void applyTabVisual(android.widget.TextView tab, MusicSkin skin,
                                int standardIcon, int characterIcon) {
        Drawable drawable = ContextCompat.getDrawable(requireContext(),
                skin.isCharacterSkin() ? characterIcon : standardIcon);
        if (drawable == null) {
            return;
        }
        int size = getResources().getDimensionPixelSize(skin.isCharacterSkin()
                ? com.artillery.musicbase.R.dimen.dp_34
                : com.artillery.musicbase.R.dimen.dp_20);
        drawable.setBounds(0, 0, size, size);
        tab.setCompoundDrawables(null, drawable, null, null);
        tab.setCompoundDrawablePadding(getResources().getDimensionPixelSize(
                com.artillery.musicbase.R.dimen.dp_2));
        TextViewCompat.setCompoundDrawableTintList(tab,
                skin.isCharacterSkin() ? null : skin.tabIconTint(requireContext()));
    }

    private void applyProfileAvatar(MusicSkin skin) {
        String uri = requireContext().getSharedPreferences(PROFILE_PREFERENCES, Context.MODE_PRIVATE)
                .getString(PROFILE_IMAGE_URI, null);
        if (skin.isCharacterSkin() && TextUtils.isEmpty(uri)) {
            mBinding.drawerAvatar.setImageResource(com.os.sys.musicskin.R.drawable.skin_sakura_avatar);
            return;
        }
        loadProfileImage();
    }

    private void tintDrawerIcons(int color) {
        for (ViewGroup row : new ViewGroup[]{mBinding.drawerRefresh, mBinding.drawerPlayMode,
                mBinding.drawerSkin, mBinding.drawerAbout}) {
            for (int index = 0; index < row.getChildCount(); index++) {
                View child = row.getChildAt(index);
                if (child instanceof androidx.appcompat.widget.AppCompatImageView) {
                    ((androidx.appcompat.widget.AppCompatImageView) child).setColorFilter(color);
                }
            }
        }
    }

    private void applyDrawerItemSkin(MusicSkin skin) {
        int elevation = getResources().getDimensionPixelSize(com.artillery.musicbase.R.dimen.dp_2);
        View[] drawerItems = new View[]{
                mBinding.drawerRefresh,
                mBinding.drawerPlayMode,
                mBinding.drawerSkin,
                mBinding.drawerAbout
        };
        for (View drawerItem : drawerItems) {
            drawerItem.setBackground(skin.cardBackground(requireContext(), false));
            drawerItem.setElevation(elevation);
        }
    }

    private void showProfileDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.music_profile_name)
                .setMessage(R.string.music_profile_message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.music_about)
                .setMessage(R.string.music_about_message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showPlayModeDialog() {
        if (mMusicListener == null) {
            return;
        }
        MusicMode currentMode = mMusicListener.getPlayMode();
        MusicMode[] modes = MusicMode.values();
        int selectedIndex = currentMode == null ? MusicMode.getDefault().ordinal() : currentMode.ordinal();
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.music_play_mode_title)
                .setSingleChoiceItems(R.array.music_play_modes, selectedIndex, (dialog, which) -> {
                    mMusicListener.setPlayMode(modes[which]);
                    updatePlayMode(modes[which]);
                    dialog.dismiss();
                    mBinding.musicDrawerLayout.closeDrawer(mBinding.musicDrawerPanel);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void updateDrawerPlayMode(@Nullable MusicMode playMode) {
        MusicMode mode = playMode == null ? MusicMode.getDefault() : playMode;
        String[] modeNames = getResources().getStringArray(R.array.music_play_modes);
        mBinding.drawerPlayModeValue.setText(modeNames[mode.ordinal()]);
    }

    private void activateMarquee(boolean activate) {
        mBinding.musicNamePlay.setFocusable(activate);
        mBinding.musicNamePlay.setFocusableInTouchMode(activate);
        mBinding.musicNamePlay.setSelected(activate);
    }

    @Override
    public void onStop() {
        super.onStop();
        activateMarquee(false);
    }

    @Override
    public void onSwitchLast(@Nullable Song last) {
        onSongUpdated(last);
    }

    @Override
    public void onSwitchNext(@Nullable Song next) {
        onSongUpdated(next);
    }

    @Override
    public void onComplete(@Nullable Song next) {
        onSongUpdated(next);
    }

    @Override
    public void onPlaybackStateChanged(@Nullable Song song, long position, boolean isPlaying) {
        onSongUpdated(song);
        updatePlayToggle(isPlaying);
        MusicDaraListener.getInstance().setUpdatePosition(position);
    }

    @Override
    public void onPlayStatusChanged(boolean isPlaying) {
        updatePlayToggle(isPlaying);
    }

    @Override
    public void handleError(Throwable error) {
        KLogUtils.e("error: " + error);
    }

    @Override
    public void onPlaybackServiceBound(MusicService service) {
        KLogUtils.e("onPlaybackServiceBound: ");
        mMusicListener = service;
        mMusicListener.registerCallback(this);
        MusicDaraListener.getInstance().setMusicListener(mMusicListener);
        onPlaybackStateChanged(mMusicListener.getPlayingSong(), mMusicListener.currentPosition(), mMusicListener.isPlaying());
        updateDrawerPlayMode(mMusicListener.getPlayMode());
        if (mMusicListLoaded) {
            mMusicListener.syncPlayList(mMusicList);
        }
    }

    private void startPlay() {
        KLogUtils.e("startPlay: ");
        if (mMusicListener != null) {
            if (!mMusicListener.play(mPlayList, mStartIndex)) {
                updatePlayToggle(false);
                return;
            }
            MusicMainDialog existingDialog = MusicMainDialog.getExistingInstance();
            if (existingDialog != null) {
                BackgroundThreadUtils.getInstance().post(existingDialog.mProgressCallback);
            }
        }
    }

    @Override
    public void onPlaybackServiceUnbound() {
        KLogUtils.e("onPlaybackServiceUnbound: ");
        if (mMusicListener != null) {
            mMusicListener.unregisterCallback(this);
        }
        mMusicListener = null;
    }

    @Override
    public void onSongSetAsFavorite(@NonNull Song song) {
        KLogUtils.e("onSongSetAsFavorite: ");
    }

    @Override
    public void onSongUpdated(@Nullable Song song) {
        KLogUtils.e("onSongUpdated: ");
        //Music Duration
        updateMainUi(song);
    }

    private void updateMainUi(Song song) {
        KLogUtils.e("updateMainUi: ");
        MusicDaraListener.getInstance().setUpdate(song);
        if (song == null) {
            mBinding.musicNamePlay.setText(R.string.music_not_play);
            mBinding.musicArtistPlay.setText(R.string.music_unknown);
            return;
        }
        mBinding.musicNamePlay.setText(song.getTitle());
        mBinding.musicArtistPlay.setText(song.getArtist());
    }

    @Override
    public void updatePlayMode(MusicMode playMode) {
        KLogUtils.e("updatePlayMode: ");
        updateDrawerPlayMode(playMode);
    }

    @Override
    public void updatePlayToggle(boolean play) {
        KLogUtils.e("updatePlayToggle: " + play);
        MusicDaraListener.getInstance().setUpdateUi(play);
        mBinding.musicPlayBtn.setImageDrawable(play ? ContextCompat.getDrawable(requireContext(), R.drawable.pause)
                : ContextCompat.getDrawable(requireContext(), R.drawable.play));
        mBinding.musicPlayBtn.setColorFilter((mMusicSkin == null ? MusicSkin.load(requireContext()) : mMusicSkin)
                .icon(requireContext()));
    }

    @Override
    public void updateFavoriteToggle(boolean favorite) {
        KLogUtils.e("updateFavoriteToggle: ");
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_music_main;
    }

    @Override
    public MusicMainViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(requireActivity().getApplication());
        return ViewModelProviders.of(this, factory).get(MusicMainViewModel.class);
    }

    @Override
    public int initVariableId() {
        return BR.musicMainModel;
    }

    @Override
    public void onDestroyView() {
        if (mMusicListener != null) {
            mMusicListener.unregisterCallback(this);
            MusicDaraListener.getInstance().clearMusicListener(mMusicListener);
            mMusicListener = null;
        }
        AppManager.getAppManager().removeFragment(this);
        MusicDaraListener.getInstance().clearMusicDataListener();
        MusicDaraListener.getInstance().clearMusicObserver();
        super.onDestroyView();
        MusicMainDialog.dismissCurrent();
        mViewModel.mMusicRepository.unBindMusicService();
    }
}
