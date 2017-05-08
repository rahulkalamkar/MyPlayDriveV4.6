package com.hungama.myplay.activity.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.DataBase.CacheState;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.campaigns.PlacementType;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.PromoUnit;
import com.hungama.myplay.activity.data.events.AppEvent;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.adapters.MyAdapter.ViewHolderSong.subLayout;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.ui.widgets.CustomCacheStateProgressBar;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.FlurryConstants.FlurrySubSectionDescription;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.hungama.myplay.activity.util.PicassoUtil.PicassoCallBack;
import com.hungama.myplay.activity.util.PicassoUtil.PicassoTarget;
import com.hungama.myplay.activity.util.QuickAction;
import com.hungama.myplay.activity.util.Utils;
import com.squareup.picasso.Picasso.LoadedFrom;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class MyAdapter extends RecyclerView.Adapter<ViewHolder>
		implements OnClickListener {
	private String TAG = "MyAdapterNew";
	private List<Object> itemsData;

	// int counter;
	private Activity mActivity;
	private MediaCategoryType mCategoryType;

	private HashMap<Integer, Placement> mPlacementMap = new HashMap<Integer, Placement>();

	private OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener;
	private String mFlurrySubSectionDescription;
	private boolean firstPositionPost, firstPromoPost;
	boolean showDeleteButton;
	boolean showDownloadOption = true;
	DisplayMetrics metrics;
	int width;
	private PicassoUtil picasso;
	String tag;
	boolean isEnglish = false;
	private ApplicationConfigurations mApplicationConfigurations;
	boolean needToShowTrend = false;
	boolean isPlaylistScreen = false;
	String txtAdvertise = "Advertisement";
	private boolean isCachedDataLoaded = false;

	private final boolean isDetailForTrack=false;
	private boolean isNeedAdvertiseText=true;

	public MyAdapter(List<Object> itemsData, Activity mActivity,
			MediaCategoryType mCategoryType, MediaContentType mContentType,
			CampaignsManager manager, boolean showDeleteButton,
			String flurrySubSectionDescription, RecyclerView listView) {
		mApplicationConfigurations = ApplicationConfigurations
				.getInstance(mActivity);
		isEnglish = mApplicationConfigurations.isLanguageSupportedForWidget();
		Logger.i("isEnglish", "isSupportedLang:" + isEnglish);
		// (mApplicationConfigurations.getUserSelectedLanguage() == 0);
		this.itemsData = itemsData;
		if(itemsData!=null && itemsData.size()>0){
			isCachedDataLoaded = true;
		}
		this.showDeleteButton = showDeleteButton;
		this.mActivity = mActivity;
		this.mCategoryType = mCategoryType;
		this.mCampaignsManager = manager;
		mFlurrySubSectionDescription = flurrySubSectionDescription;
		type = getPlacementType();
		firstPositionPost = true;
		firstPromoPost = true;
		viewedPositions = new Vector<String>();
		this.listView = listView;

		final WindowManager w = (WindowManager) mActivity
				.getSystemService(Context.WINDOW_SERVICE);
		final Display d = w.getDefaultDisplay();
		metrics = new DisplayMetrics();
		d.getMetrics(metrics);
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		width = (metrics.widthPixels - 3 * ((int) mActivity.getResources()
				.getDimension(R.dimen.home_music_tile_margin))) / 2;
		// if(this.mCategoryType== MediaCategoryType.)
		if (mCategoryType == null) {
			tag = PicassoUtil.PICASSO_TAG;
		} else if (mCategoryType.equals(MediaCategoryType.LATEST)) {
			tag = PicassoUtil.PICASSO_NEW_MUSIC_LIST_TAG;
		} else if (mCategoryType.equals(MediaCategoryType.POPULAR)) {
			tag = PicassoUtil.PICASSO_POP_MUSIC_LIST_TAG;
		}
		txtAdvertise = Utils.getMultilanguageText(mActivity,
				mActivity.getString(R.string.txtAdvertisement));
		picasso = PicassoUtil.with(mActivity);
	}

	public void setTrendVisibility(boolean needToShowTrend) {
		this.needToShowTrend = needToShowTrend;
	}

	public void showDownloadOption(boolean showDownloadOption) {
		this.showDownloadOption = showDownloadOption;
	}

	public void setIsPlaylistScreen(boolean isPlaylistScreen) {
		this.isPlaylistScreen = isPlaylistScreen;
	}

	public static final int PLAYLIST = 1;
	public static final int SONGALBUM = 0;
	public static final int PROMOUNIT = 2;

	@Override
	public int getItemViewType(int position) {

		Object obj = itemsData.get(position);
		// RelativeLayout rl = viewHolder.view_left;

		if (obj instanceof MediaItem)
			return PLAYLIST;
		else if (obj instanceof PromoUnit)
			return PROMOUNIT;
		else
			return SONGALBUM;
	}

	// Create new views (invoked by the layout manager)
	public ViewHolder onCreateViewHolder(ViewGroup parent,
			int viewType) {

		if (isEnglish)
			return onCreateViewHolderEnglish(parent, viewType);

		// create a new view
		if (viewType == SONGALBUM) {
			View itemLayoutView = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.item_table, null);

			View view_left_tile = itemLayoutView
					.findViewById(R.id.view_left_tile);
			LayoutParams lp1 = (LayoutParams) view_left_tile.getLayoutParams();
//			lp1.height = width;
			lp1.width = width;
			view_left_tile.setLayoutParams(lp1);

            int image_size = width/*-10*/;
            View view_left_tile_image = view_left_tile
                    .findViewById(R.id.home_music_tile_image);
            RelativeLayout.LayoutParams lp11 = (RelativeLayout.LayoutParams) view_left_tile_image.getLayoutParams();
            lp11.height = image_size;
            lp11.width = image_size;
            view_left_tile_image.setLayoutParams(lp11);

			View view_right_tile = itemLayoutView
					.findViewById(R.id.view_right_tile);
			lp1 = (LayoutParams) view_right_tile.getLayoutParams();
//			lp1.height = width;
			lp1.width = width;
			view_right_tile.setLayoutParams(lp1);

            View view_right_tile_image = view_right_tile
                    .findViewById(R.id.home_music_tile_image);
            lp11 = (RelativeLayout.LayoutParams) view_right_tile_image.getLayoutParams();
            lp11.height = image_size;
            lp11.width = image_size;
            view_right_tile_image.setLayoutParams(lp11);

			ViewHolderSong viewHolderSongAlbum = new ViewHolderSong(
					itemLayoutView);

			return viewHolderSongAlbum;
		} else if (viewType == PROMOUNIT) {
			View itemLayoutView = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.item_list_promo_unit, null);
			ViewHolder_Promo_Unit viewHolder_Promo_Unit = new ViewHolder_Promo_Unit(
					itemLayoutView);
			return viewHolder_Promo_Unit;
		} else {
			View itemLayoutView = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.item_table_playlist, null);
			// create ViewHolder
			ViewHolder_Playlist viewHolderPlaylist = new ViewHolder_Playlist(
					itemLayoutView);

			return viewHolderPlaylist;
		}
	}

	private ViewHolder onCreateViewHolderEnglish(ViewGroup parent,
			int viewType) {

		// create a new view
		if (viewType == SONGALBUM) {
			View itemLayoutView = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.item_table_eng, null);
			View view_left_tile = itemLayoutView
					.findViewById(R.id.view_left_tile);
            LayoutParams lp1 = (LayoutParams) view_left_tile.getLayoutParams();
//            lp1.height = width;
            lp1.width = width;
            view_left_tile.setLayoutParams(lp1);

            int image_size = width/*-10*/;
            View view_left_tile_image = view_left_tile
                    .findViewById(R.id.home_music_tile_image);
            RelativeLayout.LayoutParams lp11 = (RelativeLayout.LayoutParams) view_left_tile_image.getLayoutParams();
			lp11.height = image_size;
			lp11.width = image_size;
            view_left_tile_image.setLayoutParams(lp11);

			View view_right_tile = itemLayoutView
					.findViewById(R.id.view_right_tile);
			lp1 = (LayoutParams) view_right_tile.getLayoutParams();
//			lp1.height = width;
			lp1.width = width;
			view_right_tile.setLayoutParams(lp1);

            View view_right_tile_image = view_right_tile
                    .findViewById(R.id.home_music_tile_image);
            lp11 = (RelativeLayout.LayoutParams) view_right_tile_image.getLayoutParams();
            lp11.height = image_size;
            lp11.width = image_size;

//			View play = itemLayoutView
//					.findViewById(R.id.home_music_tile_button_play);
//			ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) play.getLayoutParams();
//			params.topMargin = image_size/2;
//			play.setLayoutParams(params);

			ViewHolderSong_English viewHolderSongAlbum = new ViewHolderSong_English(
					itemLayoutView);



			return viewHolderSongAlbum;
		} else if (viewType == PROMOUNIT) {
			View itemLayoutView = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.item_list_promo_unit, null);
			ViewHolder_Promo_Unit viewHolder_Promo_Unit = new ViewHolder_Promo_Unit(
					itemLayoutView);
			return viewHolder_Promo_Unit;
		} else {
			View itemLayoutView = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.item_table_playlist_eng, null);
			// create ViewHolder
			ViewHolder_Playlist_English viewHolderPlaylist = new ViewHolder_Playlist_English(
					itemLayoutView);

			return viewHolderPlaylist;
		}
	}

	// Replace the contents of a view (invoked by the l ComboMediaItem
	// comboMediaItem = (ComboMediaItem) obj;ayout manager)
	@Override
	public void onBindViewHolder(
			ViewHolder viewHolder,
			int position) {
		// - get data from your itemsData at this position
		// - replace the contents of the view with that itemsData

		if (isEnglish) {
			onBindViewHolderEnglish(viewHolder, position);
			return;
		}

		MediaItem mediaItem;

		Object obj = itemsData.get(position);
		// RelativeLayout rl = viewHolder.view_left;

		if (obj != null && obj instanceof MediaItem) {
			// display playlist
			ViewHolder_Playlist viewHolderPlaylist = null;
			if (viewHolder instanceof ViewHolder_Playlist) {
				viewHolderPlaylist = (ViewHolder_Playlist) viewHolder;
			} else
				return;
			mediaItem = (MediaItem) obj;
			getMusicViewPlayList(mediaItem, position, viewHolderPlaylist);
		} else if (obj != null && obj instanceof PromoUnit) {
			loadPromoUnit(viewHolder, (PromoUnit) obj);
			return;
		} else {
			// display song or album
			ViewHolderSong viewHolderSongAlbum = null;
			if (viewHolder instanceof ViewHolder) {
				viewHolderSongAlbum = (ViewHolderSong) viewHolder;
			}

			ComboMediaItem comboMediaItem = (ComboMediaItem) obj;

			mediaItem = comboMediaItem.left;
			if (mediaItem != null)
				getMusicView(viewHolderSongAlbum.left, mediaItem, position,
						viewHolderSongAlbum);

			mediaItem = comboMediaItem.right;
			if (mediaItem != null) {
				viewHolderSongAlbum.right.parent.setVisibility(View.VISIBLE);
				getMusicView(viewHolderSongAlbum.right, mediaItem, position,
						viewHolderSongAlbum);
			} else
				viewHolderSongAlbum.right.parent.setVisibility(View.INVISIBLE);
		}
	}

	int promoWidth, promoHeight;

	private void loadPromoUnit(
			ViewHolder viewHolder,
			final PromoUnit mPromoUnit) {
		final ViewHolder_Promo_Unit viewHolder_Promo_Unit = (ViewHolder_Promo_Unit) viewHolder;
		if (promoHeight == 0 || promoWidth == 0) {
			promoWidth = (int) (HomeActivity.metrics.widthPixels - (2 * mActivity
					.getResources()
					.getDimension(R.dimen.home_music_tile_margin)));
			viewHolder_Promo_Unit.imageTile.getLayoutParams().width = promoWidth;
			promoHeight = (int) (promoWidth / 4);
		}
		if (promoHeight != 0)
			viewHolder_Promo_Unit.imageTile.getLayoutParams().height = promoHeight;

		String[] images = ImagesManager.getImagesUrlArray(
				mPromoUnit.getImagesUrlArray(), ImagesManager.PROMO_UNIT_SIZE,
				DataManager.getDisplayDensityLabel());
		if (images != null && images.length > 0) {
			if (!TextUtils.isEmpty(images[0])) {
				picasso.loadWithFit(new PicassoCallBack() {
					@Override
					public void onSuccess() {
						viewHolder_Promo_Unit.imageTile
								.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {

										if(TextUtils.isEmpty(mPromoUnit.getLanding_url()))
											return;
										Utils.performclickEventAction(
												mActivity,
												mPromoUnit.getLanding_url());

										String bannerLocation = "music_new";
										if (mCategoryType.equals(MediaCategoryType.LATEST)) {
											bannerLocation = "music_new";
										} else if (mCategoryType.equals(MediaCategoryType.POPULAR)) {
											bannerLocation = "music_popular";
										}
										Analytics.postPromoAppEvent(mActivity, mPromoUnit, "banner_click", bannerLocation);
									}
								});

//						Analytics.postPromoAppEvent(mActivity, mPromoUnit, "banner_view", mFlurrySubSectionDescription);

						if (type == PlacementType.MUSIC_NEW && firstPromoPost) {
							firstPromoPost = false;
							Checkforpost(0, 0);
						}

//						try{
//							int consumerId = mApplicationConfigurations
//									.getConsumerID();
//							String deviceId = mApplicationConfigurations
//									.getDeviceID();
//
//							DataManager mDataManager = DataManager.getInstance(mActivity);
//							String timeStamp = mDataManager.getDeviceConfigurations()
//									.getTimeStampDelta();
//
//							Map<String, String> extraData = new HashMap<String, String>();
//							extraData.put("action", "banner_view");
//							extraData.put("banner_id", "" + mPromoUnit.getPromo_id());
//							extraData.put("banner_name", mPromoUnit.getPromo_name());
//							AppEvent campaignPlayEvent = new AppEvent(
//									consumerId, deviceId, "app_action", timeStamp, 0, 0, null);
//							mDataManager.addEvent(campaignPlayEvent);
//						} catch (Exception e) {
//							Logger.printStackTrace(e);
//						}
					}

					@Override
					public void onError() {
					}
				}, images[0], viewHolder_Promo_Unit.imageTile,
						R.drawable.background_home_tile_album_default, tag);
			}
		}
	}

	private void onBindViewHolderEnglish(
			ViewHolder viewHolder,
			int position) {
		// - get data from your itemsData at this position
		// - replace the contents of the view with that itemsData
		Logger.s(" ::::::::::::::;; onBindViewHolderEnglish");
		MediaItem mediaItem;

		Object obj = itemsData.get(position);

		if (obj != null && obj instanceof MediaItem) {
			// display playlist
			ViewHolder_Playlist_English viewHolderPlaylist = null;
			if (viewHolder instanceof ViewHolder_Playlist_English) {
				viewHolderPlaylist = (ViewHolder_Playlist_English) viewHolder;
			} else
				return;
			mediaItem = (MediaItem) obj;
			getMusicViewPlayList(mediaItem, position, viewHolderPlaylist);
		} else if (obj != null && obj instanceof PromoUnit) {
			loadPromoUnit(viewHolder, (PromoUnit) obj);
			return;
		} else {
			// display song or album
			ViewHolderSong_English viewHolderSongAlbum = null;
			if (viewHolder instanceof ViewHolder) {
				viewHolderSongAlbum = (ViewHolderSong_English) viewHolder;
			}

			ComboMediaItem comboMediaItem = (ComboMediaItem) obj;

			mediaItem = comboMediaItem.left;
			if (mediaItem != null)
				getMusicView(viewHolderSongAlbum.left, mediaItem, position,
						viewHolderSongAlbum);

			mediaItem = comboMediaItem.right;
			if (mediaItem != null) {
				viewHolderSongAlbum.right.parent.setVisibility(View.VISIBLE);
				getMusicView(viewHolderSongAlbum.right, mediaItem, position,
						viewHolderSongAlbum);
			} else
				viewHolderSongAlbum.right.parent.setVisibility(View.INVISIBLE);
		}
	}

	// inner class to hold a reference to each item of RecyclerView
	public static class ViewHolderSong extends ViewHolder {

		class subLayout {
			View parent;
			ImageView imageTile, iv_selector;
			ImageView iv_home_tile_options;
			LanguageTextView textTitle;
			LanguageTextView textDescription;
			CustomCacheStateProgressBar progressCacheState;
			RelativeLayout rl_bg;
			ImageButton buttonPlay;

			public subLayout(View view_right) {
				parent = view_right;
				// song detail
				imageTile = (ImageView) view_right
						.findViewById(R.id.home_music_tile_image);
				iv_selector = (ImageView) view_right
						.findViewById(R.id.iv_selector);
				iv_home_tile_options = (ImageView) view_right
						.findViewById(R.id.iv_home_tile_options);
				textTitle = (LanguageTextView) view_right
						.findViewById(R.id.home_music_tile_title);
				buttonPlay = (ImageButton) view_right
						.findViewById(R.id.home_music_tile_button_play);
				buttonPlay.setAlpha(0.8f);
				rl_bg = (RelativeLayout) view_right
						.findViewById(R.id.rl_bg_song);
				textDescription = (LanguageTextView) view_right
						.findViewById(R.id.home_music_tile_description);
				progressCacheState = (CustomCacheStateProgressBar) view_right
						.findViewById(R.id.home_music_tile_progress_cache_state);

				// album detail
			}
		}

		// public View itemLayoutView;
		// public RelativeLayout view_left, view_right, view_playlist_tile;

		subLayout left;
		subLayout right;

		public ViewHolderSong(View itemLayoutView) {
			super(itemLayoutView);

			left = new subLayout(
					itemLayoutView.findViewById(R.id.view_left_tile));
			right = new subLayout(
					itemLayoutView.findViewById(R.id.view_right_tile));
			itemLayoutView.setTag(this);
		}
	}

	// inner class to hold a reference to each item of RecyclerView
	public static class ViewHolder_Playlist extends ViewHolder {
		public View itemLayoutView;
		ImageView imageTile;
		LinearLayout ll_images;
		// ImageView imageTile_playlist;
		ImageView iv_selector;
		ImageView iv_home_tile_options_playlist;
		LanguageTextView textTitle_playlist;
		LanguageTextView textDescription_playlist;
		CustomCacheStateProgressBar progressCacheState_playlist;
		ImageButton buttonPlay_playlist;
		ImageView home_music_tile_image1, home_music_tile_image2,
				home_music_tile_image3, home_music_tile_image4,
				home_music_tile_image5, home_music_tile_image6;
		RelativeLayout view_playlist_tile;

		public ViewHolder_Playlist(View itemLayoutView) {
			super(itemLayoutView);
			this.itemLayoutView = itemLayoutView;

			imageTile = (ImageView) itemLayoutView
					.findViewById(R.id.home_music_tile_image);
			ll_images = (LinearLayout) itemLayoutView
					.findViewById(R.id.ll_images);

			view_playlist_tile = (RelativeLayout) (itemLayoutView
					.findViewById(R.id.view_playlist_tile));
			iv_selector = (ImageView) itemLayoutView
					.findViewById(R.id.iv_selector);

			home_music_tile_image1 = (ImageView) itemLayoutView
					.findViewById(R.id.home_music_tile_image1);
			home_music_tile_image2 = (ImageView) itemLayoutView
					.findViewById(R.id.home_music_tile_image2);
			home_music_tile_image3 = (ImageView) itemLayoutView
					.findViewById(R.id.home_music_tile_image3);
			home_music_tile_image4 = (ImageView) itemLayoutView
					.findViewById(R.id.home_music_tile_image4);
			home_music_tile_image5 = (ImageView) itemLayoutView
					.findViewById(R.id.home_music_tile_image5);
			home_music_tile_image6 = (ImageView) itemLayoutView
					.findViewById(R.id.home_music_tile_image6);

			// playlsit detail
			// imageTile_playlist = (ImageView) view_playlist_tile
			// .findViewById(R.id.home_music_tile_image);
			iv_home_tile_options_playlist = (ImageView) view_playlist_tile
					.findViewById(R.id.iv_home_tile_options);
			buttonPlay_playlist = (ImageButton) view_playlist_tile
					.findViewById(R.id.home_music_tile_button_play);
			buttonPlay_playlist.setAlpha(0.8f);
			textTitle_playlist = (LanguageTextView) view_playlist_tile
					.findViewById(R.id.home_music_tile_title);

			textDescription_playlist = (LanguageTextView) view_playlist_tile
					.findViewById(R.id.home_music_tile_description);
			progressCacheState_playlist = (CustomCacheStateProgressBar) view_playlist_tile
					.findViewById(R.id.home_music_tile_progress_cache_state);

			itemLayoutView.setTag(this);
		}
	}

	// inner class to hold a reference to each item of RecyclerView
	public static class ViewHolderSong_English extends ViewHolder {

		class subLayout {
			View parent;
			ImageView imageTile, iv_selector;
			ImageView iv_home_tile_options;
			TextView textTitle;
			TextView textDescription;
			CustomCacheStateProgressBar progressCacheState;
			RelativeLayout rl_bg;
			ImageButton buttonPlay;

			public subLayout(View view_right) {
				parent = view_right;
				// song detail
				imageTile = (ImageView) view_right
						.findViewById(R.id.home_music_tile_image);
				iv_selector = (ImageView) view_right
						.findViewById(R.id.iv_selector);
				iv_home_tile_options = (ImageView) view_right
						.findViewById(R.id.iv_home_tile_options);
				textTitle = (TextView) view_right
						.findViewById(R.id.home_music_tile_title);
				buttonPlay = (ImageButton) view_right
						.findViewById(R.id.home_music_tile_button_play);
				buttonPlay.setAlpha(0.8f);
				rl_bg = (RelativeLayout) view_right
						.findViewById(R.id.rl_bg_song);
				textDescription = (TextView) view_right
						.findViewById(R.id.home_music_tile_description);
				progressCacheState = (CustomCacheStateProgressBar) view_right
						.findViewById(R.id.home_music_tile_progress_cache_state);

				// album detail
			}
		}

		// public View itemLayoutView;
		// public RelativeLayout view_left, view_right, view_playlist_tile;

		subLayout left;
		subLayout right;

		public ViewHolderSong_English(View itemLayoutView) {
			super(itemLayoutView);

			left = new subLayout(
					itemLayoutView.findViewById(R.id.view_left_tile));
			right = new subLayout(
					itemLayoutView.findViewById(R.id.view_right_tile));
			itemLayoutView.setTag(this);
		}
	}

	// inner class to hold a reference to each item of RecyclerView
	public static class ViewHolder_Promo_Unit extends ViewHolder {

		public View itemLayoutView;
		ImageView imageTile;

		public ViewHolder_Promo_Unit(View itemView) {
			super(itemView);
			this.itemLayoutView = itemView;
			imageTile = (ImageView) itemView.findViewById(R.id.iv_promo);
		}
	}

	// inner class to hold a reference to each item of RecyclerView
	public static class ViewHolder_Playlist_English extends
			ViewHolder {
		public View itemLayoutView;
		ImageView imageTile;
		LinearLayout ll_images;
		// ImageView imageTile_playlist;
		ImageView iv_selector;
		ImageView iv_home_tile_options_playlist;
		TextView textTitle_playlist;
		TextView textDescription_playlist;
		CustomCacheStateProgressBar progressCacheState_playlist;
		ImageButton buttonPlay_playlist;
		ImageView home_music_tile_image1, home_music_tile_image2,
				home_music_tile_image3, home_music_tile_image4,
				home_music_tile_image5, home_music_tile_image6;
		RelativeLayout view_playlist_tile;

		public ViewHolder_Playlist_English(View itemLayoutView) {
			super(itemLayoutView);
			this.itemLayoutView = itemLayoutView;

			imageTile = (ImageView) itemLayoutView
					.findViewById(R.id.home_music_tile_image);
			ll_images = (LinearLayout) itemLayoutView
					.findViewById(R.id.ll_images);

			view_playlist_tile = (RelativeLayout) (itemLayoutView
					.findViewById(R.id.view_playlist_tile));
			iv_selector = (ImageView) itemLayoutView
					.findViewById(R.id.iv_selector);

			home_music_tile_image1 = (ImageView) itemLayoutView
					.findViewById(R.id.home_music_tile_image1);
			home_music_tile_image2 = (ImageView) itemLayoutView
					.findViewById(R.id.home_music_tile_image2);
			home_music_tile_image3 = (ImageView) itemLayoutView
					.findViewById(R.id.home_music_tile_image3);
			home_music_tile_image4 = (ImageView) itemLayoutView
					.findViewById(R.id.home_music_tile_image4);
			home_music_tile_image5 = (ImageView) itemLayoutView
					.findViewById(R.id.home_music_tile_image5);
			home_music_tile_image6 = (ImageView) itemLayoutView
					.findViewById(R.id.home_music_tile_image6);

			// playlsit detail
			// imageTile_playlist = (ImageView) view_playlist_tile
			// .findViewById(R.id.home_music_tile_image);
			iv_home_tile_options_playlist = (ImageView) view_playlist_tile
					.findViewById(R.id.iv_home_tile_options);
			buttonPlay_playlist = (ImageButton) view_playlist_tile
					.findViewById(R.id.home_music_tile_button_play);
			buttonPlay_playlist.setAlpha(0.8f);
			textTitle_playlist = (TextView) view_playlist_tile
					.findViewById(R.id.home_music_tile_title);

			textDescription_playlist = (TextView) view_playlist_tile
					.findViewById(R.id.home_music_tile_description);
			progressCacheState_playlist = (CustomCacheStateProgressBar) view_playlist_tile
					.findViewById(R.id.home_music_tile_progress_cache_state);

			itemLayoutView.setTag(this);
		}
	}

	// Return the size of your itemsData (invoked by the layout manager)
	public int getItemCount() {
		return itemsData.size();
	}

	// @Override
	// public int getCount() {
	// return itemsData.size();
	// }
	//
	// @Override
	// public Object getItem(int arg0) {
	// return itemsData.get(arg0);
	// }

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	// @Override
	// public View getView(int arg0, View arg1, ViewGroup arg2) {
	// ViewHolder viewHolder;
	// if (arg1 == null) {
	// arg1 = LayoutInflater.from(arg2.getContext()).inflate(
	// R.layout.item_table, null);
	// // create ViewHolder
	// viewHolder = new ViewHolder(arg1);
	// arg1.setTag(viewHolder);
	// } else {
	// viewHolder = (ViewHolder) arg1.getTag();
	// }
	// arg1.setVisibility(View.INVISIBLE);
	// onBindViewHolder(viewHolder, arg0);
	// arg1.setVisibility(View.VISIBLE);
	// return arg1;
	// }

	OnClickListener listener_home_3dots = new OnClickListener() {

		@Override
		public void onClick(final View view) {

			Logger.d(TAG, "Long click on: " + view.toString());
			String tag = (String) view.getTag();
			Placement placement = null;
			try {
				placement = (Placement) view.getTag(R.string.key_placement);
			} catch (Exception e) {
			}

			if (tag != null && placement != null) {
				return;
			} else if (tag != null) {
				return;
			}

			LinearLayout tile_temp = (LinearLayout) view.getParent();
			RelativeLayout tile;
			try {
				tile = (RelativeLayout) tile_temp.getParent().getParent();
			} catch (Exception e) {
				tile = (RelativeLayout) tile_temp.getParent();
			}

			MediaItem mediaItem = (MediaItem) tile.getTag(R.id.view_tag_object);
			int position = (Integer) tile.getTag(R.id.view_tag_position);

			Logger.s("mediaitem title:" + mediaItem.getTitle());
			Logger.s("mediaitem Album Name:" + mediaItem.getAlbumName());
			Logger.s("mediaitem Artist name:" + mediaItem.getArtistName());
			
			QuickAction quickAction = new QuickAction(
					mActivity, QuickAction.VERTICAL,
					mediaItem, position, mOnMediaItemOptionSelectedListener,
					(FragmentActivity) mActivity, mFlurrySubSectionDescription,
					true, showDeleteButton, null, showDownloadOption,needToShowTrend,isPlaylistScreen);

			quickAction.show(view);
			view.setEnabled(false);
			quickAction
					.setOnDismissListener(new QuickAction.OnDismissListener() {
						@Override
						public void onDismiss() {
							view.setEnabled(true);
						}
					});

		}
	};

	// String home_music_tile_album_decription_title;
	// private String search_results_layout_bottom_text_for_track;
	// private String home_music_tile_playlist_decription_title;
	private String home_music_tile_playlist_decription_songs_amount;

	// public static final String VIEW_TAG_ALBUM = "Album";

	// View getRoot(View tempView, int tag) {
	// View root = tempView;
	// while (tempView.getTag(tag) == null) {
	// tempView = (View) tempView.getParent();
	// if (tempView == null)
	// break;
	// else
	// root = tempView;
	// // tile.getTag(R.id.view_tag_object)
	// }
	// return root;
	// }

	private View getRoot(View tempView) {
		View root = tempView;
		while (tempView != null) {
			tempView = (View) tempView.getParent();
			if (tempView == null)
				break;
			else
				root = tempView;
			// tile.getTag(R.id.view_tag_object)
		}
		return root;
	}

	private void getMusicViewPlayList(MediaItem mediaItem, int position,
			final ViewHolder_Playlist viewHolder) {

		// if (position == 0 && mActivity instanceof HomeActivity
		// && ((HomeActivity) mActivity).isPromoUnit()) {
		// LinearLayout.LayoutParams lp = (LayoutParams) ((RelativeLayout)
		// viewHolder.imageTile
		// .getParent()).getLayoutParams();
		// lp.topMargin = 0;
		// } else {
		// LinearLayout.LayoutParams lp = (LayoutParams) ((RelativeLayout)
		// viewHolder.imageTile
		// .getParent()).getLayoutParams();
		// lp.topMargin = (int)
		// mActivity.getResources().getDimension(R.dimen.home_music_tile_margin);
		// }

		if (isAd(mediaItem)) {
			loadAd(viewHolder, position);
			// return;
		} else {
			viewHolder.imageTile.setTag(null);
			viewHolder.imageTile.setTag(R.string.key_placement, null);
			viewHolder.iv_selector.setTag(null);
			viewHolder.iv_selector.setTag(R.string.key_placement, null);

			viewHolder.ll_images.setVisibility(View.VISIBLE);
			viewHolder.buttonPlay_playlist.setVisibility(View.VISIBLE);
			viewHolder.iv_home_tile_options_playlist
					.setVisibility(View.VISIBLE);
			viewHolder.textTitle_playlist.setVisibility(View.VISIBLE);
			viewHolder.textDescription_playlist.setVisibility(View.VISIBLE);
			viewHolder.imageTile.setVisibility(View.GONE);

			if (playlistAdHeight != 0) {
				RelativeLayout rl = (RelativeLayout) viewHolder.ll_images
						.getParent();
				rl.getLayoutParams().height = (int) mActivity.getResources()
						.getDimension(R.dimen.home_music_playlist_height);
			}

			try {
				viewHolder.view_playlist_tile.setOnClickListener(this);
			} catch (Exception e) {
			}

			((LinearLayout) getRoot(viewHolder.view_playlist_tile)).setTag(
					R.string.key_is_ad, (Boolean) false);

			viewHolder.iv_home_tile_options_playlist
					.setOnClickListener(listener_home_3dots);

			if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
				viewHolder.buttonPlay_playlist.setOnClickListener(this);
				viewHolder.iv_selector.setOnClickListener(this);

				downloadingImages(mediaItem, position, viewHolder);

				// sets the title.
				viewHolder.textTitle_playlist.setText(mediaItem.getTitle());

				if (home_music_tile_playlist_decription_songs_amount == null)
					home_music_tile_playlist_decription_songs_amount = Utils
							.getMultilanguageTextLayOut(
									mActivity,
									mActivity
											.getResources()
											.getString(
													R.string.home_music_tile_playlist_decription_songs_amount));

				// sets the description.
				viewHolder.textDescription_playlist.setText(mediaItem
						.getMusicTrackCount()
						+ " "
						+ home_music_tile_playlist_decription_songs_amount);

				CacheState cacheState = DBOHandler.getPlaylistCacheState(
						mActivity, "" + mediaItem.getId());

				if (cacheState != CacheState.NOT_CACHED) {
					viewHolder.progressCacheState_playlist
							.setCacheCountVisibility(true);
					viewHolder.progressCacheState_playlist.setCacheCount(""
							+ DBOHandler.getPlaylistCachedCount(mActivity, ""
									+ mediaItem.getId()));
					viewHolder.progressCacheState_playlist
							.setCacheState(cacheState);
					viewHolder.progressCacheState_playlist
							.setVisibility(View.VISIBLE);
				}
			}
		}
		try {
			viewHolder.view_playlist_tile.setTag(R.id.view_tag_object,
					mediaItem);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		try {
			viewHolder.view_playlist_tile.setTag(R.id.view_tag_position,
					position);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

		Logger.e(TAG, position + "firstPositionPost" + firstPositionPost);
		if (type == PlacementType.MUSIC_NEW && firstPositionPost
				&& position == 3) {
			firstPositionPost = false;
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					Checkforpost(3, 3);
				}
			}, 1000);
		}
		Logger.s("MediaTiledapter getView return"
				+ viewHolder.view_playlist_tile);

	}

	private void getMusicViewPlayList(MediaItem mediaItem, int position,
			final ViewHolder_Playlist_English viewHolder) {

		// if (position == 0 && mActivity instanceof HomeActivity
		// && ((HomeActivity) mActivity).isPromoUnit()) {
		// LinearLayout.LayoutParams lp = (LayoutParams) ((RelativeLayout)
		// viewHolder.imageTile
		// .getParent()).getLayoutParams();
		// lp.topMargin = 0;
		// } else {
		// LinearLayout.LayoutParams lp = (LayoutParams) ((RelativeLayout)
		// viewHolder.imageTile
		// .getParent()).getLayoutParams();
		// lp.topMargin = (int)
		// mActivity.getResources().getDimension(R.dimen.home_music_tile_margin);
		// }

		if (isAd(mediaItem)) {
			loadAd(viewHolder, position);
			// return;
		} else {
			viewHolder.imageTile.setTag(null);
			viewHolder.imageTile.setTag(R.string.key_placement, null);
			viewHolder.iv_selector.setTag(null);
			viewHolder.iv_selector.setTag(R.string.key_placement, null);

			viewHolder.ll_images.setVisibility(View.VISIBLE);
			viewHolder.buttonPlay_playlist.setVisibility(View.VISIBLE);
			viewHolder.iv_home_tile_options_playlist
					.setVisibility(View.VISIBLE);
			viewHolder.textTitle_playlist.setVisibility(View.VISIBLE);
			viewHolder.textDescription_playlist.setVisibility(View.VISIBLE);
			viewHolder.imageTile.setVisibility(View.GONE);

			if (playlistAdHeight != 0) {
				RelativeLayout rl = (RelativeLayout) viewHolder.ll_images
						.getParent();

				rl.getLayoutParams().height = (int) mActivity.getResources()
						.getDimension(R.dimen.home_music_playlist_height);
			}

			try {
				viewHolder.view_playlist_tile.setOnClickListener(this);
			} catch (Exception e) {
			}

			((LinearLayout) getRoot(viewHolder.view_playlist_tile)).setTag(
					R.string.key_is_ad, (Boolean) false);

			viewHolder.iv_home_tile_options_playlist
					.setOnClickListener(listener_home_3dots);

			if (mediaItem.getMediaType() == MediaType.PLAYLIST) {

				viewHolder.buttonPlay_playlist.setOnClickListener(this);
				viewHolder.iv_selector.setOnClickListener(this);

				downloadingImages(mediaItem, position, viewHolder);

				// sets the title.
				viewHolder.textTitle_playlist.setText(mediaItem.getTitle());

				if (home_music_tile_playlist_decription_songs_amount == null)
					home_music_tile_playlist_decription_songs_amount = Utils
							.getMultilanguageTextLayOut(
									mActivity,
									mActivity
											.getResources()
											.getString(
													R.string.home_music_tile_playlist_decription_songs_amount));

				// sets the description.
				viewHolder.textDescription_playlist.setText(mediaItem
						.getMusicTrackCount()
						+ " "
						+ home_music_tile_playlist_decription_songs_amount);

				CacheState cacheState = DBOHandler.getPlaylistCacheState(
						mActivity, "" + mediaItem.getId());

				if (cacheState != CacheState.NOT_CACHED) {
					viewHolder.progressCacheState_playlist
							.setCacheCountVisibility(true);
					viewHolder.progressCacheState_playlist.setCacheCount(""
							+ DBOHandler.getPlaylistCachedCount(mActivity, ""
									+ mediaItem.getId()));
					viewHolder.progressCacheState_playlist
							.setCacheState(cacheState);
					viewHolder.progressCacheState_playlist
							.setVisibility(View.VISIBLE);
				}
			}
		}

		try {
			viewHolder.view_playlist_tile.setTag(R.id.view_tag_object,
					mediaItem);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		try {
			viewHolder.view_playlist_tile.setTag(R.id.view_tag_position,
					position);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

		Logger.e(TAG, position + "firstPositionPost" + firstPositionPost);
		if (type == PlacementType.MUSIC_NEW && firstPositionPost
				&& position == 3) {
			firstPositionPost = false;
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					Checkforpost(3, 3);
				}
			}, 1000);
		}
		Logger.s("MediaTiledapter getView return"
				+ viewHolder.view_playlist_tile);

	}

	private void downloadingImages(MediaItem mediaItem, int position,
			final ViewHolder_Playlist viewHolder) {

		String[] images = ImagesManager.getPlaylistTileImagesUrlArray(
				mediaItem.getImagesUrlArray(), ImagesManager.HOME_MUSIC_TILE,
				DataManager.getDisplayDensityLabel());

		if(mActivity != null && !TextUtils.isEmpty(mediaItem.getPlaylistArtwork())){
			viewHolder.ll_images
					.setBackgroundResource(R.drawable.background_playlist_main_thumb);
			picasso.load(mediaItem.getPlaylistArtwork(), new PicassoTarget() {
				@Override
				public void onPrepareLoad(Drawable arg0) {
				}

				@Override
				public void onBitmapLoaded(Bitmap arg0, LoadedFrom arg1) {
					if (Build.VERSION.SDK_INT >= 16) {

						viewHolder.ll_images.setBackground(new BitmapDrawable(
								mActivity.getResources(), arg0));

					} else {
						viewHolder.ll_images
								.setBackgroundDrawable(new BitmapDrawable(
										mActivity.getResources(), arg0));
					}

				}

				@Override
				public void onBitmapFailed(Drawable arg0) {
				}
			}, tag);

//			picasso.cancelRequest(viewHolder.home_music_tile_image1);
//			picasso.cancelRequest(viewHolder.home_music_tile_image2);
//			picasso.cancelRequest(viewHolder.home_music_tile_image3);
//			picasso.cancelRequest(viewHolder.home_music_tile_image4);
//			picasso.cancelRequest(viewHolder.home_music_tile_image5);
//			picasso.cancelRequest(viewHolder.home_music_tile_image6);
//			viewHolder.home_music_tile_image1.setImageBitmap(null);
//			viewHolder.home_music_tile_image2.setImageBitmap(null);
//			viewHolder.home_music_tile_image3.setImageBitmap(null);
//			viewHolder.home_music_tile_image4.setImageBitmap(null);
//			viewHolder.home_music_tile_image5.setImageBitmap(null);
//			viewHolder.home_music_tile_image6.setImageBitmap(null);
		}/* else {// if (mediaItem.getMusicTrackCount() == 1) {
			viewHolder.ll_images
					.setBackgroundResource(R.drawable.background_playlist_main_thumb);*/
//			if (images != null && images.length > 0) {
//				if (mActivity != null && !TextUtils.isEmpty(images[0])) {
//
//					picasso.load(images[0], new PicassoTarget() {
//						@Override
//						public void onPrepareLoad(Drawable arg0) {
//						}
//
//						@Override
//						public void onBitmapLoaded(Bitmap arg0, LoadedFrom arg1) {
//							if (Build.VERSION.SDK_INT >= 16) {
//
//								viewHolder.ll_images.setBackground(new BitmapDrawable(
//										mActivity.getResources(), arg0));
//
//							} else {
//								viewHolder.ll_images
//										.setBackgroundDrawable(new BitmapDrawable(
//												mActivity.getResources(), arg0));
//							}
//						}
//
//						@Override
//						public void onBitmapFailed(Drawable arg0) {
//						}
//					}, tag);
//
//				}
//			}
//		}
	else {
			viewHolder.ll_images
					.setBackgroundResource(R.drawable.background_playlist_main_thumb);
			if (images != null && images.length > 0)
				downloadImage(images[0], viewHolder.home_music_tile_image1);
			else
				viewHolder.home_music_tile_image1.setImageBitmap(null);

			if (images != null && images.length > 1)
				downloadImage(images[1], viewHolder.home_music_tile_image2);
			else
				viewHolder.home_music_tile_image2.setImageBitmap(null);

			if (images != null && images.length > 2)
				downloadImage(images[2], viewHolder.home_music_tile_image3);
			else
				viewHolder.home_music_tile_image3.setImageBitmap(null);

			if (images != null && images.length > 3)
				downloadImage(images[3], viewHolder.home_music_tile_image4);
			else
				viewHolder.home_music_tile_image4.setImageBitmap(null);

			if (images != null && images.length > 4)
				downloadImage(images[4], viewHolder.home_music_tile_image5);
			else
				viewHolder.home_music_tile_image5.setImageBitmap(null);

			if (images != null && images.length > 5)
				downloadImage(images[5], viewHolder.home_music_tile_image6);
			else
				viewHolder.home_music_tile_image6.setImageBitmap(null);
		}
		// String url =
		// "http://content.hungama.com/audio%20album/display%20image/400x400%20jpeg/420997770.jpg";
		// downloadImage(url, viewHolder.home_music_tile_image1);
		// url =
		// "http://content.hungama.com/audio%20album/display%20image/400x400%20jpeg/96759251.jpg";
		// downloadImage(url, viewHolder.home_music_tile_image2);
		// url =
		// "http://content.hungama.com/audio%20album/display%20image/400x400%20jpeg/85568975.jpg";
		// downloadImage(url, viewHolder.home_music_tile_image3);
		// url =
		// "http://content.hungama.com/audio%20album/display%20image/400x400%20jpeg/416940957.jpg";
		// downloadImage(url, viewHolder.home_music_tile_image4);
		// url =
		// "http://content.hungama.com/audio%20album/display%20image/400x400%20jpeg/89252609.jpg";
		// downloadImage(url, viewHolder.home_music_tile_image5);
		// url =
		// "http://content.hungama.com/audio%20album/display%20image/400x400%20jpeg/416940957.jpg";
		// downloadImage(url, viewHolder.home_music_tile_image6);
	}

	private void downloadImage(String url1, ImageView iv) {
		try {
			if (mActivity != null && !TextUtils.isEmpty(url1)) {
				picasso.loadWithFit(null, url1, iv, -1, tag);

			}
		} catch (Exception e) {
			Logger.e(getClass() + ":701", e.toString());
		} catch (Error e) {
			Logger.e(getClass() + ":701", e.toString());
		}
	}

	private void downloadingImages(MediaItem mediaItem, int position,
			final ViewHolder_Playlist_English viewHolder) {
		String[] images = ImagesManager.getPlaylistTileImagesUrlArray(
				mediaItem.getImagesUrlArray(), ImagesManager.HOME_MUSIC_TILE,
				DataManager.getDisplayDensityLabel());

		Logger.s("playlist_artwork ::::: " + TextUtils.isEmpty(mediaItem.getPlaylistArtwork()));
		if(mActivity != null && !TextUtils.isEmpty(mediaItem.getPlaylistArtwork())){
			viewHolder.ll_images
					.setBackgroundResource(R.drawable.background_playlist_main_thumb);
			picasso.load(mediaItem.getPlaylistArtwork(), new PicassoTarget() {
				@Override
				public void onPrepareLoad(Drawable arg0) {
				}

				@Override
				public void onBitmapLoaded(Bitmap arg0, LoadedFrom arg1) {
					if (Build.VERSION.SDK_INT >= 16) {

						viewHolder.ll_images.setBackground(new BitmapDrawable(
								mActivity.getResources(), arg0));

					} else {
						viewHolder.ll_images
								.setBackgroundDrawable(new BitmapDrawable(
										mActivity.getResources(), arg0));
					}
				}

				@Override
				public void onBitmapFailed(Drawable arg0) {
				}
			}, tag);
			picasso.cancelRequest(viewHolder.home_music_tile_image1);
			picasso.cancelRequest(viewHolder.home_music_tile_image2);
			picasso.cancelRequest(viewHolder.home_music_tile_image3);
			picasso.cancelRequest(viewHolder.home_music_tile_image4);
			picasso.cancelRequest(viewHolder.home_music_tile_image5);
			picasso.cancelRequest(viewHolder.home_music_tile_image6);
			viewHolder.home_music_tile_image1.setImageBitmap(null);
			viewHolder.home_music_tile_image2.setImageBitmap(null);
			viewHolder.home_music_tile_image3.setImageBitmap(null);
			viewHolder.home_music_tile_image4.setImageBitmap(null);
			viewHolder.home_music_tile_image5.setImageBitmap(null);
			viewHolder.home_music_tile_image6.setImageBitmap(null);
		}/* else {//if (mediaItem.getMusicTrackCount() == 1) {
			viewHolder.ll_images
					.setBackgroundResource(R.drawable.background_playlist_main_thumb);*/
//			if (images != null && images.length > 0) {
//				if (mActivity != null && !TextUtils.isEmpty(images[0])) {
//					picasso.load(images[0], new PicassoTarget() {
//						@Override
//						public void onPrepareLoad(Drawable arg0) {
//						}
//
//						@Override
//						public void onBitmapLoaded(Bitmap arg0, LoadedFrom arg1) {
//							if (Build.VERSION.SDK_INT >= 16) {
//
//								viewHolder.ll_images.setBackground(new BitmapDrawable(
//										mActivity.getResources(), arg0));
//
//							} else {
//								viewHolder.ll_images
//										.setBackgroundDrawable(new BitmapDrawable(
//												mActivity.getResources(), arg0));
//							}
//						}
//
//						@Override
//						public void onBitmapFailed(Drawable arg0) {
//						}
//					}, tag);
//				}
//			}
//			picasso.cancelRequest(viewHolder.home_music_tile_image1);
//			picasso.cancelRequest(viewHolder.home_music_tile_image2);
//			picasso.cancelRequest(viewHolder.home_music_tile_image3);
//			picasso.cancelRequest(viewHolder.home_music_tile_image4);
//			picasso.cancelRequest(viewHolder.home_music_tile_image5);
//			picasso.cancelRequest(viewHolder.home_music_tile_image6);
//			viewHolder.home_music_tile_image1.setImageBitmap(null);
//			viewHolder.home_music_tile_image2.setImageBitmap(null);
//			viewHolder.home_music_tile_image3.setImageBitmap(null);
//			viewHolder.home_music_tile_image4.setImageBitmap(null);
//			viewHolder.home_music_tile_image5.setImageBitmap(null);
//			viewHolder.home_music_tile_image6.setImageBitmap(null);
//		}
	else {

			viewHolder.ll_images
					.setBackgroundResource(R.drawable.background_playlist_main_thumb);

			if (images != null && images.length > 0)
				downloadImage(images[0], viewHolder.home_music_tile_image1);
			else
				viewHolder.home_music_tile_image1.setImageBitmap(null);

			if (images != null && images.length > 1)
				downloadImage(images[1], viewHolder.home_music_tile_image2);
			else
				viewHolder.home_music_tile_image2.setImageBitmap(null);

			if (images != null && images.length > 2)
				downloadImage(images[2], viewHolder.home_music_tile_image3);
			else
				viewHolder.home_music_tile_image3.setImageBitmap(null);

			if (images != null && images.length > 3)
				downloadImage(images[3], viewHolder.home_music_tile_image4);
			else
				viewHolder.home_music_tile_image4.setImageBitmap(null);

			if (images != null && images.length > 4)
				downloadImage(images[4], viewHolder.home_music_tile_image5);
			else
				viewHolder.home_music_tile_image5.setImageBitmap(null);

			if (images != null && images.length > 5)
				downloadImage(images[5], viewHolder.home_music_tile_image6);
			else
				viewHolder.home_music_tile_image6.setImageBitmap(null);
		}

		// String url =
		// "http://content.hungama.com/audio%20album/display%20image/400x400%20jpeg/420997770.jpg";
		// downloadImage(url, viewHolder.home_music_tile_image1);
		// url =
		// "http://content.hungama.com/audio%20album/display%20image/400x400%20jpeg/96759251.jpg";
		// downloadImage(url, viewHolder.home_music_tile_image2);
		// url =
		// "http://content.hungama.com/audio%20album/display%20image/400x400%20jpeg/85568975.jpg";
		// downloadImage(url, viewHolder.home_music_tile_image3);
		// url =
		// "http://content.hungama.com/audio%20album/display%20image/400x400%20jpeg/416940957.jpg";
		// downloadImage(url, viewHolder.home_music_tile_image4);
		// url =
		// "http://content.hungama.com/audio%20album/display%20image/400x400%20jpeg/89252609.jpg";
		// downloadImage(url, viewHolder.home_music_tile_image5);
		// url =
		// "http://content.hungama.com/audio%20album/display%20image/400x400%20jpeg/416940957.jpg";
		// downloadImage(url, viewHolder.home_music_tile_image6);
	}

	public void loadTrack(subLayout layout, MediaItem mediaItem, int position) {
		try {
//			layout.rl_bg.setBackgroundColor(R.color.myPrimaryColor_transparent);

			layout.textTitle.setVisibility(View.VISIBLE);
			layout.textTitle.setText(String.valueOf(position));
			if(isDetailForTrack)
				layout.textDescription.setVisibility(View.VISIBLE);
			else
				layout.textDescription.setVisibility(View.GONE);

			layout.imageTile
					.setImageResource(R.drawable.background_music_tile_light);
//			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//					(int) LayoutParams.MATCH_PARENT,
//					(int) LayoutParams.WRAP_CONTENT);
//
//			params.setMargins(0, 0, 0, 0);
//			layout.textTitle.setLayoutParams(params);
			layout.textTitle.setMaxLines(1);
			layout.textTitle.setText(mediaItem.getTitle());

			layout.textDescription.setText(mediaItem.getAlbumName());

			CacheState cacheState = DBOHandler.getTrackCacheState(mActivity, ""
					+ mediaItem.getId());

			try {
				String imageUrl = mediaItem.getImageUrl();
				// String[] images = ImagesManager.getImagesUrlArray(
				// mediaItem.getImagesUrlArray(),
				// ImagesManager.HOME_MUSIC_TILE,
				// DataManager.getDisplayDensityLabel());
				// if (images != null && images.length > 0)
				// imageUrl = images[0];

				imageUrl = ImagesManager.getMusicArtSmallImageUrl(mediaItem
						.getImagesUrlArray());

				if (mActivity != null && !TextUtils.isEmpty(imageUrl)) {

					picasso.load(new PicassoCallBack() {

						@Override
						public void onSuccess() {
						}

						@Override
						public void onError() {
						}
					}, imageUrl, layout.imageTile,
							R.drawable.background_music_tile_light, tag);
				} else {
					layout.imageTile
							.setImageResource(R.drawable.background_music_tile_light);
				}
			} catch (Error e) {
				Logger.e(getClass() + ":701", e.toString());
			}

			// if(cacheState==CacheState.CACHED){
			// imageCacheState.setBackgroundResource(R.drawable.icon_media_details_saved);
			// imageCacheState.setVisibility(View.VISIBLE);
			// } else if(cacheState==CacheState.CACHING ||
			// cacheState==CacheState.QUEUED){
			// imageCacheState.setBackgroundResource(R.drawable.icon_media_details_saving);
			// imageCacheState.setVisibility(View.VISIBLE);
			// }
			/*
			 * if(cacheState==CacheState.QUEUED || cacheState ==
			 * CacheState.CACHED){ progressCacheState.setCacheState(cacheState);
			 * progressCacheState.setVisibility(View.VISIBLE); } else
			 */if (cacheState != CacheState.NOT_CACHED
			/* && CacheManager.isProUser(mContext) */) {
				layout.progressCacheState.setCacheState(cacheState);
				layout.progressCacheState.setProgress(DBOHandler
						.getTrackCacheProgress(mActivity,
								"" + mediaItem.getId()));
				layout.progressCacheState.setVisibility(View.VISIBLE);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void loadTrack(ViewHolderSong_English.subLayout layout,
			MediaItem mediaItem, int position) {
		try {
//			layout.rl_bg.setBackgroundColor(R.color.myPrimaryColor_transparent);

			layout.textTitle.setVisibility(View.VISIBLE);
			layout.textTitle.setText(String.valueOf(position));
			if(isDetailForTrack)
				layout.textDescription.setVisibility(View.VISIBLE);
			else
				layout.textDescription.setVisibility(View.GONE);

			// layout.imageTile
			// .setImageResource(R.drawable.background_music_tile_light);
//			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//					(int) LayoutParams.MATCH_PARENT,
//					(int) LayoutParams.WRAP_CONTENT);
//
//			params.setMargins(0, 0, 0, 0);
//			layout.textTitle.setLayoutParams(params);
			layout.textTitle.setMaxLines(1);
			layout.textTitle.setText(mediaItem.getTitle());
			layout.textDescription.setText(mediaItem.getAlbumName());

			CacheState cacheState = DBOHandler.getTrackCacheState(mActivity, ""
					+ mediaItem.getId());

			try {
				String imageUrl = mediaItem.getImageUrl();
				// String[] images = ImagesManager.getImagesUrlArray(
				// mediaItem.getImagesUrlArray(),
				// ImagesManager.HOME_MUSIC_TILE,
				// DataManager.getDisplayDensityLabel());
				// if (images != null && images.length > 0)
				// imageUrl = images[0];
				imageUrl = ImagesManager.getMusicArtSmallImageUrl(mediaItem
						.getImagesUrlArray());
				if (mActivity != null && !TextUtils.isEmpty(imageUrl)) {

					picasso.load(new PicassoCallBack() {

						@Override
						public void onSuccess() {
						}

						@Override
						public void onError() {
						}
					}, imageUrl, layout.imageTile,
							R.drawable.background_music_tile_light, tag);
				} else {
					layout.imageTile
							.setImageResource(R.drawable.background_music_tile_light);
				}
			} catch (Error e) {
				Logger.e(getClass() + ":701", e.toString());
			}

			// if(cacheState==CacheState.CACHED){
			// imageCacheState.setBackgroundResource(R.drawable.icon_media_details_saved);
			// imageCacheState.setVisibility(View.VISIBLE);
			// } else if(cacheState==CacheState.CACHING ||
			// cacheState==CacheState.QUEUED){
			// imageCacheState.setBackgroundResource(R.drawable.icon_media_details_saving);
			// imageCacheState.setVisibility(View.VISIBLE);
			// }
			/*
			 * if(cacheState==CacheState.QUEUED || cacheState ==
			 * CacheState.CACHED){ progressCacheState.setCacheState(cacheState);
			 * progressCacheState.setVisibility(View.VISIBLE); } else
			 */if (cacheState != CacheState.NOT_CACHED
			/* && CacheManager.isProUser(mContext) */) {
				layout.progressCacheState.setCacheState(cacheState);
				layout.progressCacheState.setProgress(DBOHandler
						.getTrackCacheProgress(mActivity,
								"" + mediaItem.getId()));
				layout.progressCacheState.setVisibility(View.VISIBLE);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void loadAlbum(final subLayout layout, MediaItem mediaItem,
			int position) {

		layout.buttonPlay.setOnClickListener(this);
		if (mediaItem.getMediaType() == MediaType.ARTIST)
			layout.iv_home_tile_options.setVisibility(View.GONE);
		else
			layout.iv_home_tile_options.setVisibility(View.VISIBLE);
		// hides the texts.
		// layout.textTitleSongOrPlaylist.setVisibility(View.VISIBLE);
		layout.textTitle.setVisibility(View.VISIBLE);
		layout.textDescription.setVisibility(View.GONE);

//		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//				(int) LayoutParams.MATCH_PARENT,
//				(int) LayoutParams.WRAP_CONTENT);
//
//		params.setMargins(0, 15, 0, 0);
//		layout.textTitle.setLayoutParams(params);
		layout.textTitle.setMaxLines(1);

		// sets the title.
		// layout.textTitle.setText(mediaItem.getTitle());
		if (mediaItem.getMediaType() == MediaType.ARTIST)
			layout.textTitle.setText(mediaItem.getTitle());
		else
			layout.textTitle.setText(mediaItem.getAlbumName());

		// loads the image for it.
		try {
			String imageUrl = mediaItem.getImageUrl();
			// String[] images = ImagesManager.getImagesUrlArray(
			// mediaItem.getImagesUrlArray(),
			// ImagesManager.HOME_MUSIC_TILE,
			// DataManager.getDisplayDensityLabel());
			// if (images != null && images.length > 0)
			// imageUrl = images[0];
			imageUrl = ImagesManager.getMusicArtSmallImageUrl(mediaItem
					.getImagesUrlArray());
			if (mActivity != null && !TextUtils.isEmpty(imageUrl)) {

				picasso.load(new PicassoCallBack() {

					@Override
					public void onSuccess() {
						layout.textTitle.setVisibility(View.VISIBLE);
					}

					@Override
					public void onError() {
					}
				}, imageUrl, layout.imageTile,
						R.drawable.background_home_tile_album_default, tag);
			} else {
				layout.imageTile
						.setImageResource(R.drawable.background_music_tile_light);
			}
		} catch (Error e) {
			Logger.e(getClass() + ":701", e.toString());
		}

		CacheState cacheState = DBOHandler.getAlbumCacheState(mActivity, ""
				+ mediaItem.getId());
		if (cacheState != CacheState.NOT_CACHED) {
			layout.progressCacheState.setCacheCountVisibility(true);
//			layout.progressCacheState.setCacheCount(""
//					+ DBOHandler.getAlbumCachedCount(mActivity,
//							"" + mediaItem.getId()));
			layout.progressCacheState.setCacheState(cacheState);
			layout.progressCacheState.setVisibility(View.VISIBLE);
		} else if (DBOHandler.getAlbumCachedCount(mActivity,
				"" + mediaItem.getId()) > 0) {
			layout.progressCacheState.setCacheCountVisibility(true);
//			layout.progressCacheState.setCacheCount(""
//					+ DBOHandler.getAlbumCachedCount(mActivity,
//							"" + mediaItem.getId()));
			layout.progressCacheState.setCacheState(CacheState.CACHED);
			layout.progressCacheState.setVisibility(View.VISIBLE);
		}

	}

	public void loadAlbum(final ViewHolderSong_English.subLayout layout,
			MediaItem mediaItem, int position) {

		layout.buttonPlay.setOnClickListener(this);
		if (mediaItem.getMediaType() == MediaType.ARTIST)
			layout.iv_home_tile_options.setVisibility(View.GONE);
		else
			layout.iv_home_tile_options.setVisibility(View.VISIBLE);
		// hides the texts.
		// layout.textTitleSongOrPlaylist.setVisibility(View.VISIBLE);
		layout.textTitle.setVisibility(View.VISIBLE);
		layout.textDescription.setVisibility(View.GONE);

//		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//				(int) LayoutParams.MATCH_PARENT,
//				(int) LayoutParams.WRAP_CONTENT);
//
//		params.setMargins(0, 15, 0, 0);
//		layout.textTitle.setLayoutParams(params);
		layout.textTitle.setMaxLines(1);

		// sets the title.
		// layout.textTitle.setText(mediaItem.getTitle());
		if (mediaItem.getMediaType() == MediaType.ARTIST)
			layout.textTitle.setText(mediaItem.getTitle());
		else
			layout.textTitle.setText(mediaItem.getAlbumName());

		// loads the image for it.
		try {
			String imageUrl = mediaItem.getImageUrl();
			// String[] images = ImagesManager.getImagesUrlArray(
			// mediaItem.getImagesUrlArray(),
			// ImagesManager.HOME_MUSIC_TILE,
			// DataManager.getDisplayDensityLabel());
			// if (images != null && images.length > 0)
			// imageUrl = images[0];
			imageUrl = ImagesManager.getMusicArtSmallImageUrl(mediaItem
					.getImagesUrlArray());
			if (mActivity != null && !TextUtils.isEmpty(imageUrl)) {

				picasso.load(new PicassoCallBack() {

					@Override
					public void onSuccess() {
						layout.textTitle.setVisibility(View.VISIBLE);
					}

					@Override
					public void onError() {
					}
				}, imageUrl, layout.imageTile,
						R.drawable.background_home_tile_album_default, tag);
			} else {
				layout.imageTile
						.setImageResource(R.drawable.background_music_tile_light);
			}
		} catch (Error e) {
			Logger.e(getClass() + ":701", e.toString());
		}

		CacheState cacheState = DBOHandler.getAlbumCacheState(mActivity, ""
				+ mediaItem.getId());
		if (cacheState != CacheState.NOT_CACHED) {
			layout.progressCacheState.setCacheCountVisibility(true);
//			layout.progressCacheState.setCacheCount(""
//					+ DBOHandler.getAlbumCachedCount(mActivity,
//							"" + mediaItem.getId()));
			layout.progressCacheState.setCacheState(cacheState);
			layout.progressCacheState.setVisibility(View.VISIBLE);
		} else if (DBOHandler.getAlbumCachedCount(mActivity,
				"" + mediaItem.getId()) > 0) {
			layout.progressCacheState.setCacheCountVisibility(true);
//			layout.progressCacheState.setCacheCount(""
//					+ DBOHandler.getAlbumCachedCount(mActivity,
//							"" + mediaItem.getId()));
			layout.progressCacheState.setCacheState(CacheState.CACHED);
			layout.progressCacheState.setVisibility(View.VISIBLE);
		}

	}

	public void loadAd(subLayout layout, int position) {
		Placement placement = null;
		int adPosition = position;
		if (promoHeight != 0)
			adPosition = position - 1;
		placement = mPlacementMap.get(adPosition);
		if (placement == null) {
			placement = getPlacement();
		}
		layout.iv_home_tile_options.setVisibility(View.GONE);
		if (placement != null) {
			((LinearLayout) layout.parent.getParent().getParent()).setTag(
					R.string.key_is_ad, (Boolean) true);
			layout.imageTile.setTag(R.string.key_placement, null);
			layout.imageTile.setTag("ad");
			layout.imageTile.setImageDrawable(null);
			layout.imageTile.setOnClickListener(null);
			layout.imageTile.setOnLongClickListener(null);

			layout.iv_selector.setTag(R.string.key_placement, null);
			layout.iv_selector.setTag("ad");
			layout.iv_selector.setImageDrawable(null);
			layout.iv_selector.setOnClickListener(null);
			layout.iv_selector.setOnLongClickListener(null);


			layout.textTitle.setVisibility(View.VISIBLE);
			layout.textTitle.setText(txtAdvertise);

			layout.textDescription.setVisibility(View.GONE);
			layout.buttonPlay.setVisibility(View.GONE);
			layout.rl_bg.setVisibility(View.VISIBLE);
			// layout.textTitleSongOrPlaylist.setVisibility(View.GONE);
			// rl.findViewById(R.id.radio_translucent_strip_layout).setVisibility(
			// View.GONE);
			getAdView(placement, adPosition, layout.imageTile, layout.iv_selector);
		}
	}

	private int playlistAdHeight = 0;

	public void loadAd(ViewHolder_Playlist layout, int position) {
		Placement placement = null;
		int adPosition = position;
		if (promoHeight != 0)
			adPosition = position - 1;
		placement = mPlacementMap.get(adPosition);
		if (placement == null) {
			placement = getPlacement();
		}
		layout.ll_images.setVisibility(View.GONE);
		layout.buttonPlay_playlist.setVisibility(View.GONE);
		layout.iv_home_tile_options_playlist.setVisibility(View.GONE);
		layout.textTitle_playlist.setVisibility(View.GONE);
		layout.textDescription_playlist.setVisibility(View.GONE);
		layout.imageTile.setVisibility(View.VISIBLE);
		RelativeLayout rl = (RelativeLayout) layout.ll_images.getParent();
		// System.out.println(rl.getHeight() + " ::::::::::s:: " +
		// rl.getWidth());
		// System.out.println(HomeActivity.metrics.widthPixels +
		// " ::::::::::s:: " +
		// mActivity.getResources().getDimension(R.dimen.home_music_tile_margin));
		if (playlistAdHeight == 0)
			playlistAdHeight = (int) ((HomeActivity.metrics.widthPixels - (2*mActivity
					.getResources()
					.getDimension(R.dimen.home_music_tile_margin))) / 3.57f) + (int) mActivity
					.getResources()
					.getDimension(R.dimen.home_playlist_bottompadding);
		if (playlistAdHeight != 0)
			rl.getLayoutParams().height = playlistAdHeight;

		if (placement != null) {
			((LinearLayout) layout.imageTile.getParent().getParent()).setTag(
					R.string.key_is_ad, (Boolean) true);
			layout.imageTile.setTag(R.string.key_placement, null);
			layout.imageTile.setTag("ad");
			layout.imageTile.setImageDrawable(null);
			layout.imageTile.setOnClickListener(null);
			layout.imageTile.setOnLongClickListener(null);

			layout.iv_selector.setTag(R.string.key_placement, null);
			layout.iv_selector.setTag("ad");
			layout.iv_selector.setImageDrawable(null);
			layout.iv_selector.setOnClickListener(null);
			layout.iv_selector.setOnLongClickListener(null);

			if(isNeedAdvertiseText) {
				layout.textTitle_playlist.setVisibility(View.VISIBLE);
				layout.textTitle_playlist.setText(txtAdvertise);
			}
			// layout.textTitle.setVisibility(View.GONE);
			// layout.textDescription.setVisibility(View.GONE);
			// layout.buttonPlay.setVisibility(View.GONE);
			// layout.rl_bg.setVisibility(View.GONE);
			// layout.textTitleSongOrPlaylist.setVisibility(View.GONE);
			// rl.findViewById(R.id.radio_translucent_strip_layout).setVisibility(
			// View.GONE);
			getAdView(placement, adPosition, layout.imageTile, layout.iv_selector);
		}
	}

	public void loadAd(ViewHolder_Playlist_English layout, int position) {
		Placement placement = null;
		int adPosition = position;
		if (promoHeight != 0)
			adPosition = position - 1;
		placement = mPlacementMap.get(adPosition);
		if (placement == null) {
			placement = getPlacement();
		}
		layout.ll_images.setVisibility(View.GONE);
		layout.buttonPlay_playlist.setVisibility(View.GONE);
		layout.iv_home_tile_options_playlist.setVisibility(View.GONE);
		layout.textTitle_playlist.setVisibility(View.GONE);
		layout.textDescription_playlist.setVisibility(View.GONE);
		layout.imageTile.setVisibility(View.VISIBLE);
		RelativeLayout rl = (RelativeLayout) layout.ll_images.getParent();
		// System.out.println(rl.getHeight() + " ::::::::::s:: " +
		// rl.getWidth());
		// System.out.println(HomeActivity.metrics.widthPixels +
		// " ::::::::::s:: " +
		// mActivity.getResources().getDimension(R.dimen.home_music_tile_margin));
		if (playlistAdHeight == 0)
			playlistAdHeight = (int) ((HomeActivity.metrics.widthPixels - (2*mActivity
								.getResources()
								.getDimension(R.dimen.home_music_tile_margin))) / 3.57f) + (int) mActivity
								.getResources()
								.getDimension(R.dimen.home_playlist_bottompadding);
		if (playlistAdHeight != 0)
			rl.getLayoutParams().height = playlistAdHeight;

		if (placement != null) {
			((LinearLayout) layout.imageTile.getParent().getParent()).setTag(
					R.string.key_is_ad, (Boolean) true);
			layout.imageTile.setTag(R.string.key_placement, null);
			layout.imageTile.setTag("ad");
			layout.imageTile.setImageDrawable(null);
			layout.imageTile.setOnClickListener(null);
			layout.imageTile.setOnLongClickListener(null);

			layout.iv_selector.setTag(R.string.key_placement, null);
			layout.iv_selector.setTag("ad");
			layout.iv_selector.setImageDrawable(null);
			layout.iv_selector.setOnClickListener(null);
			layout.iv_selector.setOnLongClickListener(null);

			if(isNeedAdvertiseText) {
				layout.textTitle_playlist.setVisibility(View.VISIBLE);
				layout.textTitle_playlist.setText(txtAdvertise);
			}
			// layout.textTitle.setVisibility(View.GONE);
			// layout.textDescription.setVisibility(View.GONE);
			// layout.buttonPlay.setVisibility(View.GONE);
			// layout.rl_bg.setVisibility(View.GONE);
			// layout.textTitleSongOrPlaylist.setVisibility(View.GONE);
			// rl.findViewById(R.id.radio_translucent_strip_layout).setVisibility(
			// View.GONE);
			getAdView(placement, adPosition, layout.imageTile, layout.iv_selector);
		}
	}

	public void loadAd(ViewHolderSong_English.subLayout layout, int position) {
		Placement placement = null;
		int adPosition = position;
		if (promoHeight != 0)
			adPosition = position - 1;
		placement = mPlacementMap.get(adPosition);
		Logger.s("post ad view : " + adPosition + " " + (placement == null));
		if (placement == null) {
			placement = getPlacement();
		}
		layout.iv_home_tile_options.setVisibility(View.GONE);
		if (placement != null) {
			((LinearLayout) layout.parent.getParent().getParent()).setTag(
					R.string.key_is_ad, (Boolean) true);
			layout.imageTile.setTag(R.string.key_placement, null);
			layout.imageTile.setTag("ad");
			layout.imageTile.setImageDrawable(null);
			layout.imageTile.setOnClickListener(null);
			layout.imageTile.setOnLongClickListener(null);

			layout.iv_selector.setTag(R.string.key_placement, null);
			layout.iv_selector.setTag("ad");
			layout.iv_selector.setImageDrawable(null);
			layout.iv_selector.setOnClickListener(null);
			layout.iv_selector.setOnLongClickListener(null);

			layout.textTitle.setVisibility(View.GONE);
			layout.textDescription.setVisibility(View.GONE);
			layout.buttonPlay.setVisibility(View.GONE);
			layout.rl_bg.setVisibility(View.VISIBLE);


				layout.textTitle.setVisibility(View.VISIBLE);
				layout.textTitle.setText(txtAdvertise);

			// layout.textTitleSongOrPlaylist.setVisibility(View.GONE);
			// rl.findViewById(R.id.radio_translucent_strip_layout).setVisibility(
			// View.GONE);
			getAdView(placement, adPosition, layout.imageTile, layout.iv_selector);
		}
	}

	private void getMusicView(subLayout layout, MediaItem mediaItem,
			int position, final ViewHolderSong viewHolder) {

		// if (position == 0 && mActivity instanceof HomeActivity
		// && ((HomeActivity) mActivity).isPromoUnit()) {
		// LinearLayout.LayoutParams lp = (LayoutParams) ((LinearLayout)
		// layout.imageTile
		// .getParent().getParent()).getLayoutParams();
		// lp.topMargin = 0;
		// } else {
		// LinearLayout.LayoutParams lp = (LayoutParams) ((LinearLayout)
		// layout.imageTile
		// .getParent().getParent()).getLayoutParams();
		// lp.topMargin = (int)
		// mActivity.getResources().getDimension(R.dimen.home_music_tile_margin);
		// }

		layout.progressCacheState.setVisibility(View.GONE);
		layout.progressCacheState.showProgressOnly(true);
		layout.iv_home_tile_options.setVisibility(View.VISIBLE);

		if (isAd(mediaItem)) {
			loadAd(layout, position);
			// return;
		} else {
			layout.iv_home_tile_options.setOnClickListener(listener_home_3dots);
			layout.imageTile.setOnClickListener(this);
			layout.iv_selector.setOnClickListener(this);

			((LinearLayout) layout.parent.getParent().getParent()).setTag(
					R.string.key_is_ad, (Boolean) false);
			layout.imageTile.setTag(null);
			layout.imageTile.setTag(R.string.key_placement, null);
			layout.iv_selector.setTag(null);
			layout.iv_selector.setTag(R.string.key_placement, null);
			layout.textTitle.setVisibility(View.VISIBLE);
			if(isDetailForTrack)
				layout.textDescription.setVisibility(View.VISIBLE);
			else
				layout.textDescription.setVisibility(View.GONE);
			// layout.textTitleSongOrPlaylist.setVisibility(View.VISIBLE);
//			layout.rl_bg
//					.setBackgroundResource(R.drawable.bg_full_player_shadow);
			layout.buttonPlay.setOnClickListener(this);
			layout.buttonPlay.setVisibility(View.VISIBLE);
			layout.rl_bg.setVisibility(View.VISIBLE);

			if (mediaItem.getMediaType() == MediaType.ALBUM
					|| mediaItem.getMediaType() == MediaType.ARTIST) {
				loadAlbum(layout, mediaItem, position);
			} else {
				loadTrack(layout, mediaItem, position);
			}
		}

		try {
			layout.parent.setTag(R.id.view_tag_object, mediaItem);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		try {
			layout.parent.setTag(R.id.view_tag_position, position);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

		Logger.e(TAG, position + "firstPositionPost" + firstPositionPost);
		if (type == PlacementType.MUSIC_NEW && firstPositionPost
				&& position == 1) {
			firstPositionPost = false;
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					Checkforpost(1, 1);
				}
			}, 1000);
		}
	}

	private void getMusicView(ViewHolderSong_English.subLayout layout,
			MediaItem mediaItem, int position,
			final ViewHolderSong_English viewHolder) {

		// if (position == 0 && mActivity instanceof HomeActivity
		// && ((HomeActivity) mActivity).isPromoUnit()) {
		// LinearLayout.LayoutParams lp = (LayoutParams) ((LinearLayout)
		// layout.imageTile
		// .getParent().getParent()).getLayoutParams();
		// lp.topMargin = 0;
		// } else {
		// LinearLayout.LayoutParams lp = (LayoutParams) ((LinearLayout)
		// layout.imageTile
		// .getParent().getParent()).getLayoutParams();
		// lp.topMargin = (int)
		// mActivity.getResources().getDimension(R.dimen.home_music_tile_margin);
		// }

		layout.progressCacheState.setVisibility(View.GONE);
		layout.progressCacheState.showProgressOnly(true);

		if (mediaItem.getMediaType() == MediaType.ARTIST)
			layout.iv_home_tile_options.setVisibility(View.GONE);
		else
			layout.iv_home_tile_options.setVisibility(View.VISIBLE);

		if (isAd(mediaItem)) {
			loadAd(layout, position);
			// return;
		} else {
			layout.iv_home_tile_options.setOnClickListener(listener_home_3dots);
			layout.imageTile.setOnClickListener(this);
			layout.iv_selector.setOnClickListener(this);

			((LinearLayout) layout.parent.getParent().getParent()).setTag(
					R.string.key_is_ad, (Boolean) false);
			layout.imageTile.setTag(null);
			layout.imageTile.setTag(R.string.key_placement, null);
			layout.iv_selector.setTag(null);
			layout.iv_selector.setTag(R.string.key_placement, null);
			layout.textTitle.setVisibility(View.VISIBLE);
			if(isDetailForTrack)
				layout.textDescription.setVisibility(View.VISIBLE);
			else
				layout.textDescription.setVisibility(View.GONE);
			// layout.textTitleSongOrPlaylist.setVisibility(View.VISIBLE);
//			layout.rl_bg
//					.setBackgroundResource(R.drawable.bg_full_player_shadow);
			layout.buttonPlay.setOnClickListener(this);
			layout.buttonPlay.setVisibility(View.VISIBLE);
			layout.rl_bg.setVisibility(View.VISIBLE);

			if (mediaItem.getMediaType() == MediaType.ALBUM
					|| mediaItem.getMediaType() == MediaType.ARTIST) {
				loadAlbum(layout, mediaItem, position);
			} else {
				loadTrack(layout, mediaItem, position);
			}
		}

		try {
			layout.parent.setTag(R.id.view_tag_object, mediaItem);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		try {
			layout.parent.setTag(R.id.view_tag_position, position);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

		Logger.e(TAG, position + "firstPositionPost" + firstPositionPost);
		if (type == PlacementType.MUSIC_NEW && firstPositionPost
				&& position == 1) {
			firstPositionPost = false;
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					Checkforpost(1, 1);
				}
			}, 1000);
		}
	}

	public void clearAdPositions(){
		if(Logger.repostAdOnTabChange && viewedPositions!=null)
			viewedPositions.removeAllElements();
	}

	public void postAdForPosition() {
		try {
			// if (!isHomeActivity)
			// return;
			if (/* placement != null && */itemsData != null
					&& itemsData.size() > 0) {
				// System.out.println("CampaignPlayEvent.py request ::: 2 " +
				// placement.getCampaignID() + " :: " +
				// placement.getTrackingID());
				LinearLayoutManager layoutManager = ((LinearLayoutManager) listView
						.getLayoutManager());

				int visibleItems = layoutManager.findLastVisibleItemPosition();
				// if (visibleItems <= 0) {
				// for (int position = gridView.getFirstVisiblePosition();
				// position < visibleItems; position++) {
				// if (mMediaItems.get(position).getImageUrl()
				// .equals(backgroundLink)) {
				// if (!viewedPositions.contains(position)) {
				// Log.e(TAG, "Post ad view>>" + position);
				// Utils.postViewEvent(mContext, placement);
				// viewedPositions.add(position);
				// break;
				// }
				// }
				// }
				// }
				// for(String temp : viewedPositions)
				// System.out.println("CampaignPlayEvent.py request ::: view camp ::::: "
				// + temp);
				for (int position = layoutManager
						.findFirstVisibleItemPosition(); position <= visibleItems; position++) {
					int childPosition = position
							- layoutManager.findFirstVisibleItemPosition();
					// mMediaItems.get(position).getImageUrl());
					// System.out.println("CampaignPlayEvent.py request ::: 41 "
					// +
					// Utils.getDisplayProfile(HomeActivity.metrics,
					// ((Placement) gridView.getChildAt(position).
					// findViewById(R.id.home_music_tile_image).
					// getTag(R.string.key_placement))));
					// if (mMediaItems.get(position).getImageUrl()
					// .equals(backgroundLink)) {
					Checkforpost(position, childPosition);
					// }
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	Handler handler = new Handler();
	Vector<String> viewedPositions;
	private RecyclerView listView;

	private void Checkforpost(int position, int childPosition) {
		try {
			if(position == 0 && childPosition == 0){
				Object obj = itemsData.get(position);
				if (obj != null && obj instanceof PromoUnit) {
					PromoUnit promoUnit = (PromoUnit) obj;
					if (!viewedPositions.contains(position + ":"
							+ promoUnit.getPromo_id())) {
						String bannerLocation = "music_new";
						if (mCategoryType.equals(MediaCategoryType.LATEST)) {
							bannerLocation = "music_new";
						} else if (mCategoryType.equals(MediaCategoryType.POPULAR)) {
							bannerLocation = "music_popular";
						}
						Analytics.postPromoAppEvent(mActivity, promoUnit, "banner_view", bannerLocation);
						viewedPositions.add(position + ":"
								+ promoUnit.getPromo_id());
						return;
					}
				}
			}
			View v = listView.getChildAt(childPosition);
			if (v != null && v.getTag(R.string.key_is_ad) != null) {
				boolean isAd = (Boolean) v.getTag(R.string.key_is_ad);
				if (isAd) {
					View parent;
					if (type == PlacementType.MUSIC_NEW_PLAYLIST
							|| type == PlacementType.MUSIC_POP_PLAYLIST) {
						if (isEnglish) {
							ViewHolder_Playlist_English viewHolder = (ViewHolder_Playlist_English) v
									.getTag();
							parent = (View) viewHolder.ll_images.getParent();
						} else {
							ViewHolder_Playlist viewHolder = (ViewHolder_Playlist) v
									.getTag();
							parent = (View) viewHolder.ll_images.getParent();
						}
					} else {
						if (isEnglish) {
							ViewHolderSong_English viewHolder = (ViewHolderSong_English) v
									.getTag();
							parent = viewHolder.right.parent;
						} else {
							ViewHolderSong viewHolder = (ViewHolderSong) v
									.getTag();
							parent = viewHolder.right.parent;
						}
					}
					String tag = (String) parent.findViewById(
							R.id.home_music_tile_image).getTag();
					if (tag != null) {
						MediaItem mediaItem = (MediaItem) parent
								.getTag(R.id.view_tag_object);
						MediaType mediaType = mediaItem.getMediaType();
						MediaContentType mediaContentType = mediaItem
								.getMediaContentType();
						View iv = null;
						if (mediaContentType == MediaContentType.RADIO
								|| mediaContentType == MediaContentType.MUSIC
								|| mediaType == MediaType.ALBUM
								|| mediaType == MediaType.PLAYLIST
								|| (mediaType == MediaType.TRACK && mediaContentType != MediaContentType.VIDEO)) {
							iv = parent
									.findViewById(R.id.home_music_tile_image);
						} else {
							iv = parent
									.findViewById(R.id.home_videos_tile_image);
						}
						if (iv != null) {
							Placement placement = (Placement) iv
									.getTag(R.string.key_placement);
							if (placement != null) {
								int adPosition = position;
								if (promoHeight != 0)
									adPosition = position - 1;
								if (!viewedPositions.contains(adPosition + ":"
										+ placement.getCampaignID())) {
									Logger.e(TAG, "Post ad view>>" + adPosition);
									Utils.postViewEvent(mActivity, placement);
									viewedPositions.add(adPosition + ":"
											+ placement.getCampaignID());
								}
							}
						}
					}
				}

				// MediaItem mediaItem = (MediaItem) v
				// .getTag(R.id.view_tag_object);
				// MediaType mediaType = mediaItem.getMediaType();
				// MediaContentType mediaContentType = mediaItem
				// .getMediaContentType();
				// View iv = null;
				// if (mediaContentType == MediaContentType.RADIO
				// || mediaContentType == MediaContentType.MUSIC
				// || mediaType == MediaType.ALBUM
				// || mediaType == MediaType.PLAYLIST
				// || (mediaType == MediaType.TRACK && mediaContentType !=
				// MediaContentType.VIDEO)) {
				// iv = v.findViewById(R.id.home_music_tile_image);
				// } else {
				// iv = v.findViewById(R.id.home_videos_tile_image);
				// }
				// if (iv != null) {
				// Placement placement = (Placement) iv
				// .getTag(R.string.key_placement);
				// if (placement != null) {
				// if (!viewedPositions.contains(position + ":"
				// + placement.getCampaignID())) {
				// Logger.e(TAG, "Post ad view>>" + position);
				// Utils.postViewEvent(mActivity, placement);
				// viewedPositions.add(position + ":"
				// + placement.getCampaignID());
				// }
				// }
				// }
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	private PlacementType getPlacementType() {
		try {
			if (mApplicationConfigurations.getFilterPlaylistsOption()
					&& !mApplicationConfigurations.getFilterSongsOption()
					&& !mApplicationConfigurations.getFilterAlbumsOption()) {
				// return PlacementType.PLAYLIST_AD_UNIT;
				if (mCategoryType.equals(MediaCategoryType.LATEST)) {
					return PlacementType.MUSIC_NEW_PLAYLIST;
				} else if (mCategoryType.equals(MediaCategoryType.POPULAR)) {
					return PlacementType.MUSIC_POP_PLAYLIST;
				}
			} else {
				if (mCategoryType.equals(MediaCategoryType.LATEST)) {
					return PlacementType.MUSIC_NEW;
				} else if (mCategoryType.equals(MediaCategoryType.POPULAR)) {
					return PlacementType.MUSIC_POPULAR;
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	private CampaignsManager mCampaignsManager;
	PlacementType type;

	private Placement getPlacement() {
		try {
			if (type != null)
				return mCampaignsManager.getPlacementOfType(type);
		} catch (Exception e) {
		}
		return null;
	}

	private boolean isAd(MediaItem mediaItem) {
		if (mediaItem != null) {
			String title = mediaItem.getTitle();
			String albumname = mediaItem.getAlbumName();
			String artistname = mediaItem.getArtistName();
			if (!TextUtils.isEmpty(title) && title.equalsIgnoreCase("no")
					&& !TextUtils.isEmpty(albumname)
					&& albumname.equalsIgnoreCase("no")
					&& !TextUtils.isEmpty(artistname)
					&& artistname.equalsIgnoreCase("no")) {
				return true;
			}
		}
		return false;
	}

	private void getAdView(final Placement placement, final int location,
			final ImageView imageTile, final ImageView iv_selector) {
		try {
			mPlacementMap.put(location, placement);

			try {
				// DisplayMetrics metrics = metrics;
				// width = metrics.widthPixels;
				String backgroundLink = Utils.getDisplayProfile(metrics,
						placement);
				if (backgroundLink != null) {

					picasso.load(
							new PicassoCallBack() {

								@Override
								public void onSuccess() {
									imageTile.setTag(R.string.key_placement,
											placement);
									iv_selector.setTag(R.string.key_placement,
											placement);
									imageTile
											.setOnClickListener(MyAdapter.this);
									iv_selector
											.setOnClickListener(MyAdapter.this);
								}

								@Override
								public void onError() {
								}
							}, backgroundLink, imageTile,
							R.drawable.background_home_tile_album_default, tag);
				}
				return;
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View view) {

		if (mOnMediaItemOptionSelectedListener == null) {
			Intent new_intent = new Intent();
			new_intent.setAction(HomeActivity.ACTION_LISTENER);
			mActivity.sendBroadcast(new_intent);
		}

		Logger.e("statrt time", new Date().toString());
		if (mActivity instanceof HomeActivity) {
		}
		Logger.d(TAG, "Simple click on: " + view.toString());
		int viewId = view.getId();
		String tag = (String) view.getTag();

		Placement placement = null;
		try {
			placement = (Placement) view.getTag(R.string.key_placement);
		} catch (Exception e) {
		}

		if ((viewId == R.id.home_music_tile_image || viewId == R.id.iv_selector
				|| viewId == R.id.view_playlist_tile || viewId == R.id.home_videos_tile_image)
				&& tag != null // ((ImageView) view).getDrawable() == null
				&& placement != null) {
			try {
				Utils.performclickEvent(mActivity, placement);
				// System.out.println("Placement :::::::::::::::: " + new
				// Gson().toJson(placement).toString());
				// Intent browserIntent = new Intent(Intent.ACTION_VIEW,
				// Uri.parse(placement.getActions().get(0).action));
				// browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// mContext.startActivity(browserIntent);
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
			return;
		} else if ((viewId == R.id.home_music_tile_image
				|| viewId == R.id.iv_selector
				|| viewId == R.id.view_playlist_tile || viewId == R.id.home_videos_tile_image)
				&& tag != null) {
			return;
		}
		// a tile was clicked, shows its media item's details.
		if (viewId == R.id.home_music_tile_image || viewId == R.id.iv_selector
				|| viewId == R.id.home_videos_tile_image
				|| viewId == R.id.home_videos_tile_button_play
				|| viewId == R.id.view_playlist_tile
				|| viewId == R.id.view_right_tile
				|| viewId == R.id.view_left_tile) {

			RelativeLayout tile;
			// if (viewId == R.id.home_music_tile_image){
			// LinearLayout tile_temp = (LinearLayout) view.getParent();
			// tile = (RelativeLayout) tile_temp.getParent();
			// }else
			MediaItem mediaItem;
			int position;
			try {
				tile = (RelativeLayout) view.getParent();
				mediaItem = (MediaItem) tile.getTag(R.id.view_tag_object);
				position = (Integer) tile.getTag(R.id.view_tag_position);

			} catch (Exception e) {
				if (viewId == R.id.view_playlist_tile) {
					tile = (RelativeLayout) view;
					mediaItem = (MediaItem) tile.getTag(R.id.view_tag_object);
					position = (Integer) tile.getTag(R.id.view_tag_position);
				} else {
					tile = (RelativeLayout) view.getParent().getParent();
					mediaItem = (MediaItem) tile.getTag(R.id.view_tag_object);
					position = (Integer) tile.getTag(R.id.view_tag_position);
				}
			}

			if (viewId == R.id.home_music_tile_image
					|| viewId == R.id.iv_selector
					|| viewId == R.id.view_playlist_tile) {

				if (mOnMediaItemOptionSelectedListener != null) {

					if (mediaItem.getMediaContentType() == MediaContentType.VIDEO
							|| mediaItem.getMediaType() == MediaType.ALBUM
							|| mediaItem.getMediaType() == MediaType.ARTIST
							|| mediaItem.getMediaType() == MediaType.PLAYLIST) {
						if (PlayerService.service != null
								&& PlayerService.service.isPlaying()) {
							mOnMediaItemOptionSelectedListener
									.onMediaItemOptionShowDetailsSelected(
											mediaItem, position);
						} else {

							mOnMediaItemOptionSelectedListener
									.onMediaItemOptionPlayNowSelected(
											mediaItem, position);
							mOnMediaItemOptionSelectedListener
									.onMediaItemOptionShowDetailsSelected(
											mediaItem, position);
							// mOnMediaItemOptionSelectedListener
							// .onMediaItemOptionPlayAndOpenSelected(
							// mediaItem, position);
						}
					} else {
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionPlayNowSelected(mediaItem,
										position);

						Map<String, String> reportMap = new HashMap<String, String>();

						reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong
								.toString(), mediaItem.getTitle());
						reportMap.put(mediaItem.getMediaType().toString(),
								Utils.toWhomSongBelongto(mediaItem));
						reportMap
								.put(FlurryConstants.FlurryKeys.Source
										.toString(),
										FlurryConstants.FlurrySourceDescription.TapOnSongTile
												.toString());
						reportMap.put(FlurryConstants.FlurryKeys.SubSection
								.toString(), mFlurrySubSectionDescription);

						Analytics
								.logEvent(
										FlurryConstants.FlurryEventName.SongSelectedForPlay
												.toString(), reportMap);
					}
					// }
					// }
				} else {

					// homeTileHint.setVisibility(View.GONE);
					Logger.d(TAG, "Show details of: " + mediaItem.getId());

					if (mOnMediaItemOptionSelectedListener != null) {

						if (mediaItem.getMediaContentType() == MediaContentType.VIDEO
								|| mediaItem.getMediaType() == MediaType.ALBUM
								|| mediaItem.getMediaType() == MediaType.ARTIST
								|| mediaItem.getMediaType() == MediaType.PLAYLIST) {
							if (PlayerService.service.isPlaying()) {
								mOnMediaItemOptionSelectedListener
										.onMediaItemOptionShowDetailsSelected(
												mediaItem, position);
							} else {
								mOnMediaItemOptionSelectedListener
										.onMediaItemOptionPlayNowSelected(
												mediaItem, position);
								mOnMediaItemOptionSelectedListener
										.onMediaItemOptionShowDetailsSelected(
												mediaItem, position);
							}
						} else {
							mOnMediaItemOptionSelectedListener
									.onMediaItemOptionPlayNowSelected(
											mediaItem, position);

							Map<String, String> reportMap = new HashMap<String, String>();

							reportMap.put(
									FlurryConstants.FlurryKeys.TitleOfTheSong
											.toString(), mediaItem.getTitle());
							reportMap.put(mediaItem.getMediaType().toString(),
									Utils.toWhomSongBelongto(mediaItem));
							reportMap
									.put(FlurryConstants.FlurryKeys.Source
											.toString(),
											FlurryConstants.FlurrySourceDescription.TapOnSongTile
													.toString());
							reportMap.put(FlurryConstants.FlurryKeys.SubSection
									.toString(), mFlurrySubSectionDescription);

							Analytics
									.logEvent(
											FlurryConstants.FlurryEventName.SongSelectedForPlay
													.toString(), reportMap);

							if (mFlurrySubSectionDescription
									.equalsIgnoreCase(FlurrySubSectionDescription.DiscoveryResults
											.toString())) {

								// Flurry report: Discovery - Result Clicked
								Map<String, String> reportMap1 = new HashMap<String, String>();
								reportMap1
										.put(FlurryConstants.FlurryDiscoveryParams.SongNamePlayed
												.toString(), mediaItem
												.getTitle());
								Analytics
										.logEvent(
												FlurryConstants.FlurryEventName.DiscoveryResultClicked
														.toString(), reportMap1);
							}
						}

						if (mFlurrySubSectionDescription
								.equalsIgnoreCase(FlurrySubSectionDescription.FullPlayerSimilarSongs
										.toString())) {
							Map<String, String> reportMap = new HashMap<String, String>();
							reportMap.put(
									FlurryConstants.FlurryKeys.TitleOfTheSong
											.toString(), mediaItem.getTitle());
							Analytics
									.logEvent(
											FlurryConstants.FlurryEventName.SimilarSongsResultClicked
													.toString(), reportMap);
						}
					}
				}
			} else {
				Logger.d(TAG, "Show details of: " + mediaItem.getId());

				if (mOnMediaItemOptionSelectedListener != null) {

					if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										mediaItem, position);

						// Flurry report:
						Map<String, String> reportMap = new HashMap<String, String>();
						reportMap.put(
								FlurryConstants.FlurryKeys.Title.toString(),
								mediaItem.getTitle());
						reportMap.put(FlurryConstants.FlurryKeys.SubSection
								.toString(), mFlurrySubSectionDescription);
						// xtpl
						// FlurryAgent.logEvent(FlurryConstants.FlurryEventName.SongSelectedForPlay.toString(),
						// reportMap);
						Analytics.logEvent(
								FlurryConstants.FlurryEventName.VideoSelected
										.toString(), reportMap);
						// xtpl

						// Flurry report: Tapped on any related video
						Map<String, String> reportMap1 = new HashMap<String, String>();
						reportMap1.put(
								FlurryConstants.FlurryKeys.Title.toString(),
								mediaItem.getTitle());
						Analytics
								.logEvent(
										FlurryConstants.FlurryEventName.TappedOnAnyRelatedVideo
												.toString(), reportMap1);

					} else if (mediaItem.getMediaType() == MediaType.ALBUM
							|| mediaItem.getMediaType() == MediaType.ARTIST) {
						if (PlayerService.service.isPlaying()) {
							mOnMediaItemOptionSelectedListener
									.onMediaItemOptionShowDetailsSelected(
											mediaItem, position);
						} else {
							mOnMediaItemOptionSelectedListener
									.onMediaItemOptionPlayNowSelected(
											mediaItem, position);
							mOnMediaItemOptionSelectedListener
									.onMediaItemOptionShowDetailsSelected(
											mediaItem, position);

						}
					} else if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
						if (PlayerService.service.isPlaying()) {
							mOnMediaItemOptionSelectedListener
									.onMediaItemOptionShowDetailsSelected(
											mediaItem, position);
						} else {
							mOnMediaItemOptionSelectedListener
									.onMediaItemOptionPlayNowSelected(
											mediaItem, position);
							mOnMediaItemOptionSelectedListener
									.onMediaItemOptionShowDetailsSelected(
											mediaItem, position);
						}
					} else {
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionPlayNowSelected(mediaItem,
										position);

						Map<String, String> reportMap = new HashMap<String, String>();

						reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong
								.toString(), mediaItem.getTitle());
						reportMap.put(mediaItem.getMediaType().toString(),
								Utils.toWhomSongBelongto(mediaItem));
						reportMap
								.put(FlurryConstants.FlurryKeys.Source
										.toString(),
										FlurryConstants.FlurrySourceDescription.TapOnSongTile
												.toString());
						reportMap.put(FlurryConstants.FlurryKeys.SubSection
								.toString(), mFlurrySubSectionDescription);

						Analytics
								.logEvent(
										FlurryConstants.FlurryEventName.SongSelectedForPlay
												.toString(), reportMap);
					}
				}
			}

			if ((viewId == R.id.home_music_tile_image
					|| viewId == R.id.iv_selector || viewId == R.id.home_videos_tile_image)
					&& (mediaItem.getMediaContentType() == MediaContentType.VIDEO
							|| mediaItem.getMediaType() == MediaType.ALBUM
							|| mediaItem.getMediaType() == MediaType.PLAYLIST || mediaItem
							.getMediaType() == MediaType.TRACK)) {
				Map<String, String> reportMap = new HashMap<String, String>();
				if (mediaItem.getMediaContentType() == MediaContentType.VIDEO)
					reportMap.put(FlurryConstants.FlurryKeys.Type.toString(),
							FlurryConstants.FlurryKeys.Video.toString());
				else if (mediaItem.getMediaType() == MediaType.ALBUM)
					reportMap.put(FlurryConstants.FlurryKeys.Type.toString(),
							FlurryConstants.FlurryKeys.Album.toString());
				else if (mediaItem.getMediaType() == MediaType.PLAYLIST)
					reportMap.put(FlurryConstants.FlurryKeys.Type.toString(),
							FlurryConstants.FlurryKeys.Playlist.toString());
				else if (mediaItem.getMediaType() == MediaType.TRACK)
					reportMap.put(FlurryConstants.FlurryKeys.Type.toString(),
							FlurryConstants.FlurryKeys.Song.toString());
				reportMap.put(FlurryConstants.FlurryKeys.Section.toString(),
						mFlurrySubSectionDescription);
				Analytics.logEvent(
						FlurryConstants.FlurryEventName.TileClicked.toString(),
						reportMap);
			}

			// play now was selected.
		} else if (viewId == R.id.home_music_tile_button_play) {

			// LinearLayout tile_temp = (LinearLayout) view.getParent();
			MediaItem mediaItem;
			int position;
			try {
				RelativeLayout tile = (RelativeLayout) view.getParent();
				mediaItem = (MediaItem) tile.getTag(R.id.view_tag_object);
				position = (Integer) tile.getTag(R.id.view_tag_position);

			} catch (Exception e) {
				RelativeLayout tile = (RelativeLayout) view.getParent()
						.getParent();
				mediaItem = (MediaItem) tile.getTag(R.id.view_tag_object);
				position = (Integer) tile.getTag(R.id.view_tag_position);

			}

			Logger.d(TAG, "Play now item: " + mediaItem.getId());

			// homeTileHint.setVisibility(View.GONE);
			if (mOnMediaItemOptionSelectedListener != null) {
				// mOnMediaItemOptionSelectedListener
				// .onMediaItemOptionAddToQueueSelected(mediaItem,
				// position);
				mOnMediaItemOptionSelectedListener
						.onMediaItemOptionPlayNowSelected(mediaItem, position);
			}
			// }

			// Flurry
			Map<String, String> reportMap = new HashMap<String, String>();

			reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong.toString(),
					mediaItem.getTitle());
			reportMap.put(mediaItem.getMediaType().toString(),
					Utils.toWhomSongBelongto(mediaItem));
			reportMap.put(FlurryConstants.FlurryKeys.Source.toString(),
					FlurryConstants.FlurrySourceDescription.TapOnPlayButtonTile
							.toString());
			reportMap.put(FlurryConstants.FlurryKeys.SubSection.toString(),
					mFlurrySubSectionDescription);

			Analytics.logEvent(
					FlurryConstants.FlurryEventName.SongSelectedForPlay
							.toString(), reportMap);

			// remove tile was selected.
		} /*else if (*//*viewId == R.id.home_music_tile_button_remove
				||*//* viewId == R.id.home_videos_tile_button_remove) {

			RelativeLayout tile;
//			if (viewId == R.id.home_music_tile_button_remove) {
//				LinearLayout tile_temp = (LinearLayout) view.getParent();
//				tile = (RelativeLayout) tile_temp.getParent();
//
//			} else
				tile = (RelativeLayout) view.getParent();

			MediaItem mediaItem = (MediaItem) tile.getTag(R.id.view_tag_object);
			int position = (Integer) tile.getTag(R.id.view_tag_position);

			Logger.d(TAG, "Remove item: " + mediaItem.getId() + " :: "
					+ mediaItem.getTitle() + " :: " + position);

			// if (!mOnlyCallbackWhenRemovingItem) {
			// mMediaItems.remove(mediaItem);
			// notifyDataSetChanged();
			// }

			if (mOnMediaItemOptionSelectedListener != null) {
				mOnMediaItemOptionSelectedListener
						.onMediaItemOptionRemoveSelected(mediaItem, position);
			}
		}*/
		Logger.e("statrt time 22", new Date().toString());

	}

	/**
	 * Sets the media items to be presented as tiles in the grid.</br> For
	 * updating the change, call {@code BaseAdapter.notifyDataSetChanged()}/
	 * 
	 * @param mediaItems
	 *            to be presented.
	 */
	public void setMediaItems(List<Object> mediaItems) {

		if (mediaItems != null) {
			if ((itemsData == null || itemsData.size() == 0) && !isCachedDataLoaded) {
				Logger.s("post ad view : clear ad map set");
				firstPositionPost = true;
				mPlacementMap = new HashMap<Integer, Placement>();
			}
			isCachedDataLoaded = false;
			if (getPlacement() == null) {
				List<MediaItem> temp = new ArrayList<MediaItem>();
				// for (Object object : mediaItems) {
				// if(object instanceof ComboMediaItem){
				// ComboMediaItem comboItem=(ComboMediaItem) object;
				//
				// }
				// }
				mediaItems.removeAll(temp);
				mPlacementMap = new HashMap<Integer, Placement>();
			}
			if(itemsData==null)
				itemsData = new ArrayList<Object>();
			else
				itemsData.clear();
			itemsData.addAll(mediaItems);
//			itemsData = mediaItems;
		} else {
			itemsData = new ArrayList<Object>();
			mPlacementMap = new HashMap<Integer, Placement>();
		}
		type = getPlacementType();

		if (mApplicationConfigurations.getFilterPlaylistsOption()
				&& !mApplicationConfigurations.getFilterSongsOption()
				&& !mApplicationConfigurations.getFilterAlbumsOption()) {
			isNeedAdvertiseText=false;
		}


	}

	public void resetAd() {
		Logger.s("post ad view : clear ad map");
		mPlacementMap = new HashMap<Integer, Placement>();
	}

	/**
	 * Register a callback to be invoked when an action was selected to be
	 * performed on the tile.
	 * 
	 * @param listener
	 *            to be invoked.
	 */
	public void setOnMusicItemOptionSelectedListener(
			OnMediaItemOptionSelectedListener listener) {
		mOnMediaItemOptionSelectedListener = listener;
	}

	// ======================================================
	// Images Loading control methods.
	// ======================================================

	public void resumeLoadingImages() {

		// mImageFetcher.setExitTasksEarly(false);
		// notifyDataSetChanged();
	}

	public void stopLoadingImages() {

		// mImageFetcher.setPauseWork(false);
		// mImageFetcher.setExitTasksEarly(true);
		// mImageFetcher.flushCache();

		// mImageFetcher.setExitTasksEarly(true);
		// mImageFetcher.flushCache();
	}

	public void releaseLoadingImages() {
		// mImageFetcher.closeCache();
	}
}