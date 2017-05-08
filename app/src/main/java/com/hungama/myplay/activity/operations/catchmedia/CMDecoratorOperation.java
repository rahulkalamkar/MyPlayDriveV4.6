package com.hungama.myplay.activity.operations.catchmedia;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.CommunicationOperation;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.NoConnectivityException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.communication.exceptions.RecreateLoginException;
import com.hungama.myplay.activity.data.ServerCommandsManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.util.Logger;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;

import org.json.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Decorator Operation for adding support of the Json-RPC2 protocol for CM
 * requests / responses.
 */
public class CMDecoratorOperation extends CommunicationOperation {

	private static final String TAG = "CMDecoratorOperation";

	private static final String JSON_RPC2_ID = "jsonrpc";
	/*
	 * Responses from CM servers are build like: "error","result","id"
	 */
	private static final String REPONSE_KEY_RESULT = "result";
	private static final String REPONSE_KEY_ERROR = "error";
	private static final String REPONSE_KEY_ID = "id";
	private static final String REPONSE_KEY_DATA = "data";

	private static final String SERVER_COMMANDS = "server_commands";

	private final String mServerUrl;
	private final CMOperation mCMOperation;

	public CMDecoratorOperation(String serverUrl, CMOperation cmOperation) {
		mServerUrl = serverUrl;
		mCMOperation = cmOperation;
	}

	@Override
	public int getOperationId() {
		return mCMOperation.getOperationId();
	}

	@Override
	public RequestMethod getRequestMethod() {
		return mCMOperation.getRequestMethod();
	}

	@Override
	public String getServiceUrl(final Context context) {
		return (mServerUrl + mCMOperation.getServiceUrl(context));
	}

	@Override
	public String getRequestBody() {

		List<Object> requestBodyMap = new ArrayList<Object>();

		// List<Map<String, Object>> requestBodyMap = new ArrayList<Map<String,
		// Object>>();
		requestBodyMap.add(mCMOperation.getCredentials());

		if (mCMOperation instanceof EventMultiCreateOperation) {
			// ArrayList<Map<String, Object>> all = ((EventMultiCreateOperation)
			// mCMOperation)
			// .getDescriptorAll();
			Map<String, ArrayList<Map<String, Object>>> all = ((EventMultiCreateOperation) mCMOperation)
					.getDescriptorAll();
			// for (Map<String, Object> map : all)
			// requestBodyMap.add(map);
			requestBodyMap.add(all);
		} else
			requestBodyMap.add(mCMOperation.getDescriptor());

		JSONRPC2Request request = new JSONRPC2Request(mCMOperation.getMethod()
				.toString(), requestBodyMap, JSON_RPC2_ID);

		return request.toString();
	}

