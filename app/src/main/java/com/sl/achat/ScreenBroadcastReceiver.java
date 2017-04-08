package com.sl.achat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
//			System.out.println("ACTION_SCREEN_ON");
		} else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
			System.out.println("ACTION_SCREEN_OFF");
			KeepliveActivity.startKeeplive(context);
		} else if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
			System.out.println("ACTION_USER_PRESENT");
			KeepliveActivity.stopKeeplive();
		}
		
//		KeepLiveManeger.getInstance(mContext).startKeepLiveService();
	}

}
