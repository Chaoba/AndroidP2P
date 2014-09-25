package com.chaoba.p2ptest;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chaoba.p2p.ManagerService;
import com.chaoba.p2p.interf.IManagerService;
import com.chaoba.p2p.interf.IManagerServiceCallback;
import com.chaoba.p2p.utils.Logger;
import com.chaoba.p2p.utils.ToastManager;
import com.ipaulpro.afilechooser.utils.FileUtils;

public class MainActivity extends Activity implements OnClickListener,
		IManagerServiceCallback {

	protected static final String TAG = "MainActivity";
	private static final int REQUEST_CODE = 0;
	Button mCreateButton, mConnectButton, mSendButton, mFindButton,mSendFile;
	EditText mEdit, mMessageEdit;
	TextView mLog,mPercent;
	private Context mContext;
	private ServiceConnection mServiceConnection;
	protected IManagerService mManagerServiceBinder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;
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
		mServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "onServiceConnected");
				mManagerServiceBinder = (IManagerService) service;
				mManagerServiceBinder.registCallback(MainActivity.this);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "onServiceDisconnected");
				mManagerServiceBinder.unRegistCallback();
				mManagerServiceBinder=null;
			}
		};

		// Intent i = new Intent("com.chaoba.p2p.interf.IManagerService");
		Intent i = new Intent();
		i.setClass(mContext, ManagerService.class);
		Logger.d(
				TAG,
				"bind service:"
						+ bindService(i, mServiceConnection,
								Context.BIND_AUTO_CREATE));
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
		unbindService(mServiceConnection);
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
		if (mManagerServiceBinder != null) {
			int id = v.getId();
			switch (id) {
			case R.id.create_server:
				mManagerServiceBinder.startServer("test server",true);
				break;
			case R.id.connect_server:
				mManagerServiceBinder.connectServer(mEdit.getEditableText()
						.toString(),true);
				break;
			case R.id.send:
				String s=mMessageEdit
						.getEditableText().toString();
				mManagerServiceBinder.sendMessage(s);
				Message msg=mHandler.obtainMessage();
				msg.what=1;
				msg.obj="me:"+s;
				mHandler.sendMessage(msg);
				break;
			case R.id.find:
				mManagerServiceBinder.findServer();
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
	                            mManagerServiceBinder.sendFiles(paths);
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
	public void receiveMessage(String s) {
		Logger.d(TAG,"receive message:"+s);
		Message msg=mHandler.obtainMessage();
		msg.what=1;
		msg.obj="receive message:"+s;
		mHandler.sendMessage(msg);
	}

	@Override
	public void serverCreated() {
		Message msg=mHandler.obtainMessage();
		msg.what=1;
		msg.obj="server created Ok!";
		mHandler.sendMessage(msg);
	}

	@Override
	public void updateServer(String s) {
		Logger.d(TAG,"updateServer:"+s);
		ToastManager.show(mContext, "find server:"+s);
		Message msg=mHandler.obtainMessage();
		msg.what=0;
		msg.obj=s;
		mHandler.sendMessage(msg);

	}

	@Override
	public void connectToServer() {
		Message msg=mHandler.obtainMessage();
		msg.what=1;
		msg.obj="connect to server Ok!";
		mHandler.sendMessage(msg);
	}

	@Override
	public void acceptClient() {
		Message msg=mHandler.obtainMessage();
		msg.what=1;
		msg.obj="acceptClient Ok!";
		mHandler.sendMessage(msg);
	}

	@Override
	public void updateFilePercent(String FileName, int percent) {
		Logger.d(TAG,"updateFilePercent"+FileName+":"+percent);
		Message msg=mHandler.obtainMessage();
		msg.what=2;
		msg.obj=FileName+":"+percent+"%";
		mHandler.sendMessage(msg);
	}

}
