package com.moysof.hashtagnews.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	
	private static final int DB_VERSION = 1;

	public DBHelper(Context context) {
		super(context, "myDB", null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table categories (id integer primary key autoincrement, name text, hashtags text, color text, last_id_instagram text, last_id_twitter text, last_time text);");
		ContentValues cv = new ContentValues();
		cv.put("name", "#HashtagNews");
		cv.put("hashtags", "[\"hashtagnews\"]");
		cv.put("color", "#fe8b2e");
		cv.put("last_id_instagram", "");
		cv.put("last_id_twitter", "");
		cv.put("last_time", "");
		db.insert("categories", null, cv);
		cv.put("name", "Near Me");
		cv.put("hashtags", "[\"\"]");
		cv.put("color", "#888888");
		cv.put("last_id_instagram", "");
		cv.put("last_id_twitter", "");
		cv.put("last_time", "");
		db.insert("categories", null, cv);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS categories");
		
		onCreate(db);
	}
}
