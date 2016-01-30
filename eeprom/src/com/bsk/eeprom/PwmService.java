package com.bsk.eeprom;



import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class PwmService extends Service {  
    private static final String TAG = "PwmService";  

	public static final String KEY_F1 = "CNLAUNCHER_KEY_F1_DOWN";
	public static final String KEY_F2 = "CNLAUNCHER_KEY_F2_DOWN";
	public static final String KEY_F3 = "CNLAUNCHER_KEY_F3_DOWN";
	public static final String KEY_F4 = "CNLAUNCHER_KEY_F4_DOWN";
	public static final String KEY_F5 = "CNLAUNCHER_KEY_F5_DOWN";
	public static final String KEY_F6 = "CNLAUNCHER_KEY_F6_DOWN";
	public static final String KEY_F7 = "CNLAUNCHER_KEY_F7_DOWN";
	public static final String KEY_F8 = "CNLAUNCHER_KEY_F8_DOWN";
	public static final String KEY_F9 = "CNLAUNCHER_KEY_F9_DOWN";
	public static final String KEY_F10 = "CNLAUNCHER_KEY_F10_DOWN";
	public static final String KEY_F11 = "CNLAUNCHER_KEY_F11_DOWN";
	public static final String KEY_F12 = "CNLAUNCHER_KEY_F12_DOWN";
	
	public static final String KEYU_F1 = "CNLAUNCHER_KEY_F1_UP";
	public static final String KEYU_F2 = "CNLAUNCHER_KEY_F2_UP";
	public static final String KEYU_F3 = "CNLAUNCHER_KEY_F3_UP";
	public static final String KEYU_F4 = "CNLAUNCHER_KEY_F4_UP";
	public static final String KEYU_F5 = "CNLAUNCHER_KEY_F5_UP";
	public static final String KEYU_F6 = "CNLAUNCHER_KEY_F6_UP";
	public static final String KEYU_F7 = "CNLAUNCHER_KEY_F7_UP";
	public static final String KEYU_F8 = "CNLAUNCHER_KEY_F8_UP";
	public static final String KEYU_F9 = "CNLAUNCHER_KEY_F9_UP";
	public static final String KEYU_F10 = "CNLAUNCHER_KEY_F10_UP";
	public static final String KEYU_F11 = "CNLAUNCHER_KEY_F11_UP";
	public static final String KEYU_F12 = "CNLAUNCHER_KEY_F12_UP";
	
	

    
    @Override  
    public IBinder onBind(Intent intent) {  
        return null;  
    }  
  
    @Override  
    public void onCreate() {  
    //    Toast.makeText(this, "My Service created", Toast.LENGTH_LONG).show();  
        Log.e(TAG, "onCreate");  
        
        PwmControlReceiver receive = new PwmControlReceiver();
        IntentFilter filter = new IntentFilter();
		filter.addAction(KEY_F1);
		filter.addAction(KEY_F2);
		filter.addAction(KEY_F3);
		filter.addAction(KEY_F4);
		filter.addAction(KEY_F5);
		filter.addAction(KEY_F6);
		filter.addAction(KEY_F7);
		filter.addAction(KEY_F8);
		filter.addAction(KEY_F9);
		filter.addAction(KEY_F10);
		filter.addAction(KEY_F11);
		filter.addAction(KEY_F12);
		
		filter.addAction(KEYU_F1);
		filter.addAction(KEYU_F2);
		filter.addAction(KEYU_F3);
		filter.addAction(KEYU_F4);
		filter.addAction(KEYU_F5);
		filter.addAction(KEYU_F6);
		filter.addAction(KEYU_F7);
		filter.addAction(KEYU_F8);
		filter.addAction(KEYU_F9);
		filter.addAction(KEYU_F10);
		filter.addAction(KEYU_F11);
		filter.addAction(KEYU_F12);
		registerReceiver(receive, filter);
  
    }  
  
    @Override  
    public void onDestroy() {  
    //    Toast.makeText(this, "My Service Stoped", Toast.LENGTH_LONG).show();  
        Log.e(TAG, "onDestroy");  
    }  
  
    @Override  
    public void onStart(Intent intent, int startid) {  
    //    Toast.makeText(this, "My Service Start", Toast.LENGTH_LONG).show();  
        Log.e(TAG, "onStart");  
        
        try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        Intent intent2 = new Intent();
        intent2.setAction(KEY_F1);
        this.sendBroadcast(intent2);
        
        
        MainActivity.setDirection("PH",6,"out");
		MainActivity.setValue("PH",6,0);
       
    }  
    
    
    public class PwmControlReceiver extends BroadcastReceiver {
    	
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		Log.e("", "on receive action!");
    		if(intent.getAction().equals(KEY_F1) || intent.getAction().equals(KEY_F2)
    				|| intent.getAction().equals(KEY_F3) || intent.getAction().equals(KEY_F4)
    				|| intent.getAction().equals(KEY_F5) || intent.getAction().equals(KEY_F6)
    				|| intent.getAction().equals(KEY_F7) || intent.getAction().equals(KEY_F8)
    				|| intent.getAction().equals(KEY_F9) || intent.getAction().equals(KEY_F10)
    				|| intent.getAction().equals(KEY_F11) || intent.getAction().equals(KEY_F12)) {
    			
    			Log.i(TAG, "key down ----------");
    			
    			new Thread(new Runnable() {
					@Override
					public void run() {
						MainActivity.enablePwm();
						
						
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						MainActivity.disablePwm();
						
						
						MainActivity.setDirection("PH",7,"out");
						MainActivity.setValue("PH",7,0);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
							
						MainActivity.setValue("PH",7,1);
						
					}
				}).start();
    			
    			
    		}
    		
    		if(intent.getAction().equals(KEYU_F1) || intent.getAction().equals(KEYU_F2)
    				|| intent.getAction().equals(KEYU_F3) || intent.getAction().equals(KEYU_F4)
    				|| intent.getAction().equals(KEYU_F5) || intent.getAction().equals(KEYU_F6)
    				|| intent.getAction().equals(KEYU_F7) || intent.getAction().equals(KEYU_F8)
    				|| intent.getAction().equals(KEYU_F9) || intent.getAction().equals(KEYU_F10)
    				|| intent.getAction().equals(KEYU_F11) || intent.getAction().equals(KEYU_F12)) {
    			
    			Log.i(TAG, "key up ++++++++++++++++++++++++++++");
    			
    			
    		}
    	}	

    }
}  
