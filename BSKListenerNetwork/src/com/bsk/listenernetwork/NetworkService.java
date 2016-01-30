package com.bsk.listenernetwork;

import java.util.HashMap;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class NetworkService extends Service {
	private static final String TAG = "NetworkService";
	private boolean netWorkFlag = true;// ����仯�㲥�ı�־
	private Handler handler = new Handler();
	private UrlResponse urlResponse = new UrlResponse(NetworkService.this);
	private HashMap<Integer, String> hashmap;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		netWorkBroadcastReg();
		Log.v(TAG, "onCreate");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.v(TAG, "onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.v(TAG, "onBind");
		return null;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.v(TAG, "onDestroy");
		unregisterReceiver(netWorkBroadcast);
	}

	/**
	 * ע������仯�㲥
	 */
	private void netWorkBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		this.registerReceiver(netWorkBroadcast, filter);
	}

	/**
	 * ��������仯�㲥
	 */
	private BroadcastReceiver netWorkBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
				if (netWorkFlag) {
					netWorkFlag = false;
					getCurrentNetType(NetworkService.this);
				}
			}
		}
	};

	/**
	 * ��ȡ��������
	 * 
	 * @param context
	 * @return
	 */
	public void getCurrentNetType(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info == null) {
			Log.v(TAG, "null");
			handlerPostDelayed();
		} else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
			Log.v(TAG, "TYPE_WIFI");
			requestConnect();
			handlerPostDelayed();
		} else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
			Log.v(TAG, "TYPE_MOBILE");
			requestConnect();
			handlerPostDelayed();
		} else {
			Log.v(TAG, "null");
			handlerPostDelayed();
		}
	}

	/**
	 * ѭ������xml�������
	 */
	public void requestConnect() {
		hashmap = urlResponse.parseProperties(Utils.URL);
		if (hashmap != null) {
			urlResponse.getURLResponse(1);
			Log.v(TAG, "��ǰ�����������ӣ�" + hashmap.get(1));
		} else {
			Log.v(TAG, "����XMLʧ�ܻ���XML�ļ�!");
		}
	}

	/**
	 * �ӳ٣���ֹ��ν�������仯�㲥
	 */
	public void handlerPostDelayed() {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				netWorkFlag = true;
			}
		}, 1500);
	}

}
