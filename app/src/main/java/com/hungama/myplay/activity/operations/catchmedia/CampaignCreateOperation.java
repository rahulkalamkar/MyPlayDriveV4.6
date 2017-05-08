package com.hungama.myplay.activity.operations.catchmedia;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Campaign;
import com.hungama.myplay.activity.data.dao.campaigns.Node;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

/**
 * Retrieves device ID from CM servers, based on the device's properties.
 */
public class CampaignCreateOperation extends CMOperation {

	private static final String TAG = "CampaignCreateOperation";

	public static final String RESPONSE_KEY_OBJECT_CAMPAIGN = "response_key_campaign";

	private List<String> campaignIDs;
	private final String screenSize;

	public CampaignCreateOperation(Context context, List<String> campaignIDs) {
		super(context);
		screenSize = DataManager.getInstance(context)
				.getDisplayDensityCampaign();
		this.campaignIDs = new ArrayList<String>(campaignIDs);

	}

	@Override
	public JsonRPC2Methods getMethod() {
		return JsonRPC2Methods.READ;
	}

	@Override
	public Map<String, Object> getDescriptor() {
		Map<String, Object> descriptor = new HashMap<String, Object>();
		descriptor.put(ServerConfigurations.VERSION,
				pServerConfigurations.getmVersion());
		descriptor.put(ApplicationConfigurations.CAMPAIGN_IDS, campaignIDs);
		descriptor.put(ApplicationConfigurations.PARAMS_SIZE, screenSize);
		return descriptor;
	}

	@Override
	protected Map<String, Object> getCredentials() {

		Map<String, Object> credentials = new HashMap<String, Object>();
		credentials.put(ServerConfigurations.LC_ID,
				pServerConfigurations.getLcId());
		credentials.put(ServerConfigurations.PARTNER_ID,
				pServerConfigurations.getPartnerId());
		credentials.put(ServerConfigurations.WEB_SERVER_VERSION,
				pServerConfigurations.getWebServiceVersion());
		credentials.put(DeviceConfigurations.TIMESTAMP,
				pDeviceConfigurations.getTimeStampDelta());
		// credentials.put(ServerConfigurations.APPLICATION_CODE,
		// pServerConfigurations.getAppCode());
		// credentials.put(ServerConfigurations.APPLICATION_VERSION,
		// pServerConfigurations.getAppVersion());
		// credentials.put(DeviceConfigurations.HARDWARE_ID,
		// pDeviceConfigurations.getHardwareId());
		credentials.put(ApplicationConfigurations.SESSION_ID,
				pApplicationConfigurations.getSessionID());

		return credentials;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.CatchMedia.OperationId.CAMPAIGN_READ;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.POST;
	}

	@Override
	public String getServiceUrl(final Context context) {
		return OperationDefinition.CatchMedia.ServiceName.CAMPAIGN_READ;
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
		// Runtime.getRuntime().gc();
		JSONParser jsonParser = new JSONParser();
		try {

			Logger.d(TAG, response.response);
			Map<String, Object> campainMap = (Map<String, Object>) jsonParser
					.parse(response.response);

			Type listType = new TypeToken<ArrayList<Campaign>>() {
			}.getType();
			Gson gsonParser = new Gson();

			List<Campaign> campaigns = gsonParser.fromJson(
					campainMap.get("campaigns").toString(), listType);

			// sets the internal nodes with their parent campaigns.
			if (!Utils.isListEmpty(campaigns)) {
				List<Node> nodesList = new ArrayList<Node>();
				for (Campaign c : campaigns) {
					CampaignsManager.setCampaignIdForNode(c.getNode(),
							c.getID());
					nodesList.add(c.getNode());
				}
			}

			Map<String, Object> responseMap = new HashMap<String, Object>();
			responseMap.put(RESPONSE_KEY_OBJECT_CAMPAIGN, campaigns);

			return responseMap;

		} catch (ParseException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException("Device map parsing error.");
		}

	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return null;
	}

}
