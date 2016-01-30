package com.bsk.listenerCamera;

import java.io.File;
import java.util.List;

import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class MyService extends Service {
	UsbMonitorThread mUsbthread;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mUsbthread = new UsbMonitorThread();
		mUsbthread.start();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
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
	}

	private class UsbMonitorThread extends Thread {
		boolean mIsUsbthreadStop;
		ComponentName mCmpName;
		ComponentName mCmpName_video;

		public UsbMonitorThread() {
			super();
			mIsUsbthreadStop = true;
			mCmpName = new ComponentName("com.android.awgallery", "com.android.camera.CameraLauncher");
			mCmpName_video = new ComponentName("com.android.awgallery", "com.android.camera.VideoCamera");
			// mCmpName = new ComponentName("com.android.gallery3d",
			// "com.android.camera.CameraLauncher");
			// mCmpName_video = new ComponentName("com.android.gallery3d",
			// "com.android.camera.VideoCamera");
		}

		boolean isExistUSBCamera() {
			File file = new File("/dev/video1");
			if (file.exists()) {
				return true;
			}
			return false;

		}

		private boolean isCameraRuning() {
			ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			List<RunningTaskInfo> taskInfoList = am.getRunningTasks(10);
			for (RunningTaskInfo taskInfo : taskInfoList) {
				if (taskInfo.baseActivity.equals(mCmpName) || taskInfo.baseActivity.equals(mCmpName_video)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void run() {
			while (true) {
				// 照相机正在运行并且有USB摄像头(只弹一次),只要摄像头没有拔出来，就不需要再次监听
				if (mIsUsbthreadStop) {
					if (isExistUSBCamera() && !isCameraRuning()) {
						mIsUsbthreadStop = false;
					} else if (isCameraRuning() && isExistUSBCamera()) {
						// 弹出一次后就不再弹出
						mIsUsbthreadStop = false;
						Intent intent = new Intent(MyService.this, FActivity.class);
//						intent.addCategory(Intent.CATEGORY_DEFAULT);
//						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
						startActivity(intent);						
					}
					// 如果没有USB摄像头，就再次监听
				} else if (!isExistUSBCamera()) {
					mIsUsbthreadStop = true;
					Intent in = new Intent("com.cnlaunch.intent.action.CLOSE_CAMERA_ACTIVITY");
					sendBroadcast(in);
				}
				SystemClock.sleep(1000);
			}
		}

	}

}
