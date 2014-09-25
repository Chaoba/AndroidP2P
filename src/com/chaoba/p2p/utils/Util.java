package com.chaoba.p2p.utils;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class Util {
	public static final int FIND_SERVER = 0;

	public static final int MESSAGE_PORT = 8000;
	public static final int FILE_PORT = 8001;
	
	public static final int BUFFER_SIZE = 1024 * 4;
	public static final int FILE_BUFFER_SIZE = BUFFER_SIZE*10;
	public static final int SEND_MESSAGE_DELAY=300;
	
	public static final String SEND_FILE_COMMAND="$SEND_FILE&";
	public static final String READY_RECEIVE_FILE_COMMAND="$READY&";

	public static String SAVE_PATH="ANDROIDP2P";
	
	public static String getIp(Context c) {
		WifiManager wifiManager = (WifiManager) c
				.getSystemService(Context.WIFI_SERVICE);
		if (!wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(true);
		}
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		String ip = intToIp(ipAddress);
		return ip;
	}

	private static String intToIp(int i) {

		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
				+ "." + (i >> 24 & 0xFF);
	}
	
	public static boolean isNum(String str) {
		return str.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
	}
	
	public static File getSaveFile(String fileNamePath) {
		File file = new File(fileNamePath);
		int n;
		if (file.exists()) {
			String fileName = fileNamePath.substring(0,
					fileNamePath.lastIndexOf("."));
			String extension = fileNamePath.substring(
					fileNamePath.lastIndexOf("."), fileNamePath.length());
			String number = null;
			if (fileName.contains("-")) {
				number = fileName.substring(fileName.lastIndexOf("-"),
						fileName.length());
			}
			if (number == null || number.length() == 0 || !Util.isNum(number)) {
				n = 0;
			} else {
				n = Integer.valueOf(number);
			}
			do {
				n++;
				StringBuffer buffer = new StringBuffer();
				buffer.append(fileName);
				buffer.append("_");
				buffer.append(n);
				buffer.append(extension);
				file = new File(buffer.toString());
			} while (file.exists());
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}
}
