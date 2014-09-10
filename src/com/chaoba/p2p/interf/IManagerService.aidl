
package com.chaoba.p2p.interf;

import com.chaoba.p2p.interf.IManagerServiceCallback;
interface IManagerService { 
	 void startServer(String serverName);
	 void stopServer();
	 void findServer();
	 void connectServer(String serverName);
	 void registCallback(IManagerServiceCallback callback);
	 void unRegistCallback();
	 void sendMessage(String message);
}  
