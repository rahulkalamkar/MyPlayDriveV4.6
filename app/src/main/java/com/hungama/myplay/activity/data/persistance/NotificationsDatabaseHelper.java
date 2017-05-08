/**
 * 
 */
package com.hungama.myplay.activity.data.persistance;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hungama.myplay.activity.util.Logger;

/**
 * @author stas
 *
 */
public class NotificationsDatabaseHelper extends SQLiteOpenHelper {

	/**
	 * @param context
	 * @param name
	 * @param factory
	 * @param version
	 */

	private static final String DATABASE_NAME = "notifications.db";
	private static final int DATABASE_VERSION = 4;
	public static final String KEY_ROWID = "_id";
	static final String TABLE_NAME = "notifications_table";
	public static final String KEY_ALERT = "alert";
	static final String KEY_CODE = "code";
	static final String KEY_CATEGORY = "category";
	static final String KEY_CONTENT_ID = "content_id";
	static final String KEY_CONTENT_TYPE = "content_type";
	static final String KEY_CHANNEL_INDEX = "channel_index";
	static final String KEY_ARTIST_ID = "artist_id";
	static final String KEY_LANGUAGE = "language";
	static final String KEY_IS_READ = "id_read";
	static final String KEY_MESSAGE_ID = "message_id";
	public static final String KEY_DATE = "date";
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
	 * .SQLiteDatabase)
	 */
	private static NotificationsDatabaseHelper mInstance = null;

	public NotificationsDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + KEY_ROWID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_ALERT + " TEXT,"
				+ KEY_CODE + " INTEGER," + KEY_CATEGORY + " TEXT,"
				+ KEY_CONTENT_ID + " TEXT," + KEY_CONTENT_TYPE + " INTEGER,"
				+ KEY_CHANNEL_INDEX + " TEXT," + KEY_ARTIST_ID + " TEXT,"
				+ KEY_LANGUAGE + " TEXT," + KEY_MESSAGE_ID + " TEXT,"
				+ KEY_DATE + " TEXT," + KEY_IS_READ + " INTEGER);");

	}

	public static NotificationsDatabaseHelper getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new NotificationsDatabaseHelper(context);
		}
		return mInstance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
	 * .SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		Logger.i("DB", "onUpgrade");
		onCreate(db);
	}

}
