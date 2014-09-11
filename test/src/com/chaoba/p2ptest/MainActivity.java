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
			 }
		    }
	};
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
				mManagerServiceBinder.sendMessage(mMessageEdit
						.getEditableText().toString());
				break;
			case R.id.find:
				mManagerServiceBinder.findServer();
				mEdit.setText("");
				break;
			}
		}
	}

	@Override
	public void receiveMessage(byte[] receMeg, int size) {
		String s = new String(receMeg,0,size);
		Logger.d(TAG,"receive message:"+s);
		ToastManager.show(mContext, s);
	}

	@Override
	public void serverCreated() {
		ToastManager.show(mContext, "Create server Ok!");
	}

	@Override
	public void updateServer(String s) {
		Logger.d(TAG,"updateServer:"+s);
		Message msg=mHandler.obtainMessage();
		msg.what=0;
		msg.obj=s;
		mHandler.sendMessage(msg);

	}

}
