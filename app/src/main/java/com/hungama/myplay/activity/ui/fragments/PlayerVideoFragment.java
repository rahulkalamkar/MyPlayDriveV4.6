package com.hungama.myplay.activity.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaTrackDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.RelatedVideoOperation;
import com.hungama.myplay.activity.ui.adapters.MediaTilesAdapterVideo;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.FileCache;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.hungama.myplay.activity.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Shows related videos to the given playing {@link Track}.
 */
public class PlayerVideoFragment extends Fragment implements
		CommunicationOperationListener {

	private static final String TAG = "PlayerVideoFragment";

	public static final String FRAGMENT_ARGUMENT_TRACK_DETAILS = "fragment_argument_track_details";
	
	private DataManager mDataManager;
	public List<MediaItem> mMediaItems = null;

	private MediaTrackDetails mMediaTrackDetails;

	private RecyclerView mTilesGridView;
	private ProgressBar mProgressBar;
	private int mTileSize = 0;

	private MediaTilesAdapterVideo mHomeMediaTilesAdapter;

	private OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener;

	private String mFlurrySubSectionDescription;

	private String backgroundLink;
	private static BitmapDrawable backgroundImage;
	private FileCache fileCache;
	private static Handler h;
	private View rootView;
	private Placement placement;
	private CampaignsManager mCampaignsManager;

	private int width;

	private int dpi;
	// ======================================================
	// PABLIC.
	// ======================================================

	private boolean isMusicLoaded;

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

	private boolean isNeedToChangeTextColor=true;

	public void setIsNeedToChangeTextColor(boolean isNeedToChangeTextColor) {
		this.isNeedToChangeTextColor = isNeedToChangeTextColor;
	}

	// ======================================================
	// FRAGMENT'S LIFE CYCLE.
	// ======================================================
	private Track mTrack = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());

		// gets the track to load.
		Bundle data = getArguments();
		if (data != null && data.containsKey(FRAGMENT_ARGUMENT_TRACK_DETAILS)) {
			mMediaTrackDetails = (MediaTrackDetails) data
					.getSerializable(FRAGMENT_ARGUMENT_TRACK_DETAILS);
			mTrack = (Track) data
					.getSerializable(PlayerTriviaFragment.FRAGMENT_ARGUMENT_TRACK);
		}
		if (data != null
				&& data.containsKey(MediaTileGridFragment.FLURRY_SUB_SECTION_DESCRIPTION)) {
			mFlurrySubSectionDescription = data
					.getString(MediaTileGridFragment.FLURRY_SUB_SECTION_DESCRIPTION);
		}
		Analytics.postCrashlitycsLog(getActivity(), PlayerVideoFragment.class.getName());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Fetch the root view
		rootView = inflater.inflate(R.layout.fragment_player_video, container,
				false);
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(getActivity());
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(rootView, getActivity());
		}
		mCampaignsManager = CampaignsManager.getInstance(getActivity());
		placement = mCampaignsManager
				.getPlacementOfType(PlacementType.PLAYER_VIDEOS_BANNER);
		//
		// if(placement != null){
		// DisplayMetrics metrics = new DisplayMetrics();
		//
		// fileCache = new FileCache(getActivity());
		// getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		// width = metrics.widthPixels;
		// dpi = metrics.densityDpi;
		// Log.i("Width", String.valueOf(width));
		// Log.i("Width", "Dpi " + String.valueOf(dpi));
		// switch(dpi){
		// case DisplayMetrics.DENSITY_LOW:
		// backgroundLink = placement.getDisplayInfoLdpi();
		// break;
		// case DisplayMetrics.DENSITY_MEDIUM:
		// backgroundLink = placement.getDisplayInfoMdpi();
		// break;
		// case DisplayMetrics.DENSITY_HIGH:
		// backgroundLink = placement.getDisplayInfoHdpi();
		// break;
		// case DisplayMetrics.DENSITY_XHIGH:
		// backgroundLink = placement.getDisplayInfoXdpi();
		// break;
		// }
		// if(backgroundLink != null){
		// Log.i("AdURL", placement.getDisplayInfoLdpi());
		// new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// if(backgroundImage == null) {
		// backgroundImage = getBitmap(backgroundLink);
		// }
		// h.sendEmptyMessage(0);
		// }
		// }).start();
		// }
		// h = new Handler() {
		// @SuppressWarnings("deprecation")
		// public void handleMessage(android.os.Message msg) {
		// initializeTiles();
		// }
		// };
		// }
		initializeTiles();

		return rootView;
	}

	private void initializeTiles() {
		int imageTileSpacing = getResources().getDimensionPixelSize(
				R.dimen.home_tiles_spacing_vertical);

		mTilesGridView = (RecyclerView) rootView.findViewById(R.id.gridView);
		mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar1);
		// mTilesGridView = new GridView(getActivity());

		// sets the gird's properties.
		// mTilesGridView.setGravity(Gravity.CENTER_HORIZONTAL);
		// mTilesGridView.setVerticalSpacing(imageTileSpacing);
		// mTilesGridView.setNumColumns(GridView.AUTO_FIT);
		// mTilesGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);

		ImageView iv_close = (ImageView) rootView
				.findViewById(R.id.ivDownArrow);
		iv_close.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getActivity().onBackPressed();
			}
		});

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

		ImageView mButtonShare = (ImageView) rootView
				.findViewById(R.id.player_lyrics_title_bar_button_share);
		mButtonShare.setVisibility(View.GONE);

		LanguageTextView mTextTitle = (LanguageTextView) rootView
				.findViewById(R.id.player_lyrics_title_bar_text);

		mTextTitle.setText(mTrack.getTitle());

		LanguageTextView mTextSubTitle = (LanguageTextView) rootView
				.findViewById(R.id.player_lyrics_sub_title_bar_text);
		mTextSubTitle
				.setText(Utils
						.getMultilanguageText(
								getActivity(),
								getString(R.string.search_results_layout_bottom_text_for_video)));

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
		// mTilesGridView.setPadding(imageTileSpacing, imageTileSpacing,
		// imageTileSpacing, imageTileSpacing);

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
		// mTilesGridView.setNumColumns(2);
		// mTilesGridView.setColumnWidth(mTileSize);
		// mTilesGridView.setNumColumns(1);
		// mTilesGridView.setColumnWidth(screenWidth);
		mTilesGridView.setOnScrollListener(new RecyclerView.OnScrollListener() {

			@Override
			public void onScrollStateChanged(RecyclerView view, int scrollState) {
				if (scrollState == RecyclerView.SCROLL_STATE_IDLE) {
					PicassoUtil.with(getActivity()).resumeTag();
					try {
						mHomeMediaTilesAdapter.postAdForPosition();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					PicassoUtil.with(getActivity()).pauseTag();
				}
			}
		});

		mHomeMediaTilesAdapter = new MediaTilesAdapterVideo(getActivity(),
				mTilesGridView, mTileSize, this.getClass().getCanonicalName(),
				null, null, mCampaignsManager, null, false,
				mFlurrySubSectionDescription);
		mHomeMediaTilesAdapter.setEditModeEnabled(false);
		mHomeMediaTilesAdapter.setTrendVisibility(true);
		mHomeMediaTilesAdapter.setIsNeedToChangeTextColor(isNeedToChangeTextColor);
		mHomeMediaTilesAdapter.setShowDetailsInOptionsDialogEnabled(true);
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
		try {
			if (Utils.isListEmpty(mMediaItems)) {

				MediaItem mediaItem = new MediaItem(mMediaTrackDetails.getId(),
						mMediaTrackDetails.getTitle(),
						mMediaTrackDetails.getAlbumName(),
						mMediaTrackDetails.getAlbumName(), null, null,
						MediaType.TRACK.toString().toLowerCase(), 0,
						mMediaTrackDetails.getAlbumId());
				mediaItem.setMediaContentType(MediaContentType.VIDEO);
				mDataManager.getRelatedVideo(mMediaTrackDetails, mediaItem,
						this);
			}
		} catch (Exception e) {
			Logger.e(getClass().getName() + ":235", e.toString());
		}
	}

	@Override
	public void onStop() {
		super.onStop();

		// cancels loading in the background the similar media items.
		mDataManager.cancelGetMediaDetails();
		if (mProgressBar != null
				&& mProgressBar.getVisibility() == View.VISIBLE) {
			mProgressBar.setVisibility(View.GONE);
		}
	}

	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_RELATED) {
			mProgressBar.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_RELATED) {
				mMediaItems = (List<MediaItem>) responseObjects
						.get(RelatedVideoOperation.RESPONSE_KEY_RELATED_VIDEO);
				if (placement != null && !isMusicLoaded) {
					for (int i = 3; i < mMediaItems.size(); i += 6) {
						Logger.i("Hint", String.valueOf(i));
						isMusicLoaded = true;
						MediaItem temp = new MediaItem(i, "no", "no", "no",
								backgroundLink, backgroundLink, "track", 0, 0);
						temp.setMediaContentType(MediaContentType.VIDEO);
						mMediaItems.add(i, temp);
					}
				} else {
					isMusicLoaded = false;
				}
				if (!Utils.isListEmpty(mMediaItems)) {
					List<Object> temp = new ArrayList<Object>();
					temp.addAll(mMediaItems);
					mHomeMediaTilesAdapter.setMediaItems(temp);
					mHomeMediaTilesAdapter.notifyDataSetChanged();
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
		if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_RELATED) {
			mProgressBar.setVisibility(View.GONE);
		}
	}

	@Override
	public void onDestroyView() {
//		System.gc();
//		System.runFinalization();
		super.onDestroyView();
	}

	public void postAd() {
		if (mHomeMediaTilesAdapter != null)
			mHomeMediaTilesAdapter.postAdForPosition();
	}

	private CacheStateReceiver cacheStateReceiver;

	@Override
	public void onResume() {
		super.onResume();
		if (cacheStateReceiver == null) {
			cacheStateReceiver = new CacheStateReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(CacheManager.ACTION_VIDEO_CACHE_STATE_UPDATED);
			filter.addAction(CacheManager.ACTION_VIDEO_TRACK_CACHED);
			filter.addAction(CacheManager.ACTION_VIDEO_UPDATED_CACHE);
			getActivity().registerReceiver(cacheStateReceiver, filter);
		}
	}

	@Override
	public void onDestroy() {
		if (cacheStateReceiver != null) {
			getActivity().unregisterReceiver(cacheStateReceiver);
		}
		super.onDestroy();
	}

	private class CacheStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			Logger.s("========================= cachestateupdatereceived ========"
					+ arg1.getAction());
			if (arg1.getAction()
					.equals(CacheManager.ACTION_CACHE_STATE_UPDATED)
					|| arg1.getAction()
							.equals(CacheManager.ACTION_TRACK_CACHED)) {
			} else if (arg1.getAction().equals(
					CacheManager.ACTION_UPDATED_CACHE)) {
			} else if (arg1.getAction().equals(
					CacheManager.ACTION_VIDEO_CACHE_STATE_UPDATED)
					|| arg1.getAction().equals(
							CacheManager.ACTION_VIDEO_TRACK_CACHED)) {
				try {
					if (mHomeMediaTilesAdapter != null) {
						mHomeMediaTilesAdapter.notifyDataSetChanged();

					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else if (arg1.getAction().equals(
					CacheManager.ACTION_VIDEO_UPDATED_CACHE)) {
			}
		}
	}
}
