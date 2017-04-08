package com.sl.achat;

public class PreconnInfo {

	public static class UPNPInfo {
		public String extip;
		public int tcpp;
		public int udpp;
	}

	public static class NATInfo {
		public int type;
		public int mode;
		public String extip;
		public String intip;
		public int extp;
		public int intp;
	}

	public int currentflow;

	public int preconn_mode;
	public String preconn_ip;
	public int preconn_port;
	
	public int preconn_punchf;

	public int preconn_status_req_ms;
	public int preconn_punchinfo_req_ms;
	public int preconn_lanseach_req_ms;
	public int preconn_natinfo_req_ms;
	public int preconn_punch_ms;
	public int preconn_usingtime;

	public UPNPInfo localUPNPInfo;
	public NATInfo localNATInfo;

	public String localExtIP;
	public int localExtTCPport;

	public UPNPInfo peerUPNPInfo;
	public NATInfo peerNATInfo;

	public String peerExtIP;
	public int peerExtTCPport;
}
