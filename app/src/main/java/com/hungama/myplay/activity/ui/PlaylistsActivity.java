package com.hungama.myplay.activity.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.ui.fragments.BackHandledFragment;
import com.hungama.myplay.activity.ui.fragments.ItemableTilesFragment;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class PlaylistsActivity extends BackHandledFragment {

    //private LanguageTextView mainTitleBarText;
    //private ImageView mainTitleBarButtonOptions;
    //private FrameLayout mainContainer;
    //private LinearLayout playlistTracksOptions;
    //private LanguageButton playAllButton;
    private Stack<String> stack_text = new Stack<String>();

    private MainActivity getApplicationContext() {
        return (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
                .getInstance(getApplicationContext());

        View rootView = inflater.inflate(R.layout.activity_playlists_new,
                container, false);

        //super.onCreate(savedInstanceState);

        // getDrawerLayout();
        if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
            Utils.traverseChild(rootView, getApplicationContext());
        }

        ItemableTilesFragment mTilesFragment = new ItemableTilesFragment();
        mTilesFragment.setPlaylistActivity(this);
        mTilesFragment.init(MediaType.PLAYLIST, null);
        mTilesFragment.setIsPlaylistScreen(true);
        FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager
                .beginTransaction();

        fragmentTransaction.add(R.id.main_fragmant_container, mTilesFragment, "test");
//        fragmentTransaction.addToBackStack("test");
        fragmentTransaction.disallowAddToBackStack();
        if(Constants.IS_COMMITALLOWSTATE)
            fragmentTransaction.commitAllowingStateLoss();
        else
            fragmentTransaction.commit();

        //mainTitleBarText = (LanguageTextView) root.findViewById(R.id.main_title_bar_text);
        //mainTitleBarButtonOptions = (ImageView) root.findViewById(R.id.main_title_bar_button_options);
        //mainTitleBarButtonOptions.setOnClickListener(this);
        //mainContainer = (FrameLayout) root.findViewById(R.id.player_queue_content_container);
        /*playlistTracksOptions = (LinearLayout) root.findViewById(R.id.playlist_tracks_options);
		playAllButton = (LanguageButton) root.findViewById(R.id.playlist_tracks_play_all);
		playAllButton.setOnClickListener(this);*/

		/*((LinearLayout) findViewById(R.id.ll_playlist_tracks_save_all_offline))
				.setVisibility(View.VISIBLE);
		((LanguageButton) findViewById(R.id.playlist_tracks_save_all_offline))
				.setOnClickListener(this);*/

		/*findViewById(R.id.main_title_bar).setVisibility(View.GONE);*/
        return rootView;
    }

    /*public LanguageTextView getMainTitleBarText() {
		return mainTitleBarText;
	}

	public ImageView getMainTitleBarButtonOptions() {
		return mainTitleBarButtonOptions;
	}*/

	/*public FrameLayout getMainContainer() {
		return mainContainer;
	}*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    //@Override
    public boolean onBackPressed() {

        if(getApplicationContext()==null)
            return false;

        if (getApplicationContext().mDrawerLayout != null
                && getApplicationContext().mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            getApplicationContext().mDrawerLayout.closeDrawers();
            return true;
        }

        if (getApplicationContext().mPlayerBarFragment != null && getApplicationContext().mPlayerBarFragment.isContentOpened()) {
            // Minimize player
            if (!getApplicationContext().mPlayerBarFragment.removeAllFragments())
                getApplicationContext().mPlayerBarFragment.closeContent();
            return true;
        } else {
            if (getActivity().getSupportFragmentManager().getBackStackEntryCount() > 1) {

                getActivity().getSupportFragmentManager().popBackStack();
                ItemableTilesFragment fragment = (ItemableTilesFragment) getFragmentManager().findFragmentByTag("test");

                fragment.refreshplaylist();
                refreshtitle();
                return true;
            }
        }
        return false;
    }

    private List<Playlist> getPlaylists() {

        // Get all playlists
        Playlist dummy = new Playlist();
        DataManager mDataManager = DataManager
                .getInstance(getApplicationContext());
        Map<Long, Playlist> map = mDataManager.getStoredPlaylists();
        List<Playlist> UpdatedPlaylists = new ArrayList<Playlist>();

        // Convert from Map<Long, Playlist> to List<Itemable>
        if (map != null && map.size() > 0) {
            for (Map.Entry<Long, Playlist> p : map.entrySet()) {
                UpdatedPlaylists.add(p.getValue());
            }
        }
        if (UpdatedPlaylists.size() > 0)
            Collections.reverse(UpdatedPlaylists);
        return UpdatedPlaylists;
    }


    @Override
    public void onStop() {
        super.onStop();
        // HungamaApplication.activityStoped();
        Analytics.onEndSession(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        // FlurryAgent.setContinueSessionMillis(Constants.FLURRY_SESSION_LENGTH);
        // FlurryAgent.onStartSession(this, getString(R.string.flurry_app_key));
        Analytics.startSession(getActivity(), this);
        Analytics.onPageView();
        Analytics.logEvent("My Playlists");
        try {
            getApplicationContext().showBackButtonWithTitle(
                    getString(R.string.itemable_text_title, "("
                            + getPlaylists().size() + ")"), "");
            if (!stack_text.contains(getString(R.string.itemable_text_title,
                    "(" + getPlaylists().size() + ")")))
                stack_text.push(getString(R.string.itemable_text_title, "("
                        + getPlaylists().size() + ")"));
        } catch (Exception e) {
            getApplicationContext().showBackButtonWithTitle(
                    getString(R.string.itemable_text_title, "(" + 0 + ")"), "");
            if (!stack_text.contains(getString(R.string.itemable_text_title,
                    "(" + 0 + ")")))
                stack_text.push(getString(R.string.itemable_text_title, "(" + 0
                        + ")"));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setNavigationClick();
    }

    public void setNavigationClick(){

        ((MainActivity)getActivity()).mToolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    onBackPressed();
            }
        });

    }

    @Override
    public void setTitle(boolean needOnlyHight, boolean needToSetTitle) {
        if (stack_text != null && stack_text.size() > 0)
            getApplicationContext().showBackButtonWithTitle(stack_text.get(stack_text.size()-1), "");
       setNavigationClick();
        Utils.setToolbarColor(((MainActivity) getActivity()));
    }

    public void refreshtitle(){
        try {
            getApplicationContext().showBackButtonWithTitle(
                    getString(R.string.itemable_text_title, "("
                            + getPlaylists().size() + ")"), "");
            if (!stack_text.contains(getString(R.string.itemable_text_title,
                    "(" + getPlaylists().size() + ")")))
                stack_text.push(getString(R.string.itemable_text_title, "("
                        + getPlaylists().size() + ")"));
        } catch (Exception e) {
            getApplicationContext().showBackButtonWithTitle(
                    getString(R.string.itemable_text_title, "(" + 0 + ")"), "");
            if (!stack_text.contains(getString(R.string.itemable_text_title,
                    "(" + 0 + ")")))
                stack_text.push(getString(R.string.itemable_text_title, "(" + 0
                        + ")"));
        }
    }
}