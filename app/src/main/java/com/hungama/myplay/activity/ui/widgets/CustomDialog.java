/**
 * 
 */
package com.hungama.myplay.activity.ui.widgets;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

/**
 * @author XTPL
 * 
 */
public class CustomDialog extends Dialog implements DialogInterface {
	private Context context;
	// private AlertDialog alertDialog;

	private LanguageTextView titleViewEng,
			download_custom_dialog_title_textEng;
	private LanguageButton btnPositiveEng, btnNegativeEng;

	public CustomDialog(Context context, int theme) {
		super(context, theme);
		this.context = context;
		// initialize();
	}

	public CustomDialog(Context context) {
		super(context, android.R.style.Theme_Holo);
		this.context = context;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		initialize();
		// initialize();
	}

	// public Context getContext() {
	// if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
	// return context;
	// else
	// return super.getContext();
	// }

	// boolean isEnglish = false;

	private void initialize() {

		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(context);
		// isEnglish = (mApplicationConfigurations.getUserSelectedLanguage() ==
		// 0);
		// isEnglish = false;

		View view = LayoutInflater.from(getContext()).inflate(
				R.layout.custom_dialog_eng, null);
		setContentView(view);
		titleViewEng = (LanguageTextView) view
				.findViewById(R.id.text_custom_alert_message);
		download_custom_dialog_title_textEng = (LanguageTextView) view
				.findViewById(R.id.download_custom_dialog_title_text);

		titleViewEng.setVisibility(View.GONE);
		btnPositiveEng = (LanguageButton) view
				.findViewById(R.id.button_custom_alert_positive);
		btnPositiveEng.setVisibility(View.GONE);
		btnNegativeEng = (LanguageButton) view
				.findViewById(R.id.button_custom_alert_negative);
		btnNegativeEng.setVisibility(View.GONE);

	}

	public void setTitle(CharSequence title) {

		download_custom_dialog_title_textEng.setText(Utils
				.getMultilanguageTextLayOut(getContext(), title.toString()));

	}

	public void setTitle(int titleId) {
		setTitle(getContext().getResources().getString(titleId));
	}

	public void setMessage(CharSequence message) {

		titleViewEng.setText(Utils.getMultilanguageTextLayOut(getContext(),
				message.toString()));
		titleViewEng.setVisibility(View.VISIBLE);

		// return this;
	}

	public void setMessage(int messageId) {
		setMessage(getContext().getResources().getString(messageId));
	}

	public void setPositiveButton(CharSequence text,
			final OnClickListener listener) {

		String str = Utils.getMultilanguageTextLayOut(getContext(),
				text.toString());
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {

			btnPositiveEng.setText(str);
			btnPositiveEng.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (listener != null) {
						listener.onClick(CustomDialog.this,
								DialogInterface.BUTTON_POSITIVE);
					}
					dismiss();
				}
			});
			btnPositiveEng.setVisibility(View.VISIBLE);

		} else {

			btnNegativeEng.setText(str);
			btnNegativeEng.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (listener != null) {
						listener.onClick(CustomDialog.this,
								DialogInterface.BUTTON_POSITIVE);
					}
					dismiss();
				}
			});
			btnNegativeEng.setVisibility(View.VISIBLE);

		}
	}

	// public void setPositiveButton(int textId, OnClickListener listener) {
	// setPositiveButton(getContext().getResources().getString(textId),
	// listener);
	// }

	public void setNegativeButton(CharSequence text,
			final OnClickListener listener) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {

			btnNegativeEng.setText(Utils.getMultilanguageTextLayOut(
					getContext(), text.toString()));
			btnNegativeEng.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (listener != null) {
						listener.onClick(CustomDialog.this,
								DialogInterface.BUTTON_NEGATIVE);
					}
					dismiss();
				}
			});
			btnNegativeEng.setVisibility(View.VISIBLE);

		} else {

			btnPositiveEng.setText(Utils.getMultilanguageTextLayOut(
					getContext(), text.toString()));
			btnPositiveEng.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (listener != null) {
						listener.onClick(CustomDialog.this,
								DialogInterface.BUTTON_NEGATIVE);
					}
					dismiss();
				}
			});
			btnPositiveEng.setVisibility(View.VISIBLE);

		}
		// return this;
	}

	@Override
	public void cancel() {
	}

	@Override
	public void dismiss() {
		try {
			// if (alertDialog != null)
			super.dismiss();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void show() {
		super.show();
		// return alertDialog;
	}
}
