package com.hungama.myplay.activity.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hungama.myplay.activity.services.CampaignsPreferchingService;

public class AlaramReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Logger.s(" ::::::::::::::-- AlaramReceiver");
		context.startService(new Intent(context,
				CampaignsPreferchingService.class));
	}
}
