package com.hungama.myplay.activity.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.social.ShareURL;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.SocialBadgeAlertOperation;
import com.hungama.myplay.activity.operations.hungama.SocialGetUrlOperation;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.ui.dialogs.MyProgressDialog;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.hungama.myplay.activity.util.ScreenLockStatus;
import com.hungama.myplay.activity.util.Utils;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TrendNowActivity extends Activity implements
		OnClickListener, CommunicationOperationListener {

	public static final String TAG = "TrendNowActivity";
	private ApplicationConfigurations mApplicationConfigurations;
	private DataManager mDataManager;
	protected Toolbar mToolbar;
	//	private String mFlurrySourceSection;
	MediaItem mediaItem;
	public static final String EXTRA_DATA_MEDIA_ITEM = "extra_data_media_item";
	// public static final String EXTRA_DATA_MEDIA_SET_DETAILS =
	// "extra_data_media_set_details";
//	public static final String EXTRA_DATA_DO_SHOW_TITLE = "extra_data_do_show_title";
	public static final String FLURRY_SOURCE_SECTION = "flurry_source_section";

	// public MediaSetDetails mMediaSetDetails;

	private Activity getActivity() {
		return TrendNowActivity.this;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		TrendNowActivity.this.finish();
		return false;
	}

	RelativeLayout rlMain;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (PlayerService.service == null)
			finish();

		Bundle incomingArgs = getIntent().getExtras();
		mediaItem = (MediaItem) incomingArgs
				.getSerializable(EXTRA_DATA_MEDIA_ITEM);
		// if (incomingArgs.containsKey(EXTRA_DATA_MEDIA_SET_DETAILS)) {
		// mMediaSetDetails = (MediaSetDetails) incomingArgs
		// .getSerializable(EXTRA_DATA_MEDIA_SET_DETAILS);
		// }
		if (mediaItem == null)
			finish();
//		setContentView(R.layout.activity_trend);
//
//		rlMain = (RelativeLayout) findViewById(R.id.rlMain);
//		rlMain.setVisibility(View.INVISIBLE);
//		mApplicationConfigurations = ApplicationConfigurations
//				.getInstance(TrendNowActivity.this);
//		picasso = PicassoUtil.with(this);
//		initializeUserControls();

		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();

//		mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
//		if (mToolbar != null)
//			setSupportActionBar(mToolbar);
//
//		ActionBar mActionBar = getSupportActionBar();
//		mActionBar.setIcon(R.drawable.icon_actionbar_logo);
//
//		mActionBar.setDisplayUseLogoEnabled(true);
//
//		mActionBar.setDisplayShowTitleEnabled(false);
//		mActionBar.setDisplayShowHomeEnabled(true);
//
//		mActionBar.setHomeButtonEnabled(true);
//		mActionBar.setDisplayHomeAsUpEnabled(true);
//
//		Utils.setActionBarTitle(this, mActionBar,
//				getResources().getString(R.string.full_player_setting_menu_Trend_This));

        if(getIntent().getExtras()!=null && getIntent().hasExtra("hashTag")) {
            strTags = getIntent().getStringExtra("hashTag");
        }
        else if(mediaItem.getMediaType() == MediaType.ALBUM) {
			if(!TextUtils.isEmpty(mediaItem.getAlbumName()))
				strTags = mediaItem.getAlbumName().replace(" ", "");
			else if(!TextUtils.isEmpty(mediaItem.getTitle()))
				strTags = mediaItem.getTitle().replace(" ", "");
		}
        else {
			if(!TextUtils.isEmpty(mediaItem.getTitle()))
				strTags = mediaItem.getTitle().replace(" ", "");
			else if(!TextUtils.isEmpty(mediaItem.getAlbumName()))
				strTags = mediaItem.getAlbumName().replace(" ", "");

		}
		mediaTag = strTags;
        strTags = "#" + strTags + " #Hungama";
//        ((TextView) findViewById(R.id.tvArtistName)).setText(strTags);

//		setPoster();
		tweetCompose();
	}

    private String strTags = "", mediaTag = "";
	String imageUrl = null;
	boolean needToLoadFullImg = false;
    File dest = null;

