package com.sl.achat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.sl.db.DB;
import com.sl.db.DBService;

public class LoadingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        View e = findViewById(R.id.activity_loading);
        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        e.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        Intent extra = getIntent();
        long uidFromPush = extra.getLongExtra("uidFromPush", 0);
        int chFromPush = extra.getIntExtra("chFromPush", 0);

        System.out.println("uidFromPush:" + uidFromPush + ",chFromPush:" + chFromPush);

        Object obj = null;
        int what = LOAD_DONE;
        int delay = 2000;

        MainApplication app = ((MainApplication)getApplication());
        DB.User user = app.getUser();
        if(user == null){
            app.startSLService();
        }else{
//            what = PREVIEW;
        }

        if(uidFromPush != 0){
            what = PREVIEW;
            obj = (Long)uidFromPush;
            delay = 0;
        }

        Message m = handler.obtainMessage(what, chFromPush, 0, obj);
        handler.sendMessageDelayed(m, delay);
    }

    private static final int LOAD_DONE = 1;
    private static final int PREVIEW = 2;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LOAD_DONE: {
                    Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                    if(msg.obj != null){
                        intent.putExtra("uidFromPush", (Long)msg.obj);
                        intent.putExtra("chFromPush", msg.arg1);
                    }

                    startActivity(intent);
                    LoadingActivity.this.finish();
                    break;
                }
                case PREVIEW:{
                    int ch = msg.arg1;
                    long uid = (Long)msg.obj;

                    previewFromPush(LoadingActivity.this, ((MainApplication)getApplication()).getUser(), uid, ch);
                    LoadingActivity.this.finish();
                    break;
                }
            }
        }
    };

    public static void previewFromPush(Context context, DB.User user, long uid, int ch){
        DBService db = new DBService(context);
        DB.Device dev = db.queryDevice(DB.Device.SELECT_ONE_UID, new String[]{""+uid, "" + user.getId()});
        db.close();

        if(dev != null) {
            Intent intent = new Intent(context, DevicePreviewActivity.class);
            intent.putExtra("sid", dev.getSid());
            intent.putExtra("uid", dev.getUid());
            intent.putExtra("uname", dev.getUsername());
            intent.putExtra("pwd", dev.getPasswd());

            intent.putExtra("ch", ch);
            context.startActivity(intent);
        }
    }
}
