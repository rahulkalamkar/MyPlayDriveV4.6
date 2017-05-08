package com.hungama.myplay.activity.ui.inappprompts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class InAppPromptReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// SaveOfflineHelpDialog dialog = new SaveOfflineHelpDialog(arg0);
		// dialog.show();
		Intent i = new Intent(arg0, InAppPromptDialogActivity.class);
		i.setAction(arg1.getAction());
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		arg0.startActivity(i);
	}
}
