package com.hungama.myplay.activity.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.apsalar.sdk.Apsalar;
import com.bosch.myspin.serversdk.MySpinException;
import com.bosch.myspin.serversdk.MySpinServerSDK;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.gson.Gson;
import com.hungama.hungamamusic.ford.carmode.ProxyService;
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
import com.hungama.myplay.activity.data.ActionDefinition;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.DataBase.CacheState;
import com.hungama.myplay.activity.data.audiocaching.MediaCachingTaskNew;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.LeftMenuItem;
import com.hungama.myplay.activity.data.dao.hungama.LeftMenuResponse;
import com.hungama.myplay.activity.data.dao.hungama.LiveStation;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.NewVersionCheckResponse;
import com.hungama.myplay.activity.data.dao.hungama.PlayerOption;
import com.hungama.myplay.activity.data.dao.hungama.PromoUnit;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionStatusResponse;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.VersionCheckResponse;
import com.hungama.myplay.activity.gcm.IntentReceiver;
import com.hungama.myplay.activity.gigya.InviteFriendsActivity;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.GetPromoUnitOperation;
import com.hungama.myplay.activity.operations.hungama.LiveRadioDetailsOperation;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.operations.hungama.NewVersionCheckOperation;
import com.hungama.myplay.activity.operations.hungama.RadioTopArtistSongsOperation;
import com.hungama.myplay.activity.operations.hungama.SubscriptionCheckOperation;
import com.hungama.myplay.activity.operations.hungama.VersionCheckOperation;
import com.hungama.myplay.activity.player.PlayMode;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.player.PlayerServiceBindingManager;
import com.hungama.myplay.activity.player.PlayerUpdateWidgetService;
import com.hungama.myplay.activity.services.ReloadTracksDataService;
import com.hungama.myplay.activity.ui.adapters.MediaTilesAdapterVideo;
import com.hungama.myplay.activity.ui.adapters.MyAdapter;
import com.hungama.myplay.activity.ui.fragments.BackHandledFragment;
import com.hungama.myplay.activity.ui.fragments.BrowseRadioFragment;
import com.hungama.myplay.activity.ui.fragments.FavoritesFragment;
import com.hungama.myplay.activity.ui.fragments.GlobalMenuFragment;
import com.hungama.myplay.activity.ui.fragments.GlobalMenuFragment.Category;
import com.hungama.myplay.activity.ui.fragments.HomeMediaTileGridFragmentNew;
import com.hungama.myplay.activity.ui.fragments.HomeMediaTileGridFragmentVideo;
import com.hungama.myplay.activity.ui.fragments.ItemableTilesFragment;
import com.hungama.myplay.activity.ui.fragments.MainSearchFragmentNew;
import com.hungama.myplay.activity.ui.fragments.MainSearchResultsFragment;
import com.hungama.myplay.activity.ui.fragments.MainSearchResultsFragment.OnSearchResultsOptionSelectedListener;
import com.hungama.myplay.activity.ui.fragments.MediaDetailsFragment;
import com.hungama.myplay.activity.ui.fragments.MediaTileGridFragment;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment.TrackReloadReceiver;
import com.hungama.myplay.activity.ui.fragments.RedeemFragment;
import com.hungama.myplay.activity.ui.fragments.RootFragmentDiscovery;
import com.hungama.myplay.activity.ui.fragments.SocialMyStreamFragment;
import com.hungama.myplay.activity.ui.fragments.social.BadgesFragment;
import com.hungama.myplay.activity.ui.fragments.social.LeaderboardFragment;
import com.hungama.myplay.activity.ui.inappprompts.AppPromptCategoryPrefSelectionGeneric;
import com.hungama.myplay.activity.ui.inappprompts.AppPromptOfflineCaching3rdSong;
import com.hungama.myplay.activity.ui.inappprompts.AppPromptRegistrationSignIn;
import com.hungama.myplay.activity.ui.inappprompts.AppPromptSocialNative2;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.ui.widgets.CustomAlertDialog;
import com.hungama.myplay.activity.ui.widgets.HomeTabBar;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.ApsalarEvent;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.hungama.myplay.activity.util.ScreenLockStatus;
import com.hungama.myplay.activity.util.ToastExpander;
import com.hungama.myplay.activity.util.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.urbanairship.UAirship;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author hungama2
 *
 */

