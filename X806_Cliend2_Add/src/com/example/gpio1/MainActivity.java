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
	// ����1
	private Button btn_connection_port1;
	private Button btn_sendData1;
	private Button btn_receive_data1;
	// ����2
	private Button btn_connection_port2;
	private Button btn_sendData2;
	private Button btn_receive_data2;
	// ������
	private Button btn_open_bluetooth;
	// ����״̬
	private TextView tv_bluetooth_state;
	// �ر�����
	private Button btn_close_bluetooth;

	// ����������Pl08 gpio
	private Button btn_OpenPl08;
	private Button btn_ClosePl08;

	// ��ȡ��������
	private Button btn_get_bluetooth_name;
	// ������������
	private Button btn_set_bluetooth_name;
	// ��ʾ�����������ƺ�Ľ����ok
	private TextView tv_show_bluetooth_result;
	// ��ȡRTCʱ��
	private Button btn_get_time;
	// ����RTCʱ��
	private Button btn_set_time;
	// ����RTCʱ������ʾ
	private TextView tv_rtc_show_result;
	// �������������
	private EditText tv_set_bluetooth_name;

	// ģ��ժ�����һ������尴ť
	private Button btn_a;
	private Button btn_b;
	private Button btn_c;

	private Button btn_Down;// ģ�°���,���������Ǹ���ť
	private Button btn_Up;// ģ��̧��,���������Ǹ���ť
	PrintWriter pw = null;
	FileWriter fw = null;

	private EditText et_year;// ��
	private EditText et_mouth;// ��
	private EditText et_day;// ��
	private EditText et_hour;// ʱ
	private EditText et_minute;// ��
	private EditText et_second;// ��

	public native String setDirection(String port, int num, String inout);

	public native int setValue(String port, int num, int value);

	private static final String TAG = "X806Cliend";
	// ���浱ǰ����
	public static int current;

	Runnable runnable = null;
	// ��ʱ������
	public int count = 0;

	long downTime = 0;// ���հ��¹㲥ʱ��
	long upTime = 0;// ����̧��㲥ʱ��
	// �����������ʧ�ܣ���ô��try3��
	int countBluetoothNum = 0;
	// �������RTCʧ�ܣ���ô��try3��
	int countRtcNum = 0;

	static {
		System.loadLibrary("gpio_lib");
	}

	// ʵ����ServiceConnection
	private ServiceConnection conn = new ServiceConnection() {
		@Override
		synchronized public void onServiceConnected(ComponentName name, IBinder service) {
			// ���IPerson�ӿ�
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
				// �����̣߳���ѭ������ÿ��1��ʱ����һ�����ݳ�ȥ
				count = 0;
				handler.removeCallbacks(runnable);
				runnable = new Runnable() {
					@Override
					public void run() {
						handler.postDelayed(this, 1 * 1000);
						count++;
						Log.v(TAG, "count:" + count);
						// 8s������,�ײ��������Ĺ㲥���Ѿ���Ҫ4s
						if (count == 8) {
							handler.sendEmptyMessage(2);
						}
					}
				};
				handler.postDelayed(runnable, 1 * 1000);
				break;
			case 2:
				// ִ������Ļ���˵�����˵绰��������ʱ
				handler.removeCallbacks(runnable);
				count = 0;
				Intent intent = new Intent();
				intent.setAction("BSK_BROADCAST_HAND_OFF");
				sendBroadcast(intent);
				break;
			case 3:
				// �ر�����
				bsk_disanble_bt();
				Toast.makeText(MainActivity.this, "�����ѹر�", Toast.LENGTH_SHORT).show();
				break;
			case 4:
				// ������������3��
				countBluetoothNum++;
				Log.v(TAG, "countBluetoothNum:" + countBluetoothNum);
				if (countBluetoothNum <= 3) {
					// ���Ӵ��ڲ�������������
					setBluetoothName();
					// ����������������ʱ���ص�����
					getBluetoothData();
				} else {
					countBluetoothNum = 0;
					tv_show_bluetooth_result.setText("������������ʧ��!");
					// handler.sendEmptyMessageDelayed(5, 5 * 1000);
				}
				break;
			case 5:
				// 5s�����tv_show_bluetooth_result
				tv_show_bluetooth_result.setText("");
				break;
			case 6:
				// ������RTC��3��
				countRtcNum++;
				Log.v(TAG, "countNum:" + countRtcNum);
				if (countRtcNum <= 3) {
					// ���Ӵ��ڲ�����Rtcʱ��
					setRtc();
					// ��ȡRtc����
					getRtcData();
				} else {
					countRtcNum = 0;
					tv_rtc_show_result.setText("����RTCʱ��ʧ�ܣ�");
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

		// ����1
		btn_connection_port1 = (Button) findViewById(R.id.btn_connection_port1);
		btn_sendData1 = (Button) findViewById(R.id.btn_sendData1);
		btn_receive_data1 = (Button) findViewById(R.id.btn_receive_data1);
		// ����2
		btn_connection_port2 = (Button) findViewById(R.id.btn_connection_port2);
		btn_sendData2 = (Button) findViewById(R.id.btn_sendData2);
		btn_receive_data2 = (Button) findViewById(R.id.btn_receive_data2);

		// ����ر�����
		btn_open_bluetooth = (Button) findViewById(R.id.btn_open_bluetooth);
		btn_close_bluetooth = (Button) findViewById(R.id.btn_close_bluetooth);
		// ��ʾ����״̬
		tv_bluetooth_state = (TextView) findViewById(R.id.tv_bluetooth_state);
		// ��ȡ��������������
		btn_get_bluetooth_name = (Button) findViewById(R.id.btn_get_bluetooth_name);
		btn_set_bluetooth_name = (Button) findViewById(R.id.btn_set_bluetooth_name);
		tv_show_bluetooth_result = (TextView) findViewById(R.id.tv_show_bluetooth_result);

		// ��ȡ������rtcʱ��
		btn_set_time = (Button) findViewById(R.id.btn_set_time);
		btn_get_time = (Button) findViewById(R.id.btn_get_time);
		tv_rtc_show_result = (TextView) findViewById(R.id.tv_rtc_show_result);
		btn_Down = (Button) findViewById(R.id.btn_Down);// ģ�°���
		btn_Up = (Button) findViewById(R.id.btn_Up);// ģ��̧��

		btn_a = (Button) findViewById(R.id.btn_a);
		btn_b = (Button) findViewById(R.id.btn_b);
		btn_c = (Button) findViewById(R.id.btn_c);
		// �������������
		tv_set_bluetooth_name = (EditText) findViewById(R.id.tv_set_bluetooth_name);

		// ����������Pl08 gpio
		btn_OpenPl08 = (Button) findViewById(R.id.btn_OpenPl08);
		btn_ClosePl08 = (Button) findViewById(R.id.btn_ClosePl08);

		et_year = (EditText) findViewById(R.id.et_year);// ��
		et_mouth = (EditText) findViewById(R.id.et_mouth);// ��
		et_day = (EditText) findViewById(R.id.et_day);// ��
		et_hour = (EditText) findViewById(R.id.et_hour);// ʱ
		et_minute = (EditText) findViewById(R.id.et_minute);// ��
		et_second = (EditText) findViewById(R.id.et_second);// ��

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

		// ʵ����Intent
		Intent intent = new Intent("com.bsk.x806.action.MY_REMOTE_SERVICE");
		// ����Intent Action ����
		bindService(intent, conn, Service.BIND_AUTO_CREATE);
		// ������
		// bsk_enble_bt();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// �ҵ绰���ָ������㲥ע��
		OpenSoundBroadcastReg();
		// ����绰�㲥�������㲥ע��
		closeSoundBroadcastReg();
		// ����㲥������㲥ע��
		phoneVoiceBroadcastReg();
		// ����ֹͣ�㲥ע��
		phoneVoiceBroadcastReg1();
		// ��ݼ����¹㲥
		BtnDownBroadcastReg();
		// ��ݼ�̧��㲥
		BtnUpBroadcastReg();

		// ��ť�Ƶ����
		Btn1UpBroadcastReg();
		Btn1DownBroadcastReg();

		// ��ť�Ժ�����
		Btn2UpBroadcastReg();
		Btn2DownBroadcastReg();

		// ��ťʹ��˵��
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

	// ����绰�㲥�������㲥ע��
	private void closeSoundBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("BSK_BROADCAST_HAND_ON");// �����Լ���Ҫ���յ�ϵͳ�㲥
		this.registerReceiver(closeSoundBroadcast, filter);
	}

	// int max =mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
	// ����绰�㲥�����ս����㲥;BSK_BROADCAST_HAND_ON
	private BroadcastReceiver closeSoundBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("BSK_BROADCAST_HAND_ON")) {
				Log.v(TAG, "BSK_BROADCAST_HAND_ON");
				Toast.makeText(MainActivity.this, "BSK_BROADCAST_HAND_ON", Toast.LENGTH_SHORT).show();
				AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				// ÿ������绰���Ѷ�ʱ��ֹͣ��������
				handler.removeCallbacks(runnable);
				count = 0;
				// ��ǰ��Ϊ����ʱ���Űѵ�ǰ�����������������þ���
				if ((mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM)) != 0) {
					current = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
					mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
				}
				int testcurrent = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
				Log.v(TAG, "testcurrent:" + testcurrent);
			}
		}
	};

	// �ҵ绰���ָ������㲥ע��
	private void OpenSoundBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("BSK_BROADCAST_HAND_OFF");// �����Լ���Ҫ���յ�ϵͳ�㲥
		this.registerReceiver(openSoundBroadcast, filter);
	}

	// �ҵ绰�����ջָ������㲥
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

	// ����㲥������㲥ע��
	private void phoneVoiceBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("BSK_BROADCAST_RING_ON");// �����Լ���Ҫ���յ�ϵͳ�㲥
		this.registerReceiver(phoneVoiceBroadcast, filter);
	}

	// ����㲥����������㲥��ÿ��һ�ζ���һ���㲥������ʱ��������������5s����û�й㲥�����Ļ���˵��ֹͣ�˻��߽��˵绰�������ûָ�������
	private BroadcastReceiver phoneVoiceBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("BSK_BROADCAST_RING_ON")) {
				Log.v(TAG, "BSK_BROADCAST_RING_ON");
				// SimpleDateFormat formatter = new
				// SimpleDateFormat("yyyy��MM��dd�� HH:mm:ss ");
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

	// ����ֹͣ�㲥��û�õ�
	private void phoneVoiceBroadcastReg1() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("BSK_BROADCAST_RING_OFF");// �����Լ���Ҫ���յ�ϵͳ�㲥
		this.registerReceiver(phoneVoiceBroadcast1, filter);
	}

	// ����ֹͣ�㲥��û�õ�
	private BroadcastReceiver phoneVoiceBroadcast1 = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("BSK_BROADCAST_RING_OFF")) {
				Log.v(TAG, "BSK_BROADCAST_RING_OFF");
				// SimpleDateFormat formatter = new
				// SimpleDateFormat("yyyy��MM��dd�� HH:mm:ss ");
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

	// ��ݼ����¹㲥
	private void BtnDownBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("BSK_BROADCAST_HOTKEY_DOWN");// �����Լ���Ҫ���յ�ϵͳ�㲥
		this.registerReceiver(BtnDownBroadcast, filter);
	}

	// ��ݼ����¹㲥
	private BroadcastReceiver BtnDownBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// ���հ��¹㲥ʱ��
			downTime = System.currentTimeMillis();
			if (action.equals("BSK_BROADCAST_HOTKEY_DOWN")) {
				Log.v(TAG, "BSK_BROADCAST_HOTKEY_DOWN");
				Toast.makeText(MainActivity.this, "BSK_BROADCAST_HOTKEY_DOWN", Toast.LENGTH_SHORT).show();
			}
		}
	};

	// ��ݼ�̧��㲥
	private void BtnUpBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("BSK_BROADCAST_HOTKEY_UP");// �����Լ���Ҫ���յ�ϵͳ�㲥
		this.registerReceiver(BtnUpBroadcast, filter);
	}

	// ��ݼ�̧��㲥
	private BroadcastReceiver BtnUpBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// ����̧��㲥ʱ��
			upTime = System.currentTimeMillis();
			if (action.equals("BSK_BROADCAST_HOTKEY_UP")) {
				long timeOut = upTime - downTime;
				Log.v(TAG, "timeOut:" + timeOut);
				if (timeOut < 1000) {
					bsk_enble_bt();// ������
					// 10���Ӻ�ر�����
					handler.sendEmptyMessageDelayed(3, 600 * 1000);
					Toast.makeText(MainActivity.this, "���ڴ�����....", Toast.LENGTH_SHORT).show();
				} else if (timeOut > 1000) {
					Toast.makeText(MainActivity.this, "����:BSK_BROADCAST_HOTKEY_UP", Toast.LENGTH_SHORT).show();
				}
				// ����
				downTime = 0;
				upTime = 0;

			}
		}
	};

	// ��ť�Ƶ����Up�㲥
	private void Btn1UpBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("BSK_BROADCAST_INTRODUCTION_UP");// �����Լ���Ҫ���յ�ϵͳ�㲥
		this.registerReceiver(Btn1UpBroadcast, filter);
	}

	// ��ť�Ƶ����Up�㲥
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

	// ��ť�Ƶ����Down�㲥
	private void Btn1DownBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("BSK_BROADCAST_INTRODUCTION_DOWN");// �����Լ���Ҫ���յ�ϵͳ�㲥
		this.registerReceiver(Btn1DownBroadcast, filter);
	}

	// ��ť�Ƶ����Down�㲥
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

	// ��ť�Ժ�����Up�㲥
	private void Btn2UpBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("BSK_BROADCAST_HAVEFUN_UP");// �����Լ���Ҫ���յ�ϵͳ�㲥
		this.registerReceiver(Btn2UpBroadcast, filter);
	}

	// ��ť�Ժ�����Up�㲥
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

	// ��ť�Ժ�����Down�㲥
	private void Btn2DownBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("BSK_BROADCAST_HAVEFUN_DOWN");// �����Լ���Ҫ���յ�ϵͳ�㲥
		this.registerReceiver(Btn2DownBroadcast, filter);
	}

	// ��ť�Ժ�����Down�㲥
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

	// ��ťʹ��˵��Up�㲥
	private void Btn3UpBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("BSK_BROADCAST_GUIDE_UP");// �����Լ���Ҫ���յ�ϵͳ�㲥
		this.registerReceiver(Btn3UpBroadcast, filter);
	}

	// ��ťʹ��˵��Up�㲥
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

	// ��ťʹ��˵��Down�㲥
	private void Btn3DownBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("BSK_BROADCAST_GUIDE_DOWN");// �����Լ���Ҫ���յ�ϵͳ�㲥
		this.registerReceiver(Btn3DownBroadcast, filter);
	}

	// ��ťʹ��˵��Down�㲥
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
	 * ��ť����¼�
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// ���Ӵ���1
		if (R.id.btn_connection_port1 == v.getId()) {
			Log.v(TAG, "btn_connection_port1");
			Test11.getInstance(iPerson).recvData(new Test11.IX806masterRecv() {
				@Override
				public void received(String data) {
					Log.v(TAG, "data" + data);
				}

				@Override
				public void connected() {
					Log.v(TAG, "��λ������1���ӳɹ���");
					// ���Ӵ��ڳɹ���ʼ������
				}

				@Override
				public void disconnected(String error) {
					Log.v(TAG, error);
				}
			});
		}
		// ����1��������
		if (R.id.btn_sendData1 == v.getId()) {
			Log.v(TAG, "����1��������");
			// sendData("AT-CD-2");
			// Test11.getInstance(iPerson).recvValue();
		}
		// ����1��������
		if (R.id.btn_receive_data1 == v.getId()) {
			// Test11.getInstance(iPerson).recvValue();
		}
		// ���Ӵ���2
		if (R.id.btn_connection_port2 == v.getId()) {
			Log.v(TAG, "btn_connection_port2");
			Test11.getInstance(iPerson).recvData2(new Test11.IX806masterRecv() {

				@Override
				public void received(String data) {
					Log.v(TAG, "data" + data);
				}

				@Override
				public void connected() {
					Log.v(TAG, "��λ������2���ӳɹ���");
					// ���Ӵ��ڳɹ���ʼ������
				}

				@Override
				public void disconnected(String error) {
					Log.v(TAG, error);
				}
			});
		}
		// ����2��������
		if (R.id.btn_sendData2 == v.getId()) {
			Log.v(TAG, "����2��������");
			// sendData("AT-CD-2");
		}
		// ����2��������
		if (R.id.btn_receive_data2 == v.getId()) {
			// Test11.getInstance(iPerson).recvValue2();
		}
		// ������
		if (R.id.btn_open_bluetooth == v.getId()) {
			bsk_enble_bt();
		}
		// �ر�����
		if (R.id.btn_close_bluetooth == v.getId()) {
			bsk_disanble_bt();
		}

		// ��ȡ��������
		if (R.id.btn_get_bluetooth_name == v.getId()) {
			bsk_read_bt_name();
		}
		// ������������
		if (R.id.btn_set_bluetooth_name == v.getId()) {
			// ������������
			setBluetoothName();
			// ��ȡ����
			getBluetoothData();
		}

		// ��ȡRTCʱ��
		if (R.id.btn_get_time == v.getId()) {
			bsk_get_rtc();
		}

		// ����RTCʱ��
		if (R.id.btn_set_time == v.getId()) {
			// ���Ӵ��ڲ�����Rtcʱ��
			setRtc();
			// ��ȡRtc����
			getRtcData();
		}
		//// ����Pl08 gpio
		if (R.id.btn_OpenPl08 == v.getId()) {
			bsk_open_pl08();
		}
		// ����Pl08 gpio
		if (R.id.btn_ClosePl08 == v.getId()) {
			bsk_close_pl08();
		}
		if (R.id.btn_Down == v.getId()) {// ģ�°���
			Intent intent = new Intent();
			intent.setAction("anxia");
			sendBroadcast(intent);
		}

		if (R.id.btn_Up == v.getId()) {// ģ��̧��
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
	 * ���Ӵ��ڲ�������������
	 */
	public void setBluetoothName() {
		String name = tv_set_bluetooth_name.getText().toString().trim();
		if (name.equals("")) {
			Toast.makeText(MainActivity.this, "��������������", Toast.LENGTH_SHORT).show();
			return;
		}
		Test11.getInstance(iPerson).recvData2(new Test11.IX806masterRecv() {
			@Override
			public void received(String data) {
				Log.v(TAG, "data" + data);
			}

			@Override
			public void connected() {
				Log.v(TAG, "��λ������2���ӳɹ���");
				// ���Ӵ��ڳɹ���ʼ������
			}

			@Override
			public void disconnected(String error) {
				Log.v(TAG, error);
			}
		});
		sleepp(500);

		// ����
		setDirection("PL", 9, "out");
		setValue("PL", 9, 0);

		sleepp(1 * 1000);
		bsk_write_bt_name(name);
		sleepp(1 * 1000);

		// ����
		int open = setValue("PL", 9, 1);
		// ��ʾ����״̬
		showBluetoothState(open);
		sleepp(500);
	}

	/**
	 * ����������������ʱ���ص�����
	 */
	public void getBluetoothData() {
		// ��������
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
						Log.v(TAG, "�����������Ƴɹ���");
						tv_show_bluetooth_result.setText("�����������Ƴɹ���");
						// handler.sendEmptyMessageDelayed(5, 5 * 1000);
					} else {
						tv_show_bluetooth_result.setText("");
						countData = 0;
						handler.sendEmptyMessage(4);
					}
				} else {
					// Toast.makeText(MainActivity.this, "�����ݷ��أ�������������ʧ��!",
					// Toast.LENGTH_SHORT).show();
					tv_show_bluetooth_result.setText("");
					handler.sendEmptyMessage(4);
					// handler.sendEmptyMessageDelayed(5, 5 * 1000);
				}
			}
		});
	}

	/**
	 * ���Ӵ��ڲ�����RTCʱ��
	 */
	public void setRtc() {
		Test11.getInstance(iPerson).recvData(new Test11.IX806masterRecv() {
			@Override
			public void received(String data) {
				Log.v(TAG, "data" + data);
			}

			@Override
			public void connected() {
				Log.v(TAG, "��λ������1���ӳɹ���");
				// ���Ӵ��ڳɹ���ʼ������
			}

			@Override
			public void disconnected(String error) {
				Log.v(TAG, error);
			}
		});
		sleepp(500);
		// bsk_set_rtc(2115, "10", "12", "2", "30", "20");
		try {
			et_day = (EditText) findViewById(R.id.et_day);// ��
			et_hour = (EditText) findViewById(R.id.et_hour);// ʱ
			et_minute = (EditText) findViewById(R.id.et_minute);// ��
			et_second = (EditText) findViewById(R.id.et_second);// ��

			int year = Integer.parseInt(et_year.getText().toString());
			String mouth = et_mouth.getText().toString();
			String day = et_day.getText().toString();
			String hour = et_hour.getText().toString();
			String minute = et_minute.getText().toString();
			String second = et_second.getText().toString();

			bsk_set_rtc(year, mouth, day, hour, minute, second);
		} catch (Exception e) {
			Toast.makeText(MainActivity.this, "����������  ��  ��������ݸ�ʽ����!", Toast.LENGTH_SHORT).show();
			tv_rtc_show_result.setText("");
			return;
		}
		sleepp(500);
	}

	/**
	 * ��������RTCʱ��ʱ���ص�����
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
						Log.v(TAG, "����RTCʱ��ɹ���");
						tv_rtc_show_result.setText("����RTCʱ��ɹ���");
						// handler.sendEmptyMessageDelayed(5, 5 * 1000);
					} else {
						tv_rtc_show_result.setText("");
						countData = 0;
						handler.sendEmptyMessage(6);
					}
				} else {
					tv_rtc_show_result.setText("");
					handler.sendEmptyMessage(6);
					// Toast.makeText(MainActivity.this, "����RTC��ʽ����!",
					// Toast.LENGTH_SHORT).show();
					// handler.sendEmptyMessageDelayed(5, 5 * 1000);
				}
			}

		});
	}

	// ������
	public void bsk_enble_bt() {
		Log.v(TAG, "����");
		setDirection("PL", 9, "out");
		setValue("PL", 9, 0);
		sleepp(1 * 1000);
		Log.v(TAG, "����");
		int open = setValue("PL", 9, 1);
		Log.v(TAG, "open:" + open);
		// ��ʾ����״̬
		showBluetoothState(open);
	}

	// �ر�����
	public void bsk_disanble_bt() {
		Log.v(TAG, "����");
		setDirection("PL", 9, "out");
		int close = setValue("PL", 9, 0);
		Log.v(TAG, "close:" + close);
		// ��ʾ����״̬
		showBluetoothState(close);
	}

	// ����PL08 GPIO
	public void bsk_open_pl08() {
		Log.v(TAG, "����PL08");
		setDirection("PL", 8, "out");
		setValue("PL", 8, 1);
	}

	// ����PL08 GPIO
	public void bsk_close_pl08() {
		Log.v(TAG, "����PL08 GPIO");
		setDirection("PL", 8, "out");
		setValue("PL", 8, 0);
	}

	/**
	 * ��ʾ����״̬
	 * 
	 * @param state
	 */
	public void showBluetoothState(int state) {
		if (state == 1 || state == 0) {
			if (state == 0) {
				tv_bluetooth_state.setText("�����ѹرգ�");
			} else if (state == 1) {
				tv_bluetooth_state.setText("�����Ѵ򿪣�");
			}
		} else {
			tv_bluetooth_state.setText("�򿪻�ر�����ʧ�ܣ�");
		}
	}

	// ��ȡ��������
	public void bsk_read_bt_name() {
		Log.v(TAG, "bsk_read_bt_name����ȡ��������");
	}

	// ������������
	public void bsk_write_bt_name(String name) {
		Log.v(TAG, "bsk_write_bt_name��������������");
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
					Log.v(TAG, "��������,sendToSlave����:" + sendToSlave[i] + "   ;Test11.SerialInt2:" + Test11.SerialInt2);
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

	// ��ȡRTCʱ��
	public void bsk_get_rtc() {
		Log.v(TAG, "bsk_get_rtc����ȡRTCʱ��");
	}

	// ����RTCʱ��
	// ���ã�bsk_set_rtc(2015, "10", "26", "17", "22", "0");
	public void bsk_set_rtc(int year, String mouth, String day, String hour, String minute, String second) {
		char sendToSlave[] = new char[12];
		String s = Integer.toHexString(year);
		String year1 = "";
		String year2_1 = "";
		if (s.length() > 2) {
			year1 = s.charAt(0) + "";// ��һ��
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
					// Log.v(TAG, "��������,sendToSlave����:" + aa + "
					// ;Test11.SerialInt:" + Test11.SerialInt);
					Log.v(TAG, "��������,sendToSlave����:" + sendToSlave[i] + ";Test11.SerialInt:" + Test11.SerialInt);
					// Log.v(TAG,"��������,sendToSlave����:" + iTen + "
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

	// û�õ�
	/**
	 * ��װ�ӿ����ڻص�
	 * 
	 * @author gzdlw
	 */
	public interface IX806masterRecv {
		/**
		 * �����յ�����ʱ
		 * 
		 * @param recvType
		 *            ��������
		 * @param data
		 *            ����
		 */
		public void received(String data);

		/**
		 * �������ӳɹ�
		 */
		public void connected();

		/**
		 * ��������ʧ��
		 * 
		 * @param error
		 *            ������Ϣ
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

	// ����RTCʱ��,û�õ�
	public void bsk_set_rtc() {
		Log.v(TAG, "bsk_set_rtc������RTCʱ��");
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
					// Log.v(TAG, "��������,sendToSlave����:" + aa + "
					// ;Test11.SerialInt:" + Test11.SerialInt);
					Log.v(TAG, "��������,sendToSlave����:" + sendToSlave[i] + ";Test11.SerialInt:" + Test11.SerialInt);
					// Log.v(TAG,"��������,sendToSlave����:" + iTen + "
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

	// ���ʹ���1���ݣ�û�õ�
	public void sendData1(String name) {
		char sendToSlave[] = new char[name.length()];
		sendToSlave = name.toCharArray();
		for (int i = 0; i < sendToSlave.length; i++) {
			try {
				if ((iPerson.sendDataChar(sendToSlave[i], Test11.SerialInt) == 0)) {
					Log.v(TAG, "��������,sendToSlave����:" + sendToSlave[i] + "   ;Test11.SerialInt:" + Test11.SerialInt);
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

	// ���ʹ���2���ݣ�û�õ�
	public void sendData2(String name) {
		char sendToSlave[] = new char[name.length()];
		sendToSlave = name.toCharArray();
		for (int i = 0; i < sendToSlave.length; i++) {
			try {
				if ((iPerson.sendDataChar(sendToSlave[i], Test11.SerialInt2) == 0)) {
					Log.v(TAG, "��������,sendToSlave����:" + sendToSlave[i] + "   ;Test11.SerialInt2:" + Test11.SerialInt2);
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

	// ����
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
					Log.v(TAG, "��������,sendToSlave����:" + sendToSlave[i] + "   ;Test11.SerialInt2:" + Test11.SerialInt2);
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
