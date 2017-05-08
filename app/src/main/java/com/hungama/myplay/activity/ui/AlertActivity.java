/**
 * 
 */
package com.hungama.myplay.activity.ui;

/**
 * @author stas
 *
 */
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.HungamaApplication;

import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.gcm.IntentReceiver;
import com.hungama.myplay.activity.ui.widgets.CustomAlertDialog;
import com.hungama.myplay.activity.ui.widgets.HomeTabBar;
import com.hungama.myplay.activity.util.Extras;
import com.hungama.myplay.activity.util.Links;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Needles;
import com.hungama.myplay.activity.util.ScreenLockStatus;
import com.hungama.myplay.activity.util.Utils;
import com.urbanairship.richpush.RichPushManager;

import java.io.Serializable;
import java.util.Set;

public class AlertActivity extends Activity implements OnClickListener {
	// private Button view, ok;
	private TextView view, ok;
	private TextView content;
	public static boolean isMessage = false;
	private String StringClassname, message, code, alert;
	private String extraName;
	public static final String ALERT_MARK = "from_alert";
	public static final String IS_CUSTOM_RICHPUSH_LINK = "is_custom_richpush_link";
	public static final String ALERT_NOTI = "from_notification";
	boolean isRichPush;
	boolean isDeepLinked;

