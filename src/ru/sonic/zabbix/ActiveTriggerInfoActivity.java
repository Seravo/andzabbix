package ru.sonic.zabbix;

import java.util.ArrayList;
import java.util.List;

import ru.sonic.zabbix.base.Trigger;
import ru.sonic.zabbix.base.ZabbixAPIException;
import android.os.Bundle;
import android.util.Log;

/**
 * display active triggers in a listview
 * @author gryphius
 *
 */
public class ActiveTriggerInfoActivity extends DefaultZabbixListActivity {
	
	@Override
	protected List<String> getData() throws ZabbixAPIException {
		Bundle extras  = getIntent().getExtras();
		Log.d(TAG, "Trigger ID: "+extras.getString("triggerID"));
		List<?> TriggerInfo = api.getActiveTriggerInfo(extras.getString("triggerID"));
		Trigger trigger = (Trigger) TriggerInfo.get(0);
		
		ArrayList<String> ret = new ArrayList<String>();
		
			ret.add("Issue ststus: "+trigger.getIssueStatus()); 
			ret.add("Host: "+trigger.getHost());
			ret.add("Description: "+trigger.getDescription());
			ret.add("Last change: "+trigger.getLastchangeString());
			ret.add("Age: "+trigger.getAgeTime());
			ret.add("Priority: "+trigger.getPriorityString());
			ret.add("Ack: "+trigger.getAckString());
			ret.add("Status: "+trigger.getStatus());
			//ret.add("Type: "+trigger.gettype());
			//ret.add("Value_flags: "+trigger.getvalue_flags());
			//ret.add("Flags: "+trigger.getflags());
			ret.add("Comments: "+trigger.getComments());
			ret.add("Error: "+trigger.getError());
		
		return ret;
	}
}
