package com.hungama.myplay.activity.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.HungamaApplication;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.LeftMenuExtraData;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MusicCategoriesResponse;
import com.hungama.myplay.activity.data.dao.hungama.MusicCategoryGenre;
import com.hungama.myplay.activity.data.dao.hungama.MyPreferencesResponse;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionStatusResponse;
import com.hungama.myplay.activity.gigya.InviteFriendsActivity;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MediaCategoriesOperation;
import com.hungama.myplay.activity.operations.hungama.PreferencesRetrieveOperation;
import com.hungama.myplay.activity.operations.hungama.PreferencesSaveOperation;
import com.hungama.myplay.activity.operations.hungama.SubscriptionCheckOperation;
import com.hungama.myplay.activity.ui.dialogs.CustomProgressDialog;
import com.hungama.myplay.activity.ui.dialogs.GenreSelectionDialogNew;
import com.hungama.myplay.activity.ui.dialogs.MyProgressDialog;
import com.hungama.myplay.activity.ui.dialogs.OtpConfirmationDialog;
import com.hungama.myplay.activity.ui.dialogs.RadioFullPlayerInfoDialog;
import com.hungama.myplay.activity.ui.dialogs.RedeeomCouponDialog;
import com.hungama.myplay.activity.ui.dialogs.VerifyMobileNumberDialog;
import com.hungama.myplay.activity.ui.fragments.CommentsFragment;
import com.hungama.myplay.activity.ui.fragments.GlobalMenuFragment;
import com.hungama.myplay.activity.ui.fragments.GlobalMenuFragment.Category;
import com.hungama.myplay.activity.ui.fragments.GlobalMenuFragment.OnGlobalMenuItemSelectedListener;
import com.hungama.myplay.activity.ui.fragments.LanguageSettingsFragment;
import com.hungama.myplay.activity.ui.fragments.MainSearchFragment;
import com.hungama.myplay.activity.ui.fragments.MainSearchFragmentNew;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment;
import com.hungama.myplay.activity.ui.widgets.CustomAlertDialog;
import com.hungama.myplay.activity.ui.widgets.HomeTabBar;
import com.hungama.myplay.activity.ui.widgets.LanguageButtonLollipop;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Appirater;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.QuickActionEditerPics;
import com.hungama.myplay.activity.util.QuickActionEditerPics.OnEditerPicsSelectedListener;
import com.hungama.myplay.activity.util.ScreenLockStatus;
import com.hungama.myplay.activity.util.Utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Main Activity of the application, supports handling the ActionBar and the
 * Player Bar.
 */
public abstract class MainActivity extends ActionBarActivity implements
		OnGlobalMenuItemSelectedListener, // OnQuickNavigationItemSelectedListener,
		OnEditerPicsSelectedListener {

	public static final String ITEM_ID = "itemId";
	public static final String ITEM_TITLE = "title";
	public static final String IS_VIDEO = "isVideo";
	public static final String DEVICE_ID = "DeviceId";
	public static final String DELIVERY_ID = "DeliveryId";

	public static final String VIDEO_CURRENT_POS = "video_current_pos";

	public static final int LOGIN_ACTIVITY_CODE = 100;
	public static final int PROFILE_ACTIVITY_CODE = 1011;
	public static final int RECHARGE_ACTIVITY_CODE = 1012;

	private static final String TAG = "MainActivity";
    public boolean needToOpenSearchActivity;

	/**
	 * Extra to launch a Message ID.
	 */
	// public static final String EXTRA_MESSAGE_ID =
	// "com.hungama.myplay.activity.EXTRA_MESSAGE_ID";
	/**
	 * Extra to select what item the fragment. Either {@link #HOME_ITEM} or
	 * {@link #INBOX_ITEM}.
	 */
	// public static final String EXTRA_NAVIGATE_ITEM =
	// "com.hungama.myplay.activity.EXTRA_NAVIGATE_ITEM";
	/**
	 * Home fragment position.
	 */
	// public static final int HOME_ITEM = 0;

	/**
	 * Inbox fragment position.
	 */
	// public static final int INBOX_ITEM = 1;

	// protected static final String FRAGMENT_TAG_HOME = "fragment_tag_home";
	public static final String FRAGMENT_TAG_MAIN_GLOBAL_MENU = "fragment_tag_main_global_menu";
	// protected static final String FRAGMENT_TAG_MAIN_QUICK_NAVIGATION =
	// "fragment_tag_main_quick_navigation";
	protected static final String FRAGMENT_TAG_MAIN_SEARCH = "fragment_tag_main_search";
	protected static final String FRAGMENT_TAG_MAIN_PLAYER_BAR = "fragment_tag_main_player_bar";

	public static final String ACTION_OFFLINE_MODE_CHANGED = "com.hungama.myplay.activity.intent.action.mode_changed";
	public static final String ACTION_LANGUAGE_CHANGED = "com.hungama.myplay.activity.intent.action.language_changed";
	public static final String ACTION_PLAYER_DRAWER_OPEN = "playerdraweropen";

	public static final String SELECTED_GLOBAL_MENU_ID = "global_menu_id";
	public static final String SELECTED_GLOBAL_MENU_LINK_TYPE = "global_menu_link_type";
	public static final String SELECTED_GLOBAL_MENU_HTML_URL = "global_menu_html_url";
	public static final String SELECTED_GLOBAL_MENU_ID_POPUP_MSG = "global_menu_popup_msg";

	public static final String SELECTED_SEARCH_OPTION = "selected_search_option";
	public static final String PLAY_FROM_POSITION = "play_from_position";
	public static final String PLAYER_BAR_ACTION = "player_bar_action";
	public static final String PLAYER_QUEUE_ACTION = "player_queue_action";
	public static final String IS_FROM_PLAYER_QUEUE = "from_player_queqe";
	public static final String IS_FROM_PLAYER_BAR = "from_player_bar";
    public static final String EXTRA_DATA_MEDIA_ITEM = "extra_data_media_item";
    public static final String EXTRA_DATA_DO_SHOW_TITLE = "extra_data_do_show_title";
    public static final String FLURRY_SOURCE_SECTION = "flurry_source_section";

	protected Appirater mAppirater;

	private FragmentManager mFragmentManager;
	private ActionBar mActionBar;
	protected Menu mMenu;
	private MenuItem mLastSelectedMenuItem;
	public PlayerBarFragment mPlayerBarFragment = null;
	private boolean mIsResumed = false;

	/*
	 * Manages all the Quick Navigation Current / Selected Item.
	 */
	private static NavigationItem mCurrentNavigationItem = null;
	private static NavigationItem mLastNavigationItem = null;

	private volatile boolean mIsDestroyed = false;

	public DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	// ======================================================
	// ACTIVITY'S LIFECYCLE.
	// ======================================================

	// private MenuItem currentItem;
	// private int newItemId;

	// private static FragmentManager fragmentManager;
	// private ActionBarDrawerToggle mActionBarDrawerToggle;
	// private static boolean isOfflineMode = false;

    public void setNeedToOpenSearchActivity(boolean needToOpenSearchActivity) {
        this.needToOpenSearchActivity = needToOpenSearchActivity;
    }


    public Toolbar mToolbar;
	public GlobalMenuFragment mainSettingsFragment;

	protected void getDrawerLayout() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		// mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		// setSupportActionBar(mToolbar);
		mDrawerLayout.setDrawerListener(new DemoDrawerListener());
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

		mActionBarHelper = createActionBarHelper();
		mActionBarHelper.init();

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				mToolbar, R.string.drawer_open, R.string.drawer_close);

		setUpNavigationFragment();

		mDrawerToggle.syncState();

	}

	protected GlobalMenuFragment getGlobalMenu(){
		GlobalMenuFragment temp = new GlobalMenuFragment();
		temp.setOnGlobalMenuItemSelectedListener(this);
		return temp;
	}

	protected void setUpNavigationFragment(){
		mainSettingsFragment = new GlobalMenuFragment();
		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();
		mainSettingsFragment.setOnGlobalMenuItemSelectedListener(this);

		fragmentTransaction.add(R.id.left_drawer, mainSettingsFragment,
				FRAGMENT_TAG_MAIN_GLOBAL_MENU);
		fragmentTransaction.addToBackStack(FRAGMENT_TAG_MAIN_GLOBAL_MENU);
		fragmentTransaction.commit();
	}

	// int selectedPreferenceTab = -1;
	private static final int SUCCESS = 1;
	// private TextView mTitleBarText;
	// private List<Category> mCategories;
	private MusicCategoriesResponse musicCategoriesResponse;

	// private List<MyCategory> mCategoriesRetrieve;
	/**
	 * Configures the action bar to have a navigation list of 'Home' and 'Inbox'
	 */

	protected void setOverlayAction() {
		if(getWindow()!=null)
			getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
	}

	void showCategoryActionBar() {
		showCategoryActionBar(false);
	}

	void showCategoryActionBar(boolean isFromCreate) {
		try {

		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayUseLogoEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(false);
		LanguageButtonLollipop btn_preference = (LanguageButtonLollipop) findViewById(R.id.btn_preferences);
		btn_preference.setVisibility(View.VISIBLE);

		if (!mApplicationConfigurations.getSaveOfflineMode()) {
			// selectedPreferenceTab = HomeTabBar.TAB_INDEX_MUSIC;
			if(!isFromCreate) {
				final String preferencesResponse = mDataManager
						.getApplicationConfigurations()
						.getMusicPreferencesResponse();
				if (TextUtils.isEmpty(preferencesResponse))
					mDataManager.getPreferences(preferenceOperation);
				else {
					try {
						if (musicCategoriesResponse != null) {
							populateCategories(musicCategoriesResponse);

							Map<String, Object> resultMap = new HashMap<String, Object>();
							resultMap
									.put(MediaCategoriesOperation.RESULT_KEY_OBJECT_CATEGORIES,
											musicCategoriesResponse);
							resultMap.put("already_saved", true);
							preferenceOperation
									.onSuccess(
											OperationDefinition.Hungama.OperationId.PREFERENCES_GET,
											resultMap);
							//
						} else {
							Map<String, Object> resultMap = new HashMap<String, Object>();
							resultMap
									.put(MediaCategoriesOperation.RESULT_KEY_OBJECT_CATEGORIES,
											new Gson().fromJson(
													preferencesResponse,
													MusicCategoriesResponse.class));
							resultMap.put("already_saved", true);
							preferenceOperation
									.onSuccess(
											OperationDefinition.Hungama.OperationId.PREFERENCES_GET,
											resultMap);
						}
					} catch (Exception e) {
						Logger.printStackTrace(e);
						mDataManager.getPreferences(preferenceOperation);
					}
				}
			}
		} else {
			Utils.setActionBarTitle(MainActivity.this, mActionBar,
					getResources().getString(R.string.application_name));
		}
		// }
		}catch (Exception e){
			e.printStackTrace();
		}catch (Error e){
			e.printStackTrace();
		}
	}

	public static boolean isUserPreferenceLoaded = false;

	private CommunicationOperationListener preferenceOperation = new CommunicationOperationListener() {
		@Override
		public void onSuccess(int operationId,
				final Map<String, Object> responseObjects) {
			try {

				if (operationId == OperationDefinition.Hungama.OperationId.PREFERENCES_RETRIEVE) {
					Logger.i(TAG, "Successed getting users Preferences.");

					MyPreferencesResponse myPreferencesResponse = (MyPreferencesResponse) responseObjects
							.get(PreferencesRetrieveOperation.RESPONSE_KEY_PREFERENCES_RETRIEVE);
					MediaContentType mediaContentType = (MediaContentType) responseObjects
							.get(PreferencesRetrieveOperation.RESPONSE_KEY_PREFERENCES_CONTENT_TYPE);

					Logger.e(
							"new Gson().toJson(myPreferencesResponse).toString()",
							new Gson().toJson(myPreferencesResponse).toString());
					String changedCategory = "";
					String changedGenre = "";
					try {
						// if (myPreferencesResponse.getMycategories() != null
						// && myPreferencesResponse.getMycategories()
						// .size() > 0) {
						// MyCategory myCategory = myPreferencesResponse
						// .getMycategories()
						// .get(0);
						// // if (mediaContentType == MediaContentType.VIDEO) {
						// // if (!mApplicationConfigurations
						// // .getSelctedVideoPreference()
						// // .equalsIgnoreCase(myCategory.getName())) {
						// // changedCategory = new Category(myCategory.getId(),
						// // myCategory.getName(),
						// // new ArrayList<CategoryTypeObject>());
						// // }
						// // mApplicationConfigurations
						// // .setSelctedVideoPreference(myCategory
						// // .getName()
						// // /* .toUpperCase() */);
						// // } else {
						// if (!mApplicationConfigurations
						// .getSelctedMusicPreference()
						// .equalsIgnoreCase(
						// myCategory.getName())) {
						// changedCategory = new Category(myCategory.getId(),
						// myCategory.getName(),
						// new ArrayList<CategoryTypeObject>());
						// }
						// mApplicationConfigurations
						// .setSelctedMusicPreference(myCategory
						// .getName()
						// /* .toUpperCase() */);
						// // }
						// } else {
						// List<MyCategory> mycategories = new
						// ArrayList<MyCategory>();
						// MyCategory myCategory = new MyCategory("0",
						// "Editors Picks");
						// mycategories.add(myCategory);
						// MyPreferencesResponse updatedPreferencesResponse =
						// new MyPreferencesResponse(
						// 0, "", 0, mycategories, myCategory.getName(), "");
						// String updatedPreferences = new Gson().toJson(
						// updatedPreferencesResponse).toString();
						// if (!TextUtils.isEmpty(updatedPreferences)) {
						// // if (mediaContentType == MediaContentType.VIDEO) {
						// // if (!mApplicationConfigurations
						// // .getSelctedVideoPreference()
						// // .equalsIgnoreCase(
						// // myCategory
						// // .getName())) {
						// // changedCategory = new Category(myCategory.getId(),
						// // myCategory.getName(),
						// // new ArrayList<CategoryTypeObject>());
						// // }
						// // // mDataManager.getApplicationConfigurations()
						// // // .setSelectedVideoPreferencesResponse(
						// // // updatedPreferences);
						// // mApplicationConfigurations
						// // .setSelctedVideoPreference(myCategory
						// // .getName().toUpperCase());
						// // } else {
						// if (!mApplicationConfigurations
						// .getSelctedMusicPreference()
						// .equalsIgnoreCase(
						// myCategory
						// .getName())) {
						// changedCategory = new Category(myCategory.getId(),
						// myCategory.getName(),
						// new ArrayList<CategoryTypeObject>());
						// }
						// mApplicationConfigurations
						// .setSelctedMusicPreference(myCategory
						// .getName().toUpperCase());
						// // }
						// }
						// }

						if (!TextUtils.isEmpty(myPreferencesResponse
								.getCategoryName())) {
							if (!mApplicationConfigurations
									.getSelctedMusicPreference()
									.equalsIgnoreCase(
											myPreferencesResponse
													.getCategoryName())) {
								changedCategory = myPreferencesResponse
										.getCategoryName();
							}
							mApplicationConfigurations
									.setSelctedMusicPreference(myPreferencesResponse
											.getCategoryName());
						}

						if (!TextUtils.isEmpty(myPreferencesResponse
								.getGenerName())) {
							if (!mApplicationConfigurations
									.getSelctedMusicGenre().equalsIgnoreCase(
											myPreferencesResponse
													.getGenerName())) {
								changedGenre = myPreferencesResponse
										.getGenerName();
							}
							mApplicationConfigurations
									.setSelctedMusicGenre(myPreferencesResponse
											.getGenerName());
						}

						isUserPreferenceLoaded = true;
					} catch (Exception e) {
					}

					if (musicCategoriesResponse != null) {
						populateCategories(musicCategoriesResponse);
					}
					if (!TextUtils.isEmpty(changedCategory)
							|| !TextUtils.isEmpty(changedGenre)) {
						savePreferences(0, changedCategory, mediaContentType,
								changedGenre, false);// Needed
					}

				} else if (operationId == OperationDefinition.Hungama.OperationId.PREFERENCES_GET) {
					Logger.i(TAG, "Successed getting categories.");

					musicCategoriesResponse = (MusicCategoriesResponse) responseObjects
							.get(MediaCategoriesOperation.RESULT_KEY_OBJECT_CATEGORIES);

					Logger.i(TAG, "Successed getting categories.");

					if (musicCategoriesResponse != null) {
						populateCategories(musicCategoriesResponse);
					}

					if (responseObjects.containsKey("already_saved")) {
						if (!mApplicationConfigurations
								.isMusicPreferencesResponseLoaded())
							mDataManager.getPreferences(preferenceOperation);
					} else {
						if (musicCategoriesResponse != null) {
							mDataManager.getApplicationConfigurations()
									.setMusicPreferencesResponse(
											new Gson().toJson(
													musicCategoriesResponse)
													.toString());
						}
					}

					if (!isUserPreferenceLoaded) {
						mDataManager.getMyPreferences(preferenceOperation);
					} else if (musicCategoriesResponse != null) {
						populateCategories(musicCategoriesResponse);
					}

					mApplicationConfigurations
							.setIsMusicPreferencesResponseLoaded(true);
				} else if (operationId == OperationDefinition.Hungama.OperationId.PREFERENCES_SAVE) {
					Logger.i(TAG, "Successed saving my preferences.");

					MyPreferencesResponse myPreferencesResponse = (MyPreferencesResponse) responseObjects
							.get(PreferencesSaveOperation.RESPONSE_KEY_PREFERENCES_SAVE);

					if (myPreferencesResponse.getCode() == SUCCESS
							|| myPreferencesResponse.getCode() == 200) {

						mApplicationConfigurations
								.setMusicLatestTimeStamp(null);
						mApplicationConfigurations
								.setMusicPopularTimeStamp(null);
						mApplicationConfigurations
								.setVideoLatestTimeStamp(null);
						mApplicationConfigurations.setLiveRadioTimeStamp(null);
						mApplicationConfigurations.setOnDemandTimeStamp(null);

//						mDataManager.getCacheManager()
//								.storeMusicLatestResponse("", null);
//						mDataManager.getCacheManager()
//								.storeMusicFeaturedResponse("", null);
//						mDataManager.getCacheManager()
//								.storeVideoLatestResponse("", null);
//						mDataManager.getCacheManager().storeLiveRadioResponse(
//								"", null);
//						mDataManager.getCacheManager().storeCelebRadioResponse(
//								"", null);

						mApplicationConfigurations
								.setSearchPopularTimeStamp(null);

						Intent new_intent = new Intent();
						new_intent
								.setAction(HomeActivity.ACTION_PREFERENCE_CHANGE);
						new_intent.putExtra("preference_change", true);
						new_intent.putExtra("selectedLanguage",
								selected_languge);
						sendBroadcast(new_intent);

						// mOnBrowseByCategoryItemSelectedListener
						// .onBrowseByCategoryItemSelected(true,
						// selectedLanguage);
						if (!isFinishing()) {
							Utils.makeText(
									MainActivity.this,
									getResources()
											.getString(
													R.string.my_preferences_saved_categories),
									Toast.LENGTH_LONG).show();

						}
					}
				} else if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_CATEGORIES) {

					MusicCategoriesResponse musicCategoriesResponse = (MusicCategoriesResponse) responseObjects
							.get(MediaCategoriesOperation.RESULT_KEY_OBJECT_CATEGORIES);
					if (musicCategoriesResponse != null) {
						populateCategories(musicCategoriesResponse);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				Logger.printStackTrace(e);
			}
		}

		@Override
		public void onStart(int operationId) {
			Logger.s("Operation id :::: " + operationId);
		}

		@Override
		public void onFailure(int operationId, ErrorType errorType,
				String errorMessage) {
			Logger.s("Operation id :::: " + operationId);
		}
	};

//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
//			// openSearch();
//			return true;
//		}
//		return super.onKeyDown(keyCode, event);
//	}

	// boolean isPreferencChanged=false;
	int lastItemPosition = -1;
	QuickActionEditerPics quickAction;

	@SuppressWarnings("deprecation")
	private void populateCategories(
			MusicCategoriesResponse musicCategoriesResponse) {
		List<String> categories = musicCategoriesResponse.getCategories();
		if (categories != null && categories.size() > 0) {
			ArrayAdapter<String> adapter = new ArrayAdapter(
					mActionBar.getThemedContext(), R.layout.simple_list_item_1,
					categories);

			adapter.setDropDownViewResource(R.layout.simple_layout);

			String editerPics[] = new String[categories.size()];
			for (int i = 0; i < categories.size(); i++) {
				editerPics[i] = categories.get(i);
			}

			quickAction = new QuickActionEditerPics(this, editerPics, false);
			quickAction.setOnEditerPicsSelectedListener(this);

			LanguageButtonLollipop btn_preference = (LanguageButtonLollipop) findViewById(R.id.btn_preferences);

			btn_preference.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					quickAction.show(v);
				}
			});

			btn_preference.setText("");

			for (int i = 0; i < categories.size(); i++) {
				if (mDataManager.getApplicationConfigurations()
						.getSelctedMusicPreference()
						.equalsIgnoreCase(categories.get(i))) {
					btn_preference.setText(categories.get(i));
					lastItemPosition = i;
					break;
				}
			}
		}
	}

	String selected_languge = "";

	public void savePreferences(int item_position, String categoryName,
			MediaContentType mediaContentType, String genre,
			boolean updateToServer) {
		selected_languge = categoryName;

		try {
			Set<String> tags = Utils.getTags();

			for (String category : musicCategoriesResponse.getCategories()) {
				if (categoryName.equals(category)) {
					tags.add(category.toLowerCase());
				} else {
					tags.remove(category.toLowerCase());
				}
			}

			for (MusicCategoryGenre category : musicCategoriesResponse
					.getGenres()) {
				for (String genreName : category.getGenre()) {
					tags.remove("genre_" + genreName);
				}
			}
			if (!TextUtils.isEmpty(genre)) {
				tags.add("genre_" + genre);
			}

			Utils.AddTag(tags);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (updateToServer) {
            mDataManager.saveMyPreferences("", preferenceOperation,
                    categoryName, genre);
        } else if(isUserLoggedIn) {
            mApplicationConfigurations
                    .setMusicLatestTimeStamp(null);
            mApplicationConfigurations
                    .setMusicPopularTimeStamp(null);
            mApplicationConfigurations
                    .setVideoLatestTimeStamp(null);
            mApplicationConfigurations.setLiveRadioTimeStamp(null);
            mApplicationConfigurations.setOnDemandTimeStamp(null);

//            mDataManager.getCacheManager()
//                    .storeMusicLatestResponse("", null);
//            mDataManager.getCacheManager()
//                    .storeMusicFeaturedResponse("", null);
//            mDataManager.getCacheManager()
//                    .storeVideoLatestResponse("", null);
//            mDataManager.getCacheManager().storeLiveRadioResponse(
//                    "", null);
//            mDataManager.getCacheManager().storeCelebRadioResponse(
//                    "", null);

            mApplicationConfigurations
                    .setSearchPopularTimeStamp(null);

            Intent new_intent = new Intent();
            new_intent
                    .setAction(HomeActivity.ACTION_PREFERENCE_CHANGE);
            new_intent.putExtra("preference_change", true);
            new_intent.putExtra("selectedLanguage",
                    selected_languge);
            sendBroadcast(new_intent);
            isUserLoggedIn = false;
        }

		if (!mApplicationConfigurations.getCategoryPrefSelectionGeneric6()
				&& !categoryName.equalsIgnoreCase("editors picks"))
			mApplicationConfigurations.setCategoryPrefSelectionGeneric6(true);
	}

	/**
	 * Configures the action bar to have a navigation list of 'Home' and 'Inbox'
	 */
	void hideCategoryActionBar() {
		// selectedPreferenceTab = -1;
		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayUseLogoEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(false);

		LanguageButtonLollipop btn_preference = (LanguageButtonLollipop) findViewById(R.id.btn_preferences);
		btn_preference.setVisibility(View.GONE);

	}

	/**
	 * Create a compatible helper that will manipulate the action bar if
	 * available.
	 */
	private ActionBarHelper createActionBarHelper() {
		return new ActionBarHelper();
	}

	private static MainActivity _activity;


	protected HomeActivity homeActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!Utils.isAllPermissionsGranted(this)) {
			Toast.makeText(this, "Please grant all permissions.", Toast.LENGTH_SHORT).show();
			sendBroadcast(new Intent(HomeActivity.ACTION_CLOSE_APP));
			finish();
			return;
		}