	public static final String ACTION_UA_LINK = "com.hungama.myplay.activity.intent.action.ualink";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Logger.s("---------------onCreate---------------");
		super.onCreate(savedInstanceState);
		/* requestWindowFeature(Window.FEATURE_LEFT_ICON); */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.alert_layout);
		// getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
		// R.drawable.icon_launcher);
		content = (TextView) findViewById(R.id.tvAlertContent);
		if (getIntent().getStringExtra("alert") != null) {
			alert = getIntent().getStringExtra("alert");
			content.setText(alert);
		}
		view = (TextView) findViewById(R.id.bView);
		ok = (TextView) findViewById(R.id.bOk);
		view.setOnClickListener(this);
		ok.setOnClickListener(this);

		try {
			isRichPush = RichPushManager.isRichPushMessage(getIntent()
					.getExtras());
		} catch (Exception e) {
			isRichPush = false;
		}

		code = null;
		message = null;
		if (getIntent().getExtras() != null
				&& (getIntent().getExtras().containsKey("code") || getIntent()
						.getExtras().containsKey("message"))) {
			code = getIntent().getStringExtra("code");
			message = getIntent().getStringExtra("message");
		}
		isDeepLinked = (!isRichPush && code != null);

		// try{
		// if(code!=null && Integer.parseInt(code)>0)
		// isDeepLinked = true;
		// } catch(Exception e){
		// Logger.e("AlertActivity", e.toString());
		// }
		if (!isDeepLinked && !isRichPush) {
			view.setVisibility(View.GONE);
		}

		try {
			Set<String> keyset = getIntent().getExtras().keySet();
			for (String key : keyset) {
				com.hungama.myplay.activity.util.Logger.i("Key", key + " ::: "
						+ getIntent().getExtras().get(key));
			}
		} catch (Exception e){
			Logger.printStackTrace(e);
		}

		try {
			if (getIntent().getBooleanExtra(IS_CUSTOM_RICHPUSH_LINK, false)
					|| !getIntent().getBooleanExtra("isAppOpen", false)) {
				view.performClick();
			}
		} catch (Exception e){
			Logger.printStackTrace(e);
		}
		// xtpl
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		Logger.s("---------------onNewIntent---------------");
		super.onNewIntent(intent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		Logger.s("---------------onStart---------------");
		super.onStart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		Logger.s("---------------onRestart---------------");
		super.onRestart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onUserLeaveHint() {
		ScreenLockStatus.getInstance(getBaseContext()).onStop(true, this);
		super.onUserLeaveHint();
	}

	@Override
	protected void onResume() {
		super.onResume();
		ScreenLockStatus.getInstance(getBaseContext()).onResume(this, this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		Logger.s("---------------onDestroy---------------");
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		super.onStop();
		ScreenLockStatus.getInstance(getBaseContext()).onStop();
	}

	@Override
	public void onClick(View v) {
		try {
			if (v == ok) {
				finish();
				return;
			}

			final ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
					.getInstance(AlertActivity.this);
			if (code != null && code.equals("19")) {
				if (mApplicationConfigurations.getSaveOfflineMode()) {
					Intent intent = new Intent(this, GoOfflineActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
							| Intent.FLAG_ACTIVITY_CLEAR_TOP);// xtpl
					startActivity(intent);
					finish();
				} else if (!getIntent().getBooleanExtra("isAppOpen", false)) {
					mApplicationConfigurations.setSaveOfflineMode(true);
					Intent intent = new Intent(this, GoOfflineActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
							| Intent.FLAG_ACTIVITY_CLEAR_TOP);// xtpl
					startActivity(intent);
					finish();
				} else {
					mApplicationConfigurations.setSaveOfflineMode(true);
					sendBroadcast(new Intent(
							MainActivity.ACTION_OFFLINE_MODE_CHANGED));
					finish();
				}
			} else if (mApplicationConfigurations.getSaveOfflineMode()) {
				CustomAlertDialog alertBuilder = new CustomAlertDialog(this);
				alertBuilder.setTitle(Utils.getMultilanguageTextHindi(
						getApplicationContext(),
						getResources().getString(
								R.string.caching_text_popup_title_go_online)));
				alertBuilder
						.setMessage(Utils
								.getMultilanguageTextHindi(
										getApplicationContext(),
										getResources()
												.getString(
														R.string.caching_text_message_go_online_alert)));
				alertBuilder.setPositiveButton(Utils.getMultilanguageTextHindi(
						getApplicationContext(),
						getResources().getString(
								R.string.caching_text_popup_title_go_online)),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								mApplicationConfigurations
										.setSaveOfflineMode(false);
								try {
									Intent intent = new Intent(
											MainActivity.ACTION_OFFLINE_MODE_CHANGED);
									intent.putExtra("isFromPush", true);
									intent.putExtras(getIntent().getExtras());
									sendBroadcast(intent);
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
								// view.performClick();
							}
						});
				alertBuilder.setNegativeButton(Utils.getMultilanguageTextHindi(
						getApplicationContext(),
						getResources().getString(
								R.string.caching_text_popup_button_cancel)),
						null);
				// alertBuilder.create();
				alertBuilder.show();
			} else {
				Logger.s("RichPushManager :::: " + isRichPush);
				if (!RichPushManager.isRichPushMessage(getIntent().getExtras())
						&& (code == null || Integer.parseInt(code) < 1)) {
					Intent intent = new Intent(this, HomeActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);// xtpl
					intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);// xtpl
					if(!TextUtils.isEmpty(code) && Integer.parseInt(code) == -1)
						intent.putExtra("login", true);
					finish();
					intent.putExtra(ALERT_MARK, true);
					HomeActivity.set = false;
					startActivity(intent);
					return;
				}
				com.hungama.myplay.activity.util.Logger.i("", code + " :::: "
						+ message);
				if (v == view && code != null) {
					StringClassname = Links.getLinks(Integer.parseInt(code))
							.getAction();
					extraName = Extras.getExtras(Integer.parseInt(code))
							.getExtra();
					com.hungama.myplay.activity.util.Logger.i("",
							StringClassname + " :::: " + extraName);
					Class<?> c = null;
					if (StringClassname != null) {
						try {
							c = Class.forName(StringClassname);
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					Intent intent = new Intent(this, c);
					intent.putExtra(ALERT_NOTI, true);
					if (extraName != null) {
						String extra = getIntent().getStringExtra(extraName);
						if (extraName.equalsIgnoreCase("radio_id"))
							extra = getIntent().getStringExtra(
									IntentReceiver.CONTENT_ID);
//						else if (Integer.parseInt(code) == 44)
//							extra = getIntent().getStringExtra(
//									IntentReceiver.STATION_ID);
						if (getIntent().getStringExtra("Category") != null) {
							intent.putExtra("Category", getIntent()
									.getStringExtra("Category"));
						}
						if (code.equals("7")) {
							isMessage = true;
							extraName = "audio_" + extraName;
							if (getIntent().getStringExtra(
									IntentReceiver.CONTENT_TYPE) != null) {
								intent.putExtra(
										IntentReceiver.CONTENT_TYPE,
										getIntent().getStringExtra(
												IntentReceiver.CONTENT_TYPE));
							}
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE,
									(Serializable) MediaContentType.MUSIC);
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION,
									HomeTabBar.TAB_INDEX_MUSIC);
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_MEDIA_CATEGORY_TYPE,
									HomeTabBar.TAB_ID_LATEST);
						} else if (code.equals("8")) {
							extraName = "video_" + extraName;
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE,
									(Serializable) MediaContentType.VIDEO);
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION,
									HomeTabBar.TAB_INDEX_VIDEO);
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_MEDIA_CATEGORY_TYPE,
									HomeTabBar.TAB_ID_LATEST);
						} else if (code.equals("36")) {
							extraName = "video_in_audio_" + extraName;
							if (getIntent().getStringExtra(
									IntentReceiver.CONTENT_TYPE) != null) {
								intent.putExtra(
										IntentReceiver.CONTENT_TYPE,
										getIntent().getStringExtra(
												IntentReceiver.CONTENT_TYPE));
							}
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE,
									(Serializable) MediaContentType.MUSIC);
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION,
									HomeTabBar.TAB_INDEX_MUSIC);
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_MEDIA_CATEGORY_TYPE,
									HomeTabBar.TAB_ID_LATEST);
							HomeActivity.videoInAlbumSet = false;
						} else if (code.equals("42")) {
							extraName = "browse_by_" + extraName;
							if (getIntent().getStringExtra(
									IntentReceiver.CONTENT_TYPE) != null) {
								intent.putExtra(
										IntentReceiver.CONTENT_TYPE,
										getIntent().getStringExtra(
												IntentReceiver.CONTENT_TYPE));
							}
						}

						intent.putExtra(extraName, extra);
					}
					if (Needles.contains(Integer.parseInt(code))) {
						intent.putExtra(Extras
								.getExtras(Integer.parseInt(code)).getExtra(),
								true);
						if (Extras.getExtras(Integer.parseInt(code)).getExtra()
								.equals("video_featured")) {
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE,
									(Serializable) MediaContentType.VIDEO);
							// intent.putExtra(
							// HomeActivity.ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION,
							// HomeTabBar.TAB_ID_FEATURED);
							// intent.putExtra(
							// HomeActivity.ACTIVITY_EXTRA_IS_FROM_MY_PREFERENCES,
							// false);
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION,
									HomeTabBar.TAB_INDEX_VIDEO);
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_MEDIA_CATEGORY_TYPE,
									HomeTabBar.TAB_ID_FEATURED);
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
									| Intent.FLAG_ACTIVITY_CLEAR_TOP
							/* | Intent.FLAG_ACTIVITY_CLEAR_TASK */);
						} else if (Extras.getExtras(Integer.parseInt(code))
								.getExtra().equals("video_latest")) {
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE,
									(Serializable) MediaContentType.VIDEO);
							// intent.putExtra(
							// HomeActivity.ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION,
							// HomeTabBar.TAB_ID_LATEST);
							// intent.putExtra(
							// HomeActivity.ACTIVITY_EXTRA_IS_FROM_MY_PREFERENCES,
							// false);
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION,
									HomeTabBar.TAB_INDEX_VIDEO);
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_MEDIA_CATEGORY_TYPE,
									HomeTabBar.TAB_ID_LATEST);
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
									| Intent.FLAG_ACTIVITY_CLEAR_TOP
							/* | Intent.FLAG_ACTIVITY_CLEAR_TASK */);
						} else if (Extras.getExtras(Integer.parseInt(code))
								.getExtra().equals("video_recommended")) {
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE,
									(Serializable) MediaContentType.VIDEO);
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION,
									HomeTabBar.TAB_ID_RECOMMENDED);
							// intent.putExtra(
							// HomeActivity.ACTIVITY_EXTRA_IS_FROM_MY_PREFERENCES,
							// false);
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
									| Intent.FLAG_ACTIVITY_CLEAR_TOP
							/* | Intent.FLAG_ACTIVITY_CLEAR_TASK */);
						} else if (Extras.getExtras(Integer.parseInt(code))
								.getExtra().equals("audio_latest")) {
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE,
									(Serializable) MediaContentType.MUSIC);
							// intent.putExtra(
							// HomeActivity.ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION,
							// HomeTabBar.TAB_ID_LATEST);
							// intent.putExtra(
							// HomeActivity.ACTIVITY_EXTRA_IS_FROM_MY_PREFERENCES,
							// false);
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION,
									HomeTabBar.TAB_INDEX_MUSIC);
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_MEDIA_CATEGORY_TYPE,
									HomeTabBar.TAB_ID_LATEST);
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
									| Intent.FLAG_ACTIVITY_CLEAR_TOP
									/* | Intent.FLAG_ACTIVITY_CLEAR_TASK */
									| Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
						} else if (Extras.getExtras(Integer.parseInt(code))
								.getExtra().equals("audio_featured")) {
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE,
									(Serializable) MediaContentType.MUSIC);
							// intent.putExtra(
							// HomeActivity.ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION,
							// HomeTabBar.TAB_ID_FEATURED);
							// intent.putExtra(
							// HomeActivity.ACTIVITY_EXTRA_IS_FROM_MY_PREFERENCES,
							// false);
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION,
									HomeTabBar.TAB_INDEX_MUSIC_POPULAR);
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_MEDIA_CATEGORY_TYPE,
									HomeTabBar.TAB_ID_FEATURED);
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
									| Intent.FLAG_ACTIVITY_CLEAR_TOP
									/* | Intent.FLAG_ACTIVITY_CLEAR_TASK */
									| Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
						} else if (Extras.getExtras(Integer.parseInt(code))
								.getExtra().equals("audio_recommended")) {
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE,
									(Serializable) MediaContentType.MUSIC);
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION,
									HomeTabBar.TAB_ID_RECOMMENDED);
							// intent.putExtra(
							// HomeActivity.ACTIVITY_EXTRA_IS_FROM_MY_PREFERENCES,
							// false);
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
									| Intent.FLAG_ACTIVITY_CLEAR_TOP
									/* | Intent.FLAG_ACTIVITY_CLEAR_TASK */
									| Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
						} else if (Extras.getExtras(Integer.parseInt(code))
								.getExtra().equals("content_type")) {
							if (getIntent().getStringExtra(
									IntentReceiver.CONTENT_TYPE).equals("1"))
								intent.putExtra(
										HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE,
										(Serializable) MediaContentType.VIDEO);
							else
								intent.putExtra(
										HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE,
										(Serializable) MediaContentType.MUSIC);
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION,
									HomeTabBar.TAB_ID_LATEST);
							// intent.putExtra(
							// HomeActivity.ACTIVITY_EXTRA_IS_FROM_MY_PREFERENCES,
							// false);
							intent.putExtra(
									HomeActivity.ACTIVITY_EXTRA_OPEN_BROWSE_BY,
									true);
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
									| Intent.FLAG_ACTIVITY_CLEAR_TOP
									/* | Intent.FLAG_ACTIVITY_CLEAR_TASK */
									| Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
						}
					}
					// isMessage = true;
					if ((extraName != null && (Extras
							.getExtras(Integer.parseInt(code)).getExtra()
							.equals("top_celebs")
							|| Extras.getExtras(Integer.parseInt(code))
									.getExtra().equals("radio_id") || Extras
							.getExtras(Integer.parseInt(code)).getExtra()
							.equals("artist_id") || Extras
							.getExtras(Integer.parseInt(code)).getExtra()
							.equals("Station_ID")))
							|| StringClassname
									.equalsIgnoreCase("com.hungama.myplay.activity.ui.RadioActivity")) {
						// sendBroadcast(new Intent(ACTION_UA_LINK));
						// if (code != null && Integer.parseInt(code) == 11)
						// intent.putExtra("load_live_radio", true);
						// else if (code != null && Integer.parseInt(code) ==
						// 12)
						// intent.putExtra("load_celeb_radio", true);
						// intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
						// | Intent.FLAG_ACTIVITY_CLEAR_TOP
						// /* | Intent.FLAG_ACTIVITY_CLEAR_TASK */);
						// isMessage = false;
					} else if (code != null && Integer.parseInt(code) == 11) {
						// intent.putExtra("load_live_radio", true);
						// intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
						// | Intent.FLAG_ACTIVITY_CLEAR_TOP);
						// isMessage = false;
					} else if (code != null
							&& (Integer.parseInt(code) == 12 || Integer
									.parseInt(code) == 45)) {
						intent.putExtra("load_celeb_radio", true);
						intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
								| Intent.FLAG_ACTIVITY_CLEAR_TOP);
						isMessage = false;
					}
					if (extraName != null
							&& (Extras.getExtras(Integer.parseInt(code))
									.getExtra().equals("content_id") || Extras
									.getExtras(Integer.parseInt(code))
									.getExtra().equals("app_tour"))
							|| (code != null && ((Integer.parseInt(code) >= 11 && Integer
									.parseInt(code) <= 18)
									|| Integer.parseInt(code) == 30
									|| Integer.parseInt(code) == 40 || Integer
									.parseInt(code) >= 41))) {
						intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
								| Intent.FLAG_ACTIVITY_CLEAR_TOP
						/* | Intent.FLAG_ACTIVITY_CLEAR_TASK */);
					}
					intent.putExtra("code", code);
					intent.putExtra(ALERT_MARK, true);
					if (getIntent().getBooleanExtra(IS_CUSTOM_RICHPUSH_LINK,
							false))
						intent.putExtra(HomeActivity.NOTIFICATION_MAIL, false);

					// } else
					// HomeActivity.set = false;
					// intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION |
					// Intent.FLAG_ACTIVITY_CLEAR_TOP |
					// Intent.FLAG_ACTIVITY_CLEAR_TASK);// xtpl
					// intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);//
					// xtpl
					// isMessage = true;
					HomeActivity.set = false;
					finish();
					startActivity(intent);
				} else if (v == ok) {
					finish();
					System.exit(0);
				} else {
					String messageId = getIntent().getStringExtra(
							IntentReceiver.EXTRA_MESSAGE_ID_KEY);
					com.hungama.myplay.activity.util.Logger.v("Alert",
							"Notified of a notification opened with id "
									+ messageId);
					Intent messageIntent = null;
					// Set the activity to receive the intent
					if ("home".equals(getIntent().getStringExtra(
							IntentReceiver.ACTIVITY_NAME_KEY))) {
						messageIntent = new Intent(this, MainActivity.class);
					} else {
						// default to the Inbox
						messageIntent = new Intent(this, HomeActivity.class);
						HomeActivity.set = false;
					}
					messageIntent
							.putExtra(HomeActivity.NOTIFICATION_MAIL, true);
					messageIntent.putExtra(
							HungamaApplication.MESSAGE_ID_RECEIVED_KEY,
							messageId);
					messageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_SINGLE_TOP
							| Intent.FLAG_ACTIVITY_NEW_TASK);
					finish();
					startActivity(messageIntent);
					com.hungama.myplay.activity.util.Logger.i("Failure",
							"Couldn't read code");
				}
			}
		} catch (Exception e) {
		} catch (Error e) {
		}
	}
}
