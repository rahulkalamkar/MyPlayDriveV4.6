/**
 * 
 */
package com.hungama.myplay.activity.data.audiocaching;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hungama.myplay.activity.HungamaApplication;
import com.hungama.myplay.activity.communication.ThreadPoolManager;
import com.hungama.myplay.activity.data.audiocaching.DataBase.CacheState;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaTrackDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.util.Logger;

import org.json.JSONObject;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author XTPL
 * 
 */
public final class DBOHandler {

	private static final Object mDBOHandlerMutext = new Object();

	private static boolean debug = false;

	public static boolean insertTrackToCache(Context context, String id,
			String path, String jsonResponse, String state, String album_id,
			String playlist_id) {
		synchronized (mDBOHandlerMutext) {
			if (!isTrackExist(context, id)) {
				DataBase db = DataBase.getInstance(HungamaApplication
						.getContext());
				db.open();
				long rowId = db.insert(DataBase.Track_Cache_table,
						DataBase.Track_Cache_int, new String[] { id, path,
								jsonResponse, state, "0" });
				// if ((album_id.length() > 0 && !album_id.equals("0"))
				// || (playlist_id.length() > 0 && !playlist_id.equals("0"))) {
				// db.insert(DataBase.Tracklist_table, DataBase.Tracklist_int,
				// new String[] { id, album_id, playlist_id });
				// }
				db.close();
				if (rowId != -1 && context != null) {
					context.sendBroadcast(new Intent(
							CacheManager.ACTION_CACHE_STATE_UPDATED));
				}
				printLog("insertTrackToCache ::::: " + rowId);
				return rowId != -1;
			}
			return false;
		}
	}

	public static boolean insertTrackToTrackListTable(Context context,
			String id, String album_id, String playlist_id) {
		synchronized (mDBOHandlerMutext) {
			long rowId = -1;
			DataBase db = DataBase.getInstance(HungamaApplication.getContext());
			db.open();

			if ((album_id.length() > 0 && !album_id.equals("0"))
					|| (playlist_id.length() > 0 && !playlist_id.equals("0"))) {
				rowId = db.insert(DataBase.Tracklist_table,
						DataBase.Tracklist_int, new String[] { id, album_id,
								playlist_id });
			}
			db.close();
			context.sendBroadcast(new Intent(
					CacheManager.ACTION_CACHE_STATE_UPDATED));
			printLog("insertTrackListTable ::::: " + rowId);
			return rowId != -1;
		}
	}

	public static boolean checkTrackInTrackListTableAndInsert(Context context,
			String id, String playlist_id) {
		synchronized (mDBOHandlerMutext) {
			long rowId = -1;
			DataBase db = DataBase.getInstance(HungamaApplication.getContext());
			db.open();

			Cursor cursor = db.fetch(DataBase.Tracklist_table,
					DataBase.Tracklist_int,
					DataBase.tables[DataBase.Tracklist_int][1] + "=" + id
							+ " AND "
							+ DataBase.tables[DataBase.Tracklist_int][3] + "="
							+ playlist_id);

			if (cursor != null) {
				if (cursor.getCount() == 0) {
					rowId = db.insert(DataBase.Tracklist_table,
							DataBase.Tracklist_int, new String[] { id, "0",
									playlist_id });
				}
				cursor.close();
			} else {
				rowId = db.insert(DataBase.Tracklist_table,
						DataBase.Tracklist_int, new String[] { id, "0",
								playlist_id });
			}
			db.close();
			printLog("insertTrackListTable ::::: " + rowId);
			return rowId != -1;
		}
	}

	public static boolean insertAlbumToCache(Context context, String id,
			String jsonResponse) {
		synchronized (mDBOHandlerMutext) {
			if (isAlbumExist(context, id)) {
				return true;
			}
			DataBase db = DataBase.getInstance(HungamaApplication.getContext());
			db.open();
			long rowId = db.insert(DataBase.Album_Cache_table,
					DataBase.Album_Cache_int, new String[] { id, jsonResponse,
							CacheState.QUEUED.toString() });
			db.close();
			if (rowId != -1) {
				context.sendBroadcast(new Intent(
						CacheManager.ACTION_CACHE_STATE_UPDATED));
			}
			printLog("insertAlbumToCache ::::: " + rowId);
			return rowId != -1;
		}
	}

	public static boolean insertPlaylistToCache(Context context, String id,
			String jsonResponse) {
		synchronized (mDBOHandlerMutext) {
			if (isPlaylistExist(context, id)) {
				return true;
			}
			DataBase db = DataBase.getInstance(HungamaApplication.getContext());
			db.open();
			long rowId = db.insert(
					DataBase.Playlist_Cache_table,
					DataBase.Playlist_Cache_int,
					new String[] { id, jsonResponse,
							CacheState.QUEUED.toString() });
			db.close();
			if (rowId != -1) {
				context.sendBroadcast(new Intent(
						CacheManager.ACTION_CACHE_STATE_UPDATED));
			}
			printLog("insertPlaylistToCache ::::: " + rowId);
			return rowId != -1;
		}
	}

	public static boolean insertVideoTrackToCache(Context context, String id,
			String path, String jsonResponse, String state) {
		synchronized (mDBOHandlerMutext) {
			DataBase db = DataBase.getInstance(HungamaApplication.getContext());
			db.open();
			long rowId = db.insert(DataBase.Video_Track_Cache_table,
					DataBase.Video_Track_Cache_int, new String[] { id, path,
							jsonResponse, state, "0" });
			db.close();
			if (rowId != -1) {
				context.sendBroadcast(new Intent(
						CacheManager.ACTION_VIDEO_CACHE_STATE_UPDATED));
			}
			printLog("insertTrackToCache ::::: " + rowId);
			return rowId != -1;
		}
	}

	// public static Cursor getTrackById(Context context, String id) {
	// synchronized (mDBOHandlerMutext) {
	// DataBase db = DataBase.getInstance(context);
	// db.open();
	// Cursor cursor = db.fetch(DataBase.Track_Cache_table,
	// DataBase.Track_Cache_int,
	// DataBase.tables[DataBase.Track_Cache_int][1] + "=" + id);
	// // if (cursor != null) {
	// // cursor.close();
	// // }
	// db.close();
	// printLog("getTrackById ::::: " + cursor.toString());
	// return cursor;
	// }
	// }

