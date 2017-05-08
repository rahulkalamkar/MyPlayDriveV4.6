package com.hungama.myplay.activity.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.HungamaApplication;

import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.MediaCachingTaskNew;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.CollectionItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.MyCollectionResponse;
import com.hungama.myplay.activity.data.dao.hungama.PlayerOption;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.SocialMyCollectionOperation;
import com.hungama.myplay.activity.ui.adapters.ComboMediaItem;
import com.hungama.myplay.activity.ui.fragments.BackHandledFragment;
import com.hungama.myplay.activity.ui.fragments.MediaTileGridFragment;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.ScreenLockStatus;
import com.hungama.myplay.activity.util.Utils;

public class MyCollectionActivity extends BackHandledFragment implements
		CommunicationOperationListener, OnMediaItemOptionSelectedListener {

	private static final String TAG = "MyCollectionActivity";

	private Context mContext;
	private DataManager mDataManager;
	private FragmentManager mFragmentManager;

	private LanguageTextView mTitleBarText;
    String title;
	private MediaTileGridFragment mMediaTileGridFragment;

	private Stack<String> stack_text = new Stack<String>();
	private ImageView menu;

	public static final int FILTER_OPTIONS_ACTIVITY_RESULT_CODE_MYCOLLECTION = 1094;
	boolean isOncreate;
    private Activity activity;

	// ======================================================
	// ACTIVITY'S LIFECYCLE.
	// ======================================================

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity)getActivity()).lockDrawer();
        ((MainActivity)getActivity()).removeDrawerIconAndPreference();
        ((MainActivity)getActivity()).setNeedToOpenSearchActivity(false);
        //This file is indented with tabs instead of 4 spaces
		/*setOverlayAction();
		View root = (View) LayoutInflater.from(MyCollectionActivity.this)
				.inflate(R.layout.activity_favorites, null);
		setContentView(root);
		super.onCreate(savedInstanceState);
		mContext = this;

		mDataManager = DataManager.getInstance(mContext);
		mFragmentManager = getSupportFragmentManager();

		// SetS title bar
		mTitleBarText = (LanguageTextView) findViewById(R.id.main_title_bar_text);
		mTitleBarText.setText(Utils.getMultilanguageText(mContext,
				getResources().getString(R.string.my_collection_title)));

		findViewById(R.id.main_title_bar).setVisibility(View.GONE);

		menu = (ImageView) findViewById(R.id.menu);
		menu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ScreenLockStatus.getInstance(MyCollectionActivity.this)
						.dontShowAd();
				Logger.s("SatelliteMenu onClick");
				Intent i = new Intent(MyCollectionActivity.this,
						TransperentActivityMyCollection.class);

				startActivityForResult(i,
						FILTER_OPTIONS_ACTIVITY_RESULT_CODE_MYCOLLECTION);

				menu.setTag("sethidden");
				menu.setVisibility(View.INVISIBLE);
				// overridePendingTransition(R.anim.pull_up_from_bottom,R.anim.push_out_to_bottom_discover);
			}
		});

		ApplicationConfigurations appConfig = ApplicationConfigurations
				.getInstance(this);
		appConfig.setMyCollectionSelection(-1);

		// new Handler().postDelayed(new Runnable() {
		//
		// @Override
		// public void run() {
		// TODO Auto-generated method stub
		isOncreate = true;
		Intent i = new Intent(MyCollectionActivity.this,
				TransperentActivityMyCollection.class);
		startActivityForResult(i,
				FILTER_OPTIONS_ACTIVITY_RESULT_CODE_MYCOLLECTION);
		menu.setTag("sethidden");
		menu.setVisibility(View.INVISIBLE);
*/
	}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.activity_favorites,
                container, false);
        activity=getActivity();


       // mContext = this;

        mDataManager = DataManager.getInstance(mContext);
        mFragmentManager = getChildFragmentManager();

        // SetS title bar
        mTitleBarText = (LanguageTextView) rootView.findViewById(R.id.main_title_bar_text);
       /* mTitleBarText.setText(Utils.getMultilanguageText(mContext,
                getResources().getString(R.string.my_collection_title)));*/

        rootView.findViewById(R.id.main_title_bar).setVisibility(View.GONE);

        menu = (ImageView) rootView.findViewById(R.id.menu);
        menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ScreenLockStatus.getInstance(activity)
                        .dontShowAd();
                Logger.s("SatelliteMenu onClick");
                Intent i = new Intent(activity,
                        TransperentActivityMyCollection.class);

                startActivityForResult(i,
                        FILTER_OPTIONS_ACTIVITY_RESULT_CODE_MYCOLLECTION);

                menu.setTag("sethidden");
                menu.setVisibility(View.INVISIBLE);
                // overridePendingTransition(R.anim.pull_up_from_bottom,R.anim.push_out_to_bottom_discover);
            }
        });

        ApplicationConfigurations appConfig = ApplicationConfigurations
                .getInstance(activity);
        appConfig.setMyCollectionSelection(-1);

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
				try{
					Intent i = new Intent(activity,
							TransperentActivityMyCollection.class);
					startActivityForResult(i,
							FILTER_OPTIONS_ACTIVITY_RESULT_CODE_MYCOLLECTION);
					menu.setTag("sethidden");
					menu.setVisibility(View.INVISIBLE);
				}catch (Exception e){
					e.printStackTrace();
				}

            }
        }, 400);



        return rootView;
    }

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
        ((MainActivity)getActivity()).showBackButtonWithTitleMediaDetail(
				Utils.getMultilanguageTextLayOut(activity, Utils
						.getMultilanguageText(
								activity,
								getResources().getString(
										R.string.my_collection_title))), "");

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
				if(getActivity()!=null && getActivity() instanceof DownloadConnectingActivity){
					if (count == 1) {
						getActivity().finish();
						return true;
					}
				}
                if (count > 0) {
                    results = false;

                    getActivity().getSupportFragmentManager().popBackStack();
                    //mplayerbar.collapseexpandplayerbar(false);
                    //         displayTitle(actionbar_title);

                    isNeedToClose = true;

                    return true;
                } else {
                    //mplayerbar.collapseexpandplayerbar(false);
					getActivity().getSupportFragmentManager().popBackStack();
//                    getActivity().getSupportFragmentManager().beginTransaction().remove(MyCollectionActivity.this).commit();

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
        ((MainActivity)getActivity()).showBackButtonWithTitleMediaDetail(title, "");
        ((MainActivity)getActivity()).mToolbar.setNavigationOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		Utils.setToolbarColor(((MainActivity)getActivity()));

    }

    @Override
	public void onResume() {
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

		if (stack_text != null && stack_text.size() > 0)
            ((MainActivity)getActivity()).showBackButtonWithTitleMediaDetail(stack_text.get(stack_text.size() - 1), "");

		super.onResume();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onPause() {
		HungamaApplication.activityPaused();
		super.onPause();
	}

	/*@Override
	protected NavigationItem getNavigationItem() {
		return NavigationItem.OTHER;
	}
*/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/*int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			onBackPressed();
			return true;
		} else*/
			return super.onOptionsItemSelected(item);
	}

	/**
	 * Add mediatile fragment if not else refresh list
	 * 
	 * @param detailsData
	 */
	public void addFragment(Bundle detailsData) {

		if (mMediaTileGridFragment == null) {
			mMediaTileGridFragment = new MediaTileGridFragment();
			mMediaTileGridFragment.setArguments(detailsData);
			mMediaTileGridFragment.setOnMediaItemOptionSelectedListener(this);
			mMediaTileGridFragment.setshowDownloadOption(false);

			try {

				FragmentTransaction fragmentTransaction = mFragmentManager
						.beginTransaction();
              				fragmentTransaction.replace(R.id.main_fragmant_container,
									mMediaTileGridFragment);
				if(Constants.IS_COMMITALLOWSTATE)
					fragmentTransaction.commitAllowingStateLoss();
				else
					fragmentTransaction.commit();
			} catch (Exception e) {
			}
		} else {

			List<MediaItem> mMediaItems = (List<MediaItem>) detailsData
					.getSerializable(MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS);

			ArrayList<Object> mediaitemMusic = new ArrayList<Object>();
			mediaitemMusic.clear();

			List<MediaItem> tracks = new ArrayList<MediaItem>();
			List<MediaItem> playlists = new ArrayList<MediaItem>();

			for (MediaItem mediaItem : mMediaItems) {
				if (mediaItem.getMediaType() == MediaType.TRACK)
					tracks.add(mediaItem);
				else if (mediaItem.getMediaType() == MediaType.ALBUM)
					tracks.add(mediaItem);
				else if (mediaItem.getMediaType() == MediaType.PLAYLIST)
					playlists.add(mediaItem);
			}

			int blocks = (tracks.size()) / 2;

			if (playlists != null && playlists.size() > 0) {
				blocks = (playlists.size());
			} else {
				blocks = (tracks.size()) / 2;
				if ((tracks.size()) % 2 > 0)
					blocks += 1;
			}

			ComboMediaItem c;
			for (int i = 0; i < blocks; i++) {
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
			}
			if (isVideo)
				mMediaTileGridFragment.setMediaItemsVideo(mMediaItems);
			else
				mMediaTileGridFragment.setMediaItems(mediaitemMusic);
		}
	}

	// ======================================================
	// Communication Operations events.
	// ======================================================

	@Override
	public void onStart(int operationId) {
		switch (operationId) {
		case OperationDefinition.Hungama.OperationId.SOCIAL_MY_COLLECTION:
			showLoadingDialog(R.string.application_dialog_loading_content);
			break;

		default:
			break;
		}

	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			Logger.i(TAG, "Success Loading My Collection ");

			MyCollectionResponse mMyCollectionResponse = (MyCollectionResponse) responseObjects
					.get(SocialMyCollectionOperation.RESULT_KEY_MY_COLLECTION);

			List<CollectionItem> mediaItems = new ArrayList<CollectionItem>();
			mediaItems = mMyCollectionResponse.getMyData();

			Bundle data = new Bundle();
			int listSize;
			if (mediaItems != null) {
				for (CollectionItem mediaItem : mediaItems) {
					MediaType mMediaType = mediaItem.getMediaType();
					if (mMediaType == MediaType.ALBUM
							|| mMediaType == MediaType.PLAYLIST
							|| mMediaType == MediaType.TRACK
							|| mMediaType == MediaType.ARTIST) {

						mediaItem.setMediaContentType(MediaContentType.MUSIC);
					} else {
						// mediaItems.remove(mediaItem);
						mediaItem.setMediaContentType(MediaContentType.VIDEO);
					}
				}

				listSize = mediaItems.size();
				data.putSerializable(
						MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS,
						(Serializable) mediaItems);
				data.putString(
						MediaTileGridFragment.FLURRY_SUB_SECTION_DESCRIPTION,
						FlurryConstants.FlurrySubSectionDescription.MyCollection
								.toString());
			} else {
				listSize = 0;
			}

			 title = "";
			if (isVideo) {
				title = Utils.getMultilanguageText(mContext, getResources()
						.getString(R.string.collection_mp4))
						+ " (" + listSize + ")";
			} else
				title = Utils.getMultilanguageText(mContext, getResources()
						.getString(R.string.collection_mp3))
						+ " (" + listSize + ")";


			if (!stack_text.contains(title))
				stack_text.push(title);

            setTitle(false,true);

			addFragment(data);

		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

		new Handler().post(new Runnable() {

			@Override
			public void run() {
				hideLoadingDialog();
			}
		});

	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		switch (operationId) {
		case OperationDefinition.Hungama.OperationId.SOCIAL_MY_COLLECTION:
			hideLoadingDialog();
			break;

		default:
			break;
		}

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
					FlurryConstants.FlurrySourceSection.MyCollection.toString());

			mediaDetailsFragment.setArguments(bundle);

			fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
					mediaDetailsFragment, "MediaDetailsActivitycollection");
			fragmentTransaction.addToBackStack("MediaDetailsActivitycollection");
			if(Constants.IS_COMMITALLOWSTATE)
				fragmentTransaction.commitAllowingStateLoss();
			else
				fragmentTransaction.commit();
			return;
