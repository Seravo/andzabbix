package ru.sonic.zabbix.service;

import ru.sonic.zabbix.service.ZBXCheckRepeetingService;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

public class ZBXCheckService extends Service {
	public BroadcastReceiver mConnReceiver;
	public int INTERVAL = 60000 * 30; // every 30 min
	public int FIRST_RUN = 60000 * 10; // 10 min
	int REQUEST_CODE = 11223344;

	  AlarmManager alarmManager;

	  @Override
	  public void onCreate() {
	    super.onCreate();
	    Log.d("service","onCreate");
	    if (true)
	    	INTERVAL = getInterval();
	    else {
	    	INTERVAL = 60000;
	    	FIRST_RUN = 60000;
	    }
	    startService();
	  }
 
	  @Override
	  public IBinder onBind(Intent intent) {
	    //Log.v(this.getClass().getName(), "onBind(..)");
	    return null;
	  }

	  @Override
	  public void onDestroy() {
	    if (alarmManager != null) {
	      Intent intent = new Intent(this, ZBXCheckRepeetingService.class);
	      alarmManager.cancel(PendingIntent.getBroadcast(this, REQUEST_CODE, intent, 0));
	    }
	  }

	  private void startService() {
		  Log.d("Service","Starting service");
	    Intent intent = new Intent(this, ZBXCheckRepeetingService.class);
	    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, intent, 0);

	    alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
	    alarmManager.setRepeating(
	        AlarmManager.ELAPSED_REALTIME_WAKEUP,
	        SystemClock.elapsedRealtime() + FIRST_RUN,
	        INTERVAL,
	        pendingIntent);
	  }
	  
	public Integer getInterval(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
        Integer interval = Integer.parseInt(prefs.getString("service_update_interval","30"));
        return interval * 60000;
	}
}
