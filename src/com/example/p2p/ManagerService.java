package com.example.p2p;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.p2p.AdvertiseService.AdvertiseServiceBinder;
import com.example.p2p.ClientService.ClientServiceBinder;
import com.example.p2p.ServerService.ServerServiceBinder;
import com.example.p2p.interf.IManagerService;

public class ManagerService extends Service {
	private static final String TAG = "ManagerService";
	private Context mContext;
	private ServiceConnection mClientServiceConnection;
	protected ClientServiceBinder mClientServiceBinder;
	
	private ServiceConnection mServerServiceConnection;
	protected ServerServiceBinder mServerServiceBinder;
	
	private ServiceConnection mAdvertiseServiceConnection;
	protected AdvertiseServiceBinder mAdvertiseServiceBinder;
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
		mClientServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "onServiceConnected");
				mClientServiceBinder = (ClientServiceBinder) service;
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "onServiceDisconnected");
				mClientServiceBinder=null;
			}
		};

		mServerServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "onServiceConnected");
				mServerServiceBinder = (ServerServiceBinder) service;
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "onServiceDisconnected");
				mServerServiceBinder=null;
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
				mAdvertiseServiceBinder=null;
			}
		};

	}

	private void bindAdvertiseService() {
		bindService(
				new Intent(getApplicationContext(), AdvertiseService.class),
				mAdvertiseServiceConnection, Context.BIND_AUTO_CREATE);
	}

	private void bindClientService() {
		bindService(new Intent(getApplicationContext(), ClientService.class),
				mClientServiceConnection, Context.BIND_AUTO_CREATE);
	}
	
	private void startServer(String serverName){
		
	}
	
	private void soptServer(String serverName){  
		
	}
	
	private void connectServer(String serverName){
		
	}
	
	private void findServer(){
		
	}
	
}
