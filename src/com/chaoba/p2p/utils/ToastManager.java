package com.chaoba.p2p.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class ToastManager {

	private static final int SHOW_STRING = 0;
	private static final int SHOW_INT = 1;
	private static Context mContext;
	private static Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOW_STRING:
				show(mContext, (String) msg.obj, false);
				break;
			case SHOW_INT:
				show(mContext, mContext.getString(msg.arg1), false);
				break;
			}
			super.handleMessage(msg);
		}

	};

	public static void show(Context context, String msg) {
		mContext = context;
		Message message = new Message();
		message.what = SHOW_STRING;
		message.obj = msg;
		mHandler.sendMessage(message);
	}

	public static void show(Context context, int id) {
		mContext = context;
		Message message = new Message();
		message.what = SHOW_INT;
		message.arg1 = id;
		mHandler.sendMessage(message);
	}

	public static void show(Context context, String msg, boolean isLong) {
		mContext = context;
		if (null != msg && !"".equalsIgnoreCase(msg)) {
			if (isLong) {
				Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
				
				toast.show();
			} else {
				Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
				
				
				toast.show();
			}
		}
	}

}
