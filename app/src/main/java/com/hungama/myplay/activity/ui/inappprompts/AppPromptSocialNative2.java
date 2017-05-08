package com.hungama.myplay.activity.ui.inappprompts;

// Idan1
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;

import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.gigya.GigyaManager;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.LoginActivity;
import com.hungama.myplay.activity.ui.dialogs.RegisterSignCustomDialog;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

public class AppPromptSocialNative2 {

	private static final boolean DEBUG = false;

	public static final int REGISTRATION_LOGIN_SESSION_FREQUENCY = 5;
	public static final int REGISTRATION_LOGIN_POST_SESSION = 2;

	private Context mContext;

	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	private GigyaManager mGigyaManager;

	public AppPromptSocialNative2(Context context) {
		mContext = context;

		mGigyaManager = new GigyaManager(((Activity) mContext));
		mDataManager = DataManager.getInstance(mContext);
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();

	}

	public boolean appLaunched(boolean canPromptForRating) {

		if (canPromptForRating && ratingConditionsHaveBeenMet()) {
			RegisterSignCustomDialog alertBuilder = new RegisterSignCustomDialog(
					mContext);
			AppPromptConstants constants;
			if (mApplicationConfigurations.isuserLoggedIn())
				constants = new AppPromptConstants(mContext,
						AppPromptConstants.KEY_SOCIAL_SIGNIN_WITH_LOGIN);
			else
				constants = new AppPromptConstants(mContext,
						AppPromptConstants.KEY_SOCIAL_SIGNIN_WITHOUT_LOGIN);

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
			Logger.s("Appprompt :::::: 1 ");
			if (!mGigyaManager.isFBConnected() && !mGigyaManager.isTwitterConnected() && !mGigyaManager.isGoogleConnected() &&
					TextUtils.isEmpty(mApplicationConfigurations.getGigyaFBEmail()) &&
					TextUtils.isEmpty(mApplicationConfigurations.getGigyaTwitterEmail()) &&
					TextUtils.isEmpty(mApplicationConfigurations.getGigyaGoogleEmail())) {
				Logger.s("Appprompt :::::: 2 ");
//				if(true)
//					return true;

				if (REGISTRATION_LOGIN_POST_SESSION < mApplicationConfigurations
						.getTotalSession()
						&& mApplicationConfigurations.getTotalSession() < REGISTRATION_LOGIN_SESSION_FREQUENCY
						&& mApplicationConfigurations
								.getLastSessionSocialLogin2() < REGISTRATION_LOGIN_POST_SESSION) {
					Logger.s("------show popup "
							+ mApplicationConfigurations.getTotalSession());
					mApplicationConfigurations
							.setLastSessionSocialLogin2(mApplicationConfigurations
									.getTotalSession());
					if (mApplicationConfigurations.isUserRegistered())
						return true;
				} else if ((mApplicationConfigurations
						.getLastSessionSocialLogin2() + REGISTRATION_LOGIN_SESSION_FREQUENCY) == mApplicationConfigurations
						.getTotalSession()) {
					Logger.s("------show popup else "
							+ mApplicationConfigurations.getTotalSession());
					mApplicationConfigurations
							.setLastSessionSocialLogin2(mApplicationConfigurations
									.getTotalSession());
					if (mApplicationConfigurations.isUserRegistered())
						return true;
				}
			}

		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return false;
	}

}
