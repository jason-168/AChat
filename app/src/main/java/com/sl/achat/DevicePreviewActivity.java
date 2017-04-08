package com.sl.achat;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.sl.achat.databinding.ActivityDevicePreviewBinding;
import com.sl.achat.recyleradapter.BaseViewAdapter;
import com.sl.achat.recyleradapter.LineItem;
import com.sl.achat.recyleradapter.MultiTypeAdapter;
import com.sl.media.MViewRenderer;
import com.sl.media.MediaPlayer;
import com.sl.media.SLDataSource;
import com.sl.media.SLDataSourceListener;
import com.sl.media.SLOSSDataSource;
import com.sl.protocol.PStorageInfo;
import com.sl.protocol.PStorageVerify;
import com.sl.storage.SLStorageMgr;
import com.sl.storage.SLStorageMgrListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DevicePreviewActivity extends AppCompatActivity implements MediaPlayer.OnVideoSizeChangedListener,
        MediaPlayer.OnCompletionListener, MediaControllerView.MediaControllerListener{

    private static final int VIEW_TYPE_ITEM = 1;
    private static final int VIEW_TYPE_TITLE = 2;

    private ActivityDevicePreviewBinding mMainBinding = null;

    private MediaPlayer player = null;

    private int screenWidth, screenHeight;
    private Rect rect = new Rect();

    private RelativeLayout player_window = null;
    private VideoView videoView = null;

    private MediaControllerView mediaCtrl = null;

    private TextView rateView = null, infoView = null;

    private String sid = null;
    private long uid = 0;
    private int ch = 0;

    private String uname = "";
    private String pwd = "";


    private MultiTypeAdapter dataAdapter = null;

//    private ListView listview = null;
//    private SimpleAdapter simpleAdapter = null;
//    private ArrayList<HashMap<String, Object>> datalist = new ArrayList<HashMap<String, Object>>();

    private SLStorageMgr storageMgr = null;
    private long currentTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        setContentView(R.layout.activity_device_preview);
        mMainBinding =DataBindingUtil.setContentView(this, R.layout.activity_device_preview);

        changeLayout(getResources().getConfiguration());

        player_window = (RelativeLayout) findViewById(R.id.player_window);

        RelativeLayout videolayout = (RelativeLayout) findViewById(R.id.video_window0);
        videoView = new VideoView(this, 0, videolayout);

        mediaCtrl = (MediaControllerView)findViewById(R.id.mc_view);

        rateView = (TextView) findViewById(R.id.textview0);
        infoView = (TextView) findViewById(R.id.textview1);

//        listview = (ListView) findViewById(listview);
//        simpleAdapter = new SimpleAdapter(this, datalist, R.layout.search_list_item,
//                new String[]{"icon", "tm"}, new int[]{android.R.id.icon, android.R.id.text1});
//        listview.setAdapter(simpleAdapter);
//        listview.setOnItemClickListener(datalistClickListener);

        dataAdapter = new MultiTypeAdapter(this);
        dataAdapter.addViewTypeToLayoutMap(VIEW_TYPE_TITLE, R.layout.group_status_item);
        dataAdapter.addViewTypeToLayoutMap(VIEW_TYPE_ITEM, R.layout.child_status_item);
        mMainBinding.setAdapter(dataAdapter);

        mMainBinding.recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.top = dip2px(DevicePreviewActivity.this, 10);
            }
        });


        //设置点击事件
        dataAdapter.setPresenter(new ItemPresenter());


        Intent extra = getIntent();

        sid = extra.getStringExtra("sid");
        uid = extra.getLongExtra("uid", 0);
        ch = extra.getIntExtra("ch", 0);
        uname = extra.getStringExtra("uname");
        pwd = extra.getStringExtra("pwd");

        infoView.setText(sid);

        currentTime = System.currentTimeMillis() / 1000;

        handler.sendEmptyMessage(LAYOUT_INIT);
        handler.sendEmptyMessageDelayed(VIDEO_PLAY, 0);

        handler.sendEmptyMessageDelayed(STORAGE_FILES_REQ, 100);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPlay();

        mediaCtrl.setMediaControllerListener(null);
        if(storageMgr != null) {
            storageMgr.setSLStorageMgrListener(null);
        }
    }

    private boolean preview() {
        if (sid != null && !"".equals(sid.trim()) && player == null) {
            mediaCtrl.setVisibility(View.GONE);

            MViewRenderer renderer = new MViewRenderer(this, videoView.getMView());
            videoView.setViewTouchListener(renderer, new WindowTouchListener(null));

            SLDataSource source = new SLDataSource(new MySLDataSourceListener());
            source.setParams(sid, uid, uname, pwd);
            source.setRequest(1, ch, 3, 0);

            player = new MediaPlayer();
            player.prepare();

            player.setDataSource(source);
            player.setDisplay(renderer);

            player.setOnVideoSizeChangedListener(this);
            player.start();

            infoBuffer.append(String.format("\n[%s]onConnecting", hourFormat.format(new Date())));
            infoView.setText(infoBuffer.toString());
            return true;
        }
        return false;
    }

    private boolean storagePlayback(String file, int storage_type, int start_ts){
        if (player == null) {
            mediaCtrl.setVisibility(View.VISIBLE);

            MViewRenderer renderer = new MViewRenderer(this, videoView.getMView());
            videoView.setViewTouchListener(renderer, new WindowTouchListener(null));

            SLOSSDataSource source = new SLOSSDataSource(file, storage_type, start_ts);

            player = new MediaPlayer();
            player.prepare();

            player.setDataSource(source);
            player.setDisplay(renderer);

            player.setOnVideoSizeChangedListener(this);
            player.start();

            mediaCtrl.setMediaControllerListener(this);
            mediaCtrl.start();
            return true;
        }

        return false;
    }

    private void stopPlay(){
        if(player != null){
            player.stop();
            player.release();
            player = null;

            mediaCtrl.stop();
        }
    }



    int h = 1;
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        changeLayout(newConfig);
    }

    private void changeLayout(Configuration newConfig) {
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            h = 1;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            h = 2;
        }
        handler.sendEmptyMessage(LAYOUT_INIT);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void initVideoWindowLayout() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        rect.left = 0;
        rect.top = 0;
        rect.right = screenWidth;
