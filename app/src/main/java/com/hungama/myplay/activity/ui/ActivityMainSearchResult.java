package com.hungama.myplay.activity.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.MediaCachingTaskNew;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.PlayerOption;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.ui.fragments.MainSearchResultsFragment;
import com.hungama.myplay.activity.ui.fragments.MainSearchResultsFragment.OnSearchResultsOptionSelectedListener;
import com.hungama.myplay.activity.ui.fragments.RadioDetailsFragment;
import com.hungama.myplay.activity.util.ApsalarEvent;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Presents the Quick Navigation panel within the application.
 */
public class ActivityMainSearchResult extends MainActivity implements
		OnSearchResultsOptionSelectedListener, CommunicationOperationListener {

	private static final String TAG = "ActivityMainSearchResult";
	MainSearchResultsFragment fragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Utils.clearCache();
		Logger.i("Tag", "Search detail screen:0");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main_search_resumt);
		onCreateCode();
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragment = (MainSearchResultsFragment) fragmentManager
				.findFragmentByTag(MainSearchResultsFragment.TAG);
		Bundle arguments = getIntent().getExtras();
		if (fragment == null) {
			if(arguments!=null)
				arguments.putBoolean(MainSearchResultsFragment.FROM_FULL_PLAYER,
					false);
			fragment = new MainSearchResultsFragment();
			fragment.setNeedToSetBack(false);
			fragment.setArguments(arguments);
			fragment.setOnSearchResultsOptionSelectedListener(this);

			FragmentTransaction fragmentTransaction = fragmentManager
					.beginTransaction();
			fragmentTransaction.add(R.id.main_search_results_container1,
					fragment, MainSearchResultsFragment.TAG);
			fragmentTransaction.disallowAddToBackStack();
			// fragmentTransaction.addToBackStack(null);
			// fragmentTransaction.commitAllowingStateLoss();
			fragmentTransaction.commit();
		}


		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Bundle arguments = getIntent().getExtras();
