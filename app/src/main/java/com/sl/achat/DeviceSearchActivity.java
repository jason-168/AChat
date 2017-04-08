package com.sl.achat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.sl.SLService;
import com.sl.db.DB.User;
import com.sl.protocol.Pbuddy.BuddyAddReq;
import com.sl.protocol.Puser;
import com.sl.protocol.Puser.DevSearchReq;
import com.sl.protocol.Puser.DevSearchRes;
import com.sl.protocol.Puser.UserSearchReq;
import com.sl.protocol.Puser.UserSearchRes;
import com.sl.usermgr.SLUserManager;
import com.sl.usermgr.SLUserManagerListener;
import com.sl.view.MaterialSearchView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeviceSearchActivity extends AppCompatActivity {

    private MaterialSearchView searchView;

    private ListView listview = null;
    private SimpleAdapter simpleAdapter = null;
    private ArrayList<HashMap<String, Object>> datalist = new ArrayList<HashMap<String, Object>>();

    private SLUserManager mgr = null;

//    private int clickItem = 0;
//    public static HashMap<String, Object> mapAdd = null;

    public static HashMap<Long, Object> buddyAdd = new HashMap<Long, Object>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setVoiceSearch(false);
        searchView.setCursorDrawable(R.drawable.color_cursor_white);

        searchView.setSearchTypes(new int[]{R.drawable.ic_contacts, R.drawable.ic_videocam});
        searchView.setOnQueryTextListener(mOnQueryTextListener);

        listview = (ListView) findViewById(R.id.listview);
        simpleAdapter = new SimpleAdapter(this, datalist, R.layout.search_list_item,
                new String[]{"icon", "sid"}, new int[]{android.R.id.icon, android.R.id.text1});
        listview.setAdapter(simpleAdapter);
        listview.setOnItemClickListener(datalistClickListener);

        mgr = SLUserManager.get();
        mgr.setSLUserManagerListener(new MySLUserManagerListener());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SLUserManager.release();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.search_menu, menu);
