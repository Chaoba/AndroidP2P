package com.chaoba.p2p.wifidirect.interf;



public interface IP2PmanagerListener  extends Receiver{
	public void onServerFound(String serverName,boolean isSelf);

	public void onServerCreated();

	public void onServerConnected();

	public void onClientConnected(String clientName);

	public void onConnectionLost();
}
