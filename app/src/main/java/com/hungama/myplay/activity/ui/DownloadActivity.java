package com.hungama.myplay.activity.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;

/**
 * Controller for presenting details of the given MediaItem.
 */
public class DownloadActivity extends Activity {

	public static final String EXTRA_MEDIA_ITEM = "extra_media_item";

	public static final String ARGUMENT_DOWNLOAD_ACTIVITY = "argument_download_activity";

	private MediaItem mMediaItem;

	// ======================================================
	// Activity life-cycle callbacks.
	// ======================================================

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		// validate calling intent.
		Intent intent = getIntent();
		Bundle data = intent.getExtras();
		if (data != null && data.containsKey(EXTRA_MEDIA_ITEM)) {
			// retrieves the given Media item for the activity.
			mMediaItem = (MediaItem) data.getSerializable(EXTRA_MEDIA_ITEM);
		}

		Intent intentPay = new Intent(this, HungamaPayActivity.class);
		intentPay.putExtra("is_download", true);
		if(mMediaItem!=null) {
			intentPay.putExtra(HungamaPayActivity.EXTRA_LONG_CONTENT_ID, mMediaItem.getId());

			intentPay.putExtra(HungamaPayActivity.EXTRA_LONG_ALBUM_ID, mMediaItem.getAlbumId());
			if ((mMediaItem.getMediaContentType() != null && mMediaItem
					.getMediaContentType() == MediaContentType.VIDEO)
					|| (mMediaItem.getMediaType() != null && mMediaItem
					.getMediaType() == MediaType.VIDEO)) {
				intentPay.putExtra(HungamaPayActivity.EXTRA_TITLE, getString(R.string.general_download_mp4));
			} else {
				intentPay.putExtra(HungamaPayActivity.EXTRA_TITLE, getString(R.string.general_download));
			}
			intentPay.putExtra(HungamaPayActivity.EXTRA_MEDIA_ITEM, mMediaItem);
		}
		startActivity(intentPay);
		finish();
	}
}
