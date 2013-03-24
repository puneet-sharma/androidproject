package edu.umbc.cmsc628.mapassignment.db;

public class Profile {
	private float gpsX, gpsY, accel;
	private int orient, id;
	private String activity;
	
	public int getId(){
		return id;
	}
	
	public float getGpsX(){
		return gpsX;
	}
	
	public float getGpsY(){
		return gpsY;
	}
	
	public float getAccel(){
		return accel;
	}
	
	public int getOrient(){
		return orient;
	}
	
	public String getActivity(){
		return activity;
	}
	
	public void setId(int val){
		id=val;
	}
	
	public void setGpsX(float val){
		gpsX=val;
	}
	
	public void setGpsY(float val){
		gpsY=val;
	}
	
	public void setAccel(float val){
		accel=val;
	}
	
	public void setOrient(int val){
		orient=val;
	}
	
	public void setActivity(String val){
		activity=val;
	}
	
	@Override
	public String toString() {
		String str = "Lat: "+gpsX
				+ "  Long: "+gpsY
				+ "\n Accel: "+accel
				+ "\n Orient: "+orient
				+ "\n Activity: "+activity;
		return str;
	}
	
}
