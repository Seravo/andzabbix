package ru.sonic.zabbix;

import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONObject;

import ru.sonic.zabbix.adapters.DHostAdapter;
import ru.sonic.zabbix.base.DHost;
import ru.sonic.zabbix.base.ZabbixAPIException;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * display all Dhosts
 * @author gryphius
 *
 */
public class DHostListActivity extends DefaultZabbixListActivity {
	protected static final int CONTEXTMENU_SHOWHOSTPREF = 0;
	protected static final int CONTEXTMENU_SHOWITEMS = 1;
	protected static final int CONTEXTMENU_SHOWHOSTGRAPHS = 2;
	protected static final int CONTEXTMENU_ACTIVATEHOST = 3; 
	protected static final int CONTEXTMENU_DISABLEHOST = 4; 
	@Override
	protected List getData() throws ZabbixAPIException {
		List<DHost> dhosts = api.getDHosts();
		for (Iterator<DHost> host = dhosts.iterator(); host.hasNext();) {
			String hostid = host.next().getID();
			Log.e(TAG, "Hostid: "+hostid);
		}
		return null;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}

	@Override
	public void setListContent(List data) {
        DHostAdapter hostAdapter = new DHostAdapter(this,data ); 
        setListAdapter( hostAdapter );
		//Log.e(TAG, "setListContent");
		
		if (data.size()==0){
			Toast.makeText(getApplicationContext(),
					"No data to display", Toast.LENGTH_LONG).show();
			}
	}
}
