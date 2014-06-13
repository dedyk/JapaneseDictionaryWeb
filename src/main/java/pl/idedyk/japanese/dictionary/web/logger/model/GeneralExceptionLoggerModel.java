package pl.idedyk.japanese.dictionary.web.logger.model;


public class GeneralExceptionLoggerModel extends LoggerModelCommon {
	
	private static final long serialVersionUID = 1L;
	
	private String requestURI;
	
	private int statusCode;
	
	private Throwable throwable;
		
	public GeneralExceptionLoggerModel(String sessionId, String remoteIp, String userAgent, String requestURI, int statusCode, Throwable throwable) {		
		super(sessionId, remoteIp, userAgent);
		
		this.requestURI = requestURI;
		this.statusCode = statusCode;
		this.throwable = throwable;		
	}

	public String getRequestURI() {
		return requestURI;
	}

	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}
}
