package com.example.blacklist;

import android.support.v7.app.ActionBarActivity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
//bsk-app-manager11111111
//bsk-app-manager1111111
public class MainActivity extends TabActivity {
	//1.首先定义TabHost对象(不过谷歌已经不推荐用这个类了)
	TabHost tah = null;
	Intent RunningAppIntent = null;
	Intent BlackListIntent = null;
	Intent WhtieListIntent = null;
	public static MainActivity instance = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 2.加载XML布局
		setContentView(R.layout.tab_host);
		//3.获得TabHost对象
		tah = getTabHost();
		instance = this;
		// from(this)从TabActivity获取LayoutInflater
		// R.layout.main 存放Tab布局
		// 通过TabHost获得存放Tab标签页内容的FrameLayout
		// 是否将inflate 加到根布局元素上
			
		// 设置Tab标签的内容和显示内容
		RunningAppIntent = new Intent(this, RunningAppActivity.class);
		BlackListIntent = new Intent(this, BlackListActivity.class);
		WhtieListIntent = new Intent(this, WhtieListActivity.class);
		//4.跳转
		tah.addTab(tah.newTabSpec("tab1").setIndicator("正在运行的程序").setContent(RunningAppIntent));
		tah.addTab(tah.newTabSpec("tab2").setIndicator("黑名单").setContent(BlackListIntent));
		tah.addTab(tah.newTabSpec("tab3").setIndicator("白名单").setContent(WhtieListIntent));
		tah.setCurrentTab(1);
	}
}