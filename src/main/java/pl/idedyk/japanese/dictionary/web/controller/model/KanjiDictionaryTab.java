package pl.idedyk.japanese.dictionary.web.controller.model;

public enum KanjiDictionaryTab {
		
	MEANING("meaningTabLiId"),
	
	RADICALS("radicalsTabLiId"),
	
	DETECT("detectTabLiId");
	
	private String id;

	private KanjiDictionaryTab(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}	
}
