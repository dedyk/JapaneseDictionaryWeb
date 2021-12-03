package pl.idedyk.japanese.dictionary.web.logger.model;

import pl.idedyk.japanese.dictionary2.jmdict.xsd.JMdict;

public class WordDictionary2DetailsLoggerModel extends LoggerModelCommon {

	private static final long serialVersionUID = 1L;

	private JMdict.Entry entry;
		
	public WordDictionary2DetailsLoggerModel(LoggerModelCommon loggerModelCommon, JMdict.Entry entry) {
		
		super(loggerModelCommon);
		
		this.entry = entry;
	}

	public JMdict.Entry getEntry() {
		return entry;
	}

	public void setEntry(JMdict.Entry entry) {
		this.entry = entry;
	}

	@Override
	public String toString() {
		return "WordDictionaryDetailsLoggerModel [entry=" + entry + "]";
	}	
}
