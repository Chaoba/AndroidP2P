package com.chaoba.p2p.interf;


public interface Receiver {
	public void onReceiveMessage(String message);
	public void onFilePercentUpdated(String fileName,int percent);
}
