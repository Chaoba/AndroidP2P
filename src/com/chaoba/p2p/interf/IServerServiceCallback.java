package com.chaoba.p2p.interf;

public interface IServerServiceCallback {

	public void receiveMessage(byte[] receMeg, int size);
	public void serverCreated();
	
}
