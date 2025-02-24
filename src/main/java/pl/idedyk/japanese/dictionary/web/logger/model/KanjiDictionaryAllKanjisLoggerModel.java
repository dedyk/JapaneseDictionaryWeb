package pl.idedyk.japanese.dictionary.web.logger.model;

public class KanjiDictionaryAllKanjisLoggerModel extends LoggerModelCommon {

	private static final long serialVersionUID = 1L;
	
	private boolean onlyUsed;
	
	public KanjiDictionaryAllKanjisLoggerModel(LoggerModelCommon loggerModelCommon, boolean onlyUsed) {
		
		super(loggerModelCommon);
		
		this.onlyUsed = onlyUsed;
	}
	
	public boolean isOnlyUsed() {
		return onlyUsed;
	}

	public void setOnlyUsed(boolean onlyUsed) {
		this.onlyUsed = onlyUsed;
	}

	@Override
	public String toString() {
		return "KanjiDictionaryAllKanjisLoggerModel [onlyUsed=" + onlyUsed + "]";
	}	
}
