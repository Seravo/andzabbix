package ru.sonic.zabbix;

import java.util.List;

import ru.sonic.zabbix.base.DBAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


public class ServerActivity extends Activity {
	int ERROR;
	DBAdapter db = new DBAdapter(this);
	String user,pass,timeout,url,srvname,action,oldname,base_login,base_pass;
	boolean base_use;
	EditText prefServerName,prefServerUrl,prefServerUser,prefServerPass,base_auth_user,base_auth_pass;
	Spinner prefConnTimeout;
	
	CheckBox use_base_auth;

   @Override
   protected void onCreate(final Bundle savedInstanceState) {
	   super.onCreate(savedInstanceState);
	   Log.d(this.getClass().getName(),"Start activity ");
	   Bundle extras = getIntent().getExtras();
	   srvname = extras.getString("Servername");
	   action = extras.getString("action");
	   setContentView(R.layout.serverpref_layout);
	   setTitle("Server Configuration");
	   
	   Toast.makeText(getApplicationContext(), "Press 'Back' button for save changes.", Toast.LENGTH_SHORT).show();
	   
	   Log.d(this.getClass().getName(),"Get servername: " + srvname);
		if (action.equals("new")) {
			srvname = "Zabbix server";
			url = "http://example.com/zabbix/api_jsonrpc.php";
			user = "user";
			pass = "";
			timeout = "10";
		} else {
			db.open();
			List<?> credentials = db.selectServer(srvname);
			db.close();

			oldname = srvname;
			srvname = credentials.get(0).toString();
			url = credentials.get(1).toString();
			user = credentials.get(2).toString();
			pass = credentials.get(3).toString();
			timeout = credentials.get(4).toString();
			String base_use_str = credentials.get(5).toString();
			if (base_use_str.equals("true"))
				base_use = true;
			else
				base_use = false;
			base_login = credentials.get(6).toString();
			base_pass = credentials.get(7).toString();
		}
		

		use_base_auth =  (CheckBox)findViewById(R.id.use_base_auth);
		prefServerName = (EditText)findViewById(R.id.prefServerName);
		prefServerUrl = (EditText)findViewById(R.id.prefServerUrl);
		prefServerUser = (EditText)findViewById(R.id.prefServerUser);
		prefServerPass = (EditText)findViewById(R.id.prefServerPass);
		prefConnTimeout = (Spinner)findViewById(R.id.prefConnTimeout);
		base_auth_user = (EditText)findViewById(R.id.base_auth_user);
		base_auth_pass = (EditText)findViewById(R.id.base_auth_pass);
		
		ArrayAdapter<String> myAdap = (ArrayAdapter<String>) prefConnTimeout.getAdapter();
		int spinnerPosition = myAdap.getPosition(timeout);
		
		prefServerName.setText(srvname);
		prefServerUrl.setText(url);
		prefServerUser.setText(user);
		prefServerPass.setText(pass);
		prefConnTimeout.setSelection(spinnerPosition);
		base_auth_user.setText(base_login);
		base_auth_pass.setText(base_pass);
		
        if (base_use) {
        	use_base_auth.setChecked(true);
        	check_base_auth(true);
        } else {
        	use_base_auth.setChecked(false);
        	check_base_auth(false);
        }
        
        use_base_auth.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if ( isChecked )
                	check_base_auth(true);
                else
                	check_base_auth(false);
            }
        });
   }
   
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        //moveTaskToBack(true);
	        //return true;
	    		srvname = prefServerName.getText().toString();
	    		url = prefServerUrl.getText().toString();
	    		user = prefServerUser.getText().toString();
	    		pass = prefServerPass.getText().toString();
	    		timeout = prefConnTimeout.getSelectedItem().toString();
	    		base_use = use_base_auth.isChecked();
	    		int base_use_flag = 0;
	    		if (base_use) base_use_flag =1;
	    		base_login = base_auth_user.getText().toString();
	    		base_pass = base_auth_pass.getText().toString();
	    		Log.d(this.getClass().getName(), "Selected Item: "+timeout);
	    		db.open();
	    	if (action.equals("new")) {
	    		List<String> slist = db.selectServerNames();
	    		for (String serverInList : slist) {
	    			if (serverInList.equals(srvname)) { ERROR = 1;}
	    		}
	    		if (ERROR!=1) {
	    		db.insertServer(srvname,url,user,pass,timeout,base_use_flag, base_login, base_pass);
	    		} else {
	    			Log.e(this.getClass().getName(), "Server with same name already exist");
	    		}
	    	} else if (action.equals("edit")) {
	    		db.updateServer(oldname,srvname,url,user,pass,timeout,base_use_flag, base_login, base_pass);
	    	}
	    	db.close();
			//Log.d(this.getClass().getName(), "Data inserted");
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	
	public void check_base_auth(boolean checked) {
		TextView base_auth_user_txt = (TextView)findViewById(R.id.base_auth_user_txt);
		TextView base_auth_pass_txt = (TextView)findViewById(R.id.base_auth_pass_txt);
		if (!checked) {
			base_auth_user.setVisibility(View.GONE);
			base_auth_pass.setVisibility(View.GONE);
			base_auth_user_txt.setVisibility(View.GONE);
			base_auth_pass_txt.setVisibility(View.GONE);
		} else {
			base_auth_user.setVisibility(View.VISIBLE);
			base_auth_pass.setVisibility(View.VISIBLE);
			base_auth_user_txt.setVisibility(View.VISIBLE);
			base_auth_pass_txt.setVisibility(View.VISIBLE);
		}
	}
}