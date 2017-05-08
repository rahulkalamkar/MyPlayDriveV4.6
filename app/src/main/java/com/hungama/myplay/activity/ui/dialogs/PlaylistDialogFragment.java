package com.hungama.myplay.activity.ui.dialogs;

import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.JsonRPC2Methods;
import com.hungama.myplay.activity.playlist.PlaylistManager;
import com.hungama.myplay.activity.playlist.PlaylistOperation;
import com.hungama.myplay.activity.playlist.PlaylistsAdapter;
import com.hungama.myplay.activity.services.InventoryLightService;
import com.hungama.myplay.activity.ui.listeners.OnLoadMenuItemOptionSelectedListener;
import com.hungama.myplay.activity.ui.widgets.LanguageButton;
import com.hungama.myplay.activity.ui.widgets.LanguageEditText;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.ApsalarEvent;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lets the user select in which playlist he would like to save the given
 * tracks.
 */
public class PlaylistDialogFragment extends DialogFragment implements
		CommunicationOperationListener, OnClickListener, OnItemClickListener {

	private final static String TRACK_LIST = "track_list";
	private final static String FLURRY_SOURCE = "flurry_source";

	public static final String FRAGMENT_TAG = "PlaylistDialogFragment";

	public interface OnPlaylistPerformActionListener {

		public void onCanceled();

		public void onSuccessed();

		public void onFailed();
	}

	// Layout members
	private LanguageTextView title;
	private LanguageButton saveButton;
	private ImageButton closeButton;
	private LanguageEditText playlistEditText;
	private ListView listview;

	private MyProgressDialog mProgressDialog;

	private DataManager mDataManager;
	private PlaylistManager mPlaylistManager;

	private PlaylistsAdapter mAdapter;
	private List<Playlist> list = new ArrayList<Playlist>();

	private List<Track> tracks;
	private static boolean mIsFromLoadMenu;

	private OnPlaylistPerformActionListener mOnPlaylistPerformActionListener = null;

	private OnLoadMenuItemOptionSelectedListener mOnLoadMenuItemOptionSelectedListener = null;

	private String mFlurrySource;

	private String clickedPlaylist;

	private boolean isCancelled = true;

	public static PlaylistDialogFragment newInstance(List<Track> tracks,
			boolean isFromLoadMenu, String flurrySource) {
		PlaylistDialogFragment f = new PlaylistDialogFragment();
		mIsFromLoadMenu = isFromLoadMenu;
		// Supply data input as an argument.
		Bundle args = new Bundle();
		args.putSerializable(TRACK_LIST, new ArrayList<Track>(tracks));
		args.putString(FLURRY_SOURCE, flurrySource);

		f.setArguments(args);

		return f;
	}

	private void setDialogSize() {
		if (getDialog() == null) {
			return;
		}
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getDialog().getWindow().getWindowManager().getDefaultDisplay()
				.getMetrics(displaymetrics);
		int width = (int) (displaymetrics.widthPixels);

		WindowManager.LayoutParams params = getDialog().getWindow()
				.getAttributes();
		params.width = width;
		getDialog().getWindow().setAttributes(params);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE,
				android.support.v7.appcompat.R.style.Theme_AppCompat_Dialog);

		tracks = (List<Track>) getArguments().getSerializable(TRACK_LIST);
		mFlurrySource = (String) getArguments().getSerializable(FLURRY_SOURCE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_playlist_dialog,
				container);
		Utils.traverseChild(view, getActivity());

		saveButton = (LanguageButton) view.findViewById(R.id.save_button);

		playlistEditText = (LanguageEditText) view
				.findViewById(R.id.play_list_edit_text);
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));
		closeButton = (ImageButton) view.findViewById(R.id.close_button);
		closeButton.setOnClickListener(this);
		Button cancel_button = (Button) view.findViewById(R.id.cancel_button);
		cancel_button.setOnClickListener(this);
		listview = (ListView) view.findViewById(R.id.list_view);
		listview.setOnItemClickListener(this);

		title = (LanguageTextView) view
				.findViewById(R.id.download_custom_dialog_title_text);

		if (mIsFromLoadMenu) {
			saveButton.setVisibility(View.GONE);
			playlistEditText.setVisibility(View.GONE);
			title = (LanguageTextView) view
					.findViewById(R.id.download_custom_dialog_title_text);
			title.setText(Utils
					.getMultilanguageText(
							getActivity(),
							getResources()
									.getString(
											R.string.player_load_menu_my_playlist_dialog_title_load)));
		} else {
			saveButton.setVisibility(View.VISIBLE);
			saveButton.setOnClickListener(this);
			title.setText(Utils
					.getMultilanguageText(
							getActivity(),
							getResources()
									.getString(
											R.string.player_load_menu_my_playlist_dialog_title_add)));
			playlistEditText.setVisibility(View.VISIBLE);
		}

		return view;
	}

	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);

		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());

		mPlaylistManager = PlaylistManager.getInstance(getActivity()
				.getApplicationContext());

		// Get all playlists
		Map<Long, Playlist> map = mDataManager.getStoredPlaylists();

		if (map != null) {
			// Convert from Map<Long, Playlist> to List<Itemable>
			for (Map.Entry<Long, Playlist> p : map.entrySet()) {
				list.add(p.getValue());

			}
		}

		if (list != null && list.isEmpty()) {

			Toast emptyListToast = Utils.makeText(getActivity(),
					getString(R.string.you_do_not_have_any_playlist_saved),
					Toast.LENGTH_LONG);

			emptyListToast.setGravity(Gravity.CENTER, 0, 0);

			emptyListToast.show();

			if (mIsFromLoadMenu) {
				this.dismissAllowingStateLoss();
			}
		}

		if (list != null && !list.isEmpty()) {
			Collections.reverse(list);
			mAdapter = new PlaylistsAdapter(getActivity(), list);
			listview.setAdapter(mAdapter);
		} else {
			listview.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.save_button:

			// "SAVE" button is clicked -> then need to create a new Playlist

			// Create
			String newPlaylistName = playlistEditText.getText().toString();

			newPlaylistName = newPlaylistName.trim();

			if (!TextUtils.isEmpty(newPlaylistName)) {
				v.setEnabled(false);
				String tracksStr = tracksStringBuilder(tracks);
				mDataManager.playlistOperation(this, 0, newPlaylistName,
						tracksStr, JsonRPC2Methods.CREATE);
				ApsalarEvent.postEvent(getActivity(), ApsalarEvent.PLAYLIST_CREATED, ApsalarEvent.TYPE_PLAYLIST_CREATED);
			} else {
				Utils.makeText(getActivity(),
						getString(R.string.new_playist_error_alert),
						Toast.LENGTH_LONG).show();

				if (mOnPlaylistPerformActionListener != null) {
					mOnPlaylistPerformActionListener.onFailed();
				}
			}

			break;
		case R.id.cancel_button:
		case R.id.close_button:

			if (mOnPlaylistPerformActionListener != null) {
				mOnPlaylistPerformActionListener.onCanceled();
			}
			dismissAllowingStateLoss();
			break;

		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		try {
			if (mIsFromLoadMenu) {

				Playlist playlist = (Playlist) list.get(position);

				// Get Tracks by Playlist
				// mPlaylistManager.getTracksListByPlaylist(playlist);

				// Add Tracks to queue
				if (mOnLoadMenuItemOptionSelectedListener != null) {
					mOnLoadMenuItemOptionSelectedListener
							.onLoadPlaylistFromDialogSelected(mPlaylistManager
									.getTracksListByPlaylist(playlist));
				}
				dismissAllowingStateLoss();

			} else {

				// list item is clicked -> then need to add the track to the
				// clicked
				// Playlist

				// Update
				Boolean trackAdded = false;
				Playlist playlist = (Playlist) list.get(position);
				trackAdded = playlist.addTracksList(tracks);
				if (trackAdded) {
					clickedPlaylist = playlist.getName();
					mDataManager.playlistOperation(this, playlist.getId(),
							playlist.getName(), playlist.getTrackList(),
							JsonRPC2Methods.UPDATE);
					ApsalarEvent.postEvent(getActivity(), ApsalarEvent.PLAYLIST_CREATED, ApsalarEvent.TYPE_PLAYLIST_SAVED);
				} else {
					Utils.makeText(
							getActivity(),
							getString(R.string.song_already_exists_in_playlist),
							Toast.LENGTH_LONG).show();
				}
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onStart(int operationId) {
		try {
			switch (operationId) {
			case OperationDefinition.CatchMedia.OperationId.PLAYLIST:
				showLoadingDialog(Utils.getMultilanguageTextHindi(
						getActivity(),
						getActivity().getString(R.string.processing)));
				break;

			case OperationDefinition.Hungama.OperationId.MEDIA_DETAILS:
				showLoadingDialog(Utils.getMultilanguageTextHindi(
						getActivity(),
						getActivity().getString(R.string.loading_playlist)));
				break;

			default:
				break;
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
//		Analytics.startSession(getActivity(), this);
		Analytics.onPageView();
		setDialogSize();
	}

	@Override
	public void onStop() {
		super.onStop();

		Analytics.onEndSession(getActivity());
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			switch (operationId) {
			case (OperationDefinition.CatchMedia.OperationId.PLAYLIST):

				Map<String, Object> response = (Map<String, Object>) responseObjects
						.get(PlaylistOperation.RESPONSE_KEY_PLAYLIST);
				JsonRPC2Methods methodType = (JsonRPC2Methods) responseObjects
						.get(PlaylistOperation.RESPONSE_KEY_METHOD_TYPE);

				// Convert from List<Track> to Map<Long, Track>
				Map<Long, Track> map = mDataManager.getStoredTracks();
				if (map == null) {
					map = new HashMap<Long, Track>();
				}

				// Add the Tracks to map
				try {
					for (Track t : tracks) {
						try {
							// System.out.println("t list ::::::::::::: " + new
							// Gson().toJson(t));
							map.put(t.getId(), t);
						} catch (Exception e) {
						}
					}
				} catch (Exception e) {
				}

				// Store the Tracks in cache
				mDataManager.storeTracks(map, null);

				if (methodType != null) {

					if (methodType == JsonRPC2Methods.CREATE) {

						// Badges and Coins
						Object contentId = (Object) response.get("playlist_id");
						String playlistName = (String) response
								.get("playlist_name");

						if (contentId != null) {
							String contentIdStr = String.valueOf(contentId);

							mDataManager
									.checkBadgesAlert(contentIdStr,
											MediaType.PLAYLIST.toString()
													.toLowerCase(),
											"create_playlist", this);
						}

						try {
							Utils.makeText(
									getActivity(),
									getString(R.string.song_s_added_to_your_playlist),
									Toast.LENGTH_LONG).show();
						} catch (Exception e) {
							Logger.e(getClass().getName() + ":364",
									e.toString());
						}

						// Flurry report: create to play list
						Map<String, String> reportMap = new HashMap<String, String>();
						reportMap.put(FlurryConstants.FlurryPlaylists.Source
								.toString(), mFlurrySource);
						reportMap.put(
								FlurryConstants.FlurryPlaylists.PlaylistName
										.toString(), playlistName);
						// reportMap.put(FlurryConstants.FlurryPlaylists.PlaylistStatus.toString(),
						// FlurryConstants.FlurryPlaylists.New.toString());
						Analytics.logEvent(
								FlurryConstants.FlurryPlaylists.PlaylistSaved
										.toString(), reportMap);

						InventoryLightService.callService(getActivity(),
								mDataManager.getApplicationConfigurations()
										.isRealUser());
                        Set<String> tags = Utils.getTags();
                        String tag= Constants.UA_TAG_PLAYLIST_CREATED;
                        if (!tags.contains(tag)) {
                            tags.add(tag);
                            Utils.AddTag(tags);
                        }

					} else if (methodType == JsonRPC2Methods.UPDATE) {
						try {
							Utils.makeText(
									getActivity(),
									getString(R.string.song_s_added_to_your_playlist),
									Toast.LENGTH_LONG).show();

							// Flurry report: add to play list
							Map<String, String> reportMap = new HashMap<String, String>();
							reportMap.put(
									FlurryConstants.FlurryPlaylists.Source
											.toString(), mFlurrySource);
							reportMap
									.put(FlurryConstants.FlurryPlaylists.PlaylistName
											.toString(), clickedPlaylist);
							// reportMap.put(FlurryConstants.FlurryPlaylists.PlaylistStatus.toString(),
							// FlurryConstants.FlurryPlaylists.Existing.toString());
							Analytics
									.logEvent(
											FlurryConstants.FlurryPlaylists.AddToPlaylist
													.toString(), reportMap);
						} catch (Exception e) {
							Logger.e(getClass().getName() + ":386",
									e.toString());
						}
					}
				}

				isCancelled = false;
				dismissAllowingStateLoss();

				if (mOnPlaylistPerformActionListener != null) {
					mOnPlaylistPerformActionListener.onSuccessed();
				}

				break;

			default:
				break;
			}

			hideLoadingDialog();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {

		switch (operationId) {
		case OperationDefinition.CatchMedia.OperationId.PLAYLIST:
			try {
				dismissAllowingStateLoss();

				hideLoadingDialog();
			} catch (Exception e) {
				Logger.e(getClass().getName() + ":418", e.toString());
			}
			if (mOnPlaylistPerformActionListener != null) {
				mOnPlaylistPerformActionListener.onFailed();
			}

			if (getActivity() != null) {
				Toast taost = Toast.makeText(getActivity()
						.getApplicationContext(), errorMessage,
						Toast.LENGTH_LONG);
				taost.setGravity(Gravity.CENTER, 0, 0);
				taost.show();
			}
			break;

		case OperationDefinition.Hungama.OperationId.MEDIA_DETAILS:
			try {
				dismissAllowingStateLoss();
				hideLoadingDialog();
			} catch (Exception e) {
				Logger.e(getClass().getName() + ":436", e.toString());
			}
			break;

		default:
			break;
		}

	}

	public void showLoadingDialog(String message) {
		if (!getActivity().isFinishing()) {
			if (mProgressDialog == null) {
				mProgressDialog = new MyProgressDialog(getActivity());
			}
		}
	}

	public void hideLoadingDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	// Build string of track id's from a list of Track's
	public String tracksStringBuilder(List<Track> tracks) {

		StringBuilder tracksStr = new StringBuilder();
		try {
			for (Track t : tracks) {
				tracksStr.append(t.getId()).append(" ");
			}
		} catch (Exception e) {
			Logger.e(getClass().getName() + ":464", e.toString());
		}
		return tracksStr.toString().trim();
	}

	public void setOnPlaylistPerformActionListener(
			OnPlaylistPerformActionListener listener) {
		mOnPlaylistPerformActionListener = listener;
	}

	public void setOnLoadMenuItemOptionSelectedListener(
			OnLoadMenuItemOptionSelectedListener listener) {
		mOnLoadMenuItemOptionSelectedListener = listener;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);

		if (isCancelled) {
			// Flurry report: add to play list
			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(FlurryConstants.FlurryPlaylists.Source.toString(),
					mFlurrySource);
			Analytics.logEvent(
					FlurryConstants.FlurryPlaylists.CancelledAddToPlayList
							.toString(), reportMap);
		}
	}

}
