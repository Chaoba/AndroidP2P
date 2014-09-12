package com.chaoba.p2p.interf;

public interface IServerService {

	public void startServer();
	public void stopServer();
	public void send(String sendMessage);
	public void registCallback(IServerServiceCallback callback);
}
