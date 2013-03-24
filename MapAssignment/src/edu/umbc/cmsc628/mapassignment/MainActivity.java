package edu.umbc.cmsc628.mapassignment;

import java.util.HashMap;

import edu.umbc.cmsc628.mapassignment.db.ProfileDataSource;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener{

	HashMap<Boolean,String> hasStartedMap;
	boolean hasStarted=false;
	ImageButton profilerButton,mapButton;
	private ProfileDataSource dataSource;
	
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
	}
	
	@Override
    public void onClick(View v) {
		if(v.getId()==R.id.imageButton2){
			Intent mapIntent = new Intent(this, MapActivity.class);
			startActivity(mapIntent);
		}else{
			hasStarted=!hasStarted;
			TextView textView = (TextView) findViewById(R.id.textView1);
			textView.setText(hasStartedMap.get(hasStarted));
			
			//Start inserting data here. You can created more methods in this class to calculate accelerometer
			//readings, gps cordinates, orientation etc.
			//dataSource.insertProfile(profile)
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
