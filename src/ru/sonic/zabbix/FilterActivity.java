package ru.sonic.zabbix;


import ru.sonic.zabbix.base.ZabbixAPIHandler;
import android.app.Activity;
import android.os.Bundle;

/**
 * display active triggers in a listview
 * @author gryphius
 *
 */
public class FilterActivity extends Activity {
	protected static ZabbixAPIHandler api = null;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.serverpref_layout);
		
	}
}