//        rect.bottom = screenHeight / h;
        rect.bottom = dip2px(this, 220);

//        System.out.println("rect.bottom:" + rect.bottom);

        videoView.setLayoutParams(rect, 0);
    }
    //--------------------------------------------------
    /**
     * 设置菜单按钮的点击事件
     */
    public class ItemPresenter implements BaseViewAdapter.Presenter {

        /**
         * 参考 dataBinding的用法，下同
         *
         * @param item
         */
        public void onStarClick(LineItem item) {
            stopPlay();

            String file = item.getFile();
            int stype = item.getStype();
            int ofs = item.getOfs();

            System.out.println("file:" + file + ", stype:" + stype + ", ofs:" + ofs);
            storagePlayback(file, stype, ofs);
        }
    }



    // ----------------------------------------------
    @Override
    public int onMCGetPosition(){
        if(player != null){
            return player.getCurrentPosition();
        }
        return -1;
    }
    @Override
    public boolean onMCPause(){
        if(player != null){
            return player.pause() == 0 ? true : false;
        }
        return false;
    }
    @Override
    public boolean onMCResume(){
        if(player != null){
            return player.resume() == 0 ? true : false;
        }
        return false;
    }
    @Override
    public boolean onMCSeekTo(int sec){
        if(player != null){
            player.seekTo(sec);
            return true;
        }
        return false;
    }

    //----------------------------------------------

    @Override
    public void onCompletion(MediaPlayer mp) {
        System.out.println("onCompletion");
    }

    //----------------------------------------------
