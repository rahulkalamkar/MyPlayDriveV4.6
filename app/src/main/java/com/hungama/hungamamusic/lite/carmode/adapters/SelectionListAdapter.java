package com.hungama.hungamamusic.lite.carmode.adapters;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;

public class SelectionListAdapter extends BaseAdapter {

	private Context mCtx;
	private List<String> mListCategories;
	private int mSelectedPos = -1;

	public SelectionListAdapter(Context ctx, List<String> listCategories, int initialCate) {
		this.mCtx = ctx;
		this.mListCategories = listCategories;
		this.mSelectedPos = initialCate;
	}

	@Override
	public int getCount() {
		return this.mListCategories.size();
	}

	@Override
	public Object getItem(int position) {
		return (String) mListCategories.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setSelectPos(int pos) {
		this.mSelectedPos = pos;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final ViewHolder holder;

		if (convertView == null) {
			convertView = ((Activity) mCtx).getLayoutInflater().inflate(R.layout.carmode_item_selection, parent, false);

			holder = new ViewHolder();
			holder.tvCateName = (TextView) convertView.findViewById(R.id.tv_name);
			holder.ivTick = (ImageView) convertView.findViewById(R.id.iv_tick);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final String cate = (String) getItem(position);
		if (cate != null) {
			holder.tvCateName.setText(cate);

			if (mSelectedPos == position) {
				holder.ivTick.setVisibility(View.VISIBLE);
				convertView.setBackgroundColor(Color.TRANSPARENT);
			} else {
				holder.ivTick.setVisibility(View.INVISIBLE);
				convertView.setBackgroundResource(R.color.carmode_sub_bg_color);
			}

		}

		return convertView;
	}

	static class ViewHolder {
		TextView tvCateName;
		ImageView ivTick;
	}
}
