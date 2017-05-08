package com.hungama.myplay.activity.ui.fragments;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.HungamaApplication;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.Comment;
import com.hungama.myplay.activity.data.dao.hungama.CommentsListingResponse;
import com.hungama.myplay.activity.data.dao.hungama.CommentsPostResponse;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.SocialNetwork;
import com.hungama.myplay.activity.data.dao.hungama.UserProfileResponse;
import com.hungama.myplay.activity.gigya.FBFriend;
import com.hungama.myplay.activity.gigya.GigyaManager;
import com.hungama.myplay.activity.gigya.GigyaManager.OnGigyaResponseListener;
import com.hungama.myplay.activity.gigya.GoogleFriend;
import com.hungama.myplay.activity.gigya.TwitterLoginFragment;
import com.hungama.myplay.activity.gigya.TwitterLoginFragment.OnTwitterLoginListener;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.CMOperation;
import com.hungama.myplay.activity.operations.catchmedia.PartnerConsumerProxyCreateOperation;
import com.hungama.myplay.activity.operations.hungama.GetUserProfileOperation;
import com.hungama.myplay.activity.operations.hungama.SocialBadgeAlertOperation;
import com.hungama.myplay.activity.operations.hungama.SocialCommentsListingOperation;
import com.hungama.myplay.activity.operations.hungama.SocialCommentsPostOperation;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.player.PlayerServiceBindingManager;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.ProfileActivity;
import com.hungama.myplay.activity.ui.widgets.LanguageButton;
import com.hungama.myplay.activity.ui.widgets.LanguageEditText;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.ApsalarEvent;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommentsFragment extends BackHandledFragment implements OnClickListener,
		CommunicationOperationListener, OnGigyaResponseListener,
		OnTwitterLoginListener,ServiceConnection {

	private static final String TAG = "CommentsFragment";
	public static final String FRAGMENT_COMMENTS = "fragment_comments";
    private PlayerServiceBindingManager.ServiceToken mServiceToken = null;
	private static final String COMMA = ",";
	private static final String FACEBOOK_PROVIDER = "facebook";
	private static final String TWITTER_PROVIDER = "twitter";

	private static final int SUCCESS = 1;
	private static final int NO_COMMENTS_FOUND = 2;

	private static final String VALUE = "value";

	private DataManager mDataManager;
	private GigyaManager mGigyaManager;
	private ApplicationConfigurations mApplicationConfigurations;

	private View rootView;
	private MediaItem mMediaItem;
	private int numOfComments;

	private boolean mIsFacebookLoggedIn = false;
	private boolean mIsTwitterLoggedIn = false;

	private ImageButton facebookImageButton;
	private ImageButton twitterImageButton;
	private LanguageButton postButton;
	private LanguageEditText commentBox;
	private Button commentsNum;
    //private PlayerBarFragment mplayerbar;
	private ListView mCommentsList;
	// private ImageFetcher mImageFetcher = null;
//    String actionbar_title1 = "Comments";
	private TextWatcher mTextWatcher;

	// Data members
	private TwitterLoginFragment mTwitterLoginFragment;

	private Context mContext;
	FragmentManager mFragmentManager;

	private String mFlurrySourceSection;

	private List<Comment> mListComments;
	private CommentsAdapter mCommentsAdapter;

	// ======================================================
	// Fragment lifecycle methods
	// ======================================================

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mGigyaManager = new GigyaManager(getActivity());
		mGigyaManager.setOnGigyaResponseListener(this);
		// showLoadingDialog(R.string.application_dialog_loading_content);

		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();

		// gets the media item from parent.
		Bundle data = getArguments();
		mMediaItem = (MediaItem) data
				.getSerializable(MainActivity.EXTRA_DATA_MEDIA_ITEM);
		mFlurrySourceSection = data
				.getString(MainActivity.FLURRY_SOURCE_SECTION);

		numOfComments = 0;
		Analytics.postCrashlitycsLog(getActivity(), CommentsFragment.class.getName());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_comments, container,
					false);
			if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
				Utils.traverseChild(rootView, getActivity());
			}
		} else {
			ViewGroup parent = (ViewGroup) Utils.getParentViewCustom(rootView);
			parent.removeView(rootView);
		}
        //mplayerbar=((MainActivity)getActivity()).getPlayerBar();
        ((MainActivity)getActivity()). getSupportActionBar().setHomeAsUpIndicator(
                R.drawable.abc_ic_ab_back_mtrl_am_alpha_normal);
        ((MainActivity)getActivity()). getSupportActionBar().setHomeButtonEnabled(true);
        ((MainActivity)getActivity()).mToolbar.setNavigationOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
                //mplayerbar.collapseexpandplayerbar(true);
            }
        });

