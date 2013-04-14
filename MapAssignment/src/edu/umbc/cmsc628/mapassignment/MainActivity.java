package edu.umbc.cmsc628.mapassignment;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import edu.umbc.cmsc628.mapassignment.db.Profile;
import edu.umbc.cmsc628.mapassignment.db.ProfileDataSource;

public class MainActivity extends FragmentActivity implements OnClickListener,
		LocationListener, SensorEventListener {

	final int MIN_TIME = 10;
	final int MIN_DISTANCE = 100;
	HashMap<Boolean, String> hasStartedMap;
	boolean hasStarted = false;
	ImageButton profilerButton, mapButton;
	private ProfileDataSource dataSource;
	private LocationManager locationManager;
	private SensorManager sensorMgr;
	private Sensor accelSensor;
	private double accel;
	private Location lastKnownLocation;

	// private Sensor orientSensor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		hasStartedMap = new HashMap<Boolean, String>();
		hasStartedMap.put(true, "Stop service");
		hasStartedMap.put(false, "Start service");

		profilerButton = (ImageButton) findViewById(R.id.imageButton1);
		profilerButton.setOnClickListener(this);

		mapButton = (ImageButton) findViewById(R.id.imageButton2);
		mapButton.setOnClickListener(this);
		dataSource = new ProfileDataSource(this);
		//dataSource.deleteTable();
		sensorMgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		// orientSensor=sensorMgr.getDefaultSensor(Sensor.TYPE_ORIENTATION);

	}

	private void enableLocationSettings() {
		Intent settingsIntent = new Intent(
				Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(settingsIntent);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.imageButton2) {
			Intent mapIntent = new Intent(this, MapActivity.class);
			startActivity(mapIntent);
		} else {
			hasStarted = !hasStarted;
			TextView textView = (TextView) findViewById(R.id.textView1);
			textView.setText(hasStartedMap.get(hasStarted));

			if (hasStarted) {
				locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				boolean enabled = locationManager
						.isProviderEnabled(LocationManager.GPS_PROVIDER);
				if (!enabled) {
					new EnableGpsDialogFragment().show(
							getSupportFragmentManager(), "enableGpsDialog");
				}
				locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, MIN_TIME * 1000,
						MIN_DISTANCE, this);
			} else {
				sensorMgr.unregisterListener(this);
				locationManager.removeUpdates(this);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		if(lastKnownLocation==null){
			lastKnownLocation=location;
		}else if(location.distanceTo(lastKnownLocation)<MIN_DISTANCE*0.8){
			return;
		}
		
		final Profile profile = new Profile();
		profile.setLatitude(location.getLatitude());
		profile.setLongitude(location.getLongitude());
		profile.setAccel(accel);
		int orientation = getResources().getConfiguration().orientation;
		profile.setOrient(orientation);
		//profile.setActivity("Walking");
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Enter Activity");
		alert.setMessage("Enter briefly whatever you're doing");

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		alert.setView(input);
		
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				profile.setActivity(value);
				if (Geocoder.isPresent()) {
					(new InsertProfileWithRevGeo())
							.execute(new Profile[] { profile });
				} else {
					(new InsertProfileWithoutRevGeo())
							.execute(new Profile[] { profile });
				}
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});
		
		if(profile.getOrient()==2 ){
			alert.show();
		}else{
			String activity = getActivity(profile.getAccel());
			profile.setActivity(activity);
			
			if (Geocoder.isPresent()) {
				(new InsertProfileWithRevGeo())
						.execute(new Profile[] { profile });
			} else {
				(new InsertProfileWithoutRevGeo())
						.execute(new Profile[] { profile });
			}
		}
		
//		new Handler().postDelayed(new Runnable() {
//			public void run() {
//				profile.setActivity(ad.get);
//				ad.dismiss();
//			}
//		}, MIN_TIME * 500);

	}

	private String getActivity(double accel) {
		if(accel>1 && accel < 3){
			return "Walking";
		}else if (accel>=5){
			return "Driving";
		}else{
			return "Stationary";
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			float x = Float.valueOf(String.format(Locale.getDefault(), "%.2f",
					event.values[0]));
			float y = Float.valueOf(String.format("%.1f", event.values[1]));
			float z = Float.valueOf(String.format("%.1f", event.values[2]));
			double threeDimAccl = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)
					+ Math.pow(z, 2));
			accel = Math.abs(threeDimAccl - SensorManager.GRAVITY_EARTH);
			accel = Double.valueOf(String.format(Locale.getDefault(), "%.2f",
					accel));
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		System.err.println("Paused");
		
		if(sensorMgr!=null)
			sensorMgr.unregisterListener(this);
		if(locationManager!=null)
			locationManager.removeUpdates(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (hasStarted) {
			sensorMgr.registerListener(this, accelSensor,
					SensorManager.SENSOR_DELAY_NORMAL);
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, MIN_TIME * 1000,
					MIN_DISTANCE, this);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	private class InsertProfileWithRevGeo extends
			AsyncTask<Profile, Void, Void> {
		Context mContext;
		String message;

		public InsertProfileWithRevGeo() {
			super();
			mContext = getApplicationContext();
		}

		@Override
		protected Void doInBackground(Profile... params) {
			Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

			Profile profile = params[0];
			List<Address> addresses = null;
			try {
				// Call the synchronous getFromLocation() method by passing in
				// the lat/long values.
				addresses = geocoder.getFromLocation(profile.getLatitude(),
						profile.getLongitude(), 1);
			} catch (IOException e) {
				e.printStackTrace();
				profile.setAddress("Address not found");
				message = "Address not found";
			}
			if (addresses != null && addresses.size() > 0) {
				Address address = addresses.get(0);
				// Format the first line of address (if available), city, and
				// country name.
				String addressText = String.format(
						"%s, %s, %s",
						address.getMaxAddressLineIndex() > 0 ? address
								.getAddressLine(0) : "", address.getLocality(),
						address.getCountryName());
				profile.setAddress(addressText);
				// Insert in DB
				dataSource.insertProfile(profile);
				message = profile.toString();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			Toast toast = Toast.makeText(getApplicationContext(), message,
					Toast.LENGTH_SHORT);
			toast.show();
			toast = Toast.makeText(getApplicationContext(), "Row inserted",
					Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	private class InsertProfileWithoutRevGeo extends
			AsyncTask<Profile, Void, Void> {

		String message;

		public InsertProfileWithoutRevGeo() {
			super();
		}

		@Override
		protected Void doInBackground(Profile... params) {
			Profile profile = params[0];
			// Insert in DB
			dataSource.insertProfile(profile);
			message = profile.toString();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			Toast toast = Toast.makeText(getApplicationContext(), message,
					Toast.LENGTH_SHORT);
			toast.show();
			toast = Toast.makeText(getApplicationContext(), "Row inserted",
					Toast.LENGTH_SHORT);
			toast.show();
		}
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
					.setPositiveButton(R.string.enable_gps,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									enableLocationSettings();
								}
							}).create();
		}
	}
}
