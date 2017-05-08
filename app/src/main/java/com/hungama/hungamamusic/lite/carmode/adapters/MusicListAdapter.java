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
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.util.Utils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MusicListAdapter extends BaseAdapter {

    public static final String TAG = MusicListAdapter.class.getSimpleName();

    private List<MediaItem> mListMedia;
    private Context mContext;
    private boolean bIsMediaMenuExpanded;
    private MediaItemEvents mMediaItemEvents;
    private int mSelectedPos = -1;
    private boolean isEnabledItemEvent = true;
    private boolean isOfflineMode = false;
    private boolean bReachedTotalItems;

    public MusicListAdapter(Context ctx, List<MediaItem> listMedia) {
        this.mListMedia = listMedia;
        this.mContext = ctx;
    }

    public void setReachedTotalItems(boolean bool) {
        this.bReachedTotalItems = bool;
    }

    public void enableItemEvent(boolean bool) {
        this.isEnabledItemEvent = bool;
        this.notifyDataSetChanged();
    }

    @Override
    public boolean isEnabled(int position) {
        return isEnabledItemEvent;
    }

    public void setSelectedPos(int selectedPos) {
        this.mSelectedPos = selectedPos;
    }

    public void setMediaMenuExpanded(boolean isMediaMenuExpanded) {
        this.bIsMediaMenuExpanded = isMediaMenuExpanded;
    }

    public void setMediaItemEvents(MediaItemEvents events) {
        this.mMediaItemEvents = events;
    }

    public void setOfflineMode(boolean isInOffline) {
        this.isOfflineMode = isInOffline;
    }


    @Override
    public int getCount() {
        return mListMedia.size();
    }

    @Override
    public Object getItem(int position) {
        return mListMedia.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolderItem holder;

        if (convertView == null) {
            convertView = ((Activity) mContext).getLayoutInflater().inflate(R.layout.carmode_listsongitem, parent, false);

            holder = new ViewHolderItem();
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
            holder.tvSubTitle = (TextView) convertView.findViewById(R.id.tv_sub_title);
            holder.albumThumb = (ImageView) convertView.findViewById(R.id.album_thumb);
            holder.btnMediaMenu = (ImageButton) convertView.findViewById(R.id.btn_media_menu);
            holder.btnMediaMenu.setFocusable(false);
            holder.ivOfflineView = (ImageView) convertView.findViewById(R.id.iv_offline_view);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolderItem) convertView.getTag();
        }

        final MediaItem mMediaItem = (MediaItem) getItem(position);

        if (mMediaItem != null) {
            final String title = mMediaItem.getTitle();
            final String subTitle = mMediaItem.getAlbumName();

            if (mMediaItem.getMediaType() == MediaType.ALBUM) {

                if ((title != null) && !title.isEmpty()) {
                    holder.tvTitle.setText(title);
                } else if ((subTitle != null) && !subTitle.isEmpty()) {
                    holder.tvTitle.setText(subTitle);
                } else {
                    holder.tvTitle.setTag(R.string.msg_not_found);
                }

                if (mMediaItem.getMusicTrackCount() == 0) {
                    holder.tvSubTitle.setVisibility(View.GONE);
                } else {
                    holder.tvSubTitle.setText(mMediaItem.getMusicTrackCount() + " Songs");
                }
            } else if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
                holder.tvTitle.setText(mMediaItem.getTitle());
                holder.tvSubTitle.setText(mMediaItem.getMusicTrackCount() + " Songs");
            } else if (mMediaItem.getMediaType() == MediaType.TRACK) {
                holder.tvTitle.setText(mMediaItem.getTitle());
                holder.tvSubTitle.setText(mMediaItem.getAlbumName());
            } else if (mMediaItem.getMediaType() == MediaType.ARTIST) {
                holder.tvTitle.setText(mMediaItem.getTitle());
                holder.tvSubTitle.setVisibility(View.GONE);
            }

            final String[] images = ImagesManager.getImagesUrlArray(mMediaItem.getImagesUrlArray(), ImagesManager.HOME_MUSIC_TILE,
                    DataManager.getDisplayDensityLabel());

            String imageURL = "";
            if (images != null && images.length > 0) {
                imageURL = images[0];
            }

            if(TextUtils.isEmpty(imageURL))
                imageURL = ImagesManager.getMusicArtSmallImageUrl(mMediaItem.getImagesUrlArray());

            Picasso.with(mContext).cancelRequest(holder.albumThumb);
            if (!TextUtils.isEmpty(imageURL)) {
                Picasso.with(mContext).load(imageURL).placeholder(R.drawable.background_home_tile_album_default).into(holder.albumThumb);
            }

            holder.btnMediaMenu.setTag(position);
            holder.btnMediaMenu.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    int pos = (Integer) v.getTag();
                    if ((mSelectedPos == -1) || (mSelectedPos == pos)) {
                        if (bIsMediaMenuExpanded) { // Close options.
                            vHandleCollapseOptions();
                        } else { // Open options.
                            vHandleExpandOptions(pos);
                        }
                    }
                }
            });

            if ((mSelectedPos == -1) || (mSelectedPos == position)) {
                if (bIsMediaMenuExpanded) {
                    holder.btnMediaMenu.setImageResource(R.drawable.carmode_btn_delete);
                } else {
                    holder.btnMediaMenu.setImageResource(R.drawable.carmode_btn_show_options);
                }
            } else {
                holder.btnMediaMenu.setImageResource(R.drawable.carmode_btn_show_options);
            }

            if (this.isOfflineMode) {
                final DataManager dataManager = DataManager.getInstance(mContext);
                final ApplicationConfigurations appConfig = dataManager.getApplicationConfigurations();
                try {
                    final DataBase.CacheState cacheState = DBOHandler.getTrackCacheState(mContext, "" + mMediaItem.getId());
                    if ((appConfig.getSaveOfflineMode() || !Utils.isConnected()) && cacheState != DataBase.CacheState.CACHED) { // Stored downloaded track.
                        holder.ivOfflineView.setVisibility(View.VISIBLE);
                        holder.ivOfflineView.setClickable(true);
                        holder.ivOfflineView.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                GlobalFunction.showMessageDialog((Activity) mContext, CustomDialogLayout.DialogType.MESSAGE, R.string.message_offline_free_user, null);
                            }
                        });
                    } else {
                        holder.ivOfflineView.setVisibility(View.GONE);
                        holder.ivOfflineView.setClickable(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                holder.ivOfflineView.setVisibility(View.GONE);
                holder.ivOfflineView.setClickable(false);
            }
        }
        return convertView;
    }

    public void vHandleCollapseOptions() {
        this.mMediaItemEvents.onCollapseMenu();
        this.mSelectedPos = -1;
        this.bIsMediaMenuExpanded = false;
    }

    public void vHandleExpandOptions(int pos) {
        this.mMediaItemEvents.onExpandMenu(pos);
        this.mSelectedPos = pos;
        this.bIsMediaMenuExpanded = true;
    }

    public interface MediaItemEvents {
        void onExpandMenu(int mediaItemPos);

        void onCollapseMenu();
    }

    static class ViewHolderItem {
        ImageView albumThumb;
        TextView tvTitle;
        TextView tvSubTitle;
        ImageButton btnMediaMenu;
        ImageView ivOfflineView;
    }
}
