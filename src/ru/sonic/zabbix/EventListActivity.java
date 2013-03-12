package ru.sonic.zabbix;

import java.util.List;

import ru.sonic.zabbix.adapters.EventAdapter;
import ru.sonic.zabbix.base.Event;
import ru.sonic.zabbix.base.ZabbixAPIException;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

/**
 * display active triggers in a listview
 * @author gryphius
 *
 */ 
public class EventListActivity extends DefaultZabbixListActivity {
	protected static final int CONTEXTMENU_DELETEEVENT = 0;
	protected static final int CONTEXTMENU_ACK = 1;
	
	@SuppressWarnings("unchecked")
	@Override
	protected List getData() throws ZabbixAPIException {
		try {
			Bundle extras  = getIntent().getExtras();
			String triggerId  = extras.getString("triggerId");
			if (triggerId == null) {triggerId = "0";}
			return api.getEvent(triggerId);
		} catch (Exception e) {
			return api.getEvent("0");
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setListContent(List data) {
        EventAdapter eventAdapter = new EventAdapter(this,data);
        setListAdapter( eventAdapter );
		
		if (data.size()==0){
			Toast.makeText(getApplicationContext(),
					"No data to display", Toast.LENGTH_LONG).show();
			}
	} 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
			setPrefCurrentServer();
			try {
				Bundle extras  = getIntent().getExtras();
				String triggerDesc  = extras.getString("triggerDesc");
				setTitle("Trigger: "+triggerDesc);
			} catch (Exception e) {
				
			}
			Button selectServer = (Button)findViewById(R.id.selectServer);
			selectServer.setVisibility(View.GONE);
			
			ListView lv=(ListView)findViewById(android.R.id.list);
			lv.setTextFilterEnabled(true);
	
			
			lv.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					ListView lv=(ListView)findViewById(android.R.id.list);
					Event event=(Event)(lv.getAdapter().getItem(position));
					showAckAlertDialog(event.getID());
					/*
					Intent ActivTriggerIntent = new Intent(getBaseContext(),
							ActiveTriggerInfoActivity.class);
					ActivTriggerIntent.putExtra("triggerID", trigger.getID());
					startActivity(ActivTriggerIntent); */			
				}
	
			});  
			
			lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {  
	            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo info) {    
	                menu.setHeaderTitle("Trigger options");  
	                menu.add(0, CONTEXTMENU_DELETEEVENT, 0, "Delete");
	                menu.add(0, CONTEXTMENU_ACK, 0, "Acknowledge");
	            }
			});
		}
	
	@Override  
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo)item.getMenuInfo();
    	ListView lv=(ListView)findViewById(android.R.id.list);
    	Event event=(Event)(lv.getAdapter().getItem(menuInfo.position));
    	switch (item.getItemId()) {
        	case CONTEXTMENU_DELETEEVENT:
        		showDelAlertDialog(event.getID());
				break;
        	case CONTEXTMENU_ACK:
        		showAckAlertDialog(event.getID());
        		break;
    	}
    return true;
    } 
	
	public void showAckAlertDialog(final String eventId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final EditText message = new EditText(context);
		message.setHint("comment");
		builder.setMessage("Ack event "+ eventId + "?")
        .setCancelable(false)
	    .setView(message)
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() 
	    {
            public void onClick(DialogInterface dialog, int id) {
				try {
					api.ackEvent(eventId,message.getText().toString());
						Toast.makeText(getApplicationContext(),
								"Event acknowleged", Toast.LENGTH_LONG).show();
					refreshData(false);
				} catch (ZabbixAPIException e) {
					e.printStackTrace();
					Toast.makeText(getApplicationContext(),
							"Error: "+e.toString(), Toast.LENGTH_LONG).show();
					}
            }
        })
		
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
		});

		AlertDialog alert = builder.create();
		alert.show();
	}
	
	public void showDelAlertDialog(final String eventId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setMessage("Delete event "+ eventId + "?")
        .setCancelable(false)
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() 
	    {
            public void onClick(DialogInterface dialog, int id) {
				try {
					//String LastEventId = eventIDs.get(eventIDs.size()-1);
					api.delEvent(eventId);
						Toast.makeText(getApplicationContext(),
								"Event deleted", Toast.LENGTH_LONG).show();
					refreshData(false);
				} catch (ZabbixAPIException e) {
					e.printStackTrace();
					Toast.makeText(getApplicationContext(),
							"Error: "+e.toString(), Toast.LENGTH_LONG).show();
					}
            }
        })
		
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
		});

		AlertDialog alert = builder.create();
		alert.show();
	}
}