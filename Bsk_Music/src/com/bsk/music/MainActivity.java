package com.bsk.music;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bsk.music.entity.AppInfo;
import com.bsk.music.entity.AppInfoListResp;
import com.bsk.music.entity.Music;
import com.bsk.music.util.HttpUtils;
import com.google.gson.Gson;

public class MainActivity extends Activity {

	private final String KU_GOU = "http://m.kugou.com";
	private final String BAI_DU = "http://music.baidu.com";
	private final String QQ = "http://y.qq.com";
	private final String XIA_MI = "http://www.xiami.com/";
	private final String DOU_BAN = "http://music.douban.com/";
	// private final String DOU_BAN = "http://douban.fm";

	// private Button kugouBtn;
	// private Button qqBtn;
	// private Button baiduBtn;
	// private Button xiamiBtn;
	// private Button doubanBtn;

	private ListView lv_list;
	private List<Music> musicList;
	private List<Music> testList;
	private ArrayAdapter<String> arrayAdapter;

	private Button exitBtn;
	private Button settingBtn;

	private TextView versionTv;

	private WebView mWebView;
	private LinearLayout networkLy;

	private int index; // 记录上次点击的类型，如果相同就进行刷新，尝试解决白屏问题

	WakeLock m_wklk;

	// private final String SYSTEM_HOME_KEY = "PPFRAM_HOME_PRESSED_UP";
	// private final String SYSTEM_PIR_KEY = "PPFRAM_PIR_ACTIVE";

	private final String BC_CLOSE_FM = "com.bsk.fmradio.CLOSE_FM";
	private final String BC_SETUP_NETWORK = "PPFRAM_SETUP_NETWORK";

	private int type; // 记录上次播放的位置

	// 服务器的网址
	private final String HTTP_LOGIN = "http://112.74.211.156/api/dev/login/login1";
	private final String HTTP_GET_MUSIC = "http://112.74.211.156/api/dev/heartbeat/hb"; // +
																						// "&app1=update.zip,,";
	private final String HTTP_USERNAME = "iframe@126.com";
	private final String HTTP_PASSWORD = "123456";
	private String scode; // 登录token
	private Handler mHandler;
	private Runnable getSCodeRunnable;

	private int versionCode;

	private String versionStr = "V1.0";

