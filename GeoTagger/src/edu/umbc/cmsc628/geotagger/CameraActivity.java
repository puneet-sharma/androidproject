package edu.umbc.cmsc628.geotagger;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.Toast;

public class CameraActivity extends Activity implements OnClickListener, PictureCallback {

	CameraSurfaceView cameraSurfaceView;
	//Button shutterButton;
	private final static String DEBUG_TAG = "CameraActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);

		// set up our preview surface
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		cameraSurfaceView = new CameraSurfaceView(this);
		preview.addView(cameraSurfaceView);

		// grab out shutter button so we can reference it later
		//shutterButton = (Button) findViewById(R.id.shutter_button);
		//shutterButton.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_camera, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		takePicture();
	}

	private void takePicture() {
		//shutterButton.setEnabled(false);
		cameraSurfaceView.takePicture(this);
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		// TODO something with the image data

		File pictureFileDir = getDir();
		System.err.println("Directory "+getDir().getAbsolutePath());
		Context context = getApplicationContext();
		
		if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

			Log.d(DEBUG_TAG,
					"Can't create directory to save image.");
			Toast.makeText(context, "Can't create directory to save image.",
					Toast.LENGTH_LONG).show();
			return;

		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss", Locale.ENGLISH);
		String date = dateFormat.format(new Date());
		String photoFile = "Picture_" + date + ".jpg";
		
		String filename = pictureFileDir.getPath() + File.separator + photoFile;
		Log.d(DEBUG_TAG, filename);
		
		File pictureFile = new File(filename);

		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			fos.write(data);
			fos.close();
			Toast.makeText(context, "New Image saved:" + photoFile,
					Toast.LENGTH_LONG).show();
		} catch (Exception error) {
			Log.d(DEBUG_TAG, "File" + filename
					+ "not saved: " + error.getMessage());
			Toast.makeText(context, "Image could not be saved.",
					Toast.LENGTH_LONG).show();
		}
		
		finish();
		// Restart the preview and re-enable the shutter button so that we can take another picture
		//camera.startPreview();
		//shutterButton.setEnabled(true);
	}
	
	private File getDir() {
		File sdDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		return new File(sdDir, "CameraAPIDemo");
	}
}