package com.hungama.myplay.activity.util;

import android.content.Context;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.MediaController;
import android.widget.Toast;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.audio.AudioTrack;
import com.google.android.exoplayer.demo.player.DemoPlayer;
import com.google.android.exoplayer.demo.player.DemoPlayer.RendererBuilder;
import com.google.android.exoplayer.demo.player.ExtractorRendererBuilder;
import com.google.android.exoplayer.demo.player.HlsRendererBuilder;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.metadata.GeobMetadata;
import com.google.android.exoplayer.metadata.PrivMetadata;
import com.google.android.exoplayer.metadata.TxxxMetadata;
import com.google.android.exoplayer.util.PlayerControl;
import com.google.android.exoplayer.util.Util;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.player.PlayMode;
import com.hungama.myplay.activity.player.PlayerService;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Map;

public class ExoMusicPlayer implements MusicPlayerFunctions, DemoPlayer.Listener, DemoPlayer.Id3MetadataListener, DemoPlayer.InternalErrorListener {

    public DemoPlayer exo_player;
    String TAG = "ExoVideoPlayer";
    long exo_playerPosition = 0;
    public static final int TYPE_HLS = 2;
    public static final int TYPE_OTHER = 3;

    private MediaController exo_mediaController;
    private boolean exo_playerNeedsPrepare;
    private int exo_contentType;

    private Context context;

    private MusicPlayerListner.MyMusicOnPreparedListener preparedListener;
    private MusicPlayerListner.MyMusicOnBufferingUpdateListener bufferingUpdateListener;
    private MusicPlayerListner.MyMusicOnCompletionListener onCompletionListener;
    private MusicPlayerListner.MyMusicOnErrorListener onErrorListener;

    public ExoMusicPlayer(Context context) {
        this.context = context;
        Logger.i("MediaPlayer","Selected MediaPlayer:ExoMusicPlayer");
        handle = PlayerService.service.handlerChromeCast;
    }

    @Override
    public void init(Context context/*Object obj, VideoActivity act, View mainView, MusicPlayerListner listner*/) {
        long trackId = 0;
        this.context = context;
        handle = new Handler();
        //initializeExoPlayerComponents();
    }

    private boolean isPlayerAvailable() {
        return exo_player != null;
    }