public class HomeActivity extends MainActivity implements
        MySpinServerSDK.ConnectionStateListener,
		ServiceConnection,
        CommunicationOperationListener, OnMediaItemOptionSelectedListener,
        OnShowcaseEventListener, OnSearchResultsOptionSelectedListener, BackHandledFragment.BackHandlerInterface {
    //response:******
    private static final String TAG = "HomeActivity";

	public static final String ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE = "activity_extra_media_content_type";
	public static final String ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION = "activity_extra_default_opened_tab_position";
	public static final String ACTIVITY_EXTRA_MEDIA_CATEGORY_TYPE = "activity_extra_default_media_category_type";
	public static final String ACTIVITY_EXTRA_OPEN_BROWSE_BY = "activity_extra_open_browse_by";
	public static final String ACTION_CLOSE_APP = "action_close_app";

	public static final String ARGUMENT_HOME_ACTIVITY = "argument_home_activity";

	public static final int MY_PREFERENCES_ACTIVITY_RESULT_CODE = 101;
	private static final int ACTIVITY_GUIDE_RESULT_CODE = 102;
	private static final int FILTER_OPTIONS_ACTIVITY_RESULT_CODE = 103;
	public static final int UPGRADE_ACTIVITY_RESULT_CODE = 1001;
	public static final int LOGIN_ACTIVITY_CODE = 1002;
	private static final int HELP_ACTIVITY_CODE = 1003;

	public static final String EXTRA_MY_PREFERENCES_IS_CHANGED = "my_preferences_is_changed";

	private String mFlurrySubSectionDescription;

	private CacheStateReceiver cacheStateReceiver;

	private OfflineModeReceiver offlineModeReceiver;

	private LanguageChangeReceiver languageChangeReceiver;
	private CloseAppReceiver closeAppReceiver;
	private TrackReloadReceiver mTrackReloadReceiver;

    private BackHandledFragment selectedFragment;

	public static HomeActivity Instance = null;
    private PlayerServiceBindingManager.ServiceToken mServiceToken = null;
    @Override
    public void setSelectedFragment(BackHandledFragment selectedFragment) {
        this.selectedFragment = selectedFragment;
    }

	// ======================================================
	// Public methods.
	// ======================================================

	// view pager
	private final Handler handler = new Handler(Looper.getMainLooper());
	private PagerSlidingTabStrip tabs;

	private ViewPager pager;
	public MyPagerAdapter adapter;

	@Override
	public void onConnectionStateChanged(boolean b) {
		Logger.e(TAG, " ::::::::::::::::::>> onConnectionStateChanged");
		updateUI();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {

		Logger.v(TAG, "HomeActivity-----onServiceConnected");
		PlayerService.PlayerSericeBinder binder = (PlayerService.PlayerSericeBinder) service;
		binder.getService();

	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		Logger.d(TAG, "Player bar disconnected from service.");

	}


	private void updateUI() {
		if (Utils.isCarMode()) {
			Logger.e(TAG, " ::::::::::::::::::>> updateUI CarMode");
//            startActivity(new Intent(this, CarModeHomeActivity.class));

			findViewById(R.id.iv_bg_car_splash).setVisibility(View.VISIBLE);

			Intent i = new Intent(HomeActivity.this, HomeActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.putExtra("car_mode", true);
			startActivity(i);

//            if (curViewId != R.layout.carmode_activity_home) {
//                setContentView(R.layout.carmode_activity_home);
//                curViewId = R.layout.carmode_activity_home;
//            }
//
//            if (rlCarmode == null) {
//                rlCarmode = (RelativeLayout) findViewById(R.id.rl_carmode);
//            }
//
//            if (rlPlayer == null) {
//                rlPlayer = (RelativeLayout) findViewById(R.id.rl_player);
//            }
//
//            if (rlPlayerQueue == null) {
//                rlPlayerQueue = (RelativeLayout) findViewById(R.id.rl_player_queue);
//            }
//
//            if (rlCustomDialog == null) {
//                rlCustomDialog = (RelativeLayout) findViewById(R.id.rl_custom_dialog);
//            }
//
//            if (rlLoadingDialog == null) {
//                rlLoadingDialog = (RelativeLayout) findViewById(R.id.rl_loading_dialog);
//            }
//
//            if (rlNoNetworkDialog == null) {
//                rlNoNetworkDialog = (RelativeLayout) findViewById(R.id.rl_no_network_dialog);
//            }
//
//            if (fragMainCar == null) { // Show Splash screen at first launch.
//                fragMainCar = SplashScreenFragment.newInstance();
//                nextFragmentTransaction(false, SplashScreenFragment.TAG);
//
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        loadCarMode();
//                        updatePlayerUI();
//
//                        if (handlerInternetCheck == null) {
//                            handlerInternetCheck = new Handler();
//                            handlerInternetCheck.postDelayed(runnableInternetCheck, 1000);
//                        }
//
//                    }
//                }, 3000);
//
//            } else {
//                if (PlayerService.service != null && PlayerService.service.getState() == PlayerService.State.PAUSED) {
//                    PlayerService.service.play();
//                }
//            }
		} else {
			Logger.e(TAG, " ::::::::::::::::::>> updateUI Home");
			HungamaApplication.getCacheManager().setOnCachingTrackLister(null);

//            if (fragMainCar != null) {
//                getSupportFragmentManager().beginTransaction().remove(fragMainCar).commit();
//            }
//
//            if (fragPlayer != null) {
//                getSupportFragmentManager().beginTransaction().remove(fragPlayer).commit();
//            }
//
//            if (fragPlayerQueue != null) {
//                getSupportFragmentManager().beginTransaction().remove(fragPlayerQueue).commit();
//            }
		}
	}


//	private int currentColor = 0xFF00BAFF;

	/**
	 * Home Screen content adapter
	 *
	 * @author hungama2
	 *
	 */
	public class MyPagerAdapter extends FragmentPagerAdapter
	{
		private final String[] TITLES = {
				getResources().getString(
						R.string.main_actionbar_settings_menu_item_videos),
				getResources().getString(
						R.string.main_actionbar_settings_menu_item_new_music),
				getResources().getString(
						R.string.main_actionbar_settings_menu_item_live_radio),
				getResources()
						.getString(
								R.string.main_actionbar_settings_menu_item_popular_music),
				getResources().getString(
						R.string.main_actionbar_settings_menu_item_discover) };

		private SparseArray<Fragment> map;

		private MyPagerAdapter(FragmentManager fm) {
			super(fm);
			map = new SparseArray<Fragment>();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return TITLES[position];
		}

		@Override
		public int getCount() {
			return TITLES.length;
		}

		public Fragment getCurrentFragment(int pos) {
			Fragment current = map.get(pos, null);
			return current;
		}

		@Override
		public Fragment getItem(int position) {
			String tabSelected = null;// New/Popular/Recomended
			String eventName = null; // Music/Video
			mFlurrySubSectionDescription = "No sub section description";
			Map<String, String> reportMap;
			Logger.e("home activity", "getItem " + position);
			switch (position) {
			case HomeTabBar.TAB_INDEX_MUSIC_POPULAR:
				try {
					mFlurrySubSectionDescription = FlurryConstants.FlurrySubSectionDescription.MusicPopular
							.toString();
					eventName = FlurryConstants.FlurryEventName.MusicSection
							.toString();
				} catch (Exception e) {
				}
				HomeMediaTileGridFragmentNew popularFragment = new HomeMediaTileGridFragmentNew();
				popularFragment
						.setOnMediaItemOptionSelectedListener(HomeActivity.this);
 				// Log.i("mMediaItemsLatest size",
				// String.valueOf(mMediaItemsLatest.size()));
				Bundle arguments = new Bundle();
				arguments
						.putSerializable(
								HomeMediaTileGridFragmentNew.FRAGMENT_ARGUMENT_MEDIA_CONTENT_TYPE,
								(Serializable) MediaContentType.MUSIC);
				arguments
						.putSerializable(
								HomeMediaTileGridFragmentNew.FRAGMENT_ARGUMENT_MEDIA_CATEGORY_TYPE,
								(Serializable) MediaCategoryType.POPULAR);
				arguments.putString(
						MediaTileGridFragment.FLURRY_SUB_SECTION_DESCRIPTION,
						mFlurrySubSectionDescription);
				popularFragment.setArguments(arguments);
				popularFragment.setPromoUnit(mPromoUnit);
				if (mApplicationConfigurations.isEnabledHomeGuidePage()) {
 					openAppGuideActivity();
				}
				if (mApplicationConfigurations.getAppOpenCount() == 2
						&& mApplicationConfigurations
								.isEnabledSongCatcherGuidePage()) {
					mApplicationConfigurations.increaseAppOpenCount();
					// openSearchGuideActivity();
				}

				tabSelected = MediaCategoryType.POPULAR.toString();
				// Flurry report: Which tab is selected
				reportMap = new HashMap<String, String>();
				Logger.i(TAG, "TabSelected: " + tabSelected);
				reportMap.put(
						FlurryConstants.FlurryKeys.TabSelected.toString(),
						tabSelected);
				Analytics.logEvent(eventName, reportMap);
				map.put(position, popularFragment);
				return popularFragment;
			case HomeTabBar.TAB_INDEX_MUSIC:
				if (mCurrentMediaContentType == MediaContentType.VIDEO)
				{
					try {
						eventName = FlurryConstants.FlurryEventName.VideoSection
								.toString();
					} catch (Exception e) {
					}
				} else {
					try {
						mFlurrySubSectionDescription = FlurryConstants.FlurrySubSectionDescription.MusicNew
								.toString();
						eventName = FlurryConstants.FlurryEventName.MusicSection
								.toString();
					} catch (Exception e) {
					}
				}

				HomeMediaTileGridFragmentNew latestFragment = new HomeMediaTileGridFragmentNew();
				latestFragment
						.setOnMediaItemOptionSelectedListener(HomeActivity.this);
 				// Log.i("mMediaItemsLatest size",
				// String.valueOf(mMediaItemsLatest.size()));
				arguments = new Bundle();
				arguments
						.putSerializable(
								HomeMediaTileGridFragmentNew.FRAGMENT_ARGUMENT_MEDIA_CONTENT_TYPE,
								(Serializable) MediaContentType.MUSIC);
				arguments
						.putSerializable(
								HomeMediaTileGridFragmentNew.FRAGMENT_ARGUMENT_MEDIA_CATEGORY_TYPE,
								(Serializable) MediaCategoryType.LATEST);
				arguments.putString(
						MediaTileGridFragment.FLURRY_SUB_SECTION_DESCRIPTION,
						mFlurrySubSectionDescription);
				latestFragment.setArguments(arguments);
				latestFragment.setPromoUnit(mPromoUnit);

				if (mApplicationConfigurations.isEnabledHomeGuidePage()) {
					openAppGuideActivity();
				}

				if (mApplicationConfigurations.getAppOpenCount() == 2
						&& mApplicationConfigurations
								.isEnabledSongCatcherGuidePage()) {
					mApplicationConfigurations.increaseAppOpenCount();
					// openSearchGuideActivity();
				}

				tabSelected = MediaCategoryType.NEW.toString();
 				reportMap = new HashMap<String, String>();
				Logger.i(TAG, "TabSelected: " + tabSelected);
				reportMap.put(
						FlurryConstants.FlurryKeys.TabSelected.toString(),
						tabSelected);
				Analytics.logEvent(eventName, reportMap);
				map.put(position, latestFragment);
				return latestFragment;
			case HomeTabBar.TAB_INDEX_RADIO:
				BrowseRadioFragment radioFragment = new BrowseRadioFragment();
				map.put(position, radioFragment);
				return radioFragment;
			case HomeTabBar.TAB_INDEX_VIDEO:
				try {
					mFlurrySubSectionDescription = FlurryConstants.FlurrySubSectionDescription.Video
							.toString();
					eventName = FlurryConstants.FlurryEventName.VideoSection
							.toString();
				} catch (Exception e) {
				}

				HomeMediaTileGridFragmentVideo videoFragment = new HomeMediaTileGridFragmentVideo();
				videoFragment
						.setOnMediaItemOptionSelectedListener(HomeActivity.this);

 				// Log.i("mMediaItemsLatest size",
				// String.valueOf(mMediaItemsLatest.size()));
				arguments = new Bundle();

				arguments
						.putSerializable(
								HomeMediaTileGridFragmentNew.FRAGMENT_ARGUMENT_MEDIA_CONTENT_TYPE,
								(Serializable) MediaContentType.VIDEO);
				arguments
						.putSerializable(
								HomeMediaTileGridFragmentNew.FRAGMENT_ARGUMENT_MEDIA_CATEGORY_TYPE,
								(Serializable) MediaCategoryType.LATEST);
				arguments.putString(
						MediaTileGridFragment.FLURRY_SUB_SECTION_DESCRIPTION,
						mFlurrySubSectionDescription);
				videoFragment.setArguments(arguments);
				videoFragment.setPromoUnit(mPromoUnit);

				if (mApplicationConfigurations.isEnabledHomeGuidePage()) {
					// mApplicationConfigurations.setIsEnabledHomeGuidePage(false);
					openAppGuideActivity();
				}

				if (mApplicationConfigurations.getAppOpenCount() == 2
						&& mApplicationConfigurations
								.isEnabledSongCatcherGuidePage()) {
					mApplicationConfigurations.increaseAppOpenCount();
					// openSearchGuideActivity();
				}

				tabSelected = MediaCategoryType.NEW.toString();
				// Flurry report: Which tab is selected
				reportMap = new HashMap<String, String>();
				Logger.i(TAG, "TabSelected: " + tabSelected);
				reportMap.put(
						FlurryConstants.FlurryKeys.TabSelected.toString(),
						tabSelected);
				Analytics.logEvent(eventName, reportMap);
				map.put(position, videoFragment);
				return videoFragment;
			case HomeTabBar.TAB_INDEX_DISCOVER:
				RootFragmentDiscovery fragmentDiscover = new RootFragmentDiscovery();
				fragmentDiscover
						.setOnMediaItemOptionSelectedListener(HomeActivity.this);
				map.put(position, fragmentDiscover);
				return fragmentDiscover;

			}
			return null;
		}
	}


	private Context mContext;
	private ApplicationConfigurations mApplicationConfigurations;

	public PlayerBarFragment mPlayerBar;
	// private FragmentManager mFragmentManager;

	/**
	 * Media content type of current page. Music, Radio, Video, etc.
	 */
	private MediaContentType mCurrentMediaContentType = MediaContentType.MUSIC;

	/**
	 * Index of current page.
	 */
	private int mDeafultOpenedTab = HomeTabBar.TAB_INDEX_MUSIC;

	/**
	 * List of ongoing operations id. Used for showing progress bar.
	 */
	private List<Integer> mOperationsList = new ArrayList<Integer>();

	private boolean mActivityStopped = false;

	private static boolean wasInBackground;

	private CampaignsManager mCampaignsManager;

	public final static String NOTIFICATION_MAIL = "mail";

	// public static boolean isBackFromSettings;

	/**
	 * Used for push notification handling.
	 */
	public static boolean set;
	public static boolean videoInAlbumSet;
	public static DisplayMetrics metrics;

	private UaLinkReceiver uaLinkReceiver;

	public static final String SDK_ID = "AQEAACoRAQAAAID/BwAfHwEAAQAAAAAAAAAAAA==";
	static final String ACTION_PREFERENCE_CHANGE = "preference_change";
	public static final String ACTION_LISTENER = "listener";
	public static final String ACTION_NOTIFY_ADAPTER = "notify_adapter";
	public static final String ACTION_RADIO_DATA_CHANGE = "radio_data_change";

	private boolean mHasSubscriptionPlan;

	/**
	 * @param savedInstanceState
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Logger.i("Time Diff Test","Time Diff Test2:"+ Utils.getCurrentTimeStamp());
		Logger.s(System.currentTimeMillis()
				+ " :::::::::::::Stratup::::::::::::: " + getClass().getName());
		homeActivity = this;
		setOverlayAction();
		Logger.s(System.currentTimeMillis()
				+ " :::::::::::::Stratup::::::::::::: 21 "
				+ getClass().getName());
		Logger.s(System.currentTimeMillis()
				+ " :::::::::::::SetContentView::::::::::::: Start");
		super.onCreate(savedInstanceState);

		if(isFinishing())
			return;
        setContentView(R.layout.activity_home);
        onCreateCode();
		// setContentView(R.layout.activity_home);
		// isSearchOpened = false;
		Logger.s("Track HomeActivity onCreate");
		Logger.s("1 HomeTime:" + System.currentTimeMillis());

		Instance = this;

		mContext = getApplicationContext();
		mDataManager = DataManager.getInstance(mContext);
		Logger.s("5 HomeTime:" + System.currentTimeMillis());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();


		if (false) {
			Toast.makeText(
					HomeActivity.this,
					"Language Support:"
							+ mApplicationConfigurations
									.isLanguageSupportedForWidget(),
					Toast.LENGTH_SHORT).show();
		}
		if (getIntent().getBooleanExtra("finish_all", false)) {
			// Switch to offline mode
			// finish();
			Intent i = new Intent(this, GoOfflineActivity.class);
			i.putExtra("show_toast", true);
			i.putExtra("open_upgrade_popup",
					getIntent().getBooleanExtra("open_upgrade_popup", false));
			startActivity(i);
			super.finish();
			return;
		} else if (getIntent().getBooleanExtra("finish_restart", false)) {
			// restart application
			restartApplication();
			return;
		} else if (getIntent().getBooleanExtra("finish_app", false)) {
			// close app
			mPlayerBar.explicitStop();

			// reset the inner boolean for showing home
			// tile hints.
			mApplicationConfigurations.setIsHomeHintShownInThisSession(false);
			mApplicationConfigurations
					.setIsSearchFilterShownInThisSession(false);
			mApplicationConfigurations
					.setIsPlayerQueueHintShownInThisSession(false);
			// if this button is clicked, close
			// current activity
			HomeActivity.super.onBackPressed();
			HomeActivity.this.finish();
			return;
		}

		Logger.s("2 HomeTime:" + System.currentTimeMillis());
		FragmentManager.enableDebugLogging(true);

		// offline to online switching animation
		if (getIntent().getBooleanExtra("show_toast", false))
			overridePendingTransition(R.anim.fadein, R.anim.fadeout);

		Logger.s("3 HomeTime:" + System.currentTimeMillis());

		getDrawerLayout();

		Logger.s("4 HomeTime:" + System.currentTimeMillis());
		final WindowManager w = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		final Display d = w.getDefaultDisplay();
		metrics = new DisplayMetrics();
		d.getMetrics(metrics);
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		Logger.s("6 HomeTime:" + System.currentTimeMillis());
		ThreadPoolManager.getInstance().submit(new Runnable() {
			public void run() {
				if (!getIntent().getBooleanExtra("skip_ad", false))
					mApplicationConfigurations.increaseAppOpenCount();
			}


		});
		Logger.s("7 HomeTime:" + System.currentTimeMillis());

		// mFragmentManager = getSupportFragmentManager();
		Logger.s("8 HomeTime:" + System.currentTimeMillis());
		if (Boolean.parseBoolean(mContext.getResources().getString(
				R.string.rate_app_on_off))) {
			mAppirater.appLaunched(true);
		}
		Logger.s("9 HomeTime:" + System.currentTimeMillis());

		mPlayerBar = getPlayerBar();
		Logger.s("10 HomeTime:" + System.currentTimeMillis());
		initializeUserControls();

		Logger.s("14 HomeTime:" + System.currentTimeMillis());
		if (!set) {
			onNewIntent(getIntent());
			setVideoLinks();
			isOnCreate = true;
		}
		Logger.s("15 HomeTime:" + System.currentTimeMillis());
//		if (mCurrentMediaContentType == MediaContentType.MUSIC) {
			// Check for device offer and show toast if needed.
			messageThread();
//		}

		Logger.s("16 HomeTime:" + System.currentTimeMillis());

		if (uaLinkReceiver == null) {
			uaLinkReceiver = new UaLinkReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(AlertActivity.ACTION_UA_LINK);
			registerReceiver(uaLinkReceiver, filter);
		}

		Logger.s("Track HomeActivity onCreate finish");

		menu = (ImageView) findViewById(R.id.menu);
		menu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ScreenLockStatus.getInstance(HomeActivity.this).dontShowAd();
				Logger.s("SatelliteMenu onClick");
				Intent i = new Intent(HomeActivity.this,
						TransperentActivity.class);
				if (mDeafultOpenedTab == HomeTabBar.TAB_ID_RADIO) {
					i.putExtra("isRadioFilter", true);
				}
				i.putExtra("isMusicNew",
						(mDeafultOpenedTab == HomeTabBar.TAB_INDEX_MUSIC));

				startActivityForResult(i, FILTER_OPTIONS_ACTIVITY_RESULT_CODE);

				menu.setTag("sethidden");
				menu.setVisibility(View.INVISIBLE);
				// overridePendingTransition(R.anim.pull_up_from_bottom,R.anim.push_out_to_bottom_discover);
			}
		});

		registerReceivers();
		showCategoryActionBar(true);

		if (!mApplicationConfigurations.isVersionChecked()) {
			mDataManager.newVersionCheck(HomeActivity.this);
		}

		// Check for user subscription
		String sesion = mApplicationConfigurations
				.getSessionID();
		boolean isRealUser = mApplicationConfigurations
				.isRealUser();
		if (!TextUtils.isEmpty(sesion) && (isRealUser || Logger.allowPlanForSilentUser)) {
			mHasSubscriptionPlan = mApplicationConfigurations
					.isUserHasSubscriptionPlan();
			String accountType = Utils.getAccountName(getApplicationContext());
			mDataManager.getCurrentSubscriptionPlan(HomeActivity.this, accountType);
		}

		// Load music tab on app launch
		onTabSelected(mDeafultOpenedTab);

		// For api testing
		// mDataManager.testApi(this);

		// Locale[] locs= Locale.getAvailableLocales();
		// for(Locale l:locs){
		// Log.d("Home", "language :: " + l.getDisplayLanguage());
		// }

		// final Locale[] availableLocales = Locale.getAvailableLocales();
		// for (final Locale locale : availableLocales) {
		// if (locale.getCountry().equalsIgnoreCase("IN")) {
		// Log.d("Applog", "language : " + locale.getDisplayName() + ":"
		// + locale.getLanguage() + ":" + locale.getCountry()
		// + ":values-" + locale.toString().replace("_", "-r"));
		// }
		// }
		// 05-20 11:05:50.987: D/Applog(31536): language : Hindi
		// (India):hi:IN:values-hi-rIN
		// 05-20 11:05:50.988: D/Applog(31536): language : Punjabi
		// (Gurmukhi,India):pa:IN:values-pa-rIN-r#Guru
		// 05-20 11:05:50.988: D/Applog(31536): language : Tamil
		// (India):ta:IN:values-ta-rIN
		// 05-20 11:05:50.988: D/Applog(31536): language : Telugu
		// (India):te:IN:values-te-rIN

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            public void onBackStackChanged() {
				getSupportFragment(false);
			}
        });

		if(savedInstanceState!=null) {
//			findViewById(R.id.home_browse_by_fragmant_container).setVisibility(View.VISIBLE);
			int backCount = getSupportFragmentManager().getBackStackEntryCount();
			for (int i = backCount; i> 1; i--) {
				Logger.e("backCount",""+backCount);
				int backStackId = getSupportFragmentManager().getBackStackEntryAt(i-1).getId();
				getSupportFragmentManager().popBackStackImmediate(backStackId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			}
			unlockDrawer();
			ShowDrawerIconAndPreference();
//			lockDrawer();
//			removeDrawerIconAndPreference();
//			setNeedToOpenSearchActivity(false);
		}
//		throw new  IllegalArgumentException("test");
		if(OnApplicationStartsActivity.needToStartApsalrSession){
			Apsalar.startSession(getApplicationContext(), getString(R.string.apsalar_api_key), getString(R.string.apsalar_secret));
			ApsalarEvent.postEvent(getApplicationContext(), ApsalarEvent.APP_LAUNCH_EVENT);
			OnApplicationStartsActivity.needToStartApsalrSession = false;
		}
//		ApsalarEvent.postEvent(this, ApsalarEvent.LOADING_THE_HOME);
		//setCastCurrentIndex();


	}


	public void startSyncProxy() {
		if (ProxyService.getInstance() == null) {
			Intent startService = new Intent(this, ProxyService.class);
			Log.i(TAG,"Calling start Service to Start the Service");
			startService(startService);
		}
	}
	public void getSupportFragment(boolean isFromChild) {
		try {
			int backCount = getSupportFragmentManager().getBackStackEntryCount();
			Logger.i(TAG, "back stack changed " + backCount);
			Logger.i(TAG, "back stack fragmentCount " + fragmentCount);

			if (backCount == 0 && fragmentCount > 0) {
				setUpNavigationFragment();
				return;
			}
			if (backCount == 1 && fragmentCount > 0) {
				resetHomeScreen();
				return;
			}
			if (fragmentCount > backCount || isFromChild) {
				FragmentManager.BackStackEntry backEntry = (FragmentManager.BackStackEntry) getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1);
				String str = backEntry.getName();
				Logger.i(TAG, "back stack name " + str);
				Fragment fragment = getSupportFragmentManager().findFragmentByTag(str);
				resetCurrentFragment(fragment);
			}
			fragmentCount = backCount;
		} catch (Exception e) {

		} catch (Error e) {

		}
	}

	public void resetCurrentFragment(Fragment fragment){
		try {
				if (fragment instanceof MediaDetailsActivityNew) {
					MediaDetailsActivityNew mediaDetailsActivity = (MediaDetailsActivityNew) fragment;
					if(mediaDetailsActivity!=null && !mediaDetailsActivity.setChildFragment(true)){
						setSelectedFragment(mediaDetailsActivity);
						mediaDetailsActivity.setTitle(true, true);
					}
				} else if (fragment instanceof MediaDetailsFragment) {
					MediaDetailsFragment mediaDetailsActivity = (MediaDetailsFragment) fragment;
					setSelectedFragment(mediaDetailsActivity.mediaDetailsActivityNew);
					mediaDetailsActivity.mediaDetailsActivityNew.setTitle(true, false);
				} else if (fragment instanceof ProfileActivity) {
					ProfileActivity mPrifileActivity = (ProfileActivity) fragment;
					setSelectedFragment(mPrifileActivity);
					mPrifileActivity.setTitle(false);
				} else if (fragment instanceof PlaylistsActivity) {
					PlaylistsActivity mPlaylistsActivity = (PlaylistsActivity) fragment;
					setSelectedFragment(mPlaylistsActivity);
					mPlaylistsActivity.setTitle(false, true);
				}else if (fragment instanceof FavoritesActivity) {
					FavoritesActivity mfaFavoritesActivity = (FavoritesActivity) fragment;
					setSelectedFragment(mfaFavoritesActivity);
					mfaFavoritesActivity.setTitle(false, true);
				} else if (fragment instanceof MyCollectionActivity) {
					MyCollectionActivity mMycollectionActivity = (MyCollectionActivity) fragment;
					setSelectedFragment(mMycollectionActivity);
					mMycollectionActivity.setTitle(false, true);
				}else if (fragment instanceof FavoritesFragment) {
					FavoritesFragment mfaFavoritesFragment = (FavoritesFragment) fragment;
					if(mfaFavoritesFragment.profileActivity!=null)
						setSelectedFragment(mfaFavoritesFragment.profileActivity);
					mfaFavoritesFragment.setTitle();
				}else if (fragment instanceof MyStreamActivity) {
					MyStreamActivity mMyStreamFragment = (MyStreamActivity) fragment;
					setSelectedFragment(mMyStreamFragment);
					mMyStreamFragment.setTitle(false,true);
				}else if (fragment instanceof SocialMyStreamFragment) {
					SocialMyStreamFragment socialMyStreamFragment = (SocialMyStreamFragment) fragment;
					socialMyStreamFragment.myStreamActivity.setTitle(false,true);
				}else if (fragment instanceof BadgesFragment) {
					BadgesFragment mBadgesFragment = (BadgesFragment) fragment;
					setSelectedFragment(mBadgesFragment.getProfileActivity());
					mBadgesFragment.setTitle();
				} else if (fragment instanceof LeaderboardFragment) {
					LeaderboardFragment mLeaderboardFragment = (LeaderboardFragment) fragment;
					setSelectedFragment(mLeaderboardFragment.getProfileActivity());
					mLeaderboardFragment.setTitle();
				} else if (fragment instanceof RedeemFragment) {
					RedeemFragment mRedeemFragment = (RedeemFragment) fragment;
					setSelectedFragment(mRedeemFragment.getProfileActivity());
					mRedeemFragment.setTitle();
				} else if (fragment instanceof ItemableTilesFragment) {
					ItemableTilesFragment mediaDetailsActivity = (ItemableTilesFragment) fragment;
					setSelectedFragment(mediaDetailsActivity);
					mediaDetailsActivity.setTitle(false, true);
				} else if (fragment instanceof MediaTileGridFragment) {
					MediaTileGridFragment mediaDetailsActivity = (MediaTileGridFragment) fragment;
					setSelectedFragment(mediaDetailsActivity);
//                        setSelectedFragment(mediaDetailsActivity.);
					mediaDetailsActivity.setTitle(false, true);
				} else if (fragment instanceof MainSearchFragmentNew) {
					MainSearchFragmentNew mediaDetailsActivity = (MainSearchFragmentNew) fragment;
					setSelectedFragment(mediaDetailsActivity);
					// mediaDetailsActivity.displayTitle(mediaDetailsActivity.actionbar_title);
					mediaDetailsActivity.setTitle(false, true);
					mediaDetailsActivity.onCreateOptionsMenu(mediaDetailsActivity.mMenu, null);
					mediaDetailsActivity.showSearchView();

				} else if (fragment instanceof MainSearchResultsFragment) {
					MainSearchResultsFragment mediaDetailsActivity = (MainSearchResultsFragment) fragment;
					setSelectedFragment(mediaDetailsActivity.searchResultsFragment);
					mediaDetailsActivity.searchResultsFragment.displayTitle(mediaDetailsActivity.searchResultsFragment.actionbar_title);
//					try {
//						mMenu.findItem(R.id.media_route_menu_item).setVisible(false);
//					}catch (Exception e){
//					}

				} else if (fragment instanceof MainSearchResultsFragment) {
					MainSearchResultsFragment mediaDetailsActivity = (MainSearchResultsFragment) fragment;
					setSelectedFragment(mediaDetailsActivity.searchResultsFragment);
					mediaDetailsActivity.searchResultsFragment.displayTitle(mediaDetailsActivity.searchResultsFragment.actionbar_title);
				}/*else if (fragment instanceof SongCatcherFragment) {
					SongCatcherFragment songCatcherFragment = (SongCatcherFragment) fragment;
					setSelectedFragment(songCatcherFragment);
					songCatcherFragment.setTitle(false,true);
				}*/
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	private void playDiscoverHashTag(){

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (adapter != null) {
					RootFragmentDiscovery fragmentDiscover = (RootFragmentDiscovery) adapter
							.getCurrentFragment(HomeTabBar.TAB_INDEX_DISCOVER);
					if (fragmentDiscover != null
							&& fragmentDiscover.fragment != null) {
						fragmentDiscover.fragment.playHashtag(true);
					}
				}
			}
		}, 600);

	}
	
	private void removeFragmentOnPushNotification(){

        Handler handler1=new Handler();
        handler1.postDelayed(new Runnable() {
			@Override
			public void run() {


				int backCount = getSupportFragmentManager().getBackStackEntryCount();
				//   while (backCount>1){
				if (getIntent().getBooleanExtra(AlertActivity.ALERT_MARK, false)) {

					for (int i = backCount; i > 1; i--) {
						Logger.e("backCount", "" + backCount);
						try {
							int backStackId = getSupportFragmentManager().getBackStackEntryAt(i - 1).getId();
							getSupportFragmentManager().popBackStackImmediate(backStackId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
//
				if (!set) {
					if (getIntent().getBooleanExtra("rate", false)) {

						mAppirater.rateAppClick(HomeActivity.this);
						set = true;
						getIntent().removeExtra("rate");

					} else if (getIntent().getBooleanExtra("faq", false)) {
						Intent helpAndFaqActivityIntent = new Intent(
								getApplicationContext(), HelpAndFAQActivity.class);
						startActivity(helpAndFaqActivityIntent);
						set = true;
						getIntent().removeExtra("faq");
					} else if (getIntent().getBooleanExtra("feedback", false)) {
						Intent feedbackActivityIntent = new Intent(
								getApplicationContext(), FeedbackActivity.class);
						startActivity(feedbackActivityIntent);
						set = true;
						getIntent().removeExtra("feedback");
					} else if (getIntent().getBooleanExtra("about", false)) {
						Intent aboutActivityIntent = new Intent(
								getApplicationContext(), AboutActivity.class);
						startActivity(aboutActivityIntent);
						set = true;
						getIntent().removeExtra("about");
					} else if (getIntent().getBooleanExtra("invite", false)) {
						Intent inviteFriendsActivity = new Intent(HomeActivity.this,
								InviteFriendsActivity.class);
						inviteFriendsActivity.putExtra(
								InviteFriendsActivity.FLURRY_SOURCE,
								FlurryConstants.FlurryInvite.GlobalMenu.toString());
						startActivity(inviteFriendsActivity);
						set = true;
						getIntent().removeExtra("invite");
					} else if (getIntent().getBooleanExtra("rewards", false)) {
						Intent redeemActivityIntent = new Intent(
								getApplicationContext(), RedeemActivity.class);
						startActivity(redeemActivityIntent);
						set = true;
						getIntent().removeExtra("rewards");
					} else if (getIntent().getBooleanExtra("my_profile", false)) {
						openProfileActivity(false);
						set = true;
						getIntent().removeExtra("my_profile");
					} else if (getIntent().getBooleanExtra("login", false)) {
						if (!mApplicationConfigurations.isRealUser()) {
							Intent startLoginActivityIntent = new Intent(
									getApplicationContext(),
									LoginActivity.class);
							startLoginActivityIntent
									.putExtra(
											ARGUMENT_HOME_ACTIVITY,
											"home_activity");
							startLoginActivityIntent
									.putExtra(
											LoginActivity.FLURRY_SOURCE,
											FlurryConstants.FlurrySourceSection.Home
													.toString());
							startLoginActivityIntent
									.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
							startActivityForResult(
									startLoginActivityIntent,
									LOGIN_ACTIVITY_CODE);
						}
						set = true;
						getIntent().removeExtra("login");
					} else if (getIntent().getBooleanExtra("my_collection", false)) {

						openMyCollection();

						set = true;
						getIntent().removeExtra("my_collection");
					} else if (getIntent().getBooleanExtra("my_favourites", false)) {

						openFavorite();
						set = true;
						getIntent().removeExtra("my_favourites");
					} else if (getIntent().getBooleanExtra("my_playlists", false)) {
						openPlaylist();
						set = true;
						getIntent().removeExtra("my_playlists");
					} else if (getIntent().getBooleanExtra("audio_latest", false)) {

						mDeafultOpenedTab = HomeTabBar.TAB_INDEX_MUSIC;
						set = true;
						getIntent().removeExtra("audio_latest");
					} else if (getIntent().getBooleanExtra("discover", false)) {
						if (!set) {
							if (isOnCreate)
								loadMediaItems();

							mDeafultOpenedTab = HomeTabBar.TAB_INDEX_DISCOVER;
							onTabSelected(mDeafultOpenedTab);
							set = true;
							getIntent().removeExtra("discover");
						}
					} else if (getIntent().getBooleanExtra("discover_hash", false)) {
						if (!set) {
							if (isOnCreate)
								loadMediaItems();

							mDeafultOpenedTab = HomeTabBar.TAB_INDEX_DISCOVER;
							onTabSelected(mDeafultOpenedTab);
							set = true;
							playDiscoverHashTag();
							getIntent().removeExtra("discover_hash");
						}
					} else if (getIntent().getBooleanExtra("live_radio", false)
							|| getIntent().getBooleanExtra("top_celebs", false)
							// || getIntent().getStringExtra("channel_index") !=
							// null
							|| getIntent().getStringExtra("radio_id") != null
							|| getIntent().getStringExtra("artist_id") != null
							|| getIntent().getStringExtra("Station_ID") != null) {
						if (!set) {
							if (isOnCreate)
								loadMediaItems();

							mDeafultOpenedTab = HomeTabBar.TAB_INDEX_RADIO;
							onTabSelected(mDeafultOpenedTab);
							set = true;
						}
					} else if (getIntent().getBooleanExtra("settings", false)) {
						Intent settingsIntent = new Intent(HomeActivity.this,
								SettingsActivity.class);
						startActivity(settingsIntent);
						set = true;
						getIntent().removeExtra("settings");
					} else if (getIntent().getBooleanExtra("app_tour", false)) {
						Intent tourIntent = new Intent(HomeActivity.this, AppTourActivity.class);
						startActivity(tourIntent);
						set = true;
						getIntent().removeExtra("app_tour");
					} else if (getIntent().getBooleanExtra("search", false)
							|| getIntent().getBooleanExtra("song_catcher", false)) {
						if (isOnCreate)
							loadMediaItems();

//					handler.postDelayed(new Runnable() {
//						@Override
//						public void run() {
						openSearch(false, false);
						//getIntent().removeExtra("song_catcher");
//						}
//					}, 1000);
						set = true;
						getIntent().removeExtra("search");
						getIntent().removeExtra("song_catcher");

					} else if (getIntent().getBooleanExtra("show_languages", false)) {
						Intent settingsIntent = new Intent(HomeActivity.this,
								SettingsActivity.class);
						settingsIntent.putExtra("show_languages", true);
						startActivity(settingsIntent);
						set = true;
						getIntent().removeExtra("show_languages");
					} else if (getIntent()
							.getBooleanExtra("show_membership", false)) {
						Intent settingsIntent = new Intent(HomeActivity.this,
								SettingsActivity.class);
						settingsIntent.putExtra("show_membership", true);
						startActivity(settingsIntent);
						set = true;
						getIntent().removeExtra("show_membership");
					} else if (getIntent().getBooleanExtra("video_latest", false)) {
						if (!set) {
							if (isOnCreate)
								loadMediaItems();

							mDeafultOpenedTab = HomeTabBar.TAB_INDEX_VIDEO;
							onTabSelected(mDeafultOpenedTab);
							set = true;
							getIntent().removeExtra("video_latest");
						}
					} else if (getIntent().getBooleanExtra("audio_featured", false)) {

						if (!set) {
							if (isOnCreate)
								loadMediaItems();

							mDeafultOpenedTab = HomeTabBar.TAB_INDEX_MUSIC_POPULAR;
							onTabSelected(mDeafultOpenedTab);
							set = true;
							getIntent().removeExtra("audio_featured");
						}
					} else if (!TextUtils.isEmpty(getIntent().getStringExtra(IntentReceiver.SUBSCRIPTION_PLANS))) {
						String value = getIntent().getStringExtra(IntentReceiver.SUBSCRIPTION_PLANS);
						Logger.s(value + " key ::::::::::::::::::::::: " + value);
						try {
							String response = new com.hungama.myplay.activity.data.CacheManager(
									HomeActivity.this).getLeftMenuResponse();
							LeftMenuResponse mLeftMenuResponse = new Gson().fromJson(response,
									LeftMenuResponse.class);
							if (mLeftMenuResponse != null) {
								List<LeftMenuItem> menu = mLeftMenuResponse.getLeftMenuItems();
								for (LeftMenuItem temp_meu : menu) {
									if ((!TextUtils.isEmpty(temp_meu.getMainMenu()) &&
											temp_meu.getMainMenu().equalsIgnoreCase(value)) ||
											(!TextUtils.isEmpty(temp_meu.getMenu_title()) &&
													temp_meu.getMenu_title().equalsIgnoreCase(value))) {
										GlobalMenuFragment temp;
										if (mainSettingsFragment == null)
											temp = getGlobalMenu();
										else
											temp = mainSettingsFragment;
										GlobalMenuFragment.MenuItem menuItem = null;
										if (temp_meu.getMainMenu() != null
												&& !temp_meu.getMainMenu().equals(""))
											menuItem = temp.new MenuItem(temp_meu.getMainMenu(), "",
													temp_meu.getInapp_action(), temp_meu.getLink_type(),
													temp_meu.getHtmlURL(), temp_meu.getPopUpMessage(), temp_meu.getExtra_data());
										else if (temp_meu.getMenu_title() != null
												&& !temp_meu.getMenu_title().equals(""))
											menuItem = temp.new MenuItem(temp_meu.getMenu_title(),
													"", temp_meu.getInapp_action(), temp_meu
													.getLink_type(), temp_meu.getHtmlURL(),
													temp_meu.getPopUpMessage(), temp_meu.getExtra_data());
										if (menuItem != null)
											onGlobalMenuItemSelected(menuItem, null);
										set = true;
										getIntent().removeExtra(IntentReceiver.SUBSCRIPTION_PLANS);
										break;
									}
								}
							}
						} catch (Exception e) {
							extraData = null;
							Logger.printStackTrace(e);
						}
					}

					Logger.s(mCurrentMediaContentType
							+ " wasInBackground ::::::::::::::::::::::: "
							+ wasInBackground);

					if (!setVideoLinks()) {
						wasInBackground = true;
						loadMediaItems();
					}


				}
				getIntent().removeExtra(AlertActivity.ALERT_MARK);
			}
		}, 500);

       }


    public int fragmentCount = 0;

    private void resetHomeScreen() {

        findViewById(R.id.pager).setVisibility(View.VISIBLE);
        findViewById(R.id.tabs).setVisibility(View.VISIBLE);

//        if (selectedFragment instanceof MediaDetailsActivityNew) {
		Utils.setToolbarColor(this);
//        }
        removeCurrentFragment();
        findViewById(R.id.home_browse_by_fragmant_container).setVisibility(View.GONE);
		findViewById(R.id.home_browse_by_fragmant_container_playlist).setVisibility(View.GONE);
        ShowDrawerIconAndPreference();

        selectedFragment = null;
		/*new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {*/
				//castMenuItemHideShow(true);
			/*}
		}, 500);*/
    }

	/**
	 * Check in-app prompts conditions and show popup.
	 */
	private void setInAppPromts() {
		if (!getIntent().getBooleanExtra("skip_ad", false)
				&& !getIntent().getBooleanExtra("show_toast", false)) {
			boolean isPromptShown = false;
			AppPromptRegistrationSignIn promptRegistration = new AppPromptRegistrationSignIn(
					this);
			isPromptShown = promptRegistration.appLaunched(true);

			if (!isPromptShown) {
				AppPromptSocialNative2 promptSocialNative2 = new AppPromptSocialNative2(
						this);
				isPromptShown = promptSocialNative2.appLaunched(true);
			}

			if (!isPromptShown) {
				AppPromptCategoryPrefSelectionGeneric promptSocialNative6 = new AppPromptCategoryPrefSelectionGeneric(
						this);
				isPromptShown = promptSocialNative6.appLaunched(true);
			}

			if (!isPromptShown) {
				AppPromptOfflineCaching3rdSong appPrompt10 = new AppPromptOfflineCaching3rdSong(
						HomeActivity.this);
				isPromptShown = appPrompt10.appLaunched(true, false);
			}
		}
	}

	/**
	 * Satellite menu option for Music New and Popular. It will open
	 * TransperentActivity.
	 */
	private ImageView menu;

	private void hideSatelliteMenu() {
		if (menu != null)
			menu.setVisibility(View.GONE);
	}

	private void showSatelliteMenu() {
		if (menu != null) {
			menu.setVisibility(View.VISIBLE);
			DisplayHelp();
		}
	}

	private boolean isOnCreate = false;

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		return super.onCreateOptionsMenu(menu);
//	}
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int itemId = item.getItemId();
//        if (itemId == android.R.id.home) {
//            int count = getSupportFragmentManager()
//                    .getBackStackEntryCount();
//            Logger.e("count:*onOptionsItemSelected",""+count);
//           onBackPressed();
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
	public void setNavigationItemSelected(NavigationItem navigationItem) {
		super.setNavigationItemSelected(navigationItem);
	}

	@Override
	protected void onStart() {

		super.onStart();
		if(isFinishing())
			return;
		mActivityStopped = false;
		Analytics.startSession(this);
		Analytics.onPageView();
		// Flurry report: Status
		if (isSkipResume) {
			return;
		}

		int registrationStatus = 0;
		int paidStatus = 0;

        try {
            MySpinServerSDK.sharedInstance().registerConnectionStateListener(this);
        } catch (MySpinException e) {
            e.printStackTrace();
        }

		Map<String, String> reportMap = new HashMap<String, String>();
		reportMap.put(
				FlurryConstants.FlurryUserStatus.RegistrationStatus.toString(),
				String.valueOf(registrationStatus));
		reportMap.put(FlurryConstants.FlurryUserStatus.PaidStatus.toString(),
				String.valueOf(paidStatus));
		Analytics.logEvent(FlurryConstants.FlurryUserStatus.Status.toString(),
				reportMap);



//		new Thread() {
//			public void run() {
				// Write UA APIID & tags to file on sdcard for debuging &
				// testing purpose
				String apid = UAirship.shared().getPushManager().getChannelId();
				Logger.s("PUSH APID: >>>>>>>>>>>>>>>>>" + apid);
				Logger.writetofile_("PUSH APID: >>>>>>>>>>>>>>>>>" + apid, false);
                String gcmtoken=UAirship.shared().getPushManager().getGcmToken();
				Logger.s("GCM TOKEN: >>>>>>>>>>>>>>>>>" + gcmtoken);
                call_gcmtoken(gcmtoken);
				Set<String> tagset = Utils.getTags();
				String tags = "";
				Iterator<String> itr = tagset.iterator();
				while (itr.hasNext()) {
					tags += itr.next() + ",";
				}

				Logger.s("UA TAGS: >>>>>>>>>>>>>>>>>" + tags);
				Logger.writetofile("UA TAGS: >>>>>>>>>>>>>>>>>" + tags, true);

//		String alias = UAirship.shared().getPushManager().getAlias();
		String alias = UAirship.shared().getPushManager().getNamedUser().getId();
		if(TextUtils.isEmpty(alias)){
			String hardwareId = mDataManager.getDeviceConfigurations().getHardwareId();
			if(!TextUtils.isEmpty(mApplicationConfigurations.getHungamaEmail()))
				Utils.setAlias(mApplicationConfigurations.getHungamaEmail(), hardwareId);
			else if(!TextUtils.isEmpty(mApplicationConfigurations.getGigyaFBEmail()))
				Utils.setAlias(mApplicationConfigurations.getGigyaFBEmail(), hardwareId);
			else if(!TextUtils.isEmpty(mApplicationConfigurations.getGigyaTwitterEmail()))
				Utils.setAlias(mApplicationConfigurations.getGigyaTwitterEmail(), hardwareId);
			else if(!TextUtils.isEmpty(mApplicationConfigurations.getGigyaGoogleEmail()))
				Utils.setAlias(mApplicationConfigurations.getGigyaGoogleEmail(), hardwareId);
			else
				Utils.setAlias(null, hardwareId);
		} else{
			Logger.e("PUSH TAGS: ", "Alias: >>>>>>>>>>>>>>>>>" + alias);
			Logger.writetofile("Alias: >>>>>>>>>>>>>>>>>" + alias, true);
		}
//			}
//		}.start();

		Logger.s("4 HomeTime:onStart" + System.currentTimeMillis());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Logger.s("---------------onNewIntent---------------");

		try {
			if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
				String query = intent.getStringExtra(SearchManager.QUERY);

				FragmentManager.BackStackEntry backEntry = (FragmentManager.BackStackEntry) getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1);
				String str = backEntry.getName();
				Logger.i(TAG, "back stack name " + str);
				MainSearchFragmentNew searchFragmentNew = (MainSearchFragmentNew) getSupportFragmentManager().findFragmentByTag(str);
				searchFragmentNew.onStartSearchKeyboard(query);
				return;
			}
		} catch (Exception e) {

		}

		if (getIntent().getExtras() != null) {

            if (intent.getBooleanExtra("finish_all", false)) {
                if (!Utils.isCarMode()) {
                    // Switch to offline mode
                    Intent i = new Intent(this, GoOfflineActivity.class);
                    i.putExtra("show_toast", true);
                    startActivity(i);
                    super.finish();
                }
                return;
            } else if (getIntent().getBooleanExtra("car_mode", false)) {
                // Switch to Car mode
                // finish();
                Intent i = new Intent(this, CarModeHomeActivity.class);
                i.putExtra("from_home", true);
                startActivity(i);
                super.finish();
                return;
            } else if (intent.getBooleanExtra("finish_restart", false)) {
                // restart application
                restartApplication();
                return;
            } else if (intent.getBooleanExtra("finish_app", false)) {
                // Close app
                mPlayerBar.explicitStop();

				// reset the inner boolean for showing home
				// tile hints.
				mApplicationConfigurations
						.setIsHomeHintShownInThisSession(false);
				mApplicationConfigurations
						.setIsSearchFilterShownInThisSession(false);
				mApplicationConfigurations
						.setIsPlayerQueueHintShownInThisSession(false);
				// if this button is clicked, close
				// current activity
				HomeActivity.super.onBackPressed();
				HomeActivity.this.finish();
				return;
			}

			/**
			 * Checks if it was requested for a specific Media Category to be
			 * presented, if not Music is the defaut.
			 */
			Intent callingIntent = intent;
			if (callingIntent != null) {
				Bundle arguments = callingIntent.getExtras();
				if (arguments != null) {
					if (arguments.containsKey("donothing")) {
						// Not to refresh
						return;
					}
					// gets the content type of the activity.
					if (arguments
							.containsKey(ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE)) {

						mCurrentMediaContentType = (MediaContentType) arguments
								.getSerializable(ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE);
					}
					// checks if there is a request to open other tab then the
					// "Latest".
					if (arguments
							.containsKey(ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION)) {
						getIntent()
								.putExtra(
										ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION,
										arguments
												.getInt(ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION));

						mDeafultOpenedTab = arguments
								.getInt(ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION);
						Logger.i(TAG, "TabSelected onNewIntent: "
								+ mDeafultOpenedTab);

						if (pager != null) {
							pager.setCurrentItem(mDeafultOpenedTab, true);
						}
					}

					if (arguments.getBoolean(ACTIVITY_EXTRA_OPEN_BROWSE_BY,
							false)) {
						getIntent().putExtra(ACTIVITY_EXTRA_OPEN_BROWSE_BY,
								true);
					} else if (arguments.getString("browse_by_content_type") != null
							&& arguments.getString("browse_by_content_type")
									.equals("1")) {
						arguments.remove("browse_by_content_type");
						getIntent().putExtra(ACTIVITY_EXTRA_OPEN_BROWSE_BY,
								true);
					}

					if (arguments.getBoolean("show_languages", false)) {
						getIntent().putExtra("show_languages", true);
					}

					Logger.s("arguments AlertActivity.ALERT_MARK ::::::::: "
							+ arguments.getBoolean(AlertActivity.ALERT_MARK,
									false));
					if (arguments.getBoolean(AlertActivity.ALERT_MARK, false)) {
						isSkipResume = false;
						closequeue = true;
						getIntent().removeExtra(NOTIFICATION_MAIL);
					}
					Logger.s("NOTIFICATION_MAIL ::::::::: "
							+ arguments.getBoolean(NOTIFICATION_MAIL, false));
					if (arguments.getBoolean(NOTIFICATION_MAIL, false)) {
						getIntent().putExtra(NOTIFICATION_MAIL,
								arguments.getBoolean(NOTIFICATION_MAIL, false));
					}
				}
			}
			Logger.s("NOTIFICATION_MAIL ::::::::: "
					+ getIntent().getBooleanExtra(NOTIFICATION_MAIL, false));
		}
		// sets the Action Bar's title.
		ActionBar actionBar = getSupportActionBar();

		Utils.setActionBarTitle(mContext, actionBar, "");
		setIntent(intent);
		super.onNewIntent(intent);
	}


	//private MiniController mMini;

	@Override
	protected void onResume() {
		Logger.s(System.currentTimeMillis()
				+ " :::::::::::::Stratup:::::::::::::1 " + getClass().getName());

		super.onResume();

		startSyncProxy();

		/*if (mCastManager!=null && isCastPlaying()) {
			stopCastNotification();

        }*/
      if (Utils.isCarMode() ||  isFinishing())
            return;

//		if(AdXEvent.ENABLED){
////			Apsalar.restartSession();//unregisterApsalarReceiver();
////			Apsalar.restartSession(this, getString(R.string.apsalar_api_key), getString(R.string.apsalar_secret));
//			Apsalar.registerReceiver(getApplicationContext());
//		}
		if (isOnCreate) {
//			sendBroadcast(new Intent(
//					getString(R.string.inapp_prompt_action_apppromptofflinecaching3rdsong)));
		} else {
			try {
				findViewById(R.id.progressbar).setVisibility(View.GONE);
			} catch (Exception e) {
			}

			hideLoadingDialog();
		}

		HungamaApplication.activityResumed();

		if(getIntent().getBooleanExtra(AlertActivity.ALERT_MARK, false))
			isSkipResume = false;

		if (isSkipResume) {
			isSkipResume = false;
			return;
		}

		if (menu != null && menu.getTag() != null) {
			menu.setTag(null);
			menu.setVisibility(View.VISIBLE);
		}

		// Open offline guide if 1st song auto cached.
		if (mApplicationConfigurations.isSongCatched()) {
			openOfflineGuide();
		}
		Logger.s("onResume HomeScreen");

		// Close player queue on push notification view action.
		if (closequeue) {
			closequeue = false;
			// Logger.s("----------------Closing content-----------------");
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					// Logger.s("----------------Closing content-----------------1");
//					if (SongCatcherFragment.isSongCatcherOpen) {
//						HomeActivity.super.onBackPressed();
//					}
					mPlayerBar.collapsedPanel1();
					closePlayerBarContent();
				}
			}, 1000);
		}

		if (cacheStateReceiver == null) {
			cacheStateReceiver = new CacheStateReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(CacheManager.ACTION_CACHE_STATE_UPDATED);
			filter.addAction(CacheManager.ACTION_TRACK_CACHED);
			filter.addAction(CacheManager.ACTION_UPDATED_CACHE);
			filter.addAction(CacheManager.ACTION_VIDEO_CACHE_STATE_UPDATED);
			filter.addAction(CacheManager.ACTION_VIDEO_TRACK_CACHED);
			filter.addAction(CacheManager.ACTION_VIDEO_UPDATED_CACHE);
			registerReceiver(cacheStateReceiver, filter);
		}

        if (!Utils.isCarMode() && offlineModeReceiver == null/* && CacheManager.isProUser(this) */) {
            offlineModeReceiver = new OfflineModeReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(MainActivity.ACTION_OFFLINE_MODE_CHANGED);
            registerReceiver(offlineModeReceiver, filter);
        }

		Logger.s("---------------onResume---------------"
				+ getIntent().getBooleanExtra(NOTIFICATION_MAIL, false));

		if (getIntent().getExtras() != null) {
            removeFragmentOnPushNotification();
			// Push notification handling
			/*if (!set) {
				if (getIntent().getBooleanExtra("rate", false)) {
					mAppirater.rateAppClick(this);
					set = true;
				} else if (getIntent().getBooleanExtra("faq", false)) {
					Intent helpAndFaqActivityIntent = new Intent(
							getApplicationContext(), HelpAndFAQActivity.class);
					startActivity(helpAndFaqActivityIntent);
					set = true;
				} else if (getIntent().getBooleanExtra("feedback", false)) {
					Intent feedbackActivityIntent = new Intent(
							getApplicationContext(), FeedbackActivity.class);
					startActivity(feedbackActivityIntent);
					set = true;
				} else if (getIntent().getBooleanExtra("about", false)) {
					Intent aboutActivityIntent = new Intent(
							getApplicationContext(), AboutActivity.class);
					startActivity(aboutActivityIntent);
					set = true;
				} else if (getIntent().getBooleanExtra("invite", false)) {
					Intent inviteFriendsActivity = new Intent(this,
							InviteFriendsActivity.class);
					inviteFriendsActivity.putExtra(
							InviteFriendsActivity.FLURRY_SOURCE,
							FlurryConstants.FlurryInvite.GlobalMenu.toString());
					startActivity(inviteFriendsActivity);
					set = true;
				} else if (getIntent().getBooleanExtra("rewards", false)) {
					Intent redeemActivityIntent = new Intent(
							getApplicationContext(), RedeemActivity.class);
					startActivity(redeemActivityIntent);
					set = true;
				} else if (getIntent().getBooleanExtra("my_profile", false)) {
				    openProfileActivity(true);
					set = true;
				} else if (getIntent().getBooleanExtra("my_collection", false)) {
					/*Intent myCollectionActivityIntent = new Intent(
							getApplicationContext(), MyCollectionActivity.class);
					startActivity(myCollectionActivityIntent);
					removeFragmentOnPushNotification();

					set = true;
				} else if (getIntent().getBooleanExtra("my_favourites", false)) {
					/*Intent favoritesActivityIntent = new Intent(
							getApplicationContext(), FavoritesActivity.class);
					startActivity(favoritesActivityIntent);
                    FavoritesActivity mTilesFragment = new FavoritesActivity();

                    FragmentManager mFragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = mFragmentManager
                            .beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
                            R.anim.slide_left_exit, R.anim.slide_right_enter,
                            R.anim.slide_right_exit);
                    fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
							mTilesFragment, "FavoritesActivity");
                    fragmentTransaction.addToBackStack("FavoritesActivity");
                    fragmentTransaction.commitAllowingStateLoss();
					findViewById(R.id.home_browse_by_fragmant_container).setVisibility(View.VISIBLE);
                    set = true;
				} else if (getIntent().getBooleanExtra("my_playlists", false)) {
					PlaylistsActivity mTilesFragment = new PlaylistsActivity();
					FragmentManager mFragmentManager = getSupportFragmentManager();
					FragmentTransaction fragmentTransaction = mFragmentManager
							.beginTransaction();
					fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
							R.anim.slide_left_exit, R.anim.slide_right_enter,
							R.anim.slide_right_exit);
					fragmentTransaction.add(R.id.home_browse_by_fragmant_container_playlist,
							mTilesFragment, "PlayListActivity");
					fragmentTransaction.addToBackStack("PlayListActivity");
					fragmentTransaction.commitAllowingStateLoss();
					findViewById(R.id.home_browse_by_fragmant_container_playlist).setVisibility(View.VISIBLE);
					findViewById(R.id.home_browse_by_fragmant_container).setVisibility(View.VISIBLE);
					set = true;
				} else if (getIntent().getBooleanExtra("audio_latest", false)) {
                    removeFragmentOnPushNotification();
					mDeafultOpenedTab = HomeTabBar.TAB_INDEX_MUSIC;
					set = true;
				} else if (getIntent().getBooleanExtra("discover", false)) {
					if (!set) {
						if (isOnCreate)
							loadMediaItems();
                        removeFragmentOnPushNotification();
						mDeafultOpenedTab = HomeTabBar.TAB_INDEX_DISCOVER;
						onTabSelected(mDeafultOpenedTab);
						set = true;
					}
				} else if (getIntent().getBooleanExtra("live_radio", false)
						|| getIntent().getBooleanExtra("top_celebs", false)
						// || getIntent().getStringExtra("channel_index") !=
						// null
						|| getIntent().getStringExtra("radio_id") != null
						|| getIntent().getStringExtra("artist_id") != null) {
					if (!set) {
						if (isOnCreate)
							loadMediaItems();
						removeFragmentOnPushNotification();
						mDeafultOpenedTab = HomeTabBar.TAB_INDEX_RADIO;
						onTabSelected(mDeafultOpenedTab);
						set = true;
					}
				} else if (getIntent().getBooleanExtra("settings", false)) {
					Intent settingsIntent = new Intent(this,
							SettingsActivity.class);
					startActivity(settingsIntent);
					set = true;
				} else if (getIntent().getBooleanExtra("app_tour", false)) {
					Intent tourIntent = new Intent(this, AppTourActivity.class);
					startActivity(tourIntent);
					set = true;
				} else if (getIntent().getBooleanExtra("search", false)
						|| getIntent().getBooleanExtra("song_catcher", false)) {
					if (isOnCreate)
						loadMediaItems();

//					handler.postDelayed(new Runnable() {
//						@Override
//						public void run() {
							openSearch(true,true);
							//getIntent().removeExtra("song_catcher");
//						}
//					}, 1000);
					set = true;
				} else if (getIntent().getBooleanExtra("show_languages", false)) {
					Intent settingsIntent = new Intent(this,
							SettingsActivity.class);
					settingsIntent.putExtra("show_languages", true);
					startActivity(settingsIntent);
					set = true;
				} else if (getIntent()
						.getBooleanExtra("show_membership", false)) {
					Intent settingsIntent = new Intent(this,
							SettingsActivity.class);
					settingsIntent.putExtra("show_membership", true);
					startActivity(settingsIntent);
					set = true;
					getIntent().removeExtra("show_membership");
				} else if (getIntent().getBooleanExtra("video_latest", false)) {
					if (!set) {
						if (isOnCreate)
							loadMediaItems();
                        removeFragmentOnPushNotification();
						mDeafultOpenedTab = HomeTabBar.TAB_INDEX_VIDEO;
						onTabSelected(mDeafultOpenedTab);
						set = true;
					}
				} else if (getIntent().getBooleanExtra("audio_featured", false)) {

					if (!set) {
						if (isOnCreate)
							loadMediaItems();
                        removeFragmentOnPushNotification();
						mDeafultOpenedTab = HomeTabBar.TAB_INDEX_MUSIC_POPULAR;
						onTabSelected(mDeafultOpenedTab);
						set = true;
					}
				}

				Logger.s(mCurrentMediaContentType
						+ " wasInBackground ::::::::::::::::::::::: "
						+ wasInBackground);

				if (!setVideoLinks()) {
					wasInBackground = true;
					loadMediaItems();
				}
			}*/

			if (getIntent().getBooleanExtra("show_toast", false)) {
				getIntent().removeExtra("show_toast");
				Utils.makeText(this, getString(R.string.toast_online),
						Toast.LENGTH_SHORT).show();
			}

			if (getIntent().getBooleanExtra("show_onboarding", false)) {
				getIntent().removeExtra("show_onboarding");
			}
		} else {
			wasInBackground = true;
			loadMediaItems();
		}

		handleBrowserDeepLinkFirstTime();
		ThreadPoolManager.getInstance().submit(new CheckCachedTracksAvailablility(this));
	}

	private void openMyCollection() {
		MyCollectionActivity mTilesFragment = new MyCollectionActivity();

		FragmentManager mFragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit, R.anim.slide_right_enter,
				R.anim.slide_right_exit);
		fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
				mTilesFragment, "MyCollectionActivity");
		fragmentTransaction.addToBackStack("MyCollectionActivity");
		if(Constants.IS_COMMITALLOWSTATE)
			fragmentTransaction.commitAllowingStateLoss();
		else
			fragmentTransaction.commit();
		findViewById(R.id.home_browse_by_fragmant_container).setVisibility(View.VISIBLE);
	}


    private void  openFavorite() {
        FavoritesActivity mTilesFragment = new FavoritesActivity();

        FragmentManager mFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager
                .beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit, R.anim.slide_right_enter,
				R.anim.slide_right_exit);
        fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
				mTilesFragment, "FavoritesActivity");
        fragmentTransaction.addToBackStack("FavoritesActivity");
		if(Constants.IS_COMMITALLOWSTATE)
			fragmentTransaction.commitAllowingStateLoss();
		else
			fragmentTransaction.commit();
        findViewById(R.id.home_browse_by_fragmant_container).setVisibility(View.VISIBLE);
    }

    private void  openPlaylist() {
        PlaylistsActivity mTilesFragment = new PlaylistsActivity();
        FragmentManager mFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager
                .beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit, R.anim.slide_right_enter,
				R.anim.slide_right_exit);
        fragmentTransaction.add(R.id.home_browse_by_fragmant_container_playlist,
                mTilesFragment, "PlayListActivity");
        fragmentTransaction.addToBackStack("PlayListActivity");

		if(Constants.IS_COMMITALLOWSTATE)
        	fragmentTransaction.commitAllowingStateLoss();
		else
			fragmentTransaction.commit();

        findViewById(R.id.home_browse_by_fragmant_container_playlist).setVisibility(View.VISIBLE);
        findViewById(R.id.home_browse_by_fragmant_container).setVisibility(View.VISIBLE);
    }







	@Override
	public void onUserInteraction() {
		super.onUserInteraction();
	}

	/**
	 * Delete tracks & videos records from database, which are cached but not
	 * available in device memory now.
	 *
	 * Also, start caching of tracks, which got fail in previous session.
	 *
	 * @author hungama2
	 */
	private class CheckCachedTracksAvailablility implements Runnable {
		private Context context;

		public CheckCachedTracksAvailablility(Context context) {
			this.context = context;
		}

		@Override
		public void run() {
//			super.run();
			try {
				Thread.sleep(5000);
//				File externalTracksFolder = new File(context
//						.getApplicationContext().getExternalCacheDir()
//						+ "/"
//						+ CacheManager.TRACKS_FOLDER_NAME);
//				File internalTracksFolder = new File(context
//						.getApplicationContext().getCacheDir()
//						+ "/"
//						+ CacheManager.TRACKS_FOLDER_NAME);
//				if (!externalTracksFolder.exists()
//						&& !internalTracksFolder.exists()) {
//					Logger.s("CM ---------- 1");
//					DBOHandler.cleanDatabseTables(context);
//					HungamaApplication.reset();
//					Intent TrackCached = new Intent(
//							CacheManager.ACTION_CACHE_STATE_UPDATED);
//					HungamaApplication.getContext().sendBroadcast(TrackCached);
//					stopService(new Intent(context, DownloaderService.class));
//				} else {
					HungamaApplication.reset();
					Logger.s("CM ---------- 2");
					Logger.s(" :::::::::::::checkTracksAvailability:::::::::::::::  started");
					DBOHandler.checkTracksAvailability(context);
					Logger.s(" :::::::::::::checkVideoTracksAvailability:::::::::::::::  started");
					DBOHandler.checkVideoTracksAvailability(context);

					isServiceRunning();
					CacheManager.loadNotCachedTrack(context);

					Intent TrackCached = new Intent(
							CacheManager.ACTION_CACHE_STATE_UPDATED);
					HungamaApplication.getContext().sendBroadcast(TrackCached);
//				}
				Logger.s("CM ---------- 3");
			} catch (Exception e) {
				Logger.printStackTrace(e);
			} catch (Error e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Push notification handling. Open relevant content based on deeplinking.
	 *
	 * @return true if push notification handled, else false.
	 */
	private boolean setVideoLinks() {
		boolean videoIsSet = false;
		if (getIntent().getBooleanExtra("video_featured", false)) {
			loadDeepLinkContent();
			Logger.i("Failure", "video featured should open");
			set = true;
			videoIsSet = true;
		}
		if (getIntent().getBooleanExtra("video_latest", false)) {
			loadDeepLinkContent();
			Logger.i("Failure", "Video latest should open");
			set = true;
			videoIsSet = true;
		}

		if (getIntent().getBooleanExtra("audio_featured", false)) {
			loadDeepLinkContent();
			Logger.i("Failure", "Audio featured should open");
			set = true;
			videoIsSet = true;
		}
		if (getIntent().getBooleanExtra("audio_latest", false)) {
			loadDeepLinkContent();
			Logger.i("Failure", "Audio latest should open");
			set = true;
			videoIsSet = true;
		}

		if (getIntent().getBooleanExtra("fav_songs", false)
				|| getIntent().getBooleanExtra("fav_albums", false)
				|| getIntent().getBooleanExtra("fav_playlists", false)
				|| getIntent().getBooleanExtra("fav_videos", false)) {
//			Intent favoritesActivityIntent = new Intent(
//					getApplicationContext(), FavoritesActivity.class);
//			favoritesActivityIntent.putExtras(getIntent());
//			startActivity(favoritesActivityIntent);
			FavoritesActivity mTilesFragment = new FavoritesActivity();
			FragmentManager mFragmentManager = getSupportFragmentManager();
			FragmentTransaction fragmentTransaction = mFragmentManager
					.beginTransaction();
			fragmentTransaction.setCustomAnimations(R.anim.slide_and_show_bottom_enter,
					R.anim.slide_and_show_bottom_exit);
			mTilesFragment.setArguments(getIntent().getExtras());
			fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
					mTilesFragment, "FavoritesActivity");
			fragmentTransaction.addToBackStack("FavoritesActivity");
			if(Constants.IS_COMMITALLOWSTATE)
				fragmentTransaction.commitAllowingStateLoss();
			else
				fragmentTransaction.commit();
			findViewById(R.id.home_browse_by_fragmant_container).setVisibility(View.VISIBLE);
			set = true;
			videoIsSet = true;
		}
		if (getIntent().getStringExtra("video_content_id") != null
				|| getIntent().getStringExtra("audio_content_id") != null
				|| getIntent().getStringExtra("video_in_audio_content_id") != null) {
			loadDeepLinkContent();
			set = true;
			videoIsSet = true;
		}
		return videoIsSet;
	}

	@Override
	public void onDetachedFromWindow() {
		Logger.s("onDetachedFromWindow HomeScreen");
		// HungamaApplication.activityPaused();
		super.onDetachedFromWindow();
	}

	@Override
	public void onAttachedToWindow() {
		Logger.s("onAttachedToWindow HomeScreen");
		// HungamaApplication.activityResumed();
		super.onAttachedToWindow();
	}

	@Override
	protected void onPause() {
		Logger.s("onPause HomeScreen");
		HungamaApplication.activityPaused();
		/*
		 * No matter what, remove any existing dialog from the activity. Will be
		 * resumed only if the activity is visible and content is still being
		 * loaded to it.
		 */
		hideLoadingDialog();

		super.onPause();
		/*if (mCastManager!=null && isCastPlaying()) {
			startCastNotifications();
		}*/

	}

	@Override
	protected void onStop() {
		Logger.s("onStop HomeScreen");
		// HungamaApplication.activityStoped();
		mActivityStopped = true;
        PlayerServiceBindingManager.unbindFromService(mServiceToken);
        try {
            MySpinServerSDK.sharedInstance().unregisterConnectionStateListener(this);
        } catch (MySpinException e) {
            e.printStackTrace();
        }
		super.onStop();
//		Apsalar.endSession();
		Analytics.onEndSession(this);
	}

	protected void onDestroy() {
		super.onDestroy();
		Utils.unbindDrawables((RelativeLayout) findViewById(R.id.homeScreenMain));
		wasInBackground = false;
		Instance = null;
		try {
			if (cacheStateReceiver != null)
				unregisterReceiver(cacheStateReceiver);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		cacheStateReceiver = null;

		try {
			if (offlineModeReceiver != null)
				unregisterReceiver(offlineModeReceiver);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		offlineModeReceiver = null;

		try {
			if (languageChangeReceiver != null)
				unregisterReceiver(languageChangeReceiver);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		languageChangeReceiver = null;

		try {
			if (closeAppReceiver != null)
				unregisterReceiver(closeAppReceiver);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		closeAppReceiver = null;

		try {
			if (uaLinkReceiver != null)
				unregisterReceiver(uaLinkReceiver);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

		try {
			if (preference_update != null)
				unregisterReceiver(preference_update);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

		try {
			if (reciver_notify != null)
				unregisterReceiver(reciver_notify);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

		try {
			if (listener_update != null)
				unregisterReceiver(listener_update);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

		try {
			if (listener_radio_data_change != null)
				unregisterReceiver(listener_radio_data_change);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

		try {
			if (AireplaneModeReceiver != null)
				unregisterReceiver(AireplaneModeReceiver);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

		try {
			if (mTrackReloadReceiver != null)
				unregisterReceiver(mTrackReloadReceiver);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		mTrackReloadReceiver = null;

		uaLinkReceiver = null;
//		try {
//			new Thread() {
//				public void run() {
//					try {
//						DataManager.getInstance(getApplicationContext())
//								.notifyApplicationExits();
//					} catch (Exception e) {
//                        Logger.printStackTrace(e);
//					}
//				}
//			}.start();
//		} catch (Exception e) {
//		}

		if(ApsalarEvent.ENABLED){
			Apsalar.unregisterApsalarReceiver(getApplicationContext());
			Apsalar.endSession();
		}
	}

	@Override
	protected NavigationItem getNavigationItem() {
		if (mCurrentMediaContentType == MediaContentType.MUSIC) {
			return NavigationItem.MUSIC;
		}

		return NavigationItem.VIDEOS;
	}

	/**
	 * Initialize view page and load content.
	 *
	 * @param tabId
	 *            Index of page from HomeTabBar.
	 *
	 */
	private void setUpViewpager(int tabId) {
		Logger.s("HomeTime:**setUpViewpager");
		Logger.e("home activity", "setUpViewpager ");

		if (tabId == HomeTabBar.TAB_INDEX_MUSIC) {
			if (adapter == null) {
				adapter = new MyPagerAdapter(getSupportFragmentManager());
				pager.setAdapter(adapter);
				tabs.setViewPager(pager);
				pager.setCurrentItem(HomeTabBar.TAB_INDEX_MUSIC);
				DisplayHelp();
			} else {

			}
			pager.postDelayed(new Runnable() {
				@Override
				public void run() {
					pager.setCurrentItem(HomeTabBar.TAB_INDEX_MUSIC, true);
				}
			}, 10);
		} else if (tabId == HomeTabBar.TAB_INDEX_VIDEO) {
			if (adapter == null) {
				adapter = new MyPagerAdapter(getSupportFragmentManager());
				pager.setAdapter(adapter);
				tabs.setViewPager(pager);
				pager.setCurrentItem(HomeTabBar.TAB_INDEX_MUSIC);
			} else {

			}
			pager.postDelayed(new Runnable() {
				@Override
				public void run() {
					pager.setCurrentItem(HomeTabBar.TAB_INDEX_VIDEO, true);
				}
			}, 10);
		} else if (tabId == HomeTabBar.TAB_INDEX_DISCOVER) {
			if (adapter == null) {
				adapter = new MyPagerAdapter(getSupportFragmentManager());
				pager.setAdapter(adapter);
				tabs.setViewPager(pager);
			}
			pager.postDelayed(new Runnable() {
				@Override
				public void run() {
					pager.setCurrentItem(HomeTabBar.TAB_INDEX_DISCOVER, true);
				}
			}, 10);
		} else if (tabId == HomeTabBar.TAB_INDEX_RADIO) {
			if (adapter == null) {
				adapter = new MyPagerAdapter(getSupportFragmentManager());
				pager.setAdapter(adapter);
				tabs.setViewPager(pager);
			}

			pager.postDelayed(new Runnable() {
				@Override
				public void run() {
					pager.setCurrentItem(HomeTabBar.TAB_INDEX_RADIO, true);
					if (getIntent().getBooleanExtra("live_radio", false)
					// || getIntent().getStringExtra("channel_index") != null) {
							|| getIntent().getStringExtra("radio_id") != null) {
						Fragment temp = adapter
								.getCurrentFragment(HomeTabBar.TAB_INDEX_RADIO);
						if (temp != null) {
							BrowseRadioFragment radioFragment = (BrowseRadioFragment) temp;
							radioFragment.showLiveRadio();
						}
					} else if (getIntent().getBooleanExtra("top_celebs", false)
							|| getIntent().getStringExtra("artist_id") != null
							|| getIntent().getStringExtra("Station_ID") != null) {
						Fragment temp = adapter
								.getCurrentFragment(HomeTabBar.TAB_INDEX_RADIO);
						if (temp != null) {
							BrowseRadioFragment radioFragment = (BrowseRadioFragment) temp;
							radioFragment.showArtistRadio();
							// radioFragment.showLiveRadio();
						}
					}
					getIntent().removeExtra("live_radio");
					getIntent().removeExtra("top_celebs");
					getIntent().removeExtra("radio_id");
					getIntent().removeExtra("artist_id");
					getIntent().removeExtra("Station_ID");
				}
			}, 10);
		} else if (tabId == HomeTabBar.TAB_INDEX_MUSIC_POPULAR) {
			if (adapter == null) {
				adapter = new MyPagerAdapter(getSupportFragmentManager());
				pager.setAdapter(adapter);
				tabs.setViewPager(pager);
			} else {

			}
			pager.postDelayed(new Runnable() {
				@Override
				public void run() {
					pager.setCurrentItem(HomeTabBar.TAB_INDEX_MUSIC_POPULAR,
							true);
				}
			}, 10);
		}

		tabs.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(final int arg0) {
				final String[] sectionName = {FlurryConstants.FlurryNavigation.MusicNew
						.toString()};
				if (arg0 == HomeTabBar.TAB_INDEX_MUSIC_POPULAR) {

					mCurrentMediaContentType = MediaContentType.MUSIC;
					mDeafultOpenedTab = HomeTabBar.TAB_INDEX_MUSIC_POPULAR;
					showSatelliteMenu();
//					showCategoryActionBar();
				/*	sectionName[0] = FlurryConstants.FlurryNavigation.MusicPopular
							.toString();*/
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
//							showSatelliteMenu();
//							showCategoryActionBar();
							sectionName[0] = FlurryConstants.FlurryNavigation.MusicPopular
									.toString();
							if (adapter != null) {
								HomeMediaTileGridFragmentNew temp = (HomeMediaTileGridFragmentNew) adapter
										.getCurrentFragment(arg0);
								if (temp != null)
									temp.postAd();
							}
							//castMenuItemHideShow(true);
//							showPromoUnit();
						}
					}, 100);
//					if (adapter != null) {
//						HomeMediaTileGridFragmentNew temp = (HomeMediaTileGridFragmentNew) adapter
//								.getCurrentFragment(arg0);
//						if (temp != null)
//							temp.postAd();
//					}

//					castMenuItemHideShow(true);
				} else if (arg0 == HomeTabBar.TAB_INDEX_MUSIC) {

					mCurrentMediaContentType = MediaContentType.MUSIC;
					mDeafultOpenedTab = HomeTabBar.TAB_INDEX_MUSIC;
					showSatelliteMenu();
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
//							showSatelliteMenu();
//							showCategoryActionBar();
							sectionName[0] = FlurryConstants.FlurryNavigation.MusicNew
									.toString();
							if (adapter != null) {
								HomeMediaTileGridFragmentNew temp = (HomeMediaTileGridFragmentNew) adapter
										.getCurrentFragment(arg0);
								if (temp != null)
									temp.postAd();
							}
							//castMenuItemHideShow(true);
						}
					}, 100);


				} else if (arg0 == HomeTabBar.TAB_INDEX_RADIO) {
					mDeafultOpenedTab = HomeTabBar.TAB_ID_RADIO;
					hideSatelliteMenu();
//					showCategoryActionBar();
					sectionName[0] = FlurryConstants.FlurryNavigation.Radio
							.toString();
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							if (adapter != null) {
								BrowseRadioFragment temp = (BrowseRadioFragment) adapter
										.getCurrentFragment(arg0);
								if (temp != null) {
									temp.setGridView();
									temp.postAd();
								}
							}
							//castMenuItemHideShow(true);
						}
					}, 100);
				} else if (arg0 == HomeTabBar.TAB_INDEX_VIDEO) {
					mCurrentMediaContentType = MediaContentType.VIDEO;
					mDeafultOpenedTab = HomeTabBar.TAB_INDEX_VIDEO;
					hideSatelliteMenu();

					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
//							showCategoryActionBar();
							sectionName[0] = FlurryConstants.FlurryNavigation.Video
									.toString();
							if (adapter != null) {
								HomeMediaTileGridFragmentVideo temp = (HomeMediaTileGridFragmentVideo) adapter
										.getCurrentFragment(arg0);
								if (temp != null)
									temp.postAd();
							}
							//castMenuItemHideShow(false);
						}
					}, 100);


				} else if (arg0 == HomeTabBar.TAB_INDEX_DISCOVER) {
					mDeafultOpenedTab = HomeTabBar.TAB_ID_DISCOVER;
					hideSatelliteMenu();
//					showCategoryActionBar();
					sectionName[0] = FlurryConstants.FlurryNavigation.Discover
							.toString();
					//castMenuItemHideShow(true);
				}

				if (arg0 == HomeTabBar.TAB_INDEX_DISCOVER) {

					if (adapter != null) {
						RootFragmentDiscovery fragmentDiscover = (RootFragmentDiscovery) adapter
								.getCurrentFragment(arg0);
						if (fragmentDiscover != null
								&& fragmentDiscover.fragment != null) {

							if (PlayerService.service != null
									/*&& PlayerService.service.isPlaying()*/
									&& PlayerService.service.getPlayMode() != PlayMode.DISCOVERY_MUSIC) {
								if (fragmentDiscover.fragment.isDiskRunning()) {
									fragmentDiscover.fragment.iv_Disc
											.setTag(fragmentDiscover.fragment.tag_running);
									//comment temp
//									fragmentDiscover.fragment
//											.StopPlaybackAnim(true);
								}
							}
						}
					}
				} else {
					if (adapter != null) {
						RootFragmentDiscovery fragmentDiscover = (RootFragmentDiscovery) adapter
								.getCurrentFragment(HomeTabBar.TAB_INDEX_DISCOVER);
						if (fragmentDiscover != null
								&& fragmentDiscover.fragment != null) {

							if (fragmentDiscover.fragment.isDiskRunning()) {
								//comment temp
//								fragmentDiscover.fragment
//										.stopCircleAnimation(false);
							}
						}
					}
				}

				Map<String, String> reportMap1 = new HashMap<String, String>();
				reportMap1.put(
						FlurryConstants.FlurryNavigation.NameOfTheSection
								.toString(), sectionName[0]);
				Analytics.logEvent(
						FlurryConstants.FlurryNavigation.SwipableTabs
								.toString(), reportMap1);

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}

		});

		Logger.s("HomeTime:**setUpViewpager  xxx");
		// showHelpView();
	}

	/**
	 * Switch to selected page.
	 *
	 * @param tabId
	 *            Index of page from HomeTabBar.
	 */
	private void onTabSelected(int tabId) {
		try {
			if (isActivityDestroyed()) {
				return;
			}
			setUpViewpager(tabId);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

    public void removeCurrentFragment()
    {
//        getSupportFragmentManager().popBackStack();
//		try{
//			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//			Fragment currentFrag =  getSupportFragmentManager().findFragmentByTag("test");
//
//			if(currentFrag != null){
//				transaction.remove(currentFrag);
//			}
//			transaction.commit();
//		}catch (Exception e){
//		}
//		FrameLayout layout= (FrameLayout) findViewById(R.id.home_browse_by_fragmant_container);
//		layout.removeAllViewsInLayout();

    }

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
	public void onBackPressed() {
		Utils.clearCache();

        int count = getSupportFragmentManager()
                .getBackStackEntryCount();
        Logger.e("count:*","count**"+count);
      if(selectedFragment == null || !selectedFragment.onBackPressed()) {
            // Selected fragment did not consume the back press event.
            // close drawer if open
            if (mDrawerLayout != null
                    && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawers();
                return;
            }
            if (findViewById(R.id.progressbar).getVisibility() == View.VISIBLE) {
                try {
                    findViewById(R.id.progressbar).setVisibility(View.GONE);
                } catch (Exception e) {

                }
                return;
            }

            HomeActivity.set = false;

            if (isAnyActionBarOptionSelected()) {
                super.onBackPressed();
                return;
            } else if (mPlayerBar.isContentOpened()) {
                // Minimize player
                if (!mPlayerBar.removeAllFragments())
                    mPlayerBar.closeContent();
            } else {
                if (!mPlayerBar.isPlayingForExit()) {
                    // stops playing any media.
                    Logger.w(
                            TAG,
                            "################# explicit stopping the service, Ahhhhhhhhhhhhhhhhhhh #################");
                    mPlayerBar.explicitStop();

                    // reset the inner boolean for showing home tile hints.
                    mApplicationConfigurations
                            .setIsHomeHintShownInThisSession(false);
                    mApplicationConfigurations
                            .setIsSearchFilterShownInThisSession(false);
                    mApplicationConfigurations
                            .setIsPlayerQueueHintShownInThisSession(false);
                    // reset the version check for checking for new version of the
                    // app
                    mApplicationConfigurations.setisVersionChecked(false);
                    // if this button is clicked, close
                    // current activity
                    if (!CacheManager.isProUser(mContext)) {
                        // DBOHandler.deleteCacedTrack(mContext, null);
                    }

                    if (mCampaignsManager != null)
                        mCampaignsManager.clearInstance();
                    // HomeActivity.super.onBackPressed();
                    mPlayerBar.stopTriviaTimer();

                    DBOHandler.clearMAP();
                    Intent intentStop = new Intent(getBaseContext(),
                            PlayerUpdateWidgetService.class);
                    intentStop.putExtra(PlayerUpdateWidgetService.EXTRA_COMMAND,
                            PlayerUpdateWidgetService.EXTRA_STOP_SERVICE);
                    startService(intentStop);

                    try {
                        DataManager.getInstance(getApplicationContext())
                                .notifyApplicationExits();
                    } catch (Exception e) {
                        Logger.printStackTrace(e);
                    }

                    DBOHandler.exportDB(this);

                    ScreenLockStatus.getInstance(getBaseContext()).dontShowAd();
                    android.os.Process.sendSignal(android.os.Process.myPid(),
                            android.os.Process.SIGNAL_QUIT);
                    HomeActivity.this.finish();
                } else {
                    ScreenLockStatus.getInstance(getBaseContext()).onStop(true,
                            this);
                    mPlayerBar.stopTriviaTimer();
                    moveTaskToBack(true);
					PicassoUtil.clearCache();
					Utils.clearCache(true);
				}
            }
        }
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == MY_PREFERENCES_ACTIVITY_RESULT_CODE
				&& resultCode == RESULT_OK && data != null) {
			if (data.getExtras().getBoolean(EXTRA_MY_PREFERENCES_IS_CHANGED)) {
				String selectedLanguage = mApplicationConfigurations
						.getSeletedPreferences();
				if (!TextUtils.isEmpty(selectedLanguage)) {
					Map<String, String> reportMap = new HashMap<String, String>();
					reportMap.put(FlurryConstants.FlurryKeys.LanguageSelected
							.toString(), selectedLanguage);
					Analytics
							.logEvent(FlurryConstants.FlurryEventName.BrowseBy
									.toString(), reportMap);
				}

				Intent reStartHomeActivity = new Intent(
						getApplicationContext(), HomeActivity.class);
				reStartHomeActivity.putExtra(
						HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE,
						(Serializable) MediaContentType.MUSIC);
				reStartHomeActivity
						.putExtra(
								HomeActivity.ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION,
								HomeTabBar.TAB_ID_LATEST);
				reStartHomeActivity.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
						| Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(reStartHomeActivity);
			}
		} else if (requestCode == ACTIVITY_GUIDE_RESULT_CODE) {
			CustomAlertDialog boardingAlert = new CustomAlertDialog(this);
			boardingAlert.setCancelable(false);
			boardingAlert.setTitle(Utils.getMultilanguageText(mContext,
					getResources().getString(R.string.on_boarding_title)));
			boardingAlert.setMessage(Utils.getMultilanguageText(mContext,
					getResources().getString(R.string.on_boarding_message)));
			boardingAlert.setNegativeButton(
					Utils.getMultilanguageText(mContext, getResources()
							.getString(R.string.on_boarding_button_text)),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (mApplicationConfigurations
									.isFirstTimeAppLaunch()) {
								mApplicationConfigurations
										.setIsFirstTimeAppLaunch(false);
							}
						}
					});
			boardingAlert.show();
		} else if (requestCode == LOGIN_ACTIVITY_CODE
				&& resultCode == RESULT_OK) {

			String session = mDataManager.getApplicationConfigurations()
					.getSessionID();
			Boolean isRealUser = mDataManager.getApplicationConfigurations()
					.isRealUser();
			if (!TextUtils.isEmpty(session) && (isRealUser || Logger.allowPlanForSilentUser)) {
				String accountType = Utils
						.getAccountName(getApplicationContext());
				mDataManager.getCurrentSubscriptionPlan(this, accountType);
			} else {
				Toast toast = Utils
						.makeText(
								this,
								getResources().getString(
										R.string.before_upgrade_login),
								Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER_VERTICAL
						| Gravity.CENTER_HORIZONTAL, 0, 0);
				toast.show();
			}
		} else if (requestCode == FILTER_OPTIONS_ACTIVITY_RESULT_CODE
				&& resultCode == RESULT_OK) {
			// Reload content
			if (data != null && data.hasExtra("isChangeGenre")) {
				try {
					Fragment temp = adapter
							.getCurrentFragment(HomeTabBar.TAB_INDEX_MUSIC);
					if (temp != null) {
						HomeMediaTileGridFragmentNew musicFragment = (HomeMediaTileGridFragmentNew) temp;
						musicFragment.onRefresh();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					Fragment temp = adapter
							.getCurrentFragment(HomeTabBar.TAB_INDEX_MUSIC_POPULAR);
					if (temp != null) {
						HomeMediaTileGridFragmentNew musicFragment = (HomeMediaTileGridFragmentNew) temp;
						musicFragment.onRefresh();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					Fragment temp = adapter
							.getCurrentFragment(HomeTabBar.TAB_INDEX_MUSIC);
					if (temp != null) {
						HomeMediaTileGridFragmentNew musicFragment = (HomeMediaTileGridFragmentNew) temp;
						musicFragment.RefillmediaItems();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					Fragment temp = adapter
							.getCurrentFragment(HomeTabBar.TAB_INDEX_MUSIC_POPULAR);
					if (temp != null) {
						HomeMediaTileGridFragmentNew musicFragment = (HomeMediaTileGridFragmentNew) temp;
						musicFragment.RefillmediaItems();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} else if (requestCode == HELP_ACTIVITY_CODE) {
		}
	}

	// ======================================================
	// Helper Methods
	// ======================================================

	private void initializeUserControls() {
		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		pager = (ViewPager) findViewById(R.id.pager);
		pager.setOffscreenPageLimit(4);
		tabs.setTextColor(getResources().getColor(R.color.white_transparant));
		tabs.setTextSize((int) getResources().getDimension(
				R.dimen.xlarge_text_size));
		tabs.setIndicatorColor(getResources().getColor(R.color.white));
		tabs.setUnderlineColor(getResources().getColor(R.color.white));
		tabs.setActivateTextColor(getResources().getColor(R.color.white));
		tabs.setDeactivateTextColor(getResources().getColor(
				R.color.white_transparant));
		tabs.setTabSwitch(true);
		tabs.setDividerColor(getResources().getColor(R.color.transparent));
		tabs.setUnderlineHeight(0);
		tabs.setIndicatorHeight(7);
	}

	/**
	 * Load Media details. Kept for testing purpose as it was getting used for
	 * push deep linking.
	 */
	private void loadMediaItems() {

		if (isOnCreate) {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					loadDeepLinkContent();
				}
			}, 200);
		}
	}

	/**
	 * Load Media details. Kept for testing purpose as it was getting used for
	 * push deep linking.
	 */
	private void loadDeepLinkContent() {

		MediaType filterType = null;
		if (getIntent().getStringExtra(IntentReceiver.CONTENT_TYPE) != null) {
			if (getIntent().getStringExtra(IntentReceiver.CONTENT_TYPE).equals(
					"0")) {
				filterType = MediaType.TRACK;
			} else if (getIntent().getStringExtra(IntentReceiver.CONTENT_TYPE)
					.equals("1")) {
				filterType = MediaType.ALBUM;
			} else if (getIntent().getStringExtra(IntentReceiver.CONTENT_TYPE)
					.equals("2")) {
				filterType = MediaType.PLAYLIST;
			}
			Logger.s(" :::::: content_type :::::::::::::: "
					+ getIntent().getStringExtra(IntentReceiver.CONTENT_TYPE));
		}

		if (getIntent().getStringExtra("Category") != null) {
			if (getIntent().getStringExtra("Category").equals("0")) {
				filterType = MediaType.TRACK;
			} else if (getIntent().getStringExtra("Category").equals("1")) {
				filterType = MediaType.ALBUM;
			} else if (getIntent().getStringExtra("Category").equals("2")) {
				filterType = MediaType.PLAYLIST;
			}
		}

		// fetch id from intent and pass it to home activity for deeplinking
		long id = 0;
		try {
			if (getIntent().getStringExtra("video_content_id") != null) {
				id = Long.parseLong(getIntent().getStringExtra(
						"video_content_id"));
			} else if (getIntent().getStringExtra("audio_content_id") != null) {
				id = Long.parseLong(getIntent().getStringExtra(
						"audio_content_id"));
			} else if (getIntent().getStringExtra("video_in_audio_content_id") != null) {
				id = Long.parseLong(getIntent().getStringExtra(
						"video_in_audio_content_id"));
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		if (id != 0) {
			if (getIntent().getStringExtra("video_content_id") != null) {
				try {
					MediaItem tempMedia = new MediaItem(id, "", "", "", "", "",
							"video", 0, 0);
					tempMedia.setMediaContentType(MediaContentType.VIDEO);
					onMediaItemOptionShowDetailsSelected(tempMedia, -1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (getIntent().getStringExtra("audio_content_id") != null
					&& AlertActivity.isMessage) {
				try {
					if (filterType == null)
						filterType = MediaType.TRACK;
					try {
						MediaItem tempMedia = new MediaItem(id, "", "", "", "",
								"", filterType.toString(), 0, 0);
						tempMedia.setMediaContentType(MediaContentType.MUSIC);
						onMediaItemOptionShowDetailsSelected(tempMedia, -1);
					} catch (Exception e) {
					}
				} catch (Exception e) {
				}
				AlertActivity.isMessage = false;
			} else if (getIntent().getStringExtra("video_in_audio_content_id") != null
					&& !HomeActivity.videoInAlbumSet) {
				if (filterType == null)
					filterType = MediaType.TRACK;
				try {
					MediaItem tempMedia = new MediaItem(id, "", "", "", "", "",
							filterType.toString(), -1, 0);
					tempMedia.setMediaContentType(MediaContentType.MUSIC);
					onMediaItemOptionShowDetailsSelected(tempMedia, -1);
				} catch (Exception e) {
				}
			}
		}
	}

	private boolean isShowcaseShowing = false;

	/**
	 * Show showcase view for Music Preferences
	 */
	private void openAppGuideActivity() {
		try {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (!isShowcaseShowing && !Utils.isCarMode()) {
						isShowcaseShowing = true;
						RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(
								ViewGroup.LayoutParams.WRAP_CONTENT,
								ViewGroup.LayoutParams.WRAP_CONTENT);
						lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
						lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
						int margin = ((Number) (getResources()
								.getDisplayMetrics().density * 12)).intValue();
						lps.setMargins(2 * margin, margin, margin,
								mDataManager.getShowcaseButtonMargin() * margin);

						ViewTarget target = new ViewTarget(
								findViewById(R.id.btn_preferences));
						ShowcaseView sv = new ShowcaseView.Builder(
								HomeActivity.this, false)
								.setTarget(target)
								.setContentTitle(
										R.string.showcase_preferences_title)
								.setContentText(
										R.string.showcase_preferences_message)
								.setStyle(R.style.CustomShowcaseTheme2)
								.setShowcaseEventListener(HomeActivity.this)
								.hideOnTouchOutside().build();
						sv.setBlockShowCaseTouches(true);
						sv.setButtonPosition(lps);
						mApplicationConfigurations
								.setIsEnabledHomeGuidePage(false);
					}
				}
			}, 300);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	/**
	 * Show app update dialog if new version available.
	 */
	private void showUpdateDialog() {
		CustomAlertDialog alertDialogBuilder = new CustomAlertDialog(this);

		// set title
		alertDialogBuilder.setTitle(Utils.getMultilanguageText(mContext,
				getResources().getString(R.string.new_version_title)));

		// set dialog message
		alertDialogBuilder
				.setMessage(
						Utils.getMultilanguageText(mContext, getResources()
								.getString(R.string.new_version_message)))
				.setCancelable(true)
				.setPositiveButton(
						Utils.getMultilanguageText(mContext, getResources()
								.getString(R.string.upgrade_now_button)),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Intent browserIntent = new Intent(
										Intent.ACTION_VIEW,
										Uri.parse(getResources().getString(
												R.string.google_play_url)));
								startActivity(browserIntent);

							}
						})
				.setNegativeButton(
						Utils.getMultilanguageText(mContext, getResources()
								.getString(R.string.remind_me_later_button)),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// if this button is clicked, just close
								// the dialog box and do nothing
								dialog.cancel();
							}
						});
		alertDialogBuilder.show();
	}

	/**
	 * Show app update dialog if new version available. It will be customized
	 * based on api response.
	 *
	 * @param newVersionCheckResponse
	 *            response from NEW_VERSION_CHECK api.
	 */
	private void showNewUpdateDialog(
			final NewVersionCheckResponse newVersionCheckResponse) {
		try {
			CustomAlertDialog alertDialogBuilder = new CustomAlertDialog(
					HomeActivity.this);

			// set title
			alertDialogBuilder.setTitle(Utils.getMultilanguageText(mContext,
					getResources().getString(R.string.new_version_title)));

			// set dialog message
			alertDialogBuilder.setMessage(Utils.getMultilanguageText(mContext,
					getResources().getString(R.string.new_version_message)));
			if (newVersionCheckResponse.isMandatory()) {
				alertDialogBuilder.setCancelable(false);
			} else {
				alertDialogBuilder.setCancelable(true);
			}

			alertDialogBuilder.setPositiveButton(Utils.getMultilanguageText(
					mContext,
					getResources().getString(R.string.upgrade_now_button)),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							if (!newVersionCheckResponse.getUrl().startsWith(
									"http")) {
								Intent browserIntent = new Intent(
										Intent.ACTION_VIEW,
										Uri.parse(getResources().getString(
												R.string.google_play_url)));
								startActivity(browserIntent);
							} else {
								Intent browserIntent = new Intent(
										Intent.ACTION_VIEW, Uri
												.parse(newVersionCheckResponse
														.getUrl()));
								startActivity(browserIntent);
							}

						}
					});
			if (!newVersionCheckResponse.isMandatory()) {
				alertDialogBuilder.setNegativeButton(Utils
						.getMultilanguageText(mContext, getResources()
								.getString(R.string.remind_me_later_button)),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// if this button is clicked, just close
								// the dialog box and do nothing
								dialog.cancel();
							}
						});
			}
			alertDialogBuilder.show();
		} catch (Exception e) {
		} catch (Error e) {
		}
	}


	// ======================================================
	// Communication Operation listeners.
	// ======================================================

	@Override
	public void onStart(int operationId) {
		if (mDeafultOpenedTab == HomeTabBar.TAB_ID_DISCOVER
				|| mDeafultOpenedTab == HomeTabBar.TAB_ID_RADIO) {
			return;
		}
		if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
			mOperationsList.add(operationId);
		} else if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_CATEGORIES) {
			mOperationsList.add(operationId);
		} else if (operationId == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS_SONGS
				|| operationId == OperationDefinition.Hungama.OperationId.RADIO_LIVE_STATION_DETAIL) {
			showLoadingDialog(R.string.application_dialog_loading_content);
			mOperationsList.add(operationId);
		}else if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_STREAMING_ADP) {
            //showLoadingDialog(R.string.application_dialog_loading_content);
            //mOperationsList.add(operationId);
        }
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {

			Logger.s("1 HomeTime:   onSuccess" + operationId);

			// mIsLoading = false;
			if (operationId == OperationDefinition.Hungama.OperationId.VERSION_CHECK) {
				// showRegIdDialog(); // for testing
				VersionCheckResponse versionCheckResponse = (VersionCheckResponse) responseObjects
						.get(VersionCheckOperation.RESPONSE_KEY_VERSION_CHECK);
				if (versionCheckResponse != null) {
					mApplicationConfigurations.setisVersionChecked(true);
					if (!versionCheckResponse.getVersion().equalsIgnoreCase(
							mDataManager.getServerConfigurations()
									.getAppVersion())
							&& !getIntent().getBooleanExtra(NOTIFICATION_MAIL,
									false)) {
						showUpdateDialog();
					}
				}

			} else if (operationId == OperationDefinition.Hungama.OperationId.NEW_VERSION_CHECK) {
				NewVersionCheckResponse newVersionCheckResponse = (NewVersionCheckResponse) responseObjects
						.get(NewVersionCheckOperation.RESPONSE_KEY_VERSION_CHECK);
				if (newVersionCheckResponse != null) {
					if (!newVersionCheckResponse.isMandatory()) {
						mApplicationConfigurations.setisVersionChecked(true);
					}
					if (!getIntent().getBooleanExtra(NOTIFICATION_MAIL, false))
						showNewUpdateDialog(newVersionCheckResponse);
				}

			} else if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
				try {
					try {
						findViewById(R.id.progressbar).setVisibility(View.GONE);
					} catch (Exception e) {
					}
					// findViewById(R.id.progressbar).setVisibility(View.GONE);
					Logger.i("MediaTilesAdapter",
							"Play button click: Media detail OnSuccess 4");
					MediaItem mediaItem = (MediaItem) responseObjects
							.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_ITEM);

					if (mediaItem != null
							&& (mediaItem.getMediaType() == MediaType.ALBUM || mediaItem
									.getMediaType() == MediaType.PLAYLIST)) {
						MediaSetDetails setDetails = (MediaSetDetails) responseObjects
								.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);
						PlayerOption playerOptions = (PlayerOption) responseObjects
								.get(MediaDetailsOperation.RESPONSE_KEY_PLAYER_OPTION);
						Logger.i("MediaTilesAdapter",
								"Play button click: Media detail OnSuccess 5");
						List<Track> tracks = setDetails.getTracks();
						if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
							for (Track track : tracks) {
								track.setTag(mediaItem);
							}
						} else if (mediaItem.getMediaType() == MediaType.ALBUM) {
							for (Track track : tracks) {
								track.setAlbumId(setDetails.getContentId());
							}
						}
						Logger.i("MediaTilesAdapter",
								"Play button click: Media detail OnSuccess 6");
						if (playerOptions == PlayerOption.OPTION_PLAY_NOW) {
							mPlayerBar.playNow(tracks, null, null);

						} else if (playerOptions == PlayerOption.OPTION_PLAY_NOW_AND_OPEN) {
							mPlayerBar.playNow(tracks, null, null);
							if (lastDetailedPosition != -5) {
								onMediaItemOptionShowDetailsSelected(mediaItem,
										lastDetailedPosition);
								lastDetailedPosition = -5;
							}
						} else if (playerOptions == PlayerOption.OPTION_PLAY_NEXT) {
							mPlayerBar.playNext(tracks);

						} else if (playerOptions == PlayerOption.OPTION_ADD_TO_QUEUE) {
							mPlayerBar.addToQueue(tracks, null, null);
						} else if (playerOptions == PlayerOption.OPTION_SAVE_OFFLINE) {
							if (mediaItem.getMediaType() == MediaType.ALBUM) {
								for (Track track : tracks) {
									track.setTag(mediaItem);
								}
							}
							CacheManager.saveAllTracksOfflineAction(this,
									tracks);
						}
					}
				} catch (Exception e) {
					Logger.e(getClass().getName() + ":438", e.toString());
				}

				if (!mActivityStopped) {
					hideLoadingDialog();
				}

			} else if (operationId == OperationDefinition.Hungama.OperationId.SUBSCRIPTION_CHECK) {
					SubscriptionStatusResponse subscriptionsubscriptionStatusResponse = (SubscriptionStatusResponse) responseObjects
							.get(SubscriptionCheckOperation.RESPONSE_KEY_SUBSCRIPTION_CHECK);
					if (subscriptionsubscriptionStatusResponse != null) {
						if (subscriptionsubscriptionStatusResponse.getSubscription()!=null &&
								subscriptionsubscriptionStatusResponse.getSubscription().getSubscriptionStatus() == 1) {
							if (!mHasSubscriptionPlan && mApplicationConfigurations.isUserHasSubscriptionPlan()) {
								mHasSubscriptionPlan = true;
							}

//							if (subscriptionsubscriptionStatusResponse.getPlan().isTrial()) {
//								Utils.makeText(
//										this,
//										subscriptionsubscriptionStatusResponse.getPlan()
//												.getTrailExpiryDaysLeft()
//												+ Utils.getMultilanguageText(
//												mContext, " days left."),
//										Toast.LENGTH_SHORT).show();
//							}
						}
					}
			} else if (operationId == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS_SONGS) {
				try {
					// gets the radio tracks
					List<Track> radioTracks = (List<Track>) responseObjects
							.get(RadioTopArtistSongsOperation.RESULT_KEY_OBJECT_TRACKS);
					MediaItem mediaItem = (MediaItem) responseObjects
							.get(RadioTopArtistSongsOperation.RESULT_KEY_OBJECT_MEDIA_ITEM);
					int userFav = (Integer) responseObjects
							.get(RadioTopArtistSongsOperation.RESULT_KEY_OBJECT_USER_FAVORITE);
					/*
					 * sets to each track a reference to a copy of the original
					 * radio item. This to make sure that the player bar can get
					 * source Radio item without leaking this activity!
					 */
					for (Track track : radioTracks) {
						track.setTag(mediaItem);
					}
					// starts to play.
					PlayerBarFragment.setArtistRadioId(mediaItem.getId());
					PlayerBarFragment.setArtistUserFav(userFav);
					PlayerBarFragment playerBar = getPlayerBar();
					playerBar
							.playRadio(radioTracks, PlayMode.TOP_ARTISTS_RADIO);
                    mDeafultOpenedTab = HomeTabBar.TAB_INDEX_RADIO;
                    onTabSelected(mDeafultOpenedTab);

				} catch (Exception e) {
				}
				hideLoadingDialog();
			} else if (operationId == OperationDefinition.Hungama.OperationId.RADIO_LIVE_STATION_DETAIL) {
				try {
					MediaItem mediaItem = (MediaItem) responseObjects
							.get(LiveRadioDetailsOperation.RESULT_KEY_OBJECT_MEDIA_ITEM);
					if (mediaItem != null)
						playLiveStation(mediaItem);
                    mDeafultOpenedTab = HomeTabBar.TAB_INDEX_RADIO;
                    onTabSelected(mDeafultOpenedTab);
				} catch (Exception e) {
				}
				hideLoadingDialog();
			} else if (operationId == OperationDefinition.Hungama.OperationId.GET_PROMO_UNIT) {
				try {
					mPromoUnit = (PromoUnit) responseObjects
							.get(GetPromoUnitOperation.RESPONSE_KEY_PROMO_UNIT);
					checkPromoUnitConditions(mPromoUnit);
				} catch (Exception e) {
				}
			}else if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_STREAMING_ADP) {
				Logger.i("API Call", "API call3");

				/*Video video = (Video) responseObjects
						.get(VideoStreamingOperationAdp.RESPONSE_KEY_VIDEO_STREAMING_ADP);

				//mDataManager.getMediaDetails(tempMediaItem, null, this);
				String[] images = ImagesManager.getImagesUrlArray(
						tempMediaItem.getImagesUrlArray(), ImagesManager.HOME_VIDEO_TILE,
						DataManager.getDisplayDensityLabel());
				String url = "";
				if (images != null && images.length > 0) {
					url = images[0];
				}

				MediaInfo mediaInfo = Utils.buildMediaInfo(tempMediaItem.getTitle(), tempMediaItem.getTitle(), tempMediaItem.getTitle(),
						0, video.getVideoUrl(), Utils.getVideoMimeType()    *//* "application/x-mpegurl" *//*, url, tempMediaItem.getId(), HomeActivity.this, video.getDeliveryId());
				hideLoadingDialog();

                if (isFirstVideoCast) {
                    loadRemoteMedia(0, true, mediaInfo);
                    isFirstVideoCast = false;
                } else {
					try {
						JSONObject customData = new JSONObject();
						String mediaId = tempMediaItem.getId() + "";
						customData.put(ITEM_ID, mediaId);
						customData.put(IS_VIDEO, "1");
						MediaQueueItem.Builder item = new MediaQueueItem.Builder(mediaInfo);
						mCastManager.queueAppendItem(item.build(), null);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if(mCastManager!=null){

				}*/
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

		mOperationsList.remove(Integer.valueOf(operationId));
		if (mOperationsList.isEmpty()) {
			if (!mActivityStopped) {
				hideLoadingDialog();
			}
		}
	}

	private void checkPromoUnitConditions(PromoUnit promoUnit) {
		if (promoUnit != null) {
			boolean showPromoUnit = false;
			// All Users, Logged in User, Non Logged Users, Free Trial Users,
			// Free User, Pro User
			if (promoUnit.getShow_profile().contains("All Users")) {
				showPromoUnit = true;
			} else if (mApplicationConfigurations.isuserLoggedIn()
					&& promoUnit.getShow_profile().contains("Logged in User")) {
				if (CacheManager.isProUser(HomeActivity.this)){
					if(promoUnit.getShow_profile().contains("Pro User")) {
						showPromoUnit = true;
					}
				} else if (CacheManager.isTrialUser(HomeActivity.this)){
					if(promoUnit.getShow_profile().contains(
							"Free Trial Users")) {
						showPromoUnit = true;
					}
				} else {
					if(promoUnit.getShow_profile().contains(
							"FREE Users")) {
						showPromoUnit = true;
					}
				}
			} else if (!mApplicationConfigurations.isuserLoggedIn()
					&& promoUnit.getShow_profile().contains("Non Logged Users")) {
				if (CacheManager.isProUser(HomeActivity.this)){
					if(promoUnit.getShow_profile().contains("Pro User")) {
						showPromoUnit = true;
					}
				} else {
					showPromoUnit = true;
				}
			}

			if (showPromoUnit
					&& promoUnit.getShow_category().contains(
							mApplicationConfigurations
									.getSelctedMusicPreference())) {
				showPromoUnit = true;
			} else {
				showPromoUnit = false;
			}

			if (showPromoUnit
					&& promoUnit.getShow_language().contains(
							mApplicationConfigurations
									.getUserSelectedLanguageText())) {
				showPromoUnit = true;
			} else {
				showPromoUnit = false;
			}

			if (showPromoUnit) {
				needToShowPromoUnit = true;
				// mPromoUnit = promoUnit;
				showPromoUnit();
			} else {
				// mPromoUnit = null;
				needToShowPromoUnit = false;

			}
		}
	}

	private boolean needToShowPromoUnit = false;
	private PromoUnit mPromoUnit;

	public boolean isPromoUnit() {
		// if(mPromoUnit!=null){
		// return true;
		// }
		// return false;
		return needToShowPromoUnit;
	}

	private void showPromoUnit() {
		if (mPromoUnit != null && needToShowPromoUnit) {
			HomeMediaTileGridFragmentNew musicFragment = (HomeMediaTileGridFragmentNew) adapter
					.getCurrentFragment(HomeTabBar.TAB_INDEX_MUSIC);
			if (musicFragment != null) {
				musicFragment.setPromoUnit(mPromoUnit);
			}

			HomeMediaTileGridFragmentNew musicPopularFragment = (HomeMediaTileGridFragmentNew) adapter
					.getCurrentFragment(HomeTabBar.TAB_INDEX_MUSIC_POPULAR);
			if (musicPopularFragment != null) {
				musicPopularFragment.setPromoUnit(mPromoUnit);
			}

			HomeMediaTileGridFragmentVideo videoFragment = (HomeMediaTileGridFragmentVideo) adapter
					.getCurrentFragment(HomeTabBar.TAB_INDEX_VIDEO);
			if (videoFragment != null) {
				videoFragment.setPromoUnit(mPromoUnit);
			}

			try {
				Fragment temp = adapter
						.getCurrentFragment(HomeTabBar.TAB_INDEX_RADIO);
				if (temp != null) {
					BrowseRadioFragment radioFragment = (BrowseRadioFragment) temp;
					radioFragment.setPromoUnit(mPromoUnit,
							radioFragment.mMediaItemsDisplay);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}


		}
	}

	private boolean isAutoSaveOfflineEnabled = false;

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		// mIsLoading = false;
		try {
			findViewById(R.id.progressbar).setVisibility(View.GONE);
		} catch (Exception e) {
		}
		Logger.e(TAG, "Failed to load media content " + errorMessage);

		if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS
				|| operationId == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS_SONGS
				|| operationId == OperationDefinition.Hungama.OperationId.RADIO_LIVE_STATION_DETAIL) {
			Logger.i(TAG, "Failed loading media details");
			internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
				@Override
				public void onRetryButtonClicked() {
					mDataManager.getMediaDetails(DataManager.mediaItem,
							DataManager.playerOption, DataManager.listener);
				}
			});
		}

		mOperationsList.remove(Integer.valueOf(operationId));

		if (!mActivityStopped) {
			hideLoadingDialog();
		}
	}

	// ======================================================
	// ACTIVITY'S EVENT LISTENERS - HOME.
	// ======================================================

	@Override
	public void onMediaItemOptionPlayNowSelected(final MediaItem mediaItem,
			int position) {
		Logger.i(TAG, "Play Now: " + mediaItem.getId());
//		mApplicationConfigurations.setSessionID("fe12_e8d84aa1f3c2f65d3ad04b5b82a");
//		mApplicationConfigurations.setPasskey("cca6071786bcdd34e26ee67cbc844c10379f3a13b9fa0d13c03d16e9ae621b3984f130e621f7e6bd12c7b1881b629639");
		if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				try {
					findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
				} catch (Exception e) {
				}

				Track track = new Track(mediaItem.getId(),
						mediaItem.getTitle(), mediaItem.getAlbumName(),
						mediaItem.getArtistName(), mediaItem.getImageUrl(),
						mediaItem.getBigImageUrl(), mediaItem.getImages(),
						mediaItem.getAlbumId());
				List<Track> tracks = new ArrayList<Track>();
				tracks.add(track);
				mPlayerBar.playNow(tracks, null, null);
			} else {
				Logger.i("MediaTilesAdapter",
						"Play button click: Media detail OnSuccess 2");
				boolean needToPlay = true;
				// song_ids Kautik
				if(mediaItem.getSongIdsList()!=null && mediaItem.getSongIdsList().size()>0){
					Logger.s("Song ids ::::: " + mediaItem.getSongIds().size());
					List<Track> tracks = new ArrayList<Track>();
					Track track = HomeMediaTileGridFragmentNew.getTrarkBySongId(mediaItem.getSongIdsList().get(0));
					if(track != null){
						Logger.s("SongIds ::: Play " + track.getTitle());
						tracks.add(track);
						mPlayerBar.playNow(tracks, null, null);
						needToPlay = false;
					}
				}
				final boolean needToPlay1 = needToPlay;
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						String mediaDeatils = null;
						if (mediaItem.getMediaType() == MediaType.ALBUM)
							mediaDeatils = DBOHandler.getAlbumDetails(mContext, ""
									+ mediaItem.getId());
						else if (mediaItem.getMediaType() == MediaType.PLAYLIST)
							mediaDeatils = DBOHandler.getPlaylistDetails(mContext, ""
									+ mediaItem.getId());
						showMediaDetails(mediaDeatils, mediaItem, false, needToPlay1);
					}
				}, 500);
			}
		}
	}

	private int lastDetailedPosition = -5;

	private void showMediaDetails(final String mediaDeatils,
			final MediaItem mediaItem, final boolean loadDetails, final boolean needToPlay) {

		boolean isCached = false;
		if (!Utils.isConnected()
				&& !mApplicationConfigurations.getSaveOfflineMode()) {
			internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
				@Override
				public void onRetryButtonClicked() {
					showMediaDetails(mediaDeatils, mediaItem, loadDetails, needToPlay);
				}
			});
		} else {

			if (mediaDeatils != null && mediaDeatils.length() > 0) {
				MediaDetailsOperation mediaDetailsOperation;
				if (loadDetails) {
					mediaDetailsOperation = new MediaDetailsOperation("", "",
							"", mediaItem,
							needToPlay?PlayerOption.OPTION_PLAY_NOW_AND_OPEN: PlayerOption.OPTION_ADD_TO_QUEUE, null);
				} else
					mediaDetailsOperation = new MediaDetailsOperation("", "",
							"", mediaItem, needToPlay?PlayerOption.OPTION_PLAY_NOW : PlayerOption.OPTION_ADD_TO_QUEUE, null);
				try {
					Response res = new Response();
					res.response = mediaDeatils;
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
				isCached = true;
			}
			Logger.i("MediaTilesAdapter",
					"Play button click: showMediaDetails OnSuccess 3");
			if (!isCached) {
				if (loadDetails)
					mDataManager.getMediaDetails(mediaItem,
							needToPlay?PlayerOption.OPTION_PLAY_NOW_AND_OPEN: PlayerOption.OPTION_ADD_TO_QUEUE,
							HomeActivity.this);
				else
					mDataManager.getMediaDetails(mediaItem,
							needToPlay?PlayerOption.OPTION_PLAY_NOW : PlayerOption.OPTION_ADD_TO_QUEUE, HomeActivity.this);
			}
		}

	}

	@Override
	public void onMediaItemOptionPlayNextSelected(MediaItem mediaItem,
			int position) {
		Logger.i(TAG, "Play Next: " + mediaItem.getId());
		if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				Track track = new Track(mediaItem.getId(),
						mediaItem.getTitle(), mediaItem.getAlbumName(),
						mediaItem.getArtistName(), mediaItem.getImageUrl(),
						mediaItem.getBigImageUrl(), mediaItem.getImages(),
						mediaItem.getAlbumId());
				List<Track> tracks = new ArrayList<Track>();
				tracks.add(track);
				mPlayerBar.playNext(tracks);
			} else {
				boolean isCached = false;

				String mediaDeatils = null;
				if (mediaItem.getMediaType() == MediaType.ALBUM)
					mediaDeatils = DBOHandler.getAlbumDetails(mContext, ""
							+ mediaItem.getId());
				else if (mediaItem.getMediaType() == MediaType.PLAYLIST)
					mediaDeatils = DBOHandler.getPlaylistDetails(mContext, ""
							+ mediaItem.getId());
				if (mediaDeatils != null && mediaDeatils.length() > 0) {
					MediaDetailsOperation mediaDetailsOperation = new MediaDetailsOperation(
							"", "", "", mediaItem,
							PlayerOption.OPTION_PLAY_NEXT, null);
					try {
						Response res = new Response();
						res.response = mediaDeatils;
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
					isCached = true;
				}

				if (!isCached) {
					mDataManager.getMediaDetails(mediaItem,
							PlayerOption.OPTION_PLAY_NEXT, this);
				}
			}
		}
	}

	@Override
	public void onMediaItemOptionAddToQueueSelected(MediaItem mediaItem,
			int position) {
		Logger.i(TAG, "Add to queue: " + mediaItem.getId());
		if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				Track track = new Track(mediaItem.getId(),
						mediaItem.getTitle(), mediaItem.getAlbumName(),
						mediaItem.getArtistName(), mediaItem.getImageUrl(),
						mediaItem.getBigImageUrl(), mediaItem.getImages(),
						mediaItem.getAlbumId());
				List<Track> tracks = new ArrayList<Track>();
				tracks.add(track);
				mPlayerBar.addToQueue(tracks, null, null);
			} else {
				boolean isCached = false;
				String mediaDeatils = null;
				if (mediaItem.getMediaType() == MediaType.ALBUM)
					mediaDeatils = DBOHandler.getAlbumDetails(mContext, ""
							+ mediaItem.getId());
				else if (mediaItem.getMediaType() == MediaType.PLAYLIST)
					mediaDeatils = DBOHandler.getPlaylistDetails(mContext, ""
							+ mediaItem.getId());
				if (mediaDeatils != null && mediaDeatils.length() > 0) {
					MediaDetailsOperation mediaDetailsOperation = new MediaDetailsOperation(
							"", "", "", mediaItem,
							PlayerOption.OPTION_ADD_TO_QUEUE, null);
					try {
						Response res = new Response();
						res.response = mediaDeatils;
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
					isCached = true;
				}

				if (!isCached) {
					mDataManager.getMediaDetails(mediaItem,
							PlayerOption.OPTION_ADD_TO_QUEUE, this);
				}
			}
		}
	}

	private void onMediaItemOptionShowDetails(final MediaItem mediaItem,
			final int position) {

		try {
			if (mApplicationConfigurations == null) {
				mDataManager = DataManager.getInstance(this);
				mApplicationConfigurations = mDataManager
						.getApplicationConfigurations();
			}
			if (!Utils.isConnected()
					&& !mApplicationConfigurations.getSaveOfflineMode()) {
				try {
					((MainActivity) this)
							.internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
								@Override
								public void onRetryButtonClicked() {
									onMediaItemOptionShowDetails(mediaItem, position);
								}
							});
					return;
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			} else {
				Intent intent = null;
				MediaDetailsActivityNew mediaDetailsFragment = null;

				if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {

//				intent = new Intent(this, MediaDetailsActivity.class);
//				intent.putExtra(MediaDetailsActivity.EXTRA_MEDIA_ITEM,
//						(Serializable) mediaItem);
//				intent.putExtra(MediaDetailsActivity.FLURRY_SOURCE_SECTION,
//						mFlurrySubSectionDescription);
//				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


					mediaDetailsFragment = new MediaDetailsActivityNew();

					Bundle bundle = new Bundle();
					bundle.putSerializable(MediaDetailsActivity.EXTRA_MEDIA_ITEM,
							(Serializable) mediaItem);
					bundle.putString(MediaDetailsActivity.FLURRY_SOURCE_SECTION,
							mFlurrySubSectionDescription);

					if (getIntent().getStringExtra("video_in_audio_content_id") != null) {
						bundle.putString("video_in_audio_content_id", getIntent()
								.getStringExtra("video_in_audio_content_id"));
						getIntent().removeExtra("video_in_audio_content_id");
						bundle.putBoolean("add_to_queue", true);

						mediaDetailsFragment.setArguments(bundle);
//                    FragmentManager mFragmentManager = getSupportFragmentManager();
//                    FragmentTransaction fragmentTransaction = mFragmentManager
//                            .beginTransaction();
//                    fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
//                            mediaDetailsFragment, FRAGMENT_TAG_MAIN_SEARCH);
//                    fragmentTransaction.commitAllowingStateLoss();
					} else if (AlertActivity.isMessage || position == -1) {
						bundle.putBoolean("add_to_queue", true);
						mediaDetailsFragment.setArguments(bundle);
//                    FragmentManager mFragmentManager = getSupportFragmentManager();
//                    FragmentTransaction fragmentTransaction = mFragmentManager
//                            .beginTransaction();
//                    fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
//                            mediaDetailsFragment, FRAGMENT_TAG_MAIN_SEARCH);
//                    fragmentTransaction.commitAllowingStateLoss();
					} else if (position == -2
							&& mediaItem.getMediaType() == MediaType.TRACK) {
						Logger.i(
								TAG,
								"Show Details MediaType.TRACK: "
										+ mediaItem.getId());
						Track track = new Track(mediaItem.getId(),
								mediaItem.getTitle(), mediaItem.getAlbumName(),
								mediaItem.getArtistName(), mediaItem.getImageUrl(),
								mediaItem.getBigImageUrl(), mediaItem.getImages(),
								mediaItem.getAlbumId());
						List<Track> tracks = new ArrayList<Track>();
						tracks.add(track);
						mPlayerBar.playNow(tracks, null, null);
					} else{
						mediaDetailsFragment.setArguments(bundle);
					}
				} else {
					int location = position;
					List<MediaItem> list = new ArrayList<MediaItem>();
					try {
						Fragment fragment = /*
										 * adapter.getItem(pager.getCurrentItem()
										 * );
										 */adapter.getCurrentFragment(0);
						RecyclerView mGrid = (RecyclerView) fragment.getView()
								.findViewById(R.id.recyclerView);

						if (mGrid != null) {
							// list = ((MediaTilesAdapterVideo)
							// mGrid.getAdapter()).mMediaItems;
							List<Object> temp = ((MediaTilesAdapterVideo) mGrid
									.getAdapter()).mMediaItems;
							for (Object item : temp) {
								if (item instanceof MediaItem)
									list.add((MediaItem) item);
								else if (item instanceof PromoUnit)
									location--;
							}
						}
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
					//tempMediaItemList = list;

                //Added by patibandha
                /*if (isCastConnected() && HungamaApplication.isAppRequiredVideoCast) {


					if(!isVideoPlaying(mediaItem)) {
						isTileClick = true;
						isFirstVideoCast = true;
						mCurruntVideoPosition = tempMediaItemList.indexOf(mediaItem);
						loadedCount = tempMediaItemList.indexOf(mediaItem);
						tempMediaItem = mediaItem;
						storeListInCastManager();
						mDataManager.getVideoDetailsAdp(mediaItem,
								networkSpeed, networkType,
								contentFormat, HomeActivity.this, googleEmailId);
					}else{
						Utils.makeText(HomeActivity.this, "Video already playing", Toast.LENGTH_SHORT).show();
					}

					// If cast connected than call service for get video detail
					// Patibandha get required value for direct cast video in oncreate
					// Method name "InitCastDetail"

					// Added by p
					*//*new AlertDialog.Builder(HomeActivity.this)
							.setMessage("Please select your choice")
							.setPositiveButton("Play Now", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									if(!isVideoPlaying(mediaItem)) {
										isFirstVideoCast = true;
										mCurruntVideoPosition = tempMediaItemList.indexOf(mediaItem);
										tempMediaItem = mediaItem;
										mDataManager.getVideoDetailsAdp(mediaItem,
												networkSpeed, networkType,
												contentFormat, HomeActivity.this, googleEmailId);
									}else{
										Utils.makeText(HomeActivity.this, "Video already playing", Toast.LENGTH_SHORT).show();
									}
								}
							})
							.setNegativeButton("Add To Queue", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									if(!isVideoInQueue(mediaItem)) {
										isFirstVideoCast = false;
										mCurruntVideoPosition = tempMediaItemList.indexOf(mediaItem);
										tempMediaItem = mediaItem;
										mDataManager.getVideoDetailsAdp(mediaItem,
												networkSpeed, networkType,
												contentFormat, HomeActivity.this, googleEmailId);
									}else{
										Utils.makeText(HomeActivity.this, "Video already in Queue", Toast.LENGTH_SHORT).show();
									}
								}
							})
							.show();*//*
				} else {*/

					intent = new Intent(this, VideoActivity.class);
					intent.putExtra(VideoActivity.EXTRA_MEDIA_ITEM_VIDEO,
							(Serializable) mediaItem);
					intent.putExtra(VideoActivity.EXTRA_MEDIA_LIST_VIDEO,
							(Serializable) list);
					intent.putExtra(VideoActivity.EXTRA_MEDIA_POS_VIDEO, location);
					getIntent().removeExtra("video_content_id");
					startActivity(intent);
				//}
				}
//			startActivity(intent);
				if(mediaDetailsFragment!=null) {
					if(position==-1){
						final MediaDetailsActivityNew finalMediaDetailsFragment = mediaDetailsFragment;
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								if(!isFinishing() && this!=null) {
									FragmentManager mFragmentManager = getSupportFragmentManager();
									FragmentTransaction fragmentTransaction = mFragmentManager
											.beginTransaction();
									fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
											finalMediaDetailsFragment, "MediaDetailsActivity");
									fragmentTransaction.addToBackStack("MediaDetailsActivity");
									if (Constants.IS_COMMITALLOWSTATE)
										fragmentTransaction.commitAllowingStateLoss();
									else
										fragmentTransaction.commit();
								}
							}
						},500);
						getIntent().removeExtra(AlertActivity.ALERT_MARK);
					}else{
						if(!isFinishing() && this!=null){
							FragmentManager mFragmentManager = getSupportFragmentManager();
							FragmentTransaction fragmentTransaction = mFragmentManager
									.beginTransaction();
							fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
									mediaDetailsFragment, "MediaDetailsActivity");
							fragmentTransaction.addToBackStack("MediaDetailsActivity");
							if(Constants.IS_COMMITALLOWSTATE)
								fragmentTransaction.commitAllowingStateLoss();
							else
								fragmentTransaction.commit();
						}
					}

