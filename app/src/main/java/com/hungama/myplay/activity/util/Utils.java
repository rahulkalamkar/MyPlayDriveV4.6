package com.hungama.myplay.activity.util;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bosch.myspin.serversdk.MySpinServerSDK;
import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.HungamaApplication;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.ThreadPoolManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.EventManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.events.CampaignPlayEvent;
import com.hungama.myplay.activity.gcm.IntentReceiver;
import com.hungama.myplay.activity.ui.AlertActivity;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.widgets.CustomAlertDialog;
import com.hungama.myplay.activity.ui.widgets.LanguageButton;
import com.hungama.myplay.activity.ui.widgets.LanguageCheckBox;
import com.hungama.myplay.activity.ui.widgets.LanguageEditText;
import com.hungama.myplay.activity.ui.widgets.LanguageRadioButton;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.reverie.lm.LM;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.urbanairship.UAirship;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class Utils {

	// public static boolean isNeedResume=true;

	public static final String TEXT_EMPTY = "";
	public static final String NETWORK_WIFI = "wifi";
	public static final String NETWORK_3G = "3g";
	public static final String NETWORK_4G = "4g";
	public static final String NETWORK_2G = "2g";
	public static final String DEVICE_OS = "ANDROID";
	private static final String TAG = "com.hungama.myplay.activity.util.Utils";
	// xtpl
	public static final String NETWORK_EDGE = "edge";// Changed by Hungama
	public static final Config bitmapConfig565 = Bitmap.Config.RGB_565;
	public static final Config bitmapConfig8888 = Bitmap.Config.ARGB_8888;

	// xtpl

	/**
	 * Encrypts the given string with MD5 algorithm.</br> If it doesn't success,
	 * an empty String will be returned.
	 */
	public static final String toMD5(String stringToConvert) {

		try {

			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte result[] = md5.digest(stringToConvert.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < result.length; i++) {
				String s = Integer.toHexString(result[i]);
				int length = s.length();
				if (length >= 2) {
					sb.append(s.substring(length - 2, length));
				} else {
					sb.append("0");
					sb.append(s);
				}
			}
			return sb.toString();

		} catch (NoSuchAlgorithmException e) {
			return "";
		}
	}

	public static String getAccountName(Context context) {
//		 return "test.upgrade.usera@gmail.com";
		// return "myplay.test81@gmail.com";
		try {
			ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
					.getInstance(context);
			String sesion = mApplicationConfigurations.getSessionID();
//			boolean isRealUser = mApplicationConfigurations.isRealUser();
			if (!TextUtils.isEmpty(sesion)/* && isRealUser */) {
				AccountManager accountManager = AccountManager.get(context);
				Account[] accounts = accountManager
						.getAccountsByType("com.google");
				if (accounts != null && accounts.length > 0) {
					return accounts[0].name;
				}
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return "";
	}

	public static final String secondsToString(int seconds) {
		try {
			return String.format("%02d:%02d", ((seconds % 3600) / 60),
					(seconds % 60));
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return "00:00";
	}

	public static boolean isListEmpty(List<?> list) {

		if (list == null || list.isEmpty()) {

			return true;
		}

		return false;
	}

	public static int convertDPtoPX(Context mContext, int sizeInDP) {
		final float scale = mContext.getResources().getDisplayMetrics().density;
		int sizeInPX = (int) (sizeInDP * scale + 0.5f);
		return sizeInPX;
	}

	/**
	 * Function to convert milliseconds time to Timer Format
	 * Hours:Minutes:Seconds
	 * */
	public static String milliSecondsToTimer(long milliseconds) {
		String finalTimerString = "";
		String secondsString = "";

		// Convert total duration into time
		int hours = (int) (milliseconds / (1000 * 60 * 60));
		int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
		int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
		// Add hours if there
		if (hours > 0) {
			finalTimerString = hours + ":";
		}

		// Prepending 0 to seconds if it is one digit
		if (seconds < 10) {
			secondsString = "0" + seconds;
		} else {
			secondsString = "" + seconds;
		}

		finalTimerString = finalTimerString + minutes + ":" + secondsString;

		// return timer string
		return finalTimerString;
	}

	/**
	 * Function to get Progress percentage
	 *
	 * @param currentDuration
	 * @param totalDuration
	 * */
	public static int getProgressPercentage(long currentDuration,
											long totalDuration) {

		Double percentage = (double) 0;

		long currentSeconds = (int) (currentDuration / 1000);
		long totalSeconds = (int) (totalDuration / 1000);

		// Calculating percentage
		percentage = (((double) currentSeconds) / totalSeconds) * 100;

		// Return percentage
		return percentage.intValue();
	}

	/**
	 * Function to change progress to timer
	 *
	 * @param progress
	 *            -
	 * @param totalDuration
	 *            returns current duration in milliseconds
	 * */
	public static int progressToTimer(int progress, long totalDuration) {
		int currentDuration = 0;
		totalDuration = (int) (totalDuration / 1000);
		currentDuration = (int) ((((double) progress) / 100) * totalDuration);

		// return current duration in milliseconds
		return currentDuration * 1000;
	}

	// /**
	// * Function to convert String to Date(Hungama format)
	// *
	// * @param validityDateString
	// * - returns validityDate in type Date.
	// * */
	// public static Date convertStringToDate(String validityDateString) {
	// // xtpl
	// SimpleDateFormat dateFormat = new SimpleDateFormat(
	// "dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);// Changes
	// // by
	// // Hungama
	// // xtpl
	// Date validityDate = null;
	// if (!validityDateString.equalsIgnoreCase(""))
	// try {
	// validityDate = dateFormat.parse(validityDateString);
	// } catch (ParseException e) {
	// e.printStackTrace();
	// }
	// return validityDate;
	// }

	public static Date convertTimeStampToDate(String timeStamp) {
		if (timeStamp.length() == 0) {
			return null;
		}

		String dateFormatUTC = "yyyy-MM-dd HH:mm:ss";
		// xtpl
		SimpleDateFormat sdfUTC = new SimpleDateFormat(dateFormatUTC,
				Locale.ENGLISH);// Changes
		// by
		// Hungama
		// xtpl

		Date date = null;
		try {
			date = sdfUTC.parse(timeStamp);
		} catch (ParseException e) {
			try {
				sdfUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",
						Locale.ENGLISH);// Changes
				// by
				// Hungama

				date = sdfUTC.parse(timeStamp);
			} catch (Exception ee) {
				try {
					sdfUTC = new SimpleDateFormat("dd/MM/yyyy",
							Locale.ENGLISH);// Changes
					// by
					// Hungama

					date = sdfUTC.parse(timeStamp);
				} catch (Exception e1) {
					Logger.printStackTrace(e1);
				}
			}

//			Logger.printStackTrace(e);
		}

		return date;
	}

	public static Date convertTimeStampToDateCM(String dateFormatUTC,
												String timeStamp) {

		SimpleDateFormat sdfUTC = new SimpleDateFormat(dateFormatUTC);

		Date date = null;
		try {
			date = sdfUTC.parse(timeStamp);
		} catch (ParseException e) {
			// xtpl
			try {
				sdfUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
				date = sdfUTC.parse(timeStamp);
			} catch (ParseException ex) {
				ex.printStackTrace();
			}
			// xtpl
		}

		return date;
	}

	public static boolean validateEmailAddress(String email) {
		Pattern pattern = Patterns.EMAIL_ADDRESS;
		return pattern.matcher(email).matches();
	}

	public static boolean validateName(String name) {
		String pattern = "[A-Za-z]+";
		return name.matches(pattern);
	}

	public static boolean isAlphaNumeric(String s) {
		String pattern = "^[a-zA-Z0-9]*$";
		if (s.matches(pattern)) {
			return true;
		}
		return false;
	}

	public static void invokeSMSApp(Context context, String smsBody) {

		Intent smsIntent = new Intent(Intent.ACTION_VIEW);

		smsIntent.putExtra("sms_body", smsBody);
		smsIntent.setType("vnd.android-dir/mms-sms");

		context.startActivity(smsIntent);
	}

	public static void invokeMoreShareOptions(Context context, String subject,
											  String extraText, String path, MediaType mediaType) {
		// // Send mail to all checked friends
		// Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
		// Uri.fromParts("mailto", "", null));
		// // Intent emailIntent = new Intent(Intent.ACTION_SEND);
		//
		// // emailIntent.setType("plain/text");
		// // emailIntent.setType("text/html");
		//
		// if (subject != null) {
		// emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		// }
		//
		// if (extraText != null) {
		// emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(extraText));
		// }
		//
		// if (emailBccTo != null) {
		// // Convert List<String> to String[]
		// String[] emailBccToArr = emailBccTo.toArray(new
		// String[emailBccTo.size()]);
		//
		// emailIntent.putExtra(Intent.EXTRA_BCC, emailBccToArr);
		// }
		//
		// fragment.startActivityForResult(emailIntent, 100);
		if (path == null || mediaType == null) {
			Intent sendIntent = new Intent();
			sendIntent.setAction(Intent.ACTION_SEND);
			// sendIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(extraText));
			sendIntent.putExtra(Intent.EXTRA_TEXT, extraText);
			// sendIntent.putExtra(Intent.EXTRA_SUBJECT,
			// "Text to Share");
			// sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
			sendIntent.setType("text/plain");
			context.startActivity(sendIntent);
		} else if (mediaType == MediaType.VIDEO) {
			Uri uri = Uri.parse("file://" + path);
			Intent sendIntent = new Intent(Intent.ACTION_SEND);
			sendIntent.setType("video/*");
			sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
			context.startActivity(sendIntent);
		} else if (mediaType == MediaType.TRACK) {
			Uri uri = Uri.parse("file://" + path);
			Intent sendIntent = new Intent(Intent.ACTION_SEND);
			sendIntent.setType("audio/*");
			sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
			context.startActivity(sendIntent);
		}
	}

	// public static void getCountry(Context context) {
	// try {
	// LocationManager lm = (LocationManager) context
	// .getSystemService(Context.LOCATION_SERVICE);
	// String locationProvider = LocationManager.NETWORK_PROVIDER;
	// Location lastKnownLocation = lm
	// .getLastKnownLocation(locationProvider);
	// } catch (Exception e) {
	//
	// }
	// }

	public static void invokeEmailApp(Fragment fragment,
									  List<String> emailBccTo, String subject, String extraText) {
		try {
			// Send mail to all checked friends
			Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
					Uri.fromParts("mailto", "", null));
			// Intent emailIntent = new Intent(Intent.ACTION_SEND);

			// emailIntent.setType("plain/text");
			// emailIntent.setType("text/html");

			if (subject != null) {
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
			}

			if (extraText != null) {
				emailIntent.putExtra(Intent.EXTRA_TEXT,
						Html.fromHtml(extraText));
			}

			if (emailBccTo != null) {
				// Convert List<String> to String[]
				String[] emailBccToArr = emailBccTo
						.toArray(new String[emailBccTo.size()]);

				emailIntent.putExtra(Intent.EXTRA_BCC, emailBccToArr);
			}

			fragment.startActivityForResult(emailIntent, 100);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	// public static void invokeEmailApp(Activity activity,
	// List<String> emailBccTo, String subject, String extraText) {
	//
	// // Send mail to all checked friends
	// Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
	// "mailto", "", null));
	// // Intent emailIntent = new Intent(Intent.ACTION_SEND);
	//
	// // emailIntent.setType("plain/text");
	// // emailIntent.setType("text/html");
	//
	// if (subject != null) {
	// emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
	// }
	//
	// if (extraText != null) {
	// emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(extraText));
	// }
	//
	// if (emailBccTo != null) {
	// // Convert List<String> to String[]
	// String[] emailBccToArr = emailBccTo.toArray(new String[emailBccTo
	// .size()]);
	//
	// emailIntent.putExtra(Intent.EXTRA_BCC, emailBccToArr);
	// }
	//
	// activity.startActivityForResult(emailIntent, 100);
	// }

	// public static String getTimestampAfterDelta(String eventTimestamp,
	// Context context) {
	// String timestamp = null;
	//
	// ApplicationConfigurations applicationConfigurations = DataManager
	// .getInstance(context.getApplicationContext())
	// .getApplicationConfigurations();
	//
	// long updatedTimestamp;
	// Date eventDate = null;
	// try {
	// // xtpl
	// eventDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",
	// Locale.ENGLISH).parse(eventTimestamp);
	// // xtpl
	// } catch (ParseException e) {
	// e.printStackTrace();
	// try {
	// // xtpl
	// eventDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
	// Locale.ENGLISH).parse(eventTimestamp);
	// // xtpl
	// } catch (Exception ex) {
	//
	// }
	// }
	// if (eventDate != null) {
	// long eventTimestampMillis = eventDate.getTime();
	//
	// updatedTimestamp = eventTimestampMillis
	// + applicationConfigurations.getTimeReadDelta();
	//
	// // xtpl
	// SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat(
	// "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);// Changes
	// // by
	// // Hungama
	// // xtpl
	// sSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("utc"));
	// timestamp = sSimpleDateFormat.format(new Date(updatedTimestamp));
	// }
	//
	// return timestamp;
	// }

	public static String getNetworkType(Context context) {
		// if(true) // for mobile network checking
		// return NETWORK_2G;
		try {
			ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connectivityManager
					.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				String networkType = networkInfo.getTypeName();
				if (networkType.equalsIgnoreCase(NETWORK_WIFI)) {
					return networkType;
				} else if (networkType.equalsIgnoreCase("MOBILE")) {
					int subType = networkInfo.getSubtype();
					switch (subType) {
						case TelephonyManager.NETWORK_TYPE_GPRS:
						case TelephonyManager.NETWORK_TYPE_EDGE:
						case TelephonyManager.NETWORK_TYPE_CDMA:
						case TelephonyManager.NETWORK_TYPE_1xRTT:
						case TelephonyManager.NETWORK_TYPE_IDEN:
							return networkType = NETWORK_2G;
						case TelephonyManager.NETWORK_TYPE_EVDO_0:
						case TelephonyManager.NETWORK_TYPE_EVDO_A:
						case TelephonyManager.NETWORK_TYPE_EVDO_B:
						case TelephonyManager.NETWORK_TYPE_HSDPA:
						case TelephonyManager.NETWORK_TYPE_HSPA:
						case TelephonyManager.NETWORK_TYPE_HSUPA:
						case TelephonyManager.NETWORK_TYPE_UMTS:
							return networkType = NETWORK_3G;
						case TelephonyManager.NETWORK_TYPE_HSPAP:
						case TelephonyManager.NETWORK_TYPE_LTE:
							return networkType = NETWORK_4G;
						case TelephonyManager.NETWORK_TYPE_UNKNOWN:
							return networkType = TEXT_EMPTY;
					}
				}
			}
			return TEXT_EMPTY;
		} catch (Exception e) {
			return TEXT_EMPTY;
		}
	}

	public static String toWhomSongBelongto(MediaItem mediaItem) {

		MediaType mt = mediaItem.getMediaType();
		String songBelongsTo = "Song belongs to";
		if (mt == MediaType.ALBUM) {
			songBelongsTo = mediaItem.getTitle();
		} else if (mt == MediaType.PLAYLIST) {
			songBelongsTo = mediaItem.getTitle();
		} else if (mt == MediaType.TRACK) {
			songBelongsTo = mediaItem.getAlbumName();
		}

		return songBelongsTo;
	}

//	public static String getFlurryEventNameByPlanType(DownloadPlan clickedPLan) {
//		String flurryEventName = "No plan type";
//		PlanType pt = null;
//		if (clickedPLan != null) {
//			pt = clickedPLan.getPlanType();
//		} else {
//			Logger.e(TAG, "Clicked plan is null!");
//		}
//
//		if (pt == PlanType.GOOGLE) {
//			flurryEventName = FlurryConstants.FlurryDownloadPlansParams.BuyFromAppStore
//					.toString();
//		} else if (pt == PlanType.MOBILE) {
//			flurryEventName = FlurryConstants.FlurryDownloadPlansParams.BuyFromOperatorBilling
//					.toString();
//		} else if (pt == PlanType.REEDEM) {
//			flurryEventName = FlurryConstants.FlurryDownloadPlansParams.GetFree
//					.toString();
//		}
//
//		return flurryEventName;
//	}

	public static Date getCurrentDate() {

		Calendar c = Calendar.getInstance();

		int zoneOffset = c.get(java.util.Calendar.ZONE_OFFSET);

		int dstOffset = c.get(java.util.Calendar.DST_OFFSET);

		c.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));

		Date date2 = c.getTime();

		return date2;
	}

	public static String getCurrentTimeStamp() {
		Date currentDate = Utils.getCurrentDate();
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		String currentTimeStamp = fmt.format(currentDate);
		return currentTimeStamp;
	}

	/**
	 * @param i
	 * @return
	 */
	public static String roundTheCount(int i) {
		String returnVal = "" + i;
		if (i > 9999) {
			returnVal = String.format("%.1f", (i / 1000f));
			if (returnVal.endsWith(".0"))
				returnVal = returnVal.substring(0, returnVal.length() - 2);
			returnVal += "K";
		}
		return returnVal;
	}

	public static void AddTag(Set<String> tags) {
		try {
			UAirship.shared().getPushManager().setTags(tags);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static Set<String> getTags() {
		Set<String> tags = null;
		try {
			tags = UAirship.shared().getPushManager().getTags();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		if (tags == null)
			tags = new HashSet<String>();
		return tags;
	}

	public static String getTimeOfDay() {
		Date currentDate = new Date();
		SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
		String currentTimeStamp = fmt.format(currentDate);
		return currentTimeStamp;
	}

	public static void postclickEvent(Context context, Placement placement) {
		try {
			if (placement == null || placement.getActions() == null
					|| placement.getActions().size() <= 0)
				return;
			ScreenLockStatus.getInstance(context).reset();
			ScreenLockStatus.getInstance(context).dontShowAd();
			DataManager mDataManager = DataManager.getInstance(context);
			int consumerId = mDataManager.getApplicationConfigurations()
					.getConsumerID();
			String deviceId = mDataManager.getApplicationConfigurations()
					.getDeviceID();
			String timeStamp = mDataManager.getDeviceConfigurations()
					.getTimeStampDelta();
			CampaignPlayEvent campaignPlayEvent = new CampaignPlayEvent(
					consumerId, deviceId, false, 0, timeStamp, 0, 0,
					placement.getTrackingID(), Long.parseLong(placement
					.getCampaignID()), EventManager.CLICK);
			mDataManager.addEvent(campaignPlayEvent);
		} catch (Exception e) {
		}
	}

	public static void postPlayEvent(Context context, Placement placement,
									 int duration, boolean completeplay) {
		try {
			DataManager mDataManager = DataManager.getInstance(context);
			int consumerId = mDataManager.getApplicationConfigurations()
					.getConsumerID();
			String deviceId = mDataManager.getApplicationConfigurations()
					.getDeviceID();
			String timeStamp = mDataManager.getDeviceConfigurations()
					.getTimeStampDelta();
			CampaignPlayEvent campaignPlayEvent = new CampaignPlayEvent(
					consumerId, deviceId, completeplay, duration, timeStamp, 0,
					0, placement.getTrackingID(), Long.parseLong(placement
					.getCampaignID()), EventManager.PLAY);
			mDataManager.addEvent(campaignPlayEvent);
		} catch (Exception e) {
		}
	}

	public static void postViewEvent(Context context, Placement placement) {
		try {
			DataManager mDataManager = DataManager.getInstance(context);
			int consumerId = mDataManager.getApplicationConfigurations()
					.getConsumerID();
			String deviceId = mDataManager.getApplicationConfigurations()
					.getDeviceID();
			String timeStamp = mDataManager.getDeviceConfigurations()
					.getTimeStampDelta();

			CampaignPlayEvent campaignPlayEvent = new CampaignPlayEvent(
					consumerId, deviceId, false, 0, timeStamp, 0, 0,
					placement.getTrackingID(), Long.parseLong(placement
					.getCampaignID()), EventManager.VIEW);

			mDataManager.addEvent(campaignPlayEvent);
			Logger.d("AD postViewEvent", "" + timeStamp);
		} catch (Exception e) {
			Logger.e("EventTrack", "DM :addEvent " + e);
			e.printStackTrace();

		}

	}

	public static String getDisplayProfile(DisplayMetrics metrics,
										   Placement placement) {
		String backgroundLink = "";
		try {
			if (metrics != null && placement != null) {
				if (metrics.widthPixels <= 240) {
					backgroundLink = placement.getDisplayInfoLdpi();
				} else if (metrics.widthPixels <= 320) {
					backgroundLink = placement.getDisplayInfoMdpi();
				} else if (metrics.widthPixels <= 480) {// 540
					backgroundLink = placement.getDisplayInfoHdpi();
				} else if (metrics.widthPixels <= 800) {
					backgroundLink = placement.getDisplayInfoXdpi();
				} else if (metrics.widthPixels <= 1500) {
					backgroundLink = placement.getDisplayInfoXhdpi();
				} else {
					backgroundLink = placement.getDisplayInfoXxhdpi();
				}
				if (TextUtils.isEmpty(backgroundLink)) {
					if (!TextUtils.isEmpty(placement.getDisplayInfoXxhdpi())) {
						backgroundLink = placement.getDisplayInfoXxhdpi();
					} else if (!TextUtils.isEmpty(placement
							.getDisplayInfoXhdpi())) {
						backgroundLink = placement.getDisplayInfoXhdpi();
					} else if (!TextUtils.isEmpty(placement
							.getDisplayInfoXdpi())) {
						backgroundLink = placement.getDisplayInfoXdpi();
					} else if (!TextUtils.isEmpty(placement
							.getDisplayInfoHdpi())) {
						backgroundLink = placement.getDisplayInfoHdpi();
					} else if (!TextUtils.isEmpty(placement
							.getDisplayInfoMdpi())) {
						backgroundLink = placement.getDisplayInfoMdpi();
					} else if (!TextUtils.isEmpty(placement
							.getDisplayInfoLdpi())) {
						backgroundLink = placement.getDisplayInfoLdpi();
					}
				}
			}
			Logger.i("getDisplayProfile ADURL", ">>" + backgroundLink);
		} catch (Exception e) {
		}

		return backgroundLink;
	}

	public static String getDisplayProfileCasting(Placement placement) {
		String backgroundLink = "";
		try {
			if (placement != null) {
				backgroundLink = placement.getDisplayInfoXxhdpi();
				if (TextUtils.isEmpty(backgroundLink)) {
					if (!TextUtils.isEmpty(placement.getDisplayInfoXxhdpi())) {
						backgroundLink = placement.getDisplayInfoXxhdpi();
					} else if (!TextUtils.isEmpty(placement
							.getDisplayInfoXhdpi())) {
						backgroundLink = placement.getDisplayInfoXhdpi();
					} else if (!TextUtils.isEmpty(placement
							.getDisplayInfoXdpi())) {
						backgroundLink = placement.getDisplayInfoXdpi();
					} else if (!TextUtils.isEmpty(placement
							.getDisplayInfoHdpi())) {
						backgroundLink = placement.getDisplayInfoHdpi();
					} else if (!TextUtils.isEmpty(placement
							.getDisplayInfoMdpi())) {
						backgroundLink = placement.getDisplayInfoMdpi();
					} else if (!TextUtils.isEmpty(placement
							.getDisplayInfoLdpi())) {
						backgroundLink = placement.getDisplayInfoLdpi();
					}
				}
			}
			Logger.i("getDisplayProfile ADURL", ">>" + backgroundLink);
		} catch (Exception e) {
		}

		return backgroundLink;
	}

	// public static synchronized BitmapDrawable getBitmapOnly(Context context,
	// int width,
	// String url) {
	// try {
	// FileCache fileCache = new FileCache(context);
	// File f = fileCache.getFile(url);
	// File tempFile = fileCache.getTempFile(url);
	// // System.out.println("ad image url ::::: " + url);
	// // System.out.println("ad image url ::::: " + f.getAbsolutePath());
	// // System.out.println("ad image url ::::: " + f.exists());
	// Bitmap bitmap = null;
	// if (!f.exists()) {
	// URL imageUrl = new URL(url);
	// HttpURLConnection conn = (HttpURLConnection) imageUrl
	// .openConnection();
	// // System.out.println("ad stream length :::::::::::: " +
	// conn.getContentLength());
	// conn.setConnectTimeout(CommunicationManager.getConnectionTimeout(context));
	// conn.setReadTimeout(CommunicationManager.getConnectionTimeout(context));
	// conn.setInstanceFollowRedirects(true);
	// InputStream is = conn.getInputStream();
	// // OutputStream os = new FileOutputStream(f);
	// OutputStream os = new FileOutputStream(tempFile);
	// copyStream(is, os);
	// os.close();
	// // System.out.println("ad stream length :::::::::::: file :: " +
	// tempFile.length());
	// if(conn.getContentLength()== tempFile.length() && tempFile.exists()){
	// tempFile.renameTo(f);
	// }/* else{
	// return new BitmapDrawable(context.getResources(),getBitmap(context,
	// url));
	// }*/
	// }
	// bitmap = decodeFile(context, f, width, false);
	// if (bitmap == null)
	// {
	// f.delete();
	// return null;
	// }
	// return new BitmapDrawable(context.getResources(),bitmap);
	// } catch (Exception ex) {
	// Logger.printStackTrace(ex);
	// return null;
	// } catch (Error ex) {
	// Logger.printStackTrace(ex);
	// return getBitmapOnly(context, width * 2 / 3, url);
	// }
	// }

	public static synchronized BitmapDrawable getBitmap(Context context,
														int width, String url) {
		try {
			FileCache fileCache = new FileCache(context);
			File f = fileCache.getFile(url);
			File tempFile = fileCache.getTempFile(url);
			// System.out.println("ad image url ::::: " + url);
			// System.out.println("ad image url ::::: " + f.getAbsolutePath());
			// System.out.println("ad image url ::::: " + f.exists());
			Bitmap bitmap = null;
			if (!f.exists()) {
				InputStream is = null;
				URL imageUrl = new URL(url);
				if (Logger.enableOkHTTP) {
					OkHttpClient client = CommunicationManager.getUnsafeOkHttpClient();
//					Request.Builder requestBuilder = new Request.Builder();
//					requestBuilder.url(imageUrl);
					Request.Builder requestBuilder = CommunicationManager.getRequestBuilder(context, imageUrl);
					com.squareup.okhttp.Response responseOk = client.newCall(requestBuilder.build()).execute();
					is = responseOk.body().byteStream();
					// OutputStream os = new FileOutputStream(f);
					OutputStream os = new FileOutputStream(tempFile);
					copyStream(is, os);
					os.close();
					// System.out.println("ad stream length :::::::::::: file :: " +
					// tempFile.length());
					if (responseOk.body().contentLength() == tempFile.length()
							&& tempFile.exists()) {
						tempFile.renameTo(f);
					} else {
						return new BitmapDrawable(context.getResources(),
								getBitmap(context, url));
					}
				} else {
					HttpURLConnection conn = (HttpURLConnection) imageUrl
							.openConnection();
					// System.out.println("ad stream length :::::::::::: " +
					// conn.getContentLength());
					conn.setConnectTimeout(CommunicationManager
							.getConnectionTimeout(context));
					conn.setReadTimeout(CommunicationManager
							.getConnectionTimeout(context));
					conn.setInstanceFollowRedirects(true);
					is = conn.getInputStream();
					// OutputStream os = new FileOutputStream(f);
					OutputStream os = new FileOutputStream(tempFile);
					copyStream(is, os);
					os.close();
					// System.out.println("ad stream length :::::::::::: file :: " +
					// tempFile.length());
					if (conn.getContentLength() == tempFile.length()
							&& tempFile.exists()) {
						tempFile.renameTo(f);
					} else {
						return new BitmapDrawable(context.getResources(),
								getBitmap(context, url));
					}
				}
			}
			bitmap = decodeFile(context, f, width, false);
			if (bitmap == null) {
				f.delete();
				return null;
			}
			return new BitmapDrawable(context.getResources(), bitmap);
		} catch (Exception ex) {
			Logger.printStackTrace(ex);
			return new BitmapDrawable(context.getResources(),
					getDefaultBitmap(context));
		} catch (Error ex) {
			Logger.printStackTrace(ex);
			return getBitmap(context, width * 2 / 3, url);
		}
	}

	// public static synchronized BitmapDrawable getOptimizedBitmap(Context
	// context, int width,
	// String url) {
	// try {
	// FileCache fileCache = new FileCache(context);
	// File f = fileCache.getFile(url);
	// File tempFile = fileCache.getTempFile(url);
	// // System.out.println("ad image url ::::: " + url);
	// // System.out.println("ad image url ::::: " + f.getAbsolutePath());
	// System.out.println("ad image url ::::: " + f.exists());
	// Bitmap bitmap = null;
	// if (!f.exists()) {
	// URL imageUrl = new URL(url);
	// HttpURLConnection conn = (HttpURLConnection) imageUrl
	// .openConnection();
	// // System.out.println("ad stream length :::::::::::: " +
	// conn.getContentLength());
	// conn.setConnectTimeout(CommunicationManager.getConnectionTimeout(context));
	// conn.setReadTimeout(CommunicationManager.getConnectionTimeout(context));
	// conn.setInstanceFollowRedirects(true);
	// InputStream is = conn.getInputStream();
	// // OutputStream os = new FileOutputStream(f);
	// OutputStream os = new FileOutputStream(tempFile);
	// copyStream(is, os);
	// os.close();
	// // System.out.println("ad stream length :::::::::::: file :: " +
	// tempFile.length());
	// if(conn.getContentLength()== tempFile.length() && tempFile.exists()){
	// tempFile.renameTo(f);
	// } else{
	// return new BitmapDrawable(context.getResources(),getBitmap(context,
	// url));
	// }
	// }
	// bitmap = decodeFile(context, f, width, true);
	// if (bitmap == null)
	// {
	// f.delete();
	// return null;
	// }
	// return new BitmapDrawable(context.getResources(),bitmap);
	// } catch (Exception ex) {
	// Logger.printStackTrace(ex);
	// return new
	// BitmapDrawable(context.getResources(),getDefaultBitmap(context));
	// } catch (Error ex) {
	// Logger.printStackTrace(ex);
	// return getOptimizedBitmap(context, width * 2 / 3, url);
	// }
	// }

	public static Bitmap getBitmap(Context context, String url) {
		try {
			FileCache fileCache = new FileCache(context);
			File f = fileCache.getFile(url);
			// System.out.println("ad image url ::::: " + url);
			// System.out.println("ad image url ::::: " + f.getAbsolutePath());
			// System.out.println("ad image url ::::: " + f.exists());
			Bitmap bitmap = null;
			if (!f.exists()) {
				InputStream is = null;
				URL imageUrl = new URL(url);
				if (Logger.enableOkHTTP) {
					OkHttpClient client = CommunicationManager.getUnsafeOkHttpClient();
//					Request.Builder requestBuilder = new Request.Builder();
//					requestBuilder.url(imageUrl);
					Request.Builder requestBuilder = CommunicationManager.getRequestBuilder(context, imageUrl);
					com.squareup.okhttp.Response responseOk = client.newCall(requestBuilder.build()).execute();
					is = responseOk.body().byteStream();
				} else {
					HttpURLConnection conn = (HttpURLConnection) imageUrl
							.openConnection();
					conn.setConnectTimeout(CommunicationManager
							.getConnectionTimeout(context));
					conn.setReadTimeout(CommunicationManager
							.getConnectionTimeout(context));
					conn.setInstanceFollowRedirects(true);
					is = conn.getInputStream();
				}
				OutputStream os = new FileOutputStream(f);
				copyStream(is, os);
				os.close();
			}

			bitmap = BitmapFactory.decodeStream(new FileInputStream(f));
			return bitmap;
		} catch (Exception ex) {
			Logger.printStackTrace(ex);
			return getDefaultBitmap(context);
		} catch (Error ex) {
			Logger.printStackTrace(ex);
			return getDefaultBitmap(context);
		}
	}

	private static Bitmap decodeFile(Context context, File f, int width,
									 boolean optimized) {
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;

			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// Find the correct scale value. It should be the power of 2.
			Logger.e("Utils.decodeFile", "o.outWidth=" + o.outWidth
					+ "  o.outHeight=" + o.outHeight + " Screen :" + width);
			final int REQUIRED_SIZE = width;
			int width_tmp = o.outWidth;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				scale *= 2;
			}
			if (scale == 1 && optimized)
				scale = 2;
			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			Logger.e("Utils.decodeFile", " inSampleSize :" + scale);
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		} catch (Error e) {
			Logger.printStackTrace(e);
		}
		// try {
		// return BitmapFactory.decodeStream(new FileInputStream(f));
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		return getDefaultBitmap(context);
	}

	private static Bitmap getDefaultBitmap(Context context) {
		try {
//            BitmapFactory.Options o=new BitmapFactory.Options();
			//o.inSampleSize = 2;
			return BitmapFactory.decodeResource(context.getResources(),
					R.drawable.album_main_thumb);
			// return BitmapFactory.decodeResource(context.getResources(),
			// R.drawable.icon_launcher);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		} catch (Error e) {
			Logger.printStackTrace(e);
		}
		return null;
	}

	private static void copyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (; ; ) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}

	/*
	 * public static Drawable ResizeBitmap(float dpi, int width, Drawable
	 * backgroundImage) { try { Logger.i("Size!", "Old width = " +
	 * String.valueOf(backgroundImage.getIntrinsicWidth())); int aspectRatio =
	 * backgroundImage.getIntrinsicWidth() /
	 * backgroundImage.getIntrinsicHeight(); Logger.i("Size!", "Aspect ratio: "
	 * + String.valueOf(aspectRatio)); float density = (float) dpi / 160;
	 * Logger.i("Size!", "Density: " + String.valueOf(density)); Bitmap resized
	 * = Bitmap.createScaledBitmap(((BitmapDrawable)
	 * backgroundImage).getBitmap(), (int) (width * density), (int) ((width *
	 * density) / aspectRatio), false);
	 * 
	 * return new BitmapDrawable(resized); } catch (Exception e) { } catch
	 * (Error e) { } return backgroundImage;
	 * 
	 * }
	 */

	public static Drawable ResizeBitmap(Context context, float dpi, int width,
										Drawable backgroundImage) {
		try {
			Logger.i(
					"Size!",
					"Old width = "
							+ String.valueOf(backgroundImage
							.getIntrinsicWidth()));
			float aspectRatio = (float) backgroundImage.getIntrinsicWidth()
					/ (float) backgroundImage.getIntrinsicHeight();
			Logger.i("Size!", "Aspect ratio: " + String.valueOf(aspectRatio));
			float density = (float) dpi / 160;
			// if(width>=1920){
			// density = (float) dpi / 320;
			// }
			Logger.i("Size!", "Density: " + String.valueOf(density));
			// Bitmap resized = Bitmap.createScaledBitmap(
			// ((BitmapDrawable) backgroundImage).getBitmap(),
			// (int) (width * density),
			// (int) (((float) width * density) / aspectRatio), false);
			Bitmap resized = Bitmap.createScaledBitmap(
					((BitmapDrawable) backgroundImage).getBitmap(),
					(int) width, (int) (((float) width) / aspectRatio), false);

			Logger.i("Size!", "width : " + width + " height:"
					+ (int) (((float) width) / aspectRatio));

			return new BitmapDrawable(context.getResources(), resized);
		} catch (Exception e) {
		} catch (Error e) {
		}
		return backgroundImage;

	}

	// public static BitmapDrawable ResizeBitmap(Context context,float dpi, int
	// width,
	// BitmapDrawable backgroundImage) {
	// try {
	// Logger.i(
	// "Size!",
	// "Old width = "
	// + String.valueOf(backgroundImage
	// .getIntrinsicWidth()) + " :::::::::::: "
	// + backgroundImage.getIntrinsicHeight());
	// float aspectRatio = (float) backgroundImage.getIntrinsicWidth()
	// / (float) backgroundImage.getIntrinsicHeight();
	// Logger.i("Size!", "Aspect ratio: " + String.valueOf(aspectRatio));
	// float density = (float) dpi / 160;
	// Logger.i("Size!", "Density: " + String.valueOf(density));
	// // Bitmap resized = Bitmap.createScaledBitmap(
	// // ((BitmapDrawable) backgroundImage).getBitmap(),
	// // (int) (width * density),
	// // (int) (((float) width * density) / aspectRatio), false);
	// Bitmap resized = Bitmap.createScaledBitmap(
	// ((BitmapDrawable) backgroundImage).getBitmap(),
	// (int) width, (int) (((float) width) / aspectRatio), false);
	// Logger.i("Size!", "cal size : " + ((int) (width * density))
	// + " ::::::::: " + ((int) ((width * density) / aspectRatio)));
	// Logger.i("Size!", "size : " + resized.getWidth() + " ::::::::: "
	// + resized.getHeight());
	// return new BitmapDrawable(context.getResources(),resized);
	// } catch (Exception e) {
	// } catch (Error e) {
	// }
	// return backgroundImage;
	//
	// }

	public static BitmapDrawable ResizeBitmap(Context context,
											  DisplayMetrics metrics, BitmapDrawable backgroundImage) {
		try {
			Logger.i(
					"Size!",
					"Old width = "
							+ String.valueOf(backgroundImage
							.getIntrinsicWidth()) + " :::::::::::: "
							+ backgroundImage.getIntrinsicHeight());
			float aspectRatio = (float) backgroundImage.getIntrinsicWidth()
					/ (float) backgroundImage.getIntrinsicHeight();
			Logger.i("Size!", "Aspect ratio: " + String.valueOf(aspectRatio));
			// float density = (float) dpi / 160;
			int width = metrics.widthPixels;
			float density = metrics.density;
			Logger.i("Size!", "Density: " + String.valueOf(density));
			Bitmap resized = Bitmap.createScaledBitmap(
					((BitmapDrawable) backgroundImage).getBitmap(),
					(int) (width * density),
					(int) (((float) width * density) / aspectRatio), false);
			// Bitmap resized = Bitmap.createScaledBitmap(
			// ((BitmapDrawable) backgroundImage).getBitmap(),
			// (int) width, (int) (((float) width) / aspectRatio), false);
			Logger.i("Size!", "cal size : " + ((int) (width * density))
					+ " ::::::::: " + ((int) ((width * density) / aspectRatio)));
			Logger.i("Size!", "size : " + resized.getWidth() + " ::::::::: "
					+ resized.getHeight());
			return new BitmapDrawable(context.getResources(), resized);
		} catch (Exception e) {
		} catch (Error e) {
		}
		return backgroundImage;

	}

	private static final String HUNGAMA_CACHE = "hungama-cache";
	private static final int MIN_DISK_CACHE_SIZE = 50 * 1024 * 1024; // 5MB

	// private static final int MAX_DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB

	public static File createDefaultCacheDir(Context context) {
		File cache;
		try {
			cache = new File(context.getApplicationContext()
					.getExternalCacheDir(), HUNGAMA_CACHE);
			if (!cache.exists()) {
				cache.mkdirs();
			}
		} catch (Exception e) {
			cache = new File(context.getApplicationContext().getCacheDir(),
					HUNGAMA_CACHE);
			if (!cache.exists()) {
				cache.mkdirs();
			}
		}
		return cache;
	}

	// public static long calculateDiskCacheSize(File dir) {
	// long size = MIN_DISK_CACHE_SIZE;
	// long available = 0;
	// try {
	// StatFs statFs = new StatFs(dir.getAbsolutePath());
	// available = ((long) statFs.getBlockCount()) * statFs.getBlockSize();
	// size = (long) (available * 0.3f);// / 50;
	// } catch (IllegalArgumentException ignored) {
	// }
	// System.out.println(available + " :::::: " + size
	// + " :::: calculateDiskCacheSize :::: "
	// + Math.max(size, MIN_DISK_CACHE_SIZE));
	// return Math.max(size, MIN_DISK_CACHE_SIZE);
	// }

	private static int DOWNLOAD_TIMEOUT = 20000;

	/**
	 * Requests connection get connection from server. Handles up to 1 redirect.
	 *
	 * @param downloadUrl
	 *            of the file.
	 * @param context
	 * @return an open HttpUrlConnection pass the .connect()
	 * @throws IOException
	 */
	public static HttpURLConnection httpHandler(String downloadUrl,
												Context context) throws IOException {
		HttpURLConnection urlConnection = null;
		String protocol = parseProtocol(downloadUrl);
		URL ul = new URL(downloadUrl);
		if (protocol.equals("http"))
			urlConnection = (HttpURLConnection) ul.openConnection();
		else
			urlConnection = (HttpsURLConnection) ul.openConnection();
		urlConnection.setConnectTimeout(CommunicationManager
				.getConnectionTimeout(context));
		urlConnection.setReadTimeout(CommunicationManager
				.getConnectionTimeout(context));
		urlConnection.connect();

		boolean redirect = false;
		// normally, 3xx is redirect
		int status = urlConnection.getResponseCode();
		if (status != HttpURLConnection.HTTP_OK) {
			if (status == HttpURLConnection.HTTP_MOVED_TEMP
					|| status == HttpURLConnection.HTTP_MOVED_PERM
					|| status == HttpURLConnection.HTTP_SEE_OTHER)
				redirect = true;
		}

		Logger.s("Response Code ... " + status);

		if (redirect) {
			urlConnection.disconnect();
			// get redirect url from "location" header field
			String newUrl = urlConnection.getHeaderField("Location");
			protocol = parseProtocol(newUrl);
			ul = new URL(newUrl);
			if (protocol.equals("http"))
				urlConnection = (HttpURLConnection) ul.openConnection();
			else
				urlConnection = (HttpsURLConnection) ul.openConnection();

			urlConnection.setConnectTimeout(DOWNLOAD_TIMEOUT);
			urlConnection.setReadTimeout(DOWNLOAD_TIMEOUT);
			urlConnection.connect();
		}

		return urlConnection;
	}

	public static String parseProtocol(String url) {
		if (url != null && url.contains(":")) {
			int index = url.indexOf(":");
			return url.substring(0, index);
		} else
			return null;
	}

	public static boolean isConnected() {
		try {
			Context context = HungamaApplication.getContext();
			final ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()
					&& networkInfo.isAvailable()) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}

	}

	public static void performclickEvent(Context context, Placement placement) {
		try {
			if (placement == null || placement.getActions() == null
					|| placement.getActions().size() <= 0)
				return;

			String url = placement.getActions().get(0).action;
			if (url != null) {
				Logger.d("xxxxx", ">>" + url);
				if (url.startsWith("http://ua://")
						|| url.startsWith("http://ua//")
						|| url.startsWith("ua://")) {
					Uri uri = Uri.parse(url);
					Intent intent = new Intent(context, AlertActivity.class);
					getParameter(uri, IntentReceiver.CODE, intent);
					getParameter(uri, IntentReceiver.CATEGORY, intent);
					getParameter(uri, IntentReceiver.ARTIST_ID, intent);
					getParameter(uri, IntentReceiver.STATION_ID, intent);
					getParameter(uri, IntentReceiver.CHANNEL_INDEX, intent);
					getParameter(uri, IntentReceiver.CONTENT_ID, intent);
					getParameter(uri, IntentReceiver.CONTENT_TYPE, intent);
					getParameter(uri, IntentReceiver.SUBSCRIPTION_PLANS, intent);
					intent.putExtra(AlertActivity.IS_CUSTOM_RICHPUSH_LINK, true);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent);
				} else {
					//old
//					Intent browserIntent = new Intent(Intent.ACTION_VIEW,
//							Uri.parse(url));
//					browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					context.startActivity(browserIntent);

//					String packageName = "com.android.browser";
//					String className = "com.android.browser.BrowserActivity";
					Intent internetIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//					internetIntent.addCategory(Intent.CATEGORY_LAUNCHER);
					try {
						final PackageManager pm = context.getPackageManager();
						List<ResolveInfo> activityList = pm.queryIntentActivities(internetIntent, 0);
						for (int i = 0; i < activityList.size(); i++) {
							ResolveInfo app = activityList.get(i);
							Logger.s(app.activityInfo.packageName + " ::::::::::::::::: " + app.activityInfo.name);
							internetIntent.setClassName(app.activityInfo.packageName, app.activityInfo.name);
							break;
						}
					} catch (Exception e){
						Logger.printStackTrace(e);
					}
//					internetIntent.setClassName(packageName, className);
                    internetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(internetIntent);
				}
				postclickEvent(context, placement);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void getParameter(Uri uri, String key, Intent i) {
		if (uri.getQueryParameter(key) != null) {
			Logger.i("Utils:", key + " ::> " + uri.getQueryParameter(key));
			i.putExtra(key, uri.getQueryParameter(key));
		}
	}

	public static void setBackground(View v, Drawable d) {
		try {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
				v.setBackgroundDrawable(d);
			else
				v.setBackground(d);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static String getUserState(Context context) {
		String userStatus = FlurryConstants.FlurryCaching.Free.toString();
		Date userCurrentPlanValidityDate = null;
		Date today = new Date();
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(context);
		if (userCurrentPlanValidityDate == null) {
			userCurrentPlanValidityDate = Utils
					.convertTimeStampToDate(mApplicationConfigurations
							.getUserSubscriptionPlanDate());
		}
		if (mApplicationConfigurations.isUserHasSubscriptionPlan()
				&& userCurrentPlanValidityDate != null
				&& !today.after(userCurrentPlanValidityDate)) {
			userStatus = FlurryConstants.FlurryCaching.Paid.toString();
		} else if (mApplicationConfigurations.isUserHasTrialSubscriptionPlan()
				&& userCurrentPlanValidityDate != null
				&& !today.after(userCurrentPlanValidityDate)) {
			userStatus = FlurryConstants.FlurryCaching.Trial.toString();
		} else if (mApplicationConfigurations.isUserTrialSubscriptionExpired()
				|| (mApplicationConfigurations.isUserHasTrialSubscriptionPlan()
				&& userCurrentPlanValidityDate != null && today
				.after(userCurrentPlanValidityDate))) {
			userStatus = FlurryConstants.FlurryCaching.Trial_expired.toString();
		}
		return userStatus;
	}

	public static void saveOfflineFlurryEvent(Context context, String source,
											  MediaItem mediaItem) {
		Map<String, String> reportMap = new HashMap<String, String>();
		reportMap.put(FlurryConstants.FlurryCaching.Source.toString(), source);
		if (mediaItem.getMediaContentType() != null
				&& mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				reportMap.put(
						FlurryConstants.FlurryCaching.ContentType.toString(),
						FlurryConstants.FlurryCaching.Song.toString());
			} else if (mediaItem.getMediaType() == MediaType.ALBUM) {
				reportMap.put(
						FlurryConstants.FlurryCaching.ContentType.toString(),
						FlurryConstants.FlurryCaching.Album.toString());
			} else if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
				reportMap.put(
						FlurryConstants.FlurryCaching.ContentType.toString(),
						FlurryConstants.FlurryCaching.Playlist.toString());
			} else if (mediaItem.getMediaType() == MediaType.VIDEO) {
				reportMap.put(
						FlurryConstants.FlurryCaching.ContentType.toString(),
						FlurryConstants.FlurryCaching.Video.toString());
			}
		} else if (mediaItem.getMediaContentType() != null
				&& mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
			reportMap.put(FlurryConstants.FlurryCaching.ContentType.toString(),
					FlurryConstants.FlurryCaching.Video.toString());
		} else if (mediaItem.getMediaType() != null) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				reportMap.put(
						FlurryConstants.FlurryCaching.ContentType.toString(),
						FlurryConstants.FlurryCaching.Song.toString());
			} else if (mediaItem.getMediaType() == MediaType.ALBUM) {
				reportMap.put(
						FlurryConstants.FlurryCaching.ContentType.toString(),
						FlurryConstants.FlurryCaching.Album.toString());
			} else if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
				reportMap.put(
						FlurryConstants.FlurryCaching.ContentType.toString(),
						FlurryConstants.FlurryCaching.Playlist.toString());
			} else if (mediaItem.getMediaType() == MediaType.VIDEO) {
				reportMap.put(
						FlurryConstants.FlurryCaching.ContentType.toString(),
						FlurryConstants.FlurryCaching.Video.toString());
			}
		} else {
			reportMap.put(FlurryConstants.FlurryCaching.ContentType.toString(),
					FlurryConstants.FlurryCaching.Song.toString());
		}
		reportMap.put(FlurryConstants.FlurryCaching.UserStatus.toString(),
				getUserState(context));
		reportMap.put(FlurryConstants.FlurryCaching.Title_contentID.toString(),
				mediaItem.getTitle() + "_" + mediaItem.getId());
		Analytics.logEvent(
				FlurryConstants.FlurryCaching.TappedonSaveOffline.toString(),
				reportMap);
	}

	public static void saveAllOfflineFlurryEvent(Context context,
												 String source, List<Track> mTracks) {
		for (Track track : mTracks) {
			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(FlurryConstants.FlurryCaching.Source.toString(),
					source);
			reportMap.put(FlurryConstants.FlurryCaching.ContentType.toString(),
					FlurryConstants.FlurryCaching.Song.toString());
			reportMap.put(FlurryConstants.FlurryCaching.UserStatus.toString(),
					getUserState(context));
			reportMap.put(
					FlurryConstants.FlurryCaching.Title_contentID.toString(),
					track.getTitle() + "_" + track.getId());
			Analytics.logEvent(
					FlurryConstants.FlurryCaching.TappedonSaveOffline
							.toString(), reportMap);
		}
	}

	public static long getFolderSize(File dir) {
		try {
			if (dir.exists()) {
				long result = 0;
				File[] fileList = dir.listFiles();
				for (int i = 0; i < fileList.length; i++) {
					// Recursive call if it's a directory
					if (fileList[i].isDirectory()) {
						result += getFolderSize(fileList[i]);
					} else {
						// Sum the file size in bytes
						result += fileList[i].length();
					}
				}
				return result; // return the file size
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		} catch (Error e) {
			System.gc();
			System.runFinalization();
			System.gc();
		}
		return 0;
	}

	// public static RevTransliteration transs;

	// public static String ConvertText(Context c, String text) {
	// // return text;
	// ApplicationConfigurations mApplicationConfigurations = new
	// ApplicationConfigurations(
	// c);
	// if (Logger.enableLanguageLibrary)
	// transs = new RevTransliteration(c);
	//
	// if (mApplicationConfigurations.getUserSelectedLanguage() == 0) {
	// return text;
	// } else {
	// char[][] str = transs
	// .getTransliteratedWords(
	// text.toLowerCase(),
	// mApplicationConfigurations
	// .getUserSelectedLanguage(),
	// 1,
	// com.reverie.transliteration.RevTransliteration.TYPE_FREE_TEXT,
	// com.reverie.common.RevConstants.LANG_ENGLISH);
	// if (str != null && str.length > 0) {
	// return new String(str[0]);
	// } else {
	// return text;
	// }
	// }
	// }

	// 			Utils.performclickEventAction(
//					this,
//					"ua://callback/?code=47&key=Subscription_Plans&Value=Subscription Plans (NEW)");
	public static void performclickEventAction(Context context, String action) {
		try {
			String url = action;
			if (url != null) {
				Logger.d("xxxxx", ">>" + url);
				if (url.startsWith("http://ua://")
						|| url.startsWith("http://ua//")
						|| url.startsWith("ua://")) {
					Uri uri = Uri.parse(url);
					Intent intent = new Intent(context, AlertActivity.class);
					getParameter(uri, IntentReceiver.CODE, intent);
					getParameter(uri, IntentReceiver.CATEGORY, intent);
					getParameter(uri, IntentReceiver.ARTIST_ID, intent);
					getParameter(uri, IntentReceiver.STATION_ID, intent);
					getParameter(uri, IntentReceiver.CHANNEL_INDEX, intent);
					getParameter(uri, IntentReceiver.CONTENT_ID, intent);
					getParameter(uri, IntentReceiver.CONTENT_TYPE, intent);
					getParameter(uri, IntentReceiver.SUBSCRIPTION_PLANS, intent);
					intent.putExtra(AlertActivity.IS_CUSTOM_RICHPUSH_LINK,
							false);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent);
				} else {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW,
							Uri.parse(url));
					browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(browserIntent);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void traverseChild(View roort, Context c) {
		// if (true)
		// return;
		try {

			Logger.s("traverseChild start :::::::: "
					+ System.currentTimeMillis());
			if (Logger.enableLanguageLibrary) {
				ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
						.getInstance(c);
				boolean isEnglish = mApplicationConfigurations
						.getUserSelectedLanguage() == Constants.LANGUAGE_ENGLISH;

				if (isEnglish)
					return;

				View childView;
				LanguageTextView tv;
				if (roort instanceof ViewGroup) {
					ViewGroup relative = (ViewGroup) roort;
					for (int i = 0; i < relative.getChildCount(); i++) {
						childView = relative.getChildAt(i);
						if (childView instanceof ViewGroup)
							traverseChild(childView, c);
						else if (childView instanceof LanguageTextView) {
							tv = (LanguageTextView) childView;
							tv.setText(DBOHandler.getTextFromDb(tv.getText()
									.toString(), c));
						} else if (childView instanceof LanguageEditText) {
							LanguageEditText edt = (LanguageEditText) childView;
							edt.setHint(DBOHandler.getTextFromDb(edt.getHint()
									.toString(), c));
						} else if (childView instanceof LanguageButton) {
							LanguageButton button = (LanguageButton) childView;
							button.setText(DBOHandler.getTextFromDb(button
									.getText().toString(), c));
						} else if (childView instanceof LanguageRadioButton) {
							LanguageRadioButton radio = (LanguageRadioButton) childView;
							radio.setText(DBOHandler.getTextFromDb(radio
									.getText().toString(), c));
						} else if (childView instanceof LanguageCheckBox) {
							LanguageCheckBox radio = (LanguageCheckBox) childView;
							radio.setText(DBOHandler.getTextFromDb(radio
									.getText().toString(), c));
						}
					}
				}
				Logger.s("traverseChild end :::::::: "
						+ System.currentTimeMillis());
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public static void SetMultilanguageTextOnTextView(Context c,
													  LanguageTextView tv, String text) {
		// if (true) {
		// tv.setText(text);
		// return;
		// }
		if (TextUtils.isEmpty(text)) {
			tv.setText(text);
			return;
		}
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(c);
		if (mApplicationConfigurations.getUserSelectedLanguage() == 0) {
			tv.setText(text);
		} else {
			tv.setText(DBOHandler.getTextFromDb(text, c));

		}

	}

	public static String getMultilanguageTextLayOut(Context c, String value) {
		// if (true)
		// return value;
		if (TextUtils.isEmpty(value))
			return "";
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(c);
		if (mApplicationConfigurations.getUserSelectedLanguage() == 0) {
			return value;
		} else {
			return DBOHandler.getTextFromDb(value, c);
		}
	}

	public static String getMultilanguageText(Context c, String value) {
		// if (true)
		// return value;
		// ApplicationConfigurations mApplicationConfigurations = new
		// ApplicationConfigurations(
		// c);
		// if (mApplicationConfigurations.getUserSelectedLanguage() == 0) {
		// return value;
		// } else {
		// return DBOHandler.getTextFromDb(value, c);
		// }
		return getMultilanguageTextLayOut(c, value);
	}

	private static Toast toast;
	private static LanguageTextView textview;
	private static ApplicationConfigurations mApplicationConfigurations;

	public static Toast makeText(Context c, String value, int length) {
		try {
			if (toast == null) {
				mApplicationConfigurations = ApplicationConfigurations
						.getInstance(c.getApplicationContext());
				textview = new LanguageTextView(c.getApplicationContext());
				toast = Toast.makeText(c.getApplicationContext(), value,
						Toast.LENGTH_LONG);
				toast.setView(textview);
				textview.setBackgroundResource(R.drawable.roundshape_toast);
				textview.setPadding(25, 10, 25, 10);
				textview.setTextColor(Color.WHITE);
			}
			toast.setDuration(length);

			if (mApplicationConfigurations.getUserSelectedLanguage() == 0) {
				textview.setText(value);
			} else
				textview.setText(DBOHandler.getTextFromDb(value, c));
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return toast;
	}

	public static String getMultilanguageTextHindi(Context c, String value) {
		// if (true)
		if (TextUtils.isEmpty(value))
			return "";
		// return value;
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(c);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			if (mApplicationConfigurations.getUserSelectedLanguage() != Constants.LANGUAGE_HINDI) {
				return value;
			} else {
				return DBOHandler.getTextFromDb(value, c);
			}
		} else {
			if (mApplicationConfigurations.getUserSelectedLanguage() == 0) {
				return value;
			} else {
				return DBOHandler.getTextFromDb(value, c);
			}
		}
	}

	public static void SetMultilanguageTextOnButton(Context c,
													LanguageButton button, String text) {
		// if (true) {
		// System.out.println("SetMultilanguageTextOnButton:" + text);
		// button.setText(text);
		// return;
		// }
		if (TextUtils.isEmpty(text)) {
			button.setText("");
			return;
		}
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(c);
		if (mApplicationConfigurations.getUserSelectedLanguage() == 0) {
			button.setText(text);
		} else {
			button.setText(DBOHandler.getTextFromDb(text, c));
		}
	}

	// public static void SetMultilanguageTextOnRadioButton(Context c,
	// LanguageRadioButton radio, String text, int selected_language,
	// boolean isSelected) {
	// // if (true) {
	// // radio.setText(text);
	// // return;
	// // }
	//
	// ApplicationConfigurations mApplicationConfigurations =
	// ApplicationConfigurations.getInstance(
	// c);
	// if (mApplicationConfigurations.getUserSelectedLanguage() == 0) {
	// if (isSelected) {
	// radio.setText(text + " (selected)");
	// } else {
	// radio.setText(text);
	// }
	// } else {
	// if (isSelected) {
	// radio.setText(DBOHandler.getTextFromDb(text, c) + " (selected)");
	// } else {
	// radio.setText(DBOHandler.getTextFromDb(text, c));
	// }
	// }
	// }

	public static void SetMultilanguageTextOnTextView(Context c,
													  LanguageTextView radio, String text, int selected_language,
													  boolean isSelected) {
		// if (true) {
		// radio.setText(text);
		// return;
		// }

		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(c);
		if (mApplicationConfigurations.getUserSelectedLanguage() == 0) {
			if (isSelected) {
				radio.setText(text + " (selected)");
			} else {
				radio.setText(text);
			}
		} else {
			if (isSelected) {
				radio.setText(DBOHandler.getTextFromDb(text, c) + " (selected)");
			} else {
				radio.setText(DBOHandler.getTextFromDb(text, c));
			}
		}
	}

	public static void changeLanguage(Context c, String lang_code) {
		// Configuration Config = c.getResources().getConfiguration();
		// Locale locale = new Locale(lang_code);
		// Config.setLocale(locale);
		// System.out.println("Config" + Config.locale);
		// Config.updateFrom(Config);
		// c.getResources().updateConfiguration(Config,
		// c.getResources().getDisplayMetrics());
		DBOHandler.createLanguageHashMap(lang_code);
	}

	public static String getLanguageString(int lang_id, Context c) {
		String language = "";
		switch (lang_id) {
			case Constants.LANGUAGE_HINDI:
				language = c.getResources().getString(R.string.lang_hindi);
				break;
			case Constants.LANGUAGE_TAMIL:
				language = c.getResources().getString(R.string.lang_tamil);
				break;
			case Constants.LANGUAGE_TELUGU:
				language = c.getResources().getString(R.string.lang_telugu);
				break;
			case Constants.LANGUAGE_PUNJABI:
				language = c.getResources().getString(R.string.lang_punjabi);
				break;
			case Constants.LANGUAGE_ENGLISH:
			default:
				language = c.getResources().getString(R.string.lang_english);
				break;
		}
		return language;
	}

	// public static String getTextFromDb(String value, Context c) {
	// ApplicationConfigurations mApplicationConfigurations = new
	// ApplicationConfigurations(
	// c);
	// DataBase db = DataBase.getInstance(c);
	// db.open();
	// String result = "";
	// String where = "str_name=(select str_name from"
	// + DataBase.All_String_Values_table
	// + "where str_value='"
	// + value
	// + "'"
	// + " AND language='English') AND language='"
	// + getLanguageString(
	// mApplicationConfigurations.getUserSelectedLanguage(), c)
	// + "'";
	// Cursor cursor = db.fetch(DataBase.All_String_Values_table,
	// DataBase.All_Strings_int, where);
	// if (cursor != null && cursor.getCount() > 0) {
	// if (cursor.moveToFirst())
	// if (cursor.getString(2) != null
	// && cursor.getString(2).length() > 0)
	// result = cursor.getString(2);
	// cursor.close();
	// } else {
	// result = value;
	// }
	// db.close();
	// return result;
	// }
	public static String getDate(long milliSeconds, String dateFormat) {
		// Create a DateFormatter object for displaying date in specified
		// format.
		DateFormat formatter = new SimpleDateFormat(dateFormat,
				Locale.getDefault());

		// Create a calendar object that will convert the date and time value in
		// milliseconds to date.
		// Calendar calendar = Calendar.getInstance();
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		calendar.setTimeInMillis(milliSeconds);
		return formatter.format(calendar.getTime());
	}

	public static final void setActionBarTitle(Context context,
											   ActionBar mActionBar, int title) {
		// mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayUseLogoEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(true);

		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View convertView = mInflater.inflate(R.layout.actionbar_header, null);

		LanguageTextView titleView = (LanguageTextView) convertView
				.findViewById(R.id.header);

		// LanguageTextView titleView = new LanguageTextView(context);
		// titleView.setLayoutParams(new LayoutParams(
		// LayoutParams.MATCH_PARENT,
		// LayoutParams.WRAP_CONTENT));
		// titleView.setGravity(Gravity.CENTER_VERTICAL);
		// titleView.setTextColor(context.getResources().getColor(R.color.white));
		// titleView.setTextSize(
		// context.getResources().getDimension(R.dimen.txt_header_size));
		// titleView.setSingleLine(true);
		// titleView.setMaxLines(1);
		// titleView.setEllipsize(TextUtils.TruncateAt.END);

		titleView.setText(Utils.getMultilanguageTextLayOut(context,
				context.getString(title)));

		mActionBar.setCustomView(convertView);

		// mActionBar.setTitle(Utils.getMultilanguageTextHindi(
		// context,
		// context.getString(title)));
	}

	public static final void setActionBarTitle(Context context,
											   ActionBar mActionBar, String title) {
		// mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayUseLogoEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(true);

		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View convertView = mInflater.inflate(R.layout.actionbar_header, null);

		LanguageTextView titleView = (LanguageTextView) convertView
				.findViewById(R.id.header);

		// LanguageTextView titleView = new LanguageTextView(context);
		// titleView.setLayoutParams(new LayoutParams(
		// LayoutParams.MATCH_PARENT,
		// LayoutParams.WRAP_CONTENT));
		// titleView.setGravity(Gravity.CENTER_VERTICAL);
		// titleView.setTextColor(context.getResources().getColor(R.color.white));
		// titleView.setTextSize(
		// context.getResources().getDimension(R.dimen.txt_header_size));
		// titleView.setSingleLine(true);
		// titleView.setMaxLines(1);
		// titleView.setEllipsize(TextUtils.TruncateAt.END);
		// if(title.equals(context.getResources().getString(R.string.application_name))){
		if (!TextUtils.isEmpty(title))
			titleView.setText(Utils.getMultilanguageTextLayOut(context, title.trim()));
		else
			titleView.setText("");
		if (TextUtils.isEmpty(title))
			titleView.setPadding(0, 0, 0, 0);
		else
			titleView.setPadding(context.getResources().getDimensionPixelOffset(R.dimen.save_offline_setting_10dp), 0, 0, 0);
		// }else
		// titleView.setText(Utils.getMultilanguageTextLayOut(context,
		// title.trim()));

		mActionBar.setCustomView(convertView);

		// mActionBar.setTitle(Utils.getMultilanguageTextHindi(
		// context, title));
	}

	public static final void setActionBarTitleSubtitle_MediaDetail(
			Context context, ActionBar mActionBar, String title, String subTitle) {

		mActionBar.setIcon(R.drawable.icon_actionbar_logo);
		mActionBar.setDisplayUseLogoEnabled(true);

		// mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		mActionBar.setDisplayShowTitleEnabled(false);
		// mActionBar.setDisplayUseLogoEnabled(true);
		// mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(true);
		// mActionBar.setDisplayHomeAsUpEnabled(true);

		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayHomeAsUpEnabled(true);

		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View convertView = mInflater.inflate(
				R.layout.actionbar_header_subtitle, null);

		LanguageTextView titleView = (LanguageTextView) convertView
				.findViewById(R.id.header);
		LanguageTextView titleView_sub = (LanguageTextView) convertView
				.findViewById(R.id.header_sub);
		//
		// LanguageTextView titleView = new LanguageTextView(context);
		// titleView.setLayoutParams(new LayoutParams(
		// LayoutParams.MATCH_PARENT,
		// LayoutParams.WRAP_CONTENT));
		// titleView.setGravity(Gravity.CENTER_VERTICAL);
		// titleView.setTextColor(context.getResources().getColor(R.color.white));
		// titleView.setTextSize(
		// context.getResources().getDimension(R.dimen.txt_header_size));
		// titleView.setSingleLine(true);
		// titleView.setMaxLines(1);
		// titleView.setEllipsize(TextUtils.TruncateAt.END);
		if (!TextUtils.isEmpty(title))
			titleView.setText(Utils.getMultilanguageTextLayOut(context, title.trim()));

		if (TextUtils.isEmpty(title))
			titleView.setPadding(0, 0, 0, 0);
		else
			titleView.setPadding(context.getResources().getDimensionPixelOffset(R.dimen.save_offline_setting_10dp), 0, 0, 0);
		// if(TextUtils.isEmpty(subTitle))
		titleView_sub.setVisibility(View.GONE);
		// else
		// titleView_sub.setText(" "+subTitle.trim());
		// if(title.equals(context.getResources().getString(R.string.application_name))){
		// titleView.setText(" "+Utils.getMultilanguageTextLayOut(context,
		// title));
		// }else
		// titleView.setText(" "+Utils.getMultilanguageTextLayOut(context,
		// title));

		mActionBar.setCustomView(convertView);
		//
		// mActionBar.setTitle(title);

		// mActionBar.setTitle(Utils.getMultilanguageTextHindi(
		// context, title));
	}

	public static final void setActionBarTitleSubtitle(Context context,
													   ActionBar mActionBar, String title, String subTitle) {

		mActionBar.setIcon(R.drawable.icon_actionbar_logo);
		mActionBar.setDisplayUseLogoEnabled(true);

		// mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		mActionBar.setDisplayShowTitleEnabled(false);
		// mActionBar.setDisplayUseLogoEnabled(true);
		// mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(true);
		// mActionBar.setDisplayHomeAsUpEnabled(true);

		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayHomeAsUpEnabled(true);

		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View convertView = mInflater.inflate(R.layout.actionbar_header, null);

		LanguageTextView titleView = (LanguageTextView) convertView
				.findViewById(R.id.header);
		//
		// LanguageTextView titleView = new LanguageTextView(context);
		// titleView.setLayoutParams(new LayoutParams(
		// LayoutParams.MATCH_PARENT,
		// LayoutParams.WRAP_CONTENT));
		// titleView.setGravity(Gravity.CENTER_VERTICAL);
		// titleView.setTextColor(context.getResources().getColor(R.color.white));
		// titleView.setTextSize(
		// context.getResources().getDimension(R.dimen.txt_header_size));
		// titleView.setSingleLine(true);
		// titleView.setMaxLines(1);
		// titleView.setEllipsize(TextUtils.TruncateAt.END);
		titleView.setText(Utils.getMultilanguageTextLayOut(context, title.trim()));
		if (TextUtils.isEmpty(title))
			titleView.setPadding(0, 0, 0, 0);
		else
			titleView.setPadding(context.getResources().getDimensionPixelOffset(R.dimen.save_offline_setting_10dp), 0, 0, 0);
		// if(title.equals(context.getResources().getString(R.string.application_name))){
		// titleView.setText(" "+Utils.getMultilanguageTextLayOut(context,
		// title));
		// }else
		// titleView.setText(" "+Utils.getMultilanguageTextLayOut(context,
		// title));

		mActionBar.setCustomView(convertView);
		//
		// mActionBar.setTitle(title);

		// mActionBar.setTitle(Utils.getMultilanguageTextHindi(
		// context, title));
	}

	public static final void setActionBarWithoutLogo(Context context,
													 ActionBar mActionBar, String title, String subTitle) {

		mActionBar.setDisplayUseLogoEnabled(false);
		// mActionBar.setIcon(new
		// ColorDrawable(context.getResources().getColor(android.R.color.transparent)));

		// mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		mActionBar.setDisplayShowTitleEnabled(false);
		// mActionBar.setDisplayUseLogoEnabled(true);
		// mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(false);
		// mActionBar.setDisplayHomeAsUpEnabled(true);

		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayHomeAsUpEnabled(true);

		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View convertView = mInflater.inflate(R.layout.actionbar_header, null);

		LanguageTextView titleView = (LanguageTextView) convertView
				.findViewById(R.id.header);

		titleView.setText(Utils.getMultilanguageTextLayOut(context, title.trim()));
		if (TextUtils.isEmpty(title))
			titleView.setPadding(0, 0, 0, 0);
		else
			titleView.setPadding(context.getResources().getDimensionPixelOffset(R.dimen.save_offline_setting_10dp), 0, 0, 0);
		// if(title.equals(context.getResources().getString(R.string.application_name))){
		// titleView.setText(" "+Utils.getMultilanguageTextLayOut(context,
		// title));
		// }else
		// titleView.setText(" "+Utils.getMultilanguageTextLayOut(context,
		// title));

		mActionBar.setCustomView(convertView);

		// mActionBar.setTitle(title);

		// mActionBar.setTitle(Utils.getMultilanguageTextHindi(
		// context, title));
	}

	// public static final void setActionBarWithoutLogoTittle(Context context,
	// ActionBar mActionBar, String title,String subTitle) {
	//
	// mActionBar.setDisplayUseLogoEnabled(false);
	// // mActionBar.setIcon(new
	// ColorDrawable(context.getResources().getColor(android.R.color.transparent)));
	//
	// // mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
	// mActionBar.setDisplayShowTitleEnabled(false);
	// // mActionBar.setDisplayUseLogoEnabled(true);
	// mActionBar.setDisplayShowCustomEnabled(false);
	// mActionBar.setDisplayShowHomeEnabled(false);
	// // mActionBar.setDisplayHomeAsUpEnabled(true);
	//
	// mActionBar.setHomeButtonEnabled(true);
	// mActionBar.setDisplayHomeAsUpEnabled(true);
	//
	//
	// // mActionBar.setTitle(title);
	//
	// // mActionBar.setTitle(Utils.getMultilanguageTextHindi(
	// // context, title));
	// }

	// public static final void customDialog(final Context context, String
	// title) {
	// // AlertDialog.Builder alert = new AlertDialog.Builder(context);
	// // LanguageTextView titleView = new LanguageTextView(context);
	// // titleView.setGravity(Gravity.CENTER_VERTICAL);
	// // titleView.setText(Utils.getMultilanguageTextLayOut(context, title));
	// //
	// titleView.setTextColor(context.getResources().getColor(R.color.white));
	// // titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
	// // alert.setView(titleView);
	// // alert.show();
	//
	// CustomAlertDialog cad = new CustomAlertDialog(context);
	// cad.setMessage(title);
	// cad.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// Toast.makeText(context, "OK button clicked", Toast.LENGTH_SHORT)
	// .show();
	// }
	// });
	// cad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// Toast.makeText(context, "Cancel button clicked",
	// Toast.LENGTH_SHORT).show();
	// }
	// });
	// cad.show();
	// }

	public static final void customDialogWithOk(final Context context,
												String title) {
		// AlertDialog.Builder alert = new AlertDialog.Builder(context);
		// LanguageTextView titleView = new LanguageTextView(context);
		// titleView.setGravity(Gravity.CENTER_VERTICAL);
		// titleView.setText(Utils.getMultilanguageTextLayOut(context, title));
		// titleView.setTextColor(context.getResources().getColor(R.color.white));
		// titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
		// alert.setView(titleView);
		// alert.show();

		CustomAlertDialog cad = new CustomAlertDialog(context);
		cad.setMessage(title);
		cad.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Toast.makeText(context, "OK button clicked",
				// Toast.LENGTH_SHORT)
				// .show();
			}
		});
		// cad.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
		// {
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// // Toast.makeText(context, "Cancel button clicked",
		// // Toast.LENGTH_SHORT).show();
		// }
		// });
		cad.show();
	}

	// public static String getSimOperator(Context context) {
	// String operatorName = "";
	// if (hasSIMcard(context)) {
	// TelephonyManager telephonyManager = ((TelephonyManager) context
	// .getSystemService(Context.TELEPHONY_SERVICE));
	// operatorName = telephonyManager.getSimOperatorName();
	// return operatorName.replace("!", "I");
	// }
	// // return "Idea";
	// return operatorName;
	// }

	public static void clearCache() {
		try {
			// System.gc();
			// System.runFinalization();
			// System.gc();
			// System.runFinalization();
			// Runtime.getRuntime().gc();
			// System.gc();
		} catch (Error e) {
			Logger.printStackTrace(e);
		}
	}

	public static void clearCache(boolean isNeedTrim) {
		if (isNeedTrim)
			try {
				System.runFinalization();
				// Runtime.getRuntime().gc();
				System.gc();
			} catch (Error e) {
				Logger.printStackTrace(e);
			}
	}

	public static String numberToStringConvert(String value, int iteration) {
		try {
			double n = Double.parseDouble(value);
			char[] c = new char[]{'k', 'm', 'b', 't'};
			double d = ((long) n / 100) / 10.0;
			boolean isRound = (d * 10) % 10 == 0;// true if the decimal part is
			// equal to 0 (then it's
			// trimmed
			// anyway)
			return (d < 1000 ? // this determines the class, i.e. 'k', 'm' etc
					((d > 99.9 || isRound || (!isRound && d > 9.99) ? // this decides
							// whether to
							// trim
							// the decimals
							(int) d * 10 / 10
							: d + "" // (int) d * 10 / 10 drops the decimal
					) + "" + c[iteration])
					: numberToStringConvert(d + "", iteration + 1));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}

	public static void unbindDrawables(View view) {
		try {
			int version = Integer.parseInt(""
					+ android.os.Build.VERSION.SDK_INT);
			unbindDrawables(view, version);
		} catch (Exception e) {
		} catch (Error e) {
		}

	}

	public static final void startReverieSDK(final Context context) {
		ThreadPoolManager.getInstance().submit(new Runnable()  {
			public void run() {
				int result = new LM(context).RegisterSDK(HomeActivity.SDK_ID);
				Logger.e("Sdk Result : ", "" + result);
//				if (result == LM.LC_INVALID) {
//					// Exit with some notification
//					// Your SDK is not valid. Check your SDK_ID and Application
//					// package
//					// name. If does not resolved please contact SDK provider.
//				} else if (result == LM.LC_NT_ERROR) {
//					// Exit with notification.
//					// Check your network connectivity. If the Internet
//					// connectivity
//					// is
//					// fine, please contact SDK provider.
//				} else if (result == LM.LC_NT_UNAVAILABLE) {
//					// notify user to enable Internet connection
//				} else if (result == LM.LC_UNKNOWN) {
//					// Exit with notification. Contact SDK provider.
//				} else if (result == LM.LC_VALID) {
//				}
			}
		});
	}

	public static final void setAlias(String email, String hardwareId) {
		String alias = "";
		// if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(hardwareId))
		// alias = email + " " + hardwareId;
		if (!TextUtils.isEmpty(email))
			alias = email;
		else// if(!TextUtils.isEmpty(hardwareId))
			alias = hardwareId;
		UAirship.shared().getPushManager().setAlias(alias);
		UAirship.shared().getPushManager().getNamedUser().setId(alias);
		Logger.e("PUSH TAGS: ", "Alias: >>>>>>>>>>>>>>>>>" + alias);
		Logger.writetofile("Alias: >>>>>>>>>>>>>>>>>" + alias, true);
	}

	// public static final SignOption getSignOption(List<SignOption>
	// signOptions, long setId){
	// if(!isListEmpty(signOptions)){
	// for(SignOption signOption : signOptions){
	// if(signOption.getSetID()==setId)
	// return signOption;
	// }
	// }
	// return null;
	// }

	public static String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}

	private static String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}

	public static final boolean isDeviceAirplaneModeActive(Context context) {
		int mode = 0;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			mode = Settings.System.getInt(context.getContentResolver(),
					Settings.System.AIRPLANE_MODE_ON, 0);
		} else {
			mode = Settings.Global.getInt(context.getContentResolver(),
					Settings.Global.AIRPLANE_MODE_ON, 0);
		}
		Logger.s("Airplane Mode :::: " + mode);
		return (mode == 1);
	}

	/*
	 * public static void displayActualImage(ImageView iv, String imageurl,
	 * Context context, int loadingImg) {
	 * 
	 * UrlImageViewHelper.setUrlDrawable(iv, imageurl, loadingImg, new
	 * UrlImageViewCallback() {
	 * 
	 * @Override public void onLoaded(ImageView imageView, Bitmap loadedBitmap,
	 * String url, boolean loadedFromCache) { // if (!loadedFromCache) { //
	 * ScaleAnimation scale = new ScaleAnimation(0, 1, 0, // 1,
	 * ScaleAnimation.RELATIVE_TO_SELF, .5f, // ScaleAnimation.RELATIVE_TO_SELF,
	 * .5f); // scale.setDuration(300); // scale.setInterpolator(new
	 * OvershootInterpolator()); // imageView.startAnimation(scale); // } } });
	 * 
	 * // DisplayImageOptions options; // if (loadingImg != 0) { // options =
	 * new DisplayImageOptions.Builder().cacheInMemory(true) // //
	 * .showImageOnLoading(loadingImg).showImageForEmptyUri(loadingImg) //
	 * .showImageOnFail(loadingImg).cacheOnDisk(true) //
	 * .considerExifParams(true) //
	 * .bitmapConfig(Bitmap.Config.ARGB_8888).build(); // } else { // options =
	 * new DisplayImageOptions.Builder().cacheInMemory(true) //
	 * .cacheOnDisk(true).considerExifParams(true) //
	 * .bitmapConfig(Bitmap.Config.ARGB_8888).build(); // } //
	 * ImageLoaderInstance.getImageLoader(context).displayImage(imageurl, iv, //
	 * options); }
	 */

//	public static String getTempFileName(String trackId) {
//		String fileName = "audio_" + trackId + CacheManager.TEMP_TRACK_FORMAT;
//		return fileName;
//	}
//
//	public static String getTempDirectoryPath() {
//		boolean encrypt = true;
//		return CacheManager.getTempCacheTracksFolderPath(encrypt);
//	}
//
//	public static String getTempFilePath(Track track) {
//		String cachingTracksFolder = getTempDirectoryPath();
//		String baseFileName = getTempFileName(track.getId() + "");
//		File fileFinal = new File(cachingTracksFolder, baseFileName);
//		String filePath = fileFinal.getAbsolutePath();
//		Logger.i("Utils", "Temp File path:" + filePath);
//		return filePath;
//	}
//
//	public static boolean isTempFileExist(Track track) {
//		String filePath = getTempFilePath(track);
//		if (filePath != null)
//			return new File(filePath).exists();
//		else
//			return false;
//	}

	// public static void removeAllTempFiles() {
	// String tempDirectoryPath = getTempDirectoryPath();
	// File dir = new File(tempDirectoryPath);
	// if (dir.isDirectory()) {
	// String[] children = dir.list();
	// for (int i = 0; i < children.length; i++) {
	// new File(dir, children[i]).delete();
	// }
	// }
	// }

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void unbindDrawables(View view, int version) {
		try {
			if (view.getBackground() != null) {
				view.getBackground().setCallback(null);
				if (version >= 16)
					view.setBackground(null);
				else
					view.setBackgroundDrawable(null);
			} else if (view instanceof ImageView) {
				if (version >= 16)
					view.setBackground(null);
				else
					view.setBackgroundDrawable(null);
				((ImageView) view).setImageDrawable(null);
			} else if (view instanceof ImageButton) {
				if (version >= 16)
					view.setBackground(null);
				else
					view.setBackgroundDrawable(null);
				((ImageButton) view).setImageDrawable(null);
			}
			if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
				for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
					unbindDrawables(((ViewGroup) view).getChildAt(i), version);
				}
				((ViewGroup) view).removeAllViews();
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		} catch (Error e) {
			Logger.printStackTrace(e);
		}
		// System.runFinalization();
		// System.gc();
	}

	public static boolean isNeedToUseHLS() {
		return (android.os.Build.VERSION.SDK_INT >= 16);
	}

	public static Bitmap fastblur1(Bitmap input, int radius, Context context) {
		try {
			// input = getResizedBitmap(input, input.getWidth() / 2,
			// input.getHeight() / 2);
			// System.gc();
			RenderScript rsScript = RenderScript.create(context);
			Allocation alloc = Allocation.createFromBitmap(rsScript, input);

			ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rsScript,
					Element.U8_4(rsScript));
			blur.setRadius(radius);
			blur.setInput(alloc);
			Bitmap result;

			try {
				result = Bitmap.createBitmap(input.getWidth(),
						input.getHeight(), Utils.bitmapConfig8888);
			} catch (OutOfMemoryError e) {
				result = Bitmap.createBitmap(input.getWidth() / 2,
						input.getHeight() / 2, Utils.bitmapConfig8888);
			}
			Allocation outAlloc = Allocation.createFromBitmap(rsScript, result);

			blur.forEach(outAlloc);
			outAlloc.copyTo(result);

			rsScript.destroy();
			return result;
		} catch (Exception e) {
			Logger.printStackTrace(e);
			return input;
		}
	}

	// public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int
	// newWidth) {
	// int width = bm.getWidth();
	// int height = bm.getHeight();
	// float scaleWidth = ((float) newWidth) / width;
	// float scaleHeight = ((float) newHeight) / height;
	// // CREATE A MATRIX FOR THE MANIPULATION
	// Matrix matrix = new Matrix();
	// // RESIZE THE BIT MAP
	// matrix.postScale(scaleWidth, scaleHeight);
	//
	// // "RECREATE" THE NEW BITMAP
	// Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
	// matrix, false);
	// return resizedBitmap;
	// }

	public static int getNetworkBandwidth(Context mContext) {
		DataManager mDataManager = DataManager.getInstance(mContext);
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		String networkType = Utils.getNetworkType(mContext);
		if (!TextUtils.isEmpty(networkType)) {
			long bandwidth = mApplicationConfigurations.getBandwidth();
			if (networkType.equalsIgnoreCase(Utils.NETWORK_WIFI)
					|| networkType.equalsIgnoreCase(Utils.NETWORK_3G)
					|| networkType.equalsIgnoreCase(Utils.NETWORK_4G)) {
				if (bandwidth == 0) {
					Logger.i(
							TAG,
							networkType
									+ " - First Time - No bandwidth. Bitrate should be 64");
					return -1; // -1 = bitrate 64
				} else {
					Logger.i(TAG, networkType + " - Bandwidth from previous = "
							+ bandwidth);
					return (int) bandwidth;
				}
			} else if (networkType.equalsIgnoreCase(Utils.NETWORK_2G)) {
				return 0; // 0 - bitrate = 32
			}
		}
		Logger.i(TAG, "Not WIFI & Not Mobile - bitrate = 32");
		return 0; // Not WIFI & Not Mobile - bitrate = 32
	}

	public static final boolean isValidPhoneNumber(String target) {
		// Pattern sameDigits = Pattern.compile("(\\d)(\\1){3,}");
		if (target == null || TextUtils.isEmpty(target)) {
			return false;
		} else {
			return target.matches("(\\d)(?!\\1+$)\\d{9}");
			// return android.util.Patterns.PHONE.matcher(target).matches();
		}
	}

	public static View getParentViewCustom(View rootView) {
		return (View) rootView.getRootView();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void setToolbarColor(MainActivity mainActivity) {
		if (mainActivity != null && mainActivity.mToolbar != null) {
			ColorDrawable cd = new ColorDrawable(mainActivity.getResources().getColor(
					R.color.myPrimaryColor));
			cd.setAlpha(255);
			int sdk = android.os.Build.VERSION.SDK_INT;
			if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
				mainActivity.mToolbar.setBackgroundDrawable(cd);
			} else {
				mainActivity.mToolbar.setBackground(cd);
			}
		}
	}

	public static Location getLocation(Context context) {
		if (Utils.isAndroidM()) {
			if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
					context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				return null;
			}
		}
		LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
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
		return l;
	}

	public static void destroyFragment(){
		Utils.clearCache(true);
	}

	public static boolean isAllPermissionsGranted(Context context){
		if (Utils.isAndroidM()) {
			return (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
					context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
					context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
					context.checkSelfPermission(Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED);
		}
		return  true;
	}

	public static boolean isAndroidM(){
		return  /*Logger.enableAndroidM_Permission && */Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
	}

	public static boolean isCarMode(){
		return MySpinServerSDK.sharedInstance().isConnected();
		//return true;
	}

	private static  boolean isFordCarMode;

	public static  void setFordCarMode(boolean fordCarMode) {
		isFordCarMode = fordCarMode;
	}

	public static boolean isFordCarMode() {

		return isFordCarMode;
	}

	/*public static MediaInfo buildMediaInfo(String title, String studio, String subTitle,
										   int duration, String url, String mimeType, String imgUrl, long id, Context context, long deliveryId) {
		MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
		//url = "http://akdls3re.hungama.com/s3/r/ms2/15760068/4/8/Welcome_Back-Meet_Me_Daily_Baby.mp3?__gda__=1441972535_2f9a7aad34ea393f1bcf86a6082b5a25";
		movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, studio);
		movieMetadata.putString(MediaMetadata.KEY_TITLE, title);
        if(imgUrl!=null){
            movieMetadata.addImage(new WebImage(Uri.parse(imgUrl)));
			movieMetadata.addImage(new WebImage(Uri.parse(imgUrl)));
//			movieMetadata.addImage(new WebImage(Uri.parse(imgUrl)));
        }

		//movieMetadata.addImage(new WebImage(Uri.parse(imgUrl)));
		JSONObject jsonObj = null;
		try {
			jsonObj = new JSONObject();
			jsonObj.put("description", subTitle);
			jsonObj.put(MainActivity.IS_VIDEO, "1");
			jsonObj.put(MainActivity.ITEM_ID, id);
			jsonObj.put(MainActivity.ITEM_TITLE, title);
			jsonObj.put(MainActivity.DEVICE_ID, getAndroidId(context));
			jsonObj.put(MainActivity.DELIVERY_ID, deliveryId+"");
		} catch (JSONException e) {

		}
		try {
			url = URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return new MediaInfo.Builder(url)
				.setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
				.setContentType(mimeType)

				.setMetadata(movieMetadata)
						//.setMediaTracks(tracks)
				//.setStreamDuration(duration)
				.setCustomData(jsonObj)
				.build();
	}

public static String getAndroidId(Context context) {
		final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String deviceId = tm.getDeviceId();
		return deviceId;
	}

	public static String getDeviceId(MediaInfo mediaInfo) {
		String deviceId = "";
		try {
			JSONObject customData = mediaInfo.getCustomData();
			if (customData != null) {
				deviceId = customData.getString(MainActivity.DEVICE_ID);
			}
		} catch (JSONException e) {

		}
		return deviceId;
	}

	public static String getVideoMimeType(){
		return "video/mp4";*//*"application/x-mpegurl"*//*
	}*/

	public static boolean isTablet(Activity act) {
		Display display = act.getWindow().getWindowManager().getDefaultDisplay();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		display.getMetrics(displayMetrics);

		float width = displayMetrics.widthPixels / displayMetrics.xdpi;
		float height = displayMetrics.heightPixels / displayMetrics.ydpi;

		double screenDiagonal = Math.sqrt(width * width + height * height);
		int inch = (int) (screenDiagonal + 0.5);
		Logger.s("inch : " + inch);
		return (inch >= 7);
	}

	public static void updateUserPointUATag(long points)
	{

	}

	public static boolean isNeedToUseHLSForMusic() {
		return android.os.Build.VERSION.SDK_INT >= 16;
	}

}