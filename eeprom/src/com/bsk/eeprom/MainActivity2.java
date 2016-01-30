package com.bsk.eeprom;

import com.ponline.oo.util.MD5Utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity2 extends Activity {
	private TextView lblEditTest;
	private Button btnLogin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		lblEditTest = (EditText) findViewById(R.id.lblEditTest);
		btnLogin = (Button) findViewById(R.id.btnLogin);
		btnLogin.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String editText = lblEditTest.getText().toString();
				String editText1=MD5Utils.MD5(editText);
				System.out.println(editText1);
				//Log.v("MD5", "MD55555555555=" + MD5Utils.MD5(editText1));
				if (editText != null && "44c4c17332cace2124a1a836d9fc4b6f".equals(editText1)) {
					Intent intent = new Intent();
					intent.setClass(MainActivity2.this, MainActivity.class);
					startActivity(intent);
				} else {
					Toast.makeText(MainActivity2.this, "√‹¬Î¥ÌŒÛ£¨«Î÷ÿ–¬ ‰»Î", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

}
