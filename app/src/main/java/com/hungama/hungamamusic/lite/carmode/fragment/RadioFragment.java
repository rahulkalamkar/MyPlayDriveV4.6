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
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.player.PlayMode;

public class RadioFragment extends Fragment implements ListRadioFragment.IListRadioFragment {
    public final static String TAG = RadioFragment.class.getSimpleName();

    private MusicPageAdapter mRadioPageAdapter;

    // UI Elements
    private TabHost mTabHost;
    private ViewPager mViewPager;
    private View view;

    private IRadioFragmentListener mListener;

    public static RadioFragment newInstance(IRadioFragmentListener listener) {
        final RadioFragment fragment = new RadioFragment();
        fragment.setListener(listener);

        return fragment;
    }

    public void setListener(IRadioFragmentListener listener) {
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mRadioPageAdapter == null) {
            mRadioPageAdapter = new MusicPageAdapter(getChildFragmentManager(), getActivity(), mTabHost, mViewPager);
            mRadioPageAdapter.addTab(mTabHost.newTabSpec("one"), "Live", ListRadioFragment.newInstance(PlayMode.LIVE_STATION_RADIO, this), null);

            mRadioPageAdapter.addTab(mTabHost.newTabSpec("two"), "On Demand", ListRadioFragment.newInstance(PlayMode.TOP_ARTISTS_RADIO, this), null);

            this.mViewPager.setAdapter(mRadioPageAdapter);
        }
    }

    public void vHandleRadioClicks(View v) {
        final int currentPos = this.mViewPager.getCurrentItem();
        ((ListRadioFragment) mRadioPageAdapter.getItem(currentPos)).vHandleListRadioClicks(v);

        final int view_id = v.getId();

        switch (view_id) {
            case R.id.btn_back_main_menu:
                getFragmentManager().popBackStack();
                break;

            case R.id.btn_universal_player:
                Fragment fragment = mRadioPageAdapter.getItem(currentPos);
                if (fragment instanceof ListMusicFragment) {
                    ((ListMusicFragment) fragment).resetMusicLayout();
                }

                mListener.onGoToMusicPlayer();
                break;
            default:
                break;
        }
    }

    @Override
    public void onGoToRadioPlayer(MediaItem radioItem, PlayMode radioMode) {
        this.mListener.onGoToRadioPlayer(radioItem, radioMode);
    }

    public interface IRadioFragmentListener {
        void onGoToRadioPlayer(MediaItem radioItem, PlayMode radioMode);

        void onGoToMusicPlayer();
    }
}
