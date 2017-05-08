package com.hungama.myplay.activity.ui.fragments.social;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.social.LeaderBoardUser;
import com.hungama.myplay.activity.data.dao.hungama.social.ProfileLeaderboard;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.SocialProfileLeaderboardOperation;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.ProfileActivity;
import com.hungama.myplay.activity.ui.widgets.LanguageButton;
import com.hungama.myplay.activity.ui.widgets.TwoStatesButton;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;
import com.hungama.myplay.activity.util.images.ImageCache.ImageCacheParams;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardFragment extends Fragment implements OnClickListener,
		CommunicationOperationListener {

	public static final String FRAGMENT_ARGUMENT_USER_ID = "fragment_argument_user_id";

	private OnLeaderboardUserSelectedListener mOnLeaderboardUserSelectedListener;

	private Context mContext;
	private DataManager mDataManager;
	private String mUserId;

	private TwoStatesButton mButtonFriendsTab;
	private TwoStatesButton mButtonTopTab;
	private LanguageButton mButtonTopSeven;
	private LanguageButton mButtonTopAllTime;
	private LinearLayout mContainerTopSelection;

	private String mTextTopSeven;
	private String mTextTopAllTime;

	private List<LeaderBoardUser> mLeaderBoardUsers = new ArrayList<LeaderBoardUser>();
	private ListView mListViewUsers;
	private UserAdapter mUserAdapter;



	// ======================================================
	// public methods.
	// ======================================================

	/**
	 * Interface definition to be invoked when a user from the leaderboard has
	 * been selected.
	 */
	public interface OnLeaderboardUserSelectedListener {

		public void onLeaderboardUserSelectedListener(String selectedUserId);
	}

	public void setOnLeaderboardUserSelectedListener(
			OnLeaderboardUserSelectedListener listener) {
		mOnLeaderboardUserSelectedListener = listener;
	}

    ProfileActivity profileActivity;
    public void setProfileActivity(ProfileActivity profileActivity) {
        this.profileActivity = profileActivity;
    }

	public ProfileActivity getProfileActivity() {
		return profileActivity;
	}

	public void setTitle(){
        if(profileActivity!=null)
            profileActivity.setTitle(true);
    }

    // ======================================================
	// Life cycle.
	// ======================================================

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = getActivity().getApplicationContext();
		mDataManager = DataManager.getInstance(mContext);

		mTextTopSeven = Utils.getMultilanguageTextLayOut(
				mContext,
				getResources().getString(
						R.string.social_leaderboard_last_seven_days_top));
		mTextTopAllTime = Utils.getMultilanguageTextLayOut(
				mContext,
				getResources().getString(
						R.string.social_leaderboard_all_time_top));

		Bundle arguments = getArguments();
		if (arguments != null
				&& arguments.containsKey(FRAGMENT_ARGUMENT_USER_ID)) {
			mUserId = arguments.getString(FRAGMENT_ARGUMENT_USER_ID);

			if (TextUtils.isEmpty(mUserId)) {
				ApplicationConfigurations applicationConfigurations = mDataManager
						.getApplicationConfigurations();
				mUserId = applicationConfigurations.getPartnerUserId();
			}
		}
		Analytics.postCrashlitycsLog(getActivity(),LeaderboardFragment.class.getName());
	}

	View rootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_social_leaderboard,
					container, false);
			ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
					.getInstance(getActivity());
			if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
				Utils.traverseChild(rootView, getActivity());
			}
			initializeUserControls(rootView);

			setFriendsTabClicked();
		} else {
			ViewGroup parent = (ViewGroup) Utils.getParentViewCustom(rootView);
			parent.removeView(rootView);
		}
		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();

		// sets the title.
		String text = Utils.getMultilanguageText(mContext, getResources()
				.getString(R.string.social_leaderboard_title));
        profileActivity.setTitleBarText(text);

		Analytics.startSession(getActivity(), this);
		Analytics.onPageView();
		Map<String, String> reportMap = new HashMap<String, String>();
		reportMap.put(FlurryConstants.FlurryKeys.Source.toString(),
				"My Profile");
		Analytics.logEvent(FlurryConstants.FlurryKeys.Leaderboard.toString(),
				reportMap);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		mDataManager.cancelGetProfileLeaderboard();
		super.onStop();
		Analytics.onEndSession(getActivity());
	}

	@Override
	public void onClick(View view) {
		int viewId = view.getId();

		if (viewId == R.id.social_leaderboard_tab1) {
			setFriendsTabClicked();

		} else if (viewId == R.id.social_leaderboard_tab2) {
			setTopTabClicked();

		} else if (viewId == R.id.social_leaderboard_tab2_period_seven) {
			setTopSevenClicked();

		} else if (viewId == R.id.social_leaderboard_tab2_period_all) {
			setTopAllClicked();
		}

	}

	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_LEADERBOARD) {
			((MainActivity) getActivity())
					.showLoadingDialog(Utils
							.getMultilanguageText(
									mContext,
									getString(R.string.application_dialog_loading_content)));
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_LEADERBOARD) {
				if (responseObjects
						.containsKey(SocialProfileLeaderboardOperation.RESULT_KEY_PROFILE_LEADERBOARD)) {
					ProfileLeaderboard profileLeaderboard = (ProfileLeaderboard) responseObjects
							.get(SocialProfileLeaderboardOperation.RESULT_KEY_PROFILE_LEADERBOARD);

					// updates the list.
					mLeaderBoardUsers = new ArrayList<LeaderBoardUser>();
					mLeaderBoardUsers.clear();
					mLeaderBoardUsers = profileLeaderboard.leaderBoardUsers;
					mUserAdapter.notifyDataSetChanged();

					// if there are no users, shows the relate message.
					if (Utils.isListEmpty(mLeaderBoardUsers)) {
						ProfileLeaderboard.TYPE type = (ProfileLeaderboard.TYPE) responseObjects
								.get(SocialProfileLeaderboardOperation.RESULT_KEY_PROFILE_LEADERBOARD_TYPE);

						if (type == ProfileLeaderboard.TYPE.FRIENDS) {
							Utils.makeText(
									getActivity(),
									getString(R.string.social_leaderboard_error_message_no_friends),
									Toast.LENGTH_SHORT).show();

						} else {
							ProfileLeaderboard.PERIOD period = (ProfileLeaderboard.PERIOD) responseObjects
									.get(SocialProfileLeaderboardOperation.RESULT_KEY_PROFILE_LEADERBOARD_PERIOD);

							if (period == ProfileLeaderboard.PERIOD.ALL) {
								Utils.makeText(
										getActivity(),
										getString(R.string.social_leaderboard_error_message_no_all_time_users),
										Toast.LENGTH_SHORT).show();

							} else if (period == ProfileLeaderboard.PERIOD.SEVEN) {
								Utils.makeText(
										getActivity(),
										getString(R.string.social_leaderboard_error_message_no_last_seven_days_users),
										Toast.LENGTH_SHORT).show();

							}
						}
					}
				}
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

		if ((MainActivity) getActivity() != null)
			((MainActivity) getActivity()).hideLoadingDialogNew();
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		try {
			if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_LEADERBOARD) {
				((MainActivity) getActivity()).hideLoadingDialogNew();
			}
			if (errorType != ErrorType.OPERATION_CANCELLED)
				Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT)
						.show();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	private void initializeUserControls(View rootView) {

		mButtonFriendsTab = (TwoStatesButton) rootView
				.findViewById(R.id.social_leaderboard_tab1);
		mButtonTopTab = (TwoStatesButton) rootView
				.findViewById(R.id.social_leaderboard_tab2);
		mButtonTopSeven = (LanguageButton) rootView
				.findViewById(R.id.social_leaderboard_tab2_period_seven);
		mButtonTopAllTime = (LanguageButton) rootView
				.findViewById(R.id.social_leaderboard_tab2_period_all);
		mContainerTopSelection = (LinearLayout) rootView
				.findViewById(R.id.social_leaderboard_tab2_period);
		mListViewUsers = (ListView) rootView
				.findViewById(R.id.social_leaderboard_users);

		mButtonFriendsTab.setOnClickListener(this);
		mButtonTopTab.setOnClickListener(this);
		mButtonTopSeven.setOnClickListener(this);
		mButtonTopAllTime.setOnClickListener(this);

		mListViewUsers.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mOnLeaderboardUserSelectedListener != null)
					mOnLeaderboardUserSelectedListener
							.onLeaderboardUserSelectedListener(Long
									.toString(id));
			}
		});

		ImageCacheParams cacheParams = new ImageCacheParams(getActivity(),
				DataManager.FOLDER_THUMBNAILS_FRIENDS);
		// Set memory cache to 10% of mem class
		cacheParams.setMemCacheSizePercent(getActivity(), 0.10f);

		mUserAdapter = new UserAdapter();
		mListViewUsers.setAdapter(mUserAdapter);
	}

	private void setFriendsTabClicked() {
		// sets the FRIENDS tab as selected.
		mButtonFriendsTab.setSelected();

		// sets the top tab as unselected.
		mButtonTopTab.setUnselected();

		// if the inner tabs of the TOP tab are visible, hides them.
		if (isTopTabContentVisible()) {
			closeTopTabContent();
		}

		// queries for the list of the friends leaderboard.
		mDataManager.cancelGetProfileLeaderboard();
		mDataManager.getProfileLeaderboard(mUserId,
				ProfileLeaderboard.TYPE.FRIENDS, ProfileLeaderboard.PERIOD.ALL,
				this);
	}

	private void setTopTabClicked() {
		// sets the TOP button selected anyway.
		mButtonTopTab.setSelected();

		// sets the FRIENDS tab as unselected.
		mButtonFriendsTab.setUnselected();

		// toggles the visibility of the tab's inner tabs and it's content
		// indicator.
		if (isTopTabContentVisible()) {
			closeTopTabContent();
		} else {
			openTopTabContent();
		}
	}

	private void setTopSevenClicked() {
		// sets the TOP button selected anyway.
		mButtonTopTab.setSelected();
		// sets it's title.
		mButtonTopTab.setText(mTextTopSeven);

		// if the inner tabs of the TOP tab are visible, hides them.
		if (isTopTabContentVisible()) {
			closeTopTabContent();
		}

		// queries for the list of the top in 7 days leaderboard.
		mDataManager.cancelGetProfileLeaderboard();
		mDataManager.getProfileLeaderboard(mUserId,
				ProfileLeaderboard.TYPE.EVERYONE,
				ProfileLeaderboard.PERIOD.SEVEN, this);
	}

	private void setTopAllClicked() {
		// sets the TOP button selected anyway.
		mButtonTopTab.setSelected();
		// sets it's title.
		mButtonTopTab.setText(mTextTopAllTime);

		// if the inner tabs of the TOP tab are visible, hides them.
		if (isTopTabContentVisible()) {
			closeTopTabContent();
		}

		// queries for the list of the top all leaderboard.
		mDataManager.cancelGetProfileLeaderboard();
		mDataManager.getProfileLeaderboard(mUserId,
				ProfileLeaderboard.TYPE.EVERYONE,
				ProfileLeaderboard.PERIOD.ALL, this);
	}

	private boolean isTopTabContentVisible() {
		return mContainerTopSelection.getVisibility() == View.VISIBLE;
	}

	private void closeTopTabContent() {
		// hides the content.
		mContainerTopSelection.setVisibility(View.GONE);
		/*
		 * To sets the right icon of the tab's button, we gets the size of the
		 * exist one and sets the new icon by it.
		 */
		Drawable[] drawables = mButtonTopTab.getCompoundDrawables();
		Drawable icon = drawables[2];
		if (icon != null) {
			Rect iconBounds = icon.getBounds();
			Drawable expandIcon = getResources().getDrawable(
					R.drawable.icon_white_content_collapse_down);
			expandIcon.setBounds(iconBounds);
			// sets the tab's expand icon.
			mButtonTopTab.setCompoundDrawables(null, null, expandIcon, null);
		}
	}

	private void openTopTabContent() {
		// shows the content.
		mContainerTopSelection.setVisibility(View.VISIBLE);
		/*
		 * To sets the right icon of the tab's button, we gets the size of the
		 * exist one and sets the new icon by it.
		 */
		Drawable[] drawables = mButtonTopTab.getCompoundDrawables();
		Drawable icon = drawables[2];
		if (icon != null) {
			Rect iconBounds = icon.getBounds();
			Drawable collapseIcon = getResources().getDrawable(
					R.drawable.icon_white_content_collapse_up);
			collapseIcon.setBounds(iconBounds);
			// sets the tab's collapse icon.
			mButtonTopTab.setCompoundDrawables(null, null, collapseIcon, null);
		}
	}

	// ======================================================
	// User Adapter.
	// ======================================================

	private static class ViewHolder {
		TextView rank;
		ImageView thumbNail;
		TextView userName;
		TextView totalScore;
	}

	private class UserAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public UserAdapter() {
			mInflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			if (mLeaderBoardUsers != null)
				return mLeaderBoardUsers.size();
			else
				return 0;
		}

		@Override
		public Object getItem(int position) {
			return mLeaderBoardUsers.get(position);
		}

		@Override
		public long getItemId(int position) {
			try {
				return ((LeaderBoardUser) mLeaderBoardUsers.get(position)).id;	
			} catch (Exception e) {
				return position;
			}
			
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;

			if (convertView == null) {
				convertView = mInflater.inflate(
						R.layout.list_item_social_leaderboard_user, parent,
						false);
				viewHolder = new ViewHolder();

				viewHolder.rank = (TextView) convertView
						.findViewById(R.id.leaderboard_user_item_rank);
				viewHolder.thumbNail = (ImageView) convertView
						.findViewById(R.id.leaderboard_user_item_thumbnail);
				viewHolder.userName = (TextView) convertView
						.findViewById(R.id.leaderboard_user_item_name);
				viewHolder.totalScore = (TextView) convertView
						.findViewById(R.id.leaderboard_user_item_total_score);

				convertView.setTag(R.id.view_tag_object, viewHolder);

			} else {
				viewHolder = (ViewHolder) convertView
						.getTag(R.id.view_tag_object);
			}

			if (mLeaderBoardUsers != null && mLeaderBoardUsers.size() > 0) {
				LeaderBoardUser leaderBoardUser = mLeaderBoardUsers
						.get(position);

				viewHolder.rank.setText(Integer.toString(leaderBoardUser.rank));
				viewHolder.userName.setText(leaderBoardUser.name);
				viewHolder.totalScore.setText(Long
						.toString(leaderBoardUser.totalPoint));

				Picasso.with(mContext).cancelRequest(viewHolder.thumbNail);
				Picasso.with(mContext)
						.load(R.drawable.background_home_tile_album_default)
						.into(viewHolder.thumbNail);
				if (mContext != null && leaderBoardUser.profileImageUrl != null
						&& !TextUtils.isEmpty(leaderBoardUser.profileImageUrl)) {
					Picasso.with(mContext)
							.load(leaderBoardUser.profileImageUrl)
							.placeholder(
									R.drawable.background_home_tile_album_default)
							.into(viewHolder.thumbNail);
				}
			}

			return convertView;
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
}
