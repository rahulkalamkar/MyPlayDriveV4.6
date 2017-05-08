package com.hungama.myplay.activity.util;

import android.os.Environment;
import android.util.Log;

import com.hungama.hungamamusic.lite.BuildConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Util class for controlling the SDK Log the {@code BuildConfig.DEBUG} flag.
 */
public class Logger {// post_share

//	/**
//	 * It was added for Save Offline feature enable/disable in v4.3.
//	 *
//	 * @deprecated No use of now as Save Offline(Download) is feature for all newer app version.
//	 */
//	@Deprecated
//	public static final boolean isSaveOffline = true;

	/**
	 * It's for enableing and disabling device log.<br><br>
	 * Set it <b>false</b> for <b>SPA(market)</b> version.
	 */
	public static final boolean isDebuggable = BuildConfig.DEBUG;//true;


	/**
	 * It's for enableing and disabling for writing log file on SDCard.<br><br>
	 * Set it <b>false</b> for <b>SPA(market)</b> version.
	 */
	private static final boolean isWriteTofile = BuildConfig.DEBUG;//true;

//	/**
//	 * If it's true, app will not allow silent user to download content. Intoruded in v4.3.
//	 *
//	 * @deprecated No use of now as login is compolsory for downloading content.
//	 */
//	@Deprecated
//	public static final boolean disableAutoLogin = true;

	/**
	 * true for enableing Reverie lib and false for disabling Reverie lib.
	 */
	public static final boolean enableLanguageLibrary = true;
	public static final boolean enableLanguageLibraryThread = true;


	@Deprecated
	public static final boolean isSaveOffline = true;


//	/**
//	 * Ebabling/disabling new CDN search api.
//	 */
//	public static final boolean isCdnSearch = true;

	/**
	 * Used for testing purpose only. If it's true, it will pass default low bitate for all media content.<br><br>
	 *
	 * Set it <b>false</b> for <b>SPA(market)</b> version.
	 */
	public static final boolean defalutLowBitrate = false;

//	/**
//	 * Allow downloaded cache files to be store on external SDCard. If it's set to false, cache files will be stored in app
//	 * cache folder and it will gets cleared as soon as user clears app cache or uninstalls app.<br><br>
//	 *
//	 * Introduced with v4.5 & must be set to <b>true</b> for <b>SPA(market)</b> version.
//	 */
//	public static final boolean allowedOutsideCache = true;

//	/**
//	 * It's used to enable/disable new ConsumerDevice.Login api for user login.<br><br>
//	 *
//	 * Introduced with v4.5 & must be set to <b>true</b> for <b>SPA(market)</b> version.
//	 */
//    public static final boolean enableConsumerDeviceLogin = true;

	/**
	 * It's used to enable/disable ad reporting on each tab change for home screen.<br><br>
	 *
	 * Introduced with v4.5.1 & must be set to <b>true</b> for <b>SPA(market)</b> version.
	 */
	public static final boolean repostAdOnTabChange = true;

//	/**
//	 * It's used to enable/disable Hungama Pay(new payment) api for Upgrade and Download options.<br><br>
//	 *
//	 * Introduced for v4.6. Set it to true for enabling api and set it to false for diabling Hungama Pay api and use older apis.
//	 */
//	public static final boolean enableHungamaPay = true;

	/**
	 * It's used to enable/disable fabric events.<br><br>
	 *
	 * Introduced with v4.5.3. Set it to true for enabling fabric events & false for disabling fabric event.
	 */
	public static final boolean enableFabricEvents = false;

	/**
	 * It's used to enable/disable Ok Http api calling method.<br><br>
	 *
	 * Introduced with v4.5.5. Set it to true for enabling Ok Http api calling method & false for disabling it and call native
	 * Android HTTP method.
	 */
	public static final boolean enableOkHTTP = true;

	/**
	 * It's used to build Micromax OEM enabled version. Set it to true for enabling Micromax OEM api call for Apsalar and set
	 * it to false for creating Google Play Store or any other app store version.<br><br>
	 *
	 * Introduced for v4.5.5 up and must be set to <b>false</b> for <b>Google Play Store</b> version.
	 */
	public static final boolean isMicromaxOEMVersion = false;

