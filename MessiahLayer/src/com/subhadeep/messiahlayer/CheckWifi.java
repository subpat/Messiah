package com.subhadeep.messiahlayer;

import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

public class CheckWifi{
	private ConnectivityManager cManager;
	private volatile NetworkInfo wifi;	
	private WifiManager wifiManager;
	private Context ctx;
	
	public CheckWifi(Context ctx)
	{
		this.ctx = ctx;
		cManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		wifi = cManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	}
	
	public boolean checkAvailability()
	{	//checks if wifi hardware is present
		wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		if(wifiManager == null)
		{
			return false;
		}
		return true;
	}
	
	public boolean isConnected()
	{
		return wifi.isConnected();
	}
	
	public boolean connect()
	{ //this function tries to connect to wifi and returns true is connection successful or else returns false
		if(!wifiManager.isWifiEnabled())
			wifiManager.setWifiEnabled(true);
		wifiManager.startScan();
		
		List<ScanResult> scan_result = wifiManager.getScanResults();
		
		//sort results
 		for (int pass=1; pass < scan_result.size(); pass++) {  // count how many times
 	        // This next loop becomes shorter and shorter
 	        for (int i=0; i < scan_result.size() - pass; i++) {
 	            if (scan_result.get(i).level < scan_result.get(i+1).level) { 
 	                // exchange elements
 	                ScanResult temp = scan_result.get(i);  scan_result.set(i,scan_result.get(i+1)); scan_result.set(i+1,temp);
 	            }
 	        }
 	    }
 		
 		List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
 		for(ScanResult i: scan_result)
 		{
 			for(WifiConfiguration j : list)
 			{
 				if(i.SSID.compareTo(j.SSID) == 0)
 				{//not very clever
 					wifiManager.enableNetwork(j.networkId, true);
 					if(wifiManager.reconnect())
					return true;
 				}
 			}
 		}
//		while(!wifi.isConnected())
//			;
 		return false;
	}
}
	
	
