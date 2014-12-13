package com.chaoba.p2p.wifidirect.interf;



public interface ConnectionManager extends Sender{
	public void setName(String name);
	public void findPeer();
	public void connectServer(String name);
	public void disConnectServer();
}
