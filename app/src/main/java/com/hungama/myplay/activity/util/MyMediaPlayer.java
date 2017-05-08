package com.hungama.myplay.activity.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.PowerManager;

import com.hungama.myplay.activity.player.PlayerService;

import java.io.FileDescriptor;
import java.io.IOException;

public class MyMediaPlayer implements MusicPlayerFunctions {

    MyNormalMediaPlayer currentPlayer;
    Context context;
    //MusicPlayerListner listner;
    private MusicPlayerListner.MyMusicOnPreparedListener preparedListener;
    private MusicPlayerListner.MyMusicOnBufferingUpdateListener bufferingUpdateListener;
    private MusicPlayerListner.MyMusicOnCompletionListener onCompletionListener;
    private MusicPlayerListner.MyMusicOnErrorListener onErrorListener;
    //MusicPlayerListner.MyMusicOnPreparedListener preparedListener;

    public MyMediaPlayer(Context context){
        this.context =context;
        Logger.i("MediaPlayer","Selected MediaPlayer:MyMediaPlayer");
    }

    @Override
    public void init(Context context/*Object obj, VideoActivity act, View mainView, MusicPlayerListner listner*/) {
        long trackId = 0;
        this.context = context;
        //this.listner = listner;
        currentPlayer = new MyNormalMediaPlayer(trackId);
    }

    private boolean isPlayerAvailable() {
        return currentPlayer != null;
    }

