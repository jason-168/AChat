package com.sl.protocol;

import java.util.Set;

public class Pbuddy {

	/**
	 * 好友添加请求报文
	 * @author 1
	 *
	 */
	public static class BuddyAddReq {
		public static final int SL_USERMGR_REQ_ADD = 4;

		public int msgid;
		/**
		 * 自己的UID
		 */
		public long uid;
		/**
		 * 要添加的好友的UID, 从用户/设备搜索得来.
		 */
		public long bid;
		
		/**
		 * 好友sid
		 */
		public String uname;
		
		/**
		 * 留言信息
		 */
		public String msg;
	}

	/**
	 * 好友添加回复报文
	 * @author 1
	 *
	 */
	public static class BuddyAddRes {
		public static final int SL_USERMGR_RES_ADD = 34;
		
		public int msgid;
		/**
		 * 自己的UID
		 */
		public long uid;
		/**
		 * 要添加的好友的UID, 从用户/设备搜索得来.
		 */
		public long bid;
		/**
		 * 0:accept 1:deny 2:omit
		 */
		public int rc;
		
		/**
		 * 好友sid
		 */
		public String uname;
	}

	/**
	 * 好友删除请求报文
	 * @author 1
	 *
	 */
	public static class BuddyDelReq {
		public static final int SL_USERMGR_REQ_DEL = 5;

		public int msgid;
		/**
		 * 自己的UID
		 */
		public long uid;
		/**
		 * 要删除的好友的UID.
		 */
		public long bid;
	}

	/**
	 * 好友删除回复报文
	 * @author 1
	 *
	 */
	public static class BuddyDelRes {
		public int msgid;
		/**
		 * 请求结果, 0为成功, 其他则为失败.
		 */
		public int rc;
	}

	/**
	 * 请求好友列表报文
	 * @author 1
	 *
	 */
	public static class BuddyListReq {
		public static final int SL_USERMGR_REQ_BUDDY_LIST = 7;

		public int msgid;
		/**
		 * 自己的UID
		 */
		public long uid;
		/**
		 * 好友的类型, 0为APP, 1为DEV
		 */
		public int btype;
	}

	/**
	 * 请求好友列表回复报文
	 * @author 1
	 *
	 */
	public static class BuddyListRes {
		public int msgid;
		/**
		 * 好友UID列表.
		 */
		public Set<Long> bidSet;
	}

}
