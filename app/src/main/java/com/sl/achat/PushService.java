package com.sl.achat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.NotificationCompat;

import com.alibaba.fastjson.JSON;
import com.sl.protocol.Ppushinfo.PCS_GetPushUserInfo;
import com.sl.protocol.Ppushinfo.PCS_GetPushUserInfoRes;
import com.sl.protocol.Ppushinfo.PCS_PushUserReg;
import com.sl.protocol.Ppushmsg;
import com.sl.push.SLPushService;
import com.sl.push.SLPushServiceListener;

public class PushService extends Service {

	private static PushService pushService = null;

	private String uuid = "";

	/** Notification构造器 */
//	private NotificationCompat.Builder mNotifyBuilder;
	/** Notification的ID */
    private static final int notifyPreviewId = 1001;

	private static final int notifyId = 1002;

	private boolean connected = false;
	
	@Override
	public void onCreate() {
		super.onCreate();
		pushService = this;

		Intent intent = new Intent(this.getBaseContext(), PushService.InnerService.class);
		startService(intent);

		uuid = Installation.id(getApplication());

		SLPushService pushsvc = SLPushService.getInstance();
		pushsvc.init(getApplication());
		
		pushsvc.setStateListener(new MySLPushServiceListener());

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		System.out.println("PushService, onDestroy");

		SLPushService.release();

		pushService = null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent != null) {
			String uname = intent.getStringExtra("uname");
			String pwd = intent.getStringExtra("pwd");
			System.out.println("PushService, onStartCommand,uname:" + uname);

			if (connected == false) {
				SLPushService pushsvc = SLPushService.getInstance();
				pushsvc.start(0, uname, pwd);
			}
		}
		
