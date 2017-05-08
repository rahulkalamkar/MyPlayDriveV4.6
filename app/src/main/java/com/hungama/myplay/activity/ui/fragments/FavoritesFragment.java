package com.hungama.myplay.activity.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.social.ProfileFavoriteMediaItems;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.SocialProfileFavoriteMediaItemsOperation;
import com.hungama.myplay.activity.ui.ProfileActivity;
import com.hungama.myplay.activity.ui.adapters.ComboMediaItem;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Loads and shows the given the User's favorites MediaItems.
 */
public class FavoritesFragment extends MediaTileGridFragment implements
		CommunicationOperationListener {

	private static final String TAG = "FavoritesFragment";

	public static final String FRAGMENT_ARGUMENT_MEDIA_TYPE = "fragment_argument_media_type";
	public static final String FRAGMENT_ARGUMENT_USER_ID = "fragment_argument_user_id";
	public static FavoritesFragment Instance = null;

	public interface OnMediaItemsLoadedListener {

		public void onMediaItemsLoaded(MediaType mediaType, String userId,
				List<MediaItem> mediaItems);
	}

    public ProfileActivity profileActivity;
    public void setProfileActivity(ProfileActivity profileActivity) {
        this.profileActivity = profileActivity;
    }

	public void setOnMediaItemsLoadedListener(
			OnMediaItemsLoadedListener listener) {
		mOnMediaItemsLoadedListener = listener;
	}

	public DataManager mDataManager;
	public List<MediaItem> mMediaItems = null;

	private OnMediaItemsLoadedListener mOnMediaItemsLoadedListener;

	public String mUserId;

	public FavoritesFragment() {
		init(true);
	}

	public void init(boolean isDeleteShowing) {
		this.isDeleteShowing = isDeleteShowing;
	}

    public void setTitle(){
        if(profileActivity!=null)
            profileActivity.setTitle(true);
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Instance = FavoritesFragment.this;
		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());
        super.setProfileActivity(profileActivity);
		Analytics.postCrashlitycsLog(getActivity(), FavoritesFragment.class.getName());

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	public MediaType mediaType = null;

	@Override
	public void onStart() {
		super.onStart();

		String userId = null;

		if (mMediaItems == null) {
			Bundle arguments = getArguments();
			if (arguments != null
					&& arguments.containsKey(FRAGMENT_ARGUMENT_MEDIA_TYPE)) {
				mediaType = (MediaType) arguments
						.getSerializable(FRAGMENT_ARGUMENT_MEDIA_TYPE);

				if (arguments.containsKey(FRAGMENT_ARGUMENT_USER_ID)) {
					userId = arguments.getString(FRAGMENT_ARGUMENT_USER_ID);
					mUserId = userId;

					mDataManager.getFavorites(getActivity(), mediaType, userId,
							this);
				}
			} else {
				throw new IllegalArgumentException(TAG
						+ ": Fragment must contain a madia type in arguments.");
			}
		}
		Analytics.startSession(getActivity(), this);
		Analytics.onPageView();

		boolean isMe;
		if (mDataManager.getApplicationConfigurations().getPartnerUserId()
				.equalsIgnoreCase(userId)) {
			isMe = true;
		} else {
			isMe = false;
		}

		if (mediaType == MediaType.TRACK) {

			if (isMe) {
				Analytics.logEvent("My Fav Songs");
			} else {
				Analytics.logEvent("Others Fav Songs");
			}

		} else if (mediaType == MediaType.ALBUM) {

			if (isMe) {
				Analytics.logEvent("My Fav Albums");
			} else {
				Analytics.logEvent("Others Fav Albums");
			}

		} else if (mediaType == MediaType.PLAYLIST) {

			if (isMe) {
				Analytics.logEvent("My Fav Playlists");
			} else {
				Analytics.logEvent("Others Fav Playlists");
			}

		} else if (mediaType == MediaType.VIDEO) {

			if (isMe) {
				Analytics.logEvent("My Fav Videos");
			} else {
				Analytics.logEvent("Others Fav Videos");
			}
		} else if (mediaType == MediaType.ARTIST) {

			if (isMe) {
				Analytics.logEvent("My Fav Artists");
			} else {
				Analytics.logEvent("Others Fav Artists");
			}
		}

	}

	@Override
	public void onStop() {
		super.onStop();
		Analytics.onEndSession(getActivity());
	}

	// ======================================================
	// Communication Operations callbacks.
	// =====================================================

	@Override
	public void onStart(int operationId) {
		switch (operationId) {
		case OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS:
			pb.setVisibility(View.VISIBLE);
			break;
		}
	}

	MediaType mediType;

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			switch (operationId) {
			case OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS:
				// gets the media items and populate the adapter.
				try {
					ProfileFavoriteMediaItems profileFavoriteMediaItems = (ProfileFavoriteMediaItems) responseObjects
							.get(SocialProfileFavoriteMediaItemsOperation.RESULT_KEY_PROFILE_FAVORITE_MEDIA_ITEMS);
					mMediaItems = profileFavoriteMediaItems.mediaItems;
				} catch (Exception e) {
				}

				if (mMediaItems != null) {
					for (MediaItem item : mMediaItems) {
						if (item.getMediaType() == MediaType.ARTIST)
							item.setMediaContentType(MediaContentType.RADIO);
					}
				} else {
					hideLoadingDialog();
					mediType = mediaType;
					if (mOnMediaItemsLoadedListener != null) {
						mOnMediaItemsLoadedListener.onMediaItemsLoaded(
								mediType, mUserId, null);
					}
					pb.setVisibility(View.GONE);
					return;
				}
				Logger.e("mMediaItems size", "" + mMediaItems.size());

				ArrayList<Object> mediaitemMusic = new ArrayList<Object>();
				mediaitemMusic.clear();

				List<MediaItem> tracks = new ArrayList<MediaItem>();
				List<MediaItem> playlists = new ArrayList<MediaItem>();

				for (MediaItem mediaItem : mMediaItems) {
					if (mediaItem.getMediaType() == MediaType.TRACK)
						tracks.add(mediaItem);
					else if (mediaItem.getMediaType() == MediaType.ALBUM)
						tracks.add(mediaItem);
					else if (mediaItem.getMediaType() == MediaType.ARTIST)
						tracks.add(mediaItem);
					else if (mediaItem.getMediaType() == MediaType.PLAYLIST)
						playlists.add(mediaItem);
				}

				Logger.e("tracks size", "" + tracks.size());

				int blocks = (tracks.size()) / 2;

				if (playlists != null && playlists.size() > 0) {
					blocks = (playlists.size());
				} else {
					blocks = (tracks.size()) / 2;
					if ((tracks.size()) % 2 > 0)
						blocks += 1;
				}

				Logger.e("blocks size", "" + blocks);

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

				mediType = (MediaType) responseObjects
						.get(SocialProfileFavoriteMediaItemsOperation.RESULT_KEY_MEDIA_TYPE);

				if (mediType == MediaType.VIDEO)
					setMediaItemsVideo(mMediaItems);
				else {
					setMediaItems(mediaitemMusic);
				}
				// updates the tile's grid.

				// hide the loading.
				hideLoadingDialog();

				pb.setVisibility(View.GONE);

				if (mOnMediaItemsLoadedListener != null) {
					mOnMediaItemsLoadedListener.onMediaItemsLoaded(mediType,
							mUserId, mMediaItems);
				}

				break;
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	/**
	 * remove selected item from list and referesh list
	 * 
	 * @param mediaItem
	 */
	public void removeAndRefreshList(MediaItem mediaItem) {
		if (mediaItem.getMediaType() == MediaType.VIDEO) {
			mMediaItems.remove(mediaItem);
			setMediaItemsVideo(mMediaItems);
		} else {
			mMediaItems.remove(mediaItem);
			setMediaItems(refreshList());
		}
		if (mOnMediaItemsLoadedListener != null) {
			mOnMediaItemsLoadedListener.onMediaItemsLoaded(mediType, mUserId,
					mMediaItems);
		}

	}

	/**
	 * referesh adapter content
	 * 
	 * @return
	 */
	private ArrayList<Object> refreshList() {
		ArrayList<Object> mediaitemMusic = new ArrayList<Object>();
		mediaitemMusic.clear();

		List<MediaItem> tracks = new ArrayList<MediaItem>();
		List<MediaItem> playlists = new ArrayList<MediaItem>();

		if (mMediaItems != null)
			for (MediaItem mediaItem : mMediaItems) {
				if (mediaItem.getMediaType() == MediaType.TRACK)
					tracks.add(mediaItem);
				else if (mediaItem.getMediaType() == MediaType.ALBUM)
					tracks.add(mediaItem);
				else if (mediaItem.getMediaType() == MediaType.PLAYLIST)
					playlists.add(mediaItem);

			}
		ArrayList<MediaItem> serverSAA = new ArrayList<MediaItem>(tracks);
		int adcount = 0;

		if (adcount != 0)
			tracks = serverSAA;

		int blocks = (tracks.size()) / 2;

		if (playlists != null && playlists.size() > 0) {
			blocks = (playlists.size());
		} else if (tracks != null && tracks.size() > 0) {
			blocks = (tracks.size()) / 2;
			if ((tracks.size()) % 2 > 0)
				blocks += 1;
		} else if (playlists != null && playlists.size() > 0) {
			blocks = (tracks.size() + playlists.size()) / 5;
			if ((tracks.size() + playlists.size()) % 5 > 0)
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
		return mediaitemMusic;
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		switch (operationId) {
		case OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS:
			hideLoadingDialog();
			break;
		}
	}

	@Override
	public void onDestroyView() {
//		System.gc();
//		System.runFinalization();
		super.onDestroyView();
	}
}
