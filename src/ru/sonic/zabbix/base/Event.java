package ru.sonic.zabbix.base;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.Map;

import com.example.andzabbix.R;

/**
 * Object Representing a Zabbix Trigger
 * @author gryphius
 *
 */
public class Event extends ZabbixObject {

	private static final long serialVersionUID = 1626415952936919799L;
//	private static final String TAG = "ZabbixEvent";
//	private static final int Disaster = 5;

	public Event(Map<?, ?> m) {
		super(m);
	}

	public String getSource() {
		return (String) get("source");
	}
	
	public String getObject() {
		return (String) get("object");
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClockString());
		sb.append(" : ");
		sb.append(getValue());
		return sb.toString();
	}
		
	public String getObjectid() {
		return (String) get("objectid");
	}
	
	public String getID() {
		return (String) get("eventid");
	}
	
	public String getHost() {
			String [] Colors = new String[6];
			Colors [0] = new String ("OK");
			Colors [1] = new String ("PROBLEM");
			Colors [2] = new String ("UNKNOWN");
			String Severity = Colors[Integer.parseInt(getValue())];
		return Severity;
	}
	
	public String getDescription() {
		return getClockString();
	}
	
	public String getClock() {
		return (String) get("clock");
	}
	
	public String getClockString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm:ss"); 
		long clock = Integer.parseInt(getClock());
		Date dateClock = new Date(clock*1000);
		return dateFormat.format(dateClock).toString();
	}
	
	public String getAgeTime() {
		return getID();
	}
	
	public String getValue() {
		return (String) get("value");
	}
	
	public String getSeverity() {
		String [] Colors = new String[6];
		Colors [0] = new String ("#00EE00");
		Colors [1] = new String ("#FF6666");
		Colors [2] = new String ("#AAAAAA");
		String Severity = Colors[Integer.parseInt(getValue())];
		return Severity;
	}
	
	public int getActiveImg(){
        switch( Integer.parseInt(getValue()) ) {
          case 0:
                        return R.drawable.ok_icon;
          case 1:
                        return R.drawable.trigger_active;
        }
        return R.drawable.zabbix_unknown;
	}
	
	public String getAcknowledged() {
		return (String) get("acknowledged");
	}
	
	public int getAckImg() {
		switch(Integer.parseInt(getAcknowledged())) {
        case 0:
                      return android.R.drawable.checkbox_off_background;
        case 1:
                      return android.R.drawable.checkbox_on_background;
      }
      return android.R.drawable.checkbox_off_background;
	}
	
	/*
	public String getTriggerDesc() {
		JSONArray trigger = (JSONArray) get("triggers");
		Log.d(TAG, "trigger Aray: "+trigger);
		//JSONArray array = (JSONArray) JSONValue.parse(trigger);
		JSONObject trigger0 = (JSONObject) JSONValue.parse(trigger.get(0).toString());
		//Log.d(TAG, "Desc: "+trigger0.get("description"));
		return (String) trigger0.get("description");
	} */
}