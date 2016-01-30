package com.cnlaunch.autoclear.cleartask;

import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.util.Log;

import com.cnlaunch.autoclear.data.ClearData;

public class AppClearTask extends AbsClearTask {
	private static final String TAG = "AppClearTask";

	public static interface ClearTaskObserver {
		void onTaskFinish();
	}

	private Context mContext;
	private ClearTaskObserver mObserver;
	private boolean mIsPreAppRemoved;

	public AppClearTask(Context context, ClearTaskObserver observer, List<ClearData> clearDatas) {
		super(clearDatas);
		mContext = context;
		mObserver = observer;
	}

	@Override
	public void checkAndRun() {
		if (mClearDatas == null) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (!mClearDatas.isEmpty()) {
					Iterator<ClearData> it = mClearDatas.iterator();
					while (it.hasNext()) {
						ClearData cd = it.next();
						Log.d(TAG, "app package:" + cd.getPath());
						if (cd.getType().equals(ClearData.TYPE_APP)) {
							if (isAppExists(cd.getPath())) {
								Log.d(TAG, "app package:" +"++++++++++");
								Uri packageURI = Uri.parse("package:" + cd.getPath());
								Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
								((Activity) mContext).startActivityForResult(uninstallIntent, 0);
								pause();
								if (mIsPreAppRemoved) {
									it.remove();
								}
							} else {
								Log.d(TAG, "app package:" +"---------");
								it.remove(); // if app not exist, simply remove
							}
						}
					}
				}
				mObserver.onTaskFinish();
			}
		}).start();
	}

	private boolean isAppExists(String packageName) {
		if (packageName == null || "".equals(packageName))
			return false;
		try {
			mContext.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	public void next(boolean removePreApp) {
		mIsPreAppRemoved = removePreApp;
		resume();
	}

	private synchronized void pause() {
		try {
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private synchronized void resume() {
		notifyAll();
	}
}