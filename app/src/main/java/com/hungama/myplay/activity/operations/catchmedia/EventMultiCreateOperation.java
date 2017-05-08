package com.hungama.myplay.activity.operations.catchmedia;

import android.content.Context;
import android.text.TextUtils;

import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.events.AppEvent;
import com.hungama.myplay.activity.data.events.CampaignPlayEvent;
import com.hungama.myplay.activity.data.events.Event;
import com.hungama.myplay.activity.data.events.PlayEvent;
import com.hungama.myplay.activity.operations.OperationDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Posts user events such as playing music and campaigns events.
 */
public class EventMultiCreateOperation extends CMOperation {

	private static final String TAG = "EventCreateOperation";

	public static final String RESULT_KEY_OBJECT = "result_key_object";
	public static final String RESULT_KEY_OBJECT_OK = "RESULT_KEY_OBJECT_OK";
	public static final String RESULT_KEY_OBJECT_FAIL = "RESULT_KEY_OBJECT_FAIL";

	private static final String RESPONSE_OK = "[]";

	private final List<Event> mEvents;

	public EventMultiCreateOperation(Context context, List<Event> event) {
		super(context);
		mEvents = event;
	}

	@Override
	public JsonRPC2Methods getMethod() {
		return JsonRPC2Methods.CREATE;
	}

	@Override
	protected Map<String, Object> getCredentials() {
		Map<String, Object> params = new HashMap<String, Object>();

		params.put(ServerConfigurations.LC_ID, pServerConfigurations.getLcId());
		params.put(ServerConfigurations.PARTNER_ID,
				pServerConfigurations.getPartnerId());
		params.put(ServerConfigurations.WEB_SERVER_VERSION,
				pServerConfigurations.getWebServiceVersion());
		params.put(ServerConfigurations.APPLICATION_VERSION,
				pServerConfigurations.getAppVersion());
		params.put(ServerConfigurations.APPLICATION_CODE,
				pServerConfigurations.getAppCode());
		params.put(ApplicationConfigurations.SESSION_ID,
				pApplicationConfigurations.getSessionID());
		params.put(DeviceConfigurations.TIMESTAMP,
				pDeviceConfigurations.getTimeStampDelta());

		return params;
	}

	@Override
	public Map<String, Object> getDescriptor() {
		Map<String, Object> params = new HashMap<String, Object>();
		Event mEvent = mEvents.get(0);
		if (mEvent instanceof PlayEvent) {

			PlayEvent event = (PlayEvent) mEvent;
			params.put("consumer_id", event.getConsumerId());
			params.put("device_id", event.getDeviceId());
			params.put("media_id", event.getMediaId());
			params.put("media_kind", event.getMediaKind());
			params.put("timestamp", event.getTimestamp());
			params.put("playing_source_type", event.getPlayingSourceType()
					.toString().toLowerCase());
			params.put("complete_play", "" + event.isCompletePlay());
			params.put("duration", event.getDuration());
			params.put("start_position", event.getStartPosition());
			params.put("stop_position", event.getStopPosition());
			params.put("delivery_id", event.getDeliveryId());
			params.put(ApplicationConfigurations.KEY_MEDIA_ID_NS,
					ApplicationConfigurations.VALUE_MEDIA_ID_NS);

			if (event.isFromPlaylist()) {
				Map<String, Object> paramsExtra = new HashMap<String, Object>();
				paramsExtra.put("playlist_id", event.getId());
				paramsExtra.put("playlist_name", event.getName());
				if (event.getAlbum_id() != 0)
					paramsExtra.put("album_id", event.getAlbum_id());
				params.put("extra_data", paramsExtra);
			} else if (event.isFromArtistRadio()) {
				Map<String, Object> paramsExtra = new HashMap<String, Object>();
				paramsExtra.put("artist_id", event.getId());
				if (event.getAlbum_id() != 0)
					paramsExtra.put("album_id", event.getAlbum_id());
				params.put("extra_data", paramsExtra);
			} else if (event.isFromOnDemandRadio()) {
				Map<String, Object> paramsExtra = new HashMap<String, Object>();
				paramsExtra.put("on_demand_radio_id", event.getId());
				if (event.getAlbum_id() != 0)
					paramsExtra.put("album_id", event.getAlbum_id());
                if (!TextUtils.isEmpty(event.getOndemandname()))
                    paramsExtra.put("on_demand_radio_name", event.getOndemandname());
                params.put("extra_data", paramsExtra);
			} else if (event.isFromLiveRadio()) {
				Map<String, Object> paramsExtra = new HashMap<String, Object>();
				paramsExtra.put("channel_index", event.getId());
				paramsExtra.put("channel_name", event.getName());
				if (event.getAlbum_id() != 0)
					paramsExtra.put("album_id", event.getAlbum_id());
				params.put("extra_data", paramsExtra);
			} else if (event.isDiscovery()) {
				Map<String, Object> paramsExtra = new HashMap<String, Object>();
				if (!TextUtils.isEmpty(event.getHashTag())) {
					paramsExtra.put("type", "Discover_hashtag");
					paramsExtra.put("name", event.getHashTag());
				} else {
					paramsExtra.put("type", "Discover");
				}
				if (event.getAlbum_id() != 0)
					paramsExtra.put("album_id", event.getAlbum_id());
				params.put("extra_data", paramsExtra);
			} else if (event.getAlbum_id() != 0) {
				Map<String, Object> paramsExtra = new HashMap<String, Object>();
				paramsExtra.put("album_id", event.getAlbum_id());
				if (!TextUtils.isEmpty(event.getUserPlaylistName())) {
					paramsExtra.put("user_playlist_name",
							event.getUserPlaylistName());
				}
				params.put("extra_data", paramsExtra);
			} else if (!TextUtils.isEmpty(event.getUserPlaylistName())) {
				Map<String, Object> paramsExtra = new HashMap<String, Object>();
				paramsExtra.put("user_playlist_name",
						event.getUserPlaylistName());
				if (event.getAlbum_id() != 0)
					paramsExtra.put("album_id", event.getAlbum_id());
				params.put("extra_data", paramsExtra);
			}

			if(event.getPlayingSourceType() == PlayEvent.PlayingSourceType.CAST){
				event.setPlayingSourceType(PlayEvent.PlayingSourceType.STREAM);
				Map<String, Object> paramsExtra;
				params.put("playing_source_type", event.getPlayingSourceType().toString().toLowerCase());
				if(params.containsKey("extra_data")){
					paramsExtra = (Map<String, Object>) params.get("extra_data");
				}else{
					paramsExtra = new HashMap<>();
				}
				paramsExtra.put("player_type","chromecast");
				params.put("extra_data", paramsExtra);
			}
		} else if (mEvent instanceof AppEvent) {
			AppEvent event = (AppEvent) mEvent;
			params.put("event_type", event.getEventType());
			params.put("event_time", event.getTimestamp());

			if(event.getExtraData()!=null && event.getExtraData().size()>0) {
				params.put("extra_event_data", event.getExtraData());
			}
		} else {
			CampaignPlayEvent event = (CampaignPlayEvent) mEvent;
			params.put("consumer_id", event.getConsumerId());
			params.put("device_id", event.getDeviceId());
			params.put("campaign_media_id", event.getCampaignMediaId());
			params.put("campaign_id", event.getCampaignId());
			params.put("timestamp", event.getTimestamp());
			params.put("complete_play", "" + event.isCompletePlay());
			params.put("duration", event.getDuration());
			params.put("latitude", event.getLatitude());
			params.put("longitude", event.getLongitude());
			params.put("play_type", event.getPlayType());
		}

		return params;
	}

