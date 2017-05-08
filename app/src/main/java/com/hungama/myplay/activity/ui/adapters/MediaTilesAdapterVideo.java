package com.hungama.myplay.activity.ui.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
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
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.ui.DownloadConnectingActivity;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.VideoActivity;
import com.hungama.myplay.activity.ui.adapters.MyAdapter.ViewHolder_Promo_Unit;
import com.hungama.myplay.activity.ui.fragments.HomeMediaTileGridFragmentVideo;
import com.hungama.myplay.activity.ui.fragments.PlayerAlbumFragment;
import com.hungama.myplay.activity.ui.fragments.PlayerSimilarFragment;
import com.hungama.myplay.activity.ui.fragments.PlayerVideoFragment;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.ui.widgets.CustomCacheStateProgressBar;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.FlurryConstants.FlurrySubSectionDescription;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.hungama.myplay.activity.util.PicassoUtil.PicassoCallBack;
import com.hungama.myplay.activity.util.QuickAction;
import com.hungama.myplay.activity.util.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Adapter that binds list of {@link MediaItem} objects in tiles.
 */
public class MediaTilesAdapterVideo extends
		RecyclerView.Adapter<RecyclerView.ViewHolder> implements
		OnLongClickListener, OnClickListener {

	private static final String TAG = "MediaTilesAdapter";
	// public static final String VIEW_TAG_ALBoncUM = "Album";

	// protected boolean isHomeActivity;
	Vector<String> viewedPositions;
	private static Context mContext;
	private FragmentActivity mActivity;
	private RecyclerView gridView;

	private Resources mResources;
	// private String artist_id;
	// private String channel_id;
	// private String backgroundLink;
	// private Placement placement;
	private CampaignsManager mCampaignsManager;
	// private int width;
	// private ProgressBar main;
	private String mFragmentName;
	private MediaCategoryType mCategoryType;
	private MediaContentType mContentType;
	private boolean saveOfflineOption;

	private HashMap<Integer, Placement> mPlacementMap = new HashMap<Integer, Placement>();

	public void setChannel_id(String channel_id) {
	}

	public void setDownloadOption(boolean mShowDownloadOption) {
		this.mShowDownloadOption = mShowDownloadOption;
	}

	// private LayoutInflater mInflater;

	// private int mTileSize = 0;

	private boolean mIsEditModeEnabled = true;
	private boolean mIsShowDetailsInOptionsDialogEnabled = true;
	private boolean mShowDeleteButton = false;
	private boolean mShowDownloadOption = true;
	private boolean mShowOptionsDialog = true;

	private boolean mOnlyCallbackWhenRemovingItem = false;

	public List<Object> mMediaItems;

	// Async image loading members.
	// private ImageFetcher mImageFetcher;

	private OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener;

	private Dialog mediaItemOptionsDialog;
	// private BitmapDrawable backgroundImage;

	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;

	// hints
	// private RelativeLayout homeTileHint;

	private String mFlurrySubSectionDescription;
	// private List<ImageView> tileImages = new ArrayList<ImageView>();
	// private String content_id;

	// private Bitmap background_playlist_main_thumb;
	// private Bitmap background_music_tile_dark;
	// private Bitmap background_music_tile_light;

	// boolean tileclickEnabled;
	private boolean firstPositionPost;

	// public void disableTileclick() {
	// tileclickEnabled = false;
	//
	// }
	//
	// public void enableTileclick() {
	// tileclickEnabled = true;
	// }

	QuickAction quickAction;
	PlacementType type;
	// ======================================================
	// ADAPTER'S BASIC FUNCTIONALLITY METHODS.
	// ======================================================
	private FragmentActivity mactivity;
	PicassoUtil picasso;
	private boolean isEnglish;
	boolean needToShowTrend = false;
	private boolean isCachedDataLoaded = false;
	private boolean isNeedToChangeTextColor=false;

	public void setIsNeedToChangeTextColor(boolean isNeedToChangeTextColor) {
		this.isNeedToChangeTextColor = isNeedToChangeTextColor;
	}

	public MediaTilesAdapterVideo(FragmentActivity activity,
			RecyclerView gridView, int tileSize, String fragmentName,
			MediaCategoryType categoryType, MediaContentType contentType,
			CampaignsManager manager, List<MediaItem> mediaItems,
			boolean showDeleteButton, String flurrySubSectionDescription) {
		mActivity = activity;
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(mActivity);
		isEnglish = mApplicationConfigurations.isLanguageSupportedForWidget();
		//(mApplicationConfigurations.getUserSelectedLanguage() == 0);

		// enableTileclick();
		// isHomeActivity = activity instanceof HomeActivity;
		this.mShowDeleteButton = showDeleteButton;
		this.mactivity = activity;
		saveOfflineOption = true;
		this.gridView = gridView;
		mContext = mActivity.getBaseContext();
		mResources = mContext.getResources();
		// mInflater = (LayoutInflater) mContext
		// .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mFragmentName = fragmentName;
		mDataManager = DataManager
				.getInstance(mContext.getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		mCampaignsManager = manager;
		List<Object> temp = new ArrayList<Object>();
		if (mediaItems != null) {
			for (MediaItem item : mediaItems) {
				temp.add(item);
			}
		}
		mMediaItems = temp;
		if(mMediaItems!=null && mMediaItems.size()>0){
			isCachedDataLoaded = true;
		}
		mCategoryType = categoryType;
		mContentType = contentType;
		// mTileSize = tileSize;

		mFlurrySubSectionDescription = flurrySubSectionDescription;

		viewedPositions = new Vector<String>();
		firstPositionPost = true;
		// Logger.d(TAG, "isHomeActivity : "+isHomeActivity);
		// background_playlist_main_thumb =
		// BitmapFactory.decodeResource(mResources,
		// R.drawable.background_playlist_main_thumb);
		// background_music_tile_dark = BitmapFactory.decodeResource(mResources,
		// R.drawable.background_music_tile_dark);
		// background_music_tile_light =
		// BitmapFactory.decodeResource(mResources,
		// R.drawable.background_music_tile_light);

		// mPlacementMap = new HashMap<Integer, Placement>();
		type = getPlacementType();
		picasso = PicassoUtil.with(activity);
	}

	public void setTrendVisibility(boolean needToShowTrend) {
		this.needToShowTrend = needToShowTrend;
	}

	private PlacementType getPlacementType() {
		try {
			if (mFragmentName.equals(HomeMediaTileGridFragmentVideo.class
					.getCanonicalName())) {
				if (mContentType == MediaContentType.VIDEO) {
					// if (mCategoryType.equals(MediaCategoryType.LATEST)) {
					// return PlacementType.VIDEOS_NEW;
					// } else if
					// (mCategoryType.equals(MediaCategoryType.POPULAR)) {
					// return PlacementType.VIDEOS_POPULAR;
					// }
					return PlacementType.VIDEO_NEW;
				} else if (mCategoryType.equals(MediaCategoryType.POPULAR)) {
					return PlacementType.VIDEOS_POPULAR;
				} else if (mCategoryType.equals(MediaCategoryType.MY_STREAM)) {
				}
			} /*
			 * else if (mFragmentName.equals(DiscoveryGalleryFragment.class
			 * .getCanonicalName())) { return PlacementType.DISCOVERY_LISTING; }
			 */else if (mFragmentName.equals(PlayerSimilarFragment.class
					.getCanonicalName())) {
				return PlacementType.PLAYER_SIMILAR_BANNER;
			} else if (mFragmentName.equals(PlayerAlbumFragment.class
					.getCanonicalName())) {
				return PlacementType.PLAYER_ALBUM_BANNER;
			} else if (mFragmentName.equals(PlayerVideoFragment.class
					.getCanonicalName())) {
				return PlacementType.PLAYER_VIDEOS_BANNER;
			} else if (mFragmentName.equals(VideoActivity.class
					.getCanonicalName())) {
				return PlacementType.VIDEO_RELATED_BANNER;
			} /*
			 * else if (mFragmentName.equals(VideoMediaTileGridFragment.class
			 * .getCanonicalName())) { if
			 * (mCategoryType.equals(MediaCategoryType.LATEST)) { return
			 * PlacementType.VIDEOS_NEW; } else if
			 * (mCategoryType.equals(MediaCategoryType.FEATURED)) { return
			 * PlacementType.VIDEOS_POPULAR; } }
			 */
		} catch (Exception e) {
		}
		return null;
	}

	public void clearAndNotify() {
		if(mMediaItems!=null)
		{
			mMediaItems.clear();
			notifyDataSetChanged();
		}
	}

	// inner class to hold a reference to each item of RecyclerView
	public static class ViewHolder extends RecyclerView.ViewHolder {
		public View itemLayoutView;
		// public TextView txtViewTitle;
		// public TextView txtViewType;
		// public ImageView imgViewplaylist, imgViewSong, imgViewAlbum;
		ImageView tileImage;
		ImageView iv_selector;
		ImageView playImage;
		ImageView iv_home_tile_options;
		LanguageTextView textBig;
		LanguageTextView textSmall;
		CustomCacheStateProgressBar progressCacheState;
		RelativeLayout rl_main, llVideoTileTextBackground;

		public ViewHolder(View itemLayoutView) {
			super(itemLayoutView);
			this.itemLayoutView = itemLayoutView;
			tileImage = (ImageView) itemLayoutView
					.findViewById(R.id.home_videos_tile_image);
			iv_selector = (ImageView) itemLayoutView
					.findViewById(R.id.iv_selector);
			playImage = (ImageView) itemLayoutView
					.findViewById(R.id.home_videos_tile_button_play);
			iv_home_tile_options = (ImageView) itemLayoutView
					.findViewById(R.id.iv_home_tile_options);
			textBig = (LanguageTextView) itemLayoutView
					.findViewById(R.id.home_videos_tile_track_text_big);
			textSmall = (LanguageTextView) itemLayoutView
					.findViewById(R.id.home_videos_tile_track_text_small);
			rl_main = (RelativeLayout) itemLayoutView
					.findViewById(R.id.rl_main);

			llVideoTileTextBackground = (RelativeLayout) itemLayoutView
					.findViewById(R.id.llVideoTileBackground);

			progressCacheState = (CustomCacheStateProgressBar) itemLayoutView
					.findViewById(R.id.home_video_tile_progress_cache_state);

			try {

				int screenWidth = 0;
				WindowManager wm = (WindowManager) mContext
						.getSystemService(Context.WINDOW_SERVICE);
				Display display = wm.getDefaultDisplay();
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
					screenWidth = display.getWidth();
				} else {
					Point displaySize = new Point();
					display.getSize(displaySize);
					screenWidth = displaySize.x;
				}
				Logger.s("MediaTiledapter screenWidth:" + screenWidth + ":"
						+ (int) (screenWidth * 0.45));
				// try {
				// rl_main.getLayoutParams().width = screenWidth;
				// rl_main.getLayoutParams().height = (int) (screenWidth *
				// 0.45);
				// } catch (Exception e) {
				// Logger.printStackTrace(e);
				// }

				RelativeLayout.LayoutParams pbParam = new RelativeLayout.LayoutParams(
						screenWidth, (int) (screenWidth * 0.45));
				rl_main.setLayoutParams(pbParam);

			} catch (Error e) {
				Utils.clearCache();
			}

			itemLayoutView.setTag(this);
		}
	}

	public static class ViewHolderEnglish extends RecyclerView.ViewHolder {
		public View itemLayoutView;
		// public TextView txtViewTitle;
		// public TextView txtViewType;
		// public ImageView imgViewplaylist, imgViewSong, imgViewAlbum;
		ImageView tileImage;
		ImageView iv_selector;
		ImageView playImage;
		ImageView iv_home_tile_options;
		TextView textBig;
		TextView textSmall;
		CustomCacheStateProgressBar progressCacheState;
		RelativeLayout rl_main, llVideoTileTextBackground;

		public ViewHolderEnglish(View itemLayoutView) {
			super(itemLayoutView);
			this.itemLayoutView = itemLayoutView;
			tileImage = (ImageView) itemLayoutView
					.findViewById(R.id.home_videos_tile_image);
			iv_selector = (ImageView) itemLayoutView
					.findViewById(R.id.iv_selector);
			playImage = (ImageView) itemLayoutView
					.findViewById(R.id.home_videos_tile_button_play);
			iv_home_tile_options = (ImageView) itemLayoutView
					.findViewById(R.id.iv_home_tile_options);
			textBig = (TextView) itemLayoutView
					.findViewById(R.id.home_videos_tile_track_text_big);
			textSmall = (TextView) itemLayoutView
					.findViewById(R.id.home_videos_tile_track_text_small);
			rl_main = (RelativeLayout) itemLayoutView
					.findViewById(R.id.rl_main);



			llVideoTileTextBackground = (RelativeLayout) itemLayoutView
					.findViewById(R.id.llVideoTileBackground);

			progressCacheState = (CustomCacheStateProgressBar) itemLayoutView
					.findViewById(R.id.home_video_tile_progress_cache_state);

			try {
				if (videoViewWidth == 0 || videoViewHeight == 0) {
					int screenWidth = 0;
					WindowManager wm = (WindowManager) mContext
							.getSystemService(Context.WINDOW_SERVICE);
					Display display = wm.getDefaultDisplay();
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
						screenWidth = display.getWidth();
					} else {
						Point displaySize = new Point();
						display.getSize(displaySize);
						screenWidth = displaySize.x;
					}
					videoViewWidth = screenWidth;
					videoViewHeight = (int) (screenWidth * 0.45);
				}
				Logger.s("MediaTiledapter screenWidth:" + videoViewWidth + ":"
						+ videoViewHeight);

				RelativeLayout.LayoutParams pbParam = new RelativeLayout.LayoutParams(
						videoViewWidth, videoViewHeight);
				rl_main.setLayoutParams(pbParam);

			} catch (Error e) {
				Utils.clearCache();
			}

			itemLayoutView.setTag(this);
		}
	}

	public static final int VIDEO = 0;
	public static final int PROMOUNIT = 1;

	@Override
	public int getItemViewType(int position) {

		Object obj = mMediaItems.get(position);

		if (obj instanceof PromoUnit)
			return PROMOUNIT;
		else
			return VIDEO;
	}

	private static int videoViewWidth = 0, videoViewHeight = 0;

	// Create new views (invoked by the layout manager)
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
			int viewType) {
		View itemLayoutView;

		if (viewType == PROMOUNIT) {
			itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
					R.layout.item_list_promo_unit, null);
			ViewHolder_Promo_Unit viewHolder_Promo_Unit = new ViewHolder_Promo_Unit(
					itemLayoutView);
			return viewHolder_Promo_Unit;
		} else {
			if (isEnglish) {
				itemLayoutView = LayoutInflater.from(parent.getContext())
						.inflate(R.layout.list_item_home_videos_tile_eng, null);
				ViewHolderEnglish viewHolder = new ViewHolderEnglish(
						itemLayoutView);
				viewHolder.iv_home_tile_options
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(final View view) {
								try{
								// if (mActivity instanceof HomeActivity) {
								// if (((HomeActivity)
								// mActivity).mDeafultOpenedTab ==
								// HomeTabBar.TAB_ID_DISCOVER)
								// if (!tileclickEnabled)
								// return;
								// }

									int position = (Integer) view.getTag();
									// showMediaItemOptionsDialog(mediaItem,
									// position);
									quickAction = new QuickAction(mactivity
											.getBaseContext(),
											QuickAction.VERTICAL,
											(MediaItem) mMediaItems.get(position),
											position,
											mOnMediaItemOptionSelectedListener,
											mactivity,
											mFlurrySubSectionDescription,
											saveOfflineOption, mShowDeleteButton,
											null, mShowDownloadOption,
											needToShowTrend);
									quickAction.show(view);
									view.setEnabled(false);
									quickAction
											.setOnDismissListener(new QuickAction.OnDismissListener() {
												@Override
												public void onDismiss() {
													view.setEnabled(true);
												}
											});
								}catch (Exception e){}

							}
						});
				return viewHolder;

			} else {
				itemLayoutView = LayoutInflater.from(parent.getContext())
						.inflate(R.layout.list_item_home_videos_tile, null);

				// create ViewHolder
				ViewHolder viewHolder = new ViewHolder(itemLayoutView);
				viewHolder.iv_home_tile_options
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(final View view) {

								int position = (Integer) view.getTag();
								// showMediaItemOptionsDialog(mediaItem,
								// position);
								quickAction = new QuickAction(mactivity
										.getBaseContext(),
										QuickAction.VERTICAL,
										(MediaItem) mMediaItems.get(position),
										position,
										mOnMediaItemOptionSelectedListener,
										mactivity,
										mFlurrySubSectionDescription,
										saveOfflineOption, mShowDeleteButton,
										null, true, true);
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
						});
				return viewHolder;

			}
		}
	}

	private Placement getPlacement() {
		try {
			if (type != null)
				return mCampaignsManager.getPlacementOfType(type);
		} catch (Exception e) {
		}
		return null;
	}

	public int getItemCount() {
		return mMediaItems != null ? mMediaItems.size() : 0;
	}

	// @Override
	// public int getCount() {
	// return mMediaItems != null ? mMediaItems.size() : 0;
	// }

	// @Override
	// public Object getItem(int position) {
	// return mMediaItems.get(position);
	// }

	@Override
	public long getItemId(int position) {
		if (mMediaItems.get(position) instanceof MediaItem)
			return ((MediaItem) mMediaItems.get(position)).getId();
		else
			return 0;
	}

	int promoWidth, promoHeight;

	private void loadPromoUnit(
			RecyclerView.ViewHolder viewHolder,
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

										Analytics.postPromoAppEvent(mActivity, mPromoUnit, "banner_click", "video");
									}
								});

