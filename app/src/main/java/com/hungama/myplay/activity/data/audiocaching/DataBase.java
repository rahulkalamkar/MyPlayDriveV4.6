package com.hungama.myplay.activity.data.audiocaching;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.util.Logger;

import java.util.Arrays;

public class DataBase {

	private static DataBase db;

	public static synchronized DataBase getInstance(Context context) {
		if (db == null) {
			db = new DataBase(context);
		}
		return db;
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context ctx) {
			super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(TABLE_TRACK_CACHE_CREATE);
			db.execSQL(TABLE_ALBUM_CACHE_CREATE);
			db.execSQL(TABLE_PLAYLIST_CACHE_CREATE);
			db.execSQL(TABLE_TRACKLIST_CREATE);
			db.execSQL(TABLE_VIDEO_TRACK_CACHE_CREATE);
			db.execSQL(TABLE_SONGCATCHER_HISTORY_CREATE);
			db.execSQL(TABLE_ALL_STRINGS_CREATE);
//			db.execSQL(TABLE_MEDIA_CONSUMPTION_CREATE);
		}

		@Override
		public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//			super.onDowngrade(db, oldVersion, newVersion);
			Logger.i("DbDownVersion", "old:::" + oldVersion + ":::new::::" + newVersion);
			checkForTableUpdate(db, Track_Cache_table, Track_Cache_int);
			checkForTableUpdate(db, Video_Track_Cache_table,
					Video_Track_Cache_int);
			checkForTableUpdate(db, Album_Cache_table, Album_Cache_int);
			checkForTableUpdate(db, Playlist_Cache_table, Playlist_Cache_int);
			checkForTableUpdate(db, Tracklist_table, Tracklist_int);
			checkForTableUpdate(db, SongCatcher_History_table,
					SongCatcher_History_int);
			checkForTableUpdate(db, All_String_Values_table, All_Strings_int);
//			checkForTableUpdate(db, MEDIA_CONSUMPTION_TABLE, MEDIA_CONSUMPTION_INT);
			onCreate(db);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			// db.execSQL("DROP TABLE IF EXISTS " + Track_Cache_table);
			Logger.i("DbVersion","old:::"+oldVersion+":::new::::"+newVersion);

			checkForTableUpdate(db, Track_Cache_table, Track_Cache_int);
			checkForTableUpdate(db, Video_Track_Cache_table,
					Video_Track_Cache_int);
			checkForTableUpdate(db, Album_Cache_table, Album_Cache_int);
			checkForTableUpdate(db, Playlist_Cache_table, Playlist_Cache_int);
			checkForTableUpdate(db, Tracklist_table, Tracklist_int);
			checkForTableUpdate(db, SongCatcher_History_table,
					SongCatcher_History_int);
			checkForTableUpdate(db, All_String_Values_table, All_Strings_int);
//			checkForTableUpdate(db, MEDIA_CONSUMPTION_TABLE, MEDIA_CONSUMPTION_INT);
			onCreate(db);
		}

		private void checkForTableUpdate(SQLiteDatabase db, String table_name,
				int table_no) {
			try {
				Cursor cursor = db.rawQuery("SELECT * from " + table_name
						+ " LIMIT 1", null);
				if (cursor != null) {
					// for(String name : cursor.getColumnNames())
					// System.out.println(" ::::::::::::::: 61 " + name);
					if (!Arrays.equals(cursor.getColumnNames(),
							tables[table_no])) {
						db.execSQL("DROP TABLE IF EXISTS " + table_name);
					}
					cursor.close();
				} else {
					db.execSQL("DROP TABLE IF EXISTS " + table_name);
				}
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}
	}

	public static enum CacheState {
		NOT_CACHED, QUEUED, CACHING, CACHED, FAILED;

		public static final CacheState getCacheStateByName(String name) {
			if (name.equalsIgnoreCase(NOT_CACHED.toString())) {
				return NOT_CACHED;
			} else if (name.equalsIgnoreCase(QUEUED.toString())) {
				return QUEUED;
			} else if (name.equalsIgnoreCase(CACHING.toString())) {
				return CACHING;
			} else if (name.equalsIgnoreCase(CACHED.toString())) {
				return CACHED;
			} else if (name.equalsIgnoreCase(FAILED.toString())) {
				return FAILED;
			}
			return NOT_CACHED;
		}
	};

	private DatabaseHelper dbHelper;
	private SQLiteDatabase sqLiteDb;
	private Context HCtx = null;
	public static final String DATABASE_NAME = "hungama_music_db";
	private static final int DATABASE_VERSION = 3;

	public static final String Track_Cache_table = "track_cache_table";
	public static final int Track_Cache_int = 0;
	public static final String Album_Cache_table = "album_cache_table";
	public static final int Album_Cache_int = 1;
	public static final String Playlist_Cache_table = "playlist_cache_table";
	public static final int Playlist_Cache_int = 2;
	public static final String Tracklist_table = "tracklist_table";
	public static final int Tracklist_int = 3;
	public static final String Video_Track_Cache_table = "video_track_cache_table";
	public static final int Video_Track_Cache_int = 4;
	public static final String SongCatcher_History_table = "songcatcher_table";
	public static final int SongCatcher_History_int = 5;
	public static final String All_String_Values_table = "all_string_table";
	public static final int All_Strings_int = 6;
