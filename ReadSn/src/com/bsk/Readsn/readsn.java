package com.bsk.Readsn;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class readsn extends Activity implements OnClickListener {
	private Button btn_readSn;
	private TextView tv_showSn;
	Process process = null;
	DataOutputStream os = null;
	DataInputStream is = null;
	// static {
	// System.loadLibrary("allwinnertech_read_private");
	// }
	//
	// public native boolean initReadsn();
	//
	// public native void stopSn();
	//
	// public native String readsn(String a);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btn_readSn = (Button) findViewById(R.id.btn_readSn);
		tv_showSn = (TextView) findViewById(R.id.tv_showSn);
		btn_readSn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_readSn:
			//String sn=System.getProperty("os.name");
			String sn = SystemProperties.get("sys.hw.sn");
			Log.v("ReadSn", "sn:" + sn);
			if (sn == null || sn.equals("")) {
				Toast.makeText(readsn.this, "获取失败或SN为空！", Toast.LENGTH_SHORT).show();
			} else {
				tv_showSn.setText(sn);
			}
			break;
		}
	}

	private String toggleMobileData(Context context, String enabled) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		Method setMobileDataEnabl;
		String aa = "";
		try {
			setMobileDataEnabl = connectivityManager.getClass().getDeclaredMethod("SystemProperties.get");
			aa = (String) setMobileDataEnabl.invoke(connectivityManager, enabled);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return aa;
	}

	

	public void readTxtFile(String filePath, String key) {
		try {
			String encoding = "GBK";
			File file = new File(filePath);
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = "";
				String data = "";
				int num = 0;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					num = lineTxt.indexOf(key);
					data = lineTxt;
				}
				Log.v("zzzzb", "num:" + num);
				Log.v("zzzzb", "data:" + data);

				String sub = data.substring(40, 53);
				Log.v("zzzzb", "sub:" + sub);
				read.close();
			} else {
				System.out.println("找不到指定的文件");
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
	}

}
