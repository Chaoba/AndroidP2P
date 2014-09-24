package com.chaoba.p2p.interf;

import com.chaoba.p2p.interf.IManagerServiceCallback;

public interface IManagerService extends IFatherService{
	public void startServer(String serverName,boolean openFileServer);

	public void stopServer();

	public void findServer();

	public void connectServer(String serverName,boolean connectFileServer);

	public void unConnectServer();

	public void registCallback(IManagerServiceCallback callback);

	public void unRegistCallback();

}
