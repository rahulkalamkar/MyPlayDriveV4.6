package com.hungama.myplay.activity.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.DataBase.CacheState;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.campaigns.PlacementType;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.ui.DownloadConnectingActivity;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.hungama.myplay.activity.util.QuickActionSearchResult;
import com.hungama.myplay.activity.util.QuickActionSearchResult.OnSearchResultListener;
import com.hungama.myplay.activity.util.Utils;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Adapter that binds list of {@link MediaItem} objects in tiles.
 */
public class PlayerAlbumSimilarAdapter extends
		RecyclerView.Adapter<PlayerAlbumSimilarAdapter.ViewHolder> implements
		OnClickListener, OnSearchResultListener {
	Placement tempPlacement;
	QuickActionSearchResult quickAction;

	private static final String TAG = "MediaListAdapter";
	// public static final String VIEW_TAG_ALBUM = "Album";

	// protected boolean isHomeActivity;
	Vector<String> viewedPositions;
	private Context mContext;
	private Activity mActivity;

	private static Handler h;
	// private Placement placement;
	private boolean mIsAlbum = false;

	private List<MediaItem> mMediaItems;

	// Async image loading members.
	// private ImageFetcher mImageFetcher;

	private OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener;

	// hints
	// private RelativeLayout homeTileHint;

	private String mFlurrySubSectionDescription;
	private LruCache<String, BitmapDrawable> mMemoryCache;

	private HashMap<Integer, Placement> mPlacementMap = new HashMap<Integer, Placement>();
	private DataManager mDataManager;

	// ======================================================
	// ADAPTER'S BASIC FUNCTIONALLITY METHODS.
	// ======================================================

	public PlayerAlbumSimilarAdapter(Activity activity, RecyclerView gridView,
			int tileSize, String fragmentName, MediaCategoryType categoryType,
			MediaContentType contentType, CampaignsManager manager,
			List<MediaItem> mediaItems, boolean mIsAlbum,
			String flurrySubSectionDescription) {

		// isHomeActivity = activity instanceof HomeActivity;
		this.mIsAlbum = mIsAlbum;
		mActivity = activity;
		mContext = mActivity.getApplicationContext();

		mMediaItems = mediaItems;
		mDataManager = DataManager.getInstance(mActivity);
		mFlurrySubSectionDescription = flurrySubSectionDescription;

		final int cacheSize = 2 * 1024 * 1024;
		mMemoryCache = new LruCache<String, BitmapDrawable>(cacheSize) {
			@Override
			protected int sizeOf(String key, BitmapDrawable bitmap) {
				// The cache size will be measured in kilobytes rather than
				// number of items.
				if (bitmap.getBitmap() != null) {
					return bitmap.getBitmap().getRowBytes()
							* bitmap.getBitmap().getHeight() / 1024;
				} else {
					return 0;
				}

			}
		};
		viewedPositions = new Vector<String>();

		text_save_offline = mContext
				.getResources()
				.getString(
						R.string.media_details_custom_dialog_long_click_general_save_offline);
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

	/**
	 * Sets the media items to be presented as tiles in the grid.</br> For
	 * updating the change, call {@code BaseAdapter.notifyDataSetChanged()}/
	 * 
	 * @param mediaItems
	 *            to be presented.
	 */
	public void setMediaItems(List<MediaItem> mediaItems) {
		if (mediaItems != null) {
			if (mMediaItems == null || mMediaItems.size() == 0) {
				// firstPositionPost = true;
				mPlacementMap = new HashMap<Integer, Placement>();
			}
			if (getPlacement() == null) {
				List<MediaItem> temp = new ArrayList<MediaItem>();
				// int position=0;
				for (MediaItem items : mediaItems) {
					if (isAd(items)) {
						temp.add(items);
					}
					// position++;
				}
				mediaItems.removeAll(temp);
				mPlacementMap = new HashMap<Integer, Placement>();
			}

			mMediaItems = mediaItems;
		} else {
			mMediaItems = new ArrayList<MediaItem>();
			mPlacementMap = new HashMap<Integer, Placement>();
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

	public PlayerAlbumSimilarAdapter.ViewHolder onCreateViewHolder(
			ViewGroup parent, int viewType) {
		// create a new view
		View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
				R.layout.list_item_player_album_similar, null);
		// create ViewHolder
		ViewHolder viewHolder = new ViewHolder(itemLayoutView);
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int position) {
		setView(position, viewHolder.itemLayoutView);
	}

	@Override
	public int getItemCount() {
		if (tempPlacement != null)
			return (Utils.isListEmpty(mMediaItems) ? 0
					: (mMediaItems.size() + (mMediaItems.size() / 4)));
		else
			return (Utils.isListEmpty(mMediaItems) ? 0 : mMediaItems.size());
	}

	// @Override
	// public Object getItem(int position) {
	// if (tempPlacement != null)
	// if ((position + 1) % 5 == 0)
	// return null;
	// else
	// return mMediaItems.get(position - (position / 4));
	// return mMediaItems.get(position);
	// }

	@Override
	public long getItemId(int position) {
		return position;
	}

	private static class AlbumSimilarViewHolder {
		ImageView searchResultImage;
		TextView searchResultTopText;
		TextView searchResultTypeAndName;
		RelativeLayout searchResultRow;
		ImageButton player_queue_line_button_more;
		// public CustomCacheStateProgressBar progressCacheState;

	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public View itemLayoutView;
		// public TextView txtViewTitle;
		// public TextView txtViewType;
		// public ImageView imgViewplaylist, imgViewSong, imgViewAlbum;
		ImageView searchResultImage;
		TextView searchResultTopText;
		TextView searchResultTypeAndName;
		RelativeLayout searchResultRow;
		ImageButton player_queue_line_button_more;
		ImageView iv_media_search_result_advertisement;
		LinearLayout llAlbumSimilarItem, llAdView;

		public ViewHolder(View itemLayoutView) {
			super(itemLayoutView);
			this.itemLayoutView = itemLayoutView;

			searchResultRow = (RelativeLayout) itemLayoutView
					.findViewById(R.id.relativelayout_player_queue_line);

			player_queue_line_button_more = (ImageButton) itemLayoutView
					.findViewById(R.id.player_queue_line_button_more);

			searchResultTopText = (TextView) itemLayoutView
					.findViewById(R.id.player_queue_line_top_text);
			searchResultTypeAndName = (TextView) itemLayoutView
					.findViewById(R.id.player_queue_text_media_type_and_name);
			iv_media_search_result_advertisement = (ImageView) itemLayoutView
					.findViewById(R.id.iv_media_search_result_advertisement);
			llAlbumSimilarItem = (LinearLayout) itemLayoutView
					.findViewById(R.id.llAlbumSimilarItem);
			llAdView = (LinearLayout) itemLayoutView
					.findViewById(R.id.llAdView);
			itemLayoutView.setTag(this);
		}
	}

	private Placement getPlacement() {
		Placement placement = null;
		try {
			CampaignsManager mCampaignsManager = CampaignsManager
					.getInstance(mContext);
			if (mIsAlbum) {
				placement = mCampaignsManager
						.getPlacementOfType(PlacementType.PLAYER_ALBUM_BANNER);
			} else {
				placement = mCampaignsManager
						.getPlacementOfType(PlacementType.PLAYER_SIMILAR_BANNER);
			}
			if (placement != null) {
				Logger.i("Placement", "Main search result :: "
						+ new Gson().toJson(placement).toString());
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return placement;
	}

	private int fixedImageHeight = 0;
	private int width;

	private class AdLoader {
		private String backgroundLink = null;
		private BitmapDrawable backgroundImage = null;

		public AdLoader(final ImageView tileImage, final int location,
				final Placement placement) {
			setAdBitmap(tileImage, location, placement);
		}

		private void setAdBitmap(final ImageView tileImage, final int location,
				final Placement placement) {
			try {
				// if (mCampaignsManager == null) {
				// return;
				// }

				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							// tileImages.add(tileImage);
							// getPlacement();
							if (placement != null && mContext != null) {
								try {
									DisplayMetrics metrics = HomeActivity.metrics;
									width = metrics.widthPixels;
									backgroundLink = Utils.getDisplayProfile(
											metrics, placement);
								} catch (Exception e) {
								}
								if (backgroundLink != null) {
									if (mMemoryCache.get(backgroundLink) == null) {

										try {
											backgroundImage = Utils.getBitmap(
													mContext, width,
													backgroundLink);
										} catch (Exception e) {
										}
										// backgroundImage =
										// Utils.ResizeBitmap(dpi,
										// width, backgroundImage);
										backgroundImage = Utils.ResizeBitmap(
												mContext, HomeActivity.metrics,
												backgroundImage);
									} else {
										backgroundImage = mMemoryCache
												.get(backgroundLink);
									}
									// try {
									// Thread.sleep(2000);
									// } catch (InterruptedException e) {
									// e.printStackTrace();
									// }
									h.sendEmptyMessage(0);
								}
							}
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
					}
				}).start();
				h = new Handler() {
					@SuppressWarnings("deprecation")
					public void handleMessage(android.os.Message msg) {
						// if (msg.what < tileImages.size()) {
						if (backgroundImage != null) {
							// main = (ProgressBar) ((View) tileImages.get(
							// msg.what).getParent())
							// .findViewById(R.id.pbMain);
							// main.setVisibility(View.GONE);
							// tileImages.get(msg.what).setBackgroundDrawable(
							// backgroundImage);
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
								tileImage.setBackground(backgroundImage);
							} else {
								tileImage
										.setBackgroundDrawable(backgroundImage);
							}

							// tileImage.setImageDrawable(
							// backgroundImage);
							mMemoryCache.put(backgroundLink, backgroundImage);
							// RelativeLayout rl = (RelativeLayout)
							// tileImage.getParent();
							// rl.getLayoutParams().height =
							// LayoutParams.WRAP_CONTENT;
							tileImage.postDelayed(new Runnable() {
								public void run() {
									try {
										Logger.i(
												"Size!",
												"size 111 : "
														+ backgroundImage
																.getIntrinsicWidth()
														+ " ::::::::: "
														+ backgroundImage
																.getIntrinsicHeight());
										// tileImage.getLayoutParams().width =
										// backgroundImage.getIntrinsicWidth();
										// tileImage.getLayoutParams().height =
										// backgroundImage.getIntrinsicHeight();
										tileImage.getLayoutParams().width = width;

										int fixedImageHeight1 = (int) (((float) width * (float) backgroundImage
												.getIntrinsicHeight()) / (float) backgroundImage
												.getIntrinsicWidth());
										if (fixedImageHeight == 0) {
											fixedImageHeight = fixedImageHeight1;
											// notifyDataSetChanged();
										}
										// tileImage.getLayoutParams().height =
										// fixedImageHeight;
										// tileImage.invalidate();
										// ((LinearLayout)
										// tileImage.getParent())
										// .forceLayout();

										((View) tileImage.getParent()).setTag(
												R.string.key_placement,
												placement);
										// if (adapter != null) {
										// LocalListFragment discovery =
										// (LocalListFragment) adapter
										// .getCurrentFragment(mDeafultOpenedTab);
										// discovery.mSearchResultsAdapter
										// .notifyDataSetChanged();
										// }

									} catch (Exception e) {
										e.printStackTrace();
									}

								}
							}, 200);
							// Utils.postViewEvent(getActivity(),
							// placement);
							if (!viewedPositions.contains(location + ":"
									+ placement.getCampaignID())) {
								Logger.e(TAG, "Post ad view>>" + location);
								Utils.postViewEvent(mContext, placement);
								viewedPositions.add(location + ":"
										+ placement.getCampaignID());
							}
							// if ( && !viewedPositions.contains(location
							// + ":"
							// + placement.getCampaignID())) {
							// Utils.postViewEvent(mContext, placement);
							// viewedPositions.add(location + ":"
							// + placement.getCampaignID());
							// }
						}
						// }
					}
				};
			} catch (Exception e) {
			}
		}
	}

	int originalItemHeight;

	public void setView(int position, View convertView) {

		final AlbumSimilarViewHolder viewHolder;

		Placement placement = null;
		if ((position + 1) % 5 == 0) {
			int location = position + 1;
			placement = mPlacementMap.get(location);
			if (placement == null) {
				// isFromMap = false;
				placement = getPlacement();
			}
			if (location % 5 == 0 && placement != null) {
				mPlacementMap.put(location, placement);
				final LinearLayout llAdView = (LinearLayout) convertView
						.findViewById(R.id.llAdView);
				llAdView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Placement placement = (Placement) v
								.getTag(R.string.key_placement);
						if (placement != null) {
							try {
								Utils.performclickEvent(mContext, placement);
							} catch (Exception e) {
								Logger.printStackTrace(e);
							}
						}
					}
				});
				// convertView.setTag(R.string.key_placement,
				// placement);
				llAdView.setTag(R.string.key_placement, null);
				convertView.findViewById(R.id.llAlbumSimilarItem)
						.setVisibility(View.GONE);
				convertView.findViewById(
						R.id.iv_media_search_result_advertisement)
						.setVisibility(View.VISIBLE);

				DisplayMetrics metrics = HomeActivity.metrics;
				if (metrics == null)
					HomeActivity.resetMatrix(mActivity);
				metrics = HomeActivity.metrics;

				if (metrics != null)
					width = metrics.widthPixels;
				int height = (int) (width * 0.147);

				Logger.i("height", "height::" + height);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, height);
				convertView.setLayoutParams(params);
				// }
				new AdLoader(
						((ImageView) convertView
								.findViewById(R.id.iv_media_search_result_advertisement)),
						location, placement);

				// convertView.setBackgroundResource(R.drawable.background_actionbar);
				return;
			}
		}
		originalItemHeight = convertView.findViewById(R.id.llAlbumSimilarItem)
				.getLayoutParams().height;
		if (originalItemHeight != 0) {
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, originalItemHeight);
			convertView.setLayoutParams(params);
		}
		convertView.findViewById(R.id.llAlbumSimilarItem).setVisibility(
				View.VISIBLE);
		convertView.findViewById(R.id.iv_media_search_result_advertisement)
				.setVisibility(View.GONE);

		viewHolder = new AlbumSimilarViewHolder();
		viewHolder.searchResultRow = (RelativeLayout) convertView
				.findViewById(R.id.relativelayout_player_queue_line);

		viewHolder.player_queue_line_button_more = (ImageButton) convertView
				.findViewById(R.id.player_queue_line_button_more);

		viewHolder.searchResultTopText = (TextView) convertView
				.findViewById(R.id.player_queue_line_top_text);
		viewHolder.searchResultTypeAndName = (TextView) convertView
				.findViewById(R.id.player_queue_text_media_type_and_name);

		// viewHolder.progressCacheState = (CustomCacheStateProgressBar)
		// convertView
		// .findViewById(R.id.search_result_progress_cache_state);

		viewHolder.searchResultRow.setOnClickListener(this);
		// viewHolder.searchResultRow.setOnLongClickListener(this);

		viewHolder.player_queue_line_button_more.setOnClickListener(this);
		// viewHolder.searchResultButtonPlay
		// .setOnLongClickListener(this);

		// viewHolder.searchResultImage = (ImageView)
		// convertView.findViewById(R.id.search_result_media_image);

		convertView.setTag(R.id.view_tag_view_holder, viewHolder);

		// populate the view from the keywords's list.
		if (tempPlacement != null) {
			position = position - (position / 4);
		}
		MediaItem mediaItem = (MediaItem) mMediaItems.get(position);// getItem(position);

		// stores the object in the view.
		convertView.setTag(R.id.view_tag_object, mediaItem);
		try {
			convertView.setTag(R.id.view_tag_position, position);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

		// // gets the image and its size.
		viewHolder.searchResultImage = (ImageView) convertView
				.findViewById(R.id.player_queue_media_image);

		// Set title
		viewHolder.searchResultTopText.setText(mediaItem.getTitle());
		viewHolder.searchResultTypeAndName.setText(mediaItem.getAlbumName());
		// Set Image Type and Text Below title By Type
		if (mediaItem.getMediaType() == MediaType.TRACK) {
			try {
				if (!mIsAlbum) {
					setNotPlaylistResultImage(viewHolder, mediaItem);

				} else
					viewHolder.searchResultImage.setVisibility(View.GONE);

			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}

		// viewHolder.progressCacheState.setVisibility(View.GONE);

	}

	CacheState getCacheState(MediaItem mediaItem) {
		CacheState cacheState = null;
		// Set Image Type and Text Below title By Type
		if (mediaItem.getMediaType() == MediaType.TRACK) {
			cacheState = DBOHandler.getTrackCacheState(
					mContext.getApplicationContext(), "" + mediaItem.getId());

		} else if (mediaItem.getMediaType() == MediaType.ALBUM) {

			cacheState = DBOHandler.getAlbumCacheState(
					mContext.getApplicationContext(), "" + mediaItem.getId());

		} else if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
			cacheState = DBOHandler.getPlaylistCacheState(
					mContext.getApplicationContext(), "" + mediaItem.getId());
		} else if (mediaItem.getMediaType() == MediaType.VIDEO) {
			cacheState = DBOHandler.getVideoTrackCacheState(
					mContext.getApplicationContext(), "" + mediaItem.getId());
		}
		return cacheState;

	}

	// @Override
	// public int getItemViewType(int position) {
	// try {
	// MediaItem mediaItem = (MediaItem) getItem(position);
	// if (mediaItem != null
	// && mediaItem.getMediaType() != MediaType.PLAYLIST) {
	// return IGNORE_ITEM_VIEW_TYPE;
	// }
	// } catch (Exception e) {
	// Logger.printStackTrace(e);
	// }
	// return super.getItemViewType(position);
	// }

	public void setPlaylistResultImage(AlbumSimilarViewHolder viewHolder) {
		try {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				viewHolder.searchResultImage
						.setBackgroundDrawable(mContext
								.getResources()
								.getDrawable(
										R.drawable.background_media_details_playlist_inside_thumb));
			} else {
				viewHolder.searchResultImage
						.setImageResource(R.drawable.background_media_details_playlist_inside_thumb);
			}
		} catch (Error e) {
			System.gc();
			System.runFinalization();
			System.gc();
		}
	}

	public void setNotPlaylistResultImage(AlbumSimilarViewHolder viewHolder,
			MediaItem mediaItem) {

		String imageURL = "";
		String[] images = ImagesManager.getImagesUrlArray(
				mediaItem.getImagesUrlArray(), ImagesManager.MUSIC_ART_SMALL,
				mDataManager.getDisplayDensity());
		if (images != null && images.length > 0) {
			imageURL = images[0];
		}

		try {
			if (imageURL != null && !imageURL.equals("")) {
				PicassoUtil.with(mContext).load(null, imageURL,
						viewHolder.searchResultImage,
						R.drawable.background_home_tile_album_default);
			} else {
				Picasso.with(mContext)
						.load(R.drawable.background_home_tile_album_default)
						.into(viewHolder.searchResultImage);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
			Picasso.with(mContext)
					.load(R.drawable.background_home_tile_album_default)
					.into(viewHolder.searchResultImage);
		} catch (Error e) {
			System.gc();
			System.runFinalization();
			System.gc();
		}
	}

	private String text_save_offline = "";
	private int saveoffline_drawable = R.drawable.icon_media_details_saving;

	@Override
	public void onClick(final View view) {
		Set<String> tags = Utils.getTags();
		if (!tags.contains("search_used")) {
			tags.add("search_used");
			Utils.AddTag(tags);
		}

		final int viewId = view.getId();

		if (viewId == R.id.relativelayout_player_queue_line) {
			// gets the media item from the row.
			try {
				MediaItem mediaItem = (MediaItem) view
						.getTag(R.id.view_tag_object);
				int pos = Integer.parseInt(view.getTag(R.id.view_tag_position)
						.toString());
				if (mediaItem.getTitle().equals("no")) {
					try {
						// Utils.performclickEvent(mContext, placement);
						// Intent browserIntent = new
						// Intent(Intent.ACTION_VIEW,
						// Uri.parse(placement.getActions().get(0).action));
						// startActivity(browserIntent);
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
					return;
				}

				if (mOnMediaItemOptionSelectedListener != null) {
					if (mediaItem.getMediaType() == MediaType.TRACK) {
						// mOnMediaItemOptionSelectedListener
						// .onMediaItemOptionAddToQueueSelected(mediaItem, pos);
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionPlayNowSelected(mediaItem,
										pos);
					} else
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										mediaItem, pos);
				}

				if (mediaItem.getMediaType() == MediaType.VIDEO) {
					Map<String, String> reportMap = new HashMap<String, String>();

					reportMap.put(FlurryConstants.FlurryKeys.Title.toString(),
							mediaItem.getTitle());
					reportMap
							.put(FlurryConstants.FlurryKeys.SubSection
									.toString(),
									FlurryConstants.FlurrySubSectionDescription.SearchResults
											.toString());

					Analytics.logEvent(
							FlurryConstants.FlurryEventName.VideoSelected
									.toString(), reportMap);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (viewId == R.id.player_queue_line_button_more) {
			View parent_inner = (View) view.getParent();
			View parent = (View) parent_inner.getParent();
			MediaItem mediaItem = (MediaItem) parent
					.getTag(R.id.view_tag_object);
			int position = (Integer) parent.getTag(R.id.view_tag_position);
			// show tile's option was selected.

			CacheState cacheState = getCacheState(mediaItem);
			if (cacheState == CacheState.CACHED) {
				text_save_offline = mContext.getResources().getString(
						R.string.caching_text_play_offline);
				saveoffline_drawable = R.drawable.icon_media_details_saved;
				Logger.e("text_save_offline", text_save_offline);
				// ((ImageView)
				// dialog.findViewById(R.id.long_click_custom_dialog_save_offline_image)).setImageResource(R.drawable.icon_media_details_saved);
			} else if (cacheState == CacheState.CACHING) {
				text_save_offline = mContext.getResources().getString(
						R.string.caching_text_saving);
				saveoffline_drawable = R.drawable.icon_media_details_saving_started;
				Logger.e("text_save_offline caching or queu", text_save_offline);
			} else if (cacheState == CacheState.QUEUED) {
				text_save_offline = mContext.getResources().getString(
						R.string.caching_text_saving);
				saveoffline_drawable = R.drawable.icon_media_details_saving_queue;
				Logger.e("text_save_offline caching or queu", text_save_offline);
			} else {
				saveoffline_drawable = R.drawable.icon_media_details_saving;
				text_save_offline = mContext
						.getResources()
						.getString(
								R.string.media_details_custom_dialog_long_click_general_save_offline);
			}

			Logger.s("mediaitem title:" + mediaItem.getTitle());
			Logger.s("mediaitem Album Name:" + mediaItem.getAlbumName());
			Logger.s("mediaitem Artist name:" + mediaItem.getArtistName());
			if (mediaItem.getMediaType() == MediaType.VIDEO) {
				quickAction = new QuickActionSearchResult(mActivity,
						text_save_offline, saveoffline_drawable, true,
						position, PlayerAlbumSimilarAdapter.this,
						mediaItem.getMediaType(), false);
			} else {
				quickAction = new QuickActionSearchResult(mActivity,
						text_save_offline, saveoffline_drawable, false,
						position, PlayerAlbumSimilarAdapter.this,
						mediaItem.getMediaType(), false);
				quickAction.setMediaItem(mediaItem);
			}
			quickAction.show(view);
			view.setEnabled(false);
			quickAction
					.setOnDismissListener(new QuickActionSearchResult.OnDismissListener() {
						@Override
						public void onDismiss() {
							view.setEnabled(true);
						}
					});
		}

	}

	@Override
	public void onItemSelected(String item) {
		// TODO Auto-generated method stub
		Map<String, String> reportMap1 = new HashMap<String, String>();
		reportMap1.put(FlurryConstants.FlurryKeys.SourceSection.toString(),
				mFlurrySubSectionDescription);
		reportMap1.put(FlurryConstants.FlurryKeys.OptionSelected.toString(),
				item);
		Analytics.logEvent(
				FlurryConstants.FlurryEventName.ThreeDotsClicked.toString(),
				reportMap1);
	}

	@Override
	public void onItemSelectedPosition(int id, int pos, boolean isVideo,String item) {
		String txtAddToQueue= mActivity.getString(R.string.media_details_custom_dialog_long_click_add_to_queue);
		String txtViewDetail= mActivity.getString(R.string.media_details_custom_dialog_long_click_view_details);
		String txtDownload = mActivity.getString(R.string.general_download);
		MediaItem mediaItem = mMediaItems.get(pos);

		if(item.equals(txtDownload)){
			MediaItem trackMediaItem = new MediaItem(mediaItem.getId(),
					mediaItem.getTitle(), mediaItem.getAlbumName(),
					mediaItem.getArtistName(), mediaItem.getImageUrl(),
					mediaItem.getBigImageUrl(), MediaType.TRACK.toString(), 0,
					mediaItem.getAlbumId());
			Intent intent = new Intent(mContext,
					DownloadConnectingActivity.class);
			intent.putExtra(DownloadConnectingActivity.EXTRA_MEDIA_ITEM,
					(Serializable) trackMediaItem);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mActivity.startActivity(intent);


			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong.toString(),
					mediaItem.getTitle());
			reportMap.put(FlurryConstants.FlurryKeys.SourceSection.toString(),
					mFlurrySubSectionDescription);
			Analytics.logEvent(
					FlurryConstants.FlurryEventName.Download.toString(),
					reportMap);
		}else if(item.equals(txtViewDetail)){
			if (mOnMediaItemOptionSelectedListener != null) {
				mOnMediaItemOptionSelectedListener
						.onMediaItemOptionShowDetailsSelected(mediaItem, pos);
			}
		}else if(item.equals(txtAddToQueue)){
			if (mOnMediaItemOptionSelectedListener != null) {
				mOnMediaItemOptionSelectedListener
						.onMediaItemOptionAddToQueueSelected(mediaItem, pos);

				Map<String, String> reportMap1 = new HashMap<String, String>();

				reportMap1.put(
						FlurryConstants.FlurryKeys.TitleOfTheSong.toString(),
						mediaItem.getTitle());
				reportMap1.put(mediaItem.getMediaType().toString(),
						Utils.toWhomSongBelongto(mediaItem));
				reportMap1
						.put(FlurryConstants.FlurryKeys.Source.toString(),
								FlurryConstants.FlurrySourceDescription.TapOnAddToQueueInContextualMenu
										.toString());
				reportMap1
						.put(FlurryConstants.FlurryKeys.SubSection.toString(),
								FlurryConstants.FlurrySubSectionDescription.SearchResults
										.toString());

				Analytics.logEvent(
						FlurryConstants.FlurryEventName.SongSelectedForPlay
								.toString(), reportMap1);
			}
		}else{
			if (mOnMediaItemOptionSelectedListener != null) {
				mOnMediaItemOptionSelectedListener
						.onMediaItemOptionSaveOfflineSelected(mediaItem, pos);
			}
		}
	}

	// public void refreshData(List<MediaItem> mMediaItems) {
	// // TODO Auto-generated method stub
	// this.mMediaItems = mMediaItems;
	// notifyDataSetChanged();
	// }

}
