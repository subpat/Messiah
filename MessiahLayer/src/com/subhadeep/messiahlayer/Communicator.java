/*
 * || NodeID | TimeStamp | LocalTime | Type | Latitude | Longitude | Route_Lat 07-01-14 | Route_Lon 07-01-14 | Destination latitude 07-01-14 | Destination longitude 07-01-14 ||
 * 
 * NodeId is the IMEI number of the phone
 * TimeStamp is the time of the sender when the message was created
 * 		NodeId+TimeStamp together makes the messageID
 * LocalTime is the time of the receiver when the message was delivered
 * Type signifies the mode of operation : (0)Cop/Ambulance (1)SOS (3)Service Stopped (2)On Demand (this feature support not yet added)
 * Latitude is the last known latitude of the sender
 * Longitude is the last known longitude of the sender
 * 
 */



package com.subhadeep.messiahlayer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Communicator extends Thread{

	private volatile boolean isVictim;
	private volatile boolean isCop;
	private volatile boolean isLayerOn;
	
	private boolean needsStopMessage;
	
	private volatile boolean stop;
	private volatile boolean wait;
	
	
	private String route_lat;			// Added on 07-01-14
	private String route_lon;
	private double destination_lat;						// Added on 07-01-14
	private double destination_lon;
	
	
	public static final int PORT = 1234;
	public static final int MAX_PACKET_SIZE = 2000; //Adjust the size later 07-01-14
	public static final int TIME_OUT = 250;
	
	public static final int TYPE_ME = 0;
	public static final int TYPE_AUTHORITY = 1;
	public static final int TYPE_SOS = 2;
	public static final int TYPE_MOB = 3;			//FUTURE WORK
	public static final int TYPE_MESSAGE = 4;		//FUTURE WORK
	public static final int TYPE_STOP = 5;
	
	private DatagramSocket socket;	
	
	private DataBaseWriter dbwriter;
	
	private Context context;
	
	private long id;
	
	private final double THRESHOLD_DISTANCE = 1.0; //kilometers 
	
	public Communicator(Context context)
	{
		stop = true;
		this.context = context;
		this.wait = true;
		this.needsStopMessage = false;
		IDGenerator id_gen = new IDGenerator(context);
		id = id_gen.generateID();
		try{
			if(socket == null)
			{
				socket = new DatagramSocket(PORT);
			}
			else if(socket.isClosed())
				socket.bind(new InetSocketAddress(PORT));
			else ;
			//socket = new DatagramSocket(PORT);
			socket.setSoTimeout(TIME_OUT);
			Log.e("socket","created");
		}
		catch(Exception e)
		{
			Log.e("socket", e.toString());
		}
	}
	
	public void settingsUpdate(boolean isVictim, boolean isCop, boolean isLayerOn, String route_lat, String route_lon, double destination_lat, double destination_lon)
	{
		this.isVictim = isVictim;
		this.isCop = isCop;
		this.isLayerOn = isLayerOn;
		this.route_lat = route_lat;
		this.route_lon = route_lon;
		this.destination_lat = destination_lat;
		this.destination_lon = destination_lon;
	}
	
	public void stopThread()
	{
		//broadcast stop message and stop working
		stop = true;
	}
	
	public boolean isRunning()
	{
		return !stop;
	}
	
	public void setWait()
	{
		wait = true;
	}
	
	public void resetWait()
	{
		wait = false;
	}
	
	public void run()
	{
		Log.e("Messiah Communicator","RUNNING");
		stop = false;
		//String myip = getIP();
		long time;		
		time = System.currentTimeMillis() - 3500;
		while(!stop || needsStopMessage)
		{
			DatagramPacket packet = null;
			Locator locator = new Locator(context);
			try{
				while(wait)			
				{
					Log.e("communicator wait", "waiting");
					Thread.sleep(500);
				}
			}catch(Exception e)
			{
				Log.e("communicator sleep", e.toString());
			}			
			try
			{ //receiving and forwarding			
				packet = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);
				socket.receive(packet);
				if(packet.getData().length > 0){
					Log.e("com Received obj", new String(packet.getData()));
					ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(packet.getData()));
					Object object = ois.readObject();					
					if(object instanceof Message)
					{
						Message message = (Message) object;						 
						
						if(message.getNodeID() != id)
						{
							Log.e("COM MSG", "DIFF MSG");
							Log.e("com msg Received", Long.toString(message.getNodeID()) + " " + Long.toString(message.getTimeStamp()) + " " + Integer.toString(message.getType()));
							if(locator.distance(message.getLatitude(), message.getLongitude()) <= THRESHOLD_DISTANCE)
							{
								dbwriter = new DataBaseWriter(context);
								if(dbwriter.addEntry(message.getNodeID(), message.getTimeStamp(), System.currentTimeMillis(), message.getType(), message.getLatitude(), message.getLongitude(), message.getRouteLat(), message.getRouteLon(), Double.toString(message.getDestinationLat()), Double.toString(message.getDestinationLon())))
								{
									//if addentry successful means this is a new data
									packet = new DatagramPacket(packet.getData(), packet.getData().length, InetAddress.getByName("255.255.255.255"), PORT);					
									socket.send(packet);
									dbwriter.print();
								}
								dbwriter.closeDB();
								dbwriter = null;
								
							}
							else
							{								
								//distance was more so delete and drop
								dbwriter = new DataBaseWriter(context);
								dbwriter.deleteEntry(message.getNodeID());
								dbwriter.closeDB();
								dbwriter = null;
							}
							updateGUI();
						}
						Log.e("COM MSG", "SELF MSG");
					}
					
				}
			}
			catch(Exception ex)
			{
//				Log.e("Exception receiving and forwarding: ", ex.toString());
			}
			//sending
			try{
				if(!isLayerOn || (isLayerOn && !isVictim && !isCop))
				{
					if(needsStopMessage == true)
					{
						//first delete from database then try to forward
						dbwriter = new DataBaseWriter(context);
						dbwriter.deleteEntry(id);
						dbwriter.closeDB();
						dbwriter = null;
						
						Log.e("stop message communicator", "trying to send stop message");
						//for stop message no need to send the route or the final destination if any
						Message message = new Message(id, System.currentTimeMillis(), 0, TYPE_STOP,  locator.getLatitude(), locator.getLongitude(), "", "", 0, 0);						
						needsStopMessage = false;
						Log.e("com sending", "has message to send");				
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						ObjectOutputStream oos = new ObjectOutputStream(baos);
						oos.writeObject(message);
						
						Log.e("com message", Long.toString(message.getNodeID()));
						packet = new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, InetAddress.getByName("255.255.255.255"), PORT);
						Log.e("com packet", "created");
						socket.send(packet);
						Log.e("sent data", new String(baos.toByteArray()));
												
					}
				}
				if(System.currentTimeMillis() - time >= 2000)
				{
					//check if you have something to send and then forward
					//Log.e("com sending", "entered");
					//time to clear the database
					dbwriter = new DataBaseWriter(context);
					if(dbwriter == null)
						Log.e("object", "not existing");
					dbwriter.maintainence();
					//trying to simply save your location, and its useless to save one's own route information
					dbwriter.addEntry(id, System.currentTimeMillis(), System.currentTimeMillis(), TYPE_ME, locator.getLatitude(), locator.getLongitude(), "", "", "", "");
					Log.e("communicator", "printing database");
					dbwriter.print();
					dbwriter.closeDB();
					dbwriter = null;
					updateGUI();
					Message message = null;					
					if(isLayerOn)
					{
						if(isVictim)
						{
							Log.e("com sending", "victim");
							Log.e("com id", Long.toString(id));
							Log.e("com lat", Double.toString(locator.getLatitude()));
							/* not using System.currentTimeMillis as it is not giving the proper result */
							message = new Message(id, System.currentTimeMillis(), 0, TYPE_SOS, locator.getLatitude(), locator.getLongitude(), route_lat, route_lon, destination_lat, destination_lon);
							Log.e("THE PATH CONTAINS", "PATH HAS " + Boolean.toString(route_lat.contains(Double.toString(locator.getLatitude())) && route_lon.contains(Double.toString(locator.getLongitude()))));
							Log.e("DESTINATION LAT", "DESTINATION LAT" + destination_lat); //
							needsStopMessage = true;
						}
						else if(isCop)
						{
							Log.e("com sending", "cop");
							Log.e("com id", Long.toString(id));
							Log.e("com lat", Double.toString(locator.getLatitude()));
							message = new Message(id, System.currentTimeMillis(), 0, TYPE_AUTHORITY, locator.getLatitude(), locator.getLongitude(), route_lat, route_lon, destination_lat, destination_lon);
							needsStopMessage = true;
						}
						else;
						//add support for on demand mode						
					}					
					if(message != null)
					{	
						//first make changes to your own database then try to send
//						dbwriter = new DataBaseWriter(context);
//						dbwriter.addEntry(message.getNodeID(), message.getTimeStamp(), System.currentTimeMillis(), TYPE_ME, message.getLatitude(), message.getLongitude());
//						dbwriter.closeDB();
//						dbwriter = null;
//						
						Log.e("com sending", "has message to send");				
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						ObjectOutputStream oos = new ObjectOutputStream(baos);
						oos.writeObject(message);
						
						Log.e("com message", Long.toString(message.getNodeID()));
						packet = new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, InetAddress.getByName("255.255.255.255"), PORT);
						Log.e("com packet", "created");
						socket.send(packet);
						Log.e("sent data "+Integer.toString(baos.toByteArray().length), new String(baos.toByteArray()));
												
					}
					time = System.currentTimeMillis();
				}
			}catch(Exception e)
			{
				Log.e("COMMUNICATOR  sending and clearing",e.toString());
			}
			//trying to simply save your location
//			dbwriter = new DataBaseWriter(context);
//			dbwriter.addEntry(id, System.currentTimeMillis(), System.currentTimeMillis(), TYPE_ME, locator.getLatitude(), locator.getLongitude());
//			dbwriter.closeDB();
//			dbwriter = null;
		}		
	}
	
	public void closeSocket()
	{
		socket.close();
		socket = null;
	}
	
	@SuppressLint("NewApi")
	public String getIP()
	{
		String ip = null;
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface iface = interfaces.nextElement();
				// filters out 127.0.0.1 and inactive interfaces
				if (iface.isLoopback() || !iface.isUp())
					continue;

				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while(addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					ip = addr.getHostAddress();
					Log.e("#IP#","|"+ip+"|");
				}
			}
		} catch (SocketException e) {
			;
		}
		return ip;
	}
	
	private void updateGUI() {
		Intent it = new Intent("MessiahUpdateGUI");
		it.putExtra("com.subhadeep.messiahlayer.screen_intent", "update_screen");
		Log.e("Call UGUI", "Call");
		context.sendBroadcast(it);
	}

}
