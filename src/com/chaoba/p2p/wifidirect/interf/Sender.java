package com.chaoba.p2p.wifidirect.interf;

import java.util.ArrayList;

public interface Sender {
	public void sendMessage(String message);
	public void sendFiles(ArrayList<String> filePaths);
	public void close();
}
