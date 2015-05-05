package pl.idedyk.japanese.dictionary.web.mysql.model;

public class WordDictionarySearchLog {
	
	private Long id;
	
	private Long genericLogId;
	
	private String findWordRequestWord;
	
	private Boolean findWordRequestKanji;
	private Boolean findWordRequestKana;
	private Boolean findWordRequestRomaji;
	private Boolean findWordRequestTranslate;
	private Boolean findWordRequestInfo;
	
	private Boolean findWordRequestOnlyCommonWords;
	
	private String findWordRequestWordPlace;

	private String findWordRequestDictionaryEntryTypeList;
		
	private Integer findWordResultResultSize;
	
	private int priority;
	
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

	public String getFindWordRequestWord() {
		return findWordRequestWord;
	}

	public void setFindWordRequestWord(String findWordRequestWord) {
		this.findWordRequestWord = findWordRequestWord;
	}

	public Boolean getFindWordRequestKanji() {
		return findWordRequestKanji;
	}

	public void setFindWordRequestKanji(Boolean findWordRequestKanji) {
		this.findWordRequestKanji = findWordRequestKanji;
	}

	public Boolean getFindWordRequestKana() {
		return findWordRequestKana;
	}

	public void setFindWordRequestKana(Boolean findWordRequestKana) {
		this.findWordRequestKana = findWordRequestKana;
	}

	public Boolean getFindWordRequestRomaji() {
		return findWordRequestRomaji;
	}

	public void setFindWordRequestRomaji(Boolean findWordRequestRomaji) {
		this.findWordRequestRomaji = findWordRequestRomaji;
	}

	public Boolean getFindWordRequestTranslate() {
		return findWordRequestTranslate;
	}

	public void setFindWordRequestTranslate(Boolean findWordRequestTranslate) {
		this.findWordRequestTranslate = findWordRequestTranslate;
	}

	public Boolean getFindWordRequestInfo() {
		return findWordRequestInfo;
	}

	public void setFindWordRequestInfo(Boolean findWordRequestInfo) {
		this.findWordRequestInfo = findWordRequestInfo;
	}

	public Boolean getFindWordRequestOnlyCommonWords() {
		return findWordRequestOnlyCommonWords;
	}

	public void setFindWordRequestOnlyCommonWords(Boolean findWordRequestOnlyCommonWords) {
		this.findWordRequestOnlyCommonWords = findWordRequestOnlyCommonWords;
	}

	public String getFindWordRequestWordPlace() {
		return findWordRequestWordPlace;
	}

	public void setFindWordRequestWordPlace(String findWordRequestWordPlace) {
		this.findWordRequestWordPlace = findWordRequestWordPlace;
	}

	public String getFindWordRequestDictionaryEntryTypeList() {
		return findWordRequestDictionaryEntryTypeList;
	}

	public void setFindWordRequestDictionaryEntryTypeList(String findWordRequestDictionaryEntryTypeList) {
		this.findWordRequestDictionaryEntryTypeList = findWordRequestDictionaryEntryTypeList;
	}

	public Integer getFindWordResultResultSize() {
		return findWordResultResultSize;
	}

	public void setFindWordResultResultSize(Integer findWordResultResultSize) {
		this.findWordResultResultSize = findWordResultResultSize;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
}

