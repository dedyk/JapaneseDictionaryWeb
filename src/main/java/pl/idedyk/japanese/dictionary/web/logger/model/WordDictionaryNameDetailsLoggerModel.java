package pl.idedyk.japanese.dictionary.web.logger.model;

import pl.idedyk.japanese.dictionary2.jmnedict.xsd.JMnedict;

public class WordDictionaryNameDetailsLoggerModel extends LoggerModelCommon {

	private static final long serialVersionUID = 1L;

	private JMnedict.Entry nameDictionaryEntry2;
		
	public WordDictionaryNameDetailsLoggerModel(LoggerModelCommon loggerModelCommon, JMnedict.Entry nameDictionaryEntry2) {
		
		super(loggerModelCommon);
		
		this.nameDictionaryEntry2 = nameDictionaryEntry2;
	}
	
	public JMnedict.Entry getNameDictionaryEntry2() {
		return nameDictionaryEntry2;
	}

	public void setNameDictionaryEntry2(JMnedict.Entry nameDictionaryEntry2) {
		this.nameDictionaryEntry2 = nameDictionaryEntry2;
	}

	@Override
	public String toString() {
		// FM_FIXME: sprawdzic, jak to zachowuje sie
		return "WordDictionaryNameDetailsLoggerModel [nameDictionaryEntry2=" + nameDictionaryEntry2 + "]";
	}	
}
