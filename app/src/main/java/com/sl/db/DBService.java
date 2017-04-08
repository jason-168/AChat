package com.sl.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sl.db.DB.Device;
import com.sl.db.DB.User;

import java.util.ArrayList;

public class DBService {

    private DBHelper helper = null;
    private SQLiteDatabase db = null;

    public DBService(Context context) {
        this.helper = new DBHelper(context);
        this.db = helper.getWritableDatabase();
    }

    public DBService(String path) {
        this.db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
    }

    public void executeSQL(String sql) {
        db.execSQL(sql);
    }

    public void executeSQL(String sql, Object[] bindArgs) {
        db.execSQL(sql, bindArgs);
    }

    public Cursor query(String sql, String[] selectionArgs) {
        return db.rawQuery(sql, selectionArgs);
    }

    public void close() {
        if (db != null) {
            db.close();
            db = null;
        }
        if (helper != null) {
            helper.close();
            helper = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    //----------------------------------------------
    public class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context) {
            super(context, DB.DATABASE_NAME, null, DB.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB.Device.getTableDDL());
            db.execSQL(DB.User.getTableDDL());
            db.execSQL(DB.PushMsg.getTableDDL());
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < newVersion) {
                dropTable(db, DB.Table.TB_DEVIDE);
                dropTable(db, DB.Table.TB_USER);
                dropTable(db, DB.Table.TB_PUSHMSG);
                onCreate(db);
            }
        }

        private void dropTable(SQLiteDatabase db, String table) {
            db.execSQL("drop table if exists " + table);
        }
    }

    public void clear(String table) {
        executeSQL("delete from " + table);
    }

    //-------------------------------------------------------------------------------------
    private void queryUser(User user, Cursor c) {
        user.setId(c.getInt(c.getColumnIndexOrThrow(User.ID)));
        user.setUsername(c.getString(c.getColumnIndexOrThrow(User.USERNAME)));
        user.setPasswd(c.getString(c.getColumnIndexOrThrow(User.PASSWD)));
    }

    public User queryUser(String sql, String[] selectionArgs){
        User user = null;
        Cursor c = query(sql, selectionArgs);
        if (c.moveToNext()) {
            user = new User();
            queryUser(user, c);
        }
        return user;
    }

    //-------------------------------------------------------------------------------------
    private void queryDevice(Device device, Cursor c) {
        device.setId(c.getInt(c.getColumnIndexOrThrow(Device.ID)));
        device.setUserID(c.getInt(c.getColumnIndexOrThrow(Device.USERID)));
        device.setName(c.getString(c.getColumnIndexOrThrow(Device.NAME)));
        device.setSid(c.getString(c.getColumnIndexOrThrow(Device.SID)));
        device.setUid(c.getLong(c.getColumnIndexOrThrow(Device.UID)));
        device.setIp(c.getString(c.getColumnIndexOrThrow(Device.IP)));
        device.setPort(c.getInt(c.getColumnIndexOrThrow(Device.PORT)));
        device.setUsername(c.getString(c.getColumnIndexOrThrow(Device.USERNAME)));
        device.setPasswd(c.getString(c.getColumnIndexOrThrow(Device.PASSWD)));
        device.setChannels(c.getInt(c.getColumnIndexOrThrow(Device.CHANNELS)));
        device.setStatus(c.getInt(c.getColumnIndexOrThrow(Device.STATUS)));
        device.setType(c.getInt(c.getColumnIndexOrThrow(Device.TYPE)));
    }

    public Device queryDevice(String sql, String[] selectionArgs) {
        Device device = null;
        Cursor c = query(sql, selectionArgs);
        if (c.moveToNext()) {
            device = new Device();
            queryDevice(device, c);
        }
        return device;
    }

    public void queryDevice(ArrayList<Device> emptyList, String sql, String[] selectionArgs) {
        Cursor c = query(sql, selectionArgs);
        emptyList.clear();
        while (c.moveToNext()) {
            Device device = new Device();
            queryDevice(device, c);
            emptyList.add(device);
        }
        c.close();
    }

}
