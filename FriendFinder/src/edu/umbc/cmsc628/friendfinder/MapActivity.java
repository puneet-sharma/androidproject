package edu.umbc.cmsc628.friendfinder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends Activity implements LocationListener {

	// static final LatLng HAMBURG = new LatLng(53.558, 9.927);
	// static final LatLng KIEL = new LatLng(53.551, 9.993);
	private GoogleMap map;
	private LocationManager locationManager;
	Location location;
	final static String hostIp = "192.168.1.17";
	private static boolean autoCheckIn;
	private static int checkInFrequency;
	private static boolean autoRefresh;
	private static int refreshFrequency;
	private static String loggedUser;
	private static SharedPreferences sharedPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		boolean enabled = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (!enabled) {
			enableLocationSettings();
			// new EnableGpsDialogFragment().show(getSupportFragmentManager(),
			// "enableGpsDialog");
		}

		// Get shared preference values
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

		autoCheckIn = sharedPref.getBoolean(
				SettingsActivity.KEY_PREF_AUTO_CHECK_IN, false);
		autoRefresh = sharedPref.getBoolean(
				SettingsActivity.KEY_PREF_AUTO_REFRESH, false);

		refreshFrequency = Integer.valueOf(sharedPref.getString(
				SettingsActivity.KEY_PREF_AUTO_REFRESH_FREQ, "5"));
		checkInFrequency = Integer.valueOf(sharedPref.getString(
				SettingsActivity.KEY_PREF_AUTO_CHECK_IN_FREQ, "5"));

		loggedUser = sharedPref.getString(SettingsActivity.KEY_LOGGED_USERNAME,
				"");

		if (loggedUser == null || loggedUser == "") {
			Intent loginIntent = new Intent(getApplicationContext(),
					LoginActivity.class);
			startActivity(loginIntent);
		}

		Criteria criteria = new Criteria();
		String provider = locationManager.getBestProvider(criteria, false);
		location = locationManager.getLastKnownLocation(provider);
		setUpMapIfNeeded();

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				checkInFrequency * 60 * 1000, 0, this);
	}

	private void enableLocationSettings() {
		Intent settingsIntent = new Intent(
				Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(settingsIntent);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (locationManager != null)
			locationManager.removeUpdates(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
		if (autoCheckIn) {
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, checkInFrequency * 60 * 1000,
					0, this);
		}
	}

	private void setUpMapIfNeeded() {
		if (map == null) {
			// Try to obtain the map from the SupportMapFragment.
			map = ((MapFragment) getFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (map != null) {
				setUpMap();
			}
		}
	}

	public static void loadPrefs() {
		autoCheckIn = sharedPref.getBoolean(
				SettingsActivity.KEY_PREF_AUTO_CHECK_IN, true);
		checkInFrequency = Integer.valueOf(sharedPref.getString(
				SettingsActivity.KEY_PREF_AUTO_CHECK_IN_FREQ, ""));

		autoRefresh = sharedPref.getBoolean(
				SettingsActivity.KEY_PREF_AUTO_REFRESH, false);
		refreshFrequency = Integer.valueOf(sharedPref.getString(
				SettingsActivity.KEY_PREF_AUTO_REFRESH_FREQ, "5"));
	}

	private void setUpMap() {
		map.setMyLocationEnabled(true);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(
				new LatLng(location.getLatitude(), location.getLongitude()), 18));
	}

	// public void addMarkerAtRuntime(Profile p) {
	// map.addMarker(new MarkerOptions()
	// .position(new LatLng(p.getLatitude(), p.getLongitude()))
	// .title(String.valueOf(p.getId())).snippet(p.toString()));
	// // .icon(BitmapDescriptorFactory
	// // .fromResource(R.drawable.mapmarker)));
	// }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.menu_check_in:
			Criteria criteria = new Criteria();
			String provider = locationManager.getBestProvider(criteria, false);
			location = locationManager.getLastKnownLocation(provider);

			String[] params = new String[3];
			params[0] = loggedUser;
			params[1] = String.valueOf(location.getLatitude());
			params[2] = String.valueOf(location.getLongitude());
			try {
				String response;
				response = new CheckInTask().execute(params).get();
				Log.d("MapActivity", response);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Toast.makeText(getApplicationContext(),
					"Checked in current location", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.menu_refresh:
			params = new String[2];
			params[0] = String.valueOf(location.getLatitude());
			params[1] = String.valueOf(location.getLongitude());
			new FindFriendsTask().execute(params);
			return true;
		case R.id.menu_log_out:
			loggedUser = sharedPref.getString(
					SettingsActivity.KEY_LOGGED_USERNAME, "");
			Editor editor = sharedPref.edit();
			editor.putString(SettingsActivity.KEY_LOGGED_USERNAME, "");
			editor.commit();
			finish();
			return true;
		case R.id.menu_settings:
			Intent settingsIntent = new Intent(getApplicationContext(),
					SettingsActivity.class);
			startActivity(settingsIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onLocationChanged(Location arg0) {
		if (autoCheckIn) {
			String[] params = new String[3];
			params[0] = loggedUser;
			params[1] = String.valueOf(location.getLatitude());
			params[2] = String.valueOf(location.getLongitude());
			try {
				String response = new CheckInTask().execute(params).get();
				Log.d("MapActivity", response);
				Toast.makeText(getApplicationContext(),
						"Checked in current location", Toast.LENGTH_SHORT)
						.show();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {

	}

	private static class CheckInTask extends AsyncTask<String, Long, String> {

		@Override
		protected String doInBackground(String... arg0) {
			String[] args = arg0;
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://" + hostIp + "/checkIn.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();

			pairs.add((NameValuePair) new BasicNameValuePair("username",
					args[0]));
			pairs.add((NameValuePair) new BasicNameValuePair("latitude",
					args[1]));
			pairs.add((NameValuePair) new BasicNameValuePair("longitude",
					args[2]));

			try {
				post.setEntity(new UrlEncodedFormEntity(pairs));
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = "default";
			try {
				responseBody = client.execute(post, responseHandler);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Log.d("ResponseBody", responseBody);
			return responseBody;
		}

	}

	private class FindFriendsTask extends AsyncTask<String, Long, String> {

		private String username;
		double latitude;
		double longitude;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			map.clear();
		}
		@Override
		protected String doInBackground(String... arg0) {
			String[] args = arg0;
			JSONArray friends = null;
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://" + hostIp
					+ "/ClosestFriends.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();

			pairs.add((NameValuePair) new BasicNameValuePair("latitude",
					args[0]));
			pairs.add((NameValuePair) new BasicNameValuePair("longitude",
					args[1]));

			try {
				post.setEntity(new UrlEncodedFormEntity(pairs));
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = "default";
			try {
				responseBody = client.execute(post, responseHandler);

				JSONObject json = new JSONObject(responseBody);
				friends = json.getJSONArray("friends");
				for (int i = 0; i < friends.length(); i++) {
					JSONObject friend = friends.getJSONObject(i).getJSONObject(
							"friend");
					username = friend.getString("USERNAME");
					latitude = friend.getDouble("LATITUDE");
					longitude = friend.getDouble("LONGITUDE");
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Log.d("ResponseBody", responseBody);
			return responseBody;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (username.equals(loggedUser)) {
				map.addMarker(new MarkerOptions()
						.position(new LatLng(latitude, longitude))
						.title(username)
						.snippet("You")
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.marker_green)));
			}else{
				map.addMarker(new MarkerOptions()
				.position(new LatLng(latitude, longitude))
				.title(username));
			}
		}

	}
}
