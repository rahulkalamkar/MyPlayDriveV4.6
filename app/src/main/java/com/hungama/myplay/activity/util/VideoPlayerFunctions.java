package com.hungama.myplay.activity.util;

import android.view.View;

import com.hungama.myplay.activity.ui.VideoActivity;

public interface VideoPlayerFunctions {

	public int getDuration();

	public long getCurrentPosition();

	public void init(Object obj, VideoActivity act, View mainView);

	public void pauseVideo();

	public void startVideo(boolean isNewSong);

	public void seekToVideo(long currentPosition);

	public int getBufferPercentage();

	public void releasePlayer();

	public boolean isPlaying();

	public void startVideo();

}
