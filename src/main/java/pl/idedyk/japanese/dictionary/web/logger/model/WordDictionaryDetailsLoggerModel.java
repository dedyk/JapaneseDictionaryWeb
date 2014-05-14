package pl.idedyk.japanese.dictionary.web.logger.model;

import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;

public class WordDictionaryDetailsLoggerModel extends LoggerModelCommon {

	private static final long serialVersionUID = 1L;

	private DictionaryEntry dictionaryEntry;
		
	public WordDictionaryDetailsLoggerModel(String sessionId, String remoteIp, String remoteHost, DictionaryEntry dictionaryEntry) {
		
		super(sessionId, remoteIp, remoteHost);
		
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
		return "WordDictionaryDetailsLoggerModel [dictionaryEntry=" + dictionaryEntry + "]";
	}	
}
