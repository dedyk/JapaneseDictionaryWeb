package pl.idedyk.japanese.dictionary.web.controller.model;


public class KanjiDictionarySearchModel {
	
	private String word;
	
	private String wordPlace;
	
	private String strokeCountFrom;
	private String strokeCountTo;
		
	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getWordPlace() {
		return wordPlace;
	}

	public void setWordPlace(String wordPlace) {
		this.wordPlace = wordPlace;
	}

	public String getStrokeCountFrom() {
		return strokeCountFrom;
	}

	public void setStrokeCountFrom(String strokeCountFrom) {
		this.strokeCountFrom = strokeCountFrom;
	}

	public String getStrokeCountTo() {
		return strokeCountTo;
	}

	public void setStrokeCountTo(String strokeCountTo) {
		this.strokeCountTo = strokeCountTo;
	}
}
