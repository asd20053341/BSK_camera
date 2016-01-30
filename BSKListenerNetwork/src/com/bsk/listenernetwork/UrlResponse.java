package com.bsk.listenernetwork;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.HashMap;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

public class UrlResponse {
	private static final String TAG = "UrlResponse";
	private HttpUtils httpUtils = new HttpUtils();
	private HashMap<Integer, String> hashmap1;
	private Context myContext;
	private WifiManager wifiManager = null;
	private Handler handler = null;
	private WriteSd writeSd = null;
	private static int toggleMobileNum = 0;
	private static int toggleWifiNum = 0;

	public UrlResponse(Context context) {
		myContext = context;
	}

	/**
	 * HttpUtilsȥ��������
	 * 
	 * @param urlString
	 */
	public void getURLResponse(final int com) {
		if (hashmap1 != null) {
			String connect = hashmap1.get(com);
			httpUtils.send(HttpMethod.POST, connect, new RequestCallBack<String>() {
				@Override
				public void onFailure(HttpException arg0, String arg1) {
					// TODO Auto-generated method stub
					Log.d(TAG, "onFailure");
					Log.v(TAG, "��ǰ�����������ӣ�" + hashmap1.get(com));
					// �����ǰ��������ʧ�ܣ���ȥ�����������ӣ�ֱ����xml�ļ����������������
					if (hashmap1.size() > com) {
						int com2 = com + 1;
						getURLResponse(com2);
					} else {
						// ������xml�ļ���������Ӻ󣬾Ϳ�������һ��
						getCurrentNetType2(myContext);
						return;
					}
					// Log.d(TAG, "onFailure��arg0:" + arg0);
					// Log.d(TAG, "onFailure��arg1:" + arg1);
				}

				@Override
				public void onSuccess(ResponseInfo<String> arg0) {
					// TODO Auto-generated method stub
					// �������ɹ�����ʲô������
					Log.d(TAG, "onSuccess");
					Log.v(TAG, "��ǰ�����������ӣ�" + hashmap1.get(com));
					// Log.d(TAG, "onSuccess��arg0:" + arg0);
					// s�Ƿ��������ص�json����
					// String s = arg0.result;
					// Log.d(TAG, "s:+++" + s);
				}

			});
		}

	}

	/**
	 * ����xml�ļ�
	 * 
	 * @param propertiesPath
	 * @return
	 */
	public HashMap<Integer, String> parseProperties(String propertiesPath) {
		// �õ�·��
		File file = new File(propertiesPath);
		try {
			// ������ڣ��ͽ���
			if (file.exists()) {
				// �Լ��������XMLPullParserHandler
				XMLPullParserHandler xpph = new XMLPullParserHandler();
				// ������XMLPullParserHandler���parse�����ǰ� xml�ļ�·������ȥ
				// �������ص�ֵ����HashMap<Integer, String>��hashmap1
				hashmap1 = xpph.parse(new FileInputStream(file));
				return hashmap1;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.d(TAG, "�޷��ҵ�XML�ļ�");
		}

		return null;
	}

	/**
	 * ��ȡ��������
	 * 
	 * @param context
	 * @return
	 */
	public void getCurrentNetType2(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		handler = new Handler();
		writeSd = new WriteSd();
		if (info == null) {
			Log.v(TAG, "null");
		} else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
			toggleWifi(false);
			Log.v(TAG, "�ر�wifi");
			// �������̫�죬�ᵼ���ֻ���ӳ������
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					toggleWifi(true);
					Log.v(TAG, "��wifi");
				}
			}, 500);
			toggleWifiNum++;
			writeSd.writeSD("wifi����:" + toggleWifiNum);

		} else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
			toggleMobileData(myContext, false);
			Log.v(TAG, "�ر��ƶ�����");
			// �������̫�죬�ᵼ���ֻ���ӳ������
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					toggleMobileData(myContext, true);
					Log.v(TAG, "���ƶ�����");
				}
			}, 500);
			toggleMobileNum++;
			writeSd.writeSD("�ƶ����ݴ���:" + toggleMobileNum);
		} else {
			Log.v(TAG, "null");
		}
	}

	/**
	 * ������������
	 * 
	 * @param context
	 * @param enabled
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
	 * wifi����
	 * 
	 * @param togg
	 */
	private void toggleWifi(boolean togg) {
		wifiManager = (WifiManager) myContext.getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(togg);
	}

}
