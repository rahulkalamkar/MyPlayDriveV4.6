package com.hungama.myplay.activity.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.HungamaApplication;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.MediaCachingTaskNew;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.PlayerOption;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.ui.fragments.BackHandledFragment;
import com.hungama.myplay.activity.ui.fragments.RadioDetailsFragment;
import com.hungama.myplay.activity.ui.fragments.SocialMyStreamFragment;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Allows to browse for the available web radios from Hungama, play and show
 * their details.
 */
public class MyStreamActivity extends BackHandledFragment implements
		OnMediaItemOptionSelectedListener, CommunicationOperationListener {

	private static final String TAG = "RadioActivity";
    private Activity activity;
	Context mContext;
	// public static final String EXTRA_SHOW_DETAILS_MEDIA_ITEM =
	// "extra_show_details_media_item";
	// public static final String EXTRA_SHOW_DETAILS_CATEGORY_TYPE =
	// "extra_show_details_category_type";

	private FragmentManager mFragmentManager;
	public MediaCategoryType mediaCategoryType;

	private boolean mIfDetailsRequestedImmediately = false;

	private MyStreamActivity mInstance;

	private DataManager mDataManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		//setOverlayAction();
		//setContentView(R.layout.activity_my_stream);
		super.onCreate(savedInstanceState);
        ((MainActivity)getActivity()).lockDrawer();
        ((MainActivity)getActivity()).removeDrawerIconAndPreference();
        ((MainActivity)getActivity()).setNeedToOpenSearchActivity(false);
		setNavigationOnClickListener();

		/*AlertActivity.isMessage = false;
		mInstance = this;
		mContext = this;

		mFragmentManager = getSupportFragmentManager();
		if (savedInstanceState == null) {
			FragmentTransaction fragmentTransaction = mFragmentManager
					.beginTransaction();
			SocialMyStreamFragment radioFragment = new SocialMyStreamFragment();
			radioFragment.setOnMediaItemOptionSelectedListener(this);
			fragmentTransaction
					.add(R.id.main_fragmant_container, radioFragment);
			fragmentTransaction.commit();
		}*/
	}

	private void setNavigationOnClickListener(){
		if(getActivity()!=null && ((MainActivity) getActivity()).mToolbar!=null)
			((MainActivity) getActivity()).mToolbar.setNavigationOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					onBackPressed();
				}
		});
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.activity_my_stream,
                container, false);
        activity=getActivity();

        AlertActivity.isMessage = false;
        mInstance = this;


        mFragmentManager = getActivity().getSupportFragmentManager();
        if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = mFragmentManager
                    .beginTransaction();
            SocialMyStreamFragment radioFragment = new SocialMyStreamFragment();
            radioFragment.setMyStreamActivity(this);
            radioFragment.setOnMediaItemOptionSelectedListener(this);
            fragmentTransaction
                    .add(R.id.main_fragmant_container_new, radioFragment);
            fragmentTransaction.disallowAddToBackStack();
			if(Constants.IS_COMMITALLOWSTATE)
				fragmentTransaction.commitAllowingStateLoss();
			else
				fragmentTransaction.commit();
        }

        return rootView;
    }


    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/*int itemId = item.getItemId();s
		if (itemId == android.R.id.home) {
			finish();
			return true;
		} else*/
			return super.onOptionsItemSelected(item);
	}

	@Override
	public void onStart() {
		super.onStart();
		Analytics.startSession(activity, this);
		Analytics.onPageView();
        ((MainActivity)getActivity()).showBackButtonWithTitleMediaDetail(
				Utils.getMultilanguageTextLayOut(
						activity,
						Utils.getMultilanguageText(
								activity,
								getResources()
										.getString(
												R.string.main_actionbar_settings_menu_item_my_stream))),
				"");
	}

	@Override
	public void onStop() {
		super.onStop();
		Analytics.onEndSession(activity);
	}

	@Override
	public void onResume() {
		HungamaApplication.activityResumed();
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(activity.getBaseContext());
		if (mApplicationConfigurations.isSongCatched()) {
            ((MainActivity)getActivity()).openOfflineGuide();
		}
		super.onResume();
        ((MainActivity)getActivity()).showBackButtonWithTitleMediaDetail(
                Utils.getMultilanguageTextLayOut(
                        activity,
                        Utils.getMultilanguageText(
                                activity,
                                getResources()
                                        .getString(
                                                R.string.main_actionbar_settings_menu_item_my_stream))),
                "");
	}

	@Override
	public void onPause() {
		HungamaApplication.activityPaused();
		super.onPause();
	}

	/*@Override
	protected NavigationItem getNavigationItem() {
		return NavigationItem.MY_STREAM;
	}
*/
	// public void showDetailsOfRadio(final MediaItem mediaItem,
	// final MediaCategoryType mediaCategoryType) {
	//
	// if (mediaCategoryType == MediaCategoryType.TOP_ARTISTS_RADIO) {
	// // Flurry report: Top Artist Radio - Artist name
	// Map<String, String> reportMap = new HashMap<String, String>();
	// reportMap.put(FlurryConstants.FlurryKeys.ArtistName.toString(),
	// mediaItem.getTitle());
	// Analytics.logEvent(
	// FlurryConstants.FlurryEventName.TopArtistRadio.toString(),
	// reportMap);
	//
	// } else if (mediaCategoryType == MediaCategoryType.LIVE_STATIONS) {
	// // Flurry report: Top Artist Radio - Artist name
	// Map<String, String> reportMap = new HashMap<String, String>();
	// reportMap.put(FlurryConstants.FlurryKeys.Title.toString(),
	// mediaItem.getTitle());
	// Analytics.logEvent(
	// FlurryConstants.FlurryEventName.LiveRadio.toString(),
	// reportMap);
	// }
	//
	// PlayerBarFragment playerBarFragment = getPlayerBar();
	//
	// if (playerBarFragment.getPlayMode() == PlayMode.MUSIC
	// && (playerBarFragment.isPlaying() || playerBarFragment
	// .isLoading())) {
	//
	// // show dialog.
	// CustomAlertDialog builder = new CustomAlertDialog(this);
	// builder.setMessage(
	// Utils.getMultilanguageText(mInstance, getResources()
	// .getString(R.string.radio_confirm_dialog_title)))
	// .setCancelable(true)
	// .setPositiveButton(
	// Utils.getMultilanguageText(
	// mInstance,
	// getResources()
	// .getString(
	// R.string.radio_confirm_dialog_play_channel)),
	// new DialogInterface.OnClickListener() {
	//
	// public void onClick(DialogInterface dialog,
	// int id) {
	//
	// // shows the radio details for the given
	// // item.
	// showDetailsOfRadioHelper(mediaItem,
	// mediaCategoryType);
	// }
	//
	// })
	// .setNegativeButton(
	// Utils.getMultilanguageText(
	// mInstance,
	// getResources()
	// .getString(
	// R.string.radio_confirm_dialog_save_them))
	// + " & "
	// + Utils.getMultilanguageText(
	// mInstance,
	// getResources()
	// .getString(
	// R.string.radio_confirm_dialog_save_them2)),
	// new DialogInterface.OnClickListener() {
	//
	// public void onClick(DialogInterface dialog,
	// int id) {
	// try {
	// // open the playlists dialog.
	// List<Track> tracks = getPlayerBar()
	// .getCurrentPlayingList();
	// boolean isFromLoadMenu = false;
	// PlaylistDialogFragment playlistDialogFragment = PlaylistDialogFragment
	// .newInstance(
	// tracks,
	// isFromLoadMenu,
	// FlurryConstants.FlurryPlaylists.Radio
	// .toString());
	//
	// playlistDialogFragment
	// .setOnPlaylistPerformActionListener(new OnPlaylistPerformActionListener()
	// {
	//
	// @Override
	// public void onSuccessed() {
	// // shows the radio
	// // details
	// // for the given item.
	// showDetailsOfRadioHelper(
	// mediaItem,
	// mediaCategoryType);
	// }
	//
	// @Override
	// public void onFailed() {
	// if (mIfDetailsRequestedImmediately) {
	// finish();
	// }
	// }
	//
	// @Override
	// public void onCanceled() {
	// if (mIfDetailsRequestedImmediately) {
	// finish();
	// }
	// }
	// });
	//
	// playlistDialogFragment
	// .show(mFragmentManager,
	// PlaylistDialogFragment.FRAGMENT_TAG);
	// } catch (Exception e) {
	// Logger.printStackTrace(e);
	// }
	// }
	// });
	// builder.show();
	// } else {
	// // shows the radio details for the given item.
	// showDetailsOfRadioHelper(mediaItem, mediaCategoryType);
	// }
	//
	// }

	/*@Override
	public void onBackPressed() {

		if (mDrawerLayout != null
				&& mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
			mDrawerLayout.closeDrawers();
			return;
		}
		if (mPlayerBarFragment != null && mPlayerBarFragment.isContentOpened()) {
			// Minimize player
			if (!mPlayerBarFragment.removeAllFragments())
				mPlayerBarFragment.closeContent();
		} else {
			finish();
		}
	}*/

    boolean isNeedToClose = true;
    private boolean results;
    @Override
    public boolean onBackPressed() {
        try {
            if (((MainActivity)getActivity()).mPlayerBarFragment != null
                    && ((MainActivity)getActivity()).mPlayerBarFragment.isContentOpened()) {
                // Minimize player
                if (!((MainActivity)getActivity()).mPlayerBarFragment.removeAllFragments())
                    ((MainActivity)getActivity()).mPlayerBarFragment.closeContent();
                return true;
            } else if (((MainActivity)getActivity()).mPlayerBarFragment != null
                    && !((MainActivity)getActivity()).mPlayerBarFragment.collapsedPanel1()) {
                int count = getActivity().getSupportFragmentManager()
                        .getBackStackEntryCount();
                if (count > 0) {
                    results = false;

                    getActivity().getSupportFragmentManager().popBackStack();
                    isNeedToClose = true;

                    return true;
                } else {

                    getActivity().getSupportFragmentManager().popBackStack();

                    return true;
                }
            }
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
        return false;
    }

    @Override
    public void setTitle(boolean needOnlyHight, boolean needToSetTitle) {
        ((MainActivity)getActivity()).showBackButtonWithTitleMediaDetail(
                Utils.getMultilanguageTextLayOut(
                        activity,
                        Utils.getMultilanguageText(
                                activity,
                                getResources()
                                        .getString(
                                                R.string.main_actionbar_settings_menu_item_my_stream))),
                "");
		setNavigationOnClickListener();
		Utils.setToolbarColor(((MainActivity) getActivity()));
    }

    private void showDetailsOfRadioHelper(MediaItem mediaItem,
			MediaCategoryType mediaCategoryType) {
		FragmentManager fragmentManager = getChildFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();

		RadioDetailsFragment radioDetailsFragment = new RadioDetailsFragment();
		// sets it's arguments.
		Bundle data = new Bundle();
		data.putSerializable(RadioDetailsFragment.EXTRA_MEDIA_ITEM,
				(Serializable) mediaItem);
		data.putSerializable(RadioDetailsFragment.EXTRA_CATEGORY_TYPE,
				(Serializable) mediaCategoryType);
		data.putBoolean(RadioDetailsFragment.EXTRA_DO_SHOW_TITLE_BAR, true);
		data.putBoolean(RadioDetailsFragment.EXTRA_AUTO_PLAY, true);
		data.putBoolean(RadioDetailsFragment.IS_FOR_PLAYER_BAR, false);

		radioDetailsFragment.setArguments(data);
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit, R.anim.slide_right_enter,
				R.anim.slide_right_exit);
		/*
		 * We want when the user goes to the Radio Details from external
		 * Activity and presses "back"to exit the whole activity.
		 */
		if (mIfDetailsRequestedImmediately) {
			fragmentTransaction.add(R.id.main_fragmant_container,
					radioDetailsFragment);
			fragmentTransaction.commit();

		} else {
			try {
				fragmentTransaction.replace(R.id.main_fragmant_container,
						radioDetailsFragment);
				fragmentTransaction.addToBackStack(null);
				fragmentTransaction.commit();
			} catch (Exception e) {
			}
		}

	}

	/*@Override
	public void finish() {
		mInstance = null;
        super.finish();

	}*/

	@Override
	public void onDestroy() {
		mInstance = null;
		super.onDestroy();
	}


    public void openProfile(Bundle arguments){
        ProfileActivity mediaDetailsFragment = new ProfileActivity();
        mediaDetailsFragment.setArguments(arguments);
        FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager
                .beginTransaction();
//		fragmentTransaction.setCustomAnimations(
//				R.anim.slide_left_enter,
//				R.anim.slide_left_exit);
        fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
				mediaDetailsFragment, "ProfileActivity");
        fragmentTransaction.addToBackStack("ProfileActivity");

		if(Constants.IS_COMMITALLOWSTATE)
			fragmentTransaction.commitAllowingStateLoss();
		else
			fragmentTransaction.commit();
    }


	// public void forceFinish() {
	// if (mInstance != null)
	// mInstance.finish();
	// mInstance = null;
	// }

	// ======================================================
	// ACTIVITY'S EVENT LISTENERS - HOME.
	// ======================================================

	@Override
	public void onMediaItemOptionPlayNowSelected(MediaItem mediaItem,
			int position) {
		Logger.i(TAG, "Play Now: " + mediaItem.getId());
		if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				Track track = new Track(mediaItem.getId(),
						mediaItem.getTitle(), mediaItem.getAlbumName(),
						mediaItem.getArtistName(), mediaItem.getImageUrl(),
						mediaItem.getBigImageUrl(), mediaItem.getImages(),
						mediaItem.getAlbumId());
				List<Track> tracks = new ArrayList<Track>();
				tracks.add(track);
                ((MainActivity)getActivity()).mPlayerBarFragment.playNow(tracks, null, null);
			} else {
				boolean isCached = false;

				String mediaDeatils = null;
				if (mediaItem.getMediaType() == MediaType.ALBUM)
					mediaDeatils = DBOHandler.getAlbumDetails(mContext, ""
							+ mediaItem.getId());
				else if (mediaItem.getMediaType() == MediaType.PLAYLIST)
					mediaDeatils = DBOHandler.getPlaylistDetails(mContext, ""
							+ mediaItem.getId());
				if (mediaDeatils != null && mediaDeatils.length() > 0) {
					MediaDetailsOperation mediaDetailsOperation = new MediaDetailsOperation(
							"", "", "", mediaItem,
							PlayerOption.OPTION_PLAY_NOW, null);
					try {
						Response res = new Response();
						res.response = mediaDeatils;
						res.responseCode = CommunicationManager.RESPONSE_SUCCESS_200;
						onSuccess(mediaDetailsOperation.getOperationId(),
								mediaDetailsOperation.parseResponse(res));
					} catch (InvalidRequestParametersException e) {
						e.printStackTrace();
					} catch (InvalidRequestTokenException e) {
						e.printStackTrace();
					} catch (InvalidResponseDataException e) {
						e.printStackTrace();
					} catch (OperationCancelledException e) {
						e.printStackTrace();
					}
					isCached = true;
				}

				if (!isCached) {
					mDataManager.getMediaDetails(mediaItem,
							PlayerOption.OPTION_PLAY_NOW, this);
				}
			}
		}
	}

	@Override
	public void onMediaItemOptionPlayNextSelected(MediaItem mediaItem,
			int position) {
		Logger.i(TAG, "Play Next: " + mediaItem.getId());
		if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				Track track = new Track(mediaItem.getId(),
						mediaItem.getTitle(), mediaItem.getAlbumName(),
						mediaItem.getArtistName(), mediaItem.getImageUrl(),
						mediaItem.getBigImageUrl(), mediaItem.getImages(),
						mediaItem.getAlbumId());
				List<Track> tracks = new ArrayList<Track>();
				tracks.add(track);
                ((MainActivity)getActivity()).mPlayerBarFragment.playNext(tracks);
			} else {
				boolean isCached = false;

				String mediaDeatils = null;
				if (mediaItem.getMediaType() == MediaType.ALBUM)
					mediaDeatils = DBOHandler.getAlbumDetails(mContext, ""
							+ mediaItem.getId());
				else if (mediaItem.getMediaType() == MediaType.PLAYLIST)
					mediaDeatils = DBOHandler.getPlaylistDetails(mContext, ""
							+ mediaItem.getId());
				if (mediaDeatils != null && mediaDeatils.length() > 0) {
					MediaDetailsOperation mediaDetailsOperation = new MediaDetailsOperation(
							"", "", "", mediaItem,
							PlayerOption.OPTION_PLAY_NEXT, null);
					try {
						Response res = new Response();
						res.response = mediaDeatils;
						res.responseCode = CommunicationManager.RESPONSE_SUCCESS_200;

						onSuccess(mediaDetailsOperation.getOperationId(),
								mediaDetailsOperation.parseResponse(res));
					} catch (InvalidRequestParametersException e) {
						e.printStackTrace();
					} catch (InvalidRequestTokenException e) {
						e.printStackTrace();
					} catch (InvalidResponseDataException e) {
						e.printStackTrace();
					} catch (OperationCancelledException e) {
						e.printStackTrace();
					}
					isCached = true;
				}

				if (!isCached) {
					mDataManager.getMediaDetails(mediaItem,
							PlayerOption.OPTION_PLAY_NEXT, this);
				}
			}
		}
	}

	@Override
	public void onMediaItemOptionAddToQueueSelected(MediaItem mediaItem,
			int position) {
		Logger.i(TAG, "Add to queue: " + mediaItem.getId());
		if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				Track track = new Track(mediaItem.getId(),
						mediaItem.getTitle(), mediaItem.getAlbumName(),
						mediaItem.getArtistName(), mediaItem.getImageUrl(),
						mediaItem.getBigImageUrl(), mediaItem.getImages(),
						mediaItem.getAlbumId());
				List<Track> tracks = new ArrayList<Track>();
				tracks.add(track);
                ((MainActivity)getActivity()).mPlayerBarFragment.addToQueue(tracks, null, null);
			} else {
				boolean isCached = false;

				String mediaDeatils = null;
				if (mediaItem.getMediaType() == MediaType.ALBUM)
					mediaDeatils = DBOHandler.getAlbumDetails(mContext, ""
							+ mediaItem.getId());
				else if (mediaItem.getMediaType() == MediaType.PLAYLIST)
					mediaDeatils = DBOHandler.getPlaylistDetails(mContext, ""
							+ mediaItem.getId());
				if (mediaDeatils != null && mediaDeatils.length() > 0) {
					MediaDetailsOperation mediaDetailsOperation = new MediaDetailsOperation(
							"", "", "", mediaItem,
							PlayerOption.OPTION_ADD_TO_QUEUE, null);
					try {
						Response res = new Response();
						res.response = mediaDeatils;
						res.responseCode = CommunicationManager.RESPONSE_SUCCESS_200;

						onSuccess(mediaDetailsOperation.getOperationId(),
								mediaDetailsOperation.parseResponse(res));
					} catch (InvalidRequestParametersException e) {
						e.printStackTrace();
					} catch (InvalidRequestTokenException e) {
						e.printStackTrace();
					} catch (InvalidResponseDataException e) {
						e.printStackTrace();
					} catch (OperationCancelledException e) {
						e.printStackTrace();
					}
					isCached = true;
				}

				if (!isCached) {
					mDataManager.getMediaDetails(mediaItem,
							PlayerOption.OPTION_ADD_TO_QUEUE, this);
				}
			}
		}
	}

	@Override
	public void onMediaItemOptionShowDetailsSelected(MediaItem mediaItem,
			int position) {
		Logger.i(TAG, "Show Details: " + mediaItem.getId());

		Intent intent = null;
		String mFlurrySubSectionDescription = "No sub section description";
		if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
//			intent = new Intent(activity, MediaDetailsActivity.class);
//			intent.putExtra(MediaDetailsActivity.EXTRA_MEDIA_ITEM,
//					(Serializable) mediaItem);
//			intent.putExtra(MediaDetailsActivity.FLURRY_SOURCE_SECTION,
//					mFlurrySubSectionDescription);
//			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			Bundle bundle = new Bundle();
			/*if (getIntent().getString("video_in_audio_content_id") != null) {
				intent.putExtra("video_in_audio_content_id", getIntent()
						.getString("video_in_audio_content_id"));
               // getIntent().removeExtra("video_in_audio_content_id");
				intent.putExtra("add_to_queue", true);

			} else*/ if (AlertActivity.isMessage || position == -1) {
				bundle.putBoolean("add_to_queue", true);
			} else if (position == -2
					&& mediaItem.getMediaType() == MediaType.TRACK) {
				Track track = new Track(mediaItem.getId(),
						mediaItem.getTitle(), mediaItem.getAlbumName(),
						mediaItem.getArtistName(), mediaItem.getImageUrl(),
						mediaItem.getBigImageUrl(), mediaItem.getImages(),
						mediaItem.getAlbumId());
				List<Track> tracks = new ArrayList<Track>();
				tracks.add(track);
                ((MainActivity)getActivity()).mPlayerBarFragment.playNow(tracks, null, null);
			}

			FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();
			FragmentTransaction fragmentTransaction = mFragmentManager
					.beginTransaction();

			MediaDetailsActivityNew mediaDetailsFragment = new MediaDetailsActivityNew();

			bundle.putSerializable(MediaDetailsActivity.EXTRA_MEDIA_ITEM,
					(Serializable) mediaItem);
			bundle.putString(MediaDetailsActivity.FLURRY_SOURCE_SECTION,
					mFlurrySubSectionDescription);

			mediaDetailsFragment.setArguments(bundle);

			fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
					mediaDetailsFragment, "MediaDetailsActivitycollection");
			fragmentTransaction.addToBackStack("MediaDetailsActivitycollection");
			if(Constants.IS_COMMITALLOWSTATE)
				fragmentTransaction.commitAllowingStateLoss();
			else
				fragmentTransaction.commit();
			return;
		} else {
			intent = new Intent(activity, VideoActivity.class);
			intent.putExtra(VideoActivity.EXTRA_MEDIA_ITEM_VIDEO,
					(Serializable) mediaItem);
			//getIntent().removeExtra("video_content_id");
		}

		startActivity(intent);
	}

	@Override
	public void onMediaItemOptionSaveOfflineSelected(MediaItem mediaItem,
			int position) {
		Logger.i(TAG, "Save Offline: " + mediaItem.getId());
		if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				Track track = new Track(mediaItem.getId(),
						mediaItem.getTitle(), mediaItem.getAlbumName(),
						mediaItem.getArtistName(), mediaItem.getImageUrl(),
						mediaItem.getBigImageUrl(), mediaItem.getImages(),
						mediaItem.getAlbumId());
				CacheManager.saveOfflineAction(activity, mediaItem, track);
				Utils.saveOfflineFlurryEvent(activity,
						FlurryConstants.FlurryCaching.LongPressMenuSong
								.toString(), mediaItem);
			} else if (mediaItem.getMediaType() == MediaType.ALBUM
					|| mediaItem.getMediaType() == MediaType.PLAYLIST) {
				if (MediaCachingTaskNew.isEnabled)
					mDataManager.getMediaDetails(mediaItem,
							PlayerOption.OPTION_SAVE_OFFLINE, this);
				else
					CacheManager.saveOfflineAction(activity, mediaItem, null);

				if (mediaItem.getMediaType() == MediaType.ALBUM) {
					Utils.saveOfflineFlurryEvent(activity,
							FlurryConstants.FlurryCaching.LongPressMenuAlbum
									.toString(), mediaItem);
				} else {
					Utils.saveOfflineFlurryEvent(activity,
							FlurryConstants.FlurryCaching.LongPressMenuPlaylist
									.toString(), mediaItem);
				}
			}
		} else {
			CacheManager.saveOfflineAction(activity, mediaItem, null);
			Utils.saveOfflineFlurryEvent(
					activity,
					FlurryConstants.FlurryCaching.LongPressMenuVideo.toString(),
					mediaItem);
		}
	}

	@Override
	public void onMediaItemOptionRemoveSelected(MediaItem mediaItem,
			int position) {
		Logger.i(TAG, "Remove item: " + mediaItem.getId());
	}

	@Override
	public void onStart(int operationId) {
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
	}

	@Override
	public void onMediaItemOptionPlayAndOpenSelected(MediaItem mediaItem,
			int position) {
	}
}
