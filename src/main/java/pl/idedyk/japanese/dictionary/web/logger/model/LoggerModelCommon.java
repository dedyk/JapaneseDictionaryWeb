package pl.idedyk.japanese.dictionary.web.logger.model;

import java.io.Serializable;

public class LoggerModelCommon implements Serializable {

	private static final long serialVersionUID = 1L;

	private String sessionId;
	
	private String remoteIp;
	
	private String remoteHost;

	public LoggerModelCommon(String sessionId, String remoteIp, String remoteHost) {
		this.sessionId = sessionId;
		this.remoteIp = remoteIp;
		this.remoteHost = remoteHost;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

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
}
