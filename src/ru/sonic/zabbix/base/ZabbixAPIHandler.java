package ru.sonic.zabbix.base;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Zabbix API Main Object. All other classes can retrieve data via this class.
 * 
 * @author gryphius
 *
 */
public class ZabbixAPIHandler {
	//which activity do we belong to? required for error popups and stuff
	private Activity owner = null;

	//auth token returned by zabbix server
	public String auth = "";
	
	// current context
	private Context ctx = null;
	
	// Sserver url
	public String serverUrl = "";
	
	// DB keys
    public static final int KEY_SERVER = 0;
    public static final int KEY_URL = 1;
    public static final int KEY_USER = 2;
    public static final int KEY_PASS = 3;
    public static final int KEY_TIMEOUT = 4;
    public static final int KEY_BASE_USE = 5;
    public static final int KEY_BASE_LOGIN = 6;
    public static final int KEY_BASE_PASS = 7;
	
	//id counter
	private int id = 0;

	//logging tag
	private static final String TAG = "ZabbixAPIHandler";

	/**
	 * create an api handler
	 * @param owner Activity this api handler belongs to
	 */
	public ZabbixAPIHandler(Activity owner) {
		this.owner = owner;
		this.ctx = owner.getApplicationContext();
	}

	public ZabbixAPIHandler(Context context) { 
		this.ctx = context;
	}

	/**
	 * @return the API Username
	 */
	public String getUsername() throws ZabbixAPIException {
		return getStringPref(KEY_USER);
	}
	
	/**
	 * @return the API Limit Active Trigger
	 */
	public String getTimeOut() throws ZabbixAPIException {
		return getStringPref(KEY_TIMEOUT);
	}

	/*** 
	 * @return the API Password
	 */
	public String getPassword() throws ZabbixAPIException {
		return getStringPref(KEY_PASS);
	}
	
	/*** 
	 * @return the API use base autentification
	 */
	public String get_base_use() throws ZabbixAPIException {
		return getStringPref(KEY_BASE_USE);
	}
	
	/*** 
	 * @return the API use base autentification login
	 */
	public String get_base_login() throws ZabbixAPIException {
		return getStringPref(KEY_BASE_LOGIN);
	}

	/*** 
	 * @return the API use base autentification password
	 */
	public String get_base_pass() throws ZabbixAPIException {
		return getStringPref(KEY_BASE_PASS);
	}
	
	/*** 
	 * @return the API URL
	 */
	public String getAPIURL() throws ZabbixAPIException {
		//String url = getStringPref("api_url","http://example.com/zabbix/api_jsonrpc.php");
		return getStringPref(KEY_URL);
	}

	/**
	 * Retrieve Application Preference
	 * @param key
	 * @param defaultvalue
	 * @return
	 */
	public String getStringPref(int key) throws ZabbixAPIException {
		String server = getCurrentServer();
		//Log.d(TAG, "Server: "+ server);
		if ( server == "Not selected") throw new ZabbixAPIException("Please select server");
		DBAdapter db = new DBAdapter(this.ctx);
		if (db.open()!=null) {
			List<String> credentials = db.selectServer(getCurrentServer());
			db.close();
			if (credentials.size() == 0) {
				throw new ZabbixAPIException("Please enter user name and password for server "+server);
			}
			//Log.d(TAG, "Credential: "+ credentials.get(key).toString());
			return credentials.get(key).toString();
		} else {
			throw new ZabbixAPIException("Unknown database error");
		}
	}
	
	/**
	 * Retrieve Application Preference
	 * @param key
	 * @param defaultvalue
	 * @return
	 */
	public String getServerUrlForWidget(String server_name) throws ZabbixAPIException {
		DBAdapter db = new DBAdapter(this.ctx);
		db.open();
		String serverUrl = db.getUrl(server_name);
		db.close();
		//if (serverUrl.length() == 0) throw new ZabbixAPIException("Please select server");
		//Log.d(TAG, "serverUrl: "+ serverUrl);
		return serverUrl;
	}
	
	public String getLoginForWidget(String server_name) throws ZabbixAPIException {
		DBAdapter db = new DBAdapter(this.ctx);
		db.open();
		String login = db.getLogin(server_name);
		db.close();
		//if (login.length() == 0) throw new ZabbixAPIException("No user");
		//Log.d(TAG, "login: "+ login);
		return login;
	}
	
