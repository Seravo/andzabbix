package ru.sonic.zabbix;

import java.util.List;

import ru.sonic.zabbix.R;
import ru.sonic.zabbix.base.Item;
import ru.sonic.zabbix.base.ZabbixAPIException;
import ru.sonic.zabbix.base.ZabbixAPIHandler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

/**
 * display all items for a host in a table
 * @author gryphius
 *
 */
public class ItemListActivity extends Activity {
	private String hostid = "";

	protected List<?> getData() throws ZabbixAPIException {
		return api.getItems(hostid);
	}

	private static final int MSG_DATA_RETRIEVED = 0;
	private static final int MSG_ERROR = 1;

	protected static String TAG = "ItemListActivity";
	private ProgressDialog myProgressDialog = null;
	protected ZabbixAPIHandler api = null;
	private int idcount = 0;

	private Handler handler = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			myProgressDialog.dismiss();

			switch (msg.arg1) {
			case MSG_ERROR:
				displayErrorPopup(((ZabbixAPIException) msg.obj).getMessage());
				break;

			case MSG_DATA_RETRIEVED:
				filltable((List<Item>) msg.obj);
			default:
				break;
			}
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		hostid = (String) getIntent().getExtras().get("hostid");
		api = new ZabbixAPIHandler(this);
		setTitle("Host: "+getIntent().getExtras().get("hostName").toString());
		
		refreshData();
	}
	
	public TextView makeTextView(String content) {
		TextView labelTV = new TextView(this);
		labelTV.setId(idcount++);
		labelTV.setText(content);
		return labelTV;
	}

	public TableRow makeTableRow(String left, String right) {
		// Create a TableRow and give it an ID
		TableRow tr = new TableRow(this);
		tr.setId(idcount++);

		// Create a TextView to house the name of the province
		tr.addView(makeTextView(left+"  "));
		
		// Create a TextView to house the value of the after-tax income
		tr.addView(makeTextView(right));

		return tr;
	}

	public void addTableRow(TableRow tr){
		TableLayout tl = (TableLayout) findViewById(R.id.tblmain);
		tl.addView(tr);
	}
	public void filltable(List<Item> data) {

		// Get the TableLayout
		
		if (data.size() == 0) {
			Toast.makeText(getApplicationContext(), "No data to display",
					Toast.LENGTH_LONG).show();
			return;
		}
		
		setContentView(R.layout.table);
		
		// Go through each item in the array
		for (Item item : data) {
			Log.d(TAG, "ExpanedDescription: "+item.getExpanedDescription());
			if (Integer.parseInt(item.getStatus()) == 0) {
				Object val = item.lastValue();
				if (val == null) { val = ""; }
				TableRow tr = makeTableRow(item.getExpanedDescription(), val.toString());
				tr.setBackgroundColor(Color.parseColor("#222222"));
				addTableRow(tr);
			} else {
				Object val = item.lastValue();
				if (val == null) { val = ""; }
				TableRow tr = makeTableRow(item.getExpanedDescription(), val.toString());
				tr.setBackgroundColor(Color.RED);
				addTableRow(tr);
			}
		}
	}

	public void displayErrorPopup(String message) {
		new AlertDialog.Builder(this).setMessage(message).setTitle("Error").show();
	}

	public void refreshData() {

		myProgressDialog = ProgressDialog.show(this, "Please wait...",
				"Retrieving data", true);
		new Thread() {
			public void run() {

				try {
					List data = getData();
					Message msg = new Message();
					msg.arg1 = MSG_DATA_RETRIEVED;
					msg.obj = data;
					handler.sendMessage(msg);
				} catch (ZabbixAPIException e) {
					Log.e(TAG, "rpc call failed: " + e);
					Message msg = new Message();
					msg.arg1 = MSG_ERROR;
					msg.obj = e;
					handler.sendMessage(msg);
					return;
				}
			}
		}.start();
	}
}
