package com.sl.achat;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

public class MainService extends Service {

    private static MainService mainService = null;

    public MainService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mainService = this;

        Intent intent = new Intent(this.getBaseContext(), InnerService.class);
        startService(intent);

        startScreenBroadcastReceiver();
    }

    @Override
    public void onDestroy() {
        stopScreenStateUpdate();

        mainService = null;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    //----------------------------------------------------------------------------------
    private ScreenBroadcastReceiver mScreenReceiver = null;

    /**
     * 启动screen状态广播接收器
     */
    private void startScreenBroadcastReceiver() {
        if (mScreenReceiver == null) {
            mScreenReceiver = new ScreenBroadcastReceiver();

            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_USER_PRESENT);
            registerReceiver(mScreenReceiver, filter);
        }
    }

    /**
     * 停止screen状态更新
     */
    public void stopScreenStateUpdate() {
        if (mScreenReceiver != null) {
            unregisterReceiver(mScreenReceiver);
        }
    }

    //----------------------------------------------------------------------------------
    public static class InnerService extends Service {
        public InnerService() {
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            MainService.setForeground(MainService.mainService, this);
            return super.onStartCommand(intent, flags, startId);
        }
    }

    public static void setForeground(final Service keepliveService, final Service innerService) {
        final int foregroundID = 1;
        if (keepliveService != null) {
            keepliveService.startForeground(foregroundID, new Notification());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                innerService.startForeground(foregroundID, new Notification());
                innerService.stopSelf();
            }
        }
    }
}
