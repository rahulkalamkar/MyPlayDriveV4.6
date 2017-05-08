package com.hungama.hungamamusic.lite.carmode.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.hungama.hungamamusic.lite.R;
import com.hungama.hungamamusic.lite.carmode.adapters.RadioListAdapter;
import com.hungama.hungamamusic.lite.carmode.adapters.RadioListAdapter.IRadioListAdapterListener;
import com.hungama.hungamamusic.lite.carmode.util.GlobalFunction;
import com.hungama.hungamamusic.lite.carmode.view.CustomDialogLayout;
import com.hungama.hungamamusic.lite.carmode.view.CustomDialogLayout.DialogType;
import com.hungama.hungamamusic.lite.carmode.view.VerticalSeekBar;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.operations.hungama.WebRadioOperation;
import com.hungama.myplay.activity.player.PlayMode;
import com.hungama.myplay.activity.ui.CarModeHomeActivity;

import java.util.List;
import java.util.Map;

public class ListRadioFragment extends ListFragment implements CommunicationOperationListener {

    public static final String TAG = ListRadioFragment.class.getSimpleName();

    private RadioListAdapter mRadioListAdapter;
    private DataManager mDataManager;
    private List<MediaItem> mListMediaItems;

    //UI Elements
    private View mRootView;
    private FrameLayout flList;
    private VerticalSeekBar sbListViewProgress;
    private SwipeRefreshLayout mListViewContainer;
    private CustomDialogLayout mDialog;

    private PlayMode mRadioMode;
    private IListRadioFragment mListener;

    public static ListRadioFragment newInstance(PlayMode radioMode, IListRadioFragment listener) {
        ListRadioFragment fragment = new ListRadioFragment();
        fragment.setRadioMode(radioMode);
        fragment.setListener(listener);
        return fragment;
    }

    public void setRadioMode(PlayMode radioMode) {
        this.mRadioMode = radioMode;
    }

    public void setListener(IListRadioFragment listener) {
        this.mListener = listener;
    }

    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = (View) inflater.inflate(R.layout.carmode_fragment_list_radio, container, false);

            flList = (FrameLayout) mRootView.findViewById(R.id.fl_list);
            sbListViewProgress = (VerticalSeekBar) mRootView.findViewById(R.id.sb_listview_progress);
            mListViewContainer = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipeRefreshLayout_listView);
        }

        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mDataManager = DataManager.getInstance(getActivity());

        // Listview's seekbar
        sbListViewProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // only scroll list when seekbar is changed by user
                if (fromUser == true) {
                    getListView().setSelection(progress);
                }
            }
        });

        getListView().setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    sbListViewProgress.setMax(totalItemCount - visibleItemCount);
                }
                sbListViewProgress.setProgress(firstVisibleItem);

                // Enable/Disable SwipeToRefresh view
                boolean enable = false;
                if (view != null && view.getChildCount() > 0) {
                    // check if the first item of the list is visible
                    boolean firstItemVisible = view.getFirstVisiblePosition() == 0;
                    // check if the top of the first item is visible
                    boolean topOfFirstItemVisible = view.getChildAt(0).getTop() == 0;
                    // enabling or disabling the refresh layout
                    enable = firstItemVisible && topOfFirstItemVisible;
                }
                mListViewContainer.setEnabled(enable);
            }
        });

        if (mRadioListAdapter == null) { // First load data.
            vLoadData();
        }

        mListViewContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (mListMediaItems != null) {
                    mListMediaItems.clear(); // Reset data before fetching data.
                }
                vLoadData(); // Fetching Data.
            }
        });

        mListViewContainer.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_orange_light, android.R.color.holo_green_light,
                android.R.color.holo_red_light);
        mListViewContainer.setDistanceToTriggerSync(100);
    }

    private void vLoadData() {
        String timestamp_cache = "";

        switch (mRadioMode) {
            case LIVE_STATION_RADIO:
                timestamp_cache = mDataManager.getApplicationConfigurations().getLiveRadioTimeStamp();
                mDataManager.getRadioLiveStations(this, timestamp_cache);
                break;

            case TOP_ARTISTS_RADIO:
                timestamp_cache = mDataManager.getApplicationConfigurations().getOnDemandRadioTimeStamp();
                mDataManager.getRadioTopArtists(this, timestamp_cache);
                break;

            default:
                break;
        }

    }

    @Override
    public void onSuccess(int operationId, Map<String, Object> responseObjects) {
        flList.setVisibility(View.VISIBLE);

        @SuppressWarnings("unchecked")
        List<MediaItem> mediaItems = (List<MediaItem>) responseObjects.get(WebRadioOperation.RESULT_KEY_OBJECT_MEDIA_ITEM);

        if (mediaItems != null) {
            mListMediaItems = mediaItems;
            mRadioListAdapter = new RadioListAdapter(getActivity(), mListMediaItems);
            mRadioListAdapter.setListener(new IRadioListAdapterListener() {

                @Override
                public void onGoToRadioPlayer(MediaItem radioItem) {
                    mListener.onGoToRadioPlayer(radioItem, mRadioMode);
                }
            });

            if (isVisible()) {
                getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        final MediaItem radioItem = (MediaItem) parent.getItemAtPosition(position);
                        mListener.onGoToRadioPlayer(radioItem, mRadioMode);
                    }
                });

                getListView().setAdapter(mRadioListAdapter);
            }
        }

        mListViewContainer.setRefreshing(false);
//        mDialog.hide(); // Dismiss dialog.
        if (getActivity() != null) {
            ((CarModeHomeActivity) getActivity()).hideLoadDialog();
        }
    }

    @Override
    public void onStart(int operationId) {
        flList.setVisibility(View.INVISIBLE);
//        mDialog = GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE_FORCE_CLOSE, getResources().getString(R.string.msg_load_radio),
//                null);
        if (getActivity() != null) {
            ((CarModeHomeActivity) getActivity()).showLoadDialog();
        }
    }

    @Override
    public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
        flList.setVisibility(View.VISIBLE);
        mListViewContainer.setRefreshing(false);
//        mDialog.hide(); // Dismiss dialog.
        if (getActivity() != null) {
            ((CarModeHomeActivity) getActivity()).hideLoadDialog();
        }

        GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, errorMessage, null);
    }

    public void vHandleListRadioClicks(View v) {
        final int view_id = v.getId();
        switch (view_id) {
            case R.id.btn_scroll_up:
                sbListViewProgress.setProgress(sbListViewProgress.getProgress() - 1);
                getListView().setSelection(sbListViewProgress.getProgress());
                break;

            case R.id.btn_scroll_down:
                sbListViewProgress.setProgress(sbListViewProgress.getProgress() + 1);
                getListView().setSelection(sbListViewProgress.getProgress());
                break;

            default:
                break;
        }
    }

    public interface IListRadioFragment {
        void onGoToRadioPlayer(MediaItem radioItem, PlayMode radioMode);
    }

}
