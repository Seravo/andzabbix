package ru.sonic.zabbix;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Feedback extends Activity {

	TextView resultView;
	EditText name;
	EditText comment;
	Button submit;
	Button refresh;
	
	//@SuppressLint("newApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_faq);
		
		resultView = (TextView) findViewById(R.id.result);
        
        name = (EditText) findViewById(R.id.nameInput);
        
        comment = (EditText) findViewById(R.id.commentInput);
        
        submit = (Button) findViewById(R.id.submit);
        
        refresh = (Button) findViewById(R.id.refresh);
        
        getData();

	}

	public void getData()
    {
    	String result = "";
    	InputStream strm = null;
    	
    	try
    	{
    		HttpClient httpclient = new DefaultHttpClient();
    		
    		HttpPost httppost = new HttpPost("http://arcada.it-edu.fi/php14/haquabm/WebServiceForSQL/sqlService.php?");
    		
    		HttpResponse responce = httpclient.execute(httppost);
    		
    		HttpEntity entity = responce.getEntity();
    		
    		strm = entity.getContent();
    	}
    	catch(Exception e)
    	{
    		resultView.setText("Could not connect to the database!! Please check your internet connection.");
    	}
    	
    	try
    	{
    		BufferedReader reader = new BufferedReader (new InputStreamReader(strm,"utf-8"),8);
    		
    		StringBuilder tb = new StringBuilder();
    		
    		String line = null;
    		
    		while((line = reader.readLine())!= null)
    		{
    			tb.append(line + "\n");
    		}
    		
    		strm.close();
    		
    		result = tb.toString();
    		
    	}
    	catch(Exception e)
    	{
    		Log.e("log_tag", "Error in http connection "+e.toString());
    	}
    	
    	try
    	{
    		String s = "";
    		
    		JSONArray jarray = new JSONArray(result);
    		
    		for(int i = 0; i<jarray.length(); i++)
    		{
    			JSONObject json = jarray.getJSONObject(i);
    			
    			s = s + 
    					"Name: "+json.getString("name")+"\n"+
    					"Comment: "+json.getString("comment")+"\n\n";
    		}
    		
    		resultView.setText(s);
    	}
    	
    	catch(Exception e)
    	{
    		Log.e("log_tag", "Error in http connection "+e.toString());
    		Toast.makeText(this, "Could not connect to the database!! Please check your internet connection.",Toast.LENGTH_LONG).show();
    	}
    	
    }
	
    public void sendToDb(String name, String comment)
    {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("name",name));
        nameValuePairs.add(new BasicNameValuePair("comment",comment));

        try{
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new      
            HttpPost("http://arcada.it-edu.fi/php14/haquabm/WebServiceForSQL/welcome.php");
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            Log.i("postData", response.getStatusLine().toString());
        }

        catch(Exception e)
        {
            Log.e("log_tag", "Error in http connection "+e.toString());
        }           
    }
    
	public void sendButtonOnclick(View view)
	{
		String getName = name.getText().toString();
	       
		String getComment = comment.getText().toString();
		
		Editable editName = name.getText();
		Editable editComment = comment.getText();
	      
		if(editName.length() == 0 || editComment.length() == 0)
		{
			Toast.makeText(this, "All fields must be filled!!",Toast.LENGTH_LONG).show();
		}
		else
		{
			  sendToDb(getName, getComment);
		      Toast.makeText(this, "Thanks for your comment!!",Toast.LENGTH_LONG).show();
		      getData();
		      name.setText("");
		      comment.setText("");
			
		}
	    	  
	}
	
	public void refreshButtonOnclick(View view)
	{
		name.setText("");
		comment.setText("");
		getData();
		Toast.makeText(this, "Refreshed!!",Toast.LENGTH_LONG).show();
	}

}
