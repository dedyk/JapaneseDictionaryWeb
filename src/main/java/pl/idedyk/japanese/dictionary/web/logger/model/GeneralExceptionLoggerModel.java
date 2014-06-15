package pl.idedyk.japanese.dictionary.web.logger.model;

public class GeneralExceptionLoggerModel extends LoggerModelCommon {
	
	private static final long serialVersionUID = 1L;
		
	private int statusCode;
	
	private Throwable throwable;
		
	public GeneralExceptionLoggerModel(LoggerModelCommon loggerModelCommon, int statusCode, Throwable throwable) {		
		super(loggerModelCommon);
		
		this.statusCode = statusCode;
		this.throwable = throwable;		
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
