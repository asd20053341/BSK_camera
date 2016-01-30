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

	// SN号读取
	public native String ReadSn();
	// SN号写入
	public native String WriteSn(String buf); // 16
	//批次号读取
	public native String ReadBatch();
	//批次号写入
	public native String WriteBatch(String buf); // 16


	public native String WriteVersion(char value); 
	

	private Map<String, String> packageNameMap;

	private MediaPlayer mpMediaPlayer = null;
	// 红外广播数
	private int count;

	// 光感广播数
	private int lightCount;
	private String snValue;
	private String batchValue;
	private String uuidValue;
	private String version_name;
	private String version_code;

	// 云相册版本名称
	private TextView cloud_version;
	private TextView SN_read;
	private TextView barch_read;
	private TextView uuid_read;
	// 红外广播
	private TextView sendPIR_number;
	// 光感广播
	private TextView lightSensor_number;
	private Button btn_led;
	// SN号
	private Button snWriteBtn;
	// 批次号
	private Button batchWriteBtn;
	private Button uuidWriteBtn;
	// 一键写入SN，批次号，UUID
	private Button A_Key_To;
	private Context mContext;
	// 606值
	private EditText keyEt;
	// SN号
	private EditText snEt;
	// 批次号
	private EditText batchEt;

	private Button btn_snread;
	private Button btn_uuidread;
	private Button btn_batchread;
	private Button btn_refresh;
	private Button btn_redled;
	private Button btn_blueled;

	// 打开云相册
	private Button btn_CloudPhoto_open;
	private Button btn_radio_open;
	private Button btn_music_open;
	private Button btn_update_open;
	private Button btn_camera;
	private Button btn_settings;
	// 红外广播
	private Button btn_sendPIR;
	// 光感广播
	private Button btn_lightSensor;

	// 音乐播放与暂停
	private Button btn_musicplay;
	// 语音按钮
	private Button btn_recorder;
	// 语音文件保存路径
	private String fileName = null;
	// 语音操作对象
	private MediaPlayer mPlayer = null;
	private MediaRecorder mRecorder = null;

	// 提示灯
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
		getAppInfo();// 设置云相册的版本名称，版本号，android版本号，SDK版本号
		snWriteBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {

				String sn = ReadSn();

				if (TextUtils.isEmpty(sn)) {
					writingSn();
				} else {
					// 等于606时，调用writingSn();
					if (keyEt.getText().toString().equals(KEY)) {
						writingSn();
						Toast.makeText(mContext, "写入SN号成功！", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(mContext, "sn已存在，如需改写请输入正确的key!", Toast.LENGTH_SHORT).show();
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
						Toast.makeText(mContext, "写入批次号成功！", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(mContext, "batch已存在，如需改写请输入正确的key!", Toast.LENGTH_SHORT).show();
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
						Toast.makeText(mContext, "写入uuid成功！", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(mContext, "uuid已存在，如需改写请输入正确的key!", Toast.LENGTH_SHORT).show();
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
		 * barch_read.setText("batch为空!"); } else { barch_read.setText("读出：" +
		 * batch); } } }); btn_uuidread.setOnClickListener(new OnClickListener()
		 * {
		 * 
		 * @Override public void onClick(View v) { // TODO Auto-generated method
		 * stub String uuid = ReadUuid(); if (TextUtils.isEmpty(uuid)) {
		 * uuid_read.setText("uuid为空!"); } else { uuid_read.setText("读出：" +
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
		// checkSDCardState();//获取内外置SD卡是否正常
		netBroadcastReg();// 注册网络广播事件
		// sdBroadcastReg();//注册sd卡广播事件
		PIRBroadcastReg();// 注册红外感应广播广播事件

	}

	@Override
	protected void onResume() {
		super.onResume();
		lightSensorBroadcastReg();// 注册光感广播广播事件
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
			// 等于606时，调用writingSn();
			if (keyEt.getText().toString().equals(KEY)) {
				writingSn();
				Toast.makeText(mContext, "写入SN号成功！", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mContext, "sn已存在，如需改写请输入正确的key!", Toast.LENGTH_SHORT).show();
			}
		}

		if (TextUtils.isEmpty(batch)) {
			writingBatch();
		} else {
			if (keyEt.getText().toString().equals(KEY)) {
				writingBatch();
				Toast.makeText(mContext, "写入批次号成功！", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mContext, "batch已存在，如需改写请输入正确的key!", Toast.LENGTH_SHORT).show();
			}
		}

		if (TextUtils.isEmpty(uuid)) {
			writingUuid();
		} else {
			if (keyEt.getText().toString().equals(KEY)) {
				writingUuid();
				Toast.makeText(mContext, "写入uuid成功！", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mContext, "uuid已存在，如需改写请输入正确的key!", Toast.LENGTH_SHORT).show();
			}
		}

	}

	private void initData() {
		// 获取 版本名称 String
		version_name = getResources().getString(R.string.versionName);
		// 获取 版本号 String
		version_code = getResources().getString(R.string.versionCode);

		// 记得把包名放进来
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
			// 红外感应
			sendBroadcast(new Intent(BC_KEY));
			break;
		/*
		 * case R.id.btn_lightSensor: // 光感广播 sendBroadcast(new
		 * Intent(BC_LIGHT_SENSOR)); break;
		 */
		default:
			break;
		}
	}

	public void testMediaRecorder() {

		// 设置sdcard的路径

		// 获取sd卡的路径
		mRecorder = new MediaRecorder();
		fileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		// 在SD卡的路径下产生一个audiorecordtest.3gp
		fileName += "/audiorecordtest.3gp";
		// MediaRecorder类的对象，设置音频资源
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		// 设置输出文件格式
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		// 设置输出文件
		mRecorder.setOutputFile(fileName);
		// 设置音频编码
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		try {
			btn_recorder.setEnabled(false);
			mRecorder.prepare();
			mRecorder.start();
			Toast.makeText(mContext, "开始录音", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			Log.e("LOG_TAG", "prepare() failed");
		}

		handle.postDelayed(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(mContext, "录音结束", Toast.LENGTH_SHORT).show();
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
					// 找到文件路径
					mPlayer.setDataSource(fileName);
					// 准备
					Toast.makeText(mContext, "开始播放", Toast.LENGTH_SHORT).show();
					mPlayer.prepare();
					// 开始播放
					mPlayer.start();

				} catch (IOException e) {
					Log.e("LOG_TAG", "播放失败");
				}
			}

		}, 11000);
		handle.postDelayed(new Runnable() {
			public void run() {
				btn_recorder.setEnabled(true);
				Toast.makeText(mContext, "播放结束", Toast.LENGTH_SHORT).show();

			}
		}, 20000);

	}

	public void writingSn() {
		// 获取SN的EditText
		snValue = snEt.getText().toString();
		// 如果不为空
		if (!TextUtils.isEmpty(snValue)) {
			// 如果长度为16
			if (snValue.length() == 16) {
				// 把SN写进去，C++
				WriteSn(snValue);
			} else {
				Toast.makeText(mContext, "请输入16位sn号码!", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(mContext, "请输入sn号码!", Toast.LENGTH_SHORT).show();
		}
	}

	public void writingBatch() {
		batchValue = batchEt.getText().toString();
		if (!TextUtils.isEmpty(batchValue)) {
			if (batchValue.length() == 16) {
				WriteBatch(batchValue);
			} else {
				Toast.makeText(mContext, "请输入16位批次号!", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(mContext, "请输入批次号!", Toast.LENGTH_SHORT).show();
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
		// 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
		PackageInfo packageInfo = null;
		try {
			//getPackageManager()是获取一个PackageManager对象，
			//通过这个对象去获取包的信息getPackageInfo，packageName传进来的包名，
			//PackageManager.GET_CONFIGURATIONS获取packageName的配置
			packageInfo = getPackageManager().getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		if (packageInfo == null) {
			return;
		}

		// 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
		Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		//把包名传给Intent
		resolveIntent.setPackage(packageInfo.packageName);

		// 通过getPackageManager()的queryIntentActivities方法遍历
		//把包的配置遍历出来
		List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(resolveIntent,
				PackageManager.GET_CONFIGURATIONS);

		ResolveInfo resolveInfo = resolveInfoList.iterator().next();

		if (resolveInfo != null) {

			// packagename = 参数packname
			String mPackageName = resolveInfo.activityInfo.packageName;
			// 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
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
	 * 注册光感广播广播事件
	 */
	private void lightSensorBroadcastReg() {
		sm = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		ligthSensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT);
		sm.registerListener(new lightSensorBroadcast(), ligthSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	/**
	 * 定义光感广播事件
	 */
	public class lightSensorBroadcast implements SensorEventListener {
		// 获取监听到的广播

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub\
			float light = event.values[0];
			lightSensor_number.setText("光的强度: " + light);

		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}
	};

	/**
	 * 定义红外感应广播事件
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
	 * 注册红外感应广播广播事件
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

	// 读取SN号
	public String readSnStr() {
		String sn = ReadSn();

		if (TextUtils.isEmpty(sn)) {
			sn = "<h1><font color=\"red\">sn为空!</font></h1>";

		} else {
			// sn = ;
			sn = "<h1><font color=\"red\">SN号读出:" + sn + "</font></h1>";
		}

		return sn;
	}

	// 读取批次号
	public String readBatchStr() {
		String batch = ReadBatch();
		if (TextUtils.isEmpty(batch)) {
			batch = "<h1><font color=\"red\">批次号为空！</font></h1>";
		} else {
			batch = "<h1><font color=\"red\">批次号读出：" + batch + "</font></h1>";
		}
		return batch;
	}

	public String readUuidStr() {
		String uuid = ReadUuid();
		if (TextUtils.isEmpty(uuid)) {

			uuid = "<h1><font color=\"red\">uuid为空!</font></h1>";
		} else {
			uuid = "<h1><font color=\"red\">uuid读出：  " + uuid + "</font></h1>";
		}
		return uuid;

	}

	// 获取系统版本号，比如：4.4.2
	public String SystemVersion() {
		String systemVersion = "系统版本: " + android.os.Build.VERSION.RELEASE;
		// Log.v("banben", SystemVersion);
		return systemVersion;
	}

	// 获取SDK版本号，比如：19
	public String SDKVersion() {
		String sdkVersion = "SDK版本: " + android.os.Build.VERSION.SDK_INT;
		return sdkVersion;
	}

	// 获取固件版本号，比如：V2.1.0.01-D150828
	public String FirmwareVersion() {
		String firmwareVersion = "固件版本: " + SystemProperties.get("ro.product.firmware");
		return firmwareVersion;
	}

	public String SysLanguage() {
		String sysLanguage = (getResources().getString(R.string.sysLanguage)
				+ getResources().getConfiguration().locale.getDisplayLanguage());
		return sysLanguage;
	}

	public String ScreenDPI() {
		String screenDPI;
		WindowManager windowManager = this.getWindowManager();// 悬浮框
		DisplayMetrics metrics = new DisplayMetrics();// 屏幕尺寸
		windowManager.getDefaultDisplay().getMetrics(metrics);/// 将当前窗口的一些信息放在DisplayMetrics类中，

		// 当前屏幕的X,Y
		/*
		 * float xdpi = metrics.xdpi; float ydpi = metrics.ydpi;
		 */
		//// 获取SPI值
		int densityDpi = metrics.densityDpi;
		// 设置SPI值
		screenDPI = (getResources().getString(R.string.dpi) + densityDpi);
		return screenDPI;
	}

	public String ScreenResolution() {
		String screenResolution;
		// 新建一个窗口机制，跟Activity不同，这个窗口可以不显示出来
		WindowManager windowManager = this.getWindowManager();// 悬浮框
		DisplayMetrics metrics = new DisplayMetrics();// 屏幕尺寸
		windowManager.getDefaultDisplay().getMetrics(metrics);// 将当前窗口的一些信息放在DisplayMetrics类中，
		
		// 获取屏幕分辨率
		int widthPixels = metrics.widthPixels;
		int heightPixels = metrics.heightPixels;

		// 设置屏幕分辨率
		screenResolution = (getResources().getString(R.string.screenDPI) + widthPixels + "x" + heightPixels);
		return screenResolution;
	}

	/**
	 * 检测内置SD卡状态
	 */

	private String InsideSDCardState() {
		String sd1;
		String prefix = getResources().getString(R.string.sdcardStatus);
		String state = Environment.getExternalStorageState();// 获取的状态
		// Environment.get
		sd1 = prefix + state;
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			sd1 = prefix + "内置SD卡正常";
		} else if (state.equals(Environment.MEDIA_REMOVED)) {
			sd1 = prefix + "内置SD卡不正常(" + state + ")";
		}
		// Log.d(TAG, "外置存储卡状态：" + Environment.getExternalStorageState(new
		// File(file)));
		return sd1;
	}

	// 获取内置SD卡的大小
	private String InsideSDCardSize() {
		// File path = Environment.getDataDirectory();
		String insideSDCardSize1;
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath());
		// Block块，getBlockSize获取块的大小，getBlockCount获取块的数量，相乘就是SD卡总大小
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		insideSDCardSize1 = Formatter.formatFileSize(MainActivity.this, blockSize * totalBlocks);
		insideSDCardSize1 = "内置SD卡容量:" + insideSDCardSize1;
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

				outsideSDCardSize1 = "外置SD卡容量:" + outsideSDCardSize1;
			} else {
				outsideSDCardSize1 = "";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outsideSDCardSize1;
	}

	private String outsideSDCardState() {
		// 获取外置SD卡的路径
		String sd2;
		String file = getPath2();
		if (file != null) {
			sd2 = "外置SD卡正常";
		} else {
			sd2 = "外置SD卡不存在";
		}
		return sd2;
	}

	public String getPath2() {
		String sdcard_path = null;
		// 获取SD卡的路径
		String sd_default = Environment.getExternalStorageDirectory().getAbsolutePath();
		Log.d("text", sd_default);
		if (sd_default.endsWith("/")) {
			sd_default = sd_default.substring(0, sd_default.length() - 1);
		}
		// 得到路径
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
		Log.d("text", "外置内存卡路径：" + sdcard_path);
		return sdcard_path;
	}

	private String checkNetworkState() {
		// 获取网络连接的服务
		String checkNetworkState = null;
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		// 网络状态是否联网(网络如果不为空，就是有网络，并且可以使用的话)
		if (networkInfo != null && networkInfo.isAvailable()) {
			//获取网络的类型，比如wifi或移动数据
			String typeName = networkInfo.getTypeName();
			//获取网络状态
			State networkState = networkInfo.getState();
			//State.CONNECTED：如果网络是已经连上了
			if (networkState.equals(State.CONNECTED)) {
				checkNetworkState = getResources().getString(R.string.wifiStatus) + typeName + "已连接";
			//State.CONNECTING：如果网络是连接中
			} else if (networkState.equals(State.CONNECTING)) {
				checkNetworkState = (getResources().getString(R.string.wifiStatus) + typeName + "连接中...");
			//State.DISCONNECTED:如果网络是已断开
			} else if (networkState.equals(State.DISCONNECTED)) {
				checkNetworkState = (getResources().getString(R.string.wifiStatus) + typeName + "已断开");
			//State.DISCONNECTING:如果网络是正在断开中
			} else if (networkState.equals(State.DISCONNECTING)) {
				checkNetworkState = (getResources().getString(R.string.wifiStatus) + typeName + "正在断开...");
			//State.SUSPENDED:如果网络是已停止
			} else if (networkState.equals(State.SUSPENDED)) {
				checkNetworkState = (getResources().getString(R.string.wifiStatus) + typeName + "已停止");
				
			} else if (networkState.equals(State.UNKNOWN)) {
				checkNetworkState = (getResources().getString(R.string.wifiStatus) + typeName + "未知");
			}

		} else {
			checkNetworkState = (getResources().getString(R.string.wifiStatus) + "网络不可用！");
		}
		return checkNetworkState;
	}

	public String getScreenBrightness(Activity activity) {
		int nowBrightnessValue = 0;
		// 数据
		ContentResolver resolver = activity.getContentResolver();
		try {
			nowBrightnessValue = android.provider.Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String nowBrightnessValue1 = "系统亮度:" + Integer.toString(nowBrightnessValue);
		return nowBrightnessValue1;
	}

	/**
	 * 获取收音机的版本名称(versionName)和版本号(versionCode)
	 */
	private String getRadioAppInfo() {
		String radioName;
		String radioVersion;
		radioName = "收音机" + (version_name + getPackageInfoByPackageName(packageNameMap.get("radio")).versionName)
				+ "\t";
		radioVersion = (version_code + getPackageInfoByPackageName(packageNameMap.get("radio")).versionCode);

		return radioName + radioVersion;
	}

	/**
	 * 获取音乐的版本名称(versionName)和版本号(versionCode)
	 */
	private String getMusiAppInfo() {
		String musicName;
		String musicVersion;
		musicName = "音乐"
				+ (version_name + getPackageInfoByPackageName(packageNameMap.get("music")).versionName + "\t\t");
		musicVersion = (version_code + getPackageInfoByPackageName(packageNameMap.get("music")).versionCode);

		return musicName + musicVersion;
	}

	/**
	 * 获取更新的版本名称(versionName)和版本号(versionCode)
	 */
	private String getUpdateAppInfo() {
		String updateName = null;
		String updateVersion = null;
		if (!packageNameMap.get("update").equals("")) {
			updateName = "升级"
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
	 * 注册sd卡广播事件
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
	 * SD卡状态监测广播
	 */
	/*
	 * private final BroadcastReceiver sdCardBroadcast = new BroadcastReceiver()
	 * { String prefix = "SD卡状态：";
	 * 
	 * @Override public void onReceive(Context context, Intent intent) { if
	 * (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
	 * tv_sdcardStatus.setText(prefix + "SD卡正常使用"); } else if
	 * (intent.getAction().equals(Intent.ACTION_MEDIA_CHECKING)) {
	 * tv_sdcardStatus.setText(prefix + "SD卡正在检查..."); } else if
	 * (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED) ||
	 * intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED) ||
	 * intent.getAction().equals(Intent.ACTION_MEDIA_BAD_REMOVAL)) {
	 * tv_sdcardStatus.setText(prefix + "SD卡不存在"); } else if
	 * (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {
	 * tv_sdcardStatus.setText(prefix + "SD卡开始扫描..."); } else if
	 * (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
	 * tv_sdcardStatus.setText(prefix + "SD卡扫描结束"); } else if
	 * (intent.getAction().equals(Intent.ACTION_MEDIA_SHARED)) {
	 * tv_sdcardStatus.setText(prefix + "SD卡作为USB大容量存储被共享"); } } };
	 */

	/**
	 * 注册网络广播事件
	 */
	private void netBroadcastReg() {
		IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
		this.registerReceiver(netBroadcast, filter);
	}

	/**
	 * 网络状态监测广播
	 */
	private final BroadcastReceiver netBroadcast = new BroadcastReceiver() {
		String action = "android.net.conn.CONNECTIVITY_CHANGE";

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "网络状态已发生改变");
			// intent.getAction()接收广播
			if (intent.getAction().equals(action)) {
				checkNetworkState();
			}
		}
	};

	/**
	 * 获取云相册版本号
	 */
	private void getAppInfo() {
		try {
			String cloudPhoto;
			cloudPhoto = (getPackageInfoByPackageName(packageNameMap.get("xiangpianbao")).versionName);
			// cloudPhoto = (version_name +
			// getPackageInfoByPackageName(packageNameMap.get("radio")).versionName);
			// Log.v("cloudPhoto", "开始:"+cloudPhoto+":结束");
			cloud_version.setText("版本:" + cloudPhoto);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	/*
	 * private String getRadioAppInfo() { String radioName; String radioVersion;
	 * radioName = "收音机" + (version_name +
	 * getPackageInfoByPackageName(packageNameMap.get("radio")).versionName) +
	 * "\t"; radioVersion = (version_code +
	 * getPackageInfoByPackageName(packageNameMap.get("radio")).versionCode);
	 * 
	 * return radioName + radioVersion; }
	 */

}
