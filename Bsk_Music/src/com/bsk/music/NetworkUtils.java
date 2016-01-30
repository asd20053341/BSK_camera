package com.bsk.music;

import android.content.Context;
import android.net.ConnectivityManager;

public class NetworkUtils {
	private static Context mContext;
	
	/**
	 * ̬获取当前网络状态
	 */
	public static boolean getNetWorkStatus(Context context) {
		boolean netSataus = false;
		ConnectivityManager cwjManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		cwjManager.getActiveNetworkInfo();
		
		if (cwjManager.getActiveNetworkInfo() != null) {
			netSataus = cwjManager.getActiveNetworkInfo().isAvailable();
		}
		return netSataus;
	}
	
	
}
