package com.hungama.myplay.activity.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.HungamaApplication;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.DataBase.CacheState;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.ui.fragments.MediaDetailsFragment;
import com.hungama.myplay.activity.ui.fragments.MediaTileGridFragment;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.ui.widgets.CustomCacheStateProgressBar;
import com.hungama.myplay.activity.ui.widgets.LanguageButtonLollipop;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.QuickActionMediaDetail.OnMediaSelectedListener;
import com.hungama.myplay.activity.util.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for presenting details of the given MediaItem.
 */
public class MediaDetailsActivity extends MainActivity implements
		OnMediaItemOptionSelectedListener, OnMediaSelectedListener {

	private static final String TAG = "MediaDetailsActivity";

	public static final String EXTRA_MEDIA_ITEM = "EXTRA_MEDIA_ITEM";
	public static final String EXTRA_PLAYLIST_ITEM = "EXTRA_PLAYLIST_ITEM";

	public static final String FLURRY_SOURCE_SECTION = "flurry_source_section";

	private FragmentManager mFragmentManager;
	// private PlayerBarFragment mPlayerBarFragment;
	private MediaItem mMediaItem, mMediaItemTrack;

	private TextView mTitleBarText;

	private Dialog dialog;

	private String mFlurrySubSectionDescription;

	private String flurrySourceSection;
	MediaDetailsFragment mediaDetailsFragment;
	private CacheStateReceiver cacheStateReceiver;
	public ArrayList<String> listTitle = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Logger.s("-------------------MediaDetailsActivity onCreate---------------------");

		setOverlayAction();
		// validate calling intent.
		Intent intent = getIntent();
		if (intent == null) {
			Logger.e(TAG, "No intent for the given Activity.");
			return;
		}
		super.onCreate(savedInstanceState);
		Bundle data = intent.getExtras();
		if (data != null && data.containsKey(EXTRA_MEDIA_ITEM)) {
			setContentView(R.layout.activity_main_with_title_transparent);
			// retrieves the given Media item for the activity.
			mMediaItem = (MediaItem) data.getSerializable(EXTRA_MEDIA_ITEM);
		} else {
			Logger.e(TAG, "No MediaItem set for the given Activity.");
			return;
		}
		onCreateCode();
		// getDrawerLayout();
		// getPlayerBar();
		if (mMediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			// SetS title bar
			mTitleBarText = (TextView) findViewById(R.id.main_title_bar_text);
			mTitleBarText.setSelected(true);// xtpl
			mTitleBarText.setText(mMediaItem.getTitle());
//			listTitle.add(mMediaItem.getTitle());

		}

		flurrySourceSection = "No Flurry Source Section";
		if (data != null && data.containsKey(FLURRY_SOURCE_SECTION)) {
			flurrySourceSection = (String) data.get(FLURRY_SOURCE_SECTION);
		}

		if (mMediaItem.getMediaType() == MediaType.ALBUM) {
			mFlurrySubSectionDescription = FlurryConstants.FlurrySubSectionDescription.AlbumDetail
					.toString();
		} else if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
			mFlurrySubSectionDescription = FlurryConstants.FlurrySubSectionDescription.PlaylistDetail
					.toString();
		} else if (mMediaItem.getMediaType() == MediaType.TRACK) {
			mFlurrySubSectionDescription = FlurryConstants.FlurrySubSectionDescription.SongDetail
					.toString();
		}

		mFragmentManager = getSupportFragmentManager();

		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();
		Bundle detailsData = new Bundle();
		detailsData.putSerializable(MediaDetailsFragment.ARGUMENT_MEDIAITEM,
				(Serializable) mMediaItem);
		detailsData.putString(FLURRY_SOURCE_SECTION, flurrySourceSection);

		mediaDetailsFragment = new MediaDetailsFragment();
		mediaDetailsFragment.setArguments(detailsData);
		mediaDetailsFragment.onMediaListener(this);
        mediaDetailsFragment.onMediaItemOptionSelectedListener(this);
		fragmentTransaction.add(R.id.main_fragmant_container_media_detail,
				mediaDetailsFragment, "media_details_fragment");
		if(Constants.IS_COMMITALLOWSTATE)
			fragmentTransaction.commitAllowingStateLoss();
		else
			fragmentTransaction.commit();

		if (cacheStateReceiver == null) {
			cacheStateReceiver = new CacheStateReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(CacheManager.ACTION_CACHE_STATE_UPDATED);
			filter.addAction(CacheManager.ACTION_TRACK_CACHED);
			filter.addAction(CacheManager.ACTION_UPDATED_CACHE);
			registerReceiver(cacheStateReceiver, filter);
		}

		findViewById(R.id.main_title_bar).setVisibility(View.GONE);
		setNavigationClick();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		// validate calling intent.
		// Intent intent = getIntent();
		if (intent == null) {
			Logger.e(TAG, "No intent for the given Activity.");
			return;
		}

		Bundle data = intent.getExtras();
		if (data != null && data.containsKey(EXTRA_MEDIA_ITEM)) {
			// setContentView(R.layout.activity_main_with_title);
			// retrieves the given Media item for the activity.
			mMediaItem = (MediaItem) data.getSerializable(EXTRA_MEDIA_ITEM);
		} else {
			Logger.e(TAG, "No MediaItem set for the given Activity.");
			return;
		}

		if (mMediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			// SetS title bar
			mTitleBarText = (TextView) findViewById(R.id.main_title_bar_text);
			mTitleBarText.setSelected(true);// xtpl
			mTitleBarText.setText(mMediaItem.getTitle());
			listTitle.add(mMediaItem.getTitle());

		}

		flurrySourceSection = "No Flurry Source Section";
		if (data != null && data.containsKey(FLURRY_SOURCE_SECTION)) {
			flurrySourceSection = (String) data.get(FLURRY_SOURCE_SECTION);
		}

		if (mMediaItem.getMediaType() == MediaType.ALBUM) {
			mFlurrySubSectionDescription = FlurryConstants.FlurrySubSectionDescription.AlbumDetail
					.toString();
		} else if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
			mFlurrySubSectionDescription = FlurryConstants.FlurrySubSectionDescription.PlaylistDetail
					.toString();
		} else if (mMediaItem.getMediaType() == MediaType.TRACK) {
			mFlurrySubSectionDescription = FlurryConstants.FlurrySubSectionDescription.SongDetail
					.toString();
		}

		Bundle detailsData = new Bundle();
		detailsData.putSerializable(MediaDetailsFragment.ARGUMENT_MEDIAITEM,
				(Serializable) mMediaItem);
		detailsData.putString(FLURRY_SOURCE_SECTION, flurrySourceSection);

		mediaDetailsFragment = new MediaDetailsFragment();
		mediaDetailsFragment.setArguments(detailsData);
		mediaDetailsFragment.onMediaListener(this);
		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit, R.anim.slide_right_enter,
				R.anim.slide_right_exit);
		fragmentTransaction.remove(mFragmentManager
				.findFragmentByTag("media_details_fragment"));
		fragmentTransaction.add(R.id.main_fragmant_container_media_detail,
				mediaDetailsFragment);
		// fragmentTransaction.addToBackStack(null);
		if(Constants.IS_COMMITALLOWSTATE)
			fragmentTransaction.commitAllowingStateLoss();
		else
			fragmentTransaction.commit();
	}



	private void setNavigationClick(){
		try {
			((MainActivity)this).mToolbar.setNavigationOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					onBackPressed();
				}
			});
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Logger.s("onResume MediaDetailsActivity");
		HungamaApplication.activityResumed();
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(getBaseContext());
		if (mApplicationConfigurations.isSongCatched()) {
			openOfflineGuide();
		}

		if (isSkipResume) {
			isSkipResume = false;
			return;
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			onBackPressed();
			return true;
		} else
			return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if (closeDrawerIfOpen()) {
			return;
		}

		if (mPlayerBarFragment != null && mPlayerBarFragment.isContentOpened()) {
			// Minimize player
			if (!mPlayerBarFragment.removeAllFragments())
				mPlayerBarFragment.closeContent();
		} else {
			int lastFragmentCount = getSupportFragmentManager()
					.getBackStackEntryCount();

			if (lastFragmentCount > 0) {

				getSupportFragmentManager().popBackStackImmediate();

                int count;
//                if(getSupportFragmentManager().getBackStackEntryCount()>0)
                    count=getSupportFragmentManager().getBackStackEntryCount()-1;
//                else
//                    count=0;

                Fragment fragment=null;
                if(count!=-1) {
                    FragmentManager.BackStackEntry backEntry = (FragmentManager.BackStackEntry) getSupportFragmentManager().getBackStackEntryAt(count);
                    String str = backEntry.getName();
                    Logger.i(TAG, "back stack name " + str);
                    fragment = getSupportFragmentManager().findFragmentByTag(str);
                }
				// new Handler().postDelayed(new Runnable() {
				// @Override
				// public void run() {
                MediaDetailsFragment mediaDetailsActivity;
                if (fragment!=null && fragment instanceof MediaDetailsFragment) {
                    mediaDetailsActivity = (MediaDetailsFragment) fragment;
                }else
                    mediaDetailsActivity=mediaDetailsFragment;

                    try {
                        if (mediaDetailsActivity.isVideoInsideOpen) {
                            mediaDetailsActivity.isVideoInsideOpen = false;
                            FrameLayout layout = (FrameLayout) findViewById(R.id.main_fragmant_container_media_detail);
                            MarginLayoutParams params = (MarginLayoutParams) layout
                                    .getLayoutParams();
                            params.setMargins(0, 0, 0, 0);
							ColorDrawable cd = new ColorDrawable(getResources().getColor(
									R.color.primaryColorDark));
							cd.setAlpha(mediaDetailsActivity.alpha);

							mediaDetailsActivity.updateTitleColor(cd, false);
							setNavigationClick();
                        }
                    } catch (Exception e) {
                    }

				if (isMediaInsideOpen) {
					try {
						if (listTitle.size() > 0) {
							if (listTitle.size() > 1) {
								listTitle.remove(listTitle.size() - 1);
							}
							Logger.e("listTitle", "" + listTitle);
							mTitleBarText.setText(listTitle.get(listTitle
									.size() - 1));
							showBackButtonWithTitleMediaDetail(
									listTitle.get(listTitle.size() - 1), "");
							// listTitle.remove(listTitle.size() - 1);

						} else
							mTitleBarText.setText(mMediaItem.getTitle());
					} catch (Exception e) {
						e.printStackTrace();
					}

				}

				// }
				// }, 100);
				return;
			}
			if (mMediaItemTrack != null)
				mMediaItemTrack = null;

//			if (SongCatcherFragment.isSongCatcherOpen)
//				SongCatcherFragment.isSongCatcherOpen = false;

			if (listTitle.size() > 0) {
				if (listTitle.size() > 1) {
					listTitle.remove(listTitle.size() - 1);
				}
				mTitleBarText.setText(listTitle.get(listTitle.size() - 1));
				listTitle.remove(listTitle.size() - 1);
			} else
				mTitleBarText.setText(mMediaItem.getTitle());

			HomeActivity.videoInAlbumSet = false;

			finish();
		}

		// super.onBackPressed();
	}

	public void openVideoPage(Bundle detailsDataVideos,OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener){

		MediaTileGridFragment mediaTileGridFragment = new MediaTileGridFragment();
		mediaTileGridFragment.setOnMediaItemOptionSelectedListener(mOnMediaItemOptionSelectedListener);
		mediaTileGridFragment.setArguments(detailsDataVideos);
		mediaTileGridFragment.setIsMarginTopRequire(false);


		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit, R.anim.slide_right_enter,
				R.anim.slide_right_exit);

		fragmentTransaction.add(R.id.main_fragmant_container_media_detail,
				mediaTileGridFragment,"video");
		fragmentTransaction.addToBackStack("video");
		fragmentTransaction.commit();

		FrameLayout layout = (FrameLayout) findViewById(R.id.main_fragmant_container_media_detail);
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layout
				.getLayoutParams();
		params.setMargins(0, ((MainActivity)this).getActionBarHeight(), 0, 0);
		Utils.setToolbarColor(((MainActivity) this));
	}

	@Override
	protected NavigationItem getNavigationItem() {
		return NavigationItem.OTHER;
	}

	public boolean isMediaInsideOpen;


	public void openTrackPage(MediaItem trackMediaItem) {
		mMediaItemTrack = trackMediaItem;
		isMediaInsideOpen = true;
		updateTitleSubtitle(trackMediaItem.getTitle(), "",255);

		Bundle detailsDataTrack = new Bundle();
		detailsDataTrack.putSerializable(
				MediaDetailsFragment.ARGUMENT_MEDIAITEM,
				(Serializable) trackMediaItem);

		MediaDetailsFragment mediaDetailsFragmentTrack = new MediaDetailsFragment();
		mediaDetailsFragmentTrack.setArguments(detailsDataTrack);
		mediaDetailsFragmentTrack.onMediaListener(this);
		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit, R.anim.slide_right_enter,
				R.anim.slide_right_exit);

		fragmentTransaction.add(R.id.main_fragmant_container_media_detail,
				mediaDetailsFragmentTrack,"detail");
		fragmentTransaction.addToBackStack("detail");
		fragmentTransaction.commit();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == 100) {
			if (mMediaItem.getTitle() != null)
				mTitleBarText.setText(mMediaItem.getTitle());
		}
		super.onActivityResult(requestCode, resultCode, data);
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
		mPlayerBarFragment.addToQueue(trackList, flurryEventName,
				flurrySourceSection);
	}


	@Override
	public void onMediaItemOptionPlayNowSelected(MediaItem mediaItem,
			int position) {
	}

	@Override
	public void onMediaItemOptionPlayNextSelected(MediaItem mediaItem,
			int position) {
	}

	@Override
	public void onMediaItemOptionAddToQueueSelected(MediaItem mediaItem,
			int position) {
	}

	@Override
	public void onMediaItemOptionShowDetailsSelected(MediaItem mediaItem,
			int position) {
		Intent intent = new Intent(this, VideoActivity.class);
		intent.putExtra(VideoActivity.EXTRA_MEDIA_ITEM_VIDEO,
				(Serializable) mediaItem);
		if (mediaDetailsFragment != null
				&& mediaDetailsFragment.mMediaSetDetails != null
				&& mediaDetailsFragment.mMediaSetDetails.getVideos() != null
				&& mediaDetailsFragment.mMediaSetDetails.getVideos().size() > 0) {
			try {
				intent.putExtra(VideoActivity.EXTRA_MEDIA_LIST_VIDEO,
						(Serializable) mediaDetailsFragment.mMediaSetDetails
								.getVideos());
				intent.putExtra(VideoActivity.EXTRA_MEDIA_POS_VIDEO, position);
			} catch (Exception e) {
			}
		}
		startActivity(intent);
	}

	@Override
	public void onMediaItemOptionRemoveSelected(MediaItem mediaItem,
			int position) {
	}

	@Override
	protected void onStart() {
		super.onStart();
		// FlurryAgent.setContinueSessionMillis(Constants.FLURRY_SESSION_LENGTH);
		// FlurryAgent.onStartSession(this, getString(R.string.flurry_app_key));
		Analytics.startSession(this);
        LanguageButtonLollipop btn_preference = (LanguageButtonLollipop) findViewById(R.id.btn_preferences);
		btn_preference.setVisibility(View.GONE);

		try {
			if (mMediaItem == null) {
				Bundle data = getIntent().getExtras();
				if (data != null && data.containsKey(EXTRA_MEDIA_ITEM)) {
					// retrieves the given Media item for the activity.
					mMediaItem = (MediaItem) data
							.getSerializable(EXTRA_MEDIA_ITEM);
				} else {
					Logger.e(TAG, "No MediaItem set for the given Activity.");
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (mMediaItem != null) {
			if (mMediaItem.getMediaType() == MediaType.ALBUM
					&& !TextUtils.isEmpty(mMediaItem.getAlbumName()))
				showBackButtonWithTitleMediaDetail(mMediaItem.getAlbumName(),
						"");
			else
				showBackButtonWithTitleMediaDetail(mMediaItem.getTitle(), "");
		}

	}

	@Override
	protected void onStop() {
		super.onStop();
		Logger.s("onStop MediaDetailsActivity");
		// HungamaApplication.activityStoped();
		Analytics.onEndSession(this);
	}

	@Override
	protected void onPause() {
		Logger.s("onPause MediaDetailsActivity");
		HungamaApplication.activityPaused();
		super.onPause();
	}

	@Override
	public void onMediaItemOptionSaveOfflineSelected(MediaItem mediaItem,
			int position) {
		if (mMediaItem != null)
			mediaItem.tag = mMediaItem;
		else if (mMediaItemTrack != null)
			mediaItem.tag = mMediaItemTrack;

		if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
			CacheManager.saveOfflineAction(this, mediaItem, null);
			Utils.saveOfflineFlurryEvent(
					this,
					FlurryConstants.FlurryCaching.LongPressMenuVideo.toString(),
					mediaItem);
		}
	}


	class CacheStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			Logger.s("========================= cachestateupdatereceived ========"
					+ arg1.getAction());
			if (arg1.getAction()
					.equals(CacheManager.ACTION_CACHE_STATE_UPDATED)
					|| arg1.getAction()
							.equals(CacheManager.ACTION_TRACK_CACHED)) {
				try {
					// ((BaseAdapter)
					// mediaDetailsFragment.text_view_media_details_list.getAdapter())
					// .notifyDataSetChanged();
					ListView mList = (ListView) findViewById(R.id.text_view_media_details_list);
					((BaseAdapter) mList.getAdapter()).notifyDataSetChanged();

				} catch (Exception e) {
					Logger.printStackTrace(e);
					// break;
				}
				try {
					// final Button mActionButtonSaveOffline = (Button)
					// findViewById(R.id.button_media_details_save_offline);
					final LinearLayout mActionButtonSaveOffline = (LinearLayout) findViewById(R.id.rl_media_details_save_offline);
					final TextView tvCacheState = (TextView) mActionButtonSaveOffline
							.findViewById(R.id.media_details_text_cache_state);
					final CustomCacheStateProgressBar progressCacheState = (CustomCacheStateProgressBar) mActionButtonSaveOffline
							.findViewById(R.id.media_details_progress_cache_state);
					MediaItem mediaItem = null;
					if (mMediaItemTrack != null)
						mediaItem = mMediaItemTrack;
					else
						mediaItem = mMediaItem;
					if (mActionButtonSaveOffline != null && mediaItem != null) {
						CacheState cacheState = null;
						Logger.s("MediaType ::::::::::::: "
								+ mediaItem.getMediaType() + " ::: "
								+ mediaItem.getTitle());
						if (mediaItem.getMediaType() == MediaType.ALBUM) {
							progressCacheState.setCacheCountVisibility(true);
							progressCacheState.setCacheCount(""
									+ DBOHandler.getAlbumCachedCount(
											MediaDetailsActivity.this, ""
													+ mediaItem.getId()));
							cacheState = DBOHandler.getAlbumCacheState(
									MediaDetailsActivity.this,
									"" + mediaItem.getId());
						} else if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
							progressCacheState.setCacheCountVisibility(true);
							progressCacheState.setCacheCount(""
									+ DBOHandler.getPlaylistCachedCount(
											MediaDetailsActivity.this, ""
													+ mediaItem.getId()));
							cacheState = DBOHandler.getPlaylistCacheState(
									MediaDetailsActivity.this,
									"" + mediaItem.getId());
						} else if (mediaItem.getMediaType() == MediaType.TRACK) {
							cacheState = DBOHandler.getTrackCacheState(
									MediaDetailsActivity.this,
									"" + mediaItem.getId());
						}
						if (cacheState != null) {
							Logger.s("cacheState :::: " + cacheState);
							// if(prevCacheState!=cacheState){
							if (cacheState == CacheState.CACHED) {
								runOnUiThread(new Runnable() {
									public void run() {
										if (mMediaItem.getMediaType() == MediaType.ALBUM) {
											int trackCacheCount = DBOHandler
													.getAlbumCachedCount(
															MediaDetailsActivity.this,
															""
																	+ mMediaItem
																			.getId());
											if (trackCacheCount >= mMediaItem
													.getMusicTrackCount())
												mActionButtonSaveOffline
														.setTag(true);
										} else if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
											int trackCacheCount = DBOHandler
													.getPlaylistCachedCount(
															MediaDetailsActivity.this,
															""
																	+ mMediaItem
																			.getId());
											if (trackCacheCount >= mMediaItem
													.getMusicTrackCount())
												mActionButtonSaveOffline
														.setTag(true);
										} else {
											mActionButtonSaveOffline
													.setTag(true);
										}
										// mActionButtonSaveOffline.setText("PLAY OFFLINE");
										// mActionButtonSaveOffline.setCompoundDrawablesWithIntrinsicBounds(0,
										// R.drawable.icon_media_details_saved,
										// 0, 0);
										tvCacheState
												.setText(getResources()
														.getString(
																R.string.caching_text_play_offline_capital));
									}
								});
								// if(isCachingComplete())
								// break;
							} else if (cacheState == CacheState.CACHING
									|| cacheState == CacheState.QUEUED) {
								runOnUiThread(new Runnable() {
									public void run() {
										mActionButtonSaveOffline.setTag(null);

										tvCacheState
												.setText(getResources()
														.getString(
																R.string.caching_text_saving_capital));
									}
								});
							} else {
								runOnUiThread(new Runnable() {
									public void run() {
										mActionButtonSaveOffline.setTag(false);
										tvCacheState
												.setText(getResources()
														.getString(
																R.string.media_details_custom_dialog_long_click_general_save_offline_caps));
									}
								});
							}
							progressCacheState.setCacheState(cacheState);
							if (mediaItem.getMediaType() == MediaType.ALBUM
									&& cacheState == CacheState.NOT_CACHED
									&& DBOHandler.getAlbumCachedCount(
											MediaDetailsActivity.this, ""
													+ mediaItem.getId()) > 0) {
								progressCacheState.setCacheCount(""
										+ DBOHandler.getAlbumCachedCount(
												MediaDetailsActivity.this, ""
														+ mediaItem.getId()));
								progressCacheState
										.setCacheState(CacheState.CACHED);
								tvCacheState
										.setText(getResources()
												.getString(
														R.string.caching_text_play_offline_capital));
							}
							if (mediaItem.getMediaType() == MediaType.TRACK)
								progressCacheState.setProgress(DBOHandler
										.getTrackCacheProgress(
												MediaDetailsActivity.this, ""
														+ mediaItem.getId()));
							// prevCacheState = cacheState;
							// }
						}
					} else {
						Logger.s(" :::: cacheState is null :::: ");
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
					// break;
				}
				try {
					if (dialog != null && dialog.isShowing()) {
						CustomCacheStateProgressBar progressCacheState = (CustomCacheStateProgressBar) dialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_progress_cache_state);
						Track track = (Track) progressCacheState
								.getTag(R.id.view_tag_object);
						if (track != null) {
							CacheState cacheState;
							int progress = 0;
							cacheState = DBOHandler.getTrackCacheState(
									MediaDetailsActivity.this,
									"" + track.getId());
							progress = DBOHandler.getTrackCacheProgress(
									MediaDetailsActivity.this,
									"" + track.getId());
							LinearLayout llSaveOffline = (LinearLayout) dialog
									.findViewById(R.id.long_click_custom_dialog_save_offline_row);
							if (cacheState == CacheState.CACHED) {
								llSaveOffline.setTag(true);
								((TextView) dialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_text))
										.setText(getResources()
												.getString(
														R.string.caching_text_play_offline));
								((ImageView) dialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_image))
										.setImageResource(R.drawable.icon_media_details_saved);
							} else if (cacheState == CacheState.CACHING
									|| cacheState == CacheState.QUEUED) {
								llSaveOffline.setTag(null);
								((TextView) dialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_text))
										.setText(getResources().getString(
												R.string.caching_text_saving));
								((ImageView) dialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_image))
										.setImageResource(R.drawable.icon_media_details_saving);
							} else if (cacheState == CacheState.NOT_CACHED) {
								llSaveOffline.setTag(false);
								((TextView) dialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_text))
										.setText(getResources()
												.getString(
														R.string.caching_text_save_offline));
								((ImageView) dialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_image))
										.setImageResource(R.drawable.icon_media_details_saveoffline);
							}
							progressCacheState.setCacheState(cacheState);
							progressCacheState.setProgress(progress);
						}
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
				if (mPlayerBarFragment != null)
					mPlayerBarFragment.updatedCurrentTrackCacheState();
			} else if (arg1.getAction().equals(
					CacheManager.ACTION_UPDATED_CACHE)) {

			}
		}
	}

	protected void onDestroy() {
		super.onDestroy();
		if (cacheStateReceiver != null)
			unregisterReceiver(cacheStateReceiver);
		cacheStateReceiver = null;
	}

	public void updateTitleSubtitle(String title, String subtitle,int alpha_current) {
		if (title != null) {
			mTitleBarText.setText(title);
			listTitle.add(title);
			Logger.e("listTitle add", "" + listTitle);
			showBackButtonWithTitleMediaDetail(title, subtitle);
		}
	}


	@Override
	public void onMediaItemSelected(Track track, String item) {
	}

	@Override
	public void onMediaItemSelectedPosition(Track track, int id) {
		switch (id) {
			case 0:
				MediaItem trackMediaItem = new MediaItem(track.getId(),
						track.getTitle(), track.getAlbumName(),
						track.getArtistName(), track.getImageUrl(),
						track.getBigImageUrl(), MediaType.TRACK.toString(), 0, 0,
						track.getImages(), track.getAlbumId());
				Intent intent = new Intent(
						MediaDetailsActivity.this,
						TrendNowActivity.class);
				intent.putExtra(
						TrendNowActivity.EXTRA_DATA_MEDIA_ITEM,
						(Serializable) trackMediaItem);

				startActivity(intent);
				break;
		case 1:
			// download
			trackMediaItem = new MediaItem(track.getId(),
					track.getTitle(), track.getAlbumName(),
					track.getArtistName(), track.getImageUrl(),
					track.getBigImageUrl(), MediaType.TRACK.toString(), 0, 0,
					track.getImages(), track.getAlbumId());
			 intent = new Intent(getBaseContext(),
					DownloadConnectingActivity.class);
			intent.putExtra(DownloadConnectingActivity.EXTRA_MEDIA_ITEM,
					(Serializable) trackMediaItem);
			startActivity(intent);

			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong.toString(),
					track.getTitle());
			reportMap.put(FlurryConstants.FlurryKeys.SourceSection.toString(),
					mFlurrySubSectionDescription);
			Analytics.logEvent(
					FlurryConstants.FlurryEventName.Download.toString(),
					reportMap);
			break;

		case 2:
			List<Track> tracks = new ArrayList<Track>();
			tracks.add(track);
			addToQueueButtonClickActivity(tracks, null, null);

			reportMap = new HashMap<String, String>();

			reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong.toString(),
					track.getTitle());
			reportMap.put(mMediaItem.getMediaType().toString(),
					Utils.toWhomSongBelongto(mMediaItem));
			reportMap
					.put(FlurryConstants.FlurryKeys.Source.toString(),
							FlurryConstants.FlurrySourceDescription.TapOnAddToQueueInContextualMenu
									.toString());
			reportMap.put(FlurryConstants.FlurryKeys.SubSection.toString(),
					mFlurrySubSectionDescription);

			Analytics.logEvent(
					FlurryConstants.FlurryEventName.SongSelectedForPlay
							.toString(), reportMap);
			break;
		case 3:
			trackMediaItem = new MediaItem(track.getId(), track.getTitle(),
					null, null, null, null, MediaType.TRACK.toString(), 0,
					track.getAlbumId());
			trackMediaItem.setAlbumId(track.getAlbumId());
			trackMediaItem.setMediaContentType(MediaContentType.MUSIC);
			trackMediaItem.setMediaType(MediaType.TRACK);

			openTrackPage(trackMediaItem);
			break;

		default:
			break;
		}
	}

	public int getActionBarHeight() {
		int height;

		height = getSupportActionBar().getHeight();

		return height;
	}

	@Override
	public void onMediaItemOptionPlayAndOpenSelected(MediaItem mediaItem,
			int position) {
	}

	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		Logger.e("onTrimMemory", "onTrimMemory" + level);
		if (level != Activity.TRIM_MEMORY_RUNNING_MODERATE
				&& level != Activity.TRIM_MEMORY_MODERATE)
			Utils.clearCache(true);
	}
}
