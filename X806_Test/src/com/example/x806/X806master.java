package com.example.x806;

import android.os.RemoteException;
import android.util.Log;

public class X806master extends IPerson.Stub {
	private static final String TAG = "X806master";

	static {
		// JNI
		System.loadLibrary("x806master");
	}

	/**
	 * 打开串口，3代表/dev/ttyS2
	 * 
	 * @param port
	 * @return
	 */
	public native int openSerial(int port);

	/**
	 * 设置串口参数
	 * 
	 * @param nSpeed
	 * @param nBits
	 * @param nEvent
	 * @param nStop
	 * @return
	 */
	public native int setSerial(int nSpeed, int nBits, char nEvent, int nStop,int fd);

	/**
	 * 读写操作，读操作的值由getSlaveValue()方法获取
	 * 
	 * @param op
	 * @param data
	 * @return
	 */
	public native int readWrite(char op, char data, int fd);

	/**
	 * 读取最近一次readWrite操作的值
	 * 
	 * @return
	 */
	public native int getSlaveValue();

	/**
	 * 获取so版本号
	 * 
	 * @return
	 */
	public native String getVersionName();

	/**
	 * 接收命令
	 */
	@Override
	public int recvValueTo(int fd) throws RemoteException {
		// TODO Auto-generated method stub
		Log.v(TAG, "recvValueTo");
		return readWrite('R', ' ',fd);
	}

	/**
	 * 发送命令
	 * 
	 * @param command
	 */
	@Override
	public int sendDataInt(int command,int fd) throws RemoteException {
		// TODO Auto-generated method stub
		// readWrite('W', command));
		Log.v(TAG, "sendDataInt");
		return 111111;
	}

	/**
	 * 发送命令
	 */
	@Override
	public int sendDataChar(char command,int fd) throws RemoteException {
		// TODO Auto-generated method stub
		Log.v(TAG, "sendDataChar");
		return readWrite('W', command,fd);
	}

	/**
	 * 打开串口
	 */
	@Override
	public int openSerialTo(int port) throws RemoteException {
		// TODO Auto-generated method stub
		Log.v(TAG, "openSerialTo");
		return openSerial(port);
	}

	/**
	 * 设置串口
	 */
	@Override
	public int setSerialTo(int nSpeed, int nBits, char nEvent, int nStop,int fd) throws RemoteException {
		// TODO Auto-generated method stub
		Log.v(TAG, "setSerialTo");
		return setSerial(nSpeed, nBits, nEvent, nStop,fd);
	}

	/**
	 * 获取数据
	 */
	@Override
	public int getSlaveValueTo() throws RemoteException {
		// TODO Auto-generated method stub
		Log.v(TAG, "getSlaveValueTo");
		return getSlaveValue();
	}

}
