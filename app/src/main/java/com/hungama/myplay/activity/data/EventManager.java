package com.hungama.myplay.activity.data;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;

import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.NoConnectivityException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.events.AppEvent;
import com.hungama.myplay.activity.data.events.Event;
import com.hungama.myplay.activity.data.events.PlayEvent;
import com.hungama.myplay.activity.operations.catchmedia.CMDecoratorOperation;
import com.hungama.myplay.activity.operations.catchmedia.EventMultiCreateOperation;
import com.hungama.myplay.activity.operations.catchmedia.SessionCreateOperation;
import com.hungama.myplay.activity.operations.hungama.NewMediaDetailsOperation;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Manages posting user events to CM servers.
 */
public class EventManager {

	private static final String TAG = "EventManager";

	public static final String PLAY = "play";
	public static final String VIEW = "view";
	// public static final String SCROLL = "scroll";
	public static final String CLICK = "click";

	private final Context mContext;
//	private final ConnectivityManager mConnectivityManager;

	private final String mServerUrl;

	private volatile List<Event> mEventsQueue;
	private DataManager mDataManager;
	static EventManager manager;

	// ======================================================
	// Public.
	// ======================================================
	public static EventManager getInstance(Context applicationContezt,
			String serverUrl, List<Event> eventsQueue) {
		if (manager == null) {
			manager = new EventManager(applicationContezt, serverUrl,
					eventsQueue);
		} else {

		}
		return manager;
	}

	long lastEventPostTime = 0;

	private EventManager(Context applicationContezt, String serverUrl,
			List<Event> eventsQueue) {
		lastEventPostTime = System.currentTimeMillis();
		mContext = applicationContezt;
		mDataManager = DataManager.getInstance(mContext);
		mServerUrl = serverUrl;
//		mConnectivityManager = (ConnectivityManager) mContext
//				.getSystemService(Context.CONNECTIVITY_SERVICE);
		// synchronizing the list itself to make it been changed from different
		// threads.
		if (eventsQueue == null) {
			eventsQueue = new ArrayList<Event>();
		}
		mEventsQueue = eventsQueue;// Collections.synchronizedList(eventsQueue);
		// if (mEventsQueue == null)
		// mEventsQueue = new ArrayList<Event>();

		// mEventsPosterExecutor = Executors.newSingleThreadExecutor();
	}

	/**
	 * Adds an Event for posting to the servers.
	 * 
	 * @param event
	 */
	public void addEvent(Event event) {
		Logger.e("EventTrack", "EM mEventsQueue." + mEventsQueue.size());

		if (event == null) {
			Logger.e(TAG, "Event is null, skipping this one.");
			Logger.e("EventTrack", "Event is null, skipping this one."
					+ mEventsQueue.size());

			return;
		}

		if (TextUtils.isEmpty(event.getRegularTimestamp())) {
			Logger.e(TAG, "Event's timestamp is empty, skipping this one.");
			Logger.e("EventTrack",
					"Event's timestamp is empty, skipping this one."
							+ mEventsQueue.size());

			return;
		}

		mEventsQueue.add(event);
		Logger.e("EventTrack", "EM mEventsQueue. after" + mEventsQueue.size());

        mDataManager.storeEvents(mEventsQueue, true);
		if (Utils.isConnected()) {
			postEvent(false);
		}/* else {
			mDataManager.storeEvents(mEventsQueue, true);
		}*/
	}

	/**
	 * Stops any panding and running tasks which posts events.
	 */
	public void stopPostingEvents() {
		// mEventsPosterExecutor.shutdownNow();
		if (isRunning()) {
			stopPosting();
		}

	}

	public List<Event> getEvents() {
		return mEventsQueue;
	}

	public void clearQueue() {
		mEventsQueue.clear();
		manager = null;
	}

	public void flushEvents() {
		Logger.e(TAG, "flushEvents ::: " + mEventsQueue.size());
		if (mEventsQueue != null && mEventsQueue.size() > 0)
			postEvent(true);
		// mEventsPosterExecutor.execute(new EventPoster(event));
	}

	// ======================================================
	// Private.
	// ======================================================
	PostPendingEvents running;

	private void postEvent(boolean flush) {
		Logger.e(TAG, "postEvent ::: " + isRunning());
		if (!isRunning()
				&& (flush || mEventsQueue.size() >= MAXSize || System
						.currentTimeMillis() - lastEventPostTime > 60000)) {
			running = new PostPendingEvents();
			running.execute();
		}
	}

	public static volatile boolean isPostRunning = false;

	public static boolean isRunning() {
		return isPostRunning;
	}

	public void stopPosting() {
		isPostRunning = false;
		if (running != null) {
			running.cancel(true);
			running = null;
		}

	}

	int MAXSize = 5;

	public class PostPendingEvents extends AsyncTask<Void, Void, Boolean> {

		private PostPendingEvents() {
			Logger.e(TAG, "postPendingEvent ::: ");
			isPostRunning = true;
			lastEventPostTime = System.currentTimeMillis();
		}

		List<Event> postedevents;

