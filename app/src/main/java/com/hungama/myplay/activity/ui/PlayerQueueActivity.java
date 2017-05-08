package com.hungama.myplay.activity.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.DataBase.CacheState;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.player.PlayerService.LoopMode;
import com.hungama.myplay.activity.player.PlayerService.PlayerStateListener;
import com.hungama.myplay.activity.player.PlayerService.State;
import com.hungama.myplay.activity.player.PlayerServiceBindingManager;
import com.hungama.myplay.activity.player.PlayerServiceBindingManager.ServiceToken;
import com.hungama.myplay.activity.player.PlayerUpdateWidgetService;
import com.hungama.myplay.activity.player.PlayingQueue;
import com.hungama.myplay.activity.ui.AppGuideActivityPlayerQueue.HelpLeftDrawer;
import com.hungama.myplay.activity.ui.AppGuideActivityPlayerQueue.HelpView;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment.PlayingEventListener;
import com.hungama.myplay.activity.ui.widgets.CustomAlertDialog;
import com.hungama.myplay.activity.ui.widgets.CustomCacheStateProgressBar;
import com.hungama.myplay.activity.ui.widgets.LanguageButtonLollipop;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.ApsalarEvent;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.hungama.myplay.activity.util.PicassoUtil.PicassoCallBack;
import com.hungama.myplay.activity.util.QuickActionPlayerQueue;
import com.hungama.myplay.activity.util.QuickActionPlayerQueueItemSelect;
import com.hungama.myplay.activity.util.QuickActionPlayerQueueItemSelect.OnContextItemSelectedListener;
import com.hungama.myplay.activity.util.ScreenLockStatus;
import com.hungama.myplay.activity.util.SwipeDismissList;
import com.hungama.myplay.activity.util.Utils;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.OnItemSelectedListener;
import com.mobeta.android.dslv.DragSortListView.RemoveListener;
import com.mobeta.android.dslv.SimpleDragSortCursorAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerQueueActivity extends ActionBarActivity implements
		OnClickListener, PlayingEventListener, PlayerStateListener,
		ServiceConnection {
//::::::::::::::::onCreate::::::::::::::::::
	// @Override
	// protected NavigationItem getNavigationItem() {
	// // TODO Auto-generated method stub
	// return null;
	// }

	public static final String TAG = "PlayerQueueFragment";
	// private static PlayerQueueFragment mInstance;

	// private FragmentManager mFragmentManager;

	// private PlayerBarFragment mPlayerBarFragment;
	private QuickActionPlayerQueue quickactionPlayerQueue;
	private TextView mTextRemoveHint, mTextCancelRemoveState,
			mTextDeleteSelected;// mTextEdit,
	private LanguageTextView mTextTitle;
	private ImageButton mButtonOptions;
	// private ImageView mIvSelectAll;
	private CheckBox mChkboxSelectAll;
	private RelativeLayout mRemoveOptionsBar;

	private int mTileSize;
	// private GridView mTilesGridView;
	private DragSortListView mTrackListView;
	// private DynamicListView mTrackListView;

	// private PlayerQueueAdapter mQueueAdapter;
	// private SearchResultsAdapter mQueueAdapter;
	private MAdapter mQueueAdapter;
	// private MyListAdapter mQueueAdapter;

	private List<Track> mTracks;
	private List<Boolean> mTrackRemoveState;

	public static boolean isEditMode;

	// private OnPlayerQueueClosedListener mOnPlayerQueueClosedListener;
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;

	private Button buttonDone;
	private static final int ACTION_SAVE_AS_PLAYLIST = 1001;
	private static final int ACTION_SAVE_ALL_OFFLINE = 1002;
	private static final int ACTION_LOAD_PLAYLIST = 1003;
	private static final int ACTION_LOAD_FAVORITES = 1004;

	private PicassoUtil picasso;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		mMenu = menu;
		inflater.inflate(R.menu.main_menu_queue_more, menu);
		return true;
	}

	// ======================================================
	// FRAGMENT'S LIFE CYCLE.
	// ======================================================
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		int itemId = item.getItemId();
		int totalSelected = 0;
		if (mTrackRemoveState != null && mTrackRemoveState.size() > 0) {
			for (int i = 0; i < mTrackRemoveState.size(); i++) {
				if (mTrackRemoveState.get(i))
					totalSelected = totalSelected + 1;
			}
		}
		if (itemId == R.id.queue_delete) {
			if (mSwipeList != null)
				mSwipeList.dismissPendingUndoMsg();
			if ((mTrackRemoveState != null && totalSelected == mTrackRemoveState.size()) || totalSelected == 0) {
				displyClearDialog();
			} else {
				displyClearSelectedSongsDialog();
			}
		} else if (itemId == R.id.queue_more) {
			String menuItems[];
			if (totalSelected == 0) {
				String shuffle = "";
				if (PlayerService.service!=null && PlayerService.service.isShuffling()) {
					shuffle=Utils.getMultilanguageText(getActivity(),
							getResources().getString(R.string.txt_shuffle));
					shuffle+=" [";
					shuffle+=Utils.getMultilanguageText(getActivity(),
							getResources().getString(R.string.txt_on));
					shuffle+="]";

				} else {
					shuffle=Utils.getMultilanguageText(getActivity(),
							getResources().getString(R.string.txt_shuffle));
					shuffle+=" [";
					shuffle+=Utils.getMultilanguageText(getActivity(),
							getResources().getString(R.string.txt_off));
					shuffle+="]";

				}
				LoopMode mode = PlayerService.service!=null ? PlayerService.service.getLoopMode() : LoopMode.OFF;
				String repeate = "";
				if (mode == LoopMode.ON) {
					repeate=Utils.getMultilanguageText(getActivity(),
							getResources().getString(R.string.txt_repeat));
					repeate+=" [";
					repeate+=Utils.getMultilanguageText(getActivity(),
							getResources().getString(R.string.txt_on));
					repeate+="]";

				} else if (mode == LoopMode.OFF) {
					repeate=Utils.getMultilanguageText(getActivity(),
							getResources().getString(R.string.txt_repeat));
					repeate+=" [";
					repeate+=Utils.getMultilanguageText(getActivity(),
							getResources().getString(R.string.txt_off));
					repeate+="]";

				} else {
					repeate=Utils.getMultilanguageText(getActivity(),
							getResources().getString(R.string.txt_repeat));
					repeate+=" [";
					repeate+="1";
					repeate+="]";
				}

//				if (mode == LoopMode.ON) {
//					// sets any loop mode - OFF.
//					toastMessage = Utils.getMultilanguageText(this, getResources()
//							.getString(R.string.player_loop_mode_off));
//					PlayerService.service.setLoopMode(LoopMode.OFF);
//
//				} else if (mode == LoopMode.OFF) {
//					// sets any loop mode - REPLAY SONG.
//					toastMessage = Utils.getMultilanguageText(this, getResources()
//							.getString(R.string.player_loop_mode_replay_song));
//					PlayerService.service.setLoopMode(LoopMode.REAPLAY_SONG);
//
//				} else {
//					// sets any loop mode - ON.
//					toastMessage = getResources().getString(
//							R.string.player_loop_mode_on);
//					PlayerService.service.setLoopMode(LoopMode.ON);
//				}
				menuItems = new String[] {
						repeate,
						shuffle,
						Utils.getMultilanguageText(
								getActivity(),
								getResources().getString(
										R.string.general_download_title)) };
			} else if (totalSelected != mTrackRemoveState.size()) {
				menuItems = new String[] {
						Utils.getMultilanguageText(
								getActivity(),
								getResources().getString(
										R.string.select_all_player_queue)),
						Utils.getMultilanguageText(
								getActivity(),
								getResources().getString(
										R.string.cancel_button_text)) };
			} else {
				menuItems = new String[] { Utils.getMultilanguageText(
						getActivity(),
						getResources().getString(
								R.string.deselect_all_player_queue)) };
			}
			try {
				final int totalSelected1 = totalSelected;
				QuickActionPlayerQueueItemSelect quickaction;
				quickaction = new QuickActionPlayerQueueItemSelect(
						getActivity(), menuItems, true);
				quickaction
						.setOnContextItemSelectedListener(new OnContextItemSelectedListener() {

							@Override
							public void onItemSelectedPosition(int id) {
								// TODO Auto-generated method stub
								if (totalSelected1 == 0) {
									if (id == 0) {
										// Repeat
										setRepeat();
									} else if (id == 1) {
										// Shuffle
										setShuffle();
									} else if (id == 2) {
										// Save queue offline
										saveAllOffline();
									} else if (id == 3) {
										// Save queue offline
										selectUnSelectAllSongs(totalSelected1);
									} else if (id == 4) {
										cancelClick();
									}
								} else if (totalSelected1 != mTrackRemoveState
										.size()) {
									if (id == 0) {
										// Save queue offline
										selectUnSelectAllSongs(totalSelected1);
									} else if (id == 1) {
										cancelClick();
									}
								} else {
									cancelClick();
								}
							}

							@Override
							public void onItemSelected(String item) {
								Map<String, String> reportMap1 = new HashMap<String, String>();
								reportMap1
										.put(FlurryConstants.FlurryKeys.SourceSection
												.toString(),
												FlurryConstants.FlurryKeys.PlayerQueue
														.toString());
								reportMap1
										.put(FlurryConstants.FlurryKeys.OptionSelected
												.toString(), item);
								Analytics
										.logEvent(
												FlurryConstants.FlurryEventName.ThreeDotsClicked
														.toString(), reportMap1);

							}
						});
				quickaction.show(findViewById(itemId));
				item.setEnabled(false);
				quickaction
						.setOnDismissListener(new QuickActionPlayerQueueItemSelect.OnDismissListener() {
							@Override
							public void onDismiss() {
								item.setEnabled(true);
							}
						});
			} catch (Exception e) {
				Logger.printStackTrace(e);

			}

			// onMoreBtnClick();
		} else {
			if (totalSelected == 0)
				finish();
			else
				cancelClick();
		}
		return false;
	}

	private void cancelClick() {
		for (int i = 0; i < mTrackRemoveState.size(); i++)
			mTrackRemoveState.set(i, false);
		if (mQueueAdapter != null)
			mQueueAdapter.notifyDataSetChanged();
		mRemoveOptionsBar.setVisibility(View.GONE);
		setTitleText(true);
		mTextRemoveHint.setVisibility(View.VISIBLE);
		// mTextEdit.setVisibility(View.VISIBLE);
		// mIvSelectAll.setVisibility(View.INVISIBLE);
		mChkboxSelectAll.setVisibility(View.INVISIBLE);
		mTextCancelRemoveState.setVisibility(View.GONE);
		mTextDeleteSelected.setVisibility(View.GONE);
	}

	private Activity getActivity() {
		return PlayerQueueActivity.this;
	}

	View rootView;
	CacheStateReceiver cacheStateReceiver;
	protected Toolbar mToolbar;
	private ActionBar mActionBar;
	protected Menu mMenu;

	private ServiceToken mServiceToken;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// System.out.println(" :::-------onCreate--------- " +
		// System.currentTimeMillis());
		// setOverlayAction();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player_queue);

		// // getDrawerLayout();
		mApplicationConfigurations = ApplicationConfigurations
				.getInstance(PlayerQueueActivity.this);
		rootView = (RelativeLayout) findViewById(R.id.main_player_queue);
		picasso = PicassoUtil.with(this);

		initializeUserControls(rootView);
		//
		// Utils.clearCache();
		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		if (PlayerService.service != null) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar1);
					pb.setVisibility(View.GONE);
					initializeComponents();
                    showPlayerQueueActionBar();
                    if (mQueueAdapter != null)
                        mQueueAdapter.notifyDataSetChanged();
                    setTitleText(false);
				}
			}, 500);
			// handler.postDelayed(run, 500);
		}
		registerReceivers();
		//handler.postDelayed(run, 500);

		mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		if (mToolbar != null)
			setSupportActionBar(mToolbar);

		// mToolbar.inflateMenu(R.menu.menu_main_actionbar);
		// // Set an OnMenuItemClickListener to handle menu item clicks
		// mToolbar.setOnMenuItemClickListener(
		// new Toolbar.OnMenuItemClickListener() {
		// @Override
		// public boolean onMenuItemClick(MenuItem item) {
		// // Handle the menu item
		// return true;
		// }
		// });

		try {

			mActionBar = getSupportActionBar();
			mActionBar.setIcon(R.drawable.icon_actionbar_logo);

			// mActionBar.setBackgroundDrawable(getResources().getDrawable(
			// R.drawable.background_actionbar));
		} catch (Error e) {
			System.gc();
			System.runFinalization();
			System.gc();
			mActionBar = getSupportActionBar();
			mActionBar.setIcon(R.drawable.icon_actionbar_logo);
			// mActionBar.setBackgroundDrawable(getResources().getDrawable(
			// R.drawable.background_actionbar));
		}
		mServiceToken = PlayerServiceBindingManager.bindToService(
				getActivity(), this);

