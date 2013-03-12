package ru.sonic.zabbix.service;

import ru.sonic.zabbix.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

/**
 * display/edit user prefs
 * @author gryphius
 * 
 */
public class ServicePrefActivity extends PreferenceActivity implements
OnSharedPreferenceChangeListener {
	
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.serviceprefs);
    }
    
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	if (key.equals("service_enable")) {
    		CheckBoxPreference cbpr = (CheckBoxPreference)findPreference("service_enable");
    		if (cbpr.isChecked())
    			startService(new Intent(this, ZBXCheckService.class));
    		else
    			stopService(new Intent(this, ZBXCheckService.class));
    	}
    }
 
    @Override
    protected void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}