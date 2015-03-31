/*
 * || NodeID | TimeStamp | LocalTime (x) | Type | Latitude | Longitude ||
 *
 */

package com.subhadeep.messiahlayer;

import java.io.Serializable;

public class Message implements Serializable{


	private static final long serialVersionUID = -5500380069863053052L;
	
	private long nodeID;
	private long timestamp;
	private long localtime;
	private int type;
	private double latitude;
	private double longitude;
	private String route_lat;
	private String route_lon;
	private double destination_lat;
	private double destination_lon;

	public Message(long id, long timestamp, long localtime, int type, double lat, double lon, String route_lat, String route_lon, double destination_lat, double destination_lon)
	{
		this.nodeID = id;
		this.timestamp = timestamp;
		this.localtime = localtime;
		this.type = type;
		this.latitude = lat;
		this.longitude = lon;
		this.route_lat = route_lat;
		this.route_lon = route_lon;
		this.destination_lat = destination_lat;
		this.destination_lon = destination_lon;
	}
	
	public long getNodeID()
	{
		return nodeID;
	}
	
	public long getTimeStamp()
	{
		return timestamp;
	}
	
	public long getLocalTime()
	{
		return localtime;
	}
	
	public int getType()
	{
		return type;
	}
	
	public double getLatitude()
	{
		return latitude;
	}
	
	public double getLongitude()
	{
		return longitude;
	}

	public String getRouteLat()
	{
		return route_lat;
	}
	
	public String getRouteLon()
	{
		return route_lon;
	}
	
	public double getDestinationLat()
	{
		return destination_lat;		
	}
	
	public double getDestinationLon()
	{
		return destination_lon;
	}
}
