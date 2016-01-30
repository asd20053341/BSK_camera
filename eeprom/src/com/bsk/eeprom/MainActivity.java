package com.bsk.eeprom;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private static final String TAG = "KjunChen";
	private static final String BC_KEY = "PPFRAM_PIR_ACTIVE";

	public native String OpenDev();

	public native String CloseDev();

	public native String ReadUuid();

	public native String WriteUuid(String buf); // 32

	public native char ReadVersion();

	public static native String setDirection(String port, int num, String inout);

	public static native String setValue(String port, int num, int value);

	public static native int enablePwm();

	public static native int disablePwm();

	public native int enablePwmTime(int msecond);

	// SN�Ŷ�ȡ
	public native String ReadSn();
	// SN��д��
	public native String WriteSn(String buf); // 16
	//���κŶ�ȡ
	public native String ReadBatch();
	//���κ�д��
	public native String WriteBatch(String buf); // 16


	public native String WriteVersion(char value); 
	

	private Map<String, String> packageNameMap;

	private MediaPlayer mpMediaPlayer = null;
	// ����㲥��
	private int count;

	// ��й㲥��
	private int lightCount;
	private String snValue;
	private String batchValue;
	private String uuidValue;
	private String version_name;
	private String version_code;

	// �����汾����
	private TextView cloud_version;
	private TextView SN_read;
	private TextView barch_read;
	private TextView uuid_read;
	// ����㲥
	private TextView sendPIR_number;
	// ��й㲥
	private TextView lightSensor_number;
	private Button btn_led;
	// SN��
	private Button snWriteBtn;
	// ���κ�
	private Button batchWriteBtn;
	private Button uuidWriteBtn;
	// һ��д��SN�����κţ�UUID
	private Button A_Key_To;
	private Context mContext;
	// 606ֵ
	private EditText keyEt;
	// SN��
	private EditText snEt;
	// ���κ�
	private EditText batchEt;

	private Button btn_snread;
	private Button btn_uuidread;
	private Button btn_batchread;
	private Button btn_refresh;
	private Button btn_redled;
	private Button btn_blueled;

	// �������
	private Button btn_CloudPhoto_open;
	private Button btn_radio_open;
	private Button btn_music_open;
	private Button btn_update_open;
	private Button btn_camera;
	private Button btn_settings;
	// ����㲥
	private Button btn_sendPIR;
	// ��й㲥
	private Button btn_lightSensor;

	// ���ֲ�������ͣ
	private Button btn_musicplay;
	// ������ť
	private Button btn_recorder;
	// �����ļ�����·��
	private String fileName = null;
	// ������������
	private MediaPlayer mPlayer = null;
	private MediaRecorder mRecorder = null;

	// ��ʾ��
	private Button btn1, btn2;

	private TextView infoTv;
	private StringBuffer infoBuffer;

	static {
		System.loadLibrary("eeprom_bsk");
	}

	static {
		System.loadLibrary("gpio_lib");
	}

	private static final String KEY = "606";

	private SensorManager sm;
	private Sensor ligthSensor;
	
	Handler handle = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_frametest2);
		OpenDev();
		infoBuffer = new StringBuffer(1024);

		handle = new Handler();
		
		initData();
		PlayMusic();

		mContext = this;
		snWriteBtn = (Button) findViewById(R.id.sn_write_btn);
		keyEt = (EditText) findViewById(R.id.key_et);
		snEt = (EditText) findViewById(R.id.sn_et);
		batchEt = (EditText) findViewById(R.id.batch_et);
		batchWriteBtn = (Button) findViewById(R.id.batch_write_btn);
		uuidWriteBtn = (Button) findViewById(R.id.uuid_write_btn);
		btn_CloudPhoto_open = (Button) findViewById(R.id.btn_CloudPhoto_open);
		btn_radio_open = (Button) findViewById(R.id.btn_radio_open);
		btn_music_open = (Button) findViewById(R.id.btn_music_open);
		btn_update_open = (Button) findViewById(R.id.btn_update_open);
		btn_camera = (Button) findViewById(R.id.btn_camera);
		btn_settings = (Button) findViewById(R.id.btn_settings);
		btn_sendPIR = (Button) findViewById(R.id.btn_sendPIR);
		btn_lightSensor = (Button) findViewById(R.id.btn_lightSensor);
		A_Key_To = (Button) findViewById(R.id.A_Key_To);
		btn_redled = (Button) findViewById(R.id.btn_redled);
		btn_blueled = (Button) findViewById(R.id.btn_blueled);

		cloud_version = (TextView) findViewById(R.id.cloud_version);
		infoTv = (TextView) findViewById(R.id.textView);
		btn_refresh = (Button) findViewById(R.id.btn_refresh);
		btn_musicplay = (Button) findViewById(R.id.btn_musicplay);
		btn_recorder = (Button) findViewById(R.id.btn_recorder);
		/*
		 * btn_snread = (Button) findViewById(R.id.btn_snread); btn_uuidread =
		 * (Button) findViewById(R.id.btn_uuidread); btn_batchread = (Button)
		 * findViewById(R.id.btn_batchread); SN_read = (TextView)
		 * findViewById(R.id.SN_read); uuid_read = (TextView)
		 * findViewById(R.id.uuid_read); barch_read = (TextView)
		 * findViewById(R.id.barch_read);
		 */

		sendPIR_number = (TextView) findViewById(R.id.sendPIR_number);
		lightSensor_number = (TextView) findViewById(R.id.lightSensor_number);
		btn_recorder.setOnClickListener(this);
		btn_CloudPhoto_open.setOnClickListener(this);
		btn_radio_open.setOnClickListener(this);
		btn_music_open.setOnClickListener(this);
		btn_update_open.setOnClickListener(this);
		btn_camera.setOnClickListener(this);
		btn_settings.setOnClickListener(this);
		btn_sendPIR.setOnClickListener(this);
		btn_lightSensor.setOnClickListener(this);
		btn_musicplay.setOnClickListener(this);
		getAppInfo();// ���������İ汾���ƣ��汾�ţ�android�汾�ţ�SDK�汾��
		snWriteBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {

				String sn = ReadSn();

				if (TextUtils.isEmpty(sn)) {
					writingSn();
				} else {
					// ����606ʱ������writingSn();
					if (keyEt.getText().toString().equals(KEY)) {
						writingSn();
						Toast.makeText(mContext, "д��SN�ųɹ���", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(mContext, "sn�Ѵ��ڣ������д��������ȷ��key!", Toast.LENGTH_SHORT).show();
					}
				}

			}
		});

		batchWriteBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {

				String batch = ReadBatch();

				if (TextUtils.isEmpty(batch)) {
					writingBatch();
				} else {
					if (keyEt.getText().toString().equals(KEY)) {
						writingBatch();
						Toast.makeText(mContext, "д�����κųɹ���", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(mContext, "batch�Ѵ��ڣ������д��������ȷ��key!", Toast.LENGTH_SHORT).show();
					}
				}

			}
		});

		uuidWriteBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String uuid = ReadUuid();
				if (TextUtils.isEmpty(uuid)) {
					writingUuid();
				} else {
					if (keyEt.getText().toString().equals(KEY)) {
						writingUuid();
						Toast.makeText(mContext, "д��uuid�ɹ���", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(mContext, "uuid�Ѵ��ڣ������д��������ȷ��key!", Toast.LENGTH_SHORT).show();
					}

				}

			}
		});

		A_Key_To.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SnBatchUuid();
				infoBuffer.setLength(0);
				readAllInfo();
			}
		});
		btn_blueled.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(com.bsk.eeprom.MainActivity.this, com.example.gpio1.MainActivity.class);
				startActivity(intent);
			}
		});

		/*
		 * btn_redled.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) {
		 * 
		 * } });
		 */

		/*
		 * btn_snread.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { // TODO Auto-generated method
		 * stub
		 * 
		 * } }); btn_batchread.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { // TODO Auto-generated method
		 * stub String batch = ReadBatch(); if (TextUtils.isEmpty(batch)) {
		 * barch_read.setText("batchΪ��!"); } else { barch_read.setText("������" +
		 * batch); } } }); btn_uuidread.setOnClickListener(new OnClickListener()
		 * {
		 * 
		 * @Override public void onClick(View v) { // TODO Auto-generated method
		 * stub String uuid = ReadUuid(); if (TextUtils.isEmpty(uuid)) {
		 * uuid_read.setText("uuidΪ��!"); } else { uuid_read.setText("������" +
		 * uuid); } } });
		 */
		btn_refresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				infoBuffer.setLength(0);
				readAllInfo();
			}
		});
		readAllInfo();

	}

	protected void onStart() {
		super.onStart();
		// checkSDCardState();//��ȡ������SD���Ƿ�����
		netBroadcastReg();// ע������㲥�¼�
		// sdBroadcastReg();//ע��sd���㲥�¼�
		PIRBroadcastReg();// ע������Ӧ�㲥�㲥�¼�

	}

	@Override
	protected void onResume() {
		super.onResume();
		lightSensorBroadcastReg();// ע���й㲥�㲥�¼�
		/*
		 * checkNetworkState();
		 * tv_sysLanguage.setText(getResources().getString(R.string.sysLanguage)
		 * + getResources().getConfiguration().locale.getDisplayLanguage());
		 */
	}

	@Override
	protected void onPause() {
		super.onPause();
		sm.unregisterListener(new lightSensorBroadcast());
	}

	protected void onDestroy() {
		super.onDestroy();
		// unregisterReceiver(sdCardBroadcast);
		CloseDev();
		unregisterReceiver(PIRBroadcast);

		unregisterReceiver(netBroadcast);
	}

	private void PlayMusic() {

		try {
			mpMediaPlayer = MediaPlayer.create(this, R.raw.music1);
			// mpMediaPlayer.prepare();
			mpMediaPlayer.start();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void SnBatchUuid() {
		String sn = ReadSn();
		String batch = ReadBatch();
		String uuid = ReadUuid();

		if (TextUtils.isEmpty(sn)) {
			writingSn();
		} else { //
			// ����606ʱ������writingSn();
			if (keyEt.getText().toString().equals(KEY)) {
				writingSn();
				Toast.makeText(mContext, "д��SN�ųɹ���", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mContext, "sn�Ѵ��ڣ������д��������ȷ��key!", Toast.LENGTH_SHORT).show();
			}
		}

		if (TextUtils.isEmpty(batch)) {
			writingBatch();
		} else {
			if (keyEt.getText().toString().equals(KEY)) {
				writingBatch();
				Toast.makeText(mContext, "д�����κųɹ���", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mContext, "batch�Ѵ��ڣ������д��������ȷ��key!", Toast.LENGTH_SHORT).show();
			}
		}

		if (TextUtils.isEmpty(uuid)) {
			writingUuid();
		} else {
			if (keyEt.getText().toString().equals(KEY)) {
				writingUuid();
				Toast.makeText(mContext, "д��uuid�ɹ���", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mContext, "uuid�Ѵ��ڣ������д��������ȷ��key!", Toast.LENGTH_SHORT).show();
			}
		}

	}

	private void initData() {
		// ��ȡ �汾���� String
		version_name = getResources().getString(R.string.versionName);
		// ��ȡ �汾�� String
		version_code = getResources().getString(R.string.versionCode);

		// �ǵðѰ����Ž���
		packageNameMap = new HashMap<String, String>();
		packageNameMap.put("radio", "com.bsk.fmradio");
		packageNameMap.put("music", "com.bsk.music");
		packageNameMap.put("update", "com.bsk.update");
		packageNameMap.put("xiangpianbao", "com.wdxc.xiangpianbao");
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_musicplay:
			if (mpMediaPlayer.isPlaying()) {
				mpMediaPlayer.pause();
			} else {
				mpMediaPlayer.start();
			}
			break;
		case R.id.btn_recorder:
			testMediaRecorder();
			break;
		case R.id.btn_CloudPhoto_open:
			doStartApplication(packageNameMap.get("xiangpianbao"));
			break;
		case R.id.btn_radio_open:
			doStartApplication(packageNameMap.get("radio"));
			break;
		case R.id.btn_music_open:
			doStartApplication(packageNameMap.get("music"));
			break;
		case R.id.btn_update_open:
			doStartApplication(packageNameMap.get("update"));
			break;
		case R.id.btn_camera:
			startActivity(new Intent("android.media.action.IMAGE_CAPTURE"));
			break;
		case R.id.btn_settings:
			startActivity(new Intent(Settings.ACTION_SETTINGS));
			break;
		case R.id.btn_sendPIR:
			// �����Ӧ
			sendBroadcast(new Intent(BC_KEY));
			break;
		/*
		 * case R.id.btn_lightSensor: // ��й㲥 sendBroadcast(new
		 * Intent(BC_LIGHT_SENSOR)); break;
		 */
		default:
			break;
		}
	}

	public void testMediaRecorder() {

		// ����sdcard��·��

		// ��ȡsd����·��
		mRecorder = new MediaRecorder();
		fileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		// ��SD����·���²���һ��audiorecordtest.3gp
		fileName += "/audiorecordtest.3gp";
		// MediaRecorder��Ķ���������Ƶ��Դ
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		// ��������ļ���ʽ
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		// ��������ļ�
		mRecorder.setOutputFile(fileName);
		// ������Ƶ����
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		try {
			btn_recorder.setEnabled(false);
			mRecorder.prepare();
			mRecorder.start();
			Toast.makeText(mContext, "��ʼ¼��", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			Log.e("LOG_TAG", "prepare() failed");
		}

		handle.postDelayed(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(mContext, "¼������", Toast.LENGTH_SHORT).show();
				mRecorder.stop();
				mRecorder.release();
				mRecorder = null;
			}
		}, 8000);

		handle.postDelayed(new Runnable() {
			@Override
			public void run() {

				mPlayer = new MediaPlayer();
				try {
					// �ҵ��ļ�·��
					mPlayer.setDataSource(fileName);
					// ׼��
					Toast.makeText(mContext, "��ʼ����", Toast.LENGTH_SHORT).show();
					mPlayer.prepare();
					// ��ʼ����
					mPlayer.start();

				} catch (IOException e) {
					Log.e("LOG_TAG", "����ʧ��");
				}
			}

		}, 11000);
		handle.postDelayed(new Runnable() {
			public void run() {
				btn_recorder.setEnabled(true);
				Toast.makeText(mContext, "���Ž���", Toast.LENGTH_SHORT).show();

			}
		}, 20000);

	}

	public void writingSn() {
		// ��ȡSN��EditText
		snValue = snEt.getText().toString();
		// �����Ϊ��
		if (!TextUtils.isEmpty(snValue)) {
			// �������Ϊ16
			if (snValue.length() == 16) {
				// ��SNд��ȥ��C++
				WriteSn(snValue);
			} else {
				Toast.makeText(mContext, "������16λsn����!", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(mContext, "������sn����!", Toast.LENGTH_SHORT).show();
		}
	}

	public void writingBatch() {
		batchValue = batchEt.getText().toString();
		if (!TextUtils.isEmpty(batchValue)) {
			if (batchValue.length() == 16) {
				WriteBatch(batchValue);
			} else {
				Toast.makeText(mContext, "������16λ���κ�!", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(mContext, "���������κ�!", Toast.LENGTH_SHORT).show();
		}
	}

	public void writingUuid() {

		String uuidRnd = UUID.randomUUID().toString();
		String uuid = uuidRnd.substring(0, 8) + uuidRnd.substring(9, 13) + uuidRnd.substring(14, 18)
				+ uuidRnd.substring(19, 23) + uuidRnd.substring(24);

		if (!TextUtils.isEmpty(uuid)) {
			WriteUuid(uuid);
		}

	}

	private void doStartApplication(String packageName) {
		// ͨ��������ȡ��APP��ϸ��Ϣ������Activities��services��versioncode��name�ȵ�
		PackageInfo packageInfo = null;
		try {
			//getPackageManager()�ǻ�ȡһ��PackageManager����
			//ͨ���������ȥ��ȡ������ϢgetPackageInfo��packageName�������İ�����
			//PackageManager.GET_CONFIGURATIONS��ȡpackageName������
			packageInfo = getPackageManager().getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		if (packageInfo == null) {
			return;
		}

		// ����һ�����ΪCATEGORY_LAUNCHER�ĸð�����Intent
		Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		//�Ѱ�������Intent
		resolveIntent.setPackage(packageInfo.packageName);

		// ͨ��getPackageManager()��queryIntentActivities��������
		//�Ѱ������ñ�������
		List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(resolveIntent,
				PackageManager.GET_CONFIGURATIONS);

		ResolveInfo resolveInfo = resolveInfoList.iterator().next();

		if (resolveInfo != null) {

			// packagename = ����packname
			String mPackageName = resolveInfo.activityInfo.packageName;
			// �����������Ҫ�ҵĸ�APP��LAUNCHER��Activity[��֯��ʽ��packagename.mainActivityname]
			String mClassName = resolveInfo.activityInfo.name;

			// Log.i(TAG, "packageName: " + mPackageName + ", className: " +
			// mClassName);
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			ComponentName component = new ComponentName(mPackageName, mClassName);
			intent.setComponent(component);
			startActivity(intent);
		}

	}

	/**
	 * ע���й㲥�㲥�¼�
	 */
	private void lightSensorBroadcastReg() {
		sm = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		ligthSensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT);
		sm.registerListener(new lightSensorBroadcast(), ligthSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	/**
	 * �����й㲥�¼�
	 */
	public class lightSensorBroadcast implements SensorEventListener {
		// ��ȡ�������Ĺ㲥

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub\
			float light = event.values[0];
			lightSensor_number.setText("���ǿ��: " + light);

		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}
	};

	/**
	 * ��������Ӧ�㲥�¼�
	 */
	private BroadcastReceiver PIRBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(BC_KEY)) {
				count++;
				sendPIR_number.setText(getResources().getString(R.string.PIR_Count) + count);
			}
		}
	};

	/**
	 * ע������Ӧ�㲥�㲥�¼�
	 */
	private void PIRBroadcastReg() {
		IntentFilter filter = new IntentFilter(BC_KEY);
		this.registerReceiver(PIRBroadcast, filter);
	}

	public void readAllInfo() {
		infoBuffer.append(readSnStr());
		infoBuffer.append(readBatchStr());
		infoBuffer.append(readUuidStr());
		infoBuffer.append(SystemVersion() + "<br/>");
		infoBuffer.append(SDKVersion() + "<br/>");
		infoBuffer.append(FirmwareVersion() + "<br/>");
		infoBuffer.append("<br/>");
		infoBuffer.append("<br/>");
		infoBuffer.append("<br/>");
		infoBuffer.append("<br/>");
		infoBuffer.append(SysLanguage() + "<br/>");
		infoBuffer.append(ScreenDPI() + "<br/>");
		infoBuffer.append(ScreenResolution() + "<br/>");
		infoBuffer.append(InsideSDCardState() + "<br/>");
		infoBuffer.append(InsideSDCardSize() + "<br/>");
		infoBuffer.append(outsideSDCardState() + "<br/>");
		infoBuffer.append(OutsideSDCardSize() + "<br/>");
		infoBuffer.append(checkNetworkState() + "<br/>");
		infoBuffer.append(getScreenBrightness(MainActivity.this) + "<br/>");
		infoBuffer.append("<br/>");
		infoBuffer.append(getRadioAppInfo() + "<br/>");
		infoBuffer.append(getMusiAppInfo() + "<br/>");
		infoBuffer.append(getUpdateAppInfo() + "<br/>");

		infoTv.setText(Html.fromHtml(infoBuffer.toString()));
	}

	// ��ȡSN��
	public String readSnStr() {
		String sn = ReadSn();

		if (TextUtils.isEmpty(sn)) {
			sn = "<h1><font color=\"red\">snΪ��!</font></h1>";

		} else {
			// sn = ;
			sn = "<h1><font color=\"red\">SN�Ŷ���:" + sn + "</font></h1>";
		}

		return sn;
	}

	// ��ȡ���κ�
	public String readBatchStr() {
		String batch = ReadBatch();
		if (TextUtils.isEmpty(batch)) {
			batch = "<h1><font color=\"red\">���κ�Ϊ�գ�</font></h1>";
		} else {
			batch = "<h1><font color=\"red\">���κŶ�����" + batch + "</font></h1>";
		}
		return batch;
	}

	public String readUuidStr() {
		String uuid = ReadUuid();
		if (TextUtils.isEmpty(uuid)) {

			uuid = "<h1><font color=\"red\">uuidΪ��!</font></h1>";
		} else {
			uuid = "<h1><font color=\"red\">uuid������  " + uuid + "</font></h1>";
		}
		return uuid;

	}

	// ��ȡϵͳ�汾�ţ����磺4.4.2
	public String SystemVersion() {
		String systemVersion = "ϵͳ�汾: " + android.os.Build.VERSION.RELEASE;
		// Log.v("banben", SystemVersion);
		return systemVersion;
	}

	// ��ȡSDK�汾�ţ����磺19
	public String SDKVersion() {
		String sdkVersion = "SDK�汾: " + android.os.Build.VERSION.SDK_INT;
		return sdkVersion;
	}

	// ��ȡ�̼��汾�ţ����磺V2.1.0.01-D150828
	public String FirmwareVersion() {
		String firmwareVersion = "�̼��汾: " + SystemProperties.get("ro.product.firmware");
		return firmwareVersion;
	}

	public String SysLanguage() {
		String sysLanguage = (getResources().getString(R.string.sysLanguage)
				+ getResources().getConfiguration().locale.getDisplayLanguage());
		return sysLanguage;
	}

	public String ScreenDPI() {
		String screenDPI;
		WindowManager windowManager = this.getWindowManager();// ������
		DisplayMetrics metrics = new DisplayMetrics();// ��Ļ�ߴ�
		windowManager.getDefaultDisplay().getMetrics(metrics);/// ����ǰ���ڵ�һЩ��Ϣ����DisplayMetrics���У�

		// ��ǰ��Ļ��X,Y
		/*
		 * float xdpi = metrics.xdpi; float ydpi = metrics.ydpi;
		 */
		//// ��ȡSPIֵ
		int densityDpi = metrics.densityDpi;
		// ����SPIֵ
		screenDPI = (getResources().getString(R.string.dpi) + densityDpi);
		return screenDPI;
	}

	public String ScreenResolution() {
		String screenResolution;
		// �½�һ�����ڻ��ƣ���Activity��ͬ��������ڿ��Բ���ʾ����
		WindowManager windowManager = this.getWindowManager();// ������
		DisplayMetrics metrics = new DisplayMetrics();// ��Ļ�ߴ�
		windowManager.getDefaultDisplay().getMetrics(metrics);// ����ǰ���ڵ�һЩ��Ϣ����DisplayMetrics���У�
		
		// ��ȡ��Ļ�ֱ���
		int widthPixels = metrics.widthPixels;
		int heightPixels = metrics.heightPixels;

		// ������Ļ�ֱ���
		screenResolution = (getResources().getString(R.string.screenDPI) + widthPixels + "x" + heightPixels);
		return screenResolution;
	}

	/**
	 * �������SD��״̬
	 */

	private String InsideSDCardState() {
		String sd1;
		String prefix = getResources().getString(R.string.sdcardStatus);
		String state = Environment.getExternalStorageState();// ��ȡ��״̬
		// Environment.get
		sd1 = prefix + state;
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			sd1 = prefix + "����SD������";
		} else if (state.equals(Environment.MEDIA_REMOVED)) {
			sd1 = prefix + "����SD��������(" + state + ")";
		}
		// Log.d(TAG, "���ô洢��״̬��" + Environment.getExternalStorageState(new
		// File(file)));
		return sd1;
	}

	// ��ȡ����SD���Ĵ�С
	private String InsideSDCardSize() {
		// File path = Environment.getDataDirectory();
		String insideSDCardSize1;
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath());
		// Block�飬getBlockSize��ȡ��Ĵ�С��getBlockCount��ȡ�����������˾���SD���ܴ�С
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		insideSDCardSize1 = Formatter.formatFileSize(MainActivity.this, blockSize * totalBlocks);
		insideSDCardSize1 = "����SD������:" + insideSDCardSize1;
		return insideSDCardSize1;
	}

	private String OutsideSDCardSize() {
		// File path = Environment.getDataDirectory();
		String outsideSDCardSize1 = "";
		String file = getPath2();
		// File path = Environment.getExternalStorageDirectory();
		// String sdcard_path1=getPath2();
		try {
			StatFs stat = new StatFs(getPath2());
			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getBlockCount();
			outsideSDCardSize1 = Formatter.formatFileSize(MainActivity.this, blockSize * totalBlocks);
			Log.v("outsideSDCardSize1", outsideSDCardSize1);
			if (file != null) {

				outsideSDCardSize1 = "����SD������:" + outsideSDCardSize1;
			} else {
				outsideSDCardSize1 = "";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outsideSDCardSize1;
	}

	private String outsideSDCardState() {
		// ��ȡ����SD����·��
		String sd2;
		String file = getPath2();
		if (file != null) {
			sd2 = "����SD������";
		} else {
			sd2 = "����SD��������";
		}
		return sd2;
	}

	public String getPath2() {
		String sdcard_path = null;
		// ��ȡSD����·��
		String sd_default = Environment.getExternalStorageDirectory().getAbsolutePath();
		Log.d("text", sd_default);
		if (sd_default.endsWith("/")) {
			sd_default = sd_default.substring(0, sd_default.length() - 1);
		}
		// �õ�·��
		try {
			Runtime runtime = Runtime.getRuntime();
			Process proc = runtime.exec("mount");
			InputStream is = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			String line;
			BufferedReader br = new BufferedReader(isr);
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

	private String checkNetworkState() {
		// ��ȡ�������ӵķ���
		String checkNetworkState = null;
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		// ����״̬�Ƿ�����(���������Ϊ�գ����������磬���ҿ���ʹ�õĻ�)
		if (networkInfo != null && networkInfo.isAvailable()) {
			//��ȡ��������ͣ�����wifi���ƶ�����
			String typeName = networkInfo.getTypeName();
			//��ȡ����״̬
			State networkState = networkInfo.getState();
			//State.CONNECTED������������Ѿ�������
			if (networkState.equals(State.CONNECTED)) {
				checkNetworkState = getResources().getString(R.string.wifiStatus) + typeName + "������";
			//State.CONNECTING�����������������
			} else if (networkState.equals(State.CONNECTING)) {
				checkNetworkState = (getResources().getString(R.string.wifiStatus) + typeName + "������...");
			//State.DISCONNECTED:����������ѶϿ�
			} else if (networkState.equals(State.DISCONNECTED)) {
				checkNetworkState = (getResources().getString(R.string.wifiStatus) + typeName + "�ѶϿ�");
			//State.DISCONNECTING:������������ڶϿ���
			} else if (networkState.equals(State.DISCONNECTING)) {
				checkNetworkState = (getResources().getString(R.string.wifiStatus) + typeName + "���ڶϿ�...");
			//State.SUSPENDED:�����������ֹͣ
			} else if (networkState.equals(State.SUSPENDED)) {
				checkNetworkState = (getResources().getString(R.string.wifiStatus) + typeName + "��ֹͣ");
				
			} else if (networkState.equals(State.UNKNOWN)) {
				checkNetworkState = (getResources().getString(R.string.wifiStatus) + typeName + "δ֪");
			}

		} else {
			checkNetworkState = (getResources().getString(R.string.wifiStatus) + "���粻���ã�");
		}
		return checkNetworkState;
	}

	public String getScreenBrightness(Activity activity) {
		int nowBrightnessValue = 0;
		// ����
		ContentResolver resolver = activity.getContentResolver();
		try {
			nowBrightnessValue = android.provider.Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String nowBrightnessValue1 = "ϵͳ����:" + Integer.toString(nowBrightnessValue);
		return nowBrightnessValue1;
	}

	/**
	 * ��ȡ�������İ汾����(versionName)�Ͱ汾��(versionCode)
	 */
	private String getRadioAppInfo() {
		String radioName;
		String radioVersion;
		radioName = "������" + (version_name + getPackageInfoByPackageName(packageNameMap.get("radio")).versionName)
				+ "\t";
		radioVersion = (version_code + getPackageInfoByPackageName(packageNameMap.get("radio")).versionCode);

		return radioName + radioVersion;
	}

	/**
	 * ��ȡ���ֵİ汾����(versionName)�Ͱ汾��(versionCode)
	 */
	private String getMusiAppInfo() {
		String musicName;
		String musicVersion;
		musicName = "����"
				+ (version_name + getPackageInfoByPackageName(packageNameMap.get("music")).versionName + "\t\t");
		musicVersion = (version_code + getPackageInfoByPackageName(packageNameMap.get("music")).versionCode);

		return musicName + musicVersion;
	}

	/**
	 * ��ȡ���µİ汾����(versionName)�Ͱ汾��(versionCode)
	 */
	private String getUpdateAppInfo() {
		String updateName = null;
		String updateVersion = null;
		if (!packageNameMap.get("update").equals("")) {
			updateName = "����"
					+ (version_name + getPackageInfoByPackageName(packageNameMap.get("update")).versionName + "\t\t");
			updateVersion = (version_code + getPackageInfoByPackageName(packageNameMap.get("update")).versionCode);
		}
		return updateName + updateVersion;
	}

	private PackageInfo getPackageInfoByPackageName(String packageName) {
		PackageInfo packageInfo = null;
		try {
			packageInfo = getPackageManager().getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS);

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} finally {
			return packageInfo;
		}
	}

	/**
	 * ע��sd���㲥�¼�
	 */
	/*
	 * private void sdBroadcastReg() { IntentFilter filter = new IntentFilter();
	 * filter.addAction(Intent.ACTION_MEDIA_MOUNTED); filter.setPriority(888);
	 * filter.addAction(Intent.ACTION_MEDIA_CHECKING);
	 * filter.addAction(Intent.ACTION_MEDIA_REMOVED);
	 * filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
	 * filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
	 * filter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
	 * filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
	 * filter.addAction(Intent.ACTION_MEDIA_SHARED);
	 * filter.addDataScheme("file"); this.registerReceiver(sdCardBroadcast,
	 * filter); }
	 */
	/**
	 * SD��״̬���㲥
	 */
	/*
	 * private final BroadcastReceiver sdCardBroadcast = new BroadcastReceiver()
	 * { String prefix = "SD��״̬��";
	 * 
	 * @Override public void onReceive(Context context, Intent intent) { if
	 * (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
	 * tv_sdcardStatus.setText(prefix + "SD������ʹ��"); } else if
	 * (intent.getAction().equals(Intent.ACTION_MEDIA_CHECKING)) {
	 * tv_sdcardStatus.setText(prefix + "SD�����ڼ��..."); } else if
	 * (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED) ||
	 * intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED) ||
	 * intent.getAction().equals(Intent.ACTION_MEDIA_BAD_REMOVAL)) {
	 * tv_sdcardStatus.setText(prefix + "SD��������"); } else if
	 * (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {
	 * tv_sdcardStatus.setText(prefix + "SD����ʼɨ��..."); } else if
	 * (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
	 * tv_sdcardStatus.setText(prefix + "SD��ɨ�����"); } else if
	 * (intent.getAction().equals(Intent.ACTION_MEDIA_SHARED)) {
	 * tv_sdcardStatus.setText(prefix + "SD����ΪUSB�������洢������"); } } };
	 */

	/**
	 * ע������㲥�¼�
	 */
	private void netBroadcastReg() {
		IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
		this.registerReceiver(netBroadcast, filter);
	}

	/**
	 * ����״̬���㲥
	 */
	private final BroadcastReceiver netBroadcast = new BroadcastReceiver() {
		String action = "android.net.conn.CONNECTIVITY_CHANGE";

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "����״̬�ѷ����ı�");
			// intent.getAction()���չ㲥
			if (intent.getAction().equals(action)) {
				checkNetworkState();
			}
		}
	};

	/**
	 * ��ȡ�����汾��
	 */
	private void getAppInfo() {
		try {
			String cloudPhoto;
			cloudPhoto = (getPackageInfoByPackageName(packageNameMap.get("xiangpianbao")).versionName);
			// cloudPhoto = (version_name +
			// getPackageInfoByPackageName(packageNameMap.get("radio")).versionName);
			// Log.v("cloudPhoto", "��ʼ:"+cloudPhoto+":����");
			cloud_version.setText("�汾:" + cloudPhoto);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	/*
	 * private String getRadioAppInfo() { String radioName; String radioVersion;
	 * radioName = "������" + (version_name +
	 * getPackageInfoByPackageName(packageNameMap.get("radio")).versionName) +
	 * "\t"; radioVersion = (version_code +
	 * getPackageInfoByPackageName(packageNameMap.get("radio")).versionCode);
	 * 
	 * return radioName + radioVersion; }
	 */

}
