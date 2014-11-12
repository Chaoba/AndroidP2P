package com.chaoba.p2p.interf;


public interface CommunicatorManagerListener extends Receiver{

	public void onServerFound(String serverName);

	public void onServerCreated();

	public void onServerConnected();

	public void onClientConnected(String clientName);

	public void onConnectionLost();


}