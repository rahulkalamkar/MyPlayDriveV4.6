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
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;

import java.util.List;

public class PlaylistListAdapter extends BaseAdapter {

	private List<MediaItem> mListMediaItems;
	private Context mCtx;
	private PlaylistItemEvents mListener;
	private boolean bIsMediaMenuExpanded;
	private int mSelectedPos = -1;

	public PlaylistListAdapter(Context ctx, List<MediaItem> listMediaItems) {
		this.mCtx = ctx;
		this.mListMediaItems = listMediaItems;
	}

	public void setListener(PlaylistItemEvents listener) {
		this.mListener = listener;
	}

	@Override
	public int getCount() {
		return mListMediaItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mListMediaItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final ViewHolder holder;

		if (convertView == null) {
			convertView = ((Activity) mCtx).getLayoutInflater().inflate(R.layout.carmode_item_playlist, parent, false);

			holder = new ViewHolder();
			holder.ivSingleAvatar = (ImageView) convertView.findViewById(R.id.iv_single_avatar);
			holder.ivAvatarTrackOne = (ImageView) convertView.findViewById(R.id.iv_avatar_track_one);
			holder.ivAvatarTrackTwo = (ImageView) convertView.findViewById(R.id.iv_avatar_track_two);
			holder.ivAvatarTrackThree = (ImageView) convertView.findViewById(R.id.iv_avatar_track_three);
			holder.ivAvatarTrackFour = (ImageView) convertView.findViewById(R.id.iv_avatar_track_four);
			holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
			holder.tvSubTitle = (TextView) convertView.findViewById(R.id.tv_sub_title);
			holder.btnMediaMenu = (ImageButton) convertView.findViewById(R.id.btn_media_menu);
			holder.btnMediaMenu.setFocusable(false);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final MediaItem media = (MediaItem) getItem(position);
		if (media != null) {
			holder.tvTitle.setText(media.getTitle());
			holder.tvSubTitle.setText(media.getMusicTrackCount() + " Songs");

			String[] images = ImagesManager.getImagesUrlArray(media.getImagesUrlArray(), ImagesManager.MUSIC_ART_SMALL,
					DataManager.getDisplayDensityLabel());

			if (images.length > 0) {
				if (media.getMusicTrackCount() == 1) { // Single avatar
					holder.ivSingleAvatar.setVisibility(View.VISIBLE);
					String imageURL = null;
					if (images != null && images.length > 0) {
						imageURL = images[0];
					}
					if(TextUtils.isEmpty(imageURL))
						imageURL = ImagesManager.getMusicArtSmallImageUrl(media.getImagesUrlArray());
					GlobalFunction.downloadImage((Activity) mCtx, imageURL, holder.ivSingleAvatar);
				} else { // Multiple avatar
					holder.ivSingleAvatar.setVisibility(View.GONE);

					if (images != null && images.length > 0) {
						GlobalFunction.downloadImage((Activity) mCtx, images[0], holder.ivAvatarTrackOne);
					}

					if (images != null && images.length > 1) {
						GlobalFunction.downloadImage((Activity) mCtx, images[1], holder.ivAvatarTrackTwo);
					}

					if (images != null && images.length > 2) {
						GlobalFunction.downloadImage((Activity) mCtx, images[2], holder.ivAvatarTrackThree);
					}

					if (images != null && images.length > 3) {
						GlobalFunction.downloadImage((Activity) mCtx, images[3], holder.ivAvatarTrackFour);
					}
				}
			}

			holder.btnMediaMenu.setTag(position);
			holder.btnMediaMenu.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					int pos = (Integer) v.getTag();
					if ((mSelectedPos == -1) || (mSelectedPos == pos)) {
						if (bIsMediaMenuExpanded) { // Close options.
							mListener.onCollapseMenu();
							holder.btnMediaMenu.setImageResource(R.drawable.carmode_btn_show_options);

							mSelectedPos = -1;
						} else { // Open options.
							mListener.onExpandMenu(pos);
							holder.btnMediaMenu.setImageResource(R.drawable.carmode_btn_delete);
							mSelectedPos = pos;
						}

						bIsMediaMenuExpanded = !bIsMediaMenuExpanded;
					}
				}
			});
		}

		return convertView;
	}

	public interface PlaylistItemEvents {
		void onExpandMenu(int mediaItemPos);

		void onCollapseMenu();
	}

	static class ViewHolder {
		ImageView ivSingleAvatar;
		ImageView ivAvatarTrackOne;
		ImageView ivAvatarTrackTwo;
		ImageView ivAvatarTrackThree;
		ImageView ivAvatarTrackFour;
		TextView tvTitle;
		TextView tvSubTitle;
		ImageButton btnMediaMenu;
	}

}
