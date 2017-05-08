package com.hungama.hungamamusic.lite.carmode.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hungama.hungamamusic.lite.R;
import com.hungama.hungamamusic.lite.carmode.util.GlobalFunction;
import com.hungama.hungamamusic.lite.carmode.view.CustomDialogLayout;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.NoConnectivityException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.Discover;
import com.hungama.myplay.activity.data.dao.hungama.DiscoverSearchResultIndexer;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.Mood;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.DiscoverOptionsOperation;
import com.hungama.myplay.activity.operations.hungama.DiscoverSearchResultsOperation;
import com.hungama.myplay.activity.operations.hungama.HungamaWrapperOperation;
import com.hungama.myplay.activity.ui.CarModeHomeActivity;
import com.hungama.myplay.activity.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DiscoveryMusicFragment extends Fragment implements CommunicationOperationListener {

    public static final String TAG = DiscoveryMusicFragment.class.getSimpleName();
    private final String MOOD_HEART_BROKEN = "Heart Broken";
    private final String MOOD_SAD = "Sad";
    private final String MOOD_CHILLED_OUT = "Chilled Out";
    private final String MOOD_HAPPY = "Happy";
    private final String MOOD_ECSTATIC = "Ecstatic";
    private final String MOOD_ROMANTIC = "Romantic";
    private final String MOOD_PARTY = "Party";
    private Discover mDiscover;
    private List<Mood> mMoods;
    private DataManager mDataManager;
    private IDiscoveryListener mListener;
    private DiscoverSearchResultIndexer mDiscoverSearchResultIndexer;
    // UI Elements
    private View mRootView;
    private CustomDialogLayout mDialog;

    public static DiscoveryMusicFragment newInstance(IDiscoveryListener listener) {
        DiscoveryMusicFragment fragment = new DiscoveryMusicFragment();
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(IDiscoveryListener listener) {
        this.mListener = listener;
    }

    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.carmode_fragment_discovery, container, false);
        }

        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDataManager = DataManager.getInstance(getActivity());

        // Setup Discovery.
        mDiscover = Discover.createNewDiscover();
        mDiscover.setHashTag(null);

        String strSelectedCategory = mDataManager.getApplicationConfigurations().getSelctedMusicPreference();
        mDiscover.setCategory(strSelectedCategory);

        mMoods = mDataManager.getStoredMoods();

        if (mMoods == null || Utils.isListEmpty(mMoods)) { // No cached Moods are avaiable.
            Log.d(TAG, "Load Mood");
            new LoadMood().execute();
        }

    }

    public void vHandleDiscoveryClick(View v) {
        int view_id = v.getId();

        switch (view_id) {
            case R.id.btn_mood_chilled_out:
                setMoodToDiscovery(MOOD_CHILLED_OUT);
                break;

            case R.id.btn_mood_ecstatic:
                setMoodToDiscovery(MOOD_ECSTATIC);
                break;
            case R.id.btn_mood_happy:
                setMoodToDiscovery(MOOD_HAPPY);

                break;
            case R.id.btn_mood_heart_broken:
                setMoodToDiscovery(MOOD_HEART_BROKEN);
                break;

            case R.id.btn_mood_party:
                setMoodToDiscovery(MOOD_PARTY);

                break;
            case R.id.btn_mood_romantic:
                setMoodToDiscovery(MOOD_ROMANTIC);

                break;
            case R.id.btn_mood_sad:
                setMoodToDiscovery(MOOD_SAD);
                break;

            default:
                break;
        }
    }

    private void setMoodToDiscovery(String moodName) {
        Mood mood = null;
        if (mMoods != null) {
            for (int index = 0; index < mMoods.size(); index++) {
                if (mMoods.get(index).getName().equals(moodName)) {
                    mood = mMoods.get(index);
                    break;
                }
            }

            if (mood != null) {
                mDiscover.setMood(mood);
                mDiscover.setHashTag(null);
                mDataManager.getDiscoverSearchResult(mDiscover, mDiscoverSearchResultIndexer, this);
            } else {
                Log.e(TAG, "Wrong mood !!");
            }
        }
    }

    public List<Track> getTracks() {


        return null;
    }

    @Override
    public void onStart(int operationId) {
//        mDialog = GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE_FORCE_CLOSE,
//                getResources().getString(R.string.msg_fetching_data), null);
        if (getActivity() != null) {
            ((CarModeHomeActivity) getActivity()).showLoadDialog();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(int operationId, Map<String, Object> responseObjects) {
        if (operationId == OperationDefinition.Hungama.OperationId.DISCOVER_SEARCH_RESULT) {

            mDiscoverSearchResultIndexer = (DiscoverSearchResultIndexer) responseObjects
                    .get(DiscoverSearchResultsOperation.RESULT_KEY_DISCOVER_SEARCH_RESULT_INDEXER);

            final List<MediaItem> mediaItems = (List<MediaItem>) responseObjects.get(DiscoverSearchResultsOperation.RESULT_KEY_MEDIA_ITEMS);

            Set<String> tags = Utils.getTags();
            if (!tags.contains("discover_used")) {
                tags.add("discover_used");
                Utils.AddTag(tags);
            }

//            mDialog.hide();
            if (getActivity() != null) {
                ((CarModeHomeActivity) getActivity()).hideLoadDialog();
            }


            if (Utils.isListEmpty(mediaItems)) {
                GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.discovery_results_error_message_no_results), null);
            } else {
                final List<Track> tracks = new ArrayList<Track>();
                for (MediaItem mediaItem : mediaItems) {

                    if (!TextUtils.isEmpty(mediaItem.getTitle()) && mediaItem.getTitle().equalsIgnoreCase("no")
                            && !TextUtils.isEmpty(mediaItem.getAlbumName()) && mediaItem.getAlbumName().equalsIgnoreCase("no")
                            && !TextUtils.isEmpty(mediaItem.getArtistName()) && mediaItem.getArtistName().equalsIgnoreCase("no")) {
                    } else {
                        Track track = new Track(mediaItem.getId(), mediaItem.getTitle(), mediaItem.getAlbumName(), mediaItem.getArtistName(),
                                mediaItem.getImageUrl(), mediaItem.getBigImageUrl(), mediaItem.getImages(), mediaItem.getAlbumId());
                        tracks.add(track);
                    }
                }

                mListener.onGoToDiscoveryPlayer(mDiscover.getMood().getName(), tracks);
            }
        }

    }

    @Override
    public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
