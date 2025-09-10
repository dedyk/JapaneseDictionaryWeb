package pl.idedyk.japanese.dictionary.web.logger.model;

import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.JMdict;

public class WordDictionaryDetailsLoggerModel extends LoggerModelCommon {

	private static final long serialVersionUID = 1L;

	private DictionaryEntry dictionaryEntry;
	private JMdict.Entry dictionaryEntry2;
		
	public WordDictionaryDetailsLoggerModel(LoggerModelCommon loggerModelCommon, DictionaryEntry dictionaryEntry, JMdict.Entry dictionaryEntry2) {
		
		super(loggerModelCommon);
		
		this.dictionaryEntry = dictionaryEntry;
		this.dictionaryEntry2 = dictionaryEntry2;
	}

	public DictionaryEntry getDictionaryEntry() {
		return dictionaryEntry;
	}

	public void setDictionaryEntry(DictionaryEntry dictionaryEntry) {
		this.dictionaryEntry = dictionaryEntry;
	}

	public JMdict.Entry getDictionaryEntry2() {
		return dictionaryEntry2;
	}

	public void setDictionaryEntry2(JMdict.Entry dictionaryEntry2) {
		this.dictionaryEntry2 = dictionaryEntry2;
	}

	@Override
	public String toString() {
		return "WordDictionaryDetailsLoggerModel [dictionaryEntry=" + dictionaryEntry + "]";
	}	
}