//		if (Logger.enableLanguageLibraryThread) {
//			Utils.startReverieSDK(getApplicationContext());
//		} else if (Logger.enableLanguageLibrary) {
//			new LM(this).RegisterSDK(HomeActivity.SDK_ID);
//		}

	}

	protected void onCreateCode(){
		mFragmentManager = getSupportFragmentManager();
		mAppirater = new Appirater(this);

		RelativeLayout rlMiniPlayerParent = (RelativeLayout) findViewById(R.id.rlMiniPlayerParent);
		if (rlMiniPlayerParent != null)
			rlMiniPlayerParent.setVisibility(View.GONE);

		mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		if (mToolbar != null)
			setSupportActionBar(mToolbar);

		try {

			mActionBar = getSupportActionBar();
			mActionBar.setIcon(R.drawable.icon_actionbar_logo);

		} catch (Error e) {
			System.gc();
			System.runFinalization();
			System.gc();
			mActionBar = getSupportActionBar();
			mActionBar.setIcon(R.drawable.icon_actionbar_logo);

		}

		mDataManager = DataManager.getInstance(getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();

		registerReceivers();
		if (mCurrentNavigationItem == NavigationItem.OTHER
				&& mLastNavigationItem != null) {

			Utils.setActionBarTitle(this, mActionBar,
					mLastNavigationItem.title);
		} else {

			Utils.setActionBarTitle(this, mActionBar, "");
		}

		//Patibandha
		//InitilizeCastManager();


	}

	private void registerReceivers() {

		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_PLAYER_DRAWER_OPEN);
		registerReceiver(preference_drawer_open, filter);

	}

	BroadcastReceiver preference_drawer_open = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				boolean isDrawerOpen = intent.getBooleanExtra("isDrawerOpen",
						false);
				if (mDrawerLayout != null) {
					if (mPlayerBarFragment != null
							&& mPlayerBarFragment.isContentOpened()
							&& isDrawerOpen) {
						lockDrawer();
					} else
						unlockDrawer();
				}
			} catch (Exception e) {
			}

		}
	};

	protected void HideDrawer() {
		if (mDrawerToggle != null)
			mDrawerToggle.setDrawerIndicatorEnabled(false);
	}

	protected void showDrawer() {
		if (mDrawerToggle != null)
			mDrawerToggle.setDrawerIndicatorEnabled(true);

	}

	public boolean isDrawerIndicatorEnable(){
		if (mDrawerToggle != null)
			return mDrawerToggle.isDrawerIndicatorEnabled();
		return false;
	}

	public void lockDrawer() {
		if (mDrawerLayout != null) {
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
			mDrawerLayout
					.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		}
	}

    public void unlockDrawer() {
		if (mDrawerLayout != null)
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
	}

	@Override
	protected void onNewIntent(Intent intent) {

	}

	// left panel
	public DrawerLayout mDrawerLayout;
	public ActionBarDrawerToggle mDrawerToggle;
	private ActionBarHelper mActionBarHelper;

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		if (mDrawerToggle != null)
			mDrawerToggle.syncState();

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		if (mDrawerToggle != null)
			mDrawerToggle.onConfigurationChanged(newConfig);
	}

    public int getActionBarHeight() {
        int height;
        height = getSupportActionBar().getHeight();
        return height;
    }

	public void showBackButtonWithTitle(String title, String subTitle) {
		try {
			if(title.equals(getResources().getString(
					R.string.main_actionbar_search)))
				title = "";
			ActionBar mActionBar = getSupportActionBar();

			Utils.setActionBarTitleSubtitle(MainActivity.this, mActionBar,
					title, subTitle);
			LanguageButtonLollipop btn_preference = (LanguageButtonLollipop) findViewById(R.id.btn_preferences);
			btn_preference.setVisibility(View.GONE);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	public void showBackButtonWithTitleMediaDetail(String title, String subTitle) {
		try {
				ActionBar mActionBar = getSupportActionBar();

				Utils.setActionBarTitleSubtitle_MediaDetail(MainActivity.this,
						mActionBar, title, subTitle);
				LanguageButtonLollipop btn_preference = (LanguageButtonLollipop) findViewById(R.id.btn_preferences);
				btn_preference.setVisibility(View.GONE);
		} catch (Exception e){
					Logger.printStackTrace(e);
				}
			}

	private String isUpdateActionbarforChromecast ="";
	public void updateActionBarforChromecast(String isUpdateActionbarforChromecast){

		this.isUpdateActionbarforChromecast = isUpdateActionbarforChromecast;
		if(mMenu != null)
			mMenu.clear();

		int backCount = getSupportFragmentManager().getBackStackEntryCount();
		Logger.i(TAG,"Fragment Back Count:"+backCount+" ::: "+isUpdateActionbarforChromecast);
		if(isUpdateActionbarforChromecast.equals("detailpage")){
			onCreateOptionsMenu(mMenu);
		}else if(isUpdateActionbarforChromecast.equals("resultpage")){
			//onCreateOptionsMenu(mMenu);
		}else{
			onCreateOptionsMenu(mMenu);
		}

	}

	public void showBackButtonWithTitleWithouLogo(String title, String subTitle) {
		try {
			ActionBar mActionBar = getSupportActionBar();

			Utils.setActionBarWithoutLogo(MainActivity.this, mActionBar, title,
					subTitle);
			LanguageButtonLollipop btn_preference = (LanguageButtonLollipop) findViewById(R.id.btn_preferences);
			btn_preference.setVisibility(View.GONE);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	protected void showNormalActionBar() {
		try {

			if (!mApplicationConfigurations.getSaveOfflineMode()) {
				Utils.setActionBarTitle(MainActivity.this, mActionBar, "");
				showCategoryActionBar();
			} else {
				showBackButtonWithTitle(
						getResources().getString(R.string.application_name), "");
			}
			if (mMenu != null)
				mMenu.clear();
			// MenuInflater inflater = getMenuInflater();
			// inflater.inflate(R.menu.menu_main_actionbar, mMenu);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mMenu != null)
			onCreateOptionsMenu(mMenu);
	}



    public void CallCommemtfragment(MediaItem mediaItem){
        Bundle outcomingArgs = new Bundle();
        outcomingArgs.putSerializable(MainActivity.EXTRA_DATA_MEDIA_ITEM,
				(Serializable) mediaItem);
        outcomingArgs.putString(MainActivity.FLURRY_SOURCE_SECTION, FlurryConstants.FlurryComments.FullPlayer.toString());
        //findViewById(R.id.progressbar).setVisibility(View.GONE);

        CommentsFragment commentFragment = new CommentsFragment();
        commentFragment.setArguments(outcomingArgs);
        FragmentManager mFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager
                .beginTransaction();

        fragmentTransaction.replace(R.id.home_browse_by_fragmant_container,
				commentFragment, "CommentsFragment");
        fragmentTransaction.addToBackStack("CommentsFragment");
		if(Constants.IS_COMMITALLOWSTATE)
			fragmentTransaction.commitAllowingStateLoss();
		else
			fragmentTransaction.commit();
		findViewById(R.id.home_browse_by_fragmant_container).setVisibility(View.VISIBLE);
    }

	/**
	 * A drawer listener can be used to respond to drawer events such as
	 * becoming fully opened or closed. You should always prefer to perform
	 * expensive operations such as drastic relayout when no animation is
	 * currently in progress, either before or after the drawer animates.
	 * 
	 * When using ActionBarDrawerToggle, all DrawerLayout listener methods
	 * should be forwarded if the ActionBarDrawerToggle is not used as the
	 * DrawerLayout listener directly.
	 */
	private class DemoDrawerListener implements DrawerLayout.DrawerListener {
		@Override
		public void onDrawerOpened(View drawerView) {
			mDrawerToggle.onDrawerOpened(drawerView);
			mActionBarHelper.onDrawerOpened();

		}

		@Override
		public void onDrawerClosed(View drawerView) {
			mDrawerToggle.onDrawerClosed(drawerView);
			mActionBarHelper.onDrawerClosed();
			if (isClosedNeed) {
				isClosedNeed = false;
				return;
			}

		}

		@Override
		public void onDrawerSlide(View drawerView, float slideOffset) {
			mDrawerToggle.onDrawerSlide(drawerView, slideOffset);

		}

		@Override
		public void onDrawerStateChanged(int newState) {
			mDrawerToggle.onDrawerStateChanged(newState);
		}
	}

	private class ActionBarHelper {
		private final ActionBar mActionBar;

		private ActionBarHelper() {
			mActionBar = getSupportActionBar();
		}

		public void init() {
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setHomeButtonEnabled(true);
		}

		/**
		 * When the drawer is closedoonresiew.
		 */
		public void onDrawerClosed() {

		}

		/**
		 * When the drawer is open we set the action bar to a generic title. The
		 * action bar should only contain data relevant at the top level of the
		 * nav hierarchy represented by the drawer, as the rest of your content
		 * will be dimmed down and non-interactive.
		 */
		public void onDrawerOpened() {
		}

	}

	boolean isWebViewCalled;

	@Override
	protected void onStart() {
		if(isFinishing())
			return;
		super.onStart();

		Utils.clearCache();

		// Activity instrumentation for analytic tracking
		com.urbanairship.analytics.Analytics.activityStarted(this);

		Analytics.startSession(this);
		if (isSkipResume) {
			return;
		}

		if (isWebViewCalled) {
			return;
		}

		/*
		 * Updates the current navigation item here to make it most up - to -
		 * date when switching between sections.
		 */
		mLastNavigationItem = mCurrentNavigationItem;
		// sets the current quick navigation item to be selected.
		mCurrentNavigationItem = getNavigationItem();
		if (mCurrentNavigationItem == null) {
			mCurrentNavigationItem = NavigationItem.MUSIC;
		}

		try {
			if (//this instanceof DownloadActivity
					//|| this instanceof MyStreamActivity
					//|| this instanceof PlaylistsActivity
					//|| this instanceof MyCollectionActivity
					//|| this instanceof FavoritesActivity
					// || this instanceof DiscoveryResultActivity
					/*|| */this instanceof MediaDetailsActivity

                    || this instanceof MainSearchFragment
					|| this instanceof CommentsActivity
					|| this instanceof DownloadConnectingActivity
					/*|| this instanceof ProfileActivity*/
					|| this instanceof InviteFriendsActivity
					|| this instanceof ActivityMainSearchResult) {

			} else if (!isFromPause
					|| (mApplicationConfigurations != null && mApplicationConfigurations
							.getSaveOfflineMode())) {
				isFromPause = false;
				showNormalActionBar();
			} else
				isFromPause = false;
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

		/*
		 * Updates the flags of the navigation items,
		 */
		if (mCurrentNavigationItem == NavigationItem.MUSIC
				|| mCurrentNavigationItem == NavigationItem.VIDEOS
				|| mApplicationConfigurations.getSaveOfflineMode()) {
			mLastNavigationItem = null;
		}
	}

	@Override
	protected void onUserLeaveHint() {
		ScreenLockStatus.getInstance(getBaseContext()).onStop(true, this);
		Logger.s("onUserLeaveHint MainActivity");
		super.onUserLeaveHint();
	}

	private static Activity currentRunningActivity = null;

	@Override
	protected void onResume() {
		super.onResume();

		if(isFinishing())
			return;
		//onResumeCast();
		if (Utils.isDeviceAirplaneModeActive(this)
				&& HomeActivity.needToShowAirplaneDialog) {
			HomeActivity.needToShowAirplaneDialog = false;
			Intent i = new Intent(this, OfflineAlertActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
		}

		currentRunningActivity = this;
		ScreenLockStatus.getInstance(getBaseContext()).onResume(this, this);
		Logger.s("onResume MainActivity");
		// System.gc();
		mIsResumed = true;

		if (switchToOfflineMode) {
			internetConnectivityPopup(null);
		}

		if (GoOfflineActivity.skipResume) {
			GoOfflineActivity.skipResume = false;
			return;
		}

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				RelativeLayout rlMiniPlayerParent = (RelativeLayout) findViewById(R.id.rlMiniPlayerParent);
				if (rlMiniPlayerParent != null) {
					rlMiniPlayerParent.setVisibility(View.VISIBLE);
					getPlayerBar();
				}
			}
		}, 200);

		if (isSkipResume) {
			return;
		}

		if (isWebViewCalled) {
			isWebViewCalled = false;
			return;
		}

		ApplicationConfigurations appConfig = ApplicationConfigurations
				.getInstance(this);
		String sessionId = appConfig.getSessionID();
		String passkey = appConfig.getPasskey();
		if (sessionId == null
				|| (sessionId != null && (sessionId.length() == 0
						|| sessionId.equalsIgnoreCase("null") || sessionId
							.equalsIgnoreCase("none")))) {
			try {
				if (passkey == null
						|| (passkey != null && (passkey.length() == 0
								|| passkey.equalsIgnoreCase("null") || passkey
									.equalsIgnoreCase("none")))) {
					sendBroadcast(new Intent(
							MainActivity.ACTION_LANGUAGE_CHANGED));
					Toast.makeText(this, "Application data cleared.",
							Toast.LENGTH_SHORT).show();
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// HungamaApplication.activityResumed();
	}

	@Override
	protected void onPause() {
		mIsResumed = false;
		HungamaApplication.activityPaused();
		//onPauseCast();
		super.onPause();
		// disables any animations when pressing "Back" button.
		// overridePendingTransition(0, 0);
		isFromPause = true;
	}

	boolean isFromPause;

	@Override
	protected void onStop() {
		super.onStop();
		// HungamaApplication.activityStoped();
		ScreenLockStatus.getInstance(getBaseContext()).onStop();
		Logger.s("onStop MainActivity");
		Analytics.onEndSession(this);
		// Activity instrumentation for analytic tracking
		com.urbanairship.analytics.Analytics.activityStopped(this);
		Utils.clearCache();
	}

	@Override
	protected void onDestroy() {
		mIsDestroyed = true;
		try {
			unregisterReceiver(preference_drawer_open);
		} catch (Exception e) {
		}

		try {
			super.onDestroy();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		//mCastManager.removeVideoCastConsumer(mCastConsumer1);
		Utils.clearCache();
	}

	boolean isCalledForUpgrade = false, isUserLoggedIn = false;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (rental != null
				&& (requestCode == VerifyMobileNumberDialog.VERIFY_MOBILE_LOGIN_ACTIVITY || requestCode == OtpConfirmationDialog.OTP_MOBILE_LOGIN_ACTIVITY)) {
			rental.onActivityResult(requestCode, resultCode, data);
			return;
		}
		if (requestCode == HomeActivity.MY_PREFERENCES_ACTIVITY_RESULT_CODE
				&& resultCode == RESULT_OK && data != null) {
			if (data.getExtras().getBoolean(
					HomeActivity.EXTRA_MY_PREFERENCES_IS_CHANGED)) {
				finish();
				Intent reStartHomeActivity = new Intent(
						getApplicationContext(), HomeActivity.class);
				reStartHomeActivity.putExtra(
						HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE,
						(Serializable) MediaContentType.MUSIC);
				reStartHomeActivity.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
						| Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(reStartHomeActivity);
			}
		} else if (requestCode == LOGIN_ACTIVITY_CODE
				&& resultCode == RESULT_OK) {
			String session = mDataManager.getApplicationConfigurations()
					.getSessionID();
			Boolean isRealUser = mDataManager.getApplicationConfigurations()
					.isRealUser();
			if (!TextUtils.isEmpty(session) && isRealUser) {
				isCalledForUpgrade = false;
                isUserLoggedIn = true;
				String accountType = Utils
						.getAccountName(getApplicationContext());
                mDataManager.getMyPreferences(preferenceOperation);
//				mDataManager.getCurrentSubscriptionPlan(offlineUpgradeListener,
//						accountType);
				if (mainSettingsFragment != null)
					mainSettingsFragment.collepseGroups();
				// if (this instanceof HomeActivity) {
				Intent new_intent = new Intent();
				new_intent.setAction(HomeActivity.ACTION_NOTIFY_ADAPTER);
				sendBroadcast(new_intent);
				// }
			}
		}/* else if (requestCode == UpgradeActivity.LOGIN_ACTIVITY_CODE
				&& resultCode == RESULT_OK) {
			String session = mDataManager.getApplicationConfigurations()
					.getSessionID();
			Boolean isRealUser = mDataManager.getApplicationConfigurations()
					.isRealUser();
			if (!TextUtils.isEmpty(session) && isRealUser) {
				if (mApplicationConfigurations
						.isTrialCheckedForUserId(mApplicationConfigurations
								.getPartnerUserId())) {
					openUpgradeActivity();
				} else {
					isCalledForUpgrade = true;
					String accountType = Utils
							.getAccountName(getApplicationContext());
					mDataManager.getCurrentSubscriptionPlan(
							offlineUpgradeListener, accountType);
				}
			} else {
				Toast toast = Utils.makeText(this,
						getString(R.string.before_upgrade_login),
						UpgradeFragment.TOAST_SHOW_DELAY);
				toast.setGravity(Gravity.CENTER_VERTICAL
						| Gravity.CENTER_HORIZONTAL, 0, 0);
				toast.show();
			}
		}*/ else if (requestCode == HomeActivity.UPGRADE_ACTIVITY_RESULT_CODE
				&& resultCode == RESULT_OK) {
			String session = mDataManager.getApplicationConfigurations()
					.getSessionID();
			Boolean isRealUser = mDataManager.getApplicationConfigurations()
					.isRealUser();
			if (!TextUtils.isEmpty(session) && (isRealUser || Logger.allowPlanForSilentUser)) {
				isCalledForUpgrade = false;
				String accountType = Utils
						.getAccountName(getApplicationContext());
				mDataManager.getCurrentSubscriptionPlan(offlineUpgradeListener,
						accountType);
				if (mainSettingsFragment != null)
					mainSettingsFragment.collepseGroups();
				// if (this instanceof HomeActivity) {
				Intent new_intent = new Intent();
				new_intent.setAction(HomeActivity.ACTION_NOTIFY_ADAPTER);
				sendBroadcast(new_intent);
			}
		} else if (requestCode == PROFILE_ACTIVITY_CODE
				&& resultCode == RESULT_OK) {
            openProfileActivity(false);
//			Intent profileActivityIntent = new Intent(getApplicationContext(),
//					ProfileActivity.class);
//			startActivity(profileActivityIntent);
		}
		CacheManager.saveOfflineResultValidation(this, requestCode, resultCode,
				data);
	}

	// Updating number on notifications badge.
	// ======================================================
	// ACTION BAR'S LISTENERS.
	// ======================================================

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		mMenu = menu;

		int backCount = getSupportFragmentManager().getBackStackEntryCount();
		Logger.i(TAG,"Fragment Back Count:"+backCount+" ::: "+isUpdateActionbarforChromecast);

		if(isUpdateActionbarforChromecast.equals("detailpage") && (backCount!=2 && backCount!=0)){
			inflater.inflate(R.menu.menu_search_actionbar1, menu);
			isUpdateActionbarforChromecast = "";
		}else if(isUpdateActionbarforChromecast.equals("resultpage")){
			inflater.inflate(R.menu.menu_search_actionbar, menu);
			isUpdateActionbarforChromecast = "";
		}else{
			inflater.inflate(R.menu.menu_main_actionbar, menu);
			isUpdateActionbarforChromecast = "";
		}

		/*if(isUpdateActionbarforChromecast){
			inflater.inflate(R.menu.menu_search_actionbar1, menu);
			isUpdateActionbarforChromecast = "";
		} else{
			inflater.inflate(R.menu.menu_main_actionbar, menu);
		}*/

		// Added by Patibandha
		//InitializeMenuItem(menu);

		return true;
	}

	void openSearch(boolean needDelay,final boolean needToOpenActivity) {
        if(needDelay){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        openMainSearch(FRAGMENT_TAG_MAIN_SEARCH,
                                FlurryConstants.FlurrySearch.ActionBarSearch.toString(),needToOpenActivity);
                        getIntent().removeExtra("song_catcher");
                        getIntent().removeExtra(SELECTED_SEARCH_OPTION);
                        getIntent().removeExtra("search");
                    } catch (Exception e) {
                        Logger.printStackTrace(e);
                    }
                }
            },1000);
        }else{
                    try {
                        openMainSearch(FRAGMENT_TAG_MAIN_SEARCH,
                                FlurryConstants.FlurrySearch.ActionBarSearch.toString(),needToOpenActivity);
                        getIntent().removeExtra("song_catcher");
                        getIntent().removeExtra(SELECTED_SEARCH_OPTION);
                        getIntent().removeExtra("search");
                    } catch (Exception e) {
                        e.printStackTrace();
                        Logger.printStackTrace(e);
                    }
        }

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == android.R.id.home) {

//			try {
//				MediaDetailsActivity obj = (MediaDetailsActivity) this;
//				if (obj != null) {
//					obj.finish();
//					return true;
//				}
//			} catch (Exception e) {
//			}

			try {
				CommentsActivity obj = (CommentsActivity) this;
				if (obj != null) {
					obj.finish();
					return true;
				}
			} catch (Exception e) {
			}

			Intent startEntryActivity = null;
			if (mApplicationConfigurations.getSaveOfflineMode()) {
				startEntryActivity = new Intent(getApplicationContext(),
						GoOfflineActivity.class);
				startEntryActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(startEntryActivity);
			} else {
				if (mLastNavigationItem != null
						&& (mLastNavigationItem == NavigationItem.MUSIC || mLastNavigationItem == NavigationItem.VIDEOS)) {

					if (mLastNavigationItem == NavigationItem.MUSIC) {
						// resets the flag.
						mLastNavigationItem = null;
						// sets properties to launch the last entry activity.
						startEntryActivity = new Intent(
								getApplicationContext(), HomeActivity.class);
						startEntryActivity.putExtra(
								HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE,
								(Serializable) MediaContentType.MUSIC);

					} else if (mLastNavigationItem == NavigationItem.VIDEOS) {
						// resets the flag.
						mLastNavigationItem = null;
						// sets properties to launch the last entry activity.
						startEntryActivity = new Intent(
								getApplicationContext(), HomeActivity.class);
						startEntryActivity.putExtra(
								HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE,
								(Serializable) MediaContentType.VIDEO);
					}

				} else {
					// sets properties to launch the last entry activity.
					startEntryActivity = new Intent(getApplicationContext(),
							HomeActivity.class);
					startEntryActivity.putExtra(
							HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE,
							(Serializable) MediaContentType.MUSIC);
				}

				startEntryActivity
						.putExtra(
								HomeActivity.ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION,
								HomeTabBar.TAB_ID_LATEST);
				startEntryActivity.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
						| Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(startEntryActivity);
			}

			return true;
		}

		// close the last selected item if it's not the new one.
		if (mLastSelectedMenuItem != null
				&& mLastSelectedMenuItem.getItemId() != item.getItemId()) {
			closeFragmentOfMenuItem(mLastSelectedMenuItem);
			mLastSelectedMenuItem.setChecked(false);
			// colors back the item's background to transparent.
			View view = findViewById(mLastSelectedMenuItem.getItemId());
			if (view != null) {
				view.setBackgroundResource(R.drawable.transparent_background);
			}
		}
		mLastSelectedMenuItem = item;

		// toggle any other selected
		if (itemId == R.id.menu_item_main_actionbar_go_offline) {

			return handleOfflineSwitchCase(false);

		}
		if (itemId == R.id.menu_item_main_actionbar_search) {
			if (mApplicationConfigurations.getSaveOfflineMode()) {
				CustomAlertDialog alertBuilder = new CustomAlertDialog(this);
				alertBuilder
						.setMessage(Utils
								.getMultilanguageText(
										getApplicationContext(),
										getResources()
												.getString(
														R.string.caching_text_message_go_online_global_menu)));
				alertBuilder.setPositiveButton(
						getResources().getString(
								R.string.caching_text_popup_title_go_online),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								if (Utils.isConnected()) {
									mApplicationConfigurations
											.setSaveOfflineMode(false);
									mApplicationConfigurations
											.setSaveOfflineAutoMode(false);
									Intent i = new Intent(
											MainActivity.ACTION_OFFLINE_MODE_CHANGED);
									i.putExtra(SELECTED_SEARCH_OPTION, true);
									sendBroadcast(i);

									Map<String, String> reportMap = new HashMap<String, String>();
									reportMap
											.put(FlurryConstants.FlurryCaching.Source
													.toString(),
													FlurryConstants.FlurryCaching.Prompt
															.toString());
									reportMap
											.put(FlurryConstants.FlurryCaching.UserStatus
													.toString(),
													Utils.getUserState(MainActivity.this));
									Analytics
											.logEvent(
													FlurryConstants.FlurryCaching.GoOnline
															.toString(),
													reportMap);
								} else {
									CustomAlertDialog alertBuilder = new CustomAlertDialog(
											MainActivity.this);
									alertBuilder.setMessage(Utils
											.getMultilanguageText(
													getApplicationContext(),
													getResources()
															.getString(
																	R.string.go_online_network_error)));
									alertBuilder.setNegativeButton(
											Utils.getMultilanguageText(
													getApplicationContext(),
													"OK"),
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
							}
						});
				alertBuilder.setNegativeButton(
						getResources().getString(
								R.string.caching_text_popup_button_cancel),
						null);
				// alertBuilder.create();
				alertBuilder.show();
			} else {
				// toggles the search visibility.
				if (item.isChecked()) {
//					if (SongCatcherFragment.isSongCatcherOpen) {
//						closeMainSearch();
//						mLastSelectedMenuItem.setChecked(false);
//						View view = findViewById(mLastSelectedMenuItem
//								.getItemId());
//						if (view != null) {
//							view.setBackgroundResource(R.drawable.transparent_background);
//						}
//						Fragment searchFragment = mFragmentManager
//								.findFragmentByTag(FRAGMENT_TAG_MAIN_SEARCH);
//						if (searchFragment != null) {
//							FragmentTransaction fragmentTransaction = mFragmentManager
//									.beginTransaction();
//							fragmentTransaction.remove(searchFragment);
//							fragmentTransaction.commit();
//						}
//
//					} else {
						closeMainSearch();
						item.setChecked(false);
						// colors back the search background to transparent.
						View view = findViewById(itemId);
						view.setBackgroundResource(R.drawable.transparent_background);
//					}
				}/* else if (SongCatcherFragment.isSongCatcherOpen) {
					onBackPressed();
				} */else {
					// item.setChecked(true);
					// mLastSelectedMenuItem=null;
					// mCurrentNavigationItem=null;
					openSearch(false,needToOpenSearchActivity);
				}
			}
			return true;
		}

		return false;
	}

	// ======================================================
	// ACTIVITY'S EVENT LISTENERS.
	// ======================================================

	public boolean handleOfflineSwitchCase(final boolean isFromNoInternetPrompt) {
		try {
			if (mApplicationConfigurations.getSaveOfflineMode()) {
				try {

					if (Utils.isConnected()) {
						isClosedNeed = true;
						mApplicationConfigurations.setSaveOfflineMode(false);
						mApplicationConfigurations
								.setSaveOfflineAutoMode(false);
						sendBroadcast(new Intent(
								MainActivity.ACTION_OFFLINE_MODE_CHANGED));

						Map<String, String> reportMap = new HashMap<String, String>();
						reportMap
								.put(FlurryConstants.FlurryCaching.Source
										.toString(),
										FlurryConstants.FlurryCaching.LeftMenuToggleButton
												.toString());
						reportMap.put(FlurryConstants.FlurryCaching.UserStatus
								.toString(), Utils
								.getUserState(MainActivity.this));
						Analytics.logEvent(
								FlurryConstants.FlurryCaching.GoOnline
										.toString(), reportMap);
					} else {
						CustomAlertDialog alertBuilder = new CustomAlertDialog(
								MainActivity.this);
						alertBuilder.setMessage(getResources().getString(
								R.string.go_online_network_error));
						alertBuilder.setNegativeButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										try {
											startActivity(new Intent(
													android.provider.Settings.ACTION_SETTINGS));
										}
										catch (Exception e)
										{
											e.printStackTrace();
										}
									}
								});
						// alertBuilder.create();
						alertBuilder.show();
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
				// }
				// });
				// alertBuilder.setNegativeButton("No", null);
				// // alertBuilder.create();
				// alertBuilder.show();
			} else {
				if (CacheManager.isProUser(this)
						|| CacheManager.isTrialUser(this)) {
					closePlayerBarContent();

					goToOfflineMode(isFromNoInternetPrompt);

				} else {

					if (Utils.isConnected()) {

						String sesion = mApplicationConfigurations
								.getSessionID();
						boolean isRealUser = mApplicationConfigurations
								.isRealUser();
						if (!TextUtils.isEmpty(sesion) && isRealUser) {
							if (mApplicationConfigurations
									.isTrialCheckedForUserId(mApplicationConfigurations
											.getPartnerUserId())) {
								openUpgradeActivity();
							} else {
								String accountType = Utils
										.getAccountName(getApplicationContext());
								mDataManager.getCurrentSubscriptionPlan(
										offlineUpgradeListener, accountType);
							}
						} else {
							Intent startLoginActivityIntent = new Intent(
									getApplicationContext(),
									LoginActivity.class);
							startLoginActivityIntent.putExtra(
									UpgradeActivity.ARGUMENT_UPGRADE_ACTIVITY,
									"upgrade_activity");
							startLoginActivityIntent.putExtra(
									LoginActivity.FLURRY_SOURCE,
									FlurryConstants.FlurryUserStatus.Upgrade
											.toString());
							startActivityForResult(startLoginActivityIntent,
									UpgradeActivity.LOGIN_ACTIVITY_CODE);
						}
					} else {
						goToOfflineMode(isFromNoInternetPrompt);
					}
				}
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return true;
	}

	private void goToOfflineMode(final boolean isFromNoInternetPrompt) {
		mApplicationConfigurations.setSaveOfflineMode(true);
		sendBroadcast(new Intent(MainActivity.ACTION_OFFLINE_MODE_CHANGED));

		Map<String, String> reportMap = new HashMap<String, String>();
		if (isFromNoInternetPrompt)
			reportMap.put(FlurryConstants.FlurryCaching.Source.toString(),
					FlurryConstants.FlurryCaching.NoInternetPrompt.toString());
		else
			reportMap.put(FlurryConstants.FlurryCaching.Source.toString(),
					FlurryConstants.FlurryCaching.LeftMenuToggleButton
							.toString());
		reportMap.put(FlurryConstants.FlurryCaching.UserStatus.toString(),
				Utils.getUserState(MainActivity.this));
		Analytics.logEvent(FlurryConstants.FlurryCaching.GoOffline.toString(),
				reportMap);
	}

	private void openUpgradeActivity() {

		mApplicationConfigurations.setSaveOfflineMode(true);
		Intent intent = new Intent(MainActivity.ACTION_OFFLINE_MODE_CHANGED);
		intent.putExtra("open_upgrade_popup", true);
		sendBroadcast(intent);

		Map<String, String> reportMap = new HashMap<String, String>();
		reportMap.put(FlurryConstants.FlurryCaching.Source.toString(),
				FlurryConstants.FlurryCaching.LeftMenuToggleButton.toString());
		reportMap.put(FlurryConstants.FlurryCaching.UserStatus.toString(),
				Utils.getUserState(MainActivity.this));
		Analytics.logEvent(FlurryConstants.FlurryCaching.GoOffline.toString(),
				reportMap);
	}

	CommunicationOperationListener offlineUpgradeListener = new CommunicationOperationListener() {
		@Override
		public void onSuccess(int operationId,
				Map<String, Object> responseObjects) {
			switch (operationId) {
			case OperationDefinition.Hungama.OperationId.SUBSCRIPTION_CHECK: {
				SubscriptionStatusResponse subscriptionStatusResponse = (SubscriptionStatusResponse) responseObjects
						.get(SubscriptionCheckOperation.RESPONSE_KEY_SUBSCRIPTION_CHECK);
				if (subscriptionStatusResponse != null) {
					if (subscriptionStatusResponse.getSubscription()!=null
							&& subscriptionStatusResponse.getSubscription().getSubscriptionStatus()==1) {
						mApplicationConfigurations
								.setIsUserHasSubscriptionPlan(true);
						Utils.makeText(
								MainActivity.this,
								getResources().getString(
										R.string.already_subscribed),
								Toast.LENGTH_SHORT).show();
						mApplicationConfigurations
								.setTrialCheckedForUserId(mApplicationConfigurations
										.getPartnerUserId());
						if (isCalledForUpgrade) {
							isCalledForUpgrade = false;
							goToOfflineMode(false);
						} else {
							Intent new_intent = new Intent();
							new_intent.setAction(HomeActivity.ACTION_NOTIFY_ADAPTER);
							sendBroadcast(new_intent);
						}
						hideLoadingDialog();
						return;
					} else {

						mApplicationConfigurations
								.setTrialCheckedForUserId(mApplicationConfigurations
										.getPartnerUserId());
//							mDataManager.getSubscriptionPlans(0,
//									SubscriptionType.PLAN, this, true);
						/*if (isCalledForUpgrade) {
							isCalledForUpgrade = false;
							openUpgradeActivity();
						}*/
					   handleOfflineSwitchCase(false);
						hideLoadingDialog();
						return;
					}
				}
				if (isCalledForUpgrade) {
					isCalledForUpgrade = false;
					openUpgradeActivity();
				}
				hideLoadingDialog();
				break;
			}
			}
		}

		@Override
		public void onStart(int operationId) {
			showLoadingDialog(R.string.please_wait);
		}

		@Override
		public void onFailure(int operationId, ErrorType errorType,
				String errorMessage) {
			hideLoadingDialog();
		}
	};

	public boolean closeDrawerIfOpen() {
		if (mDrawerLayout != null
				&& mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
			mDrawerLayout.closeDrawers();
			return true;
		}
		return false;
	}

    public void removeDrawerIconAndPreference(){
        if (mDrawerToggle != null && mDrawerToggle.isDrawerIndicatorEnabled())
        {
            HideDrawer();
            isSkipResume = true;
            setNeedToOpenSearchActivity(true);
            LanguageButtonLollipop btn_preference = (LanguageButtonLollipop) findViewById(R.id.btn_preferences);
            btn_preference.setVisibility(View.GONE);

            // mActionBar.setDisplayShowCustomEnabled(true);
            mActionBar.setHomeButtonEnabled(false);
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setDefaultDisplayHomeAsUpEnabled(false);
			lockDrawer();
        }

    }

    public void showHideActionbar(boolean value){
      if(value){
          mActionBar.show();
      }else{
          mActionBar.hide();
      }

    }

    public void ShowDrawerIconAndPreference(){
        setNeedToOpenSearchActivity(false);
        isSkipResume = false;
        if(mMenu!=null) {
            mMenu.clear();
            onCreateOptionsMenu(mMenu);
        }
        showDrawer();
//      mActionBar.setHomeButtonEnabled(true);
//      mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayUseLogoEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
//      mActionBar.setDisplayShowHomeEnabled(false);

        mActionBar.setIcon(R.drawable.icon_actionbar_logo);

        Utils.setActionBarTitle(MainActivity.this, mActionBar, "");
        unlockDrawer();

        LanguageButtonLollipop btn_preference = (LanguageButtonLollipop) findViewById(R.id.btn_preferences);
		btn_preference.setVisibility(View.VISIBLE);

        mActionBar.setLogo(R.drawable.icon_actionbar_logo);
        mActionBar.setDisplayUseLogoEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);

		mToolbar.setNavigationOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mDrawerLayout.openDrawer(Gravity.LEFT);
			}
		});
		unlockDrawer();
		Utils.setToolbarColor(this);
    }

	@Override
	public void onBackPressed() {
		try {
			if (closeDrawerIfOpen()) {
				return;
			}

			Logger.d(TAG,
					"Back button was pressesd, closing any opened, Action bar menu item.");
			// changes only the last menu item's checking state.
			Logger.e(TAG, "onBackPressed");
			if (mLastSelectedMenuItem != null) {

				if (mLastSelectedMenuItem.getItemId() == R.id.menu_item_main_actionbar_search) {
					// toggles the search visibility.
					if (mLastSelectedMenuItem.isChecked()) {
						Logger.e(TAG, "onBackPressed Search");

						closeMainSearch();
						mLastSelectedMenuItem.setChecked(false);
						// colors back the search background to transparent.
						View view = findViewById(mLastSelectedMenuItem
								.getItemId());
						view.setBackgroundResource(R.drawable.transparent_background);
						mLastSelectedMenuItem = null;
						return;
					}
				}

				mLastSelectedMenuItem.setChecked(false);
				View view = findViewById(mLastSelectedMenuItem.getItemId());
				if (view != null) {
					view.setBackgroundResource(R.drawable.transparent_background);
				}

				mLastSelectedMenuItem = null;
			}

			Logger.s(" 1:::::::::::::::::::::;;;- "
					+ mFragmentManager.getBackStackEntryCount());

			if (!(currentRunningActivity instanceof HomeActivity)
					&& mFragmentManager.getBackStackEntryCount() == 1)
				MainActivity.super.onBackPressed();

			Logger.s(" 2:::::::::::::::::::::;;;- "
					+ mFragmentManager.getBackStackEntryCount());
			MainActivity.super.onBackPressed();
			Logger.s(" 3:::::::::::::::::::::;;;- "
					+ mFragmentManager.getBackStackEntryCount());
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	// public void clearSearchIfOPEN() {
	// try {
	//
	// if (SongCatcherFragment.isSongCatcherOpen) {
	// // remove any opened search
	// MenuItem item = mMenu
	// .findItem(R.id.menu_item_main_actionbar_search);
	// item.setChecked(false);
	// SongCatcherFragment.isSongCatcherOpen = false;
	// // isSearchOpened = false;
	// }
	// } catch (Exception e) {
	// }
	// }

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		try {
			if (intent.getComponent().getClassName()
					.equals(CommentsActivity.class.getCanonicalName())
					|| intent.getComponent().getClassName()
							.equals(LoginActivity.class.getCanonicalName())) {
				isSkipResume = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try{

			super.startActivityForResult(intent, requestCode);

		}
		catch (ActivityNotFoundException ex)
		{
			ex.printStackTrace();
		}
	}

	public boolean isSkipResume = false;

	@Override
	public void startActivity(Intent intent) {
		try {
			String classname = intent.getComponent().getClassName();
			if (classname.equals(AboutActivity.class.getCanonicalName())
					|| classname.equals(HelpAndFAQActivity.class
							.getCanonicalName())
					|| classname.equals(AppTourActivity.class
							.getCanonicalName())
					|| classname
							.equals(RedeemActivity.class.getCanonicalName())
					|| classname.equals(FeedbackActivity.class
							.getCanonicalName())
					|| classname.equals(MyStreamActivity.class
							.getCanonicalName())
					|| classname.equals(ProfileActivity.class
							.getCanonicalName())
					|| classname.equals(MyCollectionActivity.class
							.getCanonicalName())
					|| classname.equals(FavoritesActivity.class
							.getCanonicalName())
					|| classname.equals(PlaylistsActivity.class
							.getCanonicalName())
					|| classname.equals(SettingsActivity.class
							.getCanonicalName())
					|| classname.equals(MainSearchFragment.class
							.getCanonicalName())

					|| classname.equals(MediaDetailsActivity.class
							.getCanonicalName())
                    || classname.equals(CommentsActivity.class
                    .getCanonicalName())
					|| classname.equals(VideoActivity.class.getCanonicalName())
//					 || classname.equals(ActivityMainSearchResult.class
//					 .getCanonicalName())
					|| intent.getComponent().getClassName()
							.equals(DownloadActivity.class.getCanonicalName())
					|| classname.equals(DownloadConnectingActivity.class
							.getCanonicalName())
					|| classname.equals(ActivityMainSearchResult.class
							.getCanonicalName())) {
				isSkipResume = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try
		{
			super.startActivity(intent);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

    public void openProfileActivity(boolean needDelay){

        if(needDelay){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setNeedToOpenSearchActivity(false);

                    ProfileActivity mediaDetailsFragment = new ProfileActivity();

                    FragmentManager mFragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = mFragmentManager
                            .beginTransaction();
//		fragmentTransaction.setCustomAnimations(
//				R.anim.slide_left_enter,
//				R.anim.slide_left_exit);
                    fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
							mediaDetailsFragment, "ProfileActivity");
                    fragmentTransaction.addToBackStack("ProfileActivity");

					if(Constants.IS_COMMITALLOWSTATE)
						fragmentTransaction.commitAllowingStateLoss();
					else
						fragmentTransaction.commit();

					try {
						findViewById(R.id.home_browse_by_fragmant_container).setVisibility(View.VISIBLE);
					} catch (Exception e) {
					}

                }
            },800);
        }else{
            setNeedToOpenSearchActivity(false);

            ProfileActivity mediaDetailsFragment = new ProfileActivity();

            FragmentManager mFragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = mFragmentManager
                    .beginTransaction();
//		fragmentTransaction.setCustomAnimations(
//				R.anim.slide_left_enter,
//				R.anim.slide_left_exit);
            fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
					mediaDetailsFragment, "ProfileActivity");
            fragmentTransaction.addToBackStack("ProfileActivity");

			if(Constants.IS_COMMITALLOWSTATE)
				fragmentTransaction.commitAllowingStateLoss();
			else
				fragmentTransaction.commit();

			try {
				findViewById(R.id.home_browse_by_fragmant_container).setVisibility(View.VISIBLE);
			} catch (Exception e) {
			}
        }
    }


	String menuItemId = "";
	String link_type = "";
	String html_url = "";
	String popup_msg = "";
	String title_menu = "";
	LeftMenuExtraData extraData = null;

	private void openNavigationItems(final Object menuItemId_obj,
			final String action) {
		if (menuItemId_obj != null) {
			if (menuItemId_obj instanceof Category) {
				Category cat = (Category) menuItemId_obj;
				menuItemId = cat.getInapAction();
				link_type = cat.getLinkType();
				html_url = cat.getHtmlURL();
				popup_msg = cat.getPopUpMsg();
				title_menu = cat.getLabelResourceId();
				extraData = cat.getExtraData();
			} else if (menuItemId_obj instanceof com.hungama.myplay.activity.ui.fragments.GlobalMenuFragment.MenuItem) {
				com.hungama.myplay.activity.ui.fragments.GlobalMenuFragment.MenuItem menuitem = (com.hungama.myplay.activity.ui.fragments.GlobalMenuFragment.MenuItem) menuItemId_obj;
				menuItemId = menuitem.getInAppAction();
				link_type = menuitem.getLinkType();
				html_url = menuitem.getHtmlURL();
				popup_msg = menuitem.getPopUpMsg();
				title_menu = menuitem.getLabelResource();
				extraData = menuitem.getExtraData();
			}
		} else {
			menuItemId = action;
			link_type = "inapp";
		}
		Logger.s("Operation id :::: " + menuItemId);

		if (menuItemId.equals("") && link_type.equals("inapp"))
			return;
		// this.menuItemId_obj = null;

		if (!mApplicationConfigurations.getSaveOfflineMode()) {
			try {
				mActionBar.setHomeButtonEnabled(true);
				mActionBar.setDisplayHomeAsUpEnabled(true);
				mActionBar.setDisplayUseLogoEnabled(true);
				mActionBar.setDisplayShowTitleEnabled(false);
				LanguageButtonLollipop btn_preference = (LanguageButtonLollipop) findViewById(R.id.btn_preferences);
				btn_preference.setVisibility(View.VISIBLE);
				// Utils.setActionBarTitle(MainActivity.this, mActionBar, "");
				Utils.setActionBarTitle(MainActivity.this, mActionBar, "");
				mMenu.clear();
				onCreateOptionsMenu(mMenu);
				// populateCategories(mCategories);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		String menuItemClicked = null;

		// selects which item to select.
		if (mApplicationConfigurations.getSaveOfflineMode()
				&& menuItemId
						.equalsIgnoreCase(GlobalMenuFragment.MENU_ITEM_OFFLINE_MUSCI_ACTION)) {
			Toast.makeText(MainActivity.this,
					"You are already in offline mode.", Toast.LENGTH_SHORT)
					.show();
		} else if (mApplicationConfigurations.getSaveOfflineMode()
				&& !menuItemId
						.equalsIgnoreCase(GlobalMenuFragment.MENU_ITEM_OFFLINE_MUSCI_TOGGLE_ACTION)) {
			CustomAlertDialog alertBuilder = new CustomAlertDialog(
					MainActivity.this);
			// alertBuilder.setTitle("Go Online");
			alertBuilder
					.setMessage(Utils
							.getMultilanguageText(
									getApplicationContext(),
									getResources()
											.getString(
													R.string.caching_text_message_go_online_global_menu)));
			alertBuilder.setPositiveButton(Utils.getMultilanguageText(
					getApplicationContext(),
					getResources().getString(
							R.string.caching_text_popup_title_go_online)),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {

							if (Utils.isConnected()) {
								mApplicationConfigurations
										.setSaveOfflineMode(false);
								mApplicationConfigurations
										.setSaveOfflineAutoMode(false);
								Intent i = new Intent(
										MainActivity.ACTION_OFFLINE_MODE_CHANGED);

								i.putExtra(SELECTED_GLOBAL_MENU_ID, menuItemId);
								i.putExtra(SELECTED_GLOBAL_MENU_HTML_URL,
										html_url);
								i.putExtra(SELECTED_GLOBAL_MENU_ID_POPUP_MSG,
										popup_msg);
								i.putExtra(SELECTED_GLOBAL_MENU_LINK_TYPE,
										link_type);

								sendBroadcast(i);

								Map<String, String> reportMap = new HashMap<String, String>();
								reportMap.put(
										FlurryConstants.FlurryCaching.Source
												.toString(),
										FlurryConstants.FlurryCaching.Prompt
												.toString());
								reportMap
										.put(FlurryConstants.FlurryCaching.UserStatus
												.toString(),
												Utils.getUserState(MainActivity.this));
								Analytics.logEvent(
										FlurryConstants.FlurryCaching.GoOnline
												.toString(), reportMap);
							} else {
								CustomAlertDialog alertBuilder = new CustomAlertDialog(
										MainActivity.this);
								alertBuilder.setMessage(Utils
										.getMultilanguageText(
												getApplicationContext(),
												getResources()
														.getString(
																R.string.go_online_network_error)));
								alertBuilder.setNegativeButton(Utils
										.getMultilanguageText(
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
						}
					});
			alertBuilder.setNegativeButton(Utils.getMultilanguageText(
					getApplicationContext(),
					getResources().getString(
							R.string.caching_text_popup_button_cancel)), null);
			// alertBuilder.create();
			alertBuilder.show();
		} else {
			if (link_type.equals("inapp")
					&& menuItemId
							.equalsIgnoreCase(GlobalMenuFragment.MENU_ITEM_OFFLINE_MUSCI_ACTION)) {
				menuItemClicked = getString(R.string.main_actionbar_settings_menu_item_offline_music);
				if (this instanceof HomeActivity)
					findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
				Intent i = new Intent(this, GoOfflineActivity.class);
				i.putExtra("show_toast", false);
				startActivity(i);
			} else if (link_type.equals("inapp")
					&& menuItemId
							.equalsIgnoreCase(GlobalMenuFragment.MENU_ITEM_OFFLINE_MUSCI_TOGGLE_ACTION)) {
				handleOfflineSwitchCase(false);
			} else if (link_type.equals("inapp")
					&& menuItemId
							.equalsIgnoreCase(GlobalMenuFragment.MENU_ITEM_MY_PROFILE_ACTION)) {
				menuItemClicked = getString(R.string.main_actionbar_settings_menu_item_my_profile);

				boolean applicationRealUser = mDataManager
						.getApplicationConfigurations().isRealUser();

				if (applicationRealUser) {
                  openProfileActivity(false);

//					Intent profileActivityIntent = new Intent(
//							getApplicationContext(), ProfileActivity.class);
//					startActivity(profileActivityIntent);
				} else {
					if (menuItemId_obj != null) {
						// launches the Login page.
						Intent startLoginActivityIntent = new Intent(this,
								LoginActivity.class);
						startLoginActivityIntent.putExtra(
								ProfileActivity.ARGUMENT_PROFILE_ACTIVITY,
								"profile_activity");
						startLoginActivityIntent.putExtra(
								LoginActivity.FLURRY_SOURCE,
								FlurryConstants.FlurryUserStatus.MyProfile
										.toString());
						startActivityForResult(startLoginActivityIntent,
								PROFILE_ACTIVITY_CODE);
					} else {
						Intent startLoginActivityIntent = new Intent(this,
								LoginActivity.class);
						startLoginActivityIntent.putExtra(
								UpgradeActivity.ARGUMENT_UPGRADE_ACTIVITY,
								"upgrade_activity");
						startLoginActivityIntent.putExtra(
								LoginActivity.FLURRY_SOURCE,
								FlurryConstants.FlurryUserStatus.GlobleMenu
										.toString());
						startActivityForResult(
								startLoginActivityIntent,
								MainActivity.LOGIN_ACTIVITY_CODE);
					}
				}

			} else if (link_type.equals("inapp")
					&& menuItemId
							.equalsIgnoreCase(GlobalMenuFragment.MENU_ITEM_DOWNLOADS_ACTION)) {
				menuItemClicked = getString(R.string.main_actionbar_settings_menu_item_my_collections);
/*
				Intent myCollectionActivityIntent = new Intent(
						getApplicationContext(), MyCollectionActivity.class);
				startActivity(myCollectionActivityIntent);*/

                //mTilesFragment.init(MediaType.PLAYLIST, null);

/*
                fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
                        R.anim.slide_left_exit, R.anim.slide_right_enter,
                        R.anim.slide_right_exit);
                fragmentTransaction.replace(R.id.main_fragmant_container,
                        mTilesFragment);
                fragmentTransaction
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
*/
				MyCollectionActivity mTilesFragment = new MyCollectionActivity();

                FragmentManager mFragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = mFragmentManager
                        .beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_and_show_bottom_enter,
						R.anim.slide_and_show_bottom_exit);
                fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
						mTilesFragment, "MyCollectionActivity");
                fragmentTransaction.addToBackStack("MyCollectionActivity");
				if(Constants.IS_COMMITALLOWSTATE)
					fragmentTransaction.commitAllowingStateLoss();
				else
					fragmentTransaction.commit();
				try{
					findViewById(R.id.home_browse_by_fragmant_container).setVisibility(View.VISIBLE);
				}catch (Exception e){}


			} else if (link_type.equals("inapp")
					&& menuItemId
							.equalsIgnoreCase(GlobalMenuFragment.MENU_ITEM_MY_FAVORITES_ACTION)) {
				menuItemClicked = getString(R.string.main_actionbar_settings_menu_item_my_favorites);

				/*Intent favoritesActivityIntent = new Intent(
						getApplicationContext(), FavoritesActivity.class);
				startActivity(favoritesActivityIntent);*/


                //mTilesFragment.init(MediaType.PLAYLIST, null);

/*
                fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
                        R.anim.slide_left_exit, R.anim.slide_right_enter,
                        R.anim.slide_right_exit);
                fragmentTransaction.replace(R.id.main_fragmant_container,
                        mTilesFragment);
                fragmentTransaction
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
*/
				FavoritesActivity mTilesFragment = new FavoritesActivity();
                FragmentManager mFragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = mFragmentManager
                        .beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_and_show_bottom_enter,
						R.anim.slide_and_show_bottom_exit);
                fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
						mTilesFragment, "FavoritesActivity");
                fragmentTransaction.addToBackStack("FavoritesActivity");
				if(Constants.IS_COMMITALLOWSTATE)
					fragmentTransaction.commitAllowingStateLoss();
				else
					fragmentTransaction.commit();
				try {
					findViewById(R.id.home_browse_by_fragmant_container).setVisibility(View.VISIBLE);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			} else if (link_type.equals("inapp")
					&& menuItemId
							.equalsIgnoreCase(GlobalMenuFragment.MENU_ITEM_MY_PLAYLISTS_ACTION)) {
				menuItemClicked = getString(R.string.main_actionbar_settings_menu_item_my_playlists);

				/*Intent playlistsActivityIntent = new Intent(
						getApplicationContext(), PlaylistsActivity.class);
				startActivity(playlistsActivityIntent);*/

/*
                ItemableTilesFragment mTilesFragment = new ItemableTilesFragment();
                mTilesFragment.init(MediaType.PLAYLIST, null);
*/

                /*
                fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
                        R.anim.slide_left_exit, R.anim.slide_right_enter,
                        R.anim.slide_right_exit);
                fragmentTransaction.replace(R.id.main_fragmant_container,
                        mTilesFragment);
                fragmentTransaction
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                */

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
				try
				{
					findViewById(R.id.home_browse_by_fragmant_container_playlist).setVisibility(View.VISIBLE);
					findViewById(R.id.home_browse_by_fragmant_container).setVisibility(View.VISIBLE);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			} else if (link_type.equals("inapp")
					&& menuItemId
							.equalsIgnoreCase(GlobalMenuFragment.MENU_ITEM_MY_STREAM_ACTION)) {
				menuItemClicked = getString(R.string.main_actionbar_settings_menu_item_my_stream);

				/*Intent startHomeActivityMySrteam = new Intent(
						getApplicationContext(), MyStreamActivity.class);

				startHomeActivityMySrteam
						.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
								| Intent.FLAG_ACTIVITY_CLEAR_TOP);

				startActivity(startHomeActivityMySrteam);*/
                MyStreamActivity mTilesFragment = new MyStreamActivity();

                FragmentManager mFragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = mFragmentManager
                        .beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_and_show_bottom_enter,
						R.anim.slide_and_show_bottom_exit);
                fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
						mTilesFragment, "MyStreamActivity");
                fragmentTransaction.addToBackStack("MyStreamActivity");
				if(Constants.IS_COMMITALLOWSTATE)
					fragmentTransaction.commitAllowingStateLoss();
				else
					fragmentTransaction.commit();
				try
				{
					findViewById(R.id.home_browse_by_fragmant_container).setVisibility(View.VISIBLE);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}


			} else if (link_type.equals("inapp")
					&& menuItemId
							.equalsIgnoreCase(GlobalMenuFragment.MENU_ITEM_LOGOUT_ACTION)) {
				menuItemClicked = getString(R.string.main_actionbar_settings_menu_item_logout);
				// AccountSettingsFragment accountSettingsFragment = new
				// AccountSettingsFragment(
				// this);
				// accountSettingsFragment.showLogoutDialog();
				if (Utils.isConnected()) {
					startActivity(new Intent(MainActivity.this,
							LogoutActivity.class));
				} else {
					CustomAlertDialog alertBuilder = new CustomAlertDialog(
							MainActivity.this);
					alertBuilder.setMessage(getResources().getString(
							R.string.go_online_network_error));
					alertBuilder.setNegativeButton("OK", null);
					// alertBuilder.create();
					alertBuilder.show();
				}
			} else if (link_type.equals("inapp")
					&& menuItemId
							.equalsIgnoreCase(GlobalMenuFragment.MENU_ITEM_HTML_ACTION)) {

			} else if (link_type.equals("inapp")
					&& menuItemId
							.equalsIgnoreCase(GlobalMenuFragment.MENU_ITEM_REDEEM_COUPON_ACTION)) {
				if(!isFinishing()){
					menuItemClicked = getString(R.string.main_actionbar_settings_menu_item_redeem_coupon);
					rental = new RedeeomCouponDialog(this);
					if(extraData!=null)
						extraData.setTitle(title_menu);
					rental.setExtraData(extraData);
					rental.show();	
				}
				
			} else if (link_type.equals("inapp")
					&& menuItemId
							.equalsIgnoreCase(GlobalMenuFragment.MENU_ITEM_LANGUAGE_SETTINGS_ACTION)) {
				menuItemClicked = getString(R.string.main_actionbar_settings_menu_item_language_settings);
				// HomeActivity.isLanguageSettingOppenned = true;
				// Intent LangaugeActivityIntent = new Intent(
				// getApplicationContext(), LangangeRewardsActivity.class);
				// startActivity(LangaugeActivityIntent);
				try
				{
					Logger.s("Operation id :::: " + GlobalMenuFragment.MENU_ITEM_LANGUAGE_SETTINGS_ACTION);
						FragmentTransaction fragmentTransaction = mFragmentManager
									.beginTransaction();
						fragmentTransaction.setCustomAnimations(
						R.anim.slide_left_enter, R.anim.slide_left_exit,
						R.anim.slide_right_enter, R.anim.slide_right_exit);

						LanguageSettingsFragment languageSettingsFragment = new LanguageSettingsFragment();
						if (!this.isFinishing()) {
									// Show DialogFragment
								languageSettingsFragment.show(mFragmentManager,
								"LanguageSettingsFragment");
						}
					}
				catch (Exception e){
						e.printStackTrace();
				}

			} else if (link_type.equals("inapp")
					&& menuItemId
							.equalsIgnoreCase(GlobalMenuFragment.MENU_ITEM_SUBSCRIPTION_PLAN_ACTION)) {

			} else if (link_type.equals("inapp")
					&& menuItemId
							.equalsIgnoreCase(GlobalMenuFragment.MENU_ITEM_REWARDS_ACTION)) {
				menuItemClicked = getString(R.string.main_actionbar_settings_menu_item_rewards);

				Intent redeemActivityIntent = new Intent(
						getApplicationContext(), RedeemActivity.class);
				startActivity(redeemActivityIntent);
			} else if (link_type.equals("inapp")
					&& menuItemId
							.equalsIgnoreCase(GlobalMenuFragment.MENU_ITEM_SETTINGS_ACTION)) {
				menuItemClicked = getString(R.string.main_actionbar_settings_menu_item_settings_and_accounts);

				Intent intent = new Intent(getApplicationContext(),
						SettingsActivity.class);
				startActivity(intent);
			} else if (link_type.equals("inapp")
					&& menuItemId
							.equalsIgnoreCase(GlobalMenuFragment.MENU_ITEM_ABOUT_ACTION)) {
				menuItemClicked = getString(R.string.main_actionbar_settings_menu_item_about);

				Intent aboutActivityIntent = new Intent(
						getApplicationContext(), AboutActivity.class);
				startActivity(aboutActivityIntent);
			} else if (link_type.equals("inapp")
					&& menuItemId
							.equalsIgnoreCase(GlobalMenuFragment.MENU_ITEM_HELP_FAQ_ACTION)) {
				menuItemClicked = getString(R.string.main_actionbar_settings_menu_item_help_faq);

				Intent helpAndFaqActivityIntent = new Intent(
						getApplicationContext(), HelpAndFAQActivity.class);
				startActivity(helpAndFaqActivityIntent);
			} else if (link_type.equals("inapp")
					&& menuItemId
							.equalsIgnoreCase(GlobalMenuFragment.MENU_ITEM_APP_TOUR_ACTION)) {
				menuItemClicked = getString(R.string.main_actionbar_settings_menu_item_app_tour);
				Intent appTourActivityIntent = new Intent(
						getApplicationContext(), AppTourActivity.class);
				startActivity(appTourActivityIntent);
			} else if (link_type.equals("inapp")
					&& menuItemId
							.equalsIgnoreCase(GlobalMenuFragment.MENU_ITEM_RATE_OUT_APP_ACTION)) {
				menuItemClicked = getString(R.string.main_actionbar_settings_menu_item_rate_this_app);

				ScreenLockStatus.getInstance(getBaseContext()).dontShowAd();
				mAppirater.rateAppClick(MainActivity.this);
			} else if (link_type.equals("inapp")
					&& menuItemId
							.equalsIgnoreCase(GlobalMenuFragment.MENU_ITEM_YOUR_FEEDBACK_ACTION)) {

				menuItemClicked = getString(R.string.main_actionbar_settings_menu_item_give_feedback);

				Intent feedbackActivityIntent = new Intent(
						getApplicationContext(), FeedbackActivity.class);
				startActivity(feedbackActivityIntent);
			} else if (link_type.equals("inapp")
					&& menuItemId
							.equalsIgnoreCase(GlobalMenuFragment.MENU_ITEM_UPGRADE_ACTION)) {

				Intent intent = new Intent(MainActivity.this,
						UpgradeActivity.class);
				intent.putExtra(UpgradeActivity.IS_TRIAL_PLANS, true);
				intent.putExtra("plan_clicked", getIntent()
						.getSerializableExtra("plan_clicked"));
				startActivityForResult(intent,
						HomeActivity.UPGRADE_ACTIVITY_RESULT_CODE);
			} else if (link_type.equals("inapp")
					&& menuItemId
							.equalsIgnoreCase(GlobalMenuFragment.MENU_ITEM_SUBSCRIPTION_STATUS_ACTION)) {
				if(!(mApplicationConfigurations.isRealUser() || Logger.allowPlanForSilentUser) || (!CacheManager.isProUser(this) && !CacheManager.isTrialUser
						(this))) {
					Boolean loggedIn = mApplicationConfigurations.isRealUser();
					Map<String, String> reportMap = new HashMap<String, String>();
					reportMap.put(FlurryConstants.FlurrySubscription.SourcePage
									.toString(),
							FlurryConstants.FlurrySubscription.LeftMenu
									.toString());
					reportMap.put(
							FlurryConstants.FlurrySubscription.LoggedIn.toString(),
							loggedIn.toString());
					Analytics.logEvent(
							FlurryConstants.FlurrySubscription.TapsOnUpgrade
									.toString(), reportMap);

					Intent intent = new Intent(this, UpgradeActivity.class);
					intent.putExtra(UpgradeActivity.IS_TRIAL_PLANS, true);
					intent.putExtra(UpgradeActivity.EXTRA_IS_GO_OFFLINE, false);
					intent.putExtra(
							UpgradeActivity.EXTRA_IS_FROM_NO_INTERNET_PROMT, false);
					startActivityForResult(intent,
							HomeActivity.UPGRADE_ACTIVITY_RESULT_CODE);
				} else {
					Intent settingsIntent = new Intent(this, SettingsActivity.class);
					settingsIntent.putExtra("show_membership", true);
					startActivity(settingsIntent);
				}
			} else if (link_type.equals("inapp")
					&& menuItemId
					.equalsIgnoreCase(GlobalMenuFragment.MENU_ITEM_MOBILE_RECHARGE_ACTION)) {
				Intent i = new Intent(this, WebviewNativeActivity.class);
				i.putExtra("is_inapp", true);
				i.putExtra("title_menu", title_menu);
				startActivity(i);
			} else if (link_type.equals("html")) {
				isWebViewCalled = true;
				Intent i = new Intent(this, WebviewNativeActivity.class);
				i.putExtra("url", html_url);
				i.putExtra("title_menu", title_menu);

				startActivity(i);
			} else if (link_type.equals("popup")) {

				RadioFullPlayerInfoDialog radioFullPlayerMoreDialog = new RadioFullPlayerInfoDialog(
						this, popup_msg);
				radioFullPlayerMoreDialog.setCancelable(false);
				radioFullPlayerMoreDialog.show();
			}

		}
		if (menuItemClicked != null) {
			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(FlurryConstants.FlurryNavigation.MenuOptionselected
					.toString(), menuItemClicked);
			Analytics.logEvent(
					FlurryConstants.FlurryNavigation.NavigationDrawer
							.toString(), reportMap);
		}
	}

	private RedeeomCouponDialog rental;

	// Object menuItemId_obj = "";
	boolean isClosedNeed = false;

	@Override
	public void onGlobalMenuItemSelected(final Object menuItemId, final String action) {

		if (mDrawerLayout != null)
			mDrawerLayout.closeDrawer(GravityCompat.START);

		// this.menuItemId_obj = menuItemId;
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				openNavigationItems(menuItemId, action);
			}
		}, 300);

	}

	// @Override
	// public void onQuickNavigationItemSelected(NavigationItem navigationItem)
	// {
	// try {
	// closeQuickNavigation();
	// if (mLastSelectedMenuItem != null) {
	// mLastSelectedMenuItem
	// .setIcon(R.drawable.background_actionbar_plus);
	// mLastSelectedMenuItem.setChecked(false);
	// }
	//
	// Logger.d(TAG, navigationItem.toString());
	//
	// setNavigationItemSelected(navigationItem);
	// } catch (Exception e) {
	// }
	// }

	// =======
	public void setNavigationItemSelectedFromGlobal(
			NavigationItem navigationItem) {

		// toggles the states of the navigation.
		mLastNavigationItem = mCurrentNavigationItem;
		mCurrentNavigationItem = navigationItem;

		/*
		 * colors back the item's background to transparent.
		 */
		if (mLastSelectedMenuItem != null) {
			View view = findViewById(mLastSelectedMenuItem.getItemId());
			if (view != null) {
				view.setBackgroundResource(R.drawable.transparent_background);
			}
		}
	}

	public void setNavigationItemSelected(NavigationItem navigationItem) {

		// toggles the states of the navigation.
		mLastNavigationItem = mCurrentNavigationItem;
		mCurrentNavigationItem = navigationItem;

		/*
		 * colors back the item's background to transparent.
		 */
		if (mLastSelectedMenuItem != null) {
			View view = findViewById(mLastSelectedMenuItem.getItemId());
			if (view != null) {
				view.setBackgroundResource(R.drawable.transparent_background);
			}
		}

		if (navigationItem == NavigationItem.VIDEOS
				|| navigationItem == NavigationItem.MUSIC) {
			// restarts the Home Activity.
			Intent startHomeActivity = new Intent(getApplicationContext(),
					HomeActivity.class);
			if (navigationItem == NavigationItem.VIDEOS) {
				startHomeActivity.putExtra(
						HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE,
						(Serializable) MediaContentType.VIDEO);
			} else {
				startHomeActivity.putExtra(
						HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE,
						(Serializable) MediaContentType.MUSIC);
			}
			startHomeActivity.putExtra(
					HomeActivity.ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION,
					HomeTabBar.TAB_ID_LATEST);
			startHomeActivity.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
					| Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(startHomeActivity);

		} else if (navigationItem == NavigationItem.DISCOVER) {
			Intent startDiscoveryIntent = new Intent(getApplicationContext(),
					DiscoveryActivity.class);
			startDiscoveryIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
					| Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(startDiscoveryIntent);

		}
	}

	// ======================================================
	// PRIVATE HELPER METHODS.
	// ======================================================

	protected boolean closePlayerBarContent() {
		if (mPlayerBarFragment == null) {
			mPlayerBarFragment = getPlayerBar();

		}

		if (mPlayerBarFragment != null && mPlayerBarFragment.isVisible()
				&& mPlayerBarFragment.isContentOpened()) {
			mPlayerBarFragment.closeContent();
			return true;
		}

		return false;
	}

	private void closeFragmentOfMenuItem(MenuItem menuItem) {
		int itemId = menuItem.getItemId();

		if (itemId == R.id.menu_item_main_actionbar_search) {
			closeMainSearch();
		}

	}

	protected void openMainSearch(String query, String flurrySourceSection,boolean needToOpenActitivy) {


        Bundle detailsData = new Bundle();

        if(this instanceof HomeActivity &&  !needToOpenActitivy){
//            int actionBarHeight = 0;
//            TypedValue tv = new TypedValue();
//            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
//            {
//                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
//            }
//
//            FrameLayout fram= (FrameLayout) findViewById(R.id.home_browse_by_fragmant_container);
//            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) fram.getLayoutParams();
//            params.setMargins(0, actionBarHeight, 0, (int) getResources().getDimension(R.dimen.main_player_drawer_header_height));
//
//            fram.setLayoutParams(params);
//
//            ColorDrawable cd = new ColorDrawable(getResources().getColor(
//                    R.color.myPrimaryColor));
//
//            int sdk = android.os.Build.VERSION.SDK_INT;
//            if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
//                mToolbar.setBackgroundDrawable(cd);
//            } else {
//                mToolbar.setBackground(cd);
//            }

            MainSearchFragmentNew mediaDetailsFragment = new MainSearchFragmentNew();
            if (!query.equalsIgnoreCase(FRAGMENT_TAG_MAIN_SEARCH)) {
                detailsData.putString(VideoActivity.ARGUMENT_SEARCH_VIDEO, query);
            } else {
                if (getIntent().getBooleanExtra("song_catcher", false)) {
                    detailsData.putBoolean("song_catcher", true);
                }
            }
            if (flurrySourceSection
                    .equalsIgnoreCase(FlurryConstants.FlurrySearch.FullPlayer
                            .toString())) {
                detailsData.putBoolean("from_full_player", true);
            }

            mediaDetailsFragment.setArguments(detailsData);
            FragmentManager mFragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = mFragmentManager
                    .beginTransaction();
//		fragmentTransaction.setCustomAnimations(
//				R.anim.slide_left_enter,
//				R.anim.slide_left_exit);
            fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
					mediaDetailsFragment, "MainSearchActivity");
            fragmentTransaction.addToBackStack("MainSearchActivity");

			if(Constants.IS_COMMITALLOWSTATE)
				fragmentTransaction.commitAllowingStateLoss();
			else
				fragmentTransaction.commit();

        }else{
            Intent i = new Intent(this, MainSearchFragment.class);
            if (!query.equalsIgnoreCase(FRAGMENT_TAG_MAIN_SEARCH)) {
                i.putExtra(VideoActivity.ARGUMENT_SEARCH_VIDEO, query);
            } else {
                if (getIntent().getBooleanExtra("song_catcher", false)) {
                    i.putExtra("song_catcher", true);
                }
            }
            if (flurrySourceSection
                    .equalsIgnoreCase(FlurryConstants.FlurrySearch.FullPlayer
                            .toString())) {
                i.putExtra("from_full_player", true);
            }
            startActivity(i);
        }

       // removeDrawerIconAndPreference();

		// Flurry report: Search button tapped
		Map<String, String> reportMap = new HashMap<String, String>();
		reportMap.put(FlurryConstants.FlurrySearch.SourceSection.toString(),
				flurrySourceSection);
		Analytics.logEvent(
				FlurryConstants.FlurrySearch.SearchButtonTapped.toString(),
				reportMap);
		try {
			findViewById(R.id.progressbar).setVisibility(View.GONE);
			findViewById(R.id.home_browse_by_fragmant_container).setVisibility(View.VISIBLE);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/*public static void openMainSearch1(Context context, String query,
			String flurrySourceSection) {

		Intent i = new Intent(context, MainSearchFragment.class);

		if (!query.equalsIgnoreCase(FRAGMENT_TAG_MAIN_SEARCH)) {
			i.putExtra(VideoActivity.ARGUMENT_SEARCH_VIDEO, query);
		} else {
			if (((Activity) context).getIntent().getBooleanExtra(
					"song_catcher", false)) {
				i.putExtra("song_catcher", true);
			}
		}

		if (flurrySourceSection
				.equalsIgnoreCase(FlurryConstants.FlurrySearch.FullPlayer
						.toString())) {
			i.putExtra("from_full_player", true);
		}

		context.startActivity(i);



		// Flurry report: Search button tapped
		Map<String, String> reportMap = new HashMap<String, String>();
		reportMap.put(FlurryConstants.FlurrySearch.SourceSection.toString(),
				flurrySourceSection);
		Analytics.logEvent(
				FlurryConstants.FlurrySearch.SearchButtonTapped.toString(),
				reportMap);
	}

*/



    public static final String ARGUMENT_Offline_ACTIVITY = "argument_3Offline_activity";
	Handler handler = new Handler();

	public void openOfflineGuide() {

		if (mApplicationConfigurations.isEnabledHomeGuidePage3Offline()) {
			mApplicationConfigurations
					.setIsEnabledHomeGuidePage_3OFFLINE(false);
			mApplicationConfigurations.setIsSongCatched(false);

			try {
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						try {

							// SaveOfflineHelpDialog dialog = new
							// SaveOfflineHelpDialog(
							// currentRunningActivity);
							// dialog.show();
							sendBroadcast(new Intent(
									getString(R.string.inapp_prompt_action_saveofflinehelpdialog)));
						} catch (Exception e) {
							Logger.printStackTrace(e);
							mApplicationConfigurations
									.setIsEnabledHomeGuidePage_3OFFLINE(true);
						} catch (Error e) {
							Logger.printStackTrace(e);
							mApplicationConfigurations
									.setIsEnabledHomeGuidePage_3OFFLINE(true);
						}
					}
				}, 300);
			} catch (Exception e) {
				Logger.printStackTrace(e);
				mApplicationConfigurations
						.setIsEnabledHomeGuidePage_3OFFLINE(true);
			}

		}
	}

	private void closeQuickNavigation() {
		try {
			// mFragmentManager.popBackStack();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	private void closeMainSearch() {
		try {
			if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
				FragmentManager.BackStackEntry backEntry = getSupportFragmentManager()
						.getBackStackEntryAt(
								getSupportFragmentManager()
										.getBackStackEntryCount() - 1);
				String str = backEntry.getName();
				if (!str.equals(FRAGMENT_TAG_MAIN_GLOBAL_MENU))
					mFragmentManager.popBackStack();
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
			mFragmentManager.popBackStack();
		}
	}

	// ======================================================
	// PUBLIC.
	// ======================================================

	public enum NavigationItem {
		MUSIC(R.string.main_actionbar_navigation_music), VIDEOS(
				R.string.main_actionbar_navigation_videos), DISCOVER(
				R.string.main_actionbar_navigation_discover), RADIO(
				R.string.main_actionbar_navigation_radio), /*
															 * SPECIALS(
															 * R.string.
															 * main_actionbar_navigation_specials
															 * ),
															 */PROFILE(
				R.string.main_actionbar_navigation_profile), /*
															 * NOTIFICATIONS(
															 * R.string.
															 * main_actionbar_navigation_notifications
															 * ),
															 */OTHER(
				R.string.main_actionbar_navigation_music), /*
															 * SONGID( R.string.
															 * main_actionbar_navigation_song_id
															 * ),
															 */MY_STREAM(
				R.string.main_actionbar_settings_menu_item_my_stream);

		public final int title;

		NavigationItem(int title) {
			this.title = title;
		}
	}

	public PlayerBarFragment getPlayerBar() {

		try {
			mFragmentManager = getSupportFragmentManager();
			mPlayerBarFragment = (PlayerBarFragment) mFragmentManager
					.findFragmentByTag(FRAGMENT_TAG_MAIN_PLAYER_BAR);
			if (mPlayerBarFragment == null) {

				mPlayerBarFragment = new PlayerBarFragment();
				FragmentTransaction fragmentTransaction = mFragmentManager
						.beginTransaction();
				fragmentTransaction.add(R.id.main_fragmant_player_bar,
						mPlayerBarFragment, FRAGMENT_TAG_MAIN_PLAYER_BAR);
				// fragmentTransaction.addToBackStack(null);
				fragmentTransaction.disallowAddToBackStack();
				fragmentTransaction.commit();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		return mPlayerBarFragment;
	}

//	@Override
//	public Object onRetainCustomNonConfigurationInstance() {
//		Logger.s(" :::::::::::::::::::::::; onRetainCustomNonConfigurationInstance");
//		return super.onRetainCustomNonConfigurationInstance();
//	}

	public FragmentManager getSupportFragmentManager() {
		return super.getSupportFragmentManager();
	}

	/**
	 * Retrieves the Navigation item of the current visible activity.
	 */
	protected abstract NavigationItem getNavigationItem();

	public void showLoadingDialog(int messageResource) {
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

	public void hideLoadingDialog() {
		try {
			hideLoadingDialogNew();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private MyProgressDialog mProgressDialog;

	public void showLoadingDialog(String message) {
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

	public void hideLoadingDialogNew() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	private CustomProgressDialog mProgressDialog_discover;

	// public void showLoadingDialogDiscovery(String message) {
	// try {
	// if (!isFinishing()) {
	// if (mProgressDialog_discover == null) {
	// mProgressDialog_discover = new CustomProgressDialog(this);
	// mProgressDialog_discover.setCancelable(true);
	// mProgressDialog_discover.setCanceledOnTouchOutside(false);
	// mProgressDialog_discover.show();
	// }
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	public void hideLoadingDialogNewDiscovery() {
		try {
			if (mProgressDialog_discover != null) {
				mProgressDialog_discover.dismiss();
				mProgressDialog_discover = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Indicates whatever any of the ActionBar's item is selected.
	 */
	protected boolean isAnyActionBarOptionSelected() {
		// iterates thru the menu and checks for checked menu items.
		if (mMenu != null) {
			MenuItem menuItem = null;
			for (int i = 0; i < mMenu.size(); i++) {
				menuItem = mMenu.getItem(i);
				/*
				 * we care only if one of the Quick Navigation, Search and
				 * Global Menu is checked - visible on the screen.
				 */
				int itemId = menuItem.getItemId();
				// menu comment
				if (/* itemId == R.id.menu_item_main_actionbar_navigation || */itemId == R.id.menu_item_main_actionbar_search
				/* || itemId == R.id.menu_item_main_actionbar_settings */) {

					if (menuItem.isChecked())
						return true;
				}
			}

			// if (mLastSelectedMenuItem != null
			// && mLastSelectedMenuItem.getItemId() ==
			// R.id.menu_item_main_actionbar_search) {
			// return true;
			// }

		}
		return false;
	}

	// protected NavigationItem getCurrentNavigationItem() {
	// return mCurrentNavigationItem;
	// }

	// protected NavigationItem getLastNavigationItem() {
	// return mLastNavigationItem;
	// }

	protected boolean isActivityDestroyed() {
		return mIsDestroyed;
	}

	public static boolean switchToOfflineMode = false;

	public void internetConnectivityPopup(
			final OnRetryClickListener onRetryClickListener) {
		if (mIsResumed) {
			switchToOfflineMode = false;
			if (!Utils.isConnected()) {
				// Utils.makeText(this,
				// getString(R.string.message_offline_switching_no_internet),
				// Toast.LENGTH_SHORT).show();
				// mApplicationConfigurations.setSaveOfflineAutoMode(true);
				// closePlayerBarContent();
				// sendBroadcast(new
				// Intent(MainActivity.ACTION_OFFLINE_MODE_CHANGED));
				//
				// Map<String, String> reportMap = new HashMap<String,
				// String>();
				// reportMap.put(FlurryConstants.FlurryCaching.Source.toString(),
				// FlurryConstants.FlurryCaching.NoInternetPrompt.toString());
				// reportMap.put(FlurryConstants.FlurryCaching.UserStatus.toString(),
				// Utils.getUserState(MainActivity.this));
				// Analytics.logEvent(
				// FlurryConstants.FlurryCaching.GoOffline.toString(),
				// reportMap);
				if (HungamaApplication.isActivityVisible()) {
					HomeActivity.needToShowAirplaneDialog = false;
					Intent i = new Intent(currentRunningActivity,
							OfflineAlertActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					currentRunningActivity.startActivity(i);
				} else {
					HomeActivity.needToShowAirplaneDialog = true;
				}
			}
		} else {
			switchToOfflineMode = true;
		}

	}

	public static void internetConnectivityPopup1(
			final OnRetryClickListener onRetryClickListener, Activity context) {
		if (!Utils.isConnected()) {
			// Utils.makeText(
			// context,
			// context.getString(R.string.message_offline_switching_no_internet),
			// Toast.LENGTH_SHORT).show();
			// ApplicationConfigurations mApplicationConfigurations =
			// ApplicationConfigurations
			// .getInstance(context);
			// mApplicationConfigurations.setSaveOfflineAutoMode(true);
			// context.sendBroadcast(new Intent(
			// MainActivity.ACTION_OFFLINE_MODE_CHANGED));
			//
			// Map<String, String> reportMap = new HashMap<String, String>();
			// reportMap.put(FlurryConstants.FlurryCaching.Source.toString(),
			// FlurryConstants.FlurryCaching.NoInternetPrompt.toString());
			// reportMap.put(FlurryConstants.FlurryCaching.UserStatus.toString(),
			// Utils.getUserState(context));
			// Analytics.logEvent(FlurryConstants.FlurryCaching.GoOffline.toString(),
			// reportMap);
			if (HungamaApplication.isActivityVisible()) {
				HomeActivity.needToShowAirplaneDialog = false;
				Intent i = new Intent(context, OfflineAlertActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(i);
			} else {
				HomeActivity.needToShowAirplaneDialog = true;
			}
		}
	}

	public interface OnRetryClickListener {
		public void onRetryButtonClicked();
	}

	@Override
	public void onLowMemory() {
		Utils.clearCache(true);
		super.onLowMemory();
	}

	@Override
	public void onItemSelected(String item) {
	}

	@Override
	public void onItemSelectedPosition(int itemPosition) {
		// TODO Auto-generated method stub
		if (itemPosition == lastItemPosition) {
			boolean isSameCategory = true;
			String categoryName = musicCategoriesResponse.getCategories().get(
					itemPosition);
			List<MusicCategoryGenre> genres = musicCategoriesResponse
					.getGenres();
			if (genres != null && genres.size() > 0) {
				for (MusicCategoryGenre genre : genres) {
					if (categoryName.equals(genre.getCategory())) {
						isSameCategory = false;
						break;
					}
				}
			}
			if (isSameCategory)
				return;
		} else if (!Utils.isConnected()) {
			Toast.makeText(
					this,
					getResources().getString(
							R.string.connection_error_empty_view_title),
					Toast.LENGTH_SHORT).show();
			return;
		}
		try {
			// Category obj = mCategories.get(itemPosition);
			String categoryName = musicCategoriesResponse.getCategories().get(
					itemPosition);
			List<MusicCategoryGenre> genres = musicCategoriesResponse
					.getGenres();
			if (genres != null && genres.size() > 0) {
				for (MusicCategoryGenre genre : genres) {
					if (categoryName.equals(genre.getCategory())) {
						showGenresForCategory(itemPosition, genre);
						return;
					}
				}
			}
			lastItemPosition = itemPosition;
			MediaContentType mediaContentType = MediaContentType.MUSIC;

			mDataManager.getApplicationConfigurations()
					.setSelctedMusicPreference(categoryName);
			Map<String, String> reportMap1 = new HashMap<String, String>();
			reportMap1.put(
					FlurryConstants.FlurryBrowseBy.LanguageCategorySelected
							.toString(), categoryName);
			Analytics.logEvent(
					FlurryConstants.FlurryBrowseBy.BrowseByMusic.toString(),
					reportMap1);
			// }
			LanguageButtonLollipop btn_preference = (LanguageButtonLollipop) findViewById(R.id.btn_preferences);
			btn_preference.setText(categoryName);
			savePreferences(itemPosition, categoryName, mediaContentType, "",
					true);
//			ApsalarEvent.postEvent(this, ApsalarEvent.PREFERENCE_CHANGED);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	private void showGenresForCategory(final int itemPosition,
			final MusicCategoryGenre categoryGenre) {
		GenreSelectionDialogNew genreSelectionDialogNew = GenreSelectionDialogNew
				.newInstance();
		genreSelectionDialogNew.setLangData(this, categoryGenre.getGenre(),
				new GenreSelectionDialogNew.GenereSelectionDialogListener() {
					@Override
					public void onGenreEditDialog(String genre) {
						if (genre != null) {
							lastItemPosition = itemPosition;
							MediaContentType mediaContentType = MediaContentType.MUSIC;

							boolean isCategoryChanged = false, isGenerChanged = false;
							if (!mApplicationConfigurations
									.getSelctedMusicPreference()
									.equalsIgnoreCase(
											categoryGenre.getCategory())) {
								mDataManager.getApplicationConfigurations()
										.setSelctedMusicPreference(
												categoryGenre.getCategory());
								isCategoryChanged = true;
							}

							if (!mApplicationConfigurations
									.getSelctedMusicGenre().equalsIgnoreCase(
											genre)) {
								mDataManager.getApplicationConfigurations()
										.setSelctedMusicGenre(genre);
								isGenerChanged = true;
							}

							if (isCategoryChanged || isGenerChanged) {
								Map<String, String> reportMap1 = new HashMap<String, String>();
								reportMap1
										.put(FlurryConstants.FlurryBrowseBy.LanguageCategorySelected
												.toString(), categoryGenre
												.getCategory());
								Analytics
										.logEvent(
												FlurryConstants.FlurryBrowseBy.BrowseByMusic
														.toString(), reportMap1);

								Map<String, String> reportMap2 = new HashMap<String, String>();
								reportMap2.put(
										FlurryConstants.FlurryKeys.Source
												.toString(),
										"Category Selection");
								reportMap2.put(
										FlurryConstants.FlurryKeys.NameOfGenre
												.toString(), genre);
								Analytics
										.logEvent(
												FlurryConstants.FlurryEventName.ContextMenuGenre
														.toString(), reportMap2);

								LanguageButtonLollipop btn_preference = (LanguageButtonLollipop) findViewById(R.id.btn_preferences);
								btn_preference.setText(categoryGenre
										.getCategory());
								savePreferences(0, categoryGenre.getCategory(),
										mediaContentType, genre, true);
//								ApsalarEvent.postEvent(MainActivity.this, ApsalarEvent.PREFERENCE_CHANGED);
							}
						}
					}
				});
		genreSelectionDialogNew.show(mFragmentManager, "GenreSelectionDialog");
	}



//
	// 	Added by Patibandha
	//

	/*public void StopMediaToCast(){
		mCastManager.removeVideoCastConsumer(mCastConsumer1);
	}

	public void PauseMediaToCast(){
		*//*try {
			if(isCastConnected() && isCastPlaying() && isCastRemoteLoaded()) {
				mCastManager.pause();
			}
		} catch ( CastException | TransientNetworkDisconnectionException
				| NoConnectionException | IllegalArgumentException e) {
			Logger.e(TAG, e + "Exception pausing cast playback");

		}*//*
	}

	public boolean isCastConnected(){
		try {
			return mCastManager.isConnected();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean isCastPlaying(){
		try {
			return mCastManager.isRemoteMediaPlaying();
		} catch (TransientNetworkDisconnectionException e) {
			e.printStackTrace();
		} catch (NoConnectionException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean isCastPaused(){
		try {
			return mCastManager.isRemoteMediaPaused();
		} catch (TransientNetworkDisconnectionException e) {
			e.printStackTrace();
		} catch (NoConnectionException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean isCastRemoteLoaded(){
		try {
			return mCastManager.isRemoteMediaLoaded();
		} catch (TransientNetworkDisconnectionException e) {
			e.printStackTrace();
		} catch (NoConnectionException e) {
			e.printStackTrace();
		}
		return false;
	}*/

	public void ResumeMediaToCast(){
		/*if(PlayerService.service!=null && PlayerService.service.isPlaying() && !PlayerService.service.isAdPlaying()){
			Track track = PlayerService.service.getCurrentPlayingTrack();
			if(track!=null)
				try {

					StartMediaToCast(track, true, track.getId() + "", PlayerService.service.getCurrentPlayingProgress());
				} catch (TransientNetworkDisconnectionException e) {
					e.printStackTrace();
				} catch (NoConnectionException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
		}*/
	}

	/*boolean autoPlay = true;
	private MenuItem mMediaRouteMenuItem;
	private static final int DELAY_MILLIS = 1000;
	protected VideoCastManager mCastManager;
	//private MediaRouter mMediaRouter;
	private volatile int mCurrentPosition;
	private static final String MIME_TYPE_AUDIO_MPEG = "audio/mpeg";
	protected final VideoCastConsumerImpl mCastConsumer1 = new VideoCastConsumerImpl() {

		@Override
		public void onFailed(int resourceId, int statusCode) {
			Logger.d(TAG, "Cast ::::::::::::::::::::: onFailed " + resourceId + "  status " + statusCode);
		}

		@Override
		public void onConnectionSuspended(int cause) {
			Logger.d(TAG, "Cast ::::::::::::::::::::: onConnectionSuspended() was called with cause: " + cause);
			*//*if(PlayerService.service!=null)
				PlayerService.service.stopCasting();*//*
		}

		@Override
		public void onConnectivityRecovered() {
		}

		@Override
		public void onCastDeviceDetected(final MediaRouter.RouteInfo info) {

			Logger.d(TAG, "Cast ::::::::::::::::::::: Route is visible: " + info);
			//Utils.makeText(MainActivity.this,"Cast Device Detected", Toast.LENGTH_SHORT).show();
			if(mMediaRouteMenuItem!=null && homeActivity!=null){
				homeActivity.castMenuItemHideShow(false);
			}
		}

		@Override
		public void onApplicationConnected(ApplicationMetadata appMetadata, String sessionId,
										   boolean wasLaunched) {
			//Logger.i(TAG, "Cast ::::::::::::::::::::: Route is visible: MainAct " + wasLaunched);
			//ApsalarEvent.postEvent(MainActivity.this, ApsalarEvent.CHROMECAST_CONNECTED);
		}

		@Override
		public void onRemoteMediaPlayerMetadataUpdated() {
			Logger.d(TAG, "Cast ::::::::::::::::::::: onRemoteMediaPlayerMetadataUpdated");
			updateMetadata();
		}

		@Override
		public void onRemoteMediaPlayerStatusUpdated() {
			Logger.d(TAG, "Cast ::::::::::::::::::::: onRemoteMediaPlayerStatusUpdated");
			updatePlaybackState();
		}

		@Override
		public void onDisconnected() {
			Logger.d(TAG, "Cast ::::::::::::::::::::: onDisconnected");
			if(homeActivity!=null)
				homeActivity.stopTracking(false);

			//mSessionExtras.remove(EXTRA_CONNECTED_CAST);
			//mSession.setExtras(mSessionExtras);
			//ChromeCastPlayback playback = new LocalPlayback(MusicService.this, mMusicProvider);
			//mMediaRouter.setMediaSession(null);
			//switchToPlayer(playback, false);
		}

		@Override
		public void onMediaQueueOperationResult(int operationId, int statusCode) {
			if(homeActivity!=null){
				homeActivity.stopCastNotification();
			}else{
				mCastManager.stopNotificationService();
			}
			super.onMediaQueueOperationResult(operationId, statusCode);
		}

		@Override
		public void onDeviceSelected(CastDevice device) {
			*//*if(device!=null) {
				Utils.makeText(MainActivity.this, "onDeviceSelected:" + device.getModelName(), Toast.LENGTH_SHORT).show();
			}else{
				Utils.makeText(MainActivity.this, "onDeviceSelected: null", Toast.LENGTH_SHORT).show();
			}*//*
			super.onDeviceSelected(device);
		}

		@Override
		public void onCastAvailabilityChanged(boolean castPresent) {
			Logger.d(TAG, "Cast ::::::::::::::::::::: onCastAvailabilityChanged");
			//Utils.makeText(MainActivity.this, "onCastAvailabilityChanged:"+castPresent, Toast.LENGTH_SHORT).show();
			super.onCastAvailabilityChanged(castPresent);
		}
	};

	*//*public void addVideoCastConsumer(){
		mCastManager.addVideoCastConsumer(mCastConsumer1);
		mCastManager.incrementUiCounter();
	}*//*

	public void onResumeCast(){
		//mCastManager.addVideoCastConsumer(mCastConsumer1);
		mCastManager.incrementUiCounter();
	}


	public void onPauseCast(){
		//mCastManager.removeVideoCastConsumer(mCastConsumer1);
		mCastManager.decrementUiCounter();
	}

	public void InitilizeCastManager()
	{
		// Added by Patibandha
		mCastManager = VideoCastManager.getInstance();
		mCastManager.reconnectSessionIfPossible();
		mCastManager.addVideoCastConsumer(mCastConsumer1);
		//mMediaRouter = MediaRouter.getInstance(MainActivity.this);
	}

	public void InitializeMenuItem(Menu menu){
		mMediaRouteMenuItem = mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
		//mMediaRouteMenuItem.setVisible(false);
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
				*//*if (!TextUtils.equals(mCurrentMediaId, remoteMediaId)) {
					mCurrentMediaId = remoteMediaId;
					if (mCallback != null) {
						mCallback.onMetadataChanged(remoteMediaId);
					}
					mCurrentPosition = getCurrentStreamPosition();
				}*//*
			}
		} catch (Exception e) {
			//Logger.e(TAG, e, "Exception processing update metadata");
		}
	}

	public void updatePlaybackState(){
		if(homeActivity!=null)
			homeActivity.updatePlaybackState();
	};

	@Override
	public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
		if(mCastManager != null)
			return mCastManager.onDispatchVolumeKeyEvent(event, HungamaApplication.VOLUME_INCREMENT)
					|| super.dispatchKeyEvent(event);

		return  super.dispatchKeyEvent(event);
	}*/


	public static MainActivity getInstance(){
		return _activity;
	}

}
