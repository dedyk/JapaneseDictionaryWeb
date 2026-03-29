package pl.idedyk.japanese.dictionary.web.logger.model;

import java.io.Serializable;
import java.util.Date;

import pl.idedyk.japanese.dictionary.web.common.ClientInfo;

public class LoggerModelCommon implements Serializable {

	private static final long serialVersionUID = 1L;

	private String sessionId;
	
	private String userAgent;
	
	private String requestURL;
	private String refererURL;
	
	private Date date;
	
	private ClientInfo clientInfo;
	
	private LoggerModelCommon() {
	}
	
	public LoggerModelCommon(LoggerModelCommon loggerModelCommon) {
		
		if (loggerModelCommon != null) {

			this.sessionId = loggerModelCommon.sessionId;
			this.userAgent = loggerModelCommon.userAgent;
			this.requestURL = loggerModelCommon.requestURL;
			this.refererURL = loggerModelCommon.refererURL;
			
			this.date = loggerModelCommon.date;
			
			this.clientInfo = loggerModelCommon.clientInfo;
			
		} else {
			this.date = new Date();
		}
	}

	public static LoggerModelCommon createLoggerModelCommon(String sessionId,
			String remoteIp, String userAgent,
			String requestURL, String refererURL,
			ClientInfo clientInfo) {
		
		LoggerModelCommon loggerModelCommon = new LoggerModelCommon();
		
		loggerModelCommon.sessionId = sessionId;
		loggerModelCommon.userAgent = userAgent;
		loggerModelCommon.requestURL = requestURL;
		loggerModelCommon.refererURL = refererURL;
		
		loggerModelCommon.date = new Date();
		
		loggerModelCommon.clientInfo = clientInfo;
		
		return loggerModelCommon;
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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
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

	public ClientInfo getClientInfo() {
		return clientInfo;
	}

	public void setClientInfo(ClientInfo clientInfo) {
		this.clientInfo = clientInfo;
	}
}
