package com.golo.launch.download;
/*
 * tel.getNetworkOperator()
 3G�й���460�̶���,
 �й��ƶ����� 46000
 �й���ͨ���� 46001
 �й����ŵ��� 46003
 *��ȡ����
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
		 * �� �ο�һ����صķ������õ��Լ���Ҫ�Ĳ����������Լ���ui public void initListValues(){
		 * tel.getDeviceId());//��ȡ�豸��� tel.getSimCountryIso());//��ȡSIM������
		 * tel.getSimSerialNumber());//��ȡSIM�����к�
		 * (simState[tm.getSimState()]);//��ȡSIM��״̬
		 * (tel.getDeviceSoftwareVersion()!=null?tm.getDeviceSoftwareVersion():
		 * "δ֪")); //��ȡ����汾 tel.getNetworkOperator());//��ȡ������Ӫ�̴���
		 * tel.getNetworkOperatorName());//��ȡ������Ӫ������
		 * (phoneType[tm.getPhoneType()]);//��ȡ�ֻ���ʽ
		 * tel.getCellLocation().toString());//��ȡ�豸��ǰλ�� }
		 */
		tel = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

		getmark();
		// ���ü����¼��������ź�ǿ�ȵĸı��״̬�ĸı�
		tel.listen(new PhoneStateMonitor(),
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_SERVICE_STATE);
		inflater = LayoutInflater.from(this.mContext);
		View convertView = inflater.inflate(R.layout.main, null);
		tv_3gsignal=(TextView)convertView.findViewById(R.id.tv_3gsignal);
	}

//	private Handler handler2 = new Handler() {
//		public void handleMessage(Message msg) {
//			switch (msg.what) {
//			case 4: // ��������Խ���UI����
//				tv_3gsignal.setText("asdasdsad");
//				break;
//			default:
//				break;
//			}
//		}
//	};
	private void getmark()// �õ���ǰ�绰���Ĺ�����Ӫ��
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
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {// 3g�ź�ǿ�ȵĸı�
			super.onSignalStrengthsChanged(signalStrength);
			/*
			 * signalStrength.isGsm() �Ƿ�GSM�ź� 2G or 3G
			 * signalStrength.getCdmaDbm(); ��ͨ3G �ź�ǿ��
			 * signalStrength.getCdmaEcio(); ��ͨ3G �ظɱ�
			 * signalStrength.getEvdoDbm(); ����3G �ź�ǿ��
			 * signalStrength.getEvdoEcio(); ����3G �ظɱ�
			 * signalStrength.getEvdoSnr(); ����3G �����
			 * signalStrength.getGsmSignalStrength(); 2G �ź�ǿ��
			 * signalStrength.getGsmBitErrorRate(); 2G ������ �ظɱ�
			 * ������ָ����ģ��粨�е��ź��������ı�ֵ
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
			if (mark == 2) {// ����3g�ź�ǿ�ȵķ��࣬���԰���ui���л��ֵȼ�
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
			if (mark == 1) {// ��ͨ3g�źŻ���
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
			if (mark == 0) {// �ƶ�2g�źŵĻ���
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
			//Log.v("zzzzb", "������Ϊ��" + mark + ";�����ź�ǿ��Ϊ:" + position);
//			Log.v("zzzzb", "tv_3gsignal:+"+tv_3gsignal);
//			Message message = new Message();
//			message.what = 4;
//			handler2.sendMessage(message);
		}
	}
}
