package com.sl.protocol;

import java.util.List;

public class Puser {
	
	/**
	 * 用户注册请求报文
	 * @author 1
	 *
	 */
	public static class UserRegReq {
		public static final int SL_USERMGR_REQ_REG = 1;
		
		/**
		 * 公司ID.
		 */
		public int commid;
		/**
		 * 用户名
		 */
		public String uname;
		/**
		 * E-mail.
		 */
		public String email;
		/**
		 * 密码
		 */
		public String passwd;
		/**
		 * 手机
		 */
		public String mobile;
		/**
		 * 性别
		 */
		public int sex;
		/**
		 * 年龄
		 */
		public int age;
	}

	/**
	 * 用户注册回复报文
	 * @author 1
	 *
	 */
	public static class UserRegRes {
		/**
		 * 注册结果. 0为成功, 2为已注册过, 其他则为失败.
		 */
		public int rc;
	}

	/**
	 * 用户搜索请求报文
	 * @author 1
	 *
	 */
	public static class UserSearchReq {
		public static final int SL_USERMGR_REQ_USER_SEARCH = 2;
		
		public int msgid;
		/**
		 * 公司ID.
		 */
		public int commid;
		/**
		 * 搜索sid
		 */
		public String name;
		/**
		 * 起止序号
		 */
		public int idx;
		/**
		 * 数量
		 */
		public int limit;
	}

	public static class UserVo {
		/**
		 * 好友UID
		 */
		public long uid;
		/**
		 * 好友公司ID
		 */
		public int commid;
		/**
		 * 好友sid
		 */
		public String uname;
		/**
		 * 好友E-mail
		 */
		public String email;
		/**
		 * 好友rtsm
		 */
		public String mobile;
		/**
		 * 好友昵称
		 */
		public String nick;
		/**
		 * 好友性别
		 */
		public int sex;
		/**
		 * 好友年龄
		 */
		public int age;
	}
	
	/**
	 * 用户搜索回复报文
	 * @author 1
	 *
	 */
	public static class UserSearchRes {
		public int msgid;
		/**
		 * 请求结果, 0为成功, 其他则为失败.
		 */
		public int rc;
		/**
		 * 好友详情列表
		 */
		public List<UserVo> userList;
	}

	/**
	 * 用户信息详情请求报文
	 * @author 1
	 *
	 */
	public static class UserDetailReq {
		public static final int SL_USERMGR_REQ_USER_DETAIL = 8;
		
		public int msgid;
		/**
		 * 好友UID列表.
		 */
		public List<Long> uidList;
	}

	/**
	 * 用户信息详情回复报文
	 * @author 1
	 *
	 */
	public static class UserDetailRes {
		public int msgid;
		/**
		 * 请求结果, 0为成功, 其他则为失败.
		 */
		public int rc;
		/**
		 * 好友详情列表
		 */
		public List<UserVo> userList;
	}

	/**
	 * 设备搜索请求报文
	 * @author 1
	 *
	 */
	public static class DevSearchReq {
		public static final int SL_USERMGR_REQ_DEV_SEARCH = 3;
		
		public int msgid;
		/**
		 * 公司ID.
		 */
		public int commid;
		/**
		 * 设备sid
		 */
		public String name;
		/**
		 * 起止序号
		 */
		public int idx;
		/**
		 * limit
		 */
		public int limit;
	}

	public static class DevVo {
		/**
		 * 设备UID
		 */
		public long devid;
		/**
		 * 设备公司ID
		 */
		public int commid;
		/**
		 * 设备sid
		 */
		public String devname;
		public int type;
		public long vas;
	}
	
	/**
	 * 设备搜索回复报文
	 * @author 1
	 *
	 */
	public static class DevSearchRes {
		public int msgid;
		/**
		 * 请求结果, 0为成功, 其他则为失败.
		 */
		public int rc;
		/**
		 * 设备详情列表
		 */
		public List<DevVo> devList;
	}

	/**
	 * 设备信息详情请求报文
	 * @author 1
	 *
	 */
	public static class DevDetailReq {
		public static final int SL_USERMGR_REQ_DEV_DETAIL = 9;
		
		public int msgid;
		/**
		 * 设备UID列表.
		 */
		public List<Long> uidList;
	}

	/**
	 * 设备信息详情回复报文
	 * @author 1
	 *
	 */
	public static class DevDetailRes {
		public int msgid;
		/**
		 * 请求结果, 0为成功, 其他则为失败.
		 */
		public int rc;
		/**
		 * 设备详情列表
		 */
		public List<DevVo> devList;
	}
}
