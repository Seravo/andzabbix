package ru.sonic.zabbix;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ru.sonic.zabbix.base.Trigger;
import ru.sonic.zabbix.base.ZabbixAPIException;
import ru.sonic.zabbix.base.ZabbixAPIHandler;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

public class ZabbixWidgetProvider extends AppWidgetProvider {
    private static final String LOG_TAG = "ZabbixWidgetProvider";
    public static final String URI_SCHEME = "zabbix_widget";
	private static final int MSG_DATA_RETRIEVED = 0;
	private static final int MSG_ERROR = 1;
	
	public static String ACTION_WIDGET_REFRESH = "android.appwidget.action.APPWIDGET_REFRESH";
	public static String ACTION_WIDGET_SETTINGS = "android.appwidget.action.APPWIDGET_SETTINGSCONF";
	public static String ACTION_WIDGET_TRIGGERSLIST = "android.appwidget.action.APPWIDGET_GOTRIGGERSLIST";
	
    boolean updating = false;
    public String servername = "Server";
    public static String ACTION_WIDGET_CONFIGURE = "ConfigureWidget";
    Intent intnt;
    Context ctx;
    static AppWidgetManager appWgtMng;

    @Override
    public void onEnabled(Context context) {
        // This is only called once, regardless of the number of widgets of this
        // type
        // We do not have any global initialization
        //Log.i(LOG_TAG, "onEnabled()");
        super.onEnabled(context);
        //updating = true;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        this.ctx = context;
        this.appWgtMng = appWidgetManager;
        	
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        Intent active = new Intent(context, ZabbixWidgetProvider.class);
        active.setAction(ACTION_WIDGET_REFRESH);
        active.putExtra("appWidgetId", appWidgetIds[0]);
        PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, appWidgetIds[0], active, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_button_refresh, actionPendingIntent);
        
        /*
        active = new Intent(context, ZabbixWidgetProvider.class);
        active.setAction(ACTION_WIDGET_SETTINGS);
        actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_button_settings, actionPendingIntent);
         */
        
