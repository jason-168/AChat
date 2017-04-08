package com.sl.achat;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sl.SLChannel;
import com.sl.media.AndroidCaptureSource;
import com.sl.media.MViewRenderer;
import com.sl.media.MediaPlayer;
import com.sl.media.SLDataSource;
import com.sl.media.SLDataSourceListener;
import com.sl.prot.Protocol;
import com.sl.prot.SLAVFormat;

import org.webrtc.EglBase;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VideoCallActivity extends AppCompatActivity implements OutgoingListener, MediaPlayer.OnVideoSizeChangedListener{

    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;

    private TextView rateView = null;
    private TextView rateView1 = null;

    private TextView infoView = null;

    private TextView textView1 = null;
    private TextView textView2 = null;

    private String sid = null;
    private long uid = 0;
    private String uname = "";
    private String pwd = "";

    private int callingID = 0;

    private VideoView videoView = null;

    private EglBase rootEglBase = null;
    private SurfaceViewRenderer localRenderer = null;

    private MediaPlayer player = null;
    private SLChannel channel = null;

    private boolean dial = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_videocall);

        RelativeLayout videolayout = (RelativeLayout) findViewById(R.id.video_window0);
        videoView = new VideoView(this, 0, videolayout);
        videoView.getMView().setVisibility(View.GONE);

        changeLayout(getResources().getConfiguration());

        localRenderer = (SurfaceViewRenderer) findViewById(R.id.local_video_view);

        rateView = (TextView)findViewById(R.id.rateView);
        rateView1 = (TextView)findViewById(R.id.rateView1);

        infoView = (TextView)findViewById(R.id.infoView);

        textView1 = (TextView)findViewById(R.id.textView1);
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

        rootEglBase = EglBase.create();
        localRenderer.init(rootEglBase.getEglBaseContext(), null);

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

        if(localRenderer != null){
            localRenderer.release();
            localRenderer = null;
        }
        if(rootEglBase != null){
            rootEglBase.release();
            rootEglBase = null;
        }
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
            MViewRenderer renderer = new MViewRenderer(this, videoView.getMView());

            SLDataSource source = new SLDataSource(new MySLDataSourceListener());
//            source.getSLChannel().setPFlow(13);

            source.setParams(sid, uid, uname, pwd);
            source.setRequest(3, 0, 3, 0);

            AndroidCaptureSource.CaptureParams p = new AndroidCaptureSource.CaptureParams();
            p.width = WIDTH;
            p.height = HEIGHT;

            AndroidCaptureSource captureSource = new AndroidCaptureSource(this,
                    rootEglBase.getEglBaseContext(), new VideoRenderer(localRenderer), p);

            player = new MediaPlayer();
            player.prepare();

            player.setDataSource(source);
            player.setDisplay(renderer);

            player.setCaptureSource(captureSource);

            player.setOnVideoSizeChangedListener(this);
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
            MViewRenderer renderer = new MViewRenderer(this, videoView.getMView());
            SLDataSource source = new SLDataSource(channel, new MySLDataSourceListener());

            AndroidCaptureSource.CaptureParams p = new AndroidCaptureSource.CaptureParams();
            p.width = WIDTH;
            p.height = HEIGHT;

            AndroidCaptureSource captureSource = new AndroidCaptureSource(this,
                    rootEglBase.getEglBaseContext(), new VideoRenderer(localRenderer), p);

            player = new MediaPlayer();
            player.prepare();

            player.setDataSource(source);
            player.setDisplay(renderer);

            player.setCaptureSource(captureSource);
            player.setOnVideoSizeChangedListener(this);
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

    private static final int LAYOUT_INIT = 70;
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

                case LAYOUT_INIT: {
                    initVideoWindowLayout();
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
    private int screenWidth, screenHeight;
    private Rect rect = new Rect();

    int h = 1;

    private void changeLayout(Configuration newConfig) {
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            h = 1;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            h = 2;
        }
        handler.sendEmptyMessage(LAYOUT_INIT);
    }

    private void initVideoWindowLayout() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        rect.left = 0;
        rect.top = 0;
        rect.right = screenWidth;
        rect.bottom = screenHeight;

        videoView.setLayoutParams(rect, 0);
    }

    //--------------------------------------------------
    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        System.out.println("width:" + width + ", height:" + height);

        videoView.getMView().setVisibility(View.VISIBLE);

        initMatrix(mp.getDisplay().getMatrix(), width, height);
    }

    private void initMatrix(Matrix matrix, int videoWidth, int videoHeight){
        matrix.reset();

        int winWidth = rect.right;
        int winHeight = rect.bottom;

        float videoRatio = videoWidth / (videoHeight * 1.0f);
        float winRatio = winWidth / (winHeight * 1.0f);

        float ratio = 1.0f;
        float translateX = 0;
        float translateY = 0;

        if(videoRatio > winRatio){
            ratio = winHeight / (videoHeight * 1.0f);
            translateX = (winWidth - (videoWidth * ratio)) / 2f;
        }else{
            ratio = winWidth / (videoWidth * 1.0f);
            translateY = (winHeight - (videoHeight * ratio)) / 2f;
        }

        matrix.postScale(ratio, ratio);
        // 在纵坐标方向上进行偏移，以保证图片居中显示
        matrix.postTranslate(translateX, translateY);
    }


    //--------------------------------------------------
    private long start_time = 0;

    private void timer_start(){
        textView1.setVisibility(View.GONE);

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

//            Toast.makeText()

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

            avfmt_res.videofmt.vcodecID = SLAVFormat.SL_VCODEC_ID_H264; // 编码方式
            avfmt_res.videofmt.width = 640; // 图像宽度
            avfmt_res.videofmt.height = 360; // 图像高度
            avfmt_res.videofmt.framerate = 20; // 帧率, fps
            avfmt_res.videofmt.ifiv = 20; // I Frames Interval I帧间隔
            return 0;
        }

        @Override
        public void onInfo(String info) {
            rateView1.setText(info);
        }
    }
}