//    private AdapterView.OnItemClickListener datalistClickListener = new AdapterView.OnItemClickListener() {
//        @Override
//        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            stopPlay();
//
//            HashMap<String, Object> m = datalist.get(position);
//            String file = (String)m.get("file");
//            int stype = (Integer)m.get("stype");
//            int ofs = (Integer)m.get("ofs");
//
//            System.out.println("file:" + file + ", stype:" + stype + ", ofs:" + ofs);
//            storagePlayback(file, stype, ofs);
//        }
//    };


    private void reqStorageFiles(){
        PStorageVerify.StorageVasInfo vas = ((MainApplication)getApplication()).getStorageVasInfo(uid);
        if(vas != null){
            storageMgr = SLStorageMgr.get();
            storageMgr.setSLStorageMgrListener(new MySLStorageMgrListener());

            PStorageInfo.PCS_StorageFileInfoDown req = new PStorageInfo.PCS_StorageFileInfoDown();
            req.devid = vas.devid;
            req.tm = currentTime; //Timestamp second
            req.dt = 0; //direction 0:backward,desc; 1:forward,asc
            req.idx = 0;
            req.limit = 30;
            storageMgr.request(PStorageInfo.PCS_StorageFileInfoDown.SL_STORAGEMGR_REQ_FILES, JSON.toJSONString(req));
        }
    }

    private List<LineItem> lineItems = new ArrayList<LineItem>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.CHINESE);

    private boolean isInList(String date){
        for(int i=0; i<lineItems.size(); i++){
            LineItem item = lineItems.get(i);
            if(item.isTitle() == true && date.equals(item.getContent())){
                return true;
            }
        }
        return false;
    }

    private void parseList(List<PStorageInfo.StorageFileInfo> list){
        for(int i=0; i<list.size(); i++){
            PStorageInfo.StorageFileInfo fileInfo = list.get(i);

            String dateStr = dateFormat.format(new Date(fileInfo.tm * 1000));
            String[] dateArr = dateStr.split(" ");
            String mon = dateArr[0];
            String hour = dateArr[1];

            if(isInList(mon) == false){
                LineItem titleItem = new LineItem(mon, true);
                lineItems.add(titleItem);
            }

            LineItem lineItem = new LineItem(hour, false);
            lineItem.setFile(fileInfo.fname);
            lineItem.setStype(fileInfo.stype);
            lineItem.setOfs(fileInfo.ofs);
            lineItems.add(lineItem);
        }

    }

    private class MySLStorageMgrListener extends SLStorageMgrListener {
        @Override
        public void onResponse(int reqtype, int result, String jsondata) {
            System.out.println("Preview, onResponse, reqtype:" + reqtype + ", jsondata:" + jsondata);
//            if(PStorageVerify.PCS_StorageVas4User.SL_STORAGEMGR_REQ_VAS4USER == reqtype){
//                if(result != 0) return;
//
//                PStorageVerify.PCS_StorageVas4UserRes res = JSON.parseObject(jsondata, PStorageVerify.PCS_StorageVas4UserRes.class);
//                if(res.rc == 0 && res.vlist != null && res.vlist.size() > 0) {
//                    ((MainApplication) getApplication()).setStorageVasInfoList(res.vlist);
//
//                    handler.sendEmptyMessageDelayed(STORAGE_FILES_REQ, 0);
//                }
//            }else
            if(PStorageInfo.PCS_StorageFileInfoDown.SL_STORAGEMGR_REQ_FILES == reqtype){
                if(result != 0) return;

                PStorageInfo.PCS_StorageFileInfoDownRes res = JSON.parseObject(jsondata, PStorageInfo.PCS_StorageFileInfoDownRes.class);
                if(res.flist != null){
                    parseList(res.flist);

                    handler.sendEmptyMessage(STORAGE_FILES_RES);
                }
            }
        }
    }
    //----------------------------------------------
    private static final int LAYOUT_INIT = 10;
    private static final int VIDEO_PLAY = 20;


    private static final int STORAGE_FILES_REQ = 33;
    private static final int STORAGE_FILES_RES = 34;

    private static final int STORAGE_PLAYBACK = 41;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LAYOUT_INIT: {
                    initVideoWindowLayout();
                    break;
                }
                case VIDEO_PLAY: {
                    preview();
                    break;
                }
                case STORAGE_FILES_REQ:{
                    List<PStorageVerify.StorageVasInfo> vlist = ((MainApplication) getApplication()).getVlist();
                    if(vlist != null) {
                        reqStorageFiles();
                    }
                    break;
                }
                case STORAGE_FILES_RES:{
                    dataAdapter.addAll(lineItems, new MultiTypeAdapter.CustomMultiViewTyper() {
                        @Override
                        public int getViewType(Object item, int pos) {
                            if (item instanceof LineItem) {
                                if (((LineItem) item).isTitle()) {
                                    return VIEW_TYPE_TITLE;
                                } else {
                                    return VIEW_TYPE_ITEM;
                                }
                            }
                            throw new RuntimeException("unExcepted item type");
                        }
                    });
                    break;
                }
                case STORAGE_PLAYBACK:{

                    break;
                }
            }
        }
    };

    private SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.CHINESE);
    private StringBuffer infoBuffer = new StringBuffer();

    //--------------------------------------------------
    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