//        MenuItem item = menu.findItem(R.id.search_contact);
//        mSearchView = (SearchView) MenuItemCompat.getActionView(item);
//        mSearchView.setIconifiedByDefault(false);
//        mSearchView.setOnQueryTextListener(mOnQueryTextListener);
//        return true;
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    private AdapterView.OnItemClickListener datalistClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            HashMap<String, Object> map = datalist.get(position);
            dialog(map);
        }
    };

    private void dialog(final HashMap<String, Object> map) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceSearchActivity.this);
        builder.setTitle("提示");
        builder.setMessage("确定添加"+map.get("sid")+"吗?");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {// 添加确定按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {// 确定按钮的响应事件
                User user = ((MainApplication)getApplication()).getUser();
                BuddyAddReq req = new BuddyAddReq();
                req.msgid = 0;
                req.uid = SLService.getInstance().myUID();
                req.bid = (Long)map.get("uid");
                req.uname = user.getUsername();
                mgr.request(BuddyAddReq.SL_USERMGR_REQ_ADD, JSON.toJSONString(req));

                buddyAdd.put((Long)map.get("uid"), map);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {// 添加返回按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {// 响应事件
                dialog.dismiss();
            }
        }).show();
    }

    private MaterialSearchView.OnQueryTextListener mOnQueryTextListener = new MaterialSearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            User user = ((MainApplication)getApplication()).getUser();
            if(user == null){
                Toast.makeText(DeviceSearchActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
                return true;
            }

            doSearch(MainApplication.SL_USER, query);

            return false;
        }

        @Override
        public boolean onQueryItemSubmit(int res, String query) {
//            System.out.println("onQueryItemSubmit:" + query);
            User user = ((MainApplication)getApplication()).getUser();
            if(user == null){
                Toast.makeText(DeviceSearchActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
                return true;
            }

            int search_type = MainApplication.SL_USER;
            if(res != R.drawable.ic_contacts){
                search_type = MainApplication.SL_DEVICE;
            }
            doSearch(search_type, query);

            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return true;
        }
    };

    private void doSearch(int type, String text){
        if(TextUtils.isEmpty(text) || isUnameInvalid(text)){
            Toast.makeText(DeviceSearchActivity.this, "不能含有特殊字符", Toast.LENGTH_SHORT).show();
            return;
        }

        int reqtype = 0;
        String jsondata = "";
        if(type == 0){
            reqtype = UserSearchReq.SL_USERMGR_REQ_USER_SEARCH;

            UserSearchReq req = new UserSearchReq();
            req.msgid = 0;
            req.commid = SLService.getInstance().commID();
            req.name = text;
            req.idx = 0;
            req.limit = 10;

            jsondata = JSON.toJSONString(req);
        }else{
            reqtype = DevSearchReq.SL_USERMGR_REQ_DEV_SEARCH;

            DevSearchReq req = new DevSearchReq();
            req.msgid = 0;
            req.commid = SLService.getInstance().commID();
            req.name = text;
            req.idx = 0;
            req.limit = 10;

            jsondata = JSON.toJSONString(req);
        }
        mgr.request(reqtype, jsondata);
    }

    private boolean isUnameInvalid(String email) {
        // 编译正则表达式
        Pattern pattern = Pattern.compile(UserRegActivity.UNAME_VALID);
        // 忽略大小写的写法
        Matcher matcher = pattern.matcher(email);
        // 字符串是否与正则表达式相匹配
        return matcher.find();
    }

    private class MySLUserManagerListener extends SLUserManagerListener {
        @Override
        public void onResponse(int reqtype, int result, String jsondata){
            System.out.println("result:" + result + "," + jsondata);
            switch(reqtype){
                case UserSearchReq.SL_USERMGR_REQ_USER_SEARCH:{
                    if(result == 0){
                        UserSearchRes res = JSON.parseObject(jsondata, UserSearchRes.class);
                        if(res.rc == 0){
                            datalist.clear();

                            int size = res.userList != null ? res.userList.size() : 0;
                            if(size > 0) {
                                for (int i = 0; i < size; i++) {
                                    HashMap<String, Object> m = new HashMap<String, Object>(4);
                                    Puser.UserVo user = res.userList.get(i);
                                    m.put("icon", R.drawable.ic_contacts);

                                    m.put("uid", Long.valueOf(user.uid));
                                    m.put("sid", user.uname);
                                    m.put("type", MainApplication.SL_USER);
                                    datalist.add(m);
                                }
                            }else{
                                Toast.makeText(DeviceSearchActivity.this, "搜索结果为0", Toast.LENGTH_SHORT).show();
                            }

                            simpleAdapter.notifyDataSetChanged();
                        }
                    }
                    break;
                }
                case DevSearchReq.SL_USERMGR_REQ_DEV_SEARCH:{
                    if(result == 0){
                        DevSearchRes res = JSON.parseObject(jsondata, DevSearchRes.class);
                        if(res.rc == 0){
                            datalist.clear();

                            int size = res.devList != null ? res.devList.size() : 0;
                            if(size > 0) {
                                for (int i = 0; i < size; i++) {
                                    HashMap<String, Object> m = new HashMap<String, Object>(4);
                                    Puser.DevVo dev = res.devList.get(i);
                                    m.put("icon", R.drawable.ic_videocam);

                                    m.put("uid", Long.valueOf(dev.devid));
                                    m.put("sid", dev.devname);
                                    m.put("type", MainApplication.SL_DEVICE);
                                    datalist.add(m);
                                }
                            }else{
                                Toast.makeText(DeviceSearchActivity.this, "搜索结果为0", Toast.LENGTH_SHORT).show();
                            }

                            simpleAdapter.notifyDataSetChanged();
                        }
                    }
                    break;
                }
//                case BuddyAddReq.SL_USERMGR_REQ_ADD:{
//                    if(result == 0){
//                        BuddyAddRes res = JSON.parseObject(jsondata, BuddyAddRes.class);
//                        if(res.rc == 0 || res.rc == 2){
//                            HashMap<String, Object> map = datalist.get(clickItem);
//
//                            String sid = ""+map.get("sid");
//                            int buddytype = (Integer)map.get("type");
//
//                            User user = ((MainApplication)getApplication()).getUser();
//
//                            DBService db = new DBService(getBaseContext());
//                            Device dev = db.queryDevice(DB.Device.SELECT_ONE_SID, new String[] {sid, ""+user.getId()});
//
//                            if(dev != null){
//                                Toast.makeText(DeviceSearchActivity.this, "'" + sid + "',已添加", Toast.LENGTH_SHORT).show();
//                            }else{
//                                db.executeSQL(DB.Device.getInsertDML(), new Object[] {null, user.getId(), sid, sid, map.get("uid"),
//                                        "", 0, "", "", 1, 0, buddytype});
//                                db.close();
//
////                                if(buddytype == MainApplication.SL_DEVICE){//device
//////                                    Intent intent = new Intent(DeviceSearchActivity.this, BuddyAddActivity.class);
//////                                    intent.putExtra("newsid", sid);
//////                                    startActivity(intent);
////                                }else{//user
////
////                                }
//
//                                Toast.makeText(DeviceSearchActivity.this, "'" + sid + "', 添加成功", Toast.LENGTH_SHORT).show();
//
//                                finish();
//                            }
//                        }
//                    }
//                    break;
//                }
            }
        }
    }
}
