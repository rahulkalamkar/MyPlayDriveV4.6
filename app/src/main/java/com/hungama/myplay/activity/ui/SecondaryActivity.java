package com.hungama.myplay.activity.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;
import com.reverie.lm.LM;

/**
 * Handles the connection to the player service, if it's playing. pauses it.
 */
public class SecondaryActivity extends ActionBarActivity implements
		ServiceConnection {

	// a token for connecting the player service.
	// private ServiceToken mServiceToken = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!Utils.isAllPermissionsGranted(this)) {
			Toast.makeText(this, "Please grant all permissions.", Toast.LENGTH_LONG).show();
			sendBroadcast(new Intent(HomeActivity.ACTION_CLOSE_APP));
			finish();
			return;
		}

	}

	protected void onCreateCode(){
		//		if (Logger.enableLanguageLibraryThread) {
//			Utils.startReverieSDK(getApplicationContext());
//		} else if (Logger.enableLanguageLibrary) {
//			int result = new LM(this).RegisterSDK(HomeActivity.SDK_ID);
//			Log.e("Sdk Result : ", "" + result);
//			if (result == LM.LC_INVALID) {
//				// Exit with some notification
//				// Your SDK is not valid. Check your SDK_ID and Application
//				// package
//				// name. If does not resolved please contact SDK provider.
//			} else if (result == LM.LC_NT_ERROR) {
//				// Exit with notification.
//				// Check your network connectivity. If the Internet connectivity
//				// is
//				// fine, please contact SDK provider.
//			} else if (result == LM.LC_NT_UNAVAILABLE) {
//				// notify user to enable Internet connection
//			} else if (result == LM.LC_UNKNOWN) {
//				// Exit with notification. Contact SDK provider.
//			} else if (result == LM.LC_VALID) {
//			}
//		}

		if (this instanceof AppTourActivity) {

		} else {
			Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
			if (mToolbar != null)
				setSupportActionBar(mToolbar);

			// mToolbar.inflateMenu(R.menu.menu_main_actionbar);
			// // Set an OnMenuItemClickListener to handle menu item clicks
			// mToolbar.setOnMenuItemClickListener(
			// new Toolbar.OnMenuItemClickListener() {
			// @Override
			// public boolean onMenuItemClick(MenuItem item) {
			// // Handle the menu item
			// return true;
			// }
			// });
			ActionBar mActionBar;
			try {

				mActionBar = getSupportActionBar();
				mActionBar.setIcon(R.drawable.icon_actionbar_logo);

				// mActionBar.setBackgroundDrawable(getResources().getDrawable(
				// R.drawable.background_actionbar));
			} catch (Error e) {
				System.gc();
				System.runFinalization();
				System.gc();
				mActionBar = getSupportActionBar();
				mActionBar.setIcon(R.drawable.icon_actionbar_logo);
				// mActionBar.setBackgroundDrawable(getResources().getDrawable(
				// R.drawable.background_actionbar));
			}
		}
	}

	// @Override
	// protected void onStart() {
	// super.onStart();
	//
	// /*
	// * Binds to the PLayer service to pause it if playing.
	// */
	// // mServiceToken = PlayerServiceBindingManager.bindToService(this, this);
	// }

	@Override
	protected void onResume() {
		// ScreenLockStatus.getInstance(getBaseContext()).onResume(this, this);
		super.onResume();
		if (Utils.isDeviceAirplaneModeActive(this)
				&& HomeActivity.needToShowAirplaneDialog) {
			HomeActivity.needToShowAirplaneDialog = false;
			Intent i = new Intent(this, OfflineAlertActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
		}
	}

	@Override
	protected void onUserLeaveHint() {
		// ScreenLockStatus.getInstance(getBaseContext()).onStop(true, this);
		Logger.s("onUserLeaveHint HomeScreen");
		super.onUserLeaveHint();
	}

	// @Override
	// protected void onStop() {
	// // disconnects from the player service.
	// // ScreenLockStatus.getInstance(getBaseContext()).onStop();
	// // PlayerServiceBindingManager.unbindFromService(mServiceToken);
	// super.onStop();
	// }

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		/*
		 * we've establish a connection to the player service. if it plays,
		 * pause it.
		 */
		// PlayerSericeBinder binder = (PlayerSericeBinder) service;
		// PlayerService playerService = binder.getService();
		//
		// if (playerService.isLoading() || playerService.getState() ==
		// State.PLAYING) {
		// playerService.pause();
		// }
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		// mServiceToken = null;
	}

	public void setActionBarTitle(String title) {
		try {
			ActionBar mActionBar = getSupportActionBar();
			mActionBar.setIcon(R.drawable.icon_actionbar_logo);
			mActionBar.setDisplayUseLogoEnabled(true);

			mActionBar.setDisplayShowTitleEnabled(false);
			mActionBar.setDisplayShowHomeEnabled(true);

			mActionBar.setHomeButtonEnabled(true);
			mActionBar.setDisplayHomeAsUpEnabled(true);
			Utils.setActionBarTitle(this, mActionBar, " " + title);
		} catch (Exception e) {
			e.printStackTrace();
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

	// @Override
	// public void onBackPressed() {
	// finish();
	// // super.onBackPressed();
	// }
}
