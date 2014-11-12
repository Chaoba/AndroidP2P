package com.chaoba.p2p;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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
import com.chaoba.p2p.interf.IClientServiceCallback;
import com.chaoba.p2p.interf.IManagerService;
import com.chaoba.p2p.interf.IManagerServiceCallback;
import com.chaoba.p2p.interf.IServerServiceCallback;
import com.chaoba.p2p.utils.Logger;
import com.chaoba.p2p.utils.Util;

public class ManagerService extends Service implements IServerServiceCallback,
		IAdvertiseServiceCallback, IClientServiceCallback {
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
	private boolean isServer, mBindServer, mBindAdvertise, mBindClient;
	private Iterator<String> mFilesToSent;
	public boolean mOpenFileServer;
	public boolean mConnectFileServer;

	@Override
	public IBinder onBind(Intent intent) {
		Logger.d(TAG, "onbind");
		return new ManagerServiceBinder();
	}

	public class ManagerServiceBinder extends Binder implements IManagerService {
		@Override
		public void startServer(String serverName, boolean openFileServer) {
			Logger.d(TAG, "startserver:" + serverName);
			isServer = true;
			mServerName = serverName;
			mOpenFileServer = openFileServer;
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
		public void connectServer(String serverName, boolean connectFileServer) {
			isServer = false;
			mServerNameToConnect = serverName;
			mConnectFileServer = connectFileServer;
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

		@Override
		public void unConnectServer() {
			ManagerService.this.unConnectServer();
		}

		@Override
		public void sendFiles(ArrayList<String> filePaths) {
			mFilesToSent = filePaths.iterator();
			sendNextFile();
		}

		@Override
		public void close() {
			// TODO Auto-generated method stub
			
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
				mClientServiceBinder.registCallback(ManagerService.this);
				// if bind to clientService, means user want to connect one
				// server, so connect it directly.
				mBindClient = true;
				if (mServerNameToConnect != null) {
					connectServer(mServerNameToConnect);
				}

			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "mClientServiceConnection onServiceDisconnected");
				mClientServiceBinder.unregistCallback();
				mClientServiceBinder = null;
				mBindClient = false;
			}
		};

		mServerServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "mServerServiceConnection onServiceConnected");
				mServerServiceBinder = (ServerServiceBinder) service;
				mServerServiceBinder.registCallback(ManagerService.this);
				startServer(mOpenFileServer);
				mBindServer = true;
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "mServerServiceConnection onServiceDisconnected");
				mServerServiceBinder = null;
				mBindServer = false;
			}
		};

		mAdvertiseServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG,
						"mAdvertiseServiceConnection onAdvertiseServiceConnected");
				mAdvertiseServiceBinder = (AdvertiseServiceBinder) service;
				mAdvertiseServiceBinder.registCallback(ManagerService.this);
				mBindAdvertise = true;
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "mAdvertiseServiceConnection onServiceDisconnected");
				mAdvertiseServiceBinder.UnregistCallback();
				mAdvertiseServiceBinder = null;
				mBindAdvertise = false;
			}
		};

	}

	public void sendNextFile() {
		if (mFilesToSent.hasNext()) {
			String filePath = mFilesToSent.next();
			Logger.d(TAG, "send next file:" + filePath);
			ArrayList<String> file = new ArrayList<String>();
			file.add(filePath);
			if (isServer) {
				mServerServiceBinder.sendFiles(file);
			} else {
				mClientServiceBinder.sendFiles(file);
			}
		} else {
			mFilesToSent = null;
		}

	}

	@Override
	public void onDestroy() {
		Logger.d(TAG, "onDestroy");
		if (mBindAdvertise) {
			unbindService(mAdvertiseServiceConnection);
		}
		if (mBindClient) {
			unbindService(mClientServiceConnection);
		}
		if (mBindServer) {
			unbindService(mServerServiceConnection);
		}
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

	private void sendMess(String message) {
		if (isServer) {
			mServerServiceBinder.sendMessage(message);
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

	private void startServer(boolean openFileServer) {
		if (mServerServiceBinder != null) {
			mServerServiceBinder.startServer(openFileServer);
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
				mClientServiceBinder
						.connectMessageServer(ip, Util.MESSAGE_PORT);
				if (mConnectFileServer) {
					mClientServiceBinder.connectFileServer(ip, Util.FILE_PORT);
				}
			}
		}
	}

	private void unConnectServer() {
		if (mClientServiceBinder != null) {
			mClientServiceBinder.unConnect();
		}
	}

	private void findServer() {
		if (mAdvertiseServiceBinder != null) {
			mAdvertiseServiceBinder.find();
		}
	}

	@Override
	public void receiveMessage(String message) {
		mCallback.receiveMessage(message);
	}

	@Override
	public void serverCreated() {
		mAdvertiseServiceBinder.serverStarted(mServerName);
		mAdvertiseServiceBinder.advertise();
		mCallback.serverCreated();
	}

	@Override
	public void serverFound(String serverName) {
		Logger.d(TAG, "find server:" + serverName);
		// a new server was found, so update server list
		mServerMap = mAdvertiseServiceBinder.getServerMap();
		mCallback.updateServer(serverName);
	}

	@Override
	public void connectToServer() {
		mCallback.connectToServer();
		if (mBindAdvertise) {
			unbindService(mAdvertiseServiceConnection);
			mBindAdvertise=false;
		}
	}

	@Override
	public void acceptClient() {
		mCallback.acceptClient();
		if (mBindAdvertise) {
			unbindService(mAdvertiseServiceConnection);
			mBindAdvertise=false;
		}
	}

	@Override
	public void updateFilePercent(String FileName, int percent) {
		mCallback.updateFilePercent(FileName, percent);
		if (percent == 100 && mFilesToSent != null) {
			sendNextFile();
		}
	}

}
