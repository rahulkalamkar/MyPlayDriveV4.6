/**
 * 
 */
package com.hungama.myplay.activity.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.PlaylistIdResponse;
import com.hungama.myplay.activity.gcm.IntentReceiver;
import com.hungama.myplay.activity.operations.hungama.PlaylistIdOperation;
import com.hungama.myplay.activity.ui.dialogs.MyProgressDialog;
import com.hungama.myplay.activity.util.ApsalarEvent;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.ScreenLockStatus;

import java.util.List;
import java.util.Map;

/**
 * @author XTPL
 * 
 */
@SuppressWarnings("ucd")
public class BrowserIntentReceiverActivity extends Activity implements
		CommunicationOperationListener {

	public static Uri data = null;

	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Logger.s("Uri ::::::: BrowserIntentReceiverActivity");
		ApplicationConfigurations appConfig = ApplicationConfigurations
				.getInstance(this);
		String sessionId = appConfig.getSessionID();
		String passkey = appConfig.getPasskey();
		Logger.i("BrowserIntentReceiverActivity","BrowserIntentReceiverActivity sessionId:"+sessionId);
		Logger.i("BrowserIntentReceiverActivity","BrowserIntentReceiverActivity passkey:"+passkey);
		if (sessionId == null
				|| (sessionId != null && (sessionId.length() == 0
						|| sessionId.equalsIgnoreCase("null") || sessionId
							.equalsIgnoreCase("none")))) {
			try {
				Logger.i("BrowserIntentReceiverActivity","BrowserIntentReceiverActivity 1");
				if (passkey == null
						|| (passkey != null && (passkey.length() == 0
								|| passkey.equalsIgnoreCase("null") || passkey
									.equalsIgnoreCase("none")))) {
					Logger.i("BrowserIntentReceiverActivity", "BrowserIntentReceiverActivity 2");
					Intent intent = new Intent(this,
							OnApplicationStartsActivity.class);
					intent.putExtra("is_deeplink", true);
					data = getIntent().getData();
					super.startActivityForResult(intent, 1001);
					return;
				}
				Logger.i("BrowserIntentReceiverActivity","BrowserIntentReceiverActivity 3");
			} catch (Exception e) {
				e.printStackTrace();
				Logger.i("BrowserIntentReceiverActivity", "BrowserIntentReceiverActivity 4");
			}
		}
		handleDeepLink();
	}

	private void handleDeepLink() {
		try {

			if(data == null)
				data = getIntent().getData();

            Logger.s("Uri ::::::: " + data.toString());
			if (data != null && ApsalarEvent.ENABLED) {
				try {
//					String adxid = data.getQueryParameter("ADXID");
//					if (adxid != null && adxid.length() > 0) {
//						AdXConnect.getAdXConnectEventInstance(
//								getApplicationContext(), "DeepLinkLaunch",
//								adxid, "");
//					}
					ApsalarEvent.postEvent(getApplicationContext(), ApsalarEvent.DEEP_LINK_LAUNCH);
				} catch (Exception e) {
					Logger.printStackTrace(e);
				} catch (Error e) {
					Logger.printStackTrace(e);
				}
			}
            Logger.s("Uri ::::::: " + data.toString());
			String scheme = data.getScheme();
			// String host = data.getHost();
			List<String> params = data.getPathSegments();
			int startIndex = 0;
			if (params.get(0).equalsIgnoreCase("hungama")
					&& params.get(1).equalsIgnoreCase("www.hungama.com")) {
				startIndex = 2;
			}
			String first = null;
			String second = null;
			String third = null;
			if(params.size()>startIndex)
				first = params.get(startIndex++);
			if(params.size()>startIndex)
				second = params.get(startIndex++);
			if(params.size()>startIndex)
				third = params.get(startIndex++);
			if(third!=null && third.contains("?")){
				third = third.substring(0, third.indexOf('?'));
			}
			Logger.s(scheme + " ::::::: " + first + " :: " + second + " ::: "
					+ third);

			Intent intent = new Intent(this, AlertActivity.class);
			if (first.equalsIgnoreCase("music") && second.startsWith("album")) {
				intent.putExtra(IntentReceiver.CODE, "7");
				intent.putExtra(IntentReceiver.CONTENT_TYPE, "1");
				intent.putExtra(IntentReceiver.CONTENT_ID, third);
				intent.putExtra(IntentReceiver.ARTIST_ID, "");
			} else if (first.equalsIgnoreCase("music")
					&& second.startsWith("playlists")) {
				// setContentView(R.layout.application_splash_layout);
				intent.putExtra(IntentReceiver.CODE, "7");
				intent.putExtra(IntentReceiver.CONTENT_TYPE, "2");
				intent.putExtra(IntentReceiver.CONTENT_ID, third);
				intent.putExtra(IntentReceiver.ARTIST_ID, "");
				DataManager mDataManager = DataManager.getInstance(this);
				String playlistKey = third
						.substring(third.lastIndexOf('-') + 1);
				mDataManager.getPlaylistId(playlistKey, this);
				return;
			} else if (first.equalsIgnoreCase("music")) {
				intent.putExtra(IntentReceiver.CODE, "7");
				intent.putExtra(IntentReceiver.CONTENT_TYPE, "0");
				intent.putExtra(IntentReceiver.CONTENT_ID, third);
				intent.putExtra(IntentReceiver.ARTIST_ID, "");
			} else if (first.equalsIgnoreCase("videos")) {
				intent.putExtra(IntentReceiver.CODE, "8");
				intent.putExtra(IntentReceiver.CONTENT_ID, third);
				intent.putExtra(IntentReceiver.ARTIST_ID, "");
			} else if (first.equalsIgnoreCase("artists")) {
				intent.putExtra(IntentReceiver.CODE, "14");
				intent.putExtra(IntentReceiver.ARTIST_ID, third);
			} else if (first.equalsIgnoreCase("ondemand")) {
				intent.putExtra(IntentReceiver.CODE, "44");
				intent.putExtra(IntentReceiver.STATION_ID, third);
			} else if (first.equalsIgnoreCase("deeplink")) {
				if(second.startsWith("payment")) {
					intent.putExtra(IntentReceiver.CODE, "20");
					intent.putExtra(IntentReceiver.CONTENT_TYPE, "");
					intent.putExtra(IntentReceiver.CONTENT_ID, 0);
					intent.putExtra(IntentReceiver.ARTIST_ID, "");
				} else if(second.startsWith("video-page")) {
					intent.putExtra(IntentReceiver.CODE, "4");
					intent.putExtra(IntentReceiver.CONTENT_TYPE, "");
					intent.putExtra(IntentReceiver.CONTENT_ID, 0);
					intent.putExtra(IntentReceiver.ARTIST_ID, "");
				} else if(second.startsWith("radio-page")) {
					intent.putExtra(IntentReceiver.CODE, "11");
					intent.putExtra(IntentReceiver.CONTENT_TYPE, "");
					intent.putExtra(IntentReceiver.CONTENT_ID, 0);
					intent.putExtra(IntentReceiver.ARTIST_ID, "");
				} else if(second.startsWith("settings-page")) {
					intent.putExtra(IntentReceiver.CODE, "27");
					intent.putExtra(IntentReceiver.CONTENT_TYPE, "");
					intent.putExtra(IntentReceiver.CONTENT_ID, 0);
					intent.putExtra(IntentReceiver.ARTIST_ID, "");
				} else if(second.startsWith("login")) {
					intent.putExtra(IntentReceiver.CODE, "-1");
					intent.putExtra(IntentReceiver.CONTENT_TYPE, "");
					intent.putExtra(IntentReceiver.CONTENT_ID, 0);
					intent.putExtra(IntentReceiver.ARTIST_ID, "");
				}
			}
			intent.putExtra(IntentReceiver.CATEGORY, "");
			intent.putExtra(IntentReceiver.CHANNEL_INDEX, "");
			// intent.putExtra(IntentReceiver.CONTENT_ID, intent);
			// intent.putExtra(IntentReceiver.CONTENT_TYPE, intent);
			intent.putExtra(AlertActivity.IS_CUSTOM_RICHPUSH_LINK, true);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		} catch (Exception e) {
			Logger.s(" Exception :::::::::: " + e);
			Intent intent = new Intent(this, AlertActivity.class);
			intent.putExtra(IntentReceiver.CODE, "0");
			intent.putExtra(AlertActivity.IS_CUSTOM_RICHPUSH_LINK, true);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
		data = null;
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1001 && resultCode == RESULT_OK) {
			handleDeepLink();
		} else {
			finish();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		ScreenLockStatus.getInstance(getBaseContext()).onStop();

	}

	@Override
	protected void onUserLeaveHint() {
		ScreenLockStatus.getInstance(getBaseContext()).onStop(true, this);
		super.onUserLeaveHint();
	}

	@Override
	protected void onResume() {
		ScreenLockStatus.getInstance(getBaseContext()).onResume(this, this);
		super.onResume();
	}

	MyProgressDialog pd;

	@Override
	public void onStart(int operationId) {
		if (pd == null)
			pd = new MyProgressDialog(this);
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		PlaylistIdResponse playlistIdResponse = (PlaylistIdResponse) responseObjects
				.get(PlaylistIdOperation.RESULT_KEY_OBJECT_PLAYLIST_ID);
		String scheme = data.getScheme();
		// String host = data.getHost();
		List<String> params = data.getPathSegments();
		int startIndex = 0;
		if (params.get(0).equalsIgnoreCase("hungama")
				&& params.get(1).equalsIgnoreCase("www.hungama.com")) {
			startIndex = 2;
		}
		String first = params.get(startIndex++);
		String second = params.get(startIndex++);
		String third = params.get(startIndex++);
		if(third!=null && third.contains("?")){
			third = third.substring(0, third.indexOf('?'));
		}
		Logger.s(scheme + " ::::::: " + first + " :: " + second + " ::: "
				+ third);

		Intent intent = new Intent(this, AlertActivity.class);
		if (first.equalsIgnoreCase("music") && second.startsWith("playlists")) {
			intent.putExtra(IntentReceiver.CODE, "7");
			intent.putExtra(IntentReceiver.CONTENT_TYPE, "2");
			intent.putExtra(IntentReceiver.CONTENT_ID,
					"" + playlistIdResponse.getPlaylistId());
			intent.putExtra(IntentReceiver.ARTIST_ID, "");
		}
		intent.putExtra(IntentReceiver.CATEGORY, "");
		intent.putExtra(IntentReceiver.CHANNEL_INDEX, "");
		// intent.putExtra(IntentReceiver.CONTENT_ID, intent);
		// intent.putExtra(IntentReceiver.CONTENT_TYPE, intent);
		intent.putExtra(AlertActivity.IS_CUSTOM_RICHPUSH_LINK, true);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		data = null;
		if (pd != null)
			pd.dismiss();
		finish();
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		if (pd != null)
			pd.dismiss();
		data = null;
	}
}
