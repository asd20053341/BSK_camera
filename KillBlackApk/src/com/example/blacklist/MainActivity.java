package com.example.blacklist;

import android.support.v7.app.ActionBarActivity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
//bsk-app-manager11111111
//bsk-app-manager1111111
public class MainActivity extends TabActivity {
	//1.���ȶ���TabHost����(�����ȸ��Ѿ����Ƽ����������)
	TabHost tah = null;
	Intent RunningAppIntent = null;
	Intent BlackListIntent = null;
	Intent WhtieListIntent = null;
	public static MainActivity instance = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 2.����XML����
		setContentView(R.layout.tab_host);
		//3.���TabHost����
		tah = getTabHost();
		instance = this;
		// from(this)��TabActivity��ȡLayoutInflater
		// R.layout.main ���Tab����
		// ͨ��TabHost��ô��Tab��ǩҳ���ݵ�FrameLayout
		// �Ƿ�inflate �ӵ�������Ԫ����
			
		// ����Tab��ǩ�����ݺ���ʾ����
		RunningAppIntent = new Intent(this, RunningAppActivity.class);
		BlackListIntent = new Intent(this, BlackListActivity.class);
		WhtieListIntent = new Intent(this, WhtieListActivity.class);
		//4.��ת
		tah.addTab(tah.newTabSpec("tab1").setIndicator("�������еĳ���").setContent(RunningAppIntent));
		tah.addTab(tah.newTabSpec("tab2").setIndicator("������").setContent(BlackListIntent));
		tah.addTab(tah.newTabSpec("tab3").setIndicator("������").setContent(WhtieListIntent));
		tah.setCurrentTab(1);
	}
}