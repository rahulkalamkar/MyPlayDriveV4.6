package com.hungama.myplay.activity.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.campaigns.PlacementType;
import com.hungama.myplay.activity.data.dao.hungama.Discover;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.ui.DiscoveryActivity;
import com.hungama.myplay.activity.ui.DiscoveryPlayerContextMenuActivity;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.UpgradeActivity;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.hungama.myplay.activity.util.Utils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.Map;

/**
 * Shows details related to the selected Radio station.
 */
public class DiscoveryPlayDetailsFragment extends MainFragment implements
		OnClickListener, CommunicationOperationListener {

	public static final String TAG = "DiscoveryPlayDetailsFragment";

	public static final String IS_FOR_PLAYER_BAR = "is_for_player_bar";

	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;

	private RelativeLayout mComingUpLayout;
	private ImageView mComingUpThumbnail;
	private ImageView mRadioPlacementImage;
	private LanguageTextView mComingUpAlbumName;
	private LanguageTextView mComingUpSongName;

	private String backgroundLink;
	private BitmapDrawable backgroundImage;
	private View rootView;
	private Placement placement;

	private int width;

	private Context mContext;
	Discover mDiscover;
	String songPrefix;
	PicassoUtil picasso;
	PlayerBarFragment playerBarFragment;

	// ======================================================
	// Life Cycle callbacks.
	// ======================================================


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Analytics.postCrashlitycsLog(getActivity(), DiscoveryPlayDetailsFragment.class.getName());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Logger.i("Discovery Player", "Discovery Player : 3");
		rootView = inflater.inflate(R.layout.fragment_discovery_play_details,
				container, false);
		mContext = getActivity().getApplicationContext();
		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();

		// if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
		// Utils.traverseChild(rootView, getActivity());
		// }
		playerBarFragment = ((MainActivity) getActivity()).getPlayerBar();

		// Set<String> tags = PushManager.shared().getTags();
		// tags.add("discover_used");
		// PushManager.shared().setTags(tags);

//		new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//			@Override
//			public void run() {
				try {
					initializeUserControls(rootView);
					Bundle bundle = getArguments();
					mDiscover = (Discover) bundle
							.getSerializable(DiscoveryActivity.ARGUMENT_MOOD);

					setDiscovery(mDiscover);
					displayTileImage(mRadioPlacementImage);
				} catch (Exception e) {
					e.printStackTrace();
				} catch (Error e) {
					e.printStackTrace();
				}
//			}
//		}, 1000);//
		Logger.i("Discovery Player", "Discovery Player : 4");

		return rootView;
	}

	private void showUpcomingLayout() {
		if (mComingUpLayout != null
				&& mComingUpLayout.getVisibility() != View.VISIBLE)
			handleUpcoming.postDelayed(runUpcoming, 1000);
	}

	Handler handleUpcoming = new Handler();
	Runnable runUpcoming = new Runnable() {

		@Override
		public void run() {
			try {
				if (PlayerService.service != null
						&& PlayerService.service.hasNext())
					populateComingUpPanel(PlayerService.service.getNextTrack());
				if (mDiscover != null && mDiscover.getMood() != null) {
					rootView.findViewById(R.id.iv_discovery_play_context_menu)
							.setVisibility(View.VISIBLE);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	};

	void setDiscovery(Discover mDiscover) {
		// setDefault();
		this.mDiscover = mDiscover;
		ImageView ivSmilePic = (ImageView) rootView
				.findViewById(R.id.ivSmilePic);
		boolean isSmilyshow;
		if (mDiscover != null && mDiscover.getHashTag() == null) {
			try {
				// ivSmilePic.setImageResource(Integer.parseInt(mDiscover
				// .getMood().getSmallImageUrl()));

				try {
					if (TextUtils.isEmpty(mDiscover.getMood().getSmallImageUrl())) {
						ivSmilePic.setImageResource(mDataManager.getMoodIcon(mDiscover.getMood()
								.getId(), mDiscover.getMood().getName(), true));
					} else {
						Drawable drawable = mDataManager.getMoodIcon(mDiscover.getMood().getSmallImageUrl());
						if (drawable == null) {
							ivSmilePic.setImageResource(mDataManager.getMoodIcon(mDiscover.getMood()
									.getId(), mDiscover.getMood().getName(), true));
						} else {
							ivSmilePic.setImageDrawable(drawable);
						}
					}
				} catch (Exception e) {
					ivSmilePic.setImageResource(mDataManager.getMoodIcon(mDiscover
							.getMood().getId(), mDiscover.getMood().getName(), true));
					Logger.printStackTrace(e);
				}

//				ivSmilePic.setImageResource(mDataManager.getMoodIcon(mDiscover
//						.getMood().getId(),mDiscover.getMood().getName(),true));
			} catch (Exception e) {
			}
			ivSmilePic.setVisibility(View.VISIBLE);
			rootView.findViewById(R.id.ivBlackShadow).setVisibility(
					View.VISIBLE);
			rootView.findViewById(R.id.iv_discovery_play_context_menu)
					.setVisibility(View.VISIBLE);
			openAppGuideActivity(0);
			isSmilyshow = true;
		} else {
			isSmilyshow = false;
			ivSmilePic.setVisibility(View.GONE);
			rootView.findViewById(R.id.ivBlackShadow).setVisibility(View.GONE);
			rootView.findViewById(R.id.iv_discovery_play_context_menu)
					.setVisibility(View.GONE);
		}
		if (PlayerService.service != null
				&& PlayerService.service.isAdPlaying()) {
			hideShowSmilePic(true);
		} else {
			hideShowSmilePic(!isSmilyshow);

			// mComingUpThumbnail
			// .setImageResource(R.drawable.icon_main_player_no_content);
			// mComingUpThumbnail.setImageDrawable(null);
			// if (PlayerService.service != null
			// && !PlayerService.service.isAdPlaying())
			// mRadioPlacementImage.setImageDrawable(null);
		}
	}

	@Override
	public void onStart() {
		try {
			super.onStart();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		// if (PlayerService.service != null)
		// PlayerService.service.registerPlayerStateListener(this);
		Analytics.startSession(getActivity(), this);
		Analytics.onPageView();
	}

	@Override
	public void onStop() {
		super.onStop();
		Analytics.onEndSession(getActivity());
		// if (PlayerService.service != null)
		// PlayerService.service.unregisterPlayerStateListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();

		// coming up
		// playerBarFragment.registerToNextTrackUpdateListener(this);
		if (!CacheManager.isProUser(getActivity())) {
			final int adRefreshInterval = mApplicationConfigurations
					.getAppConfigPlayerOverlayStart();// getAdRefreshInterval();
			adhandler.removeCallbacks(refreshAd);
			adhandler.postDelayed(refreshAd, adRefreshInterval * 1000);
		}
		if (isPaused)
			openAppGuideActivity(1000);
		isPaused = false;
	}

	private boolean isPaused = false;

	@Override
	public void onPause() {
		isPaused = true;
		if (adhandler != null)
			adhandler.removeCallbacks(refreshAd);

		// playerBarFragment.unregisterToNextTrackUpdateListener(this);

		super.onPause();
	}

	@Override
	public void onDestroyView() {
		if (adhandler != null)
			adhandler.removeCallbacks(refreshAd);
		// if (handlerImag != null)
		// handlerImag.removeCallbacks(runImg);
		// System.gc();
		// System.runFinalization();
		super.onDestroyView();

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
	String txtComingUpNext;

	private void initializeUserControls(View rootView) {
		txtComingUpNext = Utils.getMultilanguageText(getActivity(),
				getResources()
						.getString(R.string.radio_details_coming_up_label));
		picasso = PicassoUtil.with(getActivity());
		ivClose = (Button) rootView.findViewById(R.id.bCloseVideoAd);
		rlTileParent = (RelativeLayout) rootView
				.findViewById(R.id.rlTileParent);
		mComingUpLayout = (RelativeLayout) rootView
				.findViewById(R.id.radio_details_layout_coming_up);
		mComingUpLayout.setVisibility(View.INVISIBLE);
		mComingUpThumbnail = (ImageView) rootView
				.findViewById(R.id.radio_details_coming_up_thumbnail);
		mComingUpAlbumName = (LanguageTextView) rootView
				.findViewById(R.id.radio_details_coming_up_album_name);
		mComingUpSongName = (LanguageTextView) rootView
				.findViewById(R.id.radio_details_coming_up_song_name);

		TextView radioDetailsComingUpNext = (LanguageTextView) rootView
				.findViewById(R.id.radio_details_coming_up_next);
		radioDetailsComingUpNext.setText(txtComingUpNext);

		mRadioPlacementImage = (ImageView) rootView
				.findViewById(R.id.radio_placement_image);
		Display dis = getActivity().getWindowManager().getDefaultDisplay();
		int orgWidth = dis.getWidth();
		orgWidth = (int) (orgWidth - (orgWidth / 4));
		rlTileParent.getLayoutParams().height = orgWidth;
		rlTileParent.getLayoutParams().width = orgWidth;
		mRadioPlacementImage.setVisibility(View.VISIBLE);
		ImageView iv_discovery_play_context_menu = (ImageView) rootView
				.findViewById(R.id.iv_discovery_play_context_menu);

		iv_discovery_play_context_menu
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						if (PlayerService.service != null
								&& PlayerService.service.isAdPlaying()) {
							return;
						}
						Intent i = new Intent(getActivity(),
								DiscoveryPlayerContextMenuActivity.class);
						Bundle data = new Bundle();

						data.putSerializable(DiscoveryActivity.ARGUMENT_MOOD,
								(Serializable) mDiscover);
						i.putExtras(data);
						startActivity(i);
					}
				});

		dontWant = (RelativeLayout) rootView
				.findViewById(R.id.main_player_dont_want_ads);
		if (CacheManager.isProUser(getActivity())) {
			dontWant.setVisibility(View.GONE);
		}
		songPrefix = Utils.getMultilanguageTextLayOut(mContext, getResources()
				.getString(R.string.radio_details_coming_up_song));
		LanguageTextView radio_details_coming_up_next = (LanguageTextView) rootView
				.findViewById(R.id.radio_details_coming_up_next);
		radio_details_coming_up_next.setText(txtComingUpNext);
		// startActivityForResult(i, FILTER_OPTIONS_ACTIVITY_RESULT_CODE);
	}

	private void populateComingUpPanel(Track track) {
		try {

			if (track != null) {
				if (mComingUpLayout.getVisibility() != View.VISIBLE) {
					mComingUpLayout.setVisibility(View.VISIBLE);
				}
				String albumName = songPrefix + " " + track.getAlbumName();
				mComingUpSongName.setText(track.getTitle());
				mComingUpAlbumName.setText(albumName);
				loadUpcomingImage(track);
			} else {
				mComingUpLayout.setVisibility(View.INVISIBLE);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	private void loadUpcomingImage(final Track track) {
		try {
			Logger.i("updateInfoDetails", "updateInfoDetails loadUpcomingImage");
			if (nextTrackId != track.getId()) {
				nextTrackId = track.getId();

				final String imageUrl = ImagesManager
						.getMusicArtSmallImageUrl(track.getImagesUrlArray());
				Logger.i("updateInfoDetails", "updateInfoDetails imageUrl:"
						+ imageUrl);
				if (getActivity() != null) {
					Logger.i("imageUrl", "imageUrl:" + imageUrl);
					if (mContext != null && track != null
							&& !TextUtils.isEmpty(imageUrl)) {
						picasso.loadWithoutTag(null, imageUrl,
								mComingUpThumbnail,
								R.drawable.background_home_tile_album_default);
						Logger.i("updateInfoDetails",
								"updateInfoDetails picasso:");
					} else {
						mComingUpThumbnail.setImageDrawable(null);
						// mComingUpThumbnail
						// .setImageResource(R.drawable.icon_main_player_no_content);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mComingUpThumbnail.setImageDrawable(null);
					// mComingUpThumbnail
					// .setImageResource(R.drawable.icon_main_player_no_content);
				}
			});
		}
	}

	// @Override
	// public void onNextTrackUpdateListener(Track track) {
	// Logger.i("onNextTrackUpdateListener", "onNextTrackUpdateListener");
	// populateComingUpPanel(track);
	// displayTileImage(mRadioPlacementImage);
	// }

	// public void setComingUpTrackDetail(LiveStationDetails detail){
	// storedComingUpTrackDetail = detail;
	// if(isVisible()){
	// update(detail);
	// }
	// }

	// public static boolean isbackFromUpgrade;
	private static boolean isUpgrading;

	private RelativeLayout adHolder;
	private RelativeLayout dontWant;
	private float aspectRatio;

	/**
	 * @param next
	 */
	@SuppressWarnings("deprecation")
	private void initializeAds(boolean next) {

		if (isAudioAdPlaying || playerBarFragment.isContentFragmentOpen()) {
            startRefreshAdsHandler();
            return;
        }
		CampaignsManager mCampaignsManager = CampaignsManager
				.getInstance(getActivity());
		if (next) {
			backgroundImage = null;
			backgroundLink = null;
		}
		placement = mCampaignsManager
				.getPlacementOfType(PlacementType.DISCOVERY_PLAYER_ARTWORK);
		if (placement != null) {
			DisplayMetrics metrics = new DisplayMetrics();

			getActivity().getWindowManager().getDefaultDisplay()
					.getMetrics(metrics);
			width = metrics.widthPixels;
			backgroundLink = Utils.getDisplayProfile(metrics, placement);

			if (backgroundLink != null) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						if (backgroundImage == null) {
							backgroundImage = Utils.getBitmap(getActivity(),
									width, backgroundLink);
						}
						// h.sendEmptyMessage(0);
						adhandler.sendEmptyMessage(0);
					}
				}).start();
			} else {
				loadStoreCelebRadioAd();
			}
		} else {
			loadStoreCelebRadioAd();
		}
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
						// adHolder.setVisibility(View.INVISIBLE);
						adHolder.setVisibility(View.GONE);
						dontWant.setVisibility(View.GONE);
					}

					aspectRatio = backgroundImage.getIntrinsicWidth()
							/ backgroundImage.getIntrinsicHeight();

					// adHolder.setOnClickListener(new OnClickListener() {
					//
					// @Override
					// public void onClick(View v) {
					// try {
					// Utils.performclickEvent(getActivity(),
					// placement);
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
							adHolder.setVisibility(View.INVISIBLE);
							// dontWant.setVisibility(View.VISIBLE);
							dontWant.setVisibility(View.GONE);
							adHolder.setVisibility(View.GONE);
							if (!isUpgrading && isVisible()) {
								adhandler.removeCallbacks(refreshAd);
								// stopAdsFlipTimer();
								adhandler
										.postDelayed(
												refreshAd,
												mDataManager
														.getApplicationConfigurations()
														.getAppConfigPlayerOverlayRefresh() * 1000);
								displayFlipAds(null);
							} else if (isUpgrading) {
							}
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
		}
	};

	Runnable refreshAd = new Runnable() {
		@Override
		public void run() {
			if (isVisible())
				initializeAds(true);
		}
	};

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
			setDefault();
			resetVariables(false);
			if (adhandler != null)
				adhandler.removeCallbacks(refreshAd);
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
						if (PlayerService.service != null
								&& !PlayerService.service.isAdPlaying())
							mRadioPlacementImage.setImageDrawable(null);
						hideShowSmilePic(true);
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
		hideShowSmilePic(false);
		// displayTileImage(mRadioPlacementImage);
		startRefreshAdsHandler();
	}

	private void hideShowSmilePic(boolean needToHide) {
		ImageView ivBlackShadow = (ImageView) rootView
				.findViewById(R.id.ivBlackShadow);
		ImageView ivSmilePic = (ImageView) rootView
				.findViewById(R.id.ivSmilePic);
		if (needToHide || isFlip || (mDiscover != null && mDiscover.getHashTag() != null)) {
			ivBlackShadow.setVisibility(View.INVISIBLE);
			ivSmilePic.setVisibility(View.INVISIBLE);
		} else {
			ivBlackShadow.setVisibility(View.VISIBLE);
			ivSmilePic.setVisibility(View.VISIBLE);
		}
	}

	private long currentTrackId = -1;
	private long nextTrackId = -1;
	private String currentImageUrl = null;

	private void displayTileImage(final ImageView mRadioPlacementImage) {
		try {
			Logger.i("updateInfoDetails", "updateInfoDetails displayTileImage2");
			if (PlayerService.service != null
					&& !PlayerService.service.isAdPlaying()) {
				final Track track = PlayerService.service
						.getCurrentPlayingTrack();
				if (currentTrackId != track.getId()) {
					currentTrackId = track.getId();
					final String imageUrl = ImagesManager
							.getMusicArtSmallImageUrl(track.getImagesUrlArray());
					currentImageUrl = imageUrl;
					Logger.i("updateInfoDetails",
							"updateInfoDetails imageUrl2:" + imageUrl);
					// track.getImageUrl();
					if (getActivity() != null && !TextUtils.isEmpty(imageUrl)) {

						picasso.loadWithoutTag(null, imageUrl,
								mRadioPlacementImage,
								R.drawable.background_home_tile_album_default);
						Logger.i("updateInfoDetails",
								"updateInfoDetails picasso2:" + picasso);
					} else {
						// mRadioPlacementImage
						// .setImageResource(R.drawable.icon_main_player_no_content);
						mRadioPlacementImage.setImageDrawable(null);
					}
					showUpcomingLayout();
				}
			}
		} catch (Exception e) {
			mRadioPlacementImage.setImageDrawable(null);
			// mRadioPlacementImage
			// .setImageResource(R.drawable.icon_main_player_no_content);
		}
	}

	// @Override
	// public void onStartLoadingTrack(Track track) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onTrackLoadingBufferUpdated(Track track, int precent) {
	// // TODO Auto-generated method stub
	//
	// }

	// @Override
	// public void onStartPlayingTrack(Track track) {
	// // TODO Auto-generated method stub
	//
	// }

	// @Override
	// public void onFinishPlayingTrack(Track track) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onFinishPlayingQueue() {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onSleepModePauseTrack(Track track) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onErrorHappened(Error error) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onStartPlayingAd(Placement audioad) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onAdCompletion() {
	// // TODO Auto-generated method stub
	//
	// }

	int adsFlipInterval = -1;

	private void startAdsFlipTimer() {
//        isFlip = false;
//		stopAdsFlipTimer();
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
	RelativeLayout rlTileParent;

	public void setDefault() {
		try{
		mRadioPlacementImage.setOnClickListener(null);
		isFlip = false;
		// coverFlowAdapter.notifyDataSetChanged();
		if (!isAudioAdPlaying) {
			if (currentImageUrl != null)
				picasso.load(null, currentImageUrl, mRadioPlacementImage,
						R.drawable.icon_main_player_no_content);
			else
				displayTileImage(mRadioPlacementImage);
		}
		// displayTileImage(mRadioPlacementImage);
		// iv_item_player_shadow.setVisibility(View.VISIBLE);
		// rlItemInfoCover.setVisibility(View.VISIBLE);
		ivClose.setVisibility(View.GONE);
		mRadioPlacementImage.clearAnimation();
		hideShowSmilePic(false);
		stopAdsFlipTimer();
		}catch (Exception e){

		}catch (Error e){

		}
	}

	public void displayFlipAds(BitmapDrawable backgroundImage) {

		ivClose.setVisibility(View.INVISIBLE);
		hideShowSmilePic(true);

		playerBarFragment.removeTitleHandler(false);
		rlTileParent.setBackgroundColor(Color.TRANSPARENT);
		if (isFlip) {
			new FlipAnimationListener(mRadioPlacementImage, null, true);
			mRadioPlacementImage.setOnClickListener(null);
		} else if (!isFlip && backgroundImage != null) {
			new FlipAnimationListener(mRadioPlacementImage, backgroundImage,
					true);
			mRadioPlacementImage.setOnClickListener(this);
			// mRadioPlacementImage.setOnClickListener(new OnClickListener() {
			//
			// @Override
			// public void onClick(View arg0) {
			// try {
			// Utils.performclickEvent(getActivity(), placement);
			// } catch (Exception e) {
			// Logger.printStackTrace(e);
			// }
			// }
			// });
		}
	}

	// ---------------------Flip animation - 11 Feb 2015----------//

	boolean isFlip = false;

	// int flipPos = -1;

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
					if (currentImageUrl != null)
						picasso.load(null, currentImageUrl,
								mRadioPlacementImage,
								R.drawable.icon_main_player_no_content);
					else
						displayTileImage(mRadioPlacementImage);
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
					// hideShowSmilePic(true);
					if (isAudioAdPlaying) {
						ivClose.setVisibility(View.GONE);
						stopAdsFlipTimer();
					} else {
						ivClose.setVisibility(View.VISIBLE);
						startAdsFlipTimer();
					}
				} else {
					hideShowSmilePic(false);
					stopAdsFlipTimer();

				}
				playerBarFragment.startTitleHandler();
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}
	}

	public void updateInfoDetails() {
		if (PlayerService.service == null)
			return;
		Utils.clearCache(true);
		Logger.i("updateInfoDetails", "updateInfoDetails Started");
		resetVariables(true);
		// mComingUpThumbnail
		// .setImageResource(R.drawable.icon_main_player_no_content);
		// mRadioPlacementImage
		// .setImageResource(R.drawable.icon_main_player_no_content);
		mComingUpThumbnail.setImageDrawable(null);
		if (PlayerService.service != null
				&& !PlayerService.service.isAdPlaying())
			mRadioPlacementImage.setImageDrawable(null);
		Track nextTrack = PlayerService.service.getNextTrack();
		if (nextTrack != null) {
			populateComingUpPanel(nextTrack);
		}
		displayTileImage(mRadioPlacementImage);
		ivClose.setVisibility(View.INVISIBLE);
        isFlip = false;
        stopAdsFlipTimer();
        Logger.i("updateInfoDetails", "updateInfoDetails Stop");
	}

	private void resetVariables(boolean needToResetNextId) {
		currentImageUrl = null;
		currentTrackId = -1;
		if (needToResetNextId)
			nextTrackId = -1;
	}

	boolean isShowcaseShowing = false;

	/**
	 * Show showcase view for Music Preferences
	 */
	private void openAppGuideActivity(int timeDelay) {
		try {

			if (rootView.findViewById(R.id.iv_discovery_play_context_menu)
					.getVisibility() != View.VISIBLE)
				return;

			if (mApplicationConfigurations.isEnabledDiscoverGuidePage()) {
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (mApplicationConfigurations
								.isEnabledDiscoverGuidePage()
								&& !isShowcaseShowing) {
							isShowcaseShowing = true;
							RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(
									ViewGroup.LayoutParams.WRAP_CONTENT,
									ViewGroup.LayoutParams.WRAP_CONTENT);
							lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
							lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
							int margin = ((Number) (getResources()
									.getDisplayMetrics().density * 12))
									.intValue();
							lps.setMargins(2 * margin, margin, margin,
									mDataManager.getShowcaseButtonMargin()
											* margin);

							ViewTarget target = new ViewTarget(
									rootView.findViewById(R.id.iv_discovery_play_context_menu));
							ShowcaseView sv = new ShowcaseView.Builder(
									getActivity(), false)
									.setTarget(target)
									.setContentTitle(
											R.string.showcase_discover_title)
									// .setContentText(
									// R.string.showcase_discover_message)
									.setStyle(R.style.CustomShowcaseTheme2)
									// .setShowcaseEventListener(getActivity())
									.hideOnTouchOutside().build();
							sv.setBlockShowCaseTouches(true);
							sv.setButtonPosition(lps);
							mApplicationConfigurations
									.setIsEnabledDiscoverGuidePage(false);
						}
					}
				}, timeDelay);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}
}
