package com.hungama.myplay.activity.ui.inappprompts;

// Idan1
import android.content.Context;

import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.ui.widgets.CustomAlertDialog;
import com.hungama.myplay.activity.util.Logger;

public class AppPromptCategoryPrefSelectionGeneric {

	private static final boolean DEBUG = false;

	// public static final int REGISTRATION_LOGIN_SESSION_FREQUENCY = 10;

	private Context mContext;

	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;

	public AppPromptCategoryPrefSelectionGeneric(Context context) {
		mContext = context;

		mDataManager = DataManager.getInstance(mContext);
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();

	}

	public boolean appLaunched(boolean canPromptForRating) {

		if (canPromptForRating && ratingConditionsHaveBeenMet()) {
			CustomAlertDialog prompt = new CustomAlertDialog(mContext);
			AppPromptConstants constants = new AppPromptConstants(mContext,
					AppPromptConstants.KEY_CATEGORY_PREF_SELECTION_GENERIC);
			prompt.setMessage(constants.getMessage());
			prompt.setNegativeButton(constants.getNegativeButtonText(), null);
			prompt.show();
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

			if (mApplicationConfigurations.getCategoryPrefSelectionGeneric6()) {
				return false;
			}

			/*
			 * if (<mApplicationConfigurations.getTotalSession() &&
			 * mApplicationConfigurations.getTotalSession() <
			 * REGISTRATION_LOGIN_SESSION_FREQUENCY &&
			 * mApplicationConfigurations
			 * .getLastSessionSocialLogin2()<REGISTRATION_LOGIN_POST_SESSION) {
			 * System.out.println("------show popup " +
			 * mApplicationConfigurations.getTotalSession());
			 * mApplicationConfigurations
			 * .setLastSessionSocialLogin2(mApplicationConfigurations
			 * .getTotalSession()); return true; } else
			 */if (mApplicationConfigurations.getTotalSession() % 10 == 0
					&& mApplicationConfigurations.getTotalSession() > 9
					&& mApplicationConfigurations.getTotalSession() <= 31) {
				Logger.s("------show popup else "
						+ mApplicationConfigurations.getTotalSession());
				return true;
			}
			// }

		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return false;
	}

}
