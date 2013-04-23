package edu.umbc.cmsc628.geotagger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
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
	private final static String SERVER_IP = "130.85.241.132";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);

		// set up our preview surface
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		cameraSurfaceView = new CameraSurfaceView(this);
		preview.addView(cameraSurfaceView);
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

		//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss", Locale.ENGLISH);
		//String date = dateFormat.format(new Date());
		long currentMilli = System.currentTimeMillis();
		String photoFile = currentMilli + ".jpg";
		
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
		
		new UploadPic().execute(filename);
		finish();

	}
	
	private File getDir() {
		File sdDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		return new File(sdDir, "Geotagger");
	}
	
	private class UploadPic extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... args) {
			String fileName = args[0];
			String response = "";
			HttpClient httpClient = new DefaultHttpClient();
			//HttpContext localContext = new BasicHttpContext();
			HttpPost httpPost = new HttpPost("http://" + SERVER_IP + "/upload.php");
			String latitude = getIntent().getExtras().getString("latitude");
			String longitude = getIntent().getExtras().getString("longitude");
			String type = getIntent().getExtras().getString("type");
			
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add((NameValuePair) new BasicNameValuePair("image",
					fileName));
			pairs.add((NameValuePair) new BasicNameValuePair("latitude",
					latitude));
			pairs.add((NameValuePair) new BasicNameValuePair("longitude",
					longitude));
			pairs.add((NameValuePair) new BasicNameValuePair("type",
					type));
			
			try {
		        MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		        for(int index=0; index < pairs.size(); index++) {
		            if(pairs.get(index).getName().equalsIgnoreCase("image")) {
		                // If the key equals to "image", we use FileBody to transfer the data
		                entity.addPart(pairs.get(index).getName(), new FileBody(new File (pairs.get(index).getValue())));
		            } else {
		                // Normal string data
		                entity.addPart(pairs.get(index).getName(), new StringBody(pairs.get(index).getValue()));
		            }
		        }

		        httpPost.setEntity(entity);

		        response = httpClient.execute(httpPost, new BasicResponseHandler());
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
			return response.toString();
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.d(DEBUG_TAG,result);
			Toast.makeText(getApplicationContext(), result,
					Toast.LENGTH_LONG).show();
		}
		
	}
}