package com.sl.achat;

import android.app.Application;
import android.content.SharedPreferences;

import com.sl.SLService;
import com.sl.db.DB;
import com.sl.db.DB.User;
import com.sl.db.DBService;
import com.sl.protocol.PStorageVerify;
import com.sl.usermgr.SLUserManager;

import java.util.ArrayList;
import java.util.List;

public class MainApplication extends Application {

    public static final int SL_USER = 0;
    public static final int SL_DEVICE = 1;

    public static final String LOGIN_SAVE_FILE = "si";
    public static final String LOGIN_NAME = "si_name";

    private User user = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }


    //---------------------------------------------------------------------------------------------
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    //---------------------------------------------------------------------------------------------
    public void startSLService(){
        DBService db = new DBService(this);

        ArrayList<DB.Device> list0 = new ArrayList<DB.Device>();

        SharedPreferences sp = getSharedPreferences(MainApplication.LOGIN_SAVE_FILE, MODE_PRIVATE);
        String uname = sp.getString(MainApplication.LOGIN_NAME, "");
        String pwd = "";

        DB.User user = db.queryUser(DB.User.SELECT_ONE, new String[]{uname});
        if(user != null){
            setUser(user);
            System.out.println("uname:" + uname + ", id:" + user.getId());

            db.executeSQL(DB.Device.UPDATE_STATUS_BY_USER, new Object[]{0, user.getId()}); //程序启动，清除状态信息

            db.queryDevice(list0, DB.Device.SELECT_BY_USER, new String[]{""+user.getId()});

            pwd = user.getPasswd();
        }
        db.close();

        SLService.SLDeviceInfo info = new SLService.SLDeviceInfo();

//		info.appOrdev = 1;
        info.sid = uname;
        info.passwd = pwd;
        info.channelCount = 1;
        info.audioCount = 1;
        info.videoStreamTypes = 1;
        info.maxConnections = 10;

        SLService service = SLService.getInstance();
        service.init(this, info);
        service.start();

        for(int i=0; i<list0.size(); i++){
            DB.Device dev = list0.get(i);

            System.out.println("sic:" + dev.getSid() + "," + dev.getUid());
            service.enablePreconnect(dev.getSid(), dev.getUid());
        }
    }

    public void stopSLService(){
        SLService.release();

        setUser(null);
    }

    //---------------------------------------------------------------------------------------------
    private List<PStorageVerify.StorageVasInfo> vlist = null;

    public void setStorageVasInfoList(List<PStorageVerify.StorageVasInfo>  l){
        vlist = l;
    }
    public List<PStorageVerify.StorageVasInfo> getVlist(){
        return vlist;
    }

    public PStorageVerify.StorageVasInfo getStorageVasInfo(long devid){
        if(vlist != null){
            for(int i=0; i<vlist.size(); i++){
                PStorageVerify.StorageVasInfo vas = vlist.get(i);
                if(devid == vas.devid){
                    return vas;
                }
            }
        }
        return null;
    }

    //---------------------------------------------------------------------------------------------

    public void logout(){
        SLUserManager.get().logout();

        setUser(null);

        SharedPreferences sp = getSharedPreferences(MainApplication.LOGIN_SAVE_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(MainApplication.LOGIN_NAME, "");
        editor.commit();
    }

}
