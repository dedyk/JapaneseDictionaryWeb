package pl.idedyk.japanese.dictionary.web.mysql.model;

public class WordDictionaryAutocomplete {

	private Long id;
	
	private Long genericLogId;
	
	private String term;
	
	private Integer foundElements;

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

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public Integer getFoundElements() {
		return foundElements;
	}

	public void setFoundElements(Integer foundElements) {
		this.foundElements = foundElements;
	}	
}
