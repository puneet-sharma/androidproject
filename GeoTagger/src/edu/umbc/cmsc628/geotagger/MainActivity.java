package edu.umbc.cmsc628.geotagger;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity implements RecognitionListener {

	private static String TAG = "MainActivity";
	protected SpeechRecognizer mSpeechRecognizer;
	protected Intent mSpeechRecognizerIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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

	@Override
	public void onPause() {
		super.onPause();
		mNoSpeechCountDown.cancel();
		mSpeechRecognizer.cancel();
		Log.i(TAG, "Timer cancelled!");
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
		Log.i(TAG, "onError");
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
		String str = new String();

		ArrayList<String> data = results
				.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

		for (int i = 0; i < data.size(); i++) {
			str += data.get(i) + "\n";

		}

		if (str.contains("camera")) {
			Intent camIntent = new Intent(this, CameraActivity.class);
			startActivity(camIntent);
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

	protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(240000,
			8000) {

		@Override
		public void onTick(long millisUntilFinished) {
			Log.d(TAG, "onTick");
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
}