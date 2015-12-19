package pl.idedyk.japanese.dictionary.web.mysql.model;

public class AndroidGetSpellCheckerSuggestionLog {

	private Long id;
	
	private Long genericLogId;
	
	private String word;

	private String type;
	
	private String spellCheckerSuggestionList;

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSpellCheckerSuggestionList() {
		return spellCheckerSuggestionList;
	}

	public void setSpellCheckerSuggestionList(String spellCheckerSuggestionList) {
		this.spellCheckerSuggestionList = spellCheckerSuggestionList;
	}
}
