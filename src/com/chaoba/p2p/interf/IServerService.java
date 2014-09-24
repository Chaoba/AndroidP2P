package com.chaoba.p2p.interf;

public interface IServerService extends IFatherService{
	public void startServer(boolean openFileServer);
	public void stopServer();
	public void registCallback(IServerServiceCallback callback);
}
