package pl.idedyk.japanese.dictionary.web.common;

import java.io.Serializable;

import pl.idedyk.japanese.dictionary.web.config.xsd.HostBlockOperation;

public class ClientInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static String REQUEST_ATTRIBUTE = "clientInfo";
	
	public String ip;
	public String hostName;
	public String userAgent;	
	public String url;
	public String httpMethod;
	
	public String fullUrl;
	
	public String autonomousSystemNumber;
	public String autonomousSystemOrganization;
	public String country;
	
	public Integer blackListLevel;

	public HostBlockOperation hostBlockOperation = null;
	public Integer hostBlockTime = null;
	public boolean doSendToLoggerListener = false;
}

