package com.hungama.myplay.activity.ui.inappprompts;

// Idan1
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;

import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.UpgradeActivity;
import com.hungama.myplay.activity.ui.widgets.CustomAlertDialog;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;

import java.util.HashMap;
import java.util.Map;

public class AppPromptOfflineCaching3rdSong {

	private static final boolean DEBUG = false;

	public static final int SESSION_FREQUENCY = 6;

	private Activity mContext;

	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;

	public AppPromptOfflineCaching3rdSong(Activity context) {
		mContext = context;

		mDataManager = DataManager.getInstance(mContext);
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();

	}

	public boolean appLaunched(boolean canPromptForRating, boolean isCached) {
		if (canPromptForRating && ratingConditionsHaveBeenMet(isCached)) {
			CustomAlertDialog prompt = new CustomAlertDialog(mContext);
			AppPromptConstants constants = new AppPromptConstants(mContext,
					AppPromptConstants.KEY_OFFLINE_CACHING_3RD_SONG);
			prompt.setMessage(constants.getMessage());
			prompt.setPositiveButton(constants.getPositiveButtonText(),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Boolean loggedIn = mApplicationConfigurations.isRealUser();
							Map<String, String> reportMap = new HashMap<String, String>();
							reportMap.put(FlurryConstants.FlurrySubscription.SourcePage
											.toString(),
									AppPromptConstants.KEY_OFFLINE_CACHING_3RD_SONG);
							reportMap.put(
									FlurryConstants.FlurrySubscription.LoggedIn.toString(),
									loggedIn.toString());
							Analytics.logEvent(
									FlurryConstants.FlurrySubscription.TapsOnUpgrade
											.toString(), reportMap);

							Intent intent = new Intent(mContext,
									UpgradeActivity.class);
							intent.putExtra(UpgradeActivity.IS_TRIAL_PLANS,
									true);
							intent.putExtra(
									UpgradeActivity.EXTRA_IS_GO_OFFLINE, false);
							intent.putExtra(
									UpgradeActivity.EXTRA_IS_FROM_NO_INTERNET_PROMT,
									false);
							mContext.startActivityForResult(intent,
									HomeActivity.UPGRADE_ACTIVITY_RESULT_CODE);
						}
					});
			prompt.setNegativeButton(constants.getNegativeButtonText(), null);
			prompt.setCancelable(false);
			prompt.setOnDismissListener(new CustomAlertDialog.OnDismissListener() {
				@Override
				public void onDismiss() {
					mContext.finish();
				}
			});
			prompt.show();
			return true;
		} else {
			return false;
		}
	}

	private boolean ratingConditionsHaveBeenMet(boolean isCached) {
		try {
			if (DEBUG) {
				return true;
			}

			if (CacheManager.isTrialUser(mContext)
					|| CacheManager.isProUser(mContext)) {
				return false;
			}

			if ((mApplicationConfigurations.getLastSessionOffline3rdSong10() == 0 && isCached)
					|| (mApplicationConfigurations
							.getLastSessionOffline3rdSong10() > 0 && mApplicationConfigurations
							.getLastSessionOffline3rdSong10()
							+ SESSION_FREQUENCY == mApplicationConfigurations
							.getTotalSession())) {
				Logger.s("------show popup "
						+ mApplicationConfigurations.getTotalSession());
				mApplicationConfigurations
						.setLastSessionOffline3rdSong10(mApplicationConfigurations
								.getTotalSession());
				return true;
			}

		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return false;
	}
}
