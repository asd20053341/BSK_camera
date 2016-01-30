package com.cnlaunch.autoclear;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.Environment;
import android.util.Log;

public class Paths {
	public static String propertiesPath() {
		String path = getPath2();
		return path + "/x431-avoid-setup/preinstall/cleardata.xml";
	}

	/**
	 * ��ȡ����sd��·��
	 * 
	 * @return
	 */
	public static String getPath2() {
		String sdcard_path = null;
		// ��ȡSD����·��
		String sd_default = Environment.getExternalStorageDirectory().getAbsolutePath();
		// Log.d("text", sd_default);
		// ����ֻ�ǰ������ġ�/��ȥ���˶���
		if (sd_default.endsWith("/")) {
			sd_default = sd_default.substring(0, sd_default.length() - 1);
		}
		// �õ�·��
		try {
			// Ӧ�ó���ͨ��Runtime������ʱ����������������Ի�ȡ���е���Ϣ�������ڴ棬CPU
			Runtime runtime = Runtime.getRuntime();
			// runtime.exec("mount");��˵��ִ�����mount����
			Process proc = runtime.exec("mount");
			// ��ִ��������Ľ�����������InputStream
			InputStream is = proc.getInputStream();
			// ��һ�����������ȥ��
			InputStreamReader isr = new InputStreamReader(is);
			String line;
			// �û���������
			BufferedReader br = new BufferedReader(isr);
			// ֱ������Ϊֹ
			while ((line = br.readLine()) != null) {
				if (line.contains("secure"))
					continue;
				if (line.contains("asec"))
					continue;
				if (line.contains("fat") && line.contains("/mnt/")) {
					String columns[] = line.split(" ");
					if (columns != null && columns.length > 1) {
						if (sd_default.trim().equals(columns[1].trim())) {
							continue;
						}
						sdcard_path = columns[1];
					}
				} else if (line.contains("fuse") && line.contains("/mnt/")) {
					String columns[] = line.split(" ");
					if (columns != null && columns.length > 1) {
						if (sd_default.trim().equals(columns[1].trim())) {
							continue;
						}
						sdcard_path = columns[1];
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d("text", "�����ڴ濨·����" + sdcard_path);
		return sdcard_path;
	}
}