	public static boolean isTrackExist(Context context, String id) {
		synchronized (mDBOHandlerMutext) {
			boolean isExist = false;
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Track_Cache_table,
					DataBase.Track_Cache_int,
					DataBase.tables[DataBase.Track_Cache_int][1] + "=" + id);
			if (cursor != null) {
				if (cursor.moveToFirst())
					isExist = true;
				cursor.close();
			}
			db.close();
			printLog("isTrackExist ::::: " + isExist);
			return isExist;
		}
	}

	public static boolean isVideoTrackExist(Context context, String id) {
		synchronized (mDBOHandlerMutext) {
			boolean isExist = false;
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Video_Track_Cache_table,
					DataBase.Video_Track_Cache_int,
					DataBase.tables[DataBase.Video_Track_Cache_int][1] + "="
							+ id);
			if (cursor != null) {
				if (cursor.moveToFirst())
					isExist = true;
				cursor.close();
			}
			db.close();
			printLog("isTrackExist ::::: " + isExist);
			return isExist;
		}
	}

	public static String getTrackDetails(Context context, String id) {
		synchronized (mDBOHandlerMutext) {
			String jsonResponse = null;
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Track_Cache_table,
					DataBase.Track_Cache_int,
					DataBase.tables[DataBase.Track_Cache_int][1] + "=" + id);
			if (cursor != null) {
				if (cursor.moveToFirst())
					jsonResponse = cursor.getString(3);
				cursor.close();
			}
			db.close();
			printLog("getTrackDetails ::::: " + jsonResponse);
			return jsonResponse;
		}
	}

	public static CacheState getTrackCacheState(Context context, String id) {
		synchronized (mDBOHandlerMutext) {
			CacheState cacheState = CacheState.NOT_CACHED;
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = null;
			try {
				if (CacheManager.isProUser(context))
					cursor = db
							.fetch(DataBase.Track_Cache_table,
									DataBase.Track_Cache_int,
									DataBase.tables[DataBase.Track_Cache_int][1]
											+ "=" + id);
				else
					cursor = db.freeUserExecute(context, id);

				if (cursor != null) {
					if (cursor.moveToFirst()) {
						cacheState = CacheState.getCacheStateByName(cursor
								.getString(4));

					}
					//cursor.close();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				if(cursor!=null)
				cursor.close();
				if(db!=null)
				db.close();
			}

			printLog("getTrackCacheState ::::: " + cacheState.toString());
			return cacheState;
		}
	}

//	public static CacheState getFreeUserTrackCacheState(Context context, String id) {
//		synchronized (mDBOHandlerMutext) {
//			CacheState cacheState = CacheState.NOT_CACHED;
//			DataBase db = DataBase.getInstance(context);
//			db.open();
//			Cursor cursor = db.freeUserExecute(context, id);
//
//			if (cursor != null) {
//				if (cursor.moveToFirst()) {
//					cacheState = CacheState.getCacheStateByName(cursor
//							.getString(4));
//
//				}
//				cursor.close();
//			}
//			db.close();
//			printLog("getTrackCacheState ::::: " + cacheState.toString());
//			return cacheState;
//		}
//	}

	public static int getTrackCacheProgress(Context context, String id) {
		synchronized (mDBOHandlerMutext) {
			int progress = 0;
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Track_Cache_table,
					DataBase.Track_Cache_int,
					DataBase.tables[DataBase.Track_Cache_int][1] + "=" + id);
			if (cursor != null) {
				if (cursor.moveToFirst())
					progress = cursor.getInt(5);
				cursor.close();
			}
			db.close();
			printLog("getTrackCacheProgress ::::: " + progress);
			return progress;
		}
	}

	public static boolean isAlbumExist(Context context, String id) {
		synchronized (mDBOHandlerMutext) {
			boolean isExist = false;
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Album_Cache_table,
					DataBase.Album_Cache_int,
					DataBase.tables[DataBase.Album_Cache_int][1] + "=" + id);
			if (cursor != null) {
				if (cursor.moveToFirst())
					isExist = true;
				cursor.close();
			}
			db.close();
			printLog("isAlbumExist ::::: " + isExist);
			return isExist;
		}
	}

	public static String getAlbumDetails(Context context, String id) {
		synchronized (mDBOHandlerMutext) {
			String jsonResponse = null;
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Album_Cache_table,
					DataBase.Album_Cache_int,
					DataBase.tables[DataBase.Album_Cache_int][1] + "=" + id);
			if (cursor != null) {
				if (cursor.moveToFirst())
					jsonResponse = cursor.getString(2);
				cursor.close();
			}
			db.close();
			printLog("getAlbumDetails ::::: " + jsonResponse);
			return jsonResponse;
		}
	}

	public static CacheState getAlbumCacheState(Context context, String id) {
		synchronized (mDBOHandlerMutext) {
			CacheState cacheState = CacheState.NOT_CACHED;
			if (!CacheManager.isProUser(context))
				return cacheState;
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Album_Cache_table,
					DataBase.Album_Cache_int,
					DataBase.tables[DataBase.Album_Cache_int][1] + "=" + id);
			if (cursor != null) {
				if (cursor.moveToFirst())
					cacheState = CacheState.getCacheStateByName(cursor
							.getString(3));
				cursor.close();
			}
			db.close();
			if (getAlbumCachedCount(context, id) > 0)
				cacheState = CacheState.CACHED;
			printLog("getAlbumCacheState ::::: " + cacheState.toString());
			return cacheState;
		}
	}

	public static boolean isPlaylistExist(Context context, String id) {
		synchronized (mDBOHandlerMutext) {
			boolean isExist = false;
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Playlist_Cache_table,
					DataBase.Playlist_Cache_int,
					DataBase.tables[DataBase.Playlist_Cache_int][1] + "=" + id);
			if (cursor != null) {
				if (cursor.moveToFirst())
					isExist = true;
				cursor.close();
			}
			db.close();
			printLog("isPlaylistExist ::::: " + isExist);
			return isExist;
		}
	}

	public static String getPlaylistDetails(Context context, String id) {
		synchronized (mDBOHandlerMutext) {
			String jsonResponse = null;
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Playlist_Cache_table,
					DataBase.Playlist_Cache_int,
					DataBase.tables[DataBase.Playlist_Cache_int][1] + "=" + id);
			if (cursor != null) {
				if (cursor.moveToFirst())
					jsonResponse = cursor.getString(2);
				cursor.close();
			}
			db.close();
			printLog("getPlaylistDetails ::::: " + jsonResponse);
			return jsonResponse;
		}
	}

	public static CacheState getPlaylistCacheState(Context context, String id) {
		synchronized (mDBOHandlerMutext) {
			CacheState cacheState = CacheState.NOT_CACHED;
			if (!CacheManager.isProUser(context))
				return cacheState;
			// System.out.println(" ::::::::::::::::::::::: 1");
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Playlist_Cache_table,
					DataBase.Playlist_Cache_int,
					DataBase.tables[DataBase.Playlist_Cache_int][1] + "=" + id);
			if (cursor != null) {
				// System.out.println(" ::::::::::::::::::::::: 2");
				if (cursor.moveToFirst())
					cacheState = CacheState.getCacheStateByName(cursor
							.getString(3));
				cursor.close();
			}
			db.close();
			int count = getPlaylistCachedCount(context, id);
			// System.out.println(" ::::::::::::::::::::::: 3 :: " + count);
			if (count > 0)
				cacheState = CacheState.CACHED;
			printLog("getPlaylistCacheState ::::: " + cacheState.toString());
			return cacheState;
		}
	}

	public static String getTrackPathById(Context context, String id) {
		synchronized (mDBOHandlerMutext) {
			String path = null;
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor;
			if (CacheManager.isProUser(context))
				cursor = db
						.fetch(DataBase.Track_Cache_table,
								DataBase.Track_Cache_int,
								DataBase.tables[DataBase.Track_Cache_int][1]
										+ "=" + id);
			else
				cursor = db.freeUserExecute(context, id);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					try {
						File file = new File(cursor.getString(2));
						if (file.exists()) {
							path = cursor.getString(2);
						} else {
							path = "";
						}
					} catch (Exception e) {
					}
				}
				cursor.close();
			}
			db.close();
			printLog("getTrackPathById ::::: " + path);
			return path;
		}
	}

	public static boolean updateTrack(Context context, String id, String path,
			String jsonResponse, String state) {
		synchronized (mDBOHandlerMutext) {
			boolean isRowUpdated = false;
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Track_Cache_table,
					DataBase.Track_Cache_int,
					DataBase.tables[DataBase.Track_Cache_int][1] + "=" + id);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					ContentValues cv = new ContentValues();
					if (path != null && path.length() > 0)
						cv.put(DataBase.tables[DataBase.Track_Cache_int][2],
								path);
					if (jsonResponse != null && jsonResponse.length() > 0)
						cv.put(DataBase.tables[DataBase.Track_Cache_int][3],
								jsonResponse);
					if (state != null)
						cv.put(DataBase.tables[DataBase.Track_Cache_int][4],
								state);
					isRowUpdated = db.update(DataBase.Track_Cache_table,
							DataBase.Track_Cache_int,
							DataBase.tables[DataBase.Track_Cache_int][1] + "="
									+ id, cv);
				}
				cursor.close();
			}
			db.close();
			printLog("updateTrack ::::: " + isRowUpdated);
			return isRowUpdated;
		}
	}

	public static boolean updateTrackCacheState(Context context, String id,
			String state) {
		synchronized (mDBOHandlerMutext) {
			boolean isRowUpdated = false;
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Track_Cache_table,
					DataBase.Track_Cache_int,
					DataBase.tables[DataBase.Track_Cache_int][1] + "=" + id);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					ContentValues cv = new ContentValues();
					if (state != null)
						cv.put(DataBase.tables[DataBase.Track_Cache_int][4],
								state);
					isRowUpdated = db.update(DataBase.Track_Cache_table,
							DataBase.Track_Cache_int,
							DataBase.tables[DataBase.Track_Cache_int][1] + "="
									+ id, cv);
				}
				cursor.close();
			}
			db.close();
			if (isRowUpdated)
				context.sendBroadcast(new Intent(
						CacheManager.ACTION_CACHE_STATE_UPDATED));
			printLog("updateTrack ::::: " + isRowUpdated);
			return isRowUpdated;
		}
	}

	public static boolean updateTrackCacheProgress(Context context, String id,
			int progress) {
		synchronized (mDBOHandlerMutext) {
			boolean isRowUpdated = false;
			DataBase db = DataBase.getInstance(context);
			db.open();
			// Cursor cursor = db.fetch(DataBase.Track_Cache_table,
			// DataBase.Track_Cache_int,
			// DataBase.tables[DataBase.Track_Cache_int][1] + "=" + id);
			// if (cursor != null) {
			// if (cursor.moveToFirst()) {
			if (progress > 0) {
				ContentValues cv = new ContentValues();
				cv.put(DataBase.tables[DataBase.Track_Cache_int][5], progress);
				isRowUpdated = db
						.update(DataBase.Track_Cache_table,
								DataBase.Track_Cache_int,
								DataBase.tables[DataBase.Track_Cache_int][1]
										+ "=" + id, cv);
			}
			// }
			// cursor.close();
			// }
			db.close();
			printLog("updateTrackCacheProgress ::::: " + isRowUpdated);
			return isRowUpdated;
		}
	}

	public static boolean updateAlbumCache(Context context, String id,
			String jsonResponse, String state) {
		synchronized (mDBOHandlerMutext) {
			boolean isRowUpdated = false;
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Album_Cache_table,
					DataBase.Album_Cache_int,
					DataBase.tables[DataBase.Album_Cache_int][1] + "=" + id);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					ContentValues cv = new ContentValues();
					if (jsonResponse != null)
						cv.put(DataBase.tables[DataBase.Album_Cache_int][2],
								jsonResponse);
					if (state != null)
						cv.put(DataBase.tables[DataBase.Album_Cache_int][3],
								state);
					isRowUpdated = db.update(DataBase.Album_Cache_table,
							DataBase.Album_Cache_int,
							DataBase.tables[DataBase.Album_Cache_int][1] + "="
									+ id, cv);
				}
				cursor.close();
			}
			db.close();
			printLog("updateAlbumCache ::::: " + isRowUpdated);
			return isRowUpdated;
		}
	}

	public static boolean updateAlbumCacheState(Context context, String id,
			String state) {
		synchronized (mDBOHandlerMutext) {
			boolean isRowUpdated = false;
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Album_Cache_table,
					DataBase.Album_Cache_int,
					DataBase.tables[DataBase.Album_Cache_int][1] + "=" + id);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					ContentValues cv = new ContentValues();
					if (state != null)
						cv.put(DataBase.tables[DataBase.Album_Cache_int][3],
								state);
					isRowUpdated = db.update(DataBase.Album_Cache_table,
							DataBase.Album_Cache_int,
							DataBase.tables[DataBase.Album_Cache_int][1] + "="
									+ id, cv);
				}
				cursor.close();
			}
			db.close();
			printLog("updateAlbumCacheState ::::: " + isRowUpdated);
			if (isRowUpdated)
				context.sendBroadcast(new Intent(
						CacheManager.ACTION_CACHE_STATE_UPDATED));
			return isRowUpdated;
		}
	}

	public static boolean updatePlaylistCache(Context context, String id,
			String jsonResponse, String state) {
		synchronized (mDBOHandlerMutext) {
			boolean isRowUpdated = false;
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Playlist_Cache_table,
					DataBase.Playlist_Cache_int,
					DataBase.tables[DataBase.Playlist_Cache_int][1] + "=" + id);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					ContentValues cv = new ContentValues();
					if (jsonResponse != null)
						cv.put(DataBase.tables[DataBase.Playlist_Cache_int][2],
								jsonResponse);
					if (state != null)
						cv.put(DataBase.tables[DataBase.Playlist_Cache_int][3],
								state);
					isRowUpdated = db.update(DataBase.Playlist_Cache_table,
							DataBase.Playlist_Cache_int,
							DataBase.tables[DataBase.Playlist_Cache_int][1]
									+ "=" + id, cv);
				}
				cursor.close();
			}
			db.close();
			printLog("updatePlaylistCache ::::: " + isRowUpdated);
			return isRowUpdated;
		}
	}

	public static boolean updatePlaylistCacheState(Context context, String id,
			String state) {
		synchronized (mDBOHandlerMutext) {
			boolean isRowUpdated = false;
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Playlist_Cache_table,
					DataBase.Playlist_Cache_int,
					DataBase.tables[DataBase.Playlist_Cache_int][1] + "=" + id);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					ContentValues cv = new ContentValues();
					if (state != null)
						cv.put(DataBase.tables[DataBase.Playlist_Cache_int][3],
								state);
					isRowUpdated = db.update(DataBase.Playlist_Cache_table,
							DataBase.Playlist_Cache_int,
							DataBase.tables[DataBase.Playlist_Cache_int][1]
									+ "=" + id, cv);
				}
				cursor.close();
			}
			db.close();
			printLog("updatePlaylistCacheState ::::: " + isRowUpdated);
			if (isRowUpdated)
				context.sendBroadcast(new Intent(
						CacheManager.ACTION_CACHE_STATE_UPDATED));
			return isRowUpdated;
		}
	}

	// public static void printAllTracks(Context context) {
	// synchronized (mDBOHandlerMutext) {
	// if (debug) {
	// DataBase db = DataBase.getInstance(context);
	// db.open();
	// Cursor cursor = db.fetchAll(DataBase.Track_Cache_table,
	// DataBase.Track_Cache_int);
	// if (cursor != null) {
	// printLog("Tracks cursor length ::: " + cursor.getCount());
	// while (cursor.moveToNext()) {
	// printLog("------------------------- : "
	// + cursor.getPosition());
	// printLog(cursor.getColumnName(1) + " :::::: "
	// + cursor.getString(1));
	// printLog(cursor.getColumnName(2) + " :::::: "
	// + cursor.getString(2));
	// if (cursor.getString(2) != null) {
	// File file = new File(cursor.getString(2));
	// Logger.s(" file exist::::::::: " + file.exists());
	// }
	// printLog(cursor.getColumnName(3) + " :::::: "
	// + cursor.getString(3));
	// printLog(cursor.getColumnName(4) + " :::::: "
	// + cursor.getString(4));
	// printLog("-------------------------");
	// }
	// cursor.close();
	// }
	// db.close();
	// }
	// }
	// }

	// public static void printAllAlbums(Context context) {
	// synchronized (mDBOHandlerMutext) {
	// if (debug) {
	// DataBase db = DataBase.getInstance(context);
	// db.open();
	// Cursor cursor = db.fetchAll(DataBase.Album_Cache_table,
	// DataBase.Album_Cache_int);
	// if (cursor != null) {
	// printLog("Albums cursor length ::: " + cursor.getCount());
	// while (cursor.moveToNext()) {
	// printLog("------------------------- : "
	// + cursor.getPosition());
	// printLog(cursor.getColumnName(1) + " :::::: "
	// + cursor.getString(1));
	// printLog(cursor.getColumnName(2) + " :::::: "
	// + cursor.getString(2));
	// printLog(cursor.getColumnName(3) + " :::::: "
	// + cursor.getString(3));
	// printLog("-------------------------");
	// }
	// cursor.close();
	// }
	// db.close();
	// }
	// }
	// }

	// public static void printAllPlaylists(Context context) {
	// synchronized (mDBOHandlerMutext) {
	// if (debug) {
	// DataBase db = DataBase.getInstance(context);
	// db.open();
	// Cursor cursor = db.fetchAll(DataBase.Playlist_Cache_table,
	// DataBase.Playlist_Cache_int);
	// if (cursor != null) {
	// printLog("Playlists cursor length ::: " + cursor.getCount());
	// while (cursor.moveToNext()) {
	// printLog("------------------------- : "
	// + cursor.getPosition());
	// printLog(cursor.getColumnName(1) + " :::::: "
	// + cursor.getString(1));
	// printLog(cursor.getColumnName(2) + " :::::: "
	// + cursor.getString(2));
	// printLog(cursor.getColumnName(3) + " :::::: "
	// + cursor.getString(3));
	// printLog("-------------------------");
	// }
	// cursor.close();
	// }
	// db.close();
	// }
	// }
	// }

	// public static void printAllVideoTracks(Context context) {
	// synchronized (mDBOHandlerMutext) {
	// if (debug) {
	// DataBase db = DataBase.getInstance(context);
	// db.open();
	// Cursor cursor = db.fetchAll(DataBase.Video_Track_Cache_table,
	// DataBase.Video_Track_Cache_int);
	// if (cursor != null) {
	// printLog("Video Tracks cursor length ::: "
	// + cursor.getCount());
	// while (cursor.moveToNext()) {
	// printLog("------------------------- : "
	// + cursor.getPosition());
	// printLog(cursor.getColumnName(1) + " :::::: "
	// + cursor.getString(1));
	// printLog(cursor.getColumnName(2) + " :::::: "
	// + cursor.getString(2));
	// printLog(cursor.getColumnName(3) + " :::::: "
	// + cursor.getString(3));
	// printLog(cursor.getColumnName(4) + " :::::: "
	// + cursor.getString(4));
	// printLog("-------------------------");
	// }
	// cursor.close();
	// }
	// db.close();
	// }
	// }
	// }

	// public static boolean updateTrackListState(Context context, String id,
	// String state) {
	// boolean isRowUpdated = false;
	// DataBase db = DataBase.getInstance(context);
	// db.open();
	// Cursor cursor = db.fetch(DataBase.Tracklist_table,
	// DataBase.Tracklist_int,
	// DataBase.tables[DataBase.Tracklist_int][1] + "=" + id);
	// if (cursor != null) {
	// if (cursor.moveToFirst()) {
	// String album_id = cursor.getString(2);
	// String playlist_id = cursor.getString(3);
	// if (!album_id.equals("0")) {
	// Cursor cursor1 = db
	// .fetch(DataBase.Track_Cache_table,
	// DataBase.Track_Cache_int,
	// DataBase.tables[DataBase.Track_Cache_int][1]
	// + " in (Select "
	// + DataBase.tables[DataBase.Tracklist_int][1]
	// + " from "
	// + DataBase.Tracklist_table
	// + " where "
	// + DataBase.tables[DataBase.Tracklist_int][2]
	// + " = "
	// + album_id
	// + " ) AND "
	// + DataBase.tables[DataBase.Track_Cache_int][4]
	// + " <> '"
	// + DataBase.CacheState.CACHED
	// .toString() + "'");
	// if (cursor1 != null) {
	// printLog("Cursor size :::::: " + cursor1.getCount());
	// if (cursor1.getCount() == 0) {
	// updateAlbumCacheState(context, album_id,
	// DataBase.CacheState.CACHED.toString());
	// } else {
	// if (cursor1.moveToFirst()) {
	// do {
	// printLog(cursor1.getPosition() + " :::: "
	// + cursor1.getString(1) + " :::::: "
	// + cursor1.getString(4));
	// } while (cursor1.moveToNext());
	// }
	// }
	// cursor1.close();
	// }
	// } else if (!playlist_id.equals("0")) {
	// Cursor cursor1 = db
	// .fetch(DataBase.Track_Cache_table,
	// DataBase.Track_Cache_int,
	// DataBase.tables[DataBase.Track_Cache_int][1]
	// + " in (Select "
	// + DataBase.tables[DataBase.Tracklist_int][1]
	// + " from "
	// + DataBase.Tracklist_table
	// + " where "
	// + DataBase.tables[DataBase.Tracklist_int][3]
	// + " = "
	// + playlist_id
	// + " ) AND "
	// + DataBase.tables[DataBase.Track_Cache_int][4]
	// + " <> '"
	// + DataBase.CacheState.CACHED
	// .toString() + "'");
	// if (cursor1 != null) {
	// printLog("Cursor size :::::: " + cursor1.getCount());
	// if (cursor1.getCount() == 0) {
	// updatePlaylistCacheState(context, playlist_id,
	// DataBase.CacheState.CACHED.toString());
	// } else {
	// if (cursor1.moveToFirst()) {
	// do {
	// printLog(cursor1.getPosition() + " :::: "
	// + cursor1.getString(1) + " :::::: "
	// + cursor1.getString(4));
	// } while (cursor1.moveToNext());
	// }
	// }
	// cursor1.close();
	// }
	// }
	// db.delete(DataBase.Tracklist_table, DataBase.Tracklist_int,
	// cursor.getInt(0));
	// }
	// cursor.close();
	// }
	// db.close();
	// printLog("updateTrack ::::: " + isRowUpdated);
	// return isRowUpdated;
	// }
	//
	// public static boolean updateTrackListFailedState(Context context, String
	// id,
	// String state) {
	// boolean isRowUpdated = false;
	// DataBase db = DataBase.getInstance(context);
	// db.open();
	// Cursor cursor = db.fetch(DataBase.Tracklist_table,
	// DataBase.Tracklist_int,
	// DataBase.tables[DataBase.Tracklist_int][1] + "=" + id);
	// if (cursor != null) {
	// if (cursor.moveToFirst()) {
	// String album_id = cursor.getString(2);
	// String playlist_id = cursor.getString(3);
	// if (!album_id.equals("0")) {
	// Cursor cursor1 = db
	// .fetch(DataBase.Track_Cache_table,
	// DataBase.Track_Cache_int,
	// DataBase.tables[DataBase.Track_Cache_int][1]
	// + " in (Select "
	// + DataBase.tables[DataBase.Tracklist_int][1]
	// + " from "
	// + DataBase.Tracklist_table
	// + " where "
	// + DataBase.tables[DataBase.Tracklist_int][2]
	// + " = "
	// + album_id
	// + " ) AND "
	// + DataBase.tables[DataBase.Track_Cache_int][4]
	// + " <> '"
	// + DataBase.CacheState.NOT_CACHED
	// .toString() + "'");
	// if (cursor1 != null) {
	// printLog("Cursor size :::::: " + cursor1.getCount());
	// if (cursor1.getCount() == 0) {
	// updateAlbumCacheState(context, album_id,
	// DataBase.CacheState.NOT_CACHED.toString());
	// } else {
	// if (cursor1.moveToFirst()) {
	// do {
	// printLog(cursor1.getPosition() + " :::: "
	// + cursor1.getString(1) + " :::::: "
	// + cursor1.getString(4));
	// } while (cursor1.moveToNext());
	// }
	// }
	// cursor1.close();
	// }
	// } else if (!playlist_id.equals("0")) {
	// Cursor cursor1 = db
	// .fetch(DataBase.Track_Cache_table,
	// DataBase.Track_Cache_int,
	// DataBase.tables[DataBase.Track_Cache_int][1]
	// + " in (Select "
	// + DataBase.tables[DataBase.Tracklist_int][1]
	// + " from "
	// + DataBase.Tracklist_table
	// + " where "
	// + DataBase.tables[DataBase.Tracklist_int][3]
	// + " = "
	// + playlist_id
	// + " ) AND "
	// + DataBase.tables[DataBase.Track_Cache_int][4]
	// + " <> '"
	// + DataBase.CacheState.NOT_CACHED
	// .toString() + "'");
	// if (cursor1 != null) {
	// printLog("Cursor size :::::: " + cursor1.getCount());
	// if (cursor1.getCount() == 0) {
	// updatePlaylistCacheState(context, playlist_id,
	// DataBase.CacheState.NOT_CACHED.toString());
	// } else {
	// if (cursor1.moveToFirst()) {
	// do {
	// printLog(cursor1.getPosition() + " :::: "
	// + cursor1.getString(1) + " :::::: "
	// + cursor1.getString(4));
	// } while (cursor1.moveToNext());
	// }
	// }
	// cursor1.close();
	// }
	// }
	// db.delete(DataBase.Tracklist_table, DataBase.Tracklist_int,
	// cursor.getInt(0));
	// }
	// cursor.close();
	// }
	// db.close();
	// printLog("updateTrack ::::: " + isRowUpdated);
	// return isRowUpdated;
	// }

	public static boolean updateTrackListState(Context context, String id,
			String state) {
		synchronized (mDBOHandlerMutext) {
			boolean isRowUpdated = false;
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Tracklist_table,
					DataBase.Tracklist_int,
					DataBase.tables[DataBase.Tracklist_int][1] + "=" + id);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					String album_id = cursor.getString(2);
					String playlist_id = cursor.getString(3);
					if (!album_id.equals("0")) {
						Cursor cursor1 = db
								.fetch(DataBase.Track_Cache_table,
										DataBase.Track_Cache_int,
										DataBase.tables[DataBase.Track_Cache_int][1]
												+ " in (Select "
												+ DataBase.tables[DataBase.Tracklist_int][1]
												+ " from "
												+ DataBase.Tracklist_table
												+ " where "
												+ DataBase.tables[DataBase.Tracklist_int][2]
												+ " = "
												+ album_id
												+ " ) AND "
												+ DataBase.tables[DataBase.Track_Cache_int][4]
												+ " <> '"
												+ CacheState.CACHED
														.toString() + "'");
						if (cursor1 != null) {
							printLog("Cursor size :::::: " + cursor1.getCount());
							if (cursor1.getCount() == 0) {
								updateAlbumCacheState(context, album_id,
										CacheState.CACHED.toString());
							} else {
								if (cursor1.moveToFirst()) {
									do {
										printLog(cursor1.getPosition()
												+ " :::: "
												+ cursor1.getString(1)
												+ " :::::: "
												+ cursor1.getString(4));
									} while (cursor1.moveToNext());
								}
							}
							cursor1.close();
						}
					} else if (!playlist_id.equals("0")) {
						Cursor cursor1 = db
								.fetch(DataBase.Track_Cache_table,
										DataBase.Track_Cache_int,
										DataBase.tables[DataBase.Track_Cache_int][1]
												+ " in (Select "
												+ DataBase.tables[DataBase.Tracklist_int][1]
												+ " from "
												+ DataBase.Tracklist_table
												+ " where "
												+ DataBase.tables[DataBase.Tracklist_int][3]
												+ " = "
												+ playlist_id
												+ " ) AND "
												+ DataBase.tables[DataBase.Track_Cache_int][4]
												+ " <> '"
												+ CacheState.CACHED
														.toString() + "'");
						if (cursor1 != null) {
							printLog("Cursor size :::::: " + cursor1.getCount());
							if (cursor1.getCount() == 0) {
								updatePlaylistCacheState(context, playlist_id,
										CacheState.CACHED.toString());
							} else {
								if (cursor1.moveToFirst()) {
									do {
										printLog(cursor1.getPosition()
												+ " :::: "
												+ cursor1.getString(1)
												+ " :::::: "
												+ cursor1.getString(4));
									} while (cursor1.moveToNext());
								}
							}
							cursor1.close();
						}
					}
					// db.delete(DataBase.Tracklist_table,
					// DataBase.Tracklist_int,
					// cursor.getInt(0));
				}
				cursor.close();
			}
			db.close();
			printLog("updateTrack ::::: " + isRowUpdated);
			return isRowUpdated;
		}
	}

	public static boolean updateTrackListFailedState(Context context,
			String id, String state) {
		synchronized (mDBOHandlerMutext) {
			boolean isRowUpdated = false;
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Tracklist_table,
					DataBase.Tracklist_int,
					DataBase.tables[DataBase.Tracklist_int][1] + "=" + id);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					String album_id = cursor.getString(2);
					String playlist_id = cursor.getString(3);
					if (!album_id.equals("0")) {
						Cursor cursor1 = db
								.fetch(DataBase.Track_Cache_table,
										DataBase.Track_Cache_int,
										DataBase.tables[DataBase.Track_Cache_int][1]
												+ " in (Select "
												+ DataBase.tables[DataBase.Tracklist_int][1]
												+ " from "
												+ DataBase.Tracklist_table
												+ " where "
												+ DataBase.tables[DataBase.Tracklist_int][2]
												+ " = "
												+ album_id
												+ " ) AND "
												+ DataBase.tables[DataBase.Track_Cache_int][4]
												+ " <> '"
												+ CacheState.NOT_CACHED
														.toString() + "'");
						if (cursor1 != null) {
							printLog("Cursor size :::::: " + cursor1.getCount());
							if (cursor1.getCount() == 0) {
								updateAlbumCacheState(context, album_id,
										CacheState.NOT_CACHED
												.toString());
							} else {
								if (cursor1.moveToFirst()) {
									do {
										printLog(cursor1.getPosition()
												+ " :::: "
												+ cursor1.getString(1)
												+ " :::::: "
												+ cursor1.getString(4));
									} while (cursor1.moveToNext());
								}
							}
							cursor1.close();
						}
					} else if (!playlist_id.equals("0")) {
						Cursor cursor1 = db
								.fetch(DataBase.Track_Cache_table,
										DataBase.Track_Cache_int,
										DataBase.tables[DataBase.Track_Cache_int][1]
												+ " in (Select "
												+ DataBase.tables[DataBase.Tracklist_int][1]
												+ " from "
												+ DataBase.Tracklist_table
												+ " where "
												+ DataBase.tables[DataBase.Tracklist_int][3]
												+ " = "
												+ playlist_id
												+ " ) AND "
												+ DataBase.tables[DataBase.Track_Cache_int][4]
												+ " <> '"
												+ CacheState.NOT_CACHED
														.toString() + "'");
						if (cursor1 != null) {
							printLog("Cursor size :::::: " + cursor1.getCount());
							if (cursor1.getCount() == 0) {
								updatePlaylistCacheState(context, playlist_id,
										CacheState.NOT_CACHED
												.toString());
							} else {
								if (cursor1.moveToFirst()) {
									do {
										printLog(cursor1.getPosition()
												+ " :::: "
												+ cursor1.getString(1)
												+ " :::::: "
												+ cursor1.getString(4));
									} while (cursor1.moveToNext());
								}
							}
							cursor1.close();
						}
					}
					// db.delete(DataBase.Tracklist_table,
					// DataBase.Tracklist_int,
					// cursor.getInt(0));
				}
				cursor.close();
			}
			db.close();
			printLog("updateTrack ::::: " + isRowUpdated);
			return isRowUpdated;
		}
	}

	private static void printLog(String message) {
		if (debug)
			System.out.println(message);
	}

	public static boolean updateVideoTrack(Context context, String id,
			String path, String jsonResponse, String state) {
		synchronized (mDBOHandlerMutext) {
			boolean isRowUpdated = false;
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Video_Track_Cache_table,
					DataBase.Video_Track_Cache_int,
					DataBase.tables[DataBase.Video_Track_Cache_int][1] + "="
							+ id);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					ContentValues cv = new ContentValues();
					if (path != null && path.length() > 0)
						cv.put(DataBase.tables[DataBase.Video_Track_Cache_int][2],
								path);
					if (jsonResponse != null && jsonResponse.length() > 0)
						cv.put(DataBase.tables[DataBase.Video_Track_Cache_int][3],
								jsonResponse);
					if (state != null)
						cv.put(DataBase.tables[DataBase.Video_Track_Cache_int][4],
								state);
					isRowUpdated = db.update(DataBase.Video_Track_Cache_table,
							DataBase.Video_Track_Cache_int,
							DataBase.tables[DataBase.Video_Track_Cache_int][1]
									+ "=" + id, cv);
				}
				cursor.close();
			}
			db.close();
			printLog("updateTrack ::::: " + isRowUpdated);
			return isRowUpdated;
		}
	}

	public static boolean updateVideoTrackCacheState(Context context,
			String id, String state) {
		synchronized (mDBOHandlerMutext) {
			boolean isRowUpdated = false;
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Video_Track_Cache_table,
					DataBase.Video_Track_Cache_int,
					DataBase.tables[DataBase.Video_Track_Cache_int][1] + "="
							+ id);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					ContentValues cv = new ContentValues();
					if (state != null)
						cv.put(DataBase.tables[DataBase.Video_Track_Cache_int][4],
								state);
					isRowUpdated = db.update(DataBase.Video_Track_Cache_table,
							DataBase.Video_Track_Cache_int,
							DataBase.tables[DataBase.Video_Track_Cache_int][1]
									+ "=" + id, cv);
				}
				cursor.close();
			}
			db.close();
			if (isRowUpdated)
				context.sendBroadcast(new Intent(
						CacheManager.ACTION_VIDEO_CACHE_STATE_UPDATED));
			printLog("updateTrack ::::: " + isRowUpdated);
			return isRowUpdated;
		}
	}

	public static boolean updateVideoTrackCacheProgress(Context context,
			String id, int progress) {
		synchronized (mDBOHandlerMutext) {
			boolean isRowUpdated = false;
			DataBase db = DataBase.getInstance(context);
			db.open();
			// Cursor cursor = db.fetch(DataBase.Track_Cache_table,
			// DataBase.Track_Cache_int,
			// DataBase.tables[DataBase.Track_Cache_int][1] + "=" + id);
			// if (cursor != null) {
			// if (cursor.moveToFirst()) {
			if (progress > 0) {
				ContentValues cv = new ContentValues();
				cv.put(DataBase.tables[DataBase.Video_Track_Cache_int][5],
						progress);
				isRowUpdated = db.update(DataBase.Video_Track_Cache_table,
						DataBase.Video_Track_Cache_int,
						DataBase.tables[DataBase.Video_Track_Cache_int][1]
								+ "=" + id, cv);
			}
			// }
			// cursor.close();
			// }
			db.close();
			printLog("updateVideoTrackCacheProgress ::::: " + isRowUpdated);
			return isRowUpdated;
		}
	}

	public static CacheState getVideoTrackCacheState(Context context, String id) {
		synchronized (mDBOHandlerMutext) {
			CacheState cacheState = CacheState.NOT_CACHED;
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Video_Track_Cache_table,
					DataBase.Video_Track_Cache_int,
					DataBase.tables[DataBase.Video_Track_Cache_int][1] + "="
							+ id);
			if (cursor != null) {
				if (cursor.moveToFirst())
					cacheState = CacheState.getCacheStateByName(cursor
							.getString(4));
				cursor.close();
			}
			db.close();
			printLog("getTrackCacheState ::::: " + cacheState.toString());
			return cacheState;
		}
	}

	public static int getVideoTrackCacheProgress(Context context, String id) {
		synchronized (mDBOHandlerMutext) {
			int progress = 0;
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Video_Track_Cache_table,
					DataBase.Video_Track_Cache_int,
					DataBase.tables[DataBase.Video_Track_Cache_int][1] + "="
							+ id);
			if (cursor != null) {
				if (cursor.moveToFirst())
					progress = cursor.getInt(5);
				cursor.close();
			}
			db.close();
			printLog("getVideoTrackCacheProgress ::::: " + progress);
			return progress;
		}
	}

	public static String getVideoTrackPathById(Context context, String id) {
		synchronized (mDBOHandlerMutext) {
			String path = null;
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Video_Track_Cache_table,
					DataBase.Video_Track_Cache_int,
					DataBase.tables[DataBase.Video_Track_Cache_int][1] + "="
							+ id);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					File file = new File(cursor.getString(2));
					if (file.exists()) {
						path = cursor.getString(2);
					} else {
						path = "";
					}
				}
				cursor.close();
			}
			db.close();
			printLog("getTrackPathById ::::: " + path);
			return path;
		}
	}

	public static String getVideoTrackDetails(Context context, String id) {
		synchronized (mDBOHandlerMutext) {
			String jsonResponse = null;
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Video_Track_Cache_table,
					DataBase.Video_Track_Cache_int,
					DataBase.tables[DataBase.Video_Track_Cache_int][1] + "="
							+ id);
			if (cursor != null) {
				if (cursor.moveToFirst())
					jsonResponse = cursor.getString(3);
				cursor.close();
			}
			db.close();
			printLog("getTrackDetails ::::: " + jsonResponse);
			return jsonResponse;
		}
	}

	public static List<MediaItem> getAllTracks(Context context) {
		synchronized (mDBOHandlerMutext) {
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetchAll(DataBase.Track_Cache_table,
					DataBase.Track_Cache_int,
					DataBase.tables[DataBase.Track_Cache_int][0] + " DESC");
			List<MediaItem> mMediaItems = new ArrayList<MediaItem>();
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					do {
						try {
							MediaItem mediaItem;
							String response = cursor.getString(3);
							try {
								// response = response.replace(
								// "{\"catalog\":{\"content\":", "");
								// response = response.substring(0,
								// response.length() - 2);
								JSONObject jsonResponse = new JSONObject(
										response);
								if (jsonResponse.has("catalog")) {
									response = jsonResponse
											.getJSONObject("catalog")
											.getJSONObject("content")
											.toString();
								} else {
									response = jsonResponse.getJSONObject(
											"response").toString();
								}

								Gson gson = new GsonBuilder()
										.excludeFieldsWithoutExposeAnnotation()
										.create();
								MediaTrackDetails trackDetails = (MediaTrackDetails) gson
										.fromJson(response,
												MediaTrackDetails.class);
								// Track track = new Track(trackDetails.getId(),
								// trackDetails.getTitle(),
								// trackDetails.getAlbumName(),
								// trackDetails.getSingers(),
								// trackDetails.getImageUrl(),
								// trackDetails.getBigImageUrl());
								mediaItem = new MediaItem(trackDetails.getId(),
										trackDetails.getTitle(),
										trackDetails.getAlbumName(),
										trackDetails.getSingers(),
										trackDetails.getImageUrl(),
										trackDetails.getBigImageUrl(),
										MediaType.TRACK.toString(), 0, 0,
										trackDetails.getImages(),
										trackDetails.getAlbumId());
							} catch (Exception e) {
								mediaItem = new MediaItem(Long.parseLong(cursor
										.getString(1)), "", "", "", "", "",
										MediaType.TRACK.name().toLowerCase(),
										0, 0);
							} catch (Error e) {
								mediaItem = new MediaItem(Long.parseLong(cursor
										.getString(1)), "", "", "", "", "",
										MediaType.TRACK.name().toLowerCase(),
										0, 0);
							}

							mediaItem
									.setMediaContentType(MediaContentType.MUSIC);
							mMediaItems.add(mediaItem);
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}catch (Error e) {
                            Logger.printStackTrace(e);
                        }
					} while (cursor.moveToNext());
				}
				cursor.close();
			}
			db.close();
			if (cursor != null)
				printLog("getAllTracks ::::: " + mMediaItems.size());
			return mMediaItems;// cursor;
		}
	}

	public static List<MediaItem> getAllCachedTracks(Context context) {
		synchronized (mDBOHandlerMutext) {
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Track_Cache_table,
					DataBase.Track_Cache_int,
					DataBase.tables[DataBase.Track_Cache_int][4] + "='"
							+ CacheState.CACHED.toString() + "'",
					DataBase.tables[DataBase.Track_Cache_int][0] + " DESC");
			List<MediaItem> mMediaItems = new ArrayList<MediaItem>();
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					do {
						try {
							MediaItem mediaItem;
							String response = cursor.getString(3);
							Logger.i("response", "getAllCachedTracks: "
									+ response);
							try {
								// response = response.replace("\"response\":{",
								// "");// {\"response\":{\"content\":
								// response = response.substring(0,
								// response.length() - 1);
								response = new JSONObject(response)
										.getJSONObject("response").toString();
								// response = response.replace(
								// "{\"catalog\":{\"content\":", "");
								// response = response.substring(0,
								// response.length() - 2);

								Gson gson = new GsonBuilder()
										.excludeFieldsWithoutExposeAnnotation()
										.create();
								MediaTrackDetails trackDetails = (MediaTrackDetails) gson
										.fromJson(response,
												MediaTrackDetails.class);
								// Track track = new Track(trackDetails.getId(),
								// trackDetails.getTitle(),
								// trackDetails.getAlbumName(),
								// trackDetails.getSingers(),
								// trackDetails.getImageUrl(),
								// trackDetails.getBigImageUrl());

								// String imageurl = null;
								// String[] images =
								// ImagesManager.getImagesUrlArray(
								// trackDetails.getImagesUrlArray(),
								// ImagesManager.MUSIC_ART_SMALL,
								// DataManager.getDisplayDensityLabel());
								// if (images != null && images.length > 0)
								// imageurl=images[0];
								//
								// String imageurlBig;
								// String[] imagesbig =
								// ImagesManager.getImagesUrlArray(
								// trackDetails.getImagesUrlArray(),
								// ImagesManager.MUSIC_ART_SMALL,
								// DataManager.getDisplayDensityLabel());
								// if (imagesbig != null && imagesbig.length >
								// 0)
								// imageurlBig=images[0];

								// mediaItem = new
								// MediaItem(trackDetails.getId(),
								// trackDetails.getTitle(),
								// trackDetails.getAlbumName(),
								// trackDetails.getSingers(),
								// trackDetails.getImageUrl(),
								// trackDetails.getBigImageUrl(),
								// MediaType.TRACK.name().toLowerCase(),
								// 0, trackDetails.getAlbumId());

								mediaItem = new MediaItem(trackDetails.getId(),
										trackDetails.getTitle(),
										trackDetails.getAlbumName(),
										trackDetails.getSingers(),
										trackDetails.getImageUrl(),
										trackDetails.getBigImageUrl(),
										MediaType.TRACK.toString(), 0, 0,
										trackDetails.getImages(),
										trackDetails.getAlbumId());

							} catch (Exception e) {
								mediaItem = new MediaItem(Long.parseLong(cursor
										.getString(1)), "", "", "", "", "",
										MediaType.TRACK.name().toLowerCase(),
										0, 0);
							}catch (Error e) {
                                mediaItem = new MediaItem(Long.parseLong(cursor
                                        .getString(1)), "", "", "", "", "",
                                        MediaType.TRACK.name().toLowerCase(),
                                        0, 0);
                            }

							mediaItem
									.setMediaContentType(MediaContentType.MUSIC);
							mMediaItems.add(mediaItem);
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
					} while (cursor.moveToNext());
				}
				cursor.close();
			}
			db.close();
			if (cursor != null)
				printLog("getAllTracks ::::: " + mMediaItems.size());
			return mMediaItems;// cursor;
		}
	}

	public static List<MediaItem> getAllTracksForFreeUser(Context context) {
		synchronized (mDBOHandlerMutext) {
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Track_Cache_table,
					DataBase.Track_Cache_int,
					DataBase.tables[DataBase.Track_Cache_int][4] + "='"
							+ CacheState.CACHED.toString() + "' AND "
							+ DataBase.tables[DataBase.Track_Cache_int][0]
							+ "<=3",
					DataBase.tables[DataBase.Track_Cache_int][0]);
			List<MediaItem> mMediaItems = new ArrayList<MediaItem>();
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					do {
						try {
							MediaItem mediaItem;
							String response = cursor.getString(3);
							try {

								// response = response.replace("\"response\":{",
								// "");//{\"response\":{\"content\":
								// response = response.substring(0,
								// response.length() - 1);
								// response = response.replace(
								// "{\"catalog\":{\"content\":", "");
								// response = response.substring(0,
								// response.length() - 2);

								response = new JSONObject(response)
										.getJSONObject("response").toString();

								Gson gson = new GsonBuilder()
										.excludeFieldsWithoutExposeAnnotation()
										.create();
								MediaTrackDetails trackDetails = (MediaTrackDetails) gson
										.fromJson(response,
												MediaTrackDetails.class);
								// Track track = new Track(trackDetails.getId(),
								// trackDetails.getTitle(),
								// trackDetails.getAlbumName(),
								// trackDetails.getSingers(),
								// trackDetails.getImageUrl(),
								// trackDetails.getBigImageUrl());
								mediaItem = new MediaItem(trackDetails.getId(),
										trackDetails.getTitle(),
										trackDetails.getAlbumName(),
										trackDetails.getSingers(),
										trackDetails.getImageUrl(),
										trackDetails.getBigImageUrl(),
										MediaType.TRACK.name().toLowerCase(),
										0, trackDetails.getAlbumId());
							} catch (Exception e) {
								mediaItem = new MediaItem(Long.parseLong(cursor
										.getString(1)), "", "", "", "", "",
										MediaType.TRACK.name().toLowerCase(),
										0, 0);
							}catch (Error e) {
                                mediaItem = new MediaItem(Long.parseLong(cursor
                                        .getString(1)), "", "", "", "", "",
                                        MediaType.TRACK.name().toLowerCase(),
                                        0, 0);
                            }

							mediaItem
									.setMediaContentType(MediaContentType.MUSIC);
							mMediaItems.add(mediaItem);
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
					} while (cursor.moveToNext() && cursor.getPosition() < 3);
				}
				cursor.close();
			}
			db.close();
			if (cursor != null)
				printLog("getAllTracks ::::: " + mMediaItems.size());
			return mMediaItems;// cursor;
		}
	}

	public static List<MediaItem> getAllOfflineTracksForFreeUser(Context context) {
		synchronized (mDBOHandlerMutext) {
			DataBase db = DataBase.getInstance(context);
			db.open();
			 Cursor cursor = db.fetch(DataBase.Track_Cache_table,
			 DataBase.Track_Cache_int,
			 DataBase.tables[DataBase.Track_Cache_int][4] + "='"
			 + CacheState.CACHED.toString() + "'",
			 DataBase.tables[DataBase.Track_Cache_int][0]);
			/*Cursor cursor = db.fetch(DataBase.Track_Cache_table,
					DataBase.Track_Cache_int, null,
					DataBase.tables[DataBase.Track_Cache_int][0]);*/
			List<MediaItem> mMediaItems = new ArrayList<MediaItem>();
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					do {
						try {
							MediaItem mediaItem;
							String response = cursor.getString(3);
							try {

								Logger.e("response:******", "" + response);
								// response = response.replace("\"response\":{",
								// "");// {\"response\":{\"content\":
								// response = response.substring(0,
								// response.length() - 1);
								response = new JSONObject(response)
										.getJSONObject("response").toString();
								// response = response.replace(
								// "{\"catalog\":{\"content\":", "");
								// response = response.substring(0,
								// response.length() - 2);

								Gson gson = new GsonBuilder()
										.excludeFieldsWithoutExposeAnnotation()
										.create();
								MediaTrackDetails trackDetails = (MediaTrackDetails) gson
										.fromJson(response,
												MediaTrackDetails.class);
								// Track track = new Track(trackDetails.getId(),
								// trackDetails.getTitle(),
								// trackDetails.getAlbumName(),
								// trackDetails.getSingers(),
								// trackDetails.getImageUrl(),
								// trackDetails.getBigImageUrl());
								// mediaItem = new
								// MediaItem(trackDetails.getId(),
								// trackDetails.getTitle(),
								// trackDetails.getAlbumName(),
								// trackDetails.getSingers(),
								// trackDetails.getImageUrl(),
								// trackDetails.getBigImageUrl(),
								// MediaType.TRACK.name().toLowerCase(),
								// 0, trackDetails.getAlbumId());
								mediaItem = new MediaItem(trackDetails.getId(),
										trackDetails.getTitle(),
										trackDetails.getAlbumName(),
										trackDetails.getSingers(),
										trackDetails.getImageUrl(),
										trackDetails.getBigImageUrl(),
										MediaType.TRACK.toString(), 0, 0,
										trackDetails.getImages(),
										trackDetails.getAlbumId());
							} catch (Exception e) {
								mediaItem = new MediaItem(Long.parseLong(cursor
										.getString(1)), "", "", "", "", "",
										MediaType.TRACK.name().toLowerCase(),
										0, 0);
							}catch (Error e) {
                                mediaItem = new MediaItem(Long.parseLong(cursor
                                        .getString(1)), "", "", "", "", "",
                                        MediaType.TRACK.name().toLowerCase(),
                                        0, 0);
                            }

							mediaItem
									.setMediaContentType(MediaContentType.MUSIC);
							mMediaItems.add(mediaItem);
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
					} while (cursor.moveToNext()/* && cursor.getPosition() < 3 */);
				}
				cursor.close();
			}
			db.close();
			if (cursor != null)
				printLog("getAllTracks ::::: " + mMediaItems.size());
			return mMediaItems;// cursor;
		}
	}

	// public static List<MediaItem> getSongCatcherHistory(Context context) {
	// synchronized (mDBOHandlerMutext) {
	// DataBase db = DataBase.getInstance(context);
	// db.open();
	// Cursor cursor = db.fetch(DataBase.SongCatcher_History_table,
	// DataBase.SongCatcher_History_int, null, "timestamp DESC");
	// List<MediaItem> mMediaItems = new ArrayList<MediaItem>();
	// if (cursor != null) {
	// if (cursor.moveToFirst()) {
	// do {
	// try {
	// MediaItem mediaItem;
	// String response = cursor.getString(3);
	// try {
	// response = response.replace(
	// "{\"catalog\":{\"content\":", "");
	// response = response.substring(0,
	// response.length() - 3);
	// response = response.substring(1);
	// Gson gson = new GsonBuilder()
	// .excludeFieldsWithoutExposeAnnotation()
	// .create();
	// MediaTrackDetails trackDetails = (MediaTrackDetails) gson
	// .fromJson(response,
	// MediaTrackDetails.class);
	// // Track track = new Track(trackDetails.getId(),
	// // trackDetails.getTitle(),
	// // trackDetails.getAlbumName(),
	// // trackDetails.getSingers(),
	// // trackDetails.getImageUrl(),
	// // trackDetails.getBigImageUrl());
	//
	// // public MediaItem(long id, String title,
	// // String albumName, String artistName,
	// // String imageUrl, String bigImageUrl, String
	// // type, int musicTrackCount) {
	// mediaItem = new MediaItem(trackDetails.getId(),
	// trackDetails.getTitle(),
	// trackDetails.getAlbumName(),
	// trackDetails.getAlbumName(),
	// trackDetails.getImageUrl(),
	// trackDetails.getBigImageUrl(),
	// MediaType.TRACK.name().toLowerCase(), 0,
	// trackDetails.getAlbumId());
	// } catch (Exception e) {
	// mediaItem = new MediaItem(Long.parseLong(cursor
	// .getString(1)), "", "", "", "", "",
	// MediaType.TRACK.name().toLowerCase(), 0, 0);
	// }
	//
	// mediaItem
	// .setMediaContentType(MediaContentType.MUSIC);
	// mMediaItems.add(mediaItem);
	// } catch (Exception e) {
	// Logger.printStackTrace(e);
	// }
	// } while (cursor.moveToNext() && cursor.getPosition() < 10);
	// }
	// cursor.close();
	// }
	// db.close();
	// if (cursor != null)
	// printLog("getHistory ::::: " + mMediaItems.size());
	// return mMediaItems;// cursor;
	// }
	// }

	public static List<MediaItem> getAllVideoTracks(Context context) {
		synchronized (mDBOHandlerMutext) {
			DataBase db = DataBase.getInstance(context);
			db.open();
			 Cursor cursor = db.fetch(DataBase.Video_Track_Cache_table,
			 DataBase.Video_Track_Cache_int,
			 DataBase.tables[DataBase.Video_Track_Cache_int][4] + "='"
			 + CacheState.CACHED.toString() + "'",
			 DataBase.tables[DataBase.Video_Track_Cache_int][0]
			 + " DESC");
			/*Cursor cursor = db.fetch(DataBase.Video_Track_Cache_table,
					DataBase.Video_Track_Cache_int, null,
					DataBase.tables[DataBase.Video_Track_Cache_int][0]
							+ " DESC");*/
			List<MediaItem> mMediaItems = new ArrayList<MediaItem>();
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					do {
						String response = cursor.getString(3);
						if (response != null && response.length() > 0) {
							try {
								// response = response.replace("\"response\":{",
								// "");//{\"response\":{\"content\":
								// response = new
								// JSONObject(response).getJSONObject(
								// "response").toString();
								JSONObject jsonResponse = new JSONObject(
										response);
								if (jsonResponse.has("catalog")) {
									response = jsonResponse
											.getJSONObject("catalog")
											.getJSONObject("content")
											.toString();
								} else {
									response = jsonResponse.getJSONObject(
											"response").toString();
								}

								//
								// response = response.replace(
								// "{\"catalog\":{\"content\":", "");
								// try {
								// response = response.substring(0,
								// response.length() - 1);
								// } catch (Exception e) {
								// Logger.printStackTrace(e);
								// }
								// try {
								Gson gson = new GsonBuilder()
										.excludeFieldsWithoutExposeAnnotation()
										.create();
								MediaTrackDetails trackDetails = (MediaTrackDetails) gson
										.fromJson(response,
												MediaTrackDetails.class);
								// Track track = new Track(trackDetails.getId(),
								// trackDetails.getTitle(),
								// trackDetails.getAlbumName(),
								// trackDetails.getSingers(),
								// trackDetails.getImageUrl(),
								// trackDetails.getBigImageUrl());
								MediaItem mediaItem = new MediaItem(
										trackDetails.getId(),
										trackDetails.getTitle(),
										trackDetails.getAlbumName(),
										trackDetails.getSingers(),
										trackDetails.getImageUrl(),
										trackDetails.getBigImageUrl(),
										MediaType.VIDEO.name().toLowerCase(),
										0, 0, trackDetails.getImages(),
										trackDetails.getAlbumId());
								mediaItem
										.setMediaContentType(MediaContentType.VIDEO);
								mMediaItems.add(mediaItem);
							} catch (Exception e) {
								Logger.printStackTrace(e);
								MediaItem mediaItem = new MediaItem(
										Long.parseLong(cursor.getString(1)),
										"", "", "", "", "", MediaType.VIDEO
												.name().toLowerCase(), 0, 0);
								mediaItem
										.setMediaContentType(MediaContentType.VIDEO);
								mMediaItems.add(mediaItem);
							}catch (Error e) {
                                Logger.printStackTrace(e);
                                MediaItem mediaItem = new MediaItem(
                                        Long.parseLong(cursor.getString(1)),
                                        "", "", "", "", "", MediaType.VIDEO
                                        .name().toLowerCase(), 0, 0);
                                mediaItem
                                        .setMediaContentType(MediaContentType.VIDEO);
                                mMediaItems.add(mediaItem);
                            }
						}
					} while (cursor.moveToNext());
				}
				cursor.close();
			}
			db.close();
			if (cursor != null)
				printLog("getAllTracks ::::: " + mMediaItems.size());
			return mMediaItems;// cursor;
		}
	}

	public static int getAlbumCachedCount(Context context, String album_id) {
		synchronized (mDBOHandlerMutext) {
			int count = 0;
			DataBase db = DataBase.getInstance(context);
			db.open();
			if (!album_id.equals("0")) {// && CacheManager.isProUser(context)) {
				String freeUserCondition = "";
				if (!CacheManager.isProUser(context)) {
					freeUserCondition = " AND "
							+ DataBase.tables[DataBase.Track_Cache_int][1]
							+ " in (select track_id from "
							+ DataBase.Track_Cache_table
							+ " limit "
							+ ApplicationConfigurations.getInstance(context)
									.getFreeCacheLimit() + ")";
				}
				Cursor cursor1 = db.fetch(
						DataBase.Track_Cache_table,
						DataBase.Track_Cache_int,
						DataBase.tables[DataBase.Track_Cache_int][1]
								+ " in (Select "
								+ DataBase.tables[DataBase.Tracklist_int][1]
								+ " from " + DataBase.Tracklist_table
								+ " where "
								+ DataBase.tables[DataBase.Tracklist_int][2]
								+ " = " + album_id + " ) AND "
								+ DataBase.tables[DataBase.Track_Cache_int][4]
								+ " = '"
								+ CacheState.CACHED.toString() + "'"
								+ freeUserCondition);
				if (cursor1 != null) {
					count = cursor1.getCount();
					cursor1.close();
				}
			}
			db.close();
			printLog("getAlbumCachedCount ::::: " + count);
			return count;
		}
	}

	public static int getPlaylistCachedCount(Context context, String playlist_id) {
		synchronized (mDBOHandlerMutext) {
			int count = 0;
			DataBase db = DataBase.getInstance(context);
			db.open();
			if (!playlist_id.equals("0")) {
				Cursor cursor1 = db.fetch(
						DataBase.Track_Cache_table,
						DataBase.Track_Cache_int,
						DataBase.tables[DataBase.Track_Cache_int][1]
								+ " in (Select "
								+ DataBase.tables[DataBase.Tracklist_int][1]
								+ " from " + DataBase.Tracklist_table
								+ " where "
								+ DataBase.tables[DataBase.Tracklist_int][3]
								+ " = " + playlist_id + " ) AND "
								+ DataBase.tables[DataBase.Track_Cache_int][4]
								+ " = '"
								+ CacheState.CACHED.toString() + "'");
				if (cursor1 != null) {
					count = cursor1.getCount();
					cursor1.close();
				}
			}
			db.close();
			printLog("getPlaylistCachedCount ::::: " + count);
			return count;
		}
	}

	public static boolean deleteCachedTrack(Context context, String ids) {
		synchronized (mDBOHandlerMutext) {
			DataBase db = DataBase.getInstance(context);
			db.open();
			String where = null;
			if (ids != null) {
				where = DataBase.tables[DataBase.Track_Cache_int][1] + " IN ("
						+ ids + ")";
				Logger.s(" ::::::::delete::::::: " + where);
			}
			boolean isDeleted = db.delete(DataBase.Tracklist_table,
					DataBase.Tracklist_int, where);
			printLog("deleteCacedTrackFromList ::::: " + isDeleted);

			try{
				if (!CacheManager.isProUser(context)) {
//					CacheState cacheState = getTrackCacheState(context, ids);
//					if (cacheState == CacheState.CACHED) {
//						ApplicationConfigurations.getInstance(context).increaseFreeUserDeleteCount();
//					}
					Cursor cursor = db.freeUserExecute(context, ids);
					if (cursor != null) {
						if (cursor.moveToFirst()) {
							CacheState cacheState = CacheState.getCacheStateByName(cursor
									.getString(4));
							if (cacheState == CacheState.CACHED) {
								ApplicationConfigurations.getInstance(context).increaseFreeUserDeleteCount();
							}

						}
						cursor.close();
					}
				}
			}catch (Exception e){
				Logger.printStackTrace(e);
			}

			isDeleted = db.delete(DataBase.Track_Cache_table,
					DataBase.Track_Cache_int, where);
			db.close();
			printLog("deleteCacedTrack ::::: " + isDeleted);
			return isDeleted;
		}
		// return false;
	}

	public static boolean deleteCachedVideoTrack(Context context, String ids) {
		synchronized (mDBOHandlerMutext) {
			DataBase db = DataBase.getInstance(context);
			db.open();
			String where = null;
			if (ids != null) {
				where = DataBase.tables[DataBase.Video_Track_Cache_int][1]
						+ " IN (" + ids + ")";
				Logger.s(" ::::::::delete::::::: " + where);
			}
			boolean isDeleted = db.delete(DataBase.Video_Track_Cache_table,
					DataBase.Video_Track_Cache_int, where);
			db.close();
			printLog("deleteCachedVideoTrack ::::: " + isDeleted);
			return isDeleted;
		}
		// return false;
	}

	public static void checkTracksAvailability(Context context) {
		synchronized (mDBOHandlerMutext) {
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Track_Cache_table,
					DataBase.Track_Cache_int,
					DataBase.tables[DataBase.Track_Cache_int][4] + "='"
							+ CacheState.CACHED.toString() + "'",
					DataBase.tables[DataBase.Track_Cache_int][0] + " DESC");
			List<String> listTracksToDelete = new ArrayList<String>();
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					do {
						try {
							String path = cursor.getString(2);
							if (path == null)
								listTracksToDelete.add(cursor.getString(1));
							else if (path.length() == 0)
								listTracksToDelete.add(cursor.getString(1));
							else {
								File file = new File(path);
								if (!file.exists()) {
									listTracksToDelete.add(cursor.getString(1));
								}
							}
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
					} while (cursor.moveToNext());
				}
				cursor.close();
			}
			if (listTracksToDelete.size() > 0) {
				String idsToDelete = "";
				for (String trackId : listTracksToDelete) {
					if (idsToDelete.length() == 0)
						idsToDelete = trackId;
					else
						idsToDelete += "," + trackId;
				}
				printLog("getAllTracks ::::: " + idsToDelete);

				String where = DataBase.tables[DataBase.Tracklist_int][1]
						+ " IN (" + idsToDelete + ")";
				boolean isDeleted = db.delete(DataBase.Tracklist_table,
						DataBase.Tracklist_int, where);
				printLog("deleteCacedTrack ::::: " + isDeleted);

				where = DataBase.tables[DataBase.Track_Cache_int][1] + " IN ("
						+ idsToDelete + ")";
				isDeleted = db.delete(DataBase.Track_Cache_table,
						DataBase.Track_Cache_int, where);
				printLog("deleteCacedTrack ::::: " + isDeleted);
			}
			db.close();
		}
	}

	public static void checkVideoTracksAvailability(Context context) {
		synchronized (mDBOHandlerMutext) {
			Logger.s(" :::::::::::::checkVideoTracksAvailability:::::::::::::::  ");
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Video_Track_Cache_table,
					DataBase.Video_Track_Cache_int,
					DataBase.tables[DataBase.Video_Track_Cache_int][4] + "='"
							+ CacheState.CACHED.toString() + "'",
					DataBase.tables[DataBase.Video_Track_Cache_int][0]
							+ " DESC");
			List<String> listTracksToDelete = new ArrayList<String>();
			if (cursor != null) {
				Logger.s(" :::::::::::::checkVideoTracksAvailability:::::::::::::::  "
						+ cursor.getCount());
				if (cursor.moveToFirst()) {
					do {
						try {
							String path = cursor.getString(2);
							if (path == null)
								listTracksToDelete.add(cursor.getString(1));
							else if (path.length() == 0)
								listTracksToDelete.add(cursor.getString(1));
							else {
								File file = new File(path);
								if (!file.exists()) {
									listTracksToDelete.add(cursor.getString(1));
								}
							}
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
					} while (cursor.moveToNext());
				}
				cursor.close();
			} else
				Logger.s(" :::::::::::::checkVideoTracksAvailability:::::::::::::::  null");
			if (listTracksToDelete.size() > 0) {
				String idsToDelete = "";
				for (String trackId : listTracksToDelete) {
					if (idsToDelete.length() == 0)
						idsToDelete = trackId;
					else
						idsToDelete += "," + trackId;
				}
				printLog("checkVideoTracksAvailability ::::: " + idsToDelete);
				Logger.s(" :::::::::::::checkVideoTracksAvailability:::::::::::::::  "
						+ idsToDelete);
				// String where = DataBase.tables[DataBase.Tracklist_int][1] +
				// " IN (" + idsToDelete + ")";
				// boolean isDeleted = db.delete(DataBase.Tracklist_table,
				// DataBase.Tracklist_int, where);
				// printLog("deleteCacedTrack ::::: " + isDeleted);

				String where = DataBase.tables[DataBase.Video_Track_Cache_int][1]
						+ " IN (" + idsToDelete + ")";
				boolean isDeleted = db.delete(DataBase.Video_Track_Cache_table,
						DataBase.Video_Track_Cache_int, where);
				printLog("deleteCacedTrack ::::: " + isDeleted);
				Logger.s(" :::::::::::::checkVideoTracksAvailability:::::::::::::::  "
						+ isDeleted);
			}
			db.close();
		}
	}

	public static List<MediaItem> getAllNotCachedTracks(Context context) {
		synchronized (mDBOHandlerMutext) {
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Track_Cache_table,
					DataBase.Track_Cache_int,
					DataBase.tables[DataBase.Track_Cache_int][4] + "='"
							+ CacheState.NOT_CACHED.toString() + "' OR "
							+ DataBase.tables[DataBase.Track_Cache_int][4]
							+ "='" + CacheState.QUEUED.toString() + "'",
					DataBase.tables[DataBase.Track_Cache_int][0] + " DESC");
			List<MediaItem> mMediaItems = new ArrayList<MediaItem>();
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					do {
						try {
							// String response = cursor.getString(3);
							// System.out.println(" ::::::::::::::::::: " +
							// response);
							// response =
							// response.replace("{\"catalog\":{\"content\":",
							// "");
							// response = response.substring(0,
							// response.length() - 2);
							//
							// Gson gson = new
							// GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
							// MediaTrackDetails trackDetails =
							// (MediaTrackDetails) gson.fromJson(response,
							// MediaTrackDetails.class);
							// // Track track = new Track(trackDetails.getId(),
							// trackDetails.getTitle(),
							// trackDetails.getAlbumName(),
							// trackDetails.getSingers(),
							// trackDetails.getImageUrl(),
							// trackDetails.getBigImageUrl());
							// MediaItem mediaItem = new
							// MediaItem(trackDetails.getId(),
							// trackDetails.getTitle(),
							// trackDetails.getAlbumName(),
							// trackDetails.getSingers(),
							// trackDetails.getImageUrl(),
							// trackDetails.getBigImageUrl(),
							// MediaType.TRACK.name().toLowerCase(),
							// 0);
							MediaItem mediaItem = new MediaItem(
									Long.parseLong(cursor.getString(1)), "",
									"", "", "", "", MediaType.TRACK.name()
											.toLowerCase(), 0, 0);
							mediaItem
									.setMediaContentType(MediaContentType.MUSIC);
							mMediaItems.add(mediaItem);
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
					} while (cursor.moveToNext());
				}
				cursor.close();
			}
			db.close();
			if (cursor != null)
				printLog("getAllNotCachedTracks ::::: " + mMediaItems.size());
			return mMediaItems;// cursor;
		}
	}

	public static List<MediaItem> getAllNotCachedTracksForFreeUser(
			Context context) {
		synchronized (mDBOHandlerMutext) {
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Track_Cache_table,
					DataBase.Track_Cache_int,
					DataBase.tables[DataBase.Track_Cache_int][4] + "='"
							+ CacheState.NOT_CACHED.toString() + "' OR "
							+ DataBase.tables[DataBase.Track_Cache_int][4]
							+ "='" + CacheState.QUEUED.toString() + "'",
					DataBase.tables[DataBase.Track_Cache_int][0]);
			List<MediaItem> mMediaItems = new ArrayList<MediaItem>();
			if (cursor != null) {
				if (cursor.moveToLast()) {
					do {
						try {
							// String response = cursor.getString(3);
							// response =
							// response.replace("{\"catalog\":{\"content\":",
							// "");
							// response = response.substring(0,
							// response.length() - 2);
							//
							// Gson gson = new
							// GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
							// MediaTrackDetails trackDetails =
							// (MediaTrackDetails) gson.fromJson(response,
							// MediaTrackDetails.class);
							// // Track track = new Track(trackDetails.getId(),
							// trackDetails.getTitle(),
							// trackDetails.getAlbumName(),
							// trackDetails.getSingers(),
							// trackDetails.getImageUrl(),
							// trackDetails.getBigImageUrl());
							// MediaItem mediaItem = new
							// MediaItem(trackDetails.getId(),
							// trackDetails.getTitle(),
							// trackDetails.getAlbumName(),
							// trackDetails.getSingers(),
							// trackDetails.getImageUrl(),
							// trackDetails.getBigImageUrl(),
							// MediaType.TRACK.name().toLowerCase(),
							// 0);

							MediaItem mediaItem = new MediaItem(
									Long.parseLong(cursor.getString(1)), "",
									"", "", "", "", MediaType.TRACK.name()
											.toLowerCase(), 0, 0);
							mediaItem
									.setMediaContentType(MediaContentType.MUSIC);
							mMediaItems.add(mediaItem);

							mediaItem
									.setMediaContentType(MediaContentType.MUSIC);
							mMediaItems.add(mediaItem);
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
					} while (cursor.moveToPrevious());
				}
				cursor.close();
			}
			db.close();
			if (cursor != null)
				printLog("getAllNotCachedTracksForFreeUser ::::: "
						+ mMediaItems.size());
			return mMediaItems;// cursor;
		}
	}

	public static List<MediaItem> getAllNotCachedVideoTracks(Context context) {
		synchronized (mDBOHandlerMutext) {
			DataBase db = DataBase.getInstance(context);
			db.open();
			Cursor cursor = db.fetch(DataBase.Video_Track_Cache_table,
					DataBase.Video_Track_Cache_int,
					DataBase.tables[DataBase.Video_Track_Cache_int][4] + "='"
							+ CacheState.NOT_CACHED.toString() + "'",
					DataBase.tables[DataBase.Video_Track_Cache_int][0]
							+ " DESC");
			List<MediaItem> mMediaItems = new ArrayList<MediaItem>();
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					do {
						try {
							MediaItem mediaItem = new MediaItem(
									Long.parseLong(cursor.getString(1)), "",
									"", "", "", "", MediaType.VIDEO.name()
											.toLowerCase(), 0, 0);
							mediaItem
									.setMediaContentType(MediaContentType.VIDEO);
							mMediaItems.add(mediaItem);
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
					} while (cursor.moveToNext());
				}
				cursor.close();
			}
			db.close();
			if (cursor != null)
				printLog("getAllNotCachedTracks ::::: " + mMediaItems.size());
			return mMediaItems;// cursor;
		}
	}

	// public static void updateFailedCachedTracks(Context context) {
	// synchronized (mDBOHandlerMutext) {
	// DataBase db = DataBase.getInstance(context);
	// db.open();
	// ContentValues cv = new ContentValues();
	// cv.put(DataBase.tables[DataBase.Track_Cache_int][4],
	// CacheState.NOT_CACHED.toString());
	// boolean status = db.update(DataBase.Track_Cache_table,
	// DataBase.Track_Cache_int,
	// DataBase.tables[DataBase.Track_Cache_int][4] + "<>'"
	// + CacheState.CACHED.toString() + "'", cv);
	// db.close();
	// printLog("updateFailedCachedTracks ::::: " + status);
	// }
	// }

	// public static void updateFailedCachedVideoTracks(Context context) {
	// synchronized (mDBOHandlerMutext) {
	// DataBase db = DataBase.getInstance(context);
	// db.open();
	// ContentValues cv = new ContentValues();
	// cv.put(DataBase.tables[DataBase.Video_Track_Cache_int][4],
	// CacheState.NOT_CACHED.toString());
	// boolean status = db.update(DataBase.Video_Track_Cache_table,
	// DataBase.Video_Track_Cache_int,
	// DataBase.tables[DataBase.Video_Track_Cache_int][4] + "<>'"
	// + CacheState.CACHED.toString() + "'", cv);
	// db.close();
	// printLog("updateFailedCachedVideoTracks ::::: " + status);
	// }
	// }

	public static void cleanDatabseTables(Context context) {
		synchronized (mDBOHandlerMutext) {
			DataBase db = DataBase.getInstance(context);
			db.open();
			db.clean();
			db.close();
			printLog(" :::: cleanDatabseTables ::::: ");
		}
	}

	public static boolean updateStateForCachingStoppedTracks(Context context) {
		synchronized (mDBOHandlerMutext) {
			DataBase db = DataBase.getInstance(context);
			db.open();
			ContentValues cv = new ContentValues();
			cv.put(DataBase.tables[DataBase.Track_Cache_int][4],
					CacheState.NOT_CACHED.toString());
			boolean isRowUpdated = db.update(DataBase.Track_Cache_table,
					DataBase.Track_Cache_int,
					DataBase.tables[DataBase.Track_Cache_int][4] + "='"
							+ CacheState.CACHING.toString() + "' OR "
							+ DataBase.tables[DataBase.Track_Cache_int][4]
							+ "='" + CacheState.QUEUED.toString() + "'", cv);
			db.close();
			return isRowUpdated;
		}
	}

	public static boolean updateStateForCachingStoppedVideoTracks(
			Context context) {
		synchronized (mDBOHandlerMutext) {
			DataBase db = DataBase.getInstance(context);
			db.open();
			ContentValues cv = new ContentValues();
			cv.put(DataBase.tables[DataBase.Video_Track_Cache_int][4],
					CacheState.NOT_CACHED.toString());
			boolean isRowUpdated = db
					.update(DataBase.Video_Track_Cache_table,
							DataBase.Video_Track_Cache_int,
							DataBase.tables[DataBase.Video_Track_Cache_int][4]
									+ "='"
									+ CacheState.CACHING.toString()
									+ "' OR "
									+ DataBase.tables[DataBase.Video_Track_Cache_int][4]
									+ "='" + CacheState.QUEUED.toString() + "'",
							cv);
			db.close();
			return isRowUpdated;
		}
	}

	public static String getTextFromDb(String value, Context c) {
		try {

			if (TextUtils.isEmpty(value))
				return "";
			// synchronized (mDBOHandlerMutext) {

			ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
					.getInstance(c);
			if (value.contains("&amp;")) {
				value = value.replace("&amp;", "&");
			}
			if (value.contains("\n")) {
				value = value.replaceAll("[\\n\\r]+", "\\\n");
			}

			// if (value.contains("\n")) {
			// value = value.replaceAll("[\\n\\r]+", ":|new|:");
			// }
			if (value.contains("’")) {
				value = value.replace("’", "'");
			}
			if (value.contains("–")) {
				value = value.replace("–", "-");
			}
			if (value.contains("…")) {
				value = value.replace("…", "...");
			}
			if (value.contains("\\’")) {
				value = value.replace("\\’", "'");
			}

			// DataBase db = DataBase.getInstance(c);
			// db.open();
			// String result = "";
			// try {
			// String where = "str_name=(select str_name from "
			// + DataBase.All_String_Values_table
			// + " where str_value='"
			// + value// + "_English"
			// + "'"
			// + " AND language='English') AND language='"
			// + Utils.getLanguageString(mApplicationConfigurations
			// .getUserSelectedLanguage(), c) + "'";
			// Cursor cursor = db.fetch(DataBase.All_String_Values_table,
			// DataBase.All_Strings_int, where);
			// if (cursor != null && cursor.getCount() > 0) {
			// if (cursor.moveToFirst())
			// if (cursor.getString(2) != null
			// && cursor.getString(2).length() > 0)
			// result = cursor.getString(2);
			// cursor.close();
			// } else {
			// result = value;// + "_English";
			// }
			// } catch (Exception e) {
			// Logger.printStackTrace(e);
			// }
			// db.close();
			synchronized (mDBOHandlerMutext) {
				if (hash_strings.size() == 0) {
					createLanguageHashMap(mApplicationConfigurations
									.getUserSelectedLanguageText());
				}
			}

			String result = hash_strings.get(value);
			if (TextUtils.isEmpty(result)) {

				if (TextUtils.isEmpty(result))
					result = value;
			}

			// if (result.contains(":|new|:")) {
			// result = result.replaceAll(":|new|:", "\n");
			// }

			return result.replace("\\\\n", "\\n");
		} catch (Exception e) {
			return value;
		}
		// }
	}

	private static final HashMap<String, String> hash_strings = new HashMap<String, String>();

	public static void clearMAP() {
		hash_strings.clear();
	}

	public static void createLanguageHashMap(String language) {
		if (!language.equalsIgnoreCase("English")) {
			synchronized (mDBOHandlerMutext) {
				hash_strings.clear();
				DataBase db = DataBase.getInstance(HungamaApplication
						.getContext());
				db.open();
				SQLiteDatabase sampleDB = db.getSqlDb();
				String sql = "SELECT a.str_value, b.str_value FROM all_string_table "
						+ "a INNER JOIN all_string_table b ON a.str_name = b.str_name "
						+ "where a.language='English' AND b.language='"
						+ language + "';";
				Cursor cursor = sampleDB.rawQuery(sql, null);
				if (cursor != null) {
					if (cursor.moveToFirst()) {
						do {
							try {
								String key = cursor.getString(0);
								hash_strings.put(key, cursor.getString(1));
								// System.out.println("hashmap:" +
								// cursor.getString(0)
								// + " : " + cursor.getString(1) + ":"
								// + language);
							} catch (Exception e) {
								Logger.printStackTrace(e);
							}
						} while (cursor.moveToNext());
					}
					cursor.close();
				}
				db.close();
			}
		}
	}

	// public static boolean saveToDB(String name, String value, String
	// language) {
	// synchronized (mDBOHandlerMutext) {
	// DataBase db = DataBase.getInstance(HungamaApplication.getContext());
	// db.open();
	// long rowId = -1;
	// try {
	//
	// rowId = db.insert(DataBase.All_String_Values_table,
	// DataBase.All_Strings_int, new String[] { name, value,// +
	// // "_"
	// // +
	// // language,
	// language });
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// db.close();
	// System.out.println(rowId + " : " + language);
	// return rowId != -1;
	// }
	// }

	public static void cleanAllStringTable() {
		synchronized (mDBOHandlerMutext) {
			DataBase db = DataBase.getInstance(HungamaApplication.getContext());
			db.open();
			db.cleanTable(DataBase.All_Strings_int);
			db.close();
		}
	}

	public static void bulkInsertOneHundredRecords(NodeList nodes,
			String language) {
		synchronized (mDBOHandlerMutext) {
			DataBase db = DataBase.getInstance(HungamaApplication.getContext());
			db.open();

			SQLiteDatabase sampleDB = db.getSqlDb();
			String sql = "INSERT INTO " + DataBase.All_String_Values_table
					+ " (str_name, str_value, language) VALUES (?,?,?);";
			SQLiteStatement statement = sampleDB.compileStatement(sql);
			sampleDB.beginTransaction();
			for (int i = 0; i < nodes.getLength(); i++) {

				Element element = (Element) nodes.item(i);
				statement.clearBindings();
				String[] values = new String[] { element.getAttribute("name"),
						getCharacterDataFromElement(element).trim(), language };
				for (int j = 0; j < values.length; j++) {
					// if (j == 0) {
					// statement.bindLong(j + 1, Integer.parseInt(values[j]));
					// } else {
					// printLog("bulk insert: "+language);
					statement.bindString(j + 1, values[j]);
					// }
				}
				statement.execute();
			}
			sampleDB.setTransactionSuccessful();
			sampleDB.endTransaction();
			db.close();
		}
	}

	public static String getCharacterDataFromElement(Element e) {
		Node child = e.getFirstChild();
		if (child instanceof CharacterData) {
			CharacterData cd = (CharacterData) child;
			return cd.getData();
		}
		return "?";
	}

    public static final void exportDB(final Context context) {
		ThreadPoolManager.getInstance().submit(new Runnable() {
			@Override
			public void run() {
//				super.run();
				try {
//            File dbFile =
//                    new File(Environment.getDataDirectory() + "/data/com.hungama.myplay.activity/databases/"+ DataBase.DATABASE_NAME);
					File exportDir = new File(Environment.getExternalStorageDirectory(), ".hungama");
					if (!exportDir.exists()) {
						exportDir.mkdirs();
					}
//                    File spfile = new File(exportDir, "temp_hungama");
//                    spfile.createNewFile();
//                    saveSharedPreferencesToFile(context, spfile);

					File dbFile =
							context.getDatabasePath(DataBase.DATABASE_NAME);
					File file = new File(exportDir, dbFile.getName());
					file.createNewFile();
//                    copyFile(dbFile, file);
					copyEncryptedFile(dbFile, file, true);

					File dbFile1 =
							context.getDatabasePath(NewDataBase.DATABASE_NAME);
					File file1 = new File(exportDir, dbFile1.getName());
					file1.createNewFile();
//                    copyFile(dbFile, file);
					copyEncryptedFile(dbFile1, file1, true);
				} catch (Exception e) {
					Logger.e("hungama", e.getMessage());
				}
			}
		});
    }

    public static final void importDb(final Context context) {
//        new Thread() {
//            @Override
//            public void run() {
//                super.run();
                try {
                    File sd = Environment.getExternalStorageDirectory();
//                    File spfile = new File(sd, ".hungama/temp_hungama");
//                    spfile.createNewFile();
//                    loadSharedPreferencesFromFile(context, spfile);
//            File data = Environment.getDataDirectory();

                    // if (data.canWrite()) {
//            String currentDBPath = "/data/" + getPackageName() + "/databases/" + DataBase.DATABASE_NAME;
                    String backupDBPath = ".hungama/" + DataBase.DATABASE_NAME;
                    File currentDB = context.getDatabasePath(DataBase.DATABASE_NAME);//new File(data, currentDBPath);
                    File backupDB = new File(sd, backupDBPath);
                    currentDB.setWritable(true);
                    backupDB.setWritable(true);
                    if (backupDB.exists()) {
//                        copyFile(backupDB, currentDB);
                        copyEncryptedFile(backupDB, currentDB, false);
                    }
                    // }

					String backupDBPath1 = ".hungama/" + NewDataBase.DATABASE_NAME;
					File currentDB1 = context.getDatabasePath(NewDataBase.DATABASE_NAME);//new File(data, currentDBPath);
					File backupDB1 = new File(sd, backupDBPath1);
					currentDB1.setWritable(true);
					backupDB1.setWritable(true);
					if (backupDB1.exists()) {
//                        copyFile(backupDB, currentDB);
						copyEncryptedFile(backupDB1, currentDB1, false);
					}
                } catch (Exception e) {
                    Logger.i("Exceptions", "Exceptions:" + e);
                }
//            }
//        }.start();
    }

