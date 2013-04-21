package ru.sonic.zabbix;

import ru.sonic.zabbix.R;
import ru.sonic.zabbix.ServerActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity {
	
	
	public final static String FAQ_MESSAGE = "com.example.myfirstapp.FAQMESSAGE";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Called when the user clicks  Overview,host,Trigger,Graphs  button
		
		Button systemOverviewButton=(Button)findViewById(R.id.az_button_center);
		systemOverviewButton.setOnClickListener(systemOverviewListener);
		
		Button hostGroupButton=(Button)findViewById(R.id.az_button_right_down);
		hostGroupButton.setOnClickListener(hostGroupListener);
		
		Button triggergButton=(Button)findViewById(R.id.az_button_left_down);
		triggergButton.setOnClickListener(triggerListener);
		
		Button graphButton=(Button)findViewById(R.id.az_button_left_up);
		graphButton.setOnClickListener(graphsListener);
		
		Button servConfigButton=(Button)findViewById(R.id.az_button_config_down);
		servConfigButton.setOnClickListener(servConfigListener);
		
		
	}
	//The intent initiates the Activity class
	
	public OnClickListener graphsListener=new OnClickListener(){
		public void onClick(View v){
			Intent intent=new Intent();
			intent.setClass(MainActivity.this,GraphHostsActivity.class);
			startActivity(intent);
			
		}
	};
	
	public OnClickListener hostGroupListener=new OnClickListener(){
		public void onClick(View v){
			Intent intent=new Intent();
			intent.setClass(MainActivity.this,HostGroupActivity.class);
			startActivity(intent);
			
		}	
	};
	
	public OnClickListener systemOverviewListener=new OnClickListener(){
		public void onClick(View v){
			Intent intent=new Intent();
			intent.putExtra("groupID", "");
			intent.setClass(MainActivity.this,HostListActivity.class);
			startActivity(intent);
			
			 
			
		}
	};
	
	public OnClickListener triggerListener=new OnClickListener(){
		public void onClick(View v){
			Intent intent=new Intent();
			intent.setClass(MainActivity.this,ActiveTriggerActivity.class);
			startActivity(intent);
			
		}
		
	};
	
		public OnClickListener servConfigListener=new OnClickListener(){
		public void onClick(View v){
			Intent intent=new Intent();
			intent.setClass(MainActivity.this,ConfigurationActivity.class);
			startActivity(intent);
			
		}
		
	};
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	



	
	// Called when the user clicks the FAQ button
	// Intent can carry a data bundle! In this case the String seen below
	public void openFAQ(View view){
		Intent intent = new Intent(this, FAQActivity.class);
		String displayText = "This is the FAQ view/activity".toString();
		intent.putExtra(FAQ_MESSAGE, displayText);
		startActivity(intent);
		
	}
	

}
