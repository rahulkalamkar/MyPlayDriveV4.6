package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LeftMenuImages implements Serializable {

	@Expose
	@SerializedName("ldpi")
	private final String ldpi;
	@Expose
	@SerializedName("mdpi")
	private final String mdpi;
	@Expose
	@SerializedName("hdpi")
	private final String hdpi;
	@Expose
	@SerializedName("xdpi")
	private final String xdpi;

	public LeftMenuImages(String ldpi, String mdpi, String hdpi, String xdpi) {
		this.ldpi = ldpi;
		this.mdpi = mdpi;
		this.hdpi = hdpi;
		this.xdpi = xdpi;
	}

	public String getLdpi() {
		return ldpi;
	}

	public String getMdpi() {
		return mdpi;
	}

	public String getHdpi() {
		return hdpi;
	}

	public String getXdpi() {
		return xdpi;
	}
}
