package com.sl.protocol;

import java.util.List;

public class PStorageInfo {

	public static class StorageFileInfo{
		public String	fname;
		//	Ftype int    `json:"ftype"` //0:urt 1:mp4 2:rtmp
		public int		ch;
		public int		loc;   //0:flash 1:cloud
		public int		stype; //storage type 1:alarm, 2:7x24
		public int		alarm; //no alarm trigger default 0
		public int		ofs;   //offset
		public long		tm;    //Timestamp second
	}
	
	public static class PCS_StorageFileInfoDown{
		public static final int SL_STORAGEMGR_REQ_FILES = 3;

		public long		devid;
		public long		tm; //Timestamp second
		public int		dt; //direction 0:backward,desc; 1:forward,asc
		public int		idx;
		public int		limit; //[1-1000]
	}
	
	public static class PCS_StorageFileInfoDownRes{
		public int		msgid;
		public int		rc;
		public long		devid;
		public List<StorageFileInfo> flist;
	}
}
