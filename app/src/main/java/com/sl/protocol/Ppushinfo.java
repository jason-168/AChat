package com.sl.protocol;

import java.util.List;

public class Ppushinfo {
	
	/******************************************************************************/
	public static class PCS_PushUserReg {
		public static final int PCS_PushUserReg_TYPE = 1;
		public long		uid;
		public int		ptype;
		public int		plang;
		public String	appid;
		public String	token;
	}

	public static class PCS_PushUserRegRes {
		public int		rc;
	}

	/******************************************************************************/
	public static class PCS_GetPushUserInfo {
		public static final int PCS_GetPushUserInfo_TYPE = 2;
		public long		uid;
	}

	public static class PCS_GetPushUserInfoRes {
		public int		rc;
		public int		ptype;
		public int		plang;
		public String	appid;
		public String	token;
		public long		updateTime;
	}

	/******************************************************************************/

	public static class PCS_UserPushOnoff {
		public static final int PCS_UserPushOnoff_TYPE = 3;
		public long		uid;
		public long		devid;
		public String	nick; //dev remark
		public int		onoff;
	}

	public static class PCS_UserPushOnoffRes {
		public int		rc;
	}

	/******************************************************************************/
	public static class PCS_GetUserPushOnoff {
		public static final int PCS_GetUserPushOnoff_TYPE = 4;
		public long		uid;
	}

	public static class PushOnoffInfo {
		public long		devid;
		public String	nick;
		public int		onoff;
	}

	public static class PCS_GetUserPushOnoffRes {
		public int		rc;
		public List<PushOnoffInfo> devList;
	}
	
}
