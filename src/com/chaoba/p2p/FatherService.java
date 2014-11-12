package com.chaoba.p2p;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.chaoba.p2p.utils.Logger;
import com.chaoba.p2p.utils.Util;

public abstract class FatherService extends Service {
	private static final String TAG = "FatherService";
	private static final int SEND_FILE_MESSAGE = 0;
	protected Context mContext;
	private HandlerThread mSendFileHandlerThread;
	private SendFileHandler mSendFileHandler;
	private long mCurrentFileSize, mCurrentSRFileSize;
	private String mCurrentFileName;
	private FileOutputStream mFileOutPutStream = null;
	private FileInputStream mFileInPutStream = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		mSendFileHandlerThread = new HandlerThread("file");
		mSendFileHandlerThread.start();
		mSendFileHandler = new SendFileHandler(
				mSendFileHandlerThread.getLooper());
	}

	protected void startSendFile(String filePath) {
		Logger.d(TAG, "startSendFile:" + filePath);
		File file = new File(filePath);
		if (!file.exists()) {
			return;
		}
		mCurrentFileName = filePath;
		mCurrentFileSize = file.length();
		try {
			mFileInPutStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			return;
		}
		String name = file.getName();
		// tell the target will send file and file info
		startSendMessage(Util.SEND_FILE_COMMAND + name + File.separator
				+ file.length());
	}

	/**
	 * hanlde the received message, it may contains some command
	 * 
	 * @param message
	 * @return true if message is handled
	 */
	protected boolean handleReceivedMessage(String message) {
		Logger.d(TAG, "hand message:" + message);
		boolean handled = false;
		if (message.startsWith(Util.SEND_FILE_COMMAND)) {
			handled = true;
			if (prepareToReceiveFile(message.substring(11, message.length()))) {
				startSendMessage(Util.READY_RECEIVE_FILE_COMMAND);
			}
		} else if (message.startsWith(Util.READY_RECEIVE_FILE_COMMAND)) {
			handled = true;
			mSendFileHandler.sendEmptyMessage(SEND_FILE_MESSAGE);
		}
		return handled;
	}

	protected void handleReceivedFile(byte[] buffer, int size) {
		mCurrentSRFileSize += size;
		int percent = (int) (mCurrentSRFileSize * 100 / mCurrentFileSize);
		Logger.d(TAG, "mCurrentSRFileSize:" + mCurrentSRFileSize + " ::prcent"
				+ percent);
		updateFilePercent(mCurrentFileName, percent);
		try {
			mFileOutPutStream.write(buffer, 0, size);
			if (percent == 100) {
				mFileOutPutStream.close();
			}
		} catch (IOException e) {
			try {
				mFileOutPutStream.close();
			} catch (IOException e1) {
			}
		}

	}

	public class SendFileHandler extends Handler {

		public SendFileHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			Logger.d(TAG, "handlemsessage:" + msg.what);
			switch (msg.what) {
			case SEND_FILE_MESSAGE:
				long startTime = System.currentTimeMillis();
				byte[] buffer = new byte[Util.FILE_BUFFER_SIZE];
				mCurrentSRFileSize = 0;
				OutputStream out = getOutputStream();
				while (true) {
					try {
						int n = mFileInPutStream.read(buffer);
						if (n > 0) {
							out.write(buffer, 0, n);
							out.flush();
							mCurrentSRFileSize += n;
							Logger.d(TAG, "mCurrentSRFileSize:"
									+ mCurrentSRFileSize + "::n:" + n);
							int percent = (int) (mCurrentSRFileSize * 100 / mCurrentFileSize);
							updateFilePercent(mCurrentFileName, percent);
							if (percent == 100) {
								mFileInPutStream.close();
								break;
							}
						} else {
							mFileInPutStream.close();
							break;
						}
					} catch (IOException e) {
						break;
					}
				}

				double useTime = (System.currentTimeMillis() - startTime) / 1000;
				Logger.d(TAG, "send time:"
						+ (System.currentTimeMillis() - startTime));
				Logger.d(TAG, "send speed:"
						+ (mCurrentFileSize / 1024 / 1024 / useTime) + ",size:"
						+ (mCurrentFileSize / 1024 / 1024));
				break;
			default:
				break;
			}
		}
	}

	private boolean prepareToReceiveFile(String s) {
		Logger.d(TAG, "prepareToReceiveFile:" + s);
		// fileName/fileSize
		String[] command = s.split(File.separator);
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
			mCurrentFileName = path.getAbsolutePath() + File.separator
					+ command[0];
			mCurrentFileSize = Integer.valueOf(command[1]);
			mCurrentSRFileSize = 0;
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

	/**
	 * notify currently send/received file percent
	 * 
	 * @param fileName
	 * @param percent
	 */
	protected abstract void updateFilePercent(String fileName, int percent);

	/**
	 * get the outputStream which can send stream to another peer;
	 * 
	 * @return
	 */
	protected abstract OutputStream getOutputStream();

	/**
	 * send message to another peer;
	 * 
	 * @param message
	 */
	protected abstract void startSendMessage(String message);

}
