package com.chaoba.p2p.wifidirect.interf;

import com.chaoba.p2p.interf.Receiver;


public interface IP2PmanagerListener  extends Receiver{
	public void onServerFound(String serverName);

	public void onServerCreated();

	public void onServerConnected();

	public void onClientConnected(String clientName);

	public void onConnectionLost();
}
