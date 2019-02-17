package pl.idedyk.japanese.dictionary.web.mysql.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class WordDictionaryUniqueSearch implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	
	private String word;
	
	private int counter;
	
	private Timestamp firstAppearanceTimestamp;
	private Timestamp lastAppearanceTimestamp;
		
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	public Timestamp getFirstAppearanceTimestamp() {
		return firstAppearanceTimestamp;
	}

	public void setFirstAppearanceTimestamp(Timestamp firstAppearanceTimestamp) {
		this.firstAppearanceTimestamp = firstAppearanceTimestamp;
	}

	public Timestamp getLastAppearanceTimestamp() {
		return lastAppearanceTimestamp;
	}

	public void setLastAppearanceTimestamp(Timestamp lastAppearanceTimestamp) {
		this.lastAppearanceTimestamp = lastAppearanceTimestamp;
	}
}
