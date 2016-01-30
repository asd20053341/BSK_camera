package com.example.blacklist;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Debug.MemoryInfo;

//Model类 ，用来存储应用程序信息
public class RunningAppInfo {
  
	private String appLabel;    //应用程序标签
	private Drawable appIcon ;  //应用程序图像
	private String pkgName ;    //应用程序所对应的包名
	private double mInfo;
	private int pid ;  //该应用程序所在的进程号
	private String processName ;  // 该应用程序所在的进程名
	
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
