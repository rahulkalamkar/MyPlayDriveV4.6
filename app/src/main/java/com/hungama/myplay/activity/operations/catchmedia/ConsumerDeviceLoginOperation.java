package com.hungama.myplay.activity.operations.catchmedia;

import android.content.Context;
import android.location.Location;
import android.text.TextUtils;

import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.CacheManager.Callback;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.dao.catchmedia.SignupField;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.Map;

public class ConsumerDeviceLoginOperation extends CMOperation {

	private static final String TAG = "ConsumerDeviceLoginOperation";

	public static final String RESPONSE_KEY_OBJECT_SIGNUP_FIELDS = "response_key_object_signup_fields";

	private static final String KEY_SIGNUP_FIELDS = "signup_fields";
	private static final String KEY_SET_ID = "set_id";

	private final Map<String, Object> mSignupFields;
	private final long mSetID;

	private boolean isSkipSelected;
	private Context mContext;
//	private ApplicationConfigurations mApplicationConfigurations;
//    private DeviceConfigurations mDeviceConfigurations;

	public ConsumerDeviceLoginOperation(Context context,
                                        Map<String, Object> signupFields, long setId, boolean value) {
		super(context);
		mSignupFields = signupFields;
		mSetID = setId;
		isSkipSelected = value;
		mContext = context;
//		mApplicationConfigurations = ApplicationConfigurations
//				.getInstance(mContext);
//        mDeviceConfigurations = DeviceConfigurations.getInstance(context);
	}

	@Override
	public JsonRPC2Methods getMethod() {
		return JsonRPC2Methods.LOGIN;
	}

	@Override
	public Map<String, Object> getDescriptor() {
		Map<String, Object> descriptorMap = new HashMap<String, Object>();
		descriptorMap.put(ServerConfigurations.PARTNER_ID,
				Integer.parseInt(pServerConfigurations.getPartnerId()));
		descriptorMap.put(KEY_SIGNUP_FIELDS, mSignupFields);
		descriptorMap.put(KEY_SET_ID, mSetID);
		descriptorMap.put(ServerConfigurations.APPLICATION_CODE,
				pServerConfigurations.getAppCode());
		descriptorMap.put(DeviceConfigurations.DEVICE_MODEL_NAME,
				pDeviceConfigurations.getDeviceModelName());
        descriptorMap.put("client_type","full");
        descriptorMap.put("hardware_id_type",pDeviceConfigurations.getHardwareIdType());
        descriptorMap.put("app_ver",DataManager.getVersionName(mContext));
        descriptorMap.put("app_code", pServerConfigurations.getAppCode());
        descriptorMap.put("device_os","Android");
        descriptorMap.put("device_os_description",pDeviceConfigurations.getDeviceOSDescription());
        descriptorMap.put("hardware_id",pDeviceConfigurations.getHardwareId());
		Location location = Utils.getLocation(getContext());
		if(location!=null){
			descriptorMap.put(ServerConfigurations.LATITUDE,
					location.getLatitude());
			descriptorMap.put(ServerConfigurations.LONGITUDE,
					location.getLongitude());
		}

		return descriptorMap;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.CatchMedia.OperationId.CONSUMER_DEVICE_LOGIN;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.POST;
	}

