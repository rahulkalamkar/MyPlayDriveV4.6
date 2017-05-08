/**
 * 
 */
package com.hungama.myplay.activity.util;

/**
 * @author stas
 *
 */
import java.io.File;

import android.content.Context;

public class FileCache {

	private File cacheDir;

	public FileCache(Context context) {
		// Find the dir to save cached images
		try {
			if (android.os.Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED))
				// cacheDir=new
				// File(android.os.Environment.getExternalStorageDirectory(),"TempImages");
				cacheDir = new File(context.getExternalCacheDir(), "TempImages");
			else
				cacheDir = context.getCacheDir();
		} catch (Exception e) {
			Logger.printStackTrace(e);
			cacheDir = context.getCacheDir();
		}
		if (!cacheDir.exists())
			cacheDir.mkdirs();
	}

	public File getFile(String url) {
		String filename = String.valueOf(url.hashCode());
		File f = new File(cacheDir, filename);
		return f;

	}

	public File getTempFile(String url) {
		String filename = String.valueOf(url.hashCode()) + ".temp";
		File f = new File(cacheDir, filename);
		return f;

	}

	// public void clear(){
	// File[] files=cacheDir.listFiles();
	// if(files==null)
	// return;
	// for(File f:files)
	// f.delete();
	// }

}
