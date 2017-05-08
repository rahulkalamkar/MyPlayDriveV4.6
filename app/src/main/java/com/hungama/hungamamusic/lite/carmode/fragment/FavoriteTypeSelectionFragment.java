package com.hungama.hungamamusic.lite.carmode.fragment;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.hungama.hungamamusic.lite.R;
import com.hungama.hungamamusic.lite.carmode.adapters.SelectionListAdapter;

public class FavoriteTypeSelectionFragment extends ListFragment {
    public static final String TAG = FavoriteTypeSelectionFragment.class.getSimpleName();
    public static final String MEDIA_TYPE = "media_type";

    private SelectionListAdapter mMediaTypeAdapter;
    private int initPos = 0;
    private IFavCategoryListener mListener;

    // UI Elements
    private View mRootView;

    public static FavoriteTypeSelectionFragment newInstance(IFavCategoryListener listener, int curMediaType, Fragment targetFragment) {
        FavoriteTypeSelectionFragment fragment = new FavoriteTypeSelectionFragment();
        fragment.setListener(listener);

        Bundle args = new Bundle();
        args.putInt(FavoriteFragment.KEY_MEDIA_TYPE, curMediaType);
        fragment.setArguments(args);
        fragment.setTargetFragment(targetFragment, FavoriteFragment.CODE_SELECTED_MEDIA_TYPE);

        return fragment;
    }

    public void setListener(IFavCategoryListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && !getArguments().isEmpty()) {
            initPos = getArguments().getInt(MEDIA_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = (View) inflater.inflate(R.layout.carmode_fragment_favorite_type_selection, container, false);
        }
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        List<String> listMediaType = Arrays.asList(getResources().getStringArray(R.array.list_media_types));

        mMediaTypeAdapter = new SelectionListAdapter(getActivity(), listMediaType, initPos);
        setListAdapter(mMediaTypeAdapter);

        getListView().setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mMediaTypeAdapter.setSelectPos(position);
                mMediaTypeAdapter.notifyDataSetChanged();

                Intent intent = new Intent();
                intent.putExtra(MEDIA_TYPE, position);
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                getFragmentManager().popBackStack();
            }
        });
    }

    public void vHandleFavCategoryClicks(View v) {
        int view_id = v.getId();

        switch (view_id) {
            case R.id.btn_back_main_menu:
                getFragmentManager().popBackStack();
                break;

            default:
                break;
        }
    }

    public interface IFavCategoryListener {
        void onGoToMusicPlayer();
    }

}
