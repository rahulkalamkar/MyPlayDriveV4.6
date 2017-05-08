package com.hungama.myplay.activity.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.HungamaApplication;

import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.ActionDefinition;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.MediaCachingTaskNew;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.BaseHungamaResponse;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.PlayerOption;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.operations.hungama.RadioTopArtistSongsOperation;
import com.hungama.myplay.activity.operations.hungama.RemoveFromFavoriteOperation;
import com.hungama.myplay.activity.player.PlayMode;
import com.hungama.myplay.activity.ui.fragments.BackHandledFragment;
import com.hungama.myplay.activity.ui.fragments.FavoritesFragment;
import com.hungama.myplay.activity.ui.fragments.FavoritesFragment.OnMediaItemsLoadedListener;
import com.hungama.myplay.activity.ui.fragments.MediaDetailsFragment;
import com.hungama.myplay.activity.ui.fragments.MediaTileGridFragment;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.ScreenLockStatus;
import com.hungama.myplay.activity.util.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class FavoritesActivity extends BackHandledFragment implements
		CommunicationOperationListener, OnMediaItemOptionSelectedListener,
		OnMediaItemsLoadedListener {

	private static final String TAG = "FavoritesActivity";

	private DataManager mDataManager;
	private FragmentManager mFragmentManager;
    private Activity activity;
	// private PlayerBarFragment mPlayerBar;

	private LinearLayout favSongs;
	private LinearLayout favAlbums;
	private LinearLayout favPlaylists;
	private LinearLayout favVideos;
	private LinearLayout favArtists;

	private TextView mTitle;
    String title="";
	private CacheStateReceiver cacheStateReceiver;
	private List<MediaItem> mMediaItems;

	private Stack<String> stack_text = new Stack<String>();

	private ImageView menu;

	public static final int FILTER_OPTIONS_ACTIVITY_RESULT_CODE_Fav = 1093;
	boolean isOncreate;

    private LocalBroadcastManager mLocalBroadcastManager;

	// ======================================================
	// ACTIVITY'S LIFECYCLE.
	// ======================================================

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity)getActivity()).lockDrawer();
        ((MainActivity)getActivity()).removeDrawerIconAndPreference();
        ((MainActivity)getActivity()).setNeedToOpenSearchActivity(false);


		//setOverlayAction();
		/*setContentView(R.layout.activity_main_with_title);
	    	super.onCreate(savedInstanceState);
       // activity=getActivity();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
		mDataManager = DataManager.getInstance(getApplicationContext());
		mFragmentManager = getSupportFragmentManager();
		// mPlayerBar = getPlayerBar();

		// adjust the title.
		mTitle = (TextView) findViewById(R.id.main_title_bar_text);
		mTitle.setText(Utils.TEXT_EMPTY);

		// shows the favorite type selection dialog.
		// showFavoritesMediaTypeSelectionDialog();
		if (getIntent().getBooleanExtra("fav_songs", false)) {
			favSongs.performClick();
		} else if (getIntent().getBooleanExtra("fav_albums", false)) {
			favAlbums.performClick();
		} else if (getIntent().getBooleanExtra("fav_playlists", false)) {
			favPlaylists.performClick();
		} else if (getIntent().getBooleanExtra("fav_videos", false)) {
			favVideos.performClick();
		} else if (getIntent().getBooleanExtra("fav_artists", false)) {
			favArtists.performClick();
		}

		// if(cacheStateReceiver==null && Logger.isSaveOffline &&
		// CacheManager.isProUser(this)){
		// cacheStateReceiver = new CacheStateReceiver();
		// IntentFilter filter = new IntentFilter();
		// filter.addAction(CacheManager.ACTION_CACHE_STATE_UPDATED);
		// filter.addAction(CacheManager.ACTION_TRACK_CACHED);
		// filter.addAction(CacheManager.ACTION_UPDATED_CACHE);
		// registerReceiver(cacheStateReceiver, filter);
		// }

		findViewById(R.id.main_title_bar).setVisibility(View.GONE);

		menu = (ImageView) findViewById(R.id.menu);
		menu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ScreenLockStatus.getInstance(FavoritesActivity.this)
						.dontShowAd();
				Logger.s("SatelliteMenu onClick");
				Intent i = new Intent(FavoritesActivity.this,
						TransperentActivityMyFavorite.class);

				startActivityForResult(i,
						FILTER_OPTIONS_ACTIVITY_RESULT_CODE_Fav);

				menu.setTag("sethidden");
				menu.setVisibility(View.INVISIBLE);
				// overridePendingTransition(R.anim.pull_up_from_bottom,R.anim.push_out_to_bottom_discover);
			}
		});

		ApplicationConfigurations appConfig = ApplicationConfigurations
				.getInstance(this);
		appConfig.setFavSelection(-1);

		// new Handler().postDelayed(new Runnable() {
		//
		// @Override
		// public void run() {
		// TODO Auto-generated method stub
		isOncreate = true;
		Intent i = new Intent(FavoritesActivity.this,
				TransperentActivityMyFavorite.class);
		startActivityForResult(i, FILTER_OPTIONS_ACTIVITY_RESULT_CODE_Fav);
		menu.setTag("sethidden");
		menu.setVisibility(View.INVISIBLE);
		// }
		// }, 10);*/


	}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.activity_main_with_title,
                container, false);
        activity=getActivity();

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(activity);
        mDataManager = DataManager.getInstance(activity);
        mFragmentManager = getChildFragmentManager();
        // mPlayerBar = getPlayerBar();

        // adjust the title.
        mTitle = (TextView) rootView.findViewById(R.id.main_title_bar_text);
        mTitle.setText(Utils.TEXT_EMPTY);

        // shows the favorite type selection dialog.
       /* // showFavoritesMediaTypeSelectionDialog();
        if (getIntent().getBooleanExtra("fav_songs", false)) {
            favSongs.performClick();
        } else if (getIntent().getBooleanExtra("fav_albums", false)) {
            favAlbums.performClick();
        } else if (getIntent().getBooleanExtra("fav_playlists", false)) {
            favPlaylists.performClick();
        } else if (getIntent().getBooleanExtra("fav_videos", false)) {
            favVideos.performClick();
        } else if (getIntent().getBooleanExtra("fav_artists", false)) {
            favArtists.performClick();
        }
*/


        rootView.findViewById(R.id.main_title_bar).setVisibility(View.GONE);

        menu = (ImageView) rootView.findViewById(R.id.menu);

        menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ScreenLockStatus.getInstance(activity)
                        .dontShowAd();
                Logger.s("SatelliteMenu onClick");
                Intent i = new Intent(activity,
                        TransperentActivityMyFavorite.class);

                startActivityForResult(i,
                        FILTER_OPTIONS_ACTIVITY_RESULT_CODE_Fav);

                menu.setTag("sethidden");
                menu.setVisibility(View.INVISIBLE);
                // overridePendingTransition(R.anim.pull_up_from_bottom,R.anim.push_out_to_bottom_discover);
            }
        });

        ApplicationConfigurations appConfig = ApplicationConfigurations
                .getInstance(activity);
        appConfig.setFavSelection(-1);

        // new Handler().postDelayed(new Runnable() {
        //
        // @Override
        // public void run() {
        // TODO Auto-generated method stub
        isOncreate = true;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(activity,
                        TransperentActivityMyFavorite.class);
                startActivityForResult(i, FILTER_OPTIONS_ACTIVITY_RESULT_CODE_Fav);
                menu.setTag("sethidden");
                menu.setVisibility(View.INVISIBLE);
            }
        }, 400);

		if(((MainActivity) getActivity()).mToolbar!=null)
			setNavigationListener();

		return rootView;
    }



        @Override
	public void onStart() {
		super.onStart();
            ((MainActivity)getActivity()).showBackButtonWithTitleMediaDetail(getString(R.string.my_favorite), "");
	}

	@Override
	public void onPause() {
		HungamaApplication.activityPaused();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();

		HungamaApplication.activityResumed();

		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(activity.getBaseContext());
		if (!isOncreate)
			if (menu != null && menu.getTag() != null) {
				menu.setTag(null);
				menu.setVisibility(View.VISIBLE);
			}
		isOncreate = false;
		if (mApplicationConfigurations.isSongCatched()) {
            ((MainActivity)getActivity()).openOfflineGuide();
		}
		if (cacheStateReceiver == null) {
			cacheStateReceiver = new CacheStateReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(CacheManager.ACTION_CACHE_STATE_UPDATED);
			filter.addAction(CacheManager.ACTION_TRACK_CACHED);
			filter.addAction(CacheManager.ACTION_UPDATED_CACHE);
            ((MainActivity)getActivity()).registerReceiver(cacheStateReceiver, filter);
		}



		if (stack_text != null && stack_text.size() > 0){
            ((MainActivity)getActivity()).showBackButtonWithTitleMediaDetail(stack_text.get(stack_text.size() - 1), "");
        }else if(title.equals("")){

            ((MainActivity)getActivity()).showBackButtonWithTitleMediaDetail(getString(R.string.my_favorite), "");
        }else{
            ((MainActivity)getActivity()).showBackButtonWithTitleMediaDetail(title, "");
        }



	}

