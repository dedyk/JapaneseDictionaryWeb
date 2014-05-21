package pl.idedyk.japanese.dictionary.web.mysql.model;

public class KanjiDictionaryDetectLog {
	
	private Long id;
	
	private Long genericLogId;

	private String strokes;
	
	private String detectKanjiResult;

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

	public String getStrokes() {
		return strokes;
	}

	public void setStrokes(String strokes) {
		this.strokes = strokes;
	}

	public String getDetectKanjiResult() {
		return detectKanjiResult;
	}

	public void setDetectKanjiResult(String detectKanjiResult) {
		this.detectKanjiResult = detectKanjiResult;
	}	
}