//        setControl();
		return rootView;
	}

    public void setControl(){
       /* ((MainActivity)getActivity()).mDrawerToggle.setDrawerIndicatorEnabled(false);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity)getActivity()).mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);*/


        ((MainActivity)getActivity()).lockDrawer();
        //((MainActivity)getActivity()).mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        ((MainActivity)getActivity()).removeDrawerIconAndPreference();

    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mContext = getActivity();
		mFragmentManager = getActivity().getSupportFragmentManager();

		// Flurry report: user tapped on comment
		Map<String, String> reportMap = new HashMap<String, String>();
		reportMap.put(
				FlurryConstants.FlurryComments.UserTappedOnComment.toString(),
				Boolean.TRUE.toString());
		reportMap.put(FlurryConstants.FlurryComments.Title.toString(),
				mMediaItem.getTitle());
		reportMap.put(FlurryConstants.FlurryComments.Type.toString(),
				mMediaItem.getMediaType().toString());
		reportMap.put(FlurryConstants.FlurryComments.SourceSection.toString(),
				mFlurrySourceSection);
		reportMap.put(
				FlurryConstants.FlurryComments.IsSocialLoggedIn.toString(),
				mGigyaManager.isSocialNetorkConnected().toString());
		Analytics.logEvent(FlurryConstants.FlurryComments.Comment.toString(),
				reportMap);
//        displayTitle(actionbar_title);
	}

	@Override
	public void onStart() {
		super.onStart();
		if (mTwitterLoginFragment == null) {
			mGigyaManager.socializeGetUserInfo();
			((MainActivity) getActivity())
					.showLoadingDialog(R.string.application_dialog_loading_content);
			Analytics.startSession(getActivity(), this);
			Analytics.onPageView();
			mServiceToken = PlayerServiceBindingManager.bindToService(getActivity(), this);
		} else {
			mTwitterLoginFragment = null;
		}

	}

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mServiceToken = null;
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
		/*
		 * we've establish a connection to the player service. if it plays,
		 * pause it.
		 */
        PlayerService.PlayerSericeBinder binder = (PlayerService.PlayerSericeBinder) service;
        PlayerService playerService = binder.getService();

        // does nothing, just holds the connection to the playing service.
    }

	@Override
	public void onStop() {
		super.onStop();
		Analytics.onEndSession(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
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

	// ======================================================
	// Helper Methods
	// ======================================================

	private void initializeComponents() {
		// get all the comments
		mListComments = new ArrayList<Comment>();
		if (mMediaItem.getMediaContentType() == MediaContentType.VIDEO) {
			mDataManager.getComments(mMediaItem.getId(), MediaType.VIDEO, 1,
					30, this);
		} else {
			mDataManager.getComments(mMediaItem.getId(),
					mMediaItem.getMediaType(), 1, 30, this);
		}

		if (mMediaItem != null) {
			// set Components
			try {
				LanguageButton loginButton = (LanguageButton) rootView
						.findViewById(R.id.login_signup_button_login);
				loginButton.setOnClickListener(this);
				LinearLayout notLoggedInPanel = (LinearLayout) rootView
						.findViewById(R.id.need_to_login_panel);
				notLoggedInPanel.setOnClickListener(this);
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}

			facebookImageButton = (ImageButton) rootView
					.findViewById(R.id.comments_image_facebook);
			twitterImageButton = (ImageButton) rootView
					.findViewById(R.id.comments_image_twitter);
			facebookImageButton.setOnClickListener(this);
			twitterImageButton.setOnClickListener(this);

			postButton = (LanguageButton) rootView
					.findViewById(R.id.post_button);
			postButton.setEnabled(false);
			postButton.setOnClickListener(this);

			// set the number of comments for the item.
			commentsNum = (Button) rootView
					.findViewById(R.id.button_media_details_comment);
			commentsNum.setText(String.valueOf(numOfComments));

			// set the title
			LanguageTextView title = (LanguageTextView) rootView
					.findViewById(R.id.main_title_bar_text);
			title.setText(Utils.getMultilanguageTextLayOut(mContext,
					mMediaItem.getTitle()));

			mCommentsList = (ListView) rootView
					.findViewById(R.id.listview_comments);
			mCommentsList.setOnScrollListener(new ScrollToBottomListener());

			// check if there is a social login (facebook/twitter)
			// mIsFacebookLoggedIn = true; //TODO: ############# need gigya
			// method from Dudu #########################
			// mIsTwitterLoggedIn = false; //TODO: ############# need gigya
			// method from Dudu #########################

			if (!mIsFacebookLoggedIn && !mIsTwitterLoggedIn) {
				// show "Need to login Panel"
				LinearLayout notLoggedInPanel = (LinearLayout) rootView
						.findViewById(R.id.need_to_login_panel);
				notLoggedInPanel.setVisibility(View.VISIBLE);

				// hide "logged in panel"
				LinearLayout LoggedInPanel = (LinearLayout) rootView
						.findViewById(R.id.logged_in_panel);
				LoggedInPanel.setVisibility(View.GONE);

				RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mCommentsList
						.getLayoutParams();
				layoutParams.addRule(RelativeLayout.ABOVE,
						R.id.need_to_login_panel);
				mCommentsList.setLayoutParams(layoutParams);
			} else {
				// hide "Need to login Panel"
				LinearLayout notLoggedInPanel = (LinearLayout) rootView
						.findViewById(R.id.need_to_login_panel);
				notLoggedInPanel.setVisibility(View.GONE);

				// show "logged in panel"
				LinearLayout LoggedInPanel = (LinearLayout) rootView
						.findViewById(R.id.logged_in_panel);
				LoggedInPanel.setVisibility(View.VISIBLE);

				RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mCommentsList
						.getLayoutParams();
				layoutParams
						.addRule(RelativeLayout.ABOVE, R.id.logged_in_panel);
				mCommentsList.setLayoutParams(layoutParams);

				// set EditText key listener for post button
				commentBox = (LanguageEditText) rootView
						.findViewById(R.id.comment_edit_text);
				mTextWatcher = new TextWatcher() {
					private boolean isEnabled = true;

					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {
						if (commentBox.length() > 0) {
							setPostButtonEnabled(true);
						} else {
							setPostButtonEnabled(false);
						}
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {
						// TODO Auto-generated method stub

					}

					@Override
					public void afterTextChanged(Editable s) {
						// TODO Auto-generated method stub

					}
				};
				commentBox.addTextChangedListener(mTextWatcher);
				try {
					// set facebook and twitter
					if (mIsFacebookLoggedIn) {
						// setFacebookImageButtonLoggedIn();
						setFacebookImageButtonSelected();
					} else {
						setFacebookImageButtonNotLoggedIn();
					}

					if (mIsTwitterLoggedIn) {
						// setTwitterImageButtonLoggedIn();
						setTwitterImageButtonSelected();
					} else {
						setTwitterImageButtonNotLoggedIn();
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			}

		}
	}

	public void showSocialLoginDialog() {
		// set up custom dialog
		final Dialog dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// RelativeLayout root = (RelativeLayout) LayoutInflater.from(
		// getActivity()).inflate(R.layout.dialog_social_login, null);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
		dialog.setContentView(R.layout.dialog_social_login);
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(dialog.getWindow().getDecorView(),
					getActivity());
		}

		LanguageTextView title = (LanguageTextView) dialog
				.findViewById(R.id.long_click_custom_dialog_title_text);
		title.setText(Utils.getMultilanguageTextLayOut(mContext, getResources()
				.getString(R.string.comments_login_dialog_title)));

		LanguageButton closeButton = (LanguageButton) dialog
				.findViewById(R.id.long_click_custom_dialog_title_image);
		closeButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.setCancelable(true);
		dialog.show();

		LinearLayout facebookLogin = (LinearLayout) dialog
				.findViewById(R.id.long_click_custom_dialog_play_now_row);
		LinearLayout twitterLogin = (LinearLayout) dialog
				.findViewById(R.id.long_click_custom_dialog_add_to_queue_row);

		facebookLogin.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mGigyaManager.facebookLogin();
				dialog.dismiss();
			}
		});

		twitterLogin.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mGigyaManager.twitterLogin();
				dialog.dismiss();
			}
		});

	}

	/**
	 * set the provider to send in postComment
	 * 
	 * @return provider
	 */
	private String setProvider() {
		String provider = "";
		if (facebookImageButton.isSelected()) {
			provider = FACEBOOK_PROVIDER;
		}
		if (twitterImageButton.isSelected()) {
			if (provider.length() > 0) {
				provider = provider + COMMA + TWITTER_PROVIDER;
			} else {
				provider = provider + TWITTER_PROVIDER;
			}
		}
		return provider;
	}

	/*public void toggleFragmentTitle() {
		if (getActivity() instanceof CommentsActivity) {
			((CommentsActivity) getActivity()).toggleActivityTitle();
		}
	}
*/
	// ======================================================
	// set facebook and twitter buttons
	// ======================================================

	private void setFacebookImageButtonNotLoggedIn() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			facebookImageButton.setBackgroundDrawable(getResources()
					.getDrawable(R.drawable.icon_facebook_unselected));
		} else {
			facebookImageButton.setBackground(getResources().getDrawable(
					R.drawable.icon_facebook_unselected));
		}
		facebookImageButton.setEnabled(true);
	}

	private void setFacebookImageButtonLoggedIn() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			facebookImageButton.setBackgroundDrawable(getResources()
					.getDrawable(R.drawable.icon_invite_facebook));
		} else {
			facebookImageButton.setBackground(getResources().getDrawable(
					R.drawable.icon_invite_facebook));
		}
		facebookImageButton.setEnabled(true);
	}

	private void setFacebookImageButtonSelected() {
		if (getActivity() != null) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				facebookImageButton.setBackgroundDrawable(getResources()
						.getDrawable(R.drawable.icon_facebook_selected));
			} else {
				facebookImageButton.setBackground(getResources().getDrawable(
						R.drawable.icon_facebook_selected));
			}
		}
		facebookImageButton.setSelected(true);
		facebookImageButton.setEnabled(true);
	}

	private void setTwitterImageButtonNotLoggedIn() {
		if (getActivity() != null) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				twitterImageButton.setBackgroundDrawable(getResources()
						.getDrawable(R.drawable.icon_twitter_unselected));
			} else {
				twitterImageButton.setBackground(getResources().getDrawable(
						R.drawable.icon_twitter_unselected));
			}
		}
		twitterImageButton.setEnabled(true);
	}

	private void setTwitterImageButtonLoggedIn() {
		if (getActivity() != null) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				twitterImageButton.setBackgroundDrawable(getResources()
						.getDrawable(R.drawable.icon_invite_twitter));
			} else {
				twitterImageButton.setBackground(getResources().getDrawable(
						R.drawable.icon_invite_twitter));
			}
		}
		twitterImageButton.setEnabled(true);
	}

	private void setTwitterImageButtonSelected() {
		if (getActivity() != null) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				twitterImageButton.setBackgroundDrawable(getResources()
						.getDrawable(R.drawable.icon_twitter_selected));
			} else {
				twitterImageButton.setBackground(getResources().getDrawable(
						R.drawable.icon_twitter_selected));
			}
		}
		twitterImageButton.setSelected(true);
		twitterImageButton.setEnabled(true);
	}

	private void setPostButtonEnabled(boolean isEnabled) {
		if (isEnabled) {
			if (getActivity() != null) {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
					postButton.setBackgroundDrawable(getResources()
							.getDrawable(R.drawable.background_button_blue));
				} else {
					postButton.setBackground(getResources().getDrawable(
							R.drawable.background_button_blue));
				}
			}
			postButton.setEnabled(true);
		} else {
			if (getActivity() != null) {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
					postButton
							.setBackgroundDrawable(getResources().getDrawable(
									R.drawable.background_button_blue_disabled));
				} else {
					postButton.setBackground(getResources().getDrawable(
							R.drawable.background_button_blue_disabled));
				}
			}
			postButton.setEnabled(false);
		}

	}

	// ======================================================
	// Communication Operation Methods
	// ======================================================

	@Override
	public void onStart(int operationId) {
		switch (operationId) {
		case OperationDefinition.Hungama.OperationId.SOCIAL_COMMENT_GET:
		case OperationDefinition.Hungama.OperationId.SOCIAL_COMMENT_POST:

			if ((MainActivity) getActivity() != null)
				((MainActivity) getActivity())
						.showLoadingDialog(R.string.application_dialog_loading_content);
			// showLoadingDialog(R.string.application_dialog_loading_content);
			break;
		}

	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			switch (operationId) {
			case OperationDefinition.Hungama.OperationId.SOCIAL_COMMENT_GET: {
				CommentsListingResponse mCommentsListingResponse = (CommentsListingResponse) responseObjects
						.get(SocialCommentsListingOperation.RESULT_KEY_COMMENTS_LISTING);
				if (mCommentsListingResponse != null) {
					try {
						numOfComments = mCommentsListingResponse
								.getTotalCount();
						if (numOfComments == 0) {
							numOfComments = 0;
							commentsNum.setText(String.valueOf(numOfComments));
							List<Comment> emptyList = new ArrayList<Comment>();
							if (mContext != null) {
								CommentsAdapter mCommentsAdapterNoComments = new CommentsAdapter(
										mContext, emptyList);
								mCommentsList
										.setAdapter(mCommentsAdapterNoComments);
							}
						} else {
							commentsNum.setText(String.valueOf(numOfComments));
							if (mListComments == null)
								mListComments = mCommentsListingResponse
										.getMyData();
							else
								mListComments.addAll(mCommentsListingResponse
										.getMyData());

							if (mContext != null) {
								if (mCommentsAdapter == null) {
									mCommentsAdapter = new CommentsAdapter(
											mContext,
											mCommentsListingResponse
													.getMyData());
									mCommentsList.setAdapter(mCommentsAdapter);
								} else {
									mCommentsAdapter
											.updateCommentsList(mListComments);
									mCommentsAdapter.notifyDataSetChanged();
								}
							}
						}
					} catch (Exception e) {
						numOfComments = 0;
						commentsNum.setText(String.valueOf(numOfComments));
						List<Comment> emptyList = new ArrayList<Comment>();
						if (mContext != null) {
							CommentsAdapter mCommentsAdapterNoComments = new CommentsAdapter(
									mContext, emptyList);
							mCommentsList
									.setAdapter(mCommentsAdapterNoComments);
						}
					}

					// switch (mCommentsListingResponse.getCode()) {
					// case SUCCESS:
					// numOfComments = mCommentsListingResponse
					// .getTotalCount();
					// commentsNum.setText(String.valueOf(numOfComments));
					// if (mListComments == null)
					// mListComments = mCommentsListingResponse
					// .getMyData();
					// else
					// mListComments.addAll(mCommentsListingResponse
					// .getMyData());
					//
					// if (mContext != null) {
					// if (mCommentsAdapter == null) {
					// mCommentsAdapter = new CommentsAdapter(
					// mContext,
					// mCommentsListingResponse.getMyData());
					// mCommentsList.setAdapter(mCommentsAdapter);
					// } else {
					// mCommentsAdapter.updateCommentsList(mListComments);
					// mCommentsAdapter.notifyDataSetChanged();
					// }
					// }
					// break;
					//
					// case NO_COMMENTS_FOUND:
					// numOfComments = 0;
					// commentsNum.setText(String.valueOf(numOfComments));
					// List<Comment> emptyList = new ArrayList<Comment>();
					// if (mContext != null) {
					// CommentsAdapter mCommentsAdapterNoComments = new
					// CommentsAdapter(
					// mContext, emptyList);
					// mCommentsList
					// .setAdapter(mCommentsAdapterNoComments);
					// }
					// break;
					//
					// default:
					// break;
					// }

				}
				mIsThrottling = false;
				break;
			}

			case OperationDefinition.Hungama.OperationId.SOCIAL_COMMENT_POST: {
				CommentsPostResponse mCommentsPostResponse = (CommentsPostResponse) responseObjects
						.get(SocialCommentsPostOperation.RESULT_KEY_COMMENTS_POST);
				if (mCommentsPostResponse != null) {
					// Toast toast = Toast.makeText(getActivity(),
					// getResources().getString(R.string.comments_post_confirmation,
					// String.valueOf(mCommentsPostResponse.getPointsEarned())),
					// Toast.LENGTH_LONG);
					// toast.setGravity(Gravity.CENTER, 0, 0);
					// toast.show();
					commentBox.setText("");
					// refresh list and numOfComments button
					numOfComments++;
					initializeComponents();
					// mDataManager.getComments(mMediaItem.getId(),
					// mMediaItem.getMediaType(), 1, 100, this);

					if (mMediaItem.getMediaContentType() == MediaContentType.VIDEO) {
						mDataManager.checkBadgesAlert("" + mMediaItem.getId(),
								MediaType.VIDEO.toString(),
								SocialBadgeAlertOperation.ACTION_COMMENT,
								CommentsFragment.this);
					} else {
						mDataManager.checkBadgesAlert("" + mMediaItem.getId(),
								mMediaItem.getMediaType().toString(),
								SocialBadgeAlertOperation.ACTION_COMMENT,
								CommentsFragment.this);
					}
				} else {
					Toast toast = Utils.makeText(getActivity(), getResources()
							.getString(R.string.comments_post_error),
							Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
				break;
			}

			case (OperationDefinition.CatchMedia.OperationId.PARTNER_CONSUMER_PROXY_CREATE):

				String activationCode = (String) responseObjects
						.get(ApplicationConfigurations.ACTIVATION_CODE);
				String partnerUserId = (String) responseObjects
						.get(ApplicationConfigurations.PARTNER_USER_ID);
				boolean isRealUser = (Boolean) responseObjects
						.get(ApplicationConfigurations.IS_REAL_USER);
				Map<String, Object> signupFieldsMap = (Map<String, Object>) responseObjects
						.get(PartnerConsumerProxyCreateOperation.RESPONSE_KEY_OBJECT_SIGNUP_FIELDS);

				/*
				 * iterates thru the original signup fields, looking for the
				 * registered phone number, if exists, stores it in the
				 * application configuration as part of the user's credentials.
				 */
				Map<String, Object> fieldMap = (Map<String, Object>) signupFieldsMap
						.get("phone_number");
				String value = "";
				if (fieldMap != null) {
					value = (String) fieldMap.get(VALUE);
				}
				mApplicationConfigurations.setUserLoginPhoneNumber(value);

				// stores partner user id to connect with Hungama REST API.
				mApplicationConfigurations.setPartnerUserId(partnerUserId);
				// mApplicationConfigurations.setIsRealUser(isRealUser);
				// let's party!
				mDataManager.createDeviceActivationLogin(activationCode, this);

				mApplicationConfigurations.setIsUserRegistered(true);
				mDataManager.getUserProfileDetail(this);

				Set<String> tags = Utils.getTags();
				if (!tags.contains("registered_user")) {
					if (tags.contains("non_registered_user"))
						tags.remove("non_registered_user");

					tags.add("registered_user");
					Utils.AddTag(tags);
				}
				break;
            case (OperationDefinition.CatchMedia.OperationId.CONSUMER_DEVICE_LOGIN):
				hideLoadingDialog();
//                String activationCode = (String) responseObjects
//                        .get(ApplicationConfigurations.ACTIVATION_CODE);
//                String partnerUserId = (String) responseObjects
//                        .get(ApplicationConfigurations.PARTNER_USER_ID);
//                boolean isRealUser = (Boolean) responseObjects
//                        .get(ApplicationConfigurations.IS_REAL_USER);
                signupFieldsMap = (Map<String, Object>) responseObjects
                        .get(PartnerConsumerProxyCreateOperation.RESPONSE_KEY_OBJECT_SIGNUP_FIELDS);

            /*
             * iterates thru the original signup fields, looking for the
             * registered phone number, if exists, stores it in the
             * application configuration as part of the user's credentials.
             */
                fieldMap = (Map<String, Object>) signupFieldsMap
                        .get("phone_number");
                value = "";
                if (fieldMap != null) {
                    value = (String) fieldMap.get(VALUE);
                }
                mApplicationConfigurations.setUserLoginPhoneNumber(value);

                // stores partner user id to connect with Hungama REST API.
//                mApplicationConfigurations.setPartnerUserId(partnerUserId);
                // mApplicationConfigurations.setIsRealUser(isRealUser);
                // let's party!
//                mDataManager.createDeviceActivationLogin(activationCode, this);
//				ApsalarEvent.postEvent(getActivity(), ApsalarEvent.LOGIN_COMPLETED);
				ApsalarEvent.postLoginEvent(provider);

                mApplicationConfigurations.setIsUserRegistered(true);
                mDataManager.getUserProfileDetail(this);

                tags = Utils.getTags();
                if (!tags.contains("registered_user")) {
                    if (tags.contains("non_registered_user"))
                        tags.remove("non_registered_user");

                    tags.add("registered_user");
                    Utils.AddTag(tags);
                }

                if (mTwitterLoginFragment != null) {
                    mTwitterLoginFragment.finish();
                    // mIsTwitterLoggedIn = true;
                    // setTwitterImageButtonLoggedIn();
                    // initializeComponents();
                    mGigyaManager.socializeGetUserInfo();
                }
                break;
			case OperationDefinition.Hungama.OperationId.GET_USER_PROFILE:
				hideLoadingDialog();
				UserProfileResponse userProfileResponse = (UserProfileResponse) responseObjects
						.get(GetUserProfileOperation.RESPONSE_KEY_USER_DETAIL);
				if (userProfileResponse != null
						&& userProfileResponse.getCode() == 200) {
					mApplicationConfigurations
							.setHungamaEmail(userProfileResponse.getUsername());
					mApplicationConfigurations
							.setHungamaFirstName(userProfileResponse
									.getFirst_name());
					mApplicationConfigurations
							.setHungamaLastName(userProfileResponse
									.getLast_name());
				}
				break;

			case (OperationDefinition.CatchMedia.OperationId.DEVICE_ACTIVATION_LOGIN_CREATE):
//				ApsalarEvent.postEvent(getActivity(), ApsalarEvent.LOGIN_COMPLETED);
				ApsalarEvent.postLoginEvent(provider);

				Map<String, Object> responseMap = (Map<String, Object>) responseObjects
						.get(CMOperation.RESPONSE_KEY_GENERAL_OBJECT);
				// stores the session and other crucial properties.
				String sessionID = (String) responseMap
						.get(ApplicationConfigurations.SESSION_ID);
				int householdID = ((Long) responseMap
						.get(ApplicationConfigurations.HOUSEHOLD_ID))
						.intValue();
				int consumerID = ((Long) responseMap
						.get(ApplicationConfigurations.CONSUMER_ID)).intValue();
				String passkey = (String) responseMap
						.get(ApplicationConfigurations.PASSKEY);

				mApplicationConfigurations.setSessionID(sessionID);
				mApplicationConfigurations.setHouseholdID(householdID);
				mApplicationConfigurations.setConsumerID(consumerID);
				mApplicationConfigurations.setPasskey(passkey);

				// CampaignsManager mCampaignsManager = CampaignsManager
				// .getInstance(getActivity().getApplicationContext());
				// mCampaignsManager.clearCampaigns();
				// mCampaignsManager.getCampignsList();

				if (mTwitterLoginFragment != null) {
					mTwitterLoginFragment.finish();
					// mIsTwitterLoggedIn = true;
					// setTwitterImageButtonLoggedIn();
					// initializeComponents();
					mGigyaManager.socializeGetUserInfo();
				}

				break;

			default:
				break;
			}

			// ((MainActivity)getActivity()).showLoadingDialog(R.string.application_dialog_loading_content);

		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		if(getActivity()!=null)
			((MainActivity) getActivity()).hideLoadingDialog();
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		switch (operationId) {
		case OperationDefinition.Hungama.OperationId.SOCIAL_COMMENT_GET: {
			Logger.i(TAG, "Failed loading comments");
			mIsThrottling = false;
		}
			break;

		case OperationDefinition.Hungama.OperationId.SOCIAL_COMMENT_POST: {
			Logger.i(TAG, "Failed posting comment");
		}
			break;
        case (OperationDefinition.CatchMedia.OperationId.CONSUMER_DEVICE_LOGIN):
		case OperationDefinition.CatchMedia.OperationId.PARTNER_CONSUMER_PROXY_CREATE: {
			if(!TextUtils.isEmpty(errorMessage)) {
				Toast toast = Toast.makeText(getActivity(), errorMessage,
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
			if(mTwitterLoginFragment!=null)
				mTwitterLoginFragment.setIsFailerLogin(true);
			mGigyaManager.cancelGigyaProviderLogin();

		}
			break;

		default:
			break;
		}
		if ((MainActivity) getActivity() != null)
			((MainActivity) getActivity()).hideLoadingDialog();
//         displayTitle(actionbar_title);

         hideLoadingDialog();
	}

	// ======================================================
	// On Click Method
	// ======================================================

	@Override
	public void onClick(View view) {
		int viewId = view.getId();

		switch (viewId) {
		case R.id.login_signup_button_login:
		case R.id.need_to_login_panel:
			showSocialLoginDialog();
			break;

		case R.id.comments_image_facebook:
			if (!mIsFacebookLoggedIn) {
				mGigyaManager.facebookLogin();
			} else if (view.isSelected()) {
				view.setSelected(false);
				setFacebookImageButtonLoggedIn();
			} else {
				view.setSelected(true);
				setFacebookImageButtonSelected();
			}

			break;

		case R.id.comments_image_twitter:
			if (!mIsTwitterLoggedIn) {
				mGigyaManager.twitterLogin();
			} else if (view.isSelected()) {
				view.setSelected(false);
				setTwitterImageButtonLoggedIn();
			} else {
				view.setSelected(true);
				setTwitterImageButtonSelected();
			}

			break;

		case R.id.post_button:
			if (facebookImageButton.isSelected()
					|| twitterImageButton.isSelected()) {
				String provider = setProvider();
				String encodedTextToPost = null;
				try {
					encodedTextToPost = HungamaApplication.encodeURL(commentBox
							.getText().toString(), "UTF-8");
				} catch (Exception e) {
					Logger.e(TAG, "Failed to encode url");
					e.printStackTrace();
				}
				if (encodedTextToPost != null) {
					if (mMediaItem.getMediaContentType() == MediaContentType.VIDEO) {
						mDataManager.postComment(mMediaItem.getId(),
								MediaType.VIDEO, provider, encodedTextToPost,
								this);
					} else {
						mDataManager.postComment(mMediaItem.getId(),
								mMediaItem.getMediaType(), provider,
								encodedTextToPost, this);
					}
				}

				// Which socail networks selected on posting
				StringBuilder socialNetworksSelected = new StringBuilder();
				if (facebookImageButton.isSelected()) {
					socialNetworksSelected.append("Facebook");
				}
				if (twitterImageButton.isSelected()) {
					if (socialNetworksSelected.length() == 0) {
						socialNetworksSelected.append("Twitter");
					} else {
						socialNetworksSelected.append(", Twitter");
					}
				}

				// Flurry report: comment posted
				Map<String, String> reportMap = new HashMap<String, String>();
				reportMap.put(
						FlurryConstants.FlurryComments.UserTappedOnComment
								.toString(), Boolean.TRUE.toString());
				reportMap.put(FlurryConstants.FlurryComments.Title.toString(),
						mMediaItem.getTitle());
				reportMap.put(FlurryConstants.FlurryComments.Type.toString(),
						mMediaItem.getMediaType().toString());
				reportMap
						.put(FlurryConstants.FlurryComments.SourceSection
								.toString(), mFlurrySourceSection);
				reportMap.put(FlurryConstants.FlurryComments.IsSocialLoggedIn
						.toString(), mGigyaManager.isSocialNetorkConnected()
						.toString());
				reportMap.put(
						FlurryConstants.FlurryComments.SocialNetworksSelected
								.toString(), socialNetworksSelected.toString());
				Analytics
						.logEvent(FlurryConstants.FlurryComments.CommentPosted
								.toString(), reportMap);

			} else {
				Toast toast = Utils.makeText(getActivity(), getResources()
						.getString(R.string.comments_post_select_provider),
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}

			break;

		case R.id.comment_user_image:
			View parent = (View) view.getParent();
			Comment commentLine = (Comment) parent.getTag(R.id.view_tag_object);
			Bundle arguments = new Bundle();
			arguments.putString(ProfileActivity.DATA_EXTRA_USER_ID,
					String.valueOf(commentLine.getUserId()));

			ProfileActivity mediaDetailsFragment = new ProfileActivity();
			FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();
			FragmentTransaction fragmentTransaction = mFragmentManager
					.beginTransaction();
			mediaDetailsFragment.setArguments(arguments);
			fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
					mediaDetailsFragment, "ProfileActivity");
			fragmentTransaction.addToBackStack("ProfileActivity");
			if(Constants.IS_COMMITALLOWSTATE)
				fragmentTransaction.commitAllowingStateLoss();
			else
				fragmentTransaction.commit();

//			Intent profileActivity = new Intent(getActivity()
//					.getApplicationContext(), ProfileActivity.class);
//			profileActivity.putExtras(arguments);
//			startActivity(profileActivity);
			break;

		default:
			break;
		}

	}

	// ======================================================
	// Gigya Response Listener Methods
	// ======================================================

	private SocialNetwork provider;

	@Override
	public void onGigyaLoginListener(SocialNetwork provider,
			Map<String, Object> signupFields, long setId) {
		this.provider = provider;
		if (provider == SocialNetwork.TWITTER) {
			// Twitter
			//toggleFragmentTitle();
			FragmentTransaction fragmentTransaction = mFragmentManager
					.beginTransaction();
			fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
					R.anim.slide_left_exit, R.anim.slide_right_enter,
					R.anim.slide_right_exit);

			mTwitterLoginFragment/* fragment */= new TwitterLoginFragment();
			mTwitterLoginFragment.init(signupFields, setId);
			fragmentTransaction.replace(R.id.comments_fragmant_container,
					mTwitterLoginFragment, TwitterLoginFragment.FRAGMENT_TWITTER_LOGIN);
			fragmentTransaction.addToBackStack(TwitterLoginFragment.class
					.toString());
			fragmentTransaction.commit();

			// Listen to result from TwitterLoinFragment
			mTwitterLoginFragment.setOnTwitterLoginListener(this);
//			setTwitterImageButtonLoggedIn();
		} else {
			// FaceBook, Google
			// Call PCP
			if((provider== SocialNetwork.FACEBOOK && TextUtils.isEmpty(mApplicationConfigurations.getGigyaFBEmail()))
					|| (provider== SocialNetwork.GOOGLE && TextUtils.isEmpty(mApplicationConfigurations.getGigyaGoogleEmail()))){
				mGigyaManager.removeConnetion(provider);
				Toast.makeText(getActivity(), R.string.gigya_login_error_email_required, Toast.LENGTH_SHORT).show();
			} else {
				mDataManager.createPartnerConsumerProxy(signupFields, setId, this,
						false);
				mGigyaManager.socializeGetUserInfo();
			}
		}

		// Flurry report: Social login
		String registrationStatus;
		if (mApplicationConfigurations.isRealUser()) {
			registrationStatus = FlurryConstants.FlurryUserStatus.Login
					.toString();
		} else {
			registrationStatus = FlurryConstants.FlurryUserStatus.NewRegistration
					.toString();
		}
		Map<String, String> reportMap = new HashMap<String, String>();
		reportMap.put(FlurryConstants.FlurryUserStatus.TypeOfLogin.toString(),
				provider.name());
		reportMap.put(
				FlurryConstants.FlurryUserStatus.RegistrationStatus.toString(),
				registrationStatus);
		Analytics.logEvent(
				FlurryConstants.FlurryUserStatus.SocialLogin.toString(),
				reportMap);

	}

	@Override
	public void onSocializeGetFriendsInfoListener(List<FBFriend> fbFriendsList) {
	}

	@Override
	public void onSocializeGetContactsListener(
			List<GoogleFriend> googleFriendsList) {
	}

	@Override
	public void onSocializeGetUserInfoListener() {
		mIsFacebookLoggedIn = mGigyaManager.isFBConnected();
		mIsTwitterLoggedIn = mGigyaManager.isTwitterConnected();
		try {
			initializeComponents();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onGigyaLogoutListener() {
	}

	// ======================================================
	// Twitter login callback
	// ======================================================

	@Override
	public void onTwitterLoginListener(TwitterLoginFragment fragment,
			Map<String, Object> signupFields, long setId) {
		this.provider = SocialNetwork.TWITTER;
		// Call PCP
		// It's include the email and password that user insert in
		// TwitterLoginFragment
		mDataManager.createPartnerConsumerProxy(signupFields, setId, this,
				false);
		mTwitterLoginFragment = fragment;

	}

	@Override
	public void onCancelLoginListener() {
		provider = null;
		mGigyaManager.removeConnetion(SocialNetwork.TWITTER);
		mGigyaManager.setIsTwitterConnected(false);
	}

	// ======================================================
	// Comments Adapter
	// ======================================================

	private static class ViewHolder {
		TextView commentUserName;
		TextView commentDate;
		TextView commentText;
		ImageView commentUserImage;
	}

	private class CommentsAdapter extends BaseAdapter {

		private List<Comment> mComments;
		private LayoutInflater mInflater;

		private Context mContext;

		public CommentsAdapter(Context context, List<Comment> comments) {

			mContext = context;

			mComments = comments;
			mInflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			// initializes the image loader.
			int imageSize = 110;
			if (getActivity() != null) {
				imageSize = getResources().getDimensionPixelSize(
						R.dimen.comment_result_line_image_size);
			}

			// creates the cache.
			// ImageCache.ImageCacheParams cacheParams =
			// new ImageCache.ImageCacheParams(getActivity(),
			// DataManager.FOLDER_THUMBNAILS_CACHE);
			// cacheParams.setMemCacheSizePercent(getActivity(), 0.10f);
			//
			// mImageFetcher = new ImageFetcher(getActivity(), imageSize);
			// mImageFetcher.setLoadingImage(R.drawable.background_home_tile_album_default);
			// mImageFetcher.addImageCache(getChildFragmentManager(),
			// cacheParams);
			// mImageFetcher.setImageFadeIn(true);
		}

		@Override
		public int getCount() {
			return mComments.size();
		}

		@Override
		public Object getItem(int position) {
			return mComments.get(position);
		}

		@Override
		public long getItemId(int position) {
			return mComments.get(position).getUserId();
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			ViewHolder viewHolder;
			// create view if not exist.
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.list_item_comment,
						parent, false);

				viewHolder = new ViewHolder();

				viewHolder.commentUserName = (TextView) convertView
						.findViewById(R.id.comment_user_name);
				viewHolder.commentDate = (TextView) convertView
						.findViewById(R.id.comment_date);
				viewHolder.commentUserImage = (ImageView) convertView
						.findViewById(R.id.comment_user_image);

				convertView.setTag(R.id.view_tag_view_holder, viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView
						.getTag(R.id.view_tag_view_holder);
			}

			// populate the view from the Comments list.
			Comment comment = mComments.get(position);
			// stores the object in the view.
			convertView.setTag(R.id.view_tag_object, comment);

			viewHolder.commentUserName.setText(comment.getUserName());
			viewHolder.commentDate.setText("at " + comment.getTime());
			TextView commentText = (TextView) convertView
					.findViewById(R.id.comment_text);
			commentText.setText(comment.getComment());

			// gets the image and its size.
			// viewHolder.commentUserImage = (ImageView)
			// convertView.findViewById(R.id.comment_user_image);
			viewHolder.commentUserImage
					.setOnClickListener(CommentsFragment.this);
			// viewHolder.commentUserImage.setOnClickListener(new
			// OnClickListener() {
			//
			// @Override
			// public void onClick(View v) {
			// // Bundle arguments = new Bundle();
			// // arguments.putLong(ProfileActivity.DATA_EXTRA_USER_ID,
			// comment.getUserId());
			// // Intent profileActivity = new
			// Intent(getActivity().getApplicationContext(),
			// ProfileActivity.class);
			// // profileActivity.putExtras(arguments);
			// // startActivity(profileActivity);
			// }
			// });
			Picasso.with(mContext).cancelRequest(viewHolder.commentUserImage);
			if (mContext != null && !TextUtils.isEmpty(comment.getPhotoUrl())) {
				// mImageFetcher.loadImage(comment.getPhotoUrl(),
				// viewHolder.commentUserImage);
				Picasso.with(mContext)
						.load(comment.getPhotoUrl())
						.placeholder(
								R.drawable.background_home_tile_album_default)
						.into(viewHolder.commentUserImage);
			}
			// else {
			// if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			// viewHolder.commentUserImage.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_media_details_playlist_inside_thumb));
			// } else {
			// viewHolder.commentUserImage.setBackground(getResources().getDrawable(R.drawable.background_media_details_playlist_inside_thumb));
			// }
			// }

			return convertView;
		}

		public void updateCommentsList(List<Comment> mComments) {
			this.mComments = mComments;
		}

	}

	@Override
	public void onFacebookInvite() {
	}

	@Override
	public void onTwitterInvite() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hungama.myplay.activity.gigya.GigyaManager.OnGigyaResponseListener
	 * #onFailSocialGetFriendsContactsListener()
	 */
	@Override
	public void onFailSocialGetFriendsContactsListener() {
		// TODO Auto-generated method stub

	}

	private class ScrollToBottomListener implements OnScrollListener {
		private int totalItemCount;
		private int currentFirstVisibleItem;
		private int currentVisibleItemCount;

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			this.currentFirstVisibleItem = firstVisibleItem;
			this.currentVisibleItemCount = visibleItemCount;
			this.totalItemCount = totalItemCount;
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (mIsThrottling)
				return;

			if (scrollState == SCROLL_STATE_IDLE && currentVisibleItemCount > 0) {
				Logger.v(TAG, "totalItemCount " + totalItemCount
						+ " currentFirstVisibleItem " + currentFirstVisibleItem
						+ " currentVisibleItemCount " + currentVisibleItemCount);

				boolean lastItemVisible = (currentFirstVisibleItem + currentVisibleItemCount) == totalItemCount;
				boolean needMorePages = !Utils.isListEmpty(mListComments)
						&& mListComments.size() < numOfComments
						&& (mListComments.size()
								% Constants.LOADING_CHUNK_NUMBER == 0);

				if (lastItemVisible && needMorePages) {
					Logger.v(TAG, "More Items are requested - throttling !!!");
					throttleForNextPage();
				}
			}
		}
	}

	private boolean mIsThrottling = false;

	private void throttleForNextPage() {
		int currentPagingIndex = mListComments.size();
		mIsThrottling = true;

		// Querying like a bous!
		if (mMediaItem.getMediaContentType() == MediaContentType.VIDEO) {
			mDataManager.getComments(mMediaItem.getId(), MediaType.VIDEO,
					currentPagingIndex + 1, 30, this);
		} else {
			mDataManager
					.getComments(mMediaItem.getId(), mMediaItem.getMediaType(),
							currentPagingIndex + 1, 30, this);
		}
	}

	@Override
	public void onDestroyView() {
//		System.gc();
//		System.runFinalization();
		super.onDestroyView();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hungama.myplay.activity.gigya.GigyaManager.OnGigyaResponseListener
	 * #onCancelRequestListener()
	 */
	@Override
	public void onCancelRequestListener() {

	}

	@Override
	public void onConnectionRemoved() {

	}

//    private void displayTitle(final String actionbar_title) {
//        //((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        ((MainActivity)getActivity()).showBackButtonWithTitle(actionbar_title,"");
//
//    }


    private boolean results;
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
                int count = getActivity().getSupportFragmentManager()
                        .getBackStackEntryCount();
                if (count > 0) {
                    results = false;

                    getActivity().getSupportFragmentManager().popBackStack();
                    //mplayerbar.collapseexpandplayerbar(false);
//                    displayTitle(actionbar_title);

                    isNeedToClose = true;

                    return true;
                } else {
                    //mplayerbar.collapseexpandplayerbar(false);
                    getActivity().getSupportFragmentManager().beginTransaction().remove(CommentsFragment.this).commit();

                    return true;
                }
            }
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
        return false;
    }

    @Override
    public void setTitle(boolean needOnlyHight,boolean needToSetTitle) {

    }


}
