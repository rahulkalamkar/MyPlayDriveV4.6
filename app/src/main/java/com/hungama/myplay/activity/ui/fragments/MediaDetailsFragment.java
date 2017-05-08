package com.hungama.myplay.activity.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.ThreadPoolManager;
import com.hungama.myplay.activity.data.ActionDefinition;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.DataBase.CacheState;
import com.hungama.myplay.activity.data.audiocaching.MediaCachingTaskNew;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.dao.hungama.BaseHungamaResponse;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaTrackDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.Video;
import com.hungama.myplay.activity.gigya.ShareDialogFragment;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.AddToFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.operations.hungama.RelatedVideoOperation;
import com.hungama.myplay.activity.operations.hungama.RemoveFromFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.SocialBadgeAlertOperation;
import com.hungama.myplay.activity.operations.hungama.VideoStreamingOperation;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.ui.CommentsActivity;
import com.hungama.myplay.activity.ui.DownloadConnectingActivity;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.MediaDetailsActivity;
import com.hungama.myplay.activity.ui.MediaDetailsActivityNew;
import com.hungama.myplay.activity.ui.TrendNowActivity;
import com.hungama.myplay.activity.ui.dialogs.PlaylistDialogFragment;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.ui.widgets.CustomAlertDialog;
import com.hungama.myplay.activity.ui.widgets.CustomCacheStateProgressBar;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Appirater;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.hungama.myplay.activity.util.PicassoUtil.PicassoTarget;
import com.hungama.myplay.activity.util.QuickActionDiscoveryGallery;
import com.hungama.myplay.activity.util.QuickActionDiscoveryGallery.OnDiscoverySelectedListener;
import com.hungama.myplay.activity.util.QuickActionMediaDetail;
import com.hungama.myplay.activity.util.QuickActionMediaDetail.OnMediaSelectedListener;
import com.hungama.myplay.activity.util.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class MediaDetailsFragment extends MainFragment implements
		CommunicationOperationListener {

	private static final String TAG = "MediaDetailsFragment";

	public static final String ARGUMENT_MEDIAITEM = "argument_mediaitem";
	// public static final String ARGUMENT_MEDIA_ITEMS_VIDEOS =
	// "argument_media_items_videos";

	public static final int FAVORITE_SUCCESS = 1;

	private DataManager mDataManager;
	private MediaItem mMediaItem;

	public MediaSetDetails mMediaSetDetails;
	private MediaTrackDetails mMediaTrackDetails;
	private Video video;

	// Favorites
	private Drawable whiteHeart;
	private Drawable blueHeart;
	private String mediaType;
	private TextView favButton;
	private Button favButton_main;
	private Context mContext;
	private boolean mHasLoaded = false;


	private LocalBroadcastManager mLocalBroadcastManager;
	private MediaItemFavoriteStateReceiver mMediaItemFavoriteStateReceiver;

	private String mFlurrySubSectionDescription;
	private String mFlurrySourceDescription;

	private String mFlurrySourceSection;
	private String mFlurryEventName;

	private View rootView,headerView;

	private int width;

	private OnMediaSelectedListener mOnLoginOptionSelectedListener;

    private View rootViewParent;

    public void onRootViewParent(
            View rootViewParent) {
        this.rootViewParent = rootViewParent;
    }


    public void onMediaListener(
			OnMediaSelectedListener mOnLoginOptionSelectedListener) {
		this.mOnLoginOptionSelectedListener = mOnLoginOptionSelectedListener;
	}

    private OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener;

    public void onMediaItemOptionSelectedListener(
            OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener) {
        this.mOnMediaItemOptionSelectedListener = mOnMediaItemOptionSelectedListener;
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// initialize components.
		mContext = getActivity().getApplicationContext();
		mDataManager = DataManager.getInstance(mContext);

		if(mediaDetailsActivityNew!=null)
			mFragmentManager = getChildFragmentManager();
		else
			mFragmentManager = getActivity().getSupportFragmentManager();


		mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);
		mMediaItemFavoriteStateReceiver = new MediaItemFavoriteStateReceiver(
				this);

		// gets the media item from parent.
		Bundle data = getArguments();
		mMediaItem = (MediaItem) data.getSerializable(ARGUMENT_MEDIAITEM);
		mFlurrySourceSection = data
				.getString(MediaDetailsActivity.FLURRY_SOURCE_SECTION);

		if (TextUtils.isEmpty(mFlurrySourceSection)) {
			mFlurrySourceSection = "No Flurry Source Section";
		}

		mFlurryEventName = "No Event Name";

		if (mMediaItem.getMediaType() == MediaType.ALBUM) {
			mFlurrySubSectionDescription = FlurryConstants.FlurrySubSectionDescription.AlbumDetail
					.toString();
			mFlurrySourceDescription = FlurryConstants.FlurrySourceDescription.TapOnPlayAlbumPlaylistDetail
					.toString();

			mFlurryEventName = FlurryConstants.FlurryEventName.AlbumDetail
					.toString();

		} else if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
			mFlurrySubSectionDescription = FlurryConstants.FlurrySubSectionDescription.PlaylistDetail
					.toString();
			mFlurrySourceDescription = FlurryConstants.FlurrySourceDescription.TapOnPlayAlbumPlaylistDetail
					.toString();

			mFlurryEventName = FlurryConstants.FlurryEventName.PlaylistDetail
					.toString();

		} else if (mMediaItem.getMediaType() == MediaType.TRACK) {
			mFlurrySubSectionDescription = FlurryConstants.FlurrySubSectionDescription.SongDetail
					.toString();
			mFlurrySourceDescription = FlurryConstants.FlurrySourceDescription.TapOnPlaySongDetail
					.toString();

			mFlurryEventName = FlurryConstants.FlurryEventName.SongDetail
					.toString();
		}

		// Flurry report:
		Map<String, String> reportMap = new HashMap<String, String>();
		reportMap.put(FlurryConstants.FlurryKeys.SourceSection.toString(),
				mFlurrySourceSection);
		reportMap.put(FlurryConstants.FlurryKeys.Title.toString(),
				mMediaItem.getTitle());
		Analytics.logEvent(mFlurryEventName, reportMap);


		Analytics.postCrashlitycsLog(getActivity(), MediaDetailsFragment.class.getName());
	}

    ListView mList;
    int lastScrollPos=0;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (rootView == null) {
			try {
				if (mMediaItem.getMediaContentType() == MediaContentType.MUSIC) {
					rootView = inflater.inflate(
							R.layout.fragment_media_details_new, container,
							false);
                    headerView = inflater.inflate(R.layout.detail_header,null);

					Utils.traverseChild(rootView, getActivity());
					Utils.traverseChild(headerView, getActivity());
				}
			} catch (Error e) {
				System.gc();
				System.runFinalization();
				System.gc();
				if (mMediaItem.getMediaContentType() == MediaContentType.MUSIC) {
					rootView = inflater.inflate(
							R.layout.fragment_media_details_new, container,
							false);
                    headerView = inflater.inflate(R.layout.detail_header,null);
				}
			}
//            initializeOverlay(null);
            mList = (ListView) rootView.findViewById(
                    R.id.text_view_media_details_list);

            mList.addHeaderView(headerView, null, false);

            TracksAdapter tracksAdapter = new TracksAdapter(null);
            mList.setAdapter(tracksAdapter);
            mList.setVisibility(View.VISIBLE);

			// if (placementLeft != null) {
			DisplayMetrics metrics = new DisplayMetrics();

			// fileCache = new FileCache(getActivity());
			getActivity().getWindowManager().getDefaultDisplay()
					.getMetrics(metrics);
			width = metrics.widthPixels;

			cd = new ColorDrawable(getResources().getColor(
					R.color.myPrimaryColor));
            updateTitleColor(cd, true);

			CustomCacheStateProgressBar progressCacheState = (CustomCacheStateProgressBar) headerView
					.findViewById(R.id.media_details_progress_cache_state);
			progressCacheState.setNotCachedStateVisibility(true);
			progressCacheState.setCacheState(CacheState.NOT_CACHED);
            mList.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                    View c = headerView;
                    if (c != null) {

                        int t = -c.getTop() + view.getFirstVisiblePosition() * c.getHeight();
                        final int headerHeight = (int) (getResources().getDimension(R.dimen.flexible_space_image_height) - ((MainActivity)getActivity()).mToolbar.getHeight());
                        final float ratio = (float) Math.min(Math.max(t, 0), headerHeight) / headerHeight;
                        Logger.e("Math.min(Math.max(t, 0), headerHeight)",""+Math.min(Math.max(t, 0), headerHeight));
                        Logger.e("Math.max(t, 0)",""+Math.max(t, 0));
                        Logger.e("headerHeight",""+headerHeight);
                        Logger.e("ratio",""+ratio);
                        if(Math.max(t, 0)==lastScrollPos){
                            return;
                        }
                        lastScrollPos=Math.max(t, 0);
                        int  maxDist = 300;
                        if (Math.max(t, 0) > maxDist) {
                            alpha = 255;
                        }
                        else
                            alpha = (int) (ratio * 255);

                        Logger.e("alpha",""+alpha);
//                    alpha = getAlphaforActionBar(view.getScrollY());
                        if (alpha <= 160)
                            alpha = 160;

//                        Fragment fragment=null;
//                        try {
//                            FragmentManager.BackStackEntry backEntry = (FragmentManager.BackStackEntry) getChildFragmentManager().getBackStackEntryAt(getChildFragmentManager().getBackStackEntryCount() - 1);
//                            String str = backEntry.getName();
//                            Logger.i(TAG, "back stack name " + str);
//                            fragment = getChildFragmentManager().findFragmentByTag(str);
//                        }catch (Exception e){
//
//                        }
//
//                        if(!isVideoInsideOpen && (fragment==null || fragment instanceof  MediaDetailsFragment || fragment instanceof  MediaDetailsActivityNew))
//                        {
                            cd.setAlpha(alpha);
                            updateTitleColor(cd, false);
//                        }

                        if(mediaDetailsActivityNew!=null){
                            if(mediaDetailsActivityNew.alpha.size()>0)
                                mediaDetailsActivityNew.alpha.set(mediaDetailsActivityNew.alpha.size()-1,alpha);
                        }
                    }
                }

                private int getAlphaforActionBar(int scrollY) {

                    int minDist = 0, maxDist = 300;
                    if (scrollY > maxDist) {
                        return 255;
                    } else {
                        if (scrollY < minDist) {
                            return 0;
                        } else {
                            return (int) ((255.0 / maxDist) * scrollY);
                        }
                    }
                }
            });
			setUpViewPager();
		} else {
			Logger.e("HomeMediaTileGridFragment", "onCreateView else");
			ViewGroup parent = (ViewGroup) Utils.getParentViewCustom(rootView);
			parent.removeView(rootView);
		}
		return rootView;
	}

	public int alpha=160;
	private ColorDrawable cd;


	private PicassoUtil picasso;

	private void fillUpPlaylistImage() {
		picasso = PicassoUtil.with(getActivity());
		String[] images = ImagesManager.getImagesUrlArray(
				mMediaSetDetails.getImagesUrlArray(),
				ImagesManager.MUSIC_ART_SMALL,
				DataManager.getDisplayDensityLabel());

		if(getActivity() != null && !TextUtils.isEmpty(mMediaSetDetails.getPlaylistArtwork())){
			final ImageView img = (ImageView) headerView.findViewById(R.id.image);
			img.setVisibility(View.VISIBLE);
			picasso.load(mMediaSetDetails.getPlaylistArtwork(), new PicassoTarget() {
				@Override
				public void onPrepareLoad(Drawable arg0) {
				}

				@SuppressWarnings("deprecation")
				@Override
				public void onBitmapLoaded(Bitmap arg0, LoadedFrom arg1) {
					if (Build.VERSION.SDK_INT >= 16) {
						img.setBackground(new BitmapDrawable(
								getActivity().getResources(), arg0));
					} else {
						img.setBackgroundDrawable(new BitmapDrawable(
								getActivity().getResources(), arg0));
					}
				}

				@Override
				public void onBitmapFailed(Drawable arg0) {
				}
			});
		} else if (mMediaSetDetails.getNumberOfTracks() == 1) {
			final ImageView img = (ImageView) headerView.findViewById(R.id.image);

			img.setVisibility(View.VISIBLE);
			if (images != null && images.length > 0) {

				if (!TextUtils.isEmpty(images[0])) {

					picasso.load(images[0], new PicassoTarget() {

						@Override
						public void onPrepareLoad(Drawable arg0) {
						}

						@SuppressWarnings("deprecation")
						@Override
						public void onBitmapLoaded(Bitmap arg0, LoadedFrom arg1) {
							if (Build.VERSION.SDK_INT >= 16) {
								img.setBackground(new BitmapDrawable(
										getActivity().getResources(), arg0));
							} else {
								img.setBackgroundDrawable(new BitmapDrawable(
										getActivity().getResources(), arg0));
							}
						}

						@Override
						public void onBitmapFailed(Drawable arg0) {
						}
					});

				}
			}
		} else {

			DisplayMetrics displaymetrics = new DisplayMetrics();
			getActivity().getWindowManager().getDefaultDisplay()
					.getMetrics(displaymetrics);
			// int height = displaymetrics.heightPixels;
			int width = displaymetrics.widthPixels;

			int width_height_image = displaymetrics.widthPixels / 3;

			LinearLayout mainLinearLayout = (LinearLayout) headerView
					.findViewById(R.id.ll_playlist_images);

			mainLinearLayout.setOrientation(LinearLayout.VERTICAL);

			LinearLayout innerLinearLayout = new LinearLayout(getActivity());
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					width, width_height_image);
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
	}

	private void downloadImage(String url1, ImageView iv) {
		try {
			if (!TextUtils.isEmpty(url1)) {

				picasso.loadWithFit(null, url1, iv, -1);

			}
		} catch (Exception e) {
			Logger.e(getClass() + ":701", e.toString());
		} catch (Error e) {
			Logger.e(getClass() + ":701", e.toString());
		}
	}