//			intent = new Intent(activity, MediaDetailsActivity.class);
//			intent.putExtra(MediaDetailsActivity.EXTRA_MEDIA_ITEM,
//					(Serializable) mediaItem);
		} else {
			intent = new Intent(activity, VideoActivity.class);
			intent.putExtra(VideoActivity.EXTRA_MEDIA_ITEM_VIDEO,
					(Serializable) mediaItem);
		}
		intent.putExtra(MediaDetailsActivity.FLURRY_SOURCE_SECTION,
				FlurryConstants.FlurrySourceSection.MyCollection.toString());
		startActivity(intent);
	}

	@Override
	public void onMediaItemOptionRemoveSelected(MediaItem mediaItem,
			int position) {
		Logger.i(TAG, "Remove item: " + mediaItem.getId());
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

	boolean isVideo;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == FILTER_OPTIONS_ACTIVITY_RESULT_CODE_MYCOLLECTION
				&& resultCode == activity.RESULT_OK) {

			// if (data != null && data.getExtras().getBoolean("pos")) {
			// int pos = data.getIntExtra("pos", 0);\
			ApplicationConfigurations appConfig = ApplicationConfigurations
					.getInstance(activity);
			int pos = appConfig.getMyCollectionSelection();
			if (pos != -1) {
				while (mFragmentManager.getBackStackEntryCount() > 0) {
					mFragmentManager.popBackStackImmediate();
				}
			} else if (pos == -1)
				//finish();
                getActivity().onBackPressed();
			if (pos == 0) {
				openMyCollectionSongs("audio");
				isVideo = false;
			} else if (pos == 1) {
				// showFavoriteFragmentFor(MediaType.ALBUM);
				isVideo = true;
				openMyCollectionSongs("video");
			}
			// }
		} else if (requestCode == FILTER_OPTIONS_ACTIVITY_RESULT_CODE_MYCOLLECTION
				&& resultCode == activity.RESULT_CANCELED) {
			ApplicationConfigurations appConfig = ApplicationConfigurations
					.getInstance(activity);
			if (appConfig.getMyCollectionSelection() == -1)
				//finish();
                getActivity().onBackPressed();
		}
	}

	private void openMyCollectionSongs(String type) {
		mMediaTileGridFragment = null;
		Bundle data = new Bundle();
		List<CollectionItem> mediaItems = new ArrayList<CollectionItem>();
		data.putSerializable(
				MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS,
				(Serializable) mediaItems);
		data.putString(MediaTileGridFragment.FLURRY_SUB_SECTION_DESCRIPTION,
				FlurryConstants.FlurrySubSectionDescription.MyCollection
						.toString());

		addFragment(data);

		mDataManager.getMyCollection(this, type);
	}

	@Override
	public void onMediaItemOptionPlayAndOpenSelected(MediaItem mediaItem,
			int position) {
	}
}