	public String getPassForWidget(String server_name) throws ZabbixAPIException {
		DBAdapter db = new DBAdapter(this.ctx);
		db.open();
		String pass = db.getPassword(server_name);
		db.close();
		//if (pass.length() == 0) throw new ZabbixAPIException("No pass");
		//Log.d(TAG, "pass: "+ pass);
		return pass;
	}
	
	public void setFirstServer() throws ZabbixAPIException {
		DBAdapter db = new DBAdapter(this.ctx);
		db.open();
		List<String> servers = db.selectServerNames();
		if (servers.size()>0) {
			setCurrentServer(servers.get(0).toString());
		} else {
			SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(this.ctx);
	        SharedPreferences.Editor configEditor = config.edit();
	        configEditor.putBoolean("firstStart",true);
	        configEditor.commit();
		}
		db.close();
	}
	
	/**
	 * get current server
	 * @param 
	 */
	public String getCurrentServer() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.ctx);
		return prefs.getString("currentServer","Not selected");
	}
	
	/**
	 * set current server
	 * @param 
	 */
	public void setCurrentServer(String serverName) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.ctx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("currentServer", serverName);
        editor.commit();
	}

	/**
	 * if we don't have an auth token yet, log in
	 * @throws ZabbixAPIException
	 */
	public void verifyLogin() throws ZabbixAPIException {
		if (auth == null || auth == "") {
				login(getUsername(), getPassword()); 
		}
	}
	
	public void verifyLoginWidget(String server) throws ZabbixAPIException {
		if (auth == null || auth == "") {
				login(getLoginForWidget(server), getPassForWidget(server)); 
		}
	}

	/**
	 * call an API function that returns a list of objects (items, triggers, ...)
	 * @param method name of the function to call, eg. trigger.get
	 * @param params params as defined by the zabbix api
	 * @return a JSONArray with the returned objects
	 * @throws ZabbixAPIException
	 */
	@SuppressWarnings("unchecked")
	public JSONArray requestObjects(String method,
			Hashtable<Object, Object> params) throws ZabbixAPIException {
		//Log.d(TAG, "requestObjects method: " + method);
		serverUrl = getAPIURL();
		verifyLogin();

		JSONObject methodcall = makeRPCObject(method, params);
		//Log.d(TAG, "requestObjects. Method: " + methodcall.toString());
		String ret = post(methodcall.toString());

		JSONObject reply = parseObj(ret);
		if (reply.containsKey("error")) {
			JSONObject err = parseObj(reply.get("error").toString());
			String errmsg = err.get("data").toString();
			throw new ZabbixAPIException(errmsg);
		}

		if (method.equals("trigger.update") | method.equals("event.acknowledge") | method.equals("event.delete") | method.equals("host.update")) {
			JSONArray result = new JSONArray();
			JSONObject json = new JSONObject();
            result.add(json);
			return result;
		} else {
			String res = reply.get("result").toString();
			//Log.d(TAG, "requestObjects. res: " + res);
			return parseArray(res);
		}
	}
	
	/**
	 * call an API function that returns a list of objects (items, triggers, ...)
	 * @param method name of the function to call, eg. trigger.get
	 * @param params params as defined by the zabbix api
	 * @return a JSONArray with the returned objects
	 * @throws ZabbixAPIException
	 */
	@SuppressWarnings("unchecked")
	public JSONArray requestObjectCount(String servername, String method,
			Hashtable<Object, Object> params) throws ZabbixAPIException {
		//Log.d(TAG, "requestObjects method: " + method);
		serverUrl = getServerUrlForWidget(servername);
		verifyLoginWidget(servername);

		JSONObject methodcall = makeRPCObject(method, params);
		String ret = post(methodcall.toString());

		JSONObject reply = parseObj(ret);
		if (reply.containsKey("error")) {
			JSONObject err = parseObj(reply.get("error").toString());
			String errmsg = err.get("data").toString();
			throw new ZabbixAPIException(errmsg);
		}
		
		if (method.equals("trigger.update") | method.equals("event.acknowledge") | method.equals("event.delete")) {
			JSONArray result = new JSONArray();
			JSONObject json = new JSONObject();
            result.add(json);
			return result;
		} else {
			String res = reply.get("result").toString();
			//Log.e(TAG, "requestObjects. res: " + res);
			return parseArray(res);
		}
	}
	
	@SuppressWarnings("unchecked")
	private JSONArray deleteMethod(String method, String objectId) throws ZabbixAPIException {
		//Log.d(TAG, "Request deleteMethod: " + method);
		verifyLogin();
		JSONObject methodcall = new JSONObject();
		methodcall.put("jsonrpc", "2.0");
		methodcall.put("method", method);
		methodcall.put("auth", auth);
		
		JSONArray params = new JSONArray();;
		params.add(objectId);
        
		methodcall.put("params", params);
		//Log.d(TAG, "Method for makeRPCObject: "+method);
		//Log.d(TAG, "Paramf for makeRPCObject: "+params);

		id++;
		methodcall.put("id", "" + id);
		
		String ret = post(methodcall.toString());
		//Log.d(TAG, "Return delete trigger: "+ret);
		JSONObject reply = parseObj(ret);
		if (reply.containsKey("error")) {
			JSONObject err = parseObj(reply.get("error").toString());
			String errmsg = err.get("data").toString();
			throw new ZabbixAPIException(errmsg);
		}
		
		JSONArray result = new JSONArray();
		return result;
	}

	/**
	 * get all hostgroups
	 * @return
	 * @throws ZabbixAPIException
	 */
	public List<HostGroup> getHostGroups() throws ZabbixAPIException {
		Hashtable<Object, Object> params = new Hashtable<Object, Object>();
		params.put("output", "extend");
		JSONArray jsonobjects = requestObjects("hostgroup.get", params);
		
		ArrayList<HostGroup> ret = new ArrayList<HostGroup>();
		for (Iterator<JSONObject> it = jsonobjects.iterator(); it.hasNext();) {
			JSONObject obj = it.next();
			ret.add(new HostGroup(obj));
		}
		//Log.d(TAG, "getHostGroups result: "+ret);
		return ret;
	}
	
	/**
	 * get all items for a given host
	 * @param hostid
	 * @return
	 * @throws ZabbixAPIException
	 */
	public List<Item> getItems(String hostid) throws ZabbixAPIException{
		Hashtable<Object, Object> params = new Hashtable<Object, Object>();
		JSONArray hostids=new JSONArray();
		hostids.add(hostid);
		params.put("hostids", hostids);
		params.put("output", "extend");
		JSONArray jsonobjects = requestObjects("item.get", params);
		
		ArrayList<Item> ret = new ArrayList<Item>();
		for (Iterator<JSONObject> it = jsonobjects.iterator(); it.hasNext();) {
			JSONObject obj = it.next();
			ret.add(new Item(obj));
		}
		return ret;
	}
	
	/**
	 * 
	 * @param hostid
	 * @return
	 * @throws ZabbixAPIException
	 */
	public List<Event> getEvent(String triggerId) throws ZabbixAPIException{
		Hashtable<Object, Object> params = new Hashtable<Object, Object>();
		JSONArray triggerids=new JSONArray();
		if (!triggerId.equals("0")) { 
			triggerids.add(triggerId);
			params.put("triggerids", triggerids);
		}
		params.put("output", "extend");
		params.put("select_triggers", "extend");
		params.put("sortfield","eventid");
		params.put("sortorder","DESC");
		params.put("limit",60);
		JSONArray jsonobjects = requestObjects("event.get", params);
		
		ArrayList<Event> ret = new ArrayList<Event>();
		for (Iterator<JSONObject> it = jsonobjects.iterator(); it.hasNext();) {
			JSONObject obj = it.next();
			ret.add(new Event(obj));
		}
		return ret;
	}
	
	/**
	 * get all hosts
	 * @return
	 * @throws ZabbixAPIException
	 */
	public List<Host> getHosts(String groupID) throws ZabbixAPIException{
		
		Hashtable<Object, Object> params = new Hashtable<Object, Object>();
		params.put("output", "extend");
		//Log.d(TAG, "getHosts!!!! ");
		if (groupID.length() != 0) {params.put("groupids", groupID);}

		ArrayList<Host> ret = new ArrayList<Host>();
		JSONArray jsonobjects = requestObjects("host.get", params);	
		//Log.d(TAG, "getHosts. ret: " + ret);
		
		for (Iterator<JSONObject> it = jsonobjects.iterator(); it.hasNext();) {
			JSONObject obj = it.next();
			ret.add(new Host(obj));
		}
		//Log.d(TAG, "getHosts. ret: " + ret);
		return ret;
	}
	
	/**
	 * get all discovered hosts
	 * @return
	 * @throws ZabbixAPIException
	 */
	public List<DHost> getDHosts() throws ZabbixAPIException{
		Hashtable<Object, Object> params = new Hashtable<Object, Object>();
		params.put("output", "extend");
		//params.put("selectHosts","extend");
		//Log.d(TAG, "getDHosts!!!! ");
		//if (groupID.length() != 0) {params.put("groupids", groupID);}

		ArrayList<DHost> ret = new ArrayList<DHost>();
		JSONArray jsonobjects = requestObjects("dhost.get", params);	
		//Log.d(TAG, "getDHosts. ret: " + ret);
		for (Iterator<JSONObject> it = jsonobjects.iterator(); it.hasNext();) {
			JSONObject obj = it.next();
			ret.add(new DHost(obj));
		}
		//Log.d(TAG, "getDHosts. ret: " + ret);
		return ret;
	}
	
	/**
	 * get all hosts with graphs
	 * @return
	 * @throws ZabbixAPIException
	 */
	public List<Host> getGHostsWithGraphs() throws ZabbixAPIException{
		Hashtable<Object, Object> params = new Hashtable<Object, Object>();
		params.put("output", "extend");
		params.put("with_graphs", "1");

		ArrayList<Host> ret = new ArrayList<Host>();
		JSONArray jsonobjects = requestObjects("host.get", params);
		
		for (Iterator<JSONObject> it = jsonobjects.iterator(); it.hasNext();) {
			JSONObject obj = it.next();
			ret.add(new Host(obj));
		}
		return ret;
	}

	/**
	 * get all active triggers
	 * @return
	 * @throws ZabbixAPIException
	 */
	public List<Trigger> getActiveTriggers(boolean unask) throws ZabbixAPIException {
		List<Trigger> ret = new ArrayList<Trigger>();
		
		Hashtable<Object, Object> params = new Hashtable<Object, Object>();
		
		int trigger_check_type = check_type();
		
		if (get_trigger_filter() == 3) {
			params.put("monitored","1");
		} else if (get_trigger_filter() == 2) {
			params.put("active","1");
		} else if (get_trigger_filter() == 0) {
			trigger_check_type = 0;
		}
		
		if (trigger_check_type == 1) {
			params.put("only_true", "yes");
		} else if (trigger_check_type == 2) {
			float time = getTimeActiveTriggersCheck();
			Date now = new Date();
			String From = (now.getTime()/1000-time*60)+"";
			String to = (now.getTime()/1000)+"";
			params.put("lastChangeSince", From);
			params.put("lastChangeTill", to);
		} else if (trigger_check_type == 3) {
			Hashtable<Object, Object> filter = new Hashtable<Object, Object>();
				filter.put("status", "0");
				filter.put("value", "1");
			params.put("filter", filter);
		}

		if (!old_zbx_fix())
			params.put("expandDescription","yes");
//		params.put("select_items","extend");
		params.put("output", "extend");
		params.put("expandData","hostid");
		params.put("sortfield","priority");
		params.put("sortorder","DESC");
		params.put("limit","1000");
		if (unask) params.put("withUnacknowledgedEvents",1); 
		else params.put("withAcknowledgedEvents",1);

		JSONArray jsonobjects = requestObjects("trigger.get", params);
		
		String colors = "";
		if (use_modern_colors()) {
			colors = "modern";
		} else {
			colors = "default";
		}
		
		long timecorection = getTimeCorrection();
		
		for (Iterator<JSONObject> it = jsonobjects.iterator(); it.hasNext();) {
			JSONObject obj = it.next();
			
			if (obj.containsKey("ClassCastException") | obj.containsKey("NullPointerException") | obj.containsKey("ParseException")) {
				throw new ZabbixAPIException("Can't parsing response. Try configure app for use older version of Zabbix.");
			}

			Trigger trigger = new Trigger(obj);
			trigger.Ack = unask;
			trigger.colors = colors;
			trigger.timecorrection = timecorection;
			if (trigger.get("status").toString().equals("0"))     // enabled
				ret.add(trigger);
			else if (showdisabled())
				ret.add(trigger);
		}
		//Log.d(TAG, "Ret info: "+ret);
		return ret;
	}
	
	/**
	 * get correction timezone
	 * @return
	 * @throws ZabbixAPIException
	 */
	private long getTimeCorrection() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		String tc = "0";
		if (prefs.getBoolean("use_timecorrection",false))
			tc = prefs.getString("time_correction","0");
        return Integer.parseInt(tc);
	}

	public boolean use_modern_colors(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("use_modern_colors",false);
	}
	
	public boolean timecorr(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("use_timecorrection",false);
	}
	
	public int check_type(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		int type = Integer.parseInt(prefs.getString("check_type","1"));
        return type;
	}
	
	public float getTimeActiveTriggersCheck(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return Float.parseFloat(prefs.getString("activetriggertime","0"));
	}
	
	public int get_trigger_filter(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		int filter_type = Integer.parseInt(prefs.getString("trigger_filter","3"));
        return filter_type;
	}
	
	public boolean showdisabled(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("showdisabled",false);
	}
	
	public boolean old_zbx_fix(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("old_zbx_fix",false);
	}
	
	/**
	 * get all active triggers count
	 * @return
	 * @throws ZabbixAPIException
	 */
	public List<Trigger> getActiveTriggersCount(String server_name) throws ZabbixAPIException {
		//Log.d(TAG, "Request: Active Triggers count server:"+server_name);
		Hashtable<Object, Object> params = new Hashtable<Object, Object>();
		params.put("output", "extend");
		params.put("monitored", "1");
		params.put("only_true", "yes");
		JSONArray jsonobjects = requestObjectCount(server_name,"trigger.get", params);
		List<Trigger> ret = new ArrayList<Trigger>();
		for (Iterator<JSONObject> it = jsonobjects.iterator(); it.hasNext();) {
			JSONObject obj = it.next();
			ret.add(new Trigger(obj));
		}
		return ret;
	}
	
	/**
	 * get all active triggers count for service
	 * @return
	 * @throws ZabbixAPIException
	 */
	public List<Trigger> getActiveTriggersCountService(float interval) throws ZabbixAPIException {
		//Log.d(TAG, "Request: Active Triggers count server:"+server_name);
		DBAdapter db = new DBAdapter(ctx);
  		db.open();
  		List<String> server = db.selectServerNames();
	  	db.close();
		Date now = new Date();
		String From = (now.getTime()/1000-interval*60)+"";
		String to = (now.getTime()/1000)+"";
	  	
		Hashtable<Object, Object> params = new Hashtable<Object, Object>();
		params.put("output", "extend");
		params.put("monitored", "1");
		params.put("lastChangeSince", From);
		params.put("lastChangeTill", to);
		params.put("expandData","hostid");
		if (!old_zbx_fix())
			params.put("expandDescription","yes");
		Hashtable<Object, Object> filter = new Hashtable<Object, Object>();
			filter.put("status", "0");
			filter.put("value", "1");
		params.put("filter", filter);
		
		List<Trigger> ret = new ArrayList<Trigger>();
		for (String string: server) {
			List<Trigger> triggers = new ArrayList<Trigger>();
			try {
				JSONArray jsonobjects = requestObjectCount(string,"trigger.get", params);
				for (Iterator<JSONObject> it = jsonobjects.iterator(); it.hasNext();) {
					JSONObject obj = it.next();
					triggers.add(new Trigger(obj));
				}
			} catch (ZabbixAPIException e) {
				
			}
			ret.addAll(triggers);	
		}
		return ret;
	}
	
	/**
	 * Disabling trigger
	 * @param triggerId
	 * @return
	 * @throws ZabbixAPIException
	 */
	public void disableTrigger(String triggerId) throws ZabbixAPIException {
		//Log.d(TAG, "Request: disableTrigger");
		Hashtable<Object, Object> params = new Hashtable<Object, Object>();
		params.put("triggerid", triggerId);
		params.put("status", 1);
		requestObjects("trigger.update", params);
	}
	
	/**
	 * Activating trigger
	 * @param triggerId
	 * @return
	 * @throws ZabbixAPIException
	 */
	public void activateTrigger(String triggerId) throws ZabbixAPIException {
		//Log.d(TAG, "Request: disableTrigger");
		Hashtable<Object, Object> params = new Hashtable<Object, Object>();
		params.put("triggerid", triggerId);
		params.put("status", 0);
		requestObjects("trigger.update", params);
	}
	
	/**
	 * Acknowledge trigger
	 * @param triggerId
	 * @return
	 * @throws ZabbixAPIException
	 */
	public void ackEvent(String eventId, String message) throws ZabbixAPIException {
		Hashtable<Object, Object> params = new Hashtable<Object, Object>();
		params.put("eventids", eventId);
		params.put("message", message);
		requestObjects("event.acknowledge", params);
	}
	
	/**
	 * Activating host
	 * @param triggerId
	 * @return
	 * @throws ZabbixAPIException
	 */
	public void activateHost(String hostID) throws ZabbixAPIException {
		//Log.d(TAG, "Request: disableTrigger");
		Hashtable<Object, Object> params = new Hashtable<Object, Object>();
		params.put("hostid", hostID);
		params.put("status", "0");
		requestObjects("host.update", params);
	}
	
	/**
	 * Disabling host
	 * @param triggerId
	 * @return
	 * @throws ZabbixAPIException
	 */
	public void disableHost(String hostID) throws ZabbixAPIException {
		//Log.d(TAG, "Request: disableTrigger");
		Hashtable<Object, Object> params = new Hashtable<Object, Object>();
		params.put("hostid", hostID);
		params.put("status", "1");
		requestObjects("host.update", params);
	}
	
	public void delEvent(String eventId) throws ZabbixAPIException {
		deleteMethod("event.delete", eventId);
	}
	
	public void deleteTrigger(String triggerID) throws ZabbixAPIException {
		deleteMethod("trigger.delete", triggerID);
	}

	/**
	 * get all info about triggers
	 * @return
	 * @throws ZabbixAPIException
	 */
	public List<Trigger> getActiveTriggerInfo(String triggerid) throws ZabbixAPIException {
		//String apiVersion = getApiVersion();
		Hashtable<Object, Object> params = new Hashtable<Object, Object>();
		params.put("output", "extend");
		params.put("expandData","host");
		params.put("expandDescription","yes");
		params.put("triggerids", triggerid);
		
		JSONArray jsonobjects = requestObjects("trigger.get", params);
		ArrayList<Trigger> ret = new ArrayList<Trigger>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm:ss");  
		for (Iterator<JSONObject> it = jsonobjects.iterator(); it.hasNext();) {
			JSONObject obj = it.next();
			ret.add(new Trigger(obj));
		}
		return ret;
	}

	/**
	 * get all graphs
	 * @return
	 * @throws ZabbixAPIException
	 */
	public List<Graph> getGraphs(String hostid) throws ZabbixAPIException{
		Hashtable<Object, Object> params = new Hashtable<Object, Object>();
		params.put("output", "extend");
		params.put("hostids", hostid);
		JSONArray jsonobjects = requestObjects("graph.get", params);
		
		ArrayList<Graph> ret = new ArrayList<Graph>();
		for (Iterator<JSONObject> it = jsonobjects.iterator(); it.hasNext();) {
			JSONObject obj = it.next();
			ret.add(new Graph(obj));
		}
		//Log.d(TAG, "Ret getGraphs: "+ret);
		return ret;
	}
	
	public List<Host> getHostInfo(String hostId) throws ZabbixAPIException {
		Hashtable<Object, Object> params = new Hashtable<Object, Object>();
		params.put("output", "extend");
		if (hostId.length() != 0) 
			{params.put("hostids", hostId);}
		ArrayList<Host> ret = new ArrayList<Host>();
		JSONArray jsonobjects = requestObjects("host.get", params);
		for (Iterator<JSONObject> it = jsonobjects.iterator(); it.hasNext();) {
			JSONObject obj = it.next();
			ret.add(new Host(obj));
			}
		return ret;
	}
	
	public Bitmap getGraphImage(String imageurl) throws ZabbixAPIException, UnsupportedEncodingException, IOException {
		serverUrl = getAPIURL();
		verifyLogin();
		URL url = new URL(imageurl);
		//Log.d(TAG,"Auth string: "+auth.toString());
		HttpURLConnection connection  = (HttpURLConnection) url.openConnection();
		connection.addRequestProperty("Cookie", "zbx_sessionid="+auth);
		InputStream is = connection.getInputStream();
		return BitmapFactory.decodeStream(is); 
	}
	
	/**
	 * generate a json rpc methodcall
	 * @param method name of the method to call
	 * @param params method params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public  JSONObject makeRPCObject(String method,
			Hashtable<Object, Object> params) {
		JSONObject methodcall = new JSONObject();
		methodcall.put("jsonrpc", "2.0");
		methodcall.put("method", method);

		JSONObject jparams = new JSONObject();

		for (Enumeration<Object> en = params.keys(); en.hasMoreElements();) {
			Object key = en.nextElement();
			Object value = params.get(key);
			jparams.put(key, value);
		}

		methodcall.put("auth", auth);
		methodcall.put("params", params);
		//Log.d(TAG, "Method for makeRPCObject: "+method);
		//Log.d(TAG, "Paramf for makeRPCObject: "+params);

		id++;
		methodcall.put("id", "" + id);

		return methodcall;
	}

	
	/**
	 * generate Api version
	 * @param method name of the method to call
	 * @param params method params
	 * @return
	 */
	
	@SuppressWarnings("unchecked")
	public String getApiVersion() throws ZabbixAPIException {
		serverUrl = getAPIURL();
		verifyLogin();
		JSONObject version = new JSONObject();
		version.put("jsonrpc", "2.0");
		version.put("method", "apiinfo.version");
		version.put("auth", auth);
		id++;
		version.put("id", "" + id);

		String ret = post(version.toString());
		JSONObject reply = parseObj(ret);
		if (reply.containsKey("error")) {
			JSONObject err = parseObj(reply.get("error").toString());
			String errmsg = err.get("data").toString();
			throw new ZabbixAPIException(errmsg);
		}
		return reply.get("result").toString();
	}
	
	/**
	 * parses a string into a single JSONObject
	 * @param s
	 * @return
	 */
	public JSONObject parseObj(String s) {
		Object obj = JSONValue.parse(s);
		return (JSONObject) obj;
	}

	/**
	 * parses a string into a JSONArray
	 * @param s
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONArray parseArray(String s) {
		//JSONArray obj = (JSONArray) JSONValue.parse(s);
		JSONArray obj = null;
		try {
			obj = (JSONArray) JSONValue.parseWithException(s);
		} catch (ClassCastException e) {
			//e.printStackTrace();
			JSONObject json = new JSONObject();
			json.put("ClassCastException", e);
            //Log.e(TAG, "Got ClassCastException: "+e);
            JSONArray ret = new JSONArray();
            ret.add(json);
            return ret;
		} catch (ParseException e) {
			JSONObject json = new JSONObject();
			json.put("ParseException", e);
            //Log.e(TAG, "Got ClassCastException: "+e);
            JSONArray ret = new JSONArray();
            ret.add(json);
            return ret;
			//Log.e(TAG, "Got ParseException: "+e);
		} catch (NullPointerException e) {
			JSONObject json = new JSONObject();
			json.put("NullPointerException", e);
            //Log.e(TAG, "Got ClassCastException: "+e);
            JSONArray ret = new JSONArray();
            ret.add(json);
            return ret;
			//Log.e(TAG, "Got NullPointerException: "+e);
		}
		return obj;
	}

	/**
	 * login to the zabbix server and retrieve auth token
	 * @param username
	 * @param password
	 * @throws ZabbixAPIException
	 */
	public void login(String username, String password) throws ZabbixAPIException {
		Hashtable<Object, Object> params = new Hashtable<Object, Object>();
		//Log.d(TAG, "Logging in, user=" + username + " password=" + password);
		if (username.length() == 0 | password.length() ==0) {
			throw new ZabbixAPIException("You can not use empty username or password");
		}
		params.put("user", username);
		params.put("password", password);
					
		JSONObject methodcall = makeRPCObject("user.authenticate", params);
		//Log.d(TAG, "Parsing JSONObject !!!!!Methodcall: \n"+methodcall);

		String ret = post(methodcall.toString());
		//Log.d(TAG, "Parsing JSONObject !!!!!Ret: \n"+ret);

		JSONObject reply = parseObj(ret); 
			
		boolean iferror = false;
		try {
			iferror = reply.containsKey("error");
		} catch (NullPointerException e) {
			throw new ZabbixAPIException("Can't login. Something wrong with Zabbix server or incorrect URL.");
		}
		
		if (iferror) {
			JSONObject err = parseObj(reply.get("error").toString());
			String errmsg = err.get("data").toString();
			throw new ZabbixAPIException(errmsg);
		}
				
		String auth = reply.get("result").toString();
		this.auth = auth;
		//Log.d(TAG, "Log success, got token:" + auth);
	}
 
	/**
	 * post to the zabbix api url and retrieve result
	 * @param postcontent
	 * @return
	 * @throws ZabbixAPIException
	 * @throws KeyManagementException 
	 * @throws NoSuchAlgorithmException 
	 */
	public String post(String postcontent) throws ZabbixAPIException {
		try {
			SSLContext sc;
			sc = SSLContext.getInstance("TLS");
			sc.init(null, new TrustManager[] { new MyTrustManager() }, new SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	        HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());    	

			URL zabbixserverurl = new URL(serverUrl);
			//Log.d(TAG, "Opening connection to " + zabbixserverurl);
			URLConnection zabbixservercon = zabbixserverurl.openConnection();
			
			if (get_base_use().equals("true")) {
				String base_string = get_base_login()+":"+get_base_pass();
				zabbixservercon.setRequestProperty("Authorization", "basic " + Base64.encodeBytes(base_string.getBytes()));
			}
				
			zabbixservercon.setDoOutput(true);
			
			if (zabbixservercon instanceof HttpURLConnection) {
					((HttpURLConnection) zabbixservercon).setRequestMethod("POST");
				} else if (zabbixservercon instanceof HttpsURLConnection) {
					((HttpsURLConnection) zabbixservercon).setRequestMethod("POST");	
				}
			zabbixservercon.setRequestProperty("Content-Type", "application/json-rpc");
			zabbixservercon.setConnectTimeout(Integer.parseInt(getTimeOut())*1000);
			zabbixservercon.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; U; Android 2.3.4; ru-ru; Nexus One Build/GRJ22) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
			OutputStreamWriter out = new OutputStreamWriter(zabbixservercon.getOutputStream());
			out.write(postcontent);
			out.close();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(zabbixservercon.getInputStream()));
			StringBuffer sb = new StringBuffer();
			String s = null;
			while ((s = in.readLine()) != null) {
				sb.append(s);
				sb.append("\n");
			}
			in.close();
				if (sb.length() == 0) {throw new ZabbixAPIException("Connection problem.\nPlease select server\nor check URL");}
			Log.d(TAG, "Post Rsponse: " + sb);
			return sb.toString();
			
		} catch (FileNotFoundException f) {
			throw new ZabbixAPIException("Wrong API URL - check settings");
		} catch (MalformedURLException m) {
			throw new ZabbixAPIException("Please check API URL");
		} catch (KeyManagementException e) {
			e.printStackTrace();
			return e.toString();
		} catch (NoSuchAlgorithmException e) {
			 e.printStackTrace();
			 return e.toString();
		} catch (IOException e) {
			throw new ZabbixAPIException("API Communication error: "+ e.getMessage());
		}

	}
}

class MyHostnameVerifier implements HostnameVerifier {
    public boolean verify(String hostname, SSLSession session) {
            return true;
    }
}
class MyTrustManager implements X509TrustManager {	 
    public void checkClientTrusted(X509Certificate[] chain, String authType) {

    }
    public void checkServerTrusted(X509Certificate[] chain, String authType) {
    }
    public X509Certificate[] getAcceptedIssuers() {
            return null;
    }
}
