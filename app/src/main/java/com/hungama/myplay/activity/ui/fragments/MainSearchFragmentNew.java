package com.hungama.myplay.activity.ui.fragments;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.MenuItemCompat.OnActionExpandListener;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.ShowcaseView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.MediaCachingTaskNew;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.campaigns.PlacementType;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.PlayerOption;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.operations.hungama.SearchAutoSuggestOperation;
import com.hungama.myplay.activity.operations.hungama.SearchPopularKeywordOperation;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.MediaDetailsActivity;
import com.hungama.myplay.activity.ui.MediaDetailsActivityNew;
import com.hungama.myplay.activity.ui.VideoActivity;
import com.hungama.myplay.activity.ui.fragments.MainSearchResultsFragment.OnSearchResultsOptionSelectedListener;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.ApsalarEvent;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import com.hungama.myplay.activity.ui.widgets.SearchBar.OnSearchBarStateChangedListener;

/**
 * Presents the Quick Navigation panel within the application.
 */
public class MainSearchFragmentNew extends BackHandledFragment implements
		CommunicationOperationListener,// OnSearchBarStateChangedListener,
		OnSearchResultsOptionSelectedListener, OnClickListener {

	private static final String TAG = "MainSearchFragment";

	private static final int MAXIMUM_SUGGESTIONS_TO_PRESENT = 5;

	public static final String SEARCH_FILTER_TYPE_ALL = ""; // should be empty
	// public static final String SEARCH_FILTER_TYPE_ARTISTS = "Artists";
	public static final String SEARCH_FILTER_TYPE_SONGS = "Song";
	public static final String SEARCH_FILTER_TYPE_ALBUMS = "Album";
	public static final String SEARCH_FILTER_TYPE_PLAYLISTS = "Playlist";
	public static final String SEARCH_FILTER_TYPE_VIDEOS = "Videos";

	private DataManager mDataManager;
	// private PlayerBarFragment mPlayerBarFragment;

	private boolean mHasLoaded = false;

	private InputMethodManager mInputMethodManager;

	private TextView tvSearchCategory;
	// private SearchBar mSearchBar;

	private String backgroundLink;
	private Drawable backgroundImage;
	private Handler h;

	// panels.
//	public ListView mSearchSuggestionsList;

	private ApplicationConfigurations applicationConfigurations;
	private View rootView;
	private Placement placement;

	private boolean isFirstVisitToPage;

	private String mSearchActionSelected = "No search action selected";
    public String actionbar_title = "";
	private int dpi;

	private int width;

	// public static boolean isSearchOpen = false;

	//private Activity getActivity() {
//		return MainSearchFragment.this;
//	}

    DisplayMetrics metrics;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (rootView == null) {
            try {
                rootView = inflater.inflate(R.layout.fragment_main_search_new,
                        container, false);
                Utils.traverseChild(rootView, getActivity());

            } catch (Error e) {
                System.gc();
                System.runFinalization();
                System.gc();
            }

            initializeUserControls(rootView);
            CampaignsManager mCampaignsManager = CampaignsManager
                    .getInstance(getActivity());
            placement = mCampaignsManager
                    .getPlacementOfType(PlacementType.SEARCH_TAG);
            openSearchShowCaseView();

            if (placement == null) {
                rootView.findViewById(R.id.llSearchAdHolder).setVisibility(
                        View.GONE);
                return rootView;
            }

            backgroundLink = Utils.getDisplayProfile(metrics, placement);
            if (backgroundLink != null) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            if (backgroundImage == null) {

                                try {
                                    backgroundImage = Utils.getBitmap(
                                            getActivity(), width, backgroundLink);
                                } catch (Exception e) {
                                }
                            }
                            h.sendEmptyMessage(0);
                        } catch (Exception e) {
                            Logger.printStackTrace(e);
                        }
                    }
                }).start();
            }
            h = new Handler() {
                @SuppressWarnings("deprecation")
                public void handleMessage(android.os.Message msg) {
                    // ((ImageView)rootView.findViewById(R.id.ivAdMainSearch)).setImageDrawable(backgroundImage);
                    if (backgroundImage != null) {
                        try {
                            backgroundImage = Utils.ResizeBitmap(getActivity(),
                                    dpi, width, backgroundImage);
                            final ImageView adView = (ImageView) rootView
                                    .findViewById(R.id.ivAdMainSearch);
                            adView.setBackgroundDrawable(backgroundImage);
                            Utils.postViewEvent(getActivity(), placement);
                            ((ImageView) rootView
                                    .findViewById(R.id.ivHungamaPopularSearch))
                                    .setVisibility(View.GONE);
                            ((ProgressBar) rootView
                                    .findViewById(R.id.pbHungamaPopularSearch))
                                    .setVisibility(View.GONE);
                            adView.setImageDrawable(null);
                            adView.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (results) {
                                        return;
                                    }
                                    try {
                                        Utils.performclickEvent(getActivity(),
                                                placement);
                                    } catch (Exception e) {
                                        Logger.printStackTrace(e);
                                    }
                                }
                            });

                            adView.postDelayed(new Runnable() {
                                public void run() {
                                    try {
                                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) adView
                                                .getLayoutParams();
                                        params.width = width;
                                        params.height = (backgroundImage
                                                .getIntrinsicHeight() * width)
                                                / backgroundImage
                                                .getIntrinsicWidth();
                                        adView.setLayoutParams(params);
                                        adView.setVisibility(View.VISIBLE);
                                    } catch (Exception e) {
                                        Logger.printStackTrace(e);
                                    }
                                }
                            }, 100);
                        } catch (Exception e) {
                        } catch (Error e) {
                        }
                    }
                }
            };

            actionbar_title = getResources().getString(
                    R.string.main_actionbar_search);

//            FrameLayout layout = (FrameLayout) ((MainActivity)getActivity()).findViewById(R.id.home_browse_by_fragmant_container);
//            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layout
//                    .getLayoutParams();
//            params.setMargins(0, ((MainActivity)getActivity()).getActionBarHeight(), 0, 0);

        } else {
            Logger.e("HomeMediaTileGridFragment", "onCreateView else");
            ViewGroup parent = (ViewGroup) Utils.getParentViewCustom(rootView);
            parent.removeView(rootView);
        }
        return rootView;
    }

	// private PlayerBarFragment mPlayerBar;
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Utils.clearCache();

        mDataManager = DataManager.getInstance(getActivity()
                .getApplicationContext());
        applicationConfigurations = mDataManager.getApplicationConfigurations();
        // mPlayerBarFragment = ((MainActivity) getActivity()).getPlayerBar();

        mInputMethodManager = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

