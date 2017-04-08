package com.sl.achat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

public class KeepliveActivity extends Activity {
	private static final String TAG = KeepliveActivity.class.getSimpleName();
	
	private static KeepliveActivity keeplive = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setGravity(Gravity.LEFT|Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x=0;
        params.y=0;
        params.height=1;
        params.width=1;
        window.setAttributes(params);
        
        keeplive = this;
        
        Log.i(TAG, "onCreate");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy");
	}
	
	public static void startKeeplive(Context context){
		Intent intent = new Intent(context, KeepliveActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
	
	public static void stopKeeplive(){
		if(keeplive != null){
			keeplive.finish();
			keeplive = null;
		}
	}
}
