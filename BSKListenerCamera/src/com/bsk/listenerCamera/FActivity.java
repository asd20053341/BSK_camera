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

		// String message = "确定还是取消？";
		AlertDialog.Builder builder = new AlertDialog.Builder(FActivity.this);
		builder.setTitle(R.string.camera_close_prompt); // 设置对话框的标题
		// builder.setMessage(message); // 设置对话框的内容
		builder.setCancelable(false);// 点击对话框以外的地方，对话框不会消失
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {// 设置一个点击了积极时的事件

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

		builder.show();// 最后记得把对话框显示出来，不要只创建，不显示出来

	}
}
