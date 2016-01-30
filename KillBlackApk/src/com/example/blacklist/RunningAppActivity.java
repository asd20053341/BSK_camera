package com.example.blacklist;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.example.blacklist.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SyncStatusObserver;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Debug;
import android.os.Debug.MemoryInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;

public class RunningAppActivity extends Activity implements OnItemClickListener {
	// hashmap1：解析XML文件返回的数据
	HashMap<Integer, String> hashmap1;
	private MyDatabaseHelper dbHelper;
	// 最后显示出来的ListView
	private ListView listview = null;
	// 弹出菜单
	private String[] dialogItems = new String[] { "杀死该程序", "加入黑名单并杀死该程序" };
	// 存RunningAppInfo类里面的，应用程序标签，应用程序图像，应用程序所对应的包名等等，就是存每一个应用的信息
	private List<RunningAppInfo> mlistAppInfo = null;
	// 存从数据库遍历出来的数据
	HashMap<Integer, String> map1;
	// 一段文字而已
	private TextView tvInfo = null;
	// 包管理类，通过这个类可以获取包的很多信息
	private PackageManager pm;
	private ActivityManager mActivityManager = null;
	// private Button btn111;
	private Button btn222;
	// 测试用
	SQLiteDatabase db;
	// 开线程来刷新
	private Handler handler = null;
	// 自定义适配器
	BrowseRunningAppAdapter browseAppAdapter = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_app_list);
		listview = (ListView) findViewById(R.id.listviewApp);
		listview.setOnItemClickListener(this);
		tvInfo = (TextView) findViewById(R.id.tvInfo);
		// btn111 = (Button) findViewById(R.id.btn111);
		btn222 = (Button) findViewById(R.id.btn222);
		mlistAppInfo = new ArrayList<RunningAppInfo>();
		hashmap1 = new HashMap<Integer, String>();
		handler = new Handler();
		map1 = new HashMap<Integer, String>();
		creatDataBase();
		firstRunning();
		// timerTaskrefresh();

		// timerTask();
		// 查询某一特定进程的所有应用程序
		// Intent intent = getIntent();
		// 是否查询某一特定pid的应用程序
		// 如果没有获取到EXTRA_PROCESS_ID，就返回-1
		// int pid = intent.getIntExtra("EXTRA_PROCESS_ID", -1);

		/*
		 * if ( pid != -1) { //某一特定进程里所有正在运行的应用程序 mlistAppInfo
		 * =querySpecailPIDRunningAppInfo(intent, pid); } else{
		 */
		// 查询所有正在运行的应用程序信息： 包括他们所在的进程id和进程名
		tvInfo.setText("正在运行的应用程序：");
		// queryAllRunningAppInfo方法返回每一个应用的信息，
		// 存到ArrayList<RunningAppInfo>()的mlistAppInfo对象里
		mlistAppInfo = queryAllRunningAppInfo();
		// }
		// 把数据mlistAppInfo（每个应用的信息）传给自定义的适配器，适配器里面把mlistAppInfo与布局结合起来，最后让listView显示出来
		browseAppAdapter = new BrowseRunningAppAdapter(this, mlistAppInfo);
		listview.setAdapter(browseAppAdapter);
		/*
		 * Intent intent = new Intent();
		 * intent.setClass(RunningAppActivity.this, RunningAppService.class);
		 * startService(intent);
		 */
		/*
		 * btn111.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) {
		 * 
		 * creatDataBase(); queryData();
		 * 
		 * } });
		 */

		btn222.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// clearTable();
				refresh();

			}
		});

	}

	// 原理好像是，没运行前sharedPreferences没数据，所以返回true给isFirstRun，可以进入if
	// 运行后editor.putBoolean("isFirstRun", false);插入了数据，并改为fasle，所以以后运行都不会进入if
	// sharedPreferences的数据是全局的，以后都存在，除非重装APK
	public void firstRunning() {
		SharedPreferences sharedPreferences = this.getSharedPreferences("share", MODE_PRIVATE);
		boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
		Editor editor = sharedPreferences.edit();
		if (isFirstRun) {
			// 如果是第一次运行，就执行addDataXML();
			addDataXML();
			editor.putBoolean("isFirstRun", false);
			editor.commit();
		} else {
			;
		}

	}

	public void addDataToDatabase(String package1) {
		// 因为如果new出了MyDatabaseHelper的对象dbHelper后，如果增加数据之前对象被杀死了，就会报错
		// 反正如果数据库已经存在，就算再次执行创建数据库操作，也不会再创建一次
		creatDataBase();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		// 把package1创建来，插到表里的"package"字段里
		values.put("package", package1);
		// 插到blacklist表
		db.insert("blacklist", null, values);
		values.clear();
	}

	public void addDataXML() {
		creatDataBase();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		HashMap<Integer, String> hasp1 = parseProperties(Paths.propertiesPath());
		// try {
		ContentValues values = new ContentValues();
		// queryDataContrast（）返回的是数据库里的package
		// HashMap<Integer, String> map2 = queryDataContrast();
		if (hasp1 != null) {
			for (int i = 1; i <= hasp1.size(); i++) {
				if (!hasp1.get(i).equals("true") && !hasp1.get(i).equals("false") &&!hasp1.get(i).equals("aaa")) {
					values.put("package", hasp1.get(i));
					db.insert("blacklist", null, values);
					values.clear();
				}
			}
		}

	}

	public void queryData() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query("blacklist", null, null, null, null, null, null);
		int ad = cursor.getCount();
		System.out.println("共有" + ad + "行数据");
		if (cursor.moveToFirst()) {
			do {
				String name = cursor.getString(cursor.getColumnIndex("package"));
				System.out.println(name);
			} while (cursor.moveToNext());
		}
		cursor.close();
	}

	public void DeleteDataBase() {
		db.execSQL("drop table if exists Book");
	}

	public void creatDataBase() {
		// MyDatabaseHelper类是继承了
		dbHelper = new MyDatabaseHelper(this, "BlackList.db", null, 1);
		dbHelper.getWritableDatabase();
	}

	public void clearTable() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("drop table BlackList");
		Toast.makeText(RunningAppActivity.this, "删除blacklist表成功", Toast.LENGTH_SHORT).show();
	}

	public void clearData() {
		try {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.execSQL("delete table BlackList");
			Toast.makeText(RunningAppActivity.this, "清除blacklist表格数据成功", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("我在clearData方法，都没数据了还执行我干嘛，逗比啊！");
		}
	}

	// 从数据库遍历数据，并存到HashMAp对象map1
	public HashMap<Integer, String> queryDataContrast() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query("blacklist", null, null, null, null, null, null);
		int i = 1;
		int ad = cursor.getCount();
		System.out.println("共有" + ad + "行数据");
		// 移到第一行数据
		if (cursor.moveToFirst()) {
			do {
				// 获取属性为package的数据
				String name = cursor.getString(cursor.getColumnIndex("package"));
				map1.put(i, name);
				i++;
				System.out.println(name);
				// 下一行有数据的话，就移到下一行
			} while (cursor.moveToNext());
		}
		cursor.close();
		System.out.println(map1);
		return map1;
	}

	public void timerTaskrefresh() {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				refresh();
				handler.removeMessages(0);
			}
		}, 1500);

	}

	public void refresh() {
		mlistAppInfo.clear();
		mlistAppInfo = queryAllRunningAppInfo();
		BrowseRunningAppAdapter mprocessInfoAdapter = new BrowseRunningAppAdapter(RunningAppActivity.this,
				mlistAppInfo);
		listview.setAdapter(mprocessInfoAdapter);
	}

	private HashMap<Integer, String> parseProperties(String propertiesPath) {
		// 得到路径
		File file = new File(propertiesPath);
		// System.out.println(propertiesPath);
		// System.out.println(file);

		try {
			// 如果存在，就进入
			if (file.exists()) {
				// 自己定义的类XMLPullParserHandler
				XMLPullParserHandler xpph = new XMLPullParserHandler();
				// 调用了XMLPullParserHandler里的parse，就是把xml文件路径传进去
				// 解析返回的值传入HashMap<Integer, String>的hashmap1
				hashmap1 = xpph.parse(new FileInputStream(file));
				System.out.println(hashmap1);
				return hashmap1;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("我在RunningAppActivity的parseProperties方法，文件找不到了！");
		}

		return null;
	}

	// 杀死该进程，并且刷新
	// onItemClick是ListView的点击事件
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {
		// new一个弹出框，把上面定义的dialogItems对象传进来，并为每一个菜单项设置点击事件
		new AlertDialog.Builder(this).setItems(dialogItems, new DialogInterface.OnClickListener() {

			//
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 杀死进程
				// 0代表数组【0】
				try {

					if (which == 0) {
						// 杀死该进程，释放进程占用的空间
						System.out.println(mlistAppInfo.get(position).getPkgName());
						forceStopAPK(mlistAppInfo.get(position).getPkgName());
						// 刷新界面
						// refresh();
						timerTaskrefresh();
					}
					// 1代表数组【1】
					else if (which == 1) {
						// position为点中那一项的信息
						addDataToDatabase(mlistAppInfo.get(position).getPkgName());
						forceStopAPK(mlistAppInfo.get(position).getPkgName());
						timerTaskrefresh();

					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

		}).create().show();
	}

	// 传入包名就可以杀死程序
	private void forceStopAPK(String pkgName) {
		Process sh = null;
		DataOutputStream os = null;
		try {
			sh = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(sh.getOutputStream());
			final String Command = "am force-stop " + pkgName + "\n";
			os.writeBytes(Command);
			os.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * try { sh.waitFor(); } catch (InterruptedException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */
	}

	public boolean filterApp(ApplicationInfo info) {

		return false;
	}

	// 原理：1.通过获取所有已经安装的程序 。2.然后获取正在运行的程序。 3.遍历正在运行的程序，存入HashMap。
	// 4.遍历所有已安装的程序，同时与存在了HashMap的程序做对比，并存入自己定义的类里
	// 获取正在运行的程序，需要PackageManager类，List<ApplicationInfo>类，ActivityManager类，
	// List<ActivityManager.RunningAppProcessInfo>类
	private List<RunningAppInfo> queryAllRunningAppInfo() {
		// PackageManager类
		pm = this.getPackageManager();
		// 查询所有已经安装的应用程序,(要通过PackageManager类的
		// 对象获取所有已安装的程序，返回一个List<ApplicationInfo>后，PackageManager类就没用处了)
		List<ApplicationInfo> listAppcations = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		// 用来保存所有正在运行的程序的信息 ，存在HashMap，通过get来获取每一个应用信息
		Map<String, ActivityManager.RunningAppProcessInfo> pgkProcessAppMap = new HashMap<String, ActivityManager.RunningAppProcessInfo>();
		// 获取系统服务
		mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

		// 通过调用ActivityManager的getRunningAppProcesses()方法获得系统里所有正在运行的进程
		// 这里是获取到了所有正在运行的信息，但是怎么准确得到我们自己想到的那个程序呢？
		List<ActivityManager.RunningAppProcessInfo> appProcessList = mActivityManager.getRunningAppProcesses();

		// 这里就能准备得到，把每一个应用的信息遍历出来，然后获取每一个应用的包名，存到HashMap，以后通过get包名得到该包名下的应用的信息
		for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
			// 一个进程pid，数字
			int pid = appProcess.pid;
			// 好像就是包名
			String processName = appProcess.processName;
			// 也是包名，但好像有好几个一样的包名，获得运行在该进程里的所有应用程序包
			String[] pkgNameList = appProcess.pkgList;
			// 输出所有应用程序的包名
			for (int i = 0; i < pkgNameList.length; i++) {
				String pkgName = pkgNameList[i];
				// 加入至map对象里,最后所有正在运行的程序都可以通过get包名（pkgName），得到这个包名下的应用的信息
				pgkProcessAppMap.put(pkgName, appProcess);
			}

		}
		// 保存所有正在运行的应用程序信息
		List<RunningAppInfo> runningAppInfos = new ArrayList<RunningAppInfo>(); // 保存过滤查到的AppInfo
		// listAppcations：已经保存了所有的程序，一个List<ApplicationInfo>，
		for (ApplicationInfo app : listAppcations) {
			// 过滤系统应用
			if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				if (!app.packageName.equals("com.example.blacklist")) {
					// 把保存到HashMap里的每一个正在运行的程序与所有程序做对比，如果在所有程序中找到了对应的包名， 就进入
					if (pgkProcessAppMap.containsKey(app.packageName)) {
						// 获得该packageName的 pid 和 processName
						int pid = pgkProcessAppMap.get(app.packageName).pid;
						String processName = pgkProcessAppMap.get(app.packageName).processName;
						/*
						 * int[] psds = new int[] {
						 * pgkProcessAppMap.get(app.packageName).pid };
						 * MemoryInfo[] memoryInfo =
						 * mActivityManager.getProcessMemoryInfo(psds); int
						 * memorySize = memoryInfo[0].dalvikPrivateDirty; double
						 * memorySize2 = (memorySize / 1024);
						 * System.out.println(runningAppInfos.add(getAppInfo(
						 * app, pid, memorySize2))+"111111111");
						 */
						// 先setAppInfo(app, pid,
						// processName)，存到我们自己定义的方法里，setAppInfo方法就是我们自己定义的，
						// 里面有很多set方法，保存信息的，返回一个RunningAppInfo对象，
						// 再runningAppInfos.add，把返回的RunningAppInfo对象存到List<RunningAppInfo>里
						runningAppInfos.add(setAppInfo(app, pid, processName));
					}
				}
			}
		}

		// 返回这个runningAppInfos对象，里面存了所有正在运行的应用的信息，每个正在运行的应用又存在RunningAppInfo对象里
		return runningAppInfos;

	}

	// 构造一个RunningAppInfo对象 ，保存每一个应用的信息
	private RunningAppInfo setAppInfo(ApplicationInfo app, int pid, String mInfo) {
		RunningAppInfo appInfo = new RunningAppInfo();
		// 获取应用名
		appInfo.setAppLabel((String) app.loadLabel(pm));
		// 获取应用图标
		appInfo.setAppIcon(app.loadIcon(pm));
		// 获取应用包名
		appInfo.setPkgName(app.packageName);
		// 获取应用PID
		appInfo.setPid(pid);
		// appInfo.setMemoryInfo(mInfo);
		// 获取应用进程名
		appInfo.setProcessName(mInfo);
		return appInfo;
	}

	// 某一特定经常里所有正在运行的应用程序
	private List<RunningAppInfo> querySpecailPIDRunningAppInfo(Intent intent, int pid) {

		String[] pkgNameList = intent.getStringArrayExtra("EXTRA_PKGNAMELIST");
		String processName = intent.getStringExtra("EXTRA_PROCESS_NAME");

		// update ui
		tvInfo.setText("进程id为" + pid + " 运行的应用程序共有  :  " + pkgNameList.length);

		pm = this.getPackageManager();

		// 保存所有正在运行的应用程序信息
		List<RunningAppInfo> runningAppInfos = new ArrayList<RunningAppInfo>(); // 保存过滤查到的AppInfo

		for (int i = 0; i < pkgNameList.length; i++) {
			// 根据包名查询特定的ApplicationInfo对象
			ApplicationInfo appInfo;
			try {
				appInfo = pm.getApplicationInfo(pkgNameList[i], 0);
				// runningAppInfos.add(getAppInfo(appInfo, pid, processName));
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // 0代表没有任何标记;
		}
		return runningAppInfos;
	}
}