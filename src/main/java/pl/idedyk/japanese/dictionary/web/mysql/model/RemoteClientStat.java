package pl.idedyk.japanese.dictionary.web.mysql.model;

public class RemoteClientStat {

	private String remoteIp;
	
	private String remoteHost;
	
	private long stat;

	public String getRemoteIp() {
		return remoteIp;
	}

	public void setRemoteIp(String remoteIp) {
		this.remoteIp = remoteIp;
	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	public long getStat() {
		return stat;
	}

	public void setStat(long stat) {
		this.stat = stat;
	}
}
