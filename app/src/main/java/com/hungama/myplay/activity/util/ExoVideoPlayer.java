package com.hungama.myplay.activity.util;

import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer.ExoPlayer;
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
import com.hungama.myplay.activity.ui.VideoActivity;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Map;

public class ExoVideoPlayer implements VideoPlayerFunctions, SurfaceHolder.Callback, DemoPlayer.Listener, /*DemoPlayer.CaptionListener,*/DemoPlayer.Id3MetadataListener/*,
        AudioCapabilitiesReceiver.Listener*/ {

    VideoActivity videoAct;
    public DemoPlayer exo_player;
    String TAG = "ExoVideoPlayer";
    long exo_playerPosition = 0;
    public static final int TYPE_DASH = 0;
    public static final int TYPE_SS = 1;
    public static final int TYPE_HLS = 2;
    public static final int TYPE_OTHER = 3;

    private MediaController exo_mediaController;
    public SurfaceView exo_surfaceView;
    private TextView exo_debugTextView;
    private boolean exo_playerNeedsPrepare;
    private boolean exo_enableBackgroundAudio;
    private int exo_contentType;
    private String exo_contentId = "1";

    @Override
    public int getDuration() {
        try {
            return (int) (exo_player.getDuration());
        } catch (Exception e) {
        }
        return 0;
    }

    @Override
    public long getCurrentPosition() {
        if (exo_player == null)
            return exo_playerPosition;
        return exo_player.getCurrentPosition();
    }

    @Override
    public void init(Object obj, VideoActivity act, View mainView) {
        this.exo_surfaceView = (SurfaceView) obj;
        videoAct = act;
        initializeExoPlayerComponents(mainView);
    }

    private static final CookieManager defaultCookieManager;
    static {
        defaultCookieManager = new CookieManager();
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private void initializeExoPlayerComponents(View mainView) {
        // exo_contentUri = Uri
        // .parse("http://hungcomiosta-vh.akamaihd.net/i/VideoS3/r/ms2/5946489/3/811/Aao-Raja_,80000,160000,321000,884000,1328000,.mp4.csmil/master.m3u8?hdnea=exp=1430733664~acl=/i/VideoS3/r/ms2/5946489/3/811/Aao-Raja_,80000,160000,321000,884000,1328000,.mp4.csmil/*~hmac=57d01d47b5e82641be9ad55acfbf926a10266359c8cb11fcd9252437cb43be49");
        exo_contentType = TYPE_HLS;
        View root = mainView.findViewById(R.id.root);
        exo_surfaceView = (SurfaceView) mainView
                .findViewById(R.id.videoview_video_surface_view);
        exo_surfaceView.getHolder().addCallback(this);
        exo_debugTextView = (TextView) mainView
                .findViewById(R.id.debug_text_view);

        // exo_playerStateTextView = (TextView)
        // findViewById(R.id.player_state_view);

        // exo_subtitleView = (SubtitleView) findViewById(R.id.subtitles);
        exo_mediaController = new MediaController(videoAct);
        exo_mediaController.setAnchorView(root);
        // exo_retryButton = (Button) findViewById(R.id.retry_button);
        // exo_retryButton.setOnClickListener(this);
        CookieHandler currentHandler = CookieHandler.getDefault();
        if (currentHandler != defaultCookieManager) {
            CookieHandler.setDefault(defaultCookieManager);
        }
        exo_surfaceView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean needToShowPlayBtn = true;
                if (!videoAct.needToEnableTouch())
                    needToShowPlayBtn = false;

                if (videoAct.isAdLoading && !videoAct.isAdPlaying)
                    return false;

                if (videoAct.isAdPlaying) {
                    Utils.performclickEvent(videoAct, videoAct.placementVideoAd);
                } else if (videoAct.videoControllersBar.getVisibility() == View.VISIBLE) {
                    videoAct.setMediaControlVisibility(false);
                } else {
                    videoAct.setMediaControlVisibility(true);
                    videoAct.updateControllersVisibilityThread();
                }
                if (!needToShowPlayBtn && videoAct.playButton != null) {
                    videoAct.playButton.setVisibility(View.INVISIBLE);
                }
                return false;
            }
        });
    }

    public void setDefaultCookieManager(){
        CookieHandler currentHandler = CookieHandler.getDefault();
        if (currentHandler != defaultCookieManager) {
            CookieHandler.setDefault(defaultCookieManager);
        }
    }

    private void error(Exception e) {
        Logger.i("ExoPlayer", "ExoVideoPlayer Error" + e);
    }

    public void pauseVideo() {
        exoPlayerPause();
    }

    public void startVideo(boolean isNewSong) {
        if (videoAct.isNextIndicatorLoaderDisplay())
            return;
        exoPlayerStart(isNewSong);
    }

    public void seekToVideo(long currentPosition) {
        if (exo_player != null) {
            exo_player.seekTo(currentPosition);
            exo_player.setSurface(exo_surfaceView.getHolder().getSurface());
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

    public void exoPlayerStart(Boolean isNewSong) {
        try {
            if (videoAct.isAdLoading || videoAct.isAdPlaying) {
                exoPreparePlayer(videoAct.placementVideoAd.get3gpVideo(), true);
            } else if (!(isPlaying()))
                exoPreparePlayer(videoAct.video.getVideoUrl(), isNewSong);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exoPlayerPause() {
        PlayerControl control = getExoPlayerControl();
        if (control != null)
            control.pause();
    }

    public void releasePlayer() {

        if (exo_player != null) {
            exo_playerPosition = exo_player.getCurrentPosition();
            exo_player.release();
            exo_player = null;
            // exo_playerNeedsPrepare = true;
        }
    }

    // public void exoOnResume() {
    // if (exo_player == null) {
    // if (videoAct.video != null)
    // exoPreparePlayer(videoAct.video.getVideoUrl(), false);
    // } else if (exo_player != null) {
    // exo_player.setBackgrounded(false);
    // }
    // }

    // public void exoOnPause() {
    // if (!exo_enableBackgroundAudio) {
    // releasePlayer();
    // } else {
    // exo_player.setBackgrounded(true);
    // }
    // }

    // public void exoOnDestroy() {
    // releasePlayer();
    // }

    // Internal methods
    public RendererBuilder getRendererBuilder(String videoPath) {
        String userAgent =  Util.getUserAgent(videoAct, videoAct.getString(R.string.application_name));
        if (videoPath.contains("http") && !videoAct.isAdLoading && !videoAct.isAdPlaying)
            exo_contentType = TYPE_HLS;
        else
            exo_contentType = TYPE_OTHER;
        switch (exo_contentType) {
            case TYPE_HLS:
                //return new HlsRendererBuilder(userAgent, videoPath, exo_contentId);
                return new HlsRendererBuilder(videoAct, userAgent, videoPath);
            default:
                return new ExtractorRendererBuilder(videoAct, userAgent, Uri.parse(videoPath));
                //return new DefaultRendererBuilder(videoAct, Uri.parse(videoPath),exo_debugTextView);
        }
    }

    public void exoPreparePlayer(String videoPath, boolean isNewSong) {
        if (videoAct.isNextIndicatorLoaderDisplay())
            return;
        if (isPlaying() && (!videoAct.isAdPlaying && !videoAct.isAdLoading))
            return;
        Logger.i("exoPreparePlayer", "exoPreparePlayer: VideoPath:" + videoPath);
        // if (exo_player == null) {
        releasePlayer();
        if (isNewSong)
            exo_playerPosition = 0;

        exo_player = new DemoPlayer(getRendererBuilder(videoPath));
        exo_player.addListener(this);
        //exo_player.setCaptionListener(this);
        exo_player.setMetadataListener(this);
        exo_player.seekTo(exo_playerPosition);
        exo_playerNeedsPrepare = true;
        exo_mediaController.setMediaPlayer(exo_player.getPlayerControl());
        exo_mediaController.setEnabled(true);
        // eventLogger = new EventLogger();
        // eventLogger.startSession();
        // player.addListener(eventLogger);
        // player.setInfoListener(eventLogger);
        //exo_player.setInternalErrorListener(internalError);
        // }
        if (exo_playerNeedsPrepare) {
            exo_player.prepare();
            exo_playerNeedsPrepare = false;
            updateButtonVisibilities();
        }
        exo_player.setSurface(exo_surfaceView.getHolder().getSurface());
        // if (!isNextIndicatorLoaderDisplay())
        exo_player.setPlayWhenReady(true);
    }

    public void refreshSurfaceView() {
        exo_player.setSurface(exo_surfaceView.getHolder().getSurface());
        exo_surfaceView.invalidate();
        //exo_surfaceView.setZOrderOnTop(false);
        //exo_player.n
    }

    // DemoPlayer.Listener implementation
    boolean isPrepare = false;

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
            // showControls();
            if (videoAct.isAdLoading || videoAct.isAdPlaying) {
                Utils.clearCache(true);
                releasePlayer();
                videoAct.adCompletion(false);
            } else {
                /*RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                exo_surfaceView.setLayoutParams(lp);*/
                //if(videoRate==0 || videoRate == 1.0){
                //videoRate = (float)videoAct.getScreenWidth()/videoAct.getScreenHeight();
                //}
                /*if(videoRate!=0 && videoRate!=1.0)
                   exo_surfaceView.setVideoWidthHeightRatio(videoRate);*/
                videoAct.onCompletion(null);
                // stopVideoPlayEvent(false, (int) mCurrentPosition);
            }
        }
        String text = "playWhenReady=" + playWhenReady + ", playbackState=";
        Logger.i("ExoPlayerState", "ExoPlayerState1:" + text);
        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                if (!videoAct.isNextIndicatorLoaderDisplay()) {
                    videoAct.pbVideo.setVisibility(View.VISIBLE);
                    videoAct.setMediaControlVisibility(false);
                }
                text += "buffering";
                break;
            case ExoPlayer.STATE_ENDED:
                text += "ended";
                break;
            case ExoPlayer.STATE_IDLE:
                text += "idle";
                break;
            case ExoPlayer.STATE_PREPARING:
                isPrepare = true;
                text += "preparing";
                if (!videoAct.isAdLoading && !videoAct.isAdPlaying) {
                    videoAct.setReapatTag(false);
                    videoAct.playButton.setImageResource(R.drawable.ic_pause);
                    if (playWhenReady) {
                        videoAct.loadBlurImgBitmap();
                        videoAct.startVideoPlayEvent();
                    } else {
                        videoAct.mApplicationConfigurations
                                .increaseVideoPlayBackCounter();
                    }
                }
                break;
            case ExoPlayer.STATE_READY:
                if (isPrepare && (videoAct.isAdLoading || videoAct.isAdPlaying)) {
                    videoAct.prepareAd(null);
                    isPrepare = false;
                } else {
                    videoAct.setReapatTag(false);
                    videoAct.pbVideo.setVisibility(View.GONE);
                    videoAct.playButton.setImageResource(R.drawable.ic_pause);
                }
                videoAct.updateProgressBar();
                text += "ready";
                break;
            default:
                text += "unknown";
                break;
        }
        Logger.i("ExoPlayerState", "ExoPlayerState:" + text);
        // exo_playerStateTextView.setText(text);
        updateButtonVisibilities();
    }

    @Override
    public void onError(Exception e) {
        if (e instanceof UnsupportedDrmException) {
            // Special case DRM failures.
            UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
            int stringId = Util.SDK_INT < 18 ? R.string.drm_error_not_supported
                    : unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                    ? R.string.drm_error_unsupported_scheme : R.string.drm_error_unknown;
            Toast.makeText(videoAct, stringId, Toast.LENGTH_LONG).show();
        }
        Logger.i("ExoVideoPlayer", "ExoVideoPlayer Error:" + e.toString());
        // if (video != null)
        // exoPreparePlayer(video.getVideoUrl(), true);
        exo_playerNeedsPrepare = true;
        updateButtonVisibilities();
        showControls();
        // if (videoAct.isAdLoading || videoAct.isAdPlaying) {
        if (videoAct != null)
            videoAct.errorAdLoad();
        // }else{
        // onResume();
        // }
    }

    float videoRate = 0;

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                   float pixelWidthAspectRatio) {
        /*shutterView.setVisibility(View.GONE);
        videoFrame.setAspectRatio(
                height == 0 ? 1 : (width * pixelWidthAspectRatio) / height);*/
    }


    // User controls

    public void updateButtonVisibilities() {
        // exo_retryButton.setVisibility(exo_playerNeedsPrepare ? View.VISIBLE
        // : View.GONE);
    }

    public void showControls() {
        // exo_mediaController.show(0);
        // exo_debugRootView.setVisibility(View.VISIBLE);
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

    // SurfaceHolder.Callback implementation

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (exo_player != null) {
            exo_player.setSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // Do nothing.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (exo_player != null) {
            exo_player.blockingClearSurface();
        }
    }

    public boolean isPlaying() {
        PlayerControl control = getExoPlayerControl();
        return exo_player != null && control != null && control.isPlaying();
    }

    public void startVideo() {
        PlayerControl control = getExoPlayerControl();
        if (control != null)
            control.start();
    }

    /*@Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
        if (exo_player == null) {
            return;
        }
        boolean backgrounded = exo_player.getBackgrounded();
        boolean playWhenReady = exo_player.getPlayWhenReady();
        releasePlayer();
        preparePlayer(playWhenReady);
        exo_player.setBackgrounded(backgrounded);
    }*/

}
