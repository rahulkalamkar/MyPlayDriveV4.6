package com.hungama.myplay.activity.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.DataBase.CacheState;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.ui.DownloadConnectingActivity;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.TrendNowActivity;
import com.hungama.myplay.activity.ui.VideoActivity;
import com.hungama.myplay.activity.ui.adapters.MyAdapter;
import com.hungama.myplay.activity.ui.dialogs.PlaylistDialogFragment;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;

public class QuickAction extends PopupWindows implements OnDismissListener,
		OnItemClickListener, CommunicationOperationListener {
	private View mRootView;
	// private ImageView mArrowUp;
	// private ImageView mArrowDown;
	private LayoutInflater mInflater;
	private RelativeLayout mScroller;
	AdapterOptions adapter_quality_option;
	private OnDismissListener mDismissListener;
	ListView listview_options;
	private String[] arr_options;
	private int[] arr_images;
	Context context;

	private List<ActionItem> actionItems = new ArrayList<ActionItem>();

	private boolean mDidAction;

	private int mAnimStyle;
	private int mOrientation;
	private int rootWidth = 0;

	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;

	public static final int ANIM_GROW_FROM_LEFT = 1;
	public static final int ANIM_GROW_FROM_RIGHT = 2;
	public static final int ANIM_GROW_FROM_CENTER = 3;
	public static final int ANIM_REFLECT = 4;
	public static final int ANIM_AUTO = 5;

	boolean showDeleteButton;

	/**
	 * Constructor for default vertical layout
	 * 
	 * @param context
	 *            Context
	 */

	boolean needToShowTrend;

	public QuickAction(
			Context context,
			MediaItem mediaitem,
			int position,
			OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener,
			FragmentActivity mActivity, String mFlurrySubSectionDescription,
			boolean saveOfflineOption, boolean showDeleteButton,
			MyAdapter adapter, boolean showDownloadOption,
			boolean needToShowTrend) {
		this(context, VERTICAL, mediaitem, position,
				mOnMediaItemOptionSelectedListener, mActivity,
				mFlurrySubSectionDescription, saveOfflineOption,
				showDeleteButton, adapter, showDownloadOption, needToShowTrend);
	}
 
	/**
	 * Constructor allowing orientation override
	 * 
	 * @param context
	 *            Context
	 * @param orientation
	 *            Layout orientation, can be vartical or horizontal
	 */
	private OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener;
	private MediaItem mediaItem;
	private int position;
	private FragmentActivity mActivity;
	private String mFlurrySubSectionDescription;
	private String already_offline_message_for_tracklist;
	private String already_offline_message_track;
	private String caching_text_play_offline;
	private boolean saveOfflineOption;
	private String text_save_offline = "";
	private String caching_text_saving;
	private int saveoffline_drawable;
	boolean showDownloadOption;
	String txtTrendThis,txtAddToPlayList;
	int drawableAddToPlayList;
	boolean isPlaylistScreen = false;

	public QuickAction(
			Context context,
			int orientation,
			final MediaItem mediaItem,
			final int position,
			OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener,
			FragmentActivity mActivity, String mFlurrySubSectionDescription,
			boolean saveOfflineOption, boolean showDeleteButton,
			MyAdapter adapter, boolean showDownloadOption,
			boolean needToShowTrend) {
		super(context);
		this.context = context;
		this.showDeleteButton = showDeleteButton;
		this.showDownloadOption = showDownloadOption;
		this.mediaItem = mediaItem;
		this.needToShowTrend = needToShowTrend;
		// this.adapter=adapter;
		this.position = position;
		this.mOnMediaItemOptionSelectedListener = mOnMediaItemOptionSelectedListener;
		this.mActivity = mActivity;
		this.mFlurrySubSectionDescription = mFlurrySubSectionDescription;
		this.saveOfflineOption = saveOfflineOption;
		mOrientation = orientation;

		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		text_save_offline = context
				.getResources()
				.getString(
						R.string.media_details_custom_dialog_long_click_general_save_offline);
		txtTrendThis = context
				.getString(R.string.full_player_setting_menu_Trend_This);
		txtAddToPlayList= context
				.getString(R.string.more_menu_add_to_playlist);
		drawableAddToPlayList = R.drawable.icon_media_details_add_to_playlist_grey;
		if (mOrientation == HORIZONTAL) {
			setRootViewId(R.layout.popup_horizontal);
		} else {
			setRootViewId(R.layout.popup_vertical);
		}
		Logger.s("Quicj Action:" + mediaItem.getTitle());
		mAnimStyle = ANIM_AUTO;
	}

	public QuickAction(
			Context context,
			int orientation,
			final MediaItem mediaItem,
			final int position,
			OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener,
			FragmentActivity mActivity, String mFlurrySubSectionDescription,
			boolean saveOfflineOption, boolean showDeleteButton,
			MyAdapter adapter, boolean showDownloadOption,
			boolean needToShowTrend, boolean isPlaylistScreen) {
		super(context);
		this.isPlaylistScreen = isPlaylistScreen;
		this.context = context;
		this.showDeleteButton = showDeleteButton;
		this.showDownloadOption = showDownloadOption;
		this.mediaItem = mediaItem;
		this.needToShowTrend = needToShowTrend;
		// this.adapter=adapter;
		this.position = position;
		this.mOnMediaItemOptionSelectedListener = mOnMediaItemOptionSelectedListener;
		this.mActivity = mActivity;
		this.mFlurrySubSectionDescription = mFlurrySubSectionDescription;
		this.saveOfflineOption = saveOfflineOption;
		mOrientation = orientation;

		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		text_save_offline = context
				.getResources()
				.getString(
						R.string.media_details_custom_dialog_long_click_general_save_offline);
		txtTrendThis = context
				.getString(R.string.full_player_setting_menu_Trend_This);
		txtAddToPlayList= context
				.getString(R.string.more_menu_add_to_playlist);
		drawableAddToPlayList = R.drawable.icon_media_details_add_to_playlist_grey;
		if (mOrientation == HORIZONTAL) {
			setRootViewId(R.layout.popup_horizontal);
		} else {
			setRootViewId(R.layout.popup_vertical);
		}
		Logger.s("Quicj Action:" + mediaItem.getTitle());
		mAnimStyle = ANIM_AUTO;
	}

	/**
	 * Get action item at an index
	 * 
	 * @param index
	 *            Index of item (position from callback)
	 * 
	 * @return Action Item at the position
	 */
	public ActionItem getActionItem(int index) {
		return actionItems.get(index);
	}

	/**
	 * Set root view.
	 * 
	 * @param id
	 *            Layout resource id
	 */
	String isSaveOfline = null;

	public void setRootViewId(int id) {
		mWindow.dismiss();
		mRootView = (ViewGroup) mInflater.inflate(id, null);
		listview_options = (ListView) mRootView
				.findViewById(R.id.listview_hd_options);
		// mArrowDown = (ImageView) mRootView.findViewById(R.id.arrow_down);

		mScroller = (RelativeLayout) mRootView.findViewById(R.id.scroller);

		// This was previously defined on show() method, moved here to prevent
		// force close that occured
		// when tapping fastly on a view to show quickaction dialog.
		// Thanx to zammbi (github.com/zammbi)
		mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		// if (mediaItem.getMediaType() == MediaType.TRACK
		// && mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
		// params.height = (int) mContext.getResources().getDimension(
		// R.dimen.hd_option_popup_height);
		// params.width = (int) mContext.getResources().getDimension(
		// R.dimen.hd_option_popup_width);
		// } else if (mediaItem.getMediaContentType() == MediaContentType.VIDEO)
		// {
		// params.height = (int) mContext.getResources().getDimension(
		// R.dimen.hd_option_popup_height_for_video);
		// params.width = (int) mContext.getResources().getDimension(
		// R.dimen.hd_option_popup_width);
		// } else {
		// params.height = (int) mContext.getResources().getDimension(
		// R.dimen.hd_option_popup_height_small);
		// params.width = (int) mContext.getResources().getDimension(
		// R.dimen.hd_option_popup_width);
		// }

		setContentView(mRootView);
		saveoffline_drawable = R.drawable.icon_media_details_saving;
		if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
			// arr_options = new String[] { "View Details", text_save_offline };
			if (showDeleteButton) {
				arr_options = new String[] {
						context.getString(R.string.general_download_mp4),
						text_save_offline,
						context.getString(R.string.media_details_custom_dialog_long_click_delete) };
				arr_images = new int[] {
						R.drawable.icon_general_download_grey_mp4_gray,
						saveoffline_drawable,
						R.drawable.icon_general_delete_grey };
			} else {
				arr_options = new String[] {
						context.getString(R.string.general_download_mp4),
						text_save_offline };
				arr_images = new int[] {
						R.drawable.icon_general_download_grey_mp4_gray,
						saveoffline_drawable };
			}

			// llPlayNow.setVisibility(View.GONE);
			// llAddtoQueue.setVisibility(View.GONE);
		}
		// ||

		if (!saveOfflineOption) {// mFragmentName.equals(DiscoveryGalleryFragment.class.getCanonicalName()))
			if (showDeleteButton) {
				arr_options = new String[] {
						context.getString(R.string.media_details_custom_dialog_long_click_play_now),
						context.getString(R.string.media_details_custom_dialog_long_click_add_to_queue),
						context.getString(R.string.media_details_custom_dialog_long_click_view_details),
						context.getString(R.string.media_details_custom_dialog_long_click_delete) };
			} else {
				arr_options = new String[] {
						context.getString(R.string.media_details_custom_dialog_long_click_play_now),
						context.getString(R.string.media_details_custom_dialog_long_click_add_to_queue),
						context.getString(R.string.media_details_custom_dialog_long_click_view_details) };
			}

			// llSaveOffline.setVisibility(View.GONE);
			// hide save offline
		} else {
			isSaveOfline = "no";
			// llSaveOffline.setTag(false);
			// CustomCacheStateProgressBar progressCacheState =
			// (CustomCacheStateProgressBar) mediaItemOptionsDialog
			// .findViewById(R.id.long_click_custom_dialog_save_offline_progress_cache_state);
			// progressCacheState.setNotCachedStateVisibility(true);
			// progressCacheState.setTag(R.id.view_tag_object, mediaItem);

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

			if (mediaItem.getMediaType() == MediaType.TRACK
					&& mediaItem.getMediaContentType() == MediaContentType.MUSIC) {

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
					cacheState = DBOHandler.getVideoTrackCacheState(mContext,
							"" + mediaItem.getId());
					progress = DBOHandler.getVideoTrackCacheProgress(mContext,
							"" + mediaItem.getId());
				} else {
					cacheState = DBOHandler.getTrackCacheState(mContext, ""
							+ mediaItem.getId());
					progress = DBOHandler.getTrackCacheProgress(mContext, ""
							+ mediaItem.getId());
				}
				if (cacheState == CacheState.CACHED) {
					// llSaveOffline.setTag(true);
					isSaveOfline = "yes";
					if (caching_text_play_offline == null)
						caching_text_play_offline = Utils
								.getMultilanguageTextLayOut(
										mContext,
										mContext.getResources()
												.getString(
														R.string.caching_text_play_offline));
					text_save_offline = caching_text_play_offline;
					// ((LanguageTextView) mediaItemOptionsDialog
					// .findViewById(R.id.long_click_custom_dialog_save_offline_text))
					// .setText(caching_text_play_offline);// "Play Offline"

					saveoffline_drawable = R.drawable.icon_media_details_saved;
					// ((ImageView) mediaItemOptionsDialog
					// .findViewById(R.id.long_click_custom_dialog_save_offline_image))
					// .setImageResource(R.drawable.icon_media_details_saved);
				} else if (cacheState == CacheState.CACHING) {
					isSaveOfline = null;
					if (caching_text_saving == null)
						caching_text_saving = Utils.getMultilanguageTextLayOut(
								mContext,
								mContext.getResources().getString(
										R.string.caching_text_saving));
					text_save_offline = caching_text_saving;
					saveoffline_drawable = R.drawable.icon_media_details_saving_started;

				} else if (cacheState == CacheState.QUEUED) {
					isSaveOfline = null;
					if (caching_text_saving == null)
						caching_text_saving = Utils.getMultilanguageTextLayOut(
								mContext,
								mContext.getResources().getString(
										R.string.caching_text_saving));
					text_save_offline = caching_text_saving;
					saveoffline_drawable = R.drawable.icon_media_details_saving_queue;

				}
				if (showDeleteButton) {
					if (showDownloadOption) {
						if(isPlaylistScreen){
							arr_options = new String[] {
									context.getString(R.string.general_download),
									text_save_offline,
									context.getString(R.string.media_details_custom_dialog_long_click_add_to_queue),
									context.getString(R.string.media_details_custom_dialog_long_click_view_details),
									context.getString(R.string.media_details_custom_dialog_long_click_delete) };
							arr_images = new int[] {
									R.drawable.icon_general_download_grey,
									saveoffline_drawable,
									R.drawable.icon_media_details_add_to_queue_grey,
									R.drawable.icon_view_detail,
									R.drawable.icon_general_delete_grey };
						}else{
							arr_options = new String[] {
									txtAddToPlayList,
									context.getString(R.string.general_download),
									text_save_offline,
									context.getString(R.string.media_details_custom_dialog_long_click_add_to_queue),
									context.getString(R.string.media_details_custom_dialog_long_click_view_details),
									context.getString(R.string.media_details_custom_dialog_long_click_delete) };
							arr_images = new int[] {
									drawableAddToPlayList,
									R.drawable.icon_general_download_grey,
									saveoffline_drawable,
									R.drawable.icon_media_details_add_to_queue_grey,
									R.drawable.icon_view_detail,
									R.drawable.icon_general_delete_grey };
						}
					} else {
						arr_options = new String[] {
								txtAddToPlayList,
								text_save_offline,
								context.getString(R.string.media_details_custom_dialog_long_click_add_to_queue),
								context.getString(R.string.media_details_custom_dialog_long_click_view_details),
								context.getString(R.string.media_details_custom_dialog_long_click_delete) };
						arr_images = new int[] {
								drawableAddToPlayList,
								saveoffline_drawable,
								R.drawable.icon_media_details_add_to_queue_grey,
								R.drawable.icon_view_detail,
								R.drawable.icon_general_delete_grey };
					}

				} else {
					if (showDownloadOption) {
						if (needToShowTrend) {
							arr_options = new String[] {
									txtAddToPlayList,
									context.getString(R.string.general_download),
									text_save_offline,
									txtTrendThis,
									context.getString(R.string.media_details_custom_dialog_long_click_add_to_queue),
									context.getString(R.string.media_details_custom_dialog_long_click_view_details) };
							arr_images = new int[] {
									drawableAddToPlayList,
									R.drawable.icon_general_download_grey,
									saveoffline_drawable,
									R.drawable.icon_media_details_trend_grey,
									R.drawable.icon_media_details_add_to_queue_grey,
									R.drawable.icon_view_detail };
						} else {
							arr_options = new String[] {
									txtAddToPlayList,
									context.getString(R.string.general_download),
									text_save_offline,
									context.getString(R.string.media_details_custom_dialog_long_click_add_to_queue),
									context.getString(R.string.media_details_custom_dialog_long_click_view_details) };
							arr_images = new int[] {
									drawableAddToPlayList,
									R.drawable.icon_general_download_grey,
									saveoffline_drawable,
									R.drawable.icon_media_details_add_to_queue_grey,
									R.drawable.icon_view_detail };
						}
					} else {
						if (needToShowTrend) {

							arr_options = new String[] {
									txtAddToPlayList,
									text_save_offline,
									txtTrendThis,
									context.getString(R.string.media_details_custom_dialog_long_click_add_to_queue),
									context.getString(R.string.media_details_custom_dialog_long_click_view_details) };
							arr_images = new int[] {
									drawableAddToPlayList,
									saveoffline_drawable,
									R.drawable.icon_media_details_trend_grey,
									R.drawable.icon_media_details_add_to_queue_grey,
									R.drawable.icon_view_detail };
						} else {
							arr_options = new String[] {txtAddToPlayList,
									text_save_offline,
									context.getString(R.string.media_details_custom_dialog_long_click_add_to_queue),
									context.getString(R.string.media_details_custom_dialog_long_click_view_details) };
							arr_images = new int[] {drawableAddToPlayList,
									saveoffline_drawable,
									R.drawable.icon_media_details_add_to_queue_grey,
									R.drawable.icon_view_detail };
						}
					}

				}

				// progressCacheState.setCacheState(cacheState);
				// progressCacheState.setProgress(progress);
			} else if (mediaItem.getMediaType() == MediaType.ALBUM
					&& mediaItem.getMediaContentType() == MediaContentType.MUSIC) {

				CacheState cacheState = DBOHandler.getAlbumCacheState(mContext,
						"" + mediaItem.getId());
				if (cacheState == CacheState.CACHED) {
					// int trackCacheCount =
					// DBOHandler.getAlbumCachedCount(mContext, ""+
					// mediaItem.getId());
					// if(trackCacheCount>=mediaItem.getMusicTrackCount())
					// llSaveOffline.setTag(true);
					isSaveOfline = "yes";
					if (caching_text_play_offline == null)
						caching_text_play_offline = Utils
								.getMultilanguageTextLayOut(
										mContext,
										mContext.getResources()
												.getString(
														R.string.caching_text_play_offline));
					text_save_offline = caching_text_play_offline;
					// ((LanguageTextView) mediaItemOptionsDialog
					// .findViewById(R.id.long_click_custom_dialog_save_offline_text))
					// .setText(caching_text_play_offline);
					saveoffline_drawable = R.drawable.icon_media_details_saved;
					// ((ImageView) mediaItemOptionsDialog
					// .findViewById(R.id.long_click_custom_dialog_save_offline_image))
					// .setImageResource(R.drawable.icon_media_details_saved);
				} else if (cacheState == CacheState.CACHING) {
					isSaveOfline = null;
					if (caching_text_saving == null)
						caching_text_saving = Utils.getMultilanguageTextLayOut(
								mContext,
								mContext.getResources().getString(
										R.string.caching_text_saving));
					text_save_offline = caching_text_saving;
					saveoffline_drawable = R.drawable.icon_media_details_saving_started;

				} else if (cacheState == CacheState.QUEUED) {
					isSaveOfline = null;
					if (caching_text_saving == null)
						caching_text_saving = Utils.getMultilanguageTextLayOut(
								mContext,
								mContext.getResources().getString(
										R.string.caching_text_saving));
					text_save_offline = caching_text_saving;
					saveoffline_drawable = R.drawable.icon_media_details_saving_queue;

				}
				if (showDeleteButton) {
					arr_options = new String[] {
							txtAddToPlayList,
							text_save_offline,
							context.getString(R.string.media_details_custom_dialog_long_click_add_to_queue),
							context.getString(R.string.media_details_custom_dialog_long_click_view_details),
							context.getString(R.string.media_details_custom_dialog_long_click_delete) };
					arr_images = new int[] { drawableAddToPlayList, saveoffline_drawable,
							R.drawable.icon_media_details_add_to_queue_grey,
							R.drawable.icon_view_detail,
							R.drawable.icon_general_delete_grey };
				} else {
					if (needToShowTrend) {
						arr_options = new String[] {
								txtAddToPlayList,
								text_save_offline,
								txtTrendThis,
								context.getString(R.string.media_details_custom_dialog_long_click_add_to_queue),
								context.getString(R.string.media_details_custom_dialog_long_click_view_details) };
						arr_images = new int[] {
								drawableAddToPlayList,
								saveoffline_drawable,
								R.drawable.icon_media_details_trend_grey,
								R.drawable.icon_media_details_add_to_queue_grey,
								R.drawable.icon_view_detail };
					} else {
						arr_options = new String[] { txtAddToPlayList,
								text_save_offline,
								context.getString(R.string.media_details_custom_dialog_long_click_add_to_queue),
								context.getString(R.string.media_details_custom_dialog_long_click_view_details) };
						arr_images = new int[] { drawableAddToPlayList,
								saveoffline_drawable,
								R.drawable.icon_media_details_add_to_queue_grey,
								R.drawable.icon_view_detail };
					}
				}

				// progressCacheState.setCacheCountVisibility(true);
				// progressCacheState.setCacheCount(""
				// + DBOHandler.getAlbumCachedCount(mContext, ""
				// + mediaItem.getId()));
				// progressCacheState.setCacheState(cacheState);
			} else if (mediaItem.getMediaType() == MediaType.PLAYLIST
					&& mediaItem.getMediaContentType() == MediaContentType.MUSIC) {

				CacheState cacheState = DBOHandler.getPlaylistCacheState(
						mContext, "" + mediaItem.getId());
				if (cacheState == CacheState.CACHED) {
					// int trackCacheCount =
					// DBOHandler.getPlaylistCachedCount(mContext, ""+
					// mediaItem.getId());
					// if(trackCacheCount>=mediaItem.getMusicTrackCount())
					// llSaveOffline.setTag(true);
					isSaveOfline = "yes";
					if (caching_text_play_offline == null)
						caching_text_play_offline = Utils
								.getMultilanguageTextLayOut(
										mContext,
										mContext.getResources()
												.getString(
														R.string.caching_text_play_offline));
					text_save_offline = caching_text_play_offline;
					// ((LanguageTextView) mediaItemOptionsDialog
					// .findViewById(R.id.long_click_custom_dialog_save_offline_text))
					// .setText(caching_text_play_offline);
					saveoffline_drawable = R.drawable.icon_media_details_saved;
					// ((ImageView) mediaItemOptionsDialog
					// .findViewById(R.id.long_click_custom_dialog_save_offline_image))
					// .setImageResource(R.drawable.icon_media_details_saved);
				} else if (cacheState == CacheState.CACHING
						|| cacheState == CacheState.QUEUED) {
					// llSaveOffline.setTag(null);
					isSaveOfline = null;
					if (caching_text_saving == null)
						caching_text_saving = Utils.getMultilanguageTextLayOut(
								mContext,
								mContext.getResources().getString(
										R.string.caching_text_saving));
					text_save_offline = caching_text_saving;
					// ((LanguageTextView) mediaItemOptionsDialog
					// .findViewById(R.id.long_click_custom_dialog_save_offline_text))
					// .setText(caching_text_saving);
					saveoffline_drawable = R.drawable.icon_media_details_saving;
					// ((ImageView) mediaItemOptionsDialog
					// .findViewById(R.id.long_click_custom_dialog_save_offline_image))
					// .setImageResource(R.drawable.icon_media_details_saving);
				}
				if (showDeleteButton) {
					if(isPlaylistScreen){
						arr_options = new String[] {
								text_save_offline,
								context.getString(R.string.media_details_custom_dialog_long_click_add_to_queue),
								context.getString(R.string.media_details_custom_dialog_long_click_view_details),
								context.getString(R.string.media_details_custom_dialog_long_click_delete) };
						arr_images = new int[] {
								saveoffline_drawable,
								R.drawable.icon_media_details_add_to_queue_grey,
								R.drawable.icon_view_detail,
								R.drawable.icon_general_delete_grey };
					}else {
						arr_options = new String[] {
							txtAddToPlayList,
							text_save_offline,
							context.getString(R.string.media_details_custom_dialog_long_click_add_to_queue),
							context.getString(R.string.media_details_custom_dialog_long_click_view_details),
							context.getString(R.string.media_details_custom_dialog_long_click_delete) };
						arr_images = new int[] {
							drawableAddToPlayList,
							saveoffline_drawable,
							R.drawable.icon_media_details_add_to_queue_grey,
							R.drawable.icon_view_detail,
							R.drawable.icon_general_delete_grey };
					}
				} else {
					if (needToShowTrend) {
						arr_options = new String[] {
								txtAddToPlayList,
								text_save_offline,
								txtTrendThis,
								context.getString(R.string.media_details_custom_dialog_long_click_add_to_queue),
								context.getString(R.string.media_details_custom_dialog_long_click_view_details) };
						arr_images = new int[] {
								drawableAddToPlayList,
								saveoffline_drawable,
								R.drawable.icon_media_details_trend_grey,
								R.drawable.icon_media_details_add_to_queue_grey,
								R.drawable.icon_view_detail };
					} else {
						arr_options = new String[] {
								text_save_offline,
								context.getString(R.string.media_details_custom_dialog_long_click_add_to_queue),
								context.getString(R.string.media_details_custom_dialog_long_click_view_details) };
						arr_images = new int[] {
								saveoffline_drawable,
								R.drawable.icon_media_details_add_to_queue_grey,
								R.drawable.icon_view_detail };
					}
				}

				// progressCacheState.setCacheCountVisibility(true);
				// progressCacheState.setCacheCount(""
				// + DBOHandler.getPlaylistCachedCount(mContext, ""
				// + mediaItem.getId()));
				// progressCacheState.setCacheState(cacheState);
			} else if (mediaItem.getMediaType() == MediaType.VIDEO
					|| mediaItem.getMediaContentType() == MediaContentType.VIDEO) {

				CacheState cacheState = DBOHandler.getVideoTrackCacheState(
						mContext, "" + mediaItem.getId());
				if (cacheState == CacheState.CACHED) {
					// llSaveOffline.setTag(true);
					isSaveOfline = "yes";
					if (caching_text_play_offline == null)
						caching_text_play_offline = Utils
								.getMultilanguageTextLayOut(
										mContext,
										mContext.getResources()
												.getString(
														R.string.caching_text_play_offline));
					text_save_offline = caching_text_play_offline;
					// ((LanguageTextView) mediaItemOptionsDialog
					// .findViewById(R.id.long_click_custom_dialog_save_offline_text))
					// .setText(caching_text_play_offline);
					saveoffline_drawable = R.drawable.icon_media_details_saved;
					// ((ImageView) mediaItemOptionsDialog
					// .findViewById(R.id.long_click_custom_dialog_save_offline_image))
					// .setImageResource(R.drawable.icon_media_details_saved);
				} else if (cacheState == CacheState.CACHING) {
					isSaveOfline = null;
					if (caching_text_saving == null)
						caching_text_saving = Utils.getMultilanguageTextLayOut(
								mContext,
								mContext.getResources().getString(
										R.string.caching_text_saving));
					text_save_offline = caching_text_saving;
					saveoffline_drawable = R.drawable.icon_media_details_saving_started;

				} else if (cacheState == CacheState.QUEUED) {
					isSaveOfline = null;
					if (caching_text_saving == null)
						caching_text_saving = Utils.getMultilanguageTextLayOut(
								mContext,
								mContext.getResources().getString(
										R.string.caching_text_saving));
					text_save_offline = caching_text_saving;
					saveoffline_drawable = R.drawable.icon_media_details_saving_queue;

				}
				// arr_options = new String[] { "View Details",
				// text_save_offline };

				if (showDeleteButton) {
					if (showDownloadOption) {
						if (needToShowTrend) {
							arr_options = new String[] {
									context.getString(R.string.general_download_mp4),
									text_save_offline,
									txtTrendThis,
									context.getString(R.string.media_details_custom_dialog_long_click_delete) };
							arr_images = new int[] {
									R.drawable.icon_general_download_grey_mp4_gray,
									saveoffline_drawable,
									R.drawable.icon_media_details_trend_grey,
									R.drawable.icon_general_delete_grey };
						} else {
							arr_options = new String[] {
									context.getString(R.string.general_download_mp4),
									text_save_offline,
									context.getString(R.string.media_details_custom_dialog_long_click_delete) };
							arr_images = new int[] {
									R.drawable.icon_general_download_grey_mp4_gray,
									saveoffline_drawable,
									R.drawable.icon_general_delete_grey };
						}
					} else {
						if (needToShowTrend) {
							arr_options = new String[] {
									text_save_offline,
									txtTrendThis,
									context.getString(R.string.media_details_custom_dialog_long_click_delete) };
							arr_images = new int[] {
									saveoffline_drawable,
									R.drawable.icon_media_details_trend_grey,
									R.drawable.icon_general_delete_grey };

						} else {
							arr_options = new String[] {

									text_save_offline,
									context.getString(R.string.media_details_custom_dialog_long_click_delete) };
							arr_images = new int[] {

							saveoffline_drawable,
									R.drawable.icon_general_delete_grey };
						}
					}
				} else {

					if (showDownloadOption) {
						if (needToShowTrend) {
							arr_options = new String[] {
									context.getString(R.string.general_download_mp4),
									text_save_offline,txtTrendThis
							};
							arr_images = new int[] {
									R.drawable.icon_general_download_grey_mp4_gray,
									saveoffline_drawable,
									R.drawable.icon_media_details_trend_grey };
						} else {
							arr_options = new String[] {
									context.getString(R.string.general_download_mp4),
									text_save_offline };
							arr_images = new int[] {
									R.drawable.icon_general_download_grey_mp4_gray,
									saveoffline_drawable };
						}
					} else {
						if (needToShowTrend) {
							arr_options = new String[] {
									text_save_offline,txtTrendThis };
							arr_images = new int[] {
									saveoffline_drawable, R.drawable.icon_media_details_trend_grey };
						} else {
							arr_options = new String[] { text_save_offline };
							arr_images = new int[] { saveoffline_drawable };

						}
					}

				}

				// progressCacheState.setCacheCountVisibility(true);
				// progressCacheState.setCacheCount(""
				// + DBOHandler.getPlaylistCachedCount(mContext, ""
				// + mediaItem.getId()));
				// progressCacheState.setCacheState(cacheState);
			}
			// arr_options = new String[] { "Play Now", "Add to queue",
			// "Details",
			// text_save_offline };
			// llSaveOffline.setVisibility(View.VISIBLE);

		}

		// if (mIsShowDetailsInOptionsDialogEnabled) {
		// arr_options = new String[] { "Play Now", "Add to queue", "Details",
		// text_save_offline };
		// // llDetails.setVisibility(View.VISIBLE);
		// } else {
		// arr_options = new String[] { "Play Now", "Add to queue",
		// text_save_offline };
		// // llDetails.setVisibility(View.GONE);
		// }

		fillUpList();
	}

	void fillUpList() {
		adapter_quality_option = new AdapterOptions();
		listview_options.setAdapter(adapter_quality_option);
		listview_options.setOnItemClickListener(this);
	}

	/**
	 * Set animation style
	 * 
	 * @param mAnimStyle
	 *            animation style, default is set to ANIM_AUTO
	 */
	public void setAnimStyle(int mAnimStyle) {
		this.mAnimStyle = mAnimStyle;
	}

	/**
	 * Set listener for action item clicked.
	 * 
	 * @param listener
	 *            Listener
	 */

	/**
	 * Show quickaction popup. Popup is automatically positioned, on top or
	 * bottom of anchor view.
	 * 
	 */

	public void show(View anchor) {
		try {
			preShow();

			int xPos = 0, yPos, arrowPos;

			mDidAction = false;

			int[] location = new int[2];

			anchor.getLocationOnScreen(location);

			Rect anchorRect = new Rect(location[0], location[1], location[0]
					+ anchor.getWidth(), location[1] + anchor.getHeight());

			// mRootView.setLayoutParams(new
			// LayoutParams(LayoutParams.WRAP_CONTENT,
			// LayoutParams.WRAP_CONTENT));

			mRootView.measure(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);

			int rootHeight = mRootView.getMeasuredHeight();

			if (rootWidth == 0) {
				rootWidth = mRootView.getMeasuredWidth();
			}

			int screenWidth = mWindowManager.getDefaultDisplay().getWidth();
			int screenHeight = mWindowManager.getDefaultDisplay().getHeight();

			// automatically get X coord of popup (top left)

			// if ((anchorRect.left + rootWidth) > screenWidth) {
			xPos = anchorRect.left - anchor.getWidth();
			// xPos = (xPos < 0) ? 0 : xPos;

			arrowPos = 1100;// anchorRect.centerX() - xPos-100;

			// }
			// else {
			// if (anchor.getWidth() > rootWidth) {
			// xPos = anchorRect.centerX() - (rootWidth / 2);
			// } else {
			// xPos = anchorRect.left;
			// }

			// arrowPos = anchorRect.centerX() - xPos;
			// }

			int dyTop = anchorRect.top;
			int dyBottom = screenHeight - anchorRect.bottom;

			boolean onTop = false;
			if (anchorRect.top > (screenHeight / 2)) {
				onTop = true;
			} else {
				onTop = false;
			}

			if (onTop) {
				if (rootHeight > dyTop) {
					yPos = 0;
					LayoutParams l = mScroller.getLayoutParams();
					l.height = dyTop - anchor.getHeight();
				} else {
					yPos = anchorRect.top - rootHeight;
				}
			} else {
				yPos = anchorRect.bottom + 0;

				if (rootHeight > dyBottom) {
					LayoutParams l = mScroller.getLayoutParams();
					l.height = dyBottom;
				}
			}

			setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);

			mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.printStackTrace(e);
		}
	}

	/**
	 * Set animation style
	 * 
	 * @param screenWidth
	 *            screen width
	 * @param requestedX
	 *            distance from left edge
	 * @param onTop
	 *            flag to indicate where the popup should be displayed. Set TRUE
	 *            if displayed on top of anchor view and vice versa
	 */
	private void setAnimationStyle(int screenWidth, int requestedX,
			boolean onTop) {
		int arrowPos = requestedX;
		switch (mAnimStyle) {
		case ANIM_GROW_FROM_LEFT:
			mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left
					: R.style.Animations_PopDownMenu_Left);
			break;

		case ANIM_GROW_FROM_RIGHT:
			mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right
					: R.style.Animations_PopDownMenu_Right);
			break;

		case ANIM_GROW_FROM_CENTER:
			mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center
					: R.style.Animations_PopDownMenu_Center);
			break;

		case ANIM_REFLECT:
			mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Reflect
					: R.style.Animations_PopDownMenu_Reflect);
			break;

		case ANIM_AUTO:
			if (arrowPos <= screenWidth / 4) {
				mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left
						: R.style.Animations_PopDownMenu_Left);
			} else if (arrowPos > screenWidth / 4
					&& arrowPos < 3 * (screenWidth / 4)) {
				mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center
						: R.style.Animations_PopDownMenu_Center);
			} else {
				mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right
						: R.style.Animations_PopDownMenu_Right);
			}

			break;
		}
	}

	/**
	 * Show arrow
	 * 
	 * @param whichArrow
	 *            arrow type resource id
	 * @param requestedX
	 *            distance from left screen
	 */
	// private void showArrow(int whichArrow, int requestedX) {
	// final View showArrow = (whichArrow == R.id.arrow_up) ? mArrowUp
	// : mArrowDown;
	// final View hideArrow = (whichArrow == R.id.arrow_up) ? mArrowDown
	// : mArrowUp;
	//
	// final int arrowWidth = mArrowUp.getMeasuredWidth();
	//
	// showArrow.setVisibility(View.VISIBLE);
	//
	// ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams)
	// showArrow
	// .getLayoutParams();
	//
	// param.leftMargin = requestedX - arrowWidth / 2;
	//
	// hideArrow.setVisibility(View.INVISIBLE);
	// }

	/**
	 * Set listener for window dismissed. This listener will only be fired if
	 * the quicakction dialog is dismissed by clicking outside the dialog or
	 * clicking on sticky item.
	 */

	public void setOnDismissListener(QuickAction.OnDismissListener listener) {
		setOnDismissListener(this);

		mDismissListener = listener;
	}

	@Override
	public void onDismiss() {
		if (!mDidAction && mDismissListener != null) {
			mDismissListener.onDismiss();
		}
	}


	/**
	 * Listener for item click
	 * 
	 */
	// public interface OnActionItemClickListener {
	// public abstract void onItemClick(QuickAction source, int pos,
	// int actionId);
	// }

	/**
	 * Listener for window dismiss
	 * 
	 */
	public interface OnDismissListener {
		public abstract void onDismiss();
	}

	static class ViewHolder {
		LanguageTextView tv_option_item;
		ImageView img_option_item;
	}

	public class AdapterOptions extends BaseAdapter {

		public AdapterOptions() {
		}

		int selectedPosition = 0;

		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder holder = new ViewHolder();
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.list_item_options, null);

				holder.tv_option_item = (LanguageTextView) convertView
						.findViewById(R.id.tv_option_item);
				holder.img_option_item = (ImageView) convertView
						.findViewById(R.id.img_option_item);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.tv_option_item.setText(Utils.getMultilanguageTextLayOut(
					context, arr_options[position]));

			// holder.tv_option_item.setText(arr_options[position]);
			holder.img_option_item.setImageResource(arr_images[position]);

			// if(position == selectedPosition)
			// holder.radio_button.setChecked(true);
			// else
			// holder.radio_button.setChecked(false);

			return convertView;
		}

		@Override
		public int getCount() {
			if (arr_options != null) {
				Logger.s("Quicj Action:" + arr_options.length);
				return arr_options.length;
			} else
				return 0;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

	}

	// public static final int ACTION_DOWNLOAD = 1012;

	@Override
	public void onItemClick(AdapterView<?> arg0, final View arg1, int arg2,
			long arg3) {
		Logger.s("onItemClick");

		if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(FlurryConstants.FlurryKeys.OptionSelected.toString(),
					arr_options[arg2]);
			Analytics.logEvent(
					FlurryConstants.FlurryEventName.MusicSection3dots
							.toString(), reportMap);
		} else if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(FlurryConstants.FlurryKeys.OptionSelected.toString(),
					arr_options[arg2]);
			Analytics.logEvent(
					FlurryConstants.FlurryEventName.VideosSection3dots
							.toString(), reportMap);
		}

		Map<String, String> reportMap1 = new HashMap<String, String>();
		reportMap1.put(FlurryConstants.FlurryKeys.SourceSection.toString(),
				mFlurrySubSectionDescription);
		reportMap1.put(FlurryConstants.FlurryKeys.OptionSelected.toString(),
				arr_options[arg2]);
		Analytics.logEvent(
				FlurryConstants.FlurryEventName.ThreeDotsClicked.toString(),
				reportMap1);

		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(context);

		if (arr_options[arg2].equals(context
				.getString(R.string.general_download))
				|| arr_options[arg2].equals(context
						.getString(R.string.general_download_mp4))) {
			// download
			if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
				MediaItem trackMediaItem = new MediaItem(mediaItem.getId(),
						mediaItem.getTitle(), mediaItem.getAlbumName(),
						mediaItem.getArtistName(), mediaItem.getImageUrl(),
						mediaItem.getBigImageUrl(), MediaType.TRACK.toString(),
						0, mediaItem.getAlbumId());
				Intent intent = new Intent(mContext,
						DownloadConnectingActivity.class);
				intent.putExtra(DownloadConnectingActivity.EXTRA_MEDIA_ITEM,
						(Serializable) trackMediaItem);
				mActivity.startActivity(intent);
			} else if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
				Intent intent = new Intent(context,
						DownloadConnectingActivity.class);
				intent.putExtra(DownloadConnectingActivity.EXTRA_MEDIA_ITEM,
						(Serializable) mediaItem);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
			dismiss();

			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong.toString(),
					mediaItem.getTitle());
			reportMap.put(FlurryConstants.FlurryKeys.SourceSection.toString(),
					mFlurrySubSectionDescription);
			Analytics.logEvent(
					FlurryConstants.FlurryEventName.Download.toString(),
					reportMap);
		} else if (arr_options[arg2]
				.equals(context
						.getString(R.string.media_details_custom_dialog_long_click_add_to_queue))) {

			if (mediaItem.getMediaType() == MediaType.PLAYLIST
					&& mediaItem.getMusicTrackCount() == 0) {
				Utils.makeText(mActivity,

				mContext.getString(R.string.no_song_available), 0).show();
			}

			if (!Utils.isConnected()
					&& !mApplicationConfigurations.getSaveOfflineMode()) {
				try {
					((MainActivity) mActivity)
							.internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
								@Override
								public void onRetryButtonClicked() {
									arg1.performClick();
								}
							});
					dismiss();
					// mediaItemOptionsDialog.
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

				reportMap.put(
						FlurryConstants.FlurryKeys.TitleOfTheSong.toString(),
						mediaItem.getTitle());
				reportMap.put(mediaItem.getMediaType().toString(),
						Utils.toWhomSongBelongto(mediaItem));
				reportMap
						.put(FlurryConstants.FlurryKeys.Source.toString(),
								FlurryConstants.FlurrySourceDescription.TapOnAddToQueueInContextualMenu
										.toString());
				reportMap.put(FlurryConstants.FlurryKeys.SubSection.toString(),
						mFlurrySubSectionDescription);

				Analytics.logEvent(
						FlurryConstants.FlurryEventName.SongSelectedForPlay
								.toString(), reportMap);

			}
			dismiss();
			// mediaItemOptionsDialog.dismiss();
		} else if (arr_options[arg2]
				.equals(context
						.getString(R.string.media_details_custom_dialog_long_click_view_details))) {

			if (!Utils.isConnected()
					&& !mApplicationConfigurations.getSaveOfflineMode()) {
				try {
					((MainActivity) mActivity)
							.internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
								@Override
								public void onRetryButtonClicked() {
									arg1.performClick();
								}
							});
					dismiss();
					// mediaItemOptionsDialog.dismiss();
					return;
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			}

			if (mOnMediaItemOptionSelectedListener != null) {
				mOnMediaItemOptionSelectedListener
						.onMediaItemOptionShowDetailsSelected(mediaItem,
								position);
			}
			dismiss();
			// mediaItemOptionsDialog.dismiss();
		} else if (arr_options[arg2]
				.equals(context
						.getString(R.string.media_details_custom_dialog_long_click_general_save_offline))) {

			if (!Utils.isConnected()
					&& !mApplicationConfigurations.getSaveOfflineMode()) {
				try {
					((MainActivity) mActivity)
							.internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
								@Override
								public void onRetryButtonClicked() {
									arg1.performClick();
								}
							});
					dismiss();
					// mediaItemOptionsDialog.dismiss();
					return;
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			}

			if (mOnMediaItemOptionSelectedListener != null
					&& isSaveOfline != null) {
				if (isSaveOfline.equals("yes")) {
					if (mediaItem.getMediaType() == MediaType.TRACK
							|| mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
						// mOnMediaItemOptionSelectedListener
						// .onMediaItemOptionPlayNowSelected(
						// mediaItem, position);
						if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
							CacheManager.removeTrackFromCache(mActivity,
									mediaItem.getId(), MediaContentType.MUSIC);
						} else {
							if (already_offline_message_track == null)
								already_offline_message_track = Utils
										.getMultilanguageTextLayOut(
												mContext,
												mContext.getResources()
														.getString(
																R.string.already_offline_message_track));

							Utils.makeText(mActivity,
									already_offline_message_track,
									Toast.LENGTH_SHORT).show();
						}
					} else {

						if (already_offline_message_for_tracklist == null)
							already_offline_message_for_tracklist = Utils
									.getMultilanguageTextLayOut(
											mContext,
											mContext.getResources()
													.getString(
															R.string.already_offline_message_for_tracklist));

						Utils.makeText(mActivity,
								already_offline_message_for_tracklist,
								Toast.LENGTH_SHORT).show();
					}
				} else if (isSaveOfline.equals("no")) {
					mOnMediaItemOptionSelectedListener
							.onMediaItemOptionSaveOfflineSelected(mediaItem,
									position);
				}

			}
			dismiss();
			// mediaItemOptionsDialog.dismiss();
		} else if (arr_options[arg2]
				.equals(context
						.getString(R.string.media_details_custom_dialog_long_click_delete))) {

			if (mOnMediaItemOptionSelectedListener != null) {
				mOnMediaItemOptionSelectedListener
						.onMediaItemOptionRemoveSelected(mediaItem, position);
			}
			dismiss();
		} else if (arr_options[arg2].equals(txtTrendThis)) {
			Intent intent = new Intent(mContext, TrendNowActivity.class);
			Bundle args = new Bundle();
			args.putSerializable(TrendNowActivity.EXTRA_DATA_MEDIA_ITEM,
					(Serializable) mediaItem);

			intent.putExtras(args);
			mActivity.startActivity(intent);
			dismiss();
		}else if (arr_options[arg2].equals(txtAddToPlayList)) {
			openAddToPlaylistDialog();
			dismiss();
		}
	}

	public void openAddToPlaylistDialog(){
		try {
			if (mediaItem.getMediaType() == MediaType.ALBUM
					|| mediaItem.getMediaType() == MediaType.PLAYLIST) {
				DataManager mDataManager = DataManager.getInstance(mContext);
				//addToPlaylistButtonClickActivity(mMediaSetDetails.getTracks());
				// Added by David Svilem 20/11/2012
				//showPlaylistDialog(mMediaSetDetails.getTracks());
				mDataManager.getMediaDetails(mediaItem, null, this);
			} else {
				Track track = new Track(mediaItem.getId(),
						mediaItem.getTitle(), mediaItem
						.getAlbumName(), mediaItem
						.getArtistName(), mediaItem
						.getImageUrl(), mediaItem
						.getBigImageUrl(), mediaItem
						.getImages(), mediaItem
						.getAlbumId());
				List<Track> tracks = new ArrayList<Track>();
				tracks.add(track);

				//addToPlaylistButtonClickActivity(tracks);
				// Added by David Svilem 20/11/2012
				showPlaylistDialog(tracks);
				//
			}

			// Flurry report: Add to Playlist
			/*Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(
					FlurryConstants.FlurryKeys.SourceSection
							.toString(), mFlurrySourceSection);
			reportMap
					.put(FlurryConstants.FlurryMediaDetailActions.ActionTaken
									.toString(),
							FlurryConstants.FlurryMediaDetailActions.AddToPlaylist
									.toString());
			Analytics.logEvent(mFlurryEventName, reportMap);*/
		} catch (Exception e) {
			Logger.e(getClass().getName() + ":1316",
					e.toString());
		}
	}

	private void showPlaylistDialog(List<Track> tracks) {
		FragmentManager fm = ((MainActivity)context).getSupportFragmentManager();
		boolean isFromLoadMenu = false;
		PlaylistDialogFragment editNameDialog = PlaylistDialogFragment
					.newInstance(tracks, isFromLoadMenu, FlurryConstants.FlurryEventName.PlaylistDetail
							.toString());
		editNameDialog.show(fm, "PlaylistDialogFragment");
	}

	@Override
	public void onStart(int operationId) {
		try {
			((MainActivity)mContext).showLoadingDialog(mContext.getResources().getString(R.string.main_player_bar_text_not_playing));
		}catch(Exception e) {
			Utils.makeText(mContext,mContext.getResources().getString(R.string.main_player_bar_text_not_playing),0).show();
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {

			MediaItem mediaItem = (MediaItem) responseObjects
					.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_ITEM);

			if (mediaItem != null) {
				if (mediaItem.getMediaType() == MediaType.ALBUM
						|| mediaItem.getMediaType() == MediaType.PLAYLIST) {
					try {
						MediaSetDetails mMediaSetDetails = (MediaSetDetails) responseObjects
								.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);
						showPlaylistDialog(mMediaSetDetails.getTracks());
					} catch (Exception e) {

					}
				}
			}
		}
		try {
			((MainActivity)mContext).hideLoadingDialogNew();
		}catch(Exception e){
		}
	}

	@Override
	public void onFailure(int operationId, CommunicationManager.ErrorType errorType, String errorMessage) {
		try {
			((MainActivity)mContext).hideLoadingDialogNew();
		}catch(Exception e){
		}
	}

}