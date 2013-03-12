package ru.sonic.zabbix;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

/**
 * display/edit user prefs
 * @author gryphius
 *
 */
public class InterfacePrefActivity extends PreferenceActivity implements
OnSharedPreferenceChangeListener {
	
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.interfaceprefs);
        
        ListPreference tabscount = (ListPreference) findPreference("tabscount");
        String key = tabscount.getValue().toString();
        checktabs(key);
        
        ListPreference tab1 = (ListPreference) findPreference("tab_first");
        tab1.setSummary(tab1.getEntry());
    	ListPreference tab2 = (ListPreference) findPreference("tab_second");
    	tab2.setSummary(tab2.getEntry());
    	ListPreference tab3 = (ListPreference) findPreference("tab_third");
    	tab3.setSummary(tab3.getEntry());
    	ListPreference tab4 = (ListPreference) findPreference("tab_fourd");
    	tab4.setSummary(tab4.getEntry());
    }
    
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	if (key.equals("tabscount"))
        checktabs(key);
    	
    	if (key.equals("tab_first")) {
    		ListPreference tab = (ListPreference)findPreference("tab_first");
    		tab.setSummary(tab.getEntry());
    	}
    	
    	if (key.equals("tab_second")) {
    		ListPreference tab = (ListPreference)findPreference("tab_second");
    		tab.setSummary(tab.getEntry());
    	}
    	
    	if (key.equals("tab_third")) {
    		ListPreference tab = (ListPreference)findPreference("tab_third");
    		tab.setSummary(tab.getEntry());
    	}
    	
    	if (key.equals("tab_fourd")) {
    		ListPreference tab = (ListPreference)findPreference("tab_fourd");
    		tab.setSummary(tab.getEntry());
    	}
    		
    }
    
    public void checktabs(String key) {
    	ListPreference tab1 = (ListPreference) findPreference("tab_first");
    	ListPreference tab2 = (ListPreference) findPreference("tab_second");
    	ListPreference tab3 = (ListPreference) findPreference("tab_third");
    	ListPreference tab4 = (ListPreference) findPreference("tab_fourd");
    	ListPreference tabscount = (ListPreference) findPreference("tabscount");
        
        if(tabscount.getValue().equals("1")) {
        	tab1.setEnabled(true);
        	tab2.setEnabled(false);
        	tab3.setEnabled(false);
        	tab4.setEnabled(false);
        } else if (tabscount.getValue().equals("2")) {
          	tab1.setEnabled(true);
        	tab2.setEnabled(true);
        	tab3.setEnabled(false);
        	tab4.setEnabled(false);
        } else if (tabscount.getValue().equals("3")) {
          	tab1.setEnabled(true);
        	tab2.setEnabled(true);
        	tab3.setEnabled(true);
        	tab4.setEnabled(false);
        } else if (tabscount.getValue().equals("4")) {
          	tab1.setEnabled(true);
        	tab2.setEnabled(true);
        	tab3.setEnabled(true);
        	tab4.setEnabled(true);
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
