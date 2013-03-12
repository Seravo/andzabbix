package ru.sonic.zabbix.service;

import java.util.ArrayList;

import ru.sonic.zabbix.StartTabActivity;
import ru.sonic.zabbix.R.color;
import ru.sonic.zabbix.base.Trigger;
import ru.sonic.zabbix.base.ZabbixAPIException;
import ru.sonic.zabbix.base.ZabbixAPIHandler;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

public class ZBXCheckRepeetingService extends BroadcastReceiver {
	private static final int MSG_DATA_RETRIEVED = 0;
	private static final int MSG_ERROR = 1;
	public final static String TAG = "ZBXCheckRepeetingService";

	Context context;
	
	@Override
	public void onReceive(Context ctsx, Intent intent) {
	      //Log.v(this.getClass().getName(), "Timed alarm onReceive() started at time: " + new java.sql.Timestamp(System.currentTimeMillis()).toString());
	      this.context = ctsx;
	      
	      if (checkInternet()) {
	      new Thread(new Runnable() {
	          public void run() {
					try {
						Message msg=new Message();
						msg.obj = databse_gets();
						msg.arg1=MSG_DATA_RETRIEVED;
						handler.sendMessage(msg);
				} catch (Exception e) {
						Log.e(TAG, "run() Error: " + e);
						Message msg=new Message();
						msg.arg1=MSG_ERROR;
						msg.obj=e;
						handler.sendMessage(msg);
						return;
				}
	          }
	      }).start();
	      }	      
	}
	
	public boolean checkInternet() {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm.getActiveNetworkInfo() == null) {
			return false;
		} else {
			return true;}
	}
		 
	private Handler handler = new Handler() {
		@Override
	    public void handleMessage(Message msg) { 
			switch (msg.arg1) {
	 		case  MSG_ERROR:
	 			Log.e(TAG,"ERROR getting data: "+msg.obj);
	 			break;
	 		case MSG_DATA_RETRIEVED:
					Log.d(TAG,"Do check from service!"+msg.obj);
					if (msg.obj != null)
					parse_trigger_list((ArrayList<Trigger>) msg.obj);
	 			}
	        }
	};
		 
	public void notify(String info, String hostname, String age) {
			int NOTIFY_ID = Integer.parseInt(age);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			boolean service_alert_sound = prefs.getBoolean("service_alert_sound",false);
			boolean service_alert_led = prefs.getBoolean("service_alert_led",false);
			boolean service_alert_vibration = prefs.getBoolean("service_alert_vibration",false);
				NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				Intent startAppintent = new Intent(context, StartTabActivity.class);
				Notification notification = new Notification(android.R.drawable.ic_menu_info_details, info, System.currentTimeMillis());
				PendingIntent contentIntent = PendingIntent.getActivity(context, 0, startAppintent, 0);
				//String info = "You have "+triggers_count+" new triggers!";

				notification.setLatestEventInfo(context, hostname+ " ALERT!", info , contentIntent);
				if (service_alert_sound)
					notification.defaults |= Notification.DEFAULT_SOUND;
				if (service_alert_vibration)
					notification.defaults |= Notification.DEFAULT_VIBRATE;
				notification.ledARGB = color.orange;
				notification.ledOnMS = 300;
				notification.ledOffMS = 1000;
				if (service_alert_led)
					notification.flags |= Notification.FLAG_SHOW_LIGHTS;
					
				mNotificationManager.notify(NOTIFY_ID, notification);
			}
		
	protected void parse_trigger_list(ArrayList<Trigger> obj) {
		for (Trigger trigger: obj) {
			notify(trigger.getDescription(), trigger.getHost(), trigger.getLastchangeStamp());
		}
	}

	protected Object databse_gets() {
		ZabbixAPIHandler zbx = new ZabbixAPIHandler(context.getApplicationContext());
	  	try {
			return zbx.getActiveTriggersCountService(getInterval());
		} catch (ZabbixAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
		
		public Integer getInterval(){
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			
	        Integer interval = Integer.parseInt(prefs.getString("service_update_interval","30"));
	        return interval;
		}

		//@Override
		public IBinder onBind(Intent intent) {
			// TODO Auto-generated method stub
			return null;
		}
}