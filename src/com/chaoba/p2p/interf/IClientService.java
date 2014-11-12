package com.chaoba.p2p.interf;

public interface IClientService extends Sender{

	public void connectMessageServer(String ip,int port);
	public void connectFileServer(String ip,int port);
	public void registCallback(IClientServiceCallback callback);
	public void unregistCallback();
}
