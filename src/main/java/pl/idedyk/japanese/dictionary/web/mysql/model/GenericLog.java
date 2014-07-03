package pl.idedyk.japanese.dictionary.web.mysql.model;

import java.sql.Timestamp;

public class GenericLog {
	
	private Long id;
	
	private Timestamp timestamp;

	private String sessionId;
	
	private String userAgent;
	
	private String requestURL;
	
	private String refererURL;
	
	private String remoteIp;
	
	private String remoteHost;
	
	private GenericLogOperationEnum operation;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getRequestURL() {
		return requestURL;
	}

	public void setRequestURL(String requestURL) {
		this.requestURL = requestURL;
	}

	public String getRefererURL() {
		return refererURL;
	}

	public void setRefererURL(String refererURL) {
		this.refererURL = refererURL;
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

	public GenericLogOperationEnum getOperation() {
		return operation;
	}

	public void setOperation(GenericLogOperationEnum operation) {
		this.operation = operation;
	}
}
