package com.hungama.myplay.activity.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.StatFs;
import android.text.TextUtils;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.Builder;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;
import com.squareup.picasso.UrlConnectionDownloader;

import java.io.File;

public class PicassoUtil {

	private Picasso picasso;
	private LruCache picassoCache;
	private Context context;
	public static String PICASSO_TAG = "loadImg";
	public static String PICASSO_REMOTE = "loadImg_remote";
	public static String PICASSO_WIDGET = "loadImg_widget";
	public static String PICASSO_WIDGET_SMALL = "loadImg_widget_small";
	public static String PICASSO_NEW_MUSIC_LIST_TAG = PICASSO_TAG;// "loadImg";
	public static String PICASSO_POP_MUSIC_LIST_TAG = PICASSO_TAG;// "loadImg";
	public static String PICASSO_RADIO_LIST_TAG = PICASSO_TAG;// "loadImg";
	public static String PICASSO_VIDEO_LIST_TAG = PICASSO_TAG;// "loadImg";
	public static String PICASSO_BLUR_TAG = "loadImg_blur";

	private static PicassoUtil instance;

	PicassoUtil(Context context) {
		this.context = context;
		// picasso = Picasso.with(context);

		picasso = PicassoBigCache.INSTANCE.getPicassoBigCache(context);

		// Picasso.Builder builder = new Picasso.Builder(context);
		//
		//
		//
		// picassoCache = new LruCache(context);
		// builder.memoryCache(picassoCache);
		// Picasso.setSingletonInstance(builder.build());
		// picasso = new
		// Picasso.Builder(context).memoryCache(picassoCache).build();
	}

	public static PicassoUtil with(Context context) {
		if (instance == null)
			instance = new PicassoUtil(context);
		return instance;
	}

	public static void clearCache () {
		PicassoBigCache.INSTANCE.clearCache();
	}

	public interface PicassoCallBack extends Callback {

	}

