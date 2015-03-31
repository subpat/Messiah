package com.subhadeep.Messiah;

import net.osmand.data.LatLon;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.views.OsmandMapLayer;
import net.osmand.plus.views.OsmandMapTileView;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.subhadeep.messiahlayer.CheckWifi;
import com.subhadeep.messiahlayer.DataBaseReader;
import com.subhadeep.messiahlayer.Tokenizer;

public class DrawMessiah extends OsmandMapLayer{
	
	private static OsmandMapTileView view; //TODO: test
	private OsmandSettings osm_settings;
	private OsmandApplication app;
	private MapActivity mactivity;
	
	private Drawable messiah_button;
	private Drawable messiah_button_stop;
	private Drawable police_button;
	private Drawable police_button_pressed;
	private Drawable sos_button;
	private Drawable sos_button_pressed;
	private Drawable police_car;
	
	private Paint paint_me;
	private Paint paint_auth;
	private Paint paint_auth_route;
	private Paint paint_sos;
	private Paint paint_sos_route;
	
	private CheckWifi wifi;
	
	private boolean wifi_usable;
	
	private long nodeID[];
	private String lat[];
	private String lon[];
	private int type[];
	
	private String routeLat[];
	private String routeLon[];
	private String destinationLat[];
	private String destinationLon[];
	
	private Drawable carsIcon[];
	private boolean carsLoaded = false;
	
	private DataBaseReader dbReader;
	
	public static final int TYPE_ME = 0;
	public static final int TYPE_AUTHORITY = 1;
	public static final int TYPE_SOS = 2;
	public static final int TYPE_MOB = 3;			//FUTURE WORK
	public static final int TYPE_MESSAGE = 4;		//FUTURE WORK
	public static final int TYPE_STOP = 5;
		
	public static BroadcastReceiver br = new BroadcastReceiver() {
		public void onReceive(android.content.Context context, android.content.Intent intent) {
			if(intent.getStringExtra("com.subhadeep.messiahlayer.screen_intent").contains("update_screen"))
			{
				Log.e("drawmessiah","update screen GUI");
				updateGUI();
			}
		};
	};
	
	
	public DrawMessiah(OsmandApplication app, MapActivity mactivity)
	{
		osm_settings = app.getSettings();
		this.app = app;
		this.mactivity = mactivity;
		wifi = new CheckWifi(app.getApplicationContext());
		//mactivity.registerReceiver(br, new IntentFilter("UpdateGUI"));
		try {
			app.getApplicationContext().unregisterReceiver(br);
		} catch (Exception e){}
		app.getApplicationContext().registerReceiver(br, new IntentFilter("MessiahUpdateGUI"));
	}
	
	public void getIconData(long nodeID[], String lat[], String lon[], int type[], String routeLat[], String routeLon[], String destinationLat[], String destinationLon[])
	{
		
		this.nodeID = nodeID;
		this.lat = lat;
		this.lon = lon;
		this.type = type;
		
		this.routeLat = routeLat;
		this.routeLon = routeLon;
		this.destinationLat = destinationLat;
		this.destinationLon = destinationLon;
		
		if(nodeID != null)
			carsIcon = new Drawable[this.nodeID.length];
		else
			carsIcon = null;
	}
	
