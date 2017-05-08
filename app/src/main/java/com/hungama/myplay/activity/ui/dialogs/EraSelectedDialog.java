package com.hungama.myplay.activity.ui.dialogs;

import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.edmodo.rangebar.RangeBar;
import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.Era;
import com.hungama.myplay.activity.ui.PrefrenceDialogListener;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.util.HashMap;
import java.util.Map;

public class EraSelectedDialog extends DialogFragment implements
		OnClickListener {
	private TextView mTextTimeFrom;
	private TextView mTextTimeSeparator;
	private TextView mTextTimeTo;

	private TextView mTextRulerMinimum;
	private TextView mTextRulerMiddle;
	private TextView mTextRulerCurrent;

	private RangeBar rangebar;

	private int fromYear;
	private int toYear;

	String era_year[];

	Era mEra;
	PrefrenceDialogListener listener;

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

	public EraSelectedDialog() {
	}

	public void init(Era mEra, PrefrenceDialogListener listener) {
		this.mEra = mEra;
		this.listener = listener;
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
		setStyle(
				DialogFragment.STYLE_NO_TITLE,
				android.support.v7.appcompat.R.style.Theme_AppCompat_Light_Dialog);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(getActivity());
		View view = inflater.inflate(R.layout.fragment_era_dialog, container);
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(view, getActivity());
		}
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));

		((TextView) view.findViewById(R.id.discovery_era_title))
				.setText(getString(R.string.discovery_era_title));

		mTextTimeFrom = (TextView) view
				.findViewById(R.id.discovery_era_time_from);
		mTextTimeSeparator = (TextView) view
				.findViewById(R.id.discovery_era_time_separator);
		mTextTimeTo = (TextView) view.findViewById(R.id.discovery_era_time_to);

		mTextRulerMinimum = (TextView) view
				.findViewById(R.id.discovery_era_slider_ruler_text_time_minumum_year);
		mTextRulerMiddle = (TextView) view
				.findViewById(R.id.discovery_era_slider_ruler_text_time_middle_year);
		mTextRulerCurrent = (TextView) view
				.findViewById(R.id.discovery_era_slider_ruler_text_time_current_year);

		rangebar = (RangeBar) view.findViewById(R.id.rangebar1);

		view.findViewById(R.id.start_timer_button).setOnClickListener(this);

		// Sets the display values of the indices
		rangebar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
			@Override
			public void onIndexChangeListener(RangeBar rangeBar,
					int leftThumbIndex, int rightThumbIndex) {
				try {
					fromYear = Integer.parseInt(era_year[leftThumbIndex]);
					toYear = Integer.parseInt(era_year[rightThumbIndex]);

					mTextTimeFrom.setText(Era.getTime(fromYear));
					mTextTimeTo.setText("" + toYear);

				} catch (Exception e) {
				}
			}
		});

		if(mEra==null)
			mEra=new Era(Era.getDefaultFrom(),Era.getDefaultTo());

		fromYear = mEra.getFrom();
		toYear = mEra.getTo();

		// sets ruler year.
		mTextRulerMinimum.setText(Era.getTime(Era.getDefaultFrom()));
		mTextRulerMiddle.setText(Era.getTime(Era.getDefaultMiddle()));
		mTextRulerCurrent.setText(Era.getTime(Era.getDefaultTo()));

		setTimeLabels(mEra.getFrom(), mEra.getTo());

		int tickCount = (mEra.getDefaultMiddle() - mEra.getDefaultFrom()) / 10
				+ mEra.getDefaultTo() - mEra.getDefaultMiddle() + 1;
		Logger.e("mEra.getDefaultTo()", "" + mEra.getDefaultTo());
		Logger.e("mEra.getDefaultMiddle()", "" + mEra.getDefaultMiddle());
		Logger.e("mEra.getDefaultFrom()", "" + mEra.getDefaultFrom());

		rangebar.setTickCount(tickCount);
		rangebar.setTickHeight(10);

		int leftThumbIndex = 0, rightThumbIndex = 0;

		era_year = new String[tickCount];
		int counter = 0;
		Logger.e("tickCount", "" + tickCount);
		for (int i = 0; i < tickCount; i++) {
			if (i < (mEra.getDefaultMiddle() - mEra.getDefaultFrom()) / 10) {
				era_year[i] = "" + (mEra.getDefaultFrom() + (10 * i));
			} else {
				era_year[i] = "" + (mEra.getDefaultMiddle() + (counter));
				counter++;
			}
			if (mEra.getFrom() == Integer.parseInt(era_year[i])) {
				leftThumbIndex = i;
			} else if (mEra.getTo() == Integer.parseInt(era_year[i])) {
				rightThumbIndex = i;
			}
			Logger.e("era_year:" + i, era_year[i]);
		}
		if (leftThumbIndex != 0 && rightThumbIndex != 0)
			rangebar.setThumbIndices(leftThumbIndex, rightThumbIndex);

		return view;
	}

	private void setTimeLabels(int from, int to) {
		if (from != to) {
			mTextTimeSeparator.setVisibility(View.VISIBLE);
			mTextTimeTo.setVisibility(View.VISIBLE);

			mTextTimeFrom.setText(Era.getTime(from));
			mTextTimeTo.setText(Era.getTime(to));

		} else {
			mTextTimeSeparator.setVisibility(View.GONE);
			mTextTimeTo.setVisibility(View.GONE);

			mTextTimeFrom.setText(Era.getTime(from));
		}
	}

	@Override
	public void onClick(View v) {
		dismiss();
		mEra = new Era(fromYear, toYear);
		listener.onEraEditDialog(mEra);
		Map<String, String> reportMap = new HashMap<String, String>();
		reportMap.put(
				FlurryConstants.FlurryDiscoveryParams.EraSelected.toString(),
				mEra.getFromToString());
		Logger.i("Eraselected", mEra.getFromToString());
		Analytics.logEvent(
				FlurryConstants.FlurryEventName.DiscoveryEra.toString(),
				reportMap);

	}
}