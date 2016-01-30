package com.bsk.update.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bsk.update.R;
import com.bsk.update.util.OtaUpgradeUtils;

public class InstallPackage extends LinearLayout implements OtaUpgradeUtils.ProgressListener{
    
    private ProgressBar mProgressBar;
    private OtaUpgradeUtils mUpdateUtils;
    private LinearLayout mOutputField;
    private LayoutInflater mInflater;
    private String mPackagePath;
    private Handler mHandler = new Handler();
    private Button mDismiss;

    public InstallPackage(Context context, AttributeSet attrs) {
        super(context, attrs);
        mUpdateUtils = new OtaUpgradeUtils(context);
        mInflater = LayoutInflater.from(context);
        requestFocus();
    }
    
    public void setPackagePath(String path){
        mPackagePath = path;
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mProgressBar = (ProgressBar) findViewById(R.id.verify_progress);
        mOutputField = (LinearLayout) findViewById(R.id.output_field);
        final TextView tv = (TextView) mInflater.inflate(R.layout.medium_text, null);
        tv.setText(R.string.install_ota_output_confirm);
        tv.setPadding(10, 8, 2, 2);
        tv.setTextSize(20);
        mOutputField.addView(tv);
        Animation animation = new AlphaAnimation(0.0f,1.0f);
        animation.setDuration(600);
        LayoutAnimationController controller = 
            new LayoutAnimationController(animation);
        mOutputField.setLayoutAnimation(controller);
        mDismiss = (Button)findViewById(R.id.confirm_cancel);
        findViewById(R.id.confirm_update).setOnClickListener(new View.OnClickListener() {            
            @Override
            public void onClick(View v) {
            	mOutputField.removeAllViews();
                tv.setText(R.string.install_ota_output_start);
                mOutputField.addView(tv);
                new Thread(new Runnable(){
                    @Override
                    public void run() {                        
                        mUpdateUtils.upgradeFromOta(mPackagePath, InstallPackage.this);
                    }            
                }).start();
                mDismiss.setEnabled(false);
            }
        });
    }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(!mDismiss.isEnabled() && keyCode == KeyEvent.KEYCODE_BACK){
    		return true;
    	}
		return super.onKeyDown(keyCode, event);
	}

	public void deleteSource(boolean b){
        mUpdateUtils.deleteSource(b);
    }

    @Override
    public void onProgress(final int progress) {
        mHandler.post(new Runnable(){

            @Override
            public void run() {
                if(progress == 0){
                    TextView tv = (TextView) mInflater.inflate(R.layout.medium_text, null);
                    tv.setText(R.string.install_ota_output_checking);
                    tv.setPadding(10, 2, 2, 2);
                    tv.setTextSize(20);
                    mOutputField.addView(tv);
                }else if(progress == 100){
                    TextView tv = (TextView) mInflater.inflate(R.layout.medium_text, null);
                    tv.setText(R.string.install_ota_output_check_ok);
                    tv.setPadding(10, 2, 2, 2);
                    tv.setTextSize(20);
                    mOutputField.addView(tv);
                }
              //  mProgressBar.setProgress(progress/2);                
                mProgressBar.setProgress(progress);                
            }            
        });        
    }

    @Override
    public void onVerifyFailed(int errorCode, Object object) {
        mHandler.post(new Runnable(){
            @Override
            public void run() {
                TextView tv = (TextView) mInflater.inflate(R.layout.medium_text, null);
                tv.setText(R.string.install_ota_output_check_error);
                tv.setTextColor(getResources().getColor(R.color.red));
                tv.setPadding(10, 2, 2, 2);
                tv.setTextSize(20);
                mOutputField.addView(tv);
                mDismiss.setEnabled(true);
            }}        
        );        
    }

    @Override
    public void onCopyProgress(final int progress) {
        mHandler.post(new Runnable(){
            @Override
            public void run() {
                if(progress == 0){
                	mOutputField.removeAllViews();
                    TextView tv1 = (TextView) mInflater.inflate(R.layout.medium_text, null);
                    tv1.setText(R.string.install_ota_output_copying);
                    tv1.setPadding(10, 2, 2, 2);
                    tv1.setTextSize(20);
                    mOutputField.addView(tv1);
                }else if(progress == 100){
                    TextView tv = (TextView) mInflater.inflate(R.layout.medium_text, null);
                    tv.setText(R.string.install_ota_output_copy_ok);
                    tv.setPadding(6, 2, 2, 2);
                    tv.setTextSize(14);
                    mOutputField.addView(tv);
                    tv = (TextView) mInflater.inflate(R.layout.medium_text, null);
                    tv.setText(R.string.install_ota_output_restart);
                    tv.setPadding(10, 2, 2, 2);
                    tv.setTextSize(20);
                    mOutputField.addView(tv);
                }        
                mProgressBar.setProgress(50 + progress/2);
            }            
        });        
    }

    @Override
    public void onCopyFailed(int errorCode, Object object) {
        mHandler.post(new Runnable(){
            @Override
            public void run() {
                TextView tv = (TextView) mInflater.inflate(R.layout.medium_text, null);
                tv.setText(R.string.install_ota_output_copy_failed);
                tv.setTextColor(getResources().getColor(R.color.red));
                tv.setPadding(10, 2, 2, 2);
                tv.setTextSize(20);
                mOutputField.addView(tv);
                mDismiss.setEnabled(true);
            }            
        });        
    }
}
