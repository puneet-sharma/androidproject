package edu.umbc.CMSC628.galleria;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ListActivity {

	private int CAMERA_REQUEST_CODE =1;
	String [] classes = {"Camera","View"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(MainActivity.this,
				android.R.layout.simple_list_item_1, classes);
		this.setListAdapter(myAdapter);
		}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		
		String myclass =classes[position];
		if(myclass.compareTo("Camera")==0){
			Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
		} else if(myclass.compareTo("View")==0){
			Intent galIntent = new Intent(this, GalleryActivity.class);
			startActivity(galIntent);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == CAMERA_REQUEST_CODE){
			if ( resultCode == RESULT_OK){
				Bundle extra = data.getExtras();
				Bitmap image = (Bitmap) extra.get("data");
				saveImage(image);
			}
		}
	}
	
	protected void saveImage(Bitmap image){
		try {
			System.err.println("Application's directory path is " + this.getFilesDir());
			File file = File.createTempFile("IMG", ".png", this.getFilesDir());
			FileOutputStream ostream = new FileOutputStream(file);
			image.compress(CompressFormat.PNG, 100, ostream);
			ostream.close();
			Toast.makeText(this, file.getName() +" saved in " + this.getFilesDir(),
					Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			Toast.makeText(this, "Not enough space!", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}

}