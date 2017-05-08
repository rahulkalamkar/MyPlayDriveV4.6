/**
 * 
 */
package com.hungama.myplay.activity.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MyStreamSettingsResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MyStreamSettingsOperation;
import com.hungama.myplay.activity.ui.SettingsActivity;
import com.hungama.myplay.activity.ui.adapters.SettingsAdapter;
import com.hungama.myplay.activity.ui.dialogs.MyProgressDialog;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author DavidSvilem
 * 
 */
public class MyStreamSettingsFragment extends Fragment implements
		CommunicationOperationListener, OnCheckedChangeListener {

	private static final String TAG = "MyStreamSettingsFragment";

	public static final String MUSICLISTEN = "musiclisten";
	public static final String LIKES = "likes";
	public static final String DOWNLOADS = "downloads";
	public static final String COMMENTS = "comments";
	public static final String VIDEOWATCHED = "videowatched";
	public static final String SHARES = "shares";
	public static final String BADGES = "badges";
	// public static final String TRIVIA_SHOW = "triviashow";
	// public static final String LYRICS_SHOW = "lyricsshow";

	// Views
	private ListView listview;
	private MyProgressDialog mProgressDialog;
	// Data Members
	private Map<String, Integer> settingsMap;
	// Managers
	private DataManager mDataManager;
	// Adapter
	private SettingsAdapter adapter;

	private ApplicationConfigurations mApplicationConfigurations;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		Analytics.postCrashlitycsLog(getActivity(), MyStreamSettingsFragment.class.getName());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Fetch the root view
		View rootView = inflater.inflate(
				R.layout.fragment_my_stream_setting_new, container, false);
		if (mApplicationConfigurations.getUserSelectedLanguage() == 0) {
			((TextView) rootView.findViewById(R.id.main_title_bar_text))
					.setText(R.string.my_stream_settings);
		} else {
			Utils.SetMultilanguageTextOnTextView(getActivity(),
					((LanguageTextView) rootView
							.findViewById(R.id.main_title_bar_text)),
					getResources().getString(R.string.my_stream_settings));

		}

		listview = (ListView) rootView.findViewById(R.id.listview);

		if (getActivity() instanceof SettingsActivity) {
			String title = getResources().getString(
					R.string.settings_mystream_title_english);
			((SettingsActivity) getActivity()).setTitleBarText(title);
		}

		mDataManager.getMyStreamSettings(this, false, "", 0);

		return rootView;
	}

	/**
	 * show loading dialog
	 */
	public void showLoadingDialog(String message) {
		if (getActivity()!=null && !getActivity().isFinishing()) {
			if (mProgressDialog == null) {
				mProgressDialog = new MyProgressDialog(getActivity());
			}
		}
	}

	/**
	 * hide loading dialog
	 */
	public void hideLoadingDialog() {
		if (mProgressDialog != null && getActivity()!=null && !getActivity().isFinishing()) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	@Override
	public void onStart(int operationId) {
		if(getActivity()!=null)
			showLoadingDialog(Utils.getMultilanguageTextHindi(getActivity(),
					getActivity().getResources().getString(R.string.processing)));
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			switch (operationId) {
			case (OperationDefinition.Hungama.OperationId.MY_STREAM_SETTINGS):
				try {
					MyStreamSettingsResponse response = (MyStreamSettingsResponse) responseObjects
							.get(MyStreamSettingsOperation.RESULT_KEY_MY_STREAM_SETTINGS);

					// Build the map for the adapter
					settingsMap = new HashMap<String, Integer>();
					String[] keys = getResources().getStringArray(
							R.array.my_stream_settings_properties);
					settingsMap.put(keys[0], response.data.musiclisten);
					settingsMap.put(keys[1], response.data.likes);
					settingsMap.put(keys[2], response.data.downloads);
					settingsMap.put(keys[3], response.data.comments);
					settingsMap.put(keys[4], response.data.videowatched);
					settingsMap.put(keys[5], response.data.shares);
					settingsMap.put(keys[6], response.data.badges);

					List<String> propList = new ArrayList<String>();

					propList = Arrays.asList(keys);

					adapter = new SettingsAdapter(getActivity()
							.getApplicationContext(), propList, settingsMap,
							this, mDataManager);

					LanguageTextView headerView = (LanguageTextView) ((LayoutInflater) getActivity()
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
							.inflate(R.layout.settings_title_row_layout, null,
									false);

					headerView.setText(Utils.getMultilanguageTextLayOut(
							getActivity(), getString(R.string.show_feeds_of_)));

					// listview.addHeaderView(headerView);

					listview.setAdapter(adapter);

					hideLoadingDialog();
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
				break;

			case (OperationDefinition.Hungama.OperationId.MY_STREAM_SETTINGS_UPDATE):

				hideLoadingDialog();

				break;
			default:
				break;
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		hideLoadingDialog();
	}

	/**
	 * update the my stream setting as per user selected
	 * 
	 * @param key
	 * @param value
	 */
	private void updateMyStreamSettings(String key, boolean value) {

		int state;
		String streamSettingType = "";

		if (key.equalsIgnoreCase(getActivity().getString(
				R.string.mystream_settings_music_listened_to))) {
			streamSettingType = MUSICLISTEN;
		} else if (key.equalsIgnoreCase(getActivity().getString(
				R.string.mystream_settings_likes))) {
			streamSettingType = LIKES;
		} else if (key.equalsIgnoreCase(getActivity().getString(
				R.string.mystream_settings_downlods))) {
			streamSettingType = DOWNLOADS;
		} else if (key.equalsIgnoreCase(getActivity().getString(
				R.string.mystream_settings_comments))) {
			streamSettingType = COMMENTS;
		} else if (key.equalsIgnoreCase(getActivity().getString(
				R.string.mystream_settings_videos_watched))) {
			streamSettingType = VIDEOWATCHED;
		} else if (key.equalsIgnoreCase(getActivity().getString(
				R.string.mystream_settings_shares))) {
			streamSettingType = SHARES;
		} else if (key.equalsIgnoreCase(getActivity().getString(
				R.string.mystream_settings_badges_earned))) {
			streamSettingType = BADGES;
		}

		if (value) {
			state = 1;
		} else {
			state = 0;
		}

		mDataManager.getMyStreamSettings(this, true, streamSettingType, state);

	}

	@Override
	public void onDestroyView() {
		/*System.gc();
		System.runFinalization();*/
		super.onDestroyView();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		switch (buttonView.getId()) {
		case R.id.switch_button:

			Switch tb = (Switch) buttonView;

			String str = (String) buttonView.getTag();

			updateMyStreamSettings(str, tb.isChecked());

			break;

		default:
			break;
		}
	}
}
