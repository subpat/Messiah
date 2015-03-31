


package com.subhadeep.Messiah;


import java.util.List;

import net.osmand.Location;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.routing.RoutingHelper;
import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.subhadeep.messiahlayer.Communicator;
import com.subhadeep.messiahlayer.DataBaseWriter;
import com.subhadeep.messiahlayer.Locator;
import com.subhadeep.messiahlayer.Tokenizer;

public class MessiahControllerService extends IntentService{
	
	private boolean deleteDB;
	private volatile OsmandSettings settings;
	private volatile RoutingHelper routingHelper;
	
	private Communicator com = null;
	
	private static final int MAXIMUM_NUMBER_OF_POINTS = 50; //arbitrary calculation with spaces remaining
	
	public MessiahControllerService() {
		super("MessiahController");			
		deleteDB = false;
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.e("MESSIAH MONITOR","INSIDE");
		routingHelper = ((OsmandApplication) getApplication()).getRoutingHelper();
		com = new Communicator(((OsmandApplication) getApplication()).getApplicationContext());
		com.start();
		while(true)
		{			
			settings = ((OsmandApplication) getApplication()).getSettings();
			if(settings != null && settings.MESSIAH_ON.get()) //making sure that the app has been properly loaded
			{	if(routingHelper.isRouteCalculated())
				{
					int start = 0;
					double dist = Double.MAX_VALUE;
					Locator loc = new Locator(this);
					loc.isLocationAvailable();
					List<Location> route = routingHelper.getCurrentRoute();
//					Log.e("UPV", "UPV" + Double.toString(temp.get(0).getLatitude()) + " " + Double.toString(temp.get(0).getLongitude()));
//					Log.e("DEST", "DEST" + Double.toString(temp.get(temp.size()-1).getLatitude()) + " " + Double.toString(temp.get(temp.size()-1).getLongitude()));
					
					//find where you are on the route
					for(int i = 0; i < route.size(); i++)
					{
						if(dist > loc.distance(route.get(i).getLatitude(), route.get(i).getLongitude()))
						{
							dist = loc.distance(route.get(i).getLatitude(), route.get(i).getLongitude());
							start = i;
						}
					}
					Tokenizer route_lat = new Tokenizer();
					Tokenizer route_lon = new Tokenizer();				
					for(int i = start; i < ((route.size() - start < MAXIMUM_NUMBER_OF_POINTS)? route.size() : start + MAXIMUM_NUMBER_OF_POINTS); i++)
					{
						route_lat.addToken(Double.toString(precisionControl(route.get(i).getLatitude())));
						route_lon.addToken(Double.toString(precisionControl(route.get(i).getLongitude())));
					}
//					Log.e("ROUTE LAT", "ROUTE LAT " + Integer.toString(route.size()) + " " + route_lat.getString());
					com.settingsUpdate(settings.SOS.get(), settings.IS_POLICE.get(), settings.MESSIAH_ON.get(), route_lat.getString(), route_lon.getString(), precisionControl(route.get(route.size()-1).getLatitude()), precisionControl(route.get(route.size()-1).getLongitude()));
					route_lat = route_lon = null;
				}
				else
				{
					com.settingsUpdate(settings.SOS.get(), settings.IS_POLICE.get(), settings.MESSIAH_ON.get(), "", "", 0, 0);
				}
//				Log.e("IS ROUTE CALCULATED", Boolean.toString(routingHelper.isRouteCalculated()) + " Location matrix " + Boolean.toString(routingHelper.getIntermediatePoints() != null));								
				com.resetWait();
				deleteDB = true;
			}
			else if(settings != null && !settings.MESSIAH_ON.get())
			{
				//the layer is being turned off
				com.settingsUpdate(settings.SOS.get(), settings.IS_POLICE.get(), settings.MESSIAH_ON.get(), "", "", 0,0);
				com.setWait();
				if(deleteDB)
				{
					DataBaseWriter dbr = new DataBaseWriter(this);				
					dbr.deleteAllAndClose();
					dbr = null;
					deleteDB = false;
				}
			}
			else
				;			
			SystemClock.sleep(25);
		}
	}
	
	public void onDestroy()
	{
		com.stopThread();
		com.closeSocket();
		com = null;
		if(deleteDB)
		{
			DataBaseWriter dbr = new DataBaseWriter(this);				
			dbr.deleteAllAndClose();
			dbr = null;
			deleteDB = false;
		}	
	}
	
	public double precisionControl(double d)
    {
        int temp = (int)d;
        d -= temp;
        return Double.parseDouble(String.format("%.7g%n", d)) + temp;
    }
	
}
