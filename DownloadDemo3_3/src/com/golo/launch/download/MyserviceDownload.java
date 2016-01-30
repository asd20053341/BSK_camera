package com.golo.launch.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;

public class MyserviceDownload extends Service {
	private URL url = null;
	public Thread thread1 = null;
	HttpURLConnection urlConn = null;
	private Timer timer6 = null;
	private String SDPATH;
	public int b = 0;
	public int d = 0;
	public int e = 0;
	private Timer timer3 = null;
	private Timer timer4 = null;
	private Timer timer5 = null;
	Handler handler1 = null;
	Handler handler2 = null;
	Handler handler3 = null;
	PrintWriter pw = null;
	FileWriter fw = null;
	public int c = 0;
	int urlCode=0;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		SDPATH = Environment.getExternalStorageDirectory() + "/";
		handler3 = new Handler();
	}


	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		DownloadBroadcastReg();
		/*
		 * if(thread1.isAlive()){ thread1.stop(); }
		 */
		
		MyThread1 thread1 = new MyThread1();
		thread1.start();
//		try {
//			thread1.join();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
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
	}

	private void DownloadBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		// filter.addAction("CNLAUNCH_KEY_MODEL_SEARCH_UP");// BC_LIGHT_SENSOR
		filter.addAction("BSK_KEY_MODEL_SEARCH_UP"); // 就是自己想要接收的系统广播
		this.registerReceiver(DownloadBroadcast, filter);
	}

	private BroadcastReceiver DownloadBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 获取监听到的广播，获取系统发出的BC_LIGHT_SENSOR广播
			String action = intent.getAction();
			// 这里是只要有BC_LIGHT_SENSOR广播，就让它自动自增
			if (action.equals("BSK_KEY_MODEL_SEARCH_UP")) {
				// timerTaskDatabase3();
				/*if (thread1.isAlive()) {
					thread1.stop();
				}*/
				MyThread1 thread1 = new MyThread1();
				thread1.start();
			}
		}
	};

	public int downFile(String urlStr, String path, String fileName) {
		InputStream inputStream = null;
		try {

			// if (fileUtils.isFileExist(path + fileName)) {
			// return 1;
			// } else {
			inputStream = getInputStreamFromUrl(urlStr);
			File resultFile = write2SDFromInput(path, fileName, inputStream);
			if (resultFile == null) {
				return -1;
			}
			// }
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			try {
				inputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	public InputStream getInputStreamFromUrl(String urlStr) {
		// throws MalformedURLException,IOException
		try {
			urlConn.disconnect();
			//urlConn = null;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		try {
			url = new URL(urlStr);
			urlConn = (HttpURLConnection) url.openConnection();					
			// 设置连接主机超时（单位：毫秒）		
			urlConn.setConnectTimeout(30000);
			// 设置从主机读取数据超时（单位：毫秒）
			urlConn.setReadTimeout(20000);
			// urlConn.setAllowUserInteraction(true);
			// urlConn.setRequestMethod("GET");
			// urlConn.setRequestProperty("Accept-Language", "zh-CN");
			// urlConn.setRequestProperty("Referer", url.toString());
			// urlConn.setRequestProperty("Accept-Encoding", "identity");
			// urlConn.setRequestProperty("Charset", "UTF-8");

			urlConn.setRequestMethod("GET");
			urlConn.setRequestProperty("Accept",
					"image/gif, image/jpeg, image/pjpeg, image/pjpeg, "
							+ "application/x-shockwave-flash, application/xaml+xml, "
							+ "application/vnd.ms-xpsdocument, application/x-ms-xbap, "
							+ "application/x-ms-application, application/vnd.ms-excel, "
							+ "application/vnd.ms-powerpoint, application/msword, */*");
			urlConn.setRequestProperty("Accept-Language", "zh-CN");
			urlConn.setRequestProperty("Charset", "UTF-8");
			urlConn.setRequestProperty("Connection", "Keep-Alive");
			//获取要下载的文件大小
			//int asds=urlConn.getContentLength();
			InputStream inputStream = urlConn.getInputStream();
			
			/*
			 * if(urlConn.getReadTimeout()==5000){ Toast.makeText(Download.this,
			 * "超时1秒", Toast.LENGTH_SHORT).show(); }
			 */
			return inputStream;

		} catch (MalformedURLException e) {
			// TODO: handle exception
			// Toast.makeText(MyserviceDownload.this, "链接错误",
			// Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * class MyThread implements Runnable { public void run() { // 处理具体的逻辑
	 * //Toast.makeText(Download.this, "开始下载", Toast.LENGTH_SHORT).show(); int
	 * result = downFile(
	 * "http://res.media.golo5.com/topic/music/201501/20150122/c0472e02074cee47d72a25dc4f8f60b2_96.mp3",
	 * "voa/", "a1.mp3"); int result = downFile(
	 * "http://res.media.golo5.com/topic/music/201507/20150720/b5bdaa986693bc0821db7bd4574b922a_128.mp3",
	 * "voa/", "a1.mp3"); System.out.println(result); } }
	 */

	class MyThread1 extends Thread {
		public void run() {
			// 处理具体的逻辑
			/*
			 * int result = downFile(
			 * "http://lx.cdn.baidupcs.com/file/988eea1b935346d989c190aaea897d3e?bkt=p3-1400988eea1b935346d989c190aaea897d3e314d3047000000322e03&xcode=fa877fbbc8325a335e7ff93d98114121ecf1544edc4e7a088bb5ee938cac2427&fid=1510574754-250528-9153912873366&time=1445512996&sign=FDTAXGERLBH-DCb740ccc5511e5e8fedcff06b081203-g7K9LW%2FXiAwyFzwYpwV%2FjcJx4vc%3D&to=lc&fm=Nin,B,T,ny&sta_dx=3&sta_cs=5388&sta_ft=mp3&sta_ct=0&fm2=Ningbo,B,T,ny&newver=1&newfm=1&secfm=1&flow_ver=3&pkey=1400988eea1b935346d989c190aaea897d3e314d3047000000322e03&sl=68288590&expires=8h&rt=sh&r=192131967&mlogid=6837425785215035222&vuk=1510574754&vbdid=1907402705&fin=%E5%8D%81%E5%B9%B4.mp3&fn=%E5%8D%81%E5%B9%B4.mp3&slt=pm&uta=0&rtype=1&iv=0&isw=0&dp-logid=6837425785215035222&dp-callid=0.1.1",
			 * "voa/", "a1.mp3");
			 */
			int result = downFile(
					"http://res.media.golo5.com/topic/music/201501/20150122/c0472e02074cee47d72a25dc4f8f60b2_96.mp3",
					"voa/", "a1.mp3");

			/*
			 * int result = downFile(
			 * "http://res.media.golo5.com/topic/music/201507/20150720/b5bdaa986693bc0821db7bd4574b922a_128.mp3",
			 * "voa/", "a1.mp3");
			 */
			// System.out.println(result);
			
		}
	}

	public String getSDPATH() {
		return SDPATH;
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

	public void timerTaskDatabase3() {
		try {
			timer6 = new Timer();
			timer6.schedule(new TimerTask() {

				@Override
				public void run() {
					// c:0秒的数据
					// c:8秒的数据
					e = b;
				}
			}, 1000, 2000);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	
	public void timerTaskDatabase() {
		try {
			timer3 = new Timer();
			timer3.schedule(new TimerTask() {
				@Override
				public void run() {
					//String ab = b + "";
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
					Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
					String str = formatter.format(curDate);				
					try {
//						try {
//							urlCode=0;
//							urlCode=urlConn.getResponseCode();
//						} catch (Exception e) {
//							// TODO: handle exception
//							e.printStackTrace();
//						}
						
						fw = new FileWriter(Environment.getExternalStorageDirectory() + "/" + "test.txt", true);
						// fw.
						pw = new PrintWriter(fw);
						//if (b == e && b != 0 && e != 0) {
						//	pw.println(str + " V/BSK" + " " + b +  "000000" + "\r\n");
						//} else {
							pw.println(str + " V/BSK" + " " + b + "\r\n");
							System.out.println(str + " V/BSK" + " " + b);
						//}
						pw.close();
					} catch (Exception e) {
					}
				}
			}, 0, 1000);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	// 11:25:44
	public void timerTaskDatabase2() {
		try {
			timer4 = new Timer();
			timer4.schedule(new TimerTask() {

				@Override
				public void run() {
					// c:0秒的数据
					// c:8秒的数据
					c = b;
				}
			}, 0, 8500);
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

	public void TimeAndHttpCannel() {
		/*
		 * new AlertDialog.Builder(Download.this).setTitle("网络不给力")// 设置对话框标题
		 * .setMessage("8秒内没有数据，已停止下载")// 设置显示的内容 .setPositiveButton("确定", new
		 * DialogInterface.OnClickListener() {// 添加确定按钮
		 * 
		 * @Override public void onClick(DialogInterface dialog, int which) {//
		 * 确定按钮的响应事件 // TODO Auto-generated method stub try { CannelTime();
		 * urlConn.disconnect(); } catch (Exception e) { // TODO: handle
		 * exception e.printStackTrace(); } } }).show();
		 */
		/*
		 * .setNegativeButton("返回", new DialogInterface.OnClickListener() {//
		 * 添加返回按钮
		 * 
		 * @Override public void onClick(DialogInterface dialog, int which) {//
		 * 响应事件 // TODO Auto-generated method stub }
		 * 
		 * })
		 */// 在按键响应事件中显示此对话框
		// try {
		// SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日
		// HH:mm:ss ");
		// Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		// String str = formatter.format(curDate);
		// fw = new FileWriter(Environment.getExternalStorageDirectory() + "/" +
		// "test.txt", true);
		// pw = new PrintWriter(fw);
		// pw.println(str +" V/BSK" + " 网路不给力，8秒内没数据，已停止下载！"+"\r\n");
		// pw.close();
		// }catch (Exception e) {
		// e.printStackTrace();
		// }

		try {
			CannelTime();
			urlConn.disconnect();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		// System.exit(0);
		// Toast.makeText(getApplicationContext(), "网络不给力，8秒内没有数据，已停止下载！",
		// Toast.LENGTH_LONG).show();
	}

	public void timerTaskDatabase5() {
		try {
			timer5 = new Timer();
			timer5.schedule(new TimerTask() {
				
				@Override
				public void run() {
					// d:8秒的数据,c:0秒的数据
					// d:16秒的数据，c:8秒的数据
					d = b;
					if (c == d) {
						// System.exit(0);
						// asdasd();
						// new Thread(new Runnable() {
						// @Override
						// public void run() {
						// Message message = new Message();
						// message.what = 0;
						// handler.sendMessage(message); // 将Message对象发送出去
						// }
						// }).start();
						TimeAndHttpCannel();

						try {
							CannelTime();
							urlConn.disconnect();
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
					}
				}
			}, 8000, 8000);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	/*
	 * public void asd() { try { Process process = Runtime.getRuntime().exec(
	 * "logcat -d"); BufferedReader bufferedReader = new BufferedReader(new
	 * InputStreamReader(process.getInputStream()));
	 * 
	 * StringBuilder log = new StringBuilder(); String line; while ((line =
	 * bufferedReader.readLine()) != null) { log.append(line); } } catch
	 * (IOException e) { } }
	 */


	public void CannelTime() {
		try {
			b = 0;
			c = 0;
			d = 0;
			// e = 0;
			if (timer3 != null) {
				timer3.cancel();
			}
			if (timer4 != null) {
				timer4.cancel();
			}
			if (timer5 != null) {
				timer5.cancel();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void CannelTimer3() {
		handler3.postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					b = 0;
					c = 0;
					d = 0;
					// e = 0;
					if (timer3 != null) {
						timer3.cancel();
					}
					if (timer4 != null) {
						timer4.cancel();
					}
					if (timer5 != null) {
						timer5.cancel();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 1000);

	}
	
	public void getCode(){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String str = formatter.format(curDate);			
				
		try {	
			fw = new FileWriter(Environment.getExternalStorageDirectory() + "/" + "test.txt", true);
			pw = new PrintWriter(fw);			
			pw.println(str + " V/BSK" + " " + b + "\r\n");
			System.out.println(str + " V/BSK" + " " + b);
			pw.close();
		} catch (Exception e) {
		}
	}

	

	/**
	 * 将一个InputStream里面的数据写入到SD卡中
	 */
	public File write2SDFromInput(String path, String fileName, InputStream input) {
		CannelTime();
		// System.out.println(timer3);
		// System.out.println(timer3);
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
			Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
			String str = formatter.format(curDate);
			try {
				urlCode=0;
				urlCode=urlConn.getResponseCode();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}	
			fw = new FileWriter(Environment.getExternalStorageDirectory() + "/" + "test.txt", true);
			pw = new PrintWriter(fw);
			pw.println("---------------------------------------------------------");
			pw.println("\r\n");
			pw.println("开始下载,请求码为:"+urlCode);
			pw.println("\r\n");
			System.out.println("---------------------------------------------------------");
			System.out.println("开始下载,请求码为:"+urlCode);
			pw.close();
		} catch (Exception e) {
		}
		int a = 0;
		timerTaskDatabase2();
		timerTaskDatabase5();
		// timerTaskDatabase3();
		timerTaskDatabase();
		// System.out.println(input.available());
		File file = null;
		OutputStream output = null;
		try {
			creatSDDir(path);
			file = creatSDFile(path + fileName);
			output = new FileOutputStream(file);
			byte buffer[] = new byte[4 * 1024];
			while ((a = input.read(buffer)) != -1) {
				b = a + b;
				// output.write(buffer);
				output.write(buffer, 0, a);
			}
			output.flush();
			CannelTimer3();
			// CannelTime();
		} catch (Exception e) {
			e.printStackTrace();
			// Toast.makeText(MyserviceDownload.this, "停止下载",
			// Toast.LENGTH_SHORT).show();
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