//                fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
//                        mediaDetailsFragment, "MediaDetailsActivity");
//                fragmentTransaction.addToBackStack("MediaDetailsActivity");
//                fragmentTransaction.commitAllowingStateLoss();
				}
				findViewById(R.id.progressbar).setVisibility(View.GONE);
				findViewById(R.id.home_browse_by_fragmant_container).setVisibility(View.VISIBLE);
			}
		}catch (Exception e){
		}catch (Error e){
		}

	}

	@Override
	public void onMediaItemOptionShowDetailsSelected(final MediaItem mediaItem,
			final int position) {
		Logger.i(TAG, "Show Details first: " + mediaItem != null ? ""
				+ mediaItem.getId() : "");
		if(mApplicationConfigurations==null) {
			mContext = getApplicationContext();
			if(mDataManager==null)
				mDataManager = DataManager.getInstance(mContext);
			mApplicationConfigurations = mDataManager
					.getApplicationConfigurations();
		}
		if (!Utils.isConnected()
				&& !mApplicationConfigurations.getSaveOfflineMode()) {
			internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
				@Override
				public void onRetryButtonClicked() {
					onMediaItemOptionShowDetailsSelected(mediaItem, position);
				}
			});
			try {
				findViewById(R.id.progressbar).setVisibility(View.GONE);
			} catch (Exception e) {
			}
		}else {
			try {
				findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
			} catch (Exception e) {
			}

			onMediaItemOptionShowDetails(mediaItem, position);
		}
	}

	@Override
	public void onMediaItemOptionRemoveSelected(MediaItem mediaItem,
			int position) {
		Logger.i(TAG, "Remove item: " + mediaItem.getId());
	}

	/**
	 * Load next page for content. Used for home pages.
	 *
	 * @param start
	 *            start index of page
	 * @param length
	 *            content length
	 * @param mediaCategoryType
	 *            page content category type
	 * @param context
	 *            communication listener object for operation handling
	 */
	public void loadMoreResults(int start, int length,
			MediaCategoryType mediaCategoryType,
			CommunicationOperationListener context) {

		String timestamp_cache = null;
		if (mCurrentMediaContentType == MediaContentType.MUSIC) {
			if (mediaCategoryType == MediaCategoryType.LATEST)
				timestamp_cache = mDataManager.getApplicationConfigurations()
						.getMusicLatestTimeStamp();
			else
				timestamp_cache = mDataManager.getApplicationConfigurations()
						.getMusicPopularTimeStamp();
		} else if (mCurrentMediaContentType == MediaContentType.VIDEO) {
			timestamp_cache = mDataManager.getApplicationConfigurations()
					.getVideoLatestTimeStamp();
		}

		mDataManager.getMediaItemsPaging(mCurrentMediaContentType,
				mediaCategoryType, null, String.valueOf(start),
				String.valueOf(length), context, timestamp_cache);
	}

	private String getHomeTabBar(int selectedTab) {
		String tab = "";
		if (selectedTab == HomeTabBar.TAB_INDEX_MUSIC
				|| selectedTab == HomeTabBar.TAB_INDEX_VIDEO) {
			tab = "_new";
		} else if (selectedTab == HomeTabBar.TAB_INDEX_MUSIC_POPULAR) {
			tab = "_popular";
		}
		return tab;
	}

	/**
	 * Check for device offer and show toast if needed.
	 */
	private void messageThread() {
		final Activity _this = this;
		ThreadPoolManager.getInstance().submit(new Runnable() {
			public void run() {
				URL url;
				try {
					Thread.sleep(3000);
					DeviceConfigurations deviceConfig = mDataManager
							.getDeviceConfigurations();

					ApplicationConfigurations config=ApplicationConfigurations.getInstance(getBaseContext());
					String mHardwareId = deviceConfig.getHardwareId();
					String userAgent = deviceConfig.getDefaultUserAgentString(
							HomeActivity.this, handler);
					String macAddress = deviceConfig.getMac(HomeActivity.this);
					com.hungama.myplay.activity.util.Logger.i("",
							"SecuredThread");
//					url = new URL(
//							getString(R.string.hungama_server_url_device_offer)
//									+ HungamaApplication.encodeURL(mHardwareId,
//											"utf-8")
//									+ "&mac="
//									+ HungamaApplication.encodeURL(macAddress,
//											"utf-8")
//									+ "&user_agent="
//									+ HungamaApplication.encodeURL(userAgent,
//											"utf-8") + "&login=1");

					String gcmtoken=UAirship.shared().getPushManager().getGcmToken();
					if(TextUtils.isEmpty(gcmtoken)) {
						gcmtoken = "";
					}
					url = new URL(getString(R.string.hungama_server_url_device_offer_dev)
							+ HungamaApplication.encodeURL(mHardwareId) + "&mac="
							+ HungamaApplication.encodeURL(macAddress) + "&user_agent="
							+ HungamaApplication.encodeURL(userAgent) + "&login="+(config.isRealUser()?"1":"0")+
							"&app=music&os=android&dt="+gcmtoken+"&dtype="+(Utils.isTablet(HomeActivity.this)?"tab":"phone"));
					com.hungama.myplay.activity.util.Logger.i(TAG,
							"URL fetched-" + url.toString());
					// +"&mac="
					// +URLEncoder.encode(OnApplicationStartsActivity.macAddress,
					// "utf-8")
					// HttpURLConnection urlConnection = (HttpURLConnection)
					// url.openConnection();
					if(Logger.enableOkHTTP){
						OkHttpClient client = CommunicationManager.getUnsafeOkHttpClient();
//						Request.Builder requestBuilder = new Request.Builder();
//						requestBuilder.url(url);
						Request.Builder requestBuilder = CommunicationManager.getRequestBuilder(HomeActivity.this, url);
						com.squareup.okhttp.Response responseOk = client.newCall(requestBuilder.build()).execute();
						if (responseOk.code() == HttpURLConnection.HTTP_OK) {
//							final String response = parseJSON(responseOk.body().string());
							final String response = ServerConfigurations.getInstance(HomeActivity.this)
									.parseDeviceOfferJSON(responseOk.body().string(), HomeActivity.this);
							_this.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									showToast(response);
								}
							});
							return;
						}
					} else {
						HttpURLConnection urlConnection = (HttpURLConnection) url
								.openConnection();
						if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
							InputStream in = new BufferedInputStream(
									urlConnection.getInputStream());
							StringBuilder sb = new StringBuilder();
							int ch = -1;
							while ((ch = in.read()) != -1) {
								sb.append((char) ch);
							}
//							final String response = parseJSON(sb.toString());
							final String response = ServerConfigurations.getInstance(HomeActivity.this)
									.parseDeviceOfferJSON(sb.toString(), HomeActivity.this);
							_this.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									showToast(response);
								}
							});
							return;
						}
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
					Logger.i("Error-response-", "" + e);
				} catch (IOException e) {
					e.printStackTrace();
					Logger.i("Error-response-", "" + e);
				} catch (Exception e) {
					e.printStackTrace();
					Logger.i("Error-response-", "" + e);
				} catch (Error e) {
					System.gc();
					System.runFinalization();
					System.gc();
				}
			};
		});

	}

