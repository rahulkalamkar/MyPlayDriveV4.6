package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.CommunicationOperation;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.ContentNotAvailableException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.Video;
import com.hungama.myplay.activity.data.dao.hungama.VideoStreamingResponseCatalog;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

public class VideoStreamingOperationAdp extends HungamaOperation {

	private static final String TAG = "VideoStreamingOperationAdp";

	public static final String RESPONSE_KEY_VIDEO_STREAMING_ADP = "response_key_video_streaming_adp";

	private final String mServerUrl;
	private final String mUserId;
	private final String mContentId;
	private final String mSize;
	private final String mAuthKey;
	private int mNetworkSpeed;
	private String mNetworkType;
	private String mContentFormat;
	private final String mGoogleEmailId;
	private boolean isCaching;

	public VideoStreamingOperationAdp(String serverUrl, String userId,
			String contentId, String size, String authKey, int networkSpeed,
			String networkType, String contentFormat, String googleEmailId,
			boolean isCaching) 
	{
		mServerUrl = serverUrl;
		mUserId = userId;
		mContentId = contentId;
		mSize = size;
		mAuthKey = authKey;
		mNetworkSpeed = networkSpeed;
		mNetworkType = networkType;
		mContentFormat = contentFormat;
		mGoogleEmailId = googleEmailId;
		// if (isCaching && !mNetworkType.toLowerCase().equals("2g") &&
		// (mNetworkSpeed<1 || mNetworkSpeed>96))
		// {
		// mNetworkType = Utils.NETWORK_WIFI;
		// mNetworkSpeed = 2048;
		// mContentFormat = "high";
		// }
		this.isCaching = isCaching;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.VIDEO_STREAMING_ADP;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	int height;
	int width;

	@Override
	public String getServiceUrl(final Context context) {

		StringBuilder url = new StringBuilder();

		// HUNGAPP-589
		// width (value 640 or 720)
		// height (value 480 or 350)
		// width = 640;
		height = 480;
		try {
			// if (HomeActivity.metrics.heightPixels > 640) {
			// width = 720;
			// height = 350;
			// }
			width = HomeActivity.metrics.heightPixels;
			height = HomeActivity.metrics.widthPixels;
		} catch (Exception e) {
		}
		boolean needToUseHls = Utils.isNeedToUseHLS();
		if (isCaching)
			needToUseHls = false;

		DeviceConfigurations config = DeviceConfigurations.getInstance(context);
		url.append(mServerUrl).append(URL_SEGMENT_VIDEO_STREAMING_ADP)
				.append(PARAMS_USER_ID).append("=").append(mUserId).append("&")
				.append(PARAMS_WIDTH).append("=").append(width).append("&")
				.append(PARAMS_HEIGHT).append("=").append(height).append("&")
				.append(PARAMS_CONTENT_ID).append("=").append(mContentId)
				.append("&").append(PARAMS_DEVICE).append("=")
				.append(VALUE_DEVICE).append("&").append(PARAMS_SIZE)
				.append("=").append(mSize).append("&").append(PARAMS_AUTH_KEY)
				.append("=").append(mAuthKey).append("&")
				.append(PARAMS_NETWORK_SPEED).append("=").append(mNetworkSpeed)
				.append("&").append(PARAMS_NETWORK_TYPE).append("=")
				.append(mNetworkType).append("&").append(PARAMS_CONTENT_FORMAT)
				.append("=").append(mContentFormat).append("&")
				.append(PARAMS_DOWNLOAD_HARDWARE_ID).append("=")
				.append(config.getHardwareId()).append("&")
				.append(PARAMS_PROTOCOL).append("=")
				.append(needToUseHls ? "hls" : "filedl").append("&")
				.append(PARAMS_OFFLINE_CACHING).append("=")
				.append(isCaching ? "1" : "0");
		
		if (mGoogleEmailId != null) {
			url.append("&").append(PARAMS_GOOGLE_EMAIL_ID).append("=")
					.append(mGoogleEmailId);
		}
		Logger.i("Video URL", "Video--- URL:" + url.toString());
		return url.toString();
	}

	@Override
	public String getRequestBody() {
		return null;
	}

//	static int temp=1;

	@Override
	public Map<String, Object> parseResponse(Response response)
			throws InvalidResponseDataException,
			InvalidRequestParametersException, InvalidRequestTokenException,
			OperationCancelledException, ContentNotAvailableException {

		Logger.e("videoadp response", response.response);
//		if(temp%2==0)
//			response.responseCode=204;
//		temp++;
		if(response.responseCode== CommunicationManager.RESPONSE_NO_CONTENT_204){
			throw new ContentNotAvailableException();
		}

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		Map<String, Object> resultMap = new HashMap<String, Object>();

		if (Thread.currentThread().isInterrupted()) {
			throw new OperationCancelledException();
		}

		try {

			// response.response = response.response.replace("{\"catalog\":",
			// "");
			// response.response = response.response.substring(0,
			// response.response.length() - 1);

			VideoStreamingResponseCatalog videoStreamingResponseCatalog = (VideoStreamingResponseCatalog) gson
					.fromJson(response.response,
							VideoStreamingResponseCatalog.class);

			Video video = videoStreamingResponseCatalog.getCatalog();
			// (Video) gson.fromJson(response.response, Video.class);
			resultMap.put(RESPONSE_KEY_VIDEO_STREAMING_ADP, video);

			return resultMap;

		} catch (JsonSyntaxException exception) {
			Logger.e(TAG, exception.toString());
			throw new InvalidResponseDataException();
		} catch (JsonParseException exception) {
			Logger.e(TAG, exception.toString());
			throw new InvalidResponseDataException();
		} catch (Exception exception) {
			Logger.e(TAG, exception.toString());
			throw new InvalidResponseDataException();
		}
	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return null;
	}

}
