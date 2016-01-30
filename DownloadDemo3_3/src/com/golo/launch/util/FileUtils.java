package com.golo.launch.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import junit.framework.Test;

import org.apache.http.conn.ConnectTimeoutException;

import com.golo.launch.download.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

public class FileUtils {
	private String SDPATH;
	public int b = 0;
	private Timer timer3 = null;
	private Timer timer4 = null;
	private Timer timer5 = null;
	Handler handler1 = null;
	Handler handler2 = null;
	PrintWriter pw = null;
	FileWriter fw = null;
	public int c = 0;
	
	// Download asd=new Download();
	public String getSDPATH() {
		return SDPATH;
	}

	public FileUtils() {
		// 得到当前外部存储设备的目录
		// /SDCARD
		SDPATH = Environment.getExternalStorageDirectory() + "/";
		
	}

	/**
	 * 在SD卡上创建文件
	 * 
	 * @throws IOException
	 */
	public File creatSDFile(String fileName) throws IOException {
		File file = new File(SDPATH + fileName);
		file.createNewFile();
		return file;
	}

	/**
	 * 在SD卡上创建目录
	 * 
	 * @param dirName
	 */
	public File creatSDDir(String dirName) {
		File dir = new File(SDPATH + dirName);
		dir.mkdir();
		return dir;
	}

	/**
	 * 判断SD卡上的文件夹是否存在
	 */
	public boolean isFileExist(String fileName) {
		File file = new File(SDPATH + fileName);
		return file.exists();
	}

	public void timerTaskDatabase() {
		try {
			timer3 = new Timer();
			timer3.schedule(new TimerTask() {
				@Override
				public void run() {
					// /System.out.println(b);
					String ab = b + "";
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
					Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
					String str = formatter.format(curDate);
					try {
						// if (fw.getPath().equals(
						// Environment.getExternalStorageDirectory() + "/"
						// + "test.txt")) {
						// out.append(str + " " + b + "\n");
						// } else {
						/*
						 * FileOutputStream out = new
						 * FileOutputStream(Environment
						 * .getExternalStorageDirectory() + "/" +
						 * "test.txt",true);
						 */

						fw = new FileWriter(Environment.getExternalStorageDirectory() + "/" + "test.txt", true);
						/*
						 * test = new File(Environment
						 * .getExternalStorageDirectory() + "/" + "test.txt");
						 */

						pw = new PrintWriter(fw);
						// if(b==c&&b!=0&&c!=0){
						// pw.println(str + " V/BSK" + " " + b +"
						// 000000"+"\r\n");
						// }else{
						pw.println(str + " V/BSK" + " " + b + "\r\n");
						// pw.println(str + " 111111111111" + "\r\n");
						// }
						pw.close();

						// }
						// System.setOut(out);
						// System.out.println();
					} catch (Exception e) {
					}

					/*
					 * if (b == b) {
					 * 
					 * System.out.println("aaaaaaaaaa"); }
					 */
				}

			}, 0, 1000);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	public void timerTaskDatabase2() {
		try {
			timer4 = new Timer();
			timer4.schedule(new TimerTask() {

				@Override
				public void run() {
					c = b;
				}
			}, 0, 5000);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	public void daxiao() {
		if (b == c && b != 0 && c != 0) {
			System.exit(0);

		}
	}

	public void timerTaskDatabase5() {
		try {
			timer5 = new Timer();
			timer5.schedule(new TimerTask() {

				@Override
				public void run() {
					if (b == c && b != 0 && c != 0) {
						System.exit(0);
					}
				}
			}, 0, 9000);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	public void asd() {
		try {
			Process process = Runtime.getRuntime().exec("logcat -d");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			StringBuilder log = new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				log.append(line);
			}
		} catch (IOException e) {
		}
	}

	public void CannelTime2() {
		timerTaskDatabase();
		timer3.cancel();
	}

	public void CannelTime() {
		try {
			b = 0;
			c = 0;
			// timer4=new Timer();
			// timer5=new Timer();
			if (timer3 != null) {
				timer3.cancel();
			}
			// timer4.cancel();
			// timer5.cancel();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 将一个InputStream里面的数据写入到SD卡中
	 */
	public File write2SDFromInput(String path, String fileName, InputStream input) {
		CannelTime();
		System.out.println(timer3);
		System.out.println(timer3);
		try {
			fw = new FileWriter(Environment.getExternalStorageDirectory() + "/" + "test.txt", true);
			pw = new PrintWriter(fw);
			pw.println("---------------------------------------------------------");
			pw.println("\r\n");
			pw.println("开始下载：");
			pw.println("\r\n");
			pw.close();
		} catch (Exception e) {
		}
		int a = 0;
		timerTaskDatabase5();

		// timerTaskDatabase2();
		// System.out.println(input.available());
		File file = null;
		OutputStream output = null;
		try {
			creatSDDir(path);
			file = creatSDFile(path + fileName);
			output = new FileOutputStream(file);
			byte buffer[] = new byte[4 * 1024];
			int len = -1;
			while ((a = input.read(buffer)) != -1) {
				b = a + b;
				// output.write(buffer);
				output.write(buffer, 0, a);
			}
			// timer3.cancel();
			output.flush();
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			try {
				output.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return file;
	}

}