//		ApsalarEvent.postEvent(this, ApsalarEvent.PLAYER_QUEUE_ACCESSED);
	}

	private void registerReceivers() {
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

	Handler handler = new Handler();
	Runnable run = new Runnable() {

		@Override
		public void run() {
			showPlayerQueueActionBar();
			if (mQueueAdapter != null)
				mQueueAdapter.notifyDataSetChanged();
			setTitleText(false);
		}
	};

	public void showPlayerQueueActionBar() {

		try {

			Utils.setActionBarTitle(PlayerQueueActivity.this, mActionBar,
					getResources().getString(R.string.application_name));
			mMenu.clear();
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.main_menu_queue_more, mMenu);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void onMoreBtnClick() {

		AlertDialog.Builder builderSingle = new AlertDialog.Builder(
				getActivity());
		builderSingle.setTitle("Player Queue");
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				getActivity(), android.R.layout.select_dialog_item);
		arrayAdapter.add("Repeat");
		arrayAdapter.add("Shuffle");
		arrayAdapter.add("Save queue offline");

		builderSingle.setAdapter(arrayAdapter,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// String strName = arrayAdapter.getItem(which);
						if (which == 0) {
							// Repeat
							setRepeat();
						} else if (which == 1) {
							// Shuffle
							setShuffle();
						} else {
							// Save queue offline
							saveAllOffline();
						}
					}
				});
		builderSingle.show();
	}

	public void setShuffle() {
		try {
			Analytics.logEvent(FlurryConstants.FlurryAllPlayer.Shuffle
					.toString());

			String toastMessage = null;
			if (PlayerService.service.isShuffling()) {
				// sets any loop mode - OFF.
				toastMessage = Utils.getMultilanguageText(this, getResources()
						.getString(R.string.player_shuffle_mode_off));
				PlayerService.service.stopShuffle();

			} else {
				// sets any loop mode - REPLAY SONG.
				toastMessage = Utils.getMultilanguageText(this, getResources()
						.getString(R.string.player_shuffle_mode_on));
				PlayerService.service.startShuffle();

			}
			// shows a message to indicate the user.
			Utils.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT)
					.show();
			// updates the next / prev buttons to the new situation.
			mTracks = PlayerService.service.getPlayingQueue();
			initTrackRemoveState();
			mQueueAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	public void setRepeat() {
		try {
			Analytics.logEvent(FlurryConstants.FlurryAllPlayer.OnLoop
					.toString());

			// gets the new state of the button.
			String toastMessage = null;
			LoopMode mode = PlayerService.service.getLoopMode();

			if (mode == LoopMode.ON) {
				// sets any loop mode - OFF.
				toastMessage = Utils.getMultilanguageText(this, getResources()
						.getString(R.string.player_loop_mode_off));
				PlayerService.service.setLoopMode(LoopMode.OFF);

			} else if (mode == LoopMode.OFF) {
				// sets any loop mode - REPLAY SONG.
				toastMessage = Utils.getMultilanguageText(this, getResources()
						.getString(R.string.player_loop_mode_replay_song));
				PlayerService.service.setLoopMode(LoopMode.REAPLAY_SONG);

			} else {
				// sets any loop mode - ON.
				toastMessage = getResources().getString(
						R.string.player_loop_mode_on);
				PlayerService.service.setLoopMode(LoopMode.ON);
			}

			Utils.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT)
					.show();
			mTracks = PlayerService.service.getPlayingQueue();
			initTrackRemoveState();
			mQueueAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	// private void setRepeat() {
	// mPlayerBarFragment.setRepeat();
	// mTracks = mPlayerBarFragment.getCurrentPlayingList();
	// initTrackRemoveState();
	// mQueueAdapter.notifyDataSetChanged();
	// }
	//
	// private void setShuffle() {
	// mPlayerBarFragment.setShuffle();
	// mTracks = mPlayerBarFragment.getCurrentPlayingList();
	// initTrackRemoveState();
	// mQueueAdapter.notifyDataSetChanged();
	// }

	private void saveAllOffline() {
		if (!isHandledActionOffline(ACTION_SAVE_ALL_OFFLINE)) {
			ApplicationConfigurations appConfig = ApplicationConfigurations
					.getInstance(getActivity());
			// if(appConfig.getSaveOfflineAutoSaveMode()){
			// Toast.makeText(getActivity(),
			// R.string.player_queue_error_auto_save_on,
			// Toast.LENGTH_SHORT).show();
			// } else{
			if (mTracks != null && mTracks.size() > 0) {
				boolean isAllOffline = true;
				for (Track track : mTracks) {
					if (DBOHandler.getTrackCacheState(getActivity(),
							"" + track.getId()) != CacheState.CACHED) {
						isAllOffline = false;
						break;
					}
				}
				if (isAllOffline) {
					Utils.makeText(
							getActivity(),
							getString(R.string.player_queue_error_all_available_offline),
							Toast.LENGTH_SHORT).show();
				} else if (appConfig.getSaveOfflineAutoSaveMode()) {
					Utils.makeText(
							getActivity(),
							getString(R.string.player_queue_error_auto_save_on),
							Toast.LENGTH_SHORT).show();
				} else {
					CacheManager.saveAllTracksOfflineAction(getActivity(),
							mTracks);
					Utils.saveAllOfflineFlurryEvent(getActivity(),
							FlurryConstants.FlurryCaching.PlayerQueue
									.toString(), mTracks);
				}
				// }
				// if(mTracks!=null && mTracks.size()>0)
				// CacheManager.saveAllTracksOfflineAction(getActivity(),
				// mTracks);
			}
		}
	}

	// @Override
	// public void onStart() {
	// super.onStart();
	private void initializeComponents() {
		// System.out.println(" :::-------initializeComponents--------- " +
		// System.currentTimeMillis());
		Logger.i("Time", "Time:1");
		// if (mPlayerBarFragment == null) {
		// mPlayerBarFragment = ((MainActivity) getActivity()).getPlayerBar();
		// }
		Logger.i("Time", "Time:2");
		if (PlayerService.service != null)
			mTracks = PlayerService.service.getPlayingQueue();
		// mPlayerBarFragment.getCurrentPlayingList();
		if (mTracks == null) {
			mTracks = new ArrayList<Track>();
		}
		Logger.i("Time", "Time:3");
		initTrackRemoveState();
		Logger.i("Time", "Time:4");
		// if (mQueueAdapter == null) {
		// // mQueueAdapter = new PlayerQueueAdapter();
		// // mTilesGridView.setAdapter(mQueueAdapter);
		// mQueueAdapter = new SearchResultsAdapter();
		// mTrackListView.setAdapter(mQueueAdapter);
		// } else {
		// mQueueAdapter.notifyDataSetChanged();
		// }

		String[] cols = { "name" };
		int[] ids = { R.id.player_queue_line_top_text };
		mQueueAdapter = new MAdapter(getActivity().getApplicationContext(),
				R.layout.list_item_player_queue_line, null, cols, ids, 0);

		mTrackListView.setAdapter(mQueueAdapter);
		Logger.i("Time", "Time:5");
		setUpSwipeView();
		Logger.i("Time", "Time:6");
		mTrackListView.setDragEnabled(true);
		mTrackListView.setDropListener(new DragSortListView.DropListener() {
			@Override
			public void drop(int from, int to) {
				if (from != to) {
					if (mSwipeList != null)
						mSwipeList.dismissPendingUndoMsg();
					trackDragAndDrop(from, to);
					if (PlayerService.service != null)
						mTracks = PlayerService.service.getPlayingQueue();
					initTrackRemoveState();
					Logger.i("Notify",
							"Notify::::::::::  setDropListener Count:"
									+ mTracks.size());
					mQueueAdapter.notifyDataSetChanged();
					Intent in = new Intent(PlayerService.ACTION_REMOVED_TRACK);
					in.putExtra("removedTrackid", "0");
					sendBroadcast(in);
				}
			}
		});
		if (PlayerService.service != null)
			mTrackListView.setSelection(PlayerService.service
					.getCurrentPlayingTrackPosition());

		mTrackListView.setRemoveListener(new RemoveListener() {

			@Override
			public void remove(final int which) {
				// Logger.i("Notify", "Notify::::::::::  setRemoveListener");
				if (mSwipeList != null) {
					mSwipeList.dismissPendingUndoMsg();
					mSwipeList.dismissCall(mTrackListView, which);
				}
				// new Handler().postDelayed(new Runnable() {
				// @Override
				// public void run() {
				// removeFromQueueList(which);
				// }
				// }, 200);
			}
		});

		mTrackListView.setItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(int pos) {

				int firstPosition = mTrackListView.getFirstVisiblePosition()
						- mTrackListView.getHeaderViewsCount();
				int wantedChild = pos - firstPosition;
				if (wantedChild < 0
						|| wantedChild >= mTrackListView.getChildCount()) {
					Logger.w(TAG,
							"Unable to get view for desired position, because it's not being displayed on screen.");
					return;
				}

				View tile = mTrackListView.getChildAt(wantedChild);

				// View tile = mTrackListView.getChildAt(pos);
				// View tile = (View) wantedView;
				RelativeLayout rlChild = (RelativeLayout) tile
						.findViewById(R.id.relativelayout_player_queue_line);
				// start play from beginning or pause
				// RelativeLayout tile = (RelativeLayout) view;//
				//
				// ViewHolder1 viewHolder = (ViewHolder1) tile
				// .getTag(R.id.view_tag_view_holder);
				ViewHolder1 viewHolder = (ViewHolder1) rlChild
						.getTag(R.id.view_tag_view_holder);
				int position = (Integer) rlChild.getTag(R.id.view_tag_position);

				handlePlayClick(position, viewHolder);
			}
		});

		// registers for playing tracks events.

		Logger.i("Time", "Time:7");
		// FlurryAgent.setContinueSessionMillis(Constants.FLURRY_SESSION_LENGTH);
		// FlurryAgent.onStartSession(getActivity(),
		// getString(R.string.flurry_app_key));
		Analytics.startSession(getActivity());
		Analytics.onPageView();
		Analytics.logEvent(FlurryConstants.FlurryAllPlayer.PlayerQueueViewed
				.toString());
		Logger.i("Time", "Time:8");
		// System.out.println(" :::-------initializeComponents---------1 " +
		// System.currentTimeMillis());
	}

	public void trackDragAndDrop(int from, int to) {
		if (PlayerService.service != null) {
			PlayerService.service.trackDragAndDrop(from, to);
		}
	}

	public void updateNotificationForOffflineMode() {
		if (PlayerService.service != null)
			PlayerService.service.updateNotificationForOffflineMode();
	}

	private boolean cacheStateUpdated = false;

	private class CacheStateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			Logger.s("========================= cachestateupdatereceived ========"
					+ arg1.getAction());
			if (arg1.getAction()
					.equals(CacheManager.ACTION_CACHE_STATE_UPDATED)
					|| arg1.getAction()
							.equals(CacheManager.ACTION_TRACK_CACHED)) {
				if (mQueueAdapter != null) {
					mQueueAdapter.notifyDataSetChanged();
					if (playerStateReceiver == null) {
						cacheStateUpdated = true;
					}
				}
			} else if (arg1.getAction().equals(
					CacheManager.ACTION_UPDATED_CACHE)) {
				if (mQueueAdapter != null) {
					mQueueAdapter.notifyDataSetChanged();
					if (playerStateReceiver == null) {
						cacheStateUpdated = true;
					}
				}
			} else if (arg1.getAction().equals(
					CacheManager.ACTION_VIDEO_CACHE_STATE_UPDATED)
					|| arg1.getAction().equals(
							CacheManager.ACTION_VIDEO_TRACK_CACHED)) {
				if (mQueueAdapter != null) {
					mQueueAdapter.notifyDataSetChanged();
					if (playerStateReceiver == null) {
						cacheStateUpdated = true;
					}
				}
			} else if (arg1.getAction().equals(
					CacheManager.ACTION_VIDEO_UPDATED_CACHE)) {
				if (mQueueAdapter != null) {
					mQueueAdapter.notifyDataSetChanged();
					if (playerStateReceiver == null) {
						cacheStateUpdated = true;
					}
				}
			}
		}
	}

	@Override
	public void onPause() {

		// if (isRemoving()) {
		// if (mOnPlayerQueueClosedListener != null) {
		// mOnPlayerQueueClosedListener.onPlayerQueueClosed();
		// }
		// }
		try {
			if (playerStateReceiver != null) {
				getActivity().unregisterReceiver(playerStateReceiver);
				playerStateReceiver = null;
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		super.onPause();
	}

	@Override
	protected void onUserLeaveHint() {
		ScreenLockStatus.getInstance(getBaseContext()).onStop(true, this);
		Logger.s("onUserLeaveHint MainActivity");
		super.onUserLeaveHint();
	}

	@Override
	public void onStop() {
		ScreenLockStatus.getInstance(getBaseContext()).onStop();

		// xtpl
		Map<String, String> reportMap = new HashMap<String, String>();
		reportMap
				.put(FlurryConstants.FlurryMediaDetailActions.ActionTaken
						.toString(), FlurryConstants.FlurryAllPlayer.Back
						.toString());
		Analytics.logEvent(
				FlurryConstants.FlurryAllPlayer.PlayerQueueViewed.toString(),
				reportMap);
		// xtpl

		// unregisters for playing tracks events.
		// mPlayerBarFragment.setPlayingEventListener(null);
		super.onStop();
		Analytics.onEndSession(getActivity());
	}

	private void displyClearSelectedSongsDialog() {
		final CustomAlertDialog clearDialogBuilder = new CustomAlertDialog(
				getActivity());
		// clearDialogBuilder.setTitle(Utils.TEXT_EMPTY);
		clearDialogBuilder
				.setMessage(Utils
						.getMultilanguageText(
								getActivity(),
								getResources()
										.getString(
												R.string.player_queue_message_confirm_clear_selected_songs)));
		clearDialogBuilder.setCancelable(false);
		// sets the OK button.
		clearDialogBuilder
				.setPositiveButton(
						Utils.getMultilanguageText(
								getActivity(),
								getResources()
										.getString(
												R.string.player_queue_message_confirm_clear_selected_songs_ok)),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								deleteSelectedAudios();
							}
						});
		// sets the Cancel button.
		clearDialogBuilder
				.setNegativeButton(
						Utils.getMultilanguageText(
								getActivity(),
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

	private void displyClearDialog() {
		final CustomAlertDialog clearDialogBuilder = new CustomAlertDialog(
				getActivity());
		// clearDialogBuilder.setTitle(Utils.TEXT_EMPTY);
		clearDialogBuilder.setMessage(Utils.getMultilanguageText(
				getActivity(),
				getResources().getString(
						R.string.player_queue_message_confirm_clear_all)));
		clearDialogBuilder.setCancelable(false);
		// sets the OK button.
		clearDialogBuilder.setPositiveButton(Utils.getMultilanguageText(
				getActivity(),
				getResources().getString(
						R.string.player_queue_message_confirm_clear_all_ok)),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						clearQueue();

					}
				});
		// sets the Cancel button.
		clearDialogBuilder
				.setNegativeButton(
						Utils.getMultilanguageText(
								getActivity(),
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

	@Override
	public void onClick(View view) {
		int viewId = view.getId();

		// FragmentTransaction fragmentTransaction = mFragmentManager
		// .beginTransaction();
		// fragmentTransaction
		// .setCustomAnimations(R.anim.slide_and_show_top_enter,
		// R.anim.slide_and_show_top_exit,
		// R.anim.slide_and_show_top_enter,
		// R.anim.slide_and_show_top_exit);
		if (viewId == R.id.ivBackArrow) {
			// getFragmentManager().popBackStack();
			finish();
		} else if (viewId == R.id.player_queue_title_bar_button_clear) {
			displyClearDialog();
		} else if (viewId == R.id.player_queue_title_bar_button_more) {
			onMoreBtnClick();
		} else if (viewId == R.id.player_queue_title_bar_button_options) {
			// try {
			// if (view.isSelected()) {
			// // closes the option list.
			// view.setSelected(false);
			// view.setBackgroundResource(0);
			//
			// OptionsFragment optionsFragment = (OptionsFragment)
			// mFragmentManager
			// .findFragmentByTag(OptionsFragment.TAG);
			// if (optionsFragment != null) {
			// fragmentTransaction.remove(optionsFragment);
			// fragmentTransaction.commit();
			// }
			//
			// //
			// FlurryAgent.logEvent(FlurryConstants.FlurryAllPlayer.None.toString());
			// // xtpl
			// Map<String, String> reportMap = new HashMap<String, String>();
			// reportMap
			// .put(FlurryConstants.FlurryMediaDetailActions.ActionTaken
			// .toString(),
			// FlurryConstants.FlurryAllPlayer.None
			// .toString());
			// Analytics.logEvent(
			// FlurryConstants.FlurryAllPlayer.PlayerQueueViewed
			// .toString(), reportMap);
			// // xtpl
			//
			// } else {
			// // opens the option list.
			// view.setSelected(true);
			// view.setBackgroundResource(R.color.black);
			//
			// OptionsFragment optionsFragment = new OptionsFragment();
			//
			// fragmentTransaction.add(
			// R.id.player_queue_content_container,
			// optionsFragment, OptionsFragment.TAG);
			// fragmentTransaction.commit();
			// }
			// } catch (Exception e) {
			// Logger.printStackTrace(e);
			// }
		} else if (viewId == R.id.player_queue_options_save_as_playlist
				|| viewId == R.id.player_queue_options_clear_queue
				|| viewId == R.id.player_queue_options_save_all_offline) {
			//
			// // closes the option list.
			// mButtonOptions.setSelected(false);
			// mButtonOptions.setBackgroundResource(0);
			// try {
			// OptionsFragment optionsFragment = (OptionsFragment)
			// mFragmentManager
			// .findFragmentByTag(OptionsFragment.TAG);
			// if (optionsFragment != null) {
			// fragmentTransaction.remove(optionsFragment);
			// fragmentTransaction.commit();
			// }
			// } catch (Exception e) {
			// Logger.e(getClass().getName() + ":224", e.toString());
			// }
			//
			// if (viewId == R.id.player_queue_options_save_as_playlist) {
			// if (!isHandledActionOffline(ACTION_SAVE_AS_PLAYLIST)) {
			// if (isNetworkAvailable(view)) {
			// try {
			// List<Track> tracks = mPlayerBarFragment
			// .getCurrentPlayingList();
			// if (tracks == null || tracks.size() == 0) {
			// Utils.makeText(
			// getActivity(),
			// getString(R.string.player_queue_error_add_songs_to_save),
			// Toast.LENGTH_SHORT).show();
			// return;
			// }
			// boolean isFromLoadMenu = false;
			// PlaylistDialogFragment playlistDialogFragment =
			// PlaylistDialogFragment
			// .newInstance(
			// tracks,
			// isFromLoadMenu,
			// FlurryConstants.FlurryPlaylists.PlayerQueue
			// .toString());
			//
			// playlistDialogFragment
			// .setOnPlaylistPerformActionListener(new
			// OnPlaylistPerformActionListener() {
			// @Override
			// public void onSuccessed() {
			// }
			//
			// @Override
			// public void onFailed() {
			// }
			//
			// @Override
			// public void onCanceled() {
			// }
			// });
			// playlistDialogFragment.show(mFragmentManager,
			// PlaylistDialogFragment.FRAGMENT_TAG);
			// } catch (Exception e) {
			// Logger.printStackTrace(e);
			// }
			// }
			// }
			// } else if (viewId == R.id.player_queue_options_clear_queue) {
			// diaplyClearDialog();
			// }
			// // else if (viewId == R.id.player_queue_options_load_playlist) {
			// //
			// // if (!isHandledActionOffline(ACTION_LOAD_PLAYLIST)) {
			// // if (isNetworkAvailable(view)) {
			// // try {
			// // List<Track> playingQueue = mPlayerBarFragment
			// // .getCurrentPlayingList();
			// //
			// // PlaylistDialogFragment selectPlaylistDialog =
			// // PlaylistDialogFragment
			// // .newInstance(
			// // playingQueue,
			// // true,
			// // FlurryConstants.FlurryPlaylists.PlayerQueue
			// // .toString());
			// //
			// // selectPlaylistDialog
			// // .setOnLoadMenuItemOptionSelectedListener(new
			// // OnLoadMenuItemOptionSelectedListener() {
			// //
			// // @Override
			// // public void onLoadPlaylistFromDialogSelected(
			// // List<Track> tracks) {
			// // if (!Utils.isListEmpty(tracks)) {
			// // // adds the playlist's tracks to
			// // // the playing
			// // // queue.
			// // mPlayerBarFragment.addToQueue(tracks,
			// // null, null);
			// //
			// // // updates the grid.
			// // mTracks = mPlayerBarFragment
			// // .getCurrentPlayingList();
			// // initTrackRemoveState();
			// // mTextTitle.setText(Utils
			// // .getMultilanguageText(
			// // getActivity(),
			// // getResources()
			// // .getString(
			// // R.string.player_queue_title_1))
			// // + " ("
			// // + mTracks.size()
			// // + ")");
			// // // mTextTitle.setText(title);
			// // mQueueAdapter
			// // .notifyDataSetChanged();
			// //
			// // }
			// // }
			// //
			// // @Override
			// // public void onLoadMenuTop10Selected(
			// // List<Track> topTenMediaItems) {
			// // }
			// //
			// // @Override
			// // public void onLoadMenuRadioSelected() {
			// // }
			// //
			// // @Override
			// // public void onLoadMenuMyPlaylistSelected() {
			// // }
			// //
			// // @Override
			// // public void onLoadMenuMyFavoritesSelected() {
			// // }
			// //
			// // @Override
			// // public void onLoadMenuMyOfflineSongs() {
			// // }
			// // });
			// // selectPlaylistDialog.show(mFragmentManager,
			// // "PlaylistDialogFragment");
			// //
			// // //
			// //
			// FlurryAgent.logEvent(FlurryConstants.FlurryAllPlayer.LoadPlaylist.toString());
			// // // xtpl
			// // Map<String, String> reportMap = new HashMap<String, String>();
			// // reportMap
			// // .put(FlurryConstants.FlurryMediaDetailActions.ActionTaken
			// // .toString(),
			// // FlurryConstants.FlurryAllPlayer.LoadPlaylist
			// // .toString());
			// // FlurryAgent
			// // .logEvent(
			// // FlurryConstants.FlurryAllPlayer.PlayerQueueViewed
			// // .toString(), reportMap);
			// // // xtpl
			// // } catch (Exception e) {
			// // Logger.printStackTrace(e);
			// // }
			// // }
			// // }
			// // }
			// // else if (viewId == R.id.player_queue_options_load_favorites) {
			// // if (!isHandledActionOffline(ACTION_LOAD_FAVORITES)) {
			// // if (isNetworkAvailable(view)) {
			// // // shows the favorite activity.
			// // Intent favoritesActivityIntent = new Intent(
			// // getActivity().getApplicationContext(),
			// // FavoritesActivity.class);
			// // startActivity(favoritesActivityIntent);
			// //
			// // // closes the player bar content.
			// // mPlayerBarFragment.closeContent();
			// //
			// // //
			// //
			// FlurryAgent.logEvent(FlurryConstants.FlurryAllPlayer.LoadFavotrites.toString());
			// // // xtpl
			// // Map<String, String> reportMap = new HashMap<String, String>();
			// // reportMap
			// // .put(FlurryConstants.FlurryMediaDetailActions.ActionTaken
			// // .toString(),
			// // FlurryConstants.FlurryAllPlayer.LoadFavotrites
			// // .toString());
			// // FlurryAgent
			// // .logEvent(
			// // FlurryConstants.FlurryAllPlayer.PlayerQueueViewed
			// // .toString(), reportMap);
			// // // xtpl
			// // }
			// // }
			// // }
			// // else if (viewId == R.id.player_queue_options_exit_queue) {
			// // // closes the player bar content.
			// // mPlayerBarFragment.closeContent();
			// // // xtpl
			// // Map<String, String> reportMap = new HashMap<String, String>();
			// // reportMap.put(
			// // FlurryConstants.FlurryMediaDetailActions.ActionTaken
			// // .toString(),
			// // FlurryConstants.FlurryAllPlayer.Back.toString());
			// // FlurryAgent.logEvent(
			// // FlurryConstants.FlurryAllPlayer.PlayerQueueViewed
			// // .toString(), reportMap);
			// // // xtpl
			// // }
			// else if (viewId == R.id.player_queue_options_save_all_offline) {
			// // for(Track track : mTracks){
			// // saveTrackOffline(track);
			// // }
			// if (!isHandledActionOffline(ACTION_SAVE_ALL_OFFLINE)) {
			// ApplicationConfigurations appConfig = new
			// ApplicationConfigurations(
			// getActivity());
			// // if(appConfig.getSaveOfflineAutoSaveMode()){
			// // Toast.makeText(getActivity(),
			// // R.string.player_queue_error_auto_save_on,
			// // Toast.LENGTH_SHORT).show();
			// // } else{
			// if (mTracks != null && mTracks.size() > 0) {
			// boolean isAllOffline = true;
			// for (Track track : mTracks) {
			// if (DBOHandler.getTrackCacheState(getActivity(), ""
			// + track.getId()) != CacheState.CACHED) {
			// isAllOffline = false;
			// break;
			// }
			// }
			// if (isAllOffline) {
			// Utils.makeText(
			// getActivity(),
			// getString(R.string.player_queue_error_all_available_offline),
			// Toast.LENGTH_SHORT).show();
			// } else if (appConfig.getSaveOfflineAutoSaveMode()) {
			// Utils.makeText(
			// getActivity(),
			// getString(R.string.player_queue_error_auto_save_on),
			// Toast.LENGTH_SHORT).show();
			// } else {
			// CacheManager.saveAllTracksOfflineAction(
			// getActivity(), mTracks);
			// Utils.saveAllOfflineFlurryEvent(getActivity(),
			// FlurryConstants.FlurryCaching.PlayerQueue
			// .toString(), mTracks);
			// }
			// // }
			// // if(mTracks!=null && mTracks.size()>0)
			// // CacheManager.saveAllTracksOfflineAction(getActivity(),
			// // mTracks);
			// }
			// }
			// }
		} else if (viewId == R.id.player_queue_title_bar_button_done) {
			isEditMode = !isEditMode;
			if (mQueueAdapter != null)
				mQueueAdapter.notifyDataSetChanged();
			if (!isEditMode) {
				mTrackListView.setDragEnabled(false);
				buttonDone.setVisibility(View.GONE);
				mButtonOptions.setVisibility(View.VISIBLE);
				// String title = getResources().getString(
				// R.string.player_queue_title_1);
				//
				// Utils.SetMultilanguageTextOnTextView(getActivity(),
				// mTextTitle,
				// title);
				// mTextTitle.append(" (" + mTracks.size() + ")");
				// mTextTitle.setText(Utils
				// .getMultilanguageText(getActivity(), getResources()
				// .getString(R.string.player_queue_title_1))
				// + " (" + mTracks.size() + ")");
				setTitleText(false);
				// mTextTitle.setText(title);
				// mRemoveOptionsBar.setVisibility(View.VISIBLE);
				mRemoveOptionsBar.setVisibility(View.GONE);
			}
		}
		// else if (viewId == R.id.player_queue_textview_edit) {
		// isEditMode = !isEditMode;
		// if (mQueueAdapter != null)
		// mQueueAdapter.notifyDataSetChanged();
		// if (isEditMode) {
		// mTrackListView.setDragEnabled(true);
		// buttonDone.setVisibility(View.VISIBLE);
		// mButtonOptions.setVisibility(View.GONE);
		// String title = "Edit Player Queue";
		// Utils.SetMultilanguageTextOnTextView(getActivity(), mTextTitle,
		// title);
		// // mTextTitle.setText(title);
		// mRemoveOptionsBar.setVisibility(View.GONE);
		// } else {
		// mTrackListView.setDragEnabled(false);
		// buttonDone.setVisibility(View.GONE);
		// mButtonOptions.setVisibility(View.VISIBLE);
		// // String title = getResources().getString(
		// // R.string.player_queue_title_1);
		// //
		// // Utils.SetMultilanguageTextOnTextView(getActivity(),
		// // mTextTitle,
		// // title);
		// // mTextTitle.append(" (" + mTracks.size() + ")");
		// mTextTitle.setText(Utils
		// .getMultilanguageText(getActivity(), getResources()
		// .getString(R.string.player_queue_title_1))
		// + " (" + mTracks.size() + ")");
		// // mTextTitle.setText(title);
		// mRemoveOptionsBar.setVisibility(View.VISIBLE);
		// }
		// }
		else if (viewId == R.id.player_queue_iv_select_all) {
			for (int i = 0; i < mTrackRemoveState.size(); i++)
				mTrackRemoveState.set(i, true);
			if (mQueueAdapter != null)
				mQueueAdapter.notifyDataSetChanged();
		} else if (viewId == R.id.player_queue_textview_cancel_selection) {
			cancelClick();
		} else if (viewId == R.id.player_queue_textview_delete_selected) {
			// xtpl
			deleteSelectedAudios();
		}
	}

	private void deleteSelectedAudios() {
		Map<String, String> reportMap = new HashMap<String, String>();
		reportMap
				.put(FlurryConstants.FlurryMediaDetailActions.ActionTaken
						.toString(), FlurryConstants.FlurryAllPlayer.DeleteSong
						.toString());
		Analytics.logEvent(
				FlurryConstants.FlurryAllPlayer.PlayerQueueViewed.toString(),
				reportMap);
		// xtpl

		// remove track from queue
		// RelativeLayout tile = (RelativeLayout)
		// view.getParent().getParent();
		ArrayList<Long> idsToRemove = new ArrayList<Long>();
		// Track track = (Track) tile.getTag(R.id.view_tag_object);
		for (int i = 0; i < mTrackRemoveState.size(); i++) {
			if (mTrackRemoveState.get(i)) {
				Track track = mTracks.get(i);
				if (track != null) {
					CacheState cacheState = DBOHandler.getTrackCacheState(
							getActivity(), "" + track.getId());
					Logger.s(track.getTitle() + " ::::::delete:::::: "
							+ cacheState);
					if (cacheState != CacheState.CACHED
							&& cacheState != CacheState.CACHING) {
						boolean isTracksDeleted = DBOHandler.deleteCachedTrack(
								getActivity(), "" + track.getId());
						Logger.s(track.getTitle() + " ::::::delete:::::: "
								+ isTracksDeleted);
						if (isTracksDeleted) {
							Intent TrackCached = new Intent(
									CacheManager.ACTION_TRACK_CACHED);
							Logger.i("Update Cache State",
									" SENDING BROADCAST TRACK_CACHED");
							getActivity().sendBroadcast(TrackCached);
						}
					}
				}
				// int position = (Integer)
				// tile.getTag(R.id.view_tag_position);
				// removes the original from the player.
				if (track != null)
					idsToRemove.add(track.getId());
				mTracks.remove(i);
				mTrackRemoveState.remove(i);
				// mPlayerBarFragment.removeFrom(i);
				i--;
			}
		}
		if (!idsToRemove.isEmpty())
			removeTrack(idsToRemove);

		// updates the current list.
		if (PlayerService.service != null)
			mTracks = PlayerService.service.getPlayingQueue();

		initTrackRemoveState();

		mQueueAdapter.notifyDataSetChanged();
		mRemoveOptionsBar.setVisibility(View.GONE);
		mTextRemoveHint.setVisibility(View.VISIBLE);
		// mTextEdit.setVisibility(View.VISIBLE);
		// mIvSelectAll.setVisibility(View.INVISIBLE);
		mChkboxSelectAll.setVisibility(View.INVISIBLE);
		mTextCancelRemoveState.setVisibility(View.GONE);
		mTextDeleteSelected.setVisibility(View.GONE);
		setTitleText(false);
	}

	public Track removeTrack(ArrayList<Long> idsToRemove) {
		if (PlayerService.service != null) {
			boolean removedCurrentPlayingSong = false;
			for (long trackId : idsToRemove) {
				List<Track> trackList = PlayerService.service.getPlayingQueue();
				int position = -1;
				if (trackList != null) {
					for (int i = 0; i < trackList.size(); i++) {
						// System.out.println(mQueue.get(i).getId() +
						// " ::::::: updateTrack ::::::: " + track.getId());
						if (trackList.get(i).getId() == trackId) {
							// System.out.println("Track found :::::::::::::: "
							// + i);
							position = i;
							break;
						}
					}
				}

				if (position > -1) {
					final int currentPosition = PlayerService.service
							.getCurrentPlayingTrackPosition();

					if (currentPosition == position) {
						removedCurrentPlayingSong = true;
						PlayerService.service.stop();

						PlayerService.service.removeFrom(position);

					} else {
						PlayerService.service.removeFrom(position);
					}

				}
			}
			if (idsToRemove.size() > 0) {
				if (PlayerService.service.getPlayingQueue().size() == 0)
					clearQueue();
				else if (removedCurrentPlayingSong)
					PlayerService.service
							.playFromPositionNew(PlayerService.service
									.getCurrentPlayingTrackPosition());
				getActivity()
						.sendBroadcast(
								new Intent(
										PlayerUpdateWidgetService.ACTION_PLAYER_QUEUE_UPDATED));
			}
		}
		return null;
	}

	// ======================================================
	// Playing Events.
	// ======================================================

	@Override
	public void onTrackLoad() {
		Logger.i("Notify", "Notify:::::::::: onTrackLoad");
		// loadingTrack = -1;
		if (mQueueAdapter != null)
			mQueueAdapter.notifyDataSetChanged();
	}

	@Override
	public void onTrackPlay() {
		Logger.i("Notify", "Notify:::::::::: onTrackPlay");
		if (mQueueAdapter != null)
			mQueueAdapter.notifyDataSetChanged();
	}

	@Override
	public void onTrackFinish() {
		Logger.i("Notify", "Notify:::::::::: onTrackFinish");
		if (mQueueAdapter != null)
			mQueueAdapter.notifyDataSetChanged();
	}

	// ======================================================
	// Private Helper methods.
	// ======================================================

	private void initializeUserControls(View rootView) {
		// System.out.println(" :::-------initializeUserControls--------- " +
		// System.currentTimeMillis());
		isEditMode = false;
		rootView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		int imageTileSpacing = getResources().getDimensionPixelSize(
				R.dimen.home_tiles_spacing_vertical);

		// mTilesGridView = (GridView) rootView
		// .findViewById(R.id.player_queue_gridview);
		mTrackListView = (DragSortListView) rootView
				.findViewById(R.id.player_queue_listview);

		/*
		 * For placing the tiles correctly in the grid, calculates the maximum
		 * size that a tile can be and the column width.
		 */

		// measuring the device's screen width. and setting the grid column
		// width.
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		int screenWidth = 0;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
			screenWidth = display.getWidth();
		} else {
			Point displaySize = new Point();
			display.getSize(displaySize);
			screenWidth = displaySize.x;
		}

		mTileSize = (int) ((screenWidth - (imageTileSpacing + imageTileSpacing * 1.5)) / 2);

		// mTilesGridView.setNumColumns(2);
		// mTilesGridView.setColumnWidth(mTileSize);

		mTextTitle = (LanguageTextView) rootView
				.findViewById(R.id.player_queue_title_bar_text);
		mButtonOptions = (ImageButton) rootView
				.findViewById(R.id.player_queue_title_bar_button_options);
		mButtonOptions.setOnClickListener(this);

		buttonDone = (Button) rootView
				.findViewById(R.id.player_queue_title_bar_button_done);
		buttonDone.setOnClickListener(this);

		mRemoveOptionsBar = (RelativeLayout) rootView
				.findViewById(R.id.player_queue_remove_option_bar);
		// mTextEdit = (TextView)
		// rootView.findViewById(R.id.player_queue_textview_edit);
		// mTextEdit.setOnClickListener(this);

		// ----------------AddedOn01Dec2014-----------//
		ImageView ivPlayer_queue_options_save_all_offline = (ImageView) rootView
				.findViewById(R.id.player_queue_options_save_all_offline);
		ivPlayer_queue_options_save_all_offline.setOnClickListener(this);

		ImageView ivPlayer_queue_options_clear_queue = (ImageView) rootView
				.findViewById(R.id.player_queue_options_clear_queue);
		ivPlayer_queue_options_clear_queue.setOnClickListener(this);

		ImageView ivPlayer_queue_options_save_as_playlist = (ImageView) rootView
				.findViewById(R.id.player_queue_options_save_as_playlist);
		ivPlayer_queue_options_save_as_playlist.setOnClickListener(this);

		ImageView ivPlayer_queue_options_more = (ImageView) rootView
				.findViewById(R.id.player_queue_title_bar_button_more);
		ivPlayer_queue_options_more.setOnClickListener(this);

		ImageView ivPlayer_queue_options_clear = (ImageView) rootView
				.findViewById(R.id.player_queue_title_bar_button_clear);
		ivPlayer_queue_options_clear.setOnClickListener(this);

		ImageView ivBackArrow = (ImageView) rootView
				.findViewById(R.id.ivBackArrow);
		ivBackArrow.setOnClickListener(this);
		// ----------------------------------------------------//

		mTextRemoveHint = (TextView) rootView
				.findViewById(R.id.player_queue_textview_remove_hint);
		// mIvSelectAll = (ImageView)
		// rootView.findViewById(R.id.player_queue_iv_select_all);
		// mIvSelectAll.setOnClickListener(this);
		mChkboxSelectAll = (CheckBox) rootView
				.findViewById(R.id.player_queue_chkbox_select_all);
		mTextCancelRemoveState = (TextView) rootView
				.findViewById(R.id.player_queue_textview_cancel_selection);
		mTextCancelRemoveState.setOnClickListener(this);
		mTextDeleteSelected = (TextView) rootView
				.findViewById(R.id.player_queue_textview_delete_selected);
		mTextDeleteSelected.setOnClickListener(this);

		mChkboxSelectAll
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// sd
					}
				});
		// System.out.println(" :::-------initializeUserControls---------1 " +
		// System.currentTimeMillis());
	}

	private void selectUnSelectAllSongs(int totalSelected) {
		try {
			if (isAutoCheckedChange) {
				isAutoCheckedChange = false;
				return;
			}
			if (totalSelected != mTrackRemoveState.size()) {
				for (int i = 0; i < mTrackRemoveState.size(); i++)
					mTrackRemoveState.set(i, true);
				setTitleText(true);
			} else {
				for (int i = 0; i < mTrackRemoveState.size(); i++)
					mTrackRemoveState.set(i, false);
				setTitleText(false);
			}
			if (mQueueAdapter != null)
				mQueueAdapter.notifyDataSetChanged();

		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	private void clearQueue() {
		try {
			boolean isTracksDeleted = false;
			for (Track track : mTracks) {
				// Track track = (Track) tile.getTag(R.id.view_tag_object);
				if (track != null) {
					CacheState cacheState = DBOHandler.getTrackCacheState(
							getActivity(), "" + track.getId());
					Logger.s(track.getTitle() + " ::::::delete:::::: "
							+ cacheState);
					if (cacheState != CacheState.CACHED
							&& cacheState != CacheState.CACHING) {
						boolean status = DBOHandler.deleteCachedTrack(
								getActivity(), "" + track.getId());
						Logger.s(track.getTitle() + " ::::::delete:::::: "
								+ status);
						if (status)
							isTracksDeleted = true;
					}
				}
			}
			if (isTracksDeleted) {
				Intent TrackCached = new Intent(
						CacheManager.ACTION_TRACK_CACHED);
				Logger.i("Update Cache State", " SENDING BROADCAST TRACK_CACHED");
				getActivity().sendBroadcast(TrackCached);
			}
		

		// clears the queue.
		clearQueue1();

		// updates the list.
		if (PlayerService.service != null) {
			mTracks = PlayerService.service.getPlayingQueue();
		} else {
			mTracks.clear();
		}
		// mQueueAdapter.addAll(mTracks);
		initTrackRemoveState();
		Logger.i("Notify", "Notify:::::::::: clearQueue");
		mQueueAdapter.notifyDataSetChanged();

		// updates the text of the title
		// String title = ;

		setTitleText(false);
} catch (Exception e) {
			Logger.printStackTrace(e);
		}catch (Error e) {
			Logger.printStackTrace(e);
		}
		finish();

	}

	public void clearQueue1() {
		if (PlayerService.service != null) {
			//PlayerService.service.StopCastPlaying();
			PlayerService.service.stopCasting();
			//PlayerService.service.clearCastingQueue();
			//PlayerService.service.removeCastCallBack();
			PlayerService.service.stop();
			PlayerService.service.clearAd();
			PlayerService.service.setPlayingQueue(new PlayingQueue(null, 0,
					PlayerService.service));
			Intent in = new Intent(PlayerService.ACTION_REMOVED_TRACK);
			in.putExtra("clearQueue", true);
			in.putExtra("removedTrackid", "0");
			sendBroadcast(in);
			// System.out.println(" ::: " + getPlayerState().toString());

			// mPlayerService.explicitStop();
			// clearDrawerContent();
		}
	}

	private void setTitleText(boolean needToSetCounter) {
		String title = "";
		int totalSelected = 0;
		if (mTrackRemoveState != null && mTrackRemoveState.size() > 0) {
			for (int i = 0; i < mTrackRemoveState.size(); i++) {
				if (mTrackRemoveState.get(i))
					totalSelected = totalSelected + 1;
			}
		}
		if (totalSelected != 0) {
			title = totalSelected + "";
			// mTextTitle.setText(mTrackRemoveState.size() + "");
		} else {
			// mTextTitle.setText(Utils.getMultilanguageText(getActivity(),
			// getResources().getString(R.string.player_queue_title_1))
			// + " (" + mTracks.size() + ")");
			if (PlayerService.service != null) {
				List<Track> mTracks = PlayerService.service.getPlayingQueue();
				if (mTracks != null && mTracks.size() > 0)
					title = Utils.getMultilanguageText(
							getActivity(),
							getResources().getString(
									R.string.player_queue_title_1))
							+ " (" + mTracks.size() + ")";
				else
					title = Utils.getMultilanguageText(
							getActivity(),
							getResources().getString(
									R.string.player_queue_title_1))
							+ " (0)";
			} else
				title = Utils
						.getMultilanguageText(getActivity(), getResources()
								.getString(R.string.player_queue_title_1))
						+ " (0)";

		}
		showBackButtonWithTitle(title, "");
        if(getSupportActionBar()!=null)
		getSupportActionBar().setIcon(
				new ColorDrawable(getResources().getColor(
						android.R.color.transparent)));
	}

	public void showBackButtonWithTitle(String title, String subTitle) {
		ActionBar mActionBar = getSupportActionBar();
        if(mActionBar==null)
            return;
		Utils.setActionBarTitleSubtitle(this, mActionBar, title, subTitle);
        LanguageButtonLollipop btn_preference = (LanguageButtonLollipop) findViewById(R.id.btn_preferences);
		btn_preference.setVisibility(View.GONE);
	}

	// private static final class ViewHolder {
	// ImageView imageTile;
	// ImageButton buttonPlay;
	// Button buttonRemove;
	// TextView textTitle;
	// TextView textDescription;
	// TextView textNowPlaying;
	// }

	// private class PlayerQueueAdapter extends BaseAdapter implements
	// OnLongClickListener, OnClickListener {
	//
	// private LayoutInflater inflater;
	//
	// public PlayerQueueAdapter() {
	// inflater = (LayoutInflater) getActivity().getApplicationContext()
	// .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	// }
	//
	// @Override
	// public int getCount() {
	// return mTracks.size();
	// }
	//
	// @Override
	// public Object getItem(int position) {
	// return mTracks.get(position);
	// }
	//
	// @Override
	// public long getItemId(int position) {
	// return mTracks.get(position).getId();
	// }
	//
	// @Override
	// public View getView(int position, View convertView, ViewGroup parent) {
	//
	// ViewHolder viewHolder;
	//
	// if (convertView == null) {
	//
	// convertView = inflater.inflate(
	// R.layout.list_item_player_queue_track, parent, false);
	// viewHolder = new ViewHolder();
	//
	// viewHolder.imageTile = (ImageView) convertView
	// .findViewById(R.id.player_queue_tile_image);
	// viewHolder.buttonPlay = (ImageButton) convertView
	// .findViewById(R.id.player_queue_tile_button_play);
	// viewHolder.buttonRemove = (Button) convertView
	// .findViewById(R.id.player_queue_tile_button_remove);
	// viewHolder.textTitle = (TextView) convertView
	// .findViewById(R.id.player_queue_tile_title);
	// viewHolder.textDescription = (TextView) convertView
	// .findViewById(R.id.player_queue_tile_description);
	// viewHolder.textNowPlaying = (TextView) convertView
	// .findViewById(R.id.player_queue_tile_now_playing);
	//
	// convertView.setTag(R.id.view_tag_view_holder, viewHolder);
	//
	// } else {
	// viewHolder = (ViewHolder) convertView
	// .getTag(R.id.view_tag_view_holder);
	// }
	//
	// /*
	// * colors the tile, if it's the selected one, also shows it's
	// * "NOW PLAYING" text.
	// */
	// if (position == mPlayerBarFragment.getCurrentPlayingInQueuePosition()) {
	// // grey it
	// viewHolder.imageTile
	// .setBackgroundResource(R.color.player_queue_now_playing_background);
	// viewHolder.textNowPlaying.setVisibility(View.VISIBLE);
	//
	// // makes the remove button gone, we can't delete a playing
	// // track.
	// // viewHolder.buttonRemove.setVisibility(View.INVISIBLE);
	//
	// // adjusts the play / pause button based on the current playing
	// // state.
	// // if (mPlayerBarFragment.isPlaying()) {
	// if (mPlayerBarFragment.getPlayerState() == State.PLAYING) {// xtpl
	// // playing, shows the pause button.
	// viewHolder.buttonPlay
	// .setBackgroundResource(R.drawable.icon_general_pause_grey);
	// viewHolder.buttonPlay.setSelected(true);
	// } else {
	// // pausing, shows the play button.
	// viewHolder.buttonPlay
	// .setBackgroundResource(R.drawable.icon_general_play_grey);
	// viewHolder.buttonPlay.setSelected(false);
	// }
	//
	// } else {
	// // blue it
	//
	// /*
	// * Creates a pattern of coloring the tiles.
	// */
	// int row = position / 2;
	// int column = position % 2;
	//
	// if (((row % 2 == 0) && (column % 2 == 0))
	// || ((row % 2 != 0) && (column % 2 != 0))) {
	// viewHolder.imageTile
	// .setBackgroundResource(R.drawable.background_music_tile_dark);
	// } else {
	// viewHolder.imageTile
	// .setBackgroundResource(R.drawable.background_music_tile_light);
	// }
	//
	// viewHolder.textNowPlaying.setVisibility(View.GONE);
	//
	// // enables the remove button.
	// viewHolder.buttonRemove.setVisibility(View.VISIBLE);
	//
	// // sets default the icon of the button to play.
	// viewHolder.buttonPlay
	// .setBackgroundResource(R.drawable.icon_general_play_grey);
	// viewHolder.buttonPlay.setSelected(false);
	// }
	//
	// Track track = mTracks.get(position);
	//
	// // sets click listeners to tiles buttons.
	// viewHolder.imageTile.setOnClickListener(this);
	// viewHolder.buttonPlay.setOnClickListener(this);
	// viewHolder.buttonRemove.setOnClickListener(this);
	//
	// // sets long click listeners to the tile and play button.
	// viewHolder.imageTile.setOnLongClickListener(this);
	// viewHolder.buttonPlay.setOnLongClickListener(this);
	//
	// viewHolder.textTitle.setVisibility(View.VISIBLE);
	// viewHolder.textDescription.setVisibility(View.VISIBLE);
	//
	// viewHolder.imageTile.getBackground().setDither(true);
	//
	// // sets the texts.
	// viewHolder.textTitle.setText(track.getTitle());
	// viewHolder.textDescription.setText(track.getAlbumName());
	//
	// /*
	// * sets the media item as the tag to the tile, so other invoked
	// * listeners methods can pull its reference.
	// */
	// convertView.setTag(R.id.view_tag_object, track);
	// convertView.setTag(R.id.view_tag_position, position);
	//
	// // sets the size of the tile before it's being drawn.
	// // convertView.getLayoutParams().width = mTileSize;
	// // convertView.getLayoutParams().height = mTileSize;
	//
	// return convertView;
	// }
	//
	// @Override
	// public void onClick(View view) {
	// int viewId = view.getId();
	// // a tile was clicked, shows its media item's details.
	// if (viewId == R.id.player_queue_tile_image) {
	//
	// //
	// FlurryAgent.logEvent(FlurryConstants.FlurryAllPlayer.SongByTap.toString());
	// // xtpl
	// Map<String, String> reportMap = new HashMap<String, String>();
	// reportMap.put(
	// FlurryConstants.FlurryMediaDetailActions.ActionTaken
	// .toString(),
	// FlurryConstants.FlurryAllPlayer.SongByTap.toString());
	// FlurryAgent.logEvent(
	// FlurryConstants.FlurryAllPlayer.PlayerQueueViewed
	// .toString(), reportMap);
	// // xtpl
	//
	// // start play from beginning or pause
	// RelativeLayout tile = (RelativeLayout) view.getParent();
	//
	// ViewHolder viewHolder = (ViewHolder) tile
	// .getTag(R.id.view_tag_view_holder);
	// int position = (Integer) tile.getTag(R.id.view_tag_position);
	//
	// handlePlayClick(position, viewHolder);
	//
	// // play now was selected.
	// } else if (viewId == R.id.player_queue_tile_button_play) {
	// // play or pause
	// RelativeLayout tile = (RelativeLayout) view.getParent();
	//
	// ViewHolder viewHolder = (ViewHolder) tile
	// .getTag(R.id.view_tag_view_holder);
	// int position = (Integer) tile.getTag(R.id.view_tag_position);
	//
	// handlePlayClick(position, viewHolder);
	//
	// // remove tile was selected.
	// } else if (viewId == R.id.player_queue_tile_button_remove) {
	//
	// //
	// FlurryAgent.logEvent(FlurryConstants.FlurryAllPlayer.DeleteSong.toString());
	// // xtpl
	// Map<String, String> reportMap = new HashMap<String, String>();
	// reportMap.put(
	// FlurryConstants.FlurryMediaDetailActions.ActionTaken
	// .toString(),
	// FlurryConstants.FlurryAllPlayer.DeleteSong.toString());
	// FlurryAgent.logEvent(
	// FlurryConstants.FlurryAllPlayer.PlayerQueueViewed
	// .toString(), reportMap);
	// // xtpl
	//
	// // remove track from queue
	// RelativeLayout tile = (RelativeLayout) view.getParent();
	//
	// int position = (Integer) tile.getTag(R.id.view_tag_position);
	//
	// // removes the original from the player.
	// mPlayerBarFragment.removeFrom(position);
	//
	// // updates the current list.
	// mTracks = mPlayerBarFragment.getCurrentPlayingList();
	//
	// notifyDataSetChanged();
	//
	// // updates the title.
	// // updates the text of the title
	// String title = getResources().getString(
	// R.string.player_queue_title, mTracks.size());
	// mTextTitle.setText(title);
	// }
	// }
	//
	// @Override
	// public boolean onLongClick(View view) {
	// int viewId = view.getId();
	// if (viewId == R.id.player_queue_tile_image
	// || viewId == R.id.player_queue_tile_button_play) {
	//
	// // get the item's id from the tile itself.
	// RelativeLayout tile = (RelativeLayout) view.getParent();
	//
	// ViewHolder viewHolder = (ViewHolder) tile
	// .getTag(R.id.view_tag_view_holder);
	// Track track = (Track) tile.getTag(R.id.view_tag_object);
	// int position = (Integer) tile.getTag(R.id.view_tag_position);
	//
	// showMediaItemOptionsDialog(track, position, viewHolder);
	// }
	//
	// return false;
	// }
	//
	// private void handlePlayClick(int position, ViewHolder viewHolder) {
	// if (mPlayerBarFragment.getCurrentPlayingInQueuePosition() == position) {
	// if (mPlayerBarFragment.isPlaying()) {
	// // checks if the current tile is in the state of play or
	// // pause.
	// if (viewHolder.buttonPlay.isSelected()) {
	// // currently is playing, pauses and shows the "play"
	// // button.
	// mPlayerBarFragment.pause();
	// // sets the button's icon and state.
	// viewHolder.buttonPlay
	// .setBackgroundResource(R.drawable.icon_general_play_grey);
	// viewHolder.buttonPlay.setSelected(false);
	// } else {
	// // currently is paused, resumes playing and shows the
	// // "pause" button.
	// mPlayerBarFragment.play();
	// // sets the button's icon and state.
	// viewHolder.buttonPlay
	// .setBackgroundResource(R.drawable.icon_general_pause_grey);
	// viewHolder.buttonPlay.setSelected(true);
	// }
	// }
	// } else {
	// // goto the new track
	// mPlayerBarFragment.playFromPosition(position);
	// // update adapter.
	// notifyDataSetChanged();
	// }
	// }
	//
	// private void showMediaItemOptionsDialog(final Track track,
	// final int position, final ViewHolder viewHolder) {
	// // set up custom dialog
	// final Dialog mediaItemOptionsDialog = new Dialog(getActivity());
	//
	// mediaItemOptionsDialog
	// .requestWindowFeature(Window.FEATURE_NO_TITLE);
	// mediaItemOptionsDialog
	// .setContentView(R.layout.dialog_media_playing_options);
	// mediaItemOptionsDialog.setCancelable(true);
	// mediaItemOptionsDialog.show();
	//
	// // sets the title.
	// TextView title = (TextView) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_title_text);
	// title.setText(track.getTitle());
	//
	// // sets the cancel button.
	// ImageButton closeButton = (ImageButton) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_title_image);
	// closeButton.setOnClickListener(new View.OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// mediaItemOptionsDialog.dismiss();
	// }
	// });
	//
	// // sets the options buttons.
	// LinearLayout llPlayNow = (LinearLayout) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_play_now_row);
	// LinearLayout llAddtoQueue = (LinearLayout) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_add_to_queue_row);
	// LinearLayout llDetails = (LinearLayout) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_details_row);
	//
	// llAddtoQueue.setVisibility(View.GONE);
	//
	// // play now.
	// llPlayNow.setOnClickListener(new View.OnClickListener() {
	// @Override
	// public void onClick(View view) {
	// //
	// FlurryAgent.logEvent(FlurryConstants.FlurryAllPlayer.ChangeNowPlaying.toString());
	// // xtpl
	// Map<String, String> reportMap = new HashMap<String, String>();
	// reportMap
	// .put(FlurryConstants.FlurryMediaDetailActions.ActionTaken
	// .toString(),
	// FlurryConstants.FlurryAllPlayer.ChangeNowPlaying
	// .toString());
	// FlurryAgent.logEvent(
	// FlurryConstants.FlurryAllPlayer.PlayerQueueViewed
	// .toString(), reportMap);
	// // xtpl
	// mediaItemOptionsDialog.dismiss();
	// handlePlayClick(position, viewHolder);
	// }
	// });
	//
	// // show details.
	// llDetails.setOnClickListener(new View.OnClickListener() {
	// @Override
	// public void onClick(View view) {
	// mediaItemOptionsDialog.dismiss();
	//
	// MediaItem mediaItem = new MediaItem(track.getId(), track
	// .getTitle(), track.getAlbumName(), track
	// .getArtistName(), track.getImageUrl(), track
	// .getBigImageUrl(), MediaType.TRACK.toString()
	// .toLowerCase(), 0);
	//
	// mediaItem.setMediaContentType(MediaContentType.MUSIC);
	// mediaItem.setMediaType(MediaType.TRACK);
	//
	// Intent intent = new Intent(getActivity()
	// .getApplicationContext(),
	// MediaDetailsActivity.class);
	// intent.putExtra(MediaDetailsActivity.EXTRA_MEDIA_ITEM,
	// (Serializable) mediaItem);
	// intent.putExtra(MediaDetailsActivity.FLURRY_SOURCE_SECTION,
	// FlurryConstants.FlurrySourceSection.PlayerQueue
	// .toString());
	//
	// getActivity().startActivity(intent);
	// }
	// });
	// }
	//
	// }

	// ======================================================
	// Options.
	// ======================================================

	private static class ViewHolder1 {
		ImageView ivAlbumImage;
		LanguageTextView tvTrackTitle;
		ImageView ivTrackType;
		LanguageTextView tvTrackTypeAndName;
		RelativeLayout rlRow;
		// RelativeLayout rlRow1, rlUndo;
		ImageButton buttonPlay;
		// ImageButton buttonSaveOffline;
		ImageButton buttonDelete, buttonMore;
		LinearLayout ivDragHandle;
		CustomCacheStateProgressBar progressCacheState;
		View viewDisabled;
		ProgressBar player_queu_loading_indicator_handle;
	}

	private class MAdapter extends SimpleDragSortCursorAdapter implements
			OnClickListener {
		private Context mContext;
		private ApplicationConfigurations mApplicationConfiguration;

		// private Drawable drawableDefaultTile, drawableRightMark;

		public MAdapter(Context ctxt, int rmid, Cursor c, String[] cols,
				int[] ids, int something) {
			super(ctxt, rmid, c, cols, ids, something);
			mContext = ctxt;
			mApplicationConfiguration = ApplicationConfigurations
					.getInstance(mContext);
			// bmpDefaultTile = BitmapFactory.decodeResource(getResources(),
			// R.drawable.background_home_tile_album_default);
			// drawableDefaultTile = new BitmapDrawable(getResources(),
			// BitmapFactory.decodeResource(getResources(),
			// R.drawable.background_home_tile_album_default));
			text_save_offline = mContext
					.getResources()
					.getString(
							R.string.media_details_custom_dialog_long_click_general_save_offline);
		}

		@Override
		public int getCount() {
			try {
				return mTracks.size();
			} catch (Exception e) {
				return 0;
			}
		}

		@Override
		public Track getItem(int position) {
			if (position < mTracks.size())
				return mTracks.get(position);
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;// mTracks.get(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// System.out.println(position + " :::-------getView--------- " +
			// System.currentTimeMillis());
			// View v = super.getView(position, convertView1, parent);
			// View tv = v.findViewById(R.id.player_queue_line_top_text);
			// tv.setOnClickListener(new View.OnClickListener() {
			// @Override
			// public void onClick(View v) {
			// Toast.makeText(mContext, "text clicked",
			// Toast.LENGTH_SHORT).show();
			// }
			// });
			// v.findViewById(R.id.player_queue_media_drag_handle).setVisibility(View.VISIBLE);
			// return v;

			// View convertView;
			// try {
			// convertView = super.getView(position, convertView1, parent);
			// } catch (Exception e) {
			// Logger.printStackTrace(e);
			// convertView = super.newView(mContext, mCursor, parent);
			// }
			ViewHolder1 viewHolder;
			if (convertView == null) {
				LayoutInflater inflater = getActivity().getLayoutInflater();
				convertView = inflater.inflate(
						R.layout.list_item_player_queue_line, null);
				// ViewHolder1 viewHolder = new ViewHolder1();
				viewHolder = new ViewHolder1();
				viewHolder.rlRow = (RelativeLayout) convertView
						.findViewById(R.id.relativelayout_player_queue_line);
				// viewHolder.rlRow1 = (RelativeLayout) convertView
				// .findViewById(R.id.relativelayout_player_queue_item);
				// viewHolder.rlUndo = (RelativeLayout) convertView
				// .findViewById(R.id.relativelayout_player_queue_undo);
				// viewHolder.btnUndo = (Button)
				// convertView.findViewById(R.id.undo);

				viewHolder.buttonPlay = (ImageButton) convertView
						.findViewById(R.id.player_queue_line_button_play);

				viewHolder.tvTrackTitle = (LanguageTextView) convertView
						.findViewById(R.id.player_queue_line_top_text);
				viewHolder.ivTrackType = (ImageView) convertView
						.findViewById(R.id.player_queue_media_image_type);
				viewHolder.tvTrackTypeAndName = (LanguageTextView) convertView
						.findViewById(R.id.player_queue_text_media_type_and_name);
				viewHolder.buttonMore = (ImageButton) convertView
						.findViewById(R.id.player_queue_line_button_more);

				viewHolder.player_queu_loading_indicator_handle = (ProgressBar) convertView
						.findViewById(R.id.player_queu_loading_indicator_handle);

				// viewHolder.buttonSaveOffline = (ImageButton)
				// convertView.findViewById(R.id.player_queue_line_button_save_offline);
				viewHolder.progressCacheState = (CustomCacheStateProgressBar) convertView
						.findViewById(R.id.player_queue_progress_cache_state);
				viewHolder.buttonDelete = (ImageButton) convertView
						.findViewById(R.id.player_queue_line_button_delete);
				viewHolder.ivDragHandle = (LinearLayout) convertView
						.findViewById(R.id.player_queue_media_drag_handle);
				viewHolder.ivAlbumImage = (ImageView) convertView
						.findViewById(R.id.player_queue_media_image);
				viewHolder.viewDisabled = (View) convertView
						.findViewById(R.id.view_disable);
				convertView.setTag(R.id.view_tag_view_holder, viewHolder);

				convertView.setTag(viewHolder);
				// rowView.setTag(viewHolder);
				Logger.i("", "Position1:" + position);
			} else {
				Logger.i("", "Position2:" + position);
				viewHolder = (ViewHolder1) convertView.getTag();
			}
			// System.out.println(position + " :::-------getView---------01 " +
			// System.currentTimeMillis());
			//
			// ViewHolder1 viewHolder = (ViewHolder1) convertView.getTag();
			viewHolder.tvTrackTitle.setTag(R.id.view_tag_view_holder, position);
			viewHolder.buttonPlay.setTag(R.id.view_tag_view_holder, position);
			viewHolder.tvTrackTypeAndName.setTag(R.id.view_tag_view_holder,
					position);
			viewHolder.buttonDelete.setVisibility(View.GONE);
			viewHolder.ivDragHandle.setVisibility(View.VISIBLE);
			viewHolder.ivAlbumImage.setVisibility(View.VISIBLE);
			viewHolder.buttonPlay.setVisibility(View.VISIBLE);
			if (mApplicationConfiguration.getSaveOfflineMode())
				viewHolder.buttonMore.setVisibility(View.GONE);
			else
				viewHolder.buttonMore.setVisibility(View.VISIBLE);
			// viewHolder.buttonSaveOffline.setVisibility(View.VISIBLE);

			// System.out.println(position + " :::-------getView---------11 " +
			// System.currentTimeMillis());
			Track mediaItem = (Track) getItem(position);
			if (mediaItem != null) {
				Logger.i("ID", "Track Id:" + mediaItem.getId());

				if (PlayerService.service != null) {
					// int pos =
					// PlayerService.service.getCurrentQueuePosition();
					Track currentPlayingTrack = PlayerService.service
							.getCurrentPlayingTrack();
					if (currentPlayingTrack != null)
						Logger.i("ID", "Track Id: Current Playing: "
								+ currentPlayingTrack.getId());
					if (currentPlayingTrack != null
							&& currentPlayingTrack.getId() == mediaItem.getId()) {

						viewHolder.buttonPlay.setVisibility(View.VISIBLE);
						convertView.setBackgroundColor(getResources().getColor(
								R.color.player_queue_now_playing_background));

						if (PlayerService.service.getState() == State.INTIALIZED) {
							viewHolder.player_queu_loading_indicator_handle
									.setVisibility(View.VISIBLE);
							viewHolder.buttonPlay.setVisibility(View.GONE);
						} else if (PlayerService.service.getState() == State.PLAYING) {
							viewHolder.player_queu_loading_indicator_handle
									.setVisibility(View.GONE);
							viewHolder.buttonPlay
									.setImageResource(R.drawable.icon_circle_pause_blue_outline);
							viewHolder.buttonPlay.setSelected(true);
						} else {
							viewHolder.player_queu_loading_indicator_handle
									.setVisibility(View.GONE);
							viewHolder.buttonPlay
									.setImageResource(R.drawable.icon_circle_play_blue_outline);
							viewHolder.buttonPlay.setSelected(false);
						}

						// if (PlayerService.service.getState() ==
						// State.PLAYING)
						// {// xtpl
						// // // playing, shows the pause button.
						// viewHolder.player_queu_loading_indicator_handle
						// .setVisibility(View.GONE);
						// viewHolder.buttonPlay
						// .setImageResource(R.drawable.icon_circle_pause_blue_outline);
						// viewHolder.buttonPlay.setSelected(true);
						// } else {
						// // pausing, shows the play button.
						// if (PlayerService.service.getState() ==
						// State.INTIALIZED)
						// {
						// viewHolder.player_queu_loading_indicator_handle
						// .setVisibility(View.VISIBLE);
						// viewHolder.buttonPlay.setVisibility(View.GONE);
						// } else {
						// viewHolder.player_queu_loading_indicator_handle
						// .setVisibility(View.GONE);
						// viewHolder.buttonPlay
						// .setImageResource(R.drawable.icon_circle_pause_blue_outline);
						// viewHolder.buttonPlay.setSelected(true);
						// }
						// else if (!isFromReciver) {
						// viewHolder.player_queu_loading_indicator_handle
						// .setVisibility(View.VISIBLE);
						// viewHolder.buttonPlay.setVisibility(View.GONE);
						// }
						// if (isFromReciver)
						// isFromReciver = false;

						// viewHolder.buttonPlay
						// .setImageResource(R.drawable.icon_circle_play_blue_outline);
						// viewHolder.buttonPlay.setSelected(false);
						// }
						// viewHolder.buttonPlay
						// .setImageResource(R.drawable.icon_circle_play_blue_outline);
					} else {
						viewHolder.player_queu_loading_indicator_handle
								.setVisibility(View.GONE);
						viewHolder.buttonPlay.setVisibility(View.INVISIBLE);
						try {
							if (mTrackRemoveState.get(position)) {
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
					viewHolder.buttonPlay.setVisibility(View.INVISIBLE);
					try {
						if (mTrackRemoveState.get(position)) {
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
					viewHolder.player_queu_loading_indicator_handle
							.setVisibility(View.GONE);
					// viewHolder.buttonPlay.setVisibility(View.GONE);
					viewHolder.buttonPlay.setSelected(false);
				}

				// stores the object in the view.
				convertView.setTag(R.id.view_tag_object, mediaItem);

				convertView.setTag(R.id.view_tag_position, position);
				//
				viewHolder.tvTrackTitle
						.setTag(R.id.view_tag_position, position);
				viewHolder.buttonPlay.setTag(R.id.view_tag_position, position);
				viewHolder.tvTrackTypeAndName.setTag(R.id.view_tag_position,
						position);
				// System.out.println(position +
				// " :::-------getView---------31 " +
				// System.currentTimeMillis());
				// Set title
				viewHolder.tvTrackTitle.setText(mediaItem.getTitle());

				// Set Image Type and Text Below title By Type
				viewHolder.ivTrackType
						.setImageResource(R.drawable.icon_main_settings_music);

				viewHolder.tvTrackTypeAndName
						.setText(Utils
								.getMultilanguageTextLayOut(
										getActivity(),
										getString(R.string.search_results_layout_bottom_text_for_track))
								+ " - " + mediaItem.getAlbumName());
				// System.out.println(position +
				// " :::-------getView---------41 " +
				// System.currentTimeMillis());
				// viewHolder.ivAlbumImage
				// .setBackgroundResource(R.drawable.background_home_tile_album_default);
				// viewHolder.ivAlbumImage.setBackgroundDrawable(drawableDefaultTile);
				// viewHolder.ivAlbumImage
				// .setBackgroundResource(R.drawable.background_home_tile_album_default);
				try {
					if (mTrackRemoveState.get(position)) {
						// viewHolder.ivAlbumImage
						// .setBackgroundResource(R.drawable.background_player_queue_album_right_mark);
						// if (drawableRightMark == null)
						// drawableRightMark = new BitmapDrawable(
						// getResources(),
						// BitmapFactory
						// .decodeResource(
						// getResources(),
						// R.drawable.background_player_queue_album_right_mark));
						// viewHolder.ivAlbumImage
						// .setBackgroundDrawable(drawableRightMark);
						viewHolder.ivAlbumImage
								.setImageResource(R.drawable.background_player_queue_album_right_mark);
						// viewHolder.ivAlbumImage
						// .setBackgroundResource(R.drawable.background_player_queue_album_right_mark);
						// viewHolder.ivAlbumImage.setImageBitmap(null);
					} else {
						setNotPlaylistResultImage(viewHolder, mediaItem,
								position);
					}
				} catch (Exception e) {

					e.printStackTrace();
				}

				// System.out.println(position +
				// " :::-------getView---------51 " +
				// System.currentTimeMillis());
				// if (!isEditMode) {
				// if (mTrackRemoveState.get(position)) {
				// // viewHolder.ivAlbumImage
				// //
				// .setBackgroundResource(R.drawable.background_player_queue_album_right_mark);
				// if (drawableRightMark == null)
				// drawableRightMark = new BitmapDrawable(
				// getResources(),
				// BitmapFactory
				// .decodeResource(
				// getResources(),
				// R.drawable.background_player_queue_album_right_mark));
				// viewHolder.ivAlbumImage
				// .setBackgroundDrawable(drawableRightMark);
				// viewHolder.ivAlbumImage.setImageBitmap(null);
				// } else {
				// // setNotPlaylistResultImage(viewHolder, mediaItem,
				// position);
				// }
				// }
				CacheState cacheState = DBOHandler.getTrackCacheState(
						getActivity().getApplicationContext(),
						"" + mediaItem.getId());
				// if(cacheState==CacheState.CACHED){
				// viewHolder.buttonSaveOffline.setImageResource(R.drawable.icon_media_details_saved);
				// } else if(cacheState==CacheState.CACHING ||
				// cacheState==CacheState.QUEUED){
				// viewHolder.buttonSaveOffline.setImageResource(R.drawable.icon_media_details_saving);
				// } else{
				// viewHolder.buttonSaveOffline.setImageResource(R.drawable.icon_media_details_saveoffline);
				// }
				// if(CacheManager.isProUser(mContext)){
				viewHolder.progressCacheState.showProgressOnly(true);
				viewHolder.progressCacheState.setCacheState(cacheState);
				// viewHolder.progressCacheState.setProgress(DBOHandler
				// .getTrackCacheProgress(getActivity()
				// .getApplicationContext(), "" + mediaItem.getId()));
				// }
				// viewHolder.rlRow.setOnClickListener(this);

				if (mApplicationConfiguration.getSaveOfflineMode()
						&& cacheState != CacheState.CACHED) {
					viewHolder.rlRow.setEnabled(false);
					viewHolder.viewDisabled.setVisibility(View.VISIBLE);
					viewHolder.viewDisabled.setOnClickListener(this);
				} else {
					viewHolder.rlRow.setEnabled(true);
					viewHolder.viewDisabled.setVisibility(View.GONE);
				}
				// System.out.println(position +
				// " :::-------getView---------61 " +
				// System.currentTimeMillis());
				if (isEditMode) {
					viewHolder.buttonDelete.setOnClickListener(this);
					// viewHolder.rlRow.setClickable(false);
					// viewHolder.rlRow.setOnClickListener(null);

					// viewHolder.rlRow.setOnLongClickListener(null);
					((LinearLayout) viewHolder.tvTrackTitle.getParent())
							.setOnLongClickListener(null);
				} else {
					viewHolder.buttonMore.setOnClickListener(this);
					// viewHolder.rlRow.setClickable(true);
					// viewHolder.rlRow.setOnClickListener(this);
					// viewHolder.rlRow.setOnLongClickListener(this);
					// viewHolder.rlRow.setOnClickListener(null);
					// ((LinearLayout) viewHolder.tvTrackTitle.getParent())
					// .setOnLongClickListener(this);
					// viewHolder.ivAlbumImage.setOnLongClickListener(this);
					viewHolder.ivAlbumImage.setOnClickListener(this);

					// viewHolder.tvTrackTitle.setOnClickListener(this);
					// viewHolder.tvTrackTypeAndName.setOnClickListener(this);
					viewHolder.buttonPlay.setOnClickListener(this);
					// viewHolder.buttonPlay.setOnLongClickListener(this);

					// viewHolder.buttonSaveOffline.setOnClickListener(this);
					// viewHolder.buttonSaveOffline.setOnLongClickListener(this);

					// if (removeItemAlertPos != null
					// && removeItemAlertPos.equals(position + "")) {
					// viewHolder.rlUndo.setVisibility(View.VISIBLE);
					// viewHolder.rlRow1.setVisibility(View.GONE);
					// } else {
					// viewHolder.rlUndo.setVisibility(View.GONE);
					// viewHolder.rlRow1.setVisibility(View.VISIBLE);
					// }
				}
			}
			// System.out.println(position + " :::-------getView---------1 " +
			// System.currentTimeMillis());
			return convertView;
		}

		public void setNotPlaylistResultImage(final ViewHolder1 viewHolder,
				Track mediaItem, final int position) {
			try {
				final String imageUrl = ImagesManager
						.getMusicArtSmallImageUrl(mediaItem.getImagesUrlArray());
				// mediaItem.getImageUrl();
				if (imageUrl != null && viewHolder.ivAlbumImage != null)

					picasso.load(
							new PicassoCallBack() {

								@Override
								public void onSuccess() {
									if (position < mTrackRemoveState.size()
											&& mTrackRemoveState.get(position)) {
										viewHolder.ivAlbumImage
												.setImageResource(R.drawable.background_home_tile_album_default);
									}
								}

								@Override
								public void onError() {
								}
							}, imageUrl, viewHolder.ivAlbumImage,
							R.drawable.background_home_tile_album_default);
			} catch (Exception e) {
				Logger.printStackTrace(e);
				// viewHolder.ivAlbumImage
				// .setBackgroundResource(R.drawable.background_home_tile_album_default);
				// viewHolder.ivAlbumImage
				// .setBackgroundDrawable(drawableDefaultTile);
				// viewHolder.ivAlbumImage
				// .setBackgroundResource(R.drawable.background_home_tile_album_default);
				viewHolder.ivAlbumImage
						.setImageResource(R.drawable.background_home_tile_album_default);
			}
		}

		@Override
		public void onClick(final View view) {
			int viewId = view.getId();
			// a tile was clicked, shows its media item's details.
			if (viewId == R.id.relativelayout_player_queue_line) {
				// FlurryAgent.logEvent(FlurryConstants.FlurryAllPlayer.SongByTap.toString());
				// xtpl
				Map<String, String> reportMap = new HashMap<String, String>();
				reportMap.put(
						FlurryConstants.FlurryMediaDetailActions.ActionTaken
								.toString(),
						FlurryConstants.FlurryAllPlayer.SongByTap.toString());
				Analytics.logEvent(
						FlurryConstants.FlurryAllPlayer.PlayerQueueViewed
								.toString(), reportMap);
				// xtpl

				// start play from beginning or pause
				RelativeLayout tile = (RelativeLayout) view;// .getParent();

				ViewHolder1 viewHolder = (ViewHolder1) tile
						.getTag(R.id.view_tag_view_holder);
				int position = (Integer) tile.getTag(R.id.view_tag_position);

				handlePlayClick(position, viewHolder);

				// play now was selected.
			} else if (viewId == R.id.player_queue_line_top_text
					|| viewId == R.id.player_queue_line_button_play
					|| viewId == R.id.player_queue_text_media_type_and_name) {
				// play or pause
				View tile = (View) view.getParent().getParent().getParent();
				if (viewId == R.id.player_queue_text_media_type_and_name) {
					tile = (View) view.getParent().getParent().getParent();
				}

				// start play from beginning or pause
				// RelativeLayout tile = (RelativeLayout) view;// .getParent();
				//
				// ViewHolder1 viewHolder = (ViewHolder1) tile
				// .getTag(R.id.view_tag_view_holder);

				ViewHolder1 viewHolder = (ViewHolder1) tile
						.getTag(R.id.view_tag_view_holder);
				int position = (Integer) tile.getTag(R.id.view_tag_position);

				handlePlayClick(position, viewHolder);

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
						.getParent();

				Track track = (Track) tile.getTag(R.id.view_tag_object);
				if (track != null) {
					CacheState cacheState = DBOHandler.getTrackCacheState(
							mContext, "" + track.getId());
					Logger.s(track.getTitle() + " ::::::delete:::::: "
							+ cacheState);
					if (cacheState != CacheState.CACHED
							&& cacheState != CacheState.CACHING) {
						boolean isTracksDeleted = DBOHandler.deleteCachedTrack(
								mContext, "" + track.getId());
						Logger.s(track.getTitle() + " ::::::delete:::::: "
								+ isTracksDeleted);
						if (isTracksDeleted) {
							Intent TrackCached = new Intent(
									CacheManager.ACTION_TRACK_CACHED);
							Logger.i("Update Cache State",
									" SENDING BROADCAST TRACK_CACHED");
							getActivity().sendBroadcast(TrackCached);
						}
					}
				}

				int position = (Integer) tile.getTag(R.id.view_tag_position);

				// removes the original from the player.
				PlayerService.service.removeFrom(position);

				// updates the current list.
				mTracks = PlayerService.service.getPlayingQueue();
				initTrackRemoveState();
				Logger.i("Notify",
						"Notify:::::::::: player_queue_line_button_delete");
				notifyDataSetChanged();

				// updates the title.
				// updates the text of the title
				// String title = getResources().getString(
				// R.string.player_queue_title, mTracks.size());
				// mTextTitle.setText(title);
			} else if (viewId == R.id.player_queue_line_button_more) {
				((MAdapter) mQueueAdapter).onLongClick(view);
			}

			else if (viewId == R.id.view_disable) {
				final ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
						.getInstance(mContext);
				if (mApplicationConfigurations.getSaveOfflineMode()) {
					CustomAlertDialog alertBuilder = new CustomAlertDialog(
							getActivity());
					alertBuilder
							.setMessage(Utils
									.getMultilanguageTextHindi(
											mContext,
											getResources()
													.getString(
															R.string.caching_text_message_go_online_player)));
					alertBuilder
							.setPositiveButton(
									Utils.getMultilanguageTextHindi(
											mContext,
											getResources()
													.getString(
															R.string.caching_text_popup_title_go_online)),
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											if (Utils.isConnected()) {
												mApplicationConfigurations
														.setSaveOfflineMode(false);

												Map<String, String> reportMap = new HashMap<String, String>();
												reportMap
														.put(FlurryConstants.FlurryCaching.Source
																.toString(),
																FlurryConstants.FlurryCaching.Prompt
																		.toString());
												reportMap
														.put(FlurryConstants.FlurryCaching.UserStatus
																.toString(),
																Utils.getUserState(getActivity()));
												Analytics
														.logEvent(
																FlurryConstants.FlurryCaching.GoOnline
																		.toString(),
																reportMap);

												RelativeLayout tile = (RelativeLayout) view
														.getParent();
												// ViewHolder1 viewHolder =
												// (ViewHolder1) tile
												// .getTag(R.id.view_tag_view_holder);
												int position = (Integer) tile
														.getTag(R.id.view_tag_position);
												// handlePlayClick(position,
												// viewHolder);

												Intent i = new Intent(
														MainActivity.ACTION_OFFLINE_MODE_CHANGED);
												i.putExtra(
														MainActivity.IS_FROM_PLAYER_QUEUE,
														true);
												i.putExtra(
														MainActivity.PLAY_FROM_POSITION,
														position);
												mContext.sendBroadcast(i);
											} else {
												CustomAlertDialog alertBuilder = new CustomAlertDialog(
														getActivity());
												alertBuilder.setMessage(Utils
														.getMultilanguageText(
																mContext,
																getResources()
																		.getString(
																				R.string.go_online_network_error)));
												alertBuilder.setNegativeButton(
														Utils.getMultilanguageText(
																mContext, "OK"),
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
					alertBuilder
							.setNegativeButton(
									Utils.getMultilanguageText(
											mContext,
											getResources()
													.getString(
															R.string.caching_text_popup_button_cancel)),
									null);
					// alertBuilder.create();
					alertBuilder.show();
				}
			} else if (viewId == R.id.player_queue_media_image) {
				new FlipAnimationListener((ImageView) view);
				// View parentView = (View) view.getParent();
				// if(!mTrackRemoveState.get((Integer)
				// parentView.getTag(R.id.view_tag_position))){
				// mTrackRemoveState.set((Integer)
				// parentView.getTag(R.id.view_tag_position), true);
				// ((ImageView)
				// view).setBackgroundResource(R.drawable.background_player_queue_album_right_mark);
				// ((ImageView) view).setImageBitmap(null);
				// } else{
				// mTrackRemoveState.set((Integer)
				// parentView.getTag(R.id.view_tag_position), false);
				// setNotPlaylistResultImage((ViewHolder1)parentView.getTag(R.id.view_tag_view_holder),
				// (Track) parentView.getTag(R.id.view_tag_object));
				// }
				// boolean removeState = false;
				// for(boolean state : mTrackRemoveState){
				// if(state){
				// removeState = true;
				// break;
				// }
				// }
				// if(removeState){
				// mTextRemoveHint.setVisibility(View.GONE);
				// mTextEdit.setVisibility(View.GONE);
				// mIvSelectAll.setVisibility(View.VISIBLE);
				// mTextCancelRemoveState.setVisibility(View.VISIBLE);
				// mTextDeleteSelected.setVisibility(View.VISIBLE);
				// } else{
				// mTextRemoveHint.setVisibility(View.VISIBLE);
				// mTextEdit.setVisibility(View.VISIBLE);
				// mIvSelectAll.setVisibility(View.GONE);
				// mTextCancelRemoveState.setVisibility(View.GONE);
				// mTextDeleteSelected.setVisibility(View.GONE);
				// }
			}
		}

		private void openPlayDialog(int position) {
			View convertView = mTrackListView.getChildAt(position);
			Track track = (Track) mTrackListView.getAdapter().getItem(position);

			ViewHolder1 viewHolder = new ViewHolder1();
			viewHolder.rlRow = (RelativeLayout) convertView
					.findViewById(R.id.relativelayout_player_queue_line);
			viewHolder.buttonPlay = (ImageButton) convertView
					.findViewById(R.id.player_queue_line_button_play);

			viewHolder.tvTrackTitle = (LanguageTextView) convertView
					.findViewById(R.id.player_queue_line_top_text);
			viewHolder.ivTrackType = (ImageView) convertView
					.findViewById(R.id.player_queue_media_image_type);
			viewHolder.tvTrackTypeAndName = (LanguageTextView) convertView
					.findViewById(R.id.player_queue_text_media_type_and_name);

			viewHolder.progressCacheState = (CustomCacheStateProgressBar) convertView
					.findViewById(R.id.player_queue_progress_cache_state);
			viewHolder.buttonDelete = (ImageButton) convertView
					.findViewById(R.id.player_queue_line_button_delete);
			viewHolder.ivDragHandle = (LinearLayout) convertView
					.findViewById(R.id.player_queue_media_drag_handle);
			viewHolder.ivAlbumImage = (ImageView) convertView
					.findViewById(R.id.player_queue_media_image);
			viewHolder.viewDisabled = (View) convertView
					.findViewById(R.id.view_disable);
			convertView.setTag(R.id.view_tag_view_holder, viewHolder);
			viewHolder.tvTrackTitle.setTag(R.id.view_tag_view_holder, position);
			viewHolder.buttonPlay.setTag(R.id.view_tag_view_holder, position);
			viewHolder.tvTrackTypeAndName.setTag(R.id.view_tag_view_holder,
					position);
			// showMediaItemOptionsDialog(track, position, viewHolder);
		}

		// @Override
		public boolean onLongClick(View view) {
			int viewId = view.getId();
			if (viewId == R.id.relativelayout_player_queue_line
					|| viewId == R.id.player_queue_line_button_play
					|| viewId == R.id.player_queue_line_button_more
					// || viewId ==
					// R.id.player_queue_line_button_save_offline
					|| viewId == R.id.ll_item_details
					|| viewId == R.id.player_queue_media_image) {
				// get the item's id from the tile itself.
				RelativeLayout tile;
				if (viewId == R.id.player_queue_line_button_play
						|| viewId == R.id.player_queue_line_button_more)
					// || viewId ==
					// R.id.player_queue_line_button_save_offline)
					tile = (RelativeLayout) view.getParent().getParent()
							.getParent();
				else if (viewId == R.id.ll_item_details
						|| viewId == R.id.player_queue_media_image)
					tile = (RelativeLayout) view.getParent();
				else
					tile = (RelativeLayout) view;// .getParent();

				ViewHolder1 viewHolder = (ViewHolder1) tile
						.getTag(R.id.view_tag_view_holder);
				Track track = (Track) tile.getTag(R.id.view_tag_object);
				int position = (Integer) tile.getTag(R.id.view_tag_position);

				showMediaItemOptionsDialog(track, position, viewHolder, view);
				return true;
			}
			return false;
		}

		private int saveoffline_drawable = R.drawable.icon_media_details_saving;
		private String text_save_offline = "";
		private boolean displaysaveOffline = true;

		public void showMediaItemOptionsDialog(final Track track,
				final int position, final ViewHolder1 viewHolder, View view) {
			Logger.s("showMediaItemOptionsDialog");

			if (mApplicationConfiguration.getSaveOfflineMode()) {
				// llSaveOffline.setVisibility(View.GONE);
				displaysaveOffline = false;
			} else {
				// llSaveOffline.setTag(false);
				// CustomCacheStateProgressBar progressCacheState =
				// (CustomCacheStateProgressBar) mediaItemOptionsDialog
				// .findViewById(R.id.long_click_custom_dialog_save_offline_progress_cache_state);
				// progressCacheState.setNotCachedStateVisibility(true);
				// progressCacheState.setTag(R.id.view_tag_object, track);
				CacheState cacheState = DBOHandler.getTrackCacheState(
						getActivity().getApplicationContext(),
						"" + track.getId());
				if (cacheState == CacheState.CACHED) {
					// llSaveOffline.setTag(null);
					// ((TextView) mediaItemOptionsDialog
					// .findViewById(R.id.long_click_custom_dialog_save_offline_text))
					// .setText(getResources().getString(
					// R.string.caching_text_play_offline));
					text_save_offline = getResources().getString(
							R.string.caching_text_play_offline);
					saveoffline_drawable = R.drawable.icon_media_details_saved;
					// ((ImageView) mediaItemOptionsDialog
					// .findViewById(R.id.long_click_custom_dialog_save_offline_image))
					// .setImageResource(R.drawable.icon_media_details_saved);
				} else if (cacheState == CacheState.CACHING) {
					text_save_offline = mContext.getResources().getString(
							R.string.caching_text_saving);
					saveoffline_drawable = R.drawable.icon_media_details_saving_started;
					Logger.e("text_save_offline caching or queu",
							text_save_offline);
				} else if (cacheState == CacheState.QUEUED) {
					text_save_offline = mContext.getResources().getString(
							R.string.caching_text_saving);
					saveoffline_drawable = R.drawable.icon_media_details_saving_queue;
					Logger.e("text_save_offline caching or queu",
							text_save_offline);
				} else {
					saveoffline_drawable = R.drawable.icon_media_details_saving;
					text_save_offline = mContext
							.getResources()
							.getString(
									R.string.media_details_custom_dialog_long_click_general_save_offline);
					displaysaveOffline = true;
				}
				// progressCacheState.setCacheState(cacheState);
				// progressCacheState.setProgress(DBOHandler
				// .getTrackCacheProgress(mContext, "" + track.getId()));
				// if(!CacheManager.isProUser(mContext)){
				// ((TextView)
				// mediaItemOptionsDialog.findViewById(R.id.long_click_custom_dialog_save_offline_text)).setText("Save Offline");
				// progressCacheState.setCacheState(CacheState.NOT_CACHED);
				// }
				displaysaveOffline = true;
				// llSaveOffline.setVisibility(View.VISIBLE);
			}

			try {
				if (quickactionPlayerQueue != null)
					quickactionPlayerQueue.dismiss();
				quickactionPlayerQueue = new QuickActionPlayerQueue(
						getActivity(), track, text_save_offline,
						saveoffline_drawable, displaysaveOffline);
				quickactionPlayerQueue.show(view);
			} catch (Exception e) {
				Logger.printStackTrace(e);

			}
		}
	}

	// private int loadingTrack = -1;

	private void handlePlayClick(int position, ViewHolder1 viewHolder) {
		if (PlayerService.service != null) {
			if (PlayerService.service.getCurrentQueuePosition() == position) {
				if (PlayerService.service.isPlaying()) {
					// checks if the current tile is in the state of play or
					// pause.
					if (viewHolder.buttonPlay.isSelected()) {
						// currently is playing, pauses and shows the "play"
						// button.
						PlayerService.service.pause();
						// sets the button's icon and state.
						viewHolder.buttonPlay
								.setImageResource(R.drawable.icon_circle_play_blue_outline);
						viewHolder.buttonPlay.setSelected(false);
					} else {
						// currently is paused, resumes playing and shows the
						// "pause" button.
						PlayerService.service.play();
						// sets the button's icon and state.
						viewHolder.buttonPlay
								.setImageResource(R.drawable.icon_circle_pause_blue_outline);
						viewHolder.buttonPlay.setSelected(true);
					}
				}
			} else {
				// goto the new track
				PlayerService.service.playFromPosition(position);
				// loadingTrack = position;
				// update adapter.
				Logger.i("Notify", "Notify:::::::::: handlePlayClick");
				mQueueAdapter.notifyDataSetChanged();
			}
		}
	}

	private void saveTrackOffline(Track track) {
		MediaItem mediaItem = new MediaItem(track.getId(), track.getTitle(),
				track.getAlbumName(), track.getArtistName(),
				track.getImageUrl(), track.getBigImageUrl(), MediaType.TRACK
						.name().toLowerCase(), 0, 0, track.getImages(),
				track.getAlbumId());
		// new MediaCachingTask(getActivity().getApplicationContext(),
		// mediaItem, track).execute();
		CacheManager.saveOfflineAction(getActivity(), mediaItem, track);
		// HomeActivity.refreshOfflineState = true;

		Utils.saveOfflineFlurryEvent(getActivity(),
				FlurryConstants.FlurryCaching.LongPressMenuPlayerQueue
						.toString(), mediaItem);
	}

	private boolean isNetworkAvailable(final View view) {
		if (DataManager.getInstance(getActivity()).isDeviceOnLine()) {
			return true;
		} else {
			((MainActivity) getActivity())
					.internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
						@Override
						public void onRetryButtonClicked() {
							view.performClick();
						}
					});
			return false;
		}
	}

	public boolean isHandledActionOffline(final int action) {
		final ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(getActivity());
		if (mApplicationConfigurations.getSaveOfflineMode()) {
			CustomAlertDialog alertBuilder = new CustomAlertDialog(
					getActivity());
			alertBuilder.setMessage(Utils.getMultilanguageTextHindi(
					getActivity(),
					getResources().getString(
							R.string.caching_text_message_go_online_player)));
			alertBuilder.setPositiveButton(Utils.getMultilanguageTextHindi(
					getActivity(),
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
												.getUserState(getActivity()));
								Analytics.logEvent(
										FlurryConstants.FlurryCaching.GoOnline
												.toString(), reportMap);

								Intent i = new Intent(
										MainActivity.ACTION_OFFLINE_MODE_CHANGED);
								i.putExtra(MainActivity.IS_FROM_PLAYER_QUEUE,
										true);
								i.putExtra(MainActivity.PLAYER_QUEUE_ACTION,
										action);
								getActivity().sendBroadcast(i);
							} else {
								CustomAlertDialog alertBuilder = new CustomAlertDialog(
										getActivity());
								alertBuilder.setMessage(Utils
										.getMultilanguageText(
												getActivity(),
												getResources()
														.getString(
																R.string.go_online_network_error)));
								alertBuilder.setNegativeButton(Utils
										.getMultilanguageText(getActivity(),
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
			alertBuilder.setNegativeButton(Utils.getMultilanguageText(
					getActivity(),
					getResources().getString(
							R.string.caching_text_popup_button_cancel)), null);
			// alertBuilder.create();
			alertBuilder.show();
			return true;
		} else {
			return false;
		}
	}

	private void initTrackRemoveState() {
		if (mTrackRemoveState != null)
			mTrackRemoveState.clear();
		mTrackRemoveState = new ArrayList<Boolean>();
		if (mTracks != null)
			for (int i = 0; i < mTracks.size(); i++)
				mTrackRemoveState.add(false);
		mRemoveOptionsBar.setVisibility(View.GONE);
		mTextRemoveHint.setVisibility(View.VISIBLE);
		// mTextEdit.setVisibility(View.VISIBLE);
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
			animation1 = AnimationUtils.loadAnimation(getActivity(),
					R.anim.to_middle);
			animation1.setAnimationListener(this);
			animation2 = AnimationUtils.loadAnimation(getActivity(),
					R.anim.from_middle);
			animation2.setAnimationListener(this);

			this.view = view;
			view.clearAnimation();
			view.setAnimation(animation1);
			view.startAnimation(animation1);
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			if (animation == animation1) {
				View tile = (View) view.getParent().getParent().getParent(); // .getParent();
				ViewHolder1 viewHolder = (ViewHolder1) tile
						.getTag(R.id.view_tag_view_holder);
				int position = (Integer) tile.getTag(R.id.view_tag_position);

				if(mTrackRemoveState==null || mTrackRemoveState.size()<position)
					return;

				if (!mTrackRemoveState.get(position)) {
					mTrackRemoveState.set(position, true);
					// ((ImageView) view)
					// .setBackgroundResource(R.drawable.background_player_queue_album_right_mark);
					// ((ImageView) view).setImageBitmap(null);
					((ImageView) view)
							.setImageResource(R.drawable.background_player_queue_album_right_mark);

					setTitleText(true);
				} else {
					isAutoCheckedChange = true;
					mChkboxSelectAll.setChecked(false);
					isAutoCheckedChange = false;
					mTrackRemoveState.set(position, false);
					setNotPlaylistResultImage(viewHolder,
							(Track) tile.getTag(R.id.view_tag_object));
				}
				boolean removeState = false;
				for (boolean state : mTrackRemoveState) {
					if (state) {
						removeState = true;
						break;
					}
				}

				if (removeState) {
					// mRemoveOptionsBar.setVisibility(View.VISIBLE);
					mRemoveOptionsBar.setVisibility(View.GONE);
					mTextRemoveHint.setVisibility(View.GONE);
					// mTextEdit.setVisibility(View.GONE);
					// mIvSelectAll.setVisibility(View.VISIBLE);
					if (mChkboxSelectAll.getVisibility() != View.VISIBLE)
						mChkboxSelectAll.setChecked(false);
					mChkboxSelectAll.setVisibility(View.VISIBLE);
					mTextCancelRemoveState.setVisibility(View.VISIBLE);
					mTextDeleteSelected.setVisibility(View.VISIBLE);
					setTitleText(true);
				} else {
					mRemoveOptionsBar.setVisibility(View.GONE);
					mTextRemoveHint.setVisibility(View.VISIBLE);
					// mTextEdit.setVisibility(View.VISIBLE);
					// mIvSelectAll.setVisibility(View.INVISIBLE);
					mChkboxSelectAll.setVisibility(View.INVISIBLE);
					mTextCancelRemoveState.setVisibility(View.GONE);
					mTextDeleteSelected.setVisibility(View.GONE);
					setTitleText(false);
				}

				view.clearAnimation();
				view.setAnimation(animation2);
				view.startAnimation(animation2);
			} else if (animation == animation2) {
				mQueueAdapter.notifyDataSetChanged();
			}
			/*
			 * else { isBackOfCardShowing=!isBackOfCardShowing; //
			 * findViewById(R.id.button1).setEnabled(true); }
			 */
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}

		public void setNotPlaylistResultImage(ViewHolder1 viewHolder,
				Track mediaItem) {
			try {
				final String imageUrl = ImagesManager
						.getMusicArtSmallImageUrl(mediaItem.getImagesUrlArray());
				// mediaItem.getImageUrl();
				if (imageUrl != null && viewHolder.ivAlbumImage != null)
					picasso.load(null, imageUrl, viewHolder.ivAlbumImage,
							R.drawable.background_home_tile_album_default);
			} catch (Exception e) {
				Logger.printStackTrace(e);
				// viewHolder.ivAlbumImage
				// .setBackgroundResource(R.drawable.background_home_tile_album_default);
				viewHolder.ivAlbumImage
						.setImageResource(R.drawable.background_home_tile_album_default);

			}
		}
	}

	private SwipeDismissList mSwipeList;
	private final static String EXTRA_MODE = "MODE";
	boolean isDeleting = false;

	private PlayerStateReceiver playerStateReceiver;

	private void setUpSwipeView() {
		int modeInt = 0;
		SwipeDismissList.UndoMode mode = SwipeDismissList.UndoMode.values()[modeInt];

		// Get the regular ListView of this activity.
		// Create a new SwipeDismissList from the activities listview.
		mSwipeList = new SwipeDismissList(mTrackListView,
				new SwipeDismissList.OnDismissCallback() {
					public SwipeDismissList.Undoable onDismiss(
							AbsListView listView, final int position) {

						isDeleting = true;
						// Get item that should be deleted from the adapter.
						// final String item = mAdapter.getItem(position);
						final Object item = mQueueAdapter.getItem(position);
						final Track track = mTracks.get(position);

						// Delete that item from the adapter.
						// mQueueAdapter.remove(position);
						// mQueueAdapter.remove(position);
						mTracks.remove(position);
						mTrackRemoveState.remove(position);
						// initTrackRemoveState();
						Logger.i("Notify",
								"Notify::::::::::  SwipeDismissList.Undoable onDismiss: Count:"
										+ mTracks.size());
						mQueueAdapter.notifyDataSetChanged();
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
						return new SwipeDismissList.Undoable() {
							@Override
							public String getTitle() {
								return track.getTitle() + " deleted";
							}

							@Override
							public void undo() {
								// Reinsert the item at its previous position.
								mTracks.add(position, track);
								mTrackRemoveState.add(position, false);
								Logger.i("Notify",
										"Notify::::::::::  undo Count:"
												+ mTracks.size());
								// initTrackRemoveState();
								mQueueAdapter.notifyDataSetChanged();
								isDeleting = false;
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
								// mTrackRemoveState.remove(position);
								isDeleting = false;
								removeFromQueueList(position);
								// }
								// }, 200);
							}
						};

					}
				},
				// 3rd parameter needs to be the mode the list is generated.
				mode);
		mSwipeList.setRequireTouchBeforeDismiss(true);

	}

	private void removeFromQueueList(final int j) {
		// new Thread() {
		// public void run() {
		try {

			int i = j;
			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(FlurryConstants.FlurryMediaDetailActions.ActionTaken
					.toString(), FlurryConstants.FlurryAllPlayer.DeleteSong
					.toString());
			Analytics.logEvent(
					FlurryConstants.FlurryAllPlayer.PlayerQueueViewed
							.toString(), reportMap);

			List<Track> mTracks = PlayerService.service.getPlayingQueue();
			ArrayList<Boolean> mTrackRemoveState = new ArrayList<Boolean>();
			mTrackRemoveState.addAll(this.mTrackRemoveState);
			// xtpl
			mTrackRemoveState.add(i, true);
			// remove track from queue
			// RelativeLayout tile = (RelativeLayout)
			// view.getParent().getParent();
			ArrayList<Long> idsToRemove = new ArrayList<Long>();
			// Track track = (Track) tile.getTag(R.id.view_tag_object);
			// for (int i = 0; i < mTrackRemoveState.size(); i++) {
			if (mTrackRemoveState.get(i)) {
				Track track = mTracks.get(i);
				if (track != null) {
					CacheState cacheState = DBOHandler.getTrackCacheState(
							getActivity(), "" + track.getId());
					Logger.s(track.getTitle() + " ::::::delete:::::: "
							+ cacheState);
					if (cacheState != CacheState.CACHED
							&& cacheState != CacheState.CACHING) {
						boolean isTracksDeleted = DBOHandler.deleteCachedTrack(
								getActivity(), "" + track.getId());
						Logger.s(track.getTitle() + " ::::::delete:::::: "
								+ isTracksDeleted);
						if (isTracksDeleted) {
							Intent TrackCached = new Intent(
									CacheManager.ACTION_TRACK_CACHED);
							Logger.i("Update Cache State",
									" SENDING BROADCAST TRACK_CACHED");
							getActivity().sendBroadcast(TrackCached);
						}
					}
				}
				// int position = (Integer)
				// tile.getTag(R.id.view_tag_position);
				// removes the original from the player.
				if (track != null)
					idsToRemove.add(track.getId());
				mTracks.remove(i);
				mTrackRemoveState.remove(i);
				// mPlayerBarFragment.removeFrom(i);
				i--;
			}
			// }
			if (!idsToRemove.isEmpty())
				removeTrack(idsToRemove);
			// updates the current list.
			mTracks = PlayerService.service.getPlayingQueue();
			PlayerQueueActivity.this.mTracks = mTracks;
			PlayerQueueActivity.this.mTrackRemoveState = mTrackRemoveState;

			// initTrackRemoveState();

			Logger.i("Notify", "Notify:::::::::: Remove Item:" + i + "Count:"
					+ mTracks.size());
			// getActivity().runOnUiThread(new Runnable() {
			//
			// @Override
			// public void run() {
			// if (!isDeleting)
			// if (mSwipeList != null && !mSwipeList.isUndoMessageShowing())
			// mQueueAdapter.notifyDataSetChanged();
			setTitleText(false);
			// }
			// });
			// };
			// }.start();
		} catch (Error e) {
			// TODO: handle exception
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	protected void onDestroy() {
		if (cacheStateReceiver != null)
			unregisterReceiver(cacheStateReceiver);
		if (PlayerService.service != null)
			PlayerService.service.unregisterPlayerStateListener(this);

		Utils.clearCache();
		try {
			PlayerServiceBindingManager.unbindFromService(mServiceToken);
		} catch (Exception e) {
		}


		super.onDestroy();
	}

	// @Override
	// public void onDestroyView() {
	// mSwipeList.discardUndo();
	// System.gc();
	// System.runFinalization();
	// super.onDestroyView();
	// }

	class PlayerStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
            if (mQueueAdapter != null) {
                mQueueAdapter.notifyDataSetChanged();
            }
        }
	}

	// private boolean isFromReciver = true;

	public void onResume() {
		ScreenLockStatus.getInstance(getBaseContext()).onResume(this, this);

		// System.out.println(" :::-------onResume--------- " +
		// System.currentTimeMillis());
		// setTitleText(false);
		super.onResume();

		try {
			if (playerStateReceiver == null) {
				playerStateReceiver = new PlayerStateReceiver();
				getActivity().registerReceiver(
						playerStateReceiver,
						new IntentFilter(
								PlayerBarFragment.ACTION_PLAY_STATE_CHANGED));
			}
		} catch (Exception e) {
		} catch (java.lang.Error e) {
			Utils.clearCache();
		}

		if (cacheStateUpdated) {
			mQueueAdapter.notifyDataSetChanged();
		}

		if (!mApplicationConfigurations.isPlayerQueueHintChecked()) {
			handler.postDelayed(runnableQueueHint, 100);
			// new Handler().postDelayed(new Runnable() {
			//
			// @Override
			// public void run() {
			// try {
			// // View Settings = getActivity().findViewById(
			// // R.id.player_queue_title_bar_button_clear);
			// // Settings.setDrawingCacheEnabled(true);
			// // Bitmap bmp = Settings.getDrawingCache();
			// // int[] containerLocation = new int[2];
			// // Settings.getLocationInWindow(containerLocation);
			// //
			// // View Options = getActivity().findViewById(
			// // R.id.player_queue_title_bar_button_more);
			// // Options.setDrawingCacheEnabled(true);
			// // Bitmap bmpOptions = Options.getDrawingCache();
			// // int[] containerLocationOptions = new int[2];
			// // Options.getLocationInWindow(containerLocationOptions);
			// //
			// // View dragHandle = rootView
			// // .findViewById(R.id.player_queue_media_drag_handle);
			// // dragHandle.setDrawingCacheEnabled(true);
			// // Bitmap bmpDrag = dragHandle.getDrawingCache();
			// // int[] containerLocationDrag = new int[2];
			// // dragHandle.getLocationInWindow(containerLocationDrag);
			//
			// View listItem;
			// int[] containerLocationItem = new int[2];
			// if (mTrackListView.getChildCount() > 3) {
			// listItem = mTrackListView.getChildAt(3);
			// listItem.getLocationInWindow(containerLocationItem);
			// } else {
			// listItem = mTrackListView.getChildAt(0);
			// listItem.getLocationInWindow(containerLocationItem);
			// }
			// listItem.setDrawingCacheEnabled(true);
			// // Bitmap bmpItem = dragHandle.getDrawingCache();
			// Bitmap bmpItem = loadBitmapFromView(listItem);
			//
			// // HelpView settingHelp = new HelpView(
			// // containerLocation[0], containerLocation[1], bmp);
			// // HelpView OptionsHelp = new HelpView(
			// // containerLocationOptions[0],
			// // containerLocationOptions[1], bmpOptions);
			// // HelpView OptionsDrag = new HelpView(
			// // containerLocationDrag[0],
			// // containerLocationDrag[1], bmpDrag);
			// HelpView listItemHelp = new HelpView(
			// containerLocationItem[0],
			// containerLocationItem[1], bmpItem);
			//
			// // AppGuideActivityPlayerQueue.classObject = new
			// // HelpLeftDrawer(
			// // settingHelp, OptionsHelp, OptionsDrag,
			// // listItemHelp);
			// if (!isFinishing()) {
			// AppGuideActivityPlayerQueue.classObject = new HelpLeftDrawer(
			// null, null, null, listItemHelp);
			//
			// Intent intent = new Intent(getActivity(),
			// AppGuideActivityPlayerQueue.class);
			// startActivity(intent);
			// mApplicationConfigurations
			// .setPlayerQueueHintChecked(true);
			// }
			// } catch (Exception e) {
			// Logger.printStackTrace(e);
			// }
			// }
			// }, 1000);
		}
		setTitleText(false);
		if (mQueueAdapter != null)
			mQueueAdapter.notifyDataSetChanged();
		// System.out.println(" :::-------onResume---------1 " +
		// System.currentTimeMillis());
	}

	Runnable runnableQueueHint = new Runnable() {
		@Override
		public void run() {
			try {
				if (mTrackListView.getChildCount() == 0) {
					handler.postDelayed(runnableQueueHint, 1000);
					return;
				}
				View listItem;
				int[] containerLocationItem = new int[2];
				if (mTrackListView.getChildCount() > 3) {
					listItem = mTrackListView.getChildAt(3);
					listItem.getLocationInWindow(containerLocationItem);
				} else {
					listItem = mTrackListView.getChildAt(0);
					listItem.getLocationInWindow(containerLocationItem);
				}
				listItem.setDrawingCacheEnabled(true);

				Bitmap bmpItem = loadBitmapFromView(listItem);
				HelpView listItemHelp = new HelpView(containerLocationItem[0],
						containerLocationItem[1], bmpItem);

				if (!isFinishing()) {
					AppGuideActivityPlayerQueue.classObject = new HelpLeftDrawer(
							null, null, null, listItemHelp);

					Intent intent = new Intent(getActivity(),
							AppGuideActivityPlayerQueue.class);
					startActivity(intent);
					mApplicationConfigurations.setPlayerQueueHintChecked(true);
				}
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}
	};

	public void finish() {
		try {
			if (mSwipeList != null)
				mSwipeList.discardUndo();
		} catch (Exception e) {
		}
		handler.removeCallbacks(runnableQueueHint);
		super.finish();
	};

	public static Bitmap loadBitmapFromView(View v) {
		Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(),
				Utils.bitmapConfig8888);
		Canvas c = new Canvas(b);
		// v.layout(0, 0, v.getWidth(), v.getHeight());
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
	public void onBackPressed() {
		Utils.clearCache();
		int totalSelected = 0;
		if (mTrackRemoveState != null && mTrackRemoveState.size() > 0) {
			for (int i = 0; i < mTrackRemoveState.size(); i++) {
				if (mTrackRemoveState.get(i))
					totalSelected = totalSelected + 1;
			}
		}
		if (totalSelected != 0) {
			cancelClick();
		} else {
			finish();
		}
	}

	@Override
	public void onStartLoadingTrack(Track track) {
		// TODO Auto-generated method stub
		if (mQueueAdapter != null)
			mQueueAdapter.notifyDataSetChanged();
	}

	@Override
	public void onTrackLoadingBufferUpdated(Track track, int precent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStartPlayingTrack(Track track) {
		// TODO Auto-generated method stub
		if (mQueueAdapter != null)
			mQueueAdapter.notifyDataSetChanged();
	}

	@Override
	public void onFinishPlayingTrack(Track track) {
		// TODO Auto-generated method stub
		if (mQueueAdapter != null)
			mQueueAdapter.notifyDataSetChanged();
	}

	@Override
	public void onFinishPlayingQueue() {
		// TODO Auto-generated method stub
		if (mQueueAdapter != null)
			mQueueAdapter.notifyDataSetChanged();
	}

	@Override
	public void onSleepModePauseTrack(Track track) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onErrorHappened(
			com.hungama.myplay.activity.player.PlayerService.Error error) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStartPlayingAd(Placement audioad) {
		finish();
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

	@Override
	protected void onStart() {
		super.onStart();
		if (PlayerService.service == null) {
			mServiceToken = PlayerServiceBindingManager.bindToService(
					getActivity(), this);
		}
	}
}
