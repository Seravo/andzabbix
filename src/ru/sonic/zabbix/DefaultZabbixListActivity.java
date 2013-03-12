package ru.sonic.zabbix;

import java.util.List;
import java.util.Vector;

import ru.sonic.zabbix.adapters.DefaultAdapter;
import ru.sonic.zabbix.base.DBAdapter;
import ru.sonic.zabbix.base.ZabbixAPIException;
import ru.sonic.zabbix.base.ZabbixAPIHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * base listview with common code to retrieve a list of zabbix objects and display them
 * @author gryphius
 *
 */
public class DefaultZabbixListActivity extends Activity {
	private static final int MSG_DATA_RETRIEVED = 0;
	private static final int MSG_ERROR = 1;
	private static final int MSG_LENGTH = 2;
	private static final int MSG_LOOP = 3;

	private Button refresh;
	protected static String TAG = "DefaultZabbixListActivity";
	private ProgressDialog myProgressDialog = null;
	protected ZabbixAPIHandler api = null;
	protected Context context = this;
	DBAdapter db = new DBAdapter(this);
	
	/**
	 * the retriever thread cannot modify the gui after it's done
	 * Therefore it justs sends the information (data or error) to the handler which again does the gui stuff
	 */
	private Handler handler = new Handler() {
		int k = 0;
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.arg1) {
			case  MSG_ERROR:
				if (msg.arg2 == 1)
					try {
						myProgressDialog.dismiss();
					} catch (Exception e) {
			        	// TO-DO exception 
					}
					displayErrorPopup(((ZabbixAPIException)msg.obj).getMessage());
				break;
				
			case MSG_DATA_RETRIEVED:
				try {
					myProgressDialog.dismiss();
				} catch (Exception e) {
		        	// TO-DO exception 
				}
				setListContent((List<String>)msg.obj);
				break;
			default:
				break;
			}		
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_item);
		api = new ZabbixAPIHandler(this);
		
		if (api.getCurrentServer().equals("Not selected")) {
			try {
				api.setFirstServer();
			} catch (ZabbixAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
		refresh = (Button)findViewById(R.id.refreshActivity);
		refresh.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setPrefCurrentServer();
				refreshData(true);
			};
		});
		
		Button srv = (Button)findViewById(R.id.selectServer);
		srv.setOnClickListener(on_click_select_server());
		
		refreshData(false);
	}
	
    public OnClickListener on_click_select_server() {
        return new OnClickListener() {
            //@Override
            public void onClick(View v) {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
				final SharedPreferences.Editor editor = prefs.edit();
				db.open();
				final List<String> serverNames =  db.selectServerNames();
				CharSequence[] cs = serverNames.toArray(new CharSequence[serverNames.size()]);
				// Chose SERVER
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Select Zabbix server");
				builder.setItems(cs, new DialogInterface.OnClickListener() {
	        		public void onClick(DialogInterface dialog, int item) {
	        	        //Toast.makeText(getApplicationContext(), cs[item], Toast.LENGTH_SHORT).show();
	        			//Log.e(TAG, "Selecter item: " + serverNames.get(item));
	        			editor.putString("currentServer", serverNames.get(item));
	    		        editor.commit();
	    		        setPrefCurrentServer();
	    		        api.auth = "";
	        	    }
	        	});
	        	AlertDialog alert = builder.create();
	        	alert.show();
				db.close();
            }
        };
    }
	
	public List<String> getServersList () {
		if(db.open()!=null) {
			List<String> Servers = db.selectServerNames();
			db.close();
			return Servers;
		} else {
			return null;
		}
	}

	/**
	 * fill the list
	 * @param data
	 */
	public void setListContent(List<String> data) {
		//ArrayAdapter DataArray = new ArrayAdapter(this,R.layout.list_item,data);
		//setListAdapter(DataArray);
        DefaultAdapter Adapter = new DefaultAdapter(this,data);
        setListAdapter( Adapter );
		if (data.size()==0){
			Toast.makeText(getApplicationContext(),
					"No data to display", Toast.LENGTH_LONG).show();
			}
	}

	protected void setListAdapter(Adapter dataArray) {
		ListView myList=(ListView)findViewById(android.R.id.list);
		myList.setAdapter((ListAdapter) dataArray);
	}

	/**
	 * display an error poup
	 * @param message
	 */
	public void displayErrorPopup(String message) {
		try {
			new AlertDialog.Builder(this).setMessage(message).setTitle("Error").show();
		} catch (Exception e) {
			
		}
	}
	
	/**
	 * set current server
	 * @param 
	 */
	public void setPrefCurrentServer() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		TextView currentServer = (TextView)findViewById(R.id.serverName);
		currentServer.setText(prefs.getString("currentServer","Not selected"));
	}

	/**
	 * get and return the data objects to display
	 * subclasses should override this
	 * @return
	 * @throws ZabbixAPIException
	 */
	protected List<String> getData() throws ZabbixAPIException{
		return new Vector<String>();
	}
	
	/**
	 * start thread to refresh the data from the server
	 */
	public void refreshData(final boolean showDialog) {
		setPrefCurrentServer();
   		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
   		NetworkInfo ni = cm.getActiveNetworkInfo();
   		if (ni!=null && ni.isAvailable() && ni.isConnected()) {
   			if (showDialog) {myProgressDialog = ProgressDialog.show(this, "Please wait...",
				"Retrieving data", true);
   			}
   			
		new Thread() {
			public void run() {
				Message msg=new Message();
				try {
					List<?> data=getData();
					msg.arg1=MSG_DATA_RETRIEVED;
					msg.arg2=showDialog?1:0;
					msg.obj=data;
					//Log.e(TAG, "Retrived data: " + data);
					handler.sendMessage(msg);
				} catch (ZabbixAPIException e) {
					Log.e(TAG, "RPC Call failed: " + e);
					msg.arg1=MSG_ERROR;
					msg.arg2=showDialog?1:0;
					msg.obj=e;
					handler.sendMessage(msg);
					return;
				}
			}
		}.start();
		} else {
			if (showDialog)
			displayErrorPopup ("Check internet connection");
		}
	}

	/**
	 * create the options menu (display refresh button)
	 */
	//@Override
	//public boolean onCreateOptionsMenu(Menu menu) {
		//MenuInflater inflater = getMenuInflater();
		//inflater.inflate(R.menu.menu, menu);
		//Intent confctivity = new Intent(getBaseContext(),
		//		ConfigurationActivity.class);
		//startActivityForResult(confctivity,1);
		//return true;
	//}
	
	/**
	 * handle refresh user request
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
        case R.id.filter:     
			Intent FilterActivity = new Intent(getBaseContext(),
					FilterActivity.class);
			startActivity(FilterActivity);
			break;
		case R.id.mainconfiguration:
			Intent confctivity = new Intent(getBaseContext(),
					ConfigurationActivity.class);
			startActivity(confctivity);
			break;
		default:
			Toast.makeText(getApplicationContext(),
					"Not yet implemented", Toast.LENGTH_SHORT).show();
			break;	
			}
    return true;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	//Log.d(TAG, "onKeyDown KEYCODE_BACK");
	    }
	    return super.onKeyDown(keyCode, event);
	}
}