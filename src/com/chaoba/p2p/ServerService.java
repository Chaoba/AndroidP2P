package com.chaoba.p2p;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

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
public class ServerService extends FatherService {
	private static final String TAG = "ServerService";
	private static final int OPEN_SERVER_MESSAGE = 0;
	private static final int SERVER_CREATED_MESSAGE = 1;
	private static final int SEND_MESSAGE = 2;
	private static final int SEND_FILE = 3;
	private static final int BEGIN_LISTENING_MESSAGE = 0;
	private static final int BEGIN_LISTENING_FILE = 0;

	
	private Context mContext;
	private ServerSocket mMessageSever, mFileServer;
	private HandlerThread mBackgroundHandlerThread;
	private BackgroundHandler mBackgroundHandler;
	private HandlerThread mMessageHandlerThread;
	private MessageHandler mMessageHandler;
	private InputStream mMessageInputStream, mFileInputStream;
	private OutputStream mMessageOutputStream, mFileOutputStream;
	private IServerServiceCallback mCallback;
	private HandlerThread mFileHandlerThread;
	private FileHandler mFileHandler;

	@Override
	public IBinder onBind(Intent intent) {
		return new ServerServiceBinder();
	}

	public class ServerServiceBinder extends Binder implements IServerService {

		@Override
		public void startServer(boolean openFileServer) {
			Message msg = mBackgroundHandler.obtainMessage();
			msg.obj = openFileServer;
			msg.what = OPEN_SERVER_MESSAGE;
			mBackgroundHandler.sendMessage(msg);

		}

		@Override
		public void stopServer() {
			stopServ();
		}

		@Override
		public void sendMessage(String sendMessage) {
			startSendMessage(sendMessage);

		}

		@Override
		public void registCallback(IServerServiceCallback callback) {
			mCallback = callback;
		}

		@Override
		public void sendFiles(ArrayList<String> filePaths) {
			Message msg=mBackgroundHandler.obtainMessage();
			msg.obj=filePaths.get(0);
			msg.what=SEND_FILE;
			mBackgroundHandler.sendMessage(msg);

		}

	}

	@Override
	public void onCreate() {
		super.onCreate();
		Logger.d(TAG, "oncreate");
		mContext = this;
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
		Logger.d(TAG, "onDestroy");
		stopServ();
		super.onDestroy();
	}

	private void stopServ() {
		if (!mMessageSever.isClosed()) {
			try {
				mMessageSever.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (!mFileServer.isClosed()) {
			try {
				mFileServer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public class BackgroundHandler extends Handler {

		public BackgroundHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case OPEN_SERVER_MESSAGE:
				createMessageServer(Util.MESSAGE_PORT);
				if((Boolean) msg.obj){
					createFileServer(Util.FILE_PORT);
				}
				break;
			case SERVER_CREATED_MESSAGE:
				mCallback.serverCreated();
				break;
			case SEND_MESSAGE:
				try {
					String s = (String) msg.obj;
					mMessageOutputStream.write(s.getBytes());
					mMessageOutputStream.flush();
				} catch (IOException e) {
					Logger.e(TAG, "out put stream error");
				}
				break;
			case SEND_FILE:
				startSendFile((String) msg.obj);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
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

	private void createMessageServer(int port) {
		Logger.d(TAG, "creatge message Server");
		try {
			InetAddress address = InetAddress.getByName(Util.getIp(mContext));
			mMessageSever = new ServerSocket(port, 50, address);
			mMessageHandler.sendEmptyMessage(BEGIN_LISTENING_MESSAGE);
			mBackgroundHandler.sendEmptyMessageDelayed(SERVER_CREATED_MESSAGE,
					1000);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createFileServer(int port) {
		Logger.d(TAG, "creatge file Server");
		try {
			InetAddress address = InetAddress.getByName(Util.getIp(mContext));
			mFileServer = new ServerSocket(port, 50, address);
			mFileHandler.sendEmptyMessage(BEGIN_LISTENING_FILE);
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
				Logger.d(TAG, "listening:" + mMessageSever.getInetAddress());
				final Socket socket = mMessageSever.accept();
				Logger.d(TAG, "accept client:");
				mCallback.acceptClient();
				mMessageInputStream = socket.getInputStream();
				mMessageOutputStream = socket.getOutputStream();
				while (!socket.isClosed()) {
					n = mMessageInputStream.read(buffer);
					Logger.d(TAG, "read mMessageInputStream n:" + n);
					String s = new String(buffer, 0, n);
					if (!super.handleReceivedMessage(s)) {
						mCallback.receiveMessage(s);
					}
				}
				Logger.d(TAG, "close");
				socket.close();
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
				Logger.d(TAG,
						"file server listening:" + mFileServer.getInetAddress());
				final Socket socket = mFileServer.accept();
				Logger.d(TAG, "accept file client:");
				mCallback.acceptClient();
				mFileInputStream = socket.getInputStream();
				mFileOutputStream = socket.getOutputStream();
				while (!socket.isClosed()) {
					n = mFileInputStream.read(buffer);
					Logger.d(TAG, "read mFileInputStream n:" + n);
					handleReceivedFile(buffer, n);
				}
				Logger.d(TAG, "close");
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
				listening = false;
			}
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
