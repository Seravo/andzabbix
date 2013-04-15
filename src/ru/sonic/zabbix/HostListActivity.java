package ru.sonic.zabbix;

import java.util.List;

import ru.sonic.zabbix.adapters.HostAdapter;
import ru.sonic.zabbix.base.Host;
import ru.sonic.zabbix.base.ZabbixAPIException;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;



/**
 * display all hosts
 * @author gryphius
 *
 */
public class HostListActivity extends DefaultZabbixListActivity {
	protected static final int CONTEXTMENU_SHOWHOSTPREF = 0;
	protected static final int CONTEXTMENU_SHOWITEMS = 1;
	protected static final int CONTEXTMENU_SHOWHOSTGRAPHS = 2;
	protected static final int CONTEXTMENU_ACTIVATEHOST = 3; 
	protected static final int CONTEXTMENU_DISABLEHOST = 4; 
	@Override
	protected List getData() throws ZabbixAPIException {
		Bundle extras = getIntent().getExtras();
		String groupID  = extras.getString("groupID");
		if (groupID == null) {groupID = "";}
		return api.getHosts(groupID);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Button selectServer = (Button)findViewById(R.id.selectServer);
		//selectServer.setVisibility(View.GONE);
		
		ListView lv=(ListView)findViewById(android.R.id.list);
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ListView lv=(ListView)findViewById(android.R.id.list);
				Host host=(Host)(lv.getAdapter().getItem(position));
				Intent itemlistintent = new Intent(getBaseContext(),
						ItemListActivity.class);
				itemlistintent.putExtra("hostid", host.getID());
				itemlistintent.putExtra("hostName", host.getName());
				startActivity(itemlistintent);
			}

		});
		
		lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {  
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo info) {    
                menu.setHeaderTitle(R.string.contentMenu);  
                menu.add(0, CONTEXTMENU_SHOWITEMS, 0, "Show Items");
                menu.add(0, CONTEXTMENU_SHOWHOSTPREF, 0, "Host Info");
                menu.add(0, CONTEXTMENU_SHOWHOSTGRAPHS, 0, "Show Graphs");
                menu.add(0, CONTEXTMENU_ACTIVATEHOST, 0, "Activate");
                menu.add(0, CONTEXTMENU_DISABLEHOST, 0, "Disable");
            }
		});
	}
	
	@Override  
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo)item.getMenuInfo();
    	ListView lv=(ListView)findViewById(android.R.id.list);
    	Host host=(Host)(lv.getAdapter().getItem(menuInfo.position));
    	switch (item.getItemId()) {
        	case CONTEXTMENU_SHOWITEMS:
				Intent itemlistintent = new Intent(getBaseContext(),ItemListActivity.class);
				itemlistintent.putExtra("hostid", host.getID());
				itemlistintent.putExtra("hostName", host.getName());
				startActivity(itemlistintent);
        	break;
        	case CONTEXTMENU_SHOWHOSTPREF:
				Intent hostPrefs = new Intent(getBaseContext(),HostInfoActivity.class);
				hostPrefs.putExtra("hostid", host.getID());
				startActivity(hostPrefs);
        	break;
           	case CONTEXTMENU_SHOWHOSTGRAPHS:
				Intent graphs = new Intent(getBaseContext(),GraphsListActivity.class);
				graphs.putExtra("hostid", host.getID());
				graphs.putExtra("hostName", host.getName());
				startActivity(graphs);
        	break;
           	case CONTEXTMENU_ACTIVATEHOST:
			try {
				api.activateHost(host.getID());
           		refreshData(false);
			} catch (ZabbixAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	break;  
           	case CONTEXTMENU_DISABLEHOST:
    			try {
    				api.disableHost(host.getID());
               		refreshData(false);
    			} catch (ZabbixAPIException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
            break;
    	}
    return true;
    } 

	@Override
	public void setListContent(List data) {
        HostAdapter hostAdapter = new HostAdapter(this,data ); 
        setListAdapter( hostAdapter );
		//Log.e(TAG, "setListContent");
		
		if (data.size()==0){
			Toast.makeText(getApplicationContext(),
					"No data to display", Toast.LENGTH_LONG).show();
			}
	}
}
