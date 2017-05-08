package com.hungama.myplay.activity.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.HungamaApplication;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.gigya.TwitterLoginFragment;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.player.PlayerService.PlayerSericeBinder;
import com.hungama.myplay.activity.player.PlayerServiceBindingManager;
import com.hungama.myplay.activity.player.PlayerServiceBindingManager.ServiceToken;
import com.hungama.myplay.activity.ui.fragments.CommentsFragment;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.io.Serializable;

/**
 * Shows the comments per media item, This Activity usually been called
 * explicitly by the player bar's comment buttons.
 */
public class CommentsActivity extends MainActivity implements ServiceConnection {

	public static final String EXTRA_DATA_MEDIA_ITEM = "extra_data_media_item";
	public static final String EXTRA_DATA_DO_SHOW_TITLE = "extra_data_do_show_title";
	public static final String FLURRY_SOURCE_SECTION = "flurry_source_section";

	// a token for connecting the player service.
	private ServiceToken mServiceToken = null;
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	private String mFlurrySourceSection;
	public int fragmentCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		setOverlayAction();
		mDataManager = DataManager.getInstance(this);
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		View root = (View) LayoutInflater.from(CommentsActivity.this).inflate(
				R.layout.activity_comments, null);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comments);
		onCreateCode();
		// getDrawerLayout();
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(root, CommentsActivity.this);
		}

		/*
		 * called for the first time, adds the given comments fragment.
		 */
		if (getIntent().getExtras() == null) {
			return;
		}

		// pulls the argument for the activity, and packs them as args for the
		// fragment.
		Bundle incomingArgs = getIntent().getExtras();
		MediaItem mediaItem = (MediaItem) incomingArgs
				.getSerializable(EXTRA_DATA_MEDIA_ITEM);
		mFlurrySourceSection = incomingArgs.getString(FLURRY_SOURCE_SECTION);
		// boolean doShowTitle =
		// incomingArgs.getBoolean(EXTRA_DATA_DO_SHOW_TITLE, true);

		// creates the fragment.
		Bundle outcomingArgs = new Bundle();
		outcomingArgs.putSerializable(EXTRA_DATA_MEDIA_ITEM,
				(Serializable) mediaItem);
		outcomingArgs.putString(FLURRY_SOURCE_SECTION, mFlurrySourceSection);

		CommentsFragment commentsFragment = new CommentsFragment();
		commentsFragment.setArguments(outcomingArgs);

		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
				.beginTransaction();
		fragmentTransaction.replace(R.id.comments_fragmant_container,
				commentsFragment, CommentsFragment.FRAGMENT_COMMENTS);
		fragmentTransaction.commit();

		// sets the Action Bar's title.
		// ActionBar actionBar = getSupportActionBar();
		// String title;
		if (getIntent().getExtras().getBoolean("is_video", false)) {
			// title =
			// getResources().getString(R.string.main_actionbar_title_videos);
			// Utils.setActionBarTitle(this, getSupportActionBar(),
			// NavigationItem.VIDEOS.title);
			// getSupportActionBar().setTitle(NavigationItem.VIDEOS.title);
		} else {
			// title =
			// getResources().getString(R.string.main_actionbar_title_music);
		}
		// actionBar.setTitle(title);
		findViewById(R.id.comments_title_bar).setVisibility(View.GONE);


		showBackButtonWithTitle(
						getResources().getString(R.string.comments_title), "");
		setNavigationClick();

		getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
			public void onBackStackChanged() {
				try {
					int backCount = getSupportFragmentManager().getBackStackEntryCount();

					if (backCount == 0 && fragmentCount>0) {
						showBackButtonWithTitle(
								getResources().getString(R.string.comments_title), "");
						setNavigationClick();
						return;
					}

					FragmentManager.BackStackEntry backEntry = (FragmentManager.BackStackEntry) getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1);
					String str = backEntry.getName();
					Logger.i("test", "back stack name " + str);
					Fragment fragment = getSupportFragmentManager().findFragmentByTag(str);

					if (fragment instanceof ProfileActivity) {
						ProfileActivity mPrifileActivity = (ProfileActivity) fragment;
						mPrifileActivity.setTitle(false);
					}
					fragmentCount = backCount;
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		/*
		 * Binds to the PLayer service to pause it if playing.
		 */
		mServiceToken = PlayerServiceBindingManager.bindToService(this, this);
		// System.out.println(" :::::::: " +
		// getIntent().getExtras().getBoolean("is_video", false));
		if (getIntent().getExtras().getBoolean("is_video", false)) {
			getSupportActionBar().setTitle(NavigationItem.VIDEOS.title);
		}

		showBackButtonWithTitle(
				getResources().getString(R.string.comments_title), "");
	}

	@Override
	protected void onResume() {
		super.onResume();

		HungamaApplication.activityResumed();
		if (mApplicationConfigurations.isSongCatched()) {
			openOfflineGuide();
		}
        if (getIntent().getExtras().getBoolean("is_video", false) && PlayerService.service != null) {
            PlayerService.service.setPausedFromVideo(true);
        }
	}

	@Override
	protected void onStop() {
		// HungamaApplication.activityStoped();
		// disconnects from the player service.
		PlayerServiceBindingManager.unbindFromService(mServiceToken);
		super.onStop();
	}

	@Override
	protected void onPause() {
		HungamaApplication.activityPaused();
		super.onPause();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		/*
		 * we've establish a connection to the player service. if it plays,
		 * pause it.
		 */
		PlayerSericeBinder binder = (PlayerSericeBinder) service;
		PlayerService playerService = binder.getService();

		// does nothing, just holds the connection to the playing service.
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		mServiceToken = null;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		/*
		 * Invoked when the activity was visible, replace the old fragment with
		 * the new one to update the visible data.
		 */

		if (intent.getExtras() == null) {
			return;
		}

		// pulls the argument for the activity, and packs them as args for the
		// fragment.
		Bundle incomingArgs = getIntent().getExtras();
		MediaItem mediaItem = (MediaItem) incomingArgs
				.getSerializable(EXTRA_DATA_MEDIA_ITEM);
		boolean doShowTitle = incomingArgs.getBoolean(EXTRA_DATA_DO_SHOW_TITLE,
				true);

		// creates the fragment.
		Bundle outcomingArgs = new Bundle();
		outcomingArgs.putSerializable(EXTRA_DATA_MEDIA_ITEM,
				(Serializable) mediaItem);
		outcomingArgs.putBoolean(EXTRA_DATA_DO_SHOW_TITLE, doShowTitle);

		CommentsFragment commentsFragment = new CommentsFragment();
		commentsFragment.setArguments(outcomingArgs);

		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
				.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit, R.anim.slide_right_enter,
				R.anim.slide_right_exit);

		fragmentTransaction.replace(R.id.comments_fragmant_container,
				commentsFragment, CommentsFragment.FRAGMENT_COMMENTS);
		fragmentTransaction.commit();
	}

	@Override
	protected NavigationItem getNavigationItem() {
		return NavigationItem.MUSIC;
	}


	public void setNavigationClick(){

		((MainActivity)this).mToolbar.setNavigationOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
					onBackPressed();
			}
		});
	}

	@Override
	public void onBackPressed() {
		// checks if the webview exists and calls its back button support.
		// Fragment fragmentParent =
		// getSupportFragmentManager().findFragmentByTag(CommentsFragment.FRAGMENT_COMMENTS);
		// if (fragmentParent != null) {

		if (mDrawerLayout != null
				&& mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
			mDrawerLayout.closeDrawers();
			return;
		}
		if (mPlayerBarFragment != null && mPlayerBarFragment.isContentOpened()) {
			// Minimize player
			if (!mPlayerBarFragment.removeAllFragments())
				mPlayerBarFragment.closeContent();
		} else {
			int backCount = getSupportFragmentManager().getBackStackEntryCount();
			if(backCount>0)
				getSupportFragmentManager().popBackStack();
			else{
				setResult(RESULT_OK);
				Fragment fragment = getSupportFragmentManager().findFragmentByTag(
						TwitterLoginFragment.FRAGMENT_TWITTER_LOGIN);
				if (fragment != null) {
					TwitterLoginFragment twitterLoginFragment = (TwitterLoginFragment) fragment;
					twitterLoginFragment.onBackPressed();
					return;
				}
				finish();
			}

		}
	}

	public void toggleActivityTitle() {
		RelativeLayout titleBar = (RelativeLayout) findViewById(R.id.comments_title_bar);
		if (titleBar.getVisibility() == View.VISIBLE) {
			titleBar.setVisibility(View.GONE);
		} else {
			titleBar.setVisibility(View.VISIBLE);
		}

	}

}
