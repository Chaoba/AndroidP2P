package com.chaoba.p2p.interf;

public interface IClientServiceCallback {
	public void receiveClientMessage(byte[] buffer, int size);
	public void connectToServer();
}
