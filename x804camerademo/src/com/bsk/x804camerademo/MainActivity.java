package com.bsk.x804camerademo;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class MainActivity extends Activity {
	public native String setDirection(String port, int num, String inout);

	public native String setValue(String port, int num, int value);

	public native int getValue(String port, int num);

	public native int cameraMode(int mode, int value);

	private static final int daylight_mode = 0;
	private static final int night_mode = 1;
	private int ircut_pull_for_light = 0;
	private int ircut_pull_for_night = 0;
	private Button btn1_start = null;
	private int ls_det;
	SimpleDateFormat formatter = null;
	SimpleDateFormat formatter1 = null;
	Date curDate = null;
	Date curDate1 = null;
	boolean cycle = false;
	MyThread myThread = null;
	MyThread1 myThread1 = null;
	Thread thread = null;
	WakeLock m_wklk;

	static {
		System.loadLibrary("x804camerademo");
	}

	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btn1_start = (Button) findViewById(R.id.btn_start);
		setDirection("PA", 17, "in");
		setDirection("PB", 0, "out");
		setDirection("PB", 4, "out");
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		m_wklk = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "cn");
		// 获取相应的锁
		m_wklk.acquire();

		btn1_start.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				cycle = true;
				myThread1 = new MyThread1();
				thread = new Thread(myThread1);
				thread.start();
				btn1_start.setEnabled(false);
				Intent intent = new Intent();
				intent.setAction("android.media.action.STILL_IMAGE_CAMERA");
				startActivity(intent);

				// btn1_start.setClickable(false);
			}
		});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		cycle = false;
		try {

			if (m_wklk != null && m_wklk.isHeld()) {
				m_wklk.release();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	class MyThread1 implements Runnable {
		public void run() {
			while (cycle) {
				setValue("PB", 4, 1);
				// SystemClock.sleep(100);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				setValue("PB", 4, 0);
				cameraMode(night_mode, 0);

				formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
				curDate = new Date(System.currentTimeMillis());
				// 获取当前时间
				String str = formatter.format(curDate);
				System.out.println(str);

				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				setValue("PB", 0, 1);
				// SystemClock.sleep(100);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				setValue("PB", 0, 0);
				cameraMode(daylight_mode, 1);

				formatter1 = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
				curDate1 = new Date(System.currentTimeMillis());// 获取当前时间
				String str1 = formatter.format(curDate1);
				System.out.println(str1);

				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

	}

	class MyThread implements Runnable {
		public void run() {
			while (cycle) {
				formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
				curDate = new Date(System.currentTimeMillis());// 获取当前时间
				String str = formatter.format(curDate);
				System.out.println(str);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				formatter1 = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
				curDate1 = new Date(System.currentTimeMillis());// 获取当前时间
				String str1 = formatter.format(curDate1);
				System.out.println(str1);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

	}

	public void a() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (true) {
					ls_det = getValue("PA", 17);
					if (1 == ls_det) {
						if (0 == ircut_pull_for_night) {
							Log.d("CAMERA_LOG", "night mode.");
							ircut_pull_for_night = 1;
							ircut_pull_for_light = 0;
							setValue("PB", 4, 1);
							// SystemClock.sleep(100);
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							setValue("PB", 4, 0);
						}
						cameraMode(night_mode, 0);
					} else {
						if (0 == ircut_pull_for_light) {
							Log.d("CAMERA_LOG", "light mode.");
							ircut_pull_for_light = 1;
							ircut_pull_for_night = 0;
							setValue("PB", 0, 1);
							// SystemClock.sleep(100);
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							setValue("PB", 0, 0);
						}
						cameraMode(daylight_mode, 1);
					}

					// SystemClock.sleep(5000);
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
}