//	private JSONObject jsonObject;
//
//	private String parseJSON(String response) throws JSONException {
//		try {
//			com.hungama.myplay.activity.util.Logger.i(TAG, "TrackIMEI>>>"
//					+ response);
//			jsonObject = new JSONObject(response);
//
//			if (jsonObject.getInt("code") == 200) {
//				response = jsonObject.getString("message");
//			} else {
//				response = null;
//			}
//		} catch (Exception e) {
//			response = null;
//		}
//		return response;
//	}

	private void showToast(String response) {
		if (response == null)
			return;
		Toast toast = Toast.makeText(this, response, Toast.LENGTH_LONG);
		ToastExpander.showFor(toast, 5000);
	}

	@Override
	public void onMediaItemOptionSaveOfflineSelected(MediaItem mediaItem,
			int position) {
		Logger.i(TAG, "Save Offline: " + mediaItem.getId());
		if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				Track track = new Track(mediaItem.getId(),
						mediaItem.getTitle(), mediaItem.getAlbumName(),
						mediaItem.getArtistName(), mediaItem.getImageUrl(),
						mediaItem.getBigImageUrl(), mediaItem.getImages(),
						mediaItem.getAlbumId());
				CacheManager.saveOfflineAction(this, mediaItem, track);
				Utils.saveOfflineFlurryEvent(this,
						FlurryConstants.FlurryCaching.LongPressMenuSong
								.toString(), mediaItem);
			} else if (mediaItem.getMediaType() == MediaType.ALBUM
					|| mediaItem.getMediaType() == MediaType.PLAYLIST) {
				if (MediaCachingTaskNew.isEnabled)
					mDataManager.getMediaDetails(mediaItem,
							PlayerOption.OPTION_SAVE_OFFLINE, this);
				else
					CacheManager.saveOfflineAction(this, mediaItem, null);

				if (mediaItem.getMediaType() == MediaType.ALBUM) {
					Utils.saveOfflineFlurryEvent(this,
							FlurryConstants.FlurryCaching.LongPressMenuAlbum
									.toString(), mediaItem);
				} else {
					Utils.saveOfflineFlurryEvent(this,
							FlurryConstants.FlurryCaching.LongPressMenuPlaylist
									.toString(), mediaItem);
				}
			}
		} else {
			CacheManager.saveOfflineAction(this, mediaItem, null);
			Utils.saveOfflineFlurryEvent(
					this,
					FlurryConstants.FlurryCaching.LongPressMenuVideo.toString(),
					mediaItem);
		}
	}

	/**
	 * It will receive when offline state changes event for song, album,
	 * playlist, video caching.
	 *
	 * @author hungama2
	 *
	 */
	private class CacheStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			Logger.s("========================= cachestateupdatereceived ========"
					+ arg1.getAction());
			if (arg1.getAction()
					.equals(CacheManager.ACTION_CACHE_STATE_UPDATED)
					|| arg1.getAction()
							.equals(CacheManager.ACTION_TRACK_CACHED)) {
				try {
					if (mCurrentMediaContentType == MediaContentType.MUSIC) {
						Fragment fragment = adapter.getCurrentFragment(pager
								.getCurrentItem());
						RecyclerView mGrid = (RecyclerView) fragment.getView()
								.findViewById(R.id.recyclerView);
						if (mGrid != null) {
							((MyAdapter) mGrid.getAdapter())
									.notifyDataSetChanged();

						}
					}
					mPlayerBar.updatedCurrentTrackCacheState();
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}

				boolean isPopupShown = false;
				try {
					long id = mApplicationConfigurations
							.getSaveOfflineAutoSaveFreeUser();
					if (mApplicationConfigurations.isEnabledHomeGuidePage3Offline() && arg1.getAction().equals(
							CacheManager.ACTION_TRACK_CACHED)
							&& DBOHandler.getTrackCacheState(HomeActivity.this,
									"" + id) == CacheState.CACHED) {
						Logger.s("songcatched HomeScreen");
						mApplicationConfigurations.setIsSongCatched(true);
						// if (HungamaApplication.isActivityVisible()) {
						Logger.s("songcatched ifActivityVisible HomeScreen");
						openOfflineGuide();
						isPopupShown = true;
						// }
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}

				try {
					if (!isPopupShown && arg1.getAction().equals(
							CacheManager.ACTION_TRACK_CACHED)
							&& DBOHandler.getAllCachedTracks(mContext).size() == CacheManager
									.getFreeUserCacheLimit(mContext)
							&& mApplicationConfigurations
									.getFreeUserCacheCount() == CacheManager
									.getFreeUserCacheLimit(mContext)) {

						sendBroadcast(new Intent(
								getString(R.string.inapp_prompt_action_apppromptofflinecaching3rdsong)));
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			} else if (arg1.getAction().equals(
					CacheManager.ACTION_UPDATED_CACHE)) {
			} else if (arg1.getAction().equals(
					CacheManager.ACTION_VIDEO_CACHE_STATE_UPDATED)
					|| arg1.getAction().equals(
							CacheManager.ACTION_VIDEO_TRACK_CACHED)) {
				try {

					Fragment fragment = adapter
							.getCurrentFragment(HomeTabBar.TAB_INDEX_VIDEO);
					RecyclerView mGrid = (RecyclerView) fragment.getView()
							.findViewById(R.id.recyclerView);
					if (mGrid != null) {
						((MediaTilesAdapterVideo) mGrid.getAdapter())
								.notifyDataSetChanged();

					}
					// }
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else if (arg1.getAction().equals(
					CacheManager.ACTION_VIDEO_UPDATED_CACHE)) {
			}
		}
	}

	private class OfflineModeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			boolean offlineMode = (ApplicationConfigurations
					.getInstance(getApplicationContext())).getSaveOfflineMode();
			if (offlineMode) {
				try {
					findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
				} catch (Exception e) {
				}

				// new SwitchToOfflineMode().execute();
				Intent i = new Intent(HomeActivity.this, HomeActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.putExtra("finish_all", true);
				i.putExtra("open_upgrade_popup",
						arg1.getBooleanExtra("open_upgrade_popup", false));
				startActivity(i);
				mPlayerBar.updateNotificationForOffflineMode();
				hideLoadingDialog();
			}
		}
	}

	private class LanguageChangeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
//			mDataManager.getCacheManager().storeMusicLatestResponse("", null);
//			mDataManager.getCacheManager().storeMusicFeaturedResponse("", null);
//			mDataManager.getCacheManager().storeVideoLatestResponse("", null);
//			mDataManager.getCacheManager().storeLiveRadioResponse("", null);
//			mDataManager.getCacheManager().storeCelebRadioResponse("", null);

			mApplicationConfigurations.setMusicLatestTimeStamp(null);
			mApplicationConfigurations.setMusicPopularTimeStamp(null);
			mApplicationConfigurations.setVideoLatestTimeStamp(null);
			mApplicationConfigurations.setLiveRadioTimeStamp(null);
			mApplicationConfigurations.setOnDemandTimeStamp(null);

			startService(new Intent(HomeActivity.this,
					ReloadTracksDataService.class));

			Intent i = new Intent(HomeActivity.this, HomeActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.putExtra("finish_restart", true);
			startActivity(i);
			hideLoadingDialog();
		}
	}

	private class CloseAppReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			mPlayerBar.explicitStop();

			// reset the inner boolean for showing home
			// tile hints.
			mApplicationConfigurations.setIsHomeHintShownInThisSession(false);
			mApplicationConfigurations
					.setIsSearchFilterShownInThisSession(false);
			mApplicationConfigurations
					.setIsPlayerQueueHintShownInThisSession(false);
			// if this button is clicked, close
			// current activity
			HomeActivity.super.onBackPressed();
			HomeActivity.this.finish();

			hideLoadingDialog();
		}
	}

	private void restartApplication() {
		set = false;

		Intent mStartActivity = new Intent(this,
				OnApplicationStartsActivity.class);
		mStartActivity.putExtra("skip_ad", true);
		int mPendingIntentId = 123456;
		PendingIntent mPendingIntent = PendingIntent.getActivity(this,
				mPendingIntentId, mStartActivity,
				PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager mgr = (AlarmManager) this
				.getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis()+300, mPendingIntent);

		super.finish();
	}

	private boolean closequeue = false;

	private class UaLinkReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			closequeue = true;

		}
	}

	/**
	 * Checks weather any offline caching is going on or not. If no caching is
	 * going on, it will check for stopped track in previous session and restart
	 * it.
	 */
	private void isServiceRunning() {
		final ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		final List<RunningServiceInfo> services = activityManager
				.getRunningServices(Integer.MAX_VALUE);
		boolean isServiceFound = false;
		for (int i = 0; i < services.size(); i++) {


			if ("com.hungama.myplay.activity".equals(services.get(i).service
					.getPackageName())) {

				if ("com.hungama.myplay.activity.data.audiocaching.DownloaderService"
						.equals(services.get(i).service.getClassName())) {
					// Logger.d(TAG,
					// " ::::: Service Nr. -- getClassName stimmt �berein !!!");
					isServiceFound = true;
					break;
				}
			}
		}
		try {
			if (!isServiceFound) {
				CacheManager.resumeCachingStoppedTrack(this);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	public void showLoadingDialog(int messageResource) {
		super.showLoadingDialog(messageResource);
	}

	public void hideLoadingDialog() {
		super.hideLoadingDialog();
	}

	private void registerReceivers() {
		Logger.s(" ::::::::::::::::::- - registerReceivers");
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_PREFERENCE_CHANGE);
		registerReceiver(preference_update, filter);

		IntentFilter filter_notify = new IntentFilter();
		filter_notify.addAction(ACTION_NOTIFY_ADAPTER);
		registerReceiver(reciver_notify, filter_notify);

		IntentFilter filter_listener_update = new IntentFilter();
		filter_listener_update.addAction(ACTION_LISTENER);
		registerReceiver(listener_update, filter_listener_update);

		if (languageChangeReceiver == null) {
			languageChangeReceiver = new LanguageChangeReceiver();
			IntentFilter filter_lang = new IntentFilter();
			filter_lang.addAction(MainActivity.ACTION_LANGUAGE_CHANGED);
			registerReceiver(languageChangeReceiver, filter_lang);
		}

		if (closeAppReceiver == null) {
			closeAppReceiver = new CloseAppReceiver();
			IntentFilter filter_close = new IntentFilter();
			filter_close.addAction(ACTION_CLOSE_APP);
			registerReceiver(closeAppReceiver, filter_close);
		}

		IntentFilter filter_radio_change = new IntentFilter();
		filter_radio_change.addAction(ACTION_RADIO_DATA_CHANGE);
		registerReceiver(listener_radio_data_change, filter_radio_change);

		IntentFilter airplaneMode = new IntentFilter();
		airplaneMode.addAction("android.intent.action.AIRPLANE_MODE");
		registerReceiver(AireplaneModeReceiver, airplaneMode);

        if (!Utils.isCarMode()) {
            mTrackReloadReceiver = mPlayerBar.new TrackReloadReceiver();
            IntentFilter trackReloadFilter = new IntentFilter();
            trackReloadFilter
                    .addAction(ActionDefinition.ACTION_MEDIA_DETAIL_RELOADED);
            registerReceiver(mTrackReloadReceiver, trackReloadFilter);
        }
    }

	private BroadcastReceiver listener_radio_data_change = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				Fragment temp = adapter
						.getCurrentFragment(HomeTabBar.TAB_INDEX_RADIO);
				if (temp != null) {
					BrowseRadioFragment radioFragment = (BrowseRadioFragment) temp;
					radioFragment.refreshData();
				}
			} catch (Exception e) {
				e.printStackTrace();
				e.printStackTrace();
			}
		}
	};

	private BroadcastReceiver listener_update = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			setTileAdapterListners();
		}
	};

	private BroadcastReceiver reciver_notify = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Logger.s(" ::::::::::::::::::- - reciver_notify");
			checkPromoUnitConditions(mPromoUnit);

			try {
				Fragment temp = adapter
						.getCurrentFragment(HomeTabBar.TAB_INDEX_MUSIC);
				if (temp != null) {
					HomeMediaTileGridFragmentNew frag1 = (HomeMediaTileGridFragmentNew) temp;

					frag1.RefillmediaItems();
					Logger.s(" ::::::::::::::::::- - reciver_notify 1");
					// frag1.setMediaItemsMusic(frag1.mediaitemMusic);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Fragment temp = adapter
						.getCurrentFragment(HomeTabBar.TAB_INDEX_MUSIC_POPULAR);
				if (temp != null) {
					HomeMediaTileGridFragmentNew frag1 = (HomeMediaTileGridFragmentNew) temp;
					frag1.RefillmediaItems();
					Logger.s(" ::::::::::::::::::- - reciver_notify 2");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Fragment temp = adapter
						.getCurrentFragment(HomeTabBar.TAB_INDEX_VIDEO);
				if (temp != null) {
					HomeMediaTileGridFragmentVideo frag1 = (HomeMediaTileGridFragmentVideo) temp;
					frag1.setMediaItems(frag1.mediaItems);
					Logger.s(" ::::::::::::::::::- - reciver_notify 3");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// radio update pendig
			try {
				Fragment temp = adapter
						.getCurrentFragment(HomeTabBar.TAB_INDEX_RADIO);
				if (temp != null) {
					BrowseRadioFragment radioFragment = (BrowseRadioFragment) temp;
					radioFragment.mediaItemsGridFragment
							.setMediaItems(radioFragment.mMediaItemsDisplay);
					Logger.s(" ::::::::::::::::::- - reciver_notify 4");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				GlobalMenuFragment temp;
				if (mainSettingsFragment == null)
					temp = getGlobalMenu();
				else
					temp = mainSettingsFragment;
				temp.onResume();
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}
	};

	private void setTileAdapterListners() {
		try {
			Fragment temp = adapter.getItem(HomeTabBar.TAB_INDEX_MUSIC);
			if (temp != null) {
				HomeMediaTileGridFragmentNew frag1 = (HomeMediaTileGridFragmentNew) temp;
				if (frag1.mAdapter != null)
					frag1.mAdapter
							.setOnMusicItemOptionSelectedListener(HomeActivity.this);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Fragment temp = adapter.getItem(HomeTabBar.TAB_INDEX_MUSIC_POPULAR);
			if (temp != null) {
				HomeMediaTileGridFragmentNew frag1 = (HomeMediaTileGridFragmentNew) temp;
				if (frag1.mAdapter != null)
					frag1.mAdapter
							.setOnMusicItemOptionSelectedListener(HomeActivity.this);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Fragment temp = adapter.getItem(HomeTabBar.TAB_INDEX_VIDEO);
			if (temp != null) {
				HomeMediaTileGridFragmentVideo frag1 = (HomeMediaTileGridFragmentVideo) temp;
				if (frag1.mHomeMediaTilesAdapter != null)
					frag1.mHomeMediaTilesAdapter
							.setOnMusicItemOptionSelectedListener(HomeActivity.this);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private BroadcastReceiver preference_update = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String selectedLanguage = intent.getStringExtra("selectedLanguage");

			if (!TextUtils.isEmpty(selectedLanguage)) {
				Map<String, String> reportMap = new HashMap<String, String>();
				reportMap.put(
						FlurryConstants.FlurryKeys.LanguageSelected.toString(),
						selectedLanguage);
				Analytics.logEvent(
						FlurryConstants.FlurryEventName.BrowseBy.toString(),
						reportMap);
				if (mCurrentMediaContentType == MediaContentType.VIDEO)
					mApplicationConfigurations
							.setSeletedPreferencesVideo(selectedLanguage
									.toUpperCase());
				else
					mApplicationConfigurations
							.setSeletedPreferences(selectedLanguage
									.toUpperCase());
				Logger.s("PREFERENCES_SAVE onBrowseByCategoryItemSelected:"
						+ selectedLanguage);
			}
			loadDeepLinkContent();

			checkPromoUnitConditions(mPromoUnit);

			try {
				Fragment temp = adapter
						.getCurrentFragment(HomeTabBar.TAB_INDEX_MUSIC);
				if (temp != null) {
					HomeMediaTileGridFragmentNew musicFragment = (HomeMediaTileGridFragmentNew) temp;
					musicFragment.onRefresh();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				Fragment temp = adapter
						.getCurrentFragment(HomeTabBar.TAB_INDEX_MUSIC_POPULAR);
				if (temp != null) {
					HomeMediaTileGridFragmentNew musicFragment = (HomeMediaTileGridFragmentNew) temp;
					musicFragment.onRefresh();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Fragment temp = adapter
						.getCurrentFragment(HomeTabBar.TAB_INDEX_VIDEO);
				if (temp != null) {
					HomeMediaTileGridFragmentVideo videoFragment = (HomeMediaTileGridFragmentVideo) temp;
					videoFragment.onRefresh();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				Fragment temp = adapter
						.getCurrentFragment(HomeTabBar.TAB_INDEX_RADIO);
				if (temp != null) {
					BrowseRadioFragment radioFragment = (BrowseRadioFragment) temp;
					radioFragment.reloadData();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				RootFragmentDiscovery fragmentDiscover = (RootFragmentDiscovery) adapter
						.getCurrentFragment(HomeTabBar.TAB_INDEX_DISCOVER);
				if (fragmentDiscover != null
						&& fragmentDiscover.fragment != null) {
					fragmentDiscover.fragment.updateHashList();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	public void onShowcaseViewHide(ShowcaseView showcaseView) {
		Logger.s(" ::::::::::::::: onShowcaseViewHide");
		isShowcaseShowing = false;
		if (mApplicationConfigurations.isFirstTimeAppLaunch()) {
			mApplicationConfigurations.setIsFirstTimeAppLaunch(false);
		}
	}

	@Override
	public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
		Logger.s(" ::::::::::::::: onShowcaseViewDidHide");
		isShowcaseShowing = false;
	}

	@Override
	public void onShowcaseViewShow(ShowcaseView showcaseView) {
		Logger.s(" ::::::::::::::: onShowcaseViewShow");
	}

	@Override
	public void onPlayNowSelected(MediaItem mediaItem) {
		try {
			if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
				if (mediaItem.getMediaType() == MediaType.TRACK) {
					Track track = new Track(mediaItem.getId(),
							mediaItem.getTitle(), mediaItem.getAlbumName(),
							mediaItem.getArtistName(), mediaItem.getImageUrl(),
							mediaItem.getBigImageUrl(), mediaItem.getImages(),
							mediaItem.getAlbumId());
					List<Track> tracks = new ArrayList<Track>();
					tracks.add(track);
					try {
						findViewById(R.id.progressbar).setVisibility(
								View.VISIBLE);
					} catch (Exception e) {
					}

					mPlayerBarFragment.playNow(tracks, null, null);

				} else {
					mDataManager.getMediaDetails(mediaItem,
							PlayerOption.OPTION_PLAY_NOW, HomeActivity.this);
				}
			}
		} catch (Exception e) {
			Logger.e(getClass().getName() + ":1076", e.toString());
		}
	}

	@Override
	public void onAddToQueueSelected(MediaItem mediaItem) {
		try {
			if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
				if (mediaItem.getMediaType() == MediaType.TRACK) {
					Track track = new Track(mediaItem.getId(),
							mediaItem.getTitle(), mediaItem.getAlbumName(),
							mediaItem.getArtistName(), mediaItem.getImageUrl(),
							mediaItem.getBigImageUrl(), mediaItem.getImages(),
							mediaItem.getAlbumId());
					List<Track> tracks = new ArrayList<Track>();
					tracks.add(track);

					mPlayerBarFragment.addToQueue(tracks, null, null);

				} else {
					mDataManager
							.getMediaDetails(mediaItem,
									PlayerOption.OPTION_ADD_TO_QUEUE,
									HomeActivity.this);
				}
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void onShowDetails(final MediaItem mediaItem, final boolean playnow) {
		if (!Utils.isConnected()
				&& !mApplicationConfigurations.getSaveOfflineMode()) {
			try {
				((MainActivity) this)
						.internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
							@Override
							public void onRetryButtonClicked() {
								onShowDetails(mediaItem, playnow);
							}
						});
				return;
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}
		Intent intent;
		if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
			intent = new Intent(HomeActivity.this, VideoActivity.class);
			intent.putExtra(VideoActivity.EXTRA_MEDIA_ITEM_VIDEO,
					(Serializable) mediaItem);
            startActivity(intent);
		} else {
//			intent = new Intent(HomeActivity.this, MediaDetailsActivity.class);
//			intent.putExtra(MediaDetailsActivity.EXTRA_MEDIA_ITEM,
//					(Serializable) mediaItem);
//			intent.putExtra(MediaDetailsActivity.FLURRY_SOURCE_SECTION,
//					FlurryConstants.FlurrySourceSection.Search.toString());

            MediaDetailsActivityNew mediaDetailsFragment = new MediaDetailsActivityNew();

            Bundle bundle = new Bundle();
            bundle.putSerializable(MediaDetailsActivity.EXTRA_MEDIA_ITEM,
                    (Serializable) mediaItem);
            bundle.putString(MediaDetailsActivity.FLURRY_SOURCE_SECTION,
                    FlurryConstants.FlurrySourceSection.Search.toString());

            mediaDetailsFragment.setArguments(bundle);
            FragmentManager mFragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = mFragmentManager
                    .beginTransaction();
            fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
					mediaDetailsFragment, "MediaDetailsActivity");
            fragmentTransaction.addToBackStack("MediaDetailsActivity");
			if(Constants.IS_COMMITALLOWSTATE)
				fragmentTransaction.commitAllowingStateLoss();
			else
				fragmentTransaction.commit();
			findViewById(R.id.progressbar).setVisibility(View.GONE);
			findViewById(R.id.home_browse_by_fragmant_container).setVisibility(View.VISIBLE);
		}

//		startActivity(intent);
	}

	@Override
	public void onSaveOffline(MediaItem mediaItem) {
		Logger.i(TAG, "Save Offline: " + mediaItem.getId());
		if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				Track track = new Track(mediaItem.getId(),
						mediaItem.getTitle(), mediaItem.getAlbumName(),
						mediaItem.getArtistName(), mediaItem.getImageUrl(),
						mediaItem.getBigImageUrl(), mediaItem.getImages(),
						mediaItem.getAlbumId());
				CacheManager.saveOfflineAction(HomeActivity.this, mediaItem,
						track);
				Utils.saveOfflineFlurryEvent(HomeActivity.this,
						FlurryConstants.FlurryCaching.LongPressMenuSong
								.toString(), mediaItem);
			} else if (mediaItem.getMediaType() == MediaType.ALBUM
					|| mediaItem.getMediaType() == MediaType.PLAYLIST) {
				if (MediaCachingTaskNew.isEnabled)
					mDataManager.getMediaDetails(mediaItem,
							PlayerOption.OPTION_SAVE_OFFLINE, this);
				else
					CacheManager.saveOfflineAction(HomeActivity.this,
							mediaItem, null);
				if (mediaItem.getMediaType() == MediaType.ALBUM)
					Utils.saveOfflineFlurryEvent(HomeActivity.this,
							FlurryConstants.FlurryCaching.LongPressMenuAlbum
									.toString(), mediaItem);
				else
					Utils.saveOfflineFlurryEvent(HomeActivity.this,
							FlurryConstants.FlurryCaching.LongPressMenuPlaylist
									.toString(), mediaItem);
			}
		} else {
			CacheManager.saveOfflineAction(HomeActivity.this, mediaItem, null);
			Utils.saveOfflineFlurryEvent(
					HomeActivity.this,
					FlurryConstants.FlurryCaching.LongPressMenuVideo.toString(),
					mediaItem);
		}
	}

	@Override
	public void finish() {
		/*if (mCastManager!=null && isCastPlaying()) {
			stopCastNotification();
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
		}*/
		// Utils.clearCache(true);
//		try {
//			System.exit(0);
//			PicassoUtil.clearCache();
//		}catch (Exception e){
//		}catch (Error e){
//		}

		super.finish();
	}

	@Override
	public void onFinishSongCatcher(boolean isFinishSongCatcher) {
		if (isFinishSongCatcher) {
			String timestamp_cache = mDataManager
					.getApplicationConfigurations().getSearchPopularTimeStamp();
			mDataManager.getSearchPopularSerches(this, this, timestamp_cache);
		}
	}

	public void showDetailsOfRadio(final MediaItem mediaItem,
			final MediaCategoryType mediaCategoryType) {
		if (mediaCategoryType == MediaCategoryType.TOP_ARTISTS_RADIO) {
			// Flurry report: Top Artist Radio - Artist name
			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(FlurryConstants.FlurryKeys.ArtistName.toString(),
					mediaItem.getTitle());
			Analytics.logEvent(
					FlurryConstants.FlurryEventName.TopArtistRadio.toString(),
					reportMap);
		} else if (mediaCategoryType == MediaCategoryType.LIVE_STATIONS) {
			// Flurry report: Top Artist Radio - Artist name
			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(FlurryConstants.FlurryKeys.Title.toString(),
					mediaItem.getTitle());
			Analytics.logEvent(
					FlurryConstants.FlurryEventName.LiveRadio.toString(),
					reportMap);
		}
		showDetailsOfRadioHelper(mediaItem, mediaCategoryType);
	}

	private void showDetailsOfRadioHelper(MediaItem mediaItem,
			MediaCategoryType mediaCategoryType) {
		try {
			if (mediaCategoryType == MediaCategoryType.TOP_ARTISTS_RADIO) {
				mDataManager.getRadioTopArtistSongs(mediaItem, this);
			} else if (mediaCategoryType == MediaCategoryType.LIVE_STATIONS) {
				playLiveStation(mediaItem);
			}
		} catch (Exception e) {
			if (mediaItem instanceof LiveStation) {
				playLiveStation(mediaItem);
			} else if (mediaCategoryType == MediaCategoryType.LIVE_STATIONS) {
				// mDataManager.getRadioTopArtistSongs(mediaItem, this);
				mDataManager.getLiveRadioDetails("" + mediaItem.getId(), this);
			}
		}
	}

	private void playLiveStation(MediaItem mediaItem) {
		if(mPlayerBarFragment!=null){
			mPlayerBarFragment.currentRadioMediaItem = null;
		}
		LiveStation liveStation = (LiveStation) mediaItem;

		Track liveStationTrack = new Track(liveStation.getId(),
				liveStation.getTitle(), liveStation.getDescription(), null,
				liveStation.getImageUrl(), liveStation.getImageUrl(),
				mediaItem.getImages(), mediaItem.getAlbumId());
		if (CacheManager.isProUser(mContext)
				&& !TextUtils.isEmpty(liveStation.getStreamingUrl_320())) {
			liveStationTrack.setMediaHandle(liveStation.getStreamingUrl_320());
		} else if (CacheManager.isProUser(mContext)
				&& !TextUtils.isEmpty(liveStation.getStreamingUrl_128())) {
			liveStationTrack.setMediaHandle(liveStation.getStreamingUrl_128());
		} else {
			liveStationTrack.setMediaHandle(liveStation.getStreamingUrl());
		}

		List<Track> liveStationList = new ArrayList<Track>();
		liveStationList.add(liveStationTrack);

		/*
		 * sets to each track a reference to a copy of the original radio item.
		 * This to make sure that the player bar can get source Radio item
		 * without leaking this activity!
		 */
		for (Track track : liveStationList) {
			track.setTag(liveStation);
		}


		// Patibandha
		/*if(isCastConnected() && isCastPlaying()){
			onPauseCast();
		}*/

		// starts to play.
		getPlayerBar().playRadio(liveStationList, PlayMode.LIVE_STATION_RADIO);
	}

	/**
	 * Display showcase view help for Music filter menu
	 */
	private void DisplayHelp() {
		try {
			if (mApplicationConfigurations.getAppOpenCount() >= 2
					&& !mApplicationConfigurations.isHomeLandingHelpDisplayed()
					&& !mApplicationConfigurations.isEnabledHomeGuidePage()) {
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						try {
							if (!isShowcaseShowing) {
								isShowcaseShowing = true;
								if (menu.getVisibility() == View.VISIBLE) {
									RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(
											ViewGroup.LayoutParams.WRAP_CONTENT,
											ViewGroup.LayoutParams.WRAP_CONTENT);
									lps.addRule(RelativeLayout.CENTER_IN_PARENT);
									lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
									int margin = ((Number) (getResources()
											.getDisplayMetrics().density * 12))
											.intValue();
									lps.setMargins(
											2 * margin,
											margin,
											2 * margin,
											mDataManager
													.getShowcaseButtonMargin()
													* margin);

									ViewTarget target = new ViewTarget(menu);
									ShowcaseView sv = new ShowcaseView.Builder(
											HomeActivity.this, false)
											.setTarget(target)
											.setContentTitle(
													R.string.showcase_home_screen_filter_title)
											.setContentText(
													R.string.showcase_home_screen_filter_message)
											.setStyle(
													R.style.CustomShowcaseTheme2)
											.setShowcaseEventListener(
													HomeActivity.this)
											.hideOnTouchOutside().build();
									sv.setBlockShowCaseTouches(true);
									sv.setButtonPosition(lps);
									mApplicationConfigurations
											.setHomeLandingHelpDisplayed(true);
								}
							}
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
					}
				}, 1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean needToShowAirplaneDialog = false;

	private BroadcastReceiver AireplaneModeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Utils.isDeviceAirplaneModeActive(context)) {
				if (HungamaApplication.isActivityVisible()) {
					needToShowAirplaneDialog = false;
					Intent i = new Intent(context, OfflineAlertActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(i);
				} else {
					needToShowAirplaneDialog = true;
				}
			}
		}
	};

	@Override
	public void onMediaItemOptionPlayAndOpenSelected(MediaItem mediaItem,
			int position) {
		lastDetailedPosition = -5;
		Logger.i(TAG, "Play Now: " + mediaItem.getId());
		if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				try {
					findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
				} catch (Exception e) {
				}

				Track track = new Track(mediaItem.getId(),
						mediaItem.getTitle(), mediaItem.getAlbumName(),
						mediaItem.getArtistName(), mediaItem.getImageUrl(),
						mediaItem.getBigImageUrl(), mediaItem.getImages(),
						mediaItem.getAlbumId());
				List<Track> tracks = new ArrayList<Track>();
				tracks.add(track);
				mPlayerBar.playNow(tracks, null, null);
			} else {
				Logger.i("MediaTilesAdapter",
						"Play button click: Media detail OnSuccess 2");

				String mediaDeatils = null;
				if (mediaItem.getMediaType() == MediaType.ALBUM)
					mediaDeatils = DBOHandler.getAlbumDetails(mContext, ""
							+ mediaItem.getId());
				else if (mediaItem.getMediaType() == MediaType.PLAYLIST)
					mediaDeatils = DBOHandler.getPlaylistDetails(mContext, ""
							+ mediaItem.getId());
				lastDetailedPosition = position;
				showMediaDetails(mediaDeatils, mediaItem, true, true);
			}
		}
	}

	/**
	 * Used for app initialization purpose. It will reload previous session
	 * playlist in player and handles push notification depplinking. Call it
	 * after media content loaded, so it will do remaining operation after
	 * content load.
	 */
	public void MediaContentLoaded() {
		Logger.s("isOnCreate ::::::::::::::: " + isOnCreate);
		if (isOnCreate) {
			isOnCreate = false;
			try {
				findViewById(R.id.progressbar).setVisibility(View.GONE);
			} catch (Exception e) {
			}

			// Dsiplay Trial days left popup on app open.
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {

					Logger.s("isOnCreate ::::::::::::::: "
							+ getIntent().getStringExtra(
							SELECTED_GLOBAL_MENU_ID));

					if (getIntent().getStringExtra(
							SELECTED_GLOBAL_MENU_LINK_TYPE) != null) {

						GlobalMenuFragment temp;
						if (mainSettingsFragment == null)
							temp=getGlobalMenu();
						else
							temp=mainSettingsFragment;

						Category cat = temp.new Category(
								"", null, "", getIntent().getStringExtra(
								SELECTED_GLOBAL_MENU_ID),
								getIntent().getStringExtra(
										SELECTED_GLOBAL_MENU_LINK_TYPE),
								getIntent().getStringExtra(
										SELECTED_GLOBAL_MENU_HTML_URL),
								getIntent().getStringExtra(
										SELECTED_GLOBAL_MENU_ID_POPUP_MSG), null);
						onGlobalMenuItemSelected(cat, null);
						getIntent().removeExtra(SELECTED_GLOBAL_MENU_ID);
						getIntent().removeExtra(SELECTED_GLOBAL_MENU_HTML_URL);
						getIntent().removeExtra(
								SELECTED_GLOBAL_MENU_ID_POPUP_MSG);
						getIntent().removeExtra(SELECTED_GLOBAL_MENU_LINK_TYPE);
					}

					if (getIntent().getBooleanExtra(SELECTED_SEARCH_OPTION,
							false)) {
						openSearch(true, true);
						//getIntent().removeExtra(SELECTED_SEARCH_OPTION);
					}

					if (getIntent()
							.getBooleanExtra(IS_FROM_PLAYER_QUEUE, false)) {
						getIntent().removeExtra(IS_FROM_PLAYER_QUEUE);
						if (getIntent().getIntExtra(PLAYER_QUEUE_ACTION, 0) == 0)
							mPlayerBar.playFromPosition(getIntent()
									.getIntExtra(PLAY_FROM_POSITION, 0));
						else {
							mPlayerBar.openDrawerWithAction(0);
						}
					}

					if (getIntent().getBooleanExtra(IS_FROM_PLAYER_BAR, false)) {
						getIntent().removeExtra(IS_FROM_PLAYER_BAR);
						mPlayerBar.openDrawerWithAction(getIntent()
								.getIntExtra(PLAYER_BAR_ACTION, 0));
					}

//					mDataManager.getStoredCurrentPlan(new ReadCallback() {
//						@Override
//						public void onRead(Object respose) {
//							if (respose != null
//									&& respose instanceof SubscriptionCheckResponse) {
//								SubscriptionCheckResponse subscriptionCheckResponse = (SubscriptionCheckResponse) respose;
//								if (subscriptionCheckResponse
//										.getCode()
//										.equalsIgnoreCase(
//												UpgradeActivity.PASSWORD_SMS_SENT)) {
//
//									if (subscriptionCheckResponse.getPlan()
//											.isTrial()) {
//										Utils.makeText(
//												HomeActivity.this,
//												subscriptionCheckResponse
//														.getPlan()
//														.getTrailExpiryDaysLeft()
//														+ Utils.getMultilanguageText(
//														mContext,
//														" days left."),
//												Toast.LENGTH_SHORT).show();
//									}
//								}
//
//							}
//						}
//					});

					DisplayHelp();
					setInAppPromts();
					mDataManager.getPromoUnit(HomeActivity.this);

					updateTrackLanguage();
				}
			}, 300);

		}

		if (getIntent().getBooleanExtra("search", false)) {
			openSearch(true,true);
			//getIntent().removeExtra("search");
			set = true;
		}

		if (getIntent().getBooleanExtra("song_catcher", false)) {
			openSearch(true,true);
			//getIntent().removeExtra("song_catcher");
			set = true;
		}

		if (!isAutoSaveOfflineEnabled && CacheManager.isProUser(mContext)
				&& mApplicationConfigurations.getSaveOfflineAutoSaveMode()) {
			isAutoSaveOfflineEnabled = true;
			mPlayerBar.startAutoSavingPlayerQueue();
		}

//		System.gc();

		navrasOfferCheck();
	}

	private void navrasOfferCheck() {

		// Check networkCheck api response and alert if needed.
		try {
			String session = mDataManager.getApplicationConfigurations()
					.getSessionID();
			Boolean isRealUser = mDataManager.getApplicationConfigurations()
					.isRealUser();
			if (TextUtils.isEmpty(session) || !isRealUser) {
				if (OnApplicationStartsActivity.networkCheckResponse != null) {
					JSONObject json = new JSONObject(
							OnApplicationStartsActivity.networkCheckResponse);
					JSONObject jsonResponse = json.getJSONObject("response");
					try {
						if (jsonResponse.getInt("pro_redirect") == 1) {
							mDataManager.getApplicationConfigurations()
									.checkForProUser(true);
						} else {
							mDataManager.getApplicationConfigurations()
									.checkForProUser(false);
						}
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
					if (jsonResponse.getInt("code") == 1
							&& jsonResponse.getInt("display") == 1) {
						String networkMessage = jsonResponse
								.getString("message");
						if (networkMessage != null
								&& networkMessage.length() > 0) {
							CustomAlertDialog alertBuilder = new CustomAlertDialog(
									this);
							alertBuilder.setMessage(Utils.getMultilanguageText(
									mContext, networkMessage));
							if (jsonResponse.getInt("signup") == 1) {
								String msisdn = jsonResponse
										.getString("msisdn");
								if (msisdn != null) {
									mApplicationConfigurations
											.setNawrasMsisdn(msisdn);
								} else {
									mApplicationConfigurations
											.setNawrasMsisdn("");
								}
								alertBuilder.setPositiveButton(Utils
										.getMultilanguageText(mContext, "Ok"),
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												Intent startLoginActivityIntent = new Intent(
														getApplicationContext(),
														LoginActivity.class);
												startLoginActivityIntent
														.putExtra(
																ARGUMENT_HOME_ACTIVITY,
																"home_activity");
												startLoginActivityIntent
														.putExtra(
																LoginActivity.FLURRY_SOURCE,
																FlurryConstants.FlurrySourceSection.Home
																		.toString());
												startLoginActivityIntent
														.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
												startActivityForResult(
														startLoginActivityIntent,
														LOGIN_ACTIVITY_CODE);
											}
										});
							} else
								alertBuilder.setPositiveButton(Utils
										.getMultilanguageText(mContext, "Ok"),
										null);
							alertBuilder.show();
						}
					}
					OnApplicationStartsActivity.networkCheckResponse = null;
				}
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		Logger.e("onTrimMemory", "onTrimMemory" + level);
		if (level != Activity.TRIM_MEMORY_RUNNING_MODERATE
				&& level != Activity.TRIM_MEMORY_MODERATE)
			Utils.clearCache(true);
	}

	@Override
	public void onShowDetails(MediaItem mediaItem, List<MediaItem> items,
			boolean addToQueue) {
	}

	private void updateTrackLanguage() {

		if (getIntent().getBooleanExtra("skip_ad", false))
			startService(new Intent(this, ReloadTracksDataService.class));
	}

	public static void resetMatrix(Context c) {
		Logger.s("4 HomeTime:" + System.currentTimeMillis());
		final WindowManager w = (WindowManager) c
				.getSystemService(Context.WINDOW_SERVICE);
		final Display d = w.getDefaultDisplay();
		metrics = new DisplayMetrics();
		d.getMetrics(metrics);
		if (c instanceof Activity)
			((Activity) c).getWindowManager().getDefaultDisplay()
					.getMetrics(metrics);
	}


	Activity act;
	private void handleBrowserDeepLinkFirstTime() {
		if (BrowserIntentReceiverActivity.data != null){
			Intent intent = new Intent(HomeActivity.this, BrowserIntentReceiverActivity.class);
			startActivity(intent);
		}
	}

	private void call_gcmtoken(String gcmtoken) {

        try {

            if (!mApplicationConfigurations.getGcmToken()) {

                final String regId = gcmtoken;

                if (regId == null || TextUtils.isEmpty(regId)) {


                } else {
                    //ApplicationConfigurations appConfig = new ApplicationConfigurations(this);
                    mDataManager = DataManager.getInstance(mContext);
                    String sessionId = mApplicationConfigurations.getSessionID();
                    if (sessionId != null
                            && (sessionId.length() != 0
                            && !sessionId.equalsIgnoreCase("null") && !sessionId
                            .equalsIgnoreCase("none"))) {
                        mDataManager.getTokenUpdate(regId,
                                new CommunicationOperationListener() {

                                    @Override
                                    public void onSuccess(int operationId,
                                                          Map<String, Object> responseObjects) {

                                        mApplicationConfigurations.setGcmToken(true);
                                    }

                                    @Override
                                    public void onStart(int operationId) {

                                    }

                                    @Override
                                    public void onFailure(int operationId,
                                                          ErrorType errorType,
                                                          String errorMessage) {
                                    }
                                });
                        Logger.i("gcmtoken","1");
                    }
                }
            }
        }catch (Exception e) {
            Logger.i("gcmtoken","2");
                e.printStackTrace();
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
                    || networkType.equalsIgnoreCase(Utils.NETWORK_3G) || networkType.equalsIgnoreCase(Utils.NETWORK_4G)) {
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


    /*private void loadRemoteMedia(int position, boolean autoPlay, MediaInfo mSelectedMedia) {
		if(PlayerService.service!=null && PlayerService.service.isPlaying() && PlayerService.service.getState() == PlayerService.State.PLAYING){
			PlayerService.service.pause();
		}
        mCastManager.startVideoCastControllerActivity(HomeActivity.this, mSelectedMedia, position, autoPlay);
        //mCastManager.addVideoCastConsumer(mCastConsumer1);

    }

@Override
    public void onConnectionStateChanged(boolean b) {
        Logger.e(TAG, " ::::::::::::::::::>> onConnectionStateChanged");
        updateUI();
    }


    private void InitCastDetail() {
        googleEmailId = Utils.getAccountName(getApplicationContext());
        networkSpeed = getNetworkBandwidth();
        networkType = Utils.getNetworkType(this);
        contentFormat = "high"; // mApplicationConfigurations.getContentFormat();   //"high";
    }

	private void updateUI() {
        if (Utils.isCarMode()) {
            Logger.e(TAG, " ::::::::::::::::::>> updateUI CarMode");
//            startActivity(new Intent(this, CarModeHomeActivity.class));

            findViewById(R.id.iv_bg_car_splash).setVisibility(View.VISIBLE);

            Intent i = new Intent(HomeActivity.this, HomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("car_mode", true);
            startActivity(i);
}
}

    private boolean isFirstVideoCast = false;
	public boolean isTileClick = false;
    private MediaItem tempMediaItem, tempCurrentMediaItem;
    public List<MediaItem> tempMediaItemList;
    private String googleEmailId, networkType, contentFormat;
    private int networkSpeed;
    // private ChromeCastPlayback.Callback mCallback;
    // private CastPlayback mPlayback;
    public int mCurruntVideoPosition;
    String mCurrentMediaId;

	CastPlayEventTracking castPlayEvent = null;

    public void updatePlaybackState() {
        int status = mCastManager.getPlaybackStatus();
        int idleReason = mCastManager.getIdleReason();

        Logger.d(TAG, "onRemoteMediaPlayerStatusUpdated " + status);

        // Convert the remote playback states to media playback states.
        switch (status) {
            case MediaStatus.PLAYER_STATE_IDLE:
                if (idleReason == MediaStatus.IDLE_REASON_FINISHED || idleReason == MediaStatus.IDLE_REASON_INTERRUPTED) {
					if(!isTileClick*//* && isRealUserCasting()*//*)
                    	mCurruntVideoPosition = getCurrentVideoCastPos() + 1;
                }
				Logger.i(TAG, "updatePlaybackState PLAYER_STATE_IDLE :::::::: " + mCurruntVideoPosition);
				stopCastNotification();
				if(idleReason == MediaStatus.IDLE_REASON_FINISHED){
					stopTracking(true);
				}else{
					stopTracking(false);
				}
                break;
            case MediaStatus.PLAYER_STATE_BUFFERING:
                //mState = PlaybackState.STATE_BUFFERING;
                break;
            case MediaStatus.PLAYER_STATE_PLAYING:
                //mState = PlaybackState.STATE_PLAYING;
                //Load next video
                try {
					Logger.i("", "VideoPosition ::: mCurruntVideoPosition:" + mCurruntVideoPosition);
                    Logger.i("", "VideoPosition ::: loadedCount:" + loadedCount);
					isTileClick = false;
					if(isRealUserCasting()) {
						if (mCurruntVideoPosition + 1 != loadedCount) {
							tempCurrentMediaItem =  tempMediaItem.getCopy();
							startTracking();
							stopCastNotification();
							startCastNotifications();
							MediaInfo mCompleteMediaItem = mCastManager.getRemoteMediaInformation();
							if (mCompleteMediaItem != null) {
								LoadNextVideoForCast(mCompleteMediaItem);
							}
						}
					}
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case MediaStatus.PLAYER_STATE_PAUSED:
                //mState = PlaybackState.STATE_PAUSED;
                updateMetadata();
                break;
            default: // case unknown
                Logger.d(TAG, "State default : " + status);
                break;
        }
    }

	public void stopTracking(boolean isCompleted){
		if (castPlayEvent != null) {
			try {
				MediaInfo mediaInfo = getCurrentMediaInfo();
				JSONObject customData = mediaInfo.getCustomData();
				String itemId = customData.get(ITEM_ID).toString();
				if (!TextUtils.isEmpty(itemId)) {
					CastPlayEventTracking.CastPlayDetail detail = castPlayEvent.mediaDetail.get(itemId);
					if(!TextUtils.isEmpty(detail.mediaId) && itemId.equals(detail.mediaId)){
						if(isCompleted)
							castPlayEvent.stopVideoPlayEvent(isCompleted, (int)mCastManager.getMediaDuration() ,itemId);
						else
							castPlayEvent.stopVideoPlayEvent(isCompleted, (int)mCastManager.getCurrentMediaPosition() ,itemId);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			castPlayEvent = null;
		}
	}
	private void startTracking(){
		if (castPlayEvent == null) {
			try {
				MediaInfo mediaInfo = getCurrentMediaInfo();
				JSONObject customData = mediaInfo.getCustomData();
				String itemId = customData.get(ITEM_ID).toString();
				String itemTitle = customData.get(ITEM_TITLE).toString();
				String deliveryId = customData.get(DELIVERY_ID).toString();
				if (!TextUtils.isEmpty(itemId)) {
					castPlayEvent = CastPlayEventTracking.getInstance(HomeActivity.this);
					castPlayEvent.startTracking(itemId, itemTitle, Long.parseLong(deliveryId));
				}
			} catch (Exception e) {
			}
		} *//*else {
			try {
				MediaInfo mediaInfo = getCurrentMediaInfo();
				JSONObject customData = mediaInfo.getCustomData();
				String itemId = customData.get(ITEM_ID).toString();
				if (!TextUtils.isEmpty(itemId)) {
					CastPlayEventTracking.CastPlayDetail detail = castPlayEvent.mediaDetail.get(itemId);
					if(!TextUtils.isEmpty(detail.mediaId) && !itemId.equals(detail.mediaId)){

					}
				}
			} catch (Exception e) {
			}
		}*//*
	}

	private MediaInfo getCurrentMediaInfo(){
		try {
			MediaQueue queue = mCastManager.getMediaQueue();
			if (!queue.isEmpty()) {
				MediaQueueItem detail = queue.getCurrentItem();
				MediaInfo mediaInfo = detail.getMedia();
				return  mediaInfo;
			}
		} catch (Exception e) {
		}
		return  null;
	}


    int loadedCount = 0;

    private boolean isAd(MediaItem mediaItem) {
        if (mediaItem != null) {
            String title = mediaItem.getTitle();
            String albumname = mediaItem.getAlbumName();
            String artistname = mediaItem.getArtistName();
            if (!TextUtils.isEmpty(title) && title.equalsIgnoreCase("no")
                    && !TextUtils.isEmpty(albumname)
                    && albumname.equalsIgnoreCase("no")
                    && !TextUtils.isEmpty(artistname)
                    && artistname.equalsIgnoreCase("no")) {
                return true;
            }
        }
        return false;
    }


    private void LoadNextVideoForCast(MediaInfo mediaItem) {
        loadedCount = mCurruntVideoPosition + 1;
        tempMediaItem = tempMediaItemList.get(loadedCount);
        if(isAd(tempMediaItem)){
            mCurruntVideoPosition = mCurruntVideoPosition + 1;
            loadedCount = mCurruntVideoPosition + 1;
            tempMediaItem = tempMediaItemList.get(loadedCount);
        }
        if(!isAd(tempMediaItem) && tempMediaItem!=null) {
            mDataManager.getVideoDetailsAdp(tempMediaItem,
                    networkSpeed, networkType,
                    contentFormat, HomeActivity.this, googleEmailId);
        }
    }

    private void updateMetadata() {
        // Sync: We get the customData from the remote media information and update the local
        // metadata if it happens to be different from the one we are currently using.
        // This can happen when the app was either restarted/disconnected + connected, or if the
        // app joins an existing session while the Chromecast was playing a queue.
        try {
            MediaInfo mediaInfo = mCastManager.getRemoteMediaInformation();
            if (mediaInfo == null) {
                return;
            }
            JSONObject customData = mediaInfo.getCustomData();

            if (customData != null && customData.has(ITEM_ID)) {
                String remoteMediaId = customData.getString(ITEM_ID);
                if (!TextUtils.equals(mCurrentMediaId, remoteMediaId)) {
                    mCurrentMediaId = remoteMediaId;
							 *//*if (mCallback != null) {
								 mCallback.onMetadataChanged(remoteMediaId);
							 }*//*

                }
            }
        } catch (TransientNetworkDisconnectionException | NoConnectionException | JSONException e) {
            Logger.d(TAG, "Exception processing update metadata");
        }

    }


    public boolean isVideoTrackRunning() {
        boolean isVideoTrackLoading = true;
        try {
            MediaQueue queue = mCastManager.getMediaQueue();
            if (!queue.isEmpty()) {
                List<MediaQueueItem> list = queue.getQueueItems();
                for (int i = 0; i < list.size(); i++) {
                    MediaQueueItem detail = list.get(i);
                    MediaInfo mediaInfo = detail.getMedia();
                    if (mediaInfo != null) {
                        JSONObject customData = mediaInfo.getCustomData();
                        String itemId = customData.get(ITEM_ID).toString();
                        String isVideo = customData.get(IS_VIDEO).toString();
                        if (!TextUtils.isEmpty(isVideo) && isVideo.equals("0")) {
                            mCastManager.queueRemoveItem(Integer.parseInt(itemId),customData);
                            isVideoTrackLoading = false;
                        }
                    }
                }
            }
            queue = mCastManager.getMediaQueue();
            if (!queue.isEmpty()) {
                List<MediaQueueItem> list = queue.getQueueItems();
            }
        } catch (Exception e) {

        }
        return isVideoTrackLoading;
    }

	public boolean isVideoInQueue(MediaItem mediaItem) {
		boolean isVideoInQueue = false;
		try {
			MediaQueue queue = mCastManager.getMediaQueue();
			if (!queue.isEmpty()) {
				List<MediaQueueItem> list = queue.getQueueItems();
				for (int i = 0; i < list.size(); i++) {
					MediaQueueItem detail = list.get(i);
					MediaInfo mediaInfo = detail.getMedia();
					if (mediaInfo != null) {
						JSONObject customData = mediaInfo.getCustomData();
						String itemId = customData.get(ITEM_ID).toString();
						//String isVideo = customData.get(IS_VIDEO).toString();
						if (!TextUtils.isEmpty(itemId) && itemId.equals(mediaItem.getId()+"")) {
							isVideoInQueue = true;
							break;
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return isVideoInQueue;
	}

	public boolean isVideoPlaying(MediaItem mediaItem) {
		boolean isVideoPlaying = false;
		try {
			MediaQueue queue = mCastManager.getMediaQueue();
			if (!queue.isEmpty()) {
				MediaQueueItem detail = queue.getCurrentItem();
				MediaInfo mediaInfo = detail.getMedia();
				if (mediaInfo != null) {
					JSONObject customData = mediaInfo.getCustomData();
					String itemId = customData.get(ITEM_ID).toString();
					if (!TextUtils.isEmpty(itemId) && itemId.equals(mediaItem.getId() + "")) {
						isVideoPlaying = true;
					}
				}
			}
		} catch (Exception e) {
		}
		return isVideoPlaying;
	}

	private void startCastNotifications(){
		*//*if(!HungamaApplication.isActivityVisible() && isCastPlaying())
			mCastManager.startNotificationService();*//*
	}
	public void stopCastNotification(){
		if(HungamaApplication.isActivityVisible())
			mCastManager.stopNotificationService();
	}

	public void castMenuItemHideShow(boolean needToHide){
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if(mMenu==null)
					return;
				MenuItem item = mMenu.findItem(R.id.media_route_menu_item);
				if(item ==null)
					return;

				if(pager.getCurrentItem() == 0) {
					//if(item.isVisible())
					item.setVisible(false);
				}
				else {
					//if(!item.isVisible())
					item.setVisible(true);
				}
			}
		}, 500);

	}

	private boolean isRealUserCasting() {
		boolean isDevicePlaying = false;
		try {
			MediaQueue queue = mCastManager.getMediaQueue();
			if (!queue.isEmpty()) {
				MediaQueueItem detail = queue.getCurrentItem();
				MediaInfo mediaInfo = detail.getMedia();
				if (mediaInfo != null) {
					isDevicePlaying =(Utils.getAndroidId(HomeActivity.this).equals(Utils.getDeviceId(mediaInfo)));
				}
			}
		} catch (Exception e) {
		}
		Logger.i("isRealUser", "isRealUser:"+isDevicePlaying);
		return isDevicePlaying;
	}

	private void setCastCurrentIndex(){
		if(mCastManager!=null && isCastConnected() && isCastPlaying()) {
			getStoreListFromCastManager();
			if(tempMediaItemList!=null && tempMediaItemList.size()>0) {
				try {
					MediaQueue queue = mCastManager.getMediaQueue();
					if (!queue.isEmpty()) {
						MediaQueueItem detail = queue.getCurrentItem();
						MediaInfo mediaInfo = detail.getMedia();//VideoPosition :::
						if (mediaInfo != null) {
							JSONObject customData = mediaInfo.getCustomData();
							String itemId = customData.get(ITEM_ID).toString();
							MediaItem mediaItem;
							for (int i = 0; i < tempMediaItemList.size(); i++) {
								mediaItem = tempMediaItemList.get(i);
								if (!TextUtils.isEmpty(itemId) && itemId.equals(mediaItem.getId() + "")) {
									Logger.i("setCastCurrentIndex ", "setCastCurrentIndex mCurruntVideoPosition :" + i);
									mCurruntVideoPosition = i;
									if (mCurruntVideoPosition == tempMediaItemList.size() - 1)
										loadedCount = i;
									else
										loadedCount = i + 1;
									break;
								}
							}
						}
					}
				} catch (Exception e) {
				}
			}
		}
	}

	private void storeListInCastManager(){
		List<Object> list = new ArrayList<>();
		for (int i = 0; i < tempMediaItemList.size(); i++) {
			list.add(tempMediaItemList.get(i));
		}
		mCastManager.storeList(list);
	}


	private void getStoreListFromCastManager(){
		List<Object> list = mCastManager.getStoreList();
		if(list == null)
			return;
		if(tempMediaItemList!=null && tempMediaItemList.size()>0)
			return;
		tempMediaItemList = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			tempMediaItemList.add((MediaItem)list.get(i));
		}
	}

	private int getCurrentVideoCastPos() {
		int pos = mCurruntVideoPosition;
		if (tempMediaItemList != null && tempMediaItemList.size() > 0) {
			try {

				Logger.i("getCurrentVideoCastPos","getCurrentVideoCastPos:::::::::::1 "+ tempMediaItemList.indexOf(tempCurrentMediaItem));

				MediaQueue queue = mCastManager.getMediaQueue();
				if (!queue.isEmpty()) {
					MediaQueueItem detail = queue.getCurrentItem();
					MediaInfo mediaInfo = detail.getMedia();//VideoPosition :::
					if (mediaInfo != null) {
						JSONObject customData = mediaInfo.getCustomData();
						String itemId = customData.get(ITEM_ID).toString();
						MediaItem mediaItem;
						for (int i = 0; i < tempMediaItemList.size(); i++) {
							mediaItem = tempMediaItemList.get(i);
							if (!TextUtils.isEmpty(itemId) && itemId.equals(mediaItem.getId() + "")) {
								Logger.i("setCastCurrentIndex ", "getCurrentVideoCastPos:::::::::::2 " + i);
								pos = i;
								break;
							}
						}
					}
				}
			} catch (Exception e) {
			}
		}
		return pos;
	}

	// Patibandha
	public void onPauseCasting(){
		if (mCastManager!=null && isCastPlaying()) {
			stopCastNotification();
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
		}
	}*/

}
