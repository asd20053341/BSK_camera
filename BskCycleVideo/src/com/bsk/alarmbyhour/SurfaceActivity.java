package com.bsk.alarmbyhour;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class SurfaceActivity extends Activity implements SurfaceHolder.Callback {
	/** Called when the activity is first created. */
	static {
		System.loadLibrary("x804camerademo");
	}

	// 视频播放类
	MediaPlayer player;
	// 视频显示类
	SurfaceView surface;
	SurfaceHolder surfaceHolder;
	Button play, pause, stop;
	WakeLock m_wklk;
	
	public native String setDirection(String port, int num, String inout);

	public native String setValue(String port, int num, int value);

	// 储存从文件读出来的老化时间
	String time;
	int mhour;
	int mmin;
	int msecond;
	// 是否把时间写入sd卡的标志
	private boolean tag = true;
	// 是否进入黄灯线程的标志
	private boolean yTag = true;
	private static final String TAG = "CycleVideo";
	PrintWriter pw = null;
	FileWriter fw = null;
	private TextView txttime;

	Runnable runnable = null;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				// 写入结束时间
				endTime();

				// 结束黄灯闪烁线程
				yTag = false;

				// 关闭黄灯
				setDirection("PH", 18, "out");
				setValue("PH", 18, 0);
				// 蓝灯常亮
				blueLamp();
				// 退出APK
				System.exit(0);
				break;
			case 2:
				runnable = new Runnable() {
					@Override
					public void run() {
						ComputeTime();
						String strTime = mhour + "小时:" + mmin + "分钟:" + msecond + "秒";
						txttime.setText(strTime);
						Log.v("zzzzb", "在运行");
						handler.postDelayed(runnable, 1000);
					}
				};
				handler.postDelayed(runnable, 1000);
				break;

			case 3:
				handler.removeCallbacks(runnable);
				Log.v("zzzzb", "停止运行");
				txttime.setVisibility(View.GONE);
				break;
			}
		};
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);
		play = (Button) findViewById(R.id.button1);
		pause = (Button) findViewById(R.id.button2);
		stop = (Button) findViewById(R.id.button3);
		surface = (SurfaceView) findViewById(R.id.surface);
		txttime = (TextView) findViewById(R.id.txttime);

		surfaceHolder = surface.getHolder();// SurfaceHolder是SurfaceView的控制接口
		surfaceHolder.addCallback(this);// 因为这个类实现了SurfaceHolder.Callback接口，所以回调参数直接this
		// surfaceHolder.setFixedSize(320, 220);// 显示的分辨率,不设置为视频默认
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// Surface类型
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		m_wklk = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "cn");
		// 获取相应的锁
		m_wklk.acquire();
		// 一初始化就关闭蓝灯
		setDirection("PH", 19, "out");
		setValue("PH", 19, 0);

		play.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				player.start();
			}
		});

		pause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				player.pause();
			}
		});
		stop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				player.stop();
			}
		});
	}

	/**
	 * 倒计时计算
	 */
	private void ComputeTime() {
		msecond--;
		if (msecond < 0) {
			mmin--;
			msecond = 59;
			if (mmin < 0) {
				mmin = 59;
				mhour--;
				if (mhour < 0) {
					handler.sendEmptyMessage(2);
				}
			}

		}

	}

	/**
	 * 启动老化定时器
	 */
	private void startVideo() {
		time = readTime();
		if (time.equals("") || time.equals(null)) {
			Log.v(TAG, "没有输入老化时间或文件不存在！");
			System.exit(0);
		} else {
			int time1 = 0;
			try {
				time1 = Integer.parseInt(time);
			} catch (Exception e) {
				Log.v(TAG, "时间输入错误！");
			}
			// 开始播放
			player.start();
			// 开始倒计时
			countDown();
			// 老化过程中，黄灯闪烁
			yellowLamp();
			// 开始时间写入到Data
			startTime();
			handler.sendEmptyMessageDelayed(1, time1 * 60 * 1000);
			// handler.sendEmptyMessageDelayed(1, 20 * 1000);
		}
	}

	/**
	 * 开始倒计时
	 */
	private void countDown() {
		long ms = Long.parseLong(time);
		long ms2 = ms * 60 * 1000;
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");// 初始化Formatter的转换格式。
		formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
		String hms = formatter.format(ms2);
		String[] my = hms.split(":");
		mhour = Integer.parseInt(my[0]);
		mmin = Integer.parseInt(my[1]);
		msecond = Integer.parseInt(my[2]);
		handler.sendEmptyMessage(2);
	}

	/**
	 * 读取需要老化的时间，分钟
	 * 
	 * @return
	 */
	private String readTime() {
		StringBuffer sb = null;
		try {
			// File file = new File("/mnt/extsd/", "readTime.txt");
			File file = new File(getPath2(), "x431-avoid-setup/preinstall/readTime.txt");
			Log.v(TAG, "file:" + file);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String readline = "";
			sb = new StringBuffer();
			while ((readline = br.readLine()) != null) {
				Log.v(TAG, "readline:" + readline);
				sb.append(readline);
			}
			br.close();
			Log.v(TAG, "sb.toString():" + sb.toString());
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * 获取外置sd卡路径
	 * 
	 * @return
	 */
	public String getPath2() {
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

	/**
	 * 开始时间写入到data
	 */
	private void startTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		// str的格式就是，比如:2015年10月27日
		String str = formatter.format(curDate);
		Log.v(TAG, "str:" + str);
		writeData("开始老化时间:", str, null);
	}

	/**
	 * 结束时间写入到data
	 */
	private void endTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		// str的格式就是，比如:2015年10月27日
		String str = formatter.format(curDate);

		writeData("结束老化时间:", str, "OK");
	}

	/**
	 * 写入时间到data
	 * 
	 * @param tag
	 * @param str
	 */
	private void writeData(String str, String time, String tag) {
		// 这里创建路径为：
		// sd卡的路径/test.txt；后面有一个true，代表如果这个test.txt一直存在的话，以后的内容就直接追加到test.txt
		// 内容的后面，不会覆盖；如果没加true，就代表覆盖
		try {
			if (tag == null) {
				fw = new FileWriter(Environment.getExternalStorageDirectory() + "/" + "time.txt");
			} else {
				fw = new FileWriter(Environment.getExternalStorageDirectory() + "/" + "time.txt", true);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// new一个写的对象
		pw = new PrintWriter(fw);
		// 写入“V/BSK”，然后换行
		if (tag == null) {
			pw.println(str + "     " + time + "\r\n");
		} else {
			pw.println(str + "     " + time + "     " + tag);
		}
		// 写完后关闭
		pw.close();
	}

	class CompletionListener implements OnCompletionListener {
		// 播放完监听事件，播放完后自动执行该方法
		@Override
		public void onCompletion(MediaPlayer mp) {
			// TODO Auto-generated method stub
			Log.v(TAG, "CompletionListener");
			// 让surface隐藏再显示（重新调用了surfaceCreated），不然有些机型第二次播放时显示不出来画面
			if (!surface.isLayoutRequested())
				surface.requestLayout();

			surface.setVisibility(View.INVISIBLE);
			surface.setVisibility(View.VISIBLE);

		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// 必须在surface创建后才能初始化MediaPlayer,否则不会显示图像
		player = new MediaPlayer();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setDisplay(surfaceHolder);
		// player.setLooping(true);
		// 播放完监听事件
		player.setOnCompletionListener(new CompletionListener());
		// 设置显示视频显示在SurfaceView上
		try {
			// player.setDataSource(Environment.getExternalStorageDirectory() +
			// "/" + "aaa.mp4");
			player.setDataSource(this, Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.bbb));
			player.prepare();

			// 开始播放
			if (tag) {
				startVideo();
				tag = false;
			} else {
				player.start();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * 老化过程中，黄灯闪烁
	 */
	public void yellowLamp() {
		yTag = true;
		new Thread() {
			@Override
			public void run() {
				setDirection("PH", 18, "out");
				while (yTag) {
					// TODO Auto-generated method stub
					setValue("PH", 18, 1);
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					setValue("PH", 18, 0);
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		System.exit(0);
	}

	/**
	 * 老化完成，蓝灯常亮
	 */
	public void blueLamp() {
		setDirection("PH", 19, "out");
		setValue("PH", 19, 1);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (player.isPlaying()) {
			player.stop();
		}
		player.release();
		// Activity销毁时停止播放，释放资源。不做这个操作，即使退出还是能听到视频播放的声音
		try {
			if (m_wklk != null && m_wklk.isHeld()) {
				m_wklk.release();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}