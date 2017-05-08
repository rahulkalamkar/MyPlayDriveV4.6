package com.hungama.myplay.activity.ui.fragments.social;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.social.Badge;
import com.hungama.myplay.activity.data.dao.hungama.social.LeaderBoardUser;
import com.hungama.myplay.activity.data.dao.hungama.social.Profile;
import com.hungama.myplay.activity.data.dao.hungama.social.UserBadges;
import com.hungama.myplay.activity.data.dao.hungama.social.UserFavoriteAlbums;
import com.hungama.myplay.activity.data.dao.hungama.social.UserFavoriteOnDemand;
import com.hungama.myplay.activity.data.dao.hungama.social.UserFavoritePlaylists;
import com.hungama.myplay.activity.data.dao.hungama.social.UserFavoriteSongs;
import com.hungama.myplay.activity.data.dao.hungama.social.UserFavoriteVideos;
import com.hungama.myplay.activity.data.dao.hungama.social.UserLeaderBoardUsers;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.SocialProfileOperation;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.ProfileActivity;
import com.hungama.myplay.activity.ui.fragments.MainFragment;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class ProfileFragment extends MainFragment implements OnClickListener,
		CommunicationOperationListener {

	private static final String TAG = "ProfileFragment";

	public static final String FRAGMENT_ARGUMENT_USER_ID = "fragment_argument_user_id";

	public interface OnProfileSectionSelectedListener {

		public void onCurrencySectionSelected(String userId, int currency);

		public void onDownloadSectionSelected(String userId);

		public void onBadgesSectionSelected(String userId);

		public void onLeaderboardSectionSelected(String userId);

		public void onMyplaylistsSectionSelected(String userId);

		public void onFavAlbumsSectionSelected(String userId);

		public void onFavSongsSectionSelected(String userId);

		public void onFavPlaylistsSectionSelected(String userId);

		public void onFavVideosSectionSelected(String userId);

		public void onFavArtistsSectionSelected(String userId);

	}

	public void setOnProfileSectionSelectedListener(
			OnProfileSectionSelectedListener listener) {
		mOnProfileSectionSelectedListener = listener;
	}

	private OnProfileSectionSelectedListener mOnProfileSectionSelectedListener;

	private DataManager mDataManager;
	private String mUserId;
	private Profile mUserProfile = null;

	private boolean mIsApplicationUser = true;

	private int mScreenWidth = 0;

	// user bar.
	private RelativeLayout mContainerUserBar;
	private ImageView mImageUserThumbnail;
	private LanguageTextView mTextUserName;
	private RelativeLayout mContainerUserCurrency;
	private TextView mTextUserCurrencyValue;
	private RelativeLayout mContainerUserDownloads;
	private TextView mTextUserDownloadsValue;
	private LanguageTextView mTextUserCurrentLevel;

	private TextView mMyCollectionText;
	private TextView mRedeemText;

	// Level bar.
	private RelativeLayout mContainerLevelBar;
	private ProgressBar mProgressLevelBar;
	private TextView mTextLevelZero;
	private LinearLayout mContainerLevels;

	// badges section.
	private LinearLayout mContainerBadges;
	private TextView mTextBadgesValue;
	private ImageView mImageBadge1;
	private LanguageTextView mTextBadge1;
	private ImageView mImageBadge2;
	private LanguageTextView mTextBadge2;
	private ImageView mImageBadge3;
	private LanguageTextView mTextBadge3;

	// leaderboard section.
	private RelativeLayout mContainerLeaderboard;
	private LinearLayout mHeaderLeaderboard;
	private TextView mTextLeaderboardValue;
	private RelativeLayout mContainerLeaderboardUser1;
	private RelativeLayout mContainerLeaderboardUser2;
	private RelativeLayout mContainerLeaderboardUser3;
	private TextView mTextLeaderboardUser1Rank;
	private TextView mTextLeaderboardUser1Name;
	private TextView mTextLeaderboardUser1TotalPoints;
	private TextView mTextLeaderboardUser2Rank;
	private TextView mTextLeaderboardUser2Name;
	private TextView mTextLeaderboardUser2TotalPoints;
	private TextView mTextLeaderboardUser3Rank;
	private TextView mTextLeaderboardUser3Name;
	private TextView mTextLeaderboardUser3TotalPoints;

	// my playlists section.
	private LinearLayout mContainerMyPlaylists;
	private TextView mTextMyPlaylistsValue;
	private TextView mTextMyPlaylist1Name;
	private TextView mTextMyPlaylist2Name;
	private TextView mTextMyPlaylist3Name;
	private ImageView mImageMoreIndicator;
	private TextView mTextMyPlaylistEmpty;

	// favorite albums.
	private LinearLayout mContainerFavoriteAlbums;
	private TextView mTextFavoriteFavoriteAlbumsValue;
	private ImageView mTextFavoriteFavoriteAlbum1;
	private ImageView mTextFavoriteFavoriteAlbum2;
	private ImageView mTextFavoriteFavoriteAlbum3;

	// favorite songs.
	private LinearLayout mContainerFavoriteSongs;
	private TextView mTextFavoriteSongsValue;
	private LanguageTextView mTextFavoriteSong1Name;
	private LanguageTextView mTextFavoriteSong2Name;
	private LanguageTextView mTextFavoriteSong3Name;

	// favorite playlists.
	private LinearLayout mContainerFavoritePlaylists;
	private TextView mTextFavoritePlaylistValue;
	private LanguageTextView mTextFavoritePlaylist1Name;
	private LanguageTextView mTextFavoritePlaylist2Name;
	private LanguageTextView mTextFavoritePlaylist3Name;

	// favorite videos.
	private LinearLayout mContainerFavoriteVideos;
	private TextView mTextFavoriteVideosValue;
	private ImageView mTextFavoriteVideo1;
	private ImageView mTextFavoriteVideo2;
	private ImageView mTextFavoriteVideo3;

	// favorite artists.
	private LinearLayout mContainerFavoriteArtists;
	private TextView mTextFavoriteArtistsValue;
	private LanguageTextView mTextFavoriteArtist1Name;
	private LanguageTextView mTextFavoriteArtist2Name;
	private LanguageTextView mTextFavoriteArtist3Name;

	// ======================================================
	// Life cycle.
	// ======================================================
	private ApplicationConfigurations mApplicationConfigurations;

    ProfileActivity profileActivity;
    public void setProfileActivity(ProfileActivity profileActivity) {
        this.profileActivity = profileActivity;
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		Analytics.postCrashlitycsLog(getActivity(), ProfileFragment.class.getName());
	}

	View rootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_social_profile,
				container, false);
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(rootView, getActivity());
		}

		initializeUserControls(rootView);

		adjustControllersSizes();

		// hides all the sections.
		mTextUserCurrentLevel.setVisibility(View.INVISIBLE);
		mContainerBadges.setVisibility(View.GONE);
		mContainerLeaderboard.setVisibility(View.GONE);
		mContainerMyPlaylists.setVisibility(View.GONE);
		mContainerFavoriteAlbums.setVisibility(View.GONE);
		mContainerFavoriteSongs.setVisibility(View.GONE);
		mContainerFavoritePlaylists.setVisibility(View.GONE);
		mContainerFavoriteVideos.setVisibility(View.GONE);
		mContainerFavoriteArtists.setVisibility(View.GONE);

		setControllersListeners();
		rootView.invalidate();
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mUserProfile == null) {
			// gets the user id.
			Bundle arguments = getArguments();
			if (arguments != null
					&& arguments.containsKey(FRAGMENT_ARGUMENT_USER_ID)) {
				mUserId = arguments.getString(FRAGMENT_ARGUMENT_USER_ID);

				// checks if the given user is the application user.
				ApplicationConfigurations applicationConfigurations = mDataManager
						.getApplicationConfigurations();
				String applicationUserId = applicationConfigurations
						.getPartnerUserId();
				if (TextUtils.isEmpty(mUserId)
						|| applicationUserId.equals(mUserId)) {
					mUserId = mDataManager.getApplicationConfigurations()
							.getPartnerUserId();
					mIsApplicationUser = true;

				} else {
					mIsApplicationUser = false;
				}
				Logger.i(TAG, "User Id: " + mUserId);
				mDataManager.getUserProfile(getActivity(), mUserId, this);

			} else {
				throw new IllegalArgumentException(
						"ProfileFragment must be created with a user id argument for key: "
								+ FRAGMENT_ARGUMENT_USER_ID);
			}

		} else {
			// resets the title for this fragment.
//			String title = null;
//			if (mIsApplicationUser || TextUtils.isEmpty(mUserProfile.name)) {
//				title = getResources().getString(
//						R.string.social_profile_title_bar_text_my_plofile);
//			} else {
//				title = mUserProfile.name
//						+ "'s "
//						+ Utils.getMultilanguageTextLayOut(
//								getActivity(),
//								getResources()
//										.getString(
//												R.string.social_profile_title_bar_text_user_plofile_1));
//			}
//            profileActivity.setTitleBarText(title);

			/*
			 * Android is a piece of shit, you can't get views measures like in
			 * any normal platform.
			 */
//			ViewTreeObserver viewTreeObserver = getView().getViewTreeObserver();
//			if (viewTreeObserver.isAlive()) {
//				viewTreeObserver
//						.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
//							@Override
//							public void onGlobalLayout() {
//								// removes the listener.
//								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//									getView().getViewTreeObserver()
//											.removeOnGlobalLayoutListener(this);
//
//								} else {
//									getView().getViewTreeObserver()
//											.removeGlobalOnLayoutListener(this);
//								}
//								// draws the progress bar.
//								populateUserControlls();
//							}
//						});
//			}
		}

		if (mDataManager.getApplicationConfigurations().getPartnerUserId()
				.equalsIgnoreCase(mUserId)) {
			Analytics.logEvent("My Profile");
		} else {
			Analytics.logEvent("Others Profile");
		}
	}

	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
	public void onStart() {
		super.onStart();
		Analytics.startSession(getActivity(), this);
		Analytics.onPageView();
	}

	@Override
	public void onStop() {
		super.onStop();

		Analytics.onEndSession(getActivity());
	}

	@Override
	public void onClick(View view) {
		try {
			int viewId = view.getId();

			if (viewId == R.id.profile_user_bar_currency) {
				if (mOnProfileSectionSelectedListener != null) {
					int currency = (int) mUserProfile.points;
					mOnProfileSectionSelectedListener
							.onCurrencySectionSelected(mUserId, currency);
				}
			} else if (viewId == R.id.profile_user_bar_download) {
				if (mOnProfileSectionSelectedListener != null)
					mOnProfileSectionSelectedListener
							.onDownloadSectionSelected(mUserId);

			} else if (viewId == R.id.social_profile_section_badges) {
				if (mOnProfileSectionSelectedListener != null)
					mOnProfileSectionSelectedListener
							.onBadgesSectionSelected(mUserId);

			} else if (viewId == R.id.social_profile_section_leaderboard) {
				if (mOnProfileSectionSelectedListener != null)
					mOnProfileSectionSelectedListener
							.onLeaderboardSectionSelected(mApplicationConfigurations.getPartnerUserId());//mUserId

			} else if (viewId == R.id.social_profile_section_my_playlists) {
				if (mOnProfileSectionSelectedListener != null)
					mOnProfileSectionSelectedListener
							.onMyplaylistsSectionSelected(mUserId);

			} else if (viewId == R.id.social_profile_section_fav_albums) {
				if (mOnProfileSectionSelectedListener != null)
					mOnProfileSectionSelectedListener
							.onFavAlbumsSectionSelected(mUserId);

			} else if (viewId == R.id.social_profile_section_fav_songs) {
				if (mOnProfileSectionSelectedListener != null)
					Logger.i(TAG, "User Id: " + mUserId + " FavSongs");
				mOnProfileSectionSelectedListener
						.onFavSongsSectionSelected(mUserId);

			} else if (viewId == R.id.social_profile_section_fav_playlists) {
				if (mOnProfileSectionSelectedListener != null)
					mOnProfileSectionSelectedListener
							.onFavPlaylistsSectionSelected(mUserId);

			} else if (viewId == R.id.social_profile_section_fav_videos) {
				if (mOnProfileSectionSelectedListener != null)
					mOnProfileSectionSelectedListener
							.onFavVideosSectionSelected(mUserId);

			} else if (viewId == R.id.social_profile_section_fav_artists) {
				if (mOnProfileSectionSelectedListener != null)
					Logger.i(TAG, "User Id: " + mUserId + " FavArtists");
				mOnProfileSectionSelectedListener
						.onFavArtistsSectionSelected(mUserId);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	// ======================================================
	// Communication callbacks.
	// ======================================================

	boolean isCall;

	@Override
	public void onStart(int operationId) {

		if (!isCall)
			if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE) {
				isCall = true;
				if (((MainActivity) getActivity()) != null)
					((MainActivity) getActivity())
							.showLoadingDialog(R.string.application_dialog_loading_content);
			}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE) {
				mUserProfile = (Profile) responseObjects
						.get(SocialProfileOperation.RESULT_KEY_PROFILE);

				// sets the title.
				String title = null;

				if (mIsApplicationUser) {

					if (!isDetached()) {
						if (getActivity() != null) {
							title = getString(R.string.social_profile_title_bar_text_my_plofile);
						}
					} else {
						title = getString(R.string.social_profile_title_bar_text_my_plofile);
					}
				} else if (TextUtils.isEmpty(mUserProfile.name)) {
					if (!isDetached()) {
						if (getActivity() != null) {
							title = getString(R.string.social_profile_title_bar_text_user_plofile_1);
						}
					} else {
						title = getString(R.string.social_profile_title_bar_text_user_plofile_1);
					}
				} else {
					if (!isDetached() && getActivity() != null) {
						title = mUserProfile.name
								+ "'s "
								+ Utils.getMultilanguageTextLayOut(
										getActivity(),
										getResources()
												.getString(
														R.string.social_profile_title_bar_text_user_plofile_1));
					} else {
						title = mUserProfile.name + "Profile";
					}
				}
				if (getActivity() != null) {
                    profileActivity.setTitleBarText(title);
				}
				// populates the sections based the given profile.
				populateUserControlls();
				// hideLoadingDialog();

				if (mUserProfile != null) {
					Utils.updateUserPointUATag(mUserProfile.points);
				}
			}
		} catch (Exception e) {
            e.printStackTrace();
			Logger.printStackTrace(e);
		}
		try {
			((MainActivity) getActivity()).hideLoadingDialog();
		} catch (Exception e) {
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {

		if (isRemoving() || isDetached() || !isInLayout()) {
			return;
		}

		if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE
				&& !TextUtils.isEmpty(errorMessage)) {
			if (!TextUtils.isEmpty(errorMessage)) {
				Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT)
						.show();
			}
		}
		try {
			((MainActivity) getActivity()).hideLoadingDialog();
		} catch (Exception e) {
		}
	}

	public void initializeUserControls(View rootView) {

		// the user bar.
		mContainerUserBar = (RelativeLayout) rootView
				.findViewById(R.id.profile_user_bar);
		mImageUserThumbnail = (ImageView) mContainerUserBar
				.findViewById(R.id.profile_user_bar_user_thumbnail);
		mTextUserName = (LanguageTextView) mContainerUserBar
				.findViewById(R.id.profile_user_bar_text_user_name);
		mContainerUserCurrency = (RelativeLayout) mContainerUserBar
				.findViewById(R.id.profile_user_bar_currency);
		mTextUserCurrencyValue = (TextView) mContainerUserBar
				.findViewById(R.id.profile_user_bar_currency_text_value);
		mContainerUserDownloads = (RelativeLayout) mContainerUserBar
				.findViewById(R.id.profile_user_bar_download);
		mTextUserDownloadsValue = (TextView) mContainerUserBar
				.findViewById(R.id.profile_user_bar_download_text_value);
		mTextUserCurrentLevel = (LanguageTextView) rootView
				.findViewById(R.id.social_profile_user_bar_text_current_level);

		mMyCollectionText = (TextView) rootView
				.findViewById(R.id.profile_user_my_collection_text);
		mRedeemText = (TextView) rootView
				.findViewById(R.id.profile_user_redeem_text);

		// Level Bar.
		mContainerLevelBar = (RelativeLayout) rootView
				.findViewById(R.id.social_profile_user_bar_level_bar);
		mProgressLevelBar = (ProgressBar) mContainerLevelBar
				.findViewById(R.id.social_profile_user_bar_level);
		mTextLevelZero = (TextView) mContainerLevelBar
				.findViewById(R.id.social_profile_user_bar_level_bar_level1);
		mContainerLevels = (LinearLayout) mContainerLevelBar
				.findViewById(R.id.social_profile_user_bar_level_bar_level_container);

		mTextLevelZero.setVisibility(View.INVISIBLE);

		// Badges section.
		mContainerBadges = (LinearLayout) rootView
				.findViewById(R.id.social_profile_section_badges);

		mTextBadgesValue = (TextView) mContainerBadges
				.findViewById(R.id.social_profile_section_badges_header_value);
		mImageBadge1 = (ImageView) mContainerBadges
				.findViewById(R.id.social_profile_section_badges_item1_image);
		mTextBadge1 = (LanguageTextView) mContainerBadges
				.findViewById(R.id.social_profile_section_badges_item1_text);
		mImageBadge2 = (ImageView) mContainerBadges
				.findViewById(R.id.social_profile_section_badges_item2_image);
		mTextBadge2 = (LanguageTextView) mContainerBadges
				.findViewById(R.id.social_profile_section_badges_item2_text);
		mImageBadge3 = (ImageView) mContainerBadges
				.findViewById(R.id.social_profile_section_badges_item3_image);
		mTextBadge3 = (LanguageTextView) mContainerBadges
				.findViewById(R.id.social_profile_section_badges_item3_text);

		// leaderboard section.
		mContainerLeaderboard = (RelativeLayout) rootView
				.findViewById(R.id.social_profile_section_leaderboard);
		mHeaderLeaderboard = (LinearLayout) mContainerLeaderboard
				.findViewById(R.id.social_profile_section_leaderboard_header);
		mTextLeaderboardValue = (TextView) mContainerLeaderboard
				.findViewById(R.id.social_profile_section_leaderboard_header_value);

		mContainerLeaderboardUser1 = (RelativeLayout) mContainerLeaderboard
				.findViewById(R.id.social_profile_section_leaderboard_item1);
		mContainerLeaderboardUser2 = (RelativeLayout) mContainerLeaderboard
				.findViewById(R.id.social_profile_section_leaderboard_item2);
		mContainerLeaderboardUser3 = (RelativeLayout) mContainerLeaderboard
				.findViewById(R.id.social_profile_section_leaderboard_item3);

		mTextLeaderboardUser1Rank = (TextView) mContainerLeaderboard
				.findViewById(R.id.social_profile_section_leaderboard_item1_rank);
		mTextLeaderboardUser1Name = (TextView) mContainerLeaderboard
				.findViewById(R.id.social_profile_section_leaderboard_item1_user_name);
		mTextLeaderboardUser1TotalPoints = (TextView) mContainerLeaderboard
				.findViewById(R.id.social_profile_section_leaderboard_item1_total_points);
		mTextLeaderboardUser2Rank = (TextView) mContainerLeaderboard
				.findViewById(R.id.social_profile_section_leaderboard_item2_rank);
		mTextLeaderboardUser2Name = (TextView) mContainerLeaderboard
				.findViewById(R.id.social_profile_section_leaderboard_item2_user_name);
		mTextLeaderboardUser2TotalPoints = (TextView) mContainerLeaderboard
				.findViewById(R.id.social_profile_section_leaderboard_item2_total_points);
		mTextLeaderboardUser3Rank = (TextView) mContainerLeaderboard
				.findViewById(R.id.social_profile_section_leaderboard_item3_rank);
		mTextLeaderboardUser3Name = (TextView) mContainerLeaderboard
				.findViewById(R.id.social_profile_section_leaderboard_item3_user_name);
		mTextLeaderboardUser3TotalPoints = (TextView) mContainerLeaderboard
				.findViewById(R.id.social_profile_section_leaderboard_item3_total_points);

		// my playlists section.
		mContainerMyPlaylists = (LinearLayout) rootView
				.findViewById(R.id.social_profile_section_my_playlists);

		mTextMyPlaylistsValue = (TextView) mContainerMyPlaylists
				.findViewById(R.id.social_profile_section_my_playlists_header_value);
		mTextMyPlaylist1Name = (TextView) mContainerMyPlaylists
				.findViewById(R.id.social_profile_section_my_playlists_item1);
		mTextMyPlaylist2Name = (TextView) mContainerMyPlaylists
				.findViewById(R.id.social_profile_section_my_playlists_item2);
		mTextMyPlaylist3Name = (TextView) mContainerMyPlaylists
				.findViewById(R.id.social_profile_section_my_playlists_item3);
		mImageMoreIndicator = (ImageView) mContainerMyPlaylists
				.findViewById(R.id.social_profile_section_my_playlists_more_indicator);
		mTextMyPlaylistEmpty = (TextView) mContainerMyPlaylists
				.findViewById(R.id.social_profile_section_my_playlists_empty);

		// favorite albums.
		mContainerFavoriteAlbums = (LinearLayout) rootView
				.findViewById(R.id.social_profile_section_fav_albums);

		mTextFavoriteFavoriteAlbumsValue = (TextView) mContainerFavoriteAlbums
				.findViewById(R.id.social_profile_section_fav_albums_header_value);
		mTextFavoriteFavoriteAlbum1 = (ImageView) mContainerFavoriteAlbums
				.findViewById(R.id.social_profile_section_fav_albumes_item1_image);
		mTextFavoriteFavoriteAlbum2 = (ImageView) mContainerFavoriteAlbums
				.findViewById(R.id.social_profile_section_fav_albumes_item2_image);
		mTextFavoriteFavoriteAlbum3 = (ImageView) mContainerFavoriteAlbums
				.findViewById(R.id.social_profile_section_fav_albumes_item3_image);

		// favorite songs.
		mContainerFavoriteSongs = (LinearLayout) rootView
				.findViewById(R.id.social_profile_section_fav_songs);

		mTextFavoriteSongsValue = (TextView) mContainerFavoriteSongs
				.findViewById(R.id.social_profile_section_fav_songs_header_value);
		mTextFavoriteSong1Name = (LanguageTextView) mContainerFavoriteSongs
				.findViewById(R.id.social_profile_section_fav_songs_item1);
		mTextFavoriteSong2Name = (LanguageTextView) mContainerFavoriteSongs
				.findViewById(R.id.social_profile_section_fav_songs_item2);
		mTextFavoriteSong3Name = (LanguageTextView) mContainerFavoriteSongs
				.findViewById(R.id.social_profile_section_fav_songs_item3);

		// favorite playlists.
		mContainerFavoritePlaylists = (LinearLayout) rootView
				.findViewById(R.id.social_profile_section_fav_playlists);
		mTextFavoritePlaylistValue = (TextView) mContainerFavoritePlaylists
				.findViewById(R.id.social_profile_section_fav_playlists_header_value);
		mTextFavoritePlaylist1Name = (LanguageTextView) mContainerFavoritePlaylists
				.findViewById(R.id.social_profile_section_fav_playlists_item1);
		mTextFavoritePlaylist2Name = (LanguageTextView) mContainerFavoritePlaylists
				.findViewById(R.id.social_profile_section_fav_playlists_item2);
		mTextFavoritePlaylist3Name = (LanguageTextView) mContainerFavoritePlaylists
				.findViewById(R.id.social_profile_section_fav_playlists_item3);

		// favorite videos.
		mContainerFavoriteVideos = (LinearLayout) rootView
				.findViewById(R.id.social_profile_section_fav_videos);
		mTextFavoriteVideosValue = (TextView) mContainerFavoriteVideos
				.findViewById(R.id.social_profile_section_fav_videos_header_value);
		mTextFavoriteVideo1 = (ImageView) mContainerFavoriteVideos
				.findViewById(R.id.social_profile_section_fav_videos_item1_image);
		mTextFavoriteVideo2 = (ImageView) mContainerFavoriteVideos
				.findViewById(R.id.social_profile_section_fav_videos_item2_image);
		mTextFavoriteVideo3 = (ImageView) mContainerFavoriteVideos
				.findViewById(R.id.social_profile_section_fav_videos_item3_image);

		// favorite songs.
		mContainerFavoriteArtists = (LinearLayout) rootView
				.findViewById(R.id.social_profile_section_fav_artists);
		mTextFavoriteArtistsValue = (TextView) mContainerFavoriteArtists
				.findViewById(R.id.social_profile_section_fav_artists_header_value);
		mTextFavoriteArtist1Name = (LanguageTextView) mContainerFavoriteArtists
				.findViewById(R.id.social_profile_section_fav_artists_item1);
		mTextFavoriteArtist2Name = (LanguageTextView) mContainerFavoriteArtists
				.findViewById(R.id.social_profile_section_fav_artists_item2);
		mTextFavoriteArtist3Name = (LanguageTextView) mContainerFavoriteArtists
				.findViewById(R.id.social_profile_section_fav_artists_item3);
	}

	private void adjustControllersSizes() {

		/*
		 * Calculating the desired width for any section item in the page, to do
		 * that, we based on the structure of the "Badges" section like sizes.
		 */

		int sectionMargin = getResources().getDimensionPixelSize(
				R.dimen.profile_section_item_margin);
		int sectionIndicationWidth = getResources().getDimensionPixelSize(
				R.dimen.profile_section_item_more_indicator_width);

		// measuring the device's screen width. and setting the grid column
		// width.
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
			mScreenWidth = display.getWidth();
		} else {
			Point displaySize = new Point();
			display.getSize(displaySize);
			mScreenWidth = displaySize.x;
		}

		/*
		 * from the width of the screen subtracting the inner item tiles margin
		 * and the more content indicator width, dividing in the number of tiles
		 * in a section row include the header - which is 4.
		 */
		int sectionWidth = (mScreenWidth - (sectionMargin * 3) - sectionIndicationWidth) / 4;

		// leaderboard.
		RelativeLayout.LayoutParams leaderboardHeaderParams = (RelativeLayout.LayoutParams) mHeaderLeaderboard
				.getLayoutParams();
		leaderboardHeaderParams.width = sectionWidth;
		mHeaderLeaderboard.setLayoutParams(leaderboardHeaderParams);
	}

	/**
	 * set listener for controller
	 * 
	 */
	private void setControllersListeners() {
		mImageUserThumbnail.setOnClickListener(null);
		mTextUserName.setOnClickListener(null);
		mContainerLevels.setOnClickListener(null);
		mTextUserCurrentLevel.setOnClickListener(null);
		mContainerBadges.setOnClickListener(this);
		mContainerLeaderboard.setOnClickListener(this);
		mContainerMyPlaylists.setOnClickListener(this);
		mContainerFavoriteAlbums.setOnClickListener(this);
		mContainerFavoriteSongs.setOnClickListener(this);
		mContainerFavoritePlaylists.setOnClickListener(this);
		mContainerFavoriteVideos.setOnClickListener(this);
		mContainerFavoriteArtists.setOnClickListener(this);
	}

	private static final String KEY_LEADERBOARD_CONTAINER = "key_leaderboard_container";
	private static final String KEY_LEADERBOARD_RANK = "key_leaderboard_rank";
	private static final String KEY_LEADERBOARD_NAME = "key_leaderboard_name";
	private static final String KEY_LEADERBOARD_TOTAL = "key_leaderboard_total";

	/**
	 * set all control for visibliliy or invisible
	 */
	private void populateUserControlls() {

		/*
		 * sets the only-can-be-visible buttons when there is no connection or
		 * failed to retrieve normal data.
		 */
		if (mIsApplicationUser) {
			mContainerUserCurrency.setOnClickListener(this);
			mContainerUserDownloads.setOnClickListener(this);
		}

		// populates the user bar.
		try {
			populdateUserBar();
		} catch (Exception e) {
		}
		// populates the badges section.
		try {
			populateBadgesSection();
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			// populates the leader board section.
			populdateLeaderboardSection();
		} catch (Exception e) {
		}
		// populates the user's playlists if is the application's user.
		if (mIsApplicationUser) {
			populateUserPlaylitstsSection();

		} else {
			mContainerMyPlaylists.setVisibility(View.GONE);
		}

		populateFavoritesSections();

		// populdateLevelBar();
		populdateLevelBarNew();
	}

	/**
	 * set up user bar like points and download, etc
	 */
	private void populdateUserBar() {

		try {
			Resources resources = getResources();

			try {
				if (mIsApplicationUser) {
					mContainerUserCurrency.setClickable(true);
					mContainerUserDownloads.setClickable(true);
				} else {
					mContainerUserCurrency.setClickable(false);
					mContainerUserDownloads.setClickable(false);
				}
			} catch (Exception e) {
			}

			if (getActivity() != null
					&& !TextUtils.isEmpty(mApplicationConfigurations
							.getGiGyaFBThumbUrl())) {
				Picasso.with(getActivity()).cancelRequest(mImageUserThumbnail);
				Picasso.with(getActivity())
						.load(mApplicationConfigurations.getGiGyaFBThumbUrl())
						.into(mImageUserThumbnail);
			} else if (!TextUtils.isEmpty(mApplicationConfigurations
					.getGiGyaTwitterThumbUrl())) {
				Picasso.with(getActivity()).cancelRequest(mImageUserThumbnail);
				Picasso.with(getActivity())
						.load(mApplicationConfigurations
								.getGiGyaTwitterThumbUrl())
						.into(mImageUserThumbnail);
			} else if (!TextUtils.isEmpty(mUserProfile.imageUrl)) {
				Picasso.with(getActivity()).cancelRequest(mImageUserThumbnail);
				if (getActivity() != null && mUserProfile.imageUrl != null) {
					Picasso.with(getActivity()).load(mUserProfile.imageUrl)
							.into(mImageUserThumbnail);
				}
			}

			if (!TextUtils.isEmpty(mUserProfile.name)) {
				Utils.SetMultilanguageTextOnTextView(getActivity(),
						mTextUserName, mUserProfile.name);
				mTextUserName.setText(mUserProfile.name);
			} else {
				mTextUserName.setText(Utils.TEXT_EMPTY);
			}
			mTextUserCurrencyValue.setText(Long.toString(mUserProfile.points));
			mTextUserDownloadsValue.setText(Long
					.toString(mUserProfile.collections));

			Utils.SetMultilanguageTextOnTextView(
					getActivity(),
					mTextUserCurrentLevel,
					resources
							.getString(R.string.social_profile_user_bar_current_level_1));
			mTextUserCurrentLevel.append(" " + mUserProfile.currentLevel);
			mTextUserCurrentLevel.setVisibility(View.VISIBLE);

			process("Hungama Level " + mUserProfile.currentLevel);

			if (mDataManager.getApplicationConfigurations().getPartnerUserId()
					.equalsIgnoreCase(mUserId)) {

			} else {
				mRedeemText.setVisibility(View.INVISIBLE);
				mMyCollectionText.setVisibility(View.INVISIBLE);
			}
		} catch (Exception e) {
		} catch (Error e) {
		}
	}

	/**
	 * set up Badge screen
	 */
	private void populateBadgesSection() {
		if (mUserProfile.userBadges != null
				&& !Utils.isListEmpty(mUserProfile.userBadges.badges)) {
			adjustTags(mUserProfile.userBadges);
			mContainerBadges.setVisibility(View.VISIBLE);

			UserBadges userBadges = mUserProfile.userBadges;
			mTextBadgesValue.setText(Integer.toString(userBadges.earnedBadges));

			// gets the badges, sets their names and icons.
			Badge badge1 = userBadges.badges.get(0);
			Badge badge2 = userBadges.badges.get(1);
			Badge badge3 = userBadges.badges.get(2);

			mTextBadge1.setText(badge1.name);
			mTextBadge2.setText(badge2.name);
			mTextBadge3.setText(badge3.name);
			try {
				mImageBadge1
						.setBackgroundColor(getResources()
								.getColor(
										R.color.social_profile_section_content_item_backgorund));
				mImageBadge2
						.setBackgroundColor(getResources()
								.getColor(
										R.color.social_profile_section_content_item_backgorund));
				mImageBadge3
						.setBackgroundColor(getResources()
								.getColor(
										R.color.social_profile_section_content_item_backgorund));
			} catch (Exception e) {
			}

			if (getActivity() != null) {

				if (!TextUtils.isEmpty(badge1.imageUrl)
						&& badge1.imageUrl != null) {
					Picasso.with(getActivity()).cancelRequest(mImageBadge1);
					Picasso.with(getActivity()).load(badge1.imageUrl)
							.into(mImageBadge1);
				}

				if (!TextUtils.isEmpty(badge2.imageUrl)
						&& badge2.imageUrl != null) {
					Picasso.with(getActivity()).cancelRequest(mImageBadge2);
					Picasso.with(getActivity()).load(badge2.imageUrl)
							.into(mImageBadge2);
				}

				if (!TextUtils.isEmpty(badge3.imageUrl)
						&& badge3.imageUrl != null) {
					Picasso.with(getActivity()).cancelRequest(mImageBadge3);
					Picasso.with(getActivity()).load(badge3.imageUrl)
							.into(mImageBadge3);
				}
			}
		} else {
			mContainerBadges.setVisibility(View.GONE);
		}
	}

	String badgesetKeys[] = new String[] { "Hungama", "Downloader", "NightOwl",
			"TuneHunter" };
	String badges[][] = new String[][] {
			{ "Hungama Level 1", "Hungama Level 2", "Hungama Level 3",
					"Hungama Level 4", "Hungama Level 5", "Hungama Level 6",
					"Hungama Level 7", "Hungama Level 8", "Hungama Level 9",
					"Hungama Level 10" },
			{ "Downloader Level 1", "Downloader Level 2", "Downloader Level 3",
					"Downloader Level 4", "Downloader Level 5",
					"Downloader Level 6", "Downloader Level 7",
					"Downloader Level 8", "Downloader Level 9",
					"Downloader Level 10" },
			{ "NightOwl Level 1", "NightOwl Level 2", "NightOwl Level 3",
					"NightOwl Level 4", "NightOwl Level 5", "NightOwl Level 6",
					"NightOwl Level 7", "NightOwl Level 8", "NightOwl Level 9",
					"NightOwl Level 10" },
			{ "TuneHunter Level 1", "TuneHunter Level 2", "TuneHunter Level 3",
					"TuneHunter Level 4", "TuneHunter Level 5",
					"TuneHunter Level 6", "TuneHunter Level 7",
					"TuneHunter Level 8", "TuneHunter Level 9",
					"TuneHunter Level 10" }

	};

	/**
	 * set up badges Tags
	 */
	void adjustTags(UserBadges userBadges) {

		try {
			for (Badge bedge : userBadges.badges) {
				try {
					String name = bedge.name;
					process(name);
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
		}
	}

	void process(String name) {
		Logger.d(TAG, "Tag process >>" + name);
		if (name != null && name.length() > 0) {
			Set<String> tags = Utils.getTags();
			String tmp = "";
			for (int i = 0; i < badgesetKeys.length; i++) {
				if (name.startsWith(badgesetKeys[i])) {
					for (int j = 0; j < badges[i].length; j++) {
						tmp = badges[i][j].replaceAll(" ", "_");
						if (tags.contains(tmp)) {
							tags.remove(tmp);
							Logger.d(TAG, "Tag remove >>" + tmp);
						}
					}
					// break the loop;

					tags.add(name.replaceAll(" ", "_"));
					Logger.d(TAG, "Tag Added >>" + name.replaceAll(" ", "_"));

					i = badgesetKeys.length;
				}
			}
			Utils.AddTag(tags);

		}
	}

	/**
	 * set up Leader board screen screen
	 */
	private void populdateLeaderboardSection() {
		if (mUserProfile.userLeaderBoardUsers != null) {
			UserLeaderBoardUsers userLeaderBoardUsers = mUserProfile.userLeaderBoardUsers;

			mContainerLeaderboard.setVisibility(View.VISIBLE);
			mTextLeaderboardValue.setText(userLeaderBoardUsers.userRank);

			if (!Utils.isListEmpty(userLeaderBoardUsers.leaderBoardUsers)) {
				mContainerLeaderboardUser1.setVisibility(View.INVISIBLE);
				mContainerLeaderboardUser2.setVisibility(View.INVISIBLE);
				mContainerLeaderboardUser3.setVisibility(View.INVISIBLE);

				// constructs the views for each user as a set for iteration.
				Map<String, Object> leaderboardUser1 = new HashMap<String, Object>();
				leaderboardUser1.put(KEY_LEADERBOARD_CONTAINER,
						mContainerLeaderboardUser1);
				leaderboardUser1.put(KEY_LEADERBOARD_RANK,
						mTextLeaderboardUser1Rank);
				leaderboardUser1.put(KEY_LEADERBOARD_NAME,
						mTextLeaderboardUser1Name);
				leaderboardUser1.put(KEY_LEADERBOARD_TOTAL,
						mTextLeaderboardUser1TotalPoints);
				Map<String, Object> leaderboardUser2 = new HashMap<String, Object>();
				leaderboardUser2.put(KEY_LEADERBOARD_CONTAINER,
						mContainerLeaderboardUser2);
				leaderboardUser2.put(KEY_LEADERBOARD_RANK,
						mTextLeaderboardUser2Rank);
				leaderboardUser2.put(KEY_LEADERBOARD_NAME,
						mTextLeaderboardUser2Name);
				leaderboardUser2.put(KEY_LEADERBOARD_TOTAL,
						mTextLeaderboardUser2TotalPoints);
				Map<String, Object> leaderboardUser3 = new HashMap<String, Object>();
				leaderboardUser3.put(KEY_LEADERBOARD_CONTAINER,
						mContainerLeaderboardUser3);
				leaderboardUser3.put(KEY_LEADERBOARD_RANK,
						mTextLeaderboardUser3Rank);
				leaderboardUser3.put(KEY_LEADERBOARD_NAME,
						mTextLeaderboardUser3Name);
				leaderboardUser3.put(KEY_LEADERBOARD_TOTAL,
						mTextLeaderboardUser3TotalPoints);

				Stack<Map<String, Object>> leaderboardUserMaps = new Stack<Map<String, Object>>();
				leaderboardUserMaps.add(leaderboardUser3);
				leaderboardUserMaps.add(leaderboardUser2);
				leaderboardUserMaps.add(leaderboardUser1);

				Map<String, Object> leaderboardUserMap = null;

				for (LeaderBoardUser leaderboardUser : userLeaderBoardUsers.leaderBoardUsers) {
					if (leaderboardUserMaps.isEmpty())
						break;

					leaderboardUserMap = leaderboardUserMaps.pop();
					((RelativeLayout) leaderboardUserMap
							.get(KEY_LEADERBOARD_CONTAINER))
							.setVisibility(View.VISIBLE);
					((TextView) leaderboardUserMap.get(KEY_LEADERBOARD_RANK))
							.setText(Integer.toString(leaderboardUser.rank));
					((TextView) leaderboardUserMap.get(KEY_LEADERBOARD_NAME))
							.setText(leaderboardUser.name);
					((TextView) leaderboardUserMap.get(KEY_LEADERBOARD_TOTAL))
							.setText(Long.toString(leaderboardUser.totalPoint));

					if (leaderboardUser.id == Long.valueOf(mUserId)) {

						((TextView) leaderboardUserMap
								.get(KEY_LEADERBOARD_RANK))
								.setTextColor(getResources()
										.getColor(
												R.color.social_leaderboard_user_name_text_color));

						((TextView) leaderboardUserMap
								.get(KEY_LEADERBOARD_NAME))
								.setTextColor(getResources()
										.getColor(
												R.color.social_leaderboard_user_name_text_color));

						((TextView) leaderboardUserMap
								.get(KEY_LEADERBOARD_TOTAL))
								.setTextColor(getResources()
										.getColor(
												R.color.social_leaderboard_user_name_text_color));
					}
				}

			} else {
				// hides all the users rows.
				mContainerLeaderboardUser1.setVisibility(View.INVISIBLE);
				mContainerLeaderboardUser2.setVisibility(View.INVISIBLE);
				mContainerLeaderboardUser3.setVisibility(View.INVISIBLE);
			}

		} else {
			mContainerLeaderboard.setVisibility(View.GONE);
		}
	}

	/**
	 * set up Favorite screen
	 */
	private void populateFavoritesSections() {
		// populates the favorite albums.
		if (mUserProfile.userFavoriteAlbums != null
				&& !Utils.isListEmpty(mUserProfile.userFavoriteAlbums.albums)) {
			mContainerFavoriteAlbums.setVisibility(View.VISIBLE);

			UserFavoriteAlbums userFavoriteAlbums = mUserProfile.userFavoriteAlbums;
			mTextFavoriteFavoriteAlbumsValue.setText(Integer
					.toString(userFavoriteAlbums.albumCount));

			Stack<ImageView> favoriteAlbumsImages = new Stack<ImageView>();
			favoriteAlbumsImages.add(mTextFavoriteFavoriteAlbum3);
			favoriteAlbumsImages.add(mTextFavoriteFavoriteAlbum2);
			favoriteAlbumsImages.add(mTextFavoriteFavoriteAlbum1);

			ImageView albumImage = null;

			for (MediaItem mediaItem : userFavoriteAlbums.albums) {
				if (favoriteAlbumsImages.isEmpty())
					break;

				albumImage = favoriteAlbumsImages.pop();

				Picasso.with(getActivity()).cancelRequest(albumImage);
				if (!TextUtils.isEmpty(mediaItem.getImageUrl())
						&& getActivity() != null
						&& mediaItem.getImageUrl() != null) {
					Picasso.with(getActivity()).load(mediaItem.getImageUrl())
							.into(albumImage);
				}
			}

		} else {
			mContainerFavoriteAlbums.setVisibility(View.GONE);
		}

		// populates the favorite songs.
		if (mUserProfile.userFavoriteSongs != null
				&& !Utils.isListEmpty(mUserProfile.userFavoriteSongs.songs)) {
			mContainerFavoriteSongs.setVisibility(View.VISIBLE);

			UserFavoriteSongs userFavoriteSongs = mUserProfile.userFavoriteSongs;
			mTextFavoriteSongsValue.setText(Integer
					.toString(userFavoriteSongs.songsCount));

			Stack<TextView> favoriteSongsNames = new Stack<TextView>();
			favoriteSongsNames.add(mTextFavoriteSong3Name);
			favoriteSongsNames.add(mTextFavoriteSong2Name);
			favoriteSongsNames.add(mTextFavoriteSong1Name);

			TextView songName = null;

			for (MediaItem mediaItem : userFavoriteSongs.songs) {
				if (favoriteSongsNames.isEmpty())
					break;

				songName = favoriteSongsNames.pop();
				songName.setText(mediaItem.getTitle());
			}

		} else {
			mContainerFavoriteSongs.setVisibility(View.GONE);
		}

		// populates the favorite playlists.
		if (mUserProfile.userFavoritePlaylists != null
				&& !Utils
						.isListEmpty(mUserProfile.userFavoritePlaylists.playlists)) {
			mContainerFavoritePlaylists.setVisibility(View.VISIBLE);

			UserFavoritePlaylists userFavoritePlaylists = mUserProfile.userFavoritePlaylists;
			mTextFavoritePlaylistValue.setText(Integer
					.toString(userFavoritePlaylists.playlistCount));

			Stack<TextView> favoritePlaylistsNames = new Stack<TextView>();
			favoritePlaylistsNames.add(mTextFavoritePlaylist3Name);
			favoritePlaylistsNames.add(mTextFavoritePlaylist2Name);
			favoritePlaylistsNames.add(mTextFavoritePlaylist1Name);

			TextView playlistsName = null;

			for (MediaItem mediaItem : userFavoritePlaylists.playlists) {
				if (favoritePlaylistsNames.isEmpty())
					break;

				playlistsName = favoritePlaylistsNames.pop();
				playlistsName.setText(mediaItem.getTitle());
			}

		} else {
			mContainerFavoritePlaylists.setVisibility(View.GONE);
		}

		// populates the favorite videos.
		if (mUserProfile.userFavoriteVideos != null
				&& !Utils.isListEmpty(mUserProfile.userFavoriteVideos.videos)) {
			mContainerFavoriteVideos.setVisibility(View.VISIBLE);

			UserFavoriteVideos userFavoriteVideos = mUserProfile.userFavoriteVideos;
			mTextFavoriteVideosValue.setText(Integer
					.toString(userFavoriteVideos.videoCount));

			Stack<ImageView> favoriteVideosImages = new Stack<ImageView>();
			favoriteVideosImages.add(mTextFavoriteVideo3);
			favoriteVideosImages.add(mTextFavoriteVideo2);
			favoriteVideosImages.add(mTextFavoriteVideo1);

			ImageView videoImage = null;

			for (MediaItem mediaItem : userFavoriteVideos.videos) {
				if (favoriteVideosImages.isEmpty())
					break;

				videoImage = favoriteVideosImages.pop();

				Picasso.with(getActivity()).cancelRequest(videoImage);
				if (getActivity() != null
						&& !TextUtils.isEmpty(mediaItem.getImageUrl())) {
					Picasso.with(getActivity()).load(mediaItem.getImageUrl())
							.into(videoImage);
				}
			}

		} else {
			mContainerFavoriteVideos.setVisibility(View.GONE);
		}

		// populates the favorite artists.
		if (mUserProfile.userFavoriteOnDemand != null
				&& !Utils
						.isListEmpty(mUserProfile.userFavoriteOnDemand.artists)) {
			mContainerFavoriteArtists.setVisibility(View.VISIBLE);

			UserFavoriteOnDemand userFavoriteArtists = mUserProfile.userFavoriteOnDemand;
			mTextFavoriteArtistsValue.setText(Integer
					.toString(userFavoriteArtists.artistsCount));

			Stack<TextView> favoriteArtistsNames = new Stack<TextView>();
			favoriteArtistsNames.add(mTextFavoriteArtist3Name);
			favoriteArtistsNames.add(mTextFavoriteArtist2Name);
			favoriteArtistsNames.add(mTextFavoriteArtist1Name);

			TextView artistName = null;

			for (MediaItem mediaItem : userFavoriteArtists.artists) {
				if (favoriteArtistsNames.isEmpty())
					break;

				artistName = favoriteArtistsNames.pop();
				artistName.setText(mediaItem.getTitle());
			}

		} else {
			mContainerFavoriteArtists.setVisibility(View.GONE);
		}
	}

	/**
	 * set up Playlist screen
	 */
	private void populateUserPlaylitstsSection() {
		// gets the playlists from the DB.
		Map<Long, Playlist> playlistsMap = mDataManager.getStoredPlaylists();
		List<Playlist> playlists = new ArrayList<Playlist>();

		// Convert from Map<Long, Playlist> to List<Itemable>
		if (playlistsMap != null && playlistsMap.size() > 0) {
			for (Map.Entry<Long, Playlist> p : playlistsMap.entrySet()) {
				playlists.add(p.getValue());
			}
		}

		// populates the favorite playlists.
		if (!Utils.isListEmpty(playlists)) {
			Collections.reverse(playlists);
			mContainerMyPlaylists.setVisibility(View.VISIBLE);
			// shows any internal component except the empty text.
			mTextMyPlaylist1Name.setVisibility(View.VISIBLE);
			mTextMyPlaylist2Name.setVisibility(View.VISIBLE);
			mTextMyPlaylist3Name.setVisibility(View.VISIBLE);
			mImageMoreIndicator.setVisibility(View.VISIBLE);
			mTextMyPlaylistEmpty.setVisibility(View.GONE);

			mTextMyPlaylistsValue.setText(Integer.toString(playlists.size()));

			Stack<TextView> playlistsNames = new Stack<TextView>();
			playlistsNames.add(mTextMyPlaylist3Name);
			playlistsNames.add(mTextMyPlaylist2Name);
			playlistsNames.add(mTextMyPlaylist1Name);

			TextView songName = null;

			for (Playlist playlist : playlists) {
				if (playlistsNames.isEmpty())
					break;

				songName = playlistsNames.pop();
				songName.setText(playlist.getName());
			}

		} else {
			mContainerMyPlaylists.setVisibility(View.GONE);
		}
	}

	/**
	 * set up level of user
	 */
	private void populdateLevelBarNew() {
		try {
			mTextLevelZero.setVisibility(View.GONE);

			int startInterval;
			int currentLevel = (int) mUserProfile.currentLevel;
			int maxLevel = (int) mUserProfile.maxLevel;

			Logger.i(TAG, "Current Level: " + Integer.toString(currentLevel)
					+ " Max level: " + Integer.toString(maxLevel));

			boolean IsEven = maxLevel % 2 == 0;

			// If the maxLevel is even then startInterval = 0 else startInterval
			// = 1
			if (IsEven) {
				startInterval = 0;
			} else {
				startInterval = 1;
			}

			TextView levelText = null;
			LinearLayout.LayoutParams levelParams = null;

			Context context = getActivity();
			int textColor = getResources().getColor(R.color.white);
			float textSize = (float) getResources().getDimensionPixelSize(
					R.dimen.small_text_size);

			mContainerLevels.removeAllViews();

			boolean isVisible = true;

			for (int i = startInterval; i <= maxLevel; i++) {

				levelText = new TextView(context);
				levelText.setTextColor(textColor);
				levelText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
				levelText.setGravity(Gravity.CENTER);

				levelParams = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.MATCH_PARENT);
				levelParams.weight = 1;

				if (isVisible) {
					levelText.setVisibility(View.VISIBLE);
					levelText.setText(String.valueOf(i));
					isVisible = false;
				} else {
					levelText.setVisibility(View.INVISIBLE);
					isVisible = true;
				}

				mContainerLevels.addView(levelText, levelParams);
			}

			// Set the progress 100% by the screen width
			int levelBarWidth = mContainerLevelBar.getWidth();
			mProgressLevelBar.setMax(levelBarWidth);

			// Get how many levels we have (including the invisible ones)
			int numOfLevelTexts = mContainerLevels.getChildCount();

			// Get the size of each of them
			int levelTextSize = levelBarWidth / numOfLevelTexts;

			// If its even levels then need to color another leveText
			int numOfLevelTextColoring = currentLevel;

			if (IsEven) {
				numOfLevelTextColoring++;
			}

			// coloring the till the current level
			mProgressLevelBar.setProgress(levelTextSize
					* numOfLevelTextColoring);
		} catch (Exception e) {
			Logger.printStackTrace(e);
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
				Utils.destroyFragment();
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}
	}
}
