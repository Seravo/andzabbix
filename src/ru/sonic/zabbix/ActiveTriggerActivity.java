package ru.sonic.zabbix;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ru.sonic.zabbix.ActiveTriggerInfoActivity;
import ru.sonic.zabbix.DefaultZabbixListActivity;
import ru.sonic.zabbix.EventListActivity;
import ru.sonic.zabbix.GraphsListActivity;
import ru.sonic.zabbix.ServerListActivity;
import ru.sonic.zabbix.adapters.TriggersAdapter;
import ru.sonic.zabbix.base.Trigger;
import ru.sonic.zabbix.base.ZabbixAPIException;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

/**
 * display active triggers in a listview
 * @author gryphius
 *
 */ 
public class ActiveTriggerActivity extends DefaultZabbixListActivity {
	protected static final int CONTEXTMENU_SHOWHTRIGGERPARAMS = 0;
	protected static final int CONTEXTMENU_DISABLE = 1;
	protected static final int CONTEXTMENU_DELETE = 2;
	protected static final int CONTEXTMENU_SHOWEVENTS = 3;
	protected static final int CONTEXTMENU_ACTIVATE = 4;
	TimerTask mTimerTask;
	final Handler handler = new Handler();
	Timer t = new Timer();	
	ActiveTriggerActivity ctx;
	
