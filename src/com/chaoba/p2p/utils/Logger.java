package com.chaoba.p2p.utils;

import android.util.Log;

public class Logger {
	private static final boolean IS_DEBUG = true;
	private static boolean mIsInnerShowLog = IS_DEBUG;

	private static String getFunctionName() {
		StackTraceElement[] sts = Thread.currentThread().getStackTrace();
		if (sts == null) {
			return null;
		}
		for (StackTraceElement st : sts) {
			if (st.isNativeMethod()) {
				continue;
			}
			if (st.getClassName().equals(Thread.class.getName())) {
				continue;
			}
			if (st.getClassName().equals("com.chaoba.p2p.utils.Logger")) {
				continue;
			}
			return "[Line: " + st.getLineNumber() + "] ";
		}
		return null;
	}

	public static void i(String tag, Object message) {
		if (mIsInnerShowLog) {
			String name = getFunctionName();
			if (name == null) {
				Log.i(tag, message.toString());
			} else {
				Log.i(tag, name + message.toString());
			}

		}
	}

	public static void d(String tag, Object message) {
		if (mIsInnerShowLog) {
			String name = getFunctionName();
			if (name == null) {
				Log.d(tag, message.toString());
			} else {
				Log.d(tag, name + message.toString());
			}
		}
	}

	public static void v(String tag, Object message) {
		if (mIsInnerShowLog) {
			String name = getFunctionName();
			if (name == null) {
				Log.v(tag, message.toString());
			} else {
				Log.v(tag, name + message.toString());
			}
		}
	}

	public static void w(String tag, Object message) {
		if (mIsInnerShowLog) {
			String name = getFunctionName();
			if (name == null) {
				Log.w(tag, message.toString());
			} else {
				Log.w(tag, name + message.toString());
			}
		}
	}

	public static void e(String tag, Object message) {
		if (mIsInnerShowLog) {
			String name = getFunctionName();
			if (name == null) {
				Log.e(tag, message.toString());
			} else {
				Log.e(tag, name + message);
			}

		}
	}

	public static void e(String tag, Exception e) {
		if (mIsInnerShowLog) {
			String name = getFunctionName();
			if (name == null) {
				Log.e(tag, e.getMessage());
			} else {
				Log.e(tag, name + e.getMessage());
			}

		}
	}
}
