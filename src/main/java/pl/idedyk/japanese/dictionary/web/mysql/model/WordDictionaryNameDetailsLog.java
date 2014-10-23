package pl.idedyk.japanese.dictionary.web.mysql.model;

public class WordDictionaryNameDetailsLog {

	private Long id;
	
	private Long genericLogId;

	private Integer dictionaryEntryId;
	
	private String dictionaryEntryKanji;
	
	private String dictionaryEntryKanaList;
	
	private String dictionaryEntryRomajiList;
	
	private String dictionaryEntryTranslateList;
	
	private String dictionaryEntryInfo;

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

	public Integer getDictionaryEntryId() {
		return dictionaryEntryId;
	}

	public void setDictionaryEntryId(Integer dictionaryEntryId) {
		this.dictionaryEntryId = dictionaryEntryId;
	}

	public String getDictionaryEntryKanji() {
		return dictionaryEntryKanji;
	}

	public void setDictionaryEntryKanji(String dictionaryEntryKanji) {
		this.dictionaryEntryKanji = dictionaryEntryKanji;
	}

	public String getDictionaryEntryKanaList() {
		return dictionaryEntryKanaList;
	}

	public void setDictionaryEntryKanaList(String dictionaryEntryKanaList) {
		this.dictionaryEntryKanaList = dictionaryEntryKanaList;
	}

	public String getDictionaryEntryRomajiList() {
		return dictionaryEntryRomajiList;
	}

	public void setDictionaryEntryRomajiList(String dictionaryEntryRomajiList) {
		this.dictionaryEntryRomajiList = dictionaryEntryRomajiList;
	}

	public String getDictionaryEntryTranslateList() {
		return dictionaryEntryTranslateList;
	}

	public void setDictionaryEntryTranslateList(String dictionaryEntryTranslateList) {
		this.dictionaryEntryTranslateList = dictionaryEntryTranslateList;
	}

	public String getDictionaryEntryInfo() {
		return dictionaryEntryInfo;
	}

	public void setDictionaryEntryInfo(String dictionaryEntryInfo) {
		this.dictionaryEntryInfo = dictionaryEntryInfo;
	}	
}
