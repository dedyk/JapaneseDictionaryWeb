package pl.idedyk.japanese.dictionary.web.logger.model;

import java.util.List;

import pl.idedyk.japanese.dictionary.api.dto.KanjiEntry;

public class KanjiDictionaryGetKanjiEntryListLoggerModel extends LoggerModelCommon {

	private static final long serialVersionUID = 1L;
	
	private List<KanjiEntry> kanjiEntryList;
	
	public KanjiDictionaryGetKanjiEntryListLoggerModel(LoggerModelCommon loggerModelCommon, List<KanjiEntry> kanjiEntryList) {
		
		super(loggerModelCommon);
		
		this.kanjiEntryList = kanjiEntryList;
	}

	public List<KanjiEntry> getKanjiEntryList() {
		return kanjiEntryList;
	}

	public void setKanjiEntryList(List<KanjiEntry> kanjiEntryList) {
		this.kanjiEntryList = kanjiEntryList;
	}

	@Override
	public String toString() {
		return "KanjiDictionaryGetKanjiEntryListLoggerModel [kanjiEntryList=" + kanjiEntryList + "]";
	}
}
