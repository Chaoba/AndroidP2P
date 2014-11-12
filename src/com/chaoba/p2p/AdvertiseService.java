package com.chaoba.p2p;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.chaoba.p2p.interf.IAdvertiseService;
import com.chaoba.p2p.interf.IAdvertiseServiceCallback;
import com.chaoba.p2p.utils.Logger;

public class AdvertiseService extends Service {
	private static final String TAG = "AdvertiseService";
	private static final int FIND_MESSAGE=0;
	private static final int ADVERTISE_MESSAGE=1;
	private static final int LISTEN_MESSAGE=2;
	private static final int FIND_DELAY=3000;
	private Context mContext;
	private HandlerThread mBackgroundHandlerThread;
	private BackgroundHandler mBackgroundHandler;
	private MulticastLock multicastLock;
	private MulticastSocket multicastSocket;
	private InetAddress group;
	private HandlerThread mListenHandlerThread;
	private ListenHandler mListenHandler;
	private static final int MULTICAST_PORT = 5111;
	private static final String GROUP_IP = "239.5.0.7";
	private static final String FIND = "FIND";
	private static final String ADVERTISE = "ADVERTISE";
	private HashMap<String, String> mServerMap = new HashMap<String, String>();
	private String mServerName;
	private boolean mListening=true;
	private IAdvertiseServiceCallback mCallback;
	@Override
	public void onCreate() {
		super.onCreate();
		Logger.d(TAG,"oncreate");
		mContext = this;
		mBackgroundHandlerThread = new HandlerThread(TAG);
		mBackgroundHandlerThread.start();
		mBackgroundHandler = new BackgroundHandler(
				mBackgroundHandlerThread.getLooper());

		mListenHandlerThread = new HandlerThread(TAG + "/listen");
		mListenHandlerThread.start();
		mListenHandler = new ListenHandler(mListenHandlerThread.getLooper());

		allowMulticast();
		try {
			multicastSocket = new MulticastSocket(MULTICAST_PORT);
			multicastSocket.setLoopbackMode(true);
			group = InetAddress.getByName(GROUP_IP);
			multicastSocket.joinGroup(group);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mListenHandler.sendEmptyMessage(LISTEN_MESSAGE);
	}

	@Override
	public void onDestroy() {
		Logger.d(TAG,"onDestroy");
		if(multicastSocket!=null&&!multicastSocket.isClosed()){
			multicastSocket.close();
		}
		mBackgroundHandler.removeMessages(FIND_MESSAGE);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new AdvertiseServiceBinder();
	}
	
	public void onEvent(Integer event) {
		Logger.d(TAG, "onEvent:" + event);
	}
	
	public class AdvertiseServiceBinder extends Binder implements
			IAdvertiseService {

		@Override
		public void advertise() {
			mBackgroundHandler.sendEmptyMessage(ADVERTISE_MESSAGE);

		}

		@Override
		public void listen() {
			mListenHandler.sendEmptyMessage(LISTEN_MESSAGE);
		}

		@Override
		public void stopListen() {
			mListening=false;
			multicastSocket.close();
		}

		@Override
		public void find() {
			mBackgroundHandler.sendEmptyMessage(FIND_MESSAGE);

		}

		@Override
		public void serverStarted(String serverName) {
			mServerName=serverName;
			
		}

		@Override
		public void serverStoped() {
			mServerName=null;
			
		}

		@Override
		public HashMap getServerMap() {
			return mServerMap;
		}

		@Override
		public void registCallback(IAdvertiseServiceCallback callback) {
			mCallback=callback;
			
		}

		@Override
		public void UnregistCallback() {
			mCallback=null;
			
		}

	}

	public class BackgroundHandler extends Handler {

		public BackgroundHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			Logger.d(TAG, "handlemsessage:" + msg.what);
			switch (msg.what) {
			case ADVERTISE_MESSAGE:
				advertise();
				break;
			case FIND_MESSAGE:
				find();
				mBackgroundHandler.sendEmptyMessageDelayed(FIND_MESSAGE,FIND_DELAY);
			default:
				break;
			}
		}
	}

	public class ListenHandler extends Handler {

		public ListenHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			Logger.d(TAG, "handlemsessage:" + msg.what);
			switch (msg.what) {
			case LISTEN_MESSAGE:
				mListening=true;
				Listening();
				break;
			default:
				break;
			}
		}
	}

	private void allowMulticast() {
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		multicastLock = wifiManager.createMulticastLock("multicast.test");
		multicastLock.acquire();
	}

	private void find() {
		mServerMap.clear();
		byte[] adv = FIND.getBytes();
		DatagramPacket packet = new DatagramPacket(adv, adv.length, group,
				MULTICAST_PORT);
		try {
			multicastSocket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Logger.d(TAG, ">>>send FIND packet ok");

	}

	private void advertise() {
		//not start server,just return
		if(TextUtils.isEmpty(mServerName)){
			return;
		}
		byte[] adv = (ADVERTISE + File.separator + mServerName).getBytes();
		DatagramPacket packet = new DatagramPacket(adv, adv.length, group,
				MULTICAST_PORT);
		try {
			multicastSocket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Logger.d(TAG, ">>>send ADVERTISE packet ok");

	}

	private void Listening() {
		while (mListening) {
			byte[] receiveData = new byte[256];
			DatagramPacket packet = new DatagramPacket(receiveData,
					receiveData.length);
			try {
				Logger.d(TAG, "listening");
				multicastSocket.receive(packet);
			} catch (IOException e) {
				Logger.d(TAG,"stop listening thread");
				break;
			}
			String packetIpAddress = packet.getAddress().toString();
			packetIpAddress = packetIpAddress.substring(1,
					packetIpAddress.length());
			Logger.d(TAG, "target ip address: " + packetIpAddress);

			StringBuilder packetContent = new StringBuilder();
			for (int i = 0; i < receiveData.length; i++) {
				if (receiveData[i] == 0) {
					break;
				}
				packetContent.append((char) receiveData[i]);
			}
			String content = packetContent.toString();
			Logger.d(TAG, "target command is: " + content);
			if (content.equals(FIND)) {
				//receive find request, advertise myself
				mBackgroundHandler.sendEmptyMessage(ADVERTISE_MESSAGE);
			} else if (content.startsWith(ADVERTISE)) {
				String[] names = content.split(File.separator);
				if (names.length > 1) {
					//find a server,notify manager.
					mServerMap.put(names[1], packetIpAddress);
					if(mCallback!=null){
						mCallback.serverFound(names[1]);
						mBackgroundHandler.removeMessages(FIND_MESSAGE);
					}
				}
			}
		}

	}
}
