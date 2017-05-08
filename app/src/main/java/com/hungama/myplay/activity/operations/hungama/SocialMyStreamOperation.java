/**
 * 
 */
package com.hungama.myplay.activity.operations.hungama;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.simple.parser.JSONParser;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.social.MyStreamResult;
import com.hungama.myplay.activity.data.dao.hungama.social.StreamItem;
import com.hungama.myplay.activity.data.dao.hungama.social.StreamItemCategory;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

/**
 * Retrieves Stream Items to be presented in the My Stream section if the
 * application.
 */
public class SocialMyStreamOperation extends SocialOperation {

	private static final String TAG = "SocialMyStreamOperation";

	public static final String RESULT_KEY_STREAM_ITEMS = "result_key_stream_items";
	public static final String RESULT_KEY_STREAM_ITEMS_CATEGORY = "result_key_stream_items_category";

	private static final String PARAMS_FOR = "for";

	private final String mServiceUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final String mImages;
	private final StreamItemCategory mStreamItemCategory;

	public SocialMyStreamOperation(String serviceUrl, String authKey,
			String userId, StreamItemCategory streamItemCategory, String images) {

		this.mServiceUrl = serviceUrl;
		this.mAuthKey = authKey;
		this.mUserId = userId;
		this.mStreamItemCategory = streamItemCategory;
		mImages = images;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SOCIAL_MY_STREAM;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		DeviceConfigurations config = DeviceConfigurations.getInstance(context);

		String imageParams = "";
		if (!TextUtils.isEmpty(mImages))
			imageParams = "&images=" + mImages;

		String serviceUrl = mServiceUrl + URL_SEGMENT_SOCIAL_MY_STREAM
				+ PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND + PARAMS_FOR
				+ EQUALS + mStreamItemCategory.name().toLowerCase()
				+ HungamaOperation.AMPERSAND + PARAMS_DOWNLOAD_HARDWARE_ID
				+ HungamaOperation.EQUALS + config.getHardwareId()
				+ imageParams;

		return serviceUrl;
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

		// if (!TextUtils.isEmpty(response.response))
		// response.response =
		// removeUglyResponseWrappingObjectFromResponse(response.response);

		Gson gsonParser = new Gson();

		try {
			JSONParser jsonParser = new JSONParser();
			Map<String, Object> catalogMap = (Map<String, Object>) jsonParser
					.parse(response.response);
			String reponseString;
			if (catalogMap.containsKey(KEY_RESPONSE)) {
				reponseString = catalogMap.get(KEY_RESPONSE).toString();
			} else {
				throw new InvalidResponseDataException(
						"Parsing error - no catalog available");
			}
			Logger.e("response stream boar", "**" + reponseString);
			// Logger.writetofile("response stream boar--", reponseString);

			MyStreamResult myStreamResult = gsonParser.fromJson(reponseString,
					MyStreamResult.class);

			List<StreamItem> streamItems = myStreamResult.getStreamItems();
			MediaContentType mediaContentType = null;
			List<MediaItem> mediaItems;

			// this is ugly.
			Date time = null;
			// xtpl
			SimpleDateFormat gmtTimeFormater = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);// Changes by Hungama
			// xtpl

			for (StreamItem streamItem : streamItems) {

				// populates the media items with the related content type.
				if (!Utils.isListEmpty(streamItem.moreSongsItems)) {
					mediaContentType = getMediaContentTypeForType(streamItem.type);
					mediaItems = streamItem.moreSongsItems;
					for (MediaItem mediaItem : mediaItems) {
						mediaItem.setMediaContentType(mediaContentType);
					}
				}

				// populates the stream items with dates instead working with
				// string times.
				try {
					time = gmtTimeFormater.parse(streamItem.time);
					streamItem.setDate(time);
				} catch (java.text.ParseException e) {
					Logger.e(
							TAG,
							"Bad time data for Stream item: "
									+ Long.toString(streamItem.conentId)
									+ " skipping it.");
					e.printStackTrace();
					continue;
				}

			}

			if (Thread.currentThread().isInterrupted()) {
				throw new OperationCancelledException();
			}

			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_STREAM_ITEMS,
					myStreamResult.getStreamItems());
			resultMap
					.put(RESULT_KEY_STREAM_ITEMS_CATEGORY, mStreamItemCategory);

			if (Thread.currentThread().isInterrupted()) {
				throw new OperationCancelledException();
			}

			return resultMap;

		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException();
		} catch (Exception e) {
			e.printStackTrace();
			throw new InvalidResponseDataException();
		}
	}

	private MediaContentType getMediaContentTypeForType(String type) {
		if (MediaType.VIDEO.name().equalsIgnoreCase(type)) {
			return MediaContentType.VIDEO;
		} else {
			return MediaContentType.MUSIC;
		}
	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return null;
	}

}
