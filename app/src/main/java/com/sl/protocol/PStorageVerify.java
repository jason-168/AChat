package com.sl.protocol;

import java.util.List;

public class PStorageVerify {

	public static class StorageVasInfo{
		public long		devid;
		public int		ch;
		public int		stype;//STORAGE_TYPE_
	}

	/******************************************************************************/
	public static class PCS_StorageVas4User {
		public static final int SL_STORAGEMGR_REQ_VAS4USER = 1;
		public int		msgid;
	}

	public static class PCS_StorageVas4UserRes {
		public int		msgid;
		public int		rc;
		public List<StorageVasInfo> vlist;
	}
}
