package ru.sonic.zabbix;

import ru.sonic.zabbix.base.ZabbixAPIHandler;
import ru.sonic.zabbix.service.ServicePrefActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * main window, display available tasks
 * @author gryphius
 */
public class ConfigurationActivity extends Activity {
	/** Called when the activity is first created. */
	private static final int SERVERCONFIG=0;
	
	private static final int CHECKSCONFIG=1;
	private static final int SERVICE=2;
	private static final int OTHER=3;
	protected ZabbixAPIHandler api = null;
	
	private OnItemClickListener mainMenuItemListener = new OnItemClickListener(){

		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			
			switch (position) {
			case SERVERCONFIG:
				Intent ServerListActivity = new Intent(getBaseContext(),
						ServerListActivity.class);
				startActivity(ServerListActivity);
				break;
				
		
				
			case CHECKSCONFIG:
				Intent checkspref = new Intent(getBaseContext(),
						ChecksPrefActivity.class);
				startActivity(checkspref);
				break;
				
			case SERVICE:
				Intent service = new Intent(getBaseContext(),
						ServicePrefActivity.class);
				startActivity(service);
				break;
				
			case OTHER:
				Intent other = new Intent(getBaseContext(),
						OtherPrefActivity.class);
				startActivity(other);
				break;
				
			default:
				Toast.makeText(getApplicationContext(),
						"Not yet implemented", Toast.LENGTH_SHORT).show();
				break;
			}
		}
		
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configuration);

		ListView lv=(ListView)findViewById(R.id.configlistview);
		lv.setOnItemClickListener(mainMenuItemListener);
	}
}