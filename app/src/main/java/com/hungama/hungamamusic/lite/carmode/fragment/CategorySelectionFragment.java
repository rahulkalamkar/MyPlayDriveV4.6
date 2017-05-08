package com.hungama.hungamamusic.lite.carmode.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.hungama.hungamamusic.lite.R;
import com.hungama.hungamamusic.lite.carmode.adapters.SelectionListAdapter;
import com.hungama.hungamamusic.lite.carmode.listeners.EndlessScrollListener;
import com.hungama.hungamamusic.lite.carmode.util.GlobalFunction;
import com.hungama.hungamamusic.lite.carmode.view.CustomDialogLayout;
import com.hungama.hungamamusic.lite.carmode.view.VerticalSeekBar;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MusicCategoriesResponse;
import com.hungama.myplay.activity.data.dao.hungama.MusicCategoryGenre;
import com.hungama.myplay.activity.data.dao.hungama.MyPreferencesResponse;
import com.hungama.myplay.activity.operations.hungama.MediaCategoriesOperation;
import com.hungama.myplay.activity.operations.hungama.PreferencesSaveOperation;
import com.hungama.myplay.activity.util.Utils;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CategorySelectionFragment extends Fragment {

    public static final String TAG = CategorySelectionFragment.class.getSimpleName();

    private SelectionListAdapter mCateListAdapter;
    private DataManager mDataManager;
    private MusicCategoriesResponse repsonseCategories;
    private boolean isStartTracking;

    // UI Elements
    private ImageButton btnCancel;
    private ListView lvCategories;
    private ProgressBar pbLoad;
    private VerticalSeekBar sbListViewProgress;
    private CustomDialogLayout mDialog;

    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View view = (View) inflater.inflate(R.layout.carmode_fragment_category_selection, container, false);

        btnCancel = (ImageButton) view.findViewById(R.id.btn_cancel);
        pbLoad = (ProgressBar) view.findViewById(R.id.pb_load);
        lvCategories = (ListView) view.findViewById(R.id.lv_categories);
        sbListViewProgress = (VerticalSeekBar) view.findViewById(R.id.sb_listview_progress);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        btnCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        mDataManager = DataManager.getInstance(getActivity());
        vInitCategoryData();

        this.sbListViewProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

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
                    lvCategories.setSelection(progress);
                }
            }

        });

    }

    private void vInitCategoryData() {
        CommunicationManager communicationManager = new CommunicationManager();
        communicationManager.performOperationAsync(new MediaCategoriesOperation(getActivity(), mDataManager.getServerConfigurations()
                        .getHungamaServerUrl_2(), mDataManager.getServerConfigurations().getHungamaAuthKey(), MediaContentType.MUSIC, mDataManager
                        .getApplicationConfigurations().getPartnerUserId(), mDataManager.getApplicationConfigurations().getCategoriesGenerTimeStamp()),
                new CommunicationOperationListener() {

                    @Override
                    public void onSuccess(int operationId, Map<String, Object> responseObjects) {
                        if (responseObjects.containsKey(MediaCategoriesOperation.RESULT_KEY_OBJECT_CATEGORIES)) {
                            repsonseCategories = (MusicCategoriesResponse) responseObjects.get(MediaCategoriesOperation.RESULT_KEY_OBJECT_CATEGORIES);

                            if (repsonseCategories.getCategories() != null) {
                                List<String> listCates = repsonseCategories.getCategories();
                                String initCategory = mDataManager.getApplicationConfigurations().getSelctedMusicPreference();
                                int initPos = listCates.indexOf((String) initCategory);

                                mCateListAdapter = new SelectionListAdapter(getActivity(), listCates, initPos);

                                if(isVisible()){
                                    lvCategories.setAdapter(mCateListAdapter);
                                }

                                lvCategories.setOnItemClickListener(new OnItemClickListener() {

                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        mCateListAdapter.setSelectPos(position);
                                        mCateListAdapter.notifyDataSetChanged();

                                        // Save to Preferences
                                        String strSelectedCategory = (String) parent.getItemAtPosition(position);
                                        mDataManager.getApplicationConfigurations().setSelctedMusicPreference(strSelectedCategory);

                                        vSavePreferences(strSelectedCategory, "", true);
                                    }
                                });

                                lvCategories.setOnScrollListener(new EndlessScrollListener(null) {

                                    @Override
                                    public void onSyncWithSeekbar(int firstVisibleItem) {
                                        sbListViewProgress.setProgress(firstVisibleItem);
                                    }

                                    @Override
                                    public void onLoadMore(int page, int totalItemsCount) {
                                    }
                                });

                                sbListViewProgress.setMax(listCates.size());

                            } else if(isVisible()){
                                lvCategories.setAdapter(null);
                            }
                        }

                        pbLoad.setVisibility(View.GONE);
                    }

                    @Override
                    public void onStart(int operationId) {
                        pbLoad.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
                        pbLoad.setVisibility(View.GONE);
                    }
                }, getActivity());
    }

    public void vHandleCategoryClick(View v) {
        int view_id = v.getId();

        switch (view_id) {
            case R.id.btn_scroll_up:
                sbListViewProgress.setProgress(sbListViewProgress.getProgress() - 1);
                lvCategories.setSelection(sbListViewProgress.getProgress());
                break;

            case R.id.btn_scroll_down:
                sbListViewProgress.setProgress(sbListViewProgress.getProgress() + 1);
                lvCategories.setSelection(sbListViewProgress.getProgress());
                break;

            default:
                break;
        }
    }

    private void vSavePreferences(final String categoryName, String genre, boolean updateToServer) {

        try {
            Set<String> tags = Utils.getTags();

            for (String category : repsonseCategories.getCategories()) {
                if (categoryName.equals(category)) {
                    tags.add(category.toLowerCase());
                } else {
                    tags.remove(category.toLowerCase());
                }
            }

            for (MusicCategoryGenre category : repsonseCategories.getGenres()) {
                for (String genreName : category.getGenre()) {
                    tags.remove("genre_" + genreName);
                }
            }
            if (!TextUtils.isEmpty(genre)) {
                tags.add("genre_" + genre);
            }

            Utils.AddTag(tags);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (updateToServer) {
            mDataManager.saveMyPreferences("", new CommunicationOperationListener() {

                @Override
                public void onSuccess(int operationId, Map<String, Object> responseObjects) {
                    MyPreferencesResponse myPreferencesResponse = (MyPreferencesResponse) responseObjects
                            .get(PreferencesSaveOperation.RESPONSE_KEY_PREFERENCES_SAVE);

                    ApplicationConfigurations appConfigs = mDataManager.getApplicationConfigurations();

                    if (myPreferencesResponse.getCode() == 1 // SUCCESS.
                            || myPreferencesResponse.getCode() == 200) {

                        appConfigs.setMusicLatestTimeStamp(null);
                        appConfigs.setMusicPopularTimeStamp(null);
                        appConfigs.setVideoLatestTimeStamp(null);
                        appConfigs.setLiveRadioTimeStamp(null);
                        appConfigs.setOnDemandTimeStamp(null);

                        mDataManager.getCacheManager().storeMusicLatestResponse("", null);
                        mDataManager.getCacheManager().storeMusicFeaturedResponse("", null);
                        mDataManager.getCacheManager().storeVideoLatestResponse("", null);
                        mDataManager.getCacheManager().storeLiveRadioResponse("", null);
                        mDataManager.getCacheManager().storeCelebRadioResponse("", null);

                        appConfigs.setSearchPopularTimeStamp(null);

                        Intent new_intent = new Intent();
                        new_intent.setAction("preference_change");
                        new_intent.putExtra("preference_change", true);
                        new_intent.putExtra("selectedLanguage", categoryName);
                        if (getActivity() != null) {
                            getActivity().sendBroadcast(new_intent);
                            mDialog.hide();

                            // Show SUCCESSFUL message and back to Main Menu.
                            GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE,
                                    getResources().getString(R.string.my_preferences_saved_categories), new CustomDialogLayout.IDialogListener() {
                                        @Override
                                        public void onPositiveBtnClick() {

                                        }

                                        @Override
                                        public void onNegativeBtnClick() {

                                        }

                                        @Override
                                        public void onNegativeBtnClick(CustomDialogLayout layout) {

                                        }

                                        @Override
                                        public void onMessageAction() {
                                            getFragmentManager().popBackStack();
                                        }
                                    });
                        }
                    }
                }

                @Override
                public void onStart(int operationId) {
                    mDialog = GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE_FORCE_CLOSE,
                            getResources().getString(R.string.msg_save_category), null);
                }

                @Override
                public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
                    mDialog.hide();
                    mDialog.hide(); // Dismiss dialog.

                    GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, errorMessage, new CustomDialogLayout.IDialogListener() {

                        @Override
                        public void onPositiveBtnClick() {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onNegativeBtnClick() {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onNegativeBtnClick(CustomDialogLayout layout) {

                        }

                        @Override
                        public void onMessageAction() {
                            getFragmentManager().popBackStack();
                        }
                    });
                }
            }, categoryName, genre);

        }

        boolean strCatePrefSelectionGeneric = mDataManager.getApplicationConfigurations().getCategoryPrefSelectionGeneric6();
        if (!strCatePrefSelectionGeneric && !categoryName.equalsIgnoreCase("editors picks")) {
            mDataManager.getApplicationConfigurations().setCategoryPrefSelectionGeneric6(true);
        }

    }

}
