package com.hungama.myplay.activity.util;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;

public class QuickActionOfflineSongMenu extends PopupWindows implements
		OnDismissListener, OnItemClickListener {
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
	private int pos = 0;

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
	public QuickActionOfflineSongMenu(Activity context,
			OnOfflineItemSelectListener listener, boolean isSong, int pos) {
		this(context, VERTICAL, listener, isSong, pos);
	}

	/**
	 * Constructor allowing orientation override
	 * 
	 * @param context
	 *            Context
	 * @param orientation
	 *            Layout orientation, can be vartical or horizontal
	 */

	OnOfflineItemSelectListener listener;
	private boolean isSong;
	private boolean isOfflineMode = false;

	public QuickActionOfflineSongMenu(Activity context, int orientation,
			OnOfflineItemSelectListener listener, boolean isSong, int pos) {
		super(context);

		this.context = context;
		DataManager mDataManager = DataManager.getInstance(context);
		ApplicationConfigurations mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		isOfflineMode = mApplicationConfigurations.getSaveOfflineMode();
		this.listener = listener;
		mOrientation = orientation;
		this.isSong = isSong;
		this.pos = pos;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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
		if (isSong) {
			arr_options = new String[] {
					context.getString(R.string.general_download),
					context.getString(R.string.media_details_custom_dialog_long_click_add_to_queue),
					context.getString(R.string.media_details_custom_dialog_long_click_view_details) };
			arr_images = new int[] { R.drawable.icon_general_download_grey,
					R.drawable.icon_media_details_add_to_queue_grey,
					R.drawable.icon_view_detail };
		} else {
			arr_options = new String[] { context
					.getString(R.string.general_download_mp4) };
			arr_images = new int[] { R.drawable.icon_general_download_grey_mp4_gray };
		}
		fillUpList();
	}

	void fillUpList() {
		adapter_quality_option = new AdapterOptions();
		listview_options.setAdapter(adapter_quality_option);
		listview_options.setOnItemClickListener(this);
	}

	public interface OnOfflineItemSelectListener {

		public void onItemSelectedPosition(int id, int pos, String item,
				boolean isSong);
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
	 * Set listener for window dismissed. This listener will only be fired if
	 * the quicakction dialog is dismissed by clicking outside the dialog or
	 * clicking on sticky item.
	 */

	public void setOnDismissListener(
			QuickActionOfflineSongMenu.OnDismissListener listener) {
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
	// public abstract void onItemClick(QuickActionSearchResult source,
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
		LinearLayout llDisableView;
		RelativeLayout rlMain;
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
						R.layout.list_item_options_offline, null);

				holder.tv_option_item = (LanguageTextView) convertView
						.findViewById(R.id.tv_option_item);
				holder.img_option_item = (ImageView) convertView
						.findViewById(R.id.img_option_item);
				holder.llDisableView = (LinearLayout) convertView
						.findViewById(R.id.llDisableView);
				holder.rlMain = (RelativeLayout) convertView
						.findViewById(R.id.rlmain);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			String lable = arr_options[position];

			if (isOfflineMode) {
				if (lable.equals(context.getString(R.string.general_download))) {
					holder.llDisableView.setVisibility(View.VISIBLE);
					holder.rlMain.setBackgroundColor(Color.WHITE);
				} else if (lable
						.equals(context
								.getString(R.string.media_details_custom_dialog_long_click_view_details))) {
					holder.rlMain.setBackgroundColor(Color.WHITE);
					holder.llDisableView.setVisibility(View.VISIBLE);
				} else {
					// if (Build.VERSION.SDK_INT <
					// Build.VERSION_CODES.JELLY_BEAN) {
					holder.rlMain
							.setBackgroundResource(R.drawable.background_media_details_long_press_menu_item_selector);

					// } else {
					// holder.rlMain
					// .setbImageResource(R.drawable.background_media_details_playlist_inside_thumb);
					// }

					holder.llDisableView.setVisibility(View.GONE);
				}
			} else {
				holder.rlMain
						.setBackgroundResource(R.drawable.background_media_details_long_press_menu_item_selector);
				holder.llDisableView.setVisibility(View.GONE);
			}
			holder.tv_option_item.setText(Utils.getMultilanguageTextLayOut(
					context, lable));
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
		Logger.s("onItemClick");
		dismiss();
		listener.onItemSelectedPosition(arg2, pos, arr_options[arg2], isSong);
		// if (!mDidAction && mDismissListener != null) {
		// mDismissListener.onDismiss();
		// Logger.s("onItemClick");
		// }
	}

}