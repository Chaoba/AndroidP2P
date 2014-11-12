package com.chaoba.p2p.interf;

public interface IServerService extends Sender{
	public void startServer(boolean openFileServer);
	public void stopServer();
	public void registCallback(IServerServiceCallback callback);
}
