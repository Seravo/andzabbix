package ru.sonic.zabbix.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.util.Log;

/**
 * Object representing a Zabbix Item
 * @author gryphius
 *
 */
public class Item extends ZabbixObject {
	protected static String TAG = "ZabbixItem";
	/**
	 * 
	 */
	private static final long serialVersionUID = -2100624850747391912L;

	public Item(Map<?, ?> m){
		super(m);
	}
	
	public Object lastValue(){
		return get("lastvalue");
	}
	
	public String getDescription(){
		return (String)get("description");
	}
	
	public String getName(){
		return (String)get("name");
	}
	
	public String getStatus(){
		return (String)get("status");
	}
	
	public String getValueType(){
		return (String)get("value_type");
	}
	
	public String getKey(){
		return (String)get("key_");
	}
	
	@Override
	public String toString() {
		if (getDescription().equals(""))
			return getName();
		return getDescription();
	}
	
	public String getExpanedDescription(){
		String desc = "";
		if (getDescription().length()>0) {
			desc = getDescription();
		} else if (getName().length()>0) {
			desc = getName();
		}
		Log.d(TAG, "getExpanedDescription: "+desc);
		String key = getKey();
		String expDesc = "";
		if (desc.indexOf("$1") > 0) {
			Log.d(TAG, "Description: " + desc);
			String param1 = key.substring(key.indexOf("["), key.lastIndexOf("]"));
			String [] params = param1.substring(1, param1.length()).split(",");
			List<String> para = new ArrayList<String>();
			  for (String strings : params) {
		            para.add(strings);
		        }
			expDesc = desc.replace("$1", para.get(0).toString());
		} else {
			expDesc = desc;
		}
		Log.d(TAG, expDesc);
		return expDesc;
	}
}