	@Override
	public Map<String, Object> parseResponse(Response response1)
			throws InvalidResponseDataException,
			InvalidRequestParametersException, InvalidRequestTokenException,
			OperationCancelledException, RecreateLoginException {
		// Runtime.getRuntime().gc();
		// Logger.i(TAG, response);
		String response = null;

		try {
			response = response1.response;
			JSONParser mJSONParser = new JSONParser();

			Map<String, Object> responseRootMap = (Map<String, Object>) mJSONParser
					.parse(response);
			if (!responseRootMap.containsKey(REPONSE_KEY_RESULT)) {
				throw new InvalidResponseDataException("Result is empty.");
			}

			Map<String, Object> resultMap = (Map<String, Object>) responseRootMap
					.get(REPONSE_KEY_RESULT);
			int code = ((Long) resultMap.get(CMOperation.CODE)).intValue();
			String message = (String) resultMap.get(CMOperation.MESSAGE);
			switch (code) {
			case CMOperation.ERROR_CODE_GENERAL:
				throw new InvalidResponseDataException(message);

			case CMOperation.ERROR_CODE_THIRD_PARTY_AUTH_INVALID:
				throw new InvalidResponseDataException(
						"Error: "
								+ Integer
										.toString(CMOperation.ERROR_CODE_THIRD_PARTY_AUTH_INVALID)
								+ " message: " + message);

			case CMOperation.ERROR_CODE_PLAYLIST_WITH_THE_SAME_EXIST:
				throw new InvalidResponseDataException(message);

			case CMOperation.ERROR_CODE_SESSION:
				/*
				 * Performs creation of the session again,
				 */
				CommunicationManager communicationManager = new CommunicationManager();
				try {
					// gets the new session for the user.
					ApplicationConfigurations appConfig = ApplicationConfigurations
							.getInstance(mCMOperation.getContext());
					String passkey = appConfig.getPasskey();
					if (passkey != null
							&& !(passkey.length() == 0
							|| passkey.equalsIgnoreCase("null") || passkey
							.equalsIgnoreCase("none"))) {
						Map<String, Object> result = communicationManager
								.performOperation(new CMDecoratorOperation(
												mServerUrl, new SessionCreateOperation(
												mCMOperation.getContext())),
										mCMOperation.getContext());
					} else {
						throw new InvalidRequestParametersException();
					}
				} catch (InvalidRequestException e) {
					e.printStackTrace();
					Logger.e(TAG, "Failed recreating session id!");
					throw new InvalidRequestParametersException();
				} catch (NoConnectivityException e) {
					e.printStackTrace();
					Logger.e(TAG,
							"Failed recreating session id due to connectivity error!");
					throw new InvalidRequestParametersException();
				}

				/*
				 * if no session was given in response, the
				 * SessionCreateOperation where thrown an exception for that. So
				 * reaching here means that we have got one and it was stored in
				 * the configuration.
				 * 
				 * Recreating the original operation that was failed for that.
				 */
				try {
					Map<String, Object> operationResult = communicationManager
							.performOperation(new CMDecoratorOperation(
									mServerUrl, mCMOperation), mCMOperation
									.getContext());

					return operationResult;

				} catch (InvalidRequestException e) {
					e.printStackTrace();
					Logger.e(TAG, "Failed recalling operation!");
					throw new InvalidRequestParametersException();

				} catch (NoConnectivityException e) {
					e.printStackTrace();
					Logger.e(TAG,
							"Failed recalling operation due to connectivity error!");
					throw new InvalidRequestParametersException();
				}
			case CMOperation.ERROR_CODE_SESSION_RECREATE:
				throw new RecreateLoginException();

			case CMOperation.ERROR_CODE_PASSKEY_INVALID:
				ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations.getInstance(mCMOperation.getContext());
				mApplicationConfigurations.setSessionID(null);
				mApplicationConfigurations.setPasskey(null);
				mCMOperation.getContext().sendBroadcast(new Intent(
						MainActivity.ACTION_LANGUAGE_CHANGED));
				throw new RecreateLoginException();
			}

			if (!resultMap.containsKey(REPONSE_KEY_DATA)) {
				throw new InvalidResponseDataException("Result is empty.");
			}

			if (resultMap.containsKey(SERVER_COMMANDS)) {

				List<Map<String, String>> serverCommands = (List<Map<String, String>>) resultMap
						.get(SERVER_COMMANDS);

				ServerCommandsManager.sendServerCommands(
						mCMOperation.getContext(), serverCommands);
			}

			/*
			 * Some of the web services retrieves the responses as Map and some
			 * as List.
			 */
			Object reslutObject = resultMap.get(REPONSE_KEY_DATA);

			Response res = new Response();

			if (reslutObject instanceof Map) {

				res.response = JSONValue
						.toJSONString((Map<String, Object>) reslutObject);
				return mCMOperation.parseResponse(res);
			} else if (reslutObject instanceof List) {
				res.response = JSONValue
						.toJSONString((List<Map<String, Object>>) reslutObject);
				return mCMOperation.parseResponse(res);
			} else {

				res.response = JSONValue
						.toJSONString((Map<String, Object>) resultMap);
				return mCMOperation.parseResponse(res);

			}
		} catch (RecreateLoginException exception) {
			exception.printStackTrace();
			throw exception;
		} catch (Exception exception) {
			exception.printStackTrace();
			throw new InvalidResponseDataException(exception.getMessage());
		} catch (Error e) {
			// Runtime.getRuntime().gc();
			try {
				response = response1.response;

				JSONObject json = new JSONObject(response);
				JSONObject jsonResult = json.getJSONObject(REPONSE_KEY_RESULT);
				JSONObject jsonData = jsonResult
						.getJSONObject(REPONSE_KEY_DATA);

				Response res = new Response();

				if (!TextUtils.isEmpty(jsonData.toString())) {
					res.response = jsonData.toString();

					return mCMOperation.parseResponse(res);
				} else {
					res.response = jsonResult.toString();

					return mCMOperation.parseResponse(res);
				}
			} catch (Exception exception) {
				exception.printStackTrace();
				throw new InvalidResponseDataException(exception.getMessage());
			} catch (Error e1) {
				throw new InvalidResponseDataException(e1.getMessage());
			}
			// throw new InvalidResponseDataException(e.getMessage());
		}
	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return null;
	}
}
