package com.bsk.alarmbyhour;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiverVideo extends BroadcastReceiver {

	static final String ACTION = "android.intent.action.BOOT_COMPLETED";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction().equals(ACTION)) {
			if (isFolderExists("/mnt/extsd/x431-avoid-setup")) {
				Intent mIntent = new Intent(context, SurfaceActivity.class);
				mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS); 
				context.startActivity(mIntent);
				Log.v("CycleVideo", "开机自启动服务");
			}
		}
	}

	boolean isFolderExists(String strFolder) {
		File file = new File(strFolder);

		if (file.exists()) {
			return true;
		}
		return false;

	}

}