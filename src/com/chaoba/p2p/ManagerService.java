package com.chaoba.p2p;

import java.util.HashMap;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.chaoba.p2p.AdvertiseService.AdvertiseServiceBinder;
import com.chaoba.p2p.ClientService.ClientServiceBinder;
import com.chaoba.p2p.ServerService.ServerServiceBinder;
import com.chaoba.p2p.interf.IAdvertiseServiceCallback;
import com.chaoba.p2p.interf.IManagerService;
import com.chaoba.p2p.interf.IManagerServiceCallback;
import com.chaoba.p2p.interf.IServerServiceCallback;
import com.chaoba.p2p.utils.Logger;
import com.chaoba.p2p.utils.Util;

public class ManagerService extends Service implements IServerServiceCallback,
		IAdvertiseServiceCallback {
	private static final String TAG = "ManagerService";
	private Context mContext;
	private ServiceConnection mClientServiceConnection;
	protected ClientServiceBinder mClientServiceBinder;

	private ServiceConnection mServerServiceConnection;
	protected ServerServiceBinder mServerServiceBinder;

	private ServiceConnection mAdvertiseServiceConnection;
	protected AdvertiseServiceBinder mAdvertiseServiceBinder;
	private HashMap<String, String> mServerMap = new HashMap<String, String>();
	private String mServerName;
	private String mServerNameToConnect;
	private IManagerServiceCallback mCallback;
	private boolean isServer;

	@Override
	public IBinder onBind(Intent intent) {
		Logger.d(TAG, "onbind");
		return new ManagerServiceBinder();
	}

	public class ManagerServiceBinder extends Binder implements IManagerService {
		@Override
		public void startServer(String serverName) {
			Logger.d(TAG, "startserver:" + serverName);
			isServer = true;
			mServerName = serverName;
			bindServerService();
			bindAdvertiseService();
		}

		@Override
		public void stopServer() {
			soptServerService();

		}

		@Override
		public void findServer() {
			bindAdvertiseService();
			mHandler.sendEmptyMessageDelayed(0, 2000);
		}

		@Override
		public void connectServer(String serverName) {
			isServer = false;
			mServerNameToConnect = serverName;
			bindClientService();

		}

		@Override
		public void registCallback(IManagerServiceCallback callback) {
			mCallback = callback;

		}

		@Override
		public void unRegistCallback() {
			mCallback = null;

		}

		@Override
		public void sendMessage(String message) {
			sendMess(message);

		}

	};

	@Override
	public void onCreate() {
		super.onCreate();
		Logger.d(TAG, "onCreate");
		mContext = this;

		mClientServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "mClientServiceConnection onServiceConnected");
				mClientServiceBinder = (ClientServiceBinder) service;
				// if bind to clientService, means user want to connect one
				// server, so connect it directly.

				if (mServerNameToConnect != null) {
					connectServer(mServerNameToConnect);
				}

			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "mClientServiceConnection onServiceDisconnected");
				mClientServiceBinder = null;
			}
		};

		mServerServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "mServerServiceConnection onServiceConnected");
				mServerServiceBinder = (ServerServiceBinder) service;
				mServerServiceBinder.registCallback(ManagerService.this);
				startServer();
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "mServerServiceConnection onServiceDisconnected");
				mServerServiceBinder = null;
			}
		};

		mAdvertiseServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG,
						"mAdvertiseServiceConnection onAdvertiseServiceConnected");
				mAdvertiseServiceBinder = (AdvertiseServiceBinder) service;
				mAdvertiseServiceBinder.registCallback(ManagerService.this);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "mAdvertiseServiceConnection onServiceDisconnected");
				mAdvertiseServiceBinder.UnregistCallback();
				mAdvertiseServiceBinder = null;
			}
		};

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				findServer();
				break;
			}
		}
	};

	public void onEvent(Integer event) {
		Logger.d(TAG, "onEvent:" + event);
		switch (event) {
		case Util.FIND_SERVER:
			// a new server was found, so update server list
			mServerMap = mAdvertiseServiceBinder.getServerMap();
			mCallback.updateServer(mServerMap.keySet().iterator().next());
			break;
		}
	}

	private void sendMess(String message) {
		if (isServer) {
			mServerServiceBinder.send(message.getBytes());
		} else {
			mClientServiceBinder.sendMessage(message);
		}
	}

	private void bindAdvertiseService() {
		if (mAdvertiseServiceBinder == null) {
			boolean bindAdvertise = bindService(new Intent(
					getApplicationContext(), AdvertiseService.class),
					mAdvertiseServiceConnection, Context.BIND_AUTO_CREATE);
			Logger.d(TAG, "bindAdvertise:" + bindAdvertise);
		}
	}

	private void bindClientService() {
		if (mClientServiceBinder == null) {
			boolean bindClient = bindService(new Intent(mContext,
					ClientService.class), mClientServiceConnection,
					Context.BIND_AUTO_CREATE);
			Logger.d(TAG, "bindServer:" + bindClient);
		}
	}

	private void bindServerService() {
		if (mServerServiceBinder == null) {
			Intent i = new Intent();
			i.setClass(mContext, ServerService.class);
			boolean bindServer = bindService(i, mServerServiceConnection,
					Context.BIND_AUTO_CREATE);
			Logger.d(TAG, "bindServer:" + bindServer);
		}
	}

	private void startServer() {
		if (mServerServiceBinder != null) {
			mServerServiceBinder.startServer();
		}

	}

	private void soptServerService() {
		if (mServerServiceBinder != null) {
			mServerServiceBinder.stopServer();
		}
	}

	private void connectServer(String serverName) {
		if (mClientServiceBinder != null) {
			String ip = mServerMap.get(serverName);
			if (ip != null) {
				mClientServiceBinder.connect(ip, Util.PORT);
			}
		}
	}

	private void findServer() {
		if (mAdvertiseServiceBinder != null) {
			mAdvertiseServiceBinder.find();
		}
	}

	@Override
	public void receiveMessage(byte[] receMeg, int size) {
		mCallback.receiveMessage(receMeg, size);

	}

	@Override
	public void serverCreated() {
		mAdvertiseServiceBinder.serverStarted(mServerName);
		mAdvertiseServiceBinder.advertise();
	}

	@Override
	public void serverFound(String serverName) {
		Logger.d(TAG,"find server:"+serverName);
		// a new server was found, so update server list
		mServerMap = mAdvertiseServiceBinder.getServerMap();
		mCallback.updateServer(serverName);
	}

}
