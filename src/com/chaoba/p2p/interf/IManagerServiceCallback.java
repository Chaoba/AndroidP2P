package com.chaoba.p2p.interf;
public interface IManagerServiceCallback  extends IFatherCallback{ 
	public void serverCreated();
	public void updateServer(String serverName);
	public void connectToServer();
	public void acceptClient();
}  