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
		// ���ﴴ��·��Ϊ��
		// sd����·��/test.txt��������һ��true������������test.txtһֱ���ڵĻ����Ժ�����ݾ�ֱ��׷�ӵ�test.txt
		// ���ݵĺ��棬���Ḳ�ǣ����û��true���ʹ�����
		try {
			fw = new FileWriter(Environment.getExternalStorageDirectory() + "/" + "NetWork.txt", true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.v(TAG, "д��SD��ʧ��");
		}
		// newһ��д�Ķ���
		pw = new PrintWriter(fw);
		// д�롰V/BSK����Ȼ����
		pw.println(data + "\r\n");
		// д���ر�
		pw.close();
	}
}
