package com.hungama.myplay.activity.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Rect;
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
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;

public class QuickActionFullPlayerMore extends PopupWindows implements
		OnDismissListener, OnItemClickListener {
	private View mRootView;
	private LayoutInflater mInflater;
	private RelativeLayout mScroller;
	AdapterOptions adapter_quality_option;
	private OnDismissListener mDismissListener;
	ListView listview_options;
	// Share
	// similar
	// trivia
	// lyrics
	// info
	// album
	// comment
	private String[] arr_options = new String[] { "Share", "Similar", "Trivia",
			"Lyrics", "Info", "Comments" };

	private int[] arr_icons = new int[] { R.drawable.icon_general_share_grey,
			R.drawable.icon_general_download_grey,
			R.drawable.icon_general_trivia_grey,
			R.drawable.icon_general_lyrics_grey,
			R.drawable.icon_general_download_grey, R.drawable.ic_comments };

	// private Boolean[] arr_status = new Boolean[] { false, false, false,
	// false, false, false,false};
	private ArrayList<Boolean> arr_optionsS = new ArrayList<Boolean>();
	private ArrayList<String> arr_nameoptionsS = new ArrayList<String>();
	private ArrayList<Integer> arr_images = new ArrayList<Integer>();
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

	public void setOnMoreSelectedListener(OnMoreSelectedListener listener) {
		mOnMoreOptionSelectedListener = listener;
	}

	private OnMoreSelectedListener mOnMoreOptionSelectedListener;

	public interface OnMoreSelectedListener {

		public void onMoreItemSelected(String item);

		public void onMoreItemSelectedPosition(int id);

	}

	/**
	 * Constructor for default vertical layout
	 * 
	 * @param context
	 *            Context
	 */
	boolean hasInfo, hasSimilar, hasLyrics, hasTrivia, hasAlbum;

	public QuickActionFullPlayerMore(Context context, boolean hasInfo,
			boolean hasSimilar, boolean hasLyrics, boolean hasTrivia,
			boolean hasAlbum) {
		this(context, VERTICAL, hasInfo, hasSimilar, hasLyrics, hasTrivia,
				hasAlbum);

	}

	/**
	 * Constructor allowing orientation override
	 * 
	 * @param context
	 *            Context
	 * @param orientation
	 *            Layout orientation, can be vartical or horizontal
	 */

	public QuickActionFullPlayerMore(Context context, int orientation,
			boolean hasInfo, boolean hasSimilar, boolean hasLyrics,
			boolean hasTrivia, boolean hasAlbum) {
		super(context);
		this.context = context;
		this.hasInfo = hasInfo;
		this.hasSimilar = hasSimilar;
		this.hasLyrics = hasLyrics;
		this.hasTrivia = hasTrivia;
		this.hasAlbum = hasAlbum;
		mOrientation = orientation;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (mOrientation == HORIZONTAL) {
			setRootViewId(R.layout.popup_horizontal);
		} else {
			setRootViewId(R.layout.popup_vertical_fullplayer_setting);
		}
		arr_optionsS.add(true);
		arr_optionsS.add(hasSimilar);
		arr_optionsS.add(hasTrivia);
		arr_optionsS.add(hasLyrics);
		arr_optionsS.add(false);
		// arr_optionsS.add(hasAlbum);
		arr_optionsS.add(true);

		for (int i = 0; i < arr_optionsS.size(); i++) {
			if (arr_optionsS.get(i).booleanValue()) {
				arr_nameoptionsS.add(arr_options[i]);
				arr_images.add(arr_icons[i]);
			}
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
			xPos = anchorRect.left;
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

			boolean onTop = true;

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
			QuickActionFullPlayerMore.OnDismissListener listener) {
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
	// public abstract void onItemClick(QuickActionFullPlayerMore source,
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

			holder.tv_option_item.setText(Utils.getMultilanguageText(mContext,
					arr_nameoptionsS.get(position)));
			holder.img_option_item.setImageResource(arr_images.get(position));
			convertView
					.setPadding(
							(int) context
									.getResources()
									.getDimension(
											R.dimen.quick_action_without_logo_margin_left_right),
							(int) context
									.getResources()
									.getDimension(
											R.dimen.quick_action_without_logo_margin_top_bottom),
							0,
							(int) context
									.getResources()
									.getDimension(
											R.dimen.quick_action_without_logo_margin_top_bottom));
			holder.img_option_item.setVisibility(View.GONE);
			// if(position == selectedPosition)
			// holder.radio_button.setChecked(true);
			// else
			// holder.radio_button.setChecked(false);

			return convertView;
		}

		@Override
		public int getCount() {
			return arr_nameoptionsS.size();
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

	@Override
	public void onItemClick(AdapterView<?> arg0, final View arg1, int arg2,
			long arg3) {
		Map<String, String> reportMap1 = new HashMap<String, String>();
		reportMap1.put(FlurryConstants.FlurryKeys.SourceSection.toString(),
				FlurryConstants.FlurryKeys.Fullplayer.toString());
		reportMap1.put(FlurryConstants.FlurryKeys.OptionSelected.toString(),
				arr_options[arg2]);
		Analytics.logEvent(
				FlurryConstants.FlurryEventName.ThreeDotsClicked.toString(),
				reportMap1);

		mOnMoreOptionSelectedListener.onMoreItemSelected(arr_nameoptionsS
				.get(arg2));
		mOnMoreOptionSelectedListener.onMoreItemSelectedPosition(arg2);
		dismiss();
	}

	// public boolean isHandledActionOffline(final int action) {
	// ApplicationConfigurations mApplicationConfigurations = new
	// ApplicationConfigurations(
	// mActivity);
	// if (mApplicationConfigurations.getSaveOfflineMode()) {
	// CustomAlertDialog alertBuilder = new CustomAlertDialog(mActivity);
	// alertBuilder.setMessage(Utils.getMultilanguageText(
	// mContext,
	// mContext.getResources().getString(
	// R.string.caching_text_message_go_online_player)));
	// alertBuilder.setPositiveButton(Utils.getMultilanguageText(
	// mContext,
	// mActivity.getResources().getString(
	// R.string.caching_text_popup_title_go_online)),
	// new DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface arg0, int arg1) {
	// if (Utils.isConnected()) {
	// ApplicationConfigurations mApplicationConfigurations = new
	// ApplicationConfigurations(
	// mActivity);
	// mApplicationConfigurations
	// .setSaveOfflineMode(false);
	//
	// Map<String, String> reportMap = new HashMap<String, String>();
	// reportMap.put(
	// FlurryConstants.FlurryCaching.Source
	// .toString(),
	// FlurryConstants.FlurryCaching.Prompt
	// .toString());
	// reportMap
	// .put(FlurryConstants.FlurryCaching.UserStatus
	// .toString(), Utils
	// .getUserState(mContext));
	// FlurryAgent.logEvent(
	// FlurryConstants.FlurryCaching.GoOnline
	// .toString(), reportMap);
	//
	// Intent i = new Intent(
	// MainActivity.ACTION_OFFLINE_MODE_CHANGED);
	// i.putExtra(MainActivity.IS_FROM_PLAYER_BAR,
	// true);
	// i.putExtra(MainActivity.PLAYER_BAR_ACTION,
	// action);
	// mContext.sendBroadcast(i);
	// } else {
	// CustomAlertDialog alertBuilder = new CustomAlertDialog(
	// mContext);
	// alertBuilder.setMessage(Utils
	// .getMultilanguageText(
	// mContext,
	// mContext.getResources()
	// .getString(
	// R.string.go_online_network_error)));
	// alertBuilder.setNegativeButton(Utils
	// .getMultilanguageText(mContext, "OK"),
	// null);
	// alertBuilder.create().show();
	// }
	// }
	// });
	// alertBuilder.setNegativeButton(Utils.getMultilanguageText(
	// mContext,
	// mActivity.getResources().getString(
	// R.string.caching_text_popup_button_cancel)), null);
	// alertBuilder.create().show();
	// return true;
	// } else {
	// return false;
	// }
	// }
}