package com.sl.db;

/**
 * 数据库描述类
 *
 * @author 1
 */
public final class DB {

    //数据库名
    public static final String DATABASE_NAME = "achat.db";
    public static final int DATABASE_VERSION = 1;

    public static class Table {
        public static final String TB_USER = "tb_user";
        public static final String TB_DEVIDE = "tb_device";

        public static final String TB_PUSHMSG   = "tb_pushmsg";
    }

    public static class User{
        public static final String ID = "_id";            //int 主键
        public static final String USERNAME = "_username";        //String 登录用户名
        public static final String PASSWD = "_passwd";        //String 登录密码

        private int id = 0;
        private String username = "";
        private String passwd = "";

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPasswd() {
            return passwd;
        }

        public void setPasswd(String passwd) {
            this.passwd = passwd;
        }

        public static String getTableDDL() {
            String table = "CREATE TABLE IF NOT EXISTS [" + DB.Table.TB_USER + "] (" +
                    "[_id] INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "[_username] VARCHAR2(32)," +
                    "[_passwd] VARCHAR2(32))";
            return table;
        }

        public static final String INSERT = "insert into " + DB.Table.TB_USER + " (_id,_username,_passwd) values (?,?,?)";
        public static final String UPDATE_ONE = "update " + DB.Table.TB_USER + " set _passwd=? where _id=?";
        public static final String SELECT_ONE = "select * from " + DB.Table.TB_USER + " where _username=?";
    }

    public static class Device {
        public static final String ID = "_id";            //int 主键
        public static final String USERID = "_userID";        //User ID
        public static final String NAME = "_name";            //String 名称
        public static final String SID = "_sid";            //String sid
        public static final String UID = "_uid";            //long uid
        public static final String IP = "_ip";            //String IP 地址
        public static final String PORT = "_port";            //int 端口
        public static final String USERNAME = "_username";        //String 登录用户名
        public static final String PASSWD = "_passwd";        //String 登录密码
        public static final String CHANNELS = "_channels";        //int 通道数
        public static final String STATUS = "_status";        //int 状态, 0:不在线， 1:在线
        public static final String TYPE = "_type";            //int 0:APP, 1:DEV

        private int id = 0;
        private int userID = 0;
        private String name = "";
        private String sid = "";
        private long uid = 0;
        private String ip = "";
        private int port = 0;
        private String username = "";
        private String passwd = "";
        private int channels = 0;
        private int status = 0;
        private int type = 0;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getUserID() {
            return userID;
        }

        public void setUserID(int userID) {
            this.userID = userID;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSid() {
            return sid;
        }

        public void setSid(String sid) {
            this.sid = sid;
        }

        public long getUid() {
            return uid;
        }

        public void setUid(long uid) {
            this.uid = uid;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPasswd() {
            return passwd;
        }

        public void setPasswd(String passwd) {
            this.passwd = passwd;
        }

        public int getChannels() {
            return channels;
        }

        public void setChannels(int channels) {
            this.channels = channels;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public static String getTableDDL() {
            String table = "CREATE TABLE IF NOT EXISTS [" + DB.Table.TB_DEVIDE + "] (" +
                    "[_id] INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "[_userID] INTEGER," +
                    "[_name] VARCHAR2(32)," +
                    "[_sid] VARCHAR2(32)," +
                    "[_uid] INTEGER," +
                    "[_ip] VARCHAR2(46)," +
                    "[_port] INTEGER," +
                    "[_username] VARCHAR2(32)," +
                    "[_passwd] VARCHAR2(32)," +
                    "[_channels] INTEGER," +
                    "[_status] INTEGER," +
                    "[_type] INTEGER)";
            return table;
        }

        public static String getInsertDML() {
            return "insert into " + DB.Table.TB_DEVIDE + " (_id,_userID,_name,_sid,_uid,_ip,_port,_username,_passwd,_channels,_status,_type) values (?,?,?,?,?,?,?,?,?,?,?,?)";
        }

        public static String getUpdateDML() {
            return "update " + DB.Table.TB_DEVIDE + " set _userID=?,_name=?,_sid=?,_uid=?,_ip=?,_port=?,_username=?,_passwd=?,_channels=?,_status=?,_type=? where _id=?";
        }

        public static final String DEL_BY_ID = "delete from " + DB.Table.TB_DEVIDE + " where _id=?";
        public static final String DEL_BY_SID = "delete from " + DB.Table.TB_DEVIDE + " where _sid=? and _userID=?";

        public static final String UPDATE_STATUS_BY_USER = "update " + DB.Table.TB_DEVIDE + " set _status=? where _userID=?";
        public static final String UPDATE_STATUS_BY_SID = "update " + DB.Table.TB_DEVIDE + " set _status=? where _sid=? and _userID=?";

        public static final String SELECT_BY_USER = "select * from " + DB.Table.TB_DEVIDE + " where _userID=?";
        public static final String SELECT_BY_TYPE = "select * from " + DB.Table.TB_DEVIDE + " where _userID=? and _type=?";

        public static final String SELECT_ONLINK_BY_TYPE = "select * from " + DB.Table.TB_DEVIDE + " where _status>0 and _userID=? and _type=?";

        public static final String SELECT_ONE = "select * from " + DB.Table.TB_DEVIDE + " where _id=?";
        public static final String SELECT_ONE_SID = "select * from " + DB.Table.TB_DEVIDE + " where _sid=? and _userID=?";
        public static final String SELECT_ONE_UID = "select * from " + DB.Table.TB_DEVIDE + " where _uid=? and _userID=?";
        
        public static final String UPDATE_PASSWD = "update " + DB.Table.TB_DEVIDE + " set _name=?,_username=?,_passwd=? where _sid=? and _userID=?";
    }

    public static class PushMsg{
        public static final String ID       = "_id";            //int 主键
        public static final String ALERY    = "_alert";
        public static final String MTYPE    = "_mtype";
        public static final String DEVID    = "_devid";
        public static final String CH       = "_ch";
        public static final String CTX      = "_ctx";

        private int     id;
        private String  alert;
        private int		mtype; //0:devmsg, 1:user define
        private long	devid;
        private int		ch;
        private String	ctx; //user define json

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getAlert() {
            return alert;
        }

        public void setAlert(String alert) {
            this.alert = alert;
        }

        public int getMtype() {
            return mtype;
        }

        public void setMtype(int mtype) {
            this.mtype = mtype;
        }

        public long getDevid() {
            return devid;
        }

        public void setDevid(long devid) {
            this.devid = devid;
        }

        public int getCh() {
            return ch;
        }

        public void setCh(int ch) {
            this.ch = ch;
        }

        public String getCtx() {
            return ctx;
        }

        public void setCtx(String ctx) {
            this.ctx = ctx;
        }

        public static String getTableDDL() {
            String table = "CREATE TABLE IF NOT EXISTS [" + DB.Table.TB_PUSHMSG + "] (" +
                    "[_id] INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "[_alert] VARCHAR2(256)," +
                    "[_mtype] INTEGER," +
                    "[_devid] INTEGER," +
                    "[_ch] INTEGER," +
                    "[_ctx] VARCHAR2(1024))";
            return table;
        }

        public static String getInsertDML() {
            return "insert into " + DB.Table.TB_PUSHMSG + " (_id,_alert,_mtype,_devid,_ch,_ctx) values (?, ?, ?, ?, ?, ?)";
        }
    }

}