    @Override
    public void prepare() {
        if (isPlayerAvailable())
            try {
                currentPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void start() {
        if (isPlayerAvailable())
            try {
                currentPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void pause() {
        if (isPlayerAvailable())
            try {
                currentPlayer.pause();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void stop() {
        if (isPlayerAvailable())
            try {
                currentPlayer.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void release() {
        if (isPlayerAvailable())
            try {
                currentPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void reset() {
        if (isPlayerAvailable())
            try {
                currentPlayer.reset();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public int getCurrentPosition() {
        int pos = 0;
        if (isPlayerAvailable())
            try {
                pos = currentPlayer.getCurrentPosition();
            } catch (Exception e) {
                e.printStackTrace();
            }
        return pos;
    }

    @Override
    public int getDuration() {
        int duration = 0;
        if (isPlayerAvailable())
            try {
                duration = currentPlayer.getDuration();
            } catch (Exception e) {
                e.printStackTrace();
            }
        return duration;
    }

    @Override
    public boolean isPlaying() {
        if (isPlayerAvailable())
            try {
                return currentPlayer.isPlaying();
            } catch (Exception e) {
                e.printStackTrace();
            }
        return false;
    }

    @Override
    public int getPlayState() {
        if (isPlayerAvailable())
            try {
                currentPlayer.getPlayerState();
            } catch (Exception e) {
                e.printStackTrace();
            }
        return 0;
    }

    @Override
    public void seekTo(int timeMilliseconds) {
        if (isPlayerAvailable())
            try {
                currentPlayer.seekTo(timeMilliseconds);
            } catch (Exception e) {
                e.printStackTrace();
            }

    }

    @Override
    public void setWakeMode(Context context, int value) {
        if (isPlayerAvailable())
            try {
                currentPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void setOnPreparedListener(MusicPlayerListner.MyMusicOnPreparedListener listener) {
        preparedListener = listener;

    }

    @Override
    public void setOnBufferingUpdateListener(MusicPlayerListner.MyMusicOnBufferingUpdateListener listener) {
        bufferingUpdateListener = listener;
        if (isPlayerAvailable())
            currentPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    if (bufferingUpdateListener != null)
                        bufferingUpdateListener.onBufferingUpdate(currentPlayer, percent);
                }
            });
    }

    @Override
    public void setOnCompletionListener(MusicPlayerListner.MyMusicOnCompletionListener listener) {
        onCompletionListener = listener;
        if (isPlayerAvailable()) {
            currentPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (onCompletionListener != null)
                        onCompletionListener.onCompletion(currentPlayer);
                }
            });
        }
    }

    @Override
    public void setOnErrorListener(MusicPlayerListner.MyMusicOnErrorListener listener) {
        this.onErrorListener = listener;
        if (isPlayerAvailable()) {
            currentPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if (onErrorListener != null) {
                        onErrorListener.onError(currentPlayer, what, extra);
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void setDataSource(String url) {
        if (isPlayerAvailable())
            try {
                currentPlayer.setDataSource(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void setDataSource(FileDescriptor desc) {
        if (isPlayerAvailable())
            try {
                currentPlayer.setDataSource(desc);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void setAudioStreamType(int value) {
        if (isPlayerAvailable())
            try {
                currentPlayer.setAudioStreamType(value);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void prepareAsync(MusicPlayerListner.MyMusicOnPreparedListener listener) {
        this.preparedListener = listener;
        if (isPlayerAvailable()) {
            currentPlayer.prepareAsync(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (preparedListener != null)
                        preparedListener.onPrepared(currentPlayer);
                }
            });
        }
    }

    @Override
    public int getAudioSessionId() {
        if (isPlayerAvailable())
            try {
                return currentPlayer.getAudioSessionId();
            } catch (Exception e) {
                e.printStackTrace();
            }
        return 0;
    }

    @Override
    public void setTrackId(long trackId) {
        if (isPlayerAvailable())
            try {
                currentPlayer.setTrackId(trackId);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void setPlayState(int state) {
        if (isPlayerAvailable())
            try {
                currentPlayer.setPlayerState(state);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }


	/*@Override
    public void setAudioStreamType(int id) {
		if(isPlayerAvailable())
			try {
				currentPlayer.setAudioStreamType(id);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}*/

    private class PlayerNotReadyException extends IllegalStateException {

    }

    private class MyNormalMediaPlayer extends MediaPlayer {

        private String url;
        // long timestmp;
        private int playerState;
        long trackId;
        boolean needToAutoPlayAfterPrepare = false;

        public MyNormalMediaPlayer(long trackId) {
            this.trackId = trackId;
        }

        public void setTrackId(long trackId) {
            this.trackId = trackId;
        }

        @Override
        public void reset() {
            playerState = PlayerService.PlayerState.STATE_NEW;
            url = "";
            trackId = 0;
            super.reset();
        }

        @Override
        public void setDataSource(String path) throws IOException,
                IllegalArgumentException, SecurityException,
                IllegalStateException {
            url = path;
            playerState = PlayerService.PlayerState.STATE_NEW;
            super.setDataSource(path);
        }

        @Override
        public void setDataSource(FileDescriptor path) throws IOException,
                IllegalArgumentException, SecurityException,
                IllegalStateException {
            // url = path;
            playerState = PlayerService.PlayerState.STATE_NEW;
            super.setDataSource(path);
        }

        public void prepareAsync(final OnPreparedListener listner) throws IllegalStateException {
            if (this.playerState != PlayerService.PlayerState.STATE_PREPARING) {
                playerState = PlayerService.PlayerState.STATE_PREPARING;
                Logger.i("prepareAsync", "prepareAsync:1");
                setOnPreparedListener(new OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        Logger.i("prepareAsync", "prepareAsync:2");
                        playerState = PlayerService.PlayerState.STATE_PREPARED;
                        listner.onPrepared(mp);
                        setOnPreparedListener(null);
                    }
                });
                super.prepareAsync();
                Logger.i("prepareAsync", "prepareAsync:3");
            }
        }

        @Override
        public void prepare() throws IOException, IllegalStateException {
            // if (needToPrepareMediaPlayer(this)) {
            if (this.playerState != PlayerService.PlayerState.STATE_PREPARING) {
                playerState = PlayerService.PlayerState.STATE_PREPARING;
                super.prepare();
                playerState = PlayerService.PlayerState.STATE_PREPARED;
                // if (needToAutoPlayAfterPrepare) {
                // start();
                // }
            } else {
            }

            // }
        }

        @Override
        public void start() throws IllegalStateException {
            /*if (PlayerService.service.needToPlayCacheMediaPlayer(this) || PlayerService.service.isAdPlaying()
					|| playerState == PlayerState.STATE_PAUSED) {*/
            //service.stopUnusedPlayer();
            super.start();
            playerState = PlayerService.PlayerState.STATE_PLAYING;
			/*} else {
				throw new PlayerNotReadyException();
			}*/
        }

        @Override
        public void stop() throws IllegalStateException {
            try {
                reset();
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.stop();
        }

        @Override
        public void pause() throws IllegalStateException {
            if (playerState == PlayerService.PlayerState.STATE_PLAYING) {
                super.pause();
                playerState = PlayerService.PlayerState.STATE_PAUSED;
            }
        }

        public int getPlayerState(){
            return playerState;
        }

        public void setPlayerState(int state){
            playerState = state;
        }
    }
}
