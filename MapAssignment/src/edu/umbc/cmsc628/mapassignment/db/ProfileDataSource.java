package edu.umbc.cmsc628.mapassignment.db;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import edu.umbc.cmsc628.mapassignment.MapActivity;

public class ProfileDataSource {
	private SQLiteDatabase database;
	private DBHelper dbHelper;
	private MapActivity mapActivity;
	private String[] allColumns = { DBHelper.COLUMN_ID, DBHelper.COLUMN_ACCEL,
			DBHelper.COLUMN_ACTIVITY, DBHelper.COLUMN_GPS_X,
			DBHelper.COLUMN_GPS_Y, DBHelper.COLUMN_ORIENT };

	public ProfileDataSource(MapActivity activity) {
		mapActivity = activity;
		dbHelper = new DBHelper(activity);
	}
	
	public ProfileDataSource(Activity activity) {
		dbHelper = new DBHelper(activity);
	}

	public Profile insertProfile(Profile profile) {
		ContentValues values = new ContentValues();
		values.put(DBHelper.COLUMN_ACCEL, profile.getAccel());
		values.put(DBHelper.COLUMN_ACTIVITY, profile.getActivity());
		values.put(DBHelper.COLUMN_GPS_X, profile.getGpsX());
		values.put(DBHelper.COLUMN_GPS_Y, profile.getGpsY());
		values.put(DBHelper.COLUMN_ORIENT, profile.getOrient());

		long insertId = database.insert(DBHelper.TABLE_PROFILE, null,
				values);
		Cursor cursor = database.query(DBHelper.TABLE_PROFILE, allColumns,
				DBHelper.COLUMN_ID + " = " + insertId, null, null, null,
				null);
		cursor.moveToFirst();
		Profile newProfile = cursorToProfile(cursor);
		cursor.close();
		return newProfile;
	}

	public void deleteTable() {
		System.out.println("Emptying table");
		database.delete(DBHelper.TABLE_PROFILE, DBHelper.COLUMN_ID + " >=0 ",
				null);
	}

	public List<Profile> getAllProfiles() {
		List<Profile> profiles = new ArrayList<Profile>();

		Cursor cursor = database.query(DBHelper.TABLE_PROFILE, allColumns,
				null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Profile profile = cursorToProfile(cursor);
			profiles.add(profile);
			cursor.moveToNext();
		}
		cursor.close();
		return profiles;
	}

	private Profile cursorToProfile(Cursor cursor) {
		Profile profile = new Profile();
		profile.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
		profile.setGpsX(cursor.getFloat(cursor
				.getColumnIndex(DBHelper.COLUMN_GPS_X)));
		profile.setGpsY(cursor.getFloat(cursor
				.getColumnIndex(DBHelper.COLUMN_GPS_Y)));
		profile.setAccel(cursor.getFloat(cursor
				.getColumnIndex(DBHelper.COLUMN_ACCEL)));
		profile.setOrient(cursor.getInt(cursor
				.getColumnIndex(DBHelper.COLUMN_ORIENT)));
		profile.setActivity(cursor.getString(cursor
				.getColumnIndex(DBHelper.COLUMN_ACTIVITY)));
		return profile;
	}

	public void ScanLocations() {
		new LoadMapMarkersTask().execute();
	}

	public void insertDummyData() {
		database = dbHelper.getWritableDatabase();
		//deleteTable();
		System.err.println("Current size==" + getAllProfiles().size());
		if (getAllProfiles().size() == 0) {
			Log.i(ProfileDataSource.class.getName(), "Inserting dummy data");
			
			float gpsX = 39.26159F, gpsY = -76.70278F, accel = 3.432F;
			int orient = 0;
			String activity = "activity 1";

			Profile p1 = new Profile();
			p1.setGpsX(gpsX);
			p1.setGpsY(gpsY);
			p1.setAccel(accel);
			p1.setOrient(orient);
			p1.setActivity(activity);
			insertProfile(p1);
			Log.i(ProfileDataSource.class.getName(), "Profile 1 inserted");
			
			gpsX = 39.26108F;
			gpsY = -76.70178F;
			accel = 3.112F;
			orient = 2;
			activity = "activity 2";

			Profile p2 = new Profile();
			p2.setGpsX(gpsX);
			p2.setGpsY(gpsY);
			p2.setAccel(accel);
			p2.setOrient(orient);
			p2.setActivity(activity);
			insertProfile(p2);
			Log.i(ProfileDataSource.class.getName(), "Profile 2 inserted");
			
		}
	}

	/*
	 * --------------------------------------------------------------------------
	 * -------
	 * ------------------------------------------------------------------
	 * --------------- -------------------Private AsyncTask Classes Down
	 * here---------------------------
	 * ------------------------------------------
	 * ---------------------------------------
	 * ----------------------------------
	 * -----------------------------------------------
	 */

	private class LoadMapMarkersTask extends AsyncTask<Void, Profile, Void> {

		@Override
		protected Void doInBackground(Void... vals) {

			database = dbHelper.getWritableDatabase();

			Cursor cursor = database.query(DBHelper.TABLE_PROFILE, allColumns,
					null, null, null, null, null);

			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				Profile profile = cursorToProfile(cursor);
				publishProgress(profile);
				cursor.moveToNext();
			}
			cursor.close();

			return null;
		}

		@Override
		protected void onProgressUpdate(Profile... values) {
			super.onProgressUpdate(values);
			mapActivity.addMarkerAtRuntime(values[0]);
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			database.close();
		}
	}
}
