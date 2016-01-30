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
	 * 获取外置sd卡路径
	 * 
	 * @return
	 */
	public static String getPath2() {
		String sdcard_path = null;
		// 获取SD卡的路径
		String sd_default = Environment.getExternalStorageDirectory().getAbsolutePath();
		// Log.d("text", sd_default);
		// 这里只是把最后面的“/”去掉了而已
		if (sd_default.endsWith("/")) {
			sd_default = sd_default.substring(0, sd_default.length() - 1);
		}
		// 得到路径
		try {
			// 应用程序通过Runtime与运行时环境相连，此类可以获取运行的信息，比如内存，CPU
			Runtime runtime = Runtime.getRuntime();
			// runtime.exec("mount");是说，执行命令“mount”，
			Process proc = runtime.exec("mount");
			// 把执行命令完的结果给予输出流InputStream
			InputStream is = proc.getInputStream();
			// 用一个读输出流类去读
			InputStreamReader isr = new InputStreamReader(is);
			String line;
			// 用缓冲器读行
			BufferedReader br = new BufferedReader(isr);
			// 直到读完为止
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
		Log.d("text", "外置内存卡路径：" + sdcard_path);
		return sdcard_path;
	}
}
