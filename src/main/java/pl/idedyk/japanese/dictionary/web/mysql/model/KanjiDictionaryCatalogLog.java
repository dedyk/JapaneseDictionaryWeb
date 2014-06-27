package pl.idedyk.japanese.dictionary.web.mysql.model;

public class KanjiDictionaryCatalogLog {
	
	private Long id;
	
	private Long genericLogId;
	
	private int pageNo;

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

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}
}

