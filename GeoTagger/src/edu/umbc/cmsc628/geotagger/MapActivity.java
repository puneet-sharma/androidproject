package edu.umbc.cmsc628.geotagger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends Activity implements OnItemSelectedListener,
		LocationListener, OnMarkerClickListener {

	private GoogleMap map;
	private LocationManager locationManager;
	Location location;
	private final static String SERVER_IP = "http://mpss.csce.uark.edu/~sharma_fan/";
	private Marker clickedMarker;
	View infoWindowView;

//	Handler handler = new Handler() {
//		@Override
//		public void handleMessage(Message msg) {
//			System.err.println("Message received!");
//			clickedMarker.hideInfoWindow();
//			clickedMarker.showInfoWindow();
//		}
//	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		Spinner spinner = (Spinner) findViewById(R.id.layers_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.layers_array, R.layout.activity_map_spinner);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		boolean enabled = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (!enabled) {
			enableLocationSettings();
			// new EnableGpsDialogFragment().show(getSupportFragmentManager(),
			// "enableGpsDialog");
		}

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, this);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, this);
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
				new LatLng(location.getLatitude(), location.getLongitude()), 16));
		//map.setInfoWindowAdapter(new PopUpAdapter(getLayoutInflater()));
		map.setInfoWindowAdapter(new InfoWindowAdapter() {

            // Use default InfoWindow frame
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            // Defines the contents of the InfoWindow
            public View getInfoContents(Marker arg0) {
                if (clickedMarker.isInfoWindowShown()) {
                    return infoWindowView;
                } else {
                    // Getting view from the layout file info_window_layout
                    infoWindowView = getLayoutInflater().inflate(
                            R.layout.popup, null);
                    // Stash the base view in infoWindowView
                    new FillImage(getApplicationContext(), infoWindowView, arg0).execute(arg0.getSnippet());

                    // Returning the view containing InfoWindow contents
                    return infoWindowView;
                }
            }
        });
		map.setOnMarkerClickListener(this);
		// map.setOnInfoWindowClickListener(this);
		new GetLocationsTask().execute(LocationType.all);
	}
	
	private void enableLocationSettings() {
		Intent settingsIntent = new Intent(
				Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(settingsIntent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_map, menu);
		return true;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {

		LocationType filter = LocationType.all;
		switch (position) {
		case 1:
			filter = LocationType.stop;
			break;
		case 2:
			filter = LocationType.traffic;
			break;
		case 3:
			filter = LocationType.signal;
			break;
		case 4:
			filter = LocationType.accident;
			break;
		}
		if(map!=null)
			new GetLocationsTask().execute(filter);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocationChanged(Location loc) {
		location = loc;
		setUpMapIfNeeded();
		if (locationManager != null)
			locationManager.removeUpdates(this);
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
		// TODO Auto-generated method stub

	}

	private class GetLocationsTask extends
			AsyncTask<LocationType, Long, String> {

		JSONArray locations = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (map != null)
				map.clear();
		}

		@Override
		protected String doInBackground(LocationType... arg0) {
			LocationType filter = arg0[0];
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(SERVER_IP + "GetLocations.php");

			List<NameValuePair> pairs = new ArrayList<NameValuePair>();

			pairs.add((NameValuePair) new BasicNameValuePair("filter", filter
					.toString().toLowerCase(Locale.US)));

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
				System.err.println(responseBody);
				JSONObject json = new JSONObject(responseBody);
				locations = json.getJSONArray("locations");
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
			if (locations == null)
				finish();

			for (int i = 0; i < locations.length(); i++) {
				JSONObject location;
				try {
					location = locations.getJSONObject(i).getJSONObject(
							"location");
					// String time = location.getString("time");
					LocationType type = LocationType.valueOf(location
							.getString("type"));
					String imageURL = SERVER_IP.concat(
							location.getString("filename".replace("\\", "")))
							.concat(".jpg");
					double latitude = location.getDouble("latitude");
					double longitude = location.getDouble("longitude");

					switch (type) {
					case stop:
						map.addMarker(new MarkerOptions()
								.position(new LatLng(latitude, longitude))
								.title("Stop sign")
								.snippet(imageURL)
								.icon(BitmapDescriptorFactory
										.fromResource(R.drawable.stop_sign)));
						break;
					case accident:
						map.addMarker(new MarkerOptions()
								.position(new LatLng(latitude, longitude))
								.title("Accident")
								.snippet(imageURL)
								.icon(BitmapDescriptorFactory
										.fromResource(R.drawable.accident)));
						break;
					case construction:
						map.addMarker(new MarkerOptions()
								.position(new LatLng(latitude, longitude))
								.title("Construction")
								.snippet(imageURL)
								.icon(BitmapDescriptorFactory
										.fromResource(R.drawable.construction_icon)));
						break;
					case signal:
						map.addMarker(new MarkerOptions()
								.position(new LatLng(latitude, longitude))
								.title("traffic signal")
								.snippet(imageURL)
								.icon(BitmapDescriptorFactory
										.fromResource(R.drawable.traffic_signal)));
						break;
					case traffic:
						map.addMarker(new MarkerOptions()
								.position(new LatLng(latitude, longitude))
								.title("High traffic")
								.snippet(imageURL)
								.icon(BitmapDescriptorFactory
										.fromResource(R.drawable.traffic)));
						break;
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		System.err.println("In marker click");
		clickedMarker = arg0;
		infoWindowView = null;
		return false;
		
	}
	
	private class FillImage extends AsyncTask<String, Void, Bitmap> {

		Marker marker;
		View mainView;
		public FillImage(Context applicationContext, View infoWindowView,
				Marker arg0) {
			marker = arg0;
			mainView = infoWindowView;
			
		}

		@Override
		protected Bitmap doInBackground(String... arg0) {
			String imageUrl = arg0[0];
			URL url;
			Bitmap bmp = null;
			try {
				url = new URL(imageUrl);
				bmp = BitmapFactory.decodeStream(url.openConnection()
						.getInputStream());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return bmp;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			ImageView thumb = (ImageView) mainView.findViewById(R.id.thumbnail);
			thumb.setImageBitmap(result);
			marker.showInfoWindow();
		}
	}

}
