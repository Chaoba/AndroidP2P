package com.chaoba.p2p.interf;
public interface IManagerServiceCallback { 
	public void receiveMessage(String message);
	public void serverCreated();
	public void updateServer(String serverName);
	public void connectToServer();
	public void acceptClient();
}  