package com.hungama.hungamamusic.lite.carmode.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

import com.hungama.hungamamusic.lite.R;
import com.hungama.hungamamusic.lite.carmode.adapters.MusicPageAdapter;
import com.hungama.hungamamusic.lite.carmode.fragment.SearchResultListFragment.IListSearchResultListener;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;

import java.util.List;

public class SearchResultFragment extends Fragment implements IListSearchResultListener {

    public final static String TAG = SearchResultFragment.class.getSimpleName();
    public final static String QUERY_TEXT = "query_text";

    private MusicPageAdapter mMusicPageAdapter;
    private String strQueryText;
    private ISearchResultListener mListener;

    // UI Elements
    private View mRootView;
    private TabHost mTabHost;
    private ViewPager mViewPager;

    public static SearchResultFragment newInstance(ISearchResultListener listener) {
        final SearchResultFragment fragment = new SearchResultFragment();
        fragment.setListener(listener);

        return fragment;
    }

    public void setListener(ISearchResultListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getArguments().isEmpty()) {
            strQueryText = getArguments().getString(QUERY_TEXT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.carmode_fragment_music, container, false);

            mViewPager = (ViewPager) mRootView.findViewById(R.id.pager);
            mTabHost = (TabHost) mRootView.findViewById(android.R.id.tabhost);
            mTabHost.setup();
        }

        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mMusicPageAdapter == null) {
            mMusicPageAdapter = new MusicPageAdapter(getChildFragmentManager(), getActivity(), mTabHost, mViewPager);
            mMusicPageAdapter.addTab(mTabHost.newTabSpec("song"), "SONG", SearchResultListFragment.newInstance(this, strQueryText, MediaType.TRACK),
                    null);
            mMusicPageAdapter.addTab(mTabHost.newTabSpec("playlist"), "PLAYLIST",
                    SearchResultListFragment.newInstance(this, strQueryText, MediaType.PLAYLIST), null);
            mMusicPageAdapter.addTab(mTabHost.newTabSpec("album"), "ALBUM",
                    SearchResultListFragment.newInstance(this, strQueryText, MediaType.ALBUM), null);

            if(isVisible()){
                mViewPager.setAdapter(mMusicPageAdapter);
            }

            this.mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    // Reset all ListMusicFragment
                    MusicPageAdapter pageAdapter = (MusicPageAdapter) mViewPager.getAdapter();
                    for (int i = 0; i < pageAdapter.getCount(); i++) {
                        final Fragment fragment = pageAdapter.getItem(i);
                        if (fragment instanceof SearchResultListFragment) {
                            ((SearchResultListFragment) fragment).resetMusicLayout();
                        }
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        }
    }

    public void vHandleSearchResultClicks(View v) {
        final int currentPos = this.mViewPager.getCurrentItem();
        final SearchResultListFragment fragment = (SearchResultListFragment) mMusicPageAdapter.getItem(currentPos);
        fragment.vHandleListSearchResultClicks(v);

        // Handle Header's button.
        switch (v.getId()) {
            case R.id.btn_back_main_menu:
                getFragmentManager().popBackStack();
                break;

            case R.id.btn_universal_player:
                fragment.resetMusicLayout();
                mListener.onGoToMusicPlayer();
                break;

            default:
                break;
        }
    }

    public void handleCacheState() {
        final MusicPageAdapter pageAdapter = (MusicPageAdapter) this.mViewPager.getAdapter();
        final int currentPos = this.mViewPager.getCurrentItem();
        final Fragment fragment = pageAdapter.getItem(currentPos);

        if (fragment instanceof SearchResultListFragment) {
            ((SearchResultListFragment) fragment).handleCacheState();
        }
    }

    @Override
    public void onAddTrackToQueue(List<Track> listTracks) {
        mListener.onAddTrackToQueue(listTracks);
    }

    @Override
    public void goToMusicDetail(MediaItem selectedMedia) {
        mListener.onGoToMusicDetail(selectedMedia);
    }

    @Override
    public void popBackStack() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPlayNow() {
        mListener.onPlayNow();
    }

    public interface ISearchResultListener {
        void onGoToMusicPlayer();

        void onAddTrackToQueue(List<Track> listTrack);

        void onGoToMusicDetail(MediaItem selectedMediaItem);

        void onPlayNow();
    }
}