//        mDialog.hide();

        if (getActivity() != null) {
            ((CarModeHomeActivity) getActivity()).hideLoadDialog();
        }

        if (errorType != ErrorType.NO_CONNECTIVITY) {
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
    }

    public interface IDiscoveryListener {
        void onGoToDiscoveryPlayer(String mood, List<Track> listTracks);
    }

    // Get the moods from the servers.
    private class LoadMood extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String mServiceUrl = mDataManager.getServerConfigurations().getHungamaServerUrl_2();

            try {
                CommunicationManager communicationManager = new CommunicationManager();
                DiscoverOptionsOperation discoverOptionsOperation = new DiscoverOptionsOperation(mServiceUrl, mDataManager
                        .getApplicationConfigurations().getPartnerUserId(), mDataManager.getDeviceConfigurations().getHardwareId());
                Map<String, Object> resultMoodsMap = communicationManager.performOperation(new HungamaWrapperOperation(null, getActivity(),
                        discoverOptionsOperation), getActivity());

                @SuppressWarnings("unchecked")
                List<Mood> moods = (List<Mood>) resultMoodsMap.get(DiscoverOptionsOperation.RESULT_KEY_OBJECT_MOODS);
                // stores the objects in an internal file dir.
                mDataManager.storeMoods(moods);

            } catch (InvalidRequestException e) {
                Log.e(TAG, "Failed to prefetch moods.>>" + e);
                cancel(true);
            } catch (InvalidResponseDataException e) {
                Log.e(TAG, "Failed to prefetch moods.##" + e);
                cancel(true);
            } catch (OperationCancelledException e) {
                Log.e(TAG, "Failed to prefetch moods.$$" + e);
                cancel(true);
            } catch (NoConnectivityException e) {
                Log.e(TAG, "Failed to prefetch moods.%%" + e);
                cancel(true);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            mMoods = mDataManager.getStoredMoods();

//            for (Mood mood : mMoods) {
//                Log.d(TAG, "list: " + mood.getName());
//                Log.d(TAG, "id: " + mood.getId());
//            }
        }
    }
}
