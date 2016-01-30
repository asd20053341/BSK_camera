package com.example.blacklist;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Debug.MemoryInfo;

//Model�� �������洢Ӧ�ó�����Ϣ
public class RunningAppInfo {
  
	private String appLabel;    //Ӧ�ó����ǩ
	private Drawable appIcon ;  //Ӧ�ó���ͼ��
	private String pkgName ;    //Ӧ�ó�������Ӧ�İ���
	private double mInfo;
	private int pid ;  //��Ӧ�ó������ڵĽ��̺�
	private String processName ;  // ��Ӧ�ó������ڵĽ�����
	
	public RunningAppInfo(){}
	
	public String getAppLabel() {
		return appLabel;
	}
	public void setAppLabel(String appName) {
		this.appLabel = appName;
	}
	public Drawable getAppIcon() {
		return appIcon;
	}
	public void setAppIcon(Drawable appIcon) {
		this.appIcon = appIcon;
	}
	public String getPkgName(){
		return pkgName ;
	}
	public void setPkgName(String pkgName){
		this.pkgName=pkgName ;
	}

	public int getPid() {
		return pid;
	}
	public double getMemoryInfo() {
		return mInfo;
	}
	public void setMemoryInfo(double mInfo) {
		this.mInfo=mInfo;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}
	
	
}
