/*
Copyright 2009-2014 Urban Airship Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE URBAN AIRSHIP INC ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
EVENT SHALL URBAN AIRSHIP INC OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.hungama.myplay.activity.preference;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.util.ScreenLockStatus;
import com.urbanairship.analytics.Analytics;
import com.urbanairship.preference.UAPreferenceAdapter;

public class PushPreferencesActivity extends PreferenceActivity {

	private UAPreferenceAdapter preferenceAdapter;

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		LinearLayout root = (LinearLayout) findViewById(android.R.id.list)
				.getParent().getParent().getParent();
		Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(
				R.layout.settings_toolbar, root, false);
		root.addView(bar, 0); // insert at top
		bar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		// ListView list = (ListView) findViewById(android.R.id.list);
		// list.setDivider(new ColorDrawable(
		// getResources().getColor(R.color.black))); // or some other color int
		// list.setDividerHeight((5));
	}

	@Override
	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set the actionBar to have up navigation if HoneyComb or higher.
		// PreferenceFragment or PreferenceActivity is not available in the
		// support
		// library. ActionBarSherlock provides a PreferenceActivity if you
		// absolutely
		// need an action bar in the preferences on older devices.
		// if (Build.VERSION.SDK_INT >= 11) {
		// ActionBar actionBar = getActionBar();
		// if (actionBar != null) {
		// actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
		// ActionBar.DISPLAY_HOME_AS_UP);
		// }
		// }

		getWindow().getDecorView().setBackgroundColor(
				getResources().getColor(R.color.white));

		// Display the push preferences
		this.addPreferencesFromResource(R.xml.push_preferences);

		// this.addPreferencesFromResource(R.xml.location_preferences);

		// Display the advanced settings
		// this.addPreferencesFromResource(R.xml.advanced_preferences);

		// Creates the UAPreferenceAdapter with the entire preference screen
		preferenceAdapter = new UAPreferenceAdapter(getPreferenceScreen());

		// ListView list = (ListView) findViewById(android.R.id.list);
		// list.setDivider(new ColorDrawable(
		// getResources().getColor(R.color.black))); // or some other color int
		// list.setDividerHeight();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return false;
	}

	@Override
	protected void onStart() {
		super.onStart();

		// Activity instrumentation for analytic tracking
		Analytics.activityStarted(this);
	}

	@Override
	protected void onUserLeaveHint() {
		ScreenLockStatus.getInstance(getBaseContext()).onStop(true, this);
		super.onUserLeaveHint();
	}

	@Override
	protected void onResume() {
		super.onResume();
		ScreenLockStatus.getInstance(getBaseContext()).onResume(this, this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		ScreenLockStatus.getInstance(getBaseContext()).onStop();
		// Activity instrumentation for analytic tracking
		Analytics.activityStopped(this);

		// Apply any changed UA preferences from the preference screen
		preferenceAdapter.applyUrbanAirshipPreferences();
	}
}
