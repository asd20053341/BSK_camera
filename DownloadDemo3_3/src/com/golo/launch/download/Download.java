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

//�������
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
	// SD��·��
	private String SDPATH;
	// �����˶������ݣ��ۼ�
	public int b = 0;
	// ÿ8��ʱ���ݣ�������,�����c��ʼֵ��Ϊ1��d��Ϊ2�������ʼֵ��Ϊһ���Ļ�����ôһ��ʼ���ؾ�û����ʱ����ֱ��c��d��ͬ�ˣ���û�жԱ�8��������ˣ�
	public int c = 1;
	// ��8���16������ݣ�������
	public int d = 2;
	public int e = 0;
	private Timer timer3 = null;
	private Timer timer4 = null;
	private Timer timer5 = null;
	Handler handler1 = null;
	Handler handler2 = null;
	// �ӳ�1��رն�ʱ��
	Handler handler3 = null;
	// ��ʼ�����ӳ٣���ֹȡ���뿪ʼƵ������
	Handler handlerStartDown = null;
	// ֹͣ�����ӳ�
	Handler handlerStopDown = null;
	Handler handlerwhile = null;
	PrintWriter pw = null;
	FileWriter fw = null;
	boolean flag1 = false;// ֹͣ�̵߳ı�־
	int urlCode = 0;
	String responseMessage = "������";
	// ��ȡ�ļ��ܳ���
	int length = 0;
	/* ��¼���������� */
	private int progress;
	// �����жϴ�����ʾ�ؼ�
	private TextView tv_down_interrupt2;
	/* ���½����� */
	private ProgressBar mProgress;
	private MediaPlayer mpMediaPlayer = null;
	// ��ǰ����״̬��ʾ�ؼ�
	private TextView tv_data_state2;
	// ��¼���ز��˵Ĵ���
	int count = 0;
	// ���ش���
	int downCount = 0;
	// ��ֹ��������
	WakeLock m_wklk;
	// ���ݿ���
	public Button data_open_close;

	private TextView tv_3gtype;
	// MyThread myThread = null;
	// public Activity activity;
	// �ı����綨ʱ��
	private Timer timerinternet = null;
	// ��ֹ�ڶ��ν����߳�ѭ�����ж�
	String flag2 = "δ��ʼ";

	My3GInfo mi;
	// ���ذٷֱȿؼ���ʾ
	private TextView tv_baifenbi;
	// ���ش���
	private TextView tv_down_count2;
	// �������Ϳؼ���ʾ
	private TextView tv_3gsignal;

	// ��֤ȡ�����صĴ��������꣬���������صĴ��룬���Ƿֿ������㲥
	boolean broadcast1 = true;
	// ��־�����ֹͣʱΪ��������ֹͣ������
	boolean changePross = true;
	// ��ʼ���ص�new�̶߳���
	MyThread1 thread = null;
	Thread thread2 = null;
	boolean startAndStop = false;

	int count1 = 0;
	// ��֤12����ֻ����һ�ι㲥��ͬһ����
	boolean broadcast2 = true;

	int aa = 1;
	int bb = 2;
	int count2 = 0;
	int count3 = 0;
	// �߳�,�����룺a.
	// 8����û���ݾ�ֹͣ���أ����ﲢû��ֹͣ�̣߳�ֻͣ�˶�ʱ����ӡ����httpConnection����Ϊ�̻߳�Ҫ�����ܣ�8����û���ݣ������¿�ʼ���أ�һֱѭ����
	// b.���ع����У�ÿ��ȡ��һ�����ݣ��͸��½�����
	// c.�ö�ʱ�����ı�����״̬�������ź�ǿ��
	// d.��ʾ���صĴ������ۼ�
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0: // ��������Խ���UI����
				// ��¼���ز��˵Ĵ���
				count++;
				StopDown();
				Toast.makeText(Download.this, "���粻������8����û�����ݣ���ֹͣ���أ�����Ϊ��" + count, Toast.LENGTH_SHORT).show();
				tv_down_interrupt2.setText(count + "");
				PlayMusic();
				CannelTime();
				//progress=0;
				break;
			case 1:
				// �ı������
				// Log.v("zzzzb", "progress:++" + progress);
				mProgress.setProgress(progress);
				tv_baifenbi.setText(progress + "%");
				break;
			case 2:
				// �ı�����״̬���ÿؼ���ʾ����
				internetType();
				// �ı������ź�ǿ��
				int internetType = mi.getposition();
				tv_3gsignal.setText(internetType + "");
				break;
			case 3:
				// ��ʾ���ش����ؼ�
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
		// �ÿؼ���ʾ����״̬��һ��Activity�ʹ���������
		setNetWorkStatus();
		// ��������״̬����ʾ�ڿؼ�����Ϊ�ù㲥�ڿؼ�����ʾ״̬�Ļ�����Ƚ�����ʾ��
		timerTaskInternet();
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		m_wklk = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "cn");
		// ��ȡ��Ӧ����
		m_wklk.acquire();
	}

	// 6222 9800 2054 3585
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// ���ع㲥
		DownloadBroadcastReg();
		// ��ȡ�������͹㲥
		InternetBroadcastReg();
		// ȡ�����ع㲥
		StopDownloadBroadcastReg();
		// �򿪻�ر�����㲥
		OpenAndCloseBroadcastReg();
		// �򿪻�ر�WIFI�㲥
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
		// �˳�ǰ��flag2��Ϊ��δ��ʼ���������´ν����߳�ѭ�����أ���Ȼ����費���ö����ԣ���Ϊ������onDestroy����Դȫ�������ͷ�
		flag2 = "δ��ʼ";
		// �ı����綨ʱ��
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
			// ��MP3�ļ��ŵ�res.raw�£�û�оʹ���
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

	// 8��û���ݺ󣬱����ã���ӡ�������͵�logs
	public void internetTypeToLog(String types) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy��MM��dd�� HH:mm:ss ");
		Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
		String str2 = formatter.format(curDate);
		try {
			fw = new FileWriter(Environment.getExternalStorageDirectory() + "/" + "test.txt", true);
			pw = new PrintWriter(fw);
			pw.println(str2 + " V/BSK" + " " + "��ǰ��������Ϊ��" + types + ";   " + "�����ж�:" + (count + 1) + "\r\n");
			System.out.println(str2 + " V/BSK" + " " + "��ǰ��������Ϊ��" + types + ";   " + "�����ж�:" + (count + 1));
			pw.close();
		} catch (Exception e) {
			Log.v("zzzzb", "����internetTypeToLog�������ұ��쳣��");
			Toast.makeText(Download.this, "����internetTypeToLog�������ұ��쳣��", 20).show();
			e.printStackTrace();
		}
	}

	// �ÿؼ���ʾ����״̬
	public void setNetWorkStatus() {
		// ��ȡ����״̬
		if (!getNetWorkStatus(this)) {
			// �򿪻�ر���������
			toggleMobileData(Download.this, true);
			tv_data_state2.setText("��");
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

	// ��ȡ����״̬
	public static boolean getNetWorkStatus(Context context) {
		boolean netSataus = false;
		ConnectivityManager cwjManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		cwjManager.getActiveNetworkInfo();

		if (cwjManager.getActiveNetworkInfo() != null) {
			netSataus = cwjManager.getActiveNetworkInfo().isAvailable();
		}
		return netSataus;
	}

	// ע�����ع㲥
	private void DownloadBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("CNLAUNCH_KEY_MODEL_TALK_DOWN");// �����Լ���Ҫ���յ�ϵͳ�㲥
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

	// �������ع㲥����������
	private BroadcastReceiver DownloadBroadcast = new BroadcastReceiver() {
		@Override
		public synchronized void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// ��֤12����ֻ����һ�ι㲥��ͬһ����
			if (broadcast2) {
				// ��֤ȡ�����صĴ��������꣬���������صĴ��룬���Ƿֿ������㲥
				if (broadcast1) {
					if (action.equals("CNLAUNCH_KEY_MODEL_TALK_DOWN")) {
						broadcast2 = false;
						if ("δ��ʼ".equals(flag2)) {
							Toast.makeText(Download.this, "׼������...", Toast.LENGTH_SHORT).show();
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
							Toast.makeText(Download.this, "����������...�����ظ�������", Toast.LENGTH_SHORT).show();
						}
					}
				}else{
					Toast.makeText(Download.this, "ֹͣ���غ���ȴ�5���ٿ�ʼ��", Toast.LENGTH_SHORT).show();
				}
			}else{
				Toast.makeText(Download.this, "����������...�����ظ�������ֹͣ���غ���ȴ�5���ٿ�ʼ��", Toast.LENGTH_SHORT).show();
			}
		}
	};

	// ע��ȡ�����ع㲥
	private void StopDownloadBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("CNLAUNCH_KEY_MODEL_SEARCH_DOWN");// �����Լ���Ҫ���յ�ϵͳ�㲥
		this.registerReceiver(StopDownloadBroadcastReg, filter);
	}

	// ����ȡ�����ع㲥��ȡ������
	private BroadcastReceiver StopDownloadBroadcastReg = new BroadcastReceiver() {
		@Override
		public synchronized void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// ��֤ȡ�����صĴ��������꣬���������صĴ��룬���Ƿֿ������㲥
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
						Toast.makeText(Download.this, "��ֹͣ,ֹͣ���غ���ȴ�5���ٿ�ʼ!", Toast.LENGTH_SHORT).show();
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
							flag2 = "δ��ʼ";
							broadcast1 = true;
							broadcast2 = true;
						}
					}, 4000);

				}
			}else{
				Toast.makeText(Download.this, "��ֹͣ,�����ظ�������", Toast.LENGTH_SHORT).show();
			}
		}
	};

	// ע�����ر���������㲥
	private void OpenAndCloseBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("CNLAUNCH_KEY_MODEL_DOWN");// �����Լ���Ҫ���յ�ϵͳ�㲥
		this.registerReceiver(OpenAndCloseBroadcastReg, filter);
	}

	// ���մ���ر���������㲥,�򿪻��߹ر�����
	private BroadcastReceiver OpenAndCloseBroadcastReg = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("CNLAUNCH_KEY_MODEL_DOWN")) {
				if (getNetWorkStatus(Download.this)) {
					toggleMobileData(Download.this, false);
					tv_data_state2.setText("��");
					internetCloseToLogs();
				} else {
					toggleMobileData(Download.this, true);
					tv_data_state2.setText("��");
					internetOpenToLogs();
				}
			}
		}
	};

	// ע�����ر�WIFI�㲥
	private void OpenAndCloseWIFIBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("CNLAUNCH_KEY_MODEL_OK_DOWN");// �����Լ���Ҫ���յ�ϵͳ�㲥
		this.registerReceiver(OpenAndCloseWIFIBroadcastReg, filter);
	}

	// ���մ���ر�WIFI�㲥
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

	// ע��㲥����ȡ��������
	private void InternetBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		// filter.addAction("CNLAUNCH_KEY_MODEL_SEARCH_UP");// BC_LIGHT_SENSOR
		// //
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");// �����Լ���Ҫ���յ�ϵͳ�㲥
		this.registerReceiver(InternetBroadcast, filter);
	}

	// ���չ㲥����ȡ��������
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

	// ֹͣ����to��ӡLogs
	public void stopDownLogs() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy��MM��dd�� HH:mm:ss ");
		Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
		String str = formatter.format(curDate);
		try {
			fw = new FileWriter(Environment.getExternalStorageDirectory() + "/" + "test.txt", true);
			pw = new PrintWriter(fw);
			pw.println(str + " V/BSK" + " " + "��ֹͣ����" + "\r\n");
			System.out.println(str + " V/BSK" + " " + "��ֹͣ����");
			// }
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ��������״̬����ʾ�ڿؼ�����Ϊ�ù㲥�ڿؼ�����ʾ״̬�Ļ�����Ƚ�����ʾ��
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

	// �ı�����״̬���ÿؼ���ʾ����
	// ��ͨ��3GΪUMTS��HSDPA���ƶ�����ͨ��2GΪGPRS��EDGE�����ŵ�2GΪCDMA�����ŵ�3GΪEVDO
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
			tv_3gtype.setText("������");
		}

	}

	// ��8��û���ݵĶ�ʱ���ڽ���if���Ļ����͵��������������ӡ��ǰ�����״̬��Logs
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
			internetTypeToLog("������");
		}

	}

	// û�õ�
	class DownloadTxtListener implements OnClickListener {

		public void onClick(View v) {
			FileUtils asd = new FileUtils();
			asd.CannelTime2();
		}
	}

	// ֹͣ����
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
					Toast.makeText(Download.this, "��ֹͣ����", Toast.LENGTH_SHORT).show();
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
						flag2 = "δ��ʼ";
						broadcast1 = true;
						broadcast2 = true;
					}
				}, 4000);

			}
		}
	}

	// ͨ�������ť����������
	class DownloadMp3Listener implements OnClickListener {
		public void onClick(View v) {
			// ��֤12����ֻ����һ�ι㲥��ͬһ����
			if (broadcast2) {
				// ��֤ȡ�����صĴ��������꣬���������صĴ��룬���Ƿֿ������㲥
				if (broadcast1) {
					broadcast2 = false;
					if ("δ��ʼ".equals(flag2)) {
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

	// �������翪��
	class DataOpenCloseListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			// ��ȡ����״̬����ʾ
			// setNetWorkStatus();
			// Log.v("zzzzb", "���ڿ��ذ�ť����¼�");
			// toggleMobileData(Download.this, false);
			if (getNetWorkStatus(Download.this)) {
				toggleMobileData(Download.this, false);
				tv_data_state2.setText("��");
				internetCloseToLogs();
			} else {
				toggleMobileData(Download.this, true);
				tv_data_state2.setText("��");
				internetOpenToLogs();
			}
		}
	}

	// ����򿪴�ӡLogs
	public void internetOpenToLogs() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy��MM��dd�� HH:mm:ss ");
		Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
		String str = formatter.format(curDate);
		try {
			// ���ﴴ��·��Ϊ��sd����·��/test.txt��������һ��true������������test.txtһֱ���ڵĻ����Ժ�����ݾ�ֱ��׷�ӵ�test.txt
			// ���ݵĺ��棬���Ḳ�ǣ����û��true���ʹ�����
			fw = new FileWriter(Environment.getExternalStorageDirectory() + "/" + "test.txt", true);
			// newһ��д�Ķ���
			pw = new PrintWriter(fw);
			// if (b == e && b != 0 && e != 0) {
			// pw.println(str + " V/BSK" + " " + b + " 000000" +
			// "\r\n");
			// } else {
			pw.println(str + " V/BSK" + " " + "�����Ѵ�" + "\r\n");
			System.out.println(str + " V/BSK" + " " + b);
			// }
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ����رմ�ӡLogs
	public void internetCloseToLogs() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy��MM��dd�� HH:mm:ss ");
		Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
		String str = formatter.format(curDate);
		try {
			// ���ﴴ��·��Ϊ��sd����·��/test.txt��������һ��true������������test.txtһֱ���ڵĻ����Ժ�����ݾ�ֱ��׷�ӵ�test.txt
			// ���ݵĺ��棬���Ḳ�ǣ����û��true���ʹ�����
			fw = new FileWriter(Environment.getExternalStorageDirectory() + "/" + "test.txt", true);
			// newһ��д�Ķ���
			pw = new PrintWriter(fw);
			// if (b == e && b != 0 && e != 0) {
			// pw.println(str + " V/BSK" + " " + b + " 000000" +
			// "\r\n");
			// } else {
			pw.println(str + " V/BSK" + " " + "�����ѹر�" + "\r\n");
			System.out.println(str + " V/BSK" + " " + b);
			// }
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// �򿪻�ر���������
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

	// ���صĹؼ�����
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

	

	// ��http���ӣ���ȡgetInputStream����
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
			// ��������������ʱ����λ�����룩
			urlConn.setConnectTimeout(30000);
			// ���ô�������ȡ���ݳ�ʱ����λ�����룩
			urlConn.setReadTimeout(20000);
			// urlConn.setAllowUserInteraction(true);
			// urlConn.setRequestMethod("GET");
			// urlConn.setRequestProperty("Accept-Language", "zh-CN");
			// urlConn.setRequestProperty("Referer", url.toString());
			// urlConn.setRequestProperty("Accept-Encoding", "identity");
			// urlConn.setRequestProperty("Charset", "UTF-8");
			// ���÷���
			urlConn.setRequestMethod("GET");
			// ���ò���
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
			 * "��ʱ1��", Toast.LENGTH_SHORT).show(); }
			 */
			return inputStream;

		} catch (MalformedURLException e) {
			// TODO: handle exception
			Toast.makeText(Download.this, "���Ӵ���", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			// TODO: handle exception
			if (e instanceof ConnectTimeoutException) {
				Toast.makeText(Download.this, "���ӳ�ʱ", Toast.LENGTH_SHORT).show();
			} else if (e instanceof SocketTimeoutException) {
				Toast.makeText(Download.this, "��Ӧ��ʱ", Toast.LENGTH_SHORT).show();
			}
		}
		return null;
	}

	/*
	 * class MyThread implements Runnable { public void run() { // ���������߼�
	 * //Toast.makeText(Download.this, "��ʼ����", Toast.LENGTH_SHORT).show(); int
	 * result = downFile(
	 * "http://res.media.golo5.com/topic/music/201501/20150122/c0472e02074cee47d72a25dc4f8f60b2_96.mp3"
	 * , "voa/", "a1.mp3"); int result = downFile(
	 * "http://res.media.golo5.com/topic/music/201507/20150720/b5bdaa986693bc0821db7bd4574b922a_128.mp3"
	 * , "voa/", "a1.mp3"); System.out.println(result); } }
	 */

	// �������ص��߳�
	class MyThread1 implements Runnable {
		public void run() {
			while (flag1) {
				flag2 = "��������";

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

	// ��ӡ����ʾ���ش���
	public void downCount() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy��MM��dd�� HH:mm:ss ");
		Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
		String str = formatter.format(curDate);
		downCount++;
		try {
			fw = new FileWriter(Environment.getExternalStorageDirectory() + "/" + "test.txt", true);
			pw = new PrintWriter(fw);
			pw.println(str + " V/BSK" + " " + "���ش�����" + downCount + "\r\n");
			System.out.println(str + " V/BSK" + " " + "���ش�����" + downCount);
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Message message = new Message();
		message.what = 3;
		handler.sendMessage(message);
	}

	// ����һ��SD��·��
	public String getSDPATH() {
		return SDPATH;
	}

	/**
	 * ��SD���ϴ����ļ�
	 * 
	 * @throws IOException
	 */
	// ��SD���ϴ�����Ŀ¼���ٴ����ļ�
	public File creatSDFile(String fileName) throws IOException {
		File file = new File(SDPATH + fileName);
		file.createNewFile();
		return file;
	}

	/**
	 * ��SD���ϴ���Ŀ¼
	 * 
	 * @param dirName
	 */
	// ��SD�����ȴ���Ŀ¼
	public File creatSDDir(String dirName) {
		File dir = new File(SDPATH + dirName);
		dir.mkdir();
		return dir;
	}

	/**
	 * �ж�SD���ϵ��ļ��Ƿ����
	 */
	// �ж�SD�����ļ��Ƿ����
	public boolean isFileExist(String fileName) {
		File file = new File(SDPATH + fileName);
		return file.exists();
	}

	// ÿ���ӡ��ȡ��������
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
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy��MM��dd�� HH:mm:ss ");
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
						// ��һ����aa
						if (count2 % 2 == 0) {
							aa = b;
						} else {
							// ��һ����bb
							bb = b;
						}
						// ÿ��Ա�bb��aa �����ݣ������ͬ��count3��++�������һ�β���ͬ��count3������
						if (bb == aa) {
							count3++;
						} else {
							count3 = 0;
						}
						// ���count3����8,�͵���˵8���ڵ����ݶ�һ����������ֹͣ���أ�����ӡlogs
						if (count3 == 7) {
							getInternetTypeToLogs();
							Message message = new Message();
							message.what = 0;
							handler.sendMessage(message); // ��Message�����ͳ�ȥ
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

	// ÿ���ӡ��ȡ��������
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
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy��MM��dd�� HH:mm:ss ");
						Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
						String str = formatter.format(curDate);
						try {
							// ���ﴴ��·��Ϊ��sd����·��/test.txt��������һ��true������������test.txtһֱ���ڵĻ����Ժ�����ݾ�ֱ��׷�ӵ�test.txt
							// ���ݵĺ��棬���Ḳ�ǣ����û��true���ʹ�����
							fw = new FileWriter(Environment.getExternalStorageDirectory() + "/" + "test.txt", true);
							// newһ��д�Ķ���
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

	// ���������������û�õ�timerTaskDatabase5�ˣ���Ϊ8�����ݵĴ���Աȶ�������ִ����
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
						handler.sendMessage(message); // ��Message�����ͳ�ȥ
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

	// ��ʱ��1��ÿ8��ִ��һ�Σ������صĶ������ݴ浽����c��c������ݾ���0�룬8�룬16�룬24�룬32��.....��

	// ��ʱ��2����ʼ�ӳ�8��ִ�У�����ÿ8��ִ��һ�Σ�����Ĳ�����
	// 1.�����صĶ������ݴ浽����d��d������ݾ���8�룬16�룬24�룬32�룩
	// 2.�Ա�c��d�������Ƿ���ͬ������ͬ��ֹͣ����

	// 8��û���ݺ���ø÷���
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

	// û�õ�
	// �Ƚϡ����ڡ��롰ǰ8������ݡ������磺������16�룬��ô�Ƚϵ���֮ǰ8��ʱ��������16�루���ڣ�������
	public void timerTaskDatabase5() {
		try {
			if (timer5 == null) {
				timer5 = new Timer();
			}
			timer5.schedule(new TimerTask() {

				@Override
				public void run() {
					// d:8�������,c:0�������
					// d:16������ݣ�c:8�������
					if (changePross) {
						d = b;
						if (c == d) {
							// System.exit(0);
							// asdasd();
							getInternetTypeToLogs();

							Message message = new Message();
							message.what = 0;
							handler.sendMessage(message); // ��Message�����ͳ�ȥ

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

	// ��Ϊ�漰����ӡ������ÿ������ǰ�����ôη��������b,c,d��ֹͣ��ʱ��
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

	// ��Ϊ�漰����ӡ������ÿ������ǰ�����ô˷��������b,c,d��ֹͣ��ʱ��
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
	 * ��һ��InputStream���������д�뵽SD����
	 */
	public File write2SDFromInput(String path, String fileName, InputStream input) {
		CannelTime();
		// System.out.println(timer3);
		// System.out.println(timer3);
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy��MM��dd�� HH:mm:ss ");
			Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
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
			// pw.println("��ʼ����,������Ϊ:" + urlCode);
			pw.println("��ʼ����:");
			pw.println("\r\n");
			System.out.println("---------------------------------------------------------");
			// System.out.println("��ʼ����,������Ϊ:" + urlCode);
			System.out.println("��ʼ����:");
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
			// ����������Ϊ4*1024��������Ӧ�þ���ÿ�δ�getInputStream����������ʱ��ŵĵط���Ȼ���ٴӻ�����д�뵽SD��
			byte buffer[] = new byte[1024];
			while ((a = input.read(buffer)) != -1) {
				// a��ÿ�ζ�ȡ�����������ݣ�ÿ�ζ���b����ôb���������˶�������
				// ������������ͻȻ����������������û��������ȡ�������ݣ����ǻ�ֱ������catch���
				b = a + b;
				progress = (int) (((float) b / length) * 100);
				if (changePross) {
					handler.sendEmptyMessage(1);
				}

				output.write(buffer, 0, a);
			}
			// д���ر���
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