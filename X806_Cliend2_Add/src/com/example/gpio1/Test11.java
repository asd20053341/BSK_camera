package com.example.gpio1;

import com.example.x806.IPerson;

import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

public class Test11 {
	private static final String TAG = "X806Cliend";
	private volatile boolean connected = false; // 串口是否已经连接
	private boolean startRecved = false; // 是否已开始接收
	private volatile boolean connected2 = false; // 串口是否已经连接
	private boolean startRecved2 = false; // 是否已开始接收
	private static IPerson iPerson1;
	private static Test11 instance;
	// 从下位机读取数据保存
	public int recvFromSlave1[] = new int[30];
	public char recvFromSlaveChar1[] = new char[30];
	public int recvFromSlave2[] = new int[30];
	public char recvFromSlaveChar2[] = new char[30];
	private Object lock = new Object(); // 线程同步锁
	public static int SerialInt = 0;
	public static int SerialInt2 = 0;

	static {
		// 单例模式
		if (instance == null) {
			instance = new Test11();
		}
	}

	public static Test11 getInstance(IPerson iPerson) {
		iPerson1 = iPerson;
		return instance;
	}

	/**
	 * 连接串口，接收数据1
	 * 
	 * @param recvObj
	 */
	public void recvData(final IX806masterRecv recvObj) {
		Log.v(TAG, "recvData");
		// 避免多线程引用导致混乱
		if (startRecved) {
			return;
		}
		startRecved = true;
		// 子线程接收
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
					// recvObj.disconnected("读卡过程中发生异常，准备重连。" + e.getMessage());
					// connected = false;
					// }
					// } else {

					// 连接不上就死连（现在只连1次）
					if (!connected) {
						try {
							connect(recvObj);
							// 休眠5秒再连接
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
	 * 连接串口，接收数据2
	 * 
	 * @param recvObj
	 */
	public void recvData2(final IX806masterRecv recvObj) {
		Log.v(TAG, "recvData2");
		// 避免多线程引用导致混乱
		if (startRecved2) {
			return;
		}
		startRecved2 = true;
		// 子线程接收
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
					// recvObj.disconnected("读卡过程中发生异常，准备重连。" + e.getMessage());
					// connected2 = false;
					// }
					// } else {

					// 连接不上就死连（现在只连1次）
					if (!connected2) {
						try {
							connect2(recvObj);
							// 休眠5秒再连接
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
	 * 接收数据处理，没用到
	 */
	private void recvValue(IX806masterRecv recvObj) {
		// Log.v(TAG, "recvValue(IX806masterRecv recvObj)");
		// 单独只读一位，来降低CPU消耗,readWrite('R', ' ') != 0 说明接收数据失败，只有等于0时才说明有数据
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
		// 接收的类型
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
	 * 接收数据处理1
	 */
	public void recvValue(final X806GetData data) {
		// Log.v(TAG, "recvValue(IX806masterRecv recvObj)");
		// 单独只读一位，来降低CPU消耗,readWrite('R', ' ') != 0 说明接收数据失败，只有等于0时才说明有数据
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
		// 接收的类型
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
	 * 接收数据处理2
	 */
	public void recvValue2(final X806GetData data) {
		// Log.v(TAG, "recvValue(IX806masterRecv recvObj)");
		// 单独只读一位，来降低CPU消耗,readWrite('R', ' ') != 0 说明接收数据失败，只有等于0时才说明有数据
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

		// 接收的类型
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
	 * 清理缓存数组1
	 */
	private void cleanRecvBuffer1() {
		recvFromSlave1 = new int[30];
		recvFromSlaveChar1 = new char[30];
	}

	/**
	 * 清理缓存数组2
	 */
	private void cleanRecvBuffer2() {
		recvFromSlave2 = new int[30];
		recvFromSlaveChar2 = new char[30];
	}

	/**
	 * 连接串口1
	 */
	private void connect(final IX806masterRecv recvObj) {
		try {
			if ((SerialInt = iPerson1.openSerialTo(1)) < 0) {
				Log.v(TAG, "#############serial open err#############");
				connected = true;
				recvObj.disconnected("下位机串口1打开失败！！！");
			} else {
				if (iPerson1.setSerialTo(115200, 8, 'N', 1, SerialInt) < 0) {
					Log.v(TAG, "#############setial set err#############");
					connected = true;
					recvObj.disconnected("下位机串口1设置失败！！！");
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
	 * 连接串口2
	 */
	private void connect2(final IX806masterRecv recvObj) {
		try {
			if ((SerialInt2 = iPerson1.openSerialTo(2)) < 0) {
				Log.v(TAG, "#############serial open err#############");
				//在MainActivity里将尝试修改三次，所以这里就设置连接一次就不连了
				connected2 = true;
				recvObj.disconnected("下位机串口2打开失败！！！");			
			} else {
				if (iPerson1.setSerialTo(115200, 8, 'N', 1, SerialInt2) < 0) {
					Log.v(TAG, "#############setial set err#############");
					connected2 = true;
					recvObj.disconnected("下位机串口2设置失败！！！");				
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
	 * 封装接口用于回调
	 * 
	 * @author gzdlw
	 */
	public interface IX806masterRecv {
		/**
		 * 当接收到数据时
		 * 
		 * @param recvType
		 *            数据类型
		 * @param data
		 *            数据
		 */
		public void received(String data);

		/**
		 * 串口连接成功
		 */
		public void connected();

		/**
		 * 串口连接失败
		 * 
		 * @param error
		 *            错误信息
		 */
		public void disconnected(String error);
	}

	/**
	 * 封装接口用于回调
	 * 
	 * @author gzdlw
	 */
	public interface X806GetData {
	/**
	 * 接收数据返回给MainActivity做对比是否有OK
	 * @param data
	 */
		public void getData(char[] data);

	}
}
