package com.hungama.myplay.activity.ui.dialogs;

import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.Tempo;
import com.hungama.myplay.activity.ui.PrefrenceDialogListener;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TempoSelectedDialog extends DialogFragment implements
		OnCheckedChangeListener {

	private RadioGroup timesRadioGroup;
	PrefrenceDialogListener listener;
	ArrayList<Tempo> tempos = new ArrayList<Tempo>();

	public void onStart() {
		super.onStart();
		setDialogSize();
	};

	private DialogInterface.OnDismissListener onDismissListener;

	public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
		this.onDismissListener = onDismissListener;
	}
	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		if (onDismissListener != null) {
			onDismissListener.onDismiss(dialog);
		}
	}

	public TempoSelectedDialog() {
	}

	public void init(List<Tempo> list,
			PrefrenceDialogListener listener) {
		this.tempos = (ArrayList<Tempo>) list;
		this.listener = listener;
	}

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
		setStyle(
				DialogFragment.STYLE_NO_TITLE,
				android.support.v7.appcompat.R.style.Theme_AppCompat_Light_Dialog);
	}

	View rootView;

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

		timesRadioGroup = (RadioGroup) view
				.findViewById(R.id.times_radio_group);
		initializeComponents(view);
		((TextView) view.findViewById(R.id.download_custom_dialog_title_text))
				.setText(getString(R.string.discovery_tempo_title));

		view.findViewById(R.id.ll_button).setVisibility(View.GONE);

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				settingTempo();
			}
		}, 100);
		rootView = view;
		return view;
	}

	boolean isInitialize = true;

	private void settingTempo() {
		try {
			clearSelection();
			isInitialize = true;
			if (tempos != null)
				if (tempos.get(0) == Tempo.AUTO) {
					rb15.setChecked(true);
				} else if (tempos.get(0) == Tempo.MEDIUM) {
					rb45.setChecked(true);
				} else if (tempos.get(0) == Tempo.LOW) {
					rb30.setChecked(true);
				} else if (tempos.get(0) == Tempo.HIGH) {
					rb60.setChecked(true);
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
		timesRadioGroup.setOnCheckedChangeListener(this);
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

		LanguageTextView radio_txt_15 = (LanguageTextView) rootView
				.findViewById(R.id.radio_txt_15);
		radio_txt_15.setText(getActivity().getString(
				R.string.discovery_tempo_auto));
		radio_txt_15.setOnClickListener(onRadioBtnClick);

		LanguageTextView radio_txt_30 = (LanguageTextView) rootView
				.findViewById(R.id.radio_txt_30);
		radio_txt_30.setText(getActivity().getString(
				R.string.discovery_tempo_low));
		radio_txt_30.setOnClickListener(onRadioBtnClick);

		LanguageTextView radio_txt_45 = (LanguageTextView) rootView
				.findViewById(R.id.radio_txt_45);
		radio_txt_45.setText(getActivity().getString(
				R.string.discovery_tempo_medium));
		radio_txt_45.setOnClickListener(onRadioBtnClick);

		LanguageTextView radio_txt_60 = (LanguageTextView) rootView
				.findViewById(R.id.radio_txt_60);
		radio_txt_60.setText(getActivity().getString(
				R.string.discovery_tempo_high));
		radio_txt_60.setOnClickListener(onRadioBtnClick);

		if (ApplicationConfigurations.getInstance(getActivity())
				.getUserSelectedLanguage() != 0) {
			radio_txt_15.setText(DBOHandler.getTextFromDb(getActivity()
					.getString(R.string.discovery_tempo_auto), getActivity()));
			radio_txt_30.setText(DBOHandler.getTextFromDb(getActivity()
					.getString(R.string.discovery_tempo_low), getActivity()));
			radio_txt_45
					.setText(DBOHandler.getTextFromDb(
							getActivity().getString(
									R.string.discovery_tempo_medium),
							getActivity()));
			radio_txt_60.setText(DBOHandler.getTextFromDb(getActivity()
					.getString(R.string.discovery_tempo_high), getActivity()));
		}

		clearSelection();
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
			clearSelection();
			((RadioButton) rootView.findViewById(checkedId)).setChecked(true);
		}
	};

	private void clearSelection() {
		rb15.setChecked(false);
		rb30.setChecked(false);
		rb45.setChecked(false);
		rb60.setChecked(false);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		dismiss();
		tempos = new ArrayList<Tempo>();

		switch (group.getId()) {
		case R.id.times_radio_group:

			if (checkedId == R.id.radio15) {
				tempos.add(Tempo.AUTO);
			} else if (checkedId == R.id.radio30) {
				tempos.add(Tempo.LOW);
			} else if (checkedId == R.id.radio45) {
				tempos.add(Tempo.MEDIUM);
			} else if (checkedId == R.id.radio60) {
				tempos.add(Tempo.HIGH);
			}
			break;

		default:
			break;
		}
		Map<String, String> reportMap;
		reportMap = new HashMap<String, String>();
		reportMap.put(
				FlurryConstants.FlurryDiscoveryParams.TempoSelected.toString(),
				tempos.toString());
		Logger.i("temoselected", tempos.toString());
		Analytics.logEvent(
				FlurryConstants.FlurryEventName.DiscoveryTempo.toString(),
				reportMap);
		listener.onTempoEditDialog(tempos);
	}
}
