package com.hungama.myplay.activity.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.Window;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.gigya.TwitterLoginFragment;
import com.hungama.myplay.activity.ui.fragments.MembershipDetailsFragment;
import com.hungama.myplay.activity.ui.fragments.SettingFragmentNew;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SettingsActivity extends SecondaryActivity implements
		ServiceConnection {

	private final String TAG = "SettingsActivity";

	public static final String ARGUMENT_SETTINGS_ACTIVITY = "argument_settings_activity";
	public static final int LOGIN_ACTIVITY_CODE = 1;

	private OfflineModeReceiver offlineModeReceiver;
	public static boolean ReoladHomeScreen = false;

	// private SettingsFragment settingsFragment;
	public boolean isAudioSetting;
	public Stack<String> stack_text = new Stack<String>();
	private boolean isShowMembership = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		onCreateCode();
		ReoladHomeScreen = false;

		isAudioSetting = getIntent().getBooleanExtra("isAudioSetting", false);

		// Invite Friends Fragment
		FragmentManager mFragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();
		if (getIntent().getBooleanExtra("show_membership", false)) {
//			getIntent().removeExtra("show_membership");
//			addMembershipDetailsFragment();
//			isShowMembership = true;
			ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations.getInstance(this);
			if (mApplicationConfigurations.isUserHasTrialSubscriptionPlan()) {
			} else if (mApplicationConfigurations.isUserHasSubscriptionPlan()) {
				MembershipDetailsFragment membershipDetailsFragment = new MembershipDetailsFragment();
				fragmentTransaction.replace(R.id.main_fragmant_container,
						membershipDetailsFragment);
//			fragmentTransaction.addToBackStack(null);
				fragmentTransaction.commit();
				isShowMembership = true;
			} else {
				// Flurry report: upgrade clicked
				Boolean loggedIn = mApplicationConfigurations.isRealUser();
				Map<String, String> reportMap = new HashMap<String, String>();
				reportMap.put(
						FlurryConstants.FlurrySubscription.SourcePage.toString(),
						FlurryConstants.FlurrySubscription.Membership.toString());
				reportMap.put(
						FlurryConstants.FlurrySubscription.LoggedIn.toString(),
						loggedIn.toString());
				Analytics
						.logEvent(FlurryConstants.FlurrySubscription.TapsOnUpgrade
								.toString(), reportMap);

				Intent intent = new Intent(this, UpgradeActivity.class);
				startActivityForResult(intent, 0);
				finish();
			}
		} else {
			SettingFragmentNew settingsFragment = new SettingFragmentNew();
			fragmentTransaction.replace(R.id.main_fragmant_container,
					settingsFragment);
			fragmentTransaction.commit();

			boolean loadSaveOffline = getIntent().getBooleanExtra(
					"load_save_offline", false);
			if (loadSaveOffline)
				settingsFragment.addSaveOfflineSettingsFragment();

			if (offlineModeReceiver == null/* && CacheManager.isProUser(this) */) {
				offlineModeReceiver = new OfflineModeReceiver();
				IntentFilter filter = new IntentFilter();
				filter.addAction(MainActivity.ACTION_OFFLINE_MODE_CHANGED);
				registerReceiver(offlineModeReceiver, filter);
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			onBackPressed();
			return true;
		} else
			return super.onOptionsItemSelected(item);
	}

	public void setTitleBarText(String text) {
		if (TextUtils.isEmpty(text)) {

			String title = Utils.getMultilanguageTextLayOut(this,
					getResources().getString(R.string.settings_title));

			setActionBarTitle(title);

			if (!stack_text.contains(title))
				stack_text.push(title);

			setActionBarTitle(title);
		} else {
			String title = text;
			setActionBarTitle(title);

			if (!stack_text.contains(title))
				stack_text.push(title);
			setActionBarTitle(Utils.getMultilanguageTextLayOut(this, title));
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (stack_text != null && stack_text.size() > 0)
			setActionBarTitle(stack_text.get(stack_text.size() - 1));
		else
			setTitleBarText("");
	}

	@Override
	protected void onDestroy() {
		if (offlineModeReceiver != null)
			unregisterReceiver(offlineModeReceiver);
		offlineModeReceiver = null;
		// System.gc();

		super.onDestroy();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		/*
		 * Overriding this to make the activity still playing the music, instead
		 * pausing it by default.
		 */
	}

	@Override
	public void onBackPressed() {
		boolean loadSaveOffline = getIntent().getBooleanExtra(
				"load_save_offline", false);
		if ((loadSaveOffline)
				|| getIntent().getBooleanExtra("close_on_back", false)
				|| getIntent().getBooleanExtra("show_membership", false)) {
			finish();
			return;
		}
		// xtpl
		// checks if the webview exists and calls its back button support.
		Fragment fragment = getSupportFragmentManager().findFragmentByTag(
				TwitterLoginFragment.FRAGMENT_TWITTER_LOGIN);
		if (fragment != null) {
			TwitterLoginFragment twitterLoginFragment = (TwitterLoginFragment) fragment;
			twitterLoginFragment.onBackPressed();
			// return;
		} else if (getSupportFragmentManager().getBackStackEntryCount() > 0
				&& !loadSaveOffline) {
			if (stack_text != null && stack_text.size() > 1) {
				setActionBarTitle(stack_text.firstElement());
				stack_text.pop();
			} else if (stack_text != null && stack_text.size() > 0)
				setActionBarTitle(stack_text.firstElement());

			getSupportFragmentManager().popBackStack();
		} else {
			if (ApplicationConfigurations.getInstance(this)
					.getSaveOfflineMode()) {
				Intent i = new Intent(this, GoOfflineActivity.class);
				startActivity(i);
				finish();
			} else {
				if (ReoladHomeScreen) {
					Intent i = new Intent(this, HomeActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
					finish();
					// HomeActivity.isBackFromSettings = true;
				} else {
					super.onBackPressed();
				}
			}
		}
	}

	class OfflineModeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			boolean offlineMode = (ApplicationConfigurations
					.getInstance(getApplicationContext())).getSaveOfflineMode();
			if (offlineMode) {
				// startActivity(new Intent(SettingsActivity.this,
				// GoOfflineActivity.class));
				Intent i = new Intent(SettingsActivity.this, HomeActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.putExtra("finish_all", true);
				startActivity(i);
				finish();
			}
		}
	}

}
