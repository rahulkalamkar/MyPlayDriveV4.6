package com.hungama.myplay.activity.util;

import android.view.View;
import android.widget.VideoView;

import com.hungama.myplay.activity.ui.VideoActivity;

public class VideoPlayer implements VideoPlayerFunctions {

	public VideoView videoView;
	VideoActivity videoAct;
	View mainView;

	@Override
	public int getDuration() {
		try {
			return (int) videoView.getDuration();
		} catch (Exception e) {
		}
		return 0;
	}

	@Override
	public long getCurrentPosition() {
		// TODO Auto-generated method stub
		return videoView.getCurrentPosition();
	}

	@Override
	public void init(Object obj, VideoActivity act, View mainView) {
		videoView = (VideoView) obj;
		videoAct = act;
		this.mainView = mainView;
	}

	public void pauseVideo() {
		videoView.pause();
	}

	public void startVideo(boolean isNewSong) {
		if (videoAct.isNextIndicatorLoaderDisplay())
			return;
		videoView.start();
	}

	public void seekToVideo(long currentPosition) {
		videoView.seekTo((int) currentPosition);
	}

	public int getBufferPercentage() {
		return videoView.getBufferPercentage();
	}

	public void releasePlayer() {
	}

	public boolean isPlaying() {
		return videoView != null && videoView.isPlaying();
	}

	public void startVideo() {
		videoView.start();
	}
}
