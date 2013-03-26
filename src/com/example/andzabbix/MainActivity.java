package com.example.andzabbix;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	
	public final static String GRAPHS_MESSAGE = "com.example.myfirstapp.MESSAGE";
	public final static String FAQ_FAQMESSAGE = "com.example.myfirstapp.FAQMESSAGE";
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
	
	// Called when the user clicks the graph button
	// Intent can carry a data bundle!
	public void openGraph(View view){
		GraphsActivity graphs = new GraphsActivity();
		Intent graphsIntent = graphs.getIntent(this);
		startActivity(graphsIntent);
	}
	
	public void openFAQ(View view){
		Intent intent = new Intent(this, FAQActivity.class);
		String displayText = "This is the FAQ view/activity".toString();
		intent.putExtra(FAQ_FAQMESSAGE, displayText);
		startActivity(intent);
		
	}

}
