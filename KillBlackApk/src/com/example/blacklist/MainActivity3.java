package com.example.blacklist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity3 extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity3);
		Intent intent =new Intent();
		intent.setClass(MainActivity3.this, RunningAppService.class);
		startService(intent);
		
	}
}
