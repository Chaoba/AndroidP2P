package com.example.p2p;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

import com.example.p2p.interf.IServiceService;
import com.example.p2p.utils.Logger;
import com.example.p2p.utils.Util;

public class ServerService extends Service {
	private static final String TAG = "ServerService";
	private Context mContext;
	private ServerSocket sever;
	private HandlerThread mBackgroundHandlerThread;
	private BackgroundHandler mBackgroundHandler;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	public class ServerServiceBinder extends Binder implements IServiceService {

		@Override
		public void startServer() {
			mBackgroundHandler.sendEmptyMessage(0);
			
		}

		@Override
		public void stopServer() {
			
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
			case 1:
				beginListen();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	}

	private void createServer(int port) {
		Logger.d(TAG,"creatge Server");
		try {
			InetAddress address = InetAddress.getByName(Util.getIp(mContext));
			sever = new ServerSocket(port, 50, address);
			mBackgroundHandler.sendEmptyMessage(1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void beginListen() {
		while (true) {
			try {
				Logger.d(TAG, "listening:"+sever.getInetAddress());
				final Socket socket = sever.accept();
				new Thread(new Runnable() {
					public void run() {
						BufferedReader in;
						try {
							in = new BufferedReader(new InputStreamReader(
									socket.getInputStream(), "UTF-8"));
							PrintWriter out = new PrintWriter(
									socket.getOutputStream());
							while (!socket.isClosed()) {
								String str;
								str = in.readLine();
								Logger.d(TAG, str);
								
//								if (str == null || str.equals("end"))
//									break;
							}
							Logger.d(TAG,"close");
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
