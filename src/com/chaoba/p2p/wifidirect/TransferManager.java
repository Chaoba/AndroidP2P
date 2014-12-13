package com.chaoba.p2p.wifidirect;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Environment;
import android.util.Log;

import com.chaoba.p2p.utils.Logger;
import com.chaoba.p2p.utils.MessageBean;
import com.chaoba.p2p.wifidirect.interf.FileSender;
import com.chaoba.p2p.wifidirect.interf.Receiver;
import com.chaoba.p2p.wifidirect.interf.Sender;
import com.google.gson.Gson;

public class TransferManager implements Receiver, Sender {
	private static final String TAG = "TransferManager";
	Receiver mReceiver;
	Sender mMessageSender;
	FileSender mFileSender;
	WifiP2pInfo mInfo;
	Gson gson = new Gson();
	private Iterator<String> mFilesIterrator;
	private String mCurrentFilePath;
	boolean sdCardExist = Environment.getExternalStorageState().equals(
			android.os.Environment.MEDIA_MOUNTED);

	public TransferManager(Receiver re, WifiP2pInfo info) {
		mReceiver = re;
		mInfo = info;
		MessageSocket messageSocket = null;
		FileSocket fileSocket = null;
		if (info.isGroupOwner) {
			Log.d(TAG, "Connected as group owner");
			messageSocket = new MessageSocket(this, null, true);
			fileSocket = new FileSocket(this, null, true);
		} else {
			Log.d(TAG, "Connected as peer");
			messageSocket = new MessageSocket(this,
					info.groupOwnerAddress, false);
			fileSocket = new FileSocket(this,
					info.groupOwnerAddress, false);
		}
		mMessageSender = messageSocket;
		mFileSender=fileSocket;
	}

	@Override
	public void sendMessage(String message) {
		MessageBean bean = new MessageBean();
		bean.message = message;
		mMessageSender.sendMessage(gson.toJson(bean));

	}

	@Override
	public void sendFiles(ArrayList<String> filePaths) {
		mFilesIterrator = filePaths.iterator();
		sendNextFile();

	}

	public void sendNextFile() {
		if (mFilesIterrator!=null&&mFilesIterrator.hasNext()) {
			mCurrentFilePath = mFilesIterrator.next();
			Logger.d(TAG, "send next file:" + mCurrentFilePath);
			File f = new File(mCurrentFilePath);
			if (f.exists()) {
				MessageBean bean = new MessageBean();
				bean.mType = MessageBean.TYPE_COMMAND;
				bean.mCommand = MessageBean.COMMAND_SEND_FILE;
				bean.fileName = f.getName();
				bean.fileSize = f.length();
				mMessageSender.sendMessage(gson.toJson(bean));
			}
		} else {
			mCurrentFilePath = null;
			mFilesIterrator = null;
		}

	}

	private void sendFileContent() {
		ArrayList<String> file = new ArrayList<String>();
		file.add(mCurrentFilePath);
		mFileSender.sendFiles(file);
	}

	@Override
	public void onReceiveMessage(String message) {
		Logger.d(TAG, "receive json:" + message);
		MessageBean bean = gson.fromJson(message, MessageBean.class);
		switch (bean.mType) {
		case MessageBean.TYPE_MESSAGE:
			mReceiver.onReceiveMessage(bean.message);
			break;
		case MessageBean.TYPE_COMMAND:
			switch (bean.mCommand) {
			case MessageBean.COMMAND_SEND_FILE:
				if(mFileSender.prepareToReceiveFile(bean.fileName, bean.fileSize)){
					MessageBean filebean = new MessageBean();
					filebean.mType = MessageBean.TYPE_COMMAND;
					filebean.mCommand = MessageBean.COMMAND_READY_TO_RECEIVE_FILE;
					filebean.fileName = bean.fileName;
					mMessageSender.sendMessage(gson.toJson(filebean));
				}
				break;
			case MessageBean.COMMAND_READY_TO_RECEIVE_FILE:
				sendFileContent();
				break;
			}
			break;
		case MessageBean.TYPE_HEART_BEAT:
			break;
		}

	}

	@Override
	public void onFilePercentUpdated(String fileName, int percent) {
		mReceiver.onFilePercentUpdated(fileName, percent);
		if(percent==100){
			sendNextFile();
		}
	}

	@Override
	public void close() {
		mMessageSender.close();
		mFileSender.close();
	}

}
