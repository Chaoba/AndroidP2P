package com.chaoba.p2p.wifidirect.interf;


public interface Receiver {
	public void onReceiveMessage(String message);
	public void onFilePercentUpdated(String fileName,int percent);
}
