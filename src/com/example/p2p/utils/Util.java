package com.example.p2p.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.EditText;

public class Util {

	public static String getIp(Context c){
		  WifiManager wifiManager = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);   
	        //判断wifi是否开启   
	        if (!wifiManager.isWifiEnabled()) {    
	        	wifiManager.setWifiEnabled(true);      
	        }    
	        WifiInfo wifiInfo = wifiManager.getConnectionInfo();         
	        int ipAddress = wifiInfo.getIpAddress();     
	        String ip = intToIp(ipAddress);     
	       return ip;
	}
	
	private static String intToIp(int i) {         
        
        return (i & 0xFF ) + "." +         
      ((i >> 8 ) & 0xFF) + "." +         
      ((i >> 16 ) & 0xFF) + "." +         
      ( i >> 24 & 0xFF) ;   
   }
}
