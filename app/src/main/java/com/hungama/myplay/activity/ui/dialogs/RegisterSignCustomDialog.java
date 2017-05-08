package com.hungama.myplay.activity.ui.dialogs;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

public class RegisterSignCustomDialog extends AlertDialog.Builder implements
		DialogInterface {

	private Context context;
	private AlertDialog alertDialog;
	private LanguageTextView titleView, TitleMain;
	private LanguageTextView btnPositive, btnNegative;

	public RegisterSignCustomDialog(Context context, int theme) {
		super(context, theme);
		this.context = context;
		initialize();
	}

	public RegisterSignCustomDialog(Context context) {
		super(context, R.style.MyThemeAlertDialog);
		this.context = context;
		initialize();
	}

	@Override
	public Context getContext() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			return context;
		else
			return super.getContext();
	}

	private void initialize() {
		View view = LayoutInflater.from(getContext()).inflate(
				R.layout.custom_prompt_registration, null);
		setView(view);

		TitleMain = (LanguageTextView) view
				.findViewById(R.id.download_custom_dialog_title_text);
		TitleMain.setVisibility(View.GONE);
		titleView = (LanguageTextView) view
				.findViewById(R.id.text_custom_alert_message);
		titleView.setVisibility(View.GONE);
		btnPositive = (LanguageTextView) view
				.findViewById(R.id.button_custom_alert_positive);
		btnPositive.setVisibility(View.GONE);
		btnNegative = (LanguageTextView) view
				.findViewById(R.id.button_custom_alert_negative);
		btnNegative.setVisibility(View.GONE);

	}

	@Override
	public Builder setIcon(int iconId) {
		super.setIcon(iconId);
		return this;
	}

	@Override
	public Builder setTitle(CharSequence title) {

		TitleMain.setText(Utils.getMultilanguageTextLayOut(getContext(),
				title.toString()));
		TitleMain.setVisibility(View.VISIBLE);

		return this;
	}

	@Override
	public Builder setTitle(int titleId) {
		return setTitle(getContext().getResources().getString(titleId));
	}

	@Override
	public Builder setMessage(CharSequence message) {

		titleView.setText(Utils.getMultilanguageTextLayOut(getContext(),
				message.toString()));
		titleView.setVisibility(View.VISIBLE);

		return this;
	}

	@Override
	public Builder setMessage(int messageId) {
		return setMessage(getContext().getResources().getString(messageId));
	}

	@Override
	public Builder setPositiveButton(CharSequence text,
			final OnClickListener listener) {

		String str = Utils.getMultilanguageTextLayOut(getContext(),
				text.toString());
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {

			btnPositive.setText(str);
			btnPositive.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (listener != null) {
						listener.onClick(RegisterSignCustomDialog.this,
								DialogInterface.BUTTON_POSITIVE);
					}
					dismiss();
				}
			});
			btnPositive.setVisibility(View.VISIBLE);

		} else {

			btnNegative.setText(str);
			btnNegative.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (listener != null) {
						listener.onClick(RegisterSignCustomDialog.this,
								DialogInterface.BUTTON_POSITIVE);
					}
					dismiss();
				}
			});
			btnNegative.setVisibility(View.VISIBLE);

		}
		return this;
	}

	@Override
	public Builder setPositiveButton(int textId, OnClickListener listener) {
		return setPositiveButton(getContext().getResources().getString(textId),
				listener);
	}

	@Override
	public Builder setNeutralButton(int text, OnClickListener listener) {
		return super.setNeutralButton(text, listener);
	}

	@Override
	public Builder setNegativeButton(CharSequence text,
			final OnClickListener listener) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {

			btnNegative.setText(Utils.getMultilanguageTextLayOut(getContext(),
					text.toString()));
			btnNegative.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (listener != null) {
						listener.onClick(RegisterSignCustomDialog.this,
								DialogInterface.BUTTON_NEGATIVE);
					}
					dismiss();
				}
			});
			btnNegative.setVisibility(View.VISIBLE);

		} else {

			btnPositive.setText(Utils.getMultilanguageTextLayOut(getContext(),
					text.toString()));
			btnPositive.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (listener != null) {
						listener.onClick(RegisterSignCustomDialog.this,
								DialogInterface.BUTTON_NEGATIVE);
					}
					dismiss();
				}
			});
			btnPositive.setVisibility(View.VISIBLE);

		}
		return this;
	}

	@Override
	public Builder setNegativeButton(int textId, OnClickListener listener) {
		return setNegativeButton(getContext().getResources().getString(textId),
				listener);
	}

	@Override
	public void cancel() {
	}

	@Override
	public void dismiss() {
		try {
			if (alertDialog != null)
				alertDialog.dismiss();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public AlertDialog create() {
		if (alertDialog == null)
			alertDialog = super.create();

		setDialogSize();
		return alertDialog;
	}

	@Override
	public AlertDialog show() {
		try {
			if (alertDialog == null)
				alertDialog = super.show();
			setDialogSize();
		} catch (Exception e) {
		}
		return alertDialog;
	}

	private void setDialogSize() {
		if (alertDialog != null) {
			DisplayMetrics displaymetrics = new DisplayMetrics();
			alertDialog.getWindow().getWindowManager().getDefaultDisplay()
					.getMetrics(displaymetrics);
			int width = (int) (displaymetrics.widthPixels);

			WindowManager.LayoutParams params = alertDialog.getWindow()
					.getAttributes();
			params.width = width;
			alertDialog.getWindow().setAttributes(params);
		}
	}
}