	private AppInfoListResp appInfoListResp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		m_wklk = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "cn");
		// 获取相应的锁
		m_wklk.acquire();

		versionTv = (TextView) findViewById(R.id.version_tv);

		PackageManager manager = this.getPackageManager();
		PackageInfo info;
		try {
			info = manager.getPackageInfo(this.getPackageName(), 0);
			versionCode = info.versionCode;
		} catch (NameNotFoundException e1) {
			e1.printStackTrace();
		}

		// registerReceiver(mHomeKeyEventReceiver, new
		// IntentFilter("SYSTEM_HOME_KEY"));

		/*
		 * IntentFilter filter = new IntentFilter();
		 * filter.addAction(SYSTEM_HOME_KEY); //
		 * filter.addAction(SYSTEM_PIR_KEY);
		 * registerReceiver(mHomeKeyEventReceiver, filter);
		 */

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BC_CLOSE_FM);
		registerReceiver(mSystemKeyEventReceiver, intentFilter);

		/*
		 * kugouBtn = (Button)findViewById(R.id.kugou_btn); qqBtn =
		 * (Button)findViewById(R.id.qq_btn); baiduBtn =
		 * (Button)findViewById(R.id.baidu_btn); xiamiBtn =
		 * (Button)findViewById(R.id.xiami_btn); doubanBtn =
		 * (Button)findViewById(R.id.douban_btn);
		 */

		exitBtn = (Button) findViewById(R.id.exit_btn);
		settingBtn = (Button) findViewById(R.id.setting_btn);

		mWebView = (WebView) findViewById(R.id.music_wv);
		networkLy = (LinearLayout) findViewById(R.id.network_ly);

		// 显示左边的列表
		lv_list = (ListView) findViewById(R.id.lv_list);

		// 一开始获取的肯定是没数据把
		SharedPreferences settings = this.getSharedPreferences("my_music", 0);
		type = settings.getInt("type", 0);
		Log.e("", "type=" + type);

		// 登录服务器的链接，线程
		getSCodeRunnable = new Runnable() {
			@Override
			public void run() {
				String urlPath = HTTP_LOGIN + "?login_type=bind_device&email=" + HTTP_USERNAME + "&password="
						+ HTTP_PASSWORD;

				Message msg = mHandler.obtainMessage();

				// 返回链接上的数据
				// {"ret":0,"scode":"2c255ef684ad49489051b7bb756428e0","user":{"u_id":15,"u_nick_name":"","u_sex":"","u_height":"","u_weight":"","u_birth":"","u_head_photo":"","u_regist_time":"2015-09-06
				// 19:14:09","bind_email":"iframe@126.com","bind_handset":"","bind_device":"iframe","bind_qq":"","bind_sina_weibo":"","login_type":"bind_email","hx_id":15,"hx_pwd":"44c4c17332cace2124a1a836d9fc4b6f"},"head_url":"","hx_data":{"hx_id":15,"hx_pwd":"44c4c17332cace2124a1a836d9fc4b6f"}}
				String jsonString = HttpUtils.doGet(urlPath);
				Log.e("", "jsonString= " + jsonString);
				if (!TextUtils.isEmpty(jsonString)) {
					try {
						JSONObject dataResponse = new JSONObject(jsonString);
						// result:0
						String result = dataResponse.getString("ret");
						// 这里把接收消息的what设为result
						// 在这里面的方法本来就是一个线程，现在又要启动一个线程
						msg.what = Integer.parseInt(result);
						Bundle bundle = new Bundle();

						// 可能就是返回的result是0 的话，就说明成功，就有scode；如果不是0，就有msg
						if (!TextUtils.isEmpty(result)) {
							if (result.equals("0")) {
								// scode:2c255ef684ad49489051b7bb756428e0
								scode = dataResponse.getString("scode");
								bundle.putString("scode", scode);
							} else {
								String msgStr = dataResponse.getString("msg");
								bundle.putString("msg", msgStr);
							}
						}

						// 把数据存入bundle，然后发送消息，启动线程
						msg.setData(bundle);
						msg.sendToTarget();

					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

			}
		};

		// 这里接收线程
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Bundle bundle = null;
				bundle = msg.getData();

				if (msg.what == 0) { // 获取score成功
					// {"ret":12,"msg":"Need relogin"},可能没登录，登录后返回的就一样了
					// 上一个线程接收到的scode在这里使用了
					String path = HTTP_GET_MUSIC + "?scode=" + scode + "&app1=com.bsk.music,1.0,1";
					MyTask myTask = new MyTask();
					// 启动线程
					myTask.execute(path);
				} else if (msg.what == 0x888) {
					Log.e("", "0x888---");
					View view = lv_list.getChildAt(type);
					view.setBackgroundColor(getResources().getColor(R.color.btn_bg));
					// view.setTextColor(getResources().getColor(R.color.white));
				}

			}
		};

		// 在线音乐列表获取，
		musicList = new ArrayList<Music>();
		// 适配器
		arrayAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.item_list);
		// lv_list是ArrayList，一开始估计也是没数据，因为下面才开始加载数据
		lv_list.setAdapter(arrayAdapter);

		Music m1 = new Music("酷狗音乐", "Kugou Music", KU_GOU, 1);
		Music m2 = new Music("百度音乐", "Baidu Music", BAI_DU, 2);
		Music m3 = new Music("QQ音乐", "QQ Music", QQ, 3);
		Music m4 = new Music("虾米音乐", "Xiami Music", XIA_MI, 4);
		Music m5 = new Music("豆瓣音乐", "Douban Music", DOU_BAN, 5);
		// 把数据先存到testList
		testList = new ArrayList<Music>();
		testList.add(m1);
		testList.add(m2);
		testList.add(m3);
		testList.add(m4);
		testList.add(m5);

		// 目前为止都是没有联网操作，都是直接把写好的数据加到了适配器，所以左边显示的是5个listView子项
		for (int i = 0; i < testList.size(); i++) {
			Music music = (Music) testList.get(i);
			// 判断当前系统的语言环境，来获取中文名还是英文名，并把名字给予适配器arrayAdapter，这样就把数据加入到了适配器啦
			if (getResources().getConfiguration().locale.getLanguage().equals("en")) {
				arrayAdapter.add(music.getEnglishName());
			} else {
				arrayAdapter.add(music.getName());
			}
			// 每次取出一组数据，都存到musicList
			musicList.add(music);
		}

		// 加完之后刷新一下
		arrayAdapter.notifyDataSetChanged();

		// 判断网络状态，可用的话，就显示mWebView，否则显示networkLy
		// 这里就是联网获取服务器的数据了
		if (NetworkUtils.getNetWorkStatus(this)) {
			// initMusic(type);

			// 打开登录服务器的线程
			new Thread(getSCodeRunnable).start();
			mWebView.setVisibility(View.VISIBLE);
			networkLy.setVisibility(View.INVISIBLE);

		} else {
			mWebView.setVisibility(View.INVISIBLE);
			networkLy.setVisibility(View.VISIBLE);

			// if
			// (getResources().getConfiguration().locale.getLanguage().equals("en"))
			// {
			// arrayAdapter.add("Kugou Music");
			// arrayAdapter.add("Baidu Music");
			// arrayAdapter.add("QQ Music");
			// arrayAdapter.add("Xiami Music");
			// arrayAdapter.add("Douban Music");
			// arrayAdapter.notifyDataSetChanged();
			// } else {
			// arrayAdapter.add("酷狗音乐");
			// arrayAdapter.add("百度音乐");
			// arrayAdapter.add("QQ音乐");
			// arrayAdapter.add("虾米音乐");
			// arrayAdapter.add("豆瓣音乐");
			// arrayAdapter.notifyDataSetChanged();
			// }
		}

		// lv_list.setOnItemClickListener(new OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> parent, View view, int
		// position, long id) {
		// if (NetworkUtils.getNetWorkStatus(MainActivity.this)) {
		// mWebView.setVisibility(View.VISIBLE);
		//
		// Music music = musicList.get(position);
		// loadWeb(music.getUrl(), music.getType());
		// for (int i = 0; i < parent.getChildCount(); i++) {
		// TextView tv = (TextView) parent.getChildAt(i);
		// tv.setBackgroundColor(getResources().getColor(R.color.no_bg));
		// tv.setTextColor(getResources().getColor(R.color.black));
		// }
		// TextView selectView = (TextView) view;
		// selectView.setBackgroundColor(getResources().getColor(R.color.btn_bg));
		// selectView.setTextColor(getResources().getColor(R.color.white));
		// } else {
		// mWebView.setVisibility(View.GONE);
		// networkLy.setVisibility(View.VISIBLE);
		// }
		//
		// }
		// });

		// List的点击事件
		lv_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (NetworkUtils.getNetWorkStatus(MainActivity.this)) {

					mWebView.setVisibility(View.VISIBLE);
					networkLy.setVisibility(View.GONE);

					Log.e("", "pos=" + position);

					// type是记录上次播放的位置，position为点击了list的哪一项
					type = position;

					Log.e("", "type=" + type + ",index=" + index);
					// 把点击了哪一项，存到全局数据里
					SharedPreferences settings = MainActivity.this.getSharedPreferences("my_music", 0);
					SharedPreferences.Editor localEditor = settings.edit();
					localEditor.putInt("type", type);
					localEditor.commit();
					// musicList为那5组数据
					Music music = musicList.get(position);
					// 调用加载网页的方法，并把url与type传过去，这样就可以实现点击了哪里，就显示哪个网页了
					loadWeb(music.getUrl(), music.getType());
					for (int i = 0; i < parent.getChildCount(); i++) {
						TextView tv = (TextView) parent.getChildAt(i);
						tv.setBackgroundColor(getResources().getColor(R.color.no_bg));
						tv.setTextColor(getResources().getColor(R.color.black));
					}
					TextView selectView = (TextView) view;
					selectView.setBackgroundColor(getResources().getColor(R.color.btn_bg));
					selectView.setTextColor(getResources().getColor(R.color.white));

				} else {
					mWebView.setVisibility(View.GONE);
					networkLy.setVisibility(View.VISIBLE);
				}

			}
		});

		settingBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setAction(BC_SETUP_NETWORK);
				sendBroadcast(intent);
				finish();
			}
		});

		exitBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// 释放锁
				m_wklk.release();
				mWebView.reload();
				finish();
			}
		});
	}

	public void loadWeb(String url, int pos) {
		if (NetworkUtils.getNetWorkStatus(MainActivity.this)) {

			// index=记录上次点击的类型，如果相同就进行刷新，尝试解决白屏问题
			//if (pos == index) {
				// mWebView刷新
			//	mWebView.reload();

		//	} else {
				mWebView.getSettings().setJavaScriptEnabled(true);
				mWebView.loadUrl(url);
				mWebView.setWebChromeClient(new WebChromeClient());
				mWebView.setWebViewClient(new WebViewClient() {
					@Override
					public boolean shouldOverrideUrlLoading(WebView view, String url) {
						view.loadUrl(url);
						return false;
					}
				});

			//}

			index = type;

		} else {
			mWebView.setVisibility(View.GONE);
			networkLy.setVisibility(View.VISIBLE);
		}
	}

	public void initMusic(Music music) {
		networkLy.setVisibility(View.INVISIBLE);
		mWebView.setVisibility(View.VISIBLE);

		loadWeb(music.getUrl(), music.getType());
	}

	@Override
	protected void onPause() {

		super.onPause();
	}

	@Override
	protected void onDestroy() {

		try {

			if (m_wklk != null && m_wklk.isHeld()) {
				m_wklk.release();
			}

			if (mSystemKeyEventReceiver != null) {
				unregisterReceiver(mSystemKeyEventReceiver);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {

			Log.e("", "back down---");
			mWebView.reload();
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// 退出广播
	private BroadcastReceiver mSystemKeyEventReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(BC_CLOSE_FM)) {
				Log.e("PFRAME", " close music ...");
				System.exit(0);
			}
		}
	};

	// 自定义异步任务，从网络中获取数据，加载音乐列表
	class MyTask extends AsyncTask<String, Void, String> {
		
		// 将在onPreExecute 方法执行后马上执行，该方法运行在后台线程中。这里将主要负责执行那些很耗时的后台计算工作。可以调用
		// publishProgress方法来更新实时的任务进度。该方法是抽象方法，子类必须实现。
		@Override
		protected String doInBackground(String... params) {
			// HttpUtils http = new HttpUtils();
			String content = HttpUtils.doGet(params[0]);
			return content;
		}

		// 该方法将在执行实际的后台操作前被UI thread调用。可以在该方法中做一些准备工作，如在界面上显示一个进度条。
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		// 在doInBackground 执行完成后，onPostExecute 方法将被UI
		// thread调用，后台的计算结果将通过该方法传递到UI thread.
		// result估计是服务器返回的json数据
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			/*
			 * {"ret":0,"apps":[{"ver_id":27,"disabled":0,"devtype":"iframe",
			 * "appname":"com.bsk.music","pkgname":"com.bsk.music","reqver":1,
			 * "reqbuild":1,"pkgver":2,"pkgbuild":2,"pkgurl":"","pkgmd5":"",
			 * "pkgsize":0,"toclean":0,"create_time":"2015-09-06 21:56:22"
			 * ,"push_time":"2015-01-01 00:00:00"
			 * ,"push_max":2000,"push_cnt":1,"remarks":"{ \"msg\":0, \"data\":
			 * [{\
			 * "name\":\"酷狗\", \"englishName\":\"Kugou Music\", \"url\":\"http://m.kugou.com\",   \"type\"
			 * :1}, {\
			 * "name\":\"QQ\",  \"englishName\":\"QQ Music\",   \"url\":\"http://y.qq.com\",      \"type\":2},{\"name\":\"虾米\", \"englishName\":\"Xiami Music\", \"url\":\"http://www.xiami.com\", \"type\":3},{\"name\":\"百度\", \"englishName\":\"Baidu Music\", \"url\":\"http://music.baidu.com\", \"type\":4}]}"
			 * ,"ptime":1420041600}]}
			 */
			Log.i("Tag", "result: " + result);
			// 把ArrayList清空
			musicList.clear();
			// 把适配器情况清空
			arrayAdapter.clear();
			try {// json数据解析

				Log.e("", "result=" + result);

				Gson gson = new Gson();
				// 把服务器返回的数据储存到AppInfoListResp类，这里估计是自动存进去了
				appInfoListResp = gson.fromJson(result, AppInfoListResp.class);
				int code = appInfoListResp.getRet();
				Log.e("", "code=" + code);

				AppInfo appInfo = null;
				List<AppInfo> appInfos = appInfoListResp.getApps();
				// 有数据返回的话，就获取get(0)
				if (appInfos != null && appInfos.size() > 0) {

					appInfo = appInfos.get(0);

				}

				// JSONObject jsonObject = new JSONObject(result);// json对象
				// String code = jsonObject.getString("code");

				JSONArray jsonArray = null;
				JSONObject jsonObject = null;
				// 说明成功从服务器获取到数据
				if (code == 0) {
					if (appInfo != null) {
						// 设置为服务器上的版本号
						versionStr = "V" + appInfo.getPkgver() + ".0";
						versionTv.setText(versionStr);
						Log.e("", "versionStr=" + versionStr);
						String musics = appInfo.getRemarks();
						jsonObject = new JSONObject(musics);

						jsonArray = jsonObject.getJSONArray("data");// json数组

						for (int i = 0; i < jsonArray.length(); i++) {
							// 通过i来获取每一组 json数据
							JSONObject object = (JSONObject) jsonArray.get(i);
							// 把json数据存到Music里面
							Music music = new Music(object.getString("name"), object.getString("englishName"),
									object.getString("url"), object.getInt("type"));

							// 判断当前系统的语言环境，把json返回的名字给予适配器arrayAdapter
							if (getResources().getConfiguration().locale.getLanguage().equals("en")) {
								arrayAdapter.add(music.getEnglishName());
							} else {
								arrayAdapter.add(music.getName());
							}
							// 最后增加到ArrayList里面
							musicList.add(music);
						}
					}

				}

				// 如果musicList.size()为0的话，说明没有从服务器获取到数据，没获取到，那么就重新把旧数据传给适配器
				if (musicList.size() == 0) {
					for (int i = 0; i < testList.size(); i++) {
						Music music = (Music) testList.get(i);
						// 判断当前系统的语言环境
						if (getResources().getConfiguration().locale.getLanguage().equals("en")) {
							arrayAdapter.add(music.getEnglishName());
						} else {
							arrayAdapter.add(music.getName());
						}
						musicList.add(music);
					}
				}

				// System.out.println(jsonArray.toString());

				if (musicList != null && musicList.size() > 0) {

					Log.e("", "musicList.size=" + musicList.size());

					arrayAdapter.notifyDataSetChanged();

					if (!(type > 0 && type < musicList.size())) {
						type = 0; //
					}
					initMusic(musicList.get(type));

					int count = lv_list.getChildCount();

					lv_list.setSelection(type);// 想要设置我认选中第一个

					System.out.println("lv_list count: " + count);

					Message msg = mHandler.obtainMessage();
					msg.what = 0x888;
					msg.sendToTarget();
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

	}

}
