package com.laithlab.rhythm.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import java.util.List;

import com.laithlab.rhythm.dto.SongDTO;

import timber.log.Timber;

public class PlayBackUtil {
    private static List<SongDTO> currentPlayList = null;
    private static int currentSongPosition = 0;
    private static MediaPlayer mediaPlayer = null;
    private static PlayMode currentPlayMode = PlayMode.NONE;
    private static RhythmSong currentSong;

    public static MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public static MediaPlayer setMediaPlayer(Context context, String songLocation) {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
            }
            mediaPlayer = MediaPlayer.create(context, Uri.parse(songLocation));
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            return mediaPlayer;
        } catch (Exception e) {
            Timber.e(e, "setMediaPlayer");
            mediaPlayer = MediaPlayer.create(context, Uri.parse(songLocation));
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            return mediaPlayer;
        }
    }

    public static List<SongDTO> getCurrentPlayList() {
        return currentPlayList;
    }

    public static void setCurrentPlayList(List<SongDTO> newPlayList) {
        currentPlayList = newPlayList;
    }

    public static int getCurrentSongPosition() {
        return currentSongPosition;
    }

    public static void setCurrentSongPosition(int newCurrentPosition) {
        currentSongPosition = newCurrentPosition;
    }

    public static PlayMode getCurrentPlayMode() {
        return currentPlayMode;
    }

    public static PlayMode getUpdateCurrentPlayMode(PlayMode newPlayMode) {
        if (newPlayMode == PlayMode.SHUFFLE) {
            if (currentPlayMode == PlayMode.NONE || currentPlayMode == PlayMode.SINGLE_REPEAT) {
                currentPlayMode = PlayMode.SHUFFLE;
            } else if (currentPlayMode == PlayMode.ALL_REPEAT) {
                currentPlayMode = PlayMode.SHUFFLE_REPEAT;
            } else if (currentPlayMode == PlayMode.SHUFFLE_REPEAT) {
                currentPlayMode = PlayMode.ALL_REPEAT;
            } else if (currentPlayMode == PlayMode.SHUFFLE) {
                currentPlayMode = PlayMode.NONE;
            }
        } else if (newPlayMode == PlayMode.REPEAT) {
            if (currentPlayMode == PlayMode.NONE) {
                currentPlayMode = PlayMode.SINGLE_REPEAT;
            } else if (currentPlayMode == PlayMode.SINGLE_REPEAT) {
                currentPlayMode = PlayMode.ALL_REPEAT;
            } else if (currentPlayMode == PlayMode.ALL_REPEAT) {
                currentPlayMode = PlayMode.NONE;
            } else if (currentPlayMode == PlayMode.SHUFFLE) {
                currentPlayMode = PlayMode.SHUFFLE_REPEAT;
            } else if (currentPlayMode == PlayMode.SHUFFLE_REPEAT) {
                currentPlayMode = PlayMode.SHUFFLE;
            }
        }
        return currentPlayMode;
    }

    public static RhythmSong getCurrentSong() {
        return currentSong;
    }

    public static void setCurrentSong(RhythmSong currentSong) {
        PlayBackUtil.currentSong = currentSong;
    }
}
