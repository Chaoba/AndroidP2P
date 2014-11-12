package com.chaoba.p2p;

import java.util.ArrayList;

import com.chaoba.p2p.interf.Advertiser;
import com.chaoba.p2p.interf.Communicator;
import com.chaoba.p2p.interf.CommunicatorManager;
import com.chaoba.p2p.interf.CommunicatorManagerListener;

public class CommunicatorManagerImp implements CommunicatorManager{
	private Communicator mCommunicator;
	private Advertiser mAdvertiser;
	private CommunicatorManagerListener mCommunicatorManagerListener;
	@Override
	public void sendMessage(String message) {
		mCommunicator.sendMessage(message);
	}

	@Override
	public void sendFiles(ArrayList<String> filePaths) {
		mCommunicator.sendFiles(filePaths);
		
	}

	@Override
	public void startServer(String serverName, boolean openFileServer) {
		mCommunicator.startServer(serverName, openFileServer);
		
	}

	@Override
	public void stopServer() {
		mCommunicator.stopServer();
		
	}

	@Override
	public void findServer() {
		mAdvertiser.findServer();
		
	}

	@Override
	public void connectServer(String serverName, boolean connectFileServer) {
		mCommunicator.connectServer(serverName, connectFileServer);
		
	}

	@Override
	public void unConnectServer() {
		mCommunicator.unConnectServer();
		
	}

	@Override
	public void registListener(CommunicatorManagerListener lister) {
		mCommunicatorManagerListener=lister;
		
	}

	@Override
	public void unRegistListener() {
		mCommunicatorManagerListener=null;
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
