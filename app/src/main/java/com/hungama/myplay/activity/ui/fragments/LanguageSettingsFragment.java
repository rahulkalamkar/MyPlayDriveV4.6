/**
 * 
 */
package com.hungama.myplay.activity.ui.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.LanguageResponse;
import com.hungama.myplay.activity.data.dao.hungama.LanguageSaveResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.LanguageListSettingsOperation;
import com.hungama.myplay.activity.operations.hungama.LanguagePostOperation;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.SettingsActivity;
import com.hungama.myplay.activity.ui.dialogs.MyProgressDialog;
import com.hungama.myplay.activity.ui.widgets.CustomAlertDialog;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author DavidSvilem
 * 
 */
public class LanguageSettingsFragment extends DialogFragment implements
		OnItemClickListener, CommunicationOperationListener {

	private static final String TAG = "LanguageSettingsFragment";

	private ListView listview;
	private MyProgressDialog mProgressDialog;
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	private LanguageSettingAdapter adapter;

	private static final int SUCCESS = 1;
	private String selected_lang = "English";
	private LanguageResponse selectedLanguage = null;
	private boolean isFromHomeActivity = false;

//	private ObjLanguagePackage lp;
	private ArrayList<LanguageResponse> arrlanglist;

	public LanguageSettingsFragment() {
		this.isFromHomeActivity = false;
	}

//	public LanguageSettingsFragment(boolean isFromHomeActivity) {
//		this.isFromHomeActivity = isFromHomeActivity;
//	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(
				DialogFragment.STYLE_NO_TITLE,
				android.support.v7.appcompat.R.style.Theme_AppCompat_Light_Dialog);

		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		Analytics.postCrashlitycsLog(getActivity(), LanguageSettingsFragment.class.getName());
	}

	// ======================================================
	// Fragment life cycle and listeners.
	// ======================================================

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (getDialog() != null)
			getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Fetch the root view
		View rootView = inflater.inflate(
				R.layout.fragment_language_settings_new, container, false);

		if (mApplicationConfigurations.getUserSelectedLanguage() != Constants.LANGUAGE_ENGLISH) {
			Utils.traverseChild(rootView, getActivity());
		}
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));
		listview = (ListView) rootView.findViewById(R.id.language_listview);

		((RelativeLayout) listview.getParent()).setVisibility(View.GONE);
		// get languages for display
		mDataManager.getAllLanguages(this);
		return rootView;
	}

	/**
	 * Language setting adapter
	 * 
	 * @author hungama
	 *
	 */
	public class LanguageSettingAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private Context context;
		int selected;

		public class ViewHolder {
			RadioButton radio_button;
			LanguageTextView tv_language;
		}

		public LanguageSettingAdapter(Context context) {

			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.context = context;
			mInflater = LayoutInflater.from(this.context);
			selected = 0;
		}

		@Override
		public int getCount() {
			if(arrlanglist!=null)
				return arrlanglist.size();
			else
				return 0;
		}

		public void select(int position) {
			this.selected = position;
			notifyDataSetInvalidated();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(
						R.layout.settings_languages_row_layout, null);
				holder = new ViewHolder();
				holder.radio_button = (RadioButton) convertView
						.findViewById(R.id.radio_button);
				holder.tv_language = (LanguageTextView) convertView
						.findViewById(R.id.tv_language);
				convertView.setTag(holder);
			} else
				holder = (ViewHolder) convertView.getTag();

			holder.radio_button.setChecked((selected == position));
