package com.hungama.myplay.activity.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.campaigns.PlacementType;
import com.hungama.myplay.activity.data.dao.hungama.LiveStation;
import com.hungama.myplay.activity.data.dao.hungama.LiveStationDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MultiSongDetailOperation;
import com.hungama.myplay.activity.operations.hungama.RadioTopArtistSongsOperation;
import com.hungama.myplay.activity.player.PlayMode;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.UpgradeActivity;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment.LiveRadioUpdateListener;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment.NextTrackUpdateListener;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.FileCache;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.hungama.myplay.activity.util.Utils;
import com.hungama.myplay.activity.util.images.ImageCache;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Shows details related to the selected Radio station.
 */
public class RadioDetailsFragment extends MainFragment implements
		NextTrackUpdateListener, OnClickListener,
		CommunicationOperationListener, LiveRadioUpdateListener {

	public static final String TAG = "RadioDetailsFragment";

	/**
	 * Extra data of the Radio item.
	 */
	public static final String EXTRA_MEDIA_ITEM = "extra_media_item";
	/**
	 * Extra data of the given Radio item kind, if it's Live Station or Top
	 * Artist Radio.
	 */
	public static final String EXTRA_CATEGORY_TYPE = "extra_category_type";

	/**
	 * Flag for indication if the fragment should present a title bar with the
	 * name of the station. Default is false.
	 */
	public static final String EXTRA_DO_SHOW_TITLE_BAR = "extra_do_show_title_bar";

	/**
	 * Flag indicates if when this fragment is launched it will also play the
	 * given radio channel, default is false.
	 */
	public static final String EXTRA_AUTO_PLAY = "extra_auto_play";

	public static final String EXTRA_COMING_UP_NEXT = "extra_coming_up_next";

	public static final String IS_FOR_PLAYER_BAR = "is_for_player_bar";

	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	private Placement radioPlacement;
	// private ImageFetcher mImageFetcher = null;

	private MediaItem mMediaItem = null;
	private MediaCategoryType mMediaCategoryType = null;
	private boolean mDoShowTitleBar = false;
	private boolean mAutoPlay = false;

	// general views.
	private LanguageTextView mTextTitle;

	private RelativeLayout mComingUpLayout;
	private ImageView mComingUpThumbnail;
	private ImageView mRadioPlacementImage;
	private LanguageTextView mComingUpAlbumName;
	private LanguageTextView mComingUpSongName;

	private String backgroundLink;
	private BitmapDrawable backgroundImage;
	private FileCache fileCache;
	// private static Handler h;
	private View rootView;
	private Placement placement;

	private int width;

	private int dpi;

	private Context mContext;
	private PlayerBarFragment playerBarFragment;
	private String txtComingUpNext, txtNowPlaying, txtNoAlbum;

	// ======================================================
	// Life Cycle callbacks.
	// ======================================================

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity().getApplicationContext();
		playerBarFragment = ((MainActivity) getActivity()).getPlayerBar();
		Bundle data = getArguments();
		updateInfoDetails(data, true);
	}

	public void updateInfoDetails(Bundle data, boolean firstTime) {
		if (data != null) {
			mMediaItem = (MediaItem) data.getSerializable(EXTRA_MEDIA_ITEM);
			mMediaCategoryType = (MediaCategoryType) data
					.getSerializable(EXTRA_CATEGORY_TYPE);
			mDoShowTitleBar = data.getBoolean(EXTRA_DO_SHOW_TITLE_BAR, false);
			mAutoPlay = data.getBoolean(EXTRA_AUTO_PLAY, false);
		}
		if (mMediaItem instanceof LiveStation) {
			mMediaCategoryType = MediaCategoryType.LIVE_STATIONS;
		} else if (mMediaCategoryType != MediaCategoryType.LIVE_STATIONS) {
			mMediaCategoryType = MediaCategoryType.TOP_ARTISTS_RADIO;
		}
		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();

		Set<String> tags = Utils.getTags();

		tags.add("radio_used");
		Utils.AddTag(tags);
		setRadioDiscriptions();
		if(ivClose!=null)
			ivClose.setVisibility(View.INVISIBLE);
		isFlip = false;
		stopAdsFlipTimer();
//		System.out.println("getAppConfigPlayerOverlayStart ::- " + mApplicationConfigurations
//				.getAppConfigPlayerOverlayStart());
//		System.out.println("getAppConfigPlayerOverlayRefresh ::- " + mApplicationConfigurations
//				.getAppConfigPlayerOverlayRefresh());
//		System.out.println("getAppConfigPlayerOverlayFlipBackDuration ::- " + mApplicationConfigurations
//				.getAppConfigPlayerOverlayFlipBackDuration());
		if (!firstTime)
			playResume();
	}

	private void setRadioDiscriptions() {
		try {
			LanguageTextView tvDescription = (LanguageTextView) rootView
					.findViewById(R.id.radio_details_live_station_text_radio_name);
			tvDescription.setVisibility(View.GONE);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// try {
		//
		// if (mMediaItem instanceof LiveStation) {
		// LiveStation liveStation = (LiveStation) mMediaItem;
		// String descriptions = liveStation.getDescription();
		// if (descriptions != null && !descriptions.equals("")) {
		// tvDescription.setText(Utils.getMultilanguageTextLayOut(
		// mContext, descriptions));
		// tvDescription.setVisibility(View.VISIBLE);
		// } else {
		// tvDescription.setVisibility(View.GONE);
		// }
		// } else if (mMediaCategoryType != MediaCategoryType.LIVE_STATIONS) {
		// tvDescription.setVisibility(View.GONE);
		// } else {
		// tvDescription.setVisibility(View.GONE);
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_radio_details, container,
				false);
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(rootView, getActivity());
		}
		initializeUserControls(rootView);
		txtComingUpNext = Utils.getMultilanguageText(getActivity(),
				getResources()
						.getString(R.string.radio_details_coming_up_label));
		txtNowPlaying = Utils.getMultilanguageText(getActivity(),
				getResources()
						.getString(R.string.player_queue_text_now_playing));
		txtNoAlbum = "";
		// Utils.getMultilanguageText(getActivity(), getResources()
		// .getString(R.string.full_player_text_no_album));
		return rootView;
	}

	public void setTransparentBg() {

		// Utils.clearCache();
		// RelativeLayout rlRadioDetailParent = (RelativeLayout) rootView
		// .findViewById(R.id.rlRadioDetailParent);
		// rlRadioDetailParent.setBackgroundColor(Color.TRANSPARENT);
		// LanguageTextView textArtistName = (LanguageTextView) rootView
		// .findViewById(R.id.radio_details_top_artists_text_radio_name);
		// textArtistName.setTextColor(Color.WHITE);
		// mComingUpAlbumName = (LanguageTextView) rootView
		// .findViewById(R.id.radio_details_coming_up_album_name);
		// mComingUpAlbumName.setTextColor(Color.WHITE);
		// mComingUpSongName = (LanguageTextView) rootView
		// .findViewById(R.id.radio_details_coming_up_song_name);
		// mComingUpSongName.setTextColor(Color.WHITE);
		// LanguageTextView description = (LanguageTextView) rootView
		// .findViewById(R.id.radio_details_live_station_text_radio_name);
		// description.setTextColor(Color.WHITE);
		// // RelativeLayout container = (RelativeLayout) rootView
		// // .findViewById(R.id.radio_details_layout_top_artists_radio);
		// // container.setBackgroundColor(getResources().getColor(
		// // R.color.main_player_content_buttons_background_trans));
		// mComingUpLayout = (RelativeLayout) rootView
		// .findViewById(R.id.radio_details_layout_coming_up);
		// mComingUpLayout.setBackgroundColor(getResources().getColor(
		// R.color.main_player_content_buttons_background_trans));
		// LinearLayout radio_details_layout = (LinearLayout) rootView
		// .findViewById(R.id.radio_details_layout);
		// radio_details_layout.setBackgroundColor(getResources().getColor(
		// R.color.main_player_content_buttons_background_trans));

	}

	@Override
	public void onStart() {
		try {
			super.onStart();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		// FlurryAgent.setContinueSessionMillis(Constants.FLURRY_SESSION_LENGTH);
		// FlurryAgent.onStartSession(getActivity(),
		// getString(R.string.flurry_app_key));
//		Analytics.startSession(getActivity(), this);
		Analytics.onPageView();

		/*
		 * Checks if it should play it for the first time launching the
		 * fragment.
		 */
		if (mAutoPlay) {
			// disables it to avoid replaying when coming from the background.
			mAutoPlay = false;
			try {
				if (mMediaCategoryType == MediaCategoryType.TOP_ARTISTS_RADIO) {
					/*
					 * gets the songs for the artist, when it finishes, it will
					 * start playing them and will show the details for it.
					 */
					mDataManager.getRadioTopArtistSongs(mMediaItem, this);

				} else if (mMediaCategoryType == MediaCategoryType.LIVE_STATIONS) {
					playLiveStation(mMediaItem);
				}
			} catch (Exception e) {
				if (mMediaItem instanceof LiveStation) {
					/*
					 * gets the songs for the artist, when it finishes, it will
					 * start playing them and will show the details for it.
					 */
					playLiveStation(mMediaItem);
				} else if (mMediaCategoryType == MediaCategoryType.LIVE_STATIONS) {
					mDataManager.getRadioTopArtistSongs(mMediaItem, this);
				}
			}
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		Analytics.onEndSession(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();
		isPaused = false;
		playResume();
//		if (mMediaCategoryType == MediaCategoryType.TOP_ARTISTS_RADIO) {
//			// if (mImageFetcher == null) {
//			// creates the image loader.
//
//			// creates the cache.
//			ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(
//					getActivity(), DataManager.FOLDER_TILES_CACHE);
//			cacheParams.setMemCacheSizePercent(getActivity(), 0.10f);
//
//			// gets the image and its size.
//			ImageView thumbNail = (ImageView) getView().findViewById(
//					R.id.radio_details_thumbnail);
//			// int imageSize = Math.min(thumbNail.getMeasuredHeight(),
//			// thumbNail.getMeasuredWidth());
//
//			// mImageFetcher = new ImageFetcher(getActivity(), imageSize);
//			// mImageFetcher.addImageCache(getFragmentManager(), cacheParams);
//			// mImageFetcher.setImageFadeIn(false);
//
//			// mImageFetcher.loadImage(mMediaItem.getImageUrl(), thumbNail);
//			Picasso.with(mContext).cancelRequest(thumbNail);
//			// if(mMediaItem!=null){
//			// String imageUrl =
//			// ImagesManager.getMusicArtSmallImageUrl(mMediaItem.getImagesUrlArray());
//			// // String imageUrl = mMediaItem.getImageUrl();
//			// if (mContext != null && mMediaItem != null
//			// && !TextUtils.isEmpty(imageUrl)) {
//			// Picasso.with(mContext).load(imageUrl).fit()
//			// .centerInside().into(thumbNail);
//			// }
//			// }
//
//			displayTileImage(mRadioPlacementImage, mMediaItem);
//			// coming up
//			playerBarFragment.registerToNextTrackUpdateListener(this);
//			displayUpcomingRadio();
//			// populateComingUpPanel(playerBarFragment.getNextTrack());
//
//			// } else {
//			// // refreshes the cache of the image.
//			// mImageFetcher.setExitTasksEarly(false);
//			// }
//		} else {
//			// mRadioPlacementImage
//			// .setBackgroundDrawable(backgroundImage);
//
//			displayTileImage(mRadioPlacementImage, mMediaItem);
//
//			playerBarFragment.registerLiveRadioUpdateListener(this);
//			// if (storedComingUpTrackDetail != null)
//			// update(storedComingUpTrackDetail);
//			// else {
//			// Bundle data = getArguments();
//			// if (data != null) {
//			// LiveStationDetails tempDetail = (LiveStationDetails) data
//			// .getSerializable(EXTRA_COMING_UP_NEXT);
//			// if (tempDetail != null) {
//			// update(tempDetail);
//			// }
//			// }
//			// }
//			displayUpcomingLiveRadio();
//			if (isbackFromUpgrade) {
//				isbackFromUpgrade = false;
//				isUpgrading = false;
//			}
//		}
//		// initializeAds(true);
//		final int adRefreshInterval = mApplicationConfigurations
//				.getAppConfigPlayerOverlayStart();// getAdRefreshInterval();
//		adhandler.removeCallbacks(refreshAd);
//		adhandler.postDelayed(refreshAd, adRefreshInterval * 1000);
//
//		/*
//		 * Loads the image for the campaigns.
//		 */
//		// ImageCache.ImageCacheParams cacheParams =
//		// new ImageCache.ImageCacheParams(getActivity(),
//		// DataManager.FOLDER_CAMPAIGNS_CACHE);
//		// cacheParams.setMemCacheSizePercent(getActivity(), 0.10f);
//		//
//		// mImageFetcher = new ImageFetcher(getActivity(), 0);
//		// mImageFetcher.addImageCache(getFragmentManager(), cacheParams);
//		// mImageFetcher.setLoadingImage(R.drawable.background_home_tile_album_default);
//		// mImageFetcher.setImageFadeIn(false);
//
//		// Get the Radio Placement List and generate a random number in order to
//		// show one
//		// of the Placements.
//		// if (mMediaCategoryType == MediaCategoryType.TOP_ARTISTS_RADIO){
//		// List<Placement> radioPlacements = mDataManager
//		// .getStoredRadioPlacement();
//		//
//		// if (radioPlacements != null && !radioPlacements.isEmpty()
//		// && backgroundLink == null) {
//		//
//		// Random myRandom = new Random();
//		// int listSize = radioPlacements.size();
//		//
//		// int randomRadioPlacement = (Math.abs(myRandom.nextInt()) %
//		// (listSize));
//		//
//		// radioPlacement = radioPlacements.get(randomRadioPlacement);
//		//
//		// // mImageFetcher.loadImage(radioPlacement.getBgImageSmall(),
//		// // mRadioPlacementImage);
//		// Picasso.with(mContext).cancelRequest(mRadioPlacementImage);
//		// if (mContext != null && radioPlacement != null
//		// && !TextUtils.isEmpty(radioPlacement.getBgImageSmall())) {
//		// Picasso.with(mContext)
//		// .load(radioPlacement.getBgImageSmall())
//		// .placeholder(
//		// R.drawable.background_home_tile_album_default)
//		// .into(mRadioPlacementImage);
//		// }
//		// } else {
//		// if (radioPlacement == null)
//		// mRadioPlacementImage.setClickable(false);
//		// }
//		// }
//
//		// Utils.performclickEventTest(getActivity(), "ua://callback/?code=41");
//		// new Handler().postDelayed(new Runnable() {
//		// @Override
//		// public void run() {
//		// Utils.performclickEventTest(getActivity(), "ua://callback/?code=41");
//		// }
//		// }, 2000);
	}

	private void playResume(){
		if (mMediaCategoryType == MediaCategoryType.TOP_ARTISTS_RADIO) {
			// creates the cache.
			ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(
					getActivity(), DataManager.FOLDER_TILES_CACHE);
			cacheParams.setMemCacheSizePercent(getActivity(), 0.10f);

			// gets the image and its size.
			ImageView thumbNail = (ImageView) getView().findViewById(
					R.id.radio_details_thumbnail);
			Picasso.with(mContext).cancelRequest(thumbNail);

			displayTileImage(mRadioPlacementImage, mMediaItem);
			// coming up
			playerBarFragment.registerToNextTrackUpdateListener(this);
			displayUpcomingRadio();
		} else {
			displayTileImage(mRadioPlacementImage, mMediaItem);

			playerBarFragment.registerLiveRadioUpdateListener(this);
			displayUpcomingLiveRadio();
			if (isbackFromUpgrade) {
				isbackFromUpgrade = false;
				isUpgrading = false;
			}
		}
		// initializeAds(true);
		final int adRefreshInterval = mApplicationConfigurations
				.getAppConfigPlayerOverlayStart();// getAdRefreshInterval();
		adhandler.removeCallbacks(refreshAd);
		adhandler.postDelayed(refreshAd, adRefreshInterval * 1000);
	}

	private void displayUpcomingLiveRadio() {
		try {
			isNowPlayingDisplay = true;
			isFirstTimeLiveRadioCalled = true;
			handlerNextLiveRadio.removeCallbacks(runnableNextLiveRadio);
			PlayerBarFragment playerBarFragment = ((MainActivity) getActivity())
					.getPlayerBar();

			// populateComingUpPanelLiveRadio(playerBarFragment.detail);
			update(playerBarFragment.detail, true);
			handlerNextLiveRadio.postDelayed(runnableNextLiveRadio, 10000);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	private void populateComingUpPanelLiveRadio(LiveStationDetails detail) {

		if (detail != null) {
			boolean updateThumb = false;
			if (storedComingUpTrackDetail == null
					|| (storedComingUpTrackDetail != null && detail != null && storedComingUpTrackDetail
							.getId() != detail.getId())) {
				updateThumb = true;
				try {
					if (mComingUpThumbnail != null)
						mComingUpThumbnail
								.setImageResource(R.drawable.background_home_tile_album_default);
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			}
			LanguageTextView radio_details_coming_up_label = (LanguageTextView) rootView
					.findViewById(R.id.radio_details_coming_up_label);
			radio_details_coming_up_label.setText(txtComingUpNext);
			storedComingUpTrackDetail = detail;
			try {
				if (detail != null && !detail.getTrack().equalsIgnoreCase("no")) {
					if (mComingUpLayout.getVisibility() != View.VISIBLE) {
						mComingUpLayout.setVisibility(View.VISIBLE);
					}

					radio_details_coming_up_label.setText(txtNowPlaying);
					if (!detail.getTrack().equalsIgnoreCase("no"))
						mComingUpSongName.setText(Utils
								.getMultilanguageTextLayOut(mContext,
										detail.getTrack()));

					String songPrefix = Utils.getMultilanguageTextLayOut(
							mContext,
							getResources().getString(
									R.string.radio_details_coming_up_song));
					if (!detail.getAlbum().equalsIgnoreCase("no")) {
						songPrefix = songPrefix + " " + detail.getAlbum();
						mComingUpAlbumName.setText(Utils
								.getMultilanguageTextLayOut(mContext,
										songPrefix));
						if (updateThumb)
							mDataManager.getMultiSongDetail(
									"" + detail.getId(),
									RadioDetailsFragment.this);
					}

				} else {
					mComingUpLayout.setVisibility(View.INVISIBLE);
				}
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}
	}

	Handler handlerNextLiveRadio = new Handler();
	Runnable runnableNextLiveRadio = new Runnable() {

		@Override
		public void run() {
			if (PlayerService.service != null
					&& PlayerService.service.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
				if (isNowPlayingDisplay) {
					try {
						isNowPlayingDisplay = false;
						isFirstTimeLiveRadioCalled = false;
						if (storedComingUpTrackDetail != null)
							update(storedComingUpTrackDetail, false);
						else {
							Bundle data = getArguments();
							if (data != null) {
								LiveStationDetails tempDetail = (LiveStationDetails) data
										.getSerializable(EXTRA_COMING_UP_NEXT);
								if (tempDetail != null) {
									update(tempDetail, false);
								}
							}
						}
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
				} else {
					try {
						isNowPlayingDisplay = true;
						PlayerBarFragment playerBarFragment = ((MainActivity) getActivity())
								.getPlayerBar();
						// populateComingUpPanelLiveRadio(playerBarFragment.detail);
						update(playerBarFragment.detail, true);
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
				}
				handlerNextLiveRadio.postDelayed(runnableNextLiveRadio, 10000);
			} else {
				handlerNextLiveRadio.removeCallbacks(runnableNextLiveRadio);
			}
		}
	};

	boolean isNowPlayingDisplay = false;

	private void displayUpcomingRadio() {
		try {
			isNowPlayingDisplay = true;
			handlerNextRadio.removeCallbacks(runnableNextRadio);
			PlayerBarFragment playerBarFragment = ((MainActivity) getActivity())
					.getPlayerBar();
			populateComingUpPanel(
					playerBarFragment.mPlayerService.getCurrentPlayingTrack(),
					true);
			handlerNextRadio.postDelayed(runnableNextRadio, 10000);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	Handler handlerNextRadio = new Handler();
	Runnable runnableNextRadio = new Runnable() {

		@Override
		public void run() {
			try {
				if (PlayerService.service != null
						&& PlayerService.service.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
					PlayerBarFragment playerBarFragment = ((MainActivity) getActivity())
							.getPlayerBar();
					if (isNowPlayingDisplay) {
						isNowPlayingDisplay = false;
						populateComingUpPanel(playerBarFragment.getNextTrack(),
								false);
					} else {
						isNowPlayingDisplay = true;
						populateComingUpPanel(
								playerBarFragment.mPlayerService
										.getCurrentPlayingTrack(),
								true);
					}
					handlerNextRadio.postDelayed(runnableNextRadio, 10000);
				} else {
					handlerNextRadio.removeCallbacks(runnableNextRadio);
				}
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}
	};

	private boolean isPaused = false;

	@Override
	public void onPause() {
		isPaused = true;
		if (adhandler != null)
			adhandler.removeCallbacks(refreshAd);

		if (mMediaCategoryType == MediaCategoryType.TOP_ARTISTS_RADIO) {
			PlayerBarFragment playerBarFragment = ((MainActivity) getActivity())
					.getPlayerBar();
			playerBarFragment.unregisterToNextTrackUpdateListener(this);
		} else if (mMediaCategoryType == MediaCategoryType.LIVE_STATIONS) {
			PlayerBarFragment playerBarFragment = ((MainActivity) getActivity())
					.getPlayerBar();
			playerBarFragment.unregisterLiveRadioUpdateListener(this);
		}
		if (handlerNextRadio != null && runnableNextRadio != null)
			handlerNextRadio.removeCallbacks(runnableNextRadio);
		if (handlerNextLiveRadio != null && runnableNextLiveRadio != null)
			handlerNextLiveRadio.removeCallbacks(runnableNextLiveRadio);
		// if (mImageFetcher != null) {
		// mImageFetcher.setExitTasksEarly(true);
		// mImageFetcher.flushCache();
		// }

		super.onPause();
	}

	@Override
	public void onDestroyView() {
		if (adhandler != null)
			adhandler.removeCallbacks(refreshAd);
//		System.gc();
//		System.runFinalization();
		super.onDestroyView();

		// if (mImageFetcher != null) {
		// mImageFetcher.closeCache();
		// mImageFetcher = null;
		// }

	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.radio_placement_image:
			try {
				Utils.performclickEvent(getActivity(), placement);
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
			break;

		default:
			break;
		}

	}

	// ======================================================
	// Communication Callbacks.
	// ======================================================

	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS_SONGS) {
			showLoadingDialog(R.string.application_dialog_loading_content);
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			if (operationId == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS_SONGS) {
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

					if (mMediaItem.getTitle().equalsIgnoreCase("Celeb Radio")
							&& !mMediaItem.getTitle().equals(
									mediaItem.getTitle())) {
						mMediaItem = mediaItem;
						LanguageTextView textArtistName = (LanguageTextView) rootView
								.findViewById(R.id.radio_details_top_artists_text_radio_name);
						textArtistName.setText(Utils
								.getMultilanguageTextLayOut(mContext,
										mMediaItem.getTitle()));
					}

					// starts to play.
					PlayerBarFragment.setArtistRadioId(mMediaItem.getId());
					PlayerBarFragment.setArtistUserFav(userFav);
					PlayerBarFragment playerBar = ((MainActivity) getActivity())
							.getPlayerBar();
					playerBar
							.playRadio(radioTracks, PlayMode.TOP_ARTISTS_RADIO);

					// updates the coming up section.
					// populateComingUpPanel(playerBar.getNextTrack(), false);
				} catch (Exception e) {
				}
				hideLoadingDialog();
			} else if (operationId == OperationDefinition.Hungama.OperationId.MULTI_SONG_DETAIL_SONGCATCHER) {
				hideLoadingDialog();
				// Set<String> tags = Utils.getTags();
				// if (!tags.contains("SongCatcher_Used")) {
				// tags.add("SongCatcher_Used");
				// Utils.AddTag(tags);
				// }
				List<MediaItem> mediaItems = (List<MediaItem>) responseObjects
						.get(MultiSongDetailOperation.RESULT_KEY_OBJECT_MEDIA_ITEMS);
				String server_response = (String) responseObjects
						.get(MultiSongDetailOperation.RESPONSE_SERVER);
				if (!Utils.isListEmpty(mediaItems)) {
					if (mediaItems.size() == 1) {
						// displayExactResult(mediaItems);
						// saveMediaDetail(mediaItems, server_response);
						MediaItem mediaItem = mediaItems.get(0);
						// String imageUrl = mediaItem.getImageUrl();
						String imageUrl = ImagesManager
								.getMusicArtSmallImageUrl(mediaItem
										.getImagesUrlArray());
						if (mContext != null && mediaItem != null
								&& !TextUtils.isEmpty(imageUrl)) {
							Picasso.with(mContext).load(imageUrl)
									.into(mComingUpThumbnail);
						}
					} else {
						// displayResultList(mediaItems);
					}
				}
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {

		try {
			hideLoadingDialog();
			// if (!TextUtils.isEmpty(errorMessage) && getActivity() != null) {
			// Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT)
			// .show();
			// }

			if (!TextUtils.isEmpty(errorMessage)
					&& getActivity() != null
					&& errorMessage
							.equalsIgnoreCase("There are no songs for this artist")) {
				Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT)
						.show();
				return;
			}
			if (errorType == ErrorType.NO_CONNECTIVITY) {
				((MainActivity) getActivity())
						.internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
							@Override
							public void onRetryButtonClicked() {
								mAutoPlay = true;
								onStart();
							}
						});
			}
			// this.getFragmentManager().popBackStack();
		} catch (Exception e) {
		}
	}

	// ======================================================
	// Helper methods.
	// ======================================================

	private void initializeUserControls(View rootView) {
		mComingUpLayout = (RelativeLayout) rootView
				.findViewById(R.id.radio_details_layout_coming_up);
		mComingUpThumbnail = (ImageView) rootView
				.findViewById(R.id.radio_details_coming_up_thumbnail);
		mComingUpAlbumName = (LanguageTextView) rootView
				.findViewById(R.id.radio_details_coming_up_album_name);
		mComingUpSongName = (LanguageTextView) rootView
				.findViewById(R.id.radio_details_coming_up_song_name);
		if (mMediaItem != null && mMediaCategoryType != null) {

			// sets the title bar.
			RelativeLayout titleBar = (RelativeLayout) rootView
					.findViewById(R.id.radio_details_title_bar);
			if (mDoShowTitleBar) {
				titleBar.setVisibility(View.VISIBLE);

				// sets the title bar's text.
				mTextTitle = (LanguageTextView) rootView
						.findViewById(R.id.radio_details_title_bar_text);

				if (mMediaCategoryType == MediaCategoryType.LIVE_STATIONS) {
					mTextTitle.setText(Utils.getMultilanguageTextLayOut(
							mContext, mMediaItem.getTitle()));

				} else if (mMediaCategoryType == MediaCategoryType.TOP_ARTISTS_RADIO) {
					mTextTitle.setText(Utils.getMultilanguageTextLayOut(
							mContext,
							getResources().getString(
									R.string.radio_top_artist_radio)));
				}
			}

			// sets the content.
			if (mMediaCategoryType == MediaCategoryType.LIVE_STATIONS) {
				try {
					// RelativeLayout container = (RelativeLayout) rootView
					// .findViewById(R.id.radio_details_layout_live_station);
					//
					// container.setVisibility(View.VISIBLE);

					// shows the details of the Live Station.
					// LanguageTextView description = (LanguageTextView)
					// rootView
					// .findViewById(R.id.radio_details_live_station_text_radio_name);

					// LiveStation liveStation = (LiveStation) mMediaItem;
					// description.setText(Utils.getMultilanguageTextLayOut(
					// mContext, liveStation.getDescription()));
					// description.setVisibility(View.VISIBLE);
					// coming up layout.

					Analytics.logEvent("Live Radio details");
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			} else if (mMediaCategoryType == MediaCategoryType.TOP_ARTISTS_RADIO) {

				RelativeLayout container = (RelativeLayout) rootView
						.findViewById(R.id.radio_details_layout_top_artists_radio);

				container.setVisibility(View.GONE);

				// shows the details of the Artist.

				LanguageTextView textArtistName = (LanguageTextView) rootView
						.findViewById(R.id.radio_details_top_artists_text_radio_name);

				textArtistName.setText(Utils.getMultilanguageTextLayOut(
						mContext, mMediaItem.getTitle()));

				// coming up layout.
				mComingUpLayout = (RelativeLayout) rootView
						.findViewById(R.id.radio_details_layout_coming_up);
				mComingUpThumbnail = (ImageView) rootView
						.findViewById(R.id.radio_details_coming_up_thumbnail);
				mComingUpAlbumName = (LanguageTextView) rootView
						.findViewById(R.id.radio_details_coming_up_album_name);
				mComingUpSongName = (LanguageTextView) rootView
						.findViewById(R.id.radio_details_coming_up_song_name);

				Analytics.logEvent("Top Artists Radio details");
			}
		}

		mRadioPlacementImage = (ImageView) rootView
				.findViewById(R.id.radio_placement_image);
		RelativeLayout rlTileParent = (RelativeLayout) rootView
				.findViewById(R.id.rlTileParent);
		// mRadioPlacementImage.setOnClickListener(this);
		Display dis = getActivity().getWindowManager().getDefaultDisplay();
		int orgWidth = dis.getWidth();
		orgWidth = (int) (orgWidth - (orgWidth / 4));
		rlTileParent.getLayoutParams().height = orgWidth;
		rlTileParent.getLayoutParams().width = orgWidth;
	}

	private void populateComingUpPanel(Track track, boolean isFirstTime) {
		try {
			if (track != null) {
				if (mComingUpLayout.getVisibility() != View.VISIBLE) {
					mComingUpLayout.setVisibility(View.VISIBLE);
				}

				String songPrefix = Utils.getMultilanguageTextLayOut(
						mContext,
						getResources().getString(
								R.string.radio_details_coming_up_song));

				String albumName = "";
				albumName = track.getAlbumName();

				if (!TextUtils.isEmpty(albumName)
						&& !albumName.equals(txtNoAlbum))
					albumName = songPrefix + " " + albumName;

				mComingUpSongName.setText(track.getTitle());
				mComingUpAlbumName.setText(albumName);

				LanguageTextView radio_details_coming_up_label = (LanguageTextView) rootView
						.findViewById(R.id.radio_details_coming_up_label);
				if (isFirstTime) {
					radio_details_coming_up_label.setText(txtNowPlaying);
				} else {
					radio_details_coming_up_label.setText(txtComingUpNext);
				}
				// mImageFetcher.loadImage(track.getImageUrl(),
				// mComingUpThumbnail);
				Picasso.with(mContext).cancelRequest(mComingUpThumbnail);
				final String imageUrl = ImagesManager
						.getMusicArtSmallImageUrl(track.getImagesUrlArray());
				// track.getImageUrl();
				if (mContext != null && track != null
						&& !TextUtils.isEmpty(imageUrl)) {
					Picasso.with(mContext).load(imageUrl)
							.into(mComingUpThumbnail);
				}
			} else {
				mComingUpLayout.setVisibility(View.INVISIBLE);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onNextTrackUpdateListener(Track track) {
		Logger.i("onNextTrackUpdateListener", "onNextTrackUpdateListener");
		populateComingUpPanel(track, false);
	}

	private void playLiveStation(MediaItem mediaItem) {

		LiveStation liveStation = (LiveStation) mediaItem;

		Track liveStationTrack = new Track(liveStation.getId(),
				liveStation.getTitle(), liveStation.getDescription(), null,
				liveStation.getImageUrl(), liveStation.getImageUrl(),
				mediaItem.getImages(), mediaItem.getAlbumId());
		liveStationTrack.setMediaHandle(liveStation.getStreamingUrl());

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

		// starts to play.
		((MainActivity) getActivity()).getPlayerBar().playRadio(
				liveStationList, PlayMode.LIVE_STATION_RADIO);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hungama.myplay.activity.ui.fragments.PlayerBarFragment.
	 * LiveRadioUpdateListener
	 * #onLiveRadioUpdateListener(com.hungama.myplay.activity
	 * .data.dao.hungama.Track)
	 */

	LiveStationDetails storedComingUpTrackDetail;

	// public void setComingUpTrackDetail(LiveStationDetails detail){
	// storedComingUpTrackDetail = detail;
	// if(isVisible()){
	// update(detail);
	// }
	// }
	boolean isFirstTimeLiveRadioCalled = false, isTrackDetailChanged = true;

	void update(LiveStationDetails detail, boolean isRunningLiveRadio) {
		boolean updateThumb = false;
		if (storedComingUpTrackDetail == null
				|| (storedComingUpTrackDetail != null && detail != null && storedComingUpTrackDetail
						.getId() != detail.getId())/*
													 * || (!isRunningLiveRadio
													 * &&
													 * storedCurrentTrackDetail
													 * !=null && detail != null
													 * &&
													 * storedCurrentTrackDetail
													 * .getId() !=
													 * detail.getId())
													 */
				|| (isTrackDetailChanged != isRunningLiveRadio)) {
			isTrackDetailChanged = isRunningLiveRadio;
			updateThumb = true;
			try {
				if (mComingUpThumbnail != null)
					mComingUpThumbnail
							.setImageResource(R.drawable.background_home_tile_album_default);
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
			// if (mContext != null && mMediaItem != null
			// && !TextUtils.isEmpty(mMediaItem.getImageUrl())) {
			// Picasso.with(mContext)
			// .load(mMediaItem.getImageUrl())
			// .placeholder(
			// R.drawable.background_home_tile_album_default)
			// .into(mComingUpThumbnail);
			// }
		}
		LanguageTextView radio_details_coming_up_label = (LanguageTextView) rootView
				.findViewById(R.id.radio_details_coming_up_label);
		if (radio_details_coming_up_label != null) {
			if (!isRunningLiveRadio) {
				radio_details_coming_up_label.setText(txtComingUpNext);
				storedComingUpTrackDetail = detail;
			} else {
				radio_details_coming_up_label.setText(txtNowPlaying);
			}
		} else {
			return;
		}
		try {
			if (detail != null && !detail.getTrack().equalsIgnoreCase("no")) {
				if (mComingUpLayout.getVisibility() != View.VISIBLE) {
					mComingUpLayout.setVisibility(View.VISIBLE);
				}

				if (!detail.getTrack().equalsIgnoreCase("no"))
					mComingUpSongName.setText(Utils.getMultilanguageTextLayOut(
							mContext, detail.getTrack()));

				String songPrefix = Utils.getMultilanguageTextLayOut(
						mContext,
						getResources().getString(
								R.string.radio_details_coming_up_song));
				if (!detail.getAlbum().equalsIgnoreCase("no")) {
					songPrefix = songPrefix + " " + detail.getAlbum();
					mComingUpAlbumName.setText(Utils
							.getMultilanguageTextLayOut(mContext, songPrefix));
					if (updateThumb)
						mDataManager.getMultiSongDetail("" + detail.getId(),
								RadioDetailsFragment.this);
				}

				// if (mContext != null && mMediaItem != null
				// && !TextUtils.isEmpty(mMediaItem.getImageUrl())) {
				// Picasso.with(mContext)
				// .load(mMediaItem.getImageUrl())
				// .placeholder(
				// R.drawable.background_home_tile_album_default)
				// .into(mComingUpThumbnail);
				// }

				// mImageFetcher.loadImage(track.getImageUrl(),
				// mComingUpThumbnail);
				// Picasso.with(mContext).cancelRequest(mComingUpThumbnail);
				// if (mContext != null && track != null &&
				// !TextUtils.isEmpty(track.getImageUrl())) {
				// Picasso.with(mContext).load(track.getImageUrl()).into(mComingUpThumbnail);
				// }
			} else {
				mComingUpLayout.setVisibility(View.INVISIBLE);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onLiveRadioUpdateListener(LiveStationDetails detail) {
		update(detail, false);
	}

    @Override
    public void onLiveRadioUpdateFailedListener() {
        storedComingUpTrackDetail = null;
        if(getActivity()!=null && mComingUpLayout.getVisibility()==View.VISIBLE) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mComingUpLayout.setVisibility(View.INVISIBLE);
                    //Toast.makeText(getActivity(), "onLiveRadioUpdateFailedListener called", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static boolean isbackFromUpgrade;
	private static boolean isUpgrading;

	private RelativeLayout adHolder;
	private RelativeLayout dontWant;
	private float aspectRatio;
	private float density;

	/**
	 * @param next
	 */
	@SuppressWarnings("deprecation")
	private void initializeAds(boolean next) {

		if (isAudioAdPlaying || ((MainActivity) getActivity()).getPlayerBar()
                .isContentFragmentOpen()){
            startRefreshAdsHandler();
				return;
        }

		CampaignsManager mCampaignsManager = CampaignsManager
				.getInstance(getActivity());
		if (next) {
			backgroundImage = null;
			backgroundLink = null;
		}

		if (adHolder != null) {
			// adHolder.setVisibility(View.INVISIBLE);
			// ((RelativeLayout) adHolder.getParent())
			// .setVisibility(View.INVISIBLE);
			adHolder.setVisibility(View.GONE);
			((RelativeLayout) adHolder.getParent()).setVisibility(View.GONE);

		}
		if (dontWant != null) {
			// dontWant.setVisibility(View.VISIBLE);
			dontWant.setVisibility(View.GONE);
		}
		// placement = mCampaignsManager
		// .getPlacementOfType(PlacementType.RADIO_CHANNEL);
		placement = mCampaignsManager
				.getPlacementOfType(PlacementType.RADIO_PLAYER_ARTWORK);
		if (placement != null) {
			DisplayMetrics metrics = null;
			if (HomeActivity.metrics == null) {
				metrics = new DisplayMetrics();
				getActivity().getWindowManager().getDefaultDisplay()
						.getMetrics(metrics);
			} else {
				metrics = HomeActivity.metrics;
			}

			// fileCache = new FileCache(getActivity());

			width = metrics.widthPixels;
			dpi = metrics.densityDpi;
			if (next) {
				backgroundImage = null;
			}
			backgroundLink = Utils.getDisplayProfile(metrics, placement);
			if (backgroundLink != null) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						if (backgroundImage == null) {
							backgroundImage = Utils.getBitmap(getActivity(),
									width, backgroundLink);
							// width = width / 2;
						}
						if (!isUpgrading && backgroundImage != null || !((MainActivity) getActivity()).getPlayerBar()
                                .isContentFragmentOpen() ) {
							adhandler.sendEmptyMessage(0);
							Logger.i("Message", "Sent");
						}
					}
				}).start();
			}
		}
		// }
		// System.out.println(" :::::::::::::::::: " +
		// mDataManager.getApplicationConfigurations().getAdRefreshInterval());
		// System.out.println(" :::::::::::::::::: " + new
		// Gson().toJson(placement));
		// placementAudioAd =
		// mCampaignsManager.getPlacementOfType(PlacementType.AUDIO_AD);
		// if (mPlayerService != null && placementAudioAd != null)
		// mPlayerService.setAudioAd(placementAudioAd);
	}

	Handler adhandler = new Handler() {
		@SuppressWarnings("deprecation")
		public void handleMessage(android.os.Message msg) {

			if (backgroundImage != null
					&& backgroundImage.getIntrinsicHeight() > 0) {
				try {
					View adParentView = rootView;
					Logger.i("Message", "used");

					if (adHolder == null) {
						adHolder = (RelativeLayout) adParentView
								.findViewById(R.id.main_player_drawer_ad);
					}

					if (dontWant == null) {
						dontWant = (RelativeLayout) adParentView
								.findViewById(R.id.main_player_dont_want_ads);
					}

					if (adHolder != null
							&& (adHolder.getVisibility() == View.INVISIBLE)) {
						adHolder.setVisibility(View.GONE);
						// ((RelativeLayout) adHolder.getParent())
						// .setVisibility(View.VISIBLE);
						((RelativeLayout) adHolder.getParent())
								.setVisibility(View.GONE);
						dontWant.setVisibility(View.GONE);
					}

					aspectRatio = backgroundImage.getIntrinsicWidth()
							/ backgroundImage.getIntrinsicHeight();
					density = (float) dpi / 160;

					// adHolder.setOnClickListener(new OnClickListener() {
					//
					// @Override
					// public void onClick(View v) {
					// try {
					// Utils.performclickEvent(getActivity(),
					// placement);
					// // Intent browserIntent = new Intent(
					// // Intent.ACTION_VIEW, Uri.parse(placement
					// // .getActions().get(0).action));
					// // startActivity(browserIntent);
					// } catch (Exception e) {
					// Logger.printStackTrace(e);
					// }
					// }
					// });
					if (ApplicationConfigurations.getInstance(mContext)
							.isUserHasTrialSubscriptionPlan()) {
						Utils.SetMultilanguageTextOnTextView(
								mContext,
								(LanguageTextView) adParentView
										.findViewById(R.id.main_player_text_dont_want_ads),
								mContext.getResources()
										.getString(
												R.string.dont_want_ad_message_trial_user));
						// ((TextView) adParentView
						// .findViewById(R.id.main_player_text_dont_want_ads))
						// .setText(R.string.dont_want_ad_message_trial_user);
						dontWant.setOnClickListener(null);
					} else {
						dontWant.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								Intent upgradeIntent = new Intent(
										getActivity(), UpgradeActivity.class);
								upgradeIntent.putExtra("player", true);
								startActivity(upgradeIntent);
								isUpgrading = true;
							}
						});
					}
					android.view.ViewGroup.LayoutParams params = adHolder
							.getLayoutParams();
					// params.height = (adHolder.getWidth() *
					// backgroundImage
					// .getIntrinsicHeight())
					// / backgroundImage.getIntrinsicWidth();// (int)
					// // ((width
					// // *
					// // density)
					// // /
					// // aspectRatio);
					// params.width = adHolder.getWidth();// (int) ((width *
					// // density));
					params.height = (int) ((backgroundImage.getIntrinsicWidth()) / aspectRatio);
					params.width = (int) ((backgroundImage.getIntrinsicWidth()));
					adHolder.setLayoutParams(params);
					// adHolder.setBackgroundDrawable(backgroundImage);
					displayFlipAds(backgroundImage);
					// System.out.println("CampaignPlayEvent.py request :::: "
					// + ((MainActivity)
					// getActivity()).getPlayerBar().isContentOpened()
					// + " ::: " + isPaused + " ::::: " +
					// getArguments().getBoolean(IS_FOR_PLAYER_BAR, false));
					if (isVisible()
							&& !isPaused
							&& ((((MainActivity) getActivity()).getPlayerBar()
									.isContentOpened() && getArguments()
									.getBoolean(IS_FOR_PLAYER_BAR, false)) || (!((MainActivity) getActivity())
									.getPlayerBar().isContentOpened() && !getArguments()
									.getBoolean(IS_FOR_PLAYER_BAR, false))))
						Utils.postViewEvent(getActivity(), placement);

					Logger.i("Message", "set");
					Logger.i(
							"Message",
							"visibility:"
									+ String.valueOf(adHolder.getVisibility()));

					Button close = (Button) adParentView
							.findViewById(R.id.bCloseVideoAd);
					close.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							adHolder.setVisibility(View.GONE);
							((RelativeLayout) adHolder.getParent())
									.setVisibility(View.GONE);
							// dontWant.setVisibility(View.VISIBLE);
							dontWant.setVisibility(View.GONE);
							displayFlipAds(null);
							// final Thread t = new Thread(new Runnable() {
							//
							// @Override
							// public void run() {
							if (!isUpgrading && isVisible()) {
								adhandler.removeCallbacks(refreshAd);
								adhandler
										.postDelayed(
												refreshAd,
												mDataManager
														.getApplicationConfigurations()
														.getAppConfigPlayerOverlayRefresh() * 1000);
							} else if (isUpgrading) {
							}
							// }
							// });
							// t.start();
						}
					});
				} catch (Exception e) {
				} catch (java.lang.Error e) {
				}
				adhandler.removeCallbacks(refreshAd);
				adhandler.postDelayed(refreshAd, mDataManager
						.getApplicationConfigurations()
						.getAppConfigPlayerOverlayRefresh() * 1000);
			} else {
				adhandler.removeCallbacks(refreshAd);
				adhandler.sendEmptyMessage(0);
			}
			// }
		}
	};

	Runnable refreshAd = new Runnable() {
		@Override
		public void run() {
			if (isVisible())
				initializeAds(true);
		}
	};

	int adsFlipInterval = -1;

	private void startAdsFlipTimer() {
		stopAdsFlipTimer();
		if (adsFlipInterval == -1) {
			adsFlipInterval = mApplicationConfigurations
					.getAppConfigPlayerOverlayFlipBackDuration();
		}
		if (adsFlipInterval <= 0)
			adsFlipInterval = 10;
		handlerAdsFlipTimer.postDelayed(runAdsFlip, adsFlipInterval * 1000);
	}

	@Override
	public void onDestroy() {
		stopAdsFlipTimer();
		super.onDestroy();
	}

	public void stoprefreshAdsHandle() {
		adhandler.removeCallbacks(refreshAd);
	}

	public void startRefreshAdsHandler() {
		final int adRefreshInterval = mApplicationConfigurations
				.getAppConfigPlayerOverlayRefresh();//Start();// getAdRefreshInterval();
		adhandler.removeCallbacks(refreshAd);
		adhandler.postDelayed(refreshAd, adRefreshInterval * 1000);
	}

	public void stopAdsFlipTimer() {
		handlerAdsFlipTimer.removeCallbacks(runAdsFlip);
	}

	Handler handlerAdsFlipTimer = new Handler();
	Runnable runAdsFlip = new Runnable() {

		@Override
		public void run() {
			if (!isFlip && backgroundImage != null)
				displayFlipAds(backgroundImage);
			else
				displayFlipAds(null);
		}
	};
	Button ivClose;

	public void displayFlipAds(BitmapDrawable backgroundImage) {
		ivClose = (Button) rootView.findViewById(R.id.bCloseVideoAd);
		ivClose.setVisibility(View.INVISIBLE);
		playerBarFragment.removeTitleHandler(false);
		if (isFlip) {
			new FlipAnimationListener(mRadioPlacementImage, null, true);
			mRadioPlacementImage.setOnClickListener(null);
		} else if (!isFlip && backgroundImage != null) {
			new FlipAnimationListener(mRadioPlacementImage, backgroundImage,
					true);
			mRadioPlacementImage.setOnClickListener(this);
		}
	}

	public void setDefault() {
		try {
			mRadioPlacementImage.setOnClickListener(null);
			isFlip = false;
			// coverFlowAdapter.notifyDataSetChanged();
			displayTileImage(mRadioPlacementImage, mMediaItem);
			// iv_item_player_shadow.setVisibility(View.VISIBLE);
			// rlItemInfoCover.setVisibility(View.VISIBLE);
			ivClose.setVisibility(View.GONE);
			mRadioPlacementImage.clearAnimation();
			stopAdsFlipTimer();
		}catch (Exception e){

		}catch (Error e){

		}
	}

	// ---------------------Flip animation - 11 Feb 2015----------//

	boolean isFlip = false;
	int flipPos = -1;
	PicassoUtil picasso;

	private class FlipAnimationListener implements AnimationListener {
		private Animation animation1;
		private Animation animation2;
		// private boolean isBackOfCardShowing = true;
		View view;
		BitmapDrawable adBitmap;
		boolean needToRestartFlip = true;

		public FlipAnimationListener(View view, BitmapDrawable adUrl,
				boolean needToRestartFlip) {
			animation1 = AnimationUtils.loadAnimation(getActivity(),
					R.anim.to_middle_player);
			animation1.setAnimationListener(this);
			animation2 = AnimationUtils.loadAnimation(getActivity(),
					R.anim.from_middle_player);
			animation2.setAnimationListener(this);
			this.adBitmap = adUrl;
			this.view = view;
			this.needToRestartFlip = needToRestartFlip;
			view.clearAnimation();
			view.setAnimation(animation1);
			view.startAnimation(animation1);
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			try {
				if (animation == animation1) {
					if (!isFlip) {
						isFlip = true;
						((ImageView) view).setImageDrawable(adBitmap);
						// iv_item_player_shadow.setVisibility(View.INVISIBLE);
						// rlItemInfoCover.setVisibility(View.INVISIBLE);
						// ivClose.setVisibility(View.VISIBLE);
					} else {
						isFlip = false;
						// coverFlowAdapter.notifyDataSetChanged();
						displayTileImage(mRadioPlacementImage, mMediaItem);
						// if (picasso == null)
						// picasso = PicassoUtil.with(getActivity());
						// picasso.load(null, mMediaItem.getImageUrl(),
						// (ImageView) view,
						// R.drawable.icon_main_player_no_content);
						// iv_item_player_shadow.setVisibility(View.VISIBLE);
						// rlItemInfoCover.setVisibility(View.VISIBLE);
						// ivClose.setVisibility(View.GONE);
					}
					view.clearAnimation();
					view.setAnimation(animation2);
					view.startAnimation(animation2);

				} else if (animation == animation2) {
					// isFlip = false;
					view.clearAnimation();
					animation1.setFillAfter(false);
					animation2.setFillAfter(false);
					// view.setClickable(false);
					if (isFlip) {
						if (isAudioAdPlaying) {
							ivClose.setVisibility(View.GONE);
							stopAdsFlipTimer();
						} else {
							ivClose.setVisibility(View.VISIBLE);
							startAdsFlipTimer();
						}
					} else {
						stopAdsFlipTimer();
					}
					playerBarFragment.startTitleHandler();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}
	}

	private void loadStoreCelebRadioAd() {
		// List<Placement> radioPlacements = mDataManager
		// .getStoredRadioPlacement();
		//
		// if (radioPlacements != null && !radioPlacements.isEmpty()
		// && backgroundLink == null) {
		//
		// Random myRandom = new Random();
		// int listSize = radioPlacements.size();
		//
		// int randomRadioPlacement = (Math.abs(myRandom.nextInt()) %
		// (listSize));
		//
		// radioPlacement = radioPlacements.get(randomRadioPlacement);
		//
		// // mImageFetcher.loadImage(radioPlacement.getBgImageSmall(),
		// // mRadioPlacementImage);
		// Picasso.with(mContext).cancelRequest(mRadioPlacementImage);
		// if (mContext != null && radioPlacement != null
		// && !TextUtils.isEmpty(radioPlacement.getBgImageSmall())) {
		// Picasso.with(mContext)
		// .load(radioPlacement.getBgImageSmall())
		// .placeholder(
		// R.drawable.background_home_tile_album_default)
		// .into(mRadioPlacementImage);
		// }
		// } else {
		// if (radioPlacement == null)
		// mRadioPlacementImage.setClickable(false);
		// }
	}

	private static long lastAdReportingTime = 0;
	private boolean isAudioAdPlaying = false;

	public void loadAudioAd(final Placement audioAdPlacement) {
		try {
			isAudioAdPlaying = true;
			if (adhandler != null)
				adhandler.removeCallbacks(refreshAd);
			if (isFlip)
				displayFlipAds(null);
			stopAdsFlipTimer();
			if (audioAdPlacement != null) {
				DisplayMetrics metrics = new DisplayMetrics();
				if (getActivity() == null)
					metrics = HomeActivity.metrics;
				else
					getActivity().getWindowManager().getDefaultDisplay()
							.getMetrics(metrics);
				String adImageUrl = Utils.getDisplayProfile(metrics,
						audioAdPlacement);
				if (adImageUrl != null) {
					// Picasso.with(getActivity()).load(
					// R.drawable.icon_main_player_no_content);
					if (mRadioPlacementImage != null
							&& !TextUtils.isEmpty(adImageUrl)) {
						mRadioPlacementImage.setBackgroundDrawable(null);
						mRadioPlacementImage.setImageDrawable(null);
						Picasso.with(getActivity()).load(adImageUrl).fit()
								.centerInside()
								// .placeholder(R.drawable.icon_main_player_no_content)
								.into(mRadioPlacementImage, new Callback() {
									@Override
									public void onSuccess() {
										// System.out.println("CampaignPlayEvent.py request ::::::::: onSuccess");
										if (lastAdReportingTime == 0
												|| (System.currentTimeMillis() - lastAdReportingTime) >= (mDataManager
														.getApplicationConfigurations()
														.getAppConfigPlayerOverlayRefresh() - 2) * 1000) {
											lastAdReportingTime = System
													.currentTimeMillis();
											Utils.postViewEvent(getActivity(),
													audioAdPlacement);
										}
										// Utils.postViewEvent(getActivity(),
										// audioAdPlacement);
										mRadioPlacementImage
												.setOnClickListener(new OnClickListener() {
													@Override
													public void onClick(View v) {
														try {
															Utils.performclickEvent(
																	getActivity(),
																	audioAdPlacement);
														} catch (Exception e) {
															Logger.printStackTrace(e);
														}
													}
												});
									}

									@Override
									public void onError() {
									}
								});

					}
				}
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	public void completeAudioAd() {
		isAudioAdPlaying = false;
		mRadioPlacementImage.setImageBitmap(null);
		// initializeAds(true);
		mRadioPlacementImage.setOnClickListener(null);
		lastAdReportingTime = 0;
		if (mMediaCategoryType == MediaCategoryType.TOP_ARTISTS_RADIO) {
			displayTileImage(mRadioPlacementImage, mMediaItem);
		}
		startRefreshAdsHandler();
	}

	private void displayTileImage(ImageView mRadioPlacementImage,
			MediaItem mMediaItem) {
		if(isFlip || isAudioAdPlaying)
			return;
		if (mMediaItem != null && mMediaItem.getImagesUrlArray() != null) {
			String imageUrl = ImagesManager.getRadioArtImageUrl(mMediaItem
					.getImagesUrlArray());
			if (getActivity() != null && mMediaItem != null
					&& !TextUtils.isEmpty(imageUrl)) {
				if (picasso == null)
					picasso = PicassoUtil.with(getActivity());
				picasso.loadWithoutTag(null, imageUrl, mRadioPlacementImage,
						R.drawable.icon_main_player_no_content);
			} else {
				mRadioPlacementImage
						.setImageResource(R.drawable.icon_main_player_no_content);
				mRadioPlacementImage.setBackgroundColor(Color.WHITE);
			}
		}
	}

}
