package pl.idedyk.japanese.dictionary.web.logger.model;

import java.util.List;

import pl.idedyk.japanese.dictionary2.kanjidic2.xsd.KanjiCharacterInfo;

public class KanjiDictionaryGetKanjiCharacterInfoListLoggerModel extends LoggerModelCommon {

	private static final long serialVersionUID = 1L;
	
	private List<KanjiCharacterInfo> kanjiCharacterInfoList;
	
	public KanjiDictionaryGetKanjiCharacterInfoListLoggerModel(LoggerModelCommon loggerModelCommon, List<KanjiCharacterInfo> kanjiCharacterInfoList) {
		
		super(loggerModelCommon);
		
		this.kanjiCharacterInfoList = kanjiCharacterInfoList;
	}

	public List<KanjiCharacterInfo> getKanjiCharacterInfoList() {
		return kanjiCharacterInfoList;
	}

	public void setKanjiCharacterInfoList(List<KanjiCharacterInfo> kanjiCharacterInfoList) {
		this.kanjiCharacterInfoList = kanjiCharacterInfoList;
	}

	@Override
	public String toString() {
		return "KanjiDictionaryGetKanjiCharacterInfoListLoggerModel [kanjiCharacterInfoList=" + kanjiCharacterInfoList + "]";
	}
}
