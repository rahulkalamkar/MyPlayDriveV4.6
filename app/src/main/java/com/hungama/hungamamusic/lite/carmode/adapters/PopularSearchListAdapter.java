package com.hungama.hungamamusic.lite.carmode.adapters;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;

public class PopularSearchListAdapter extends BaseAdapter {
		
	private Context mCtx;
	private List<String> mListPopularSearches;
	
	public PopularSearchListAdapter (Context ctx, List<String> listPopularSearches) {
		this.mCtx = ctx;
		this.mListPopularSearches = listPopularSearches;
	}
	
	
	@Override
	public int getCount() {
		return mListPopularSearches.size();
	}

	@Override
	public Object getItem(int position) {
		return mListPopularSearches.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		final ViewHolder holder;
		
		if(convertView == null) {
				
			convertView = ((Activity)mCtx).getLayoutInflater().inflate(R.layout.carmode_item_popular_searches, parent, false);
			
			holder = new ViewHolder();
			holder.tvPopularSearchText = (TextView) convertView.findViewById(R.id.tv_popular_text_search);
			
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		String popularSearchText = (String) getItem(position);
		if(popularSearchText != null) {
			holder.tvPopularSearchText.setText(popularSearchText);
		}
		
		
		return convertView;
	}
	
	
	static class ViewHolder {
		TextView tvPopularSearchText;
	}
}
