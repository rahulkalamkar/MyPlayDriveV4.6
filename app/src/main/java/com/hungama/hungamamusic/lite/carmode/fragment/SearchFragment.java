package com.hungama.hungamamusic.lite.carmode.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.hungama.hungamamusic.lite.R;
import com.hungama.hungamamusic.lite.carmode.adapters.PopularSearchListAdapter;
import com.hungama.hungamamusic.lite.carmode.view.VerticalSeekBar;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.SearchPopularKeywordOperation;

import java.util.List;
import java.util.Map;

public class SearchFragment extends Fragment implements CommunicationOperationListener {

    public static final String TAG = SearchFragment.class.getSimpleName();

    private DataManager mDataManager;
    private PopularSearchListAdapter mPopularSearchListAdapter;
    private ISearchListener mListener;

    // UI Elements
    private View mRootView;
    private ProgressBar pbLoading;
    private ListView lvPopularSearches;
    private EditText etSearchText;
    private VerticalSeekBar sbListViewProgress;
    private ListView lvListPopularSearch;

    public static SearchFragment newInstance(ISearchListener listener) {
        SearchFragment fragment = new SearchFragment();
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(ISearchListener listener) {
        this.mListener = listener;
    }

    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.carmode_fragment_search, container, false);

            lvPopularSearches = (ListView) mRootView.findViewById(R.id.lv_popular_searches);
            pbLoading = (ProgressBar) mRootView.findViewById(R.id.pb_popular_searches);
            etSearchText = (EditText) mRootView.findViewById(R.id.et_search_text);
            sbListViewProgress = (VerticalSeekBar) mRootView.findViewById(R.id.sb_listview_progress);
            lvListPopularSearch = (ListView) mRootView.findViewById(R.id.lv_popular_searches);
        }
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mDataManager = DataManager.getInstance(getActivity());

        String timestamp_cache = mDataManager.getApplicationConfigurations().getSearchPopularTimeStamp();
        mDataManager.getSearchPopularSerches(getActivity(), this, timestamp_cache);

        etSearchText.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String queryText = etSearchText.getEditableText().toString();
                    mListener.onGoToSearchResult(queryText);

                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etSearchText.getWindowToken(), 0);

                    etSearchText.setText("");

                    return true;
                }

                return false;
            }
        });

        lvListPopularSearch.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    sbListViewProgress.setMax(totalItemCount - visibleItemCount);
                }
                sbListViewProgress.setProgress(firstVisibleItem);
            }
        });

        sbListViewProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

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
                    lvListPopularSearch.setSelection(progress);
                }
            }

        });

    }

    @Override
    public void onStart(int operationId) {
        pbLoading.setVisibility(View.VISIBLE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(int operationId, Map<String, Object> responseObjects) {
        switch (operationId) {
            case OperationDefinition.Hungama.OperationId.SEARCH_POPULAR_KEYWORDS: {
                List<String> list = (List<String>) responseObjects.get(SearchPopularKeywordOperation.RESULT_KEY_LIST_KEYWORDS);

                mPopularSearchListAdapter = new PopularSearchListAdapter(getActivity(), list);

                if(isVisible()){
                    lvPopularSearches.setAdapter(mPopularSearchListAdapter);
                }

                lvPopularSearches.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String queryText = (String) parent.getItemAtPosition(position);
                        mListener.onGoToSearchResult(queryText);
                    }
                });

            }
            break;

            default:
                break;
        }

        pbLoading.setVisibility(View.GONE);
    }

    @Override
    public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
        pbLoading.setVisibility(View.GONE);

    }

    public void vHandleSearchClicks(View v) {
        int view_id = v.getId();

        switch (view_id) {
            case R.id.btn_back_main_menu:
                getFragmentManager().popBackStack();
                break;

            case R.id.btn_universal_player:
                mListener.onGoToMusicPlayer();
                break;
            case R.id.btn_scroll_up:
                sbListViewProgress.setProgress(sbListViewProgress.getProgress() - 1);
                lvListPopularSearch.setSelection(sbListViewProgress.getProgress());
                break;

            case R.id.btn_scroll_down:
                sbListViewProgress.setProgress(sbListViewProgress.getProgress() + 1);
                lvListPopularSearch.setSelection(sbListViewProgress.getProgress());
                break;
            default:
                break;
        }
    }

    public interface ISearchListener {
        void onGoToSearchResult(String queryText);

        void onGoToMusicPlayer();
    }

}
