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

import com.chaoba.p2p.AdvertiseService.AdvertiseServiceBinder;
import com.chaoba.p2p.ClientService.ClientServiceBinder;
import com.example.p2p.R;

public class MainActivity extends Activity implements OnClickListener {

	protected static final String TAG = "MainActivity";
	Button mCreateButton, mConnectButton,mSendButton,mAdvertiseButton,mListenButton,mFindButton;
	EditText mEdit,mMessageEdit;
	private Context mContext;
	private ServiceConnection mServiceConnection;
	protected ClientServiceBinder mDiagnosisServiceBinder;
	private ServiceConnection mAdvertiseServiceConnection;
	protected AdvertiseServiceBinder mAdvertiseServiceBinder;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext=this;
		mCreateButton = (Button) findViewById(R.id.create_server);
		mConnectButton = (Button) findViewById(R.id.connect_server);
		mSendButton=(Button) findViewById(R.id.send);
		mAdvertiseButton=(Button) findViewById(R.id.advertise);
		mListenButton=(Button) findViewById(R.id.listen);
		mFindButton=(Button) findViewById(R.id.find);
		mEdit = (EditText) findViewById(R.id.server_ip);
		mMessageEdit= (EditText) findViewById(R.id.message_edit);
		
		mCreateButton.setOnClickListener(this);
		mConnectButton.setOnClickListener(this);
		mSendButton.setOnClickListener(this);
		mAdvertiseButton.setOnClickListener(this);
		mListenButton.setOnClickListener(this);
		mFindButton.setOnClickListener(this);
		mServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "onServiceConnected");
				mDiagnosisServiceBinder = (ClientServiceBinder) service;
				mDiagnosisServiceBinder.connect(mEdit.getText().toString(), 8000);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "onServiceDisconnected");
			}
		};
		
		mAdvertiseServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "onAdvertiseServiceConnected");
				mAdvertiseServiceBinder = (AdvertiseServiceBinder) service;
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "onServiceDisconnected");
			}
		};
	
		bindService(
				new Intent(getApplicationContext(), AdvertiseService.class),
				mAdvertiseServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		int id=v.getId();
		switch(id){
		case R.id.create_server:
			Intent i=new Intent(mContext,ServerService.class);
			startService(i);
			break;
		case R.id.connect_server:
			bindService(
					new Intent(getApplicationContext(), ClientService.class),
					mServiceConnection, Context.BIND_AUTO_CREATE);
			break;
		case R.id.send:
			if(mDiagnosisServiceBinder!=null){
				mDiagnosisServiceBinder.sendMessage(mMessageEdit.getText().toString());
			}
			break;
		case R.id.advertise:
			mAdvertiseServiceBinder.advertise();
			break;
		case R.id.listen:
			mAdvertiseServiceBinder.listen();
			break;
		case R.id.find:
			mAdvertiseServiceBinder.find();
			break;
		}
		
	}

}
