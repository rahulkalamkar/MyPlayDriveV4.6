package com.hungama.hungamamusic.lite.carmode.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.hungamamusic.lite.carmode.util.GlobalFunction;
import com.hungama.hungamamusic.lite.carmode.view.CustomDialogLayout;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.DataBase;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.hungama.myplay.activity.util.Utils;

import java.util.List;

public class PlayingTrackListAdapter extends BaseAdapter {

    private static final String TAG = PlayingTrackListAdapter.class.getSimpleName();
    private List<Track> mListTracks;
    private Context mCtx;
    private int mSelectedPos = -1;
    private IPlayingTrackAdapterListener mListener;

    public PlayingTrackListAdapter(Context ctx, List<Track> listTracks) {
        this.mListTracks = listTracks;
        this.mCtx = ctx;
    }

    @Override
    public int getCount() {
        return mListTracks.size();
    }

    @Override
    public Object getItem(int position) {
        return (Track) this.mListTracks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setSelectedTrack(int pos) {
        this.mSelectedPos = pos;
    }

    public void setListener(IPlayingTrackAdapterListener listener) {
        this.mListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = ((Activity) mCtx).getLayoutInflater().inflate(R.layout.carmode_item_playing_track, parent, false);

            holder = new ViewHolder();
            holder.ivTrackAvatar = (ImageView) convertView.findViewById(R.id.iv_track_avatar);
            holder.tvTrackTitle = (TextView) convertView.findViewById(R.id.tv_track_title);
            holder.btnDelete = (ImageButton) convertView.findViewById(R.id.btn_track_delete);
            holder.ivOfflineView = (ImageView) convertView.findViewById(R.id.iv_offline_view);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Track track = (Track) getItem(position);
        if (track != null) {
            holder.tvTrackTitle.setText(track.getTitle());

            String[] images = ImagesManager.getImagesUrlArray(track.getImagesUrlArray(), ImagesManager.MUSIC_ART_SMALL,
                    DataManager.getDisplayDensityLabel());
            String imageURL = "";
            if (images != null && images.length > 0) {
                imageURL = images[0];
            }

            if(TextUtils.isEmpty(imageURL))
                imageURL = ImagesManager.getMusicArtSmallImageUrl(track.getImagesUrlArray());

            if (!TextUtils.isEmpty(imageURL)) {
                PicassoUtil.with(mCtx).loadWithFit(null, imageURL, holder.ivTrackAvatar, -1, TAG);
            }

            if (mSelectedPos != -1 && position == mSelectedPos) {
                convertView.setBackgroundResource(R.color.carmode_sub_bg_color);
            } else {
                convertView.setBackgroundResource(R.color.carmode_playing_track_item_color);
            }

            holder.btnDelete.setTag(position);
            holder.btnDelete.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    final int pos = (Integer) v.getTag();
                    mListener.onTrackDelete(pos);
                }
            });

            final DataManager dataManager = DataManager.getInstance(mCtx);
            final ApplicationConfigurations appConfig = dataManager.getApplicationConfigurations();
            try {
                final DataBase.CacheState cacheState = DBOHandler.getTrackCacheState(mCtx, "" + track.getId());
                if ((appConfig.getSaveOfflineMode() || !Utils.isConnected()) && cacheState != DataBase.CacheState.CACHED) { // Stored downloaded track.
                    holder.ivOfflineView.setVisibility(View.VISIBLE);
                    holder.ivOfflineView.setClickable(true);
                    holder.ivOfflineView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            GlobalFunction.showMessageDialog((Activity) mCtx, CustomDialogLayout.DialogType.MESSAGE, R.string.caching_text_message_go_online_global_menu, null);
                        }
                    });
                } else {
                    holder.ivOfflineView.setVisibility(View.GONE);
                    holder.ivOfflineView.setClickable(false);
                    holder.ivOfflineView.setOnClickListener(null);
                }
            }catch (Exception e) {
                e.printStackTrace();
            }

        }
        return convertView;
    }


    public interface IPlayingTrackAdapterListener {
        void onTrackDelete(int position);
    }

    static class ViewHolder {
        ImageView ivTrackAvatar;
        TextView tvTrackTitle;
        ImageButton btnDelete;
        ImageView ivOfflineView;
    }

}