		void post(List<Event> campaignEvents) throws Exception {
			List<Event> sublist;
			if (campaignEvents.size() <= MAXSize) {
				if (!isPostRunning) {
					throw new Exception("manualStop");
				}
				if (postEvent(campaignEvents)) {
					postedevents.addAll(campaignEvents);
				}
			} else
				for (int i = 0; i < campaignEvents.size(); i += MAXSize) {
					if (!isPostRunning) {
						throw new Exception("manualStop");
					}
					sublist = campaignEvents.subList(i, Math.min(
							(i + MAXSize - 1), campaignEvents.size() - 1));
					if (postEvent(sublist)) {
						postedevents.addAll(sublist);
					}
				}
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			Logger.e(TAG, "postEvent ::: bg");
			postedevents = new ArrayList<Event>();
			try {
				Logger.e(TAG, "mEventsQueue :" + mEventsQueue.size());
				List<Event> PlayEvents = new ArrayList<Event>();
				List<Event> AppEvents = new ArrayList<Event>();
				List<Event> campaignEvents = new ArrayList<Event>();
				for (Event e : mEventsQueue) {
					if (e instanceof PlayEvent)
						PlayEvents.add(e);
					else if (e instanceof AppEvent)
						AppEvents.add(e);
					else
						campaignEvents.add(e);
				}
				if (PlayEvents.size() > 0) {
					post(PlayEvents);
				}

				if (AppEvents.size() > 0) {
					post(AppEvents);
				}

				if (campaignEvents.size() > 0) {
					post(campaignEvents);
				}

			} catch (Exception e) {
				Logger.printStackTrace(e);
			} catch (Error e) {
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Boolean result) {
			Logger.e(TAG,
					"mEventsQueue onPostExecute before:" + mEventsQueue.size());
			for (Event e : postedevents)
				mEventsQueue.remove(e);

			if (isPostRunning)
				mDataManager.storeEvents(mEventsQueue, true);

			Logger.e("EventTrack onPostExecute",
					"mEventsQueue." + mEventsQueue.size());

			Logger.e(TAG,
					"mEventsQueue onPostExecute Now:" + mEventsQueue.size());
			isPostRunning = false;
			// running ==null means its forsfully closd by app.
			if (running != null && mEventsQueue != null
					&& !mEventsQueue.isEmpty()) {
				handle.post(new Runnable() {
					@Override
					public void run() {
						new PostPendingEvents().execute();
					}
				});
			}
			running = null;
		}

		Handler handle = new Handler();

		private boolean postEvent(List<Event> events) {
			CommunicationManager communicationManager = new CommunicationManager();
			Map<String, Object> result = null;

			String sessionId = mDataManager.getApplicationConfigurations()
					.getSessionID();
			String passkey = mDataManager.getApplicationConfigurations()
					.getPasskey();
			if (sessionId == null
					|| (sessionId != null && (sessionId.length() == 0
							|| sessionId.equalsIgnoreCase("null") || sessionId
								.equalsIgnoreCase("none")))) {
				try {
					if (passkey == null
							|| (passkey != null && (passkey.length() == 0
									|| passkey.equalsIgnoreCase("null") || passkey
										.equalsIgnoreCase("none")))) {

						return false;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					// gets the new session for the user.
					result = communicationManager.performOperation(
							new CMDecoratorOperation(mServerUrl,
									new SessionCreateOperation(mContext)),
							mContext);
				} catch (InvalidRequestException e) {
					e.printStackTrace();
					return false;
				} catch (InvalidResponseDataException e) {
					e.printStackTrace();
					return false;
				} catch (OperationCancelledException e) {
					e.printStackTrace();
					return false;
				} catch (NoConnectivityException e) {
					e.printStackTrace();
					return false;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			result = null;
			try {
				for (Event event : events) {
					if (event instanceof PlayEvent) {
						if (((PlayEvent) event).isFromPlaylist()) {
							try {
								// gets the new session for the user.
								result = communicationManager
										.performOperation(
												new NewMediaDetailsOperation(
														ServerConfigurations
																.getInstance(
																		mContext)
																.getHungamaServerUrl_2(),
														((PlayEvent) event)
																.getId()),
												mContext);
								// communicationManager.performOperation(new
								// NewMediaDetailsOperation(mServerUrl,
								// mEvent.getId()), mContext);
								String title = (String) result
										.get(NewMediaDetailsOperation.RESPONSE_KEY_MEDIA_TITLE);
								if (!TextUtils.isEmpty(title)) {
									((PlayEvent) event).setPlaylistDetails(
											((PlayEvent) event).getId(), title);
								}
							} catch (InvalidRequestException e) {
								e.printStackTrace();
							} catch (InvalidResponseDataException e) {
								e.printStackTrace();
							} catch (OperationCancelledException e) {
								e.printStackTrace();
							} catch (NoConnectivityException e) {
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}

			result = null;
			try {
				Logger.d("Posting event.......", "" + events.size());
				result = communicationManager
						.performOperation(
								new CMDecoratorOperation(mServerUrl,
										new EventMultiCreateOperation(mContext,
												events)), mContext);
			} catch (InvalidRequestException e) {
				e.printStackTrace();
				return false;
			} catch (InvalidResponseDataException e) {
				e.printStackTrace();
				return false;
			} catch (OperationCancelledException e) {
				e.printStackTrace();
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}

			if (result != null) {
				String responseResult = (String) result
						.get(EventMultiCreateOperation.RESULT_KEY_OBJECT);
				if (responseResult
						.equalsIgnoreCase(EventMultiCreateOperation.RESULT_KEY_OBJECT_OK)) {
					return true;
				}
			}
			return false;
		}

	}

}
