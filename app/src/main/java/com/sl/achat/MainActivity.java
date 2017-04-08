package com.sl.achat;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.sl.SLChannel;
import com.sl.SLService;
import com.sl.SLSessionListener;
import com.sl.SLStateListener;
import com.sl.db.DB;
import com.sl.db.DBService;
import com.sl.prot.SLAVFormat;
import com.sl.protocol.Pbuddy;
import com.sl.usermgr.SLUserManager;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SLStateListener {

    private static final String BUILD_DATE = "";// [16-12-07 14:57]

    public static boolean started = false;

//    private FloatingActionButton fab = null;
    private FloatingActionButton fab_login = null;

    private TextView show_login_name_view = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, DeviceSearchActivity.class);
//                startActivity(intent);
//            }
//        });

        fab_login = (FloatingActionButton) findViewById(R.id.fab_login);
        fab_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);

        show_login_name_view = (TextView)headerView.findViewById(R.id.show_login_name_view);

        setContentFragment(R.id.nav_contacts, MainApplication.SL_USER);

        SLService svc = SLService.getInstance();
        svc.setStateListener(this);
        svc.setSLSessionListener(new MySLSessionListener());



        Intent extra = getIntent();
        long uidFromPush = extra.getLongExtra("uidFromPush", 0);
        int chFromPush = extra.getIntExtra("chFromPush", 0);
        if(uidFromPush != 0){
            LoadingActivity.previewFromPush(this, ((MainApplication)getApplication()).getUser(), uidFromPush, chFromPush);
        }

        Intent msIntent = new Intent(MainActivity.this, MainService.class);
        startService(msIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ((MainApplication)getApplication()).stopSLService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        DB.User user = ((MainApplication)this.getApplication()).getUser();
        if(user != null){
//            fab.setVisibility(View.VISIBLE);
            fab_login.setVisibility(View.GONE);
        }else{
//            fab.setVisibility(View.GONE);
            fab_login.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_buddy) {
            Intent intent = new Intent(MainActivity.this, DeviceSearchActivity.class);
            startActivity(intent);
            return true;
        }else if(id == R.id.action_logout){
            ((MainApplication)getApplication()).logout();

            deviceFragment.notifyDataSetChanged(null, null);

            fab_login.setVisibility(View.VISIBLE);
            show_login_name_view.setText("未登录");

            Intent intent = new Intent(this, PushService.class);
            stopService(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_contacts) {
            // Handle the camera action
            setContentFragment(id, MainApplication.SL_USER);
        }else if(id == R.id.nav_devices){
            setContentFragment(id, MainApplication.SL_DEVICE);
        }else if(id == R.id.nav_push){
            setContentFragment(id, MainApplication.SL_DEVICE);
        }else if(id == R.id.nav_storage){
            setContentFragment(id, MainApplication.SL_DEVICE);
        }
//        else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        }

//        else if (id == R.id.nav_settings) {
//            Intent intent = new Intent(MainActivity.this, ConnTestingActivity.class);
//            startActivity(intent);
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private DeviceFragment deviceFragment = null;

    private void setContentFragment(int id, int type){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        Fragment fragment = null;
        if (id == R.id.nav_contacts || id == R.id.nav_devices) {
            deviceFragment = DeviceFragment.newInstance(type, null);
            fragment = deviceFragment;
        }else if(id == R.id.nav_push){
            fragment = PushMgrFragment.newInstance(type, null);
        }else if(id == R.id.nav_storage){
            fragment = StorageFragment.newInstance(type, null);
        }

        transaction.replace(R.id.content_main, fragment);
        transaction.commit();
    }


    @Override
    public void onMineChanged(int state) {
        SharedPreferences sp = getSharedPreferences(MainApplication.LOGIN_SAVE_FILE, MODE_PRIVATE);
        String uname = sp.getString(MainApplication.LOGIN_NAME, "");

        if(state == 1){
            String pwd = "";

            DBService db = new DBService(MainActivity.this);
            DB.User user = db.queryUser(DB.User.SELECT_ONE,
                    new String[] { uname });
            if (user != null) {
                pwd = user.getPasswd();
            }
            db.close();

            if (uname != null && !"".equals(uname) && pwd != null
                    && !"".equals(pwd)) {
                Intent intent = new Intent(MainActivity.this,
                        PushService.class);
                intent.putExtra("uname", uname);
                intent.putExtra("pwd", pwd);
                startService(intent);
            }

            show_login_name_view.setText(uname + "[在线]");
        }else{
            if(state == 2999){
                Toast.makeText(this, "'" + uname + "',已在别处登录", Toast.LENGTH_SHORT).show();
            }
            show_login_name_view.setText(uname + "[离线]");
        }
    }

    @Override
    public void onPreconnectChanged(String sid, int state) {
        System.out.println("onPreconnectChanged, sid:" + sid + ", state:" + state);

        DB.User user = ((MainApplication)getApplication()).getUser();
        if(user != null){
            DBService db = new DBService(this);
            db.executeSQL(DB.Device.UPDATE_STATUS_BY_SID, new Object[] { state, sid, user.getId() });

            deviceFragment.notifyDataSetChanged(db, user);
            db.close();
        }
    }

    @Override
    public void onMessage(int reqtype, String jsondata) {
        System.out.println("reqtype:" + reqtype + ", jsondata:" + jsondata);
        switch(reqtype){
            case Pbuddy.BuddyAddReq.SL_USERMGR_REQ_ADD:{
                Pbuddy.BuddyAddReq req = JSON.parseObject(jsondata, Pbuddy.BuddyAddReq.class);

                buddyAddDialog(req);
                break;
            }
            case Pbuddy.BuddyAddRes.SL_USERMGR_RES_ADD:{
                Pbuddy.BuddyAddRes res = JSON.parseObject(jsondata, Pbuddy.BuddyAddRes.class);
//			if(res.rc) todo

                HashMap<Long, Object> buddy = (HashMap<Long, Object>)DeviceSearchActivity.buddyAdd.get((Long)res.bid);
                if(buddy != null){
                    buddySave((Integer)buddy.get("type"), "" + buddy.get("sid"), (Long)buddy.get("uid"));
                }
                break;
            }
        }
    }

    private void buddyAddDialog(final Pbuddy.BuddyAddReq req) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(req.uname + "请求添加您为好友");
        builder.setPositiveButton("接受", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                buddyAddRes(req, 0);

                dialog.dismiss();
            }
        });
        builder.setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                buddyAddRes(req, 1);

                dialog.dismiss();
            }
        }).show();
    }

    private void buddyAddRes(Pbuddy.BuddyAddReq req, int rc){
        DB.User user = ((MainApplication)this.getApplication()).getUser();

        Pbuddy.BuddyAddRes res = new Pbuddy.BuddyAddRes();
        res.uid = req.uid;
        res.bid = req.bid;
        res.rc = rc;
        res.uname = user.getUsername();

        SLUserManager.get().response(Pbuddy.BuddyAddRes.SL_USERMGR_RES_ADD, JSON.toJSONString(res));

        if(rc == 0){
            System.out.println("buddySave" + req.uname + ",req.uid:" + req.uid + ",bid" + req.bid);
            buddySave(SLService.SLDeviceInfo.SL_APP, req.uname, req.uid);
        }
    }

    private void buddySave(int buddytype, String sid, long uid){
        DB.User user = ((MainApplication)getApplication()).getUser();

        DBService db = new DBService(this);
        DB.Device dev = db.queryDevice(DB.Device.SELECT_ONE_SID, new String[] {sid, ""+user.getId()});

        if(dev != null){
            Toast.makeText(this, "'" + sid + "',已存在", Toast.LENGTH_SHORT).show();
        }else{
            db.executeSQL(DB.Device.getInsertDML(), new Object[] {null, user.getId(), sid, sid, uid,
                    "", 0, "", "", 1, 0, buddytype});

            deviceFragment.notifyDataSetChanged(db, user);

            db.close();

            if(buddytype == SLService.SLDeviceInfo.SL_DEV){//device
                Intent intent = new Intent(this, DeviceAddActivity.class);
                intent.putExtra("newsid", sid);
                startActivity(intent);
            }else{//user
                Toast.makeText(this, "'" + sid + "', 添加成功", Toast.LENGTH_SHORT).show();
            }

            SLService.getInstance().enablePreconnect(sid, uid);
        }
    }

    public static byte[] glock = new byte[0];
    public static SLChannel callingChannel = null;

    private static OutgoingListener mOutgoingListener = null;

    public static void setOutgoingListener(OutgoingListener l){
        mOutgoingListener = l;
    }

    private static int genID = 0;
    private static int callingID = 0;

    public static int requestCall(){
        synchronized (glock) {
            if (callingID != 0) {
                return -1;
            }
            callingID = ++genID;
            return callingID;
        }
    }

    public static void endCall(int id){
        synchronized (glock) {
            if(callingID != 0 && callingID == id){
                callingID = 0;
            }
        }
    }

    private class MySLSessionListener extends SLSessionListener {
        @Override
        public int incomingSession(SLChannel channel, String sid, String username, String passwd, int reqtype) {
            System.out.println("incomingSession, sid:" + sid + ", username:" + username + ", reqtype:" + reqtype + ", callingChannel == null:" + (callingChannel == null));
            if(reqtype == 2 || reqtype == 3) {
                synchronized (glock) {
                    if(callingID != 0){
                        return -1;
                    }
                    callingID = ++genID;

                    callingChannel = channel;
                }

                Intent intent;
                if(reqtype == 2){
                    intent = new Intent(MainActivity.this, VoiceCallActivity.class);
                }else{
                    intent = new Intent(MainActivity.this, VideoCallActivity.class);
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("sid", sid);
                intent.putExtra("callingID",callingID);

                MainActivity.this.startActivity(intent);
                return 1;
            }
            return -1;
        }

        @Override
        public void outgoingSession(SLChannel channel) {
            System.out.println("outgoingSession.");
            synchronized (glock) {
                if(channel == callingChannel){
                    if(mOutgoingListener != null){
                        mOutgoingListener.onOutgoingSession();
                        mOutgoingListener = null;
                    }
                    callingChannel = null;
                }
            }
        }

        @Override
        public boolean hasAudioPermission(String username, int channelNum_req) {
            System.out.println("hasAudioPermission,username:" + username + ",channelNum_req:" + channelNum_req);
            return false;
        }

        @Override
        public int onAudioOpen(long abuffer, int channelNum_req, SLAVFormat.SLAudioFormat afmt_res) {
            System.out.println("onAudioOpen:" + channelNum_req);
            return 0;
        }

        @Override
        public void onAudioClose(int channelNum_req) {
            System.out.println("onAudioClose:" + channelNum_req);
        }

        @Override
        public boolean hasVideoPermission(String username, int channelNum_req,
                                          int videoStreamType_req) {
            System.out.println("hasVideoPermission,username:" + username + ",channelNum_req:" + channelNum_req);
            return false;
        }

        @Override
        public int onVideoOpen(long vbuffer, int channelNum_req,
                               int videoStreamType_req, SLAVFormat.SLVideoFormat vfmt_res) {
            System.out.println("onVideoOpen:" + channelNum_req);
            return 0;
        }

        @Override
        public void onVideoClose(int channelNum_req, int videoStreamType_req) {
            System.out.println("onVideoClose:" + channelNum_req);
        }
    }

    //----------------------------------------------------------------------------------
    private long presstime = 0l;
    private int clicks = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            long t1 = System.currentTimeMillis();
            long t = t1 - presstime;
            clicks++;
            if(t > 3000){
                presstime = t1;
                clicks = 1;
            }
            if(clicks == 1){
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            }else if(clicks == 2){
                finish();
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
