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
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RadioListAdapter extends BaseAdapter {

    public static final String TAG = RadioListAdapter.class.getSimpleName();

    private Context mCtx;
    private List<MediaItem> mListMedia;
    private PicassoUtil picasso;
    private IRadioListAdapterListener mListener;

    public RadioListAdapter(Context ctx, List<MediaItem> listMedia) {
        this.mCtx = ctx;
        this.mListMedia = listMedia;
        this.picasso = PicassoUtil.with(ctx);
    }

    public void setListener(IRadioListAdapterListener listener) {
        this.mListener = listener;
    }

    @Override
    public int getCount() {
        return this.mListMedia.size();
    }

    @Override
    public Object getItem(int position) {
        return this.mListMedia.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = ((Activity) mCtx).getLayoutInflater().inflate(R.layout.carmode_item_radio, parent, false);

            holder = new ViewHolder();
            holder.albumThumb = (ImageView) convertView.findViewById(R.id.album_thumb);
            holder.title = (TextView) convertView.findViewById(R.id.tv_title);
            holder.btnPlay = (ImageButton) convertView.findViewById(R.id.btn_play);
            holder.btnPlay.setFocusable(false);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final MediaItem radio = (MediaItem) getItem(position);
        if (radio != null) {
            holder.title.setText(radio.getTitle());

            String[] images = ImagesManager.getImagesUrlArray(radio.getImagesUrlArray(), ImagesManager.RADIO_LIST_ART,
                    DataManager.getDisplayDensityLabel());

            String imageURL = "";
            if (images != null) {
                if (images.length > 0) {
                    imageURL = images[0];
                    if(TextUtils.isEmpty(imageURL))
                        imageURL = ImagesManager.getMusicArtSmallImageUrl(radio.getImagesUrlArray());
                } else if (images.length == 0) {
                    images = ImagesManager.getImagesUrlArray(radio.getImagesUrlArray(), ImagesManager.HOME_MUSIC_TILE,
                            DataManager.getDisplayDensityLabel());
                    if (images != null && images.length > 0) {
                        imageURL = images[0];
                    }
                    if(TextUtils.isEmpty(imageURL))
                        imageURL = ImagesManager.getMusicArtSmallImageUrl(radio.getImagesUrlArray());
                }
            }

            Picasso.with(mCtx).cancelRequest(holder.albumThumb);
            if (!TextUtils.isEmpty(imageURL)) {
                Picasso.with(mCtx).load(imageURL).placeholder(R.drawable.background_home_tile_album_default).into(holder.albumThumb);
            }


            holder.btnPlay.setTag(position);
            holder.btnPlay.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    int pos = (Integer) v.getTag();
                    mListener.onGoToRadioPlayer(mListMedia.get(pos));
                }
            });
        }

        return convertView;
    }

    public interface IRadioListAdapterListener {
        void onGoToRadioPlayer(MediaItem radioItem);
    }

    static class ViewHolder {
        ImageView albumThumb;
        TextView title;
        ImageButton btnPlay;
    }

}
