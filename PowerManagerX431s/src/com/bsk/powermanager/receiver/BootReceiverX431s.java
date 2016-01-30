package com.bsk.powermanager.receiver;

import com.bsk.powermanager.x431s.ServiceX431S;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiverX431s extends BroadcastReceiver {
	static final String ACTION = "android.intent.action.BOOT_COMPLETED";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction().equals(ACTION)) {
			Intent mIntent = new Intent(context, ServiceX431S.class);
			context.startService(mIntent);
			Log.v("X431s", "开机自启动服务");
		}
	}

}