	public Map<String, ArrayList<Map<String, Object>>> getDescriptorAll() {
		Map<String, Object> params;
		ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (Event mEvent : mEvents) {
			params = new HashMap<String, Object>();
			list.add(params);
			if (mEvent instanceof PlayEvent) {

				PlayEvent event = (PlayEvent) mEvent;
				params.put("consumer_id", event.getConsumerId());
				params.put("device_id", event.getDeviceId());
				params.put("media_id", event.getMediaId());
				params.put("media_kind", event.getMediaKind());
				params.put("timestamp", event.getTimestamp());
				params.put("playing_source_type", event.getPlayingSourceType()
						.toString().toLowerCase());
				params.put("complete_play", "" + event.isCompletePlay());
				params.put("duration", event.getDuration());
				params.put("start_position", event.getStartPosition());
				params.put("stop_position", event.getStopPosition());
				params.put("delivery_id", event.getDeliveryId());
				params.put(ApplicationConfigurations.KEY_MEDIA_ID_NS,
						ApplicationConfigurations.VALUE_MEDIA_ID_NS);

				if (event.isFromPlaylist()) {
					Map<String, Object> paramsExtra = new HashMap<String, Object>();
					paramsExtra.put("playlist_id", event.getId());
					paramsExtra.put("playlist_name", event.getName());
					if (event.getAlbum_id() != 0)
						paramsExtra.put("album_id", event.getAlbum_id());
					params.put("extra_data", paramsExtra);
				} else if (event.isFromArtistRadio()) {
					Map<String, Object> paramsExtra = new HashMap<String, Object>();
					paramsExtra.put("artist_id", event.getId());
					if (event.getAlbum_id() != 0)
						paramsExtra.put("album_id", event.getAlbum_id());
					params.put("extra_data", paramsExtra);
				} else if (event.isFromOnDemandRadio()) {
                    Map<String, Object> paramsExtra = new HashMap<String, Object>();
                    paramsExtra.put("on_demand_radio_id", event.getId());
                    if (event.getAlbum_id() != 0)
                        paramsExtra.put("album_id", event.getAlbum_id());
                    if (!TextUtils.isEmpty(event.getOndemandname()))
                        paramsExtra.put("on_demand_radio_name", event.getOndemandname());
                    params.put("extra_data", paramsExtra);
				} else if (event.isFromLiveRadio()) {
					Map<String, Object> paramsExtra = new HashMap<String, Object>();
					paramsExtra.put("channel_index", event.getId());
					paramsExtra.put("channel_name", event.getName());
					if (event.getAlbum_id() != 0)
						paramsExtra.put("album_id", event.getAlbum_id());
					params.put("extra_data", paramsExtra);
				} else if (event.isDiscovery()) {
					Map<String, Object> paramsExtra = new HashMap<String, Object>();
					if (!TextUtils.isEmpty(event.getHashTag())) {
						paramsExtra.put("type", "Discover_hashtag");
						paramsExtra.put("name", event.getHashTag());
					} else {
						paramsExtra.put("type", "Discover");
					}
					if (event.getAlbum_id() != 0)
						paramsExtra.put("album_id", event.getAlbum_id());
					params.put("extra_data", paramsExtra);
				} else if (event.getAlbum_id() != 0) {
					Map<String, Object> paramsExtra = new HashMap<String, Object>();
					paramsExtra.put("album_id", event.getAlbum_id());
					if (!TextUtils.isEmpty(event.getUserPlaylistName())) {
						paramsExtra.put("user_playlist_name",
								event.getUserPlaylistName());
					}
					params.put("extra_data", paramsExtra);
				} else if (!TextUtils.isEmpty(event.getUserPlaylistName())) {
					Map<String, Object> paramsExtra = new HashMap<String, Object>();
					paramsExtra.put("user_playlist_name",
							event.getUserPlaylistName());
					if (event.getAlbum_id() != 0)
						paramsExtra.put("album_id", event.getAlbum_id());
					params.put("extra_data", paramsExtra);
				}
				if(event.getPlayingSourceType() == PlayEvent.PlayingSourceType.CAST){
					event.setPlayingSourceType(PlayEvent.PlayingSourceType.STREAM);
					Map<String, Object> paramsExtra;
					params.put("playing_source_type", event.getPlayingSourceType().toString().toLowerCase());
					if(params.containsKey("extra_data")){
						paramsExtra = (Map<String, Object>) params.get("extra_data");
					}else{
						paramsExtra = new HashMap<>();
					}
					paramsExtra.put("player_type","chromecast");
					params.put("extra_data", paramsExtra);
				}
			} else if (mEvent instanceof AppEvent) {
				AppEvent event = (AppEvent) mEvent;
				params.put("event_type", event.getEventType());
				params.put("event_time", event.getTimestamp());

				if(event.getExtraData()!=null && event.getExtraData().size()>0) {
					params.put("extra_event_data", event.getExtraData());
				}
			} else {
				CampaignPlayEvent event = (CampaignPlayEvent) mEvent;
				params.put("consumer_id", event.getConsumerId());
				params.put("device_id", event.getDeviceId());
				params.put("campaign_media_id", event.getCampaignMediaId());
				params.put("campaign_id", event.getCampaignId());
				params.put("timestamp", event.getTimestamp());
				params.put("complete_play", "" + event.isCompletePlay());
				params.put("duration", event.getDuration());
				params.put("latitude", event.getLatitude());
				params.put("longitude", event.getLongitude());
				params.put("play_type", event.getPlayType());
			}
		}
		Map<String, ArrayList<Map<String, Object>>> map = new HashMap<String, ArrayList<Map<String, Object>>>();
		map.put("event_data_arr", list);
		return map;
	}

