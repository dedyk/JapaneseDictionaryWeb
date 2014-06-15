package pl.idedyk.japanese.dictionary.web.logger.model;


public class WordDictionaryAutocompleteLoggerModel extends LoggerModelCommon {
	
	private static final long serialVersionUID = 1L;

	private String term;
	
	private int foundElemets;
		
	public WordDictionaryAutocompleteLoggerModel(LoggerModelCommon loggerModelCommon, String term, int foundElemets) {
		
		super(loggerModelCommon);
		
		this.term = term;
		this.foundElemets = foundElemets;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public int getFoundElemets() {
		return foundElemets;
	}

	public void setFoundElemets(int foundElemets) {
		this.foundElemets = foundElemets;
	}

	@Override
	public String toString() {
		return "WordDictionaryAutocompleLoggerModel [term=" + term + ", foundElemets=" + foundElemets + "]";
	}
}
