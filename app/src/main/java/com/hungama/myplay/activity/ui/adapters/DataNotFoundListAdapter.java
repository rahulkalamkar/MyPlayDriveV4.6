package com.hungama.myplay.activity.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;


public class DataNotFoundListAdapter extends BaseAdapter {

	private String mCrime;

	public DataNotFoundListAdapter(String mCrime) {
		this.mCrime = mCrime;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		convertView = LayoutInflater.from(parent.getContext()).inflate(
				R.layout.text_layout, parent, false);
		TextView text_layout = (TextView) convertView
				.findViewById(R.id.txt_heading);
		text_layout.setText(mCrime);
		return convertView;
	}

	@Override
	public int getCount() {
		return 1;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}
}
