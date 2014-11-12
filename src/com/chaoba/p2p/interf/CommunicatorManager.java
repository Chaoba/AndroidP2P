package com.chaoba.p2p.interf;


public interface CommunicatorManager extends Sender{

	public void startServer(String serverName,boolean openFileServer);

	public void stopServer();

	public void findServer();

	public void connectServer(String serverName,boolean connectFileServer);

	public void unConnectServer();

	public void registListener(CommunicatorManagerListener lister);

	public void unRegistListener();
}