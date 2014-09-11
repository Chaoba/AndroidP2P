package com.chaoba.p2p;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

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
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "onServiceDisconnected");
			}
		};
		bindService(
				new Intent(getApplicationContext(), ManagerService.class),
				mServiceConnection, Context.BIND_AUTO_CREATE);
		
//		ComponentName com = new ComponentName("com.chaoba.p2p",
//				"ManagerService");
//		Intent i = new Intent();
//		i.setComponent(com);
//		Logger.d(TAG,
//				getApplicationContext().bindService(i, mServiceConnection, Context.BIND_AUTO_CREATE));
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
//			case R.id.create_server:
//				mManagerServiceBinder.startServer("test server");
//				break;
//			case R.id.connect_server:
//				mManagerServiceBinder.connectServer(mEdit.getEditableText()
//						.toString());
//				break;
//			case R.id.send:
//				mManagerServiceBinder.sendMessage(mMessageEdit
//						.getEditableText().toString());
//				break;
//			case R.id.find:
//				mManagerServiceBinder.findServer();
//				break;
			}
		}
	}

	@Override
	public void receiveMessage(byte[] receMeg, int size) {
		String s = new String(receMeg);
		Logger.d(TAG,"receive message:"+s);
		ToastManager.show(mContext, s);
	}

	@Override
	public void serverCreated() {
		ToastManager.show(mContext, "Create server Ok!");
	}


	@Override
	public void updateServer(String serverName) {
		Logger.d(TAG,"update server:"+serverName);
		mEdit.setText(serverName);
	}

}
