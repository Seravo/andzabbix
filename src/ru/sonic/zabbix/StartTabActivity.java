package ru.sonic.zabbix;

import java.util.ArrayList;
import java.util.Arrays;

import ru.sonic.zabbix.base.ZabbixAPIHandler;
import ru.sonic.zabbix.service.ZBXCheckService;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

/**
 * display active triggers in a listview
 * @author gryphius
 *
 */
public class StartTabActivity extends TabActivity {
    public static final int FIRST_TAB = 1;
    public static final int SECOND_TAB = 2;
    public static final int THIRD_TAB = 3;
    public static final int FOURD_TAB = 4;
    public static final int FIFTED_TAB = 5;
	protected ZabbixAPIHandler api = null;
    public static final ArrayList<String> TabNames = new ArrayList<String>( Arrays.asList("tab_first", "tab_second", "tab_third", "tab_fourd"));
    
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabs);
		
	    if (firstTabStart()) {
	    	SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	        SharedPreferences.Editor configEditor = config.edit();
	        configEditor.putBoolean("firstTabStart",false);
	        configEditor.putString("tabscount","3");
	        configEditor.putString("tab_first","1");
	        configEditor.putString("tab_second","2");
	        configEditor.putString("tab_third","3");
	        configEditor.putString("tab_fourd","4");
	        configEditor.putString("tab_fifted","5");
	        configEditor.commit();
		}

	    TabHost tabHost = getTabHost();  // The activity TabHost
	
	    int tabscount = gettabscount();
	    int count = 0;
	    for (String str: TabNames) {
	     	if (count < tabscount) {
	       		count++;
	       		tabHost.addTab(getActivity(str));
	       	}
	       	else break;
	    }
	
	    Intent intent = new Intent().setClass(this, ConfigurationActivity.class);
	    Resources res = getResources();
	    TabHost.TabSpec spec = tabHost.newTabSpec("Options").setIndicator("Options",
	            res.getDrawable(android.R.drawable.ic_menu_manage)).setContent(intent);
	    tabHost.addTab(spec);
	     
	    tabHost.setCurrentTab(0);
	    
	    service_check();
	}
	
	private TabSpec getActivity(String num) {
        Intent intent;  // Ресурс Intent для получения acticity
        TabHost tabHost = getTabHost();  // The activity TabHost
        Resources res = getResources(); // Resource object to get Drawables
        TabHost.TabSpec spec = null;  // Ресурс TabSpec для создания вкладок
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int TabName = Integer.parseInt(prefs.getString(num,"0"));
		switch (TabName) {
			case 1:
		        intent = new Intent().setClass(this, ActiveTriggerActivity.class);
		        spec = tabHost.newTabSpec("Active Triggers").setIndicator("Active Triggers",
		                res.getDrawable(R.drawable.trigger_active)).setContent(intent);
		    break;
			case 2:
			    intent = new Intent().setClass(this, HostListActivity.class);
			    intent.putExtra("groupID", "");
			    spec = tabHost.newTabSpec("Overwiev").setIndicator("Overwiev",
			            res.getDrawable(R.drawable.zabbix_available)).setContent(intent);
			break;
			case 3:
		        intent = new Intent().setClass(this, GraphHostsActivity.class);
		        spec = tabHost.newTabSpec("Graphs list").setIndicator("Graphs",
		                res.getDrawable(R.drawable.graph)).setContent(intent);
			break;
			case 4:
		        intent = new Intent().setClass(this, HostGroupActivity.class);
		        spec = tabHost.newTabSpec("Host groups").setIndicator("Host groups",
		                res.getDrawable(android.R.drawable.ic_menu_slideshow)).setContent(intent);
			break; /*
			case 5:
		        intent = new Intent().setClass(this, DHostListActivity.class);
		        spec = tabHost.newTabSpec("DHost list").setIndicator("DHost list",
		                res.getDrawable(android.R.drawable.ic_menu_manage)).setContent(intent);
			break;/*
			case 5:
		        intent = new Intent().setClass(this, EventListActivity.class);
		        spec = tabHost.newTabSpec("Events list").setIndicator("Events list",
		                res.getDrawable(android.R.drawable.ic_menu_manage)).setContent(intent);
			break; */
			default:
		        intent = new Intent().setClass(this, ActiveTriggerActivity.class);
		        spec = tabHost.newTabSpec("Active Triggers").setIndicator("Active Triggers",
		                res.getDrawable(R.drawable.trigger_active)).setContent(intent);
			break;
		}
        return spec;
	}
	
	public boolean firstTabStart(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean("firstTabStart",true);
	}
	
	public boolean use_switch(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean("use_switch_interface",false);
	}
	
	public Integer gettabscount(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return Integer.parseInt(prefs.getString("tabscount","0"));
	}
	
	public void service_check(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs.getBoolean("service_enable", false)) {
			startService(new Intent(this, ZBXCheckService.class));
		}
	}
}
