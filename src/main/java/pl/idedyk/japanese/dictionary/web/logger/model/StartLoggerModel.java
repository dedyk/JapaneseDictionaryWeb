package pl.idedyk.japanese.dictionary.web.logger.model;


public class StartLoggerModel extends LoggerModelCommon {
	
	private static final long serialVersionUID = 1L;
		
	public StartLoggerModel(String sessionId, String remoteIp, String userAgent) {		
		super(sessionId, remoteIp, userAgent);
	}
}
