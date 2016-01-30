package com.golo.launch.download;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import org.apache.http.conn.ConnectTimeoutException;

import com.golo.launch.download.R;
import com.golo.launch.util.FileUtils;
import com.golo.launch.util.HttpDownloader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView.Validator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

//完美解决
/*
 
  */
public class Download extends Activity {
	/** Called when the activity is first created. */
	private Button downloadTxtButton;
	private Button downloadMp3Button;
	private Button downloadMp3Button2;
	private URL url = null;
	public Thread thread1 = null;
	HttpURLConnection urlConn = null;
	private WifiManager wifiManager = null;
	private Timer timer6 = null;
	// SD卡路径
	private String SDPATH;
	// 下载了多少数据，累计
	public int b = 0;
	// 每8秒时数据，存起来,这里把c初始值设为1，d设为2（如果初始值设为一样的话，那么一开始下载就没网络时，就直接c跟d相同了，而没有对比8秒的数据了）
	public int c = 1;
	// 比8秒后16秒的数据，存起来
	public int d = 2;
	public int e = 0;
	private Timer timer3 = null;
	private Timer timer4 = null;
	private Timer timer5 = null;
	Handler handler1 = null;
	Handler handler2 = null;
	// 延迟1秒关闭定时器
	Handler handler3 = null;
	// 开始下载延迟，防止取消与开始频繁操作
	Handler handlerStartDown = null;
	// 停止下载延迟
	Handler handlerStopDown = null;
	Handler handlerwhile = null;
	PrintWriter pw = null;
	FileWriter fw = null;
	boolean flag1 = false;// 停止线程的标志
	int urlCode = 0;
	String responseMessage = "无请求";
	// 获取文件总长度
	int length = 0;
	/* 记录进度条数量 */
	private int progress;
	// 下载中断次数显示控件
	private TextView tv_down_interrupt2;
	/* 更新进度条 */
	private ProgressBar mProgress;
	private MediaPlayer mpMediaPlayer = null;
	// 当前网络状态显示控件
	private TextView tv_data_state2;
	// 记录下载不了的次数
	int count = 0;
	// 下载次数
	int downCount = 0;
	// 防止机器休眠
	WakeLock m_wklk;
	// 数据开关
	public Button data_open_close;

	private TextView tv_3gtype;
	// MyThread myThread = null;
	// public Activity activity;
	// 改变网络定时器
	private Timer timerinternet = null;
	// 禁止第二次进入线程循环的判断
	String flag2 = "未开始";

	My3GInfo mi;
	// 下载百分比控件显示
	private TextView tv_baifenbi;
	// 下载次数
	private TextView tv_down_count2;
	// 网络类型控件显示
	private TextView tv_3gsignal;

	// 保证取消下载的代码运行完，再运行下载的代码，就是分开两个广播
	boolean broadcast1 = true;
	// 标志，点击停止时为了能立刻停止进度条
	boolean changePross = true;
	// 开始下载的new线程对象
	MyThread1 thread = null;
	Thread thread2 = null;
	boolean startAndStop = false;

	int count1 = 0;
	// 保证12秒内只接收一次广播（同一个）
	boolean broadcast2 = true;

