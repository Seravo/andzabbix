package ru.sonic.zabbix;

import java.util.List;

import ru.sonic.zabbix.base.DBAdapter;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class ZabbixWidgetControl extends Activity {
    public static final String PREFS_NAME = "ZabbixWidgetPrefs";
    public static final String PREFS_WIDGET_SERVER = "ServerNme-%d";
    public static final String PREFS_UPDATE_RATE_FIELD_PATTERN = "UpdateRate-%d";
//    private static final int PREFS_UPDATE_RATE_DEFAULT = 30;
    private static final String LOG_TAG = "ZabbixWidgetControl";
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "setContentView");
        setContentView(R.layout.widget_config);
        // get any data we were launched with
        Intent launchIntent = getIntent();
        Bundle extras = launchIntent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            Intent cancelResultValue = new Intent();
            cancelResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_CANCELED, cancelResultValue);
        } else {
            // only launch if it's for configuration
            // Note: when you launch for debugging, this does prevent this
            // activity from running. We could also turn off the intent
            // filtering for main activity.
            // But, to debug this activity, we can also just comment the
            // following line out.
            finish();
        }

        final Spinner servers = (Spinner)findViewById(R.id.server_spinner);
        List<String> serversnames = getServersList();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, serversnames); 
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        servers.setAdapter(adapter);
        
        final Spinner updateRateEntry = (Spinner)findViewById(R.id.update_rate_entry_spinner);
		
        Button saveButton = (Button)findViewById(R.id.buttonStart);
        saveButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //int updateRateSeconds = Integer.parseInt(updateRateEntry.getSelectedItem().toString());
                int updateRateSeconds = Integer.parseInt(getResources().getStringArray(R.array.UpdateIntervals)[updateRateEntry.getSelectedItemPosition()]);
                String serverName = servers.getSelectedItem().toString();

                SharedPreferences config = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor configEditor = config.edit();
                configEditor.putInt(String.format(PREFS_UPDATE_RATE_FIELD_PATTERN, appWidgetId), updateRateSeconds);
                configEditor.putString(String.format(PREFS_WIDGET_SERVER, appWidgetId), serverName);
                configEditor.commit();

                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {

                    // tell the app widget manager that we're now configured
                    Intent resultValue = new Intent();
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                    setResult(RESULT_OK, resultValue);

                    Intent widgetUpdate = new Intent();
                    widgetUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId });

                    // make this pending intent unique
                    widgetUpdate.setData(Uri.withAppendedPath(Uri.parse(ZabbixWidgetProvider.URI_SCHEME + "://widget/id/"), String.valueOf(appWidgetId)));
                    PendingIntent newPending = PendingIntent.getBroadcast(getApplicationContext(), 0, widgetUpdate, PendingIntent.FLAG_UPDATE_CURRENT);

                    // schedule the new widget for updating
                    //Log.d(LOG_TAG, "updateRateSeconds: " + updateRateSeconds* 1000 * 60+"\nMin:"+updateRateSeconds);
                    AlarmManager alarms = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                    alarms.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), updateRateSeconds * 1000 * 60, newPending);
                }

                // activity is now done
                finish();
            }
        });
    }
    
    public List<String> getServersList() {
		DBAdapter db = new DBAdapter(this);
		db.open();
		List<String> Servers = db.selectServerNames();
		db.close();
		return Servers;
	}
    
    public void onClickWidget(View view) {
    	Log.d(LOG_TAG, "onClickWidget ZabbixWidgetPrefs");
    }
}