        active = new Intent(context, ZabbixWidgetProvider.class);
        active.setAction(ACTION_WIDGET_TRIGGERSLIST);
        actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_button_triggerlist, actionPendingIntent);
		
        refreshThread(context,appWidgetIds[0]);
        
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }
    
    public void refreshThread(Context context, final int appWidgetId) {
   		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
   		NetworkInfo ni = cm.getActiveNetworkInfo();
   		if (ni!=null && ni.isAvailable() && ni.isConnected()) {

   		new Thread() {
		  	public void run() {
		  		try {
		    					ZabbixAPIHandler zbx = new ZabbixAPIHandler(ctx.getApplicationContext());
		    					Message msg=new Message();
		    					msg.arg1=MSG_DATA_RETRIEVED;
		    					msg.obj=zbx.getActiveTriggersCount(getCurrentServer(appWidgetId));
		    					msg.arg2=appWidgetId;
		    					//Log.d(LOG_TAG, "Retrived data id: "+msg.arg2+" obj: " + msg.obj);
		    					handler.sendMessage(msg);
		    				} catch (ZabbixAPIException e) {
		    					Message msg=new Message();
		    					msg.arg1=MSG_ERROR;
		    					msg.obj=e;
		    					Log.e(LOG_TAG, "Retrived error: " + msg.obj);
		    					handler.sendMessage(msg);
		    					return;
		    				}
		    			}
		    		}.start(); 
        	}
   	}
    
    public String getCurrentTime() {
		//SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MMMM, HH:mm"); 
    	SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
		Date now = new Date();
		return dateFormat.format(now);
    }
	
	public String getCurrentServer(int appWidgetId) {
		SharedPreferences config = ctx.getSharedPreferences(ZabbixWidgetControl.PREFS_NAME, 0);
		//Log.d(LOG_TAG, "Widget get current server name: " + config.getString(String.format(ZabbixWidgetControl.PREFS_WIDGET_SERVER, appWidgetId),"server"));
        //int updateRateSeconds = config.getInt(String.format(ZabbixWidgetControl.PREFS_UPDATE_RATE_FIELD_PATTERN, appWidgetId), -1);
        //Log.d(LOG_TAG, "Widget get current updateRateSeconds: " + updateRateSeconds);
		return config.getString(String.format(ZabbixWidgetControl.PREFS_WIDGET_SERVER, appWidgetId),"server");
	}

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        //Log.d(LOG_TAG, "onDelete()");

        for (int appWidgetId : appWidgetIds) {
            // stop alarm
            Intent widgetUpdate = new Intent();
            widgetUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            widgetUpdate.setData(Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/"), String.valueOf(appWidgetId)));
            PendingIntent newPending = PendingIntent.getBroadcast(context, 0, widgetUpdate, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarms.cancel(newPending);

            // remove preference
            //Log.d(LOG_TAG, "Removing preference for id " + appWidgetId);
            SharedPreferences config = context.getSharedPreferences(ZabbixWidgetControl.PREFS_NAME, 0);
            SharedPreferences.Editor configEditor = config.edit();

            configEditor.remove(String.format(ZabbixWidgetControl.PREFS_UPDATE_RATE_FIELD_PATTERN, appWidgetId));
            configEditor.remove(String.format(ZabbixWidgetControl.PREFS_WIDGET_SERVER, appWidgetId));
            configEditor.commit();
        }

        super.onDeleted(context, appWidgetIds);
    }

    // Fix SDK 1.5 Bug per note here:
    // http://developer.android.com/guide/topics/appwidgets/index.html#AppWidgetProvider
    // linking to this post:
    // http://groups.google.com/group/android-developers/msg/e405ca19df2170e2
    @Override
    public void onReceive(Context context, Intent intent) {
    	this.intnt = intent;
    	this.ctx = context;
        final String action = intent.getAction();
        //int actint = Integer.parseInt(action);
        Log.d(LOG_TAG, "OnReceive:Action: " + action);
        
        //switch (actint) {
        //case ACTION_APPWIDGET_DELETED:
        
        if (context != null && intent != null)
        if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
            final int appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                this.onDeleted(context, new int[] { appWidgetId });
            }
        //break;
        } else if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
        //case ACTION_APPWIDGET_UPDATE:
            if (!URI_SCHEME.equals(intent.getScheme())) {
                // if the scheme doesn't match, that means it wasn't from the
                // alarm
                // either it's the first time in (even before the configuration
                // is done) or after a reboot or update
                final int[] appWidgetIds = intent.getExtras().getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);

                for (int appWidgetId : appWidgetIds) {

                    // get the user settings for how long to schedule the update
                    // time for
                    SharedPreferences config = context.getSharedPreferences(ZabbixWidgetControl.PREFS_NAME, 0);
                    int updateRateSeconds = config.getInt(String.format(ZabbixWidgetControl.PREFS_UPDATE_RATE_FIELD_PATTERN, appWidgetId), -1);
                    if (updateRateSeconds != -1) {
                        //Log.i(LOG_TAG, "Starting recurring alarm for id " + appWidgetId);
                        Intent widgetUpdate = new Intent();
                        widgetUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                        widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId });

                        // make this pending intent unique by adding a scheme to
                        // it
                        widgetUpdate.setData(Uri.withAppendedPath(Uri.parse(ZabbixWidgetProvider.URI_SCHEME + "://widget/id/"), String.valueOf(appWidgetId)));
                        PendingIntent newPending = PendingIntent.getBroadcast(context, 0, widgetUpdate, PendingIntent.FLAG_UPDATE_CURRENT);

                        // schedule the updating
                        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        alarms.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), updateRateSeconds * 60000, newPending);
                    }
                }
            } 
            super.onReceive(context, intent);
        } else if (action.equals(ACTION_WIDGET_REFRESH)) {
            int appWidgetId = intent.getExtras().getInt("appWidgetId");
            this.appWgtMng = ru.sonic.zabbix.ZabbixWidgetProvider.appWgtMng;
            refreshThread(context,appWidgetId);
        /*
        } else if (intent.getAction().equals(ACTION_WIDGET_SETTINGS)) {
            Log.i("onReceive", ACTION_WIDGET_SETTINGS);
        */
        } else if (action.equals(ACTION_WIDGET_TRIGGERSLIST)) {
            Log.d("onReceive", ACTION_WIDGET_TRIGGERSLIST+"");
            Intent ActiveTriggers = new Intent(context,StartTabActivity.class);
            ActiveTriggers.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(ActiveTriggers);
            
        } else {
            super.onReceive(context, intent);
        }
        //break;
        //}
    }
    
    
	/**
	 * the retriever thread cannot modify the gui after it's done
	 * Therefore it justs sends the information (data or error) to the handler which again does the gui stuff
	 */
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				super.handleMessage(msg);
			switch (msg.arg1) {
			case  MSG_ERROR:
				//Log.d(LOG_TAG, "Handler MSG_ERROR: " + MSG_ERROR);
				break;
				
			case MSG_DATA_RETRIEVED:
				List<Trigger> listtr = (List<Trigger>) msg.obj;
				int active=0,inactive=0;
				for (Trigger tr: listtr) {
					 if (tr.getActive().contains("1")) { active++;}
					 if (tr.getActive().contains("0")) { inactive++;}
				}

        	    //Intent intent = new Intent(ctx, ZabbixWidgetControl.class);
        	    //intent.setAction(ACTION_WIDGET_CONFIGURE);
        	    //PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0, intent, 0);
        	    //remoteView.setOnClickPendingIntent(R.id.widgetClick, pendingIntent);
	            //remoteView = new RemoteViews(ctx.getPackageName(), R.layout.widget_layout);
				
				RemoteViews remoteView = new RemoteViews(ctx.getPackageName(), R.layout.widget_layout);
	            remoteView.setTextViewText(R.id.widgettextactive, ""+active);
	            remoteView.setTextViewText(R.id.widgettextinactive, ""+inactive);
	            remoteView.setTextViewText(R.id.widgettextdata, getCurrentTime());
	            remoteView.setTextViewText(R.id.widgettextserver, getCurrentServer(msg.arg2));
	            
	            appWgtMng.updateAppWidget(msg.arg2, remoteView);
				break;
				
			default:
				break;
			}
			} catch (Exception e) {
				
			}
		}
	};
}