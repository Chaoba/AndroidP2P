package com.chaoba.p2p.wifidirect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import android.util.Log;

import com.chaoba.p2p.utils.Logger;
import com.chaoba.p2p.utils.Util;
import com.chaoba.p2p.wifidirect.interf.Receiver;
import com.chaoba.p2p.wifidirect.interf.Sender;

public class MessageClientSocket implements Sender {
	private static final String TAG = "ClientSocketThread";
	private InetAddress mAddress;
	Receiver mReceiver;
	InputStream input;
	OutputStream output;
	Socket socket;

	public MessageClientSocket(Receiver receiver, InetAddress serverAddress) {
		mReceiver = receiver;
		mAddress = serverAddress;
		new ClientThread().start();
	}

	class ClientThread extends Thread {
		@Override
		public void run() {
			byte[] buffer = new byte[Util.BUFFER_SIZE];
			int n = 0;
			socket = new Socket();
			try {
				socket.bind(null);
				socket.connect(new InetSocketAddress(mAddress.getHostAddress(),
						Util.MESSAGE_PORT), 5000);
				Log.d(TAG, "connected to server");
				input = socket.getInputStream();
				output = socket.getOutputStream();
				while (!socket.isClosed()) {
					n = input.read(buffer);
					Logger.d(TAG, "read input n:" + n);
					String s = new String(buffer, 0, n);
					mReceiver.onReceiveMessage(s);
				}
			} catch (IOException e) {
				Logger.e(TAG, e);
				try {
					socket.close();
				} catch (IOException e1) {
					Logger.e(TAG, e);
				}
				return;
			}
		}
	}

	@Override
	public void sendMessage(String message) {
		Logger.d(TAG, "send message:" + message);
		try {
			output.write(message.getBytes());
		} catch (IOException e) {
			Logger.e(TAG, e);
		}

	}

	@Override
	public void sendFiles(ArrayList<String> filePaths) {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		Logger.d(TAG, "close");
		try {
			socket.close();
		} catch (IOException e) {
			Logger.e(TAG, e);
		}

	}

}
