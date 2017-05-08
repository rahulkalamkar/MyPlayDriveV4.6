package com.hungama.myplay.activity.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.widget.Toast;

import com.apsalar.sdk.Apsalar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.HungamaApplication;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.ThreadPoolManager;
import com.hungama.myplay.activity.data.CacheManager;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.MoveDownloadedTracksService;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.campaigns.PlacementType;
import com.hungama.myplay.activity.data.dao.hungama.LiveStation;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaItemsResponse;
import com.hungama.myplay.activity.data.dao.hungama.MediaItemsResponseCatalog;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.MusicCategoriesResponse;
import com.hungama.myplay.activity.data.dao.hungama.SocialNetwork;
import com.hungama.myplay.activity.data.dao.hungama.social.Profile;
import com.hungama.myplay.activity.data.events.AppEvent;
import com.hungama.myplay.activity.gigya.FBFriend;
import com.hungama.myplay.activity.gigya.GigyaManager;
import com.hungama.myplay.activity.gigya.GoogleFriend;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MediaCategoriesOperation;
import com.hungama.myplay.activity.operations.hungama.RadioLiveStationsOperation;
import com.hungama.myplay.activity.operations.hungama.RadioTopArtistsOperation;
import com.hungama.myplay.activity.operations.hungama.SocialProfileOperation;
import com.hungama.myplay.activity.services.CampaignsPreferchingService;
import com.hungama.myplay.activity.services.ReloadTracksDataService;
import com.hungama.myplay.activity.services.SubscriptionService;
import com.hungama.myplay.activity.ui.adapters.ComboMediaItem;
import com.hungama.myplay.activity.ui.dialogs.MyProgressDialog;
import com.hungama.myplay.activity.ui.fragments.AccountSettingsFragment.ObjLanguagePackage;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.ApsalarEvent;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;
import com.reverie.lm.LM;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.urbanairship.UAirship;
import com.urbanairship.google.PlayServicesUtils;

import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * Invisible controller that delegates to other activities based on the
 * application credential's states.
 */
