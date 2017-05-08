/**
 * 
 */
package com.hungama.myplay.activity.ui.dialogs;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.gigya.ShareDialogFragment;
import com.hungama.myplay.activity.ui.widgets.LanguageButton;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.SleepModeManager;
import com.hungama.myplay.activity.util.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author DavidSvilem
 * 
 */
public class SleepModeDialog extends DialogFragment implements OnClickListener,
		OnCheckedChangeListener {

	// public static final String FRAGMENT_TAG = "CountDownTimerDialog";

	// public static final String TIME_TO_COUNT = "time_to_count";

	// Data Members
	private int time = 15; // default

	// Views
	private ImageButton closeButton;
	private LanguageButton startTimerButton;
	private TextView remainingTimeText;
	private RadioGroup timesRadioGroup;

	private SleepModeManager mSleepModeManager;
	private View rootView;

	public static SleepModeDialog newInstance() {
		SleepModeDialog f = new SleepModeDialog();

		// Supply data input as an argument.

		return f;
	}

	public void onStart() {
		super.onStart();
		setDialogSize();
	};

	private void setDialogSize() {
		if (getDialog() == null) {
			return;
		}
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getDialog().getWindow().getWindowManager().getDefaultDisplay()
				.getMetrics(displaymetrics);
		int width = (int) (displaymetrics.widthPixels);

		WindowManager.LayoutParams params = getDialog().getWindow()
				.getAttributes();
		params.width = width;
		getDialog().getWindow().setAttributes(params);

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSleepModeManager = SleepModeManager.getInstance(getActivity()
				.getApplicationContext());

		setStyle(
				DialogFragment.STYLE_NO_TITLE,
				android.support.v7.appcompat.R.style.Theme_AppCompat_Light_Dialog);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(getActivity());
		View view = inflater.inflate(R.layout.dialog_sleep_mode_timer,
				container);
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(view, getActivity());
		}
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));

		closeButton = (ImageButton) view.findViewById(R.id.close_button);
		startTimerButton = (LanguageButton) view
				.findViewById(R.id.start_timer_button);
		remainingTimeText = (TextView) view
				.findViewById(R.id.remaining_time_text);
		timesRadioGroup = (RadioGroup) view
				.findViewById(R.id.times_radio_group);

		// Set listeners
		closeButton.setOnClickListener(this);
		Button cancelButton = (Button) view
				.findViewById(R.id.cancel_timer_button);
		cancelButton.setOnClickListener(this);

		startTimerButton.setOnClickListener(this);
		timesRadioGroup.setOnCheckedChangeListener(this);

		if (mSleepModeManager.isCountingDown()) {

			remainingTimeText.setVisibility(View.VISIBLE);
			timesRadioGroup.setVisibility(View.GONE);

			// Set text
			String timeLeft = mSleepModeManager.getTimeLeftStr();
			remainingTimeText.setText(timeLeft);

			mSleepModeManager.setTextView(remainingTimeText);
			mSleepModeManager.setDialog(this);
			// Set button text
			startTimerButton
					.setText(Utils.getMultilanguageTextLayOut(getActivity(),
							getString(R.string.reset_timer_button_text)));

		} else {

		}
		rootView = view;
		// time = getArguments().getInt(TIME_TO_COUNT);
		initializeComponents(view);
		return view;
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.cancel_timer_button:
		case R.id.close_button:

			dismiss();

			break;

		case R.id.start_timer_button:

			if (mSleepModeManager.isCountingDown()) {
				mSleepModeManager.cancelCounting();
				dismiss();
			} else {
				mSleepModeManager.startAlarm(time);
				dismiss();
				Map<String, String> reportMap = new HashMap<String, String>();
				reportMap.put(
						FlurryConstants.FlurryAllPlayer.TimeOfDay.toString(),
						Utils.getTimeOfDay());
				reportMap.put(
						FlurryConstants.FlurryAllPlayer.Duration.toString(), ""
								+ time);
				Analytics.logEvent(
						FlurryConstants.FlurryAllPlayer.SleepModeUsed
								.toString(), reportMap);
			}

			// Show CountDownTimerDialog
			SleepModeDialog shareDialogFragment = SleepModeDialog.newInstance();

			FragmentManager mFragmentManager = getFragmentManager();
			shareDialogFragment.show(mFragmentManager,
					ShareDialogFragment.FRAGMENT_TAG);

			break;

		default:
			break;
		}
	}

	RadioButton rb15, rb30, rb45, rb60;

	private void initializeComponents(View rootView) {
		// Bit-Rate settings
		rb15 = (RadioButton) rootView.findViewById(R.id.radio15);
		rb30 = (RadioButton) rootView.findViewById(R.id.radio30);
		rb45 = (RadioButton) rootView.findViewById(R.id.radio45);
		rb60 = (RadioButton) rootView.findViewById(R.id.radio60);

		rb15.setOnClickListener(onRadioBtnClick);
		rb30.setOnClickListener(onRadioBtnClick);
		rb45.setOnClickListener(onRadioBtnClick);
		rb60.setOnClickListener(onRadioBtnClick);

		TextView radio_txt_15 = (TextView) rootView
				.findViewById(R.id.radio_txt_15);
		radio_txt_15.setText(getActivity().getString(
				R.string.sleep_mode_15_mins));
		radio_txt_15.setOnClickListener(onRadioBtnClick);

		TextView radio_txt_30 = (TextView) rootView
				.findViewById(R.id.radio_txt_30);
		radio_txt_30.setText(getActivity().getString(
				R.string.sleep_mode_30_mins));
		radio_txt_30.setOnClickListener(onRadioBtnClick);

		TextView radio_txt_45 = (TextView) rootView
				.findViewById(R.id.radio_txt_45);
		radio_txt_45.setText(getActivity().getString(
				R.string.sleep_mode_45_mins));
		radio_txt_45.setOnClickListener(onRadioBtnClick);

		TextView radio_txt_60 = (TextView) rootView
				.findViewById(R.id.radio_txt_60);
		radio_txt_60.setText(getActivity().getString(
				R.string.sleep_mode_60_mins));
		radio_txt_60.setOnClickListener(onRadioBtnClick);
		setTimeSelection();
		rb15.setChecked(true);
	}

	OnClickListener onRadioBtnClick = new OnClickListener() {
		public void onClick(View v) {
			int checkedId = v.getId();
			if (v instanceof TextView) {
				if (v.getId() == R.id.radio_txt_15)
					checkedId = rb15.getId();
				else if (v.getId() == R.id.radio_txt_60)
					checkedId = rb60.getId();
				else if (v.getId() == R.id.radio_txt_30)
					checkedId = rb30.getId();
				else if (v.getId() == R.id.radio_txt_45)
					checkedId = rb45.getId();
			}
			onCheckedChanged(timesRadioGroup, checkedId);
			setTimeSelection();
			((RadioButton) rootView.findViewById(checkedId)).setChecked(true);
		}
	};

	private void setTimeSelection() {
		rb15.setChecked(false);
		rb30.setChecked(false);
		rb45.setChecked(false);
		rb60.setChecked(false);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {

		switch (group.getId()) {
		case R.id.times_radio_group:
			if (checkedId == R.id.radio15) {
				time = 15;
			} else if (checkedId == R.id.radio30) {
				time = 30;
			} else if (checkedId == R.id.radio45) {
				time = 45;
			} else if (checkedId == R.id.radio60) {
				time = 60;
			}
			break;

		default:
			break;
		}
	}

}
