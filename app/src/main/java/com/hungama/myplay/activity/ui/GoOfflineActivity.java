/**
 * 
 */
package com.hungama.myplay.activity.ui;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apsalar.sdk.Apsalar;
import com.hungama.hungamamusic.ford.carmode.ProxyService;
import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.HungamaApplication;

import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.DataBase.CacheState;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.hungama.ApplicationImagesOperation;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.player.PlayerService.PlayerStateListener;
import com.hungama.myplay.activity.player.PlayerService.State;
import com.hungama.myplay.activity.player.PlayerServiceBindingManager;
import com.hungama.myplay.activity.ui.AppGuideActivityOfflineMusic.HelpLeftDrawer;
import com.hungama.myplay.activity.ui.AppGuideActivityOfflineMusic.HelpView;
import com.hungama.myplay.activity.ui.HomeActivity.MyPagerAdapter;
import com.hungama.myplay.activity.ui.adapters.DataNotFoundListAdapter;
import com.hungama.myplay.activity.ui.fragments.GlobalMenuFragment;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment.PlayingEventListener;
import com.hungama.myplay.activity.ui.inappprompts.AppPromptOfflineCachingTrialExpired;
import com.hungama.myplay.activity.ui.widgets.CustomAlertDialog;
import com.hungama.myplay.activity.ui.widgets.CustomCacheStateProgressBar;
import com.hungama.myplay.activity.ui.widgets.CustomViewPager;
import com.hungama.myplay.activity.ui.widgets.GoOfflineTabBar;
import com.hungama.myplay.activity.ui.widgets.GoOfflineTabBar.OnTabSelectedListener;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.ApsalarEvent;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.QuickActionDiscoveryGallery;
import com.hungama.myplay.activity.util.QuickActionDiscoveryGallery.OnDiscoverySelectedListener;
import com.hungama.myplay.activity.util.QuickActionOfflineSongMenu;
import com.hungama.myplay.activity.util.QuickActionOfflineSongMenu.OnDismissListener;
import com.hungama.myplay.activity.util.QuickActionOfflineSongMenu.OnOfflineItemSelectListener;
import com.hungama.myplay.activity.util.SwipeDismissListOffline;
import com.hungama.myplay.activity.util.Utils;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.OnItemSelectedListener;
import com.mobeta.android.dslv.DragSortListView.RemoveListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * @author XTPL
 * 
 */