//			switch (position) {
//			case 0:
//				if (mApplicationConfigurations.getUserSelectedLanguage() == Constants.LANGUAGE_ENGLISH) {
//					holder.tv_language.setText("English (selected)");
//					holder.radio_button.setChecked(true);
//				} else {
//					holder.tv_language.setText(getResources().getString(
//							R.string.lang_english));
//					holder.radio_button.setChecked(false);
//				}
//				break;
//			case 1:
//				if (mApplicationConfigurations.getUserSelectedLanguage() == Constants.LANGUAGE_HINDI) {
//					Utils.SetMultilanguageTextOnTextView(
//							context,
//							holder.tv_language,
//							getResources().getString(
//									R.string.lang_hindi_converted),
//							RevConstants.LANG_HINDI, true);
//
//					holder.radio_button.setChecked(true);
//				} else {
//					Utils.SetMultilanguageTextOnTextView(
//							context,
//							holder.tv_language,
//							getResources().getString(
//									R.string.lang_hindi_converted),
//							RevConstants.LANG_HINDI, false);
//					holder.radio_button.setChecked(false);
//				}
//
//				break;
//			case 2:
//				if (mApplicationConfigurations.getUserSelectedLanguage() == Constants.LANGUAGE_TAMIL) {
//					Utils.SetMultilanguageTextOnTextView(
//							context,
//							holder.tv_language,
//							getResources().getString(
//									R.string.lang_tamil_converted),
//							RevConstants.LANG_TAMIL, true);
//					holder.radio_button.setChecked(true);
//				} else {
//					Utils.SetMultilanguageTextOnTextView(
//							context,
//							holder.tv_language,
//							getResources().getString(
//									R.string.lang_tamil_converted),
//							RevConstants.LANG_TAMIL, false);
//					holder.radio_button.setChecked(false);
//				}
//
//				break;
//			case 3:
//				if (mApplicationConfigurations.getUserSelectedLanguage() == Constants.LANGUAGE_TELUGU) {
//					Utils.SetMultilanguageTextOnTextView(
//							context,
//							holder.tv_language,
//							getResources().getString(
//									R.string.lang_telugu_converted),
//							RevConstants.LANG_TELUGU, true);
//					holder.radio_button.setChecked(true);
//				} else {
//					Utils.SetMultilanguageTextOnTextView(
//							context,
//							holder.tv_language,
//							getResources().getString(
//									R.string.lang_telugu_converted),
//							RevConstants.LANG_TELUGU, false);
//					holder.radio_button.setChecked(false);
//				}
//
//				break;
//			case 4:
//				if (mApplicationConfigurations.getUserSelectedLanguage() == Constants.LANGUAGE_PUNJABI) {
//					Utils.SetMultilanguageTextOnTextView(
//							context,
//							holder.tv_language,
//							getResources().getString(
//									R.string.lang_punjabi_converted),
//							RevConstants.LANG_PUNJABI, true);
//					holder.radio_button.setChecked(true);
//				} else {
//					Utils.SetMultilanguageTextOnTextView(
//							context,
//							holder.tv_language,
//							getResources().getString(
//									R.string.lang_punjabi_converted),
//							RevConstants.LANG_PUNJABI, false);
//					holder.radio_button.setChecked(false);
//				}
//
//				break;
//			default:
//				break;
//			}
//Operation id ::::
			LanguageResponse language = arrlanglist.get(position);
