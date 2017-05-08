package com.hungama.myplay.activity.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.util.Utils;

public class RadioFullPlayerInfoDialog extends Dialog implements
		android.view.View.OnClickListener {

	private String descriptions;
	public Context context;

	public RadioFullPlayerInfoDialog(Context context, String descriptions) {
		super(context);
		this.context = context;
		this.descriptions = descriptions;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		// getWindow().getAttributes().windowAnimations =
		// R.style.dialog_animation;
		setContentView(R.layout.dialog_radio_full_player_info);
		setCanceledOnTouchOutside(true);
		setCancelable(true);

		View rootView = findViewById(R.id.ll_main);

		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(getContext());
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(rootView, getContext());
		}

		TextView txt_subtitle = (TextView) findViewById(R.id.txt_subtitle);
		txt_subtitle.setText(descriptions);

		findViewById(R.id.btn_dismiss).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_dismiss:
			dismiss();
			break;
		}
	}
}