//    private static void copyFile(File src, File dst) throws IOException {
//        FileChannel inChannel = new FileInputStream(src).getChannel();
//        FileChannel outChannel = new FileOutputStream(dst).getChannel();
//        try {
////                inChannel.transferTo(0, inChannel.size(), outChannel);
//            outChannel.transferFrom(inChannel, 0, inChannel.size());
//        } finally {
//            if (inChannel != null)
//                inChannel.close();
//            if (outChannel != null)
//                outChannel.close();
//        }
//    }

    private static void copyEncryptedFile(File src, File dst, boolean isExport) throws IOException {
        InputStream inChannel = new FileInputStream(src);
        FileOutputStream outChannel = new FileOutputStream(dst);
        try {
            String deviceID = "630358525";
            CMEncryptor cmEncryptor = new CMEncryptor(deviceID);
            byte[] buffer = new byte[1024 * 100];
            int bytesRead = 0;
            while ((bytesRead = inChannel.read(buffer)) != -1) {
                if(isExport)
                    cmEncryptor.encrypt(buffer, 0, bytesRead);
                else
                    cmEncryptor.decrypt(buffer, 0, bytesRead);
                outChannel.write(buffer, 0, bytesRead);
            }
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    private static boolean saveSharedPreferencesToFile(Context context, File dst) {
        boolean res = false;
        ObjectOutputStream output = null;
        try {
            output = new ObjectOutputStream(new FileOutputStream(dst));
            SharedPreferences pref =
                    context.getSharedPreferences("preferences_application_configurations", Context.MODE_PRIVATE);
            output.writeObject(pref.getAll());

            res = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }

    @SuppressWarnings({"unchecked"})
    private static boolean loadSharedPreferencesFromFile(Context context, File src) {
        boolean res = false;
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(src));
            SharedPreferences.Editor prefEdit = context.getSharedPreferences("preferences_application_configurations", Context.MODE_PRIVATE).edit();
            prefEdit.clear();
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();

                if (v instanceof Boolean)
                    prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
                else if (v instanceof Float)
                    prefEdit.putFloat(key, ((Float) v).floatValue());
                else if (v instanceof Integer)
                    prefEdit.putInt(key, ((Integer) v).intValue());
                else if (v instanceof Long)
                    prefEdit.putLong(key, ((Long) v).longValue());
                else if (v instanceof String)
                    prefEdit.putString(key, ((String) v));
            }
            prefEdit.commit();
            res = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }

	public static boolean insertMediaConsumptionDetail(Context context, int duration, int mediaType) {
		synchronized (mDBOHandlerMutext) {
			NewDataBase db = NewDataBase.getInstance(context);
			db.open();
			long rowId = db.insert(NewDataBase.MEDIA_CONSUMPTION_TABLE,
					NewDataBase.MEDIA_CONSUMPTION_INT, new String[]{"" + System.currentTimeMillis(), "" + duration,
							"" + mediaType});
			db.close();
			printLog("insertMediaConsumptionDetail ::::: " + rowId + " ::: " + duration + " :: " + mediaType);
			return rowId != -1;
		}
	}

	public static int Last7DaysSongConsumptionDetail(Context context, int mediaType) {
		synchronized (mDBOHandlerMutext) {
			int totalTime = 0;
			Calendar cal = Calendar.getInstance();
			final long today = cal.getTimeInMillis();
			Logger.s("Date 1 = " + cal.getTime() + " ::: " + today);
			cal.add(Calendar.DATE, -6);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			final long before = cal.getTimeInMillis();//1448010631941L;//
			Logger.s("Date 2 = " + cal.getTime() + " ::: " + before);

			NewDataBase db = NewDataBase.getInstance(context);
			db.open();
//			Cursor cursor = db.fetch(DataBase.MEDIA_CONSUMPTION_TABLE, DataBase.MEDIA_CONSUMPTION_INT,
//					DataBase.tables[DataBase.MEDIA_CONSUMPTION_INT][1] + " BETWEEN '" + before + "' AND '" + today + "'");
//			if(cursor!=null){
//				if(cursor.moveToFirst()) {
//					do {
//						Logger.s("Last7DaysSongConsumptionDetail ::::: " + cursor.getString(1) + " ::: " + cursor.getString(2) + " :: " + cursor.getString(3));
//					} while (cursor.moveToNext());
//				}
//				cursor.close();
//			}
			Cursor cursor = db.fetch(NewDataBase.MEDIA_CONSUMPTION_TABLE, new String[] {"sum(" +
							NewDataBase.tables[NewDataBase.MEDIA_CONSUMPTION_INT][2] + ")"},
					NewDataBase.tables[NewDataBase.MEDIA_CONSUMPTION_INT][3] + " = " + mediaType + " AND " +
							NewDataBase.tables[NewDataBase.MEDIA_CONSUMPTION_INT][1] + " BETWEEN '" + before + "' AND '" + today + "'");
			if(cursor!=null){
				if(cursor.moveToFirst()) {
					do {
						totalTime = cursor.getInt(0);
						Logger.s("Last7DaysSongConsumptionDetail SUM ::::: " + totalTime);
					} while (cursor.moveToNext());
				}
				cursor.close();
			}

			db.delete(NewDataBase.MEDIA_CONSUMPTION_TABLE, NewDataBase.MEDIA_CONSUMPTION_INT,
					NewDataBase.tables[NewDataBase.MEDIA_CONSUMPTION_INT][1] + " < '" + before + "'");

			db.close();
			return totalTime;
		}
	}
}