//	public static final String MEDIA_CONSUMPTION_TABLE = "media_consumption_table";
//	public static final int MEDIA_CONSUMPTION_INT = 7;

	public static final String[][] tables = new String[][] {
			{ "sr_no", "track_id", "file_path", "json_details", "state",
					"progress" },
			{ "sr_no", "album_id", "json_details", "state" },
			{ "sr_no", "playlist_id", "json_details", "state" },
			{ "sr_no", "track_id", "album_id", "playlist_id" },
			{ "sr_no", "track_id", "file_path", "json_details", "state",
					"progress" },
			{ "sr_no", "track_id", "title", "json_details", "timestamp" },
			{ "sr_no", "str_name", "str_value", "language" }};//,
//			{ "sr_no", "timestamp", "duration", "mediatype" }};

	private static final String TABLE_TRACK_CACHE_CREATE = "create table IF NOT EXISTS "
			+ Track_Cache_table
			+ "("
			+ tables[Track_Cache_int][0]
			+ " integer primary key autoincrement,"
			+ tables[Track_Cache_int][1]
			+ " text not null, "
			+ tables[Track_Cache_int][2]
			+ " text not null, "
			+ tables[Track_Cache_int][3]
			+ " text not null, "
			+ tables[Track_Cache_int][4]
			+ " text not null, "
			+ tables[Track_Cache_int][5] + " integer default 0);";

	private static final String TABLE_ALBUM_CACHE_CREATE = "create table IF NOT EXISTS "
			+ Album_Cache_table
			+ "("
			+ tables[Album_Cache_int][0]
			+ " integer primary key autoincrement,"
			+ tables[Album_Cache_int][1]
			+ " text not null, "
			+ tables[Album_Cache_int][2]
			+ " text not null, "
			+ tables[Album_Cache_int][3] + " text not null);";

	private static final String TABLE_PLAYLIST_CACHE_CREATE = "create table IF NOT EXISTS "
			+ Playlist_Cache_table
			+ "("
			+ tables[Playlist_Cache_int][0]
			+ " integer primary key autoincrement,"
			+ tables[Playlist_Cache_int][1]
			+ " text not null, "
			+ tables[Playlist_Cache_int][2]
			+ " text not null, "
			+ tables[Playlist_Cache_int][3] + " text not null);";

	private static final String TABLE_TRACKLIST_CREATE = "create table IF NOT EXISTS "
			+ Tracklist_table
			+ "("
			+ tables[Tracklist_int][0]
			+ " integer primary key autoincrement,"
			+ tables[Tracklist_int][1]
			+ " text not null, "
			+ tables[Tracklist_int][2]
			+ " text not null, "
			+ tables[Tracklist_int][3]
			+ " text not null);";

	private static final String TABLE_VIDEO_TRACK_CACHE_CREATE = "create table IF NOT EXISTS "
			+ Video_Track_Cache_table
			+ "("
			+ tables[Video_Track_Cache_int][0]
			+ " integer primary key autoincrement,"
			+ tables[Video_Track_Cache_int][1]
			+ " text not null, "
			+ tables[Video_Track_Cache_int][2]
			+ " text not null, "
			+ tables[Video_Track_Cache_int][3]
			+ " text not null, "
			+ tables[Video_Track_Cache_int][4]
			+ " text not null, "
			+ tables[Video_Track_Cache_int][5] + " integer default 0);";

	private static final String TABLE_SONGCATCHER_HISTORY_CREATE = "create table IF NOT EXISTS "
			+ SongCatcher_History_table
			+ "("
			+ tables[SongCatcher_History_int][0]
			+ " integer primary key autoincrement,"
			+ tables[SongCatcher_History_int][1]
			+ " text not null, "
			+ tables[SongCatcher_History_int][2]
			+ " text not null, "
			+ tables[SongCatcher_History_int][3]
			+ " text not null, "
			+ tables[SongCatcher_History_int][4] + " text not null);";

	private static final String TABLE_ALL_STRINGS_CREATE = "create table IF NOT EXISTS "
			+ All_String_Values_table
			+ "("
			+ tables[All_Strings_int][0]
			+ " integer primary key autoincrement,"
			+ tables[All_Strings_int][1]
			+ " text not null, "
			+ tables[All_Strings_int][2]
			+ " text not null, "
			+ tables[All_Strings_int][3] + " text not null);";

