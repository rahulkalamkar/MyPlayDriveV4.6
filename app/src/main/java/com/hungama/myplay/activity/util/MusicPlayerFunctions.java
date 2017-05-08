package com.hungama.myplay.activity.util;

import android.content.Context;

import java.io.FileDescriptor;

public interface MusicPlayerFunctions {

    public void init(Context context /*Object obj, VideoActivity act, View mainView, MusicPlayerListner listner*/);

    public void prepare();

    public void start();

    public void pause();

    public void stop();

    public void release();

    public void reset();

    public int getCurrentPosition();

    public int getDuration();

    public boolean isPlaying();

    public int getPlayState();

    public void setPlayState(int value);

    public void seekTo(int timeMilliseconds);

    public void setWakeMode(Context context, int value);

    public void setOnPreparedListener(MusicPlayerListner.MyMusicOnPreparedListener listener);

    public void setOnBufferingUpdateListener(MusicPlayerListner.MyMusicOnBufferingUpdateListener listener);

    public void setOnCompletionListener(MusicPlayerListner.MyMusicOnCompletionListener listener);

    public void setOnErrorListener(MusicPlayerListner.MyMusicOnErrorListener listener);

    public void setDataSource(String url);

    public void setDataSource(FileDescriptor desc);

    public void setAudioStreamType(int value);

    public void prepareAsync(MusicPlayerListner.MyMusicOnPreparedListener myMusicOnPreparedListener);

    public int getAudioSessionId();

    public void setTrackId(long trackId);

    //public void setAudioStreamType(int trackId);

}
    /*isPlaying()
playerState
        currentPlayer.stop();
        currentPlayer.release();
        currentPlayer.prepare();
        currentPlayer.start();
        currentPlayer.pause();
        getCurrentPosition()
        currentPlayer.stop();
        currentPlayer.reset();
        getDuration()
        currentPlayer.seekTo(timeMilliseconds);
        currentPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
        currentPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        currentPlayer.setOnBufferingUpdateListener(this);
        currentPlayer.setOnCompletionListener(this);
        currentPlayer.setOnErrorListener(this);
        currentPlayer.setDataSource(audioAd);
        currentPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        currentPlayer.prepareAsync(new MediaPlayer.OnPreparedListener);
        currentPlayer.getAudioSessionId();
        currentPlayer.prepare();
        currentPlayer.setTrackId(trackId);*/
