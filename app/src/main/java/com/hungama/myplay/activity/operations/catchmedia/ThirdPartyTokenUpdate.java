package com.hungama.myplay.activity.operations.catchmedia;

import android.content.Context;

import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.operations.OperationDefinition;

import java.util.HashMap;
import java.util.Map;

public class ThirdPartyTokenUpdate extends CMOperation {

	private static final String TAG = "ThirdPartyTokenUpdate";

	Context mContext;
	String token;

	public ThirdPartyTokenUpdate(Context context, String token) {
		super(context);
		mContext = context;
		this.token = token;
	}

	@Override
	public JsonRPC2Methods getMethod() {
		return JsonRPC2Methods.UPDATE;
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
		return OperationDefinition.CatchMedia.OperationId.AUTH_UPDATE;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.POST;
	}

	@Override
	public String getServiceUrl(final Context context) {
		return OperationDefinition.CatchMedia.ServiceName.AUTH_UPDATE;
	}

	@Override
	public String getRequestBody() {
		return null;
	}

    @Override
    public String getTimeStampCache() {
        return null;
    }

    @Override
	public Map<String, Object> parseResponse(Response response) {
		Map<String, Object> responseMap = new HashMap<String, Object>();
		return responseMap;
	}

	@Override
	public Map<String, Object> getDescriptor() {
		Map<String, Object> descriptorMap = new HashMap<String, Object>();
		descriptorMap.put("third_party_type", "google");
		Map<String, Object> descriptorToken = new HashMap<String, Object>();
		descriptorToken.put("token", token);
		descriptorMap.put("third_party_auth_data", descriptorToken);
		// {
		// "third_party_type": "facebook"
		// , "third_party_auth_data": {
		// "token": "123456789"
		// }
		// }
		return descriptorMap;
	}

}
