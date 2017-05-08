package com.hungama.hungamamusic.lite.carmode.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.util.Utils;

public class MainMenuFragment extends Fragment {

    public final static String TAG = MainMenuFragment.class.getSimpleName();

    private IMainMenuListener mListeners;

    // UI Elements
    private Button btnCategory;

    public static final MainMenuFragment newInstance(IMainMenuListener listener) {
        MainMenuFragment fragment = new MainMenuFragment();
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(IMainMenuListener listener) {
        this.mListeners = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.carmode_fragment_main_menu, container, false);

        btnCategory = (Button) view.findViewById(R.id.btn_categories);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String strCate = DataManager.getInstance(getActivity()).getApplicationConfigurations().getSelctedMusicPreference();
        btnCategory.setText(strCate);

    }

    public void vHandleMainMenuClicks(View v) {
        final DataManager dataManager = DataManager.getInstance(getActivity());
        final ApplicationConfigurations appConfig = dataManager.getApplicationConfigurations();
        int view_id = v.getId();
        switch (view_id) {
            case R.id.btn_music:
                if (Utils.isConnected() && !appConfig.getSaveOfflineMode()) {
                    mListeners.vNavigateToMusic();
                } else {
                    mListeners.onShowNoNetwork();
                }
                break;

            case R.id.btn_radio:
                if (Utils.isConnected() && !appConfig.getSaveOfflineMode()) {
                    mListeners.vNavigateToRadio();
                } else {
                    mListeners.onShowNoNetwork();
                }
                break;

            case R.id.btn_favorite:
                if (Utils.isConnected() && !appConfig.getSaveOfflineMode()) {
                    mListeners.vNavigateToFavorite();
                } else {
                    mListeners.onShowNoNetwork();
                }
                break;

            case R.id.btn_offline:
                mListeners.vNavigateToOfflineMode();
                break;

            case R.id.btn_search:
                if (Utils.isConnected() && !appConfig.getSaveOfflineMode()) {
                    mListeners.vNavigateToSearch();
                } else {
                    mListeners.onShowNoNetwork();
                }
                break;

            case R.id.btn_playlist:
                if (Utils.isConnected() && !appConfig.getSaveOfflineMode()) {
                    mListeners.vNavigateToPlaylist();
                } else {
                    mListeners.onShowNoNetwork();
                }
                break;

            case R.id.btn_universal_player:
                if (Utils.isConnected() && !appConfig.getSaveOfflineMode()) {
                    mListeners.vOnUniversalPlayerClick();
                } else {
                    mListeners.onShowNoNetwork();
                }
                break;

            case R.id.btn_categories:
                if (Utils.isConnected() && !appConfig.getSaveOfflineMode()) {
                    mListeners.onCategorySelection();
                } else {
                    mListeners.onShowNoNetwork();
                }
                break;

            default:
                break;
        }
    }

    public interface IMainMenuListener {
        void vNavigateToMusic();

        void vNavigateToRadio();

        void vNavigateToSearch();

        void vNavigateToFavorite();

        void vNavigateToPlaylist();

        void vNavigateToOfflineMode();

        void vOnUniversalPlayerClick();

        void onCategorySelection();

        void onShowNoNetwork();
    }

}
