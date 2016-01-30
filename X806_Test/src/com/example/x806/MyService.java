package com.example.x806;

import java.util.Timer;
import java.util.TimerTask;

import com.example.x806.IPerson.Stub;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;

public class MyService extends Service {
	private Stub iPerson = new X806master();
	private static final String TAG = "X806master";
	private Timer timer3 = null;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.v("X806master", "Æô¶¯service");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
//		try {
//			timer3 = new Timer();
//			timer3.schedule(new TimerTask() {
//
//				@Override
//				public void run() {
//					Log.v("X806master", "onStartCommand");
//				}
//			}, 0, 1000);
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (IllegalStateException e) {
//			e.printStackTrace();
//		}
		Log.v(TAG, "onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.v("X806master", "·µ»ØiPerson");
		return iPerson;
	}

}
