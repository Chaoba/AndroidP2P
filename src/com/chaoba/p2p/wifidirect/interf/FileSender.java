package com.chaoba.p2p.wifidirect.interf;


public interface FileSender extends Sender{
	public boolean prepareToReceiveFile(String fileName,long fileSize);
}
