package com.chaoba.p2p.wifidirect;

import com.chaoba.p2p.wifidirect.interf.ConnectionManager;
import com.chaoba.p2p.wifidirect.interf.TransmissionManager;

public class P2PManger {
	ConnectionManager mConnectionManager;
	TransmissionManager mTransmissionManager;
	
	public P2PManger(){
		
	}
	void setConnectionManager(ConnectionManager manager){
		mConnectionManager=manager;
	}
	
	void setTransmissionManager(TransmissionManager manager){
		mTransmissionManager=manager;
	}
}
