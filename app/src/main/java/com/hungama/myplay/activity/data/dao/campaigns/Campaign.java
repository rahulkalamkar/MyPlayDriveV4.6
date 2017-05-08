package com.hungama.myplay.activity.data.dao.campaigns;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class Campaign implements Serializable {

	private static final long serialVersionUID = 1L;

	@SerializedName("id")
	private String ID;

	@SerializedName("root_node")
	private Node rootNode;

	@SerializedName("control_parameters")
	private ControlParameters controlParameters;

	@SerializedName("placements")
	private List<Placement> placements;

	private List<String> tags;

	// Empty C'tor
	public Campaign() {
	}

	// C'tor
	public Campaign(Map mapNode, List<Map> mapPlacements,
			Map<String, Object> mapCP, String id) {
		setID(id);

		// rootNode = new Node(mapNode, mapCP,id);

		// placements = (List<Map>) mapPlacements;
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public Node getNode() {
		return rootNode;
	}

	public void setNode(Node rootNode) {
		this.rootNode = rootNode;
	}

	public ControlParameters getControlParameters() {
		return controlParameters;
	}

	public void setControlParameters(ControlParameters controlParameters) {
		this.controlParameters = controlParameters;
	}

	public List<Placement> getPlacements() {
		return placements;
	}

	public void setPlacements(List<Placement> placements) {
		this.placements = placements;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

}
