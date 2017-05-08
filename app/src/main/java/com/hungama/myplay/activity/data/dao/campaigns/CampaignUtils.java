package com.hungama.myplay.activity.data.dao.campaigns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class CampaignUtils {

	public static final boolean D = false;

	public static final SimpleDateFormat FULL_DATE = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");
	public static final SimpleDateFormat FULL_DATE2 = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm'Z'");
	public static final SimpleDateFormat TIME_ONLY = new SimpleDateFormat(
			"HH:mm'Z'");

	public static final int TIME_UTC_DIVISOR = 1000;

	// public static long convertTimeToUNIX(String time){
	// if (time == null)
	// return 0;
	// try {
	// FULL_DATE.setTimeZone(TimeZone.getTimeZone("UTC"));
	// return FULL_DATE.parse(time).getTime();
	// } catch (ParseException e) {
	// //Fail
	// }
	// try {
	// TIME_ONLY.setTimeZone(TimeZone.getTimeZone("UTC"));
	// return TIME_ONLY.parse(time).getTime();
	// } catch (ParseException e) {
	// e.printStackTrace();
	// }
	// return 0;
	// }

	public static long convertTimeToUNIX(String time) {
		if (time == null)
			return 0;
		try {
			FULL_DATE.setTimeZone(TimeZone.getTimeZone("UTC"));
			return FULL_DATE.parse(time).getTime();
		} catch (ParseException e) {
		}
		try {
			FULL_DATE2.setTimeZone(TimeZone.getTimeZone("UTC"));
			return FULL_DATE2.parse(time).getTime();
		} catch (ParseException e) {

		}

		try {
			TIME_ONLY.setTimeZone(TimeZone.getTimeZone("UTC"));
			return TIME_ONLY.parse(time).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
