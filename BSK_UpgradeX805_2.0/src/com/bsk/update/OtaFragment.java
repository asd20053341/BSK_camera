package com.bsk.update;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bsk.update.model.AppInfo;
import com.bsk.update.model.AppInfoListResp;
import com.bsk.update.util.CommonUtils;
import com.bsk.update.util.FileUtils;
import com.bsk.update.util.HttpUtils;
import com.bsk.update.util.LogUtils;
import com.bsk.update.util.MD5FileUtil;
import com.bsk.update.view.InstallPackage;
import com.google.gson.Gson;

public class OtaFragment extends Fragment {

	private View rootView;

	private Button localBtn;
	private Button checkBtn;

	private TextView versionTv;
	private TextView tipTv;
	private TextView titleTv;
	private TextView descTv;
	private TextView downloadPrecent;

	private ProgressBar downloadProgress;

	private String email;
	private String password;

	private String urlPath;
	private Handler mHandler;
	private JSONObject dataResponse;

	private String scode;
	private AppInfoListResp appInfoListResp;

	private boolean isSCode;

	private AppInfo appInfo; // update.zip包
	private DownloadManager dowanloadmanager = null;
	private DownloadChangeObserver downloadObserver;
	private long lastDownloadId;

	private Handler dlHandler;
	public static final Uri CONTENT_URI = Uri.parse("content://downloads/my_downloads");

	private String firmVersion; // 固件版本
	private String filePath; // update.zip文件路径

	private boolean isDownload; // 是否可下载
	private boolean isDownloading; // 是否下载中

	private Resources res;
	private Context mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		mContext = OtaFragment.this.getActivity();
		res = this.getResources();

		// 是否可下载
		isDownload = false;

		// 固件版本
		firmVersion = SystemProperties.get("ro.product.firmware", "");

		// 获取update.zip这个文件的路径
		filePath = Constants.SD_PATH + "/" + Environment.DIRECTORY_DOWNLOADS + "/" + Constants.APP_UPDATE_ZIP;

		// log输出：固件版本
		LogUtils.e("firm version=" + firmVersion);

		// String 类型
		email = "aaaad";
		// String 类型
		password = "test";

