package com.example.p2p;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.example.p2p.ServerService.BackgroundHandler;
import com.example.p2p.interf.IClientService;
import com.example.p2p.utils.Logger;

public class ClientService extends Service {

	private static final String TAG = "ClientService";
	private String mIp;
	int mPort;
	private Context mContext;
	private HandlerThread mBackgroundHandlerThread;
	private BackgroundHandler mBackgroundHandler;
	private BufferedReader in;
	private PrintWriter out;
	static Socket client;
	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		mBackgroundHandlerThread = new HandlerThread(TAG);
		mBackgroundHandlerThread.start();
		mBackgroundHandler = new BackgroundHandler(
				mBackgroundHandlerThread.getLooper());
	}

	public class BackgroundHandler extends Handler {

		public BackgroundHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			Logger.d(TAG,"handlemsessage:"+msg.what);
			switch (msg.what) {
			case 0:
				SocketClient(mIp,mPort);
				break;
			case 1:
				sendMsg((String) msg.obj);
				Logger.d(TAG, "send ok");
				break;
			default:
				break;
			}
		}
	}
	@Override
	public IBinder onBind(Intent intent) {
		return new ClientServiceBinder();
	}

	public class ClientServiceBinder extends Binder implements
			IClientService {

		@Override
		public void connect(String ip, int port) {
			mIp=ip;
			mPort=port;
			mBackgroundHandler.sendEmptyMessage(0);
		}

		@Override
		public void sendMessage(String message) {
			Message msg=mBackgroundHandler.obtainMessage();
			msg.what=1;
			msg.obj=message;
			mBackgroundHandler.sendMessage(msg);
		}

	}

	

	public void SocketClient(String site, int port) {
		try {
			client = new Socket(site, port);
			Logger.d(TAG, "Client is created! site:" + site + " port:"
					+ port);
			in = new BufferedReader(new InputStreamReader(
					client.getInputStream()));
			out = new PrintWriter(client.getOutputStream());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public void sendMsg(String msg) {
		Logger.d(TAG,"sendmsg:"+msg);
		out.println(msg);
		out.flush();
		Logger.d(TAG,"flush:");
	}

	public void closeSocket() {
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