//        getActivity().getWindowManager().getDefaultDisplay()
//                .getMetrics(metrics);

        if (HomeActivity.metrics == null)
            HomeActivity.resetMatrix(getActivity());
        else
            metrics= HomeActivity.metrics;

        metrics= HomeActivity.metrics;
        width = metrics.widthPixels;
        dpi = metrics.densityDpi;

        setHasOptionsMenu(true);

        ((MainActivity)getActivity()).lockDrawer();
        ((MainActivity)getActivity()).removeDrawerIconAndPreference();

        Utils.setToolbarColor(((MainActivity) getActivity()));

		Analytics.postCrashlitycsLog(getActivity(), MainSearchFragmentNew.class.getName());
	}

	public void onNewIntent(Intent intent) {
		handleIntent(intent);
	}

	/**
	 * Handling intent data
	 */
	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);

			/**
			 * Use this query to display search results like 1. Getting the data
			 * from SQLite and showing in listview 2. Making webrequest and
			 * displaying the data For now we just display the query only
			 */
			onStartSearchKeyboard(query);
		}
	}

	public Menu mMenu;
	SearchView search;

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if(getActivity()==null)
			return;
		mMenu = menu;
        mMenu.clear();
		if(inflater==null)
			inflater=getActivity().getMenuInflater();

//        ((MainActivity)getActivity()).removeDrawerIconAndPreference();
		inflater.inflate(R.menu.menu_search_actionbar, menu);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

			SearchManager manager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

			search = (SearchView) menu.findItem(R.id.search).getActionView();

			search.setSearchableInfo(manager
					.getSearchableInfo(getActivity().getComponentName()));
			// search.setGravity(Gravity.LEFT);

			MenuItem searchMenuItem = menu.findItem(R.id.search);
			// search.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM |
			// MenuItem.SHOW_AS_ACTION_WITH_TEXT);

			searchMenuItem.expandActionView();
			search.setQueryHint(Utils.getMultilanguageTextLayOut(getActivity(),
					getResources().getString(R.string.search_hint)));

            ((MainActivity)getActivity()).getSupportActionBar().setDisplayUseLogoEnabled(false);

			MenuItemCompat.setOnActionExpandListener(searchMenuItem,
					new OnActionExpandListener() {
						@Override
						public boolean onMenuItemActionCollapse(MenuItem item) {
							if (isNeedToClose && !songCatcherClick) {
//                               finish();
								onBackPressed();
//                                getActivity().getSupportFragmentManager().beginTransaction().remove(MainSearchFragment.this).commit();
								return false;
							} else {
								songCatcherClick = false;
							}
							return true; // Return true to collapse action view
						}

						@Override
						public boolean onMenuItemActionExpand(MenuItem item) {
							// Do something when expanded

							return true; // Return true to expand action view
						}
					});


			// setSearchIcons();

			search.setOnSearchClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					actionbar_title = "";
					if (getActivity() != null)
						((MainActivity) getActivity()).showBackButtonWithTitleWithouLogo(actionbar_title, "");
				}
			});

			search.setOnQueryTextListener(new OnQueryTextListener() {

				@Override
				public boolean onQueryTextChange(String query) {

					if (query == null || query.isEmpty()) {
						if (null != listAdapter)
							listAdapter.swapCursor(null);
						search.setSuggestionsAdapter(null);
					} else {
						onStartSearch(query);
					}
					return true;

				}

				@Override
				public boolean onQueryTextSubmit(String query) {
					onStartSearchKeyboard(query);
					return true;
				}
			});