//			System.out.println("Language :::::::::: " + language.getLanguage());
			if (mApplicationConfigurations.getUserSelectedLanguage() == language.getId()) {
				holder.tv_language.setText(language.getDisplayText() + " (selected)");
				holder.radio_button.setChecked(true);
			} else {
				holder.tv_language.setText(language.getDisplayText());
				holder.radio_button.setChecked(false);
			}

			return convertView;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,
			long arg3) {
		if (arg2 == 0) {
			return;
		}

		adapter.select(arg2 - 1);
		selectedLanguage = arrlanglist.get(arg2 - 1);
		selected_lang = selectedLanguage.getLanguage();
		if (selected_lang.equals(mApplicationConfigurations.getUserSelectedLanguageText())) {
			return;
		}
		CustomAlertDialog alertDialog = new CustomAlertDialog(getActivity());
		alertDialog.setMessage(Utils.getMultilanguageTextHindi(getActivity(),
				getResources().getString(R.string.mag_language_selection))
				+ " " + selected_lang + " ?");
		alertDialog.setPositiveButton(Utils.getMultilanguageText(getActivity(),
				getString(R.string.exit_dialog_text_ok)),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
//						SettingsActivity.ReoladHomeScreen = true;
//						final String tagToRemove;
////						switch (mApplicationConfigurations
////								.getUserSelectedLanguage()) {
////						case Constants.LANGUAGE_HINDI:
////							tagToRemove = "pref_display_"
////									+ getResources().getString(
////											R.string.lang_hindi);
////							break;
////						case Constants.LANGUAGE_TAMIL:
////							tagToRemove = "pref_display_"
////									+ getResources().getString(
////											R.string.lang_tamil);
////							break;
////						case Constants.LANGUAGE_TELUGU:
////							tagToRemove = "pref_display_"
////									+ getResources().getString(
////											R.string.lang_telugu);
////							break;
////						case Constants.LANGUAGE_PUNJABI:
////							tagToRemove = "pref_display_"
////									+ getResources().getString(
////											R.string.lang_punjabi);
////							break;
////						case Constants.LANGUAGE_ENGLISH:
////						default:
////							tagToRemove = "pref_display_"
////									+ getResources().getString(
////											R.string.lang_english);
////							break;
////						}
//
//						if(!TextUtils.isEmpty(mApplicationConfigurations.getUserSelectedLanguageText())){
//							tagToRemove = "pref_display_"
//									+ mApplicationConfigurations.getUserSelectedLanguageText();
//						} else{
//							tagToRemove = "pref_display_"
//									+ getResources().getString(
//											R.string.lang_english);
//						}
//
//						mApplicationConfigurations
//								.setUserSelectedLanguageText(selected_lang);
////						if (selected_lang.equals(getResources().getString(
////								R.string.lang_english))) {
////							mApplicationConfigurations
////									.setUserSelectedLanguage(Constants.LANGUAGE_ENGLISH);
////							Utils.changeLanguage(
////									getActivity().getBaseContext(), "English");
////						} else if (selected_lang.equals(getResources()
////								.getString(R.string.lang_hindi))) {
////							mApplicationConfigurations
////									.setUserSelectedLanguage(Constants.LANGUAGE_HINDI);
////							Utils.changeLanguage(
////									getActivity().getBaseContext(), "Hindi");
////						} else if (selected_lang.equals(getResources()
////								.getString(R.string.lang_tamil))) {
////							mApplicationConfigurations
////									.setUserSelectedLanguage(Constants.LANGUAGE_TAMIL);
////							Utils.changeLanguage(
////									getActivity().getBaseContext(), "Tamil");
////						} else if (selected_lang.equals(getResources()
////								.getString(R.string.lang_telugu))) {
////							mApplicationConfigurations
////									.setUserSelectedLanguage(Constants.LANGUAGE_TELUGU);
////							Utils.changeLanguage(
////									getActivity().getBaseContext(), "Telugu");
////						} else if (selected_lang.equals(getResources()
////								.getString(R.string.lang_punjabi))) {
////							mApplicationConfigurations
////									.setUserSelectedLanguage(Constants.LANGUAGE_PUNJABI);
////							Utils.changeLanguage(
////									getActivity().getBaseContext(), "Punjabi");
////						}
//						mApplicationConfigurations
//									.setUserSelectedLanguage(selectedLanguage.getId());
//						Utils.changeLanguage(
//								getActivity().getBaseContext(), selectedLanguage.getLanguage());
//						Map<String, String> reportMap = new HashMap<String, String>();
//						reportMap.put(
//								FlurryConstants.FlurryLanguage.LanguageSelected
//										.toString(), selected_lang);
//						Analytics
//								.logEvent(
//										FlurryConstants.FlurryLanguage.DisplayLanguageSelected
//												.toString(), reportMap);

						// getFragmentManager().popBackStack();
						mDataManager.postUserLanguageMap(selected_lang,
								LanguageSettingsFragment.this);
						// new TextConversion(getActivity()).execute();

//						try {
//							String tagToAdd = "pref_display_" + selected_lang;
//
//							Set<String> tags = Utils.getTags();
//							if (!tags.contains(tagToAdd)) {
//								if (tags.contains(tagToRemove))
//									tags.remove(tagToRemove);
//								tags.add(tagToAdd);
//								Utils.AddTag(tags);
//							}
//						} catch (Exception e) {
//							Logger.printStackTrace(e);
//						}

					}
				});
		alertDialog.setNegativeButton(
				Utils.getMultilanguageText(getActivity(), "Cancel"), null);
		// alertDialog.create();
		alertDialog.show();

	}

	@Override
	public void onStart(int operationId) {
		if (getActivity() != null)
			if (operationId == OperationDefinition.Hungama.OperationId.LANGUAGE_SETTINGS_POST) {
				showLoadingDialog(Utils.getMultilanguageTextHindi(
						getActivity(),
						getActivity().getResources().getString(
								R.string.setting_language_post_msg)));
			} else {
				showLoadingDialog(Utils.getMultilanguageTextHindi(
						getActivity(),
						getActivity().getResources().getString(
								R.string.processing)));
			}

	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			switch (operationId) {
			case (OperationDefinition.Hungama.OperationId.LANGUAGE_SETTINGS): {
				try {
					arrlanglist = new ArrayList<LanguageResponse>();
					arrlanglist = (ArrayList<LanguageResponse>) responseObjects.get(LanguageListSettingsOperation.RESULT_KEY_LANGUAGE_SETTINGS_LIST);
//					lp = new ObjLanguagePackage(responseObjects.toString());
//					for (int i = 0; i < lp.getLanguagelist().length(); i++) {
//						arrlanglist.add(lp.getLanguagelist().get(i).toString());
//					}
					LanguageTextView headerView = (LanguageTextView) ((LayoutInflater) getActivity()
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
							.inflate(R.layout.settings_title_row_layout, null,
									false);
					Utils.SetMultilanguageTextOnTextView(
							getActivity(),
							headerView,
							getResources().getString(
									R.string.select_language_title));
					((RelativeLayout) listview.getParent())
							.setVisibility(View.VISIBLE);
					listview.setVisibility(View.VISIBLE);
					adapter = new LanguageSettingAdapter(getActivity()
							.getApplicationContext());
					listview.addHeaderView(headerView);
					listview.setAdapter(adapter);
					listview.setOnItemClickListener(LanguageSettingsFragment.this);
					hideLoadingDialog();
				} catch (Exception e) {
				}
			}
				break;

			case (OperationDefinition.Hungama.OperationId.LANGUAGE_SETTINGS_POST): {
				try {
					LanguageSaveResponse mlanguagePostResponse = (LanguageSaveResponse) responseObjects
							.get(LanguagePostOperation.RESULT_KEY_LANGUAGE_POST);
					if (mlanguagePostResponse != null
							&& (mlanguagePostResponse.getCode() == SUCCESS || mlanguagePostResponse
									.getCode() == 200)) {
						SettingsActivity.ReoladHomeScreen = true;
						final String tagToRemove;
						if(!TextUtils.isEmpty(mApplicationConfigurations.getUserSelectedLanguageText())){
							tagToRemove = "pref_display_"
									+ mApplicationConfigurations.getUserSelectedLanguageText();
						} else{
							tagToRemove = "pref_display_"
									+ getResources().getString(
									R.string.lang_english);
						}

						mApplicationConfigurations
								.setUserSelectedLanguageText(selected_lang);
						mApplicationConfigurations
								.setUserSelectedLanguage(selectedLanguage.getId());
						Utils.changeLanguage(
								getActivity().getBaseContext(), selectedLanguage.getLanguage());
						Map<String, String> reportMap = new HashMap<String, String>();
						reportMap.put(
								FlurryConstants.FlurryLanguage.LanguageSelected
										.toString(), selected_lang);
						Analytics
								.logEvent(
										FlurryConstants.FlurryLanguage.DisplayLanguageSelected
												.toString(), reportMap);

						try {
							String tagToAdd = "pref_display_" + selected_lang;

							Set<String> tags = Utils.getTags();
							if (!tags.contains(tagToAdd)) {
								if (tags.contains(tagToRemove))
									tags.remove(tagToRemove);
								tags.add(tagToAdd);
								Utils.AddTag(tags);
							}
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}

						adapter.notifyDataSetChanged();
						Toast toast = Utils.makeText(getActivity(),
								mlanguagePostResponse.getMessage()
										+ ". Application will restart",
								Toast.LENGTH_LONG);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
						if (isFromHomeActivity) {
							getActivity()
									.sendBroadcast(
											new Intent(
													MainActivity.ACTION_LANGUAGE_CHANGED));
						} else {
							getFragmentManager().popBackStack();
							getActivity()
									.sendBroadcast(
											new Intent(
													MainActivity.ACTION_LANGUAGE_CHANGED));
						}
					} else {
						Toast toast = Toast.makeText(getActivity(),
								mlanguagePostResponse.getMessage(),
								Toast.LENGTH_LONG);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				hideLoadingDialog();
			}
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
		switch (operationId) {
		case OperationDefinition.Hungama.OperationId.LANGUAGE_SETTINGS: {
			Logger.i(TAG, "Failed getting languages");
		}
			break;

		case OperationDefinition.Hungama.OperationId.LANGUAGE_SETTINGS_POST: {
			Logger.i(TAG, "Failed posting selected language");
		}
			break;

		default:
			break;
		}

		hideLoadingDialog();
	}

	// Dialog help methods
	public void showLoadingDialog(String message) {
		try {
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (!getActivity().isFinishing()) {
						if (mProgressDialog == null) {
							mProgressDialog = new MyProgressDialog(
									getActivity());
							mProgressDialog.setCancelable(true);
							mProgressDialog.setCanceledOnTouchOutside(false);
						}
					}
				}
			});
		} catch (Exception e) {
		}

	}

	public void hideLoadingDialog() {
		if (mProgressDialog != null && getActivity()!=null && !getActivity().isFinishing()) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

//	public class ObjLanguagePackage extends JSONObject {
//
//		public ObjLanguagePackage(String response) throws JSONException {
//			super(response);
//		}
//
//		public JSONArray getLanguagelist() {
//			// ArrayList<String> list = new ArrayList<String>();
//
//			try {
//				return this.getJSONObject("response").getJSONArray("language");
//
//			} catch (Exception e) {
//			}
//			return null;
//		}
//	}

	@Override
	public void onDestroyView() {
//		System.gc();
//		System.runFinalization();
		super.onDestroyView();
	}

	@Override
	public void onStart() {
		super.onStart();
		Analytics.startSession(getActivity(), this);
	}

	@Override
	public void onStop() {
		super.onStop();
		Analytics.onEndSession(getActivity());
	}
}
