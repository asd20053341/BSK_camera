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
	// ��������������еĳ�����Ϣ
	List<RunningAppInfo> mlistAppInfo1 = null;
	HashMap<Integer, String> hasp1 = null;
	// ����XML����������
	HashMap<Integer, String> hashmap1;
	// ��RunningAppInfo������ģ�Ӧ�ó����ǩ��Ӧ�ó���ͼ��Ӧ�ó�������Ӧ�İ����ȵȣ����Ǵ�ÿһ��Ӧ�õ���Ϣ
	// �������࣬ͨ���������Ի�ȡ���ĺܶ���Ϣ
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
			// ��ȡ�������Ĺ㲥����ȡϵͳ������BC_LIGHT_SENSOR�㲥
			String action = intent.getAction();
			// ������ֻҪ��BC_LIGHT_SENSOR�㲥���������Զ�����
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
					System.out.println("����RunningAppService��timerTask�ļ��XML��ʱ������������");
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
			System.out.println("����RunningAppService��OpenActivity���������쳣��");
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
					System.out.println("RunningAppService��timerTask2:���Ǽ��XML�Ƿ�Ϊfalse��ʱ������������");
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
			System.out.println("��������RunningAppService��OpenActivity���������쳣��");
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
			System.out.println("��������RunningAppService��killApk��������ָ���쳣");
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
			System.out.println("����RunningAppService��killApkSlepp��������ָ���쳣");
		}
	}
	
	
	

	public HashMap<Integer, String> parseProperties(String propertiesPath) {
		// �õ�·��
		File file = new File(propertiesPath);
		// System.out.println(propertiesPath);
		// System.out.println(file);
		// �������
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
			System.out.println("����RunningAppService�е�parseProperties�������ļ��Ҳ����ˣ�");

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

	// ��ѯ�����������е�Ӧ�ó�����Ϣ�� �����������ڵĽ���id�ͽ�����
	// �����ֱ�ӻ�ȡ��ϵͳ�ﰲװ������Ӧ�ó���Ȼ����ݱ���pkgname���˻�ȡ�����������е�Ӧ�ó���
	private List<RunningAppInfo> queryAllRunningAppInfo() {
		// pm=ApplicationPackageManager
		pm = this.getPackageManager();
		// ��ѯ�����Ѿ���װ��Ӧ�ó���
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
		// ApplicationInfo.DisplayNameComparator(pm));// ����

		// ���������������еİ��� �Լ������ڵĽ�����Ϣ,pgkProcessAppMap=HashMap
		Map<String, ActivityManager.RunningAppProcessInfo> pgkProcessAppMap = new HashMap<String, ActivityManager.RunningAppProcessInfo>();

		mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		// ͨ������ActivityManager��getRunningAppProcesses()�������ϵͳ�������������еĽ���
		// appProcessList=ArrayList
		List<ActivityManager.RunningAppProcessInfo> appProcessList = mActivityManager.getRunningAppProcesses();

		for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {

			/*
			 * if (appProcess.pkgList.equals("com.tencent.mm") ||
			 * appProcess.processName.equals("com.android.phone")) { continue; }
			 */
			// һ������pid������
			int pid = appProcess.pid;
			// Debug.MemoryInfo asd=getProcessMemoryInfo(pid);
			// ������ǰ���
			String processName = appProcess.processName; // ������

			// Log.i(TAG, "processName: " + processName + " pid: " + pid);
			// Ҳ�ǰ������������кü���һ���İ���
			String[] pkgNameList = appProcess.pkgList; // ��������ڸý����������Ӧ�ó����

			// �������Ӧ�ó���İ���
			for (int i = 0; i < pkgNameList.length; i++) {
				String pkgName = pkgNameList[i];
				// Log.i(TAG, "packageName " + pkgName + " at index " + i + " in
				// process " + pid);
				// ������map������,��������������еĳ��򶼿���ͨ��get������pkgName��
				// �õ������appProcess��pid����������������
				pgkProcessAppMap.put(pkgName, appProcess);
			}

		}
		// ���������������е�Ӧ�ó�����Ϣ
		List<RunningAppInfo> runningAppInfos = new ArrayList<RunningAppInfo>(); // ������˲鵽��AppInfo

		for (ApplicationInfo app : listAppcations) {
			// ����ð������� ����һ��RunningAppInfo����
			if (pgkProcessAppMap.containsKey(app.packageName)) {
				// ��ø�packageName�� pid �� processName
				int pid = pgkProcessAppMap.get(app.packageName).pid;
				String processName = pgkProcessAppMap.get(app.packageName).processName;
				runningAppInfos.add(getAppInfo(app, pid, processName));
			}
		}

		return runningAppInfos;

	}

	// ����һ��RunningAppInfo���� ������ֵ
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