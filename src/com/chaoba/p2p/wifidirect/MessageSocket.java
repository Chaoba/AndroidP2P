package com.chaoba.p2p.wifidirect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import android.util.Log;

import com.chaoba.p2p.utils.Logger;
import com.chaoba.p2p.utils.Util;
import com.chaoba.p2p.wifidirect.interf.Receiver;
import com.chaoba.p2p.wifidirect.interf.Sender;

public class MessageSocket implements Sender {
	private static final String TAG = "ServerSocketThread";
	ServerSocket mServerSocket;
	Socket socket;
	InputStream input;
	OutputStream output;
	Receiver mReceiver;
	private InetAddress mAddress;
	private boolean mIsServer;

	public MessageSocket(Receiver receiver, InetAddress serverAddress,
			boolean isServer) {
		mReceiver = receiver;
		mAddress = serverAddress;
		mIsServer = isServer;
		new WorkerThread().start();
	}

	class WorkerThread extends Thread {
		@Override
		public void run() {
			try {
				if (mIsServer) {
					mServerSocket = new ServerSocket(Util.MESSAGE_PORT);
					socket = mServerSocket.accept();
					Log.d(TAG, "accept client socket");
				} else {
					socket = new Socket();
					socket.bind(null);
					socket.connect(
							new InetSocketAddress(mAddress.getHostAddress(),
									Util.MESSAGE_PORT), 5000);
				}
				input = socket.getInputStream();
				output = socket.getOutputStream();
				manageInput(socket);
			} catch (IOException e) {
				try {
					if (socket != null && !socket.isClosed())
						socket.close();
				} catch (IOException ioe) {

				}
				Logger.e(TAG, e);
			}
		}

	}

	private void manageInput(Socket socket) throws IOException {
		byte[] buffer = new byte[Util.BUFFER_SIZE];
		int n = 0;
		while (!socket.isClosed()) {
			n = input.read(buffer);
			Logger.d(TAG, "read input n:" + n);
			if(n>0){
			String s = new String(buffer, 0, n);
			mReceiver.onReceiveMessage(s);
			}
		}
	}

	@Override
	public void sendMessage(String message) {
		Logger.d(TAG, "send message:" + message);
		if (output != null) {
			try {
				output.write(message.getBytes());
				output.flush();
			} catch (IOException e) {
				Logger.e(TAG, e);
			}
		}
	}

	@Override
	public void sendFiles(ArrayList<String> filePaths) {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		Logger.d(TAG, "close");
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				Logger.e(TAG, e);
			}
		}
	}

}
