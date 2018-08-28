package pl.idedyk.japanese.dictionary.web.logger.model;

public class KanjiDictionaryAllKanjisLoggerModel extends LoggerModelCommon {

	private static final long serialVersionUID = 1L;

	private boolean withDetails;
	
	private boolean onlyUsed;
	
	public KanjiDictionaryAllKanjisLoggerModel(LoggerModelCommon loggerModelCommon, boolean withDetails, boolean onlyUsed) {
		
		super(loggerModelCommon);
		
		this.withDetails = withDetails;
		this.onlyUsed = onlyUsed;
	}

	public boolean isWithDetails() {
		return withDetails;
	}

	public void setWithDetails(boolean withDetails) {
		this.withDetails = withDetails;
	}

	public boolean isOnlyUsed() {
		return onlyUsed;
	}

	public void setOnlyUsed(boolean onlyUsed) {
		this.onlyUsed = onlyUsed;
	}

	@Override
	public String toString() {
		return "KanjiDictionaryAllKanjisLoggerModel [withDetails=" + withDetails + ", onlyUsed=" + onlyUsed + "]";
	}	
}
