package com.hungama.myplay.activity.ui;

import java.io.Serializable;
import java.util.Set;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.BadgesAndCoins;
import com.hungama.myplay.activity.ui.MainActivity.NavigationItem;
import com.hungama.myplay.activity.ui.fragments.BadgesAndCoinsFragment;
import com.hungama.myplay.activity.ui.fragments.BadgesAndCoinsFragment.OnNotificationFinishedListener;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.hungama.myplay.activity.util.ScreenLockStatus;
import com.hungama.myplay.activity.util.Utils;

public class BadgesAndCoinsActivity extends FragmentActivity implements
		OnNotificationFinishedListener {

	private static final String TAG = "BadgesAndCoinsActivity";

	public static final String ARGUMENT_OBJECT = "argument_object";
	public static final String ARGUMENT_IS_FINISHED_BADGES = "argument_is_finished_badges";

	private FragmentManager mFragmentManager;
	private FragmentTransaction fragmentTransaction;
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;

	private BadgesAndCoins mBadgesAndCoins;
	// private ImageFetcher mImageFetcher = null;
	private String url;

	private volatile boolean mIsDestroyed = false;

	// ======================================================
	// ACTIVITY'S LIFECYCLE.
	// ======================================================

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_badges_and_coins);

		mDataManager = DataManager.getInstance(this.getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		// shows the favorite type selection dialog.
		mBadgesAndCoins = (BadgesAndCoins) getIntent().getSerializableExtra(
				ARGUMENT_OBJECT);
		if (mBadgesAndCoins != null) {
			Bundle data = new Bundle();
			data.putSerializable(
					BadgesAndCoinsFragment.FRAGMENT_ARGUMENT_BADGES_AND_COINS,
					(Serializable) mBadgesAndCoins);
			addFragment(data);
		}
		if (mBadgesAndCoins.getBadgeUrl() != null) {
			Logger.i(TAG, mBadgesAndCoins.getBadgeUrl().toString());
			url = mBadgesAndCoins.getBadgeUrl();
			Logger.i(TAG, url);
		}

		if (mBadgesAndCoins != null) {
			try {
				String name = mBadgesAndCoins.getBadgeName().trim();
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
							Logger.d(TAG,
									"Tag Added >>" + name.replaceAll(" ", "_"));

							i = badgesetKeys.length;
						}
					}
					Utils.AddTag(tags);
				}
			} catch (Exception e) {
			}
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
	public void onBackPressed() {
	}

	// protected NavigationItem getNavigationItem() {
	// return NavigationItem.OTHER;
	// }

	@Override
	protected void onResume() {
		super.onResume();
		ScreenLockStatus.getInstance(getBaseContext()).onResume(this, this);
		// if (mImageFetcher != null) {
		// mImageFetcher.setExitTasksEarly(false);
		// }
	}

	@Override
	protected void onPause() {
		super.onPause();

		// if (mImageFetcher != null) {
		// mImageFetcher.setExitTasksEarly(true);
		// mImageFetcher.flushCache();
		// }
	}

	@Override
	protected void onDestroy() {
		mIsDestroyed = true;
		Intent intent = new Intent(this, BadgesAndCoinsActivity.class);
		intent.putExtra(BadgesAndCoinsActivity.ARGUMENT_IS_FINISHED_BADGES,
				true);
		setResult(RESULT_OK, intent);
		super.onDestroy();

		// if (mImageFetcher != null) {
		// mImageFetcher.closeCache();
		// mImageFetcher = null;
		// }
	}

	@Override
	protected void onStop() {
		ScreenLockStatus.getInstance(getBaseContext()).onStop();
		super.onStop();
	}

	@Override
	protected void onUserLeaveHint() {
		ScreenLockStatus.getInstance(getBaseContext()).onStop(true, this);
		super.onUserLeaveHint();
	}

	// ======================================================
	// Helper Methods.
	// ======================================================

	public void addFragment(Bundle detailsData) {

		mFragmentManager = getSupportFragmentManager();
		fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(
				R.anim.slide_and_show_bottom_enter,
				R.anim.slide_and_show_bottom_exit,
				R.anim.slide_and_show_bottom_enter,
				R.anim.slide_and_show_bottom_exit);

		BadgesAndCoinsFragment mBadgesAndCoinsFragment = new BadgesAndCoinsFragment();
		mBadgesAndCoinsFragment.setOnNotificationFinishedListener(this);
		mBadgesAndCoinsFragment.setArguments(detailsData);
		fragmentTransaction.add(R.id.main_fragmant_container,
				mBadgesAndCoinsFragment);

		if(Constants.IS_COMMITALLOWSTATE)
			fragmentTransaction.commitAllowingStateLoss();
		else
			fragmentTransaction.commit();
	}

	private void showBadgeDialog() {
		try {
			// set up custom dialog
			final Dialog dialog = new Dialog(this,
					android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

			// LinearLayout root = (LinearLayout) LayoutInflater.from(
			// BadgesAndCoinsActivity.this).inflate(
			// R.layout.dialog_badge_notification, null);
			// dialog.setContentView(root);
			dialog.setContentView(R.layout.dialog_badge_notification);
			if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
				Utils.traverseChild(dialog.getWindow().getDecorView(),
						BadgesAndCoinsActivity.this);
			}

			LanguageTextView text = (LanguageTextView) dialog
					.findViewById(R.id.badge_bottom_inner_text_3);
			text.setText(mBadgesAndCoins.getBadgeName());

			// initializes the image loader.
			ImageView imageBadge = (ImageView) dialog
					.findViewById(R.id.badge_dialog_image);

			// creates the cache.
			// ImageCache.ImageCacheParams cacheParams =
			// new ImageCache.ImageCacheParams(this,
			// DataManager.FOLDER_THUMBNAILS_CACHE);
			// cacheParams.compressFormat = Bitmap.CompressFormat.PNG;
			// cacheParams.setMemCacheSizePercent(this, 0.10f);
			//
			// mImageFetcher = new ImageFetcher(this,
			// imageBadge.getMeasuredWidth(),
			// imageBadge.getMeasuredHeight());
			// mImageFetcher.addImageCache(getSupportFragmentManager(),
			// cacheParams);
			// mImageFetcher.setImageFadeIn(true);
			// mImageFetcher.loadImage(mBadgesAndCoins.getBadgeUrl(),
			// imageBadge);

			if (this != null && mBadgesAndCoins.getBadgeUrl() != null
					&& !TextUtils.isEmpty(mBadgesAndCoins.getBadgeUrl())) {
				PicassoUtil.with(this).load(null,
						mBadgesAndCoins.getBadgeUrl(), imageBadge, -1);
			}

			ImageButton closeButton = (ImageButton) dialog
					.findViewById(R.id.badges_info_dialog_title_close_button);
			closeButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					finish();
				}
			});

			dialog.setCancelable(true);
			dialog.setCanceledOnTouchOutside(false);
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					dialog.dismiss();
					finish();

				}
			});

			dialog.show();
			if (mApplicationConfigurations.isFirstBadgeDisplayed()) {
				// Appirater appirater = new Appirater(this);
				// appirater.userDidSignificantEvent(true);
				mApplicationConfigurations.setIsFirstBadgeDisplayed(false);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	public boolean isActivityDestroyed() {
		return mIsDestroyed;
	}

	// ======================================================
	// ACTIVITY'S Listener.
	// ======================================================

	@Override
	public void onNotificationFinishedListener() {
		showBadgeDialog();
	}

}
