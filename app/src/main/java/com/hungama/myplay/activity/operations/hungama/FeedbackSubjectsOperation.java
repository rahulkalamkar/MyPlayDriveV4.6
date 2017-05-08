package com.hungama.myplay.activity.operations.hungama;

import android.content.Context;
import android.text.TextUtils;

import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.CacheManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedbackSubjectsOperation extends HungamaOperation {

	private static final String TAG = "FeedbackSubjectsOperation";

	public static final String RESULT_OBJECT_SUBJECTS_LIST = "result_object_subjects_list";

	private static final String KEY_SUBJECT = "subject";
	private static final String KEY_TITLE = "title";
	private final String mServerUrl;
	private final String mUserId;
	private final Context context;
	private final String mHardwareid;
	private final String timestamp_cache;

	// private final String mAuthKey;

	public FeedbackSubjectsOperation(Context context, String serverUrl,
			String hardverid, String userId, String timestamp_cache) {
		mServerUrl = serverUrl;
		this.context = context;
		this.timestamp_cache = timestamp_cache;
		// mAuthKey = authKey;
		this.mHardwareid = hardverid;
		this.mUserId = userId;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.FEEDBACK_SUBJECTS;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		String uri = mServerUrl + URL_SEGMENT_FEEDBACK_SUBJECTS
				+ PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND
				+ PARAMS_DOWNLOAD_HARDWARE_ID + EQUALS + mHardwareid;

		return uri;
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
		List<String> subjects = null;
		JSONParser jsonParser = new JSONParser();
		Logger.i("Testing", "");
		try {

			if (response.responseCode == CommunicationManager.RESPONSE_CONTENT_NOT_MODIFIED_304
					|| response.responseCode == CommunicationManager.RESPONSE_SERVER_ERROR_500
					|| response.responseCode == CommunicationManager.RESPONSE_BAD_REQUEST_400
					|| response.responseCode == CommunicationManager.RESPONSE_FORBIDDEN_403) {
				subjects = new CacheManager(context)
						.getStoredFeedbackSubjects();
			}

			if (subjects == null) {

				Map<String, Object> catalogMap = (Map<String, Object>) jsonParser
						.parse(response.response);
				// gets the "actual" catalog.
				if (!catalogMap.containsKey(KEY_RESPONSE)) {
					throw new InvalidResponseDataException(TAG
							+ " Catalog is missing, not as defined!!!!");
				}

				String lastTimesStamp = null;
				try {
					if (catalogMap.containsKey(KEY_LAST_MODIFIED)) {
						DataManager mDataManager = null;
						mDataManager = DataManager.getInstance(context);
						ApplicationConfigurations mApplicationConfigurations = mDataManager
								.getApplicationConfigurations();
						lastTimesStamp = catalogMap.get(KEY_LAST_MODIFIED)
								.toString();
						mApplicationConfigurations
								.setFeedbackTimeStamp(lastTimesStamp);
						com.hungama.myplay.activity.util.Logger.e(
								"lastTimesStamp 111111 search", lastTimesStamp);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

				catalogMap = (Map<String, Object>) catalogMap.get(KEY_RESPONSE);

				if (TextUtils.isEmpty(response.response)) {
					response.response = "";
				}

				// gets the "actual" subjects.
				if (!catalogMap.containsKey(KEY_SUBJECT)) {
					throw new InvalidResponseDataException(TAG
							+ " Subject is missing, not as defined!!!!");
				}

				List<Map<String, Object>> subjectMapList = (List<Map<String, Object>>) catalogMap
						.get(KEY_SUBJECT);

				if (Utils.isListEmpty(subjectMapList)) {
					throw new InvalidResponseDataException(
							TAG
									+ " Ohh No!!! the List of Subjects is empty!!!! We can't test our new large hadron collider.");
				}

				subjects = new ArrayList<String>();
				String subject = null;

				for (Map<String, Object> subjectMap : subjectMapList) {
					subject = (String) subjectMap.get(KEY_TITLE);
					subjects.add(subject);
				}

				if (response.responseCode == CommunicationManager.RESPONSE_SUCCESS_200
						&& !TextUtils.isEmpty(response.response)) {
					new CacheManager(context).storeFeedbackSubjects(subjects);
				}
			}

			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_OBJECT_SUBJECTS_LIST, subjects);

			return resultMap;

		} catch (ParseException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException();
		}
	}

	@Override
	public String getTimeStampCache() {
		return timestamp_cache;
	}

}
