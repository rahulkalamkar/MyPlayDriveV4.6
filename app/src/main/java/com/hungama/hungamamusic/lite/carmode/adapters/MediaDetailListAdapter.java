package com.hungama.hungamamusic.lite.carmode.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.DataBase;
import com.hungama.myplay.activity.data.dao.hungama.Track;

import java.util.List;

public class MediaDetailListAdapter extends BaseAdapter {

    private List<Track> mListTracks;
    private Context mCtx;
    private int mSelectedPos = -1;

    public MediaDetailListAdapter(Context ctx, List<Track> tracks) {
        this.mCtx = ctx;
        this.mListTracks = tracks;
    }

    @Override
    public int getCount() {
        return mListTracks.size();
    }

    @Override
    public Object getItem(int position) {
        return mListTracks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setSelectedTrack(int pos) {
        this.mSelectedPos = pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = ((Activity) mCtx).getLayoutInflater().inflate(R.layout.carmode_item_media_detail, parent, false);

            holder = new ViewHolder();
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
            holder.tvSubTitle = (TextView) convertView.findViewById(R.id.tv_sub_title);
            holder.ivCacheState = (ImageView) convertView.findViewById(R.id.iv_cache_state);
            holder.pbDownloadQueued = (ProgressBar) convertView.findViewById(R.id.pb_download_queued);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Track track = (Track) getItem(position);
        if (track != null) {
            holder.tvTitle.setText(track.getTitle());
            holder.tvSubTitle.setText(track.getAlbumName());

            if (mSelectedPos != -1 && position == mSelectedPos) {
                convertView.setBackgroundResource(R.color.carmode_sub_bg_color);
            } else {
                convertView.setBackgroundResource(R.color.carmode_playing_track_item_color);
            }

            // Cache state
            // Reset before updating Cache state.
            holder.ivCacheState.setVisibility(View.GONE);
            holder.pbDownloadQueued.setVisibility(View.GONE);

            final DataBase.CacheState cacheState = DBOHandler.getTrackCacheState(mCtx, "" + track.getId());
            switch (cacheState) {
                case CACHED:
                    holder.ivCacheState.setVisibility(View.VISIBLE);
                    holder.ivCacheState.setImageResource(R.drawable.carmode_cache_state_cached);
                    break;

                case CACHING:
                    holder.pbDownloadQueued.setVisibility(View.VISIBLE);
                    break;

                case FAILED:
                    break;
                case NOT_CACHED:
                    break;
                case QUEUED:
                    holder.ivCacheState.setVisibility(View.VISIBLE);
                    holder.ivCacheState.setImageResource(R.drawable.carmode_cache_state_queued);
                    break;
                default:
                    break;
            }
        }

        return convertView;
    }

    static class ViewHolder {
        TextView tvTitle;
        TextView tvSubTitle;
        ImageView ivCacheState;
        ProgressBar pbDownloadQueued;
    }

}
