package edu.umbc.cmsc628.mapassignment.db;

public class Profile {
	private double lati, longi, accel;
	private int orient, id;
	private String activity, address;
	
	public String getAddress(){
		return address;
	}
	public int getId(){
		return id;
	}
	
	public double getLatitude(){
		return lati;
	}
	
	public double getLongitude(){
		return longi;
	}
	
	public double getAccel(){
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
	
	public void setLatitude(double val){
		lati=val;
	}
	
	public void setLongitude(double val){
		longi=val;
	}
	
	public void setAccel(double val){
		accel=val;
	}
	
	public void setOrient(int val){
		orient=val;
	}
	
	public void setActivity(String val){
		activity=val;
	}
	
	public void setAddress(String val){
		address = val;
	}
	
	@Override
	public String toString() {
		String str = lati+"$"
				+ longi+"$"
				+ accel+"$"
				+ orient+"$"
				+ activity+"$"
				+ address;
		return str;
	}
	
}
