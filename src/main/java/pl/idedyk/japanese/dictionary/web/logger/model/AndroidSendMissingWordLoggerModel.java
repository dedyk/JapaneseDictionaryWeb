package pl.idedyk.japanese.dictionary.web.logger.model;

import pl.idedyk.japanese.dictionary.api.dictionary.dto.WordPlaceSearch;

public class AndroidSendMissingWordLoggerModel extends LoggerModelCommon {

	private static final long serialVersionUID = 1L;

	private String word;
	
	private WordPlaceSearch wordPlaceSearch;
	
	public AndroidSendMissingWordLoggerModel(LoggerModelCommon loggerModelCommon, String word, WordPlaceSearch wordPlaceSearch) {
		
		super(loggerModelCommon);
		
		this.word = word;
		this.wordPlaceSearch = wordPlaceSearch;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public WordPlaceSearch getWordPlaceSearch() {
		return wordPlaceSearch;
	}

	public void setWordPlaceSearch(WordPlaceSearch wordPlaceSearch) {
		this.wordPlaceSearch = wordPlaceSearch;
	}
}