public class GoOfflineActivity extends MainActivity implements
		OnTabSelectedListener, OnClickListener, PlayingEventListener,
		PlayerStateListener, ServiceConnection, OnOfflineItemSelectListener {

	// Resource ID #0xffffffff

	private GoOfflineTabBar mGoOfflineTabBar;
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;

	private List<MediaItem> mMediaItems;
	private List<MediaItem> mMediaItemsMusic;
	private List<MediaItem> mMediaItemsVideo;
	// private List<Boolean> mTrackRemoveState;
	private List<Boolean> mTrackRemoveStateMusic;
	private List<Boolean> mTrackRemoveStateVideo;

	// private PlayerBarFragment mPlayerBar;
	// private ListView mListView;
	private DragSortListView mListView;
	private GoOfflineTrackAdapter mOfflineAdapter;
	private GoOfflineTrackAdapter mOfflineAdapterMusic;
	private GoOfflineTrackAdapter mOfflineAdapterVideo;

	private OfflineModeReceiver offlineModeReceiver;

	private boolean isOnCreate;
	private boolean loadVideoDefault = false;
	private View footerViewMusic, footerViewVideo;

	private LanguageTextView mTextRemoveHint, mTextCancelRemoveState,
			mTextDeleteSelected;// , mTextOfflineOptions;
	private ImageView mIvSelectAll;
	private CheckBox mChkboxSelectAll;
	// private ImageButton mIbOfflineOptions;

	private PlayerStateReceiver playerStateReceiver;

	// private Animation animation1;
	// private Animation animation2;

	private PagerSlidingTabStrip tabs;
	private CustomViewPager pager;
	private MyPagerAdapter adapter;

	private final int TAB_SONGS = 0;
	private final int TAB_VIDEOS = 1;

	public static GoOfflineActivity Instance = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Instance = this;
		setOverlayAction();
		mDataManager = DataManager.getInstance(getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		super.onCreate(savedInstanceState);
		if (!mApplicationConfigurations.getSaveOfflineMode()) {
			try {
				setContentView(R.layout.activity_go_offline_music);
			} catch (Error e) {
				System.gc();
				System.runFinalization();
				System.gc();
				setContentView(R.layout.activity_go_offline_music);
			}
		} else
			try {
				setContentView(R.layout.activity_go_offline);
			} catch (Error e) {
				System.gc();
				System.runFinalization();
				System.gc();
				setContentView(R.layout.activity_go_offline);
			}

		onCreateCode();
        Set<String> tags = Utils.getTags();
        String tag= Constants.UA_TAG_OFFLINE_SWITCHUSED;
        if (!tags.contains(tag)) {
            tags.add(tag);
            Utils.AddTag(tags);
        }
		if (getIntent().getBooleanExtra("show_toast", false))
			overridePendingTransition(R.anim.fadein, R.anim.fadeout);

		if (mApplicationConfigurations.getSaveOfflineMode())
			getDrawerLayout();

		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(getWindow().getDecorView(),
					GoOfflineActivity.this);
		}
		if (mPlayerBarFragment == null) {
			mPlayerBarFragment = getPlayerBar();
			// mPlayerBar.updateNotificationForOffflineMode();
		}

		initializeUserControls();

		mGoOfflineTabBar = (GoOfflineTabBar) findViewById(R.id.offline_tab_bar);
		mGoOfflineTabBar.setOnTabSelectedListener(this);

		mListView = (DragSortListView) findViewById(R.id.go_offline_listview);
		// if (getCurrentNavigationItem() == NavigationItem.VIDEOS
		// || (getCurrentNavigationItem() == NavigationItem.OTHER &&
		// getLastNavigationItem() == NavigationItem.VIDEOS)) {
		// loadVideoDefault = true;
		// //
		// mGoOfflineTabBar.setCurrentSelected(GoOfflineTabBar.TAB_ID_VIDOES);
		// // // setTabCount();
		// // // if(CacheManager.isProUser(this))
		// // // mGoOfflineTabBar.updateTabCount(GoOfflineTabBar.TAB_ID_SONGS,
		// // // DBOHandler.getAllCachedTracks(this).size());
		// // // else
		// // // mGoOfflineTabBar.updateTabCount(GoOfflineTabBar.TAB_ID_SONGS,
		// // // DBOHandler.getAllTracksForFreeUser(this).size());
		// } else {
		// loadVideoDefault = false;
		// // onTabSelected(mGoOfflineTabBar.getSelectedTab());
		// // //
		// // mGoOfflineTabBar.setCurrentSelected(GoOfflineTabBar.TAB_ID_SONGS);
		// // // if(CacheManager.isProUser(this))
		// // // mMediaItems = DBOHandler.getAllCachedTracks(this);
		// // // else
		// // // mMediaItems = DBOHandler.getAllTracksForFreeUser(this);
		// // //
		// // // if (mMediaItems.size() == 0) {
		// // // displayNoDataDialog();
		// // // } else {
		// // // mOfflineAdapter = new GoOfflineTrackAdapter();
		// // // mListView.setAdapter(mOfflineAdapter);
		// // // mGoOfflineTabBar.updateTabCount(GoOfflineTabBar.TAB_ID_SONGS,
		// // // mMediaItems.size());
		// // // }
		// // // setTabCount();
		// // // mGoOfflineTabBar.updateTabCount(GoOfflineTabBar.TAB_ID_VIDOES,
		// // // DBOHandler.getAllVideoTracks(this).size());
		// }
		// setOptionsItemsVisibility();

		findViewById(R.id.go_offline_play_all).setOnClickListener(this);
		findViewById(R.id.go_offline_sort_a_to_z).setOnClickListener(this);
		findViewById(R.id.go_offline_sort_latest).setOnClickListener(this);
		findViewById(R.id.go_offline_settings).setOnClickListener(this);
		// findViewById(R.id.go_offline_settings).setVisibility(View.GONE);
		findViewById(R.id.offline_imagebutton_options).setOnClickListener(this);
		// findViewById(R.id.offline_textview_sorttype).setOnClickListener(this);

		if (offlineModeReceiver == null/* && CacheManager.isProUser(this) */) {
			offlineModeReceiver = new OfflineModeReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(MainActivity.ACTION_OFFLINE_MODE_CHANGED);
			registerReceiver(offlineModeReceiver, filter);
		}

		// if(!CacheManager.isProUser(this)){
		// findViewById(R.id.layout_free_user_offline_message).setVisibility(View.VISIBLE);
		// findViewById(R.id.layout_free_user_offline_message).setOnClickListener(this);
		// footerView =
		// getLayoutInflater().inflate(R.layout.list_item_free_user_offline_message,
		// null);
		// footerView.findViewById(R.id.text_free_user_offline_message).setOnClickListener(this);

		footerViewMusic = getLayoutInflater().inflate(
				R.layout.list_item_footer_go_pro, null);
		footerViewMusic.findViewById(R.id.iv_go_pro_now).setOnClickListener(
				this);
		footerViewMusic.findViewById(R.id.rl_footer_bg)
				.setOnClickListener(this);
		footerViewVideo = getLayoutInflater().inflate(
				R.layout.list_item_footer_go_pro, null);
		footerViewVideo.findViewById(R.id.iv_go_pro_now).setOnClickListener(
				this);
		footerViewVideo.findViewById(R.id.rl_footer_bg)
				.setOnClickListener(this);

		// footerView = findViewById(R.id.rl_go_pro_now);
		// findViewById(R.id.iv_go_pro_now).setOnClickListener(this);

		// mListView.addFooterView(footerView);
		// }
		DataManager mDataManager = DataManager.getInstance(this);
		if (mDataManager
				.isApplicationImageExist(ApplicationImagesOperation.DRAWABLE_GO_PRO_NOW)) {
			((ImageView) findViewById(R.id.iv_go_pro_now))
					.setImageDrawable(mDataManager
							.getApplicationImage(ApplicationImagesOperation.DRAWABLE_GO_PRO_NOW));
		}
		isOnCreate = true;
		// mTextOfflineOptions = (LanguageTextView)
		// findViewById(R.id.offline_textview_sorttype);
		// mIbOfflineOptions = (ImageButton)
		// findViewById(R.id.offline_imagebutton_options);
		mTextRemoveHint = (LanguageTextView) findViewById(R.id.offline_textview_tab_name);
		mTextCancelRemoveState = (LanguageTextView) findViewById(R.id.offline_textview_cancel_selection);
		mTextCancelRemoveState.setOnClickListener(this);
		mTextDeleteSelected = (LanguageTextView) findViewById(R.id.offline_textview_delete_selected);
		mTextDeleteSelected.setOnClickListener(this);
		mIvSelectAll = (ImageView) findViewById(R.id.offline_iv_select_all);
		mIvSelectAll.setOnClickListener(this);
		mChkboxSelectAll = (CheckBox) findViewById(R.id.offline_chkbox_select_all);
		// mChkboxSelectAll.setOnClickListener(this);
		mChkboxSelectAll
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							if (isAutoCheckedChange) {
								isAutoCheckedChange = false;
								return;
							}
							List<Boolean> mTrackRemoveState;
							if (mGoOfflineTabBar.getSelectedTab() == GoOfflineTabBar.TAB_ID_SONGS)
								mTrackRemoveState = mTrackRemoveStateMusic;
							else
								mTrackRemoveState = mTrackRemoveStateVideo;
							if (isChecked) {
								for (int i = 0; i < mTrackRemoveState.size(); i++)
									mTrackRemoveState.set(i, true);
							} else {
								for (int i = 0; i < mTrackRemoveState.size(); i++)
									mTrackRemoveState.set(i, false);
							}
							if (mOfflineAdapterMusic != null)
								mOfflineAdapterMusic.notifyDataSetChanged();
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
					}
				});

		// animation1 = AnimationUtils.loadAnimation(this, R.anim.to_middle);
		// animation1.setAnimationListener(this);
		// animation2 = AnimationUtils.loadAnimation(this, R.anim.from_middle);
		// animation2.setAnimationListener(this);
		// showCategoryActionBar(false);
		hideCategoryActionBar();
		registerReceivers();

		mListView.setItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(int pos) {
				try {
					int firstPosition = mListView.getFirstVisiblePosition()
							- mListView.getHeaderViewsCount();
					int wantedChild = pos - firstPosition;
					if (wantedChild < 0
							|| wantedChild >= mListView.getChildCount()) {
						Logger.w("GoOfflineActivity",
								"Unable to get view for desired position, because it's not being displayed on screen.");
						return;
					}

					View tile = mListView.getChildAt(wantedChild);
					if (tile != footerViewVideo && tile != footerViewMusic) {
						// View tile = mTrackListView.getChildAt(pos);
						// View tile = (View) wantedView;
						RelativeLayout rlChild = (RelativeLayout) tile
								.findViewById(R.id.relativelayout_player_queue_line);
						// start play from beginning or pause
						// RelativeLayout tile = (RelativeLayout) view;//
						//
						// ViewHolder1 viewHolder = (ViewHolder1) tile
						// .getTag(R.id.view_tag_view_holder);
						// ViewHolder1 viewHolder = (ViewHolder1) rlChild
						// .getTag(R.id.view_tag_view_holder);
						// int position = (Integer)
						// rlChild.getTag(R.id.view_tag_position);
						//
						// handlePlayClick(position, viewHolder);
						ViewHolder1 viewHolder = (ViewHolder1) rlChild
								.getTag(R.id.view_tag_view_holder);
						int position = (Integer) rlChild
								.getTag(R.id.view_tag_position);

						handlePlayClick(position, viewHolder, true);
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			}
		});

		// if (mApplicationConfigurations.getSaveOfflineMode()) {
		if (mApplicationConfigurations.idNeedToShowSaveOfflineHelp()) {
			findViewById(R.id.offline_tab_message).setVisibility(View.VISIBLE);
			findViewById(R.id.btn_help_close).setOnClickListener(this);
		} else {
			findViewById(R.id.offline_tab_message).setVisibility(View.GONE);
		}
		// }

		mServiceToken = PlayerServiceBindingManager.bindToService(
				this, this);
		startSyncProxy();
	}

	public void startSyncProxy() {
		if (ProxyService.getInstance() == null) {
			Intent startService = new Intent(this, ProxyService.class);
			Log.i("offline","Calling start Service to Start the Service");
			startService(startService);
		}
	}

	private PlayerServiceBindingManager.ServiceToken mServiceToken;

	// ListView listMusic, listVideo;
	DragSortListView listMusic, listVideo;

	private void initializeUserControls() {
		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		pager = (CustomViewPager) findViewById(R.id.pager);
		pager.setPagingEnabled(false);
		tabs.setTextColor(getResources().getColor(R.color.white_transparant));
		// tabs.setSelectedColor(getResources().getColor(R.color.white));
		// tabs.setFooterColor(getResources().getColor(R.color.white));
		// tabs.setFooterIndicatorStyle(IndicatorStyle.Underline);
		// tabs.setSelectedBold(true);
		// tabs.setAllCaps(true);
		tabs.setTextSize((int) getResources().getDimension(
				R.dimen.xlarge_text_size));
		tabs.setIndicatorColor(getResources().getColor(R.color.white));
		tabs.setUnderlineColor(getResources().getColor(R.color.white));
		tabs.setActivateTextColor(getResources().getColor(R.color.white));
		tabs.setDeactivateTextColor(getResources().getColor(
				R.color.white_transparant));
		tabs.setTabSwitch(true);
		tabs.setDividerColor(getResources().getColor(R.color.transparent));
		// tabs.setDividerColor(getResources().getColor(R.color.white));
		tabs.setUnderlineHeight(0);
		tabs.setIndicatorHeight(7);
		// tabs.setSelectedBold(true);

		if (adapter == null) {
			// adapter = new MyPagerAdapter(getSupportFragmentManager());
			// final int pageMargin = (int) TypedValue.applyDimension(
			// TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
			// .getDisplayMetrics());
			// pager.setPageMargin(pageMargin);
			// pager.setAdapter(adapter);
			// tabs.setViewPager(pager);
			// pager.setCurrentItem(HomeTabBar.TAB_INDEX_MUSIC);
			// tabs.notifyDataSetChanged();

			// listMusic = new ListView(this);
			// listVideo = new ListView(this);
			listMusic = new DragSortListView(this, null);
			listVideo = new DragSortListView(this, null);
			mSwipeListMusic = setUpSwipeView(listMusic,
					GoOfflineTabBar.TAB_ID_SONGS);
			mSwipeListVideo = setUpSwipeViewVideo(listVideo,
					GoOfflineTabBar.TAB_ID_VIDOES);
			setSelectionListener(listMusic, mSwipeListMusic);
			setSelectionListener(listVideo, mSwipeListVideo);
			// ListView listview3 = new ListView(this);

			Vector<View> pages = new Vector<View>();

			pages.add(listMusic);
			pages.add(listVideo);
			// pages.add(listview3);

			CustomPagerAdapter adapter = new CustomPagerAdapter(this, pages);
			final int pageMargin = (int) TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP, 2, getResources()
							.getDisplayMetrics());
			pager.setPageMargin(pageMargin);
			pager.setAdapter(adapter);
			tabs.setViewPager(pager);

			// listview1.setAdapter(new ArrayAdapter<String>(this,
			// android.R.layout.simple_list_item_1,new
			// String[]{"A1","B1","C1","D1"}));
			// listview2.setAdapter(new ArrayAdapter<String>(this,
			// android.R.layout.simple_list_item_1,new
			// String[]{"A2","B2","C2","D2"}));
			// listview3.setAdapter(new ArrayAdapter<String>(this,
			// android.R.layout.simple_list_item_1,new
			// String[]{"A3","B3","C3","D3"}));

			tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
				@Override
				public void onPageSelected(int arg0) {
					try {
						if (mSwipeListVideo != null)
							mSwipeListVideo.discardUndo();
					} catch (Exception e) {
					}
					try {
						if (mSwipeListMusic != null)
							mSwipeListMusic.discardUndo();
					} catch (Exception e) {
					}
					if (arg0 == TAB_SONGS) {
						mGoOfflineTabBar
								.setCurrentSelected(GoOfflineTabBar.TAB_ID_SONGS);
						mMediaItems = mMediaItemsMusic;
						// setUpSwipeView(listMusic);
						mTrackRemoveStateMusic = new ArrayList<Boolean>();
						initTrackRemoveState();
						mOfflineAdapter = mOfflineAdapterMusic;
						if (mMenu != null) {
							mMenu.clear();
							onCreateOptionsMenu(mMenu);
						}
					} else if (arg0 == TAB_VIDEOS) {
						mGoOfflineTabBar
								.setCurrentSelected(GoOfflineTabBar.TAB_ID_VIDOES);
						mMediaItems = mMediaItemsVideo;
						// setUpSwipeView(listVideo);
						mTrackRemoveStateVideo = new ArrayList<Boolean>();
						initTrackRemoveState();
						mOfflineAdapter = mOfflineAdapterVideo;
						if (mMenu != null) {
							mMenu.clear();
							onCreateOptionsMenu(mMenu);
						}
					}
					notifyAdapters();
				}

				@Override
				public void onPageScrollStateChanged(int arg0) {
				}

				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {
				}
			});
		}
	}

	public class CustomPagerAdapter extends PagerAdapter {

		// private Context mContext;
		private Vector<View> pages;

		public CustomPagerAdapter(Context context, Vector<View> pages) {
			// this.mContext = context;
			this.pages = pages;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View page = pages.get(position);
			container.addView(page);
			return page;
		}

		@Override
		public int getCount() {
			return pages.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		private final String[] TITLES = {
				getResources().getString(
						R.string.social_profile_info_section_fav_songs_1),
				getResources().getString(
						R.string.main_actionbar_navigation_videos) };

		@Override
		public CharSequence getPageTitle(int position) {
			return TITLES[position];
		}
	}

	private CloseAppReceiver closeAppReceiver;
	private CacheDeleteReceiver cacheDeleteReceiver;

	private void registerReceivers() {

		if (closeAppReceiver == null) {
			closeAppReceiver = new CloseAppReceiver();
			IntentFilter filter_close = new IntentFilter();
			filter_close.addAction(HomeActivity.ACTION_CLOSE_APP);
			registerReceiver(closeAppReceiver, filter_close);
		}

		cacheDeleteReceiver = new CacheDeleteReceiver();
		IntentFilter deleteReceiver = new IntentFilter();
		deleteReceiver.addAction(CacheManager.ACTION_TRACK_CACHED);
		deleteReceiver.addAction(CacheManager.ACTION_VIDEO_TRACK_CACHED);
		registerReceiver(cacheDeleteReceiver, deleteReceiver);

	}

	class CacheDeleteReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!intent.getBooleanExtra("is_from_offline", false)
					&& ((intent.getAction().equals(
							CacheManager.ACTION_TRACK_CACHED) && mGoOfflineTabBar
							.getSelectedTab() == GoOfflineTabBar.TAB_ID_SONGS) || (intent
							.getAction().equals(
									CacheManager.ACTION_VIDEO_TRACK_CACHED) && mGoOfflineTabBar
							.getSelectedTab() == GoOfflineTabBar.TAB_ID_VIDOES)))
				onResume();
		}
	}

	private void closeApp() {
		// Intent i = new Intent(this, GoOfflineActivity.class);
		// i.putExtra("show_toast", true);
		// startActivity(i);
		mPlayerBarFragment.explicitStop();

		// reset the inner boolean for showing home
		// tile hints.
		mApplicationConfigurations.setIsHomeHintShownInThisSession(false);
		mApplicationConfigurations.setIsSearchFilterShownInThisSession(false);
		mApplicationConfigurations
				.setIsPlayerQueueHintShownInThisSession(false);
		// if this button is clicked, close
		// current activity
		GoOfflineActivity.super.onBackPressed();
		GoOfflineActivity.this.finish();

	}

	class CloseAppReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {

			closeApp();
			// Intent i = new Intent(GoOfflineActivity.this,
			// GoOfflineActivity.class);
			// i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			// i.putExtra("finish_app", true);
			// startActivity(i);
			// mPlayerBar.updateNotificationForOffflineMode();
			hideLoadingDialog();
		}
	}

	static boolean skipResume = false;

	@Override
	protected void onResume() {
		super.onResume();
//		if(AdXEvent.ENABLED){
////			Apsalar.restartSession();//unregisterApsalarReceiver();
//			Apsalar.restartSession(this, getString(R.string.apsalar_api_key), getString(R.string.apsalar_secret));
//		}

		// System.out.println("---------Go offline -------- " + mPlayerBar);
		// if (mPlayerBarFragment!=null) {
		// mPlayerBarFragment = getPlayerBar();
		// }

		if (isOnNewIntent)
			return;

		isShowingHelp = false;
		try {
			findViewById(R.id.progressbar).setVisibility(View.GONE);
		} catch (Exception e) {
		}

		try {
			if (playerStateReceiver == null) {
				playerStateReceiver = new PlayerStateReceiver();
				registerReceiver(playerStateReceiver, new IntentFilter(
						PlayerBarFragment.ACTION_PLAY_STATE_CHANGED));
			}
		} catch (Exception e) {
		} catch (java.lang.Error e) {
			Utils.clearCache();
		}

		try {
			IntentFilter airplaneMode = new IntentFilter();
			airplaneMode.addAction("android.intent.action.AIRPLANE_MODE");
			airplaneMode.addAction("android.net.conn.CONNECTIVITY_CHANGE");
			airplaneMode.addAction("android.net.wifi.WIFI_STATE_CHANGED");
			registerReceiver(AireplaneModeReceiver, airplaneMode);
		} catch (Exception e) {
		} catch (java.lang.Error e) {
			Utils.clearCache();
		}

		HungamaApplication.activityResumed();
		if (needToSwitchToOnlineMode) {
			needToSwitchToOnlineMode = false;
			handleOfflineSwitchCase(false);
		}
		// mMenu.findItem(R.id.menu_item_main_actionbar_offline_options).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		if (mApplicationConfigurations.isSongCatched()) {
			openOfflineGuide();
		}

		if (!mApplicationConfigurations.getSaveOfflineMode()) {
			showBackButtonWithTitle(
					getResources()
							.getString(
									R.string.main_actionbar_settings_menu_item_offline_music),
					"");
		}

		if (isOnCreate) {
			isOnCreate = false;
			// setTitleText(false);
			if (loadVideoDefault) {
				mGoOfflineTabBar
						.setCurrentSelected(GoOfflineTabBar.TAB_ID_VIDOES);
			} else {
				findViewById(R.id.offline_tab_bar_options1)
						.setVisibility(View.GONE);
				((LinearLayout) findViewById(R.id.go_offline_options))
						.setVisibility(View.GONE);
				showLoadingDialog(null);
				// onTabSelected(mGoOfflineTabBar.getSelectedTab());
				mListView.postDelayed(new Runnable() {
					@Override
					public void run() {
						onTabSelected(GoOfflineTabBar.TAB_ID_SONGS);
						onTabSelected(GoOfflineTabBar.TAB_ID_VIDOES);
						hideLoadingDialog();
					}
				}, 100);
//				onTabSelected(GoOfflineTabBar.TAB_ID_SONGS);
//				onTabSelected(GoOfflineTabBar.TAB_ID_VIDOES);
			}

			// new Handler().postDelayed(new Runnable() {
			// @Override
			// public void run() {
			// if(mPlayerBar!=null && mPlayerBar.getPlayerState()==State.IDLE){
			// DataManager mDataManager =
			// DataManager.getInstance(GoOfflineActivity.this);
			// PlayingQueue mPlayingQueue = mDataManager
			// .getStoredPlayingQueue(mDataManager.getApplicationConfigurations());
			// if (mPlayingQueue.size() > 0) {
			// mPlayerBar.playNow(mPlayingQueue.getCopy(), null, null);
			// }
			// }
			// }
			// }, 1000);
		} else if (!isOnNewIntent && !isPaused) {
			// onTabSelected(mGoOfflineTabBar.getSelectedTab());
			onTabSelected(GoOfflineTabBar.TAB_ID_SONGS);
			onTabSelected(GoOfflineTabBar.TAB_ID_VIDOES);
		}

		isPaused = false;

		if (getIntent().getBooleanExtra("show_toast", false)) {
			getIntent().removeExtra("show_toast");
			Utils.makeText(this, getString(R.string.toast_offline),
					Toast.LENGTH_SHORT).show();

			// List<MediaItem> mMediaItems = null;
			// if(CacheManager.isProUser(this))
			// mMediaItems = DBOHandler.getAllCachedTracks(this);
			// else
			// mMediaItems = DBOHandler.getAllTracksForFreeUser(this);
			// if (mMediaItems!=null && mMediaItems.size() > 0) {
			// final List<Track> tracks = new ArrayList<Track>();
			// for(MediaItem mediaItem:mMediaItems){
			// Track track = new Track(mediaItem.getId(),
			// mediaItem.getTitle(),
			// mediaItem.getAlbumName(),
			// mediaItem.getArtistName(),
			// mediaItem.getImageUrl(),
			// mediaItem.getBigImageUrl());
			// tracks.add(track);
			// }
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					// if(tracks!=null && tracks.size()>0)
					// mPlayerBar.playNow(tracks, null, null);
					if (mPlayerBarFragment != null)
						mPlayerBarFragment.updateTrackForOfflinePlay();
				}
			}, 1000);
			// }
		}

		// onTabSelected(mGoOfflineTabBar.getSelectedTab());

		try {
			mPlayerBarFragment.setDrawerPanelHeight();
		} catch (Exception e) {
		}

		// TelephonyManager telephonyManager = null;
		// telephonyManager = (TelephonyManager)
		// getSystemService(TELEPHONY_SERVICE);
		// String simSerialNo = telephonyManager.getSimSerialNumber();
		//
		// String deviceId = Secure.getString(this.getContentResolver(),
		// Secure.ANDROID_ID);
		// Toast.makeText(this, "ANDROID_ID : " + deviceId + "\nSerial : " +
		// android.os.Build.SERIAL +
		// "\nsimSerialNo : " + simSerialNo, Toast.LENGTH_LONG).show();

		if (getIntent().getBooleanExtra("open_upgrade_popup", false)) {
			getIntent().removeExtra("open_upgrade_popup");
			Intent intent = new Intent(this, UpgradeActivity.class);
			intent.putExtra(UpgradeActivity.IS_TRIAL_PLANS, true);
			intent.putExtra(UpgradeActivity.EXTRA_IS_GO_OFFLINE, true);
			intent.putExtra(UpgradeActivity.EXTRA_IS_FROM_NO_INTERNET_PROMT,
					false);
			startActivityForResult(intent,
					HomeActivity.UPGRADE_ACTIVITY_RESULT_CODE);
		}

		showTrialOfferExpiredPopup();
	}

	private void showTrialOfferExpiredPopup() {
		if (CacheManager.isTrialOfferExpired(this)) {
			AppPromptOfflineCachingTrialExpired prompt9 = new AppPromptOfflineCachingTrialExpired(
					this);
			prompt9.appLaunched(true);
		}
	}

	private boolean isPaused = false;

	@Override
	protected void onPause() {
		HungamaApplication.activityPaused();
		// mMenu.findItem(R.id.menu_item_main_actionbar_offline_options).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		isPaused = true;
		super.onPause();
	}

	private boolean isOnNewIntent = false;

	@Override
	protected void onNewIntent(Intent intent) {
		isOnNewIntent = true;
		skipResume = true;
		if (intent.getBooleanExtra("finish_all", false)) {
			if (intent.getBooleanExtra("isFromPush", false)) {
				Intent i1 = new Intent(this, HomeActivity.class);
				i1.putExtra("show_toast", true);
				startActivity(i1);

				Intent i = new Intent(this, AlertActivity.class);
				intent.getExtras().putBoolean("isAppOpen", false);
				i.putExtras(intent.getExtras());
				// i.putExtra(AlertActivity.IS_CUSTOM_RICHPUSH_LINK, true);
				startActivity(i);
			} else if (intent.getBooleanExtra(IS_FROM_PLAYER_QUEUE, false)) {
				Intent i = new Intent(this, HomeActivity.class);
				i.putExtra(IS_FROM_PLAYER_QUEUE,
						intent.getBooleanExtra(IS_FROM_PLAYER_QUEUE, false));
				i.putExtra(PLAY_FROM_POSITION,
						intent.getIntExtra(PLAY_FROM_POSITION, 0));
				i.putExtra(PLAYER_QUEUE_ACTION,
						intent.getIntExtra(PLAYER_QUEUE_ACTION, 0));
				i.putExtra("show_toast", true);
				startActivity(i);
			} else if (intent.getBooleanExtra(IS_FROM_PLAYER_BAR, false)) {
				Intent i = new Intent(this, HomeActivity.class);
				i.putExtra(IS_FROM_PLAYER_BAR,
						intent.getBooleanExtra(IS_FROM_PLAYER_BAR, false));
				i.putExtra(PLAYER_BAR_ACTION,
						intent.getIntExtra(PLAYER_BAR_ACTION, 0));
				i.putExtra("show_toast", true);
				startActivity(i);
			} else {
				Intent i = new Intent(this, HomeActivity.class);
				// overridePendingTransition(R.anim.fadein, R.anim.fadeout);
				i.putExtra(SELECTED_GLOBAL_MENU_ID,
						intent.getStringExtra(SELECTED_GLOBAL_MENU_ID));

				i.putExtra(SELECTED_GLOBAL_MENU_HTML_URL,
						intent.getStringExtra(SELECTED_GLOBAL_MENU_HTML_URL));
				i.putExtra(SELECTED_GLOBAL_MENU_ID_POPUP_MSG, intent
						.getStringExtra(SELECTED_GLOBAL_MENU_ID_POPUP_MSG));
				i.putExtra(SELECTED_GLOBAL_MENU_LINK_TYPE,
						intent.getStringExtra(SELECTED_GLOBAL_MENU_LINK_TYPE));
				i.putExtra(SELECTED_SEARCH_OPTION,
						intent.getBooleanExtra(SELECTED_SEARCH_OPTION, false));
				i.putExtra("plan_clicked",
						intent.getSerializableExtra("plan_clicked"));
				i.putExtra("show_toast", true);
				startActivity(i);
			}
			finish();
			return;
		} else if (intent.getBooleanExtra("finish_app", false)) {
			// Intent i = new Intent(this, GoOfflineActivity.class);
			// i.putExtra("show_toast", true);
			// startActivity(i);
			closeApp();
			return;
		}
		super.onNewIntent(intent);
	}

	// @Override
	// protected void onResume() {
	// super.onResume();
	// if(mPlayerBar!=null)
	// mPlayerBar.updateNotificationForOffflineMode();
	// }

	@Override
	public void finish() {
		try {
			if (mSwipeListVideo != null)
				mSwipeListVideo.discardUndo();
		} catch (Exception e) {
		} catch (Error e) {
		}
		try {
			if (mSwipeListMusic != null)
				mSwipeListMusic.discardUndo();
		} catch (Exception e) {
		} catch (Error e) {
		}
		super.finish();
	}
	
	@Override
	protected void onDestroy() {
		try {
			if (offlineModeReceiver != null)
				unregisterReceiver(offlineModeReceiver);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

		try {
			if (cacheDeleteReceiver != null)
				unregisterReceiver(cacheDeleteReceiver);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

		try {
			if (closeAppReceiver != null)
				unregisterReceiver(closeAppReceiver);
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
			if (playerStateReceiver != null) {
				unregisterReceiver(playerStateReceiver);
			}
		} catch (Exception e) {
		}
		try {
			PlayerService.service.unregisterPlayerStateListener(this);
		} catch (Exception e) {
		}

		try {
			PlayerServiceBindingManager.unbindFromService(mServiceToken);
		} catch (Exception e) {
		}

		if(mApplicationConfigurations.getSaveOfflineMode() && ApsalarEvent.ENABLED){
			Apsalar.unregisterApsalarReceiver();
			Apsalar.endSession();
		}

		closeAppReceiver = null;
		offlineModeReceiver = null;
		cacheDeleteReceiver = null;
		playerStateReceiver = null;
		Instance = null;
		super.onDestroy();
	}

	@Override
	protected NavigationItem getNavigationItem() {
		return null;
	}

	@Override
	public void onTabSelected(int tabId) {
		DBOHandler.checkTracksAvailability(this);
		DBOHandler.checkVideoTracksAvailability(this);

		if (tabId == GoOfflineTabBar.TAB_ID_SONGS) {
			mMediaItemsMusic = new ArrayList<MediaItem>();
			// findViewById(R.id.offline_tab_bar_options1).setVisibility(
			// View.VISIBLE);
			((LinearLayout) findViewById(R.id.go_offline_options))
					.setVisibility(View.GONE);
			// ((LinearLayout) findViewById(R.id.ll_go_offline_play_all))
			// .setVisibility(View.VISIBLE);
			// findViewById(R.id.go_offline_play_all).setVisibility(View.VISIBLE);
			 if (CacheManager.isProUser(this))
			 	mMediaItemsMusic = DBOHandler.getAllCachedTracks(this);
			 else
			 // mMediaItems = DBOHandler.getAllTracksForFreeUser(this);
				 mMediaItemsMusic = DBOHandler
					 .getAllOfflineTracksForFreeUser(this);
			/*if (CacheManager.isProUser(this))
				mMediaItemsMusic = DBOHandler.getAllTracks(this);
			else
				// mMediaItems = DBOHandler.getAllTracksForFreeUser(this);
				mMediaItemsMusic = DBOHandler
						.getAllOfflineTracksForFreeUser(this);*/
			filterTracksList();

			if (mMediaItemsMusic.size() == 0) {
				if (pager.getCurrentItem() == TAB_SONGS)
					displayNoDataDialog();
				findViewById(R.id.divider_top).setVisibility(View.VISIBLE);
				findViewById(R.id.divider_bottom).setVisibility(View.GONE);
			} else {
				findViewById(R.id.divider_top).setVisibility(View.VISIBLE);
				findViewById(R.id.divider_bottom).setVisibility(View.VISIBLE);
				// mOfflineAdapter = new GoOfflineTrackAdapter();
				// mListView.setAdapter(mOfflineAdapter);
				if (!isSortByLatest && !isSortByAlbum) {
					Collections.sort(mMediaItemsMusic,
							new Comparator<MediaItem>() {
								public int compare(MediaItem a, MediaItem b) {
									try {
										return a.getTitle()
												.compareToIgnoreCase(
														b.getTitle());
									} catch (Exception e) {
										Logger.printStackTrace(e);
									}
									return 0;
								}
							});
				} else if (isSortByAlbum) {
					Collections.sort(mMediaItemsMusic,
							new Comparator<MediaItem>() {
								public int compare(MediaItem a, MediaItem b) {
									try {
										return a.getAlbumName()
												.compareToIgnoreCase(
														b.getAlbumName());
									} catch (Exception e) {
										Logger.printStackTrace(e);
									}
									return 0;
								}
							});
					notifyAdapters();
				}
			}
			if (!CacheManager.isProUser(this) && footerViewMusic != null
					&& listMusic.getFooterViewsCount() == 0) {
				listMusic.addFooterView(footerViewMusic);
				// footerView.setVisibility(View.VISIBLE);
			}// else if (footerViewMusic != null) {
				// listMusic.removeFooterView(footerViewMusic);
			// footerView.setVisibility(View.GONE);
			// }
			if (mMediaItemsMusic != null && mMediaItemsMusic.size() > 0) {
				mOfflineAdapterMusic = new GoOfflineTrackAdapter(
						mMediaItemsMusic);
				// mListView.setAdapter(mOfflineAdapter);
				listMusic.setAdapter(mOfflineAdapterMusic);
			} else {
				String message = getString(
						R.string.txt_no_search_result_alert_msg,
						getString(R.string.search_results_layout_bottom_text_for_track));
				DataNotFoundListAdapter adapter = new DataNotFoundListAdapter(
						message);
				listMusic.setAdapter(adapter);
			}

			if (pager.getCurrentItem() == TAB_SONGS) {
				mMediaItems = mMediaItemsMusic;
				if (mGoOfflineTabBar.getSelectedTab() == tabId) {
					// setUpSwipeView(listMusic);
					mOfflineAdapter = mOfflineAdapterMusic;
				}
				mTrackRemoveStateMusic = new ArrayList<Boolean>();
				initTrackRemoveState();
			}
			// setUpSwipeView(listMusic);
//			mGoOfflineTabBar.updateTabCount(GoOfflineTabBar.TAB_ID_SONGS,
//					mMediaItemsMusic.size());
			setTabCount();
			resetOptionsButton();
			/*
			 * ((LanguageTextView) findViewById(R.id.offline_textview_sorttype))
			 * .setText(Utils.getMultilanguageTextLayOut(
			 * getApplicationContext(), getResources().getString(
			 * R.string.go_offline_activity_latest)));
			 */

//			if (CacheManager.isProUser(this))
//				mGoOfflineTabBar.updateTabCount(GoOfflineTabBar.TAB_ID_VIDOES,
//						DBOHandler.getAllVideoTracks(this).size());
//			else {
//				mGoOfflineTabBar.updateTabCount(GoOfflineTabBar.TAB_ID_VIDOES,
//						0);
//				// System.out.println("footerview ::::::::::::::::::::::: " +
//				// mListView.getFooterViewsCount());
//				// if(footerView!=null && mListView.getFooterViewsCount()==0)
//				// mListView.addFooterView(footerView);
//			}

//			if (mMenu != null) {
//				mMenu.clear();
//				MenuInflater inflater = getMenuInflater();
//				if (mApplicationConfigurations.getSaveOfflineMode())
//					inflater.inflate(R.menu.menu_main_offline_music_actionbar,
//							mMenu);
//				else
//					inflater.inflate(
//							R.menu.menu_main_offline_music_without_airoplan_actionbar,
//							mMenu);
//			}
		} else if (tabId == GoOfflineTabBar.TAB_ID_VIDOES) {
			mMediaItemsVideo = new ArrayList<MediaItem>();
			findViewById(R.id.offline_tab_bar_options1)
					.setVisibility(View.GONE);
			((LinearLayout) findViewById(R.id.go_offline_options))
					.setVisibility(View.GONE);
			// ((LinearLayout) findViewById(R.id.ll_go_offline_play_all))
			// .setVisibility(View.GONE);

			if (CacheManager.isProUser(this))
				mMediaItemsVideo = DBOHandler.getAllVideoTracks(this);
			else
				mMediaItemsVideo = new ArrayList<MediaItem>();
			filterVideosList();

			if (mMediaItemsVideo.size() == 0) {
				// displayNoDataDialog();
				if (pager.getCurrentItem() == TAB_VIDEOS)
					displayNoDataDialog();
				findViewById(R.id.divider_top).setVisibility(View.GONE);
				findViewById(R.id.divider_bottom).setVisibility(View.GONE);
			} else {
				findViewById(R.id.divider_top).setVisibility(View.GONE);
				findViewById(R.id.divider_bottom).setVisibility(View.VISIBLE);
				// mOfflineAdapter = new GoOfflineTrackAdapter();
				// mListView.setAdapter(mOfflineAdapter);
				if (!isSortByLatest && !isSortByAlbum) {
					Collections.sort(mMediaItemsVideo,
							new Comparator<MediaItem>() {
								public int compare(MediaItem a, MediaItem b) {
									try {
										return a.getTitle()
												.compareToIgnoreCase(
														b.getTitle());
									} catch (Exception e) {
										Logger.printStackTrace(e);
									}
									return 0;
								}
							});
				} else if (isSortByAlbum) {
					Collections.sort(mMediaItemsVideo,
							new Comparator<MediaItem>() {
								public int compare(MediaItem a, MediaItem b) {
									try {
										return a.getAlbumName()
												.compareToIgnoreCase(
														b.getAlbumName());
									} catch (Exception e) {
										Logger.printStackTrace(e);
									}
									return 0;
								}
							});
					notifyAdapters();
				}
			}
			if (!CacheManager.isProUser(this) && footerViewVideo != null
					&& listVideo.getFooterViewsCount() == 0) {
				listVideo.addFooterView(footerViewVideo);
				// footerView.setVisibility(View.VISIBLE);
			}// else if (footerViewVideo != null) {
				// listVideo.removeFooterView(footerViewVideo);
			// footerView.setVisibility(View.GONE);
			// }
			if (mMediaItemsVideo != null && mMediaItemsVideo.size() > 0) {
				mOfflineAdapterVideo = new GoOfflineTrackAdapter(
						mMediaItemsVideo);
				listVideo.setAdapter(mOfflineAdapterVideo);
				// if(mGoOfflineTabBar.getSelectedTab()==tabId)
				// setUpSwipeView(listVideo);
			} else {
				String message = getString(
						R.string.txt_no_search_result_alert_msg,
						getString(R.string.search_results_layout_bottom_text_for_video));
				DataNotFoundListAdapter adapter = new DataNotFoundListAdapter(
						message);
				listVideo.setAdapter(adapter);
			}

			if (pager.getCurrentItem() == TAB_VIDEOS) {
				mMediaItems = mMediaItemsVideo;
				if (mGoOfflineTabBar.getSelectedTab() == tabId) {
					// setUpSwipeView(listVideo);
					mOfflineAdapter = mOfflineAdapterVideo;
				}
				mTrackRemoveStateVideo = new ArrayList<Boolean>();
				initTrackRemoveState();
			}

			// setUpSwipeView(listVideo);
//			mGoOfflineTabBar.updateTabCount(GoOfflineTabBar.TAB_ID_VIDOES,
//					mMediaItemsVideo.size());
			setTabCount();
			resetOptionsButton();
			/*
			 * ((LanguageTextView) findViewById(R.id.offline_textview_sorttype))
			 * .setText(Utils.getMultilanguageTextLayOut(
			 * getApplicationContext(), getResources().getString(
			 * R.string.go_offline_activity_latest)));
			 */

//			if (CacheManager.isProUser(this))
//				mGoOfflineTabBar.updateTabCount(GoOfflineTabBar.TAB_ID_SONGS,
//						DBOHandler.getAllTracks(this).size());
//			else {
//				mGoOfflineTabBar.updateTabCount(GoOfflineTabBar.TAB_ID_SONGS,
//				// DBOHandler.getAllTracksForFreeUser(this).size());
//						DBOHandler.getAllOfflineTracksForFreeUser(this).size());
//				// if(footerView!=null)
//				// mListView.removeFooterView(footerView);
//			}

//			if (mMenu != null) {
//				mMenu.clear();
//				MenuInflater inflater = getMenuInflater();
//				if (mApplicationConfigurations.getSaveOfflineMode())
//					inflater.inflate(R.menu.menu_main_offline_actionbar, mMenu);
//				else
//					inflater.inflate(
//							R.menu.menu_main_offline_without_airoplan_actionbar,
//							mMenu);
//			}
		}/*
		 * else if(tabId == GoOfflineTabBar.TAB_ID_OPTIONS){ ((LinearLayout)
		 * findViewById(R.id.go_offline_options)).setVisibility(View.VISIBLE); }
		 */
		setOptionsItemsVisibility();
		// initTrackRemoveState();
		showOfflineMusicHelp();

		if (mMenu != null) {
			mMenu.clear();
			MenuInflater inflater = getMenuInflater();
			if (mApplicationConfigurations.getSaveOfflineMode()) {
				if (pager.getCurrentItem() == TAB_SONGS) {
					inflater.inflate(R.menu.menu_main_offline_music_actionbar, mMenu);
				} else if (pager.getCurrentItem() == TAB_VIDEOS) {
					inflater.inflate(R.menu.menu_main_offline_actionbar, mMenu);
				}
			} else if (!mApplicationConfigurations.getSaveOfflineMode()) {
				if (pager.getCurrentItem() == TAB_SONGS) {
					inflater.inflate(
							R.menu.menu_main_offline_music_without_airoplan_actionbar,
							mMenu);
				} else if (pager.getCurrentItem() == TAB_VIDEOS) {
					inflater.inflate(
							R.menu.menu_main_offline_without_airoplan_actionbar,
							mMenu);
				}
			}
		}
	}

	@Override
	public void onTabReselected(int tabId) {
		// if(tabId == GoOfflineTabBar.TAB_ID_OPTIONS){
		// ((LinearLayout)
		// findViewById(R.id.go_offline_options)).setVisibility(View.GONE);
		// }
	}

	private static class ViewHolder1 {
		ImageView ivAlbumImage;
		TextView tvTrackTitle;
		ImageView ivTrackType;
		TextView tvTrackTypeAndName;
		RelativeLayout rlRow;
		ImageButton buttonPlay;
		// ImageButton buttonSaveOffline;
		ImageButton buttonDelete;
		LinearLayout ivDragHandle;
		ImageButton buttonMore;
		CustomCacheStateProgressBar progressCacheState;
		View viewDisabled;
		ProgressBar player_queu_loading_indicator_handle;
	}

	private class GoOfflineTrackAdapter extends BaseAdapter implements
			OnClickListener/* , OnLongClickListener */{
		private LayoutInflater inflater;
		private List<MediaItem> mMediaItems;

		public GoOfflineTrackAdapter(List<MediaItem> mMediaItems) {
			inflater = (LayoutInflater) getApplicationContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.mMediaItems = mMediaItems;
		}

		public void setMediaItems(List<MediaItem> mMediaItems) {
			this.mMediaItems = mMediaItems;
		}

		@Override
		public int getCount() {
			if (mMediaItems != null && mMediaItems.size() > 0)
				return mMediaItems.size();
			else
				return 0;
		}

		@Override
		public Object getItem(int position) {
			return mMediaItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return mMediaItems.get(position).getId();
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			final ViewHolder1 viewHolder;
			// create view if not exist.
			if (convertView == null) {
				try {
					convertView = inflater
							.inflate(R.layout.list_item_player_queue_line,
									parent, false);
				} catch (Error e) {
					System.gc();
					System.runFinalization();
					System.gc();
					convertView = inflater
							.inflate(R.layout.list_item_player_queue_line,
									parent, false);
				}

				viewHolder = new ViewHolder1();
				viewHolder.rlRow = (RelativeLayout) convertView
						.findViewById(R.id.relativelayout_player_queue_line);
				viewHolder.buttonPlay = (ImageButton) convertView
						.findViewById(R.id.player_queue_line_button_play);

				viewHolder.tvTrackTitle = (TextView) convertView
						.findViewById(R.id.player_queue_line_top_text);
				viewHolder.ivTrackType = (ImageView) convertView
						.findViewById(R.id.player_queue_media_image_type);
				viewHolder.tvTrackTypeAndName = (TextView) convertView
						.findViewById(R.id.player_queue_text_media_type_and_name);

				// viewHolder.buttonSaveOffline = (ImageButton)
				// convertView.findViewById(R.id.player_queue_line_button_save_offline);
				// viewHolder.progressCacheState = (CustomCacheStateProgressBar)
				// convertView
				// .findViewById(R.id.player_queue_progress_cache_state);
				viewHolder.buttonDelete = (ImageButton) convertView
						.findViewById(R.id.player_queue_line_button_delete);
				viewHolder.ivDragHandle = (LinearLayout) convertView
						.findViewById(R.id.player_queue_media_drag_handle);
				viewHolder.ivAlbumImage = (ImageView) convertView
						.findViewById(R.id.player_queue_media_image);
				viewHolder.buttonMore = (ImageButton) convertView
						.findViewById(R.id.player_queue_line_button_more);

				viewHolder.player_queu_loading_indicator_handle = (ProgressBar) convertView
						.findViewById(R.id.player_queu_loading_indicator_handle);
				viewHolder.progressCacheState = (CustomCacheStateProgressBar) convertView
						.findViewById(R.id.player_queue_progress_cache_state);
				viewHolder.viewDisabled = (View) convertView
						.findViewById(R.id.view_disable);

				convertView.setTag(R.id.view_tag_view_holder, viewHolder);
			} else {
				viewHolder = (ViewHolder1) convertView
						.getTag(R.id.view_tag_view_holder);
			}

			viewHolder.buttonDelete.setVisibility(View.GONE);
			viewHolder.ivDragHandle.setVisibility(View.GONE);
			viewHolder.ivAlbumImage.setVisibility(View.VISIBLE);
			// viewHolder.buttonPlay.setVisibility(View.VISIBLE);

			convertView.setBackgroundColor(getResources().getColor(
					R.color.application_background_grey));
			// sets default the icon of the button to play.
			viewHolder.buttonPlay
					.setImageResource(R.drawable.icon_circle_play_blue_outline);
			viewHolder.buttonPlay.setSelected(false);

			// populate the view from the keywords's list.
			MediaItem mediaItem = (MediaItem) getItem(position);

			// stores the object in the view.
			convertView.setTag(R.id.view_tag_object, mediaItem);
			convertView.setTag(R.id.view_tag_position, position);

			// Set title
			try {
				if (mediaItem != null && mediaItem.getTitle() != null)
					viewHolder.tvTrackTitle.setText(mediaItem.getTitle());
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}

			// Set Image Type and Text Below title By Type
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				viewHolder.ivTrackType
						.setImageResource(R.drawable.icon_main_settings_music);

				viewHolder.tvTrackTypeAndName.setText("Song - "
						+ mediaItem.getAlbumName());
				// if(!isEditMode)
				if (mTrackRemoveStateMusic != null
						&& mTrackRemoveStateMusic.size() > position
						&& mTrackRemoveStateMusic.get(position)) {
					viewHolder.ivAlbumImage
							.setBackgroundResource(R.drawable.background_player_queue_album_right_mark);
					viewHolder.ivAlbumImage.setImageBitmap(null);

				} else {
					setNotPlaylistResultImage(viewHolder, mediaItem, position);
				}

			} else if (mediaItem.getMediaType() == MediaType.VIDEO) {
				viewHolder.ivTrackType
						.setImageResource(R.drawable.icon_main_settings_videos);
				viewHolder.tvTrackTypeAndName.setText("Video - "
						+ String.valueOf(mediaItem.getAlbumName()));
				if (mTrackRemoveStateVideo != null
						&& mTrackRemoveStateVideo.size() > position
						&& mTrackRemoveStateVideo.get(position)) {
					viewHolder.ivAlbumImage
							.setBackgroundResource(R.drawable.background_player_queue_album_right_mark);
					viewHolder.ivAlbumImage.setImageBitmap(null);
				} else {
					setNotPlaylistResultImage(viewHolder, mediaItem, position);
				}
			}

			viewHolder.buttonPlay.setOnClickListener(this);

			if (pager.getCurrentItem() == 0) {
				viewHolder.buttonMore.setVisibility(View.VISIBLE);
				viewHolder.buttonMore.setOnClickListener(this);
			} else {
				if (mApplicationConfigurations.getSaveOfflineMode()) {
					viewHolder.buttonMore.setVisibility(View.GONE);
					viewHolder.buttonMore.setOnClickListener(null);
				} else {
					viewHolder.buttonMore.setVisibility(View.VISIBLE);
					viewHolder.buttonMore.setOnClickListener(this);
				}
			}

			viewHolder.ivAlbumImage.setTag(R.id.view_tag_position, position);
			viewHolder.ivAlbumImage.setOnClickListener(this);
			int pos = -1;
			if (PlayerService.service != null) {
				pos = PlayerService.service.getCurrentQueuePosition();
			}
			viewHolder.buttonPlay.setVisibility(View.GONE);
			if (PlayerService.service != null
					&& (pos != -1 || (loadingTrack != -1 && loadingTrack == position))) {
				Track currentPlayingTrack = PlayerService.service
						.getCurrentPlayingTrack();
				if (currentPlayingTrack != null)
					Logger.i("ID", "Track Id: Current Playing: "
							+ currentPlayingTrack.getId());
				if (currentPlayingTrack != null
						&& currentPlayingTrack.getId() == mediaItem.getId()) {

					convertView.setBackgroundColor(getResources().getColor(
							R.color.player_queue_now_playing_background));
					if (PlayerService.service.getState() == State.PLAYING) {// xtpl
						// // playing, shows the pause button.
						viewHolder.player_queu_loading_indicator_handle
								.setVisibility(View.GONE);
						viewHolder.buttonPlay
								.setImageResource(R.drawable.icon_circle_pause_blue_outline);
						viewHolder.buttonPlay.setSelected(true);
						viewHolder.buttonPlay.setVisibility(View.VISIBLE);
					} else {
						// pausing, shows the play button.
						if (PlayerService.service.getState() == State.INTIALIZED) {
							viewHolder.player_queu_loading_indicator_handle
									.setVisibility(View.VISIBLE);
							viewHolder.buttonPlay.setVisibility(View.GONE);
						} else/*
							 * if (PlayerService.service.getState() ==
							 * State.PAUSED)
							 */{
							viewHolder.player_queu_loading_indicator_handle
									.setVisibility(View.GONE);
							viewHolder.buttonPlay.setVisibility(View.VISIBLE);
						}/*
						 * else if(!isFromReciver){
						 * viewHolder.player_queu_loading_indicator_handle
						 * .setVisibility(View.VISIBLE);
						 * viewHolder.buttonPlay.setVisibility(View.GONE); }
						 * if(isFromReciver) isFromReciver=false;
						 */

						viewHolder.buttonPlay
								.setImageResource(R.drawable.icon_circle_play_blue_outline);
						viewHolder.buttonPlay.setSelected(false);

					}
					// viewHolder.buttonPlay
					// .setImageResource(R.drawable.icon_circle_play_blue_outline);
				} else {
					viewHolder.player_queu_loading_indicator_handle
							.setVisibility(View.GONE);
					// viewHolder.buttonPlay.setVisibility(View.VISIBLE);
					List<Boolean> mTrackRemoveState;
					if (mGoOfflineTabBar.getSelectedTab() == GoOfflineTabBar.TAB_ID_SONGS)
						mTrackRemoveState = mTrackRemoveStateMusic;
					else
						mTrackRemoveState = mTrackRemoveStateVideo;
					try {
						if (mTrackRemoveState != null
								&& mTrackRemoveState.size() > position
								&& mTrackRemoveState.get(position)) {
							convertView
									.setBackgroundColor(getResources()
											.getColor(
													R.color.player_queue_selected_background));
						} else {
							convertView.setBackgroundColor(getResources()
									.getColor(R.color.transparent));
						}
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
					// sets default the icon of the button to play.
					viewHolder.buttonPlay
							.setImageResource(R.drawable.icon_circle_play_blue_outline);
					// viewHolder.player_queu_loading_indicator_handle.setVisibility(View.VISIBLE);
					// viewHolder.buttonPlay.setVisibility(View.GONE);
					viewHolder.buttonPlay.setSelected(false);
				}
			} else {
				viewHolder.player_queu_loading_indicator_handle
						.setVisibility(View.GONE);
				// viewHolder.buttonPlay.setVisibility(View.VISIBLE);
				try {
					List<Boolean> mTrackRemoveState;
					if (mGoOfflineTabBar.getSelectedTab() == GoOfflineTabBar.TAB_ID_SONGS)
						mTrackRemoveState = mTrackRemoveStateMusic;
					else
						mTrackRemoveState = mTrackRemoveStateVideo;
					if (mTrackRemoveState != null
							&& mTrackRemoveState.size() > position
							&& mTrackRemoveState.get(position)) {
						convertView.setBackgroundColor(getResources().getColor(
								R.color.player_queue_selected_background));
					} else {
						convertView.setBackgroundColor(getResources().getColor(
								R.color.transparent));
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
				// sets default the icon of the button to play.
				viewHolder.buttonPlay
						.setImageResource(R.drawable.icon_circle_play_blue_outline);
				viewHolder.player_queu_loading_indicator_handle
						.setVisibility(View.GONE);
				// viewHolder.buttonPlay.setVisibility(View.GONE);
				viewHolder.buttonPlay.setSelected(false);
			}

			CacheState cacheState;
			if (mediaItem.getMediaType() == MediaType.VIDEO)
				cacheState = DBOHandler.getVideoTrackCacheState(
						GoOfflineActivity.this.getApplicationContext(), ""
								+ mediaItem.getId());
			else
				cacheState = DBOHandler.getTrackCacheState(
						GoOfflineActivity.this.getApplicationContext(), ""
								+ mediaItem.getId());
			viewHolder.progressCacheState.showProgressOnly(true);
			viewHolder.progressCacheState.setCacheState(cacheState);

			// if (/*mApplicationConfigurations.getSaveOfflineMode()
			// && */cacheState != CacheState.CACHED) {
			// viewHolder.rlRow.setEnabled(false);
			// viewHolder.viewDisabled.setVisibility(View.VISIBLE);
			// viewHolder.viewDisabled.setOnClickListener(this);
			// } else {
			// viewHolder.rlRow.setEnabled(true);
			// viewHolder.viewDisabled.setVisibility(View.GONE);
			// }I76ROO9CPE
			if (cacheState == CacheState.NOT_CACHED
					|| cacheState == CacheState.FAILED) {
				viewHolder.rlRow.setEnabled(false);
				viewHolder.viewDisabled.setVisibility(View.VISIBLE);
				viewHolder.viewDisabled.setOnClickListener(this);
			} else {
				viewHolder.rlRow.setEnabled(true);
				viewHolder.viewDisabled.setVisibility(View.GONE);
			}

			return convertView;
		}

		@Override
		public void onClick(final View view) {
			int viewId = view.getId();

			// a tile was clicked, shows its media item's details.
			if (viewId == R.id.relativelayout_player_queue_line) {

				RelativeLayout tile = (RelativeLayout) view;// .getParent();

				ViewHolder1 viewHolder = (ViewHolder1) tile
						.getTag(R.id.view_tag_view_holder);
				int position = (Integer) tile.getTag(R.id.view_tag_position);

				handlePlayClick(position, viewHolder, true);

				// play now was selected.
			} else if (viewId == R.id.player_queue_line_button_play) {
				// play or pause
				RelativeLayout tile = (RelativeLayout) view.getParent()
						.getParent().getParent();

				ViewHolder1 viewHolder = (ViewHolder1) tile
						.getTag(R.id.view_tag_view_holder);
				int position = (Integer) tile.getTag(R.id.view_tag_position);

				handlePlayClick(position, viewHolder, true);

				// remove tile was selected.
			} else if (viewId == R.id.player_queue_line_button_delete) {

				// FlurryAgent.logEvent(FlurryConstants.FlurryAllPlayer.DeleteSong.toString());
				// xtpl
				Map<String, String> reportMap = new HashMap<String, String>();
				reportMap.put(
						FlurryConstants.FlurryMediaDetailActions.ActionTaken
								.toString(),
						FlurryConstants.FlurryAllPlayer.DeleteSong.toString());
				Analytics.logEvent(
						FlurryConstants.FlurryAllPlayer.PlayerQueueViewed
								.toString(), reportMap);
				// xtpl

				// remove track from queue
				RelativeLayout tile = (RelativeLayout) view.getParent()
						.getParent().getParent();

				int position = (Integer) tile.getTag(R.id.view_tag_position);

				// removes the original from the player.
				mPlayerBarFragment.removeFrom(position);

				// updates the current list.
				// mTracks = mPlayerBar.getCurrentPlayingList();

				notifyDataSetChanged();
				setTitleText(false);
				// updates the title.
				// updates the text of the title
				// String title = getResources().getString(
				// R.string.player_queue_title, mTracks.size());
				// mTextTitle.setText(title);
			} else if (viewId == R.id.player_queue_media_image) {
				new FlipAnimationListener((ImageView) view);

			} else if (viewId == R.id.player_queue_line_button_more) {
				// play or pause
				RelativeLayout tile = (RelativeLayout) view.getParent()
						.getParent().getParent();

				// ViewHolder1 viewHolder = (ViewHolder1) tile
				// .getTag(R.id.view_tag_view_holder);
				// // Track track = (Track) tile.getTag(R.id.view_tag_object);
				// MediaItem mediaItem = (MediaItem) tile
				// .getTag(R.id.view_tag_object);
				int position = (Integer) tile.getTag(R.id.view_tag_position);

				QuickActionOfflineSongMenu quickAction = new QuickActionOfflineSongMenu(
						GoOfflineActivity.this, GoOfflineActivity.this,
						(pager.getCurrentItem() == 0) ? true : false, position);
				quickAction.show(view);
				view.setEnabled(false);
				quickAction.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss() {
						view.setEnabled(true);
					}
				});
				// showMediaItemOptionsDialog(mediaItem, position, viewHolder);
			} else if (viewId == R.id.view_disable) {
				if(CacheManager.isProUser(GoOfflineActivity.this))
					return;
				CustomAlertDialog alertBuilder = new CustomAlertDialog(
						GoOfflineActivity.this);
				if (!CacheManager.isProUser(GoOfflineActivity.this)
						&& mApplicationConfigurations
								.isUserTrialSubscriptionExpired()) {
					alertBuilder
							.setMessage(Utils
									.getMultilanguageTextHindi(
											getApplicationContext(),
											getResources()
													.getString(
															R.string.message_offline_trial_period_expired)));
				} else {
					alertBuilder.setMessage(Utils.getMultilanguageTextHindi(
							getApplicationContext(),
							getResources().getString(
									R.string.message_offline_free_user)));
				}
				alertBuilder.setPositiveButton(Utils.getMultilanguageTextHindi(
						getApplicationContext(),
						getString(R.string.global_menu_button_upgrade_to_pro)),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								upgradeToProNow();
							}
						});
				alertBuilder.setNegativeButton(Utils.getMultilanguageTextHindi(
						getApplicationContext(),
						getString(R.string.cancel_button_text)), null);
				// alertBuilder.create();
				alertBuilder.show();
			}
		}

		// @Override
		// public boolean onLongClick(View view) {
		// int viewId = view.getId();
		// if (viewId == R.id.relativelayout_player_queue_line
		// || viewId == R.id.player_queue_line_button_play
		// /* || viewId == R.id.player_queue_line_button_save_offline */) {
		//
		// // get the item's id from the tile itself.
		// RelativeLayout tile;
		// if (viewId == R.id.player_queue_line_button_play
		// /* || viewId == R.id.player_queue_line_button_save_offline */)
		// tile = (RelativeLayout) view.getParent().getParent();
		// else
		// tile = (RelativeLayout) view;// .getParent();
		//
		// ViewHolder1 viewHolder = (ViewHolder1) tile
		// .getTag(R.id.view_tag_view_holder);
		// // Track track = (Track) tile.getTag(R.id.view_tag_object);
		// MediaItem mediaItem = (MediaItem) tile
		// .getTag(R.id.view_tag_object);
		// int position = (Integer) tile.getTag(R.id.view_tag_position);
		//
		// showMediaItemOptionsDialog(mediaItem, position, viewHolder);
		// }
		//
		// return false;
		// }

	}

	private int loadingTrack = -1;

	// private boolean isFromReciver = true;

	private void handlePlayClick(int position, ViewHolder1 viewHolder,
			boolean playNow) {
		try {
			MediaItem mediaItem = mMediaItems.get(position);
			Logger.s("handlePlayClick :::::::: " + position + " :::::: "
					+ mediaItem.getMediaType());
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				Track currentPlayingTrack = PlayerService.service
						.getCurrentPlayingTrack();
				if (currentPlayingTrack != null
						&& currentPlayingTrack.getId() == mediaItem.getId()) {
					if (PlayerService.service.isPlaying()) {
						// checks if the current tile is in the state of play or
						// pause.
						if (viewHolder.buttonPlay.isSelected()) {
							// currently is playing, pauses and shows the "play"
							// button.
							PlayerService.service.pause();
							// sets the button's icon and state.
							// viewHolder.buttonPlay
							// .setImageResource(R.drawable.icon_circle_play_blue_outline);
							// viewHolder.buttonPlay.setSelected(false);
						} else {
							PlayerService.service.play();
							// viewHolder.buttonPlay
							// .setImageResource(R.drawable.icon_circle_pause_blue_outline);
							// viewHolder.buttonPlay.setSelected(true);
						}
						sendBroadcast(new Intent(
								PlayerBarFragment.ACTION_PLAY_STATE_CHANGED));
					}
				} else {
					Track track = new Track(mediaItem.getId(),
							mediaItem.getTitle(), mediaItem.getAlbumName(),
							mediaItem.getArtistName(), mediaItem.getImageUrl(),
							mediaItem.getBigImageUrl(), mediaItem.getImages(),
							mediaItem.getAlbumId());
					List<Track> tracks = new ArrayList<Track>();
					tracks.add(track);
					if (playNow)
						mPlayerBarFragment.playNow(tracks, null, null);
					else
						mPlayerBarFragment.addToQueue(tracks, null, null);
					// viewHolder.buttonPlay
					// .setImageResource(R.drawable.icon_circle_pause_blue_outline);
					// viewHolder.buttonPlay.setSelected(true);
				}
			} else if (mediaItem.getMediaType() == MediaType.VIDEO) {
				Intent intent = new Intent(GoOfflineActivity.this,
						VideoActivity.class);
				if (mMediaItems != null) {
					intent.putExtra(VideoActivity.EXTRA_MEDIA_LIST_VIDEO,
							(Serializable) mMediaItems);
					intent.putExtra(VideoActivity.EXTRA_MEDIA_POS_VIDEO,
							position);
				}
				intent.putExtra(VideoActivity.EXTRA_MEDIA_ITEM_VIDEO,
						(Serializable) mediaItem);

				startActivity(intent);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	private void showMediaItemOptionsDialog(final MediaItem mediaItem,
			final int position, final ViewHolder1 viewHolder) {
		// set up custom dialog
		final Dialog mediaItemOptionsDialog = new Dialog(GoOfflineActivity.this);

		mediaItemOptionsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mediaItemOptionsDialog
				.setContentView(R.layout.dialog_media_playing_options);
		mediaItemOptionsDialog.setCancelable(true);
		mediaItemOptionsDialog.show();

		// sets the title.
		try {
			LanguageTextView title = (LanguageTextView) mediaItemOptionsDialog
					.findViewById(R.id.long_click_custom_dialog_title_text);
			title.setText(mediaItem.getTitle());
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

		// sets the cancel button.
		ImageButton closeButton = (ImageButton) mediaItemOptionsDialog
				.findViewById(R.id.long_click_custom_dialog_title_image);
		closeButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mediaItemOptionsDialog.dismiss();
			}
		});

		// sets the options buttons.
		LinearLayout llPlayNow = (LinearLayout) mediaItemOptionsDialog
				.findViewById(R.id.long_click_custom_dialog_download);
		LinearLayout llAddtoQueue = (LinearLayout) mediaItemOptionsDialog
				.findViewById(R.id.long_click_custom_dialog_add_to_queue_row);
		LinearLayout llDetails = (LinearLayout) mediaItemOptionsDialog
				.findViewById(R.id.long_click_custom_dialog_details_row);
		LinearLayout llSaveOffline = (LinearLayout) mediaItemOptionsDialog
				.findViewById(R.id.long_click_custom_dialog_save_offline_row);

		llDetails.setVisibility(View.GONE);
		llSaveOffline.setVisibility(View.GONE);
			// llSaveOffline.setTag(false);
			// CacheState cacheState;
			// if(mediaItem.getMediaType()==MediaType.TRACK)
			// cacheState =
			// DBOHandler.getTrackCacheState(getApplicationContext(), "" +
			// mediaItem.getId());
			// else
			// cacheState =
			// DBOHandler.getVideoTrackCacheState(getApplicationContext(),
			// "" + mediaItem.getId());
			// if(cacheState == CacheState.CACHED){
			// llSaveOffline.setTag(null);
			// ((TextView)
			// mediaItemOptionsDialog.findViewById(R.id.long_click_custom_dialog_save_offline_text)).setText("Play Offline");
			// ((ImageView)
			// mediaItemOptionsDialog.findViewById(R.id.long_click_custom_dialog_save_offline_image)).setImageResource(R.drawable.icon_media_details_saved);
			// } else if(cacheState == CacheState.CACHING || cacheState ==
			// CacheState.QUEUED){
			// llSaveOffline.setTag(null);
			// ((TextView)
			// mediaItemOptionsDialog.findViewById(R.id.long_click_custom_dialog_save_offline_text)).setText("Saving");
			// ((ImageView)
			// mediaItemOptionsDialog.findViewById(R.id.long_click_custom_dialog_save_offline_image)).setImageResource(R.drawable.icon_media_details_saving);
			// }
			// llSaveOffline.setVisibility(View.VISIBLE);

		// llAddtoQueue.setVisibility(View.GONE);

		// Add To Queue.
		llAddtoQueue.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// FlurryAgent.logEvent(FlurryConstants.FlurryAllPlayer.ChangeNowPlaying.toString());
				// xtpl
				Map<String, String> reportMap = new HashMap<String, String>();
				reportMap.put(
						FlurryConstants.FlurryMediaDetailActions.ActionTaken
								.toString(),
						FlurryConstants.FlurryAllPlayer.ChangeNowPlaying
								.toString());
				Analytics.logEvent(
						FlurryConstants.FlurryAllPlayer.PlayerQueueViewed
								.toString(), reportMap);
				// xtpl
				mediaItemOptionsDialog.dismiss();
				handlePlayClick(position, viewHolder, false);

				Map<String, String> reportMap1 = new HashMap<String, String>();
				reportMap1.put(FlurryConstants.FlurryKeys.OptionSelected
						.toString(),
						FlurryConstants.FlurryMediaDetailActions.Addtoqueue
								.toString());
				Analytics.logEvent(
						FlurryConstants.FlurryEventName.OfflineSongs3dots
								.toString(), reportMap1);
			}
		});

		// play now.
		llPlayNow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				// download
				MediaItem trackMediaItem = new MediaItem(mediaItem.getId(),
						mediaItem.getTitle(), mediaItem.getAlbumName(),
						mediaItem.getArtistName(), mediaItem.getImageUrl(),
						mediaItem.getBigImageUrl(), MediaType.TRACK.toString(),
						0, mediaItem.getAlbumId());
				Intent intent = new Intent(getBaseContext(),
						DownloadConnectingActivity.class);
				intent.putExtra(DownloadConnectingActivity.EXTRA_MEDIA_ITEM,
						(Serializable) trackMediaItem);
				startActivity(intent);
				mediaItemOptionsDialog.dismiss();
				// FlurryAgent.logEvent(FlurryConstants.FlurryAllPlayer.ChangeNowPlaying.toString());
				// xtpl
				// Map<String, String> reportMap = new HashMap<String,
				// String>();
				// reportMap
				// .put(FlurryConstants.FlurryMediaDetailActions.ActionTaken
				// .toString(),
				// FlurryConstants.FlurryAllPlayer.ChangeNowPlaying
				// .toString());
				// Analytics.logEvent(
				// FlurryConstants.FlurryAllPlayer.PlayerQueueViewed
				// .toString(), reportMap);
				// // xtpl
				// mediaItemOptionsDialog.dismiss();
				// handlePlayClick(position, viewHolder, true);
				//
				// Map<String, String> reportMap1 = new HashMap<String,
				// String>();
				// reportMap1
				// .put(FlurryConstants.FlurryKeys.OptionSelected
				// .toString(),
				// FlurryConstants.FlurryMediaDetailActions.PlayNow
				// .toString());
				// Analytics.logEvent(
				// FlurryConstants.FlurryEventName.OfflineSongs3dots
				// .toString(), reportMap1);
			}
		});

		// show details.
		// llDetails.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View view) {
		// mediaItemOptionsDialog.dismiss();
		// Intent intent = new Intent(getApplicationContext(),
		// MediaDetailsActivity.class);
		// intent.putExtra(MediaDetailsActivity.EXTRA_MEDIA_ITEM,
		// (Serializable) mediaItem);
		// intent.putExtra(MediaDetailsActivity.FLURRY_SOURCE_SECTION,
		// FlurryConstants.FlurrySourceSection.PlayerQueue
		// .toString());
		//
		// startActivity(intent);
		// }
		// });

		// Save Offline
		// llSaveOffline.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View view) {
		// if (view.getTag()!=null && !((Boolean) view.getTag())){
		// if(mediaItem.getMediaType()==MediaType.TRACK){
		// Track track = new Track(mediaItem.getId(),
		// mediaItem.getTitle(),
		// mediaItem.getAlbumName(),
		// mediaItem.getArtistName(),
		// mediaItem.getImageUrl(),
		// mediaItem.getBigImageUrl());
		// // new MediaCachingTask(getApplicationContext(), mediaItem,
		// track).execute();
		// CacheManager.saveOfflineAction(GoOfflineActivity.this, mediaItem,
		// track);
		// } else if(mediaItem.getMediaType()==MediaType.VIDEO){
		// // new MediaCachingTask(getApplicationContext(), mediaItem,
		// null).execute();
		// CacheManager.saveOfflineAction(GoOfflineActivity.this, mediaItem,
		// null);
		// }
		// HomeActivity.refreshOfflineState = true;
		// } else{
		// handlePlayClick(position, viewHolder);
		// }
		// mediaItemOptionsDialog.dismiss();
		// }
		// });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.offline_imagebutton_options:
			openOptionAction(v);
			// if (v.isSelected()) {
			// // v.setSelected(false);
			// resetOptionsButton();
			// } else {
			// ((LinearLayout) findViewById(R.id.go_offline_options))
			// .setVisibility(View.VISIBLE);
			// v.setSelected(true);
			// }
			break;
		// case R.id.offline_textview_sorttype:
		// findViewById(R.id.offline_imagebutton_options).performClick();
		// break;
		case R.id.go_offline_play_all:
			playAllSongs();
			break;
		case R.id.go_offline_sort_a_to_z:
			resetOptionsButton();
			// Sorting by property v using a custom comparator.
			Collections.sort(mMediaItems, new Comparator<MediaItem>() {
				public int compare(MediaItem a, MediaItem b) {
					try {
						return a.getTitle().compareToIgnoreCase(b.getTitle());
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
					return 0;
				}
			});
			// ((LanguageTextView) findViewById(R.id.offline_textview_sorttype))
			// .setText("A - Z");
			// if (mOfflineAdapter != null)
			// mOfflineAdapter.notifyDataSetChanged();
			notifyAdapters();
			findViewById(R.id.go_offline_sort_latest).setVisibility(
					View.VISIBLE);
			v.setVisibility(View.GONE);
			break;
		case R.id.go_offline_sort_latest:
			resetOptionsButton();
			if (mGoOfflineTabBar.getSelectedTab() == GoOfflineTabBar.TAB_ID_SONGS) {
				if (CacheManager.isProUser(this))
					mMediaItemsMusic = DBOHandler.getAllTracks(this);
				else
					// mMediaItems = DBOHandler.getAllTracksForFreeUser(this);
					mMediaItemsMusic = DBOHandler
							.getAllOfflineTracksForFreeUser(this);
				filterTracksList();
				mMediaItems = mMediaItemsMusic;
				// if(mMediaItems.size()==0){
				// displayNoDataDialog();
				// } else{
				initTrackRemoveState();

				if (mMediaItemsMusic != null && mMediaItemsMusic.size() > 0) {
					mOfflineAdapterMusic = new GoOfflineTrackAdapter(
							mMediaItemsMusic);
					mListView.setAdapter(mOfflineAdapterMusic);
				} else {
					String message = getString(
							R.string.txt_no_search_result_alert_msg,
							getString(R.string.search_results_layout_bottom_text_for_track));
					DataNotFoundListAdapter adapter = new DataNotFoundListAdapter(
							message);
					mListView.setAdapter(adapter);
				}

				mOfflineAdapter = mOfflineAdapterMusic;
				// }
			} else if (mGoOfflineTabBar.getSelectedTab() == GoOfflineTabBar.TAB_ID_VIDOES) {
				mMediaItemsVideo = DBOHandler.getAllVideoTracks(this);
				filterVideosList();
				mMediaItems = mMediaItemsVideo;
				initTrackRemoveState();
				// if(mMediaItems.size()==0){
				// displayNoDataDialog();
				// } else {
				if (mMediaItemsVideo != null && mMediaItemsVideo.size() > 0) {
					mOfflineAdapterVideo = new GoOfflineTrackAdapter(
							mMediaItemsVideo);
					mListView.setAdapter(mOfflineAdapterVideo);
				} else {
					String message = getString(
							R.string.txt_no_search_result_alert_msg,
							getString(R.string.search_results_layout_bottom_text_for_video));
					DataNotFoundListAdapter adapter = new DataNotFoundListAdapter(
							message);
					mListView.setAdapter(adapter);
				}

				mOfflineAdapter = mOfflineAdapterVideo;
				// }
			}
			// ((LanguageTextView) findViewById(R.id.offline_textview_sorttype))
			// .setText(Utils.getMultilanguageTextLayOut(
			// getApplicationContext(),
			// getResources().getString(
			// R.string.go_offline_activity_latest)));
			findViewById(R.id.go_offline_sort_a_to_z).setVisibility(
					View.VISIBLE);
			v.setVisibility(View.GONE);
			break;
		case R.id.go_offline_settings:
			resetOptionsButton();
			Intent intent = new Intent(this, SettingsActivity.class);
			intent.putExtra("load_save_offline", true);
			startActivity(intent);
			break;
		case R.id.layout_free_user_offline_message:
		case R.id.text_free_user_offline_message:
		case R.id.iv_go_pro_now:
			// final ApplicationConfigurations mApplicationConfiguration = new
			// ApplicationConfigurations(
			// this);
			upgradeToProNow();
			break;
		case R.id.offline_iv_select_all:
			// for (int i = 0; i < mTrackRemoveState.size(); i++)
			// mTrackRemoveState.set(i, true);
			// if (mOfflineAdapter != null)
			// mOfflineAdapter.notifyDataSetChanged();
			notifyAdapters();
			break;
		case R.id.offline_textview_cancel_selection:
			cancelClick();
			break;
		case R.id.offline_textview_delete_selected:
			clearSelectedAudios();
			break;
		// mIvSelectAll.setVisibility(View.INVISIBLE);
		case R.id.btn_help_close:
			mApplicationConfigurations.setNeedToShowSaveOfflineHelp(false);
			findViewById(R.id.offline_tab_message).setVisibility(View.GONE);
			break;
		}
	}

	private void playAllSongs() {
		resetOptionsButton();
		List<Track> tracks = new ArrayList<Track>();
		if (mMediaItemsMusic != null && mMediaItemsMusic.size() > 0) {
			for (MediaItem mediaItem : mMediaItemsMusic) {
				Track track = new Track(mediaItem.getId(),
						mediaItem.getTitle(), mediaItem.getAlbumName(),
						mediaItem.getArtistName(), mediaItem.getImageUrl(),
						mediaItem.getBigImageUrl(), mediaItem.getImages(),
						mediaItem.getAlbumId());
				tracks.add(track);
			}
		}
		if (tracks.size() > 0) {
			//mPlayerBarFragment.addToQueue(tracks, null, null);
			mPlayerBarFragment.playNowNew(tracks, null, null);
		}
		Analytics.logEvent(FlurryConstants.FlurryEventName.OfflineSongsPlayAll
				.toString());
	}

	private void upgradeToProNow() {
		if (mApplicationConfigurations.getSaveOfflineMode()) {
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
							if (Utils.isConnected()) {
								mApplicationConfigurations
										.setSaveOfflineMode(false);
								Intent i = new Intent(
										MainActivity.ACTION_OFFLINE_MODE_CHANGED);
								i.putExtra(
										SELECTED_GLOBAL_MENU_ID,
										GlobalMenuFragment.MENU_ITEM_UPGRADE_ACTION);
								sendBroadcast(i);
							} else {
								CustomAlertDialog alertBuilder = new CustomAlertDialog(
										GoOfflineActivity.this);
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
						}
					});
			alertBuilder.setNegativeButton(Utils.getMultilanguageTextLayOut(
					getApplicationContext(),
					getResources().getString(
							R.string.caching_text_popup_button_cancel)), null);
			// alertBuilder.create();
			alertBuilder.show();
		} else {
			Boolean loggedIn = mApplicationConfigurations.isRealUser();
			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(FlurryConstants.FlurrySubscription.SourcePage
							.toString(),
					FlurryConstants.FlurryKeys.OfflineMusic
							.toString());
			reportMap.put(
					FlurryConstants.FlurrySubscription.LoggedIn.toString(),
					loggedIn.toString());
			Analytics.logEvent(
					FlurryConstants.FlurrySubscription.TapsOnUpgrade
							.toString(), reportMap);

			Intent intent = new Intent(this, UpgradeActivity.class);
			intent.putExtra(UpgradeActivity.IS_TRIAL_PLANS, true);
			startActivityForResult(intent,
					HomeActivity.UPGRADE_ACTIVITY_RESULT_CODE);
		}
	}

	private void clearSelectedAudios() {
		ArrayList<Long> idsToRemove = new ArrayList<Long>();
		List<Boolean> mTrackRemoveState;
		if (mGoOfflineTabBar.getSelectedTab() == GoOfflineTabBar.TAB_ID_SONGS)
			mTrackRemoveState = mTrackRemoveStateMusic;
		else
			mTrackRemoveState = mTrackRemoveStateVideo;
		for (int i = 0; i < mTrackRemoveState.size(); i++) {
			if (mTrackRemoveState.get(i)) {
				MediaItem track = mMediaItems.get(i);
				if (track != null) {
					if (mGoOfflineTabBar.getSelectedTab() == GoOfflineTabBar.TAB_ID_SONGS) {
						String path = DBOHandler.getTrackPathById(this, ""
								+ track.getId());
						if (path != null && path.length() > 0) {
							File file = new File(path);
							if (file.exists()) {
								file.delete();
							}
						}
						boolean isTracksDeleted = DBOHandler.deleteCachedTrack(
								this, "" + track.getId());
						Logger.s(track.getTitle() + " ::::::delete:::::: "
								+ isTracksDeleted);
						if (isTracksDeleted) {
							Intent TrackCached = new Intent(
									CacheManager.ACTION_TRACK_CACHED);
							TrackCached.putExtra("is_from_offline", true);
							Logger.i("Update Cache State",
									" SENDING BROADCAST TRACK_CACHED");
							sendBroadcast(TrackCached);
						}
						idsToRemove.add(track.getId());
					} else if (mGoOfflineTabBar.getSelectedTab() == GoOfflineTabBar.TAB_ID_VIDOES) {
						String path = DBOHandler.getVideoTrackPathById(this, ""
								+ track.getId());
						if (path != null && path.length() > 0) {
							File file = new File(path);
							if (file.exists()) {
								file.delete();
							}
						}
						boolean isTracksDeleted = DBOHandler
								.deleteCachedVideoTrack(this,
										"" + track.getId());
						Logger.s(track.getTitle() + " ::::::delete:::::: "
								+ isTracksDeleted);
						if (isTracksDeleted) {
							Intent TrackCached = new Intent(
									CacheManager.ACTION_VIDEO_TRACK_CACHED);
							Logger.i("Update Cache State",
									" SENDING BROADCAST VIDEO_TRACK_CACHED");
							sendBroadcast(TrackCached);
						}
					}
					// CacheState cacheState =
					// DBOHandler.getTrackCacheState(this, "" +
					// track.getId());
					// Logger.s(track.getTitle() + " ::::::delete:::::: " +
					// cacheState);
					// if(cacheState!=CacheState.CACHED &&
					// cacheState!=CacheState.CACHING){
					// boolean isTracksDeleted =
					// DBOHandler.deleteCachedTrack(this, "" +
					// track.getId());
					// Logger.s(track.getTitle() + " ::::::delete:::::: " +
					// isTracksDeleted);
					// if(isTracksDeleted){
					// Intent TrackCached = new
					// Intent(CacheManager.ACTION_TRACK_CACHED);
					// Logger.i("Update Cache State",
					// " SENDING BROADCAST TRACK_CACHED");
					// sendBroadcast(TrackCached);
					// }
					// }
				}
				// int position = (Integer)
				// tile.getTag(R.id.view_tag_position);
				// removes the original from the player.
				mMediaItems.remove(i);
				mTrackRemoveState.remove(i);
				// mPlayerBar.removeFrom(i);
				i--;
			}
		}

		if (idsToRemove.size() > 0) {
			mPlayerBarFragment.removeTrack(idsToRemove);
		}
		// onTabSelected(mGoOfflineTabBar.getSelectedTab());
		onTabSelected(GoOfflineTabBar.TAB_ID_SONGS);
	}

	private void cancelClick() {
		List<Boolean> mTrackRemoveState;
		if (mGoOfflineTabBar.getSelectedTab() == GoOfflineTabBar.TAB_ID_SONGS)
			mTrackRemoveState = mTrackRemoveStateMusic;
		else
			mTrackRemoveState = mTrackRemoveStateVideo;
		for (int i = 0; i < mTrackRemoveState.size(); i++)
			mTrackRemoveState.set(i, false);
		// if (mOfflineAdapter != null)
		// mOfflineAdapter.notifyDataSetChanged();
		notifyAdapters();
		findViewById(R.id.offline_tab_bar_options).setVisibility(View.GONE);
		mTextRemoveHint.setVisibility(View.VISIBLE);
		setTitleText(true);
		// mTextOfflineOptions.setVisibility(View.VISIBLE);
		// mIbOfflineOptions.setVisibility(View.VISIBLE);
		// mIvSelectAll.setVisibility(View.INVISIBLE);
		mChkboxSelectAll.setVisibility(View.INVISIBLE);
		mTextCancelRemoveState.setVisibility(View.GONE);
		mTextDeleteSelected.setVisibility(View.GONE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == HomeActivity.UPGRADE_ACTIVITY_RESULT_CODE
				&& resultCode == RESULT_OK) {
			// onTabSelected(mGoOfflineTabBar.getSelectedTab());
			if (Utils.isConnected()) {
				mApplicationConfigurations.setSaveOfflineMode(false);
				Intent i = new Intent(MainActivity.ACTION_OFFLINE_MODE_CHANGED);
				i.putExtra(SELECTED_GLOBAL_MENU_ID,
						GlobalMenuFragment.MENU_ITEM_UPGRADE_ACTION);
				i.putExtra("plan_clicked",
						data.getSerializableExtra("plan_clicked"));
				sendBroadcast(i);
			}
		}
	}

	private void resetOptionsButton() {
		((LinearLayout) findViewById(R.id.go_offline_options))
				.setVisibility(View.GONE);
		// mGoOfflineTabBar.setCurrentSelected(GoOfflineTabBar.TAB_ID_OPTIONS);
		findViewById(R.id.offline_imagebutton_options).setSelected(false);
	}

	private void displayNoDataDialog() {
		if (isFinishing())
			return;
		CustomAlertDialog alertBuilder = new CustomAlertDialog(this);
		if (mGoOfflineTabBar.getSelectedTab() == GoOfflineTabBar.TAB_ID_SONGS
				&& mMediaItemsMusic.size() == 0) {
			ApplicationConfigurations mApplicationConfiguration = ApplicationConfigurations
					.getInstance(this);
			if (mApplicationConfiguration.getSaveOfflineMode()) {
				alertBuilder
						.setMessage(Utils
								.getMultilanguageTextLayOut(
										getApplicationContext(),
										getResources().getString(
												R.string.no_offline_songs_1))
								+ "\n"
								+ Utils.getMultilanguageTextLayOut(
										getApplicationContext(),
										getResources().getString(
												R.string.no_offline_songs_2)));
			} else {
				alertBuilder
						.setMessage(Utils
								.getMultilanguageTextLayOut(
										getApplicationContext(),
										getResources()
												.getString(
														R.string.no_offline_songs_for_online_1))
								+ "\n"
								+ Utils.getMultilanguageTextLayOut(
										getApplicationContext(),
										getResources()
												.getString(
														R.string.no_offline_songs_for_online_2)));
			}
		} else if (mMediaItemsVideo.size() == 0) {
			alertBuilder.setMessage(Utils.getMultilanguageTextLayOut(
					getApplicationContext(),
					getResources().getString(R.string.no_offline_videos_1))
					+ "\n"
					+ Utils.getMultilanguageTextLayOut(
							getApplicationContext(),
							getResources().getString(
									R.string.no_offline_videos_2)));
		}
		alertBuilder.setNegativeButton(
				Utils.getMultilanguageTextLayOut(getApplicationContext(),
						getResources().getString(R.string.ok)), null);
		alertBuilder.show();
	}

	private void setTabCount() {
		// if (mGoOfflineTabBar.getSelectedTab() ==
		// GoOfflineTabBar.TAB_ID_SONGS) {
		// ((TextView) findViewById(R.id.offline_textview_tab_name))
		// .setText("Songs(" + mMediaItems.size() + ")");
		// } else {
		// ((TextView) findViewById(R.id.offline_textview_tab_name))
		// .setText("Videos(" + mMediaItems.size() + ")");
		// }
	}

	class OfflineModeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			boolean offlineMode = mApplicationConfigurations
					.getSaveOfflineMode();
			if (!offlineMode) {
				// if(mPlayerBar!=null){
				// mPlayerBar.clearQueue();
				//
				// PlayingQueue mPlayingQueue =
				// DataManager.getInstance(GoOfflineActivity.this)
				// .getStoredPlayingQueue(new
				// ApplicationConfigurations(GoOfflineActivity.this));
				// if (mPlayingQueue.size() > 0) {
				// mPlayerBar.addToQueue(mPlayingQueue.getCopy(), null, null);
				// }
				// }
				try {
					findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
				} catch (Exception e) {
				}

				Intent i = new Intent(GoOfflineActivity.this,
						GoOfflineActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				i.putExtra(SELECTED_GLOBAL_MENU_ID,
						arg1.getStringExtra(SELECTED_GLOBAL_MENU_ID));
				i.putExtra(SELECTED_GLOBAL_MENU_HTML_URL,
						arg1.getStringExtra(SELECTED_GLOBAL_MENU_HTML_URL));
				i.putExtra(SELECTED_GLOBAL_MENU_ID_POPUP_MSG,
						arg1.getStringExtra(SELECTED_GLOBAL_MENU_ID_POPUP_MSG));
				i.putExtra(SELECTED_GLOBAL_MENU_LINK_TYPE,
						arg1.getStringExtra(SELECTED_GLOBAL_MENU_LINK_TYPE));

				i.putExtra(SELECTED_SEARCH_OPTION,
						arg1.getBooleanExtra(SELECTED_SEARCH_OPTION, false));
				i.putExtra(IS_FROM_PLAYER_QUEUE,
						arg1.getBooleanExtra(IS_FROM_PLAYER_QUEUE, false));
				i.putExtra(PLAY_FROM_POSITION,
						arg1.getIntExtra(PLAY_FROM_POSITION, 0));
				i.putExtra(PLAYER_QUEUE_ACTION,
						arg1.getIntExtra(PLAYER_QUEUE_ACTION, 0));
				i.putExtra(IS_FROM_PLAYER_BAR,
						arg1.getBooleanExtra(IS_FROM_PLAYER_BAR, false));
				i.putExtra(PLAYER_BAR_ACTION,
						arg1.getIntExtra(PLAYER_BAR_ACTION, 0));
				if (arg1.getBooleanExtra("isFromPush", false)) {
					i.putExtra("isFromPush", true);
					i.putExtras(arg1.getExtras());
				}
				i.putExtra("plan_clicked",
						arg1.getSerializableExtra("plan_clicked"));
				i.putExtra("finish_all", true);
				startActivity(i);
				mPlayerBarFragment.updateNotificationForOffflineMode();
			}
		}
	}

	@Override
	public void onBackPressed() {
		int totalSelected = 0;
		List<Boolean> mTrackRemoveState;
		if (mGoOfflineTabBar.getSelectedTab() == GoOfflineTabBar.TAB_ID_SONGS)
			mTrackRemoveState = mTrackRemoveStateMusic;
		else
			mTrackRemoveState = mTrackRemoveStateVideo;
		if (mTrackRemoveState != null && mTrackRemoveState.size() > 0) {
			for (int i = 0; i < mTrackRemoveState.size(); i++) {
				if (mTrackRemoveState.get(i))
					totalSelected = totalSelected + 1;
			}
		}
		if (totalSelected != 0) {
			cancelClick();
			return;
		}
		if (mDrawerLayout != null
				&& mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
			mDrawerLayout.closeDrawers();
			return;
		}

		if (mPlayerBarFragment != null
				&& !mApplicationConfigurations.getSaveOfflineMode())
			mPlayerBarFragment.updateNotificationForOffflineMode();
		if (isAnyActionBarOptionSelected()) {
			super.onBackPressed();
			return;
		} else if (mPlayerBarFragment.isContentOpened()) {
			mPlayerBarFragment.closeContent();
		} else if (!mApplicationConfigurations.getSaveOfflineMode()) {
			finish();
			return;
		} else {
			if (!mPlayerBarFragment.isPlayingForExit()) {
				// stops playing any media.
				Logger.w(
						"GoOfflineActivity",
						"################# explicit stopping the service, Ahhhhhhhhhhhhhhhhhhh #################");
				mPlayerBarFragment.explicitStop();
				
				DBOHandler.exportDB(this);

				super.onBackPressed();
			} else {
				moveTaskToBack(true);
			}
		}
	}

	private void setOptionsItemsVisibility() {
		// if (mGoOfflineTabBar.getSelectedTab() ==
		// GoOfflineTabBar.TAB_ID_SONGS) {
		// if (mMediaItems.size() == 0) {
		// ((LinearLayout) findViewById(R.id.ll_go_offline_play_all))
		// .setVisibility(View.GONE);
		// ((LinearLayout) findViewById(R.id.ll_go_offline_sort_a_to_z))
		// .setVisibility(View.GONE);
		// ((LinearLayout) findViewById(R.id.ll_go_offline_sort_latest))
		// .setVisibility(View.GONE);
		// } else {
		// ((LinearLayout) findViewById(R.id.ll_go_offline_play_all))
		// .setVisibility(View.VISIBLE);
		// ((LinearLayout) findViewById(R.id.ll_go_offline_sort_a_to_z))
		// .setVisibility(View.VISIBLE);
		// ((LinearLayout) findViewById(R.id.ll_go_offline_sort_latest))
		// .setVisibility(View.VISIBLE);
		// }
		// } else {
		// if (mMediaItems.size() == 0) {
		// ((LinearLayout) findViewById(R.id.ll_go_offline_play_all))
		// .setVisibility(View.GONE);
		// ((LinearLayout) findViewById(R.id.ll_go_offline_sort_a_to_z))
		// .setVisibility(View.GONE);
		// ((LinearLayout) findViewById(R.id.ll_go_offline_sort_latest))
		// .setVisibility(View.GONE);
		// } else {
		// ((LinearLayout) findViewById(R.id.ll_go_offline_play_all))
		// .setVisibility(View.GONE);
		// ((LinearLayout) findViewById(R.id.ll_go_offline_sort_a_to_z))
		// .setVisibility(View.VISIBLE);
		// ((LinearLayout) findViewById(R.id.ll_go_offline_sort_latest))
		// .setVisibility(View.VISIBLE);
		// }
		// }
	}

	private void initTrackRemoveState() {
		// if(mGoOfflineTabBar.getSelectedTab()==GoOfflineTabBar.TAB_ID_SONGS)
		// mTrackRemoveState = mTrackRemoveStateMusic;
		// else
		// mTrackRemoveState = mTrackRemoveStateVideo;
		// if (mTrackRemoveState != null)
		// mTrackRemoveState.clear();
		// mTrackRemoveState = new ArrayList<Boolean>();
		// if (mMediaItems != null && mMediaItems.size() > 0)
		// for (int i = 0; i < mMediaItems.size(); i++)
		// mTrackRemoveState.add(false);
		if (mMediaItemsMusic != null && mMediaItemsMusic.size() > 0
				&& mTrackRemoveStateMusic != null)
			for (int i = 0; i < mMediaItemsMusic.size(); i++)
				mTrackRemoveStateMusic.add(false);

		if (mMediaItemsVideo != null && mMediaItemsVideo.size() > 0
				&& mTrackRemoveStateVideo != null)
			for (int i = 0; i < mMediaItemsVideo.size(); i++)
				mTrackRemoveStateVideo.add(false);

		findViewById(R.id.offline_tab_bar_options).setVisibility(View.GONE);
		mTextRemoveHint.setVisibility(View.VISIBLE);
		// mTextOfflineOptions.setVisibility(View.VISIBLE);
		// mIbOfflineOptions.setVisibility(View.VISIBLE);
		// mIvSelectAll.setVisibility(View.INVISIBLE);
		mChkboxSelectAll.setVisibility(View.INVISIBLE);
		mTextCancelRemoveState.setVisibility(View.GONE);
		mTextDeleteSelected.setVisibility(View.GONE);
		setTitleText(false);
	}

	private boolean isAutoCheckedChange = false;

	private class FlipAnimationListener implements AnimationListener {
		private Animation animation1;
		private Animation animation2;
		// private boolean isBackOfCardShowing = true;
		private ImageView view;

		public FlipAnimationListener(ImageView view) {
			animation1 = AnimationUtils.loadAnimation(GoOfflineActivity.this,
					R.anim.to_middle);
			animation1.setAnimationListener(this);
			animation2 = AnimationUtils.loadAnimation(GoOfflineActivity.this,
					R.anim.from_middle);
			animation2.setAnimationListener(this);

			this.view = view;
			view.clearAnimation();
			view.setAnimation(animation1);
			view.startAnimation(animation1);
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			try {
				List<Boolean> mTrackRemoveState;
				if (mGoOfflineTabBar.getSelectedTab() == GoOfflineTabBar.TAB_ID_SONGS)
					mTrackRemoveState = mTrackRemoveStateMusic;
				else
					mTrackRemoveState = mTrackRemoveStateVideo;
				if (animation == animation1) {
					// View parentView = (View) view.getParent();
					View tile = (View) view.getParent().getParent().getParent();
					ViewHolder1 viewHolder = (ViewHolder1) tile
							.getTag(R.id.view_tag_view_holder);
					int position = (Integer) tile
							.getTag(R.id.view_tag_position);
					try {
						if (!mTrackRemoveState.get(position)) {
							mTrackRemoveState.set(position, true);
							((ImageView) view)
									.setBackgroundResource(R.drawable.background_player_queue_album_right_mark);
							((ImageView) view).setImageBitmap(null);
							setTitleText(true);
						} else {
							isAutoCheckedChange = true;
							mChkboxSelectAll.setChecked(false);
							isAutoCheckedChange = false;
							mTrackRemoveState.set(position, false);
							setNotPlaylistResultImage(viewHolder,
									(MediaItem) tile
											.getTag(R.id.view_tag_object),
									position);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					boolean removeState = false;
					for (boolean state : mTrackRemoveState) {
						if (state) {
							removeState = true;
							break;
						}
					}
					if (removeState) {
						// findViewById(R.id.offline_tab_bar_options).setVisibility(
						// View.VISIBLE);
						findViewById(R.id.offline_tab_bar_options)
								.setVisibility(View.GONE);
						mTextRemoveHint.setVisibility(View.GONE);
						// mTextOfflineOptions.setVisibility(View.GONE);
						// mIbOfflineOptions.setVisibility(View.GONE);
						// mIvSelectAll.setVisibility(View.VISIBLE);
						if (mChkboxSelectAll.getVisibility() != View.VISIBLE)
							mChkboxSelectAll.setChecked(false);
						mChkboxSelectAll.setVisibility(View.VISIBLE);
						mTextCancelRemoveState.setVisibility(View.VISIBLE);
						mTextDeleteSelected.setVisibility(View.VISIBLE);
						setTitleText(true);
					} else {
						findViewById(R.id.offline_tab_bar_options)
								.setVisibility(View.GONE);
						mTextRemoveHint.setVisibility(View.VISIBLE);
						// mTextOfflineOptions.setVisibility(View.VISIBLE);
						// mIbOfflineOptions.setVisibility(View.VISIBLE);
						// mIvSelectAll.setVisibility(View.INVISIBLE);
						mChkboxSelectAll.setVisibility(View.INVISIBLE);
						mTextCancelRemoveState.setVisibility(View.GONE);
						mTextDeleteSelected.setVisibility(View.GONE);
						setTitleText(false);
					}

					view.clearAnimation();
					view.setAnimation(animation2);
					view.startAnimation(animation2);
				} else {
					notifyAdapters();
				}/*
				 * else { isBackOfCardShowing=!isBackOfCardShowing; //
				 * findViewById(R.id.button1).setEnabled(true); }
				 */
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}

		// public void setNotPlaylistResultImage(ViewHolder1 viewHolder,
		// MediaItem mediaItem) {
		// try {
		// Picasso.with(getApplicationContext())
		// .load(mediaItem.getImageUrl())
		// .placeholder(
		// R.drawable.background_home_tile_album_default)
		// .into(viewHolder.ivAlbumImage);
		// } catch (Exception e) {
		// viewHolder.ivAlbumImage
		// .setBackgroundResource(R.drawable.background_home_tile_album_default);
		// Logger.printStackTrace(e);
		// }
		// }
	}

	@Override
	protected void onStart() {
		super.onStart();
		Analytics.startSession(this);
		if (!mApplicationConfigurations.getSaveOfflineMode()) {
			showBackButtonWithTitle(
					getResources()
							.getString(
									R.string.main_actionbar_settings_menu_item_offline_music),
					"");
		} else
			showNormalActionBar();

		mPlayerBarFragment.setPlayingEventListener(this);
	}

	public void setNotPlaylistResultImage(final ViewHolder1 viewHolder,
			MediaItem mediaItem, final int position) {
		try {
			String imgUrl = ImagesManager.getMusicArtSmallImageUrl(mediaItem
					.getImagesUrlArray());

			if (!TextUtils.isEmpty(imgUrl)) {
				Picasso.with(getApplicationContext())
						.load(imgUrl)
						.placeholder(
								R.drawable.background_home_tile_album_default)
						.into(viewHolder.ivAlbumImage, new Callback() {
							@Override
							public void onSuccess() {
								try {
									List<Boolean> mTrackRemoveState;
									if (mGoOfflineTabBar.getSelectedTab() == GoOfflineTabBar.TAB_ID_SONGS)
										mTrackRemoveState = mTrackRemoveStateMusic;
									else
										mTrackRemoveState = mTrackRemoveStateVideo;
									if (mTrackRemoveState != null
											&& mTrackRemoveState.get(position)) {
										viewHolder.ivAlbumImage
												.setBackgroundResource(R.drawable.background_player_queue_album_right_mark);
										viewHolder.ivAlbumImage
												.setImageBitmap(null);
									}
								} catch (Exception e) {
									Logger.printStackTrace(e);
								}
							}

							@Override
							public void onError() {
							}
						});
			} else {
				viewHolder.ivAlbumImage
						.setBackgroundResource(R.drawable.background_home_tile_album_default);
				viewHolder.ivAlbumImage
						.setImageResource(R.drawable.background_home_tile_album_default);
			}
		} catch (Exception e) {
			viewHolder.ivAlbumImage
					.setBackgroundResource(R.drawable.background_home_tile_album_default);
			viewHolder.ivAlbumImage
					.setImageResource(R.drawable.background_home_tile_album_default);
			Logger.printStackTrace(e);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		Analytics.onEndSession(this);

		mPlayerBarFragment.setPlayingEventListener(null);
	}

	private boolean isShowingHelp = false;

	public void showOfflineMusicHelp() {
		if (!mApplicationConfigurations.isOfflineMusicHintChecked()) {
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					try {
						// View Settings =
						// findViewById(R.id.offline_imagebutton_options);
						// Settings.setDrawingCacheEnabled(true);
						// Bitmap bmp = Settings.getDrawingCache();
						// int[] containerLocation = new int[2];
						// Settings.getLocationInWindow(containerLocation);
						ListView list = listMusic;
						boolean isContentAvailable = false;
						if (mGoOfflineTabBar.getSelectedTab() == GoOfflineTabBar.TAB_ID_SONGS) {
							isContentAvailable = mMediaItemsMusic.size() > 0;
						} else {
							list = listVideo;
							isContentAvailable = mMediaItemsVideo.size() > 0;
						}

						if (isContentAvailable && !isPaused && !isShowingHelp) {
							isShowingHelp = true;
							View listItem;
							int[] containerLocationItem = new int[2];
							listItem = list.getChildAt(0);
							listItem.getLocationInWindow(containerLocationItem);
							listItem.setDrawingCacheEnabled(true);
							Bitmap bmpItem = loadBitmapFromView(listItem);

							// HelpView settingHelp = new HelpView(
							// containerLocation[0], containerLocation[1], bmp);
							HelpView listItemHelp = new HelpView(
									containerLocationItem[0],
									containerLocationItem[1], bmpItem);

							AppGuideActivityOfflineMusic.classObject = new HelpLeftDrawer(
							/* settingHelp */null, listItemHelp);

							Intent intent = new Intent(GoOfflineActivity.this,
									AppGuideActivityOfflineMusic.class);
							startActivity(intent);
							mApplicationConfigurations
									.setOfflineMusicHintChecked(true);
						}
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
				}
			}, 100);
		}
		// else
		// if(mApplicationConfigurations.isEnabledDownloadDeleteGuidePage()){
		// handler.postDelayed(new Runnable() {
		// @Override
		// public void run() {
		// try {
		// ListView list = listMusic;
		// boolean isContentAvailable = false;
		// if(mGoOfflineTabBar.getSelectedTab() ==
		// GoOfflineTabBar.TAB_ID_SONGS){
		// isContentAvailable = mMediaItemsMusic.size()>0;
		// } else{
		// list = listVideo;
		// isContentAvailable = mMediaItemsVideo.size()>0;
		// }
		//
		// if (isContentAvailable && !isPaused && !isShowingHelp) {
		// isShowingHelp = true;
		// RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(
		// ViewGroup.LayoutParams.WRAP_CONTENT,
		// ViewGroup.LayoutParams.WRAP_CONTENT);
		// lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		// lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		// int margin = ((Number) (getResources()
		// .getDisplayMetrics().density * 12))
		// .intValue();
		// lps.setMargins(
		// 2 * margin,
		// margin,
		// 2 * margin,
		// mDataManager
		// .getShowcaseButtonMargin()
		// * margin);
		//
		// ViewTarget target = new ViewTarget(list
		// .getChildAt(0).findViewById(
		// R.id.player_queue_media_image));
		// ShowcaseView sv = new ShowcaseView.Builder(
		// GoOfflineActivity.this, false)
		// .setTarget(target)
		// .setContentTitle(
		// R.string.showcase_offline_screen_delete_title)
		// .setContentText(
		// R.string.showcase_offline_screen_delete_message)
		// .setStyle(
		// R.style.CustomShowcaseTheme2)
		// .hideOnTouchOutside().build();
		// sv.setBlockShowCaseTouches(true);
		// sv.setButtonPosition(lps);
		// mApplicationConfigurations
		// .setIsEnabledDownloadDeleteGuidePage(false);
		// }
		// } catch (Exception e) {
		// Logger.printStackTrace(e);
		// }
		// }
		// }, 1000);
		// }
	}

	// private SwipeDismissListOffline mSwipeList;
	private SwipeDismissListOffline mSwipeListMusic;
	private SwipeDismissListOffline mSwipeListVideo;
	// private final static String EXTRA_MODE = "MODE";
	private boolean isDeleting = false;

	// private List<MediaItem> mAllItemsList;

	// private void setUpSwipeView() {
	// final int contentType = mGoOfflineTabBar.getSelectedTab();
	// mAllItemsList = new ArrayList<MediaItem>();
	// mAllItemsList.addAll(mMediaItems);
	// int modeInt = 0;
	// SwipeDismissListOffline.UndoMode mode = SwipeDismissListOffline.UndoMode
	// .values()[modeInt];
	//
	// // Get the regular ListView of this activity.
	//
	// // Create a new SwipeDismissList from the activities listview.
	// mSwipeList = new SwipeDismissListOffline(mListView,
	// new SwipeDismissListOffline.OnDismissCallback() {
	// public SwipeDismissListOffline.Undoable onDismiss(
	// AbsListView listView, final int position) {
	//
	// isDeleting = true;
	// // Get item that should be deleted from the adapter.
	// // final String item = mAdapter.getItem(position);
	// final Object item = mOfflineAdapter.getItem(position);
	// final MediaItem track = mMediaItems.get(position);
	//
	// // Delete that item from the adapter.
	// // mQueueAdapter.remove(position);
	// // mQueueAdapter.remove(position);
	// mMediaItems.remove(position);
	// mTrackRemoveState.remove(position);
	// Logger.i("Notify",
	// "Notify::::::::::  SwipeDismissList.Undoable onDismiss: Count:"
	// + mMediaItems.size());
	// notifyAdapters();
	// // mOfflineAdapter.notifyDataSetChanged();
	// // new Handler().postDelayed(new Runnable() {
	// //
	// // @Override
	// // public void run() {
	// // mQueueAdapter.notifyDataSetChanged();
	// // }
	// // }, 5000);
	// // Return an Undoable, for that deletion. If you write
	// // return null
	// // instead, this deletion won't be undoable.
	// return new SwipeDismissListOffline.Undoable() {
	// @Override
	// public String getTitle() {
	// return track.getTitle() + " deleted";
	// }
	//
	// @Override
	// public void undo() {
	// // Reinsert the item at its previous position.
	// mMediaItems.add(position, track);
	// mTrackRemoveState.add(position, false);
	// Logger.i("Notify",
	// "Notify::::::::::  undo Count:"
	// + mMediaItems.size());
	// // mOfflineAdapter.notifyDataSetChanged();
	// notifyAdapters();
	// isDeleting = false;
	// setTitleText(true);
	// // mQueueAdapter.insert(item, position);
	// }
	//
	// @Override
	// public void discard() {
	// // Just write a log message (use logcat to see
	// // the effect)
	// Logger.w("DISCARD", "item " + item
	// + " now finally discarded");
	// // new Handler().postDelayed(new Runnable() {
	// // @Override
	// // public void run() {
	// isDeleting = false;
	// removeFromQueueList(position, contentType, mAllItemsList);
	// // }
	// // }, 200);
	// }
	// };
	//
	// }
	// },
	// // 3rd parameter needs to be the mode the list is generated.
	// mode);
	// mSwipeList.setRequireTouchBeforeDismiss(true);
	// }

	ArrayList<Long> listTracksDeleted, listVideosDeleted;

	private SwipeDismissListOffline setUpSwipeView(ListView mListView,
			final int contentType) {
		// final int contentType = mGoOfflineTabBar.getSelectedTab();
		// if (mMediaItems == null || mMediaItems.size()==0)
		// return;
		// mAllItemsList = new ArrayList<MediaItem>();
		// if (mAllItemsList != null)
		// mAllItemsList.addAll(mMediaItems);
		// final List<MediaItem> mCurrentAllItemsList = new
		// ArrayList<MediaItem>();
		// mCurrentAllItemsList.addAll(mMediaItems);
		listTracksDeleted = new ArrayList<Long>();
		int modeInt = 0;
		SwipeDismissListOffline.UndoMode mode = SwipeDismissListOffline.UndoMode
				.values()[modeInt];

		// Get the regular ListView of this activity.

		// Create a new SwipeDismissList from the activities listview.
		SwipeDismissListOffline mSwipeList = new SwipeDismissListOffline(
				mListView, new SwipeDismissListOffline.OnDismissCallback() {
					public SwipeDismissListOffline.Undoable onDismiss(
							AbsListView listView, final int position) {
						MediaItem track1 = null;
						if(mOfflineAdapterMusic!=null) {
							try {
								isDeleting = true;

								final Object item = mOfflineAdapterMusic
										.getItem(position);
								track1 = mMediaItemsMusic.get(position);
								listTracksDeleted.add(track1.getId());
								mMediaItemsMusic.remove(position);
								mTrackRemoveStateMusic.remove(position);
								// if(contentType == GoOfflineTabBar.TAB_ID_SONGS)
								mOfflineAdapterMusic.setMediaItems(mMediaItemsMusic);
								// else
								// mOfflineAdapterVideo.setMediaItems(mMediaItemsMusic);
								Logger.i("Notify",
										"Notify::::::::::  SwipeDismissList.Undoable onDismiss: Count:"
												+ mMediaItemsMusic.size());
								mOfflineAdapterMusic.notifyDataSetChanged();
								// notifyAdapters();
								if (mMediaItemsMusic == null
										|| mMediaItemsMusic.size() == 0) {
									String message = getString(
											R.string.txt_no_search_result_alert_msg,
											getString(R.string.search_results_layout_bottom_text_for_track));
									DataNotFoundListAdapter adapter = new DataNotFoundListAdapter(
											message);
									listMusic.setAdapter(adapter);
								}
							}catch (Exception e){
								e.printStackTrace();
							}
						}
						final MediaItem track = track1;
						return new SwipeDismissListOffline.Undoable() {
							@Override
							public String getTitle() {
								return (track!=null)?track.getTitle():"" + " deleted";
							}

							@Override
							public void undo() {
  try {
								// Reinsert the item at its previous position.
								// if(contentType ==
								// mGoOfflineTabBar.getSelectedTab()){
								boolean resetAdapter = false;
								if (mMediaItemsMusic.size() == 0) {
									resetAdapter = true;
								}
								listTracksDeleted.remove(track.getId());
								mMediaItemsMusic.add(position, track);
								mTrackRemoveStateMusic.add(position, false);
								// if(contentType==GoOfflineTabBar.TAB_ID_SONGS)
								mOfflineAdapterMusic
										.setMediaItems(mMediaItemsMusic);
								// else
								// mOfflineAdapterVideo.setMediaItems(mMediaItems);
								Logger.i("Notify",
										"Notify::::::::::  undo Count:"
												+ mMediaItemsMusic.size());
								// mOfflineAdapter.notifyDataSetChanged();
								if (resetAdapter) {
									mOfflineAdapterMusic = new GoOfflineTrackAdapter(
											mMediaItemsMusic);
									listMusic.setAdapter(mOfflineAdapterMusic);
								}
								notifyAdapters();
								isDeleting = false;
								setTitleText(true);
								// } else{
								// if(contentType==GoOfflineTabBar.TAB_ID_SONGS){
								// mMediaItemsMusic.add(position, track);
								// mOfflineAdapterMusic.setMediaItems(mMediaItemsMusic);
								// } else
								// if(contentType==GoOfflineTabBar.TAB_ID_VIDOES){
								// mMediaItemsVideo.add(position, track);
								// mOfflineAdapterVideo.setMediaItems(mMediaItemsVideo);
								// }
								// isDeleting = false;
								// }
								// mQueueAdapter.insert(item, position);
 } catch (Exception e) {
                        } catch (Error e) {
                        }
							}

							@Override
							public void discard() {
								// new Handler().postDelayed(new Runnable() {
								// @Override
								// public void run() {
								isDeleting = false;
								// removeFromQueueList(position, contentType,
								// mCurrentAllItemsList);
								removeFromQueueList(position, contentType,
										track);
								listTracksDeleted.remove(track.getId());
								// }
								// }, 200);
							}
						};
					}
				},
				// 3rd parameter needs to be the mode the list is generated.
				mode);
		mSwipeList.setRequireTouchBeforeDismiss(true);
		return mSwipeList;
	}

	private SwipeDismissListOffline setUpSwipeViewVideo(ListView mListView,
			final int contentType) {
		// final int contentType = mGoOfflineTabBar.getSelectedTab();
		// if (mMediaItems == null || mMediaItems.size()==0)
		// return;
		// mAllItemsList = new ArrayList<MediaItem>();
		// if (mAllItemsList != null)
		// mAllItemsList.addAll(mMediaItems);
		// final List<MediaItem> mCurrentAllItemsList = new
		// ArrayList<MediaItem>();
		// mCurrentAllItemsList.addAll(mMediaItems);
		listVideosDeleted = new ArrayList<Long>();

		int modeInt = 0;
		SwipeDismissListOffline.UndoMode mode = SwipeDismissListOffline.UndoMode
				.values()[modeInt];

		// Get the regular ListView of this activity.

		// Create a new SwipeDismissList from the activities listview.
		SwipeDismissListOffline mSwipeList = new SwipeDismissListOffline(
				mListView, new SwipeDismissListOffline.OnDismissCallback() {
					public SwipeDismissListOffline.Undoable onDismiss(
							AbsListView listView, final int position) {
						isDeleting = true;
						// Get item that should be deleted from the adapter.
						// final String item = mAdapter.getItem(position);
						final Object item = mOfflineAdapterVideo
								.getItem(position);
						final MediaItem track = mMediaItemsVideo.get(position);
						listVideosDeleted.add(track.getId());
						// Delete that item from the adapter.
						// mQueueAdapter.remove(position);
						// mQueueAdapter.remove(position);
						mMediaItemsVideo.remove(position);
						mTrackRemoveStateVideo.remove(position);
						// if(contentType == GoOfflineTabBar.TAB_ID_SONGS)
						// mOfflineAdapterMusic.setMediaItems(mMediaItemsVideo);
						// else
						mOfflineAdapterVideo.setMediaItems(mMediaItemsVideo);
						Logger.i("Notify",
								"Notify::::::::::  SwipeDismissList.Undoable onDismiss: Count:"
										+ mMediaItemsVideo.size());
						mOfflineAdapterVideo.notifyDataSetChanged();
						if (mMediaItemsVideo == null
								|| mMediaItemsVideo.size() == 0) {
							String message = getString(
									R.string.txt_no_search_result_alert_msg,
									getString(R.string.search_results_layout_bottom_text_for_video));
							DataNotFoundListAdapter adapter = new DataNotFoundListAdapter(
									message);
							listVideo.setAdapter(adapter);
						}
						// notifyAdapters();
						// new Handler().postDelayed(new Runnable() {
						//
						// @Override
						// public void run() {
						// mQueueAdapter.notifyDataSetChanged();
						// }
						// }, 5000);
						// Return an Undoable, for that deletion. If you write
						// return null
						// instead, this deletion won't be undoable.
						return new SwipeDismissListOffline.Undoable() {
							@Override
							public String getTitle() {
								return track.getTitle() + " deleted";
							}

							@Override
							public void undo() {
								// Reinsert the item at its previous position.
								// if(contentType ==
								// mGoOfflineTabBar.getSelectedTab()){
								boolean resetAdapter = false;
								if (mMediaItemsVideo.size() == 0) {
									resetAdapter = true;
								}
								listVideosDeleted.remove(track.getId());
								mMediaItemsVideo.add(position, track);
								mTrackRemoveStateVideo.add(position, false);
								// if(contentType==GoOfflineTabBar.TAB_ID_SONGS)
								// mOfflineAdapterMusic.setMediaItems(mMediaItems);
								// else
								mOfflineAdapterVideo
										.setMediaItems(mMediaItemsVideo);
								Logger.i("Notify",
										"Notify::::::::::  undo Count:"
												+ mMediaItemsVideo.size());
								// mOfflineAdapter.notifyDataSetChanged();
								if (resetAdapter) {
									mOfflineAdapterVideo = new GoOfflineTrackAdapter(
											mMediaItemsVideo);
									listVideo.setAdapter(mOfflineAdapterVideo);
								}
								notifyAdapters();
								isDeleting = false;
								// setTitleText(true);
								// } else{
								// if(contentType==GoOfflineTabBar.TAB_ID_SONGS){
								// mMediaItemsMusic.add(position, track);
								// mOfflineAdapterMusic.setMediaItems(mMediaItemsMusic);
								// } else
								// if(contentType==GoOfflineTabBar.TAB_ID_VIDOES){
								// mMediaItemsVideo.add(position, track);
								// mOfflineAdapterVideo.setMediaItems(mMediaItemsVideo);
								// }
								// isDeleting = false;
								// }
								// mQueueAdapter.insert(item, position);
							}

							@Override
							public void discard() {
								// Just write a log message (use logcat to see
								// the effect)
								Logger.w("DISCARD", "item " + item
										+ " now finally discarded");
								// new Handler().postDelayed(new Runnable() {
								// @Override
								// public void run() {
								isDeleting = false;
								// removeFromQueueList(position, contentType,
								// mCurrentAllItemsList);
								removeFromQueueList(position, contentType,
										track);
								listVideosDeleted.remove(track.getId());
								// }
								// }, 200);
							}
						};
					}
				},
				// 3rd parameter needs to be the mode the list is generated.
				mode);
		mSwipeList.setRequireTouchBeforeDismiss(true);
		return mSwipeList;
	}

	private void filterTracksList() {
		List<MediaItem> temp = new ArrayList<MediaItem>();
		temp.addAll(mMediaItemsMusic);
		for (MediaItem mediaItem : temp) {
			if (listTracksDeleted.contains(mediaItem.getId())) {
				mMediaItemsMusic.remove(mediaItem);
			}
		}
	}

	private void filterVideosList() {
		List<MediaItem> temp = new ArrayList<MediaItem>();
		temp.addAll(mMediaItemsVideo);

		for(int i=0; i<mMediaItemsVideo.size(); i++) {
			MediaItem mediaItem=mMediaItemsVideo.get(i);
			if (listVideosDeleted.contains(mediaItem.getId())) {
				mMediaItemsVideo.remove(mediaItem);
				i--;
			}
		}

//		for (MediaItem mediaItem : mMediaItemsVideo) {
//			if (listVideosDeleted.contains(mediaItem.getId())) {
//				mMediaItemsVideo.remove(mediaItem);
//			}
//		}
	}

	private void setSelectionListener(final DragSortListView listView,
			final SwipeDismissListOffline mSwipeList) {
		listView.setRemoveListener(new RemoveListener() {
			@Override
			public void remove(final int which) {
				// Logger.i("Notify", "Notify::::::::::  setRemoveListener");
				if (mSwipeList != null) {
					mSwipeList.dismissPendingUndoMsg();
					mSwipeList.dismissCall(listView, which);
				}
				// new Handler().postDelayed(new Runnable() {
				// @Override
				// public void run() {
				// removeFromQueueList(which);
				// }
				// }, 200);
			}
		});

		listView.setItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(int pos) {
				try {
					int firstPosition = listView.getFirstVisiblePosition()
							- listView.getHeaderViewsCount();
					int wantedChild = pos - firstPosition;
					if (wantedChild < 0
							|| wantedChild >= listView.getChildCount()) {
						Logger.w("GoOfflineActivity",
								"Unable to get view for desired position, because it's not being displayed on screen.");
						return;
					}

					View tile = listView.getChildAt(wantedChild);
					if (tile != footerViewVideo && tile != footerViewMusic) {
						// View tile = mTrackListView.getChildAt(pos);
						// View tile = (View) wantedView;
						RelativeLayout rlChild = (RelativeLayout) tile
								.findViewById(R.id.relativelayout_player_queue_line);
						// start play from beginning or pause
						// RelativeLayout tile = (RelativeLayout) view;//
						//
						// ViewHolder1 viewHolder = (ViewHolder1) tile
						// .getTag(R.id.view_tag_view_holder);
						// ViewHolder1 viewHolder = (ViewHolder1) rlChild
						// .getTag(R.id.view_tag_view_holder);
						// int position = (Integer)
						// rlChild.getTag(R.id.view_tag_position);
						//
						// handlePlayClick(position, viewHolder);
						ViewHolder1 viewHolder = (ViewHolder1) rlChild
								.getTag(R.id.view_tag_view_holder);
						int position = (Integer) rlChild
								.getTag(R.id.view_tag_position);

						handlePlayClick(position, viewHolder, true);
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			}
		});
	}

	// private void removeFromQueueList(int pos, int contentType,
	// List<MediaItem> mAllItemsList) {
	// ArrayList<Boolean> mTrackRemoveState = new ArrayList<Boolean>();
	// // mTrackRemoveState.addAll(this.mTrackRemoveState);
	// // mTrackRemoveState.add(pos, true);
	// ArrayList<Long> idsToRemove = new ArrayList<Long>();
	// List<MediaItem> mMediaItems = mAllItemsList;
	// if(contentType == GoOfflineTabBar.TAB_ID_SONGS){
	// for(int i =0; i<mMediaItemsMusic.size();i++)
	// mTrackRemoveState.add(false);
	// // mMediaItems = mMediaItemsMusic;
	// } else{
	// for(int i =0; i<mMediaItemsVideo.size();i++)
	// mTrackRemoveState.add(false);
	// // mMediaItems = mMediaItemsVideo;
	// }
	// mTrackRemoveState.add(pos, true);
	// // if (CacheManager.isProUser(this))
	// // mMediaItems = DBOHandler.getAllCachedTracks(this);
	// // else
	// // mMediaItems = DBOHandler.getAllTracksForFreeUser(this);
	// // if (mMediaItems.size() == 0) {
	// // displayNoDataDialog();
	// // findViewById(R.id.divider_bottom).setVisibility(View.GONE);
	// // } else {
	// // findViewById(R.id.divider_bottom).setVisibility(View.VISIBLE);
	// // // mOfflineAdapter = new GoOfflineTrackAdapter();
	// // // mListView.setAdapter(mOfflineAdapter);
	// // }
	// // for (int i = 0; i < mTrackRemoveState.size(); i++) {
	// int i = pos;
	// if (mTrackRemoveState.get(i)) {
	// MediaItem track = mMediaItems.get(i);
	// if (track != null) {
	// if (contentType == GoOfflineTabBar.TAB_ID_SONGS) {
	// String path = DBOHandler.getTrackPathById(this,
	// "" + track.getId());
	// if (path != null && path.length() > 0) {
	// File file = new File(path);
	// if (file.exists()) {
	// file.delete();
	// }
	// }
	// boolean isTracksDeleted = DBOHandler.deleteCachedTrack(
	// this, "" + track.getId());
	// Logger.s(track.getTitle() + " ::::::delete:::::: "
	// + isTracksDeleted);
	// if (isTracksDeleted) {
	// Intent TrackCached = new Intent(
	// CacheManager.ACTION_TRACK_CACHED);
	// TrackCached.putExtra("is_from_offline", true);
	// Logger.i("Update Cache State",
	// " SENDING BROADCAST TRACK_CACHED");
	// sendBroadcast(TrackCached);
	// }
	// idsToRemove.add(track.getId());
	// } else if (contentType == GoOfflineTabBar.TAB_ID_VIDOES) {
	// String path = DBOHandler.getVideoTrackPathById(this, ""
	// + track.getId());
	// if (path != null && path.length() > 0) {
	// File file = new File(path);
	// if (file.exists()) {
	// file.delete();
	// }
	// }
	// boolean isTracksDeleted = DBOHandler
	// .deleteCachedVideoTrack(this, "" + track.getId());
	// Logger.s(track.getTitle() + " ::::::delete:::::: "
	// + isTracksDeleted);
	// if (isTracksDeleted) {
	// Intent TrackCached = new Intent(
	// CacheManager.ACTION_VIDEO_TRACK_CACHED);
	// Logger.i("Update Cache State",
	// " SENDING BROADCAST VIDEO_TRACK_CACHED");
	// sendBroadcast(TrackCached);
	// }
	// }
	// }
	// // int position = (Integer)
	// // tile.getTag(R.id.view_tag_position);
	// // removes the original from the player.
	// mMediaItems.remove(i);
	// mTrackRemoveState.remove(i);
	// // mPlayerBar.removeFrom(i);
	// i--;
	// }
	// // }
	//
	// if (idsToRemove.size() > 0) {
	// mPlayerBarFragment.removeTrack(idsToRemove);
	// }
	// // mMediaItems = getOfflineSongs();
	// // this.mMediaItems = mMediaItems;
	// // this.mTrackRemoveState = mTrackRemoveState;
	// // setTitleText(false);
	// // mGoOfflineTabBar.updateTabCount(mGoOfflineTabBar.getSelectedTab(),
	// // this.mMediaItems.size());
	// if (contentType == mGoOfflineTabBar.getSelectedTab()) {
	// this.mMediaItems = mMediaItems;
	// this.mTrackRemoveState = mTrackRemoveState;
	// setTitleText(false);
	// mGoOfflineTabBar.updateTabCount(mGoOfflineTabBar.getSelectedTab(),
	// this.mMediaItems.size());
	//
	// if(contentType == GoOfflineTabBar.TAB_ID_SONGS)
	// mOfflineAdapterMusic.setMediaItems(mMediaItems);
	// else
	// mOfflineAdapterVideo.setMediaItems(mMediaItems);
	// notifyAdapters();
	// }
	//
	// // onTabSelected(mGoOfflineTabBar.getSelectedTab());
	// // mIvSelectAll.setVisibility(View.INVISIBLE);
	// }

	private void removeFromQueueList(int pos, int contentType, MediaItem track) {
		// ArrayList<Boolean> mTrackRemoveState = new ArrayList<Boolean>();
		// mTrackRemoveState.addAll(this.mTrackRemoveState);
		// mTrackRemoveState.add(pos, true);
		ArrayList<Long> idsToRemove = new ArrayList<Long>();
		// List<MediaItem> mMediaItems = mAllItemsList;
		// if(contentType == GoOfflineTabBar.TAB_ID_SONGS){
		// for(int i =0; i<mMediaItemsMusic.size();i++)
		// mTrackRemoveState.add(false);
		// // mMediaItems = mMediaItemsMusic;
		// } else{
		// for(int i =0; i<mMediaItemsVideo.size();i++)
		// mTrackRemoveState.add(false);
		// // mMediaItems = mMediaItemsVideo;
		// }
		// mTrackRemoveState.add(pos, true);
		// if (CacheManager.isProUser(this))
		// mMediaItems = DBOHandler.getAllCachedTracks(this);
		// else
		// mMediaItems = DBOHandler.getAllTracksForFreeUser(this);
		// if (mMediaItems.size() == 0) {
		// displayNoDataDialog();
		// findViewById(R.id.divider_bottom).setVisibility(View.GONE);
		// } else {
		// findViewById(R.id.divider_bottom).setVisibility(View.VISIBLE);
		// // mOfflineAdapter = new GoOfflineTrackAdapter();
		// // mListView.setAdapter(mOfflineAdapter);
		// }
		// for (int i = 0; i < mTrackRemoveState.size(); i++) {
		// int i = pos;
		// if (mTrackRemoveState.get(i)) {
		// MediaItem track = mMediaItems.get(i);
		if (track != null) {
			if (contentType == GoOfflineTabBar.TAB_ID_SONGS) {
				String path = DBOHandler.getTrackPathById(this,
						"" + track.getId());
				if (path != null && path.length() > 0) {
					File file = new File(path);
					if (file.exists()) {
						file.delete();
					}
				}
				boolean isTracksDeleted = DBOHandler.deleteCachedTrack(this, ""
						+ track.getId());
				Logger.s(track.getTitle() + " ::::::delete:::::: "
						+ isTracksDeleted);
				if (isTracksDeleted) {
					Intent TrackCached = new Intent(
							CacheManager.ACTION_TRACK_CACHED);
					TrackCached.putExtra("is_from_offline", true);
					Logger.i("Update Cache State",
							" SENDING BROADCAST TRACK_CACHED");
					sendBroadcast(TrackCached);
				}
				idsToRemove.add(track.getId());
			} else if (contentType == GoOfflineTabBar.TAB_ID_VIDOES) {
				String path = DBOHandler.getVideoTrackPathById(this,
						"" + track.getId());
				if (path != null && path.length() > 0) {
					File file = new File(path);
					if (file.exists()) {
						file.delete();
					}
				}
				boolean isTracksDeleted = DBOHandler.deleteCachedVideoTrack(
						this, "" + track.getId());
				Logger.s(track.getTitle() + " ::::::delete:::::: "
						+ isTracksDeleted);
				if (isTracksDeleted) {
					Intent TrackCached = new Intent(
							CacheManager.ACTION_VIDEO_TRACK_CACHED);
					TrackCached.putExtra("is_from_offline", true);
					Logger.i("Update Cache State",
							" SENDING BROADCAST VIDEO_TRACK_CACHED");
					sendBroadcast(TrackCached);
				}
			}
		}
		// // int position = (Integer)
		// // tile.getTag(R.id.view_tag_position);
		// // removes the original from the player.
		// mMediaItems.remove(i);
		// mTrackRemoveState.remove(i);
		// // mPlayerBar.removeFrom(i);
		// i--;
		// }
		// }

		if (idsToRemove.size() > 0) {
			mPlayerBarFragment.removeTrack(idsToRemove);
		}
		// mMediaItems = getOfflineSongs();
		// this.mMediaItems = mMediaItems;
		// this.mTrackRemoveState = mTrackRemoveState;
		// setTitleText(false);
		// mGoOfflineTabBar.updateTabCount(mGoOfflineTabBar.getSelectedTab(),
		// this.mMediaItems.size());
		// if (contentType == mGoOfflineTabBar.getSelectedTab()) {
		// this.mMediaItems = mMediaItems;
		// this.mTrackRemoveState = mTrackRemoveState;
		// setTitleText(false);
		// mGoOfflineTabBar.updateTabCount(mGoOfflineTabBar.getSelectedTab(),
		// this.mMediaItems.size());
		//
		// if(contentType == GoOfflineTabBar.TAB_ID_SONGS)
		// mOfflineAdapterMusic.setMediaItems(mMediaItems);
		// else
		// mOfflineAdapterVideo.setMediaItems(mMediaItems);
		// notifyAdapters();
		// }

		// onTabSelected(mGoOfflineTabBar.getSelectedTab());
		// mIvSelectAll.setVisibility(View.INVISIBLE);
	}

	// private List<MediaItem> getOfflineSongs() {
	// List<MediaItem> mMediaItems;
	// if (CacheManager.isProUser(this))
	// mMediaItems = DBOHandler.getAllCachedTracks(this);
	// else
	// // mMediaItems = DBOHandler.getAllTracksForFreeUser(this);
	// mMediaItems = DBOHandler.getAllOfflineTracksForFreeUser(this);
	// return mMediaItems;
	// }

	public static Bitmap loadBitmapFromView(View v) {
		Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(),
				Utils.bitmapConfig8888);
		Canvas c = new Canvas(b);
		v.layout(0, 0, v.getWidth(), v.getHeight());
		v.draw(c);
		return b;

		// v.setDrawingCacheEnabled(true);
		//
		// // this is the important code :)
		// // Without it the view will have a dimension of 0,0 and the bitmap
		// will be null
		// v.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
		// MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		// v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
		//
		// v.buildDrawingCache(true);
		// Bitmap b = Bitmap.createBitmap(v.getDrawingCache());
		// v.setDrawingCacheEnabled(false); // clear drawing cache
		// return b;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMenu = menu;
		if (mApplicationConfigurations.getSaveOfflineMode()) {
			MenuInflater inflater = getMenuInflater();
			if (pager.getCurrentItem() == TAB_SONGS) {
				inflater.inflate(R.menu.menu_main_offline_music_actionbar, menu);
			} else if (pager.getCurrentItem() == TAB_VIDEOS) {
				inflater.inflate(R.menu.menu_main_offline_actionbar, menu);
			}
			return true;
		} else if (!mApplicationConfigurations.getSaveOfflineMode()) {
			MenuInflater inflater = getMenuInflater();
			if (pager.getCurrentItem() == TAB_SONGS) {
				inflater.inflate(
						R.menu.menu_main_offline_music_without_airoplan_actionbar,
						menu);
			} else if (pager.getCurrentItem() == TAB_VIDEOS) {
				inflater.inflate(
						R.menu.menu_main_offline_without_airoplan_actionbar,
						menu);
			}
			return true;
		} else {
			return super.onCreateOptionsMenu(menu);
		}

	}

	private boolean isSortByLatest = true, isSortByAlbum = false;

	private void openOptionAction(final View view) {
		String menuItems[] = new String[] {
				getString(R.string.go_offline_option_sort_a_to_z),
				getString(R.string.go_offline_option_sort_album),
				getString(R.string.go_offline_option_settings) };
		if (!isSortByLatest && !isSortByAlbum) {
			menuItems = new String[] {
					getString(R.string.go_offline_option_sort_latest),
					getString(R.string.go_offline_option_sort_album),
					getString(R.string.go_offline_option_settings) };
		} else if (isSortByAlbum) {
			menuItems = new String[] {
					getString(R.string.go_offline_option_sort_a_to_z),
					getString(R.string.go_offline_option_sort_latest),
					getString(R.string.go_offline_option_settings) };
		}
		try {
			final String items[] = menuItems;
			QuickActionDiscoveryGallery quickaction;
			quickaction = new QuickActionDiscoveryGallery(this, menuItems,
					FlurryConstants.FlurryKeys.OfflineMusic.toString());
			quickaction
					.setOnDiscoverySelectedListener(new OnDiscoverySelectedListener() {
						@Override
						public void onItemSelectedPosition(int id) {
							if (id == 0 || id == 1) {
								if (mMediaItems == null
										|| (mMediaItems != null && mMediaItems
												.size() == 0)) {
									return;
								}
								if (items[id]
										.equals(getString(R.string.go_offline_option_sort_latest))) {
									isSortByAlbum = false;
									isSortByLatest = true;
									if (mGoOfflineTabBar.getSelectedTab() == GoOfflineTabBar.TAB_ID_SONGS) {
										if (CacheManager
												.isProUser(GoOfflineActivity.this))
											mMediaItemsMusic = DBOHandler
													.getAllTracks(GoOfflineActivity.this);
										else
											// mMediaItems = DBOHandler
											// .getAllTracksForFreeUser(GoOfflineActivity.this);
											mMediaItemsMusic = DBOHandler
													.getAllOfflineTracksForFreeUser(GoOfflineActivity.this);
										filterTracksList();
										mMediaItems = mMediaItemsMusic;
										mTrackRemoveStateMusic = new ArrayList<Boolean>();
										initTrackRemoveState();

										if (mMediaItemsMusic != null
												&& mMediaItemsMusic.size() > 0) {
											mOfflineAdapterMusic = new GoOfflineTrackAdapter(
													mMediaItemsMusic);
											listMusic
													.setAdapter(mOfflineAdapterMusic);
										} else {
											String message = getString(
													R.string.txt_no_search_result_alert_msg,
													getString(R.string.search_results_layout_bottom_text_for_track));
											DataNotFoundListAdapter adapter = new DataNotFoundListAdapter(
													message);
											listMusic.setAdapter(adapter);
										}

										mOfflineAdapter = mOfflineAdapterMusic;
										// notifyAdapters();
									} else if (mGoOfflineTabBar
											.getSelectedTab() == GoOfflineTabBar.TAB_ID_VIDOES) {
										mMediaItemsVideo = DBOHandler
												.getAllVideoTracks(GoOfflineActivity.this);
										filterVideosList();
										mMediaItems = mMediaItemsVideo;
										mTrackRemoveStateVideo = new ArrayList<Boolean>();
										initTrackRemoveState();
										if (mMediaItemsVideo != null
												&& mMediaItemsVideo.size() > 0) {
											mOfflineAdapterVideo = new GoOfflineTrackAdapter(
													mMediaItemsVideo);
											listVideo
													.setAdapter(mOfflineAdapterVideo);
										} else {
											String message = getString(
													R.string.txt_no_search_result_alert_msg,
													getString(R.string.search_results_layout_bottom_text_for_video));
											DataNotFoundListAdapter adapter = new DataNotFoundListAdapter(
													message);
											listVideo.setAdapter(adapter);
										}

										mOfflineAdapter = mOfflineAdapterVideo;
									}
									// setUpSwipeView();
								} else if (items[id]
										.equals(getString(R.string.go_offline_option_sort_a_to_z))) {
									isSortByLatest = false;
									isSortByAlbum = false;
									if (mMediaItems != null)
										Collections.sort(mMediaItems,
												new Comparator<MediaItem>() {
													public int compare(
															MediaItem a,
															MediaItem b) {
														try {
															return a.getTitle()
																	.compareToIgnoreCase(
																			b.getTitle());
														} catch (Exception e) {
															Logger.printStackTrace(e);
														}
														return 0;
													}
												});
									if (mGoOfflineTabBar.getSelectedTab() == GoOfflineTabBar.TAB_ID_SONGS) {
										mOfflineAdapterMusic
												.setMediaItems(mMediaItems);
										// setUpSwipeView(listMusic);
									} else {
										mOfflineAdapterVideo
												.setMediaItems(mMediaItems);
										// setUpSwipeView(listVideo);
									}
									// mAllItemsList = new
									// ArrayList<MediaItem>();
									// if (mAllItemsList != null)
									// mAllItemsList.addAll(mMediaItems);
									// setUpSwipeView();
									// if (mOfflineAdapter != null)
									// mOfflineAdapter.notifyDataSetChanged();
									notifyAdapters();
								} else if (items[id]
										.equals(getString(R.string.go_offline_option_sort_album))) {
									isSortByLatest = false;
									isSortByAlbum = true;
									Collections.sort(mMediaItems,
											new Comparator<MediaItem>() {
												public int compare(MediaItem a,
														MediaItem b) {
													try {
														return a.getAlbumName()
																.compareToIgnoreCase(
																		b.getAlbumName());
													} catch (Exception e) {
														Logger.printStackTrace(e);
													}
													return 0;
												}
											});
									if (mGoOfflineTabBar.getSelectedTab() == GoOfflineTabBar.TAB_ID_SONGS) {
										mOfflineAdapterMusic
												.setMediaItems(mMediaItems);
										// setUpSwipeView(listMusic);
									} else {
										mOfflineAdapterVideo
												.setMediaItems(mMediaItems);
										// setUpSwipeView(listVideo);
									}
									// mAllItemsList = new
									// ArrayList<MediaItem>();
									// if (mAllItemsList != null)
									// mAllItemsList.addAll(mMediaItems);
									notifyAdapters();
								}
							} else if (id == 2) {
								Intent intent = new Intent(
										GoOfflineActivity.this,
										SettingsActivity.class);
								intent.putExtra("load_save_offline", true);
								startActivity(intent);
							}
						}

						@Override
						public void onItemSelected(String item) {
						}
					});
			quickaction.show(view);
			view.setEnabled(false);
			quickaction
					.setOnDismissListener(new QuickActionDiscoveryGallery.OnDismissListener() {
						@Override
						public void onDismiss() {
							view.setEnabled(true);
						}
					});
		} catch (Exception e) {
			Logger.printStackTrace(e);

		}

	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		int itemId = item.getItemId();
		int totalSelected = 0;
		List<Boolean> mTrackRemoveState;
		if (mGoOfflineTabBar.getSelectedTab() == GoOfflineTabBar.TAB_ID_SONGS)
			mTrackRemoveState = mTrackRemoveStateMusic;
		else
			mTrackRemoveState = mTrackRemoveStateVideo;
		if (mTrackRemoveState != null && mTrackRemoveState.size() > 0) {
			for (int i = 0; i < mTrackRemoveState.size(); i++) {
				if (mTrackRemoveState.get(i))
					totalSelected = totalSelected + 1;
			}
		}
		if (itemId == R.id.queue_delete) {
			// if (totalSelected == mTrackRemoveState.size() || totalSelected ==
			// 0) {
			displyClearDialog();
			// } else {
			// displyClearSelectedSongsDialog();
			// }
		} else if (itemId == android.R.id.home) {
			if (!mApplicationConfigurations.getSaveOfflineMode()
					&& totalSelected == 0) {
				finish();
			} else
				cancelClick();
			return true;
		} /*
		 * else if (item.getItemId() ==
		 * R.id.menu_item_main_actionbar_offline_options) { String menuItems[] =
		 * new String[] { getString(R.string.go_offline_option_sort_a_to_z),
		 * getString(R.string.go_offline_option_settings) }; if
		 * (!isSortByLatest) { menuItems = new String[] {
		 * getString(R.string.go_offline_option_sort_latest),
		 * getString(R.string.go_offline_option_settings) }; } try {
		 * QuickActionDiscoveryGallery quickaction; quickaction = new
		 * QuickActionDiscoveryGallery(this, menuItems,
		 * FlurryConstants.FlurryKeys.OfflineMusic.toString()); quickaction
		 * .setOnDiscoverySelectedListener(new OnDiscoverySelectedListener() {
		 * 
		 * @Override public void onItemSelectedPosition(int id) { // TODO
		 * Auto-generated method stub if (id == 0) { if (isSortByLatest) {
		 * isSortByLatest = false; Collections.sort(mMediaItems, new
		 * Comparator<MediaItem>() { public int compare( MediaItem a, MediaItem
		 * b) { try { return a.getTitle() .compareToIgnoreCase( b.getTitle()); }
		 * catch (Exception e) { Logger.printStackTrace(e); } return 0; } }); //
		 * setUpSwipeView(); if (mOfflineAdapter != null) mOfflineAdapter
		 * .notifyDataSetChanged(); } else { isSortByLatest = true; if
		 * (mGoOfflineTabBar.getSelectedTab() == GoOfflineTabBar.TAB_ID_SONGS) {
		 * if (CacheManager .isProUser(GoOfflineActivity.this)) mMediaItemsMusic
		 * = DBOHandler .getAllCachedTracks(GoOfflineActivity.this); else //
		 * mMediaItems = DBOHandler //
		 * .getAllTracksForFreeUser(GoOfflineActivity.this); mMediaItemsMusic =
		 * DBOHandler .getAllOfflineTracksForFreeUser(GoOfflineActivity.this);
		 * mMediaItems = mMediaItemsMusic; initTrackRemoveState();
		 * mOfflineAdapterMusic = new GoOfflineTrackAdapter( mMediaItemsMusic);
		 * mListView .setAdapter(mOfflineAdapterMusic); mOfflineAdapter =
		 * mOfflineAdapterMusic; } else if (mGoOfflineTabBar .getSelectedTab()
		 * == GoOfflineTabBar.TAB_ID_VIDOES) { mMediaItemsVideo = DBOHandler
		 * .getAllVideoTracks(GoOfflineActivity.this); mMediaItems =
		 * mMediaItemsVideo; initTrackRemoveState(); mOfflineAdapterVideo = new
		 * GoOfflineTrackAdapter( mMediaItemsVideo); mListView
		 * .setAdapter(mOfflineAdapterVideo); mOfflineAdapter =
		 * mOfflineAdapterVideo; } setUpSwipeView(); } } else if (id == 1) {
		 * Intent intent = new Intent( GoOfflineActivity.this,
		 * SettingsActivity.class); intent.putExtra("load_save_offline", true);
		 * startActivity(intent); } }
		 * 
		 * @Override public void onItemSelected(String item) { } });
		 * quickaction.show(findViewById(item.getItemId()));
		 * item.setEnabled(false); quickaction .setOnDismissListener(new
		 * QuickActionDiscoveryGallery.OnDismissListener() {
		 * 
		 * @Override public void onDismiss() { item.setEnabled(true); } }); }
		 * catch (Exception e) { Logger.printStackTrace(e);
		 * 
		 * } return true; }
		 */else if (item.getItemId() == R.id.menu_item_main_actionbar_go_offline_more_option) {
			openOptionAction(findViewById(item.getItemId()));
		} else if (item.getItemId() == R.id.menu_item_main_actionbar_go_offline_play_all) {
			playAllSongs();
		}
		return super.onOptionsItemSelected(item);
	}

	private void displyClearDialog() {
		final CustomAlertDialog clearDialogBuilder = new CustomAlertDialog(
				GoOfflineActivity.this);
		// clearDialogBuilder.setTitle(Utils.TEXT_EMPTY);
		clearDialogBuilder.setMessage(Utils.getMultilanguageText(
				GoOfflineActivity.this,
				getResources().getString(
						R.string.go_offline_option_confirm_clear_all)));
		clearDialogBuilder.setCancelable(false);
		// sets the OK button.
		clearDialogBuilder
				.setPositiveButton(
						Utils.getMultilanguageText(
								GoOfflineActivity.this,
								getResources()
										.getString(
												R.string.player_queue_message_confirm_clear_selected_songs_ok)),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								clearSelectedAudios();
							}
						});
		// sets the Cancel button.
		clearDialogBuilder
				.setNegativeButton(
						Utils.getMultilanguageText(
								GoOfflineActivity.this,
								getResources()
										.getString(
												R.string.player_queue_message_confirm_clear_all_cancel)),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});

		// clearDialogBuilder.create();
		clearDialogBuilder.show();

		// FlurryAgent.logEvent(FlurryConstants.FlurryAllPlayer.ClearQueue.toString());
		// xtpl
		Map<String, String> reportMap = new HashMap<String, String>();
		reportMap
				.put(FlurryConstants.FlurryMediaDetailActions.ActionTaken
						.toString(), FlurryConstants.FlurryAllPlayer.ClearQueue
						.toString());
		Analytics.logEvent(
				FlurryConstants.FlurryAllPlayer.PlayerQueueViewed.toString(),
				reportMap);
	}

	private void setTitleText(boolean needToSetCounter) {
		String title = "";
		int totalSelected = 0;
		List<Boolean> mTrackRemoveState;
		if (mGoOfflineTabBar.getSelectedTab() == GoOfflineTabBar.TAB_ID_SONGS)
			mTrackRemoveState = mTrackRemoveStateMusic;
		else
			mTrackRemoveState = mTrackRemoveStateVideo;
		if (mTrackRemoveState != null && mTrackRemoveState.size() > 0) {
			for (int i = 0; i < mTrackRemoveState.size(); i++) {
				if (mTrackRemoveState.get(i))
					totalSelected = totalSelected + 1;
			}
		}
		if (totalSelected != 0) {
			title = totalSelected + "";
			changeMenu(true, title);
			// mTextTitle.setText(mTrackRemoveState.size() + "");
		} else {
			title = Utils.getMultilanguageText(GoOfflineActivity.this,
					getResources().getString(R.string.application_name));
			changeMenu(false, title);
		}
		// getSupportActionBar().setIcon(
		// new ColorDrawable(getResources().getColor(
		// android.R.color.transparent)));
	}

	private void changeMenu(boolean needToShowDeleteIcon, String title) {
		if (needToShowDeleteIcon) {
			try {
				// Utils.setActionBarTitle(GoOfflineActivity.this,
				// getSupportActionBar(),
				// getResources().getString(R.string.application_name));
				// hideCategoryActionBar();
				mMenu.clear();
				MenuInflater inflater = getMenuInflater();
				inflater.inflate(R.menu.main_menu_offline_delete, mMenu);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// getSupportActionBar().setHomeButtonEnabled(false);
			// getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			// getSupportActionBar().setDisplayHomeAsUpEnabled(true);

			HideDrawer();

			showBackButtonWithTitleWithouLogo(title, "");
			getSupportActionBar().setHomeAsUpIndicator(
					R.drawable.abc_ic_ab_back_mtrl_am_alpha_normal);
			getSupportActionBar().setHomeButtonEnabled(true);
			if (mApplicationConfigurations.getSaveOfflineMode()) {
				mToolbar.setNavigationOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						onBackPressed();
					}
				});
			}
			// getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			// getSupportActionBar().setHomeButtonEnabled(true);
			// setActionBarArrowDependingOnFragmentsBackStack();
			lockDrawer();
		} else {
			if (!mApplicationConfigurations.getSaveOfflineMode()) {
				try {
					mMenu.clear();
					onCreateOptionsMenu(mMenu);
				} catch (Exception e) {
				}
				showBackButtonWithTitle(
						getResources()
								.getString(
										R.string.main_actionbar_settings_menu_item_offline_music),
						"");
			} else {
				try {
					showNormalActionBar();
					unlockDrawer();
				} catch (Exception e) {
				}
				mToolbar.setNavigationOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						try{
							if (mDrawerLayout != null) {
								mDrawerLayout.openDrawer(Gravity.LEFT);
							} else {
								mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
								mDrawerLayout.openDrawer(Gravity.LEFT);
							}
						}catch (Exception e){}
						catch (Error e){}
					}
				});
			}
			// getSupportActionBar().setHomeButtonEnabled(true);
			// getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			// onCreateOptionsMenu(mMenu);
			// showBackButtonWithTitleWithouLogo(title, "");
			showDrawer();
		}
	}

	@Override
	public void onTrackLoad() {
		Logger.i("Notify", "Notify:::::::::: onTrackLoad");
		loadingTrack = -1;
		// if (mOfflineAdapter != null)
		// mOfflineAdapter.notifyDataSetChanged();
		// if (mOfflineAdapterMusic != null) {
		// mOfflineAdapterMusic.notifyDataSetChanged();
		// }
		notifyAdapters();
	}

	@Override
	public void onTrackPlay() {
		Logger.i("Notify", "Notify:::::::::: onTrackPlay");
		// if (mOfflineAdapter != null)
		// mOfflineAdapter.notifyDataSetChanged();
		// if (mOfflineAdapterMusic != null) {
		// mOfflineAdapterMusic.notifyDataSetChanged();
		// }
		notifyAdapters();
	}

	@Override
	public void onTrackFinish() {
		Logger.i("Notify", "Notify:::::::::: onTrackFinish");
		// if (mOfflineAdapter != null)
		// mOfflineAdapter.notifyDataSetChanged();
		// if (mOfflineAdapterMusic != null) {
		// mOfflineAdapterMusic.notifyDataSetChanged();
		// }
		notifyAdapters();
	}

	class PlayerStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// if (mOfflineAdapter != null) {
			// isFromReciver = true;
			// mOfflineAdapter.notifyDataSetChanged();
			// }
			// if (mOfflineAdapterMusic != null) {
			// isFromReciver = true;
			// mOfflineAdapterMusic.notifyDataSetChanged();
			// }
			notifyAdapters();
		}
	}

	@Override
	public void onStartLoadingTrack(Track track) {
		// TODO Auto-generated method stub
		// if (mOfflineAdapter != null)
		// mOfflineAdapter.notifyDataSetChanged();
		// if (mOfflineAdapterMusic != null) {
		// mOfflineAdapterMusic.notifyDataSetChanged();
		// }
		notifyAdapters();
	}

	@Override
	public void onTrackLoadingBufferUpdated(Track track, int precent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStartPlayingTrack(Track track) {
		// TODO Auto-generated method stub
		// if (mOfflineAdapter != null)
		// mOfflineAdapter.notifyDataSetChanged();
		// if (mOfflineAdapterMusic != null) {
		// mOfflineAdapterMusic.notifyDataSetChanged();
		// }
		notifyAdapters();
	}

	@Override
	public void onFinishPlayingTrack(Track track) {
		// if (mOfflineAdapter != null)
		// mOfflineAdapter.notifyDataSetChanged();
		// if (mOfflineAdapterMusic != null) {
		// mOfflineAdapterMusic.notifyDataSetChanged();
		// }
		notifyAdapters();
	}

	@Override
	public void onFinishPlayingQueue() {
		// TODO Auto-generated method stub
		// if (mOfflineAdapter != null)
		// mOfflineAdapter.notifyDataSetChanged();
		// if (mOfflineAdapterMusic != null) {
		// mOfflineAdapterMusic.notifyDataSetChanged();
		// }
		notifyAdapters();
	}

	@Override
	public void onSleepModePauseTrack(Track track) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onErrorHappened(
			com.hungama.myplay.activity.player.PlayerService.Error error) {

	}

	@Override
	public void onStartPlayingAd(Placement audioad) {
		// finish();
	}

	@Override
	public void onAdCompletion() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onServiceConnected(ComponentName arg0, IBinder arg1) {
		if (PlayerService.service != null) {
			PlayerService.service.registerPlayerStateListener(this);
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName arg0) {
		// TODO Auto-generated method stub

	}

	public static boolean needToSwitchToOnlineMode = false;

	BroadcastReceiver AireplaneModeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Logger.s("AireplaneModeReceiver :: " + intent.getAction()
					+ " " + Utils.isDeviceAirplaneModeActive(context) + " "
					+ Utils.isConnected());
			if (mApplicationConfigurations.getSaveOfflineAutoMode()
					&& !Utils.isDeviceAirplaneModeActive(context)
					&& Utils.isConnected()) {
				// CustomAlertDialog dialog = new CustomAlertDialog(context);
				// dialog.setMessage("You need data connection to play this service");
				// dialog.setPositiveButton("Settings", new
				// DialogInterface.OnClickListener() {
				// @Override
				// public void onClick(DialogInterface dialog, int which) {
				// startActivity(new
				// Intent(android.provider.Settings.ACTION_SETTINGS));
				// }
				// });
				// dialog.setNegativeButton("Offline", new
				// DialogInterface.OnClickListener() {
				// @Override
				// public void onClick(DialogInterface dialog, int which) {
				// handleOfflineSwitchCase(true);
				// }
				// });
				// dialog.show();
				if (HungamaApplication.isActivityVisible()) {
					needToSwitchToOnlineMode = false;
					handleOfflineSwitchCase(false);
				} else {
					needToSwitchToOnlineMode = true;
				}
			}
		}
	};

	private void notifyAdapters() {
		if (mOfflineAdapter != null)
			mOfflineAdapter.notifyDataSetChanged();

		if (mOfflineAdapterMusic != null && pager.getCurrentItem() == TAB_SONGS)
			mOfflineAdapterMusic.notifyDataSetChanged();
		if (mOfflineAdapterVideo != null
				&& pager.getCurrentItem() == TAB_VIDEOS)
			mOfflineAdapterVideo.notifyDataSetChanged();
	}

	@Override
	public void onItemSelectedPosition(int id, int pos, String item,
			boolean isSong) {
		Logger.i("GoOffline", "Pos:" + id + "::: Pos1:" + pos);
		if (mMediaItems == null
				|| (mMediaItems != null && mMediaItems.size() == 0)) {
			return;
		}
		MediaItem mediaItem = mMediaItems.get(pos);

		if (item.equals(getString(R.string.general_download))) {
			if (mApplicationConfigurations.getSaveOfflineMode())
				return;
			String mFlurrySubSectionDescription = FlurryConstants.FlurrySubSectionDescription.SongDetail
					.toString();
			// download
			MediaItem trackMediaItem = new MediaItem(mediaItem.getId(),
					mediaItem.getTitle(), mediaItem.getAlbumName(),
					mediaItem.getArtistName(), mediaItem.getImageUrl(),
					mediaItem.getBigImageUrl(), MediaType.TRACK.toString(), 0,
					mediaItem.getAlbumId());
			Intent intent = new Intent(GoOfflineActivity.this,
					DownloadConnectingActivity.class);
			intent.putExtra(DownloadConnectingActivity.EXTRA_MEDIA_ITEM,
					(Serializable) trackMediaItem);
			startActivity(intent);

			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong.toString(),
					mediaItem.getTitle());
			reportMap.put(FlurryConstants.FlurryKeys.SourceSection.toString(),
					mFlurrySubSectionDescription);
			Analytics.logEvent(
					FlurryConstants.FlurryEventName.Download.toString(),
					reportMap);
		} else if (item.equals(getString(R.string.general_download_mp4))) {
			if (mApplicationConfigurations.getSaveOfflineMode())
				return;
			Intent intent = new Intent(GoOfflineActivity.this,
					DownloadConnectingActivity.class);
			intent.putExtra(DownloadConnectingActivity.EXTRA_MEDIA_ITEM,
					(Serializable) mediaItem);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);

			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong.toString(),
					mediaItem.getTitle());
			reportMap.put(FlurryConstants.FlurryKeys.SourceSection.toString(),
					FlurryConstants.FlurryKeys.SearchResult.toString());
			Analytics.logEvent(
					FlurryConstants.FlurryEventName.Download.toString(),
					reportMap);
		} else if (item
				.equals(getString(R.string.media_details_custom_dialog_long_click_add_to_queue))) {
			onAddToQueueSelected(mediaItem);
		} else if (item
				.equals(getString(R.string.media_details_custom_dialog_long_click_view_details))) {
			if (mApplicationConfigurations.getSaveOfflineMode())
				return;
			onShowDetails(mediaItem, false);
		}
	}

	public void onAddToQueueSelected(MediaItem mediaItem) {
		try {
			if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
				Track track = new Track(mediaItem.getId(),
						mediaItem.getTitle(), mediaItem.getAlbumName(),
						mediaItem.getArtistName(), mediaItem.getImageUrl(),
						mediaItem.getBigImageUrl(), mediaItem.getImages(),
						mediaItem.getAlbumId());
				List<Track> tracks = new ArrayList<Track>();
				tracks.add(track);
				mPlayerBarFragment.addToQueue(tracks, null, null);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	public void onShowDetails(MediaItem mediaItem, boolean playnow) {
		Intent intent = new Intent(GoOfflineActivity.this,
				MediaDetailsActivity.class);
		intent.putExtra(MediaDetailsActivity.EXTRA_MEDIA_ITEM,
				(Serializable) mediaItem);
		intent.putExtra(MediaDetailsActivity.FLURRY_SOURCE_SECTION,
				FlurryConstants.FlurrySourceSection.Search.toString());
		startActivity(intent);
	}
}
