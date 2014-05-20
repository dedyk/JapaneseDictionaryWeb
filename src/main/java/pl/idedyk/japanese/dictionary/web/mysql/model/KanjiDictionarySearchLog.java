package pl.idedyk.japanese.dictionary.web.mysql.model;

public class KanjiDictionarySearchLog {
	
	private Long id;
	
	private Long genericLogId;
	
	private String findKanjiRequestWord;
	
	private String findKanjiRequestWordPlace;
	
    private Integer findKanjiRequestStrokeCountFrom;
    private Integer findKanjiRequestStrokeCountTo;
    
    private Integer findKanjiResultResultSize;

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

	public String getFindKanjiRequestWord() {
		return findKanjiRequestWord;
	}

	public void setFindKanjiRequestWord(String findKanjiRequestWord) {
		this.findKanjiRequestWord = findKanjiRequestWord;
	}

	public String getFindKanjiRequestWordPlace() {
		return findKanjiRequestWordPlace;
	}

	public void setFindKanjiRequestWordPlace(String findKanjiRequestWordPlace) {
		this.findKanjiRequestWordPlace = findKanjiRequestWordPlace;
	}

	public Integer getFindKanjiRequestStrokeCountFrom() {
		return findKanjiRequestStrokeCountFrom;
	}

	public void setFindKanjiRequestStrokeCountFrom(Integer findKanjiRequestStrokeCountFrom) {
		this.findKanjiRequestStrokeCountFrom = findKanjiRequestStrokeCountFrom;
	}

	public Integer getFindKanjiRequestStrokeCountTo() {
		return findKanjiRequestStrokeCountTo;
	}

	public void setFindKanjiRequestStrokeCountTo(Integer findKanjiRequestStrokeCountTo) {
		this.findKanjiRequestStrokeCountTo = findKanjiRequestStrokeCountTo;
	}

	public Integer getFindKanjiResultResultSize() {
		return findKanjiResultResultSize;
	}

	public void setFindKanjiResultResultSize(Integer findKanjiResultResultSize) {
		this.findKanjiResultResultSize = findKanjiResultResultSize;
	}
}

