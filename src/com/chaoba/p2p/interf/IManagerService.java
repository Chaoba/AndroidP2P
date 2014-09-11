package com.chaoba.p2p.interf;

import com.chaoba.p2p.interf.IManagerServiceCallback;

public interface IManagerService {
	public void startServer(String serverName);

	public void stopServer();

	public void findServer();

	public void connectServer(String serverName);

	public void registCallback(IManagerServiceCallback callback);

	public void unRegistCallback();

	public void sendMessage(String message);
}
