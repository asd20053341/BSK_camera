package com.bsk.listenernetwork;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NetworkBootReceiver extends BroadcastReceiver {

	static final String ACTION = "android.intent.action.BOOT_COMPLETED";  
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction().equals(ACTION)) {
			Intent mIntent= new Intent(context, NetworkService.class);
			context.startService(mIntent);
			Log.v("BSKListenerNetwork","开机自启动服务");
		}
	}
}