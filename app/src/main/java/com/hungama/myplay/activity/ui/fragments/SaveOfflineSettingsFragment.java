/**
 * 
 */
package com.hungama.myplay.activity.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.ui.SaveOfflineUpdate;
import com.hungama.myplay.activity.ui.SettingsActivity;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

/**
 * @author XTPL
 * 
 */
public class SaveOfflineSettingsFragment extends Fragment implements
		OnClickListener, OnCheckedChangeListener {
	private static final String TAG = "SaveOfflineSettingsFragment";

	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	private Switch save_offline_switchbutton, autosave_switch_button;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDataManager = DataManager.getInstance(getActivity());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		Analytics.postCrashlitycsLog(getActivity(), SaveOfflineSettingsFragment.class.getName());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(
				R.layout.fragment_save_offline_settings_new, container, false);
		save_offline_switchbutton = (Switch) rootView
				.findViewById(R.id.save_offline_settings_toggle_button_cellular);
		autosave_switch_button = (Switch) rootView
				.findViewById(R.id.save_offline_settings_toggle_button_auto_save);

		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(rootView, getActivity());
		}
		initialize(rootView);
		if (getActivity() instanceof SettingsActivity) {
			String title = getResources().getString(
					R.string.save_offline_settings);
			((SettingsActivity) getActivity()).setTitleBarText(title);
		}
		return rootView;
	}

	/**
	 * initialize view and set listener of view
	 * 
	 * @param rootView
	 */
	private void initialize(final View rootView) {

		rootView.findViewById(R.id.save_offline_settings_learn_more)
				.setOnClickListener(this);
		save_offline_switchbutton.setOnCheckedChangeListener(this);
		autosave_switch_button.setOnCheckedChangeListener(this);
		rootView.findViewById(R.id.rl_autosave).setOnClickListener(this);
		rootView.findViewById(R.id.rl_offline_mobile).setOnClickListener(this);

		if (CacheManager.isProUser(getActivity())) {
			save_offline_switchbutton.setChecked(mApplicationConfigurations
					.getSaveOfflineOnCellularNetwork());
			autosave_switch_button.setChecked(mApplicationConfigurations
					.getSaveOfflineAutoSaveMode());

			((SeekBar) rootView
					.findViewById(R.id.save_offline_settings_seek_bar_memory))
					.setProgress(mApplicationConfigurations
							.getSaveOfflineMemoryAllocatedPercentage());

			rootView.findViewById(R.id.save_offline_settings_text_memory)
					.setOnClickListener(null);

			rootView.findViewById(
					R.id.save_offline_settings_textview_memory_allocated)
					.setOnClickListener(null);

			rootView.findViewById(R.id.save_offline_settings_seek_bar_memory)
					.setOnClickListener(null);

			// long maxSize = mApplicationConfigurations
			// .getSaveOfflineMaximumMemoryAllocated();
			((LanguageTextView) rootView
					.findViewById(R.id.save_offline_settings_textview_memory_allocated))
					.setText(CacheManager
							.getLimitedFormattedCacheMemory(getActivity()));
			((LanguageTextView) rootView
					.findViewById(R.id.save_offline_settings_textview_memory_max))
					.setText(CacheManager.getAvailableFormattedCacheMemory());

			((SeekBar) rootView
					.findViewById(R.id.save_offline_settings_seek_bar_memory))
					.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
							Logger.s(" onStopTrackingTouch ::::::::::::::::::::: "
									+ seekBar.getProgress());
							Logger.s(" onProgressChanged ::::::::::::::::::::: "
									+ CacheManager.isCacheSizeAllowed(
											getActivity(),
											seekBar.getProgress()));
							if (CacheManager.isCacheSizeAllowed(getActivity(),
									seekBar.getProgress())) {
							} else {
								Toast.makeText(getActivity(),
										"Please allocate some more memory.",
										Toast.LENGTH_SHORT).show();
								seekBar.setProgress(mApplicationConfigurations
										.getSaveOfflineMemoryAllocatedPercentage());
							}
						}

						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
							Logger.s(" onStartTrackingTouch ::::::::::::::::::::: "
									+ seekBar.getProgress());
						}

						@Override
						public void onProgressChanged(SeekBar seekBar,
								int progress, boolean fromUser) {
							Logger.s(" onProgressChanged ::::::::::::::::::::: "
									+ CacheManager.isCacheSizeAllowed(
											getActivity(), progress));
							if (CacheManager.isCacheSizeAllowed(getActivity(),
									progress)) {
								mApplicationConfigurations
										.setSaveOfflineMemoryAllocatedPercentage(progress);
								((LanguageTextView) rootView
										.findViewById(R.id.save_offline_settings_textview_memory_allocated)).setText(CacheManager
										.getLimitedFormattedCacheMemory(getActivity()));
								((LanguageTextView) rootView
										.findViewById(R.id.save_offline_settings_textview_memory_max)).setText(CacheManager
										.getAvailableFormattedCacheMemory());
							} else {
								// Toast.makeText(getActivity(),
								// "Please allocate some more memory.",
								// Toast.LENGTH_SHORT).show();
								// seekBar.setProgress(mApplicationConfigurations
								// .getSaveOfflineMemoryAllocatedPercentage());
							}
						}
					});
		} else {

			save_offline_switchbutton.setEnabled(false);

			autosave_switch_button.setEnabled(false);
			((SeekBar) rootView
					.findViewById(R.id.save_offline_settings_seek_bar_memory))
					.setEnabled(false);
			((RelativeLayout) rootView.findViewById(
					R.id.save_offline_settings_toggle_button_cellular)
					.getParent()).setEnabled(false);

			rootView.findViewById(R.id.save_offline_settings_text_memory)
					.setOnClickListener(this);

			rootView.findViewById(
					R.id.save_offline_settings_textview_memory_allocated)
					.setOnClickListener(this);

			rootView.findViewById(R.id.save_offline_settings_seek_bar_memory)
					.setOnClickListener(this);

			((LanguageTextView) rootView
					.findViewById(R.id.save_offline_settings_text_cellular))
					.setTextColor(getResources().getColor(
							R.color.membership_detail_text_color));
			((LanguageTextView) rootView
					.findViewById(R.id.save_offline_settings_text_auto_save))
					.setTextColor(getResources().getColor(
							R.color.membership_detail_text_color));

			((LanguageTextView) rootView
					.findViewById(R.id.save_offline_settings_text_memory))
					.setTextColor(getResources().getColor(
							R.color.membership_detail_text_color));
			((LanguageTextView) rootView
					.findViewById(R.id.save_offline_settings_textview_memory_min))
					.setTextColor(getResources().getColor(
							R.color.membership_detail_text_color));
		}
	}

	@Override
	public void onClick(final View arg0) {
		if (arg0.getId() != R.id.save_offline_settings_learn_more
				&& !CacheManager.isProUser(getActivity())) {
			// CacheManager.showUpgradeDialog(getActivity(),
			// new View.OnClickListener() {
			// @Override
			// public void onClick(View v) {
			// try {
			// switch (v.getId()) {
			// case R.id.button_upgrade:
			// Dialog dialog = (Dialog) v.getTag();
			// if (dialog != null) {
			// dialog.dismiss();
			// }
			// getActivity().onBackPressed();
			// Intent intent = new Intent(getActivity(),
			// UpgradeActivity.class);
			// startActivityForResult(
			// intent,
			// HomeActivity.UPGRADE_ACTIVITY_RESULT_CODE);
			// break;
			// case R.id.close_button:
			// dialog = (Dialog) v.getTag();
			// if (dialog != null) {
			// dialog.dismiss();
			// }
			// break;
			// }
			// } catch (Exception e) {
			// Logger.printStackTrace(e);
			// }
			// }
			// });
			Utils.makeText(getActivity(),
					getActivity().getString(R.string.offline_setting_text),
					Toast.LENGTH_SHORT).show();
			return;
		} else {
			Intent intent = new Intent(getActivity(), SaveOfflineUpdate.class);
			intent.putExtra("isLearnMore", true);
			startActivity(intent);
		}

		switch (arg0.getId()) {

		case R.id.rl_offline_mobile:
			if (save_offline_switchbutton.isChecked()) {
				// mApplicationConfigurations
				// .setSaveOfflineOnCellularNetwork(false);
				save_offline_switchbutton.setChecked(false);
			} else {
				// mApplicationConfigurations
				// .setSaveOfflineOnCellularNetwork(true);
				save_offline_switchbutton.setChecked(true);
			}
			break;
		case R.id.rl_autosave:
			if (autosave_switch_button.isChecked()) {
				// mApplicationConfigurations.setSaveOfflineAutoSaveMode(false);
				autosave_switch_button.setChecked(false);
			} else {
				// mApplicationConfigurations.setSaveOfflineAutoSaveMode(true);
				autosave_switch_button.setChecked(true);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onDestroyView() {
//		System.gc();
//		System.runFinalization();
		super.onDestroyView();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.save_offline_settings_toggle_button_cellular:
			mApplicationConfigurations
					.setSaveOfflineOnCellularNetwork(isChecked);
			break;
		case R.id.save_offline_settings_toggle_button_auto_save:
			mApplicationConfigurations.setSaveOfflineAutoSaveMode(isChecked);
			break;
		default:
			break;
		}
	}
}
