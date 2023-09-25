package com.artillery.musicservice.service;

/**
 * @author ArtilleryOrchid
 */
public enum MusicMode {
    SINGLE,
    LOOP,
    LIST,
    SHUFFLE;

    public static MusicMode getDefault() {
        return LOOP;
    }

    public static MusicMode switchNextMode(MusicMode current) {
        if (current == null) {
            return getDefault();
        }
        switch (current) {
            case LOOP:
                return LIST;
            case LIST:
                return SHUFFLE;
            case SHUFFLE:
                return SINGLE;
            case SINGLE:
                return LOOP;
        }
        return getDefault();
    }
}
