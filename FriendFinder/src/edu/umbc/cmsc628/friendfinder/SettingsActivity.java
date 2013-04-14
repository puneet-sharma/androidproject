package edu.umbc.cmsc628.friendfinder;

import android.app.Activity;
import android.os.Bundle;

public class SettingsActivity extends Activity{
	
	public static final String KEY_PREF_AUTO_CHECK_IN = "pref_auto_check_in";
	public static final String KEY_PREF_AUTO_CHECK_IN_FREQ = "sync_frequency";
	public static final String KEY_LOGGED_USERNAME = "logged_username";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
