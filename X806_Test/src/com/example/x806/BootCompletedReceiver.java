package com.example.x806;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {

	static final String ACTION = "android.intent.action.BOOT_COMPLETED";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction().equals(ACTION)) {
			Intent mIntent= new Intent(context, MyService.class);
			context.startService(mIntent);
			Log.v("X806master", "开机自启动服务");
		}
	}

}