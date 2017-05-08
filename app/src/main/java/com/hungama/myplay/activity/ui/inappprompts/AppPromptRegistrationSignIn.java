package com.hungama.myplay.activity.ui.inappprompts;

// Idan1
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;

import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.LoginActivity;
import com.hungama.myplay.activity.ui.dialogs.RegisterSignCustomDialog;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

public class AppPromptRegistrationSignIn {

	private static final boolean DEBUG = false;

	public static final int REGISTRATION_LOGIN_SESSION_FREQUENCY = 4;
	public static final int REGISTRATION_LOGIN_POST_SESSION = 2;

	private Context mContext;

	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;

	public AppPromptRegistrationSignIn(Context context) {
		mContext = context;

		mDataManager = DataManager.getInstance(mContext);
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();

	}

	public boolean appLaunched(boolean canPromptForRating) {

		if (canPromptForRating && ratingConditionsHaveBeenMet()) {
			AppPromptConstants constants = new AppPromptConstants(mContext,
					AppPromptConstants.KEY_REGISTRATION_LOGIN);
			RegisterSignCustomDialog alertBuilder = new RegisterSignCustomDialog(
					mContext);
			alertBuilder.setMessage(Utils.getMultilanguageText(mContext,
					constants.getMessage()));

			alertBuilder.setPositiveButton(
					Utils.getMultilanguageText(mContext,
							constants.getPositiveButtonText()),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent startLoginActivityIntent = new Intent(
									mContext, LoginActivity.class);
							startLoginActivityIntent.putExtra(
									HomeActivity.ARGUMENT_HOME_ACTIVITY,
									"home_activity");
							startLoginActivityIntent.putExtra(
									LoginActivity.FLURRY_SOURCE,
									FlurryConstants.FlurrySourceSection.Home
											.toString());
							startLoginActivityIntent
									.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
							((Activity) mContext).startActivityForResult(
									startLoginActivityIntent,
									HomeActivity.LOGIN_ACTIVITY_CODE);

						}
					});
			alertBuilder.setNegativeButton(constants.getNegativeButtonText(),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			alertBuilder.show();
			return true;
		} else {
			return false;
		}
	}

	private boolean ratingConditionsHaveBeenMet() {
		try {
			if (DEBUG) {
				return true;
			}

			if (!mApplicationConfigurations.isUserRegistered()) {
				String session = mApplicationConfigurations.getSessionID();
				boolean isRealUser = mApplicationConfigurations.isRealUser();
				if (!TextUtils.isEmpty(session) && isRealUser) {
					mApplicationConfigurations.setIsUserRegistered(true);
					return false;
				} else {
					if (REGISTRATION_LOGIN_POST_SESSION < mApplicationConfigurations
							.getTotalSession()
							&& mApplicationConfigurations.getTotalSession() < REGISTRATION_LOGIN_SESSION_FREQUENCY) {
						Logger.s("------show popup "
								+ mApplicationConfigurations.getTotalSession());
						mApplicationConfigurations
								.setLastSessionRegister_1(mApplicationConfigurations
										.getTotalSession());
						mApplicationConfigurations
								.setLastSessionSocialLogin2(mApplicationConfigurations
										.getTotalSession());
						return true;
					} else if ((mApplicationConfigurations
							.getLastSessionRegister_1() + REGISTRATION_LOGIN_SESSION_FREQUENCY) == mApplicationConfigurations
							.getTotalSession()) {
						Logger.s("------show popup else "
								+ mApplicationConfigurations.getTotalSession());
						mApplicationConfigurations
								.setLastSessionRegister_1(mApplicationConfigurations
										.getTotalSession());
						return true;
					}
				}
			}

		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return false;
	}

}
