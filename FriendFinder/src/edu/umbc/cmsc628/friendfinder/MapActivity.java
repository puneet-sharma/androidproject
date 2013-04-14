package edu.umbc.cmsc628.friendfinder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

public class MapActivity extends Activity {

	// static final LatLng HAMBURG = new LatLng(53.558, 9.927);
	// static final LatLng KIEL = new LatLng(53.551, 9.993);
	private GoogleMap map;
	private LocationManager locationManager;
	Location location;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		boolean enabled = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (!enabled) {
			enableLocationSettings();
			//new EnableGpsDialogFragment().show(getSupportFragmentManager(), "enableGpsDialog");
		}

		Criteria criteria = new Criteria();
		String provider = locationManager.getBestProvider(criteria, false);
		location = locationManager.getLastKnownLocation(provider);
		setUpMapIfNeeded();
		//dataSource.insertDummyData();
	}
	
	private void enableLocationSettings() {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
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

	private void setUpMap() {
		map.setMyLocationEnabled(true);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(
				new LatLng(location.getLatitude(), location.getLongitude()), 18));
	}

//	public void addMarkerAtRuntime(Profile p) {
//		map.addMarker(new MarkerOptions()
//				.position(new LatLng(p.getLatitude(), p.getLongitude()))
//				.title(String.valueOf(p.getId())).snippet(p.toString()));
//		// .icon(BitmapDescriptorFactory
//		// .fromResource(R.drawable.mapmarker)));
//	}

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
			Toast.makeText(getApplicationContext(), "Check in", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.menu_refresh:
			Toast.makeText(getApplicationContext(), "Refresh", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.menu_log_out:
			Toast.makeText(getApplicationContext(), "Log out", Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private static class sessionPostTask extends AsyncTask<String, Long, String>{

		@Override
		protected String doInBackground(String... arg0) {
			String[] credentials = arg0;
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://130.85.251.231/db.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add((NameValuePair) new BasicNameValuePair("latitude", credentials[0]));
			pairs.add((NameValuePair) new BasicNameValuePair("longitude", credentials[1]));
			pairs.add((NameValuePair) new BasicNameValuePair("time", credentials[2]));
			pairs.add((NameValuePair) new BasicNameValuePair("id", "puneetsharma"));

			try {
				post.setEntity(new UrlEncodedFormEntity(pairs));
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			ResponseHandler<String> responseHandler=new BasicResponseHandler();
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
			//Log.d("ResponseBody", responseBody);
			return responseBody;
		}	
          
	}

}