//
				if (arguments != null)
					displayTitle(arguments
							.getString(MainSearchResultsFragment.FRAGMENT_ARGUMENT_QUERY));
			}
		}, 10000);
		ApsalarEvent.postEvent(getActivity(), ApsalarEvent.SEARCH_PERFORMED);
	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	@Override
	protected void onResume() {
		super.onResume();
		Bundle arguments = getIntent().getExtras();
		if (arguments != null)
			displayTitle(arguments
					.getString(MainSearchResultsFragment.FRAGMENT_ARGUMENT_QUERY));
	}

	// Menu mMenu;
	// SearchView search;
	//
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		mMenu = menu;
		inflater.inflate(R.menu.menu_search_actionbar1, menu);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

			// search = (SearchView) menu.findItem(R.id.search).getActionView();

		}
		return true;
	}

	//
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		finish();
		return true;

	}

	private void displayTitle(String actionbar_title) {
		Utils.setToolbarColor(((MainActivity)getActivity()));
		showBackButtonWithTitle(actionbar_title, "");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected NavigationItem getNavigationItem() {
		return null;
	}

	private MediaItem tempMediaItem;

	@Override
	public void onPlayNowSelected(MediaItem mediaItem) {
		tempMediaItem = mediaItem;
		try {
			if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
				if (mediaItem.getMediaType() == MediaType.TRACK) {
					Track track = new Track(mediaItem.getId(),
							mediaItem.getTitle(), mediaItem.getAlbumName(),
							mediaItem.getArtistName(), mediaItem.getImageUrl(),
							mediaItem.getBigImageUrl(), mediaItem.getImages(),
							mediaItem.getAlbumId());
					List<Track> tracks = new ArrayList<Track>();
					tracks.add(track);

					mPlayerBarFragment.playNow(tracks, null, null);

				} else {
					mDataManager.getMediaDetails(mediaItem,
							PlayerOption.OPTION_PLAY_NOW,
							ActivityMainSearchResult.this);
				}
			} else if (mediaItem.getMediaContentType() == MediaContentType.RADIO) {
				// Intent intent = new Intent(getActivity(),
				// RadioActivity.class);
				// intent.putExtra(RadioActivity.EXTRA_SHOW_DETAILS_MEDIA_ITEM,
				// (Serializable) mediaItem);
				// intent.putExtra(RadioActivity.EXTRA_SHOW_DETAILS_CATEGORY_TYPE,
				// (Serializable) MediaCategoryType.TOP_ARTISTS_RADIO);
				// startActivity(intent);
				showDetailsOfRadioHelper(mediaItem,
						MediaCategoryType.TOP_ARTISTS_RADIO);
			}
		} catch (Exception e) {
			Logger.e(getClass().getName() + ":1076", e.toString());
		}
	}

	private void showDetailsOfRadioHelper(MediaItem mediaItem,
			MediaCategoryType mediaCategoryType) {
		FragmentManager mFragmentManager = ((MainActivity) getActivity())
				.getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = mFragmentManager
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
		// if (mIfDetailsRequestedImmediately) {
		// /*
		// * IMPORTANT: We use the "root frame" defined in "root_fragment.xml"
		// * as the reference to replace fragment
		// */
		// fragmentTransaction.add(R.id.root_frame, radioDetailsFragment);
		// /*
		// * IMPORTANT: The following lines allow us to add the fragment to
		// * the stack and return to it later, by pressing back
		// */
		// fragmentTransaction
		// .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		// fragmentTransaction.commit();
		//
		// } else {
		try {
			/*
			 * IMPORTANT: We use the "root frame" defined in "root_fragment.xml"
			 * as the reference to replace fragment
			 */
			fragmentTransaction.add(R.id.main_navigation_fragmant_container,
					radioDetailsFragment);
			/*
			 * IMPORTANT: The following lines allow us to add the fragment to
			 * the stack and return to it later, by pressing back
			 */
			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} catch (Exception e) {
		}
		// }
	}

	@Override
	public void onAddToQueueSelected(MediaItem mediaItem) {
		try {
			if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
				if (mediaItem.getMediaType() == MediaType.TRACK) {
					Track track = new Track(mediaItem.getId(),
							mediaItem.getTitle(), mediaItem.getAlbumName(),
							mediaItem.getArtistName(), mediaItem.getImageUrl(),
							mediaItem.getBigImageUrl(), mediaItem.getImages(),
							mediaItem.getAlbumId());
					List<Track> tracks = new ArrayList<Track>();
					tracks.add(track);

					mPlayerBarFragment.addToQueue(tracks, null, null);

				} else {
					mDataManager.getMediaDetails(mediaItem,
							PlayerOption.OPTION_ADD_TO_QUEUE,
							ActivityMainSearchResult.this);
				}

			} else if (mediaItem.getMediaContentType() == MediaContentType.RADIO) {
				// Intent intent = new Intent(getActivity(),
				// RadioActivity.class);
				// intent.putExtra(RadioActivity.EXTRA_SHOW_DETAILS_MEDIA_ITEM,
				// (Serializable) mediaItem);
				// intent.putExtra(RadioActivity.EXTRA_SHOW_DETAILS_CATEGORY_TYPE,
				// (Serializable) MediaCategoryType.TOP_ARTISTS_RADIO);
				// startActivity(intent);
				showDetailsOfRadioHelper(mediaItem,
						MediaCategoryType.TOP_ARTISTS_RADIO);
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void onShowDetails(MediaItem mediaItem, List<MediaItem> list,
			boolean addToQueue) {
		Intent intent = new Intent(this, VideoActivity.class);
		intent.putExtra(VideoActivity.EXTRA_MEDIA_ITEM_VIDEO,
				(Serializable) mediaItem);
		intent.putExtra(VideoActivity.EXTRA_MEDIA_LIST_VIDEO,
				(Serializable) list);
		intent.putExtra(VideoActivity.EXTRA_MEDIA_POS_VIDEO,
				list.indexOf(mediaItem));
		startActivity(intent);
	}

	@Override
	public void onShowDetails(MediaItem mediaItem, boolean playnow) {
		Intent intent;
		if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
			intent = new Intent(getActivity(), VideoActivity.class);
			intent.putExtra(VideoActivity.EXTRA_MEDIA_ITEM_VIDEO,
					(Serializable) mediaItem);
			startActivity(intent);
		} else if (mediaItem.getMediaContentType() == MediaContentType.RADIO) {
			showDetailsOfRadioHelper(mediaItem,
					MediaCategoryType.TOP_ARTISTS_RADIO);
			// intent = new Intent(getActivity(), RadioActivity.class);
			// intent.putExtra(RadioActivity.EXTRA_SHOW_DETAILS_MEDIA_ITEM,
			// (Serializable) mediaItem);
			// intent.putExtra(RadioActivity.EXTRA_SHOW_DETAILS_CATEGORY_TYPE,
			// (Serializable) MediaCategoryType.TOP_ARTISTS_RADIO);

		} else {
			intent = new Intent(getActivity(), MediaDetailsActivity.class);
			intent.putExtra(MediaDetailsActivity.EXTRA_MEDIA_ITEM,
					(Serializable) mediaItem);
			intent.putExtra(MediaDetailsActivity.FLURRY_SOURCE_SECTION,
					FlurryConstants.FlurrySourceSection.Search.toString());
			startActivity(intent);
		}

	}

	private Activity getActivity() {
		return ActivityMainSearchResult.this;
	}

	@Override
	public void onSaveOffline(MediaItem mediaItem) {

		Logger.i(TAG, "Save Offline: " + mediaItem.getId());
		if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				Track track = new Track(mediaItem.getId(),
						mediaItem.getTitle(), mediaItem.getAlbumName(),
						mediaItem.getArtistName(), mediaItem.getImageUrl(),
						mediaItem.getBigImageUrl(), mediaItem.getImages(),
						mediaItem.getAlbumId());
				CacheManager.saveOfflineAction(getActivity(), mediaItem, track);
				Utils.saveOfflineFlurryEvent(getActivity(),
						FlurryConstants.FlurryCaching.LongPressMenuSong
								.toString(), mediaItem);
			} else if (mediaItem.getMediaType() == MediaType.ALBUM
					|| mediaItem.getMediaType() == MediaType.PLAYLIST) {
				if (MediaCachingTaskNew.isEnabled)
					mDataManager.getMediaDetails(mediaItem,
							PlayerOption.OPTION_SAVE_OFFLINE, this);
				else
					CacheManager.saveOfflineAction(getActivity(), mediaItem,
							null);
				if (mediaItem.getMediaType() == MediaType.ALBUM)
					Utils.saveOfflineFlurryEvent(getActivity(),
							FlurryConstants.FlurryCaching.LongPressMenuAlbum
									.toString(), mediaItem);
				else
					Utils.saveOfflineFlurryEvent(getActivity(),
							FlurryConstants.FlurryCaching.LongPressMenuPlaylist
									.toString(), mediaItem);
			}
		} else {
			CacheManager.saveOfflineAction(getActivity(), mediaItem, null);
			Utils.saveOfflineFlurryEvent(
					getActivity(),
					FlurryConstants.FlurryCaching.LongPressMenuVideo.toString(),
					mediaItem);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hungama.myplay.activity.ui.fragments.MainSearchResultsFragment.
	 * OnSearchResultsOptionSelectedListener#onFinishSongCatcher(boolean)
	 */
	@Override
	public void onFinishSongCatcher(boolean isFinishSongCatcher) {
		if (isFinishSongCatcher) {
			String timestamp_cache = mDataManager
					.getApplicationConfigurations().getSearchPopularTimeStamp();
			mDataManager.getSearchPopularSerches(this, this, timestamp_cache);
		}
	}

	@Override
	public void onStart(int operationId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
				try {
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
							mPlayerBarFragment.playNow(tracks, null, null);
						} else if (playerOptions == PlayerOption.OPTION_PLAY_NEXT) {
							mPlayerBarFragment.playNext(tracks);
						} else if (playerOptions == PlayerOption.OPTION_ADD_TO_QUEUE) {
							mPlayerBarFragment.addToQueue(tracks, null, null);
						} else if (playerOptions == PlayerOption.OPTION_SAVE_OFFLINE) {
							if (mediaItem.getMediaType() == MediaType.ALBUM) {
								for (Track track : tracks) {
									track.setTag(mediaItem);
								}
							}
							CacheManager.saveAllTracksOfflineAction(
									getActivity(), tracks);
						}
					}
				} catch (Exception e) {
					Logger.e(getClass().getName() + ":679", e.toString());
				}

				hideLoadingDialog();
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		// TODO Auto-generated method stub
	}

}