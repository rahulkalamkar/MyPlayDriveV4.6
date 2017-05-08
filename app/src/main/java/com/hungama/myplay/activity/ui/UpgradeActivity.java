package com.hungama.myplay.activity.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Controller for presenting details of the given MediaItem.
 */
public class UpgradeActivity extends Activity {

	public static final int LOGIN_ACTIVITY_CODE = 125;

	public static final String PASSWORD_SMS_SENT = "1";
	public static final String VERIFICATION_CODE_DELIVERED = "4";

	public static final String IS_TRIAL_PLANS = "is_trial";

	public static final String ARGUMENT_UPGRADE_ACTIVITY = "argument_upgrade_activity";
	public static final String EXTRA_DATA_ORIGIN_MEDIA_CONTENT_TYPE = "extra_data_origin_media_content_type";
	public static final String EXTRA_DATA_ORIGIN_MEDIA_ITEM = "extra_data_origin_media_item";
	public static final String EXTRA_DATA_ORIGIN_MEDIA_TRACK = "extra_data_origin_media_track";
	public static final String EXTRA_IS_GO_OFFLINE = "is_go_offline";
	public static final String EXTRA_IS_FROM_NO_INTERNET_PROMT = "is_from_no_internet_prompt";

	// ======================================================
	// Activity life-cycle callbacks.
	// ======================================================

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = new Intent(this, HungamaPayActivity.class);
		intent.putExtra(EXTRA_IS_GO_OFFLINE, getIntent().getBooleanExtra(UpgradeActivity.EXTRA_IS_GO_OFFLINE, false));
		intent.putExtra(EXTRA_IS_FROM_NO_INTERNET_PROMT, getIntent().getBooleanExtra(UpgradeActivity.
				EXTRA_IS_FROM_NO_INTERNET_PROMT, false));
		startActivity(intent);
		finish();
	}
}