	@Override
	public String getServiceUrl(final Context context) {
		return OperationDefinition.CatchMedia.ServiceName.CONSUMER_DEVICE_LOGIN;
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

		JSONParser parser = new JSONParser();
		try {
			Map<String, Object> activationMap = (Map<String, Object>) parser
					.parse(response.response);
			Map<String, Object> responseMap = new HashMap<String, Object>();

			if (!activationMap
					.containsKey(ApplicationConfigurations.ACTIVATION_CODE)
					|| !activationMap
							.containsKey(ApplicationConfigurations.PARTNER_USER_ID)) {
				throw new InvalidResponseDataException();
			}

			String activationCode = (String) activationMap
					.get(ApplicationConfigurations.ACTIVATION_CODE);
			String partnerUserId = (String) activationMap
					.get(ApplicationConfigurations.PARTNER_USER_ID);
			boolean isRealUser = Boolean.parseBoolean((String) activationMap
					.get(ApplicationConfigurations.IS_REAL_USER));

			String deviceId = (String) activationMap
					.get(ApplicationConfigurations.DEVICE_ID);
			String existingDevice = (String) activationMap
					.get(ApplicationConfigurations.EXISTING_DEVICE);

			pApplicationConfigurations.setDeviceID(deviceId);
			pApplicationConfigurations.setIfDeviceExist(Boolean
					.getBoolean(existingDevice));

			// Save the gigya login token and secret
			String gigyaLoginSessionToken = (String) activationMap
					.get(ApplicationConfigurations.GIGYA_LOGIN_SESSION_TOKEN);
			String gigyaLoginSessionSecret = (String) activationMap
					.get(ApplicationConfigurations.GIGYA_LOGIN_SESSION_SECRET);

			if (!TextUtils.isEmpty(gigyaLoginSessionToken)
					&& !TextUtils.isEmpty(gigyaLoginSessionSecret)) {
				pApplicationConfigurations
						.setGigyaSessionToken(gigyaLoginSessionToken);
				pApplicationConfigurations
						.setGigyaSessionSecret(gigyaLoginSessionSecret);
			}

			if (isSkipSelected) {
				String skippedPartnerUserId = (String) activationMap
						.get(ApplicationConfigurations.PARTNER_USER_ID);
				if (!TextUtils.isEmpty(skippedPartnerUserId)) {
					pApplicationConfigurations
							.setSkippedPartnerUserId(skippedPartnerUserId);
				}
			}

			if (mSignupFields != null
					&& mSignupFields.containsKey("phone_number")) {
				Map<String, Object> fieldMap = (Map<String, Object>) mSignupFields
						.get("phone_number");
				String value = "";
				if (fieldMap != null) {
					value = (String) fieldMap.get(SignupField.VALUE);
				}
				pApplicationConfigurations.setUserLoginPhoneNumber(value);
			}

			pApplicationConfigurations.setPartnerUserId(partnerUserId);

			boolean realUser = pApplicationConfigurations.isRealUser();

			if (!realUser && isRealUser) {
				//
				pApplicationConfigurations.setConsumerRevision(0);
				pApplicationConfigurations.setHouseholdRevision(0);

				// Delete all locale playlists on device
				DataManager mDataManager = DataManager
						.getInstance(getContext());
				Map<Long, Playlist> empty = new HashMap<Long, Playlist>();
				mDataManager.storePlaylists(empty, new Callback() {

					@Override
					public void onResult(Boolean gotResponse) {
						// TODO Auto-generated method stub

					}
				});
			}

			pApplicationConfigurations.setIsRealUser(isRealUser);

            try {
                // stores the session and other crucial properties.
                String sessionID = (String) activationMap
                        .get(ApplicationConfigurations.SESSION_ID);
                int householdID = ((Long) activationMap
                        .get(ApplicationConfigurations.HOUSEHOLD_ID)).intValue();
                int consumerID = ((Long) activationMap
                        .get(ApplicationConfigurations.CONSUMER_ID)).intValue();
                String passkey = (String) activationMap
                        .get(ApplicationConfigurations.PASSKEY);

                pApplicationConfigurations.setSessionID(sessionID);
                pApplicationConfigurations.setHouseholdID(householdID);
                pApplicationConfigurations.setConsumerID(consumerID);
                pApplicationConfigurations.setPasskey(passkey);
            } catch (Exception e){
                Logger.printStackTrace(e);
            }

			responseMap.put(ApplicationConfigurations.ACTIVATION_CODE,
					activationCode);
			responseMap.put(ApplicationConfigurations.PARTNER_USER_ID,
					partnerUserId);
			responseMap.put(ApplicationConfigurations.IS_REAL_USER, isRealUser);
			responseMap.put(RESPONSE_KEY_OBJECT_SIGNUP_FIELDS, mSignupFields);

			// Grab the GCM registration id and send to Hungama.

			// if(mSetID == SignOption.SET_ID_MYPLAY_SIGNUP)
//			ApsalarEvent.postEvent(mContext, ApsalarEvent.SIGNUP_COMPLITION);

			return responseMap;

		} catch (ParseException exception) {
			exception.printStackTrace();
		}

		return null;
	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return null;
	}

}
