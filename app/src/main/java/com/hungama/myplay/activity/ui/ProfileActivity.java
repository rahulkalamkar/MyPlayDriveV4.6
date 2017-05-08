package com.hungama.myplay.activity.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.HungamaApplication;

import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
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
import com.hungama.myplay.activity.ui.fragments.ItemableTilesFragment;
import com.hungama.myplay.activity.ui.fragments.MediaDetailsFragment;
import com.hungama.myplay.activity.ui.fragments.MediaTileGridFragment;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment;
import com.hungama.myplay.activity.ui.fragments.RedeemFragment;
import com.hungama.myplay.activity.ui.fragments.social.BadgesFragment;
import com.hungama.myplay.activity.ui.fragments.social.LeaderboardFragment;
import com.hungama.myplay.activity.ui.fragments.social.LeaderboardFragment.OnLeaderboardUserSelectedListener;
import com.hungama.myplay.activity.ui.fragments.social.ProfileFragment;
import com.hungama.myplay.activity.ui.fragments.social.ProfileFragment.OnProfileSectionSelectedListener;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

//thumbnailURL photoURL
public class ProfileActivity extends BackHandledFragment implements
		OnProfileSectionSelectedListener, OnLeaderboardUserSelectedListener,
		CommunicationOperationListener, OnMediaItemOptionSelectedListener,
		OnMediaItemsLoadedListener {

	private static final String TAG = "ProfileActivity";

	public static final String DATA_EXTRA_USER_ID = "data_extra_user_id";

	public static final String ARGUMENT_PROFILE_ACTIVITY = "argument_profile_activity";

	private final int LOGIN_ACTIVITY_CODE = 1;

	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	private FragmentManager mFragmentManager;

	public Stack<String> stack_text = new Stack<String>();

	/*
	 * Creating Fragments from onActivityResult causes the application to crash
	 * due to the android-support library.
	 * 
	 * To make the Activity presents the user's profile after a redirection to
	 * the Login page, the activity assigns this flag to TRUE in the
	 * onActivityResult and resets it in the onResume, then shows this
	 * activity's content.
	 * 
	 * Note that this is necessary only when this activity launches the Login
	 * Activity to force the User to sign in and retrieves a response that the
	 * user has successfully Logged in / Signed in the application.
	 */
	private boolean mDoShowContentDueAndroidBug = false;

    View rootView;
	private String requestedUesrId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (rootView == null) {
            Bundle data = getArguments();

            rootView = inflater.inflate(R.layout.activity_main_profile,
                    container, false);
            Logger.i("Tag", "Search detail screen:4");

            // gets the user id.
            /*String */requestedUesrId = "";
            Bundle extras = getArguments();
            if (extras != null && extras.containsKey(DATA_EXTRA_USER_ID)) {
                requestedUesrId = extras.getString(DATA_EXTRA_USER_ID);
            }

            	/*
		 * To show the user's profile he must be logged in to the application.
		 * if he is not, pop up the login page, then directs him to here again.
		 */
            boolean applicationRealUser = mDataManager
                    .getApplicationConfigurations().isRealUser();
/*
		 * Shows the profile only if the application user is signed in or the
		 * requested profile is not of him.
		 */
            if (applicationRealUser || !(TextUtils.isEmpty(requestedUesrId))) {
                showProfileContent(requestedUesrId);
            } else {
                // launches the Login page.
                Intent startLoginActivityIntent = new Intent(getActivity(),
                        LoginActivity.class);
                startLoginActivityIntent.putExtra(ARGUMENT_PROFILE_ACTIVITY,
                        "profile_activity");
                startLoginActivityIntent.putExtra(LoginActivity.FLURRY_SOURCE,
                        FlurryConstants.FlurryUserStatus.MyProfile.toString());
                startActivityForResult(startLoginActivityIntent,
                        LOGIN_ACTIVITY_CODE);
            }

        } else {
            Logger.e("ProfileActivity", "onCreateView else");
            ViewGroup parent = (ViewGroup) Utils.getParentViewCustom(rootView);
            parent.removeView(rootView);
        }
        return rootView;
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// getDrawerLayout();
		// initializes activity's components.
		mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		mFragmentManager = getActivity().getSupportFragmentManager();

        ((MainActivity)getActivity()).lockDrawer();
        ((MainActivity)getActivity()).removeDrawerIconAndPreference();
        ((MainActivity)getActivity()).setNeedToOpenSearchActivity(false);



		setNavigationClick();
	}


	@Override
	public void onResume() {
		super.onResume();
		HungamaApplication.activityResumed();
		if (mApplicationConfigurations.isSongCatched()) {
            ((MainActivity)getActivity()).openOfflineGuide();
		}
		/*
		 * Happens only for the first time this Activity was launched when the
		 * user was not a real one, and he signed / logged in the app via the a
		 * redirected LoginActivity from here.
		 */
		if (mDoShowContentDueAndroidBug) {
			mDoShowContentDueAndroidBug = false;

			// gets the user id.
			String userID = "";
			Bundle extras = getArguments();
			if (extras != null && extras.containsKey(DATA_EXTRA_USER_ID)) {
				userID = extras.getString(DATA_EXTRA_USER_ID);
			}
			showProfileContent(userID);
		}

        setTitle(true);

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOGIN_ACTIVITY_CODE) {
			if (resultCode == getActivity().RESULT_OK) {
				boolean realUser = mDataManager.getApplicationConfigurations()
						.isRealUser();
				if (realUser) {
					// the user has logged in, shows his profile.
					mDoShowContentDueAndroidBug = true;
					String session = mDataManager
							.getApplicationConfigurations().getSessionID();
					if (!TextUtils.isEmpty(session)) {
						String accountType = Utils
								.getAccountName(getActivity().getApplicationContext());
						mDataManager.getCurrentSubscriptionPlan(this,
								accountType);
						// openPlansPage();
					}
				} else {
					// the user has tricked us.
                    onBackPressed();
//					finish();
				}

			} else {
				// closes this activity.
                onBackPressed();
//				finish();
			}
		}
	}

	/**
	 * display profile fragment
	 * 
	 * @param userId
	 */
	private void showProfileContent(String userId) {

		// the operation sets by default the application's user id if the given
		// is empty.

		ProfileFragment profileFragment = new ProfileFragment();
		profileFragment.setOnProfileSectionSelectedListener(this);
        profileFragment.setProfileActivity(this);
		Bundle arguments = new Bundle();
		arguments.putString(ProfileFragment.FRAGMENT_ARGUMENT_USER_ID, userId);
		profileFragment.setArguments(arguments);

		FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager()
				.beginTransaction();
		fragmentTransaction.add(R.id.main_fragmant_container, profileFragment, "profileFragment");
        fragmentTransaction.disallowAddToBackStack();
		if(Constants.IS_COMMITALLOWSTATE)
			fragmentTransaction.commitAllowingStateLoss();
		else
			fragmentTransaction.commit();
	}

	@Override
	public boolean onBackPressed() {
		if(getActivity()==null)
			return false;
		if (((MainActivity)getActivity()).closeDrawerIfOpen()) {
			return true;
		}
		if (((MainActivity)getActivity()).mPlayerBarFragment != null && ((MainActivity)getActivity()).mPlayerBarFragment.isContentOpened()) {
			// Minimize player
			if (!((MainActivity)getActivity()).mPlayerBarFragment.removeAllFragments())
                ((MainActivity)getActivity()).mPlayerBarFragment.closeContent();
            return true;
		} else {
			if (getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                setTitle(false);
                getActivity().getSupportFragmentManager().popBackStack();
                return true;
			} /*else
				finish();*/
		}
        return false;
	}

    public void setTitle(boolean isOnResume){

        if(isOnResume) {
            if (stack_text != null && stack_text.size() > 0)
                ((MainActivity) getActivity()).showBackButtonWithTitle(stack_text.get(stack_text.size() - 1), "");
            else {
                String title = getResources().getString(
                        R.string.social_profile_title_bar_text_my_plofile);
				if(!mApplicationConfigurations.getPartnerUserId().equals(requestedUesrId))
					title = getResources().getString(
							R.string.social_profile_title_bar_text_user_plofile_1);
                ((MainActivity) getActivity()).showBackButtonWithTitle(title, "");
            }
        }else{
			if (stack_text != null && stack_text.size() > 1) {
				((MainActivity) getActivity()).showBackButtonWithTitle(stack_text.pop(), "");
			} else if (stack_text != null && stack_text.size() > 0) {
				((MainActivity) getActivity()).showBackButtonWithTitle(stack_text.get(0), "");
			} else {
                String title = getResources().getString(
                        R.string.social_profile_title_bar_text_my_plofile);
				if(!mApplicationConfigurations.getPartnerUserId().equals(requestedUesrId))
					title = getResources().getString(
							R.string.social_profile_title_bar_text_user_plofile_1);
                ((MainActivity)getActivity()).showBackButtonWithTitle(title, "");
            }
        }
        setNavigationClick();
		Utils.setToolbarColor(((MainActivity) getActivity()));
    }

    public void setNavigationClick(){

		if(((MainActivity)getActivity()).mToolbar!=null)
			((MainActivity)getActivity()).mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(getActivity()!=null)
						onBackPressed();
				}
			});
    }

    @Override
    public void setTitle(boolean needOnlyHight, boolean needToSetTitle) {

    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			onBackPressed();
			return true;
		} else
			return super.onOptionsItemSelected(item);
	}

	/**
	 * set actionbar title for specific text
	 *
	 * @param text
	 */
	public void setTitleBarText(String text) {
        if(getActivity()==null)
            return;
		if (TextUtils.isEmpty(text)) {

			String title = Utils.getMultilanguageTextLayOut(
                    getActivity(),
					getResources().getString(
							R.string.social_profile_title_bar_text_my_plofile));

            ((MainActivity)getActivity()).showBackButtonWithTitle(title, "");
			if (!stack_text.contains(title))
				stack_text.push(title);

            ((MainActivity)getActivity()).showBackButtonWithTitle(title, "");

		} else {
			String title = text;

            ((MainActivity)getActivity()).showBackButtonWithTitle(title, "");
			if (!stack_text.contains(title))
				stack_text.push(title);

            ((MainActivity)getActivity()).showBackButtonWithTitle(
                    Utils.getMultilanguageTextLayOut(getActivity(), text), "");
		}
	}

	String title = "";

	/**
	 * set actionbar title for Favorite content
	 *
	 * @param mMediaType
	 * @param userId
	 * @param size
	 */
	private void setTitleForFavorite(MediaType mMediaType, String userId,
			int size) {
		// SetS title bar
		boolean isMe;

		if (mApplicationConfigurations.getPartnerUserId().equalsIgnoreCase(
				userId)) {
			isMe = true;
		} else {
			isMe = false;
		}

		if (isMe) {
			if (mMediaType == MediaType.ALBUM) {
				title = getResources().getString(
						R.string.favorite_fragment_title_albums/* , size */);
				// title = getResources().getString(
				// R.string.favorite_dialog_albums);
			} else if (mMediaType == MediaType.TRACK) {
				title = getResources().getString(
						R.string.favorite_fragment_title_songs/* , size */);
				// title = getResources()
				// .getString(R.string.favorite_dialog_songs);
			} else if (mMediaType == MediaType.PLAYLIST) {
				title = getResources().getString(
						R.string.favorite_fragment_title_playlists/* , size */);
				// title = getResources().getString(
				// R.string.favorite_dialog_playlists);
			} else if (mMediaType == MediaType.VIDEO) {
				title = getResources().getString(
						R.string.favorite_fragment_title_videos/* , size */);
				// title = getResources().getString(
				// R.string.favorite_dialog_videos);
			} else if (mMediaType == MediaType.ARTIST) {
				title = getResources()
						.getString(
								R.string.favorite_fragment_title_artists_other/*
																			 * ,
																			 * size
																			 */);
				// title = getResources().getString(
				// R.string.favorite_dialog_artists);
			}
			title = Utils.getMultilanguageText(getActivity(), title);
			if (title != null) {
				title = title.trim() + " (" + size + ")";
			}
		} else {
			if (mMediaType == MediaType.ALBUM) {
				title = getResources()
						.getString(
								R.string.favorite_fragment_title_albums_other/*
																			 * ,
																			 * size
																			 */);
			} else if (mMediaType == MediaType.TRACK) {
				title = getResources()
						.getString(R.string.favorite_fragment_title_songs_other/*
																				 * ,
																				 * size
																				 */);
			} else if (mMediaType == MediaType.PLAYLIST) {
				title = getResources().getString(
						R.string.favorite_fragment_title_playlists_other/*
																		 * ,
																		 * size
																		 */);
			} else if (mMediaType == MediaType.VIDEO) {
				title = getResources()
						.getString(
								R.string.favorite_fragment_title_videos_other/*
																			 * ,
																			 * size
																			 */);
			} else if (mMediaType == MediaType.ARTIST) {
				title = getResources()
						.getString(
								R.string.favorite_fragment_title_artists_other/*
																			 * ,
																			 * size
																			 */);
			}

			title = Utils.getMultilanguageText(getActivity(), title);
			if (title != null) {
				title = title.trim() + " (" + size + ")";
			}
		}

		setTitleBarText(title);
	}

	FavoritesFragment favoritesFragment;

	/**
	 * display favortie fragment for specific media type
	 *
	 * @param mediaType
	 * @param userId
	 */
	private void showFavoriteFragmentFor(MediaType mediaType, String userId) {

		boolean isAppUser = false;
		ApplicationConfigurations applicationConfigurations = mDataManager
				.getApplicationConfigurations();
		String applicationUserId = applicationConfigurations.getPartnerUserId();
		if (TextUtils.isEmpty(userId) || applicationUserId.equals(userId)) {
			isAppUser = true;
		}

		favoritesFragment = new FavoritesFragment();
        favoritesFragment.init(isAppUser);
		favoritesFragment.setOnMediaItemOptionSelectedListener(this);
		favoritesFragment.setOnMediaItemsLoadedListener(this);
        favoritesFragment.setProfileActivity(this);

		Bundle arguments = new Bundle();
		arguments.putSerializable(
				FavoritesFragment.FRAGMENT_ARGUMENT_MEDIA_TYPE,
				(Serializable) mediaType);
		arguments
				.putString(FavoritesFragment.FRAGMENT_ARGUMENT_USER_ID, userId);
		arguments.putString(
				MediaTileGridFragment.FLURRY_SUB_SECTION_DESCRIPTION,
				FlurryConstants.FlurrySubSectionDescription.MyFavorite
						.toString());
		favoritesFragment.setArguments(arguments);

		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit, R.anim.slide_right_enter,
				R.anim.slide_right_exit);
		fragmentTransaction.add(R.id.main_fragmant_container,
				favoritesFragment,"FavoritesFragment");
		fragmentTransaction
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		fragmentTransaction.addToBackStack("FavoritesFragment");
		fragmentTransaction.commit();


		setNavigationClick();
	}

	/**
	 * display Redeem fragment
	 */
	@Override
	public void onCurrencySectionSelected(String userId, int currency) {
		// shows the redeem page.
		setTitleBarText(getString(R.string.redeem_text));
		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit, R.anim.slide_right_enter,
				R.anim.slide_right_exit);

		RedeemFragment redeemFragment = new RedeemFragment();
        redeemFragment.setProfileActivity(this);
		Bundle arguments = new Bundle();
		arguments.putInt(RedeemActivity.ARGUMENT_REDEEM, currency);
		redeemFragment.setArguments(arguments);

		fragmentTransaction.add(R.id.main_fragmant_container,
				redeemFragment,"RedeemFragment");
		fragmentTransaction
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		fragmentTransaction.addToBackStack("RedeemFragment");
		fragmentTransaction.commit();
	}

	/**
	 * display download content
	 */
	@Override
	public void onDownloadSectionSelected(String userId) {
		/*Intent intent = new Intent(getActivity().getApplicationContext(),
				MyCollectionActivity.class);
		startActivity(intent);*/
		MyCollectionActivity mTilesFragment = new MyCollectionActivity();
		FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_and_show_bottom_enter,
				R.anim.slide_and_show_bottom_exit);
		fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
				mTilesFragment, "MyCollectionActivity");
		fragmentTransaction.addToBackStack("MyCollectionActivity");
		if(Constants.IS_COMMITALLOWSTATE)
			fragmentTransaction.commitAllowingStateLoss();
		else
			fragmentTransaction.commit();
	}

	/**
	 * display Badge fragment content
	 */
	@Override
	public void onBadgesSectionSelected(String userId) {
		try {
			FragmentTransaction fragmentTransaction = mFragmentManager
					.beginTransaction();
			fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
					R.anim.slide_left_exit, R.anim.slide_right_enter,
					R.anim.slide_right_exit);

			BadgesFragment badgesFragment = new BadgesFragment();
            badgesFragment.setProfileActivity(this);
			Bundle arguments = new Bundle();
			arguments.putString(BadgesFragment.FRAGMENT_ARGUMENT_USER_ID,
					userId);
			badgesFragment.setArguments(arguments);

			fragmentTransaction.add(R.id.main_fragmant_container,
					badgesFragment,"BadgesFragment");
			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			fragmentTransaction.addToBackStack("BadgesFragment");
			fragmentTransaction.commit();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	/**
	 * display leaderboard content
	 */
	@Override
	public void onLeaderboardSectionSelected(String userId) {
		// Shows the leaderboard to the given user.
		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit, R.anim.slide_right_enter,
				R.anim.slide_right_exit);

		LeaderboardFragment leaderboardFragment = new LeaderboardFragment();
        leaderboardFragment.setProfileActivity(this);

		leaderboardFragment.setOnLeaderboardUserSelectedListener(this);

		Bundle arguments = new Bundle();
		arguments.putString(ProfileFragment.FRAGMENT_ARGUMENT_USER_ID, userId);
		leaderboardFragment.setArguments(arguments);

		fragmentTransaction.add(R.id.main_fragmant_container,
				leaderboardFragment,"LeaderboardFragment");
		fragmentTransaction
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		fragmentTransaction.addToBackStack("LeaderboardFragment");
		fragmentTransaction.commit();
	}

	@Override
	public void onMyplaylistsSectionSelected(String userId) {
		boolean isAppUser = false;
		ApplicationConfigurations applicationConfigurations = mDataManager
				.getApplicationConfigurations();
		String applicationUserId = applicationConfigurations.getPartnerUserId();
		if (TextUtils.isEmpty(userId) || applicationUserId.equals(userId)) {
			isAppUser = true;
		}
        boolean isFromProfile = true;
		ItemableTilesFragment mTilesFragment = new ItemableTilesFragment();
        mTilesFragment.setProfileActivity(this);
        mTilesFragment.init(MediaType.PLAYLIST, null, isAppUser,isFromProfile);

		FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit, R.anim.slide_right_enter,
				R.anim.slide_right_exit);

		fragmentTransaction.add(R.id.main_fragmant_container,
                mTilesFragment, "ItemableTilesFragment");
		fragmentTransaction
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		fragmentTransaction.addToBackStack("ItemableTilesFragment");
		fragmentTransaction.commit();
	}

	@Override
	public void onFavAlbumsSectionSelected(String userId) {
		showFavoriteFragmentFor(MediaType.ALBUM, userId);
	}

	@Override
	public void onFavSongsSectionSelected(String userId) {
		showFavoriteFragmentFor(MediaType.TRACK, userId);
	}

	@Override
	public void onFavPlaylistsSectionSelected(String userId) {
		showFavoriteFragmentFor(MediaType.PLAYLIST, userId);
	}

	@Override
	public void onFavVideosSectionSelected(String userId) {
		showFavoriteFragmentFor(MediaType.VIDEO, userId);
	}


	// ======================================================
	// Leaderboard Callbacks.
	// ======================================================

	@Override
	public void onLeaderboardUserSelectedListener(String selectedUserId) {
		// shows the profile of the selected user.

		ProfileFragment profileFragment = new ProfileFragment();
        profileFragment.setProfileActivity(this);
		profileFragment.setOnProfileSectionSelectedListener(this);

		Bundle arguments = new Bundle();
		arguments.putString(ProfileFragment.FRAGMENT_ARGUMENT_USER_ID,
				selectedUserId);
		profileFragment.setArguments(arguments);

		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit, R.anim.slide_right_enter,
				R.anim.slide_right_exit);

		fragmentTransaction.add(R.id.main_fragmant_container,
				profileFragment,"ProfileFragment");
		fragmentTransaction
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		fragmentTransaction.addToBackStack("ProfileFragment");
		fragmentTransaction.commit();
	}

	// ======================================================
	// Profile Favorites callbacks.
	// ======================================================

	@Override
	public void onMediaItemsLoaded(MediaType mediaType, String userId,
			List<MediaItem> mediaItems) {
		setTitleForFavorite(mediaType, userId,
				(mediaItems != null ? mediaItems.size() : 0));
	}

	// ======================================================
	// Communication callbacks.
	// ======================================================

	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS)
			showLoadingDialog(R.string.application_dialog_loading_content);
		else if (operationId == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS_SONGS)
			showLoadingDialog(R.string.application_dialog_loading_content);
		else if (operationId == OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE)
			showLoadingDialog(R.string.application_dialog_loading_content);
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
					}
				}
			} else if (operationId == OperationDefinition.Hungama.OperationId.SUBSCRIPTION_CHECK) {
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
						favoritesFragment
								.removeAndRefreshList(mediaItemToBeRemoved);
						Utils.makeText(getActivity(),
								removeFromFavoriteResponse.getMessage(),
								Toast.LENGTH_LONG).show();
					} else {
						Utils.makeText(
                                getActivity(),
								getResources().getString(
										R.string.favorite_error_removing,
										mediaItemToBeRemoved.getTitle()),
								Toast.LENGTH_LONG).show();
					}
				} catch (Exception e) {
					Logger.e(getClass().getName() + ":601", e.toString());
				}
			}
			hideLoadingDialog();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
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

	// ======================================================
	// MediaDetails callbacks.
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
		} else if (mediaItem.getMediaContentType() == MediaContentType.RADIO) {
			mDataManager.getRadioTopArtistSongs(mediaItem, this);
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
					FlurryConstants.FlurrySourceSection.Profile.toString());

			mediaDetailsFragment.setArguments(bundle);

			fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
					mediaDetailsFragment, "MediaDetailsActivitySearch111");
			fragmentTransaction.addToBackStack("MediaDetailsActivitySearch111");
			if(Constants.IS_COMMITALLOWSTATE)
				fragmentTransaction.commitAllowingStateLoss();
			else
				fragmentTransaction.commit();

