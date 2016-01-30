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
	 * HttpUtils去请求链接
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
					Log.v(TAG, "当前正在请求链接：" + hashmap1.get(com));
					// 如果当前链接请求失败，就去请求其他链接，直到把xml文件的里的链接请求完
					if (hashmap1.size() > com) {
						int com2 = com + 1;
						getURLResponse(com2);
					} else {
						// 请求完xml文件的里的链接后，就开关网络一次
						getCurrentNetType2(myContext);
						return;
					}
					// Log.d(TAG, "onFailure中arg0:" + arg0);
					// Log.d(TAG, "onFailure中arg1:" + arg1);
				}

				@Override
				public void onSuccess(ResponseInfo<String> arg0) {
					// TODO Auto-generated method stub
					// 如果请求成功，就什么都不做
					Log.d(TAG, "onSuccess");
					Log.v(TAG, "当前正在请求链接：" + hashmap1.get(com));
					// Log.d(TAG, "onSuccess中arg0:" + arg0);
					// s是服务器返回的json数据
					// String s = arg0.result;
					// Log.d(TAG, "s:+++" + s);
				}

			});
		}

	}

	/**
	 * 解析xml文件
	 * 
	 * @param propertiesPath
	 * @return
	 */
	public HashMap<Integer, String> parseProperties(String propertiesPath) {
		// 得到路径
		File file = new File(propertiesPath);
		try {
			// 如果存在，就进入
			if (file.exists()) {
				// 自己定义的类XMLPullParserHandler
				XMLPullParserHandler xpph = new XMLPullParserHandler();
				// 调用了XMLPullParserHandler里的parse，就是把 xml文件路径传进去
				// 解析返回的值传入HashMap<Integer, String>的hashmap1
				hashmap1 = xpph.parse(new FileInputStream(file));
				return hashmap1;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.d(TAG, "无法找到XML文件");
		}

		return null;
	}

	/**
	 * 获取网络类型
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
			Log.v(TAG, "关闭wifi");
			// 如果开关太快，会导致手机反映不过来
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					toggleWifi(true);
					Log.v(TAG, "打开wifi");
				}
			}, 500);
			toggleWifiNum++;
			writeSd.writeSD("wifi次数:" + toggleWifiNum);

		} else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
			toggleMobileData(myContext, false);
			Log.v(TAG, "关闭移动数据");
			// 如果开关太快，会导致手机反映不过来
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					toggleMobileData(myContext, true);
					Log.v(TAG, "打开移动数据");
				}
			}, 500);
			toggleMobileNum++;
			writeSd.writeSD("移动数据次数:" + toggleMobileNum);
		} else {
			Log.v(TAG, "null");
		}
	}

	/**
	 * 开关数据网络
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
	 * wifi开关
	 * 
	 * @param togg
	 */
	private void toggleWifi(boolean togg) {
		wifiManager = (WifiManager) myContext.getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(togg);
	}

}
