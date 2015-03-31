
/*
 * No support for update a record yet
 * 
 * || NodeID | TimeStamp | LocalTime | Type | Latitude | Longitude ||
 */

package com.subhadeep.messiahlayer;

import java.io.File;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;


public class DataBaseWriter extends SQLiteOpenHelper{

	private static final long MAX_STRING_LENGTH = 500; //ADJUSTABLE
	
	private static final int VERSION = 1;
	
	private String DB_PATH;
	private static final String DB_NAME = "messiah_file.db";
	private static final String TABLE = "messiah_table";
	private static final String SUB_FOLDER = "Messiah";
	
	private Context context;
	
	private SQLiteDatabase database;
	
	@SuppressLint("NewApi")
	public DataBaseWriter(Context context) {
		super(context, DB_NAME, null, VERSION);
		this.context = context;
		DB_PATH = context.getFilesDir().toString() + File.separator + SUB_FOLDER + File.separator;
		File file = new File(DB_PATH);
		try{
			if(!file.exists())
			{
				file.mkdir();
				file.setWritable(true);
			}
			database = SQLiteDatabase.openOrCreateDatabase(DB_PATH + DB_NAME, null);
			database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE + "(NodeID INTEGER PRIMARY KEY, TimeStamp INTEGER, LocalTime INTEGER," +
					" Type INTEGER, Latitude VARCHAR, Longitude VARCHAR, Route_Lat VARCHAR, Route_Lon VARCHAR, Destination_Lat VARCHAR, Destination_Lon VARCHAR);");
			
			Log.e("DATABASE OPEN", "DATABASE OPEN");
		}catch(Exception e)
		{
			Log.e("error", e.toString());
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}

	/*
	public boolean databaseExist()
	{
	    File dbFile = new File(DB_PATH + DB_NAME);
	    return dbFile.exists();
	}*/

	
	public boolean addEntry(long nodeID, long timeStamp, long localTime, int type, double lat, double lon, String route_lat, String route_lon, String destination_lat, String destination_lon)
	{
		boolean flag = false;
		ContentValues values = new ContentValues();
        //values.put("NodeID", nodeID);
        values.put("TimeStamp", timeStamp);
        values.put("LocalTime", localTime);
        values.put("Type", type);
        
        values.put("Route_Lat", route_lat);
        values.put("Route_Lon", route_lon);
        values.put("Destination_Lat", destination_lat);
        values.put("Destination_Lon", destination_lon);
        
        // insert row
        database.beginTransaction();
        Cursor csr = database.rawQuery("SELECT * FROM " + TABLE + " WHERE NodeID=" + nodeID + ";", null);
        if(csr.moveToFirst())
        {	//successfully found query
        	if(csr.getLong(csr.getColumnIndex("TimeStamp")) < timeStamp)
        	{
        		Log.e("db time stamp lower", csr.getString(csr.getColumnIndex("TimeStamp")) + " < " + Long.toString(timeStamp));
        		String lat_list[] = new Tokenizer(csr.getString(csr.getColumnIndex("Latitude"))).getTokens();
        		String lon_list[] = new Tokenizer(csr.getString(csr.getColumnIndex("Longitude"))).getTokens();
        		if(lat != Double.parseDouble(lat_list[lat_list.length-1]) && lon != Double.parseDouble(lon_list[lon_list.length -1]))
        		{
        			//the previous position is not the same as current position        			
        			String lat_temp = new Tokenizer(csr.getString(csr.getColumnIndex("Latitude"))).addToken(Double.toString(lat));
        			String lon_temp = new Tokenizer(csr.getString(csr.getColumnIndex("Longitude"))).addToken(Double.toString(lon));
        			while(lat_temp.length() > MAX_STRING_LENGTH || lon_temp.length() > MAX_STRING_LENGTH)
        			{
        				//delete part of the string
        				lat_temp = new Tokenizer(lat_temp).deleteToken();
        				lon_temp = new Tokenizer(lon_temp).deleteToken();
        			}
        			values.put("Latitude", lat_temp);
        			values.put("Longitude", lon_temp);
        		}
        		else
        		{  //no need to append the values as the new onces are the same as the old onces        			
        			values.put("Latitude", csr.getString(csr.getColumnIndex("Latitude")));
        			values.put("Longitude", csr.getString(csr.getColumnIndex("Longitude")));
        		}
        		if(database.update(TABLE, values,"NodeID=" + nodeID, null) > 0) //first try to update 
        			flag = true;        		
        	}
        }        
        else
        {
        	//update has failed so try to insert
        	values.put("Latitude", new Tokenizer().addToken(Double.toString(lat)));
            values.put("Longitude", new Tokenizer().addToken(Double.toString(lon)));
        	values.put("NodeID", nodeID);
        	if(database.insert(TABLE, null, values) != -1)
        		flag = true;
        }
        csr.close();
        database.setTransactionSuccessful();
		database.endTransaction();
		return flag;
	}
	

