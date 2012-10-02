package ru.sonic.zabbix.base;

import java.util.Map;
import org.json.simple.JSONObject;

/**
 * base class for all zabbix objects (hosts, items, triggers, etc)
 * @author gryphius
 *
 */
public class ZabbixObject extends JSONObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4027800885260370007L;


	public ZabbixObject() {
		super();
	}

	public ZabbixObject(Map<?, ?> map) {
		super(map);
	}
}
