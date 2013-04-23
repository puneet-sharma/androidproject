package edu.umbc.cmsc628.geotagger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class MainActivity extends Activity implements OnClickListener{

	ImageButton buttonMic, buttonMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonMic = (ImageButton) findViewById(R.id.imageButton1);
        buttonMap = (ImageButton) findViewById(R.id.imageButton2);
        buttonMic.setOnClickListener(this);
        buttonMap.setOnClickListener(this);
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	@Override
	public void onClick(View v) {

		if(v.getId()==R.id.imageButton1){
			Intent listenIntent = new Intent(this, ListenActivity.class);
			startActivity(listenIntent);
		}else{
			Intent mapIntent = new Intent(this, MapActivity.class);
			startActivity(mapIntent);
		}
	}
    
}
