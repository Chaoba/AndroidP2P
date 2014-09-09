package com.chaoba.p2p.interf;

public interface IClientService {

	public void connect(String ip,int port);
	public void sendMessage(String message);
}