//	private void setPoster() {
//		final ImageView imgPoster = (ImageView) findViewById(R.id.ivTile);
//		final LinearLayout ll_playlist_images = (LinearLayout) findViewById(R.id.ll_playlist_images);
//		final RelativeLayout rlPoster = (RelativeLayout) findViewById(R.id.rlPoster);
//		if (mediaItem.getMediaType() == MediaType.VIDEO || mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
//			String[] images = ImagesManager.getImagesUrlArray(
//					mediaItem.getImagesUrlArray(),
//					ImagesManager.HOME_VIDEO_TILE,
//					DataManager.getDisplayDensityLabel());
//			if (images != null && images.length > 0) {
//				imageUrl = images[0];
//			}
//			needToLoadFullImg = true;
//		} else if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
//			String[] images = ImagesManager.getImagesUrlArray(
//					mediaItem.getImagesUrlArray(),
//					ImagesManager.HOME_MUSIC_TILE,
//					DataManager.getDisplayDensityLabel());
//
//			if (mediaItem.getMusicTrackCount() == 1) {
//				needToLoadFullImg = true;
//				imageUrl = images[0];
//			} else {
//				needToLoadFullImg = false;
//			}
//			// String[] images = ImagesManager.getImagesUrlArray(
//			// mMediaSetDetails.getImagesUrlArray(),
//			// ImagesManager.MUSIC_ART_SMALL,
//			// DataManager.getDisplayDensityLabel());
//			// if (mMediaSetDetails.getNumberOfTracks() == 1) {
//			// needToLoadFullImg = true;
//			// imageUrl = images[0];
//			// } else {
//			// needToLoadFullImg = false;
//			// }
//		} else {
//			imageUrl = ImagesManager.getMusicArtSmallImageUrl(mediaItem
//					.getImagesUrlArray());
//			needToLoadFullImg = true;
//		}
//		Logger.i("updateInfoDetails", "updateInfoDetails imageUrl:" + imageUrl);
//		Logger.i("imageUrl", "imageUrl:" + imageUrl);
//
//		imgPoster.post(new Runnable() {
//
//			@Override
//			public void run() {
//				final int newWidth = imgPoster.getWidth();
//				final int newHeight = /* (int) */(newWidth * 2) / 3;
//				// imgPoster.getLayoutParams().height = newHeight;
//				// imgPoster.getLayoutParams().width = newWidth;
//				rlPoster.getLayoutParams().height = newHeight;
//				rlPoster.getLayoutParams().width = newWidth;
//				if (needToLoadFullImg) {
//					imgPoster.setVisibility(View.VISIBLE);
//					ll_playlist_images.setVisibility(View.GONE);
//					if (mediaItem != null && !TextUtils.isEmpty(imageUrl)) {
//						picasso.loadWithoutTag(imageUrl, new PicassoTarget() {
//
//							@Override
//							public void onPrepareLoad(Drawable arg0) {
//								imgPoster
//										.setImageResource(R.drawable.background_home_tile_album_default);
//							}
//
//							@Override
//							public void onBitmapLoaded(Bitmap bitmap,
//									LoadedFrom arg1) {
//                                String filename = "temp" + imageUrl.substring(imageUrl.lastIndexOf('.'));
//                                File sd = getExternalCacheDir();//Environment.getExternalStorageDirectory();
//                                dest = new File(sd, filename);
//                                try {
//                                    if(!dest.exists())
//                                        dest.createNewFile();
//                                    dest.deleteOnExit();
//                                    FileOutputStream out = new FileOutputStream(dest);
//                                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
//                                    out.flush();
//                                    out.close();
////                                    System.out.println(dest.getAbsolutePath() + " length :::::::>> " + dest.length());
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//
//								int width = bitmap.getWidth();
//								int height = bitmap.getHeight();
//								if (width > 0 && height > 0 && newWidth > 0
//										&& newHeight > 0) {
//									float scaleWidth = ((float) newWidth)
//											/ width;
//									float scaleHeight = ((float) newHeight)
//											/ height;
//									Matrix matrix = new Matrix();
//									if (scaleWidth > scaleHeight)
//										matrix.setScale(scaleWidth, scaleWidth);
//									else
//										matrix.setScale(scaleHeight,
//												scaleHeight);
//									bitmap = Bitmap.createBitmap(bitmap, 0, 0,
//											width, height, matrix, true);
//									bitmap = Bitmap.createBitmap(bitmap, 0, 0,
//											newWidth, newHeight);
//									imgPoster.setImageBitmap(bitmap);
//								} else {
//									imgPoster
//											.setImageResource(R.drawable.background_home_tile_album_default);
//								}
//							}
//
//							@Override
//							public void onBitmapFailed(Drawable arg0) {
//								imgPoster
//										.setImageResource(R.drawable.background_home_tile_album_default);
//							}
//						});
//
//					} else {
//						imgPoster
//								.setImageResource(R.drawable.background_home_tile_album_default);
//					}
//				} else {
//					imgPoster.setVisibility(View.GONE);
//					ll_playlist_images.setVisibility(View.VISIBLE);
//					fillUpPlaylistImage(newWidth);
//				}
//				rlMain.setVisibility(View.VISIBLE);
//			}
//		});
//	}

	private PicassoUtil picasso;

	private void fillUpPlaylistImage(int width) {
		// String[] images = ImagesManager.getImagesUrlArray(
		// mMediaSetDetails.getImagesUrlArray(),
		// ImagesManager.MUSIC_ART_SMALL,
		// DataManager.getDisplayDensityLabel());
		String[] images = ImagesManager.getImagesUrlArray(
				mediaItem.getImagesUrlArray(), ImagesManager.HOME_MUSIC_TILE,
				DataManager.getDisplayDensityLabel());

		int width_height_image = width / 3;

		LinearLayout mainLinearLayout = (LinearLayout) findViewById(R.id.ll_playlist_images);
		// RelativeLayout rl_header = (RelativeLayout)
		// rootView.findViewById(R.id.rl_header);
		// mainLinearLayout.getLayoutParams().width = width;
		// mainLinearLayout.getLayoutParams().height = width_height_image *
		// 2;
		// rl_header.getLayoutParams().height = width_height_image * 2;

		mainLinearLayout.setOrientation(LinearLayout.VERTICAL);

		LinearLayout innerLinearLayout = new LinearLayout(getActivity());
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,
				width_height_image);
		innerLinearLayout.setLayoutParams(params);

		ImageView imageView = new ImageView(getActivity());
		LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
				width_height_image, width_height_image);
		imageView.setLayoutParams(params2);
		imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

		ImageView imageView1 = new ImageView(getActivity());
		imageView1.setLayoutParams(params2);
		imageView1.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

		ImageView imageView2 = new ImageView(getActivity());
		imageView2.setLayoutParams(params2);
		imageView2.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

		innerLinearLayout.addView(imageView);
		innerLinearLayout.addView(imageView1);
		innerLinearLayout.addView(imageView2);

		mainLinearLayout.addView(innerLinearLayout);

		mainLinearLayout
				.setBackgroundResource(R.drawable.background_home_tile_album_default);

		if (images != null && images.length > 0)
			downloadImage(images[0], imageView);
		else
			imageView.setImageBitmap(null);

		if (images != null && images.length > 1)
			downloadImage(images[1], imageView1);
		else
			imageView1.setImageBitmap(null);

		if (images != null && images.length > 2)
			downloadImage(images[2], imageView2);
		else
			imageView2.setImageBitmap(null);

		LinearLayout innerLinearLayout1 = new LinearLayout(getActivity());
		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
				width, width_height_image);
		innerLinearLayout1.setLayoutParams(params1);

		ImageView imageView3 = new ImageView(getActivity());
		imageView3.setLayoutParams(params2);
		imageView3.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

		ImageView imageView4 = new ImageView(getActivity());
		imageView4.setLayoutParams(params2);
		imageView4.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

		ImageView imageView5 = new ImageView(getActivity());
		imageView5.setLayoutParams(params2);
		imageView5.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

		innerLinearLayout1.addView(imageView3);
		innerLinearLayout1.addView(imageView4);
		innerLinearLayout1.addView(imageView5);

		mainLinearLayout.addView(innerLinearLayout1);

		if (images != null && images.length > 3)
			downloadImage(images[3], imageView3);
		else
			imageView3.setImageBitmap(null);

		if (images != null && images.length > 4)
			downloadImage(images[4], imageView4);
		else
			imageView4.setImageBitmap(null);

		if (images != null && images.length > 5)
			downloadImage(images[5], imageView5);
		else
			imageView5.setImageBitmap(null);
	}

	private void downloadImage(String url1, ImageView iv) {
		try {
			if (!TextUtils.isEmpty(url1)) {

				picasso.loadWithFitWithoutTag(null, url1, iv, -1);

			}
		} catch (Exception e) {
			Logger.e(getClass() + ":701", e.toString());
		} catch (Error e) {
			Logger.e(getClass() + ":701", e.toString());
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		Analytics.startSession(this);
	}

	private void initializeUserControls() {
		ImageButton comments_image_twitter = (ImageButton) findViewById(R.id.comments_image_twitter);
		comments_image_twitter.setOnClickListener(this);

        findViewById(R.id.post_button).setOnClickListener(this);
	}

//	public void showBackButtonWithTitle(String title, String subTitle) {
//		ActionBar mActionBar = getSupportActionBar();
//		Utils.setActionBarTitleSubtitle(this, mActionBar, title, subTitle);
//	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.comments_image_twitter:
        case R.id.post_button:
			tweetCompose();
			break;
		default:
			break;
		}
	}

	private void tweetCompose() {//Fabric.with
		mDataManager.getShareUrl("" + mediaItem.getId(), mediaItem.getMediaType().toString()
				.toLowerCase(), this);

//        System.out.println(" :::::::>> " + Uri.fromFile(dest).toString());
//        System.out.println(dest.getAbsolutePath() + " length :::::::>> " + dest.length());
//        TweetComposer.Builder builder = new TweetComposer.Builder(this)
//                .text(strTags);
////                        .text("android-app://com.hungama.myplay.activity/http/www.hungama.com/music/album-gabbar+is+back-songs/5846125/play");
////				.text("This is testing. Please ignore this.");
////        try {
////            builder.url(new URL("https://play.google.com/store/apps/details?id=com.hungama.myplay.activity"));
////        } catch (MalformedURLException e) {
////            e.printStackTrace();
////        }
//        if (dest != null)
//            builder.image(Uri.fromFile(dest));
//        else if(mediaItem.getMediaType()==MediaType.PLAYLIST){
//            LinearLayout ll_playlist_images = (LinearLayout) findViewById(R.id.ll_playlist_images);
//            ll_playlist_images.setDrawingCacheEnabled(true);
//            String filename = "temp.png";
//            File sd = getExternalCacheDir();//Environment.getExternalStorageDirectory();
//            dest = new File(sd, filename);
//            try {
//                Bitmap bitmap = Bitmap.createBitmap(ll_playlist_images.getWidth(), ll_playlist_images.getHeight(),
//                        Utils.bitmapConfig8888);
//                Canvas c = new Canvas(bitmap);
//                ll_playlist_images.draw(c);
//
//                if(!dest.exists())
//                    dest.createNewFile();
//                dest.deleteOnExit();
//                FileOutputStream out = new FileOutputStream(dest);
//                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
//                out.flush();
//                out.close();
//                builder.image(Uri.fromFile(dest));
////                                    System.out.println(dest.getAbsolutePath() + " length :::::::>> " + dest.length());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
////		builder.show();

		// working #TrndThis
//        TweetComposer.Builder builder = new TweetComposer.Builder(this);
//        try {
////            builder.url(new URL("http://apistaging.hungama.com/webservice/hungama/html/sandbox/metadata.html"));
////            builder.url(new URL("http://www.bigmobileapps.com/admin/metadata_generator.php?content_id=6024797&content_title=ABCD2&content_desc=ABCD%202%20description&image_url=http://content.hungama.com/audio%20album/display%20image/100x100%20jpeg/427802942.jpg&type=album"));
//
//            String title = mediaItem.getTitle();
//            String type = mediaItem.getMediaType().toString().toLowerCase();
//            if(mediaItem.getMediaType() == MediaType.ALBUM)
//                title = mediaItem.getAlbumName();
//
//            if(mediaItem.getMediaType() == MediaType.TRACK)
//                type = "song";
//
//            builder.text(strTags);
////            builder.url(new URL("http://www.bigmobileapps.com/admin/metadata_generator.php?content_id=" +
//            builder.url(new URL("http://apistaging.hungama.com/webservice/hungama/html/sandbox/metadata_generator.php?content_id=" +
//                    mediaItem.getId() +
//                    "&content_title=" +
//                    title.replace(" ", "") +
//                    "&content_desc=" +
//                    title.replace(" ", "") + "%20description" +
//                    "&image_url=" +
//                    imageUrl +
//                    "&type=" +
//                    type));
////            builder.url(new URL("http://www.hungama.com/videos/song-bad%20blood/8868674"));
////            builder.url(new URL("http://www.hungama.com/music/song-mera+naam+mary/15012101/play"));
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//
//        Intent intent = builder.createIntent();
//        startActivityForResult(intent, 1001);
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.s(" ::::::::::>>> " + resultCode);// + " :: " + data.getExtras());
		if(requestCode==1001 && resultCode==RESULT_OK){
			gotResult = true;
			mDataManager.checkBadgesAlert("" + mediaItem.getId(), mediaItem.getMediaType()
							.toString().toLowerCase(),
					SocialBadgeAlertOperation.ACTION_SHARE, this);
		}
    }

	MyProgressDialog progressDialog;
	boolean isPosting = false, gotResult = false;

	@Override
	public void onStart(int operationId) {
		progressDialog = new MyProgressDialog(this);
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_GET_URL) {
				ShareURL shareURL = (ShareURL) responseObjects
						.get(SocialGetUrlOperation.RESULT_KEY_GET_SOCIAL_URL);
				if (shareURL != null) {
					Map<String, String> reportMap = new HashMap<String, String>();
					reportMap.put(FlurryConstants.FlurryKeys.Type
							.toString(), mediaItem.getMediaType()
							.toString());
					reportMap.put(FlurryConstants.FlurryKeys.HashTag
							.toString(), mediaTag);
					Analytics
							.logEvent(FlurryConstants.FlurryEventName.TweetThis
									.toString(), reportMap);

					TweetComposer.Builder builder = new TweetComposer.Builder(this);
					try {
						if (strTags.length() > mApplicationConfigurations.getTweetLimit())
							strTags = strTags.substring(0, mApplicationConfigurations.getTweetLimit());
						builder.text(strTags);
						if (shareURL.url.contains("?"))
							shareURL.url = shareURL.url + "&type=mobile";
						else
							shareURL.url = shareURL.url + "?type=mobile";
						builder.url(new URL(shareURL.url));
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}

					Intent intent = builder.createIntent();
					try {
						startActivityForResult(intent, 1001);
					} catch (ActivityNotFoundException e) {
						e.printStackTrace();
					}

					String tag;
					Set<String> tags = Utils.getTags();

					if (mediaItem.getMediaType() == MediaType.VIDEO) {
						tag = Constants.UA_TAG_TWEET_VIDEO;
						if (!tags.contains(tag)) {
							tags.add(tag);
							Utils.AddTag(tags);
						}


					} else if (mediaItem.getMediaType() == MediaType.TRACK) {

						tag = Constants.UA_TAG_TWEET_SONG;
						if (!tags.contains(tag)) {
							tags.add(tag);
							Utils.AddTag(tags);
						}

					} else if (mediaItem.getMediaType() == MediaType.ALBUM) {
						tag = Constants.UA_TAG_TWEET_ALBUM;
						if (!tags.contains(tag)) {
							tags.add(tag);
							Utils.AddTag(tags);
						}

					} else if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
						tag = Constants.UA_TAG_TWEET_PLAYLIST;
						if (!tags.contains(tag)) {
							tags.add(tag);
							Utils.AddTag(tags);
						}
					}

					isPosting = true;
				}
			}
			if (!isFinishing() && progressDialog != null) {
				progressDialog.dismiss();
			}

			if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_BADGE_ALERT) {
				finish();
			}
		} catch (Exception e){
			Logger.printStackTrace(e);
		}
//		finish();
	}

	@Override
	public void onFailure(int operationId, CommunicationManager.ErrorType errorType, String errorMessage) {
		progressDialog.dismiss();
		finish();
	}

	@Override
	protected void onUserLeaveHint() {
		ScreenLockStatus.getInstance(getBaseContext()).onStop(true, this);
		super.onUserLeaveHint();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Logger.s(" ::::::::::>>> onResume");
		ScreenLockStatus.getInstance(getBaseContext()).onResume(this, this);
		if(isPosting && !gotResult){
			finish();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		ScreenLockStatus.getInstance(getBaseContext()).onStop();
		Analytics.onEndSession(this);
	}
}