	int aa = 1;
	int bb = 2;
	int count2 = 0;
	int count3 = 0;
	// 线程,作用与：a.
	// 8秒内没数据就停止下载，这里并没有停止线程，只停了定时器打印的与httpConnection（因为线程还要继续跑，8秒内没数据，就重新开始下载，一直循环）
	// b.下载过程中，每获取到一笔数据，就更新进度条
	// c.用定时器来改变网络状态与网络信号强度
	// d.显示下载的次数，累计
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0: // 在这里可以进行UI操作
				// 记录下载不了的次数
				count++;
				StopDown();
				Toast.makeText(Download.this, "网络不给力，8秒内没有数据，已停止下载！次数为：" + count, Toast.LENGTH_SHORT).show();
				tv_down_interrupt2.setText(count + "");
				PlayMusic();
				CannelTime();
				//progress=0;
				break;
			case 1:
				// 改变进度条
				// Log.v("zzzzb", "progress:++" + progress);
				mProgress.setProgress(progress);
				tv_baifenbi.setText(progress + "%");
				break;
			case 2:
				// 改变网络状态并用控件显示出来
				internetType();
				// 改变网络信号强度
				int internetType = mi.getposition();
				tv_3gsignal.setText(internetType + "");
				break;
			case 3:
				// 显示下载次数控件
				tv_down_count2.setText(downCount + "");
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.main);
		// activity=this;
		downloadTxtButton = (Button) findViewById(R.id.downloadTxt);
		downloadTxtButton.setOnClickListener(new DownloadTxtListener());

		downloadMp3Button2 = (Button) findViewById(R.id.downloadMp3_2);
		downloadMp3Button2.setOnClickListener(new DownloadMp3Listener_2());

		downloadMp3Button = (Button) findViewById(R.id.downloadMp3);
		downloadMp3Button.setOnClickListener(new DownloadMp3Listener());
		mProgress = (ProgressBar) findViewById(R.id.update_progress);
		tv_down_interrupt2 = (TextView) findViewById(R.id.tv_down_interrupt2);
		tv_data_state2 = (TextView) findViewById(R.id.tv_data_state2);
		data_open_close = (Button) findViewById(R.id.data_open_close);
		tv_3gtype = (TextView) findViewById(R.id.tv_3gtype);
		tv_baifenbi = (TextView) findViewById(R.id.tv_baifenbi);
		tv_3gsignal = (TextView) findViewById(R.id.tv_3gsignal);
		tv_down_count2 = (TextView) findViewById(R.id.tv_down_count2);
		data_open_close.setOnClickListener(new DataOpenCloseListener());
		SDPATH = Environment.getExternalStorageDirectory() + "/";
		// timerTaskDatabase3();
		handler3 = new Handler();
		handlerStartDown = new Handler();
		handlerStopDown = new Handler();
		handlerwhile = new Handler();
		wifiManager = (WifiManager) super.getSystemService(Context.WIFI_SERVICE);
		mi = new My3GInfo(Download.this);
		// 用控件显示网络状态，一打开Activity就打开网络数据
		setNetWorkStatus();
		// 设置网络状态，显示在控件（因为用广播在控件上显示状态的话，会比较慢显示）
		timerTaskInternet();
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		m_wklk = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "cn");
		// 获取相应的锁
		m_wklk.acquire();
	}

	// 6222 9800 2054 3585
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// 下载广播
		DownloadBroadcastReg();
		// 获取网络类型广播
		InternetBroadcastReg();
		// 取消下载广播
		StopDownloadBroadcastReg();
		// 打开或关闭网络广播
		OpenAndCloseBroadcastReg();
		// 打开或关闭WIFI广播
		OpenAndCloseWIFIBroadcastReg();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(DownloadBroadcast);
		unregisterReceiver(InternetBroadcast);
		unregisterReceiver(StopDownloadBroadcastReg);
		unregisterReceiver(OpenAndCloseBroadcastReg);
		unregisterReceiver(OpenAndCloseWIFIBroadcastReg);
		// 退出前把flag2设为“未开始”，便于下次进入线程循环下载，虽然这个设不设置都可以，因为调用了onDestroy后，资源全部都会释放
		flag2 = "未开始";
		// 改变网络定时器
		if (timerinternet != null) {
			timerinternet.cancel();
		}
		try {
			if (m_wklk != null && m_wklk.isHeld()) {
				m_wklk.release();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void PlayMusic() {
		try {
			// 把MP3文件放到res.raw下，没有就创建
			mpMediaPlayer = MediaPlayer.create(this, R.raw.music2);

			mpMediaPlayer.start();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 8秒没数据后，被调用，打印网络类型到logs
	public void internetTypeToLog(String types) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String str2 = formatter.format(curDate);
		try {
			fw = new FileWriter(Environment.getExternalStorageDirectory() + "/" + "test.txt", true);
			pw = new PrintWriter(fw);
			pw.println(str2 + " V/BSK" + " " + "当前网络类型为：" + types + ";   " + "下载中断:" + (count + 1) + "\r\n");
			System.out.println(str2 + " V/BSK" + " " + "当前网络类型为：" + types + ";   " + "下载中断:" + (count + 1));
			pw.close();
		} catch (Exception e) {
			Log.v("zzzzb", "我是internetTypeToLog方法，我报异常了");
			Toast.makeText(Download.this, "我是internetTypeToLog方法，我报异常了", 20).show();
			e.printStackTrace();
		}
	}

	// 用控件显示网络状态
	public void setNetWorkStatus() {
		// 获取网络状态
		if (!getNetWorkStatus(this)) {
			// 打开或关闭数据网络
			toggleMobileData(Download.this, true);
			tv_data_state2.setText("开");
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			System.exit(0);
		}
		return true;
	}

	// 获取网络状态
	public static boolean getNetWorkStatus(Context context) {
		boolean netSataus = false;
		ConnectivityManager cwjManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		cwjManager.getActiveNetworkInfo();

		if (cwjManager.getActiveNetworkInfo() != null) {
			netSataus = cwjManager.getActiveNetworkInfo().isAvailable();
		}
		return netSataus;
	}

	// 注册下载广播
	private void DownloadBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("CNLAUNCH_KEY_MODEL_TALK_DOWN");// 就是自己想要接收的系统广播
		this.registerReceiver(DownloadBroadcast, filter);
	}

	public boolean startAndStop() {
		while (true) {
			if (startAndStop == false) {
				startAndStop = true;
			} else {
				startAndStop = false;
			}
			return startAndStop;
		}

	}

	public void stopDown2() {
		try {
			flag1 = false;
			changePross = false;
			// DownloadBroadcastReg();
			CannelTime();
			try {
				if (urlConn != null) {
					urlConn.disconnect();
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	// 接收下载广播，启动下载
	private BroadcastReceiver DownloadBroadcast = new BroadcastReceiver() {
		@Override
		public synchronized void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// 保证12秒内只接收一次广播（同一个）
			if (broadcast2) {
				// 保证取消下载的代码运行完，再运行下载的代码，就是分开两个广播
				if (broadcast1) {
					if (action.equals("CNLAUNCH_KEY_MODEL_TALK_DOWN")) {
						broadcast2 = false;
						if ("未开始".equals(flag2)) {
							Toast.makeText(Download.this, "准备下载...", Toast.LENGTH_SHORT).show();
							handlerStartDown.postDelayed(new Runnable() {
								@Override
								public  void run() {
									// unregisterReceiver(DownloadBroadcast);
									broadcast2 = true;
									flag1 = true;
									changePross = true;
									thread = new MyThread1();
									thread2 = new Thread(thread);
									thread2.start();
								}
							}, 8000);
						}else{
							Toast.makeText(Download.this, "正在下载中...请勿重复操作！", Toast.LENGTH_SHORT).show();
						}
					}
				}else{
					Toast.makeText(Download.this, "停止下载后请等待5秒再开始！", Toast.LENGTH_SHORT).show();
				}
			}else{
				Toast.makeText(Download.this, "正在下载中...请勿重复操作！停止下载后请等待5秒再开始！", Toast.LENGTH_SHORT).show();
			}
		}
	};

	// 注册取消下载广播
	private void StopDownloadBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("CNLAUNCH_KEY_MODEL_SEARCH_DOWN");// 就是自己想要接收的系统广播
		this.registerReceiver(StopDownloadBroadcastReg, filter);
	}

	// 接收取消下载广播，取消下载
	private BroadcastReceiver StopDownloadBroadcastReg = new BroadcastReceiver() {
		@Override
		public synchronized void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// 保证取消下载的代码运行完，再运行下载的代码，就是分开两个广播
			if (broadcast1) {
				if (action.equals("CNLAUNCH_KEY_MODEL_SEARCH_DOWN")) {
					// handlerStopDown.postDelayed(new Runnable() {
					// public void run() {
					try {
						flag1 = false;
						changePross = false;
						broadcast1 = false;
						// DownloadBroadcastReg();
						CannelTime();
						try {
							if (urlConn != null) {
								urlConn.disconnect();
								urlConn = null;
							}
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
						Toast.makeText(Download.this, "已停止,停止下载后请等待5秒再开始!", Toast.LENGTH_SHORT).show();
						stopDownLogs();
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
					// }
					// }, 1000);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					handlerStopDown.postDelayed(new Runnable() {
						@Override
						public void run() {
							flag2 = "未开始";
							broadcast1 = true;
							broadcast2 = true;
						}
					}, 4000);

				}
			}else{
				Toast.makeText(Download.this, "已停止,请勿重复操作！", Toast.LENGTH_SHORT).show();
			}
		}
	};

	// 注册打开与关闭数据网络广播
	private void OpenAndCloseBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("CNLAUNCH_KEY_MODEL_DOWN");// 就是自己想要接收的系统广播
		this.registerReceiver(OpenAndCloseBroadcastReg, filter);
	}

	// 接收打开与关闭数据网络广播,打开或者关闭网络
	private BroadcastReceiver OpenAndCloseBroadcastReg = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("CNLAUNCH_KEY_MODEL_DOWN")) {
				if (getNetWorkStatus(Download.this)) {
					toggleMobileData(Download.this, false);
					tv_data_state2.setText("关");
					internetCloseToLogs();
				} else {
					toggleMobileData(Download.this, true);
					tv_data_state2.setText("开");
					internetOpenToLogs();
				}
			}
		}
	};

	// 注册打开与关闭WIFI广播
	private void OpenAndCloseWIFIBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("CNLAUNCH_KEY_MODEL_OK_DOWN");// 就是自己想要接收的系统广播
		this.registerReceiver(OpenAndCloseWIFIBroadcastReg, filter);
	}

	// 接收打开与关闭WIFI广播
	private BroadcastReceiver OpenAndCloseWIFIBroadcastReg = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("CNLAUNCH_KEY_MODEL_OK_DOWN")) {
				if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
					wifiManager.setWifiEnabled(false);
				} else {
					wifiManager.setWifiEnabled(true);
				}

			}
		}
	};

	// 注册广播，获取网络类型
	private void InternetBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		// filter.addAction("CNLAUNCH_KEY_MODEL_SEARCH_UP");// BC_LIGHT_SENSOR
		// //
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");// 就是自己想要接收的系统广播
		this.registerReceiver(InternetBroadcast, filter);
	}

	// 接收广播，获取网络类型
	private BroadcastReceiver InternetBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
				// Log.v("zzzzb", "action:+" + action);
				internetType();

			}
		}
	};

	// 停止下载to打印Logs
	public void stopDownLogs() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String str = formatter.format(curDate);
		try {
			fw = new FileWriter(Environment.getExternalStorageDirectory() + "/" + "test.txt", true);
			pw = new PrintWriter(fw);
			pw.println(str + " V/BSK" + " " + "已停止下载" + "\r\n");
			System.out.println(str + " V/BSK" + " " + "已停止下载");
			// }
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 设置网络状态，显示在控件（因为用广播在控件上显示状态的话，会比较慢显示）
	public void timerTaskInternet() {
		try {
			timerinternet = new Timer();
			timerinternet.schedule(new TimerTask() {

				@Override
				public void run() {
					Message message = new Message();
					message.what = 2;
					handler.sendMessage(message);
				}
			}, 0, 1000);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	// 改变网络状态并用控件显示出来
	// 联通的3G为UMTS或HSDPA，移动和联通的2G为GPRS或EDGE，电信的2G为CDMA，电信的3G为EVDO
	public void internetType() {
		ConnectivityManager connectMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectMgr.getActiveNetworkInfo();
		if (info != null) {
			// Log.v("zzzzb", "info.getType():+" + info.getType());
			if (info.getType() == ConnectivityManager.TYPE_WIFI) {
				// Log.v("zzzzb", "ConnectivityManager.TYPE_WIFI:++" +
				// ConnectivityManager.TYPE_WIFI);
				tv_3gtype.setText("WIFI");
			} else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
				int subType = info.getSubtype();
				// Log.v("zzzzb", "info.getSubtype():+" + info.getSubtype());
				if (subType == TelephonyManager.NETWORK_TYPE_CDMA) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_CDMA:++" +
					// TelephonyManager.NETWORK_TYPE_CDMA);
					tv_3gtype.setText("CDMA");
				} else if (subType == TelephonyManager.NETWORK_TYPE_EDGE) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_EDGE:++" +
					// TelephonyManager.NETWORK_TYPE_EDGE);
					tv_3gtype.setText("EDGE");
				} else if (subType == TelephonyManager.NETWORK_TYPE_EVDO_0) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_EVDO_0:++"
					// + TelephonyManager.NETWORK_TYPE_EVDO_0);
					tv_3gtype.setText("EVDO_0");
				} else if (subType == TelephonyManager.NETWORK_TYPE_1xRTT) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_EVDO_0:++"
					// + TelephonyManager.NETWORK_TYPE_EVDO_0);
					tv_3gtype.setText("1xRTT");
				} else if (subType == TelephonyManager.NETWORK_TYPE_EVDO_A) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_EVDO_A:++"
					// + TelephonyManager.NETWORK_TYPE_EVDO_A);
					tv_3gtype.setText("EVDO_A");
				} else if (subType == TelephonyManager.NETWORK_TYPE_HSPAP) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_EVDO_A:++"
					// + TelephonyManager.NETWORK_TYPE_EVDO_A);
					tv_3gtype.setText("HSPAP");
				} else if (subType == TelephonyManager.NETWORK_TYPE_GPRS) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_GPRS:++" +
					// TelephonyManager.NETWORK_TYPE_GPRS);
					tv_3gtype.setText("GPRS");
				} else if (subType == TelephonyManager.NETWORK_TYPE_HSDPA) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_HSDPA:++" +
					// TelephonyManager.NETWORK_TYPE_HSDPA);
					tv_3gtype.setText("HSDPA");
				} else if (subType == TelephonyManager.NETWORK_TYPE_HSPA) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_HSPA:++" +
					// TelephonyManager.NETWORK_TYPE_HSPA);
					tv_3gtype.setText("HSPA");
				} else if (subType == TelephonyManager.NETWORK_TYPE_HSUPA) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_HSUPA:++" +
					// TelephonyManager.NETWORK_TYPE_HSUPA);
					tv_3gtype.setText("HSUPA");
				} else if (subType == TelephonyManager.NETWORK_TYPE_UMTS) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_UMTS:++" +
					// TelephonyManager.NETWORK_TYPE_UMTS);
					tv_3gtype.setText("UMTS");
				} else if (subType == TelephonyManager.NETWORK_TYPE_IDEN) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_UMTS:++" +
					// TelephonyManager.NETWORK_TYPE_UMTS);
					tv_3gtype.setText("IDEN");
				} else if (subType == TelephonyManager.NETWORK_TYPE_EVDO_B) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_UMTS:++" +
					// TelephonyManager.NETWORK_TYPE_UMTS);
					tv_3gtype.setText("EVDO_B");
				} else if (subType == TelephonyManager.NETWORK_TYPE_EHRPD) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_UMTS:++" +
					// TelephonyManager.NETWORK_TYPE_UMTS);
					tv_3gtype.setText("EHRPD");
				} else if (subType == TelephonyManager.NETWORK_TYPE_LTE) {
					// Log.v("zzzzb", "TelephonyManager.NETWORK_TYPE_LTE:++"
					// +
					// TelephonyManager.NETWORK_TYPE_LTE);
					tv_3gtype.setText("LTE");
				}
			}

		} else {
			tv_3gtype.setText("无网络");
		}

	}

	// 当8秒没数据的定时器内进入if语句的话，就调用这个方法，打印当前网络的状态到Logs
	public void getInternetTypeToLogs() {
		ConnectivityManager connectMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectMgr.getActiveNetworkInfo();
		if (info != null) {
			// Log.v("zzzzb", "info.getType():+" + info.getType());
			if (info.getType() == ConnectivityManager.TYPE_WIFI) {
				// Log.v("zzzzb", "ConnectivityManager.TYPE_WIFI:++" +
				// ConnectivityManager.TYPE_WIFI);
				internetTypeToLog("WIFI");
			} else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
				int subType = info.getSubtype();
				// Log.v("zzzzb", "info.getSubtype():+" + info.getSubtype());
				if (subType == TelephonyManager.NETWORK_TYPE_CDMA) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_CDMA:++" +
					// TelephonyManager.NETWORK_TYPE_CDMA);
					internetTypeToLog("CDMA");
				} else if (subType == TelephonyManager.NETWORK_TYPE_EDGE) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_EDGE:++" +
					// TelephonyManager.NETWORK_TYPE_EDGE);
					internetTypeToLog("EDGE");
				} else if (subType == TelephonyManager.NETWORK_TYPE_1xRTT) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_EDGE:++" +
					// TelephonyManager.NETWORK_TYPE_EDGE);
					internetTypeToLog("1xRTT");
				} else if (subType == TelephonyManager.NETWORK_TYPE_EVDO_B) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_EDGE:++" +
					// TelephonyManager.NETWORK_TYPE_EDGE);
					internetTypeToLog("EVDO_B");
				} else if (subType == TelephonyManager.NETWORK_TYPE_IDEN) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_EDGE:++" +
					// TelephonyManager.NETWORK_TYPE_EDGE);
					internetTypeToLog("IDEN");
				} else if (subType == TelephonyManager.NETWORK_TYPE_EHRPD) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_EDGE:++" +
					// TelephonyManager.NETWORK_TYPE_EDGE);
					internetTypeToLog("EHRPD");
				} else if (subType == TelephonyManager.NETWORK_TYPE_EVDO_0) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_EVDO_0:++"
					// + TelephonyManager.NETWORK_TYPE_EVDO_0);
					internetTypeToLog("EVDO_0");
				} else if (subType == TelephonyManager.NETWORK_TYPE_EVDO_A) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_EVDO_A:++"
					// + TelephonyManager.NETWORK_TYPE_EVDO_A);
					internetTypeToLog("EVDO_A");
				} else if (subType == TelephonyManager.NETWORK_TYPE_HSPAP) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_EVDO_A:++"
					// + TelephonyManager.NETWORK_TYPE_EVDO_A);
					internetTypeToLog("HSPAP");
				} else if (subType == TelephonyManager.NETWORK_TYPE_GPRS) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_GPRS:++" +
					// TelephonyManager.NETWORK_TYPE_GPRS);
					internetTypeToLog("GPRS");
				} else if (subType == TelephonyManager.NETWORK_TYPE_HSDPA) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_HSDPA:++" +
					// TelephonyManager.NETWORK_TYPE_HSDPA);
					internetTypeToLog("HSDPA");
				} else if (subType == TelephonyManager.NETWORK_TYPE_HSPA) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_HSPA:++" +
					// TelephonyManager.NETWORK_TYPE_HSPA);
					internetTypeToLog("HSPA");
				} else if (subType == TelephonyManager.NETWORK_TYPE_HSUPA) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_HSUPA:++" +
					// TelephonyManager.NETWORK_TYPE_HSUPA);
					internetTypeToLog("HSUPA");
				} else if (subType == TelephonyManager.NETWORK_TYPE_UMTS) {
					// Log.v("zzzzb",
					// "TelephonyManager.NETWORK_TYPE_UMTS:++" +
					// TelephonyManager.NETWORK_TYPE_UMTS);
					internetTypeToLog("UMTS");
				} else if (subType == TelephonyManager.NETWORK_TYPE_LTE) {
					// Log.v("zzzzb", "TelephonyManager.NETWORK_TYPE_LTE:++"
					// +
					// TelephonyManager.NETWORK_TYPE_LTE);
					internetTypeToLog("LTE");
				}
			}

		} else {
			internetTypeToLog("无网络");
		}

	}

	// 没用到
	class DownloadTxtListener implements OnClickListener {

		public void onClick(View v) {
			FileUtils asd = new FileUtils();
			asd.CannelTime2();
		}
	}

	// 停止下载
	class DownloadMp3Listener_2 implements OnClickListener {
		public void onClick(View v) {
			if (broadcast1) {
				// handlerStopDown.postDelayed(new Runnable() {
				// public void run() {
				try {
					flag1 = false;
					changePross = false;
					broadcast1 = false;
					// DownloadBroadcastReg();
					CannelTime();
					try {
						if (urlConn != null) {
							urlConn.disconnect();
							urlConn = null;
						}
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
					Toast.makeText(Download.this, "已停止下载", Toast.LENGTH_SHORT).show();
					stopDownLogs();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				// }
				// }, 1000);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				handlerStopDown.postDelayed(new Runnable() {
					@Override
					public void run() {
						flag2 = "未开始";
						broadcast1 = true;
						broadcast2 = true;
					}
				}, 4000);

			}
		}
	}

	// 通过点击按钮来启动下载
	class DownloadMp3Listener implements OnClickListener {
		public void onClick(View v) {
			// 保证12秒内只接收一次广播（同一个）
			if (broadcast2) {
				// 保证取消下载的代码运行完，再运行下载的代码，就是分开两个广播
				if (broadcast1) {
					broadcast2 = false;
					if ("未开始".equals(flag2)) {
						handlerStartDown.postDelayed(new Runnable() {
							@Override
							public void run() {
								// unregisterReceiver(DownloadBroadcast);
								broadcast2 = true;
								flag1 = true;
								changePross = true;
								thread = new MyThread1();
								thread2 = new Thread(thread);
								thread2.start();
							}
						}, 12000);
					}
				}
			}
		}
	}

	// 设置网络开关
	class DataOpenCloseListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			// 获取网络状态并显示
			// setNetWorkStatus();
			// Log.v("zzzzb", "我在开关按钮点击事件");
			// toggleMobileData(Download.this, false);
			if (getNetWorkStatus(Download.this)) {
				toggleMobileData(Download.this, false);
				tv_data_state2.setText("关");
				internetCloseToLogs();
			} else {
				toggleMobileData(Download.this, true);
				tv_data_state2.setText("开");
				internetOpenToLogs();
			}
		}
	}

	// 网络打开打印Logs
	public void internetOpenToLogs() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String str = formatter.format(curDate);
		try {
			// 这里创建路径为：sd卡的路径/test.txt；后面有一个true，代表如果这个test.txt一直存在的话，以后的内容就直接追加到test.txt
			// 内容的后面，不会覆盖；如果没加true，就代表覆盖
			fw = new FileWriter(Environment.getExternalStorageDirectory() + "/" + "test.txt", true);
			// new一个写的对象
			pw = new PrintWriter(fw);
			// if (b == e && b != 0 && e != 0) {
			// pw.println(str + " V/BSK" + " " + b + " 000000" +
			// "\r\n");
			// } else {
			pw.println(str + " V/BSK" + " " + "网络已打开" + "\r\n");
			System.out.println(str + " V/BSK" + " " + b);
			// }
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 网络关闭打印Logs
	public void internetCloseToLogs() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String str = formatter.format(curDate);
		try {
			// 这里创建路径为：sd卡的路径/test.txt；后面有一个true，代表如果这个test.txt一直存在的话，以后的内容就直接追加到test.txt
			// 内容的后面，不会覆盖；如果没加true，就代表覆盖
			fw = new FileWriter(Environment.getExternalStorageDirectory() + "/" + "test.txt", true);
			// new一个写的对象
			pw = new PrintWriter(fw);
			// if (b == e && b != 0 && e != 0) {
			// pw.println(str + " V/BSK" + " " + b + " 000000" +
			// "\r\n");
			// } else {
			pw.println(str + " V/BSK" + " " + "网络已关闭" + "\r\n");
			System.out.println(str + " V/BSK" + " " + b);
			// }
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 打开或关闭数据网络
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

	// 下载的关键方法
	public int downFile(String urlStr, String path, String fileName) {
		
		progress = 0;
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
				if (inputStream != null) {
					inputStream.close();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	

	// 打开http链接，获取getInputStream（）
	public InputStream getInputStreamFromUrl(String urlStr) {
		// throws MalformedURLException,IOException
		try {
			if (urlConn != null) {
				urlConn.disconnect();
				urlConn = null;
			}

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
			// 设置方法
			urlConn.setRequestMethod("GET");
			// 设置参数
			urlConn.setRequestProperty("Accept",
					"image/gif, image/jpeg, image/pjpeg, image/pjpeg, "
							+ "application/x-shockwave-flash, application/xaml+xml, "
							+ "application/vnd.ms-xpsdocument, application/x-ms-xbap, "
							+ "application/x-ms-application, application/vnd.ms-excel, "
							+ "application/vnd.ms-powerpoint, application/msword, */*");
			urlConn.setRequestProperty("Accept-Language", "zh-CN");
			urlConn.setRequestProperty("Charset", "UTF-8");
			urlConn.setRequestProperty("Connection", "Keep-Alive");
			length = urlConn.getContentLength();
			InputStream inputStream = urlConn.getInputStream();

			/*
			 * if(urlConn.getReadTimeout()==5000){ Toast.makeText(Download.this,
			 * "超时1秒", Toast.LENGTH_SHORT).show(); }
			 */
			return inputStream;

		} catch (MalformedURLException e) {
			// TODO: handle exception
			Toast.makeText(Download.this, "链接错误", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			// TODO: handle exception
			if (e instanceof ConnectTimeoutException) {
				Toast.makeText(Download.this, "连接超时", Toast.LENGTH_SHORT).show();
			} else if (e instanceof SocketTimeoutException) {
				Toast.makeText(Download.this, "响应超时", Toast.LENGTH_SHORT).show();
			}
		}
		return null;
	}

	/*
	 * class MyThread implements Runnable { public void run() { // 处理具体的逻辑
	 * //Toast.makeText(Download.this, "开始下载", Toast.LENGTH_SHORT).show(); int
	 * result = downFile(
	 * "http://res.media.golo5.com/topic/music/201501/20150122/c0472e02074cee47d72a25dc4f8f60b2_96.mp3"
	 * , "voa/", "a1.mp3"); int result = downFile(
	 * "http://res.media.golo5.com/topic/music/201507/20150720/b5bdaa986693bc0821db7bd4574b922a_128.mp3"
	 * , "voa/", "a1.mp3"); System.out.println(result); } }
	 */

	// 用来下载的线程
	class MyThread1 implements Runnable {
		public void run() {
			while (flag1) {
				flag2 = "已在下载";

				StopDown();

				int result = downFile(
						"http://res.media.golo5.com/topic/music/201501/20150122/c0472e02074cee47d72a25dc4f8f60b2_96.mp3",
						"voa/", "a1.mp3");

				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				downCount();

				try {
					Thread.sleep(8000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	// 打印与显示下载次数
	public void downCount() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String str = formatter.format(curDate);
		downCount++;
		try {
			fw = new FileWriter(Environment.getExternalStorageDirectory() + "/" + "test.txt", true);
			pw = new PrintWriter(fw);
			pw.println(str + " V/BSK" + " " + "下载次数：" + downCount + "\r\n");
			System.out.println(str + " V/BSK" + " " + "下载次数：" + downCount);
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Message message = new Message();
		message.what = 3;
		handler.sendMessage(message);
	}

	// 返回一个SD卡路径
	public String getSDPATH() {
		return SDPATH;
	}

	/**
	 * 在SD卡上创建文件
	 * 
	 * @throws IOException
	 */
	// 在SD卡上创建了目录后，再创建文件
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
	// 在SD卡上先创建目录
	public File creatSDDir(String dirName) {
		File dir = new File(SDPATH + dirName);
		dir.mkdir();
		return dir;
	}

	/**
	 * 判断SD卡上的文件是否存在
	 */
	// 判断SD卡的文件是否存在
	public boolean isFileExist(String fileName) {
		File file = new File(SDPATH + fileName);
		return file.exists();
	}

	// 每秒打印获取到的数据
	public void timerTaskDatabase() {
		try {
			if (timer3 == null) {
				timer3 = new Timer();
			}

			timer3.schedule(new TimerTask() {
				@Override
				public void run() {
					if (changePross) {
						count2++;
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
						Date curDate = new Date(System.currentTimeMillis());
						String str = formatter.format(curDate);
						try {
							fw = new FileWriter(Environment.getExternalStorageDirectory() + "/" + "test.txt", true);
							pw = new PrintWriter(fw);
							pw.println(str + " V/BSK" + " " + b + "\r\n");
							System.out.println(str + " V/BSK" + " " + b);
							pw.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
						// 这一秒存给aa
						if (count2 % 2 == 0) {
							aa = b;
						} else {
							// 下一秒存给bb
							bb = b;
						}
						// 每秒对比bb与aa 的数据，如果相同，count3就++，如果有一次不相同，count3就清零
						if (bb == aa) {
							count3++;
						} else {
							count3 = 0;
						}
						// 如果count3等于8,就等于说8秒内的数据都一样，这样就停止下载，并打印logs
						if (count3 == 7) {
							getInternetTypeToLogs();
							Message message = new Message();
							message.what = 0;
							handler.sendMessage(message); // 将Message对象发送出去
							try {
								CannelTime();
								urlConn.disconnect();
							} catch (Exception e) {
								// TODO: handle exception
								e.printStackTrace();
							}
						}
					}
				}

			}, 0, 1000);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	// 每秒打印获取到的数据
	public void timerTaskDatabase11() {
		try {
			if (timer3 == null) {
				timer3 = new Timer();
			}
			timer3.schedule(new TimerTask() {
				@Override
				public void run() {
					if (changePross) {
						try {
							urlCode = 0;
							// responseMessage="";
							// urlConn.disconnect();
							urlCode = urlConn.getResponseCode();
							// responseMessage= urlConn.getResponseMessage();
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
						String ab = b + "";
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
						Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
						String str = formatter.format(curDate);
						try {
							// 这里创建路径为：sd卡的路径/test.txt；后面有一个true，代表如果这个test.txt一直存在的话，以后的内容就直接追加到test.txt
							// 内容的后面，不会覆盖；如果没加true，就代表覆盖
							fw = new FileWriter(Environment.getExternalStorageDirectory() + "/" + "test.txt", true);
							// new一个写的对象
							pw = new PrintWriter(fw);
							// if (b == e && b != 0 && e != 0) {
							// pw.println(str + " V/BSK" + " " + b + " 000000" +
							// "\r\n");
							// } else {
							pw.println(str + " V/BSK" + " " + b + "\r\n");
							System.out.println(str + " V/BSK" + " " + b);
							// }
							pw.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

			}, 0, 1000);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	// 有了这个方法，就没用到timerTaskDatabase5了，因为8秒数据的存与对比都在这里执行了
	public void timerTaskDatabase2() {
		try {
			if (timer4 == null) {
				timer4 = new Timer();
			}
			timer4.schedule(new TimerTask() {
				@Override
				public void run() {
					count1++;
					if (count1 % 2 == 0) {
						d = b;
					} else {
						c = b;
					}
					if (c == d) {
						getInternetTypeToLogs();
						Message message = new Message();
						message.what = 0;
						handler.sendMessage(message); // 将Message对象发送出去
						try {
							CannelTime();
							urlConn.disconnect();
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
					}
				}
			}, 0, 8000);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	// 定时器1，每8秒执行一次，把下载的多少数据存到变量c（c存的数据就是0秒，8秒，16秒，24秒，32秒.....）

	// 定时器2，开始延迟8秒执行，后面每8秒执行一次，里面的操作：
	// 1.把下载的多少数据存到变量d（d存的数据就是8秒，16秒，24秒，32秒）
	// 2.对比c与d的数据是否相同，若相同，停止下载

	// 8秒没数据后调用该方法
	public void StopDown() {
		try {
			CannelTime();
			if (urlConn != null) {
				urlConn.disconnect();
				urlConn = null;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	// 没用到
	// 比较“现在”与“前8秒的数据”，比如：现在是16秒，那么比较的是之前8秒时的数据与16秒（现在）的数据
	public void timerTaskDatabase5() {
		try {
			if (timer5 == null) {
				timer5 = new Timer();
			}
			timer5.schedule(new TimerTask() {

				@Override
				public void run() {
					// d:8秒的数据,c:0秒的数据
					// d:16秒的数据，c:8秒的数据
					if (changePross) {
						d = b;
						if (c == d) {
							// System.exit(0);
							// asdasd();
							getInternetTypeToLogs();

							Message message = new Message();
							message.what = 0;
							handler.sendMessage(message); // 将Message对象发送出去

							try {
								CannelTime();
								urlConn.disconnect();
							} catch (Exception e) {
								// TODO: handle exception
								e.printStackTrace();
							}
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

	// 因为涉及到打印，所以每次下载前都调用次方法，清空b,c,d，停止定时器
	public void CannelTime() {
		try {
			b = 0;
			c = 1;
			d = 2;
			count1 = 0;

			aa = 1;
			bb = 2;
			count2 = 0;
			count3 = 0;

			// e = 0;
			if (timer3 != null) {
				timer3.cancel();
				timer3 = null;
				
			}
			if (timer4 != null) {
				timer4.cancel();
				timer4 = null;
			}
			if (timer5 != null) {
				timer5.cancel();
				timer5 = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 因为涉及到打印，所以每次下载前都调用此方法，清空b,c,d，停止定时器
	public void CannelTimer3() {
		handler3.postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					b = 0;
					c = 1;
					d = 2;
					count1 = 0;

					aa = 1;
					bb = 2;
					count2 = 0;
					count3 = 0;

					// e = 0;
					if (timer3 != null) {
						timer3.cancel();
						timer3 = null;
					}
					if (timer4 != null) {
						timer4.cancel();
						timer4 = null;
					}
					if (timer5 != null) {
						timer5.cancel();
						timer5 = null;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 1000);
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
				urlCode = 0;
				urlCode = urlConn.getResponseCode();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			fw = new FileWriter(Environment.getExternalStorageDirectory() + "/" + "test.txt", true);
			pw = new PrintWriter(fw);
			pw.println("---------------------------------------------------------");
			pw.println("\r\n");
			// pw.println("开始下载,请求码为:" + urlCode);
			pw.println("开始下载:");
			pw.println("\r\n");
			System.out.println("---------------------------------------------------------");
			// System.out.println("开始下载,请求码为:" + urlCode);
			System.out.println("开始下载:");
			pw.close();
		} catch (Exception e) {

			e.printStackTrace();
		}
		int a = 0;
		// timerTaskDatabase2();
		timerTaskDatabase();
		File file = null;
		OutputStream output = null;
		try {
			creatSDDir(path);
			file = creatSDFile(path + fileName);
			output = new FileOutputStream(file);
			// 缓冲区，设为4*1024，缓冲区应该就是每次从getInputStream过来数据暂时存放的地方，然后再从缓冲区写入到SD卡
			byte buffer[] = new byte[1024];
			while ((a = input.read(buffer)) != -1) {
				// a是每次读取缓冲区的数据，每次都给b，那么b就是下载了多少数据
				// 无论是下载中突然断网，还是下载中没断网但获取不到数据，都是会直接跳到catch语句
				b = a + b;
				progress = (int) (((float) b / length) * 100);
				if (changePross) {
					handler.sendEmptyMessage(1);
				}

				output.write(buffer, 0, a);
			}
			// 写完后关闭流
			output.flush();
			CannelTimer3();
			// CannelTime();
		} catch (Exception e) {
			// CannelTime();
			e.printStackTrace();
		} finally {
			try {
				if (output != null) {
					output.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return file;
	}

}