	public boolean deleteEntry(long id)
	{
		boolean flag = true;
		database.beginTransaction();
		if(database.delete(TABLE, "NodeID="+id, null) == 0)
			flag = false;
		database.setTransactionSuccessful();
		database.endTransaction();
		return flag;
	}
	
	public void deleteAllAndClose()
	{
		//to be used when plugin has been stopped
		try{
			database.beginTransaction();
			//just equivalent to clearing the table
			database.delete(TABLE, null, null);
			database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE + "(NodeID INTEGER PRIMARY KEY,TimeStamp INTEGER,LocalTime INTEGER," +
					"Type INTEGER,Latitude VARCHAR,Longitude VARCHAR);");
			database.setTransactionSuccessful();
			database.endTransaction();
			closeDB();
		}catch(Exception e)
		{
//			File file = new File(DB_PATH + DB_NAME);
//			if(file.exists())
//				file.delete();
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
			Log.e("record and time", Long.toString(csr.getLong(csr.getColumnIndex("NodeID"))) + " " + Long.toString(csr.getLong(csr.getColumnIndex("TimeStamp"))));
			Log.e("record mode", Integer.toString(csr.getInt(csr.getColumnIndex("Type"))));
		}while(csr.moveToNext());
		csr.close();
	}
	
	public void closeDB()
	{
		if(database.isOpen())
		{
			database.close();			
		}			
		database = null;
		Log.e("DATABASE CLOSE", "DATABASE CLOSE");
	}
	
	public void maintainence()
	{
		/*
		 *  First find number of rows
		 *  Allocate and array and store ids of the entries that needs to be deleted
		 *  Delete the entries one by one
		 */
		int entries = 0, counter;
		long ids[];
		database.beginTransaction();
		Cursor csr = database.rawQuery("SELECT * FROM " + TABLE + ";", null);
		Log.e("maintainence", "1/4");
		if(!csr.moveToFirst())
		{
			csr.close();
			database.setTransactionSuccessful();
			database.endTransaction();
			return;
		}
		do{
			entries++;
		}while(csr.moveToNext());
		ids = new long[entries];
		Log.e("maintainence", "half");
		for(counter = 0; counter < ids.length; counter++)
			ids[counter] = -1;
		csr.moveToFirst();
		counter = 0;
		do{			
			if(System.currentTimeMillis() - csr.getLong(csr.getColumnIndex("LocalTime")) >= 10000)
			{
				ids[counter++] = csr.getLong(csr.getColumnIndex("NodeID"));
			}
		}while(csr.moveToNext());
		Log.e("maintainence", "3/4");
		for(counter = 0; counter < ids.length; counter++)
		{
			if(ids[counter] == -1)
				break;
			deleteEntry(ids[counter]);
		}
		csr.close();
		database.setTransactionSuccessful();
		database.endTransaction();
		Log.e("maintainence", "finish");
	}
}
