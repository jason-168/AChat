package com.sl.achat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.sl.SLChannel;
import com.sl.media.MView;
import com.sl.media.MViewRenderer;
import com.sl.media.MediaPlayer;
import com.sl.media.SLDataSource;
import com.sl.media.SLDataSourceListener;
import com.sl.prot.Protocol;
import com.sl.prot.SLAVFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VoiceCallActivity extends AppCompatActivity implements OutgoingListener{

//    private FloatingActionButton call;
//    private FloatingActionButton call_end;

    private TextView rateView = null;
    private TextView rateView1 = null;

    private TextView infoView = null;

    private TextView textView2 = null;

    private String sid = null;
    private long uid = 0;
    private String uname = "";
    private String pwd = "";

    private int callingID = 0;

    private MediaPlayer player = null;
    private SLChannel channel = null;

    private boolean dial = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_voicecall);

        rateView = (TextView)findViewById(R.id.rateView);
        rateView1 = (TextView)findViewById(R.id.rateView1);

        infoView = (TextView)findViewById(R.id.infoView);

        TextView textView1 = (TextView)findViewById(R.id.textView1);
        textView2 = (TextView)findViewById(R.id.textView2);

        FloatingActionButton callFab = (FloatingActionButton) findViewById(R.id.call);
        callFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setVisibility(View.GONE);
                handler.sendEmptyMessage(HANGON);
            }
        });

        FloatingActionButton callEndFab = (FloatingActionButton) findViewById(R.id.call_end);
        callEndFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setVisibility(View.GONE);
                handler.sendEmptyMessage(HANGUP);
            }
        });

        Intent extra = getIntent();
        sid = extra.getStringExtra("sid");
        uid = extra.getLongExtra("uid", 0);
        uname = extra.getStringExtra("uname");
        pwd = extra.getStringExtra("pwd");

        callingID = extra.getIntExtra("callingID", 0);

        dial = extra.getBooleanExtra("dial", false);
        if (dial == true && sid != null && !"".equals(sid.trim())) {
            callFab.setVisibility(View.GONE);
            handler.sendEmptyMessageDelayed(CALL, 0);
        }else{
            MainActivity.setOutgoingListener(this);
        }

        textView1.setText(sid);
        textView2.setText(dial == true ? "calling" : "incoming");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        callEnd();

        if(dial == false) {
            MainActivity.setOutgoingListener(null);
        }

        MainActivity.endCall(callingID);

        handler.removeCallbacksAndMessages(null);
        System.out.println("-------------onDestroy");
    }

    private boolean call() {
        int id = MainActivity.requestCall();
        if(id <= 0){
            handler.sendEmptyMessageDelayed(FINISH, 200);
            return false;
        }
        callingID = id;

        if (dial == true && sid != null && !"".equals(sid.trim()) && player == null) {
            MViewRenderer renderer = new MViewRenderer(this, new MView(this));

            SLDataSource source = new SLDataSource(new MySLDataSourceListener());
            source.setParams(sid, uid, uname, pwd);
            source.setRequest(2, 0, 3, 0);

            player = new MediaPlayer();
            player.prepare();

            player.setDataSource(source);
            player.setDisplay(renderer);
            player.start();

//            infoBuffer.append(String.format("\n[%s]onConnecting", dateFormat.format(new Date())));
//            infoView.setText(infoBuffer.toString());
            return true;
        }
        return false;
    }

    private boolean hangon(){
        synchronized (MainActivity.glock) {
            if(MainActivity.callingChannel != null){
                channel = MainActivity.callingChannel;
            }
        }

        if(channel != null){
            channel.authRes(0, 0);
            MViewRenderer renderer = new MViewRenderer(this, new MView(this));

            SLDataSource source = new SLDataSource(channel, new MySLDataSourceListener());

            player = new MediaPlayer();
            player.prepare();

            player.setDataSource(source);
            player.setDisplay(renderer);
            player.start();

            timer_start();
            return true;
        }

        return false;
    }

    private void hangup(){
        if(player != null){
            callEnd();
        }else if(MainActivity.callingChannel != null){
            MainActivity.callingChannel.authRes(-1, 0);
        }

        handler.sendEmptyMessageDelayed(FINISH, 200);
    }

    private void callEnd(){
        if(player != null){
            player.stop();
            player.release();
            player = null;
        }
    }

    @Override
    public void onOutgoingSession() {
        handler.sendEmptyMessageDelayed(FINISH, 200);
    }

    private static final int SHOW_TIME = 10;

    private static final int CALL = 20;

    private static final int HANGON = 50;
    private static final int HANGUP = 60;

    private static final int FINISH = 99;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_TIME:{
                    timer_run();
                    break;
                }
                case CALL: {
                    call();
                    break;
                }
                case HANGON:{
                    hangon();
                    break;
                }
                case HANGUP:{
                    hangup();
                    break;
                }
                case FINISH:{
                    finish();
                    break;
                }
            }
        }
    };

    //--------------------------------------------------
    private long start_time = 0;

    private void timer_start(){
        start_time = System.currentTimeMillis();
        textView2.setText("00:00:00");

        handler.sendEmptyMessageDelayed(SHOW_TIME, 1000);
    }

    private void timer_run(){
        long timenow = System.currentTimeMillis();
        long usingtime = (timenow - start_time)/1000;

//        System.out.println("usingtime:" + usingtime);

        int hour = (int)(usingtime / 3600);
        int min = (int)((usingtime - hour * 3600) / 60);
        int sec = (int)(usingtime - hour * 3600 - min * 60);

        String show_time = String.format("%02d:%02d:%02d", hour, min, sec);

        textView2.setText(show_time);
        handler.sendEmptyMessageDelayed(SHOW_TIME, 1000);
    }

    //--------------------------------------------------
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.CHINESE);
    private StringBuffer infoBuffer = new StringBuffer();

    private class MySLDataSourceListener extends SLDataSourceListener {

        @Override
        public void onReConnecting(){
//            infoBuffer.append(String.format("\n[%s]onReConnecting", dateFormat.format(new Date())));
//            infoView.setText(infoBuffer.toString());
        }

        @Override
        public void onConnected(int mode, String ip, int port) {
//            infoBuffer.append(String.format("\n[%s]onConnected\nmode: %s, ip: %s, port: %d",
//                    dateFormat.format(new Date()), (mode == 2) ? "relay" : "p2p", ip, port));
//            infoView.setText(infoBuffer.toString());
            infoBuffer.append(String.format("\n连接模式:%s", (mode == 2) ? "relay" : "p2p"));
            infoView.setText(infoBuffer.toString());
        }

        @Override
        public void onAuth(int result) {
//            infoBuffer.append(String.format("\n[%s]onAuth\nresult: %d", dateFormat.format(new Date()), result));
//            infoView.setText(infoBuffer.toString());

            timer_start();
        }

        @Override
        public void onDisconnected(int errcode) {
            infoBuffer.append(String.format("\n[%s]onDisconnected\nresult: %d", dateFormat.format(new Date()), errcode));
            infoView.setText(infoBuffer.toString());

            handler.sendEmptyMessage(HANGUP);
        }

        @Override
        public void onDataRate(int upstreamRate, int downstreamRate) {
//            upstreamRate *= 8;
//            downstreamRate *= 8;

            String upRate = (upstreamRate > 1024) ? (upstreamRate / 1024) + "KB" : upstreamRate + "B";
            String downRate = (downstreamRate > 1024) ? (downstreamRate / 1024) + "KB" : downstreamRate + "B";

            rateView.setText(upRate + "↑  " + downRate + "↓");
        }

        @Override
        public void incomingData(byte[] data) {
        }

        @Override
        public int onVideoCallReq(Protocol.SLVideocallReq req, SLAVFormat avfmt_res) {
            avfmt_res.audiofmt.acodecID = SLAVFormat.SL_ACODEC_ID_G711A; // 编码方式
            avfmt_res.audiofmt.samplerate = 16000; // 采样率
            avfmt_res.audiofmt.channels = 1; // 通道数
            return 0;
        }

        @Override
        public void onInfo(String info) {
            rateView1.setText(info);
        }
    }
}
