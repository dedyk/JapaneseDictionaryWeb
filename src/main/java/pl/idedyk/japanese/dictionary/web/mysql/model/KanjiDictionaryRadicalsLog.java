package pl.idedyk.japanese.dictionary.web.mysql.model;

public class KanjiDictionaryRadicalsLog {
	
	private Long id;
	
	private Long genericLogId;

	private String radicals;
	
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

	public String getRadicals() {
		return radicals;
	}

	public void setRadicals(String radicals) {
		this.radicals = radicals;
	}

	public Integer getFoundElements() {
		return foundElements;
	}

	public void setFoundElements(Integer foundElements) {
		this.foundElements = foundElements;
	}
}