	public void loadIconCars()
	{
		//type 0 is police/ambulance, 1 is need of help and 2 is a nearby car only visible to people suffering
		if(carsIcon == null)
		{
			carsLoaded = false;
			return;
		}
		for(int i = 0; i < carsIcon.length; i++)
		{
			if(type[i] == TYPE_AUTHORITY)
				carsIcon[i] = view.getResources().getDrawable(R.drawable.messiah_police_car);
			else if(type[i] == TYPE_SOS)
				carsIcon[i] = view.getResources().getDrawable(R.drawable.messiah_help_car);
			else if(type[i] == TYPE_MOB)
				carsIcon[i] = view.getResources().getDrawable(R.drawable.messiah_car_normal);
			else
				carsIcon[i] = null; //Its me
		}
		carsLoaded = true;
	}
	
	
	public void initLayer(OsmandMapTileView view)
	{
		paint_me = new Paint();
		paint_me.setColor(Color.BLUE);//view.getResources().getColor(R.color.nav_track_fluorescent));
		paint_me.setAlpha(128); //transparency
		paint_me.setAntiAlias(true);
		paint_me.setFilterBitmap(true);
		paint_me.setDither(true);
		paint_me.setStyle(Style.STROKE);
		paint_me.setStrokeWidth(14);
		paint_me.setStrokeCap(Cap.ROUND);
		paint_me.setStrokeJoin(Join.ROUND);
		
		paint_auth = new Paint();
		paint_auth.setColor(Color.RED);//view.getResources().getColor(R.color.nav_track_fluorescent));
		paint_auth.setAlpha(75); //transparency
		paint_auth.setAntiAlias(true);
		paint_auth.setFilterBitmap(true);
		paint_auth.setDither(true);
		paint_auth.setStyle(Style.STROKE);
		paint_auth.setStrokeWidth(14);
		paint_auth.setStrokeCap(Cap.ROUND);
		paint_auth.setStrokeJoin(Join.ROUND);
		
		paint_auth_route = new Paint();
		paint_auth_route.setColor(Color.RED);//view.getResources().getColor(R.color.nav_track_fluorescent));
		paint_auth_route.setAlpha(128); //transparency
		paint_auth_route.setAntiAlias(true);
		paint_auth_route.setFilterBitmap(true);
		paint_auth_route.setDither(true);
		paint_auth_route.setStyle(Style.STROKE);
		paint_auth_route.setPathEffect(new DashPathEffect(new float[] {15,15}, 0));
		paint_auth_route.setStrokeWidth(14);
		paint_auth_route.setStrokeCap(Cap.SQUARE);
		paint_auth_route.setStrokeJoin(Join.ROUND);
		
		paint_sos = new Paint();
		paint_sos.setColor(Color.BLACK);//view.getResources().getColor(R.color.nav_track_fluorescent));
		paint_sos.setAlpha(75); //transparency
		paint_sos.setAntiAlias(true);
		paint_sos.setFilterBitmap(true);
		paint_sos.setDither(true);
		paint_sos.setStyle(Style.STROKE);
		paint_sos.setStrokeWidth(14);
		paint_sos.setStrokeCap(Cap.ROUND);
		paint_sos.setStrokeJoin(Join.ROUND);
		
		paint_sos_route = new Paint();
		paint_sos_route.setColor(Color.BLACK);//view.getResources().getColor(R.color.nav_track_fluorescent));
		paint_sos_route.setAlpha(128); //transparency
		paint_sos_route.setAntiAlias(true);
		paint_sos_route.setFilterBitmap(true);
		paint_sos_route.setDither(true);
		paint_sos_route.setStyle(Style.STROKE);
		paint_sos_route.setPathEffect(new DashPathEffect(new float[] {15,15}, 0));
		paint_sos_route.setStrokeWidth(14);
		paint_sos_route.setStrokeCap(Cap.SQUARE);
		paint_sos_route.setStrokeJoin(Join.ROUND);
		
		this.view = view;
		messiah_button = view.getResources().getDrawable(R.drawable.messiah_icon_green);
		messiah_button_stop = view.getResources().getDrawable(R.drawable.messiah_icon_red);
		police_button = view.getResources().getDrawable(R.drawable.messiah_police);
		police_button_pressed = view.getResources().getDrawable(R.drawable.messiah_police_pressed);
		sos_button = view.getResources().getDrawable(R.drawable.messiah_sos);
		sos_button_pressed = view.getResources().getDrawable(R.drawable.messiah_sos_pressed);
		
		osm_settings.MESSIAH_REQUISITES.set(wifi.checkAvailability());
		if(!osm_settings.MESSIAH_REQUISITES.get())
		{
			app.showToastMessage("Wifi Unavailable! The Messiah Plugin needs Wifi to run!");
		}
		if(osm_settings.MESSIAH_REQUISITES.get() && !wifi.isConnected())
			wifi_usable = wifi.connect();
		else if(osm_settings.MESSIAH_REQUISITES.get() && wifi.isConnected())
			wifi_usable = true; //wifi has been connected before running the application
		

	}
	
