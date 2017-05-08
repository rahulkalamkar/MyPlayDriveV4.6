package com.hungama.myplay.activity.util;

public interface MusicPlayerListner {

    public interface MyMusicOnPreparedListener {
        public void onPrepared(Object mp);
    }

    public interface MyMusicOnBufferingUpdateListener {
        public void onBufferingUpdate(Object mp, int percent);
    }


    public interface MyMusicOnCompletionListener {
        public void onCompletion(Object mp);
    }

    public interface MyMusicOnErrorListener {
        public boolean onError(Object mp, int what, int extra);
    }

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
