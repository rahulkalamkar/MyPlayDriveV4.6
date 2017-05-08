package com.hungama.myplay.activity.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.HungamaApplication;

import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.ThreadPoolManager;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.NoConnectivityException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.Discover;
import com.hungama.myplay.activity.data.dao.hungama.DiscoverSearchResultIndexer;
import com.hungama.myplay.activity.data.dao.hungama.Era;
import com.hungama.myplay.activity.data.dao.hungama.Genre;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MessageFromResponse;
import com.hungama.myplay.activity.data.dao.hungama.Mood;
import com.hungama.myplay.activity.data.dao.hungama.MusicCategoriesResponse;
import com.hungama.myplay.activity.data.dao.hungama.MusicCategoryGenre;
import com.hungama.myplay.activity.data.dao.hungama.Tempo;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.gigya.ShareDialogFragment;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.DiscoverOptionsOperation;
import com.hungama.myplay.activity.operations.hungama.DiscoverSaveOperation;
import com.hungama.myplay.activity.operations.hungama.DiscoverSearchResultsOperation;
import com.hungama.myplay.activity.operations.hungama.HashResultOperation;
import com.hungama.myplay.activity.operations.hungama.HashTagListOperation;
import com.hungama.myplay.activity.operations.hungama.HungamaWrapperOperation;
import com.hungama.myplay.activity.operations.hungama.MediaCategoriesOperation;
import com.hungama.myplay.activity.player.PlayMode;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.ui.dialogs.EraSelectedDialog;
import com.hungama.myplay.activity.ui.dialogs.GenreSelectionDialogNew;
import com.hungama.myplay.activity.ui.dialogs.LanguageSelectedDialog;
import com.hungama.myplay.activity.ui.dialogs.TempoSelectedDialog;
import com.hungama.myplay.activity.ui.fragments.MainFragment;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.ApsalarEvent;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;
import com.hungama.myplay.activity.util.images.DiskLruCache;
import com.lukedeighton.wheelview.WheelView;
import com.lukedeighton.wheelview.adapter.WheelArrayAdapter;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DiscoveryActivity extends MainFragment implements OnClickListener,
		CommunicationOperationListener,// OnMediaItemOptionSelectedListener,
		PrefrenceDialogListener {

	private static final String TAG = "DiscoveryActivity";

	// public static final String DATA_EXTRA_DISCOVER = "data_extra_discover";
	// public static final String DATA_EXTRA_DISCOVER_USERID =
	// "data_extra_discover_userid";

	public static final String ARGUMENT_MOOD = "argument_mood";
	// public static final String ARGUMENT_CATEGORIES = "argument_categories";
	private List<Mood> mMoods = new ArrayList<Mood>();

	// public enum CategoryType {
	// SUB_CATEGORY, GENRE
	// }

	private FragmentManager mFragmentManager;
	// private PlayerBarFragment mPlayerBarFragment;

	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	private LanguageTextView mTextTitle;
	private ImageButton mButtonOptions;

	private Discover mDiscover;
	// private List<CategoryTypeObject> mCategoryTypeObjects;

	// private OnFragmentEditModeStateChangedListener
	// mOnFragmentEditModeStateChangedListener;

	// boolean mHasCategoriesChanged = false;

	// storing the last fragment that was in selected mood.
	// Fragment mLastSelectedEditModeFragment;

	// private List<Category> mCategories;
	private MusicCategoriesResponse musicCategoriesResponse;

	private static DiscoveryActivity mDiscoveryActivity;
	private ActionBar mActionBar;

	// ======================================================
	// Activity life-cycle.
	// ======================================================

	ImageView iv_Disc;
	private int selectedPosition = -1;
	private ImageView selection;
	private ImageView iv_Needle;
	private WheelView wheelView;
	private int duration_handle = 400;
	private int duration = 2000;

	private int tag_stopped = 0;
	int tag_running = 1;
	private ImageView startStopButton;

	private float angle;

	private int needleAngle = 25;
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_IMAGE_BIG = "image_big";
    private static final String KEY_IMAGE_SMALL = "image_small";

	static final String ACTION_DISVERY_PREFERENCE_CHANGE = "preference_Discovery_change";
	private BroadcastReceiver discoveryChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			mDiscover = (Discover) bundle
					.getSerializable(DiscoveryActivity.ARGUMENT_MOOD);
			if (!Utils.isConnected()
					&& !mApplicationConfigurations.getSaveOfflineMode()) {
				try {
					((MainActivity) getActivity())
							.internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
								@Override
								public void onRetryButtonClicked() {
									getDiscoveryResult();
								}
							});			
					return;
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			}
			getDiscoveryResult();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_discovery);

		mDataManager = DataManager.getInstance(getActivity());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		mFragmentManager = ((MainActivity) getActivity())
				.getSupportFragmentManager();

		// if (getActivity() instanceof DiscoveryResultActivity) {
		// mActionBar = ((DiscoveryResultActivity) getActivity())
		// .getSupportActionBar();
		// // mPlayerBarFragment = ((DiscoveryResultActivity)
		// // getActivity()).getPlayerBar();
		// }
		if (Utils.isListEmpty(mMoods)) {
			mMoods = mDataManager.getStoredMoods();
		}

		int density = getResources().getDisplayMetrics().densityDpi;

		switch (density) {
		case DisplayMetrics.DENSITY_HIGH:
			needleAngle = 28;
			break;
		}

		mDiscover = Discover.createNewDiscover();
		mDiscover.setHashTag(null);
		fillUpMood();
		// setTextForPredefineDiscovery();

		// GET HASH TAG
		mDataManager.getHashTagList(getActivity(), this, null);

		registeredReceiver();
	}

	private void fillUpMood() {
		if (mMoods == null || Utils.isListEmpty(mMoods)) {
			if (getActivity() != null) {
//				Utils.makeText(getActivity(), "Loading moods...",
//						Toast.LENGTH_LONG).show();
				ThreadPoolManager.getInstance().submit(new MoodAsync1());
			}
		} else {
			// res_ = new int[mMoods.size()];
			// res_large = new int[mMoods.size()];
			//
			// for (int i = 0; i < mMoods.size(); i++) {
			// Mood temp_main = mMoods.get(i);
			// Logger.e("temp_main.getName()", "" + temp_main.getName());
			// if (temp_main.getName().equalsIgnoreCase("Heart Broken")) {
			// Mood temp = new Mood(temp_main.getId(),
			// temp_main.getName(), ""
			// + R.drawable.discovery_large_007, ""
			// + R.drawable.ic_07_moods);
			// mMoods.set(i, temp);
			// res_[i] = R.drawable.ic_07_moods;
			// res_large[i] = R.drawable.discovery_large_007;
			// } else if (temp_main.getName().equalsIgnoreCase("Sad")) {
			// Mood temp = new Mood(temp_main.getId(),
			// temp_main.getName(), ""
			// + R.drawable.discovery_large_004, ""
			// + R.drawable.ic_04_moods);
			// mMoods.set(i, temp);
			// res_[i] = R.drawable.ic_04_moods;
			// res_large[i] = R.drawable.discovery_large_004;
			// } else if (temp_main.getName().equalsIgnoreCase("Chilled Out")) {
			// Mood temp = new Mood(temp_main.getId(),
			// temp_main.getName(), ""
			// + R.drawable.discovery_large_003, ""
			// + R.drawable.ic_03_moods);
			// mMoods.set(i, temp);
			// res_[i] = R.drawable.ic_03_moods;
			// res_large[i] = R.drawable.discovery_large_003;
			// } else if (temp_main.getName().equalsIgnoreCase("Happy")) {
			// Mood temp = new Mood(temp_main.getId(),
			// temp_main.getName(), ""
			// + R.drawable.discovery_large_006, ""
			// + R.drawable.ic_06_moods);
			// mMoods.set(i, temp);
			// res_[i] = R.drawable.ic_06_moods;
			// res_large[i] = R.drawable.discovery_large_006;
			// } else if (temp_main.getName().equalsIgnoreCase("Ecstatic")) {
			// Mood temp = new Mood(temp_main.getId(),
			// temp_main.getName(), ""
			// + R.drawable.discovery_large_005, ""
			// + R.drawable.ic_05_moods);
			// mMoods.set(i, temp);
			// res_[i] = R.drawable.ic_05_moods;
			// res_large[i] = R.drawable.discovery_large_005;
			// } else if (temp_main.getName().equalsIgnoreCase("Romantic")) {
			// Mood temp = new Mood(temp_main.getId(),
			// temp_main.getName(), ""
			// + R.drawable.discovery_large_001, ""
			// + R.drawable.ic_01_moods);
			// mMoods.set(i, temp);
			// res_[i] = R.drawable.ic_01_moods;
			// res_large[i] = R.drawable.discovery_large_001;
			// } else if (temp_main.getName().equalsIgnoreCase("Party")) {
			// Mood temp = new Mood(temp_main.getId(),
			// temp_main.getName(), ""
			// + R.drawable.discovery_large_002, ""
			// + R.drawable.ic_02_moods);
			// mMoods.set(i, temp);
			// res_[i] = R.drawable.ic_02_moods;
			// res_large[i] = R.drawable.discovery_large_002;
			// }
			// }
		}
	}

	private void updateTitle(String title) {
		if (mActionBar != null)
			Utils.setActionBarTitle(getActivity(), mActionBar, title);
	}

	private View rootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (rootView == null) {
			/* Inflate the layout for this fragment */
			rootView = inflater.inflate(R.layout.activity_discovery_new,
					container, false);

			if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
				Utils.traverseChild(rootView, getActivity());
			}

			// if(mPlayerBarFragment==null)
			// mPlayerBarFragment = (PlayerBarFragment) mFragmentManager
			// .findFragmentById(R.id.main_fragmant_player_bar);

			// Bitmap selectedBitmap =
			// BitmapFactory.decodeResource(getResources(),
			// R.drawable.discover_bg_new);
			// byte[] webpImageData =
			// WebPFactory.nativeEncodeBitmap(selectedBitmap, 100);
			// Bitmap bitmap=WebPFactory.nativeDecodeByteArray(webpImageData,
			// null);
			// ImageView img= (ImageView)rootView.findViewById(R.id.img_disk);
			// if (android.os.Build.VERSION.SDK_INT >= 17){
			// img.setImageResource(R.drawable.disk_new);
			// }else{
			// img.setImageResource(R.drawable.disk);
			// }

			initializeUserControls();
			initializeNewDiscovery();
			/*
			 * Checks if calling this activity was to start new Discover or to
			 * view results of a given one.
			 */
		} else {
			Logger.e("HomeMediaTileGridFragment", "onCreateView else");
			ViewGroup parent = (ViewGroup) Utils.getParentViewCustom(rootView);
			parent.removeView(rootView);
		}

		return rootView;
	}

	private void setClickEnable(boolean isClickEnable){
		this.isClickEnable=isClickEnable;
	}

	boolean isClickEnable=true;

	private Handler handle = new Handler();

	@Override
	public void onClick(final View view) {

		int viewId = view.getId();

		if (viewId == R.id.iv_era) {
			if (isClickEnable && mDiscover.getMood() != null) {
				setClickEnable(false);
				Era era = mDiscover.getEra();
				if (era == null) {
					era = new Era(Era.getDefaultFrom(), Era.getDefaultTo());
				}
				EraSelectedDialog dialogn = new EraSelectedDialog();
				dialogn.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						setClickEnable(true);
					}
				});
                dialogn.init(era, this);
				dialogn.show(mFragmentManager, ShareDialogFragment.FRAGMENT_TAG);
			}
		} else if (viewId == R.id.iv_tempo) {
			if (isClickEnable && mDiscover.getMood() != null) {
				setClickEnable(false);
				// if(dialogn==null)
				TempoSelectedDialog dialogn = new TempoSelectedDialog();
				dialogn.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						setClickEnable(true);
					}
				});
				dialogn.init(mDiscover.getTempos(), this);

				dialogn.show(mFragmentManager, "tempo dialog");

			}
		} else if (viewId == R.id.img_close) {
			StopPlaybackAnim(true);
		} else if (viewId == R.id.start_timer_button) {
			if (!Utils.isConnected()
					&& !mApplicationConfigurations.getSaveOfflineMode()) {
				try {
					((MainActivity) getActivity())
							.internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
								@Override
								public void onRetryButtonClicked() {
									view.performClick();
								}
							});			
					return;
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			}
			PointNeedle();
			((Button) rootView.findViewById(R.id.start_timer_button))
					.setEnabled(false);
			getDiscoveryResult();
