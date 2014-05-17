package pl.idedyk.japanese.dictionary.web.logger.model;

import java.io.Serializable;

public abstract class LoggerModelCommon implements Serializable {

	private static final long serialVersionUID = 1L;

	private String sessionId;
	
	private String remoteIp;

	public LoggerModelCommon(String sessionId, String remoteIp) {
		this.sessionId = sessionId;
		this.remoteIp = remoteIp;
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
}
