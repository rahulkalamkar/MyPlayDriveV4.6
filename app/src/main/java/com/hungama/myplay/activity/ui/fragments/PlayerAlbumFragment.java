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
import android.widget.Button;
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
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaTrackDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.ui.adapters.PlayerAlbumSimilarAdapter;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.HorizontalDividerItemDecoration;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Shows related tracks to the given playing {@link Track}.
 */
public class PlayerAlbumFragment extends Fragment implements
		CommunicationOperationListener {

	private static final String TAG = "PlayerAlbumFragment";
	// public static final String FRAGMENT_ARGUMENT_TRACK =
	// "fragment_argument_track";
	private DataManager mDataManager;
	private MediaTrackDetails mCurrentTrackDetails;
	private List<MediaItem> mMediaItems = null;

	private RecyclerView mTilesGridView;
	private ProgressBar mProgressBar;
	private LinearLayout mProgressBarLoadMore;
	// private MediaItemsResponse mediaItemsResponse;
	private int mTileSize = 0;
	CampaignsManager mCampaignsManager;
	private Placement placement;

	private View rootView;

	private PlayerAlbumSimilarAdapter mHomeMediaTilesAdapter;

	private OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener;

	private String mflurrySubSectionDescription;
	private Track mTrack = null;

	// ======================================================
	// PABLIC.
	// ======================================================

	// private boolean mIsThrottling = false;

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
		if (data != null
				&& data.containsKey(PlayerInfoFragment.FRAGMENT_ARGUMENT_MEDIA_TRACK_DETAILS)) {
			mCurrentTrackDetails = (MediaTrackDetails) data
					.getSerializable(PlayerInfoFragment.FRAGMENT_ARGUMENT_MEDIA_TRACK_DETAILS);
			mTrack = (Track) data
					.getSerializable(PlayerTriviaFragment.FRAGMENT_ARGUMENT_TRACK);
		}

		if (data != null
				&& data.containsKey(MediaTileGridFragment.FLURRY_SUB_SECTION_DESCRIPTION)) {
			mflurrySubSectionDescription = data
					.getString(MediaTileGridFragment.FLURRY_SUB_SECTION_DESCRIPTION);
		}
		Analytics.postCrashlitycsLog(getActivity(), PlayerAlbumFragment.class.getName());
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
				.getPlacementOfType(PlacementType.PLAYER_ALBUM_BANNER);

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
		mTilesGridView
				.addItemDecoration(new HorizontalDividerItemDecoration.Builder(
						getActivity())
						.color(getResources().getColor(
								R.color.divider_listview_color_similar_album))
						.size(getResources().getDimensionPixelSize(
								R.dimen.media_details_seperetor_height))
						.build());
		mTilesGridView.setHasFixedSize(true);
		mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar1);
		mProgressBarLoadMore = (LinearLayout) rootView
				.findViewById(R.id.progressBarLayout);

		// mTilesGridView = new GridView(getActivity());

		// sets the gird's properties.

		LanguageTextView mTextTitle = (LanguageTextView) rootView
				.findViewById(R.id.player_lyrics_title_bar_text);
		if (mTrack != null)
			mTextTitle.setText(mTrack.getTitle());

		LanguageTextView mTextSubTitle = (LanguageTextView) rootView
				.findViewById(R.id.player_lyrics_sub_title_bar_text);
		mTextSubTitle.setText(Utils.getMultilanguageText(getActivity(),
				getString(R.string.player_more_menu_album)));

		ImageView ivDownArrow = (ImageView) rootView
				.findViewById(R.id.ivDownArrow);
		ivDownArrow.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				// if(mDrawerActionAlbum!=null)
				// mDrawerActionAlbum.performClick();
				// else
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

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			mTilesGridView.setOverScrollMode(GridView.OVER_SCROLL_NEVER);
		}

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
				null, null, mCampaignsManager, null, true,
				mflurrySubSectionDescription);
		mHomeMediaTilesAdapter
				.setOnMusicItemOptionSelectedListener(mOnMediaItemOptionSelectedListener);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(
				getActivity());
		mTilesGridView.setLayoutManager(mLayoutManager);
		mTilesGridView.setAdapter(mHomeMediaTilesAdapter);
	}

	private CacheStateReceiver cacheStateReceiver;

	class CacheStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if (arg1.getAction()
					.equals(CacheManager.ACTION_CACHE_STATE_UPDATED)
					|| arg1.getAction()
							.equals(CacheManager.ACTION_TRACK_CACHED)) {
				if (mHomeMediaTilesAdapter != null) {
					mHomeMediaTilesAdapter.notifyDataSetChanged();
				}
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

	@Override
	public void onStart() {
		super.onStart();

		if (Utils.isListEmpty(mMediaItems) && mCurrentTrackDetails != null) {

			mDataManager.getAlbumDetails(mCurrentTrackDetails, null, this,
					false);

		}
	}

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
		if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
			mProgressBar.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
				MediaItem mediaItem = (MediaItem) responseObjects
						.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_ITEM);

				MediaSetDetails setDetails = (MediaSetDetails) responseObjects
						.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);
				List<Track> tracksItemsList = setDetails.getTracks();
				List<MediaItem> mediaItems = new ArrayList<MediaItem>();
				for (Track t : tracksItemsList) {
					MediaItem mediaItemTemp = new MediaItem(t.getId(),
							t.getTitle(), t.getAlbumName(), t.getArtistName(),
							t.getImageUrl(), t.getBigImageUrl(),
							MediaType.TRACK.toString(), 0, 0, t.getImages(),
							t.getAlbumId());
					mediaItemTemp.setMediaContentType(MediaContentType.MUSIC);
					mediaItems.add(mediaItemTemp);
				}

				if (!Utils.isListEmpty(mediaItems) && mediaItems.size() > 0) {
					// ((RelativeLayout)
					// rootView.findViewById(R.id.rlMainSimilar))
					// .setBackgroundColor(getResources().getColor(
					// R.color.application_background_grey));
					mMediaItems = new ArrayList<MediaItem>();
					mHomeMediaTilesAdapter.setMediaItems(mMediaItems);
					if (placement != null) {
						for (int i = 4; i < mediaItems.size(); i += 5) {
							Logger.i("Hint", String.valueOf(i));
							mediaItems.add(i, new MediaItem(i, "no", "no",
									"no", null, null, "album", 0, 0));
						}
					}
					mMediaItems.addAll(mediaItems);
					refreshAdapter();
					mProgressBar.setVisibility(View.GONE);
				} else {
					Utils.makeText(getActivity(),
							getString(R.string.main_player_bar_no_album_found),
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
		if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
			mProgressBar.setVisibility(View.GONE);
			mProgressBarLoadMore.setVisibility(View.GONE);
		}
	}

	private void refreshAdapter() {
		if (isVisible()) {
			mHomeMediaTilesAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onDestroyView() {
//		System.gc();
//		System.runFinalization();
		super.onDestroyView();
	}

	Button mDrawerActionAlbum;

	public void setAlbumButton(Button mDrawerActionAlbum) {
		this.mDrawerActionAlbum = mDrawerActionAlbum;
	}
}
