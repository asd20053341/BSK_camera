package com.cnlaunch.autoclear;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cnlaunch.autoclear.cleartask.AppClearTask;
import com.cnlaunch.autoclear.cleartask.DirClearTask;
import com.cnlaunch.autoclear.cleartask.FileClearTask;
import com.cnlaunch.autoclear.data.ClearData;
import com.cnlaunch.autoclear.data.XMLPullParserHandler;

public class MainActivity extends Activity implements AppClearTask.ClearTaskObserver {
	private final static String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		View clearBtn = findViewById(R.id.clear_btn);
		clearBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				clearTestApp();
			}
		});
	}

	private void clearTestApp() {
		// 1. parse the app to clear
		// 2. start clear file
		// 3. start clear app
		// 4. clear self
		List<ClearData> clearDatas = parseProperties(Paths.propertiesPath());
		Log.d(TAG, "clearTestApp÷–clearDatas:"+Arrays.toString(clearDatas.toArray()));
		clearFiles(clearDatas);
		clearDirs(clearDatas);
		clearApps(clearDatas);
	}

	private FileClearTask mFileClearTask;
	private DirClearTask mDirClearTask;
	private AppClearTask mAppClearTask;

	private void clearFiles(List<ClearData> clearDatas) {
		mFileClearTask = new FileClearTask(clearDatas);
		mFileClearTask.run();
	}

	private void clearDirs(List<ClearData> clearDatas) {
		mDirClearTask = new DirClearTask(clearDatas);
		mDirClearTask.run();
	}

	private void clearApps(List<ClearData> clearDatas) {
		mAppClearTask = new AppClearTask(this, this, clearDatas);
		mAppClearTask.run();
	}

	@Override
	public void onTaskFinish() {
		// uninstall self
		Uri packageURI = Uri.parse("package:" + getPackageName());
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
		startActivity(uninstallIntent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "delete app:" + resultCode);
		super.onActivityResult(requestCode, resultCode, data);

		if (mAppClearTask != null) {
			mAppClearTask.next(resultCode != 0);
		}
	}

	private List<ClearData> parseProperties(String propertiesPath) {
		File file = new File(propertiesPath);
		if (file.exists()) {
			Log.d(TAG, "file exist!");
			XMLPullParserHandler xpph = new XMLPullParserHandler();
			try {
				List<ClearData> clearDatas = xpph.parse(new FileInputStream(file));
				Log.d(TAG, Arrays.toString(clearDatas.toArray())+"+++++++++");
				return clearDatas;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			showTips("Can't find the properties file!");
		}
		return null;
	}

	private void showTips(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

}
