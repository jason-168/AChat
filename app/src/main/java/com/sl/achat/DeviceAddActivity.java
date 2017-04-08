package com.sl.achat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.sl.db.DB;
import com.sl.db.DB.User;
import com.sl.db.DBService;

public class DeviceAddActivity extends Activity {

	private EditText device_sid, device_username, device_passwd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		setContentView(R.layout.activity_device_add);

		device_sid = (EditText) findViewById(R.id.device_sid);
		device_username = (EditText) findViewById(R.id.device_username);
		device_passwd = (EditText) findViewById(R.id.device_passwd);

		Intent extra = getIntent();
		String newsid = extra.getStringExtra("newsid");
		
		if(newsid != null && !"".equals(newsid.trim())){
			device_sid.setText(newsid);
		}

		findViewById(R.id.button01).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 存储
				boolean suc = update();

				if (suc == true) {
					// 做预连接
					String sid = device_sid.getText().toString();

					// 完成，返回。
					setResult(RESULT_OK);
					finish();
				}
			}
		});
	}

	private boolean update() {
		String sid = device_sid.getText().toString();
		String uname = device_username.getText().toString();
		String pwd = device_passwd.getText().toString();
		String name = sid;
		if (sid == null || "".equals(sid.trim())) {
			return false;
		}

		User user = ((MainApplication)getApplication()).getUser();
		
		DBService db = new DBService(this);
		db.executeSQL(DB.Device.UPDATE_PASSWD, new Object[] { name, uname, pwd, sid, user.getId() });
		db.close();

		Toast.makeText(this, "'" + sid + "', 添加成功", Toast.LENGTH_SHORT).show();
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
