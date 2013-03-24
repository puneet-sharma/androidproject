package edu.umbc.cmsc628.mapassignment.db;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	public static final String TABLE_PROFILE = "profile";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_GPS_X = "gpsx";
	public static final String COLUMN_GPS_Y = "gpsy";
	public static final String COLUMN_ACCEL = "accel";
	public static final String COLUMN_ORIENT = "orient";
	public static final String COLUMN_ACTIVITY = "activity";

	private static final String DATABASE_NAME = "profile.db";
	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE = "create table "
		      + TABLE_PROFILE + "(" + COLUMN_ID
		      + " integer primary key autoincrement, "
		      + COLUMN_GPS_X +" real not null, "
		      + COLUMN_GPS_Y +" real not null, "
		      + COLUMN_ACCEL +" real not null, "
		      + COLUMN_ORIENT +" integer not null, "
		      + COLUMN_ACTIVITY +" text "
		      +");";
	
	public DBHelper(Activity activity) {
		super(activity,DATABASE_NAME,null,DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
	}

}
