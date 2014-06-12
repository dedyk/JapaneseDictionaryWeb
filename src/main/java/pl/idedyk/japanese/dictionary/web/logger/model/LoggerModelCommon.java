package pl.idedyk.japanese.dictionary.web.logger.model;

import java.io.Serializable;
import java.util.Date;

public abstract class LoggerModelCommon implements Serializable {

	private static final long serialVersionUID = 1L;

	private String sessionId;
	
	private String remoteIp;
	
	private String userAgent;
	
	private Date date;

	public LoggerModelCommon(String sessionId, String remoteIp, String userAgent) {
		this.sessionId = sessionId;
		this.remoteIp = remoteIp;
		this.userAgent = userAgent;
		
		date = new Date();
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

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}
