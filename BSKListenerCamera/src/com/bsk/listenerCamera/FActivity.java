package com.bsk.listenerCamera;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

public class FActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.factivity);

		// String message = "ȷ������ȡ����";
		AlertDialog.Builder builder = new AlertDialog.Builder(FActivity.this);
		builder.setTitle(R.string.camera_close_prompt); // ���öԻ���ı���
		// builder.setMessage(message); // ���öԻ��������
		builder.setCancelable(false);// ����Ի�������ĵط����Ի��򲻻���ʧ
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {// ����һ������˻���ʱ���¼�

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub				
				Intent in = new Intent("com.cnlaunch.intent.action.CLOSE_CAMERA_ACTIVITY");
				sendBroadcast(in);		
				FActivity.this.finish();
				Intent home = new Intent(Intent.ACTION_MAIN);  
				home.addCategory(Intent.CATEGORY_HOME);   
				startActivity(home);
			}
		});

		builder.show();// ���ǵðѶԻ�����ʾ��������Ҫֻ����������ʾ����

	}
}
