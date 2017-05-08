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

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.util.Utils;

import java.util.List;

public class GenreSelectionDialogNew extends DialogFragment implements
		OnItemClickListener {

	private static final String TAG = "GenreSelectionDialogNew";

	GenereSelectionDialogListener listener;
	Context context;

	private List<String> mGenres;

	public static GenreSelectionDialogNew newInstance() {
		GenreSelectionDialogNew f = new GenreSelectionDialogNew();
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
	public GenreSelectionDialogNew() {
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

		((TextView) view.findViewById(R.id.download_custom_dialog_title_text))
				.setText(getString(R.string.discovery_select_genre));

		// getCategoriesAndShow();
		showCategorySelectionDialog();

		getDialog().setOnKeyListener(new Dialog.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface arg0, int keyCode,
					KeyEvent event) {
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					dismiss();
					if (listener != null)
						listener.onGenreEditDialog("");
				}
				return true;
			}
		});

		return view;
	}

	public void setLangData(Context context, List<String> mGenres,
			GenereSelectionDialogListener listener) {
		this.mGenres = mGenres;
		this.listener = listener;
		this.context = context;

	}

	private GenrelistsAdapter mAdapter;

	public void showCategorySelectionDialog() {

		if (mGenres != null && !mGenres.isEmpty()) {
			mAdapter = new GenrelistsAdapter(getActivity());
			listview.setAdapter(mAdapter);
		} else {
			listview.setVisibility(View.GONE);
		}
	}

	public class GenrelistsAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public GenrelistsAdapter(Context context) {

			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mGenres != null ? mGenres.size() : 0;
		}

		@Override
		public String getItem(int position) {
			return mGenres.get(position);
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

			tv.setText(mGenres.get(position));

			return view;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		dismiss();
		listener.onGenreEditDialog(mGenres.get(position));
	}

	public interface GenereSelectionDialogListener {
		void onGenreEditDialog(String genre);
	}
}