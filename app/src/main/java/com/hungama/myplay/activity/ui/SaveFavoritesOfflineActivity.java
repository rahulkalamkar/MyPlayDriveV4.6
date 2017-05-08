/**
 * 
 */
package com.hungama.myplay.activity.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.social.Profile;
import com.hungama.myplay.activity.data.dao.hungama.social.ProfileFavoriteMediaItems;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.SocialProfileFavoriteMediaItemsOperation;
import com.hungama.myplay.activity.operations.hungama.SocialProfileOperation;
import com.hungama.myplay.activity.ui.dialogs.MyProgressDialog;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.ScreenLockStatus;
import com.hungama.myplay.activity.util.Utils;

import java.util.List;
import java.util.Map;

/**
 * @author XTPL
 * 
 */
public class SaveFavoritesOfflineActivity extends Activity implements
		OnClickListener, CommunicationOperationListener {

	private final String TAG = "SaveFavoritesOfflineActivity";

	private FavoritesAdapter mFavoritesAdapter;
	private Profile mUserProfile;
	private DataManager mDataManager;
	private int ProgressCount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_save_favorites_offline);

		TextView text = (TextView) findViewById(R.id.text_not_now);
		SpannableString spanString = new SpannableString("Not Now");
		spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
		text.setText(spanString);
		text.setOnClickListener(this);
		findViewById(R.id.ll_bottom).setOnClickListener(this);

		// ListView mFavoritesList = (ListView)
		// findViewById(R.id.list_view_favorites);
		// mFavoritesAdapter = new FavoritesAdapter();
		// mFavoritesList.setAdapter(mFavoritesAdapter);

		String mUserId = "";
		mDataManager = DataManager.getInstance(this);
		// checks if the given user is the application user.
		ApplicationConfigurations applicationConfigurations = mDataManager
				.getApplicationConfigurations();
		String applicationUserId = applicationConfigurations.getPartnerUserId();
		if (TextUtils.isEmpty(mUserId) || applicationUserId.equals(mUserId)) {
			mUserId = mDataManager.getApplicationConfigurations()
					.getPartnerUserId();
			mDataManager.getUserProfile(this, mUserId, this);
		} else {
		}
		Logger.i(TAG, "User Id: " + mUserId);
	}

	private class FavoritesAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private String[] items = { "Favorite Songs", "Favorite Albums",
				"Favorite Playlists", "Favorite Videos" };
		private String[] subItems = { " Songs", " Albums", " Playlists",
				" Videos" };
		private boolean[] isChecked = { true, false, false, false };
		private int counts[];

		public FavoritesAdapter(int counts[]) {
			this.counts = counts;
			mInflater = getLayoutInflater();
		}

		@Override
		public int getCount() {
			return items.length;
		}

		@Override
		public Object getItem(int arg0) {
			return items[arg0];
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(final int arg0, View arg1, ViewGroup arg2) {
			arg1 = mInflater.inflate(R.layout.list_item_save_favorites_offline,
					null);
			((TextView) arg1.findViewById(R.id.text_title))
					.setText(items[arg0]);
			((TextView) arg1.findViewById(R.id.text_subtitle))
					.setText(counts[arg0] + subItems[arg0]);
			CheckBox checkBox = (CheckBox) arg1
					.findViewById(R.id.check_box_save_offline);
			checkBox.setChecked(isChecked[arg0]);
			checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton button, boolean arg1) {
					isChecked[arg0] = arg1;
				}
			});
			return arg1;
		}

		public boolean isItemChecked(int position) {
			return isChecked[position];
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.text_not_now:
			finish();
			break;
		case R.id.ll_bottom:
			if (mFavoritesAdapter != null && mUserProfile != null) {
				String mUserId = mDataManager.getApplicationConfigurations()
						.getPartnerUserId();
				ProgressCount = 0;
				if (mFavoritesAdapter.isItemChecked(0)) {
					mDataManager.getFavorites(this, MediaType.TRACK, mUserId,
							this);
					ProgressCount++;
				}
				if (mFavoritesAdapter.isItemChecked(1)) {
					mDataManager.getFavorites(this, MediaType.ALBUM, mUserId,
							this);
					ProgressCount++;
				}
				if (mFavoritesAdapter.isItemChecked(2)) {
					mDataManager.getFavorites(this, MediaType.PLAYLIST,
							mUserId, this);
					ProgressCount++;
				}
				if (mFavoritesAdapter.isItemChecked(3)) {
					mDataManager.getFavorites(this, MediaType.VIDEO, mUserId,
							this);
					ProgressCount++;
				}
			}
			finish();
			break;
		}
	}

	@Override
	public void finish() {
		if (!getIntent().getBooleanExtra("isLearnMore", false)) {
			Intent startHomeActivityIntent = new Intent(
					getApplicationContext(), HomeActivity.class);
			if (ApplicationConfigurations.getInstance(this)
					.getSaveOfflineMode())
				startHomeActivityIntent = new Intent(getApplicationContext(),
						GoOfflineActivity.class);
			startHomeActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(startHomeActivityIntent);
		}
		super.finish();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hungama.myplay.activity.communication.CommunicationOperationListener
	 * #onStart(int)
	 */
	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE) {
			showLoadingDialog(Utils.getMultilanguageTextHindi(
					getApplicationContext(),
					getString(R.string.application_dialog_loading)));
		} else if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS) {
			showLoadingDialog(Utils.getMultilanguageTextHindi(
					getApplicationContext(),
					getString(R.string.application_dialog_loading)));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hungama.myplay.activity.communication.CommunicationOperationListener
	 * #onSuccess(int, java.util.Map)
	 */
	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE) {
				mUserProfile = (Profile) responseObjects
						.get(SocialProfileOperation.RESULT_KEY_PROFILE);
				if (mUserProfile != null) {
					int[] counts = new int[4];
					counts[0] = mUserProfile.userFavoriteSongs.songsCount;
					counts[1] = mUserProfile.userFavoriteAlbums.albumCount;
					counts[2] = mUserProfile.userFavoritePlaylists.playlistCount;
					counts[3] = mUserProfile.userFavoriteVideos.videoCount;

					ListView mFavoritesList = (ListView) findViewById(R.id.list_view_favorites);
					mFavoritesAdapter = new FavoritesAdapter(counts);
					mFavoritesList.setAdapter(mFavoritesAdapter);
				}
				hideLoadingDialog();
			} else if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS) {
				ProfileFavoriteMediaItems profileFavoriteMediaItems = (ProfileFavoriteMediaItems) responseObjects
						.get(SocialProfileFavoriteMediaItemsOperation.RESULT_KEY_PROFILE_FAVORITE_MEDIA_ITEMS);
				List<MediaItem> mMediaItems = profileFavoriteMediaItems.mediaItems;

				if (mMediaItems != null) {
					for (MediaItem mediaItem : mMediaItems) {
						if (mediaItem.getMediaType() == MediaType.TRACK) {
							Track track = new Track(mediaItem.getId(),
									mediaItem.getTitle(),
									mediaItem.getAlbumName(),
									mediaItem.getArtistName(),
									mediaItem.getImageUrl(),
									mediaItem.getBigImageUrl(),
									mediaItem.getImages(),
									mediaItem.getAlbumId());
							CacheManager.saveOfflineAction(this, mediaItem,
									track);
						} else {
							CacheManager.saveOfflineAction(this, mediaItem,
									null);
						}
						Utils.saveOfflineFlurryEvent(this,
								FlurryConstants.FlurryCaching.SaveFavorites
										.toString(), mediaItem);
					}
				}
				ProgressCount--;
				if (ProgressCount == 0) {
					hideLoadingDialog();
					finish();
				}
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hungama.myplay.activity.communication.CommunicationOperationListener
	 * #onFailure(int,
	 * com.hungama.myplay.activity.communication.CommunicationManager.ErrorType,
	 * java.lang.String)
	 */
	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE) {
			hideLoadingDialog();
		} else if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS) {
			ProgressCount--;
			if (ProgressCount == 0) {
				hideLoadingDialog();
				finish();
			}
		}
	}

	private MyProgressDialog mProgressDialog;

	public void showLoadingDialog(String message) {
		if (!isFinishing()) {
			if (mProgressDialog == null) {
				// mProgressDialog = ProgressDialog.show(this, "", message,
				// true);
				mProgressDialog = new MyProgressDialog(this);
				mProgressDialog.setCancelable(true);
				mProgressDialog.setCanceledOnTouchOutside(false);
			}
		}
	}

	public void hideLoadingDialog() {
		try {
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		ScreenLockStatus.getInstance(getBaseContext()).onStop();

	}

	@Override
	protected void onUserLeaveHint() {
		ScreenLockStatus.getInstance(getBaseContext()).onStop(true, this);
		super.onUserLeaveHint();
	}

	@Override
	protected void onResume() {
		ScreenLockStatus.getInstance(getBaseContext()).onResume(this, this);
		super.onResume();
	}
}
