package com.hungama.myplay.activity.data.configurations;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class ServerConfigurations {

	// "affiliate_id":960,"app_code":"MYPLAY-ANDROID-GIONEE","package_name":"com.hungama.myplay_gionee.activity","oem":"GIONEE","offer_base":"tac"}

	/*
	 * Definition of CM server configurations.
	 */
	private final String mServerUrl;
	private final String mServerVersion;
	private final String mLcId;
	private final String mPartnerId;
	private final String mWebServiceVersion;
	private final String mAPI;
	private final String mAppVersion;
	private String mAppCode;
	private final String mFormat;
	private String mReferralId;
	private final String mVersion;

	/*
	 * Definition of Hungama servers configurations.
	 */
	// private final String mHungamaServerUrl;
	private final String mHungamaServerUrl_2;
	private final String mHungamaPayUrl;
	private final String mHungamaSocialServerUrl;
	private final String mHungamaSocialServerUrl_2;
	private final String mHungamaSubscriptionServerUrl;
	// private final String mHungamaMobileVerificationServerUrl;
	private final String mHungamaDownloadServerUrl;
	private final String mHungamaAuthKey;
	private final String mHungamaVersionCheckServerUrl;
	private final String mHungamaRedeemCouponServerUrl;
	private final String mHungamaUserProfileServerUrl;

	private static ServerConfigurations sIntance;

	public static final synchronized ServerConfigurations getInstance(
			Context applicationContext) {
		if (sIntance == null) {
			if (applicationContext != null)
				sIntance = new ServerConfigurations(
						applicationContext.getApplicationContext());
		}
		return sIntance;
	}
	public static  synchronized void destroy()
	{
		sIntance=null;
	}
	private ServerConfigurations(Context context) {
		/*
		 * CM initialization.
		 */

		SharedPreferences pref = context.getSharedPreferences("oem",
				Context.MODE_PRIVATE);

		mServerVersion = context.getResources().getString(
				R.string.web_service_url_version);
		mLcId = context.getResources().getString(R.string.lc_id);

		mPartnerId = context.getResources().getString(R.string.partner_id);

		mWebServiceVersion = context.getResources().getString(R.string.ws_ver);
		mAPI = context.getResources().getString(R.string.api);
		mAppVersion = DataManager.getVersionName(context);

		// mAppCode = context.getResources().getString(R.string.app_code);
		mAppCode = pref.getString("app_code",
				context.getResources().getString(R.string.app_code));

		mFormat = context.getResources().getString(R.string.format);

		mReferralId = pref.getString("affiliateId", context.getResources()
				.getString(R.string.referal_id));

		mVersion = context.getResources().getString(R.string.version);

		String serviceUrl = context.getResources().getString(
				R.string.web_service_url);
		mServerUrl = "http://" + serviceUrl + "/web_services/apps/"
				+ mServerVersion + "/jsonrpc/";

		/*
		 * Hungama initialization.
		 */
		// mHungamaServerUrl = context.getResources().getString(
		// R.string.hungama_server_url);

		mHungamaServerUrl_2 = context.getResources().getString(
				R.string.hungama_server_url_new);
		// mHungamaServerUrl_2 =
		// context.getResources().getString(R.string.hungama_server_url);

		mHungamaSocialServerUrl = context.getResources().getString(
				R.string.hungama_social_server_url);
		mHungamaSocialServerUrl_2 = context.getResources().getString(
				R.string.hungama_social_server_url_new);

		mHungamaSubscriptionServerUrl = context.getResources().getString(
				R.string.hungama_subscription_server_url);
		// mHungamaMobileVerificationServerUrl =
		// context.getResources().getString(R.string.hungama_mobile_verification_server_url);
		mHungamaDownloadServerUrl = context.getResources().getString(
				R.string.hungama_download_server_url_v2);
		mHungamaAuthKey = Utils.toMD5(context.getResources().getString(
				R.string.hungama_auth_key));
		mHungamaVersionCheckServerUrl = context.getResources().getString(
				R.string.hungama_version_check_server_url);
		mHungamaRedeemCouponServerUrl = context.getResources().getString(
				R.string.hungama_server_url_coupon_redeem);
		mHungamaUserProfileServerUrl = context.getResources().getString(
				R.string.hungama_server_url_user_profile);
		mHungamaPayUrl = context.getResources().getString(
				R.string.hungama_pay_url);
	}

	public static final String LC_ID = "lc_id";
	public static final String PARTNER_ID = "partner_id";
	public static final String WEB_SERVER_VERSION = "ws_ver";
	public static final String API = "api";
	public static final String APPLICATION_VERSION = "app_ver";
	public static final String APPLICATION_CODE = "app_code";
	public static final String FORMAT = "format";
	public static final String VERSION = "version";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";

	public String getServerUrl() {
		return mServerUrl;
	}

	public String getServerVersion() {
		return mServerVersion;
	}

	public String getLcId() {
		return mLcId;
	}

	public String getPartnerId() {
		return mPartnerId;
	}

	public String getWebServiceVersion() {
		return mWebServiceVersion;
	}

	public String getAPI() {
		return mAPI;
	}

	public String getAppVersion() {
		return mAppVersion;
	}

	public String getAppCode() {
		return mAppCode;
	}

	public String getFormat() {
		return mFormat;
	}

	public String getReferralId() {
		return mReferralId;
	}

	public String getmVersion() {
		return mVersion;
	}

	// ======================================================
	// Hungama Server Configurations.
	// ======================================================

	// public String getHungamaServerUrl() {
	// return mHungamaServerUrl;
	// }

	public String getHungamaServerUrl_2() {
		return mHungamaServerUrl_2;
	}

	public String getHungamaSocialServerUrl() {
		return mHungamaSocialServerUrl;
	}

	public String getHungamaSocialServerUrl_2() {
		return mHungamaSocialServerUrl_2;
	}

	public String getHungamaSubscriptionServerUrl() {
		return mHungamaSubscriptionServerUrl;
	}

	// public String getHungamaMobileVerificationServerUrl() {
	// return mHungamaMobileVerificationServerUrl;
	// }

	public String getHungamaDownloadServerUrl() {
		return mHungamaDownloadServerUrl;
	}

	public String getHungamaAuthKey() {
		return mHungamaAuthKey;
	}

	public String getmHungamaVersionCheckServerUrl() {
		return mHungamaVersionCheckServerUrl;
	}

	public String getHungamaCouponRedeemServerUrl() {
		return mHungamaRedeemCouponServerUrl;
	}

	public String getHungamaUserProfileServerUrl() {
		return mHungamaUserProfileServerUrl;
	}

	public String getHungamaPayUrl() {
		return mHungamaPayUrl;
	}

	public String parseDeviceOfferJSON(String response, Context context) throws JSONException {
		Logger.i("ServerConfiguration", "TrackIMEI>>>"
				+ response);
		String message=null;
		try {
			JSONObject jsonObject = new JSONObject(response);

			SharedPreferences pref = context.getSharedPreferences("oem",
					Context.MODE_PRIVATE);

			if (jsonObject.getInt("code") == 200) {
				pref.edit().putString("message", jsonObject.getString("message")).commit();
				pref.edit().putString("oem", jsonObject.getString("oem")).commit();
				if(TextUtils.isEmpty(jsonObject.getString("app_code"))) {
					pref.edit().putString("app_code", context.getResources().getString(R.string.app_code)).commit();
				} else {
					pref.edit().putString("app_code", jsonObject.getString("app_code")).commit();
				}
				if(TextUtils.isEmpty(jsonObject.getString("affiliateId"))) {
					pref.edit().putString("affiliateId", context.getResources().getString(
							R.string.affiliate_id)).commit();
				} else {
					pref.edit().putString("affiliateId", jsonObject.getString("affiliate_id")).commit();
				}
				if(TextUtils.isEmpty(jsonObject.getString("package_name"))) {
					pref.edit().putString("oemPackageName", context.getResources().getString(
							R.string.oem_package_name)).commit();
				} else {
					pref.edit().putString("oemPackageName", jsonObject.getString("package_name")).commit();
				}
				message=jsonObject.getString("message");
				Logger.i("ServerConfiguration",
						"TrackIMEI>>>Response >>Fields set");
			} else if (jsonObject.getInt("code") == 100) {
				pref.edit().putString("oem", jsonObject.getString("oem")).commit();
				if(TextUtils.isEmpty(jsonObject.getString("app_code"))) {
					pref.edit().putString("app_code", context.getResources().getString(R.string.app_code)).commit();
				} else {
					pref.edit().putString("app_code", jsonObject.getString("app_code")).commit();
				}
				if(TextUtils.isEmpty(jsonObject.getString("affiliateId"))) {
					pref.edit().putString("affiliateId", context.getResources().getString(
							R.string.affiliate_id)).commit();
				} else {
					pref.edit().putString("affiliateId", jsonObject.getString("affiliate_id")).commit();
				}
				if(TextUtils.isEmpty(jsonObject.getString("package_name"))) {
					pref.edit().putString("oemPackageName", context.getResources().getString(
							R.string.oem_package_name)).commit();
				} else {
					pref.edit().putString("oemPackageName", jsonObject.getString("package_name")).commit();
				}
			} else {
				pref.edit().putString("message", null).commit();
				pref.edit().putString("app_code", context.getResources().getString(R.string.app_code)).commit();
				pref.edit().putString("affiliateId", context.getResources().getString(
						R.string.affiliate_id)).commit();
				pref.edit().putString("oemPackageName", context.getResources().getString(
						R.string.oem_package_name)).commit();
 			}
			mAppCode = pref.getString("app_code",
					context.getResources().getString(R.string.app_code));
			mReferralId = pref.getString("affiliateId", context.getResources()
					.getString(R.string.referal_id));
		} catch (Exception e) {
 		}
		return message;
	}
}