	public void load(PicassoCallBack callBack, String imgUrl,
			ImageView imageTile, int defaultResource) {
		try {
			cancelRequest(imageTile);
			if (TextUtils.isEmpty(imgUrl)) {
                if(defaultResource!=-1)
				    imageTile.setImageResource(defaultResource);
                else
                    imageTile.setImageBitmap(null);
				return;
			}
			RequestCreator rb = picasso.load(imgUrl).tag(PICASSO_TAG).noFade()
					.config(Utils.bitmapConfig565);
			if (defaultResource == -1) {
				rb.into(imageTile, callBack);
			} else
				rb.placeholder(defaultResource).into(imageTile, callBack);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void loadWithoutTag(PicassoCallBack callBack, String imgUrl,
			ImageView imageTile, int defaultResource) {
		try {
			cancelRequest(imageTile);
			if (TextUtils.isEmpty(imgUrl)) {
				imageTile.setImageResource(defaultResource);
				return;
			}
			RequestCreator rb = picasso.load(imgUrl).config(
					Utils.bitmapConfig565).noFade();
			if (defaultResource == -1) {
				rb.into(imageTile, callBack);
			} else
				rb.placeholder(defaultResource).into(imageTile, callBack);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void loadWithFit(PicassoCallBack callBack, String imgUrl,
			ImageView imageTile, int defaultResource) {
		cancelRequest(imageTile);
		if (imgUrl == null) {
			imageTile.setImageResource(defaultResource);
			return;
		}
		RequestCreator rb = picasso.load(imgUrl).tag(PICASSO_TAG).noFade()
				.config(Utils.bitmapConfig565);
		if (defaultResource == -1) {
			rb.fit().into(imageTile, callBack);
		} else
			rb.placeholder(defaultResource).fit().into(imageTile, callBack);
	}

	public void loadWithFitWithoutTag(PicassoCallBack callBack, String imgUrl,
			ImageView imageTile, int defaultResource) {
		cancelRequest(imageTile);
		if (imgUrl == null) {
			imageTile.setImageResource(defaultResource);
			return;
		}
		RequestCreator rb = picasso.load(imgUrl).config(Utils.bitmapConfig565).noFade();
		if (defaultResource == -1) {
			rb.fit().into(imageTile, callBack);
		} else
			rb.placeholder(defaultResource).fit().into(imageTile, callBack);
	}

	public void cancelRequest(ImageView imageTile) {
		 picasso.cancelRequest(imageTile);
	}

	public void cancelRequestTarget(PicassoTarget target) {
		// picasso.cancelRequest(target);
	}

	public void resumeTag() {
		picasso.resumeTag(PICASSO_TAG);
	}

	public void pauseTag() {
		picasso.pauseTag(PICASSO_TAG);
	}

	public void resumeTag(String tag) {
		Logger.i("PICASSO", "PICASSO::::::::::::::::resumeTag");
		if (TextUtils.isEmpty(tag))
			resumeTag();
		else
			picasso.resumeTag(tag);
	}

	public void pauseTag(String tag) {
		Logger.i("PICASSO", "PICASSO::::::::::::::::pauseTag");
		if (TextUtils.isEmpty(tag))
			pauseTag();
		else
			picasso.pauseTag(tag);
	}

	public interface PicassoTarget extends Target {

	}

	public void load(String imgUrl, PicassoTarget target) {
		cancelRequestTarget(target);
		picasso.load(imgUrl).tag(PICASSO_TAG).config(Utils.bitmapConfig565).noFade()
				.into(target);

	}

	public void loadWithoutTag(String imgUrl, PicassoTarget target) {
		cancelRequestTarget(target);
		picasso.load(imgUrl).config(Utils.bitmapConfig565).noFade().into(target);

	}

	// public void load(String imgUrl, int width, int height, PicassoTarget
	// target) {
	// picasso.cancelRequest(target);
	// picasso.load(imgUrl).tag(PICASSO_TAG).resize(width, height)
	// .config(Utils.bitmapConfig565).into(target);
	//
	// }
	//
	// public void loadWithoutConfig(String imgUrl, int width, int height,
	// PicassoTarget target) {
	// picasso.cancelRequest(target);
	// if (width == 0) {
	// picasso.load(imgUrl).tag(PICASSO_TAG)
	// .config(Utils.bitmapConfig565).into(target);
	// } else {
	// picasso.load(imgUrl).tag(PICASSO_TAG)
	// .config(Utils.bitmapConfig565).resize(width, height)
	// .into(target);
	// }
	// }

	public void loadWithoutConfig8888(String imgUrl, int width, int height,
			PicassoTarget target) {
		cancelRequestTarget(target);
		if (width == 0) {
			picasso.load(imgUrl).tag(PICASSO_BLUR_TAG).noFade()
					.config(Utils.bitmapConfig8888).into(target);
		} else {
			picasso.load(imgUrl).tag(PICASSO_BLUR_TAG).noFade()
					.config(Utils.bitmapConfig8888).resize(width, height)
					.into(target);
		}
	}

	public enum PicassoBigCache {

		INSTANCE;

		private static final String BIG_CACHE_PATH = "picasso-big-cache";
		private static final int MIN_DISK_CACHE_SIZE = 32 * 1024 * 1024; // 32MB
		private static final int MAX_DISK_CACHE_SIZE = 100 * 1024 * 1024; // 512MB

		private static final float MAX_AVAILABLE_SPACE_USE_FRACTION = 0.9f;
		private static final float MAX_TOTAL_SPACE_USE_FRACTION = 0.25f;

		private Picasso picassoInstance;
		LruCache picassoCache;

		private void init(Context ctx) {
			if (ctx == null) {
				throw new IllegalStateException(
						"Must provide context to init PicassoBigCache!"); // fail
																			// fast
			}
			ctx = ctx.getApplicationContext(); // need application context -
												// activity's context could
												// cause harm
			Builder builder = new Builder(ctx);
			builder.downloader(createBigCacheDownloader(ctx));
			picassoCache = new LruCache(ctx);
			picassoInstance = builder.memoryCache(picassoCache).build();

			// builder.memoryCache(picassoCache);
			// Picasso.setSingletonInstance(builder.build());
			// picasso = new
			// Picasso.Builder(context).memoryCache(picassoCache).build();

		}

		public Picasso getPicassoBigCache(Context ctx) {
			if (picassoInstance == null) {
				synchronized (INSTANCE) {
					if (picassoInstance == null)
						init(ctx);
				}
			}
			return picassoInstance;
		}

		public void clearCache(){
			if(picassoCache!=null) {
				picassoCache.clear();

			}
		}

		static Downloader createBigCacheDownloader(Context ctx) {
			try {
				Class.forName("com.squareup.okhttp.OkHttpClient");
				File cacheDir = createDefaultCacheDir(ctx, BIG_CACHE_PATH);
				long cacheSize = calculateDiskCacheSize(cacheDir);
				OkHttpDownloader downloader = new OkHttpDownloader(cacheDir,
						cacheSize);
				return downloader;
			} catch (ClassNotFoundException e) {
				return new UrlConnectionDownloader(ctx);
			}
		}

		static File createDefaultCacheDir(Context context, String path) {
			File cacheDir = context.getApplicationContext()
					.getExternalCacheDir();
			if (cacheDir == null)
				cacheDir = context.getApplicationContext().getCacheDir();
			File cache = new File(cacheDir, path);
			if (!cache.exists()) {
				cache.mkdirs();
			}
			return cache;
		}

		/**
		 * Calculates bonded min max cache size. Min value is
		 * {@link #MIN_DISK_CACHE_SIZE}
		 * 
		 * @param dir
		 *            cache dir
		 * @return disk space in bytes
		 */

		static long calculateDiskCacheSize(File dir) {
			long size = Math.min(calculateAvailableCacheSize(dir),
					MAX_DISK_CACHE_SIZE);
			return Math.max(size, MIN_DISK_CACHE_SIZE);
		}

		/**
		 * Calculates minimum of available or total fraction of disk space
		 * 
		 * @param dir
		 * @return space in bytes
		 */
		@SuppressLint("NewApi")
		static long calculateAvailableCacheSize(File dir) {
			long size = 0;
			try {
				StatFs statFs = new StatFs(dir.getAbsolutePath());
				int sdkInt = Build.VERSION.SDK_INT;
				long totalBytes;
				long availableBytes;
				if (sdkInt < Build.VERSION_CODES.JELLY_BEAN_MR2) {
					int blockSize = statFs.getBlockSize();
					availableBytes = ((long) statFs.getAvailableBlocks())
							* blockSize;
					totalBytes = ((long) statFs.getBlockCount()) * blockSize;
				} else {
					availableBytes = statFs.getAvailableBytes();
					totalBytes = statFs.getTotalBytes();
				}
				// Target at least 90% of available or 25% of total space
				size = (long) Math.min(availableBytes
						* MAX_AVAILABLE_SPACE_USE_FRACTION, totalBytes
						* MAX_TOTAL_SPACE_USE_FRACTION);
			} catch (IllegalArgumentException ignored) {
				// ignored
			}
			return size;
		}

	}

	public void loadWithFit(PicassoCallBack callBack, String imgUrl,
			ImageView imageTile, int defaultResource, String tag) {
		if (TextUtils.isEmpty(tag)) {
			loadWithFit(callBack, imgUrl, imageTile, defaultResource);
		} else {
			cancelRequest(imageTile);
			if (imgUrl == null) {
				imageTile.setImageResource(defaultResource);
				return;
			}
			RequestCreator rb = picasso.load(imgUrl).tag(tag).noFade()
					.config(Utils.bitmapConfig565);
			if (defaultResource == -1) {
				rb.fit().into(imageTile, callBack);
			} else
				rb.placeholder(defaultResource).fit().into(imageTile, callBack);
		}
	}

	public void load(PicassoCallBack callBack, String imgUrl,
			ImageView imageTile, int defaultResource, String tag) {
		if (TextUtils.isEmpty(tag)) {
			load(callBack, imgUrl, imageTile, defaultResource);
		} else {
			try {
				cancelRequest(imageTile);
				if (TextUtils.isEmpty(imgUrl)) {
					imageTile.setImageResource(defaultResource);
					return;
				}
				RequestCreator rb = picasso.load(imgUrl).tag(tag).noFade()
						.config(Utils.bitmapConfig565);
				if (defaultResource == -1) {
					rb.into(imageTile, callBack);
				} else
					rb.placeholder(defaultResource).into(imageTile, callBack);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void load(String imgUrl, PicassoTarget target, String tag) {
		if (TextUtils.isEmpty(tag))
			load(imgUrl, target);
		else {
			cancelRequestTarget(target);
			picasso.load(imgUrl).tag(tag).config(Utils.bitmapConfig565).noFade()
					.into(target);
		}
	}
}