//		initMatrix(((MViewRenderer)renderer).getMatrix(), width, height);

        if (videoView != null) {
            videoView.winSizeReset(width, height);
        }

        infoBuffer.append(String.format("\n[%s]onDisplay: %dx%d", hourFormat.format(new Date()), width, height));
        infoView.setText(infoBuffer.toString());

        if(mediaCtrl != null && mediaCtrl.getVisibility() == View.VISIBLE && player != null){
            mediaCtrl.setTime(player.getStartPosition(), player.getDuration());
        }
    }

    //--------------------------------------------------
    private class MySLDataSourceListener extends SLDataSourceListener {

        @Override
        public void onReConnecting(){
            infoBuffer.append(String.format("\n[%s]onReConnecting", hourFormat.format(new Date())));
            infoView.setText(infoBuffer.toString());
        }

        @Override
        public void onConnected(int mode, String ip, int port) {
            infoBuffer.append(String.format("\n[%s]onConnected\nmode: %d, ip: %s, port: %d", hourFormat.format(new Date()), mode, ip, port));
            infoView.setText(infoBuffer.toString());
        }

        @Override
        public void onAuth(int result) {
            infoBuffer.append(String.format("\n[%s]onAuth\nresult: %d", hourFormat.format(new Date()), result));
            infoView.setText(infoBuffer.toString());
        }

        @Override
        public void onDisconnected(int errcode) {
            infoBuffer.append(String.format("\n[%s]onDisconnected\nresult: %d", hourFormat.format(new Date()), errcode));
            infoView.setText(infoBuffer.toString());

//            Toast.makeText(DevicePreviewActivity.this, "连接断开", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDataRate(int upstreamRate, int downstreamRate) {
//            upstreamRate *= 8;
//            downstreamRate *= 8;

            String upRate = (upstreamRate > 1024) ? (upstreamRate / 1024) + "KB" : upstreamRate + "B";
            String downRate = (downstreamRate > 1024) ? (downstreamRate / 1024) + "KB" : downstreamRate + "B";

            rateView.setText(upRate + "↑  " + downRate + "↓");
        }
    }

    // --------------------------------------------------
    private class WindowTouchListener extends ViewTouchListener {

        int point1[] = {0, 0};
        boolean isSendConnand = false;

        public WindowTouchListener(VideoView[] vw) {
            super(vw);
        }

        @Override
        protected void onActionDown(View v, MotionEvent event) {
            super.onActionDown(v, event);
            VideoView win = (VideoView) v.getTag();
            if (!win.isDragable() && hasZoomIn() == false) {
                point1[0] = (int) event.getX();
                point1[1] = (int) event.getY();
            }
        }

        @Override
        protected void onActionPointerDown(View v, MotionEvent event) {
            super.onActionPointerDown(v, event);
        }

        @Override
        protected void onActionMove(View v, MotionEvent event) {
            super.onActionMove(v, event);
            VideoView win = (VideoView) v.getTag();
            if (!win.isDragable() && hasZoomIn() == false && (event.getPointerCount() == 1)) {
                if(isSendConnand == true){
                    return;
                }

                int x = (int) event.getX();
                int y = (int) event.getY();
                int xDistance = Math.abs(x - point1[0]);
                int yDistance = Math.abs(y - point1[1]);

                if(xDistance < MOVE_DISTANCE && yDistance < MOVE_DISTANCE){
                    return;
                }

//					if (xDistance > yDistance) {
//						if (x > point1[0]) {
//							changeImage(win, PTZCMD.PTZ_MV_LEFT);
//						} else {
//							changeImage(win, PTZCMD.PTZ_MV_RIGHT);
//						}
//					} else {
//						if (y > point1[1]) {
//							changeImage(win, PTZCMD.PTZ_MV_UP);
//						} else {
//							changeImage(win, PTZCMD.PTZ_MV_DOWN);
//						}
//					}

                point1[0] = x;
                point1[1] = y;
                isSendConnand = true;
            }
        }

        @Override
        protected void onActionPointerUp(View v, MotionEvent event) {
            super.onActionPointerUp(v, event);
        }

//			@Override
//			protected void onActionUp(View v, MotionEvent event) {
//				super.onActionUp(v, event);
//				if (isSendConnand == true) {
//					VideoView win = (VideoView) v.getTag();
//					win.stopPTZ();
//					isSendConnand = false;
//				}
//			}

    }
}
