package com.chaoba.p2p.interf;

public interface IFatherCallback {
	public void receiveMessage(String message);
	public void updateFilePercent(String FileName,int percent);
}
