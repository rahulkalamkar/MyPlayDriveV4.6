/**
 * 
 */
package com.hungama.myplay.activity.ui.widgets;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

/**
 * @author XTPL
 * 
 */
public class CustomAlertDialog extends AlertDialog.Builder implements
		DialogInterface {
	private Context context;
	private AlertDialog alertDialog;
	private LanguageTextView titleView, TitleMain;
	private LanguageTextView btnPositive, btnNegative;

	private TextView titleViewEng, titleMainViewEng;
	private TextView btnPositiveEng, btnNegativeEng;

	private LanguageCheckBox chkRemember;

	public CustomAlertDialog(Context context, int theme) {
		super(context, theme);
		this.context = context;
		initialize();
	}

	public CustomAlertDialog(Context context) {
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

	boolean isEnglish = false;

	private void initialize() {

		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(context);
		isEnglish = (mApplicationConfigurations.getUserSelectedLanguage() == 0);
		// isEnglish = false;
		if (!isEnglish) {
			View view = LayoutInflater.from(getContext()).inflate(
					R.layout.custom_alert_dialog, null);
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

			chkRemember = (LanguageCheckBox) view
					.findViewById(R.id.check_box_remember);
		} else {
			View view = LayoutInflater.from(getContext()).inflate(
					R.layout.custom_alert_dialog_eng, null);
			setView(view);

			titleMainViewEng = (TextView) view
					.findViewById(R.id.download_custom_dialog_title_text);
			titleMainViewEng.setVisibility(View.GONE);
			titleViewEng = (TextView) view
					.findViewById(R.id.text_custom_alert_message);
			titleViewEng.setVisibility(View.GONE);
			btnPositiveEng = (TextView) view
					.findViewById(R.id.button_custom_alert_positive);
			btnPositiveEng.setVisibility(View.GONE);
			btnNegativeEng = (TextView) view
					.findViewById(R.id.button_custom_alert_negative);
			btnNegativeEng.setVisibility(View.GONE);

			chkRemember = (LanguageCheckBox) view
					.findViewById(R.id.check_box_remember);
		}

		// LinearLayout ll = new LinearLayout(getContext());
		// ll.setOrientation(LinearLayout.VERTICAL);
		// setView(ll);
		//
		// titleView = new LanguageTextView(getContext());
		// titleView.setGravity(Gravity.CENTER_VERTICAL);
		// titleView.setTextColor(getContext().getResources().getColor(R.color.white));
		// titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
		// ll.addView(titleView);
		// LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)
		// titleView.getLayoutParams();
		// lp.setMargins(10, 10, 10, 10);
		//
		// LinearLayout llButtons = new LinearLayout(getContext());
		// llButtons.setOrientation(LinearLayout.HORIZONTAL);
		// ll.addView(llButtons);
		//
		// LinearLayout.LayoutParams lpButton = new
		// LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
		// LayoutParams.WRAP_CONTENT);
		// lpButton.weight = 1;
		//
		// btnPositive = new LanguageButton(getContext());
		// btnPositive.setTextColor(getContext().getResources().getColor(R.color.white));
		// btnPositive.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
		// llButtons.addView(btnPositive);
		// btnPositive.setLayoutParams(lpButton);
		//
		// btnNegative = new LanguageButton(getContext());
		// btnNegative.setTextColor(getContext().getResources().getColor(R.color.white));
		// btnNegative.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
		// llButtons.addView(btnNegative);
		// btnNegative.setLayoutParams(lpButton);
	}

	@Override
	public Builder setIcon(int iconId) {
		super.setIcon(iconId);
		return this;
	}

	@Override
	public Builder setTitle(CharSequence title) {
		if (isEnglish) {
			// TextView alertTitleView = new TextView(getContext());
			// alertTitleView.setGravity(Gravity.CENTER_VERTICAL);
			// alertTitleView.setTextColor(getContext().getResources().getColor(
			// R.color.white));
			// alertTitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
			// alertTitleView.setPadding(15, 15, 15, 15);
			// setCustomTitle(alertTitleView);
			// alertTitleView.setText(title.toString());
			titleMainViewEng.setText(title);
			titleMainViewEng.setVisibility(View.VISIBLE);
		} else {
			// LanguageTextView alertTitleView = new
			// LanguageTextView(getContext());
			//
			// alertTitleView.setGravity(Gravity.CENTER_VERTICAL);
			// alertTitleView.setTextColor(getContext().getResources().getColor(
			// R.color.white));
			// alertTitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
			// alertTitleView.setPadding(15, 15, 15, 15);
			// setCustomTitle(alertTitleView);
			// alertTitleView.setText(Utils.getMultilanguageTextLayOut(
			// getContext(), title.toString()));

			TitleMain.setText(Utils.getMultilanguageTextLayOut(getContext(),
					title.toString()));
			TitleMain.setVisibility(View.VISIBLE);
		}

		return this;
	}

	@Override
	public Builder setTitle(int titleId) {
		return setTitle(getContext().getResources().getString(titleId));
	}

	@Override
	public Builder setMessage(CharSequence message) {
		if (isEnglish) {
			titleViewEng.setText(message.toString());
			titleViewEng.setVisibility(View.VISIBLE);
		} else {
			titleView.setText(Utils.getMultilanguageTextLayOut(getContext(),
					message.toString()));
			titleView.setVisibility(View.VISIBLE);
		}

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

			if (isEnglish) {
				btnPositiveEng.setText(text);
				btnPositiveEng.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (listener != null) {
							listener.onClick(CustomAlertDialog.this,
									DialogInterface.BUTTON_POSITIVE);
						}
						dismiss();
					}
				});
				btnPositiveEng.setVisibility(View.VISIBLE);
			} else {
				btnPositive.setText(str);
				btnPositive.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (listener != null) {
							listener.onClick(CustomAlertDialog.this,
									DialogInterface.BUTTON_POSITIVE);
						}
						dismiss();
					}
				});
				btnPositive.setVisibility(View.VISIBLE);
			}
		} else {
			if (isEnglish) {
				btnNegativeEng.setText(text);
				btnNegativeEng.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (listener != null) {
							listener.onClick(CustomAlertDialog.this,
									DialogInterface.BUTTON_POSITIVE);
						}
						dismiss();
					}
				});
				btnNegativeEng.setVisibility(View.VISIBLE);
			} else {
				btnNegative.setText(str);
				btnNegative.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (listener != null) {
							listener.onClick(CustomAlertDialog.this,
									DialogInterface.BUTTON_POSITIVE);
						}
						dismiss();
					}
				});
				btnNegative.setVisibility(View.VISIBLE);
			}
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

			if (isEnglish) {
				btnNegativeEng.setText(Utils.getMultilanguageTextLayOut(
						getContext(), text.toString()));
				btnNegativeEng.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (listener != null) {
							listener.onClick(CustomAlertDialog.this,
									DialogInterface.BUTTON_NEGATIVE);
						}
						dismiss();
					}
				});
				btnNegativeEng.setVisibility(View.VISIBLE);
			} else {

				btnNegative.setText(Utils.getMultilanguageTextLayOut(
						getContext(), text.toString()));
				btnNegative.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (listener != null) {
							listener.onClick(CustomAlertDialog.this,
									DialogInterface.BUTTON_NEGATIVE);
						}
						dismiss();
					}
				});
				btnNegative.setVisibility(View.VISIBLE);
			}
		} else {
			if (isEnglish) {
				btnPositiveEng.setText(text);
				btnPositiveEng.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (listener != null) {
							listener.onClick(CustomAlertDialog.this,
									DialogInterface.BUTTON_NEGATIVE);
						}
						dismiss();
					}
				});
				btnPositiveEng.setVisibility(View.VISIBLE);
			} else {
				btnPositive.setText(Utils.getMultilanguageTextLayOut(
						getContext(), text.toString()));
				btnPositive.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (listener != null) {
							listener.onClick(CustomAlertDialog.this,
									DialogInterface.BUTTON_NEGATIVE);
						}
						dismiss();
					}
				});
				btnPositive.setVisibility(View.VISIBLE);

			}
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
			if (mOnDismissListener != null) {
				mOnDismissListener.onDismiss();
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	public interface OnDismissListener {
		public void onDismiss();
	}

	private OnDismissListener mOnDismissListener;

	public void setOnDismissListener(OnDismissListener mOnDismissListener) {
		this.mOnDismissListener = mOnDismissListener;
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
			if (!((Activity) context).isFinishing()) {
				if (alertDialog == null)
					alertDialog = super.show();
				setDialogSize();
			}
		} catch (Exception e) {
			if (alertDialog == null)
				alertDialog = super.show();
			setDialogSize();
		}

		return alertDialog;
	}

	private void setDialogSize() {
		try {
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setRememberCheckVisibility(boolean isVisible) {
		if (isVisible)
			chkRemember.setVisibility(View.VISIBLE);
		else
			chkRemember.setVisibility(View.GONE);
	}

	public boolean getRememberCheckState() {
		return chkRemember.isChecked();
	}
}
