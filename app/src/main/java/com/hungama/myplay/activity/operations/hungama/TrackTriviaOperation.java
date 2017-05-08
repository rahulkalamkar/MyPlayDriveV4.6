package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.JSONParser;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.TrackTrivia;
import com.hungama.myplay.activity.operations.OperationDefinition;

public class TrackTriviaOperation extends HungamaOperation {

	private static final String TAG = "TrackTriviaOperation";

	public static final String RESULT_KEY_OBJECT_TRACK_TRIVIA = "result_key_object_track_trivia";

	private final String mServerUrl;
	private final String mAuthkey;
	private final Track mTrack;
	private final String mUserId;

	public TrackTriviaOperation(String serverUrl, String authkey, Track track,
			String userId) {
		this.mServerUrl = serverUrl;
		this.mAuthkey = authkey;
		this.mTrack = track;
		mUserId = userId;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.TRACK_TRIVIA;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		DeviceConfigurations config = DeviceConfigurations.getInstance(context);
		String content_id = "";
		try {
			content_id = Long.toString(mTrack.getId());
		} catch (Exception e) {
		}

		String serviceUrl = mServerUrl + URL_SEGMENT_CONTENT
				+ URL_SEGMENT_MUSIC + URL_SEGMENT_TRIVIA + "?" + PARAMS_USER_ID
				+ "=" + mUserId + HungamaOperation.AMPERSAND
				+ PARAMS_DOWNLOAD_HARDWARE_ID + HungamaOperation.EQUALS
				+ config.getHardwareId() + HungamaOperation.AMPERSAND
				+ PARAMS_CONTENT_ID + HungamaOperation.EQUALS + content_id;

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

		// removes the: {"content": string and the last } .
		try {
			JSONParser parser = new JSONParser();

			String reponseString = ""; /*
										 * response.response.substring(11,
										 * response.response.length() - 1);
										 */

			Map<String, Object> catalogMap = (Map<String, Object>) parser
					.parse(response.response);

			if (catalogMap.containsKey(KEY_RESPONSE)) {
				reponseString = catalogMap.get(KEY_RESPONSE).toString();
			} else {
				throw new InvalidResponseDataException(
						"Parsing error - no catalog available");
			}

			Map<String, Object> catalogContent = (Map<String, Object>) parser
					.parse(reponseString);
			if (catalogContent.containsKey(KEY_CONTENT)) {
				reponseString = catalogContent.get(KEY_CONTENT).toString();
			} else {
				throw new InvalidResponseDataException(
						"Parsing error - no catalog available");
			}

			Gson gson = new GsonBuilder().create();
			TrackTrivia trackTrivia = gson.fromJson(reponseString,
					TrackTrivia.class);

			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_OBJECT_TRACK_TRIVIA, trackTrivia);

			return resultMap;

		} catch (JsonSyntaxException exception) {
			throw new InvalidResponseDataException();

		} catch (JsonParseException exception) {
			throw new InvalidResponseDataException();
		} catch (Exception exception) {
			throw new InvalidResponseDataException();
		}

	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return null;
	}

}
