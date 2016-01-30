package com.example.gpio1;

import android.os.Bundle;

import com.bsk.eeprom.R;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.SystemClock;

public class MainActivity extends Activity {
	  	public static native String  setDirection(String port,int num,String inout);
	    public static  native String setValue(String port,int num,int value);
	    public  native int getValue(String port,int num);
	    public static native int enablePwm();
	    public static native int disablePwm();
	    public native int enablePwmTime(int msecond);
	  
	    int i;
	    
	    private Button btn1,btn2;
	    	
		static {
	        System.loadLibrary("gpio_lib");
	    }
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.gpio_layout);
			
		//	Intent intent = new Intent(this, PwmService.class);
		//	startService(intent);
			
			//云相框指示灯测试
			btn1 = (Button)findViewById(R.id.btn1);
			btn2 = (Button)findViewById(R.id.btn2);
			
			btn1.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					
					for(int i=0;i<3;i++) {
						MainActivity.setDirection("PD",10,"out");
						MainActivity.setValue("PD",10,1);
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						
						MainActivity.setValue("PD",10,0);
						
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
			
			btn2.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					for(int i=0;i<3;i++) {
						MainActivity.setDirection("PD",11,"out");
						MainActivity.setValue("PD",11,1);
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						
						MainActivity.setValue("PD",11,0);
						
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
			
			
		//	enablePwm();
			
			/*enablePwm();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			disablePwm();*/
			
				
			//}
			/*
			while(true){
				enablePwm();
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
			*/
		/*	
			Button setuuid= (Button)findViewById(R.id.button1);

			setuuid.setOnClickListener(new View.OnClickListener()
			{
				int a = 1;
			
				TextView curruuidTxt = (TextView)findViewById(R.id.textView1);
			
				@Override
				public void onClick(View v)
				{
					
					//WritePwm((char) 1);
					enablePwm();
					
	
				
					curruuidTxt.setText("ok");
				}
			});
			*/
			/*
				Button buttonDisable= (Button)findViewById(R.id.button2);

				buttonDisable.setOnClickListener(new View.OnClickListener()
				{
					int b = 0;
				
					TextView curruuidTxt = (TextView)findViewById(R.id.textView1);
				
					@Override
					public void onClick(View v)
					{
						
						//WritePwm((char) 0);
						disablePwm();
						
						//WriteVersion('3');
						//WriteBatch(a);
					
						curruuidTxt.setText("ok");
					}
			});
			*/
			/*
			setDirection("PH",6,"out");
			setDirection("PH",7,"out");
			for(int i=0; i< 100; i++)
			{
				//if(i%2==0) 
				setValue("PH", 6, 0);  //��ʼ��˸10s
					SystemClock.sleep(100);
				//else
					setValue("PH", 6, 1); //�ر���˸
					SystemClock.sleep(100);
					setValue("PH", 7, 0);  //��ʼ��˸10s
					SystemClock.sleep(100);
				//else
					setValue("PH", 7, 1); //�ر���
					SystemClock.sleep(100);
				
			}
			*/
			
		}

@Override
public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	//getMenuInflater().inflate(R.menu.main, menu);
	return true;
}

  
}


