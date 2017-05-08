package com.hungama.myplay.activity.ui.adapters;

import com.hungama.myplay.activity.data.dao.hungama.MediaItem;

public class ComboMediaItem {
	public ComboMediaItem(MediaItem left, MediaItem right) {
		this.left = left;
		this.right = right;
	}

	public MediaItem left;
	public MediaItem right;
}
