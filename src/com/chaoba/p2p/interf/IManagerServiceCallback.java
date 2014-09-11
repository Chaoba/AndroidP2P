package com.chaoba.p2p.interf;
public interface IManagerServiceCallback { 
	public void receiveMessage(byte[] receMeg, int size);
	public void serverCreated();
	public void updateServer(String serverName);
}  