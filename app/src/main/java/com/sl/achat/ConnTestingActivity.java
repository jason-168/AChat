package com.sl.achat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.sl.SLChannel;
import com.sl.SLChannelListener;
import com.sl.SLService;
import com.sl.db.DB;
import com.sl.db.DB.Device;
import com.sl.db.DB.User;
import com.sl.db.DBService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ConnTestingActivity extends AppCompatActivity {

	private String[] punchfArr = new String[] { 
			"NOP:0",
			"LAN_UX:1",
			"SAME_EXTERIP:2",
			"REMOTE_TYPE_FULL:3",
			"LOCAL_TYPE_FULL:4",
			"LOCAL_TYPE_RESTRICTED:5",
			"REMOTE_TYPE_RESTRICTED:6",
			"UPNP_UX_FWD:7",
			"UPNP_UX_RVS:8",
			"TYPE_SYMMETRIC:9",
			"LAN_TCP:10",
			"WLAN_TCP:11",
			"UPNP_TCP_FWD:12",
			"SPECIFIED_RELAY_TCP:13",
	};

	private ArrayList<Device> list0 = new ArrayList<Device>();

	private Spinner spinner01 = null;
	private Spinner spinner02 = null;
	private Button button01 = null;
	private Button button02 = null;

	
	private TextView textview01 = null;
	private TextView textview02 = null;
	
	private ScrollView showinfo = null;
	
	private TextView textview03 = null;
	private TextView textview04 = null;//local
	private TextView textview05 = null;//peer
	
	private SLChannel channel = null;
	private MySLChannelListener channelListener = null;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conntesting);

		spinner01 = (Spinner) findViewById(R.id.spinner01);
		spinner02 = (Spinner) findViewById(R.id.spinner02);

		button01 = (Button) findViewById(R.id.button01);
		button02 = (Button) findViewById(R.id.button02);

		User user = ((MainApplication) getApplication()).getUser();

		list0.clear();
		if (user != null) {
			DBService db = new DBService(this);
			db.queryDevice(list0, DB.Device.SELECT_BY_USER, new String[] { ""
					+ user.getId() });
			db.close();
		}

		ArrayList<String> devlist = new ArrayList<String>();
		if (list0.size() > 0) {
			for (int i = 0; i < list0.size(); i++) {
				Device dev = list0.get(i);
				devlist.add(dev.getSid());
			}
		}
		ArrayAdapter<String> devAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, devlist);
		spinner01.setAdapter(devAdapter);

		ArrayList<String> punchflist = new ArrayList<String>(punchfArr.length);
		for (int i = 0; i < punchfArr.length; i++) {
			punchflist.add(punchfArr[i]);
		}
		ArrayAdapter<String> punchfAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, punchflist);
		spinner02.setAdapter(punchfAdapter);

		button01.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (channel == null) {
					if (list0.size() == 0) {
						return;
					}

					int devItem = spinner01.getSelectedItemPosition();
					Device dev = list0.get(devItem);
					
					int punchfItem = spinner02.getSelectedItemPosition();

					connectTo(dev.getSid(), dev.getUid(), dev.getUsername(),
							dev.getPasswd(), punchfItem);

					button01.setText("disconnect:" + dev.getSid() + "(" + punchfItem + ")");
				} else {
					disconnect();
					button01.setText("connect");
				}
			}
		});

		button02.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(showinfo.getVisibility() == View.GONE){
					if(channel == null){
						getDeviceInfo(null);
					}else{
						getDeviceInfo(_sid);
					}
					
					showinfo.setVisibility(View.VISIBLE);
					button02.setText("Hide");
				}else{
					showinfo.setVisibility(View.GONE);
					button02.setText("Show");
				}
			}
		});
		
		textview01 = (TextView) findViewById(R.id.textView1);
		textview02 = (TextView) findViewById(R.id.textView2);
		
		showinfo = (ScrollView) findViewById(R.id.showinfo);
		
		textview03 = (TextView) findViewById(R.id.textView3);
		textview04 = (TextView) findViewById(R.id.textView4);
		textview05 = (TextView) findViewById(R.id.textView5);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		disconnect();
	}

	private StringBuffer infoBuffer = null;
	private int authorized = -1;

	private String _sid = null;
	
	// 连接开始
	private void connectTo(String sid, long uid, String uname, String passwd, int punchf) {
		this._sid = sid;
		System.out.println("connect: " + sid);
		infoBuffer = new StringBuffer();

		channelListener = new MySLChannelListener();
		channel = new SLChannel(channelListener);
		channel.setPFlow(punchf);
		
		channel.setParams(sid, uid, uname, passwd);
		channel.connect();
		
		infoBuffer.append(String.format("\n[%s]onConnecting", dateFormat.format(new Date())));
		textview01.setText(infoBuffer.toString());
	}

	// 连接断开
	private void disconnect() {
		if (channel != null) {
			channel.disconnect();
			channel.release();
			channel = null;
		}
	}
	
	private void getDeviceInfo(String sid){
		String json = SLService.getInstance().getPreconnInfo(sid);
		PreconnInfo info = JSON.parseObject(json, PreconnInfo.class);
		if(info == null){
			return;
		}

		if(sid != null){
			StringBuffer preconnInfo = new StringBuffer();
			preconnInfo.append("currentflow:" + info.currentflow);
			preconnInfo.append("\npreconn_mode:" + info.preconn_mode);
			preconnInfo.append("\npreconn_ip:" + info.preconn_ip);
			preconnInfo.append(":" + info.preconn_port);
			
			preconnInfo.append("\npreconn_punchf:" + info.preconn_punchf);
			
			preconnInfo.append("\nstatus_req:" + info.preconn_status_req_ms);
			preconnInfo.append(", punchinfo_req:" + info.preconn_punchinfo_req_ms);
			preconnInfo.append(", lanseach_req:" + info.preconn_lanseach_req_ms);
			preconnInfo.append(", natinfo_req:" + info.preconn_natinfo_req_ms);
			preconnInfo.append(", punch_ms:" + info.preconn_punch_ms);
			
			preconnInfo.append("\npreconn_usingtime:" + info.preconn_usingtime);
			
			textview03.setText(preconnInfo.toString());
		}
		
		StringBuffer localInfo = new StringBuffer();
		if(info.localUPNPInfo != null){
			localInfo.append("Local UPNP:");
			localInfo.append("\nextip:" + info.localUPNPInfo.extip);
			localInfo.append("\ntcpp:" + info.localUPNPInfo.tcpp);
			localInfo.append("\nudpp:" + info.localUPNPInfo.udpp);
		}
		if(info.localNATInfo != null){
			localInfo.append("\nLocal NAT:");
			localInfo.append("\ntype:" + info.localNATInfo.type);
			localInfo.append("\nmode:" + info.localNATInfo.mode);
			localInfo.append("\nextip:" + info.localNATInfo.extip);
			localInfo.append("\nintip:" + info.localNATInfo.intip);
			localInfo.append("\nextp:" + info.localNATInfo.extp);
			localInfo.append("\nintp:" + info.localNATInfo.intp);
		}
		localInfo.append("\nlocalExtIP:" + info.localExtIP);
		localInfo.append("\nlocalExtTCPport:" + info.localExtTCPport);
		textview04.setText(localInfo.toString());
		
		if(sid != null){
			StringBuffer peerInfo = new StringBuffer();
			if(info.peerUPNPInfo != null){
				peerInfo.append("Peer UPNP:");
				peerInfo.append("\nextip:" + info.peerUPNPInfo.extip);
				peerInfo.append("\ntcpp:" + info.peerUPNPInfo.tcpp);
				peerInfo.append("\nudpp:" + info.peerUPNPInfo.udpp);
			}
			if(info.peerNATInfo != null){
				peerInfo.append("\nPeer NAT:");
				peerInfo.append("\ntype:" + info.peerNATInfo.type);
				peerInfo.append("\nmode:" + info.peerNATInfo.mode);
				peerInfo.append("\nextip:" + info.peerNATInfo.extip);
				peerInfo.append("\nintip:" + info.peerNATInfo.intip);
				peerInfo.append("\nextp:" + info.peerNATInfo.extp);
				peerInfo.append("\nintp:" + info.peerNATInfo.intp);
			}
			
			peerInfo.append("\npeerExtIP:" + info.peerExtIP);
			peerInfo.append("\npeerExtTCPport:" + info.peerExtTCPport);
			
			textview05.setText(peerInfo.toString());
		}
	}

	// -------------------------------------
	private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.CHINESE);
	
	private class MySLChannelListener extends SLChannelListener {
		@Override
		public void onReConnecting(){
			infoBuffer.append(String.format("\n[%s]onReConnecting", dateFormat.format(new Date())));
			textview01.setText(infoBuffer.toString());
		}
		
		@Override
		public void onConnected(int mode, String ip, int port) {
			infoBuffer.append(String.format("\n[%s]onConnected\nmode: %d, ip: %s, port: %d", dateFormat.format(new Date()), mode, ip, port));
			textview01.setText(infoBuffer.toString());
		}
	
		@Override
		public void onAuth(int result) {
			infoBuffer.append(String.format("\n[%s]onAuth\nresult: %d", dateFormat.format(new Date()), result));
			textview01.setText(infoBuffer.toString());
		}
	
		@Override
		public void onDisconnected(int errcode) {
			infoBuffer.append(String.format("\n[%s]onDisconnected\nresult: %d", dateFormat.format(new Date()), errcode));
			textview01.setText(infoBuffer.toString());
		}

		@Override
		public void onDataRate(int upstreamRate, int downstreamRate) {
			textview02.setText("up:" + upstreamRate + ", down:" + downstreamRate);
		}
	}

}