//			ApsalarEvent.postEvent(getActivity(), ApsalarEvent.DISCOVERY_PLAY);
		} else if (viewId == R.id.rl_discovery_tag) {
			playHashtag(false);
//			ApsalarEvent.postEvent(getActivity(), ApsalarEvent.DISCOVERY_PLAY_HASH);
		}
	}

	private boolean playHashTagDirectly;
	public void playHashtag(boolean isFromPush){
		if (!Utils.isConnected()
				&& !mApplicationConfigurations.getSaveOfflineMode()) {
			try {
				((MainActivity) getActivity())
						.internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
							@Override
							public void onRetryButtonClicked() {
								playHashtag(false);
							}
						});
				return;
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}

		if (list_tag != null && list_tag.size() > 0) {
			mApplicationConfigurations.setNeedTrendDialogShow(true);
			String hash_search = list_tag.get(0);
			mDataManager.getHashTagResutl(getActivity(), this, hash_search,
					null);
			mDiscover.setHashTag(hash_search);
			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap
					.put(FlurryConstants.FlurryDiscoveryParams.DiscoveryOfTheDaySelected
							.toString(), mDiscover.getHashTag());

			Analytics.logEvent(
					FlurryConstants.FlurryEventName.DiscoveryOfTheDay
							.toString(), reportMap);

		}else if(isFromPush){
			playHashTagDirectly=true;
		}

		// to do
		// ((ImageView) view.findViewById(R.id.img_play))
		// .setEnabled(false);
		// getDiscoveryResult();
	}

	private void showCategoryDialog() {
		Mood mood = mDiscover.getMood();

		Logger.e("onMoodSelected", "" + System.currentTimeMillis());
		// Flurry report
		if (mood != null) {
			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(FlurryConstants.FlurryDiscoveryParams.MoodSelected
					.toString(), mood.getName());
			Analytics.logEvent(
					FlurryConstants.FlurryEventName.DiscoveryMood.toString(),
					reportMap);
		}

		// starts the categories selection fragment.
		if (mApplicationConfigurations.getSelctedMusicPreference()
				.equalsIgnoreCase("Editors Picks")
				|| TextUtils.isEmpty(mApplicationConfigurations
						.getSelctedMusicPreference())) {
			mDataManager.getPreferences(this);
		} else {
			isGenreNeedToDisplay(mApplicationConfigurations
					.getSelctedMusicPreference());
		}
	}

	// private List<Genre> getGenres(List<CategoryTypeObject> mCategories) {
	// if (!Utils.isListEmpty(mCategories)) {
	// List<Genre> genres = new ArrayList<Genre>();
	// for (CategoryTypeObject categoryTypeObject : mCategories) {
	// if (categoryTypeObject.getType().equals(
	// CategoryTypeObject.TYPE_GENRE)) {
	// genres.add((Genre) categoryTypeObject);
	// }
	// }
	// return genres;
	// }
	// return null;
	// }
	//
	// private List<Category> getCategories(List<CategoryTypeObject>
	// mCategories) {
	// if (!Utils.isListEmpty(mCategories)) {
	// List<Category> categories = new ArrayList<Category>();
	// for (CategoryTypeObject categoryTypeObject : mCategories) {
	// if (categoryTypeObject.getType().equals(
	// CategoryTypeObject.TYPE_CATEGORY)) {
	// categories.add((Category) categoryTypeObject);
	// }
	// }
	// return categories;
	// }
	// return null;
	// }

	private List<MediaItem> mMediaItems = null;
	private DiscoverSearchResultIndexer mDiscoverSearchResultIndexer;

	private void getDiscoveryResult() {

		mDiscover.setHashTag(null);
		mDataManager.getDiscoverSearchResult(mDiscover,
				mDiscoverSearchResultIndexer, this);
	}

	// ======================================================
	// Fragment's Communication Manager callbacks.
	// ======================================================

	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.DISCOVER_RETRIEVE
				|| operationId == OperationDefinition.Hungama.OperationId.DISCOVER_SAVE
				|| operationId == OperationDefinition.Hungama.OperationId.PREFERENCES_GET
				|| operationId == OperationDefinition.Hungama.OperationId.DISCOVER_SEARCH_RESULT
				|| operationId == OperationDefinition.Hungama.OperationId.DISCOVERY_HASH_TAG_RESULT) {
			// if (getActivity() != null) {
			// ((MainActivity) getActivity())
			// .showLoadingDialogDiscovery(getString(R.string.application_dialog_loading));
			// }
			// showLoadingDialog(R.string.application_dialog_loading);
		}
	}

	private List<String> list_tag;

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			if (operationId == OperationDefinition.Hungama.OperationId.DISCOVER_RETRIEVE) {

			} else if (operationId == OperationDefinition.Hungama.OperationId.DISCOVERY_HASH_TAG) {
				if (responseObjects
						.containsKey(HashTagListOperation.RESULT_KEY_MESSAGE)) {
					MessageFromResponse message = (MessageFromResponse) responseObjects
							.get(HashTagListOperation.RESULT_KEY_MESSAGE);
					if (message.getShowMessage() == 1) {
						Utils.makeText(getActivity(), message.getMessageText(),
								Toast.LENGTH_SHORT).show();
					}
				}

				list_tag = (List<String>) responseObjects
						.get(HashTagListOperation.RESULT_KEY_HASH_TAG_LIST);

				long hash_tobe_display = (Long) responseObjects
						.get(HashTagListOperation.RESULT_show_hash_text);

				if (list_tag != null && list_tag.size() > 0) {

					if (rootView.findViewById(R.id.ll_discovery_detail)
							.getVisibility() == View.GONE || rootView.findViewById(R.id.ll_discovery_detail)
                            .getVisibility() == View.INVISIBLE)
						rootView.findViewById(R.id.ll_discovery_predefine)
								.setVisibility(View.VISIBLE);

					if (list_tag.size() > hash_tobe_display
							&& hash_tobe_display != 0)
						((TextView) rootView
								.findViewById(R.id.discovery_subtitle))
								.setText("#"
										+ list_tag
												.get((int) (hash_tobe_display - 1)));
					else
						((TextView) rootView
								.findViewById(R.id.discovery_subtitle))
								.setText("#" + list_tag.get(0));

					if(playHashTagDirectly){
						playHashTagDirectly=false;
						playHashtag(false);
					}

				} else
					rootView.findViewById(R.id.ll_discovery_predefine)
							.setVisibility(View.GONE);

			} else if (operationId == OperationDefinition.Hungama.OperationId.DISCOVER_SAVE) {

				if (responseObjects
						.containsKey(DiscoverSaveOperation.RESULT_KEY_RESTART_IF_SUCCESS)) {
					boolean shouldRestart = (Boolean) responseObjects
							.get(DiscoverSaveOperation.RESULT_KEY_RESTART_IF_SUCCESS);
					if (shouldRestart) {
						// startNewDiscover(true);
					} else {

						if (!TextUtils.isEmpty(mDiscover.getName())) {
							setTextInTitleBar(mDiscover.getName());
							rootView.findViewById(R.id.main_title_bar)
									.setVisibility(View.GONE);
							updateTitle(mDiscover.getName());
						} else {
							setTextInTitleBar(R.string.discovery_title);
							rootView.findViewById(R.id.main_title_bar)
									.setVisibility(View.GONE);
						}
					}
				}
			} else if (operationId == OperationDefinition.Hungama.OperationId.PREFERENCES_GET) {
				// List<CategoryTypeObject> categoryTypeObjects =
				// (List<CategoryTypeObject>) responseObjects
				// .get(MediaCategoriesOperation.RESULT_KEY_OBJECT_CATEGORIES);
				//
				// if (categoryTypeObjects != null
				// && categoryTypeObjects.size() > 0) {
				// Logger.i(TAG, "Success! " + categoryTypeObjects.toString());
				//
				// mCategories = new ArrayList<Category>();
				//
				// Category category = null;
				// for (CategoryTypeObject categoryTypeObject :
				// categoryTypeObjects) {
				// category = (Category) categoryTypeObject;
				// category.setIsRoot(true);
				// mCategories.add(category);
				// }
				//
				// showCategorySelectionDialog();
				// }

				musicCategoriesResponse = (MusicCategoriesResponse) responseObjects
						.get(MediaCategoriesOperation.RESULT_KEY_OBJECT_CATEGORIES);
				showCategorySelectionDialog();

			} else if (operationId == OperationDefinition.Hungama.OperationId.DISCOVER_SEARCH_RESULT
					|| operationId == OperationDefinition.Hungama.OperationId.DISCOVERY_HASH_TAG_RESULT) {
				try {
					mDiscoverSearchResultIndexer = (DiscoverSearchResultIndexer) responseObjects
							.get(DiscoverSearchResultsOperation.RESULT_KEY_DISCOVER_SEARCH_RESULT_INDEXER);
//					ApsalarEvent.postEvent(getActivity(), ApsalarEvent.DISCOVERED_MUSIC);
					List<MediaItem> mediaItems;
					if (operationId == OperationDefinition.Hungama.OperationId.DISCOVERY_HASH_TAG_RESULT) {
						mediaItems = (List<MediaItem>) responseObjects
								.get(HashResultOperation.RESULT_KEY_HASH_TAG_RESULT);

						Set<String> tags = Utils.getTags();
						if (!tags.contains("hashtag_used")) {
							tags.add("hashtag_used");
							Utils.AddTag(tags);
						}
					} else {
						mediaItems = (List<MediaItem>) responseObjects
								.get(DiscoverSearchResultsOperation.RESULT_KEY_MEDIA_ITEMS);

						Set<String> tags = Utils.getTags();
						if (!tags.contains("discover_used")) {
							tags.add("discover_used");
							Utils.AddTag(tags);
						}
					}

					if (Utils.isListEmpty(mediaItems)) {
						mMediaItems = new ArrayList<MediaItem>();
						if (operationId == OperationDefinition.Hungama.OperationId.DISCOVERY_HASH_TAG_RESULT) {
							Utils.makeText(getActivity(),

							getString(R.string.result_no_content),
									Toast.LENGTH_SHORT).show();
						} else {
							Utils.makeText(
									getActivity(),

									getString(R.string.discovery_results_error_message_no_results),
									Toast.LENGTH_SHORT).show();
							StopPlaybackAnim(true);
						}

					} else {
						mMediaItems = mediaItems;
						MainActivity activity = ((HomeActivity) getActivity());
						if (PlayerService.service != null
								&& PlayerService.service.mDiscover != null)
							PlayerService.service.prevDiscover = PlayerService.service.mDiscover
									.newCopy();
						if (PlayerService.service != null)
							PlayerService.service.mDiscover = mDiscover
									.newCopy();
						activity.mPlayerBarFragment.setDiscovery(mDiscover);
						activity.mPlayerBarFragment.playDiscoveryMusic(
								getTracks(), PlayMode.DISCOVERY_MUSIC);
					}

					((MainActivity) getActivity())
							.hideLoadingDialogNewDiscovery();
					// hideLoadingDialog();
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			}
			// ((MainActivity) getActivity()).hideLoadingDialogNewDiscovery();
			// hideLoadingDialog();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	public List<Track> getTracks() {
		if (!Utils.isListEmpty(mMediaItems)) {
			List<Track> tracks = new ArrayList<Track>();
			for (MediaItem mediaItem : mMediaItems) {
				if (!TextUtils.isEmpty(mediaItem.getTitle())
						&& mediaItem.getTitle().equalsIgnoreCase("no")
						&& !TextUtils.isEmpty(mediaItem.getAlbumName())
						&& mediaItem.getAlbumName().equalsIgnoreCase("no")
						&& !TextUtils.isEmpty(mediaItem.getArtistName())
						&& mediaItem.getArtistName().equalsIgnoreCase("no")) {
				} else {
					Track track = new Track(mediaItem.getId(),
							mediaItem.getTitle(), mediaItem.getAlbumName(),
							mediaItem.getArtistName(), mediaItem.getImageUrl(),
							mediaItem.getBigImageUrl(), mediaItem.getImages(),
							mediaItem.getAlbumId());
					tracks.add(track);
				}
			}

			return tracks;
		}

		return null;
	}

	private void showCategorySelectionDialog() {
		// String[] preferenceList = new String[mCategories.size()];
		// for (int i = 0; i < mCategories.size(); i++) {
		// preferenceList[i] = mCategories.get(i).getName();
		// }

		if (mDiscover != null) {
			LanguageSelectedDialog editNameDialog = LanguageSelectedDialog
					.newInstance();
			editNameDialog.setLangData(getActivity(), musicCategoriesResponse,
					this);
			editNameDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if(TextUtils.isEmpty(mDiscover.getCategory()))
						onLangaugeEditDialog(null);
				}
			});
			editNameDialog.show(mFragmentManager, "LanguageSelectedDialog");

			// LanguageSelectedDialog dialog_lang = new LanguageSelectedDialog(
			// this, mCategories, this);
			// dialog_lang.getCategoriesAndShow();
		}

	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		if (operationId == OperationDefinition.Hungama.OperationId.DISCOVER_SAVE) {
			if (((MainActivity) getActivity()) != null)
				((MainActivity) getActivity()).hideLoadingDialogNewDiscovery();

			// Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
		} else if (operationId == OperationDefinition.Hungama.OperationId.DISCOVER_RETRIEVE
				|| operationId == OperationDefinition.Hungama.OperationId.DISCOVERY_HASH_TAG_RESULT) {
			if (((MainActivity) getActivity()) != null)
				((MainActivity) getActivity()).hideLoadingDialogNewDiscovery();
		}
	}

	// ======================================================
	// Fragment's events callbacks.
	// ======================================================

	// @Override
	// public void onMediaItemOptionPlayNowSelected(MediaItem mediaItem,
	// int position) {
	//
	// showLoadingDialog(R.string.application_dialog_loading_content);
	// new Handler().postDelayed(new Runnable() {
	// @Override
	// public void run() {
	// hideLoadingDialog();
	// }
	// }, 2000);
	//
	// Logger.i(TAG, "Play Now: " + mediaItem.getId());
	// Track track = new Track(mediaItem.getId(), mediaItem.getTitle(),
	// mediaItem.getAlbumName(), mediaItem.getArtistName(),
	// mediaItem.getImageUrl(), mediaItem.getBigImageUrl(),
	// mediaItem.getImages(), mediaItem.getAlbumId());
	// List<Track> tracks = new ArrayList<Track>();
	// tracks.add(track);
	//
	// MainActivity activity = ((DiscoveryResultActivity) getActivity());
	// activity.mPlayerBarFragment.playNow(tracks, null, null);
	// }
	//
	// @Override
	// public void onMediaItemOptionPlayNextSelected(MediaItem mediaItem,
	// int position) {
	// Logger.i(TAG, "Play Next: " + mediaItem.getId());
	// Track track = new Track(mediaItem.getId(), mediaItem.getTitle(),
	// mediaItem.getAlbumName(), mediaItem.getArtistName(),
	// mediaItem.getImageUrl(), mediaItem.getBigImageUrl(),
	// mediaItem.getImages(), mediaItem.getAlbumId());
	// List<Track> tracks = new ArrayList<Track>();
	// tracks.add(track);
	// MainActivity activity = ((DiscoveryResultActivity) getActivity());
	// activity.mPlayerBarFragment.playNext(tracks);
	// }
	//
	// @Override
	// public void onMediaItemOptionAddToQueueSelected(MediaItem mediaItem,
	// int position) {
	// Logger.i(TAG, "Add to queue: " + mediaItem.getId());
	// Track track = new Track(mediaItem.getId(), mediaItem.getTitle(),
	// mediaItem.getAlbumName(), mediaItem.getArtistName(),
	// mediaItem.getImageUrl(), mediaItem.getBigImageUrl(),
	// mediaItem.getImages(), mediaItem.getAlbumId());
	// List<Track> tracks = new ArrayList<Track>();
	// tracks.add(track);
	// MainActivity activity = ((DiscoveryResultActivity) getActivity());
	// activity.mPlayerBarFragment.addToQueue(tracks, null, null);
	// }
	//
	// @Override
	// public void onMediaItemOptionShowDetailsSelected(MediaItem mediaItem,
	// int position) {
	// Logger.i(TAG, "Show Details: " + mediaItem.getId());
	// Intent intent = new Intent(getActivity(), MediaDetailsActivity.class);
	// intent.putExtra(MediaDetailsActivity.EXTRA_MEDIA_ITEM,
	// (Serializable) mediaItem);
	// intent.putExtra(MediaDetailsActivity.FLURRY_SOURCE_SECTION,
	// FlurryConstants.FlurrySourceSection.Discovery.toString());
	// startActivity(intent);
	// }
	//
	// @Override
	// public void onMediaItemOptionRemoveSelected(MediaItem mediaItem,
	// int position) {
	// Logger.i(TAG, "Remove item: " + mediaItem.getId());
	// }

	private ImageView iv_era;

	private ImageView iv_tempo;

	/**
	 * Interface definition to be invoked when the states of the edit mode has
	 * been changed.
	 */
	// public interface OnFragmentEditModeStateChangedListener {
	//
	// public void onStartEditMode(Fragment fragment);
	//
	// public void onStopEditMode(boolean hasDataChanged);
	// }

	// public void setOnFragmentEditModeStateChangedListener(
	// OnFragmentEditModeStateChangedListener listener) {
	// mOnFragmentEditModeStateChangedListener = listener;
	// }

	public void setTextInTitleBar(String title) {
		Utils.SetMultilanguageTextOnTextView(getActivity(), mTextTitle, title);
		// mTextTitle.setText(title);
	}

	public void setTextInTitleBar(int title) {
		Utils.SetMultilanguageTextOnTextView(getActivity(), mTextTitle,
				getResources().getString(title));
		// mTextTitle.setText(getResources().getString(title));
	}

	public Discover getDiscover() {
		return mDiscover;
	}

	// public List<CategoryTypeObject> getCategoryTypeObjects() {
	// if (!Utils.isListEmpty(mCategoryTypeObjects)) {
	// return new ArrayList<CategoryTypeObject>(mCategoryTypeObjects);
	// } else {
	// return new ArrayList<CategoryTypeObject>();
	// }
	// }

	// public boolean hasCategoriesChanged() {
	// return mHasCategoriesChanged;
	// }

	// ======================================================
	// Private helper methods.
	// ======================================================

	private void initializeUserControls() {
		// initializes the title bar text.
		mTextTitle = (LanguageTextView) rootView
				.findViewById(R.id.main_title_bar_text);
		// options.
		mButtonOptions = (ImageButton) rootView
				.findViewById(R.id.main_title_bar_button_options);
		// disable the options button for moods and categories.
		mButtonOptions.setVisibility(View.VISIBLE);
		mButtonOptions.setSelected(false);

	}

	// public void closeAnyEditModeFragment() {
	// if (mLastSelectedEditModeFragment != null) {
	//
	// // clears the state of the last selected (opened) edit mode
	// // fragment.
	// mLastSelectedEditModeFragment = null;
	//
	// // notifies the gallery to be updated as well.
	// if (mOnFragmentEditModeStateChangedListener != null) {
	// mOnFragmentEditModeStateChangedListener.onStopEditMode(false);
	// }
	//
	// // close the fragment.
	// mFragmentManager.popBackStack();
	// }
	// }

	@Override
	public void onStart() {
		super.onStart();
		Logger.e("onStart", "onStart");
		Analytics.startSession(getActivity(), this);
		setText();
		if (mMoods == null || Utils.isListEmpty(mMoods)) {
//			Utils.makeText(getActivity(), "Loading moods...", Toast.LENGTH_LONG)
//					.show();
			ThreadPoolManager.getInstance().submit(new MoodAsync1());
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		Logger.e("onStop", "onStop");
		// HungamaApplication.activityStoped();
		Analytics.onEndSession(getActivity());
	}

	@Override
	public void onPause() {
		Logger.e("onPause", "onPause");
		HungamaApplication.activityPaused();
		super.onPause();
	}

	@Override
	public void onResume() {
		hideLoadingDialog();
		if (mDiscoveryActivity != null && getActivity() instanceof HomeActivity) {
			mDiscoveryActivity = null;
			try {
				// discoveryMoodFragment
				// .resetMoodWithouAnimation(discoveryMoodFragment.rootView
				// .findViewById(R.id.discovery_mood_selected_item_icon));

			} catch (Exception e) {
				e.printStackTrace();
			}
			// startNewDiscover(false);
		}
		HungamaApplication.activityResumed();

		if (mApplicationConfigurations.isSongCatched()
				&& getActivity() instanceof HomeActivity) {
			((HomeActivity) getActivity()).openOfflineGuide();
		}
		super.onResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener
	 * #
	 * onMediaItemOptionSaveOfflineSelected(com.hungama.myplay.activity.data.dao
	 * .hungama.MediaItem, int)
	 */
	// @Override
	// public void onMediaItemOptionSaveOfflineSelected(MediaItem mediaItem,
	// int position) {
	// // TODO Auto-generated method stub
	// }

	// private boolean areEqualTempos(List<Tempo> originalTempos,
	// List<Tempo> newTempos) {
	//
	// if (!Utils.isListEmpty(originalTempos) && !Utils.isListEmpty(newTempos))
	// {
	// return originalTempos.toString().equalsIgnoreCase(
	// newTempos.toString());
	// } else {
	// if (Utils.isListEmpty(originalTempos)
	// && !Utils.isListEmpty(newTempos)) {
	// return false;
	// }
	//
	// if (!Utils.isListEmpty(originalTempos)
	// && Utils.isListEmpty(newTempos)) {
	// return false;
	// }
	// }
	//
	// // both are nulls.
	// return false;
	// }

	private boolean areEqualEras(Era originalEra, Era newEra) {
		if (originalEra != null && newEra != null) {
			return originalEra.equals(newEra);
		} else {
			if (originalEra == null && newEra != null) {
				return false;
			}

			if (originalEra != null && newEra == null) {
				return false;
			}
		}

		// both are nulls.
		return true;
	}

	// private Era mEra = null;

	// List<Tempo> tempos = new ArrayList<Tempo>();

	// /---new changes

	// float ratio = 60;
	private RotateAnimation animation;
	private Runnable animateCicleDisc = new Runnable() {
		@Override
		public void run() {
			if ((Integer) iv_Disc.getTag() != tag_stopped) {
				animation = new RotateAnimation(0, 360, iv_Disc.getWidth() / 2,
						iv_Disc.getHeight() / 2);
				animation.setFillAfter(false);
				animation.setDuration(duration);
				animation.setInterpolator(new LinearInterpolator());

				iv_Disc.startAnimation(animation);
				handle.removeCallbacks(this);
				handle.postDelayed(this, duration);
			} else {
				// if (animation != null)
				// animation.cancel();
			}
		}
	};

	private Runnable animateNeedle = new Runnable() {
		@Override
		public void run() {
			int fromto = iv_Needle.getWidth() / 2;
			if ((Integer) iv_Needle.getTag() != tag_stopped) {
				RotateAnimation animation = new RotateAnimation(0, needleAngle,
						fromto, fromto);
				animation.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationEnd(Animation animation) {
						startCircleAnimation();
					}
				});
				animation.setFillAfter(true);
				animation.setDuration(duration_handle);
				iv_Needle.startAnimation(animation);
			} else {
				RotateAnimation animation = new RotateAnimation(needleAngle, 0,
						fromto, fromto);
				animation.setFillAfter(true);
				animation.setDuration(duration_handle);
				iv_Needle.startAnimation(animation);
			}
		}
	};

	// public void startStopClick(View v) {
	// if (((Integer) v.getTag()) == tag_stopped) {
	// // startStopButton.setText("Stop");
	// rootView.findViewById(R.id.ll_discovery_detail).setVisibility(
	// View.VISIBLE);
	// ((Button) rootView.findViewById(R.id.start_timer_button))
	// .setEnabled(true);
	// setText();
	// iv_era.setImageResource(R.drawable.era_active);
	// iv_tempo.setImageResource(R.drawable.tempo_active);
	// startStopButton.setTag(tag_running);
	// handle.post(animateNeedle);
	// handle.postDelayed(animateRunnable, duration_handle);
	// } else {
	// selection.setVisibility(View.GONE);
	// Animation anim = AnimationUtils.loadAnimation(getActivity(),
	// R.anim.zoom_out);
	// anim.setAnimationListener(new AnimationListener() {
	//
	// @Override
	// public void onAnimationStart(Animation animation) {
	// // TODO Auto-generated method stub
	// }
	//
	// @Override
	// public void onAnimationRepeat(Animation animation) {
	// // TODO Auto-generated method stub
	// }
	//
	// @Override
	// public void onAnimationEnd(Animation animation) {
	// wheelView.deselect();
	// }
	// });
	// selection.startAnimation(anim);
	// startStopButton.setTag(tag_stopped);
	// // startStopButton.setText("Start");
	// mDiscover = Discover.createNewDiscover();
	// rootView.findViewById(R.id.ll_discovery_detail).setVisibility(
	// View.GONE);
	// iv_era.setImageResource(R.drawable.era_non_active);
	// iv_tempo.setImageResource(R.drawable.tempo_non_active);
	// handle.post(animateNeedle);
	// handle.postDelayed(animateRunnable, duration_handle);
	//
	// }
	// }

	public void StopPlaybackAnim(boolean stopDisc) {

		if (selection.getVisibility() == View.VISIBLE && getActivity()!=null) {
			Animation anim = AnimationUtils.loadAnimation(getActivity(),
					R.anim.zoom_out);
			anim.setStartOffset(duration_handle);
			anim.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
					wheelView.deselect();
					// selection
					// .setImageResource(R.drawable.disk_text);
				}
			});

			// if(stopDisc)
			if (((Integer) iv_Disc.getTag()) == tag_running)
				RemoveNeedle(stopDisc);
			else
				hidePlayButton();
			selectedPosition = -1;
			// change for disck
			selection.setVisibility(View.GONE);

			selection.startAnimation(anim);

		}
	}

	private void startCircleAnimation() {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (((Integer) iv_Disc.getTag()) == tag_stopped) {
					Logger.e("startCircleAnimation", "startCircleAnimation");
					iv_Disc.setTag(tag_running);
					handle.postDelayed(animateCicleDisc, duration_handle);
				}
			}
		});
	}

	private void showPlaybutton(final boolean isFromPush) {
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {

				try {
					rootView.findViewById(R.id.ll_discovery_detail)
							.setVisibility(View.VISIBLE);
					rootView.findViewById(R.id.ll_discovery_predefine)
							.setVisibility(View.GONE);
				if(!isFromPush)
					((ScrollView) rootView.findViewById(R.id.svScrollView))
							.smoothScrollTo(
									0,
									rootView.findViewById(
											R.id.ll_discovery_detail)
											.getBottom());

					// ((ScrollView)rootView.findViewById(R.id.svScrollView)).fullScroll(View.FOCUS_DOWN);

					((Button) rootView.findViewById(R.id.start_timer_button))
							.setEnabled(true);
					setText();
					iv_era.setImageResource(R.drawable.era_active);
					iv_tempo.setImageResource(R.drawable.tempo_active);
					iv_era.setEnabled(true);
					iv_tempo.setEnabled(true);

					showAnimationButton();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}, 500);

	}

	private void showAnimationButton() {
		ColorDrawable frame1 = new ColorDrawable(getResources().getColor(
				R.color.color_primary));
		Drawable frame2 = getActivity().getResources().getDrawable(
				R.drawable.background_blue_dialog_btn);
		ColorDrawable frame3 = new ColorDrawable(getResources().getColor(
				R.color.color_primary));
		Drawable frame4 = getActivity().getResources().getDrawable(
				R.drawable.background_blue_dialog_btn);

		AnimationDrawable Anim = new AnimationDrawable();
		Anim.addFrame(frame1, 400);
		Anim.addFrame(frame2, 400);
		Anim.addFrame(frame3, 400);
		Anim.addFrame(frame4, 400);
		Anim.setOneShot(true);

		Button btn = ((Button) rootView.findViewById(R.id.start_timer_button));
		btn.setBackgroundDrawable(Anim);

		Anim.start();
	}

	private void PointNeedle() {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (((Integer) iv_Needle.getTag()) == tag_stopped) {

					iv_Needle.setTag(tag_running);
					handle.post(animateNeedle);
				}
			}
		});
	}

	private void RemoveNeedle(final boolean stopCircle) {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (((Integer) iv_Needle.getTag()) == tag_running) {
					iv_Needle.setTag(tag_stopped);
					handle.post(animateNeedle);
				}
				if (stopCircle)
					stopCircleAnimation(true);
			}
		});

	}

	private void hidePlayButton() {
		if (selection.getVisibility() == View.VISIBLE)
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					try {
						rootView.findViewById(R.id.ll_discovery_detail)
								.setVisibility(View.GONE);
						if (list_tag != null && list_tag.size() > 0) {
							rootView.findViewById(R.id.ll_discovery_predefine)
									.setVisibility(View.VISIBLE);
						} else
							rootView.findViewById(R.id.ll_discovery_predefine)
									.setVisibility(View.GONE);
						((ScrollView) rootView.findViewById(R.id.svScrollView))
								.smoothScrollTo(0, 0);

						((RelativeLayout) rootView
								.findViewById(R.id.rl_discovery_tag))
								.setEnabled(true);
						iv_era.setImageResource(R.drawable.era_non_active);
						iv_tempo.setImageResource(R.drawable.tempo_non_active);
						iv_era.setEnabled(false);
						iv_tempo.setEnabled(false);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}, 500);
	}

	void stopCircleAnimation(final boolean isHide) {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				hidePlayButton();

				if (((Integer) iv_Disc.getTag()) == tag_running) {
					// startStopButton.setText("Start");
					Logger.e("stopCircleAnimation", "stopCircleAnimation");

					if (isHide) {
						isDiskStartpause = false;
						mDiscover = Discover.createNewDiscover();

						iv_Disc.setTag(tag_stopped);
						handle.postDelayed(animateCicleDisc,
								duration_handle * 2);
					} else {
						if (((Integer) iv_Disc.getTag()) == tag_running) {
							iv_Disc.setTag(tag_stopped);
							handle.postDelayed(animateCicleDisc,
									duration_handle * 2);
						}
					}
				}
			}
		});
	}

	public boolean isDiskRunning() {
		return isDiskStartpause;
	}

	private boolean isDiskStartpause;

	private void initializeNewDiscovery() {

		try {
			iv_Disc = (ImageView) rootView.findViewById(R.id.rotate_iv);
			iv_Needle = (ImageView) rootView.findViewById(R.id.needle_iv);
			selection = (ImageView) rootView.findViewById(R.id.selection_iv);
			iv_Needle.setTag(tag_stopped);
			rootView.findViewById(R.id.img_close).setOnClickListener(this);
			rootView.findViewById(R.id.rl_discovery_tag).setOnClickListener(
					this);
			rootView.findViewById(R.id.start_timer_button).setOnClickListener(
					this);

			iv_era = (ImageView) rootView.findViewById(R.id.iv_era);
			iv_tempo = (ImageView) rootView.findViewById(R.id.iv_tempo);

			iv_era.setImageResource(R.drawable.era_non_active);
			iv_tempo.setImageResource(R.drawable.tempo_non_active);

			iv_era.setEnabled(false);
			iv_tempo.setEnabled(false);

			iv_era.setOnClickListener(this);
			iv_tempo.setOnClickListener(this);

			handle = new Handler();
			startStopButton = (ImageView) rootView.findViewById(R.id.img_close);
			iv_Disc.setTag(tag_stopped);

			startStopButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (selection.getVisibility() == View.VISIBLE) {
						StopPlaybackAnim(true);
					}

				}
			});

			selection.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					StopPlaybackAnim(true);
				}
			});

			wheelView = (WheelView) rootView.findViewById(R.id.wheelview);
			int width = getActivity().getWindowManager().getDefaultDisplay()
					.getWidth();
			wheelView.setWheelRadius((int) (width / 3.2));
			wheelView.setWheelItemCount(mMoods != null ? mMoods.size() : 0);
			wheelView.setWheelItemRadius(width / 14);
			wheelView.setWheelColor(Color.TRANSPARENT);

			try {
				// List<Integer> entries = new
				// ArrayList<Integer>(mMoods.size());
				// for (int i = 0; i < mMoods.size(); i++) {
				// entries.add(res_[i]);
				// }
				// // populate the adapter, that knows how to draw each item (as
				// // you would
				// // do with a ListAdapter)
				// wheelView.setAdapter(new MaterialColorAdapter(entries,
				// getActivity()));

				List<String> entries = new ArrayList<String>(mMoods.size());
				for (int i = 0; i < mMoods.size(); i++) {
					entries.add(mMoods.get(i).getSmallImageUrl());
				}
				// populate the adapter, that knows how to draw each item (as
				// you would
				// do with a ListAdapter)
				wheelView.setAdapter(new MaterialColorAdapter(entries,
						getActivity(),mMoods));
			} catch (Exception e) {
				e.printStackTrace();
			}

			// a listener for receiving a callback for when the item closest
			// to the
			// selection angle changes
			wheelView
					.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectListener() {
						@Override
						public void onWheelItemSelected(WheelView parent,
								int position) {
						}
					});

			wheelView.setSelectionAngle(wheelView.getAngleForPosition(1));

			wheelView
					.setOnWheelItemClickListener(new WheelView.OnWheelItemClickListener() {
						@Override
						public void onWheelItemClick(final WheelView wheelView,
								final int position, boolean isSelected) {

							startPlaying(false,position);
						}
					});

			wheelView.setSelectionColor(Color.TRANSPARENT);
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
	}

	private void startPlaying(final boolean isFromPush,final int position){
		ThreadPoolManager.getInstance().submit(new Runnable() {

			@Override
			public void run() {
				try {
					// if ((Integer) iv_Needle.getTag() ==
					// tag_running) {
					getActivity().runOnUiThread(
							new Runnable() {
								public void run() {
									StopPlaybackAnim(false);
								}
							});
					try {
						Thread.sleep((long) (duration_handle * 1.5));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					// }
					isDiskStartpause = true;
					try {
						Thread.sleep(200);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}

					angle = wheelView.getAngle();
					final float newangle = wheelView
							.getAngleForPosition(position);
					final int sleeptime = 20;
					final int _angleDivide = 3;

					if (newangle < angle)
						angle = angle - 360;

					int iteration = Math
							.abs((int) (newangle - angle)
									/ _angleDivide);
//					Logger.s("angle :: " + angle + " :: " + newangle + " :: " + _angleDivide + " :: " + iteration);
					for (int i = 0; i < iteration; i++) {
						try {
							Thread.sleep(sleeptime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (angle + _angleDivide < newangle) {
							angle += _angleDivide;
						} else {
							angle = newangle;
						}

						getActivity().runOnUiThread(
								new Runnable() {
									public void run() {

										wheelView
												.setAngle(
														angle,
														(Math.abs(angle
																- newangle) <= _angleDivide));
									}
								});

						if (Math.abs(angle - newangle) <= _angleDivide)
							break;

					}
					if (iteration == 0) {
						getActivity().runOnUiThread(
								new Runnable() {
									public void run() {
										wheelView
												.setAngle(
														angle,
														true);
									}
								});
					}

					getActivity().runOnUiThread(
							new Runnable() {
								public void run() {
									try {
										if (position < mMoods
												.size()) {
											selectedPosition = position;
											selection
													.setVisibility(View.VISIBLE);
											// selection
											// .setImageResource(res_large[position]);
											// .setImageResource(res_large[position]);
											try {
												Drawable drawable = mDataManager.getMoodIcon(
														mMoods.get(position).getBigImageUrl());
												if (drawable == null) {
													selection
															.setImageResource(mDataManager
																	.getMoodIcon(mMoods
																			.get(position)
																			.getId(), mMoods.get(position).getName(), false));
												} else {
													selection.setImageDrawable(drawable);
												}
											} catch (Exception e) {
												Logger.printStackTrace(e);
												selection
														.setImageResource(mDataManager
																.getMoodIcon(mMoods
																		.get(position)
																		.getId(), mMoods.get(position).getName(), false));
											}

//											selection
//													.setImageResource(mDataManager
//															.getMoodIcon(mMoods
//																	.get(position)
//																	.getId(), mMoods.get(position).getName(), false));
											selection
													.startAnimation(AnimationUtils
															.loadAnimation(
																	getActivity(),
																	R.anim.zoom_in));

											mDiscover
													.setMood(mMoods
															.get(position));
											if (!isFromPush)
												showCategoryDialog();
										}
										showPlaybutton(isFromPush);
										// PointNeedle();
									} catch (Exception e) {
									}
								}
							});
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			}
		});
	}

	MoodSelectedReceiver moodSelectedReceiver;
	public static final String ACTION_DISCOVERY_CHANGE = "action_discovery_change";
	private void registeredReceiver(){

		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_DISVERY_PREFERENCE_CHANGE);
		getActivity().registerReceiver(discoveryChangeReceiver, filter);

		if (moodSelectedReceiver == null) {
			moodSelectedReceiver = new MoodSelectedReceiver();
			IntentFilter filter_close = new IntentFilter();
			filter_close.addAction(ACTION_DISCOVERY_CHANGE);
			getActivity().registerReceiver(moodSelectedReceiver, filter_close);
		}
	}

	private class MoodSelectedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			int selectedMood = intent.getIntExtra("selectedMood",0);
			setSelectionImage(selectedMood);
		}
	}

	public void setSelectionImage(int position) {
		startPlaying(true,position);

	}

	private void setText() {
		try {
			if (mDiscover != null && mDiscover.getMood() != null) {
				((TextView) rootView.findViewById(R.id.discovery_title))
						.setText(mDiscover.getMood().getName());
				String desc = "Era '" + mDiscover.getEra().getFrom() + "-"
						+ mDiscover.getEra().getTo() + "', Tempo '"
						+ mDiscover.getTempos().get(0).name() + "'";
				((TextView) rootView.findViewById(R.id.discovery_discription))
						.setText(desc);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	// private void setTextForPredefineDiscovery() {
	// if (mDiscover != null && mDiscover.getMood() != null) {
	// String desc = "Era '" + mDiscover.getEra().getFrom() + "-"
	// + mDiscover.getEra().getTo() + "', Tempo '"
	// + mDiscover.getTempos().get(0).name() + "'";
	// ((TextView) rootView
	// .findViewById(R.id.discovery_predefine_discription))
	// .setText(desc);
	//
	// }
	// }

	private static class MaterialColorAdapter extends WheelArrayAdapter<String> {
		private Context context;
        private List<Mood> moods;

		MaterialColorAdapter(List<String> entries, Context context,List<Mood> moods) {
			super(entries, context);
			this.context = context;
            this.moods=moods;

		}

		@Override
		public Drawable getDrawable(int position) {
			try {
				Drawable drawable = DataManager.getInstance(context).getMoodIcon(
						getItem(position));
				if (drawable == null) {
					return context.getResources().getDrawable(DataManager.getInstance(context).getMoodIcon(
							moods.get(position).getId(), moods.get(position).getName(), true));
				}
				return drawable;
			} catch (Exception e) {
				Logger.printStackTrace(e);
				return context.getResources().getDrawable(DataManager.getInstance(context).getMoodIcon(
						moods.get(position).getId(), moods.get(position).getName(), true));
			}
		/*	return DataManager.getInstance(context).getMoodIcon(
					getItem(position));*/
		}
	}

	public void openOptions() {
		// TODO Auto-generated method stub

	}

	// public void onOptionsItemAddAllToQueueClicked(View view) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// public void onOptionsItemLoadMyDiscoveriesClicked(View view) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// public void onOptionsItemSaveDiscoveryClicked(View view) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// public void onOptionsItemStartNewDiscovery(View view) {
	// // TODO Auto-generated method stub
	//
	// }

	public void postAd() {
		// TODO Auto-generated method stub

	}

	// public void disableAnyClosingButton() {
	// // TODO Auto-generated method stub
	// }

	// mood get referesh
	private class MoodAsync1 implements Runnable {
//		private DataManager mDataManager;
//
//		private DiskLruCache mDiskLruCache;
//		private File mImagesFile;
//
//		private String mServiceUrl;

		// private String mAuthKey;

		@Override
		public void run() {
			try {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (rootView != null
								&& rootView.findViewById(R.id.progressBar_init) != null)
							rootView.findViewById(R.id.progressBar_init)
									.setVisibility(View.VISIBLE);
					}
				});

				/*mDataManager = DataManager.getInstance(getActivity());
				ServerConfigurations serverConfigurations = mDataManager
						.getServerConfigurations();

				mImagesFile = getActivity().getDir(
						DataManager.FOLDER_MOODS_IMAGES, Context.MODE_PRIVATE);

				mServiceUrl = serverConfigurations.getHungamaServerUrl_2();*/
				// mAuthKey = serverConfigurations.getHungamaAuthKey();

				boolean hasSuccess = false;

				Logger.d(TAG, "Starts prefetching moods.");
				// gets the moods from the servers.
//				CommunicationManager communicationManager = new CommunicationManager();
				try {
					// stores the discover preferences.
//					DiscoverOptionsOperation discoverOptionsOperation = new DiscoverOptionsOperation(
//							mServiceUrl, mDataManager
//									.getApplicationConfigurations()
//									.getPartnerUserId(), mDataManager
//									.getDeviceConfigurations().getHardwareId());
//					Map<String, Object> resultMoodsMap = communicationManager
//							.performOperation(new HungamaWrapperOperation(null,
//									getActivity(), discoverOptionsOperation),
//									getActivity());
                    JSONParser jsonParser = new JSONParser();
                    String response=readFileFromAssets();



                        Map<String, Object> reponseMap = (Map<String, Object>) jsonParser
                                .parse(response);

                        reponseMap = (Map<String, Object>) reponseMap.get("response");
                        Map<String, Object> moodsMap = (Map<String, Object>) reponseMap
                                .get("moods");
                        Map<String, Object> tagsMap = (Map<String, Object>) reponseMap
                                .get("tags");

                        List<Map<String, Object>> moodsMapList = (List<Map<String, Object>>) moodsMap
                                .get("mood");
                        List<Map<String, Object>> tagsMapList = (List<Map<String, Object>>) tagsMap
                                .get("tag");

                        List<Mood> moods = new ArrayList<Mood>();

                        int id;
                        String name;
                        String bigImageUrl;
                        String smallImageUrl;

                        for (Map<String, Object> map : moodsMapList) {
                            id = ((Long) map.get(KEY_ID)).intValue();
                            name = (String) map.get(KEY_NAME);
                            bigImageUrl = (String) map.get(KEY_IMAGE_BIG);
                            smallImageUrl = (String) map.get(KEY_IMAGE_SMALL);
                            moods.add(new Mood(id, name, bigImageUrl, smallImageUrl));
                        }

                        for (Map<String, Object> map : tagsMapList) {
                            id = 0; // tags don't contain any id.
                            name = (String) map.get(KEY_NAME);
                            bigImageUrl = (String) map.get(KEY_IMAGE_BIG);
                            smallImageUrl = (String) map.get(KEY_IMAGE_SMALL);
                            moods.add(new Mood(id, name, bigImageUrl, smallImageUrl));
                        }

                        /*Map<String, Object> resultMap = new HashMap<String, Object>();
                        resultMap.put(DiscoverOptionsOperation.RESULT_KEY_OBJECT_MOODS, moods);





                    Map<String, Object> resultMoodsMap = new HashMap<String, Object>();
					List<Mood> moodslist = (List<Mood>) resultMoodsMap
							.get(DiscoverOptionsOperation.RESULT_KEY_OBJECT_MOODS);*/
					// stores the objects in an internal file dir.
					mDataManager.storeMoods(moods);

					// deletes any existing images
					/*mDiskLruCache = DiskLruCache.open(mImagesFile, 1, 1,
							DataManager.CACHE_SIZE_MOODS_IMAGES);*/
					// mDiskLruCache.delete();

					// for each mood.
					/*for (Mood mood : moods) {
						if (!TextUtils.isEmpty(mood.getBigImageUrl())) {
							HungamaApplication.downloadBitmapToInternalStorage(
									mDiskLruCache, mood.getBigImageUrl());
						}
						if (!TextUtils.isEmpty(mood.getSmallImageUrl())) {
							HungamaApplication.downloadBitmapToInternalStorage(
									mDiskLruCache, mood.getSmallImageUrl());
						}
					}
*/
					Logger.d(TAG, "Done prefetching moods.");
					hasSuccess = true;

					// updates the preferences.
					mDataManager.getApplicationConfigurations()
							.setHasSuccessedPrefetchingMoods(hasSuccess);


				}catch (ParseException e) {
                    e.printStackTrace();
                }
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mMoods = mDataManager.getStoredMoods();
						fillUpMood();
						initializeNewDiscovery();
						try {
							if (rootView != null)
								rootView.findViewById(R.id.progressBar_init)
										.setVisibility(View.GONE);
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
				});

			} catch (Exception e) {
				Logger.e(TAG, "Failed to prefetch moods.**" + e);
			} catch (Error e) {
				Logger.e(TAG, "Failed to prefetch moods.((" + e);
			}
//			try {
//				Thread.sleep(10000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			ThreadPoolManager.getInstance().submit(new MoodAsyncApiCall());
		}
	}

