package com.hungama.hungamamusic.lite.carmode.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

import com.hungama.hungamamusic.lite.R;
import com.hungama.hungamamusic.lite.carmode.adapters.MusicPageAdapter;
import com.hungama.hungamamusic.lite.carmode.fragment.DiscoveryMusicFragment.IDiscoveryListener;
import com.hungama.hungamamusic.lite.carmode.fragment.ListMusicFragment.IListMusicListener;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.Track;

import java.util.List;

public class MusicFragment extends Fragment {

    public final static String TAG = MusicFragment.class.getSimpleName();

    private MusicPageAdapter mMusicPageAdapter;
    private IMusicListener mListener;

    // UI Elements
    private TabHost mTabHost;
    private ViewPager mViewPager;
    private View view;

    public void setMusicListener(IMusicListener listener) {
        this.mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.carmode_fragment_music, container, false);

            mViewPager = (ViewPager) view.findViewById(R.id.pager);
            mTabHost = (TabHost) view.findViewById(android.R.id.tabhost);
            mTabHost.setup();
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mMusicPageAdapter == null) {
            mMusicPageAdapter = new MusicPageAdapter(getChildFragmentManager(), getActivity(), mTabHost, mViewPager);
            mMusicPageAdapter.addTab(mTabHost.newTabSpec("one"), "NEW", ListMusicFragment.newInstance(new IListMusicListener() {

                @Override
                public void onAddTrackToQueue(List<Track> listTrack) {
                    MusicFragment.this.mListener.onAddTrackToQueue(listTrack);
                }

                @Override
                public void goToMusicDetail(MediaItem selectedMediaItem) {
                    MusicFragment.this.mListener.onGoToMusicDetail(selectedMediaItem);
                }

                @Override
                public void popBackStack() {
                    // Implement later
                }

                @Override
                public void onPlayNow() {
                    mListener.onPlayNow();
                }


            }, MediaCategoryType.LATEST), null);

            mMusicPageAdapter.addTab(mTabHost.newTabSpec("two"), "POPULAR", ListMusicFragment.newInstance(new IListMusicListener() {

                @Override
                public void onAddTrackToQueue(List<Track> listTrack) {
                    MusicFragment.this.mListener.onAddTrackToQueue(listTrack);
                }

                @Override
                public void goToMusicDetail(MediaItem selectedMediaItem) {
                    MusicFragment.this.mListener.onGoToMusicDetail(selectedMediaItem);
                }

                @Override
                public void popBackStack() {
                    // Implement later
                }

                @Override
                public void onPlayNow() {
                    mListener.onPlayNow();
                }


            }, MediaCategoryType.POPULAR), null);

            mMusicPageAdapter.addTab(mTabHost.newTabSpec("three"), "DISCOVER", DiscoveryMusicFragment.newInstance(new IDiscoveryListener() {

                @Override
                public void onGoToDiscoveryPlayer(String mood, List<Track> listTracks) {
                    mListener.onGoToDiscoveryPlayer(mood, listTracks);
                }
            }), null);

            if(isVisible()){
                this.mViewPager.setAdapter(mMusicPageAdapter);
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
                        if (fragment instanceof ListMusicFragment) {
                            ((ListMusicFragment) fragment).resetMusicLayout();
                        }
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
//            this.mViewPager.setOnTouchListener(new View.OnTouchListener(){
//
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    return true;
//                }
//            });
        }

    }

    public void vHandleMusicClicks(View v) {
        final int currentPos = this.mViewPager.getCurrentItem();
        MusicPageAdapter pageAdapter = (MusicPageAdapter) this.mViewPager.getAdapter();

        switch (currentPos) {
            case 0:
                ((ListMusicFragment) pageAdapter.getItem(currentPos)).vHandlePopularMusicClicks(v);
                break;

            case 1:
                ((ListMusicFragment) pageAdapter.getItem(currentPos)).vHandlePopularMusicClicks(v);
                break;
            case 2:
                ((DiscoveryMusicFragment) pageAdapter.getItem(currentPos)).vHandleDiscoveryClick(v);
                break;
            default:
                break;
        }

        // Handle Header's button.
        Fragment fragment = pageAdapter.getItem(currentPos);
        switch (v.getId()) {
            case R.id.btn_back_main_menu:
                if (fragment instanceof ListMusicFragment) {
//                    ((ListMusicFragment) fragment).resetPlayerService();
                }
                getFragmentManager().popBackStack();
                break;

            case R.id.btn_universal_player:
                if (fragment instanceof ListMusicFragment) {
                    ((ListMusicFragment) fragment).resetMusicLayout();
//                    ((ListMusicFragment) fragment).resetPlayerService();
                }

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

        if (fragment instanceof ListMusicFragment) {
            ((ListMusicFragment) fragment).handleCacheState();
        }
    }

    @Override
    public void onStop() {
        // Reset all ListMusicFragment
        MusicPageAdapter pageAdapter = (MusicPageAdapter) mViewPager.getAdapter();
        for (int i = 0; i < pageAdapter.getCount(); i++) {
            final Fragment fragment = pageAdapter.getItem(i);
            if (fragment instanceof ListMusicFragment) {
                ((ListMusicFragment) fragment).resetMusicLayout();
            }
        }

        super.onStop();
    }

    public interface IMusicListener {
        void onAddTrackToQueue(List<Track> listTrack);

        void onGoToDiscoveryPlayer(String mood, List<Track> listTracks);

        void onGoToMusicPlayer();

        void onGoToMusicDetail(MediaItem selectedMediaItem);

        void onPlayNow();
    }
}
