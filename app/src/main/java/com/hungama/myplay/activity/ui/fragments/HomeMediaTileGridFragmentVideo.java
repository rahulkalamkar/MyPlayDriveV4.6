package com.hungama.myplay.activity.ui.fragments;

import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.campaigns.PlacementType;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaItemsResponse;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.MessageFromResponse;
import com.hungama.myplay.activity.data.dao.hungama.PromoUnit;
import com.hungama.myplay.activity.gcm.IntentReceiver;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.HashTagListOperation;
import com.hungama.myplay.activity.operations.hungama.MediaContentOperation;
import com.hungama.myplay.activity.operations.hungama.MediaContentOperationPaging;
import com.hungama.myplay.activity.ui.AlertActivity;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.OnApplicationStartsActivity;
import com.hungama.myplay.activity.ui.adapters.MediaTilesAdapterVideo;
import com.hungama.myplay.activity.ui.adapters.MyAdapter;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.ui.widgets.LanguageButton;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.hungama.myplay.activity.util.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the {@link MediaTileGridFragment} that gives the required
 * behavior for presenting media items in the grid.
 */
public class HomeMediaTileGridFragmentVideo extends MainFragment implements
		CommunicationOperationListener, SwipeRefreshLayout.OnRefreshListener {

	private static final String TAG = "MediaTileGridFragment";

	// public static final String FRAGMENT_ARGUMENT_MEDIA_ITEMS =
	// "fragment_argument_media_items";
	public static final String FRAGMENT_ARGUMENT_MEDIA_CATEGORY_TYPE = "fragment_argument_media_category_type";

	private GridView mTilesGridView;
	private LinearLayout progressBar;
	private int mTileSize = 0;
	private DataManager mDataManager;

	public MediaTilesAdapterVideo mHomeMediaTilesAdapter;

	private OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener;
	private OnRetryButtonClickedLister mOnRetryButtonClickedLister;

	public List<MediaItem> mediaItems;
	private MediaCategoryType mediaCategoryType;
	private MediaContentType mediaContentType;
	private MediaItemsResponse mediaItemsResponse;
	private int itemsInList = com.hungama.myplay.activity.util.Constants.LOADING_CHUNK_NUMBER;

	private String backgroundLink;
	private View rootView;
	private Placement placement;
	private CampaignsManager mCampaignsManager;
	private ProgressBar progressInit;

	// public MediaContentType mct;

	// public boolean isMusicLoaded, isVideoLoaded;
	private SwipeRefreshLayout mListViewContainer;

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
		isRefresh = true;
		itemsInList = com.hungama.myplay.activity.util.Constants.LOADING_CHUNK_NUMBER;

		loadMoreResults(1, Constants.LOADING_CHUNK_NUMBER, mediaCategoryType,
				HomeMediaTileGridFragmentVideo.this);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mListViewContainer.setRefreshing(false);

			}
		}, 1000);
	}

	boolean isRefresh;
	private boolean onCreate = false;

	// ======================================================
	// FRAGMENT'S LIFE CYCLE.
	// ======================================================

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mDataManager = DataManager.getInstance(getActivity());

		setRetainInstance(true);
		mApplicationConfigurations = ApplicationConfigurations
				.getInstance(getActivity());
		onCreate = true;
		Analytics.postCrashlitycsLog(getActivity(), HomeMediaTileGridFragmentVideo.class.getName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (rootView == null) {
			try {
				rootView = inflater.inflate(
						R.layout.fragment_home_media_tiles_grid, container,
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
						R.layout.fragment_home_media_tiles_grid, container,
						false);
				if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
					Utils.traverseChild(rootView, getActivity());
				}
			}

			Bundle data = getArguments();
			Logger.e("HomeMediaTileGridFragment", "onCreateView");
			// SwipeRefreshLayout
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

	public void setUpData(Bundle data) {

		if (data != null
				&& data.containsKey(HomeMediaTileGridFragmentVideo.FRAGMENT_ARGUMENT_MEDIA_CATEGORY_TYPE)) {
			mCampaignsManager = CampaignsManager.getInstance(getActivity());
			mediaCategoryType = (MediaCategoryType) data
					.getSerializable(HomeMediaTileGridFragmentVideo.FRAGMENT_ARGUMENT_MEDIA_CATEGORY_TYPE);
			if (!Utils
					.isListEmpty(((List<MediaItem>) data
							.getSerializable(MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS)))) {
				mediaContentType = ((List<MediaItem>) data
						.getSerializable(MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS))
						.get(0).getMediaContentType();
			}
			if (((MediaContentType) data
					.getSerializable(HomeMediaTileGridFragmentNew.FRAGMENT_ARGUMENT_MEDIA_CONTENT_TYPE)) != null) {
				mediaContentType = (MediaContentType) data
						.getSerializable(HomeMediaTileGridFragmentNew.FRAGMENT_ARGUMENT_MEDIA_CONTENT_TYPE);
			}
			if (mediaContentType == MediaContentType.MUSIC) {
				if (mediaCategoryType.equals(MediaCategoryType.LATEST)) {
					placement = mCampaignsManager
							.getPlacementOfType(PlacementType.MUSIC_NEW);
				} else if (mediaCategoryType.equals(MediaCategoryType.POPULAR)) {
					placement = mCampaignsManager
							.getPlacementOfType(PlacementType.MUSIC_POPULAR);
				} else if (mediaCategoryType
						.equals(MediaCategoryType.MY_STREAM)) {

				}
			} else if (mediaContentType == MediaContentType.VIDEO) {
				if (mediaCategoryType.equals(MediaCategoryType.LATEST)) {
					placement = mCampaignsManager
							.getPlacementOfType(PlacementType.VIDEO_NEW);
				} else if (mediaCategoryType.equals(MediaCategoryType.POPULAR)) {
					placement = mCampaignsManager
							.getPlacementOfType(PlacementType.VIDEOS_POPULAR);
				} else if (mediaCategoryType
						.equals(MediaCategoryType.MY_STREAM)) {

				}
			}
			if (placement != null) {
				{
					itemsInList = getFilteredTileCount()
							+ getFilteredTileAdCount();
					DisplayMetrics metrics = HomeActivity.metrics;
					backgroundLink = Utils
							.getDisplayProfile(metrics, placement);
				}
			} else
				itemsInList = getFilteredTileCount();

		}
		initializeTiles(rootView, data);
		progressInit = (ProgressBar) rootView
				.findViewById(R.id.progressBar_init);
	}

	RecyclerView recyclerView;

	private void initializeTiles(View rootView, Bundle data) {

		recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

		mTilesGridView = (GridView) rootView
				.findViewById(R.id.media_tile_gridview);
		mTilesGridView.setVisibility(View.GONE);

		recyclerView.setVisibility(View.VISIBLE);

		progressBar = (LinearLayout) rootView
				.findViewById(R.id.progressBarLayout);
		recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

			@Override
			public void onScrollStateChanged(RecyclerView view, int scrollState) {
				if (scrollState == RecyclerView.SCROLL_STATE_IDLE) {
					PicassoUtil.with(getActivity()).resumeTag(
							PicassoUtil.PICASSO_VIDEO_LIST_TAG);
					try {
						processPaging(view);
						mHomeMediaTilesAdapter.postAdForPosition();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					PicassoUtil.with(getActivity()).pauseTag(
							PicassoUtil.PICASSO_VIDEO_LIST_TAG);
				}
			}

			void processPaging(RecyclerView recyclerView) {
				LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView
						.getLayoutManager();
				int visibleItemCount = recyclerView.getChildCount();
				int totalItemCount = layoutManager.getItemCount();
				int firstVisibleItem = layoutManager
						.findFirstVisibleItemPosition();

				int pos_start_end = firstVisibleItem + visibleItemCount;

				if (mPromoUnit != null
						&& (((HomeActivity) getActivity()).isPromoUnit())) {
					totalItemCount -= 1;
					pos_start_end -= 1;
				}

				Logger.i("ItemsInList + tic", String.valueOf(itemsInList)
						+ ", " + String.valueOf(totalItemCount));

				if (totalItemCount > 0) {
					int mediaItemsSize = mediaItems.size();
					if (placement != null) {
						mediaItemsSize = mediaItemsSize
								- ((mediaItemsSize / (getFilteredTileCount() + getFilteredTileAdCount())) * getFilteredTileAdCount());

					}

					if (mediaItemsResponse != null)
						Logger.i("onScroll 0>>  + tic",
								(pos_start_end) + ", " + mediaItemsSize + ","
										+ mediaItemsResponse.getTotal());

					if (pos_start_end == totalItemCount
							&& totalItemCount == itemsInList
							&& (mediaItemsResponse != null && mediaItemsSize <= mediaItemsResponse
									.getTotal())
							&& (mediaItemsSize % getFilteredTileCount() == 0)) {
						Logger.i(TAG, "Loading more results");
						if (getActivity() != null) {
							progressBar.setVisibility(View.VISIBLE);
							int adCount = 0;
							if (placement != null) {
								itemsInList = totalItemCount
										+ getFilteredTileCount()
										+ getFilteredTileAdCount();
								adCount = (totalItemCount / (getFilteredTileCount() + getFilteredTileAdCount()))
										* getFilteredTileAdCount();
							} else {
								itemsInList = totalItemCount
										+ getFilteredTileCount();
							}

							int start = 1 + totalItemCount - adCount;
							Logger.i(TAG, "Start = " + start);
							((HomeActivity) getActivity()).loadMoreResults(
									start, Constants.LOADING_CHUNK_NUMBER,
									mediaCategoryType,
									HomeMediaTileGridFragmentVideo.this);
						}
					}
				}

			}
		});
		LinearLayout emptyView = (LinearLayout) rootView
				.findViewById(R.id.connection_error_empty_view);
		Utils.SetMultilanguageTextOnTextView(
				getActivity(),
				(LanguageTextView) emptyView
						.findViewById(R.id.connection_error_empty_view_title),
				getActivity().getString(
						R.string.connection_error_empty_view_title));
		LanguageButton retryButton = (LanguageButton) emptyView
				.findViewById(R.id.connection_error_empty_view_button_retry);
		Utils.SetMultilanguageTextOnButton(
				getActivity().getApplicationContext(),
				retryButton,
				getActivity().getResources().getString(
						R.string.connection_error_empty_view_button_retry));
		retryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mOnRetryButtonClickedLister != null) {
					mOnRetryButtonClickedLister.onRetryButtonClicked(view);
				}
			}
		});

		LanguageButton offlineButton = (LanguageButton) emptyView
				.findViewById(R.id.connection_error_empty_view_button_play_offline);
		Utils.SetMultilanguageTextOnButton(
				getActivity().getApplicationContext(),
				offlineButton,
				getActivity()
						.getResources()
						.getString(
								R.string.connection_error_empty_view_button_play_offline));
		offlineButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				try {
					HomeActivity act = (HomeActivity) getActivity();
					act.handleOfflineSwitchCase(true);
				} catch (Exception e) {
				}
			}
		});

		int imageTileSpacingVertical = getResources().getDimensionPixelSize(
				R.dimen.home_tiles_spacing_vertical);

		Logger.v(
				TAG,
				"The device build number is: "
						+ Integer.toString(Build.VERSION.SDK_INT));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			recyclerView.setBackground(null);
		} else {
			recyclerView.setBackgroundDrawable(null);
		}

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

		mediaItems = null;

		MediaType filterType = null;

		if (data != null
				&& data.containsKey(MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS)
				&& !Utils
						.isListEmpty((List<MediaItem>) data
								.getSerializable(MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS))) {
			mediaItems = new ArrayList<MediaItem>();

			if (getActivity().getIntent().getStringExtra(
					IntentReceiver.CONTENT_TYPE) != null) {
				if (getActivity().getIntent()
						.getStringExtra(IntentReceiver.CONTENT_TYPE)
						.equals("0")) {
					filterType = MediaType.TRACK;
				} else if (getActivity().getIntent()
						.getStringExtra(IntentReceiver.CONTENT_TYPE)
						.equals("1")) {
					filterType = MediaType.ALBUM;
				} else if (getActivity().getIntent()
						.getStringExtra(IntentReceiver.CONTENT_TYPE)
						.equals("2")) {
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
			List<MediaItem> buffer = (List<MediaItem>) data
					.getSerializable(MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS);
			// for (int i = 0; i < buffer.size(); i++) {
			// mediaItems.add(buffer.get(i));
			// }

			for (int i = 0; i < buffer.size(); i++) {
				mediaItems.add(buffer.get(i));
			}

			if (placement != null && mediaContentType == MediaContentType.VIDEO) {
				int adPos = 0;
				for (int i = 3; i < mediaItems.size(); i += 4) {
					adPos++;
					Logger.i("Hint", String.valueOf(i));
					// isVideoLoaded = true;
					MediaItem temp = new MediaItem(i, "no", "no", "no",
							backgroundLink, backgroundLink, "track", 0, 0);
					temp.setMediaContentType(mediaContentType);
					mediaItems.add(i, temp);
				}
				if (adPos == 9) {
					MediaItem tempAd = new MediaItem(mediaItems.size(), "no",
							"no", "no", backgroundLink, backgroundLink,
							"track", 0, 0);
					tempAd.setMediaContentType(mediaContentType);
					mediaItems.add(mediaItems.size(), tempAd);
				}
			}

			mediaItemsResponse = (MediaItemsResponse) data
					.getSerializable(MediaContentOperationPaging.RESPONSE_KEY_OBJECT_MEDIA_ITEMS_RESPONSE);
		}

		String flurrySubSectionDescription = "";
		if (data != null
				&& data.containsKey(MediaTileGridFragment.FLURRY_SUB_SECTION_DESCRIPTION)) {
			flurrySubSectionDescription = data
					.getString(MediaTileGridFragment.FLURRY_SUB_SECTION_DESCRIPTION);
		}

		try {
			mHomeMediaTilesAdapter = null;
			recyclerView.setAdapter(null);
		} catch (Exception e) {
			e.printStackTrace();
		}

        if(mediaItems==null)
            mediaItems = new ArrayList<>();
		if(OnApplicationStartsActivity.mediaitemVideo!=null && OnApplicationStartsActivity.mediaitemVideo.size()>0 && mediaItems.size()==0){
            mediaItems = new ArrayList<>(OnApplicationStartsActivity.mediaitemVideo);
            OnApplicationStartsActivity.mediaitemVideo.clear();
            recyclerView.setVisibility(View.VISIBLE);
		}

		mHomeMediaTilesAdapter = new MediaTilesAdapterVideo(getActivity(),
				recyclerView, mTileSize, this.getClass().getCanonicalName(),
				mediaCategoryType, mediaContentType, mCampaignsManager,
				mediaItems, false, flurrySubSectionDescription);
		mHomeMediaTilesAdapter.setTrendVisibility(true);
		if (mPromoUnit != null
				&& (((HomeActivity) getActivity()).isPromoUnit()))
			setMediaItems(mediaItems);

		int scrollPosition = -1;

		if (mOnMediaItemOptionSelectedListener == null) {
			try {
				mHomeMediaTilesAdapter
						.setOnMusicItemOptionSelectedListener((HomeActivity) getActivity());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else
			mHomeMediaTilesAdapter
					.setOnMusicItemOptionSelectedListener(mOnMediaItemOptionSelectedListener);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(
				getActivity());
		recyclerView.setLayoutManager(mLayoutManager);
		recyclerView.setAdapter(mHomeMediaTilesAdapter);

		Logger.e("HomeMediaTileGridFragment",
				"mOnMediaItemOptionSelectedListener "
						+ mOnMediaItemOptionSelectedListener);

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
		if (mediaItems != null) {
			for (Object mi : mediaItems) {
				MediaItem mediaItem = (MediaItem) mi;
				Logger.i("RequestId", String.valueOf(mediaItem.getId()));
				if (mediaItem.getId() == id) {
					scrollPosition = mediaItems.indexOf(mi);
					break;
				}
			}

			if (getActivity().getIntent().getStringExtra("video_content_id") != null) {
				Logger.s(mediaItems.size() + " ::::::video_content_id::::: "
						+ scrollPosition + " ::::::::: " + id);
				if (scrollPosition > -1) {
					try {
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										(MediaItem) mediaItems
												.get(scrollPosition),
										scrollPosition);
					} catch (Exception e) {
						e.printStackTrace();
					}
					// mOnMediaItemOptionSelectedListener.onMediaItemOptionAddToQueueSelected(mediaItems.get(scrollPosition),
					// scrollPosition);
				} else {
					// showDetails((int) id, MediaContentType.VIDEO);
					try {
						MediaItem tempMedia = new MediaItem(id, "", "", "", "",
								"", "video", 0, 0);
						tempMedia.setMediaContentType(MediaContentType.VIDEO);
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										tempMedia, -1);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else if (getActivity().getIntent().getStringExtra(
					"audio_content_id") != null
					&& AlertActivity.isMessage) {
				try {
					Logger.s(mediaItems.size()
							+ " ::::::audio_content_id::::: " + scrollPosition
							+ " ::::::::: " + id + " :: "
							+ AlertActivity.isMessage);
					if (scrollPosition > -1) {
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										(MediaItem) mediaItems
												.get(scrollPosition),
										scrollPosition);
						// mOnMediaItemOptionSelectedListener.onMediaItemOptionAddToQueueSelected(mediaItems.get(scrollPosition),
						// scrollPosition);
					} else {
						// showDetails((int) id, MediaContentType.MUSIC);
						if (filterType == null)
							filterType = MediaType.TRACK;
						try {
							MediaItem tempMedia = new MediaItem(id, "", "", "",
									"", "", filterType.toString(), 0, 0);
							tempMedia
									.setMediaContentType(MediaContentType.MUSIC);
							mOnMediaItemOptionSelectedListener
									.onMediaItemOptionShowDetailsSelected(
											tempMedia, -1);
						} catch (Exception e) {
						}
						// mOnMediaItemOptionSelectedListener.onMediaItemOptionAddToQueueSelected(tempMedia,
						// -1);
					}
				} catch (Exception e) {
				}
				AlertActivity.isMessage = false;
			} else if (getActivity().getIntent().getStringExtra(
					"video_in_audio_content_id") != null
					&& !HomeActivity.videoInAlbumSet) {
				Logger.s(mediaItems.size()
						+ " ::::::video_in_audio_content_id::::: "
						+ scrollPosition + " ::::::::: " + id);
				if (scrollPosition > -1) {
					mOnMediaItemOptionSelectedListener
							.onMediaItemOptionShowDetailsSelected(
									(MediaItem) mediaItems.get(scrollPosition),
									scrollPosition);
				} else {
					// showDetails((int) id, MediaContentType.MUSIC);
					if (filterType == null)
						filterType = MediaType.TRACK;
					try {
						MediaItem tempMedia = new MediaItem(id, "", "", "", "",
								"", filterType.toString(), -1, 0);
						tempMedia.setMediaContentType(MediaContentType.MUSIC);
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										tempMedia, -1);
					} catch (Exception e) {
					}
				}
			}
		}
	}

	boolean isLoadingDisplay;

	@Override
	public void onResume() {
		super.onResume();

		if (onCreate && rootView != null) {
			onCreate = false;
			isLoadingDisplay = true;
			onRefresh();
		}

		if (mHomeMediaTilesAdapter != null) {
			mHomeMediaTilesAdapter.resumeLoadingImages();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mHomeMediaTilesAdapter != null) {
			mHomeMediaTilesAdapter.stopLoadingImages();
		}
	}

	// ======================================================
	// PABLIC.
	// ======================================================

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

	public void setMediaItems(List<MediaItem> mediaItems) {
		List<Object> temp = new ArrayList<Object>();
		if (mediaItems != null)
			temp.addAll(mediaItems);
		if (mPromoUnit != null
				&& (((HomeActivity) getActivity()).isPromoUnit())) {
			if (mHomeMediaTilesAdapter.getItemCount() > 0
					&& mHomeMediaTilesAdapter.getItemViewType(0) == MyAdapter.PROMOUNIT)
				temp.set(0, mPromoUnit);
			else
				temp.add(0, mPromoUnit);
		}
		mHomeMediaTilesAdapter.setMediaItems(temp);
		// mHomeMediaTilesAdapter.notifyDataSetChanged();
	}

	/**
	 * Retrieves the adapter which binds the MediaItems with the GridView.
	 */
	public MediaTilesAdapterVideo getAdapter() {
		return mHomeMediaTilesAdapter;
	}

	// protected RecyclerView getGridView() {
	// return recyclerView;
	// }

	// protected void setGridView(RecyclerView gridView) {
	// recyclerView = gridView;
	// }

	public interface OnRetryButtonClickedLister {

		public void onRetryButtonClicked(View retryButton);
	}

	public void setOnRetryButtonClickedLister(
			OnRetryButtonClickedLister listener) {
		mOnRetryButtonClickedLister = listener;
	}

	@Override
	public void onStart() {
		// if(mHomeMediaTilesAdapter != null){
		// mHomeMediaTilesAdapter.startFlurrySession();
		// }
		super.onStart();
		// FlurryAgent.setContinueSessionMillis(Constants.FLURRY_SESSION_LENGTH);
		// FlurryAgent.onStartSession(getActivity(),
		// getString(R.string.flurry_app_key));
		Analytics.startSession(getActivity(), this);
	}

	@Override
	public void onStop() {
		// if(mHomeMediaTilesAdapter != null){
		// mHomeMediaTilesAdapter.endFlurrySession();
		// }
		super.onStop();
		Analytics.onEndSession(getActivity());
	}

	// ======================================================
	// Communication Operation Listener Callbacks
	// ======================================================

	@Override
	public void onStart(int operationId) {
		try {
			if (isLoadingDisplay && (mediaItems==null|| (mediaItems!=null && mediaItems.size()==0))) {
				isLoadingDisplay = false;
				progressInit.setVisibility(View.VISIBLE);
			}

		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_FEATURED
					|| operationId == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_LATEST
					|| operationId == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_RECOMMANDED) {

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

				// if (mediaCategoryType == MediaCategoryType.LATEST) {
				Logger.i(TAG, "Explicit loading MORE media items for LATEST");
				progressBar.setVisibility(View.GONE);

				if (isRefresh) {
					isRefresh = false;
					if (mediaItems != null)
						mediaItems.clear();
					mediaItems = new ArrayList<MediaItem>();
					List<MediaItem> temp = mediaItemsResponse.getContent();
					mediaItems.addAll(temp);
					Bundle arguments = getArguments();
					arguments
							.putSerializable(
									MediaContentOperationPaging.RESPONSE_KEY_OBJECT_MEDIA_ITEMS_RESPONSE,
									(Serializable) mediaItemsResponse);

					arguments
							.putSerializable(
									MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS,
									(Serializable) mediaItems);

					// frag1.setArguments(arguments);

					setUpData(arguments);
				} else {
					List<MediaItem> temp = mediaItemsResponse.getContent();

					if (placement != null
							&& mediaContentType == MediaContentType.VIDEO) {
						int adPos = 0;
						for (int i = 3; i < temp.size(); i += 4) {
							adPos++;
							Logger.i("Hint", String.valueOf(i));
							// isVideoLoaded = true;
							MediaItem tempAd = new MediaItem(i, "no", "no",
									"no", backgroundLink, backgroundLink,
									"track", 0, 0);
							tempAd.setMediaContentType(mediaContentType);
							temp.add(i, tempAd);
						}
						if (adPos == 9) {
							MediaItem tempAd = new MediaItem(temp.size(), "no",
									"no", "no", backgroundLink, backgroundLink,
									"track", 0, 0);
							tempAd.setMediaContentType(mediaContentType);
							temp.add(temp.size(), tempAd);
						}
					}
					mediaItems.addAll(temp);
					setMediaItems(mediaItems);
				}
				// }
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.printStackTrace(e);
		}
		isRefresh = false;
		progressInit.setVisibility(View.GONE);
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {

		Logger.e(TAG, "Failed to load media content " + errorMessage);
		isRefresh = false;
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

	public boolean isMediaItemNotExist() {
		Bundle data = getArguments();
		if (data == null
				|| (data != null && data
						.getSerializable(MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS) == null)
				|| (data != null && Utils
						.isListEmpty(((List<MediaItem>) data
								.getSerializable(MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS))))) {
			return true;
		}
		return false;
	}

	public boolean isMediaItemResponseNotExist() {
		Bundle data = getArguments();
		if (data == null
				|| (data != null && data
						.getSerializable(MediaContentOperationPaging.RESPONSE_KEY_OBJECT_MEDIA_ITEMS_RESPONSE) == null)) {
			return true;
		}
		return false;
	}

	ApplicationConfigurations mApplicationConfigurations;

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
			return 10;
		}
	}

	public void postAd() {
		if (mHomeMediaTilesAdapter != null) {
			mHomeMediaTilesAdapter.clearAdPositions();
			mHomeMediaTilesAdapter.postAdForPosition();
		}
	}

	public void loadMoreResults(int start, int length,
			MediaCategoryType mediaCategoryType,
			CommunicationOperationListener context) {
		// mDataManager.getMediaItems(mCurrentMediaContentType,
		// MediaCategoryType.LATEST, null, context);

		String timestamp_cache = null;
		// if (mediaContentType == MediaContentType.MUSIC) {
		// if(mediaCategoryType == MediaCategoryType.LATEST)
		timestamp_cache = mDataManager.getApplicationConfigurations()
				.getVideoLatestTimeStamp();
		// else
		// timestamp_cache=
		// mDataManager.getApplicationConfigurations().getMusicPopularTimeStamp();
		// }

		mDataManager.getMediaItemsPaging(mediaContentType, mediaCategoryType,
				null, String.valueOf(start), String.valueOf(length), context,
				timestamp_cache);
	}

	private PromoUnit mPromoUnit;

	public void setPromoUnit(PromoUnit mPromoUnit) {
		this.mPromoUnit = mPromoUnit;
		if (getActivity() != null && mPromoUnit != null
				&& (((HomeActivity) getActivity()).isPromoUnit()))
			setMediaItems(mediaItems);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mHomeMediaTilesAdapter != null) {
			mHomeMediaTilesAdapter.releaseLoadingImages();
		}

		if (rootView != null) {
			try {
				int version = Integer.parseInt(""
						+ android.os.Build.VERSION.SDK_INT);
				Utils.unbindDrawables(rootView, version);
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}
	}
}
