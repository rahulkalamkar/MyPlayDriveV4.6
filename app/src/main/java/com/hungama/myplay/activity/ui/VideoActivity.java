package com.hungama.myplay.activity.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.HungamaApplication;

import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.ThreadPoolManager;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.audiocaching.CMEncryptor;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.DataBase.CacheState;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.campaigns.PlacementType;
import com.hungama.myplay.activity.data.dao.hungama.BadgesAndCoins;
import com.hungama.myplay.activity.data.dao.hungama.BaseHungamaResponse;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaTrackDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Video;
import com.hungama.myplay.activity.data.events.PlayEvent;
import com.hungama.myplay.activity.data.events.PlayEvent.PlayingSourceType;
import com.hungama.myplay.activity.gigya.ShareDialogFragment;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.MediaHandleOperation;
import com.hungama.myplay.activity.operations.hungama.AddToFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.operations.hungama.RelatedVideoOperation;
import com.hungama.myplay.activity.operations.hungama.RemoveFromFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.SocialBadgeAlertOperation;
import com.hungama.myplay.activity.operations.hungama.VideoStreamingOperationAdp;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.player.PlayerService.PlayerSericeBinder;
import com.hungama.myplay.activity.player.PlayerService.State;
import com.hungama.myplay.activity.player.PlayerServiceBindingManager;
import com.hungama.myplay.activity.player.PlayerServiceBindingManager.ServiceToken;
import com.hungama.myplay.activity.ui.adapters.MediaTilesAdapterVideo;
import com.hungama.myplay.activity.ui.dialogs.MyProgressDialog;
import com.hungama.myplay.activity.ui.fragments.GlobalMenuFragment;
import com.hungama.myplay.activity.ui.fragments.MainSearchResultsFragment;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.ui.widgets.CustomAlertDialog;
import com.hungama.myplay.activity.ui.widgets.CustomCacheStateProgressBar;
import com.hungama.myplay.activity.ui.widgets.LanguageButton;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Appirater;
import com.hungama.myplay.activity.util.ApsalarEvent;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.ExoVideoPlayer;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.hungama.myplay.activity.util.PicassoUtil.PicassoTarget;
import com.hungama.myplay.activity.util.QuickActionVideoMore;
import com.hungama.myplay.activity.util.QuickActionVideoMore.OnVideoPlayerMoreSelectedListener;
import com.hungama.myplay.activity.util.ScreenLockStatus;
import com.hungama.myplay.activity.util.Utils;
import com.hungama.myplay.activity.util.VideoPlayer;
import com.hungama.myplay.activity.util.VideoPlayerFunctions;
import com.saranyu.SaranyuVideo;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.picasso.Picasso.LoadedFrom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Controller for presenting details of the given MediaItem.
 */
