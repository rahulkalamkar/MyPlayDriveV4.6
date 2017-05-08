package com.hungama.myplay.activity.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.HungamaApplication;

import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.ui.fragments.BackHandledFragment;
import com.hungama.myplay.activity.ui.fragments.FavoritesFragment;
import com.hungama.myplay.activity.ui.fragments.ItemableTilesFragment;
import com.hungama.myplay.activity.ui.fragments.MainSearchFragmentNew;
import com.hungama.myplay.activity.ui.fragments.MainSearchResultsFragment;
import com.hungama.myplay.activity.ui.fragments.MediaDetailsFragment;
import com.hungama.myplay.activity.ui.fragments.MediaTileGridFragment;
import com.hungama.myplay.activity.ui.fragments.RedeemFragment;
import com.hungama.myplay.activity.ui.fragments.SocialMyStreamFragment;
import com.hungama.myplay.activity.ui.fragments.social.BadgesFragment;
import com.hungama.myplay.activity.ui.fragments.social.LeaderboardFragment;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.FileUtils;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.io.File;
import java.io.Serializable;

/**
 * Controller for presenting details of the given MediaItem.
 */
public class DownloadConnectingActivity extends MainActivity implements BackHandledFragment.BackHandlerInterface {

	private static final String TAG = "DownloadConnectingActivity";

	// protected static final String FRAGMENT_TAG_UPGRADE =
	// "fragment_tag_upgrade";

	// public static final String PASSWORD_SMS_SENT = "1";
	// public static final String MSISDN_ALREADY_EXIST_AND_VERIFIED = "3";
	public static final String EXTRA_MEDIA_ITEM = "extra_media_item";

	// public static final String CONTENT_TYPE_AUDIO = "audio";
	// public static final String CONTENT_TYPE_VIDEO = "video";

	private MediaItem mMediaItem;
	private Dialog downloadDialog;

	private FileUtils fileUtils;

	// public static String mobileToSend;

	private Bundle data;

	private boolean backFromDownloadActivity = false;

