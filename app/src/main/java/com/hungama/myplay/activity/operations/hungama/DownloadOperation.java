package com.hungama.myplay.activity.operations.hungama;

import android.content.Context;
import android.text.TextUtils;

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
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.DownloadOperationType;
import com.hungama.myplay.activity.data.dao.hungama.DownloadResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

import java.util.HashMap;
import java.util.Map;

public class DownloadOperation extends HungamaOperation {

	private static final String TAG = "DownloadOperation";

	public static final String RESPONSE_KEY_DOWNLOAD = "response_key_download";

	private static final String CONTENT_TYPE = "content_type";

	private final String mServerUrl;
	private final String mUserId;
	private final String mPlanId;
	private final String mContentId;
	private final String mContentType; // (audio / video)
	private final String mDevice;
	private final String mSize;
	private final String mAuthKey;
	private final String mMsisdn;
	private final String transactionSession;
	private final String mGoogleEmailId;
	private final String mAffCode;
	private final DownloadOperationType mDownloadType;
	private final String mAlbumId;

	String urlParams = null;

	public DownloadOperation(String serverUrl, String userId, String msisdn,
			String planId, String contentId, String contentType, String device,
			String size, DownloadOperationType downloadOperationType,
			String authKey, String transactionSession, String googleEmailId, String affCode, String mAlbumId) {
		mServerUrl = serverUrl;
		mUserId = userId;
		mPlanId = planId;
		mContentId = contentId;
		mContentType = contentType;
		mDevice = device;
		mSize = size;
		mAuthKey = authKey;
		mDownloadType = downloadOperationType;
		if (msisdn == null) {
			mMsisdn = "";
		} else {
			mMsisdn = msisdn;
		}
		this.transactionSession = transactionSession;
		mGoogleEmailId = googleEmailId;
		mAffCode = affCode;
		this.mAlbumId = mAlbumId;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.DOWNLOAD;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.POST;
	}

	@Override
	public String getServiceUrl(final Context context) {
		String segmentType = null;
		DeviceConfigurations config = DeviceConfigurations.getInstance(context);
		String HardID = config.getHardwareId();
		ServerConfigurations Sconfig = ServerConfigurations
				.getInstance(context);
		String affiliate_id = Sconfig.getReferralId();
		if (mDownloadType == DownloadOperationType.CONTENT_DELIVERY) {
			urlParams = PARAMS_IDENTITY + HungamaOperation.EQUALS + mUserId
					+ AMPERSAND + PARAMS_PRODUCT + EQUALS + VALUE_PRODUCT
					+ AMPERSAND + PARAMS_PLATFORM + EQUALS + VALUE_DEVICE
					+ AMPERSAND + PARAMS_CONTENT_ID + EQUALS + mContentId
					+ AMPERSAND + PARAMS_GOOGLE_EMAIL_ID + EQUALS + mGoogleEmailId
					+ AMPERSAND + PARAMS_DOWNLOAD_HARDWARE_ID + EQUALS + HardID
					+ AMPERSAND + PARAMS_AFF_CODE + EQUALS + mAffCode;

			if(!TextUtils.isEmpty(mAlbumId) && !mAlbumId.equals("0"))
				urlParams += AMPERSAND + PARAMS_ALBUM_ID + EQUALS + mAlbumId;

			return mServerUrl + HungamaOperation.URL_SEGMENT_CONTENT_DELIVERY;
		}

		return mServerUrl + segmentType;
	}

	@Override
	public String getRequestBody() {
		return urlParams;
	}

	@Override
	public Map<String, Object> parseResponse(Response response)
			throws InvalidResponseDataException,
			InvalidRequestParametersException, InvalidRequestTokenException,
			OperationCancelledException {

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		Map<String, Object> resultMap = new HashMap<String, Object>();

		if (Thread.currentThread().isInterrupted()) {
			throw new OperationCancelledException();
		}

//		response.response = "{\"response\": {\"url\":" +
//				"\"http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4\", \"message\":\"success\"}}";
		try {
			response.response = response.response.replace("{\"response\":", "");
			response.response = response.response.substring(0,
					response.response.length() - 1);

			Logger.i(TAG, response.response);

			DownloadResponse downloadResponse = (DownloadResponse) gson
					.fromJson(response.response, DownloadResponse.class);
			downloadResponse.setDownloadType(mDownloadType);
			resultMap.put(RESPONSE_KEY_DOWNLOAD, downloadResponse);
//			}
		} catch (JsonSyntaxException exception) {
			throw new InvalidResponseDataException();

		} catch (JsonParseException exception) {
			throw new InvalidResponseDataException();
		} catch (IndexOutOfBoundsException e) {
			throw new InvalidResponseDataException();
		} catch (Exception e) {
			Logger.printStackTrace(e);
			throw new InvalidResponseDataException();
		}

		return resultMap;
	}

	public DownloadOperationType getDownloadOperation() {
		return mDownloadType;
	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return null;
	}
}
