package ru.sonic.zabbix.base;

import java.util.Map;


/**
 * Object representing a Zabbix Hostgroup
 * @author gryphius
 *
 */
public class HostGroup extends ZabbixObject {
	private static final long serialVersionUID = -2102845859667327412L;
	
	public HostGroup(Map<?, ?> m){
		super(m);
	}
	
	public String getName(){
		return (String)get("name");
	}
	
	public String getID(){
		return (String)get("groupid");
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
