package com.hungama.myplay.activity.data.audiocaching;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;

import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.util.Logger;

public class MoveDownloadedTracksService extends IntentService {

	public MoveDownloadedTracksService() {
		super("MoveDownloadedTracksService");
	}

	public MoveDownloadedTracksService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (MemoryInfoHelper.externalMemoryAvailable()) {
			moveMusicTracks();
			moveVideoTracks();
		}
	}

	private void moveMusicTracks() {
		String newPath = CacheManager.getCacheTracksFolderPath(true);
		List<MediaItem> mediaItems = DBOHandler.getAllTracks(this);
		for (MediaItem mediaItem : mediaItems) {
			String path = DBOHandler.getTrackPathById(this,
					"" + mediaItem.getId());
			if (!TextUtils.isEmpty(path)) {
				String newFullPath = moveFile(path, newPath);
				if (!TextUtils.isEmpty(newFullPath)) {
					DBOHandler.updateTrack(this, "" + mediaItem.getId(),
							newFullPath, null, null);
				}
			}
		}
	}

	private void moveVideoTracks() {
		String newPath = CacheManager.getCacheVideoTracksFolderPath(true);
		List<MediaItem> mediaItems = DBOHandler.getAllVideoTracks(this);
		for (MediaItem mediaItem : mediaItems) {
			String path = DBOHandler.getVideoTrackPathById(this,
					"" + mediaItem.getId());
			if (!TextUtils.isEmpty(path)) {
				String newFullPath = moveFile(path, newPath);
				if (!TextUtils.isEmpty(newFullPath)) {
					DBOHandler.updateVideoTrack(this, "" + mediaItem.getId(),
							newFullPath, null, null);
				}
			}
		}
	}

	private String moveFile(String oldPath, String newPath) {
		String newFullPath = null;
		try {
			if (!oldPath.startsWith(newPath)) {
				File from = new File(oldPath);
				Logger.s("Old path ::::::::::::--- " + oldPath);
				Logger.s("File Name ::::::::::::--- " + from.getName());
				Logger.s("New path ::::::::::::--- " + newPath);
				File to = new File(newPath + "/" + from.getName());
				// if (from.renameTo(to)) {
				// newFullPath = newPath + "/" + from.getName();
				// Logger.s("newFullPath ::::::::::::--- " + newFullPath);
				// }

				InputStream in = new FileInputStream(from);
				OutputStream out = new FileOutputStream(to);
				// Copy the bits from instream to outstream
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
				from.delete();
				newFullPath = newPath + "/" + from.getName();
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return newFullPath;
	}
}
