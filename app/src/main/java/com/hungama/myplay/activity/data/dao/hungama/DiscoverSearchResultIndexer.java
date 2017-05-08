package com.hungama.myplay.activity.data.dao.hungama;

/**
 * Holds properties of the indexing from the response thru the paging process.
 */
public class DiscoverSearchResultIndexer {

	public static final int DEFAULT_START_INDEX = 1;
	public static final int DEFAULT_LENGTH = 30;

	private int startIndex;
	private final int length;
	private final int total;

	public DiscoverSearchResultIndexer(int startIndex, int length, int total) {
		this.startIndex = startIndex;
		this.length = length;
		this.total = total;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public int getLength() {
		return length;
	}

	public int getTotal() {
		return total;
	}
}
