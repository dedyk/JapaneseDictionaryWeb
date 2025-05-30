package pl.idedyk.japanese.dictionary.web.logger.model;

import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;

public class WordDictionaryNameDetailsLoggerModel extends LoggerModelCommon {

	private static final long serialVersionUID = 1L;

	private DictionaryEntry dictionaryEntry;
		
	public WordDictionaryNameDetailsLoggerModel(LoggerModelCommon loggerModelCommon, DictionaryEntry dictionaryEntry) {
		
		super(loggerModelCommon);
		
		this.dictionaryEntry = dictionaryEntry;
	}

	public DictionaryEntry getDictionaryEntry() {
		return dictionaryEntry;
	}

	public void setDictionaryEntry(DictionaryEntry dictionaryEntry) {
		this.dictionaryEntry = dictionaryEntry;
	}

	@Override
	public String toString() {
		return "WordDictionaryNameDetailsLoggerModel [dictionaryEntry=" + dictionaryEntry + "]";
	}	
}