/*
	@Override
	public NavigationItem getNavigationItem() {
		return NavigationItem.OTHER;
	}
*/

	// ======================================================
	// Helper Methods.
	// ======================================================

	/*private void showFavoritesMediaTypeSelectionDialog() {

		// set up custom dialog
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(getApplicationContext());
		// RelativeLayout root = (RelativeLayout) LayoutInflater.from(
		// FavoritesActivity.this).inflate(
		// R.layout.dialog_my_favorites_options, null);
		dialog.setContentView(R.layout.dialog_my_favorites_options);
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(dialog.getWindow().getDecorView(),
					FavoritesActivity.this);
		}
		dialog.setCancelable(true);

		dialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				finish();

			}
		});
		dialog.show();

		// sets the cancel button.
		ImageButton closeButton = (ImageButton) dialog
				.findViewById(R.id.long_click_custom_dialog_title_image);
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				finish();
			}
		});

		// sets the options buttons.
		favSongs = (LinearLayout) dialog
				.findViewById(R.id.long_click_custom_dialog_play_now_row1);
		favAlbums = (LinearLayout) dialog
				.findViewById(R.id.long_click_custom_dialog_add_to_queue_row2);
		favPlaylists = (LinearLayout) dialog
				.findViewById(R.id.long_click_custom_dialog_details_row3);
		favVideos = (LinearLayout) dialog
				.findViewById(R.id.long_click_custom_dialog_details_row4);
		favArtists = (LinearLayout) dialog
				.findViewById(R.id.long_click_custom_dialog_details_row5);

		// fav songs.
		favSongs.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showFavoriteFragmentFor(MediaType.TRACK);
				findViewById(R.id.main_title_bar_button_options).setVisibility(
						View.VISIBLE);
				dialog.dismiss();
			}
		});

		// fav albums.
		favAlbums.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showFavoriteFragmentFor(MediaType.ALBUM);
				dialog.dismiss();
				if (mMenu != null) {
					mMenu.clear();
					MenuInflater inflater = getMenuInflater();
					inflater.inflate(R.menu.menu_main_actionbar, mMenu);
				}
			}
		});

		// fav playlists.
		favPlaylists.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showFavoriteFragmentFor(MediaType.PLAYLIST);
				dialog.dismiss();
				if (mMenu != null) {
					mMenu.clear();
					MenuInflater inflater = getMenuInflater();
					inflater.inflate(R.menu.menu_main_actionbar, mMenu);
				}
			}
		});

		// fav videos.
		favVideos.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showFavoriteFragmentFor(MediaType.VIDEO);
				dialog.dismiss();
				if (mMenu != null) {
					mMenu.clear();
					MenuInflater inflater = getMenuInflater();
					inflater.inflate(R.menu.menu_main_actionbar, mMenu);
				}
			}
		});
		favArtists.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showFavoriteFragmentFor(MediaType.ARTIST);
				dialog.dismiss();
				if (mMenu != null) {
					mMenu.clear();
					MenuInflater inflater = getMenuInflater();
					inflater.inflate(R.menu.menu_main_actionbar, mMenu);
				}
			}
		});
	}


*/


	// String title_current;
	private void setTitle(MediaType mMediaType, int size) {
		// SetS title bar
		if (mMediaType == MediaType.ALBUM) {
			// showBackButtonWithTitle(Utils.getMultilanguageTextLayOut(this,
			// getResources().getString(
			// R.string.favorite_fragment_title_artists, size)), "");

			 title = getResources().getString(
					R.string.favorite_fragment_title_albums, size);
			title = Utils.getMultilanguageText(activity, title)
					+ " (" + size + ")";
            //((MainActivity)getActivity()).showBackButtonWithTitle(title, "");
            ((MainActivity)getActivity()).showBackButtonWithTitleMediaDetail(title, "");

			// if (!stack_text.contains(title))

			//stack_text.push(title);

			//mTitle.setText(title);
		} else if (mMediaType == MediaType.TRACK) {

			 title = getResources().getString(
					R.string.favorite_fragment_title_songs, size);
			title = Utils.getMultilanguageText(activity, title)
					+ " (" + size + ")";
            //((MainActivity)getActivity()).showBackButtonWithTitle(title, "");
			// if (!stack_text.contains(title))
            ((MainActivity)getActivity()).showBackButtonWithTitleMediaDetail(title, "");
            //stack_text.push(title);

			//mTitle.setText(title);
			// playAllButton.setVisibility(View.VISIBLE);
		} else if (mMediaType == MediaType.PLAYLIST) {
			 title = getResources().getString(
					R.string.favorite_fragment_title_playlists, size);
			title = Utils.getMultilanguageText(activity, title)
					+ " (" + size + ")";
           // ((MainActivity)getActivity()).showBackButtonWithTitle(title, "");
            ((MainActivity)getActivity()).showBackButtonWithTitleMediaDetail(title, "");
			// if (!stack_text.contains(title))
			//stack_text.push(title);

			//mTitle.setText(title);
		} else if (mMediaType == MediaType.VIDEO) {
			 title = getResources().getString(
					R.string.favorite_fragment_title_videos, size);
			title = Utils.getMultilanguageText(activity, title)
					+ " (" + size + ")";
            //((MainActivity)getActivity()).showBackButtonWithTitle(title, "");
            ((MainActivity)getActivity()).showBackButtonWithTitleMediaDetail(title, "");
			// if (!stack_text.contains(title))
			//stack_text.push(title);

			//mTitle.setText(title);
		} else if (mMediaType == MediaType.ARTIST) {
			 title = getResources().getString(
					R.string.favorite_fragment_title_artists, size);
			title = Utils.getMultilanguageText(activity, title)
					+ " (" + size + ")";
            //((MainActivity)getActivity()).showBackButtonWithTitle(title, "");
            ((MainActivity)getActivity()).showBackButtonWithTitleMediaDetail(title, "");
			// if (!stack_text.contains(title))
			//stack_text.push(title);

			//mTitle.setText(title);
		}
	}

	FavoritesFragment favoritesFragment;

	private void showFavoriteFragmentFor(MediaType mediaType) {

		favoritesFragment = new FavoritesFragment();
		favoritesFragment.setOnMediaItemOptionSelectedListener(this);
		favoritesFragment.setOnMediaItemsLoadedListener(this);

		Bundle arguments = new Bundle();
		arguments.putSerializable(
				FavoritesFragment.FRAGMENT_ARGUMENT_MEDIA_TYPE,
				(Serializable) mediaType);
		arguments.putString(FavoritesFragment.FRAGMENT_ARGUMENT_USER_ID,
				mDataManager.getApplicationConfigurations().getPartnerUserId());
		arguments.putString(
				MediaTileGridFragment.FLURRY_SUB_SECTION_DESCRIPTION,
				FlurryConstants.FlurrySubSectionDescription.MyFavorite
						.toString());
		favoritesFragment.setArguments(arguments);

		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();
		// fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
		// R.anim.slide_left_exit, R.anim.slide_right_enter,
		// R.anim.slide_right_exit);
		fragmentTransaction.replace(R.id.main_fragmant_container,
				favoritesFragment);
		if(Constants.IS_COMMITALLOWSTATE)
			fragmentTransaction.commitAllowingStateLoss();
		else
			fragmentTransaction.commit();
	}

	// ======================================================
	// Communication Operations events.
	// ======================================================

	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS)
			showLoadingDialog(R.string.application_dialog_loading_content);
		else if (operationId == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS_SONGS) {
			showLoadingDialog(R.string.application_dialog_loading_content);
		} else if (operationId == OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE) {
			showLoadingDialog(R.string.application_dialog_loading_content);
		}

	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
				MediaItem mediaItem = (MediaItem) responseObjects
						.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_ITEM);

				if (mediaItem.getMediaType() == MediaType.ALBUM
						|| mediaItem.getMediaType() == MediaType.PLAYLIST) {

					MediaSetDetails setDetails = (MediaSetDetails) responseObjects
							.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);
					PlayerOption playerOptions = (PlayerOption) responseObjects
							.get(MediaDetailsOperation.RESPONSE_KEY_PLAYER_OPTION);

					List<Track> tracks = setDetails.getTracks();
					if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
						for (Track track : tracks) {
							track.setTag(mediaItem);
						}
					} else if (mediaItem.getMediaType() == MediaType.ALBUM) {
						for (Track track : tracks) {
							track.setAlbumId(setDetails.getContentId());
						}
					}
					if (playerOptions == PlayerOption.OPTION_PLAY_NOW) {
                        ((MainActivity)getActivity()).mPlayerBarFragment.playNow(tracks, null, null);

					} else if (playerOptions == PlayerOption.OPTION_PLAY_NEXT) {
                        ((MainActivity)getActivity()).mPlayerBarFragment.playNext(tracks);

					} else if (playerOptions == PlayerOption.OPTION_ADD_TO_QUEUE) {
                        ((MainActivity)getActivity()).mPlayerBarFragment.addToQueue(tracks, null, null);
					} else if (playerOptions == PlayerOption.OPTION_SAVE_OFFLINE) {
						if (mediaItem.getMediaType() == MediaType.ALBUM) {
							for (Track track : tracks) {
								track.setTag(mediaItem);
							}
						}
						CacheManager.saveAllTracksOfflineAction(activity, tracks);
					}
				}
			} else if (operationId == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS_SONGS) {
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
					// starts to play.
					PlayerBarFragment.setArtistRadioId(mediaItem.getId());
					PlayerBarFragment.setArtistUserFav(userFav);
					PlayerBarFragment playerBar = ((MainActivity)getActivity()).getPlayerBar();
					playerBar
							.playRadio(radioTracks, PlayMode.TOP_ARTISTS_RADIO);

				} catch (Exception e) {
				}
			} else if (operationId == OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE) {
				try {
					BaseHungamaResponse removeFromFavoriteResponse = (BaseHungamaResponse) responseObjects
							.get(RemoveFromFavoriteOperation.RESULT_KEY_REMOVE_FROM_FAVORITE);

					// has the item been removed from favorites.
					if (removeFromFavoriteResponse.getCode() == MediaDetailsFragment.FAVORITE_SUCCESS) {
                        Intent intent = new Intent(
                                ActionDefinition.ACTION_MEDIA_ITEM_FAVORITE_STATE_CHANGED);
                        Bundle extras = new Bundle();
                        extras.putSerializable(
                                ActionDefinition.EXTRA_MEDIA_ITEM,
                                (Serializable) mediaItemToBeRemoved);
                        extras.putBoolean(
                                ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_IS_FAVORITE,
                                false);
                        extras.putInt(
                                ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_COUNT,
                                0);
                        intent.putExtras(extras);
                        mLocalBroadcastManager.sendBroadcast(intent);

						favoritesFragment
								.removeAndRefreshList(mediaItemToBeRemoved);
						Utils.makeText(activity,
								removeFromFavoriteResponse.getMessage(),
								Toast.LENGTH_LONG).show();
					} else {
						Utils.makeText(
								activity,
								getResources().getString(
										R.string.favorite_error_removing,
										mediaItemToBeRemoved.getTitle()),
								Toast.LENGTH_LONG).show();
					}
				} catch (Exception e) {
					Logger.e(getClass().getName() + ":601", e.toString());
				}

			}

		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		hideLoadingDialog();
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
			Logger.i(TAG, "Failed loading media details");
            ((MainActivity)getActivity()).internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
				@Override
				public void onRetryButtonClicked() {
					mDataManager.getMediaDetails(DataManager.mediaItem,
							DataManager.playerOption, DataManager.listener);
				}
			});
		}

		if (errorType != ErrorType.OPERATION_CANCELLED)
			hideLoadingDialog();
	}

	@Override
	public void onMediaItemsLoaded(final MediaType mediaType,
			final String userId, final List<MediaItem> mediaItems) {
		setTitle(mediaType, (mediaItems != null ? mediaItems.size() : 0));

		if (mediaType == MediaType.TRACK) {
			mMediaItems = mediaItems;
		}

		// playAllButton.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// if (mediaType == MediaType.TRACK) {
		// List<Track> tracks = new ArrayList<Track>();
		// for (MediaItem mediaItem : mediaItems) {
		// if (mediaItem.getMediaType() == MediaType.TRACK) {
		// Track track = new Track(mediaItem.getId(),
		// mediaItem.getTitle(), mediaItem
		// .getAlbumName(), mediaItem
		// .getArtistName(), mediaItem
		// .getImageUrl(), mediaItem
		// .getBigImageUrl(), mediaItem.getImages());
		// tracks.add(track);
		//
		// }
		// }
		//
		// mPlayerBarFragment.addToQueue(tracks, null, null);
		// }
		//
		// }
		// });
	}

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
				mDataManager.getMediaDetails(mediaItem,
						PlayerOption.OPTION_PLAY_NOW, this);
			}
		} else if (mediaItem.getMediaContentType() == MediaContentType.RADIO) {
			mDataManager.getRadioTopArtistSongs(mediaItem, this);
			// Intent intent = new Intent(this, RadioActivity.class);
			// intent.putExtra(RadioActivity.EXTRA_SHOW_DETAILS_MEDIA_ITEM,
			// (Serializable) mediaItem);
			// intent.putExtra(RadioActivity.EXTRA_SHOW_DETAILS_CATEGORY_TYPE,
			// (Serializable) MediaCategoryType.TOP_ARTISTS_RADIO);
			// startActivity(intent);
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
				mDataManager.getMediaDetails(mediaItem,
						PlayerOption.OPTION_PLAY_NEXT, this);
			}
		} else if (mediaItem.getMediaContentType() == MediaContentType.RADIO) {
			mDataManager.getRadioTopArtistSongs(mediaItem, this);
			//
			// Intent intent = new Intent(this, RadioActivity.class);
			// intent.putExtra(RadioActivity.EXTRA_SHOW_DETAILS_MEDIA_ITEM,
			// (Serializable) mediaItem);
			// intent.putExtra(RadioActivity.EXTRA_SHOW_DETAILS_CATEGORY_TYPE,
			// (Serializable) MediaCategoryType.TOP_ARTISTS_RADIO);
			// startActivity(intent);
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
				mDataManager.getMediaDetails(mediaItem,
						PlayerOption.OPTION_ADD_TO_QUEUE, this);
			}
		} else if (mediaItem.getMediaContentType() == MediaContentType.RADIO) {
			mDataManager.getRadioTopArtistSongs(mediaItem, this);
			// Intent intent = new Intent(this, RadioActivity.class);
			// intent.putExtra(RadioActivity.EXTRA_SHOW_DETAILS_MEDIA_ITEM,
			// (Serializable) mediaItem);
			// intent.putExtra(RadioActivity.EXTRA_SHOW_DETAILS_CATEGORY_TYPE,
			// (Serializable) MediaCategoryType.TOP_ARTISTS_RADIO);
			// startActivity(intent);
		}
	}

	@Override
	public void onMediaItemOptionShowDetailsSelected(MediaItem mediaItem,
			int position) {
		Logger.i(TAG, "Show Details: " + mediaItem.getId());
		Intent intent = null;
		if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {

			FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();
			FragmentTransaction fragmentTransaction = mFragmentManager
					.beginTransaction();

			MediaDetailsActivityNew mediaDetailsFragment = new MediaDetailsActivityNew();

			Bundle bundle = new Bundle();
			bundle.putSerializable(MediaDetailsActivity.EXTRA_MEDIA_ITEM,
					(Serializable) mediaItem);
			bundle.putString(MediaDetailsActivity.FLURRY_SOURCE_SECTION,
					FlurryConstants.FlurrySourceSection.Favorites.toString());

			mediaDetailsFragment.setArguments(bundle);

			fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
					mediaDetailsFragment, "MediaDetailsActivitySearch111");
			fragmentTransaction.addToBackStack("MediaDetailsActivitySearch111");

			if(Constants.IS_COMMITALLOWSTATE)
				fragmentTransaction.commitAllowingStateLoss();
			else
				fragmentTransaction.commit();

//			intent = new Intent(activity, MediaDetailsActivity.class);
//			intent.putExtra(MediaDetailsActivity.EXTRA_MEDIA_ITEM,
//					(Serializable) mediaItem);
//			intent.putExtra(MediaDetailsActivity.FLURRY_SOURCE_SECTION,
//					FlurryConstants.FlurrySourceSection.Favorites.toString());
//			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//			startActivity(intent);

		} else if (mediaItem.getMediaContentType() == MediaContentType.RADIO) {
			mDataManager.getRadioTopArtistSongs(mediaItem, this);

		} else {
			intent = new Intent(activity, VideoActivity.class);
			if (favoritesFragment != null) {
				try {
					intent.putExtra(VideoActivity.EXTRA_MEDIA_LIST_VIDEO,
							(Serializable) favoritesFragment.mMediaItems);
					intent.putExtra(VideoActivity.EXTRA_MEDIA_POS_VIDEO,
							favoritesFragment.mMediaItems.indexOf(mediaItem));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			intent.putExtra(VideoActivity.EXTRA_MEDIA_ITEM_VIDEO,
					(Serializable) mediaItem);

			intent.putExtra(MediaDetailsActivity.FLURRY_SOURCE_SECTION,
					FlurryConstants.FlurrySourceSection.Favorites.toString());
			startActivity(intent);

		}

		// intent.putExtra(MediaDetailsActivity.FLURRY_SOURCE_SECTION,
		// FlurryConstants.FlurrySourceSection.Favorites.toString());
		// startActivity(intent);
	}

	MediaItem mediaItemToBeRemoved;

	@Override
	public void onMediaItemOptionRemoveSelected(MediaItem mediaItem,
			int position) {
		Logger.i(TAG, "Remove item: " + mediaItem.getId());
		mediaItemToBeRemoved = mediaItem;
		mDataManager.removeFromFavorites(String.valueOf(mediaItem.getId()),
				mediaItem.getMediaType().toString(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener
	 * #
	 * onMediaItemOptionSaveOfflineSelected(com.hungama.myplay.activity.data.dao
	 * .hungama.MediaItem, int)
	 */
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
				// new MediaCachingTask(this, mediaItem, track).execute();
				CacheManager.saveOfflineAction(activity, mediaItem, track);
				Utils.saveOfflineFlurryEvent(activity,
						FlurryConstants.FlurryCaching.LongPressMenuSong
								.toString(), mediaItem);
			} else if (mediaItem.getMediaType() == MediaType.ALBUM
					|| mediaItem.getMediaType() == MediaType.PLAYLIST) {
				// new MediaCachingTask(this, mediaItem, null).execute();
				if (MediaCachingTaskNew.isEnabled)
					mDataManager.getMediaDetails(mediaItem,
							PlayerOption.OPTION_SAVE_OFFLINE, this);
				else
					CacheManager.saveOfflineAction(activity, mediaItem, null);
				if (mediaItem.getMediaType() == MediaType.ALBUM)
					Utils.saveOfflineFlurryEvent(activity,
							FlurryConstants.FlurryCaching.LongPressMenuAlbum
									.toString(), mediaItem);
				else
					Utils.saveOfflineFlurryEvent(activity,
							FlurryConstants.FlurryCaching.LongPressMenuPlaylist
									.toString(), mediaItem);
			}
		}else {
			CacheManager.saveOfflineAction(activity, mediaItem, null);
			Utils.saveOfflineFlurryEvent(
					activity,
					FlurryConstants.FlurryCaching.LongPressMenuVideo.toString(),
					mediaItem);
		}
	}

	class CacheStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			Logger.s("========================= cachestateupdatereceived ========"
					+ arg1.getAction());
			if (arg1.getAction()
					.equals(CacheManager.ACTION_CACHE_STATE_UPDATED)
					|| arg1.getAction()
							.equals(CacheManager.ACTION_TRACK_CACHED)) {
				try {
					FavoritesFragment fragment = (FavoritesFragment) mFragmentManager
							.findFragmentById(R.id.main_fragmant_container);
					if (fragment != null) {
						fragment.updateMediaItemCacheState();
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			} else if (arg1.getAction().equals(
					CacheManager.ACTION_UPDATED_CACHE)) {
			}
		}
	}

	public void onDestroy() {
		super.onDestroy();
		if (cacheStateReceiver != null)
            ((MainActivity)getActivity()).unregisterReceiver(cacheStateReceiver);
		cacheStateReceiver = null;
	}

	// public void onOptionsClicked(View view) {
	// if (view.isSelected()) {
	// // closes the option list.
	// view.setSelected(false);
	// view.setBackgroundResource(0);
	// ((LinearLayout) findViewById(R.id.favorite_list_options))
	// .setVisibility(View.GONE);
	// } else {
	// // opens the option list.
	// view.setSelected(true);
	// view.setBackgroundResource(R.color.black);
	// ((LinearLayout) findViewById(R.id.favorite_list_options))
	// .setVisibility(View.VISIBLE);
	// }
	// }

	// public void onOptionsItemSaveAllOfflineClicked(View view) {
	// if (mMediaItems != null && mMediaItems.size() > 0) {
	// List<Track> mTracks = new ArrayList<Track>();
	// for (MediaItem mediaItem : mMediaItems) {
	// if (mediaItem.getMediaType() == MediaType.TRACK) {
	// Track track = new Track(mediaItem.getId(),
	// mediaItem.getTitle(), mediaItem.getAlbumName(),
	// mediaItem.getArtistName(), mediaItem.getImageUrl(),
	// mediaItem.getBigImageUrl(), mediaItem.getImages(),
	// mediaItem.getAlbumId());
	// // new MediaCachingTask(getApplicationContext(), mediaItem,
	// // track).execute();
	// // CacheManager.saveOfflineAction(this, mediaItem, track);
	// // HomeActivity.refreshOfflineState = true;
	// mTracks.add(track);
	// }
	// }
	// if (mTracks != null && mTracks.size() > 0) {
	// CacheManager.saveAllTracksOfflineAction(this, mTracks);
	// Utils.saveAllOfflineFlurryEvent(this,
	// FlurryConstants.FlurryCaching.MyFavorites.toString(),
	// mTracks);
	// }
	// ((ImageButton) findViewById(R.id.main_title_bar_button_options))
	// .setSelected(false);
	// ((ImageButton) findViewById(R.id.main_title_bar_button_options))
	// .setBackgroundResource(0);
	// ((LinearLayout) findViewById(R.id.favorite_list_options))
	// .setVisibility(View.GONE);
	// }
	// }

	// Menu mMenu;
	//
	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// MenuInflater inflater = getMenuInflater();
	// mMenu = menu;
	// inflater.inflate(R.menu.menu_main_offline_actionbar, menu);
	// return true;
	// }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/*int itemId = item.getItemId();
		if (itemId == android.R.id.home) {

            activity.onBackPressed();
			return true;
		} else*/
			return super.onOptionsItemSelected(item);
	}

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
                    //mplayerbar.collapseexpandplayerbar(false);
           //         displayTitle(actionbar_title);

                    isNeedToClose = true;

                    return true;
                } else {
                    //mplayerbar.collapseexpandplayerbar(false);
                        getActivity().getSupportFragmentManager().beginTransaction().remove(FavoritesActivity.this).commit();

                    return true;
                }
            }
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
        return false;
    }

	private void setNavigationListener(){
		((MainActivity)getActivity()).mToolbar.setNavigationOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				getActivity().onBackPressed();
			}
		});
	}

    @Override
    public void setTitle(boolean needOnlyHight, boolean needToSetTitle) {
        ((MainActivity)getActivity()).showBackButtonWithTitleMediaDetail(title, "");
		setNavigationListener();
		Utils.setToolbarColor(((MainActivity) getActivity()));
    }


    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == FILTER_OPTIONS_ACTIVITY_RESULT_CODE_Fav
				&& resultCode == activity.RESULT_OK) {

			// if (data != null && data.getExtras().getBoolean("pos")) {
			// int pos = data.getIntExtra("pos", 0);\
			ApplicationConfigurations appConfig = ApplicationConfigurations
					.getInstance(activity);
			int pos = appConfig.getFavSelection();
			if (pos != -1) {
				while (mFragmentManager.getBackStackEntryCount() > 0) {
					mFragmentManager.popBackStackImmediate();
				}
			} else if (pos == -1)
				getActivity().onBackPressed();

			if (pos == 0) {
				showFavoriteFragmentFor(MediaType.TRACK);
				/*findViewById(R.id.main_title_bar_button_options).setVisibility(
						View.VISIBLE);*/
			} else if (pos == 1) {
				showFavoriteFragmentFor(MediaType.ALBUM);
				/*if (mMenu != null) {
					mMenu.clear();
					MenuInflater inflater = getMenuInflater();
					inflater.inflate(R.menu.menu_main_actionbar, mMenu);
				}*/
			} else if (pos == 2) {
				showFavoriteFragmentFor(MediaType.PLAYLIST);
				/*if (mMenu != null) {
					mMenu.clear();
					MenuInflater inflater = getMenuInflater();
					inflater.inflate(R.menu.menu_main_actionbar, mMenu);
				}*/
			} else if (pos == 3) {
				showFavoriteFragmentFor(MediaType.VIDEO);
				/*if (mMenu != null) {
					mMenu.clear();
					MenuInflater inflater = getMenuInflater();
					inflater.inflate(R.menu.menu_main_actionbar, mMenu);
				}*/
			} else if (pos == 4) {
				showFavoriteFragmentFor(MediaType.ARTIST);
				/*if (mMenu != null) {
					mMenu.clear();
					MenuInflater inflater = getMenuInflater();
					inflater.inflate(R.menu.menu_main_actionbar, mMenu);
				}*/
			}
			// }
		} else if (requestCode == FILTER_OPTIONS_ACTIVITY_RESULT_CODE_Fav
				&& resultCode == activity.RESULT_CANCELED) {
			ApplicationConfigurations appConfig = ApplicationConfigurations
					.getInstance(activity);
			if (appConfig.getFavSelection() == -1)
				//finish();
            activity.onBackPressed();
		}

	}

	@Override
	public void onMediaItemOptionPlayAndOpenSelected(MediaItem mediaItem,
			int position) {

	}

}
