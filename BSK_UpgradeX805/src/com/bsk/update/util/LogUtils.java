package com.bsk.update.util;

import android.util.Log;

import com.bsk.update.Constants;

public class LogUtils {

	public  static void i(String msg) {
		if(Constants.isDebug) {
			Log.i("", msg);
		}
	}
	
	public  static void e(String msg) {
		if(Constants.isDebug) {
			Log.e("", msg);
		}
	}
}
