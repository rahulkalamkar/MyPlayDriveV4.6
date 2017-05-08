package com.hungama.myplay.activity.gigya;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.HungamaApplication;

import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.util.Analytics;

/**
 * @author DavidSvilem
 */
public class InviteFriendsActivity extends MainActivity {

	public static final String FLURRY_SOURCE = "flurry_source";

	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;

	// public static boolean isInviteFriendsActivityOpen = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setOverlayAction();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		onCreateCode();
		// getDrawerLayout();
		mDataManager = DataManager.getInstance(getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();

		// Invite Friends Fragment
		FragmentManager mFragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();
		InviteFriendsFragment inviteFriendsFragment = new InviteFriendsFragment();

		Bundle bundle = getIntent().getExtras();

		if (bundle != null && bundle.containsKey(FLURRY_SOURCE)) {
			inviteFriendsFragment.setArguments(bundle);
		}

		fragmentTransaction.replace(R.id.main_fragmant_container,
				inviteFriendsFragment);
		fragmentTransaction.commit();
		// isInviteFriendsActivityOpen = true;

		String title = getString(R.string.invite_friends_title);
		showBackButtonWithTitle(title, "");
	}

	@Override
	protected NavigationItem getNavigationItem() {
		return NavigationItem.OTHER;
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

	@Override
	public void onBackPressed() {
		if (closeDrawerIfOpen()) {
			return;
		}
		if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
			getSupportFragmentManager().popBackStack();
		} else
			finish();

	}

	@Override
	protected void onStart() {
		super.onStart();
		// FlurryAgent.setContinueSessionMillis(Constants.FLURRY_SESSION_LENGTH);
		// FlurryAgent.onStartSession(this, getString(R.string.flurry_app_key));
		Analytics.startSession(this);

		String title = getString(R.string.invite_friends_title);
		showBackButtonWithTitle(title, "");
	}

	@Override
	protected void onStop() {
		super.onStop();
		// HungamaApplication.activityStoped();
		Analytics.onEndSession(this);
	}

	@Override
	protected void onResume() {
		HungamaApplication.activityResumed();
		if (mApplicationConfigurations.isSongCatched()) {
			openOfflineGuide();
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		HungamaApplication.activityPaused();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// isInviteFriendsActivityOpen = false;
		super.onDestroy();
	}

}