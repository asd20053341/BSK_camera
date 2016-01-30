package com.bsk.powermanager.x431s;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

/**
 * 问题：1.怎么知道锁是否真的作用了，无论我有没有加锁，service都在运行
 * 2.判断lan是用ConnectivityManager.TYPE_ETHERNET吗 3.打开与关闭lan 的jni 4.cup降频,调jni
 * 5.屏幕自动黑屏，收的到广播
 * 
 * @author Administrator
 *
 */
public class ServiceX431S extends Service {
	private Timer timer3 = null;
	private static final String TAG = "X431ssss";
	private PowerManager powerManager;
	private PowerManager.WakeLock wakeLock;
	private WifiManager wifiManager = null;
	private boolean lanFlag = true;
	Runnable lanRunnable = null;

	static {
		System.loadLibrary("gpio_lib");
	}

	// Runnable cupRunnable = null;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if (lanFlag) {
					// 关闭lan
					Log.v(TAG, "达到20s，关闭LAN");
				} else {
					Log.v(TAG, "没有执行关闭LAN");
				}
				// lanRunnable = new Runnable() {
				// @Override
				// public void run() {
				//
				// }
				// };
				// handler.postDelayed(lanRunnable,100);
				break;
			case 1:
				// Cpu降频
				Log.v(TAG, "达到20s，Cpu降频");
				break;
			}
		}
	};

	public native String setDirection(String port, int num, String inout);

	public native String setValue(String port, int num, int value);

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		wifiManager = (WifiManager) super.getSystemService(Context.WIFI_SERVICE);
		screenOnBroadcastReg();
		Log.v(TAG, "启动service");
		try {
			timer3 = new Timer();
			timer3.schedule(new TimerTask() {
				@Override
				public void run() {

					Log.v("X431Service", "wakeLock:" + wakeLock);

				}
			}, 0, 1000);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(screenBroadcast);
	}

	public void sleepp(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 打开LAN
	 */
	public void bsk_enble_lan() {
		setDirection("PG", 12, "out");
		setValue("PG", 12, 1);
	}

	/**
	 * 关闭LAN
	 */
	public void bsk_disanble_lan() {
		setDirection("PG", 12, "out");
		setValue("PG", 12, 0);
	}

	/**
	 * CPU降频
	 */
	public void cpuFrequency() {
		Log.v(TAG, "Cpu降频");
	}

	/**
	 * 禁止休眠
	 */
	public void banSlepp() {
		powerManager = (PowerManager) (getSystemService(Context.POWER_SERVICE));
		// 防止不为空时又执行了一次锁
		if (wakeLock != null) {
			Log.v(TAG, "禁止休眠");
			Log.v(TAG, "wakeLock:" + wakeLock);
			return;
		}
		wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");
		wakeLock.acquire();
		Log.v(TAG, "禁止休眠");
		Log.v(TAG, "wakeLock:" + wakeLock);
	}

	/**
	 * 释放禁止休眠
	 */
	public void allowSlepp() {
		try {
			if (wakeLock != null && wakeLock.isHeld()) {
				wakeLock.release();
				wakeLock = null;
			}
		} catch (Exception e) {
			Toast.makeText(ServiceX431S.this, "锁异常", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		Log.v(TAG, "释放禁止休眠");
		Log.v(TAG, "wakeLock:" + wakeLock);
	}

	/**
	 * 亮屏时获取网络状态
	 * 
	 * @return
	 */
	public int getNetworkTypeOpen() {
		int netType = 0;
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		Log.v(TAG, "networkInfo:" + networkInfo);
		if (networkInfo == null) {
			Log.v(TAG, "亮屏网络未连接");
			Toast.makeText(ServiceX431S.this, "网络未连接！", Toast.LENGTH_SHORT).show();
			return netType;
		}
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_ETHERNET) {
			Log.v(TAG, "以太网");
			// 防止20s后关闭LAN
			lanFlag = false;
			// 打开wifi
			setWifi(true);
			// 打开数据网络
			toggleMobileData(ServiceX431S.this, true);
			// 禁止休眠
			banSlepp();

		} else if (nType == ConnectivityManager.TYPE_MOBILE) {
			Log.v(TAG, "移动网络");
			// 打开wifi
			setWifi(true);
			// 打开lan
			Log.v(TAG, "打开LAN");
			// 防止20s后关闭LAN
			lanFlag = false;
			// 禁止休眠
			banSlepp();

		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			Log.v(TAG, "wifi网络");
			// 打开数据网络
			toggleMobileData(ServiceX431S.this, true);
			// 打开lan
			Log.v(TAG, "打开LAN");
			// 防止20s后关闭LAN
			lanFlag = false;
			// 释放禁止休眠
			allowSlepp();
		}
		return netType;
	}

	/**
	 * 黑屏时获取网络状态
	 * 
	 * @return
	 */
	public int getNetworkTypeClose() {
		int netType = 0;
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		Log.v(TAG, "networkInfo:" + networkInfo);
		if (networkInfo == null) {
			Log.v(TAG, "黑屏网络未连接");
			Toast.makeText(ServiceX431S.this, "网络未连接！", Toast.LENGTH_SHORT).show();
			return netType;
		}
		int nType = networkInfo.getType();

		if (nType == ConnectivityManager.TYPE_ETHERNET) {
			Log.v(TAG, "以太网");
			// 防止20s后关闭LAN
			lanFlag = false;
			// 打开wifi
			setWifi(true);
			// 打开数据网络
			toggleMobileData(ServiceX431S.this, true);
			// 禁止休眠
			banSlepp();
			// 20后cpu降频
			handler.sendEmptyMessageDelayed(1, 20 * 1000);
		} else if (nType == ConnectivityManager.TYPE_MOBILE) {
			Log.v(TAG, "移动网络");
			// 打开wifi
			setWifi(true);
			// 打开数据网络
			toggleMobileData(ServiceX431S.this, true);
			// 禁止休眠
			banSlepp();
			// 为true时才20s后关闭LAN
			lanFlag = true;
			// 20后cpu降频
			handler.sendEmptyMessageDelayed(1, 20 * 1000);
			// 20s后关闭lan
			handler.sendEmptyMessageDelayed(0, 20 * 1000);

		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			Log.v(TAG, "wifi网络");
			// 打开数据网络
			toggleMobileData(ServiceX431S.this, true);
			// 释放禁止休眠
			allowSlepp();
			// 为true时才20s后关闭LAN
			lanFlag = true;
			// 20后关闭lan
			handler.sendEmptyMessageDelayed(0, 20 * 1000);
		}
		return netType;
	}

	/**
	 * 打开或关闭数据网络
	 * 
	 * @param context:上下文
	 * @param enabled：网络开关，boolean类型
	 */
	private void toggleMobileData(Context context, boolean enabled) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		Method setMobileDataEnabl;
		try {
			setMobileDataEnabl = connectivityManager.getClass().getDeclaredMethod("setMobileDataEnabled",
					boolean.class);
			setMobileDataEnabl.invoke(connectivityManager, enabled);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 打开或关闭wifi
	 * 
	 * @param state
	 */
	private void setWifi(boolean state) {
		wifiManager.setWifiEnabled(state);
	}

	/**
	 * 注册黑屏与亮屏广播
	 */
	private void screenOnBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.SCREEN_ON");
		filter.addAction("android.intent.action.SCREEN_OFF");
		this.registerReceiver(screenBroadcast, filter);
	}

	/**
	 * 接收黑屏与亮屏广播
	 */
	private BroadcastReceiver screenBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("android.intent.action.SCREEN_ON")) {
				Log.v(TAG, "亮屏");
				getNetworkTypeOpen();
			} else if (action.equals("android.intent.action.SCREEN_OFF")) {
				Log.v(TAG, "黑屏");
				getNetworkTypeClose();
			}
		}

	};

}
