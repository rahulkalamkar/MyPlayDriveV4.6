package com.hungama.myplay.activity.ui.adapters;

import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
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
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.VideoActivity;
import com.hungama.myplay.activity.ui.adapters.MyAdapter.ViewHolder_Promo_Unit;
import com.hungama.myplay.activity.ui.fragments.HomeMediaTileGridFragmentNew;
import com.hungama.myplay.activity.ui.fragments.MediaTileListFragment;
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
import com.hungama.myplay.activity.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Adapter that binds list of {@link MediaItem} objects in tiles.
 */
public class MediaListAdapter extends
		RecyclerView.Adapter<RecyclerView.ViewHolder> implements
		OnLongClickListener, OnClickListener {

	private static final String TAG = "MediaListAdapterNew";
	private static final String VIEW_TAG_ALBUM = "Album";

	// protected boolean isHomeActivity;
	private Vector<String> viewedPositions;
	private Context mContext;
	private FragmentActivity mActivity;
	private RecyclerView gridView;

	// private Placement placement;
	private CampaignsManager mCampaignsManager;
	private int width;
	private String mFragmentName;
	private MediaCategoryType mCategoryType;
	private MediaContentType mContentType;
	private boolean saveOfflineOption;

	public void setChannel_id(String channel_id) {
		// this.channel_id = channel_id;
	}

	private boolean mIsEditModeEnabled = true;
	private boolean mIsShowDetailsInOptionsDialogEnabled = true;
	private boolean mShowOptionsDialog = true;

	private boolean mOnlyCallbackWhenRemovingItem = false;

	private List<Object> mMediaItems;

	// Async image loading members.
	// private ImageFetcher mImageFetcher;

	private OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener;

	private Dialog mediaItemOptionsDialog;
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;

	// hints
	// private RelativeLayout homeTileHint;

	private String mFlurrySubSectionDescription;
	private HashMap<Integer, Placement> mPlacementMap = new HashMap<Integer, Placement>();

	// boolean tileclickEnabled;
	//
	// public void disableTileclick() {
	// tileclickEnabled = false;
	//
	// }
	//
	// public void enableTileclick() {
	// tileclickEnabled = true;
	// }

	// ======================================================
	// ADAPTER'S BASIC FUNCTIONALLITY METHODS.
	// ======================================================

	private PicassoUtil picasso;
	private boolean isEnglish;

	public MediaListAdapter(FragmentActivity activity, RecyclerView gridView,
			String fragmentName, MediaCategoryType categoryType,
			MediaContentType contentType, CampaignsManager manager,
			List<MediaItem> mediaItems, boolean showDeleteButton,
			String flurrySubSectionDescription) {
		mActivity = activity;
		this.mApplicationConfigurations = ApplicationConfigurations
				.getInstance(mActivity);
		isEnglish = mApplicationConfigurations.isLanguageSupportedForWidget();
		//(mApplicationConfigurations.getUserSelectedLanguage() == 0);

		// enableTileclick();
		// isHomeActivity = activity instanceof HomeActivity;
		saveOfflineOption = true;
		this.gridView = gridView;
		mContext = mActivity.getApplicationContext();

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
		mCategoryType = categoryType;
		mContentType = contentType;

		mFlurrySubSectionDescription = flurrySubSectionDescription;
		viewedPositions = new Vector<String>();
		// Logger.d(TAG, "isHomeActivity : "+isHomeActivity);
		// background_playlist_main_thumb =
		// BitmapFactory.decodeResource(mResources,
		// R.drawable.background_playlist_main_thumb);
		// background_music_tile_dark = BitmapFactory.decodeResource(mResources,
		// R.drawable.background_music_tile_dark);
		// background_music_tile_light =
		// BitmapFactory.decodeResource(mResources,
		// R.drawable.background_music_tile_light);
		picasso = PicassoUtil.with(mActivity);
	}

	private Placement getPlacement(PlacementType type) {
		Placement placement = null;
		try {
			if (mFragmentName == null) {
				return null;
			}
			if (mCampaignsManager == null) {
				return null;
			}
			/*
			 * if (mFragmentName.equals(MediaTileGridFragment.class
			 * .getCanonicalName())) { if
			 * (BrowseRadioFragment.mCurrentMediaCategoryType ==
			 * MediaCategoryType.TOP_ARTISTS_RADIO) { placement =
			 * mCampaignsManager .getPlacementOfType(PlacementType.CELEB_RADIO);
			 * } else { placement = mCampaignsManager
			 * .getPlacementOfType(PlacementType.LIVE_RADIO); } } else
			 */if (mFragmentName.equals(MediaTileListFragment.class
					.getCanonicalName())) {
				// if (BrowseRadioFragment.mCurrentMediaCategoryType ==
				// MediaCategoryType.TOP_ARTISTS_RADIO) {
				// // placement = mCampaignsManager
				// // .getPlacementOfType(PlacementType.CELEB_RADIO);
				// placement = mCampaignsManager
				// .getPlacementOfType(PlacementType.CELEB_RADIO_BANNER);
				// } else {
				// // placement = mCampaignsManager
				// // .getPlacementOfType(PlacementType.LIVE_RADIO);
				// placement = mCampaignsManager
				// .getPlacementOfType(PlacementType.LIVE_RADIO_BANNER);
				// }
				placement = mCampaignsManager.getPlacementOfType(type);
			} else if (mFragmentName.equals(HomeMediaTileGridFragmentNew.class
					.getCanonicalName())) {
				if (mContentType == MediaContentType.MUSIC) {
					if (mCategoryType.equals(MediaCategoryType.LATEST)) {
						placement = mCampaignsManager
								.getPlacementOfType(PlacementType.MUSIC_NEW);
					} else if (mCategoryType.equals(MediaCategoryType.POPULAR)) {
						placement = mCampaignsManager
								.getPlacementOfType(PlacementType.MUSIC_POPULAR);
					} else if (mCategoryType
							.equals(MediaCategoryType.MY_STREAM)) {

					}
				} else if (mContentType == MediaContentType.VIDEO) {
					if (mCategoryType.equals(MediaCategoryType.LATEST)) {
						placement = mCampaignsManager
								.getPlacementOfType(PlacementType.VIDEO_NEW);
					} else if (mCategoryType.equals(MediaCategoryType.POPULAR)) {
						placement = mCampaignsManager
								.getPlacementOfType(PlacementType.VIDEOS_POPULAR);
					} else if (mCategoryType
							.equals(MediaCategoryType.MY_STREAM)) {

					}
				}
			} /*
			 * else if (mFragmentName.equals(DiscoveryGalleryFragment.class
			 * .getCanonicalName())) { placement = mCampaignsManager
			 * .getPlacementOfType(PlacementType.DISCOVERY_LISTING); }
			 */else if (mFragmentName.equals(PlayerSimilarFragment.class
					.getCanonicalName())) {
				placement = mCampaignsManager
						.getPlacementOfType(PlacementType.PLAYER_SIMILAR_BANNER);
			} else if (mFragmentName.equals(PlayerAlbumFragment.class
					.getCanonicalName())) {
				placement = mCampaignsManager
						.getPlacementOfType(PlacementType.PLAYER_ALBUM_BANNER);
			} else if (mFragmentName.equals(PlayerVideoFragment.class
					.getCanonicalName())) {
				placement = mCampaignsManager
						.getPlacementOfType(PlacementType.PLAYER_VIDEOS_BANNER);
			} else if (mFragmentName.equals(VideoActivity.class
					.getCanonicalName())) {
				placement = mCampaignsManager
						.getPlacementOfType(PlacementType.VIDEO_RELATED_BANNER);
			} /*
			 * else if (mFragmentName.equals(VideoMediaTileGridFragment.class
			 * .getCanonicalName())) { if
			 * (mCategoryType.equals(MediaCategoryType.LATEST)) { placement =
			 * mCampaignsManager .getPlacementOfType(PlacementType.VIDEOS_NEW);
			 * } else if (mCategoryType.equals(MediaCategoryType.FEATURED)) {
			 * placement = mCampaignsManager
			 * .getPlacementOfType(PlacementType.VIDEOS_POPULAR); } }
			 */
		} catch (Exception e) {
		}
		// placement = new
		// Gson().fromJson("{\"control_parameters\":{\"placement_type\":\"radio_banner\",\"effective_from\":\"2014-05-23T18:30:00Z\",\"effective_till\":\"2014-05-24T18:30:00Z\",\"excess\":false,\"is_shareable\":false,\"display_in_spinner\":false,\"weight\":1.0994195},\"display_info\":{\"hdpi\":\"http://s3.amazonaws.com/ads-cm.hungama.com/465604.jpg\",\"ipad\":\"http://s3.amazonaws.com/ads-cm.hungama.com/465617.jpg\",\"iphone\":\"http://s3.amazonaws.com/ads-cm.hungama.com/465610.jpg\",\"iphone_retina\":\"http://s3.amazonaws.com/ads-cm.hungama.com/465613.jpg\",\"ldpi\":\"http://s3.amazonaws.com/ads-cm.hungama.com/465598.jpg\",\"mdpi\":\"http://s3.amazonaws.com/ads-cm.hungama.com/465601.jpg\",\"xdpi\":\"http://s3.amazonaws.com/ads-cm.hungama.com/465608.jpg\"},\"display_widget_info\":{\"widget_display_options\":{\"actions\":[{\"action\":\"ua://callback/?code\u003d7\u0026content_type\u003d0\u0026content_id\u003d2371961\",\"tracking_id\":\"586\"}],\"tracking_id\":\"586\",\"show_child_text\":false,\"show_text\":false,\"show_thumb\":false,\"skip\":false,\"show_child_media\":false,\"show_bg_img\":false,\"static\":false}}}",
		// Placement.class);
		return placement;
	}

	private class AdLoader {

		public AdLoader(final ImageView tileImage, final int location,
				final Placement placement) {
			setAdBitmap(tileImage, location, placement);
		}

		private void setAdBitmap(final ImageView tileImage, final int location,
				final Placement placement) {
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
									float imgWidth = tileImage.getDrawable()
											.getIntrinsicWidth();
									float imgHeight = tileImage.getDrawable()
											.getIntrinsicHeight();
									Logger.s(imgHeight + " ::: "
											+ imgWidth);
									tileImage.setTag(R.string.key_placement,
											placement);

									DisplayMetrics metrics = HomeActivity.metrics;
									width = metrics.widthPixels;
									int height = (int) (width * (imgHeight / imgWidth));
									Logger.i("height", "height:::" + height);
									tileImage.getLayoutParams().height = height;
									tileImage.requestLayout();
								}

								@Override
								public void onError() {
								}
							}, backgroundLink, tileImage, -1,
							PicassoUtil.PICASSO_RADIO_LIST_TAG);
				}
				return;
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}

		}
	}

	private static final int RADIO = 0;
	private static final int PROMOUNIT = 1;

	@Override
	public int getItemViewType(int position) {

		Object obj = mMediaItems.get(position);

		if (obj instanceof PromoUnit)
			return PROMOUNIT;
		else
			return RADIO;
	}

	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
			int viewType) {
		// create a new view
		View itemLayoutView;
		if (viewType == PROMOUNIT) {
			itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
					R.layout.item_list_promo_unit, null);
			ViewHolder_Promo_Unit viewHolder_Promo_Unit = new ViewHolder_Promo_Unit(
					itemLayoutView);
			return viewHolder_Promo_Unit;
		} else {
			if (isEnglish)
				itemLayoutView = LayoutInflater.from(parent.getContext())
						.inflate(R.layout.list_item_radio_line_eng, null);
			else
				itemLayoutView = LayoutInflater.from(parent.getContext())
						.inflate(R.layout.list_item_radio_line, null);
			// create ViewHolder
			ViewHolder viewHolder = new ViewHolder(itemLayoutView);
			return viewHolder;
		}
	}

	private int promoWidth, promoHeight;

	private void loadPromoUnit(
			android.support.v7.widget.RecyclerView.ViewHolder viewHolder,
			final PromoUnit mPromoUnit) {
		final ViewHolder_Promo_Unit viewHolder_Promo_Unit = (ViewHolder_Promo_Unit) viewHolder;
		if (promoHeight == 0 || promoWidth == 0) {
			promoWidth = (int) (HomeActivity.metrics.widthPixels - (2 * mActivity
					.getResources()
					.getDimension(R.dimen.home_music_tile_margin)));
			viewHolder_Promo_Unit.imageTile.getLayoutParams().width = promoWidth;
			promoHeight = (int) (promoWidth / 4);
		}
		if (promoWidth != 0)
			viewHolder_Promo_Unit.imageTile.getLayoutParams().width = promoWidth;
		if (promoHeight != 0)
			viewHolder_Promo_Unit.imageTile.getLayoutParams().height = promoHeight;

		String[] images = ImagesManager.getImagesUrlArray(
				mPromoUnit.getImagesUrlArray(), ImagesManager.PROMO_UNIT_SIZE,
				DataManager.getDisplayDensityLabel());
		if (images != null && images.length > 0) {
			if (!TextUtils.isEmpty(images[0])) {
				picasso.loadWithFit(new PicassoUtil.PicassoCallBack() {
					@Override
					public void onSuccess() {
						viewHolder_Promo_Unit.imageTile
								.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										if(TextUtils.isEmpty(mPromoUnit.getLanding_url()))
											return;
										Utils.performclickEventAction(
												mActivity,
												mPromoUnit.getLanding_url());

										Analytics.postPromoAppEvent(mActivity, mPromoUnit, "banner_click", "radio");
									}
								});

//						Analytics.postPromoAppEvent(mActivity, mPromoUnit, "banner_view", mFlurrySubSectionDescription);
					}

					@Override
					public void onError() {
					}
				}, images[0], viewHolder_Promo_Unit.imageTile,
						R.drawable.background_home_tile_album_default,
						PicassoUtil.PICASSO_RADIO_LIST_TAG);
			}
		}
	}

	// @Override
	public void onBindViewHolder(RecyclerView.ViewHolder rvViewHolder,
			int position) {
		if (mMediaItems.get(position) instanceof PromoUnit) {
			loadPromoUnit(rvViewHolder, (PromoUnit) mMediaItems.get(position));
			return;
		}

		MediaItem mediaItem = (MediaItem) mMediaItems.get(position);// getItem(position);
		MediaType mediaType = mediaItem.getMediaType();
		MediaContentType mediaContentType = mediaItem.getMediaContentType();
		ViewHolder viewHolder = (ViewHolder) rvViewHolder;
		RelativeLayout rl = (RelativeLayout) viewHolder.itemLayoutView;
		rl.setTag(R.id.view_tag_object, mediaItem);
		rl.setTag(R.id.view_tag_position, position);
		if (mediaContentType == MediaContentType.RADIO) {
			getRadioView(mediaItem, mediaType, viewHolder, position, true);
		} else {
			if (mediaItem.getTitle().equalsIgnoreCase("no")) {
				// if(mediaItem.getId()==-3)
				loadAd(position, viewHolder, PlacementType.DEMAND_RADIO_BANNER);
				// else
				// loadAd(position, viewHolder,
				// PlacementType.LIVE_RADIO_BANNER);
			} else
				getTitleView(mediaItem, viewHolder);
		}
	}

	@Override
	public int getItemCount() {
		return mMediaItems != null ? mMediaItems.size() : 0;
	}

	@Override
	public long getItemId(int position) {
		if (mMediaItems.get(position) instanceof MediaItem)
			return ((MediaItem) mMediaItems.get(position)).getId();
		else
			return 0;
	}

	private void getTitleView(MediaItem mediaItem, ViewHolder viewHolder) {
		viewHolder.relativelayout_list_radio_line.setVisibility(View.GONE);
		viewHolder.imageAd.setTag(null);
		viewHolder.imageAd.setTag(R.string.key_placement, null);
		viewHolder.imageAd.setOnClickListener(null);
		viewHolder.relativelayout_list_radio_line_title
				.setVisibility(View.VISIBLE);
		if (viewHolder.textTitle instanceof LanguageTextView) {
			LanguageTextView text;// = new TextView(mContext);
			text = (LanguageTextView) viewHolder.itemLayoutView
					.findViewById(R.id.list_radio_line_top_text_title);
			String titleText = mediaItem.getTitle();
			text.setText(Utils.getMultilanguageText(mActivity, titleText));
		} else {
			TextView text;// = new TextView(mContext);
			text = (TextView) viewHolder.itemLayoutView
					.findViewById(R.id.list_radio_line_top_text_title);
			String titleText = mediaItem.getTitle().toUpperCase();
			text.setText(titleText);
		}
		// return parent;
	}

	private void loadAd(int position, ViewHolder viewHolder, PlacementType type) {
		viewHolder.relativelayout_list_radio_line_title
				.setVisibility(View.GONE);
		viewHolder.relativelayout_list_radio_line.setVisibility(View.VISIBLE);
		int location = position + 1;
		ImageView ll = viewHolder.imageAd;
		int adPosition = location;
		if (promoHeight != 0)
			adPosition = location - 1;
		Placement placement = mPlacementMap.get(adPosition);
		if (placement == null) {
			placement = getPlacement(type);
		}
		if (placement != null) {
			mPlacementMap.put(adPosition, placement);
			viewHolder.imageTile.setVisibility(View.GONE);
			viewHolder.buttonPlay.setVisibility(View.GONE);
			viewHolder.textTitle.setVisibility(View.GONE);
			((RelativeLayout) viewHolder.textTitle.getParent())
					.setVisibility(View.GONE);

			ll.setVisibility(View.VISIBLE);
			DisplayMetrics metrics = HomeActivity.metrics;
			width = metrics.widthPixels;
			int height = (int) (width * 0.147);
			Logger.i("height", "height:::" + height);
			ll.getLayoutParams().height = height;
			// }
			ll.setTag("ad");
			ll.setTag(R.string.key_placement, null);
			new AdLoader(ll, adPosition, placement);

			ll.setOnClickListener(this);

		} else {
			ll.setVisibility(View.GONE);
			ll.setTag(null);
			ll.setTag(R.string.key_placement, null);
			ll.setOnClickListener(null);

			viewHolder.imageTile.setVisibility(View.VISIBLE);
			viewHolder.buttonPlay.setVisibility(View.VISIBLE);
			viewHolder.textTitle.setVisibility(View.VISIBLE);
			((RelativeLayout) viewHolder.textTitle.getParent())
					.setVisibility(View.VISIBLE);

			((RelativeLayout) ll.getParent()).forceLayout();
		}
	}

	private void getRadioView(MediaItem mediaItem, MediaType mediaType,
			ViewHolder viewHolder, int position, boolean isRadio) {

		viewHolder.relativelayout_list_radio_line.setVisibility(View.VISIBLE);
		viewHolder.relativelayout_list_radio_line_title
				.setVisibility(View.GONE);

		// convertView.setTag(viewHolder);

		viewHolder.imageAd.setVisibility(View.GONE);
		viewHolder.imageAd.setTag(null);
		viewHolder.imageAd.setTag(R.string.key_placement, null);
		viewHolder.imageAd.setOnClickListener(null);

		viewHolder.imageTile.setVisibility(View.VISIBLE);
		viewHolder.buttonPlay.setVisibility(View.VISIBLE);
		viewHolder.textTitle.setVisibility(View.VISIBLE);
		((RelativeLayout) viewHolder.textTitle.getParent())
				.setVisibility(View.VISIBLE);

		((RelativeLayout) viewHolder.imageAd.getParent()).forceLayout();

		((RelativeLayout) viewHolder.imageTile.getParent().getParent())
				.setOnClickListener(this);

		viewHolder.imageTile.setOnClickListener(this);
		viewHolder.buttonPlay.setOnClickListener(this);

		// media type different viewing of the tile.
		if (mediaType == MediaType.ALBUM || mediaType == MediaType.ARTIST) {

			if (viewHolder.textTitle instanceof LanguageTextView)
				((LanguageTextView) viewHolder.textTitle).setText(mediaItem
						.getTitle());
			else
				((TextView) viewHolder.textTitle).setText(mediaItem.getTitle());
			// loads the image for it.
			viewHolder.imageTile.setTag(R.id.view_tag_type, VIEW_TAG_ALBUM);

			try {
				// String imageUrl = mediaItem.getImageUrl();
				String imageUrl = ImagesManager
						.getRadioListArtImageUrl(mediaItem.getImagesUrlArray());
				if ((mContext != null && !TextUtils.isEmpty(imageUrl))) {

					picasso.load(null, imageUrl, viewHolder.imageTile,
							R.drawable.background_home_tile_album_default,
							PicassoUtil.PICASSO_RADIO_LIST_TAG);

				} else {
					viewHolder.imageTile
							.setImageResource(R.drawable.background_home_tile_album_default);
				}
			} catch (Error e) {
				Logger.e(getClass() + ":701", e.toString());
			}

			// CacheState cacheState = DBOHandler.getAlbumCacheState(mContext,
			// ""
			// + mediaItem.getId());
			// // if(cacheState==CacheState.CACHED){
			// //
			// imageCacheState.setBackgroundResource(R.drawable.icon_media_details_saved);
			// // imageCacheState.setVisibility(View.VISIBLE);
			// // } else if(cacheState==CacheState.CACHING ||
			// // cacheState==CacheState.QUEUED){
			// //
			// imageCacheState.setBackgroundResource(R.drawable.icon_media_details_saving);
			// // imageCacheState.setVisibility(View.VISIBLE);
			// // }
			// if (cacheState != CacheState.NOT_CACHED
			// /* && CacheManager.isProUser(mContext) */) {
			// progressCacheState.setCacheCountVisibility(true);
			// progressCacheState.setCacheCount(""
			// + DBOHandler.getAlbumCachedCount(mContext, ""
			// + mediaItem.getId()));
			// progressCacheState.setCacheState(cacheState);
			// progressCacheState.setVisibility(View.VISIBLE);
			// } else if (DBOHandler.getAlbumCachedCount(mContext,
			// "" + mediaItem.getId()) > 0) {
			// progressCacheState.setCacheCountVisibility(true);
			// progressCacheState.setCacheCount(""
			// + DBOHandler.getAlbumCachedCount(mContext, ""
			// + mediaItem.getId()));
			// progressCacheState.setCacheState(CacheState.CACHED);
			// progressCacheState.setVisibility(View.VISIBLE);
			// }
		} else if (mediaType == MediaType.PLAYLIST) {

		} else if (mediaType == MediaType.TRACK || mediaType == MediaType.LIVE) {
			try {
				// textDescription.setVisibility(View.VISIBLE);

				/*
				 * Creates a pattern of coloring the tiles.
				 */

				if (isRadio) {
					try {
						// String imageUrl = mediaItem.getImageUrl();
						String imageUrl = ImagesManager
								.getRadioListArtImageUrl(mediaItem
										.getImagesUrlArray());
						if ((mContext != null && !TextUtils.isEmpty(imageUrl))) {

							picasso.load(
									null,
									imageUrl,
									viewHolder.imageTile,
									R.drawable.background_home_tile_album_default,
									PicassoUtil.PICASSO_RADIO_LIST_TAG);
						} else {
							viewHolder.imageTile
									.setImageResource(R.drawable.background_home_tile_album_default);
						}
					} catch (Error e) {
						Logger.e(getClass() + ":701", e.toString());
					}

				}

				if (viewHolder.textTitle instanceof LanguageTextView)
					((LanguageTextView) viewHolder.textTitle).setText(mediaItem
							.getTitle());
				else
					((TextView) viewHolder.textTitle).setText(mediaItem
							.getTitle());

			} catch (Exception e) {
			} catch (Error e) {
			}
		}

		// if ((location == 4 || ((location - 4) % 6 == 0)) && placement !=
		// null) {
		// try {
		// imageTile.setBackgroundDrawable(null);
		// imageTile.setBackgroundResource(R.drawable.album_main_thumb);
		// // main.setVisibility(View.VISIBLE);
		//
		// setAdBitmap(imageTile, location);
		// imageTile.setTag("ad");
		// imageTile.setTag(R.string.key_placement, placement);
		// imageTile.setImageDrawable(null);
		// imageTile.setOnClickListener(this);
		// imageTile.setOnLongClickListener(null);
		// textTitle.setVisibility(View.GONE);
		// // textDescription.setVisibility(View.GONE);
		// // textTitleSongOrPlaylist.setVisibility(View.GONE);
		// buttonPlay.setVisibility(View.GONE);
		// ((RelativeLayout) convertView
		// .findViewById(R.id.radio_translucent_strip_layout))
		// .setVisibility(View.GONE);
		// } catch (Exception e) {
		// e.printStackTrace();
		// } catch (Error e) {
		// e.printStackTrace();
		// }
		// } else {
		// imageTile.setTag(null);
		// imageTile.setTag(R.string.key_placement, null);
		// }
	}

