package com.artillery.musicservice.data;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;

/**
 * @author ArtilleryOrchid
 */
public class MusicLocalUtils {
    private static volatile MusicLocalUtils mInstance;

    public static MusicLocalUtils getInstance() {
        if (mInstance == null) {
            synchronized (MusicLocalUtils.class) {
                if (mInstance == null) {
                    mInstance = new MusicLocalUtils();
                }
            }
        }
        return mInstance;
    }

    public ArrayList<Song> getMusic(Context context) {
        ArrayList<Song> list = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                , null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Song song = new Song();
                // 提取公共逻辑到方法中，增强复用性
                String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                int songId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                String artistName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                int songDuration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                int fileSize = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                String albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                // 设置基础属性
                song.setId(songId);
                song.setPath(filePath);
                song.setDuration(songDuration);
                song.setSize(fileSize);
                song.setAlbum(albumName);
                // 检查文件大小是否符合要求
                if (fileSize <= 800 * 1000) { // 小于800KB直接跳过
                    return null;
                }
                // 提取标题和艺术家信息
                String[] parsedData = parseDisplayName(displayName);
                if (parsedData != null) {
                    song.setTitle(parsedData[0]);
                    song.setArtist(parsedData[1]);
                } else {
                    song.setTitle(displayName.trim()); // 使用原始艺术家名称
                    song.setArtist(artistName);
                }
                // 添加到列表
                list.add(song);
            }
        }
        assert cursor != null;
        cursor.close();
        return list;
    }

    /**
     * 工具方法：解析显示名称
     *
     * @param displayName 文件显示名称
     * @return 包含艺术家和标题的数组，格式：[artist, title]，如果无法解析则返回null
     */
    private String[] parseDisplayName(String displayName) {
        if (displayName == null || !displayName.contains("-")) {
            return null;
        }
        String[] splitName = displayName.split("-");
        if (splitName.length < 2) {
            return null;
        }
        // 去除文件扩展名并返回结果
        String title = splitName[0].trim();
        String artist = splitName[1].trim().replaceAll("\\.[^.]+$", ""); // 去除扩展名
        return new String[]{title, artist};
    }
}
