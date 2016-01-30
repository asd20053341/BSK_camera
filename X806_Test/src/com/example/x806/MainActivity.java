package com.example.x806;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {
	private static final String TAG = "X806master";
	private Button btn_connection = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);
		btn_connection = (Button) findViewById(R.id.btn_connection);
		btn_connection.setOnClickListener(this);
		//Log.v("X806master", "Activity");
		Intent intent = new Intent();
		intent.setAction("com.bsk.x806.action.MY_REMOTE_SERVICE");
		// °ó¶¨·þÎñ
		startService(intent);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			System.exit(0);
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		System.out.println("aaaaaaaa");
	}
}
