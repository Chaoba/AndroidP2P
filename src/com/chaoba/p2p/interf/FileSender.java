package com.chaoba.p2p.interf;


public interface FileSender extends Sender{
	public boolean prepareToReceiveFile(String fileName,long fileSize);
}
