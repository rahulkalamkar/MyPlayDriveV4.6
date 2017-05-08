package com.hungama.myplay.activity.data.dao.campaigns;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public class DisplayInfo implements Serializable {

	public String text1_color;
	public String bg_image_small;
	public String bg_color;
	public String text3_color;
	public String text3;
	public String text1;
	public String text2;
	// public String banner_xlarge;
	public String thumb_large;
	public String text2_color;
	public String bg_image_large;
	public String bg_image_medium;
	public String thumb_small;

	public String hdpi;
	public String mdpi;
	public String xdpi;
	public String xhdpi;
	public String xxhdpi;
	public String ldpi;
	// public String ipad;
	// public String iphone;
	// public String iphone_retina;

	public String mp3_audio;

	@SerializedName("3gp_video")
	public String video_3gp;

	// @SerializedName("mp4_video")
	// public String video_mp4;
}
