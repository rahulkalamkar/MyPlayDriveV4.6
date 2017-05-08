package com.hungama.myplay.activity.ui.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.DataBase.CacheState;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.social.StreamItem;
import com.hungama.myplay.activity.data.dao.hungama.social.StreamItemCategory;
import com.hungama.myplay.activity.gigya.InviteFriendsActivity;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.SocialMyStreamOperation;
import com.hungama.myplay.activity.ui.DownloadConnectingActivity;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.MyStreamActivity;
import com.hungama.myplay.activity.ui.ProfileActivity;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.ui.widgets.CustomCacheStateProgressBar;
import com.hungama.myplay.activity.ui.widgets.LanguageButton;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.ui.widgets.StreamMediaItemView;
import com.hungama.myplay.activity.ui.widgets.StreamMediaItemView.SocialMyStreamMediaItemListener;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class SocialMyStreamFragment extends Fragment implements
		OnClickListener, CommunicationOperationListener {

	private static final String TAG = "SocialMyStreamFragment";

	private Context mContext;
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	private OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener;

	public void setOnMediaItemOptionSelectedListener(
			OnMediaItemOptionSelectedListener listener) {
		mOnMediaItemOptionSelectedListener = listener;
	}

	private LinearLayout mContainerNoContent;
	private LinearLayout mContainerConnectionError;
	private ListView mListContent;

	private LinearLayout mTitleBar;
	private LanguageButton mButtonOpen;
	private LanguageButton mButtonClose;
	private LanguageButton mButtonEveryone;
	private LanguageButton mButtonFriends;
	private LanguageButton mButtonMe;
	private LanguageButton mButtonInvite;
	private LanguageButton mButtonContentInvite;

	private StreamItemsAdapter mStreamItemsAdapter;
	private List<StreamItem> mStreamItems = new ArrayList<StreamItem>();

	private String mFlurrySubSectionDescription;
	private View rootView = null;

    public MyStreamActivity myStreamActivity;

    public void setMyStreamActivity(MyStreamActivity myStreamActivity) {
        this.myStreamActivity = myStreamActivity;
    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = getActivity().getApplicationContext();
		mDataManager = DataManager.getInstance(mContext);
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		Analytics.postCrashlitycsLog(getActivity(), SocialMyStreamFragment.class.getName());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (rootView == null) {
			try {
				rootView = inflater.inflate(R.layout.fragment_social_mystream,
						container, false);
			} catch (Error e) {
				System.gc();
				System.runFinalization();
				System.gc();
				rootView = inflater.inflate(R.layout.fragment_social_mystream,
						container, false);
			}
			if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
				Utils.traverseChild(rootView, getActivity());
			}

			initializeUserControls(rootView);
			((MainActivity) getActivity())
					.showLoadingDialog(R.string.application_dialog_loading_content);
			onEveryoneButtonClicked();
		} else {
			ViewGroup parent = (ViewGroup) Utils.getParentViewCustom(rootView);
			parent.removeView(rootView);
		}
		return rootView;
	}

	@Override
	public void onClick(View view) {
		int viewId = view.getId();

		if (viewId == R.id.social_mystream_title_bar_button_close) {
			closeTitleBar();

		} else if (viewId == R.id.social_mystream_title_bar_button_open) {
			openTitleBar();

		} else if (viewId == R.id.social_mystream_title_bar_button_everyone) {
			((MainActivity) getActivity())
					.showLoadingDialog(R.string.application_dialog_loading_content);
			onEveryoneButtonClicked();
			closeTitleBar();
		} else if (viewId == R.id.social_mystream_title_bar_button_friends) {
			((MainActivity) getActivity())
					.showLoadingDialog(R.string.application_dialog_loading_content);
			onFriendsButtonClicked();
			closeTitleBar();
		} else if (viewId == R.id.social_mystream_title_bar_button_me) {
			((MainActivity) getActivity())
					.showLoadingDialog(R.string.application_dialog_loading_content);
			onMeButtonClicked();
			closeTitleBar();
		} else if (viewId == R.id.social_mystream_title_bar_button_invite
				|| viewId == R.id.social_mystream_container_no_content_button_invite) {

			String flurrySection = "No Source";
			if (viewId == R.id.social_mystream_title_bar_button_invite) {
				//
				flurrySection = FlurryConstants.FlurryInvite.MyStreamInviteFriends
						.toString();
			} else if (viewId == R.id.social_mystream_container_no_content_button_invite) {
				// Invite button
				flurrySection = FlurryConstants.FlurryInvite.MyStreamFriendsWhenEmpty
						.toString();
			}
			if (viewId == R.id.social_mystream_title_bar_button_invite) {
				closeTitleBar();
			}

			// Invite Friends Section
			Intent intent = new Intent(getActivity(),
					InviteFriendsActivity.class);
			intent.putExtra(InviteFriendsActivity.FLURRY_SOURCE, flurrySection);
			startActivity(intent);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Analytics.startSession(getActivity(), this);
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
		mDataManager.cancelGetMyStreamItems();

		super.onStop();

		Analytics.onEndSession(getActivity());
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

	// ======================================================
	// Communication Operations callbacks.
	// ======================================================

	@Override
	public void onStart(int operationId) {
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			((MainActivity) getActivity()).hideLoadingDialog();

			if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_MY_STREAM) {
				if (responseObjects
						.containsKey(SocialMyStreamOperation.RESULT_KEY_STREAM_ITEMS)) {
					mStreamItems = (List<StreamItem>) responseObjects
							.get(SocialMyStreamOperation.RESULT_KEY_STREAM_ITEMS);

					if (mStreamItems != null) {

						if (!mStreamItems.isEmpty()) {
							mListContent.setVisibility(View.VISIBLE);
							mContainerNoContent.setVisibility(View.GONE);
						} else {
							mListContent.setVisibility(View.GONE);
							mContainerNoContent.setVisibility(View.VISIBLE);
						}
					}

					mContainerConnectionError.setVisibility(View.GONE);

					mStreamItemsAdapter.notifyDataSetChanged();

				} else {
					mListContent.setVisibility(View.GONE);
					mContainerNoContent.setVisibility(View.VISIBLE);

					mStreamItems.clear();
					mStreamItemsAdapter.notifyDataSetChanged();
				}
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		try {
			if (((MainActivity) getActivity()) != null)
				((MainActivity) getActivity()).hideLoadingDialog();
			if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_MY_STREAM) {

				if (mListContent.getVisibility() == View.VISIBLE)
					mListContent.setVisibility(View.GONE);

				if (mContainerNoContent.getVisibility() != View.VISIBLE)
					mContainerNoContent.setVisibility(View.VISIBLE);

				mStreamItems.clear();
				mStreamItemsAdapter.notifyDataSetChanged();

				if (errorType != ErrorType.OPERATION_CANCELLED) {
					Logger.e(TAG,
							"asjdfnksdjfnskdjfnsdkjfnskdjfnskdjfnk Error Error");
					mContainerConnectionError.setVisibility(View.VISIBLE);
					mContainerNoContent.setVisibility(View.GONE);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// ======================================================
	// Helper methods.
	// ======================================================

	private void initializeUserControls(View rootView) {

		mContainerNoContent = (LinearLayout) rootView
				.findViewById(R.id.social_mystream_container_no_content);
		mListContent = (ListView) rootView
				.findViewById(R.id.social_mystream_content_list);

		mTitleBar = (LinearLayout) rootView
				.findViewById(R.id.social_mystream_title_bar);

		mButtonOpen = (LanguageButton) rootView
				.findViewById(R.id.social_mystream_title_bar_button_open);
		mButtonClose = (LanguageButton) rootView
				.findViewById(R.id.social_mystream_title_bar_button_close);
		mButtonEveryone = (LanguageButton) rootView
				.findViewById(R.id.social_mystream_title_bar_button_everyone);
		mButtonFriends = (LanguageButton) rootView
				.findViewById(R.id.social_mystream_title_bar_button_friends);
		mButtonMe = (LanguageButton) rootView
				.findViewById(R.id.social_mystream_title_bar_button_me);
		mButtonInvite = (LanguageButton) rootView
				.findViewById(R.id.social_mystream_title_bar_button_invite);
		mButtonContentInvite = (LanguageButton) rootView
				.findViewById(R.id.social_mystream_container_no_content_button_invite);

		mButtonOpen.setOnClickListener(this);
		mButtonClose.setOnClickListener(this);
		mButtonEveryone.setOnClickListener(this);
		mButtonFriends.setOnClickListener(this);
		mButtonMe.setOnClickListener(this);
		mButtonInvite.setOnClickListener(this);
		mButtonContentInvite.setOnClickListener(this);

		mStreamItemsAdapter = new StreamItemsAdapter();
		mListContent.setAdapter(mStreamItemsAdapter);

		// sets the connection error message.
		mContainerConnectionError = (LinearLayout) rootView
				.findViewById(R.id.social_mystream_container_connection_error);
		Utils.SetMultilanguageTextOnTextView(
				getActivity(),
				(LanguageTextView) mContainerConnectionError
						.findViewById(R.id.connection_error_empty_view_title),
				getActivity().getString(
						R.string.connection_error_empty_view_title));

		LanguageButton retryButton = (LanguageButton) mContainerConnectionError
				.findViewById(R.id.connection_error_empty_view_button_retry);
		Utils.SetMultilanguageTextOnButton(mContext, retryButton, getActivity()
				.getString(R.string.connection_error_empty_view_button_retry));
		retryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {

				StreamItemCategory streamItemCategory = null;
				if (mButtonEveryone.isSelected()) {
					streamItemCategory = StreamItemCategory.EVERYONE;

				} else if (mButtonFriends.isSelected()) {
					streamItemCategory = StreamItemCategory.FRIENDS;

				} else {
					streamItemCategory = StreamItemCategory.ME;
				}

				mDataManager.cancelGetMyStreamItems();
				mDataManager.getMyStreamItems(streamItemCategory,
						SocialMyStreamFragment.this);
			}
		});
		if (mContainerNoContent != null)
			mContainerNoContent.setVisibility(View.GONE);
	}

	private void openTitleBar() {
		Animation slideInAnimation = AnimationUtils.loadAnimation(
				getActivity(), R.anim.slide_left_enter);
		slideInAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
				mTitleBar.setVisibility(View.VISIBLE);
			}

		});
		mButtonOpen.setVisibility(View.GONE);
		mTitleBar.startAnimation(slideInAnimation);
	}

	private void closeTitleBar() {
		try {
			Animation slideOutAnimation = AnimationUtils.loadAnimation(
					getActivity(), R.anim.slide_right_exit);
			slideOutAnimation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					mTitleBar.setVisibility(View.INVISIBLE);
				}
			});

			mTitleBar.startAnimation(slideOutAnimation);
			mButtonOpen.setVisibility(View.VISIBLE);
			return;
		} catch (Exception e) {
		} catch (Error e) {
			System.gc();
			System.runFinalization();
			System.gc();
		}
		mTitleBar.setVisibility(View.INVISIBLE);
		mButtonOpen.setVisibility(View.VISIBLE);
	}

	private void onEveryoneButtonClicked() {
		mButtonEveryone.setSelected(true);
		mButtonFriends.setSelected(false);
		mButtonMe.setSelected(false);

		mDataManager.cancelGetMyStreamItems();
		mDataManager.getMyStreamItems(StreamItemCategory.EVERYONE, this);

		mFlurrySubSectionDescription = FlurryConstants.FlurrySubSectionDescription.MyStreamEveryone
				.toString();

	}

	private void onFriendsButtonClicked() {
		mButtonEveryone.setSelected(false);
		mButtonFriends.setSelected(true);
		mButtonMe.setSelected(false);

		mDataManager.cancelGetMyStreamItems();
		mDataManager.getMyStreamItems(StreamItemCategory.FRIENDS, this);

		mFlurrySubSectionDescription = FlurryConstants.FlurrySubSectionDescription.MyStreamFriends
				.toString();

	}

	private void onMeButtonClicked() {
		mButtonEveryone.setSelected(false);
		mButtonFriends.setSelected(false);
		mButtonMe.setSelected(true);

		mDataManager.cancelGetMyStreamItems();
		mDataManager.getMyStreamItems(StreamItemCategory.ME, this);

		mFlurrySubSectionDescription = FlurryConstants.FlurrySubSectionDescription.MyStreamMe
				.toString();

	}

	// ======================================================
	// Adapter.
	// ======================================================

	private static final String STREAM_DATE_FORMATE = "dd.MM.yyyy";

	private static final class ViewHolder {

		ImageView friendThumbnail;
		TextView title;
		TextView dateLabel;
		StreamMediaItemView streamMediaItem1;
		StreamMediaItemView streamMediaItem2;
		StreamMediaItemView streamMediaItem3;
		StreamMediaItemView streamMediaItem4;
	}

	private class StreamItemsAdapter extends BaseAdapter implements
			SocialMyStreamMediaItemListener, OnClickListener {

		private Resources resources;
		private LayoutInflater mLayoutInflater;

		private int evenPositionRowColorResource;
		private int oddPositionRowColorResource;

		private int streamMediaItemViewSize;
		private int mediaItemViewMargin;

		private DateTime dateTimeNow = new DateTime(new Date());

		// private Calendar nowCalendar = Calendar.getInstance();
		// private Calendar itemCalendar = Calendar.getInstance();

		public StreamItemsAdapter() {
			mLayoutInflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			resources = getResources();

			// initializes the image loader.
			mediaItemViewMargin = resources
					.getDimensionPixelSize(R.dimen.social_mystream_item_padding);

			oddPositionRowColorResource = resources
					.getColor(R.color.social_mystream_item_background_odd);
			evenPositionRowColorResource = resources
					.getColor(R.color.social_mystream_item_background_even);

			Display display = getActivity().getWindowManager()
					.getDefaultDisplay();
			int screenWidth = 0;
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
				screenWidth = display.getWidth();
			} else {
				Point displaySize = new Point();
				display.getSize(displaySize);
				screenWidth = displaySize.x;
			}

			// calculates the mediaItemView's size, considers the item's
			// mergins.
			streamMediaItemViewSize = (screenWidth - (5 * mediaItemViewMargin)) / 4;

		}

		@Override
		public int getCount() {
			return mStreamItems.size();
		}

		@Override
		public Object getItem(int position) {
			return mStreamItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return mStreamItems.get(position).conentId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder viewHolder;
			if (convertView == null) {
				convertView = mLayoutInflater.inflate(
						R.layout.list_item_social_mystream_item, parent, false);

				viewHolder = new ViewHolder();
				viewHolder.friendThumbnail = (ImageView) convertView
						.findViewById(R.id.social_mystream_item_friend_thumbnail);
				viewHolder.title = (TextView) convertView
						.findViewById(R.id.social_mystream_item_title);
				viewHolder.dateLabel = (TextView) convertView
						.findViewById(R.id.social_mystream_item_date_from_label);
				viewHolder.streamMediaItem1 = (StreamMediaItemView) convertView
						.findViewById(R.id.social_mystream_item_streammediaitem1);
				viewHolder.streamMediaItem2 = (StreamMediaItemView) convertView
						.findViewById(R.id.social_mystream_item_streammediaitem2);
				viewHolder.streamMediaItem3 = (StreamMediaItemView) convertView
						.findViewById(R.id.social_mystream_item_streammediaitem3);
				viewHolder.streamMediaItem4 = (StreamMediaItemView) convertView
						.findViewById(R.id.social_mystream_item_streammediaitem4);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			StreamItem streamItem = mStreamItems.get(position);

			// colors the background by pattern.
			boolean isRowEven = position % 2 == 0;
			if (isRowEven) {
				convertView.setBackgroundColor(evenPositionRowColorResource);
			} else {
				convertView.setBackgroundColor(oddPositionRowColorResource);
			}

			viewHolder.friendThumbnail.setTag(R.id.view_tag_object,
					streamItem.userId);
			viewHolder.friendThumbnail
					.setImageResource(R.drawable.background_home_tile_album_default);
			Picasso.with(mContext).cancelRequest(viewHolder.friendThumbnail);
			if (mContext != null && streamItem != null
					&& streamItem.photoUrl != null
					&& !TextUtils.isEmpty(streamItem.photoUrl)) {
				Picasso.with(mContext).load(streamItem.photoUrl)
						.into(viewHolder.friendThumbnail);
			}
			viewHolder.friendThumbnail.setOnClickListener(this);

			// sets the title.
			String titleString = streamItem.userName + " " + streamItem.action
					+ " " + streamItem.title + " " + streamItem.moreSongs;
			viewHolder.title.setText(titleString);

			// sets the date label.
			DateTime streamItemDate = new DateTime(streamItem.getDate());
			int daysInterval = Days.daysBetween(dateTimeNow, streamItemDate)
					.getDays();
			String dateLabelString;
			if (daysInterval == 0) {
				// shows it as hours.
				int hours = Math.abs(Hours.hoursBetween(dateTimeNow,
						streamItemDate).getHours());
				if (hours > 0) {
					dateLabelString = Integer.toString(hours)
							+ " "
							+ Utils.getMultilanguageText(
									mContext,
									resources
											.getString(R.string.social_mystream_date_label_hours));
				} else {
					dateLabelString = Utils
							.getMultilanguageText(
									mContext,
									resources
											.getString(R.string.social_mystream_date_label_now));
				}

			} else if (daysInterval > 0 && daysInterval < 7) {
				if (daysInterval == 1) {
					// shows the "a day" label.
					dateLabelString = Utils
							.getMultilanguageText(
									mContext,
									resources
											.getString(R.string.social_mystream_date_label_a_day));
				} else {
					// shows the "X days" label.
					dateLabelString = Integer.toString(daysInterval)
							+ " "
							+ Utils.getMultilanguageText(
									mContext,
									resources
											.getString(R.string.social_mystream_date_label_days));
				}

			} else if (daysInterval == 7) {
				// shows the "week" label.
				dateLabelString = Utils
						.getMultilanguageText(
								mContext,
								resources
										.getString(R.string.social_mystream_date_label_week));
			} else {
				// sets the current date.
				dateLabelString = (String) DateFormat.format(
						STREAM_DATE_FORMATE, streamItem.getDate());
			}
			viewHolder.dateLabel.setText(dateLabelString);

			/*
			 * sets the mediaItems.
			 */
			Stack<StreamMediaItemView> socialstreamMediaItemsStack = new Stack<StreamMediaItemView>();
			socialstreamMediaItemsStack.add(viewHolder.streamMediaItem4);
			socialstreamMediaItemsStack.add(viewHolder.streamMediaItem3);
			socialstreamMediaItemsStack.add(viewHolder.streamMediaItem2);

			viewHolder.streamMediaItem4.setVisibility(View.INVISIBLE);
			viewHolder.streamMediaItem3.setVisibility(View.INVISIBLE);
			viewHolder.streamMediaItem2.setVisibility(View.INVISIBLE);

			// resets the media items views with an updated size.
			LinearLayout.LayoutParams mediaItemParams = null;

			// the first visible one.
			mediaItemParams = new LinearLayout.LayoutParams(
					streamMediaItemViewSize, streamMediaItemViewSize, 1);
			mediaItemParams.rightMargin = mediaItemViewMargin;
			viewHolder.streamMediaItem1.setLayoutParams(mediaItemParams);

			for (StreamMediaItemView streamMediaItemView : socialstreamMediaItemsStack) {
				mediaItemParams = new LinearLayout.LayoutParams(
						streamMediaItemViewSize, streamMediaItemViewSize, 1);
				mediaItemParams.rightMargin = mediaItemViewMargin;
				streamMediaItemView.setLayoutParams(mediaItemParams);
			}

			// populates the first visible one.
			MediaItem firstItem = createMediaItemFromStreamItem(streamItem);
			viewHolder.streamMediaItem1.setVisibility(View.VISIBLE);
			viewHolder.streamMediaItem1.setTag(R.id.view_tag_object, firstItem);
			viewHolder.streamMediaItem1
					.setSocialMyStreamMediaItemListener(this);

			if (StreamItem.TYPE_BADGE.equalsIgnoreCase(streamItem.type)) {
				viewHolder.streamMediaItem1.setPlayButtonVisibilty(false);
				viewHolder.streamMediaItem1.setClickable(false);
				if (isRowEven) {
					viewHolder.streamMediaItem1
							.setBackgroundColor(evenPositionRowColorResource);
				} else {
					viewHolder.streamMediaItem1
							.setBackgroundColor(oddPositionRowColorResource);
				}

			} else {
				viewHolder.streamMediaItem1.setPlayButtonVisibilty(true);
				viewHolder.streamMediaItem1.setClickable(true);
				viewHolder.streamMediaItem1
						.setImageResource(R.drawable.background_home_tile_album_default);
			}

			Picasso.with(mContext).cancelRequest(
					viewHolder.streamMediaItem1.getBackgroundImage());

			if (mContext != null && firstItem != null) {
				String imageUrl = firstItem.getImageUrl();
				String[] images = ImagesManager.getImagesUrlArray(
						firstItem.getImagesUrlArray(),
						ImagesManager.RADIO_LIST_ART,
						DataManager.getDisplayDensityLabel());
				if (images != null && images.length > 0) {
					imageUrl = images[0];
					Picasso.with(mContext)
							.load(imageUrl)
							.into(viewHolder.streamMediaItem1
									.getBackgroundImage());
				}
			} else {
				viewHolder.streamMediaItem1.setVisibility(View.INVISIBLE);
			}

			// populates the other.
			if (!Utils.isListEmpty(streamItem.moreSongsItems)) {
				List<MediaItem> mediaItems = streamItem.moreSongsItems;
				StreamMediaItemView mediaItemView = null;

				for (MediaItem mediaItem : mediaItems) {

					if (socialstreamMediaItemsStack.isEmpty()) {
						break;
					}

					mediaItemView = socialstreamMediaItemsStack.pop();
					mediaItemView.setVisibility(View.VISIBLE);
					mediaItemView.setTag(R.id.view_tag_object, mediaItem);
					mediaItemView.setSocialMyStreamMediaItemListener(this);

					Picasso.with(mContext).cancelRequest(
							mediaItemView.getBackgroundImage());

					if (mContext != null && mediaItem != null) {
						String imageUrl = mediaItem.getImageUrl();
						String[] images = ImagesManager.getImagesUrlArray(
								mediaItem.getImagesUrlArray(),
								ImagesManager.RADIO_LIST_ART,
								DataManager.getDisplayDensityLabel());
						if (images != null && images.length > 0) {
							imageUrl = images[0];
							Picasso.with(mContext).load(imageUrl)
									.into(mediaItemView.getBackgroundImage());
						}
					}
				}
			}

			return convertView;
		}

		@Override
		public void onPlayButtonClicked(View mediaItemView) {
			MediaItem mediaItem = (MediaItem) mediaItemView
					.getTag(R.id.view_tag_object);

			if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
				if (mOnMediaItemOptionSelectedListener != null) {
					mOnMediaItemOptionSelectedListener
							.onMediaItemOptionShowDetailsSelected(mediaItem, -2);

					Map<String, String> reportMap = new HashMap<String, String>();

					reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong
							.toString(), mediaItem.getTitle());
					reportMap.put(mediaItem.getMediaType().toString(),
							Utils.toWhomSongBelongto(mediaItem));
					reportMap
							.put(FlurryConstants.FlurryKeys.Source.toString(),
									FlurryConstants.FlurrySourceDescription.TapOnPlayButtonTile
											.toString());
					reportMap.put(
							FlurryConstants.FlurryKeys.SubSection.toString(),
							mFlurrySubSectionDescription);

					Analytics.logEvent(
							FlurryConstants.FlurryEventName.SongSelectedForPlay
									.toString(), reportMap);
				}
			} else if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
				if (mOnMediaItemOptionSelectedListener != null) {
					mOnMediaItemOptionSelectedListener
							.onMediaItemOptionShowDetailsSelected(mediaItem, 0);

					Map<String, String> reportMap = new HashMap<String, String>();

					reportMap.put(FlurryConstants.FlurryKeys.Title.toString(),
							mediaItem.getTitle());
					reportMap.put(
							FlurryConstants.FlurryKeys.SubSection.toString(),
							mFlurrySubSectionDescription);

					Analytics.logEvent(
							FlurryConstants.FlurryEventName.VideoSelected
									.toString(), reportMap);
				}
			}
		}

		@Override
		public void onItemClicked(View mediaItemView) {
			MediaItem mediaItem = (MediaItem) mediaItemView
					.getTag(R.id.view_tag_object);

			if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
				if (mOnMediaItemOptionSelectedListener != null) {
					mOnMediaItemOptionSelectedListener
							.onMediaItemOptionShowDetailsSelected(mediaItem, -2);

					Map<String, String> reportMap = new HashMap<String, String>();

					reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong
							.toString(), mediaItem.getTitle());
					reportMap.put(mediaItem.getMediaType().toString(),
							Utils.toWhomSongBelongto(mediaItem));
					reportMap
							.put(FlurryConstants.FlurryKeys.Source.toString(),
									FlurryConstants.FlurrySourceDescription.TapOnSongTile
											.toString());
					reportMap.put(
							FlurryConstants.FlurryKeys.SubSection.toString(),
							mFlurrySubSectionDescription);

					Analytics.logEvent(
							FlurryConstants.FlurryEventName.SongSelectedForPlay
									.toString(), reportMap);
				}
			} else if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
				if (mOnMediaItemOptionSelectedListener != null) {
					mOnMediaItemOptionSelectedListener
							.onMediaItemOptionShowDetailsSelected(mediaItem, 0);

					Map<String, String> reportMap = new HashMap<String, String>();

					reportMap.put(FlurryConstants.FlurryKeys.Title.toString(),
							mediaItem.getTitle());
					reportMap.put(
							FlurryConstants.FlurryKeys.SubSection.toString(),
							mFlurrySubSectionDescription);

					Analytics.logEvent(
							FlurryConstants.FlurryEventName.VideoSelected
									.toString(), reportMap);
				}
			}
		}

		@Override
		public void onPlayButtonLongClicked(View mediaItemView) {
			MediaItem mediaItem = (MediaItem) mediaItemView
					.getTag(R.id.view_tag_object);
			if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
				showMediaItemOptionsDialog(mediaItem, 0);
			}
		}

		@Override
		public void onItemLongClicked(View mediaItemView) {
			MediaItem mediaItem = (MediaItem) mediaItemView
					.getTag(R.id.view_tag_object);
			if (mediaItem.getMediaContentType() == MediaContentType.MUSIC
					|| mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
				showMediaItemOptionsDialog(mediaItem, 0);
			}
		}

		private void showMediaItemOptionsDialog(final MediaItem mediaItem,
				final int position) {
			// set up custom dialog
			final Dialog dialog = new Dialog(getActivity());
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.dialog_media_playing_options);
			dialog.setCancelable(true);
			dialog.show();

			// sets the title.
			TextView title = (TextView) dialog
					.findViewById(R.id.long_click_custom_dialog_title_text);
			title.setText(mediaItem.getTitle());

			// sets the cancel button.
			ImageButton closeButton = (ImageButton) dialog
					.findViewById(R.id.long_click_custom_dialog_title_image);
			closeButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});

			// sets the options buttons.
			LinearLayout llPlayNow = (LinearLayout) dialog
					.findViewById(R.id.long_click_custom_dialog_download);
			LinearLayout llAddtoQueue = (LinearLayout) dialog
					.findViewById(R.id.long_click_custom_dialog_add_to_queue_row);
			LinearLayout llDetails = (LinearLayout) dialog
					.findViewById(R.id.long_click_custom_dialog_details_row);
			LinearLayout llSaveOffline = (LinearLayout) dialog
					.findViewById(R.id.long_click_custom_dialog_save_offline_row);

			if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
				llPlayNow.setVisibility(View.GONE);
				llAddtoQueue.setVisibility(View.GONE);
			}
			llSaveOffline.setTag(false);
			CustomCacheStateProgressBar progressCacheState = (CustomCacheStateProgressBar) dialog
					.findViewById(R.id.long_click_custom_dialog_save_offline_progress_cache_state);
			progressCacheState.setNotCachedStateVisibility(true);
			progressCacheState.setTag(R.id.view_tag_object, mediaItem);

			if (mediaItem.getMediaType() == MediaType.TRACK) {
				CacheState cacheState;
				int progress = 0;
				if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
					cacheState = DBOHandler.getVideoTrackCacheState(
							mContext, "" + mediaItem.getId());
					progress = DBOHandler.getVideoTrackCacheProgress(
							mContext, "" + mediaItem.getId());
				} else {
					cacheState = DBOHandler.getTrackCacheState(mContext, ""
							+ mediaItem.getId());
					progress = DBOHandler.getTrackCacheProgress(mContext,
							"" + mediaItem.getId());
				}
				if (cacheState == CacheState.CACHED) {
					llSaveOffline.setTag(true);
					((TextView) dialog
							.findViewById(R.id.long_click_custom_dialog_save_offline_text))
							.setText(mContext.getResources().getString(
									R.string.caching_text_play_offline));// "Play Offline"
				} else if (cacheState == CacheState.CACHING
						|| cacheState == CacheState.QUEUED) {
					llSaveOffline.setTag(null);
					((TextView) dialog
							.findViewById(R.id.long_click_custom_dialog_save_offline_text))
							.setText(mContext.getResources().getString(
									R.string.caching_text_saving));
				}
				progressCacheState.setCacheState(cacheState);
				progressCacheState.setProgress(progress);
			} else if (mediaItem.getMediaType() == MediaType.ALBUM) {
				CacheState cacheState = DBOHandler.getAlbumCacheState(
						mContext, "" + mediaItem.getId());
				if (cacheState == CacheState.CACHED) {
					int trackCacheCount = DBOHandler.getAlbumCachedCount(
							mContext, "" + mediaItem.getId());
					if (trackCacheCount >= mediaItem.getMusicTrackCount())
						llSaveOffline.setTag(true);
					((TextView) dialog
							.findViewById(R.id.long_click_custom_dialog_save_offline_text))
							.setText(mContext.getResources().getString(
									R.string.caching_text_play_offline));
				} else if (cacheState == CacheState.CACHING
						|| cacheState == CacheState.QUEUED) {
					llSaveOffline.setTag(null);
					((TextView) dialog
							.findViewById(R.id.long_click_custom_dialog_save_offline_text))
							.setText(mContext.getResources().getString(
									R.string.caching_text_saving));
				}
				progressCacheState.setCacheCountVisibility(true);
				progressCacheState.setCacheCount(""
						+ DBOHandler.getAlbumCachedCount(mContext, ""
								+ mediaItem.getId()));
				progressCacheState.setCacheState(cacheState);
			} else if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
				CacheState cacheState = DBOHandler.getPlaylistCacheState(
						mContext, "" + mediaItem.getId());
				if (cacheState == CacheState.CACHED) {
					int trackCacheCount = DBOHandler
							.getPlaylistCachedCount(mContext, ""
									+ mediaItem.getId());
					if (trackCacheCount >= mediaItem.getMusicTrackCount())
						llSaveOffline.setTag(true);
					((TextView) dialog
							.findViewById(R.id.long_click_custom_dialog_save_offline_text))
							.setText(mContext.getResources().getString(
									R.string.caching_text_play_offline));
				} else if (cacheState == CacheState.CACHING
						|| cacheState == CacheState.QUEUED) {
					llSaveOffline.setTag(null);
					((TextView) dialog
							.findViewById(R.id.long_click_custom_dialog_save_offline_text))
							.setText(mContext.getResources().getString(
									R.string.caching_text_saving));
				}
				progressCacheState.setCacheCountVisibility(true);
				progressCacheState.setCacheCount(""
						+ DBOHandler.getPlaylistCachedCount(mContext, ""
								+ mediaItem.getId()));
				progressCacheState.setCacheState(cacheState);
			} else if (mediaItem.getMediaType() == MediaType.VIDEO) {
				CacheState cacheState = DBOHandler.getVideoTrackCacheState(
						mContext, "" + mediaItem.getId());
				if (cacheState == CacheState.CACHED) {
					llSaveOffline.setTag(true);
					((TextView) dialog
							.findViewById(R.id.long_click_custom_dialog_save_offline_text))
							.setText(mContext.getResources().getString(
									R.string.caching_text_play_offline));
				} else if (cacheState == CacheState.CACHING
						|| cacheState == CacheState.QUEUED) {
					llSaveOffline.setTag(null);
					((TextView) dialog
							.findViewById(R.id.long_click_custom_dialog_save_offline_text))
							.setText(mContext.getResources().getString(
									R.string.caching_text_saving));
				}
				progressCacheState.setCacheCountVisibility(true);
				progressCacheState.setCacheCount(""
						+ DBOHandler.getPlaylistCachedCount(mContext, ""
								+ mediaItem.getId()));
				progressCacheState.setCacheState(cacheState);
			}

			llSaveOffline.setVisibility(View.VISIBLE);

			// play now.
			llPlayNow.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {

					// download
					MediaItem trackMediaItem = new MediaItem(mediaItem.getId(),
							mediaItem.getTitle(), mediaItem.getAlbumName(),
							mediaItem.getArtistName(), mediaItem.getImageUrl(),
							mediaItem.getBigImageUrl(), MediaType.TRACK
									.toString(), 0, mediaItem.getAlbumId());
					Intent intent = new Intent(mContext,
							DownloadConnectingActivity.class);
					intent.putExtra(
							DownloadConnectingActivity.EXTRA_MEDIA_ITEM,
							(Serializable) trackMediaItem);
					getActivity().startActivity(intent);

					Map<String, String> reportMap = new HashMap<String, String>();
					reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong
							.toString(), mediaItem.getTitle());
					reportMap.put(
							FlurryConstants.FlurryKeys.SourceSection.toString(),
							mFlurrySubSectionDescription);
					Analytics.logEvent(
							FlurryConstants.FlurryEventName.Download.toString(),
							reportMap);

					dialog.dismiss();
				}
			});

			// add to queue.
			llAddtoQueue.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (mOnMediaItemOptionSelectedListener != null) {
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionAddToQueueSelected(mediaItem,
										position);

						Map<String, String> reportMap = new HashMap<String, String>();

						reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong
								.toString(), mediaItem.getTitle());
						reportMap.put(mediaItem.getMediaType().toString(),
								Utils.toWhomSongBelongto(mediaItem));
						reportMap.put(
								FlurryConstants.FlurryKeys.Source.toString(),
								FlurryConstants.FlurrySourceDescription.TapOnPlayInContextualMenu
										.toString());
						reportMap.put(FlurryConstants.FlurryKeys.SubSection
								.toString(), mFlurrySubSectionDescription);

						Analytics
								.logEvent(
										FlurryConstants.FlurryEventName.SongSelectedForPlay
												.toString(), reportMap);
					}
					dialog.dismiss();
				}
			});

			// show details.
			llDetails.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View view) {
					if (mOnMediaItemOptionSelectedListener != null) {
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										mediaItem, position);
					}
					dialog.dismiss();
				}
			});

			// Save Offline
			llSaveOffline.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (mOnMediaItemOptionSelectedListener != null
							&& view.getTag() != null) {
						if ((Boolean) view.getTag())
							if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {

								Toast.makeText(getActivity(),
										R.string.already_offline_message_track,
										Toast.LENGTH_SHORT).show();
							} else {
								if (mediaItem.getMediaType() == MediaType.TRACK) {

									CacheManager.removeTrackFromCache(
											getActivity(), mediaItem.getId(),
											MediaContentType.MUSIC);
								} else {

									Toast.makeText(
											getActivity(),
											R.string.already_offline_message_for_tracklist,
											Toast.LENGTH_SHORT).show();
								}
							}
						else
							mOnMediaItemOptionSelectedListener
									.onMediaItemOptionSaveOfflineSelected(
											mediaItem, position);
					}
					dialog.dismiss();
				}
			});
		}

		private MediaItem createMediaItemFromStreamItem(StreamItem streamItem) {
			MediaItem mediaItem = new MediaItem(streamItem.conentId,
					streamItem.title, streamItem.albumName,
					streamItem.albumName, streamItem.imageUrl,
					streamItem.bigImageUrl, streamItem.type,
					streamItem.songsCount, streamItem.getAlbumId());

			mediaItem.setImagesUrlArray(streamItem.getImages());

			if (MediaType.VIDEO.name().equalsIgnoreCase(streamItem.type)) {
				mediaItem.setMediaContentType(MediaContentType.VIDEO);

			} else if (MediaType.BADGE.name().equalsIgnoreCase(streamItem.type)) {
				mediaItem.setMediaContentType(MediaContentType.BADGE);

			} else {
				mediaItem.setMediaContentType(MediaContentType.MUSIC);
			}

			return mediaItem;
		}

		@Override
		public void onClick(View view) {
			long friendId = (Long) view.getTag(R.id.view_tag_object);

            Bundle arguments = new Bundle();
            arguments.putString(ProfileActivity.DATA_EXTRA_USER_ID,
                    String.valueOf(friendId));

            myStreamActivity.openProfile(arguments);


//			Intent profileActivity = new Intent(getActivity()
//					.getApplicationContext(), ProfileActivity.class);
//			profileActivity.putExtras(arguments);
//			startActivity(profileActivity);
		}

	}

	@Override
	public void onDestroyView() {
//		System.gc();
//		System.runFinalization();
		super.onDestroyView();
	}
}
