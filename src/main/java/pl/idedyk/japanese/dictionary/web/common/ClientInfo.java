package pl.idedyk.japanese.dictionary.web.common;

import java.io.Serializable;

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
	public String country;

	public boolean doBlock = false;
	public boolean doBlockSendRandomData = false;
}

