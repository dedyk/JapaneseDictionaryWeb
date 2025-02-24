package pl.idedyk.japanese.dictionary.web.mysql.model;

public class KanjiDictionaryDetailsLog {
	
	private Long id;
	
	private Long genericLogId;

	private Integer kanjiId;
	
	private String kanjiEntryKanji;
	
	private String kanjiEntryTranslateList;
	
	private String kanjiEntryInfo;

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

	public Integer getKanjiEntryId() {
		return kanjiEntryId;
	}

	public void setKanjiEntryId(Integer kanjiEntryId) {
		this.kanjiEntryId = kanjiEntryId;
	}

	public String getKanjiEntryKanji() {
		return kanjiEntryKanji;
	}

	public void setKanjiEntryKanji(String kanjiEntryKanji) {
		this.kanjiEntryKanji = kanjiEntryKanji;
	}

	public String getKanjiEntryTranslateList() {
		return kanjiEntryTranslateList;
	}

	public void setKanjiEntryTranslateList(String kanjiEntryTranslateList) {
		this.kanjiEntryTranslateList = kanjiEntryTranslateList;
	}

	public String getKanjiEntryInfo() {
		return kanjiEntryInfo;
	}

	public void setKanjiEntryInfo(String kanjiEntryInfo) {
		this.kanjiEntryInfo = kanjiEntryInfo;
	}
}
