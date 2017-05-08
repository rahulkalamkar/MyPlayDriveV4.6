package com.hungama.myplay.activity.ui.fragments;

import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.ThreadPoolManager;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.campaigns.PlacementType;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaItemPlaylist;
import com.hungama.myplay.activity.data.dao.hungama.MediaItemsResponse;
import com.hungama.myplay.activity.data.dao.hungama.MediaTrackDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.MessageFromResponse;
import com.hungama.myplay.activity.data.dao.hungama.PromoUnit;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.gcm.IntentReceiver;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.HashTagListOperation;
import com.hungama.myplay.activity.operations.hungama.HungamaOperation;
import com.hungama.myplay.activity.operations.hungama.MediaContentOperation;
import com.hungama.myplay.activity.operations.hungama.MediaContentOperationPaging;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.ui.AlertActivity;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.OnApplicationStartsActivity;
import com.hungama.myplay.activity.ui.adapters.ComboMediaItem;
import com.hungama.myplay.activity.ui.adapters.MyAdapter;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.hungama.myplay.activity.util.Utils;

import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the {@link MediaTileGridFragment} that gives the required
 * behavior for presenting media items in the grid.
 */
public class HomeMediaTileGridFragmentNew extends MainFragment implements
		CommunicationOperationListener, SwipeRefreshLayout.OnRefreshListener {

	private static final String TAG = "MediaTileGridFragment";
	public static final String FRAGMENT_ARGUMENT_MEDIA_CATEGORY_TYPE = "fragment_argument_media_category_type";
	public static final String FRAGMENT_ARGUMENT_MEDIA_CONTENT_TYPE = "fragment_argument_media_content_type";

	private LinearLayout progressBar;
	private int mTileSize = 0;
	private DataManager mDataManager;

	private OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener;

	private MediaCategoryType mediaCategoryType;
	public List<MediaItem> mediaItems_final;
	private MediaContentType mediaContentType;
	private MediaItemsResponse mediaItemsResponse;
	private int itemsInList = com.hungama.myplay.activity.util.Constants.LOADING_CHUNK_NUMBER;

	private String backgroundLink;
	private View rootView;
	private Placement placement;
	private CampaignsManager mCampaignsManager;
	private ProgressBar progressInit;

	// public boolean isMusicLoaded, isVideoLoaded;
	private SwipeRefreshLayout mListViewContainer;

	RecyclerView recyclerView;
	private static Map<Long, MediaTrackDetails> mapTracks;

	@SuppressWarnings("deprecation")
	private void onCreateSwipeToRefresh(SwipeRefreshLayout refreshLayout) {
		refreshLayout.setOnRefreshListener(this);

		refreshLayout.setColorScheme(android.R.color.holo_blue_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_green_light,
				android.R.color.holo_red_light);

		refreshLayout.setDistanceToTriggerSync(200);
	}

	@Override
	public void onRefresh() {
		Logger.s(mediaCategoryType + " ::::::::::::::;; onRefresh");
		isRefresh = true;
		loadMoreResults(1, Constants.LOADING_CHUNK_NUMBER, mediaCategoryType,
				HomeMediaTileGridFragmentNew.this);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mListViewContainer.setRefreshing(false);
			}
		}, 1000);
	}

	boolean isRefresh;

	// ======================================================
	// FRAGMENT'S LIFE CYCLE.
	// ======================================================

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logger.s(mediaCategoryType + " ::::::::::::::;; onCreate");
		mapTracks = new HashMap<>();
		mDataManager = DataManager.getInstance(getActivity());
		setRetainInstance(true);
		mApplicationConfigurations = ApplicationConfigurations
				.getInstance(getActivity());
		onCreate = true;
		Analytics.postCrashlitycsLog(getActivity(), HomeMediaTileGridFragmentNew.class.getName());
	}

	private boolean onCreate = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Logger.s(mediaCategoryType + " ::::::::::::::;; onCreateView");
		if (rootView == null) {
			try {
				rootView = inflater.inflate(
						R.layout.fragment_home_media_tiles_grid_new, container,
						false);
				ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
						.getInstance(getActivity());
				if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
					Utils.traverseChild(rootView, getActivity());
				}
			} catch (Error e) {
				System.gc();
				System.runFinalization();
				System.gc();
				ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
						.getInstance(getActivity());
				rootView = inflater.inflate(
						R.layout.fragment_home_media_tiles_grid_new, container,
						false);
				if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
					Utils.traverseChild(rootView, getActivity());
				}
			}
			Bundle data = getArguments();
			Logger.e("HomeMediaTileGridFragment", "onCreateView");
			mListViewContainer = (SwipeRefreshLayout) rootView
					.findViewById(R.id.swipeRefreshLayout_listView);
			onCreateSwipeToRefresh(mListViewContainer);
			setUpData(data);
		} else {
			Logger.e("HomeMediaTileGridFragment", "onCreateView else");
			ViewGroup parent = (ViewGroup) Utils.getParentViewCustom(rootView);
			parent.removeView(rootView);
		}

		return rootView;
	}

	/**
	 * Set required data for loading media tile like, category type, content
	 * type, placement, etc.
	 * 
	 * @param data
	 *            bundle of category type, content type, etc.
	 */
	public void setUpData(Bundle data) {
		Logger.s(mediaCategoryType + " ::::::::::::::;; setUpData");
		if (data != null
				&& data.containsKey(HomeMediaTileGridFragmentNew.FRAGMENT_ARGUMENT_MEDIA_CATEGORY_TYPE))
		{
			mCampaignsManager = CampaignsManager.getInstance(getActivity());
			mediaCategoryType = (MediaCategoryType) data
					.getSerializable(HomeMediaTileGridFragmentNew.FRAGMENT_ARGUMENT_MEDIA_CATEGORY_TYPE);
			if (((MediaContentType) data
					.getSerializable(FRAGMENT_ARGUMENT_MEDIA_CONTENT_TYPE)) != null) {
				mediaContentType = (MediaContentType) data
						.getSerializable(FRAGMENT_ARGUMENT_MEDIA_CONTENT_TYPE);
			}

			// initialize placement
			if (mediaContentType == MediaContentType.MUSIC) {
				if (mediaCategoryType.equals(MediaCategoryType.LATEST)) {
					placement = mCampaignsManager
							.getPlacementOfType(PlacementType.MUSIC_NEW);
				} else if (mediaCategoryType.equals(MediaCategoryType.POPULAR)) {
					placement = mCampaignsManager
							.getPlacementOfType(PlacementType.MUSIC_POPULAR);
				}
				Logger.s(" ::::::::::::::::::- - setUpData 1 " + (placement == null));
			}

			// calculate list item count
			if (placement != null)
			{

					itemsInList = getFilteredTileCount()
							+ getFilteredTileAdCount();
					DisplayMetrics metrics = HomeActivity.metrics;
					backgroundLink = Utils
							.getDisplayProfile(metrics, placement);

			} else
				itemsInList = getFilteredTileCount();

		}
		initializeTiles(rootView, data);
		progressInit = (ProgressBar) rootView
				.findViewById(R.id.progressBar_init);
		// progressInit.getIndeterminateDrawable().setColorFilter(
		// new LightingColorFilter(0xFF000000, 0xFFFFFF));
		// Color.GRAY, Mode.SRC_ATOP);
	}

	boolean loadingMore = false;
	int mediaItemsSize = 0;

	/**
	 * Initialize adapter and content type. Also, used for media detail
	 * deeplinking.
	 * 
	 * @param rootView
	 *            fragment root view
	 * @param data
	 *            bundled data passed in fragment
	 */
	@SuppressWarnings("deprecation")
	private void initializeTiles(View rootView, Bundle data) {
		Logger.s(mediaCategoryType + " ::::::::::::::;; initializeTiles");
		recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

		progressBar = (LinearLayout) rootView
				.findViewById(R.id.progressBarLayout);

		recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView view, int scrollState) {
				Logger.i("ItemsInList + tic", "onScrollStateChanged"
						+ scrollState);
				String picasso_tag = "";
				if (mediaCategoryType == null) {
					picasso_tag = PicassoUtil.PICASSO_TAG;
				} else if (mediaCategoryType.equals(MediaCategoryType.LATEST)) {
					picasso_tag = PicassoUtil.PICASSO_NEW_MUSIC_LIST_TAG;
				} else if (mediaCategoryType.equals(MediaCategoryType.POPULAR)) {
					picasso_tag = PicassoUtil.PICASSO_POP_MUSIC_LIST_TAG;
				}
				if (scrollState == RecyclerView.SCROLL_STATE_IDLE) {
					PicassoUtil.with(getActivity()).resumeTag(picasso_tag);
					processPaging(view);
					try {
						mAdapter.postAdForPosition();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					PicassoUtil.with(getActivity()).pauseTag(picasso_tag);
				}
			}

			void processPaging(RecyclerView recyclerView) {
				try {
					LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView
							.getLayoutManager();
					int visibleItemCount = recyclerView.getChildCount();
					int totalItemCount = layoutManager.getItemCount();
					int firstVisibleItem = layoutManager
							.findFirstVisibleItemPosition();

					checkMediaItems(firstVisibleItem, visibleItemCount);

					// Logger.i("ItemsInList + tic", String.valueOf(itemsInList)
					// + ", " + String.valueOf(totalItemCount));
					if (totalItemCount > 0) {
						Logger.i(TAG, "getFilteredTileCount():" + ""
								+ getFilteredTileCount());
						Logger.i(TAG, "itemsInList:" + "" + itemsInList);
						Logger.i(TAG, "totalItemCount:" + "" + totalItemCount);
						Logger.i(TAG, "mediaItemsSize:" + "" + mediaItemsSize);
						Logger.i(
								TAG,
								"mediaItemsResponse.getTotal():"
										+ ""
										+ (mediaItemsResponse != null ? mediaItemsResponse
												.getTotal() : ""));

						if (firstVisibleItem + visibleItemCount == totalItemCount
								/* && totalItemCount == itemsInList */
								&& mediaItemsSize <= mediaItemsResponse
										.getTotal()
						/* && (mediaItemsSize % getFilteredTileCount() == 0) */) {
							if (!loadingMore) {
								loadingMore = true;
								Logger.i(TAG, "Loading more results");
								if (getActivity() != null) {
									progressBar.setVisibility(View.VISIBLE);
									// int adCount = 0;
									if (placement != null) {
										itemsInList = totalItemCount
												+ getFilteredTileCount()
												+ getFilteredTileAdCount();
										// adCount = (totalItemCount /
										// (getFilteredTileCount() +
										// getFilteredTileAdCount())) *
										// getFilteredTileAdCount();
									} else {
										itemsInList = totalItemCount
												+ getFilteredTileCount();
									}

									Logger.i(TAG, "totalItemCount before= "
											+ totalItemCount);

									mediaItemsSize = getMediaItemCount();
									Logger.i(TAG, "mediaItemsSize before= "
											+ mediaItemsSize);

									Logger.i(TAG, "mediaItemsSize final= "
											+ mediaItemsSize);
									if (mediaItemsSize >= mediaItemsResponse
											.getTotal()) {
										loadingMore = false;
										progressBar.setVisibility(View.GONE);
										return;
									}

									int start = 1 + mediaItemsSize /*- adCount*/;
									Logger.i(TAG, "totalItemCount = "
											+ totalItemCount);
									// Logger.i(TAG, "adCount = " + adCount);
									Logger.i(TAG, "Start = " + start);
									loadMoreResults(start,
											Constants.LOADING_CHUNK_NUMBER,
											mediaCategoryType,
											HomeMediaTileGridFragmentNew.this);
								}
							}
						}
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
			}
		});

		// sets the gird's properties.
		int imageTileSpacingVertical = getResources().getDimensionPixelSize(
				R.dimen.home_tiles_spacing_vertical);
		// int imageTileSpacingHorizontal =
		// getResources().getDimensionPixelSize(
		// R.dimen.home_tiles_spacing_horizontal);

		Logger.v(
				TAG,
				"The device build number is: "
						+ Integer.toString(Build.VERSION.SDK_INT));

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
		mTileSize = (int) ((screenWidth - (imageTileSpacingVertical + imageTileSpacingVertical * 1.5)) / 2);

		Logger.i(TAG, "screenWidth: " + screenWidth + " mTileSize: "
				+ mTileSize);

		/*
		 * gets the list of the media items from the arguments, and sets it as
		 * the source to the adapter.
		 */
		mediaitemMusic = null;
		mediaItems_final = null;

		loadingMore = false;
		mediaitemMusic = new ArrayList<Object>();
		mediaitemMusic.clear();

		mediaItems_final = new ArrayList<MediaItem>();
		mediaItems_final.clear();
		if(OnApplicationStartsActivity.mediaitemMusic!=null && OnApplicationStartsActivity.mediaitemMusic.size()>0) {
			mediaitemMusic = new ArrayList<>(OnApplicationStartsActivity.mediaitemMusic);
			mediaItems_final= new ArrayList<>(OnApplicationStartsActivity.mediaItems_final);
			OnApplicationStartsActivity.mediaitemMusic.clear();
			OnApplicationStartsActivity.mediaItems_final.clear();
			checkMediaItems(mediaItems_final);
		}

		mediaItemsSize = getMediaItemCount();

		// Filter type initialization. It's used for push deeplinking purpose.
		MediaType filterType = null;
		if (getActivity().getIntent().getStringExtra(
				IntentReceiver.CONTENT_TYPE) != null) {
			if (getActivity().getIntent()
					.getStringExtra(IntentReceiver.CONTENT_TYPE).equals("0")) {
				filterType = MediaType.TRACK;
			} else if (getActivity().getIntent()
					.getStringExtra(IntentReceiver.CONTENT_TYPE).equals("1")) {
				filterType = MediaType.ALBUM;
			} else if (getActivity().getIntent()
					.getStringExtra(IntentReceiver.CONTENT_TYPE).equals("2")) {
				filterType = MediaType.PLAYLIST;
			}
			Logger.s(" :::::: content_type :::::::::::::: "
					+ getActivity().getIntent().getStringExtra(
							IntentReceiver.CONTENT_TYPE));
		}

		if (getActivity().getIntent().getStringExtra("Category") != null) {
			if (getActivity().getIntent().getStringExtra("Category")
					.equals("0")) {
				filterType = MediaType.TRACK;
			} else if (getActivity().getIntent().getStringExtra("Category")
					.equals("1")) {
				filterType = MediaType.ALBUM;
			} else if (getActivity().getIntent().getStringExtra("Category")
					.equals("2")) {
				filterType = MediaType.PLAYLIST;
			}
		}

		String flurrySubSectionDescription = "";
		if (data != null
				&& data.containsKey(MediaTileGridFragment.FLURRY_SUB_SECTION_DESCRIPTION)) {
			flurrySubSectionDescription = data
					.getString(MediaTileGridFragment.FLURRY_SUB_SECTION_DESCRIPTION);
		}

		try {
			recyclerView.setAdapter(null);
//			System.gc();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Media adapter initialization
		Logger.e("mediaitemMusic size", "" + getMediaItemCount());
		mediaItemsSize = getMediaItemCount();
		mAdapter = new MyAdapter(mediaitemMusic, getActivity(),
				mediaCategoryType, mediaContentType, mCampaignsManager, false,
				flurrySubSectionDescription, recyclerView);
		mAdapter.setTrendVisibility(true);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(
				getActivity());
		recyclerView.setLayoutManager(mLayoutManager);
		recyclerView.setAdapter(mAdapter);
		recyclerView.setVisibility(View.VISIBLE);
		if (mOnMediaItemOptionSelectedListener == null) {
			try {
				mAdapter.setOnMusicItemOptionSelectedListener((HomeActivity) getActivity());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else
			mAdapter.setOnMusicItemOptionSelectedListener(mOnMediaItemOptionSelectedListener);

		recyclerView.postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView
							.getLayoutManager();
					int visibleItemCount = recyclerView.getChildCount();
					int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
					checkMediaItems(firstVisibleItem, visibleItemCount);
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			}
		}, 500);

		Logger.e("HomeMediaTileGridFragment",
				"mOnMediaItemOptionSelectedListener "
						+ mOnMediaItemOptionSelectedListener);

		// fetch id from intent and pass it to home activity for deeplinking
		long id = 0;
		try {
			if (getActivity().getIntent().getStringExtra("video_content_id") != null) {
				id = Long.parseLong(getActivity().getIntent().getStringExtra(
						"video_content_id"));
			} else if (getActivity().getIntent().getStringExtra(
					"audio_content_id") != null) {
				id = Long.parseLong(getActivity().getIntent().getStringExtra(
						"audio_content_id"));
			} else if (getActivity().getIntent().getStringExtra(
					"video_in_audio_content_id") != null) {
				id = Long.parseLong(getActivity().getIntent().getStringExtra(
						"video_in_audio_content_id"));
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		if (mediaitemMusic != null && id != 0) {
			if (getActivity().getIntent().getStringExtra("video_content_id") != null) {
				try {
					MediaItem tempMedia = new MediaItem(id, "", "", "", "", "",
							"video", 0, 0);
					tempMedia.setMediaContentType(MediaContentType.VIDEO);
					mOnMediaItemOptionSelectedListener
							.onMediaItemOptionShowDetailsSelected(tempMedia, -1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (getActivity().getIntent().getStringExtra(
					"audio_content_id") != null
					&& AlertActivity.isMessage) {
				try {
					if (filterType == null)
						filterType = MediaType.TRACK;
					try {
						MediaItem tempMedia = new MediaItem(id, "", "", "", "",
								"", filterType.toString(), 0, 0);
						tempMedia.setMediaContentType(MediaContentType.MUSIC);
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										tempMedia, -1);
					} catch (Exception e) {
					}
				} catch (Exception e) {
				}
				AlertActivity.isMessage = false;
			} else if (getActivity().getIntent().getStringExtra(
					"video_in_audio_content_id") != null
					&& !HomeActivity.videoInAlbumSet) {
				if (filterType == null)
					filterType = MediaType.TRACK;
				try {
					MediaItem tempMedia = new MediaItem(id, "", "", "", "", "",
							filterType.toString(), -1, 0);
					tempMedia.setMediaContentType(MediaContentType.MUSIC);
					mOnMediaItemOptionSelectedListener
							.onMediaItemOptionShowDetailsSelected(tempMedia, -1);
				} catch (Exception e) {
				}
			}
		}
	}

	public MyAdapter mAdapter;
	private ArrayList<Object> mediaitemMusic = new ArrayList<Object>();
	boolean isLoadingDisplay;

	@Override
	public void onResume() {
		super.onResume();
		Logger.s(mediaCategoryType + " ::::::::::::::;; onResume");
		if (onCreate && rootView != null) {
			onCreate = false;
			isLoadingDisplay = true;
			onRefresh();
		}
	}

	private int getMediaItemCount() {
		int counter = 0;
		if (mediaItems_final != null)
			counter = mediaItems_final.size();
		return counter;
	}

	/**
	 * Registers a callback to be invoked when the user has selected an action
	 * upon a {@link MediaItem}.
	 * 
	 * @param listener
	 */
	public void setOnMediaItemOptionSelectedListener(
			OnMediaItemOptionSelectedListener listener) {
		mOnMediaItemOptionSelectedListener = listener;
	}

	/**
	 * Update adapter media items
	 * 
	 * @param mediaItems
	 */
	public void setMediaItemsMusic(List<Object> mediaItems, boolean isRefill) {
		try {
			if (mPromoUnit != null
					&& (((HomeActivity) getActivity()).isPromoUnit())) {
				if (mAdapter.getItemCount() > 0
						&& mAdapter.getItemViewType(0) == MyAdapter.PROMOUNIT) {
					if (mediaItems.size() == 0)
						mediaItems.add(0, mPromoUnit);
					else if (mediaItems.get(0) instanceof PromoUnit)
						mediaItems.set(0, mPromoUnit);
					else
						mediaItems.add(0, mPromoUnit);
				} else
					mediaItems.add(0, mPromoUnit);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		mAdapter.setMediaItems(mediaItems);
		if (isRefill) {
			mAdapter.resetAd();
		}
		mAdapter.notifyDataSetChanged();

		recyclerView.postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView
							.getLayoutManager();
					int visibleItemCount = recyclerView.getChildCount();
					int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
					checkMediaItems(firstVisibleItem, visibleItemCount);
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			}
		}, 500);
	}

	/**
	 * Filter media items in list based on preferences selected in filter screen
	 */
	public void RefillmediaItems() {
		Logger.s(mediaCategoryType + " ::::::::::::::;; RefillmediaItems");
		/*if(!isSuccessCalled)
			return;;*/
		if (mediaContentType == MediaContentType.MUSIC) {
			if (mediaCategoryType.equals(MediaCategoryType.LATEST)) {
				placement = mCampaignsManager
						.getPlacementOfType(PlacementType.MUSIC_NEW);
			} else if (mediaCategoryType.equals(MediaCategoryType.POPULAR)) {
				placement = mCampaignsManager
						.getPlacementOfType(PlacementType.MUSIC_POPULAR);
			} else if (mediaCategoryType.equals(MediaCategoryType.MY_STREAM)) {
			}
			Logger.s(" ::::::::::::::::::- - RefillmediaItems 1 " + (placement == null));
		}

		List<MediaItem> tracks = new ArrayList<MediaItem>();
		List<MediaItem> playlists = new ArrayList<MediaItem>();

		for (MediaItem mediaItem : mediaItems_final) {
			if (!mApplicationConfigurations.getFilterSongsOption()
					&& !mApplicationConfigurations.getFilterAlbumsOption()
					&& !mApplicationConfigurations.getFilterPlaylistsOption()) {
				if (mediaItem.getMediaType() == MediaType.PLAYLIST)
					playlists.add(mediaItem);
				else
					tracks.add(mediaItem);
			} else if (mediaItem.getMediaType() == MediaType.TRACK
					&& mApplicationConfigurations.getFilterSongsOption())
				tracks.add(mediaItem);
			else if (mediaItem.getMediaType() == MediaType.ALBUM
					&& mApplicationConfigurations.getFilterAlbumsOption())
				tracks.add(mediaItem);
			else if (mediaItem.getMediaType() == MediaType.PLAYLIST
					&& mApplicationConfigurations.getFilterPlaylistsOption())
				playlists.add(mediaItem);

		}

		mediaitemMusic = new ArrayList<Object>();
		mediaitemMusic.clear();

		if (mApplicationConfigurations.getFilterPlaylistsOption()
				&& !mApplicationConfigurations.getFilterSongsOption()
				&& !mApplicationConfigurations.getFilterAlbumsOption()) {
			// Playlist ad placing in list, when filter is set to only show
			// Playlists
			if (mediaCategoryType.equals(MediaCategoryType.POPULAR))
				placement = mCampaignsManager
						.getPlacementOfType(PlacementType.MUSIC_POP_PLAYLIST);
			else
				placement = mCampaignsManager
						.getPlacementOfType(PlacementType.MUSIC_NEW_PLAYLIST);
			if (placement != null) {
				int adStartPosition = 3;
				if (mediaitemMusic.size() > 0
						&& mediaitemMusic.get(0) instanceof PromoUnit)
					adStartPosition = 4;
				for (int i = adStartPosition; i < playlists.size(); i += 6) {
					Logger.i("Hint", String.valueOf(i));
					// isVideoLoaded = true;
					MediaItem temp = new MediaItem(i, "no", "no", "no",
							backgroundLink, backgroundLink, "track", 0, 0);
					temp.setMediaContentType(mediaContentType);
					playlists.add(i, temp);
				}
			}
			mediaitemMusic.addAll(playlists);
		} else {
			// Ad placement in list

			arrangeTilePattern(tracks, playlists);

		}
		setMediaItemsMusic(mediaitemMusic, true);
	}

	private void arrangeTilePattern(List<MediaItem> tracks,
			List<MediaItem> playlists) {
		Logger.s(mediaCategoryType + " ::::::::::::::;; arrangeTilePattern");
		ArrayList<MediaItem> serverSAA = new ArrayList<MediaItem>(tracks);
		int adcount = 0;
		// backgroundLink="http://content.hungama.com/audio%20album/display%20image/400x400%20jpeg/427650334.jpg";
		if (placement != null) {
			for (int i = 0; i < serverSAA.size(); i++) {
				// System.out.println("i"+i +"       "+((i - 4) % 5));

				if (!isAd((MediaItem) serverSAA.get(i))
						&& (i > 0 && (i == 3 || (i > 6 && ((i - 3) % 6) == 0)))) {
					// System.out.println("add ad ");
					serverSAA.add(i /* + adcount */, new MediaItem(i, "no",
							"no", "no", backgroundLink, backgroundLink,
							"album", 0, 0));
					// serverSAA.add(i + adcount, "AD" + adcount);
					// if(i>6)
					adcount++;
				}
			}
			if ((serverSAA.size() > 6 && ((serverSAA.size() - 4) % 6) == 5)
					|| serverSAA.size() % 4 == 3) {
				Logger.s("add ad last " + serverSAA.size());
				serverSAA.add(serverSAA.size(), new MediaItem(serverSAA.size(),
						"no", "no", "no", backgroundLink, backgroundLink,
						"album", 0, 0));
				adcount++;
			}
		}

		if (adcount != 0)
			tracks = serverSAA;

		int blocks = (tracks.size() + playlists.size()) / 5;
		if ((tracks.size() + playlists.size()) % 5 > 0)
			blocks += 1;

		// Playlist & media content arrangement in list as per new design
		// for v4.4
		ComboMediaItem c;
		for (int i = 0; i < blocks; i++) {

			Logger.i("filter", "i=" + i);
			Logger.i("filter", "tracks.size()=" + tracks.size()
					+ "mediaitemMusic.size()=" + mediaitemMusic.size()
					+ "playlists.size()=" + playlists.size());

			if (tracks.size() > 0) {
				c = new ComboMediaItem(tracks.get(0),
						(tracks.size() > 1) ? tracks.get(1) : null);
				mediaitemMusic.add(c);
				tracks.remove(0);
				if (tracks.size() > 0)
					tracks.remove(0);
			}

			if (tracks.size() > 0) {
				c = new ComboMediaItem(tracks.get(0),
						(tracks.size() > 1) ? tracks.get(1) : null);
				mediaitemMusic.add(c);
				tracks.remove(0);
				if (tracks.size() > 0)
					tracks.remove(0);
			}
			if (playlists.size() > 0) {
				mediaitemMusic.add(playlists.get(0));
				playlists.remove(0);

				if (tracks.size() == 0) {
					for (MediaItem obj : playlists) {
						mediaitemMusic.add(obj);
					}
					playlists.clear();
				}
			}
			Logger.i("filter end", "tracks.size()=" + tracks.size()
					+ "mediaitemMusic.size()=" + mediaitemMusic.size()
					+ "playlists.size()=" + playlists.size());
		}
		if (tracks.size() > 0) {
			while (tracks.size() > 0) {
				c = new ComboMediaItem(tracks.get(0),
						(tracks.size() > 1) ? tracks.get(1) : null);
				mediaitemMusic.add(c);
				tracks.remove(0);
				if (tracks.size() > 0)
					tracks.remove(0);
			}
		}
		Logger.i("filter end***", "tracks.size()=" + tracks.size()
				+ "mediaitemMusic.size()=" + mediaitemMusic.size()
				+ "playlists.size()=" + playlists.size());
	}

	/**
	 * Retrieves the adapter which binds the MediaItems with the GridView.
	 */
	public MyAdapter getAdapter() {
		return mAdapter;
	}

	// protected RecyclerView getGridView() {
	// return recyclerView;
	// }
	//
	// protected void setGridView(RecyclerView gridView) {
	// recyclerView = gridView;
	// }

	@Override
	public void onStart() {
		super.onStart();
		Logger.s(mediaCategoryType + " ::::::::::::::;; onStart");
		Analytics.startSession(getActivity(), this);
	}

	@Override
	public void onStop() {
		super.onStop();
		Logger.s(mediaCategoryType + " ::::::::::::::;; onStop");
		Analytics.onEndSession(getActivity());
	}

	// ======================================================
	// Communication Operation Listener Callbacks
	// ======================================================

	@Override
	public void onStart(int operationId) {
		try {
			Logger.s(mediaCategoryType.toString()
					+ " Home Loading** ::::: Start ::::: "
					+ System.currentTimeMillis() + "isRefresh:" + isRefresh);
			if (isLoadingDisplay && (mediaitemMusic==null || (mediaitemMusic!=null && mediaitemMusic.size()==0))) {
				isLoadingDisplay = false;
				progressInit.setVisibility(View.VISIBLE);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	boolean isSuccessCalled = false;
	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			isSuccessCalled = true;
			if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_FEATURED
					|| operationId == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_LATEST
					|| operationId == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_RECOMMANDED) {
				Logger.s(mediaCategoryType.toString()
						+ " Home Loading ::::: success ::::: "
						+ System.currentTimeMillis());
				mediaCategoryType = (MediaCategoryType) responseObjects
						.get(MediaContentOperation.RESULT_KEY_OBJECT_MEDIA_CATEGORY_TYPE);
				mediaItemsResponse = (MediaItemsResponse) responseObjects
						.get(MediaContentOperationPaging.RESPONSE_KEY_OBJECT_MEDIA_ITEMS_RESPONSE);
				if (responseObjects
						.containsKey(HashTagListOperation.RESULT_KEY_MESSAGE)) {
					MessageFromResponse message = (MessageFromResponse) responseObjects
							.get(HashTagListOperation.RESULT_KEY_MESSAGE);
					if (message.getShowMessage() == 1) {
						Utils.makeText(getActivity(), message.getMessageText(),
								Toast.LENGTH_SHORT).show();
					}
				}

				Logger.i(TAG, "Explicit loading MORE media items for LATEST");
				progressBar.setVisibility(View.GONE);

				List<MediaItem> temp = mediaItemsResponse.getContent();
				checkMediaItems(temp);
				mediaitemMusic.clear();
				mediaitemMusic = new ArrayList<Object>();

				if (isRefresh) {
					// Clear array lists for refreshing contents
					mediaitemMusic.clear();
					mediaitemMusic = new ArrayList<Object>();

					mediaItems_final.clear();
					mediaItems_final = new ArrayList<MediaItem>();

					mediaItemsSize = getMediaItemCount();
					loadingMore = false;

					((HomeActivity) getActivity()).MediaContentLoaded();
				}
				mediaItems_final.addAll(temp);
				if (mediaContentType == MediaContentType.MUSIC) {

					List<MediaItem> tracks = new ArrayList<MediaItem>();
					List<MediaItem> playlists = new ArrayList<MediaItem>();

					for (MediaItem mediaItem : mediaItems_final) {
						if (!mApplicationConfigurations.getFilterSongsOption()
								&& !mApplicationConfigurations
										.getFilterAlbumsOption()
								&& !mApplicationConfigurations
										.getFilterPlaylistsOption()) {
							if (mediaItem.getMediaType() == MediaType.PLAYLIST)
								playlists.add(mediaItem);
							else
								tracks.add(mediaItem);
						} else if (mediaItem.getMediaType() == MediaType.TRACK
								&& mApplicationConfigurations
										.getFilterSongsOption())
							tracks.add(mediaItem);
						else if (mediaItem.getMediaType() == MediaType.ALBUM
								&& mApplicationConfigurations
										.getFilterAlbumsOption())
							tracks.add(mediaItem);
						else if (mediaItem.getMediaType() == MediaType.PLAYLIST
								&& mApplicationConfigurations
										.getFilterPlaylistsOption())
							playlists.add(mediaItem);

					}

					if (mApplicationConfigurations.getFilterPlaylistsOption()
							&& !mApplicationConfigurations
									.getFilterSongsOption()
							&& !mApplicationConfigurations
									.getFilterAlbumsOption()) {
						// Playlist ad placing in list, when filter is set to
						// only show Playlists
						if (mediaCategoryType.equals(MediaCategoryType.POPULAR))
							placement = mCampaignsManager
									.getPlacementOfType(PlacementType.MUSIC_POP_PLAYLIST);
						else
							placement = mCampaignsManager
									.getPlacementOfType(PlacementType.MUSIC_NEW_PLAYLIST);

						if (isRefresh) {
							mediaitemMusic.addAll(playlists);
							if (placement != null) {
								for (int i = 3; i < mediaitemMusic.size(); i += 6) {
									Logger.i("Hint", String.valueOf(i));
									// isVideoLoaded = true;
									if (!isAd((MediaItem) mediaitemMusic.get(i))) {
										MediaItem tempMedia = new MediaItem(i,
												"no", "no", "no",
												backgroundLink, backgroundLink,
												"track", 0, 0);
										tempMedia
												.setMediaContentType(mediaContentType);
										mediaitemMusic.add(i, tempMedia);
									}
								}
							}
						} else {
							mediaitemMusic.addAll(playlists);
							if (placement != null) {
								int adStartPosition = 3;
								if (mediaitemMusic.size() > 0
										&& mediaitemMusic.get(0) instanceof PromoUnit)
									adStartPosition = 4;
								for (int i = adStartPosition; i < mediaitemMusic
										.size(); i += 6) {
									Logger.i("Hint", String.valueOf(i));
									// isVideoLoaded = true;
									if (!isAd((MediaItem) mediaitemMusic.get(i))) {
										MediaItem tempMedia = new MediaItem(i,
												"no", "no", "no",
												backgroundLink, backgroundLink,
												"track", 0, 0);
										tempMedia
												.setMediaContentType(mediaContentType);
										mediaitemMusic.add(i, tempMedia);
									}
								}
							}
						}
					} else {
						// Ad placement in list

						arrangeTilePattern(tracks, playlists);
					}
				}
				setMediaItemsMusic(mediaitemMusic, false);
				loadingMore = false;
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		if (isRefresh) {
			isRefresh = false;

			mediaItemsSize = getMediaItemCount();
			loadingMore = false;
		}
		progressInit.setVisibility(View.GONE);
	}

	/**
	 * Check whether media item is ad or not
	 * 
	 * @param mediaItem
	 *            media item to verify
	 * @return true if is ad, else false
	 */
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

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		Logger.e(TAG, "Failed to load media content " + errorMessage);
		isRefresh = false;
		loadingMore = false;
		if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_LATEST) {
		}
		progressInit.setVisibility(View.GONE);
	}

	@Override
	public void onDestroyView() {
//		System.gc();
//		System.runFinalization();
		super.onDestroyView();
	}

	ApplicationConfigurations mApplicationConfigurations;

	/**
	 * get tile count from list
	 * 
	 * @return tile count
	 */
	private int getFilteredTileCount() {
		if (mediaContentType == MediaContentType.MUSIC) {
			if (!mApplicationConfigurations.getFilterSongsOption()
					&& !mApplicationConfigurations.getFilterAlbumsOption()
					&& !mApplicationConfigurations.getFilterPlaylistsOption())
				return com.hungama.myplay.activity.util.Constants.LOADING_CHUNK_NUMBER;
			int count = 0;
			if (mApplicationConfigurations.getFilterSongsOption())
				count = 10;
			if (mApplicationConfigurations.getFilterAlbumsOption())
				count += 10;
			if (mApplicationConfigurations.getFilterPlaylistsOption())
				count += 10;
			return count;
		} else {
			return com.hungama.myplay.activity.util.Constants.LOADING_CHUNK_NUMBER;
		}
	}

	/**
	 * Get ad count from list
	 * 
	 * @return ad count
	 */
	private int getFilteredTileAdCount() {
		if (mediaContentType == MediaContentType.MUSIC) {
			if (!mApplicationConfigurations.getFilterSongsOption()
					&& !mApplicationConfigurations.getFilterAlbumsOption()
					&& !mApplicationConfigurations.getFilterPlaylistsOption())
				return 6;
			int count = 0;
			if (mApplicationConfigurations.getFilterSongsOption())
				count = 2;
			if (mApplicationConfigurations.getFilterAlbumsOption())
				count += 2;
			if (mApplicationConfigurations.getFilterPlaylistsOption())
				count += 2;
			return count;
		} else {
			return 6;
		}
	}

	/**
	 * post ad view event to CM when home screen page changed
	 */
	public void postAd() {
		if (mAdapter != null) {
			mAdapter.clearAdPositions();
			mAdapter.postAdForPosition();
		}
	}

	/**
	 * Load music tile content in paging
	 * 
	 * @param start
	 *            page start index
	 * @param length
	 *            page content length
	 * @param mediaCategoryType
	 *            category type
	 * @param context
	 *            activity context
	 */
	public void loadMoreResults(int start, int length,
			MediaCategoryType mediaCategoryType,
			CommunicationOperationListener context) {
		String timestamp_cache = null;
		if (mediaContentType == MediaContentType.MUSIC) {
			if (mediaCategoryType == MediaCategoryType.LATEST)
				timestamp_cache = mDataManager.getApplicationConfigurations()
						.getMusicLatestTimeStamp();
			else
				timestamp_cache = mDataManager.getApplicationConfigurations()
						.getMusicPopularTimeStamp();
		}

		mDataManager.getMediaItemsPaging(mediaContentType, mediaCategoryType,
				null, String.valueOf(start), String.valueOf(length), context,
				timestamp_cache);
	}

	private PromoUnit mPromoUnit;

	public void setPromoUnit(PromoUnit mPromoUnit) {
		this.mPromoUnit = mPromoUnit;
		if (getActivity() != null && mPromoUnit != null
				&& (((HomeActivity) getActivity()).isPromoUnit()))
			setMediaItemsMusic(mediaitemMusic, false);
	}

	private void checkMediaItems(int firstVisibleItem, int visibleItemCount) {
//		Logger.s("SongIds ::: " + firstVisibleItem + " ::: " + visibleItemCount);
//		for (int i = firstVisibleItem; i < (firstVisibleItem + visibleItemCount); i++) {
//			if (mediaitemMusic != null && i < mediaitemMusic.size()) {
//				if (mediaitemMusic.get(i) instanceof MediaItem) {
//					checkMediaSongIds((MediaItem) mediaitemMusic.get(i));
//				} else if (mediaitemMusic.get(i) instanceof ComboMediaItem) {
//					ComboMediaItem comboMediaItem = (ComboMediaItem) mediaitemMusic.get(i);
//					if (comboMediaItem.left != null) {
//						checkMediaSongIds(comboMediaItem.left);
//					}
//					if (comboMediaItem.right != null) {
//						checkMediaSongIds(comboMediaItem.right);
//					}
//				}
//			}
//		}
	}

	private void checkMediaItems(List<MediaItem> mediaItems) {
//		if (mediaItems != null && mediaItems.size()>0) {
//			Logger.s("SongIds ::: " + mediaItems.size());
//			for (int i = 0; i < (mediaItems.size()); i++) {
//				checkMediaSongIds(mediaItems.get(i));
//			}
//		}
	}

	private void checkMediaSongIds(MediaItem mediaItem) {
		if (mediaItem.getMediaType() == MediaType.ALBUM)
			Logger.s("SongIds ::: " + mediaItem.getAlbumName());
		else
			Logger.s("SongIds ::: " + mediaItem.getTitle());
		if (mediaItem.getMediaType() == MediaType.PLAYLIST || mediaItem.getMediaType() == MediaType.ALBUM) {
			if (mediaItem.getSongIds() != null && mediaItem.getSongIds().size() > 0) {
				if (!mapTracks.containsKey(mediaItem.getSongIds().get(0))) {
					Logger.s("SongIds ::: " + mediaItem.getSongIds().get(0));
					mapTracks.put(mediaItem.getSongIds().get(0), null);
					if (mediaItem.getMediaType() == MediaType.ALBUM) {
						loadTrackDetail(mediaItem.getSongIds().get(0), mediaItem.getId());
					} else {
						loadTrackDetail(mediaItem.getSongIds().get(0), 0);
					}
				}
			}
		}
	}

	private final Object mTrackDetailMutext = new Object();

	private void loadTrackDetail(final long songId, final long albumId) {
		Logger.s("SongIds ::: load details " + songId);
		ThreadPoolManager.getInstance().submit(new Runnable() {
			@Override
			public void run() {
				synchronized (mTrackDetailMutext) {
//					super.run();
					MediaItem mediaTrack = new MediaItem(songId, null, null,
							null, null, null, MediaType.TRACK.toString(), 0,
							albumId);
					ServerConfigurations mServerConfigurations = mDataManager.getServerConfigurations();
					String images = ImagesManager.getImageSize(
							ImagesManager.MUSIC_ART_SMALL,
							DataManager.getDisplayDensityLabel())
							+ ","
							+ ImagesManager.getImageSize(
							ImagesManager.MUSIC_ART_BIG,
							DataManager.getDisplayDensityLabel());
					CommunicationManager communicationManager = new CommunicationManager();
					try {
						CommunicationManager.Response response = communicationManager.performOperationNew(
								new MediaDetailsOperation(mServerConfigurations
										.getHungamaServerUrl_2(), mServerConfigurations
										.getHungamaAuthKey(), ApplicationConfigurations
										.getInstance(getActivity()).getPartnerUserId(),
										mediaTrack, null, images), getActivity());
						Logger.i("response", "SongIds ::: Media Detail Response:"
								+ response);
						if (response != null) {
							JSONParser jsonParser = new JSONParser();
							Map<String, Object> catalogMap = (Map<String, Object>) jsonParser
									.parse(response.response);
							String reponseString = null;
							if (catalogMap
									.containsKey(HungamaOperation.KEY_RESPONSE)) {
								reponseString = catalogMap.get(
										HungamaOperation.KEY_RESPONSE)
										.toString();
							}
							if (reponseString != null) {
								MediaTrackDetails mediaTrackDetails = new Gson().fromJson(reponseString,
										MediaTrackDetails.class);
								mapTracks.put(songId, mediaTrackDetails);
							} else {
								mapTracks.remove(songId);
							}
						}
					} catch (Exception e) {
						Logger.printStackTrace(e);
						mapTracks.remove(songId);
					}
				}
			}
		});
	}

    /*public static final Track getTrarkBySongId(long songId) {
        if (mapTracks.containsKey(songId)) {
            MediaTrackDetails mediaTrackDetails = mapTracks.get(songId);
            if (mediaTrackDetails != null) {
                Track track = new Track(mediaTrackDetails.getId(),
                        mediaTrackDetails.getTitle(), mediaTrackDetails.getAlbumName(),
                        mediaTrackDetails.getSingers(), mediaTrackDetails.getImageUrl(),
                        mediaTrackDetails.getBigImageUrl(), mediaTrackDetails.getImages(),
                        mediaTrackDetails.getAlbumId());
                return track;
            }
        }
        return null;
    }*/
    public static final Track getTrarkBySongId(MediaItemPlaylist playListSongDetail) {
        Track track = new Track(playListSongDetail.id,
                playListSongDetail.title, playListSongDetail.title,
                playListSongDetail.title, "",
                "", null,
                0);
        return track;
    }
}