package com.chaoba.p2p.interf;

import java.util.ArrayList;

public interface IFatherService {
	public void sendMessage(String message);
	public void sendFiles(ArrayList<String> filePaths);
}
