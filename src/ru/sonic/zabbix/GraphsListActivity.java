package ru.sonic.zabbix;

import java.util.List;

import ru.sonic.zabbix.adapters.GraphListAdapter;
import ru.sonic.zabbix.base.Graph;
import ru.sonic.zabbix.base.ZabbixAPIException;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


/**
 * display all hosts
 * @author gryphius
 *
 */
public class GraphsListActivity extends DefaultZabbixListActivity {
//	private static final String TAG = "GraphsListActivity";
	
	@Override
	protected List getData() throws ZabbixAPIException {
		Bundle extras = getIntent().getExtras();
		String hostID  = extras.getString("hostid");
		if (hostID == null) {hostID = "";}
		return api.getGraphs(hostID);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		setTitle("Host: "+extras.getString("hostName"));

		Button selectServer = (Button)findViewById(R.id.selectServer);
		selectServer.setVisibility(View.GONE);
		
		ListView lv=(ListView)findViewById(android.R.id.list);
		lv.setTextFilterEnabled(true);
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ListView lv=(ListView)findViewById(android.R.id.list);
				Graph graph=(Graph)(lv.getAdapter().getItem(position));
				Intent graphIntent = new Intent(getBaseContext(),GraphActivity.class);
				graphIntent.putExtra("graphID", graph.getID());
				graphIntent.putExtra("yaxismax",graph.getyaxismax());
				graphIntent.putExtra("yaxismin",graph.getyaxismin());
				graphIntent.putExtra("name",graph.getName());
				startActivity(graphIntent);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setListContent(List data) {
		GraphListAdapter graphListAdapter = new GraphListAdapter(this,data);
        setListAdapter( graphListAdapter );
		
		if (data.size()==0){
			Toast.makeText(getApplicationContext(),
					"No data to display", Toast.LENGTH_LONG).show();
			}
	}
	
}
