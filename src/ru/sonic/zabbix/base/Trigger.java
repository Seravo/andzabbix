package ru.sonic.zabbix.base;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import ru.sonic.zabbix.R;

/**
 * Object Representing a Zabbix Trigger
 * @author gryphius
 *
 */
public class Trigger extends ZabbixObject {

	private static final long serialVersionUID = 1696664952321919799L;
//	private static final String TAG = "ZabbixTrigger";
	
	private static final int Information = 1;
	private static final int Warning = 2;
	private static final int Average = 3;
	private static final int High = 4;
	private static final int Disaster = 5;
	
	public boolean Ack;
	public String colors;
	public long timecorrection;

	public Trigger(Map<?, ?> m) {
		super(m);
	}

	public String getDescription() {
		return (String) get("description");
	}
	
	public String getActive() {
		return (String) get("value");
	}
	
	public int getActiveImg(){
        switch( Integer.parseInt(get("value").toString()) ) {
          case 0:
                        return R.drawable.ok_icon;
          case 1:
                        return R.drawable.trigger_active;
        }
        return R.drawable.ok_icon;
	}

	@Override
	public String toString() {
		return getID();
	}
		
	public String getHost() {
		return (String) get("host");
	}
	
	public String getID() {
		return (String) get("triggerid");
	}
	
	public String getSeverity() {
		String [] Colors = new String[6];
		if ( colors.equals("modern")) {
			Colors [0] = new String ("#FFFFFF");
			Colors [1] = new String ("#00EEEE");
			Colors [2] = new String ("#00EE00");
			Colors [3] = new String ("#FFFF33");
			Colors [4] = new String ("#FF66FF");
			Colors [5] = new String ("#DD0000");
		} else {
			Colors [0] = new String ("#DBDBDB");
			Colors [1] = new String ("#D6F6FF");
			Colors [2] = new String ("#FFF6A5");
			Colors [3] = new String ("#FFB689");
			Colors [4] = new String ("#FF9999");
			Colors [5] = new String ("#FF3838");
		}
		String Severity = Colors[Integer.parseInt(get("priority").toString())];
		return Severity;
	}
	
	public String getAgeTime() { 
		long timestamp = Integer.parseInt((String) getLastchangeStamp());
		return getAgeTime(timestamp);
	}
	
	public String getLastchangeStamp() {
		//Log.d(TAG, "getLastchangeStamp: "+ get("lastchange"));
		return (String) get("lastchange");
	}
	
	public String getLastchangeString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm:ss"); 
		long timestamp = Integer.parseInt(getLastchangeStamp());
		Date lastchange = new Date(timestamp*1000);
		return dateFormat.format(lastchange).toString();
	}
	
	public String getIssueStatus() {
		String IssueStatus = "";
		if (Integer.parseInt(getActive()) == 1) { 
			IssueStatus = "Trigger  Active!!!";
		} else {
			IssueStatus = "Not Active";}
		return IssueStatus;
	}
	
	public String getPriorityString() {
		String priority = "";
		switch (Integer.parseInt(get("priority").toString())) {
			case Information:
				priority = "Information";
				break;
			case  Warning:
				priority = "Warning";
				break;
			case Average:
				priority = "Warning";
				break;
			case High:
				priority = "High";
				break;
			case Disaster:
				priority = "Disaster";
				break;
			default:
				priority =  "none";
		}
		return priority;
	}
	
	public String getStatus() {
		switch(Integer.parseInt((String) get("status"))) {
        case 0:
                      return "Enabled";
        case 1:
                      return "Disabled";
      }
      return "Unknown";
	}
	
	public String gettype() {
		return (String) get("type");
	}
	public String getvalue_flags() {
		return (String) get("value_flags");
	}
	public String getflags() {
		return (String) get("flags");
	}
	public String getComments() {
		return (String) get("comments");
	}
	public String getError() {
		return (String) get("error");
	}
	
	/**
	 * get Age time as String
	 * @return
	 */
	private String getAgeTime(long lastchange) {
		Date now = new Date();
		long diffTime = Math.abs(now.getTime()/1000 + timecorrection - lastchange);
		//Log.d(TAG, "diffTime: "+diffTime);
		StringBuffer sb = new StringBuffer();
		
		long months = diffTime/2592000;
		if ( months >= 1 ) { sb.append(months+"M "); diffTime = (diffTime % 2592000);}
		
		long weeks = diffTime/604800;
		if ( weeks >= 1 ) { sb.append(weeks+"w "); diffTime = (diffTime % 604800);}
		
		long days = diffTime/86400;
		if ( days >= 1 ) { sb.append(days+"d "); diffTime = (diffTime % 86400);}
		
		long hours = diffTime/3600;
		if ( hours >= 1 ) { sb.append(hours+"h "); diffTime = (diffTime % 3600);}
		
		long mins = diffTime/60;
		if ( mins >= 1 ) { sb.append(mins+"m "); diffTime = (diffTime % 60);}
		
		long Sec = diffTime;
		sb.append(Sec+"s");
		return sb.toString();
	}
	
	public boolean getAck() {
		return Ack;
	}
	
	public String getAckString() {
		if(getAck()) {
            return "Acknowledged";
		} else {
			return "Unacknowledged";
		}
	}
	
	public int getAckImg() {
		if(getAck()) {
			return android.R.drawable.checkbox_off_background;
		} else {
            return android.R.drawable.checkbox_on_background;
		}
	}
	
	public String getHostID() {
		return (String) get("hostid");
	}
	
	public String getExpression() {
		return (String) get("expression");
	}
}