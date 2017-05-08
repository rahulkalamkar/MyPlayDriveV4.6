package com.hungama.myplay.activity.ui.adapters;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;

public class DataNotFoundAdapter extends
		RecyclerView.Adapter<DataNotFoundAdapter.CrimeHolder> {

	class CrimeHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
		private final LanguageTextView text_layout;

		public CrimeHolder(View itemView) {
			super(itemView);

			text_layout = (LanguageTextView) itemView.findViewById(R.id.txt_heading);
		}
	}

	private String mCrime;

	public DataNotFoundAdapter(String mCrime) {
		this.mCrime = mCrime;
	}

	@Override
	public CrimeHolder onCreateViewHolder(ViewGroup parent, int pos) {
		View view = LayoutInflater.from(parent.getContext()).inflate(
				R.layout.text_layout, parent, false);
		return new CrimeHolder(view);
	}

	@Override
	public void onBindViewHolder(CrimeHolder holder, int pos) {
		holder.text_layout.setText(mCrime);
		holder.text_layout.setTypeface(holder.text_layout.getTypeface(), Typeface.BOLD);
	}

	@Override
	public int getItemCount() {
		return 1;
	}
}
