package com.chaoba.p2p.interf;

import java.util.HashMap;

public interface IAdvertiseService {

	public void advertise();
	public void listen();
	public void stopListen();
	public void find();
	public void serverStarted(String serverName);
	public void serverStoped();
	public HashMap getServerMap();
	public void registCallback(IAdvertiseServiceCallback callback);
	public void UnregistCallback();
}
