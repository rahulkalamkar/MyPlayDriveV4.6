package com.hungama.myplay.activity.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
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


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.ui.DownloadConnectingActivity;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.MediaDetailsActivity;
import com.hungama.myplay.activity.ui.dialogs.PlaylistDialogFragment;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;

public class QuickActionPlayerQueue extends PopupWindows implements
		OnDismissListener, OnItemClickListener,CommunicationOperationListener {
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
	Activity context;

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

	/**
	 * Constructor for default vertical layout
	 * 
	 * @param context
	 *            Context
	 */
	public QuickActionPlayerQueue(Activity context, Track track,
			String text_save_offline, int saveoffline_drawable,
			boolean displaysaveOffline) {
		this(context, VERTICAL, track, text_save_offline, saveoffline_drawable,
				displaysaveOffline);
	}

	/**
	 * Constructor allowing orientation override
	 * 
	 * @param context
	 *            Context
	 * @param orientation
	 *            Layout orientation, can be vartical or horizontal
	 */
	private Track track;
	private String text_save_offline;
	private int saveoffline_drawable;
	private boolean displaysaveOffline;
	String txtTrendThis1,txtAddToPlayList;
	int drawableAddToPlayList;
	public QuickActionPlayerQueue(Activity context, int orientation,
			Track track, String text_save_offline, int saveoffline_drawable,
			boolean displaysaveOffline) {
		super(context);
		this.context = context;
		this.track = track;
		this.text_save_offline = text_save_offline;
		this.saveoffline_drawable = saveoffline_drawable;
		this.displaysaveOffline = displaysaveOffline;
		mOrientation = orientation;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		txtAddToPlayList= context
				.getString(R.string.more_menu_add_to_playlist);
		drawableAddToPlayList = R.drawable.icon_media_details_add_to_playlist_grey;
		if (mOrientation == HORIZONTAL) {
			setRootViewId(R.layout.popup_horizontal);
		} else {
			setRootViewId(R.layout.popup_vertical);
		}

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
		setContentView(mRootView);
		if (displaysaveOffline) {
			arr_options = new String[] {
					txtAddToPlayList,
					context.getString(R.string.general_download),
					text_save_offline,
					context.getString(R.string.media_details_custom_dialog_long_click_view_details) };
			arr_images = new int[] { drawableAddToPlayList, R.drawable.icon_general_download_grey,
					saveoffline_drawable, R.drawable.icon_view_detail };
		} else {
			arr_options = new String[] {txtAddToPlayList,
					context.getString(R.string.general_download),
					context.getString(R.string.media_details_custom_dialog_long_click_view_details) };
			arr_images = new int[] {drawableAddToPlayList, R.drawable.icon_general_download_grey,
					R.drawable.icon_view_detail };
		}
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
				mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center
						: R.style.Animations_PopDownMenu_Center);
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

	public void setOnDismissListener(
			QuickActionPlayerQueue.OnDismissListener listener) {
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
	// public abstract void onItemClick(QuickActionplayerQueue source,
	// int pos, int actionId);
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
			Logger.s("Quicj Action:" + arr_options.length);
			return arr_options.length;
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
		Map<String, String> reportMap1 = new HashMap<String, String>();
		reportMap1.put(FlurryConstants.FlurryKeys.SourceSection.toString(),
				FlurryConstants.FlurryKeys.PlayerQueue.toString());
		reportMap1.put(FlurryConstants.FlurryKeys.OptionSelected.toString(),
				arr_options[arg2]);
		Analytics.logEvent(
				FlurryConstants.FlurryEventName.ThreeDotsClicked.toString(),
				reportMap1);

		Logger.s("onItemClick");
		if (arr_options[arg2].equals(context
				.getString(R.string.general_download))) {
			// download
			MediaItem trackMediaItem = new MediaItem(track.getId(),
					track.getTitle(), track.getAlbumName(),
					track.getArtistName(), track.getImageUrl(),
					track.getBigImageUrl(), MediaType.TRACK.toString(), 0, 0,
					track.getImages(), track.getAlbumId());
			Intent intent = new Intent(mContext,
					DownloadConnectingActivity.class);
			intent.putExtra(DownloadConnectingActivity.EXTRA_MEDIA_ITEM,
					(Serializable) trackMediaItem);
			mContext.startActivity(intent);
			dismiss();
			// if (!isHandledActionOffline(ACTION_DOWNLOAD)) {
			// if (mPlayerService != null) {
			// try {
			// downloadCurrentTrack();
			//
			// Map<String, String> reportMap = new HashMap<String, String>();
			// reportMap.put(FlurryConstants.FlurryKeys.Title
			// .toString(), mPlayerService
			// .getCurrentPlayingTrack().getTitle());
			// FlurryAgent.logEvent(
			// FlurryConstants.FlurryEventName.Download
			// .toString(), reportMap);
			// } catch (Exception e) {
			// }
			// }
			// }

			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong.toString(),
					track.getTitle());
			reportMap.put(FlurryConstants.FlurryKeys.SourceSection.toString(),
					FlurryConstants.FlurryKeys.PlayerQueue.toString());
			Analytics.logEvent(
					FlurryConstants.FlurryEventName.Download.toString(),
					reportMap);

		} else if (arr_options[arg2]
				.equals(context
						.getString(R.string.media_details_custom_dialog_long_click_view_details))) {
			MediaItem mediaItem = new MediaItem(track.getId(),
					track.getTitle(), track.getAlbumName(),
					track.getArtistName(), track.getImageUrl(),
					track.getBigImageUrl(), MediaType.TRACK.toString()
							.toLowerCase(), 0, 0, track.getImages(),
					track.getAlbumId());

			mediaItem.setMediaContentType(MediaContentType.MUSIC);
			mediaItem.setMediaType(MediaType.TRACK);

			Intent intent = new Intent(mContext.getApplicationContext(),
					MediaDetailsActivity.class);
			intent.putExtra(MediaDetailsActivity.EXTRA_MEDIA_ITEM,
					(Serializable) mediaItem);
			intent.putExtra(MediaDetailsActivity.FLURRY_SOURCE_SECTION,
					FlurryConstants.FlurrySourceSection.PlayerQueue.toString());

			mContext.startActivity(intent);
			dismiss();
			// mediaItemOptionsDialog.dismiss();
		} else if (arr_options[arg2]
				.equals(context
						.getString(R.string.media_details_custom_dialog_long_click_general_save_offline))) {
			MediaItem mediaItem = new MediaItem(track.getId(),
					track.getTitle(), track.getAlbumName(),
					track.getArtistName(), track.getImageUrl(),
					track.getBigImageUrl(), MediaType.TRACK.name()
							.toLowerCase(), 0, 0, track.getImages(),
					track.getAlbumId());
			// new MediaCachingTask(getActivity().getApplicationContext(),
			// mediaItem, track).execute();
			CacheManager.saveOfflineAction(context, mediaItem, track);
			// HomeActivity.refreshOfflineState = true;

			Utils.saveOfflineFlurryEvent(mContext,
					FlurryConstants.FlurryCaching.LongPressMenuPlayerQueue
							.toString(), mediaItem);
			dismiss();
		}else if (arr_options[arg2].equals(txtAddToPlayList)) {

			openAddToPlaylistDialog();
			dismiss();
		}
		// if (!mDidAction && mDismissListener != null) {
		// mDismissListener.onDismiss();
		// Logger.s("onItemClick");
		// }
	}

	public void openAddToPlaylistDialog(){
		try {
			MediaItem mediaItem = new MediaItem(track.getId(),
					track.getTitle(), track.getAlbumName(),
					track.getArtistName(), track.getImageUrl(),
					track.getBigImageUrl(), MediaType.TRACK.name()
					.toLowerCase(), 0, 0, track.getImages(),
					track.getAlbumId());
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
		FragmentManager fm = ((ActionBarActivity)context).getSupportFragmentManager();
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