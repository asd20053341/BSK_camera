package com.bsk.listenernetwork;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import android.os.Environment;
import android.util.Log;

public class WriteSd {
	PrintWriter pw = null;
	FileWriter fw = null;
	private static final String TAG = "NetworkService";

	public void writeSD(String data) {
		// 这里创建路径为：
		// sd卡的路径/test.txt；后面有一个true，代表如果这个test.txt一直存在的话，以后的内容就直接追加到test.txt
		// 内容的后面，不会覆盖；如果没加true，就代表覆盖
		try {
			fw = new FileWriter(Environment.getExternalStorageDirectory() + "/" + "NetWork.txt", true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.v(TAG, "写入SD卡失败");
		}
		// new一个写的对象
		pw = new PrintWriter(fw);
		// 写入“V/BSK”，然后换行
		pw.println(data + "\r\n");
		// 写完后关闭
		pw.close();
	}
}
