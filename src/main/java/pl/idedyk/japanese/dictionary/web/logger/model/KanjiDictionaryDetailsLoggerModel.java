package pl.idedyk.japanese.dictionary.web.logger.model;

import pl.idedyk.japanese.dictionary.api.dto.KanjiEntry;

public class KanjiDictionaryDetailsLoggerModel extends LoggerModelCommon {

	private static final long serialVersionUID = 1L;

	private KanjiEntry kanjiEntry;
		
	public KanjiDictionaryDetailsLoggerModel(String sessionId, String remoteIp, String userAgent, KanjiEntry kanjiEntry) {
		
		super(sessionId, remoteIp, userAgent);
		
		this.kanjiEntry = kanjiEntry;
	}

	public KanjiEntry getKanjiEntry() {
		return kanjiEntry;
	}

	public void setKanjiEntry(KanjiEntry kanjiEntry) {
		this.kanjiEntry = kanjiEntry;
	}

	@Override
	public String toString() {
		return "KanjiDictionaryDetailsLoggerModel [kanjiEntry=" + kanjiEntry + "]";
	}	
}