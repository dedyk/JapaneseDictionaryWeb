package pl.idedyk.japanese.dictionary.web.logger.model;

public class KanjiDictionarySearchStrokeCountLoggerModel extends LoggerModelCommon {

	private static final long serialVersionUID = 1L;

	private int from;
	
	private int to;
	
	public KanjiDictionarySearchStrokeCountLoggerModel(LoggerModelCommon loggerModelCommon, int from, int to) {
		
		super(loggerModelCommon);
		
		this.from = from;
		this.to = to;
	}

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public int getTo() {
		return to;
	}

	public void setTo(int to) {
		this.to = to;
	}

	@Override
	public String toString() {
		return "KanjiDictionarySearchFromStrokeCountLoggerModel [from=" + from + ", to=" + to + "]";
	}	
}