//						Analytics.postPromoAppEvent(mActivity, mPromoUnit, "banner_view", mFlurrySubSectionDescription);
					}

					@Override
					public void onError() {
					}
				}, images[0], viewHolder_Promo_Unit.imageTile,
						R.drawable.background_home_tile_album_default,
						PicassoUtil.PICASSO_VIDEO_LIST_TAG);
			}
		}
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder,
			int position) {

		if (mMediaItems.get(position) instanceof PromoUnit) {
			loadPromoUnit(viewHolder, (PromoUnit) mMediaItems.get(position));
			return;
		}

		MediaItem mediaItem = (MediaItem) mMediaItems.get(position);
		MediaType mediaType = mediaItem.getMediaType();

		if (isEnglish)
			getVideosView(mediaItem, mediaType, (ViewHolderEnglish) viewHolder,
					position);
		else
			getVideosView(mediaItem, mediaType, (ViewHolder) viewHolder,
					position);

		if (isEnglish) {

			try {
				((ViewHolderEnglish) viewHolder).rl_main.setTag(
						R.id.view_tag_object, mediaItem);
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
			try {
				((ViewHolderEnglish) viewHolder).rl_main.setTag(
						R.id.view_tag_position, position);
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		} else {

			try {
				((ViewHolder) viewHolder).rl_main.setTag(R.id.view_tag_object,
						mediaItem);
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
			try {
				((ViewHolder) viewHolder).rl_main.setTag(
						R.id.view_tag_position, position);
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}
		Logger.e(TAG, position + "firstPositionPost" + firstPositionPost);
		if (firstPositionPost && position == 3) {
			firstPositionPost = false;
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					// Checkforpost(3, 3);
				}
			}, 1000);
		}
		// return convertView;

	}

	Handler handler = new Handler();

	public void setArtist_id(String artist_id) {
		// this.artist_id = artist_id;
	}

	@Override
	public void onClick(View view) {

		if (mOnMediaItemOptionSelectedListener == null) {
			Intent new_intent = new Intent();
			new_intent.setAction(HomeActivity.ACTION_LISTENER);
			mContext.sendBroadcast(new_intent);
		}

		Logger.e("statrt time", new Date().toString());
		// if (mActivity instanceof HomeActivity) {
		// if (((HomeActivity) mActivity).mDeafultOpenedTab ==
		// HomeTabBar.TAB_ID_DISCOVER) {
		// Set<String> tags = Utils.getTags();
		// if (!tags.contains("discover_used")) {
		// tags.add("discover_used");
		// Utils.AddTag(tags);
		// }
		// if (!tileclickEnabled)
		// return;
		//
		// }
		// }
		Logger.d(TAG, "Simple click on: " + view.toString());
		int viewId = view.getId();
		String tag = (String) view.getTag();

		Placement placement = null;
		try {
			placement = (Placement) view.getTag(R.string.key_placement);
		} catch (Exception e) {
		}

		if ((viewId == R.id.home_music_tile_image
				|| viewId == R.id.home_videos_tile_image || viewId == R.id.iv_selector)
				&& tag != null // ((ImageView) view).getDrawable() == null
				&& placement != null) {
			try {
				Utils.performclickEvent(mContext, placement);

			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
			return;
		} else if ((viewId == R.id.home_music_tile_image
				|| viewId == R.id.home_videos_tile_image || viewId == R.id.iv_selector)
				&& tag != null) {
			return;
		}
		// a tile was clicked, shows its media item's details.
		if (viewId == R.id.home_music_tile_image
				|| viewId == R.id.home_videos_tile_image
				|| viewId == R.id.iv_selector
				|| viewId == R.id.home_videos_tile_button_play) {

			RelativeLayout tile;
			// if (viewId == R.id.home_music_tile_image){
			// LinearLayout tile_temp = (LinearLayout) view.getParent();
			// tile = (RelativeLayout) tile_temp.getParent();
			// }else
			tile = (RelativeLayout) view.getParent();

			MediaItem mediaItem = (MediaItem) tile.getTag(R.id.view_tag_object);
			int position = (Integer) tile.getTag(R.id.view_tag_position);

			if (viewId == R.id.home_music_tile_image
					|| viewId == R.id.iv_selector) {

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
									.onMediaItemOptionShowDetailsSelected(
											mediaItem, position);
							mOnMediaItemOptionSelectedListener
									.onMediaItemOptionPlayNowSelected(
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
							if (PlayerService.service != null
									&& PlayerService.service.isPlaying()) {
								mOnMediaItemOptionSelectedListener
										.onMediaItemOptionShowDetailsSelected(
												mediaItem, position);
							} else {
								mOnMediaItemOptionSelectedListener
										.onMediaItemOptionShowDetailsSelected(
												mediaItem, position);
								mOnMediaItemOptionSelectedListener
										.onMediaItemOptionPlayNowSelected(
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
									.onMediaItemOptionShowDetailsSelected(
											mediaItem, position);
							mOnMediaItemOptionSelectedListener
									.onMediaItemOptionPlayNowSelected(
											mediaItem, position);

						}
					} else if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
						if (PlayerService.service.isPlaying()) {
							mOnMediaItemOptionSelectedListener
									.onMediaItemOptionShowDetailsSelected(
											mediaItem, position);
						} else {
							mOnMediaItemOptionSelectedListener
									.onMediaItemOptionShowDetailsSelected(
											mediaItem, position);
							mOnMediaItemOptionSelectedListener
									.onMediaItemOptionPlayNowSelected(
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
					|| viewId == R.id.home_videos_tile_image || viewId == R.id.iv_selector)
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
			RelativeLayout tile = (RelativeLayout) view.getParent();
			MediaItem mediaItem = (MediaItem) tile.getTag(R.id.view_tag_object);
			int position = (Integer) tile.getTag(R.id.view_tag_position);

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

			if (!mOnlyCallbackWhenRemovingItem) {
				mMediaItems.remove(mediaItem);
				notifyDataSetChanged();
			}

			if (mOnMediaItemOptionSelectedListener != null) {
				mOnMediaItemOptionSelectedListener
						.onMediaItemOptionRemoveSelected(mediaItem, position);
			}
		}*/
		Logger.e("statrt time 22", new Date().toString());

	}

	@Override
	public boolean onLongClick(View view) {
		// if (mActivity instanceof HomeActivity) {
		// if (((HomeActivity) mActivity).mDeafultOpenedTab ==
		// HomeTabBar.TAB_ID_DISCOVER) {
		// if (!tileclickEnabled)
		// return false;
		// }
		// }

		Logger.d(TAG, "Long click on: " + view.toString());
		int viewId = view.getId();

		// if ((viewId == R.id.home_music_tile_image || viewId ==
		// R.id.home_videos_tile_image)
		// && ((ImageView) view).getDrawable() == null
		// && placement != null) {
		String tag = (String) view.getTag();
		Placement placement = null;
		try {
			placement = (Placement) view.getTag(R.string.key_placement);
		} catch (Exception e) {
		}

		if ((viewId == R.id.home_music_tile_image
				|| viewId == R.id.home_videos_tile_image || viewId == R.id.iv_selector)
				&& tag != null // ((ImageView) view).getDrawable() == null
				&& placement != null) {
			return false;
		} else if ((viewId == R.id.home_music_tile_image
				|| viewId == R.id.home_videos_tile_image || viewId == R.id.iv_selector)
				&& tag != null) {
			return false;
		}

		// get the item's id from the tile itself.
		LinearLayout tile_temp = (LinearLayout) view.getParent();
		RelativeLayout tile = (RelativeLayout) tile_temp.getParent();
		MediaItem mediaItem = (MediaItem) tile.getTag(R.id.view_tag_object);
		int position = (Integer) tile.getTag(R.id.view_tag_position);

		// show tile's option was selected.
		if (viewId == R.id.home_music_tile_button_play
				|| viewId == R.id.home_music_tile_image) {

			if (mShowOptionsDialog) {
				// sets its tile's options visible.
				showMediaItemOptionsDialog(mediaItem, position);
			}
			return true;

		}

		if (viewId == R.id.home_videos_tile_button_play
				|| viewId == R.id.home_videos_tile_image
				|| viewId == R.id.iv_selector) {
			if (mShowOptionsDialog) {
				// sets its tile's options visible.
				showMediaItemOptionsDialog(mediaItem, position);
			}
			return true;
		}

		return false;
	}

	String home_music_tile_album_decription_title;
	private String home_music_tile_playlist_decription_title;
	private String home_music_tile_playlist_decription_songs_amount;
	private String search_results_layout_bottom_text_for_track;
	private String caching_text_play_offline;
	private String long_click_custom_dialog_save_offline_text;
	private String caching_text_saving;
	private String caching_text_save_offline;
	private String already_offline_message_track;
	private String already_offline_message_for_tracklist;

	void getAdView(final Placement placement, final int location,
			final ImageView imageTile, final ImageView iv_selector) {
		try {
			mPlacementMap.put(location, placement);

			try {
				DisplayMetrics metrics = HomeActivity.metrics;
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
								}

								@Override
								public void onError() {
								}
							}, backgroundLink, imageTile,
							R.drawable.background_home_tile_album_default,
							PicassoUtil.PICASSO_VIDEO_LIST_TAG);

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

	private int videoAdHeight = 0;

	private void loadAd(int location, ViewHolder viewHolder) {
		Placement placement = null;
		int adPosition = location;
		if (promoHeight != 0)
			adPosition = location - 1;
		placement = mPlacementMap.get(adPosition);
		if (placement == null) {
			placement = getPlacement();
		}
		RelativeLayout rl = (RelativeLayout) viewHolder.tileImage.getParent();
		// System.out.println(rl.getHeight() + " ::::::::::s:: " +
		// rl.getWidth());
		// System.out.println(HomeActivity.metrics.widthPixels +
		// " ::::::::::s:: " +
		// mActivity.getResources().getDimension(R.dimen.home_music_tile_margin));
		if (videoAdHeight == 0)
//			videoAdHeight = (int) ((HomeActivity.metrics.widthPixels - mActivity
//					.getResources()
//					.getDimension(R.dimen.home_music_tile_margin)) / 4);
			videoAdHeight = (int) ((HomeActivity.metrics.widthPixels - (2*mActivity
					.getResources()
					.getDimension(R.dimen.home_music_tile_margin))) / 3.57f);
		if (videoAdHeight != 0)
			rl.getLayoutParams().height = videoAdHeight;

		if (placement != null) {
			mPlacementMap.put(adPosition, placement);
			viewHolder.tileImage.setTag("ad");
			viewHolder.tileImage.setTag(R.string.key_placement, null);
			viewHolder.tileImage.setImageDrawable(null);
			viewHolder.tileImage.setOnClickListener(this);

			viewHolder.iv_selector.setTag("ad");
			viewHolder.iv_selector.setTag(R.string.key_placement, null);
			viewHolder.iv_selector.setImageDrawable(null);
			viewHolder.iv_selector.setOnClickListener(this);

			viewHolder.textBig.setVisibility(View.GONE);
			viewHolder.textSmall.setVisibility(View.GONE);
			viewHolder.playImage.setVisibility(View.GONE);
			viewHolder.iv_home_tile_options.setVisibility(View.GONE);
			viewHolder.llVideoTileTextBackground.setVisibility(View.GONE);
			getAdView(placement, location, viewHolder.tileImage,
					viewHolder.iv_selector);
		}
	}

	private void loadAd(int location, ViewHolderEnglish viewHolder) {
		Placement placement = null;
		int adPosition = location;
		if (promoHeight != 0)
			adPosition = location - 1;
		placement = mPlacementMap.get(adPosition);
		if (placement == null) {
			placement = getPlacement();
		}
		RelativeLayout rl = (RelativeLayout) viewHolder.tileImage.getParent();
		// System.out.println(rl.getHeight() + " ::::::::::s:: " +
		// rl.getWidth());
		// System.out.println(HomeActivity.metrics.widthPixels +
		// " ::::::::::s:: " +
		// mActivity.getResources().getDimension(R.dimen.home_music_tile_margin));
		if (videoAdHeight == 0)
//			videoAdHeight = (int) ((HomeActivity.metrics.widthPixels - mActivity
//					.getResources()
//					.getDimension(R.dimen.home_music_tile_margin)) / 4);
			videoAdHeight = (int) ((HomeActivity.metrics.widthPixels - (2*mActivity
					.getResources()
					.getDimension(R.dimen.home_music_tile_margin))) / 3.57f);
		if (videoAdHeight != 0)
			rl.getLayoutParams().height = videoAdHeight;

		if (placement != null) {
			mPlacementMap.put(adPosition, placement);
			viewHolder.tileImage.setTag("ad");
			viewHolder.tileImage.setTag(R.string.key_placement, null);
			viewHolder.tileImage.setImageDrawable(null);
			viewHolder.tileImage.setOnClickListener(this);

			viewHolder.iv_selector.setTag("ad");
			viewHolder.iv_selector.setTag(R.string.key_placement, null);
			viewHolder.iv_selector.setImageDrawable(null);
			viewHolder.iv_selector.setOnClickListener(this);

			viewHolder.textBig.setVisibility(View.GONE);
			viewHolder.textSmall.setVisibility(View.GONE);
			viewHolder.playImage.setVisibility(View.GONE);
			viewHolder.iv_home_tile_options.setVisibility(View.GONE);
			viewHolder.llVideoTileTextBackground.setVisibility(View.GONE);
			getAdView(placement, location, viewHolder.tileImage,
					viewHolder.iv_selector);
		}
	}

	private void getVideosView(final MediaItem mediaItem, MediaType mediaType,
			ViewHolder viewHolder, final int position) {

		// int padding = (int) mActivity.getResources().getDimension(
		// R.dimen.home_music_tile_margin);
		// if (position == 0 && mActivity instanceof HomeActivity
		// && ((HomeActivity) mActivity).isPromoUnit()) {
		// ((RelativeLayout) viewHolder.tileImage.getParent()).setPadding(
		// padding, 0, padding, 0);
		// } else {
		// ((RelativeLayout) viewHolder.tileImage.getParent()).setPadding(
		// padding, padding, padding, 0);
		// }

		try {
			if(isNeedToChangeTextColor){
				viewHolder.textBig.setTextColor(mContext.getResources().getColor(R.color.white));
				viewHolder.textSmall.setTextColor(mContext.getResources().getColor(R.color.white));
			}
			viewHolder.progressCacheState.setVisibility(View.GONE);
			viewHolder.progressCacheState.showProgressOnly(true);
			viewHolder.iv_home_tile_options.setVisibility(View.VISIBLE);
			viewHolder.textBig.setVisibility(View.VISIBLE);
			viewHolder.textSmall.setVisibility(View.VISIBLE);
			viewHolder.playImage.setVisibility(View.VISIBLE);
			viewHolder.llVideoTileTextBackground.setVisibility(View.VISIBLE);

			viewHolder.tileImage.setOnClickListener(this);
			viewHolder.iv_selector.setOnClickListener(this);
			viewHolder.playImage.setOnClickListener(this);
			viewHolder.tileImage.setOnLongClickListener(null);

			viewHolder.textBig.setText(mediaItem.getTitle());
			viewHolder.textSmall.setText(mediaItem.getAlbumName());

			viewHolder.iv_home_tile_options.setTag(position);

			int location = position + 1;
			if (isAd(mediaItem)) {
				loadAd(location, viewHolder);
				return;
			}

			if (videoViewHeight != 0 && videoAdHeight != 0) {
				RelativeLayout rl = (RelativeLayout) viewHolder.tileImage
						.getParent();
				rl.getLayoutParams().height = videoViewHeight;
			} else if (videoViewHeight == 0) {
				int screenWidth = 0;
				WindowManager wm = (WindowManager) mContext
						.getSystemService(Context.WINDOW_SERVICE);
				Display display = wm.getDefaultDisplay();
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
					screenWidth = display.getWidth();
				} else {
					Point displaySize = new Point();
					display.getSize(displaySize);
					screenWidth = displaySize.x;
				}
				videoViewWidth = screenWidth;
				videoViewHeight = (int) (screenWidth * 0.45);
				RelativeLayout rl = (RelativeLayout) viewHolder.tileImage
						.getParent();
				rl.getLayoutParams().height = videoViewHeight;
			}

			String imageUrl = mediaItem.getImageUrl();
			int tileSizeIndex = ImagesManager.HOME_VIDEO_TILE;
//			if (mFlurrySubSectionDescription
//					.equalsIgnoreCase(FlurryConstants.FlurrySubSectionDescription.VideoRelatedAudio
//							.toString()))
//				tileSizeIndex = ImagesManager.MUSIC_ART_SMALL;
			String[] images = ImagesManager.getImagesUrlArray(
					mediaItem.getImagesUrlArray(), tileSizeIndex,
					DataManager.getDisplayDensityLabel());
			if (images != null && images.length > 0)
				imageUrl = images[0];
			if (mContext != null && !TextUtils.isEmpty(imageUrl))
				picasso.load(null, imageUrl, viewHolder.tileImage,
						R.drawable.background_home_tile_album_default,
						PicassoUtil.PICASSO_VIDEO_LIST_TAG);
			else
				viewHolder.tileImage
						.setImageResource(R.drawable.background_home_tile_album_default);

			viewHolder.tileImage.setTag(null);
			viewHolder.tileImage.setTag(R.string.key_placement, null);

			viewHolder.iv_selector.setTag(null);
			viewHolder.iv_selector.setTag(R.string.key_placement, null);

			CacheState cacheState = DBOHandler.getVideoTrackCacheState(
					mContext, "" + mediaItem.getId());
			if (cacheState != CacheState.NOT_CACHED
					&& CacheManager.isProUser(mContext)) {
				viewHolder.progressCacheState.setCacheState(cacheState);
				viewHolder.progressCacheState.setProgress(DBOHandler
						.getVideoTrackCacheProgress(mContext,
								"" + mediaItem.getId()));
				viewHolder.progressCacheState.setVisibility(View.VISIBLE);
			}
		} catch (Exception e) {
			Logger.e(getClass().getName() + ":855", e.toString());
		}

	}

	private void getVideosView(final MediaItem mediaItem, MediaType mediaType,
			ViewHolderEnglish viewHolder, final int position) {

		// int padding = (int) mActivity.getResources().getDimension(
		// R.dimen.home_music_tile_margin);
		// if (position == 0 && mActivity instanceof HomeActivity
		// && ((HomeActivity) mActivity).isPromoUnit()) {
		// ((RelativeLayout) viewHolder.tileImage.getParent()).setPadding(
		// padding, 0, padding, 0);
		// } else {
		// ((RelativeLayout) viewHolder.tileImage.getParent()).setPadding(
		// padding, padding, padding, 0);
		// }

		try {

			if(isNeedToChangeTextColor){
				viewHolder.textBig.setTextColor(mContext.getResources().getColor(R.color.white));
				viewHolder.textSmall.setTextColor(mContext.getResources().getColor(R.color.white));
			}

			viewHolder.progressCacheState.setVisibility(View.GONE);
			viewHolder.progressCacheState.showProgressOnly(true);
			viewHolder.iv_home_tile_options.setVisibility(View.VISIBLE);
			viewHolder.textBig.setVisibility(View.VISIBLE);
			viewHolder.textSmall.setVisibility(View.VISIBLE);
			viewHolder.playImage.setVisibility(View.VISIBLE);
			viewHolder.llVideoTileTextBackground.setVisibility(View.VISIBLE);

			viewHolder.tileImage.setOnClickListener(this);
			viewHolder.iv_selector.setOnClickListener(this);
			viewHolder.playImage.setOnClickListener(this);
			viewHolder.tileImage.setOnLongClickListener(null);

			viewHolder.textBig.setText(mediaItem.getTitle());
			viewHolder.textSmall.setText(mediaItem.getAlbumName());

			viewHolder.iv_home_tile_options.setTag(position);

			int location = position + 1;
			if (getItemViewType(0) == PROMOUNIT)
				location = location - 1;
			if (isAd(mediaItem)) {
				loadAd(location, viewHolder);
				return;
			}

			if (videoViewHeight != 0 && videoAdHeight != 0) {
				RelativeLayout rl = (RelativeLayout) viewHolder.tileImage
						.getParent();
				rl.getLayoutParams().height = videoViewHeight;
			} else if (videoViewHeight == 0) {
				int screenWidth = 0;
				WindowManager wm = (WindowManager) mContext
						.getSystemService(Context.WINDOW_SERVICE);
				Display display = wm.getDefaultDisplay();
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
					screenWidth = display.getWidth();
				} else {
					Point displaySize = new Point();
					display.getSize(displaySize);
					screenWidth = displaySize.x;
				}
				videoViewWidth = screenWidth;
				videoViewHeight = (int) (screenWidth * 0.45);
				RelativeLayout rl = (RelativeLayout) viewHolder.tileImage
						.getParent();
				rl.getLayoutParams().height = videoViewHeight;
			}

			String imageUrl = mediaItem.getImageUrl();
			int tileSizeIndex = ImagesManager.HOME_VIDEO_TILE;
			// if (mFlurrySubSectionDescription
			// .equalsIgnoreCase(FlurryConstants.FlurrySubSectionDescription.VideoRelatedAudio
			// .toString()))
			// tileSizeIndex = ImagesManager.MUSIC_ART_SMALL;
			String[] images = ImagesManager.getImagesUrlArray(
					mediaItem.getImagesUrlArray(), tileSizeIndex,
					DataManager.getDisplayDensityLabel());
			if (images != null && images.length > 0)
				imageUrl = images[0];
			if (mContext != null && !TextUtils.isEmpty(imageUrl))
				picasso.load(null, imageUrl, viewHolder.tileImage,
						R.drawable.background_home_tile_album_default,
						PicassoUtil.PICASSO_VIDEO_LIST_TAG);
			else
				viewHolder.tileImage
						.setImageResource(R.drawable.background_home_tile_album_default);

			viewHolder.tileImage.setTag(null);
			viewHolder.tileImage.setTag(R.string.key_placement, null);

			viewHolder.iv_selector.setTag(null);
			viewHolder.iv_selector.setTag(R.string.key_placement, null);

			CacheState cacheState = DBOHandler.getVideoTrackCacheState(
					mContext, "" + mediaItem.getId());
			if (cacheState != CacheState.NOT_CACHED
					&& CacheManager.isProUser(mContext)) {
				viewHolder.progressCacheState.setCacheState(cacheState);
				viewHolder.progressCacheState.setProgress(DBOHandler
						.getVideoTrackCacheProgress(mContext,
								"" + mediaItem.getId()));
				viewHolder.progressCacheState.setVisibility(View.VISIBLE);
			}
		} catch (Exception e) {
			Logger.e(getClass().getName() + ":855", e.toString());
		}

	}

	private void showMediaItemOptionsDialog(final MediaItem mediaItem,
			final int position) {
		try {
			// set up custom dialog
			mediaItemOptionsDialog = new Dialog(mActivity);
			mediaItemOptionsDialog
					.requestWindowFeature(Window.FEATURE_NO_TITLE);
			// RelativeLayout root = (RelativeLayout) LayoutInflater.from(
			// mActivity).inflate(R.layout.dialog_media_playing_options,
			// null);
			// mediaItemOptionsDialog.setContentView(root);
			// Utils.traverseChild(root, mActivity);
			mediaItemOptionsDialog
					.setContentView(R.layout.dialog_media_playing_options);
			Utils.traverseChild(mediaItemOptionsDialog.getWindow()
					.getDecorView(), mContext);
			mediaItemOptionsDialog.setCancelable(true);
			mediaItemOptionsDialog.show();

			// sets the title.
			LanguageTextView title = (LanguageTextView) mediaItemOptionsDialog
					.findViewById(R.id.long_click_custom_dialog_title_text);
			title.setText(mediaItem.getTitle());

			// sets the cancel button.
			ImageButton closeButton = (ImageButton) mediaItemOptionsDialog
					.findViewById(R.id.long_click_custom_dialog_title_image);
			closeButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mediaItemOptionsDialog.dismiss();
				}
			});

			// sets the options buttons.
			LinearLayout llPlayNow = (LinearLayout) mediaItemOptionsDialog
					.findViewById(R.id.long_click_custom_dialog_download);
			LinearLayout llAddtoQueue = (LinearLayout) mediaItemOptionsDialog
					.findViewById(R.id.long_click_custom_dialog_add_to_queue_row);
			LinearLayout llDetails = (LinearLayout) mediaItemOptionsDialog
					.findViewById(R.id.long_click_custom_dialog_details_row);
			LinearLayout llSaveOffline = (LinearLayout) mediaItemOptionsDialog
					.findViewById(R.id.long_click_custom_dialog_save_offline_row);

			if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
				llPlayNow.setVisibility(View.GONE);
				llAddtoQueue.setVisibility(View.GONE);
			}
			if (mediaItem.getMediaType() == MediaType.ALBUM
					|| mediaItem.getMediaType() == MediaType.PLAYLIST) {
				llPlayNow.setVisibility(View.GONE);
			}

			if (!saveOfflineOption)
				llSaveOffline.setVisibility(View.GONE);
			else {
				llSaveOffline.setTag(false);
				CustomCacheStateProgressBar progressCacheState = (CustomCacheStateProgressBar) mediaItemOptionsDialog
						.findViewById(R.id.long_click_custom_dialog_save_offline_progress_cache_state);
				progressCacheState.setNotCachedStateVisibility(true);
				progressCacheState.setTag(R.id.view_tag_object, mediaItem);

				// DataBase db = new DataBase(HungamaApplication.getContext());
				// db.open();
				// Cursor cursor = db.fetch(DataBase.Track_Cache_table,
				// DataBase.Track_Cache_int,
				// DataBase.tables[DataBase.Track_Cache_int][1] + "=" +
				// mediaItem.getId());
				// if(cursor!=null){
				// if(cursor.moveToFirst()){
				// File file = new File(cursor.getString(2));
				// if(file.exists()){
				// llSaveOffline.setTag(true);
				// ((TextView)
				// mediaItemOptionsDialog.findViewById(R.id.long_click_custom_dialog_save_offline_text)).setText("Play Offline");
				// ((ImageView)
				// mediaItemOptionsDialog.findViewById(R.id.long_click_custom_dialog_save_offline_image)).setImageResource(R.drawable.icon_media_details_saved);
				// }
				// }
				// cursor.close();
				// }
				// db.close();

				if (mediaItem.getMediaType() == MediaType.TRACK) {
					// String path = DBOHandler.getTrackPathById(mContext, "" +
					// mediaItem.getId());
					// if(path!=null && path.length()>0){
					// llSaveOffline.setTag(true);
					// ((TextView)
					// mediaItemOptionsDialog.findViewById(R.id.long_click_custom_dialog_save_offline_text)).setText("Play Offline");
					// ((ImageView)
					// mediaItemOptionsDialog.findViewById(R.id.long_click_custom_dialog_save_offline_image)).setImageResource(R.drawable.icon_media_details_saved);
					// }
					CacheState cacheState;
					int progress = 0;
					if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
						cacheState = DBOHandler.getVideoTrackCacheState(
								mContext, "" + mediaItem.getId());
						progress = DBOHandler.getVideoTrackCacheProgress(
								mContext, "" + mediaItem.getId());
					} else {
						cacheState = DBOHandler.getTrackCacheState(mContext, ""
								+ mediaItem.getId());
						progress = DBOHandler.getTrackCacheProgress(mContext,
								"" + mediaItem.getId());
					}
					if (cacheState == CacheState.CACHED) {
						llSaveOffline.setTag(true);

						if (caching_text_play_offline == null)
							caching_text_play_offline = Utils
									.getMultilanguageTextLayOut(
											mContext,
											mResources
													.getString(R.string.caching_text_play_offline));

						((LanguageTextView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_text))
								.setText(caching_text_play_offline);// "Play Offline"

						((ImageView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_image))
								.setImageResource(R.drawable.icon_media_details_saved);
					} else if (cacheState == CacheState.CACHING
							|| cacheState == CacheState.QUEUED) {
						llSaveOffline.setTag(null);

						if (caching_text_saving == null)
							caching_text_saving = Utils
									.getMultilanguageTextLayOut(
											mContext,
											mResources
													.getString(R.string.caching_text_saving));

						((LanguageTextView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_text))
								.setText(caching_text_saving);
						if (cacheState == CacheState.QUEUED)
							((ImageView) mediaItemOptionsDialog
									.findViewById(R.id.long_click_custom_dialog_save_offline_image))
									.setImageResource(R.drawable.icon_media_details_saving_queue);
						else
							((ImageView) mediaItemOptionsDialog
									.findViewById(R.id.long_click_custom_dialog_save_offline_image))
									.setImageResource(R.drawable.icon_media_details_saving_started);
						// ((ProgressBar)
						// mediaItemOptionsDialog.findViewById(R.id.pb_cache_progress)).setProgress(50);
					}
					progressCacheState.setCacheState(cacheState);
					progressCacheState.setProgress(progress);
				} else if (mediaItem.getMediaType() == MediaType.ALBUM) {
					CacheState cacheState = DBOHandler.getAlbumCacheState(
							mContext, "" + mediaItem.getId());
					if (cacheState == CacheState.CACHED) {
						// int trackCacheCount =
						// DBOHandler.getAlbumCachedCount(mContext, ""+
						// mediaItem.getId());
						// if(trackCacheCount>=mediaItem.getMusicTrackCount())
						llSaveOffline.setTag(true);

						if (caching_text_play_offline == null)
							caching_text_play_offline = Utils
									.getMultilanguageTextLayOut(
											mContext,
											mResources
													.getString(R.string.caching_text_play_offline));

						((LanguageTextView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_text))
								.setText(caching_text_play_offline);
						((ImageView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_image))
								.setImageResource(R.drawable.icon_media_details_saved);
					} else if (cacheState == CacheState.CACHING
							|| cacheState == CacheState.QUEUED) {
						llSaveOffline.setTag(null);

						if (caching_text_saving == null)
							caching_text_saving = Utils
									.getMultilanguageTextLayOut(
											mContext,
											mResources
													.getString(R.string.caching_text_saving));

						((LanguageTextView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_text))
								.setText(caching_text_saving);
						((ImageView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_image))
								.setImageResource(R.drawable.icon_media_details_saving);
					}
					progressCacheState.setCacheCountVisibility(true);
					progressCacheState.setCacheCount(""
							+ DBOHandler.getAlbumCachedCount(mContext, ""
									+ mediaItem.getId()));
					progressCacheState.setCacheState(cacheState);
				} else if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
					CacheState cacheState = DBOHandler.getPlaylistCacheState(
							mContext, "" + mediaItem.getId());
					if (cacheState == CacheState.CACHED) {
						// int trackCacheCount =
						// DBOHandler.getPlaylistCachedCount(mContext, ""+
						// mediaItem.getId());
						// if(trackCacheCount>=mediaItem.getMusicTrackCount())
						llSaveOffline.setTag(true);

						if (caching_text_play_offline == null)
							caching_text_play_offline = Utils
									.getMultilanguageTextLayOut(
											mContext,
											mResources
													.getString(R.string.caching_text_play_offline));

						((LanguageTextView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_text))
								.setText(caching_text_play_offline);
						((ImageView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_image))
								.setImageResource(R.drawable.icon_media_details_saved);
					} else if (cacheState == CacheState.CACHING
							|| cacheState == CacheState.QUEUED) {
						llSaveOffline.setTag(null);

						if (caching_text_saving == null)
							caching_text_saving = Utils
									.getMultilanguageTextLayOut(
											mContext,
											mResources
													.getString(R.string.caching_text_saving));

						((LanguageTextView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_text))
								.setText(caching_text_saving);
						if (cacheState == CacheState.QUEUED)
							((ImageView) mediaItemOptionsDialog
									.findViewById(R.id.long_click_custom_dialog_save_offline_image))
									.setImageResource(R.drawable.icon_media_details_saving_queue);
						else
							((ImageView) mediaItemOptionsDialog
									.findViewById(R.id.long_click_custom_dialog_save_offline_image))
									.setImageResource(R.drawable.icon_media_details_saving_started);
					}
					progressCacheState.setCacheCountVisibility(true);
					progressCacheState.setCacheCount(""
							+ DBOHandler.getPlaylistCachedCount(mContext, ""
									+ mediaItem.getId()));
					progressCacheState.setCacheState(cacheState);
				} else if (mediaItem.getMediaType() == MediaType.VIDEO) {
					CacheState cacheState = DBOHandler.getVideoTrackCacheState(
							mContext, "" + mediaItem.getId());
					if (cacheState == CacheState.CACHED) {
						llSaveOffline.setTag(true);

						if (caching_text_play_offline == null)
							caching_text_play_offline = Utils
									.getMultilanguageTextLayOut(
											mContext,
											mResources
													.getString(R.string.caching_text_play_offline));

						((LanguageTextView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_text))
								.setText(caching_text_play_offline);
						((ImageView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_image))
								.setImageResource(R.drawable.icon_media_details_saved);
					} else if (cacheState == CacheState.CACHING
							|| cacheState == CacheState.QUEUED) {
						llSaveOffline.setTag(null);

						if (caching_text_saving == null)
							caching_text_saving = Utils
									.getMultilanguageTextLayOut(
											mContext,
											mResources
													.getString(R.string.caching_text_saving));

						((LanguageTextView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_text))
								.setText(caching_text_saving);
						((ImageView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_image))
								.setImageResource(R.drawable.icon_media_details_saving);
					}
					progressCacheState.setCacheCountVisibility(true);
					progressCacheState.setCacheCount(""
							+ DBOHandler.getPlaylistCachedCount(mContext, ""
									+ mediaItem.getId()));
					progressCacheState.setCacheState(cacheState);
				}

				// if (!CacheManager.isProUser(mContext)) {
				// ((TextView) mediaItemOptionsDialog
				// .findViewById(R.id.long_click_custom_dialog_save_offline_text))
				// .setText("Save Offline");
				// progressCacheState.setCacheState(CacheState.NOT_CACHED);
				// }
				llSaveOffline.setVisibility(View.VISIBLE);

				// final HoloCircularProgressBar mHoloCircularProgressBar =
				// (HoloCircularProgressBar)
				// llSaveOffline.findViewById(R.id.holoCircularProgressBar1);
				// final ImageView iv = (ImageView)
				// llSaveOffline.findViewById(R.id.long_click_custom_dialog_save_offline_image);
				// Handler h = new Handler();
				// h.postDelayed(new Runnable() {
				// @Override
				// public void run() {
				// RelativeLayout.LayoutParams params = (LayoutParams)
				// mHoloCircularProgressBar.getLayoutParams();
				// System.out.println(params.height + " ::::::::: " +
				// params.width);
				// System.out.println(iv.getLayoutParams().height +
				// " ::::::::: " + iv.getLayoutParams().width);
				// System.out.println(mHoloCircularProgressBar.getHeight() +
				// " ::::::::: " + mHoloCircularProgressBar.getWidth());
				// System.out.println(iv.getHeight() + " ::::::::: " +
				// iv.getWidth());
				// params.height = iv.getWidth();
				// params.width = iv.getWidth();
				// mHoloCircularProgressBar.setLayoutParams(params);
				// System.out.println(mHoloCircularProgressBar.getHeight() +
				// " ::::::::: " + mHoloCircularProgressBar.getWidth());
				// }
				// }, 100);
			}

			if (mIsShowDetailsInOptionsDialogEnabled) {
				llDetails.setVisibility(View.VISIBLE);
			} else {
				llDetails.setVisibility(View.GONE);
			}

			// play now.
			llPlayNow.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View view) {

					// download
					MediaItem trackMediaItem = new MediaItem(mediaItem.getId(),
							mediaItem.getTitle(), mediaItem.getAlbumName(),
							mediaItem.getArtistName(), mediaItem.getImageUrl(),
							mediaItem.getBigImageUrl(), MediaType.TRACK
									.toString(), 0, mediaItem.getAlbumId());
					Intent intent = new Intent(mContext,
							DownloadConnectingActivity.class);
					intent.putExtra(
							DownloadConnectingActivity.EXTRA_MEDIA_ITEM,
							(Serializable) trackMediaItem);
					mActivity.startActivity(intent);
					// if (mActivity instanceof HomeActivity) {
					// if(((HomeActivity)mActivity).mDeafultOpenedTab ==
					// HomeTabBar.TAB_ID_DISCOVER){
					//
					//
					// Set<String> tags = Utils.getTags();
					// if (!tags.contains("discover_used")) {
					// tags.add("discover_used");
					// Utils.AddTag(tags);
					// }
					// }
					// }
					// if (mediaItem.getMediaType() == MediaType.PLAYLIST
					// && mediaItem.getMusicTrackCount() == 0) {
					// Utils.makeText(mActivity,
					//
					// mContext.getString(R.string.no_song_available), 0)
					// .show();
					// }
					//
					// if (!Utils.isConnected()
					// && !mApplicationConfigurations.getSaveOfflineMode()) {
					// try {
					// ((MainActivity) mActivity)
					// .internetConnectivityPopup(new
					// MainActivity.OnRetryClickListener() {
					// @Override
					// public void onRetryButtonClicked() {
					// view.performClick();
					// }
					// });
					// mediaItemOptionsDialog.dismiss();
					// return;
					// } catch (Exception e) {
					// Logger.printStackTrace(e);
					// }
					// }
					//
					// if (mOnMediaItemOptionSelectedListener != null) {
					// mOnMediaItemOptionSelectedListener
					// .onMediaItemOptionPlayNowSelected(mediaItem,
					// position);
					//
					// Map<String, String> reportMap = new HashMap<String,
					// String>();
					//
					// reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong
					// .toString(), mediaItem.getTitle());
					// reportMap.put(mediaItem.getMediaType().toString(),
					// Utils.toWhomSongBelongto(mediaItem));
					// reportMap.put(
					// FlurryConstants.FlurryKeys.Source.toString(),
					// FlurryConstants.FlurrySourceDescription.TapOnPlayInContextualMenu
					// .toString());
					// reportMap.put(FlurryConstants.FlurryKeys.SubSection
					// .toString(), mFlurrySubSectionDescription);
					//
					// FlurryAgent
					// .logEvent(
					// FlurryConstants.FlurryEventName.SongSelectedForPlay
					// .toString(), reportMap);
					// }
					mediaItemOptionsDialog.dismiss();
				}
			});

			// add to queue.
			llAddtoQueue.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View view) {
					// if (mActivity instanceof HomeActivity) {
					// if (((HomeActivity) mActivity).mDeafultOpenedTab ==
					// HomeTabBar.TAB_ID_DISCOVER) {
					// Set<String> tags = Utils.getTags();
					// if (!tags.contains("discover_used")) {
					// tags.add("discover_used");
					// Utils.AddTag(tags);
					// }
					// }
					// }
					if (mediaItem.getMediaType() == MediaType.PLAYLIST
							&& mediaItem.getMusicTrackCount() == 0) {
						Utils.makeText(mActivity,

						mContext.getString(R.string.no_song_available), 0)
								.show();
					}

					if (!Utils.isConnected()
							&& !mApplicationConfigurations.getSaveOfflineMode()) {
						try {
							((MainActivity) mActivity)
									.internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
										@Override
										public void onRetryButtonClicked() {
											view.performClick();
										}
									});
							mediaItemOptionsDialog.dismiss();
							return;
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
					}

					if (mOnMediaItemOptionSelectedListener != null) {
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionAddToQueueSelected(mediaItem,
										position);

						Map<String, String> reportMap = new HashMap<String, String>();

						reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong
								.toString(), mediaItem.getTitle());
						reportMap.put(mediaItem.getMediaType().toString(),
								Utils.toWhomSongBelongto(mediaItem));
						reportMap.put(
								FlurryConstants.FlurryKeys.Source.toString(),
								FlurryConstants.FlurrySourceDescription.TapOnAddToQueueInContextualMenu
										.toString());
						reportMap.put(FlurryConstants.FlurryKeys.SubSection
								.toString(), mFlurrySubSectionDescription);

						Analytics
								.logEvent(
										FlurryConstants.FlurryEventName.SongSelectedForPlay
												.toString(), reportMap);

					}
					mediaItemOptionsDialog.dismiss();
				}
			});

			// show details.
			llDetails.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(final View view) {
					// if (mActivity instanceof HomeActivity) {
					// if (((HomeActivity) mActivity).mDeafultOpenedTab ==
					// HomeTabBar.TAB_ID_DISCOVER) {
					// Set<String> tags = Utils.getTags();
					// if (!tags.contains("discover_used")) {
					// tags.add("discover_used");
					// Utils.AddTag(tags);
					// }
					// }
					// }
					if (!Utils.isConnected()
							&& !mApplicationConfigurations.getSaveOfflineMode()) {
						try {
							((MainActivity) mActivity)
									.internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
										@Override
										public void onRetryButtonClicked() {
											view.performClick();
										}
									});
							mediaItemOptionsDialog.dismiss();
							return;
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
					}

					if (mOnMediaItemOptionSelectedListener != null) {
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										mediaItem, position);
					}
					mediaItemOptionsDialog.dismiss();
				}
			});

			// Save Offline
			llSaveOffline.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(final View view) {
					// if (mActivity instanceof HomeActivity) {
					// if (((HomeActivity) mActivity).mDeafultOpenedTab ==
					// HomeTabBar.TAB_ID_DISCOVER) {
					// Set<String> tags = Utils.getTags();
					// if (!tags.contains("discover_used")) {
					// tags.add("discover_used");
					// Utils.AddTag(tags);
					// }
					// }
					// }
					if (!Utils.isConnected()
							&& !mApplicationConfigurations.getSaveOfflineMode()) {
						try {
							((MainActivity) mActivity)
									.internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
										@Override
										public void onRetryButtonClicked() {
											view.performClick();
										}
									});
							mediaItemOptionsDialog.dismiss();
							return;
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
					}

					if (mOnMediaItemOptionSelectedListener != null
							&& view.getTag() != null) {
						if ((Boolean) view.getTag()) {
							if (mediaItem.getMediaType() == MediaType.TRACK
									|| mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
								// mOnMediaItemOptionSelectedListener
								// .onMediaItemOptionPlayNowSelected(
								// mediaItem, position);
								if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
									CacheManager.removeTrackFromCache(
											mActivity, mediaItem.getId(),
											MediaContentType.MUSIC);
								} else {
									if (already_offline_message_track == null)
										already_offline_message_track = Utils.getMultilanguageTextLayOut(
												mContext,
												mResources
														.getString(R.string.already_offline_message_track));

									Utils.makeText(mActivity,
											already_offline_message_track,
											Toast.LENGTH_SHORT).show();
								}
							} else {
								// AlertDialog.Builder alertDialogBuilder = new
								// AlertDialog.Builder(mActivity);
								// if (mediaItem.getMediaType() ==
								// MediaType.ALBUM)
								// alertDialogBuilder.setMessage(R.string.already_offline_message_album);
								// else
								// alertDialogBuilder.setMessage(R.string.already_offline_message_playlist);
								// alertDialogBuilder.setNegativeButton(R.string.ok,
								// null);
								// alertDialogBuilder.show();

								if (already_offline_message_for_tracklist == null)
									already_offline_message_for_tracklist = Utils.getMultilanguageTextLayOut(
											mContext,
											mResources
													.getString(R.string.already_offline_message_for_tracklist));

								Utils.makeText(mActivity,
										already_offline_message_for_tracklist,
										Toast.LENGTH_SHORT).show();
							}
						} else
							mOnMediaItemOptionSelectedListener
									.onMediaItemOptionSaveOfflineSelected(
											mediaItem, position);
					}
					mediaItemOptionsDialog.dismiss();
				}
			});
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	// ======================================================
	// PUBLIC METHODS.
	// ======================================================

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

	/**
	 * Sets the media items to be presented as tiles in the grid.</br> For
	 * updating the change, call {@code BaseAdapter.notifyDataSetChanged()}/
	 * 
	 * @param mediaItems
	 *            to be presented.
	 */
	public void setMediaItems(List<Object> mediaItems) {

		if (mediaItems != null) {
			if ((mMediaItems == null || mMediaItems.size() == 0) && !isCachedDataLoaded) {
				firstPositionPost = true;
				mPlacementMap = new HashMap<Integer, Placement>();
			}
			isCachedDataLoaded = false;
			if (getPlacement() == null) {
				List<MediaItem> temp = new ArrayList<MediaItem>();
				// int position=0;
				for (Object items : mediaItems) {
					if (items != null && items instanceof MediaItem) {
						MediaItem item = (MediaItem) items;
						if (isAd(item)) {
							temp.add(item);
						}
					}
					// position++;
				}
				mediaItems.removeAll(temp);
				mPlacementMap = new HashMap<Integer, Placement>();
			}
			if(mMediaItems==null)
				mMediaItems = new ArrayList<Object>();
			else
				mMediaItems.clear();
			mMediaItems.addAll(mediaItems);
//			mMediaItems = mediaItems;
		} else {
			mMediaItems = new ArrayList<Object>();
			mPlacementMap = new HashMap<Integer, Placement>();
		}

		notifyDataSetChanged1();
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

	/**
	 * Sets if the Grid's item list can be edited or not.
	 * 
	 * @param isEditModeEnabled
	 */
	public void setEditModeEnabled(boolean isEditModeEnabled) {
		mIsEditModeEnabled = isEditModeEnabled;
	}

	/**
	 * Retrieves if the Grid's item list can be edited or not.
	 * 
	 * @return
	 */
	public boolean isEditModeEnabled() {
		return mIsEditModeEnabled;
	}

	/**
	 * Sets if the Grid's item can suggest showing its details.
	 * 
	 * @param isShowDetailsEnabled
	 */
	public void setShowDetailsInOptionsDialogEnabled(
			boolean isShowDetailsEnabled) {
		mIsShowDetailsInOptionsDialogEnabled = isShowDetailsEnabled;
	}

	/**
	 * Retrieves if the Grid's item can suggest showing its details.
	 */
	public boolean isShowDetailsInOptionsDialogEnabled() {
		return mIsShowDetailsInOptionsDialogEnabled;
	}

	/**
	 * Sets if clicking on removing an item only will invoke the
	 * {@code OnMediaItemOptionSelectedListener.onMediaItemOptionRemoveSelected(MediaItem mediaItem, int position)}
	 * method, if setting to true, the callback will be invoked and you will
	 * have to remove the item yourself and update the Adapter.
	 */
	public void setOnlyCallbackWhenRemovingItem(
			boolean onlyCallbackWhenRemovingItem) {
		mOnlyCallbackWhenRemovingItem = onlyCallbackWhenRemovingItem;
	}

	public void setShowOptionsDialog(boolean showOptionsDialog) {
		mShowOptionsDialog = showOptionsDialog;
	}

	// private void showHomeTileHint() {
	//
	// Animation animationIn = AnimationUtils.loadAnimation(mContext,
	// R.anim.slide_and_show_bottom_enter);
	// final Animation animationOut = AnimationUtils.loadAnimation(mContext,
	// R.anim.slide_and_show_bottom_exit);
	//
	// homeTileHint.setVisibility(View.VISIBLE);
	// homeTileHint.startAnimation(animationIn);
	//
	// final CountDownTimer countDownTimer = new CountDownTimer(7000, 1000) {
	//
	// public void onTick(long millisUntilFinished) {
	//
	// }
	//
	// public void onFinish() {
	// cancel();
	// homeTileHint.startAnimation(animationOut);
	// homeTileHint.setVisibility(View.GONE);
	// }
	// }.start();
	//
	// homeTileHint.setOnClickListener(new OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// homeTileHint.startAnimation(animationOut);
	// countDownTimer.cancel();
	// homeTileHint.setVisibility(View.GONE);
	//
	// }
	// });
	// }

	// public void startFlurrySession() {
	// if (mContext != null) {
	// // FlurryAgent
	// // .setContinueSessionMillis(Constants.FLURRY_SESSION_LENGTH);
	// // FlurryAgent.onStartSession(mActivity,
	// // mActivity.getString(R.string.flurry_app_key));
	// Analytics.startSession(mActivity);
	// }
	// }

	// public void endFlurrySession() {
	// if (mContext != null) {
	// Analytics.onEndSession(mActivity);
	// }
	// }

	public void setMediaCategoryType(MediaCategoryType mediaCategoryType) {
		this.mCategoryType = mediaCategoryType;
	}

	public void setSaveofflineOption(boolean value) {
		saveOfflineOption = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.BaseAdapter#notifyDataSetChanged()
	 */

	public void notifyDataSetChanged1() {

		if (/* CacheManager.isProUser(mContext) */true) {

			try {
				if (mediaItemOptionsDialog != null
						&& mediaItemOptionsDialog.isShowing()) {
					CustomCacheStateProgressBar progressCacheState = (CustomCacheStateProgressBar) mediaItemOptionsDialog
							.findViewById(R.id.long_click_custom_dialog_save_offline_progress_cache_state);
					MediaItem mediaItem = (MediaItem) progressCacheState
							.getTag(R.id.view_tag_object);
					if (mediaItem != null) {
						if (mediaItem.getMediaType() == MediaType.TRACK) {
							CacheState cacheState;
							int progress = 0;
							if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
								cacheState = DBOHandler
										.getVideoTrackCacheState(mContext, ""
												+ mediaItem.getId());
								progress = DBOHandler
										.getVideoTrackCacheProgress(mContext,
												"" + mediaItem.getId());
							} else {
								cacheState = DBOHandler.getTrackCacheState(
										mContext, "" + mediaItem.getId());
								progress = DBOHandler.getTrackCacheProgress(
										mContext, "" + mediaItem.getId());
							}
							LinearLayout llSaveOffline = (LinearLayout) mediaItemOptionsDialog
									.findViewById(R.id.long_click_custom_dialog_save_offline_row);
							if (cacheState == CacheState.CACHED) {
								llSaveOffline.setTag(true);

								if (caching_text_play_offline == null)
									caching_text_play_offline = Utils
											.getMultilanguageTextLayOut(
													mContext,
													mResources
															.getString(R.string.caching_text_play_offline));

								((LanguageTextView) mediaItemOptionsDialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_text))
										.setText(caching_text_play_offline);
								((ImageView) mediaItemOptionsDialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_image))
										.setImageResource(R.drawable.icon_media_details_saved);
							} else if (cacheState == CacheState.CACHING
									|| cacheState == CacheState.QUEUED) {
								llSaveOffline.setTag(null);

								if (caching_text_saving == null)
									caching_text_saving = Utils
											.getMultilanguageTextLayOut(
													mContext,
													mResources
															.getString(R.string.caching_text_saving));

								((LanguageTextView) mediaItemOptionsDialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_text))
										.setText(caching_text_saving);
								if (cacheState == CacheState.QUEUED)
									((ImageView) mediaItemOptionsDialog
											.findViewById(R.id.long_click_custom_dialog_save_offline_image))
											.setImageResource(R.drawable.icon_media_details_saving_queue);
								else
									((ImageView) mediaItemOptionsDialog
											.findViewById(R.id.long_click_custom_dialog_save_offline_image))
											.setImageResource(R.drawable.icon_media_details_saving_started);
							} else if (cacheState == CacheState.NOT_CACHED) {
								llSaveOffline.setTag(false);

								if (caching_text_save_offline == null)
									caching_text_save_offline = Utils
											.getMultilanguageTextLayOut(
													mContext,
													mResources
															.getString(R.string.caching_text_save_offline));

								((LanguageTextView) mediaItemOptionsDialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_text))
										.setText(caching_text_save_offline);
								((ImageView) mediaItemOptionsDialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_image))
										.setImageResource(R.drawable.icon_media_details_saveoffline);
							}
							progressCacheState.setCacheState(cacheState);
							progressCacheState.setProgress(progress);
						} else if (mediaItem.getMediaType() == MediaType.ALBUM) {
							CacheState cacheState = DBOHandler
									.getAlbumCacheState(mContext, ""
											+ mediaItem.getId());
							LinearLayout llSaveOffline = (LinearLayout) mediaItemOptionsDialog
									.findViewById(R.id.long_click_custom_dialog_save_offline_row);
							if (cacheState == CacheState.CACHED) {
								int trackCacheCount = DBOHandler
										.getAlbumCachedCount(mContext, ""
												+ mediaItem.getId());
								if (trackCacheCount >= mediaItem
										.getMusicTrackCount())
									llSaveOffline.setTag(true);

								if (caching_text_play_offline == null)
									caching_text_play_offline = Utils
											.getMultilanguageTextLayOut(
													mContext,
													mResources
															.getString(R.string.caching_text_play_offline));

								((LanguageTextView) mediaItemOptionsDialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_text))
										.setText(caching_text_play_offline);
								((ImageView) mediaItemOptionsDialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_image))
										.setImageResource(R.drawable.icon_media_details_saved);
							} else if (cacheState == CacheState.CACHING
									|| cacheState == CacheState.QUEUED) {
								llSaveOffline.setTag(null);

								if (caching_text_saving == null)
									caching_text_saving = Utils
											.getMultilanguageTextLayOut(
													mContext,
													mResources
															.getString(R.string.caching_text_saving));

								((LanguageTextView) mediaItemOptionsDialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_text))
										.setText(caching_text_saving);
								((ImageView) mediaItemOptionsDialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_image))
										.setImageResource(R.drawable.icon_media_details_saving);
							} else if (cacheState == CacheState.NOT_CACHED) {
								llSaveOffline.setTag(false);
								if (caching_text_save_offline == null)
									caching_text_save_offline = Utils
											.getMultilanguageTextLayOut(
													mContext,
													mResources
															.getString(R.string.caching_text_save_offline));

								((LanguageTextView) mediaItemOptionsDialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_text))
										.setText(caching_text_save_offline);
								((ImageView) mediaItemOptionsDialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_image))
										.setImageResource(R.drawable.icon_media_details_saveoffline);
							}
							progressCacheState.setCacheCountVisibility(true);
							progressCacheState.setCacheCount(""
									+ DBOHandler.getAlbumCachedCount(mContext,
											"" + mediaItem.getId()));
							progressCacheState.setCacheState(cacheState);
						} else if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
							CacheState cacheState = DBOHandler
									.getPlaylistCacheState(mContext, ""
											+ mediaItem.getId());
							LinearLayout llSaveOffline = (LinearLayout) mediaItemOptionsDialog
									.findViewById(R.id.long_click_custom_dialog_save_offline_row);
							if (cacheState == CacheState.CACHED) {
								int trackCacheCount = DBOHandler
										.getPlaylistCachedCount(mContext, ""
												+ mediaItem.getId());
								if (trackCacheCount >= mediaItem
										.getMusicTrackCount())

									if (caching_text_play_offline == null)
										caching_text_play_offline = Utils
												.getMultilanguageTextLayOut(
														mContext,
														mResources
																.getString(R.string.caching_text_play_offline));

								llSaveOffline.setTag(true);
								((LanguageTextView) mediaItemOptionsDialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_text))
										.setText(caching_text_play_offline);
								((ImageView) mediaItemOptionsDialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_image))
										.setImageResource(R.drawable.icon_media_details_saved);
							} else if (cacheState == CacheState.CACHING
									|| cacheState == CacheState.QUEUED) {
								llSaveOffline.setTag(null);

								if (caching_text_saving == null)
									caching_text_saving = Utils
											.getMultilanguageTextLayOut(
													mContext,
													mResources
															.getString(R.string.caching_text_saving));

								((LanguageTextView) mediaItemOptionsDialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_text))
										.setText(caching_text_saving);
								if (cacheState == CacheState.QUEUED)
									((ImageView) mediaItemOptionsDialog
											.findViewById(R.id.long_click_custom_dialog_save_offline_image))
											.setImageResource(R.drawable.icon_media_details_saving_queue);
								else
									((ImageView) mediaItemOptionsDialog
											.findViewById(R.id.long_click_custom_dialog_save_offline_image))
											.setImageResource(R.drawable.icon_media_details_saving_started);
							} else if (cacheState == CacheState.NOT_CACHED) {
								llSaveOffline.setTag(false);

								if (caching_text_save_offline == null)
									caching_text_save_offline = Utils
											.getMultilanguageTextLayOut(
													mContext,
													mResources
															.getString(R.string.caching_text_save_offline));

								((LanguageTextView) mediaItemOptionsDialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_text))
										.setText(caching_text_save_offline);
								((ImageView) mediaItemOptionsDialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_image))
										.setImageResource(R.drawable.icon_media_details_saveoffline);
							}
							progressCacheState.setCacheCountVisibility(true);
							progressCacheState.setCacheCount(""
									+ DBOHandler.getPlaylistCachedCount(
											mContext, "" + mediaItem.getId()));
							progressCacheState.setCacheState(cacheState);
						}
					}
				}
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}
		super.notifyDataSetChanged();
	}

	// public void postFirstAD(String position) {
	// if (!isHomeActivity)
	// return;
	// Logger.e(TAG, " Position :" + position);
	// try {
	// if (viewedPositions == null)
	// viewedPositions = new Vector<String>();
	// if (backgroundLink == null) {
	// backgroundLink = Utils.getDisplayProfile(HomeActivity.metrics,
	// placement);
	// }
	// if (mMediaItems.size() > 0 && placement != null)
	// if (mMediaItems.get(3).getImageUrl().equals(backgroundLink)) {
	// if (!viewedPositions.contains("3:"
	// + placement.getCampaignID())) {
	// Logger.e(TAG, "Post ad view" + 3);
	// Utils.postViewEvent(mContext, placement);
	// viewedPositions.add(3 + "3:"
	// + placement.getCampaignID());
	// }
	// }
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// }

	void Checkforpost(int position, int childPosition) {
		try {
			if(position == 0 && childPosition == 0){
				Object obj = mMediaItems.get(position);
				if (obj != null && obj instanceof PromoUnit) {
					PromoUnit promoUnit = (PromoUnit) obj;
					if (!viewedPositions.contains(position + ":"
							+ promoUnit.getPromo_id())) {
						Analytics.postPromoAppEvent(mActivity, promoUnit, "banner_view", "video");
						viewedPositions.add(position + ":"
								+ promoUnit.getPromo_id());
						return;
					}
				}
			}

			ViewGroup v_parent = (ViewGroup) gridView.getChildAt(childPosition);
			View v = v_parent.getChildAt(0);
			if (v != null && v.getTag(R.id.view_tag_object) != null) {
				MediaItem mediaItem = (MediaItem) v
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
					iv = v.findViewById(R.id.home_music_tile_image);
				} else {
					iv = v.findViewById(R.id.home_videos_tile_image);
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
							Utils.postViewEvent(mContext, placement);
							viewedPositions.add(adPosition + ":"
									+ placement.getCampaignID());
						}
					}
				}
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
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
			if (/* placement != null && */mMediaItems != null
					&& mMediaItems.size() > 0) {
				// System.out.println("CampaignPlayEvent.py request ::: 2 " +
				// placement.getCampaignID() + " :: " +
				// placement.getTrackingID());

				LinearLayoutManager layoutManager = ((LinearLayoutManager) gridView
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
}
