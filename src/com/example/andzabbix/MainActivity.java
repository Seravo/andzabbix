package com.example.andzabbix;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	
	public final static String GRAPHS_MESSAGE = "com.example.myfirstapp.MESSAGE";
	public final static String FAQ_MESSAGE = "com.example.myfirstapp.FAQMESSAGE";
	public final static String SystemOverview_MESSAGE = "com.example.myfirstapp.FAQMESSAGE";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	//Called when the user clicks the Graphs button
	//This intent initiates the GraphsActivity class that uses the achartengine
	public void openGraph(View view){
		GraphsActivity graphs = new GraphsActivity();
		Intent graphsIntent = graphs.getIntent(this);
		startActivity(graphsIntent);
	}
	
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
	
	//Sami testing

}
