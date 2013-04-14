package edu.umbc.cmsc628.mapassignment;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_TERRAIN;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.umbc.cmsc628.mapassignment.db.Profile;
import edu.umbc.cmsc628.mapassignment.db.ProfileDataSource;

public class MapActivity extends Activity implements OnItemSelectedListener {

	// static final LatLng HAMBURG = new LatLng(53.558, 9.927);
	// static final LatLng KIEL = new LatLng(53.551, 9.993);
	private GoogleMap map;
	private ProfileDataSource dataSource;
	private LocationManager locationManager;
	Location location;

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
			//new EnableGpsDialogFragment().show(getSupportFragmentManager(), "enableGpsDialog");
		}

		Criteria criteria = new Criteria();
		String provider = locationManager.getBestProvider(criteria, false);
		location = locationManager.getLastKnownLocation(provider);
		dataSource = new ProfileDataSource(this);
		setUpMapIfNeeded();
		//dataSource.insertDummyData();
		dataSource.ScanLocations();

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
		map.setInfoWindowAdapter(new PopUpAdapter(getLayoutInflater()));
		//map.setOnInfoWindowClickListener(this);
	}

	public void addMarkerAtRuntime(Profile p) {
		map.addMarker(new MarkerOptions()
				.position(new LatLng(p.getLatitude(), p.getLongitude()))
				.title(String.valueOf(p.getId())).snippet(p.toString()));
		// .icon(BitmapDescriptorFactory
		// .fromResource(R.drawable.mapmarker)));
	}

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
		}
		return super.onOptionsItemSelected(item);
	}

	private boolean checkReady() {
		if (map == null) {
			Toast.makeText(this, "Map not ready", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {

		setLayer((String) parent.getItemAtPosition(position));
	}

	private void setLayer(String layerName) {
		if (!checkReady()) {
			return;
		}
		if (layerName.equals(getString(R.string.normal))) {
			map.setMapType(MAP_TYPE_NORMAL);
		} else if (layerName.equals(getString(R.string.hybrid))) {
			map.setMapType(MAP_TYPE_HYBRID);
		} else if (layerName.equals(getString(R.string.satellite))) {
			map.setMapType(MAP_TYPE_SATELLITE);
		} else if (layerName.equals(getString(R.string.terrain))) {
			map.setMapType(MAP_TYPE_TERRAIN);
		} else {
			Log.i("LDA", "Error setting layer with name " + layerName);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}
	
	/**
     * Dialog to prompt users to enable GPS on the device.
     */
    @SuppressLint("ValidFragment")
	private class EnableGpsDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.enable_gps)
                    .setMessage(R.string.enable_gps_dialog)
                    .setPositiveButton(R.string.enable_gps, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            enableLocationSettings();
                        }
                    })
                    .create();
        }
    }


}
