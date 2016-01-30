package com.golo.launch.download;
/*
 * tel.getNetworkOperator()
 3G中国是460固定的,
 中国移动的是 46000
 中国联通的是 46001
 中国电信的是 46003
 *获取国别
 tel.getSimCountryIso()
 */

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class My3GInfo {
	private Context mContext;
	private String STRNetworkOperator[] = { "46000", "46001", "46003" };
	private int mark = -1;
	private int position=0;
	private TelephonyManager tel;
	private LayoutInflater inflater = null;
	TextView tv_3gsignal;
	public My3GInfo(Context context) {
		mContext = context;
		init();
	}

	public int getposition(){
		return position;
	}
	private void init() {
		// TODO Auto-generated method stub
		/*
		 * 可 参考一下相关的方法，得到自己想要的参数来处理自己的ui public void initListValues(){
		 * tel.getDeviceId());//获取设备编号 tel.getSimCountryIso());//获取SIM卡国别
		 * tel.getSimSerialNumber());//获取SIM卡序列号
		 * (simState[tm.getSimState()]);//获取SIM卡状态
		 * (tel.getDeviceSoftwareVersion()!=null?tm.getDeviceSoftwareVersion():
		 * "未知")); //获取软件版本 tel.getNetworkOperator());//获取网络运营商代号
		 * tel.getNetworkOperatorName());//获取网络运营商名称
		 * (phoneType[tm.getPhoneType()]);//获取手机制式
		 * tel.getCellLocation().toString());//获取设备当前位置 }
		 */
		tel = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

		getmark();
		// 设置监听事件，监听信号强度的改变和状态的改变
		tel.listen(new PhoneStateMonitor(),
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_SERVICE_STATE);
		inflater = LayoutInflater.from(this.mContext);
		View convertView = inflater.inflate(R.layout.main, null);
		tv_3gsignal=(TextView)convertView.findViewById(R.id.tv_3gsignal);
	}

//	private Handler handler2 = new Handler() {
//		public void handleMessage(Message msg) {
//			switch (msg.what) {
//			case 4: // 在这里可以进行UI操作
//				tv_3gsignal.setText("asdasdsad");
//				break;
//			default:
//				break;
//			}
//		}
//	};
	private void getmark()// 得到当前电话卡的归属运营商
	{
		String strNetworkOperator = tel.getNetworkOperator();
		if (strNetworkOperator != null) {
			for (int i = 0; i < 3; i++) {
				if (strNetworkOperator.equals(STRNetworkOperator[i])) {
					mark = i;
					Log.v(TAG, "mark==" + i);
					break;
				}
			}
		} else {
			mark = -1;
		}
	}

	private String TAG = "zzzzb";
	int signal;

	class PhoneStateMonitor extends PhoneStateListener {
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {// 3g信号强度的改变
			super.onSignalStrengthsChanged(signalStrength);
			/*
			 * signalStrength.isGsm() 是否GSM信号 2G or 3G
			 * signalStrength.getCdmaDbm(); 联通3G 信号强度
			 * signalStrength.getCdmaEcio(); 联通3G 载干比
			 * signalStrength.getEvdoDbm(); 电信3G 信号强度
			 * signalStrength.getEvdoEcio(); 电信3G 载干比
			 * signalStrength.getEvdoSnr(); 电信3G 信噪比
			 * signalStrength.getGsmSignalStrength(); 2G 信号强度
			 * signalStrength.getGsmBitErrorRate(); 2G 误码率 载干比
			 * ，它是指空中模拟电波中的信号与噪声的比值
			 */
			// Log.v(TAG, "change signal");
			if (mark < 0) {
				getmark();
			}
			if (mark == 0) {
				signal = signalStrength.getGsmSignalStrength();
				//Log.v("zzzzb", "signal:" + signal);
			} else if (mark == 1) {
				signal = signalStrength.getCdmaDbm();
				//Log.v("zzzzb", "signal:" + signal);
			} else if (mark == 2) {
				signal = signalStrength.getEvdoDbm();
				//Log.v("zzzzb", "signal:" + signal);
			}
			getLevel();
		}

		private void getLevel() {
			// TODO Auto-generated method stub
			if (mark == 2) {// 电信3g信号强度的分类，可以按照ui自行划分等级
				if (signal >= -65)
					position = 5;
				else if (signal >= -75)
					position = 4;
				else if (signal >= -85)
					position = 3;
				else if (signal >= -95)
					position = 2;
				else if (signal >= -105)
					position = 1;
				else
					position = 0;
			}
			if (mark == 1) {// 联通3g信号划分
				if (signal >= -75)
					position = 5;
				else if (signal >= -80)
					position = 4;
				else if (signal >= -85)
					position = 3;
				else if (signal >= -95)
					position = 2;
				else if (signal >= -100)
					position = 1;
				else
					position = 0;
			}
			if (mark == 0) {// 移动2g信号的划分
				if (signal <= 2 || signal == 99)
					position = 0;
				else if (signal >= 12)
					position = 5;
				else if (signal >= 10)
					position = 4;
				else if (signal >= 8)
					position = 3;
				else if (signal >= 5)
					position = 2;
				else
					position = 1;
			}
			//Log.v("zzzzb", "卡类型为：" + mark + ";网络信号强度为:" + position);
//			Log.v("zzzzb", "tv_3gsignal:+"+tv_3gsignal);
//			Message message = new Message();
//			message.what = 4;
//			handler2.sendMessage(message);
		}
	}
}
