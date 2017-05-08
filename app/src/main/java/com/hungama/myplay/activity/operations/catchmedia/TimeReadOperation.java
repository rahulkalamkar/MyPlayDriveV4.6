package com.hungama.myplay.activity.operations.catchmedia;

import android.content.Context;

import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Retrieves device ID from CM servers, based on the device's properties.
 */
public class TimeReadOperation extends CMOperation {

	private static final String TAG = "TimeReadOperation";

	// public static final String RESPONSE_KEY_OBJECT_TIME_READ_OPERATION =
	// "response_key_object_time_read_operation";
	// public static final String KEY_RESULT = "result";
	// public static final int VALUE_SUCCESS = 200;
	public static final String KEY_DATA = "data";

	Context mContext;

	public TimeReadOperation(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public JsonRPC2Methods getMethod() {
		return JsonRPC2Methods.READ;
	}

	@Override
	protected Map<String, Object> getCredentials() {

		Map<String, Object> credentials = new HashMap<String, Object>();

		credentials.put(ServerConfigurations.APPLICATION_CODE,
				pServerConfigurations.getAppCode());
		credentials.put(ServerConfigurations.APPLICATION_VERSION,
				pServerConfigurations.getAppVersion());
		credentials.put(DeviceConfigurations.HARDWARE_ID,
				pDeviceConfigurations.getHardwareId());
		credentials.put(ServerConfigurations.LC_ID,
				pServerConfigurations.getLcId());
		credentials.put(ServerConfigurations.PARTNER_ID,
				pServerConfigurations.getPartnerId());
		credentials.put(ApplicationConfigurations.SESSION_ID,
				pApplicationConfigurations.getSessionID());
		credentials.put(DeviceConfigurations.TIMESTAMP,
				pDeviceConfigurations.getTimeStampDelta());
		credentials.put(ServerConfigurations.WEB_SERVER_VERSION,
				pServerConfigurations.getWebServiceVersion());

		return credentials;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.CatchMedia.OperationId.TIME_READ;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.POST;
	}

	@Override
	public String getServiceUrl(final Context context) {
		return OperationDefinition.CatchMedia.ServiceName.TIME;
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
		Map<String, Object> responseMap = new HashMap<String, Object>();

		try {

			Logger.i(TAG, "################################" + response);
			Map<String, Object> timeReadMap = (Map<String, Object>) jsonParser
					.parse(response.response);

			if (timeReadMap.containsKey(KEY_DATA)) {
				String data = (String) timeReadMap.get(KEY_DATA);
				// xtpl
				DeviceConfigurations config = DeviceConfigurations
						.getInstance(mContext);

				Date dateCM = null;
				try {

					Logger.d(TAG, "dateCM : " + data);
					dateCM = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",
							Locale.ENGLISH).parse(data);
				} catch (Exception e) {
					e.printStackTrace();
				}// Changes

				Date dateCurr = null;
				try {
					Logger.d(TAG, "dateCurr : " + config.getTimeStamp());
					dateCurr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",
							Locale.ENGLISH).parse(config.getTimeStamp());
				} catch (Exception e) {
					e.printStackTrace();
				} // by

				long delataInMillis = dateCM.getTime() - dateCurr.getTime();
				Logger.s("&&&&&&&&&&&&Diff" + delataInMillis + "  "
						+ new Date(delataInMillis + dateCurr.getTime()));
				pApplicationConfigurations.setTimeReadDelta(delataInMillis);
			}

		} catch (ParseException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException("Device map parsing error.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return responseMap;
	}

	@Override
	public Map<String, Object> getDescriptor() {

		Map<String, Object> descriptorMap = new HashMap<String, Object>();
		descriptorMap.put(ApplicationConfigurations.LOCALE_TIME,
				pDeviceConfigurations.getTimeStampDelta());

		return descriptorMap;
	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return null;
	}

}
