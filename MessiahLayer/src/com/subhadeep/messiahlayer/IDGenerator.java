package com.subhadeep.messiahlayer;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class IDGenerator {

private WifiManager wifiManager;
	
	public IDGenerator(Context context)
	{
		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}
	
	public long generateID()
	{
		if(!wifiManager.isWifiEnabled()) 
		    // WIFI ALREADY ENABLED. GRAB THE MAC ADDRESS HERE		    
		    wifiManager.setWifiEnabled(true);
		// WIFI IS NOW ENABLED. GRAB THE MAC ADDRESS HERE
		WifiInfo info = wifiManager.getConnectionInfo();
		Log.e("MAC", info.getMacAddress());
		return Long.parseLong(info.getMacAddress().replace(":", ""), 16);
	}
}

