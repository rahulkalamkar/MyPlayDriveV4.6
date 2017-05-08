package com.hungama.myplay.activity.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.LruCache;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.DataBase.CacheState;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.campaigns.PlacementType;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.SearchResponse;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.operations.hungama.SearchKeyboardOperation;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.ui.DownloadConnectingActivity;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.PagerSlidingTabStrip;
import com.hungama.myplay.activity.ui.adapters.DataNotFoundAdapter;
import com.hungama.myplay.activity.ui.widgets.CustomAlertDialog;
import com.hungama.myplay.activity.ui.widgets.CustomCacheStateProgressBar;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.HorizontalDividerItemDecoration;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.hungama.myplay.activity.util.QuickActionSearchResult;
import com.hungama.myplay.activity.util.QuickActionSearchResult.OnSearchResultListener;
import com.hungama.myplay.activity.util.Utils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class MainSearchResultsFragment extends MainFragment {

	public static final String TAG = "MainSearchResultsFragment";

	public static final String FRAGMENT_ARGUMENT_QUERY = "fragment_argument_query";
	public static final String FRAGMENT_ARGUMENT_TYPE = "fragment_argument_type";

	// public static final String FLURRY_SOURCE_SECTION =
	// "flurry_source_section";
	public static final String FLURRY_SEARCH_ACTION_SELECTED = "flurry_search_action_selected";
	public static final String FROM_FULL_PLAYER = "from_full_player";

//	private  LanguageTextView mTitleResultCount;
//	private  LanguageTextView mTitleResultLabel;
//	private  LanguageTextView mTitleSearchQuery;

	// private Placement placement;

	// private ImageFetcher mImageFetcher = null;

	private  String mLoadingContent;
	private  List<MediaItem> mMediaItems_playlist;

	private  String trackPrefix;
	private  String albumPrefix;
	private  String playlistPrefix;
	private  String artistPrefix;
	private  String videoPrefix;
	private  String playlistAlbumSuffix;

	private Context mContext;
	private  OnSearchResultsOptionSelectedListener mOnSearchResultsOptionSelectedListener;
	// ======================================================
	// Life Cycle.
	// ======================================================
	private DataManager mDataManager;
	private PicassoUtil picasso;

	public static MainSearchResultsFragment newInstance;

    public void setSearchResultsFragment(MainSearchFragmentNew searchResultsFragment) {
        this.searchResultsFragment = searchResultsFragment;
    }

	boolean needToSetBack=true;

	public void setNeedToSetBack(boolean needToSetBack) {
		this.needToSetBack = needToSetBack;
	}

	public MainSearchFragmentNew searchResultsFragment;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		newInstance=this;
		// Caching advertisement bitmaps
		// final int maxMemory = (int) (Runtime.getRuntime().maxMemory() /
		// 1024);
		picasso = PicassoUtil.with(getActivity());
		mContext = getActivity().getApplicationContext();
		mDataManager = DataManager.getInstance(mContext);

		// LayoutInflater mInflater = (LayoutInflater) mContext
		// .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mLoadingContent = Utils
				.getMultilanguageTextLayOut(
						mContext,
						getResources()
								.getString(
										R.string.search_results_loading_indicator_loading_more));

//        setHasOptionsMenu(true);

		if(needToSetBack) {
			try{
				((MainActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(
						R.drawable.abc_ic_ab_back_mtrl_am_alpha_normal);
				((MainActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
				((MainActivity) getActivity()).mToolbar.setNavigationOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (getActivity() != null)
							getActivity().onBackPressed();
					}
				});
				((MainActivity) getActivity()).isSkipResume = true;
			}catch (Exception e){
			}

		}
		Analytics.postCrashlitycsLog(getActivity(), MainSearchResultsFragment.class.getName());
	}

	View rootView;
	Boolean from_full_player = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (rootView == null) {
			Bundle data = getArguments();
			from_full_player = data.getBoolean(FROM_FULL_PLAYER);
			rootView = inflater.inflate(R.layout.fragment_main_search_results,
					container, false);
			Logger.i("Tag", "Search detail screen:4");
			if (!from_full_player) {
				initializeSearchResultContents();
			} else {
				// new Handler().postDelayed(new Runnable() {
				//
				// @Override
				// public void run() {
				initializeSearchResultContents();
				// }
				// }, 1000);
			}

		} else {
			Logger.e("HomeMediaTileGridFragment", "onCreateView else");
			ViewGroup parent = (ViewGroup) Utils.getParentViewCustom(rootView);
			parent.removeView(rootView);
		}

		Logger.i("Tag", "Search detail screen:5");
		return rootView;
	}

	private void initializeSearchResultContents() {

		rootView.findViewById(R.id.main_search_results_loading_indicator)
				.setVisibility(View.VISIBLE);
		Bundle data = getArguments();
		try {
			trackPrefix = Utils
					.getMultilanguageText(
							mContext,
							getResources()
									.getString(
											R.string.search_results_layout_bottom_text_for_track));
			albumPrefix = Utils
					.getMultilanguageText(
							mContext,
							getResources()
									.getString(
											R.string.search_results_layout_bottom_text_for_album));
			playlistPrefix = Utils
					.getMultilanguageText(
							mContext,
							getResources()
									.getString(
											R.string.search_results_layout_bottom_text_for_playlist));
			artistPrefix = Utils.getMultilanguageText(
					mContext,
					getResources().getString(
							R.string.search_result_line_type_and_name_artist));
			videoPrefix = Utils
					.getMultilanguageText(
							mContext,
							getResources()
									.getString(
											R.string.search_results_layout_bottom_text_for_video));
			playlistAlbumSuffix = Utils
					.getMultilanguageText(
							mContext,
							getResources()
									.getString(
											R.string.search_results_layout_bottom_text_album_playlist));

		} catch (Exception e) {
		} catch (Error e) {

		}

		try {
			ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
					.getInstance(mContext);
			if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
				Utils.traverseChild(rootView, getActivity());
			}
//			rootView.findViewById(R.id.main_search_results_title)
//					.setOnClickListener(new View.OnClickListener() {
//						@Override
//						public void onClick(View v) {
//						}
//					});

			query = data.getString(FRAGMENT_ARGUMENT_QUERY);
			Logger.e("query on onCreateView", query);
			mDataManager.getSearchKeyboard(query,
					MainSearchFragment.SEARCH_FILTER_TYPE_ALL,
					String.valueOf(LocalListFragment.RESULT_MINIMUM_INDEX),
					String.valueOf(LocalListFragment.RESULT_TO_PRESENT),
					searchOperation);

			initializeUserControls(rootView);

		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}

	}

	private  String query;

	@Override
	public void onStart() {
		super.onStart();
		Bundle data = getArguments();
		query = data.getString(FRAGMENT_ARGUMENT_QUERY);
		// if (adapter != null) {
		// LocalListFragment discovery = (LocalListFragment) adapter
		// .getCurrentFragment(mDeafultOpenedTab);
		// discovery.mSearchResultsAdapter.notifyDataSetChanged();
		//
		// if (discovery.mSearchResultsAdapter == null
		// || discovery.mSearchResultsAdapter.getCount() == 0) {
		// if (!isResumed()) {
		//
		// // String type = data.getString(FRAGMENT_ARGUMENT_TYPE);
		// }
		// }
		// }
		// FlurryAgent.setContinueSessionMillis(Constants.FLURRY_SESSION_LENGTH);
		// FlurryAgent.onStartSession(getActivity(),
		// getString(R.string.flurry_app_key));
		Analytics.startSession(getActivity(), this);
	}

	@Override
	public void onResume() {
		super.onResume();

		// ((MainSearchFragment) getActivity()).search.clearFocus();
		// if (mImageFetcher != null) {
		// mImageFetcher.setExitTasksEarly(false);
		// }
	}

	@Override
	public void onPause() {
		super.onPause();

		// if (mImageFetcher != null) {
		// mImageFetcher.setExitTasksEarly(true);
		// mImageFetcher.flushCache();
		// }
	}

	@Override
	public void onStop() {
		// cancels any running operation.

		super.onStop();

		Analytics.onEndSession(getActivity());
	}

	@Override
	public void onDestroy() {

		try {
			if (alertDialogBuilder != null)
				alertDialogBuilder.cancel();
		} catch (Exception e) {
		}
        try {
            ((MainActivity)getActivity()).mToolbar.setNavigationOnClickListener(null);
        }catch (Exception e){
                e.printStackTrace();
        }
		Utils.destroyFragment();
		if(rootView!=null)
			Utils.unbindDrawables(rootView);
		super.onDestroy();
	}

	// ======================================================
	// Public.
	// ======================================================

	public interface OnSearchResultsOptionSelectedListener {

		public void onPlayNowSelected(MediaItem mediaItem);

		public void onAddToQueueSelected(MediaItem mediaItem);

		public void onShowDetails(MediaItem mediaItem, boolean addToQueue);

		public void onSaveOffline(MediaItem mediaItem);

		public void onFinishSongCatcher(boolean isFinishSongCatcher);

		public void onShowDetails(MediaItem mediaItem, List<MediaItem> items,
				boolean addToQueue);
	}

	public void setOnSearchResultsOptionSelectedListener(
			OnSearchResultsOptionSelectedListener listener) {
		mOnSearchResultsOptionSelectedListener = listener;
	}

	// ======================================================
	// Helper methods.
	// ======================================================

	private void initializeUserControls(View rootView) {
//		mTitleResultCount = (LanguageTextView) rootView
//				.findViewById(R.id.main_search_results_title_text_count);
//		mTitleResultLabel = (LanguageTextView) rootView
//				.findViewById(R.id.main_search_results_title_label_result_for);
//		mTitleSearchQuery = (LanguageTextView) rootView
//				.findViewById(R.id.main_search_results_title_text_search_query);
		// mListResults = (ListView) rootView
		// .findViewById(R.id.main_search_results_list);
		// mListResults.setOnScrollListener(new ScrollToBottomListener());
		tabs = (PagerSlidingTabStrip) rootView.findViewById(R.id.tabs);
		pager = (ViewPager) rootView.findViewById(R.id.pager);

		tabs.setTextSize((int) getResources().getDimension(
				R.dimen.xlarge_text_size));

		tabs.setTextColor(getResources().getColor(R.color.white_transparant));
		tabs.setIndicatorColor(getResources().getColor(R.color.white));
		tabs.setUnderlineColor(getResources().getColor(R.color.white));
		tabs.setActivateTextColor(getResources().getColor(R.color.white));
		tabs.setDeactivateTextColor(getResources().getColor(
				R.color.white_transparant));
		tabs.setDividerColor(getResources().getColor(R.color.transparent));
		tabs.setTabSwitch(true);
		tabs.setUnderlineHeight(0);
		tabs.setIndicatorHeight(7);

		// mSearchResultsAdapter = new SearchResultsAdapter();

		// mListResults.setAdapter(mSearchResultsAdapter);
	}

	private CommunicationOperationListener searchOperation = new CommunicationOperationListener() {
		@Override
		public void onSuccess(int operationId,
				final Map<String, Object> responseObjects) {

			Logger.s("-----------------Search result---------------"
					+ operationId);
			List<MediaItem> mMediaItems;
			SearchResponse mSearchResponse = null;
			try {
				if (operationId == OperationDefinition.Hungama.OperationId.SEARCH) {

					if (responseObjects
							.containsKey(SearchKeyboardOperation.RESPONSE_KEY_TOAST)) {

						displayResultNotFountMessage(
								""
										+ responseObjects
												.get(SearchKeyboardOperation.RESPONSE_KEY_TOAST),
								true);
						return;
					}

					mSearchResponse = (SearchResponse) responseObjects
							.get(SearchKeyboardOperation.RESPONSE_KEY_SEARCH);

					String query = (String) responseObjects
							.get(SearchKeyboardOperation.RESPONSE_KEY_QUERY);
					String type = (String) responseObjects
							.get(SearchKeyboardOperation.RESPONSE_KEY_TYPE);

					mSearchResponse.setQuery(query);
					mSearchResponse.setType(type);

					// it's a new search results.
					mMediaItems = mSearchResponse.getContent();

					Logger.s("-----------------mMediaItems**---------------"
							+ mMediaItems);

					if (mMediaItems != null && !Utils.isListEmpty(mMediaItems)) {
						MediaType media_type = mMediaItems.get(0)
								.getMediaType();
						Logger.e("media_type", media_type.toString());

						if (media_type == MediaType.PLAYLIST) {
							mDeafultOpenedTab = POS_PLAYLIST;
						} else if (media_type == MediaType.TRACK) {
							mDeafultOpenedTab = POS_SONG;
						} else if (media_type == MediaType.VIDEO) {
							mDeafultOpenedTab = POS_VIDEO;
						} else if (media_type == MediaType.ALBUM) {
							mDeafultOpenedTab = POS_ALBUMS;
						}

						mMediaItems_playlist = new ArrayList<MediaItem>();
						if (mDeafultOpenedTab == POS_PLAYLIST) {
							mMediaItems_playlist.add(mMediaItems.get(0));
						}
					} else {
						displayResultNotFountMessage(query, false);
						return;
					}
					// mLoadingBar.setVisibility(View.GONE);
				}
			} catch (Exception e) {
				e.printStackTrace();
				Logger.printStackTrace(e);

			}

			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					try {
						rootView.findViewById(
								R.id.main_search_results_loading_indicator)
								.setVisibility(View.GONE);
						setUpViewpager(mDeafultOpenedTab);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, 50);

		}

		@Override
		public void onStart(int operationId) {
		}

		@Override
		public void onFailure(int operationId, ErrorType errorType,
				String errorMessage) {
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					try {
						displayResultNotFountMessage(query, false);
						rootView.findViewById(
								R.id.main_search_results_loading_indicator)
								.setVisibility(View.GONE);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, 50);
		}
	};

	CustomAlertDialog alertDialogBuilder;

	private void displayResultNotFountMessage(String query,
			boolean isDirectDisplay) {
		try {
			if (!isDetached() && getActivity() != null) {
				String message = "";
				// if(isDirectDisplay)
				// message=query;p
				// else
				// message = getString(R.string.txt_no_search_result_alert_msg,
				// query);
				message = getString(R.string.txt_no_search_result_alert_msg1);
				alertDialogBuilder = new CustomAlertDialog(getActivity());
				// set dialog message
				alertDialogBuilder.setMessage(message);
				alertDialogBuilder.setCancelable(false);
				alertDialogBuilder.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								try {
									getActivity().onBackPressed();
								} catch (Exception e) {
									dialog.cancel();
								}

							}
						});

				alertDialogBuilder.show();
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	private  void showTitle() {
//		mTitleResultCount.setVisibility(View.VISIBLE);
//		mTitleResultLabel.setVisibility(View.VISIBLE);
//		mTitleSearchQuery.setVisibility(View.VISIBLE);
	}

	private  void hideTitle() {
//		mTitleResultCount.setVisibility(View.GONE);
//		mTitleResultLabel.setVisibility(View.GONE);
//		mTitleSearchQuery.setVisibility(View.GONE);
	}

	private CacheStateReceiver cacheStateReceiver;

	private class CacheStateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if (arg1.getAction()
					.equals(CacheManager.ACTION_CACHE_STATE_UPDATED)
					|| arg1.getAction()
							.equals(CacheManager.ACTION_TRACK_CACHED)) {
				try {
					((MainActivity) getActivity()).mPlayerBarFragment
							.updatedCurrentTrackCacheState();
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else if (arg1.getAction().equals(
					CacheManager.ACTION_UPDATED_CACHE)) {

			} else if (arg1.getAction().equals(
					CacheManager.ACTION_VIDEO_CACHE_STATE_UPDATED)
					|| arg1.getAction().equals(
							CacheManager.ACTION_VIDEO_TRACK_CACHED)) {

			} else if (arg1.getAction().equals(
					CacheManager.ACTION_VIDEO_UPDATED_CACHE)) {

			}
		}
	}

	@Override
	public void onAttach(Activity activity) {
		if (cacheStateReceiver == null) {
			cacheStateReceiver = new CacheStateReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(CacheManager.ACTION_CACHE_STATE_UPDATED);
			filter.addAction(CacheManager.ACTION_TRACK_CACHED);
			filter.addAction(CacheManager.ACTION_UPDATED_CACHE);
			filter.addAction(CacheManager.ACTION_VIDEO_CACHE_STATE_UPDATED);
			filter.addAction(CacheManager.ACTION_VIDEO_TRACK_CACHED);
			filter.addAction(CacheManager.ACTION_VIDEO_UPDATED_CACHE);
			getActivity().registerReceiver(cacheStateReceiver, filter);
		}
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		if (cacheStateReceiver != null)
			getActivity().unregisterReceiver(cacheStateReceiver);
		super.onDetach();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@SuppressLint("ValidFragment")
    public static class LocalListFragment extends Fragment implements
			CommunicationOperationListener {

		RecyclerView mListResults;
		SearchResultsAdapter mSearchResultsAdapter;
		private List<MediaItem> mMediaItems;
		private View mLoadingBar;
		private View rootView;
		Vector<String> viewedPositions;

		private boolean mIsThrottling = false;
		private LruCache<String, BitmapDrawable> mMemoryCache;
		private int width;
		public static final int RESULT_MINIMUM_INDEX = 1;
		public static final int RESULT_TO_PRESENT = 30;

		private DataManager mDataManager;
		private Context mContext;
		private Handler h;

		private SearchResponse mSearchResponse = null;

		private String backgroundLink;

		private HashMap<Integer, Placement> mPlacementMap = new HashMap<Integer, Placement>();

		private final Handler handler = new Handler();

		public static LocalListFragment newInstance() {
			LocalListFragment fragment = new LocalListFragment();
			return fragment;
		}

		public LocalListFragment() {
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			viewedPositions = new Vector<String>();

			final int cacheSize = 2 * 1024 * 1024;
			mMemoryCache = new LruCache<String, BitmapDrawable>(cacheSize) {
				@Override
				protected int sizeOf(String key, BitmapDrawable bitmap) {
					try {
						// The cache size will be measured in kilobytes rather
						// than
						// number of items.
						return bitmap.getBitmap().getRowBytes()
								* bitmap.getBitmap().getHeight() / 1024;
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
					return 0;
				}
			};
			mContext = getActivity().getApplicationContext();
			mDataManager = DataManager.getInstance(mContext);
			// setRetainInstance(true);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			if (rootView == null) {
				rootView = inflater.inflate(R.layout.search_result_fragment,
						container, false);

				Bundle data = getArguments();
				String query = data.getString(FRAGMENT_ARGUMENT_QUERY);
				String type = data.getString(FRAGMENT_ARGUMENT_TYPE);

				mListResults = (RecyclerView) rootView
						.findViewById(R.id.main_search_results_list);
				LinearLayoutManager mLayoutManager = new LinearLayoutManager(
						getActivity());
				mListResults.setLayoutManager(mLayoutManager);

				mListResults
						.addItemDecoration(new HorizontalDividerItemDecoration.Builder(
								getActivity())
								.color(getResources()
										.getColor(
												R.color.media_details_listview_seperator_color))
								.size(getResources().getDimensionPixelSize(
										R.dimen.media_details_seperetor_height))
								.build());

				boolean needScrollListener = false;
				if (newInstance.mMediaItems_playlist != null
						&& newInstance.mMediaItems_playlist.size() > 0
						&& type.equalsIgnoreCase(MainSearchFragment.SEARCH_FILTER_TYPE_SONGS)) {

				} else
					needScrollListener = true;

				if (newInstance.mMediaItems_playlist != null
						&& newInstance.mMediaItems_playlist.size() > 0
						&& type.equalsIgnoreCase(MainSearchFragment.SEARCH_FILTER_TYPE_SONGS)) {

				} else
					needScrollListener = true;
				// mListResults
				// .setOnScrollListener(new ScrollToBottomListener());
				if (needScrollListener)
					mListResults
							.setOnScrollListener(new RecyclerView.OnScrollListener() {

								@Override
								public void onScrollStateChanged(
										RecyclerView view, int scrollState) {
									if (mIsThrottling)
										return;

									if (scrollState == RecyclerView.SCROLL_STATE_IDLE) {
										try {
											postAdForPosition();
										} catch (Exception e) {
											e.printStackTrace();
										}
									}

									LinearLayoutManager layoutManager = (LinearLayoutManager) mListResults
											.getLayoutManager();

									int visibleItemCount = mListResults
											.getChildCount();
									int totalItemCount = layoutManager
											.getItemCount();
									int firstVisibleItem = layoutManager
											.findFirstVisibleItemPosition();

									Logger.v(TAG, "totalItemCount "
											+ totalItemCount
											+ " currentFirstVisibleItem "
											+ firstVisibleItem
											+ " currentVisibleItemCount "
											+ visibleItemCount);

									if (scrollState == RecyclerView.SCROLL_STATE_IDLE
											&& firstVisibleItem > 0) {

										// firstVisibleItem + visibleItemCount
										// == totalItemCount
										Logger.v(TAG, "totalItemCount inner");

										boolean lastItemVisible = (firstVisibleItem + visibleItemCount) == totalItemCount;
										boolean needMorePages = !Utils
												.isListEmpty(mMediaItems)
												&& mMediaItems.size() < mSearchResponse
														.getTotalCount()
												&& (mMediaItems.size()
														% Constants.LOADING_CHUNK_NUMBER == 0);

										if (backgroundLink != null) {
											needMorePages = !Utils
													.isListEmpty(mMediaItems)
													&& (mMediaItems.size() - (mMediaItems
															.size() / 5)) < mSearchResponse
															.getTotalCount()
													&& ((mMediaItems.size() - (mMediaItems
															.size() / 5))
															% Constants.LOADING_CHUNK_NUMBER == 0);
										}

										if (lastItemVisible && needMorePages) {
											Logger.v(TAG,
													"More Items are requested - throttling !!!");
											throttleForNextPage();
										}
									}
								}

							});

				mLoadingBar = (View) rootView
						.findViewById(R.id.main_search_results_loading_indicator);

				mMediaItems = new ArrayList<MediaItem>();
				mSearchResultsAdapter = new SearchResultsAdapter();
				mListResults.setAdapter(mSearchResultsAdapter);

				ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
						.getInstance(mContext);
				if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
					Utils.traverseChild(rootView, getActivity());
				}

				searchForQueury(query, type);

			} else {
				ViewGroup parent = (ViewGroup) Utils.getParentViewCustom(rootView);
				parent.removeView(rootView);
			}
			return rootView;
		}

		private void refreshAdapter() {
			if (mSearchResultsAdapter != null) {
				mSearchResultsAdapter.notifyDataSetChanged();
			}
		}

		// ======================================================
		// Communication.
		// ======================================================

		@Override
		public void onStart(int operationId) {
			if (operationId == OperationDefinition.Hungama.OperationId.SEARCH
					|| operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {

				if (mIsThrottling) {
					Utils.makeText(getActivity(), newInstance.mLoadingContent,
							Toast.LENGTH_SHORT).show();

				} else {
					if (!Utils.isListEmpty(mMediaItems)) {
						mMediaItems.clear();
						if (newInstance.adapter != null) {
							LocalListFragment discovery = (LocalListFragment) newInstance.adapter
									.getCurrentFragment(newInstance.mDeafultOpenedTab);
							discovery.mSearchResultsAdapter
									.notifyDataSetChanged();
						}
					}

					newInstance.hideTitle();

					mLoadingBar.setVisibility(View.VISIBLE);
				}
			}
		}

		CustomAlertDialog alertDialogBuilder;

		private void displayResultNotFountMessage(String query,
				boolean isDirectDisplay) {
			try {
				if (!isDetached() && getActivity() != null) {
					String message = "";

					message = getString(R.string.txt_no_search_result_alert_msg1);
					alertDialogBuilder = new CustomAlertDialog(getActivity());
					// set dialog message
					alertDialogBuilder.setMessage(message);
					alertDialogBuilder.setCancelable(false);
					alertDialogBuilder.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									try {
										getActivity().onBackPressed();
									} catch (Exception e) {
										dialog.cancel();
									}

								}
							});

					alertDialogBuilder.show();
				}
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}

		private boolean firstPositionPost = true;

		@Override
		public void onSuccess(int operationId,
				Map<String, Object> responseObjects) {
			try {

				if (operationId == OperationDefinition.Hungama.OperationId.SEARCH) {
					try {
						Logger.s("-----------------Search result local list---------------"
								+ mIsThrottling);
						if (responseObjects
								.containsKey(SearchKeyboardOperation.RESPONSE_KEY_TOAST)) {
							Utils.makeText(
									getActivity(),
									""
											+ responseObjects
													.get(SearchKeyboardOperation.RESPONSE_KEY_TOAST),
									1);

//							mTitleResultCount
//									.setText(Utils.getMultilanguageTextLayOut(
//											mContext, "No"));
//							mTitleResultLabel
//									.setText(Utils
//											.getMultilanguageTextLayOut(
//													mContext,
//													getResources()
//															.getString(
//																	R.string.search_results_layout_top_text_results_for)));
//							String query = (String) responseObjects
//									.get(SearchKeyboardOperation.RESPONSE_KEY_QUERY);
//							mTitleSearchQuery.setText(query);
							newInstance.showTitle();
							mLoadingBar.setVisibility(View.GONE);

							return;
						}

						mSearchResponse = (SearchResponse) responseObjects
								.get(SearchKeyboardOperation.RESPONSE_KEY_SEARCH);

						String query = (String) responseObjects
								.get(SearchKeyboardOperation.RESPONSE_KEY_QUERY);
						String type = (String) responseObjects
								.get(SearchKeyboardOperation.RESPONSE_KEY_TYPE);

						mSearchResponse.setQuery(query);
						mSearchResponse.setType(type);

						if (mIsThrottling) {

							mIsThrottling = false;

							List<MediaItem> newMediaItems = mSearchResponse
									.getContent();

							final int lastSize = mMediaItems.size();

							mMediaItems.addAll(newMediaItems);
							if (backgroundLink != null) {
								for (int i = lastSize + (4 - (lastSize % 5)); i < mMediaItems
										.size(); i += 5) {
									mMediaItems
											.add(i,
													new MediaItem(
															i,
															"no",
															"no",
															"no",
															"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ1tMe2aczrJBma6ii3qgKhwzIGa-Vrf6lD1LyVimnxHaQ-oiWLsA",
															"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ1tMe2aczrJBma6ii3qgKhwzIGa-Vrf6lD1LyVimnxHaQ-oiWLsA",
															"no", 0, 0));
								}
							}

							refreshAdapter();

						} else {
							// it's a new search results.
							mMediaItems = mSearchResponse.getContent();

							if (backgroundLink != null) {
								for (int i = 4; i < mMediaItems.size(); i += 5) {
									mMediaItems
											.add(i,
													new MediaItem(
															i,
															"no",
															"no",
															"no",
															"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ1tMe2aczrJBma6ii3qgKhwzIGa-Vrf6lD1LyVimnxHaQ-oiWLsA",
															"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ1tMe2aczrJBma6ii3qgKhwzIGa-Vrf6lD1LyVimnxHaQ-oiWLsA",
															"no", 0, 0));
								}
							}

							if (!Utils.isListEmpty(mMediaItems)) {
								// System.out.println("-----------------Search result loading adapter---------------");
								// set the list.
								if (newInstance.adapter != null) {
									final LocalListFragment discovery = (LocalListFragment) newInstance.adapter
											.getCurrentFragment(newInstance.mDeafultOpenedTab);
									discovery.mSearchResultsAdapter
											.notifyDataSetChanged();

									if (firstPositionPost) {
										firstPositionPost = false;
										handler.postDelayed(new Runnable() {
											@Override
											public void run() {
												try {
													discovery
															.postAdForPosition();
												} catch (Exception e) {
													e.printStackTrace();
												}
											}
										}, 1000);
									}
								}
								// sets the title.
//								mTitleResultCount
//										.setText(Utils.getMultilanguageTextLayOut(
//												mContext,
//												Integer.toString(mSearchResponse
//														.getTotalCount())));
//								if (mMediaItems.size() == 1) {
//									mTitleResultLabel
//											.setText(Utils
//													.getMultilanguageTextLayOut(
//															mContext,
//															getResources()
//																	.getString(
//																			R.string.search_results_layout_top_text_results_for_single)));
//								} else {
//									mTitleResultLabel
//											.setText(Utils
//													.getMultilanguageTextLayOut(
//															mContext,
//															getResources()
//																	.getString(
//																			R.string.search_results_layout_top_text_results_for)));
//								}
//
//								mTitleSearchQuery.setText(Utils
//										.getMultilanguageTextLayOut(mContext,
//												query));
								newInstance.showTitle();

							} else {
								// System.out.println("-----------------Search result no result---------------");
								// sets the title.
//								mTitleResultCount.setText(Utils
//										.getMultilanguageTextLayOut(mContext,
//												"No"));
//								mTitleResultLabel
//										.setText(Utils
//												.getMultilanguageTextLayOut(
//														mContext,
//														getResources()
//																.getString(
//																		R.string.search_results_layout_top_text_results_for)));
//								mTitleSearchQuery.setText(Utils
//										.getMultilanguageTextLayOut(mContext,
//												query));
								newInstance.showTitle();

								// ArrayAdapter<String> adapter = null;
								// adapter = new
								// ArrayAdapter<String>(getActivity(),
								// R.layout.text_layout,
								// new String[] { "Records not available" });
								String message = "";
								if (type.equals(MainSearchFragment.SEARCH_FILTER_TYPE_SONGS)) {
									message = getString(R.string.txt_no_search_result_alert_msg_song);
								} else if (type
										.equals(MainSearchFragment.SEARCH_FILTER_TYPE_PLAYLISTS)) {
									message = getString(R.string.txt_no_search_result_alert_msg_playlist);
								} else if (type
										.equals(MainSearchFragment.SEARCH_FILTER_TYPE_VIDEOS)) {
									message = getString(R.string.txt_no_search_result_alert_msg_video);
								} else if (type
										.equals(MainSearchFragment.SEARCH_FILTER_TYPE_ALBUMS)) {
									message = getString(R.string.txt_no_search_result_alert_msg_album);
								}
								message = Utils.getMultilanguageText(
										getActivity(), message);
								// String
								// message=getString(R.string.txt_no_search_result_alert_msg1);
								DataNotFoundAdapter adapter = new DataNotFoundAdapter(
										message);

								mListResults.setAdapter(adapter);
							}
						}

						// Flurry report:
						Map<String, String> reportMap = new HashMap<String, String>();
						reportMap.put(FlurryConstants.FlurrySearch.SearchTerm
								.toString(), query);
						reportMap.put(FlurryConstants.FlurrySearch.FilterValue
								.toString(), type);
						reportMap.put(
								FlurryConstants.FlurrySearch.NumberOfResults
										.toString(), String.valueOf(mMediaItems
										.size()));
						// FlurryAgent.logEvent(mSearchActionSelected,
						// reportMap);
						Analytics.logEvent(
								FlurryConstants.FlurrySearch.Filter.toString(),
								reportMap);// xtpl
					} catch (Exception e) {
						e.printStackTrace();
						displayResultNotFountMessage(newInstance.query, false);
						return;
					}

					mLoadingBar.setVisibility(View.GONE);
				} else if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
					// gets the given media item to check it's type to retrieve
					// the
					// correct details implementation
					// System.out.println(" ::::::::::::::::::: " +
					// getActivity().getIntent().getBooleanExtra("add_to_queue",
					// false));
					MediaItem mediaItem = (MediaItem) responseObjects
							.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_ITEM);
					if (mediaItem != null) {
						if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
							try {
								MediaSetDetails mMediaSetDetails;
								// get details for albums / playlists.
								mMediaSetDetails = (MediaSetDetails) responseObjects
										.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);

								if (mMediaSetDetails != null) {
									// it's a new search results.

									List<Track> mTracks = mMediaSetDetails
											.getTracks();
									mMediaItems = new ArrayList<MediaItem>();
									mMediaItems.clear();
									for (Track track : mTracks) {
										MediaItem temp = new MediaItem(
												track.getId(),
												track.getTitle(),
												track.getAlbumName(),
												track.getArtistName(),
												track.getImageUrl(),
												track.getBigImageUrl(),
												"track", 0, 0,
												track.getImages(),
												track.getAlbumId());
										mMediaItems.add(temp);
									}
									// mMediaItems =
									// mSearchResponse.getContent();

									if (backgroundLink != null) {
										for (int i = 4; i < mMediaItems.size(); i += 5) {
											mMediaItems
													.add(i,
															new MediaItem(
																	i,
																	"no",
																	"no",
																	"no",
																	"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ1tMe2aczrJBma6ii3qgKhwzIGa-Vrf6lD1LyVimnxHaQ-oiWLsA",
																	"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ1tMe2aczrJBma6ii3qgKhwzIGa-Vrf6lD1LyVimnxHaQ-oiWLsA",
																	"no", 0, 0));
										}
									}

									if (!Utils.isListEmpty(mMediaItems)) {
										// System.out.println("-----------------Search result loading adapter---------------");
										// set the list.
										if (newInstance.adapter != null) {
											LocalListFragment discovery = (LocalListFragment)newInstance. adapter
													.getCurrentFragment(newInstance.mDeafultOpenedTab);
											discovery.mSearchResultsAdapter
													.notifyDataSetChanged();
										}
										// sets the title.
										// mTitleResultCount
										// .setText(Utils
										// .getMultilanguageTextLayOut(
										// mContext,
										// Integer.toString(mSearchResponse
										// .getTotalCount())));
//										if (mMediaItems.size() == 1) {
//											mTitleResultLabel
//													.setText(Utils
//															.getMultilanguageTextLayOut(
//																	mContext,
//																	getResources()
//																			.getString(
//																					R.string.search_results_layout_top_text_results_for_single)));
//										} else {
//											mTitleResultLabel
//													.setText(Utils
//															.getMultilanguageTextLayOut(
//																	mContext,
//																	getResources()
//																			.getString(
//																					R.string.search_results_layout_top_text_results_for)));
//										}
//
//										mTitleSearchQuery.setText(Utils
//												.getMultilanguageTextLayOut(
//														mContext, query));
										newInstance.showTitle();
										mLoadingBar.setVisibility(View.GONE);
									} else {
										// System.out.println("-----------------Search result no result---------------");
										// sets the title.
//										mTitleResultCount.setText(Utils
//												.getMultilanguageTextLayOut(
//														mContext, "No"));
//										mTitleResultLabel
//												.setText(Utils
//														.getMultilanguageTextLayOut(
//																mContext,
//																getResources()
//																		.getString(
//																				R.string.search_results_layout_top_text_results_for)));
//										mTitleSearchQuery.setText(Utils
//												.getMultilanguageTextLayOut(
//										newInstance.				mContext, query));
										newInstance.showTitle();
										// ArrayAdapter<String> adapter = null;
										// adapter = new ArrayAdapter<String>(
										// getActivity(),
										// R.layout.text_layout,
										// new String[] {
										// "Records not available" });
										// String message = getString(
										// R.string.txt_no_search_result_alert_msg,
										// MainSearchFragment.SEARCH_FILTER_TYPE_PLAYLISTS);
										String message = getString(R.string.txt_no_search_result_alert_msg_playlist);
										message = Utils.getMultilanguageText(
												getActivity(), message);

										DataNotFoundAdapter adapter = new DataNotFoundAdapter(
												message);
										mListResults.setAdapter(adapter);
										mLoadingBar.setVisibility(View.GONE);
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
								Logger.printStackTrace(e);
								mLoadingBar.setVisibility(View.GONE);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				Logger.printStackTrace(e);
			}
		}

		@Override
		public void onFailure(int operationId, ErrorType errorType,
				String errorMessage) {
			if (operationId == OperationDefinition.Hungama.OperationId.SEARCH
					|| operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {

				if (!mIsThrottling) {

					// mIsThrottling = false;
					//
					// } else {
					if (!Utils.isListEmpty(mMediaItems)) {
						mMediaItems.clear();
						refreshAdapter();
					}

					newInstance.hideTitle();

					mLoadingBar.setVisibility(View.GONE);
				}

				if (errorType != ErrorType.OPERATION_CANCELLED
						&& !TextUtils.isEmpty(errorMessage)
						&& getActivity() != null) {
					// Toast.makeText(getActivity().getApplicationContext(),
					// errorMessage, Toast.LENGTH_SHORT).show();
					((MainActivity) getActivity())
							.internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
								@Override
								public void onRetryButtonClicked() {
									if (mIsThrottling) {
										mIsThrottling = false;
										throttleForNextPage();
									} else {
										Bundle data = getArguments();
										String query = data
												.getString(FRAGMENT_ARGUMENT_QUERY);
										String type = data
												.getString(FRAGMENT_ARGUMENT_TYPE);

										searchForQueury(query, type);
									}
								}
							});
				}
			}

		}

		private void searchForQueury(String query, String type) {

			// cancels any running operation.
			// mDataManager.cancelGetSearch();

			// sets the flag to indicate any new response is as brand new.
			mIsThrottling = false;

			// Querying like a bous!
			try {
				query = URLEncoder.encode(query, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				// something is wrong with the query - void the request.
				return;
			}

			if (newInstance.mMediaItems_playlist != null
					&& newInstance.mMediaItems_playlist.size() > 0
					&& type.equalsIgnoreCase(MainSearchFragment.SEARCH_FILTER_TYPE_SONGS)) {
				mDataManager.getMediaDetails(newInstance.mMediaItems_playlist.get(0), null,
						this);
			} else
				mDataManager.getSearchKeyboard(query, type,
						String.valueOf(RESULT_MINIMUM_INDEX),
						String.valueOf(RESULT_TO_PRESENT),
						LocalListFragment.this);
		}

		private void throttleForNextPage() {

			int currentPagingIndex = mMediaItems.size();

			if (backgroundLink != null) {
				currentPagingIndex = currentPagingIndex
						- (currentPagingIndex / 5);
			}

			mIsThrottling = true;

			// Querying like a bous!
			mDataManager
					.getSearchKeyboard(mSearchResponse.getQuery(),
							mSearchResponse.getType(),
							Integer.toString(currentPagingIndex + 1),
							Integer.toString(RESULT_TO_PRESENT),
							LocalListFragment.this);
		}

		private int fixedImageHeight = 0;

		private class AdLoader {
			private String backgroundLink = null;
			private BitmapDrawable backgroundImage = null;

			public AdLoader(final ImageView tileImage, final int location,
					final Placement placement) {
				setAdBitmap(tileImage, location, placement);
			}

			private void setAdBitmap(final ImageView tileImage,
					final int location, final Placement placement) {
				try {
					// if (mCampaignsManager == null) {
					// return;
					// }

					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								// tileImages.add(tileImage);
								// getPlacement();
								if (placement != null && mContext != null) {
									try {
										DisplayMetrics metrics = HomeActivity.metrics;
										width = metrics.widthPixels;
										backgroundLink = Utils
												.getDisplayProfile(metrics,
														placement);
									} catch (Exception e) {
									}
									if (backgroundLink != null) {
										if (mMemoryCache.get(backgroundLink) == null) {

											try {
												backgroundImage = Utils
														.getBitmap(
																getActivity(),
																width,
																backgroundLink);
											} catch (Exception e) {
											}
											// backgroundImage =
											// Utils.ResizeBitmap(dpi,
											// width, backgroundImage);
											backgroundImage = Utils
													.ResizeBitmap(
															getActivity(),
															HomeActivity.metrics,
															backgroundImage);
										} else {
											backgroundImage = mMemoryCache
													.get(backgroundLink);
										}
										// try {
										// Thread.sleep(2000);
										// } catch (InterruptedException e) {
										// e.printStackTrace();
										// }
										h.sendEmptyMessage(0);
									}
								}
							} catch (Exception e) {
								Logger.printStackTrace(e);
							}
						}
					}).start();
					h = new Handler() {
						@SuppressWarnings("deprecation")
						public void handleMessage(android.os.Message msg) {
							// if (msg.what < tileImages.size()) {
							if (backgroundImage != null) {
								// main = (ProgressBar) ((View) tileImages.get(
								// msg.what).getParent())
								// .findViewById(R.id.pbMain);
								// main.setVisibility(View.GONE);
								// tileImages.get(msg.what).setBackgroundDrawable(
								// backgroundImage);
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
									tileImage.setBackground(backgroundImage);
								} else {
									tileImage
											.setBackgroundDrawable(backgroundImage);
								}

								// tileImage.setImageDrawable(
								// backgroundImage);
								mMemoryCache.put(backgroundLink,
										backgroundImage);
								// RelativeLayout rl = (RelativeLayout)
								// tileImage.getParent();
								// rl.getLayoutParams().height =
								// LayoutParams.WRAP_CONTENT;
								tileImage.postDelayed(new Runnable() {
									public void run() {
										Logger.i(
												"Size!",
												"size 111 : "
														+ backgroundImage
																.getIntrinsicWidth()
														+ " ::::::::: "
														+ backgroundImage
																.getIntrinsicHeight());
										// tileImage.getLayoutParams().width =
										// backgroundImage.getIntrinsicWidth();
										// tileImage.getLayoutParams().height =
										// backgroundImage.getIntrinsicHeight();
										tileImage.getLayoutParams().width = width;
										fixedImageHeight = (int) (((float) width * (float) backgroundImage
												.getIntrinsicHeight()) / (float) backgroundImage
												.getIntrinsicWidth());
										tileImage.getLayoutParams().height = fixedImageHeight;
										tileImage.invalidate();
										if(((RelativeLayout) tileImage.getParent())!=null)
											((RelativeLayout) tileImage.getParent())
													.forceLayout();
										((View) tileImage).setTag(
												R.string.key_placement,
												placement);
										// if (adapter != null) {
										// LocalListFragment discovery =
										// (LocalListFragment) adapter
										// .getCurrentFragment(mDeafultOpenedTab);
										// discovery.mSearchResultsAdapter
										// .notifyDataSetChanged();
										// }

									}
								}, 200);
								// Utils.postViewEvent(getActivity(),
								// placement);
								// if (!viewedPositions.contains(location + ":"
								// + placement.getCampaignID())) {
								// Logger.e(TAG, "Post ad view>>" + location);
								// Utils.postViewEvent(mContext, placement);
								// viewedPositions.add(location + ":"
								// + placement.getCampaignID());
								// }
								// if ( && !viewedPositions.contains(location
								// + ":"
								// + placement.getCampaignID())) {
								// Utils.postViewEvent(mContext, placement);
								// viewedPositions.add(location + ":"
								// + placement.getCampaignID());
								// }
							}
							// }
						}
					};
				} catch (Exception e) {
				}
			}
		}

		private void postAdForPosition() {
			try {
				if (/* placement != null && */mMediaItems != null
						&& mMediaItems.size() > 0) {
					LinearLayoutManager layoutManager = (LinearLayoutManager) mListResults
							.getLayoutManager();

					int firstVisibleItem = layoutManager
							.findFirstVisibleItemPosition();

					int visibleItems = layoutManager
							.findLastVisibleItemPosition();
					for (int position = firstVisibleItem; position <= visibleItems; position++) {
						int childPosition = position - firstVisibleItem;
						Checkforpost(position, childPosition);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void Checkforpost(int position, int childPosition) {
			try {
				View v = mListResults.getChildAt(childPosition);
				if (v != null) {
					// MediaItem mediaItem = (MediaItem) v
					// .getTag(R.id.view_tag_object);
					// MediaType mediaType = mediaItem.getMediaType();
					// MediaContentType mediaContentType = mediaItem
					// .getMediaContentType();
					View iv = null;
					// if (mediaContentType == MediaContentType.RADIO
					// || mediaContentType == MediaContentType.MUSIC
					// || mediaType == MediaType.ALBUM
					// || mediaType == MediaType.PLAYLIST
					// || (mediaType == MediaType.TRACK && mediaContentType !=
					// MediaContentType.VIDEO)) {
					iv = v.findViewById(R.id.iv_media_search_result_advertisement);
					// } else {
					// iv = v.findViewById(R.id.home_videos_tile_image);
					// }
					if (iv != null) {
						// Placement placement = (Placement) ((View) iv
						// .getParent()).getTag(R.string.key_placement);
						Placement placement = (Placement) iv
								.getTag(R.string.key_placement);
						if (placement != null) {
							// System.out.println("CampaignPlayEvent.py request ::: 21 "
							// +(position + ":"
							// + placement.getCampaignID()));
							if (!viewedPositions.contains(position + ":"
									+ placement.getCampaignID())) {
								Logger.e(TAG, "Post ad view>>" + position);
								Utils.postViewEvent(mContext, placement);
								viewedPositions.add(position + ":"
										+ placement.getCampaignID());
							}
						}
					}
				}
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}

		}

		private class SearchResultsAdapter extends
				RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> implements
				OnClickListener, OnSearchResultListener {
			Placement tempPlacement;
			QuickActionSearchResult quickAction;

			// inner class to hold a reference to each item of RecyclerView
			public class ViewHolder extends RecyclerView.ViewHolder {

				ImageView searchResultImage;
				TextView searchResultTopText;
				ImageView searchResultImageType,
						iv_media_search_result_advertisement;
				LanguageTextView searchResultTypeAndName;
				TextView searchResultTypeAndNameEnglish;
				RelativeLayout searchResultRow;
				ImageButton player_queue_line_button_more;
				ImageButton searchResultButtonPlay;
				LinearLayout ll_content, ll_right_buttons;
				public CustomCacheStateProgressBar progressCacheState;

				public ViewHolder(View itemLayoutView) {
					super(itemLayoutView);

					searchResultRow = (RelativeLayout) itemLayoutView
							.findViewById(R.id.linearlayout_search_result_line);

					player_queue_line_button_more = (ImageButton) itemLayoutView
							.findViewById(R.id.player_queue_line_button_more);

					searchResultButtonPlay = (ImageButton) itemLayoutView
							.findViewById(R.id.search_result_line_button_play);

					searchResultTopText = (TextView) itemLayoutView
							.findViewById(R.id.search_result_line_top_text);
					searchResultImageType = (ImageView) itemLayoutView
							.findViewById(R.id.search_result_media_image_type);
					searchResultTypeAndNameEnglish = (TextView) itemLayoutView
							.findViewById(R.id.search_result_text_media_type_and_name_english);
					searchResultTypeAndName = (LanguageTextView) itemLayoutView
							.findViewById(R.id.search_result_text_media_type_and_name);

					iv_media_search_result_advertisement = (ImageView) itemLayoutView
							.findViewById(R.id.iv_media_search_result_advertisement);

					searchResultImage = (ImageView) itemLayoutView
							.findViewById(R.id.search_result_media_image);

					ll_content = (LinearLayout) itemLayoutView
							.findViewById(R.id.ll_content);
					ll_right_buttons = (LinearLayout) itemLayoutView
							.findViewById(R.id.ll_right_buttons);

					progressCacheState = (CustomCacheStateProgressBar) itemLayoutView
							.findViewById(R.id.search_result_progress_cache_state);

					itemLayoutView.setTag(this);
				}
			}

			// Create new views (invoked by the layout manager)
			public SearchResultsAdapter.ViewHolder onCreateViewHolder(
					ViewGroup parent, int viewType) {
				// create a new view
				View itemLayoutView = LayoutInflater.from(parent.getContext())
						.inflate(R.layout.list_item_media_search_result_line,
								null);
				ViewHolder viewHolder = new ViewHolder(itemLayoutView);
				return viewHolder;
			}

			public SearchResultsAdapter() {
				tempPlacement = getPlacement();
				text_save_offline = mContext
						.getResources()
						.getString(
								R.string.media_details_custom_dialog_long_click_general_save_offline);
			}

			@Override
			public int getItemCount() {
				if (tempPlacement != null)
					return (Utils.isListEmpty(mMediaItems) ? 0 : (mMediaItems
							.size() + (mMediaItems.size() / 4)));
				else
					return (Utils.isListEmpty(mMediaItems) ? 0 : mMediaItems
							.size());
			}

			// @Override
			// public Object getItem(int position) {
			// if (tempPlacement != null)
			// if ((position + 1) % 5 == 0)
			// return null;
			// else
			// return mMediaItems.get(position - (position / 4));
			// return mMediaItems.get(position);
			// }

			@Override
			public long getItemId(int position) {
				return position;
			}

			// Replace the contents of a view (invoked by the layout manager)
			@Override
			public void onBindViewHolder(ViewHolder viewHolder, int position) {
				try {
					getView(position, viewHolder);
				} catch (Exception e){
					Logger.printStackTrace(e);
				}
			}

			private Placement getPlacement() {
				Placement placement = null;
				try {
					CampaignsManager mCampaignsManager = CampaignsManager
							.getInstance(getActivity());
					placement = mCampaignsManager
							.getPlacementOfType(PlacementType.SEARCH_RESULTS);
					if (placement != null) {
						Logger.i("Placement", "Main search result :: "
								+ new Gson().toJson(placement).toString());
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
				return placement;
			}

			public void getView(int position, ViewHolder viewHolder) {

				Placement placement = null;
				if (((position + 1) % 5 == 0/* && placement != null */)) {
					int location = position + 1;
					placement = mPlacementMap.get(location);
					if (placement == null) {
						// isFromMap = false;
						placement = getPlacement();
					}

					if (location % 5 == 0 && placement != null) {
						mPlacementMap.put(location, placement);

						viewHolder.iv_media_search_result_advertisement
								.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										Placement placement = (Placement) v
												.getTag(R.string.key_placement);
										if (placement != null) {
											try {
												Utils.performclickEvent(
														getActivity(),
														placement);
												// Intent browserIntent = new
												// Intent(Intent.ACTION_VIEW,
												// Uri.parse(placement.getActions().get(0).action));
												// startActivity(browserIntent);
											} catch (Exception e) {
												Logger.printStackTrace(e);
											}
										}

									}
								});

						// convertView.setTag(R.string.key_placement,
						// placement);
						viewHolder.iv_media_search_result_advertisement.setTag(
								R.string.key_placement, null);

						viewHolder.iv_media_search_result_advertisement
								.setVisibility(View.VISIBLE);
						viewHolder.ll_right_buttons.setVisibility(View.GONE);
						viewHolder.ll_content.setVisibility(View.GONE);
						viewHolder.searchResultImage.setVisibility(View.GONE);
						viewHolder.progressCacheState.setVisibility(View.GONE);

						if (fixedImageHeight != 0) {
							viewHolder.iv_media_search_result_advertisement
									.getLayoutParams().height = fixedImageHeight;
						} else {
							viewHolder.iv_media_search_result_advertisement
									.getLayoutParams().height = (int) mContext
									.getResources().getDimension(
											R.dimen.radio_list_item_height);
						}

						new AdLoader(
								viewHolder.iv_media_search_result_advertisement,
								location, placement);

						// convertView.setBackgroundResource(R.drawable.background_actionbar);
						return;
					}

					// convertView = mInflater.inflate(
					// R.layout.list_item_media_search_result_line,
					// parent, false);

					// viewHolder.searchResultButtonPlay
					// .setOnLongClickListener(this);

					// viewHolder.searchResultImage = (ImageView)
					// convertView.findViewById(R.id.search_result_media_image);

					// convertView.setTag(R.id.view_tag_view_holder,
					// viewHolder);
				} else {
					// viewHolder = (ViewHolder) convertView
					// .getTag(R.id.view_tag_view_holder);
				}

                viewHolder.iv_media_search_result_advertisement.setTag(
                        R.string.key_placement, null);

				viewHolder.searchResultRow.setOnClickListener(this);
				// viewHolder.searchResultRow.setOnLongClickListener(this);

				viewHolder.searchResultButtonPlay.setOnClickListener(this);
				viewHolder.searchResultButtonPlay.setVisibility(View.GONE);
				viewHolder.player_queue_line_button_more
						.setOnClickListener(this);

				viewHolder.iv_media_search_result_advertisement
						.setVisibility(View.GONE);
				viewHolder.ll_right_buttons.setVisibility(View.VISIBLE);
				viewHolder.ll_content.setVisibility(View.VISIBLE);
				viewHolder.searchResultImage.setVisibility(View.VISIBLE);
				viewHolder.progressCacheState.setVisibility(View.VISIBLE);

				// populate the view from the keywords's list.
				if (tempPlacement != null) {
					position = position - (position / 5);
				}
				MediaItem mediaItem = (MediaItem) mMediaItems.get(position);// getItem(position);
				// if(mediaItem.getTitle().equals("no")){
				// convertView =
				// mInflater.inflate(R.layout.list_item_media_search_result_advertisement,
				// parent, false);
				// convertView.setBackgroundDrawable(backgroundImage);
				// convertView.setOnClickListener(new OnClickListener() {
				//
				// @Override
				// public void onClick(View v) {
				// if(placement != null){
				// Intent browserIntent = new Intent(Intent.ACTION_VIEW,
				// Uri.parse(placement.getActions().get(0).action));
				// startActivity(browserIntent);
				// }
				//
				// }
				// });
				// // convertView.setTag(R.id.view_tag_ad, backgroundImage);
				// return convertView;
				// }

				// stores the object in the view.
				viewHolder.searchResultRow.setTag(R.id.view_tag_object,
						mediaItem);
				try {
					viewHolder.searchResultRow.setTag(R.id.view_tag_position,
							position);
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}

				// // gets the image and its size.

				//
				// // Picasso
				// Picasso.with(mContext).cancelRequest(viewHolder.searchResultImage);
				// if (mContext != null &&
				// !TextUtils.isEmpty(mediaItem.getImageUrl()))
				// {
				// // mImageFetcher.loadImage(mediaItem.getImageUrl(),
				// viewHolder.searchResultImage);
				//
				// mImageLoader.displayImage(mediaItem.getImageUrl(),
				// viewHolder.searchResultImage, mDisplayImageOptions);
				// if (mediaItem.getMediaType() != MediaType.PLAYLIST) {
				// Picasso.with(mContext)
				// .load(mediaItem.getImageUrl())
				// .placeholder(R.drawable.background_home_tile_album_default)
				// .into(viewHolder.searchResultImage);
				// } else {
				// Picasso.with(mContext).cancelRequest(viewHolder.searchResultImage);
				// }
				// } else {
				// Picasso.with(mContext).cancelRequest(viewHolder.searchResultImage);
				// if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				// viewHolder.searchResultImage.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_media_details_playlist_inside_thumb));
				// } else {
				// viewHolder.searchResultImage.setImageResource(R.drawable.background_media_details_playlist_inside_thumb);
				// }
				// }

				// Set title
				viewHolder.searchResultTopText.setText(mediaItem.getTitle());
				CacheState cacheState = null;
				// Set Image Type and Text Below title By Type
				if (mediaItem.getMediaType() == MediaType.TRACK) {
					try {
						cacheState = DBOHandler.getTrackCacheState(
								getActivity().getApplicationContext(), ""
										+ mediaItem.getId());

						viewHolder.searchResultImageType
								.setImageResource(R.drawable.icon_main_settings_music);

						viewHolder.searchResultTypeAndName.setText(newInstance.trackPrefix
								+ " - " + mediaItem.getAlbumName());
						viewHolder.searchResultTypeAndNameEnglish.setText(newInstance.trackPrefix
								+ " - " + mediaItem.getAlbumName());
						setNotPlaylistResultImage(viewHolder, mediaItem);
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
				} else if (mediaItem.getMediaType() == MediaType.ALBUM) {
					try {
						cacheState = DBOHandler.getAlbumCacheState(
								getActivity().getApplicationContext(), ""
										+ mediaItem.getId());
						viewHolder.searchResultImageType
								.setImageResource(R.drawable.icon_main_search_album);
						viewHolder.searchResultTypeAndName.setText(newInstance.albumPrefix
								+ " - "
								+ String.valueOf(mediaItem.getMusicTrackCount()
								+ " " + newInstance.playlistAlbumSuffix));
						viewHolder.searchResultTypeAndNameEnglish.setText(newInstance.albumPrefix
								+ " - "
								+ String.valueOf(mediaItem.getMusicTrackCount()
								+ " " + newInstance.playlistAlbumSuffix));
						setNotPlaylistResultImage(viewHolder, mediaItem);
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
				} else if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
					try {
						cacheState = DBOHandler.getPlaylistCacheState(
								getActivity().getApplicationContext(), ""
										+ mediaItem.getId());
						viewHolder.searchResultImageType
								.setImageResource(R.drawable.icon_home_music_tile_playlist);
						viewHolder.searchResultTypeAndName
								.setText(newInstance.playlistPrefix
										+ " - "
										+ String.valueOf(mediaItem
										.getMusicTrackCount()
										+ " "
										+ newInstance.playlistAlbumSuffix));
						viewHolder.searchResultTypeAndNameEnglish
								.setText(newInstance.playlistPrefix
										+ " - "
										+ String.valueOf(mediaItem
										.getMusicTrackCount()
										+ " "
										+ newInstance.playlistAlbumSuffix));
						setPlaylistResultImage(viewHolder);
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
				} else if (mediaItem.getMediaType() == MediaType.ARTIST) {
					try {
						viewHolder.searchResultImageType
								.setImageResource(R.drawable.icon_main_settings_live_radio);
						viewHolder.searchResultTypeAndName
								.setText(newInstance.artistPrefix);
						viewHolder.searchResultTypeAndNameEnglish
								.setText(newInstance.artistPrefix);
						setNotPlaylistResultImage(viewHolder, mediaItem);
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
				} else if (mediaItem.getMediaType() == MediaType.VIDEO) {
					try {
						cacheState = DBOHandler.getVideoTrackCacheState(
								getActivity().getApplicationContext(), ""
										+ mediaItem.getId());
						viewHolder.searchResultImageType
								.setImageResource(R.drawable.icon_main_settings_videos);
						viewHolder.searchResultTypeAndName.setText(newInstance.videoPrefix
								+ " - "
								+ String.valueOf(mediaItem.getAlbumName()));
						viewHolder.searchResultTypeAndNameEnglish.setText(newInstance.videoPrefix
								+ " - "
								+ String.valueOf(mediaItem.getAlbumName()));
						setNotPlaylistResultImage(viewHolder, mediaItem);
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
				}
				// if(cacheState==CacheState.CACHED){
				// viewHolder.buttonSaveOffline.setImageResource(R.drawable.icon_media_details_saved);
				// } else if(cacheState==CacheState.CACHING ||
				// cacheState==CacheState.QUEUED){
				// viewHolder.buttonSaveOffline.setImageResource(R.drawable.icon_media_details_saving);
				// } else{
				// viewHolder.buttonSaveOffline.setImageResource(R.drawable.icon_media_details_saveoffline);
				// }
				// if(CacheManager.isProUser(mContext)){
				if(mDataManager.getApplicationConfigurations().isLanguageSupportedForWidget()){
					viewHolder.searchResultTypeAndNameEnglish.setVisibility(View.VISIBLE);
					viewHolder.searchResultTypeAndName.setVisibility(View.GONE);
				} else{
					viewHolder.searchResultTypeAndNameEnglish.setVisibility(View.GONE);
					viewHolder.searchResultTypeAndName.setVisibility(View.VISIBLE);
				}
				viewHolder.progressCacheState.setVisibility(View.GONE);
				// if (cacheState != null) {
				//
				// // viewHolder.progressCacheState.showProgressOnly(true);
				// // viewHolder.progressCacheState.setCacheState(cacheState);
				// }
				return;
			}

			CacheState getCacheState(MediaItem mediaItem) {
				CacheState cacheState = null;
				// Set Image Type and Text Below title By Type
				if (mediaItem.getMediaType() == MediaType.TRACK) {
					cacheState = DBOHandler.getTrackCacheState(getActivity()
							.getApplicationContext(), "" + mediaItem.getId());

				} else if (mediaItem.getMediaType() == MediaType.ALBUM) {

					cacheState = DBOHandler.getAlbumCacheState(getActivity()
							.getApplicationContext(), "" + mediaItem.getId());

				} else if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
					cacheState = DBOHandler.getPlaylistCacheState(getActivity()
							.getApplicationContext(), "" + mediaItem.getId());
				} else if (mediaItem.getMediaType() == MediaType.VIDEO) {
					cacheState = DBOHandler.getVideoTrackCacheState(
							getActivity().getApplicationContext(), ""
									+ mediaItem.getId());
				}
				return cacheState;

			}

			// @Override
			// public int getItemViewType(int position) {
			// try {
			// MediaItem mediaItem = (MediaItem) getItem(position);
			// if (mediaItem != null
			// && mediaItem.getMediaType() != MediaType.PLAYLIST) {
			// return IGNORE_ITEM_VIEW_TYPE;
			// }
			// } catch (Exception e) {
			// Logger.printStackTrace(e);
			// }
			// return super.getItemViewType(position);
			// }

			public void setPlaylistResultImage(ViewHolder viewHolder) {
				try {
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
						viewHolder.searchResultImage
								.setBackgroundDrawable(getResources()
										.getDrawable(
												R.drawable.background_media_details_playlist_inside_thumb));
					} else {
						viewHolder.searchResultImage
								.setImageResource(R.drawable.background_media_details_playlist_inside_thumb);
					}
				} catch (Error e) {
					System.gc();
					System.runFinalization();
					System.gc();
				}
			}

			public void setNotPlaylistResultImage(ViewHolder viewHolder,
					MediaItem mediaItem) {
				try {
					String imageURL = "";
					String[] images = ImagesManager.getImagesUrlArray(
							mediaItem.getImagesUrlArray(),
							ImagesManager.HOME_MUSIC_TILE,
							mDataManager.getDisplayDensity());
					if (images != null && images.length > 0) {
						imageURL = images[0];
					}

					if (imageURL != null && imageURL.length() > 0) {
						PicassoUtil.with(getActivity()).load(null, imageURL,
								viewHolder.searchResultImage,
								R.drawable.background_home_tile_album_default);
					} else {
						PicassoUtil.with(getActivity()).load(null, null,
								viewHolder.searchResultImage,
								R.drawable.background_home_tile_album_default);
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
					PicassoUtil.with(getActivity()).load(null, null,
							viewHolder.searchResultImage,
							R.drawable.background_home_tile_album_default);
				} catch (Error e) {
					System.gc();
					System.runFinalization();
					System.gc();
				}
			}

			private String text_save_offline = "";
			private int saveoffline_drawable = R.drawable.icon_media_details_saving;

			@Override
			public void onClick(final View view) {
				Set<String> tags = Utils.getTags();
				if (!tags.contains("search_used")) {
					tags.add("search_used");
					Utils.AddTag(tags);
				}

				final int viewId = view.getId();

				if (viewId == R.id.linearlayout_search_result_line) {
					// gets the media item from the row.

					MediaItem mediaItem = (MediaItem) view
							.getTag(R.id.view_tag_object);
					if (mediaItem != null
							&& !TextUtils.isEmpty(mediaItem.getTitle())
							&& mediaItem.getTitle().equals("no")) {
						try {
							// Utils.performclickEvent(mContext, placement);
							// Intent browserIntent = new
							// Intent(Intent.ACTION_VIEW,
							// Uri.parse(placement.getActions().get(0).action));
							// startActivity(browserIntent);
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
						return;
					}

					if (newInstance.mOnSearchResultsOptionSelectedListener != null) {
						if (mediaItem.getMediaType() == MediaType.TRACK) {
							newInstance.mOnSearchResultsOptionSelectedListener
									.onPlayNowSelected(mediaItem);
						} else {
							if (PlayerService.service != null
									&& PlayerService.service.isPlaying()) {
								newInstance.mOnSearchResultsOptionSelectedListener
										.onShowDetails(mediaItem, false);
							} else {

								if (mediaItem.getMediaType() == MediaType.VIDEO) {
									// mOnSearchResultsOptionSelectedListener.onShowDetails(
									// mediaItem, false);
									newInstance.mOnSearchResultsOptionSelectedListener
											.onShowDetails(mediaItem,
													mMediaItems, false);
								} else {
									newInstance.mOnSearchResultsOptionSelectedListener
											.onShowDetails(mediaItem, false);
									newInstance.mOnSearchResultsOptionSelectedListener
											.onPlayNowSelected(mediaItem);
								}
							}
						}

					}

					if (mediaItem.getMediaType() == MediaType.VIDEO) {
						Map<String, String> reportMap = new HashMap<String, String>();

						reportMap.put(
								FlurryConstants.FlurryKeys.Title.toString(),
								mediaItem.getTitle());
						reportMap
								.put(FlurryConstants.FlurryKeys.SubSection
										.toString(),
										FlurryConstants.FlurrySubSectionDescription.SearchResults
												.toString());

						Analytics.logEvent(
								FlurryConstants.FlurryEventName.VideoSelected
										.toString(), reportMap);
					}

					// Flurry report: Search result tapped
					if (mSearchResponse != null
							&& mSearchResponse.getQuery().toString() != null
							&& !mSearchResponse.getQuery().toString().trim()
									.equals("")) {
						Map<String, String> reportMap = new HashMap<String, String>();
						reportMap.put(FlurryConstants.FlurrySearch.SearchTerm
								.toString(), mSearchResponse.getQuery());// xtpl
						reportMap
								.put(FlurryConstants.FlurrySearch.TitleOfResultTapped
										.toString(), mediaItem.getTitle());
						reportMap.put(
								FlurryConstants.FlurrySearch.TypeOfResultTaped
										.toString(), mediaItem.getMediaType()
										.toString());
						Analytics.logEvent(
								FlurryConstants.FlurrySearch.SearchResultTapped
										.toString(), reportMap);
					}

					Map<String, String> reportMap = new HashMap<String, String>();
					if (mediaItem.getMediaContentType() == MediaContentType.VIDEO)
						reportMap.put(
								FlurryConstants.FlurryKeys.Type.toString(),
								FlurryConstants.FlurryKeys.Video.toString());
					else if (mediaItem.getMediaType() == MediaType.ALBUM)
						reportMap.put(
								FlurryConstants.FlurryKeys.Type.toString(),
								FlurryConstants.FlurryKeys.Album.toString());
					else if (mediaItem.getMediaType() == MediaType.PLAYLIST)
						reportMap.put(
								FlurryConstants.FlurryKeys.Type.toString(),
								FlurryConstants.FlurryKeys.Playlist.toString());
					else if (mediaItem.getMediaType() == MediaType.TRACK)
						reportMap.put(
								FlurryConstants.FlurryKeys.Type.toString(),
								FlurryConstants.FlurryKeys.Song.toString());
					reportMap.put(
							FlurryConstants.FlurryKeys.Section.toString(),
							FlurryConstants.FlurrySearch.Search.toString());
					Analytics.logEvent(
							FlurryConstants.FlurryEventName.TileClicked
									.toString(), reportMap);
				} else if (viewId == R.id.search_result_line_button_play) {
					// gets the media item from the row.
					View parent_inner = (View) view.getParent();
					View parent = (View) parent_inner.getParent();

					MediaItem mediaItem = (MediaItem) parent
							.getTag(R.id.view_tag_object);
					play(mediaItem);

				} else if (viewId == R.id.player_queue_line_button_more) {
					View parent_inner = (View) view.getParent();
					View parent = (View) parent_inner.getParent();
					MediaItem mediaItem = (MediaItem) parent
							.getTag(R.id.view_tag_object);
					int position = (Integer) parent
							.getTag(R.id.view_tag_position);
					// show tile's option was selected.

					CacheState cacheState = getCacheState(mediaItem);
					if (cacheState == CacheState.CACHED) {
						text_save_offline = getResources().getString(
								R.string.caching_text_play_offline);
						saveoffline_drawable = R.drawable.icon_media_details_saved;
						Logger.e("text_save_offline", text_save_offline);
						// ((ImageView)
						// dialog.findViewById(R.id.long_click_custom_dialog_save_offline_image)).setImageResource(R.drawable.icon_media_details_saved);
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

					}

					if (mediaItem.getMediaType() == MediaType.VIDEO) {
						quickAction = new QuickActionSearchResult(
								getActivity(), text_save_offline,
								saveoffline_drawable, true, position,
								SearchResultsAdapter.this,
								mediaItem.getMediaType(), true);
					} else {
						quickAction = new QuickActionSearchResult(
								getActivity(), text_save_offline,
								saveoffline_drawable, false, position,
								SearchResultsAdapter.this,
								mediaItem.getMediaType(), true);
						quickAction.setMediaItem(mediaItem);
					}
					quickAction.show(view);
					view.setEnabled(false);
					quickAction
							.setOnDismissListener(new QuickActionSearchResult.OnDismissListener() {
								@Override
								public void onDismiss() {
									view.setEnabled(true);
								}
							});
				}

			}

			private void play(MediaItem mediaItem) {
				if (newInstance.mOnSearchResultsOptionSelectedListener != null) {
					if (mediaItem.getMediaType() == MediaType.VIDEO) {
						// mOnSearchResultsOptionSelectedListener.onShowDetails(
						// mediaItem, false);
						newInstance.mOnSearchResultsOptionSelectedListener.onShowDetails(
								mediaItem, mMediaItems, false);
					} else
						newInstance.mOnSearchResultsOptionSelectedListener
								.onPlayNowSelected(mediaItem);

					if (mediaItem.getMediaType() == MediaType.VIDEO) {

						Map<String, String> reportMap = new HashMap<String, String>();

						reportMap.put(
								FlurryConstants.FlurryKeys.Title.toString(),
								mediaItem.getTitle());
						reportMap
								.put(FlurryConstants.FlurryKeys.SubSection
										.toString(),
										FlurryConstants.FlurrySubSectionDescription.SearchResults
												.toString());

						Analytics.logEvent(
								FlurryConstants.FlurryEventName.VideoSelected
										.toString(), reportMap);

					} else {
						Map<String, String> reportMap = new HashMap<String, String>();

						reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong
								.toString(), mediaItem.getTitle());
						reportMap.put(mediaItem.getMediaType().toString(),
								Utils.toWhomSongBelongto(mediaItem));
						reportMap
								.put(FlurryConstants.FlurryKeys.Source
										.toString(),
										FlurryConstants.FlurrySourceDescription.TapOnPlaySearchResult
												.toString());// xtpl
						reportMap
								.put(FlurryConstants.FlurryKeys.SubSection
										.toString(),
										FlurryConstants.FlurrySubSectionDescription.SearchResults
												.toString());
						Analytics
								.logEvent(
										FlurryConstants.FlurryEventName.SongSelectedForPlay
												.toString(), reportMap);
					}
				}
			}

			public boolean onLongClick(View view) {
				final int viewId = view.getId();
				if (viewId == R.id.linearlayout_search_result_line) {
					// gets the media item from the row.
					MediaItem mediaItem = (MediaItem) view
							.getTag(R.id.view_tag_object);

					// radio items does not support long clicks.
					if (mediaItem.getMediaContentType() != MediaContentType.RADIO) {
						showMediaItemOptionsDialog(mediaItem);
					}

					return true;

				} else if (viewId == R.id.search_result_line_button_play) {
					// gets the media item from the row.
					View parent = (View) view.getParent();
					MediaItem mediaItem = (MediaItem) parent
							.getTag(R.id.view_tag_object);

					// radio items does not support long clicks.
					if (mediaItem.getMediaContentType() != MediaContentType.RADIO) {
						showMediaItemOptionsDialog(mediaItem);
					}

					return true;
				}

				return false;
			}

			private void showMediaItemOptionsDialog(final MediaItem mediaItem) {
				// set up custom dialog
				final Dialog mediaItemOptionsDialog = new Dialog(getActivity());
				mediaItemOptionsDialog
						.requestWindowFeature(Window.FEATURE_NO_TITLE);
				mediaItemOptionsDialog
						.setContentView(R.layout.dialog_media_playing_options);
				mediaItemOptionsDialog.setCancelable(true);
				mediaItemOptionsDialog.show();

				// sets the title.
				TextView title = (TextView) mediaItemOptionsDialog
						.findViewById(R.id.long_click_custom_dialog_title_text);
				title.setText(mediaItem.getTitle());

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

				if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
					llPlayNow.setVisibility(View.GONE);
					llAddtoQueue.setVisibility(View.GONE);
				}

				llSaveOffline.setVisibility(View.VISIBLE);
				CustomCacheStateProgressBar progressCacheState = (CustomCacheStateProgressBar) mediaItemOptionsDialog
						.findViewById(R.id.long_click_custom_dialog_save_offline_progress_cache_state);
				progressCacheState.setNotCachedStateVisibility(true);
				progressCacheState.setTag(R.id.view_tag_object, mediaItem);

				CacheState cacheState = getCacheState(mediaItem);
				if (cacheState == CacheState.CACHED) {
					llSaveOffline.setTag(true);
					((TextView) mediaItemOptionsDialog
							.findViewById(R.id.long_click_custom_dialog_save_offline_text))
							.setText(getResources().getString(
									R.string.caching_text_play_offline));
					// ((ImageView)
					// dialog.findViewById(R.id.long_click_custom_dialog_save_offline_image)).setImageResource(R.drawable.icon_media_details_saved);
				} else if (cacheState == CacheState.CACHING
						|| cacheState == CacheState.QUEUED) {
					llSaveOffline.setTag(false);
					((TextView) mediaItemOptionsDialog
							.findViewById(R.id.long_click_custom_dialog_save_offline_text))
							.setText(getResources().getString(
									R.string.caching_text_saving));

				}

				if (cacheState != null)
					progressCacheState.setCacheState(cacheState);

				// play now.
				llPlayNow.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {

						// download
						MediaItem trackMediaItem = new MediaItem(mediaItem
								.getId(), mediaItem.getTitle(), mediaItem
								.getAlbumName(), mediaItem.getArtistName(),
								mediaItem.getImageUrl(), mediaItem
										.getBigImageUrl(), MediaType.TRACK
										.toString(), 0, mediaItem.getAlbumId());
						Intent intent = new Intent(mContext,
								DownloadConnectingActivity.class);
						intent.putExtra(
								DownloadConnectingActivity.EXTRA_MEDIA_ITEM,
								(Serializable) trackMediaItem);
						getActivity().startActivity(intent);

						Map<String, String> reportMap = new HashMap<String, String>();
						reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong
								.toString(), mediaItem.getTitle());
						reportMap.put(FlurryConstants.FlurryKeys.SourceSection
								.toString(),
								FlurryConstants.FlurryKeys.SearchResult
										.toString());
						Analytics.logEvent(
								FlurryConstants.FlurryEventName.Download
										.toString(), reportMap);

						mediaItemOptionsDialog.dismiss();
					}
				});

				// add to queue.
				llAddtoQueue.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (newInstance.mOnSearchResultsOptionSelectedListener != null) {
							newInstance.mOnSearchResultsOptionSelectedListener
									.onAddToQueueSelected(mediaItem);

							Map<String, String> reportMap = new HashMap<String, String>();

							reportMap.put(
									FlurryConstants.FlurryKeys.TitleOfTheSong
											.toString(), mediaItem.getTitle());
							reportMap.put(mediaItem.getMediaType().toString(),
									Utils.toWhomSongBelongto(mediaItem));
							reportMap.put(
									FlurryConstants.FlurryKeys.Source
											.toString(),
									FlurryConstants.FlurrySourceDescription.TapOnAddToQueueInContextualMenu
											.toString());
							reportMap.put(
									FlurryConstants.FlurryKeys.SubSection
											.toString(),
									FlurryConstants.FlurrySubSectionDescription.SearchResults
											.toString());

							Analytics
									.logEvent(
											FlurryConstants.FlurryEventName.SongSelectedForPlay
													.toString(), reportMap);
						}

						mediaItemOptionsDialog.dismiss();
					}
				});

				// show details.
				llDetails.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						if (newInstance.mOnSearchResultsOptionSelectedListener != null) {
							newInstance.mOnSearchResultsOptionSelectedListener
									.onShowDetails(mediaItem, false);
						}

						mediaItemOptionsDialog.dismiss();
					}
				});

				llSaveOffline.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (newInstance.mOnSearchResultsOptionSelectedListener != null
								&& view.getTag() == null) {
							newInstance.mOnSearchResultsOptionSelectedListener
									.onSaveOffline(mediaItem);
							mediaItemOptionsDialog.dismiss();

						} else if (view.getTag() != null
								&& (Boolean) view.getTag()) {
							if (mediaItem.getMediaContentType() == MediaContentType.VIDEO
									|| mediaItem.getMediaType() == MediaType.TRACK) {
								Toast.makeText(getActivity(),
										R.string.already_offline_message_track,
										Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(
										getActivity(),
										R.string.already_offline_message_for_tracklist,
										Toast.LENGTH_SHORT).show();
							}
						}
					}
				});

			}

			@Override
			public void onItemSelected(String item) {
				// TODO Auto-generated method stub
				Map<String, String> reportMap1 = new HashMap<String, String>();
				reportMap1.put(
						FlurryConstants.FlurryKeys.SourceSection.toString(),
						FlurryConstants.FlurryKeys.SearchResult.toString());
				reportMap1.put(
						FlurryConstants.FlurryKeys.OptionSelected.toString(),
						item);
				Analytics.logEvent(
						FlurryConstants.FlurryEventName.ThreeDotsClicked
								.toString(), reportMap1);
			}

			@Override
			public void onItemSelectedPosition(int id, int pos, boolean isVideo,String item) {
				if(getActivity()!=null){
					String txtPlay= getActivity().getString(R.string.caching_text_play);
					String txtAddToQueue= getActivity().getString(R.string.media_details_custom_dialog_long_click_add_to_queue);
					String txtViewDetail= getActivity().getString(R.string.media_details_custom_dialog_long_click_view_details);
					String txtMp3 = getActivity().getString(R.string.general_download);
					String txtMp4 = getActivity().getString(R.string.general_download_mp4);

					MediaItem mediaItem = mMediaItems.get(pos);

					if(item.equals(txtPlay)) {
						play(mediaItem);
					}else if(item.equals(txtMp4) || item.equals(txtMp3)){
						Intent intent = new Intent(getActivity(),
								DownloadConnectingActivity.class);
						intent.putExtra(
								DownloadConnectingActivity.EXTRA_MEDIA_ITEM,
								(Serializable) mediaItem);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						getActivity().startActivity(intent);

						Map<String, String> reportMap = new HashMap<String, String>();
						reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong
								.toString(), mediaItem.getTitle());
						reportMap.put(FlurryConstants.FlurryKeys.SourceSection
										.toString(),
								FlurryConstants.FlurryKeys.SearchResult
										.toString());
						Analytics.logEvent(
								FlurryConstants.FlurryEventName.Download
										.toString(), reportMap);
					}else if(item.equals(txtAddToQueue)) {

						if (newInstance.mOnSearchResultsOptionSelectedListener != null) {
							newInstance.mOnSearchResultsOptionSelectedListener
									.onAddToQueueSelected(mediaItem);

							Map<String, String> reportMap = new HashMap<String, String>();

							reportMap.put(
									FlurryConstants.FlurryKeys.TitleOfTheSong
											.toString(), mediaItem.getTitle());
							reportMap.put(mediaItem.getMediaType().toString(),
									Utils.toWhomSongBelongto(mediaItem));
							reportMap
									.put(FlurryConstants.FlurryKeys.Source
													.toString(),
											FlurryConstants.FlurrySourceDescription.TapOnAddToQueueInContextualMenu
													.toString());
							reportMap
									.put(FlurryConstants.FlurryKeys.SubSection
													.toString(),
											FlurryConstants.FlurrySubSectionDescription.SearchResults
													.toString());

							Analytics
									.logEvent(
											FlurryConstants.FlurryEventName.SongSelectedForPlay
													.toString(), reportMap);
						}
					/*} else {
						// save offline
						if (mOnSearchResultsOptionSelectedListener != null) {
							mOnSearchResultsOptionSelectedListener
									.onAddToQueueSelected(mediaItem);

							Map<String, String> reportMap = new HashMap<String, String>();

							reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong
									.toString(), mediaItem.getTitle());
							reportMap.put(mediaItem.getMediaType().toString(),
									Utils.toWhomSongBelongto(mediaItem));
							reportMap
									.put(FlurryConstants.FlurryKeys.Source
													.toString(),
											FlurryConstants.FlurrySourceDescription.TapOnAddToQueueInContextualMenu
													.toString());
							reportMap
									.put(FlurryConstants.FlurryKeys.SubSection
													.toString(),
											FlurryConstants.FlurrySubSectionDescription.SearchResults
													.toString());

							Analytics
									.logEvent(
											FlurryConstants.FlurryEventName.SongSelectedForPlay
													.toString(), reportMap);
						}
					}*/
					}else if(item.equals(txtViewDetail)) {
						if (newInstance.mOnSearchResultsOptionSelectedListener != null) {
							newInstance.mOnSearchResultsOptionSelectedListener.onShowDetails(
									mediaItem, false);
						}
					}else {
						if (newInstance.mOnSearchResultsOptionSelectedListener != null) {
							newInstance.mOnSearchResultsOptionSelectedListener
									.onSaveOffline(mediaItem);
						}
					}
				}
			}

		}
	}

	// view pager

	SparseArray<Fragment> mp;
	// private PagerSlidingTabStrip tabs;
	private PagerSlidingTabStrip tabs;
	private ViewPager pager;
	private MyPagerAdapter adapter;

	// Playlist | Songs | Albums| Video
	// final int POS_RADIO = 0;
	private final static int POS_SONG = 1;
	private final static int POS_ALBUMS = 2;
	private final static int POS_PLAYLIST = 0;
	private final static int POS_VIDEO = 3;
	private int mDeafultOpenedTab = POS_SONG;

	// private int currentColor = 0xFF666666;

	private class MyPagerAdapter extends FragmentPagerAdapter {
		// Song ID, Music, Radio, Videos, Discover

		// private final String[] TITLES = {
		// /* getString(R.string.search_result_line_type_and_name_artist),*/
		// getString(R.string.search_results_layout_bottom_text_for_track),
		// getString(R.string.search_results_layout_bottom_text_for_album),
		// getString(R.string.search_results_layout_bottom_text_for_playlist),
		// getString(R.string.search_results_layout_bottom_text_for_video) };
		//
		private final String[] TITLES = {
				/* getString(R.string.search_result_line_type_and_name_artist), */
				getString(R.string.search_results_layout_bottom_text_for_playlist),
				getString(R.string.search_results_layout_bottom_text_for_track),
				getString(R.string.search_results_layout_bottom_text_for_album),
				getString(R.string.search_results_layout_bottom_text_for_video) };

		SparseArray<Fragment> map;

		public MyPagerAdapter(FragmentManager fm) {
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

		private Fragment getCurrentFragment(int pos) {
			Fragment current = map.get(pos, null);
			return current;
		}

		@Override
		public Fragment getItem(int position) {


            LocalListFragment localListFragment = new LocalListFragment();
			// return SuperAwesomeCardFragment.newInstance(position);
			Bundle arguments = new Bundle();

			arguments.putString(
					MainSearchResultsFragment.FRAGMENT_ARGUMENT_QUERY, query);

			String type = MainSearchFragment.SEARCH_FILTER_TYPE_SONGS;
			/*
			 * if (position == POS_RADIO) { type =
			 * MainSearchFragment.SEARCH_FILTER_TYPE_ARTISTS; } else
			 */if (position == POS_SONG) {
				type = MainSearchFragment.SEARCH_FILTER_TYPE_SONGS;
			} else if (position == POS_ALBUMS) {
				type = MainSearchFragment.SEARCH_FILTER_TYPE_ALBUMS;
			} else if (position == POS_PLAYLIST) {
				type = MainSearchFragment.SEARCH_FILTER_TYPE_PLAYLISTS;
			} else if (position == POS_VIDEO) {
				type = MainSearchFragment.SEARCH_FILTER_TYPE_VIDEOS;
			}
			arguments.putString(
					MainSearchResultsFragment.FRAGMENT_ARGUMENT_TYPE, type);

			localListFragment.setArguments(arguments);
			map.put(position, localListFragment);
			return localListFragment;
		}
	}

	private void setUpViewpager(final int tabId) {
		try {
			adapter = new MyPagerAdapter(getChildFragmentManager());
			pager.setAdapter(adapter);
//			final int pageMargin = (int) TypedValue.applyDimension(
//					TypedValue.COMPLEX_UNIT_DIP, 1, getResources()
//							.getDisplayMetrics());
//			pager.setPageMargin(pageMargin);

			// pager.setCurrentItem(tabId,true);
			tabs.setViewPager(pager);
			pager.setCurrentItem(tabId);
			// try {
			// LocalListFragment localListFragment = (LocalListFragment) adapter
			// .getCurrentFragment(tabId);
			// localListFragment.postAdForPosition();
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			// tabs.invalidate();

			tabs.setOnPageChangeListener(new OnPageChangeListener() {

				@Override
				public void onPageSelected(int arg0) {
					/*
					 * if (arg0 == POS_RADIO) { mDeafultOpenedTab = POS_RADIO; }
					 * else
					 */if (arg0 == POS_SONG) {
						mDeafultOpenedTab = POS_SONG;
					} else if (arg0 == POS_ALBUMS) {
						mDeafultOpenedTab = POS_ALBUMS;
					} else if (arg0 == POS_PLAYLIST) {
						mDeafultOpenedTab = POS_PLAYLIST;
					} else if (arg0 == POS_VIDEO) {
						mDeafultOpenedTab = POS_VIDEO;
					}

					try {
						LocalListFragment localListFragment = (LocalListFragment) adapter
								.getCurrentFragment(arg0);
						localListFragment.postAdForPosition();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {
				}

				@Override
				public void onPageScrollStateChanged(int arg0) {
				}
			});
			// changeColor(currentColor);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}
	// end

}
