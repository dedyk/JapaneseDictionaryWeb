package pl.idedyk.japanese.dictionary.web.logger.model;

import java.io.Serializable;

import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;

public class WordDictionaryDetailsLoggerModel implements Serializable {

	private static final long serialVersionUID = 1L;

	private DictionaryEntry dictionaryEntry;
	
	public WordDictionaryDetailsLoggerModel() {
	}
	
	public WordDictionaryDetailsLoggerModel(DictionaryEntry dictionaryEntry) {
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
