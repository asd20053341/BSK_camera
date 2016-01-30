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
	// hashmap1������XML�ļ����ص�����
	HashMap<Integer, String> hashmap1;
	private MyDatabaseHelper dbHelper;
	// �����ʾ������ListView
	private ListView listview = null;
	// �����˵�
	private String[] dialogItems = new String[] { "ɱ���ó���", "�����������ɱ���ó���" };
	// ��RunningAppInfo������ģ�Ӧ�ó����ǩ��Ӧ�ó���ͼ��Ӧ�ó�������Ӧ�İ����ȵȣ����Ǵ�ÿһ��Ӧ�õ���Ϣ
	private List<RunningAppInfo> mlistAppInfo = null;
	// ������ݿ��������������
	HashMap<Integer, String> map1;
	// һ�����ֶ���
	private TextView tvInfo = null;
	// �������࣬ͨ���������Ի�ȡ���ĺܶ���Ϣ
	private PackageManager pm;
	private ActivityManager mActivityManager = null;
	// private Button btn111;
	private Button btn222;
	// ������
	SQLiteDatabase db;
	// ���߳���ˢ��
	private Handler handler = null;
	// �Զ���������
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
		// ��ѯĳһ�ض����̵�����Ӧ�ó���
		// Intent intent = getIntent();
		// �Ƿ��ѯĳһ�ض�pid��Ӧ�ó���
		// ���û�л�ȡ��EXTRA_PROCESS_ID���ͷ���-1
		// int pid = intent.getIntExtra("EXTRA_PROCESS_ID", -1);

		/*
		 * if ( pid != -1) { //ĳһ�ض������������������е�Ӧ�ó��� mlistAppInfo
		 * =querySpecailPIDRunningAppInfo(intent, pid); } else{
		 */
		// ��ѯ�����������е�Ӧ�ó�����Ϣ�� �����������ڵĽ���id�ͽ�����
		tvInfo.setText("�������е�Ӧ�ó���");
		// queryAllRunningAppInfo��������ÿһ��Ӧ�õ���Ϣ��
		// �浽ArrayList<RunningAppInfo>()��mlistAppInfo������
		mlistAppInfo = queryAllRunningAppInfo();
		// }
		// ������mlistAppInfo��ÿ��Ӧ�õ���Ϣ�������Զ�����������������������mlistAppInfo�벼�ֽ�������������listView��ʾ����
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

	// ԭ������ǣ�û����ǰsharedPreferencesû���ݣ����Է���true��isFirstRun�����Խ���if
	// ���к�editor.putBoolean("isFirstRun", false);���������ݣ�����Ϊfasle�������Ժ����ж��������if
	// sharedPreferences��������ȫ�ֵģ��Ժ󶼴��ڣ�������װAPK
	public void firstRunning() {
		SharedPreferences sharedPreferences = this.getSharedPreferences("share", MODE_PRIVATE);
		boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
		Editor editor = sharedPreferences.edit();
		if (isFirstRun) {
			// ����ǵ�һ�����У���ִ��addDataXML();
			addDataXML();
			editor.putBoolean("isFirstRun", false);
			editor.commit();
		} else {
			;
		}

	}

	public void addDataToDatabase(String package1) {
		// ��Ϊ���new����MyDatabaseHelper�Ķ���dbHelper�������������֮ǰ����ɱ���ˣ��ͻᱨ��
		// ����������ݿ��Ѿ����ڣ������ٴ�ִ�д������ݿ������Ҳ�����ٴ���һ��
		creatDataBase();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		// ��package1���������嵽�����"package"�ֶ���
		values.put("package", package1);
		// �嵽blacklist��
		db.insert("blacklist", null, values);
		values.clear();
	}

	public void addDataXML() {
		creatDataBase();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		HashMap<Integer, String> hasp1 = parseProperties(Paths.propertiesPath());
		// try {
		ContentValues values = new ContentValues();
		// queryDataContrast�������ص������ݿ����package
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
		System.out.println("����" + ad + "������");
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
		// MyDatabaseHelper���Ǽ̳���
		dbHelper = new MyDatabaseHelper(this, "BlackList.db", null, 1);
		dbHelper.getWritableDatabase();
	}

	public void clearTable() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("drop table BlackList");
		Toast.makeText(RunningAppActivity.this, "ɾ��blacklist��ɹ�", Toast.LENGTH_SHORT).show();
	}

	public void clearData() {
		try {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.execSQL("delete table BlackList");
			Toast.makeText(RunningAppActivity.this, "���blacklist������ݳɹ�", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("����clearData��������û�����˻�ִ���Ҹ�����Ȱ���");
		}
	}

	// �����ݿ�������ݣ����浽HashMAp����map1
	public HashMap<Integer, String> queryDataContrast() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query("blacklist", null, null, null, null, null, null);
		int i = 1;
		int ad = cursor.getCount();
		System.out.println("����" + ad + "������");
		// �Ƶ���һ������
		if (cursor.moveToFirst()) {
			do {
				// ��ȡ����Ϊpackage������
				String name = cursor.getString(cursor.getColumnIndex("package"));
				map1.put(i, name);
				i++;
				System.out.println(name);
				// ��һ�������ݵĻ������Ƶ���һ��
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
		// �õ�·��
		File file = new File(propertiesPath);
		// System.out.println(propertiesPath);
		// System.out.println(file);

		try {
			// ������ڣ��ͽ���
			if (file.exists()) {
				// �Լ��������XMLPullParserHandler
				XMLPullParserHandler xpph = new XMLPullParserHandler();
				// ������XMLPullParserHandler���parse�����ǰ�xml�ļ�·������ȥ
				// �������ص�ֵ����HashMap<Integer, String>��hashmap1
				hashmap1 = xpph.parse(new FileInputStream(file));
				System.out.println(hashmap1);
				return hashmap1;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("����RunningAppActivity��parseProperties�������ļ��Ҳ����ˣ�");
		}

		return null;
	}

	// ɱ���ý��̣�����ˢ��
	// onItemClick��ListView�ĵ���¼�
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {
		// newһ�������򣬰����涨���dialogItems���󴫽�������Ϊÿһ���˵������õ���¼�
		new AlertDialog.Builder(this).setItems(dialogItems, new DialogInterface.OnClickListener() {

			//
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// ɱ������
				// 0�������顾0��
				try {

					if (which == 0) {
						// ɱ���ý��̣��ͷŽ���ռ�õĿռ�
						System.out.println(mlistAppInfo.get(position).getPkgName());
						forceStopAPK(mlistAppInfo.get(position).getPkgName());
						// ˢ�½���
						// refresh();
						timerTaskrefresh();
					}
					// 1�������顾1��
					else if (which == 1) {
						// positionΪ������һ�����Ϣ
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

	// ��������Ϳ���ɱ������
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

	// ԭ��1.ͨ����ȡ�����Ѿ���װ�ĳ��� ��2.Ȼ���ȡ�������еĳ��� 3.�����������еĳ��򣬴���HashMap��
	// 4.���������Ѱ�װ�ĳ���ͬʱ�������HashMap�ĳ������Աȣ��������Լ����������
	// ��ȡ�������еĳ�����ҪPackageManager�࣬List<ApplicationInfo>�࣬ActivityManager�࣬
	// List<ActivityManager.RunningAppProcessInfo>��
	private List<RunningAppInfo> queryAllRunningAppInfo() {
		// PackageManager��
		pm = this.getPackageManager();
		// ��ѯ�����Ѿ���װ��Ӧ�ó���,(Ҫͨ��PackageManager���
		// �����ȡ�����Ѱ�װ�ĳ��򣬷���һ��List<ApplicationInfo>��PackageManager���û�ô���)
		List<ApplicationInfo> listAppcations = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		// �������������������еĳ������Ϣ ������HashMap��ͨ��get����ȡÿһ��Ӧ����Ϣ
		Map<String, ActivityManager.RunningAppProcessInfo> pgkProcessAppMap = new HashMap<String, ActivityManager.RunningAppProcessInfo>();
		// ��ȡϵͳ����
		mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

		// ͨ������ActivityManager��getRunningAppProcesses()�������ϵͳ�������������еĽ���
		// �����ǻ�ȡ���������������е���Ϣ��������ô׼ȷ�õ������Լ��뵽���Ǹ������أ�
		List<ActivityManager.RunningAppProcessInfo> appProcessList = mActivityManager.getRunningAppProcesses();

		// �������׼���õ�����ÿһ��Ӧ�õ���Ϣ����������Ȼ���ȡÿһ��Ӧ�õİ������浽HashMap���Ժ�ͨ��get�����õ��ð����µ�Ӧ�õ���Ϣ
		for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
			// һ������pid������
			int pid = appProcess.pid;
			// ������ǰ���
			String processName = appProcess.processName;
			// Ҳ�ǰ������������кü���һ���İ�������������ڸý����������Ӧ�ó����
			String[] pkgNameList = appProcess.pkgList;
			// �������Ӧ�ó���İ���
			for (int i = 0; i < pkgNameList.length; i++) {
				String pkgName = pkgNameList[i];
				// ������map������,��������������еĳ��򶼿���ͨ��get������pkgName�����õ���������µ�Ӧ�õ���Ϣ
				pgkProcessAppMap.put(pkgName, appProcess);
			}

		}
		// ���������������е�Ӧ�ó�����Ϣ
		List<RunningAppInfo> runningAppInfos = new ArrayList<RunningAppInfo>(); // ������˲鵽��AppInfo
		// listAppcations���Ѿ����������еĳ���һ��List<ApplicationInfo>��
		for (ApplicationInfo app : listAppcations) {
			// ����ϵͳӦ��
			if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				if (!app.packageName.equals("com.example.blacklist")) {
					// �ѱ��浽HashMap���ÿһ���������еĳ��������г������Աȣ���������г������ҵ��˶�Ӧ�İ����� �ͽ���
					if (pgkProcessAppMap.containsKey(app.packageName)) {
						// ��ø�packageName�� pid �� processName
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
						// ��setAppInfo(app, pid,
						// processName)���浽�����Լ�����ķ����setAppInfo�������������Լ�����ģ�
						// �����кܶ�set������������Ϣ�ģ�����һ��RunningAppInfo����
						// ��runningAppInfos.add���ѷ��ص�RunningAppInfo����浽List<RunningAppInfo>��
						runningAppInfos.add(setAppInfo(app, pid, processName));
					}
				}
			}
		}

		// �������runningAppInfos����������������������е�Ӧ�õ���Ϣ��ÿ���������е�Ӧ���ִ���RunningAppInfo������
		return runningAppInfos;

	}

	// ����һ��RunningAppInfo���� ������ÿһ��Ӧ�õ���Ϣ
	private RunningAppInfo setAppInfo(ApplicationInfo app, int pid, String mInfo) {
		RunningAppInfo appInfo = new RunningAppInfo();
		// ��ȡӦ����
		appInfo.setAppLabel((String) app.loadLabel(pm));
		// ��ȡӦ��ͼ��
		appInfo.setAppIcon(app.loadIcon(pm));
		// ��ȡӦ�ð���
		appInfo.setPkgName(app.packageName);
		// ��ȡӦ��PID
		appInfo.setPid(pid);
		// appInfo.setMemoryInfo(mInfo);
		// ��ȡӦ�ý�����
		appInfo.setProcessName(mInfo);
		return appInfo;
	}

	// ĳһ�ض������������������е�Ӧ�ó���
	private List<RunningAppInfo> querySpecailPIDRunningAppInfo(Intent intent, int pid) {

		String[] pkgNameList = intent.getStringArrayExtra("EXTRA_PKGNAMELIST");
		String processName = intent.getStringExtra("EXTRA_PROCESS_NAME");

		// update ui
		tvInfo.setText("����idΪ" + pid + " ���е�Ӧ�ó�����  :  " + pkgNameList.length);

		pm = this.getPackageManager();

		// ���������������е�Ӧ�ó�����Ϣ
		List<RunningAppInfo> runningAppInfos = new ArrayList<RunningAppInfo>(); // ������˲鵽��AppInfo

		for (int i = 0; i < pkgNameList.length; i++) {
			// ���ݰ�����ѯ�ض���ApplicationInfo����
			ApplicationInfo appInfo;
			try {
				appInfo = pm.getApplicationInfo(pkgNameList[i], 0);
				// runningAppInfos.add(getAppInfo(appInfo, pid, processName));
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // 0����û���κα��;
		}
		return runningAppInfos;
	}
}