package com.chaoba.p2p.wifidirect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import android.os.Environment;
import android.util.Log;

import com.chaoba.p2p.utils.Logger;
import com.chaoba.p2p.utils.Util;
import com.chaoba.p2p.wifidirect.interf.FileSender;
import com.chaoba.p2p.wifidirect.interf.Receiver;

public class FileSocket implements FileSender {
	private static final String TAG = "FileSocket";
	ServerSocket mServerSocket;
	Socket socket;
	InputStream input;
	OutputStream output;
	Receiver mReceiver;
	private InetAddress mAddress;
	private boolean mIsServer;
	private String mCurrentFileName;
	private long mCurrentFileSize;
	private FileOutputStream mFileOutPutStream;
	private long mCurrentSRFileSize;
	private long startTime;
	private long useTime;

	public FileSocket(Receiver receiver, InetAddress serverAddress,
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
					mServerSocket = new ServerSocket(Util.FILE_PORT);
					socket = mServerSocket.accept();
					Log.d(TAG, "accept client socket");
				} else {
					socket = new Socket();
					socket.bind(null);
					socket.connect(
							new InetSocketAddress(mAddress.getHostAddress(),
									Util.FILE_PORT), 5000);
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
		byte[] buffer = new byte[Util.FILE_BUFFER_SIZE];
		int n = 0;
		while (!socket.isClosed()) {
			n = input.read(buffer);
			Logger.d(TAG, "read input n:" + n);
			if(n>0)
			handleReceivedFile(buffer, n);
		}
	}

	@Override
	public void sendMessage(String message) {

	}

	@Override
	public void sendFiles(ArrayList<String> filePaths) {
		String filePath = filePaths.get(0);
		Logger.d(TAG, "send sendFiles:" + filePath);
		File file = new File(filePath);
		long mCurrentFileSize = file.length();
		long mCurrentSRFileSize = 0;

		if (file.exists()) {
			startTime = System.currentTimeMillis();
			byte[] buffer = new byte[Util.FILE_BUFFER_SIZE];
			try {
				FileInputStream mFileInPutStream = new FileInputStream(file);
				while (true) {
					int n = mFileInPutStream.read(buffer);
					if (n > 0) {
						output.write(buffer, 0, n);
						output.flush();
						mCurrentSRFileSize += n;
						Logger.d(TAG, "mCurrentSRFileSize:"
								+ mCurrentSRFileSize + "::n:" + n);
						int percent = (int) (mCurrentSRFileSize * 100 / mCurrentFileSize);
						mReceiver.onFilePercentUpdated(filePath, percent);
						if (percent == 100) {
							mFileInPutStream.close();
							break;
						}
					} else {
						mFileInPutStream.close();
						mReceiver.onFilePercentUpdated(filePath, 100);
						break;
					}
				}
			} catch (IOException e) {
				Logger.e(TAG, e);
			}

			calculateTime();
		} else {
			Logger.d(TAG, "file not exist");
		}

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

	@Override
	public boolean prepareToReceiveFile(String fileName, long fileSize) {
		Logger.d(TAG, "prepareToReceiveFile:" + fileName);
		startTime = System.currentTimeMillis();
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			File mSdCardDir = Environment.getExternalStorageDirectory();
			File path = new File(mSdCardDir.getAbsolutePath() + File.separator
					+ Util.SAVE_PATH);
			if (!path.exists()) {
				if (!path.mkdir()) {
					path = mSdCardDir;
				}
			}
			mCurrentSRFileSize = 0;
			mCurrentFileName = path.getAbsolutePath() + File.separator
					+ fileName;
			mCurrentFileSize = fileSize;
			File receiveFile = Util.getSaveFile(mCurrentFileName);
			try {
				mFileOutPutStream = new FileOutputStream(receiveFile);
			} catch (FileNotFoundException e) {
				Logger.e(TAG, e);
				return false;
			}
			return true;
		} else {
			return false;
		}

	}

	private void handleReceivedFile(byte[] buffer, int size) {
		mCurrentSRFileSize += size;
		int percent = (int) (mCurrentSRFileSize * 100 / mCurrentFileSize);
		Logger.d(TAG, "mCurrentSRFileSize:" + mCurrentSRFileSize + " ::prcent"
				+ percent);
		mReceiver.onFilePercentUpdated(mCurrentFileName, percent);
		try {
			mFileOutPutStream.write(buffer, 0, size);
			if (percent == 100) {
				mFileOutPutStream.close();
				calculateTime();
			}
		} catch (IOException e) {
			try {
				mFileOutPutStream.close();
			} catch (IOException e1) {
			}
		}

	}

	private void calculateTime() {
		useTime = (System.currentTimeMillis() - startTime) / 1000;
		if (useTime > 0) {
			Logger.d(TAG, "send time:"
					+ (System.currentTimeMillis() - startTime) + "ms");
			Logger.d(
					TAG,
					"send speed:"
							+ ((double) mCurrentFileSize / 1024 / 1024 / (double) useTime)
							+ "M/s,size:"
							+ ((double) mCurrentFileSize / 1024 / 1024) + "M");
		}
	}
}
