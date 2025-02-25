package pl.idedyk.japanese.dictionary.web.logger.model;

import java.util.List;

import pl.idedyk.japanese.dictionary2.kanjidic2.xsd.KanjiCharacterInfo;

public class KanjiDictionaryGetKanjiEntryListLoggerModel extends LoggerModelCommon {

	private static final long serialVersionUID = 1L;
	
	private List<KanjiCharacterInfo> kanjiEntryList;
	
	public KanjiDictionaryGetKanjiEntryListLoggerModel(LoggerModelCommon loggerModelCommon, List<KanjiCharacterInfo> kanjiEntryList) {
		
		super(loggerModelCommon);
		
		this.kanjiEntryList = kanjiEntryList;
	}

	public List<KanjiCharacterInfo> getKanjiEntryList() {
		return kanjiEntryList;
	}

	public void setKanjiEntryList(List<KanjiCharacterInfo> kanjiEntryList) {
		this.kanjiEntryList = kanjiEntryList;
	}

	@Override
	public String toString() {
		return "KanjiDictionaryGetKanjiEntryListLoggerModel [kanjiEntryList=" + kanjiEntryList + "]";
	}
}
