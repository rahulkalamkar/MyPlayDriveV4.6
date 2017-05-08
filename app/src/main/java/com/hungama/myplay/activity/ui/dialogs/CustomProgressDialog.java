package com.hungama.myplay.activity.ui.dialogs;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;


public class CustomProgressDialog extends ProgressDialog {

	public CustomProgressDialog(Context context) {
		super(context, R.style.Theme_CustomDialogTheme);
		init(context, "Loading");
	}

	public CustomProgressDialog(Context context, String message) {
		super(context, R.style.Theme_CustomDialogTheme);
		init(context, message);
	}

	public CustomProgressDialog(Context context, int theme) {
		super(context, R.style.Theme_CustomDialogTheme);
		init(context, "Loading");
	}

	private void init(Context context, String message) {
		show();
		try {
			getWindow().setBackgroundDrawable(
					new ColorDrawable(android.graphics.Color.TRANSPARENT));
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}

		setContentView(R.layout.custom_progress_dialog_discovery);
		((TextView) findViewById(android.R.id.message)).setText("");// message
		final ImageView ivRotate = (ImageView) findViewById(android.R.id.progress);
		Animation rotate = AnimationUtils.loadAnimation(context, R.anim.rotate);
		ivRotate.startAnimation(rotate);
		setCancelable(false);
	}
}
