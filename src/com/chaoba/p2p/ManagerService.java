package com.chaoba.p2p;

import java.util.HashMap;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.chaoba.p2p.AdvertiseService.AdvertiseServiceBinder;
import com.chaoba.p2p.ClientService.ClientServiceBinder;
import com.chaoba.p2p.ServerService.ServerServiceBinder;
import com.chaoba.p2p.interf.IManagerService;
import com.chaoba.p2p.utils.Logger;
import com.chaoba.p2p.utils.Util;

import de.greenrobot.event.EventBus;

public class ManagerService extends Service {
	private static final String TAG = "ManagerService";
	private Context mContext;
	private ServiceConnection mClientServiceConnection;
	protected ClientServiceBinder mClientServiceBinder;
	
	private ServiceConnection mServerServiceConnection;
	protected ServerServiceBinder mServerServiceBinder;
	
	private ServiceConnection mAdvertiseServiceConnection;
	protected AdvertiseServiceBinder mAdvertiseServiceBinder;
	private EventBus eventBus;
	private HashMap<String, String> mServerMap = new HashMap<String, String>();
	
	@Override
	public IBinder onBind(Intent intent) {
		return new ManagerServiceBinder();
	}

	public class ManagerServiceBinder extends Binder implements IManagerService {

		@Override
		public void startServer() {
			// TODO Auto-generated method stub

		}

		@Override
		public void stopServer() {
			// TODO Auto-generated method stub

		}

		@Override
		public void findServer() {
			// TODO Auto-generated method stub

		}

	}

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		eventBus = new EventBus();
		eventBus.register(this);
		
		mClientServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "mClientServiceConnection onServiceConnected");
				mClientServiceBinder = (ClientServiceBinder) service;
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "mClientServiceConnection onServiceDisconnected");
				mClientServiceBinder=null;
			}
		};

		mServerServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "mServerServiceConnection onServiceConnected");
				mServerServiceBinder = (ServerServiceBinder) service;
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "mServerServiceConnection onServiceDisconnected");
				mServerServiceBinder=null;
			}
		};
		
		mAdvertiseServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "mAdvertiseServiceConnection onAdvertiseServiceConnected");
				mAdvertiseServiceBinder = (AdvertiseServiceBinder) service;
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "mAdvertiseServiceConnection onServiceDisconnected");
				mAdvertiseServiceBinder=null;
			}
		};

	}


	@Override
	public void onDestroy() {
		eventBus.unregister(this);
		super.onDestroy();
	}
	
    public void onEvent(Integer event) {
    	Logger.d(TAG,"onEvent:"+event);
        switch(event){
        	case Util.FIND_SERVER:
        		mServerMap=mAdvertiseServiceBinder.getServerMap();
        		break;
        }
    }
	private void bindAdvertiseService() {
		if(mAdvertiseServiceBinder==null){
		bindService(
				new Intent(getApplicationContext(), AdvertiseService.class),
				mAdvertiseServiceConnection, Context.BIND_AUTO_CREATE);
		}
	}

	private void bindClientService() {
		if(mClientServiceBinder==null){
		bindService(new Intent(getApplicationContext(), ClientService.class),
				mClientServiceConnection, Context.BIND_AUTO_CREATE);
		}
	}
	
	private void bindServerService() {
		if(mServerServiceBinder==null){
		bindService(new Intent(getApplicationContext(), ServerService.class),
				mServerServiceConnection, Context.BIND_AUTO_CREATE);
		}
	}
	
	private void startServer(String serverName){
		if(mServerServiceBinder!=null){
			mServerServiceBinder.startServer();
		}
		
	}
	
	private void soptServer(String serverName){  
		if(mServerServiceBinder!=null){
			mServerServiceBinder.stopServer();
		}
	}
	
	private void connectServer(String serverName){
		if(mClientServiceBinder!=null){
			String ip=mServerMap.get(serverName);
			if(ip!=null){
			mClientServiceBinder.connect(ip, Util.PORT);
			}
		}
	}
	
	private void findServer(){
		
	}
	
}