//            if(!songCatcherClick)
//			    showSearchView();

		}
	}

	public void showSearchView() {
		if (search != null && getActivity()!=null) {
			// search.setIconifiedByDefault(true);
			// showBackButtonWithTitleWithouLogoTittle("", "");
			MenuItem searchMenuItem = mMenu.findItem(R.id.search);
            ((MainActivity)getActivity()).getSupportActionBar().setDisplayUseLogoEnabled(false);
			searchMenuItem.expandActionView();
			// search.setQueryHint(getResources().getText(R.string.search_hint));
			search.setQueryHint(Utils.getMultilanguageTextLayOut(getActivity(),
					getResources().getString(R.string.search_hint)));
			searchMenuItem.setVisible(true);
			search.setIconifiedByDefault(true);
			search.setIconified(false);
			search.clearFocus();

			ViewGroup.LayoutParams layoutParams = search.getLayoutParams();
			if(layoutParams!=null)
				layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
		}
	}

	private void hideSearchView() {
		try {
			if (search != null) {
				// search.setIconifiedByDefault(true);
				MenuItem searchMenuItem = mMenu.findItem(R.id.search);
				searchMenuItem.collapseActionView();
				searchMenuItem.setVisible(false);
				search.setIconifiedByDefault(true);
				search.setIconified(true);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public void displayTitle(final String actionbar_title) {

        try {
            if (actionbar_title.equals(getResources().getString(
                    R.string.main_actionbar_search)))
                ((MainActivity) getActivity()).showBackButtonWithTitle("", "");
            else
                ((MainActivity) getActivity()).showBackButtonWithTitle(actionbar_title, "");
            Utils.setToolbarColor(((MainActivity) getActivity()));
            ((MainActivity) getActivity()).mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity() != null)
                        onBackPressed();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }



	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			if (search != null && search.getQuery().toString().trim() != null
					&& !search.getQuery().toString().trim().equals("")) {
				search.setQuery("", false);
				search.clearFocus();
			} else {
                // to do
				onBackPressed();
			}
			return true;
		} else if (itemId == R.id.search) {
			actionbar_title = "";
			displayTitle(actionbar_title);
			// showBackButtonWithTitle(actionbar_title, "");
			return true;
		} else
			return super.onOptionsItemSelected(item);
	}

	ShowcaseView sv;
	long showcaseViewTime = 0;

	private void openSearchShowCaseView() {

//		try {
//			// search_popular_searches_search_category_all_config
//			rootView.postDelayed(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						Bundle data = getArguments();//getActivity().getIntent().getExtras();
//						if (mDataManager.getApplicationConfigurations()
//								.isEnabledSongCatcherGuidePage()
//								&& !(data != null && data
//										.containsKey(VideoActivity.ARGUMENT_SEARCH_VIDEO))
//								&& !isPaused) {
//							mDataManager.getApplicationConfigurations()
//									.setIsEnabledSongCatcherGuidePage(false);
//							RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(
//									ViewGroup.LayoutParams.WRAP_CONTENT,
//									ViewGroup.LayoutParams.WRAP_CONTENT);
//							lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//							lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//							int margin = ((Number) (getResources()
//									.getDisplayMetrics().density * 12))
//									.intValue();
//							lps.setMargins(2 * margin, margin, margin,
//									mDataManager.getShowcaseButtonMargin()
//											* margin);
//
//							ViewTarget target = new ViewTarget(R.id.iv_mice,
//									getActivity());
//							sv = new ShowcaseView.Builder(getActivity(), false)
//									.setTarget(target)
//									.setContentTitle(
//											R.string.showcase_song_catacher_title)
//									.setContentText(
//											R.string.showcase_song_catacher_message)
//									.setStyle(R.style.CustomShowcaseTheme2)
//									.hideOnTouchOutside().build();
//							sv.setBlockShowCaseTouches(true);
//							sv.setButtonPosition(lps);
//							showcaseViewTime = System.currentTimeMillis();
//						}
//					} catch (Exception e) {
//						Logger.printStackTrace(e);
//					} catch (Error e) {
//						Logger.printStackTrace(e);
//					}
//				}
//			}, 300);
//		} catch (Exception e) {
//			Logger.printStackTrace(e);
//		}
		mDataManager.getApplicationConfigurations()
						.setIsEnabledSongCatcherGuidePage(false);
	}

	private void hideShowcaseView() {
//		if (sv != null && sv.isShowing()) {
//			sv.hide();
//			if (System.currentTimeMillis() - showcaseViewTime < 2000) {
//				mDataManager.getApplicationConfigurations()
//						.setIsEnabledSongCatcherGuidePage(true);
//			}
//		}
	}

	boolean isNeedToClose = true;

	@Override
	public boolean onBackPressed() {
		try {
			if (((MainActivity)getActivity()).mPlayerBarFragment != null
					&& ((MainActivity)getActivity()).mPlayerBarFragment.isContentOpened()) {
				// Minimize player
				if (!((MainActivity)getActivity()).mPlayerBarFragment.removeAllFragments())
                    ((MainActivity)getActivity()).mPlayerBarFragment.closeContent();
                return true;
			} else if (((MainActivity)getActivity()).mPlayerBarFragment != null
					&& !((MainActivity)getActivity()).mPlayerBarFragment.collapsedPanel1()) {
//				int count = getActivity().getSupportFragmentManager()
//						.getBackStackEntryCount();
//				if (count > 0) {
					results = false;

					getActivity().getSupportFragmentManager().popBackStack();
					return true;
//				} else {
//                    getActivity().getSupportFragmentManager().popBackStack();
//                    return true;
//                }
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
        return false;
	}

    @Override
    public void setTitle(boolean needOnlyHight,boolean needToSetTitle) {
//        ColorDrawable cd = new ColorDrawable(getResources().getColor(
//                R.color.myPrimaryColor));
//        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
//        actionBar.setBackgroundDrawable(cd);
try{
	actionbar_title = getResources().getString(
			R.string.main_actionbar_search);
	// showBackButtonWithTitle(actionbar_title, "");
	displayTitle(actionbar_title);
	search = (SearchView) mMenu.findItem(R.id.search)
			.getActionView();
	search.setVisibility(View.VISIBLE);
	showSearchView();

}catch (Exception e){

}
		isNeedToClose = true;

    }

    private void initializeUserControls(final View rootView) {

		rootView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return false;
			}
		});



		tvSearchCategory = (TextView) rootView
				.findViewById(R.id.search_popular_searches_category);
		tvSearchCategory.setText(getResources().getString(
				R.string.search_popular_searches_search_category_all_config));

		ListView listPopulerKeywords = (ListView) rootView
				.findViewById(R.id.listview_search_keywords);

		RelativeLayout headerView = (RelativeLayout) ((LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.search_header, null, false);
		if (applicationConfigurations.getUserSelectedLanguage() != Constants.LANGUAGE_ENGLISH) {
			Utils.traverseChild(headerView, getActivity());
		}
//		((LanguageTextView) headerView
//				.findViewById(R.id.tv_catcher_title_upper)).setText(Utils
//				.getMultilanguageText(getActivity(),
//						getString(R.string.songcatcher_title_upper)));
//
//		headerView.findViewById(R.id.tv_catcher_title_upper)
//				.setOnClickListener(this);
//		headerView.findViewById(R.id.iv_mice).setOnClickListener(this);
//		headerView.findViewById(R.id.iv_help_songcatcher).setOnClickListener(
//				this);
		listPopulerKeywords.addHeaderView(headerView);
		listPopulerKeywords.setAdapter(null);



	}

	@Override
	public void onStart() {
		try {
			super.onStart();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

		try {
			if (!mHasLoaded) {
				String timestamp_cache = mDataManager
						.getApplicationConfigurations()
						.getSearchPopularTimeStamp();
				mDataManager.getSearchPopularSerches(getActivity(), this,
						timestamp_cache);
			} else {
				Logger.e(TAG, "START POPULATE HERE");
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

		// mDataManager.getLeftMenu(this, this, null);

		isFirstVisitToPage = applicationConfigurations
				.isFirstVisitToSearchPage();
		if (isFirstVisitToPage) {
			isFirstVisitToPage = false;
			applicationConfigurations.setIsFirstVisitToSearchPage(false);

		} else if (applicationConfigurations.getHintsState()) {
			if (!applicationConfigurations.isSearchFilterShownInThisSession()) {
				applicationConfigurations
						.setIsSearchFilterShownInThisSession(true);

			} else {
			}
		}
		try {
			Analytics.startSession(getActivity(), this);
			Analytics.onPageView();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		if (actionbar_title.equals(""))
			actionbar_title = getResources().getString(
					R.string.main_actionbar_search);
		// showBackButtonWithTitle(actionbar_title, "");
		displayTitle(actionbar_title);
		int count = getActivity().getSupportFragmentManager().getBackStackEntryCount();
		if (count == 0)
			showSearchView();
	}

	@Override
	public void onResume() {
		super.onResume();
		isPaused = false;
		try {

			Bundle data = getArguments(); //getActivity().getIntent().getExtras();
			if (data != null && data.getBoolean("song_catcher", false)) {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        rootView.findViewById(R.id.tv_catcher_title_upper)
//                                .performClick();
//                    }
//                },200);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		if (sv != null)
			openSearchShowCaseView();

        displayTitle(actionbar_title);

	}

	private boolean isPaused = false;

	@Override
	public void onPause() {
		mDataManager.cancelGetSearchAutoSuggest();
		hideShowcaseView();
		super.onPause();
		isPaused = true;
	}

	@Override
	public void onStop() {
		super.onStop();
//		mDataManager.cancelGetSearchAutoSuggest();

		Analytics.onEndSession(getActivity());
	}

	private boolean results;

	// ======================================================
	// Operation Methods
	// ======================================================

	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.SEARCH_POPULAR_KEYWORDS) {
			// showLoadingDialog(R.string.application_dialog_loading_content);
			mHasLoaded = true;

		} else if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
			showLoadingDialog(R.string.application_dialog_loading_content);
		}
	}

	HashMap<String, Map<String, Object>> responseCache = new HashMap<String, Map<String, Object>>();

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			if (operationId == OperationDefinition.Hungama.OperationId.SEARCH_POPULAR_KEYWORDS) {

				// gets the given media item to check it's type to retrieve the
				// correct
				// details implementation
				List<String> list = (List<String>) responseObjects
						.get(SearchPopularKeywordOperation.RESULT_KEY_LIST_KEYWORDS);

				populateKeywords(list);

				Bundle data = getArguments();//getActivity().getIntent().getExtras();
				if (data != null
						&& data.containsKey(VideoActivity.ARGUMENT_SEARCH_VIDEO)) {
					openSearchVideo(data
							.getString(VideoActivity.ARGUMENT_SEARCH_VIDEO));
				}

				hideLoadingDialog();

			} else if (operationId == OperationDefinition.Hungama.OperationId.SEARCH_AUTO_SUGGEST) {

				List<String> suggestions = (List<String>) responseObjects
						.get(SearchAutoSuggestOperation.RESULT_KEY_LIST_SUGGESTED_KEYWORDS);
				String queryProcessed = (String) responseObjects
						.get(SearchAutoSuggestOperation.QUERY_STRING);

				if (queryProcessed.equals(currentQuery)) {
					if (!responseCache.containsKey(queryProcessed))
						responseCache.put(queryProcessed, responseObjects);

					if (suggestions.size() > MAXIMUM_SUGGESTIONS_TO_PRESENT) {
						suggestions = suggestions.subList(0,
								MAXIMUM_SUGGESTIONS_TO_PRESENT);
					}
					populateAutoSuggestedKeywords(suggestions);
				}

			} else if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
				try {
					MediaItem mediaItem = (MediaItem) responseObjects
							.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_ITEM);
					if (mediaItem.getMediaType() == MediaType.ALBUM
							|| mediaItem.getMediaType() == MediaType.PLAYLIST) {

						MediaSetDetails setDetails = (MediaSetDetails) responseObjects
								.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);
						PlayerOption playerOptions = (PlayerOption) responseObjects
								.get(MediaDetailsOperation.RESPONSE_KEY_PLAYER_OPTION);

						List<Track> tracks = setDetails.getTracks();
						if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
							for (Track track : tracks) {
								track.setTag(mediaItem);
							}
						} else if (mediaItem.getMediaType() == MediaType.ALBUM) {
							for (Track track : tracks) {
								track.setAlbumId(setDetails.getContentId());
							}
						}
						if (playerOptions == PlayerOption.OPTION_PLAY_NOW) {
                            ((MainActivity)getActivity()).mPlayerBarFragment.playNow(tracks, null, null);
						} else if (playerOptions == PlayerOption.OPTION_PLAY_NEXT) {
                            ((MainActivity)getActivity()).mPlayerBarFragment.playNext(tracks);
						} else if (playerOptions == PlayerOption.OPTION_ADD_TO_QUEUE) {
                            ((MainActivity)getActivity()).mPlayerBarFragment.addToQueue(tracks, null, null);
						} else if (playerOptions == PlayerOption.OPTION_SAVE_OFFLINE) {
							if (mediaItem.getMediaType() == MediaType.ALBUM) {
								for (Track track : tracks) {
									track.setTag(mediaItem);
								}
							}
							CacheManager.saveAllTracksOfflineAction(
									getActivity(), tracks);
						}
					}
				} catch (Exception e) {
					Logger.e(getClass().getName() + ":679", e.toString());
				}

				hideLoadingDialog();
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		if (operationId == OperationDefinition.Hungama.OperationId.SEARCH_POPULAR_KEYWORDS) {
			Logger.i(TAG, "Failed loading media details");
			hideLoadingDialog();
			try {
				((MainActivity) getActivity())
						.internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
							@Override
							public void onRetryButtonClicked() {
								mHasLoaded = false;
								onStart();
							}
						});
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		} else if (operationId == OperationDefinition.Hungama.OperationId.SEARCH_AUTO_SUGGEST) {
			Logger.i(TAG, "Failed loading auto suggest keywords");
			populateAutoSuggestedKeywords(new ArrayList<String>());
		} else if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
			hideLoadingDialog();
			try {
				((MainActivity) getActivity())
						.internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
							@Override
							public void onRetryButtonClicked() {
								if (tempMediaItem != null)
									onPlayNowSelected(tempMediaItem);
							}
						});
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}
	}

	// ======================================================
	// Helper Methods
	// ======================================================

	private void openSearchVideo(String videoQuery) {
		onStartSearchKeyboard(videoQuery);
	}

	// ======================================================
	// Populate Methods
	// ======================================================

	private static final String COLUMN_ID = "_id";
	private static final String COLUMN_TERM = "term";
	private static final String DEFAULT = "default";

	public void populateAutoSuggestedKeywords(List<String> mList) {

		try {
			if(getActivity()==null)
				return;

			if (mList != null)
				Logger.s("populateAutoSuggestedKeywords >>>>>>>>>>>> "
						+ mList.size());
			// new search
			SearchManager manager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

			final SearchView search = (SearchView) mMenu.findItem(R.id.search)
					.getActionView();

			search.setSearchableInfo(manager.getSearchableInfo(getActivity().getComponentName()));

			final String[] columns = new String[] { COLUMN_ID, COLUMN_TERM };
			final Object[] object = new Object[] { 0, DEFAULT };

			final MatrixCursor matrixCursor = new MatrixCursor(columns);

			for (int i = 0; i < mList.size(); i++) {

				object[0] = i;
				object[1] = mList.get(i);

				matrixCursor.addRow(object);
			}

			if (search != null && !search.getQuery().toString().trim().equals("")) {
				boolean firstserch = (listAdapter == null);

				listAdapter = new ExampleAdapter(getActivity(), matrixCursor, mList);

				search.setSuggestionsAdapter(listAdapter);

				listAdapter.notifyDataSetChanged();

				if (firstserch)
					search.setQuery(search.getQuery(), false);
			} else {
				if (null != listAdapter) {
					listAdapter.swapCursor(null);
				}
				search.setSuggestionsAdapter(null);
			}
		}catch (Exception e){
			e.printStackTrace();
		}

	}

	ExampleAdapter listAdapter;

	public void populateKeywords(final List<String> keywords) {
		if (keywords != null) {
			if (getActivity() != null) {

				ListView listPopulerKeywords = (ListView) rootView
						.findViewById(R.id.listview_search_keywords);

				listPopulerKeywords.setAdapter(new PopularKeywordsAdapter(
						keywords));
				listPopulerKeywords
						.setOnItemClickListener(new AdapterView.OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> arg0,
									View arg1, int arg2, long arg3) {
								if (arg2 == 0) {
									return;
								}
								arg2 = arg2 - 1;

								// gets the search query from the button.
								String query = keywords.get(arg2);// ((Button)
																	// view).getText().toString();
								String type;
								if (tvSearchCategory.getText().toString()
										.equalsIgnoreCase("All:")) {
									type = "";
								} else {
									type = tvSearchCategory.getText()
											.toString().replace("s:", "");
								}
								mSearchActionSelected = FlurryConstants.FlurrySearch.SearchesUsingPopularKeywords
										.toString();

								openSearchResults(query, type);
							}
						});
			}
		}

	}

	private class PopularKeywordsAdapter extends BaseAdapter {
		private List<String> keywords;

		public PopularKeywordsAdapter(List<String> keywords) {
			this.keywords = keywords;
		}

		@Override
		public int getCount() {
			return keywords.size();
		}

		@Override
		public String getItem(int arg0) {
			return keywords.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			if (arg1 == null) {
				arg1 = LayoutInflater.from(getActivity()).inflate(
						R.layout.list_item_search_popular_keyword, null);
			}
			((TextView) arg1.findViewById(R.id.name)).setText(keywords
					.get(arg0));
			return arg1;
		}
	}

	public void openSearchResults(String query, String type) {
		try {
			ApsalarEvent.postEvent(getActivity(), ApsalarEvent.SEARCH_PERFORMED);
			isNeedToClose = false;

			results = true;
			FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
			MainSearchResultsFragment fragment = (MainSearchResultsFragment) fragmentManager
					.findFragmentByTag(MainSearchResultsFragment.TAG);

			actionbar_title = query;

			hideSearchView();
			displayTitle(query);

			try {

				final SearchView search = (SearchView) mMenu.findItem(
						R.id.search).getActionView();
				search.setQuery("", false);
				search.clearFocus();
				search.setVisibility(View.GONE);
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println(ex);
			}

			try {
				if (getActivity().getCurrentFocus() != null) {
					InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
					inputMethodManager.hideSoftInputFromWindow(
                            getActivity().getCurrentFocus().getWindowToken(), 0);
				}
			} catch (Exception e) {
			}

			if (fragment != null) {
				// the fragment has been already initialized and shown on the
				// screen.
				// fragment.searchForQueury(query, type);
			} else {
				rootView.findViewById(R.id.linearlayout_search_popular_searches)
						.setPadding(0, 0, 0, 0);
				// rootView.findViewById(R.id.llSearchAdHolder).setVisibility(
				// View.GONE);
				// the are no results presented on the screen, creates them.
				Bundle arguments = new Bundle();
				arguments.putString(
						MainSearchResultsFragment.FRAGMENT_ARGUMENT_QUERY,
						query);
				arguments.putString(
						MainSearchResultsFragment.FRAGMENT_ARGUMENT_TYPE, type);
				arguments
						.putString(
								MainSearchResultsFragment.FLURRY_SEARCH_ACTION_SELECTED,
								mSearchActionSelected);
//                ((MainActivity)getActivity()).removeDrawerIconAndPreference();
				fragment = new MainSearchResultsFragment();
				fragment.setArguments(arguments);
				fragment.setOnSearchResultsOptionSelectedListener(this);
                fragment.setSearchResultsFragment(this);

				FragmentTransaction fragmentTransaction = fragmentManager
						.beginTransaction();
				fragmentTransaction.setCustomAnimations(
						R.anim.slide_and_show_bottom_enter,
						R.anim.slide_and_show_bottom_exit);
				fragmentTransaction.add(R.id.main_search_results_container,
						fragment, MainSearchResultsFragment.TAG);
//				fragmentTransaction.disallowAddToBackStack()

				fragmentTransaction.addToBackStack(MainSearchResultsFragment.TAG);
				if(Constants.IS_COMMITALLOWSTATE)
					fragmentTransaction.commitAllowingStateLoss();
				else
					fragmentTransaction.commit();

			}
		} catch (Exception e) {
            e.printStackTrace();
			Logger.e(getClass().getName() + ":916", e.toString());
		}

	}

	String currentQuery = "";

	// @Override
	public void onStartSearch(String query) {
		// cancels any running progress.
		currentQuery = query;

		if (responseCache != null && responseCache.containsKey(query)) {
			onSuccess(
					OperationDefinition.Hungama.OperationId.SEARCH_AUTO_SUGGEST,
					responseCache.get(query));
			return;
		}

		mDataManager.cancelGetSearchAutoSuggest();
		// Querying like a bous!
		mDataManager.getSearchAutoSuggest(query,
				String.valueOf(query.length()), this);
	}

	// @Override
	public void onStartSearchKeyboard(String query) {

		// cancels any existing work.
		// mDataManager.cancelGetSearchAutoSuggest();

		mInputMethodManager
				.hideSoftInputFromWindow(
						rootView.findViewById(
								R.id.linearlayout_search_popular_searches)
								.getWindowToken(), 0);

		String type;
		if (tvSearchCategory.getText().toString().equalsIgnoreCase("All:")) {
			type = "";
		} else {
			type = tvSearchCategory.getText().toString().replace("s:", "");
		}

		mSearchActionSelected = FlurryConstants.FlurrySearch.SearchesByTypingInBox
				.toString();

		openSearchResults(query, type);
	}

	// ======================================================
	// Adapters
	// ======================================================

	private static class ViewHolder {
		TextView suggestedKeyword;
	}

	public class ExampleAdapter extends CursorAdapter {

		View convertView = null;

		public ExampleAdapter(Context context, Cursor cursor, List<String> list) {

			super(context, cursor, false);

			suggestedKeywords = list;
			try {
				mInflater = (LayoutInflater) getActivity()
						.getApplicationContext().getSystemService(
								Context.LAYOUT_INFLATER_SERVICE);
			} catch (Exception e) {
				Logger.e(getClass().getName() + ":1002", e.toString());
			}

		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			// Show list item data from cursor
			// text.setText(items.get(cursor.getPosition()));
			TextView suggestedKeyword = (TextView) convertView
					.findViewById(R.id.search_auto_suggest_name);

			// populate the view from the keywords's list.
			String keyword = suggestedKeywords.get(cursor.getPosition());
			// stores the object in the view.
			convertView.setTag(R.id.view_tag_object, keyword);

			suggestedKeyword.setText(keyword);
			suggestedKeyword.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {

					final String keyword = (String) view
							.getTag(R.id.view_tag_object);
					// Set the keyword in the search bar
					onStartSearchKeyboard(keyword);

					// mSearchActionSelected =
					// FlurryConstants.FlurrySearch.SearchesUsingAutoComplete.toString();
					// //xtpl
				}
			});


		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {

			ViewHolder viewHolder;

			// create view if not exist.
			// if (convertView == null) {
			convertView = mInflater.inflate(
					R.layout.list_item_search_auto_suggest_line, parent, false);

			viewHolder = new ViewHolder();
			viewHolder.suggestedKeyword = (TextView) convertView
					.findViewById(R.id.search_auto_suggest_name);
			convertView.setTag(R.id.view_tag_view_holder, viewHolder);
			// } else {
			// viewHolder = (ViewHolder) convertView
			// .getTag(R.id.view_tag_view_holder);
			// }

			return convertView;

		}

		private List<String> suggestedKeywords;
		private LayoutInflater mInflater;

	}

	private MediaItem tempMediaItem;

	@Override
	public void onPlayNowSelected(MediaItem mediaItem) {
		tempMediaItem = mediaItem;
		try {
			if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
				if (mediaItem.getMediaType() == MediaType.TRACK) {
					Track track = new Track(mediaItem.getId(),
							mediaItem.getTitle(), mediaItem.getAlbumName(),
							mediaItem.getArtistName(), mediaItem.getImageUrl(),
							mediaItem.getBigImageUrl(), mediaItem.getImages(),
							mediaItem.getAlbumId());
					List<Track> tracks = new ArrayList<Track>();
					tracks.add(track);

                    ((MainActivity)getActivity()).mPlayerBarFragment.playNow(tracks, null, null);

				} else {
					mDataManager.getMediaDetails(mediaItem,
							PlayerOption.OPTION_PLAY_NOW,
                            MainSearchFragmentNew.this);
				}
			} else if (mediaItem.getMediaContentType() == MediaContentType.RADIO) {

				showDetailsOfRadioHelper(mediaItem,
						MediaCategoryType.TOP_ARTISTS_RADIO);
			}
		} catch (Exception e) {
			Logger.e(getClass().getName() + ":1076", e.toString());
		}
	}

	private void showDetailsOfRadioHelper(MediaItem mediaItem,
			MediaCategoryType mediaCategoryType) {
		FragmentManager mFragmentManager = ((MainActivity) getActivity())
				.getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();

		RadioDetailsFragment radioDetailsFragment = new RadioDetailsFragment();
		// sets it's arguments.
		Bundle data = new Bundle();
		data.putSerializable(RadioDetailsFragment.EXTRA_MEDIA_ITEM,
				(Serializable) mediaItem);
		data.putSerializable(RadioDetailsFragment.EXTRA_CATEGORY_TYPE,
				(Serializable) mediaCategoryType);
		data.putBoolean(RadioDetailsFragment.EXTRA_DO_SHOW_TITLE_BAR, true);
		data.putBoolean(RadioDetailsFragment.EXTRA_AUTO_PLAY, true);
		data.putBoolean(RadioDetailsFragment.IS_FOR_PLAYER_BAR, false);

		radioDetailsFragment.setArguments(data);
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit, R.anim.slide_right_enter,
				R.anim.slide_right_exit);

		try {
			/*
			 * IMPORTANT: We use the "root frame" defined in "root_fragment.xml"
			 * as the reference to replace fragment
			 */
			fragmentTransaction.add(R.id.main_navigation_fragmant_container,
					radioDetailsFragment);
			/*
			 * IMPORTANT: The following lines allow us to add the fragment to
			 * the stack and return to it later, by pressing back
			 */
			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} catch (Exception e) {
		}
		// }
	}

	@Override
	public void onAddToQueueSelected(MediaItem mediaItem) {
		try {
			if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
				if (mediaItem.getMediaType() == MediaType.TRACK) {
					Track track = new Track(mediaItem.getId(),
							mediaItem.getTitle(), mediaItem.getAlbumName(),
							mediaItem.getArtistName(), mediaItem.getImageUrl(),
							mediaItem.getBigImageUrl(), mediaItem.getImages(),
							mediaItem.getAlbumId());
					List<Track> tracks = new ArrayList<Track>();
					tracks.add(track);

                    ((MainActivity)getActivity()).mPlayerBarFragment.addToQueue(tracks, null, null);

				} else {
					mDataManager.getMediaDetails(mediaItem,
							PlayerOption.OPTION_ADD_TO_QUEUE,
                            MainSearchFragmentNew.this);
				}

			} else if (mediaItem.getMediaContentType() == MediaContentType.RADIO) {

				showDetailsOfRadioHelper(mediaItem,
						MediaCategoryType.TOP_ARTISTS_RADIO);
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void onShowDetails(MediaItem mediaItem, boolean playnow) {
		Intent intent;
		if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
            ((MainActivity) getActivity()).isSkipResume=true;
			intent = new Intent(getActivity(), VideoActivity.class);
			intent.putExtra(VideoActivity.EXTRA_MEDIA_ITEM_VIDEO,
					(Serializable) mediaItem);
			startActivity(intent);
		} else if (mediaItem.getMediaContentType() == MediaContentType.RADIO) {
			showDetailsOfRadioHelper(mediaItem,
					MediaCategoryType.TOP_ARTISTS_RADIO);
		} else {

//            isNeedToClose = false;
//
//            results = true;
//            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//            MainSearchResultsFragment fragment = (MainSearchResultsFragment) fragmentManager
//                    .findFragmentByTag(MainSearchResultsFragment.TAG);
//
//            hideSearchView();
//
//            try {
//                final SearchView search = (SearchView) mMenu.findItem(
//                        R.id.search).getActionView();
//                search.setQuery("", false);
//                search.clearFocus();
//                search.setVisibility(View.GONE);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                System.out.println(ex);
//            }
//
//            try {
//                if (getActivity().getCurrentFocus() != null) {
//                    InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
//                    inputMethodManager.hideSoftInputFromWindow(
//                            getActivity().getCurrentFocus().getWindowToken(), 0);
//                }
//            } catch (Exception e) {
//            }
//            rootView.findViewById(R.id.linearlayout_search_popular_searches)
//                    .setPadding(0, 0, 0, 0);

//			Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag("MediaDetailsActivity");
//			if(fragment!=null){
//				((MainActivity) getActivity()).isSkipResume=true;
//				intent = new Intent(getActivity(), MediaDetailsActivity.class);
//				intent.putExtra(MediaDetailsActivity.EXTRA_MEDIA_ITEM,
//						(Serializable) mediaItem);
//				intent.putExtra(MediaDetailsActivity.FLURRY_SOURCE_SECTION,
//						FlurryConstants.FlurrySourceSection.Search.toString());
//				startActivity(intent);
//			}else {

//			MyCollectionActivity mTilesFragment = new MyCollectionActivity();
//
//			FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();
//			FragmentTransaction fragmentTransaction = mFragmentManager
//					.beginTransaction();
//			fragmentTransaction.setCustomAnimations(R.anim.slide_and_show_bottom_enter,
//					R.anim.slide_and_show_bottom_exit);
//			fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
//					mTilesFragment, "MyCollectionActivity");
//			fragmentTransaction.addToBackStack("MyCollectionActivity");
//			fragmentTransaction.commitAllowingStateLoss();

			if(getActivity()==null)
				return;

				FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();
				FragmentTransaction fragmentTransaction = mFragmentManager
						.beginTransaction();

//			Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag("MediaDetailsActivity");
//			boolean fragmentPopped = mFragmentManager.popBackStackImmediate("MediaDetailsActivity", 0);
//			fragmentTransaction.remove(fragment);

				MediaDetailsActivityNew mediaDetailsFragment = new MediaDetailsActivityNew();

				Bundle bundle = new Bundle();
				bundle.putSerializable(MediaDetailsActivity.EXTRA_MEDIA_ITEM,
						(Serializable) mediaItem);
				bundle.putString(MediaDetailsActivity.FLURRY_SOURCE_SECTION,
						FlurryConstants.FlurrySourceSection.Search.toString());

				mediaDetailsFragment.setArguments(bundle);

				fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
						mediaDetailsFragment, "MediaDetailsActivitySearch111");
				fragmentTransaction.addToBackStack("MediaDetailsActivitySearch111");
				if(Constants.IS_COMMITALLOWSTATE)
					fragmentTransaction.commitAllowingStateLoss();
				else
					fragmentTransaction.commit();
//			}
//            ((MainActivity) getActivity()).isSkipResume=true;
//			intent = new Intent(getActivity(), MediaDetailsActivity.class);
//			intent.putExtra(MediaDetailsActivity.EXTRA_MEDIA_ITEM,
//					(Serializable) mediaItem);
//			intent.putExtra(MediaDetailsActivity.FLURRY_SOURCE_SECTION,
//					FlurryConstants.FlurrySourceSection.Search.toString());
//			startActivity(intent);
		}

	}

	@Override
	public void onSaveOffline(MediaItem mediaItem) {

		Logger.i(TAG, "Save Offline: " + mediaItem.getId());
		if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				Track track = new Track(mediaItem.getId(),
						mediaItem.getTitle(), mediaItem.getAlbumName(),
						mediaItem.getArtistName(), mediaItem.getImageUrl(),
						mediaItem.getBigImageUrl(), mediaItem.getImages(),
						mediaItem.getAlbumId());
				CacheManager.saveOfflineAction(getActivity(), mediaItem, track);
				Utils.saveOfflineFlurryEvent(getActivity(),
						FlurryConstants.FlurryCaching.LongPressMenuSong
								.toString(), mediaItem);
			} else if (mediaItem.getMediaType() == MediaType.ALBUM
					|| mediaItem.getMediaType() == MediaType.PLAYLIST) {
				if (MediaCachingTaskNew.isEnabled)
					mDataManager.getMediaDetails(mediaItem,
							PlayerOption.OPTION_SAVE_OFFLINE, this);
				else
					CacheManager.saveOfflineAction(getActivity(), mediaItem,
							null);
				if (mediaItem.getMediaType() == MediaType.ALBUM)
					Utils.saveOfflineFlurryEvent(getActivity(),
							FlurryConstants.FlurryCaching.LongPressMenuAlbum
									.toString(), mediaItem);
				else
					Utils.saveOfflineFlurryEvent(getActivity(),
							FlurryConstants.FlurryCaching.LongPressMenuPlaylist
									.toString(), mediaItem);
			}
		} else {
			CacheManager.saveOfflineAction(getActivity(), mediaItem, null);
			Utils.saveOfflineFlurryEvent(
					getActivity(),
					FlurryConstants.FlurryCaching.LongPressMenuVideo.toString(),
					mediaItem);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hungama.myplay.activity.ui.fragments.MainSearchResultsFragment.
	 * OnSearchResultsOptionSelectedListener#onFinishSongCatcher(boolean)
	 */
	@Override
	public void onFinishSongCatcher(boolean isFinishSongCatcher) {
		if (isFinishSongCatcher) {
			String timestamp_cache = mDataManager
					.getApplicationConfigurations().getSearchPopularTimeStamp();
			mDataManager.getSearchPopularSerches(getActivity(), this, timestamp_cache);
		}
	}

	boolean songCatcherClick = false;

	private void displaySongCatcherFragment() {
//        Bundle data = getArguments();//getActivity().getIntent().getExtras();
//        if (data != null && data.getBoolean("song_catcher", false)) {
//            Handler handle=new Handler();
//                    handle.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            sonfcatcherpush();
//                        }
//                    },200);
//        }else{
//            sonfcatcherpush();
//        }
	}

//    private void sonfcatcherpush(){
//        if (getActivity().isFinishing() != true) {
//            actionbar_title = getResources().getString(
//                    R.string.main_actionbar_search);
//            songCatcherClick = true;
//            FragmentManager mragmentManager = getActivity().getSupportFragmentManager();
//            FragmentTransaction fragmentTransaction = mragmentManager
//                    .beginTransaction();
//            fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
//                    R.anim.slide_left_exit, R.anim.slide_right_enter,
//                    R.anim.slide_right_exit);
//
//            SongCatcherFragment songcatcherFragment = new SongCatcherFragment();
//            Bundle data = getArguments();//getActivity().getIntent().getExtras();
//            if (data != null && data.getBoolean("song_catcher", false)) {
//                data.remove("song_catcher");
//                Bundle dataSC = new Bundle();
//                dataSC.putBoolean("is_from_push", true);
//                songcatcherFragment.setArguments(dataSC);
//            }
//            songcatcherFragment.setSongCatherFlag(true);
//            songcatcherFragment.setOnSearchResultsOptionSelectedListener(this);
//            fragmentTransaction.replace(R.id.main_search_results_container,
//					songcatcherFragment, SongCatcherFragment.TAG);
//            fragmentTransaction.addToBackStack(SongCatcherFragment.TAG);
//			if(Constants.IS_COMMITALLOWSTATE)
//				fragmentTransaction.commitAllowingStateLoss();
//			else
//				fragmentTransaction.commit();
//
//            hideSearchView();
//            displayTitle(actionbar_title);
//        }
//
//    }


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {

//		switch (v.getId()) {
//		case R.id.tv_catcher_title_upper:
//		case R.id.iv_mice:
////			displaySongCatcherFragment();
//			break;
//		case R.id.iv_help_songcatcher:
//			showInitializationDialog();
//			break;
//		default:
//			break;
//		}
	}

	private Dialog downloadDialog;

	public void showInitializationDialog() {
		// set up custom dialog
		downloadDialog = new Dialog(getActivity());
		downloadDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		downloadDialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(Color.TRANSPARENT));

		downloadDialog.setContentView(R.layout.dialog_songcatcher_initial);
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(getActivity());
		View root = downloadDialog.getWindow().getDecorView();
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(root, getActivity());
		}
		((LanguageTextView) root.findViewById(R.id.help_text_line1))
				.setText(Html.fromHtml("&#8226;"
						+ Utils.getMultilanguageText(getActivity(),
								getString(R.string.songcatcher_help_text_line1))));
		((LanguageTextView) root.findViewById(R.id.help_text_line2))
				.setText(Html.fromHtml("&#8226;"
						+ Utils.getMultilanguageText(getActivity(),
								getString(R.string.songcatcher_help_text_line2))));
		root.findViewById(R.id.bCloseVideoAd).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {

						downloadDialog.dismiss();
					}
				});

		DisplayMetrics displaymetrics = new DisplayMetrics();
		downloadDialog.getWindow().getWindowManager().getDefaultDisplay()
				.getMetrics(displaymetrics);
		int width = (int) (displaymetrics.widthPixels);

		WindowManager.LayoutParams params = downloadDialog.getWindow()
				.getAttributes();
		params.width = width;
		downloadDialog.getWindow().setAttributes(params);

		downloadDialog.show();
	}

	@Override
	public void onShowDetails(MediaItem mediaItem, List<MediaItem> list,
			boolean addToQueue) {
        ((MainActivity) getActivity()).isSkipResume=true;
		Intent intent = new Intent(getActivity(), VideoActivity.class);
		intent.putExtra(VideoActivity.EXTRA_MEDIA_ITEM_VIDEO,
				(Serializable) mediaItem);
		intent.putExtra(VideoActivity.EXTRA_MEDIA_LIST_VIDEO,
				(Serializable) list);
		intent.putExtra(VideoActivity.EXTRA_MEDIA_POS_VIDEO,
				list.indexOf(mediaItem));

		startActivity(intent);

	}

	@Override
	public void onDestroy() {
		try {
			if (rootView != null) {
				isNeedToClose = false;
				hideSearchView();
				try {
					int version = Integer.parseInt(""
							+ android.os.Build.VERSION.SDK_INT);
					Utils.unbindDrawables(rootView, version);
					Utils.destroyFragment();
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}

		super.onDestroy();
	}
}
