package com.chaoba.p2p.wifidirect.interf;

import com.chaoba.p2p.interf.Sender;


public interface ConnectionManager extends Sender{
	public void createServer(String name);
	public void findServer();
	public void connectServer(String name);
	public void disConnectServer();
}
