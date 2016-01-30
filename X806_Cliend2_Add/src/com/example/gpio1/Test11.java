package com.example.gpio1;

import com.example.x806.IPerson;

import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

public class Test11 {
	private static final String TAG = "X806Cliend";
	private volatile boolean connected = false; // �����Ƿ��Ѿ�����
	private boolean startRecved = false; // �Ƿ��ѿ�ʼ����
	private volatile boolean connected2 = false; // �����Ƿ��Ѿ�����
	private boolean startRecved2 = false; // �Ƿ��ѿ�ʼ����
	private static IPerson iPerson1;
	private static Test11 instance;
	// ����λ����ȡ���ݱ���
	public int recvFromSlave1[] = new int[30];
	public char recvFromSlaveChar1[] = new char[30];
	public int recvFromSlave2[] = new int[30];
	public char recvFromSlaveChar2[] = new char[30];
	private Object lock = new Object(); // �߳�ͬ����
	public static int SerialInt = 0;
	public static int SerialInt2 = 0;

	static {
		// ����ģʽ
		if (instance == null) {
			instance = new Test11();
		}
	}

	public static Test11 getInstance(IPerson iPerson) {
		iPerson1 = iPerson;
		return instance;
	}

	/**
	 * ���Ӵ��ڣ���������1
	 * 
	 * @param recvObj
	 */
	public void recvData(final IX806masterRecv recvObj) {
		Log.v(TAG, "recvData");
		// ������߳����õ��»���
		if (startRecved) {
			return;
		}
		startRecved = true;
		// ���߳̽���
		new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				while (true) {
					// if (connected) {
					// try {
					// recvValue(recvObj);
					// } catch (Exception e) {
					// e.printStackTrace();
					// recvObj.disconnected("���������з����쳣��׼��������" + e.getMessage());
					// connected = false;
					// }
					// } else {

					// ���Ӳ��Ͼ�����������ֻ��1�Σ�
					if (!connected) {
						try {
							connect(recvObj);
							// ����5��������
							Thread.sleep(5 * 1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// }
					}
				}
			}
		}).start();
	}

	/**
	 * ���Ӵ��ڣ���������2
	 * 
	 * @param recvObj
	 */
	public void recvData2(final IX806masterRecv recvObj) {
		Log.v(TAG, "recvData2");
		// ������߳����õ��»���
		if (startRecved2) {
			return;
		}
		startRecved2 = true;
		// ���߳̽���
		new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				while (true) {
					// if (connected2) {
					// try {
					// // recvValue(recvObj);
					// } catch (Exception e) {
					// e.printStackTrace();
					// recvObj.disconnected("���������з����쳣��׼��������" + e.getMessage());
					// connected2 = false;
					// }
					// } else {

					// ���Ӳ��Ͼ�����������ֻ��1�Σ�
					if (!connected2) {
						try {
							connect2(recvObj);
							// ����5��������
							Thread.sleep(5 * 1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// }
					}
				}
			}
		}).start();
	}

	/**
	 * �������ݴ���û�õ�
	 */
	private void recvValue(IX806masterRecv recvObj) {
		// Log.v(TAG, "recvValue(IX806masterRecv recvObj)");
		// ����ֻ��һλ��������CPU����,readWrite('R', ' ') != 0 ˵����������ʧ�ܣ�ֻ�е���0ʱ��˵��������
		try {
			if ((iPerson1.recvValueTo(SerialInt)) != 0) {
				// Log.v(TAG, "readWrite('R', ' ')!=0, dropped");
				this.cleanRecvBuffer1();
				return;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			recvFromSlave1[0] = (iPerson1.getSlaveValueTo() & 0XFF);
			Log.v(TAG, "recvValue2222");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.v(TAG, "recvFromSlave[" + 0 + "]:" + recvFromSlave1[0]);
		// ���յ�����
		for (int count = 1; count < 16; count++) {
			try {
				if ((iPerson1.recvValueTo(SerialInt)) == 0) {
					recvFromSlave1[count] = (iPerson1.getSlaveValueTo() & 0XFF);
					Log.v(TAG, "recvFromSlave1[" + count + "]:" + recvFromSlave1[count]);
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		String recvData = "";
		recvObj.received(recvData);
		this.cleanRecvBuffer1();
	}

	/**
	 * �������ݴ���1
	 */
	public void recvValue(final X806GetData data) {
		// Log.v(TAG, "recvValue(IX806masterRecv recvObj)");
		// ����ֻ��һλ��������CPU����,readWrite('R', ' ') != 0 ˵����������ʧ�ܣ�ֻ�е���0ʱ��˵��������
		try {
			if ((iPerson1.recvValueTo(SerialInt)) != 0) {
				Log.v(TAG, "readWrite('R', ' ')!=0, dropped++++" + "   ;SerialInt:" + SerialInt);
				this.cleanRecvBuffer1();
				return;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			recvFromSlave1[0] = (iPerson1.getSlaveValueTo() & 0XFF);
			recvFromSlaveChar1[0]=(char) recvFromSlave1[0];
			Log.v(TAG, "recvValue1111" + "   ;SerialInt:" + SerialInt);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.v(TAG, "recvFromSlaveChar1[" + 0 + "]:" + recvFromSlaveChar1[0]);
		// ���յ�����
		for (int count = 1; count < 30; count++) {
			try {
				if ((iPerson1.recvValueTo(SerialInt)) == 0) {
					recvFromSlave1[count] = (iPerson1.getSlaveValueTo() & 0XFF);
					recvFromSlaveChar1[count] = (char) recvFromSlave1[count];
					Log.v(TAG, "recvFromSlaveChar1[" + count + "]:" + recvFromSlaveChar1[count]);
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		data.getData(recvFromSlaveChar1);
		this.cleanRecvBuffer1();
	}

	/**
	 * �������ݴ���2
	 */
	public void recvValue2(final X806GetData data) {
		// Log.v(TAG, "recvValue(IX806masterRecv recvObj)");
		// ����ֻ��һλ��������CPU����,readWrite('R', ' ') != 0 ˵����������ʧ�ܣ�ֻ�е���0ʱ��˵��������
		try {
			if ((iPerson1.recvValueTo(SerialInt2)) != 0) {
				Log.v(TAG, "readWrite('R', ' ')!=0, dropped++++" + "   ;SerialInt2:" + SerialInt2);
				this.cleanRecvBuffer2();
				return;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			recvFromSlave2[0] = (iPerson1.getSlaveValueTo() & 0XFF);
			recvFromSlaveChar2[0]=(char) recvFromSlave2[0];
			Log.v(TAG, "recvValue2222" + "   ;SerialInt2:" + SerialInt2);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.v(TAG, "recvFromSlaveChar2[" + 0 + "]:" + recvFromSlaveChar2[0]);

		// ���յ�����
		for (int count = 1; count < 30; count++) {
			try {
				if ((iPerson1.recvValueTo(SerialInt2)) == 0) {
					recvFromSlave2[count] = (iPerson1.getSlaveValueTo() & 0XFF);
					recvFromSlaveChar2[count] = (char) recvFromSlave2[count];
					Log.v(TAG, "recvFromSlaveChar2[" + count + "]:" + recvFromSlaveChar2[count]);
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		data.getData(recvFromSlaveChar2);

		this.cleanRecvBuffer2();
	}

	/**
	 * ����������1
	 */
	private void cleanRecvBuffer1() {
		recvFromSlave1 = new int[30];
		recvFromSlaveChar1 = new char[30];
	}

	/**
	 * ����������2
	 */
	private void cleanRecvBuffer2() {
		recvFromSlave2 = new int[30];
		recvFromSlaveChar2 = new char[30];
	}

	/**
	 * ���Ӵ���1
	 */
	private void connect(final IX806masterRecv recvObj) {
		try {
			if ((SerialInt = iPerson1.openSerialTo(1)) < 0) {
				Log.v(TAG, "#############serial open err#############");
				connected = true;
				recvObj.disconnected("��λ������1��ʧ�ܣ�����");
			} else {
				if (iPerson1.setSerialTo(115200, 8, 'N', 1, SerialInt) < 0) {
					Log.v(TAG, "#############setial set err#############");
					connected = true;
					recvObj.disconnected("��λ������1����ʧ�ܣ�����");
				} else {
					Log.v(TAG, "#############setial connected1#############" + "   ;SerialInt:" + SerialInt);
					connected = true;
					recvObj.connected();
				}
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * ���Ӵ���2
	 */
	private void connect2(final IX806masterRecv recvObj) {
		try {
			if ((SerialInt2 = iPerson1.openSerialTo(2)) < 0) {
				Log.v(TAG, "#############serial open err#############");
				//��MainActivity�ｫ�����޸����Σ������������������һ�ξͲ�����
				connected2 = true;
				recvObj.disconnected("��λ������2��ʧ�ܣ�����");			
			} else {
				if (iPerson1.setSerialTo(115200, 8, 'N', 1, SerialInt2) < 0) {
					Log.v(TAG, "#############setial set err#############");
					connected2 = true;
					recvObj.disconnected("��λ������2����ʧ�ܣ�����");				
				} else {
					Log.v(TAG, "#############setial connected2#############" + "   ;SerialInt2:" + SerialInt2);
					connected2 = true;
					recvObj.connected();
				}
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * ��װ�ӿ����ڻص�
	 * 
	 * @author gzdlw
	 */
	public interface IX806masterRecv {
		/**
		 * �����յ�����ʱ
		 * 
		 * @param recvType
		 *            ��������
		 * @param data
		 *            ����
		 */
		public void received(String data);

		/**
		 * �������ӳɹ�
		 */
		public void connected();

		/**
		 * ��������ʧ��
		 * 
		 * @param error
		 *            ������Ϣ
		 */
		public void disconnected(String error);
	}

	/**
	 * ��װ�ӿ����ڻص�
	 * 
	 * @author gzdlw
	 */
	public interface X806GetData {
	/**
	 * �������ݷ��ظ�MainActivity���Ա��Ƿ���OK
	 * @param data
	 */
		public void getData(char[] data);

	}
}
