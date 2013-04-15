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
	
	public final static String GRAPHS_MESSAGE = "com.example.myfirstapp.MESSAGE";
	public final static String FAQ_MESSAGE = "com.example.myfirstapp.FAQMESSAGE";
	public final static String SystemOverview_MESSAGE = "com.example.myfirstapp.FAQMESSAGE";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		Button servConfigButton=(Button)findViewById(R.id.az_button_config_down);
		servConfigButton.setOnClickListener(servConfigListener);
	}
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
	


	//Called when the user clicks the Graphs button
	//This intent initiates the GraphsActivity class that uses the achartengine
	
//	public void openGraph(View view){
	//	GraphsActivity graphs = new GraphsActivity();
	//	Intent graphsIntent = graphs.getIntent(this);
	//	startActivity(graphsIntent);
	
	
	// Called when the user clicks the FAQ button
	// Intent can carry a data bundle! In this case the String seen below
	public void openFAQ(View view){
		Intent intent = new Intent(this, FAQActivity.class);
		String displayText = "This is the FAQ view/activity".toString();
		intent.putExtra(FAQ_MESSAGE, displayText);
		startActivity(intent);
		
	}
	
	public void openSystemOverview(View view){
		Intent intent = new Intent(this, SystemOverviewActivity.class);
		String displayText = "This is the System Overview activity".toString();
		intent.putExtra(SystemOverview_MESSAGE, displayText);
		startActivity(intent);
	}
	//public void openServerConfiguration(View view){
		//ServerActivity servConfig=new ServerActivity();
		//Intent serverIntent=servConfig.getIntent();
		
//		startActivity(serverIntent);
		//Intent serverIntent = new Intent(MainActivity.this, ServerActivity.class);
		//MainActivity.this.startActivity(serverIntent);
		
//}
}
