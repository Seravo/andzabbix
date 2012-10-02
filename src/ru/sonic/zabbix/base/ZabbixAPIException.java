package ru.sonic.zabbix.base;

/**
 * Exeption thrown by Code dealing with the Zabbix API (Login failures, network errors etc)
 * @author gryphius
 *
 */
public class ZabbixAPIException extends Exception {
 /**
	 * 
	 */
	private static final long serialVersionUID = -1633513964299323021L;

public ZabbixAPIException(String description){
	 super(description);
 }
}
