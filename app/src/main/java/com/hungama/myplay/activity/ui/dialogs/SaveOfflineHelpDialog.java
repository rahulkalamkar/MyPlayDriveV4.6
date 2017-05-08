package com.hungama.myplay.activity.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.ui.GoOfflineActivity;
import com.hungama.myplay.activity.ui.widgets.LanguageButton;
import com.hungama.myplay.activity.util.Utils;

public class SaveOfflineHelpDialog extends Dialog implements
		android.view.View.OnClickListener/* , CommunicationOperationListener */{

	public SaveOfflineHelpDialog(Activity activity) {
		super(activity);
		this.mActivity = activity;
	}

	public Activity mActivity;
	LanguageButton btn_apply;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		setContentView(R.layout.dialog_save_offline_help_layout);
		setCanceledOnTouchOutside(true);
		setCancelable(true);

		View rootView = findViewById(R.id.ll_main);

		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(getContext());
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(rootView, getContext());
		}

		findViewById(R.id.btn_showme).setOnClickListener(this);
		findViewById(R.id.btn_dismiss).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_dismiss:
			dismiss();
			break;
		case R.id.btn_showme:
			Intent i = new Intent(mActivity, GoOfflineActivity.class);
			i.putExtra("show_toast", false);
			mActivity.startActivity(i);
			dismiss();

			break;
		}
	}

	@Override
	public void dismiss() {
		try{
			super.dismiss();
		}catch (Exception e){}
		if(mActivity!=null)
			mActivity.finish();
	}
}