	static public void updateGUI(){
		view.invalidate();		
	}
	
	public boolean drawInScreenPixels()
	{
		return true;
	}
	
	public void onDraw(Canvas canvas, RectF latlon, RectF tilesRect, DrawSettings settings)
	{
		Log.e("DRAW CYCLE", "DRAW CYCLE starts");
		Rect bounds = canvas.getClipBounds();
		
		if(!osm_settings.MESSIAH_ON.get())
		{
			messiah_button.setVisible(true,false);
			messiah_button_stop.setVisible(false, false);
			police_button.setVisible(false, false);
			police_button_pressed.setVisible(false, false);
			sos_button.setVisible(false, false);
			sos_button_pressed.setVisible(false, false);
			messiah_button.setBounds(bounds.right - messiah_button.getIntrinsicWidth() - 3 , bounds.top + (bounds.bottom - messiah_button.getIntrinsicHeight())/2, bounds.right - 3, bounds.top + (bounds.bottom + messiah_button.getIntrinsicHeight())/2);			
			messiah_button.draw(canvas);
		}
		else
		{
			//the plugin is on
			
			//solution is simply draw the path, then the cars  and ultimately then the buttons to have the buttons on top

			dbReader = new DataBaseReader(app.getApplicationContext());
			dbReader.readFromDB();
			getIconData(dbReader.getID(), dbReader.getLat(), dbReader.getLon(), dbReader.getType(), dbReader.getRouteLat(), dbReader.getRouteLon(), dbReader.getDestinationLat(), dbReader.getDestinationLon());
			dbReader.close();
			dbReader = null;
			//here draw the paths
			loadIconCars();
			if(carsLoaded)
			{
				//draw layer by layer
				for(int i = 0; i < carsIcon.length; i++)
				{	/*
							It has been decided that two paths will be drawn onscreen: one is from predefined start to end displayed using dotted lines
							Another is a small trail to the current path that is being followed by a normal filled line													
					*/
					if(routeLat == null)
						break;
					String route_lat_list[] = new Tokenizer(routeLat[i]).getTokens();
					//route does not exist for the self-node or if a node has no predefined path
					if(route_lat_list != null)
					{
						//there exists a route, hurrah!
						String route_lon_list[] = new Tokenizer(routeLon[i]).getTokens();
						Path path = new Path();
						path.moveTo(view.getRotatedMapXForPoint(Double.parseDouble(route_lat_list[0]), Double.parseDouble(route_lon_list[0])), view.getRotatedMapYForPoint(Double.parseDouble(route_lat_list[0]), Double.parseDouble(route_lon_list[0])));
						int limit = (route_lat_list.length < 3)? route_lat_list.length: route_lat_list.length - 1;
						boolean arrow = !((route_lat_list[route_lat_list.length - 1].compareTo(destinationLat[i]) == 0) && (route_lon_list[route_lon_list.length - 1].compareTo(destinationLon[i]) == 0));//(route_lat_list.length < 3)? false: true;
						for(int j = 1; j < limit; j++)				
							path.lineTo(view.getRotatedMapXForPoint(Double.parseDouble(route_lat_list[j]), Double.parseDouble(route_lon_list[j])), view.getRotatedMapYForPoint(Double.parseDouble(route_lat_list[j]), Double.parseDouble(route_lon_list[j])));
						if(type[i] == TYPE_AUTHORITY)
						{
							
							paint_auth_route.setStyle(Style.STROKE);
							paint_auth_route.setAlpha(128);
							canvas.drawPath(path, paint_auth_route);
//							draw the destination point if required
							if(!arrow)
							{	
								paint_auth_route.setAlpha(255);		
								paint_auth_route.setStyle(Style.FILL);
								canvas.drawCircle(view.getRotatedMapXForPoint(Double.parseDouble(destinationLat[i]), Double.parseDouble(destinationLon[i])), view.getRotatedMapYForPoint(Double.parseDouble(destinationLat[i]), Double.parseDouble(destinationLon[i])), 10, paint_auth_route);
							}
						}
						else if(type[i] == TYPE_SOS)
						{
							paint_sos_route.setStyle(Style.STROKE);
							paint_sos_route.setAlpha(128);
							canvas.drawPath(path, paint_sos_route);
							if(!arrow)
							{
								paint_sos_route.setAlpha(255);
								paint_sos_route.setStyle(Style.FILL);
								canvas.drawCircle(view.getRotatedMapXForPoint(Double.parseDouble(destinationLat[i]), Double.parseDouble(destinationLon[i])), view.getRotatedMapYForPoint(Double.parseDouble(destinationLat[i]), Double.parseDouble(destinationLon[i])), 10, paint_sos_route);
							}
						}
						else;
						
						//now draw the arrow head in place of the temporary destination
						Paint temp = new Paint();
						if(type[i] == TYPE_AUTHORITY)
						{
							temp.setColor(Color.RED);//view.getResources().getColor(R.color.nav_track_fluorescent));
						}
						else if(type[i] == TYPE_SOS)
						{
							temp.setColor(Color.BLACK);
						}
						else;
						
						if(arrow)
						{
							temp.setAlpha(180); //transparency
							temp.setAntiAlias(true);
							temp.setFilterBitmap(true);
							temp.setDither(true);
							temp.setStyle(Style.FILL);
							temp.setPathEffect(new DashPathEffect(new float[] {15,15}, 0));
							temp.setStrokeWidth(16);
							temp.setStrokeCap(Cap.SQUARE);
							temp.setStrokeJoin(Join.ROUND);
							boolean specialCase = false;
							
							/****************************************************************************************************************************************************************
							 *																																								* 
							 * 														Let m1 and m2 be the slopes of line-1 and line-2 respectively.											*
							 * 														Since the lines intersect at 90 degrees, m1 * m2 = -1													*
							 * 																																								*
							 * 														Now, m1 = (y2-y1)/(x2-x1) using the points p1 and p2.													*
							 * 														Therefore, m2 = -1/m1 = (x1-x2)/(y2-y1)																	*
							 * 							*																																	*
							 * 							|	*						Equation of line 2 is : (y-y1) = m2(x-x1)																*
							 * 							|		*											 y = m2x + y1 - m2x1 															*
							 * 				  p1(x1,y1) |			*																														*
							 * 	Line-1	----------------*--------------* p2(x2,y2)	Now, given a know distance d,																			*
							 * 							|			*				d² = (x1-x3)² + (y1-y2)²																				*
							 * 						  d |		*					y3 = y1 - (d² - (x1-x3)²)^0.5																			*
							 * 					Line-2	|	*																																*
							 * 							* p3(x3,y3)					Putting y = y3 and x = x3 in the equation of line 2, we get												*
							 * 														y3 = m2x3 + y1 - m2x1																					*
							 * 														y1 - (d² - (x1-x3)²)^0.5 = m2x3 + y1 - m2x1		since y3 = y1 - (d² - (x1-x3)²)^0.5						*
							 * 														x3 = x1 - (d²/(m2² + 1))^0.5																			*
							 * 																																								*
							 * 														Hence the required formulas are:																		*
							 * 															slope of line-2: 			m2 = (x1-x2)/(y2-y1)													*
							 * 															the unknown point p3:		x3 = x1 - (d²/(m2² + 1))^0.5 and y3 = y1 - (d² - (x1-x3)²)^0.5			*
							 * 																																								*
							 ****************************************************************************************************************************************************************/
							int distance = 18;
							int x1 = view.getRotatedMapXForPoint(Double.parseDouble(route_lat_list[route_lat_list.length - 2]), Double.parseDouble(route_lon_list[route_lon_list.length - 2]));
							int y1 = view.getRotatedMapYForPoint(Double.parseDouble(route_lat_list[route_lat_list.length - 2]), Double.parseDouble(route_lon_list[route_lon_list.length - 2]));
							int x2 = view.getRotatedMapXForPoint(Double.parseDouble(route_lat_list[route_lat_list.length - 1]), Double.parseDouble(route_lon_list[route_lon_list.length - 1]));
							int y2 = view.getRotatedMapYForPoint(Double.parseDouble(route_lat_list[route_lat_list.length - 1]), Double.parseDouble(route_lon_list[route_lon_list.length - 1]));
							int x3,y3,x4,y4;
							int m2 = 0;
							try{
								m2 = (x1-x2)/(y2-y1);
							}
							catch(ArithmeticException e)
							{
								//This will occur when you try to divide by 0, meaning that the slope of line-2 is infinite.
								//But, this is a valid case.
								specialCase = true;
							}
							if(specialCase)
							{
								x4 = x3 = x1;
								y3 = y1 + distance;
								y4 = y1 - distance;
							}
							else
							{
								x3 = (int) (x1 - Math.sqrt(distance*distance/(m2*m2 + 1)));
								y3 = (int) (y1 - Math.sqrt(distance*distance - (x1-x3)*(x1-x3)));
								x4 = (int) (x1 + Math.sqrt(distance*distance/(m2*m2 + 1)));
								y4 = (int) (y1 + Math.sqrt(distance*distance - (x1-x3)*(x1-x3)));								
							}
							Path triangle = new Path();
//							triangle.moveTo(view.getRotatedMapXForPoint(Double.parseDouble(route_lat_list[route_lat_list.length - 3]), Double.parseDouble(route_lon_list[route_lon_list.length - 3])), view.getRotatedMapYForPoint(Double.parseDouble(route_lat_list[route_lat_list.length - 3]), Double.parseDouble(route_lon_list[route_lon_list.length - 3])));
//							triangle.lineTo(x1, y1);
//							canvas.drawPath(triangle, temp);
//							temp.setStrokeWidth(20);
//							temp.setStyle(Style.FILL);
//							triangle = new Path();							
							triangle.moveTo(x1, y1);
							triangle.lineTo(x3, y3);
							triangle.lineTo(x2, y2);
							triangle.lineTo(x4, y4);
							triangle.lineTo(x1, y1);
							triangle.close();
							canvas.drawPath(triangle, temp);
						}
					}
				}
				//now the trail path and icon
				for(int i = 0; i < carsIcon.length; i++)
				{						
					String lat_list[] = new Tokenizer(lat[i]).getTokens();
					String lon_list[] = new Tokenizer(lon[i]).getTokens();
					Path path = new Path();	
					path.moveTo(view.getRotatedMapXForPoint(Double.parseDouble(lat_list[0]), Double.parseDouble(lon_list[0])), view.getRotatedMapYForPoint(Double.parseDouble(lat_list[0]), Double.parseDouble(lon_list[0])));
					for(int j = 1; j < lat_list.length; j++)				
						path.lineTo(view.getRotatedMapXForPoint(Double.parseDouble(lat_list[j]), Double.parseDouble(lon_list[j])), view.getRotatedMapYForPoint(Double.parseDouble(lat_list[j]), Double.parseDouble(lon_list[j])));
					if(type[i] == TYPE_ME)
						canvas.drawPath(path, paint_me);
					else if(type[i] == TYPE_AUTHORITY)
						canvas.drawPath(path, paint_auth);
					else if(type[i] == TYPE_SOS)
						canvas.drawPath(path, paint_sos);
					else;
					
					if(carsIcon[i] == null) //self icon set as null so icon is not loaded but path is drawn
						continue;
					LatLon point = new LatLon(Double.parseDouble(lat_list[lat_list.length-1]), Double.parseDouble(lon_list[lon_list.length-1]));
					if(view.isPointOnTheRotatedMap(point.getLatitude(), point.getLongitude()))
					{
						int X = view.getRotatedMapXForPoint(point.getLatitude(), point.getLongitude());//view.getMapXForPoint(point.getLongitude());
						int Y = view.getRotatedMapYForPoint(point.getLatitude(), point.getLongitude());//view.getMapYForPoint(point.getLatitude());
						//	canvas.rotate(-view.getRotate(), locationX, locationY);
						carsIcon[i].setVisible(true, false);
						carsIcon[i].setBounds(X - carsIcon[i].getIntrinsicWidth()/2, Y - carsIcon[i].getIntrinsicHeight()/2, X + carsIcon[i].getIntrinsicWidth()/2, Y + carsIcon[i].getIntrinsicHeight()/2);
						carsIcon[i].draw(canvas);
					}
				}
			}
			//the the buttons
			messiah_button.setVisible(false,false);
			messiah_button_stop.setVisible(true,false);
			messiah_button_stop.setBounds(bounds.right - messiah_button_stop.getIntrinsicWidth() - 3, bounds.bottom/4 - messiah_button_stop.getIntrinsicHeight()/2, bounds.right - 3, bounds.bottom/4 + messiah_button_stop.getIntrinsicHeight()/2);
			messiah_button_stop.draw(canvas);
			if(osm_settings.IS_POLICE.get()) // you are the law enforcer
			{
				police_button.setVisible(false,false);
				police_button_pressed.setVisible(true, false);
				police_button_pressed.setBounds(bounds.right - messiah_button_stop.getIntrinsicWidth() + (messiah_button_stop.getIntrinsicWidth() - police_button_pressed.getIntrinsicWidth())/2, bounds.bottom/2 - police_button_pressed.getIntrinsicHeight()/2, bounds.right - (messiah_button_stop.getIntrinsicWidth() - police_button_pressed.getIntrinsicWidth())/2, bounds.bottom/2 + police_button_pressed.getIntrinsicHeight()/2);
				police_button_pressed.draw(canvas);
			}
			else
			{
				police_button.setVisible(true,false);
				police_button_pressed.setVisible(false, false);
				police_button.setBounds(bounds.right - messiah_button_stop.getIntrinsicWidth() + (messiah_button_stop.getIntrinsicWidth() - police_button.getIntrinsicWidth())/2, bounds.bottom/2 - police_button.getIntrinsicHeight()/2, bounds.right - (messiah_button_stop.getIntrinsicWidth() - police_button.getIntrinsicWidth())/2, bounds.bottom/2 + police_button.getIntrinsicHeight()/2);
				police_button.draw(canvas);
			}
			if(osm_settings.SOS.get()) //you need help
			{
				sos_button.setVisible(false, false);
				sos_button_pressed.setVisible(true, false);
				sos_button_pressed.setBounds(bounds.right - messiah_button_stop.getIntrinsicWidth() + (messiah_button_stop.getIntrinsicWidth() - sos_button_pressed.getIntrinsicWidth())/2, 3 * bounds.bottom/4 - sos_button_pressed.getIntrinsicHeight()/2, bounds.right - (messiah_button_stop.getIntrinsicWidth() - sos_button_pressed.getIntrinsicWidth())/2, 3 * bounds.bottom/4 + sos_button_pressed.getIntrinsicHeight()/2);
				sos_button_pressed.draw(canvas);
			}
			else
			{
				sos_button.setVisible(true, false);
				sos_button_pressed.setVisible(false, false);
				sos_button.setBounds(bounds.right - messiah_button_stop.getIntrinsicWidth() + (messiah_button_stop.getIntrinsicWidth() - sos_button.getIntrinsicWidth())/2, 3 * bounds.bottom/4 - sos_button.getIntrinsicHeight()/2, bounds.right - (messiah_button_stop.getIntrinsicWidth() - sos_button.getIntrinsicWidth())/2, 3 * bounds.bottom/4 + sos_button.getIntrinsicHeight()/2);
				sos_button.draw(canvas);
			}
			//draw the cars from the recent available information after deleting the previously drawn cars
			if(carsLoaded) //this is with the previously available information
			{
				for(int i = 0; i < carsIcon.length; i++)
				{
					if(carsIcon[i] == null) //self icon set as null
						continue;
					carsIcon[i].setVisible(false, false);
				}
			}
			
		}
		Log.e("DRAW CYCLE", "DRAW CYCLE ends");
	}
	
