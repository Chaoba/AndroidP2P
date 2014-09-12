package com.chaoba.p2ptest;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
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

public class MainActivity extends Activity implements OnClickListener,
		IManagerServiceCallback {

	protected static final String TAG = "MainActivity";
	Button mCreateButton, mConnectButton, mSendButton, mFindButton;
	EditText mEdit, mMessageEdit;
	TextView mLog;
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
		mEdit = (EditText) findViewById(R.id.server_name);
		mMessageEdit = (EditText) findViewById(R.id.message_edit);
		mLog=(TextView) findViewById(R.id.log);
		mCreateButton.setOnClickListener(this);
		mConnectButton.setOnClickListener(this);
		mSendButton.setOnClickListener(this);
		mFindButton.setOnClickListener(this);
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
	public void onClick(View v) {
		if (mManagerServiceBinder != null) {
			int id = v.getId();
			switch (id) {
			case R.id.create_server:
				mManagerServiceBinder.startServer("test server");
				break;
			case R.id.connect_server:
				mManagerServiceBinder.connectServer(mEdit.getEditableText()
						.toString());
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
			}
		}
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

}