// 	Handler handler = new Handler();

	public void setArtist_id(String artist_id) {
		// this.artist_id = artist_id;
	}

	@Override
	public void onClick(View view) {
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
		// }
		// }

		Logger.d(TAG, "Simple click on: " + view.toString());
		int viewId = view.getId();
		String tag = null;
		if (viewId != R.id.relativelayout_list_radio_line)
			tag = (String) view.getTag();

		Placement placement = null;
		try {
			placement = (Placement) view.getTag(R.string.key_placement);
		} catch (Exception e) {
		}

		if ((viewId == R.id.home_music_tile_image
				|| viewId == R.id.home_videos_tile_image
				|| viewId == R.id.list_radio_line_media_image
				|| viewId == R.id.relativelayout_list_radio_line || viewId == R.id.list_radio_line_media_banner_ad)
				&& tag != null // ((ImageView) view).getDrawable() == null
				&& placement != null) {
			try {
				Utils.performclickEvent(mContext, placement);
				// Intent browserIntent = new Intent(Intent.ACTION_VIEW,
				// Uri.parse(placement.getActions().get(0).action));
				// browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// mContext.startActivity(browserIntent);
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
			return;
		}
		// a tile was clicked, shows its media item's details.
		if (viewId == R.id.home_music_tile_image
				|| viewId == R.id.list_radio_line_media_image
				|| viewId == R.id.home_videos_tile_image
				|| viewId == R.id.home_videos_tile_button_play
				|| viewId == R.id.relativelayout_list_radio_line
				|| viewId == R.id.list_radio_line_button_play) {

			View tempView = view;
			View tile = view;
			while (tempView.getTag(R.id.view_tag_object) == null) {
				tempView = (View) tempView.getParent();
				if (tempView == null)
					break;
				else
					tile = tempView;
				// tile.getTag(R.id.view_tag_object)
			}
			// if (viewId == R.id.relativelayout_list_radio_line)
			// tile = (RelativeLayout) view;
			// else
			// tile = (RelativeLayout) view.getParent().getParent();

			MediaItem mediaItem = (MediaItem) tile.getTag(R.id.view_tag_object);
			int position = (Integer) tile.getTag(R.id.view_tag_position);

			if (viewId == R.id.home_music_tile_image
					|| viewId == R.id.list_radio_line_media_image
					|| viewId == R.id.relativelayout_list_radio_line) {

				boolean isFirstVisitToPage = mApplicationConfigurations
						.isFirstVisitToHomeTilePage();
				// homeTileHint = (RelativeLayout) tile
				// .findViewById(R.id.home_tile_hint);
				if ((isFirstVisitToPage && (mediaItem.getMediaContentType() != MediaContentType.VIDEO && mediaItem
						.getMediaContentType() != MediaContentType.RADIO))) {
					isFirstVisitToPage = false;
					mApplicationConfigurations
							.setIsFirstVisitToHomeTilePage(false);
					showHomeTileHint();
				} else if (mApplicationConfigurations.getHintsState()) {
					if (!mApplicationConfigurations
							.isHomeHintShownInThisSession()) {
						mApplicationConfigurations
								.setIsHomeHintShownInThisSession(true);
						showHomeTileHint();
					} else {

						// homeTileHint.setVisibility(View.GONE);
						Logger.d(TAG, "Show details of: " + mediaItem.getId());

						if (mOnMediaItemOptionSelectedListener != null) {

							if (mediaItem.getMediaContentType() == MediaContentType.VIDEO
									|| mediaItem.getMediaType() == MediaType.ALBUM
									|| mediaItem.getMediaType() == MediaType.ARTIST
									|| mediaItem.getMediaType() == MediaType.PLAYLIST) {
								mOnMediaItemOptionSelectedListener
										.onMediaItemOptionShowDetailsSelected(
												mediaItem, position);
							} else {
								mOnMediaItemOptionSelectedListener
										.onMediaItemOptionAddToQueueSelected(
												mediaItem, position);

								Map<String, String> reportMap = new HashMap<String, String>();

								reportMap
										.put(FlurryConstants.FlurryKeys.TitleOfTheSong
												.toString(), mediaItem
												.getTitle());
								reportMap.put(mediaItem.getMediaType()
										.toString(), Utils
										.toWhomSongBelongto(mediaItem));
								reportMap
										.put(FlurryConstants.FlurryKeys.Source
												.toString(),
												FlurryConstants.FlurrySourceDescription.TapOnSongTile
														.toString());
								reportMap.put(
										FlurryConstants.FlurryKeys.SubSection
												.toString(),
										mFlurrySubSectionDescription);

								Analytics
										.logEvent(
												FlurryConstants.FlurryEventName.SongSelectedForPlay
														.toString(), reportMap);
							}
						}
					}
				} else {

					// homeTileHint.setVisibility(View.GONE);
					Logger.d(TAG, "Show details of: " + mediaItem.getId());

					if (mOnMediaItemOptionSelectedListener != null) {

						if (mediaItem.getMediaContentType() == MediaContentType.VIDEO
								|| mediaItem.getMediaType() == MediaType.ALBUM
								|| mediaItem.getMediaType() == MediaType.ARTIST
								|| mediaItem.getMediaType() == MediaType.PLAYLIST) {
							mOnMediaItemOptionSelectedListener
									.onMediaItemOptionShowDetailsSelected(
											mediaItem, position);
						} else {
							mOnMediaItemOptionSelectedListener
									.onMediaItemOptionAddToQueueSelected(
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
									.equalsIgnoreCase(FlurryConstants.FlurrySubSectionDescription.DiscoveryResults
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
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										mediaItem, position);
					} else if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										mediaItem, position);
					} else {
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionAddToQueueSelected(mediaItem,
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

			if (viewId == R.id.relativelayout_list_radio_line) {
				Map<String, String> reportMap = new HashMap<String, String>();
				reportMap.put(FlurryConstants.FlurryKeys.Type.toString(),
						FlurryConstants.FlurryKeys.Radio.toString());
				reportMap.put(FlurryConstants.FlurryKeys.Section.toString(),
						mFlurrySubSectionDescription);
				Analytics.logEvent(
						FlurryConstants.FlurryEventName.TileClicked.toString(),
						reportMap);
			}

			// play now was selected.
		} else if (viewId == R.id.home_music_tile_button_play) {

			RelativeLayout tile = (RelativeLayout) view.getParent().getParent();
			MediaItem mediaItem = (MediaItem) tile.getTag(R.id.view_tag_object);
			int position = (Integer) tile.getTag(R.id.view_tag_position);

			Logger.d(TAG, "Play now item: " + mediaItem.getId());

			// Check if first time page is shown
			// yes - show hint

			boolean isFirstVisitToPage = mApplicationConfigurations
					.isFirstVisitToHomeTilePage();
			// homeTileHint = (RelativeLayout) tile
			// .findViewById(R.id.home_tile_hint);
			if ((isFirstVisitToPage && (mediaItem.getMediaContentType() != MediaContentType.VIDEO && mediaItem
					.getMediaContentType() != MediaContentType.RADIO))) {
				isFirstVisitToPage = false;
				mApplicationConfigurations.setIsFirstVisitToHomeTilePage(false);
				showHomeTileHint();
			} else if (mApplicationConfigurations.getHintsState()) {
				if (!mApplicationConfigurations.isHomeHintShownInThisSession()) {
					mApplicationConfigurations
							.setIsHomeHintShownInThisSession(true);
					showHomeTileHint();
				} else {

					// homeTileHint.setVisibility(View.GONE);
					if (mOnMediaItemOptionSelectedListener != null) {
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionAddToQueueSelected(mediaItem,
										position);
					}
				}
			} else {

				// homeTileHint.setVisibility(View.GONE);
				if (mOnMediaItemOptionSelectedListener != null) {
					mOnMediaItemOptionSelectedListener
							.onMediaItemOptionAddToQueueSelected(mediaItem,
									position);
				}
			}

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
		}/* else if (*//*viewId == R.id.home_music_tile_button_remove
				|| *//*viewId == R.id.home_videos_tile_button_remove) {

			RelativeLayout tile = (RelativeLayout) view.getParent().getParent();
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
	}

	@Override
	public boolean onLongClick(View view) {
		// if (mActivity instanceof HomeActivity) {
		// if (((HomeActivity) mActivity).mDeafultOpenedTab ==
		// HomeTabBar.TAB_ID_DISCOVER)
		// if (!tileclickEnabled)
		// return false;
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

		if ((viewId == R.id.home_music_tile_image || viewId == R.id.home_videos_tile_image)
				&& tag != null // ((ImageView) view).getDrawable() == null
				&& placement != null) {
			return false;
		}

		// get the item's id from the tile itself.
		RelativeLayout tile = (RelativeLayout) view.getParent().getParent();
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
				|| viewId == R.id.home_videos_tile_image) {
			if (mShowOptionsDialog) {
				// sets its tile's options visible.
				showMediaItemOptionsDialog(mediaItem, position);
			}
			return true;
		}

		return false;
	}

	private static class ViewHolder extends RecyclerView.ViewHolder {
		private View itemLayoutView;
		private ImageView imageTile;
		private ImageView imageAd;
		private ImageButton buttonPlay;
		private View textTitle;
		private View relativelayout_list_radio_line,
				relativelayout_list_radio_line_title;

		private ViewHolder(View itemLayoutView) {
			super(itemLayoutView);
			this.itemLayoutView = itemLayoutView;
			imageTile = (ImageView) itemLayoutView
					.findViewById(R.id.list_radio_line_media_image);
			imageAd = (ImageView) itemLayoutView
					.findViewById(R.id.list_radio_line_media_banner_ad);
			buttonPlay = (ImageButton) itemLayoutView
					.findViewById(R.id.list_radio_line_button_play);

			textTitle = itemLayoutView
					.findViewById(R.id.list_radio_line_top_text);
			relativelayout_list_radio_line = itemLayoutView
					.findViewById(R.id.relativelayout_list_radio_line);
			relativelayout_list_radio_line_title = itemLayoutView
					.findViewById(R.id.relativelayout_list_radio_line_title);
		}
	}

	private void showMediaItemOptionsDialog(final MediaItem mediaItem,
			final int position) {
		try {
			// set up custom dialog
			mediaItemOptionsDialog = new Dialog(mActivity);
			mediaItemOptionsDialog
					.requestWindowFeature(Window.FEATURE_NO_TITLE);
			mediaItemOptionsDialog
					.setContentView(R.layout.dialog_media_playing_options);
			mediaItemOptionsDialog.setCancelable(true);
			mediaItemOptionsDialog.show();

			// sets the title.
			LanguageTextView title = (LanguageTextView) mediaItemOptionsDialog
					.findViewById(R.id.long_click_custom_dialog_title_text);
			title.setText(mediaItem.getTitle());

			// sets the cancel button.
			ImageButton closeButton = (ImageButton) mediaItemOptionsDialog
					.findViewById(R.id.long_click_custom_dialog_title_image);
			closeButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					mediaItemOptionsDialog.dismiss();
				}
			});

			// sets the options buttons.
			LinearLayout llPlayNow = (LinearLayout) mediaItemOptionsDialog
					.findViewById(R.id.long_click_custom_dialog_play_now_row);
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
						((LanguageTextView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_text))
								.setText(mContext.getResources().getString(
										R.string.caching_text_play_offline));// "Play Offline"
						((ImageView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_image))
								.setImageResource(R.drawable.icon_media_details_saved);
					} else if (cacheState == CacheState.CACHING
							|| cacheState == CacheState.QUEUED) {
						llSaveOffline.setTag(null);
						((LanguageTextView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_text))
								.setText(mContext.getResources().getString(
										R.string.caching_text_saving));
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
						((LanguageTextView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_text))
								.setText(mContext.getResources().getString(
										R.string.caching_text_play_offline));
						((ImageView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_image))
								.setImageResource(R.drawable.icon_media_details_saved);
					} else if (cacheState == CacheState.CACHING
							|| cacheState == CacheState.QUEUED) {
						llSaveOffline.setTag(null);
						((LanguageTextView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_text))
								.setText(mContext.getResources().getString(
										R.string.caching_text_saving));
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
						((LanguageTextView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_text))
								.setText(mContext.getResources().getString(
										R.string.caching_text_play_offline));
						((ImageView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_image))
								.setImageResource(R.drawable.icon_media_details_saved);
					} else if (cacheState == CacheState.CACHING
							|| cacheState == CacheState.QUEUED) {
						llSaveOffline.setTag(null);
						((LanguageTextView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_text))
								.setText(mContext.getResources().getString(
										R.string.caching_text_saving));
						((ImageView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_image))
								.setImageResource(R.drawable.icon_media_details_saving);
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
						((LanguageTextView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_text))
								.setText(mContext.getResources().getString(
										R.string.caching_text_play_offline));
						((ImageView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_image))
								.setImageResource(R.drawable.icon_media_details_saved);
					} else if (cacheState == CacheState.CACHING
							|| cacheState == CacheState.QUEUED) {
						llSaveOffline.setTag(null);
						((LanguageTextView) mediaItemOptionsDialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_text))
								.setText(mContext.getResources().getString(
										R.string.caching_text_saving));
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
			llPlayNow.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (mediaItem.getMediaType() == MediaType.PLAYLIST
							&& mediaItem.getMusicTrackCount() == 0) {
						Utils.makeText(mActivity,

						mActivity.getString(R.string.no_song_available), 0)
								.show();

					}

					if (mOnMediaItemOptionSelectedListener != null) {
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionPlayNowSelected(mediaItem,
										position);

						Map<String, String> reportMap = new HashMap<String, String>();

						reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong
								.toString(), mediaItem.getTitle());
						reportMap.put(mediaItem.getMediaType().toString(),
								Utils.toWhomSongBelongto(mediaItem));
						reportMap.put(
								FlurryConstants.FlurryKeys.Source.toString(),
								FlurryConstants.FlurrySourceDescription.TapOnPlayInContextualMenu
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

			// add to queue.
			llAddtoQueue.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (mediaItem.getMediaType() == MediaType.PLAYLIST
							&& mediaItem.getMusicTrackCount() == 0) {
						Utils.makeText(mActivity,

						mActivity.getString(R.string.no_song_available), 0)
								.show();
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
			llDetails.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View view) {
					if (mOnMediaItemOptionSelectedListener != null) {
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										mediaItem, position);
					}
					mediaItemOptionsDialog.dismiss();
				}
			});

			// Save Offline
			llSaveOffline.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (mOnMediaItemOptionSelectedListener != null
							&& view.getTag() != null) {
						if ((Boolean) view.getTag()) {
							if (mediaItem.getMediaType() == MediaType.TRACK
									|| mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
								// mOnMediaItemOptionSelectedListener
								// .onMediaItemOptionPlayNowSelected(
								// mediaItem, position);
								Utils.makeText(
										mActivity,
										Utils.getMultilanguageText(
												mActivity,
												mActivity
														.getResources()
														.getString(
																R.string.already_offline_message_track)),
										Toast.LENGTH_SHORT).show();
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
								Toast.makeText(
										mActivity,
										Utils.getMultilanguageText(
												mActivity,
												mActivity
														.getResources()
														.getString(
																R.string.already_offline_message_for_tracklist)),
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
			if (mMediaItems == null || mMediaItems.size() == 0) {
				mPlacementMap = new HashMap<Integer, Placement>();
			}
			if (getPlacement(PlacementType.LIVE_RADIO_BANNER) == null) {
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

			mMediaItems = mediaItems;
		} else {
			mMediaItems = new ArrayList<Object>();
			mPlacementMap = new HashMap<Integer, Placement>();
		}
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

	private void showHomeTileHint() {

//		Animation animationIn = AnimationUtils.loadAnimation(mContext,
//				R.anim.slide_and_show_bottom_enter);
//		final Animation animationOut = AnimationUtils.loadAnimation(mContext,
//				R.anim.slide_and_show_bottom_exit);

		// homeTileHint.setVisibility(View.VISIBLE);
		// homeTileHint.startAnimation(animationIn);

//		final CountDownTimer countDownTimer = new CountDownTimer(7000, 1000) {
//
//			public void onTick(long millisUntilFinished) {
//
//			}
//
//			public void onFinish() {
//				cancel();
//				// homeTileHint.startAnimation(animationOut);
//				// homeTileHint.setVisibility(View.GONE);
//			}
//		}.start();

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
	}

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

	// @Override
	// public void notifyDataSetChanged1() {
	// if (/* CacheManager.isProUser(mContext) */true) {
	// try {
	// if (mediaItemOptionsDialog != null
	// && mediaItemOptionsDialog.isShowing()) {
	// CustomCacheStateProgressBar progressCacheState =
	// (CustomCacheStateProgressBar) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_save_offline_progress_cache_state);
	// MediaItem mediaItem = (MediaItem) progressCacheState
	// .getTag(R.id.view_tag_object);
	// if (mediaItem != null) {
	// if (mediaItem.getMediaType() == MediaType.TRACK) {
	// CacheState cacheState;
	// int progress = 0;
	// if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
	// cacheState = DBOHandler
	// .getVideoTrackCacheState(mContext, ""
	// + mediaItem.getId());
	// progress = DBOHandler
	// .getVideoTrackCacheProgress(mContext,
	// "" + mediaItem.getId());
	// } else {
	// cacheState = DBOHandler.getTrackCacheState(
	// mContext, "" + mediaItem.getId());
	// progress = DBOHandler.getTrackCacheProgress(
	// mContext, "" + mediaItem.getId());
	// }
	// LinearLayout llSaveOffline = (LinearLayout) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_save_offline_row);
	// if (cacheState == CacheState.CACHED) {
	// llSaveOffline.setTag(true);
	// ((LanguageTextView) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_save_offline_text))
	// .setText(mContext
	// .getResources()
	// .getString(
	// R.string.caching_text_play_offline));
	// ((ImageView) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_save_offline_image))
	// .setImageResource(R.drawable.icon_media_details_saved);
	// } else if (cacheState == CacheState.CACHING
	// || cacheState == CacheState.QUEUED) {
	// llSaveOffline.setTag(null);
	// ((LanguageTextView) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_save_offline_text))
	// .setText(mContext
	// .getResources()
	// .getString(
	// R.string.caching_text_saving));
	// ((ImageView) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_save_offline_image))
	// .setImageResource(R.drawable.icon_media_details_saving);
	// } else if (cacheState == CacheState.NOT_CACHED) {
	// llSaveOffline.setTag(false);
	// ((LanguageTextView) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_save_offline_text))
	// .setText(mContext
	// .getResources()
	// .getString(
	// R.string.caching_text_save_offline));
	// ((ImageView) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_save_offline_image))
	// .setImageResource(R.drawable.icon_media_details_saveoffline);
	// }
	// progressCacheState.setCacheState(cacheState);
	// progressCacheState.setProgress(progress);
	// } else if (mediaItem.getMediaType() == MediaType.ALBUM) {
	// CacheState cacheState = DBOHandler
	// .getAlbumCacheState(mContext, ""
	// + mediaItem.getId());
	// LinearLayout llSaveOffline = (LinearLayout) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_save_offline_row);
	// if (cacheState == CacheState.CACHED) {
	// int trackCacheCount = DBOHandler
	// .getAlbumCachedCount(mContext, ""
	// + mediaItem.getId());
	// if (trackCacheCount >= mediaItem
	// .getMusicTrackCount())
	// llSaveOffline.setTag(true);
	// ((LanguageTextView) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_save_offline_text))
	// .setText(mContext
	// .getResources()
	// .getString(
	// R.string.caching_text_play_offline));
	// ((ImageView) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_save_offline_image))
	// .setImageResource(R.drawable.icon_media_details_saved);
	// } else if (cacheState == CacheState.CACHING
	// || cacheState == CacheState.QUEUED) {
	// llSaveOffline.setTag(null);
	// ((LanguageTextView) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_save_offline_text))
	// .setText(mContext
	// .getResources()
	// .getString(
	// R.string.caching_text_saving));
	// ((ImageView) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_save_offline_image))
	// .setImageResource(R.drawable.icon_media_details_saving);
	// } else if (cacheState == CacheState.NOT_CACHED) {
	// llSaveOffline.setTag(false);
	// ((LanguageTextView) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_save_offline_text))
	// .setText(mContext
	// .getResources()
	// .getString(
	// R.string.caching_text_save_offline));
	// ((ImageView) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_save_offline_image))
	// .setImageResource(R.drawable.icon_media_details_saveoffline);
	// }
	// progressCacheState.setCacheCountVisibility(true);
	// progressCacheState.setCacheCount(""
	// + DBOHandler.getAlbumCachedCount(mContext,
	// "" + mediaItem.getId()));
	// progressCacheState.setCacheState(cacheState);
	// } else if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
	// CacheState cacheState = DBOHandler
	// .getPlaylistCacheState(mContext, ""
	// + mediaItem.getId());
	// LinearLayout llSaveOffline = (LinearLayout) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_save_offline_row);
	// if (cacheState == CacheState.CACHED) {
	// int trackCacheCount = DBOHandler
	// .getPlaylistCachedCount(mContext, ""
	// + mediaItem.getId());
	// if (trackCacheCount >= mediaItem
	// .getMusicTrackCount())
	// llSaveOffline.setTag(true);
	// ((LanguageTextView) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_save_offline_text))
	// .setText(mContext
	// .getResources()
	// .getString(
	// R.string.caching_text_play_offline));
	// ((ImageView) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_save_offline_image))
	// .setImageResource(R.drawable.icon_media_details_saved);
	// } else if (cacheState == CacheState.CACHING
	// || cacheState == CacheState.QUEUED) {
	// llSaveOffline.setTag(null);
	// ((LanguageTextView) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_save_offline_text))
	// .setText(mContext
	// .getResources()
	// .getString(
	// R.string.caching_text_saving));
	// ((ImageView) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_save_offline_image))
	// .setImageResource(R.drawable.icon_media_details_saving);
	// } else if (cacheState == CacheState.NOT_CACHED) {
	// llSaveOffline.setTag(false);
	// ((LanguageTextView) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_save_offline_text))
	// .setText(mContext
	// .getResources()
	// .getString(
	// R.string.caching_text_save_offline));
	// ((ImageView) mediaItemOptionsDialog
	// .findViewById(R.id.long_click_custom_dialog_save_offline_image))
	// .setImageResource(R.drawable.icon_media_details_saveoffline);
	// }
	// progressCacheState.setCacheCountVisibility(true);
	// progressCacheState.setCacheCount(""
	// + DBOHandler.getPlaylistCachedCount(
	// mContext, "" + mediaItem.getId()));
	// progressCacheState.setCacheState(cacheState);
	// }
	// }
	// }
	// } catch (Exception e) {
	// Logger.printStackTrace(e);
	// }
	// }
	// super.notifyDataSetChanged();
	// }

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

	public void setGridView(RecyclerView gridView){
		this.gridView = gridView;
	}

	private void Checkforpost(int position, int childPosition) {
		try {
			if(position == 0 && childPosition == 0){
				Object obj = mMediaItems.get(position);
				if (obj != null && obj instanceof PromoUnit) {
					PromoUnit promoUnit = (PromoUnit) obj;
					if (!viewedPositions.contains(position + ":"
							+ promoUnit.getPromo_id())) {
						Analytics.postPromoAppEvent(mActivity, promoUnit, "banner_view", "radio");
						viewedPositions.add(position + ":"
								+ promoUnit.getPromo_id());
						return;
					}
				}
			}

			View v = gridView.getChildAt(childPosition);
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
					iv = v.findViewById(R.id.list_radio_line_media_banner_ad);
				} else {
					iv = v.findViewById(R.id.home_videos_tile_image);
				}
				if (iv != null) {
					Placement placement = (Placement) iv
							.getTag(R.string.key_placement);
					Logger.i("postAdForPosition",":::::::::::: childPosition:"+childPosition + ":: position:"+position+" :: placement:"+placement);
					if (placement != null) {
						// System.out.println("CampaignPlayEvent.py request ::: 21 "
						// +(position + ":"
						// + placement.getCampaignID()));
						int adPosition = position;
						if (promoHeight != 0)
							adPosition = position - 1;
						Logger.i("postAdForPosition",":::::::::::: childPosition_viewedPositions:"+viewedPositions.toString());
						Logger.i("postAdForPosition",":::::::::::: childPosition_adPosition:"+adPosition);

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
				// int visibleItems = gridView.getLastVisiblePosition();

				// for (int position = gridView.getFirstVisiblePosition();
				// position <= visibleItems; position++) {
				// int childPosition = position
				// - gridView.getFirstVisiblePosition();
				Logger.i("postAdForPosition",":::::::::::: FirstVisibleItemPos:"+layoutManager
						.findFirstVisibleItemPosition());


				for (int position = layoutManager
						.findFirstVisibleItemPosition(); position <= visibleItems; position++) {
					Logger.i("postAdForPosition",":::::::::::: position:"+position);
					int childPosition = position
							- layoutManager.findFirstVisibleItemPosition();
					Logger.i("postAdForPosition",":::::::::::: childPosition:"+childPosition + ":: position:"+position);
					// mMediaItems.get(position).getImageUrl());
					// System.out.println("CampaignPlayEvent.py request ::: 40 "
					// + position);
					// System.out.println("CampaignPlayEvent.py request ::: 41 "
					// +
					// Utils.getDisplayProfile(HomeActivity.metrics,
					// ((Placement) gridView.getChildAt(childPosition).
					// findViewById(R.id.list_radio_line_media_banner_ad).
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
