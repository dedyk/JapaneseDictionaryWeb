package pl.idedyk.japanese.dictionary.web.logger.model;

import pl.idedyk.japanese.dictionary2.kanjidic2.xsd.KanjiCharacterInfo;

public class KanjiDictionaryDetailsLoggerModel extends LoggerModelCommon {

	private static final long serialVersionUID = 1L;

	private KanjiCharacterInfo kanjiEntry;
		
	public KanjiDictionaryDetailsLoggerModel(LoggerModelCommon loggerModelCommon, KanjiCharacterInfo kanjiEntry) {
		
		super(loggerModelCommon);
		
		this.kanjiEntry = kanjiEntry;
	}

	public KanjiCharacterInfo getKanjiEntry() {
		return kanjiEntry;
	}

	public void setKanjiEntry(KanjiCharacterInfo kanjiEntry) {
		this.kanjiEntry = kanjiEntry;
	}

	@Override
	public String toString() {
		return "KanjiDictionaryDetailsLoggerModel [kanjiEntry=" + kanjiEntry + "]";
	}	
}
