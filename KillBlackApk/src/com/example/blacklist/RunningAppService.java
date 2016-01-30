package com.example.blacklist;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;

public class RunningAppService extends Service {
	// 存放所有正在运行的程序信息
	List<RunningAppInfo> mlistAppInfo1 = null;
	HashMap<Integer, String> hasp1 = null;
	// 返回XML解析的数据
	HashMap<Integer, String> hashmap1;
	// 存RunningAppInfo类里面的，应用程序标签，应用程序图像，应用程序所对应的包名等等，就是存每一个应用的信息
	// 包管理类，通过这个类可以获取包的很多信息
	private PackageManager pm;
	private ActivityManager mActivityManager = null;
	private Timer timer = null;
	private Timer timer2 = null;
	private MyDatabaseHelper dbHelper2 = null;

	@Override
	public void onCreate() {
		super.onCreate();
		hashmap1 = new HashMap<Integer, String>();
		hasp1 = new HashMap<Integer, String>();
		SleepKillAPKBroadcastReg();
		timerTaskService();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		onCreate();
		// unregisterReceiver(SleepKillAPKBroadcast);
	}

	private void SleepKillAPKBroadcastReg() {
		IntentFilter filter = new IntentFilter();
		// filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction("android.intent.action.SCREEN_OFF");
		this.registerReceiver(SleepKillAPKBroadcast, filter);
	}

	private BroadcastReceiver SleepKillAPKBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 获取监听到的广播，获取系统发出的BC_LIGHT_SENSOR广播
			String action = intent.getAction();
			// 这里是只要有BC_LIGHT_SENSOR广播，就让它自动自增
			if (action.equals("android.intent.action.SCREEN_OFF")) {
				killApkSleep();

			}
		}
	};

	public void timerTaskService() {
		try {
			timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					killApk();
					OpenActivity();
					System.out.println("我是RunningAppService中timerTask的监控XML定时器，我在运行");
				}
			}, 0, 5000);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	public void OpenActivity() {
		try {
			hasp1 = parseProperties(Paths.propertiesPath());
			if (hasp1 != null) {
				for (int i = 1; i <= hasp1.size(); i++) {
					if (hasp1.get(i).equals("true")) {
						
						Intent intent = new Intent();
						intent.setClass(RunningAppService.this, MainActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
						startActivity(intent);
						timer.cancel();
						timerTask2();
						//unregisterReceiver(SleepKillAPKBroadcast);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("我在RunningAppService的OpenActivity方法，报异常了");
		}
	}


	public void creatDataBase() {
		dbHelper2 = new MyDatabaseHelper(this, "BlackList.db", null, 1);
		dbHelper2.getWritableDatabase();
	}

	public void timerTask2() {
		try {
			timer2 = new Timer();
			timer2.schedule(new TimerTask() {
				@Override
				public void run() {
					ShutActivity();
					System.out.println("RunningAppService中timerTask2:我是监控XML是否为false定时器，我在运行");
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
			hasp1 = parseProperties(Paths.propertiesPath());
			if (hasp1 != null) {
				for (int i = 1; i <= hasp1.size(); i++) {
					if (hasp1.get(i).equals("false")) {
						timer2.cancel();
						MainActivity.instance.finish();
						timerTaskService();
						//SleepKillAPKBroadcastReg();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("我在我在RunningAppService的OpenActivity方法，报异常了");
		}
	}
	//am startservice -n com.example.blacklist/com.example.blacklist.RunningAppService

	public void killApk() {
		try {
			mlistAppInfo1 = queryAllRunningAppInfo();
			hasp1 = parseProperties(Paths.propertiesPath());
			for (int i = 1; i <= hasp1.size(); i++) {
				for (RunningAppInfo mlistAppInfo2 : mlistAppInfo1) {
					if (hasp1.get(i).equals(mlistAppInfo2.getPkgName())) {
						forceStopAPK(hasp1.get(i));
					} else {
						;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("我在我在RunningAppService的killApk方法，空指针异常");
		}
	}

	public void killApkSleep() {
		try {
			mlistAppInfo1 = queryAllRunningAppInfo();
			hasp1 = parseProperties(Paths.propertiesPath2());
			for (int i = 1; i <= hasp1.size(); i++) {
				for (RunningAppInfo mlistAppInfo2 : mlistAppInfo1) {
					if (hasp1.get(i).equals(mlistAppInfo2.getPkgName())) {
						forceStopAPK(hasp1.get(i));
					} else {
						;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("我在RunningAppService的killApkSlepp方法，空指针异常");
		}
	}
	
	
	

	public HashMap<Integer, String> parseProperties(String propertiesPath) {
		// 得到路径
		File file = new File(propertiesPath);
		// System.out.println(propertiesPath);
		// System.out.println(file);
		// 如果存在
		try {
			if (file.exists()) {
				// Log.d(TAG, "file exist!");

				XMLPullParserHandler xpph = new XMLPullParserHandler();

				hashmap1 = xpph.parse(new FileInputStream(file));
				// Log.d(TAG, Arrays.toString(clearDatas.toArray()));

				return hashmap1;

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("我在RunningAppService中的parseProperties方法，文件找不到了！");

		}

		return null;
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

		/*
		 * try { sh.waitFor(); } catch (InterruptedException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */
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

	// 构造一个RunningAppInfo对象 ，并赋值
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