//	private ScrollViewHelper scrollViewHelper;

	private void initializeOverlay(String title) {

		ImageView img = (ImageView) headerView.findViewById(R.id.image);
		if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
			img.setVisibility(View.GONE);
			fillUpPlaylistImage();
		} else {

			String imageURL = "";

			if (mMediaItem.getMediaType() == MediaType.TRACK) {
				String[] images = ImagesManager.getImagesUrlArray(
						mMediaTrackDetails.getImagesUrlArray(),
						ImagesManager.MUSIC_ART_SMALL,
						mDataManager.getDisplayDensity());
				if (images != null && images.length > 0) {
					imageURL = images[0];
				}
			} else {
				String[] images = ImagesManager.getImagesUrlArray(
						mMediaItem.getImagesUrlArray(),
						ImagesManager.HOME_MUSIC_TILE,
						mDataManager.getDisplayDensity());
				if (images != null && images.length > 0) {
					imageURL = images[0];
				}
			}

			Picasso.with(mContext).cancelRequest(img);
			if (mContext != null && mMediaItem != null && imageURL != null
					&& !TextUtils.isEmpty(imageURL)) {
				Picasso.with(mContext)
						.load(imageURL)
						.placeholder(
								R.drawable.background_home_tile_album_default).config(Utils.bitmapConfig565)
						.into(img);
			}
		}

		// setBlurImage(imageURL);

        headerView.findViewById(R.id.image_play).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (mMediaItem.getMediaType() == MediaType.ALBUM
								|| mMediaItem.getMediaType() == MediaType.PLAYLIST) {

							if (mMediaSetDetails != null
									&& mMediaSetDetails.getTracks() != null) {
										playButtonClickActivity(
                                                mMediaSetDetails.getTracks(),
                                                mFlurryEventName,
                                                mFlurrySourceSection);
							}

							// xtpl
							// Flurry report: Play All
							Map<String, String> reportMap = new HashMap<String, String>();
							reportMap.put(
									FlurryConstants.FlurryKeys.SourceSection
											.toString(), mFlurrySourceSection);
							reportMap
									.put(FlurryConstants.FlurryMediaDetailActions.ActionTaken
											.toString(),
											FlurryConstants.FlurryMediaDetailActions.PlayAll
													.toString());
							reportMap
									.put(FlurryConstants.FlurryMediaDetailActions.PlayAllTapped
											.toString(),
											FlurryConstants.FlurryMediaDetailActions.yes
													.toString()); // xtpl
							Analytics.logEvent(mFlurryEventName, reportMap);
							// xtpl
						} else {
							// showOptionDialog();
							Track track = new Track(mMediaItem.getId(),
									mMediaItem.getTitle(), mMediaItem
											.getAlbumName(), mMediaItem
											.getArtistName(), mMediaItem
											.getImageUrl(), mMediaItem
											.getBigImageUrl(), mMediaItem
											.getImages(), mMediaItem
											.getAlbumId());
							Playlist playlist = (Playlist) getActivity()
									.getIntent()
									.getSerializableExtra(
											MediaDetailsActivity.EXTRA_PLAYLIST_ITEM);
							if (playlist != null) {
								track.setTag(playlist);
							}
							List<Track> tracks = new ArrayList<Track>();
							tracks.add(track);
							if (track != null) {
										playButtonClickActivity(tracks,
                                                mFlurryEventName,
                                                mFlurrySourceSection);
							}

							// xtpl
							Map<String, String> reportMap = new HashMap<String, String>();
							reportMap.put(
									FlurryConstants.FlurryKeys.TitleOfTheSong
											.toString(), mMediaItem.getTitle());
							reportMap.put(mMediaItem.getMediaType().toString(),
									Utils.toWhomSongBelongto(mMediaItem));
							reportMap.put(FlurryConstants.FlurryKeys.Source
									.toString(), mFlurrySourceDescription);
							reportMap.put(FlurryConstants.FlurryKeys.SubSection
									.toString(), mFlurrySubSectionDescription);

							Analytics
									.logEvent(
											FlurryConstants.FlurryEventName.SongSelectedForPlay
													.toString(), reportMap);
							// xtpl
						}
					}
				});
	}

	private void setUpViewPager() {

		LinearLayout mainLinearLayout = (LinearLayout) headerView
				.findViewById(R.id.ll_playlist_images);
		ImageView img = (ImageView) headerView.findViewById(R.id.image);
		RelativeLayout rl_header = (RelativeLayout) headerView
				.findViewById(R.id.rl_header);
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay()
				.getMetrics(displaymetrics);
		int width_height_image = displaymetrics.widthPixels / 3;

		img.getLayoutParams().width = width;
		img.getLayoutParams().height = width_height_image * 2;

		rl_header.getLayoutParams().width = width;
		rl_header.getLayoutParams().height = width_height_image * 2;

		mainLinearLayout.getLayoutParams().width = width;
		mainLinearLayout.getLayoutParams().height = width_height_image * 2;

        headerView.findViewById(
				R.id.main_player_content_actions_bar_button_playlist)
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						try {
							if (mMediaItem.getMediaType() == MediaType.ALBUM
									|| mMediaItem.getMediaType() == MediaType.PLAYLIST) {

										addToPlaylistButtonClickActivity(mMediaSetDetails
                                                .getTracks());

								// Added by David Svilem 20/11/2012
								showPlaylistDialog(mMediaSetDetails.getTracks());

							} else {
								Track track = new Track(mMediaItem.getId(),
										mMediaItem.getTitle(), mMediaItem
												.getAlbumName(), mMediaItem
												.getArtistName(), mMediaItem
												.getImageUrl(), mMediaItem
												.getBigImageUrl(), mMediaItem
												.getImages(), mMediaItem
												.getAlbumId());
								List<Track> tracks = new ArrayList<Track>();
								tracks.add(track);

										addToPlaylistButtonClickActivity(tracks);

								// Added by David Svilem 20/11/2012
								showPlaylistDialog(tracks);
								//
							}

							// Flurry report: Add to Playlist
							Map<String, String> reportMap = new HashMap<String, String>();
							reportMap.put(
									FlurryConstants.FlurryKeys.SourceSection
											.toString(), mFlurrySourceSection);
							reportMap
									.put(FlurryConstants.FlurryMediaDetailActions.ActionTaken
											.toString(),
											FlurryConstants.FlurryMediaDetailActions.AddToPlaylist
													.toString());
							Analytics.logEvent(mFlurryEventName, reportMap);
						} catch (Exception e) {
							Logger.e(getClass().getName() + ":1316",
									e.toString());
						}
					}
				});

        headerView.findViewById(R.id.media_detail_more).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(final View v) {
						String menuItems[] = null;
						// int[] arr_images;
						if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
							menuItems = new String[] {
									getString(R.string.full_player_setting_menu_Trend_This),
									getString(R.string.video_player_setting_menu_share),
									getString(R.string.video_player_setting_menu_comments) };
							// arr_images = new int[] {
							// R.drawable.icon_general_share_grey,
							//
							// R.drawable.ic_comments };
						} else if (mMediaItem.getMediaType() == MediaType.TRACK) {

							try {

								if ((mMediaTrackDetails != null && mMediaTrackDetails
										.hasVideo())
										|| (mMediaSetDetails != null && mMediaSetDetails
												.isHasVideo())) {
									menuItems = new String[] {
											getString(R.string.full_player_setting_menu_Trend_This),
											getString(R.string.general_download),
											getString(R.string.search_results_layout_bottom_text_for_video),
											getString(R.string.video_player_setting_menu_share),
											getString(R.string.video_player_setting_menu_comments) };
								} else {
									menuItems = new String[] {
											getString(R.string.full_player_setting_menu_Trend_This),
											getString(R.string.general_download),

											getString(R.string.video_player_setting_menu_share),
											getString(R.string.video_player_setting_menu_comments) };
//									mActionButtonVideo.setVisibility(View.GONE);
								}
							} catch (Exception e) {
								Logger.printStackTrace(e);
							}


						} else {
							if ((mMediaTrackDetails != null && mMediaTrackDetails
									.hasVideo())
									|| (mMediaSetDetails != null && mMediaSetDetails
											.isHasVideo())) {
								menuItems = new String[] {
										getString(R.string.full_player_setting_menu_Trend_This),
										getString(R.string.search_results_layout_bottom_text_for_video),
										getString(R.string.video_player_setting_menu_share),
										getString(R.string.video_player_setting_menu_comments) };
							}else{
								menuItems = new String[] {
										getString(R.string.full_player_setting_menu_Trend_This),
										getString(R.string.video_player_setting_menu_share),
										getString(R.string.video_player_setting_menu_comments) };
							}

						}

						QuickActionDiscoveryGallery quickaction = new QuickActionDiscoveryGallery(
								getActivity(), menuItems, /* arr_images, */null);
						quickaction
								.setOnDiscoverySelectedListener(new OnDiscoverySelectedListener() {

									@Override
									public void onItemSelectedPosition(int id) {
									}

									@Override
									public void onItemSelected(String item) {
										try{
											if(getActivity()!=null && !getActivity().isFinishing()){
												String txtTrendThis = getString(R.string.full_player_setting_menu_Trend_This);
												String txtShare = getString(R.string.video_player_setting_menu_share);
												String txtComments = getString(R.string.video_player_setting_menu_comments);
												String txtVideo = getString(R.string.search_results_layout_bottom_text_for_video);
												String txtDownload = getString(R.string.general_download);

												if (item.equals(txtTrendThis)) {
													((MainActivity) getActivity()).isSkipResume=true;
													Intent intent = new Intent(
															mContext,
															TrendNowActivity.class);
													intent.putExtra(
															TrendNowActivity.EXTRA_DATA_MEDIA_ITEM,
															(Serializable) mMediaItem);

													getActivity().startActivity(intent);
												} else if (item.equals(txtDownload)) {
													// download mp3
													((MainActivity) getActivity()).isSkipResume=true;

													Intent intent = new Intent(
															mContext,
															DownloadConnectingActivity.class);
													intent.putExtra(
															DownloadConnectingActivity.EXTRA_MEDIA_ITEM,
															(Serializable) mMediaItem);
													getActivity().startActivity(intent);
												} else if (item.equals(txtVideo)) {
													// video
													if (mMediaItem.getMediaType() == MediaType.ALBUM
															|| mMediaItem
															.getMediaType() == MediaType.PLAYLIST) {
														openVideoPage(mMediaSetDetails);
													} else if (mMediaItem
															.getMediaType() == MediaType.TRACK) {
														if (mMediaTrackDetails != null
																&& String
																.valueOf(mMediaTrackDetails
																		.getAlbumId()) != null)
															mDataManager
																	.getRelatedVideo(
																			mMediaTrackDetails,
																			mMediaItem,
																			MediaDetailsFragment.this);
													}

													// Flurry report: Videos
													Map<String, String> reportMap = new HashMap<String, String>();
													reportMap
															.put(FlurryConstants.FlurryKeys.SourceSection
																			.toString(),
																	mFlurrySourceSection);
													reportMap
															.put(FlurryConstants.FlurryMediaDetailActions.ActionTaken
																			.toString(),
																	FlurryConstants.FlurryMediaDetailActions.Videos
																			.toString());
													Analytics
															.logEvent(mFlurryEventName,
																	reportMap);
												} else if (item.equals(txtShare)) {
													// share
													Map<String, Object> shareData = new HashMap<String, Object>();

													if (mMediaItem.getMediaType() == MediaType.ALBUM
															&& !TextUtils.isEmpty(mMediaItem.getAlbumName()))
														shareData
																.put(ShareDialogFragment.TITLE_DATA,
																		mMediaItem
																				.getAlbumName());
													else
														shareData
																.put(ShareDialogFragment.TITLE_DATA,
																		mMediaItem
																				.getTitle());


													shareData
															.put(ShareDialogFragment.SUB_TITLE_DATA,
																	mMediaItem
																			.getAlbumName());

													if (mMediaItem.getMediaType() == MediaType.TRACK) {
														String[] images = ImagesManager.getImagesUrlArray(
																mMediaTrackDetails.getImagesUrlArray(),
																ImagesManager.MUSIC_ART_SMALL,
																mDataManager.getDisplayDensity());
														if (images != null && images.length > 0) {
															shareData
																	.put(ShareDialogFragment.THUMB_URL_DATA,
																			images[0]);
														}
													} else {
														String[] images = ImagesManager.getImagesUrlArray(
																mMediaItem.getImagesUrlArray(),
																ImagesManager.HOME_MUSIC_TILE,
																mDataManager.getDisplayDensity());
														if (images != null && images.length > 0) {
															shareData
																	.put(ShareDialogFragment.THUMB_URL_DATA,
																			images[0]);
														}
													}


													shareData
															.put(ShareDialogFragment.MEDIA_TYPE_DATA,
																	mMediaItem
																			.getMediaType());
													shareData
															.put(ShareDialogFragment.TRACK_NUMBER_DATA,
																	mMediaItem
																			.getMusicTrackCount());
													shareData
															.put(ShareDialogFragment.CONTENT_ID_DATA,
																	mMediaItem.getId());

													// Flurry report: Share
													Map<String, String> reportMap = new HashMap<String, String>();
													reportMap
															.put(FlurryConstants.FlurryKeys.SourceSection
																			.toString(),
																	mFlurrySourceSection);
													reportMap
															.put(FlurryConstants.FlurryMediaDetailActions.ActionTaken
																			.toString(),
																	FlurryConstants.FlurryMediaDetailActions.Share
																			.toString());
													Analytics
															.logEvent(mFlurryEventName,
																	reportMap);

													FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();
													ShareDialogFragment shareDialogFragment = ShareDialogFragment
															.newInstance(shareData,
																	mFlurryEventName);

													shareDialogFragment
															.show(mFragmentManager,
																	ShareDialogFragment.FRAGMENT_TAG);
												} else if (item.equals(txtComments)) {
													// comment
													if (mMediaItem.getMediaType() == MediaType.ALBUM
															|| mMediaItem
															.getMediaType() == MediaType.PLAYLIST) {
														if (mMediaSetDetails != null
																&& mMediaSetDetails
																.getNumOfComments() >= 0) {

															openCommentsPage(
																	mMediaItem,
																	mMediaSetDetails
																			.getNumOfComments());
														}
													} else if (mMediaItem
															.getMediaType() == MediaType.TRACK) {
														if (mMediaTrackDetails != null
																&& mMediaTrackDetails
																.getNumOfComments() >= 0) {

															openCommentsPage(
																	mMediaItem,
																	mMediaTrackDetails
																			.getNumOfComments());
														}
													}
												}
											}
										}catch (Exception e){}

									}
								});
						quickaction.show(v);
						v.setEnabled(false);
						quickaction
								.setOnDismissListener(new QuickActionDiscoveryGallery.OnDismissListener() {
									@Override
									public void onDismiss() {
										v.setEnabled(true);
									}
								});

					}
				});
	}

	private boolean isSkipResume_Act;

	@Override
	public void onStart() {
		super.onStart();
		// For Favorites

		Analytics.startSession(getActivity(), this);
		Analytics.onPageView();

		IntentFilter filter = new IntentFilter(
				ActionDefinition.ACTION_MEDIA_ITEM_FAVORITE_STATE_CHANGED);
		mLocalBroadcastManager.registerReceiver(
				mMediaItemFavoriteStateReceiver, filter);

		if(mediaDetailsActivityNew==null)
			if (((MainActivity) getActivity()).isSkipResume) {
				return;
			}

		if (isSkipResume_Act) {
			isSkipResume_Act = false;
			return;
		}
		isSkipResume_Act = true;
		whiteHeart = getResources().getDrawable(
				R.drawable.icon_media_details_fav_gray);
		blueHeart = getResources().getDrawable(
				R.drawable.icon_media_details_fav_blue);

		if (!mHasLoaded) {
			if (mMediaItem.getMediaContentType() == MediaContentType.VIDEO) {
				// mDataManager.getVideoDetails(mMediaItem, this);
				mediaType = MediaContentType.VIDEO.toString();
			} else {
				boolean isCached = false;
				if (!isCached) {
					// rootView.findViewById(R.id.progressbar).setVisibility(
					// View.VISIBLE);
					mDataManager.getMediaDetails(mMediaItem, null, this);
				}
				if (mMediaItem.getMediaType() == MediaType.TRACK) {
					mediaType = "song";
				} else {
					mediaType = mMediaItem.getMediaType().toString();
				}
			}
		} else {
			if (mMediaItem.getMediaType() == MediaType.VIDEO) {
				// For Favorites
				mediaType = MediaType.VIDEO.toString();
				// get details for video (video streaming).
				populateUserControls(video);
			} else if (mMediaItem.getMediaType() == MediaType.ALBUM
					|| mMediaItem.getMediaType() == MediaType.PLAYLIST) {
				// For favorites
				mediaType = mMediaItem.getMediaType().toString();
				// get details for albums / playlists.
				if (mMediaSetDetails != null) {
					populateUserControls(mMediaSetDetails);
				}
				setActionButtons();
			} else if (mMediaItem.getMediaType() == MediaType.TRACK) {
				// For Favorites
				mediaType = "song";
				// get details for track (song).
				populateUserControls(mMediaTrackDetails);
				setActionButtons();
			}
		}

		if (mMediaItem.getMediaType() == MediaType.TRACK) {
			Analytics.logEvent("Song detail");
		} else if (mMediaItem.getMediaType() == MediaType.ALBUM) {
			Analytics.logEvent("Album detail");
		} else if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
			Analytics.logEvent("Playlist detail");
		}
	}

	@Override
	public void onResume() {
        Logger.e("onResume:*","count**");
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
	public void onStop() {

		mLocalBroadcastManager
				.unregisterReceiver(mMediaItemFavoriteStateReceiver);

		super.onStop();
		Analytics.onEndSession(getActivity());
	}

	// ======================================================
	// Communication callbacks.
	// ======================================================

	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
			// showLoadingDialogWithoutVisibleCheck(R.string.application_dialog_loading_content);
			mHasLoaded = true;

		} else if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_STREAMING) {
//			showLoadingDialog(R.string.application_dialog_loading_content);
			((MainActivity) getActivity()).showLoadingDialog(R.string.application_dialog_loading_content);
			mHasLoaded = true;
		} else if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_RELATED) {
//			showLoadingDialog(R.string.application_dialog_loading_content);
			((MainActivity) getActivity()).showLoadingDialog(R.string.application_dialog_loading_content);
		} else if (operationId == OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE) {
			// showLoadingDialog(R.string.application_dialog_loading_content);

		} else if (operationId == OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE) {
			// showLoadingDialog(R.string.application_dialog_loading_content);
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
            Logger.e("onSuccess","onSuccess 1");
			// rootView.findViewById(R.id.progressbar).setVisibility(View.GONE);
			if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {

				MediaItem mediaItem = (MediaItem) responseObjects
						.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_ITEM);

				if (mediaItem != null) {
					if (mediaItem.getMediaType() == MediaType.ALBUM
							|| mediaItem.getMediaType() == MediaType.PLAYLIST) {
						try {

							// get details for albums / playlists.
							mMediaSetDetails = (MediaSetDetails) responseObjects
									.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);
							if (mMediaSetDetails != null) {
								if(TextUtils.isEmpty(mMediaItem.getTitle())){
									mMediaItem.setImagesUrlArray(mMediaSetDetails.getImages());
								}
                                populateUserControls(mMediaSetDetails);


                                if (mediaDetailsActivityNew != null) {
                                    if (mediaDetailsActivityNew.alpha.size() > 0) {
                                        mediaDetailsActivityNew.alpha.set(mediaDetailsActivityNew.alpha.size() - 1, alpha);
                                        mediaDetailsActivityNew.listTitle.set(mediaDetailsActivityNew.listTitle.size() - 1,mMediaSetDetails
                                                .getTitle());
                                    }
                                }


                                Logger.e("onSuccess", "onSuccess 2");

								try {
									if (getBundle()
											.getBoolean("add_to_queue",
                                                    false)) {
                                        addTracksToQueue(mMediaSetDetails);
										// RadioActivity.forceFinish();
									}
									if (getBundle()
											.getString(
                                                    "video_in_audio_content_id") != null) {
										String subtitle = mMediaSetDetails
												.getReleaseYear()
												+ " | "
												+ mMediaSetDetails
														.getLanguage()
												+ " | "
												+ mMediaSetDetails
														.getNumOfPlays()
												+ " songs";

                                        updateTitleSubtitle(
														mMediaSetDetails
																.getTitle(),
														subtitle,alpha);
									}else
                                        updateTitleSubtitle(
                                                        mMediaSetDetails.getTitle(),
                                                        generateSubtitle(mMediaSetDetails),alpha);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							setActionButtons();

						} catch (Exception e) {
							e.printStackTrace();
							hideLoadingDialog();
						}
                        setVideoInsong();
					} else if (mediaItem.getMediaType() == MediaType.TRACK) {
						try {
							// get details for track (song).
							mMediaTrackDetails = (MediaTrackDetails) responseObjects
									.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);
							if(TextUtils.isEmpty(mMediaItem.getTitle())){
								mMediaItem.setImagesUrlArray(mMediaTrackDetails.getImages());
							}
							populateUserControls(mMediaTrackDetails);
							setActionButtons();

                            if (mediaDetailsActivityNew != null) {
                                if (mediaDetailsActivityNew.alpha.size() > 0) {
                                    mediaDetailsActivityNew.alpha.set(mediaDetailsActivityNew.alpha.size() - 1, alpha);
                                    mediaDetailsActivityNew.listTitle.set(mediaDetailsActivityNew.listTitle.size() - 1,mMediaTrackDetails
                                            .getTitle());
                                }
                            }

							if (getBundle().getBoolean(
                                    "add_to_queue", false)) {
                                updateTitle(mMediaTrackDetails);
								// RadioActivity.forceFinish();
							}
							if (getBundle().getString(
                                    "video_in_audio_content_id") != null) {
										updateTitleSubtitle(
												mMediaTrackDetails.getTitle(), "",alpha);
							}else
                                        updateTitleSubtitle(
                                                mMediaTrackDetails.getTitle(), "",alpha);
							if(PlayerService.service!=null && (PlayerService.service.getCurrentPlayingTrack()!=null && PlayerService.service.getCurrentPlayingTrack().getId() == mMediaTrackDetails.getId())){
								PlayerService.service.setDetailsToCurrentPlayer(mMediaTrackDetails);
							}
                            mDataManager.updateTracks(mMediaTrackDetails, null);
						} catch (Exception e) {
							e.printStackTrace();
							hideLoadingDialog();
						}

					}
                    setVideoInsong();
				}
				((MainActivity) getActivity()).hideLoadingDialog();
				hideLoadingDialog();

			} else if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_STREAMING) {
				video = (Video) responseObjects
						.get(VideoStreamingOperation.RESPONSE_KEY_VIDEO_STREAMING);
				populateUserControls(video);
				((MainActivity) getActivity()).hideLoadingDialog();
				hideLoadingDialog();

			} else if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_RELATED) {
				try {
					List<MediaItem> mediaItems = (List<MediaItem>) responseObjects
							.get(RelatedVideoOperation.RESPONSE_KEY_RELATED_VIDEO);
                    openVideoPageTrack(mediaItems);

				} catch (Exception e) {
					Logger.e(getClass().getName() + ":542", e.toString());
				}
			} else if (operationId == OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE) {
				BaseHungamaResponse addToFavoriteResponse = (BaseHungamaResponse) responseObjects
						.get(AddToFavoriteOperation.RESULT_KEY_ADD_TO_FAVORITE);

				// has the item been added from favorites.
				if (addToFavoriteResponse.getCode() == FAVORITE_SUCCESS) {
					Utils.makeText(getActivity(),
							addToFavoriteResponse.getMessage(),
							Toast.LENGTH_LONG).show();

					int favorites = 0;
					if (mMediaItem.getMediaType() == MediaType.ALBUM
							|| mMediaItem.getMediaType() == MediaType.PLAYLIST) {
						favorites = mMediaSetDetails.getNumOfFav() + 1;

					} else if (mMediaItem.getMediaType() == MediaType.TRACK) {
						favorites = mMediaTrackDetails.getNumOfFav() + 1;
					}

					// packs an added media item intent action.
					Intent intent = new Intent(
							ActionDefinition.ACTION_MEDIA_ITEM_FAVORITE_STATE_CHANGED);
					Bundle extras = new Bundle();
					extras.putSerializable(ActionDefinition.EXTRA_MEDIA_ITEM,
							(Serializable) mMediaItem);
					extras.putBoolean(
							ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_IS_FAVORITE,
							true);
					extras.putInt(
							ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_COUNT,
							favorites);
					intent.putExtras(extras);

					mLocalBroadcastManager.sendBroadcast(intent);

					mDataManager.checkBadgesAlert("" + mMediaItem.getId(),
							mediaType,
							SocialBadgeAlertOperation.ACTION_FAVORITE, this);
				} else {
					Utils.makeText(
							getActivity(),
							getResources().getString(
									R.string.favorite_error_saving,
									mMediaItem.getTitle()), Toast.LENGTH_LONG)
							.show();
				}

			} else if (operationId == OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE) {
				try {
					BaseHungamaResponse removeFromFavoriteResponse = (BaseHungamaResponse) responseObjects
							.get(RemoveFromFavoriteOperation.RESULT_KEY_REMOVE_FROM_FAVORITE);

					// has the item been removed from favorites.
					if (removeFromFavoriteResponse.getCode() == FAVORITE_SUCCESS) {

						Utils.makeText(getActivity(),
								removeFromFavoriteResponse.getMessage(),
								Toast.LENGTH_LONG).show();

						int favorites = 0;
						if (mMediaItem.getMediaType() == MediaType.ALBUM
								|| mMediaItem.getMediaType() == MediaType.PLAYLIST) {
							favorites = mMediaSetDetails.getNumOfFav() - 1;

						} else if (mMediaItem.getMediaType() == MediaType.TRACK) {
							favorites = mMediaTrackDetails.getNumOfFav() - 1;
						}

						// packs an added media item intent action.
						Intent intent = new Intent(
								ActionDefinition.ACTION_MEDIA_ITEM_FAVORITE_STATE_CHANGED);
						Bundle extras = new Bundle();
						extras.putSerializable(
								ActionDefinition.EXTRA_MEDIA_ITEM, mMediaItem);
						extras.putBoolean(
								ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_IS_FAVORITE,
								false);
						extras.putInt(
								ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_COUNT,
								favorites);
						intent.putExtras(extras);

						mLocalBroadcastManager.sendBroadcast(intent);

					} else {
						Utils.makeText(
								getActivity(),
								getResources().getString(
										R.string.favorite_error_removing,
										mMediaItem.getTitle()),
								Toast.LENGTH_LONG).show();
					}
				} catch (Exception e) {
					Logger.e(getClass().getName() + ":601", e.toString());
				}
			}
			((MainActivity) getActivity()).hideLoadingDialog();
			hideLoadingDialog();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		if(getActivity()!=null)
			((MainActivity) getActivity()).hideLoadingDialog();
		hideLoadingDialog();
	}

	private String generateSubtitle(MediaSetDetails mMediaSetDetails) {
		String releaseDate = mMediaSetDetails.getReleaseYear() != null ? mMediaSetDetails
				.getReleaseYear() + " | "
				: "";
		String languer = mMediaSetDetails.getLanguage() != null ? mMediaSetDetails
				.getLanguage() + " | "
				: "";
		String subtitle = releaseDate + languer
				+ mMediaSetDetails.getNumberOfTracks() + " songs";
		return subtitle;
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		try {
			// rootView.findViewById(R.id.progressbar).setVisibility(View.GONE);
			if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
				Logger.i(TAG, "Failed loading media details");

                headerView.findViewById(R.id.search_results_loading_bar_progress)
						.setVisibility(View.GONE);
				((MainActivity) getActivity())
						.internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
							@Override
							public void onRetryButtonClicked() {
								mHasLoaded = false;
								onStart();
							}
						});
			} else if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_STREAMING) {
				Logger.i(TAG, "Failed loading video streaming");
				((MainActivity) getActivity())
						.internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
							@Override
							public void onRetryButtonClicked() {
								mHasLoaded = false;
								onStart();
							}
						});
			} else if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_RELATED) {
				Logger.i(TAG, "Failed loading video related");
			} else if (operationId == OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE) {
				favButton_main.setEnabled(true);
				Logger.i(TAG, "Failed add to favorite");

			} else if (operationId == OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE) {
				favButton_main.setEnabled(true);
				Logger.i(TAG, "Failed remove from favorite");
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		hideLoadingDialog();
	}

	// ======================================================
	// Private helper methods.
	// ======================================================

	private void initializeUserControls(final View rootView) {
		hideAndShowPanels();
	}

	private void hideAndShowPanels() {
		RelativeLayout mAlbumPanel = (RelativeLayout) headerView.findViewById(
				R.id.Relativelayout_media_details_album);
		RelativeLayout mAlbumAndPlaylistPanel = (RelativeLayout) headerView
				.findViewById(R.id.relativelayout_panel_for_album_and_playlist);
		RelativeLayout mTrackPanel = (RelativeLayout) headerView.findViewById(
				R.id.media_details_mid_right_song_details);

		LinearLayout linearlayout_media_details_album_details_year_and_genre = (LinearLayout) headerView
				.findViewById(
						R.id.linearlayout_media_details_album_details_year_and_genre);
		LinearLayout linearlayout_media_details_song_details_year_and_genre = (LinearLayout) headerView
				.findViewById(
						R.id.linearlayout_media_details_song_details_year_and_genre);

		if (mMediaItem.getMediaType() == MediaType.ALBUM) {
			linearlayout_media_details_album_details_year_and_genre
					.setVisibility(View.VISIBLE);
			linearlayout_media_details_song_details_year_and_genre
					.setVisibility(View.GONE);
			mTrackPanel.setVisibility(View.GONE);
			mAlbumPanel.setVisibility(View.VISIBLE);
			mAlbumAndPlaylistPanel.setVisibility(View.VISIBLE);
		} else if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
			linearlayout_media_details_album_details_year_and_genre
					.setVisibility(View.INVISIBLE);
			linearlayout_media_details_song_details_year_and_genre
					.setVisibility(View.GONE);
			mTrackPanel.setVisibility(View.GONE);
			mAlbumPanel.setVisibility(View.GONE);
			mAlbumAndPlaylistPanel.setVisibility(View.VISIBLE);

		} else {
			linearlayout_media_details_album_details_year_and_genre
					.setVisibility(View.GONE);
			linearlayout_media_details_song_details_year_and_genre
					.setVisibility(View.VISIBLE);
			mTrackPanel.setVisibility(View.VISIBLE);
			mAlbumPanel.setVisibility(View.GONE);
			mAlbumAndPlaylistPanel.setVisibility(View.GONE);
		}
	}

	private void populateUserControls(final MediaSetDetails details) {
		try {

			// nTracks = details.getNumberOfTracks();
			// initialize Texts for ALBUM page
			initializeOverlay(mMediaItem.getTitle());

            initializeUserControls(headerView);


            TracksAdapter tracksAdapter = new TracksAdapter(details);
            mList.setAdapter(tracksAdapter);
            mList.setVisibility(View.VISIBLE);

//            new Handler().postDelayed(new Runnable() {
//
//                @Override
//                public void run() {
            try {
                if (details.getNumberOfTracks() > 0) {
//                            setListViewHeightBasedOnChildren(mList, null);
                    // ListUtility.setListViewHeightBasedOnChildren(mList,
                    // getActivity());
                    headerView.findViewById(
                            R.id.search_results_loading_bar_progress).setVisibility(View.GONE);
//							mList.setVisibility(View.VISIBLE);

                }
            } catch (Exception e) {
                Logger.printStackTrace(e);
            }
//                }
//            }, 1000);


            LanguageTextView mAlbumYear = (LanguageTextView) headerView
					.findViewById(
							R.id.text_view_media_details_album_details_year);
			LanguageTextView mAlbumGenre = (LanguageTextView) headerView
					.findViewById(
							R.id.text_view_media_details_album_details_genre);
			LanguageTextView mAlbumMusicBy = (LanguageTextView) headerView
					.findViewById(
							R.id.text_view_media_details_album_details_music_by);
			LanguageTextView mAlbumLabel = (LanguageTextView) headerView
					.findViewById(
							R.id.text_view_media_details_album_details_label);


			LanguageTextView mTrackNumOfPlays = (LanguageTextView) headerView
					.findViewById(R.id.text_view_media_details_num_plays);

			if (mTrackNumOfPlays != null && details.getNumOfPlays() >= 0) {
				mTrackNumOfPlays
						.setText(String.valueOf(details.getNumOfPlays()));
			}
			if (mMediaItem.getMediaType() == MediaType.ALBUM) {
				if (details.getReleaseYear() != null) {
					mAlbumYear.setText(details.getReleaseYear());
				}

				if (details.getLanguage() != null) {
					mAlbumGenre.setText(details.getLanguage());
				}

				if (details.getDirector() != null) {
					mAlbumMusicBy.setText(details.getDirector());
				}

				if (details.getLabel() != null) {
					mAlbumLabel.setText("\u00A9 " + details.getLabel());
				}

			} else if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
			}

			// Set Favorite Button
			favButton = (TextView) headerView.findViewById(
					R.id.button_media_details_heart);

			favButton_main = (Button) headerView.findViewById(
					R.id.button_media_fav);
			favButton_main.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (v.isSelected()) {
						favButton.setCompoundDrawablesWithIntrinsicBounds(
								whiteHeart, null, null, null);
						favButton_main
								.setCompoundDrawablesWithIntrinsicBounds(
										null,
										getResources()
												.getDrawable(
														R.drawable.icon_main_player_favorites_white),
										null, null);
						favButton.setText(Utils.roundTheCount(details
								.getNumOfFav() - 1));
						favButton_main.setEnabled(false);
						mDataManager.removeFromFavorites(
								String.valueOf(mMediaItem.getId()), mediaType,
								MediaDetailsFragment.this);

					} else {
						favButton.setCompoundDrawablesWithIntrinsicBounds(
								blueHeart, null, null, null);
						favButton_main
								.setCompoundDrawablesWithIntrinsicBounds(
										null,
										getResources()
												.getDrawable(
														R.drawable.icon_main_player_favorites_blue),
										null, null);
						favButton.setText(Utils.roundTheCount(details
								.getNumOfFav() + 1));
						favButton_main.setEnabled(false);
						mDataManager.addToFavorites(
								String.valueOf(mMediaItem.getId()), mediaType,
								MediaDetailsFragment.this);

						Appirater appirater = new Appirater(mContext);
						appirater.userDidSignificantEvent(true);

						// Flurry report
						Map<String, String> reportMap = new HashMap<String, String>();

						reportMap.put(FlurryConstants.FlurryKeys.TitleContentID
								.toString(), mMediaItem.getTitle() + "_"
								+ mMediaItem.getId());
						reportMap.put(
								FlurryConstants.FlurryKeys.Type.toString(),
								mMediaItem.getMediaType().toString());
						reportMap.put(
								FlurryConstants.FlurryKeys.Source.toString(),
								mFlurrySubSectionDescription);
						Analytics.logEvent(
								FlurryConstants.FlurryEventName.FavoriteButton
										.toString(), reportMap);

						// Flurry report: Favorite
						reportMap.clear();
						reportMap.put(FlurryConstants.FlurryKeys.SourceSection
								.toString(), mFlurrySourceSection);
						reportMap
								.put(FlurryConstants.FlurryMediaDetailActions.ActionTaken
										.toString(),
										FlurryConstants.FlurryMediaDetailActions.Favorite
												.toString());
						Analytics.logEvent(mFlurryEventName, reportMap);
					}
				}
			});

			if (details.getNumOfFav() >= 0) {
				favButton.setText(Utils.roundTheCount(details.getNumOfFav()));
			}
			if (details.IsFavorite() || favButton.isSelected()) {
				favButton.setCompoundDrawablesWithIntrinsicBounds(blueHeart,
						null, null, null);
				favButton_main.setCompoundDrawablesWithIntrinsicBounds(
						null,
						getResources().getDrawable(
								R.drawable.icon_main_player_favorites_blue),
						null, null);

				favButton_main.setSelected(true);
			} else {
				favButton.setCompoundDrawablesWithIntrinsicBounds(whiteHeart,
						null, null, null);
				favButton_main.setCompoundDrawablesWithIntrinsicBounds(
						null,
						getResources().getDrawable(
								R.drawable.icon_main_player_favorites_white),
						null, null);
				favButton_main.setSelected(false);
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void populateUserControls(final MediaTrackDetails details) {
		if (details == null)
			return;
		try {


            headerView.findViewById(
                    R.id.view_media_details_album_details_seperator1)
                    .setVisibility(View.GONE);
             mList.setDividerHeight(0);


			initializeOverlay(mMediaItem.getTitle());
			// initialize Texts for TRACK page
			LanguageTextView mTrackAlbumName = (LanguageTextView) headerView
					.findViewById(
							R.id.text_view_media_details_song_details_album_name);
			LanguageTextView mTrackYear = (LanguageTextView) headerView
					.findViewById(
							R.id.text_view_media_details_song_details_year);
			LanguageTextView mTrackLanguage = (LanguageTextView) headerView
					.findViewById(
							R.id.text_view_media_details_song_details_language);
			LanguageTextView mTrackMusicBy = (LanguageTextView) headerView
					.findViewById(
							R.id.text_view_media_details_song_details_music_by);
			LanguageTextView mTrackSingerName = (LanguageTextView) headerView
					.findViewById(
							R.id.text_view_media_details_song_details_singer_name);
			LanguageTextView mTrackLyricistName = (LanguageTextView) headerView
					.findViewById(
							R.id.text_view_media_details_song_details_lyricist_name);
			LanguageTextView mTrackNumOfPlays = (LanguageTextView) headerView
					.findViewById(R.id.text_view_media_details_num_plays);

			LanguageTextView mTrackMusic = (LanguageTextView) headerView
					.findViewById(
							R.id.text_view_media_details_song_details_music);

			LanguageTextView mTrackSinger = (LanguageTextView) headerView
					.findViewById(
							R.id.text_view_media_details_song_details_singer);

			LanguageTextView mTrackLyricist = (LanguageTextView) headerView
					.findViewById(
							R.id.text_view_media_details_song_details_lyricist);

			LanguageTextView mTrackLabel = (LanguageTextView) headerView
					.findViewById(
							R.id.text_view_media_details_song_details_label);

			headerView.findViewById(
					R.id.text_view_media_details_song_details_year_seperator)
					.setVisibility(View.VISIBLE);

			// populate TRACK page fields
			mTrackAlbumName.setText(details.getAlbumName());
			mTrackYear.setText(details.getReleaseYear());
			mTrackLanguage.setText(details.getLanguage());
			if (details.getLabel() != null)
				mTrackLabel.setText(Utils.getMultilanguageTextLayOut(mContext,
						getString(R.string.media_details_song_details_label))
						+ ": " + details.getLabel());

			String music = details.getMusicDirector();
			if (!TextUtils.isEmpty(music)) {
				mTrackMusicBy.setText(music);
			} else {
				mTrackMusicBy.setVisibility(View.GONE);
				mTrackMusic.setVisibility(View.GONE);
			}

			String singer = details.getSingers();
			if (!TextUtils.isEmpty(singer)) {
				mTrackSingerName.setText(singer);
			} else {
				mTrackSingerName.setVisibility(View.GONE);
				mTrackSinger.setVisibility(View.GONE);
			}

			String lyricist = details.getLyricist();
			if (!TextUtils.isEmpty(lyricist)) {
				mTrackLyricistName.setText(lyricist);
			} else {
				mTrackLyricistName.setVisibility(View.GONE);
				mTrackLyricist.setVisibility(View.GONE);
			}

			mTrackNumOfPlays.setText(String.valueOf(details.getNumOfPlays()));


			// Set Favorite Button
			favButton = (TextView) headerView.findViewById(
					R.id.button_media_details_heart);
			favButton_main = (Button) headerView.findViewById(
					R.id.button_media_fav);
			favButton_main.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (v.isSelected()) {
						favButton.setCompoundDrawablesWithIntrinsicBounds(
								whiteHeart, null, null, null);
						favButton_main
								.setCompoundDrawablesWithIntrinsicBounds(
										null,
										getResources()
												.getDrawable(
														R.drawable.icon_main_player_favorites_white),
										null, null);
						favButton.setText(Utils.roundTheCount(details
								.getNumOfFav() - 1));
						mDataManager.removeFromFavorites(
								String.valueOf(mMediaItem.getId()), mediaType,
								MediaDetailsFragment.this);
					} else {
						favButton.setCompoundDrawablesWithIntrinsicBounds(
								blueHeart, null, null, null);
						favButton_main
								.setCompoundDrawablesWithIntrinsicBounds(
										null,
										getResources()
												.getDrawable(
														R.drawable.icon_main_player_favorites_blue),
										null, null);
						favButton.setText(Utils.roundTheCount(details
								.getNumOfFav() + 1));
						mDataManager.addToFavorites(
								String.valueOf(mMediaItem.getId()), mediaType,
								MediaDetailsFragment.this);

						Appirater appirater = new Appirater(mContext);
						appirater.userDidSignificantEvent(true);
					}
					favButton_main.setEnabled(false);
				}
			});
			favButton.setText(Utils.roundTheCount(details.getNumOfFav()));
			if (details.IsFavorite() || favButton.isSelected()) {
				favButton.setCompoundDrawablesWithIntrinsicBounds(blueHeart,
						null, null, null);
				favButton_main.setCompoundDrawablesWithIntrinsicBounds(
						null,
						getResources().getDrawable(
								R.drawable.icon_main_player_favorites_blue),
						null, null);
				favButton_main.setSelected(true);
			} else {
				favButton.setCompoundDrawablesWithIntrinsicBounds(whiteHeart,
						null, null, null);
				favButton_main.setCompoundDrawablesWithIntrinsicBounds(
						null,
						getResources().getDrawable(
								R.drawable.icon_main_player_favorites_white),
						null, null);
				favButton_main.setSelected(false);
			}


			initializeUserControls(headerView);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    private Bundle getBundle(){
        if(mediaDetailsActivityNew!=null)
            return mediaDetailsActivityNew.getArguments();
         else
            return getActivity().getIntent().getExtras();
    }

	private void setVideoInsong() {
		try {
			// System.out.println(HomeActivity.videoInAlbumSet + " ::::::: " +
			// getActivity().getIntent().getStringExtra("video_in_audio_content_id"));
			if (getBundle().getString(
                    "video_in_audio_content_id") != null
					&& !HomeActivity.videoInAlbumSet) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // System.out.println(" ::::::::: " +
                        // mActionButtonVideo.performClick());
                        HomeActivity.videoInAlbumSet = true;
                        // Flurry report: Videos
                        Map<String, String> reportMap = new HashMap<String, String>();
                        reportMap.put(
                                FlurryConstants.FlurryKeys.SourceSection.toString(),
                                mFlurrySourceSection);
                        reportMap.put(
                                FlurryConstants.FlurryMediaDetailActions.ActionTaken
                                        .toString(),
                                FlurryConstants.FlurryMediaDetailActions.Videos
                                        .toString());
                        Analytics.logEvent(mFlurryEventName, reportMap);


                        // mActionButtonVideo.performClick();
                        if ((mMediaTrackDetails != null && mMediaTrackDetails
                                .hasVideo())
                                || (mMediaSetDetails != null && mMediaSetDetails
                                .isHasVideo())) {
                            if (mMediaItem.getMediaType() == MediaType.ALBUM
                                    || mMediaItem.getMediaType() == MediaType.PLAYLIST) {
                                openVideoPage(mMediaSetDetails);
                            } else if (mMediaItem.getMediaType() == MediaType.TRACK) {
                                mDataManager.getRelatedVideo(mMediaTrackDetails,
                                        mMediaItem, MediaDetailsFragment.this);
                            }
                        }
                    }
                },200);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void populateUserControls(final Video video) {

		// Set Favorite Button
		favButton = (TextView) headerView.findViewById(
				R.id.button_media_details_heart);
		favButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.isSelected()) {
					favButton.setCompoundDrawablesWithIntrinsicBounds(
							whiteHeart, null, null, null);
					favButton.setText(Utils.roundTheCount(video.getNumOfFav() - 1));
					mDataManager.removeFromFavorites(
							String.valueOf(mMediaItem.getId()), mediaType,
							MediaDetailsFragment.this);
				} else {
					favButton.setCompoundDrawablesWithIntrinsicBounds(
							blueHeart, null, null, null);
					favButton.setText(Utils.roundTheCount(video.getNumOfFav() + 1));
					mDataManager.addToFavorites(
							String.valueOf(mMediaItem.getId()), mediaType,
							MediaDetailsFragment.this);

					Appirater appirater = new Appirater(mContext);
					appirater.userDidSignificantEvent(true);
				}
				favButton.setEnabled(false);
			}
		});

		favButton.setText(Utils.roundTheCount(video.getNumOfFav()));
		if (video.IsFavorite() || favButton.isSelected()) {
			favButton.setCompoundDrawablesWithIntrinsicBounds(null, blueHeart,
					null, null);
			favButton.setSelected(true);
		} else {
			favButton.setCompoundDrawablesWithIntrinsicBounds(null, whiteHeart,
					null, null);
			favButton.setSelected(false);
		}

		VideoView videoView = (VideoView) headerView.findViewById(
				R.id.videoview_video_details);
		videoView.setVideoURI(Uri.parse(video.getVideoUrl()));
		videoView.start();
	}

	private void setActionButtons() {
		try {

			// Button mActionButtonSaveOffline = (Button)
			// headerView.findViewById(R.id.button_media_details_save_offline);
			LinearLayout mActionButtonSaveOffline = (LinearLayout) headerView
					.findViewById(R.id.rl_media_details_save_offline);

			mActionButtonSaveOffline.setTag(false);
				LanguageTextView tvCacheState = (LanguageTextView) mActionButtonSaveOffline
						.findViewById(R.id.media_details_text_cache_state);
				CustomCacheStateProgressBar progressCacheState = (CustomCacheStateProgressBar) mActionButtonSaveOffline
						.findViewById(R.id.media_details_progress_cache_state);
				progressCacheState.setNotCachedStateVisibility(true);
				if (mMediaItem.getMediaType() == MediaType.TRACK) {
					CacheState cacheState = DBOHandler.getTrackCacheState(
							mContext, "" + mMediaTrackDetails.getId());
					if (cacheState == CacheState.CACHED) {
						mActionButtonSaveOffline.setTag(true);

						Utils.SetMultilanguageTextOnTextView(
								mContext,
								tvCacheState,
								getResources()
										.getString(
												R.string.caching_text_play_offline_capital));
						// tvCacheState.setText(getResources().getString(
						// R.string.caching_text_play_offline_capital));
					} else if (cacheState == CacheState.CACHING
							|| cacheState == CacheState.QUEUED) {
						mActionButtonSaveOffline.setTag(null);

						Utils.SetMultilanguageTextOnTextView(
								mContext,
								tvCacheState,
								getResources().getString(
										R.string.caching_text_saving_capital));
						// tvCacheState.setText(getResources().getString(
						// R.string.caching_text_saving_capital));
					}
					progressCacheState.setCacheState(cacheState);
					progressCacheState.setProgress(DBOHandler
							.getTrackCacheProgress(mContext, ""
									+ mMediaTrackDetails.getId()));
				} else if (mMediaItem.getMediaType() == MediaType.ALBUM) {
					CacheState cacheState = DBOHandler.getAlbumCacheState(
							mContext, "" + mMediaItem.getId());
					if (cacheState == CacheState.CACHED) {
						int trackCacheCount = DBOHandler.getAlbumCachedCount(
								mContext, "" + mMediaItem.getId());
						if (trackCacheCount >= mMediaItem.getMusicTrackCount())
							mActionButtonSaveOffline.setTag(true);

						Utils.SetMultilanguageTextOnTextView(
								mContext,
								tvCacheState,
								getResources()
										.getString(
												R.string.caching_text_play_offline_capital));

					} else if (cacheState == CacheState.CACHING
							|| cacheState == CacheState.QUEUED) {
						mActionButtonSaveOffline.setTag(null);

						Utils.SetMultilanguageTextOnTextView(
								mContext,
								tvCacheState,
								getResources().getString(
										R.string.caching_text_saving_capital));

					}
					progressCacheState.setCacheCountVisibility(true);
					progressCacheState.setCacheCount(""
							+ DBOHandler.getAlbumCachedCount(mContext, ""
									+ mMediaItem.getId()));

					int trackCacheCount = DBOHandler.getAlbumCachedCount(
							mContext, "" + mMediaItem.getId());
					if (trackCacheCount > 0) {
						progressCacheState.setCacheState(CacheState.CACHED);
						Utils.SetMultilanguageTextOnTextView(
								mContext,
								tvCacheState,
								getResources()
										.getString(
												R.string.caching_text_play_offline_capital));
						// tvCacheState.setText(getResources().getString(
						// R.string.caching_text_play_offline_capital));
						if (trackCacheCount >= mMediaItem.getMusicTrackCount()) {
							mActionButtonSaveOffline.setTag(true);
						}
						// }
					} else {
						progressCacheState.setCacheState(cacheState);
					}
				} else if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
					CacheState cacheState = DBOHandler.getPlaylistCacheState(
							mContext, "" + mMediaItem.getId());
					// System.out.println("PLAYLIST :::::::::::::::::::::::: " +
					// cacheState);
					if (cacheState == CacheState.CACHED) {

						int trackCacheCount = DBOHandler
								.getPlaylistCachedCount(mContext, ""
										+ mMediaItem.getId());
						if (trackCacheCount >= mMediaItem.getMusicTrackCount())
							mActionButtonSaveOffline.setTag(true);

						Utils.SetMultilanguageTextOnTextView(
								mContext,
								tvCacheState,
								getResources()
										.getString(
												R.string.caching_text_play_offline_capital));

					} else if (cacheState == CacheState.CACHING
							|| cacheState == CacheState.QUEUED) {
						mActionButtonSaveOffline.setTag(null);

						Utils.SetMultilanguageTextOnTextView(
								mContext,
								tvCacheState,
								getResources().getString(
										R.string.caching_text_saving_capital));
						// tvCacheState.setText(getResources().getString(
						// R.string.caching_text_saving_capital));
					}
					progressCacheState.setCacheCountVisibility(true);
					progressCacheState.setCacheCount(""
							+ DBOHandler.getPlaylistCachedCount(mContext, ""
									+ mMediaItem.getId()));
					progressCacheState.setCacheState(cacheState);
				}

			mActionButtonSaveOffline
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								if (v.getTag() != null) {
									if ((Boolean) v.getTag()) {
										if (mMediaItem.getMediaType() == MediaType.ALBUM
												|| mMediaItem.getMediaType() == MediaType.PLAYLIST) {
											CustomAlertDialog alertDialogBuilder = new CustomAlertDialog(
													getActivity());
											if (mMediaItem.getMediaType() == MediaType.ALBUM)
												alertDialogBuilder
														.setMessage(R.string.already_offline_message_album);
											else
												alertDialogBuilder
														.setMessage(R.string.already_offline_message_playlist);
											alertDialogBuilder
													.setNegativeButton(
															R.string.ok, null);
											alertDialogBuilder.show();
										} else {
											CacheManager.removeTrackFromCache(
													getActivity(),
													mMediaItem.getId(),
													MediaContentType.MUSIC);
										}
									} else {
												saveOfflineOptionSelected(v,
                                                        mMediaItem,
                                                        mMediaSetDetails,
                                                        mMediaTrackDetails,
                                                        false);
									}
								}
							} catch (Exception e) {
								Logger.printStackTrace(e);
							}
						}
					});
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	private void showPlaylistDialog(List<Track> tracks) {
		if (getActivity() != null && !getActivity().isFinishing()) {

			boolean isFromLoadMenu = false;
			PlaylistDialogFragment editNameDialog = PlaylistDialogFragment
					.newInstance(tracks, isFromLoadMenu, mFlurryEventName);
			editNameDialog.show(mFragmentManager, "PlaylistDialogFragment");
		}
	}

    private static class ViewHolder {
		RelativeLayout layout;
		TextView textTrackName;
		// TextView textTrackNameEnglish;
		ImageButton buttonPlay, player_queue_line_button_more;
		// ImageView media_image;
		CustomCacheStateProgressBar progressCacheState;

	}

	private class TracksAdapter extends BaseAdapter {

		private List<Track> mTracks;
		private LayoutInflater mInflater;
		// MediaSetDetails mediaSetDetailsTrack;
		boolean isEnglishHindi;

		// private int saveoffline_drawable =
		// R.drawable.icon_media_details_saving;
		// private String text_save_offline = "";

		public TracksAdapter(MediaSetDetails mediaSetDetails) {
            if(mediaSetDetails==null)
                mTracks=new ArrayList<Track>();
			else
			    mTracks = mediaSetDetails.getTracks();
			mInflater = (LayoutInflater) getActivity().getApplicationContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
					.getInstance(getActivity());
			isEnglishHindi = (mApplicationConfigurations
					.getUserSelectedLanguage() == 0 || mApplicationConfigurations
					.getUserSelectedLanguage() == 1);

		}

		@Override
		public int getCount() {
			if(mTracks!=null)
				return mTracks.size();
			else
				return 0;

		}

		@Override
		public Object getItem(int position) {
			return mTracks.get(position);
		}

		@Override
		public long getItemId(int position) {
			return mTracks.get(position).getId();
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			ViewHolder viewHolder;
			// create view if not exist.
			if (convertView == null) {
				if (isEnglishHindi)
					convertView = mInflater.inflate(
							R.layout.list_item_media_details_track_english,
							parent, false);
				else
					convertView = mInflater.inflate(
							R.layout.list_item_media_details_track, parent,
							false);

				viewHolder = new ViewHolder();
				viewHolder.layout = (RelativeLayout) convertView
						.findViewById(R.id.media_details_track);

				viewHolder.player_queue_line_button_more = (ImageButton) convertView
						.findViewById(R.id.player_queue_line_button_more);

				viewHolder.textTrackName = (TextView) convertView
						.findViewById(R.id.media_details_track_name);

				// viewHolder.textTrackNameEnglish = (LanguageTextView)
				// convertView
				// .findViewById(R.id.media_details_track_name_english);

				viewHolder.buttonPlay = (ImageButton) convertView
						.findViewById(R.id.media_details_track_button_play);
				// viewHolder.media_image = (ImageView) convertView
				// .findViewById(R.id.media_image);
				viewHolder.progressCacheState = (CustomCacheStateProgressBar) convertView
						.findViewById(R.id.media_details_progress_cache_state);

				viewHolder.player_queue_line_button_more
						.setImageResource(R.drawable.abc_ic_menu_moreoverflow_mtrl_alpha_gray);
				// if (isEnglish) {
				// viewHolder.textTrackNameEnglish
				// .setTextColor(getActivity()
				// .getResources()
				// .getColor(
				// R.color.search_fragment_result_list_item_bottom_text_color));
				// } else {
				viewHolder.textTrackName
						.setTextColor(getActivity()
								.getResources()
								.getColor(
										R.color.search_fragment_result_list_item_bottom_text_color));
				// }
				convertView.setTag(R.id.view_tag_view_holder, viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView
						.getTag(R.id.view_tag_view_holder);
			}
            Logger.e("position",""+position);
			// populate the view from the Track's list.
			final Track track = mTracks.get(position);
			// stores the object in the view.
			viewHolder.layout.setTag(R.id.view_tag_object, track);
			// if (isEnglish) {
			// viewHolder.textTrackNameEnglish.setVisibility(View.VISIBLE);
			// viewHolder.textTrackName.setVisibility(View.GONE);
			// viewHolder.textTrackNameEnglish
			// .setText(track.getTitle().trim());
			// } else {
			// viewHolder.textTrackNameEnglish.setVisibility(View.GONE);
			viewHolder.textTrackName.setVisibility(View.VISIBLE);

			viewHolder.textTrackName.setText(track.getTitle().trim());
			// viewHolder.textTrackName.setText(track.getTitle().trim().toCharArray(),0,track.getTitle().trim().length());

			// }
			viewHolder.progressCacheState
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {

//							CacheState cacheState = DBOHandler
//									.getTrackCacheState(getActivity()
//											.getApplicationContext(), ""
//											+ track.getId());
//							if (cacheState != CacheState.CACHED
//									&& cacheState != CacheState.CACHING) {
//								Utils.makeText(getActivity(),
//										getString(R.string.cahing_start),
//										Toast.LENGTH_SHORT).show();
//							}
							View rowView = (View) view.getParent();
							Track track = (Track) rowView
									.getTag(R.id.view_tag_object);

								cacheSong(track);
						}
					});

//			Logger.i("View size",
//					String.valueOf(convertView.getMeasuredHeight()));
			viewHolder.layout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					View rowView = (View) view;
					Track track = (Track) rowView.getTag(R.id.view_tag_object);
					List<Track> tracks = new ArrayList<Track>();
					tracks.add(track);
                    playButtonClickActivity(tracks, null, null);
					// ((MediaDetailsActivityNew) getActivity())
					// .addToQueueButtonClickActivity(tracks, null, null);

					Map<String, String> reportMap = new HashMap<String, String>();

					reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong
							.toString(), track.getTitle());
					reportMap.put(mMediaItem.getMediaType().toString(),
							Utils.toWhomSongBelongto(mMediaItem));
					reportMap.put(FlurryConstants.FlurryKeys.Source.toString(),
							mFlurrySourceDescription);
					reportMap.put(
							FlurryConstants.FlurryKeys.SubSection.toString(),
							mFlurrySubSectionDescription);

					Analytics.logEvent(
							FlurryConstants.FlurryEventName.SongSelectedForPlay
									.toString(), reportMap);

				}
			});


			viewHolder.buttonPlay.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					Logger.v(TAG, "Play button was clicked");
					View rowView = (View) view.getParent();
					Track track = (Track) rowView.getTag(R.id.view_tag_object);
					List<Track> tracks = new ArrayList<Track>();
					tracks.add(track);
                    addToQueueButtonClickActivity(tracks, null, null);

					Map<String, String> reportMap = new HashMap<String, String>();

					reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong
							.toString(), track.getTitle());
					reportMap.put(mMediaItem.getMediaType().toString(),
							Utils.toWhomSongBelongto(mMediaItem));
					reportMap.put(FlurryConstants.FlurryKeys.Source.toString(),
							mFlurrySourceDescription);
					reportMap.put(
							FlurryConstants.FlurryKeys.SubSection.toString(),
							mFlurrySubSectionDescription);

					Analytics.logEvent(
							FlurryConstants.FlurryEventName.SongSelectedForPlay
									.toString(), reportMap);
				}
			});

			viewHolder.player_queue_line_button_more
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(final View view) {
							View rowView = (View) view.getParent();
							Track track = (Track) rowView
									.getTag(R.id.view_tag_object);


							String[] arr_options = new String[] {
									 getResources()
									 .getString(
									 R.string.full_player_setting_menu_Trend_This),
									getResources().getString(
											R.string.general_download),

									getResources()
											.getString(
													R.string.media_details_custom_dialog_long_click_add_to_queue),
									getResources()
											.getString(
													R.string.media_details_custom_dialog_long_click_view_details) };
							int[] arr_images = new int[] {
									// R.drawable.icon_general_download_grey,
									R.drawable.icon_media_details_trend_grey,
									R.drawable.icon_general_download_grey,

									R.drawable.icon_media_details_add_to_queue_grey,
									R.drawable.icon_view_detail };

							QuickActionMediaDetail quickaction = new QuickActionMediaDetail(
									getActivity(), arr_options, arr_images,
									track);
							quickaction
									.setOnMediaSelectedListener(mOnLoginOptionSelectedListener);
							quickaction.show(view);
							view.setEnabled(false);
							quickaction
									.setOnDismissListener(new QuickActionMediaDetail.OnDismissListener() {
										@Override
										public void onDismiss() {
											view.setEnabled(true);
										}
									});
						}
					});

			CacheState cacheState = DBOHandler.getTrackCacheState(getActivity()
					.getApplicationContext(), "" + track.getId());
			viewHolder.progressCacheState.setNotCachedStateVisibility(true);
			viewHolder.progressCacheState.setisDefualtImageGray(true);
			viewHolder.progressCacheState.showProgressOnly(true);
			viewHolder.progressCacheState.setCacheState(cacheState);
			// viewHolder.progressCacheState.setProgress(DBOHandler
			// .getTrackCacheProgress(getActivity()
			// .getApplicationContext(), "" + track.getId()));
			if (cacheState == CacheState.CACHED) {

				boolean stateChanged = DBOHandler
						.checkTrackInTrackListTableAndInsert(mContext, ""
								+ track.getId(), "" + mMediaItem.getId());
				if (stateChanged) {
					Intent TrackCached = new Intent(
							CacheManager.ACTION_TRACK_CACHED);
					Logger.i("Update Cache State",
							" SENDING BROADCAST TRACK_CACHED");
					getActivity().sendBroadcast(TrackCached);
				}
			}
			// } else{
			// }
			return convertView;
		}

	}

	/**
	 * Handles changes in the favorite state of Media Items, marks the button
	 * accordingly.
	 */
	private static final class MediaItemFavoriteStateReceiver extends
			BroadcastReceiver {

		private final WeakReference<MediaDetailsFragment> mediaDetailsFragmentReference;

		MediaItemFavoriteStateReceiver(MediaDetailsFragment mediaDetailsFragment) {
			this.mediaDetailsFragmentReference = new WeakReference<MediaDetailsFragment>(
					mediaDetailsFragment);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (ActionDefinition.ACTION_MEDIA_ITEM_FAVORITE_STATE_CHANGED
					.equalsIgnoreCase(intent.getAction())) {

				Bundle extras = intent.getExtras();
				MediaItem mediaItem = (MediaItem) extras
						.getSerializable(ActionDefinition.EXTRA_MEDIA_ITEM);
				boolean isFavorite = extras
						.getBoolean(ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_IS_FAVORITE);
				int count = extras
						.getInt(ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_COUNT);

				MediaDetailsFragment mediaDetailsFragment = mediaDetailsFragmentReference
						.get();
				if (mediaDetailsFragment == null) {
					return;
				}

				if (count == 0
						&& mediaDetailsFragment.mMediaItem.getMediaType() == MediaType.TRACK
						&& mediaDetailsFragment.mMediaTrackDetails != null) {
					if (isFavorite)
						count = mediaDetailsFragment.mMediaTrackDetails
								.getNumOfFav() + 1;
					else
						count = mediaDetailsFragment.mMediaTrackDetails
								.getNumOfFav() - 1;
				}

				if (mediaItem.getId() == mediaDetailsFragment.mMediaItem
						.getId()
						&& mediaItem.getMediaType() == mediaDetailsFragment.mMediaItem
								.getMediaType()) {
					try {
						if (mediaItem.getMediaType() == MediaType.ALBUM
								|| mediaDetailsFragment.mMediaItem
										.getMediaType() == MediaType.PLAYLIST) {
							if (mediaDetailsFragment.mMediaSetDetails != null) {
								mediaDetailsFragment.mMediaSetDetails
										.setNumOfFav(count);

							}

						} else if (mediaDetailsFragment.mMediaItem
								.getMediaType() == MediaType.TRACK) {
							mediaDetailsFragment.mMediaTrackDetails
									.setNumOfFav(count);
							mediaDetailsFragment.mMediaTrackDetails
									.setIsFavorite(isFavorite);
						}
					} catch (Exception e) {
						Logger.e(getClass().getName() + ":1511", e.toString());
					}

					try {
						if (isFavorite) {
							mediaDetailsFragment.favButton
									.setCompoundDrawablesWithIntrinsicBounds(
											mediaDetailsFragment.blueHeart,
											null, null, null);
							mediaDetailsFragment.favButton_main
									.setCompoundDrawablesWithIntrinsicBounds(
											null,
											context.getResources()
													.getDrawable(
															R.drawable.icon_main_player_favorites_blue),
											null, null);
							mediaDetailsFragment.favButton_main
									.setSelected(true);
							mediaDetailsFragment.favButton.setText(Utils
									.roundTheCount(count));

						} else {
							mediaDetailsFragment.favButton
									.setCompoundDrawablesWithIntrinsicBounds(
											mediaDetailsFragment.whiteHeart,
											null, null, null);
							mediaDetailsFragment.favButton_main
									.setCompoundDrawablesWithIntrinsicBounds(
											null,
											context.getResources()
													.getDrawable(
															R.drawable.icon_main_player_favorites_white),
											null, null);
							mediaDetailsFragment.favButton_main
									.setSelected(false);
							mediaDetailsFragment.favButton.setText(Utils
									.roundTheCount(count));
						}
						mediaDetailsFragment.favButton_main.setEnabled(true);
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
				}
			}
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (rootView != null) {
			try {
				int version = Integer.parseInt(""
						+ android.os.Build.VERSION.SDK_INT);
				Utils.unbindDrawables(rootView, version);
				Utils.destroyFragment();
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}
	}

	// blurr
	private String bgImgUrl;

	public void setBlurImage(String imgUrl) {

		Utils.clearCache();
		if (imgUrl != null) {
			try {

				Logger.i("setBlurImage", "setBlurImage");
				loadBlurImgBitmap(imgUrl);

			} catch (Exception e) {
				clearBlurBg();
			}
		} else
			clearBlurBg();
	}

	/**
	 * Clear background Bur Image when song switched
	 */
	private void clearBlurBg() {
		try {
			// RelativeLayout rlFlipView = (RelativeLayout) rootView
			// .findViewById(R.id.main_player);

			RelativeLayout rl_main = (RelativeLayout) headerView
					.findViewById(R.id.rl_main);
			rl_main.setBackgroundColor(getActivity().getResources().getColor(
					R.color.application_background_grey));
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		Utils.clearCache();
	}

	private GetAndSetBlurImage getAndSetBlurImage;

	private String loadedURL = "";
	private Drawable blurbitmap;
	private String url;

	// static Bitmap normalBitmap;

	private void loadBlurImgBitmap(final String url) {
		Display dis = getActivity().getWindowManager().getDefaultDisplay();
		int orgWidth = dis.getWidth();
		if (!TextUtils.isEmpty(url))
			PicassoUtil.with(getActivity()).loadWithoutConfig8888(url,
					orgWidth, orgWidth, target);
	}

	private PicassoTarget target = new PicassoTarget() {

		@Override
		public void onPrepareLoad(Drawable arg0) {

		}

		@Override
		public void onBitmapLoaded(Bitmap arg0, LoadedFrom arg1) {
			// loadedURL = url;
			Logger.i("Bitmap", "BlurImgBitmap:" + arg0);
			getAndSetBlurImage = new GetAndSetBlurImage(arg0, url);
			ThreadPoolManager.getInstance().submit(getAndSetBlurImage);
			bgImgUrl = url;
		}

		@Override
		public void onBitmapFailed(Drawable arg0) {
			// TODO Auto-generated method stub
			bgImgUrl = null;
			Logger.i("Bitmap", "Error BlurImgBitmap");
		}
	};

	private void loadBlurBG(final Bitmap bitmap1, final Drawable loadBitmap,
			final String url) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					RelativeLayout rl_main = (RelativeLayout) headerView
							.findViewById(R.id.rl_main);
					if (android.os.Build.VERSION.SDK_INT > 15) {
						rl_main.setBackground(loadBitmap);
					} else {
						rl_main.setBackgroundDrawable(loadBitmap);
					}
					if (blurbitmap != loadBitmap) {
						blurbitmap = loadBitmap;
						loadedURL = url;

					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			}
		});

	}

	private class GetAndSetBlurImage implements Runnable {
		Bitmap bitmap;

		String url;

		public GetAndSetBlurImage(Bitmap bitmap, String url) {
			this.bitmap = bitmap;
			this.url = url;
		}

		protected Drawable doInBackground(String... urls) {
			try {
				int oldBitmpWidth = bitmap.getWidth();
				Display dis = getActivity().getWindowManager()
						.getDefaultDisplay();
				float screenWidthRatio = (float) dis.getHeight()
						/ (float) dis.getWidth();
				int newBitmpWidth = (int) (bitmap.getWidth() / screenWidthRatio);
				bitmap = Bitmap.createBitmap(bitmap,
						((oldBitmpWidth - newBitmpWidth) / 2), 0,
						newBitmpWidth, bitmap.getHeight());

				Bitmap loadBitmap = bitmap;
				try {
					loadBitmap = fastblur1(loadBitmap,
							Constants.BLUR_IMG_RADIUS);
				} catch (OutOfMemoryError e) {
					System.gc();
				} catch (Exception e) {
					loadBitmap = bitmap;
				}
				Bitmap displayBitmp = loadBitmap;
				Drawable dr = new BitmapDrawable(displayBitmp);
				return dr;
			} catch (java.lang.Error e) {
			}
			return null;
		}

		@Override
		public void run() {
			try {
				final Drawable loadBitmap = doInBackground();
				if (loadBitmap != null && getActivity() != null)
					loadBlurBG(bitmap, loadBitmap, url);
				else {
					loadedURL = null;
					blurbitmap = null;
					// normalBitmap=null;
				}
			} catch (Exception e) {
			}
		}

	}

	private Bitmap fastblur1(Bitmap input, int radius) {
		try {
			input = getResizedBitmap(input, input.getWidth() / 2,
					input.getHeight() / 2);
			// System.gc();
			RenderScript rsScript = RenderScript.create(getActivity());
			Allocation alloc = Allocation.createFromBitmap(rsScript, input);

			ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rsScript,
					Element.U8_4(rsScript));
			blur.setRadius(radius);
			blur.setInput(alloc);
			Bitmap result;

			try {
				result = Bitmap.createBitmap(input.getWidth(),
						input.getHeight(), Utils.bitmapConfig8888);
			} catch (OutOfMemoryError e) {
				result = Bitmap.createBitmap(input.getWidth() / 2,
						input.getHeight() / 2, Utils.bitmapConfig8888);
			}
			Allocation outAlloc = Allocation.createFromBitmap(rsScript, result);

			blur.forEach(outAlloc);
			outAlloc.copyTo(result);

			rsScript.destroy();
			return result;
		} catch (Exception e) {
			Logger.printStackTrace(e);
			return input;
		}
	}

	private Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// CREATE A MATRIX FOR THE MANIPULATION
		Matrix matrix = new Matrix();
		// RESIZE THE BIT MAP
		matrix.postScale(scaleWidth, scaleHeight);

		// "RECREATE" THE NEW BITMAP
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
				matrix, false);
		return resizedBitmap;
	}

    private MediaDetailsActivity mediaDetailsActivity;

    public void setMediaDetailsActivity(MediaDetailsActivity mediaDetailsActivity){
        this.mediaDetailsActivity = mediaDetailsActivity;
    }

    public MediaDetailsActivityNew mediaDetailsActivityNew;
    public void setMediaDetailsActivityNew(MediaDetailsActivityNew mediaDetailsActivityNew){
        this.mediaDetailsActivityNew = mediaDetailsActivityNew;
    }

    // acitivty methods
    public void updateTitleColor(ColorDrawable cd, boolean needalpha) {
        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        if (needalpha)
            cd.setAlpha(160);
        actionBar.setBackgroundDrawable(cd);

    }

    // Left panel buttons clicks
    public void playButtonClickActivity(List<Track> trackList,
                                        String flurryEventName, String flurrySourceSection) {
        if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
            for (Track track : trackList) {
                track.setTag(mMediaItem);
            }
        } else if (mMediaItem.getMediaType() == MediaType.ALBUM) {
            for (Track track : trackList) {
                track.setTag(mMediaItem);
            }
        }
        // mPlayerBarFragment = getPlayerBar();
        ((MainActivity) getActivity()). mPlayerBarFragment.playNow(trackList, flurryEventName,
                flurrySourceSection);
    }

    public void addToPlaylistButtonClickActivity(List<Track> trackList) {
        ((MainActivity) getActivity()). mPlayerBarFragment = ((MainActivity) getActivity()).getPlayerBar();
    }

    public boolean isMediaInsideOpen;
    public boolean isVideoInsideOpen;
    private FragmentManager mFragmentManager;
