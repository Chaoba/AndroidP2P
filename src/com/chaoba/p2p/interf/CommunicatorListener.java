package com.chaoba.p2p.interf;


public interface CommunicatorListener extends Receiver{

	public void startServer(String serverName,boolean openFileServer);

	public void stopServer();

	public void connectServer(String serverNameOrIp,boolean connectFileServer);

	public void unConnectServer();

	public void registListener(CommunicatorManagerListener lister);

	public void unRegistListener();
}