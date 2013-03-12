package ru.sonic.zabbix;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import ru.sonic.zabbix.base.Host;
import ru.sonic.zabbix.base.ZabbixAPIException;
import android.os.Bundle;

/**
 * display active triggers in a listview
 * @author gryphius
 *
 */
public class HostInfoActivity extends DefaultZabbixListActivity {
	
	@Override
	protected List getData() throws ZabbixAPIException {
		Bundle extras  = getIntent().getExtras();
		String hostId  = extras.getString("hostid");
		
		List HostInfo = api.getHostInfo(hostId);
		Host host = (Host) HostInfo.get(0);
		
		ArrayList<String> ret = new ArrayList<String>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm:ss");
			
			ret.add("Host: "+host.getName());
			ret.add("Available: "+host.getAvailableString());
			ret.add("DNS name: "+host.getdns());
			ret.add("IP address: "+host.getip());
			ret.add("Port: "+host.getport());
			ret.add("Status: "+host.getStatusString());
			ret.add("Error: "+host.geterror());
			ret.add("SNMP status: "+host.getSnmpAvailableString());
			ret.add("SNMP error: "+host.getsnmp_error());
			//ret.add("Disable until: "+dateFormat.format(host.getdisable_until()));
			//ret.add("Disable until: "+host.getdisable_until());
			ret.add("IPMI status: "+host.getIPMIAvailableString());
			ret.add("IPMI error: "+host.getIPMIerror());
			//ret.add("JMX error: "+host.getJMXerror());
		return ret;
	}
}