//    private ArrayList<String> listTitle = new ArrayList<String>();
    /**
     * Method to open Related Videos from ALBUM or PLAYLIST
     *
     * @param mediaSetDetails
     */
    public void openVideoPage(MediaSetDetails mediaSetDetails) {
        try {
            isVideoInsideOpen = true;
            Bundle detailsDataVideos = new Bundle();
            int listSize = mediaSetDetails.getVideos().size();
            for (int i = 0; i < listSize; i++) {
                mediaSetDetails.getVideos().get(i)
                        .setMediaContentType(MediaContentType.VIDEO);
                mediaSetDetails.getVideos().get(i)
                        .setMediaType(MediaType.VIDEO);
            }
            detailsDataVideos.putSerializable(
                    MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS,
                    (Serializable) mediaSetDetails.getVideos());
            String title = "";
            if (mMediaSetDetails != null) {
                title= mMediaSetDetails
                        .getTitle();
            }else if (mMediaSetDetails != null) {
                title=mMediaTrackDetails.getTitle();
            }

            detailsDataVideos.putString("title", title);

            detailsDataVideos
                    .putString(
							MediaTileGridFragment.FLURRY_SUB_SECTION_DESCRIPTION,
							FlurryConstants.FlurrySubSectionDescription.VideoRelatedAudio
									.toString());
			if(mediaDetailsActivityNew!=null)
				mediaDetailsActivityNew.openVideoPage(detailsDataVideos,mOnMediaItemOptionSelectedListener);
			else{
                ((MediaDetailsActivity)getActivity()).openVideoPage(detailsDataVideos,mOnMediaItemOptionSelectedListener);
//				MediaTileGridFragment mediaTileGridFragment = new MediaTileGridFragment();
//				mediaTileGridFragment.setOnMediaItemOptionSelectedListener(mOnMediaItemOptionSelectedListener);
//				mediaTileGridFragment.setArguments(detailsDataVideos);
//				mediaTileGridFragment.setIsMarginTopRequire(false);
//
//				FragmentTransaction fragmentTransaction = mFragmentManager
//						.beginTransaction();
//				fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
//						R.anim.slide_left_exit, R.anim.slide_right_enter,
//						R.anim.slide_right_exit);
//
//				fragmentTransaction.add(R.id.main_fragmant_container_media_detail,
//						mediaTileGridFragment,"video");
//				fragmentTransaction.addToBackStack("video");
//				fragmentTransaction.commitAllowingStateLoss();
			}

            if(rootViewParent!=null){
//                FrameLayout layout = (FrameLayout) rootViewParent.findViewById(R.id.main_fragmant_container_media_detail);
//                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layout
//                        .getLayoutParams();
//                params.setMargins(0, ((MainActivity)getActivity()).getActionBarHeight(), 0, 0);
            }else{
                FrameLayout layout = (FrameLayout) getActivity().findViewById(R.id.main_fragmant_container_media_detail);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layout
                        .getLayoutParams();
                params.setMargins(0, ((MainActivity)getActivity()).getActionBarHeight(), 0, 0);
            }


        } catch (Exception e) {
            Logger.e(getClass().getName() + ":187", e.toString());
            e.printStackTrace();
        }
    }
    public void openCommentsPage(MediaItem mediaItem, int numOfComments) {

        Bundle detailsDataTrack = new Bundle();
        detailsDataTrack.putSerializable(
                CommentsActivity.EXTRA_DATA_MEDIA_ITEM,
                (Serializable) mediaItem);
        detailsDataTrack.putBoolean(CommentsActivity.EXTRA_DATA_DO_SHOW_TITLE,
                false);
        detailsDataTrack.putString(CommentsActivity.FLURRY_SOURCE_SECTION,
                mFlurrySubSectionDescription);
        ((MainActivity) getActivity()).isSkipResume=true;
        Intent commentsIntent = new Intent(getActivity().getApplicationContext(),
                CommentsActivity.class);
        commentsIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        commentsIntent.putExtras(detailsDataTrack);
        ((MainActivity) getActivity()).startActivityForResult(commentsIntent, 100);
    }

    public void addTracksToQueue(final MediaSetDetails details) {
        try {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (((MainActivity) getActivity()).mPlayerBarFragment.isPlayerServiceAvailable())
                            ((MainActivity) getActivity()).mPlayerBarFragment.playNow(details.getTracks(),
                                    null, null);
                        else
                            handler.postDelayed(this, 1000);
                    } catch (Exception e) {
                    }
                }
            }, 1000);
            // mPlayerBarFragment.playNow(details.getTracks(), null, null);
            getBundle().remove("add_to_queue");
            String subtitle = details.getReleaseYear() + " | "
                    + details.getLanguage() + " | " + details.getNumOfPlays()
                    + " songs";
            ((MainActivity) getActivity()).showBackButtonWithTitleMediaDetail(details.getTitle(), subtitle);

			// Patibandha chromecast
			UpdateMainActivityActionbar("detailpage");


        } catch (Exception e) {
            Logger.e(TAG, e.toString());
        }
    }

    public void updateTitleSubtitle(String title, String subtitle,int alpha_current) {
        if (title != null) {

//            Logger.e("listTitle add", "" + listTitle);
            ((MainActivity) getActivity()). showBackButtonWithTitleMediaDetail(title, subtitle);

			// Patibandha chromecast
			UpdateMainActivityActionbar("detailpage");

        }
    }

    public void updateTitle(MediaTrackDetails mediaTrackDetails) {

        if (/*mTitleBarText.getText().length() == 0 && */mediaTrackDetails != null
                && mMediaItem.getMediaContentType() == MediaContentType.MUSIC) {
            mMediaItem = new MediaItem(mediaTrackDetails.getId(),
                    mediaTrackDetails.getTitle(),
                    mediaTrackDetails.getAlbumName(), "",
                    mediaTrackDetails.getImageUrl(),
                    mediaTrackDetails.getBigImageUrl(),
                    MediaContentType.MUSIC.toString(), 0,
                    mediaTrackDetails.getAlbumId());

            String subtitle = mediaTrackDetails.getReleaseYear() + " | "
                    + mediaTrackDetails.getLanguage() + " | "
                    + mediaTrackDetails.getNumOfPlays() + " songs";
            if (mMediaItem.getMediaType() == MediaType.ALBUM
                    && !TextUtils.isEmpty(mMediaItem.getAlbumName()))
                ((MainActivity) getActivity()).showBackButtonWithTitleMediaDetail(mMediaItem.getAlbumName(),
                        subtitle);
            else
                ((MainActivity) getActivity()).showBackButtonWithTitleMediaDetail(mMediaItem.getTitle(),
                        subtitle);

			// Patibandha chromecast
			UpdateMainActivityActionbar("detailpage");

            Track track = new Track(mMediaItem.getId(), mMediaItem.getTitle(),
                    mMediaItem.getAlbumName(), mMediaItem.getArtistName(),
                    mMediaItem.getImageUrl(), mMediaItem.getBigImageUrl(),
                    mMediaItem.getImages(), mMediaItem.getAlbumId());
            if (mediaTrackDetails != null && mMediaItem.getImages() == null
                    && mMediaItem.getId() == mediaTrackDetails.getId())
                track.setImagesUrlArray(mediaTrackDetails.getImages());
            // getPlayerBar().updateTrackDetails(track);
            final List<Track> tracks = new ArrayList<Track>();
            tracks.add(track);
            // getPlayerBar().addToQueue(tracks, null, null);
            if (getBundle().getBoolean("add_to_queue", false)) {
                if (((MainActivity) getActivity()).mPlayerBarFragment.isPlayerServiceAvailable()) {
                    ((MainActivity) getActivity()).mPlayerBarFragment.playNow(tracks, null, null);
                } else {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                if (((MainActivity) getActivity()).mPlayerBarFragment
                                        .isPlayerServiceAvailable())
                                    ((MainActivity) getActivity()).mPlayerBarFragment.playNow(tracks, null,
                                            null);
                                else
                                    handler.postDelayed(this, 1000);
                            } catch (Exception e) {
                            }
                        }
                    }, 1000);
                }
				getBundle().remove("add_to_queue");
            } else
                ((MainActivity) getActivity()).mPlayerBarFragment.addToQueue(tracks, null, null);
        } else if (mediaTrackDetails != null
                && mMediaItem.getMediaContentType() == MediaContentType.MUSIC) {
            mMediaItem = new MediaItem(mediaTrackDetails.getId(),
                    mediaTrackDetails.getTitle(),
                    mediaTrackDetails.getAlbumName(), "",
                    mediaTrackDetails.getImageUrl(),
                    mediaTrackDetails.getBigImageUrl(),
                    MediaContentType.MUSIC.toString(), 0,
                    mediaTrackDetails.getAlbumId());
            Track track = new Track(mMediaItem.getId(), mMediaItem.getTitle(),
                    mMediaItem.getAlbumName(), mMediaItem.getArtistName(),
                    mMediaItem.getImageUrl(), mMediaItem.getBigImageUrl(),
                    mMediaItem.getImages(), mMediaItem.getAlbumId());
            // getPlayerBar().updateTrackDetails(track);
            final List<Track> tracks = new ArrayList<Track>();
            tracks.add(track);
            if (getBundle().getBoolean("add_to_queue", false)) {
                if (((MainActivity) getActivity()).mPlayerBarFragment.isPlayerServiceAvailable()) {
                    ((MainActivity) getActivity()).mPlayerBarFragment.playNow(tracks, null, null);
                } else {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (((MainActivity) getActivity()).mPlayerBarFragment
                                        .isPlayerServiceAvailable())
                                    ((MainActivity) getActivity()).mPlayerBarFragment.playNow(tracks, null,
                                            null);
                                else
                                    handler.postDelayed(this, 1000);
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }, 1000);
                }
            } else
                ((MainActivity) getActivity()).mPlayerBarFragment.addToQueue(tracks, null, null);
			getBundle().remove("add_to_queue");
        }
    }

    /**
     * Method to open Related Videos from TRACK
     *
     * @param mediaItemsVideos
     */
    public void openVideoPageTrack(List<MediaItem> mediaItemsVideos) {
        // if(mMediaItemTrack!=null)
        // mTitleBarText.setText(mMediaItemTrack.getTitle());
        // else
        // mTitleBarText.setText(mMediaItem.getTitle());
        isVideoInsideOpen = true;
        Bundle detailsDataVideos = new Bundle();
        int listSize = mediaItemsVideos.size();
        for (int i = 0; i < listSize; i++) {
            mediaItemsVideos.get(i).setMediaContentType(MediaContentType.VIDEO);
            mediaItemsVideos.get(i).setMediaType(MediaType.VIDEO);
        }
        detailsDataVideos.putSerializable(
				MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS,
				(Serializable) mediaItemsVideos);


        String title = "";
        if (mMediaTrackDetails != null) {
            title= mMediaTrackDetails.getTitle();
        }

        detailsDataVideos.putString("title", title);

		if(mediaDetailsActivityNew!=null)
			mediaDetailsActivityNew.openVideoPage(detailsDataVideos,mOnMediaItemOptionSelectedListener);
		else{
            ((MediaDetailsActivity)getActivity()).openVideoPage(detailsDataVideos,mOnMediaItemOptionSelectedListener);
//			MediaTileGridFragment mediaTileGridFragment = new MediaTileGridFragment();
//			mediaTileGridFragment.setOnMediaItemOptionSelectedListener(mOnMediaItemOptionSelectedListener);
//			mediaTileGridFragment.setArguments(detailsDataVideos);
//			mediaTileGridFragment.setIsMarginTopRequire(false);
//
//			FragmentTransaction fragmentTransaction = mFragmentManager
//					.beginTransaction();
//			fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
//					R.anim.slide_left_exit, R.anim.slide_right_enter,
//					R.anim.slide_right_exit);
//
//			fragmentTransaction.add(R.id.main_fragmant_container_media_detail,
//					mediaTileGridFragment,"video");
//			fragmentTransaction.addToBackStack("video");
//			fragmentTransaction.commitAllowingStateLoss();
		}

        if(rootViewParent!=null){
//            FrameLayout layout = (FrameLayout) rootViewParent.findViewById(R.id.main_fragmant_container_media_detail);
//            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layout
//                    .getLayoutParams();
//            params.setMargins(0, ((MainActivity)getActivity()).getActionBarHeight(), 0, 0);
        }else{
            FrameLayout layout = (FrameLayout) getActivity().findViewById(R.id.main_fragmant_container_media_detail);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layout
                    .getLayoutParams();
            params.setMargins(0, ((MainActivity)getActivity()).getActionBarHeight(), 0, 0);
        }

//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setBackgroundDrawable(cd_main);
    }

    public void saveOfflineOptionSelected(View view, MediaItem mediaItem,
                                          MediaSetDetails mediaSetDetails,
                                          MediaTrackDetails mediaTrackDetails, boolean isProgressOnly) {
        Track track = null;

        if (mediaTrackDetails != null) {
            track = new Track(mediaTrackDetails.getId(),
                    mediaTrackDetails.getTitle(),
                    mediaTrackDetails.getAlbumName(),
                    mediaTrackDetails.getSingers(),
                    mediaTrackDetails.getImageUrl(),
                    mediaTrackDetails.getBigImageUrl(),
                    mediaTrackDetails.getImages(),
                    mediaTrackDetails.getAlbumId());
            // items.add(new CacheItem(track.getId(), CacheState.NOT_CACHED));
        }
        if (!isProgressOnly) {

            if (MediaCachingTaskNew.isEnabled) {
                if (mediaItem.getMediaType() == MediaType.PLAYLIST
                        || mediaItem.getMediaType() == MediaType.ALBUM) {
                    try {
                        List<Track> tracks = mediaSetDetails.getTracks();
                        if (tracks != null && tracks.size() > 0) {
                            // for (Track track1 : tracks) {
                            // track1.setTag(mediaItem);
                            // }
                            for (int i = 0; i < tracks.size(); i++) {
                                tracks.get(i).setTag(mediaItem);
                            }
                            CacheManager.saveAllTracksOfflineAction(getActivity(),
                                    tracks);
                        }
                    } catch (Exception e) {
                        Logger.printStackTrace(e);
                    }
                } else {
                    CacheManager.saveOfflineAction(getActivity(), mediaItem, track);
                }
            } else {
                CacheManager.saveOfflineAction(getActivity(), mediaItem, track);
            }
            if (track != null)
                Utils.saveOfflineFlurryEvent(getActivity(),
                        FlurryConstants.FlurryCaching.SongDetails.toString(),
                        mediaItem);
            else
                Utils.saveOfflineFlurryEvent(getActivity(),
                        FlurryConstants.FlurryCaching.AlbumDetails.toString(),
                        mediaItem);
        }
    }

    public void addToQueueButtonClickActivity(List<Track> trackList,
                                              String flurryEventName, String flurrySourceSection) {
        if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
            for (Track track : trackList) {
                track.setTag(mMediaItem);
            }
        } else if (mMediaItem.getMediaType() == MediaType.ALBUM) {
            for (Track track : trackList) {
                track.setTag(mMediaItem);
            }
        }
        // mPlayerBarFragment = getPlayerBar();
        ((MainActivity) getActivity()).mPlayerBarFragment.addToQueue(trackList, flurryEventName,
                flurrySourceSection);
    }
    public void cacheSong(Track track) {
        MediaItem mediaItem = new MediaItem(track.getId(), track.getTitle(),
                track.getAlbumName(), track.getArtistName(),
                track.getImageUrl(), track.getBigImageUrl(),
                MediaType.TRACK.toString(), 0, 0, track.getImages(),
                track.getAlbumId());

        // new MediaCachingTask(MediaDetailsActivity.this,
        // mediaItem, track).execute();
        CacheManager.saveOfflineAction(getActivity(), mediaItem,
                track);

        Utils.saveOfflineFlurryEvent(getActivity(),
                FlurryConstants.FlurryCaching.LongPressMenuSong.toString(),
                mediaItem);

        Vector<CacheItem> items = new Vector<MediaDetailsFragment.CacheItem>();
        items.add(new CacheItem(track.getId(), CacheState.NOT_CACHED));
    }

//    public void setTitle(){
//        if (listTitle.size() > 0) {
//
//
//            ((MainActivity) getActivity()).showBackButtonWithTitleMediaDetail(
//                    listTitle.get(listTitle.size() - 1), "");
//        }
//        ((MainActivity)getActivity()).mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                getActivity().onBackPressed();
//            }
//        });
//    }

    class CacheItem {
        long id;
        CacheState cacheState;

        CacheItem(long id, CacheState cacheState) {
            this.id = id;
            this.cacheState = cacheState;
        }
    }


	private void UpdateMainActivityActionbar(String update) {
		if (mediaDetailsActivityNew == null)
			((MainActivity) getActivity()).updateActionBarforChromecast(update);
	}
}