package pl.idedyk.japanese.dictionary.web.logger.model;


public class WordDictionaryStartLoggerModel extends LoggerModelCommon {
	
	private static final long serialVersionUID = 1L;
		
	public WordDictionaryStartLoggerModel(String sessionId, String remoteIp, String userAgent) {		
		super(sessionId, remoteIp, userAgent);
	}
}