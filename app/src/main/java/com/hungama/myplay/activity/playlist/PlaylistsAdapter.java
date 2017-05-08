package com.hungama.myplay.activity.playlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;

import java.util.List;

public class PlaylistsAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<Playlist> list;

	public PlaylistsAdapter(Context context, List<Playlist> playlists) {

		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.list = playlists;
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view = mInflater
				.inflate(R.layout.itemable_row_item, parent, false);

		TextView tv = (TextView) view.findViewById(R.id.row_item_name);

		Playlist p = (Playlist) list.get(position);
		tv.setText(p.getName() + " " + "(" + p.getNumberOfTracks() + ")");

		return view;

	}

	private static class ViewHolder {
		TextView name;
	}
}
