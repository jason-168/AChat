package com.sl.protocol;

public class Ppushmsg {
	public static class MsgBody {
		public int		mtype; //0:devmsg, 1:user define
		public long		devid;
		public int		ch;
		public String	ctx; //user define json
	}
}
