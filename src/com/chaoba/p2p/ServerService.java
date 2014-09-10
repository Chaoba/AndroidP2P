package com.chaoba.p2p;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.chaoba.p2p.interf.IServerService;
import com.chaoba.p2p.interf.IServerServiceCallback;
import com.chaoba.p2p.utils.Logger;
import com.chaoba.p2p.utils.Util;

/**
 * used to create a socket server
 * 
 * @author Liyanshun 2014-9-9
 */
public class ServerService extends Service {
	private static final String TAG = "ServerService";
	private Context mContext;
	private ServerSocket mSever;
	private HandlerThread mBackgroundHandlerThread;
	private BackgroundHandler mBackgroundHandler;
	private HandlerThread mListenHandlerThread;
	private ListenHandler mListenHandler;
	private boolean mListening;
	private InputStream mInputStream;
	private OutputStream mOutputStream;
	private IServerServiceCallback mCallback;
	@Override
	public IBinder onBind(Intent intent) {
		return new ServerServiceBinder();
	}

	public class ServerServiceBinder extends Binder implements IServerService {

		@Override
		public void startServer() {
			mBackgroundHandler.sendEmptyMessage(0);

		}

		@Override
		public void stopServer() {
			mListening = false;
			try {
				mSever.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void send(byte[] sendMessage) {
			try {
				mOutputStream.write(sendMessage);
			} catch (IOException e) {
				Logger.e(TAG, "out put stream error");
			}

		}

		@Override
		public void registCallback(IServerServiceCallback callback) {
			mCallback=callback;
		}

	}

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		mBackgroundHandlerThread = new HandlerThread(TAG);
		mBackgroundHandlerThread.start();
		mBackgroundHandler = new BackgroundHandler(
				mBackgroundHandlerThread.getLooper());

		mListenHandlerThread = new HandlerThread(TAG + "/listen");
		mListenHandlerThread.start();
		mListenHandler = new ListenHandler(mListenHandlerThread.getLooper());
	}

	public class BackgroundHandler extends Handler {

		public BackgroundHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				createServer(8000);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
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
				beginListen();
				break;
			default:
				break;
			}
		}
	}

	private void createServer(int port) {
		Logger.d(TAG, "creatge Server");
		try {
			InetAddress address = InetAddress.getByName(Util.getIp(mContext));
			mSever = new ServerSocket(port, 50, address);
			mListenHandler.sendEmptyMessage(0);
			mCallback.serverCreated();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void beginListen() {
		mListening = true;
		byte[] buffer=new byte[Util.BUFFER_SIZE];
		int n=0;
		while (mListening) {
			try {
				Logger.d(TAG, "listening:" + mSever.getInetAddress());
				final Socket socket = mSever.accept();
				try {
					mInputStream = socket.getInputStream();
					mOutputStream = socket.getOutputStream();
					while (!socket.isClosed()) {
						n=mInputStream.read(buffer);
						mCallback.receiveMessage(buffer, n);
					}
					Logger.d(TAG, "close");
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
