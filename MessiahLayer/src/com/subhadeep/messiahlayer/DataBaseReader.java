/*
 *  This class is used by the DrawMessiah class to read values for the database and place icons accordingly
 * 
 *	It has been assumed that the data is stored in the table in the following format and column names:
 *	|| NodeID | TimeStamp | LocalTime | Type | Latitude | Longitude ||
*/

package com.subhadeep.messiahlayer;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseReader extends SQLiteOpenHelper{

	private long nodeID[];
	private int type[];
	private String lat[];
	private String lon[];
	
	private String routeLat[];
	private String routeLon[];
	
	private String destinationLat[];
	private String destinationLon[];
	
	private boolean results_available;
	
	private volatile SQLiteDatabase database;
	
	private static final int VERSION = 1;
	
	public static final int TYPE_ME = 0;
	public static final int TYPE_AUTHORITY = 1;
	public static final int TYPE_SOS = 2;
	public static final int TYPE_MOB = 3;			//FUTURE WORK
	public static final int TYPE_MESSAGE = 4;		//FUTURE WORK
	public static final int TYPE_STOP = 5;
	
	private String DB_PATH;
	private static final String DB_NAME = "messiah_file.db"; //name of the database file
	private static final String TABLE = "messiah_table";	//name of the table
	private static final String SUB_FOLDER = "Messiah";		//sub folder under the current directory where the database will be stored
	
	
	public DataBaseReader(Context context) {
		super(context, DB_NAME, null, VERSION);
		// TODO Auto-generated constructor stub
		DB_PATH = context.getFilesDir().toString() + File.separator + SUB_FOLDER + File.separator;
	}
	
	public void readFromDB()
	{
		File file = new File(DB_PATH);
		try{
			if(!file.exists())
			{
				//the sub-folder has not yet been created so no data available
				results_available = false;
				type = null;
				nodeID = null;
				lat = lon = null;
				return;
			}
			database = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READONLY);
			if(database == null)
			{
				//file does not exist so no data is available yet
				results_available = false;
				type = null;
				nodeID = null;
				lat = lon = null;
				return;
			}
			//now try to read the table
			Cursor csr = database.rawQuery("SELECT * FROM " + TABLE + ";", null);
			if(!csr.moveToFirst())
			{
				//control will enter here if cursor is empty or has no elements
				results_available = false;
				type = null;
				nodeID = null;
				lat = lon = null;
				csr.close();
				database.close();
				return;
			}
			ArrayList<String> node_list = new ArrayList<String>();
			ArrayList<String> lat_list = new ArrayList<String>();
			ArrayList<String> lon_list = new ArrayList<String>();
			ArrayList<String> type_list = new ArrayList<String>();
			
			ArrayList<String> route_lat_list = new ArrayList<String>();
			ArrayList<String> route_lon_list = new ArrayList<String>();
			ArrayList<String> destination_lat_list = new ArrayList<String>();
			ArrayList<String> destination_lon_list = new ArrayList<String>();
			
			//else count the number of rows			
			do{
				if(csr.getInt(csr.getColumnIndex("Type")) != TYPE_STOP)
				{
					node_list.add(csr.getString(csr.getColumnIndex("NodeID")));
					lat_list.add(csr.getString(csr.getColumnIndex("Latitude")));
					lon_list.add(csr.getString(csr.getColumnIndex("Longitude")));
					type_list.add(csr.getString(csr.getColumnIndex("Type")));
					route_lat_list.add(csr.getString(csr.getColumnIndex("Route_Lat")));
					route_lon_list.add(csr.getString(csr.getColumnIndex("Route_Lon")));
					destination_lat_list.add(csr.getString(csr.getColumnIndex("Destination_Lat")));
					destination_lon_list.add(csr.getString(csr.getColumnIndex("Destination_Lon")));
				}
			}while(csr.moveToNext());
			//now allocate the arrays to store the results and also set the cursor to the beginning			
			csr.close();
			Log.e("database reading", "in reader");
			print();
			database.close();
			nodeID = new long[node_list.size()];			
			lat = new String[lat_list.size()];
			lon = new String[lon_list.size()];
			type = new int[type_list.size()];
			
			routeLat = new String[route_lat_list.size()];
			routeLon = new String[route_lon_list.size()];
			destinationLat = new String[destination_lat_list.size()];
			destinationLon = new String[destination_lon_list.size()];
			
			for(int i = 0; i < nodeID.length; i++)
			{
				nodeID[i] = Long.parseLong(node_list.get(i));
				lat[i] = lat_list.get(i);
				lon[i] = lon_list.get(i);
				type[i] = Integer.parseInt(type_list.get(i));
				routeLat[i] = route_lat_list.get(i);
				routeLon[i] = route_lon_list.get(i);
				destinationLat[i] = destination_lat_list.get(i);
				destinationLon[i] = destination_lon_list.get(i);
			}
			node_list = lat_list = lon_list = type_list = null;
			results_available = true;
			
		}catch(Exception e)
		{
			results_available = false;
			database.close();
			//Log.e("error", e.toString());
		}
	}
	
	public void print()
	{
		Cursor csr = database.rawQuery("SELECT * FROM " + TABLE + ";", null);
		if(!csr.moveToFirst())
		{
			csr.close();
			return;
		}
		do{
			Log.e("record", Long.toString(csr.getLong(csr.getColumnIndex("NodeID"))));
			Log.e("record mode", Integer.toString(csr.getInt(csr.getColumnIndex("Type"))));
		}while(csr.moveToNext());
		csr.close();
	}
	
	public long[] getID()
	{
		if(results_available)
			return nodeID;
		return null;
	}
	
	public int[] getType()
	{
		if(results_available)
			return type;
		return null;
	}
	
	public String[] getLat()
	{
		if(results_available)
			return lat;
		return null;
	}
	
	public String[] getLon()
	{
		if(results_available)
			return lon;
		return null;
	}
	
	public String[] getRouteLat()
	{
		if(results_available)
			return routeLat;
		return null;
	}
	
	public String[] getRouteLon()
	{
		if(results_available)
			return routeLon;
		return null;
	}
	
	public String[] getDestinationLat()
	{
		if(results_available)
			return destinationLat;
		return null;
	}
	
	public String[] getDestinationLon()
	{
		if(results_available)
			return destinationLon;
		return null;
	}
	
	@Override
	public void onCreate(SQLiteDatabase arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