	@SuppressWarnings("unchecked")
	@Override
	protected List getData() throws ZabbixAPIException {
		if (showunaskonly()) return api.getActiveTriggers(true);
		else {
			List<Trigger> ret = new ArrayList<Trigger>();
			List<Trigger> for_sort = new ArrayList<Trigger>();
			for_sort = api.getActiveTriggers(false);
			for_sort.addAll(api.getActiveTriggers(true));
			
			if (is_trigger_sort_by_status()) {
				for (Iterator<Trigger> alert = for_sort.iterator(); alert.hasNext();) {
					Trigger trig = alert.next();
					if (Integer.parseInt(trig.getActive()) == 1) {
						ret.add(trig);
					}
				}
				for (Iterator<Trigger> alert = for_sort.iterator(); alert.hasNext();) {
					Trigger trig = alert.next();
					if (Integer.parseInt(trig.getActive()) != 1) {
						ret.add(trig);
					}
				}
				return ret;
			} else {
				return for_sort;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setListContent(List data) {
        TriggersAdapter triggerAdapter = new TriggersAdapter(this,data);
        setListAdapter( triggerAdapter );
		
		if (data.size()==0){
			Toast.makeText(getApplicationContext(),
					"No data to display", Toast.LENGTH_LONG).show();
			}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			
	    if (firstStart()) {
	    	SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	        SharedPreferences.Editor configEditor = config.edit();
	        configEditor.putBoolean("firstStart",false);
	        configEditor.commit();
	    	Intent settingsActivity = new Intent(getBaseContext(),ServerListActivity.class);
	        startActivityForResult(settingsActivity,1);
		}
	    
			setPrefCurrentServer();
			setTitle("Active triggers (Last 30min)");
			
			ListView lv=(ListView)findViewById(android.R.id.list);
			lv.setTextFilterEnabled(true);
	
			lv.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					ListView lv=(ListView)findViewById(android.R.id.list);
					Trigger trigger=(Trigger)(lv.getAdapter().getItem(position));
					
					String hostid = trigger.getHostID();
					String hostname = trigger.getHost();
					
					Intent GraphListIntent = new Intent(getBaseContext(),GraphsListActivity.class);
					GraphListIntent.putExtra("hostid", hostid);
					GraphListIntent.putExtra("hostName", hostname);
					startActivity(GraphListIntent);
				}
	
			}); 
			
			lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {  
	            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo info) {    
	                menu.setHeaderTitle("Trigger options");  
	                menu.add(0, CONTEXTMENU_SHOWHTRIGGERPARAMS, 0, "Show Information");
	                menu.add(0, CONTEXTMENU_SHOWEVENTS, 0, "Show events");
	                menu.add(0, CONTEXTMENU_DISABLE, 0, "Disable");
	                menu.add(0, CONTEXTMENU_DELETE, 0, "Delete");
	                menu.add(0, CONTEXTMENU_ACTIVATE, 0, "Activate");
	            }
			});
			
			if (isautoref()) {
				ctx = this;
				doTimerTask(getAutorefPeriod());
			}
		}
	
	@Override  
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo)item.getMenuInfo();
    	ListView lv=(ListView)findViewById(android.R.id.list);
    	Trigger trigger=(Trigger)(lv.getAdapter().getItem(menuInfo.position));
    	switch (item.getItemId()) {
        	case CONTEXTMENU_SHOWHTRIGGERPARAMS:
				Intent ActivTriggerIntent = new Intent(getBaseContext(),
						ActiveTriggerInfoActivity.class);
				ActivTriggerIntent.putExtra("triggerID", trigger.getID());
				startActivity(ActivTriggerIntent);
			break;
        	case CONTEXTMENU_DISABLE:
        		showDisableAlertDialog(trigger);
        	break;
        	case CONTEXTMENU_DELETE:
        		showDelAlertDialog(trigger);
        	break;
        	case CONTEXTMENU_SHOWEVENTS:
				Intent events = new Intent(getBaseContext(),EventListActivity.class);
				events.putExtra("triggerId", trigger.getID());
				events.putExtra("triggerDesc", trigger.getDescription());
				startActivity(events);
        	break;
        	case CONTEXTMENU_ACTIVATE:
        		try {
					api.activateTrigger(trigger.getID());
						Toast.makeText(getApplicationContext(),
								"Trigger activated", Toast.LENGTH_LONG).show();
					refreshData(false);
				} catch (ZabbixAPIException e) {
					e.printStackTrace();
					Toast.makeText(getApplicationContext(),
							"Error: "+e.toString(), Toast.LENGTH_LONG).show();
				}
    		break;
    	}
    return true;
    } 
	
	public void showDisableAlertDialog(Trigger trigger) {
		final String triggerID = trigger.getID();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setMessage("Disable trigger "+ triggerID + "?")
        .setCancelable(false)
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() 
	    {
            public void onClick(DialogInterface dialog, int id) {
				try {
					api.disableTrigger(triggerID);
						Toast.makeText(getApplicationContext(),
								"Trigger disabled", Toast.LENGTH_LONG).show();
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
	
	
	public void showDelAlertDialog(Trigger trigger) {
		final String triggerID = trigger.getID();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setMessage("Delete trigger "+ triggerID + "?")
        .setCancelable(false)
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() 
	    {
            public void onClick(DialogInterface dialog, int id) {
				try {
					api.deleteTrigger(triggerID);
						Toast.makeText(getApplicationContext(),
								"Trigger deleted", Toast.LENGTH_LONG).show();
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
	
	public boolean showunaskonly(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
        return prefs.getBoolean("showunaskonly",false);
	}
	
	public boolean firstStart(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean("firstStart",true);
	}
	
	public boolean isautoref(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean("use_auto_reftriggers",false);
	}
	
	public int getAutorefPeriod(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return Integer.parseInt(prefs.getString("autoref_period","0"));
	}
	
	public boolean is_trigger_sort_by_status(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean filter_sort = prefs.getBoolean("sort_triggers",true);
        return filter_sort;
	}
	
   	@Override
 	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 	  super.onActivityResult(requestCode, resultCode, data);
		refreshData(true);
 	}
   	
   	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	stopTask();
	    }
	    return super.onKeyDown(keyCode, event);
	}
   	
   	public void doTimerTask(int period){
    	mTimerTask = new TimerTask() {
    	        public void run() {
    	                handler.post(new Runnable() {
    	                        public void run() {
    	                        	ctx.refreshData(false);
    	                        }
    	               });
    	        }};
            // public void schedule (TimerTask task, long delay, long period) 
    	    t.schedule(mTimerTask, 30000, period*1000);  // 
    	 }
	
	public void stopTask(){
    	   if(mTimerTask!=null){
    	      mTimerTask.cancel();
    	 }
    }
}