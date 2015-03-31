/*
 * CHANGE THE getLatitude() and getLongitude FUNCTIONS
 * 
 * 
 * This class is used to access the location of the device (not updated)
 * Also to calculate the distance between two points
 * 
 */

package com.subhadeep.messiahlayer;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;


public class Locator {

	
	private Location location;
	private LocationManager locationManager;
	
	private boolean locationAvailable;
	
	public Locator(Context context)
	{	
		try	
		{
			locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);		
		}
		catch(Exception e)
		{
			//do nothing
		}
	}
	
	public boolean isLocationAvailable()
	{
		locationAvailable = false;
		try	
		{	
			location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if(location != null)
				locationAvailable = true;
		}
		catch(Exception e)
		{
			//do nothing
		}
		return locationAvailable;
	}
	
	public double getLatitude()
	{	
		if(isLocationAvailable())
			return location.getLatitude();
		return 39.480593; //only for testing
	}
	
	public double getLongitude()
	{
		if(isLocationAvailable())
			return location.getLongitude();
		return -0.346554;
	}
	
	public double distance(double lat, double lon)
	{
		//return distance in km
		double lat1 = getLatitude();
		double lat2 = lat;
		double lon1 = getLongitude();
		double lon2 = lon;
        int R = 6371; // km
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lon2-lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + 
                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
        return R * c;
	}

}
