package com.bsk.fmradio;

import com.bsk.fmradio.*;
import java.security.PublicKey;
import java.util.Random;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public native String OpenDev();

	public native String CloseDev();

	public native String Tune(int oper);

	public native String AutoSeekUp();

	public native String AutoSeekDown();

	public native String GetFreq();

	public native String SetGain(int gain_value);

	public native String GetGain();

	public native String PlayChannel(int frequency);

	String currFreq;
	String gainCfg;
	//音频管理器
	AudioManager mAudioManager;
	private WakeLock mWakelock;

	private TextView currFreqTv;

	private Button incTuneBtn;
	private Button decTuneBtn;
	private Button seekUpBtn;
	private Button seekDownBtn;
	private Button powerBtn;

	private Button favorBtn;

	private ListView freqListView; // 频道列表
	private ListView favorListView; // 收藏列表

	private DatabaseHelper mOpenHelper;
	private SQLiteDatabase mDb;

	private Handler mHandler;
	private SimpleCursorAdapter listAdapter;
	private SimpleCursorAdapter favorAdapter;
	private boolean isSeek; // 是否搜台中
	private boolean isOpenDev; // 是否打开设备,isOpenDev的值默认为false

	private int currVolume;
	
	private ImageView arrowIv;

	private Animation rotateAnim;

	private LinearLayout tabLy;
	private Button favoriteBtn; // 收藏标签
	private Button listBtn; // 列表标签
	private Button seekFreqBtn; // 频道搜索按钮

	public int currAngle; // 圆圈旋转的当前角度

	public boolean isFavored; // 当前频道是否已收藏

	private IntentFilter intentFilter;
	private BroadcastReceiver localReceiver;

	private int isSoundEffect; // 是否打开了音效

	private Context mContext;
	private Resources res;

	// 自己定义的一个广播
	private final String BC_CLOSE_FM = "com.bsk.fmradio.CLOSE_FM";
	// private final String SYSTEM_HOME_KEY = "PPFRAM_HOME_PRESSED_UP";

	static {
		System.loadLibrary("fmradio");
	}

	// 耳机插拔广播，自定义FM_ACTION = "com.anl.wxb.settings.broadcast.WxbKeyDownReceiver"广播
	private final BroadcastReceiver headSetReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			Log.d("耳机广播", "into headsetreceiver!");
			String action = intent.getAction();

			if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
				currVolume = mAudioManager.getStreamVolume(10);
				// headphone plugged
				//如果state没有值传过来，那么默认值为0
				if (intent.getIntExtra("state", 0) == 1) {
					// do something
					Log.d("耳机广播", "this is headphone plugged");

					new Thread(new Runnable() {
						@Override
						public void run() {
							 //设置一个音频硬件数量可变的参数值
							mAudioManager.setParameters("routing=4");
							//直接设置手机的指定类型的音量值
							mAudioManager.setStreamVolume(10, currVolume, 0);
							//设置声音模式。可设置的值有   NORMAL,RINGTONE, 和IN_CALL。
							mAudioManager.setMode(5);
						}
					}).start();

					// audioManager.setMicrophoneMute(false);
					// audioManager.setSpeakerphoneOn(false);

					// headphone unplugged
				} else {
					// do something
					Log.d("耳机广播", "this is headphone unplugged");

					// audioManager.setMicrophoneMute(true);
					// audioManager.setSpeakerphoneOn(true);

					new Thread(new Runnable() {
						@Override
						public void run() {
							mAudioManager.setParameters("routing=2");
							mAudioManager.setStreamVolume(10, currVolume, 0);
							mAudioManager.setMode(5);
						}
					}).start();

					// audioManager.setMode(AudioManager.MODE_NORMAL);

				}

				//FM_ACTION = "com.anl.wxb.settings.broadcast.WxbKeyDownReceiver"
			} else if (action.equals(Constants.FM_ACTION)) {
				Log.e("", "btn=" + intent.getStringExtra(Constants.FM_ACTION_DATA_KEY));
				if (intent.getStringExtra(Constants.FM_ACTION_DATA_KEY).equals(Constants.FM_BTN_LEFT)) {
					//isOpenDev为true时为打开了设备
					if (isOpenDev) {
						currFreq = Tune(1);
						//currFreqTv就是频道，如108.0
						currFreqTv.setText(formatFreq(currFreq));
					}

				} else if (intent.getStringExtra(Constants.FM_ACTION_DATA_KEY).equals(Constants.FM_BTN_RIGHT)) {
					//isOpenDev为true时为打开了设备
					if (isOpenDev) {
						currFreq = Tune(2);
						currFreqTv.setText(formatFreq(currFreq));

					}
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;
		res = getResources();

		// 获取了音量键的值，处于关闭还是开启，存放到了isSoundEffect
		getSoundEffect();

		// 注册自定义BC_CLOSE_FM = "com.bsk.fmradio.CLOSE_FM"广播，直接写在了onCreate（）里
		IntentFilter intentFilter = new IntentFilter();	
		intentFilter.addAction(BC_CLOSE_FM);
		registerReceiver(mSystemKeyEventReceiver, intentFilter);

		/*
		 * IntentFilter filter = new IntentFilter();
		 * filter.addAction(SYSTEM_HOME_KEY);
		 * registerReceiver(mSystemKeyEventReceiver, filter);
		 */

		tabLy = (LinearLayout) findViewById(R.id.tab_ly);
		favoriteBtn = (Button) findViewById(R.id.favorite_btn);
		listBtn = (Button) findViewById(R.id.list_btn);
		seekFreqBtn = (Button) findViewById(R.id.search_btn);

		currFreqTv = (TextView) findViewById(R.id.freq_tv);

		freqListView = (ListView) findViewById(R.id.freq_listview);
		favorListView = (ListView) findViewById(R.id.favor_listview);

		incTuneBtn = (Button) findViewById(R.id.inc_btn);
		decTuneBtn = (Button) findViewById(R.id.dec_btn);
		powerBtn = (Button) findViewById(R.id.power_btn);
		seekUpBtn = (Button) findViewById(R.id.up_btn);
		seekDownBtn = (Button) findViewById(R.id.down_btn);

		favorBtn = (Button) findViewById(R.id.favor_btn);

		arrowIv = (ImageView) findViewById(R.id.arrow_iv);

		rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
		LinearInterpolator lin = new LinearInterpolator();
		rotateAnim.setInterpolator(lin);
		rotateAnim.setFillAfter(true);
		rotateAnim.setFillBefore(true);

		//收藏按钮
		favoriteBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// 重新设置背景图片
				tabLy.setBackgroundDrawable(res.getDrawable(R.drawable.favorite_bg));
				// 设置favoriteBtn按钮字的颜色
				favoriteBtn.setTextColor(res.getColor(R.color.blue));
				// 设置listBtn按钮字的颜色
				listBtn.setTextColor(res.getColor(R.color.black));
				// 设置favoriteBtn按钮的下面的列表可见
				favorListView.setVisibility(View.VISIBLE);
				// 设置listBtn按钮的下面的列表不可见
				freqListView.setVisibility(View.GONE);
			}
		});

		//列表按钮（频道搜索）
		listBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showFreqList();
			}
		});

		//爱心按钮
		favorBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				System.out.println();
				// isOpenDev的值第一次为默认为false,进不去
				if (isOpenDev) {

					// String freqStr = String.valueOf(Integer.parseInt(freq)*62
					// + Integer.parseInt(freq)/2);
					// String freqTitle = formatFreq(freqStr) + " MHz";
					// int freq = (int)(Float.parseFloat(currFreq) * 1000000 /
					// 62.5);
					// String freqStr = String.valueOf(freq*62 + freq/2);

					// String currFreq;
					int freq = (int) (Integer.parseInt(currFreq) / 62.5);
					String freqTitle = formatFreq(currFreq) + " MHz";

					Log.e("", "isFavored=" + isFavored);
					if (isFavored) { // 已经收藏

						// String sql = "DELETE FROM " + Constants.TABLE_FAVOR
						// +";";
						// mDb.execSQL(sql);
						mDb.delete(Constants.TABLE_FAVOR, Constants.COLUMN_FREQUENCY + "=?",
								new String[] { String.valueOf(freq) });

						Message msg = mHandler.obtainMessage();
						msg.what = 2;
						mHandler.sendMessage(msg);

						isFavored = false;
						favorBtn.setBackgroundResource(R.drawable.favor);

						Toast.makeText(mContext, "已取消收藏", Toast.LENGTH_SHORT).show();

					} else { // 未收藏

						// 查找，写入数据库并更新列表
						Cursor cursor = mDb.query(Constants.TABLE_FAVOR,
								new String[] { Constants.COLUMN_FREQUENCY, Constants.COLUMN_TITLE },
								Constants.COLUMN_FREQUENCY + "=" + freq, null, null, null, null);

						if (cursor != null && cursor.getCount() == 0) {
							saveFavor(freq, freqTitle);

							Message msg = mHandler.obtainMessage();
							msg.what = 2;
							mHandler.sendMessage(msg);

							isFavored = true;
							favorBtn.setBackgroundResource(R.drawable.favored);

							Toast.makeText(mContext, "已添加到收藏列表", Toast.LENGTH_SHORT).show();
						}

					}
				}
			}
		});

		// 注册耳机插拔广播
		IntentFilter filter1 = new IntentFilter();
		filter1.addAction(Intent.ACTION_HEADSET_PLUG);
		// 注册FM_ACTION = "com.anl.wxb.settings.broadcast.WxbKeyDownReceiver"广播
		filter1.addAction(Constants.FM_ACTION);
		registerReceiver(headSetReceiver, filter1);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		// 是否打开设备
		isOpenDev = false;
		// 是否搜台中
		isSeek = false;

		mOpenHelper = new DatabaseHelper(this);
		mDb = mOpenHelper.getWritableDatabase();

		// this.getMainLooper()表示在主线程处理
		mHandler = new Handler(this.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case 0:
					// 从数据库获取 搜索 的数据
					getFreqData();
					listAdapter.notifyDataSetChanged();
					break;

				case 1: // 搜台结束

					isSeek = false;
					// seekFreqBtn.setBackground(getResources().getDrawable(R.drawable.seek_freq_btn_selector));
					// 按钮，freq_search=频道搜索
					seekFreqBtn.setText(res.getString(R.string.freq_search));

					// TextView，currFreqTv=108.0
					currFreqTv.setText(formatFreq(currFreq));
					mAudioManager.setStreamVolume(10, currVolume, 0);

					// ImageView，清除动画，这个arrowIv就是那个转圈的
					arrowIv.clearAnimation();
					break;

				case 2:
					// 从数据库获取 收藏 的数据，并显示出来
					getFavorData();
					// 刷新适配器
					favorAdapter.notifyDataSetChanged();
					break;

				case 5: // 搜上一台下一台结束
					// TextView，currFreqTv=108.0,formatFreq(currFreq)截取字符串
					currFreqTv.setText(formatFreq(currFreq));
					// 全局数据，保存currFreq
					saveChannel(currFreq);
					// 显示的列表
					selectListFreq(freqListView);
					selectListFreq(favorListView);
					isSeek = false;
					break;

				case 8:

					break;
				case 9: // 收音机硬件故障
					seekFreqBtn.setText(res.getString(R.string.freq_search));
					Toast.makeText(mContext, "收音机硬件故障", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		};

		//列表按钮的listView
		freqListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (isOpenDev) {

					//
					rotateAngle(1);

					int frequency = (int) id;
					Log.e("", "play freq=" + frequency);
					PlayChannel(frequency);

					// currFreqTv.setText(formatFreq(String.valueOf(frequency*62+frequency/2)));
					// currFreq = String.valueOf(frequency*62+frequency/2);

					currFreq = String.valueOf(frequency * 62 + frequency / 2);
					currFreqTv.setText(formatFreq(currFreq));

					saveChannel(currFreq);

					for (int i = 0; i < parent.getCount(); i++) {
						View v = parent.getChildAt(i);
						if (v != null) {
							v.setBackgroundColor(Color.TRANSPARENT);
						}
					}

					view.setBackgroundColor(getResources().getColor(R.color.dark_yellow));

					selectListFreq(freqListView);

				}
			}
		});

		freqListView.setOnScrollListener(new OnScrollListenerImple());

		favorListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (isOpenDev) {

					//1是换台，同时执行动画
					rotateAngle(1);

					int frequency = (int) id;
					Log.e("", "play freq=" + frequency);
					PlayChannel(frequency);

					currFreq = String.valueOf(frequency * 62 + frequency / 2);

					currFreqTv.setText(formatFreq(currFreq));

					saveChannel(currFreq);

					for (int i = 0; i < parent.getCount(); i++) {
						View v = parent.getChildAt(i);
						if (v != null) {
							v.setBackgroundColor(Color.TRANSPARENT);
						}
					}

					view.setBackgroundColor(getResources().getColor(R.color.dark_yellow));

					isFavored = true;
					favorBtn.setBackgroundResource(R.drawable.favored);
				}
			}
		});

		favorListView.setOnScrollListener(new OnScrollListenerImple());

		incTuneBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isOpenDev) {

					rotateAngle(2);

					currFreq = Tune(1);
					currFreqTv.setText(formatFreq(currFreq));

					selectListFreq(freqListView);
					selectListFreq(favorListView);
				}
			}
		});

		decTuneBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isOpenDev) {

					rotateAngle(2);

					currFreq = Tune(2);
					currFreqTv.setText(formatFreq(currFreq));

					selectListFreq(freqListView);
					selectListFreq(favorListView);
				}
			}
		});

		//点击搜索按钮时执行该线程
		final Runnable seekRunnable = new Runnable() {
			@Override
			public void run() {

				// 清空数据库
				String sql = "DELETE FROM " + Constants.TABLE_FREQ + ";";
				mDb.execSQL(sql);

				Message msg = mHandler.obtainMessage();
				msg.what = 0;
				mHandler.sendMessage(msg);

				int frequency = 1400000;
				//搜索收音机的信号频道，jni来的
				PlayChannel(frequency);

				boolean isFirst = true;
				String firstFreq = "";
				int count = 0;

				//搜索完后，GetFreq获取到给freq，jni
				String freq = GetFreq();

				if (!TextUtils.isEmpty(freq) && !freq.equals("0")) { // 硬件问题，搜台返回0

					do {

						freq = GetFreq();

						String freqStr = String.valueOf(Integer.parseInt(freq) * 62 + Integer.parseInt(freq) / 2);
						String freqTitle = formatFreq(freqStr) + " MHz";

						currFreq = freqStr;

						if (isFirst) {
							firstFreq = freq;
							isFirst = false;
						} else {
							if (freq.equals(firstFreq)) {
								break;
							}
						}

						Log.e("", "freq=" + freq);
						frequency = Integer.parseInt(freq);

						count++;

						// 写入数据库并更新列表
						Cursor cursor = mDb.query(Constants.TABLE_FREQ,
								new String[] { Constants.COLUMN_FREQUENCY, Constants.COLUMN_TITLE },
								Constants.COLUMN_FREQUENCY + "=" + freq, null, null, null, null);

						if (cursor != null && cursor.getCount() == 0) {
							saveFreq(Integer.parseInt(freq), freqTitle);

							msg = mHandler.obtainMessage();
							msg.what = 0; // 更新列表
							mHandler.sendMessage(msg);
						}

					} while (isSeek && frequency > 1400000 && count <= 100); // 搜台超过100次停止

					msg = mHandler.obtainMessage();
					msg.what = 1; // 搜台结束
					mHandler.sendMessage(msg);

				} else { // 收音机硬件故障
					msg = mHandler.obtainMessage();
					msg.what = 9;
					mHandler.sendMessage(msg);
				}

			}
		};
		//搜索频道按钮
		seekFreqBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (isOpenDev) {
					// 显示频道列表
					favorListView.setVisibility(View.INVISIBLE);
					freqListView.setVisibility(View.VISIBLE);

					if (isSeek) { // 停止搜台
						isSeek = false;
						//seekFreqBtn.setBackground(getResources().getDrawable(R.drawable.seek_freq_btn_selector));
						seekFreqBtn.setText(res.getString(R.string.freq_search));
						mAudioManager.setStreamVolume(10, currVolume, 0);
						arrowIv.clearAnimation();

					} else { // 搜台
						//gainValue=3088
						int gainValue = (12 << 8) & 0xff00 | (1 << 4) & 0x00f0 | (0 << 0) & 0x000f;
						Log.e("", "gainValue=" + gainValue);
						// String gainCfg = SetGain(gainValue);
						// String gainCfg = GetGain();
						// Log.e("", "gainCfg=" + gainCfg);

						isSeek = true;
						// seekFreqBtn.setBackground(getResources().getDrawable(R.drawable.seek_freq_stop));
						seekFreqBtn.setText(res.getString(R.string.searching));
						currVolume = mAudioManager.getStreamVolume(10);
						mAudioManager.setStreamVolume(10, 0, 0);

						arrowIv.startAnimation(rotateAnim);

						new Thread(seekRunnable).start();

					}

				}
			}
		});

		seekUpBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (isOpenDev && !isSeek) {

					rotateAngle(1);

					isSeek = true;

					new Thread(new Runnable() {
						@Override
						public void run() {
							currFreq = AutoSeekDown();
							Log.e("", "freq=" + currFreq);

							Message msg = mHandler.obtainMessage();
							msg.what = 5;
							mHandler.sendMessage(msg);
						}

					}).start();

				}
			}
		});

		seekDownBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isOpenDev && !isSeek) {

					rotateAngle(1);

					isSeek = true;

					new Thread(new Runnable() {
						@Override
						public void run() {
							currFreq = AutoSeekUp();
							Log.e("", "freq=" + currFreq);

							Message msg = mHandler.obtainMessage();
							msg.what = 5;
							mHandler.sendMessage(msg);
						}

					}).start();

				}

			}
		});

		//开关按钮
		powerBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// mWakelock.release();
				// Toast.makeText(MainActivity.this, "�ͷ���", 0).show();

				//第一次为false，所以进不去,进的去的话，说明isOpenDev为true，是打开了设备的，那么就应该是要关闭了
				if (isOpenDev) {
					//设置音频模式
					mAudioManager.setMode(AudioManager.MODE_NORMAL);
					CloseDev();
					// mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,
					// 0, 0);
					// unregisterReceiver(headSetReceiver);
					// System.exit(0);

					isOpenDev = false;
					isSeek = false;

					arrowIv.clearAnimation();

					powerBtn.setBackground(getResources().getDrawable(R.drawable.power_off));

				} else {
					//系统音效是否打开了,等于0估计是没打开，1估计是打开，在oncreate时就过去了系统音效的状态，并存到了isSoundEffect里
					if (isSoundEffect == 0) {
						//如果没打开，就设为1，表示打开了
						setSoundEffect(1);
					}
					//设置音频模式
					mAudioManager.setMode(5);
					//打开收音机，jni
					OpenDev();
					// seekFreq();
					//currFreq为比如108.0
					if (!TextUtils.isEmpty(currFreq)) {
						PlayChannel((int) (Integer.parseInt(currFreq) / 62.5));

						currFreqTv.setText(formatFreq(currFreq));
					}
					isOpenDev = true;
					powerBtn.setBackground(getResources().getDrawable(R.drawable.power_on));

					int gain_value = (12 << 8) & 0xff00 | (1 << 4) & 0x00f0 | (0 << 0) & 0x000f;
					Log.e("", "gain_value=" + gain_value);
					SetGain(gain_value);

					/*
					 * new Thread(new Runnable(){
					 * 
					 * @Override public void run() { try { Thread.sleep(3000);
					 * 
					 * Message msg = mHandler.obtainMessage(); msg.what = 8;
					 * mHandler.sendMessage(msg);
					 * 
					 * } catch (InterruptedException e) { // TODO Auto-generated
					 * catch block e.printStackTrace(); }
					 * 
					 * }} ).start();
					 */

				}
			}
		});

		Log.d("BSK", "----> magic FM start.");
		// mAudioManager.setParameters("routing=2");

		Runnable playFmRunnable = new Runnable() {
			@Override
			public void run() {
				mAudioManager.setMode(5); // AudioManager.MODE_FM
				// OpenDev();
				// seekFreq();
				getFreqData();

				getFavorData();
			}
		};

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TestFM");
		mWakelock.setReferenceCounted(false);

		Message msg = mHandler.obtainMessage();
		msg.what = 3;
		mHandler.sendMessage(msg);
		mHandler.postDelayed(playFmRunnable, 100);

		SharedPreferences settings = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
		currFreq = settings.getString("channel", "");
		Log.e("", "on create:" + currFreq);

		if (!TextUtils.isEmpty(currFreq)) {
			currFreqTv.setText(formatFreq(currFreq));
		}

		selectListFreq(freqListView);
		selectListFreq(favorListView);

	}

	// 设置按钮的属性
	public void showFreqList() {
		tabLy.setBackgroundDrawable(res.getDrawable(R.drawable.list_bg));
		favoriteBtn.setTextColor(res.getColor(R.color.black));
		listBtn.setTextColor(res.getColor(R.color.blue));
		favorListView.setVisibility(View.GONE);
		freqListView.setVisibility(View.VISIBLE);
	}

	public void seekFreq() {
		String freq = GetFreq(); // GetFreq()返回频率
		Log.e("", "seekFreq freq=" + freq);
		
		String freqStr = String.valueOf(Integer.parseInt(freq) * 62 + Integer.parseInt(freq) / 2);
		String freqTitle = formatFreq(freqStr) + " MHz";

		Cursor cursor = mDb.query(Constants.TABLE_FREQ,
				new String[] { Constants.COLUMN_FREQUENCY, Constants.COLUMN_TITLE },
				Constants.COLUMN_FREQUENCY + "=" + freq, null, null, null, null);

		if (cursor != null && cursor.getCount() == 0) {

			saveFreq(Integer.parseInt(freq), freqTitle);

		}
		getFreqData();
		currFreqTv.setText(formatFreq(freqStr));

	}

	public void getFreqData() {
		try {
			Cursor cursor = mDb.query(Constants.TABLE_FREQ,
					new String[] { Constants.COLUMN_FREQUENCY, Constants.COLUMN_TITLE }, null, null, null, null, null);
			startManagingCursor(cursor);
			listAdapter = new SimpleCursorAdapter(this, R.layout.list_item_freq_tv, cursor, new String[] { "title" },
					new int[] { R.id.freq_tv });

			freqListView.setAdapter(listAdapter);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// 从数据库获取收藏的数据，并显示出来
	public void getFavorData() {
		try {
			// TABLE_FAVOR = "favor"
			// COLUMN_FREQUENCY = "_id"
			// COLUMN_TITLE = "title"
			Cursor cursor = mDb.query(Constants.TABLE_FAVOR,
					new String[] { Constants.COLUMN_FREQUENCY, Constants.COLUMN_TITLE }, null, null, null, null, null);
			startManagingCursor(cursor);
			favorAdapter = new SimpleCursorAdapter(this, R.layout.list_item_freq_tv, cursor, new String[] { "title" },
					new int[] { R.id.freq_tv });

			favorListView.setAdapter(favorAdapter);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// 字符串截取处理
	public String formatFreq(String freq) {
		String freqStr = "";
		if (!TextUtils.isEmpty(freq) && freq.length() > 5) {
			freqStr = freq.substring(0, freq.length() - 5);
			freqStr = freqStr.substring(0, freqStr.length() - 1) + "." + freqStr.substring(freqStr.length() - 1);
		}
		return freqStr;
	}

	//保存频道数据到数据库
	public void saveFreq(int frequency, String title) {
		ContentValues values = new ContentValues();
		values.put(Constants.COLUMN_FREQUENCY, frequency);
		values.put(Constants.COLUMN_TITLE, title);
		mDb.insert(Constants.TABLE_FREQ, null, values);
	}

	//保存收藏数据到数据库
	public void saveFavor(int frequency, String title) {
		ContentValues values = new ContentValues();
		values.put(Constants.COLUMN_FREQUENCY, frequency);
		values.put(Constants.COLUMN_TITLE, title);
		mDb.insert(Constants.TABLE_FAVOR, null, values);
	}

	//获取系统提示音的值
	public void getSoundEffect() {
		try {
			// mContext.getContentResolver(),要访问系统内部的东西时调用
			// Settings.System.SOUND_EFFECTS_ENABLED好像是音量键的按钮
			isSoundEffect = Settings.System.getInt(mContext.getContentResolver(),
					Settings.System.SOUND_EFFECTS_ENABLED);
			Log.e("", "isSoundEffect1=" + isSoundEffect);

		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
	}

	//value:传音量键的值
	public void setSoundEffect(int value) {
		try {
			//一开始oncreate里获取了系统提示音的状态，
			//那么退出APK的时候要把之前的系统提示音状态给回系统，因为打开APK时，如果没开系统提示音，APK就会把它打开
			Settings.System.putInt(mContext.getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, value);
			//Settings.System.getInt(mContext.getContentResolver(),Settings.System.SOUND_EFFECTS_ENABLED);
			Log.e("", "isSoundEffect2=" + isSoundEffect);
			if (value == 1) {
				//加载声音效果
				mAudioManager.loadSoundEffects();
			} else {
				//卸载音效
				mAudioManager.unloadSoundEffects();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void onResume() {

		mWakelock.acquire();

		super.onResume();
	}

	protected void onPause() {

		Log.d("BSK", "<----- magic FM suspend 1...");

		super.onPause();

	}

	@Override
	protected void onDestroy() {

		try {

			mAudioManager.setMode(AudioManager.MODE_NORMAL);
			CloseDev();
			if (headSetReceiver != null) {
				unregisterReceiver(headSetReceiver);
			}
			if (mWakelock != null && mWakelock.isHeld()) {
				mWakelock.release();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		super.onDestroy();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exitApp();
		}
		return false;
	}

	// 全局数据，保存channel
	public void saveChannel(String channel) {
		// PREF_NAME = "bskfm_pref"
		SharedPreferences settings = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("channel", channel);
		editor.commit();
	}

	public void selectListFreq(AbsListView listView) {
		// String freqStr = "";
		for (int i = 0; i < listView.getCount(); i++) {
			View v = listView.getChildAt(i);
			if (v != null) {
				v.setBackgroundColor(Color.TRANSPARENT);

				TextView tv = (TextView) v.findViewById(R.id.freq_tv);
				if (tv != null) {

					String tvStr = tv.getText().toString();
					String[] freqStrs = tvStr.split(" ");
					String freqStr = freqStrs[0];

					if (formatFreq(currFreq).equals(freqStr)) {
						v.setBackgroundColor(getResources().getColor(R.color.dark_yellow));
					}
				}
			}
		}

		checkFavoredFreq();

	}

	public void checkFavoredFreq() {
		// currFreq如果在Favor列表中，则点亮
		boolean hasFavored = false;
		Cursor cursor = mDb.query(Constants.TABLE_FAVOR,
				new String[] { Constants.COLUMN_FREQUENCY, Constants.COLUMN_TITLE }, null, null, null, null, null);
		startManagingCursor(cursor);
		while (cursor.moveToNext()) {
			String favoredFreq = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_FREQUENCY));
			Log.e("", "favoredFreq=" + favoredFreq);
			if (String.valueOf((int) (Integer.parseInt(currFreq) / 62.5)).equals(favoredFreq)) {
				hasFavored = true;
			}
		}
		Log.e("", "hasFavored=" + hasFavored);

		if (hasFavored) {
			isFavored = true;
			favorBtn.setBackgroundResource(R.drawable.favored);
		} else {
			isFavored = false;
			favorBtn.setBackgroundResource(R.drawable.favor);
		}
	}

	private class OnScrollListenerImple implements OnScrollListener {
		@Override
		public void onScroll(AbsListView listView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

		}

		@Override
		public void onScrollStateChanged(AbsListView listView, int scrollState) {
			selectListFreq(listView);
		}

	}
	//以前用来返回键的广播，现在没有用它了
	// 这是自己定义的广播，BC_CLOSE_FM = "com.bsk.fmradio.CLOSE_FM";
	private BroadcastReceiver mSystemKeyEventReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			/*
			 * if (action.equals(SYSTEM_HOME_KEY)) { Log.e("",
			 * "PPFRAM_HOME_PRESSED_UP  home down...");
			 * 
			 * Intent tIntent = new Intent(); ComponentName tComp = new
			 * ComponentName("com.wdxc.xiangpianbao",
			 * "com.wdxc.xiangpianbao.MainActivity");
			 * tIntent.setComponent(tComp);
			 * tIntent.setAction("android.intent.action.MAIN"); //
			 * tIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			 * 
			 * startActivity(tIntent); }else if(action.equals(SYSTEM_PIR_KEY)) {
			 * 
			 * Log.e("PFRAME", "PPFRAM_PIR_ACTIVE  broadcast received ...");
			 * 
			 * }
			 */

			if (action.equals(BC_CLOSE_FM)) {
				Log.e("PFRAME", " close fm ...");

				exitApp();
			}

		}
	};

	public void rotateAngle(final int type) { // type=1，换台；type=2,微调

		/*
		 * new Thread(new Runnable() {
		 * 
		 * @Override public void run() {
		 */
		//调节的幅度
		int angle = 0;

		if (type == 1) {
			// random() 方法可返回介于 0 ~ 1 之间的一个随机数。
			int offset = (new Random().nextInt() % 20);
			// 返回offset数字的绝对值
			angle = 40 + Math.abs(offset);
		} else if (type == 2) {
			int offset = (new Random().nextInt() % 8);
			angle = 16 + Math.abs(offset);
		}
		// 参数说明：
		// float fromDegrees：旋转的开始角度。
		// float toDegrees：旋转的结束角度。
		// int
		// pivotXType：X轴的伸缩模式，可以取值为ABSOLUTE、RELATIVE_TO_SELF、RELATIVE_TO_PARENT。
		// float pivotXValue：X坐标的伸缩值。
		// int
		// pivotYType：Y轴的伸缩模式，可以取值为ABSOLUTE、RELATIVE_TO_SELF、RELATIVE_TO_PARENT。
		// float pivotYValue：Y坐标的伸缩值。
		Animation anim = new RotateAnimation(currAngle, currAngle + angle, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);

		// DecelerateInterpolator,在动画开始的地方速率改变比较慢，然后开始减速
		DecelerateInterpolator lir = new DecelerateInterpolator();
		// 设置给anim
		anim.setInterpolator(lir);
		// 动画持续时间，单位为毫秒，这里是0.8秒
		anim.setDuration(800);
		// 为什么会有setFillAfter 和 setFillBefore这两个方法：
		//是因为有动画链的原因，假定你有一个移动的动画紧跟一个淡出的动画，
		//如果你不把移动的动画的setFillAfter置为true，那么移动动画结束后，
		//View会回到原来的位置淡出，如果setFillAfter置为true， 就会在移动动画结束的位置淡出
		anim.setFillBefore(true);
		anim.setFillAfter(true);
		//currAngle=旋转的开始角度，angle=调节的幅度，最后currAngle=currAngle+angle
		currAngle += angle;
		//如果角度大于等于360度，就设为0度
		if (currAngle >= 360) {
			currAngle = currAngle - 360;
		}
		//开始动画
		arrowIv.startAnimation(anim);

		/*
		 * }
		 * 
		 * }).start();
		 */

	}

	//退出APK
	public void exitApp() {
		try {
			//改变提示音
			setSoundEffect(isSoundEffect);
			mAudioManager.setMode(AudioManager.MODE_NORMAL);
			// mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
			CloseDev();
			if (headSetReceiver != null) {
				unregisterReceiver(headSetReceiver);
			}
			if (mWakelock != null && mWakelock.isHeld()) {
				mWakelock.release();
			}
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
