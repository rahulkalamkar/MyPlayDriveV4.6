package com.hungama.myplay.activity.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.ui.widgets.CustomAlertDialog;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Utils;

import java.util.HashMap;
import java.util.Map;

public class OfflineAlertActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final ApplicationConfigurations appConfig = ApplicationConfigurations
				.getInstance(OfflineAlertActivity.this);
		if (appConfig.getSaveOfflineAutoModeRemember()) {
			Utils.makeText(OfflineAlertActivity.this,
					getString(R.string.message_offline_switching_no_internet),
					Toast.LENGTH_SHORT).show();

			appConfig.setSaveOfflineAutoMode(true);
			appConfig.setSaveOfflineAutoModeRemember(true);
			sendBroadcast(new Intent(MainActivity.ACTION_OFFLINE_MODE_CHANGED));

			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(FlurryConstants.FlurryCaching.Source.toString(),
					FlurryConstants.FlurryCaching.NoInternetPrompt.toString());
			reportMap.put(FlurryConstants.FlurryCaching.UserStatus.toString(),
					Utils.getUserState(OfflineAlertActivity.this));
			Analytics.logEvent(
					FlurryConstants.FlurryCaching.GoOffline.toString(),
					reportMap);
			finish();
		} else {
			final CustomAlertDialog dialog = new CustomAlertDialog(this);
			dialog.setMessage(R.string.message_offline_switching_airplane_mode);
			dialog.setPositiveButton(R.string.settings_title,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startActivity(new Intent(
									android.provider.Settings.ACTION_SETTINGS));
							finish();
						}
					});
			dialog.setNegativeButton(
					R.string.connection_error_empty_view_button_play_offline,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog1, int which) {
							// handleOfflineSwitchCase(true);
							// internetConnectivityPopup(null);
							Utils.makeText(
									OfflineAlertActivity.this,
									getString(R.string.message_offline_switching_no_internet),
									Toast.LENGTH_SHORT).show();

							appConfig.setSaveOfflineAutoMode(true);
							appConfig.setSaveOfflineAutoModeRemember(dialog
									.getRememberCheckState());
							sendBroadcast(new Intent(
									MainActivity.ACTION_OFFLINE_MODE_CHANGED));

							Map<String, String> reportMap = new HashMap<String, String>();
							reportMap.put(
									FlurryConstants.FlurryCaching.Source
											.toString(),
									FlurryConstants.FlurryCaching.NoInternetPrompt
											.toString());
							reportMap.put(
									FlurryConstants.FlurryCaching.UserStatus
											.toString(),
									Utils.getUserState(OfflineAlertActivity.this));
							Analytics.logEvent(
									FlurryConstants.FlurryCaching.GoOffline
											.toString(), reportMap);
							finish();
						}
					});
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					finish();
				}
			});
			dialog.setRememberCheckVisibility(true);
			dialog.show();
		}
	}
}