//	private static final String TABLE_MEDIA_CONSUMPTION_CREATE = "create table IF NOT EXISTS "
//			+ MEDIA_CONSUMPTION_TABLE
//			+ "("
//			+ tables[MEDIA_CONSUMPTION_INT][0]
//			+ " integer primary key autoincrement,"
//			+ tables[MEDIA_CONSUMPTION_INT][1]
//			+ " text not null, "
//			+ tables[MEDIA_CONSUMPTION_INT][2]
//			+ " text not null, "
//			+ tables[MEDIA_CONSUMPTION_INT][3] + " integer);";

	/** Constructor */
	private DataBase(Context ctx) {
		HCtx = ctx;
		dbHelper = new DatabaseHelper(HCtx);
	}

	public DataBase open() throws SQLException {
		try {
			sqLiteDb = dbHelper.getWritableDatabase();
		} catch (Error e) {
			System.gc();
			System.runFinalization();
			System.gc();
			sqLiteDb = dbHelper.getWritableDatabase();
		} catch (Exception e) {
			sqLiteDb = dbHelper.getWritableDatabase();
		}
		return this;
	}

	public void clean() {
		try {
			sqLiteDb.delete(Track_Cache_table, null, null);
			sqLiteDb.delete(Tracklist_table, null, null);
			sqLiteDb.delete(Album_Cache_table, null, null);
			sqLiteDb.delete(Playlist_Cache_table, null, null);
			sqLiteDb.delete(Video_Track_Cache_table, null, null);
			sqLiteDb.delete(SongCatcher_History_table, null, null);
			sqLiteDb.delete(All_String_Values_table, null, null);
//			sqLiteDb.delete(MEDIA_CONSUMPTION_TABLE, null, null);
		} catch (Exception e) {
		} catch (Error e) {
			System.gc();
			System.runFinalization();
			System.gc();
		}
	}

	public void cleanTable(int tableNo) {
		switch (tableNo) {
		case Track_Cache_int:
			sqLiteDb.delete(Track_Cache_table, null, null);
			break;
		case All_Strings_int:
			sqLiteDb.delete(All_String_Values_table, null, null);
			break;
		default:
			break;
		}
	}

	public void close() {
		dbHelper.close();
	}

	public synchronized long insert(String DATABASE_TABLE, int tableNo,
			String[] values) {
		ContentValues vals = new ContentValues();
		for (int i = 0; i < values.length; i++) {
			vals.put(tables[tableNo][i + 1], values[i]);
		}
		return sqLiteDb.insert(DATABASE_TABLE, null, vals);
	}

	// public synchronized long insertWithSR_NO(String DATABASE_TABLE,
	// int tableNo, String[] values, String srno) {
	// ContentValues vals = new ContentValues();
	// for (int i = 0; i < values.length; i++) {
	// vals.put(tables[tableNo][i + 1], values[i]);
	// }
	// vals.put(tables[tableNo][0], srno);
	// return sqLiteDb.insert(DATABASE_TABLE, null, vals);
	// }

	// public boolean delete(String DATABASE_TABLE, int tableNo, long rowId) {
	// return sqLiteDb.delete(DATABASE_TABLE,
	// tables[tableNo][0] + "=" + rowId, null) > 0;
	// }

	public synchronized boolean delete(String DATABASE_TABLE, int tableNo,
			String whereCause) {
		return sqLiteDb.delete(DATABASE_TABLE, whereCause, null) > 0;
	}

	// public synchronized Cursor fetch(String DATABASE_TABLE, int tableNo,
	// long rowId) throws SQLException {
	//
	// Cursor ret = sqLiteDb.query(DATABASE_TABLE, tables[tableNo],
	// tables[tableNo][0] + "=" + rowId, null, null, null, null);
	// if (ret != null) {
	// try{
	// ret.moveToFirst();
	// } catch(Exception e){
	// Logger.printStackTrace(e);
	// }
	// }
	// return ret;
	// }

	public synchronized Cursor fetch(String DATABASE_TABLE, int tableNo,
			String where) throws SQLException {
		try {
			Cursor ret = sqLiteDb.query(DATABASE_TABLE, tables[tableNo], where,
					null, null, null, null);
			if (ret != null) {
				ret.moveToFirst();
			}
			return ret;
		} catch (Exception e) {
			return null;
		}
	}

	public synchronized Cursor fetch(String DATABASE_TABLE, int tableNo,
			String where, String orderBy) throws SQLException {

		Cursor ret = sqLiteDb.query(DATABASE_TABLE, tables[tableNo], where,
				null, null, null, orderBy);
		if (ret != null) {
			ret.moveToFirst();
		}
		return ret;
	}

	// public synchronized Cursor fetch(String DATABASE_TABLE, int tableNo,
	// String where, String orderBy, String limit) throws SQLException {
	//
	// Cursor ret = sqLiteDb.query(DATABASE_TABLE, tables[tableNo], where,
	// null, null, null, orderBy, limit);
	// if (ret != null) {
	// ret.moveToFirst();
	// }
	// return ret;
	// }

	// public synchronized Cursor fetchCounts(String DATABASE_TABLE, int
	// tableNo,
	// String[] cols,String where) throws SQLException {
	//
	// Cursor ret = sqLiteDb.query(DATABASE_TABLE, tables[tableNo], where,
	// null, null, null, null);
	// if (ret != null) {
	// ret.moveToFirst();
	// }
	// return ret;
	// }

	// public synchronized Cursor fetch(String DATABASE_TABLE, int tableNo,
	// int[] colindex, String where) throws SQLException {
	//
	// String[] cols = new String[colindex.length];
	// for (int i = 0; i < colindex.length; i++)
	// cols[i] = tables[tableNo][colindex[i]];
	//
	// Cursor ret = sqLiteDb.query(DATABASE_TABLE, cols, where, null, null,
	// null, null);
	// if (ret != null) {
	// ret.moveToFirst();
	// }
	// return ret;
	// }

	public synchronized Cursor fetch(String DATABASE_TABLE,
									 String[] cols, String where) throws SQLException {
		Cursor ret = sqLiteDb.query(DATABASE_TABLE, cols, where, null, null,
				null, null);
		if (ret != null) {
			ret.moveToFirst();
		}
		return ret;
	}

	// public synchronized Cursor fetch(String DATABASE_TABLE, int tableNo,
	// int colIndex, String colval) throws SQLException {
	//
	// Cursor ret = sqLiteDb.query(DATABASE_TABLE, tables[tableNo],
	// tables[tableNo][colIndex] + "='" + colval + "'", null, null,
	// null, null);
	// if (ret != null) {
	// ret.moveToFirst();
	// }
	// return ret;
	// }

	// public synchronized Cursor fetch(String DATABASE_TABLE, int tableNo,
	// int colIndex, String colval, int colIndex2, String colval2)
	// throws SQLException {
	//
	// Cursor ret = sqLiteDb.query(DATABASE_TABLE, tables[tableNo],
	// tables[tableNo][colIndex] + "='" + colval + "' and "
	// + tables[tableNo][colIndex2] + "='" + colval2 + "'",
	// null, null, null, null);
	// if (ret != null) {
	// ret.moveToFirst();
	// }
	// return ret;
	// }

	// public synchronized Cursor fetch(String DATABASE_TABLE, int tableNo,
	// int[] colIndex, String[] colval) throws SQLException {
	//
	// String strSelection = "";
	// for (int i = 0; i < colIndex.length; i++) {
	// strSelection = strSelection + tables[tableNo][colIndex[i]] + "='"
	// + colval[i] + "' and ";
	// }
	// strSelection = strSelection.substring(0, strSelection.length() - 5);
	// Cursor ret = sqLiteDb.query(DATABASE_TABLE, tables[tableNo],
	// strSelection, null, null, null, null);
	// if (ret != null) {
	// ret.moveToFirst();
	// }
	// return ret;
	// }

	// public synchronized Cursor fetchQ(String DATABASE_TABLE, int tableNo,
	// int[] colIndex, String[] colval) throws SQLException {
	//
	// String strSelection = "";
	// for (int i = 0; i < colIndex.length; i++) {
	// strSelection = strSelection + tables[tableNo][colIndex[i]] + "='"
	// + colval[i] + "' and ";
	// }
	// strSelection = strSelection
	// + "stackid in (select stack_id from stacks where isarchieve='No')";
	// Cursor ret = sqLiteDb.query(DATABASE_TABLE, tables[tableNo],
	// strSelection, null, null, null, null);
	// if (ret != null) {
	// ret.moveToFirst();
	// }
	// return ret;
	// }

	// public synchronized Cursor fetchFollowUp(String DATABASE_TABLE,
	// int tableNo, int[] colIndex, String[] colval) throws SQLException {
	//
	// String strSelection = "";
	// for (int i = 0; i < colIndex.length; i++) {
	// strSelection = strSelection + tables[tableNo][colIndex[i]] + "='"
	// + colval[i] + "' and ";
	// }
	// // strSelection = strSelection.substring(0,strSelection.length() - 5);
	// strSelection = strSelection
	// + "stackid in (select stack_id from stacks where isarchieve='No')";
	// Cursor ret = sqLiteDb.query(DATABASE_TABLE, tables[tableNo],
	// strSelection, null, null, null, null);
	// if (ret != null) {
	// ret.moveToFirst();
	// }
	// return ret;
	// }

	// public synchronized Cursor fetch(String DATABASE_TABLE, int tableNo,
	// int colIndex, String colval, String orderByval) throws SQLException {
	//
	// Cursor ret = sqLiteDb.query(DATABASE_TABLE, tables[tableNo],
	// tables[tableNo][colIndex] + "='" + colval + "'", null, null,
	// null, orderByval);
	// if (ret != null) {
	// ret.moveToFirst();
	// }
	// return ret;
	// }

	public synchronized Cursor fetchAll(String DATABASE_TABLE, int tableNo) {
		try {
			return sqLiteDb.query(DATABASE_TABLE, tables[tableNo], null, null,
					null, null, null);

		} catch (Exception e) {
			Logger.e("fetchAll", e.getMessage());
			return null;
		}
	}

	public synchronized Cursor fetchAll(String DATABASE_TABLE, int tableNo,
			String orderByval) {
		try {
			return sqLiteDb.query(DATABASE_TABLE, tables[tableNo], null, null,
					null, null, orderByval);
		} catch (Exception e) {
			Logger.e("fetchAll", e.getMessage());
			return null;
		}
	}

	// public synchronized Cursor fetchAll(String DATABASE_TABLE, int tableNo,
	// String orderByval, String limit) {
	// try {
	// return sqLiteDb.query(DATABASE_TABLE, tables[tableNo], null, null,
	// null, null, orderByval, limit);
	// } catch (Exception e) {
	// Logger.e("fetchAll", e.getMessage());
	// return null;
	// }
	// }
	// public synchronized Cursor fetchAll(String DATABASE_TABLE, int
	// tableNo,String where,
	// String orderByval, String limit) {
	// try {
	// return sqLiteDb.query(DATABASE_TABLE, tables[tableNo], null, null,
	// null, null, orderByval, limit);
	// } catch (Exception e) {
	// Logger.e("fetchAll", e.getMessage());
	// return null;
	// }
	// }

	public synchronized Cursor freeUserExecute(Context context, String id) {
		try {
			String sql = "Select * from "
					+ Track_Cache_table
					+ " where track_id='"
					+ id
					+ "' and track_id in(select track_id from "
					+ Track_Cache_table
					+ " limit "
					+ (ApplicationConfigurations.getInstance(context)
							.getFreeCacheLimit() - ApplicationConfigurations.getInstance(context)
					.getFreeUserDeleteCount()) + ")";
			return sqLiteDb.rawQuery(sql, null);
		} catch (Exception e) {
			Logger.e("fetchAll", e.getMessage());
			return null;
		}
	}

	// public synchronized Cursor fetchAll(String DATABASE_TABLE, int tableNo,
	// String orderByval,String where)
	// {
	// try {
	// return sqLiteDb.query(DATABASE_TABLE, tables[tableNo], where, null,
	// null, null, orderByval);
	//
	// } catch (Exception e) {
	// Log.e("yo", e.getMessage());
	// return null;
	// }
	// }

	// public boolean update(String DATABASE_TABLE, int tableNo, long rowId,
	// ContentValues vc) {
	// return sqLiteDb.update(DATABASE_TABLE, vc, tables[tableNo][0] + "="
	// + rowId, null) > 0;
	// }

	public boolean update(String DATABASE_TABLE, int tableNo, String where,
			ContentValues cv) {
		try {
			return sqLiteDb.update(DATABASE_TABLE, cv, where, null) > 0;
		} catch (Exception e) {
			Logger.printStackTrace(e);
			return false;
		}
	}

	// public boolean updatePage(String DATABASE_TABLE, int tableNo,
	// int flashCardID, int pageno, String val) {
	// ContentValues vals = new ContentValues();
	// // for (int i = 0; i < values.length; i++)
	// // vals.put(tables[tableNo][i + 1], values[i]);
	// vals.put(tables[tableNo][3], val);
	// return sqLiteDb.update(DATABASE_TABLE, vals, " flashcardid='"
	// + flashCardID + "' and pageno='" + pageno + "'", null) > 0;
	// }

	public SQLiteDatabase getSqlDb() {
		return sqLiteDb;
	}

	// public boolean update(String DATABASE_TABLE, int tableNo, long rowId,
	// int colIndex, int val) {
	// ContentValues vals = new ContentValues();
	// vals.put(tables[tableNo][colIndex], val);
	// return sqLiteDb.update(DATABASE_TABLE, vals, tables[tableNo][0] + "="
	// + rowId, null) > 0;
	// }

	// public boolean update(String DATABASE_TABLE, int tableNo, long rowId,
	// int colIndex, String val) {
	// ContentValues vals = new ContentValues();
	// vals.put(tables[tableNo][colIndex], val);
	// return sqLiteDb.update(DATABASE_TABLE, vals, tables[tableNo][0] + "="
	// + rowId, null) > 0;
	// }

	// public Cursor fetch(String DATABASE_TABLE, int tableNo, int i,
	// String string, int j) {
	// Cursor ret = sqLiteDb.query(DATABASE_TABLE, tables[tableNo],
	// tables[tableNo][i] + "='" + string + "' and mediatypeid<>" + j,
	// null, null, null, null);
	// if (ret != null) {
	// ret.moveToFirst();
	// }
	// return ret;
	// }
}