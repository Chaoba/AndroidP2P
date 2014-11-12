package com.chaoba.p2p.interf;


public interface Communicator extends Sender{

	public void startServer(String serverName,boolean openFileServer);

	public void stopServer();

	public void connectServer(String serverNameOrIp,boolean connectFileServer);

	public void unConnectServer();

	public void registListener(CommunicatorListener lister);

	public void unRegistListener();
}