//	private class MoodAsyncApiCall extends Thread {
//		private DataManager mDataManager;
//
//		private DiskLruCache mDiskLruCache;
//		private File mImagesFile;
//
//		private String mServiceUrl;
//
//		// private String mAuthKey;
//
//		@Override
//		public void run() {
//			try {
////				getActivity().runOnUiThread(new Runnable() {
////					@Override
////					public void run() {
////						if (rootView != null
////								&& rootView.findViewById(R.id.progressBar_init) != null)
////							rootView.findViewById(R.id.progressBar_init)
////									.setVisibility(View.VISIBLE);
////					}
////				});
//
//				mDataManager = DataManager.getInstance(getActivity());
//				ServerConfigurations serverConfigurations = mDataManager
//						.getServerConfigurations();
//
//				mImagesFile = getActivity().getDir(
//						DataManager.FOLDER_MOODS_IMAGES, Context.MODE_PRIVATE);
//
//				mServiceUrl = serverConfigurations.getHungamaServerUrl_2();
//				// mAuthKey = serverConfigurations.getHungamaAuthKey();
//
//				boolean hasSuccess = false;
//
//				Logger.d(TAG, "Starts prefetching moods.");
//				// gets the moods from the servers.
//				CommunicationManager communicationManager = new CommunicationManager();
////				try {
//					// stores the discover preferences.
//					DiscoverOptionsOperation discoverOptionsOperation = new DiscoverOptionsOperation(
//							mServiceUrl, mDataManager
//							.getApplicationConfigurations()
//							.getPartnerUserId(), mDataManager
//							.getDeviceConfigurations().getHardwareId());
//					Map<String, Object> resultMoodsMap = communicationManager
//							.performOperation(new HungamaWrapperOperation(null,
//											getActivity(), discoverOptionsOperation),
//									getActivity());
//					List<Mood> moods = (List<Mood>) resultMoodsMap
//							.get(DiscoverOptionsOperation.RESULT_KEY_OBJECT_MOODS);
//					// stores the objects in an internal file dir.
//					mDataManager.storeMoods(moods);
//
//					// deletes any existing images
//					mDiskLruCache = DiskLruCache.open(mImagesFile, 1, 1,
//							DataManager.CACHE_SIZE_MOODS_IMAGES);
//					// mDiskLruCache.delete();
//
//					// for each mood.
//					for (Mood mood : moods) {
//						if (!TextUtils.isEmpty(mood.getBigImageUrl())) {
//							HungamaApplication.downloadBitmapToInternalStorage(
//									mDiskLruCache, mood.getBigImageUrl());
//						}
//						if (!TextUtils.isEmpty(mood.getSmallImageUrl())) {
//							HungamaApplication.downloadBitmapToInternalStorage(
//									mDiskLruCache, mood.getSmallImageUrl());
//						}
//					}
//
//					Logger.d(TAG, "Done prefetching moods.");
//					hasSuccess = true;
//
//					// updates the preferences.
//					mDataManager.getApplicationConfigurations()
//							.setHasSuccessedPrefetchingMoods(hasSuccess);
//
////				} catch (InvalidRequestException e) {
////					e.printStackTrace();
////					Logger.e(TAG, "Failed to prefetch moods.>>" + e);
////				} catch (InvalidResponseDataException e) {
////					e.printStackTrace();
////					Logger.e(TAG, "Failed to prefetch moods.##" + e);
////				} catch (OperationCancelledException e) {
////					e.printStackTrace();
////					Logger.e(TAG, "Failed to prefetch moods.$$" + e);
////				} catch (NoConnectivityException e) {
////					e.printStackTrace();
////					Logger.e(TAG, "Failed to prefetch moods.%%" + e);
////				} catch (IOException e) {
////					e.printStackTrace();
////					Logger.e(TAG, "Failed to create / delete cache.&&" + e);
////				}
//				getActivity().runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//
//						mMoods = mDataManager.getStoredMoods();
//						fillUpMood();
//						initializeNewDiscovery();
////						try {
////							if (rootView != null)
////								rootView.findViewById(R.id.progressBar_init)
////										.setVisibility(View.GONE);
////						} catch (Exception e) {
////							// TODO: handle exception
////						}
//
//						if(selection.getVisibility() == View.VISIBLE && selectedPosition != -1) {
//							try {
//								Drawable drawable = mDataManager.getMoodIcon(
//										mMoods.get(selectedPosition).getBigImageUrl());
//								if (drawable == null) {
//									selection
//											.setImageResource(mDataManager
//													.getMoodIcon(mMoods
//															.get(selectedPosition)
//															.getId(), mMoods.get(selectedPosition).getName(), false));
//								} else {
//									selection.setImageDrawable(drawable);
//								}
//							} catch (Exception e) {
//								Logger.printStackTrace(e);
//								selection
//										.setImageResource(mDataManager
//												.getMoodIcon(mMoods
//														.get(selectedPosition)
//														.getId(), mMoods.get(selectedPosition).getName(), false));
//							}
//						}
//					}
//				});
//			} catch (InvalidRequestException e) {
//				e.printStackTrace();
//				Logger.e(TAG, "Failed to prefetch moods.>>" + e);
//			} catch (InvalidResponseDataException e) {
//				e.printStackTrace();
//				Logger.e(TAG, "Failed to prefetch moods.##" + e);
//			} catch (OperationCancelledException e) {
//				e.printStackTrace();
//				Logger.e(TAG, "Failed to prefetch moods.$$" + e);
//			} catch (NoConnectivityException e) {
//				e.printStackTrace();
//				Logger.e(TAG, "Failed to prefetch moods.%%" + e);
//			} catch (IOException e) {
//				e.printStackTrace();
//				Logger.e(TAG, "Failed to create / delete cache.&&" + e);
//			} catch (Exception e) {
//				Logger.e(TAG, "Failed to prefetch moods.**" + e);
//			} catch (Error e) {
//				Logger.e(TAG, "Failed to prefetch moods.((" + e);
//			}
//		}
//	}

	@Override
	public void onTempoEditDialog(ArrayList<Tempo> temp) {
		// TODO Auto-generated method stub
		((Button) rootView.findViewById(R.id.start_timer_button))
				.setEnabled(true);

		mDiscover.setTempos(temp);
		setText();
	}

	@Override
	public void onEraEditDialog(Era era) {
		// TODO Auto-generated method stub
		Era originalEra = mDiscover.getEra();

		if (!areEqualEras(originalEra, era)) {
			mDiscover.setEra(era);
			setText();
			((Button) rootView.findViewById(R.id.start_timer_button))
					.setEnabled(true);
		}
	}

	@Override
	public void onLangaugeEditDialog(String mCategory) {
		if (!TextUtils.isEmpty(mCategory)) {
			List<MusicCategoryGenre> genres = musicCategoriesResponse
					.getGenres();
			if (genres != null && genres.size() > 0) {
				for (MusicCategoryGenre genre : genres) {
					if (mCategory.equals(genre.getCategory())) {
						showGenresForCategory(genre);
						return;
					}
				}
			}
			showAnimationButton();
			mDiscover.setCategory(mCategory);
			mDiscover.setGenre("");
		} else
			StopPlaybackAnim(true);
	}

	private void isGenreNeedToDisplay(String mCategory) {
		boolean isNeedtodisplay = false;
		if (!TextUtils.isEmpty(mCategory)) {
			final String preferencesResponse = mDataManager
					.getApplicationConfigurations()
					.getMusicPreferencesResponse();
			try {
				musicCategoriesResponse = new Gson().fromJson(
						preferencesResponse.toString(),
						MusicCategoriesResponse.class);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			List<MusicCategoryGenre> genres = musicCategoriesResponse
					.getGenres();
			if (genres != null && genres.size() > 0) {
				for (MusicCategoryGenre genre : genres) {
					if (mCategory.equals(genre.getCategory())) {
						isNeedtodisplay = true;
						showGenresForCategory(genre);
						break;
					}
				}
			}
			if (!isNeedtodisplay) {
				mDiscover.setGenre("");
				mDiscover.setCategory(mApplicationConfigurations
						.getSelctedMusicPreference());
			}
		}
	}

	private void showGenresForCategory(final MusicCategoryGenre categoryGenre) {
		GenreSelectionDialogNew genreSelectionDialogNew = GenreSelectionDialogNew
				.newInstance();
		genreSelectionDialogNew.setLangData(getActivity(),
				categoryGenre.getGenre(),
				new GenreSelectionDialogNew.GenereSelectionDialogListener() {
					@Override
					public void onGenreEditDialog(String genre) {
						if (genre != null) {
							mDiscover.setCategory(categoryGenre.getCategory());
							mDiscover.setGenre(genre);
							showAnimationButton();
						}
					}
				});
		genreSelectionDialogNew.show(mFragmentManager, "GenreSelectionDialog");
	}

	@Override
	public void onMoodEditDialog(Mood mood,int position) {
		mDiscover.setMood(mood);
	}

	@Override
	public void onGenreEditDialog(Genre genre) {

	}

	// @Override
	// public void onMediaItemOptionPlayAndOpenSelected(MediaItem mediaItem,
	// int position) {
	//
	// }

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(discoveryChangeReceiver!=null)
			getActivity().unregisterReceiver(discoveryChangeReceiver);
		if(moodSelectedReceiver!=null)
			getActivity().unregisterReceiver(moodSelectedReceiver);
		if (rootView != null) {
			try {
				int version = Integer.parseInt(""
						+ android.os.Build.VERSION.SDK_INT);
				Utils.unbindDrawables(rootView, version);
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}
	}

	public void updateHashList() {
		mDataManager.getHashTagList(getActivity(), this, null);
	}

    private String readFileFromAssets() {

        try {
            InputStream input = getContext().getAssets().open("moods.json");

            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();

            // byte buffer into a string
            String text = new String(buffer);
            return text;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



}
