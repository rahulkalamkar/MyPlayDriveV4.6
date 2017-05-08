package com.hungama.myplay.activity.operations.hungama;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.CacheManager;
import com.hungama.myplay.activity.data.CacheManager.Callback;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.LeftMenuItem;
import com.hungama.myplay.activity.data.dao.hungama.LeftMenuResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.ui.fragments.GlobalMenuFragment;
import com.hungama.myplay.activity.util.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeftMenuOperation extends HungamaOperation {

	private static final String TAG = "LeftMenuOperation";

	public static final String RESULT_KEY_LEFT_MENU_LIST = "result_key_left_men_list";
	// public static final String CATALOG = "response";

	private final String mServerUrl;

	private final String mUserId;
	private final Context context;
	private final String timestamp_cache;

	public LeftMenuOperation(Context context, String serverUrl, String userId,
			String timestamp_cache) {
		this.context = context;
		mServerUrl = serverUrl;// "http://cdnapi.hungama.com/myplay2/v2/";
		this.timestamp_cache = timestamp_cache;
		mUserId = userId;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.LET_MENU;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	long diffTime;

	@Override
	public String getServiceUrl(final Context context) {

		DeviceConfigurations config = DeviceConfigurations.getInstance(context);
		String finalurl = "";

		finalurl = mServerUrl + URL_SEGMENT_LEFT_MENU + PARAMS_USER_ID + "="
				+ mUserId + HungamaOperation.AMPERSAND
				+ PARAMS_DOWNLOAD_HARDWARE_ID + HungamaOperation.EQUALS
				+ config.getHardwareId();

		diffTime = System.currentTimeMillis();
        Logger.writetofile(TAG, new Date().toString() + " REQUEST : "
				+ finalurl);
		return finalurl;
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
        Logger.writetofile(TAG, new Date().toString() + "  Dureation : "
				+ (System.currentTimeMillis() - diffTime) + " RESPONSE : "
				+ response);
		Map<String, Object> resultMap = new HashMap<String, Object>();

		if (Thread.currentThread().isInterrupted()) {
			throw new OperationCancelledException();
		}

		Gson gsonParser = new Gson();

		try {
			if (response.responseCode == CommunicationManager.RESPONSE_CONTENT_NOT_MODIFIED_304
					|| response.responseCode == CommunicationManager.RESPONSE_SERVER_ERROR_500
					|| response.responseCode == CommunicationManager.RESPONSE_BAD_REQUEST_400
					|| response.responseCode == CommunicationManager.RESPONSE_FORBIDDEN_403) {
				response.response = new CacheManager(context)
						.getLeftMenuResponse();
			}

			if (TextUtils.isEmpty(response.response)) {
				response.response = "";
			}

			LeftMenuResponse mLeftMenuResponse = gsonParser.fromJson(
					response.response, LeftMenuResponse.class);

			DataManager mDataManager;
			mDataManager = DataManager.getInstance(context);
			ApplicationConfigurations mApplicationConfigurations = mDataManager
					.getApplicationConfigurations();
			mApplicationConfigurations.setLeftMenuTimeStamp(""
					+ mLeftMenuResponse.getLast_modified());

			try{
				List<LeftMenuItem> menu = mLeftMenuResponse.getLeftMenuItems();
				for (int i = 1; i < menu.size(); i++) {
					LeftMenuItem temp_meu = menu.get(i);
					if (!TextUtils.isEmpty(temp_meu.getMenu_title()) &&
							temp_meu.getMenu_title().equalsIgnoreCase(GlobalMenuFragment.MENU_ITEM_REWARDS_ACTION)){
						mApplicationConfigurations.setRedeemUrl(temp_meu.getHtmlURL());
						break;
					}
				}
			} catch (Exception e){
				Logger.printStackTrace(e);
			}

			Logger.e("mLeftMenuResponse.getLast_modified() 111111", ""
					+ mLeftMenuResponse.getLast_modified());

			resultMap.put(RESULT_KEY_LEFT_MENU_LIST, mLeftMenuResponse);

			Callback callback = new Callback() {
				@Override
				public void onResult(Boolean gotResponse) {

				}
			};

			if (response.responseCode == CommunicationManager.RESPONSE_SUCCESS_200
					&& !TextUtils.isEmpty(response.response))
				new CacheManager(context).storeLeftMenuResponse(
						response.response, callback);

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
		return timestamp_cache;
	}
}