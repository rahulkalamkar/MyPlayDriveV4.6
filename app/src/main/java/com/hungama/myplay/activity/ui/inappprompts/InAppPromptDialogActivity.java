package com.hungama.myplay.activity.ui.inappprompts;

import android.app.Activity;
import android.os.Bundle;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.ui.dialogs.SaveOfflineHelpDialog;

public class InAppPromptDialogActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getIntent().getAction().equals(
				getString(R.string.inapp_prompt_action_saveofflinehelpdialog))) {
			SaveOfflineHelpDialog dialog = new SaveOfflineHelpDialog(this);
			dialog.show();
		} else if (getIntent()
				.getAction()
				.equals(getString(R.string.inapp_prompt_action_apppromptofflinecaching3rdsong))) {
			AppPromptOfflineCaching3rdSong appPrompt10 = new AppPromptOfflineCaching3rdSong(
					this);
			if (!appPrompt10.appLaunched(true, true)) {
				finish();
			}
		}
	}
}
