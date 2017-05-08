package com.hungama.myplay.activity.player;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.media.RemoteControlClient.MetadataEditor;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RemoteViews;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hungama.hungamamusic.ford.carmode.LockScreenActivity;
import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.HungamaApplication;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.ThreadPoolManager;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.NoConnectivityException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.audiocaching.CMEncryptor;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.DataBase.CacheState;
import com.hungama.myplay.activity.data.audiocaching.MediaCachingTaskNew;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.campaigns.PlacementType;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.dao.hungama.Discover;
import com.hungama.myplay.activity.data.dao.hungama.LiveStationDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaTrackDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.events.PlayEvent;
import com.hungama.myplay.activity.data.events.PlayEvent.PlayingSourceType;
import com.hungama.myplay.activity.operations.catchmedia.CMDecoratorOperation;
import com.hungama.myplay.activity.operations.catchmedia.MediaHandleOperation;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.NotificationActivity;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.ApsalarEvent;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.ExoMusicPlayer;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.MusicPlayerFunctions;
import com.hungama.myplay.activity.util.MusicPlayerListner;
import com.hungama.myplay.activity.util.MyMediaPlayer;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.hungama.myplay.activity.util.SleepModeManager;
import com.hungama.myplay.activity.util.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

public class PlayerService extends Service implements
		OnAudioFocusChangeListener, MusicPlayerListner.MyMusicOnErrorListener, MusicPlayerListner.MyMusicOnCompletionListener,MusicPlayerListner.MyMusicOnBufferingUpdateListener
		/*MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener , CastPlayback.Callback*/ {

	private static final String TAG = "PlayerService";
	public static final String TRACK_FINISHED = "track_finished";
	public static final String ACTION_REMOVED_TRACK = "TrackRemoved";

	private boolean isErrorOccured = false;

	private static int AudioAdCount = 0;
	// public static final int AUDIO_AD_LIMIT = 3;
	private static boolean needToLoadAudioAd = false;
	/*private Vector<MyMediaPlayer> playerVector = new Vector<MyMediaPlayer>();
    private Vector<MyMediaPlayer> removedPlayerVector = new Vector<MyMediaPlayer>();*/
	public Discover mDiscover, prevDiscover;

	public boolean isPlayerLoading = false;

	public enum State {

		/**
		 * When the Player is created and no {@link Track} is registered to be
		 * played.
		 */
		IDLE,

		/**
		 * When the selected {@link Track} is in the state of being loaded
		 * before playing.
		 */
		INTIALIZED,

		/**
		 * When the selected {@link Track} has been loaded, prepared and is
		 * ready to be played.
		 */
		PREPARED,

		/**
		 * When the selected {@link Track} is being played.
		 */
		PLAYING,

		/**
		 * When the selected {@link Track} is being paused.
		 */
		PAUSED,

		/**
		 * When the selected {@link Track} is being stopped and the whole
		 * process of loading this / new track is required.
		 */
		STOPPED,

		/**
		 * When the selected {@link Track} has been done playing due to
		 * completion of the track.
		 */
		COMPLETED,

		/**
		 * When the selected {@link PlayingQueue} has been done playing.
		 */
		COMPLETED_QUEUE;
	}

	public static PlayerService service;

	public enum LoopMode {
		OFF, ON, REAPLAY_SONG
	}

	public enum Error implements Serializable {
		NO_CONNECTIVITY(1), SERVER_ERROR(2), DATA_ERROR(3), TRACK_SKIPPED(4);

		private final int id;

		Error(int id) {
			this.id = id;
		}

		public int getId() {
			return this.id;
		}

		public static final Error getErrorById(int id) {
			if (id == NO_CONNECTIVITY.getId()) {
				return NO_CONNECTIVITY;
			} else if (id == SERVER_ERROR.getId()) {
				return SERVER_ERROR;
			} else if (id == TRACK_SKIPPED.getId()) {
				return TRACK_SKIPPED;
			} else {
				return DATA_ERROR;
			}
		}

	}

	/**
	 * Interface definition to be invoked when the state of the player has been
	 * changed.
	 */
	public interface PlayerStateListener {

		public void onStartLoadingTrack(Track track);

		public void onTrackLoadingBufferUpdated(Track track, int precent);

		public void onStartPlayingTrack(Track track);

		public void onFinishPlayingTrack(Track track);

		public void onFinishPlayingQueue();

		public void onSleepModePauseTrack(Track track);

		public void onErrorHappened(Error error);

		public void onStartPlayingAd(Placement audioad);

		public void onAdCompletion();
	}

	public class PlayerSericeBinder extends Binder {
		public PlayerService getService() {
			return PlayerService.this;
		}
	}

	private Context mContext;
	private String mCMServerUrl;
	// identification of the service in the system.
	private int mServiceStartId;

	private DataManager mDataManager;

	// binder for controlling the service from other components.
	private final IBinder mPlayerSericeBinder = new PlayerSericeBinder();

	// audio handler members:
	private AudioManager mAudioManager;
	private WakeLock mWakeLock;

	//private volatile MyMediaPlayer currentPlayer1;
	private volatile MusicPlayerFunctions currentPlayer;
	private volatile State mCurrentState;
	private volatile Track mCurrentTrack;

//	private Thread mMediaLoaderWorker = null;
	private MediaLoaderTask mMediaLoaderTask;
	private MediaLoaderHandler mMediaLoaderHandler = null;

	private ServiceHandler mServiceHandler;
	private Set<PlayerStateListener> mOnPlayerStateChangedListeners = new HashSet<PlayerStateListener>();

	private String mEventStartTimestamp = null;
	private String mEventStartTimestampForLiveRadio = null;

	// playing mode, identifies if playing music or radio. Deafult is Music.
	private volatile PlayMode mPlayMode = PlayMode.MUSIC;

	private volatile LoopMode mLoopMode = LoopMode.OFF;

	private SleepReciever mSleepReciever;
	private volatile boolean mShouldPauseAfterLoading = false;

	// shuffling - every day :)
	private boolean mIsShuffling = false;

	private PlayingQueue mPlayingQueue = null;
	private PlayingQueue mOriginalPlayingQueue = null;

	public static final int TIME_REPORT_BADGES_MILLIES = 120000;
	private long mReportedTrack = -1;

	private boolean mIsPausedByAudiofocusLoss = false;

	private volatile boolean mIsExplicitMarkedExit = false;

	// private final ThreadPoolExecutor mTracksMediaHandleExecutor = new
	// ThreadPoolExecutor(1, 1, 60L, TimeUnit.SECONDS,
	// new LinkedBlockingQueue<Runnable>());
	// private Future<?> mTracksMediaHandleExecutionRecord = null;

	private long mFileSize;
	// Calculate bandwidth
	private boolean firstEntry = true;
	private boolean lastEntry = true;
	private int percentStart;
	private long startTimeToCalculateBitrate;
	private long endTimeToCalculateBitrate;
	/*
	 * The Media Handle of any playing track should be updated after 30 minutes.
	 */
	// private static final long MEDIA_HANDLE_UPDATE_TIME_DELTA_MILLIS = 1000 *
	// 60 * 30;
	private static final long MEDIA_HANDLE_UPDATE_TIME_DELTA_MILLIS = 1000 * 60 * 9;

	ApplicationConfigurations mApplicationConfigurations;

	// in playing updater.
	private PlayerProgressCounter mPlayerProgressCounter;

	private PlayerBarFragment mPlayerBarFragment = null;

	private RadioBarUpdateListener mOnRadioBarUpdateListener;

	private boolean isAdPlaying = false;
	private int adSkipCount = 0;
	private Placement placementAudioAd = null;

	private PicassoUtil picasso;

	// ======================================================
	// Service life cycle.
	// ======================================================

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate() {
		super.onCreate();
		Logger.s(" ::::::::::::::::onCreate::::::::::::::::::");
		isPlayerLoading = false;
		AudioAdCount = 0;
		needToLoadAudioAd = false;
		mIsExplicitMarkedExit = false;
		try {
			TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
			if (tm.getCallState() != TelephonyManager.CALL_STATE_IDLE)
				CallInProgress = true;
			else
				CallInProgress = false;
		} catch (Exception e) {
			CallInProgress = false;
		}
		// creates binder to the service to interface between other controlling
		// components.
		service = this;
		mContext = getApplicationContext();
		mDataManager = DataManager.getInstance(getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();

		picasso = PicassoUtil.with(getApplicationContext());

		resetPlayerQueue();

		mCMServerUrl = DataManager.getInstance(getApplicationContext())
				.getServerConfigurations().getServerUrl();

		// initializing the audio manager to gain audio focus.
		mAudioManager = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);

		ComponentName mRemoteControlResponder = new ComponentName(
				getPackageName(), RemoteControlReceiver.class.getName());
		mAudioManager.registerMediaButtonEventReceiver(mRemoteControlResponder);
		registerRemoteControlClient();

		activityMediaControlReceiver = new ActivityMediaControlReceiver();
		IntentFilter filter = new IntentFilter(
				RemoteControlReceiver.ACTION_MEDIA_BUTTON);
		registerReceiver(activityMediaControlReceiver, filter);


		IntentFilter noiseFilter = new IntentFilter(
				AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		noisyAudioStreamReceiver = new NoisyAudioStreamReceiver();
		registerReceiver(noisyAudioStreamReceiver, noiseFilter);

		// creates a lock on the CPU to avoid the OS stops playing in state of
		// idling.
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this
				.getClass().getName());
		mWakeLock.setReferenceCounted(false);

		// initializes the service's handler.
		mServiceHandler = new ServiceHandler(this);

		// initializes the media player.
		initializeMediaPlayer();

		// registers a receiver for sleep requests.
		mSleepReciever = new SleepReciever(this);
		IntentFilter sleepFilter = new IntentFilter(
				SleepModeManager.COUNT_DOWN_TIMER_FINISH_INTENT);
		registerReceiver(mSleepReciever, sleepFilter);
		// FlurryAgent.setContinueSessionMillis(Constants.FLURRY_SESSION_LENGTH);
		// FlurryAgent.onStartSession(this, getString(R.string.flurry_app_key));
		Analytics.startSession(this);

		try {
			IntentFilter callFilter = new IntentFilter(
					TelephonyManager.ACTION_PHONE_STATE_CHANGED);
			registerReceiver(callReceiver, callFilter);
		} catch (Exception e) {
		}
		closeAppReceiver = new CloseAppReceiver();
		IntentFilter closeFilter = new IntentFilter(
				HomeActivity.ACTION_CLOSE_APP);
		registerReceiver(closeAppReceiver, closeFilter);
		//initializeChromeCast();
	}

	private NoisyAudioStreamReceiver noisyAudioStreamReceiver;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Logger.s(" ::::::::::::: onStartCommand :::::::::::: "
				+ mCurrentState);
		mServiceStartId = startId;
//		startServiceForeground(intent, flags, startId);
		return START_STICKY;
	}

