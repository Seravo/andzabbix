package com.example.andzabbix;
 
import java.util.List;

import com.example.andzabbix.adapters.ServerAdapter;

import ru.sonic.zabbix.base.DBAdapter;
import ru.sonic.zabbix.base.ZabbixAPIException;
//import ru.sonic.zabbix.base.ZabbixAPIHandler; commented by zak
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class ServerListActivity extends Activity {
	
	protected static final int CONTEXTMENU_DELETEITEM = 0;
	protected static final int CONTEXTMENU_EDITITEM = 1;
	protected ListView lv;
	//protected ZabbixAPIHandler api = null; --zak
	
	//@Override
	protected List<?> getData() throws ZabbixAPIException {
		return getServersList();
	}
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_item);
		setTitle("Server List");
		
		Toast.makeText(getApplicationContext(), "Press 'Menu' button to to add server.", Toast.LENGTH_SHORT).show();

		   
		LinearLayout ll = (LinearLayout)findViewById(R.id.buttonsll);
		ll.setVisibility(LinearLayout.GONE);
		
		lv=(ListView)findViewById(android.R.id.list);
		lv.setTextFilterEnabled(true);
		//registerForContextMenu(lv);
		
		lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {  
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo info) {    
                menu.setHeaderTitle(R.string.contentMenu);  
                menu.add(0, CONTEXTMENU_EDITITEM, 0, "Edit server");
                menu.add(0, CONTEXTMENU_DELETEITEM, 0, "Delete server");
            }
        	
		});
		
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ListView lv=(ListView)findViewById(android.R.id.list);
            	Intent serverEditActivity = new Intent(getBaseContext(),ServerActivity.class);
            	serverEditActivity.putExtra("Servername", lv.getAdapter().getItem(position).toString());
            	serverEditActivity.putExtra("action", "edit");
    			startActivityForResult(serverEditActivity, 1);		
			}

		}); 
		
		refreshData();
    }
    
    @Override  
    public boolean onContextItemSelected(MenuItem item) {
    	DBAdapter db = new DBAdapter(this);
    	AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo)item.getMenuInfo();
    	switch (item.getItemId()) {
        	case CONTEXTMENU_EDITITEM:
            	Intent serverEditActivity = new Intent(getBaseContext(),ServerActivity.class);
            	serverEditActivity.putExtra("Servername", getNameCheckedserver(menuInfo));
            	serverEditActivity.putExtra("action", "edit");
    			startActivityForResult(serverEditActivity, 1);
        	break;
        	case CONTEXTMENU_DELETEITEM:
            	db.open();
            	db.deleteServer(getNameCheckedserver(menuInfo));
            	db.close(); 
        	break;
    	}
    	refreshData();
    return true;
    } 
    
    public String getNameCheckedserver (AdapterContextMenuInfo menuInfo) {
    	return lv.getAdapter().getItem(menuInfo.position).toString();
    }
    
	//@Override
	public void setListContent(List<String> data) {
        ServerAdapter serverAdapter = new ServerAdapter(this,data ); 
        setListAdapter( serverAdapter );

		if (data.size()==0){
			Toast.makeText(getApplicationContext(),
					"No data to display", Toast.LENGTH_LONG).show();
			}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.servers, menu);
		return true;
	}
	
	/**
	 * 
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case R.id.add_server:
        	Intent serverprefActivity = new Intent(getBaseContext(),ServerActivity.class);
        	serverprefActivity.putExtra("Servername", "Server name");
        	serverprefActivity.putExtra("action", "new");
			startActivityForResult(serverprefActivity, 0);			
        	break;
		}
		return false;
	}
	
	@Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  super.onActivityResult(requestCode, resultCode, data);
	  refreshData();
	  
		// Set first server if it not selected
	  List<String> Servers =  getServersList();
		boolean flagsrv = false;
		for (String srv: Servers) {
			if (srv.equals(getCurrentServer())) flagsrv = true;
		}
		
		if (!flagsrv && Servers.size()>0) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("currentServer", Servers.get(0));
	        editor.commit();
		}
		// complete set
	 }
	
	public List<String> getServersList() {
		DBAdapter db = new DBAdapter(this);
		db.open();
		List<String> Servers = db.selectServerNames();
		db.close();
		return Servers;
	}
	
	protected void setListAdapter(Adapter dataArray) {
		ListView myList=(ListView)findViewById(android.R.id.list);
		myList.setAdapter((ListAdapter) dataArray);
	}
	
	protected void refreshData() {
		setListContent(getServersList());
	}
	
	public String getCurrentServer() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		return prefs.getString("currentServer","Not selected");
	}
}