	@Override
	public int getOperationId() {
		if (mEvents.get(0) instanceof PlayEvent) {
			return OperationDefinition.CatchMedia.OperationId.PLAY_EVENT_CREATE;
		} else if (mEvents.get(0) instanceof AppEvent) {
			return OperationDefinition.CatchMedia.OperationId.APP_EVENT_CREATE;
		} else {
			return OperationDefinition.CatchMedia.OperationId.CAMPAIGN_PLAY_EVENT_CREATE;
		}
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.POST;
	}

	@Override
	public String getServiceUrl(final Context context) {
		if (mEvents.get(0) instanceof PlayEvent) {
			return OperationDefinition.CatchMedia.ServiceName.PLAY_EVENT_CRERATE;
		} else if (mEvents.get(0) instanceof AppEvent) {
			return OperationDefinition.CatchMedia.ServiceName.APP_EVENT_CRERATE;
		} else {
			return OperationDefinition.CatchMedia.ServiceName.CAMPAIGN_PLAY_EVENT_CRERATE;
		}
	}

	@Override
	public String getRequestBody() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(Response response)
			throws InvalidResponseDataException,
			InvalidRequestParametersException, InvalidRequestTokenException,
			OperationCancelledException {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {

			if (response.response.equalsIgnoreCase(RESPONSE_OK)) {
				resultMap.put(RESULT_KEY_OBJECT, RESULT_KEY_OBJECT_OK);
			} else {
				resultMap.put(RESULT_KEY_OBJECT, RESULT_KEY_OBJECT_FAIL);
			}
		} catch (Exception e) {
			resultMap.put(RESULT_KEY_OBJECT, RESULT_KEY_OBJECT_FAIL);

		}

		return resultMap;
	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return null;
	}

}
