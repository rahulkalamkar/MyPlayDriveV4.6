package com.hungama.myplay.activity.operations.hungama;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;

import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.dao.hungama.Mood;
import com.hungama.myplay.activity.operations.OperationDefinition;

public class DiscoverOptionsOperation extends HungamaOperation {

	public static final String RESULT_KEY_OBJECT_MOODS = "result_key_object_moods";

	public static final String KEY_IMAGE_BIG = "image_big";
	public static final String KEY_IMAGE_SMALL = "image_small";

	private final String mServerUrl;
	// private final String mAuthKey;
	private final String mUserId;
	private final String mHardwareID;

	public DiscoverOptionsOperation(String serverUrl, String userId,
			String hardwareID) {
		this.mServerUrl = serverUrl;
		// this.mAuthKey = authKey;
		this.mUserId = userId;
		this.mHardwareID = hardwareID;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.DISCOVER_OPTIONS;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		// String serviceUrl = mServerUrl + URL_SEGMENT_DISCOVER_OPTIONS
		// + PARAMS_DEVICE + "=" + VALUE_DEVICE + "&"
		// + PARAMS_AUTH_KEY + "=" + mAuthKey;

		String serviceUrl = mServerUrl + URL_SEGMENT_DISCOVER_OPTIONS
				+ PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND
				+ PARAMS_DOWNLOAD_HARDWARE_ID + EQUALS + mHardwareID;

		return serviceUrl;
	}

	@Override
	public String getRequestBody() {
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(Response response)
			throws InvalidResponseDataException,
			InvalidRequestParametersException, InvalidRequestTokenException,
			OperationCancelledException {

		JSONParser jsonParser = new JSONParser();

		try {
			Map<String, Object> reponseMap = (Map<String, Object>) jsonParser
					.parse(response.response);

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

			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_OBJECT_MOODS, moods);

			return resultMap;

		} catch (ParseException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException();
		}
	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return null;
	}

}
