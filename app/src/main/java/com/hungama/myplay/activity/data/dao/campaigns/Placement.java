/**
 * 
 */
package com.hungama.myplay.activity.data.dao.campaigns;

import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.util.Logger;

import java.io.Serializable;
import java.util.List;

/**
 * @author DavidSvilem
 * 
 */
public class Placement implements Serializable, Comparable<Placement> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public transient static final int PREMIUM_PRIORITY = 0;
	public transient static final int EXCESS_PRIORITY = 1;

	public transient static final int[] PRIORITY_LIST = { PREMIUM_PRIORITY,
			EXCESS_PRIORITY };

	@SerializedName("control_parameters")
	private ControlParameters controlParameters;

	@SerializedName("display_widget_info")
	private DisplayWidgetInfo displayWidgetInfo;

	@SerializedName("display_info")
	private DisplayInfo displayInfo;

	// private String placementType;
	// private String bgImageSmall;
	// private String bgImageLarge;

	private String campaignID;

	public void setCampaignID(String id) {
		this.campaignID = id;
	}

	// xtpl
	public String getDisplayDuration() {
		return controlParameters.display_duration;
	}

	public String getDisplayCount() {
		return controlParameters.display_count;
	}

	// xtpl
	public String getPlacementType() {
		return controlParameters.placement_type;
	}

	public PlacementType getPlacementTypeEnum() {
		return PlacementType.valueOf(controlParameters.placement_type);
	}

	public String getEffectiveTill() {
		return controlParameters.effective_till;
	}

	public String getEffectiveFrom() {
		return controlParameters.effective_from;
	}

	public boolean isExcess() {
		return controlParameters.excess;
	}

	public float getWeight() {
		return controlParameters.weight;
	}

	public String getBgImageSmall() {
		return displayInfo.bg_image_small;
	}

	public String getBgImageLarge() {
		return displayInfo.bg_image_large;
	}

	public String getDisplayInfoXdpi() {
		return displayInfo.xdpi;
	}

	public String getDisplayInfoXhdpi() {
		return displayInfo.xhdpi;
	}

	public String getDisplayInfoXxhdpi() {
		return displayInfo.xxhdpi;
	}

	public String getDisplayInfoHdpi() {
		return displayInfo.hdpi;
	}

	public String getDisplayInfoMdpi() {
		return displayInfo.mdpi;
	}

	public String getDisplayInfoLdpi() {
		return displayInfo.ldpi;
	}

	public List<Action> getActions() {
		return displayWidgetInfo.widget_display_options.actions;
	}

	public String getTrackingID() {
		String nill = displayWidgetInfo.tracking_id;
		if (nill == null)
			nill = displayWidgetInfo.widget_display_options.tracking_id;
		return nill;
	}

	public String getCampaignID() {
		return campaignID;
	}

	public String getMp3Audio() {
		return displayInfo.mp3_audio;
	}

	public String get3gpVideo() {
		return displayInfo.video_3gp;
	}

	@Override
	public int compareTo(Placement arg0) {
		if (getWeight() == arg0.getWeight())
			return 0;
		else if (getWeight() < arg0.getWeight())
			return -1;
		else
			return 1;
	}

	public int getPriority() {
		return isExcess() ? EXCESS_PRIORITY : PREMIUM_PRIORITY;
	}

	public long getEffectiveTillInLong() {
		String effectiveTill = controlParameters.effective_till;
		long effective_till = -1;
		if (effectiveTill == null)
			effective_till = 0;
		else
			effective_till = CampaignUtils.convertTimeToUNIX(effectiveTill)
					/ CampaignUtils.TIME_UTC_DIVISOR;
		return effective_till;
	}

	public long getEffectiveFromInLong() {
		String effectiveFrom = controlParameters.effective_from;
		long effective_from = -1;
		if (effectiveFrom == null)
			effective_from = 0;
		else
			effective_from = CampaignUtils.convertTimeToUNIX(effectiveFrom)
					/ CampaignUtils.TIME_UTC_DIVISOR;
		return effective_from;
	}

	public boolean isSkipAllowed(){
		try {
			return displayWidgetInfo.widget_display_options.skip;
		} catch (Exception e){
			Logger.printStackTrace(e);
		}
		return false;
	}
}