	public void destroyLayer()
	{
		//mactivity.unregisterReceiver(br);
		app.getApplicationContext().unregisterReceiver(br);
	}
	
	public boolean onSingleTap(PointF point)
	{	//the buttons will not respond if wifi is not available
		if(!osm_settings.MESSIAH_REQUISITES.get())
		{
			app.showShortToastMessage("Wifi Unavailable! The Messiah Plugin needs Wifi to run!");
			return false;
		}
		else if(osm_settings.MESSIAH_REQUISITES.get() && !wifi.isConnected())
		{
			if(!wifi.connect())
			{
				app.showToastMessage("Please connect wifi. The Messiah Plugin needs Wifi to run!");
				return false;
			}
		}
		//don't put this under else as the tables may turn in the previous else-if
			if(!osm_settings.MESSIAH_ON.get())
			{
				//the layer was off and now is being turned on
				Rect bounds = messiah_button.getBounds();
				if(bounds.contains((int)point.x, (int)point.y))
				{
					osm_settings.MESSIAH_ON.set(true);
					view.refreshMap(false);
					app.showToastMessage("Messiah Plugin ON! NOTE: Please turn off Messiah Layer when not required!");
					return true;
				}			
			}
			else
			{
				Rect bounds_messiah_stop, bounds_police, bounds_sos;
				bounds_messiah_stop = messiah_button_stop.getBounds();
				bounds_police = (osm_settings.IS_POLICE.get()) ? police_button_pressed.getBounds(): police_button.getBounds();
				bounds_sos = (osm_settings.SOS.get()) ? sos_button_pressed.getBounds(): sos_button.getBounds();
				if(bounds_messiah_stop.contains((int)point.x, (int)point.y))
				{
					//	all protocols has to stop
					osm_settings.MESSIAH_ON.set(false);
					osm_settings.IS_POLICE.set(false);
					osm_settings.SOS.set(false);
					view.refreshMap(false);
					app.showShortToastMessage("Messiah Plugin OFF!");
					return true;
				}
				else if(bounds_police.contains((int)point.x, (int)point.y))				
				{
					osm_settings.SOS.set(false); //sos and paramedic modes are mutually exclusive
					osm_settings.IS_POLICE.set(osm_settings.IS_POLICE.get()?false:true);
					view.refreshMap(false);
					app.showShortToastMessage(osm_settings.IS_POLICE.get()? "Messiah: Police/Paramedic Mode Activated!": "Messiah: Police/Paramedic Mode Deactivated!");
					return true;
				}
				else if(bounds_sos.contains((int)point.x, (int)point.y))
				{
					osm_settings.IS_POLICE.set(false); // even if you are the law-enforcer, you will be off duty if you become a victim
					osm_settings.SOS.set(osm_settings.SOS.get()?false:true);
					view.refreshMap(false);
					app.showShortToastMessage(osm_settings.SOS.get()? "Messiah: SOS Mode Activated!": "Messiah: SOS Mode Deactivated!");
					return true;
				}
				else
				{
					// MAY have clicked on one of the car icons
					if(carsIcon != null)
					{
						for(int i = 0; i < carsIcon.length; i++)
						{
							if(carsIcon[i] == null) //null for self icon
								continue;
							if(carsIcon[i].getBounds().contains((int)point.x, (int)point.y))
							{
								//future work : do something like communicate with that car
								return true;
							}
						}
					}
					return false;
				}
			}
		
	return false;	
	}
	
}