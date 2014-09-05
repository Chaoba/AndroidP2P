package com.example.p2p;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import com.example.p2p.AdvertiseService.ListenHandler;
import com.example.p2p.interf.IAdvertiseService;
import com.example.p2p.utils.Logger;

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

public class AdvertiseService extends Service {
	private static final String TAG = "AdvertiseService";
	private Context mContext;
	private HandlerThread mBackgroundHandlerThread;
	private BackgroundHandler mBackgroundHandler;
	private MulticastLock multicastLock;
	private MulticastSocket multicastSocket;
	private InetAddress group;
	private HandlerThread mListenHandlerThread;
	private ListenHandler mListenHandler;
	private static final int MULTICAST_PORT = 5111;
	private static final String GROUP_IP = "224.5.0.7";
	private static final String FIND = "FIND";
	private static final String ADVERTISE = "ADVERTISE";

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		mBackgroundHandlerThread = new HandlerThread(TAG);
		mBackgroundHandlerThread.start();
		mBackgroundHandler = new BackgroundHandler(
				mBackgroundHandlerThread.getLooper());
		
		mListenHandlerThread = new HandlerThread(TAG+"/listen");
		mListenHandlerThread.start();
		mListenHandler = new ListenHandler(
				mListenHandlerThread.getLooper());
		
		allowMulticast();
		try {
			multicastSocket = new MulticastSocket(MULTICAST_PORT);
			multicastSocket.setLoopbackMode(true);
			group = InetAddress.getByName(GROUP_IP);
			multicastSocket.joinGroup(group);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		return new AdvertiseServiceBinder();
	}

	public class AdvertiseServiceBinder extends Binder implements
			IAdvertiseService {

		@Override
		public void advertise() {
			mBackgroundHandler.sendEmptyMessage(0);

		}

		@Override
		public void listen() {
			mListenHandler.sendEmptyMessage(0);
		}

		@Override
		public void stopListen() {
			// TODO Auto-generated method stub

		}

		@Override
		public void find() {
			mBackgroundHandler.sendEmptyMessage(2);

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
			case 0:
				advertise();
				break;
			case 2:
				find();
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
			case 0:
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
		byte[] adv = ADVERTISE.getBytes();
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
		while (true) {
			byte[] receiveData = new byte[256];
			DatagramPacket packet = new DatagramPacket(receiveData,
					receiveData.length);
			try {
				Logger.d(TAG,"listening");
				multicastSocket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			String packetIpAddress = packet.getAddress().toString();
			packetIpAddress = packetIpAddress.substring(1,
					packetIpAddress.length());
			Logger.d(TAG, "packet ip address: " + packetIpAddress);

			StringBuilder packetContent = new StringBuilder();
			for (int i = 0; i < receiveData.length; i++) {
				if (receiveData[i] == 0) {
					break;
				}
				packetContent.append((char) receiveData[i]);
			}
			String content = packetContent.toString();
			Logger.d(TAG, "packet content is: " + content);
			if(content.equals(FIND)){
				mBackgroundHandler.sendEmptyMessage(0);
			}
			// if (ip.equals(packetIpAddress)) {
			// Logger.d(TAG, "find server ip address: " + ip);
			// break;
			// } else {
			// Logger.d(TAG, "not find server ip address, continue …");
			// try {
			// Thread.sleep(1000);
			// } catch (InterruptedException e) {
			// }
			// }
		}

	}
}
