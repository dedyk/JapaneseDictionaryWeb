package pl.idedyk.japanese.dictionary.web.mysql.model;

public class AndroidSendMissingWordLog {
	
	private Long id;
	
	private Long genericLogId;
	
	private String word;
	
	private String wordPlaceSearch;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getGenericLogId() {
		return genericLogId;
	}

	public void setGenericLogId(Long genericLogId) {
		this.genericLogId = genericLogId;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getWordPlaceSearch() {
		return wordPlaceSearch;
	}

	public void setWordPlaceSearch(String wordPlaceSearch) {
		this.wordPlaceSearch = wordPlaceSearch;
	}
}

