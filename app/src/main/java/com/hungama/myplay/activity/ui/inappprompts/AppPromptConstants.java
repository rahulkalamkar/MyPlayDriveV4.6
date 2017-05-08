package com.hungama.myplay.activity.ui.inappprompts;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.hungama.myplay.activity.data.CacheManager;

public class AppPromptConstants {

	// private final String REGISTRATION_LOGIN_MESSAGE =
	// "Register Now, earn 200 Coins!\nBONUS: Get a FREE Recharge redemption coupon worth INR 10/-.";
	// private final String REGISTRATION_LOGIN_BUTTON_POSITIVE = "REGISTER NOW";
	// private final String REGISTRATION_LOGIN_BUTTON_NEGATIVE = "Cancel";

	public static final String KEY_REGISTRATION_LOGIN = "registration_login";
	public static final String KEY_SOCIAL_SIGNIN_WITHOUT_LOGIN = "social_signin_without_login";
	public static final String KEY_SOCIAL_SIGNIN_WITH_LOGIN = "social_signin_with_login";
	public static final String KEY_CATEGORY_PREF_SELECTION_GENERIC = "category_pref_selection_generic";
	public static final String KEY_OFFLINE_CACHING_3RD_SONG = "offline_caching_3rd_song";
	public static final String KEY_OFFLINE_CACHING_TRIAL_OFFER_EXPIRED = "offline_caching_trial_offer_expired";

	private String mMessage = "";
	private String mButtonPositive = "";
	private String mButtonNegative = "";

	public AppPromptConstants(Context context, String key) {
		CacheManager mCacheManager = new CacheManager(context);
		String response = mCacheManager.getInAppPromptResponse();
		if (!TextUtils.isEmpty(response)) {
			try {
				JSONObject jsonResponse = new JSONObject(response);
				JSONObject jsonObject = jsonResponse.getJSONObject(key);
				mMessage = jsonObject.getString("message");
				mMessage = mMessage.replaceAll("\\\\n", "\\\n");
				mButtonPositive = jsonObject.getString("positive_button");
				mButtonNegative = jsonObject.getString("negative_button");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if (TextUtils.isEmpty(mMessage)) {
			response = readFileFromAssets(context);
			if (response != null) {
				try {
					JSONObject jsonResponse = new JSONObject(response);
					JSONObject jsonObject = jsonResponse.getJSONObject(key);
					mMessage = jsonObject.getString("message");
					mButtonPositive = jsonObject.getString("positive_button");
					mButtonNegative = jsonObject.getString("negative_button");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public String getMessage() {
		return mMessage;
	}

	public String getPositiveButtonText() {
		return mButtonPositive;
	}

	public String getNegativeButtonText() {
		return mButtonNegative;
	}

	private String readFileFromAssets(Context context) {
		try {
			InputStream input = context.getAssets().open("InAppPrompts.json");

			int size = input.available();
			byte[] buffer = new byte[size];
			input.read(buffer);
			input.close();

			// byte buffer into a string
			String text = new String(buffer);
			return text;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
