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
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

public class BlackListActivity extends Activity implements OnItemClickListener {
	HashMap<Integer, String> map1;
	HashMap<Integer, String> hashmap1;
	private MyDatabaseHelper dbHelper1;
	// 最后显示出来的ListView
	private ListView listview = null;
	HashMap<Integer, String> hasp2 = null;
	private List<RunningAppInfo> mlistAppInfo1 = null;
	private String[] dialogItems = new String[] { "移出黑名单" };
	// 包管理类，通过这个类可以获取包的很多信息
	private PackageManager pm;
	private ActivityManager mActivityManager = null;
	SQLiteDatabase db;
	BlackListAdapter blackListAdapter = null;
	Handler handler1 = null;
	private Timer timer3=null;
	private Button btnrefresh = null;
	private RunningAppService runningAppService=null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_black_list);
		map1 = new HashMap<Integer, String>();
		handler1 = new Handler();
		hasp2 = new HashMap<Integer, String>();
		runningAppService=new RunningAppService();
		mlistAppInfo1 = new ArrayList<RunningAppInfo>();
		listview = (ListView) findViewById(R.id.listviewblack);
		btnrefresh = (Button) findViewById(R.id.btnrefresh);
		listview.setOnItemClickListener(this);

		// 查询所有正在运行的应用程序信息： 包括他们所在的进程id和进程名
		// queryAllRunningAppInfo方法返回每一个应用的信息，
		// 存到ArrayList<RunningAppInfo>()的mlistAppInfo对象里
		// mlistAppInfo = queryAllRunningAppInfo();
		// }
		creatDataBase();
		HashMap<Integer, String> hashmap = queryDataContrast();
		// 把数据mlistAppInfo（每个应用的信息）传给自定义的适配器，适配器里面把mlistAppInfo与布局结合起来，最后让listView显示出来
		blackListAdapter = new BlackListAdapter(this, hashmap);
		listview.setAdapter(blackListAdapter);
		timerTaskDatabase();

		btnrefresh.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				refresh();
			}
		});
	}
	
	public void creatDataBase() {
		dbHelper1 = new MyDatabaseHelper(this, "BlackList.db", null, 1);
		dbHelper1.getWritableDatabase();
	}

	public void deletedata(String packages) {
		creatDataBase();
		SQLiteDatabase db = dbHelper1.getWritableDatabase();
		db.delete("blacklist", "package=?", new String[] { packages });
	}

	public void deletedata() {
		//跟增加数据同理
		creatDataBase();
		SQLiteDatabase db = dbHelper1.getWritableDatabase();
		//删除blacklist表里的，条件为：package=com.tencent.mm
		db.delete("blacklist", "package=?", new String[] { "com.tencent.mm" });
	}

	public void timerTaskDatabase() {
		try {
			timer3=new Timer();
			timer3.schedule(new TimerTask() {

				@Override
				public void run() {
					killApk();
					ShutActivity();
					System.out.println("BlaclListActivity中timerTask：我是监控数据库定时器，我在运行");
				}
			}, 0, 5000);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}
	
	public void ShutActivity() {
		try {
			hasp2 = runningAppService.parseProperties(Paths.propertiesPath());
			if (hasp2 != null) {
				for (int i = 1; i <= hasp2.size(); i++) {
					if (hasp2.get(i).equals("false")) {					
						timer3.cancel();										
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("我在我在RunningAppService的OpenActivity方法，报异常了");
		}
	}
	

	public void killApk() {
		try {
			List<RunningAppInfo> mlistAppInfo1 = queryAllRunningAppInfo();
			HashMap<Integer, String> hasp2 = queryDataContrast();
			for (int i = 0; i < hasp2.size(); i++) {
				for (RunningAppInfo mlistAppInfo2 : mlistAppInfo1) {
					if (hasp2.get(i).equals(mlistAppInfo2.getPkgName())) {
						forceStopAPK(hasp2.get(i));
					} else {
						;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("我在killApk方法，空指针异常BlackListActivity");
		}
	}

	public HashMap<Integer, String> queryDataContrast() {
		SQLiteDatabase db = dbHelper1.getWritableDatabase();
		Cursor cursor = db.query("blacklist", null, null, null, null, null, null);
		int i = 0;
		int ad = cursor.getCount();
		// System.out.println("共有" + ad + "行数据");
		if (cursor.moveToFirst()) {
			do {
				String name = cursor.getString(cursor.getColumnIndex("package"));
				map1.put(i, name);
				i++;
				// System.out.println(name);
			} while (cursor.moveToNext());
		}
		cursor.close();
		// System.out.println(map1.size());
		return map1;
	}

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

	}

	// 移出黑名单，并且刷新
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(this).setItems(dialogItems, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0) {
					try {		
					//System.out.println(map1.get(position));
					deletedata(map1.get(position));
					timerTaskrefresh();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).create().show();
	}

	public void timerTaskrefresh() {
		handler1.postDelayed(new Runnable() {
			@Override
			public void run() {
				refresh();
				handler1.removeMessages(0);
			}
		}, 1500);

	}

	public void refresh() {
		map1.clear();
		HashMap<Integer, String> hashmap = queryDataContrast();
		// 把数据mlistAppInfo（每个应用的信息）传给自定义的适配器，适配器里面把mlistAppInfo与布局结合起来，最后让listView显示出来
		blackListAdapter = new BlackListAdapter(this, hashmap);
		listview.setAdapter(blackListAdapter);
	}

	// 查询所有正在运行的应用程序信息： 包括他们所在的进程id和进程名
	// 这儿我直接获取了系统里安装的所有应用程序，然后根据报名pkgname过滤获取所有真正运行的应用程序
	private List<RunningAppInfo> queryAllRunningAppInfo() {
		// pm=ApplicationPackageManager
		pm = this.getPackageManager();
		// 查询所有已经安装的应用程序
		// listAppcations=ArrayList(id=830039497624)
		// List<PackageInfo> packageInfos =
		// getPackageManager().getInstalledPackages(0);
		List<ApplicationInfo> listAppcations = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);

		/*
		 * if ((listAppcations..flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
		 * 
		 * }
		 */
		// Collections.sort(listAppcations, new
		// ApplicationInfo.DisplayNameComparator(pm));// 排序

		// 保存所有正在运行的包名 以及它所在的进程信息,pgkProcessAppMap=HashMap
		Map<String, ActivityManager.RunningAppProcessInfo> pgkProcessAppMap = new HashMap<String, ActivityManager.RunningAppProcessInfo>();

		mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		// 通过调用ActivityManager的getRunningAppProcesses()方法获得系统里所有正在运行的进程
		// appProcessList=ArrayList
		List<ActivityManager.RunningAppProcessInfo> appProcessList = mActivityManager.getRunningAppProcesses();

		for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {

			/*
			 * if (appProcess.pkgList.equals("com.tencent.mm") ||
			 * appProcess.processName.equals("com.android.phone")) { continue; }
			 */
			// 一个进程pid，数字
			int pid = appProcess.pid;
			// Debug.MemoryInfo asd=getProcessMemoryInfo(pid);
			// 好像就是包名
			String processName = appProcess.processName; // 进程名

			// Log.i(TAG, "processName: " + processName + " pid: " + pid);
			// 也是包名，但好像有好几个一样的包名
			String[] pkgNameList = appProcess.pkgList; // 获得运行在该进程里的所有应用程序包

			// 输出所有应用程序的包名
			for (int i = 0; i < pkgNameList.length; i++) {
				String pkgName = pkgNameList[i];
				// Log.i(TAG, "packageName " + pkgName + " at index " + i + " in
				// process " + pid);
				// 加入至map对象里,最后所有正在运行的程序都可以通过get包名（pkgName）
				// 得到里面的appProcess（pid，进程名，包名）
				pgkProcessAppMap.put(pkgName, appProcess);
			}

		}
		// 保存所有正在运行的应用程序信息
		List<RunningAppInfo> runningAppInfos = new ArrayList<RunningAppInfo>(); // 保存过滤查到的AppInfo

		for (ApplicationInfo app : listAppcations) {
			// 如果该包名存在 则构造一个RunningAppInfo对象
			if (pgkProcessAppMap.containsKey(app.packageName)) {
				// 获得该packageName的 pid 和 processName
				int pid = pgkProcessAppMap.get(app.packageName).pid;
				String processName = pgkProcessAppMap.get(app.packageName).processName;
				runningAppInfos.add(getAppInfo(app, pid, processName));
			}
		}

		return runningAppInfos;

	}

	private RunningAppInfo getAppInfo(ApplicationInfo app, int pid, String processName) {
		RunningAppInfo appInfo = new RunningAppInfo();
		appInfo.setAppLabel((String) app.loadLabel(pm));
		appInfo.setAppIcon(app.loadIcon(pm));
		appInfo.setPkgName(app.packageName);

		appInfo.setPid(pid);
		appInfo.setProcessName(processName);

		return appInfo;
	}

}