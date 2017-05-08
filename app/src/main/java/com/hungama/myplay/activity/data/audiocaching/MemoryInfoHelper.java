package com.hungama.myplay.activity.data.audiocaching;

import java.io.File;
import java.text.DecimalFormat;

import android.os.Environment;
import android.os.StatFs;

import com.hungama.myplay.activity.util.Logger;

public class MemoryInfoHelper {

	public static final String ERROR = "Error on getting memory info";

	public static boolean externalMemoryAvailable() {
		try {
			return android.os.Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return false;
	}

	public static String getAvailableInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return formatSize(availableBlocks * blockSize);
	}

	public static long getAvailableInternalMemorySizeLong() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();

		long size = availableBlocks * blockSize;

		return size;
	}

	public static String getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return formatSize(totalBlocks * blockSize);
	}

	public static long getTotalInternalMemorySizeLong() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();

		long size = totalBlocks * blockSize;

		return size;
	}

	public static String getAvailableExternalMemorySize() {
		try {
			if (externalMemoryAvailable()) {
				File path = Environment.getExternalStorageDirectory();
				StatFs stat = new StatFs(path.getPath());
				long blockSize = stat.getBlockSize();
				long availableBlocks = stat.getAvailableBlocks();
				return formatSize(availableBlocks * blockSize);
			} else {
				return ERROR;
			}	
		} catch (Exception e) {
			return ERROR;
		}
		
	}

	public static long getAvailableExternalMemorySizeLong() {
		try {
			if (externalMemoryAvailable()) {
				File path = Environment.getExternalStorageDirectory();
				StatFs stat = new StatFs(path.getPath());
				long blockSize = stat.getBlockSize();
				long availableBlocks = stat.getAvailableBlocks();

				long size = availableBlocks * blockSize;

				return size;
			} else {
				return -1;
			}
		} catch (Exception e) {
			return -1;
		}
	}

	// public static long getAvailableExternalMemorySizeLong(String pathStr) {
	// // if (externalMemoryAvailable()) {
	// // File path = Environment.getExternalStorageDirectory();
	// File path = new File(pathStr);
	// if(path.exists()) {
	// StatFs stat = new StatFs(path.getPath());
	// long blockSize = stat.getBlockSize();
	// long availableBlocks = stat.getAvailableBlocks();
	//
	// long size = availableBlocks * blockSize;
	//
	// return size;
	// } else {
	// return -1;
	// }
	// }

	public static String getTotalExternalMemorySize() {
		if (externalMemoryAvailable()) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getBlockCount();
			return formatSize(totalBlocks * blockSize);
		} else {
			return ERROR;
		}
	}

	public static long getTotalExternalMemorySizeLong() {
		if (externalMemoryAvailable()) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getBlockCount();
			long size = totalBlocks * blockSize;

			return size;
		} else {
			return -1;
		}
	}

	// public static String formatSize(long size) {
	// String suffix = null;
	// float msize = 0;
	// if (size >= 1024) {
	// suffix = "KB";
	// msize = size%1024;
	// size /= 1024;
	// if (size >= 1024) {
	// suffix = "MB";
	// msize = size%1024;
	// size /= 1024;
	// if (size >= 1024) {
	// suffix = "GB";
	// msize = (size%1024);
	// size /= 1024;
	// }
	// }
	// }
	// System.out.println(" :::::::::::: " + msize);
	// StringBuilder resultBuffer = new StringBuilder(Long.toString(size));
	//
	// int commaOffset = resultBuffer.length() - 3;
	// while (commaOffset > 0) {
	// resultBuffer.insert(commaOffset, ',');
	// commaOffset -= 3;
	// }
	//
	// if(msize>0){
	// StringBuilder mResultBuffer = new StringBuilder(Float.toString(msize));
	// if(mResultBuffer.length()>2)
	// mResultBuffer.setLength(2);
	// resultBuffer.append("." + mResultBuffer.toString());
	// }
	//
	// if (suffix != null) resultBuffer.append(suffix);
	// return resultBuffer.toString();
	// }

	private static final double BASE = 1024, KB = BASE, MB = KB * BASE, GB = MB
			* BASE;
	private static final DecimalFormat df = new DecimalFormat("#.##");

	public static String formatSize(double size) {
		if (size >= GB) {
			return df.format(size / GB) + " GB";
		}
		if (size >= MB) {
			return df.format(size / MB) + " MB";
		}
		if (size >= KB) {
			return df.format(size / KB) + " KB";
		}
		return "" + (int) size + " bytes";
	}

}