		return Service.START_STICKY;
	}



	@Override
	public IBinder onBind(Intent intent) {
		System.out.println("PushService, onBind");
		return null;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		System.out.println("PushService, onUnbind");
		return super.onUnbind(intent);
	}



	/** 初始化通知栏 */
	private NotificationCompat.Builder getNotificationBuilder(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("标题")
//				.setContentText("测试内容")
				.setContentIntent(getDefalutIntent(Notification.FLAG_AUTO_CANCEL))
//				.setNumber(number)//显示数量
//				.setTicker("测试通知来啦")//通知首次出现在通知栏，带上升动画效果的
//				.setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示
				.setPriority(Notification.PRIORITY_DEFAULT)//设置该通知优先级
				.setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
				.setDefaults(Notification.DEFAULT_SOUND)
                .setSmallIcon(R.mipmap.ic_launcher);

        return builder;
	}
    /**
     * @获取默认的pendingIntent,为了防止2.3及以下版本报错
     * @flags属性:
     * 在顶部常驻:Notification.FLAG_ONGOING_EVENT
     * 点击去除： Notification.FLAG_AUTO_CANCEL
     */
    private PendingIntent getDefalutIntent(int flags){
        PendingIntent pendingIntent= PendingIntent.getActivity(this, 1, new Intent(), flags);
        return pendingIntent;
    }


	/** 显示通知栏 */
	public void showNotify(String alert, Ppushmsg.MsgBody msgBody){
		NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//		mgr.cancel(notifyId);//删除一个特定的通知ID对应的通知
        NotificationCompat.Builder builder = getNotificationBuilder();
        builder.setContentTitle(alert)
				.setContentText(msgBody.ctx)
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示
//				.setNumber(number)//显示数量
				.setTicker(alert);//通知首次出现在通知栏，带上升动画效果的

		mgr.notify(notifyId, builder.build());
//		mNotification.notify(getResources().getString(R.string.app_name), notiId, mBuilder.build());
	}

    public void showPreview(String alert, Ppushmsg.MsgBody msgBody){
        NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = getNotificationBuilder();
        builder.setAutoCancel(true)//点击后让通知将消失
                .setContentTitle(alert)
                .setContentText("点击查看")
                .setTicker(alert);
        //点击的意图ACTION是跳转到Intent
        Intent resultIntent = new Intent(this, LoadingActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultIntent.putExtra("uidFromPush", msgBody.devid);
        resultIntent.putExtra("chFromPush", msgBody.ch);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        mgr.notify(notifyPreviewId, builder.build());
    }

	private class MySLPushServiceListener extends SLPushServiceListener{
		@Override
		public void onConnected() {
			System.out.println("PushService, onConnected");
			connected = true;

            handler.sendEmptyMessageDelayed(REQ_REG_INFO, 0);
		}
	
		@Override
		public void onDisconnected(int errcode) {
			System.out.println("PushService, onDisconnected:" + errcode);
			connected = false;
		}
		
		public void onResponse(int reqtype, int result, String jsondata){
			System.out.println("PushService, onResponse, reqtype:" + reqtype + ", jsondata:" + jsondata);
			switch(reqtype){
			case PCS_GetPushUserInfo.PCS_GetPushUserInfo_TYPE:{
				if(result != 0){
					break;
				}
				PCS_GetPushUserInfoRes res = JSON.parseObject(jsondata, PCS_GetPushUserInfoRes.class);
				if(res.rc != 0 || !uuid.equals(res.token)){
					SLPushService pushsvc = SLPushService.getInstance();
					
					PCS_PushUserReg reg = new PCS_PushUserReg();
					reg.uid = pushsvc.myUID();
					reg.ptype = 0;
					reg.plang = 0;
					reg.appid = "test-appid";//到时由服务器分配
					reg.token = uuid;
					
					pushsvc.request(PCS_PushUserReg.PCS_PushUserReg_TYPE, JSON.toJSONString(reg));
				}
                handler.removeMessages(REQ_REG_INFO);
				break;
			}
            case PCS_PushUserReg.PCS_PushUserReg_TYPE:{
				if(result != 0) return;
                break;
            }
			}
		}
		
		public void onMessage(String alert, String msg){
			System.out.println("PushService, onMessage, alert:" + alert + ", msg:" + msg);
//			Ppushmsg.MsgBody msgBody = JSON.parseObject(msg, Ppushmsg.MsgBody.class);
//
//			DBService db = new DBService(PushService.this);
//			//(_id,_alert,_mtype,_devid,_ch,_ctx) values (?, ?, ?, ?, ?, ?)
//			db.executeSQL(DB.PushMsg.getInsertDML(), new Object[] {null, alert, msgBody.mtype, msgBody.devid, msgBody.ch, msgBody.ctx});
//			db.close();

			PushMsg pmsg = new PushMsg(alert, msg);
			Message m = handler.obtainMessage(SHOW_NOTIFY, pmsg);
			handler.sendMessage(m);
		}
	}

	private void reqReginfo(){
        if(connected == false){
            return;
        }
        SLPushService pushsvc = SLPushService.getInstance();

        PCS_GetPushUserInfo req = new PCS_GetPushUserInfo();
        req.uid = pushsvc.myUID();
        pushsvc.request(PCS_GetPushUserInfo.PCS_GetPushUserInfo_TYPE, JSON.toJSONString(req));

        handler.sendEmptyMessageDelayed(REQ_REG_INFO, 600000);//10 * 60 * 1000
    }

    private static final int REQ_REG_INFO = 1;

	private static final int SHOW_NOTIFY = 10;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
                case REQ_REG_INFO: {
                    reqReginfo();
                    break;
                }
				case SHOW_NOTIFY: {
                    PushMsg pushMsg = (PushMsg)msg.obj;
                    Ppushmsg.MsgBody msgBody = JSON.parseObject(pushMsg.msg, Ppushmsg.MsgBody.class);
                    if(msgBody.mtype == 1) {
                        showNotify(pushMsg.alert, msgBody);
                    }else{
                        showPreview(pushMsg.alert, msgBody);
                    }
					break;
				}
			}
		}
	};

	private static class PushMsg{
		public PushMsg(String alert, String msg){
			this.alert = alert;
			this.msg = msg;
		}
		public String alert;
		public String msg;
	}


	public static class InnerService extends Service {
		public InnerService() {
		}

		@Override
		public IBinder onBind(Intent intent) {
			return null;
		}

		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			MainService.setForeground(PushService.pushService, this);
			return super.onStartCommand(intent, flags, startId);
		}
	}
}
