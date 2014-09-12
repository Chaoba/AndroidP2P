package com.chaoba.p2p;

import java.io.IOException;
import java.io.InputStream;
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

import com.chaoba.p2p.interf.IClientService;
import com.chaoba.p2p.interf.IClientServiceCallback;
import com.chaoba.p2p.utils.Logger;
import com.chaoba.p2p.utils.Util;

public class ClientService extends Service {

	private static final String TAG = "ClientService";
	private String mIp;
	int mPort;
	private Context mContext;
	private HandlerThread mBackgroundHandlerThread;
	private BackgroundHandler mBackgroundHandler;
	private InputStream in;
	private PrintWriter out;
	private HandlerThread mListenHandlerThread;
	private ListenHandler mListenHandler;
	private boolean mListening;
	private IClientServiceCallback mCallback;
	static Socket client;

	@Override
	public void onCreate() {
		super.onCreate();
		Logger.d(TAG, "oncreate");
		mContext = this;
		mBackgroundHandlerThread = new HandlerThread(TAG);
		mBackgroundHandlerThread.start();
		mBackgroundHandler = new BackgroundHandler(
				mBackgroundHandlerThread.getLooper());

		mListenHandlerThread = new HandlerThread(TAG + "/listen");
		mListenHandlerThread.start();
		mListenHandler = new ListenHandler(mListenHandlerThread.getLooper());
	}

	@Override
	public void onDestroy() {
		if(client.isConnected()){
			closeSocket();
		}
		Logger.d(TAG,"onDestroy");
		super.onDestroy();
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
				SocketClient(mIp, mPort);
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

	public class ListenHandler extends Handler {

		public ListenHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			Logger.d(TAG, "handlemsessage:" + msg.what);
			switch (msg.what) {
			case 0:
				beginListen() ;
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

	public class ClientServiceBinder extends Binder implements IClientService {

		@Override
		public void connect(String ip, int port) {
			mIp = ip;
			mPort = port;
			mBackgroundHandler.sendEmptyMessage(0);
		}

		@Override
		public void sendMessage(String message) {
			Message msg = mBackgroundHandler.obtainMessage();
			msg.what = 1;
			msg.obj = message;
			mBackgroundHandler.sendMessageDelayed(msg,Util.SEND_MESSAGE_DELAY);
		}

		@Override
		public void registCallback(IClientServiceCallback callback) {
			mCallback=callback;
			
		}

		@Override
		public void unregistCallback() {
			mCallback=null;
			
		}

	}

	public void SocketClient(String site, int port) {
		try {
			client = new Socket(site, port);
			Logger.d(TAG, "Client is created! site:" + site + " port:" + port);
			mCallback.connectToServer();
			in = client.getInputStream();
			out = new PrintWriter(client.getOutputStream());
			mListenHandler.sendEmptyMessage(0);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void beginListen() {
		mListening = true;
		byte[] buffer = new byte[Util.BUFFER_SIZE];
		int n = 0;
		while (mListening) {
			try {
				Logger.d(TAG, "listening:");
				while (!client.isClosed()) {
					n = in.read(buffer);
					Logger.d(TAG, "read n:" + n);
					mCallback.receiveClientMessage(buffer, n);
				}
			} catch (IOException e) {
				e.printStackTrace();
				mListening=false;
			}
		}
	}

	public void sendMsg(String msg) {
		Logger.d(TAG, "sendmsg:" + msg);
		out.println(msg);
		out.flush();
		Logger.d(TAG, "flush:");
	}

	public void closeSocket() {
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