	/**
	 * Used to pass hardcoded 100x100 parameter for getting playlist tile images for Home screen Music listing section. Set it
	 * to true for passing hardcoded 100x100 value in api and set it to false for disabling it.
	 */
	public static final boolean hardcodePlaylistImage = true;

	public static final boolean enableSSL = Config.enableSSL;

	public static final boolean allowPlanForSilentUser = true;
	public static final boolean enableHungamaPay = true;

	//public static final boolean enableAndroidM_Permission = false;

	public static void writetofile(String tag, String message) {
		if (isWriteTofile) {
			File f = new File(Environment.getExternalStorageDirectory(),
					"hungama_logs.txt");//
			try {
				if (!f.exists())
					f.createNewFile();
				FileWriter writer = new FileWriter(f, true);
				writer.write("\n\r" + tag + new Date() + " ::: " + message);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void writeTestApitofile(String tag, String message) {
		if (isWriteTofile) {
			File f = new File(Environment.getExternalStorageDirectory(),
					"hungama_testapi_logs.txt");//
			try {
				if (!f.exists())
					f.createNewFile();
				FileWriter writer = new FileWriter(f, true);
				writer.write("\n\r" + tag + ": " + message);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void writetofile(String message, boolean append) {
		if (isWriteTofile) {
			SimpleDateFormat sdf = new SimpleDateFormat("yy");
			File f = new File(Environment.getExternalStorageDirectory(),
					"hungama_" + sdf.format(new Date()) + ".txt");//
			try {
				if (!f.exists())
					f.createNewFile();
				FileWriter writer = new FileWriter(f, append);
				writer.write("\n\r " + message);
				writer.close();
				removeOldFile(Environment.getExternalStorageDirectory().getPath() +
						"/hungama_" + (Integer.parseInt(sdf.format(new Date())) - 1) + ".txt");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// File f = new File(Environment.getExternalStorageDirectory(),
		// "hungama_logs.txt");
		//
		// try {
		// if (!f.exists())
		// f.createNewFile();
		// FileWriter writer = new FileWriter(f, true);
		// writer.write("\n\rDIRECT : " + message);
		// writer.close();
		//
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	public static void writetofile_(String message, boolean append) {
 			SimpleDateFormat sdf = new SimpleDateFormat("yy");
			File f = new File(Environment.getExternalStorageDirectory(),
					"hungama_" + sdf.format(new Date()) + ".txt");//
			try {
				if (!f.exists())
					f.createNewFile();
				FileWriter writer = new FileWriter(f, append);
				writer.write("\n\r " + message);
				writer.close();
				removeOldFile(Environment.getExternalStorageDirectory().getPath() +
						"/hungama_" + (Integer.parseInt(sdf.format(new Date())) - 1) + ".txt");
			} catch (IOException e) {
				e.printStackTrace();
			}

		// File f = new File(Environment.getExternalStorageDirectory(),
		// "hungama_logs.txt");
		//
		// try {
		// if (!f.exists())
		// f.createNewFile();
		// FileWriter writer = new FileWriter(f, true);
		// writer.write("\n\rDIRECT : " + message);
		// writer.close();
		//
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	
	public static void writetofileCampaign(String message, boolean append) {
		if (isWriteTofile) {
			SimpleDateFormat sdf = new SimpleDateFormat("yy");
			File f = new File(Environment.getExternalStorageDirectory(),
					"hungama_campaign_" + sdf.format(new Date()) + ".txt");//
			try {
				if (!f.exists())
					f.createNewFile();
				FileWriter writer = new FileWriter(f, append);
				writer.write("\n\r " + message);
				writer.close();
				removeOldFile(Environment.getExternalStorageDirectory().getPath() +
						"/hungama_campaign_" + (Integer.parseInt(sdf.format(new Date())) - 1) + ".txt");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void writetofileApsalar(String message, boolean append) {
		if (isWriteTofile) {
			Logger.s(message);
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yy");
			File f = new File(Environment.getExternalStorageDirectory(),
					"hungama_apsalar_" + sdf.format(date) + ".txt");//
			try {
				if (!f.exists())
					f.createNewFile();
				FileWriter writer = new FileWriter(f, append);
				writer.write("\n\r " + date + " :::: " + message);
				writer.close();
				removeOldFile(Environment.getExternalStorageDirectory().getPath() +
						"/hungama_apsalar_" + (Integer.parseInt(sdf.format(new Date())) - 1) + ".txt");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void writetofileSearchlog(String message, boolean append) {
//		if (isCdnSearch && isWriteTofile) {
//			SimpleDateFormat sdf = new SimpleDateFormat("yy");
//			File f = new File(Environment.getExternalStorageDirectory(),
//					"hungama_search_" + sdf.format(new Date()) + ".txt");//
//			try {
//				if (!f.exists())
//					f.createNewFile();
//				FileWriter writer = new FileWriter(f, append);
//				writer.write("\n\r " + message);
//				writer.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
	}

	/**
	 * Set an info log message.
	 * 
	 * @param tag
	 *            for the log message.
	 * @param message
	 *            Log to output to the console.
	 */
	public static void i(String tag, String message) {
		try {
			// if (BuildConfig.DEBUG)
			if (isDebuggable) {
				writetofile(tag, message);
				Log.i(tag, message);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * Set an info log message.
	 * 
	 * @param Tag
	 *            for the log message.
	 * @param message
	 *            Log to output to the console.
	 */
	// public static void ii(String tag, String message) {
	// // if (BuildConfig.DEBUG)
	// try {
	// Log.i(tag, message);
	// } catch (Exception e) {
	// }
	// }

	/**
	 * Set an error log message.
	 * 
	 * @param tag
	 *            for the log message.
	 * @param message
	 *            Log to output to the console.
	 */
	public static void e(String tag, String message) {
		try {
			// if (BuildConfig.DEBUG)
			if (isDebuggable) {
				Log.e(tag, message);
				writetofile(tag, message);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * Set a warning log message.
	 * 
	 * @param tag
	 *            for the log message.
	 * @param message
	 *            Log to output to the console.
	 */

	public static void w(String tag, String message) {
		try {
			// if (BuildConfig.DEBUG)
			if (isDebuggable) {
				Log.w(tag, message);
				writetofile(tag, message);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * Set a debug log message.
	 * 
	 * @param tag
	 *            for the log message.
	 * @param message
	 *            Log to output to the console.
	 */
	public static void d(String tag, String message) {
		try {
			// if (BuildConfig.DEBUG)
			if (isDebuggable) {
				Log.d(tag, message);
				writetofile(tag, message);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * Set a verbose log message.
	 * 
	 * @param tag
	 *            for the log message.
	 * @param message
	 *            Log to output to the console.
	 */
	public static void v(String tag, String message) {
		try {
			// if (BuildConfig.DEBUG)
			if (isDebuggable) {
				Log.v(tag, message);
				writetofile(tag, message);
			}
		} catch (Exception e) {
		}
	}

	public static void s(String message) {
		try {
			// if (BuildConfig.DEBUG)
			if (isDebuggable) {
				System.out.println("Hungama : " + message);
				writetofile("Hungama : ", message);
			}
		} catch (Exception e) {
		}
	}

	public static void printStackTrace(Exception e) {
		try {
//			Crashlytics.logException(e);

			// if (BuildConfig.DEBUG)
			if (isDebuggable) {
				e.printStackTrace();
				writetofile("Hungama : ", e.getMessage());
			}
		} catch (Exception e1) {
		}
	}

	public static void printStackTrace(Error e) {
		try {
			// if (BuildConfig.DEBUG)
			if (isDebuggable) {
				e.printStackTrace();
				writetofile("Hungama : ", e.getMessage());
			}
		} catch (Exception e1) {
		}
	}

	private static final void removeOldFile(String fileName){
		try{
			File f = new File(fileName);//
			if (f.exists())
				f.delete();
		} catch (Exception e){
		}
	}
}
