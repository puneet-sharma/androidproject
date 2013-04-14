package edu.umbc.cmsc628.mapassignment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

class PopUpAdapter implements InfoWindowAdapter {
  LayoutInflater inflater=null;

  PopUpAdapter(LayoutInflater inflater) {
    this.inflater=inflater;
  }

  @Override
  public View getInfoWindow(Marker marker) {
    return(null);
  }

  @Override
  public View getInfoContents(Marker marker) {
    View popup=inflater.inflate(R.layout.popup, null);

    TextView activity=(TextView)popup.findViewById(R.id.activity);
    TextView latitude=(TextView)popup.findViewById(R.id.latitude);
    TextView longitude=(TextView)popup.findViewById(R.id.longitude);
    TextView accel=(TextView)popup.findViewById(R.id.accel);
    TextView orient=(TextView)popup.findViewById(R.id.orient);
    TextView address=(TextView)popup.findViewById(R.id.address);
    
    String snippet = marker.getSnippet();
    String tokens[] = snippet.split("\\$");
    
    latitude.setText("Latitude: "+tokens[0]);
    longitude.setText("Longitude "+tokens[1]);
    accel.setText("Accelerometer "+tokens[2]);
    orient.setText("Orientation "+tokens[3]);
    activity.setText("Activity: "+tokens[4]);
    address.setText("Address :"+tokens[5]);

    return(popup);
  }
}
