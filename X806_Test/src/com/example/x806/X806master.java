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
	 * �򿪴��ڣ�3����/dev/ttyS2
	 * 
	 * @param port
	 * @return
	 */
	public native int openSerial(int port);

	/**
	 * ���ô��ڲ���
	 * 
	 * @param nSpeed
	 * @param nBits
	 * @param nEvent
	 * @param nStop
	 * @return
	 */
	public native int setSerial(int nSpeed, int nBits, char nEvent, int nStop,int fd);

	/**
	 * ��д��������������ֵ��getSlaveValue()������ȡ
	 * 
	 * @param op
	 * @param data
	 * @return
	 */
	public native int readWrite(char op, char data, int fd);

	/**
	 * ��ȡ���һ��readWrite������ֵ
	 * 
	 * @return
	 */
	public native int getSlaveValue();

	/**
	 * ��ȡso�汾��
	 * 
	 * @return
	 */
	public native String getVersionName();

	/**
	 * ��������
	 */
	@Override
	public int recvValueTo(int fd) throws RemoteException {
		// TODO Auto-generated method stub
		Log.v(TAG, "recvValueTo");
		return readWrite('R', ' ',fd);
	}

	/**
	 * ��������
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
	 * ��������
	 */
	@Override
	public int sendDataChar(char command,int fd) throws RemoteException {
		// TODO Auto-generated method stub
		Log.v(TAG, "sendDataChar");
		return readWrite('W', command,fd);
	}

	/**
	 * �򿪴���
	 */
	@Override
	public int openSerialTo(int port) throws RemoteException {
		// TODO Auto-generated method stub
		Log.v(TAG, "openSerialTo");
		return openSerial(port);
	}

	/**
	 * ���ô���
	 */
	@Override
	public int setSerialTo(int nSpeed, int nBits, char nEvent, int nStop,int fd) throws RemoteException {
		// TODO Auto-generated method stub
		Log.v(TAG, "setSerialTo");
		return setSerial(nSpeed, nBits, nEvent, nStop,fd);
	}

	/**
	 * ��ȡ����
	 */
	@Override
	public int getSlaveValueTo() throws RemoteException {
		// TODO Auto-generated method stub
		Log.v(TAG, "getSlaveValueTo");
		return getSlaveValue();
	}

}
