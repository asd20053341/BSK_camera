package com.example.blacklist;

import java.util.Timer;
import java.util.TimerTask;

import com.example.blacklist.R;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();	
		//System.out.println("我是服务，已启动");
		/*
		 * Intent intent =new Intent(); intent.setClass(this,
		 * MainActivity.class); startActivity(intent);
		 */

	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		// startForeground()
		return super.onStartCommand(intent, START_REDELIVER_INTENT, startId);

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		onCreate();
	}
}
