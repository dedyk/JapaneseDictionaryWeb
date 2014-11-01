package pl.idedyk.japanese.dictionary.web.logger.model;


public class RedirectLoggerModel extends LoggerModelCommon {
	
	private static final long serialVersionUID = 1L;
	
	private String destinationUrl;
		
	public RedirectLoggerModel(LoggerModelCommon loggerModelCommon, String destinationUrl) {		
		super(loggerModelCommon);
		
		this.destinationUrl = destinationUrl;
	}

	public String getDestinationUrl() {
		return destinationUrl;
	}

	public void setDestinationUrl(String destinationUrl) {
		this.destinationUrl = destinationUrl;
	}
}
