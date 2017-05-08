package com.hungama.myplay.activity.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.MusicCategoriesResponse;
import com.hungama.myplay.activity.ui.PrefrenceDialogListener;
import com.hungama.myplay.activity.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class LanguageSelectedDialog extends DialogFragment implements
		OnItemClickListener {

	private static final String TAG = "LanguageSelectedDialog";

	private List<String> mCategories;
	private MusicCategoriesResponse musicCategoriesResponse;

	PrefrenceDialogListener listener;
	Context context;
	private boolean isItemSelected = false;

	public static LanguageSelectedDialog newInstance() {
		LanguageSelectedDialog f = new LanguageSelectedDialog();
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
		if (!isItemSelected && onDismissListener != null) {
			onDismissListener.onDismiss(dialog);
		}
	}
	public LanguageSelectedDialog() {
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
		Utils.traverseChild(view, getActivity());

		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));

		listview = (ListView) view.findViewById(R.id.list_view);
		listview.setOnItemClickListener(this);

		getCategoriesAndShow();

		getDialog().setOnKeyListener(new Dialog.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface arg0, int keyCode,
								 KeyEvent event) {
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					dismiss();
					if (listener != null)
						listener.onLangaugeEditDialog(null);
				}
				return true;
			}
		});
		isItemSelected = false;
		return view;
	}

	public void setLangData(Context context,
			MusicCategoriesResponse musicCategoriesResponse,// List<Category>
															// mCategories,
			PrefrenceDialogListener listener) {
		this.musicCategoriesResponse = musicCategoriesResponse;
		this.listener = listener;
		this.context = context;
		if (musicCategoriesResponse != null)
			mCategories = musicCategoriesResponse.getCategories();
	}

	protected DataManager mDataManager;

	public void getCategoriesAndShow() {
		mDataManager = DataManager.getInstance(context);
		if (mCategories != null && mCategories.size() > 0) {
			showCategorySelectionDialog();
		} else {
			final String preferencesResponse = mDataManager
					.getApplicationConfigurations()
					.getMusicPreferencesResponse();
			try {
				musicCategoriesResponse = new Gson().fromJson(new JSONObject(
						preferencesResponse).getJSONObject("response")
						.toString(), MusicCategoriesResponse.class);
				mCategories = musicCategoriesResponse.getCategories();
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (mCategories != null) {
				showCategorySelectionDialog();
			}

		}

	}

	private PlaylistsAdapter mAdapter;

	public void showCategorySelectionDialog() {

		if (mCategories != null && !mCategories.isEmpty()) {
			mAdapter = new PlaylistsAdapter(getActivity());
			listview.setAdapter(mAdapter);
		} else {
			listview.setVisibility(View.GONE);
		}

	}

	public class PlaylistsAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public PlaylistsAdapter(Context context) {

			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mCategories != null ? mCategories.size() : 0;
		}

		@Override
		public Object getItem(int position) {
			return mCategories.get(position);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View view = mInflater.inflate(R.layout.itemable_row_item, parent,
					false);

			TextView tv = (TextView) view.findViewById(R.id.row_item_name);

			// Category p = (Category) mCategories.get(position);
			tv.setText(mCategories.get(position));

			return view;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		isItemSelected = true;
		dismiss();
		if (listener != null)
			listener.onLangaugeEditDialog(mCategories.get(position));
	}
}