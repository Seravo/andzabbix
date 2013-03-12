package ru.sonic.zabbix;

import ru.sonic.zabbix.base.ZabbixAPIException;
import ru.sonic.zabbix.base.ZabbixAPIHandler;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;

/**
 * display active triggers in a listview
 * @author gryphius
 *
 */
public class DEBUGActivity extends TabActivity {
	protected static ZabbixAPIHandler api = null;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabs);
		
		Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Ресурс TabSpec для создания вкладок
        Intent intent;  // Ресурс Intent для получения acticity

        intent = new Intent().setClass(this, ActiveTriggerActivity.class);
        spec = tabHost.newTabSpec("ActiveTrigger").setIndicator("ActiveTrigger",
                res.getDrawable(R.drawable.graph))
           .setContent(intent);
        tabHost.addTab(spec);
        
        intent = new Intent().setClass(this, HostListActivity.class);
        intent.putExtra("groupID", "");
        spec = tabHost.newTabSpec("HostList").setIndicator("HostList",
                          res.getDrawable(R.drawable.icon))
                    .setContent(intent);
        tabHost.addTab(spec);
        
        intent = new Intent().setClass(this, GraphHostsActivity.class);
        spec = tabHost.newTabSpec("Graph").setIndicator("Graph",
                          res.getDrawable(R.drawable.ok_icon))
                    .setContent(intent);
        tabHost.addTab(spec);

        // Установим активную вкладку
        tabHost.setCurrentTab(0);
	}
	
	public static String getActiveTriggers () throws ZabbixAPIException {
		Log.d("DEBUGActivity", "getActiveTriggers...");
		int size = api.getActiveTriggersCount("ds").size();
		Log.d("DEBUGActivity", "size"+size);
		return ""+size;
	}
}
