package com.hungama.myplay.activity.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import com.hungama.hungamamusic.lite.R;


public class MyProgressDialog extends Dialog {// ProgressDialog {

	public MyProgressDialog(Context context) {
		super(context, R.style.MyThemeDialog);
		try {
			// super(context);
			setCancelable(false);
			// setProgressStyle(android.R.style.Widget_ProgressBar_Large);
			// setProgressDrawable(context.getDrawable(R.drawable.custom_progress_background));
			// setIndeterminateDrawable(context.getDrawable(R.drawable.custom_progress_background));
			show();
		} catch (Exception e) {

		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.custom_progress_dialog);
		// ((ProgressBar) findViewById(R.id.progressbar))
		// .getIndeterminateDrawable().setColorFilter(
		// new LightingColorFilter(0xFF000000, 0xFFFFFF));
		// Color.WHITE, Mode.MULTIPLY);
	}
}
