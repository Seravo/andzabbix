package ru.sonic.zabbix;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import ru.sonic.zabbix.adapters.DefaultAdapter;
import ru.sonic.zabbix.base.HostGroup;
import ru.sonic.zabbix.base.ZabbixAPIException;


/**
 * display host groups
 * @author gryphius
 *
 */
public class HostGroupActivity extends DefaultZabbixListActivity {
	private static final String TAG = "ZabbixAPIHostGroupActivity";
	
	@Override
	protected List getData() throws ZabbixAPIException {
		return api.getHostGroups();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ListView lv=(ListView)findViewById(android.R.id.list);
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ListView lv=(ListView)findViewById(android.R.id.list);
				HostGroup group=(HostGroup)(lv.getAdapter().getItem(position));
				Intent GroupIntent = new Intent(getBaseContext(),
						HostListActivity.class);
				GroupIntent.putExtra("groupID", group.getID());
				startActivity(GroupIntent);			
			}

		});
	}
	
	@Override
	public void setListContent(List data) {	
        DefaultAdapter hostgrAdapter = new DefaultAdapter(this,data); 
        setListAdapter( hostgrAdapter );
		if (data.size()==0){
			Toast.makeText(getApplicationContext(),
					"No data to display", Toast.LENGTH_LONG).show();
			}
	}
}
