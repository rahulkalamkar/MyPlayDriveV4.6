package com.hungama.myplay.activity.ui.fragments.social;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.social.Badge;
import com.hungama.myplay.activity.data.dao.hungama.social.ProfileBadges;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.SocialProfileBadgesOperation;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.ProfileActivity;
import com.hungama.myplay.activity.ui.widgets.LanguageButton;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;
import com.hungama.myplay.activity.util.images.ImageCache;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Shows grid of Badges for a given user. For creating the fragment an argument
 * {@code FRAGMENT_ARGUMENT_USER_ID} must be pased or
 * {@code IllegalArgumentException} will be thrown.
 */
public class BadgesFragment extends Fragment implements
		CommunicationOperationListener {

	private static final String TAG = "BadgesFragment";

	public static final String FRAGMENT_ARGUMENT_USER_ID = "fragment_argument_user_id";

	private Context mApplicationContext;
	private DataManager mDataManager;

	private String mUserId = null;
	private ProfileBadges mProfileBadges = null;
	private List<Badge> mBadges = new ArrayList<Badge>();

	private LinearLayout mCurrentBadgeBar;
	private ImageView mImageSelectedBdge;
	private TextView mTextSelectedBadge;

	private GridView mGridViewBadges;
	private BadgesGridAdapter mBadgesGridAdapter;

    ProfileActivity profileActivity;
    public void setProfileActivity(ProfileActivity profileActivity) {
        this.profileActivity = profileActivity;
    }

	public ProfileActivity getProfileActivity() {
		return profileActivity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// gets the user id, to get his badges.
		Bundle arguments = getArguments();
		if (arguments != null
				&& arguments.containsKey(FRAGMENT_ARGUMENT_USER_ID)) {
			mUserId = arguments.getString(FRAGMENT_ARGUMENT_USER_ID);

			if (TextUtils.isEmpty(mUserId))
				throw new IllegalArgumentException(
						"Must contain and argument: FRAGMENT_ARGUMENT_USER_ID.");

		} else {
			throw new IllegalArgumentException(
					"Must contain and argument: FRAGMENT_ARGUMENT_USER_ID.");
		}

		mApplicationContext = getActivity().getApplicationContext();
		mDataManager = DataManager.getInstance(mApplicationContext);
		Analytics.postCrashlitycsLog(getActivity(),BadgesFragment.class.getName());
	}

    public void setTitle(){
        if(profileActivity!=null)
            profileActivity.setTitle(true);
    }

	View rootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_social_badges,
					container, false);

			intializeUserControls(rootView);
		} else {
			Logger.e("HomeMediaTileGridFragment", "onCreateView else");
			ViewGroup parent = (ViewGroup) Utils.getParentViewCustom(rootView);
			parent.removeView(rootView);
		}

		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();

		if (mProfileBadges == null) {
			// gets the profile badges for the given user.
			mDataManager.getProfileBadges(mUserId, this);
		}
		Analytics.startSession(getActivity(), this);
		Analytics.onPageView();

		if (mDataManager.getApplicationConfigurations().getPartnerUserId()
				.equalsIgnoreCase(mUserId)) {
			Analytics.logEvent("My Badges");
		} else {
			Analytics.logEvent("Others Badges");
		}
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

		mDataManager.cancelGetProfileBadges();

		super.onStop();

		Analytics.onEndSession(getActivity());
	}

	/**
	 * Initialize view and set listener
	 * 
	 * @param rootView
	 */
	private void intializeUserControls(View rootView) {

		// initializes the components.
		mCurrentBadgeBar = (LinearLayout) rootView
				.findViewById(R.id.badges_current_badge_bar);
		mImageSelectedBdge = (ImageView) rootView
				.findViewById(R.id.badges_current_badge_icon);
		mTextSelectedBadge = (TextView) rootView
				.findViewById(R.id.badges_current_badge_name);
		mGridViewBadges = (GridView) rootView
				.findViewById(R.id.badges_gridview);
		mBadgesGridAdapter = new BadgesGridAdapter();

		// initializes the image loader.
		mImageSelectedBdge.measure(0, 0);

		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(
				getActivity(), DataManager.FOLDER_THUMBNAILS_CACHE);
		cacheParams.setMemCacheSizePercent(getActivity(), 0.10f);
		cacheParams.compressFormat = CompressFormat.PNG;

		// binds the data adapter with the list.
		mGridViewBadges.setAdapter(mBadgesGridAdapter);

		// sets the title.
		String title;
		if (mDataManager.getApplicationConfigurations().getPartnerUserId()
				.equalsIgnoreCase(mUserId)) {
			title = getResources().getString(R.string.badges_title);
		} else {
			title = getResources().getString(R.string.badges_title_1);
		}
        profileActivity.setTitleBarText(title);
	}

	/**
	 * set up adapter and set current badge as title
	 */
	private void populateUserControlls() {
		if (mProfileBadges.currentBadge != null) {
			mTextSelectedBadge.setText(mProfileBadges.currentBadge.get(0).name);
			Picasso.with(mApplicationContext).cancelRequest(mImageSelectedBdge);
			if (mApplicationContext != null
					&& mProfileBadges.currentBadge.get(0).imageUrl != null
					&& !TextUtils
							.isEmpty(mProfileBadges.currentBadge.get(0).imageUrl)) {
				Picasso.with(mApplicationContext)
						.load(mProfileBadges.currentBadge.get(0).imageUrl)
						.into(mImageSelectedBdge);
			}

		} else {
			mTextSelectedBadge.setVisibility(View.INVISIBLE);
			mImageSelectedBdge.setVisibility(View.INVISIBLE);
		}

		mCurrentBadgeBar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showDialogForBadge(mProfileBadges.currentBadge.get(0));
			}
		});

		mGridViewBadges.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Badge badge = mProfileBadges.badges.get(position);
				showDialogForBadge(badge);
			}
		});

		mBadgesGridAdapter.notifyDataSetChanged();
	}

	/**
	 * show dialog for selected badge
	 * 
	 * @param badge
	 */
	private void showDialogForBadge(Badge badge) {
		// creates a dialog that shows the badge's information.
		final Dialog dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(Color.TRANSPARENT));
		dialog.setContentView(R.layout.dialog_badge_info);
		dialog.setCancelable(true);

		TextView title = (TextView) dialog
				.findViewById(R.id.badges_info_dialog_title_text);
		LanguageButton closeButton = (LanguageButton) dialog
				.findViewById(R.id.badges_info_dialog_title_close_button);
		ImageView badgeIcon = (ImageView) dialog
				.findViewById(R.id.badges_info_dialog_badge_icon);
		TextView badgeDescription = (TextView) dialog
				.findViewById(R.id.badges_info_dialog_badge_description);

		title.setText(badge.name);
		badgeDescription.setText(badge.description);
		// mImageFetcher.loadImage(badge.imageUrl, badgeIcon);
		Picasso.with(mApplicationContext).cancelRequest(badgeIcon);
		if (mApplicationContext != null && badge.imageUrl != null
				&& !TextUtils.isEmpty(badge.imageUrl)) {
			Picasso.with(mApplicationContext).load(badge.imageUrl)
					.into(badgeIcon);
		}

		closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				dialog.dismiss();
			}
		});

		DisplayMetrics displaymetrics = new DisplayMetrics();
		dialog.getWindow().getWindowManager().getDefaultDisplay()
				.getMetrics(displaymetrics);
		int width = (int) (displaymetrics.widthPixels);

		WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
		params.width = width;
		dialog.getWindow().setAttributes(params);

		dialog.show();
	}

	// ======================================================
	// Communication callbacks.
	// ======================================================

	@Override
	public void onStart(int operationId) {
		try {
			((MainActivity) getActivity()).showLoadingDialog("");
		} catch (Exception e) {
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			((MainActivity) getActivity()).hideLoadingDialogNew();
		} catch (Exception e) {
		}
		try {
			if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_BADGES) {
				mProfileBadges = (ProfileBadges) responseObjects
						.get(SocialProfileBadgesOperation.RESULT_KEY_PROFILE_BADGES);
				mBadges = mProfileBadges.badges;
				// xtpl

				if (mProfileBadges.currentBadge != null) {
					try {
						for (Badge badge : mProfileBadges.currentBadge) {
							String name = badge.name;
							if (name != null && name.length() > 0) {
								Set<String> tags = Utils.getTags();
								String tmp = "";
								for (int i = 0; i < badgesetKeys.length; i++) {
									if (name.startsWith(badgesetKeys[i])) {
										for (int j = 0; j < badges[i].length; j++) {
											tmp = badges[i][j].replaceAll(" ",
													"_");
											if (tags.contains(tmp)) {
												tags.remove(tmp);
												Logger.d(TAG, "Tag remove >>"
														+ tmp);
											}
										}
										tags.add(name.replaceAll(" ", "_"));
										Logger.d(
												TAG,
												"Tag Added >>"
														+ name.replaceAll(" ",
																"_"));

										i = badgesetKeys.length;
									}
								}
								Utils.AddTag(tags);
							}
						}
					} catch (Exception e) {
					}
				}

				if (mProfileBadges.badges != null) {
					try {
						for (Badge badge : mProfileBadges.badges) {
							String name = badge.name;
							if (name != null && name.length() > 0) {
								if (badge.imageUrl != null
										&& !TextUtils.isEmpty(badge.imageUrl)) {

									Set<String> tags = Utils.getTags();
									String tmp = "";
									for (int i = 0; i < badgesetKeys.length; i++) {
										if (name.startsWith(badgesetKeys[i])) {
											for (int j = 0; j < badges[i].length; j++) {
												tmp = badges[i][j].replaceAll(
														" ", "_");
												if (tags.contains(tmp)) {
													tags.remove(tmp);
													Logger.d(TAG,
															"Tag remove >>"
																	+ tmp);
												}
											}
											// break the loop;
											tags.add(name.replaceAll(" ", "_"));
											Logger.d(
													TAG,
													"Tag Added >>"
															+ name.replaceAll(
																	" ", "_"));

											i = badgesetKeys.length;
										}
									}
									Utils.AddTag(tags);
								}
							}
						}
					} catch (Exception e) {
					}
				}
				// xtpl
				populateUserControlls();
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
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

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		try {
			if (errorType != ErrorType.OPERATION_CANCELLED
					&& getActivity() != null)
				Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT)
						.show();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	// ======================================================
	// Badges Grid Adapter.
	// ======================================================

	private static class ViewHolder {
		ImageView icon;
		LanguageTextView name;
	}

	private class BadgesGridAdapter extends BaseAdapter {

		private LayoutInflater layoutInflater;

		public BadgesGridAdapter() {
			layoutInflater = (LayoutInflater) getActivity()
					.getApplicationContext().getSystemService(
							Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mBadges.size();
		}

		@Override
		public Object getItem(int position) {
			return mBadges.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;

			if (convertView == null) {
				convertView = layoutInflater.inflate(
						R.layout.list_item_badges_single_badge, parent, false);
				viewHolder = new ViewHolder();

				viewHolder.icon = (ImageView) convertView
						.findViewById(R.id.badges_list_item_badge_icon);
				viewHolder.name = (LanguageTextView) convertView
						.findViewById(R.id.badges_list_item_badge_name);

				convertView.setTag(viewHolder);

			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			Badge badge = mBadges.get(position);

			viewHolder.name.setText(badge.name);
			Picasso.with(mApplicationContext).cancelRequest(viewHolder.icon);
			if (mApplicationContext != null && badge.imageUrl != null
					&& !TextUtils.isEmpty(badge.imageUrl)) {
				Picasso.with(mApplicationContext).load(badge.imageUrl)
						.into(viewHolder.icon);
			}
			return convertView;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mDataManager = null;
		mProfileBadges = null;
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
