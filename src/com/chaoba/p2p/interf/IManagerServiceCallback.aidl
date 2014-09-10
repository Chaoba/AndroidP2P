package com.chaoba.p2p.interf;
interface IManagerServiceCallback { 
   	 void receiveMessage(out byte[] receMeg, int size);
	 void serverCreated();
	 void updateServer(String serverName);
}  