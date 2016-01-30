package com.example.gpio1;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.x806.IPerson;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	private IPerson iPerson;
	// 串口1
	private Button btn_connection_port1;
	private Button btn_sendData1;
	private Button btn_receive_data1;
	// 串口2
	private Button btn_connection_port2;
	private Button btn_sendData2;
	private Button btn_receive_data2;
	// 打开蓝牙
	private Button btn_open_bluetooth;
	// 蓝牙状态
	private TextView tv_bluetooth_state;
	// 关闭蓝牙
	private Button btn_close_bluetooth;

	// 拉高与拉低Pl08 gpio
	private Button btn_OpenPl08;
	private Button btn_ClosePl08;

	// 获取蓝牙名称
	private Button btn_get_bluetooth_name;
	// 设置蓝牙名称
	private Button btn_set_bluetooth_name;
	// 显示设置蓝牙名称后的结果，ok
	private TextView tv_show_bluetooth_result;
	// 读取RTC时间
	private Button btn_get_time;
	// 设置RTC时间
	private Button btn_set_time;
	// 设置RTC时间结果显示
	private TextView tv_rtc_show_result;
	// 蓝牙名称输入框
	private EditText tv_set_bluetooth_name;

	// 模仿摘机，挂机，响铃按钮
	private Button btn_a;
	private Button btn_b;
	private Button btn_c;

	private Button btn_Down;// 模仿按下,广告机上面那个按钮
	private Button btn_Up;// 模仿抬起,广告机上面那个按钮
	PrintWriter pw = null;
	FileWriter fw = null;

	private EditText et_year;// 年
	private EditText et_mouth;// 月
	private EditText et_day;// 日
	private EditText et_hour;// 时
	private EditText et_minute;// 分
	private EditText et_second;// 秒

	public native String setDirection(String port, int num, String inout);

	public native int setValue(String port, int num, int value);

	private static final String TAG = "X806Cliend";
	// 保存当前声音
	public static int current;

	Runnable runnable = null;
	// 定时器秒数
	public int count = 0;

	long downTime = 0;// 接收按下广播时间
	long upTime = 0;// 接收抬起广播时间
	// 如果设置蓝牙失败，那么再try3次
	int countBluetoothNum = 0;
	// 如果设置RTC失败，那么再try3次
	int countRtcNum = 0;

	static {
		System.loadLibrary("gpio_lib");
	}

	// 实例化ServiceConnection
	private ServiceConnection conn = new ServiceConnection() {
		@Override
		synchronized public void onServiceConnected(ComponentName name, IBinder service) {
			// 获得IPerson接口
			iPerson = IPerson.Stub.asInterface(service);
			Log.v(TAG, "onServiceConnected");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			iPerson = null;
		}
	};

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				// 启动线程，，循环，，每隔1秒时候发送一个数据出去
				count = 0;
				handler.removeCallbacks(runnable);
				runnable = new Runnable() {
					@Override
					public void run() {
						handler.postDelayed(this, 1 * 1000);
						count++;
						Log.v(TAG, "count:" + count);
						// 8s后清零,底层送上来的广播就已经需要4s
						if (count == 8) {
							handler.sendEmptyMessage(2);
						}
					}
				};
				handler.postDelayed(runnable, 1 * 1000);
				break;
			case 2:
				// 执行这里的话，说明接了电话或铃声超时
				handler.removeCallbacks(runnable);
				count = 0;
				Intent intent = new Intent();
				intent.setAction("BSK_BROADCAST_HAND_OFF");
				sendBroadcast(intent);
				break;
			case 3:
				// 关闭蓝牙
				bsk_disanble_bt();
				Toast.makeText(MainActivity.this, "蓝牙已关闭", Toast.LENGTH_SHORT).show();
				break;
			case 4:
				// 再设置蓝牙，3次
				countBluetoothNum++;
				Log.v(TAG, "countBluetoothNum:" + countBluetoothNum);
				if (countBluetoothNum <= 3) {
					// 连接串口并设置蓝牙名称
					setBluetoothName();
					// 接收设置蓝牙名称时返回的数据
					getBluetoothData();
				} else {
					countBluetoothNum = 0;
					tv_show_bluetooth_result.setText("设置蓝牙名称失败!");
					// handler.sendEmptyMessageDelayed(5, 5 * 1000);
				}
				break;
			case 5:
				// 5s后清除tv_show_bluetooth_result
				tv_show_bluetooth_result.setText("");
				break;
			case 6:
				// 再设置RTC，3次
				countRtcNum++;
				Log.v(TAG, "countNum:" + countRtcNum);
				if (countRtcNum <= 3) {
					// 连接串口并设置Rtc时间
					setRtc();
					// 获取Rtc数据
					getRtcData();
				} else {
					countRtcNum = 0;
					tv_rtc_show_result.setText("设置RTC时间失败！");
					// handler.sendEmptyMessageDelayed(5, 5 * 1000);
				}
				break;
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 串口1
		btn_connection_port1 = (Button) findViewById(R.id.btn_connection_port1);
		btn_sendData1 = (Button) findViewById(R.id.btn_sendData1);
		btn_receive_data1 = (Button) findViewById(R.id.btn_receive_data1);
		// 串口2
		btn_connection_port2 = (Button) findViewById(R.id.btn_connection_port2);
		btn_sendData2 = (Button) findViewById(R.id.btn_sendData2);
		btn_receive_data2 = (Button) findViewById(R.id.btn_receive_data2);

		// 打开与关闭蓝牙
		btn_open_bluetooth = (Button) findViewById(R.id.btn_open_bluetooth);
		btn_close_bluetooth = (Button) findViewById(R.id.btn_close_bluetooth);
		// 显示蓝牙状态
		tv_bluetooth_state = (TextView) findViewById(R.id.tv_bluetooth_state);
		// 获取与设置蓝牙名称
		btn_get_bluetooth_name = (Button) findViewById(R.id.btn_get_bluetooth_name);
		btn_set_bluetooth_name = (Button) findViewById(R.id.btn_set_bluetooth_name);
		tv_show_bluetooth_result = (TextView) findViewById(R.id.tv_show_bluetooth_result);

		// 获取与设置rtc时间
		btn_set_time = (Button) findViewById(R.id.btn_set_time);
		btn_get_time = (Button) findViewById(R.id.btn_get_time);
		tv_rtc_show_result = (TextView) findViewById(R.id.tv_rtc_show_result);
		btn_Down = (Button) findViewById(R.id.btn_Down);// 模仿按下
		btn_Up = (Button) findViewById(R.id.btn_Up);// 模仿抬起

		btn_a = (Button) findViewById(R.id.btn_a);
		btn_b = (Button) findViewById(R.id.btn_b);
		btn_c = (Button) findViewById(R.id.btn_c);
		// 蓝牙名称输入框
		tv_set_bluetooth_name = (EditText) findViewById(R.id.tv_set_bluetooth_name);

		// 拉高与拉低Pl08 gpio
		btn_OpenPl08 = (Button) findViewById(R.id.btn_OpenPl08);
		btn_ClosePl08 = (Button) findViewById(R.id.btn_ClosePl08);

		et_year = (EditText) findViewById(R.id.et_year);// 年
		et_mouth = (EditText) findViewById(R.id.et_mouth);// 月
		et_day = (EditText) findViewById(R.id.et_day);// 日
		et_hour = (EditText) findViewById(R.id.et_hour);// 时
		et_minute = (EditText) findViewById(R.id.et_minute);// 分
		et_second = (EditText) findViewById(R.id.et_second);// 秒

		btn_connection_port1.setOnClickListener(this);
		btn_sendData1.setOnClickListener(this);
		btn_receive_data1.setOnClickListener(this);
		btn_connection_port2.setOnClickListener(this);
		btn_sendData2.setOnClickListener(this);
		btn_receive_data2.setOnClickListener(this);

		btn_open_bluetooth.setOnClickListener(this);
		btn_close_bluetooth.setOnClickListener(this);
		btn_get_bluetooth_name.setOnClickListener(this);
		btn_set_bluetooth_name.setOnClickListener(this);
		btn_set_time.setOnClickListener(this);
		btn_get_time.setOnClickListener(this);

		btn_a.setOnClickListener(this);
		btn_b.setOnClickListener(this);
		btn_c.setOnClickListener(this);

		btn_Down.setOnClickListener(this);
		btn_Up.setOnClickListener(this);
		btn_OpenPl08.setOnClickListener(this);
		btn_ClosePl08.setOnClickListener(this);

		// 实例化Intent
		Intent intent = new Intent("com.bsk.x806.action.MY_REMOTE_SERVICE");
		// 设置Intent Action 属性
		bindService(intent, conn, Service.BIND_AUTO_CREATE);
		// 打开蓝牙
		// bsk_enble_bt();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// 挂电话，恢复声音广播注册
		OpenSoundBroadcastReg();
		// 拿起电话广播，禁音广播注册
		closeSoundBroadcastReg();
		// 响铃广播，响铃广播注册
		phoneVoiceBroadcastReg();
		// 响铃停止广播注册
		phoneVoiceBroadcastReg1();
		// 快捷键按下广播
		BtnDownBroadcastReg();
		// 快捷键抬起广播
		BtnUpBroadcastReg();

		// 按钮酒店介绍
		Btn1UpBroadcastReg();
		Btn1DownBroadcastReg();

		// 按钮吃喝玩乐
		Btn2UpBroadcastReg();
		Btn2DownBroadcastReg();

		// 按钮使用说明
		Btn3UpBroadcastReg();
		Btn3DownBroadcastReg();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unbindService(conn);
		unregisterReceiver(openSoundBroadcast);
		unregisterReceiver(closeSoundBroadcast);
		unregisterReceiver(phoneVoiceBroadcast);

		unregisterReceiver(phoneVoiceBroadcast1);
		unregisterReceiver(BtnDownBroadcast);
		unregisterReceiver(BtnUpBroadcast);

		unregisterReceiver(Btn1UpBroadcast);
		unregisterReceiver(Btn1DownBroadcast);

		unregisterReceiver(Btn2UpBroadcast);
		unregisterReceiver(Btn2DownBroadcast);

		unregisterReceiver(Btn3UpBroadcast);
		unregisterReceiver(Btn3DownBroadcast);
		super.onDestroy();
	}

	// 拿起电话广播，禁音广播注册
	private void closeSoundBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("BSK_BROADCAST_HAND_ON");// 就是自己想要接收的系统广播
		this.registerReceiver(closeSoundBroadcast, filter);
	}

	// int max =mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
	// 拿起电话广播，接收禁音广播;BSK_BROADCAST_HAND_ON
	private BroadcastReceiver closeSoundBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("BSK_BROADCAST_HAND_ON")) {
				Log.v(TAG, "BSK_BROADCAST_HAND_ON");
				Toast.makeText(MainActivity.this, "BSK_BROADCAST_HAND_ON", Toast.LENGTH_SHORT).show();
				AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				// 每次拿起电话都把定时器停止，并清零
				handler.removeCallbacks(runnable);
				count = 0;
				// 当前不为静音时，才把当前音量存起来，并设置静音
				if ((mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM)) != 0) {
					current = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
					mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
				}
				int testcurrent = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
				Log.v(TAG, "testcurrent:" + testcurrent);
			}
		}
	};

	// 挂电话，恢复声音广播注册
	private void OpenSoundBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("BSK_BROADCAST_HAND_OFF");// 就是自己想要接收的系统广播
		this.registerReceiver(openSoundBroadcast, filter);
	}

	// 挂电话，接收恢复声音广播
	private BroadcastReceiver openSoundBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("BSK_BROADCAST_HAND_OFF")) {
				Log.v(TAG, "BSK_BROADCAST_HAND_OFF");
				Toast.makeText(MainActivity.this, "BSK_BROADCAST_HAND_OFF", Toast.LENGTH_SHORT).show();
				AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, current, 0);
				Log.v(TAG, "current:" + current);
			}
		}
	};

	// 响铃广播，响铃广播注册
	private void phoneVoiceBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("BSK_BROADCAST_RING_ON");// 就是自己想要接收的系统广播
		this.registerReceiver(phoneVoiceBroadcast, filter);
	}

	// 响铃广播，接收响铃广播（每响一次都有一个广播，响铃时设置声音禁音，5s内再没有广播过来的话，说明停止了或者接了电话，就设置恢复声音）
	private BroadcastReceiver phoneVoiceBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("BSK_BROADCAST_RING_ON")) {
				Log.v(TAG, "BSK_BROADCAST_RING_ON");
				// SimpleDateFormat formatter = new
				// SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
				// Date curDate = new Date(System.currentTimeMillis());
				// String str = formatter.format(curDate);
				// try {
				// fw = new FileWriter(Environment.getExternalStorageDirectory()
				// + "/" + "test.txt", true);
				// pw = new PrintWriter(fw);
				// pw.println(str + "CNLAUNCHER_KEY_F11_UP" + " " + "\r\n");
				// pw.close();
				// } catch (Exception e) {
				// e.printStackTrace();
				// }
				Toast.makeText(MainActivity.this, "BSK_BROADCAST_RING_ON", Toast.LENGTH_SHORT).show();
				handler.sendEmptyMessage(1);
				AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				if ((mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM)) != 0) {
					current = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
					mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
				}
			}
		}
	};

	// 响铃停止广播，没用到
	private void phoneVoiceBroadcastReg1() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("BSK_BROADCAST_RING_OFF");// 就是自己想要接收的系统广播
		this.registerReceiver(phoneVoiceBroadcast1, filter);
	}

	// 响铃停止广播，没用到
	private BroadcastReceiver phoneVoiceBroadcast1 = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("BSK_BROADCAST_RING_OFF")) {
				Log.v(TAG, "BSK_BROADCAST_RING_OFF");
				// SimpleDateFormat formatter = new
				// SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
				// Date curDate = new Date(System.currentTimeMillis());
				// String str = formatter.format(curDate);
				// try {
				// fw = new FileWriter(Environment.getExternalStorageDirectory()
				// + "/" + "test.txt", true);
				// pw = new PrintWriter(fw);
				// pw.println(str + "CNLAUNCHER_KEY_F11_DOWN" + " " + "\r\n");
				// pw.close();
				// } catch (Exception e) {
				// e.printStackTrace();
				// }
				Toast.makeText(MainActivity.this, "BSK_BROADCAST_RING_OFF", Toast.LENGTH_SHORT).show();
			}
		}
	};

	// 快捷键按下广播
	private void BtnDownBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("BSK_BROADCAST_HOTKEY_DOWN");// 就是自己想要接收的系统广播
		this.registerReceiver(BtnDownBroadcast, filter);
	}

	// 快捷键按下广播
	private BroadcastReceiver BtnDownBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// 接收按下广播时间
			downTime = System.currentTimeMillis();
			if (action.equals("BSK_BROADCAST_HOTKEY_DOWN")) {
				Log.v(TAG, "BSK_BROADCAST_HOTKEY_DOWN");
				Toast.makeText(MainActivity.this, "BSK_BROADCAST_HOTKEY_DOWN", Toast.LENGTH_SHORT).show();
			}
		}
	};

	// 快捷键抬起广播
	private void BtnUpBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("BSK_BROADCAST_HOTKEY_UP");// 就是自己想要接收的系统广播
		this.registerReceiver(BtnUpBroadcast, filter);
	}

	// 快捷键抬起广播
	private BroadcastReceiver BtnUpBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// 接收抬起广播时间
			upTime = System.currentTimeMillis();
			if (action.equals("BSK_BROADCAST_HOTKEY_UP")) {
				long timeOut = upTime - downTime;
				Log.v(TAG, "timeOut:" + timeOut);
				if (timeOut < 1000) {
					bsk_enble_bt();// 打开蓝牙
					// 10分钟后关闭蓝牙
					handler.sendEmptyMessageDelayed(3, 600 * 1000);
					Toast.makeText(MainActivity.this, "正在打开蓝牙....", Toast.LENGTH_SHORT).show();
				} else if (timeOut > 1000) {
					Toast.makeText(MainActivity.this, "长按:BSK_BROADCAST_HOTKEY_UP", Toast.LENGTH_SHORT).show();
				}
				// 清零
				downTime = 0;
				upTime = 0;

			}
		}
	};

	// 按钮酒店介绍Up广播
	private void Btn1UpBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("BSK_BROADCAST_INTRODUCTION_UP");// 就是自己想要接收的系统广播
		this.registerReceiver(Btn1UpBroadcast, filter);
	}

	// 按钮酒店介绍Up广播
	private BroadcastReceiver Btn1UpBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("BSK_BROADCAST_INTRODUCTION_UP")) {
				Log.v(TAG, "BSK_BROADCAST_INTRODUCTION_UP");
				Toast.makeText(MainActivity.this, "BSK_BROADCAST_INTRODUCTION_UP", Toast.LENGTH_SHORT).show();
			}
		}
	};

	// 按钮酒店介绍Down广播
	private void Btn1DownBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("BSK_BROADCAST_INTRODUCTION_DOWN");// 就是自己想要接收的系统广播
		this.registerReceiver(Btn1DownBroadcast, filter);
	}

	// 按钮酒店介绍Down广播
	private BroadcastReceiver Btn1DownBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("BSK_BROADCAST_INTRODUCTION_DOWN")) {
				Log.v(TAG, "BSK_BROADCAST_INTRODUCTION_DOWN");
				Toast.makeText(MainActivity.this, "BSK_BROADCAST_INTRODUCTION_DOWN", Toast.LENGTH_SHORT).show();
			}
		}
	};

	// 按钮吃喝玩乐Up广播
	private void Btn2UpBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("BSK_BROADCAST_HAVEFUN_UP");// 就是自己想要接收的系统广播
		this.registerReceiver(Btn2UpBroadcast, filter);
	}

	// 按钮吃喝玩乐Up广播
	private BroadcastReceiver Btn2UpBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("BSK_BROADCAST_HAVEFUN_UP")) {
				Log.v(TAG, "BSK_BROADCAST_HAVEFUN_UP");
				Toast.makeText(MainActivity.this, "BSK_BROADCAST_HAVEFUN_UP", Toast.LENGTH_SHORT).show();
			}
		}
	};

	// 按钮吃喝玩乐Down广播
	private void Btn2DownBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("BSK_BROADCAST_HAVEFUN_DOWN");// 就是自己想要接收的系统广播
		this.registerReceiver(Btn2DownBroadcast, filter);
	}

	// 按钮吃喝玩乐Down广播
	private BroadcastReceiver Btn2DownBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("BSK_BROADCAST_HAVEFUN_DOWN")) {
				Log.v(TAG, "BSK_BROADCAST_HAVEFUN_DOWN");
				Toast.makeText(MainActivity.this, "BSK_BROADCAST_HAVEFUN_DOWN", Toast.LENGTH_SHORT).show();
			}
		}
	};

	// 按钮使用说明Up广播
	private void Btn3UpBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("BSK_BROADCAST_GUIDE_UP");// 就是自己想要接收的系统广播
		this.registerReceiver(Btn3UpBroadcast, filter);
	}

	// 按钮使用说明Up广播
	private BroadcastReceiver Btn3UpBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("BSK_BROADCAST_GUIDE_UP")) {
				Log.v(TAG, "BSK_BROADCAST_GUIDE_UP");
				Toast.makeText(MainActivity.this, "BSK_BROADCAST_GUIDE_UP", Toast.LENGTH_SHORT).show();
			}
		}
	};

	// 按钮使用说明Down广播
	private void Btn3DownBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("BSK_BROADCAST_GUIDE_DOWN");// 就是自己想要接收的系统广播
		this.registerReceiver(Btn3DownBroadcast, filter);
	}

	// 按钮使用说明Down广播
	private BroadcastReceiver Btn3DownBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("BSK_BROADCAST_GUIDE_DOWN")) {
				Log.v(TAG, "BSK_BROADCAST_GUIDE_DOWN");
				Toast.makeText(MainActivity.this, "BSK_BROADCAST_GUIDE_DOWN", Toast.LENGTH_SHORT).show();
			}
		}
	};

	/**
	 * 按钮点击事件
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// 连接串口1
		if (R.id.btn_connection_port1 == v.getId()) {
			Log.v(TAG, "btn_connection_port1");
			Test11.getInstance(iPerson).recvData(new Test11.IX806masterRecv() {
				@Override
				public void received(String data) {
					Log.v(TAG, "data" + data);
				}

				@Override
				public void connected() {
					Log.v(TAG, "下位机串口1连接成功！");
					// 连接串口成功后开始发数据
				}

				@Override
				public void disconnected(String error) {
					Log.v(TAG, error);
				}
			});
		}
		// 串口1发送数据
		if (R.id.btn_sendData1 == v.getId()) {
			Log.v(TAG, "串口1发送数据");
			// sendData("AT-CD-2");
			// Test11.getInstance(iPerson).recvValue();
		}
		// 串口1接收数据
		if (R.id.btn_receive_data1 == v.getId()) {
			// Test11.getInstance(iPerson).recvValue();
		}
		// 连接串口2
		if (R.id.btn_connection_port2 == v.getId()) {
			Log.v(TAG, "btn_connection_port2");
			Test11.getInstance(iPerson).recvData2(new Test11.IX806masterRecv() {

				@Override
				public void received(String data) {
					Log.v(TAG, "data" + data);
				}

				@Override
				public void connected() {
					Log.v(TAG, "下位机串口2连接成功！");
					// 连接串口成功后开始发数据
				}

				@Override
				public void disconnected(String error) {
					Log.v(TAG, error);
				}
			});
		}
		// 串口2发送数据
		if (R.id.btn_sendData2 == v.getId()) {
			Log.v(TAG, "串口2发送数据");
			// sendData("AT-CD-2");
		}
		// 串口2接收数据
		if (R.id.btn_receive_data2 == v.getId()) {
			// Test11.getInstance(iPerson).recvValue2();
		}
		// 打开蓝牙
		if (R.id.btn_open_bluetooth == v.getId()) {
			bsk_enble_bt();
		}
		// 关闭蓝牙
		if (R.id.btn_close_bluetooth == v.getId()) {
			bsk_disanble_bt();
		}

		// 读取蓝牙名称
		if (R.id.btn_get_bluetooth_name == v.getId()) {
			bsk_read_bt_name();
		}
		// 设置蓝牙名称
		if (R.id.btn_set_bluetooth_name == v.getId()) {
			// 设置蓝牙名称
			setBluetoothName();
			// 获取数据
			getBluetoothData();
		}

		// 读取RTC时间
		if (R.id.btn_get_time == v.getId()) {
			bsk_get_rtc();
		}

		// 设置RTC时间
		if (R.id.btn_set_time == v.getId()) {
			// 连接串口并设置Rtc时间
			setRtc();
			// 获取Rtc数据
			getRtcData();
		}
		//// 拉高Pl08 gpio
		if (R.id.btn_OpenPl08 == v.getId()) {
			bsk_open_pl08();
		}
		// 拉低Pl08 gpio
		if (R.id.btn_ClosePl08 == v.getId()) {
			bsk_close_pl08();
		}
		if (R.id.btn_Down == v.getId()) {// 模仿按下
			Intent intent = new Intent();
			intent.setAction("anxia");
			sendBroadcast(intent);
		}

		if (R.id.btn_Up == v.getId()) {// 模仿抬起
			Intent intent = new Intent();
			intent.setAction("taiqi");
			sendBroadcast(intent);
			// test();
			// sleepp(500);
			// Test11.getInstance(iPerson).recvValue2(new Test11.X806GetData() {
			//
			// @Override
			// public void getData(char[] data) {
			// // TODO Auto-generated method stub
			// char[] dataBluetooth = data;
			// }
			//
			// });
		}
		if (R.id.btn_a == v.getId()) {
			Intent intent = new Intent();
			intent.setAction("CNLAUNCHER_KEY_F10_UP");
			sendBroadcast(intent);
		}

		if (R.id.btn_b == v.getId()) {
			Intent intent = new Intent();
			intent.setAction("CNLAUNCHER_KEY_F10_DOWN");
			sendBroadcast(intent);
		}

		if (R.id.btn_c == v.getId()) {
			Intent intent = new Intent();
			intent.setAction("CNLAUNCHER_KEY_F11_UP");
			sendBroadcast(intent);
		}
	}

	/**
	 * 连接串口并设置蓝牙名称
	 */
	public void setBluetoothName() {
		String name = tv_set_bluetooth_name.getText().toString().trim();
		if (name.equals("")) {
			Toast.makeText(MainActivity.this, "请输入蓝牙名称", Toast.LENGTH_SHORT).show();
			return;
		}
		Test11.getInstance(iPerson).recvData2(new Test11.IX806masterRecv() {
			@Override
			public void received(String data) {
				Log.v(TAG, "data" + data);
			}

			@Override
			public void connected() {
				Log.v(TAG, "下位机串口2连接成功！");
				// 连接串口成功后开始发数据
			}

			@Override
			public void disconnected(String error) {
				Log.v(TAG, error);
			}
		});
		sleepp(500);

		// 拉低
		setDirection("PL", 9, "out");
		setValue("PL", 9, 0);

		sleepp(1 * 1000);
		bsk_write_bt_name(name);
		sleepp(1 * 1000);

		// 拉高
		int open = setValue("PL", 9, 1);
		// 显示蓝牙状态
		showBluetoothState(open);
		sleepp(500);
	}

	/**
	 * 接收设置蓝牙名称时返回的数据
	 */
	public void getBluetoothData() {
		// 接收数据
		Test11.getInstance(iPerson).recvValue2(new Test11.X806GetData() {
			@Override
			public void getData(char[] data) {
				// TODO Auto-generated method stub
				char[] dataBluetooth = data;
				if (!dataBluetooth.equals("") && dataBluetooth != null) {
					int countData = 0;
					for (char d : dataBluetooth) {
						if (d == 'O') {
							countData++;
						}
						if (d == 'K') {
							countData++;
						}
					}
					if (countData == 2) {
						Log.v(TAG, "设置蓝牙名称成功！");
						tv_show_bluetooth_result.setText("设置蓝牙名称成功！");
						// handler.sendEmptyMessageDelayed(5, 5 * 1000);
					} else {
						tv_show_bluetooth_result.setText("");
						countData = 0;
						handler.sendEmptyMessage(4);
					}
				} else {
					// Toast.makeText(MainActivity.this, "无数据返回，设置蓝牙名称失败!",
					// Toast.LENGTH_SHORT).show();
					tv_show_bluetooth_result.setText("");
					handler.sendEmptyMessage(4);
					// handler.sendEmptyMessageDelayed(5, 5 * 1000);
				}
			}
		});
	}

	/**
	 * 连接串口并设置RTC时间
	 */
	public void setRtc() {
		Test11.getInstance(iPerson).recvData(new Test11.IX806masterRecv() {
			@Override
			public void received(String data) {
				Log.v(TAG, "data" + data);
			}

			@Override
			public void connected() {
				Log.v(TAG, "下位机串口1连接成功！");
				// 连接串口成功后开始发数据
			}

			@Override
			public void disconnected(String error) {
				Log.v(TAG, error);
			}
		});
		sleepp(500);
		// bsk_set_rtc(2115, "10", "12", "2", "30", "20");
		try {
			et_day = (EditText) findViewById(R.id.et_day);// 日
			et_hour = (EditText) findViewById(R.id.et_hour);// 时
			et_minute = (EditText) findViewById(R.id.et_minute);// 分
			et_second = (EditText) findViewById(R.id.et_second);// 秒

			int year = Integer.parseInt(et_year.getText().toString());
			String mouth = et_mouth.getText().toString();
			String day = et_day.getText().toString();
			String hour = et_hour.getText().toString();
			String minute = et_minute.getText().toString();
			String second = et_second.getText().toString();

			bsk_set_rtc(year, mouth, day, hour, minute, second);
		} catch (Exception e) {
			Toast.makeText(MainActivity.this, "请输入数据  或  输入的数据格式错误!", Toast.LENGTH_SHORT).show();
			tv_rtc_show_result.setText("");
			return;
		}
		sleepp(500);
	}

	/**
	 * 接收设置RTC时间时返回的数据
	 */
	public void getRtcData() {
		Test11.getInstance(iPerson).recvValue(new Test11.X806GetData() {

			@Override
			public void getData(char[] data) {
				// TODO Auto-generated method stub
				char[] dataRtc = data;
				if (!dataRtc.equals("") && dataRtc != null) {
					int countData = 0;
					for (char d : dataRtc) {
						if (d == 'O') {
							countData++;
						}
						if (d == 'K') {
							countData++;
						}
					}
					if (countData == 2) {
						Log.v(TAG, "设置RTC时间成功！");
						tv_rtc_show_result.setText("设置RTC时间成功！");
						// handler.sendEmptyMessageDelayed(5, 5 * 1000);
					} else {
						tv_rtc_show_result.setText("");
						countData = 0;
						handler.sendEmptyMessage(6);
					}
				} else {
					tv_rtc_show_result.setText("");
					handler.sendEmptyMessage(6);
					// Toast.makeText(MainActivity.this, "输入RTC格式错误!",
					// Toast.LENGTH_SHORT).show();
					// handler.sendEmptyMessageDelayed(5, 5 * 1000);
				}
			}

		});
	}

	// 打开蓝牙
	public void bsk_enble_bt() {
		Log.v(TAG, "拉低");
		setDirection("PL", 9, "out");
		setValue("PL", 9, 0);
		sleepp(1 * 1000);
		Log.v(TAG, "拉高");
		int open = setValue("PL", 9, 1);
		Log.v(TAG, "open:" + open);
		// 显示蓝牙状态
		showBluetoothState(open);
	}

	// 关闭蓝牙
	public void bsk_disanble_bt() {
		Log.v(TAG, "拉低");
		setDirection("PL", 9, "out");
		int close = setValue("PL", 9, 0);
		Log.v(TAG, "close:" + close);
		// 显示蓝牙状态
		showBluetoothState(close);
	}

	// 拉高PL08 GPIO
	public void bsk_open_pl08() {
		Log.v(TAG, "拉高PL08");
		setDirection("PL", 8, "out");
		setValue("PL", 8, 1);
	}

	// 拉低PL08 GPIO
	public void bsk_close_pl08() {
		Log.v(TAG, "拉低PL08 GPIO");
		setDirection("PL", 8, "out");
		setValue("PL", 8, 0);
	}

	/**
	 * 显示蓝牙状态
	 * 
	 * @param state
	 */
	public void showBluetoothState(int state) {
		if (state == 1 || state == 0) {
			if (state == 0) {
				tv_bluetooth_state.setText("蓝牙已关闭！");
			} else if (state == 1) {
				tv_bluetooth_state.setText("蓝牙已打开！");
			}
		} else {
			tv_bluetooth_state.setText("打开或关闭蓝牙失败！");
		}
	}

	// 读取蓝牙名称
	public void bsk_read_bt_name() {
		Log.v(TAG, "bsk_read_bt_name：读取蓝牙名称");
	}

	// 设置蓝牙名称
	public void bsk_write_bt_name(String name) {
		Log.v(TAG, "bsk_write_bt_name：设置蓝牙名称");
		char sendToSlave[] = new char[name.length() + 8];
		sendToSlave[0] = 'A';
		sendToSlave[1] = 'T';
		sendToSlave[2] = '-';
		sendToSlave[3] = 'M';
		sendToSlave[4] = 'M';
		sendToSlave[5] = '+';
		char sendToSlave1[] = new char[name.length()];
		sendToSlave1 = name.toCharArray();
		for (int i = 7; i <= sendToSlave.length - 2; i++) {
			sendToSlave[i - 1] = sendToSlave1[i - 7];
		}
		sendToSlave[name.length() + 6] = 0xd;
		sendToSlave[name.length() + 7] = 0xa;
		System.out.println(sendToSlave);
		for (int i = 0; i < sendToSlave.length; i++) {
			try {
				if ((iPerson.sendDataChar(sendToSlave[i], Test11.SerialInt2) == 0)) {
					Log.v(TAG, "发送数据,sendToSlave数组:" + sendToSlave[i] + "   ;Test11.SerialInt2:" + Test11.SerialInt2);
					// Log.d(global.SHOW,SHOW,
					// "############write : find data############");
				} else {
					Log.v(TAG, "############write card: no data###########");
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// for (int j = 1000000; j > 0; j--)
			// ;
		}
	}

	// 读取RTC时间
	public void bsk_get_rtc() {
		Log.v(TAG, "bsk_get_rtc：读取RTC时间");
	}

	// 设置RTC时间
	// 调用：bsk_set_rtc(2015, "10", "26", "17", "22", "0");
	public void bsk_set_rtc(int year, String mouth, String day, String hour, String minute, String second) {
		char sendToSlave[] = new char[12];
		String s = Integer.toHexString(year);
		String year1 = "";
		String year2_1 = "";
		if (s.length() > 2) {
			year1 = s.charAt(0) + "";// 第一个
			year2_1 = s.charAt(1) + "" + s.charAt(2) + "";
			System.out.println("year1:" + year1);
			System.out.println("year2_1:" + year2_1);
		}
		int year2_2 = Integer.valueOf(year2_1, 16);
		String year2 = year2_2 + "";
		sendToSlave[0] = 0xaa;
		sendToSlave[1] = 0x55;
		sendToSlave[2] = 0x88;
		sendToSlave[10] = 0x0d;
		sendToSlave[11] = 0x0a;
		sendToSlave[3] = (char) Integer.parseInt(year1);
		sendToSlave[4] = (char) Integer.parseInt(year2);
		sendToSlave[5] = (char) (Integer.parseInt(mouth));
		sendToSlave[6] = (char) (Integer.parseInt(day));
		sendToSlave[7] = (char) (Integer.parseInt(hour));
		sendToSlave[8] = (char) (Integer.parseInt(minute));
		sendToSlave[9] = (char) (Integer.parseInt(second));
		for (int i = 0; i < sendToSlave.length; i++) {
			try {
				if ((iPerson.sendDataChar(sendToSlave[i], Test11.SerialInt) == 0)) {
					// int aa = Integer.valueOf(sendToSlave[i]);
					// Log.v(TAG, "发送数据,sendToSlave数组:" + aa + "
					// ;Test11.SerialInt:" + Test11.SerialInt);
					Log.v(TAG, "发送数据,sendToSlave数组:" + sendToSlave[i] + ";Test11.SerialInt:" + Test11.SerialInt);
					// Log.v(TAG,"发送数据,sendToSlave数组:" + iTen + "
					// ;Test11.SerialInt:" + Test11.SerialInt);
					// Log.d(global.SHOW,SHOW,
					// "############write : find data############");
				} else {
					Log.v(TAG, "############write card: no data###########");
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int j = 1000000; j > 0; j--)
				;
		}
		for (int i = 0; i < sendToSlave.length; i++) {
			System.out.println(sendToSlave[i] & 0xff);
		}

	}

	public void sleepp(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 没用到
	/**
	 * 封装接口用于回调
	 * 
	 * @author gzdlw
	 */
	public interface IX806masterRecv {
		/**
		 * 当接收到数据时
		 * 
		 * @param recvType
		 *            数据类型
		 * @param data
		 *            数据
		 */
		public void received(String data);

		/**
		 * 串口连接成功
		 */
		public void connected();

		/**
		 * 串口连接失败
		 * 
		 * @param error
		 *            错误信息
		 */
		public void disconnected(String error);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			System.exit(0);
		}
		return true;
	}

	// 设置RTC时间,没用到
	public void bsk_set_rtc() {
		Log.v(TAG, "bsk_set_rtc：设置RTC时间");
		char sendToSlave[] = new char[12];
		sendToSlave[0] = 0xaa;
		sendToSlave[1] = 0x55;
		sendToSlave[2] = 0x88;
		sendToSlave[10] = 0x0d;
		sendToSlave[11] = 0x0a;

		sendToSlave[3] = 0x07;
		sendToSlave[4] = 0xdf;
		sendToSlave[5] = 0x0a;
		sendToSlave[6] = 0x1a;
		sendToSlave[7] = 0x11;
		sendToSlave[8] = 0x16;
		sendToSlave[9] = 0x00;

		for (int i = 0; i < sendToSlave.length; i++) {
			try {
				if ((iPerson.sendDataChar(sendToSlave[i], Test11.SerialInt) == 0)) {
					// int aa = Integer.valueOf(sendToSlave[i]);
					// Log.v(TAG, "发送数据,sendToSlave数组:" + aa + "
					// ;Test11.SerialInt:" + Test11.SerialInt);
					Log.v(TAG, "发送数据,sendToSlave数组:" + sendToSlave[i] + ";Test11.SerialInt:" + Test11.SerialInt);
					// Log.v(TAG,"发送数据,sendToSlave数组:" + iTen + "
					// ;Test11.SerialInt:" + Test11.SerialInt);
					// Log.d(global.SHOW,SHOW,
					// "############write : find data############");
				} else {
					Log.v(TAG, "############write card: no data###########");
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int j = 1000000; j > 0; j--)
				;
		}
	}

	// 发送串口1数据，没用到
	public void sendData1(String name) {
		char sendToSlave[] = new char[name.length()];
		sendToSlave = name.toCharArray();
		for (int i = 0; i < sendToSlave.length; i++) {
			try {
				if ((iPerson.sendDataChar(sendToSlave[i], Test11.SerialInt) == 0)) {
					Log.v(TAG, "发送数据,sendToSlave数组:" + sendToSlave[i] + "   ;Test11.SerialInt:" + Test11.SerialInt);
					// Log.d(global.SHOW,SHOW,
					// "############write : find data############");
				} else {
					Log.v(TAG, "############write card: no data###########");
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int j = 1000000; j > 0; j--)
				;
		}
	}

	// 发送串口2数据，没用到
	public void sendData2(String name) {
		char sendToSlave[] = new char[name.length()];
		sendToSlave = name.toCharArray();
		for (int i = 0; i < sendToSlave.length; i++) {
			try {
				if ((iPerson.sendDataChar(sendToSlave[i], Test11.SerialInt2) == 0)) {
					Log.v(TAG, "发送数据,sendToSlave数组:" + sendToSlave[i] + "   ;Test11.SerialInt2:" + Test11.SerialInt2);
					// Log.d(global.SHOW,SHOW,
					// "############write : find data############");
				} else {
					Log.v(TAG, "############write card: no data###########");
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int j = 1000000; j > 0; j--)
				;
		}
	}

	// 测试
	public void test() {
		char sendToSlave[] = new char[8];
		sendToSlave[0] = 'A';
		sendToSlave[1] = 'T';
		sendToSlave[2] = '-';
		sendToSlave[3] = 'C';
		sendToSlave[4] = 'G';
		sendToSlave[5] = '+';
		sendToSlave[6] = 0xd;
		sendToSlave[7] = 0xa;
		System.out.println(sendToSlave);
		for (int i = 0; i < sendToSlave.length; i++) {
			try {
				if ((iPerson.sendDataChar(sendToSlave[i], Test11.SerialInt2) == 0)) {
					Log.v(TAG, "发送数据,sendToSlave数组:" + sendToSlave[i] + "   ;Test11.SerialInt2:" + Test11.SerialInt2);
					// Log.d(global.SHOW,SHOW,
					// "############write : find data############");
				} else {
					Log.v(TAG, "############write card: no data###########");
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// for (int j = 1000000; j > 0; j--)
			// ;
		}
	}

}
