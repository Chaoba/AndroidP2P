package com.chaoba.p2p.interf;


public interface Advertiser {
	public void findServer();
	public void startAvertise();
	public void registerAvertiseLister(AdvertiseLister lister);
	public void stop();
}
