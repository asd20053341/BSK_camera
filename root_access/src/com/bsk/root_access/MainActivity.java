package com.bsk.root_access;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

    Process process = null;
    DataOutputStream os = null;
    DataInputStream is = null;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//per-up
        try {
        	String result = ""; 
            //process = Runtime.getRuntime().exec("/system/xbin/per-up"); //这里可能需要修改su的源代码 （注掉  if (myuid != AID_ROOT && myuid != AID_SHELL) {）
            process = Runtime.getRuntime().exec("su"); //这里可能需要修改su的源代码 （注掉  if (myuid != AID_ROOT && myuid != AID_SHELL) {）
            
            os = new DataOutputStream(process.getOutputStream());
            is = new DataInputStream(process.getInputStream());
            os.writeBytes("/system/bin/iptables -I INPUT -s 192.168.43.252 -j DROP" + "\n");       //这里可以执行具有root 权限的程序了   
           // os.writeBytes("/system/bin/iptables -I OUTPUT -o wlan0 -m owner --uid-owner u0_a44 -j DROP" + " \n");       //这里可以执行具有root 权限的程序了   
            os.writeBytes("exit \n");
            os.flush();
            String line = null;
			while ((line = is.readLine()) != null) {
				Log.v("result", line);
				result += line;
			}
            process.waitFor();

        } catch (Exception e) { 
            
            Log.e("root access", "Unexpected error - Here is what I know:" + e.getMessage());
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (is != null) {
                    is.close();
                }
                process.destroy();
            } catch (Exception e) {

            }
        }// get the root privileges
	}

	
}