//			intent = new Intent(getActivity(), MediaDetailsActivity.class);
//			intent.putExtra(MediaDetailsActivity.EXTRA_MEDIA_ITEM,
//					(Serializable) mediaItem);
//			intent.putExtra(MediaDetailsActivity.FLURRY_SOURCE_SECTION,
//					FlurryConstants.FlurrySourceSection.Profile.toString());
//			startActivity(intent);
		} else if (mediaItem.getMediaContentType() == MediaContentType.RADIO) {

			mDataManager.getRadioTopArtistSongs(mediaItem, this);

		} else {
			intent = new Intent(getActivity(), VideoActivity.class);
			intent.putExtra(VideoActivity.EXTRA_MEDIA_ITEM_VIDEO,
					(Serializable) mediaItem);
			intent.putExtra(MediaDetailsActivity.FLURRY_SOURCE_SECTION,
					FlurryConstants.FlurrySourceSection.Profile.toString());
			startActivity(intent);
		}
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
					CacheManager.saveOfflineAction(getActivity(), mediaItem, null);

				if (mediaItem.getMediaType() == MediaType.ALBUM) {
					Utils.saveOfflineFlurryEvent(getActivity(),
							FlurryConstants.FlurryCaching.LongPressMenuAlbum
									.toString(), mediaItem);
				} else {
					Utils.saveOfflineFlurryEvent(getActivity(),
							FlurryConstants.FlurryCaching.LongPressMenuPlaylist
									.toString(), mediaItem);
				}
			}
		} else {
			CacheManager.saveOfflineAction(getActivity(), mediaItem, null);
			Utils.saveOfflineFlurryEvent(
                    getActivity(),
					FlurryConstants.FlurryCaching.LongPressMenuVideo.toString(),
					mediaItem);
		}
	}

	@Override
	public void onFavArtistsSectionSelected(String userId) {
		showFavoriteFragmentFor(MediaType.ARTIST, userId);
	}

	@Override
	public void onMediaItemOptionPlayAndOpenSelected(MediaItem mediaItem,
			int position) {
	}

}
