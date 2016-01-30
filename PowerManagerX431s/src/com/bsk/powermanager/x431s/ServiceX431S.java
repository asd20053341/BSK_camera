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
 * ���⣺1.��ô֪�����Ƿ���������ˣ���������û�м�����service��������
 * 2.�ж�lan����ConnectivityManager.TYPE_ETHERNET�� 3.����ر�lan ��jni 4.cup��Ƶ,��jni
 * 5.��Ļ�Զ��������յĵ��㲥
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
					// �ر�lan
					Log.v(TAG, "�ﵽ20s���ر�LAN");
				} else {
					Log.v(TAG, "û��ִ�йر�LAN");
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
				// Cpu��Ƶ
				Log.v(TAG, "�ﵽ20s��Cpu��Ƶ");
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
		Log.v(TAG, "����service");
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
	 * ��LAN
	 */
	public void bsk_enble_lan() {
		setDirection("PG", 12, "out");
		setValue("PG", 12, 1);
	}

	/**
	 * �ر�LAN
	 */
	public void bsk_disanble_lan() {
		setDirection("PG", 12, "out");
		setValue("PG", 12, 0);
	}

	/**
	 * CPU��Ƶ
	 */
	public void cpuFrequency() {
		Log.v(TAG, "Cpu��Ƶ");
	}

	/**
	 * ��ֹ����
	 */
	public void banSlepp() {
		powerManager = (PowerManager) (getSystemService(Context.POWER_SERVICE));
		// ��ֹ��Ϊ��ʱ��ִ����һ����
		if (wakeLock != null) {
			Log.v(TAG, "��ֹ����");
			Log.v(TAG, "wakeLock:" + wakeLock);
			return;
		}
		wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");
		wakeLock.acquire();
		Log.v(TAG, "��ֹ����");
		Log.v(TAG, "wakeLock:" + wakeLock);
	}

	/**
	 * �ͷŽ�ֹ����
	 */
	public void allowSlepp() {
		try {
			if (wakeLock != null && wakeLock.isHeld()) {
				wakeLock.release();
				wakeLock = null;
			}
		} catch (Exception e) {
			Toast.makeText(ServiceX431S.this, "���쳣", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		Log.v(TAG, "�ͷŽ�ֹ����");
		Log.v(TAG, "wakeLock:" + wakeLock);
	}

	/**
	 * ����ʱ��ȡ����״̬
	 * 
	 * @return
	 */
	public int getNetworkTypeOpen() {
		int netType = 0;
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		Log.v(TAG, "networkInfo:" + networkInfo);
		if (networkInfo == null) {
			Log.v(TAG, "��������δ����");
			Toast.makeText(ServiceX431S.this, "����δ���ӣ�", Toast.LENGTH_SHORT).show();
			return netType;
		}
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_ETHERNET) {
			Log.v(TAG, "��̫��");
			// ��ֹ20s��ر�LAN
			lanFlag = false;
			// ��wifi
			setWifi(true);
			// ����������
			toggleMobileData(ServiceX431S.this, true);
			// ��ֹ����
			banSlepp();

		} else if (nType == ConnectivityManager.TYPE_MOBILE) {
			Log.v(TAG, "�ƶ�����");
			// ��wifi
			setWifi(true);
			// ��lan
			Log.v(TAG, "��LAN");
			// ��ֹ20s��ر�LAN
			lanFlag = false;
			// ��ֹ����
			banSlepp();

		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			Log.v(TAG, "wifi����");
			// ����������
			toggleMobileData(ServiceX431S.this, true);
			// ��lan
			Log.v(TAG, "��LAN");
			// ��ֹ20s��ر�LAN
			lanFlag = false;
			// �ͷŽ�ֹ����
			allowSlepp();
		}
		return netType;
	}

	/**
	 * ����ʱ��ȡ����״̬
	 * 
	 * @return
	 */
	public int getNetworkTypeClose() {
		int netType = 0;
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		Log.v(TAG, "networkInfo:" + networkInfo);
		if (networkInfo == null) {
			Log.v(TAG, "��������δ����");
			Toast.makeText(ServiceX431S.this, "����δ���ӣ�", Toast.LENGTH_SHORT).show();
			return netType;
		}
		int nType = networkInfo.getType();

		if (nType == ConnectivityManager.TYPE_ETHERNET) {
			Log.v(TAG, "��̫��");
			// ��ֹ20s��ر�LAN
			lanFlag = false;
			// ��wifi
			setWifi(true);
			// ����������
			toggleMobileData(ServiceX431S.this, true);
			// ��ֹ����
			banSlepp();
			// 20��cpu��Ƶ
			handler.sendEmptyMessageDelayed(1, 20 * 1000);
		} else if (nType == ConnectivityManager.TYPE_MOBILE) {
			Log.v(TAG, "�ƶ�����");
			// ��wifi
			setWifi(true);
			// ����������
			toggleMobileData(ServiceX431S.this, true);
			// ��ֹ����
			banSlepp();
			// Ϊtrueʱ��20s��ر�LAN
			lanFlag = true;
			// 20��cpu��Ƶ
			handler.sendEmptyMessageDelayed(1, 20 * 1000);
			// 20s��ر�lan
			handler.sendEmptyMessageDelayed(0, 20 * 1000);

		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			Log.v(TAG, "wifi����");
			// ����������
			toggleMobileData(ServiceX431S.this, true);
			// �ͷŽ�ֹ����
			allowSlepp();
			// Ϊtrueʱ��20s��ر�LAN
			lanFlag = true;
			// 20��ر�lan
			handler.sendEmptyMessageDelayed(0, 20 * 1000);
		}
		return netType;
	}

	/**
	 * �򿪻�ر���������
	 * 
	 * @param context:������
	 * @param enabled�����翪�أ�boolean����
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
	 * �򿪻�ر�wifi
	 * 
	 * @param state
	 */
	private void setWifi(boolean state) {
		wifiManager.setWifiEnabled(state);
	}

	/**
	 * ע������������㲥
	 */
	private void screenOnBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.SCREEN_ON");
		filter.addAction("android.intent.action.SCREEN_OFF");
		this.registerReceiver(screenBroadcast, filter);
	}

	/**
	 * ���պ����������㲥
	 */
	private BroadcastReceiver screenBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("android.intent.action.SCREEN_ON")) {
				Log.v(TAG, "����");
				getNetworkTypeOpen();
			} else if (action.equals("android.intent.action.SCREEN_OFF")) {
				Log.v(TAG, "����");
				getNetworkTypeClose();
			}
		}

	};

}
