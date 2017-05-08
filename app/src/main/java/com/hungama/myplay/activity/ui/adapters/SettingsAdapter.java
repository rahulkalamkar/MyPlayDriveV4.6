package com.hungama.myplay.activity.ui.adapters;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Utils;

public class SettingsAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<String> list;
	private Map<String, Integer> map;
	private Context context;
	private Fragment mFragmnet;
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;

	public SettingsAdapter(Context context, List<String> settings,
			Map<String, Integer> map, Fragment fragment, DataManager dataManager) {

		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.list = settings;
		this.map = map;
		this.context = context;
		this.mDataManager = dataManager;
		this.mFragmnet = fragment;
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
	}

	@Override
	public int getCount() {
		return list != null ? list.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = mInflater.inflate(R.layout.setting_row_layout_new,
				parent, false);
		// final int pos = position;

		LanguageTextView property = (LanguageTextView) convertView
				.findViewById(R.id.property);
		final Switch swichbutton = (Switch) convertView
				.findViewById(R.id.switch_button);
		// final ToggleButton button = (ToggleButton) convertView
		// .findViewById(R.id.toggle_button);
		// button.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		//
		// Toast.makeText(context, pos + " " + button.isChecked() ,
		// Toast.LENGTH_LONG).show();
		// mDataManager.getShareSettings((CommunicationOperationListener)
		// mActivity, null);
		// }
		// });

		// button.setOnClickListener((OnClickListener) mFragmnet);
        try {
            swichbutton
                    .setOnCheckedChangeListener(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

		String value = (String) list.get(position);
		if (mApplicationConfigurations.getUserSelectedLanguage() == 0) {
			property.setText(value);
		} else {
			Utils.SetMultilanguageTextOnTextView(context, property, value);
		}

		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				swichbutton.performClick();
			}
		});

		swichbutton.setTag(value);
		swichbutton.setChecked(map.get(value) == 1 ? true : false);
		// button.setTag(value);
		// button.setChecked(map.get(value) == 1 ? true : false);
        try {
            swichbutton
                    .setOnCheckedChangeListener((OnCheckedChangeListener) mFragmnet);
        } catch (Exception e) {
            e.printStackTrace();
        }
		return convertView;
	}

	private static class ViewHolder {
		TextView property;
		// ToggleButton toggleButton;
		// Switch switchbtn;
	}

}