		// Constants.UPDATE_PREFS = "update_prefs"
		SharedPreferences settings = mContext.getSharedPreferences(Constants.UPDATE_PREFS, 0);
		// String 类型
		scode = settings.getString("Scode", "");
		LogUtils.e("SharedPreferences Scode=" + scode);
	}

	


	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// 一开始确实为空，就把fragment_ota这个布局给它
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_ota, null);
		}

		// addView后，你要添加的view就一直存在添加view的容器中，不removeView不会有什么情况，
		// 是不过在你要想在addView上次的view之前必须先把removeView
		// 掉，否则会提示你view已存在异常
		ViewGroup parent = (ViewGroup) rootView.getParent();
		if (parent != null) {
			parent.removeView(rootView);
		}

		localBtn = (Button) rootView.findViewById(R.id.local_btn);
		// checkBtn = (Button)rootView.findViewById(R.id.check_btn);
		// versionTv = (TextView)rootView.findViewById(R.id.version_tv);
		tipTv = (TextView) rootView.findViewById(R.id.tip_tv);
		// titleTv = (TextView)rootView.findViewById(R.id.version_title);
		// descTv = (TextView)rootView.findViewById(R.id.version_desc);
		// downloadPrecent =
		// (TextView)rootView.findViewById(R.id.download_precent);
		// downloadSize = (TextView)findViewById(R.id.download_size);
		// downloadProgress =
		// (ProgressBar)rootView.findViewById(R.id.download_progress);

		// zzz descTv.setMovementMethod(new ScrollingMovementMethod());
		String verion = Build.VERSION.RELEASE;
		// versionTv.setText(res.getString(R.string.current_version) + " " +
		// firmVersion);
		// zzz versionTv.setText(firmVersion);

		final Runnable getSCodeRunnable = new Runnable() {
			@Override
			public void run() {
				isSCode = true;
				urlPath = Constants.HOST + Constants.API_ACTIVATE + "?email=" + email + "&password=" + password
						+ "&client_type=device" + "&login_type=bind_device" + "&lang=en";

				LogUtils.e("path= " + urlPath);

				Message msg = mHandler.obtainMessage();

				String jsonString = HttpUtils.doGet(urlPath);
				LogUtils.e("jsonString= " + jsonString);
				if (!TextUtils.isEmpty(jsonString)) {
					try {
						dataResponse = new JSONObject(jsonString);
						String result = dataResponse.getString("ret");
						msg.what = Integer.parseInt(result);
						Bundle bundle = new Bundle();

						if (!TextUtils.isEmpty(result)) {
							if (result.equals("0")) {
								String scode = dataResponse.getString("scode");
								bundle.putString("scode", scode);
							} else {
								String msgStr = dataResponse.getString("msg");
								bundle.putString("msg", msgStr);
							}
						}

						msg.setData(bundle);
						msg.sendToTarget();

					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

			}
		};

		final Runnable getAppInfoRunnable = new Runnable() {
			@Override
			public void run() {
				isSCode = false;
				urlPath = Constants.HOST + Constants.API_APP_UPGRADE + "?scode=" + scode + "&app1=update.zip,"
						+ firmVersion + ","; // firmVersion匹配reqver

				LogUtils.e("path= " + urlPath);

				Message msg = mHandler.obtainMessage();

				String jsonString = HttpUtils.doGet(urlPath);
				LogUtils.e("app info jsonString= " + jsonString);
				if (!TextUtils.isEmpty(jsonString)) {
					try {

						Gson gson = new Gson();
						appInfoListResp = gson.fromJson(jsonString, AppInfoListResp.class);

						int result = appInfoListResp.getRet();
						LogUtils.e("result=" + result);

						msg.what = result;
						Bundle bundle = new Bundle();

						if (result != 0) {
							String msgStr = appInfoListResp.getMsg();
							bundle.putString("msg", msgStr);
						}

						msg.setData(bundle);
						msg.sendToTarget();

					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}
		};

		localBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Toast.makeText(mContext, "Local!",
				// Toast.LENGTH_SHORT).show();
				// Intent intent = new Intent(mContext, FileSelector.class);
				// intent.putExtra(FileSelector.ROOT, "/mnt");
				// startActivityForResult(intent, 0);

				
			}
		});

		/*
		 * checkBtn.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View arg0) {
		 * 
		 * boolean isNetwordAvailable = CommonUtils.getNetWorkStatus(mContext);
		 * if(isNetwordAvailable) {
		 * 
		 * if(isDownload) { if(appInfo!=null) { File file = new File(filePath);
		 * if(file!=null && file.exists()) { file.delete(); }
		 * 
		 * tipTv.setVisibility(View.INVISIBLE);
		 * downloadPrecent.setVisibility(View.VISIBLE);
		 * downloadProgress.setVisibility(View.VISIBLE);
		 * checkBtn.setText(res.getString(R.string.is_downloading));
		 * checkBtn.setEnabled(false); startDownload(); } }else {
		 * if(!TextUtils.isEmpty(scode)) { //如果有保存scode,访问服务器获取升级信息 new
		 * Thread(getAppInfoRunnable).start(); }else{ //否则先获取scode new
		 * Thread(getSCodeRunnable).start(); }
		 * 
		 * } }else { Toast.makeText(mContext,
		 * res.getString(R.string.network_invalid), Toast.LENGTH_LONG).show(); }
		 * 
		 * } });
		 */

		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				LogUtils.e("msg.what= " + msg.what);
				Bundle bundle = null;
				bundle = msg.getData();

				if (msg.what == 0) {

					if (isSCode) { // 获取SCode成功
						scode = bundle.getString("scode");
						LogUtils.e("scode=" + scode);

						// 保存scode
						SharedPreferences settings = mContext.getSharedPreferences(Constants.UPDATE_PREFS, 0);
						SharedPreferences.Editor editor = settings.edit();
						editor.putString("Scode", scode);
						editor.commit();

						new Thread(getAppInfoRunnable).start();

					} else { // 获取AppInfo成功

						List<AppInfo> appInfos = appInfoListResp.getApps();
						if (appInfos != null && appInfos.size() > 0) {
							for (int i = 0; i < appInfos.size(); i++) {
								if (appInfos.get(i).getPkgname().equals(Constants.APP_UPDATE_ZIP)) { // 找到pkgname=update.zip
									appInfo = appInfos.get(i);

									isDownload = true;

									// checkBtn.setText(res.getString(R.string.update));
									tipTv.setVisibility(View.VISIBLE);
									String fileSize = FileUtils.getAppSize(Long.parseLong(appInfo.getPkgsize()))
											.toString();
									tipTv.setText("检测到新版本 " + appInfo.getPkgver() + " (" + fileSize + ")");
									titleTv.setVisibility(View.VISIBLE);
									descTv.setVisibility(View.VISIBLE);
									descTv.setText(appInfo.getRemarks());
								}
							}
						}

						if (appInfo == null) {
							tipTv.setVisibility(View.GONE);
							// tipTv.setText("已是最新版本" );
							Toast.makeText(mContext, "已是最新版本", Toast.LENGTH_SHORT).show();
						}
					}

				} else {
					scode = "";
					SharedPreferences settings = mContext.getSharedPreferences(Constants.UPDATE_PREFS, 0);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString("Scode", scode);
					editor.commit();
					String msgStr = bundle.getString("msg");
					Toast.makeText(mContext, msgStr, Toast.LENGTH_LONG).show();
				}

			}
		};

		dlHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				switch (msg.what) {
				case 0:
					int status = (Integer) msg.obj;

					if (isDownloading(status)) { // 下载状态显示进度条，隐藏下载按钮

						// downloadProgress.setVisibility(View.VISIBLE);
						downloadProgress.setMax(0);
						downloadProgress.setProgress(0);
						// downloadButton.setVisibility(View.GONE);
						// downloadSize.setVisibility(View.VISIBLE);
						// downloadPrecent.setVisibility(View.VISIBLE);
						// downloadCancel.setVisibility(View.VISIBLE);
						// downloadTip.setText(res.getString(R.string.download_tip));

						if (msg.arg2 < 0) { // 文件总大小<0
							downloadProgress.setIndeterminate(true);
							downloadPrecent.setText("0M/0M (0%)");
							// downloadSize.setText("0M/0M");

						} else { // 下载中 ，显示大小，
							downloadProgress.setIndeterminate(false);
							downloadProgress.setMax(msg.arg2);
							downloadProgress.setProgress(msg.arg1);
							downloadPrecent
									.setText(FileUtils.getAppSize(msg.arg1) + "/" + FileUtils.getAppSize(msg.arg2)
											+ " (" + FileUtils.getNotiPercent(msg.arg1, msg.arg2) + ")"); // 设置百分比
							// downloadSize.setText(); //设置文件大小比。
							// downloadTip.setText(res.getString(R.string.downloading));
						}

					} else { // 不是下载中状态，就隐藏进度条等，显示下载按钮并改变文字

						// downloadProgress.setVisibility(View.GONE);
						// downloadProgress.setMax(0);
						// downloadProgress.setProgress(0);
						// downloadButton.setVisibility(View.VISIBLE);
						// downloadSize.setVisibility(View.GONE);
						// downloadPrecent.setVisibility(View.GONE);
						// downloadCancel.setVisibility(View.GONE);

						if (status == DownloadManager.STATUS_FAILED) {
							// downloadButton.setText("下载失败");
							// downloadTip.setText(res.getString(R.string.download_fail));
							downloadPrecent.setText(res.getString(R.string.download_fail));

						} else if (status == DownloadManager.STATUS_SUCCESSFUL) {
							// downloadButton.setText("下载成功");
							// downloadTip.setText(res.getString(R.string.download_success));
							// upgradeBtn.setEnabled(true);

						} else {
							// downloadButton.setText("下载");
							// downloadTip.setText(res.getString(R.string.download_tip));
						}
					}
					break;
				}
			}
		};

		return rootView;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			Bundle bundle = data.getExtras();
			String file = bundle.getString("file");
			if (file != null) {

				showUpgradeDialog(file);
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			if (receiver != null) {
				mContext.unregisterReceiver(receiver);
			}
			if (downloadObserver != null) {
				mContext.getContentResolver().unregisterContentObserver(downloadObserver);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void showUpgradeDialog(String filePath) {
		final Dialog dlg = new Dialog(mContext, android.R.style.Theme_Holo_Light_Dialog);
		dlg.setTitle(R.string.confirm_update);
		LayoutInflater inflater = LayoutInflater.from(mContext);
		InstallPackage dlgView = (InstallPackage) inflater.inflate(R.layout.install_ota, null, false);
		dlgView.setPackagePath(filePath);
		dlg.setContentView(dlgView);
		dlg.findViewById(R.id.confirm_cancel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dlg.dismiss();
			}
		});
		Window dialogWindow = dlg.getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.width = 770; // 宽度
		lp.height = 535;
		dialogWindow.setAttributes(lp);
		dlg.setCanceledOnTouchOutside(false);
		dlg.show();
	}

	public void startDownload() {

		dowanloadmanager = (DownloadManager) mContext.getSystemService(android.content.Context.DOWNLOAD_SERVICE);

		// Uri uri = Uri.parse("http://commonsware.com/misc/test.mp4");
		Uri uri = Uri.parse(appInfo.getPkgurl());

		Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdir();

		Log.e("", "path1=" + Environment.DIRECTORY_DOWNLOADS);
		Log.e("", "path2=" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));

		lastDownloadId = dowanloadmanager
				.enqueue(new DownloadManager.Request(uri)
						.setAllowedNetworkTypes(
								DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI)
						.setAllowedOverRoaming(false)
						.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
						.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, Constants.APP_UPDATE_ZIP));

		mContext.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

		downloadObserver = new DownloadChangeObserver(null);
		mContext.getContentResolver().registerContentObserver(CONTENT_URI, true, downloadObserver);

	}

	private void queryDownloadStatus() {

		DownloadManager.Query query = new DownloadManager.Query();
		query.setFilterById(lastDownloadId);

		Cursor c = dowanloadmanager.query(query);
		if (c != null && c.moveToFirst()) {

			int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)); // 下载状态

			int reasonIdx = c.getColumnIndex(DownloadManager.COLUMN_REASON);

			int titleIdx = c.getColumnIndex(DownloadManager.COLUMN_TITLE); // 下载文件名字
			int fileSizeIdx = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES); // 下载文件大小
			int bytesDLIdx = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR); // 已经下载大小

			String title = c.getString(titleIdx);
			// int fileSize = c.getInt(fileSizeIdx); //文件总大小
			int fileSize = Integer.parseInt(appInfo.getPkgsize());
			int bytesDL = c.getInt(bytesDLIdx); // 文件已下载大小

			// Translate the pause reason to friendly text.
			int reason = c.getInt(reasonIdx);
			StringBuilder sb = new StringBuilder();
			sb.append(title).append("\n");
			sb.append("Downloaded ").append(bytesDL).append(" / ").append(fileSize);

			// Display the status
			LogUtils.e(sb.toString());

			switch (status) {
			case DownloadManager.STATUS_PAUSED:
				LogUtils.e("STATUS_PAUSED");
				dlHandler.sendMessage(dlHandler.obtainMessage(0, bytesDL, fileSize, DownloadManager.STATUS_PAUSED));

			case DownloadManager.STATUS_PENDING:
				LogUtils.e("STATUS_PENDING");
				dlHandler.sendMessage(dlHandler.obtainMessage(0, bytesDL, fileSize, DownloadManager.STATUS_PENDING));

			case DownloadManager.STATUS_RUNNING:
				// 正在下载，不做任何事情
				LogUtils.e("STATUS_RUNNING");
				dlHandler.sendMessage(dlHandler.obtainMessage(0, bytesDL, fileSize, DownloadManager.STATUS_RUNNING));
				break;

			case DownloadManager.STATUS_SUCCESSFUL:
				// 完成
				LogUtils.e("下载完成");
				dlHandler.sendMessage(dlHandler.obtainMessage(0, bytesDL, fileSize, DownloadManager.STATUS_SUCCESSFUL));

				break;

			case DownloadManager.STATUS_FAILED:
				// 清除已下载的内容，重新下载
				LogUtils.e("STATUS_FAILED");
				dowanloadmanager.remove(lastDownloadId);
				dlHandler.sendMessage(dlHandler.obtainMessage(0, bytesDL, fileSize, DownloadManager.STATUS_FAILED));
				break;
			}

			c.close();
		}
	}

	public void startUpgrade() {
		File file = new File(filePath);
		if (file != null) {

			LogUtils.e("app md5=" + appInfo.getPkgmd5());

			try {
				String md5 = MD5FileUtil.getFileMD5String(file);
				LogUtils.e("file md5=" + md5);

				if (md5.equals(appInfo.getPkgmd5())) {
					// Toast.makeText(OtaOnlineActivity.this, "升级包校验成功!",
					// Toast.LENGTH_LONG).show();
					LogUtils.e("md5 ==");

					showUpgradeDialog(filePath);

				} else {

					Toast.makeText(mContext, "升级包校验失败,请重新下载!", Toast.LENGTH_LONG).show();
					LogUtils.e("md5 !=");

				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	public boolean isDownloading(int downloadManagerStatus) {
		return downloadManagerStatus == DownloadManager.STATUS_RUNNING
				|| downloadManagerStatus == DownloadManager.STATUS_PAUSED
				|| downloadManagerStatus == DownloadManager.STATUS_PENDING;
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 这里可以取得下载的id，这样就可以知道哪个文件下载完成了。适用与多个下载任务的监听
			Log.e("tag",
					"DownloadManager.EXTRA_DOWNLOAD_ID=" + intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0));
			Log.e("", "downlaod onReceive************************************************************** ");
			queryDownloadStatus();
			// zzz checkBtn.setText("升级");
			// zzz checkBtn.setEnabled(true);
			startUpgrade();
		}
	};

	class DownloadChangeObserver extends ContentObserver {

		public DownloadChangeObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			Log.e("", "downlaod on change ------------------------------------------------------- ");
			queryDownloadStatus();
		}

	}

}
