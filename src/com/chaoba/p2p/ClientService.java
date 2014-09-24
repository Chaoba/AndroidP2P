package com.chaoba.p2p;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.chaoba.p2p.ServerService.MessageHandler;
import com.chaoba.p2p.interf.IClientService;
import com.chaoba.p2p.interf.IClientServiceCallback;
import com.chaoba.p2p.utils.Logger;
import com.chaoba.p2p.utils.Util;

public class ClientService extends FatherService {

	private static final String TAG = "ClientService";
	private String mIp;
	private int mMessageServerPort, mFileServerPort;
	private static final int CONNECT_MESSAGE_SERVER = 0;
	private static final int CONNECT_FILE_SERVER = 1;
	private static final int SEND_MESSAGE = 2;
	private static final int SEND_FILE = 3;
	private static final int BEGIN_LISTENING_FILE = 0;
	private static final int BEGIN_LISTENING_MESSAGE = 0;

	private HandlerThread mBackgroundHandlerThread;
	private BackgroundHandler mBackgroundHandler;
	private HandlerThread mMessageHandlerThread;
	private MessageHandler mMessageHandler;
	private IClientServiceCallback mCallback;
	private HandlerThread mFileHandlerThread;
	private FileHandler mFileHandler;
	static Socket mMessageClient;
	static Socket mFileClient;
	private InputStream mMessageInputStream, mFileInputStream;
	private OutputStream mMessageOutputStream, mFileOutputStream;

	@Override
	public void onCreate() {
		super.onCreate();
		Logger.d(TAG, "oncreate");
		mBackgroundHandlerThread = new HandlerThread(TAG);
		mBackgroundHandlerThread.start();
		mBackgroundHandler = new BackgroundHandler(
				mBackgroundHandlerThread.getLooper());

		mMessageHandlerThread = new HandlerThread(TAG + "/message");
		mMessageHandlerThread.start();
		mMessageHandler = new MessageHandler(mMessageHandlerThread.getLooper());

		mFileHandlerThread = new HandlerThread(TAG + "/file");
		mFileHandlerThread.start();
		mFileHandler = new FileHandler(mFileHandlerThread.getLooper());
	}

	@Override
	public void onDestroy() {
		closeSocket();
		Logger.d(TAG, "onDestroy");
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
			case CONNECT_MESSAGE_SERVER:
				connectMessageSocket(mIp, mMessageServerPort);
				break;
			case CONNECT_FILE_SERVER:
				connectToFileSocket(mIp, mFileServerPort);
			case SEND_MESSAGE:
				String s = (String) msg.obj;
				try {
					mMessageOutputStream.write(s.getBytes());
					mMessageOutputStream.flush();
				} catch (IOException e) {
					Logger.e(TAG, "send message error:" + s);
				}
				break;
			case SEND_FILE:
				startSendFile((String) msg.obj);
				break;
			default:
				break;
			}
		}
	}

	public class MessageHandler extends Handler {

		public MessageHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			Logger.d(TAG, "handlemsessage:" + msg.what);
			switch (msg.what) {
			case BEGIN_LISTENING_MESSAGE:
				beginListeningMessage();
				break;
			default:
				break;
			}
		}
	}

	public class FileHandler extends Handler {

		public FileHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			Logger.d(TAG, "handlemsessage:" + msg.what);
			switch (msg.what) {
			case BEGIN_LISTENING_FILE:
				beginListeningFile();
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
		public void connectMessageServer(String ip, int port) {
			mIp = ip;
			mMessageServerPort = port;
			mBackgroundHandler.sendEmptyMessage(CONNECT_MESSAGE_SERVER);
		}

		@Override
		public void connectFileServer(String ip, int port) {
			mFileServerPort = port;
			mBackgroundHandler.sendEmptyMessage(CONNECT_FILE_SERVER);
		}

		@Override
		public void sendMessage(String message) {
			startSendMessage(message);
		}

		@Override
		public void registCallback(IClientServiceCallback callback) {
			mCallback = callback;

		}

		@Override
		public void unregistCallback() {
			mCallback = null;

		}

		public void unConnect() {
			closeSocket();

		}

		@Override
		public void sendFiles(ArrayList<String> filePaths) {
			Message msg = mBackgroundHandler.obtainMessage();
			msg.obj = filePaths.get(0);
			msg.what = SEND_FILE;
			mBackgroundHandler.sendMessage(msg);
		}

	}

	public void connectMessageSocket(String site, int port) {
		try {
			mMessageClient = new Socket(site, port);
			Logger.d(TAG, "Client is created! site:" + site + " port:" + port);
			mCallback.connectToServer();
			mMessageInputStream = mMessageClient.getInputStream();
			mMessageOutputStream = mMessageClient.getOutputStream();
			mMessageHandler.sendEmptyMessage(BEGIN_LISTENING_MESSAGE);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void connectToFileSocket(String site, int port) {
		try {
			mFileClient = new Socket(site, port);
			Logger.d(TAG, "mFileClient is created! site:" + site + " port:"
					+ port);
			mFileInputStream = mFileClient.getInputStream();
			mFileOutputStream = mFileClient.getOutputStream();
			mFileHandler.sendEmptyMessage(BEGIN_LISTENING_FILE);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void beginListeningMessage() {
		boolean listening = true;
		byte[] buffer = new byte[Util.BUFFER_SIZE];
		int n = 0;
		while (listening) {
			try {
				Logger.d(TAG, "listening:");
				while (!mMessageClient.isClosed()) {
					n = mMessageInputStream.read(buffer);
					String s = new String(buffer, 0, n);
					if (!super.handleReceivedMessage(s)) {
						mCallback.receiveMessage(s);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				listening = false;
			}
		}
	}

	public void beginListeningFile() {
		boolean listening = true;
		byte[] buffer = new byte[Util.FILE_BUFFER_SIZE];
		int n = 0;
		while (listening) {
			try {
				Logger.d(TAG, "listening:");
				while (!mFileClient.isClosed()) {
					n = mFileInputStream.read(buffer);
					Logger.d(TAG, "read mFileInputStream n:" + n);
					handleReceivedFile(buffer, n);
				}
			} catch (IOException e) {
				e.printStackTrace();
				listening = false;
			}
		}
	}

	public void closeSocket() {
		try {
			if (mMessageClient.isConnected())
				mMessageClient.close();
			if (mFileClient.isConnected())
				mFileClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void updateFilePercent(String fileName, int percent) {
		mCallback.updateFilePercent(fileName, percent);
	}

	@Override
	protected OutputStream getOutputStream() {
		return mFileOutputStream;
	}

	@Override
	protected void startSendMessage(String message) {
		Message msg = mBackgroundHandler.obtainMessage();
		msg.what = SEND_MESSAGE;
		msg.obj = message;
		mBackgroundHandler.sendMessageDelayed(msg, Util.SEND_MESSAGE_DELAY);

	}

}
