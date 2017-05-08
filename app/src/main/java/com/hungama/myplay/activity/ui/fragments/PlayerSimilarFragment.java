package com.hungama.myplay.activity.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.campaigns.PlacementType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaItemsResponse;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MediaContentOperationPaging;
import com.hungama.myplay.activity.operations.hungama.TrackSimilarOperation;
import com.hungama.myplay.activity.ui.adapters.PlayerAlbumSimilarAdapter;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.HorizontalDividerItemDecoration;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Shows related tracks to the given playing {@link Track}.
 */
public class PlayerSimilarFragment extends Fragment implements
		CommunicationOperationListener {

	private static final String TAG = "PlayerSimilarFragment";

	public static final String FRAGMENT_ARGUMENT_TRACK = "fragment_argument_track";

	private DataManager mDataManager;
	private Track mTrack;
	private List<MediaItem> mMediaItems = null;

	private RecyclerView mTilesGridView;
	private ProgressBar mProgressBar;
	private LinearLayout mProgressBarLoadMore;
	private MediaItemsResponse mediaItemsResponse;
	private int itemsInList = Constants.LOADING_CHUNK_NUMBER;
	private int mTileSize = 0;
	CampaignsManager mCampaignsManager;

	private String backgroundLink;
	private View rootView;
	private Placement placement;

	private PlayerAlbumSimilarAdapter mHomeMediaTilesAdapter;

	private OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener;

	private String mflurrySubSectionDescription;
	// ======================================================
	// PABLIC.
	// ======================================================

	private boolean mIsThrottling = false;

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

	// ======================================================
	// FRAGMENT'S LIFE CYCLE.
	// ======================================================

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());

		// gets the track to load.
		Bundle data = getArguments();
		if (data != null && data.containsKey(FRAGMENT_ARGUMENT_TRACK)) {
			mTrack = (Track) data.getSerializable(FRAGMENT_ARGUMENT_TRACK);
		}

		if (data != null
				&& data.containsKey(MediaTileGridFragment.FLURRY_SUB_SECTION_DESCRIPTION)) {
			mflurrySubSectionDescription = data
					.getString(MediaTileGridFragment.FLURRY_SUB_SECTION_DESCRIPTION);
		}
		Analytics.postCrashlitycsLog(getActivity(), PlayerSimilarFragment.class.getName());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Fetch the root view
		rootView = inflater.inflate(R.layout.fragment_player_album_similar,
				container, false);
		// ((RelativeLayout) rootView.findViewById(R.id.rlMainSimilar))
		// .setBackgroundColor(Color.TRANSPARENT);
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(getActivity());
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(rootView, getActivity());
		}
		mCampaignsManager = CampaignsManager.getInstance(getActivity());
		placement = mCampaignsManager
				.getPlacementOfType(PlacementType.PLAYER_SIMILAR_BANNER);

		initializeTiles();

		return rootView;
	}

	private CacheStateReceiver cacheStateReceiver;

	class CacheStateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if (arg1.getAction()
					.equals(CacheManager.ACTION_CACHE_STATE_UPDATED)
					|| arg1.getAction()
							.equals(CacheManager.ACTION_TRACK_CACHED))
				if (mHomeMediaTilesAdapter != null)
					mHomeMediaTilesAdapter.notifyDataSetChanged();
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
			// filter.addAction(CacheManager.ACTION_VIDEO_CACHE_STATE_UPDATED);
			// filter.addAction(CacheManager.ACTION_VIDEO_TRACK_CACHED);
			// filter.addAction(CacheManager.ACTION_VIDEO_UPDATED_CACHE);
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

	private void initializeTiles() {
		int imageTileSpacing = getResources().getDimensionPixelSize(
				R.dimen.home_tiles_spacing_vertical);
		mTilesGridView = (RecyclerView) rootView.findViewById(R.id.gridView);
		mTilesGridView
				.addItemDecoration(new HorizontalDividerItemDecoration.Builder(
						getActivity())
						.color(getResources().getColor(
								R.color.divider_listview_color_similar_album))
						.size(getResources().getDimensionPixelSize(
								R.dimen.media_details_seperetor_height))
						.build());
		mTilesGridView.setHasFixedSize(false);
		mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar1);
		mProgressBarLoadMore = (LinearLayout) rootView
				.findViewById(R.id.progressBarLayout);

		LanguageTextView mTextTitle = (LanguageTextView) rootView
				.findViewById(R.id.player_lyrics_title_bar_text);
		if (mTrack != null)
			mTextTitle.setText(mTrack.getTitle());

		LanguageTextView mTextSubTitle = (LanguageTextView) rootView
				.findViewById(R.id.player_lyrics_sub_title_bar_text);
		mTextSubTitle.setText(Utils.getMultilanguageText(getActivity(),
				getString(R.string.player_more_menu_similar)));

		ImageView ivDownArrow = (ImageView) rootView
				.findViewById(R.id.ivDownArrow);
		ivDownArrow.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getActivity().onBackPressed();
			}
		});

		rootView.findViewById(R.id.player_lyrics_title_bar_button_share)
				.setVisibility(View.GONE);

		try {
			// RelativeLayout rlMainInfo=(RelativeLayout)
			// rootView.findViewById(R.id.rlMainInfo1);
			if (android.os.Build.VERSION.SDK_INT > 15) {
				// // only for gingerbread and newer
				rootView.setBackground(PlayerBarFragment.blurbitmap);
				// rlFlipView.invalidate();
			} else {
				rootView.setBackgroundDrawable(PlayerBarFragment.blurbitmap);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		mTilesGridView.setOnScrollListener(new RecyclerView.OnScrollListener() {

			@Override
			public void onScrollStateChanged(RecyclerView view, int scrollState) {
				if (scrollState == RecyclerView.SCROLL_STATE_IDLE) {
					processPaging(view);
				}
			}

			void processPaging(RecyclerView recyclerView) {
				LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView
						.getLayoutManager();
				int visibleItemCount = recyclerView.getChildCount();
				int totalItemCount = layoutManager.getItemCount();
				int firstVisibleItem = layoutManager
						.findFirstVisibleItemPosition();
				if (totalItemCount > 0) {
					if (firstVisibleItem + visibleItemCount == totalItemCount
							&& totalItemCount == itemsInList
							&& mMediaItems.size() <= mediaItemsResponse
									.getTotal()
							&& (mMediaItems.size()
									% Constants.LOADING_CHUNK_NUMBER == 0)) {
						Logger.i(TAG, "Loading more results");
						if (getActivity() != null) {
							mProgressBarLoadMore.setVisibility(View.VISIBLE);
							itemsInList = totalItemCount
									+ Constants.LOADING_CHUNK_NUMBER;
							int start = 1 + totalItemCount;
							Logger.i(TAG, "Start = " + start);
							loadMoreResults(start);
						}
					}
				}

			}
		});

		// mTilesGridView = new GridView(getActivity());

		// sets the gird's properties.

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			mTilesGridView.setOverScrollMode(GridView.OVER_SCROLL_NEVER);
		}

		// sets the background.
		// mTilesGridView.setBackgroundColor(getResources().getColor(
		// R.color.application_background_grey));

		// sets the gridview's cool margin.
		// GridView.MarginLayoutParams params =
		// new GridView.MarginLayoutParams(GridView.LayoutParams.MATCH_PARENT,
		// GridView.LayoutParams.MATCH_PARENT);
		// mTilesGridView.setLayoutParams(params);
		// doubling the top + bottom edges with padding to make the tiles fits
		// well inside.
		mTilesGridView.setPadding(imageTileSpacing, imageTileSpacing,
				imageTileSpacing, imageTileSpacing);

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

		Logger.i(TAG, "screenWidth: " + screenWidth + " mTileSize: "
				+ mTileSize);

		mHomeMediaTilesAdapter = new PlayerAlbumSimilarAdapter(getActivity(),
				mTilesGridView, mTileSize, this.getClass().getCanonicalName(),
				null, null, mCampaignsManager, null, false,
				mflurrySubSectionDescription);

		// mHomeMediaTilesAdapter.setEditModeEnabled(false);
		// mHomeMediaTilesAdapter.setShowDetailsInOptionsDialogEnabled(false);
		// mHomeMediaTilesAdapter.setSaveofflineOption(true);
		mHomeMediaTilesAdapter
				.setOnMusicItemOptionSelectedListener(mOnMediaItemOptionSelectedListener);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(
				getActivity());
		mTilesGridView.setLayoutManager(mLayoutManager);
		mTilesGridView.setAdapter(mHomeMediaTilesAdapter);
	}

	@Override
	public void onStart() {
		super.onStart();

		if (Utils.isListEmpty(mMediaItems)) {
			mDataManager.getTrackSimilar(mTrack, String.valueOf(1),
					String.valueOf(Constants.LOADING_CHUNK_NUMBER), this);
		}
	}

	@Override
	public void onStop() {
		super.onStop();

		// cancels loading in the background the similar media items.
		mDataManager.cancelGetTrackSimilar();
		if (mProgressBar != null
				&& mProgressBar.getVisibility() == View.VISIBLE) {
			mProgressBar.setVisibility(View.GONE);
		}
	}

	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.TRACK_SIMILAR) {

			if (mIsThrottling) {
				mProgressBarLoadMore.setVisibility(View.VISIBLE);
			} else {
				mProgressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			if (operationId == OperationDefinition.Hungama.OperationId.TRACK_SIMILAR) {

				List<MediaItem> mediaItems = (List<MediaItem>) responseObjects
						.get(TrackSimilarOperation.RESULT_KEY_OBJECT_MEDIA_ITEMS);

				mediaItemsResponse = (MediaItemsResponse) responseObjects
						.get(MediaContentOperationPaging.RESPONSE_KEY_OBJECT_MEDIA_ITEMS_RESPONSE);
				if (!Utils.isListEmpty(mediaItems) && mediaItems.size() > 0) {
					// ((RelativeLayout)
					// rootView.findViewById(R.id.rlMainSimilar))
					// .setBackgroundColor(getResources().getColor(
					// R.color.application_background_grey));
					if (mIsThrottling) {
						mIsThrottling = false;
						// mMediaItems = new ArrayList<MediaItem>();
						if (placement != null) {
							for (int i = 4; i < mediaItems.size(); i += 5) {
								Logger.i("Hint", String.valueOf(i));
								mediaItems.add(i, new MediaItem(i, "no", "no",
										"no", backgroundLink, backgroundLink,
										"album", 0, 0));
							}
						}
						mMediaItems.addAll(mediaItems);
						mHomeMediaTilesAdapter.setMediaItems(mMediaItems);
						Logger.i("", "mMediaItems:" + mMediaItems.size()
								+ " onSuccess:" + mIsThrottling);
						refreshAdapter();
						mProgressBarLoadMore.setVisibility(View.GONE);
					} else {
						mMediaItems = new ArrayList<MediaItem>();
						mHomeMediaTilesAdapter.setMediaItems(mMediaItems);
						if (placement != null) {
							for (int i = 4; i < mediaItems.size(); i += 5) {
								Logger.i("Hint", String.valueOf(i));
								mediaItems.add(i, new MediaItem(i, "no", "no",
										"no", backgroundLink, backgroundLink,
										"album", 0, 0));
							}
						}
						mMediaItems.addAll(mediaItems);
						Logger.i("", "mMediaItems:" + mMediaItems.size()
								+ " onSuccess:" + mIsThrottling);
						refreshAdapter();
						mProgressBar.setVisibility(View.GONE);
					}
				} else {
					Utils.makeText(
							getActivity(),
							getString(R.string.main_player_bar_no_similar_found),
							0).show();
					getActivity().onBackPressed();
				}
				mProgressBar.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		if (operationId == OperationDefinition.Hungama.OperationId.TRACK_SIMILAR) {
			mProgressBar.setVisibility(View.GONE);
			mProgressBarLoadMore.setVisibility(View.GONE);
		}
	}

	private void refreshAdapter() {
		if (isVisible()) {
			mHomeMediaTilesAdapter.notifyDataSetChanged();
		}
	}

	private void loadMoreResults(int start) {
		mIsThrottling = true;
		mDataManager.getTrackSimilar(mTrack, String.valueOf(start),
				String.valueOf(Constants.LOADING_CHUNK_NUMBER), this);
	}

	@Override
	public void onDestroyView() {
//		System.gc();
//		System.runFinalization();
		super.onDestroyView();
	}

	// private LanguageButton mDrawerActionSimilar;

	// public void setSimilarButton(LanguageButton mDrawerActionSimilar) {
	// this.mDrawerActionSimilar = mDrawerActionSimilar;
	// }

	@Override
	public void onDestroy() {
		super.onDestroy();

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
