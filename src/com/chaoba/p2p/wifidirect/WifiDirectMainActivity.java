package com.chaoba.p2p.wifidirect;

import android.app.Activity;
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

import com.chaoba.p2p.R;
import com.chaoba.p2p.utils.Logger;
import com.chaoba.p2p.utils.ToastManager;
import com.chaoba.p2p.wifidirect.interf.IP2PmanagerListener;

public class WifiDirectMainActivity extends Activity implements
		OnClickListener, IP2PmanagerListener {

	protected static final String TAG = "WifiDirectMainActivity";
	private static final int REQUEST_CODE = 0;
	Button mCreateButton, mConnectButton, mSendButton, mFindButton, mSendFile;
	EditText mEdit, mMessageEdit;
	TextView mLog, mPercent, mPeerName;
	private Context mContext;
	ConnectionManagerImpl mConnectionManagerImpl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;
		mConnectionManagerImpl = new ConnectionManagerImpl(mContext, this);
		mCreateButton = (Button) findViewById(R.id.set_name);
		mConnectButton = (Button) findViewById(R.id.connect_peer);
		mSendButton = (Button) findViewById(R.id.send);
		mFindButton = (Button) findViewById(R.id.find);
		mSendFile = (Button) findViewById(R.id.send_file);
		mEdit = (EditText) findViewById(R.id.server_name);
		mMessageEdit = (EditText) findViewById(R.id.message_edit);
		mLog = (TextView) findViewById(R.id.log);
		mPercent = (TextView) findViewById(R.id.percent);
		mPeerName = (TextView) findViewById(R.id.peer_name);
		mCreateButton.setOnClickListener(this);
		mConnectButton.setOnClickListener(this);
		mSendButton.setOnClickListener(this);
		mFindButton.setOnClickListener(this);
		mSendFile.setOnClickListener(this);

	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			String text = (String) msg.obj;
			switch (msg.what) {
			case 0:
				// mEdit.setText(text);
				break;
			case 1:
				text = (String) msg.obj;
				mLog.setText(mLog.getText() + "\n" + text);
				break;
			case 2:
				text = (String) msg.obj;
				mPercent.setText(text);
				break;
			}
		}
	};

	@Override
	public void onDestroy() {
		Logger.d(TAG, "onDestroy");
		mConnectionManagerImpl.close();
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
		case R.id.set_name:
			mConnectionManagerImpl.setName(mEdit.getText().toString());
			break;
		case R.id.connect_peer:
			mConnectionManagerImpl.connectServer(mPeerName.getText().toString());
			break;
		case R.id.send:
			String s = mMessageEdit.getEditableText().toString();
			mConnectionManagerImpl.sendMessage(s);
			Message msg = mHandler.obtainMessage();
			msg.what = 1;
			msg.obj = "me:" + s;
			mHandler.sendMessage(msg);
			break;
		case R.id.find:
			mConnectionManagerImpl.findPeer();
			mPeerName.setText("");
			break;
		case R.id.send_file:
//			// Use the GET_CONTENT intent from the utility class
//			Intent target = FileUtils.createGetContentIntent();
//			// Create the chooser Intent
//			Intent intent = Intent.createChooser(target, "chose file");
//			try {
//				startActivityForResult(intent, REQUEST_CODE);
//			} catch (ActivityNotFoundException e) {
//				// The reason for the existence of aFileChooser
//			}
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
//						// Get the file path from the URI
//						final String path = FileUtils.getPath(this, uri);
//						Logger.d(TAG, "path:" + path);
//						ArrayList<String> paths = new ArrayList<String>();
//						for(int i=0;i<3;i++)
//						paths.add(path);
//						mConnectionManagerImpl.sendFiles(paths);
					} catch (Exception e) {
						Log.e("FileSelectorTestActivity", "File select error",
								e);
					}
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onReceiveMessage(String message) {
		Logger.d(TAG, "onReceiveMessage:" + message);
		Message msg = mHandler.obtainMessage();
		msg.what = 1;
		msg.obj = "receive:" + message;
		mHandler.sendMessage(msg);
	}

	@Override
	public void onFilePercentUpdated(String fileName, int percent) {
		Logger.d(TAG, "onFilePercentUpdated:" + fileName + "::" + percent);
		Message msg = mHandler.obtainMessage();
		msg.what = 2;
		msg.obj = "onFilePercentUpdated:" + fileName + "::" + percent;
		mHandler.sendMessage(msg);
	}

	@Override
	public void onServerFound(String serverName, boolean isSelf) {
		Logger.d(TAG, "onServerFound:" + serverName);
		if (isSelf) {
			mEdit.setText(serverName);
		} else {
			mPeerName.setText(serverName);
		}
	}

	@Override
	public void onServerCreated() {
		ToastManager.show(mContext, "server created");
		Logger.d(TAG, "onServerCreated:");

	}

	@Override
	public void onServerConnected() {
		ToastManager.show(mContext, "onServerConnected");
		Logger.d(TAG, "onServerConnected:");

	}

	@Override
	public void onClientConnected(String clientName) {
		ToastManager.show(mContext, "onClientConnected:"+clientName);
		Logger.d(TAG, "onClientConnected:" + clientName);

	}

	@Override
	public void onConnectionLost() {
		ToastManager.show(mContext, "onConnectionLost");
		Logger.d(TAG, "onConnectionLost:");

	}

}