//	public int startServiceForeground(Intent intent, int flags, int startId) {
//
//		Intent notificationIntent = new Intent(this, HomeActivity.class);
//		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
//
//		Notification notification = new NotificationCompat.Builder(this)
//				.setContentTitle("File Observer Service")
//				.setContentIntent(pendingIntent)
//				.setOngoing(true)
//				.build();
//
//		startForeground(300, notification);
//
//		return START_STICKY;
//	}

	@SuppressWarnings("deprecation")
	@Override
	public void onDestroy() {
		ComponentName component = new ComponentName(this,
				RemoteControlReceiver.class);
		mAudioManager.unregisterMediaButtonEventReceiver(component);
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
				mAudioManager
						.unregisterRemoteControlClient(myRemoteControlClient);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		if (mPlayMode == PlayMode.LIVE_STATION_RADIO) {
			stopLiveRadioUpdater();
		}

		try {
			unregisterReceiver(callReceiver);
			callReceiver = null;
		} catch (Exception e) {
		}

		Analytics.onEndSession(this);

		Logger.d(TAG, "Destroying the service.");

		// stops any playing / loading track.
		stop();

		// unregisters the receiver for sleep requests.
		unregisterReceiver(mSleepReciever);
		mSleepReciever = null;

		if (closeAppReceiver != null)
			unregisterReceiver(closeAppReceiver);
		mSleepReciever = null;

		// destroy the service's handler.
		mServiceHandler.removeCallbacksAndMessages(null);
		mServiceHandler = null;

		// destroy the media player.
		destroyMediaPlayer();

		// release the lock on the CPU.
		mWakeLock.release();
		mWakeLock = null;

//		dismissNotification();
		service = null;
		try {
			unregisterReceiver(activityMediaControlReceiver);
		} catch (Exception e) {
		}
		try {
			unregisterReceiver(noisyAudioStreamReceiver);
		} catch (Exception e) {
		}
		Logger.s(" ::::::::::::::onDestroy Playerservice:::::::::::::::::: ");
		if(widgetService!=null)
			stopService(widgetService);

		try {
			stopUnusedPlayer();
			//playerVector.clear();
		}catch (Exception e){}
		removeCastCallBack();
		updatewidget(true);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mPlayerSericeBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Logger.d(TAG, "Unbiding the service, destroy it!");

		if (isAllowSelfTermination()) {
			if (mPlayMode != PlayMode.LIVE_STATION_RADIO) {
				stopPrefetchingMediaHandles(true);
			}
			stopSelf(mServiceStartId);
		}

		return false;
	}

	// ======================================================
	// Playing Listeners and Callbacks.
	// ======================================================

	private static final String MESSAGE_VALUE = "message_value";
	private static final String MESSAGE_ERROR_VALUE = "message_error_value";

	/*
	 * starts loading track's media handle from CM servers to get the playing
	 * URL / internal path to play.
	 */
	private static final int MESSAGE_START_LOADING_TRACK = 1;
	/*
	 * indication for updating the buffer of the loading track before / while it
	 * been played.
	 */
	private static final int MESSAGE_LOADING_TRACK_BUFFER_UPDATE = 2;
	/*
	 * Indication of that the track that is been initially been loaded and ready
	 * to been played.
	 */
	private static final int MESSAGE_LOADING_TRACK_PREPARED = 3;
	/*
	 * Indication that the current loading track process has been cancelled,
	 * generally to play another track.
	 */
	private static final int MESSAGE_LOADING_TRACK_CANCELLED = 4;
	/*
	 * The track has been finished to being played, generally moving to the next
	 * track in the queue.
	 */
	private static final int MESSAGE_FINISH_PLAYING_TRACK = 5;
	/*
	 * Done playing all the queue of tracks.
	 */
	private static final int MESSAGE_FINISH_PLAYING_QUEUE = 6;
	/*
	 * Another application temporarlly requests the focus on the audio.
	 */
	private static final int MESSAGE_AUDIOFOCUS_LOSS_TRANSIENT = 7;
	/*
	 * The audio focus gained back to the player.
	 */
	private static final int MESSAGE_AUDIOFOCUS_GAIN = 8;
	/*
	 * No more audio focus to the player.
	 */
	private static final int MESSAGE_AUDIOFOCUS_LOSS = 9;
	/*
	 * An error has occurred.
	 */
	private static final int MESSAGE_ERROR = 10;

	private static final int MESSAGE_SKIP_CURRENT_TRACK = 11;
	private static final int MESSAGE_SKIP_TO_PREVIOUS_TRACK = 12;

	/**
	 * Handles all the service's components messages and performs the logic
	 * business.
	 */

	public static class PlayerState {
		public static int STATE_PLAYING = 0;
		public static int STATE_PREPARING = 1;
		public static int STATE_PREPARED = 4;
		public static int STATE_PAUSED = 2;
		public static int STATE_NEW = 5;
	}

	private static class ServiceHandler extends Handler {

		PlayerService service;

		public ServiceHandler(PlayerService service) {
			this.service = service;
		}

		@Override
		public void handleMessage(Message message) {
			try {
				int what = message.what;
				switch (what) {
				case MESSAGE_START_LOADING_TRACK:
					service.mCurrentState = State.INTIALIZED;
					service.isPlayerLoading = true;
					Track loadingTrack = (Track) message.getData()
							.getSerializable(MESSAGE_VALUE);
					for (PlayerStateListener listener : service.mOnPlayerStateChangedListeners) {
						listener.onStartLoadingTrack(loadingTrack);
					}
					service.updatePlaybackState(PLAYSTATE_BUFFERING);
					service.changeLockScreenBG.sendEmptyMessage(0);
					break;

				case MESSAGE_LOADING_TRACK_BUFFER_UPDATE:
					for (PlayerStateListener listener : service.mOnPlayerStateChangedListeners) {
						listener.onTrackLoadingBufferUpdated(null, message.arg1);
					}
					break;

				case MESSAGE_LOADING_TRACK_PREPARED:
					service.isPlayerLoading = false;
					/*
					 * Checks if we are pending to exit the application.
					 */
					if (service.mIsExplicitMarkedExit) {
						/*
						 * get out of here, the service will handle the Media
						 * Player's state.
						 */
						return;
					}

					if (service.CallInProgress /*&& !service.isCastRequire()*/)
						return;

					// starts playing the track.
					service.mCurrentState = State.PLAYING;

					try {
						if ((service.mPlayMode == PlayMode.LIVE_STATION_RADIO /*&& !service.isCastConnected()*/)
								|| (!service.currentPlayer.isPlaying() && service.currentPlayer.getPlayState() == PlayerState.STATE_PREPARED))
							service.currentPlayer.start();
					} catch (Exception e) {
						e.printStackTrace();
					}
					Logger.s(" ::::::::::::::TrackPrepared:::::::::::::::::::: ");
					service.updatewidget();


					service.updatePlaybackState(PLAYSTATE_PLAYING);

					// stores the timestamp
					if (service.isAdPlaying) {
						for (PlayerStateListener listener : service.mOnPlayerStateChangedListeners) {
							listener.onStartPlayingAd(service.placementAudioAd);
						}
					} else {
						service.startLoggingEvent();

						if (service.mPlayMode != PlayMode.LIVE_STATION_RADIO) {
							service.startPrefetchingMediaHandles();
						}

						Track preparedTrack = (Track) message.getData()
								.getSerializable(MESSAGE_VALUE);
						for (PlayerStateListener listener : service.mOnPlayerStateChangedListeners) {
							listener.onStartPlayingTrack(preparedTrack);
						}

						if (service.mShouldPauseAfterLoading) {
							// resets the flag.
							service.mShouldPauseAfterLoading = false;
							// pauses the the playing.
							service.pause();

							for (PlayerStateListener listener : service.mOnPlayerStateChangedListeners) {
								listener.onSleepModePauseTrack(preparedTrack);
							}
						}
					}
					break;

				case MESSAGE_LOADING_TRACK_CANCELLED:
					service.isPlayerLoading = false;
					Logger.s(" :::::::::::::::: MESSAGE_LOADING_TRACK_CANCELLED");
					if (service.isAdPlaying) {
						service.isErrorOccured = true;
						service.isAdPlaying = false;
						for (PlayerStateListener listener : service.mOnPlayerStateChangedListeners) {
							listener.onAdCompletion();
						}
						service.adSkipCount++;
						service.startLoadingTrack();
					} else {
						service.isErrorOccured = true;
						service.currentPlayer.stop();
						service.currentPlayer.reset();
						service.mCurrentState = State.STOPPED;
						for (PlayerStateListener listener : service.mOnPlayerStateChangedListeners) {
							listener.onFinishPlayingTrack(service.mCurrentTrack);
						}
						service.updatePlaybackState(PLAYSTATE_STOPPED);
					}
					break;

				case MESSAGE_FINISH_PLAYING_TRACK:
					service.isPlayerLoading = false;
					service.mCurrentState = State.COMPLETED;

					Track finishedTrack = (Track) message.getData()
							.getSerializable(MESSAGE_VALUE);
					for (PlayerStateListener listener : service.mOnPlayerStateChangedListeners) {
						listener.onFinishPlayingTrack(finishedTrack);
					}

					// play next track.

					if (service.mPlayMode == PlayMode.MUSIC
							&& service.mLoopMode == LoopMode.REAPLAY_SONG) {
						service.stop();
						service.play();

					} else {
						service.stop();
						service.next();
					}

					break;

				case MESSAGE_FINISH_PLAYING_QUEUE:
					service.isPlayerLoading = false;
					service.mCurrentState = State.COMPLETED_QUEUE;

					for (PlayerStateListener listener : service.mOnPlayerStateChangedListeners) {
						listener.onFinishPlayingQueue();
					}

					if (service.mPlayMode == PlayMode.MUSIC
							&& service.mLoopMode == LoopMode.ON) {
						List<Track> playedQueue = service.mPlayingQueue
								.getCopy();
						service.mPlayingQueue = new PlayingQueue(playedQueue,
								0, service);

						service.play();
					}

					break;

				case MESSAGE_AUDIOFOCUS_LOSS_TRANSIENT:
					// is it playing or loading to play?
					if (service.mCurrentState == State.PLAYING
							|| service.mCurrentState == State.INTIALIZED
							|| service.mCurrentState == State.PREPARED) {
						// Pause playback
						Logger.d(TAG, "AUDIOFOCUS LOSS TRANSIENT - pausing");
						service.pause();
						service.mIsPausedByAudiofocusLoss = true;
						service.sendBroadcast(new Intent(
								PlayerBarFragment.ACTION_PLAY_STATE_CHANGED));
					}
					break;

				case MESSAGE_AUDIOFOCUS_GAIN:
					// Resume playback
					if (service.mIsPausedByAudiofocusLoss) {
						service.mIsPausedByAudiofocusLoss = false;
						Logger.d(TAG, "AUDIOFOCUS GAIN - resuming play.");
						service.play();
					}
					break;

				case MESSAGE_AUDIOFOCUS_LOSS:
					service.isPlayerLoading = false;
					// is it playing or loading to play?
					if (service.mCurrentState == State.PLAYING
							|| service.mCurrentState == State.INTIALIZED
							|| service.mCurrentState == State.PREPARED) {
						// Pause playback
						Logger.d(TAG, "AUDIOFOCUS LOSS - stop playing.");
						/*if(service.needToUseCastingPlayer())
							return;*/
						service.pause();
						service.mIsPausedByAudiofocusLoss = true;
						service.sendBroadcast(new Intent(
								PlayerBarFragment.ACTION_PLAY_STATE_CHANGED));
					}
					break;

				case MESSAGE_ERROR:
					service.isPlayerLoading = false;
					int errorId = message.getData().getInt(MESSAGE_ERROR_VALUE);
					Error error = Error
							.getErrorById(errorId);
					Logger.e(TAG, "Player Error: " + error.toString());
					service.isErrorOccured = true;
					// resets the media player.
					service.currentPlayer.reset();
					service.mCurrentState = State.STOPPED;

					if (!Utils.isConnected()
							&& service.mPlayMode == PlayMode.MUSIC) {
						if (DBOHandler.getTrackCacheState(service.mContext, ""
								+ service.mCurrentTrack.getId()) != CacheState.CACHED)
							service.updateNotificationForTrack(service.mCurrentTrack);
					}

					for (PlayerStateListener listener : service.mOnPlayerStateChangedListeners) {
						listener.onErrorHappened(error);
					}

					break;
				case MESSAGE_SKIP_CURRENT_TRACK:
					service.isPlayerLoading = false;
					service.mCurrentState = State.COMPLETED;
					Logger.s(" :::::::::::::::: MESSAGE_SKIP_CURRENT_TRACK");
					finishedTrack = (Track) message.getData().getSerializable(
							MESSAGE_VALUE);
					for (PlayerStateListener listener : service.mOnPlayerStateChangedListeners) {
						listener.onFinishPlayingTrack(finishedTrack);
					}

					service.stop();
					service.next();
					if (!service.mPlayingQueue.hasNext()) {
						for (PlayerStateListener listener : service.mOnPlayerStateChangedListeners) {
							listener.onErrorHappened(Error.TRACK_SKIPPED);
						}
					}
					break;
				case MESSAGE_SKIP_TO_PREVIOUS_TRACK:
					service.isPlayerLoading = false;
					service.mCurrentState = State.COMPLETED;
					Logger.s(" :::::::::::::::: MESSAGE_SKIP_TO_PREVIOUS_TRACK");
					finishedTrack = (Track) message.getData().getSerializable(
							MESSAGE_VALUE);
					for (PlayerStateListener listener : service.mOnPlayerStateChangedListeners) {
						listener.onFinishPlayingTrack(finishedTrack);
					}

					service.stop();
					service.previous();
					if (!service.mPlayingQueue.hasPrevious()) {
						for (PlayerStateListener listener : service.mOnPlayerStateChangedListeners) {
							listener.onErrorHappened(Error.TRACK_SKIPPED);
						}
					}
					break;
				}
			} catch (Exception e) {
				Logger.e(getClass().getName() + ":584", e.toString());
			}
		}
	}

	/**
	 * Listens for changing in the playing volume focus.
	 */
	@Override
	public void onAudioFocusChange(int focusChange) {
		try {
			if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
				Message message = Message.obtain(mServiceHandler,
						MESSAGE_AUDIOFOCUS_LOSS_TRANSIENT);
				message.sendToTarget();

			} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
				Message message = Message.obtain(mServiceHandler,
						MESSAGE_AUDIOFOCUS_GAIN);
				message.sendToTarget();

			} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
				Message message = Message.obtain(mServiceHandler,
						MESSAGE_AUDIOFOCUS_LOSS);
				message.sendToTarget();
			}
		} catch (Exception e) {
			Logger.e(getClass().getName() + ":605", e.toString());
		}
	}

	/**
	 * Listens for completion of the current playing track.
	 */
	@Override
	public void onCompletion(/*MediaPlayer mp*/ Object mp) {
		if (isAdPlaying) {
			isAdPlaying = false;
			Utils.postPlayEvent(mContext, placementAudioAd,
					getDuration() / 1000, true);
			for (PlayerStateListener listener : mOnPlayerStateChangedListeners) {
				listener.onAdCompletion();
			}
			adSkipCount++;
			startLoadingTrack();
		} else {
			try {
				if (mCurrentTrack != null) {

					Track trackCopy = mCurrentTrack.newCopy();

					if (trackCopy != null) {

						Message message = Message.obtain(mServiceHandler,
								MESSAGE_FINISH_PLAYING_TRACK);
						Bundle data = new Bundle();
						Intent finishPlay = new Intent();
						finishPlay.setAction(TRACK_FINISHED);
						mContext.sendBroadcast(finishPlay);
						data.putSerializable(MESSAGE_VALUE,
								(Serializable) trackCopy);
						message.setData(data);
						message.sendToTarget();
					}
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Listens for buffering updates in the current playing track that is being
	 * prepared.
	 */
	@Override
	public void onBufferingUpdate(Object mp /*MediaPlayer mp*/, int percent) {
		long bandwidth = 0;
		if (firstEntry) {
			firstEntry = false;
			startTimeToCalculateBitrate = System.currentTimeMillis();
			percentStart = percent;
		} else if (percent == 100 && lastEntry) {
			lastEntry = false;
			endTimeToCalculateBitrate = System.currentTimeMillis();
			long dataPercent = (percent - percentStart);
			if (startTimeToCalculateBitrate != 0
					&& endTimeToCalculateBitrate != 0) {

				float timeDiff = endTimeToCalculateBitrate
						- startTimeToCalculateBitrate;

				long fileSizeInBits = mFileSize * 8;

				float per = dataPercent / 100f;

				bandwidth = (long) (((fileSizeInBits * per) / 1024f) / (timeDiff / 1000));
				Logger.i(TAG, "BANDWIDTH = " + bandwidth);

				if (bandwidth == 0) {
					// If bandwidth == 0 then store the maximum band width
					mApplicationConfigurations
							.setBandwidth(MediaHandleOperation.MAX_BANDWIDTH);
				} else if (bandwidth > 0) {
					// If bandwidth > 0 then store for next time
					mApplicationConfigurations.setBandwidth(bandwidth);
				}
			}
		}
		try {
			Message message = Message.obtain(mServiceHandler,
					MESSAGE_LOADING_TRACK_BUFFER_UPDATE);
			message.arg1 = percent;
			message.sendToTarget();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Listens for errors when trying to prepare the current track.
	 */
	@Override
	public boolean onError(Object mp /*MediaPlayer mp*/, int what, int extra) {
		isErrorOccured = true;
		currentPlayer.reset();

		if (isAdPlaying) {
			for (PlayerStateListener listener : mOnPlayerStateChangedListeners) {
				listener.onAdCompletion();
			}
			adSkipCount++;
			startLoadingTrack();
			isAdPlaying = false;
			return true;
		}
		if(!Utils.isConnected()){
			try {
				Message message = Message
						.obtain(mServiceHandler, MESSAGE_ERROR);
				Bundle data = new Bundle();
				data.putInt(MESSAGE_ERROR_VALUE, Error.NO_CONNECTIVITY.getId());
				message.setData(data);
				message.sendToTarget();
			} catch (Exception e) {
			}
			return true;
		}
		switch (what) {
		case MediaPlayer.MEDIA_ERROR_UNKNOWN:
		case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
			try {
				Message message = Message
						.obtain(mServiceHandler, MESSAGE_ERROR);
				Bundle data = new Bundle();
				data.putInt(MESSAGE_ERROR_VALUE, Error.SERVER_ERROR.getId());
				message.setData(data);
				message.sendToTarget();
			} catch (Exception e) {
			}
			return true;

		default:
			break;
		}
		return false;
	}

	// ======================================================
	// Service public controlling methods.
	// ======================================================

	public void setPlayingQueue(PlayingQueue playingQueue) {
		mPlayingQueue = playingQueue;
		// resets the swapper queues.
		mOriginalPlayingQueue = null;

		/*
		 * if the new playing queue is empty, removes any ongoing playing
		 * notification from the foreground.
		 */
		if (mPlayingQueue.size() == 0) {
			mApplicationConfigurations.setPlayerQueue("");
			mCurrentState = State.IDLE;// STOPPED
			dismissNotification();
		}
	}

	public void registerPlayerStateListener(PlayerStateListener listner) {
		if (!mOnPlayerStateChangedListeners.contains(listner))
			mOnPlayerStateChangedListeners.add(listner);
		Logger.i("RegisterPlayerState", "PlayerStateLisCount:"
				+ mOnPlayerStateChangedListeners.size());
	}

	public void unregisterPlayerStateListener(PlayerStateListener listner) {
		if (mOnPlayerStateChangedListeners.contains(listner))
			mOnPlayerStateChangedListeners.remove(listner);
		Logger.i("RegisterPlayerState", "PlayerStateLisCount 1:"
				+ mOnPlayerStateChangedListeners.size());
	}

	/**
	 * Stops the player if playing and adds to the queue the given tracks after
	 * it stops and continue to play.
	 *
	 */

	private boolean resetPlayerQueue() {
		stop();
		// resets the service to play music.
		// if (mPlayMode != null && (mPlayMode == PlayMode.DISCOVERY_MUSIC))
		if (mPlayMode != null && (mPlayMode != PlayMode.MUSIC || !isPlaying())) {
			mPlayingQueue = mDataManager
					.getStoredPlayingQueue(mApplicationConfigurations);
			AddStoredTrackList(mPlayingQueue.getCopy());
		} else
			mPlayingQueue = new PlayingQueue(null, 0, this);
		// if (mPlayMode != PlayMode.MUSIC)
		// playerVector.clear();
		mPlayMode = PlayMode.MUSIC;
		initializeMediaPlayer();
		return mPlayingQueue.size() == 0 ? true : false;
	}

	public void playNow(List<Track> tracks) {

		// checks if it's playing in other mode.
		boolean needToFilter = false;
		PlayMode prevMode = getPlayMode();
		if (mPlayMode != PlayMode.MUSIC) {
			resetPlayerQueue();
			needToFilter = true;
		}

		// adds the tracks to be next to the current position.
		if (mPlayingQueue.size() > 0) {
			List<Track> tracksNotInQueue;
			int pos = -1;
			if (needToFilter) {
				List<Track> trackList = mPlayingQueue.getCopy();
				tracksNotInQueue = new ArrayList<Track>();
				for (Track track : tracks) {
					if (!trackList.contains(track)) {
						tracksNotInQueue.add(track);
					} else {
						if (pos == -1) {
							pos = trackList.indexOf(track);
						}
					}
				}
			} else {
				tracksNotInQueue = tracks;
			}
			if (tracksNotInQueue.size() > 0) {
				mPlayingQueue.addNext(tracksNotInQueue);
				// starts playing the next.
				next();
			} else {
				if (pos == -1)
					pos = 0;
				if (prevMode != getPlayMode() || isPlaying())
					playFromPositionNew(pos);
				else
					play();
			}
		} else {
			adSkipCount = 0;
			mPlayingQueue.addToQueue(tracks);
			//addListToQueueCast(tracks);
			//------AddedByK
			mPlayingQueue.setCurrentPos(0);
			play();
		}

		saveAllTracksOffline(tracks);

	}

	public void AddFirstTimeQueue(List<Track> tracks) {
		// checks if it's playing in other mode.
		if (mPlayMode != PlayMode.MUSIC) {
			resetPlayerQueue();
		}
		// adds the tracks to be next to the current position.
		if (mPlayingQueue.size() > 0) {
			mPlayingQueue.addNext(tracks);
			changeLockScreenBG.sendEmptyMessage(0);
		} else {
			adSkipCount = 0;
			mPlayingQueue.addToQueue(tracks);
			//addListToQueueCast(tracks);
			mCurrentTrack = mPlayingQueue.getCurrentTrack();
		}

		saveAllTracksOffline(tracks);
	}

	private void AddStoredTrackList(List<Track> tracks) {
		mCurrentTrack = mPlayingQueue.getCurrentTrack();
	}

	private boolean isPlayNowSelected = false;

	/**
	 * Stops the player if playing and adds to the queue the given tracks after
	 * it stops and continue to play.
	 *
	 * @param tracks
	 */
	public void playNowFromPosition(List<Track> tracks, int trackPosition) {
		// checks if it's playing in other mode.
		Logger.i("MediaTilesAdapter", "Play button click: PlayNow 15");
		if(isAdPlaying())
			return;
		if (mPlayMode != PlayMode.MUSIC) {
			resetPlayerQueue();
		} else {
			stop();
		}
		isPlayNowSelected = true;
		mPlayingQueue.goTo(trackPosition);
		play();

		saveAllTracksOffline(tracks);
	}

	/**
	 * Adds the given tracks to the play queue after this current playing track.
	 *
	 * @param tracks
	 */
	public void playNext(List<Track> tracks) {

		// checks if it's playing in other mode.
		if (mPlayMode != PlayMode.MUSIC) {
			resetPlayerQueue();
		}

		if (mPlayingQueue.size() > 0) {
			mPlayingQueue.addNext(tracks);
			changeLockScreenBG.sendEmptyMessage(0);
		} else {
			playNow(tracks);
		}

		saveAllTracksOffline(tracks);
	}

	/**
	 * Adds these tracks to the end of the queue to been played.
	 *
	 * @param tracks
	 */
	public void addToQueue(List<Track> tracks) {

		// checks if it's playing in other mode.
		if (mPlayMode != PlayMode.MUSIC) {
			resetPlayerQueue();
		}

		if (mPlayingQueue.size() > 0) {
			mPlayingQueue.addToQueue(tracks);
			//addListToQueueCast(tracks);
			changeLockScreenBG.sendEmptyMessage(0);
		} else {
			adSkipCount = 0;
			/*
			 * The user has added tracks to the queue when it was empty. Adds
			 * the tracks to the queue, and force him to presents it.
			 */
			mPlayingQueue.addToQueue(tracks);
			//addListToQueueCast(tracks);
			mCurrentTrack = mPlayingQueue.getCurrentTrack();

			// fake invocation to make the client updates its text.
			for (PlayerStateListener listener : mOnPlayerStateChangedListeners) {
				listener.onStartLoadingTrack(mCurrentTrack);
			}
		}

		saveAllTracksOffline(tracks);
	}

	public void addToQueueAfterCurrentPosition(List<Track> tracks) {

		/*
		 * The user has added tracks to the queue when it was empty. Adds the
		 * tracks to the queue, and force him to presents it.
		 */
		mPlayingQueue.addToCurrent(tracks);
		// mPlayingQueue.goToNew(position);
		mCurrentTrack = mPlayingQueue.getCurrentTrack();

		saveAllTracksOffline(tracks);
	}

	public void addToQueueAfterCurrentPosition(List<Track> tracks, int position) {

		/*
		 * The user has added tracks to the queue when it was empty. Adds the
		 * tracks to the queue, and force him to presents it.
		 */
		// mPlayingQueue.addNext(tracks);
		mPlayingQueue.goToNew(position);
		mCurrentTrack = mPlayingQueue.getCurrentTrack();

		saveAllTracksOffline(tracks);
	}

	public void playRadio(List<Track> radioTracks, PlayMode playMode) {
		if (!Utils.isListEmpty(radioTracks)
				&& (playMode == PlayMode.LIVE_STATION_RADIO || playMode == PlayMode.TOP_ARTISTS_RADIO)) {

			// stops any playing music.
			if (isPlaying() || isLoading()) {
				stop();
			}

			mPlayMode = playMode;
			initializeMediaPlayer();
			if (playMode == PlayMode.LIVE_STATION_RADIO && radioTracks != null
					&& radioTracks.size() > 0)
				startLiveRadioUpdater(radioTracks.get(0));
			else
				stopLiveRadioUpdater();

			// clears old playlist and creates a new one.
			mPlayingQueue = new PlayingQueue(radioTracks, 0, this);
			play();
		}
	}

	public void playDiscoverySongs(List<Track> discoveryTracks,
			PlayMode playMode) {
		if (!Utils.isListEmpty(discoveryTracks)
				&& playMode == PlayMode.DISCOVERY_MUSIC) {

			// stops any playing music.
			if (isPlaying() || isLoading()) {
				stop();
				prevDiscover = null;
			}

			mPlayMode = playMode;
			initializeMediaPlayer();
			// clears old playlist and creates a new one.
			mPlayingQueue = new PlayingQueue(discoveryTracks, 0, this);

			play();
		}
	}

	public boolean isQueueEmpty() {
		try {
			return mPlayingQueue.size() == 0;
		} catch (Exception e) {
			return true;
		}
	}

	private boolean isPlayStarted = false;

	public boolean needToPlayCacheMediaPlayer(MusicPlayerFunctions player) {
		/*if (getPlayMode() == PlayMode.LIVE_STATION_RADIO || (player != null && player.trackId == 0))
			return true;
		long currentTrackId = mCurrentTrack.getId();*/

		return false;//(player != null && (player.trackId == currentTrackId) && (player.playerState == PlayerState.STATE_PREPARED || player.playerState == PlayerState.STATE_PAUSED));
	}

	private boolean needToPlayCacheMediaPlayerNew(MyMediaPlayer player) {
		/*if (getPlayMode() == PlayMode.LIVE_STATION_RADIO || player.trackId == 0)
			return true;
		long currentTrackId = mCurrentTrack.getId();*/
		return false;//(player != null && (player.trackId == currentTrackId) && (player.playerState == PlayerState.STATE_PLAYING));
	}

	public static void stopCasting(){
		/*VideoCastManager mCastManager=VideoCastManager.getInstance();
		if (mCastManager!=null) {
			mCastManager.stopNotificationService();
			try {
				mCastManager.stop();
				//mCastManager.clearMediaSession();
				//mCastManager.disconnect();
			} catch (CastException e) {
				e.printStackTrace();
			} catch (TransientNetworkDisconnectionException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}*/
	}

	public void play() {
		try {
			mIsExplicitMarkedExit = false;
			if (CallInProgress /*&& !isCastRequire()*/)
				return;


            if(isPausedFromVideo)
                return;

			//stopCasting();
			isErrorOccured = false;
			Logger.i("MediaTilesAdapter", "Play button click: PlayNow 16");
			Logger.s(" ::::::::::::::Play:::::::::::::::::::: ");
			updatewidget();

			Logger.i("MediaTilesAdapter", "Play button click: PlayNow 17");
			if (mPlayingQueue.size() > 0) {
				mAudioManager.requestAudioFocus(PlayerService.this,
				// Use the music stream.
						AudioManager.STREAM_MUSIC,
						// Request permanent focus.
						AudioManager.AUDIOFOCUS_GAIN);

				// checks if it's currently playing.
				if (mCurrentState == State.PAUSED && !isPlayNowSelected) {
					if (mPlayMode == PlayMode.LIVE_STATION_RADIO) {
						playRadio(getPlayingQueue(),
								PlayMode.LIVE_STATION_RADIO);
					} else {
						mCurrentState = State.PLAYING;
						//StopMobileMusicPlay
                        if (currentPlayer != null && !needToUseCastingPlayer())
                            currentPlayer.start();

                        //ResumeCastSong();
						updatePlaybackState(PLAYSTATE_PLAYING);

						// Logging Events are only for music.
						if (mPlayMode == PlayMode.MUSIC) {
							startLoggingEvent();
						}
					}
				} else {
					isPlayNowSelected = false;
					/*
					 * Starts the loading of the track, when it will be
					 * prepared, it will be played automatically.
					 */
					mCurrentTrack = mPlayingQueue.getCurrentTrack();
					try {
						if (currentPlayer != null && currentPlayer.isPlaying() && !needToUseCastingPlayer())
							currentPlayer.pause();
						startLoadingTrack();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				updateNotificationForTrack(mCurrentTrack);
				isPlayStarted = true;
			}
			Logger.i("MediaTilesAdapter", "Play button click: PlayNow 18");
			startAutoSavingPlayerQueue();
		} catch (Exception e) {
		}
	}

	private Intent widgetService = null;

	public void updatewidget() {
		Logger.s(" ::::::::::::::updatewidget:::::::::::::::::: " + isTaskRemoved);
		if (Utils.isFordCarMode()) {
			if (widgetService != null)
				stopService(widgetService);
			stopNotification();
		}else if(isTaskRemoved){
			if(widgetService!=null)
				stopService(widgetService);
			if(!mIsExplicitMarkedExit)
				showNotification();
		} else {
			widgetService = new Intent(getBaseContext(),
					PlayerUpdateWidgetService.class);
			startService(widgetService);
			if(!mIsExplicitMarkedExit)
				showNotification();
		}

	}


//	private void updateWigetFord() {
//		Logger.s(" ::::::::::::::updatewidgetFord:::::::::::::::::: " + isTaskRemoved);
//
//		Logger.s(" ::::::::::::::updatewidget:::::::::::::::::: " + isTaskRemoved);
//		if (Utils.isFordCarMode()) {
//			if (widgetService != null)
//				stopService(widgetService);
//			stopNotification();
//		}
//		else if(isTaskRemoved){
//			if(widgetService!=null)
//				stopService(widgetService);
//		} else {
//			widgetService = new Intent(getBaseContext(),
//					PlayerUpdateWidgetService.class);
//			startService(widgetService);
//		}
//		if(!mIsExplicitMarkedExit)
//			showNotification();
//
//        /*if (!mIsExplicitMarkedExit)
//            showNotification();*/
//	}

	private void updatewidget(boolean dismiss) {
		Logger.s(" ::::::::::::::updatewidgetNew:::::::::::::::::: " + dismiss);
		if(dismiss){
			Intent intentStop = new Intent(getBaseContext(),
					PlayerUpdateWidgetService.class);
			intentStop.putExtra(PlayerUpdateWidgetService.EXTRA_COMMAND,
					PlayerUpdateWidgetService.EXTRA_STOP_SERVICE);
			startService(intentStop);

			stopNotification();
		} else {
			widgetService = new Intent(getBaseContext(),
					PlayerUpdateWidgetService.class);
			startService(widgetService);
		}
	}

	private boolean isTaskRemoved;

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		Logger.s(" ::::::::::::::onTaskRemoved:::::::::::::::::: Player");
		isTaskRemoved = true;
		super.onTaskRemoved(rootIntent);
	}

	public void playFromPosition(int newPosition) {
		if (mPlayingQueue.size() > 0) {
			if (mPlayingQueue.getCurrentPosition() != newPosition) {
				stop();
				mPlayingQueue.goTo(newPosition);
				play();
			} else {
				if (!isPlaying())
					play();
			}
		}
	}

	public void playFromPositionNew(int newPosition) {
		if (mPlayingQueue.size() > 0) {
			stop();
			mPlayingQueue.goToNew(newPosition);
			play();
		}
	}

	public PlayingQueue getPlayerQueueObject() {
		return mPlayingQueue;
	}

	/**
	 * Stops playing current track.
	 */
	public void stop() {
		try {
			//StopCasting();
			if (mPlayingQueue != null && mPlayingQueue.size() > 0) {
				// stop loading if any.
				stopLoadingTrack();
				mAudioManager.abandonAudioFocus(PlayerService.this);
				// stop playing if any.
				if (mCurrentState == State.PREPARED
						|| mCurrentState == State.PAUSED
						|| mCurrentState == State.PLAYING
						|| mCurrentState == State.COMPLETED
						|| mCurrentState == State.COMPLETED_QUEUE) {
					String type = FlurryConstants.FlurryFullPlayerParams.Music
							.toString();
					// Logging Events are only for music.
					if (mPlayMode == PlayMode.MUSIC) {

						Logger.i("Current Pos","Cast Cusrrent POs::::::::::"+getCurrentPlayerPosition());
						if (getCurrentPlayerPosition() > 0) {
							// Send an Event only if the track played
							if (mCurrentState == State.COMPLETED
									|| mCurrentState == State.COMPLETED_QUEUE) {
								stopLoggingEvent(true);
							} else {
								stopLoggingEvent(false);
							}
						}/*else if(isCastConnected() && totalDurationForCast>0){
							stopLoggingEvent(true);
						}else if(playDurationForCast>0 && totalDurationForCast>0){
							stopLoggingEvent(false);
						}*/
						type = FlurryConstants.FlurryFullPlayerParams.Music
								.toString();
					} else if (mPlayMode == PlayMode.TOP_ARTISTS_RADIO) {
						if (getCurrentPlayerPosition() > 0) {
							if (mCurrentState == State.COMPLETED
									|| mCurrentState == State.COMPLETED_QUEUE) {
								stopLoggingEvent(true);
							} else {
								stopLoggingEvent(false);
							}
						}/*else if(isCastConnected() && totalDurationForCast>0){
							stopLoggingEvent(true);
						}else if(playDurationForCast>0 && totalDurationForCast>0){
							stopLoggingEvent(false);
						}*/
						type = FlurryConstants.FlurryFullPlayerParams.OnDemandRadio
								.toString();
					} else if (mPlayMode == PlayMode.DISCOVERY_MUSIC) {
						if (getCurrentPlayerPosition() > 0) {
							if (mCurrentState == State.COMPLETED
									|| mCurrentState == State.COMPLETED_QUEUE) {
								stopLoggingEvent(true);
							} else {
								stopLoggingEvent(false);
							}
						}/*else if(isCastConnected() && totalDurationForCast>0){
							stopLoggingEvent(true);
						}else if(playDurationForCast>0 && totalDurationForCast>0){
							stopLoggingEvent(false);
						}*/
						type = FlurryConstants.FlurryFullPlayerParams.DiscoveryMusic
								.toString();
					}

					try {
						if (isPlayStarted && !isErrorOccured) {
							// Flurry report: song played
							Map<String, String> reportMap = new HashMap<String, String>();
							reportMap
									.put(FlurryConstants.FlurryKeys.Title
											.toString(), mCurrentTrack
											.getTitle());
							Logger.i(TAG, "playCurrentPostion Duration: 1 "
									+ getCurrentPlayerPosition());
							reportMap.put(FlurryConstants.FlurryKeys.Duration
									.toString(), String.valueOf(currentPlayer
									.getCurrentPosition()));
							reportMap.put(
									FlurryConstants.FlurryKeys.Type.toString(),
									type);
							Analytics.logEvent(
									FlurryConstants.FlurryAllPlayer.SongPlayed
											.toString(), reportMap);
						}
						isPlayStarted = false;
					} catch (Exception e) {
						Logger.e(getClass().getName() + ":945", e.toString());
					}

                    mCurrentState = State.STOPPED;
                    //removeMediaListner();
                    currentPlayer.stop();
					currentPlayer.reset();
//					currentPlayer.release();
//					currentPlayer = null;
                    //assignMediaListner();
					/*currentPlayer.pause();
					playerVector.add(currentPlayer);

					currentPlayer = new MyMediaPlayer(0);
					assignMediaListner();*/
                    updatePlaybackState(PLAYSTATE_STOPPED);
                }
                // resets the reporting flag for hungama.
                mReportedTrack = -1;
            }else{
				/*if (mPlayback != null)
					mPlayback.stop(true);*/
				//stopCasting();
			}
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
        Logger.s(" ::::::::::::::stop:::::::::::::::::::: ");
		updatewidget(false);
		//totalDurationForCast = 0;
		//playDurationForCast = 0;
		if(mPlayMode != PlayMode.LIVE_STATION_RADIO)
			mEventStartTimestamp = null;
    }

	public void clearAd() {
		if (isAdPlaying()) {
			isAdPlaying = false;
			for (PlayerStateListener listener : mOnPlayerStateChangedListeners) {
				listener.onAdCompletion();
			}
		}
	}

	public void explicitStop() {
		Logger.w(TAG,
				"################# explicit stopping the service #################");
		// this state is been questioned in the service's handler before
		// playing.
		isAdPlaying = false;

		mIsExplicitMarkedExit = true;
		stopProgressUpdater();
		// stops playing.
		stop();

		adSkipCount = 0;
		AudioAdCount = 0;
		needToLoadAudioAd = false;

		// dismisses the notification.
		dismissNotification();

		if (mPlayMode == PlayMode.MUSIC) {
			// shut down the media handle prefetching.
			stopPrefetchingMediaHandles(true);
		}
		stopCasting();
		// bye bye dear service.
		updatewidget(true);
		stopSelf();
	}

	public void removeCastCallBack(){
		/*if(mCastManager!=null && mCastConsumer!=null)
			mCastManager.removeVideoCastConsumer(mCastConsumer);*/
	}

	/**
	 * Pauses the current track if it was playing
	 */
	public void pause() {
		// checks if it's currently playing.
		try {
			/*if(CallInProgress && isCastConnected())
				return;*/
			if (mCurrentState == State.PLAYING) {
				mCurrentState = State.PAUSED;
				if (!needToUseCastingPlayer())
                    currentPlayer.pause();
                //PauseCasting();
				dismissNotification();
				// dismisses the notification.
				updatePlaybackState(PLAYSTATE_PAUSED);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Plays the next track in the queue.
	 */
	 public void next() {
		if(isPausedFromVideo)
			return;

		isPlayerLoading = true;
		// stops any playing / loading.
		stop();
		isPreviousClicked = false;
		if (mPlayingQueue.hasNext()) {
			mCurrentTrack = mPlayingQueue.next();
			play();
		} else {
			mServiceHandler
					.sendEmptyMessage(PlayerService.MESSAGE_FINISH_PLAYING_QUEUE);
		}

	}

	/**
	 * Restarts the player and starts replaying the queue.
	 */
	public void replay() {
		// stops playing.
		stop();
		// resets the queue and Rock & Roll.
		mPlayingQueue = new PlayingQueue(mPlayingQueue.getCopy(), 0, this);
		play();

		// resets the report list.
		mReportedTrack = -1;
	}

	/**
	 * Sets the next track to being played without playing it.
	 *
	 * @return Track to be played.
	 */
	public Track fakeNext() {
		// stops any playing / loading.
		stop();
		isPreviousClicked = false;
		mCurrentTrack = mPlayingQueue.next();

		return mCurrentTrack;
	}

	/**
	 * Plays the previous track in the queue.
	 */
	public void previous() {
		if(isPausedFromVideo)
			return;
		// stops any playing / loading.
		stop();
		isPreviousClicked = true;
		mCurrentTrack = mPlayingQueue.previous();

		if (mCurrentTrack != null) {
			play();
		} else {
			mServiceHandler
					.sendEmptyMessage(PlayerService.MESSAGE_FINISH_PLAYING_QUEUE);
		}
	}

	private boolean isPreviousClicked = false;

	/**
	 * Sets the previous track to being played without playing it.
	 *
	 * @return Track to be played.
	 */
	public Track fakePrevious() {
		// stops any playing / loading.
		stop();
		// isPreviousClicked = true;
		mCurrentTrack = mPlayingQueue.previous();

		return mCurrentTrack;
	}

	public boolean hasNext() {
		return mPlayingQueue.hasNext();
	}

	public boolean hasPrevious() {
		return mPlayingQueue.hasPrevious();
	}

	public Track getCurrentPlayingTrack() {

		if (mCurrentTrack == null && mPlayingQueue!=null) {
			mCurrentTrack = mPlayingQueue.getCurrentTrack();
		} else if (mPlayingQueue!=null && mPlayingQueue.size()==0) {
			mCurrentTrack = null;
			return null;
		}

		return mCurrentTrack;
	}

	public int getDuration() {
		if (needToUseCastingPlayer()) {
			/*try {
				int duration =  ((int) mCastManager.getMediaDuration()) > 0 ? (int) mCastManager.getMediaDuration() : 0;
				if(duration >0 && isCastConnected()){
					totalDurationForCast = duration;
				}
				return  duration;
			} catch (Exception e) {
				return 0;
			}*/
		}
		if (currentPlayer == null)
			return 0;
		if (isErrorOccured/* || getPlayMode() != PlayMode.MUSICs*/)
			return 0;
		//if (mCurrentState == State.PLAYING || mCurrentState == State.PAUSED)
		try {
			return (currentPlayer.getDuration() == -1)?0:currentPlayer.getDuration();
		}catch (Exception e){
			return 0;
		}catch (java.lang.Error e){
			return 0;
		}

	}

	/*public int getCurrentPlayingProgress() {
		if (currentPlayer == null)
			return 0;
		if (isErrorOccured)
			return 0;
		if (mCurrentState == State.PLAYING || mCurrentState == State.PAUSED) {
			if (currentPlayer.getCurrentPosition() < currentPlayer
					.getDuration())
				return currentPlayer.getCurrentPosition();
		}
		return 0;
	}*/

	public int getCurrentPlayerPosition() {
        if (needToUseCastingPlayer()) {
            /*try {
                long currentMediaPos = mCastManager.getCurrentMediaPosition();
                if (currentMediaPos < mCastManager
                        .getMediaDuration()) {
					if(currentMediaPos>0)
						playDurationForCast = (int)currentMediaPos;

					return (int) currentMediaPos;
                }
            } catch (Exception e) {
                return 0;
            }*/
        }
        if (currentPlayer == null)
            return 0;
        if (isErrorOccured)
            return 0;
		try{
			if (currentPlayer.getCurrentPosition() < currentPlayer
					.getDuration())
				return currentPlayer.getCurrentPosition();
			else
				return currentPlayer.getDuration();
		}catch (Exception e){
			e.printStackTrace();
		}catch (java.lang.Error e){
			e.printStackTrace();
		}
        return 0;

    }

	public State getState() {
		return mCurrentState;
	}

	public void seekTo(int timeMilliseconds) {
        if (needToUseCastingPlayer()) {
            //SeekToCasting(timeMilliseconds);
        } else {
            currentPlayer.seekTo(timeMilliseconds);
        }
    }

	/**
	 * Determines if the player is in the middle of loading of preparing a
	 * {@link Track} before playing.
	 *
	 * @return true if the player is in the state of {@code State.INTIALIZED} or
	 *         {@code State.PREPARED}, false otherwise.
	 */
	public boolean isLoading() {
		if (mCurrentState == State.INTIALIZED
				|| mCurrentState == State.PREPARED) {
			return true;
		}
		return false;
	}

	/**
	 * Determines if the player is in the middle of playing a {@link Track}. or
	 * the played track is paused it will return true too.
	 *
	 * @return true if the player is in the state of {@code State.PLAYING} or
	 *         {@code State.PAUSED}, false otherwise.
	 */
	public boolean isPlaying() {
		if (mCurrentState == State.PLAYING || mCurrentState == State.PAUSED
				|| mCurrentState == State.COMPLETED_QUEUE) {
			return true;
		}
		return false;
	}

	public boolean isPlayingForExit() {
		if (mCurrentState == State.PLAYING || mCurrentState == State.INTIALIZED) {
			return true;
		}
		return false;
	}

	public PlayMode getPlayMode() {
		return mPlayMode;
	}

	public List<Track> getPlayingQueue() {
		try {
			synchronized (mPlayingQueue) {
				if (mPlayingQueue != null) {
					return mPlayingQueue.getCopy();
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	public Track getNextTrack() {
		if (mPlayingQueue != null) {
			return mPlayingQueue.getNextTrack();
		}
		return null;
	}

	public Track getPreviousTrack() {
		if (mPlayingQueue != null) {
			return mPlayingQueue.getPreviousTrack();
		}
		return null;
	}

	public int getCurrentQueuePosition() {
		if (mPlayingQueue != null && (isPlaying() || isLoading())) {
			return mPlayingQueue.getCurrentPosition();
		}
		return PlayingQueue.POSITION_NOT_AVAILABLE;
	}

	public Track removeFrom(int position) {
		Track removedTrack = null;
		if (mPlayingQueue != null) {
			// System.out.println(position + " ::::::: " +
			// mPlayingQueue.getCurrentPosition());
			if (position == mPlayingQueue.getCurrentPosition()) {
				stop();
				removedTrack = mPlayingQueue.removeFrom(position);
				if (position <= 0)
					mCurrentState = State.STOPPED;
				return removedTrack;

			} else {
				removedTrack = mPlayingQueue.removeFrom(position);
			}
		}
		if (removedTrack != null) {
			changeLockScreenBG.sendEmptyMessage(0);
			Intent in = new Intent(ACTION_REMOVED_TRACK);
			in.putExtra("removedTrackid", removedTrack.getId() + "");
			sendBroadcast(in);
		}
		return removedTrack;
	}

	public void removeFromQueueWhenAddToQueue(int position) {
		mPlayingQueue.removeFrom(position);
	}

	public void setLoopMode(LoopMode loopMode) {
		mLoopMode = loopMode;
		//updateCastingQueue();
	}

	public LoopMode getLoopMode() {
		return mLoopMode;
	}

	public void startShuffle() {
		mIsShuffling = true;

		// swap the queue.
		mOriginalPlayingQueue = mPlayingQueue;
		mPlayingQueue = PlayingQueue.createShuffledQueue(mOriginalPlayingQueue,
				this);
		//updateCastingQueue();
	}

	public void stopShuffle() {
		try {
			mIsShuffling = false;
			// xtpl
			mOriginalPlayingQueue.setCurrentTrack(mPlayingQueue
					.getCurrentTrack().getId());
			// xtpl
			// revert to the original queue.
			mPlayingQueue = mOriginalPlayingQueue;
			//updateCastingQueue();
		} catch (Exception e) {
			Logger.e(getClass().getName() + ":1211", e.toString());
		}
	}

	public boolean isShuffling() {
		return mIsShuffling;
	}

	public void reportBadgesAndCoins() {
		if (mCurrentTrack != null && mCurrentTrack.getId() != mReportedTrack) {
			mReportedTrack = mCurrentTrack.getId();

			mDataManager.checkBadgesAlert(Long.toString(mCurrentTrack.getId()),
					"song", "musicstreaming", null);
		}
	}

	public boolean isAllowSelfTermination() {
//		if (mCurrentState == State.INTIALIZED
//				|| mCurrentState == State.PREPARED
//				|| mCurrentState == State.PLAYING
//				|| mCurrentState == State.PAUSED
//				|| mCurrentState == State.COMPLETED_QUEUE) {
//
			return false;
//		}

//		return true;
	}

	// ======================================================
	// Private helper methods.
	// ======================================================
	ExoMusicPlayer exoPlayer;
	MyMediaPlayer myMediaPlayer;

	private void initializeMediaPlayer() {
		boolean needToInitExo = false;
		if(myMediaPlayer==null){
			myMediaPlayer = new MyMediaPlayer(getApplicationContext());
		}

		if(exoPlayer==null && Utils.isNeedToUseHLS()){
			needToInitExo = true;
			exoPlayer = new ExoMusicPlayer(getApplicationContext());
		}

		boolean isCacheSong = false;
		if(mCurrentTrack!=null)
			isCacheSong = mCurrentTrack.isCached();
		if(currentPlayer==null) {
			if (Utils.isNeedToUseHLS() && mPlayMode != PlayMode.LIVE_STATION_RADIO && !isCacheSong)
				currentPlayer = exoPlayer;//new ExoMusicPlayer(getApplicationContext());
			else
				currentPlayer = myMediaPlayer;//new MyMediaPlayer(getApplicationContext());
		}else if(currentPlayer instanceof ExoMusicPlayer && (mPlayMode == PlayMode.LIVE_STATION_RADIO || isCacheSong)) {
			currentPlayer = myMediaPlayer;//new MyMediaPlayer(getApplicationContext());
		}else if(currentPlayer instanceof MyMediaPlayer && mPlayMode != PlayMode.LIVE_STATION_RADIO && Utils.isNeedToUseHLS() && !isCacheSong) {
			currentPlayer = exoPlayer;//new ExoMusicPlayer(getApplicationContext());
		}

		if(currentPlayer instanceof ExoMusicPlayer) {
			if (needToInitExo) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						Logger.i("MediaPlayer", "Selected MediaPlayer:ExoMusicPlayer");
						currentPlayer.init(getApplicationContext());
						assignMediaListner();
						currentPlayer.setWakeMode(PlayerService.service, PowerManager.PARTIAL_WAKE_LOCK);
						currentPlayer.reset();
						mCurrentState = State.IDLE;
					}
				});
			} else {
				assignMediaListner();
				currentPlayer.setWakeMode(PlayerService.service, PowerManager.PARTIAL_WAKE_LOCK);
				currentPlayer.reset();
				mCurrentState = State.IDLE;
			}
		}else{
			Logger.i("MediaPlayer","Selected MediaPlayer:MyMediaPlayer");
			currentPlayer.init(getApplicationContext());
			assignMediaListner();
			currentPlayer.setWakeMode(PlayerService.service, PowerManager.PARTIAL_WAKE_LOCK);
			currentPlayer.reset();
			mCurrentState = State.IDLE;
		}
	}

    private void assignMediaListner() {
		removeMediaListner();
        currentPlayer.setOnBufferingUpdateListener(this);
        currentPlayer.setOnCompletionListener(this);
        currentPlayer.setOnErrorListener(this);
    }

	private void removeMediaListner() {
		if (currentPlayer != null) {
			currentPlayer.setOnBufferingUpdateListener(null);
			currentPlayer.setOnCompletionListener(null);
			currentPlayer.setOnErrorListener(null);
		}
	}

	/*private void removeMediaListner() {
		if (currentPlayer != null) {
			currentPlayer.setOnBufferingUpdateListener(null);
			currentPlayer.setOnCompletionListener(null);
			currentPlayer.setOnErrorListener(null);
			currentPlayer.setOnPreparedListener(null);
		}
	}*/

	public void destroyMediaPlayer() {
		if (currentPlayer != null) {
			currentPlayer.setOnBufferingUpdateListener(null);
			currentPlayer.setOnCompletionListener(null);
			currentPlayer.setOnErrorListener(null);
			currentPlayer.setOnPreparedListener(null);
			currentPlayer.release();
		}
		mCurrentState = State.IDLE;
	}

	private void startLoadingTrack() {
		stopLoadingTrack();
		mMediaLoaderHandler = new MediaLoaderHandler(this);
		isErrorOccured = false;
		if (mPlayMode == PlayMode.MUSIC
				|| mPlayMode == PlayMode.TOP_ARTISTS_RADIO
				|| mPlayMode == PlayMode.DISCOVERY_MUSIC) {
			// stopUnusedPlayer();
			try {
				if (mMediaLoaderTask != null)
				{
					if(mMediaLoaderTask instanceof MusicTrackLoaderTask)
						((MusicTrackLoaderTask)mMediaLoaderTask).interrupt();
					else if(mMediaLoaderTask instanceof RadioTrackLoaderTask)
						((RadioTrackLoaderTask)mMediaLoaderTask).interrupt();
				}
			} catch (Exception e) {
				Logger.i("startLoadingTrack ",
						"startLoadingTrack thread Intrupt:" + e.getMessage());
			}
			if(mCurrentTrack==null){
				mCurrentTrack = mPlayingQueue.getCurrentTrack();
			}
			mMediaLoaderTask=new MusicTrackLoaderTask(
					mMediaLoaderHandler, mCurrentTrack);
			MediaItem mediaItem = new MediaItem(mCurrentTrack.getId(),
					mCurrentTrack.getTitle(), mCurrentTrack.getAlbumName(),
					mCurrentTrack.getArtistName(), mCurrentTrack.getImageUrl(),
					mCurrentTrack.getBigImageUrl(), MediaType.TRACK.toString()
							.toLowerCase(), 0, 0, mCurrentTrack.getImages(),
					mCurrentTrack.getAlbumId());
			if (mPlayMode == PlayMode.MUSIC || mPlayMode == PlayMode.DISCOVERY_MUSIC) {
				if (!CacheManager.isProUser(mContext)
						|| (CacheManager.isProUser(mContext)
								&& mApplicationConfigurations
										.getSaveOfflineAutoSaveMode() && !mApplicationConfigurations
									.getSaveOfflineMode())) {
					CacheManager.autoSaveOfflinePlayerQueue(mContext,
							mediaItem, mCurrentTrack);
				}
			}
		} else {
			// plays the music as "Normal" music - Live Radio.
			mMediaLoaderTask=new RadioTrackLoaderTask(
					mMediaLoaderHandler, mCurrentTrack);
		}

		ThreadPoolManager.getInstance().submit(mMediaLoaderTask);
	}

	private void stopLoadingTrack() {
		if (mMediaLoaderTask != null)
		{
			// mMediaLoaderHandler.removeCallbacksAndMessages(null);
			if(mMediaLoaderTask instanceof MusicTrackLoaderTask)
				((MusicTrackLoaderTask)mMediaLoaderTask).interrupt();
			else if(mMediaLoaderTask instanceof RadioTrackLoaderTask)
				((RadioTrackLoaderTask)mMediaLoaderTask).interrupt();

			mMediaLoaderHandler.removeCallbacksAndMessages(null);

			mMediaLoaderTask = null;
		}
	}

	MusicTrackHandlesPrefetchingTask prefetchTask;
	GetMusicTrackHandle musicTrackHandle;
//	Thread prefetchThread = new Thread();
//	Thread musicTrackHandleThread = new Thread();

	private void getMusicMediaHandle(boolean needToPlay, Track track) {
		if (musicTrackHandle != null) {
			musicTrackHandle.cancel();
		}
		musicTrackHandle = new GetMusicTrackHandle();
		musicTrackHandle.needToPlay(needToPlay, track);
//		musicTrackHandleThread = new Thread(musicTrackHandle);
		ThreadPoolManager.getInstance().submit(musicTrackHandle);
//		musicTrackHandleThread.start();
	}


	private void startPrefetchingMediaHandles() {
		stopPrefetchingMediaHandles(false);
		prefetchTask = new MusicTrackHandlesPrefetchingTask();
//		prefetchThread = new Thread(prefetchTask);
//		prefetchThread.start();
		ThreadPoolManager.getInstance().submit(prefetchTask);

		// mTracksMediaHandleExecutionRecord = mTracksMediaHandleExecutor
		// .submit(new MusicTrackHandlesPrefetchingTask());
	}

	private void stopPrefetchingMediaHandles(boolean shutDown) {
		// cancels any
		if (prefetchTask != null) {
			prefetchTask.cancel();
		}

		// if (mTracksMediaHandleExecutionRecord != null
		// && !mTracksMediaHandleExecutionRecord.isDone()) {
		// mTracksMediaHandleExecutionRecord.cancel(true);
		// mTracksMediaHandleExecutionRecord = null;
		// }
		//
		// if (shutDown) {
		// mTracksMediaHandleExecutor.shutdownNow();
		// }
	}

	// ======================================================
	// Media handle background operation.
	// ======================================================

	private static class MediaLoaderHandler extends Handler {
		PlayerService service;

		private MediaLoaderHandler(PlayerService service) {
			this.service = service;
		}

		public static final int MESSAGE_INITIALIZED = 1;
		public static final int MESSAGE_LOADED = 2;
		public static final int MESSAGE_PREPARED = 3;
		public static final int MESSAGE_ERROR = 4;
		public static final int MESSAGE_CANCELLED = 5;
		public static final int MESSAGE_SKIP_CURRENT = 6;
		public static final int MESSAGE_SKIP_TO_PRVIOUS = 7;

		@Override
		public void handleMessage(Message msg) {
			// resets the data. before obtaining the message.
			Bundle args = msg.getData();
			Message message = Message.obtain(msg);

			if (args != null) {
				message.setData(args);
			}

			switch (msg.what) {
			case MESSAGE_INITIALIZED:
				message.what = PlayerService.MESSAGE_START_LOADING_TRACK;
				if (service.mServiceHandler != null) {
					service.mServiceHandler.sendMessage(message);
				}
				break;

			case MESSAGE_LOADED:
				// nothing happened here, the general process of playing a track
				// doesn't care about this.
				break;

			case MESSAGE_PREPARED:
				Logger.i("onPrepared", " :::::::::::::::::::::::::::::::: onPrepared: MessagePrepared");
				message.what = PlayerService.MESSAGE_LOADING_TRACK_PREPARED;
				if (service.mServiceHandler != null) {
					service.mServiceHandler.sendMessage(message);
				}
				break;

			case MESSAGE_ERROR:
				message.what = PlayerService.MESSAGE_ERROR;
				if (service.mServiceHandler != null) {
					service.mServiceHandler.sendMessage(message);
				}
				break;

			case MESSAGE_CANCELLED:
				message.what = PlayerService.MESSAGE_LOADING_TRACK_CANCELLED;
				if (service.mServiceHandler != null) {
					service.mServiceHandler.sendMessage(message);
				}
				break;
			case MESSAGE_SKIP_CURRENT:
				message.what = PlayerService.MESSAGE_SKIP_CURRENT_TRACK;
				if (service.mCurrentTrack != null) {
					Track trackCopy = service.mCurrentTrack.newCopy();
					if (trackCopy != null) {
						// Bundle data = new Bundle();
						Intent finishPlay = new Intent();
						finishPlay.setAction(TRACK_FINISHED);
						service.mContext.sendBroadcast(finishPlay);
						// data.putSerializable(MESSAGE_VALUE,
						// (Serializable) trackCopy);
						// message.setData(data);
						// message.sendToTarget();
					}
				}
				if (service.mServiceHandler != null) {
					service.mServiceHandler.sendMessage(message);
				}
				break;
			case MESSAGE_SKIP_TO_PRVIOUS:
				message.what = PlayerService.MESSAGE_SKIP_TO_PREVIOUS_TRACK;
				if (service.mCurrentTrack != null) {
					Track trackCopy = service.mCurrentTrack.newCopy();
					if (trackCopy != null) {
						// Bundle data = new Bundle();
						Intent finishPlay = new Intent();
						finishPlay.setAction(TRACK_FINISHED);
						service.mContext.sendBroadcast(finishPlay);
						// data.putSerializable(MESSAGE_VALUE,
						// (Serializable) trackCopy);
						// message.setData(data);
						// message.sendToTarget();
					}
				}
				if (service.mServiceHandler != null) {
					service.mServiceHandler.sendMessage(message);
				}
				break;
			}

		}

	}

	private abstract class MediaLoaderTask implements Runnable {

		protected final Handler handler;
		protected final Track track;

		public MediaLoaderTask(Handler handler, Track track) {
			this.handler = handler;
			this.track = track;
		}

		protected void obtainMessage(int what) {
			if (what == MediaLoaderHandler.MESSAGE_CANCELLED
					&& mCurrentTrack != null && track != null) {
				try {
					Logger.i("", mCurrentTrack.getId()
							+ " Cancelled loading track ..... " + track.getId());
					if (mCurrentTrack.getId() != track.getId()) {
						return;
					}
				} catch (Exception e) {
					Logger.e("PlayerService:1362", e.toString());
				}
			}
			try {
				Message message = Message.obtain(handler, what);
				Bundle data = new Bundle();
				data.putSerializable(MESSAGE_VALUE, (Serializable) track);
				message.setData(data);
				message.sendToTarget();
			} catch (Exception e) {
			}
		}

		protected void obtainErrorMessage(Error error) {
			try {
				Message message = Message.obtain(handler,
						MediaLoaderHandler.MESSAGE_ERROR);
				Bundle data = new Bundle();
				data.putSerializable(MESSAGE_VALUE, (Serializable) track);
				data.putInt(MESSAGE_ERROR_VALUE, error.getId());
				message.setData(data);
				message.sendToTarget();
			} catch (Exception e) {
			}
		}

		protected void skipCurrentTrack(int what) {
			Message message = Message.obtain(handler, what);
			Bundle data = new Bundle();
			data.putSerializable(MESSAGE_VALUE, (Serializable) track);
			message.setData(data);
			message.sendToTarget();
		}
	}

	/*
	 * Task that loads the tracks playing properties and prepares it to play
	 * Music.
	 */

	MusicTrackLoaderTask musicTrackLoaderTask1;
	private Track adTrack = null;

	private boolean isCatchPlaying= false;
	private class MusicTrackLoaderTask extends MediaLoaderTask {
		boolean abort = false;
		public void interrupt()
		{
			abort=true;
		}



		public MusicTrackLoaderTask(Handler handler, Track track) {
			super(handler, track);
		}

		@Override
		public void run() {
			try {
				Logger.d(TAG, " Cast::::::::::::::::::::::::::: MusicTrackLoaderTask0:");
				musicTrackLoaderTask1 = MusicTrackLoaderTask.this;
				// start loading data.
				obtainMessage(MediaLoaderHandler.MESSAGE_INITIALIZED);

				isCatchPlaying= playCachedFile(this, track);
				if (isCatchPlaying /*&& !isCastConnected()*/) {
					return;
				}
				initializeMediaPlayer();
				isCatchPlaying = false;

				if (mApplicationConfigurations.getSaveOfflineMode()) {
					obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
					if (isPreviousClicked)
						skipCurrentTrack(MediaLoaderHandler.MESSAGE_SKIP_TO_PRVIOUS);
					else
						skipCurrentTrack(MediaLoaderHandler.MESSAGE_SKIP_CURRENT);
					return;
				}

				if (Thread.currentThread().isInterrupted() || abort) {
					obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
					return;
				}

                if (needToLoadAudioAd
                        && (!mApplicationConfigurations
                        .isUserHasSubscriptionPlan() || mApplicationConfigurations
                        .isUserHasTrialSubscriptionPlan())
                        && isAudioAdPosition()// adSkipCount % 4 == 1
                        && (mPlayMode == PlayMode.MUSIC
                        || mPlayMode == PlayMode.TOP_ARTISTS_RADIO || mPlayMode == PlayMode.DISCOVERY_MUSIC)
                        && !mApplicationConfigurations.getSaveOfflineMode()
                        && AudioAdCount < mApplicationConfigurations
                        .getAppConfigAudioAdSessionLimit()) {
                    CampaignsManager mCampaignsManager = CampaignsManager
                            .getInstance(getBaseContext());
                    placementAudioAd = mCampaignsManager
                            .getPlacementOfType(PlacementType.AUDIO_AD);
                    if (placementAudioAd != null
                            && placementAudioAd.getMp3Audio() != null
                            && placementAudioAd.getMp3Audio().length() > 0) {
                        try {
                            isAdPlaying = true;
                            needToLoadAudioAd = false;
                            AudioAdCount++;
                            currentPlayer.reset();
                            String audioAd = placementAudioAd
                                    .getMp3Audio();
							String adImageUrl = Utils.getDisplayProfileCasting(placementAudioAd);
							adTrack = new Track(0, getString(R.string.txtAdvertisement), getString(R.string.txtAdvertisement), getString(R.string.txtAdvertisement),
									adImageUrl, adImageUrl,
									null, -1);
                            adTrack.setMediaHandle(audioAd);
							/*if (isCastConnected() && !isCastRemoteLoaded()) {
								PlayCasting();
							} else if (isCastConnected()) {
								PlayCastSong();
							} else {*/
								currentPlayer.setDataSource(audioAd);
								obtainMessage(MediaLoaderHandler.MESSAGE_LOADED);
								currentPlayer
										.setAudioStreamType(AudioManager.STREAM_MUSIC);
								//currentPlayer.prepare1();

							/*MusicPlayerFunctions mp = new com.hungama.myplay.activity.util.MyMediaPlayer();
							mp.prepareAsync(new MusicPlayerListner.MyMusicOnPreparedListener() {
								@Override
								public void onPrepared(MusicPlayerFunctions mp) {

								}
							});*/

								currentPlayer.prepareAsync(new MusicPlayerListner.MyMusicOnPreparedListener() {
									@Override
									public void onPrepared(Object mp) {
										obtainMessage(MediaLoaderHandler.MESSAGE_PREPARED);
										if (currentPlayer != null && !currentPlayer.isPlaying()) {
											try {
												currentPlayer.start();
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									}
								});
								assignMediaListner();
							//}
                            return;
                        } catch (Exception e) {
                            isAdPlaying = false;
                            Logger.printStackTrace(e);
                            //obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
                        }
                    }
                }
                if (!needToLoadAudioAd && isAudioAdPosition()) {
                } else {
                    adSkipCount++;
                }

				/*
				 * Track's media handle should only been updated if it doesn't
				 * hold one, or it's been obsolete after 30 minutes.
				 */
				Calendar rightNow = Calendar.getInstance();
				boolean timeToRefresh = false;
				if (track != null)
					timeToRefresh = rightNow.getTimeInMillis()
							- MEDIA_HANDLE_UPDATE_TIME_DELTA_MILLIS >= track
							.getCurrentPrefetchTimestamp();
				if (TextUtils.isEmpty(track.getMediaHandle()) || timeToRefresh) {
					/*
					 * Retrieves for the given track its media handle string and
					 * updated it with the relevant playing properties.
					 */
					CommunicationManager communicationManager = new CommunicationManager();
					Map<String, Object> mediaHandleProperties = null;

					if (Thread.currentThread().isInterrupted() || abort) {
						obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
						return;
					}
					try {
						mediaHandleProperties = communicationManager
								.performOperation(
										new CMDecoratorOperation(mCMServerUrl,
												new MediaHandleOperation(
														mContext,
														track.getId(), false)),
										mContext);
						// if any error occurs, broadcasts an error and
						// terminates.
						if(mediaHandleProperties == null) {
							obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
							if (isPreviousClicked)
								skipCurrentTrack(MediaLoaderHandler.MESSAGE_SKIP_TO_PRVIOUS);
							else
								skipCurrentTrack(MediaLoaderHandler.MESSAGE_SKIP_CURRENT);
							return;
						}
					} catch (InvalidRequestException e) {
						e.printStackTrace();
						obtainErrorMessage(Error.DATA_ERROR);
						return;
					} catch (InvalidResponseDataException e) {
						e.printStackTrace();
						obtainErrorMessage(Error.SERVER_ERROR);
						return;
					} catch (OperationCancelledException e) {
						e.printStackTrace();
						obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
						return;
					} catch (NoConnectivityException e) {
						e.printStackTrace();
						obtainErrorMessage(Error.NO_CONNECTIVITY);
						return;
					}

					if (Thread.currentThread().isInterrupted() || abort) {
						obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
						return;
					}
					// populates the track with its playing properties.
					track.setMediaHandle((String) mediaHandleProperties
							.get(MediaHandleOperation.RESPONSE_KEY_HANDLE));
					track.setDeliveryId((Long) mediaHandleProperties
							.get(MediaHandleOperation.RESPONSE_KEY_DELIVERY_ID));
					track.setDoNotCache((Boolean) mediaHandleProperties
							.get(MediaHandleOperation.RESPONSE_KEY_DO_NOT_CACHE));

					if (mediaHandleProperties
							.get(MediaHandleOperation.RESPONSE_KEY_FILE_SIZE) != null) {
						mFileSize = ((Long) mediaHandleProperties
								.get(MediaHandleOperation.RESPONSE_KEY_FILE_SIZE));
					}

					/*
					 * sets the track's time when it was updated with the media
					 * handle.
					 */
					rightNow = Calendar.getInstance();
					track.setCurrentPrefetchTimestamp(rightNow
							.getTimeInMillis());
				}

				if (Thread.currentThread().isInterrupted() || abort) {
					obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
					return;
				}

				// media properties are loaded, prepare.
				obtainMessage(MediaLoaderHandler.MESSAGE_LOADED);

				if (!TextUtils.isEmpty(track.getMediaHandle())) {

					if (Thread.currentThread().isInterrupted() || abort) {
						obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
						return;
					}

                    try {
                        if (currentPlayer != null)
                            currentPlayer.reset();
                        else {
                            Logger.i(TAG, "Current Player Null");
                        }
                        Logger.i(TAG,
                                "Speedup = song URL" + track.getMediaHandle()
                                        + "  :: " + System.currentTimeMillis());
                        // xtpl
                        if (Thread.currentThread().isInterrupted() || abort) {
                            obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
                            return;
                        }

						Logger.d(TAG, " Cast::::::::::::::::::::::::::: MusicTrackLoaderTask1:");
                        /*if (isCastConnected() && !isCastRemoteLoaded()) {
							Logger.d(TAG, " Cast::::::::::::::::::::::::::: MusicTrackLoaderTask2:");
                            PlayCasting();
                        } else if (isCastConnected()) {
							Logger.d(TAG, " Cast::::::::::::::::::::::::::: MusicTrackLoaderTask3:");
                            PlayCastSong();
                        } else {*/
							Logger.d(TAG, " Cast::::::::::::::::::::::::::: MusicTrackLoaderTask4:");
                            startInitilization(getCurrentPlayingTrackPosition(),
                                    false, MusicTrackLoaderTask.this, track);
                            /*if (currentPlayer != null) {
								try {
									currentPlayer.start();
								} catch (Exception e) {
								}
							}*/
                        //}
                        //PlayCasting();

						// // xtpl
						// obtainMessage(MediaLoaderHandler.MESSAGE_PREPARED);
						Logger.i(
								"Playing Audio URL",
								"Audio URL MESSAGE_PREPARED: "
										+ track.getMediaHandle());

						if (Thread.currentThread().isInterrupted()) {
							obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
							return;
						}

						// startPrefetchingMediaHandles();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
						Logger.i(
								"Playing Audio URL",
								"Audio URL SERVER_ERROR: "
										+ track.getMediaHandle());
						obtainErrorMessage(Error.SERVER_ERROR);
						return;
					} catch (SecurityException e) {
						e.printStackTrace();
						Logger.i(
								"Playing Audio URL",
								"Audio URL SERVER_ERROR: "
										+ track.getMediaHandle());
						obtainErrorMessage(Error.SERVER_ERROR);
						return;
					} catch (IllegalStateException e) {
						e.printStackTrace();
						// if (isCancelled() || Thread.interrupted()) {
						// obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
						// return false;
						// }
						obtainMessage(MediaLoaderHandler.MESSAGE_ERROR);
						return;
					}
				} else {
					// no uri for loading data.
					Logger.e(TAG,
							"No loading uri for media item: " + track.getId());
					Logger.i("Playing Audio URL",
							"Audio URL DATA_ERROR: TrackId " + track.getId());
					obtainErrorMessage(Error.DATA_ERROR);
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Task that loads the tracks playing ad-hoc tracks from URL to Web Radio.
	 */
	private RadioTrackLoaderTask radioTrackLoaderTask1;
	private class RadioTrackLoaderTask extends MediaLoaderTask {

		boolean abort=false;
		public void interrupt()
		{
			abort=true;
		}
		public RadioTrackLoaderTask(Handler handler, Track track) {
			super(handler, track);
		}

		@Override
		public void run() {
			radioTrackLoaderTask1 = this;
			// start loading data.
			obtainMessage(MediaLoaderHandler.MESSAGE_INITIALIZED);
			if (Thread.currentThread().isInterrupted() || abort) {
				obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
				return;
			}
			obtainMessage(MediaLoaderHandler.MESSAGE_LOADED);
			if (Thread.currentThread().isInterrupted() || abort) {
				obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
				return;
			}

			if (!TextUtils.isEmpty(track.getMediaHandle())) {

				Logger.e(TAG,
						"Playing Live Radio URL: " + track.getMediaHandle());

				if (Thread.currentThread().isInterrupted() || abort) {
					obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
					return;
				}

				try {
					/*if (isCastConnected() && !isCastRemoteLoaded()) {
						PlayCasting();
					} else if (isCastConnected()) {
						PlayCastSong();
					} else {*/
						if (currentPlayer == null) {
							initializeMediaPlayer();
							/*currentPlayer = new com.hungama.myplay.activity.util.MyMediaPlayer(0);
							assignMediaListner();*/
						}
						currentPlayer.reset();

						currentPlayer.setDataSource(track.getMediaHandle());
						currentPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
						if (Thread.currentThread().isInterrupted()|| abort) {
							obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
							return;
						}
						// prepare the media to play the track.
						try {
							//currentPlayer.prepare1();
							currentPlayer.prepareAsync(new MusicPlayerListner.MyMusicOnPreparedListener() {
								@Override
								public void onPrepared(Object mp) {
									obtainMessage(MediaLoaderHandler.MESSAGE_PREPARED);
									if (currentPlayer != null && !currentPlayer.isPlaying()) {
										try {
											currentPlayer.start();
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}
							});

							/*currentPlayer.prepareAsync(new MediaPlayer.OnPreparedListener() {
								@Override
								public void onPrepared(MediaPlayer mp) {
									obtainMessage(MediaLoaderHandler.MESSAGE_PREPARED);
								}
							});*/
						} catch (Exception exception) {
							obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
							return;
						}
						//obtainMessage(MediaLoaderHandler.MESSAGE_PREPARED);
					//}
					if (Thread.currentThread().isInterrupted()|| abort) {
						obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
						return;
					}

				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					obtainErrorMessage(Error.SERVER_ERROR);
					return;
				} catch (SecurityException e) {
					e.printStackTrace();
					obtainErrorMessage(Error.SERVER_ERROR);
					return;
				} catch (IllegalStateException e) {
					e.printStackTrace();
					obtainMessage(MediaLoaderHandler.MESSAGE_ERROR);
					return;
				} catch (Exception e) {
					e.printStackTrace();
					obtainErrorMessage(Error.NO_CONNECTIVITY);
					return;
				}

			} else {
				// no uri for loading data.
				Logger.e(TAG, "No loading uri for media item: " + track.getId());
				obtainErrorMessage(Error.DATA_ERROR);
				return;
			}
		}

	}

	/*
	 * Prefetches media handles for the  next of the current casting
	 * track.
	 */

	private class GetMusicTrackHandle implements Runnable{

		boolean needToPlay = false;
		private Track track;
		@Override
		public void run() {
//			if (musicTrackHandleThread.isInterrupted()) {
//				return;
//			}
			try {
				track = prefetchTrackMediaHandle(track);
				Logger.i("Chromecast", "Media handle Current Track: " + track.getMediaHandle());
				if (track != null && !TextUtils.isEmpty(track.getMediaHandle())) {
					if (needToPlay) {
						mCurrentTrack = track;
						handlerChromeCast.post(runPlayToChromeCast);
					} else if (mCurrentTrack.getId() == track.getId()) {
						mCurrentTrack.setMediaHandle(track.getMediaHandle());
						handlerChromeCast.post(runAddToChromeCast);
					} else {
						getNextTrack().setMediaHandle(track.getMediaHandle());
						handlerChromeCast.post(runAddToChromeCast);
					}
				}
			} catch (Exception e) {
				Logger.i("Chromecast", e.getMessage());
			}
		}
		boolean cancelled = false;

		public void cancel() {
			if (prefetchTask != null)
				prefetchTask.interrupt();
			// Thread.currentThread().interrupt();
			cancelled = true;
		}


		public void needToPlay(boolean needToPlay, Track track) {
			this.needToPlay = needToPlay;
			this.track = track;
		}

	}

	public Handler handlerChromeCast = new Handler();
	Runnable runPlayToChromeCast = new Runnable() {
		@Override
		public void run() {
			//PlayCasting();
		}
	};

	Runnable runAddToChromeCast = new Runnable() {
		@Override
		public void run() {
			/*if(musicTrackHandle!=null){
				if(musicTrackHandle.track.getId() == mCurrentTrack.getId()){
					appendTrackToCasting(mCurrentTrack);
				}else{
					appendTrackToCasting(getNextTrack());
				}
			}*/
		}
	};

	/*
	 * Prefetches media handles for the prev and the next of the current playing
	 * track.
	 */
	private class MusicTrackHandlesPrefetchingTask implements Runnable {

		boolean isInterrupted=false;
		public void interrupt()
		{
			isInterrupted=true;
//			Thread.currentThread().interrupt();
		}

		@Override
		public void run() {

			if (isInterrupted || cancelled)
			{
				return;
			}

			if (mPlayingQueue.hasPrevious()
					&& !mApplicationConfigurations.getSaveOfflineMode()) {
				Track prevTrack = mPlayingQueue.getPreviousTrack();
				if (prevTrack != null) {
					try {
						if (mPlayMode == PlayMode.MUSIC) {
							if (!CacheManager.isProUser(mContext)
									|| (CacheManager.isProUser(mContext)
											&& mApplicationConfigurations
													.getSaveOfflineAutoSaveMode() && !mApplicationConfigurations
												.getSaveOfflineMode())) {
								MediaItem mediaItem = new MediaItem(
										prevTrack.getId(),
										prevTrack.getTitle(),
										prevTrack.getAlbumName(),
										prevTrack.getArtistName(),
										prevTrack.getImageUrl(),
										prevTrack.getBigImageUrl(),
										MediaType.TRACK.toString()
												.toLowerCase(), 0, 0,
										prevTrack.getImages(),
										prevTrack.getAlbumId());
								// new MediaCachingTask(mContext, mediaItem,
								// prevTrack, true).execute();
								CacheManager.autoSaveOfflinePlayerQueue(
										mContext, mediaItem, prevTrack);
							}
						}
						prevTrack = prefetchTrackMediaHandle(prevTrack);
					} catch (Exception e) {
						Logger.printStackTrace(e);
						return;
					}
				}
			}

			if (isInterrupted || cancelled) {
				return;
			}

			if (mPlayingQueue.hasNext()
					&& !mApplicationConfigurations.getSaveOfflineMode()) {
				Track nextTrack = mPlayingQueue.getNextTrack();
				if (nextTrack != null) {
					try {
						if (mPlayMode == PlayMode.MUSIC) {
							if (!CacheManager.isProUser(mContext)
									|| (CacheManager.isProUser(mContext)
											&& mApplicationConfigurations
													.getSaveOfflineAutoSaveMode() && !mApplicationConfigurations
												.getSaveOfflineMode())) {
								MediaItem mediaItem = new MediaItem(
										nextTrack.getId(),
										nextTrack.getTitle(),
										nextTrack.getAlbumName(),
										nextTrack.getArtistName(),
										nextTrack.getImageUrl(),
										nextTrack.getBigImageUrl(),
										MediaType.TRACK.toString()
												.toLowerCase(), 0, 0,
										nextTrack.getImages(),
										nextTrack.getAlbumId());
								// new MediaCachingTask(mContext, mediaItem,
								// nextTrack, true).execute();
								CacheManager.autoSaveOfflinePlayerQueue(
										mContext, mediaItem, nextTrack);
							}
						}
						nextTrack = prefetchTrackMediaHandle(nextTrack);

					} catch (Exception e) {
						Logger.printStackTrace(e);
						return;
					}
				}
			}

			if (isInterrupted || cancelled) {
				return;
			}

			if (!mApplicationConfigurations.getSaveOfflineMode()) {
				preCacheTracks();
				// PrefetchUtil.get(getApplicationContext())
				// .cacheNextPrevTempFiles();
			}
		}

		boolean cancelled = false;

		public void cancel() {
//			if (prefetchThread != null)
				interrupt();
			// Thread.currentThread().interrupt();
			cancelled = true;
		}
	}

	// ======================================================
	// Logging PlayEvents.
	// ======================================================

	private void startLoggingEvent() {
		DeviceConfigurations config = DeviceConfigurations
				.getInstance(mContext);
		mEventStartTimestamp = config.getTimeStampDelta();
	}

	private void stopLoggingEvent(boolean hasCompletePlay) {
		if (mCurrentTrack != null && mEventStartTimestamp!=null && (isPlayStarted && !isErrorOccured)
		/* && !mApplicationConfigurations.getSaveOfflineMode() */) {
			int playDuration1 = getCurrentPlayerPosition();
			int totalDuration = getDuration();
			PlayingSourceType playingSourceType;
			/*if(isCastConnected() && !isAppConnectFirstTime){
				if(playDuration1>0)
					playingSourceType = PlayingSourceType.CAST;
				else if(totalDurationForCast>0){
					playingSourceType = PlayingSourceType.CAST;
					totalDuration = totalDurationForCast;
					playDuration1 = totalDuration;
				}else
					return;
			}else if(playDurationForCast>0 && totalDurationForCast>0){
					playingSourceType = PlayingSourceType.CAST;
					totalDuration = totalDurationForCast;
					playDuration1 = playDurationForCast;
			} else {*/
				if (mApplicationConfigurations.getSaveOfflineMode()) {
					if (DBOHandler.getTrackCacheState(mContext,
							"" + mCurrentTrack.getId()) == CacheState.CACHED)
						playingSourceType = PlayingSourceType.CACHED;
					else {
						try {
							if (playDuration1> 0)
								playingSourceType = PlayingSourceType.STREAM;
							else
								return;
						} catch (Exception e) {
							return;
						}
					}
				} else {
					if (mCurrentTrack.isCached()) {
						playingSourceType = PlayingSourceType.CACHED;
					} else {
						playingSourceType = PlayingSourceType.STREAM;
					}
				}
			//}
			int playCurrentPostion = (playDuration1 / 1000);
			if (!needToLoadAudioAd
					&& playCurrentPostion >= mApplicationConfigurations
					.getAppConfigAudioAdFrequency()) {
				needToLoadAudioAd = true;
			}
			Logger.i(TAG, "playCurrentPostion Duration:" + playCurrentPostion);
			if (hasCompletePlay && currentPlayer != null) {
				playCurrentPostion = (totalDuration / 1000);
			}
			int playDuration = playCurrentPostion;
			Logger.i(TAG, "playDuration:" + playDuration);
			if (playDuration > 1000) {
				DeviceConfigurations deviceConfigurations = DeviceConfigurations
						.getInstance(mContext);
				Logger.i(
						TAG,
						"Current Position MediaPlayer:"
								+ playDuration1
								+ " Current Position MediaPlayer:"
								+ playCurrentPostion + " Duration:"
								+ playDuration + " MediaId:"
								+ mCurrentTrack.getId() + " Device:"
								+ deviceConfigurations.getDeviceModelName());
			}
			int consumerId = mDataManager.getApplicationConfigurations()
					.getConsumerID();
			String deviceId = mDataManager.getApplicationConfigurations()
					.getDeviceID();

			Logger.i(TAG, "delivery id:" + mCurrentTrack.getDeliveryId()
					+ " id:" + mCurrentTrack.getId() + " " + playingSourceType);
			if (currentPlayer != null
					&& playDuration <= totalDuration && playDuration>0) {

				PlayEvent playEvent;
				playEvent = new PlayEvent(consumerId, deviceId,
						mCurrentTrack.getDeliveryId(), hasCompletePlay,
						playDuration, mEventStartTimestamp, 0, 0,
						mCurrentTrack.getId(), "track", playingSourceType, 0,
						playCurrentPostion);
				if (mCurrentTrack.getAlbumId() != 0)
					playEvent.setAlbum_id(mCurrentTrack.getAlbumId());

				try {
					if (mPlayMode == PlayMode.MUSIC) {
						if (mCurrentTrack.getTag() instanceof Playlist) {
							Playlist playlist = (Playlist) mCurrentTrack
									.getTag();
							playEvent.setUserPlaylistName(playlist.getName());
						} else {
							MediaItem mediaItem = (MediaItem) mCurrentTrack
									.getTag();
							if (mediaItem != null
									&& mediaItem.getMediaType() == MediaType.PLAYLIST) {
								playEvent.setPlaylistDetails(
										"" + mediaItem.getId(),
										mediaItem.getTitle());
							} else if (mediaItem != null
									&& mediaItem.getMediaType() == MediaType.ALBUM
									&& mediaItem.getId() != 0) {
								playEvent.setAlbum_id(mediaItem.getId());
							}
						}
					} else if (mPlayMode == PlayMode.TOP_ARTISTS_RADIO) {
						if (playDuration * 1000 >= PlayerService.TIME_REPORT_BADGES_MILLIES) {
							reportBadgesAndCoins();
						}
						MediaItem mediaItem = (MediaItem) mCurrentTrack
								.getTag();
						if (mediaItem != null) {
							Logger.s("Media type :::::::::::::::::: " + mediaItem.getMediaType());
							if(mediaItem.getMediaType() == MediaType.ARTIST_OLD){
								playEvent.setArtistRadioDetails("" + mediaItem.getId());
							} else {
								playEvent.setOnDemandRadioDetails("" + mediaItem.getId(), "" + mediaItem.getTitle());
							}
						}
					} else if (mPlayMode == PlayMode.DISCOVERY_MUSIC) {
						if (playDuration * 1000 >= PlayerService.TIME_REPORT_BADGES_MILLIES) {
							reportBadgesAndCoins();
						}
						playEvent.setDiscovery(true);
						if (prevDiscover != null) {
							playEvent.setHashTag(prevDiscover.getHashTag());
						} else if (mDiscover != null) {
							playEvent.setHashTag(mDiscover.getHashTag());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					mDataManager.addEvent(playEvent);
					DBOHandler.insertMediaConsumptionDetail(this, playDuration, 0);

					if(mPlayMode != PlayMode.TOP_ARTISTS_RADIO) {
						if (playingSourceType == PlayingSourceType.CACHED) {
							if(playDuration > 10) {
								ApsalarEvent.postEvent(getApplicationContext(),
										ApsalarEvent.SONG_PLAYED_OFFLINE, ApsalarEvent.TYPE_PLAY_OFFLINE);
								ApsalarEvent.postEvent(getApplicationContext(),
										ApsalarEvent.MEDIA_PLAYED);
							}
							Set<String> tags = Utils.getTags();
							String tag = Constants.UA_TAG_OFFLINE_SONGPLAYED;
							if (!tags.contains(tag)) {
								tags.add(tag);
								Utils.AddTag(tags);
							}
						}else {
							if(playDuration > 10) {
								ApsalarEvent.postEvent(getApplicationContext(),
										ApsalarEvent.SONG_PLAYED_ONLINE, ApsalarEvent.TYPE_PLAY_ONLINE);
								ApsalarEvent.postEvent(getApplicationContext(),
										ApsalarEvent.MEDIA_PLAYED);
							}
						}
					}else {
						if(playDuration > 10) {
							ApsalarEvent.postEvent(getApplicationContext(), ApsalarEvent.RADIO_PLAYED);
							ApsalarEvent.postEvent(getApplicationContext(), ApsalarEvent.MEDIA_PLAYED);
						}
					}
				} catch (Exception e) {
				}
			}
		}
		mEventStartTimestamp = null;
		//totalDurationForCast = 0;
		//playDurationForCast = 0;
	}

	/*private void stopLoggingEvent(boolean hasCompletePlay) {
		if (mCurrentTrack != null && (isPlayStarted && !isErrorOccured)
		*//* && !mApplicationConfigurations.getSaveOfflineMode() *//*) {
			int lastPos = getCurrentPlayerPosition();
			PlayingSourceType playingSourceType;
			if(isCastConnected()){
				if(lastPos>0)
					playingSourceType = PlayingSourceType.CAST;
				else if(totalDurationForCast>0){
					playingSourceType = PlayingSourceType.CAST;
					lastPos = totalDurationForCast;
				}else
					return;
			}else {
				if (mApplicationConfigurations.getSaveOfflineMode()) {
					if (DBOHandler.getTrackCacheState(mContext,
							"" + mCurrentTrack.getId()) == CacheState.CACHED)
						playingSourceType = PlayingSourceType.CACHED;
					else {
						try {
							if (lastPos > 0)
								playingSourceType = PlayingSourceType.STREAM;
							else
								return;
						} catch (Exception e) {
							return;
						}
					}
				} else {
					if (mCurrentTrack.isCached()) {
						playingSourceType = PlayingSourceType.CACHED;
					} else {
						playingSourceType = PlayingSourceType.STREAM;
					}
				}
			}
			int playCurrentPostion = (lastPos / 1000);
			if (!needToLoadAudioAd
					&& playCurrentPostion >= mApplicationConfigurations
							.getAppConfigAudioAdFrequency()) {
				needToLoadAudioAd = true;
			}
			Logger.i(TAG, "playCurrentPostion Duration:" + playCurrentPostion);
			if (hasCompletePlay && currentPlayer != null) {
				playCurrentPostion = (lastPos / 1000);
			}
			int playDuration = playCurrentPostion;
			Logger.i(TAG, "playDuration:" + playDuration);
			if (playDuration > 1000) {
				DeviceConfigurations deviceConfigurations = DeviceConfigurations
						.getInstance(mContext);
				Logger.i(
						TAG,
						"Current Position MediaPlayer:"
								+ lastPos
								+ " Current Position MediaPlayer:"
								+ playCurrentPostion + " Duration:"
								+ playDuration + " MediaId:"
								+ mCurrentTrack.getId() + " Device:"
								+ deviceConfigurations.getDeviceModelName());
			}
			int consumerId = mDataManager.getApplicationConfigurations()
					.getConsumerID();
			String deviceId = mDataManager.getApplicationConfigurations()
					.getDeviceID();

			Logger.i(TAG, "delivery id:" + mCurrentTrack.getDeliveryId()
					+ " id:" + mCurrentTrack.getId() + " " + playingSourceType);
			if (currentPlayer != null
					&& playDuration <= getDuration()) {

				PlayEvent playEvent;
				playEvent = new PlayEvent(consumerId, deviceId,
						mCurrentTrack.getDeliveryId(), hasCompletePlay,
						playDuration, mEventStartTimestamp, 0, 0,
						mCurrentTrack.getId(), "track", playingSourceType, 0,
						playCurrentPostion);
				if (mCurrentTrack.getAlbumId() != 0)
					playEvent.setAlbum_id(mCurrentTrack.getAlbumId());

				try {
					if (mPlayMode == PlayMode.MUSIC) {
						if (mCurrentTrack.getTag() instanceof Playlist) {
							Playlist playlist = (Playlist) mCurrentTrack
									.getTag();
							playEvent.setUserPlaylistName(playlist.getName());
						} else {
							MediaItem mediaItem = (MediaItem) mCurrentTrack
									.getTag();
							if (mediaItem != null
									&& mediaItem.getMediaType() == MediaType.PLAYLIST) {
								playEvent.setPlaylistDetails(
										"" + mediaItem.getId(),
										mediaItem.getTitle());
							} else if (mediaItem != null
									&& mediaItem.getMediaType() == MediaType.ALBUM
									&& mediaItem.getId() != 0) {
								playEvent.setAlbum_id(mediaItem.getId());
							}
						}
					} else if (mPlayMode == PlayMode.TOP_ARTISTS_RADIO) {
						if (playDuration * 1000 >= PlayerService.TIME_REPORT_BADGES_MILLIES) {
							reportBadgesAndCoins();
						}
						MediaItem mediaItem = (MediaItem) mCurrentTrack
								.getTag();
						if (mediaItem != null) {
							Logger.s("Media type :::::::::::::::::: " + mediaItem.getMediaType());
							if(mediaItem.getMediaType() == MediaType.ARTIST_OLD){
								playEvent.setArtistRadioDetails("" + mediaItem.getId());
							} else {
								playEvent.setOnDemandRadioDetails("" + mediaItem.getId(), "" + mediaItem.getTitle());
							}
						}
					} else if (mPlayMode == PlayMode.DISCOVERY_MUSIC) {
						if (playDuration * 1000 >= PlayerService.TIME_REPORT_BADGES_MILLIES) {
							reportBadgesAndCoins();
						}
						playEvent.setDiscovery(true);
						if (prevDiscover != null) {
							playEvent.setHashTag(prevDiscover.getHashTag());
						} else if (mDiscover != null) {
							playEvent.setHashTag(mDiscover.getHashTag());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					mDataManager.addEvent(playEvent);
					DBOHandler.insertMediaConsumptionDetail(this, playDuration, 0);

                    if(mPlayMode != PlayMode.TOP_ARTISTS_RADIO) {
                        if (playingSourceType == PlayingSourceType.CACHED)
                            ApsalarEvent.postEvent(getApplicationContext(),
                                    ApsalarEvent.SONG_PLAYED_OFFLINE, ApsalarEvent.TYPE_PLAY_OFFLINE);
                        Set<String> tags = Utils.getTags();
                        String tag= Constants.UA_TAG_OFFLINE_SONGPLAYED;
                        if (!tags.contains(tag)) {
                            tags.add(tag);
                            Utils.AddTag(tags);
                        }
                        else
                            ApsalarEvent.postEvent(getApplicationContext(),
                                    ApsalarEvent.SONG_PLAYED_ONLINE, ApsalarEvent.TYPE_PLAY_ONLINE);
                    }else
                        ApsalarEvent.postEvent(getApplicationContext(), ApsalarEvent.RADIO_PLAYED);

				} catch (Exception e) {
				}
			}
		}
		mEventStartTimestamp = null;
		totalDurationForCast = 0;
	}*/

	// ======================================================
	// Sleep Receiver.
	// ======================================================

	private static final class SleepReciever extends BroadcastReceiver {

		private final WeakReference<PlayerService> playerServiceReference;

		SleepReciever(PlayerService playerService) {
			this.playerServiceReference = new WeakReference<PlayerService>(
					playerService);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equalsIgnoreCase(
					SleepModeManager.COUNT_DOWN_TIMER_FINISH_INTENT)) {
				// gets the instance of the player and contolls it.
				PlayerService playerService = playerServiceReference.get();
				if (playerService != null) {
					if (playerService.isPlaying()) {
						playerService.pause();
						playerService.sendBroadcast(new Intent(
								PlayerBarFragment.ACTION_PLAY_STATE_CHANGED));
					} else if (playerService.isLoading()) {
						playerService.mShouldPauseAfterLoading = true;
					}
				}
			}
		}

	}

	// ======================================================
	// Notification helper methods.
	// ======================================================

	// private static final int NOTIFICATION_PLAYING_CODE = 123456;

	private void updateNotificationForTrack(Track track) {
		Logger.s(" ::::::::::::::updateNotificationForTrack:::::::::::::::::::: ");
		updatewidget();

	}

	private void dismissNotification() {
		// stopForeground(true);
		Logger.s(" ::::::::::::::dismissNotification:::::::::::::::::::: ");
		updatewidget();

	}

	/*
	 * Updater for the progress bar and the current playing time.
	 */
	public static class PlayerProgressCounter implements Runnable {
		private WeakReference<PlayerService> playerServiceReference = null;

		private PlayerProgressCounter(PlayerService playerService) {
			playerServiceReference = new WeakReference<PlayerService>(
					playerService);
		}

//		public void execute() {
//			start();
//		}

		@Override
		public void run() {
			doInBackground();
//			super.run();
		}

		// @Override
		protected Void doInBackground(Void... params) {
			while (!isCancelled()) {
				try {
					publishProgress();
					if (isCancelled()) {
						break;
					}
					Thread.sleep(1000);
				} catch (Exception e) {
					Logger.d(TAG, "Cancelling playing progress update.");
					break;
				}

			}
			return null;
		}

		boolean cancelled = false;

		private boolean isCancelled() {
			return cancelled;
		}

		protected void publishProgress(Void... values) {
			// Logger.s("---------------onProgressUpdate--------------");
			final PlayerService playerService = playerServiceReference.get();
			if (playerService != null) {
				PlayerService.State state = playerService.getState();

				try {
					if (playerService.currentPlayer != null
							&& state == PlayerService.State.PLAYING
							&& (playerService.currentPlayer.isPlaying() /*|| playerService.isCastRemotePlaying()*/)) {
						// gets the values.

						playerService.isErrorOccured = false;
						final int progress = (int) (((float) playerService
								.getCurrentPlayerPosition() / playerService
								.getDuration()) * 100);
						final String label = Utils
								.secondsToString(playerService
										.getCurrentPlayerPosition() / 1000)
								+ " / ";

						for (PlayerBarUpdateListener listener : playerService.mOnPlayerUpdateListeners) {
							// Logger.i(TAG, "publishProgress :" + label);
							listener.OnPlayerBarUpdate(progress, label);
						}

						int timeMilliseconds = (playerService.getDuration() / 100)
								* progress;
						if (timeMilliseconds >= PlayerService.TIME_REPORT_BADGES_MILLIES) {
							PlayerService.service.handler.post(new Runnable() {

								@Override
								public void run() {
									playerService.reportBadgesAndCoins();
								}
							});
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				cancel(true);
			}
		}

		private void cancel(boolean b) {
			cancelled = b;
		}
	}

	public void startProgressUpdater() {
		if (mPlayerProgressCounter != null
				&& !mPlayerProgressCounter.isCancelled())
			mPlayerProgressCounter.cancel(true);
		mPlayerProgressCounter = new PlayerProgressCounter(this);
		ThreadPoolManager.getInstance().submit(mPlayerProgressCounter);
//		mPlayerProgressCounter.execute();
		Logger.i(TAG, "EXECUTED - Build VERSION LESS THAN HONEYCOMB");
	}

	public void stopProgressUpdater() {
		if (mPlayerProgressCounter != null)
			Logger.s("----stopProgressUpdater --- "
					+ !mPlayerProgressCounter.cancelled);
		if (mPlayerProgressCounter != null
				&& (!mPlayerProgressCounter.cancelled)) {

			mPlayerProgressCounter.cancel(true);
			mPlayerProgressCounter = null;
		}
	}

	private Set<PlayerBarUpdateListener> mOnPlayerUpdateListeners = new HashSet<PlayerBarUpdateListener>();

	public void registerPlayerUpdateListeners(PlayerBarUpdateListener listner) {
		if (!mOnPlayerUpdateListeners.contains(listner))
			mOnPlayerUpdateListeners.add(listner);
	}

	public void unregisterPlayerUpdateListeners(PlayerBarUpdateListener listner) {
		if (mOnPlayerUpdateListeners.contains(listner))
			mOnPlayerUpdateListeners.remove(listner);
	}

	public interface PlayerBarUpdateListener {

		public void OnPlayerBarUpdate(int progress, String label);
	}

	public void updateTrack(Track track) {
		mPlayingQueue.updateTrack(track);
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public int getAudioSessionId() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			try {
				return currentPlayer.getAudioSessionId();
			} catch (Exception e) {
				Logger.e(TAG + ":2044", e.toString());
				return 0;
			}
		} else {
			return 0;
		}
	}

	public boolean isAdPlaying() {
		return isAdPlaying;
	}

	public int getCurrentPlayingTrackPosition() {
		return mPlayingQueue.getCurrentPosition();
	}

    FileInputStream newFis = null;

    private boolean playCachedFile(final MusicTrackLoaderTask musicTrackLoaderTask,
                                   Track track) throws IOException {
        try {

            String playingPath = DBOHandler.getTrackPathById(mContext, ""
                    + track.getId());

			if(TextUtils.isEmpty(playingPath)){
				return false;
			}

            // Read file from cache
            File file = new File(playingPath);
			/*if(isCastConnected())
				return  file.exists();*/
            if (file.exists()) {
                if (needToLoadAudioAd
                        && (!mApplicationConfigurations
                        .isUserHasSubscriptionPlan() || mApplicationConfigurations
                        .isUserHasTrialSubscriptionPlan())
                        && isAudioAdPosition()// adSkipCount % 4 == 1
                        && (mPlayMode != PlayMode.LIVE_STATION_RADIO)
                        && !mApplicationConfigurations.getSaveOfflineMode()
                        && AudioAdCount < mApplicationConfigurations
                        .getAppConfigAudioAdSessionLimit()) {

                    CampaignsManager mCampaignsManager = CampaignsManager
                            .getInstance(getBaseContext());
                    placementAudioAd = mCampaignsManager
                            .getPlacementOfType(PlacementType.AUDIO_AD);

                    if (placementAudioAd != null
                            && placementAudioAd.getMp3Audio() != null
                            && placementAudioAd.getMp3Audio().length() > 0) {
                        try {

                            isAdPlaying = true;
                            needToLoadAudioAd = false;
                            AudioAdCount++;
                            musicTrackLoaderTask
                                    .obtainMessage(MediaLoaderHandler.MESSAGE_LOADED);
                            currentPlayer.reset();
                            currentPlayer.setDataSource(placementAudioAd
                                    .getMp3Audio());
                            currentPlayer
                                    .setAudioStreamType(AudioManager.STREAM_MUSIC);
                            currentPlayer.prepare();
							if (currentPlayer != null && !currentPlayer.isPlaying()) {
								try {
									currentPlayer.start();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
                            musicTrackLoaderTask
                                    .obtainMessage(MediaLoaderHandler.MESSAGE_PREPARED);
							assignMediaListner();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (!isAdPlaying) {
                    boolean decrypt = true;// mCurrentTrack.isEncrypted();
                    FileInputStream fis = new FileInputStream(file);

                    StringBuilder newPlayingPath = new StringBuilder(
                            playingPath);
                    int dot = newPlayingPath.lastIndexOf(".");
                    newPlayingPath.insert(dot, "decrypt");

                    FileOutputStream fos = new FileOutputStream(
                            newPlayingPath.toString());
                    CMEncryptor cmEncrpyt = new CMEncryptor("630358525");
                    byte[] buffer = new byte[1024 * 100];

                    int bytesRead = 0;

                    if (decrypt)
                        Logger.i("START DECRYPT", mCurrentTrack.getTitle());
                    else
                        Logger.i("START READING", mCurrentTrack.getTitle());
                    Logger.i("PlayerService", "Decrypt Started");
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        if (decrypt) {
                            cmEncrpyt.decrypt(buffer, 0, bytesRead);

                        }
                        fos.write(buffer, 0, bytesRead);
                    }
                    Logger.i("PlayerService", "Decrypt Ended");
                    if (decrypt)
                        Logger.i("END DECRYPT", mCurrentTrack.getTitle());
                    else
                        Logger.i("END READING", mCurrentTrack.getTitle());

                    fos.close();
                    fis.close();

                    if (!needToLoadAudioAd && isAudioAdPosition()) {
                    } else {
                        adSkipCount++;
                    }

                    try {
                        String response = DBOHandler.getTrackDetails(this, ""
                                + track.getId());
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONObject jsonCatalog = jsonResponse
                                .getJSONObject("response");

                        track.setDeliveryId(jsonCatalog.getLong("delivery_id"));
                        track.setCached(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Read file from cache
                    final File newFile = new File(newPlayingPath.toString());
                    newFis = new FileInputStream(newFile);
                    musicTrackLoaderTask
                            .obtainMessage(MediaLoaderHandler.MESSAGE_LOADED);
					initializeMediaPlayer();
                    currentPlayer.reset();
                    // mMediaPlayer.setOnPreparedListener(this);
					if(currentPlayer instanceof ExoMusicPlayer){
						currentPlayer.setDataSource(newFile.getAbsolutePath());
					}else {
						currentPlayer.setDataSource(newFis.getFD());
					}
                    currentPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    // mState = State.PREPARING;
                    //currentPlayer.prepare1();
					currentPlayer.prepareAsync(new MusicPlayerListner.MyMusicOnPreparedListener() {
						@Override
						public void onPrepared(Object mp) {
							musicTrackLoaderTask
									.obtainMessage(MediaLoaderHandler.MESSAGE_PREPARED);
							if (currentPlayer != null && !currentPlayer.isPlaying()) {
								try {
									currentPlayer.start();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							try {
								newFis.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
							newFile.delete();
						}
					});

					/*currentPlayer.prepareAsync(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            musicTrackLoaderTask
                                    .obtainMessage(MediaLoaderHandler.MESSAGE_PREPARED);
                            try {
                                newFis.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            newFile.delete();
                        }
                    });*/
					assignMediaListner();
					/*musicTrackLoaderTask
							.obtainMessage(MediaLoaderHandler.MESSAGE_PREPARED);
					newFis.close();
					newFile.delete();*/
                    // mState = State.PREPARED;
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (java.lang.Error e) {
            System.gc();
            System.runFinalization();
            System.gc();
        }
        return false;
    }

	public Track trackDragAndDrop(int from, int to) {
		if (mPlayingQueue != null) {
			mPlayingQueue.trackDragAndDrop(from, to);
			//updateCastingQueue();
		}
		return null;
	}

	public void updateNotificationForOffflineMode() {
		if (mCurrentTrack != null && mCurrentState == State.PLAYING) {
			updateNotificationForTrack(mCurrentTrack);
		}
	}

	private boolean PausebyCall = false;
	private boolean CallInProgress = false;

	private BroadcastReceiver callReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
			TelephonyManager tm = (TelephonyManager) context
					.getSystemService(TELEPHONY_SERVICE);

			if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
				CallInProgress = false;
				if (PausebyCall)
					play();
				PausebyCall = false;
			} else {
				try {
					if ((currentPlayer.isPlaying() || needToUseCastingPlayer())) {
						pause();
						PausebyCall = true;
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
				CallInProgress = true;
			}
			}catch (Exception e){
				CallInProgress = false;
				PausebyCall = false;
			}
		}
	};

	private class LiveRadioUpdater implements Runnable {// AsyncTask<Void, Void,
													// Void> {
		private WeakReference<PlayerService> playerBarReference = null;
		private Track radioTrack;
		private LiveStationDetails detail, previousTrackDetails;
		// private int duration = 1000;
		private boolean cancelled = false;

		// private int prefixColor, suffixColor;

		public LiveRadioUpdater(PlayerService playerBar, Track radioTrack) {
			playerBarReference = new WeakReference<PlayerService>(playerBar);
			this.radioTrack = radioTrack;
			startLiveRadioLoggingEvent(playerBar);

		}

		@Override
		public void run() {
//			super.run();
			doInBackground();
			publishProgress();
		}

		private void publishProgress() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					onProgressUpdate();
				}
			});
		}

		// @Override
		protected Void doInBackground(Void... params) {
			int counter = 0;
			while (!isCancelled()) {
				try {
					if (counter <= 0) {
						// counter = 19;
						final PlayerService playerBar = playerBarReference
								.get();
						if (playerBar.isPlaying()) {
							URL url;
							try {
								url = new URL(
										playerBar
												.getResources()
												.getString(
														R.string.hungama_server_url_live_radio)
												+ HungamaApplication.encodeURL(
														radioTrack.getTitle(),
														"utf-8"));
								Logger.e("Update", "" + radioTrack);
								if(Logger.enableOkHTTP){
									OkHttpClient client = CommunicationManager.getUnsafeOkHttpClient();
//									Request.Builder requestBuilder = new Request.Builder();
//									requestBuilder.url(url);
									Request.Builder requestBuilder = CommunicationManager.getRequestBuilder(PlayerService.this, url);
									com.squareup.okhttp.Response responseOk = client.newCall(requestBuilder.build()).execute();
									if (responseOk.code() == HttpsURLConnection.HTTP_OK) {
										String response = responseOk.body().string();
										Logger.s("live radio :::: " + response);
										Gson gson = new Gson();
										detail = gson.fromJson(response,
												LiveStationDetails.class);
										counter = 10;
										publishProgress();
									}
								} else {
									HttpURLConnection urlConnection = (HttpURLConnection) url
											.openConnection();
									if (urlConnection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
										InputStream in = new BufferedInputStream(
												urlConnection.getInputStream());
										StringBuilder sb = new StringBuilder();
										int ch = -1;
										while ((ch = in.read()) != -1) {
											sb.append((char) ch);
										}
										Logger.s("live radio :::: " + sb.toString());
										Gson gson = new Gson();
										detail = gson.fromJson(sb.toString(),
												LiveStationDetails.class);
										counter = 10;
										publishProgress();
									}
								}
							} catch (MalformedURLException e) {
								e.printStackTrace();
								detail = null;
                                publishProgress();
								//sendNoConnectionBroadCast();
								Logger.i("Error-response-", "" + e);
							} catch (IOException e) {
								e.printStackTrace();
								detail = null;
                                publishProgress();
								sendNoConnectionBroadCast();
								Logger.i("Error-response-", "" + e);
							} catch (Exception e) {
								e.printStackTrace();
								detail = null;
                                publishProgress();
								//sendNoConnectionBroadCast();
								Logger.i("Error-response-", "" + e);
							}
						} else {
						}
						if (isCancelled()) {
							break;
						}
					}
					counter--;
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Logger.d(TAG, "Cancelling playing progress update.");
					break;
				} catch (Exception e) {
					Logger.printStackTrace(e);
					break;
				}
			}
			return null;
		}

		private boolean isCancelled() {
			if (cancelled) {
				try {
					final PlayerService playerService = playerBarReference
							.get();
					if (playerService != null) {
						if (previousTrackDetails != null) {
							Looper.prepare();
							loggingLiveRadioEvent(playerService,
									previousTrackDetails, false);
						}
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			}
			return cancelled;
		}

		// @Override
		protected void onProgressUpdate(Void... values) {
			try {
				final PlayerService playerService = playerBarReference.get();
				if (playerService != null) {
					PlayerService.State state = playerService.getState();
					Logger.e(TAG + "", "Live Radio onProgressUpdate " + state);
					if (state == PlayerService.State.PLAYING && detail != null) {
						if (playerService.mOnRadioBarUpdateListener != null) {
							playerService.mOnRadioBarUpdateListener
									.OnRadioBarUpdate(detail);
						}
						try {
							if (previousTrackDetails != null) {
								if (previousTrackDetails.getId() != detail
										.getId()) {
									loggingLiveRadioEvent(playerService,
											previousTrackDetails, true);
									startLiveRadioLoggingEvent(playerService);
								}
							}
						} catch (Exception e) {
							Logger.e(TAG + " ", "Live Radio onProgressUpdate" + e);
						}
						previousTrackDetails = detail;
					}else{
                        if (playerService.mOnRadioBarUpdateListener != null) {
                            playerService.mOnRadioBarUpdateListener
                                    .OnRadioBarUpdate(null);
                        }
                    }
				} else {
					cancel(true);
				}
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}

		private void startLiveRadioLoggingEvent(PlayerService playerService) {
			DeviceConfigurations config = DeviceConfigurations
					.getInstance(playerService.mContext);
			playerService.mEventStartTimestampForLiveRadio = config.getTimeStampDelta();
		}

		private void loggingLiveRadioEvent(PlayerService playerService,
				LiveStationDetails detail, boolean completePlay) {
			if (radioTrack != null) {
				Map<String, String> reportMap = new HashMap<String, String>();
				reportMap.put(FlurryConstants.FlurryKeys.Title.toString(),
						radioTrack.getTitle());
				reportMap.put(FlurryConstants.FlurryKeys.Duration.toString(),
						String.valueOf(0));
				reportMap.put(FlurryConstants.FlurryKeys.Type.toString(),
						FlurryConstants.FlurryFullPlayerParams.LiveRadio
								.toString());
				Analytics.logEvent(
						FlurryConstants.FlurryAllPlayer.SongPlayed.toString(),
						reportMap);
				PlayingSourceType playingSourceType;
				/*if(isCastConnected())
					playingSourceType = PlayingSourceType.CAST;
				else*/
					playingSourceType = PlayingSourceType.STREAM;
				int playCurrentPostion = 0;
				int consumerId = playerService.mDataManager
						.getApplicationConfigurations().getConsumerID();
				String deviceId = playerService.mDataManager
						.getApplicationConfigurations().getDeviceID();
				Logger.i(TAG, "track id:" + detail.getId() + " id:"
						+ radioTrack.getId() + " " + playingSourceType);
				PlayEvent playEvent = new PlayEvent(consumerId, deviceId, 0,
						completePlay, 0, playerService.mEventStartTimestampForLiveRadio, 0,
						0, detail.getId(), "track", playingSourceType, 0,
						playCurrentPostion);
				try {
					if (detail.getAlbumId() != 0)
						playEvent.setAlbum_id(detail.getAlbumId());

					playEvent.setLiveRarioDetails("" + radioTrack.getId(),
							radioTrack.getTitle());
				} catch (Exception e) {
					e.printStackTrace();
				}
				playerService.mDataManager.addEvent(playEvent);
				ApsalarEvent.postEvent(getApplicationContext(), ApsalarEvent.RADIO_PLAYED);
				ApsalarEvent.postEvent(getApplicationContext(), ApsalarEvent.MEDIA_PLAYED);
			}
			playerService.mEventStartTimestampForLiveRadio = null;
		}

		public void cancel(boolean b) {
			cancelled = b;
		}

		public void execute() {
//			start();
		}
	}

	private void sendNoConnectionBroadCast()
	{
		try {
			Intent intent = new Intent();
			intent.setAction(LockScreenActivity.NO_CONNECTION);
			sendBroadcast(intent);
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private LiveRadioUpdater mLiveRadioUpdater;
	private Handler handler = new Handler();
	private Track liveRadioTrack;

	private void startLiveRadioUpdater(Track radioTrack) {
		Logger.s("----startLiveRadioUpdater --- ");
		if(isCatchPlaying)
			isCatchPlaying = false;
		stopLiveRadioUpdater();
		liveRadioTrack = radioTrack;
		handler.postDelayed(liveRadioUpdateHandler, 1000);
	}

	private Runnable liveRadioUpdateHandler = new Runnable() {
		public void run() {
			if (mLiveRadioUpdater == null) {
				createNewLiveRadioUpdater();
			} else {
				handler.postDelayed(liveRadioUpdateHandler, 1000);
			}
		}
	};

	private void createNewLiveRadioUpdater() {
		Logger.s("-----createNewLiveRadioUpdater-----");
		if (getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
			mLiveRadioUpdater = new LiveRadioUpdater(this, liveRadioTrack);

			ThreadPoolManager.getInstance().submit(mLiveRadioUpdater);
//			mLiveRadioUpdater.execute();
					// System.out.println("-----createNewLiveRadioUpdater-----31");
					Logger.i(TAG, "EXECUTED - Build VERSION LESS THAN HONEYCOMB");
			// }
		}
	}

	public void stopLiveRadioUpdater() {
		Logger.s("-----stopLiveRadioUpdater-----" + (mLiveRadioUpdater == null));
		try {
			handler.removeCallbacks(liveRadioUpdateHandler);

			if (mLiveRadioUpdater != null)
				mLiveRadioUpdater.cancel(true);
			mLiveRadioUpdater = null;
			// }
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	public interface RadioBarUpdateListener {

		public void OnRadioBarUpdate(LiveStationDetails detail);
	}

	public void setRadioBarUpdateListener(RadioBarUpdateListener listener) {
		mOnRadioBarUpdateListener = listener;
	}

	private boolean isAutoSaveOfflineEnabled = false;

	private void saveAllTracksOffline(List<Track> tracks) {
		if (mPlayMode == PlayMode.MUSIC) {
			if (CacheManager.isProUser(mContext)
					&& !mApplicationConfigurations.getSaveOfflineMode()
					&& mApplicationConfigurations
							.getSaveOfflineAutoSaveModeFirstTime()) {
				if (mApplicationConfigurations.getSaveOfflineAutoSaveFreeUser() == 0) {
					if (tracks != null && tracks.size() > 0) {
						Track track = tracks.get(0);
						MediaItem mediaItem = new MediaItem(track.getId(),
								track.getTitle(), track.getAlbumName(),
								track.getArtistName(), track.getImageUrl(),
								track.getBigImageUrl(), MediaType.TRACK
										.toString().toLowerCase(), 0, 0,
								track.getImages(), track.getAlbumId());
						CacheManager.autoSaveOfflinePlayerQueue(mContext,
								mediaItem, track);
					}
				}
				mApplicationConfigurations
						.setSaveOfflineAutoSaveFirstTime(false);
				if (mPlayerBarFragment != null)
					mPlayerBarFragment.askForAutoSave(tracks);
			} else {
				if (CacheManager.isProUser(mContext)
						&& mApplicationConfigurations
								.getSaveOfflineAutoSaveMode()) {
					isAutoSaveOfflineEnabled = true;
					if (MediaCachingTaskNew.isEnabled) {
						if (mPlayerBarFragment != null)
							CacheManager.saveAllTracksOfflineAction(
									mPlayerBarFragment.getActivity(), tracks);
						else
							CacheManager.saveAllTracksOfflineAction(mContext,
									tracks);
					} else {
						for (Track track : tracks) {
							MediaItem mediaItem = new MediaItem(track.getId(),
									track.getTitle(), track.getAlbumName(),
									track.getArtistName(), track.getImageUrl(),
									track.getBigImageUrl(), MediaType.TRACK
											.toString().toLowerCase(), 0, 0,
									track.getImages(), track.getAlbumId());
							CacheManager.autoSaveOfflinePlayerQueue(mContext,
									mediaItem, track);
						}
					}
				} else {
					isAutoSaveOfflineEnabled = false;

				}
			}
		}
	}

	public void startAutoSavingPlayerQueue() {
		if (!isAutoSaveOfflineEnabled && mPlayMode == PlayMode.MUSIC)
			saveAllTracksOffline(getPlayingQueue());
	}

	public void startAutoSavingTracks(List<Track> tracks) {
		saveAllTracksOffline(tracks);
	}

	public void setPlayerBarFragment(PlayerBarFragment playerBarFragment) {
		mPlayerBarFragment = playerBarFragment;
	}

	private boolean isPausedFromVideo = false;

	/*public void setPausedFromVideo(boolean value)
    {
        if(isAdPlaying && value==true)
        {
            isPausedFromVideo=true;
        }

 		if (!value && isPausedFromVideo)
        {
            isPausedFromVideo=false;
            play();

		}

    }
*/

    public void setPausedFromVideo(boolean value) {
        isPausedFromVideo = value;
        if (!isPausedFromVideo && isAdPlaying) {
            play();
        }
    }


    public boolean getPausedFromVideo() {
		return isPausedFromVideo;
	}

	public Placement getAudioAdPlacement() {
		return placementAudioAd;
	}

	private ActivityMediaControlReceiver activityMediaControlReceiver;
	@SuppressWarnings("deprecation")
	private RemoteControlClient myRemoteControlClient;

	/**
	 * Listing 15-14: Pausing output when the headset is disconnected
	 */
	private class NoisyAudioStreamReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent
					.getAction())) {
				pause();
				sendBroadcast(new Intent(
						PlayerBarFragment.ACTION_PLAY_STATE_CHANGED));
			}
		}
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("InlinedApi")
	private void registerRemoteControlClient() {
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				/**
				 * Listing 15-15: Registering a Remote Control Client
				 */

				Intent mediaButtonIntent = new Intent(
						Intent.ACTION_MEDIA_BUTTON);
				ComponentName component = new ComponentName(this,
						RemoteControlReceiver.class);

				mediaButtonIntent.setComponent(component);
				PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(
						getApplicationContext(), 0, mediaButtonIntent, 0);

				// Create a new Remote Control Client using the
				// Pending Intent and register it with the
				// Audio Manager
				myRemoteControlClient = new RemoteControlClient(
						mediaPendingIntent);

				mAudioManager
						.registerRemoteControlClient(myRemoteControlClient);

				/**
				 * Listing 15-16: Configuring the Remote Control Client playback
				 * controls
				 */
				myRemoteControlClient
						.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
								| RemoteControlClient.FLAG_KEY_MEDIA_STOP
								| RemoteControlClient.FLAG_KEY_MEDIA_NEXT
								| RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	private static final int PLAYSTATE_STOPPED = 1;
	private static final int PLAYSTATE_PAUSED = 2;
	private static final int PLAYSTATE_PLAYING = 3;
	private static final int PLAYSTATE_BUFFERING = 8;

	private long lastTrack = 0;

	@SuppressWarnings("deprecation")
	@SuppressLint("InlinedApi")
	private void updatePlaybackState(int state) {
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				if (trackDetailsInEnglish == null
						|| trackDetailsInEnglish.getId() != mCurrentTrack
								.getId()) {
					loadDataInEnglishOnly(mCurrentTrack, state);
				}

				myRemoteControlClient.setPlaybackState(state);
				setRemoteControlMetadata(mCurrentTrack.getTitle(),
						mCurrentTrack.getAlbumName(), mCurrentTrack.getId(),
						state);
				myRemoteControlClient.setPlaybackState(state);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		} catch (OutOfMemoryError e) {
		}
	}

	@SuppressLint("HandlerLeak")
	Handler changeLockScreenBG = new Handler() {
		@SuppressWarnings("deprecation")
		public void handleMessage(Message msg) {
			Logger.i("MediaTilesAdapter",
					"Play button click: PS.changeLockScreenBG ");

			if (mPlayMode == PlayMode.LIVE_STATION_RADIO) {
				myRemoteControlClient
						.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
								| RemoteControlClient.FLAG_KEY_MEDIA_STOP);
			} else if (mPlayMode == PlayMode.TOP_ARTISTS_RADIO
					|| mPlayMode == PlayMode.DISCOVERY_MUSIC) {
				int flag = RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
						| RemoteControlClient.FLAG_KEY_MEDIA_STOP;
				if(hasNext()){
					flag = flag | RemoteControlClient.FLAG_KEY_MEDIA_NEXT;
				}
				myRemoteControlClient
						.setTransportControlFlags(flag);
//				myRemoteControlClient
//						.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
//								| RemoteControlClient.FLAG_KEY_MEDIA_STOP
//								| RemoteControlClient.FLAG_KEY_MEDIA_NEXT);
			} else {
				int flag = RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
						| RemoteControlClient.FLAG_KEY_MEDIA_STOP;
				if(hasNext()){
					flag = flag | RemoteControlClient.FLAG_KEY_MEDIA_NEXT;
				}
				if(hasPrevious()){
					flag = flag | RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS;
				}
				myRemoteControlClient
						.setTransportControlFlags(flag);

//				myRemoteControlClient
//						.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
//								| RemoteControlClient.FLAG_KEY_MEDIA_STOP
//								| RemoteControlClient.FLAG_KEY_MEDIA_NEXT
//								| RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS);
			}

			try {
//				Logger.s(" :::::::::::::::: Remote :: 41 " + (backgroundImage == null));
				if(backgroundImage != null) {
//					Logger.s(" :::::::::::::::: Remote :: 42 ");
					MetadataEditor editor = myRemoteControlClient
							.editMetadata(false);
					editor.putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK,
//						backgroundImage);
							backgroundImage.copy(backgroundImage.getConfig(), true));
					editor.apply();
//					Logger.s(" :::::::::::::::: Remote :: 43 ");
				}
			} catch (Exception e) {
//				Logger.s(" :::::::::::::::: Remote :: 44 ");
				Logger.printStackTrace(e);
			} catch (java.lang.Error e) {
//				Logger.s(" :::::::::::::::: Remote :: 45 ");
				Logger.printStackTrace(e);
			}
		};
	};

	// private Bitmap backgroundImage;

	@SuppressWarnings("deprecation")
	@SuppressLint("InlinedApi")
	private void setRemoteControlMetadata(String album, String artist,
			long trackNumber, int state) {

		Logger.i("MediaTilesAdapter",
				"Play button click: setRemoteControlMetadata 1 >> state"
						+ state + " track" + trackNumber);

		/**
		 * Listing 15-17: Applying changes to the Remote Control Client metadata
		 */
		MetadataEditor editor = myRemoteControlClient.editMetadata(true);

		if (trackDetailsInEnglish != null
				&& trackDetailsInEnglish.getId() == mCurrentTrack.getId()) {
			editor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE,
					trackDetailsInEnglish.getTitle());
			editor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM,
					trackDetailsInEnglish.getAlbumName());
			editor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST,
					trackDetailsInEnglish.getSingers());
		} else {
			editor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE,
					mCurrentTrack.getTitle());
			editor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM,
					mCurrentTrack.getAlbumName());
			editor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST,
					mCurrentTrack.getArtistName());
		}
		editor.putLong(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER,
				trackNumber);

		if (PLAYSTATE_PLAYING == state && lastTrack != trackNumber) {
			try {
//                BitmapFactory.Options o=new BitmapFactory.Options();
//                o.inSampleSize = 2;
				editor.putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK,
						BitmapFactory.decodeResource(getResources(),
								R.drawable.background_home_tile_album_default));
				editor.apply();
			} catch (java.lang.Error e) {
				System.gc();
			}
			Logger.i("MediaTilesAdapter",
					"Play button click: setRemoteControlMetadata 2");
			lastTrack = trackNumber;
			// final String imageUrl = mCurrentTrack.getBigImageUrl();
			final String imageUrl = ImagesManager
					.getMusicArtBigImageUrl(mCurrentTrack.getImagesUrlArray());
			final String smallImageUrl;
			if (TextUtils.isEmpty(imageUrl)) {
				smallImageUrl = ImagesManager
						.getMusicArtSmallImageUrl(mCurrentTrack
								.getImagesUrlArray());
//				Logger.s(" :::::::::::::::: Remote :: 1 " + smallImageUrl);
				if (getBaseContext() != null
						&& !TextUtils.isEmpty(smallImageUrl)) {
//					Logger.s(" :::::::::::::::: Remote :: 11 " + smallImageUrl);

//					new Thread(new Runnable() {// callback
//								@Override
//								public void run() {
//									Logger.s(" :::::::::::::::: Remote :: 12 ");
//									try {
//										backgroundImage = Utils.getBitmap(
//												getApplicationContext(),
//												smallImageUrl);
//										Logger.s(" :::::::::::::::: Remote :: 13 ");
//										changeLockScreenBG.sendEmptyMessage(0);
//									} catch (Exception e) {
//										Logger.s(" :::::::::::::::: Remote :: 14 ");
//										Logger.printStackTrace(e);
//									}
//								}
//							}).start();
					picasso.load(smallImageUrl, targetRemote, PicassoUtil.PICASSO_TAG);
				}
			} else if (getBaseContext() != null && !TextUtils.isEmpty(imageUrl)) {
//				Logger.s(" :::::::::::::::: Remote :: 2 " + imageUrl);

//				new Thread(new Runnable() {// callback
//							@Override
//							public void run() {
//								Logger.s(" :::::::::::::::: Remote :: 21 " + imageUrl);
//								try {
//									backgroundImage = Utils.getBitmap(
//											getApplicationContext(), imageUrl);
//									Logger.s(" :::::::::::::::: Remote :: 22 " + (backgroundImage == null));
//									changeLockScreenBG.sendEmptyMessage(0);
//								} catch (Exception e) {
//									Logger.s(" :::::::::::::::: Remote :: 23 ");
//									Logger.printStackTrace(e);
//								}
//							}
//						}).start();

//				PicassoUtil.PicassoTarget target = new PicassoUtil.PicassoTarget() {
//					@Override
//					public void onPrepareLoad(Drawable arg0) {
//						Logger.s(" :::::::::::::::: Remote :: 20 ");
//					}
//
//					@Override
//					public void onBitmapLoaded(Bitmap arg0, Picasso.LoadedFrom arg1) {
//						Logger.s(" :::::::::::::::: Remote :: 21 " + imageUrl);
//						try {
//							backgroundImage = arg0;
//							Logger.s(" :::::::::::::::: Remote :: 22 " + (backgroundImage == null));
//							changeLockScreenBG.sendEmptyMessage(0);
//						} catch (Exception e) {
//							Logger.s(" :::::::::::::::: Remote :: 23 ");
//							Logger.printStackTrace(e);
//						}
//						protectedFromGarbageCollectorTargets.remove(this);
//					}
//
//					@Override
//					public void onBitmapFailed(Drawable arg0) {
//						Logger.s(" :::::::::::::::: Remote :: 24 ");
//						protectedFromGarbageCollectorTargets.remove(this);
//					}
//				};
//				protectedFromGarbageCollectorTargets.add(target);
//				picasso.load(imageUrl, target, PicassoUtil.PICASSO_TAG);
				picasso.load(imageUrl, targetRemote, PicassoUtil.PICASSO_REMOTE);
			} else {
//				Logger.s(" :::::::::::::::: Remote :: 3");

				new Thread(new Runnable() {// callback
					@Override
					public void run() {
//						Logger.s(" :::::::::::::::: Remote :: 31 ");
						try {
                            //BitmapFactory.Options o=new BitmapFactory.Options();
                           // o.inSampleSize = 2;
							backgroundImage = BitmapFactory.decodeResource(getResources(), R.drawable.icon_launcher);
//							Logger.s(" :::::::::::::::: Remote :: 32 ");
							changeLockScreenBG.sendEmptyMessage(0);
						} catch (Exception e) {
//							Logger.s(" :::::::::::::::: Remote :: 33 ");
							Logger.printStackTrace(e);
						}
					}
				}).start();
			}
//			editor.apply();
		} else {
			editor.apply();
		}
	}

	private Bitmap backgroundImage;
//	final Set<PicassoUtil.PicassoTarget> protectedFromGarbageCollectorTargets = new HashSet<>();

	PicassoUtil.PicassoTarget targetRemote = new PicassoUtil.PicassoTarget() {
		@Override
		public void onPrepareLoad(Drawable arg0) {
//			Logger.s(" :::::::::::::::: Remote :: 20 ");
		}

		@Override
		public void onBitmapLoaded(Bitmap arg0, Picasso.LoadedFrom arg1) {
//			Logger.s(" :::::::::::::::: Remote :: 21 ");
			try {
				backgroundImage = arg0;
//				Logger.s(" :::::::::::::::: Remote :: 22 " + (backgroundImage == null));
				changeLockScreenBG.sendEmptyMessage(0);
			} catch (Exception e) {
//				Logger.s(" :::::::::::::::: Remote :: 23 ");
				Logger.printStackTrace(e);
			}
		}

		@Override
		public void onBitmapFailed(Drawable arg0) {
//			Logger.s(" :::::::::::::::: Remote :: 24 ");
		}
	};

	/**
	 * Listing 15-9: Media button press Broadcast Receiver implementation
	 */
	private class ActivityMediaControlReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				Logger.v("TestApp", "Button press received ");
				if (RemoteControlReceiver.ACTION_MEDIA_BUTTON.equals(intent
						.getAction())) {
					KeyEvent event = (KeyEvent) intent
							.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
					Logger.v("TestApp",
							"Button press event :: " + event.getKeyCode());
					if (event.getAction() == KeyEvent.ACTION_DOWN)
						switch (event.getKeyCode()) {
						case KeyEvent.KEYCODE_HEADSETHOOK:
							if (currentPlayer.isPlaying()) {
								pause();
							} else {
								play();
							}
							sendBroadcast(new Intent(
									PlayerBarFragment.ACTION_PLAY_STATE_CHANGED));
							break;
						case (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE):
							if (!isAdPlaying) {
								if (currentPlayer.isPlaying())
									pause();
								else
									play();
							}
							break;
						case (KeyEvent.KEYCODE_MEDIA_PLAY):
							play();
							break;
						case (KeyEvent.KEYCODE_MEDIA_PAUSE):
							if (!isAdPlaying)
								pause();
							break;
						case (KeyEvent.KEYCODE_MEDIA_NEXT):
							if (mPlayingQueue.hasNext() && !isAdPlaying)
								next();
							break;
						case (KeyEvent.KEYCODE_MEDIA_PREVIOUS):
							if (mPlayingQueue.hasPrevious() && !isAdPlaying && mPlayMode==PlayMode.MUSIC)
								previous();
							break;
						case (KeyEvent.KEYCODE_MEDIA_STOP):
							if (!isAdPlaying)
								stop();
							break;
						default:
							break;
						}
				}
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}
	}

	private boolean isAudioAdPosition() {
		try {
			if(!Utils.isConnected())
				return false;
			if (!TextUtils.isEmpty(mApplicationConfigurations
					.getAppConfigAudioAdRule())) {
				String[] positions = mApplicationConfigurations
						.getAppConfigAudioAdRule().split(",");
				if (AudioAdCount == 0) {
					String position = positions[0];
					if (adSkipCount == (Integer.parseInt(position))) {
						return true;
					}
				} else if (adSkipCount > (Integer.parseInt(positions[0]) + Integer
						.parseInt(positions[1]))) {
					String position = positions[1];
					if ((adSkipCount - Integer.parseInt(positions[0]))
							% (Integer.parseInt(position) + 1) == 0) {
						return true;
					}
				}
			} else {
				return (adSkipCount % 4 == 1);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
			return (adSkipCount % 4 == 1);
		}
		return false;
	}

	public Track prefetchTrackMediaHandle(Track track)
			throws InterruptedException {

		Logger.d(
				TAG,
				"Prefetching Media handle for track: "
						+ Long.toString(track.getId()) + " has started.");

		String playingPath = DBOHandler.getTrackPathById(mContext,
				"" + track.getId());
		if (playingPath != null && playingPath.length() > 0 /*&& !isCastConnected()*/) {
			File file = new File(playingPath);
			if (file.exists()) {
				return track;
			}
		}

		// String tempFilePath = Utils.getTempFilePath(track);
		// if (tempFilePath != null && tempFilePath.length() > 0) {
		// File file = new File(tempFilePath);
		// if (file.exists()) {
		// return track;
		// }
		// }

		/*
		 * Track's media handle should only been updated if it doesn't hold one,
		 * or it's been obsolete after 30 minutes.
		 */
		Calendar rightNow = Calendar.getInstance();
		boolean timeToRefresh = rightNow.getTimeInMillis()
				- MEDIA_HANDLE_UPDATE_TIME_DELTA_MILLIS >= track
				.getCurrentPrefetchTimestamp();

		// if (Thread.currentThread().isInterrupted()) {
		// throw new InterruptedException();
		// }

		if (TextUtils.isEmpty(track.getMediaHandle()) || timeToRefresh) {
			try {
				// if (Thread.currentThread().isInterrupted()) {
				// throw new InterruptedException();
				// }

				Logger.d(TAG, "Start prefetching Media handle for track: "
						+ Long.toString(track.getId()));

				CommunicationManager communicationManager = new CommunicationManager();
				Map<String, Object> mediaHandleProperties = communicationManager
						.performOperation(new CMDecoratorOperation(
								mCMServerUrl, new MediaHandleOperation(
										mContext, track.getId(), false)),
								mContext);

				/*
				 * Too late, if we've reached so far without interrupting there
				 * is no point to cancel it now. Populates the track with its
				 * playing properties.
				 */
				track.setMediaHandle((String) mediaHandleProperties
						.get(MediaHandleOperation.RESPONSE_KEY_HANDLE));
				track.setDeliveryId((Long) mediaHandleProperties
						.get(MediaHandleOperation.RESPONSE_KEY_DELIVERY_ID));
				// track.setDoNotCache(Boolean.parseBoolean((String)
				// mediaHandleProperties.get(MediaHandleOperation.RESPONSE_KEY_DO_NOT_CACHE)));
				track.setDoNotCache((Boolean) mediaHandleProperties
						.get(MediaHandleOperation.RESPONSE_KEY_DO_NOT_CACHE));
				/*
				 * sets the track's time when it was updated with the media
				 * handle.
				 */
				rightNow = Calendar.getInstance();
				track.setCurrentPrefetchTimestamp(rightNow.getTimeInMillis());

				if (track.details == null) {
					ServerConfigurations mServerConfigurations = mDataManager
							.getServerConfigurations();
					MediaItem mediaItem = new MediaItem(track.getId(),
							track.getTitle(), track.getAlbumName(),
							track.getArtistName(), track.getImageUrl(),
							track.getBigImageUrl(), MediaType.TRACK.toString()
									.toLowerCase(), 0, 0, track.getImages(),
							track.getAlbumId());
					String images = ImagesManager.getImageSize(
							ImagesManager.MUSIC_ART_SMALL,
							mDataManager.getDisplayDensity())
							+ ","
							+ ImagesManager.getImageSize(
									ImagesManager.MUSIC_ART_BIG,
									mDataManager.getDisplayDensity());
					Map<String, Object> mediaDetails = communicationManager
							.performOperation(
									new MediaDetailsOperation(
											mServerConfigurations
													.getHungamaServerUrl_2(),
											mServerConfigurations
													.getHungamaAuthKey(),
											mApplicationConfigurations
													.getPartnerUserId(),
											mediaItem, null, images), mContext);
					MediaTrackDetails details = (MediaTrackDetails) mediaDetails
							.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);
					track.details = details;
				}

			} catch (InvalidRequestException e) {
				e.printStackTrace();
				return null;
			} catch (InvalidResponseDataException e) {
				e.printStackTrace();
				return null;
			} catch (OperationCancelledException e) {
				e.printStackTrace();
				return null;
			} catch (NoConnectivityException e) {
				e.printStackTrace();
				return null;
			}
			Logger.d(
					TAG,
					"stop prefetching Media handle for track: "
							+ Long.toString(track.getId()));
		}

		return track;
	}

	// -----------------------------------MyMediaPlayer-------------------------//
	/*private static class PlayerState {
		private static int STATE_PLAYING = 0;
		private static int STATE_PREPARING = 1;
		private static int STATE_PREPARED = 4;
		private static int STATE_PAUSED = 2;
		private static int STATE_NEW = 5;
	}

	private static class PlayerNotReadyException extends IllegalStateException {

	}

	private static class MyMediaPlayer extends MediaPlayer {

		private String url;
		// long timestmp;
		private int playerState;
		long trackId;
		boolean needToAutoPlayAfterPrepare = false;

		public MyMediaPlayer(long trackId) {
			this.trackId = trackId;
		}

		public void setTrackId(long trackId) {
			this.trackId = trackId;
		}

		@Override
		public void reset() {
			playerState = PlayerState.STATE_NEW;
			url = "";
			trackId = 0;
			super.reset();
		}

		@Override
		public void setDataSource(String path) throws IOException,
				IllegalArgumentException, SecurityException,
				IllegalStateException {
			url = path;
			playerState = PlayerState.STATE_NEW;
			super.setDataSource(path);
		}

		@Override
		public void setDataSource(FileDescriptor path) throws IOException,
				IllegalArgumentException, SecurityException,
				IllegalStateException {
			// url = path;
			playerState = PlayerState.STATE_NEW;
			super.setDataSource(path);
		}

		public void prepareAsync(final OnPreparedListener listner) throws IllegalStateException {
            if (this.playerState != PlayerState.STATE_PREPARING) {
                playerState = PlayerState.STATE_PREPARING;
                Logger.i("prepareAsync", "prepareAsync:1");
                setOnPreparedListener(new OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        Logger.i("prepareAsync", "prepareAsync:2");
                        playerState = PlayerState.STATE_PREPARED;
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
            if (this.playerState != PlayerState.STATE_PREPARING) {
                playerState = PlayerState.STATE_PREPARING;
                super.prepare();
                playerState = PlayerState.STATE_PREPARED;
                // if (needToAutoPlayAfterPrepare) {
                // start();
                // }
            } else {
            }

            // }
        }

		@Override
		public void start() throws IllegalStateException {
			if (service.needToPlayCacheMediaPlayer(this) || service.isAdPlaying
					|| playerState == PlayerState.STATE_PAUSED) {
				//service.stopUnusedPlayer();
				super.start();
				playerState = PlayerState.STATE_PLAYING;
			} else {
				throw new PlayerNotReadyException();
			}
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
			if (playerState == PlayerState.STATE_PLAYING) {
				super.pause();
				playerState = PlayerState.STATE_PAUSED;
			}
		}
	}*/

    private void startInitilization(int position, final boolean isPrecache,
									final MusicTrackLoaderTask musicTrackLoaderTask, Track track) {
        if (currentPlayer != null && currentPlayer.isPlaying()) {
            currentPlayer.stop();
			currentPlayer.reset();
            //currentPlayer.release();
        }

		/*if(getPlayingQueue()==null)
			return;
		if(getPlayingQueue().size()<position)
			return;*/
		/*Track track=null;
		try {
			track = getPlayingQueue().get(position);
		}catch (Exception e){}*/

		if (track == null)
			return;

		String handString = track.getMediaHandle();
		long trackId = track.getId();
		if (TextUtils.isEmpty(handString))
			return;

		if(currentPlayer == null) {
			initializeMediaPlayer();
			//currentPlayer = new MyMediaPlayer(trackId);
		}
		else
			currentPlayer.setTrackId(trackId);

			try {
				try {
					currentPlayer.setDataSource(handString);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					if (!isPrecache && Thread.currentThread().isInterrupted()) {
						sendMessage(musicTrackLoaderTask,
								MediaLoaderHandler.MESSAGE_CANCELLED);

						return;
					}
				} catch (SecurityException e) {
					e.printStackTrace();
					if (!isPrecache && Thread.currentThread().isInterrupted()) {
						sendMessage(musicTrackLoaderTask,
								MediaLoaderHandler.MESSAGE_CANCELLED);
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
					if (!isPrecache && Thread.currentThread().isInterrupted()) {
						sendMessage(musicTrackLoaderTask,
								MediaLoaderHandler.MESSAGE_CANCELLED);
						return;
					}
				}
				currentPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			} catch (IllegalStateException e) {
				e.printStackTrace();
				if (!isPrecache && Thread.currentThread().isInterrupted()) {
					sendMessage(musicTrackLoaderTask,
							MediaLoaderHandler.MESSAGE_CANCELLED);
					return;
				}
			}

            try {
                // if (!needToPrepareMediaPlayer(playerCached))
                // return;

				currentPlayer.prepareAsync(new MusicPlayerListner.MyMusicOnPreparedListener() {
					@Override
					public void onPrepared(Object mp1) {
						try {
							Logger.i("onPrepared", " :::::::::::::::::::::::::::::::: onPrepared: -1");
							Logger.i("onPrepared", "onPrepared: need To Play");
							Logger.i("onPrepared", " :::::::::::::::::::::::::::::::: onPrepared: 0");
							Logger.i("onPrepared", " :::::::::::::::::::::::::::::::: onPrepared: " + musicTrackLoaderTask + " ::: " + musicTrackLoaderTask1);
							if (musicTrackLoaderTask != null)
								sendMessage(musicTrackLoaderTask,
										MediaLoaderHandler.MESSAGE_PREPARED);
							else if (musicTrackLoaderTask1 != null)
								sendMessage(musicTrackLoaderTask1,
										MediaLoaderHandler.MESSAGE_PREPARED);
							//removeMediaListner();
							if (currentPlayer != null) {
								try {
									currentPlayer.start();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							//currentPlayer.setPlayState(PlayerState.STATE_PREPARING);
							return;
						}
					}
				});

				/*currentPlayer.prepareAsync(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
						try {
							Logger.i("onPrepared", " :::::::::::::::::::::::::::::::: onPrepared: -1");
							Logger.i("onPrepared", "onPrepared: need To Play");
							Logger.i("onPrepared", " :::::::::::::::::::::::::::::::: onPrepared: 0");
							Logger.i("onPrepared", " :::::::::::::::::::::::::::::::: onPrepared: " + musicTrackLoaderTask + " ::: " + musicTrackLoaderTask1);
							if (musicTrackLoaderTask != null)
								sendMessage(musicTrackLoaderTask,
										MediaLoaderHandler.MESSAGE_PREPARED);
							else if (musicTrackLoaderTask1 != null)
								sendMessage(musicTrackLoaderTask1,
										MediaLoaderHandler.MESSAGE_PREPARED);
							//removeMediaListner();
							if (currentPlayer != null) {
								try {
									currentPlayer.start();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							//currentPlayer.setPlayState(PlayerState.STATE_PREPARING);
							return;
						}
					}
                });*/

            } catch (Exception e) {
                e.printStackTrace();
                if (!isPrecache && Thread.currentThread().isInterrupted()) {
                    sendMessage(musicTrackLoaderTask,
                            MediaLoaderHandler.MESSAGE_CANCELLED);
                    return;
                }
            }
    }


	private void sendMessage(MusicTrackLoaderTask musicTrackLoaderTask,
			int messageCancelled) {
		Logger.i("onPrepared", " :::::::::::::::::::::::::::::::: onPrepared: SendMessage");
		musicTrackLoaderTask.obtainMessage(messageCancelled);
	}

	private void stopUnusedPlayer() {

		currentPlayer.stop();
		currentPlayer.release();

		/*if (playerVector != null) {
            Logger.i("stopUnusedPlayer1","stopUnusedPlayer: PlayerVector:"+ playerVector.size());
			long currentTrackId = mCurrentTrack.getId();
			for (int i = 0; i < playerVector.size(); i++) {
				MyMediaPlayer player = playerVector.get(i);
				if (player.trackId != currentTrackId
						&& (player.playerState == PlayerState.STATE_PREPARING || player.playerState == PlayerState.STATE_PLAYING)) {
                    Logger.i("stopUnusedPlayer", "stopUnusedPlayer: TrackId" + player.trackId + " :: PlayerState:" + player.playerState);
                    try {
						player.stop();
						player.release();
                        player = null;
                    } catch (Exception e) {
                        Logger.printStackTrace(e);
                    } catch (java.lang.Error e) {
                        Logger.printStackTrace(e);
                    }
				}
			}
		}

        if (removedPlayerVector != null) {
            Logger.i("stopUnusedPlayer1","stopUnusedPlayer1: RemovedPlayerVector:"+ removedPlayerVector.size());
            long currentTrackId = mCurrentTrack.getId();
            for (int i = 0; i < removedPlayerVector.size(); i++) {
                MyMediaPlayer player = removedPlayerVector.get(i);
                if (player!=null && player.trackId != currentTrackId) {
                    Logger.i("stopUnusedPlayer1","stopUnusedPlayer1: TrackId"+ player.trackId+" :: PlayerState:"+player.playerState);
                    try {
						player.stop();
						player.release();
                        player = null;
                    } catch (Exception e) {
                        Logger.printStackTrace(e);
                    } catch (java.lang.Error e) {
                        Logger.printStackTrace(e);
                    }
                }
            }
        }
        if(removedPlayerVector!=null)
           removedPlayerVector.clear();*/
	}

    private void preCacheTracks() {
		if (needToUseCastingPlayer()) {
            /*if (hasNext()) {
                Track track = getPlayingQueue().get(getCurrentPlayingTrackPosition() + 1);
                if (mPlayback != null && !mPlayback.isTrackAvailable(track, false) && track != null && track.getMediaHandle() != null) {
					appendTrackToCastingIfNotAvailable(track);
					//mPlayback.addToQueue(track);
                }
            }
            return;*/
        }
		/*Logger.i("PrecacheTracks", "isPrecacheRunning:" + isPrecacheRunning);
		if (hasNext()) {
			Logger.e("Precatch", "Next");
			startInitilization(getCurrentPlayingTrackPosition() + 1, true, null);
			// reseted.remove(mPlayerNext);

			Logger.e("Precatch", "Next done");
		}
		Logger.i("PrecacheTracks", "isPrecacheRunning:" + isPrecacheRunning);
		if (prefetchThread.isInterrupted()) {
			return;
		}
		if (mPlayMode == PlayMode.MUSIC && hasPrevious()) {
			Logger.e("Precatch", "Prev");
			startInitilization(getCurrentPlayingTrackPosition() - 1, true, null);
			// reseted.remove(mPlayerPrev);
			Logger.e("Precatch", "Prev Done");
		}*/
    }

	private CloseAppReceiver closeAppReceiver;

	private class CloseAppReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			explicitStop();
			// reset the inner boolean for showing home
			// tile hints.
			mApplicationConfigurations.setIsHomeHintShownInThisSession(false);
			mApplicationConfigurations
					.setIsSearchFilterShownInThisSession(false);
			mApplicationConfigurations
					.setIsPlayerQueueHintShownInThisSession(false);
		}
	}

	private MediaTrackDetails trackDetailsInEnglish;

	private void loadDataInEnglishOnly(final Track track, final int state) {
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0
				&& !mApplicationConfigurations.isLanguageSupportedForWidget()) {
			ThreadPoolManager.getInstance().submit(new Runnable() {
				@Override
				public void run() {
//					super.run();
					CommunicationManager communicationManager = new CommunicationManager();
					try {
						MediaItem mediaItem = new MediaItem(track.getId(),
								null, null, null, null, null,
								MediaType.TRACK.toString(), 0,
								track.getAlbumId());
						ServerConfigurations mServerConfigurations = DataManager
								.getInstance(PlayerService.this)
								.getServerConfigurations();
						Response response = communicationManager
								.performOperationNew(
										new MediaDetailsOperation(
												mServerConfigurations
														.getHungamaServerUrl_2(),
												mServerConfigurations
														.getHungamaAuthKey(),
												ApplicationConfigurations
														.getInstance(
																PlayerService.this)
														.getPartnerUserId(),
												mediaItem, null, null,
												"english"), PlayerService.this);
						Gson gson = new GsonBuilder()
								.excludeFieldsWithoutExposeAnnotation()
								.create();
						trackDetailsInEnglish = (MediaTrackDetails) gson
								.fromJson(new JSONObject(response.response)
												.getJSONObject("response").toString(),
										MediaTrackDetails.class);
						if (state != 0)
							updatePlaybackState(state);
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
				}
			});
		}
	}

	public void setCurrentPos(Track currentPlayingTrackTemp) {
		this.mCurrentTrack = currentPlayingTrackTemp;
		int currenPos = mPlayingQueue.getCopy()
				.indexOf(currentPlayingTrackTemp);
		mPlayingQueue.setCurrentPos(currenPos);
	}

	//------AddedByK
	public void clearQueue(){
		if(mPlayingQueue!=null)
			mPlayingQueue.clearQueue();
	}

	public void setDetailsToCurrentPlayer(MediaTrackDetails mMediaTrackDetails) {
		if(getCurrentPlayingTrack()==null || getCurrentPlayingTrack().getId() != mMediaTrackDetails.getId())
			return;

		if (getCurrentPlayingTrack().details == null) {
			getCurrentPlayingTrack().details = mMediaTrackDetails;
		}

		if(!TextUtils.isEmpty(mMediaTrackDetails.getTitle()) && TextUtils.isEmpty(getCurrentPlayingTrack().getTitle())){
			if(mPlayingQueue.getCurrentTrack()!=null)
				mPlayingQueue.getCurrentTrack().setTitle(mMediaTrackDetails.getTitle());
			mCurrentTrack = mPlayingQueue.getCurrentTrack();
		}
	}

	// ----------Status Bar Notification---------------
	private static final String EXTRA_START = "start";
	public static final String EXTRA_COMMAND = "command";
	private static final String EXTRA_STOP = "stop";
	private static final String EXTRA_PREVIOUS = "previous";
	private static final String EXTRA_NEXT = "next";
	private static final String EXTRA_CLOSE = "close";
	public static final int NOTIFICATION_PLAYING_CODE = 5326;

	private void showNotification(){
		//		if(mCurrentTrack!=null) {
//			String songName = mCurrentTrack.getTitle();
//// assign the song name to songName
//			PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
//					new Intent(getApplicationContext(), HomeActivity.class),
//					PendingIntent.FLAG_UPDATE_CURRENT);
//			Notification notification = new Notification();
//			RemoteViews remoteViewNotification;
//			// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
//			// remoteViewNotification = new RemoteViews(this
//			// .getApplicationContext().getPackageName(),
//			// R.layout.player_widget_notification_new);
//			// else
//			remoteViewNotification = new RemoteViews(this.getApplicationContext()
//					.getPackageName(),
//					R.layout.song_notification);
//			remoteViewNotification.setTextViewText(R.id.song_title, songName);
//			notification.contentView = remoteViewNotification;
//			notification.tickerText = songName;
//			notification.icon = R.drawable.ic_notification;
//			notification.flags |= Notification.FLAG_ONGOING_EVENT;
//			notification.contentIntent = pi;
////		notification.setLatestEventInfo(getApplicationContext(), "MusicPlayerSample",
////				"Playing: " + songName, pi);
//			startForeground(10001, notification);
//		}


		Boolean needNotToShowNotification = false;
		RemoteViews remoteViewNotification;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			remoteViewNotification = new RemoteViews(this
					.getApplicationContext().getPackageName(),
					R.layout.player_widget_notification_new);
		else
			remoteViewNotification = new RemoteViews(this
					.getApplicationContext().getPackageName(),
					R.layout.player_widget_notification);

//		if (PlayerService.service != null) {
			Track track = getCurrentPlayingTrack();

			try {
				if (track != null) {
					if (trackDetailsInEnglish == null
							|| trackDetailsInEnglish.getId() != track.getId()) {
						loadDataInEnglishOnly(track, 0);
					}
					if (isAdPlaying()) {
						remoteViewNotification.setTextViewText(
								R.id.player_widget_song_title, getString(R.string.txtAdvertisement));
						remoteViewNotification.setTextViewText(
								R.id.player_widget_song_detail, "");
					} else {
						if (trackDetailsInEnglish != null
								&& trackDetailsInEnglish.getId() == track
								.getId()) {
							remoteViewNotification.setTextViewText(
									R.id.player_widget_song_title, ""
											+ trackDetailsInEnglish.getTitle());
							remoteViewNotification.setTextViewText(
									R.id.player_widget_song_detail,
									"" + trackDetailsInEnglish.getAlbumName());
						} else {
							remoteViewNotification.setTextViewText(
									R.id.player_widget_song_title,
									"" + track.getTitle());
							remoteViewNotification.setTextViewText(
									R.id.player_widget_song_detail,
									"" + track.getAlbumName());
						}
					}

					// final String url = track.getBigImageUrl();
					String url = ImagesManager.getMusicArtBigImageUrl(track
							.getImagesUrlArray());
					if (TextUtils.isEmpty(url))
						url = ImagesManager.getMusicArtSmallImageUrl(track
								.getImagesUrlArray());
					// System.out.println(" ::::::::::::::::::: notification :: "
					// + url);
					// remoteViewNotification.setImageViewUri(R.id.player_widget_image_poster,
					// Uri.parse(url));
					String adImageLink = Utils.getDisplayProfile(HomeActivity.metrics, placementAudioAd);
					if (isAdPlaying()
							&& !TextUtils.isEmpty(adImageLink))
						updateImage(remoteViewNotification, adImageLink);
					else
						updateImage(remoteViewNotification, url);

					Logger.e("AppWidgetManager", " Player State ::: "
							+ getState());
					if (getState() != State.PAUSED
							&& (isPlaying() || isAdPlaying() || isLoading())) {
						// Player is in playing or loading state.
						remoteViewNotification.setViewVisibility(
								R.id.player_widget_button_play, View.GONE);

						if (getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
							Logger.i("Test", "1");
							remoteViewNotification.setImageViewResource(
									R.id.player_widget_button_pause,
									R.drawable.icon_widget_player_stop_white);
						} else {
							Logger.i("Test", "else");
							remoteViewNotification.setImageViewResource(
									R.id.player_widget_button_pause,
									R.drawable.icon_widget_player_pause_white);

						}
						remoteViewNotification.setViewVisibility(
								R.id.player_widget_button_pause, View.VISIBLE);
						Logger.e("AppWidgetManager", "1");

					} else if (getState() == State.STOPPED) {
						// needNotToShowNotification = true;
						Logger.e("AppWidgetManager", "3");
					} else {
						remoteViewNotification.setViewVisibility(
								R.id.player_widget_button_play, View.VISIBLE);
						remoteViewNotification.setViewVisibility(
								R.id.player_widget_button_pause, View.GONE);
						Logger.e("AppWidgetManager", "2");
					}

				} else {
					if (getPlayingQueue() == null
							|| getPlayingQueue().size() == 0)
						needNotToShowNotification = true;
				}
			} catch (Exception e) {
				needNotToShowNotification = true;
				Logger.e("AppWidgetManager", "4 " + e);
			}

			try {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					if (hasNext() && !isAdPlaying() && getPlayMode() != PlayMode.LIVE_STATION_RADIO) {
						remoteViewNotification.setViewVisibility(
								R.id.player_widget_button_next, View.VISIBLE);

						remoteViewNotification.setBoolean(
								R.id.player_widget_button_next, "setEnabled",
								true);
					} else {
						if (getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
							remoteViewNotification.setBoolean(
									R.id.player_widget_button_next,
									"setEnabled", false);
							remoteViewNotification.setViewVisibility(
									R.id.player_widget_button_next,
									View.INVISIBLE);
						} else {
							remoteViewNotification.setBoolean(
									R.id.player_widget_button_next,
									"setEnabled", false);
						}

					}

					if (hasPrevious() && !isAdPlaying()
							&& getPlayMode() != PlayMode.TOP_ARTISTS_RADIO
							&& getPlayMode() != PlayMode.DISCOVERY_MUSIC
							&& getPlayMode() != PlayMode.LIVE_STATION_RADIO) {
						remoteViewNotification.setViewVisibility(
								R.id.player_widget_button_prev, View.VISIBLE);
						remoteViewNotification.setBoolean(
								R.id.player_widget_button_prev, "setEnabled",
								true);

					} else {

						if (getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
								|| getPlayMode() == PlayMode.DISCOVERY_MUSIC
								|| getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
							remoteViewNotification.setBoolean(
									R.id.player_widget_button_prev,
									"setEnabled", false);
							remoteViewNotification.setViewVisibility(
									R.id.player_widget_button_prev,
									View.INVISIBLE);

						} else {
							remoteViewNotification.setViewVisibility(
									R.id.player_widget_button_prev,
									View.VISIBLE);
							remoteViewNotification.setBoolean(
									R.id.player_widget_button_prev,
									"setEnabled", false);
						}
					}
				}
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
//		} else {
//			needNotToShowNotification = true;
//		}

		// Register an onClickListener
		Intent playclickIntent = new Intent(getBaseContext(),
				PlayerUpdateWidgetService.class);
		playclickIntent.putExtra(EXTRA_COMMAND, EXTRA_START);
		PendingIntent pendingIntent = PendingIntent.getService(
				getApplicationContext(), 5555, playclickIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViewNotification.setOnClickPendingIntent(
				R.id.player_widget_button_play, pendingIntent);

		Intent pauseclickIntent = new Intent(getBaseContext(),
				PlayerUpdateWidgetService.class);
		pauseclickIntent.putExtra(EXTRA_COMMAND, EXTRA_STOP);
		pendingIntent = PendingIntent.getService(getApplicationContext(), 5556,
				pauseclickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViewNotification.setOnClickPendingIntent(
				R.id.player_widget_button_pause, pendingIntent);

		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				Intent prevclickIntent = new Intent(getBaseContext(),
						PlayerUpdateWidgetService.class);
				prevclickIntent.putExtra(EXTRA_COMMAND, EXTRA_PREVIOUS);
				PendingIntent pendingIntentPrev = PendingIntent.getService(
						getApplicationContext(), 5557, prevclickIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				remoteViewNotification.setOnClickPendingIntent(
						R.id.player_widget_button_prev, pendingIntentPrev);

				Intent nextclickIntent = new Intent(getBaseContext(),
						PlayerUpdateWidgetService.class);
				nextclickIntent.putExtra(EXTRA_COMMAND, EXTRA_NEXT);
				PendingIntent pendingIntentNext = PendingIntent.getService(
						getApplicationContext(), 5558, nextclickIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				remoteViewNotification.setOnClickPendingIntent(
						R.id.player_widget_button_next, pendingIntentNext);

				Intent closeclickIntent = new Intent(getBaseContext(),
						PlayerUpdateWidgetService.class);
				closeclickIntent.putExtra(EXTRA_COMMAND, EXTRA_CLOSE);
				PendingIntent pendingIntentClose = PendingIntent.getService(
						getApplicationContext(), 5559, closeclickIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				remoteViewNotification.setOnClickPendingIntent(
						R.id.player_widget_button_close, pendingIntentClose);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

		Notification notification;
//		notification = new NotificationCompat.Builder(getBaseContext())
//				.setContent(remoteViewNotification).setAutoCancel(false)
//				.setOngoing(true).build();
		notification = new Notification();

		notification.icon = R.drawable.ic_notification;
		// notification.setSmallIcon(R.drawable.icon_launcher);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			notification = new Notification();
			notification.contentView = remoteViewNotification;
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			notification.icon = R.drawable.ic_notification;
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			notification.contentView = getSmallRemoteView();
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			notification.icon = R.drawable.ic_notification;
			notification.bigContentView = remoteViewNotification;
		} else {
			notification.contentView = remoteViewNotification;//getSmallRemoteView();
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			notification.icon = R.drawable.ic_notification;
		}
		try {
//			NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			if (!needNotToShowNotification) {// && (HomeActivity.Instance!=null || GoOfflineActivity.Instance!=null)) {
//				DataManager mDataManager = DataManager
//						.getInstance(getApplicationContext());

				Intent startHomeIntent = new Intent(this,
						NotificationActivity.class);
				startHomeIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
				startHomeIntent.putExtra("donothing", true);
				// startHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
				PendingIntent startHomePendingIntent = PendingIntent
						.getActivity(this, 0,//NOTIFICATION_PLAYING_CODE,
								startHomeIntent,
								PendingIntent.FLAG_UPDATE_CURRENT);

//				notification.flags = Notification.FLAG_FOREGROUND_SERVICE |
//						Notification.FLAG_NO_CLEAR |
//						Notification.FLAG_ONGOING_EVENT;
				notification.flags |= Notification.FLAG_ONGOING_EVENT;
				notification.contentIntent = startHomePendingIntent;
//				manager.notify(NOTIFICATION_PLAYING_CODE, notification);
				startForeground(NOTIFICATION_PLAYING_CODE, notification);
				Logger.e("Notification Notify", "@@@@@@@@@@@");
			} else {
//				manager.cancel(NOTIFICATION_PLAYING_CODE);
				stopNotification();
				Logger.e("Notification cancel", "@@@@@@@@@@@");
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		} catch (OutOfMemoryError e) {
			Logger.printStackTrace(e);
		}
	}

	private RemoteViews getSmallRemoteView() {
		RemoteViews remoteViewNotification;
		remoteViewNotification = new RemoteViews(this.getApplicationContext()
				.getPackageName(),
				R.layout.player_widget_notification_new_small);

//		if (PlayerService.service != null) {
			Track track=null;
			try{
				track = getCurrentPlayingTrack();
			}catch (Exception e){}


			try {
				if (track != null) {
					if (trackDetailsInEnglish == null
							|| trackDetailsInEnglish.getId() != track.getId()) {
						loadDataInEnglishOnly(track, 0);
					}
					if (isAdPlaying()) {
						remoteViewNotification.setTextViewText(
								R.id.player_widget_song_title, getString(R.string.txtAdvertisement));
						remoteViewNotification.setTextViewText(
								R.id.player_widget_song_detail, "");
					} else {
						if (trackDetailsInEnglish != null
								&& trackDetailsInEnglish.getId() == track
								.getId()) {
							remoteViewNotification.setTextViewText(
									R.id.player_widget_song_title, ""
											+ trackDetailsInEnglish.getTitle());
							remoteViewNotification.setTextViewText(
									R.id.player_widget_song_detail,
									"" + trackDetailsInEnglish.getAlbumName());
						} else {
							remoteViewNotification.setTextViewText(
									R.id.player_widget_song_title,
									"" + track.getTitle());
							remoteViewNotification.setTextViewText(
									R.id.player_widget_song_detail,
									"" + track.getAlbumName());
						}
					}

					// final String url = track.getBigImageUrl();
					String url = ImagesManager.getMusicArtBigImageUrl(track
							.getImagesUrlArray());
					if (TextUtils.isEmpty(url))
						url = ImagesManager.getMusicArtSmallImageUrl(track
								.getImagesUrlArray());
					// System.out.println(" ::::::::::::::::::: notification :: "
					// + url);
					// remoteViewNotification.setImageViewUri(R.id.player_widget_image_poster,
					// Uri.parse(url));
					String adImageLink = Utils.getDisplayProfile(HomeActivity.metrics, placementAudioAd);
					if (isAdPlaying()
							&& !TextUtils.isEmpty(adImageLink))
						updateSmallImage(remoteViewNotification, adImageLink);
					else
						updateSmallImage(remoteViewNotification, url);

					Logger.e("AppWidgetManager", "Player State ::: "
							+ getState());
					if (getState() != State.PAUSED && (isPlaying()
							|| isAdPlaying() || isLoading())) {
						// Player is in playing or loading state.
						remoteViewNotification.setViewVisibility(
								R.id.player_widget_button_play, View.GONE);

						if (getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
							remoteViewNotification.setImageViewResource(
									R.id.player_widget_button_pause,
									R.drawable.icon_widget_player_stop_white);
						} else {
							remoteViewNotification.setImageViewResource(
									R.id.player_widget_button_pause,
									R.drawable.icon_widget_player_pause_white);

						}
						remoteViewNotification.setViewVisibility(
								R.id.player_widget_button_pause, View.VISIBLE);
						Logger.e("AppWidgetManager", "1");

					} else if (getState() == State.STOPPED) {
						Logger.e("AppWidgetManager", "3");
					} else {
						remoteViewNotification.setViewVisibility(
								R.id.player_widget_button_play, View.VISIBLE);
						remoteViewNotification.setViewVisibility(
								R.id.player_widget_button_pause, View.GONE);
						Logger.e("AppWidgetManager", "2");
					}
				} else {
				}
			} catch (Exception e) {
				Logger.e("AppWidgetManager", "4 " + e);
			}

			try {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					if (hasNext() && !isAdPlaying() && getPlayMode() != PlayMode.LIVE_STATION_RADIO) {
						remoteViewNotification.setViewVisibility(
								R.id.player_widget_button_next, View.VISIBLE);

						remoteViewNotification.setBoolean(
								R.id.player_widget_button_next, "setEnabled",
								true);
					} else {
						if (getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
							remoteViewNotification.setBoolean(
									R.id.player_widget_button_next,
									"setEnabled", false);
							remoteViewNotification.setViewVisibility(
									R.id.player_widget_button_next, View.GONE);
						} else {
							remoteViewNotification.setBoolean(
									R.id.player_widget_button_next,
									"setEnabled", false);
						}

					}
				}
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
//		} else {
//		}

		// Register an onClickListener
		Intent playclickIntent = new Intent(getBaseContext(),
				PlayerUpdateWidgetService.class);
		playclickIntent.putExtra(EXTRA_COMMAND, EXTRA_START);
		PendingIntent pendingIntent = PendingIntent.getService(
				getApplicationContext(), 5555, playclickIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViewNotification.setOnClickPendingIntent(
				R.id.player_widget_button_play, pendingIntent);

		Intent pauseclickIntent = new Intent(getBaseContext(),
				PlayerUpdateWidgetService.class);
		pauseclickIntent.putExtra(EXTRA_COMMAND, EXTRA_STOP);
		pendingIntent = PendingIntent.getService(getApplicationContext(), 5556,
				pauseclickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViewNotification.setOnClickPendingIntent(
				R.id.player_widget_button_pause, pendingIntent);

		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				Intent prevclickIntent = new Intent(getBaseContext(),
						PlayerUpdateWidgetService.class);
				prevclickIntent.putExtra(EXTRA_COMMAND, EXTRA_PREVIOUS);
				PendingIntent pendingIntentPrev = PendingIntent.getService(
						getApplicationContext(), 5557, prevclickIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				remoteViewNotification.setOnClickPendingIntent(
						R.id.player_widget_button_prev, pendingIntentPrev);

				Intent nextclickIntent = new Intent(getBaseContext(),
						PlayerUpdateWidgetService.class);
				nextclickIntent.putExtra(EXTRA_COMMAND, EXTRA_NEXT);
				PendingIntent pendingIntentNext = PendingIntent.getService(
						getApplicationContext(), 5558, nextclickIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				remoteViewNotification.setOnClickPendingIntent(
						R.id.player_widget_button_next, pendingIntentNext);

				Intent closeclickIntent = new Intent(getBaseContext(),
						PlayerUpdateWidgetService.class);
				closeclickIntent.putExtra(EXTRA_COMMAND, EXTRA_CLOSE);
				PendingIntent pendingIntentClose = PendingIntent.getService(
						getApplicationContext(), 5559, closeclickIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				remoteViewNotification.setOnClickPendingIntent(
						R.id.player_widget_button_close, pendingIntentClose);

				Intent startHomeIntent = new Intent(this, HomeActivity.class);
				startHomeIntent.putExtra("donothing", true);
				startHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
				PendingIntent startHomePendingIntent = PendingIntent
						.getActivity(this, NOTIFICATION_PLAYING_CODE,
								startHomeIntent,
								PendingIntent.FLAG_UPDATE_CURRENT);
				remoteViewNotification
						.setOnClickPendingIntent(
								R.id.player_widget_image_poster,
								startHomePendingIntent);
				remoteViewNotification.setOnClickPendingIntent(
						R.id.player_widget_song_title, startHomePendingIntent);
				remoteViewNotification.setOnClickPendingIntent(
						R.id.player_widget_song_detail, startHomePendingIntent);

			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return remoteViewNotification;
	}

	private String backgroundLink;
	private Bitmap backgroundImageNotification;

	private void updateImage(final RemoteViews remoteViews, final String url) {
		try {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
				return;

			// System.out.println(" ::::::::::::::::::: " + url);
			if(!TextUtils.isEmpty(url) && url.equals("default")){
//				System.out.println("updateImage :::::::::: ");
				remoteViews.setImageViewResource(
						R.id.player_widget_image_poster,
						// R.drawable.icon_launcher);
						R.drawable.background_home_tile_album_default);
			} else if (getBaseContext() != null && !TextUtils.isEmpty(url)) {
//				System.out.println("updateImage :::::::::: 1");
				if (/* backgroundImageNotification == null || */backgroundLink == null
						|| !url.equals(backgroundLink)) {
					// System.out.println(" ::::::::::::::::::: 1 ");
//					System.out.println("updateImage :::::::::: 2");
					backgroundImageNotification = null;
					backgroundLink = url;
					remoteViews.setImageViewResource(
							R.id.player_widget_image_poster,
							// R.drawable.icon_launcher);
							R.drawable.background_home_tile_album_default);
//					new Thread(new Runnable() {// callback
//						@Override
//						public void run() {
//							// if (backgroundImageNotification == null) {
//							// System.out.println(" ::::::::::::::::::: 2 ");
//							backgroundImageNotification = Utils.getBitmap(
//									getApplicationContext(), url);
//							remoteViews.setImageViewBitmap(
//									R.id.player_widget_image_poster, backgroundImageNotification);
//							// System.out.println(" ::::::::::::::::::: 3 ");
////							update();
//							// }
//							// h.sendEmptyMessage(0);
//						}
//					}).start();

					if(targetWidget == null)
						targetWidget = new TargetWidget();
					targetWidget.setRemoteViews(remoteViews);
					picasso.load(url, targetWidget, PicassoUtil.PICASSO_WIDGET);

				} else
//					System.out.println("updateImage :::::::::: 3");
					// h = new Handler() {
					// public void handleMessage(android.os.Message msg) {
					if (backgroundImageNotification != null && url.equals(backgroundLink)) {
//					System.out.println("updateImage :::::::::: 4");
						// System.out.println(" ::::::::::::::::::: 4 " +
						// backgroundImageNotification.getHeight());
						remoteViews.setImageViewBitmap(
								R.id.player_widget_image_poster, backgroundImageNotification);
						// System.out.println(" ::::::::::::::::::: 5 " +
						// backgroundImageNotification.getWidth());
					}
				// }
				// };
			} else {
//				System.out.println("updateImage :::::::::: 5");
				remoteViews.setImageViewResource(
						R.id.player_widget_image_poster,
						R.drawable.icon_launcher);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	private TargetWidget targetWidget;

	private class TargetWidget implements PicassoUtil.PicassoTarget {

		private RemoteViews remoteViews;

		public void setRemoteViews(RemoteViews remoteViews) {
			this.remoteViews = remoteViews;
		}

		@Override
		public void onPrepareLoad(Drawable arg0) {
//			Logger.s(" :::::::::::::::: Remote :: 60 ");
		}

		@Override
		public void onBitmapLoaded(Bitmap arg0, Picasso.LoadedFrom arg1) {
//			Logger.s(" :::::::::::::::: Remote :: 61 ");
			try {
				backgroundImageNotification = arg0;
				remoteViews.setImageViewBitmap(
						R.id.player_widget_image_poster, arg0);
//				Logger.s(" :::::::::::::::: Remote :: 62 ");
				showNotification();
			} catch (Exception e) {
//				Logger.s(" :::::::::::::::: Remote :: 63 ");
				Logger.printStackTrace(e);
			}
		}

		@Override
		public void onBitmapFailed(Drawable arg0) {
//			Logger.s(" :::::::::::::::: Remote :: 64 ");
		}
	};

	private String backgroundLinkSmall;
	private Bitmap backgroundImageNotificationSmall;

	private void updateSmallImage(final RemoteViews remoteViews, final String url) {
		try {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
				return;

			// System.out.println(" ::::::::::::::::::: " + url);
			if(!TextUtils.isEmpty(url) && url.equals("default")){
//				System.out.println("updateImage :::::::::: ");
				remoteViews.setImageViewResource(
						R.id.player_widget_image_poster,
						// R.drawable.icon_launcher);
						R.drawable.background_home_tile_album_default);
			} else if (getBaseContext() != null && !TextUtils.isEmpty(url)) {
//				System.out.println("updateImage :::::::::: 1");
				if (/* backgroundImageNotification == null || */backgroundLinkSmall == null
						|| !url.equals(backgroundLinkSmall)) {
					// System.out.println(" ::::::::::::::::::: 1 ");
//					System.out.println("updateImage :::::::::: 2");
					backgroundImageNotificationSmall = null;
					backgroundLinkSmall = url;
					remoteViews.setImageViewResource(
							R.id.player_widget_image_poster,
							// R.drawable.icon_launcher);
							R.drawable.background_home_tile_album_default);
//					new Thread(new Runnable() {// callback
//						@Override
//						public void run() {
//							// if (backgroundImageNotification == null) {
//							// System.out.println(" ::::::::::::::::::: 2 ");
//							backgroundImageNotificationSmall = Utils.getBitmap(
//									getApplicationContext(), url);
//							remoteViews.setImageViewBitmap(
//									R.id.player_widget_image_poster, backgroundImageNotificationSmall);
//							// System.out.println(" ::::::::::::::::::: 3 ");
////							update();
//							// }
//							// h.sendEmptyMessage(0);
//						}
//					}).start();

					if(targetWidgetSmall == null)
						targetWidgetSmall = new TargetWidgetSmall();
					targetWidgetSmall.setRemoteViews(remoteViews);
					picasso.load(url, targetWidgetSmall, PicassoUtil.PICASSO_WIDGET_SMALL);
				} else
//					System.out.println("updateImage :::::::::: 3");
					// h = new Handler() {
					// public void handleMessage(android.os.Message msg) {
					if (backgroundImageNotificationSmall != null && url.equals(backgroundLinkSmall)) {
//					System.out.println("updateImage :::::::::: 4");
						// System.out.println(" ::::::::::::::::::: 4 " +
						// backgroundImageNotification.getHeight());
						remoteViews.setImageViewBitmap(
								R.id.player_widget_image_poster, backgroundImageNotificationSmall);
						// System.out.println(" ::::::::::::::::::: 5 " +
						// backgroundImageNotification.getWidth());
					}
				// }
				// };
			} else {
//				System.out.println("updateImage :::::::::: 5");
				remoteViews.setImageViewResource(
						R.id.player_widget_image_poster,
						R.drawable.icon_launcher);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	private TargetWidgetSmall targetWidgetSmall;

	private class TargetWidgetSmall implements PicassoUtil.PicassoTarget {

		private RemoteViews remoteViews;

		public void setRemoteViews(RemoteViews remoteViews) {
			this.remoteViews = remoteViews;
		}

		@Override
		public void onPrepareLoad(Drawable arg0) {
//			Logger.s(" :::::::::::::::: Remote :: 70 ");
		}

		@Override
		public void onBitmapLoaded(Bitmap arg0, Picasso.LoadedFrom arg1) {
//			Logger.s(" :::::::::::::::: Remote :: 71 ");
			try {
				backgroundImageNotificationSmall = arg0;
				remoteViews.setImageViewBitmap(
						R.id.player_widget_image_poster, arg0);
				showNotification();
//				Logger.s(" :::::::::::::::: Remote :: 72 ");
			} catch (Exception e) {
//				Logger.s(" :::::::::::::::: Remote :: 73 ");
				Logger.printStackTrace(e);
			}
		}

		@Override
		public void onBitmapFailed(Drawable arg0) {
//			Logger.s(" :::::::::::::::: Remote :: 74 ");
		}
	};

	private void stopNotification(){
		Logger.s(":::::::::::::::::stopNotification::::::::::::::::::::::");
		stopForeground(true);
	}


	//-------------ChromeCast Code---------//
    /*private VideoCastManager mCastManager;
    private MediaRouter mMediaRouter;
    private CastPlayback mPlayback;

    public void initializeChromeCast() {
        mCastManager = VideoCastManager.getInstance();
        mCastManager.addVideoCastConsumer(mCastConsumer);
        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
    }

	boolean isAppConnectFirstTime = false;
    private final VideoCastConsumerImpl mCastConsumer = new VideoCastConsumerImpl() {

        @Override
        public void onApplicationConnected(ApplicationMetadata appMetadata, String sessionId,
                                           boolean wasLaunched) {
            // In case we are casting, send the device name as an extra on MediaSession metadata.
			*//*mSessionExtras.putString(EXTRA_CONNECTED_CAST, mCastManager.getDeviceName());
			mSession.setExtras(mSessionExtras);*//*
            // Now we can switch to CastPlayback

			if(mCurrentState == State.PLAYING || mCurrentState == State.PAUSED) {
				isAppConnectFirstTime = true;
				stopLoggingEvent(false);
				isAppConnectFirstTime = false;
			}
			if(isCatchPlaying){
				getMusicMediaHandle(true, mCurrentTrack);
			} else {
				PlayCasting();
			}
        }

        @Override
        public void onDisconnected() {
            Logger.d(TAG, "onDisconnected");
			StopCastPlaying();
			if(mCurrentState!= State.STOPPED) {
				stop();
				sendBroadcast(new Intent(
						PlayerBarFragment.ACTION_PLAY_STATE_CHANGED));
				//stopLoggingEvent(false);
			}
            //service.play();
			*//*mSessionExtras.remove(EXTRA_CONNECTED_CAST);
			mSession.setExtras(mSessionExtras);*//*
            //Playback playback = new LocalPlayback(MusicService.this, mMusicProvider);
            //mMediaRouter.setMediaSession(null);
            //switchToPlayer(playback, false);
        }
		@Override
		public void onMediaQueueOperationResult(int operationId, int statusCode) {
			if(Logger.isDebuggable)
				displayQueueList();
			super.onMediaQueueOperationResult(operationId, statusCode);
		}
    };

	private void displayQueueList(){
		MediaQueue queue = mCastManager.getMediaQueue();
		if (!queue.isEmpty()) {
			List<MediaQueueItem> list = queue.getQueueItems();
			Logger.i("", "list size update:" + list);
			for (int i = 0; i < list.size(); i++) {
				MediaQueueItem detail = list.get(i);
				MediaInfo mediaInfo = detail.getMedia();
				if (mediaInfo != null) {
					JSONObject customData = mediaInfo.getCustomData();
					Logger.i("Custom Data","CustomData:"+customData.toString());
				}
			}
		}
	}

	private void switchToPlayer(CastPlayback playback1, boolean resumePlaying,boolean isMusic) {
        if (mPlayback == null) {
            throw new IllegalArgumentException("Playback cannot be null");
        }
        int pos = 0;

		String currentMediaId = "";
		if(isAdPlaying()){
            currentMediaId = "-1";
        }
        else if(mPlayMode==PlayMode.LIVE_STATION_RADIO){
			currentMediaId = liveRadioTrack.getId()+"";
		}else{
			currentMediaId = getCurrentPlayingTrack().getId() + "";
		}
        Logger.d(TAG, " Cast:::::::::::::::::::::::::::Current position from " + mPlayback + " is " + pos + " :: mCurrentState:"+mCurrentState);
        *//*if (mPlayback != null)
            mPlayback.stop(false);*//*
		mPlayback.setCallback(this);
		mPlayback.setCurrentStreamPosition(pos < 0 ? 0 : pos);
		mPlayback.setCurrentMediaId(currentMediaId);
        //playback.start();
        //mPlayback = playback;
        // finally swap the instance
        if ((isPlaying() || mCurrentState == State.INTIALIZED) || isAdPlaying()) {
            PlayCastSong();
        }
    }

    @Override
    public void onCompletion() {
		Logger.i("Current Pos", "Cast Cusrrent POs::::::::::::::::" + getCurrentPlayerPosition());
		onCompletion(null);
	}

	int totalDurationForCast = 0;
	int playDurationForCast = 0;

    @Override
    public void onPlaybackStatusChanged(int status) {
        switch (status) {
            case MediaStatus.PLAYER_STATE_IDLE:
                //Utils.makeText(getApplicationContext(), "Idle", Toast.LENGTH_SHORT).show();
                break;
            case MediaStatus.PLAYER_STATE_BUFFERING:

                break;
            case MediaStatus.PLAYER_STATE_PLAYING:
                //Utils.makeText(getApplicationContext(), "Pause", Toast.LENGTH_SHORT).show();
				if (isRealUserCasting()) {
					if (mCurrentState != State.PAUSED) {
						mCurrentState = State.PAUSED;
						//pause();
						sendBroadcast(new Intent(
								PlayerBarFragment.ACTION_PLAY_STATE_CHANGED));
						updatewidget();
					}
				}
                break;
            case MediaStatus.PLAYER_STATE_PAUSED:
                //Utils.makeText(getApplicationContext(), "Playing", Toast.LENGTH_SHORT).show();
				if (isRealUserCasting()) {
					if (mCurrentState != State.PLAYING) {
						mCurrentState = State.PLAYING;
						if(mPlayMode ==PlayMode.LIVE_STATION_RADIO){
							if(radioTrackLoaderTask1!=null)
								radioTrackLoaderTask1.obtainMessage(MediaLoaderHandler.MESSAGE_PREPARED);
						}else {
							if (musicTrackLoaderTask1 != null)
								musicTrackLoaderTask1.obtainMessage(MediaLoaderHandler.MESSAGE_PREPARED);
							if(mPlayMode == PlayMode.MUSIC && mEventStartTimestamp==null){
								//startLoggingEvent();
							}
						}
						//play();
						sendBroadcast(new Intent(
								PlayerBarFragment.ACTION_PLAY_STATE_CHANGED));
						updatewidget();
					}

					Logger.i("isRealUserCasting", "isRealUserCasting::::::: True");
					if (mPlayMode == PlayMode.MUSIC
							&& mLoopMode == LoopMode.REAPLAY_SONG && needToAddNextSongFirstTime) {
						Track track = getCurrentPlayingTrack();
						needToAddNextSongFirstTime = false;
						if(track.getMediaHandle()!=null) {
							if (mPlayback != null && track != null && track.getMediaHandle() != null) {
								appendTrackToCastingIfNotAvailable(track);
								//mPlayback.addToQueue(track);
							}
							//appendTrackToCasting(track);
						}
						*//*else
							getMusicMediaHandle(false, track);*//*
					}else if (hasNext() && needToAddNextSongFirstTime && needToUseCastingPlayer() && (mPlayMode != PlayMode.LIVE_STATION_RADIO)) {
						Track track = getPlayingQueue().get(getCurrentPlayingTrackPosition() + 1);
						needToAddNextSongFirstTime = false;
						if(track.getMediaHandle()!=null)
							appendTrackToCasting(track);
						else
							getMusicMediaHandle(false, track);
					}
				} else {
					Logger.i("isRealUserCasting", "isRealUserCasting::::::: False");
					*//*if (mPlayback != null)
						mPlayback.stop(true);*//*
					//stopCasting();
					stop();
					sendBroadcast(new Intent(
							PlayerBarFragment.ACTION_PLAY_STATE_CHANGED));
				}
				break;
            default: // case unknown
                Logger.d(TAG, "State default : " + status);
                break;
        }
    }

    @Override
    public void onError(String error) {

    }

    @Override
    public void onMetadataChanged(String mediaId) {

    }

    private void addListToQueueCast(List<Track> tracks) {
        *//*if(mPlayback!=null && needToUseCastingPlayer()){
            mPlayback.addListToQueue(tracks);
        }*//*
    }

    private void PlayCastSong() {
		// Ad code
		if(isAdPlaying()){
			if (mPlayback != null) {
				mPlayback.play(adTrack,false);
				if (currentPlayer != null && currentPlayer.isPlaying())
					currentPlayer.pause();
			} else {
				PlayCasting();
			}
			return;
		}
		Logger.d(TAG, " Cast::::::::::::::::::::::::::: PlayCastSong0:");
        if ((mPlayMode == PlayMode.MUSIC|| mPlayMode == PlayMode.TOP_ARTISTS_RADIO || mPlayMode == PlayMode.DISCOVERY_MUSIC) && (mCastManager.getMediaStatus() == null || (mCastManager.getMediaStatus() != null *//*&& (mCastManager.getMediaStatus().getPlayerState() != MediaStatus.PLAYER_STATE_PAUSED || mCastManager.getRemoteMediaUrl().equals(mCurrentTrack.getme))*//*))) {
            if (mPlayback != null) {
                //mPlayback.start();
				needToAddNextSongFirstTime = true;
                //mPlayback.queueNext();
				Logger.d(TAG, " Cast::::::::::::::::::::::::::: PlayCastSong:"+ getCurrentPlayingTrackPosition());
                if (mPlayback.isTrackAvailable(getCurrentPlayingTrack(), true)) {
                    try {
                        *//*mPlayback.queueJumpToItem(getCurrentPlayingTrack());
                        mPlayback.PlayFromPos(getCurrentPlayingTrack());*//*
                        mPlayback.play(getCurrentPlayingTrack(), (mPlayMode == PlayMode.LIVE_STATION_RADIO));
                        //mPlayback.play(getCurrentPlayingTrack());
                    } catch (Exception e) {
                        mPlayback.play(getCurrentPlayingTrack(),(mPlayMode == PlayMode.LIVE_STATION_RADIO));
                    }
                } else {
                    mPlayback.play(getCurrentPlayingTrack(),(mPlayMode == PlayMode.LIVE_STATION_RADIO));
                }
                //mPlayback.play(getCurrentPlayingTrack());
                if (currentPlayer != null && currentPlayer.isPlaying())
                    currentPlayer.pause();
            } else {
                PlayCasting();
            }
        }else if(mPlayMode == PlayMode.LIVE_STATION_RADIO){
			if (mPlayback != null) {
				mPlayback.start();
				mPlayback.play(liveRadioTrack, (mPlayMode == PlayMode.LIVE_STATION_RADIO));
				if (currentPlayer != null && currentPlayer.isPlaying())
					currentPlayer.pause();
			} else {
				PlayCasting();
			}
		}
    }

    private void ResumeCastSong() {
        if (isCastRequire()) {
            mPlayback.play(getCurrentPlayingTrack(), (mPlayMode == PlayMode.LIVE_STATION_RADIO) ? true : false);
        }
    }

    boolean needToAddNextSongFirstTime = false;
    private void PlayCasting() {
		if(mPlayback==null){
			mPlayback = new CastPlayback();
			mPlayback.start();
		}
		if(isAdPlaying()){

			switchToPlayer(mPlayback, true, true);
			needToAddNextSongFirstTime = false;
			return;
		}
		mCurrentState = State.INTIALIZED;
		Logger.d(TAG, " Cast::::::::::::::::::::::::::: PlayCasting1:"+getCurrentPlayingTrack()+ " ::: " + mPlayMode+ " ::: "+ mCurrentState);
        Logger.i("PlayCasting", " :::::::::::::::::::::::::::::::::::::::::::::::::::::::: PlayCasting");
        if (getCurrentPlayingTrack() != null && (mPlayMode != PlayMode.LIVE_STATION_RADIO) && (mCurrentState == State.INTIALIZED || mCurrentState == State.PLAYING)) {
            //CastPlayback playback = new CastPlayback();
            switchToPlayer(mPlayback, true, true);
            needToAddNextSongFirstTime = true;
			Logger.d(TAG, " Cast::::::::::::::::::::::::::: PlayCasting2:");
        }else if(mPlayMode == PlayMode.LIVE_STATION_RADIO){
			//CastPlayback playback = new CastPlayback();
			switchToPlayer(mPlayback, true, false);
			needToAddNextSongFirstTime = false;
			Logger.d(TAG, " Cast::::::::::::::::::::::::::: PlayCasting3:");
		}
    }

    private void PauseCasting() {
        if (isCastRequire() && isCastPlaying() && isCastRemoteLoaded() && (mCastManager.getMediaStatus().getPlayerState() == MediaStatus.PLAYER_STATE_PLAYING)) {
            Logger.d(TAG, "handlePauseRequest: mState=" + mPlayback.getState());
            mPlayback.pause();
        }
    }

    *//**
     * Handle a request to stop music
     *//*
    public void StopCastPlaying() {
		Logger.i("PlayCasting", " :::::::::::::::::::::::::::::::::::::::::::::::::::::::: StopCasting");

		if (mPlayback != null && mIsExplicitMarkedExit && isCastRequire()) {
			Logger.d(TAG, "handleStopRequest: mState=" + mPlayback.getState());
			mPlayback.stop(true);
			mPlayback = null;
		} else if ((mPlayback != null && isPlaying() && isCastRequire() && mLoopMode == LoopMode.OFF)) {
			Logger.d(TAG, "handleStopRequest: mState=" + mPlayback.getState());
			mPlayback.stop(true);
			mPlayback = null;
		}
	}

	public void SeekToCasting(int position) {
        if (isCastConnected() && isCastRemoteLoaded()) {
            mPlayback.seekTo(position);
        }
    }


    private boolean isCastRequire() {
        try {
            return (*//*(mPlayMode == PlayMode.MUSIC || mPlayMode == PlayMode.TOP_ARTISTS_RADIO || mPlayMode == PlayMode.DISCOVERY_MUSIC) && *//*mPlayback != null && needToUseCastingPlayer());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isCastConnected() {
        try {
            return mCastManager.isConnected();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isCastPlaying() {
        try {
            return mCastManager.isRemoteMediaPlaying();
        } catch (TransientNetworkDisconnectionException e) {
            e.printStackTrace();
        } catch (NoConnectionException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isCastPaused() {
        try {
            return mCastManager.isRemoteMediaPaused();
        } catch (TransientNetworkDisconnectionException e) {
            e.printStackTrace();
        } catch (NoConnectionException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isCastRemoteLoaded() {
        try {
            return mCastManager.isRemoteMediaLoaded();
        } catch (TransientNetworkDisconnectionException e) {
            e.printStackTrace();
        } catch (NoConnectionException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isCastRemotePlaying() {
        try {
            return mCastManager.isRemoteMediaPlaying();
        } catch (TransientNetworkDisconnectionException e) {
            e.printStackTrace();
        } catch (NoConnectionException e) {
            e.printStackTrace();
        }
        return false;
    }



	private boolean isRealUserCasting() {
		boolean isDevicePlaying = true;
		try {
			MediaQueue queue = mCastManager.getMediaQueue();
			if (!queue.isEmpty()) {
				MediaQueueItem detail = queue.getCurrentItem();
				MediaInfo mediaInfo = detail.getMedia();
				if (mediaInfo != null) {
					isDevicePlaying =(Utils.getAndroidId(getApplicationContext()).equals(Utils.getDeviceId(mediaInfo)));
				}
			}
		} catch (Exception e) {
		}
		Logger.i("isRealUser", "isRealUser:"+isDevicePlaying);
		return isDevicePlaying;
	}


	public void updateCastingQueue() {
		if(mCastManager!=null && (isCastRemoteLoaded() || isCastRemotePlaying())){
			try {
				MediaQueue queue = mCastManager.getMediaQueue();
				if (!queue.isEmpty()) {
					int currentPos = queue.getCurrentItemPosition();
					List<MediaQueueItem> list = queue.getQueueItems();
					if(mLoopMode== LoopMode.REAPLAY_SONG){
						removeNextSongAndAppendSameSong(queue, list);
					}else if(!hasNext()){
						removeNextSongs(queue, list);
					}else if(hasNext() && (list.size()-1 == currentPos)){
						// Append next song
						appendNextTrackToCasting();
					}else if(hasNext() && (list.size()-1 > currentPos)){
						// Check next song if different update it
						compareNextSongAndAppend(queue, list);
					}

					else if(list.size()-1 == currentPos){
						if(hasNext()) {
							// It's last song
							// Append Track
						}
					}else{

					}
					*//*for (int i = 0; i < list.size(); i++) {
						MediaQueueItem detail = list.get(i);
						MediaInfo mediaInfo = detail.getMedia();
						if (mediaInfo != null) {
							JSONObject customData = mediaInfo.getCustomData();
							String itemId = customData.get(MainActivity.ITEM_ID).toString();
							String isVideo = customData.get(MainActivity.IS_VIDEO).toString();
							if (!TextUtils.isEmpty(isVideo) && isVideo.equals("0")) {
								mCastManager.queueRemoveItem(Integer.parseInt(itemId),customData);
							}
						}
					}*//*
				}
			} catch (Exception e) {

			}
		}
	}

	private void removePreviousSongs(MediaQueue queue, List<MediaQueueItem> list){
		try {
		for (int i = 0; i < queue.getCurrentItemPosition(); i++) {
			MediaQueueItem detail = list.get(i);
			MediaInfo mediaInfo = detail.getMedia();
			if (mediaInfo != null) {
				JSONObject customData = mediaInfo.getCustomData();
				String itemId = customData.get(MainActivity.ITEM_ID).toString();
				String isVideo = customData.get(MainActivity.IS_VIDEO).toString();
				if (!TextUtils.isEmpty(isVideo) && isVideo.equals("0")) {
					mCastManager.queueRemoveItem(detail.getItemId(), detail.getCustomData());
					Logger.i("removeNextSongs", "removeNextSongs: Removed Track:::");
				}
			}
		}
		}catch (Exception e){}
	}


	private void removeNextSongs(MediaQueue queue, List<MediaQueueItem> list){
		if (needToUseCastingPlayer() && (mPlayMode != PlayMode.LIVE_STATION_RADIO)) {
			try {
				for (int i = queue.getCurrentItemPosition() + 1; i < list.size(); i++) {
					MediaQueueItem detail = list.get(i);
					MediaInfo mediaInfo = detail.getMedia();
					if (mediaInfo != null) {//detail.getItemId()
						JSONObject customData = mediaInfo.getCustomData();
						String itemId = customData.get(MainActivity.ITEM_ID).toString();
						String isVideo = customData.get(MainActivity.IS_VIDEO).toString();
						if (!TextUtils.isEmpty(isVideo) && isVideo.equals("0")) {
							mCastManager.queueRemoveItem(detail.getItemId(), detail.getCustomData());
							Logger.i("removeNextSongs", "removeNextSongs: Removed Track:::");
						}
					}
				}
			} catch (Exception e) {
			}
		}
	}

	private void compareNextSongAndAppend(MediaQueue queue, List<MediaQueueItem> list){
		if (needToUseCastingPlayer() && (mPlayMode != PlayMode.LIVE_STATION_RADIO)) {
			try {
				MediaQueueItem detail = list.get(queue.getCurrentItemPosition() + 1);
				MediaInfo mediaInfo = detail.getMedia();
				if (mediaInfo != null) {
					JSONObject customData = mediaInfo.getCustomData();
					String itemId = customData.get(MainActivity.ITEM_ID).toString();
					Track track = getNextTrack();
					if (track != null && !itemId.equals(track.getId() + "")) {
						removeNextSongs(queue, list);
						getMusicMediaHandle(false, track);
						Logger.i("compareNextSongAndAppend", "compareNextSongAndAppend: Update Next Track");
						*//*String isVideo = customData.get(MainActivity.IS_VIDEO).toString();
						if (!TextUtils.isEmpty(isVideo) && isVideo.equals("0")) {
							mCastManager.queueRemoveItem(detail.getItemId(), detail.getCustomData());
						}*//*
					} else {
						Logger.i("compareNextSongAndAppend", "compareNextSongAndAppend: Next Track Same");
					}
				}
			} catch (Exception e) {
			}
		}
	}

	private void removeNextSongAndAppendSameSong(MediaQueue queue, List<MediaQueueItem> list){
		if (needToUseCastingPlayer() && (mPlayMode != PlayMode.LIVE_STATION_RADIO)) {
			try {
					Track track = getCurrentPlayingTrack();
					removeNextSongs(queue, list);
					getMusicMediaHandle(false, track);
			} catch (Exception e) {
			}
		}
	}

	public void clearCastingQueue(){
		if (isCastConnected() && (mPlayMode != PlayMode.LIVE_STATION_RADIO)) {
			try {
				MediaQueue queue = mCastManager.getMediaQueue();
				if (!queue.isEmpty()) {
					List<MediaQueueItem> list = queue.getQueueItems();
					for (int i = 0; i < list.size(); i++) {
						MediaQueueItem detail = list.get(i);
						MediaInfo mediaInfo = detail.getMedia();
						if (mediaInfo != null) {//detail.getItemId()
							mCastManager.queueRemoveItem(detail.getItemId(), detail.getCustomData());
							Logger.i("removeSongs", "removeSongs: Removed Track:::"+detail.getCustomData());
						}
					}
					*//*mCastManager.clearMediaSession();
					mCastManager.getRemoteMediaInformation();
					MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
					movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, "");
					movieMetadata.putString(MediaMetadata.KEY_TITLE, "");
					mCastManager.loadMedia(new MediaInfo.Builder("")
							.setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
							.setMetadata(movieMetadata)
							.build(), false, 0);*//*;
				}
			} catch (Exception e) {
			}
		}
	}

	private void appendNextTrackToCasting(){
		getMusicMediaHandle(false, getNextTrack());
	}

	private void appendTrackToCasting(Track track) {
		synchronized (syncAddToQueue) {
			needToAddNextSongFirstTime = false;
			// Check Track Available, If yes Remove it (2nd param, true)
		*//*if(mLoopMode != LoopMode.REAPLAY_SONG)
			mPlayback.isTrackAvailable(track,true);*//*
			removeAllNextSongs();
			if (mPlayback != null && track != null && track.getMediaHandle() != null) {
				mPlayback.addToQueue(track);
			}
		}
	}

	public static Object syncAddToQueue = new Object();
	private void appendTrackToCastingIfNotAvailable(final Track track) {
		//needToAddNextSongFirstTime  = false;
		// Check Track Available, If yes Remove it (2nd param, true)
		*//*if(mLoopMode != LoopMode.REAPLAY_SONG)
			mPlayback.isTrackAvailable(track,true);*//*
		synchronized (syncAddToQueue) {
			removeAllNextSongs();
			if (mPlayback != null && track != null && track.getMediaHandle() != null) {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						mPlayback.addToQueue(track);
					}
				}, 2000);

			}
		}
	}



	private void removeAllNextSongs()
	{
		MediaQueue queue = mCastManager.getMediaQueue();
		if (!queue.isEmpty()) {
			List<MediaQueueItem> list = queue.getQueueItems();
			if (!hasNext()) {
				removeNextSongs(queue, list);
			}
		}
	}*/

	public boolean needToUseCastingPlayer() {
		return false;//isCastConnected() && isCastRemoteLoaded();
	}



	// KhoaLT: Carmode implementation.
	public interface IUpdateCarmodePlayerUI {
		void onUpdateUI(int keyCode);
	}

	private List<IUpdateCarmodePlayerUI> mCarModeListener;

	public void setCarModeListener(IUpdateCarmodePlayerUI listener) {
		if (this.mCarModeListener == null) {
			this.mCarModeListener = new ArrayList<IUpdateCarmodePlayerUI>();
		}

		if (!this.mCarModeListener.contains(listener)) {
			this.mCarModeListener.add(listener);
		}
	}

	public void removeCarModeListener(IUpdateCarmodePlayerUI listener) {
		if (this.mCarModeListener != null && this.mCarModeListener.contains(listener)) {
			this.mCarModeListener.remove(listener);
		}
	}


	/***********************************************************
	 * CARMODE
	 ************************************************/
	public int increaseVolume() {
		if (mAudioManager != null) {
			final int curVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

			if (curVolume < mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume + 1, 0);
				return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			}
		}
		return -1;
	}

	public int descreaseVolume() {
		if (mAudioManager != null) {
			final int curVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

			if (curVolume > 0) {
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume - 1, 0);
				return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			} else {
				return 0;
			}
		}

		return -1;
	}
}