    @Override
    public void prepare() {
        //if (isPlayerAvailable())
        try {
            exoPreparePlayer(url, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        if (isPlayerAvailable())
            try {
                exoPlayerStart();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void pause() {
        if (isPlayerAvailable())
            try {
                exoPlayerPause();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void stop() {
        if (isPlayerAvailable())
            try {
                releasePlayer();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void release() {
        if (isPlayerAvailable())
            try {
                releasePlayer();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void reset() {
        if (isPlayerAvailable())
            try {
                //reset();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public int getCurrentPosition() {
        if (exo_player == null)
            return (int) exo_playerPosition;
        return (int) exo_player.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        if (isPlayerAvailable())
            try {
                return (int) (exo_player.getDuration());
            } catch (Exception e) {
            }
        else if(((int)exo_playerPosition)>0){
            return (int) exo_playerPosition;
        }
        return 0;
    }

    @Override
    public boolean isPlaying() {
        if (isPlayerAvailable()) {
            PlayerControl control = getExoPlayerControl();
            return exo_player != null && control != null && control.isPlaying();
        }
        return false;
    }

    @Override
    public int getPlayState() {
        if (isPlayerAvailable())
            try {
                //currentPlayer.getPlayerState();
            } catch (Exception e) {
                e.printStackTrace();
            }
        return 0;
    }

    @Override
    public void seekTo(int timeMilliseconds) {
        if (isPlayerAvailable())
            try {
                //getExoPlayerControl().seekTo(timeMilliseconds);
                exo_player.seekTo(timeMilliseconds);
                //getExoPlayerControl().start();

                /*exo_player.seekTo(timeMilliseconds);
                exoPlayerStart();*/
                //if(!isPlaying()){
                //exo_player.prepare();
                //exo_player.setPlayWhenReady(true);
                //}
            } catch (Exception e) {
                e.printStackTrace();
            }

    }

    @Override
    public void setWakeMode(Context context, int value) {
        /*if (isPlayerAvailable())
            try {
                currentPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
            } catch (Exception e) {
                e.printStackTrace();
            }*/
    }

    @Override
    public void setOnPreparedListener(MusicPlayerListner.MyMusicOnPreparedListener listener) {
        preparedListener = listener;

    }

    @Override
    public void setOnBufferingUpdateListener(MusicPlayerListner.MyMusicOnBufferingUpdateListener listener) {
        bufferingUpdateListener = listener;
        /*if (isPlayerAvailable())
            currentPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    if (bufferingUpdateListener != null)
                        bufferingUpdateListener.onBufferingUpdate(currentPlayer, percent);
                }
            });*/
    }

    @Override
    public void setOnCompletionListener(MusicPlayerListner.MyMusicOnCompletionListener listener) {
        onCompletionListener = listener;
        /*if (isPlayerAvailable()) {
            currentPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (onCompletionListener != null)
                        onCompletionListener.onCompletion(currentPlayer);
                }
            });
        }*/
    }

    @Override
    public void setOnErrorListener(MusicPlayerListner.MyMusicOnErrorListener listener) {
        this.onErrorListener = listener;
        /*if (isPlayerAvailable()) {
            currentPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if (onErrorListener != null) {
                        onErrorListener.onError(currentPlayer, what, extra);
                    }
                    return false;
                }
            });
        }*/
    }

    String url;

    @Override
    public void setDataSource(String url) {
        if (!url.contains("http") && !url.contains("file://")) {
            url = "file://" + url;
        }
        this.url = url;
        /*if (isPlayerAvailable())
            try {
                currentPlayer.setDataSource(url);
            } catch (Exception e) {
                e.printStackTrace();
            }*/
    }

    @Override
    public void setDataSource(FileDescriptor desc) {
        /*if (isPlayerAvailable())
            try {
                currentPlayer.setDataSource(desc);
            } catch (Exception e) {
                e.printStackTrace();
            }*/
    }

    @Override
    public void setAudioStreamType(int value) {
        /*if (isPlayerAvailable())
            try {
                currentPlayer.setAudioStreamType(value);
            } catch (Exception e) {
                e.printStackTrace();
            }*/
    }

    @Override
    public void prepareAsync(MusicPlayerListner.MyMusicOnPreparedListener listener) {
        this.preparedListener = listener;
        //if (isPlayerAvailable()) {
        exoPreparePlayer(url, true);
            /*currentPlayer.prepareAsync(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (preparedListener != null)
                        preparedListener.onPrepared(currentPlayer);
                }
            });*/
        //}
    }

    @Override
    public int getAudioSessionId() {
        if (isPlayerAvailable())
            try {
                //return currentPlayer.getAudioSessionId();
            } catch (Exception e) {
                e.printStackTrace();
            }
        return 0;
    }

    @Override
    public void setTrackId(long trackId) {
        if (isPlayerAvailable())
            try {
                //currentPlayer.setTrackId(trackId);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void setPlayState(int state) {
        if (isPlayerAvailable())
            try {
                //currentPlayer.setPlayerState(state);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }


    private void initializeExoPlayerComponents() {
        exo_contentType = TYPE_OTHER;
        exo_mediaController = new MediaController(context);
    }

    public void seekToVideo(long currentPosition) {
        if (exo_player != null) {
            exo_player.seekTo(currentPosition);
        }
    }

    public int getBufferPercentage() {
        PlayerControl control = getExoPlayerControl();
        if (control != null)
            return getExoPlayerControl().getBufferPercentage();
        return 0;
    }

    public PlayerControl getExoPlayerControl() {
        if (exo_player != null)
            return exo_player.getPlayerControl();
        return null;
    }

    public void exoPlayerPause() {
        PlayerControl control = getExoPlayerControl();
        if (control != null)
            control.pause();
    }

    public void exoPlayerStart() {
        PlayerControl control = getExoPlayerControl();
        if (control != null)
            control.start();
    }

    public void releasePlayer() {
        if (exo_player != null) {
            exo_playerPosition = exo_player.getCurrentPosition();
            exo_player.release();
            exo_player = null;
            // exo_playerNeedsPrepare = true;
        }
        handle.removeCallbacks(runBuffer);
    }

    public RendererBuilder getRendererBuilder(String path) {
        String userAgent = Util.getUserAgent(context, context.getString(R.string.application_name));
        /*if (path.contains("http") && !videoAct.isAdLoading && !videoAct.isAdPlaying)
            exo_contentType = TYPE_HLS;
        else*/
        exo_contentType = TYPE_OTHER;
        if(Utils.isNeedToUseHLSForMusic() && path.contains("m3u8"))
            exo_contentType = TYPE_HLS;
        switch (exo_contentType) {
            case TYPE_HLS:
                return new HlsRendererBuilder(context, userAgent, path);
            default:
                return new ExtractorRendererBuilder(context, userAgent, Uri.parse(path));
            //return new DefaultRendererBuilder(videoAct, Uri.parse(videoPath),exo_debugTextView);
        }
    }

//    // Internal methods
//    public RendererBuilder getRendererBuilder(String path) {
//        String userAgent = Util.getUserAgent(context, context.getString(R.string.application_name));
//        /*if (path.contains("http") && !videoAct.isAdLoading && !videoAct.isAdPlaying)
//            exo_contentType = TYPE_HLS;
//        else*/
//
//        exo_contentType = TYPE_OTHER;
//        switch (exo_contentType) {
//            case TYPE_HLS:
//                return new HlsRendererBuilder(context, userAgent, path);
//            default:
//                return new ExtractorRendererBuilder(context, userAgent, Uri.parse(path));
//            //return new DefaultRendererBuilder(videoAct, Uri.parse(videoPath),exo_debugTextView);
//        }
//    }

    public void exoPreparePlayer(final String link, final boolean isNewSong) {

        if(TextUtils.isEmpty(url) || TextUtils.isEmpty(link))
            return;

        handle.post(new Runnable() {
            @Override
            public void run() {
                Logger.i("exoPreparePlayer", "exoPreparePlayer: VideoPath:" + link);
                // if (exo_player == null) {
                releasePlayer();
                if (isNewSong)
                    exo_playerPosition = 0;

                exo_player = new DemoPlayer(getRendererBuilder(link));
                exo_player.addListener(ExoMusicPlayer.this);
                //exo_player.setCaptionListener(this);
                exo_player.setMetadataListener(ExoMusicPlayer.this);
                exo_player.seekTo(exo_playerPosition);
                exo_playerNeedsPrepare = true;
                if(exo_mediaController==null) {
                    Logger.i("ExoPlayerPrepare","exo_mediaController is Null");
                    initializeExoPlayerComponents();
                }else{
                    Logger.i("ExoPlayerPrepare","exo_mediaController is not Null");
                }
                exo_mediaController.setMediaPlayer(exo_player.getPlayerControl());
                exo_mediaController.setEnabled(true);
                // eventLogger = new EventLogger();
                // eventLogger.startSession();
                // player.addListener(eventLogger);
                // player.setInfoListener(eventLogger);

                //exo_player.setInternalErrorListener(ExoMusicPlayer.this);
                // }
                if (exo_playerNeedsPrepare) {
                    exo_player.prepare();
                    exo_playerNeedsPrepare = false;
                }
                // if (!isNextIndicatorLoaderDisplay())
                exo_player.setPlayWhenReady(true);
            }
        });

    }

    private Handler handle;
    // DemoPlayer.Listener implementation
    boolean isPrepare = false;

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
            // showControls();
            Utils.clearCache(true);
            if (onCompletionListener != null)
                onCompletionListener.onCompletion(this);
            this.url = null;
            releasePlayer();
            /*if (videoAct.isAdLoading || videoAct.isAdPlaying) {
                Utils.clearCache(true);
                releasePlayer();
                videoAct.adCompletion(false);
            } else {
                videoAct.onCompletion(null);
            }*/
        }
        String text = "playWhenReady=" + playWhenReady + ", playbackState=";
        Logger.i("ExoPlayerState", "ExoPlayerState1:" + text);
        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                /*if (!videoAct.isNextIndicatorLoaderDisplay()) {
                    videoAct.pbVideo.setVisibility(View.VISIBLE);
                    videoAct.setMediaControlVisibility(false);
                }*/
                text += "buffering";
                break;
            case ExoPlayer.STATE_ENDED:
                text += "ended";
                stopBufferListner();
                break;
            case ExoPlayer.STATE_IDLE:
                text += "idle";
                stopBufferListner();
                break;
            case ExoPlayer.STATE_PREPARING:
                isPrepare = true;
                text += "preparing";
                /*if (playWhenReady && preparedListener!=null) {
                    preparedListener.onPrepared(this);
                }*/
                break;
            case ExoPlayer.STATE_READY:
                if (playWhenReady && preparedListener != null) {
                    preparedListener.onPrepared(this);
                    startBufferListner();
                }
                /*if (isPrepare && (videoAct.isAdLoading || videoAct.isAdPlaying)) {
                    videoAct.prepareAd(null);
                    isPrepare = false;
                } else {
                    videoAct.setReapatTag(false);
                    videoAct.pbVideo.setVisibility(View.GONE);
                    videoAct.playButton.setImageResource(R.drawable.ic_pause);
                }
                videoAct.updateProgressBar();*/
                text += "ready";
                break;
            default:
                text += "unknown";
                break;
        }
    }

    @Override
    public void onError(Exception e) {
        if (e instanceof UnsupportedDrmException) {
            // Special case DRM failures.
            UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
            int stringId = Util.SDK_INT < 18 ? R.string.drm_error_not_supported
                    : unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                    ? R.string.drm_error_unsupported_scheme : R.string.drm_error_unknown;
            Toast.makeText(context, stringId, Toast.LENGTH_LONG).show();
        }
        Logger.i("ExoVideoPlayer", "ExoVideoPlayer Error:" + e.toString());
        stopBufferListner();
        // if (video != null)
        // exoPreparePlayer(video.getVideoUrl(), true);
        exo_playerNeedsPrepare = true;
        /*if (videoAct != null)
            videoAct.errorAdLoad();*/
        if (onErrorListener != null)
            onErrorListener.onError(exo_player, 0, 0);
    }


    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                   float pixelWidthAspectRatio) {
    }

    @Override
    public void onId3Metadata(Map<String, Object> metadata) {
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            if (TxxxMetadata.TYPE.equals(entry.getKey())) {
                TxxxMetadata txxxMetadata = (TxxxMetadata) entry.getValue();
                Log.i(TAG, String.format("ID3 TimedMetadata %s: description=%s, value=%s",
                        TxxxMetadata.TYPE, txxxMetadata.description, txxxMetadata.value));
            } else if (PrivMetadata.TYPE.equals(entry.getKey())) {
                PrivMetadata privMetadata = (PrivMetadata) entry.getValue();
                Log.i(TAG, String.format("ID3 TimedMetadata %s: owner=%s",
                        PrivMetadata.TYPE, privMetadata.owner));
            } else if (GeobMetadata.TYPE.equals(entry.getKey())) {
                GeobMetadata geobMetadata = (GeobMetadata) entry.getValue();
                Log.i(TAG, String.format("ID3 TimedMetadata %s: mimeType=%s, filename=%s, description=%s",
                        GeobMetadata.TYPE, geobMetadata.mimeType, geobMetadata.filename,
                        geobMetadata.description));
            } else {
                Log.i(TAG, String.format("ID3 TimedMetadata %s", entry.getKey()));
            }
        }
    }

    //----------------Buffering calculation ---------------//

    private void startBufferListner(){
        stopBufferListner();
        //if (url.contains("http"))
        if(PlayerService.service!=null && PlayerService.service.getPlayMode() == PlayMode.MUSIC && handle!=null)
            handle.post(runBuffer);
    }

    private void stopBufferListner(){
        if(handle!=null)
            handle.removeCallbacks(runBuffer);
    }

    Runnable runBuffer = new Runnable() {
        @Override
        public void run() {
            if(exo_player!=null && bufferingUpdateListener!=null){
                int percentage = exo_player.getBufferedPercentage();
                Logger.i("ExoMusicPlayer","ExoMusicPlayer Buffer:"+percentage);
                bufferingUpdateListener.onBufferingUpdate(ExoMusicPlayer.this, percentage);
                if(percentage==100) {
                    stopBufferListner();
                }else {
                    handle.postDelayed(runBuffer, 1000);
                }
            }else{
                stopBufferListner();
            }
        }
    };

    @Override
    public void onRendererInitializationError(Exception e) {
        Logger.i("Internal Error","Error::::: onRendererInitializationError");
        if (onErrorListener != null)
            onErrorListener.onError(exo_player, 0, 0);
    }

    @Override
    public void onAudioTrackInitializationError(AudioTrack.InitializationException e) {
        Logger.i("Internal Error","Error::::: onAudioTrackInitializationError");
        if (onErrorListener != null)
            onErrorListener.onError(exo_player, 0, 0);
    }

    @Override
    public void onAudioTrackWriteError(AudioTrack.WriteException e) {
        Logger.i("Internal Error","Error::::: onAudioTrackWriteError");
    }

    @Override
    public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
        Logger.i("Internal Error","Error::::: onAudioTrackUnderrun");
    }

    @Override
    public void onDecoderInitializationError(MediaCodecTrackRenderer.DecoderInitializationException e) {
        Logger.i("Internal Error","Error::::: onDecoderInitializationError");
    }

    @Override
    public void onCryptoError(MediaCodec.CryptoException e) {
        Logger.i("Internal Error","Error::::: onCryptoError");
    }

    @Override
    public void onLoadError(int sourceId, IOException e) {
        Logger.i("Internal Error","Error::::: onLoadError");
        if (onErrorListener != null)
            onErrorListener.onError(exo_player, 0, 0);
    }

    @Override
    public void onDrmSessionManagerError(Exception e) {
        Logger.i("Internal Error","Error::::: onDrmSessionManagerError");
    }
}
