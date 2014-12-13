package com.chaoba.p2p.wifidirect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.util.Log;

import com.chaoba.p2p.utils.Logger;
import com.chaoba.p2p.wifidirect.interf.ConnectionManager;
import com.chaoba.p2p.wifidirect.interf.IP2PmanagerListener;

public class ConnectionManagerImpl implements ConnectionManager,
		ChannelListener {
	public static final String TAG = "ConnectionManagerImpl";
	public static final String AVAILABLE = "available";
	public static final String SERVICE_INSTANCE = "chaobaoP2P";
	public static final String SERVICE_REG_TYPE = "_presence._tcp";
	private Context mContext;
	private WifiP2pManager manager;
	private Channel channel;
	private final IntentFilter intentFilter = new IntentFilter();
	private WiFiDirectBroadcastReceiver receiver;
	private WifiP2pDnsSdServiceRequest serviceRequest;
	private IP2PmanagerListener mListener;
	private Map<String, WiFiP2PDeviceInstance> mServerMap = new HashMap<String, WiFiP2PDeviceInstance>();
	public TransferManager mTransferManager;

	public ConnectionManagerImpl(Context c, IP2PmanagerListener listener) {
		mContext = c;
		mListener = listener;

		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		receiver = new WiFiDirectBroadcastReceiver();
		mContext.registerReceiver(receiver, intentFilter);

		manager = (WifiP2pManager) mContext
				.getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(mContext, mContext.getMainLooper(), this);
	}

	public void detroy() {
		mContext.unregisterReceiver(receiver);
	}

	@Override
	public void setName(String name) {
		setDeviceName(name);
		// startRegistration(name);
		// discoverService();
	}

	@Override
	public void findPeer() {
		startRegistration(null);
		discoverService();

	}

	@Override
	public void connectServer(String name) {
		WiFiP2PDeviceInstance service = mServerMap.get(name);
		connectP2p(service);

	}

	@Override
	public void disConnectServer() {
		Logger.d(TAG, "disconnect");
		if (mTransferManager != null) {
			mTransferManager.close();
		}
		manager.removeGroup(channel, new ActionListener() {
			
			@Override
			public void onSuccess() {
				Logger.d(TAG, "removeGrouponSuccess");
				
			}
			
			@Override
			public void onFailure(int reason) {
				Logger.d(TAG, "removeGrouponFailure");
				
			}
		});
	}

	@Override
	public void sendMessage(String message) {
		if(mTransferManager!=null){
		mTransferManager.sendMessage(message);
		}
	}

	@Override
	public void sendFiles(ArrayList<String> filePaths) {
		if(mTransferManager!=null){
		mTransferManager.sendFiles(filePaths);
		}
	}

	private void startRegistration(String name) {
		Map<String, String> record = new HashMap<String, String>();
		record.put(AVAILABLE, "visible");

		WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
				SERVICE_INSTANCE, SERVICE_REG_TYPE, record);
		manager.addLocalService(channel, service, new LocalServicesListener());
		mListener.onServerCreated();
	}

	private void discoverService() {
		manager.setDnsSdResponseListeners(channel, new DnsResponseListener(),
				new DnsTxtRecordListener());

		serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
		manager.addServiceRequest(channel, serviceRequest,
				new AddServiceListener());
		manager.discoverServices(channel, new DiscoverServicesListener());
	}

	public void connectP2p(WiFiP2PDeviceInstance service) {
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = service.device.deviceAddress;
		config.wps.setup = WpsInfo.PBC;
		if (serviceRequest != null)
			manager.removeServiceRequest(channel, serviceRequest, null);

		manager.connect(channel, config, new ConnectServiceListener());
	}

	class DnsResponseListener implements DnsSdServiceResponseListener {

		@Override
		public void onDnsSdServiceAvailable(String instanceName,
				String registrationType, WifiP2pDevice srcDevice) {
			Logger.d(TAG, "find instanceName:" + instanceName);
			if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {
				Logger.d(TAG, "onBonjourServiceAvailable " + instanceName);
				WiFiP2PDeviceInstance service = new WiFiP2PDeviceInstance();
				service.device = srcDevice;
				service.instanceName = instanceName;
				service.serviceRegistrationType = registrationType;
				mServerMap.put(srcDevice.deviceName, service);
				mListener.onServerFound(srcDevice.deviceName,false);
			}

		}

	}

	class DnsTxtRecordListener implements DnsSdTxtRecordListener {

		@Override
		public void onDnsSdTxtRecordAvailable(String fullDomainName,
				Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
			Logger.d(TAG,
					srcDevice.deviceName + ":" + txtRecordMap.get(AVAILABLE));

		}

	}

	class ConnectionIfListener implements ConnectionInfoListener {

		@Override
		public void onConnectionInfoAvailable(WifiP2pInfo info) {
			if(mTransferManager==null)
			mTransferManager = new TransferManager(mListener, info);
		}

	}

	class AddServiceListener implements ActionListener {

		@Override
		public void onSuccess() {
			Logger.d(TAG, "Added service discovery request");
		}

		@Override
		public void onFailure(int arg0) {
			Logger.d(TAG, "Failed adding service discovery request");
		}

	}

	class ConnectServiceListener implements ActionListener {

		@Override
		public void onSuccess() {
			Logger.d(TAG, "Connected");
			mListener.onServerConnected();
		}

		@Override
		public void onFailure(int errorCode) {
			Logger.d(TAG, "Failed to connect");
		}

	}

	class DiscoverServicesListener implements ActionListener {

		@Override
		public void onSuccess() {
			Logger.d(TAG, "Service discovery initiated");
		}

		@Override
		public void onFailure(int arg0) {
			Logger.d(TAG, "Service discovery failed");

		}

	}

	class LocalServicesListener implements ActionListener {

		@Override
		public void onSuccess() {
			Logger.d(TAG, "add LocalServices");
		}

		@Override
		public void onFailure(int arg0) {
			Logger.d(TAG, "add LocalServices failed");

		}

	}

	class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Logger.d(TAG, action);
			if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
				int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,
						-1);
				if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
					Logger.d(TAG, "WIFI_P2P_STATE_ENABLED");
				} else {
					Logger.d(TAG, "WIFI_P2P_STATE_NOT_ENABLED");
				}
			} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION
					.equals(action)) {
				Logger.d(TAG, "P2P peers changed");
				if (manager != null) {
					manager.requestPeers(channel, new PeerListListener() {
						@Override
						public void onPeersAvailable(WifiP2pDeviceList peers) {
							Collection<WifiP2pDevice> list = peers
									.getDeviceList();
							for (WifiP2pDevice device : list) {
								Logger.d(TAG, "peer changed:"
										+ device.deviceName);
							}
						}
					});
				}
			} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION
					.equals(action)) {
				if (manager == null) {
					return;
				}
				NetworkInfo networkInfo = (NetworkInfo) intent
						.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
				if (networkInfo.isConnected()) {
					Log.d(TAG,
							"Connected to p2p network. Requesting network details");
					manager.requestConnectionInfo(channel,
							new ConnectionIfListener());
				} else {
					// It's a disconnect
				}
			} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
					.equals(action)) {

				WifiP2pDevice device = (WifiP2pDevice) intent
						.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
				Log.d(TAG, "Device status -" + device.status);
				mListener.onServerFound(device.deviceName,true);
			}
		}

	}

	@Override
	public void onChannelDisconnected() {
		Logger.d(TAG, "onChannelDisconnected");
		mListener.onConnectionLost();
	}

	public void setDeviceName(String name) {
		try {
			Class<?> partypes[] = new Class[3];
			partypes[0] = Channel.class;
			partypes[1] = String.class;
			partypes[2] = ActionListener.class;
			Method method = manager.getClass().getMethod("setDeviceName",
					partypes);
			method.setAccessible(true);

			Object[] args = new Object[3];
			args[0] = channel;
			args[1] = name;
			args[2] = null;
			method.invoke(manager, args);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		if (mTransferManager != null) {
			mTransferManager.close();
		}
	}

}
