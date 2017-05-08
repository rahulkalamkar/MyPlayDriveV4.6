/**
 * 
 */
package com.hungama.myplay.activity.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hungama.myplay.activity.util.Logger;

/**
 * @author XTPL
 * 
 */
public class RemoteControlReceiver extends BroadcastReceiver {

	public static final String ACTION_MEDIA_BUTTON = "com.hungama.myplay.activity.player.ACTION_MEDIA_BUTTON";

	@Override
	public void onReceive(Context context, Intent intent) {
		Logger.v("TestApp", "Button press received :: " + intent.getAction());
		if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
			Intent internalIntent = new Intent(ACTION_MEDIA_BUTTON);
			try {
				internalIntent.putExtras(intent.getExtras());
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
			context.sendBroadcast(internalIntent);
		}
	}
}