	// ======================================================
	// Activity life-cycle callbacks.
	// ======================================================

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// setOverlayAction();
		setOverlayAction();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download_connecting);
		onCreateCode();
		// validate calling intent.
		Intent intent = getIntent();
		if (intent == null) {
			Logger.e(TAG, "No intent for the given Activity.");
			return;
		}

		// getDrawerLayout();
		data = intent.getExtras();

		if (data != null && data.containsKey(EXTRA_MEDIA_ITEM)) {
			// retrieves the given Media item for the activity.
			mMediaItem = (MediaItem) data.getSerializable(EXTRA_MEDIA_ITEM);
			if (mMediaItem != null) {
				// check if the file already exists in the downloaded files
				// library - display dialog.
				fileUtils = new FileUtils(this);
				// create directory and return it or just return it if already
				// exists
				// String hungamaFolder =
				// getResources().getString(R.string.download_media_folder);

				if (fileUtils.isExternalStoragePresent()) {
					File path = fileUtils.getStoragePath(mMediaItem
							.getMediaContentType());
					// hungamaCollectionDir = fileUtils.createDirectory(path);
					if (path != null) {
						String mediaFileName;
						if (mMediaItem.getMediaContentType() == MediaContentType.VIDEO) {
							mediaFileName = mMediaItem.getTitle() + "_"
									+ String.valueOf(mMediaItem.getId())
									+ ".mp4";
						} else {
							mediaFileName = mMediaItem.getTitle() + "_"
									+ String.valueOf(mMediaItem.getId())
									+ ".mp3";
						}

						String encodedMediaFileName = "";
						try {
							encodedMediaFileName = HungamaApplication
									.encodeURL(mediaFileName, "UTF-8");
						} catch (Exception e) {
							Logger.i(TAG, e.getMessage());
							e.printStackTrace();
						}
						// File file = new File(path, mediaFileName);

						if (fileUtils.isFileInDirectory(path,
								encodedMediaFileName)) {
							String title = getResources().getString(
									R.string.general_download_title);
							String body = "";
							if (mMediaItem.getMediaContentType() == MediaContentType.VIDEO) {
								body = Utils
										.getMultilanguageText(
												getApplicationContext(),
												getResources()
														.getString(
																R.string.download_same_song_dialog_body_text_video));
							} else {
								body = Utils
										.getMultilanguageText(
												getApplicationContext(),
												getResources()
														.getString(
																R.string.download_same_song_dialog_body_text));
							}

							// hideLoadingDialog();
							showDownloadDialog(title, body, true, false);

						} else {
							Intent downloadActivityIntent = new Intent(this,
									DownloadActivity.class);
							downloadActivityIntent.putExtra(
									DownloadActivity.EXTRA_MEDIA_ITEM,
									(Serializable) mMediaItem);
							startActivity(downloadActivityIntent);
							finish();
						}
					}
				} else {
					finish();
				}
			}
		} else {
			Logger.e(TAG, "No MediaItem set for the given Activity.");
			return;
		}
		getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
			public void onBackStackChanged() {
				getSupportFragment(false);
			}
		});
	}

	private void resetHomeScreen() {
		finish();
	}

	int fragmentCount;
	public void getSupportFragment(boolean isFromChild) {
		try {
			int backCount = getSupportFragmentManager().getBackStackEntryCount();
			Logger.i(TAG, "back stack changed " + backCount);

			Logger.i(TAG, "back stack fragmentCount " + fragmentCount);
			if (backCount == 0 && fragmentCount > 0) {
				resetHomeScreen();
				return;
			}
			if (fragmentCount > backCount || isFromChild) {
				FragmentManager.BackStackEntry backEntry = (FragmentManager.BackStackEntry) getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1);
				String str = backEntry.getName();
				Logger.i(TAG, "back stack name " + str);
				Fragment fragment = getSupportFragmentManager().findFragmentByTag(str);
				resetCurrentFragment(fragment);
			}
			fragmentCount = backCount;
		} catch (Exception e) {

		} catch (Error e) {

		}
	}
	private BackHandledFragment selectedFragment;

	@Override
	public void setSelectedFragment(BackHandledFragment selectedFragment) {
		this.selectedFragment = selectedFragment;
	}
	public void resetCurrentFragment(Fragment fragment){
		try {
			if (fragment instanceof MediaDetailsActivityNew) {
				MediaDetailsActivityNew mediaDetailsActivity = (MediaDetailsActivityNew) fragment;
				if(mediaDetailsActivity!=null && !mediaDetailsActivity.setChildFragment(true)){
					setSelectedFragment(mediaDetailsActivity);
					mediaDetailsActivity.setTitle(true, true);
				}
			} else if (fragment instanceof MediaDetailsFragment) {
				MediaDetailsFragment mediaDetailsActivity = (MediaDetailsFragment) fragment;
				setSelectedFragment(mediaDetailsActivity.mediaDetailsActivityNew);
				mediaDetailsActivity.mediaDetailsActivityNew.setTitle(true, false);
			} else if (fragment instanceof ProfileActivity) {
				ProfileActivity mPrifileActivity = (ProfileActivity) fragment;
				setSelectedFragment(mPrifileActivity);
				mPrifileActivity.setTitle(false);
			} else if (fragment instanceof PlaylistsActivity) {
				PlaylistsActivity mPlaylistsActivity = (PlaylistsActivity) fragment;
				setSelectedFragment(mPlaylistsActivity);
				mPlaylistsActivity.setTitle(false, true);
			}else if (fragment instanceof FavoritesActivity) {
				FavoritesActivity mfaFavoritesActivity = (FavoritesActivity) fragment;
				setSelectedFragment(mfaFavoritesActivity);
				mfaFavoritesActivity.setTitle(false, true);
			} else if (fragment instanceof MyCollectionActivity) {
				MyCollectionActivity mMycollectionActivity = (MyCollectionActivity) fragment;
				setSelectedFragment(mMycollectionActivity);
				mMycollectionActivity.setTitle(false, true);
			}else if (fragment instanceof FavoritesFragment) {
				FavoritesFragment mfaFavoritesFragment = (FavoritesFragment) fragment;
				if(mfaFavoritesFragment.profileActivity!=null)
					setSelectedFragment(mfaFavoritesFragment.profileActivity);
				mfaFavoritesFragment.setTitle();
			}else if (fragment instanceof MyStreamActivity) {
				MyStreamActivity mMyStreamFragment = (MyStreamActivity) fragment;
				setSelectedFragment(mMyStreamFragment);
				mMyStreamFragment.setTitle(false,true);
			}else if (fragment instanceof SocialMyStreamFragment) {
				SocialMyStreamFragment socialMyStreamFragment = (SocialMyStreamFragment) fragment;
				socialMyStreamFragment.myStreamActivity.setTitle(false,true);
			}else if (fragment instanceof BadgesFragment) {
				BadgesFragment mBadgesFragment = (BadgesFragment) fragment;
				setSelectedFragment(mBadgesFragment.getProfileActivity());
				mBadgesFragment.setTitle();
			} else if (fragment instanceof LeaderboardFragment) {
				LeaderboardFragment mLeaderboardFragment = (LeaderboardFragment) fragment;
				setSelectedFragment(mLeaderboardFragment.getProfileActivity());
				mLeaderboardFragment.setTitle();
			} else if (fragment instanceof RedeemFragment) {
				RedeemFragment mRedeemFragment = (RedeemFragment) fragment;
				setSelectedFragment(mRedeemFragment.getProfileActivity());
				mRedeemFragment.setTitle();
			} else if (fragment instanceof ItemableTilesFragment) {
				ItemableTilesFragment mediaDetailsActivity = (ItemableTilesFragment) fragment;
				setSelectedFragment(mediaDetailsActivity);
				mediaDetailsActivity.setTitle(false, true);
			} else if (fragment instanceof MediaTileGridFragment) {
				MediaTileGridFragment mediaDetailsActivity = (MediaTileGridFragment) fragment;
				setSelectedFragment(mediaDetailsActivity);
//                        setSelectedFragment(mediaDetailsActivity.);
				mediaDetailsActivity.setTitle(false, true);
			} else if (fragment instanceof MainSearchFragmentNew) {
				MainSearchFragmentNew mediaDetailsActivity = (MainSearchFragmentNew) fragment;
				setSelectedFragment(mediaDetailsActivity);
				// mediaDetailsActivity.displayTitle(mediaDetailsActivity.actionbar_title);
				mediaDetailsActivity.setTitle(false, true);
			} else if (fragment instanceof MainSearchResultsFragment) {
				MainSearchResultsFragment mediaDetailsActivity = (MainSearchResultsFragment) fragment;
				setSelectedFragment(mediaDetailsActivity.searchResultsFragment);
				mediaDetailsActivity.searchResultsFragment.displayTitle(mediaDetailsActivity.searchResultsFragment.actionbar_title);
			} else if (fragment instanceof MainSearchResultsFragment) {
				MainSearchResultsFragment mediaDetailsActivity = (MainSearchResultsFragment) fragment;
				setSelectedFragment(mediaDetailsActivity.searchResultsFragment);
				mediaDetailsActivity.searchResultsFragment.displayTitle(mediaDetailsActivity.searchResultsFragment.actionbar_title);
			}/*else if (fragment instanceof SongCatcherFragment) {
				SongCatcherFragment songCatcherFragment = (SongCatcherFragment) fragment;
				setSelectedFragment(songCatcherFragment);
				songCatcherFragment.setTitle(false,true);
			}*/
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		showBackButtonWithTitle(Utils.getMultilanguageTextLayOut(
				getApplicationContext(),
				getResources().getString(R.string.general_download_title)), "");
	}

	@Override
	protected void onResume() {
		super.onResume();
		HungamaApplication.activityResumed();
		// ApplicationConfigurations mApplicationConfigurations =
		// ApplicationConfigurations.getInstance(getBaseContext());
		// if(mApplicationConfigurations.isSongCatched()){
		// openOfflineGuide();
		// }
		int backCount = getSupportFragmentManager().getBackStackEntryCount();

		if (backFromDownloadActivity) {
			if(backCount==0){
				finish();
			}
			Logger.i(TAG, "finished the connecting Activity");
		} else {
			backFromDownloadActivity = true;
			Logger.i(TAG, "backFromDownloadActivity changed to true");
		}
	}

	@Override
	protected void onStop() {
		// HungamaApplication.activityStoped();
		super.onStop();
	}

	@Override
	protected void onPause() {
		HungamaApplication.activityPaused();
		super.onPause();
	}

	public void showDownloadDialog(String header, String body,
			boolean isLeftButtonVisible, boolean isRightButtonVisible) {

		// set up custom dialog
		downloadDialog = new Dialog(this);
		downloadDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		downloadDialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(Color.TRANSPARENT));
		downloadDialog.setContentView(R.layout.custom_dialog_eng);

		TextView title = (TextView) downloadDialog
				.findViewById(R.id.download_custom_dialog_title_text);
		title.setText(header);

		TextView text = (TextView) downloadDialog
				.findViewById(R.id.text_custom_alert_message);
		text.setText(body);

		Button goToMyCollectionButton = (Button) downloadDialog
				.findViewById(R.id.button_custom_alert_positive);
		Button downloadAgainButton = (Button) downloadDialog
				.findViewById(R.id.button_custom_alert_negative);

		goToMyCollectionButton.setText(getResources().getString(
				R.string.download_same_song_dialog_button_text));
		downloadAgainButton.setText(getResources().getString(R.string.cancel));

		LinearLayout ButtonsPanel = (LinearLayout) downloadDialog
				.findViewById(R.id.buttons_panel);
		if (!isLeftButtonVisible && !isRightButtonVisible) {
			ButtonsPanel.setVisibility(View.GONE);
		} else {
			ButtonsPanel.setVisibility(View.VISIBLE);
			if (isLeftButtonVisible) {
				goToMyCollectionButton.setVisibility(View.VISIBLE);
				goToMyCollectionButton
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								downloadDialog.dismiss();
								MyCollectionActivity mTilesFragment = new MyCollectionActivity();

								FragmentManager mFragmentManager = getSupportFragmentManager();
								FragmentTransaction fragmentTransaction = mFragmentManager
										.beginTransaction();
								fragmentTransaction.setCustomAnimations(R.anim.slide_and_show_bottom_enter,
										R.anim.slide_and_show_bottom_exit);
								fragmentTransaction.replace(R.id.home_browse_by_fragmant_container,
										mTilesFragment, "MyCollectionActivity");
								fragmentTransaction.addToBackStack("MyCollectionActivity");
//								fragmentTransaction.disallowAddToBackStack();
								if(Constants.IS_COMMITALLOWSTATE)
									fragmentTransaction.commitAllowingStateLoss();
								else
									fragmentTransaction.commit();

//								Intent myCollectionActivityIntent = new Intent(
//										getApplicationContext(),
//										MyCollectionActivity.class);
//								startActivity(myCollectionActivityIntent);
							}
						});
			} else {
				goToMyCollectionButton.setVisibility(View.GONE);
			}

			// if (isRightButtonVisible) {
			downloadAgainButton.setVisibility(View.VISIBLE);
			downloadAgainButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					downloadDialog.dismiss();
					onBackPressed();
				}
			});
			// } else {
			// downloadAgainButton.setVisibility(View.GONE);
			// }
		}

		// ImageButton closeButton = (ImageButton) downloadDialog
		// .findViewById(R.id.close_button);
		// closeButton.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// downloadDialog.dismiss();
		// onBackPressed();
		// }
		// });
		downloadDialog.setCancelable(true);
		downloadDialog
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						downloadDialog.dismiss();
						onBackPressed();
					}
				});

		DisplayMetrics displaymetrics = new DisplayMetrics();
		downloadDialog.getWindow().getWindowManager().getDefaultDisplay()
				.getMetrics(displaymetrics);
		int width = (int) (displaymetrics.widthPixels);

		WindowManager.LayoutParams params = downloadDialog.getWindow()
				.getAttributes();
		params.width = width;
		downloadDialog.getWindow().setAttributes(params);

		downloadDialog.show();

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

	// @Override
	// protected NavigationItem getNavigationItem() {
	// // TODO: Do MediaItem must be valid ?
	// if (mMediaItem != null) {
	// if (mMediaItem.getMediaContentType() == MediaContentType.VIDEO) {
	// return NavigationItem.VIDEOS;
	//
	// } else if (mMediaItem.getMediaContentType() == MediaContentType.MUSIC) {
	// return NavigationItem.MUSIC;
	// }
	// }
	//
	// return NavigationItem.OTHER;
	// }

	@Override
	public void onBackPressed() {
		// if (mDrawerLayout!=null &&
		// mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
		// mDrawerLayout.closeDrawers();
		// return;
		// }
		// if (mPlayerBarFragment != null &&
		// mPlayerBarFragment.isContentOpened()) {
		// // Minimize player
		// if (!mPlayerBarFragment.removeAllFragments())
		// mPlayerBarFragment.closeContent();
		// } else {
		int backCount = getSupportFragmentManager().getBackStackEntryCount();
		Logger.i(TAG, "back stack changed " + backCount);

		if (backCount == 1) {
			finish();
			return;
		}
		if(selectedFragment == null || !selectedFragment.onBackPressed()) {
			finish();
		}

//		if (SongCatcherFragment.isSongCatcherOpen)
//			finish();
//		else
//			super.onBackPressed();
		// }
	}

	@Override
	protected NavigationItem getNavigationItem() {
		return null;
	}
}