@SuppressLint("NewApi")
public class VideoActivity extends ActionBarActivity implements
		CommunicationOperationListener, OnClickListener, OnCompletionListener,
		SeekBar.OnSeekBarChangeListener, OnPreparedListener,
		OnMediaItemOptionSelectedListener, ServiceConnection, OnErrorListener,
		SaranyuVideo.OnProfileInfoListener, OnBufferingUpdateListener,
		OnVideoPlayerMoreSelectedListener
{

	private static final String TAG = "VideoActivity";
    private String mSearchActionSelected = "No search action selected";
	public static final String ARGUMENT_SEARCH_VIDEO = "argument_search_video";

	// public static final String EXTRA_MEDIA_ITEM = "extra_media_item";
	public static final String EXTRA_MEDIA_ITEM_VIDEO = "extra_media_item_video";
	public static final String EXTRA_MEDIA_LIST_VIDEO = "extra_media_list_video";
	public static final String EXTRA_MEDIA_POS_VIDEO = "extra_media_pos_video";
	public static final String ARGUMENT_MEDIAITEM = "argument_mediaitem";

	public static final String FLURRY_SOURCE_SECTION = "flurry_source_section";

	public static final int UPGRADE_ACTIVITY_RESULT_CODE = 1001;
	public static final int BADGES_ACTIVITY_RESULT_CODE = 10020;

	public static final int SUCCESS = 1;

	private static boolean isLoaded;
	private ServiceToken mServiceToken = null;

	@SuppressWarnings("unused")
	private FragmentManager mFragmentManager;
	private DataManager mDataManager;
	public ApplicationConfigurations mApplicationConfigurations;
	public Video video;
	private RelativeLayout adView;
	public  LinearLayout videoControllersBar;
	private LanguageTextView totalDurationLabel, totalDurationLabelLand;
	private LanguageTextView currentDurationLabel, currentDurationLabelLand;
	private SeekBar videoProgressBar, videoProgressBarLand;
	public ImageButton playButton;
	private LanguageButton skipButton;

	public static final int PERIOD = 4 * 1000;
	private CountDownTimer mCountDownTimer;
	private Handler mHandler = new Handler();

	private MediaItem mMediaItem;
	private boolean mHasLoaded = false;
    private  boolean isfrombackNew=false;

	// INFO PAGE
	private MediaTrackDetails mMediaTrackDetails;
	private RelativeLayout infoPage;
	private LanguageButton infoPageButton;
	private LanguageButton shareButton;
	private ImageView infoButton;
	private boolean infoWasClicked = false;
	private LinearLayout infoAlbum;
	private LinearLayout infoLanguageCategory;
	private LinearLayout infoMood;
	private LinearLayout infoGenre;
	private LinearLayout infoMusic;
	private LinearLayout infoSingers;
	private LinearLayout infoCast;
	private LinearLayout infoLyrics;

	private MediaTilesAdapterVideo mHomeMediaTilesAdapter;
	private RecyclerView mTilesGridViewRelated;
	private int mTileSize = 0;

	private RelativeLayout relatedVideoPage;
	private LanguageButton relatedVideoPageButton;

	private LanguageButton albumPageButton;

	private Dialog upgradeDialog;
	private Date userCurrentPlanValidityDate;
	private Date today;

	// Favorites
	private Drawable whiteHeart;
	private Drawable blueHeart;
	private String mediaType;
	private Button favButton;
	private Button commentsButton;

	private boolean isHasToCallBadgeApi = true;
	private boolean isBackFromBadgeApi = false;
	private boolean isInfoPagePopulated = false;
	private boolean isFavoritePressed = false;

	private LinearLayout linearlayout_video_seekbar_land,
			linearlayout_video_seekbar;

	// Advertising

	private String backgroundLink;
	private static Drawable backgroundImage;
	private String backgroundLinkInfo;
	private static Drawable backgroundImageInfo;
	private Handler handler;
	private int width;
	private Placement placementVideo;
	private Placement placementInfo;
	public Placement placementVideoAd;

	private int dpi;

	// For testing
	private int playButtonClickCounter;

	private int networkSpeed;
	private String networkType;

	private long mFileSize;
	// Calculate bandwidth
	private boolean firstEntry = true;
	private boolean lastEntry = true;
	private int percentStart;
	private long startTimeToCalculateBitrate;
	private long endTimeToCalculateBitrate;

	private boolean mIsSendToBackgroundPowerButtonPress = false;
	private boolean mIsSendToBackgroundHomeButtonPress = false;
	private long mCurrentPosition;
	int mPausedSeekPos;

	private List<MediaItem> mediaItems = new ArrayList<MediaItem>();

	private boolean pauseVideo = true;

	private String googleEmailId = null;

	private int orientation;

	private CacheStateReceiver cacheStateReceiver;
    private PrevScreenCloseReceiver prevVideoCloseReceiver;

	private PlayerService playerService;
	private List<MediaItem> videoList, relatedList;
	private int currentVideoPlayPos = 0;
	private int currentVideoPlayPosRelated = -1;
	public ProgressBar pbVideo;

	// private int playBackCounter = 0;
	private MediaPlayer player;
	VideoView videoView;
	// ======================================================
	// Activity life-cycle callbacks.
	// ======================================================
	VideoPlayerFunctions player1;

	protected PowerManager.WakeLock mWakeLock;

	//-----------CromeCast--------------//
	//private VideoCastManager mCastManager;
	int statusBarHeight = 0, navigationBarHeight = 0;
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// setOverlayAction();
		statusBarHeight = getStatusBarHeight();
		navigationBarHeight = getNavigationBarHeight();
		mDataManager = DataManager.getInstance(this.getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		try {
			/*getWindow()
					.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);*/
			super.onCreate(savedInstanceState);
			final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
					"My Tag");
			this.mWakeLock.acquire();
			View root = LayoutInflater.from(VideoActivity.this).inflate(
					R.layout.activity_video, null);

			setContentView(root);

			if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
				Utils.traverseChild(root, VideoActivity.this);
			}
		} catch (Error e) {
			System.gc();
			System.runFinalization();
			System.gc();
		}
		initializeViews();

		RelativeLayout rl_landscape_title = ((RelativeLayout) findViewById(R.id.rl_landscape_title));
		rl_landscape_title.setPadding(0, statusBarHeight, 0, 0);

		linearlayout_video_seekbar_land.setPadding(0,0,navigationBarHeight,0);
		Bundle data = getIntent().getExtras();
		if(data!=null && data.containsKey(ARGUMENT_MEDIAITEM)) {
			mMediaItem = (MediaItem) data.getSerializable(ARGUMENT_MEDIAITEM);
			needClose = data.getBoolean("needClose");
		}
		mFragmentManager = getSupportFragmentManager();
		onBackPressListner();
		// validate calling intent.
		Intent intent = getIntent();
		if (intent == null) {
			Logger.e(TAG, "No intent for the given Activity.");
			return;
		}
		//setupActionBar();
		data = intent.getExtras();
		if (data != null && data.containsKey(EXTRA_MEDIA_ITEM_VIDEO)) {
			mMediaItem = (MediaItem) data
					.getSerializable(EXTRA_MEDIA_ITEM_VIDEO);
			if (mMediaItem != null) {
				loadBlurImgBitmap();
				setTitleText(mMediaItem.getTitle(), mMediaItem.getAlbumName());
			}
		} else {
			Logger.e(TAG, "No MediaItem set for the given Activity.");
			return;
		}
		if (data != null && data.containsKey(EXTRA_MEDIA_LIST_VIDEO)) {
			videoList = (List<MediaItem>) data
					.getSerializable(EXTRA_MEDIA_LIST_VIDEO);
			Logger.i("videoList count", "Video List Count:" + videoList.size());
		} else {
			videoList = new ArrayList<MediaItem>();
			videoList.add(mMediaItem);

		}
		if (data != null && data.containsKey(EXTRA_MEDIA_POS_VIDEO)) {
			currentVideoPlayPos = data.getInt(EXTRA_MEDIA_POS_VIDEO);
			Logger.i("currentVideoPlayPos", "currentVideoPlayPos:"
					+ currentVideoPlayPos);

			if (currentVideoPlayPos < 0)
				currentVideoPlayPos = 0;
		} else {
			currentVideoPlayPos = 0;
		}

		currentVideoPlayPosRelated = -1;
		relatedList = new ArrayList<MediaItem>();
		whiteHeart = getResources().getDrawable(
				R.drawable.icon_main_player_favorites_white);
		blueHeart = getResources().getDrawable(
				R.drawable.icon_main_player_favorites_blue);

		/*
		 * For placing the tiles correctly in the grid, calculates the maximum
		 * size that a tile can be and the column width.
		 */
		int imageTileSpacing = getResources().getDimensionPixelSize(
				R.dimen.home_tiles_spacing_vertical);
		// measuring the device's screen width. and setting the grid column
		// width.
		Display display = getWindowManager().getDefaultDisplay();
		int screenWidth = 0;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
			screenWidth = display.getWidth();
		} else {
			Point displaySize = new Point();
			display.getSize(displaySize);
			screenWidth = displaySize.x;
		}

		mTileSize = (int) ((screenWidth - (imageTileSpacing + imageTileSpacing * 1.5)) / 2);

		Logger.i(TAG, "screenWidth: " + screenWidth + " mTileSize: "
				+ mTileSize);

		mTilesGridViewRelated = (RecyclerView) findViewById(R.id.video_related_gridview_tiles);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(VideoActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mTilesGridViewRelated.setLayoutManager(layoutManager);
        resetMediaTileAdapter();
		mTilesGridViewRelated
				.setOnScrollListener(new RecyclerView.OnScrollListener() {

                    @Override
                    public void onScrollStateChanged(RecyclerView view,
                                                     int scrollState) {
                        if (scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                            PicassoUtil.with(VideoActivity.this).resumeTag();
                            try {
                                mHomeMediaTilesAdapter.postAdForPosition();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            PicassoUtil.with(VideoActivity.this).pauseTag();
                        }
                    }

                });
		if (!mApplicationConfigurations.getSaveOfflineMode()) {
			String sesion = mApplicationConfigurations
					.getSessionID();
			boolean isRealUser = mApplicationConfigurations
					.isRealUser();
			if (!TextUtils.isEmpty(sesion) && (isRealUser || Logger.allowPlanForSilentUser)) {
				String accountType = Utils.getAccountName(getApplicationContext());
				if (accountType != null) {
					googleEmailId = accountType;
				}
				mDataManager.getCurrentSubscriptionPlan(this, accountType);
			}
		}
		Set<String> tags = Utils.getTags();
		if (!tags.contains("videos_used")) {
			tags.add("videos_used");
			Utils.AddTag(tags);
		}

		Switch toggleButton = (Switch) findViewById(R.id.toggleButton);
		toggleButton.setChecked(mApplicationConfigurations
				.isNextVideoAutoPlay());
		toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				mApplicationConfigurations.setNextVideoAutoPlay(arg1);
			}
		});
		// Patibandha
		//initializeCromCast();
	}

	/*Toolbar mToolbar;
	private void setupActionBar() {
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolbar.setVisibility(View.GONE);

		//mToolbar.setTitle(R.string.app_name);
		//setSupportActionBar(mToolbar);
	}*/

	RelativeLayout includeHeaderView;

	/**
	 * Initialize all views
	 */
	LanguageTextView tvUpNextName;

	private void initializeViews() {
		linearlayout_video_seekbar_land = (LinearLayout) findViewById(R.id.linearlayout_video_seekbar_land);
		linearlayout_video_seekbar = (LinearLayout) findViewById(R.id.linearlayout_video_seekbar);
		tvUpNextName = (LanguageTextView) findViewById(R.id.tvUpNextName);
		videoControllersBar = (LinearLayout) findViewById(R.id.linearlayout_player_bar);
		totalDurationLabel = (LanguageTextView) findViewById(R.id.textview_video_player_scale_length);
		currentDurationLabel = (LanguageTextView) findViewById(R.id.textview_video_player_scale_current);
		totalDurationLabelLand = (LanguageTextView) findViewById(R.id.textview_video_player_scale_length_land);
		currentDurationLabelLand = (LanguageTextView) findViewById(R.id.textview_video_player_scale_current_land);
		videoProgressBar = (SeekBar) findViewById(R.id.seekbar_video_player);
		videoProgressBar.setOnSeekBarChangeListener(this);
		videoProgressBarLand = (SeekBar) findViewById(R.id.seekbar_video_player_land);
		videoProgressBarLand.setOnSeekBarChangeListener(this);
		setSeekBarVisibility(false);
		includeHeaderView = (RelativeLayout) findViewById(R.id.includeHeaderView);
		mTitleBarTextVideo = (TextView) includeHeaderView
				.findViewById(R.id.main_player_content_info_bar_text_title_handle);

		mTitleBarTextVideoLandscape = (TextView) findViewById(R.id.main_player_content_info_bar_text_title_landscape);

		mTitleBarTextVideoAlbum = (TextView) includeHeaderView
				.findViewById(R.id.main_player_content_info_bar_text_additional_handle);
		ImageView ivBackArrow = (ImageView) includeHeaderView
				.findViewById(R.id.ivBackArrow);
		ivBackArrow.setOnClickListener(this);
		pbVideo = (ProgressBar) findViewById(R.id.pbVideo);
		relatedVideoPageButton = (LanguageButton) findViewById(R.id.video_player_content_actions_bar_button_related);

		SurfaceView exo_surfaceView = (SurfaceView) findViewById(R.id.videoview_video_surface_view);
		videoView = (VideoView) findViewById(R.id.videoview_video_details);
		RelativeLayout rlVideoMainScreen = (RelativeLayout) findViewById(R.id.relativelayout_activity_video);
		if (Utils.isNeedToUseHLS()) {
			player1 = new ExoVideoPlayer();
			player1.init(exo_surfaceView, VideoActivity.this, rlVideoMainScreen);
			exo_surfaceView.setVisibility(View.VISIBLE);
			videoView.setVisibility(View.GONE);
		} else {
			player1 = new VideoPlayer();
			player1.init(videoView, VideoActivity.this, rlVideoMainScreen);
			exo_surfaceView.setVisibility(View.GONE);
			videoView.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Set Seekbar visibility for landscape and portrait
	 *
	 * @param isLandscap
	 */
	private void setSeekBarVisibility(boolean isLandscap) {
		if (videoProgressBar != null && videoProgressBarLand != null) {
			if (isLandscap) {
				if (videoProgressBar.getVisibility() == View.VISIBLE) {
					linearlayout_video_seekbar_land.setVisibility(View.VISIBLE);
					linearlayout_video_seekbar.setVisibility(View.GONE);
				}
			} else {
				if (videoProgressBarLand.getVisibility() == View.VISIBLE) {
					linearlayout_video_seekbar_land.setVisibility(View.GONE);
					linearlayout_video_seekbar.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	private void onBackPressListner() {
		ImageView ivDownArrow = (ImageView) findViewById(R.id.ivDownArrowVideo);
		ivDownArrow.setOnClickListener(VideoActivity.this);
		ImageView ivDownArrowVideo1 = (ImageView) findViewById(R.id.ivDownArrowVideo1);
		ivDownArrowVideo1.setOnClickListener(VideoActivity.this);

	}

	/**
	 * Set media tile adapter for Related video page
	 */
	private void resetMediaTileAdapter() {
		mediaItems = new ArrayList<MediaItem>();

		//setRelativeAdapter();

		/*mHomeMediaTilesAdapter = new MediaTilesAdapterVideo(this,
				mTilesGridViewRelated, mTileSize,
				getClass().getCanonicalName(), null, null,
				CampaignsManager.getInstance(this), mediaItems, false,
				FlurryConstants.FlurrySubSectionDescription.VideoRelated
						.toString());
		mHomeMediaTilesAdapter.setOnMusicItemOptionSelectedListener(this);

		relatedVideoPage = (RelativeLayout) findViewById(R.id.video_related_relativelayout_page);
		relatedVideoPage.setOnClickListener(this);

		LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
		mTilesGridViewRelated.setLayoutManager(mLayoutManager);

		mTilesGridViewRelated.setAdapter(mHomeMediaTilesAdapter);*/

		refreshCacheState();

		TextView player_lyrics_title_bar_text = (TextView) findViewById(R.id.player_lyrics_title_bar_text);
		player_lyrics_title_bar_text.setText(mMediaItem.getTitle());

		TextView relatedVideotitleBaText = (TextView) findViewById(R.id.video_title_bar_text_related);
		relatedVideotitleBaText.setText(mMediaItem.getTitle());

		TextView player_lyrics_sub_title_bar_text = (TextView) findViewById(R.id.player_lyrics_sub_title_bar_text);
		player_lyrics_sub_title_bar_text.setText(mMediaItem.getAlbumName());

		TextView relatedVideoSubtitleBaText = (TextView) findViewById(R.id.video_sub_title_bar_text_related);
		relatedVideoSubtitleBaText.setText(mMediaItem.getAlbumName());

	}

	private void setRelativeAdapter(){
		mHomeMediaTilesAdapter = new MediaTilesAdapterVideo(this,
				mTilesGridViewRelated, mTileSize,
				getClass().getCanonicalName(), null, null,
				CampaignsManager.getInstance(this), mediaItems, false,
				FlurryConstants.FlurrySubSectionDescription.VideoRelated
						.toString());
		mHomeMediaTilesAdapter.setOnMusicItemOptionSelectedListener(this);

		relatedVideoPage = (RelativeLayout) findViewById(R.id.video_related_relativelayout_page);
		relatedVideoPage.setOnClickListener(this);

		LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
		mTilesGridViewRelated.setLayoutManager(mLayoutManager);

		mTilesGridViewRelated.setAdapter(mHomeMediaTilesAdapter);
	}

	@Override
	protected void onPause() {
		mPausedSeekPos = videoProgressBar.getProgress();
		try {
			needToResume = player1.isPlaying() ? true : false;
			Logger.i(TAG, "onPause()mPausedSeekPos:" + mPausedSeekPos
					+ " ::: needToResume:" + needToResume);
			stopProgressBar();
			if (player1 != null) {
				if (pauseVideo) {
					mIsSendToBackgroundPowerButtonPress = true;
					mIsSendToBackgroundHomeButtonPress = true;
					if (isNextIndicatorLoaderDisplay())
						mCurrentPosition = 0;
					else
						mCurrentPosition = player1.getCurrentPosition();
					// videoView.pause();
					player1.pauseVideo();
					if (player != null)
						player.pause();
					if (!isRepeatTagSet()) {
						isPAused = true;
						playButton.setImageResource(R.drawable.ic_play);
						setReapatTag(false);
						isActivityPaused = false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
        if(!isOtherScreenOpen)
            resetPlayerState();
		//Patibandha
		//onPauseCromeCast();
		super.onPause();
	}

	private boolean isRepeatTagSet() {
		return (playButton != null
				&& playButton.getTag(R.string.TAG_IS_REPEAT) != null && ((Boolean) playButton
					.getTag(R.string.TAG_IS_REPEAT)));
	}

	@Override
	protected void onStart() {
		super.onStart();

		/*
		 * Faking the name of the title due to an unsupported deep navigation
		 * (more then two items). This Activity will set the title hardcoded.
		 */
		Analytics.startSession(this);

		try {
			if (mMediaItem == null) {
				Bundle data = getIntent().getExtras();
				if (data != null && data.containsKey(ARGUMENT_MEDIAITEM)) {
					// retrieves the given Media item for the activity.
					mMediaItem = (MediaItem) data
							.getSerializable(ARGUMENT_MEDIAITEM);
				} else {
					Logger.e(TAG, "No MediaItem set for the given Activity.");
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		//Added by p
		if (isPAused && mCurrentPosition == 0 && !infoWasClicked) {
//			System.out.println("mCurrentPosition ::::::::::: "
//					+ mCurrentPosition);
//			System.out.println("Patibandha resume back ::::::::::: ");

			finish();
			return;
		}
		// Patibandha
		//onResumeCromeCast();
		mIsSendToBackgroundPowerButtonPress = false;
		mIsSendToBackgroundHomeButtonPress = false;
		if (playerService != null) {
			playerService.setPausedFromVideo(true);
			if (playerService.isLoading()
					|| playerService.getState() == State.PLAYING
					|| playerService.getState() == State.PREPARED) {
				playerService.pause();
			}
		}
        relatedVideoPage = (RelativeLayout) findViewById(R.id.video_related_relativelayout_page);
		if (relatedVideoPage.getVisibility() == View.VISIBLE) {
			return;
		}
		if (isFromFavoriteScreen) {
			isFromFavoriteScreen = false;
			return;
		}
        if(isPauseByFocusChange){
            loadNextVideoForWindowFocus();
            isPauseByFocusChange = false;
            return;
        }
		if (playButton != null
				&& playButton.getTag(R.string.TAG_IS_REPEAT) != null
				&& ((Boolean) playButton.getTag(R.string.TAG_IS_REPEAT)))
			return;
		Logger.i(TAG, "onResume():mCurrentPosition:" + mCurrentPosition
				+ " :: mPausedSeekPos:" + mPausedSeekPos + " :: needToResume:"
				+ needToResume);
		HungamaApplication.activityPaused();
		ScreenLockStatus.getInstance(getBaseContext()).onResume(this, this);

		if (isOtherScreenOpen && pauseVideo && mCurrentPosition != 0) {
			isOtherScreenOpen = false;
			// videoView.seekTo(mCurrentPosition);
			player1.seekToVideo(mCurrentPosition);
			videoProgressBar.setProgress(mPausedSeekPos);
			// if (needToResume) {
			if (!isPAusedByUser) {
				// videoView.start();
				player1.startVideo(false);
				if (player1.isPlaying())
					updateProgressBar();
				playButton.setImageResource(R.drawable.ic_pause);
				setReapatTag(false);
				// }
			}
			needToResume = false;
			isPAused = false;
			isActivityPaused = false;
			return;
		}

		if (isActivityPaused && isVideoLoaded) {
			isVideoLoaded = false;
			isActivityPaused = false;
			if (isAdPlaying)
				player1.startVideo(false);
			else if (!player1.isPlaying()) {
				startPlaying();
				playButton.setImageResource(R.drawable.ic_pause);
				setReapatTag(false);
				if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
				}
			}
			return;
		}
		if (isPAused && mCurrentPosition > 0 && !infoWasClicked) {
			Logger.s("mCurrentPosition ::::::::::: "
					+ mCurrentPosition);
			// videoView.resume();
			// videoView.seekTo(mCurrentPosition);

			player1.seekToVideo(mCurrentPosition);
			//if(Utils.isNeedToUseHLS()){
				player1.startVideo();
			//}
			isPAused = false;
			return;
		}
		if (isPausedForDownload) {
			isPausedForDownload = false;
			if (!player1.isPlaying()) {
				startPlaying();
				setReapatTag(false);
				playButton.setImageResource(R.drawable.ic_pause);
				if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
				}
			}
			return;
		}

		/*
		 * Binds to the PLayer service to pause it if playing.
		 */
		mServiceToken = PlayerServiceBindingManager.bindToService(this, this);
		if (cacheStateReceiver == null && CacheManager.isProUser(this)) {
			cacheStateReceiver = new CacheStateReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(CacheManager.ACTION_VIDEO_CACHE_STATE_UPDATED);
			filter.addAction(CacheManager.ACTION_VIDEO_TRACK_CACHED);
			filter.addAction(CacheManager.ACTION_VIDEO_UPDATED_CACHE);
			registerReceiver(cacheStateReceiver, filter);
		}

        if(prevVideoCloseReceiver==null){
            //sendBroadcast(new Intent("ClosePrevVideoAction"));
            prevVideoCloseReceiver = new PrevScreenCloseReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("ClosePrevVideoAction");
            registerReceiver(prevVideoCloseReceiver, filter);
        }

        if(badgesScreenFinishReceiver==null){
            badgesScreenFinishReceiver = new BadgesScreenFinishReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(BadgesAndCoinsActivity.ARGUMENT_IS_FINISHED_BADGES);
            registerReceiver(badgesScreenFinishReceiver, filter);
        }


		Logger.i("API Call", "API call: isBackFromBadgeApi:"
				+ isBackFromBadgeApi);
		if (!isBackFromBadgeApi && !infoWasClicked) {
			Logger.i("API Call", "API call: isFavoritePressed:"
					+ isFavoritePressed);
			if(isAdPlaying  || isAdLoading){
				player1.startVideo(true);
			}
			else if (!isFavoritePressed) {
				Logger.i("API Call", "API call: mHasLoaded:" + mHasLoaded);
				if (!mHasLoaded) {
					if (mApplicationConfigurations.getSaveOfflineMode()) {
						isVideoTrackCached();
						populateUserControls(video);
					} else {
						SaranyuVideo s = new SaranyuVideo();
						VideoView videoViewSaranyu = (VideoView) findViewById(R.id.vview_saranyu);
						networkSpeed = getNetworkBandwidth();
						networkType = Utils.getNetworkType(this);
						String contentFormat = mApplicationConfigurations
								.getContentFormat();

						if (TextUtils.isEmpty(contentFormat)) {
							showLoadingDialog(R.string.application_dialog_loading_content);
							s.getProfileCapablity(videoViewSaranyu, this);
						} else {
							Logger.i("API Call", "API call");
							if (!isVideoTrackCached()) {
								Logger.i("API Call", "API call1");
								mDataManager.getVideoDetailsAdp(mMediaItem,
										networkSpeed, networkType,
										contentFormat, this, googleEmailId);
							}
							// checkOfflineVideoAndPlay(contentFormat);
						}
					}
				} else {
					if (!isAdPlaying)
						initializeComponents();
					if (!isInfoPagePopulated) {
						isInfoPagePopulated = true;
						populateInfoPage();
					}
					populateUserControls(video);
				}
				if (!isVideoCached) {
					onConfigurationChanged(getResources().getConfiguration());
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
				}
			}

			if (isFavoritePressed) {
				isFavoritePressed = false;
			}
		} else {
			isBackFromBadgeApi = false;
		}

	}

    private void checkOfflineVideoAndPlay(final String contentFormat){
		ThreadPoolManager.getInstance().submit(new Runnable() {
			@Override
			public void run() {
//				super.run();
				if (!isVideoTrackCached()) {
					Logger.i("API Call", "API call1");
					VideoActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mDataManager.getVideoDetailsAdp(mMediaItem,
									networkSpeed, networkType,
									contentFormat, VideoActivity.this, googleEmailId);
						}
					});
				}
			}
		});
    }


	@Override
	protected void onDestroy() {

		backanddestroy(false);

		//Added by p
		//if (isPAused && mCurrentPosition > 0 && !infoWasClicked) {
		if (!isPAused && mCurrentPosition == 0 && infoWasClicked) {
//			System.out.println("mCurrentPosition ::::::::::: "
//					+ mCurrentPosition);
			//Patibandha
			//onDestroyCromeCast();
//			System.out.println("Patibandha OnDestroy Stopcasts  ::::::::::: " + mCurrentPosition);
		}else{
//			System.out.println("Patibandha Backbutton  ::::::::::: " + mCurrentPosition);
		}

		super.onDestroy();

	}





	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// put inside the current duaration.
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {

	}

	/**
	 * Connect Player service and Stop playing if music player running
	 *
	 * @param name
	 * @param service
	 */
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		PlayerSericeBinder binder = (PlayerSericeBinder) service;
		playerService = binder.getService();

		playerService.setPausedFromVideo(true);
		if (playerService.isLoading()
				|| playerService.getState() == State.PLAYING
				|| playerService.getState() == State.PREPARED) {
			playerService.pause();
		}
	}

    void resetPlayerState()
    {
        if (playerService != null)
        {
            if (playerService.getPausedFromVideo()) {
                playerService.setPausedFromVideo(false);
            }
        }
    }

    private void unbindDrawable(){
        RelativeLayout rlVideoMainScreen = (RelativeLayout) findViewById(R.id.relativelayout_activity_video);
        if (Build.VERSION.SDK_INT >= 16) {
            rlVideoMainScreen.setBackground(null);
            if(relatedVideoPage!=null){
                relatedVideoPage.setBackground(null);
            }
            if(infoPage!=null){
                infoPage.setBackground(null);
            }


        } else {
            rlVideoMainScreen.setBackgroundDrawable(null);
            if(relatedVideoPage!=null){
                relatedVideoPage.setBackgroundDrawable(null);
            }

            if(infoPage!=null){
                infoPage.setBackgroundDrawable(null);
            }


        }
    }

	@Override
	public void finish() {
		//resetPlayerState();
		super.finish();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		resetPlayerState();
		mServiceToken = null;
	}

	@Override
	public void onBackPressed() {
		/*try {
            System.out.println("VIDEO 1");
			if (infoPage.getVisibility() == View.VISIBLE) {
				infoPageButton.performClick();
				return;
			}
			if (relatedVideoPage.getVisibility() == View.VISIBLE) {
				relatedVideoPageButton.performClick();
				return;
			}

			// 1 for Configuration.ORIENTATION_PORTRAIT
			// 2 for Configuration.ORIENTATION_LANDSCAPE
			// 0 for Configuration.ORIENTATION_SQUARE
			if (getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
				fullScreenButton.performClick();
				return;
			}

			if (isAdPlaying) {
				int playCurrentPostion = (int) (player1.getCurrentPosition() / 1000);
				Utils.postPlayEvent(getApplicationContext(), placementVideoAd,
						playCurrentPostion, false);
			}
			resetPlayerState();
            cancelThread();

			finish();

//			mDataManager.cancelGetSearch();
		} catch (Exception e) {

		}*/
         if(!backanddestroy(true))
            super.onBackPressed();



	}

    private boolean backanddestroy(boolean isBackPress)
	{
        try {

            if (isBackPress) {


                if (infoPage.getVisibility() == View.VISIBLE) {
                    infoPageButton.performClick();
                    return true;
                }

                if (relatedVideoPage.getVisibility() == View.VISIBLE) {
                    relatedVideoPageButton.performClick();
                    return true;
                }

                // 1 for Configuration.ORIENTATION_PORTRAIT
                // 2 for Configuration.ORIENTATION_LANDSCAPE
                // 0 for Configuration.ORIENTATION_SQUARE
                if (getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
                    fullScreenButton.performClick();
                    return true;
                }


                if (isAdPlaying) {
                    int playCurrentPostion = (int) (player1.getCurrentPosition() / 1000);
                    Utils.postPlayEvent(getApplicationContext(), placementVideoAd,
                            playCurrentPostion, false);
                }

                resetPlayerState();
                cancelThread();


            }

            if (isfrombackNew)
                return false;

            isfrombackNew = true;


            PlayerServiceBindingManager.unbindFromService(mServiceToken);

            resetPlayerState();
		/*new Thread()
		{
			public void run()
			{
				stopVideoPlayEvent(false,(int) player1.getCurrentPosition());
			}
		}.start();
*/
            stopVideoPlayEvent(false, (int) player1.getCurrentPosition());
            if (decryptedFile != null && decryptedFile.exists())
                decryptedFile.delete();

            if (cacheStateReceiver != null)

            unregisterReceiver(cacheStateReceiver);
            cacheStateReceiver = null;

            if (prevVideoCloseReceiver != null)
                unregisterReceiver(prevVideoCloseReceiver);
            prevVideoCloseReceiver = null;

            if (badgesScreenFinishReceiver != null)
                unregisterReceiver(badgesScreenFinishReceiver);
            badgesScreenFinishReceiver = null;
       /* RelativeLayout rlVideoMainScreen = (RelativeLayout) findViewById(R.id.relativelayout_activity_video);
        Utils.unbindDrawables(rlVideoMainScreen);
        Utils.clearCache(true);*/
            unbindDrawable();
            stopProgressBar();
			ThreadPoolManager.getInstance().submit(new Runnable() {
				public void run() {
					player1.releasePlayer();

				}
			});
            this.mWakeLock.release();


            if (isBackPress)
                finish();


         }catch(Exception e){

         }
      return false;

    }


	/**
	 * Get screen Orientation
	 *
	 * @return
	 */

	public int getScreenOrientation() {
		Display getOrient = getWindowManager().getDefaultDisplay();
		int orientation = Configuration.ORIENTATION_UNDEFINED;
		if (getOrient.getWidth() == getOrient.getHeight()) {
			orientation = Configuration.ORIENTATION_SQUARE;
		} else {
			if (getOrient.getWidth() < getOrient.getHeight()) {
				orientation = Configuration.ORIENTATION_PORTRAIT;
			} else {
				orientation = Configuration.ORIENTATION_LANDSCAPE;
			}
		}
		return orientation;
	}

	@Override
	protected void onStop() {
		//resetPlayerState();
		super.onStop();

		//closePage(relatedVideoPage, relatedVideoPageButton);

		Analytics.onEndSession(this);
	}

	boolean isFromFavoriteScreen = false;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Logger.i(TAG, "onActivityResult()");
		if (requestCode == UPGRADE_ACTIVITY_RESULT_CODE
				&& resultCode == RESULT_OK) {
			if (Utils.isNeedToUseHLS() && player1 != null) {
				player1.releasePlayer();
			} else if (player1 != null)
				((VideoPlayer) player1).videoView.stopPlayback();
			mHasLoaded = false;
		} else if (requestCode == BADGES_ACTIVITY_RESULT_CODE
				&& resultCode == RESULT_OK && data != null) {
			pauseVideo = true;
		}
	}

	// ======================================================
	// Operation Callback
	// ======================================================

	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_STREAMING_ADP) {
			Logger.i("API Call", "API call2");
			showLoadingDialog(R.string.application_dialog_loading_content);
			mHasLoaded = true;
		} else if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
			if (mMediaTrackDetails == null) {
				showLoadingDialog(R.string.application_dialog_loading_content);
				mHasLoaded = true;
			}
		} else if (operationId == OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE) {
			// showLoadingDialog(R.string.application_dialog_loading_content);
			isFavoritePressed = true;
		} else if (operationId == OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE) {
			// showLoadingDialog(R.string.application_dialog_loading_content);
		}
        else if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_RELATED) {
            showLoadingDialog1();
        }

	}

	/**
	 * Get callback from any api call with operation id and Response.
	 *
	 * @param operationId
	 * @param responseObjects
	 */

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_STREAMING_ADP) {
				Logger.i("API Call", "API call3");

				video = (Video) responseObjects
						.get(VideoStreamingOperationAdp.RESPONSE_KEY_VIDEO_STREAMING_ADP);

				// Replacing the CountDownTimer below with this AsynTask
				GetVideoContentLengthAsync asyncTask = new GetVideoContentLengthAsync();
				asyncTask.setVideoUrl(video.getVideoUrl());
				asyncTask.execute();

				initializeComponents();
				// populateUserControls(video);
				/*if(isCallForCasting){
					mSelectedMedia = Utils.buildMediaInfo(mMediaItem.getTitle(), mMediaItem.getTitle(), mMediaItem.getTitle(),
							0, video.getVideoUrl(), Utils.getVideoMimeType(), url, mMediaItem.getId(), VideoActivity.this, video.getDeliveryId());
					loadRemoteMedia(0, true);
				}else {*/
					mDataManager.getMediaDetails(mMediaItem, null, this);
				//}
				//isCallForCasting = false;
			} else if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {

				if (mMediaItem.getMediaType() == MediaType.TRACK
						|| mMediaItem.getMediaType() == MediaType.VIDEO) {
					// get details for track (video).
					mMediaTrackDetails = (MediaTrackDetails) responseObjects
							.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);


					if(TextUtils.isEmpty(mMediaItem.getTitle())){
						TextView player_lyrics_title_bar_text = (TextView) findViewById(R.id.player_lyrics_title_bar_text);
						player_lyrics_title_bar_text.setText(mMediaTrackDetails.getTitle());

						TextView relatedVideotitleBaText = (TextView) findViewById(R.id.video_title_bar_text_related);
						relatedVideotitleBaText.setText(mMediaTrackDetails.getTitle());
					}
					if(TextUtils.isEmpty(mMediaItem.getAlbumName())){
						TextView player_lyrics_sub_title_bar_text = (TextView) findViewById(R.id.player_lyrics_sub_title_bar_text);
						player_lyrics_sub_title_bar_text.setText(mMediaTrackDetails.getAlbumName());

						TextView relatedVideoSubtitleBaText = (TextView) findViewById(R.id.video_sub_title_bar_text_related);
						relatedVideoSubtitleBaText.setText(mMediaTrackDetails.getAlbumName());
					}


					loadMediaContentWithAd();
					if (infoWasClicked) {
						// hideLoadingDialog();
						closePage(relatedVideoPage, relatedVideoPageButton);
						// closePage(albumPage, albumPageButton);
						openInfoPage();
					} else {
						// hideLoadingDialog();
					}



					/*if (!mApplicationConfigurations.getSaveOfflineMode()) {
                        mDataManager.getRelatedVideo(mMediaTrackDetails,mMediaItem, this);
					}*/

                  /*  if (mHomeMediaTilesAdapter != null) {
                        mHomeMediaTilesAdapter.notifyDataSetChanged();
                    }*/


					if (!isInfoPagePopulated) {
						isInfoPagePopulated = true;
						populateInfoPage();
					}
				}

			} else if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_RELATED) {

				mediaItems.addAll((List<MediaItem>) responseObjects
						.get(RelatedVideoOperation.RESPONSE_KEY_RELATED_VIDEO));
				Placement placement = null;
				CampaignsManager mCampaignsManager = CampaignsManager
						.getInstance(this);
				placement = mCampaignsManager
						.getPlacementOfType(PlacementType.VIDEO_RELATED_BANNER);
				if (placement != null && mediaItems != null) {
					for (int i = 3; i < mediaItems.size(); i += 6) {
						Logger.i("Hint", String.valueOf(i));
						MediaItem mediaItem = new MediaItem(i, "no", "no",
								"no", null, null, "album", 0, 0);
						mediaItem.setMediaContentType(MediaContentType.VIDEO);
						mediaItem.setMediaType(MediaType.TRACK);
						mediaItems.add(i, mediaItem);
					}
				}
				List<Object> temp = new ArrayList<Object>();
				if (mediaItems != null) {
					for (MediaItem item : mediaItems) {
						temp.add(item);
					}
				}
                relatedVideoPage.setVisibility(View.VISIBLE);
                //if (relatedVideoPage.getVisibility() == View.VISIBLE)
                    setRelativeAdapter();
                hideLoadingDialogNew();
               //mHomeMediaTilesAdapter.setMediaItems(temp);
				// mHomeMediaTilesAdapter.notifyDataSetChanged();

			} else if (operationId == OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE) {
				BaseHungamaResponse addToFavoriteResponse = (BaseHungamaResponse) responseObjects
						.get(AddToFavoriteOperation.RESULT_KEY_ADD_TO_FAVORITE);
				if (addToFavoriteResponse.getCode() == SUCCESS) {
					Utils.makeText(VideoActivity.this,
							addToFavoriteResponse.getMessage(),
							Toast.LENGTH_LONG).show();
					favButton.setCompoundDrawablesWithIntrinsicBounds(null,
							blueHeart, null, null);

					favButton.setSelected(true);
					isFavoritePressed = true;

					BadgesAndCoins badgesAndCoins = mApplicationConfigurations
							.getBadgesAndCoinsForVideoActivity();
					if (badgesAndCoins != null
							&& addToFavoriteResponse.getDisplay() != 1) {
						pauseVideo = false;
						startBadgesAndCoinsActivity(badgesAndCoins);
					}

					mDataManager.checkBadgesAlert("" + mMediaItem.getId(),
							"video", SocialBadgeAlertOperation.ACTION_FAVORITE,
							this);
				} else {
					Utils.makeText(
							this,
							getResources().getString(
									R.string.favorite_error_saving,
									mMediaItem.getTitle()), Toast.LENGTH_LONG)
							.show();
				}
				favButton.setClickable(true);

			} else if (operationId == OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE) {
				BaseHungamaResponse removeFromFavoriteResponse = (BaseHungamaResponse) responseObjects
						.get(RemoveFromFavoriteOperation.RESULT_KEY_REMOVE_FROM_FAVORITE);
				if (removeFromFavoriteResponse.getCode() == SUCCESS) {
					// Utils.makeText(
					// this,
					// getResources().getString(R.string.favorite_removed,
					// mMediaItem.getTitle()), Toast.LENGTH_LONG)
					// .show();
					Utils.makeText(VideoActivity.this,
							removeFromFavoriteResponse.getMessage(),
							Toast.LENGTH_LONG).show();
					favButton.setCompoundDrawablesWithIntrinsicBounds(null,
							whiteHeart, null, null);
//					if (mMediaTrackDetails != null
//							&& mMediaTrackDetails.getNumOfFav() >= 0) {
//						favButton
//								.setText(Utils.roundTheCount(mMediaTrackDetails
//										.getNumOfFav()));
//					}
					favButton.setSelected(false);
					isFavoritePressed = true;
				} else {
					Utils.makeText(
							this,
							getResources().getString(
									R.string.favorite_error_removing,
									mMediaItem.getTitle()), Toast.LENGTH_LONG)
							.show();
				}
				favButton.setClickable(true);

			} else if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_BADGE_ALERT) {
				// if (playButton != null
				// && playButton.getTag(R.string.TAG_IS_REPEAT) != null
				// && ((Boolean) playButton.getTag(R.string.TAG_IS_REPEAT))) {
				// } else {
				// if ((!player1.isPlaying()) && !isCompleted) {
				// if (!isPAused) {
				// startPlaying();
				// isPAused = false;
				// setReapatTag(false);
				// playButton.setImageResource(R.drawable.ic_pause);
				// }
				// }
				// }
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	/**
	 * Get Failer response from API call with operationId, ErrorType and Error
	 * message
	 *
	 * @param operationId
	 * @param errorType
	 * @param errorMessage
	 */
	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_STREAMING_ADP) {
			Logger.i(TAG, "Failed loading video streaming");
			/*if(isCallForCasting){
				Utils.makeText(this,getString(R.string.unable_to_play_video),Toast.LENGTH_SHORT).show();
				return;
			}*/
			if(errorType==ErrorType.CONTENT_NOT_AVAILABLE){
				Utils.makeText(this,getString(R.string.unable_to_play_video),Toast.LENGTH_SHORT).show();
				touchStopPos=0;
//				mHandler.removeCallbacks(mUpdateTimeTask);
//				updateControllersVisibilityThread();
				if(timer!=null)
					timer.cancel();
				initializeComponents();
				loadNextVideoForWindowFocus();
			} else {
				MainActivity.internetConnectivityPopup1(
						new MainActivity.OnRetryClickListener() {
							@Override
							public void onRetryButtonClicked() {
								mHasLoaded = false;
								onResume();
							}
						}, VideoActivity.this);
			}
		} else if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
			Logger.i(TAG, "Failed loading video details");
			MainActivity.internetConnectivityPopup1(
					new MainActivity.OnRetryClickListener() {
						@Override
						public void onRetryButtonClicked() {
							mHasLoaded = false;
							onResume();
						}
					}, VideoActivity.this);
		} else if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_RELATED) {
			Logger.i(TAG, "Failed loading related videos");
            hideLoadingDialogNew();
		} else if (operationId == OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE) {
			Logger.i(TAG, "Failed loading add to favorites");
			favButton.setClickable(true);

		} else if (operationId == OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE) {
			Logger.i(TAG, "Failed loading remove from favorites");
			favButton.setClickable(true);
		}
		hideLoadingDialog();

	}

	/**
	 * Initialize Components
	 * */
	ImageButton fullScreenButton;

	/**
	 * Initialize components from layout
	 */
	public void initializeComponents() {

		videoView.setOnErrorListener(this);

		videoView.setOnPreparedListener(this);
		videoView.setOnCompletionListener(VideoActivity.this);
		if (isCapableForVideoPlayerStreamingInfo())
			videoView.setOnInfoListener(onInfoToPlayStateListener);
		playButton = (ImageButton) findViewById(R.id.button_video_player_play_pause);
		playButton.setOnClickListener(this);
		skipButton = (LanguageButton) findViewById(R.id.button_video_player_skip);
		skipButton.setPaintFlags(skipButton.getPaintFlags()
				| Paint.UNDERLINE_TEXT_FLAG);
		skipButton.setOnClickListener(this);
		skipButton.setVisibility(View.GONE);
		fullScreenButton = (ImageButton) findViewById(R.id.button_video_player_fullscreen);
		fullScreenButton.setOnClickListener(this);

		// Info Page
		infoPage = (RelativeLayout) findViewById(R.id.relativelayout_info_page);
		// infoPage.setOnClickListener(this);
		infoPageButton = (LanguageButton) findViewById(R.id.video_player_content_actions_bar_button_info);
		infoPageButton.setOnClickListener(this);
		infoAlbum = (LinearLayout) findViewById(R.id.textview_row_1_right);
		infoAlbum.setOnClickListener(this);
		infoLanguageCategory = (LinearLayout) findViewById(R.id.textview_row_2_right);
		infoLanguageCategory.setOnClickListener(this);
		infoMood = (LinearLayout) findViewById(R.id.textview_row_3_right);
		infoMood.setOnClickListener(this);
		infoGenre = (LinearLayout) findViewById(R.id.textview_row_4_right);
		infoGenre.setOnClickListener(this);
		infoMusic = (LinearLayout) findViewById(R.id.textview_row_5_right);
		infoMusic.setOnClickListener(this);
		infoSingers = (LinearLayout) findViewById(R.id.textview_row_6_right);
		infoSingers.setOnClickListener(this);
		infoCast = (LinearLayout) findViewById(R.id.textview_row_7_right);
		infoCast.setOnClickListener(this);
		infoLyrics = (LinearLayout) findViewById(R.id.textview_row_8_right);
		infoLyrics.setOnClickListener(this);

		shareButton = (LanguageButton) findViewById(R.id.video_player_content_actions_bar_button_share);
		shareButton.setOnClickListener(this);

		infoButton = (ImageView) includeHeaderView
				.findViewById(R.id.main_player_content_actions_bar_button_info);
		infoButton.setOnClickListener(this);
		mCountDownTimer = new CountDownTimer(PERIOD, 1000) {

			@Override
			public void onTick(long millisUntilFinished) {

			}

			@Override
			public void onFinish() {
				if (!isAdPlaying
						&& playButton != null
						&& (playButton.getVisibility() == View.VISIBLE || linearlayout_video_seekbar_land
								.getVisibility() == View.VISIBLE)) {
					setMediaControlVisibility(false);
				}
				cancelThread();
			}
		};

		videoView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				boolean needToShowPlayBtn = true;
				if (!needToEnableTouch())
					needToShowPlayBtn = false;

				if (isAdLoading && !isAdPlaying)
					return false;

				if (isAdPlaying) {
					Utils.performclickEvent(VideoActivity.this,
							placementVideoAd);
				} else if (videoControllersBar.getVisibility() == View.VISIBLE) {
					setMediaControlVisibility(false);
				} else {
					setMediaControlVisibility(true);
					updateControllersVisibilityThread();
				}
				if (!needToShowPlayBtn) {
					playButton.setVisibility(View.INVISIBLE);
				}
				return false;
			}
		});

		relatedVideoPageButton = (LanguageButton) findViewById(R.id.video_player_content_actions_bar_button_related);
		relatedVideoPageButton.setOnClickListener(this);

		albumPageButton = (LanguageButton) findViewById(R.id.video_player_content_actions_bar_button_album);
		albumPageButton.setVisibility(View.GONE);
		Button moreButton = (LanguageButton) findViewById(R.id.video_player_content_actions_bar_button_more);
		moreButton.setOnClickListener(this);
		ImageView more_header = (ImageView) findViewById(R.id.video_player_content_actions_bar_button_more_header);
		more_header.setOnClickListener(this);
		if (mHomeMediaTilesAdapter != null) {
			mHomeMediaTilesAdapter.notifyDataSetChanged();
		}

		// For upgrade dialog in landscape
		initializeUpgradeDialog();
		userCurrentPlanValidityDate = Utils
				.convertTimeStampToDate(mApplicationConfigurations
                        .getUserSubscriptionPlanDate());
	}

	// ======================================================
	// Video Controllers timer thread
	// ======================================================

	public void stopProgressBar() {
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	/**
	 * Update timer on SeekBar
	 * */
	public void updateProgressBar() {
		stopProgressBar();
		mHandler.postDelayed(mUpdateTimeTask, 1000);
	}

	/**
	 * Background Runnable thread for updating time (total and current time,
	 * time slider).
	 * */
	long totalDuration;
	private Runnable mUpdateTimeTask = new Runnable() {

		public void run() {
			try {
				totalDuration = player1.getDuration();
				/*if(totalDuration>0 && mMediaItem!=null && player1!=null && player1.isPlaying() && mSelectedMedia==null && !isAdPlaying) {
					mSelectedMedia = Utils.buildMediaInfo(mMediaItem.getTitle(), mMediaItem.getTitle(), mMediaItem.getTitle(),
							(int) totalDuration, video.getVideoUrl(), Utils.getVideoMimeType(), url, mMediaItem.getId());
				}*/
				long currentDuration = player1.getCurrentPosition();

				if (currentDuration > 60000 && isHasToCallBadgeApi) {
					isHasToCallBadgeApi = false;
					isBackFromBadgeApi = true;
					mDataManager.checkBadgesAlert(
							String.valueOf(mMediaItem.getId()), "video",
							"watch_video", VideoActivity.this);
				}
				String toalDuration = Utils.milliSecondsToTimer(totalDuration)
						+ "";
				String currentDurationString = Utils
						.milliSecondsToTimer(currentDuration) + "";
				totalDurationLabel.setText(toalDuration);
				currentDurationLabel.setText(currentDurationString);

				totalDurationLabelLand.setText(""
						+ Utils.milliSecondsToTimer(totalDuration));
				currentDurationLabelLand.setText(currentDurationString);

				int progress = (int) (Utils.getProgressPercentage(
						currentDuration, totalDuration));
				videoProgressBar.setProgress(progress);
				videoProgressBarLand.setProgress(progress);
				// int percent = player1.getBufferPercentage();
				// videoProgressBar.setSecondaryProgress(percent);
				// videoProgressBarLand.setSecondaryProgress(percent);
				if (!isCapableForVideoPlayerStreamingInfo()) {
					long currentPos = player1.getCurrentPosition();
					if (touchStopPos != 0 && touchStopPos + 50 > currentPos
							&& currentPos != player1.getDuration()) {
						if (pbVideo.getVisibility() != View.VISIBLE) {
//							pbVideo.setVisibility(View.VISIBLE);
							setMediaControlVisibility(false);
						}
					} else {
						if (pbVideo.getVisibility() == View.VISIBLE) {
							pbVideo.setVisibility(View.GONE);
						}
					}
				} else {
					if (player1.getCurrentPosition() != 0
							&& pbVideo.getVisibility() == View.VISIBLE) {
						pbVideo.setVisibility(View.GONE);
					}
				}
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}

			// Running this thread after 100 milliseconds
			mHandler.postDelayed(mUpdateTimeTask, 1000);
		}
	};

	/**
	 * Load banner ads which is displaying at bottom
	 *
	 * @param isLoaded
	 */
	private void loadAdBanner(boolean isLoaded) {
		VideoActivity.isLoaded = isLoaded;
		if (adView != null)
			adView.setVisibility(View.INVISIBLE);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		orientation = getWindowManager().getDefaultDisplay().getRotation();
		CampaignsManager mCampaignsManager = CampaignsManager.getInstance(this);

		Logger.i("Placemen",
				"playevent Video ad1 :: " + System.currentTimeMillis());
		if (orientation == Surface.ROTATION_90
				|| orientation == Surface.ROTATION_270) {
			if (isAdPlaying)
				return;
			placementVideo = mCampaignsManager
					.getPlacementOfType(PlacementType.VIDEO_LANDSCAPE_OVERLAY);
		} else {
			placementVideo = mCampaignsManager
					.getPlacementOfType(PlacementType.VIDEO_PORTRAIT_BANNER);
		}
		Logger.i("Placement", "1 Video ad :: " + System.currentTimeMillis());
		if (placementVideo != null) {
			// Logger.i("Placement",
			// "Video :: " + new Gson().toJson(placementVideo).toString());

			width = metrics.widthPixels;
			dpi = metrics.densityDpi;

			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			backgroundLink = Utils.getDisplayProfile(metrics, placementVideo);

			handler = new Handler() {
				@SuppressWarnings("deprecation")
				public void handleMessage(android.os.Message msg) {
					try {
						if (backgroundImage != null) {
							ImageView img_Ad = ((ImageView) findViewById(R.id.iv_video_player_advertisement));
							adView = (RelativeLayout) findViewById(R.id.ivVideoPlayerAd);
							backgroundImage = Utils.ResizeBitmap(
									getBaseContext(), dpi, width,
									backgroundImage);
							// ((ImageView)
							// findViewById(R.id.iv_video_player_advertisement))
							// .setImageDrawable(backgroundImage);
							adView.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {

									if (placementVideo != null) {
										boolean needDelay = false;
										if (getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
											needDelay = true;
											fullScreenButton.performClick();
										}
										String url = placementVideo.getActions().get(0).action;
										if (url != null) {
											Logger.d("xxxxx", ">>" + url);
											if (url.startsWith("http://ua://")
													|| url.startsWith("http://ua//")
													|| url.startsWith("ua://")) {
												Logger.i("ExoVideo ","ExoVideo Release Start:"+url);
												player1.releasePlayer();
												Logger.i("ExoVideo ","ExoVideo Release Done");
											}
										}
										//if(needDelay) {
											new Handler().postDelayed(new Runnable() {
												@Override
												public void run() {
													try {
														Utils.performclickEvent(
																getApplicationContext(),
																placementVideo);
													} catch (Exception e) {
														Logger.printStackTrace(e);
													}
												}
											},500);
										/*}else{
											try {
												Utils.performclickEvent(
														getApplicationContext(),
														placementVideo);
											} catch (Exception e) {
												Logger.printStackTrace(e);
											}
										}*/
									}
								}
							});

							adView.setVisibility(View.INVISIBLE);

							if (orientation == Surface.ROTATION_90
									|| orientation == Surface.ROTATION_270) {
								LayoutParams params = (LayoutParams) adView
										.getLayoutParams();
								params.setMargins(adView.getWidth() / 10,
										adView.getHeight() / 10,
										adView.getWidth() / 10, 0);
								adView.setLayoutParams(params);
							} else {
								LayoutParams params = (LayoutParams) adView
										.getLayoutParams();
								params.setMargins(0, 0, 0, 0);
								adView.setLayoutParams(params);
							}
							// adView.postDelayed(new Runnable() {
							// public void run() {
							try {
								Logger.i(
										"add event ",
										"playevent Video ad2 :: "
												+ System.currentTimeMillis());
								Utils.postViewEvent(getApplicationContext(),
										placementVideo);
								LayoutParams params = (LayoutParams) img_Ad
										.getLayoutParams();
								params.width = backgroundImage
										.getIntrinsicWidth();
								params.height = (width * backgroundImage
										.getIntrinsicHeight())
										/ backgroundImage.getIntrinsicWidth();

								Logger.i("add event ", "params width "
										+ params.width + ":params.height:"
										+ params.height);

								img_Ad.setLayoutParams(params);
								LayoutParams relParams = (LayoutParams) adView
										.getLayoutParams();
								relParams.height = params.height;
								Logger.i("add event ", "relParams width "
										+ relParams.width + ":params.height:"
										+ relParams.height);
								adView.setLayoutParams(relParams);
								Logger.i(
										"add event ",
										"backgroundImage width "
												+ backgroundImage
														.getIntrinsicWidth()
												+ ":params.height:"
												+ backgroundImage
														.getIntrinsicHeight());
								// img_Ad.setImageDrawable(null);
								img_Ad.setImageDrawable(backgroundImage);
								// img_Ad.setBackground(backgroundImage);

								img_Ad.postInvalidate();
								adView.setVisibility(View.VISIBLE);
							} catch (Exception e) {
								adView.setVisibility(View.INVISIBLE);
							}
							// }
							// }, 1000);
						} else {
							handler.sendEmptyMessage(0);
						}
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
				}
			};
			if (backgroundLink != null) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						if (backgroundImage == null || VideoActivity.isLoaded) {
							backgroundImage = null;
							backgroundImage = Utils.getBitmap(
									getApplicationContext(), width,
									backgroundLink);

						}
						if(handler!=null)
							handler.sendEmptyMessage(0);
					}
				}).start();
			}

		} else {
			RelativeLayout adView = (RelativeLayout) findViewById(R.id.ivVideoPlayerAd);
			LayoutParams params = (LayoutParams) adView.getLayoutParams();
			params.height = 0;
			adView.setLayoutParams(params);
		}

		placementVideoAd = mCampaignsManager
				.getPlacementOfType(PlacementType.VIDEO_AD);
		if (placementVideoAd != null) {
			Logger.s("Video ad :::::: " + placementVideoAd.get3gpVideo());
		}

		if(findViewById(R.id.bCloseVideoAd)!=null)
			findViewById(R.id.bCloseVideoAd).setOnClickListener(
					new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							findViewById(R.id.ivVideoPlayerAd).setVisibility(
									View.INVISIBLE);
						}
					});
	}

	/**
	 * Load Bottom Banner Ad for Info page
	 */
	private void loadInfoAdBanner() {
		CampaignsManager mCampaignsManager = CampaignsManager.getInstance(this);
		placementInfo = mCampaignsManager
				.getPlacementOfType(PlacementType.VIDEO_INFO);

		if (placementInfo != null) {
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			orientation = getWindowManager().getDefaultDisplay().getRotation();
			width = metrics.widthPixels;
			dpi = metrics.densityDpi;
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			backgroundLinkInfo = Utils
					.getDisplayProfile(metrics, placementInfo);

			if (backgroundLinkInfo != null) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						if (backgroundImageInfo == null
								|| VideoActivity.isLoaded) {
							backgroundImageInfo = Utils.getBitmap(
									getApplicationContext(), width,
									backgroundLinkInfo);

						}
						handler.sendEmptyMessage(0);
					}
				}).start();
			}
			handler = new Handler() {
				@SuppressWarnings("deprecation")
				public void handleMessage(android.os.Message msg) {
					try {
						if (backgroundImageInfo != null) {

							backgroundImageInfo = Utils.ResizeBitmap(
									getBaseContext(), dpi, width,
									backgroundImageInfo);

							final ImageView adView = (ImageView) findViewById(R.id.ivVideoInfoAd);

							((ImageView) findViewById(R.id.ivHungamaVideoInfo))
									.setVisibility(View.GONE);
							((ProgressBar) findViewById(R.id.pbHungamaVideoInfo))
									.setVisibility(View.GONE);
							// adView.setVisibility(View.VISIBLE);
							adView.setBackgroundDrawable(backgroundImageInfo);
							adView.setImageDrawable(null);
							adView.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									try {
										Utils.performclickEvent(
												getApplicationContext(),
												placementInfo);
									} catch (Exception e) {
										Logger.printStackTrace(e);
									}
								}
							});
							Utils.postViewEvent(getApplicationContext(),
									placementInfo);

							adView.postDelayed(new Runnable() {
								public void run() {
									try {
										// LayoutParams params = (LayoutParams)
										// findViewById(R.id.ivVideoInfoAd)
										// .getLayoutParams();
										// params.width =
										// backgroundImageInfo.getIntrinsicWidth();
										// params.height =
										// backgroundImageInfo.getIntrinsicHeight();
										// findViewById(R.id.ivVideoInfoAd).setLayoutParams(params);
										// adView.setVisibility(View.VISIBLE);
										LayoutParams params = (LayoutParams) adView
												.getLayoutParams();
										params.width = width;
										params.height = (backgroundImageInfo
												.getIntrinsicHeight() * width)
												/ backgroundImageInfo
														.getIntrinsicWidth();
										adView.setLayoutParams(params);
										adView.setVisibility(View.VISIBLE);
									} catch (Exception e) {
									} catch (Error e) {
									}
								}
							}, 100);
							// Log.i("Size!", "New width = " +
							// String.valueOf((new
							// BitmapDrawable(resized)).getIntrinsicWidth()));

						} else {
							((ImageView) findViewById(R.id.ivHungamaVideoInfo))
									.setVisibility(View.GONE);
							((ProgressBar) findViewById(R.id.pbHungamaVideoInfo))
									.setVisibility(View.GONE);
							handler.sendEmptyMessage(0);
						}
					} catch (Exception e) {
					} catch (Error e) {
					}
				}
			};
		} else {
			findViewById(R.id.rl_video_info_ad).setVisibility(View.GONE);
			((ImageView) findViewById(R.id.ivHungamaVideoInfo))
					.setVisibility(View.GONE);
			((ProgressBar) findViewById(R.id.pbHungamaVideoInfo))
					.setVisibility(View.GONE);
		}

	}

	/**
	 * Set up Layout controls after get response from server to play video
	 *
	 * @param video
	 */
	private void populateUserControls(Video video) {
		if(isFinishing())
			return;
		if (mMediaTrackDetails != null) {
			if(mMediaItem!=null || mMediaItem.getImages()==null) {
				mMediaItem.setImagesUrlArray(mMediaTrackDetails.getImages());
				loadBlurImgBitmap();
			}
			/*mSelectedMedia = Utils.buildMediaInfo(mMediaItem.getTitle(), mMediaItem.getTitle(), mMediaItem.getTitle(),
					0, video.getVideoUrl(), Utils.getVideoMimeType(), url, mMediaItem.getId(), VideoActivity.this, video.getDeliveryId());*/
			setTitleText(mMediaTrackDetails.getTitle(),
					mMediaTrackDetails.getAlbumName());
			Logger.s("Video Activity title" + mMediaTrackDetails.getTitle());
			Logger.s("Video Activity getAlbumName"
					+ mMediaTrackDetails.getAlbumName());

			// Set Comments Button
			commentsButton = (Button) findViewById(R.id.main_player_content_info_bar_button_comment);
			commentsButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						openCommentsPage(mMediaItem,
								mMediaTrackDetails.getNumOfComments());
					} catch (Exception e) {
						Logger.e(getClass().getName() + ":1101", e.toString());
					}
				}
			});
			if (mMediaTrackDetails != null) {
				// commentsButton.setText(String.valueOf(mMediaTrackDetails.getNumOfComments()));
			}

			// Set Favorite Button
			mediaType = MediaContentType.VIDEO.toString();
			favButton = (Button) findViewById(R.id.video_player_content_actions_bar_button_favorite);
			favButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (isAdPlaying || isAdLoading)
						return;
					if (mApplicationConfigurations.getSaveOfflineMode()) {
						displayOfflineDialog(VideoActivity.this);
						return;
					}
					favButton.setClickable(false);
					if (v.isSelected()) {
						mDataManager.removeFromFavorites(
								String.valueOf(mMediaItem.getId()), mediaType,
								VideoActivity.this);
					} else {
						mDataManager.addToFavorites(
								String.valueOf(mMediaItem.getId()), mediaType,
								VideoActivity.this);

						Appirater appirater = new Appirater(VideoActivity.this);
						appirater.userDidSignificantEvent(true);

						// Flurry report
						Map<String, String> reportMap = new HashMap<String, String>();

						reportMap.put(FlurryConstants.FlurryKeys.TitleContentID
								.toString(), mMediaItem.getTitle() + "_"
								+ mMediaItem.getId());
						reportMap.put(
								FlurryConstants.FlurryKeys.Type.toString(),
								MediaType.VIDEO.toString());
						reportMap.put(
								FlurryConstants.FlurryKeys.Source.toString(),
								FlurryConstants.FlurrySubSectionDescription.VideoDetail
										.toString());
						Analytics.logEvent(
								FlurryConstants.FlurryEventName.FavoriteButton
										.toString(), reportMap);
					}
				}
			});

			if (mMediaTrackDetails != null) {
				// favButton.setText(Utils.roundTheCount(mMediaTrackDetails
				// .getNumOfFav()));
				if (mMediaTrackDetails.IsFavorite()) {
					favButton.setCompoundDrawablesWithIntrinsicBounds(null,
							blueHeart, null, null);
					favButton.setSelected(true);
				} else {
					favButton.setCompoundDrawablesWithIntrinsicBounds(null,
							whiteHeart, null, null);
					favButton.setSelected(false);
				}

				RelativeLayout rlSaveOffline = (RelativeLayout) findViewById(R.id.video_player_content_actions_bar_rl_save_offline);

				rlSaveOffline.setVisibility(View.VISIBLE);
				startTitleHandler();
			}

			try {

				if (playButton.getTag(R.string.TAG_IS_REPEAT) != null
						&& (Boolean) playButton.getTag(R.string.TAG_IS_REPEAT)) {
					hideLoadingDialog();
					return;
				}

				if (!mIsSendToBackgroundPowerButtonPress) {
					// exo_contentType = DemoUtil.TYPE_HLS;
					if (video.getVideoUrl().startsWith("http")) {
						if (Utils.isNeedToUseHLS()) {
							//Log.d(TAG, "isApplicationWithCastConnected1:" + isApplicationWithCastConnected);
							if(/*isCastConnected() &&*/ HungamaApplication.isAppRequiredVideoCast){
								//if(!isCastRemotePlaying() && !isCastRemoteLoaded()) {
									try {
										Logger.d("Patibandha", "Old" +
												" condition ::::: iscastconnected & video play");
										//mSelectedMedia = buildMediaInfo(mMediaItem.getTitle(), mMediaItem.getTitle(), mMediaItem.getTitle(),
										//		(int) totalDuration, video.getVideoUrl(), "video/mp4"   /*"application/x-mpegurl"*/  , url);
										//loadRemoteMedia(0, true);
									} catch (Exception e) {
									}

							}else
								((ExoVideoPlayer) player1).exoPreparePlayer(
										video.getVideoUrl(), true);

						} else {
							((VideoPlayer) player1).videoView.setVideoURI(Uri
									.parse(video.getVideoUrl()));
						}
					} else {
						if (decryptedFile != null && decryptedFile.exists()) {
							LinearLayout llEndProgressBar = (LinearLayout) findViewById(R.id.llEndProgressBar);
							llEndProgressBar.setVisibility(View.GONE);
							if (timer != null)
								timer.cancel();
							if (Utils.isNeedToUseHLS()) {
								// exo_contentType = DemoUtil.TYPE_OTHER;
								((ExoVideoPlayer) player1).exoPreparePlayer(
										decryptedFile.getAbsolutePath(), true);
							} else {
								// needToShowExoPlayer(false);
								((VideoPlayer) player1).videoView
										.setVideoPath(decryptedFile
                                                .getAbsolutePath());
							}
						}
					}
				} else {
					if (Utils.isNeedToUseHLS()) {
						if (player1 != null && player1.isPlaying()) {
							onPause();
						}
					} else {
						mIsSendToBackgroundPowerButtonPress = false;

						if (player1 != null) {
							player1.seekToVideo(mCurrentPosition);
							// videoView.seekTo(mCurrentPosition);
							if (isAdPlaying && placementVideoAd.isSkipAllowed()) {
								skipButton.setVisibility(View.VISIBLE);
							} else {
								skipButton.setVisibility(View.GONE);
							}
						}
					}
				}

				// startPlaying();

			} catch (Exception e) {
				Logger.e(TAG, "failed loading video" + e.toString());
				hideLoadingDialog();
			}
		}
	}

	/**
	 * Open offline alert dialog when user is in offline mode and clicking on
	 * Favorite, info, Related, More options
	 *
	 * @param mContext
	 */
	private void displayOfflineDialog(final Context mContext) {
		final ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(mContext);
		if (mApplicationConfigurations.getSaveOfflineMode()) {
			CustomAlertDialog alertBuilder = new CustomAlertDialog(mContext);
			alertBuilder.setMessage(Utils.getMultilanguageTextHindi(
					mContext,
					getResources().getString(
							R.string.caching_text_message_go_online_player)));
			alertBuilder.setPositiveButton(Utils.getMultilanguageTextHindi(
					mContext,
					getResources().getString(
							R.string.caching_text_popup_title_go_online)),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							if (Utils.isConnected()) {
								mApplicationConfigurations
										.setSaveOfflineMode(false);

								Map<String, String> reportMap = new HashMap<String, String>();
								reportMap.put(
										FlurryConstants.FlurryCaching.Source
												.toString(),
										FlurryConstants.FlurryCaching.Prompt
												.toString());
								reportMap
										.put(FlurryConstants.FlurryCaching.UserStatus
                                                .toString(), Utils
                                                .getUserState(mContext));
								Analytics.logEvent(
										FlurryConstants.FlurryCaching.GoOnline
												.toString(), reportMap);

								// handlePlayClick(position,
								// viewHolder);
							} else {
								CustomAlertDialog alertBuilder = new CustomAlertDialog(
										mContext);
								alertBuilder.setMessage(Utils
										.getMultilanguageText(
                                                mContext,
                                                getResources()
                                                        .getString(
                                                                R.string.go_online_network_error)));
								alertBuilder.setNegativeButton(Utils
										.getMultilanguageText(mContext, "OK"),
										null);
								// alertBuilder.create();
								alertBuilder.show();
							}
						}
					});
			alertBuilder.setNegativeButton(Utils.getMultilanguageText(
					mContext,
					getResources().getString(
							R.string.caching_text_popup_button_cancel)), null);
			// alertBuilder.create();
			alertBuilder.show();
		}
	}

	boolean isOtherScreenOpen = false, needToResume = true;

	/**
	 * Open comment screen
	 *
	 * @param mediaItem
	 * @param numOfComments
	 */
	public void openCommentsPage(MediaItem mediaItem, int numOfComments) {
		if (isNextIndicatorLoaderDisplay())
			return;
		Bundle detailsDataTrack = new Bundle();
		detailsDataTrack.putSerializable(
				CommentsActivity.EXTRA_DATA_MEDIA_ITEM,
				(Serializable) mediaItem);
		detailsDataTrack.putBoolean(CommentsActivity.EXTRA_DATA_DO_SHOW_TITLE,
				true);
		detailsDataTrack.putString(VideoActivity.FLURRY_SOURCE_SECTION,
				FlurryConstants.FlurryComments.Video.toString());
		detailsDataTrack.putBoolean("is_video", true);

		Intent commentsIntent = new Intent(getApplicationContext(),
				CommentsActivity.class);
		commentsIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		commentsIntent.putExtras(detailsDataTrack);
		startActivity(commentsIntent);
		isOtherScreenOpen = true;
	}

	public void updateControllersVisibilityThread() {

		cancelThread();
		if (playButton != null
				&& playButton.getTag(R.string.TAG_IS_REPEAT) != null
				&& ((Boolean) playButton.getTag(R.string.TAG_IS_REPEAT)))
			return;
		if (mCountDownTimer != null) {
			mCountDownTimer.start();
		}
	}

	public void cancelThread() {
		if (mCountDownTimer != null) {
			mCountDownTimer.cancel();
		}
	}

	// ======================================================
	// Handle Orientation Methods
	// ======================================================

	/**
	 * Orientataion Listener.
	 * */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		loadAdBanner(true);

		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

			// getSupportActionBar().hide();

			updateViewToLandscape();
			if (infoPage != null) {
				if (infoPage.getVisibility() == View.VISIBLE) {
					infoPage.setVisibility(View.GONE);
					// onClick(playButton);
					if (infoWasClicked) {
						infoWasClicked = false;
					} else {
						infoWasClicked = true;
					}
				} else if (relatedVideoPage.getVisibility() == View.VISIBLE) {
					relatedVideoPage.setVisibility(View.GONE);
				}
			}
			updateControllersVisibilityThread();
			if(!needToHideNavBar()){
				this.getWindow().setFlags(
						WindowManager.LayoutParams.FLAG_FULLSCREEN,
						WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
		} else {
			// getSupportActionBar().show();
			updateViewToPortrait();
			updateControllersVisibilityThread();
			this.getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}

	/**
	 * Update UI when user go to Landscape mode
	 */
	private void updateViewToLandscape() {
		LinearLayout llTop = (LinearLayout) findViewById(R.id.main_player_content_info_bar);
		llTop.setVisibility(View.GONE);
		setSeekBarVisibility(true);

		LinearLayout llBottomTabs = (LinearLayout) findViewById(R.id.main_player_content_actions_full);
		llBottomTabs.setVisibility(View.GONE);

		LinearLayout llUpgradeBar = (LinearLayout) findViewById(R.id.linearlayout_upgrade_bar);
		llUpgradeBar.setVisibility(View.GONE);

		if (upgradeDialog != null) {
			upgradeDialog.dismiss();
		}

		View viewSeperator = (View) findViewById(R.id.bottom_tabs_seperator);
		viewSeperator.setVisibility(View.GONE);

		((RelativeLayout) findViewById(R.id.rl_landscape_title)).setVisibility(View.VISIBLE);


		VideoView videoView = (VideoView) findViewById(R.id.videoview_video_details);
		RelativeLayout videoLayout = (RelativeLayout) findViewById(R.id.relativeLayout_videoview);
		videoLayout.getLayoutParams().height = getScreenHeight();

		LayoutParams rlParams = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		rlParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		rlParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		rlParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		rlParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		rlParams.setMargins(0, 0, 0, 0);
		videoLayout.updateViewLayout(videoView, rlParams);
		setSkipButtonMarginBottom();
		setMediaControlVisibilityForAdsPlaying(true);
		changeExoPlayerResolution();
		showSystemUIForLandscape();
		UIupdateForVideo();
	}

	/**
	 * Get screen height based on Landscape and portrait mode
	 */
	public int getScreenHeight() {
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		int width = metrics.widthPixels;
		int tempHeight = metrics.heightPixels;
		int height;
		// if (tempHeight > width)
		// height = (width * 3) / 4;
		// else
		height = (width * 2) / 3;
		return height;
	}

	float videoRate;
	public void changeExoPlayerResolution(){
		if(isVideoCached)
			return;
		/*if(player1 instanceof ExoVideoPlayer){
			videoRate = (float) getScreenWidth()/ getScreenHeight();
			if(videoRate!=0 && videoRate!=1.0)
				(((ExoVideoPlayer)player1).exo_surfaceView).setVideoWidthHeightRatio(videoRate);
		}*/

	}

	public int getScreenWidth(){
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		return metrics.widthPixels;
	}

	/**
	 * Update UI when user go to Portrait mode
	 */
	private void updateViewToPortrait() {
		LinearLayout rlTop = (LinearLayout) findViewById(R.id.main_player_content_info_bar);
		rlTop.setVisibility(View.VISIBLE);
		setSeekBarVisibility(false);

		LinearLayout llBottomTabs = (LinearLayout) findViewById(R.id.main_player_content_actions_full);
		llBottomTabs.setVisibility(View.VISIBLE);

		((RelativeLayout) findViewById(R.id.rl_landscape_title)).setVisibility(View.GONE);

		LinearLayout llUpgradeBar = (LinearLayout) findViewById(R.id.linearlayout_upgrade_bar);
		today = new Date();
		if (userCurrentPlanValidityDate == null) {
			// For upgrade dialog in landscape
			userCurrentPlanValidityDate = Utils
					.convertTimeStampToDate(mApplicationConfigurations
                            .getUserSubscriptionPlanDate());
		}
		if (mApplicationConfigurations.isUserHasSubscriptionPlan()
				&& userCurrentPlanValidityDate != null
				&& !today.after(userCurrentPlanValidityDate)) {
			llUpgradeBar.setVisibility(View.GONE);
		} else {
			llUpgradeBar.setVisibility(View.VISIBLE);
		}
		if (upgradeDialog != null) {
			upgradeDialog.dismiss();
		}

		View viewSeperator = (View) findViewById(R.id.bottom_tabs_seperator);
		viewSeperator.setVisibility(View.VISIBLE);

		VideoView videoView = (VideoView) findViewById(R.id.videoview_video_details);
		RelativeLayout videoLayout = (RelativeLayout) findViewById(R.id.relativeLayout_videoview);
		videoLayout.getLayoutParams().height = getScreenHeight();
		LayoutParams rlParams = new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		rlParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		// rlParams.setMargins(0, 0, 0, Utils.convertDPtoPX(this, 105));
		videoLayout.updateViewLayout(videoView, rlParams);
		setSkipButtonMarginBottom();
		setMediaControlVisibilityForAdsPlaying(true);
		changeExoPlayerResolution();
		showSystemUIForLandscape();
		UIupdateForVideo();
	}

	private void hideSystemUIForLandscape() {
		if(needToHideNavBar()){
			int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
			boolean isImmersiveModeEnabled =
					((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
			if (isImmersiveModeEnabled || true) {
				getWindow().getDecorView().setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_LAYOUT_STABLE
								| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
								| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
								| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
								| View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
								| View.SYSTEM_UI_FLAG_IMMERSIVE);
		/*getWindow().getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
						| View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
						| View.SYSTEM_UI_FLAG_IMMERSIVE);*/
			}
			toggleHideyBar();
		}
	}

	private void showSystemUIForLandscape() {
		if(needToHideNavBar()){
			int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
			boolean isImmersiveModeEnabled =
					((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
			if (getScreenOrientation() == Configuration.ORIENTATION_PORTRAIT) {
				getWindow().getDecorView().setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_LAYOUT_STABLE
								| View.SYSTEM_UI_FLAG_VISIBLE // hide status bar
								| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
			}else if (getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
				getWindow().getDecorView().setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_LAYOUT_STABLE
								| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
								| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
								| View.SYSTEM_UI_FLAG_IMMERSIVE);
			}
			toggleHideyBar();
		}
	}

	public void toggleHideyBar() {

		int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
		int newUiOptions = uiOptions;
		boolean isImmersiveModeEnabled =
				((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
		if (isImmersiveModeEnabled) {
			Log.i(TAG, "Turning immersive mode mode off. ");
		} else {
			Log.i(TAG, "Turning immersive mode mode on.");
		}

		// Navigation bar hiding:  Backwards compatible to ICS.
		/*if (Build.VERSION.SDK_INT >= 14) {
			newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
		}

		// Status bar hiding: Backwards compatible to Jellybean
		if (Build.VERSION.SDK_INT >= 16) {
			newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
		}
		if (Build.VERSION.SDK_INT >= 18) {
			newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		}

		getWindow().getDecorView().setSystemUiVisibility(newUiOptions);*/
	}

	/**
	 * Set bottom margin programmatically when user switch from Landscape to
	 * Portrait or vice versa
	 */
	private void setSkipButtonMarginBottom() {
		if (skipButton == null)
			skipButton = (LanguageButton) findViewById(R.id.button_video_player_skip);
		LayoutParams params = (LayoutParams) skipButton
				.getLayoutParams();
		int marginBottom = 0;
		if (getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE)
			marginBottom = getResources().getDimensionPixelSize(
					R.dimen.video_skip_btn_margin_bottom_landscap);

		params.setMargins(0, 0, 0, marginBottom);
		skipButton.setLayoutParams(params);

	}

	// ======================================================
	// onClick Method
	// ======================================================

	boolean isPAused = false;
	boolean isPAusedByUser = false;

	@Override
	public void onClick(final View v) {
		Logger.d(TAG, "Simple click on: " + v.toString());
		int viewId = v.getId();
		if (viewId == R.id.ivBackArrow) {
			onBackPressed();
			return;
		}
		if (isNextIndicatorLoaderDisplay()) {
			Utils.makeText(VideoActivity.this,
					getString(R.string.main_player_bar_text_not_playing), 0)
					.show();
			return;
		}

		if (viewId == R.id.button_video_player_play_pause) {
			isCompleted = false;
			// for testing
			playButtonClickCounter++;
			Logger.i(TAG, "Play/Pause button was clicked: "
					+ playButtonClickCounter + " times");
			if (player1 != null
					&& playButton.getTag(R.string.TAG_IS_REPEAT) != null
					&& ((Boolean) playButton.getTag(R.string.TAG_IS_REPEAT))) {
				// videoView.seekTo(0);
				player1.seekToVideo(0);
				if (Utils.isNeedToUseHLS() && video != null)
					player1.startVideo(true);
				// videoView.start();
				isPAusedByUser = isPAused = false;
				playButton.setImageResource(R.drawable.ic_pause);
				updateProgressBar();
				setReapatTag(false);
                startVideoPlayEvent();
			} else if (player1.isPlaying()) {
				player1.pauseVideo();
				isPAusedByUser = isPAused = true;
				((ImageButton) v).setImageResource(R.drawable.ic_play); // Changing
			} else if (!player1.isPlaying()) {
				isPAusedByUser = isPAused = false;
				startPlaying();
				((ImageButton) v).setImageResource(R.drawable.ic_pause); // Changing
			}
			updateControllersVisibilityThread();
			// setDisplayTimer(videoControllersBar, 3000);

		} else if (viewId == R.id.button_video_player_fullscreen) {
			if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				setSeekBarVisibility(false);
			} else if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				setSeekBarVisibility(true);
			} else {
				if (getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					setSeekBarVisibility(false);
				} else {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					setSeekBarVisibility(true);
				}
			}
		} else if (viewId == R.id.video_player_content_actions_bar_button_info
				|| viewId == R.id.main_player_content_actions_bar_button_info) {
			if (isAdPlaying || isAdLoading)
				return;
			if (mApplicationConfigurations.getSaveOfflineMode()) {
				displayOfflineDialog(VideoActivity.this);
				return;
			}
			if ((player1.isPlaying()) && infoPage.getVisibility() == View.GONE) {
				isPAused = true;
				player1.pauseVideo();
				setReapatTag(false);
				playButton.setImageResource(R.drawable.ic_play);
			} else if ((!player1.isPlaying())
					&& infoPage.getVisibility() == View.VISIBLE && !isCompleted) {
				if (!isPAusedByUser) {
					isPAused = false;
					startPlaying();
					setReapatTag(false);
					playButton.setImageResource(R.drawable.ic_pause);
					if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
					}
				}
			}
			if (infoWasClicked) {
				infoWasClicked = false;
			} else {
				infoWasClicked = true;
			}

			if (mMediaTrackDetails != null) {
				closePage(relatedVideoPage, relatedVideoPageButton);
				// closePage(albumPage, albumPageButton);
				openInfoPage();
			} else {
				// showLoadingDialog(getResources().getString(R.string.application_dialog_loading_content));
			}

			// FlurryAgent.logEvent("Video details - info tab");

		} else if (viewId == R.id.video_player_content_actions_bar_button_related) {
			if (isAdPlaying || isAdLoading)
				return;
			if (mApplicationConfigurations.getSaveOfflineMode()) {
				displayOfflineDialog(VideoActivity.this);
				return;
			}
			if (playButton != null
					&& playButton.getTag(R.string.TAG_IS_REPEAT) != null
					&& ((Boolean) playButton.getTag(R.string.TAG_IS_REPEAT))) {
				if (mMediaTrackDetails != null) {
					closePage(infoPage, infoPageButton);
					// closePage(albumPage, albumPageButton);

					openRelatedVideoPage();
				}
			} else {
				if ((player1.isPlaying())
						&& relatedVideoPage.getVisibility() == View.GONE) {
					isPAused = true;
					player1.pauseVideo();
					setReapatTag(false);
					playButton.setImageResource(R.drawable.ic_play);
				} else if ((!player1.isPlaying())
						&& relatedVideoPage.getVisibility() == View.VISIBLE
						&& !isCompleted) {
					if (!isPAusedByUser) {
						startPlaying();
						isPAused = false;
						setReapatTag(false);
						playButton.setImageResource(R.drawable.ic_pause);
					}
				}
				if (mMediaTrackDetails != null) {
					closePage(infoPage, infoPageButton);
					// closePage(albumPage, albumPageButton);
					openRelatedVideoPage();
				}
			}
			// FlurryAgent.logEvent("Video details - related videos");

		} else if (viewId == R.id.video_player_content_actions_bar_button_more
				|| viewId == R.id.video_player_content_actions_bar_button_more_header) {
			if (isAdPlaying || isAdLoading)
				return;
			if (mApplicationConfigurations.getSaveOfflineMode()) {
				displayOfflineDialog(VideoActivity.this);
				return;
			}
			try {
				QuickActionVideoMore quickAction;
				quickAction = new QuickActionVideoMore(
						VideoActivity.this,
						QuickActionVideoMore.VERTICAL,
						(viewId == R.id.video_player_content_actions_bar_button_more_header) ? true
								: false);
				quickAction
						.setOnVideoPlayerMoreSelectedListener(VideoActivity.this);
				quickAction.show(v);
				v.setEnabled(false);
				quickAction
						.setOnDismissListener(new QuickActionVideoMore.OnDismissListener() {
                            @Override
                            public void onDismiss() {
                                v.setEnabled(true);
                            }
                        });
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}

		if (viewId == R.id.textview_row_1_right
				|| viewId == R.id.textview_row_2_right
				|| viewId == R.id.textview_row_3_right
				|| viewId == R.id.textview_row_4_right
				|| viewId == R.id.textview_row_5_right
				|| viewId == R.id.textview_row_6_right
				|| viewId == R.id.textview_row_7_right
				|| viewId == R.id.textview_row_8_right) {
			// openMainSearchFragment(((TextView) v).getText().toString());
			if (mApplicationConfigurations.getSaveOfflineMode()) {
				displayOfflineDialog(VideoActivity.this);
				return;
			}
			/*MainActivity.openMainSearch1(VideoActivity.this,
					((LanguageTextView) v).getText().toString(),
					FlurryConstants.FlurrySearch.Video.toString());*/
            openMainSearchFragment(((LanguageTextView) v).getText().toString(),
                    FlurryConstants.FlurrySearch.Video.toString());




		} else if (viewId == R.id.button_upgrade) {
			openUpgrade(v);

		} else if (viewId == R.id.cancel_button) {
			upgradeDialog.dismiss();

		} else if (viewId == R.id.video_player_content_actions_bar_button_share) {
			if (isAdPlaying || isAdLoading)
				return;
			openShareDialog();
		} else if (viewId == R.id.button_video_player_skip) {
			int playCurrentPostion = (int) (player1.getCurrentPosition() / 1000);
			Utils.postPlayEvent(getApplicationContext(), placementVideoAd,
					playCurrentPostion, false);
			// player1.releasePlayer();
			mIsSendToBackgroundPowerButtonPress = false;
			adCompletion(true);
		} else if (viewId == R.id.video_player_content_actions_bar_rl_save_offline) {
			if (isAdPlaying || isAdLoading)
				return;
			if (v.getTag() != null && !((Boolean) v.getTag())) {
				if ((player1.isPlaying()) && !CacheManager.isProUser(this)) {
					isPAused = true;
					player1.pauseVideo();
					setReapatTag(false);
					playButton.setImageResource(R.drawable.ic_play);
				}
				CacheManager.saveOfflineAction(this, mMediaItem, null);
				Utils.saveOfflineFlurryEvent(this,
						FlurryConstants.FlurryCaching.Video.toString(),
						mMediaItem);
			}
		} else if (viewId == R.id.ivDownArrowVideo) {
			if (infoPage.getVisibility() == View.VISIBLE) {
				if (infoPageButton != null)
					infoPageButton.performClick();
			}
		} else if (viewId == R.id.ivDownArrowVideo1) {
			if (relatedVideoPage.getVisibility() == View.VISIBLE) {
				if (relatedVideoPageButton != null)
					relatedVideoPageButton.performClick();
			}
		}
	}

	/**
	 * Open share intent dialog
	 */
	private void openShareDialog() {
		if (mDataManager.isDeviceOnLine()) {
			if ((player1.isPlaying()) && infoPage.getVisibility() == View.GONE) {
				player1.pauseVideo();
				isPAused = true;
				setReapatTag(false);
				playButton.setImageResource(R.drawable.ic_play); // Changing
			}
			// Prepare data for ShareDialogFragmnet
			Map<String, Object> shareData = new HashMap<String, Object>();
			shareData
					.put(ShareDialogFragment.TITLE_DATA, mMediaItem.getTitle());
			shareData.put(ShareDialogFragment.SUB_TITLE_DATA,
					mMediaItem.getAlbumName());
			shareData.put(ShareDialogFragment.THUMB_URL_DATA,
					mMediaItem.getBigImageUrl());
			shareData.put(ShareDialogFragment.MEDIA_TYPE_DATA, MediaType.VIDEO);
			shareData.put(ShareDialogFragment.CONTENT_ID_DATA,
					mMediaItem.getId());

			// Show ShareFragmentActivity
			ShareDialogFragment shareDialogFragment = ShareDialogFragment
					.newInstance(shareData,
                            FlurryConstants.FlurryShare.Video.toString());

			FragmentManager mFragmentManager = getSupportFragmentManager();
			shareDialogFragment.show(mFragmentManager,
					ShareDialogFragment.FRAGMENT_TAG);

			// shareDialogFragment.onDismiss(new DialogInterface() {
			//
			// @Override
			// public void dismiss() {
			// Logger.i("shareDialogFragment",
			// "shareDialogFragment: dismiss");
			// }
			//
			// @Override
			// public void cancel() {
			// Logger.i("shareDialogFragment",
			// "shareDialogFragment: Cancel");
			// }
			// });
			// isOtherScreenOpen = true;
			isOtherScreenOpen = true;
		} else {
			Utils.makeText(this,

			getResources().getString(R.string.player_error_no_connectivity),
					Toast.LENGTH_LONG).show();
		}
	}

	// ======================================================
	// Media players & SeekBar listeners
	// ======================================================
	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * android.media.MediaPlayer.OnErrorListener#onError(android.media.MediaPlayer
	 * , int, int)
	 */
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		hideLoadingDialog();

		// For testing
		Logger.e(TAG, "No connection error Toast was shown");
		Logger.e(TAG, "What : " + String.valueOf(what));
		Logger.e(TAG, "Extra : " + String.valueOf(extra));
		// isPAused = true;

		player1.pauseVideo();

		return true;
	}

	@Override
	public void onPrepared(MediaPlayer player) {
		isCompleted = false;
		this.player = player;
		//mPlaybackState = PlaybackState.PLAYING;
		loadBlurImgBitmap();
		totalDurationLabel.setText(""
				+ Utils.milliSecondsToTimer(player1.getDuration()));
		currentDurationLabel.setText(""
				+ Utils.milliSecondsToTimer(player1.getCurrentPosition()));

		totalDurationLabelLand.setText(""
				+ Utils.milliSecondsToTimer(player1.getDuration()));
		currentDurationLabelLand.setText(""
				+ Utils.milliSecondsToTimer(player1.getCurrentPosition()));
		// if (isActivityDestroyed()) {
		// return;
		// }
		hideLoadingDialog();
		// onClick(playButton);

		// onStopTrackingTouch(videoProgressBar);
		// onStopTrackingTouch(videoProgressBarLand);

		LinearLayout upgradeBar = (LinearLayout) findViewById(R.id.linearlayout_upgrade_bar);
		today = new Date();
		if (mApplicationConfigurations.isUserHasSubscriptionPlan()
				&& userCurrentPlanValidityDate != null
				&& !today.after(userCurrentPlanValidityDate)) {
			upgradeBar.setVisibility(View.GONE);
		} else {
			upgradeBar.setVisibility(View.VISIBLE);

			// Flurry report: User served a message to upgrade
			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(
					FlurryConstants.FlurrySubscription.SourcePage.toString(),
					FlurryConstants.FlurrySubscription.Video.toString());
			Analytics
					.logEvent(
                            FlurryConstants.FlurrySubscription.SubscriptionMessgaeServed
                                    .toString(), reportMap);
		}
		// updateControllersVisibilityThread();
		// mDataManager.getMediaDetails(mMediaItem, null, this);

		player.setOnBufferingUpdateListener(this);

		if (!isPAused)
			startPlaying();
		if (!isNextIndicatorLoaderDisplay()) {
			setMediaControlVisibility(true);
			updateControllersVisibilityThread();
		}
		// playBackCounter++;
		mApplicationConfigurations.increaseVideoPlayBackCounter();
		String[] images = ImagesManager.getImagesUrlArray(
				mMediaItem.getImagesUrlArray(), ImagesManager.HOME_VIDEO_TILE,
				DataManager.getDisplayDensityLabel());
		String url = "";
		if (images != null && images.length > 0) {
			url = images[0];
		}

	}

	private boolean isCompleted = false;

	/**
	 * onCompletion - It will call automatically when any video complete.
	 *
	 * @param mp
	 */
	int KEY_IS_REPEAT = 1;
	boolean isFromRelative = false;

	@Override
	public void onCompletion(MediaPlayer mp) {
		isVideoCached = false;
		isCompleted = true;
		mIsSendToBackgroundPowerButtonPress = false;
		Utils.clearCache(true);

        if (mediaItems != null) {
            mediaItems.clear();
			if(mHomeMediaTilesAdapter!=null)
            	mHomeMediaTilesAdapter.clearAndNotify();
        }

		if(isFromRelative){
			stopVideoPlayEvent(false, (int) player1.getCurrentPosition());
		}else {
			stopVideoPlayEvent(true, (int) player1.getDuration());
		}
		isFromRelative = false;
		mHandler.removeMessages(0);
		videoProgressBar.setProgress(0);
		videoProgressBar.setSecondaryProgress(0);
		videoProgressBarLand.setProgress(0);
		videoProgressBarLand.setSecondaryProgress(0);
		if (!isPAused && playButton!=null)
			playButton.setImageResource(R.drawable.ic_play);
		setReapatTag(false);
		stopProgressBar();
		// onStopTrackingTouch(videoProgressBar);
		// onStopTrackingTouch(videoProgressBarLand);
		// updateControllersVisibilityThread();
		// open related page

		boolean needToGoForNext = mApplicationConfigurations
				.isNextVideoAutoPlay();
		if (isRelatedFirstVideo) {
			needToGoForNext = true;
		}
		isRelatedFirstVideo = false;
		hasMoreVideosForRelated();
		if ((!needToGoForNext || isVideoPlayCompleted())) {
			playButton.setImageDrawable(getResources().getDrawable(
					R.drawable.ic_repeat));
			setReapatTag(true);
			cancelThread();
			setMediaControlVisibility(true);
			currentDurationLabelLand.setText("00:00");
			currentDurationLabel.setText("00:00");
		} else {
			if (decryptedFile != null && decryptedFile.exists())
				decryptedFile.delete();
			player1.releasePlayer();
		}
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// getResources().getConfiguration().orientation =
			// Configuration.ORIENTATION_PORTRAIT;
			today = new Date();
			if (!needToGoForNext || isVideoPlayCompleted()) {
				fullScreenButton.performClick();
			}
			if (needToGoForNext && isFromRelated && hasMoreVideosForRelated()) {
				startNextVideoTimer(mp);
			} else if (needToGoForNext && hasMoreVideos()) {
				startNextVideoTimer(mp);
			} else if (mApplicationConfigurations.isUserHasSubscriptionPlan()
					&& userCurrentPlanValidityDate != null
					&& !today.after(userCurrentPlanValidityDate)) {
				upgradeDialog.dismiss();
				getResources().getConfiguration().orientation = Configuration.ORIENTATION_PORTRAIT;
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				openRelatedVideoPage();
			} else {
				upgradeDialog.show();
			}
			// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			today = new Date();
			if (needToGoForNext && isFromRelated && hasMoreVideosForRelated()) {
				startNextVideoTimer(mp);
			} else if (needToGoForNext && hasMoreVideos()) {
				startNextVideoTimer(mp);
			} else if (mApplicationConfigurations.isUserHasSubscriptionPlan()
					&& userCurrentPlanValidityDate != null
					&& !today.after(userCurrentPlanValidityDate)) {
				openRelatedVideoPage();
			}
		}

	}

	private boolean hasMoreVideos() {
		return (videoList != null && videoList.size() > 0 && (videoList.size() - 1 > currentVideoPlayPos));
	}

	private boolean isVideoPlayCompleted() {
		return (videoList != null && videoList.size() > 0 && (videoList.size() - 1 == currentVideoPlayPos));
	}

	private boolean hasMoreVideosForRelated() {
		boolean result = (relatedList != null && relatedList.size() > 0 && (relatedList
				.size() - 1 > currentVideoPlayPosRelated));
		if (!result)
			isFromRelated = false;
		return result;
	}

	Timer timer;
	boolean isLoading = false;

	/**
	 * Start Loader for Next Video
	 *
	 * @param mp
	 *            Media Player
	 */
	private void startNextVideoTimer(final MediaPlayer mp) {
		setMediaControlVisibility(false);
		video = null;
		final LinearLayout llEndProgressBar = (LinearLayout) findViewById(R.id.llEndProgressBar);
		llEndProgressBar.setVisibility(View.VISIBLE);
		final DonutProgress donutProgress = (DonutProgress) findViewById(R.id.donut_progress);
		TextView tvCancel = (TextView) findViewById(R.id.tvCancel);
		tvCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				timer.cancel();
				llEndProgressBar.setVisibility(View.GONE);
				if (getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
					fullScreenButton.performClick();
				}
				// setMediaControlVisibility(true);
				playButton.setImageDrawable(getResources().getDrawable(
						R.drawable.ic_repeat));
				setReapatTag(true);
				cancelThread();
				setMediaControlVisibility(true);
				hideLoadingDialog();
			}
		});
		ImageView iv_play_next = (ImageView) findViewById(R.id.iv_play_next);
		iv_play_next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				llEndProgressBar.setVisibility(View.GONE);
				timer.cancel();
				if (video == null) {
					setMediaControlVisibility(false);
					Utils.makeText(VideoActivity.this, "Please wait...", 0)
							.show();
					showLoadingDialog("Please wait...");
					Utils.makeText(VideoActivity.this, "Please wait...", 0)
							.show();
				} else {
					Logger.i("currentProgress",
							"currentProgress: Reach at Timer");
					showLoadingDialog("Please wait...");
					setMediaControlVisibility(true);
					if (!needToEnableTouch())
						playButton.setVisibility(View.INVISIBLE);
					playButton.setImageResource(R.drawable.ic_pause);
					// videoView.start();
					player1.startVideo(true);
					Logger.i("currentProgress",
							"currentProgress: Start Called From Timer");
					updateControllersVisibilityThread();
				}

				// llEndProgressBar.setVisibility(View.GONE);
				// timer.cancel();
				// if (video != null) {
				// // videoView.start();
				// player1.startVideo(true);
				// showLoadingDialog("Please wait...");
				// setMediaControlVisibility(true);
				// updateControllersVisibilityThread();
				// if (!needToEnableTouch())
				// playButton.setVisibility(View.INVISIBLE);
				// } else {
				// setMediaControlVisibility(false);
				// showLoadingDialog("Please wait...");
				// Utils.makeText(VideoActivity.this, "Please wait...", 0)
				// .show();
				// }
			}
		});
		// donutProgress.setVisibility(View.VISIBLE);
		resetVideoAndLoadNext(mp, llEndProgressBar);
		donutProgress.setProgress(0);

		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try {
							int currentProgress = donutProgress.getProgress() + 1;
							donutProgress.setProgress(currentProgress);

							if (currentProgress == 100) {
								Logger.i("currentProgress",
										"currentProgress: Reach at Timer:"
												+ currentProgress);
								llEndProgressBar.setVisibility(View.GONE);
								timer.cancel();
								if (video == null) {
									setMediaControlVisibility(false);
									Utils.makeText(VideoActivity.this,
											"Please wait...", 0).show();
									showLoadingDialog("Please wait...");
								} else {
									if (mIsSendToBackgroundPowerButtonPress)
										return;
									Logger.i("currentProgress",
											"currentProgress: Reach at Timer");
									showLoadingDialog("Please wait...");
									setMediaControlVisibility(true);
									if (!needToEnableTouch())
										playButton
												.setVisibility(View.INVISIBLE);
									// videoView.start();
									playButton
											.setImageResource(R.drawable.ic_pause);
									player1.startVideo(true);
									Logger.i("currentProgress",
											"currentProgress: Start Called From Timer");
									updateControllersVisibilityThread();
								}

								// resetVideoAndLoadNext(mp, llEndProgressBar);
							}
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
					}
				});
			}
		}, 1000, 50);
	}

	/**
	 * MediaControl (like Play button, Seek bar, Maximize-Minimize button)
	 * based change based on requirement.
	 *
	 * @param needToVisible
	 */
	public void setMediaControlVisibility(boolean needToVisible) {
		try {
			if (!player1.isPlaying()) {
				playButton.setImageResource(R.drawable.ic_play);
			}
			int visibility;
			if (needToVisible) {
				visibility = View.VISIBLE;
				playButton.setVisibility(visibility);
				videoControllersBar.setVisibility(visibility);
				if (getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
					linearlayout_video_seekbar_land.setVisibility(visibility);
					((RelativeLayout) findViewById(R.id.rl_landscape_title)).setVisibility(visibility);
					showSystemUIForLandscape();
				}
				fullScreenButton.setVisibility(visibility);
				UIupdateForVideo();
			} else {
				visibility = View.GONE;
				playButton.setVisibility(visibility);
				videoControllersBar.setVisibility(View.INVISIBLE);
				if (getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
					linearlayout_video_seekbar_land.setVisibility(visibility);
					((RelativeLayout) findViewById(R.id.rl_landscape_title)).setVisibility(visibility);
					hideSystemUIForLandscape();
				}
				fullScreenButton.setVisibility(visibility);
				UIupdateForVideo();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * MediaControl visibility change based on screen orientation when Ad
	 * playing.
	 *
	 * @param needToVisible
	 */

	private void setMediaControlVisibilityForAdsPlaying(boolean needToVisible) {
		if (isAdPlaying) {
			if (getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
				linearlayout_video_seekbar_land.setVisibility(View.VISIBLE);
				videoControllersBar.setVisibility(View.INVISIBLE);
			} else {
				linearlayout_video_seekbar_land.setVisibility(View.GONE);
				videoControllersBar.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * Reset video player and start next video if auto play option is "ON"
	 *
	 * @param mp
	 * @param llEndProgressBar
	 */
	private void resetVideoAndLoadNext(MediaPlayer mp,
			LinearLayout llEndProgressBar) {
		if (!Utils.isNeedToUseHLS() && mp != null && videoView != null) {
			mp.setDisplay(null);
			mp.reset();
			mp.setDisplay(videoView.getHolder());
		}
		loadNextVideo();
		resetMediaTileAdapter();
		onResume();
	}

	private void loadNextVideo() {
		try {
			if (isFromRelated && hasMoreVideosForRelated()) {
				currentVideoPlayPosRelated = currentVideoPlayPosRelated + 1;
				mMediaItem = relatedList.get(currentVideoPlayPosRelated);
				String title = mMediaItem.getTitle().toString().trim();
				while (title != null && title.equals("no")) {
					currentVideoPlayPosRelated = currentVideoPlayPosRelated + 1;
					mMediaItem = relatedList.get(currentVideoPlayPosRelated);
					title = mMediaItem.getTitle().toString().trim();
				}
				tvUpNextName.setText(title);
				isHasToCallBadgeApi = true;
				isBackFromBadgeApi = false;
				isInfoPagePopulated = false;
				isFavoritePressed = false;
				mHasLoaded = false;
				isPAused = false;
			} else if (hasMoreVideos()) {
				currentVideoPlayPos = currentVideoPlayPos + 1;
				if (currentVideoPlayPos < 0)
					currentVideoPlayPos = 1;

				mMediaItem = videoList.get(currentVideoPlayPos);
				String title = mMediaItem.getTitle().toString().trim();
				while (title != null && title.equals("no")) {
					currentVideoPlayPos = currentVideoPlayPos + 1;
					mMediaItem = videoList.get(currentVideoPlayPos);
					title = mMediaItem.getTitle().toString().trim();
				}
				tvUpNextName.setText(title);
				isHasToCallBadgeApi = true;
				isBackFromBadgeApi = false;
				isInfoPagePopulated = false;
				isFavoritePressed = false;
				mHasLoaded = false;
				isPAused = false;
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		Logger.i("fromUser", "onProgressChanged:" + fromUser);
		try {
			if (fromUser) {
				updateControllersVisibilityThread();
				int totalDuration = player1.getDuration();
				int currentPosition = Utils.progressToTimer(
						seekBar.getProgress(), totalDuration);
				touchStopPos = 0;
				touchStopPos = currentPosition;
				if (!isCapableForVideoPlayerStreamingInfo())
					if (pbVideo.getVisibility() != View.VISIBLE) {
						pbVideo.setVisibility(View.VISIBLE);
						setMediaControlVisibility(false);
					}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Seek bar touch will start when user start Tracking, It will update if
	 * Video Ads not playing.
	 *
	 * @param seekBar
	 */
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		if (isAdPlaying)
			return;
		touchStopPos = 0;
		mHandler.removeCallbacks(mUpdateTimeTask);
		updateControllersVisibilityThread();
		// setDisplayTimer(videoControllersBar, 3000);

	}

	int touchStopPos;

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (isAdPlaying)
			return;

		mHandler.removeCallbacks(mUpdateTimeTask);

		long totalDuration = player1.getDuration();
		long currentPosition = Utils.progressToTimer(seekBar.getProgress(),
				totalDuration);
		Logger.i("onStopTrackingTouch", "onStopTrackingTouch: totalDuration"
				+ totalDuration + " :: currentPosition:" + currentPosition);
		try {
			// Forward or backward to certain seconds
			if (!mIsSendToBackgroundHomeButtonPress) {
				player1.seekToVideo(currentPosition);
				// player.seekTo(currentPosition);
			} else {
				mIsSendToBackgroundHomeButtonPress = false;
				player1.seekToVideo(mCurrentPosition);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		// Update timer progress again
		updateProgressBar();
		updateControllersVisibilityThread();
		touchStopPos = (int) currentPosition;
		if (!isCapableForVideoPlayerStreamingInfo())
			if (pbVideo.getVisibility() != View.VISIBLE) {
				pbVideo.setVisibility(View.VISIBLE);
				setMediaControlVisibility(false);
			}
	}

	private final OnInfoListener onInfoToPlayStateListener = new OnInfoListener() {

		@Override
		public boolean onInfo(MediaPlayer mp, int what, int extra) {
			if (MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START == what) {
				pbVideo.setVisibility(View.GONE);
			}
			if (MediaPlayer.MEDIA_INFO_BUFFERING_START == what) {
				if (!isNextIndicatorLoaderDisplay()) {
					pbVideo.setVisibility(View.VISIBLE);
					setMediaControlVisibility(false);
				}
			}
			if (MediaPlayer.MEDIA_INFO_BUFFERING_END == what) {
				pbVideo.setVisibility(View.GONE);
			}
			return false;
		}
	};

	/**
	 * Open Info page if user is in ONLINE mode
	 */
	private void openInfoPage() {
		// populateInfoPage();
		hideLoadingDialog();
		if (infoPage.getVisibility() == View.VISIBLE) {
			relatedVideoPage.setBackgroundColor(getResources().getColor(
					R.color.main_player_content_buttons_background_trans));
			infoPage.setVisibility(View.GONE);
		} else {
			if (blurbitmap != null) {
				int sdk = Build.VERSION.SDK_INT;
				if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
					infoPage.setBackgroundDrawable(blurbitmap);
				} else {
					infoPage.setBackground(blurbitmap);
				}
			}

			infoPage.setVisibility(View.VISIBLE);
			loadInfoAdBanner();
			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong.toString(),
					mMediaItem.getTitle());
			Analytics.logEvent(
					FlurryConstants.FlurryEventName.InfoTab.toString(),
					reportMap);

		}
	}

	/**
	 * Setup Info page
	 */
	private void populateInfoPage() {
		View seperator;
		if (mMediaTrackDetails != null) {
			if (!TextUtils.isEmpty(mMediaTrackDetails.getAlbumName())
					&& !TextUtils.isEmpty(mMediaTrackDetails.getReleaseYear())) {
				String albumAndYear = mMediaTrackDetails.getAlbumName() + " ("
						+ mMediaTrackDetails.getReleaseYear() + ")";
				setTextForTextViewButton(albumAndYear, infoAlbum);
				// infoAlbum.setText();
			} else {
				hideTableRow(infoAlbum);
				seperator = (View) findViewById(R.id.seperator_1);
				seperator.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(mMediaTrackDetails.getLanguage())) {
				String language = mMediaTrackDetails.getLanguage();
				setTextForTextViewButton(language, infoLanguageCategory);
				// infoLanguageCategory.setText(mMediaTrackDetails.getLanguage());
			} else {
				hideTableRow(infoLanguageCategory);
				seperator = (View) findViewById(R.id.seperator_2);
				seperator.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(mMediaTrackDetails.getMood())) {
				String mood = mMediaTrackDetails.getMood();
				setTextForTextViewButton(mood, infoMood);
				// infoMood.setText(mMediaTrackDetails.getMood());
			} else {
				hideTableRow(infoMood);
				seperator = (View) findViewById(R.id.seperator_3);
				seperator.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(mMediaTrackDetails.getGenre())) {
				String genre = mMediaTrackDetails.getGenre();
				setTextForTextViewButton(genre, infoGenre);
				// infoGenre.setText(mMediaTrackDetails.getGenre());
			} else {
				hideTableRow(infoGenre);
				seperator = (View) findViewById(R.id.seperator_4);
				seperator.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(mMediaTrackDetails.getMusicDirector())) {
				String musicDirector = mMediaTrackDetails.getMusicDirector();
				setTextForTextViewButton(musicDirector, infoMusic);
				// infoMusic.setText(mMediaTrackDetails.getMusicDirector());
			} else {
				hideTableRow(infoMusic);
				seperator = (View) findViewById(R.id.seperator_5);
				seperator.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(mMediaTrackDetails.getSingers())) {
				String singers = mMediaTrackDetails.getSingers();
				setTextForTextViewButton(singers, infoSingers);
			} else {
				hideTableRow(infoSingers);
				seperator = (View) findViewById(R.id.seperator_6);
				seperator.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(mMediaTrackDetails.getCast())) {
				String cast = mMediaTrackDetails.getCast();
				setTextForTextViewButton(cast, infoCast);
				// infoCast.setText(mMediaTrackDetails.getCast());
			} else {
				hideTableRow(infoCast);
				seperator = (View) findViewById(R.id.seperator_7);
				seperator.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(mMediaTrackDetails.getLyricist())) {
				String lyricist = mMediaTrackDetails.getLyricist();
				setTextForTextViewButton(lyricist, infoLyrics);
				// infoLyrics.setText(mMediaTrackDetails.getLyricist());
			} else {
				hideTableRow(infoLyrics);
				seperator = (View) findViewById(R.id.seperator_8);
				seperator.setVisibility(View.GONE);
			}
		}
	}

	private void setTextForTextViewButton(String text, LinearLayout row) {
		try {
			row.removeAllViewsInLayout();
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean isOneWord = true;
		LanguageTextView keywordButton = null;
		if (text.contains(",")) {
			String[] parts = text.split(",");
			int i = 0;
			for (final String keyword : parts) {
				boolean lastPosition = i == parts.length - 1 ? true : false;
				if (lastPosition) {
					keywordButton = createTextViewButtonInfo(keyword, isOneWord);
				} else {
					keywordButton = createTextViewButtonInfo(keyword,
							!isOneWord);
				}
				row.addView(keywordButton);
				i++;
			}
		} else {
			keywordButton = createTextViewButtonInfo(text, isOneWord);
			row.addView(keywordButton);
		}
	}

	private LanguageTextView createTextViewButtonInfo(final String keyword,
			boolean isOneWord) {
		LanguageTextView keywordButton = new LanguageTextView(this);
		keywordButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (keyword.contains("(")) {
					int startPosition = keyword.indexOf("(");
					int endPosition = keyword.indexOf(")");
					if (endPosition > startPosition) {
						String album = keyword.substring(0, startPosition);
						String year = keyword.substring(startPosition + 1,
								endPosition);
						if (TextUtils.isDigitsOnly(year)) {
							openMainSearchFragment(album,
									FlurryConstants.FlurrySearch.Video
											.toString());
						} else {
							openMainSearchFragment(keyword,
									FlurryConstants.FlurrySearch.Video
											.toString());
						}
					}
				} else {
					openMainSearchFragment(keyword,
							FlurryConstants.FlurrySearch.Video.toString());
				}
			}
		});
		if (isOneWord) {
			keywordButton.setText(keyword);
		} else {
			keywordButton.setText(keyword + ",");
		}
		// keywordButton.setTextAppearance(this, R.style.videoPlayeInfoRowText);
		// keywordButton.setTextAppearance(this,
		// R.style.playerBarFragmentItemTextColor_player_info);

		// keywordButton.setTypeface(null, Typeface.BOLD);
//		keywordButton.setTextAppearance(this,
//				R.style.playerBarFragmentItemTextColor);
//		keywordButton.setTextSize(getResources().getDimensionPixelSize(R.dimen.normal_text_size));
		keywordButton.setTextColor(getResources().getColorStateList(R.color.info_text_selector));
		keywordButton.setClickable(true);
		// } else{
		// keywordButton.setTextAppearance(includeHeaderView,
		// R.style.videoPlayeInfoRowLabelText);
		// }
		keywordButton.setTypeface(keywordButton.getTypeface(), Typeface.BOLD);
		keywordButton.setSingleLine(false);
		return keywordButton;
	}

	/**
	 * Open Related Video Page
	 */

	private void openRelatedVideoPage() {
		if (mApplicationConfigurations.getSaveOfflineMode())
			return;
		// populateInfoPage();
		// hideLoadingDialog();
		if (relatedVideoPage.getVisibility() == View.VISIBLE) {
			relatedVideoPage.setBackgroundColor(getResources().getColor(
					R.color.main_player_content_buttons_background_trans));
			relatedVideoPage.setVisibility(View.GONE);
		} else {
			if (blurbitmap != null) {
				int sdk = Build.VERSION.SDK_INT;
				if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
					relatedVideoPage.setBackgroundDrawable(blurbitmap);
				} else {
					relatedVideoPage.setBackground(blurbitmap);
				}
			}

            relatedVideoPage.setVisibility(View.VISIBLE);
            //resetMediaTileAdapter();
            if (mediaItems == null || (mediaItems!=null && mediaItems.size()==0)) {
                relatedVideoPage.post(new Runnable() {
                    @Override
                    public void run() {
                        mDataManager.getRelatedVideo(mMediaTrackDetails,mMediaItem, VideoActivity.this);
                    }
                }/*,500*/);
            }else{
                setRelativeAdapter();
            }
            // Flurry report
			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(FlurryConstants.FlurryFullPlayerParams.Type
					.toString(),
					FlurryConstants.FlurryFullPlayerParams.VideoPlayer
							.toString());
			Analytics.logEvent(
					FlurryConstants.FlurryEventName.RelatedVideos.toString(),
					reportMap);
		}
	}

	private void hideTableRow(View v) {
		LinearLayout tableRow = (LinearLayout) v.getParent();
		tableRow.setVisibility(View.GONE);
	}

	private void closePage(RelativeLayout page, Button button) {
		if (page.getVisibility() == View.VISIBLE) {
			page.setVisibility(View.GONE);
		}
	}

	/**
	 * This method will call when user open info page and click on any Lable
	 * like (Album name, Artist name etc.)
	 *
	 * @param videoQuery
	 * @param flurrySourceSection
	 */
	protected void openMainSearchFragment(String videoQuery,
			String flurrySourceSection) {
		/*MainActivity.openMainSearch1(VideoActivity.this, videoQuery,
				flurrySourceSection);
*/
        openMainSearchFragmentvideo(VideoActivity.this,
                videoQuery,
                flurrySourceSection);




	}

	public LinearLayout getVideoControllersBar() {
		return videoControllersBar;
	}

	public void setVideoControllersBar(LinearLayout videoControllersBar) {
		this.videoControllersBar = videoControllersBar;
	}

	/**
	 * Open Upgrade Dialog if Free user
	 */

	public void openUpgrade(View v) {
		final ApplicationConfigurations mApplicationConfiguration = ApplicationConfigurations
				.getInstance(this);
		if (mApplicationConfiguration.getSaveOfflineMode()) {
			CustomAlertDialog alertBuilder = new CustomAlertDialog(this);
			// alertBuilder.setTitle("Go Online");
			alertBuilder
					.setMessage(Utils
                            .getMultilanguageTextHindi(
                                    getApplicationContext(),
                                    getResources()
                                            .getString(
                                                    R.string.caching_text_message_go_online_global_menu)));
			alertBuilder.setPositiveButton(Utils.getMultilanguageTextHindi(
					getApplicationContext(),
					getResources().getString(
							R.string.caching_text_popup_title_go_online)),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							try {
								if(VideoActivity.this ==null || isFinishing()){
									return;
								}
								if (Utils.isConnected()) {
									mApplicationConfiguration
											.setSaveOfflineMode(false);
									Intent i = new Intent(
											MainActivity.ACTION_OFFLINE_MODE_CHANGED);
									i.putExtra(
											MainActivity.SELECTED_GLOBAL_MENU_ID,
											GlobalMenuFragment.MENU_ITEM_UPGRADE_ACTION);
									sendBroadcast(i);
								} else {
									CustomAlertDialog alertBuilder = new CustomAlertDialog(
											VideoActivity.this);
									alertBuilder.setMessage(Utils
											.getMultilanguageTextHindi(
                                                    getApplicationContext(),
                                                    getResources()
                                                            .getString(
                                                                    R.string.go_online_network_error)));
									alertBuilder.setNegativeButton(Utils
													.getMultilanguageTextHindi(
                                                            getApplicationContext(), "OK"),
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													startActivity(new Intent(
															android.provider.Settings.ACTION_SETTINGS));
												}
											});
									// alertBuilder.create();
									alertBuilder.show();
								}
							}catch (Exception e){

							}

						}
					});
			alertBuilder.setNegativeButton(Utils.getMultilanguageTextLayOut(
					getApplicationContext(),
					getResources().getString(
							R.string.caching_text_popup_button_cancel)),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							try {
								if(VideoActivity.this ==null || isFinishing()){
									return;
								}
								onResume();
							}catch (Exception e){
							}
						}
					});

			alertBuilder
					.setOnCancelListener(new DialogInterface.OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            onResume();
                        }
                    });
			// alertBuilder.create();
			alertBuilder.show();
			if (player1.isPlaying()) {
				player1.pauseVideo();
				isPausedForDownload = true;
				setReapatTag(false);
				playButton.setImageResource(R.drawable.ic_play);
			}
		} else {

			Intent intent = new Intent(this, UpgradeActivity.class);
			intent.putExtra(
					UpgradeActivity.EXTRA_DATA_ORIGIN_MEDIA_CONTENT_TYPE,
					(Serializable) MediaContentType.VIDEO);
			startActivityForResult(intent, UPGRADE_ACTIVITY_RESULT_CODE);

			// Flurry report: upgrade clicked
			Boolean loggedIn = mApplicationConfigurations.isRealUser();
			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(
					FlurryConstants.FlurrySubscription.SourcePage.toString(),
					FlurryConstants.FlurrySubscription.Video.toString());
			reportMap.put(
					FlurryConstants.FlurrySubscription.LoggedIn.toString(),
					loggedIn.toString());
			Analytics
					.logEvent(FlurryConstants.FlurrySubscription.TapsOnUpgrade
                            .toString(), reportMap);
		}
	}

	private boolean isPausedForDownload = false;

	/**
	 * Open Current Video Download screen
	 */

	public void startDownloadProcess(View v) {
		if (isAdPlaying || isAdLoading || isNextIndicatorLoaderDisplay())
			return;
		if (mApplicationConfigurations.getSaveOfflineMode()) {
			displayOfflineDialog(VideoActivity.this);
			return;
		}
		if (player1.isPlaying()) {
			player1.pauseVideo();
			isPausedForDownload = true;
			setReapatTag(false);
			playButton.setImageResource(R.drawable.ic_play);
		}
		isOtherScreenOpen = true;
		Intent intent = new Intent(this, DownloadConnectingActivity.class);
		MediaItem mediaItem = null;
		if(TextUtils.isEmpty(mMediaItem.getTitle()) && TextUtils.isEmpty(mMediaItem.getAlbumName())){
			mediaItem = new MediaItem(mMediaTrackDetails.getId(),
					mMediaTrackDetails.getTitle(), mMediaTrackDetails.getAlbumName(),
					"", ImagesManager
					.getMusicArtSmallImageUrl(mMediaTrackDetails
                            .getImagesUrlArray()),
					mMediaTrackDetails.getBigImageUrl(), MediaType.VIDEO
					.name().toLowerCase(), 0, mMediaTrackDetails.getNumOfFav(), mMediaTrackDetails
					.getImages(), mMediaTrackDetails.getAlbumId());
		}
		if(mediaItem!=null) {
			intent.putExtra(DownloadConnectingActivity.EXTRA_MEDIA_ITEM,
					(Serializable) mediaItem);
		}
		else {
			intent.putExtra(DownloadConnectingActivity.EXTRA_MEDIA_ITEM,
					(Serializable) mMediaItem);
		}


		startActivity(intent);

		Map<String, String> reportMap = new HashMap<String, String>();
		if(mediaItem!=null) {
			reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong.toString(),
					mediaItem.getTitle());
		}
		else {
			reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong.toString(),
					mMediaItem.getTitle());
		}

		reportMap.put(FlurryConstants.FlurryKeys.SourceSection.toString(),
				FlurryConstants.FlurryKeys.VideoDetail.toString());
		Analytics.logEvent(FlurryConstants.FlurryEventName.Download.toString(),
				reportMap);
	}

	/**
	 * Set up Upgrade Dialog
	 */
	public void initializeUpgradeDialog() {
		// set up custom dialog
		upgradeDialog = new Dialog(this);
		upgradeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		upgradeDialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(Color.TRANSPARENT));

		upgradeDialog.setContentView(R.layout.dialog_upgrade_subscription);
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(upgradeDialog.getWindow().getDecorView(),
					VideoActivity.this);
		}

		LanguageTextView title = (LanguageTextView) upgradeDialog
				.findViewById(R.id.video_upgrade_custom_dialog_title_text);
		title.setText(Utils.getMultilanguageTextLayOut(
				getApplicationContext(),
				getResources().getString(
						R.string.video_player_upgrade_button_text)
						.toUpperCase()));

		LanguageTextView text = (LanguageTextView) upgradeDialog
				.findViewById(R.id.video_upgrade_custom_dialog_text);
		text.setText(Utils.getMultilanguageTextLayOut(getApplicationContext(),
				getResources().getString(R.string.video_player_upgrade_text)));

		Button closeButton = (Button) upgradeDialog
				.findViewById(R.id.cancel_button);
		closeButton.setOnClickListener(this);

		LanguageButton upgradeButton = (LanguageButton) upgradeDialog
				.findViewById(R.id.button_upgrade);
		upgradeButton.setOnClickListener(this);
		upgradeDialog.setCancelable(true);
		upgradeDialog.setCanceledOnTouchOutside(true);
	}

	/**
	 * Start Video Playing
	 */
	public void startPlaying() {

		needToResume = true;
		if (player1 != null) {
			Logger.i("currentProgress",
					"currentProgress: Reach at StartPlaying");
			if (isActivityPaused)
				isVideoLoaded = true;
			else if (!isNextIndicatorLoaderDisplay()) {
				Logger.i("currentProgress",
						"currentProgress: Start Called StartPlaying");
				player1.startVideo();
			}
			isLoading = false;
			startVideoPlayEvent();
			if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			}
			setReapatTag(false);
			playButton.setImageResource(R.drawable.ic_pause);
		}
		updateProgressBar();
	}

	/**
	 * Check next video loader is running or not
	 *
	 * @return
	 */
	public boolean isNextIndicatorLoaderDisplay() {
        try{
            return ((LinearLayout) findViewById(R.id.llEndProgressBar))
                    .getVisibility() == View.VISIBLE;
        }catch (Exception e){
           return false;
        }

	}

	/**
	 * Check Network Bandwidth
	 */
	public int getNetworkBandwidth() {
		String networkType = Utils.getNetworkType(this);
		if (!TextUtils.isEmpty(networkType)) {
			long bandwidth = mApplicationConfigurations.getBandwidth();
			if (networkType.equalsIgnoreCase(Utils.NETWORK_WIFI)
					|| networkType.equalsIgnoreCase(Utils.NETWORK_3G)|| networkType.equalsIgnoreCase(Utils.NETWORK_4G)) {
				if (bandwidth == 0) {
					Logger.i(
							TAG,
							networkType
									+ " - First Time - 3G No bandwidth. bandwidth should be 192");
					return 192;
				} else {
					Logger.i(TAG, networkType + " - Bandwidth from previous = "
							+ bandwidth);
					return (int) bandwidth;
				}
			} else if (networkType.equalsIgnoreCase(Utils.NETWORK_2G)) {
				Logger.i(TAG, networkType + " - 2G - bandwidth should be 80");
				return 80;
			}
		}
		Logger.i(TAG, "Not WIFI & Not Mobile - bandwidth = 64");
		return 64; // Not WIFI & Not Mobile - bandwidth = 64
	}

	/**
	 * start Badges And Coins Screen
	 *
	 * @param badgesAndCoins
	 *            BadgesAndCoins
	 */
	private void startBadgesAndCoinsActivity(BadgesAndCoins badgesAndCoins) {
		Intent intent = new Intent(this, BadgesAndCoinsActivity.class);
		intent.putExtra(BadgesAndCoinsActivity.ARGUMENT_OBJECT,
				(Serializable) badgesAndCoins);
		// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivityForResult(intent, BADGES_ACTIVITY_RESULT_CODE);
	}

	// ======================================================
	// OnMediaItemOptionSelectedListener Callbacks
	// ======================================================

	@Override
	public void onMediaItemOptionPlayNowSelected(MediaItem mediaItem,
			int position) {
		Logger.d(TAG, "onMediaItemOption PlayNowSelected");
	}

	@Override
	public void onMediaItemOptionPlayNextSelected(MediaItem mediaItem,
			int position) {
		Logger.d(TAG, "onMediaItemOption PlayNextSelected");
	}

	@Override
	public void onMediaItemOptionAddToQueueSelected(MediaItem mediaItem,
			int position) {
		Logger.d(TAG, "onMediaItemOption AddToQueueSelected");
	}

	boolean needClose;

	/**
	 * start Badges And Coins Screen
	 *
	 * @param mediaItem
	 *            MediaItem
	 * @param position
	 *            Selected Item Pos
	 */
	boolean isFromRelated = false;
	boolean isRelatedFirstVideo = false;

	@Override
	public void onMediaItemOptionShowDetailsSelected(MediaItem mediaItem,
			int position) {
		if (mediaItem.getId() == mMediaItem.getId()) {
			Utils.makeText(VideoActivity.this,
					getString(R.string.txt_video_already_playing), 0).show();
			openRelatedVideoPage();
			if (!isPAusedByUser) {
				startPlaying();
				isPAused = false;
				setReapatTag(false);
				playButton.setImageResource(R.drawable.ic_pause);
			}
		} else {
			// Flurry report: Title of the current video when user selected
			// related
			// video
			Map<String, String> reportMap = new HashMap<String, String>();

			reportMap.put(
					FlurryConstants.FlurryKeys.CurrentVideoTitle.toString(),
					mMediaItem.getTitle());
			Analytics.logEvent(
					FlurryConstants.FlurryEventName.VideoDetail.toString(),
					reportMap);

			Logger.d(TAG, "onMediaItemOption ShowDetailsSelected");
			openRelatedVideoPage();
			//player1.releasePlayer();
			isRelatedFirstVideo = true;
			currentVideoPlayPosRelated = position - 1; // We are doing +1 in
			relatedList = new ArrayList<MediaItem>(mediaItems);
			isFromRelated = true;
			isFromRelative = true;
			onCompletion(null);
		}
	}

	@Override
	public void onMediaItemOptionRemoveSelected(MediaItem mediaItem,
			int position) {
		Logger.d(TAG, "onMediaItemOption RemoveSelected");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.saranyu.SaranyuVideo.OnProfileInfoListener#onInfo(int)
	 */
	@Override
	public void onInfo(int profileInfo) {
		String contentFormat = "";
		if (profileInfo == 0) {
			contentFormat = "high";
		} else if (profileInfo == 1)
			contentFormat = "baseline";
		mApplicationConfigurations.setContentFormat(contentFormat);
		if (!isVideoTrackCached()) {
			mDataManager.getVideoDetailsAdp(mMediaItem, networkSpeed,
					networkType, contentFormat, this, googleEmailId);
		}
		// loadMediaContentWithAd(contentFormat);
	}

	/**
	 * Get video content Length from Video URL
	 *
	 * @author hungama1
	 */
	private class GetVideoContentLengthAsync extends
			AsyncTask<Void, Void, Void> {

		private String mVideoUrl;

		public void setVideoUrl(String videoUrl) {
			this.mVideoUrl = videoUrl;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				URL url = new URL(mVideoUrl);
				if(Logger.enableOkHTTP){
					OkHttpClient client = CommunicationManager.getUnsafeOkHttpClient();
//					Request.Builder requestBuilder = new Request.Builder();
//					requestBuilder.url(url);
					Request.Builder requestBuilder = CommunicationManager.getRequestBuilder(VideoActivity.this, url);
					com.squareup.okhttp.Response responseOk = client.newCall(requestBuilder.build()).execute();
					mFileSize = responseOk.body().contentLength();
				} else {
					URLConnection urlConnection = url.openConnection();
					urlConnection.connect();
					mFileSize = urlConnection.getContentLength();
				}
				Logger.i(TAG, "File Size = " + mFileSize);

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {

		long bandwidth = 0;
		if (firstEntry) {
			firstEntry = false;
			startTimeToCalculateBitrate = System.currentTimeMillis();
			percentStart = percent;
			Logger.i(TAG, "Percent = " + percent + " Start Time = "
					+ startTimeToCalculateBitrate);
		} else if (percent == 100 && lastEntry) {
			lastEntry = false;
			endTimeToCalculateBitrate = System.currentTimeMillis();
			Logger.i(TAG, "Percent = " + percent + " End Time = "
					+ endTimeToCalculateBitrate);
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
	}

	// ======================================================
	// Logging PlayEvents.
	// ======================================================
	// Event logging fields.

	private String mEventStartTimestamp = null;

	public void startVideoPlayEvent() {
		Logger.i(TAG, "Start Video Play Event: " + mMediaItem.getId());
		DeviceConfigurations config = DeviceConfigurations.getInstance(this);
		mEventStartTimestamp = config.getTimeStampDelta();
	}

	/**
	 * Stop video Play Event
	 *
	 * @param hasCompletePlay
	 * @param currentPoisition
	 */
	private void stopVideoPlayEvent(boolean hasCompletePlay,
			int currentPoisition) {
		try {
			if (mEventStartTimestamp != null) {
				Map<String, String> reportMap = new HashMap<String, String>();
				reportMap.put(FlurryConstants.FlurryKeys.Title.toString(),
						mMediaItem.getTitle());
				Logger.i(TAG, "playCurrentPostion Duration: 1 "
						+ currentPoisition);
				reportMap.put(FlurryConstants.FlurryKeys.Duration.toString(),
						String.valueOf(currentPoisition));
				Analytics.logEvent(
						FlurryConstants.FlurryAllPlayer.VideoPlayed.toString(),
						reportMap);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

		Logger.i(TAG, "Stop Video Play Event: " + mMediaItem.getId());
		try {
			if (mEventStartTimestamp != null) {
				// If the VideoView is null them do not report play event.
				if (player1 != null) {
					// If the VideoView did not played then do not report
					if (currentPoisition > 0) {
						PlayingSourceType playingSourceType;

						// if(mApplicationConfigurations.getSaveOfflineMode()){
						// playingSourceType = PlayingSourceType.CACHED;
						// } else{
						if (CacheManager.isProUser(this) && DBOHandler
								.getVideoTrackCacheState(this, ""
                                        + mMediaItem.getId()) == CacheState.CACHED) {
							playingSourceType = PlayingSourceType.CACHED;
						} else {
							playingSourceType = PlayingSourceType.STREAM;
						}
						// }

						Logger.i(TAG,
								"Stop Video Play Event " + mMediaItem.getId());
						float playCurrentPostion = (float) (currentPoisition / 1000.0);
						// int playDuration = mDuration / 1000;
						int consumerId = mDataManager
								.getApplicationConfigurations().getConsumerID();
						String deviceId = mDataManager
								.getApplicationConfigurations().getDeviceID();

						if (hasCompletePlay) {
							// playCurrentPostion = mDuration / 1000;
						}
						int playDuration = (int) playCurrentPostion;
						long deli = 0;
						try {
							deli = video.getDeliveryId();
						} catch (Exception e) {
							// TODO: handle exception
						}

						PlayEvent playEvent = new PlayEvent(consumerId,
								deviceId, deli, hasCompletePlay /*
																 * ? "true" :
																 * "false"
																 */,
								playDuration, mEventStartTimestamp, 0, 0,
								mMediaItem.getId(), "video", playingSourceType,
								0, (int) playCurrentPostion);

						try {
							mDataManager.addEvent(playEvent);
							DBOHandler.insertMediaConsumptionDetail(this, playDuration, 1);

							if(playingSourceType == PlayingSourceType.CACHED) {
                                Logger.e(TAG, "Play Apsalar event posted");
								if(playDuration > 10) {
									ApsalarEvent.postEvent(getApplicationContext(),
											ApsalarEvent.VIDEO_PLAYED_OFFLINE, ApsalarEvent.TYPE_PLAY_OFFLINE);
									ApsalarEvent.postEvent(getApplicationContext(),
											ApsalarEvent.MEDIA_PLAYED);
								}
                                Set<String> tags = Utils.getTags();
                                String tag= Constants.UA_TAG_OFFLINE_VIDEOPLAYED;
                                if (!tags.contains(tag)) {
                                    tags.add(tag);
                                    Utils.AddTag(tags);
                                }
                            } else {
                                Logger.e(TAG, "Play Apsalar event posted");
								if(playDuration > 10) {
									ApsalarEvent.postEvent(getApplicationContext(),
											ApsalarEvent.VIDEO_PLAYED_ONLINE, ApsalarEvent.TYPE_PLAY_ONLINE);
									ApsalarEvent.postEvent(getApplicationContext(),
											ApsalarEvent.MEDIA_PLAYED);
								}
                            }
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
						Logger.e(TAG, "Play event posted");
					}
				}
				mEventStartTimestamp = null;
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	public boolean isAdPlaying = false;

	private boolean isActivityPaused = false;

	private boolean isVideoLoaded = false;

	public boolean isAdLoading = false;

	TextView mTitleBarTextVideo, mTitleBarTextVideoAlbum,mTitleBarTextVideoLandscape;
	private static int videoAdCount = 0;

	private boolean isAdPosition() {
		boolean isAdPos = false;
		try {
			// exo_contentType = DemoUtil.TYPE_HLS;
			// if (true)
			// return false;
			if (videoAdCount < mApplicationConfigurations
					.getAppConfigVideoAdSessionLimit()) {
				if (mApplicationConfigurations.getVideoPlayBackCounter() == 0) {
					// exo_contentType = DemoUtil.TYPE_OTHER;
					return true;
				}
				if (mApplicationConfigurations.getAppConfigVideoAdPlay() != 0) {
					if ((mApplicationConfigurations.getVideoPlayBackCounter())
							% (mApplicationConfigurations
									.getAppConfigVideoAdPlay()) == 0) {
						isAdPos = true;
						Logger.i("isAdPos","isAdPos:"+isAdPos);
						return true;
					}
					// }
				} else {
					isAdPos = (mApplicationConfigurations
							.getVideoPlayBackCounter() % 3 == 1);
					Logger.i("isAdPos","isAdPos:"+isAdPos);
					return isAdPos;
				}
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
			isAdPos = (mApplicationConfigurations.getVideoPlayBackCounter() % 3 == 1);
			Logger.i("isAdPos","isAdPos:"+isAdPos);
			return isAdPos;
		}
		Logger.i("isAdPos","isAdPos:"+false);
		return false;
	}

	/**
	 * Check for Ad position and Start ads if it's Ad position to play
	 */
	private void loadMediaContentWithAd() {
		if (placementVideoAd != null && isAdPosition()) {
			isAdLoading = true;
			displayTitleForAdvertise();
			setTitleText("Advertisement", "");

			if (relatedVideoPage.getVisibility() == View.VISIBLE) {
				relatedVideoPage.setVisibility(View.GONE);
			}
			// exo_surfaceView.setVisibility(View.GONE);
			// videoView.setVisibility(View.VISIBLE);
			showLoadingDialog(R.string.loading_);
			if (Utils.isNeedToUseHLS()) {
				((ExoVideoPlayer) player1).exoPreparePlayer(
						placementVideoAd.get3gpVideo(), true);
			} else {
				videoView
						.setVideoURI(Uri.parse(placementVideoAd.get3gpVideo()));
				videoView
						.setOnPreparedListener(new OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                player = mp;
                                prepareAd(mp);
                            }
                        });
				videoView
						.setOnCompletionListener(new OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                // adCompletion(false);
                                mIsSendToBackgroundPowerButtonPress = false;
                                adCompletion(false);
                            }
                        });
				videoView.setOnErrorListener(new OnErrorListener() {
					@Override
					public boolean onError(MediaPlayer mp, int what, int extra) {
						errorAdLoad();
						return true;
					}
				});
			}
		} else {
			populateUserControls(video);
			// mDataManager.getVideoDetailsAdp(mMediaItem, networkSpeed,
			// networkType, contentFormat, VideoActivity.this, googleEmailId);
		}
	}

	public void prepareAd(MediaPlayer mp) {
		videoAdCount++;
		isAdPlaying = true;
		isAdLoading = false;
		hideLoadingDialog();
		if (isActivityPaused)
			isVideoLoaded = true;
		else if (!isNextIndicatorLoaderDisplay())
			videoView.start();
		updateProgressBar();
		isLoading = false;
		setMediaControlVisibilityForAdsPlaying(true);
		cancelThread();
		playButton.setVisibility(View.GONE);
		if(placementVideoAd.isSkipAllowed())
			skipButton.setVisibility(View.VISIBLE);
		else
			skipButton.setVisibility(View.GONE);
	}
    boolean isPauseByFocusChange = false;
	public void errorAdLoad() {
        if(hasWindowFocus()) {
            isPauseByFocusChange = false;
            hideLoadingDialog();
            if (isAdLoading || isAdPlaying) {
                loadVideo();
            } else {
                Utils.makeText(VideoActivity.this,
                        "Sorry, we are unable to play Video.", 0).show();
                if (player1 instanceof VideoPlayer)
                    onCompletion(player);
                else
                    onCompletion(null);
            }
        }else{
            isPauseByFocusChange = true;
        }
		// if (Utils.isNeedToUseHLS()) {
		// exo_surfaceView.setVisibility(View.VISIBLE);
		// videoView.setVisibility(View.GONE);
		// }
	}

    private void loadNextVideoForWindowFocus(){
        if (player1 instanceof VideoPlayer)
            onCompletion(player);
        else
            onCompletion(null);
    }

	public void adCompletion(boolean isSkip) {
		//isAdPlaying = false;
		isAdLoading = false;
		// if (player1 instanceof ExoVideoPlayer)
		// ((ExoVideoPlayer) player1).exo_player = null;
		if (!isSkip) {
			int playCurrentPostion = player1.getDuration() / 1000;
			if (playCurrentPostion < 0)
				playCurrentPostion = (int) (player1.getCurrentPosition() / 1000);
			if (playCurrentPostion < 0)
				playCurrentPostion = 0;
			Utils.postPlayEvent(getApplicationContext(), placementVideoAd,
					playCurrentPostion, true);
		}
		if(player1 instanceof ExoVideoPlayer)
			((ExoVideoPlayer)player1).setDefaultCookieManager();
		stopProgressBar();
		player1.releasePlayer();
		loadVideo();
	}

	private void loadVideo() {
		isAdLoading = false;
		if ((player1 instanceof VideoPlayer && (player1.isPlaying() || isAdPlaying))) {
			videoView.pause();
			videoView.setOnErrorListener(VideoActivity.this);
			videoView.setOnPreparedListener(VideoActivity.this);
			videoView.setOnCompletionListener(VideoActivity.this);
		}

		if (isAdPlaying
				&& (orientation == Surface.ROTATION_90 || orientation == Surface.ROTATION_270)) {
			isAdPlaying = false;
			loadAdBanner(true);
		}


		isAdPlaying = false;
		showLoadingDialog(R.string.application_dialog_loading_content);
		populateUserControls(video);
		skipButton.setVisibility(View.GONE);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener
	 * #
	 * onMediaItemOptionSaveOfflineSelected(com.hungama.myplay.activity.data.dao
	 * .hungama.MediaItem, int)
	 */
	@Override
	public void onMediaItemOptionSaveOfflineSelected(MediaItem mediaItem,
			int position) {
		if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
			// new MediaCachingTask(this, mediaItem, null).execute();
			CacheManager.saveOfflineAction(this, mediaItem, null);
			Utils.saveOfflineFlurryEvent(this,
					FlurryConstants.FlurryCaching.Video.toString(), mediaItem);
		}
	}

	private File decryptedFile;
	public boolean isVideoCached = false;

	private boolean isVideoTrackCached() {
        boolean state = false;
		if(CacheManager.isProUser(this)) {
			final String video_url = DBOHandler.getVideoTrackPathById(this, ""
					+ mMediaItem.getId());
			if (video_url != null && video_url.length() > 0) {
				video = new Video(video_url, 0, MediaType.TRACK.toString()
						.toLowerCase(), 0, 0, 0, 0);
				state = true;
				try {
					// FileInputStream newFis = null;
					// Read file from cache
					File file = new File(video_url);
					if (file.exists()) {
						boolean decrypt = true;// mCurrentTrack.isEncrypted();
						FileInputStream fis = new FileInputStream(file);

						StringBuilder newPlayingPath = new StringBuilder(video_url);
						int dot = newPlayingPath.lastIndexOf(".");
						newPlayingPath.insert(dot, "crypt");

						decryptedFile = new File(newPlayingPath.toString());
						decryptedFile.deleteOnExit();
						FileOutputStream fos = new FileOutputStream(decryptedFile);// newPlayingPath.toString());
						CMEncryptor cmEncrpyt = new CMEncryptor("630358525");
						byte[] buffer = new byte[1024 * 100];

						int bytesRead = 0;
						// int deviceId = Integer.parseInt("630358525");
						// int _k = ((((int) deviceId) >> 24)
						// ^ (((int) deviceId) >> 16)
						// ^ (((int) deviceId) >> 8) ^ ((int) deviceId)) & 255;
						Logger.i("decrypt", "decrypt ::: Started");
						while ((bytesRead = fis.read(buffer)) != -1) {
							//if (decrypt) {
							cmEncrpyt.decrypt(buffer, 0, bytesRead);
							//}
							fos.write(buffer, 0, bytesRead);
						}
						Logger.i("decrypt", "decrypt ::: End");
						if (decrypt)
							Logger.i("END DECRYPT", mMediaItem.getTitle());
						else
							Logger.i("END READING", mMediaItem.getTitle());

						fos.close();
						fis.close();

						// Read file from cache
						// File newFile = new File(newPlayingPath.toString());
						// newFis = new FileInputStream(newFile);
						//
						// newFis.close();
						// newFile.deleteOnExit();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				VideoActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {

						onConfigurationChanged(getResources().getConfiguration());
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

						File file = new File(video_url);
						if (file.exists())
							mFileSize = file.length();
						initializeComponents();
						String videoTrackDetails = DBOHandler.getVideoTrackDetails(VideoActivity.this, ""
								+ mMediaItem.getId());
						if (videoTrackDetails != null && videoTrackDetails.length() > 0) {
							MediaDetailsOperation mediaDetailsOperation = new MediaDetailsOperation(
									"", "", "", mMediaItem, null, null);
							try {
								Response res = new Response();
								res.response = videoTrackDetails;
								res.responseCode = CommunicationManager.RESPONSE_SUCCESS_200;

								onSuccess(mediaDetailsOperation.getOperationId(),
										mediaDetailsOperation.parseResponse(res));
							} catch (InvalidRequestParametersException e) {
								e.printStackTrace();
							} catch (InvalidRequestTokenException e) {
								e.printStackTrace();
							} catch (InvalidResponseDataException e) {
								e.printStackTrace();
							} catch (OperationCancelledException e) {
								e.printStackTrace();
							}
						} else {
							mDataManager.getMediaDetails(mMediaItem, null, VideoActivity.this);
						}

					}
				});
			}
			isVideoCached = state;
			mHasLoaded = state;
		}
        return state;
    }

	/**
	 * CacheStateReceiver - It will receive message when any Song or video cache
	 * state update like (Cache complete, Cache started, In Queue)
	 *
	 * @author hungama1
	 */
	class CacheStateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			Logger.s("========================= cachestateupdatereceived ========"
					+ arg1.getAction());
			if (arg1.getAction().equals(
					CacheManager.ACTION_VIDEO_CACHE_STATE_UPDATED)
					|| arg1.getAction().equals(
							CacheManager.ACTION_VIDEO_TRACK_CACHED)) {
				refreshCacheState();
				if (relatedVideoPage.getVisibility() == View.VISIBLE) {
					RecyclerView mGrid = (RecyclerView) findViewById(R.id.video_related_gridview_tiles);
					if (mGrid != null && mGrid.getAdapter()!=null) {
						((MediaTilesAdapterVideo) mGrid.getAdapter())
								.notifyDataSetChanged();
					}
				}
			} else if (arg1.getAction().equals(
					CacheManager.ACTION_VIDEO_UPDATED_CACHE)) {
				refreshCacheState();
				if (relatedVideoPage.getVisibility() == View.VISIBLE) {
					RecyclerView mGrid = (RecyclerView) findViewById(R.id.video_related_gridview_tiles);
					if (mGrid != null && mGrid.getAdapter()!=null) {
						((MediaTilesAdapterVideo) mGrid.getAdapter())
								.notifyDataSetChanged();
					}
				}
			}
		}
	}

    class PrevScreenCloseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
           VideoActivity.this.finish();
        }
    }

	/**
	 * Refresh Cache State UI after receive message in CacheStateReceiver
	 */
	private void refreshCacheState() {
		RelativeLayout rlSaveOffline = (RelativeLayout) findViewById(R.id.video_player_content_actions_bar_rl_save_offline);
		if (mMediaItem != null) {
			CustomCacheStateProgressBar progressCacheState = (CustomCacheStateProgressBar) findViewById(R.id.video_player_content_actions_bar_progress_cache_state);
			LanguageTextView txtSaveOffline = (LanguageTextView) findViewById(R.id.video_player_content_actions_bar_text_cache_state);
			rlSaveOffline.setTag(false);
			progressCacheState.setNotCachedStateVisibility(true);
			if (CacheManager.isProUser(this)) {
				CacheState cacheState = DBOHandler.getVideoTrackCacheState(
						this, "" + mMediaItem.getId());
				if (cacheState == CacheState.CACHED) {
					rlSaveOffline.setTag(true);
					txtSaveOffline
							.setText(Utils
                                    .getMultilanguageText(
                                            getApplicationContext(),
                                            getString(R.string.caching_text_play_offline_capital)));
				} else if (cacheState == CacheState.CACHING
						|| cacheState == CacheState.QUEUED) {
					rlSaveOffline.setTag(null);
					txtSaveOffline.setText(Utils.getMultilanguageTextLayOut(
							getApplicationContext(),
							getString(R.string.caching_text_saving_capital)));
				}
				progressCacheState.setCacheState(cacheState);
				progressCacheState.setProgress(DBOHandler
						.getVideoTrackCacheProgress(this,
                                "" + mMediaItem.getId()));
				progressCacheState.setVisibility(View.VISIBLE);
			} else {
				progressCacheState.setCacheState(CacheState.NOT_CACHED);
				progressCacheState.setVisibility(View.VISIBLE);
			}
			rlSaveOffline.setOnClickListener(this);
		} else {
			rlSaveOffline.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		} else if (item.getItemId() == R.id.menu_item_main_actionbar_search) {
			if (item.isChecked()) {
				player1.pauseVideo();
				isPAused = true;
				setReapatTag(false);
				playButton.setImageResource(R.drawable.ic_play);
			} else {
				boolean result = false;
				result = !player1.isPlaying() && !isCompleted;
				if (result) {
					if (!isPAusedByUser) {
						isPAused = false;
						startPlaying();
						setReapatTag(false);
						playButton.setImageResource(R.drawable.ic_pause);
						if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
							setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
						}
					}
				}
			}
		}
		boolean result = super.onOptionsItemSelected(item);
		return result;
	}

	static String loadedURL = "";
	static Drawable blurbitmap;
	GetAndSetBlurImage getAndSetBlurImage;
	String bgImgUrl;
	String url;

	/**
	 * Load Blur image from server or local and set as background
	 *

	 */
	public void loadBlurImgBitmap() {
		String[] images = ImagesManager.getImagesUrlArray(
				mMediaItem.getImagesUrlArray(), ImagesManager.HOME_VIDEO_TILE,
				DataManager.getDisplayDensityLabel());
		String url = "";
		if (images != null && images.length > 0) {
			url = images[0];
		}
		if (!TextUtils.isEmpty(url)) {
			this.url = url;
			Display dis = getWindowManager().getDefaultDisplay();
			int orgWidth = dis.getWidth();
			int orgHeight = dis.getHeight();
			if (orgWidth > orgHeight) {
				PicassoUtil.with(VideoActivity.this).loadWithoutConfig8888(url,
						orgHeight, orgWidth, target);
			} else {
				PicassoUtil.with(VideoActivity.this).loadWithoutConfig8888(url,
						orgWidth, orgHeight, target);
			}
		}
	}

	PicassoTarget target = new PicassoTarget() {

		@Override
		public void onPrepareLoad(Drawable arg0) {
		}

		@Override
		public void onBitmapLoaded(Bitmap arg0, LoadedFrom arg1) {
			// Log.i("Bitmap", "BlurImgBitmap:" + arg0);
			getAndSetBlurImage = new GetAndSetBlurImage(arg0, url);
			ThreadPoolManager.getInstance().submit(					getAndSetBlurImage);
			bgImgUrl = url;
		}

		@Override
		public void onBitmapFailed(Drawable arg0) {
			bgImgUrl = null;
		}
	};

	private class GetAndSetBlurImage implements Runnable {
		Bitmap bitmap;

		String url;

		public GetAndSetBlurImage(Bitmap bitmap, String url) {
			this.bitmap = bitmap;
			this.url = url;
		}

		protected Drawable doInBackground(String... urls) {
			try {
				int oldBitmpWidth = bitmap.getWidth();
				Display dis = getWindowManager().getDefaultDisplay();

				int orgWidth = dis.getWidth();
				int orgHeight = dis.getHeight();
				float screenWidthRatio;
				if (orgWidth > orgHeight) {
					screenWidthRatio = (float) orgWidth / (float) orgHeight;
				} else {
					screenWidthRatio = (float) orgHeight / (float) orgWidth;
				}

				int newBitmpWidth = (int) (bitmap.getWidth() / screenWidthRatio);
				bitmap = Bitmap.createBitmap(bitmap,
						((oldBitmpWidth - newBitmpWidth) / 2), 0,
						newBitmpWidth, bitmap.getHeight());

				Bitmap loadBitmap = bitmap;
				try {
					loadBitmap = Utils.fastblur1(loadBitmap,
							Constants.BLUR_IMG_RADIUS, VideoActivity.this);
				} catch (OutOfMemoryError e) {
					System.gc();
					System.runFinalization();
					System.gc();
				} catch (Exception e) {
					loadBitmap = bitmap;
				}
				Bitmap displayBitmp = loadBitmap;
				Drawable dr = new BitmapDrawable(displayBitmp);
				return dr;
			} catch (Error e) {
			}
			return null;
		}

		@Override
		public void run() {
			try {
				final Drawable loadBitmap = doInBackground();
				if (loadBitmap != null)
					loadBlurBG(bitmap, loadBitmap, url);
				else {
					loadedURL = null;
					blurbitmap = null;
				}
			} catch (Exception e) {
			}
		}
	}

	void loadBlurBG(final Bitmap bitmap, final Drawable loadBitmap,
			final String url) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					RelativeLayout rlVideoMainScreen = (RelativeLayout) findViewById(R.id.relativelayout_activity_video);
					// rlFlipView.invalidate();
					if (Build.VERSION.SDK_INT > 15) {
						rlVideoMainScreen.setBackground(loadBitmap);
						// rlFlipView.invalidate();
					} else {
						rlVideoMainScreen.setBackgroundDrawable(loadBitmap);
					}
					if (blurbitmap != loadBitmap) {
						blurbitmap = loadBitmap;
						// normalBitmap = bitmap;
						loadedURL = url;

					}
					// if (mPlayerService != null)
					// mPlayerService.changeLockScreenBG(bitmap);
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			}
		});

	}

	protected void showLoadingDialog(int messageResource) {
		try {
			showLoadingDialog(Utils.getMultilanguageTextHindi(
					getApplicationContext(),
					getResources().getString(messageResource)));
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}
	}

	// private ProgressDialog mProgressDialog;

	public void showLoadingDialog(String message) {
		try {
			ProgressBar pbVideo = (ProgressBar) findViewById(R.id.pbVideo);
			pbVideo.setVisibility(View.VISIBLE);
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}
	}

	public boolean needToEnableTouch() {
		return ((((ProgressBar) findViewById(R.id.pbVideo)).getVisibility() != View.VISIBLE) && !isNextIndicatorLoaderDisplay());
	}

	public void hideLoadingDialog() {
		try {
			ProgressBar pbVideo = (ProgressBar) findViewById(R.id.pbVideo);
			pbVideo.setVisibility(View.GONE);
		} catch (Exception e) {
		}

		// if (mProgressDialog != null) {
		// mProgressDialog.dismiss();
		// mProgressDialog = null;
		// }
	}

	// -----------------------Title Flip Change-------//

	/**
	 * Title Bar flip code
	 */
	Handler commonHandler = new Handler();
	boolean isHandlerRunning = false;

	Runnable runTitle = new Runnable() {

		@Override
		public void run() {
			startTitleHandler();
			setTitleHandler();
		}
	};

	/**
	 * Remove Title Bar Flip Handler
	 */
	private void removeTitleHandler(boolean needToSetDefault) {
		commonHandler.removeCallbacks(runTitle);
		isHandlerRunning = false;
		if (needToSetDefault) {
			try {
				RelativeLayout rlExpandHandleFavorite = (RelativeLayout) includeHeaderView
						.findViewById(R.id.rlExpandHandleFavorite);
				RelativeLayout rlExpandHandleTitle = (RelativeLayout) includeHeaderView
						.findViewById(R.id.rlExpandHandleTitle);
				rlExpandHandleTitle.setVisibility(View.VISIBLE);
				rlExpandHandleFavorite.setVisibility(View.INVISIBLE);
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Start Title Handler to Flip
	 */
	private void startTitleHandler() {
		removeTitleHandler(false);
		isHandlerRunning = true;
		commonHandler.removeCallbacks(runTitle);
		commonHandler.postDelayed(runTitle, 6000);
	}

	/**
	 * Stop Title Flip and display "Advertise" text on Title when Ad Start to
	 * Play/Load
	 */
	private void displayTitleForAdvertise() {
		removeTitleHandler(true);
	}

	/**
	 * Set Title and Sub Title Text
	 *
	 * @param title
	 * @param subTitle
	 */
	private void setTitleText(String title, String subTitle) {
		mTitleBarTextVideo.setText(title);
		mTitleBarTextVideoLandscape.setText(title);
		if (isAdPlaying || (title!=null && title.equals("Advertisement"))) {
			mTitleBarTextVideo.setText("Advertisement");
			mTitleBarTextVideoAlbum.setText(subTitle);
			mTitleBarTextVideoAlbum.setVisibility(View.GONE);
		} else {
			mTitleBarTextVideoAlbum.setText(subTitle);
			mTitleBarTextVideoAlbum.setVisibility(View.VISIBLE);
		}
	}

	String txtPlays;

	private void setTitleHandler() {
		if (txtPlays == null)
			txtPlays = Utils
					.getMultilanguageText(VideoActivity.this, getResources()
                            .getString(R.string.media_details_no_of_play));
		try {
			RelativeLayout rlExpandHandleFavorite = (RelativeLayout) includeHeaderView
					.findViewById(R.id.rlExpandHandleFavorite);
			RelativeLayout rlExpandHandleTitle = (RelativeLayout) includeHeaderView
					.findViewById(R.id.rlExpandHandleTitle);
			Logger.i("setTitleHandler", "setTitleHandler called");
			if (rlExpandHandleTitle.getVisibility() == View.VISIBLE) {
				rlExpandHandleTitle.setVisibility(View.INVISIBLE);
				rlExpandHandleFavorite.setVisibility(View.VISIBLE);
				if (mMediaTrackDetails != null) {
					String totalPlayed = mMediaTrackDetails.getNumOfPlays()
							+ "";
					String totalFavorite = mMediaTrackDetails.getNumOfFav()
							+ "";
					TextView text_played = (TextView) includeHeaderView
							.findViewById(R.id.main_player_content_info_bar_text_played);
					TextView text_favorite = (TextView) includeHeaderView
							.findViewById(R.id.main_player_content_info_bar_text_favorite);
					Logger.i("value", "totalPlayed value:" + totalPlayed);
					if (totalFavorite.length() > 3) {
						totalFavorite = Utils.numberToStringConvert(
								totalFavorite, 0);
					}
					if (totalPlayed.length() > 3) {
						totalPlayed = Utils.numberToStringConvert(totalPlayed,
								0);
						Logger.i("value", "totalPlayed value new:"
								+ totalPlayed);
					}
//					totalPlayed = totalPlayed + " " + txtPlays;
					text_played.setText(totalPlayed);
					text_favorite.setText(totalFavorite + "");
					rlExpandHandleFavorite.invalidate();
					rlExpandHandleTitle.invalidate();
				}
			} else {
				rlExpandHandleTitle.setVisibility(View.VISIBLE);
				rlExpandHandleFavorite.setVisibility(View.INVISIBLE);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * Check for Video Streaming info method. It's only for
	 * Build.VERSION_CODES.JELLY_BEAN_MR1+ version
	 *
	 * @return
	 */
	private boolean isCapableForVideoPlayerStreamingInfo() {
		int currentapiVersion = Build.VERSION.SDK_INT;
		return (currentapiVersion >= Build.VERSION_CODES.JELLY_BEAN_MR1);
	}

	@Override
	public void onMediaItemOptionPlayAndOpenSelected(MediaItem mediaItem,
			int position) {
	}

	/**
	 * Quick action dialog Item Selection
	 *
	 * @param item
	 */
	@Override
	public void onItemSelected(String item) {
		String txtComments = VideoActivity.this
				.getString(R.string.video_player_setting_menu_comments);
		String txtShare = VideoActivity.this
				.getString(R.string.video_player_setting_menu_share);
		String txtDownloadMp4 = VideoActivity.this
				.getString(R.string.video_player_setting_menu_Download_mp4);
		String txtTrendThis = VideoActivity.this
				.getString(R.string.full_player_setting_menu_Trend_This);
		if (item.equals(txtDownloadMp4)) { // DownloadMp4
			startDownloadProcess(null);
		} else if (item.equals(txtComments)) { // Comments
			try {
				MediaItem mediaItem = null;
				if(TextUtils.isEmpty(mMediaItem.getTitle()) && TextUtils.isEmpty(mMediaItem.getAlbumName())){
					mediaItem = new MediaItem(mMediaTrackDetails.getId(),
							mMediaTrackDetails.getTitle(), mMediaTrackDetails.getAlbumName(),
							"", ImagesManager
							.getMusicArtSmallImageUrl(mMediaTrackDetails
                                    .getImagesUrlArray()),
							mMediaTrackDetails.getBigImageUrl(), MediaType.VIDEO
							.name().toLowerCase(), 0, mMediaTrackDetails.getNumOfFav(), mMediaTrackDetails
							.getImages(), mMediaTrackDetails.getAlbumId());
				}
				if(mediaItem!=null)
					openCommentsPage(mediaItem,
							mMediaTrackDetails.getNumOfComments());
				else
					openCommentsPage(mMediaItem,
							mMediaTrackDetails.getNumOfComments());

			} catch (Exception e) {
				Logger.printStackTrace(e);
			} catch (Error e) {
				Logger.printStackTrace(e);
			}
		} else if (item.equals(txtShare)) {
			openShareDialog();
		} else if (item.equals(txtTrendThis)) {

			Intent intent = new Intent(VideoActivity.this,
					TrendNowActivity.class);
			Bundle args = new Bundle();

			MediaItem mediaItem = null;
			if(TextUtils.isEmpty(mMediaItem.getTitle()) && TextUtils.isEmpty(mMediaItem.getAlbumName())){
				mediaItem = new MediaItem(mMediaTrackDetails.getId(),
						mMediaTrackDetails.getTitle(), mMediaTrackDetails.getAlbumName(),
						"", ImagesManager
						.getMusicArtSmallImageUrl(mMediaTrackDetails
                                .getImagesUrlArray()),
						mMediaTrackDetails.getBigImageUrl(), MediaType.VIDEO
						.name().toLowerCase(), 0, mMediaTrackDetails.getNumOfFav(), mMediaTrackDetails
						.getImages(), mMediaTrackDetails.getAlbumId());
			}
			if(mediaItem!=null)
				args.putSerializable(TrendNowActivity.EXTRA_DATA_MEDIA_ITEM,
						(Serializable) mediaItem);
			else
				args.putSerializable(TrendNowActivity.EXTRA_DATA_MEDIA_ITEM,
						(Serializable) mMediaItem);
			intent.putExtras(args);
			startActivity(intent);
		}
	}

	@Override
	public void onItemSelectedPosition(int id) {
		Logger.i("onItemSelectedPosition",
				"::::::::::::::::::::::::::::::: onItemSelectedPosition");
	}

	public void setReapatTag(boolean value) {
		playButton.setTag(R.string.TAG_IS_REPEAT, value);
	}

	BadgesScreenFinishReceiver badgesScreenFinishReceiver;

	class BadgesScreenFinishReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			Logger.s("========================= BadgesScreenFinishReceiver ========"
					+ arg1.getAction());
			isFromFavoriteScreen = true;
			if (playButton != null
					&& playButton.getTag(R.string.TAG_IS_REPEAT) != null
					&& ((Boolean) playButton.getTag(R.string.TAG_IS_REPEAT))) {
			} else {
				if ((!player1.isPlaying()) && !isCompleted) {
					// if (isBackFromBadgeApi) {
					pauseVideo = true;
					startPlaying();
					isPAused = false;
					setReapatTag(false);
					playButton.setImageResource(R.drawable.ic_pause);
					// }
				}
			}
		}
	}


    private MyProgressDialog mProgressDialog;

    public void showLoadingDialog1() {
        try {
            if (!isFinishing()) {
                if (mProgressDialog == null) {
                    mProgressDialog = new MyProgressDialog(this);
                    mProgressDialog.setCancelable(true);
                    mProgressDialog.setCanceledOnTouchOutside(false);
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

    }


    public  void openMainSearchFragmentvideo(Context context,String videoQuery,String flurrySourceSection) {
        Bundle arguments = new Bundle();
        arguments.putString(MainSearchResultsFragment.FRAGMENT_ARGUMENT_QUERY,
                videoQuery);
        arguments.putString(MainSearchResultsFragment.FRAGMENT_ARGUMENT_TYPE,
                "");
        arguments.putString(
                MainSearchResultsFragment.FLURRY_SEARCH_ACTION_SELECTED,
                mSearchActionSelected);
        arguments.putBoolean(MainSearchResultsFragment.FROM_FULL_PLAYER, true);
        Map<String, String> reportMap = new HashMap<String, String>();
        reportMap.put(FlurryConstants.FlurrySearch.SourceSection.toString(),
                flurrySourceSection);
        Analytics.logEvent(
                FlurryConstants.FlurrySearch.SearchButtonTapped.toString(),
                reportMap);
        Intent intent = new Intent(context, ActivityMainSearchResult.class);
        intent.putExtras(arguments);
        startActivity(intent);

    }

    public void hideLoadingDialogNew() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

	//-------------------CromeCast--------------//

	/*public enum PlaybackLocation {
		LOCAL,
		REMOTE
	}

	private PlaybackState mPlaybackState;
	public enum PlaybackState {
		PLAYING, PAUSED, BUFFERING, IDLE
	}
	private PlaybackLocation mLocation;
	private MediaInfo mSelectedMedia;
	MiniController mMini;
	protected MediaInfo mRemoteMediaInformation;
	private VideoCastConsumerImpl mCastConsumer;
	private boolean isApplicationWithCastConnected = false;


	private MediaRouter mMediaRouter;
	private MediaRouteSelector mMediaRouteSelector;
	private MediaRouter.Callback mMediaRouterCallback;
	private MediaRouteButton mMediaRouteButton;
	//private CastDevice mSelectedDevice;
	private int mRouteCount = 0;



	private void initializeCromCast(){
		VideoCastManager.checkGooglePlayServices(this);
		mCastManager = VideoCastManager.getInstance();
		mCastManager.reconnectSessionIfPossible();
		setupMiniController();
		setupCastListener();
	}

	private void setupMiniController() {
		*//*mMini = (MiniController) findViewById(R.id.miniController1);
		mCastManager.addMiniController(mMini);*//*



		Logger.d(TAG, "From OnCreate");
		mMediaRouter = MediaRouter.getInstance(getApplicationContext());
		// Create a MediaRouteSelector for the type of routes your app supports
		*//*mMediaRouteSelector = new MediaRouteSelector.Builder()
				.addControlCategory(
						CastMediaControlIntent.categoryForCast(getResources()
								.getString(R.string.crome_cast_app_id))).build();*//*

		mMediaRouteSelector = new MediaRouteSelector.Builder()
				// These are the framework-supported intents
				.addControlCategory(CATEGORY_LIVE_AUDIO)
				.addControlCategory(CATEGORY_LIVE_VIDEO)
				.addControlCategory(CATEGORY_REMOTE_PLAYBACK)
				.build();


		// Create a MediaRouter callback for discovery events
		mMediaRouterCallback = new MyMediaRouterCallback();

		// Set the MediaRouteButton selector for device discovery.
		mMediaRouteButton = (MediaRouteButton) findViewById(R.id.media_route_button);
		//mMediaRouteButton.setVisibility(View.VISIBLE);
		mMediaRouteButton.setRouteSelector(mMediaRouteSelector);


	}

	private void setupCastListener() {
		mCastConsumer = new VideoCastConsumerImpl() {
			@Override
			public void onConnected() {
				Log.d(TAG, "isApplicationWithCastConnected2:"+isApplicationWithCastConnected);
				super.onConnected();
			}

			@Override
			public void onApplicationConnected(ApplicationMetadata appMetadata,
											   String sessionId, boolean wasLaunched) {
				Log.d(TAG, "isApplicationWithCastConnected:"+isApplicationWithCastConnected);
				Logger.i(TAG, "Route is visible: VideoAct " + wasLaunched);
				isApplicationWithCastConnected = true;
				if (null != mSelectedMedia) {

						player1.pauseVideo();
						try {
							loadRemoteMedia(0, true);
						} catch (Exception e) {
						}
						return;

				}
				//updatePlayButton(mPlaybackState);
				invalidateOptionsMenu();
			}

			@Override
			public void onApplicationDisconnected(int errorCode) {
				isApplicationWithCastConnected = false;
				Log.d(TAG, "onApplicationDisconnected() is reached with errorCode: " + errorCode);
				updatePlaybackLocation(PlaybackLocation.LOCAL);
			}

			@Override
			public void onDisconnected() {
				Log.d(TAG, "onDisconnected() is reached");
				isApplicationWithCastConnected = false;
				mPlaybackState = PlaybackState.IDLE;
				mLocation = PlaybackLocation.LOCAL;
				//updatePlayButton(mPlaybackState);
				invalidateOptionsMenu();
			}

			@Override
			public void onRemoteMediaPlayerMetadataUpdated() {
				try {
					mRemoteMediaInformation = mCastManager.getRemoteMediaInformation();
				} catch (Exception e) {
					// silent
				}
			}

			@Override
			public void onFailed(int resourceId, int statusCode) {
				Log.i(TAG, "onFailed() is reached: statusCode:"+statusCode);
			}

			@Override
			public void onConnectionSuspended(int cause) {
				Utils.makeText(VideoActivity.this,"Connection Lost", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onConnectivityRecovered() {
				Utils.makeText(VideoActivity.this,"Connection Recovered", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onCastAvailabilityChanged(boolean castPresent) {
				//Utils.makeText(VideoActivity.this,"Cast Availability Changed:"+castPresent, Toast.LENGTH_SHORT).show();
				super.onCastAvailabilityChanged(castPresent);
			}

			@Override
			public void onCastDeviceDetected(MediaRouter.RouteInfo info) {
				super.onCastDeviceDetected(info);
				//Utils.makeText(VideoActivity.this,"Cast Device Detected", Toast.LENGTH_SHORT).show();


				Logger.d("MediaRouter", "From CastDevice detacted");
				// Add the callback to start device discovery
				mMediaRouteButton.setVisibility(View.VISIBLE);

				super.onCastDeviceDetected(info);


			}


		};

	}

	@Override
	public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
		if(mCastManager != null)
			return mCastManager.onDispatchVolumeKeyEvent(event, HungamaApplication.VOLUME_INCREMENT)
					|| super.dispatchKeyEvent(event);

	    return  super.dispatchKeyEvent(event);
	}

	boolean isCallForCasting = false;
	private void loadRemoteMedia(int position, boolean autoPlay) {
		if(mSelectedMedia==null){
			Utils.makeText(VideoActivity.this,"Video details not available",Toast.LENGTH_SHORT).show();
			isCallForCasting = false;
			return;
		}
		if((!isVideoCached || isCallForCasting) && !isAdLoading && !isAdPlaying){
			HomeActivity.Instance.loadedCount = currentVideoPlayPos;
			HomeActivity.Instance.mCurruntVideoPosition = currentVideoPlayPos;
			HomeActivity.Instance.tempMediaItemList = videoList;
			HomeActivity.Instance.isTileClick = true;
			mCastManager.startVideoCastControllerActivity(VideoActivity.this, mSelectedMedia, position, autoPlay);
			VideoActivity.this.finish();
			isCallForCasting = false;
		}else if(isVideoCached){
			isCallForCasting = true;
			String contentFormat = "high";
			mDataManager.getVideoDetailsAdp(mMediaItem,
					networkSpeed, networkType,
					contentFormat, this, googleEmailId);
		}
	}

	private void updatePlaybackLocation(PlaybackLocation location) {
		mLocation = location;
	}

	final Handler handlerCast = new Handler();
	private void onResumeCromeCast(){

		mCastManager.addVideoCastConsumer(mCastConsumer);
		mCastManager.incrementUiCounter();
		//mMini.setOnMiniControllerChangedListener(mCastManager);

		if (mCastManager.isConnected()) {
			updatePlaybackLocation(PlaybackLocation.REMOTE);
		} else {
			updatePlaybackLocation(PlaybackLocation.LOCAL);
		}

		mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
				MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);

	}


	private void onPauseCromeCast(){
		if (mLocation == PlaybackLocation.LOCAL) {
			mPlaybackState = PlaybackState.PAUSED;
		}

		// Remove the callback to stop device discovery
		mMediaRouter.removeCallback(mMediaRouterCallback);
		mCastManager.removeVideoCastConsumer(mCastConsumer);

		mCastManager.decrementUiCounter();
	}

	private void onDestroyCromeCast(){
		*//*if (null != mCastManager) {
		try {
				mCastManager.stop();
				mCastManager.disconnect();
			} catch (CastException e) {
				e.printStackTrace();
			} catch (TransientNetworkDisconnectionException e) {
			e.printStackTrace();
			} catch (NoConnectionException e) {
				e.printStackTrace();
			}
			*//**//*mMini.removeOnMiniControllerChangedListener(mCastManager);
			mCastManager.removeMiniController(mMini);*//**//*

		}*//*
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		//getMenuInflater().inflate(R.menu.player_crome_cast, menu);
		//mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
		//invalidateOptionsMenu();
		return true;
	}

	@Override
	public void invalidateOptionsMenu() {
		Logger.i("Parth", "invalidateOptionsMenu");
		super.invalidateOptionsMenu();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
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

	public boolean needToUseCastingPlayer() {
		return isCastConnected() && isCastRemoteLoaded() && HungamaApplication.isAppRequiredVideoCast;
	}



	private class MyMediaRouterCallback extends MediaRouter.Callback {
		@Override
		public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
			Log.d(TAG, "onRouteAdded");
			if (++mRouteCount == 1) {
				// Show the button when a device is discovered.
				mMediaRouteButton.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo route) {
			Log.d(TAG, "onRouteRemoved");
			if (--mRouteCount == 0) {
				// Hide the button if there are no devices discovered.
				mMediaRouteButton.setVisibility(View.GONE);
			}
		}

		@Override
		public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
			Log.d(TAG, "onRouteSelected");
			// Handle route selection.
			//mSelectedDevice = CastDevice.getFromBundle(info.getExtras());

			// Just display a message for now; In a real app this would be the
			// hook to connect to the selected device and launch the receiver
			// app
			*//*Toast.makeText(VideoActivity.this,
					getString(R.string.todo_connect), Toast.LENGTH_LONG).show();*//*
		}

		@Override
		public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
			Log.d(TAG, "onRouteUnselected: info=" + info);
			//mSelectedDevice = null;

		}

	}*/

	public int getStatusBarHeight() {
		if(!needToHideNavBar())
			return 0;
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	public int getNavigationBarHeight(){
		if(!hasNavBar(VideoActivity.this))
			return 0;
		int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
		if (resourceId > 0) {
			return getResources().getDimensionPixelSize(resourceId);
		}
		return 0;
	}


	private void UIupdateForVideo(){
		RelativeLayout rl_full_screen_btn = (RelativeLayout) findViewById(R.id.rl_full_screen_btn);
		if (getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
			rl_full_screen_btn.setPadding(0,statusBarHeight, navigationBarHeight, 0);
		}else{
			rl_full_screen_btn.setPadding(0,0,0,0);
		}
	}

	private boolean hasNavBar(Context context) {
		Resources resources = context.getResources();
		int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
		if (id > 0) {
			return resources.getBoolean(id);
		} else {    // Check for keys
			boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
			boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
			return !hasMenuKey && !hasBackKey;
		}
	}

	private boolean needToHideNavBar(){
		int sdk = Build.VERSION.SDK_INT;
		return sdk>17;
	}

}
