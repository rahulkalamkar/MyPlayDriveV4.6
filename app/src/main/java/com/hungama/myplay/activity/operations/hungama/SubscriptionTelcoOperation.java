package com.hungama.myplay.activity.operations.hungama;

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
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SubscriptionTelcoOperation extends HungamaOperation {

	private static final String TAG = "SubscriptionTelcoOperation";

	public static final String RESPONSE_KEY_MSISDN = "response_key_msisdn";
	public static final String RESPONSE_KEY_IMSI = "response_key_imsi";

	private final Context mContext;
	private final String mServerUrl;

	public SubscriptionTelcoOperation(Context context, String serverUrl) {
		mContext = context;
		mServerUrl = serverUrl;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SUBSCRIPTION_TELCO_API;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.POST;
	}

	@Override
	public String getServiceUrl(final Context context) {
		return mServerUrl;
	}

	@Override
	public String getRequestBody() {
		return "";
	}

	@Override
	public Map<String, Object> parseResponse(Response response)
			throws InvalidResponseDataException,
			InvalidRequestParametersException, InvalidRequestTokenException,
			OperationCancelledException {

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Logger.s("Check subscription :::::::: " + response);
		if (Thread.currentThread().isInterrupted()) {
			throw new OperationCancelledException();
		}
		try {
			JSONObject json = new JSONObject(response.response);
			JSONObject jsonReponse = json.getJSONObject("response");
			String msisdn = jsonReponse.getString("msisdn");
			String imsi = jsonReponse.getString("imsi");
			resultMap.put(RESPONSE_KEY_MSISDN, msisdn);
			resultMap.put(RESPONSE_KEY_IMSI, imsi);
		} catch (JsonSyntaxException exception) {
			exception.printStackTrace();
			throw new InvalidResponseDataException();
		} catch (JsonParseException exception) {
			exception.printStackTrace();
			throw new InvalidResponseDataException();
		} catch (StringIndexOutOfBoundsException exception) {
			exception.printStackTrace();
		} catch (Exception e) {
			Logger.printStackTrace(e);
			throw new InvalidResponseDataException();
		}

		return resultMap;
	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return null;
	}

}
