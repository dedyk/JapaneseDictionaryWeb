package pl.idedyk.japanese.dictionary.web.logger.model;

import pl.idedyk.japanese.dictionary2.kanjidic2.xsd.KanjiCharacterInfo;

public class KanjiDictionaryDetailsLoggerModel extends LoggerModelCommon {

	private static final long serialVersionUID = 1L;

	private KanjiCharacterInfo kanjiCharacterInfo;
		
	public KanjiDictionaryDetailsLoggerModel(LoggerModelCommon loggerModelCommon, KanjiCharacterInfo kanjiCharacterInfo) {
		
		super(loggerModelCommon);
		
		this.kanjiCharacterInfo = kanjiCharacterInfo;
	}

	public KanjiCharacterInfo getKanjiCharacterInfo() {
		return kanjiCharacterInfo;
	}

	public void setKanjiCharacterInfo(KanjiCharacterInfo kanjiCharacterInfo) {
		this.kanjiCharacterInfo = kanjiCharacterInfo;
	}

	@Override
	public String toString() {
		return "KanjiDictionaryDetailsLoggerModel [kanjiCharacterInfo=" + kanjiCharacterInfo + "]";
	}	
}
