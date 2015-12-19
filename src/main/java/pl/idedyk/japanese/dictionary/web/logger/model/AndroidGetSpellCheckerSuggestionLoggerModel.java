package pl.idedyk.japanese.dictionary.web.logger.model;

import java.util.List;

public class AndroidGetSpellCheckerSuggestionLoggerModel extends LoggerModelCommon {
	
	private static final long serialVersionUID = 1L;

	private String word;
	
	private String type;
	
	private List<String> spellCheckerSuggestionList;
	
	public AndroidGetSpellCheckerSuggestionLoggerModel(LoggerModelCommon loggerModelCommon, String word, String type, List<String> spellCheckerSuggestionList) {
		
		super(loggerModelCommon);
		
		this.word = word;
		this.type = type;
		
		this.spellCheckerSuggestionList = spellCheckerSuggestionList;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<String> getSpellCheckerSuggestionList() {
		return spellCheckerSuggestionList;
	}

	public void setSpellCheckerSuggestionList(List<String> spellCheckerSuggestionList) {
		this.spellCheckerSuggestionList = spellCheckerSuggestionList;
	}
}
