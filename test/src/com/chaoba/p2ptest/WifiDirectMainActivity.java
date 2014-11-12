package com.chaoba.p2ptest;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chaoba.p2p.utils.Logger;
import com.chaoba.p2p.utils.ToastManager;
import com.chaoba.p2p.wifidirect.ConnectionManagerImpl;
import com.chaoba.p2p.wifidirect.interf.IP2PmanagerListener;
import com.ipaulpro.afilechooser.utils.FileUtils;

public class WifiDirectMainActivity extends Activity implements OnClickListener,
IP2PmanagerListener {

	protected static final String TAG = "WifiDirectMainActivity";
	private static final int REQUEST_CODE = 0;
	Button mCreateButton, mConnectButton, mSendButton, mFindButton,mSendFile;
	EditText mEdit, mMessageEdit;
	TextView mLog,mPercent;
	private Context mContext;
	ConnectionManagerImpl mConnectionManagerImpl;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;
		mConnectionManagerImpl=new ConnectionManagerImpl(mContext,this);
		mCreateButton = (Button) findViewById(R.id.create_server);
		mConnectButton = (Button) findViewById(R.id.connect_server);
		mSendButton = (Button) findViewById(R.id.send);
		mFindButton = (Button) findViewById(R.id.find);
		mSendFile = (Button) findViewById(R.id.send_file);
		mEdit = (EditText) findViewById(R.id.server_name);
		mMessageEdit = (EditText) findViewById(R.id.message_edit);
		mLog=(TextView) findViewById(R.id.log);
		mPercent=(TextView) findViewById(R.id.percent);
		mCreateButton.setOnClickListener(this);
		mConnectButton.setOnClickListener(this);
		mSendButton.setOnClickListener(this);
		mFindButton.setOnClickListener(this);
		mSendFile.setOnClickListener(this);

	}

	private Handler mHandler=new Handler(){
		 public void handleMessage(Message msg) {
			 switch(msg.what){
			 case 0:
				 String text=(String) msg.obj;
				 mEdit.setText(text);
				 break;
			 case 1:
				 text=(String) msg.obj;
				 mLog.setText(mLog.getText()+"\n"+text);
				 break;
			 case 2:
				 text=(String) msg.obj;
				 mPercent.setText(text);
				 break;
			 }
		    }
	};
	
	@Override
	public void onDestroy() {
		Logger.d(TAG,"onDestroy");
		mConnectionManagerImpl.disConnectServer();
		mConnectionManagerImpl.detroy();
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	 public boolean onMenuItemSelected(int featureId, MenuItem item) {
		finish();
		return false;
		
	}
	@Override
	public void onClick(View v) {
			int id = v.getId();
			switch (id) {
			case R.id.create_server:
				mConnectionManagerImpl.createServer("test");
				break;
			case R.id.connect_server:
				mConnectionManagerImpl.connectServer(mEdit.getEditableText()
						.toString());
				break;
			case R.id.send:
				String s=mMessageEdit
						.getEditableText().toString();
				mConnectionManagerImpl.sendMessage(s);
				Message msg=mHandler.obtainMessage();
				msg.what=1;
				msg.obj="me:"+s;
				mHandler.sendMessage(msg);
				break;
			case R.id.find:
				mConnectionManagerImpl.findServer();
				mEdit.setText("");
				break;
			case R.id.send_file:
				  // Use the GET_CONTENT intent from the utility class
		        Intent target = FileUtils.createGetContentIntent();
		        // Create the chooser Intent
		        Intent intent = Intent.createChooser(
		                target, "chose file");
		        try {
		            startActivityForResult(intent, REQUEST_CODE);
		        } catch (ActivityNotFoundException e) {
		            // The reason for the existence of aFileChooser
		        }
				break;
			}
		}
	 @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        switch (requestCode) {
	            case REQUEST_CODE:
	                // If the file selection was successful
	                if (resultCode == RESULT_OK) {
	                    if (data != null) {
	                        // Get the URI of the selected file
	                        final Uri uri = data.getData();
	                        try {
	                            // Get the file path from the URI
	                            final String path = FileUtils.getPath(this, uri);
	                            Logger.d(TAG,"path:"+path);
	                            ArrayList<String> paths=new ArrayList<String>();
	                            paths.add(path);
	                            mConnectionManagerImpl.sendFiles(paths);
	                        } catch (Exception e) {
	                            Log.e("FileSelectorTestActivity", "File select error", e);
	                        }
	                    }
	                }
	                break;
	        }
	        super.onActivityResult(requestCode, resultCode, data);
	    }



	@Override
	public void onReceiveMessage(String message) {
		Logger.d(TAG,"onReceiveMessage:"+message);
		
	}

	@Override
	public void onFilePercentUpdated(String fileName, int percent) {
		Logger.d(TAG,"onFilePercentUpdated:"+fileName+"::"+percent);
		
	}

	@Override
	public void onServerFound(String serverName) {
		Logger.d(TAG,"onServerFound:"+serverName);
		mEdit.setText(serverName);
	}

	@Override
	public void onServerCreated() {
		Logger.d(TAG,"onServerCreated:");
		
	}

	@Override
	public void onServerConnected() {
		Logger.d(TAG,"onServerConnected:");
		
	}

	@Override
	public void onClientConnected(String clientName) {
		Logger.d(TAG,"onClientConnected:"+clientName);
		
	}

	@Override
	public void onConnectionLost() {
		Logger.d(TAG,"onConnectionLost:");
		
	}

}
