package com.hungama.myplay.activity.ui.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.Mood;
import com.hungama.myplay.activity.ui.PrefrenceDialogListener;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class MoodSelectedDialog extends DialogFragment implements
		OnItemClickListener {

	private static final String TAG = "LanguageSelectedDialog";

	PrefrenceDialogListener listener;
	Context context;

	private List<Mood> mMoods = new ArrayList<Mood>();

	public static MoodSelectedDialog newInstance() {
		MoodSelectedDialog f = new MoodSelectedDialog();
		// Supply data input as an argument.
		Bundle args = new Bundle();

		f.setArguments(args);

		return f;
	}

	private DialogInterface.OnDismissListener onDismissListener;

	public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
		this.onDismissListener = onDismissListener;
	}
	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		if (onDismissListener != null) {
			onDismissListener.onDismiss(dialog);
		}
	}

	public MoodSelectedDialog() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE,
				android.support.v7.appcompat.R.style.Theme_AppCompat_Dialog);
	}

	private ListView listview;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_language_dialog,
				container);

		((TextView) view.findViewById(R.id.download_custom_dialog_title_text))
				.setText(getString(R.string.discovery_mood_title));
		Utils.traverseChild(view, getActivity());

		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));

		listview = (ListView) view.findViewById(R.id.list_view);
		listview.setOnItemClickListener(this);

		openMoodDialog();
		return view;
	}

	public void setLangData(Context context, PrefrenceDialogListener listener) {

		this.listener = listener;
		this.context = context;
	}

	public void openMoodDialog() {
		context = getActivity();
		mDataManager = DataManager.getInstance(context);
		fillUpMood();

		if (mMoods != null && !mMoods.isEmpty()) {
			ListAdapter adapter = new MoodAdapter(context, mMoods);
			listview.setAdapter(adapter);
		} else {
			listview.setVisibility(View.GONE);
		}

	}

	private void fillUpMood() {
		if (Utils.isListEmpty(mMoods)) {
			mMoods = mDataManager.getStoredMoods();
		}
		// if (mMoods != null)
		// for (int i = 0; i < mMoods.size(); i++) {
		// Mood temp_main = mMoods.get(i);
		// Logger.e("temp_main.getName()", "" + temp_main.getName());
		// Logger.e("temp_main.getId()", "" + temp_main.getId());
		// if (temp_main.getName().equalsIgnoreCase("Heart Broken")) {
		// Mood temp = new Mood(temp_main.getId(),
		// temp_main.getName(), ""
		// + R.drawable.discovery_large_007, ""
		// + R.drawable.ic_07_moods);
		// mMoods.set(i, temp);
		//
		// } else if (temp_main.getName().equalsIgnoreCase("Sad")) {
		// Mood temp = new Mood(temp_main.getId(),
		// temp_main.getName(), ""
		// + R.drawable.discovery_large_004, ""
		// + R.drawable.ic_04_moods);
		// mMoods.set(i, temp);
		// } else if (temp_main.getName().equalsIgnoreCase("Chilled Out")) {
		// Mood temp = new Mood(temp_main.getId(),
		// temp_main.getName(), ""
		// + R.drawable.discovery_large_003, ""
		// + R.drawable.ic_03_moods);
		// mMoods.set(i, temp);
		// } else if (temp_main.getName().equalsIgnoreCase("Happy")) {
		// Mood temp = new Mood(temp_main.getId(),
		// temp_main.getName(), ""
		// + R.drawable.discovery_large_006, ""
		// + R.drawable.ic_06_moods);
		// mMoods.set(i, temp);
		//
		// } else if (temp_main.getName().equalsIgnoreCase("Ecstatic")) {
		// Mood temp = new Mood(temp_main.getId(),
		// temp_main.getName(), ""
		// + R.drawable.discovery_large_005, ""
		// + R.drawable.ic_05_moods);
		// mMoods.set(i, temp);
		//
		// } else if (temp_main.getName().equalsIgnoreCase("Romantic")) {
		// Mood temp = new Mood(temp_main.getId(),
		// temp_main.getName(), ""
		// + R.drawable.discovery_large_001, ""
		// + R.drawable.ic_01_moods);
		// mMoods.set(i, temp);
		//
		// } else if (temp_main.getName().equalsIgnoreCase("Party")) {
		// Mood temp = new Mood(temp_main.getId(),
		// temp_main.getName(), ""
		// + R.drawable.discovery_large_002, ""
		// + R.drawable.ic_02_moods);
		// mMoods.set(i, temp);
		//
		// }
		// }
	}

	protected DataManager mDataManager;

	static class ViewHolder {
		TextView nameTxVw;
		ImageView img_icon;
	}

	/**
	 * Definition of the list adapter...uses the View Holder pattern to optimize
	 * performance.
	 */
	class MoodAdapter extends ArrayAdapter<Mood> {

		private static final int RESOURCE = R.layout.dialog_list_item_mood;
		private LayoutInflater inflater;

		public MoodAdapter(Context context, List<Mood> objects) {
			super(context, RESOURCE, objects);
			inflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			if (convertView == null) {
				// inflate a new view and setup the view holder for future use
				convertView = inflater.inflate(RESOURCE, null);

				holder = new ViewHolder();
				holder.nameTxVw = (TextView) convertView
						.findViewById(R.id.list_dialog_title_text);
				holder.img_icon = (ImageView) convertView
						.findViewById(R.id.img_icon);
				convertView.setTag(holder);
			} else {
				// view already defined, retrieve view holder
				holder = (ViewHolder) convertView.getTag();
			}

			Mood cat = getItem(position);
			if (cat == null) {
				Logger.e(TAG, "Invalid category for position: " + position);
			}
			holder.nameTxVw.setText(cat.getName());
			// holder.img_icon.setImageResource(Integer.parseInt(cat
			// .getSmallImageUrl()));

			try {
				if (TextUtils.isEmpty(cat.getSmallImageUrl())) {
					holder.img_icon.setImageResource(mDataManager.getMoodIcon(cat
							.getId(), cat.getName(), true));
				} else {
					Drawable drawable = mDataManager.getMoodIcon(cat.getSmallImageUrl());
					if (drawable == null) {
						holder.img_icon.setImageResource(mDataManager.getMoodIcon(cat
								.getId(), cat.getName(), true));
					} else {
						holder.img_icon.setImageDrawable(drawable);
					}
				}
			} catch (Exception e) {
				holder.img_icon.setImageResource(mDataManager.getMoodIcon(cat
						.getId(), cat.getName(), true));
				Logger.printStackTrace(e);
			}

			return convertView;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		dismiss();
		String selectedVal = mMoods.get(position).getName();
		Logger.d(TAG, "chosen " + selectedVal);
		listener.onMoodEditDialog(mMoods.get(position),position);
	}
}