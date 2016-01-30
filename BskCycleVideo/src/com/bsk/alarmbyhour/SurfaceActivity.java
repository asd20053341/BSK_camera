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

	// ��Ƶ������
	MediaPlayer player;
	// ��Ƶ��ʾ��
	SurfaceView surface;
	SurfaceHolder surfaceHolder;
	Button play, pause, stop;
	WakeLock m_wklk;
	
	public native String setDirection(String port, int num, String inout);

	public native String setValue(String port, int num, int value);

	// ������ļ����������ϻ�ʱ��
	String time;
	int mhour;
	int mmin;
	int msecond;
	// �Ƿ��ʱ��д��sd���ı�־
	private boolean tag = true;
	// �Ƿ����Ƶ��̵߳ı�־
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
				// д�����ʱ��
				endTime();

				// �����Ƶ���˸�߳�
				yTag = false;

				// �رջƵ�
				setDirection("PH", 18, "out");
				setValue("PH", 18, 0);
				// ���Ƴ���
				blueLamp();
				// �˳�APK
				System.exit(0);
				break;
			case 2:
				runnable = new Runnable() {
					@Override
					public void run() {
						ComputeTime();
						String strTime = mhour + "Сʱ:" + mmin + "����:" + msecond + "��";
						txttime.setText(strTime);
						Log.v("zzzzb", "������");
						handler.postDelayed(runnable, 1000);
					}
				};
				handler.postDelayed(runnable, 1000);
				break;

			case 3:
				handler.removeCallbacks(runnable);
				Log.v("zzzzb", "ֹͣ����");
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

		surfaceHolder = surface.getHolder();// SurfaceHolder��SurfaceView�Ŀ��ƽӿ�
		surfaceHolder.addCallback(this);// ��Ϊ�����ʵ����SurfaceHolder.Callback�ӿڣ����Իص�����ֱ��this
		// surfaceHolder.setFixedSize(320, 220);// ��ʾ�ķֱ���,������Ϊ��ƵĬ��
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// Surface����
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		m_wklk = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "cn");
		// ��ȡ��Ӧ����
		m_wklk.acquire();
		// һ��ʼ���͹ر�����
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
	 * ����ʱ����
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
	 * �����ϻ���ʱ��
	 */
	private void startVideo() {
		time = readTime();
		if (time.equals("") || time.equals(null)) {
			Log.v(TAG, "û�������ϻ�ʱ����ļ������ڣ�");
			System.exit(0);
		} else {
			int time1 = 0;
			try {
				time1 = Integer.parseInt(time);
			} catch (Exception e) {
				Log.v(TAG, "ʱ���������");
			}
			// ��ʼ����
			player.start();
			// ��ʼ����ʱ
			countDown();
			// �ϻ������У��Ƶ���˸
			yellowLamp();
			// ��ʼʱ��д�뵽Data
			startTime();
			handler.sendEmptyMessageDelayed(1, time1 * 60 * 1000);
			// handler.sendEmptyMessageDelayed(1, 20 * 1000);
		}
	}

	/**
	 * ��ʼ����ʱ
	 */
	private void countDown() {
		long ms = Long.parseLong(time);
		long ms2 = ms * 60 * 1000;
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");// ��ʼ��Formatter��ת����ʽ��
		formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
		String hms = formatter.format(ms2);
		String[] my = hms.split(":");
		mhour = Integer.parseInt(my[0]);
		mmin = Integer.parseInt(my[1]);
		msecond = Integer.parseInt(my[2]);
		handler.sendEmptyMessage(2);
	}

	/**
	 * ��ȡ��Ҫ�ϻ���ʱ�䣬����
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
	 * ��ȡ����sd��·��
	 * 
	 * @return
	 */
	public String getPath2() {
		String sdcard_path = null;
		// ��ȡSD����·��
		String sd_default = Environment.getExternalStorageDirectory().getAbsolutePath();
		// Log.d("text", sd_default);
		// ����ֻ�ǰ������ġ�/��ȥ���˶���
		if (sd_default.endsWith("/")) {
			sd_default = sd_default.substring(0, sd_default.length() - 1);
		}
		// �õ�·��
		try {
			// Ӧ�ó���ͨ��Runtime������ʱ����������������Ի�ȡ���е���Ϣ�������ڴ棬CPU
			Runtime runtime = Runtime.getRuntime();
			// runtime.exec("mount");��˵��ִ�����mount����
			Process proc = runtime.exec("mount");
			// ��ִ��������Ľ�����������InputStream
			InputStream is = proc.getInputStream();
			// ��һ�����������ȥ��
			InputStreamReader isr = new InputStreamReader(is);
			String line;
			// �û���������
			BufferedReader br = new BufferedReader(isr);
			// ֱ������Ϊֹ
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

	/**
	 * ��ʼʱ��д�뵽data
	 */
	private void startTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy��MM��dd�� HH:mm:ss ");
		Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
		// str�ĸ�ʽ���ǣ�����:2015��10��27��
		String str = formatter.format(curDate);
		Log.v(TAG, "str:" + str);
		writeData("��ʼ�ϻ�ʱ��:", str, null);
	}

	/**
	 * ����ʱ��д�뵽data
	 */
	private void endTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy��MM��dd�� HH:mm:ss ");
		Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
		// str�ĸ�ʽ���ǣ�����:2015��10��27��
		String str = formatter.format(curDate);

		writeData("�����ϻ�ʱ��:", str, "OK");
	}

	/**
	 * д��ʱ�䵽data
	 * 
	 * @param tag
	 * @param str
	 */
	private void writeData(String str, String time, String tag) {
		// ���ﴴ��·��Ϊ��
		// sd����·��/test.txt��������һ��true������������test.txtһֱ���ڵĻ����Ժ�����ݾ�ֱ��׷�ӵ�test.txt
		// ���ݵĺ��棬���Ḳ�ǣ����û��true���ʹ�����
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
		// newһ��д�Ķ���
		pw = new PrintWriter(fw);
		// д�롰V/BSK����Ȼ����
		if (tag == null) {
			pw.println(str + "     " + time + "\r\n");
		} else {
			pw.println(str + "     " + time + "     " + tag);
		}
		// д���ر�
		pw.close();
	}

	class CompletionListener implements OnCompletionListener {
		// ����������¼�����������Զ�ִ�и÷���
		@Override
		public void onCompletion(MediaPlayer mp) {
			// TODO Auto-generated method stub
			Log.v(TAG, "CompletionListener");
			// ��surface��������ʾ�����µ�����surfaceCreated������Ȼ��Щ���͵ڶ��β���ʱ��ʾ����������
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
		// ������surface��������ܳ�ʼ��MediaPlayer,���򲻻���ʾͼ��
		player = new MediaPlayer();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setDisplay(surfaceHolder);
		// player.setLooping(true);
		// ����������¼�
		player.setOnCompletionListener(new CompletionListener());
		// ������ʾ��Ƶ��ʾ��SurfaceView��
		try {
			// player.setDataSource(Environment.getExternalStorageDirectory() +
			// "/" + "aaa.mp4");
			player.setDataSource(this, Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.bbb));
			player.prepare();

			// ��ʼ����
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
	 * �ϻ������У��Ƶ���˸
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
	 * �ϻ���ɣ����Ƴ���
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
		// Activity����ʱֹͣ���ţ��ͷ���Դ�����������������ʹ�˳�������������Ƶ���ŵ�����
		try {
			if (m_wklk != null && m_wklk.isHeld()) {
				m_wklk.release();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}