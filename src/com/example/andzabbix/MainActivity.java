package com.example.andzabbix;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	
	public final static String GRAPHS_MESSAGE = "com.example.myfirstapp.MESSAGE";
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
		Intent intent = new Intent(this, GraphsActivity.class);
		String displayText = "This is the graphs view/activity".toString();
		intent.putExtra(GRAPHS_MESSAGE, displayText);
		startActivity(intent);
	}

}
