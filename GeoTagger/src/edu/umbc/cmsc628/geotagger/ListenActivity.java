package edu.umbc.cmsc628.geotagger;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ListenActivity extends Activity implements RecognitionListener, LocationListener, OnClickListener {

	private static String TAG = "MainActivity";
	protected SpeechRecognizer mSpeechRecognizer;
	protected Intent mSpeechRecognizerIntent;
	private LocationManager locationManager;
	private Button stopButton;
	
	
	LocationType type;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.activity_listen);
		stopButton = (Button) findViewById(R.id.buttonStopListening);
		stopButton.setOnClickListener(this);
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		boolean enabled = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (!enabled) {
			enableLocationSettings();
		}

		mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
		mSpeechRecognizer.setRecognitionListener(this);
		mSpeechRecognizerIntent = new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		mSpeechRecognizerIntent.putExtra(
				RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
		mNoSpeechCountDown.start();
	}

	private void enableLocationSettings() {
		Intent settingsIntent = new Intent(
				Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(settingsIntent);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.i(TAG, "on pause");
	}

	@Override
	public void onResume() {
		super.onResume();
		mNoSpeechCountDown.start();
		Log.i(TAG, "onResume");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy");
		mNoSpeechCountDown.cancel();
		mSpeechRecognizer.cancel();
		mSpeechRecognizer.destroy();
		if (locationManager != null)
			locationManager.removeUpdates(this);
	}

	@Override
	public void onBeginningOfSpeech() {
		Log.i(TAG, "onBeginningOfSpeech");
	}

	@Override
	public void onBufferReceived(byte[] buffer) {
		Log.i(TAG, "onBufferReceived");
	}

	@Override
	public void onEndOfSpeech() {
		Log.i(TAG, "onEndOfSpeech");
	}

	@Override
	public void onError(int error) {
		//Log.i(TAG, "onError");
		mNoSpeechCountDown.cancel();
		mNoSpeechCountDown.start();
	}

	@Override
	public void onEvent(int eventType, Bundle params) {
		Log.i(TAG, "onEvent");
	}

	@Override
	public void onPartialResults(Bundle partialResults) {
		Log.i(TAG, "onPartialResults");
	}

	@Override
	public void onReadyForSpeech(Bundle params) {
		Log.i(TAG, "onReadyForSpeech");
	}

	@Override
	public void onResults(Bundle results) {
		Log.i(TAG, "onResults");
		boolean matchFound =false;

		ArrayList<String> data = results
				.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

		for (int i = 0; i < data.size(); i++) {
			if(LocationType.isLocationType(data.get(i))){
				matchFound=true;
				type = LocationType.valueOf(data.get(i));
				break;
			}
		}
		if (matchFound) {
			System.err.println("Matchfound!");
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, this);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 0, this);
		} else {
			Toast.makeText(
					getApplicationContext(),
					"Are you sure "+data.get(0)+" is a keyword I'm looking for? I don't think so!",
					Toast.LENGTH_SHORT).show();
		}
		mNoSpeechCountDown.cancel();
		mNoSpeechCountDown.start();
	}
	
	@Override
	public void onRmsChanged(float rmsdB) {
		// Log.i(TAG, "onRmsChanged");
	}

	//Run for 2 hours, listen every 6 seconds
	protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(7200000,
			6000) {

		@Override
		public void onTick(long millisUntilFinished) {
			//Log.d(TAG, "onTick");
			mSpeechRecognizer.cancel();
			mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
		}

		@Override
		public void onFinish() {
			Log.d(TAG, "onFinish");
			mSpeechRecognizer.cancel();
			finish();
		}
	};

	@Override
	public void onLocationChanged(Location location) {
		System.err.println("GPS coordinates recieved");
		Intent camIntent = new Intent(this, CameraActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("latitude", String.valueOf(location.getLatitude()));
		bundle.putString("longitude", String.valueOf(location.getLongitude()));
		bundle.putString("type", String.valueOf(type));
		camIntent.putExtras(bundle);
		startActivity(camIntent);
		if (locationManager != null)
			locationManager.removeUpdates(this);
		
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

	@Override
	public void onClick(View v) {
		mNoSpeechCountDown.cancel();
		mSpeechRecognizer.cancel();
		finish();
	}
}