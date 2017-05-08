package com.hungama.myplay.activity.ui.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

public class AppTourFragment extends Fragment {

	private static final String TAG = "AppTourFragment";

	private Bitmap mPreviewImage;
	private String mTitle;
	private String mBody;

	private ImageView mImagePreview;
	private LanguageTextView mTextTitle;
	private LanguageTextView mTextBody;

	/**
	 * Create a new instance of TestDetailsFragment, providing "num" as an
	 * argument.
	 */
	public static AppTourFragment newInstance(Bitmap previewImage,
			String title, String body) {

		AppTourFragment appTourFragment = new AppTourFragment();
		// sets argument to the fragment
		appTourFragment.setPreviewImage(previewImage);
		appTourFragment.setTitle(title);
		appTourFragment.setBody(body);

		return appTourFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Analytics.postCrashlitycsLog(getActivity(), AppTourFragment.class.getName());
	}

	private void setPreviewImage(Bitmap previewImage) {
		mPreviewImage = previewImage;
	}

	private void setTitle(String title) {
		mTitle = title;
	}

	private void setBody(String body) {
		mBody = body;
	}

	// ======================================================
	// Life cycle callbacks.
	// ======================================================
	View view;

	@SuppressWarnings("deprecation")
	/**
	 * The Fragment's UI is just a simple text view showing its instance number.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (view == null) {
			view = inflater.inflate(R.layout.fragment_app_tour, container,
					false);
			mImagePreview = (ImageView) view
					.findViewById(R.id.image_view_app_tour);
			int width = getActivity().getWindowManager().getDefaultDisplay()
					.getWidth();
			int margins = Utils.convertDPtoPX(getActivity(), 20);
			int widthMinusMargin = width - margins;
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					widthMinusMargin, widthMinusMargin);
			mImagePreview.setLayoutParams(params);

			mTextTitle = (LanguageTextView) view
					.findViewById(R.id.app_tour_text_title);
			mTextBody = (LanguageTextView) view
					.findViewById(R.id.app_tour_text_body);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				mImagePreview.setBackground(new BitmapDrawable(mPreviewImage));
			} else {
				mImagePreview.setBackgroundDrawable(new BitmapDrawable(
						mPreviewImage));
			}

			mTextTitle.setText(mTitle);
			mTextBody.setText(mBody);
		} else {
			Logger.e("HomeMediaTileGridFragment", "onCreateView else");
			ViewGroup parent = (ViewGroup) view.getParent();
			if (parent!=null){
				parent.removeView(view);
			}
		}

		return view;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			int version = Integer.parseInt(""
					+ android.os.Build.VERSION.SDK_INT);
			Utils.unbindDrawables(view, version);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

}
