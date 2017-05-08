package com.hungama.myplay.activity.util;

import android.widget.Toast;

import com.hungama.myplay.activity.communication.ThreadPoolManager;

public class ToastExpander {

	public static final String TAG = "ToastExpander";

	public static void showFor(final Toast aToast,
			final long durationInMilliseconds) {

		aToast.setDuration(Toast.LENGTH_SHORT);

		ThreadPoolManager.getInstance().submit(new Runnable() {
			long timeElapsed = 0l;

			public void run() {
				try {
					while (timeElapsed <= durationInMilliseconds) {
						long start = System.currentTimeMillis();
						aToast.show();
						Thread.sleep(1750);

 						timeElapsed += System.currentTimeMillis() - start;
					}
				} catch (InterruptedException e) {
					Logger.e(TAG, e.toString());
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			}
		});

	}
}