public class OnApplicationStartsActivity extends ActionBarActivity implements
		CommunicationOperationListener {

	private static final String TAG = "OnApplicationStartsActivity";

	private static final int RESULT_ACTIVITY_CODE_SLPASH_SCREEN = 1;
	private static final int RESULT_ACTIVITY_CODE_REPLACEMENTS = 2;
	private static final int RESULT_ACTIVITY_CODE_LOGIN = 3;
	private static final int RESULT_ACTIVITY_CODE_GOOGLE_PLAY_SERVICE = 4;

	public static final String ARGUMENT_ON_APPLICATION_START_ACTIVITY = "argument_on_application_start_activity";

	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;

//	private SharedPreferences pref;
	public static String networkCheckResponse = null;

	private boolean hasRunBefore;

	public static boolean needToShowLyricsForSession = false;
	public static boolean needToShowTriviaForSession = false;
	public static long AppOpenningTime = 0;
	public static boolean needToStartApsalrSession = false;

	private boolean permissionsAllowed;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Logger.s(System.currentTimeMillis()
				+ " :::::::::::::Stratup::::::::::::: " + getClass().getName());
		super.onCreate(savedInstanceState);
		HungamaApplication.isAppReCreated = false;
		/*try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		// if (Logger.enableLanguageLibrary) {
//		if (Logger.enableLanguageLibraryThread) {
//			Utils.startReverieSDK(getApplicationContext());
//		} else if (Logger.enableLanguageLibrary) {
//			new LM(this).RegisterSDK(HomeActivity.SDK_ID);
//		}
		MainActivity.isUserPreferenceLoaded = false;

//		pref = getSharedPreferences("oem", MODE_PRIVATE);

		// ActionBar actionBar = getSupportActionBar();
		// actionBar.hide();

		mDataManager = DataManager.getInstance(getApplicationContext());

		// sets the log level of the library.
//		int logLevel = Log.ERROR;
//		if (BuildConfig.DEBUG) {
//			logLevel = Log.VERBOSE;
//		}

		try {
			Logger.s(" ::::::::::::::: isTaskRoot() :: " + isTaskRoot());
			if (!isTaskRoot()) {
				finish();
				return;
			}
		} catch (Exception e){
			Logger.printStackTrace(e);
		} catch (Error e){
			Logger.printStackTrace(e);
		}
//		ScreenLockStatus.getInstance(getApplicationContext()).dontShowAd();
		mApplicationConfigurations = ApplicationConfigurations
				.getInstance(getApplicationContext());

		mApplicationConfigurations.setDefaultUserAgent(mDataManager.getDeviceConfigurations().getDefaultUserAgentString
				(OnApplicationStartsActivity.this, handle));

		try {
			int playServiceResult = GooglePlayServicesUtil
					.isGooglePlayServicesAvailable(this);
			if (playServiceResult == ConnectionResult.SUCCESS
					&& ApsalarEvent.ENABLED) {
//				AdXConnect.setKey(getString(R.string.adx_app_secret_key));
//				AdXConnect.getAdXConnectInstance(getApplicationContext(),
//						false, logLevel);
				if(Logger.isMicromaxOEMVersion && !mApplicationConfigurations.getApsalarAttributionStatus()){
					doApsalarAttributionApiCall();
					needToStartApsalrSession = true;
				} else {
					Apsalar.startSession(getApplicationContext(), getString(R.string.apsalar_api_key), getString(R.string.apsalar_secret));
					needToStartApsalrSession = false;
				}
//				Apsalar.registerReceiver(getApplicationContext());
			} else {
				GooglePlayServicesUtil.getErrorDialog(playServiceResult, this,
						RESULT_ACTIVITY_CODE_GOOGLE_PLAY_SERVICE);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		} catch (Error e) {
			Logger.printStackTrace(e);
		}

		permissionsAllowed = checkPermissions();
		if(permissionsAllowed) {
			init();
			catchAllData();
			gigyauserinfo();
		}
	}

    private void init(){
		if (Logger.enableLanguageLibraryThread) {
			Utils.startReverieSDK(getApplicationContext());
		} else if (Logger.enableLanguageLibrary) {
			new LM(this).RegisterSDK(HomeActivity.SDK_ID);
		}

		if(!mApplicationConfigurations.isFirstVisitToApp()) {
			String sessionId = mApplicationConfigurations.getSessionID();
			String passkey = mApplicationConfigurations.getPasskey();
			if (sessionId == null
					|| (sessionId != null && (sessionId.length() == 0
					|| sessionId.equalsIgnoreCase("null") || sessionId
					.equalsIgnoreCase("none")))) {
				try {
					if (passkey == null
							|| (passkey != null && (passkey.length() == 0
							|| passkey.equalsIgnoreCase("null") || passkey
							.equalsIgnoreCase("none")))) {
						getIntent().removeExtra("skip_ad");
						mApplicationConfigurations.setIsFirstVisitToApp(true);
						mDataManager.setmIsTimeReadAlreadyCalled(false);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

        hasRunBefore = !mApplicationConfigurations
                .isFirstVisitToApp();

		if(!hasRunBefore){
			UAirship.shared().getPushManager()
					.setUserNotificationsEnabled(true);
		}

        if (!getIntent().getBooleanExtra("skip_ad", false)) {
            HungamaApplication.splashAdDisplyedCount = 0;
            if (mApplicationConfigurations.needToShowTrivia()) {
                needToShowTriviaForSession = true;
            } else {
                needToShowTriviaForSession = false;
            }
            if (mApplicationConfigurations.needToShowLyrics()) {
                needToShowLyricsForSession = true;
                AppOpenningTime = System.currentTimeMillis();
            } else {
                needToShowLyricsForSession = false;
                AppOpenningTime = 0;
            }
            mApplicationConfigurations.resetVideoPlayBackCounter();
			try{
				int consumerId = mApplicationConfigurations
						.getConsumerID();
				String deviceId = mApplicationConfigurations
						.getDeviceID();
				String timeStamp = mDataManager.getDeviceConfigurations()
						.getTimeStampDelta();
				AppEvent campaignPlayEvent = new AppEvent(
						consumerId, deviceId, "start", timeStamp, 0, 0, null);
				mDataManager.addEvent(campaignPlayEvent);
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
        }
        ApsalarEvent.postEvent(getApplicationContext(), ApsalarEvent.APP_LAUNCH_EVENT);

        String versionName;
        try {
			Set<String> tags1 = Utils.getTags();
			boolean tagExist = false;
			for (String tag : tags1){
				if (tag.startsWith("pref_display_")){
					tagExist = true;
					break;
				}
			}
			if (!tagExist) {
				tags1.add("pref_display_English");
				Utils.AddTag(tags1);
			}

            versionName = getPackageManager().getPackageInfo(getPackageName(),
                    0).versionName;
            if (!TextUtils.isEmpty(mApplicationConfigurations
                    .getApplicationVersion())
                    && !mApplicationConfigurations.getApplicationVersion()
                    .equals(versionName)) {
                if(!mApplicationConfigurations.isRealUser() || TextUtils.isEmpty(mApplicationConfigurations.getSilentPartnerUserId())
                        || TextUtils.isEmpty(mApplicationConfigurations.getSilentUserSessionID())
                        || TextUtils.isEmpty(mApplicationConfigurations.getSilentUserPasskey())){
                    mApplicationConfigurations.setSilentUserLoginPhoneNumber(mApplicationConfigurations.getUserLoginPhoneNumber());
                    mApplicationConfigurations.setSilentPartnerUserId(mApplicationConfigurations.getPartnerUserId());
                    mApplicationConfigurations
                            .setSilentUserGigyaSessionToken(mApplicationConfigurations
                                    .getGigyaSessionToken());
                    mApplicationConfigurations
                            .setSilentUserGigyaSessionSecret(mApplicationConfigurations
                                    .getGigyaSessionSecret());
                    mApplicationConfigurations.setSilentUserSessionID(mApplicationConfigurations.getSessionID());
                    mApplicationConfigurations.setSilentUserHouseholdID(mApplicationConfigurations.getHouseholdID());
                    mApplicationConfigurations.setSilentUserConsumerID(mApplicationConfigurations.getConsumerID());
                    mApplicationConfigurations.setSilentUserPasskey(mApplicationConfigurations.getPasskey());
                    mApplicationConfigurations.setSilentUserSelectedLanguage(mApplicationConfigurations.getUserSelectedLanguage());
                    mApplicationConfigurations.setSilentUserSelectedLanguageText(mApplicationConfigurations.getUserSelectedLanguageText());
                    mApplicationConfigurations.setSilentUserSelctedMusicPreference(mApplicationConfigurations.getSelctedMusicPreference());
                    mApplicationConfigurations.setSilentUserSelctedMusicGenre(mApplicationConfigurations.getSelctedMusicGenre());
                }

                startService(new Intent(OnApplicationStartsActivity.this,
                        ReloadTracksDataService.class));
                // Toast.makeText(this, "ReloadTracksDataService",
                // Toast.LENGTH_SHORT).show();
                startService(new Intent(OnApplicationStartsActivity.this,
                        MoveDownloadedTracksService.class));
            }
        } catch (NameNotFoundException e) {
            Logger.printStackTrace(e);
        }
    }

	private void trackIMEI() {
		URL url;
		try {
			DeviceConfigurations deviceConfig = mDataManager
					.getDeviceConfigurations();
//			ApplicationConfigurations config=ApplicationConfigurations.getInstance(getBaseContext());

			String mHardwareId = deviceConfig.getHardwareId();
			String userAgent = deviceConfig.getDefaultUserAgentString(this,
					handle);
			String macAddress = deviceConfig.getMac(this);
			com.hungama.myplay.activity.util.Logger.i("", "SecuredThread");
			com.hungama.myplay.activity.util.Logger.i(TAG, "trystart--");
//			url = new URL(getString(R.string.hungama_server_url_device_offer)
//					+ HungamaApplication.encodeURL(mHardwareId) + "&mac="
//					+ HungamaApplication.encodeURL(macAddress) + "&user_agent="
//					+ HungamaApplication.encodeURL(userAgent) + "&login=1");

			String gcmtoken=UAirship.shared().getPushManager().getGcmToken();
			if(TextUtils.isEmpty(gcmtoken)) {
				gcmtoken = "";
			}
			url = new URL(getString(R.string.hungama_server_url_device_offer_dev)
					+ HungamaApplication.encodeURL(mHardwareId) + "&mac="
					+ HungamaApplication.encodeURL(macAddress) + "&user_agent="
					+ HungamaApplication.encodeURL(userAgent) + "&login="+(mApplicationConfigurations.isRealUser()?"1":"0")+
					"&app=music&os=android&dt="+gcmtoken+"&dtype="+(Utils.isTablet(this)?"tab":"phone"));


			com.hungama.myplay.activity.util.Logger.i(TAG,
					"URL fetched-" + url.toString());
			if(Logger.enableOkHTTP){
				OkHttpClient client = CommunicationManager.getUnsafeOkHttpClient();
//				Request.Builder requestBuilder = new Request.Builder();
//				requestBuilder.url(url);
				Request.Builder requestBuilder = CommunicationManager.getRequestBuilder(OnApplicationStartsActivity.this, url);
				com.squareup.okhttp.Response responseOk = client.newCall(requestBuilder.build()).execute();
				if (responseOk.code() == HttpURLConnection.HTTP_OK) {
//					parseJSON(responseOk.body().string());
					ServerConfigurations.getInstance(OnApplicationStartsActivity.this)
							.parseDeviceOfferJSON(responseOk.body().string(), OnApplicationStartsActivity.this);
				}
			} else {
				HttpURLConnection urlConnection = (HttpURLConnection) url
						.openConnection();
				if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					InputStream in = new BufferedInputStream(
							urlConnection.getInputStream());
					StringBuilder sb = new StringBuilder();
					int ch = -1;
					while ((ch = in.read()) != -1) {
						sb.append((char) ch);
					}
//					parseJSON(sb.toString());
					ServerConfigurations.getInstance(OnApplicationStartsActivity.this)
							.parseDeviceOfferJSON(sb.toString(), OnApplicationStartsActivity.this);
					if (in != null)
						in.close();
				}
				if (urlConnection != null)
					urlConnection.disconnect();
				urlConnection = null;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			Logger.i("Error-response-", "" + e);
		} catch (IOException e) {
			e.printStackTrace();
			Logger.i("Error-response-", "" + e);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.i("Error-response-", "" + e);
		}
	}

	private Handler handle = new Handler();

	// xtpl
//	private void setOem(String oem) {
//		pref.edit().putString("oem", oem).commit();
//	}
//
//	private void setMessage(String message) {
//		pref.edit().putString("message", message).commit();
//	}
//
//	public void setAppCode(String appCode) {
//		pref.edit().putString("app_code", appCode).commit();
//	}
//
//	public void setAffiliateId(String affiliateId) {
//		pref.edit().putString("affiliateId", affiliateId).commit();
//	}
//
//	public void setOemPackageName(String oemPackageName) {
//		pref.edit().putString("oemPackageName", oemPackageName).commit();
//	}
//
//	private JSONObject jsonObject;
//
//	private String parseJSON(String responce) throws JSONException {
//		com.hungama.myplay.activity.util.Logger.i(TAG, "TrackIMEI>>>"
//				+ responce);
//		try {
//			jsonObject = new JSONObject(responce);
//
//			if (jsonObject.getInt("code") == 200) {
//				setMessage(jsonObject.getString("message"));
//				// message=jsonObject.getString("message");
//				// conObject = new JSONObject(responce);
//				// oem=jsonObject.getString("oem");
//				setOem(jsonObject.getString("oem"));
//				setAppCode(jsonObject.getString("app_code"));
//				setAffiliateId(jsonObject.getString("affiliate_id"));
//				setOemPackageName(jsonObject.getString("package_name"));
//				mDataManager.resetConfig(this);
//				// appCode=jsonObject.getString("app_code");
//				// affiliateId=jsonObject.getString("affiliate_id");
//				// oemPackageName=jsonObject.getString("package_name");
//				com.hungama.myplay.activity.util.Logger.i(TAG,
//						"TrackIMEI>>>Response >>Fields set");
//			} else if (jsonObject.getInt("code") == 100) {
//				setOem(jsonObject.getString("oem"));
//				setAppCode(jsonObject.getString("app_code"));
//				setAffiliateId(jsonObject.getString("affiliate_id"));
//				setOemPackageName(jsonObject.getString("package_name"));
//				mDataManager.resetConfig(this);
//			} else {
//				setMessage(null);
//				setOemPackageName(this.getResources().getString(
//						R.string.oem_package_name));
//				setAffiliateId(this.getResources().getString(
//						R.string.affiliate_id));
//				setAppCode(this.getResources().getString(R.string.app_code));
//				responce = null;
//			}
//		} catch (Exception e) {
//			responce = null;
//		}
//		return responce;
//	}

	// @TargetApi(17)
	static class NewApiWrapper {
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
		static String getDefaultUserAgent(Context context) {
			return WebSettings.getDefaultUserAgent(context);
		}
	}

	private MyProgressDialog pd;

	public class Loadit implements Runnable {

		public void run() {
//			runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					onPreExecute();
//				}
//			});
			doInBackground();
		}

//		protected void onPreExecute() {
			// pd = ProgressDialog.show(OnApplicationStartsActivity.this, "",
			// Utils.getMultilanguageText(getApplicationContext(),
			// getString(R.string.loading_)));
//		}

		protected void doInBackground() {
			try {
                if(!hasRunBefore){
                    DBOHandler.importDb(OnApplicationStartsActivity.this);
                    int offlineSongCount = DBOHandler.getAllTracks(OnApplicationStartsActivity.this).size();
                    if (offlineSongCount > 3)
                        offlineSongCount = 3;
                    mApplicationConfigurations.setFreeUserCacheCountFirstTime(offlineSongCount);
                    hasRunBefore = !mApplicationConfigurations
                            .isFirstVisitToApp();
                    if(hasRunBefore) {
                        mApplicationConfigurations.clearAllTimestamps();
                        init();
                    }
                }
				checkNetworkMessage();
				if (!hasRunBefore) {
					if (Utils.isConnected())
						trackIMEI();
					else {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(
										OnApplicationStartsActivity.this,
										R.string.application_error_no_connectivity,
										Toast.LENGTH_SHORT).show();
							}
						});
					}
//                    DBOHandler.importDb(OnApplicationStartsActivity.this);
				}
			} catch (Exception e) {
			}
			onPostExecute();
		}

		protected void onPostExecute() {
			try {
				// Initializes internal application components.
				mDataManager.notifyApplicationStarts();

				// starts prefetching the discover moods.
				// mDataManager.prefetchMoodsIfNotExists();

				String versionName = getPackageManager().getPackageInfo(
						getPackageName(), 0).versionName;
				if (!mApplicationConfigurations.getApplicationVersion().equals(
						versionName)) {
					mApplicationConfigurations
							.setApplicationVersion(versionName);
					mApplicationConfigurations
							.setHasSuccessedPrefetchingApplicationImages(false);
					mApplicationConfigurations
							.setIsLanguageSupportedForWidget(mApplicationConfigurations
									.getUserSelectedLanguage());
					// startService(new Intent(OnApplicationStartsActivity.this,
					// ReloadTracksDataService.class));
				}

				// mDataManager.prefetchImagesIfNotExists();
			} catch (Exception e) {
			}

			try {
				Set<String> tags = Utils.getTags();

				boolean needUpdate = false;
				if (!tags.contains("Android")) {

					needUpdate = true;
					// Device type
					String tagVal = "Android";
					tags.add(tagVal);
					Logger.e(TAG, "tag set-->" + tagVal);

					// Language
					Locale l = Locale.getDefault();
					tagVal = "language_" + l.getISO3Language().toLowerCase();
					tags.add(tagVal);
					Logger.e(TAG, "tag set-->" + tagVal);

					// timezone
					TimeZone zone = TimeZone.getDefault();
					tagVal = "timezone_"
							+ zone.getDisplayName(false, TimeZone.SHORT);
					tags.add(tagVal);
					Logger.e(TAG, "tag set-->" + tagVal);

					if (getResources().getBoolean(R.bool.isTablet))
						tagVal = "Tablet";
					else
						tagVal = "Phone";
					tags.add(tagVal);
					Logger.e(TAG, "tag set-->" + tagVal);

				}

				SharedPreferences pref = getSharedPreferences("tabset",
						MODE_PRIVATE);
				if (!pref.getBoolean("is_country_tag_set", false)) {
					String country = GetCountry();
					if (country != null) {
						// country
						needUpdate = true;
						tags.add("country_" + country.toLowerCase());
						Logger.e(
								TAG,
								"tag set-->" + "country_"
										+ country.toLowerCase());
						pref.edit().putBoolean("is_country_tag_set", true)
								.commit();
					}
					if (state != null) {
						// state
						needUpdate = true;
						tags.add("state_" + state.toLowerCase());
						Logger.e(TAG,
								"tag set-->" + "state_" + state.toLowerCase());
						pref.edit().putBoolean("is_state_tag_set", true)
								.commit();
					}
					if (city != null) {
						// city
						needUpdate = true;
						tags.add("city_" + city.toLowerCase());
						Logger.e(TAG,
								"tag set-->" + "city_" + city.toLowerCase());
						pref.edit().putBoolean("is_city_tag_set", true)
								.commit();
					}
				}

				if (!pref.getBoolean("is_state_tag_set", false)) {
					// String country = GetCountry();
					if (state != null) {
						// state
						needUpdate = true;
						tags.add("state_" + state.toLowerCase());
						Logger.e(TAG,
								"tag set-->" + "state_" + state.toLowerCase());
						pref.edit().putBoolean("is_state_tag_set", true)
								.commit();
					}
				}

				if (!pref.getBoolean("is_city_tag_set", false)) {
					// String country = GetCountry();
					if (city != null) {
						// state
						needUpdate = true;
						tags.add("city_" + city.toLowerCase());
						Logger.e(TAG,
								"tag set-->" + "city_" + city.toLowerCase());
						pref.edit().putBoolean("is_city_tag_set", true)
								.commit();
					}
				}

				if (!pref.getBoolean("device_tag_set", false)) {
					needUpdate = true;
					tags.add("device_" + android.os.Build.MANUFACTURER);
					Logger.e(TAG, "tag set-->" + "device_"
							+ android.os.Build.MANUFACTURER);
					pref.edit().putBoolean("device_tag_set", true).commit();
				}

				String versionName = DataManager
						.getVersionName(OnApplicationStartsActivity.this);
				if (versionName != null) {
					String storedVersion = pref.getString(
							"application_version", null);
					if (storedVersion == null
							|| (storedVersion != null && !versionName
									.equals(storedVersion))) {
						needUpdate = true;
						if (!TextUtils.isEmpty(storedVersion)) {
							// tags.remove("version_" + storedVersion);
							List<String> tagsToRemove = new ArrayList<String>();
							for (String tag : tags) {
								if (tag.startsWith("version_")) {
									tagsToRemove.add(tag);
								}
							}
							for (String tag : tagsToRemove) {
								tags.remove(tag);
							}
						}
						tags.add("version_" + versionName);
						Logger.e(TAG, "tag set-->" + "version_" + versionName);
						pref.edit()
								.putString("application_version", versionName)
								.commit();
					}
				}

				if (needUpdate)
					Utils.AddTag(tags);

				postNewUaTags();
			} catch (Exception e) {
			}

			if (getIntent().getBooleanExtra(AlertActivity.ALERT_MARK, false)) {
				startMainActivity(false);
				finish();
			} else
				continueApplicationFlow();
		}

		private void checkNetworkMessage() {
			ThreadPoolManager.getInstance().submit(new Runnable() {
				URL url;

				public void run() {
					try {
						DeviceConfigurations deviceConfig = mDataManager
								.getDeviceConfigurations();
						String mHardwareId = deviceConfig.getHardwareId();
						// mHardwareId = "354994057744795";
//						String userAgent = deviceConfig.getDefaultUserAgentString(OnApplicationStartsActivity.this,
//								handle);
//						Logger.s("UserAgent :::::::::::::::: " + userAgent);
						url = new URL(
								"http://202.87.41.147/hungamacgbilling/v2/network_check.php?hardware_id="
										+ HungamaApplication
										.encodeURL(mHardwareId)
										+ "&device=android"
						/*
						 * +
						 * "&test=1&country=OM&msisdn=96895010000&ip=5.21.249.246"
						 */);
						com.hungama.myplay.activity.util.Logger.i(TAG,
								"URL fetched-" + url.toString());
						if (Logger.enableOkHTTP) {
							OkHttpClient client = CommunicationManager.getUnsafeOkHttpClient();
//							Request.Builder requestBuilder = new Request.Builder();
//							requestBuilder.url(url);
							Request.Builder requestBuilder = CommunicationManager.getRequestBuilder(OnApplicationStartsActivity.this, url);
							com.squareup.okhttp.Response responseOk = client.newCall(requestBuilder.build()).execute();
							if (responseOk.code() == HttpURLConnection.HTTP_OK) {
								String response = responseOk.body().string();
								Logger.s("Response :::: " + response);
								// Logger.s("Response :::: " + response);
								networkCheckResponse = response;

								try {
									JSONObject json = new JSONObject(response);
									JSONObject jsonResponse = json
											.getJSONObject("response");
									if (jsonResponse.getInt("pro_redirect") == 1) {
										mDataManager.getApplicationConfigurations()
												.checkForProUser(true);
									} else {
										mDataManager.getApplicationConfigurations()
												.checkForProUser(false);
									}
								} catch (Exception e) {
									Logger.printStackTrace(e);
								}
							}
						} else {
							HttpURLConnection urlConnection = (HttpURLConnection) url
									.openConnection();
							if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
								InputStream in = new BufferedInputStream(
										urlConnection.getInputStream());
								StringBuilder sb = new StringBuilder();
								int ch = -1;
								while ((ch = in.read()) != -1) {
									sb.append((char) ch);
								}
								String response = sb.toString();
								Logger.s("Response :::: " + response);
								// Logger.s("Response :::: " + response);
								networkCheckResponse = response;

								try {
									JSONObject json = new JSONObject(response);
									JSONObject jsonResponse = json
											.getJSONObject("response");
									if (jsonResponse.getInt("pro_redirect") == 1) {
										mDataManager.getApplicationConfigurations()
												.checkForProUser(true);
									} else {
										mDataManager.getApplicationConfigurations()
												.checkForProUser(false);
									}
								} catch (Exception e) {
									Logger.printStackTrace(e);
								}
								if (in != null)
									in.close();
							}
							if (urlConnection != null)
								urlConnection.disconnect();
							urlConnection = null;
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
						Logger.i("Error-response-", "" + e);
					} catch (IOException e) {
						e.printStackTrace();
						Logger.i("Error-response-", "" + e);
					} catch (Exception e) {
						e.printStackTrace();
						Logger.i("Error-response-", "" + e);
					}
				}
			});
		}
	}

	@Override
	protected void onDestroy() {
		try {
			if (pd != null && pd.isShowing())
				pd.dismiss();
		} catch (Exception e) {
		} catch (Error e) {
		}
		super.onDestroy();
	}

	private String state = null;
	private String city = null;

	private String GetCountry() {
		LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
		List<String> providers = lm.getProviders(true);

		/*
		 * Loop over the array backwards, and if you get an accurate location,
		 * then break out the loop
		 */
		Location l = null;

		for (int i = providers.size() - 1; i >= 0; i--) {
			l = lm.getLastKnownLocation(providers.get(i));
			if (l != null)
				break;
		}

		if (l != null) {
			Geocoder gc = new Geocoder(getBaseContext());
			try {
				List<Address> addresses = gc.getFromLocation(l.getLatitude(),
						l.getLongitude(), 1);
				if (addresses != null && addresses.size() > 0) {
					Address tmpAddr = addresses.get(0);
					state = tmpAddr.getAdminArea();
					city = tmpAddr.getLocality();
					return tmpAddr.getCountryCode();
				}
			} catch (Exception e) {
			}
		}
		return null;
	}

	@Override
	protected void onStart() {
		super.onStart();

		try {
			// Handle any Google Play services errors
			if (PlayServicesUtils.isGooglePlayStoreAvailable()) {
				PlayServicesUtils.handleAnyPlayServicesError(this);
			}
		} catch (Exception e) {
		} catch (Error e) {
		}

		if (mApplicationConfigurations == null)
			mApplicationConfigurations = ApplicationConfigurations
					.getInstance(this);

		if(permissionsAllowed) {
			if (Utils.isConnected()) {
				mApplicationConfigurations.setSaveOfflineAutoMode(false);
				ThreadPoolManager.getInstance().submit(new Loadit());
			} else {
				if (!mApplicationConfigurations.getSaveOfflineMode())
					mApplicationConfigurations.setSaveOfflineAutoMode(true);
				moveAhead();
			}
		}
		try {
			if (!getIntent().getBooleanExtra("skip_ad", false)) {
				Analytics.startSession(this);
				Analytics.startGASession();
				Analytics.logEvent(FlurryConstants.FlurryEventName.AppOpen
						.toString());
			}
		} catch (Exception e) {
		}

	}

	@Override
	protected void onStop() {
		super.onStop();
		if (!getIntent().getBooleanExtra("skip_ad", false)) {// isGCMIntent
			Analytics.onEndSession(this);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// checks if the retrieved activity is the Splash Screen
		// mDataManager.GetUserLanguageMap(OnApplicationStartsActivity.this);
		if (requestCode == RESULT_ACTIVITY_CODE_SLPASH_SCREEN) {
			if (resultCode == RESULT_OK) {
				Logger.s(System.currentTimeMillis()
						+ " :::::::::::::Stratup:::::::::::::2 "
						+ getClass().getName());
				moveAhead();
			} else {
				finish();
				return;
			}
		}

		// checks if the retrieved activity is the CatchMedia's Replacements.
		if (requestCode == RESULT_ACTIVITY_CODE_REPLACEMENTS
				&& resultCode == RESULT_OK) {
			continueApplicationFlow();
		}

		// checks if the retrieved activity is the Login Activity.
		if (requestCode == OnApplicationStartsActivity.RESULT_ACTIVITY_CODE_LOGIN) {
			if (resultCode == RESULT_OK) {
				Logger.s(System.currentTimeMillis()
						+ " :::::::::::::Stratup:::::::::::::3 "
						+ getClass().getName());
				startMainActivity(true);
			} else {
				finish();
				return;
			}
		}

		if (requestCode == RESULT_ACTIVITY_CODE_GOOGLE_PLAY_SERVICE) {
			int playServiceResult = GooglePlayServicesUtil
					.isGooglePlayServicesAvailable(this);
			if (playServiceResult == ConnectionResult.SUCCESS) {
				// sets the log level of the library.
				// int logLevel = Log.ERROR;
				// if (BuildConfig.DEBUG) {
				// logLevel = Log.VERBOSE;
				// }
				// AdXConnect.getAdXConnectInstance(getApplicationContext(),
				// hasRunBefore, logLevel);
			}
		}
	}

	private boolean isRealUser;

	private void continueApplicationFlow() {
		ApplicationConfigurations applicationConfigurations = mDataManager
				.getApplicationConfigurations();
		String session = applicationConfigurations.getSessionID();
		isRealUser = applicationConfigurations.isRealUser();
		boolean isFirstVisitToApp = applicationConfigurations
				.isFirstVisitToApp();


		if (!isFirstVisitToApp && !TextUtils.isEmpty(session) && isRealUser) {
			startMainActivity(isFirstVisitToApp);
		} else {
			if (isFirstVisitToApp/* || !hasRunBefore*/)
				startLoginActivity();
			else
				startMainActivity(isFirstVisitToApp);
		}
	}

	private void startLoginActivity() {
		Set<String> tags = Utils.getTags();
		if (!tags.contains("not-logged-in")) {
			if (tags.contains("logged-in"))
				tags.remove("logged-in");
			tags.add("not-logged-in");
			Utils.AddTag(tags);
		}
		Logger.s(System.currentTimeMillis()
				+ " :::::::::::::Stratup:::::::::::::4 " + getClass().getName());
		Intent startLoginActivityIntent = new Intent(getApplicationContext(),
				LoginActivity.class);
		startLoginActivityIntent
				.putExtra(
						OnApplicationStartsActivity.ARGUMENT_ON_APPLICATION_START_ACTIVITY,
						"on_application_start_activity");
		startLoginActivityIntent.putExtra(LoginActivity.FLURRY_SOURCE,
				FlurryConstants.FlurryUserStatus.AppLaunch.toString());
		startLoginActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivityForResult(startLoginActivityIntent,
				RESULT_ACTIVITY_CODE_LOGIN);
	}

	private void startMainActivity(boolean StartFirsttimeServices) {
		if (!StartFirsttimeServices) {
			CampaignsPreferchingService.resetPrefTimeCampaign(this);
			Intent campaignsService = new Intent(getApplicationContext(),
					CampaignsPreferchingService.class);
			startService(campaignsService);

//			InventoryLightService.callService(getApplicationContext(), true);

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mDataManager
							.getPreferences(OnApplicationStartsActivity.this);
				}
			});
		}
		mDataManager.prefetchImagesIfNotExists();

		ApplicationConfigurations applicationConfigurations = mDataManager
				.getApplicationConfigurations();
		Set<String> tags = Utils.getTags();
		if (applicationConfigurations.isRealUser()) {
			if (!tags.contains("logged-in")) {
				if (tags.contains("not-logged-in"))
					tags.remove("not-logged-in");

				tags.add("logged-in");
				Utils.AddTag(tags);
			}

		} else {
			if (!tags.contains("not-logged-in")) {
				if (tags.contains("logged-in"))
					tags.remove("logged-in");
				tags.add("not-logged-in");
				Utils.AddTag(tags);
			}
		}

		String tagToAdd = "free-user";
		String tagToRemove = "paid-user";
		if (applicationConfigurations.isUserHasSubscriptionPlan()
				&& !applicationConfigurations.isUserHasTrialSubscriptionPlan()) {
			tagToRemove = "free-user";
			tagToAdd = "paid-user";
		}

		if (!tags.contains(tagToAdd)) {
			if (tags.contains(tagToRemove))
				tags.remove(tagToRemove);
			tags.add(tagToAdd);
			Utils.AddTag(tags);
		}

		if (!applicationConfigurations.isUserHasSubscriptionPlan()) {
			String trialTagToAdd = "";
			String trialTagToRemove = "";
			if (applicationConfigurations.isUserTrialSubscriptionExpired()) {
				trialTagToRemove = "Trial";
				trialTagToAdd = "Trial Expired";
			} else if (applicationConfigurations
					.isUserHasTrialSubscriptionPlan()) {
				trialTagToRemove = "Trial Expired";
				trialTagToAdd = "Trial";
			}
			if (!trialTagToRemove.equals(""))
				if (!tags.contains(trialTagToAdd)) {
					if (tags.contains(trialTagToRemove))
						tags.remove(trialTagToRemove);
					tags.add(trialTagToAdd);
					Utils.AddTag(tags);
				}
		}

		boolean isRealUser = applicationConfigurations.isRealUser();

		if (applicationConfigurations.isRealUser()) {
			if (!tags.contains("registered_user")) {
				if (tags.contains("non_registered_user"))
					tags.remove("non_registered_user");

				tags.add("registered_user");
				Utils.AddTag(tags);
			}
		} else if (!applicationConfigurations.isUserRegistered()) {
			if (!tags.contains("non_registered_user")) {
				if (tags.contains("registered_user"))
					tags.remove("registered_user");

				tags.add("non_registered_user");
				Utils.AddTag(tags);
			}
		}

		// prefetches the subscription plans.
		if (isRealUser) {
			Intent subscriptionPlansService = new Intent(
					getApplicationContext(), SubscriptionService.class);
			startService(subscriptionPlansService);
		}

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mDataManager.getLeftMenu(OnApplicationStartsActivity.this,
						OnApplicationStartsActivity.this, null);
			}
		});

		if (!StartFirsttimeServices
				&& !(isRealUser && (applicationConfigurations
						.isUserHasSubscriptionPlan() && !applicationConfigurations
						.isUserHasTrialSubscriptionPlan()))) {
//			runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					mDataManager
//							.GetUserLanguageMap(OnApplicationStartsActivity.this);
//				}
//			});

			if (SplashScreenActivity.object == null) {
				Intent splashIntent = new Intent(getApplicationContext(),
						SplashScreenActivity.class);
				splashIntent.putExtra("skip_ad",
						getIntent().getBooleanExtra("skip_ad", false));
				splashIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				// overridePendingTransition(0, 0);
				startActivityForResult(splashIntent,
						RESULT_ACTIVITY_CODE_SLPASH_SCREEN);
				Logger.s(System.currentTimeMillis()
						+ " :::::::::::::Stratup:::::::::::::1 "
						+ getClass().getName());
			}
			// startActivity(splashIntent);
		} else
			moveAhead();
	}

	private void moveAhead() {
		if (getIntent().getBooleanExtra("is_deeplink", false)) {
			setResult(RESULT_OK);
		} else {
			Intent startHomeActivityIntent = new Intent(
					getApplicationContext(), HomeActivity.class);// HomeActivity.class
			if (mApplicationConfigurations.getSaveOfflineMode())
				startHomeActivityIntent = new Intent(getApplicationContext(),
						GoOfflineActivity.class);
			else
				startHomeActivityIntent.putExtra("skip_ad", getIntent()
						.getBooleanExtra("skip_ad", false));
			startHomeActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(startHomeActivityIntent);
		}

		if (!getIntent().getBooleanExtra("skip_ad", false)) {
			mApplicationConfigurations
					.setTotalSession(mApplicationConfigurations
							.getTotalSession() + 1);
		}
		mApplicationConfigurations.setAppStartTimestamp(System
				.currentTimeMillis());
		finish();
		return;
	}

	@Override
	public void onStart(int operationId) {
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			switch (operationId) {
			case (OperationDefinition.Hungama.OperationId.LANGUAGE_SETTINGS_GET): {
				final String tagToRemove;
//				switch (mApplicationConfigurations.getUserSelectedLanguage()) {
//				case Constants.LANGUAGE_HINDI:
//					tagToRemove = "pref_display_"
//							+ getResources().getString(R.string.lang_hindi);
//					break;
//				case Constants.LANGUAGE_TAMIL:
//					tagToRemove = "pref_display_"
//							+ getResources().getString(R.string.lang_tamil);
//					break;
//				case Constants.LANGUAGE_TELUGU:
//					tagToRemove = "pref_display_"
//							+ getResources().getString(R.string.lang_telugu);
//					break;
//				case Constants.LANGUAGE_PUNJABI:
//					tagToRemove = "pref_display_"
//							+ getResources().getString(R.string.lang_punjabi);
//					break;
//				case Constants.LANGUAGE_ENGLISH:
//				default:
//					tagToRemove = "pref_display_"
//							+ getResources().getString(R.string.lang_english);
//					break;
//				}
				if(!TextUtils.isEmpty(mApplicationConfigurations.getUserSelectedLanguageText())){
					tagToRemove = "pref_display_"
							+ mApplicationConfigurations.getUserSelectedLanguageText();
				} else{
					tagToRemove = "pref_display_"
							+ getResources().getString(
							R.string.lang_english);
				}

				try {
					ObjLanguagePackage lp = new ObjLanguagePackage(
							responseObjects.toString());
					mApplicationConfigurations.setUserSelectedLanguageText(lp
							.getLanguage());
					mApplicationConfigurations
							.setUserSelectedLanguage(lp.getId());
					Utils.changeLanguage(this.getBaseContext(), lp.getLanguage());

//					if (lp.getLanguage().equals("Hindi")) {
//						mApplicationConfigurations
//								.setUserSelectedLanguage(Constants.LANGUAGE_HINDI);
//						Utils.changeLanguage(this.getBaseContext(), "Hindi");
//					} else if (lp.getLanguage().equals("Tamil")) {
//						mApplicationConfigurations
//								.setUserSelectedLanguage(Constants.LANGUAGE_TAMIL);
//						Utils.changeLanguage(this.getBaseContext(), "Tamil");
//					} else if (lp.getLanguage().equals("Telugu")) {
//						mApplicationConfigurations
//								.setUserSelectedLanguage(Constants.LANGUAGE_TELUGU);
//						Utils.changeLanguage(this.getBaseContext(), "Telugu");
//					} else if (lp.getLanguage().equals("Punjabi")) {
//						mApplicationConfigurations
//								.setUserSelectedLanguage(Constants.LANGUAGE_PUNJABI);
//						Utils.changeLanguage(this.getBaseContext(), "Punjabi");
//					} else {
//						mApplicationConfigurations
//								.setUserSelectedLanguage(Constants.LANGUAGE_ENGLISH);
//						Utils.changeLanguage(this.getBaseContext(), "English");
//						mApplicationConfigurations
//								.setUserSelectedLanguageText("English");
//					}
					try {
						String tagToAdd = "pref_display_" + lp.getLanguage();
						Set<String> tags = Utils.getTags();
						if (!tags.contains(tagToAdd)) {
							if (tags.contains(tagToRemove))
								tags.remove(tagToRemove);
							tags.add(tagToAdd);
							Utils.AddTag(tags);
						}
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
					// Toast.makeText(
					// getApplicationContext(),
					// lp.getLanguage()
					// + " "
					// + mApplicationConfigurations
					// .getPartnerUserId(),
					// Toast.LENGTH_LONG).show();
				} catch (Exception e) {
					try {
						mApplicationConfigurations
								.setUserSelectedLanguage(Constants.LANGUAGE_ENGLISH);
						Utils.changeLanguage(this.getBaseContext(), "English");
						String tagToAdd = "pref_display_English";
						Set<String> tags = Utils.getTags();
						if (!tags.contains(tagToAdd)) {
							if (tags.contains(tagToRemove))
								tags.remove(tagToRemove);
							tags.add(tagToAdd);
							Utils.AddTag(tags);
						}
					} catch (Exception e1) {
						Logger.printStackTrace(e1);
					}
					e.printStackTrace();
				}
			}
				break;
			case (OperationDefinition.Hungama.OperationId.PREFERENCES_GET): {
				MusicCategoriesResponse musicCategoriesResponse = (MusicCategoriesResponse) responseObjects
						.get(MediaCategoriesOperation.RESULT_KEY_OBJECT_CATEGORIES);
				if (musicCategoriesResponse != null) {
					mApplicationConfigurations
							.setMusicPreferencesResponse(new Gson().toJson(
									musicCategoriesResponse).toString());
				}
			}
				break;
				case (OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE): {
					Profile mUserProfile = (Profile) responseObjects
							.get(SocialProfileOperation.RESULT_KEY_PROFILE);
					if (mUserProfile != null) {
						Utils.updateUserPointUATag(mUserProfile.points);
					}
				}
				break;
			default:
				break;
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		switch (operationId) {
		case OperationDefinition.Hungama.OperationId.LANGUAGE_SETTINGS_GET: {
			Logger.i(TAG, "Failed getting user language");
		}
			break;

		default:
			break;
		}
	}

//	public static class ObjLanguagePackage extends JSONObject {
//
//		public ObjLanguagePackage(String response) throws JSONException {
//			super(response);
//		}
//
//		public int getCode() {
//			try {
//				return getJSONObject("catalog").getInt("code");
//
//			} catch (Exception e) {
//			}
//			return 0;
//		}
//
//		public String getLanguage() {
//			try {
//				// return getJSONObject("catalog").getString("language");
//				return getJSONObject("response").getJSONObject("language")
//						.getString("language_name: ");
//
//			} catch (Exception e) {
//			}
//			return null;
//		}
//	}

	private void catchAllData(){
		ThreadPoolManager.getInstance().submit(new Runnable() {
			@Override
			public void run() {
				Logger.i("catchAllData", "Music Caching Started");
				getCachData();
				Logger.i("catchAllData", "Music Caching Ended");
				Logger.i("catchAllData", "Video Caching Started");
				getCachVideoData();
				Logger.i("catchAllData", "Video Caching Ended");
				Logger.i("catchAllData", "Radio Caching Started");
				getCachRadioData();
				Logger.i("catchAllData", "Radio Caching Ended");
//				super.run();
			}
		});
	}

	//----------------------Radio data cache---------------//
	public static ArrayList<MediaItem> mMediaItemsDisplay = new ArrayList<MediaItem>();
	public static List<MediaItem> mMediaItemsLiveRadio = new ArrayList<>();
	public static List<MediaItem> mMediaItemsTopArtists = new ArrayList<>();
	public void getCachRadioData(){
		try {
		mMediaItemsLiveRadio = getLiveRadioList();
		mMediaItemsTopArtists = getCelebRadioList();
		if(mMediaItemsLiveRadio == null)
			mMediaItemsLiveRadio = new ArrayList<>();
		if(mMediaItemsTopArtists == null)
			mMediaItemsTopArtists = new ArrayList<>();

		if (mMediaItemsLiveRadio != null
				&& mMediaItemsTopArtists != null) {
			List<MediaItem> tempList = new ArrayList<MediaItem>();
			tempList.addAll(mMediaItemsLiveRadio);
			for (MediaItem mediaItem : tempList) {
				if (mediaItem.getId() == -1 || mediaItem.getId() == -2) {
					mMediaItemsLiveRadio.remove(mediaItem);
				}
			}

			tempList = new ArrayList<MediaItem>();
			tempList.addAll(mMediaItemsTopArtists);
			for (MediaItem mediaItem : tempList) {
				if (mediaItem.getId() == -1 || mediaItem.getId() == -2) {
					mMediaItemsTopArtists.remove(mediaItem);
				}
			}

			Placement placementLiveRadio = CampaignsManager
					.getInstance(OnApplicationStartsActivity.this).getPlacementOfType(
							PlacementType.LIVE_RADIO_BANNER);
			if (placementLiveRadio != null
					&& mMediaItemsLiveRadio != null) {
				for (int i = 4; i < mMediaItemsLiveRadio.size(); i += 5) {
					mMediaItemsLiveRadio
							.add(i,
									new MediaItem(
											-1,// i,
											"no",
											"no",
											"no",
											"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ1tMe2aczrJBma6ii3qgKhwzIGa-Vrf6lD1LyVimnxHaQ-oiWLsA",
											"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ1tMe2aczrJBma6ii3qgKhwzIGa-Vrf6lD1LyVimnxHaQ-oiWLsA",
											"no", 0, 0));
				}
			}

			Placement placementCelebRadio = CampaignsManager
					.getInstance(OnApplicationStartsActivity.this).getPlacementOfType(
							PlacementType.DEMAND_RADIO_BANNER);
			if (placementCelebRadio != null
					&& mMediaItemsTopArtists != null) {
				for (int i = 4 - (mMediaItemsLiveRadio.size() % 5); i < mMediaItemsTopArtists
						.size(); i += 5) {
					mMediaItemsTopArtists
							.add(i,
									new MediaItem(
											-3,// i,
											"no",
											"no",
											"no",
											"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ1tMe2aczrJBma6ii3qgKhwzIGa-Vrf6lD1LyVimnxHaQ-oiWLsA",
											"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ1tMe2aczrJBma6ii3qgKhwzIGa-Vrf6lD1LyVimnxHaQ-oiWLsA",
											"no", 0, 0));
				}
			}

			if (mMediaItemsLiveRadio.size() > 0)
				mMediaItemsLiveRadio.add(0, new MediaItem(-2,
						getString(R.string.radio_live_radio_capital),
						"", "", "", "", null, 0, 0));
			if (mMediaItemsTopArtists.size() > 0)
				mMediaItemsTopArtists
						.add(0,
								new MediaItem(
										-2,
										getString(R.string.radio_top_artist_radio_capital),
										"", "", "", "", null, 0, 0));

			// mMediaItemsLiveRadio.addAll(mMediaItemsTopArtists);
			if (mMediaItemsDisplay != null)
				mMediaItemsDisplay.clear();

			mMediaItemsDisplay = new ArrayList<MediaItem>();
			ApplicationConfigurations appConfig = ApplicationConfigurations
					.getInstance(OnApplicationStartsActivity.this);

			mMediaItemsDisplay.addAll(mMediaItemsLiveRadio);
			mMediaItemsDisplay.addAll(mMediaItemsTopArtists);

			/*if (mMediaItemsTopArtists == null
					|| (mMediaItemsTopArtists != null && mMediaItemsTopArtists
					.size() == 0)) {
				rootView.findViewById(
						R.id.radio_filter_button_top_artist)
						.setEnabled(false);
				rootView.findViewById(
						R.id.radio_filter_button_top_artist)
						.setBackgroundColor(
								getResources()
										.getColor(
												R.color.bg_button_radio_list_filter_transparent));
			} else {
				rootView.findViewById(
						R.id.radio_filter_button_top_artist)
						.setEnabled(true);
				rootView.findViewById(
						R.id.radio_filter_button_top_artist)
						.setBackgroundDrawable(
								getResources()
										.getDrawable(
												R.drawable.background_radio_filter_button_selector));
			}*/
		}
		}catch(Exception e){

		}catch(Error e){

		}
	}


	private List<MediaItem> getLiveRadioList(){
		JSONParser parser = new JSONParser();

		try {
			String response = new CacheManager(OnApplicationStartsActivity.this)
						.getLiveRadioResponse();

			if (TextUtils.isEmpty(response)) {
				return null;
			}
			Map<String, Object> catalogMap = (Map<String, Object>) parser
					.parse(response);

			if (catalogMap.containsKey(RadioLiveStationsOperation.KEY_RESPONSE)) {
				catalogMap = (Map<String, Object>) catalogMap.get(RadioLiveStationsOperation.KEY_RESPONSE);
			} else {
				return null;
			}

			if (catalogMap.containsKey(RadioLiveStationsOperation.KEY_CONTENT)) {

				List<Map<String, Object>> contentMap = (List<Map<String, Object>>) catalogMap
						.get(RadioLiveStationsOperation.KEY_CONTENT);

				List<MediaItem> mediaItems = new ArrayList<MediaItem>();

				long id = 0;

				String title;
				String description;
				String streamingUrl = null;
				String streamingUrl_128 = null;
				String streamingUrl_320 = null;
				String imageUrl = null;
				String bigImageUrl = null;
				Map<String, List<String>> images = null;

				MediaItem mediaItem;

				for (Map<String, Object> stationMap : contentMap) {
					id = (Long) stationMap
							.get(RadioLiveStationsOperation.KEY_RADIO_ID);
					title = (String) stationMap.get(RadioLiveStationsOperation.KEY_TITLE);
					description = (String) stationMap.get(RadioLiveStationsOperation.KEY_DESCRIPTION);
					try {
						streamingUrl = (String) stationMap
								.get(RadioLiveStationsOperation.KEY_STREAMING_URL_64);
					} catch (Exception e) {
						e.printStackTrace();
					}

					try {
						streamingUrl_128 = (String) stationMap
								.get(RadioLiveStationsOperation.KEY_STREAMING_URL_128);
					} catch (Exception e) {
						e.printStackTrace();
					}

					try {
						streamingUrl_320 = (String) stationMap
								.get(RadioLiveStationsOperation.KEY_STREAMING_URL_320);
					} catch (Exception e) {
						e.printStackTrace();
					}

					try {
						images = (Map<String, List<String>>) stationMap
								.get(MediaItem.KEY_IMAGES);
					} catch (Exception e) {
					}
					LiveStation temp = new LiveStation(id, title, description,
							streamingUrl, imageUrl, bigImageUrl);
					temp.setStreamingUrl_128(streamingUrl_128);
					temp.setStreamingUrl_320(streamingUrl_320);
					mediaItem = temp;
					mediaItem.setMediaContentType(MediaContentType.RADIO);
					mediaItem.setImagesUrlArray(images);
					mediaItem.setMediaType(MediaType.LIVE);
					mediaItems.add(mediaItem);

					id++;
				}
				return mediaItems;
				/*Map<String, Object> resultMap = new HashMap<String, Object>();
				resultMap.put(RadioLiveStationsOperation.RESULT_KEY_OBJECT_MEDIA_ITEM, mediaItems);
				if (message != null)
					resultMap.put(HashTagListOperation.RESULT_KEY_MESSAGE,
							message);*/
			}
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}
		return null;
	}

	private List<MediaItem> getCelebRadioList(){
		JSONParser parser = new JSONParser();

		try {
			String response = new CacheManager(OnApplicationStartsActivity.this)
						.getCelebRadioResponse();
			if (TextUtils.isEmpty(response)) {
				return  null;
			}

			Map<String, Object> catalogMap = (Map<String, Object>) parser
					.parse(response);

			/*MessageFromResponse message = null;
			if (catalogMap.containsKey("message")) {
				Map<String, Object> responseMessage = (Map<String, Object>) catalogMap
						.get("message");
				message = new MessageFromResponse(
						(Long) responseMessage.get("show_message"),
						(String) responseMessage.get("message_text"));
			}*/

			if (catalogMap.containsKey(RadioTopArtistsOperation.KEY_RESPONSE)) {
				catalogMap = (Map<String, Object>) catalogMap.get(RadioTopArtistsOperation.KEY_RESPONSE);
			} else {
				return null;
			}

			if (catalogMap.containsKey(RadioTopArtistsOperation.KEY_CONTENT)) {

				List<Map<String, Object>> contentMap = (List<Map<String, Object>>) catalogMap
						.get(RadioTopArtistsOperation.KEY_CONTENT);

				List<MediaItem> mediaItems = new ArrayList<MediaItem>();

				long id;
				String name;
				String imageUrl = null;
				Map<String, List<String>> images;
				// long albumId;

				MediaItem mediaItem;

				if (contentMap != null) {
					for (Map<String, Object> stationMap : contentMap) {

						id = ((Long) stationMap.get(RadioTopArtistsOperation.KEY_ARTIST_ID)).longValue();
						name = (String) stationMap.get(RadioTopArtistsOperation.KEY_ARTIST_NAME);

						images = (Map<String, List<String>>) stationMap
								.get(MediaItem.KEY_IMAGES);

						mediaItem = new MediaItem(id, name, null, null,
								imageUrl, imageUrl, MediaType.ALBUM.toString()
								.toLowerCase(), 0, 0, images, 0);
						mediaItem.setMediaContentType(MediaContentType.RADIO);
						mediaItem.setMediaType(MediaType.ARTIST);
						mediaItems.add(mediaItem);
					}
				}
				/*Map<String, Object> resultMap = new HashMap<String, Object>();
				resultMap.put(RESULT_KEY_OBJECT_MEDIA_ITEM, mediaItems);
				if (message != null)
					resultMap.put(HashTagListOperation.RESULT_KEY_MESSAGE,
							message);*/

				return mediaItems;
			} else {

			}
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}
		return null;
	}


	//------------------------Video data cache---------------------//
	public static ArrayList<MediaItem> mediaitemVideo = new ArrayList<MediaItem>();

	public void getCachVideoData(){
		try{
		MediaContentType mediaContentType = MediaContentType.VIDEO;
		mediaitemVideo = new ArrayList<MediaItem>();
		MediaContentType mContentType = MediaContentType.MUSIC;
		MediaCategoryType mItemCategoryType = MediaCategoryType.LATEST;
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		List<MediaItem> items = null;
		String response = new CacheManager(OnApplicationStartsActivity.this)
				.getVideoLatestResponse();

		if(TextUtils.isEmpty(response))
			return;
		if (response.contains("\"images\":[]")) {
			response.replace("\"images\":[]", "\"images\":{}");
		}
		MediaItemsResponseCatalog mediaItemsResponseCatalog = (MediaItemsResponseCatalog) gson
				.fromJson(response,
						MediaItemsResponseCatalog.class);

		String lastTimesStamp = null;
		lastTimesStamp = mediaItemsResponseCatalog.getTimeStamp();

		Logger.e("lastTimesStamp ---*", lastTimesStamp);

		if (mediaItemsResponseCatalog != null) {
			items = mediaItemsResponseCatalog.getCatalog().getContent();
		}

		if (items == null) {
			items = new ArrayList<MediaItem>();
		}

		Logger.e("items:--- ", "" + items.size());
		// TODO: temporally solving the differentiating issue between Music
		// and Videos, solve this when inserting also campaigns.

		MediaItemsResponse mediaItemsResponse = mediaItemsResponseCatalog.getCatalog();
		List<MediaItem> tempList = mediaItemsResponse.getContent();
		if(tempList==null || (tempList!=null && tempList.size()==0))
			return;

		mediaitemVideo.addAll(tempList);
		CampaignsManager mCampaignsManager = CampaignsManager.getInstance(OnApplicationStartsActivity.this);
		Placement placement = placement = mCampaignsManager
				.getPlacementOfType(PlacementType.VIDEO_NEW);
		String backgroundLink = "";

		if (placement != null && mediaContentType == MediaContentType.VIDEO) {

			if (placement != null) {
				WindowManager w = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
				Display d = w.getDefaultDisplay();
				DisplayMetrics metrics = new DisplayMetrics();
				d.getMetrics(metrics);
				getWindowManager().getDefaultDisplay().getMetrics(metrics);
				backgroundLink = Utils
						.getDisplayProfile(metrics, placement);
			}

			int adPos = 0;
			for (int i = 3; i < mediaitemVideo.size(); i += 4) {

				adPos++;
				Logger.i("Hint", String.valueOf(i));
				// isVideoLoaded = true;
				MediaItem temp = new MediaItem(i, "no", "no", "no",
						backgroundLink, backgroundLink, "track", 0, 0);
				temp.setMediaContentType(mediaContentType);
				mediaitemVideo.add(i, temp);
			}
			if (adPos == 9) {
				MediaItem tempAd = new MediaItem(mediaitemVideo.size(), "no",
						"no", "no", backgroundLink, backgroundLink,
						"track", 0, 0);
				tempAd.setMediaContentType(mediaContentType);
				mediaitemVideo.add(mediaitemVideo.size(), tempAd);
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}
	}

	//------------------Music data cache-------------//

	public static ArrayList<Object> mediaitemMusic = new ArrayList<Object>();
	public static List<MediaItem> mediaItems_final = new ArrayList<MediaItem>();

	public void getCachData(){
		try {

		mediaitemMusic = new ArrayList<Object>();
		MediaContentType mContentType = MediaContentType.MUSIC;
		MediaCategoryType mItemCategoryType = MediaCategoryType.LATEST;
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		List<MediaItem> items = null;
		String response = new CacheManager(OnApplicationStartsActivity.this)
				.getMusicLatestResponse();
		if(TextUtils.isEmpty(response))
			return;
		if (response.contains("\"images\":[]")) {
			response.replace("\"images\":[]", "\"images\":{}");
		}
		MediaItemsResponseCatalog mediaItemsResponseCatalog = (MediaItemsResponseCatalog) gson
				.fromJson(response,
						MediaItemsResponseCatalog.class);

		String lastTimesStamp = null;
		lastTimesStamp = mediaItemsResponseCatalog.getTimeStamp();

		Logger.e("lastTimesStamp ---*", lastTimesStamp);

		if (mediaItemsResponseCatalog != null) {
			items = mediaItemsResponseCatalog.getCatalog().getContent();
		}

		if (items == null) {
			items = new ArrayList<MediaItem>();
		}

		Logger.e("items:--- ", "" + items.size());
		// TODO: temporally solving the differentiating issue between Music
		// and Videos, solve this when inserting also campaigns.
		for (MediaItem mediaItem : items) {
			if (mediaItem != null) {
				mediaItem.setMediaContentType(mContentType);
			}
		}

		// stores it in the cache manager.
		if (!Utils.isListEmpty(items)) {
			DataManager dataManager = DataManager.getInstance(OnApplicationStartsActivity.this);
			dataManager.storeMediaItems(mContentType, mItemCategoryType,
					items);
		}

		MediaItemsResponse mediaItemsResponse = mediaItemsResponseCatalog.getCatalog();
		List<MediaItem> temp = mediaItemsResponse.getContent();
		mediaItems_final.addAll(temp);
		setUpList(temp);
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}
	}


	private void setUpList(List<MediaItem> mediaItems_final){
		List<MediaItem> tracks = new ArrayList<MediaItem>();
		List<MediaItem> playlists = new ArrayList<MediaItem>();

		for (MediaItem mediaItem : mediaItems_final) {
			if (!mApplicationConfigurations.getFilterSongsOption()
					&& !mApplicationConfigurations
					.getFilterAlbumsOption()
					&& !mApplicationConfigurations
					.getFilterPlaylistsOption()) {
				if (mediaItem.getMediaType() == MediaType.PLAYLIST)
					playlists.add(mediaItem);
				else
					tracks.add(mediaItem);
			} else if (mediaItem.getMediaType() == MediaType.TRACK
					&& mApplicationConfigurations
					.getFilterSongsOption())
				tracks.add(mediaItem);
			else if (mediaItem.getMediaType() == MediaType.ALBUM
					&& mApplicationConfigurations
					.getFilterAlbumsOption())
				tracks.add(mediaItem);
			else if (mediaItem.getMediaType() == MediaType.PLAYLIST
					&& mApplicationConfigurations
					.getFilterPlaylistsOption())
				playlists.add(mediaItem);
		}
		arrangeTilePattern(tracks, playlists);
	}

	private void arrangeTilePattern(List<MediaItem> tracks,
									List<MediaItem> playlists) {
		ArrayList<MediaItem> serverSAA = new ArrayList<MediaItem>(tracks);
		int adcount = 0;
		CampaignsManager mCampaignsManager = CampaignsManager.getInstance(OnApplicationStartsActivity.this);
		Placement placement = mCampaignsManager
				.getPlacementOfType(PlacementType.MUSIC_NEW);
		String backgroundLink = "";
		// backgroundLink="http://content.hungama.com/audio%20album/display%20image/400x400%20jpeg/427650334.jpg";
		if (placement != null) {
			if (placement != null) {
				WindowManager w = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
				Display d = w.getDefaultDisplay();
				DisplayMetrics metrics = new DisplayMetrics();
				d.getMetrics(metrics);
				getWindowManager().getDefaultDisplay().getMetrics(metrics);
				backgroundLink = Utils
						.getDisplayProfile(metrics, placement);
			}
			for (int i = 0; i < serverSAA.size(); i++) {
				// System.out.println("i"+i +"       "+((i - 4) % 5));

				if (!isAd((MediaItem) serverSAA.get(i))
						&& (i > 0 && (i == 3 || (i > 6 && ((i - 3) % 6) == 0)))) {
					// System.out.println("add ad ");
					serverSAA.add(i /* + adcount */, new MediaItem(i, "no",
							"no", "no", backgroundLink, backgroundLink,
							"album", 0, 0));
					// serverSAA.add(i + adcount, "AD" + adcount);
					// if(i>6)
					adcount++;
				}
			}
			if ((serverSAA.size() > 6 && ((serverSAA.size() - 4) % 6) == 5)
					|| serverSAA.size() % 4 == 3) {
				Logger.s("add ad last " + serverSAA.size());
				serverSAA.add(serverSAA.size(), new MediaItem(serverSAA.size(),
						"no", "no", "no", backgroundLink, backgroundLink,
						"album", 0, 0));
				adcount++;
			}
		}

		if (adcount != 0)
			tracks = serverSAA;

		int blocks = (tracks.size() + playlists.size()) / 5;
		if ((tracks.size() + playlists.size()) % 5 > 0)
			blocks += 1;

		// Playlist & media content arrangement in list as per new design
		// for v4.4
		ComboMediaItem c;
		for (int i = 0; i < blocks; i++) {

			Logger.i("filter", "i=" + i);
			Logger.i("filter", "tracks.size()=" + tracks.size()
					+ "mediaitemMusic.size()=" + mediaitemMusic.size()
					+ "playlists.size()=" + playlists.size());

			if (tracks.size() > 0) {
				c = new ComboMediaItem(tracks.get(0),
						(tracks.size() > 1) ? tracks.get(1) : null);
				mediaitemMusic.add(c);
				tracks.remove(0);
				if (tracks.size() > 0)
					tracks.remove(0);
			}

			if (tracks.size() > 0) {
				c = new ComboMediaItem(tracks.get(0),
						(tracks.size() > 1) ? tracks.get(1) : null);
				mediaitemMusic.add(c);
				tracks.remove(0);
				if (tracks.size() > 0)
					tracks.remove(0);
			}
			if (playlists.size() > 0) {
				mediaitemMusic.add(playlists.get(0));
				playlists.remove(0);

				if (tracks.size() == 0) {
					for (MediaItem obj : playlists) {
						mediaitemMusic.add(obj);
					}
					playlists.clear();
				}
			}
			Logger.i("filter end", "tracks.size()=" + tracks.size()
					+ "mediaitemMusic.size()=" + mediaitemMusic.size()
					+ "playlists.size()=" + playlists.size());
		}
		if (tracks.size() > 0) {
			while (tracks.size() > 0) {
				c = new ComboMediaItem(tracks.get(0),
						(tracks.size() > 1) ? tracks.get(1) : null);
				mediaitemMusic.add(c);
				tracks.remove(0);
				if (tracks.size() > 0)
					tracks.remove(0);
			}
		}
		Logger.i("filter end***", "tracks.size()=" + tracks.size()
				+ "mediaitemMusic.size()=" + mediaitemMusic.size()
				+ "playlists.size()=" + playlists.size());
	}

	private boolean isAd(MediaItem mediaItem) {
		if (mediaItem != null) {
			String title = mediaItem.getTitle();
			String albumname = mediaItem.getAlbumName();
			String artistname = mediaItem.getArtistName();
			if (!TextUtils.isEmpty(title) && title.equalsIgnoreCase("no")
					&& !TextUtils.isEmpty(albumname)
					&& albumname.equalsIgnoreCase("no")
					&& !TextUtils.isEmpty(artistname)
					&& artistname.equalsIgnoreCase("no")) {
				return true;
			}
		}
		return false;
	}

//	@Override
//	protected void onPause() {
//		super.onPause();
////		if(AdXEvent.ENABLED){
////			Apsalar.unregisterApsalarReceiver();
////		}
//	}

	private void doApsalarAttributionApiCall(){
		ThreadPoolManager.getInstance().submit(new Runnable() {
			@Override
			public void run() {
//				super.run();
				try {
					String andi = Settings.Secure.getString(getContentResolver(),
							Settings.Secure.ANDROID_ID);
					if (!TextUtils.isEmpty(andi)) {
						URL url = new URL(
								getString(R.string.apsalar_attribution_api) + andi);
						com.hungama.myplay.activity.util.Logger.i(TAG,
								"URL fetched-" + url.toString());
						if (Logger.enableOkHTTP) {
							OkHttpClient client = CommunicationManager.getUnsafeOkHttpClient();
//							Request.Builder requestBuilder = new Request.Builder();
//							requestBuilder.url(url);
							Request.Builder requestBuilder = CommunicationManager.getRequestBuilder(OnApplicationStartsActivity.this, url);
							com.squareup.okhttp.Response responseOk = client.newCall(requestBuilder.build()).execute();
							if (responseOk.code() == HttpURLConnection.HTTP_OK) {
								String response = responseOk.body().string();
								Logger.s("Response :::: " + response);
								try {
									JSONObject json = new JSONObject(response);
									if (json.getString("status").equalsIgnoreCase("ok")) {
										mApplicationConfigurations
												.setApsalarAttributionStatus(true);
									}
								} catch (Exception e) {
									Logger.printStackTrace(e);
								}
							}
						} else {
							HttpURLConnection urlConnection = (HttpURLConnection) url
									.openConnection();
							if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
								InputStream in = new BufferedInputStream(
										urlConnection.getInputStream());
								StringBuilder sb = new StringBuilder();
								int ch = -1;
								while ((ch = in.read()) != -1) {
									sb.append((char) ch);
								}
								String response = sb.toString();
								Logger.s("Response :::: " + response);

								try {
									JSONObject json = new JSONObject(response);
									if (json.getString("status").equalsIgnoreCase("ok")) {
										mApplicationConfigurations
												.setApsalarAttributionStatus(true);
									}
								} catch (Exception e) {
									Logger.printStackTrace(e);
								}
								if (in != null)
									in.close();
							}
							if (urlConnection != null)
								urlConnection.disconnect();
							urlConnection = null;
						}
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
					Logger.i("Error-response-", "" + e);
				} catch (IOException e) {
					e.printStackTrace();
					Logger.i("Error-response-", "" + e);
				} catch (Exception e) {
					e.printStackTrace();
					Logger.i("Error-response-", "" + e);
				}
			}
		});
	}

	private boolean checkPermissions(){
		if (Utils.isAndroidM()) {
//			if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//					&& checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ArrayList<String> permissionRequired = new ArrayList<String>();
			if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
				permissionRequired.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
			}
			if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
				permissionRequired.add(Manifest.permission.ACCESS_COARSE_LOCATION);
			}
			if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
				permissionRequired.add(Manifest.permission.READ_PHONE_STATE);
			}
			if (checkSelfPermission(Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED){
				permissionRequired.add(Manifest.permission.GET_ACCOUNTS);
			}
			if(permissionRequired.size()>0){
				String[] permissions = permissionRequired.toArray(new String[permissionRequired.size()]);
				requestPermissions(permissions, 10001);
				return false;
			}
//			if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
//					|| checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
//					|| checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//				// TODO: Consider calling
//				//    public void requestPermissions(@NonNull String[] permissions, int requestCode)
//				// here to request the missing permissions, and then overriding
//				//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//				//                                          int[] grantResults)
//				// to handle the case where the user grants the permission. See the documentation
//				// for Activity#requestPermissions for more details.
//				//				return TODO;
//				String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE,
//						Manifest.permission.READ_PHONE_STATE};
//				requestPermissions(permissions, 10001);
//				return false;
//			}
		}
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if(requestCode==10001){
			permissionsAllowed = true;
			for(int i=0;i<grantResults.length;i++){
				if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
				} else {
					permissionsAllowed = false;
					break;
				}
			}
			if(!permissionsAllowed){
				Toast.makeText(this, "Please grant all permissions.", Toast.LENGTH_SHORT).show();
				finish();
			} else {
				init();
				catchAllData();
				if (Utils.isConnected()) {
					mApplicationConfigurations.setSaveOfflineAutoMode(false);
					ThreadPoolManager.getInstance().submit(new Loadit());
				} else {
					if (!mApplicationConfigurations.getSaveOfflineMode())
						mApplicationConfigurations.setSaveOfflineAutoMode(true);
					moveAhead();
				}
			}
		}
	}

	private void postNewUaTags(){
		if (!mApplicationConfigurations.getUATagDate().equals(
				Utils.getDate(System.currentTimeMillis(), "dd-MM-yy"))) {
			int songDuration = DBOHandler.Last7DaysSongConsumptionDetail(this, 0);
			int videoDuration = DBOHandler.Last7DaysSongConsumptionDetail(this, 1);

			Set<String> tags = Utils.getTags();

			String songConsumptionTag = Constants.UA_TAG_SONG_CONSUMPTION_LOW;
			if(songDuration >= 3.5 * 60 * 60) {
				songConsumptionTag = Constants.UA_TAG_SONG_CONSUMPTION_HIGH;
			} else if(songDuration >= 1 * 60 * 60 && songDuration < 3.5 * 60 * 60) {
				songConsumptionTag = Constants.UA_TAG_SONG_CONSUMPTION_MEDIUM;
			}
			if (!tags.contains(songConsumptionTag)) {
				if (tags.contains(mApplicationConfigurations.getUaTagSongConsumption()))
					tags.remove(mApplicationConfigurations.getUaTagSongConsumption());
				tags.add(songConsumptionTag);
				mApplicationConfigurations.setUaTagSongConsumption(songConsumptionTag);
			}

			String videoConsumptionTag = Constants.UA_TAG_VIDEO_CONSUMPTION_LOW;
			if(videoDuration >= 7 * 60 * 60) {
				videoConsumptionTag = Constants.UA_TAG_VIDEO_CONSUMPTION_HIGH;
			} else if(videoDuration >= 2 * 60 * 60 && videoDuration < 7 * 60 * 60) {
				videoConsumptionTag = Constants.UA_TAG_VIDEO_CONSUMPTION_MEDIUM;
			}
			if (!tags.contains(videoConsumptionTag)) {
				if (tags.contains(mApplicationConfigurations.getUaTagVideoConsumption()))
					tags.remove(mApplicationConfigurations.getUaTagVideoConsumption());
				tags.add(videoConsumptionTag);
				mApplicationConfigurations.setUaTagVideoConsumption(videoConsumptionTag);
			}

			Utils.AddTag(tags);

			String session = mApplicationConfigurations.getSessionID();
			isRealUser = mApplicationConfigurations.isRealUser();
			if (!TextUtils.isEmpty(session) && isRealUser) {
				mDataManager.getUserProfile(this, mApplicationConfigurations.getPartnerUserId(), this);
			}

			mApplicationConfigurations.setUATagDate(Utils.getDate(System.currentTimeMillis(), "dd-MM-yy"));
		}
	}

	private void gigyauserinfo(){
		 mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		isRealUser = mApplicationConfigurations.isRealUser();
		if (isRealUser) {
			GigyaManager mGigyaManager = new GigyaManager(this);
			mGigyaManager
					.setOnGigyaResponseListener(new GigyaManager.OnGigyaResponseListener() {
						@Override
						public void onTwitterInvite() {
						}

						@Override
						public void onSocializeGetUserInfoListener() {
						}

						@Override
						public void onSocializeGetFriendsInfoListener(
								List<FBFriend> fbFriendsList) {
						}

						@Override
						public void onSocializeGetContactsListener(
								List<GoogleFriend> googleFriendsList) {
						}

						@Override
						public void onGigyaLogoutListener() {
						}

						@Override
						public void onGigyaLoginListener(
								SocialNetwork provider,
								Map<String, Object> signupFields, long setId) {
						}

						@Override
						public void onFailSocialGetFriendsContactsListener() {
						}

						@Override
						public void onFacebookInvite() {
						}

						@Override
						public void onCancelRequestListener() {
						}

						@Override
						public void onConnectionRemoved() {

						}
					});
			mGigyaManager.socializeGetUserInfo();
		}
	}
}
