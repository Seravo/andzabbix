package ru.sonic.zabbix;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import ru.sonic.zabbix.base.ZabbixAPIException;
import ru.sonic.zabbix.base.ZabbixAPIHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ZoomControls;

/**
 * display active triggers in a listview
 * 
 * @author gryphius
 * 
 */
public class GraphActivity extends Activity {
	private static final String TAG = "ZabbixGraphs";
	private static final int MSG_DATA_RETRIEVED = 0;
	private static final int MSG_ERROR = 1;

	private ProgressDialog myProgressDialog = null;
	protected ZabbixAPIHandler api = null;
	private String graphID = "";
	private String imageurl;
	RelativeLayout mainPanel;
	private int graphWidth = 0;
	private int period = 3600;
	private float stime = 0;
	String baseurl = "";
	ImageView imageView;
 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.graph);
		graphID = (String) getIntent().getExtras().get("graphID");
		api = new ZabbixAPIHandler(this);
		mainPanel = (RelativeLayout) findViewById(R.id.graphll);
		imageView = (ImageView)findViewById(R.id.graphView1);

		graphWidth = getDisplayWidth();
		try {
			baseurl = api.getAPIURL().split("/api_jsonrpc.php")[0];
		} catch (ZabbixAPIException e) {
			e.printStackTrace();
		}
		
		imageurl = makeFullUrl(graphWidth,stime,period);
		refreshData();

		ZoomControls zoomGrapgControls = (ZoomControls) findViewById(R.id.zoomGrapgControls);
		//SeekBar graphTimeLine = (SeekBar) findViewById(R.id.graphTimeLine);

		zoomGrapgControls.setOnZoomInClickListener(new OnClickListener() {
			public void onClick(View v) {
				period = period / 2;
				if (period<3600)
					period=3600;
				imageurl = makeFullUrl(graphWidth,stime,period);
				refreshData();
			}
		});
		zoomGrapgControls.setOnZoomOutClickListener(new OnClickListener() {
			public void onClick(View v) {
				period = period * 2;
				imageurl = makeFullUrl(graphWidth,stime,period);
				refreshData();
			}
		});
	}
	
	private String makeFullUrl (int gwidth, float gstime, int gperiod) {
		String fullURL = "";
		String chartGrapgID = "/chart2.php?graphid="+graphID;
		String widthurl =  "&width="+ gwidth;
		String stimeurl = "";
		if (gstime != 0) {
			 stimeurl = "&stime=" + gstime;
		}
		String imageurlperiod = "&period=" + gperiod;
		
		fullURL = baseurl + chartGrapgID + widthurl + stimeurl + imageurlperiod;
		Log.e(TAG,"URL: "+fullURL);
		return fullURL;
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.arg1) {
			case MSG_ERROR:
				displayErrorPopup(((ZabbixAPIException) msg.obj).getMessage());
				myProgressDialog.dismiss();
				break;
			case MSG_DATA_RETRIEVED:
				myProgressDialog.dismiss();
				drawImage((Bitmap) msg.obj);
				break;
			default:
				break;
			}
		}
	};

	public void drawImage(Bitmap img) {
		imageView.setImageBitmap(img);
		//mainPanel.addView(imageView);
	}

	private int getDisplayWidth() {
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		return width;
	}

	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	public void displayErrorPopup(String message) {
		new AlertDialog.Builder(this).setMessage(message).setTitle("Error")
				.show();
	}

	public void refreshData() {
		myProgressDialog = ProgressDialog.show(this, "Please wait...",
				"Retrieving data", false);
		new Thread() {
			public void run() {
				Message msg = new Message();
				try {
					Bitmap img = api.getGraphImage(imageurl);
					msg.obj = img;
					msg.arg1 = MSG_DATA_RETRIEVED;
					handler.sendMessage(msg);
				} catch (ZabbixAPIException e) {
					Log.e(TAG, "rpc call failed: " + e);
					msg.arg1 = MSG_ERROR;
					msg.obj = e;
					handler.sendMessage(msg);
					return;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
}