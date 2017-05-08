package com.hungama.myplay.activity.data.configurations;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.util.Logger;

import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class DeviceConfigurations {

	private Context mContext;
	private static DeviceConfigurations sIntance;

	private DeviceConfigurations(Context context) {
		mContext = context;
	}

	public static final synchronized DeviceConfigurations getInstance(
			Context applicationContext) {
		if (sIntance == null) {
			if (applicationContext != null)
				sIntance = new DeviceConfigurations(
						applicationContext.getApplicationContext());
		}
		return sIntance;
	}

	public void destroyDeviceConfig(){
		sIntance=null;
	}

	public static final String TIMESTAMP = "timestamp";
	public static final String DEVICE_MODEL_NAME = "device_model_name";
	public static final String HARDWARE_ID = "hardware_id";
	public static final String HARDWARE_ID_TYPE = "hardware_id_type";
	public static final String DEVICE_OS = "device_os";
	public static final String DEVICE_OS_DESCRIPTION = "device_os_description";

	public final String ANDROID = "Android";

	public enum DeviceHardwareIdType {
		IMEI("imei"), MAC_ADDRESS("mac addr"), SERIAL_ID("serial_id"), ANDROID_ID(
				"android_id");

		private final String name;

		DeviceHardwareIdType(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
	}

	// xtpl
	private static final SimpleDateFormat sUTCTimeFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);// Changes
														// by
														// Hungama
	// xtpl
	static {
		sUTCTimeFormat.setTimeZone(TimeZone.getTimeZone("utc"));
	}

	private String mDeviceModelName = null;
	private String mHardwareId = null;
	private DeviceHardwareIdType mDeviceHardwareIdType = null;

	// private static final String EMPTY = "empty";
	// private String mDevicePhoneNumber = EMPTY;

	/**
	 * Retrieves the current device time stamp formatted as UTC time zone:</br>
	 * yyyy-MM-dd'T'HH:mm:ss'Z'
	 */
	public String getTimeStamp() {
		return sUTCTimeFormat.format(System.currentTimeMillis());
	}

	public String getTimeStampDelta() {
		long delta = DataManager.getInstance(mContext)
				.getApplicationConfigurations().getTimeReadDelta();
		return sUTCTimeFormat.format(System.currentTimeMillis() + delta);
	}

	public String getDeviceModelName() {
		if (mDeviceModelName == null) {
			if (Build.MANUFACTURER != null)
				mDeviceModelName = Build.MANUFACTURER.toString();
			if (Build.MODEL != null) {
				if (!TextUtils.isEmpty(mDeviceModelName))
					mDeviceModelName += " ";
				mDeviceModelName += Build.MODEL.toString();
			}
			if (Build.PRODUCT != null) {
				if (!TextUtils.isEmpty(mDeviceModelName))
					mDeviceModelName += " ";
				mDeviceModelName += "(" + Build.PRODUCT.toString() + ")";
			}
		}
		return mDeviceModelName;
	}

	public String getHardwareId() {

		if (mHardwareId == null) {
			try {
				// First we try to get the device telephony id, assuming that it
				// has a
				// gsm module.
				TelephonyManager telephonyManager = (TelephonyManager) mContext
						.getSystemService(Context.TELEPHONY_SERVICE);
				/*
				 * If the device has a telephony device id, that will be enough
				 * for identification.
				 */
				if (telephonyManager != null
						&& !TextUtils.isEmpty(telephonyManager.getDeviceId())) {
					mHardwareId = telephonyManager.getDeviceId();

				} else {
					/*
					 * The device doesn't have any telephony device id, it means
					 * that it will be the mac address
					 */
					WifiManager wimanager = (WifiManager) mContext
							.getSystemService(Context.WIFI_SERVICE);
					String macAddress = wimanager.getConnectionInfo()
							.getMacAddress();
					if (macAddress == null)
						macAddress = "";
					mHardwareId = macAddress;
				}
			} catch (Exception e) {
				Logger.printStackTrace(e);
				mHardwareId = null;
			}

			if (TextUtils.isEmpty(mHardwareId)) {
				mHardwareId = android.os.Build.SERIAL;
				mDeviceHardwareIdType = DeviceHardwareIdType.SERIAL_ID;
			}

			if (TextUtils.isEmpty(mHardwareId)) {
				mHardwareId = Secure.getString(mContext.getContentResolver(),
						Secure.ANDROID_ID);
				mDeviceHardwareIdType = DeviceHardwareIdType.ANDROID_ID;
			}
		}

		return mHardwareId;// "64551335898452230";//"645513358984522310";//"6455133589845";//
	}

	public String getHardwareIdType() {

		if (mDeviceHardwareIdType == null) {
			// First we try to get the device telephony id, assuming that it has
			// a
			// gsm module.
			try {


				TelephonyManager telephonyManager = (TelephonyManager) mContext
						.getSystemService(Context.TELEPHONY_SERVICE);
			/*
			 * If the device has a telephony device id, that will be enough for
			 * identification.
			 */
			if (telephonyManager != null
					&& !TextUtils.isEmpty(telephonyManager.getDeviceId())) {
				mDeviceHardwareIdType = DeviceHardwareIdType.IMEI;
			} else {
				/*
				 * The device doesn't have any telephony device id, it means
				 * that it will be the mac address
				 */
					mDeviceHardwareIdType = DeviceHardwareIdType.MAC_ADDRESS;
				}
			} catch (Exception e) {
				mDeviceHardwareIdType = DeviceHardwareIdType.MAC_ADDRESS;
			}
		}

		return mDeviceHardwareIdType.getName();
	}

	public String getDeviceOS() {
		return ANDROID; // Build.VERSION.RELEASE;
	}

	public String getDeviceOSDescription() {
		return Build.VERSION.RELEASE;
	}

	/**
	 * If no phone number available, retrieves null; Removes the "+" prefix if
	 * exist.
	 */
	public String getDevicePhoneNumber() {
		try {


			TelephonyManager tMgr = (TelephonyManager) mContext
					.getSystemService(Context.TELEPHONY_SERVICE);

			String phoneNumber = tMgr.getLine1Number();

			if (!TextUtils.isEmpty(phoneNumber)) {
				phoneNumber = phoneNumber.replace("+", "");
			}

			return phoneNumber;
		} catch (Exception e) {
		}
		return null;
		// for testing on ugly devices.
		// return "4774839560";
	}

	public static String ua;

	public String getDefaultUserAgentString(final Context activity,
			Handler handle) {
		if (ua != null)
			return ua;

		if (Build.VERSION.SDK_INT >= 17) {
			ua = NewApiWrapper.getDefaultUserAgent(activity);
			if (ua == null)
				ua = getDeviceModelName();
			return ua;
		}

		try {
			Constructor<WebSettings> constructor = WebSettings.class
					.getDeclaredConstructor(Context.class, WebView.class);
			constructor.setAccessible(true);
			try {
				WebSettings settings = constructor.newInstance(activity, null);
				ua = settings.getUserAgentString();
				if (ua == null)
					ua = getDeviceModelName();
				return ua;
			} finally {
				constructor.setAccessible(false);
			}
		} catch (Exception e) {
			try {
				// return new WebView(context).getSettings().getUserAgentString();
				if (Thread.currentThread().getName().equalsIgnoreCase("main")) {
					WebView m_webview = new WebView(activity);
					return m_webview.getSettings().getUserAgentString();
				} else {
					final Object runObj = new Object();
					Runnable runnable = new Runnable() {
						@Override
						public void run() {
							// Looper.prepare();
							WebView m_webview = new WebView(activity);
							ua = m_webview.getSettings().getUserAgentString();
							synchronized (runObj) {
								runObj.notifyAll();
							}
							// Looper.loop();
						}
					};

					// mContext = context;
					synchronized (runObj) {
						try {
							handle.post(runnable);
							runObj.wait();
						} catch (InterruptedException e1) {
							e1.printStackTrace();
							Logger.e("Device Config", "run sync" + e1);
						}
					}
					if (ua == null)
						ua = getDeviceModelName();
					return ua;

				}	
			} catch (Error e2) {
				// TODO: handle exception
			}
			
		}
		return "";
	}

	// @TargetApi(17)
	static class NewApiWrapper {
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
		static String getDefaultUserAgent(Context context) {

			try {
				return WebSettings.getDefaultUserAgent(context);
			} catch (Exception e) {
			}
			return null;
		}
	}

	public String getMac(Context context) {
		try {
			WifiManager wimanager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			return wimanager.getConnectionInfo().getMacAddress();
		} catch (Exception e) {
		}
		return "";// getDeviceModelName();
	}
}
